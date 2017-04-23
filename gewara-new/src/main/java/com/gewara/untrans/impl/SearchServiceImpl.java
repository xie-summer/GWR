/**  
 * @Project: shanghai
 * @Title: SearchServiceImpl.java
 * @Package com.gewara.service.impl
 * @author paul.wei2011@gmail.com
 * @date Aug 10, 2012 6:00:05 PM
 * @version V1.0  
 */

package com.gewara.untrans.impl;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.stereotype.Service;

import com.gewara.Config;
import com.gewara.constant.Status;
import com.gewara.json.GewaSearchKey;
import com.gewara.model.BaseObject;
import com.gewara.model.bbs.Diary;
import com.gewara.model.bbs.commu.Commu;
import com.gewara.model.bbs.qa.GewaQuestion;
import com.gewara.model.common.BaseInfo;
import com.gewara.model.content.News;
import com.gewara.model.content.Video;
import com.gewara.model.drama.Drama;
import com.gewara.model.drama.DramaStar;
import com.gewara.model.drama.Theatre;
import com.gewara.model.movie.Cinema;
import com.gewara.model.movie.Movie;
import com.gewara.model.sport.Sport;
import com.gewara.model.sport.SportItem;
import com.gewara.service.DaoService;
import com.gewara.service.PlaceService;
import com.gewara.untrans.SearchService;
import com.gewara.util.BeanUtil;
import com.gewara.util.DateUtil;
import com.gewara.util.HttpResult;
import com.gewara.util.HttpUtils;
import com.gewara.util.JsonUtils;
import com.gewara.util.PinYinUtils;
import com.gewara.util.StringUtil;
import com.gewara.util.VmBaseUtil;
import com.gewara.util.VmUtils;

/**
 * @ClassName SearchServiceImpl
 * @Description 站内搜索服务
 * @author weihonglin pau.wei2011@gmail.com
 * @date Aug 10, 2012
 */

@Service("searchService")
public class SearchServiceImpl implements SearchService {
	private static final transient Logger logger = LoggerFactory.getLogger(SearchServiceImpl.class);
	@Autowired
	@Qualifier("config")
	private Config config;
	@Autowired
	@Qualifier("daoService")
	private DaoService daoService;
	@Autowired
	@Qualifier("hibernateTemplate")
	private HibernateTemplate hibernateTemplate;

	@Autowired
	@Qualifier("placeService")
	protected PlaceService placeService;

	@Override
	public Map<String, Object> searchKey(String ip, String citycode, String skey, String channel, String tag, String category, Integer pageNo) {
		return searchKey(ip, citycode, skey, channel, tag, category, pageNo, ROWS_PER_PAGE);
	}

	@Override
	public Map<String, Object> searchKey(String ip, String citycode, String skey, String channel, String tag, String category, Integer pageNo,
			Integer rowsPerPage) {
		Map<String, Object> map = new HashMap();
		Map<String, String> params = new HashMap<String, String>();
		params.put("ip", ip != null ? ip : "");
		params.put("citycode", StringUtils.isNotBlank(citycode) ? citycode : "");
		params.put("skey", StringUtils.isNotBlank(skey) ? skey : "");
		params.put("channel", StringUtils.isNotBlank(channel) ? channel : "");
		params.put("tag", StringUtils.isNotBlank(tag) ? tag : "");
		params.put("category", StringUtils.isNotBlank(category) ? category : "");
		params.put("pageNo", pageNo != null ? String.valueOf(pageNo) : "0");
		params.put("rowsPerPage", rowsPerPage + "");
		HttpResult result = HttpUtils.postUrlAsString(config.getString("searchUrl") + API_SEARCH_SEARCHKEY, params);
		if (result.isSuccess()) {
			map = JsonUtils.readJsonToMap(result.getResponse());
			List<GewaSearchKey> skList = JsonUtils.readJsonToObjectList(GewaSearchKey.class, (String) map.get(ROWS_SK_LIST));
			map.put(ROWS_SK_LIST, skList);
			return map;
		} else {
			logger.error("excption search conection:" + result.getMsg());
			return map;
		}
	}

	@Override
	public Set<String> searchKey(String citycode, String channel, String tag, String category, String skey, int maxnum) {
		Set<String> skListStr = new LinkedHashSet<String>(maxnum);
		Map<String, String> params = new HashMap<String, String>();
		params.put("num", String.valueOf(maxnum));
		params.put("citycode", StringUtils.isNotBlank(citycode) ? citycode : "");
		params.put("channel", channel != null ? channel : "");
		params.put("tag", StringUtils.isNotBlank(tag) ? tag : "");
		params.put("category", StringUtils.isNotBlank(category) ? category : "");
		params.put("skey", skey != null ? skey : "");
		HttpResult result = HttpUtils.postUrlAsString(config.getString("searchUrl") + API_SEARCH_SEARCHKEY_NUM, params);
		if (result.isSuccess()) {
			Map<String, Object> map = JsonUtils.readJsonToMap(result.getResponse());
			List<GewaSearchKey> skList = JsonUtils.readJsonToObjectList(GewaSearchKey.class, (String) map.get(ROWS_SK_LIST));
			if (skList != null && !skList.isEmpty()) {
				for (GewaSearchKey key : skList) {
					if (StringUtils.isNotBlank(key.getName()))
						skListStr.add(StringUtils.trim(key.getName()));
				}
			}
			return skListStr;
		} else {
			logger.error("error searchGewaSearchKey Response:" + result.getMsg());
			return skListStr;
		}
	}

	/**
	 * Returns a String where those characters that QueryParser expects to be
	 * escaped are escaped by a preceding <code>\</code>.
	 */
	public static String escape(String s) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < s.length(); i++) {
			char c = s.charAt(i);
			// These characters are part of the query syntax and must be escaped
			if (c == '\\' || c == '+' || c == '-' || c == '!' || c == '(' || c == ')' || c == ':' || c == '^' || c == '[' || c == ']' || c == '\"'
					|| c == '{' || c == '}' || c == '~' || c == '*' || c == '?' || c == '|' || c == '&') {
				sb.append('\\');
			}
			sb.append(c);
		}
		return sb.toString();
	}

	private String getSearchLight(String content, String skey) {
		if (StringUtils.isBlank(content) || StringUtils.isBlank(skey)) {
			return "";
		}
		try {
			skey = escape(skey);
			return VmUtils.getLight(content, skey);
		} catch (Exception e) {
			e.printStackTrace();
			logger.debug("" + e);
			return content;
		}
	}

	@Override
	public Map<String, Object> getBeanSearchLight(Object bean, String skey) {
		List<String> propertyNames = getPropertyNames(bean);
		Map<String, Object> properties = BeanUtil.getBeanMap(bean, false, true);
		if (properties != null) {
			for (Map.Entry<String, Object> property : properties.entrySet()) {
				try {
					String key = property.getKey();
					Object value = property.getValue();
					if (value != null && (value instanceof String)) {
						value = VmBaseUtil.getText(String.valueOf(value).trim());
						if (propertyNames != null && propertyNames.contains(key)) {
							properties.put(key, getSearchLight(String.valueOf(value), skey));
						} else if (propertyNames == null || propertyNames.isEmpty()) {
							properties.put(key, getSearchLight(String.valueOf(value), skey));
						}
					}
				} catch (Exception e) {
					logger.debug("", e);
				}
			}
		}
		return properties;
	}

	@Override
	public void saveSearchKeyList(List<GewaSearchKey> list) {
		try {
			Map<String, String> params = new HashMap<String, String>();
			List<Map<String, String>> jsonBeanList = new ArrayList<Map<String, String>>();
			if (list != null) {
				for (GewaSearchKey gewaSearchKey : list) {
					jsonBeanList.add(BeanToMap(gewaSearchKey));
				}
				String jsonData = JsonUtils.writeObjectToJson(jsonBeanList);
				params.put("jsonData", jsonData);
				HttpResult result = HttpUtils.postUrlAsString(config.getString("searchUrl") + API_SEARCH_SAVESEARCHKEY, params);
				if (result.isSuccess()) {
					logger.debug(jsonData);
				} else {
					logger.error("error saveGewaSearchKey Response:" + result.getMsg());
					for (GewaSearchKey gewaSearchKey : list) {
						logger.error("SaveGewaSearchKey:" + BeanUtil.getBeanMap(gewaSearchKey));
					}
				}
			}
		} catch (Exception e) {
			logger.error("error saveGewaSearchKey Exception:" + e);
		}

	}

	public Map BeanToMap(GewaSearchKey gewaSearchKey) {
		Map<String, Object> params = BeanUtil.getBeanMap(gewaSearchKey, true, true);
		if (params != null && params.entrySet() != null) {
			for (Map.Entry<String, Object> entry : params.entrySet()) {
				if (entry.getKey() != null && entry.getValue() != null) {
					params.put(entry.getKey(), entry.getValue().toString());
				}
			}
		}
		return params;
	}

	@Override
	public List<String> getTopSearchKeyList(Integer count) {
		Map<String, String> params = new HashMap<String, String>();
		if (count != null && count > 0) {
			params.put("count", count.toString());
		}
		HttpResult result = HttpUtils.postUrlAsString(config.getString("searchUrl") + API_SEARCH_TOPSEARCHKEY, params);
		if (result.isSuccess()) {
			Map<String, Object> map = JsonUtils.readJsonToMap(result.getResponse());
			List<String> topSkList = JsonUtils.readJsonToObjectList(String.class, (String) map.get(TOP_SK_LIST));
			return topSkList;
		} else {
			logger.error("error getTopSearchKeyList Response:" + result.getMsg());
			return new ArrayList<String>();
		}
	}

	@Override
	public boolean isCurrentCity(BaseObject bean, String citycode) {
		Map<String, Object> properties = BeanUtil.getBeanMap(bean, false, false);
		if (properties != null) {
			for (Map.Entry<String, Object> property : properties.entrySet()) {
				try {
					String key = property.getKey();
					Object value = property.getValue();
					if (key.equalsIgnoreCase("citycode")) {
						if (value != null && citycode != null && !value.equals(citycode)) {
							return false;
						}
					}
				} catch (Exception e) {
					logger.error("", e);
				}
			}
		}
		return true;
	}

	/*
	 * 批量迁移数据(旧Gewasearchkey表数据迁移)
	 */
	@Override
	public String saveBatchSearchKey(Long timenum) {
		int num = 0;
		Timestamp starttime = new Timestamp(System.currentTimeMillis());
		int rowsPerPage = 100;
		final StringBuffer hql = new StringBuffer();
		final List<Object> args = new ArrayList<Object>();
		hql.append(" from GewaSearchKey g ");
		if (timenum != null) {
			hql.append(" where g.TIMENUM>? ");
			args.add(timenum);
		}
		List<Long> counts = hibernateTemplate.find("select count(*) " + hql.toString(), args.toArray());
		long rowsCount = (counts != null && !counts.isEmpty() ? counts.get(0).longValue() : 0);
		long pageCount = (rowsCount - 1) / rowsPerPage + 1;
		for (int pageNo = 0; pageNo < pageCount; pageNo++) {
			List<GewaSearchKey> list = daoService.queryByRowsRange(hql.toString(), rowsPerPage * pageNo, rowsPerPage, args.toArray());
			saveSearchKeyList(list);
			if (list != null) {
				num += list.size();
			}
		}
		double costTime = (new Timestamp(System.currentTimeMillis()).getTime() - starttime.getTime()) * 1.00 / (1000.00);
		String msg = "批量迁移数据:" + num + "条" + "  消耗：" + costTime + "秒";
		logger.info(msg);
		return msg;
	}

	@Override
	public void pushSearchKey(Object obj) {
		executor.execute(new UpdateTask(obj));
	}

	private GewaSearchKey updateSearchKey(Object obj) {
		GewaSearchKey gewaSearchKey = wrapSearchKey(obj);
		if (gewaSearchKey != null) {
			List<GewaSearchKey> list = new ArrayList<GewaSearchKey>();
			list.add(gewaSearchKey);
			saveSearchKeyList(list);
		}
		return gewaSearchKey;
	}

	public GewaSearchKey wrapSearchKey(Object obj) {
		if (obj == null)
			return null;
		GewaSearchKey gewaSearchKey = null;
		if (obj instanceof GewaQuestion) {
			GewaQuestion question = (GewaQuestion) obj;
			String category = "gewaquestion";
			Long relatedid = question.getId();
			String skey = warpSearchField(new String[] { "知道", question.getTitle(), question.getMembername() });
			gewaSearchKey = new GewaSearchKey(category + relatedid, question.getTitle(), question.getTag(), relatedid, category, skey,
					GewaSearchKey.getSortByCategory(category), question.getStatus(), DateUtil.getCurDateMills(question.getAddtime()),
					question.getCitycode());
		} else if (obj instanceof Movie) {
			Movie movie = (Movie) obj;
			String tag = "cinema";
			String category = "movie";
			Long relatedid = movie.getId();
			String skey = warpSearchField(new String[] { "电影", movie.getMoviename(), movie.getEnglishname(), movie.getMoviealias(),
					PinYinUtils.getFirstSpell(movie.getMoviealias()), movie.getActors(), movie.getDirector(), movie.getType(), movie.getHighlight() });
			gewaSearchKey = new GewaSearchKey(category + relatedid, movie.getName(), tag, relatedid, category, skey,
					GewaSearchKey.getSortByCategory(category), Status.Y, DateUtil.getCurDateMills(movie.getAddtime()), CITY_CODE_ALL);
		} else if (obj instanceof Cinema) {
			Cinema cinema = (Cinema) obj;
			String tag = "cinema";
			String category = "cinema";
			Long relatedid = cinema.getId();
			String skey = getSearchKey(cinema);
			gewaSearchKey = new GewaSearchKey(category + relatedid, cinema.getName(), tag, relatedid, category, skey,
					GewaSearchKey.getSortByCategory(category), Status.Y, DateUtil.getCurDateMills(cinema.getAddtime()), cinema.getCitycode());
		} else if (obj instanceof Diary) {
			Diary diary = (Diary) obj;
			String scategory = "diary";
			Long srelatedid = diary.getId();
			String skey = warpSearchField(new String[] { "论坛,帖子", diary.getCname(), diary.getMembername() });
			gewaSearchKey = new GewaSearchKey(scategory + srelatedid, diary.getCname(), diary.getTag(), srelatedid, scategory, skey,
					GewaSearchKey.getSortByCategory(scategory), diary.getStatus(), DateUtil.getCurDateMills(diary.getAddtime()), diary.getCitycode());
		} else if (obj instanceof News) {
			News news = (News) obj;
			String category = "news";
			Long relatedid = news.getId();
			String skey = warpSearchField(new String[] { "资讯,新闻", news.getTitle(), news.getSummary() });
			gewaSearchKey = new GewaSearchKey(category + relatedid, news.getTitle(), news.getTag(), relatedid, category, skey,
					GewaSearchKey.getSortByCategory(category), Status.Y, DateUtil.getCurDateMills(news.getAddtime()), news.getCitycode());
		} else if (obj instanceof SportItem) {
			SportItem sportItem = (SportItem) obj;
			String tag = "sport";
			String category = "sportservice";
			Long relatedid = sportItem.getId();
			String skey = warpSearchField(new String[] { "运动项目", sportItem.getName(), sportItem.getItemname(), sportItem.getEnglishname(),
					PinYinUtils.getFirstSpell(sportItem.getItemname()), sportItem.getContent() });
			gewaSearchKey = new GewaSearchKey(category + relatedid, sportItem.getName(), tag, relatedid, category, skey,
					GewaSearchKey.getSortByCategory(category), Status.Y, DateUtil.getCurDateMills(sportItem.getUpdatetime()), CITY_CODE_ALL);
		} else if (obj instanceof Sport) {
			Sport sport = (Sport) obj;
			String tag = "sport";
			String category = "sport";
			Long relatedid = sport.getId();
			String skey = getSearchKey(sport);
			gewaSearchKey = new GewaSearchKey(category + relatedid, sport.getName(), tag, relatedid, category, skey,
					GewaSearchKey.getSortByCategory(category), Status.Y, DateUtil.getCurDateMills(sport.getAddtime()), sport.getCitycode());
		} else if (obj instanceof Video) {
			Video video = (Video) obj;
			String category = "video";
			Long relatedid = video.getId();
			String skey = "视频," + video.getVideotitle();
			gewaSearchKey = new GewaSearchKey(category + relatedid, video.getVideotitle(), video.getTag(), relatedid, category, skey,
					GewaSearchKey.getSortByCategory(category), Status.Y, DateUtil.getCurDateMills(video.getAddtime()), CITY_CODE_ALL);
		} else if (obj instanceof Commu) {
			Commu c = (Commu) obj;
			String scategory = "commu";
			Long srelatedid = c.getId();
			String skey = warpSearchField(new String[] { "社区,圈子", c.getName(), c.getInfo() });
			gewaSearchKey = new GewaSearchKey(scategory + srelatedid, c.getName(), c.getTag(), srelatedid, scategory, skey,
					GewaSearchKey.getSortByCategory(scategory), c.getStatus(), DateUtil.getCurDateMills(c.getAddtime()), c.getCitycode());
		} else if (obj instanceof Drama) {
			Drama drama = (Drama) obj;
			String tag = "theatre";
			String category = "drama";
			Long relatedid = drama.getId();
			String skey = warpSearchField(new String[] { "话剧,剧目", drama.getDramaname(), drama.getEnglishname(), drama.getDramaalias(),
					drama.getActors(), drama.getDirector(), drama.getType() });
			gewaSearchKey = new GewaSearchKey(category + relatedid, drama.getName(), tag, relatedid, category, skey,
					GewaSearchKey.getSortByCategory(category), Status.Y, DateUtil.getCurDateMills(drama.getAddtime()), drama.getCitycode());
		} else if (obj instanceof DramaStar) {
			DramaStar dramaStar = (DramaStar) obj;
			String tag = "drama";
			String category = "dramastar";
			Long relatedid = dramaStar.getId();
			String skey = warpSearchField(new String[] { "剧社,话剧人物,明星", dramaStar.getName(), dramaStar.getEnglishname(),
					PinYinUtils.getFirstSpell(dramaStar.getName()), dramaStar.getContent(), dramaStar.getJob() });
			gewaSearchKey = new GewaSearchKey(category + relatedid, dramaStar.getName(), tag, relatedid, category, skey,
					GewaSearchKey.getSortByCategory(category), Status.Y, DateUtil.getCurDateMills(dramaStar.getAddtime()), CITY_CODE_ALL);
		} else if (obj instanceof Theatre) {
			Theatre theatre = (Theatre) obj;
			String tag = "theatre";
			String category = "theatre";
			Long relatedid = theatre.getId();
			String skey = getSearchKey(theatre);
			gewaSearchKey = new GewaSearchKey(category + relatedid, theatre.getName(), tag, relatedid, category, skey,
					GewaSearchKey.getSortByCategory(category), Status.Y, DateUtil.getCurDateMills(theatre.getAddtime()), theatre.getCitycode());
		}
		gewaSearchKey.setSkey(StringUtil.getHtmlText(gewaSearchKey.getSkey(), 800));
		return gewaSearchKey;
	}

	public String warpSearchField(String[] field) {
		StringBuffer sb = new StringBuffer();
		if (field != null) {
			for (int i = 0; i < field.length; i++) {
				if (StringUtils.isNotBlank(field[i])) {
					sb.append(field[i]).append(",");
				}
			}
		}
		sb.deleteCharAt(sb.lastIndexOf(","));
		return sb.toString().toLowerCase();
	}

	@Override
	public <T extends BaseObject> int reBuildIndex(Class<T> clazz) {
		if (clazz != null) {
			List allIds = daoService.getObjectIDList(clazz);
			List<List<Long>> groupIds = BeanUtil.partition(allIds, 50);
			int i = 0;
			for (List<Long> groupId : groupIds) {
				List<GewaSearchKey> sks = new ArrayList<GewaSearchKey>();
				if (groupId != null) {
					List<T> objList = daoService.getObjectList(clazz, groupId);
					if (objList != null) {
						for (BaseObject obj : objList) {
							GewaSearchKey sk = wrapSearchKey(obj);
							sks.add(sk);
						}
					}
				}
				saveSearchKeyList(sks);// 批量重构索引
				logger.warn(clazz.getName() + " group: " + i++);
			}
			return allIds.size();
		}
		return 0;
	}

	@Override
	public <T extends BaseInfo> String getSearchKey(T baseInfo) {
		StringBuilder sb = null;
		if (StringUtils.isBlank(baseInfo.getName())) {
			sb = new StringBuilder();
		} else {
			sb = new StringBuilder(baseInfo.getName());
		}
		if (!StringUtils.isAsciiPrintable(baseInfo.getName())) {
			if (StringUtils.isNotBlank(baseInfo.getEnglishname()))
				sb.append("," + baseInfo.getEnglishname());
		}
		if (StringUtils.isNotBlank(baseInfo.getCountycode())) {
			sb.append("," + placeService.getCountyname(baseInfo.getCountycode()));
		}
		sb.append(baseInfo.getAddress());
		if (baseInfo instanceof Cinema) {
			if (baseInfo.getName().indexOf("影城") < 0)
				sb.append(",影城");
			if (baseInfo.getName().indexOf("电影院") < 0)
				sb.append(",电影院");
			if (baseInfo.getName().indexOf("影都") < 0)
				sb.append(",影都");
			if (StringUtils.isNotBlank(baseInfo.getFeature())) {
				sb.append("," + baseInfo.getFeature());// 影院特色
			}
		} else if (baseInfo instanceof Theatre) {
			if (baseInfo.getName().indexOf("话剧院") < 0)
				sb.append(",话剧院");
			if (StringUtils.isNotBlank(baseInfo.getFeature())) {
				sb.append("," + baseInfo.getFeature());// 剧院特色
			}
		} else if (baseInfo instanceof Sport) {
			if (baseInfo.getName().indexOf("运动场馆") < 0)
				sb.append(",运动场馆");
		}
		return sb.toString().toLowerCase();
	}
	private List<String> getPropertyNames(Object obj) {
		List<String> result = new ArrayList<String>();
		if (obj instanceof GewaQuestion) {
			result.add("title");
			result.add("membername");
		} else if (obj instanceof Movie) {
			result.add("name");
			result.add("moviename");
			result.add("englishname");
			result.add("moviealias");
			result.add("actors");
			result.add("type");
			result.add("director");
			result.add("highlight");
		} else if (obj instanceof Cinema) {
			result.add("name");
		} else if (obj instanceof Diary) {
			result.add("subject");
			result.add("membername");
		} else if (obj instanceof News) {
			result.add("title");
			result.add("summary");
			result.add("content");
		} else if (obj instanceof SportItem) {
			result.add("name");
			result.add("itemname");
			result.add("englishname");
			result.add("content");
		} else if (obj instanceof Sport) {
			result.add("name");
			result.add("content");
		} else if (obj instanceof Video) {
			result.add("videotitle");
			result.add("content");
		} else if (obj instanceof Commu) {
			result.add("name");
			result.add("info");
		} else if (obj instanceof Drama) {
			result.add("name");
			result.add("dramaname");
			result.add("englishname");
			result.add("dramaalias");
			result.add("actors");
			result.add("type");
			result.add("director");
		} else if (obj instanceof DramaStar) {
			result.add("name");
			result.add("englishname");
			result.add("content");
			result.add("job");
		} else if (obj instanceof Theatre) {
			result.add("name");
			result.add("englishname");
			result.add("content");
		}
		return result;
	}

	@Override
	public List<GewaSearchKey> sortSK(List<GewaSearchKey> list) {
		if (list != null && !list.isEmpty()) {
			Collections.sort(list, new Comparator<GewaSearchKey>() {
				@Override
				public int compare(GewaSearchKey g1, GewaSearchKey g2) {
					if (g1 == null || g2 == null) {
						return 0;
					}
					Object o1 = g1.getRelatedObj();
					Object o2 = g2.getRelatedObj();
					Integer c1 = GewaSearchKey.getSortByCategory(g1.getCategory());
					Integer c2 = GewaSearchKey.getSortByCategory(g2.getCategory());
					if (c1 != null && c2 != null && !c1.equals(c2)) {
						return c2.compareTo(c1);
					} else {
						if (o1 instanceof Movie && o2 instanceof Movie) {
							Movie obj1 = (Movie) o1;
							Movie obj2 = (Movie) o2;
							if (obj1.getReleasedate() == null) {
								return 1;
							} else if (obj2.getReleasedate() == null) {
								return -1;
							} else {
								return obj2.getReleasedate().compareTo(obj1.getReleasedate());
							}
						} else if (o1 instanceof Cinema && o2 instanceof Cinema) {
							Cinema obj1 = (Cinema) o1;
							Cinema obj2 = (Cinema) o2;
							if (obj1.getClickedtimes() == null) {
								return 1;
							} else if (obj2.getClickedtimes() == null) {
								return -1;
							} else {
								return obj2.getClickedtimes().compareTo(obj1.getClickedtimes());
							}
						} else if (o1 instanceof Drama && o2 instanceof Drama) {
							Drama obj1 = (Drama) o1;
							Drama obj2 = (Drama) o2;
							if (obj1.getReleasedate() == null) {
								return 1;
							} else if (obj2.getReleasedate() == null) {
								return -1;
							} else {
								return obj2.getReleasedate().compareTo(obj1.getReleasedate());
							}
						} else if (o1 instanceof Theatre && o2 instanceof Theatre) {
							Theatre obj1 = (Theatre) o1;
							Theatre obj2 = (Theatre) o2;
							if (VmUtils.getSingleMarkStar(obj1, "general") == null) {
								return 1;
							} else if (VmUtils.getSingleMarkStar(obj2, "general") == null) {
								return -1;
							} else {
								return VmUtils.getSingleMarkStar(obj2, "general").compareTo(VmUtils.getSingleMarkStar(obj1, "general"));
							}
						} else if (o1 instanceof SportItem && o2 instanceof SportItem) {
							SportItem obj1 = (SportItem) o1;
							SportItem obj2 = (SportItem) o2;
							if (obj1.getClickedtimes() == null) {
								return 1;
							} else if (obj2.getClickedtimes() == null) {
								return -1;
							} else {
								return obj2.getClickedtimes().compareTo(obj1.getClickedtimes());
							}
						} else if (o1 instanceof Sport && o2 instanceof Sport) {
							Sport obj1 = (Sport) o1;
							Sport obj2 = (Sport) o2;
							if (VmUtils.getSingleMarkStar(obj1, "general") == null) {
								return 1;
							} else if (VmUtils.getSingleMarkStar(obj2, "general") == null) {
								return -1;
							} else {
								return VmUtils.getSingleMarkStar(obj2, "general").compareTo(VmUtils.getSingleMarkStar(obj1, "general"));
							}
						} else if (o1 instanceof Commu && o2 instanceof Commu) {
							Commu obj1 = (Commu) o1;
							Commu obj2 = (Commu) o2;
							if (obj1.getAddtime() == null) {
								return 1;
							} else if (obj2.getAddtime() == null) {
								return -1;
							} else {
								return obj2.getAddtime().compareTo(obj1.getAddtime());
							}
						} else if (o1 instanceof DramaStar && o2 instanceof DramaStar) {
							DramaStar obj1 = (DramaStar) o1;
							DramaStar obj2 = (DramaStar) o2;
							if (obj1.getClickedtimes() == null) {
								return 1;
							} else if (obj2.getClickedtimes() == null) {
								return -1;
							} else {
								return obj2.getClickedtimes().compareTo(obj1.getClickedtimes());
							}
						} else if (o1 instanceof Video && o2 instanceof Video) {
							Video obj1 = (Video) o1;
							Video obj2 = (Video) o2;
							if (obj1.getAddtime() == null) {
								return 1;
							} else if (obj2.getAddtime() == null) {
								return -1;
							} else {
								return obj2.getAddtime().compareTo(obj1.getAddtime());
							}
						} else if (o1 instanceof News && o2 instanceof News) {
							News obj1 = (News) o1;
							News obj2 = (News) o2;
							if (obj1.getAddtime() == null) {
								return 1;
							} else if (obj2.getAddtime() == null) {
								return -1;
							} else {
								return obj2.getAddtime().compareTo(obj1.getAddtime());
							}
						} else if (o1 instanceof Diary && o2 instanceof Diary) {
							Diary obj1 = (Diary) o1;
							Diary obj2 = (Diary) o2;
							if (obj1.getAddtime() == null) {
								return 1;
							} else if (obj2.getAddtime() == null) {
								return -1;
							} else {
								return obj2.getAddtime().compareTo(obj1.getAddtime());
							}
						}
					}
					return 0;
				}
			});
		}
		return list;
	}

	private ThreadPoolExecutor executor;

	@PostConstruct
	public void init() {
		BlockingQueue<Runnable> taskQueue = new LinkedBlockingQueue<Runnable>();
		executor = new ThreadPoolExecutor(5, 5, 300, TimeUnit.SECONDS, taskQueue);
		executor.allowCoreThreadTimeOut(false);
	}

	private class UpdateTask implements Runnable {
		private Object obj;

		public UpdateTask(Object object) {
			this.obj = object;
		}

		@Override
		public void run() {
			updateSearchKey(obj);
		}

	}

	@Override
	public Set<String> getSearchKeyList(String tag, String skey, int maxnum) {
		return searchKey(null, null, tag, null, skey, maxnum);// tag大类，兼容合作商
	}
}
