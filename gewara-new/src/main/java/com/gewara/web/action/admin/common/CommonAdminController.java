package com.gewara.web.action.admin.common;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.gewara.Config;
import com.gewara.constant.AdminCityContant;
import com.gewara.constant.CityData;
import com.gewara.constant.DiaryConstant;
import com.gewara.constant.TagConstant;
import com.gewara.constant.content.SignName;
import com.gewara.constant.sys.JsonDataKey;
import com.gewara.constant.sys.MongoData;
import com.gewara.constant.ticket.OrderConstant;
import com.gewara.helper.sys.AdminCityHelper;
import com.gewara.json.PageView;
import com.gewara.model.BaseObject;
import com.gewara.model.acl.User;
import com.gewara.model.acl.WebModule;
import com.gewara.model.bbs.DiaryBase;
import com.gewara.model.common.City;
import com.gewara.model.common.County;
import com.gewara.model.common.GewaCity;
import com.gewara.model.common.Indexarea;
import com.gewara.model.common.JsonData;
import com.gewara.model.common.Province;
import com.gewara.model.common.RelateToCity;
import com.gewara.model.common.Subwayline;
import com.gewara.model.common.Subwaystation;
import com.gewara.model.content.Bulletin;
import com.gewara.model.content.GewaCommend;
import com.gewara.model.content.News;
import com.gewara.model.content.NewsPage;
import com.gewara.model.content.Picture;
import com.gewara.model.content.Video;
import com.gewara.model.movie.Cinema;
import com.gewara.model.movie.Movie;
import com.gewara.model.movie.MovieVideo;
import com.gewara.model.partner.CallbackOrder;
import com.gewara.model.pay.GewaOrder;
import com.gewara.model.user.Treasure;
import com.gewara.mongo.MongoService;
import com.gewara.service.GewaCityService;
import com.gewara.service.JsonDataService;
import com.gewara.service.bbs.DiaryService;
import com.gewara.service.content.NewsService;
import com.gewara.service.content.PictureService;
import com.gewara.service.content.VideoService;
import com.gewara.service.movie.MCPService;
import com.gewara.support.ErrorCode;
import com.gewara.support.ReadOnlyTemplate;
import com.gewara.support.ServiceHelper;
import com.gewara.untrans.CommentService;
import com.gewara.untrans.PageCacheService;
import com.gewara.untrans.PageParams;
import com.gewara.untrans.gym.SynchGymService;
import com.gewara.untrans.hbase.ChangeLogService;
import com.gewara.untrans.hbase.HBaseService;
import com.gewara.untrans.hbase.HbaseData;
import com.gewara.untrans.monitor.RoleTag;
import com.gewara.untrans.terminal.TerminalService;
import com.gewara.util.BeanUtil;
import com.gewara.util.DateUtil;
import com.gewara.util.JsonUtils;
import com.gewara.util.StringUtil;
import com.gewara.util.VmUtils;
import com.gewara.util.WebUtils;
import com.gewara.util.YoukuApiUtil;
import com.gewara.web.action.admin.BaseAdminController;
import com.gewara.web.menu.GBMenuDataBuilder;
import com.gewara.web.menu.MenuRepository;
import com.gewara.web.support.AclService;
import com.gewara.web.util.PageUtil;
import com.gewara.xmlbind.bbs.Comment;
import com.gewara.xmlbind.gym.RemoteGym;
import com.gewara.xmlbind.partner.YoukuVideo;
import com.gewara.xmlbind.terminal.TakeInfo;

@Controller
public class CommonAdminController extends BaseAdminController {
	@Autowired
	@Qualifier("changeLogService")
	private ChangeLogService changeLogService;
	@Autowired
	@Qualifier("hbaseService")
	private HBaseService hbaseService;
	@Autowired@Qualifier("terminalService")
	private TerminalService terminalService;
	@Autowired@Qualifier("config")
	private Config config;
	public void setConfig(Config config) {
		this.config = config;
	}
	@Autowired@Qualifier("readOnlyTemplate")
	private ReadOnlyTemplate readOnlyTemplate;
	public void setReadOnlyHibernateTemplate(ReadOnlyTemplate readOnlyTemplate) {
		this.readOnlyTemplate = readOnlyTemplate;
	}

	@Autowired
	@Qualifier("commentService")
	private CommentService commentService;
	@Autowired
	@Qualifier("pictureService")
	private PictureService pictureService;

	public void setPictureService(PictureService pictureService) {
		this.pictureService = pictureService;
	}

	@Autowired
	@Qualifier("pageCacheService")
	private PageCacheService pageCacheService;

	public void setPageCacheService(PageCacheService pageCacheService) {
		this.pageCacheService = pageCacheService;
	}

	@Autowired
	@Qualifier("mcpService")
	private MCPService mcpService;

	public void setMcpService(MCPService mcpService) {
		this.mcpService = mcpService;
	}

	@Autowired
	private NewsService newsService;

	public void setNewsService(NewsService newsService) {
		this.newsService = newsService;
	}

	@Autowired
	@Qualifier("aclService")
	private AclService aclService = null;
	@Autowired
	@Qualifier("videoService")
	private VideoService videoService;

	public void setVideoService(VideoService videoService) {
		this.videoService = videoService;
	}
	@RequestMapping("/common/contentEditor.xhtml")
	public String contentEditor() {
		return "common/contentEditor.vm";
	}

	@Autowired
	@Qualifier("jsonDataService")
	private JsonDataService jsonDataService;

	@Autowired
	@Qualifier("mongoService")
	private MongoService mongoService;

	@Autowired
	@Qualifier("synchGymService")
	private SynchGymService synchGymService;

	@Autowired
	@Qualifier("diaryService")
	private DiaryService diaryService;
	
	@Autowired@Qualifier("gewaCityService")
	private GewaCityService gewaCityService;
	
	@RequestMapping("/admin/common/subwayList.xhtml")
	public String bussbulletinList(ModelMap model,Boolean isSetTime, HttpServletRequest request) {
		String citycode = getAdminCitycode(request);
		List<Subwayline> lineList = hibernateTemplate.find("from Subwayline s where s.citycode=? order by s.id", citycode);
		if(isSetTime != null && isSetTime){//进入设置首末班车时间设置页面
			if(!lineList.isEmpty()){
				model.put("curLine", lineList.get(0));
			}
			model.put("lineList", lineList);
			return "admin/common/setTimeWithSubwayList.vm";
		}
		Map<Long, List<Subwaystation>> stationMap = new HashMap<Long, List<Subwaystation>>();
		for (Subwayline line : lineList) {
			stationMap.put(line.getId(), placeService.getSubwaystationsByLineId(line.getId()));
		}
		model.put("stationMap", stationMap);
		model.put("lineList", lineList);
		return "admin/common/subwayList.vm";
	}

	@RequestMapping("/admin/checkpass.xhtml")
	public String checkPass(String password, ModelMap model) {
		User user = getLogonUser();
		if (user == null)
			return showJsonError(model, "请先登录!");
		String md5pass = StringUtil.md5(password);
		if (!StringUtils.equals(md5pass, user.getPassword()))
			return showJsonError(model, "输入密码错误。");
		return showJsonSuccess(model, "验证通过。");
	}

	@RequestMapping("/admin/changeCity.xhtml")
	public String changeCity(String citycode, String path, HttpServletResponse response, ModelMap model) {
		if (StringUtils.isBlank(citycode)){
			citycode = "310000";
		}
		User user = getLogonUser();
		if (!user.getCitycode().contains(citycode)) {
			String cityname = AdminCityContant.getCitycode2CitynameMap().get(citycode);
			return forwardMessage(model, "你没有切换:" + cityname + "的权限！");
		}
		if (!WebUtils.isValidCitycode(citycode)) {
			throw new IllegalArgumentException("切换城市不合法！");
		}
		setAdminCode(citycode, response);
		if (StringUtils.isNotBlank(path)){
			return showRedirect(path, model);
		}
		return "redirect:/admin/adminConsole.xhtml";
	}

	@RequestMapping("/admin/adminConsole.xhtml")
	public String adminConsole(String reload, @CookieValue(required = false) String admin_citycode, HttpServletResponse response, ModelMap model) {
		User user = getLogonUser();
		String[] roles = StringUtils.split(user.getRolenames(), ",");
		MenuRepository repository = (MenuRepository) applicationContext.getServletContext().getAttribute(MenuRepository.GEWA_MENU_REPOSITORY_KEY);
		if (repository == null || "true".equals(reload)) {
			repository = new MenuRepository(aclService.getMenuList(WebModule.TAG_GEWA));
			applicationContext.getServletContext().setAttribute(MenuRepository.GEWA_MENU_REPOSITORY_KEY, repository);
		}
		GBMenuDataBuilder mdb = new GBMenuDataBuilder(config.getBasePath(), roles, repository);
		String menuData = mdb.getMenuData().toString();
		model.put("menuData", menuData);
		model.put("user", user);
		if (StringUtils.isBlank(user.getCitycode()))
			return forwardMessage(model, "该用户没有分配城市");
		if (StringUtils.isNotBlank(admin_citycode) && !user.getCitycode().contains(admin_citycode))
			admin_citycode = null;
		if (StringUtils.isBlank(admin_citycode)) {
			String[] c = StringUtils.split(user.getCitycode(), ",");
			admin_citycode = c[0];
			setAdminCode(admin_citycode, response);
		} else {
			if (!WebUtils.isValidCitycode(admin_citycode))
				throw new IllegalArgumentException("切换城市不合法！");
		}
		Map<GewaCity, List<GewaCity>> proMap = gewaCityService.getAdmCityMap();
		model.put("citycode", admin_citycode);
		model.put("cityMap", AdminCityContant.allcityMap);
		model.put("proMap", proMap);
		model.put("ssoUrl", config.getString("ssoValidateUrl"));
		return "admin/adminConsole.vm";
	}
	
	@RequestMapping("/admin/ajax/common/searchCity.xhtml")
	public String searchCity(String cityKey, String targetUrl, ModelMap model){
		if (StringUtils.isNotBlank(targetUrl)) {
			model.put("targetUrl", targetUrl);
		}
		if(StringUtils.isNotBlank(cityKey)){
			cityKey = cityKey.toLowerCase();
			Map<GewaCity, List<GewaCity>> idxMap = gewaCityService.getAdmCityMap();
			List<GewaCity> gewaCityList = new ArrayList<GewaCity>();
			for (GewaCity gewaCity : idxMap.keySet()) {
				List<GewaCity> gcList = idxMap.get(gewaCity);
				for (GewaCity city : gcList) {
					if(StringUtils.contains(city.getCityname(), cityKey) || StringUtils.contains(city.getPinyin(), cityKey) || StringUtils.contains(city.getPy(), cityKey)){
						gewaCityList.add(city);
					}
				}
			}
			model.put("gewaCityList", gewaCityList);
		}
		return "admin/common/adminCitySearch.vm";
	}
	
	@RequestMapping("/admin/ajax/common/adminCityList.xhtml")
	public String adminCityList(String targetUrl, ModelMap model) {
		if (StringUtils.isNotBlank(targetUrl)) {
			model.put("targetUrl", targetUrl);
		}
		List<GewaCity> hotCityList = gewaCityService.getHotCityList();
		model.put("hotCityList", hotCityList);
		List<AdminCityHelper> province2CityList = AdminCityHelper.province2CityListMap();
		model.put("province2CityList", province2CityList);
		return "admin/common/adminHeadCityList.vm";
	}

	@RequestMapping("/admin/common/oldBulletinList.xhtml")
	public String oldBulletinList(@RequestParam("tag") String tag, Long relatedid, ModelMap model, HttpServletRequest request) {
		List<Bulletin> bulletinList = null;
		Map map = new HashMap();
		if (relatedid != null) {
			String query = "from Bulletin n where n.citycode=? and n.relatedid=? and n.validtime<? order by n.posttime desc";
			bulletinList = hibernateTemplate.find(query, getAdminCitycode(request), relatedid, DateUtil.getCurDate());
			Object relate = relateService.getRelatedObject(tag, relatedid);
			model.put("relate", relate);
		} else {
			String hql = "from Bulletin b where b.citycode=? and b.tag=? and b.validtime<? order by b.relatedid desc";
			bulletinList = hibernateTemplate.find(hql, getAdminCitycode(request), tag, DateUtil.getCurDate());
			for (Bulletin b : bulletinList) {
				map.put(b, relateService.getRelatedObject(tag, b.getRelatedid()));
			}
			model.put("bulletinMap", map);
		}
		model.put("bulletinList", bulletinList);
		return "admin/common/bulletinList.vm";
	}

	@RequestMapping("/admin/common/bulletinListByHotvalue.xhtml")
	public String bulletinListByHotvalue(@RequestParam("tag") String tag, HttpServletRequest request, @RequestParam("hotvalue") Integer hotvalue,
			Long relatedid, ModelMap model) {
		List<Bulletin> bulletinList = null;
		Map map = new HashMap();
		if (relatedid != null) {
			bulletinList = commonService.getCurrentBulletinsByRelatedidAndHotvalue(getAdminCitycode(request), relatedid, hotvalue);
			for (Bulletin b : bulletinList) {
				map.put(b, relateService.getRelatedObject(tag, relatedid));
			}
		} else {
			bulletinList = commonService.getBulletinListByHotvalue(getAdminCitycode(request), tag, hotvalue);
			for (Bulletin b : bulletinList) {
				map.put(b, relateService.getRelatedObject(tag, b.getRelatedid()));
			}
		}
		model.put("bulletinList", bulletinList);
		model.put("bulletinMap", map);
		return "admin/common/bulletinList.vm";
	}

	@RequestMapping("/admin/common/newsList.xhtml")
	public String newsList(@RequestParam("tag") String tag, String newstype, Long relatedid, String title, Long nid, String citycode, Integer pageNo,
			HttpServletRequest request, ModelMap model) {
		if (nid != null) {
			News news = daoService.getObject(News.class, nid);
			if (news != null)
				model.put("newsList", Arrays.asList(news));
			model.put("citynameMap", commonService.initRelateCityName(Arrays.asList(news)));
			return "admin/common/newsList.vm";
		}
		if (StringUtils.isBlank(citycode)) {
			citycode = getAdminCitycode(request);
		}
		Integer rowsCount = newsService.getNewsCount(citycode, tag, newstype, relatedid, title);
		if (pageNo == null)
			pageNo = 0;
		int rowsPerPage = 20;
		int first = rowsPerPage * pageNo;
		PageUtil pageUtil = new PageUtil(rowsCount, rowsPerPage, pageNo, "admin/common/newsList.xhtml");
		Map params = new HashMap();
		if (StringUtils.isNotBlank(tag))
			params.put("tag", tag);
		if (StringUtils.isNotBlank(newstype))
			params.put("newstype", newstype);
		if (StringUtils.isNotBlank(title))
			params.put("title", title);
		if (relatedid != null)
			params.put("relatedid", relatedid);
		pageUtil.initPageInfo(params);
		List<News> newsList = newsService.getNewsList(citycode, tag, relatedid, newstype, title, first, rowsPerPage);
		model.put("pageUtil", pageUtil);
		model.put("newsList", newsList);
		model.put("citynameMap", commonService.initRelateCityName(newsList));
		model.put("syscitylist", AdminCityContant.getCitycode2CitynameMap());
		return "admin/common/newsList.vm";
	}

	@RequestMapping("/admin/common/newsDetail.xhtml")
	public String newsList(Long nid, ModelMap model) {
		if (nid != null) {
			News news = daoService.getObject(News.class, nid);
			List<NewsPage> newsPageList = newsService.getNewsPageListByNewsid(news.getId());
			model.put("news", news);
			model.put("newsPageList", newsPageList);
		}
		return "admin/common/newsDetail.vm";
	}

	@RequestMapping("/admin/common/videoList.xhtml")
	public String videoList(@RequestParam("tag") String tag, Long relatedid, Integer pageNo, ModelMap model) {
		int rowsPerPage = 50;
		if (pageNo == null)
			pageNo = 0;
		int firstRow = pageNo * rowsPerPage;
		Integer count = 0;

		Map videoMap = new HashMap();
		List<Video> videoList = null;
		if (relatedid != null) {
			if (VmUtils.eq(tag, "gym")) {
				ErrorCode<RemoteGym> code = synchGymService.getRemoteGym(relatedid, true);
				if (code.isSuccess())
					model.put("gym", code.getRetval());
			}
			videoList = videoService.getVideoListByTag(tag, relatedid,null,"orderNum",true, firstRow, rowsPerPage);
			count = videoService.getVideoCountByTag(tag, relatedid);
			for (Video video : videoList) {
				videoMap.put(video, relateService.getRelatedObject(tag, relatedid));
			}
			Object relate = relateService.getRelatedObject(tag, relatedid);
			model.put("relate", relate);
		} else {
			videoList = videoService.getVideoListByTag(tag, null, firstRow, rowsPerPage);
			count = videoService.getVideoCountByTag(tag, null);
			for (Video video : videoList) {
				videoMap.put(video, relateService.getRelatedObject(tag, video.getRelatedid()));
			}
		}
		PageUtil pageUtil = new PageUtil(count, rowsPerPage, pageNo, "admin/common/videoList.xhtml");
		Map params = new HashMap();
		params.put("tag", tag);
		if (relatedid != null)
			params.put("relatedid", relatedid);
		pageUtil.initPageInfo(params);
		model.put("pageUtil", pageUtil);
		model.put("videoMap", videoMap);
		model.put("videoList", videoList);
		return "admin/common/videoList.vm";
	}

	@RequestMapping("/admin/common/movieVideoList.xhtml")
	public String movieVideoList(Long movieid, ModelMap model) {
		Movie movie = daoService.getObject(Movie.class, movieid);
		List<MovieVideo> videoList = Arrays.asList(videoService.getMovieVideo(movieid));
		model.put("videoList", videoList);
		model.put("movie", movie);
		return "admin/common/movieVideoList.vm";
	}

	@RequestMapping("/admin/common/saveMovieVideo.xhtml")
	public String movieVideoList(Long movieid, String videoid, ModelMap model) {
		if (StringUtils.isNotBlank(videoid)) {// 保存视频ID
			MovieVideo mv = videoService.getMovieVideoByVideoid(videoid);
			if (mv == null) {
				mv = new MovieVideo(movieid, videoid);
			} else {
				mv.setVideoid(videoid);
			}
			YoukuVideo ykvideo = YoukuApiUtil.getYoukuImg(videoid);
			if (ykvideo != null) {
				mv.setImg(ykvideo.getImg());
			}
			daoService.saveObject(mv);
		}
		return showJsonSuccess(model);
	}

	@RequestMapping("/admin/common/delMovieVideo.xhtml")
	public String movieVideoList(String videoid, ModelMap model) {
		MovieVideo mv = videoService.getMovieVideoByVideoid(videoid);
		daoService.removeObject(mv);
		return showJsonSuccess(model);
	}

	@RequestMapping("/admin/common/videoListByHotvalue.xhtml")
	public String videoListByHotvalue(@RequestParam("tag") String tag, @RequestParam("hotvalue") Integer hotvalue, Long relatedid, Integer pageNo,
			ModelMap model) {
		int rowsPerPage = 50;
		if (pageNo == null)
			pageNo = 0;
		int firstRow = pageNo * rowsPerPage;
		Integer count = 0;

		Map videoMap = new HashMap();
		List<Video> videoList = null;
		if (relatedid != null) {
			videoList = videoService.getVideoListByTag(tag, relatedid, firstRow, rowsPerPage);
			count = videoService.getVideoCountByTag(tag, relatedid);
			for (Video video : videoList) {
				videoMap.put(video, relateService.getRelatedObject("movie", relatedid));
			}
		} else {
			videoList = videoService.getVideoListByTag(tag, null, hotvalue, "updatetime", false, firstRow, rowsPerPage);
			count = videoService.getVideoCountByTag(tag, relatedid, hotvalue);
			for (Video video : videoList) {
				videoMap.put(video, relateService.getRelatedObject("movie", video.getRelatedid()));
			}
		}
		PageUtil pageUtil = new PageUtil(count, rowsPerPage, pageNo, "admin/common/videoListByHotvalue.xhtml");
		Map params = new HashMap();
		params.put("tag", tag);
		params.put("hotvalue", hotvalue);
		if (relatedid != null)
			params.put("relatedid", relatedid);
		pageUtil.initPageInfo(params);
		model.put("pageUtil", pageUtil);

		model.put("videoMap", videoMap);
		model.put("videoList", videoList);
		return "admin/common/videoList.vm";
	}

	@RequestMapping("/admin/common/mappoint.xhtml")
	public String mappoint(HttpServletRequest request, ModelMap model, @RequestParam("tag") String tag, @RequestParam("id") Long id) {
		Object object = relateService.getRelatedObject(tag, id);
		model.put("object", object);
		model.put("cityData", new CityData());
		model.put("citycode", getAdminCitycode(request));
		return "admin/common/mapPoint.vm";
	}

	/**
	 * 跳转到保存地图页面
	 * 
	 * @param request
	 * @param model
	 * @param tag
	 * @param id
	 * @return
	 */
	@RequestMapping("/admin/common/mapbpoint.xhtml")
	public String mapbpoint(HttpServletRequest request, ModelMap model, @RequestParam("tag") String tag, @RequestParam("id") Long id) {
		Object object = relateService.getRelatedObject(tag, id);
		model.put("object", object);
		model.put("cityData", new CityData());
		model.put("citycode", getAdminCitycode(request));
		return "admin/common/mapBPoint.vm";
	}

	@RequestMapping("/admin/common/bulletinList.xhtml")
	public String bulletinList(@RequestParam("tag") String tag, Long relatedid, String all, HttpServletRequest request, ModelMap model) {
		List<Bulletin> bulletinList = null;
		Map map = new HashMap();
		String citycode = getAdminCitycode(request);
		if (StringUtils.isBlank(all)) {
			if (relatedid != null) {
				String query = "from Bulletin n where n.citycode=? and n.relatedid=? and (n.validtime>=? or n.validtime=null) order by n.posttime desc";
				bulletinList = hibernateTemplate.find(query, citycode, relatedid, DateUtil.getCurDate());
				Object relate = relateService.getRelatedObject(tag, relatedid);
				model.put("relate", relate);
			} else {
				bulletinList = commonService.getBulletinListByTag(citycode, tag);
				for (Bulletin b : bulletinList) {
					map.put(b, relateService.getRelatedObject(tag, b.getRelatedid()));
				}
				model.put("bulletinMap", map);
			}
		} else {
			String query = "from Bulletin n where n.tag=? and n.relatedid=? order by n.posttime desc";
			bulletinList = hibernateTemplate.find(query, tag, relatedid);
			Object relate = relateService.getRelatedObject(tag, relatedid);
			model.put("relate", relate);
		}

		model.put("bulletinList", bulletinList);
		return "admin/common/bulletinList.vm";
	}

	@RequestMapping("/admin/common/pictureList.xhtml")
	public String pictureList(@RequestParam("tag") String tag, @RequestParam("relatedid") Long relatedid, ModelMap model) {
		List<Picture> pictureList = pictureService.getPictureListByRelatedid(tag, relatedid, 0, 200);
		Object object = relateService.getRelatedObject(tag, relatedid);
		model.put("firstpic", BeanUtil.get(object, "firstpic"));
		model.put("pictureList", pictureList);
		model.put("placeList", Arrays.asList("cinema", "ktv", "sport", "bar", "gym", "theatre", "coach", "gymcourse"));
		return "admin/common/pictureList.vm";
	}

	@RequestMapping("/admin/common/discountInfoList.xhtml")
	public String discountList(@RequestParam("tag") String tag, @RequestParam("relatedid") Long relatedid, ModelMap model) {
		List discountInfoList = commonService.getDiscountInfoByRelatedidAndTag(relatedid, tag);
		model.put("discountInfoList", discountInfoList);
		Object object = null;
		if (tag.equals("sportTrain"))
			object = relateService.getRelatedObject("sport", relatedid);
		else
			object = relateService.getRelatedObject(tag, relatedid);
		model.put("relate", object);
		return "admin/common/discountInfoList.vm";
	}

	@RequestMapping("/admin/seo/paoding.xhtml")
	public String paoding() { // 分词
		return "admin/common/paoding.vm";
	}

	@RequestMapping("/admin/common/refreshPage.xhtml")
	public String refreshPage(HttpServletRequest request, String pageUrl, String citycode, String jparam, ModelMap model) {
		Map<String, String> params = VmUtils.readJsonToMap(jparam);
		PageParams pageParams = new PageParams();
		for(String key:params.keySet()){
			pageParams.addSingleString(key, params.get(key));
		}
		pageCacheService.refreshPageView(pageUrl, pageParams, citycode);
		String ip = WebUtils.getRemoteIp(request);
		PageView pv = pageCacheService.getPageView(pageUrl, pageParams, citycode, ip);
		model.put("pageView", pv);
		return "pageView.vm";
	}

	/**
	 * 查询登录后下面显示的内容
	 * 
	 * @return
	 */
	@RequestMapping("/admin/common/loginedcontent.xhtml")
	public String loginedContent(ModelMap model, HttpServletRequest request) {
		List<GewaCommend> gewacommentList = commonService.getGewaCommendList(getAdminCitycode(request), SignName.INDEX_LOGINED, null, null, false, 0,
				100);
		model.put("gewacommentList", gewacommentList);
		return "admin/common/loginedContentList.vm";
	}
	/**
	 * 有关新闻的关联城市
	 * */
	@RequestMapping("/admin/common/commonRelateCitys.xhtml")
	public String commonRelateCitys(String tag, Long relatedid, ModelMap model) {
		Object Object = relateService.getRelatedObject(tag, relatedid);
		Object selecity = BeanUtil.get(Object, "citycode");
		if (selecity != null) {
			model.put("selcitycode", citycodeList(selecity.toString()));
		}
		model.put("cityMap", AdminCityContant.getCitycode2CitynameMap());
		List<AdminCityHelper> province2CityList = AdminCityHelper.province2CityListMap();
		model.put("province2CityList", province2CityList);
		model.put("tag", tag);
		model.put("relatedid", relatedid);

		return "admin/common/selectRelateCity.vm";
	}

	@RequestMapping("/admin/common/commonRelateToCity.xhtml")
	public String commonRelateToCity(String tag, Long relatedid, String flag, ModelMap model) {
		Object Object = null;
		if (StringUtils.equals(tag, TagConstant.TAG_DIARY)) {
			Object = diaryService.getDiaryBase(relatedid);
		} else
			Object = relateService.getRelatedObject(tag, relatedid);
		String citycode = null;
		String division = (String) BeanUtil.get(Object, "division");
		if (StringUtils.equals(division, DiaryConstant.DIVISION_A)) {
			citycode = AdminCityContant.CITYCODE_ALL;
		} else if (StringUtils.equals(division, DiaryConstant.DIVISION_Y)) {
			citycode = (String) BeanUtil.get(Object, "citycode");
		} else {
			List<RelateToCity> reList = commonService.getRelateToCity(tag, relatedid, null, null);
			List<String> idList = BeanUtil.getBeanPropertyList(reList, String.class, "citycode", true);
			citycode = StringUtils.join(idList.toArray(), ",");
		}
		if (StringUtils.isBlank(citycode))
			citycode = (String) BeanUtil.get(Object, "citycode");
		model.put("selcitycode", citycodeList(citycode));
		model.put("cityMap", AdminCityContant.getCitycode2CitynameMap());
		model.put("tag", tag);
		model.put("relatedid", relatedid);
		model.put("flag", flag);
		return "admin/common/newselectRelateCity.vm";
	}

	/***
	 * 保存关联城市
	 */
	@RequestMapping("/admin/common/saveRelateCitys.xhtml")
	public String saveRelateCitys(String tag, Long relatedid, String relatecityAll, String relatecity, ModelMap model) {
		BaseObject Object = (BaseObject) relateService.getRelatedObject(tag, relatedid);
		if (StringUtils.equals(relatecityAll, AdminCityContant.CITYCODE_ALL)) {
			BeanUtil.set(Object, "citycode", relatecityAll);
		} else {
			BeanUtil.set(Object, "citycode", relatecity);
		}
		daoService.saveObject(Object);
		String cityNames = revertCityname((String) BeanUtil.get(Object, "citycode"), "，");
		return showJsonSuccess(model, cityNames);
	}

	/***
	 * 保存关联城市
	 */
	@RequestMapping("/admin/common/saveRelateToCity.xhtml")
	public String saveRelateToCity(String tag, Long relatedid, String relatecityAll, String relatecity, String flag, ModelMap model) {
		BaseObject Object = (BaseObject) relateService.getRelatedObject(tag, relatedid);
		String citycode = (String) BeanUtil.get(Object, "citycode");
		String oldflag = (String) BeanUtil.get(Object, "flag");
		if (StringUtils.equals(relatecityAll, AdminCityContant.CITYCODE_ALL)) {
			BeanUtil.set(Object, "division", DiaryConstant.DIVISION_A);
		} else {
			BeanUtil.set(Object, "division", DiaryConstant.DIVISION_N);
			if (StringUtils.isBlank(relatecity))
				return showJsonError(model, "请只至选择一个关联的城市！");
			List<String> cityList = Arrays.asList(StringUtils.split(relatecity, ","));
			List<RelateToCity> reList = new ArrayList<RelateToCity>();
			List<RelateToCity> reList2 = commonService.getRelateToCity(tag, relatedid, null, null);
			for (String str : cityList) {
				List<RelateToCity> reList3 = commonService.getRelateToCity(tag, relatedid, str, null);
				if (!reList3.isEmpty()) {
					reList.addAll(reList3);
					reList2.removeAll(reList3);
					continue;
				}
				RelateToCity relateToCity = new RelateToCity(relatedid, str, tag);
				if (StringUtils.equals(citycode, str)) {
					relateToCity.setFlag(oldflag);
				}
				reList.add(relateToCity);
			}
			if (!reList2.isEmpty())
				daoService.removeObjectList(reList2);
			if (!reList.isEmpty())
				daoService.saveObjectList(reList);
			model.put("reList", reList);
			model.put("cityMap", AdminCityContant.getCitycode2CitynameMap());
			model.put("flag", flag);
		}
		daoService.saveObject(Object);
		return "admin/common/relateToCity.vm";
	}

	private List<String> citycodeList(String citycode) {
		String[] arrs = StringUtils.split(citycode, ",");
		return Arrays.asList(arrs);
	}

	private String revertCityname(String citycode, String joinChar) {
		if (StringUtils.isBlank(citycode))
			citycode = AdminCityContant.CITYCODE_SH;
		String[] arrs = StringUtils.split(citycode, ",");
		List<String> arrss = new ArrayList<String>();
		for (String s : arrs) {
			String cityName = AdminCityContant.getCitycode2CitynameMap().get(s);
			if (StringUtils.isBlank(cityName)) {
				cityName = "全国";
			}
			arrss.add(cityName);
		}
		return StringUtils.join(arrss, joinChar);
	}

	/***
	 * 帮助中心配置 列表
	 */
	@RequestMapping("/admin/common/confHelpCenter.xhtml")
	public String confHelpCenter(ModelMap model) {
		Map<String, List<JsonData>> subdataMap = new HashMap<String, List<JsonData>>();
		// 取出1级大类
		List<JsonData> mainMenuList = jsonDataService.getListByTag(JsonDataKey.TAG_HELPCENTER, null, "tag", false, 0, 20);
		// 取出1级对应的子级
		for (JsonData jsonData : mainMenuList) {
			List<JsonData> list = jsonDataService.getListByTag(jsonData.getDkey(), null, "tag", false, 0, 20);
			subdataMap.put(jsonData.getDkey(), list);
		}
		model.put("subdataMap", subdataMap);
		model.put("mainMenuList", mainMenuList);
		return "admin/common/confHelpcenter.vm";
	}

	@RequestMapping("/admin/common/saveHelpCenter.xhtml")
	public String saveHelpCenter(HttpServletRequest request, ModelMap model) {
		Map<String, String[]> dataMap = request.getParameterMap();
		String dkey = ServiceHelper.get(dataMap, "dkey");
		JsonData json = null;
		if (StringUtils.isNotBlank(dkey))
			json = daoService.getObject(JsonData.class, dkey);
		if (json == null)
			json = new JsonData(JsonDataKey.KEY_HELPCENTER + System.currentTimeMillis());
		String tag = ServiceHelper.get(dataMap, "tag");
		String relatedid = null;
		Map<String, String> map = new HashMap<String, String>();
		for (String k : dataMap.keySet()) {
			map.put(k, ServiceHelper.get(dataMap, k));
		}
		map.put("dkey", json.getDkey());
		if (StringUtils.isNotBlank(tag)) {
			relatedid = ServiceHelper.get(dataMap, "relatedid");
			if (StringUtils.isBlank(relatedid))
				return showJsonError(model, "子菜单帖子ID不能为空!");
			DiaryBase diary = diaryService.getDiaryBase(Long.valueOf(relatedid));
			if (diary == null) {
				return showJsonError(model, "不存在该贴子!");
			}
			json.setTag(tag + map.get("matchorder"));
		} else {
			json.setTag(JsonDataKey.TAG_HELPCENTER + map.get("matchorder"));
		}
		String data = JsonUtils.writeObjectToJson(map);
		json.setData(data);
		daoService.saveObject(json);
		return showJsonSuccess(model);
	}

	@RequestMapping("/admin/common/delHelpCenter.xhtml")
	public String delHelpCenter(ModelMap model, String key) {
		JsonData json = daoService.getObject(JsonData.class, key);
		if (json == null)
			return showJsonError(model, "该模块不存在...");
		Integer result = jsonDataService.countListByTag(key, null);
		if (result > 0)
			return showJsonError(model, "有子类存在，不能删除...");
		daoService.removeObject(json);
		return showJsonSuccess(model);
	}

	@RequestMapping("/admin/common/getHelpCenter.xhtml")
	public String getHelpCenter(ModelMap model, String key) {
		JsonData json = daoService.getObject(JsonData.class, key);
		if (json == null)
			return showJsonError(model, "该模块不存在...");
		return showJsonSuccess(model, VmUtils.readJsonToMap(json.getData()));
	}

	// 场次检查
	@RequestMapping("/admin/common/mpiCheck.xhtml")
	public String mpiCheck(HttpServletRequest request, ModelMap model) {
		String citycode = getAdminCitycode(request);
		Date begin = DateUtil.getBeginningTimeOfDay(new Date());
		List<Cinema> cinemaList = placeService.getPlaceList(citycode, Cinema.class, null, false, 0, 200);
		List<Date> dateList = new ArrayList<Date>();
		for (int i = 0; i <= 11; i++) {
			Date date = DateUtil.addDay(begin, i);
			dateList.add(date);
		}
		String sql = "select new map(m.cinemaid as cinemaid, to_char(m.playdate, 'yyyy-MM-dd') as pdate, count(*) as count)  ";
		sql = sql + "from MoviePlayItem m where m.citycode=? ";
		sql = sql + "group by m.cinemaid, to_char(m.playdate, 'yyyy-MM-dd') ";
		sql = sql + "order by m.cinemaid, to_char(m.playdate, 'yyyy-MM-dd')";
		List<Map> result = hibernateTemplate.find(sql, citycode);
		Map<String, Integer> countMap = new HashMap<String, Integer>();
		for (Map m : result) {
			countMap.put(m.get("cinemaid").toString() + m.get("pdate") + "", Integer.valueOf(m.get("count") + ""));
		}
		model.put("dateList", dateList);
		model.put("cinemaList", cinemaList);
		model.put("countMap", countMap);
		return "admin/common/mpiCheck.vm";
	}

	// 统一弹出框(包含搜索, 单选+复选)
	@RequestMapping("/admin/common/movieSelectBox.xhtml")
	public String movieSelectBox(String citycode, String tag, String queryname, String flag, Integer pageNo, ModelMap model) {
		// 1. 列出城市列表,
		if (StringUtils.isBlank(citycode)) {
			Map<String, String> cityMap = AdminCityContant.getCitycode2CitynameMap();
			model.put("cityMap", cityMap);
		} else {
			if (StringUtils.equals(tag, "movie")) {// 影片
				if (StringUtils.isBlank(queryname)) {
					if (pageNo == null)
						pageNo = 0;
					int rowsPerPage = 20;
					int firstRow = pageNo * rowsPerPage;
					// 当前搜索框-上映的影片
					List<Movie> movieList = mcpService.getCurMovieListByMpiCount(citycode, firstRow, rowsPerPage);
					List<Map> movieMap = BeanUtil.getBeanMapList(movieList, "id", "moviename");
					model.put("movieMap", movieMap);
					Integer count = mcpService.getCurMovieCount(citycode);
					model.put("moviecount", count);
					PageUtil pageUtil = new PageUtil(count, rowsPerPage, pageNo, "/admin/common/movieSelectBox.xhtml");
					Map paramMap = new HashMap();
					paramMap.put("citycode", citycode);
					paramMap.put("tag", tag);
					paramMap.put("queryname", queryname);
					paramMap.put("flag", flag);
					pageUtil.initPageInfo(paramMap);
					model.put("pageUtil", pageUtil);
				} else {
					List<Movie> movieList = mcpService.searchMovieByName(queryname);
					List<Map> movieMap = BeanUtil.getBeanMapList(movieList, "id", "moviename");
					model.put("movieMap", movieMap);
				}
			} else {// 影院
				if (StringUtils.isBlank(queryname)) {
					if (pageNo == null)
						pageNo = 0;
					int rowsPerPage = 20;
					int firstRow = pageNo * rowsPerPage;
					List<Cinema> cinemaList = mcpService.getCinemaListByCitycode(citycode, firstRow, rowsPerPage);
					List<Map> cinemaMap = BeanUtil.getBeanMapList(cinemaList, "id", "name");
					model.put("cinemaMap", cinemaMap);
					Integer count = mcpService.getCinemaCountByCitycode(citycode);
					model.put("cinemacount", count);
					PageUtil pageUtil = new PageUtil(count, rowsPerPage, pageNo, "/admin/common/movieSelectBox.xhtml");
					Map paramMap = new HashMap();
					paramMap.put("citycode", citycode);
					paramMap.put("tag", tag);
					paramMap.put("queryname", queryname);
					paramMap.put("flag", flag);
					pageUtil.initPageInfo(paramMap);
					model.put("pageUtil", pageUtil);
				} else {
					List<Cinema> cinemaList = placeService.searchPlaceByName(citycode, Cinema.class, queryname);
					List<Map> cinemaMap = BeanUtil.getBeanMapList(cinemaList, "id", "name");
					model.put("cinemaMap", cinemaMap);
				}
			}
		}
		model.put("citycode", citycode);
		model.put("tag", tag);
		model.put("queryname", queryname);
		model.put("flag", flag);
		return "admin/common/movieSelectBox.vm";
	}

	@RequestMapping("/admin/common/showUntransOrders.xhtml")
	public String showUntransOrders(Timestamp startTime, Timestamp endTime, Long count, String isXls, ModelMap model, HttpServletResponse response) {
		if (startTime == null || endTime == null || count == null)
			return "admin/common/untransOrders.vm";
		List<Map> memberCountList = getTicketOrderMemberIdAndCount(OrderConstant.STATUS_PAID_SUCCESS, startTime, endTime, count);
		model.put("memberCountList", memberCountList);
		for (Map map : memberCountList) {
			String memberid = (String) map.get("memberid");
			List<String> tradeNoList = getTicketOrderTradeNoList(Long.valueOf(memberid), OrderConstant.STATUS_PAID_SUCCESS, startTime, endTime);
			map.put("tradeNoList", tradeNoList);
		}
		if (StringUtils.equals(isXls, "email")) {
			model.put("startTime", DateUtil.format(startTime, "yyyy-MM-dd HH:mm"));
			model.put("endTime", DateUtil.format(endTime, "yyyy-MM-dd HH:mm"));
			if (!memberCountList.isEmpty()) {
				//TODO:与其他合并
				monitorService.saveSysTemplateWarn("异常购票用户提醒", "warn/unusualOrders.vm", model, RoleTag.unusualorder);
				return showJsonSuccess(model, "Email发送成功");
			}
			return showJsonError(model, "没有数据发送");
		} else if (StringUtils.equals(isXls, "xls")) {
			download("xls", response);
			return "admin/common/untransXls.vm";
		}
		return "admin/common/untransOrders.vm";
	}

	/**
	 * 查询在一段时间内订单数量大于count的用户
	 * **/
	private List<Map> getTicketOrderMemberIdAndCount(String status, Timestamp startTime, Timestamp endTime, Long count) {
		String hql = "select new map(to_char(t.memberid) as memberid, to_char(count(t.memberid)) as num ) from TicketOrder t where t.status=? and t.paidtime>=? and t.paidtime<=? group by t.memberid having count(t.memberid)>? order by count(t.memberid) desc";
		List<Map> result = readOnlyTemplate.find(hql, status, startTime, endTime, count);
		return result;
	}

	private List<String> getTicketOrderTradeNoList(Long memberid, String status, Timestamp startTime, Timestamp endTime) {
		String hql = "select tradeNo from TicketOrder t where t.memberid=? and t.status=? and t.paidtime>=? and t.paidtime<=?";
		List<String> result = readOnlyTemplate.find(hql, memberid, status, startTime, endTime);
		return result;
	}

	@RequestMapping("/admin/common/getChangeHis.xhtml")
	public String getChangeHis(String tag, String relatedid, ModelMap model) {
		if (StringUtils.isBlank(tag) || StringUtils.isBlank(relatedid)) {
			return "admin/common/changeHis.vm";
		}
		Map<Long, Map<String, String>> result = changeLogService.getChangeLogList(Config.SYSTEMID, tag, relatedid);
		model.put("logList", result.values());
		return "admin/common/changeHis.vm";
	}
	@RequestMapping("/admin/common/getBatchChangeHis")
	public String getBatchChangeHis(ModelMap model){
		String relatedidList[] = new String[]{"16182095","16182059","16035340","16182639","15736773","16151830","16140834","16037674","16037698","16182596","16037703","16182064","16182088","16182053","16151846","15736826","16182079","16037680","16151790","16032915","16182104","16037676","16182611","16182604","15736802","16038785","16182069","16038797","16182056","16037673","16182110","16182060","16182612","16182082","16037684","16182065","16566108","16035778","16182589","16035765","16182100","16182605","16035746","16037716","15892840","16035345","16182638","16151824","16035753","16037720","15736777","15892812","15736823","16037701","16038750","16182623","16035777","16037710","16151806","16037692","16038775","16182613","15736765","16037717","16035344","16035771","16035348","16976062","15892836","16038760","16037719","16037709","15736824","16035351","16182081","15736767","16032925","16038774","16182090","16151796","16182061","16038763","16035740","16038754","16151839","16038808","16035739","16151852","15892818","16151815","16182632","16182674","16035785","16032935","16037693","16151849","16037685","16035764","16037694","16182600","16182084","16182085","16035763","16035760","16038792","16182590","16182599","16151817","16182603","16182616","16182581","16182663","15892802","15892805","16037715","16151844","15736813","15892824","15892856","16038756","16035350","16032950","16182588","16182678","16182077","15736837","16037711","16032968","16182637","16151799","15736803","16151833","16182105","16182580","15892844","16037669","16032918","16038758","16151793","16032962","16035757","16182650","16037691","16182587","16182682","16035749","15892803","15892838","15892825","16032916","16182677","15736838","16037702","16037695","16035772","16037708","16182074","16151800","16320972","15892834","16038804","16182595","15736763","15892839","16037687","16038803","16035761","16151820","16182684","16032953","16182109","16151828","16037705","16032952","16151791","16182670","16182102","16182634","16182093","16038744","16037718","16182080","16182108","16182629","15736828","16038770","16182103","16037679","16182608","16037697","16032961","16070804","15659096","15808657","16235907","16059533","16223542","16059573","15808638","16111688","16235931","16223555"};
		List<String> msgList = new ArrayList<String>();
		for(String relatedid: relatedidList){
			Map<Long, Map<String, String>> result = changeLogService.getChangeLogList(Config.SYSTEMID, "MoviePlayItem", relatedid);
			if(result.size()==1){
				msgList.add(result.values().iterator().next().toString());
			}
		}
		return forwardMessage(model, msgList);
	}
	/**
	 * 把comment中@用户名: 替换成@用户名+空格
	 * 
	 * @param from
	 * @param maxnum
	 */
	@RequestMapping("/admin/common/replaceCommentBody.xhtml")
	public void replaceCommentBody(Integer from, Integer maxnum) {
		DetachedCriteria query = DetachedCriteria.forClass(Comment.class);
		query.add(Restrictions.like("body", "%@%", MatchMode.ANYWHERE));
		query.addOrder(Order.desc("addtime"));
		List<Comment> commentList = hibernateTemplate.findByCriteria(query, from, maxnum);
		if (commentList.isEmpty())
			return;
		for (Comment comment : commentList) {
			String body = comment.getBody();
			body = StringUtil.substitute(body, "@([^:]+):", "@$1 ", true);
			comment.setBody(body);
			commentService.updateComment(comment);
		}
	}

	// 需要处理的系统警告信息
	@RequestMapping("/admin/common/warnList.xhtml")
	public String sysWarnList(String role, String adddate, String searchkey, String status, String jsonParams, String searchField, ModelMap model) {
		Map<String, String> query = new HashMap<String, String>();
		query.put("role", role);
		if (StringUtils.isNotBlank(status)) {
			query.put("status", status);
		}
		if (StringUtils.isNotBlank(jsonParams)) {
			Map<String, String> jmap = JsonUtils.readJsonToMap(jsonParams);
			query.putAll(jmap);
		}
		long endtime = System.currentTimeMillis();
		long starttime = endtime - DateUtil.m_day * 10;
		if (StringUtils.isNotBlank(adddate)) {
			starttime = DateUtil.parseDate(adddate).getTime();
			endtime = starttime + DateUtil.m_day;
		}
		Map<String, String> likeQuery = new HashMap<String, String>();
		if (StringUtils.isNotBlank(searchkey)){
			if(StringUtils.isBlank(searchField)){
				searchField = "content";
			}
			likeQuery.put(searchField, searchkey);
			model.put("searchkey", searchkey);
		}
		List<Map<String, String>> warnList = hbaseService.getRowListByRange(HbaseData.TABLE_SYSWARN, query, likeQuery, starttime, endtime, 2000);
		model.put("warnList", warnList);
		
		return "admin/common/sysWarnList.vm";
	}

	@RequestMapping("/admin/common/updateSysWarn.xhtml")
	public String updateSysWarn(String id, ModelMap model) {
		Map<String, String> warn = hbaseService.getRowByHex(HbaseData.TABLE_SYSWARN, id);
		User user = getLogonUser();
		String userKey = "@" + user.getId() + "@";
		warn.put("auser", warn.get("auser") + userKey);
		warn.put("status", "Y");
		hbaseService.saveRow(HbaseData.TABLE_SYSWARN, id, warn);
		return showJsonSuccess(model);
	}

	@RequestMapping("/admin/common/multiupdateSysWarn.xhtml")
	public String multiupdateSysWarn(String[] eid, String role, String adddate, String status, ModelMap model) {
		if (eid != null) {
			List<Map<String, String>> rowList = hbaseService.getRowList(HbaseData.TABLE_SYSWARN, Arrays.asList(eid));
			User user = getLogonUser();
			for (Map<String, String> warn : rowList) {
				String userKey = "@" + user.getId() + "@";
				warn.put("auser", warn.get("auser") + userKey);
				warn.put("status", "Y");
			}
			hbaseService.saveRowListByHex(HbaseData.TABLE_SYSWARN, "rowid", rowList);
		}
		model.put("role", role);
		model.put("adddate", adddate);
		model.put("status", status);
		return "redirect:/admin/common/warnList.xhtml";
	}

	/**
	 * Admin Ajax 加载省市县
	 **/
	@RequestMapping("/admin/common/ajaxLoadAddress.xhtml")
	public String ajaxLoadAddress(String tag, String provincecode, String citycode, String countycode, String agtag, ModelMap model) {
		if (StringUtils.isBlank(tag)) {
			List<Province> list = placeService.getAllProvinces();
			List<Map> provinceMap = BeanUtil.getBeanMapList(list, "provincecode", "provincename");
			model.put("provinceMap", provinceMap);
		} else if (StringUtils.equals(tag, "province")) {
			List<City> list = placeService.getCityByProvinceCode(provincecode);
			List<Map> cityMap = BeanUtil.getBeanMapList(list, "citycode", "cityname");
			model.put("cityMap", cityMap);
		} else if (StringUtils.equals(tag, "city")) {
			List<County> list = placeService.getCountyByCityCode(citycode);
			List<Map> countyMap = BeanUtil.getBeanMapList(list, "countycode", "countyname");
			model.put("countyMap", countyMap);
		} else if (StringUtils.equals(tag, "county")) {
			List<Indexarea> list = placeService.getIndexareaByCountyCode(countycode);
			List<Map> indexareaMap = BeanUtil.getBeanMapList(list, "indexareacode", "indexareaname");
			model.put("indexareaMap", indexareaMap);
		}
		model.put("agtag", agtag);
		return "admin/common/locationAddress.vm";
	}

	@RequestMapping("/admin/common/getIndexarea.xhtml")
	public String getIndexarea(@RequestParam("indexareacode") String indexareacode, ModelMap model) {
		Indexarea indexarea = daoService.getObject(Indexarea.class, indexareacode);
		if (indexarea == null)
			return showJsonError(model, "不存在该商圈或被删除！");
		Map result = BeanUtil.getBeanMap(indexarea, true);
		return showJsonSuccess(model, result);
	}

	@RequestMapping("/admin/common/saveIndexarea.xhtml")
	public String saveIndexarea(@RequestParam("countycode") String countycode, String indexareacode,
			@RequestParam("indexareaname") String indexareaname, ModelMap model) {
		County county = daoService.getObject(County.class, countycode);
		if (county == null)
			return showJsonError(model, "不存在该地区或被删除！");
		if (StringUtils.isBlank(indexareaname))
			return showJsonError(model, "商圈名称不能为空！");
		Indexarea indexarea = null;
		if (StringUtils.isNotBlank(indexareacode)) {
			indexarea = daoService.getObject(Indexarea.class, indexareacode);
			if (indexarea == null)
				return showJsonError(model, "不存在该商圈或被删除！");
			indexarea.setIndexareaname(indexareaname);
		} else {
			String hql = "select max(indexareacode) from Indexarea i where i.county.countycode=?";
			List<String> maxcodeList = hibernateTemplate.find(hql, countycode);
			int indexareaValue = 0;
			if (!maxcodeList.isEmpty())
				indexareaValue = Integer.valueOf(maxcodeList.get(0)) + 1;
			else
				indexareaValue = Integer.valueOf(countycode + "001");
			indexarea = new Indexarea(String.valueOf(indexareaValue), county, indexareaname);
		}
		daoService.saveObject(indexarea);
		return showJsonSuccess(model);
	}

	//粉丝数量不相等数据升级
	@RequestMapping("/admin/common/updateFansCount.xhtml")
	public String updateFansCount(Long memberid, ModelMap model) {
		Map map = mongoService.findOne(MongoData.NS_MEMBERCOUNT, MongoData.DEFAULT_ID_NAME, memberid);
		if(map!=null){
			if(map.containsKey("fanscount")){
				Integer fc1 = Integer.valueOf(map.get("fanscount")+"");
				Integer fc2 = getFanscount(memberid);
				if(!fc1.equals(fc2)){
					map.put("fanscount", fc2);
					mongoService.saveOrUpdateMap(map, MongoData.DEFAULT_ID_NAME, MongoData.NS_MEMBERCOUNT);
					return forwardMessage(model, "memberid:" + memberid + "," + fc1 + " --->" + fc2);
				}else {
					return forwardMessage(model, "memberid:" + memberid + "数据相等没有更新！");
				}
			}
		}
		return forwardMessage(model, "数据不存在！");
	}
	@RequestMapping("/admin/common/updateInterestcount.xhtml")
	public String updateInterestcount(Long memberid, ModelMap model) {
		Map map = mongoService.findOne(MongoData.NS_MEMBERCOUNT, MongoData.DEFAULT_ID_NAME, memberid);
		if(map!=null){
			if(map.containsKey("interestcount")){
				Integer fc1 = Integer.valueOf(map.get("interestcount")+"");
				Integer fc2 = getInterestcount(memberid);
				if(!fc1.equals(fc2)){
					map.put("interestcount", fc2);
					mongoService.saveOrUpdateMap(map, MongoData.DEFAULT_ID_NAME, MongoData.NS_MEMBERCOUNT);
					return forwardMessage(model, "memberid:" + memberid + "," + fc1 + " --->" + fc2);
				}else {
					return forwardMessage(model, "memberid:" + memberid + "数据相等没有更新！");
				}
			}
		}
		return forwardMessage(model, "数据不存在！");
	}
	private Integer getFanscount(Long memberid){
		String hql = "select count(*) from Treasure where relatedid=? and tag=? and action=?";
		List list = hibernateTemplate.find(hql, memberid, Treasure.TAG_MEMBER, Treasure.ACTION_COLLECT);
		return Integer.valueOf(list.get(0)+"");
	}
	
	private Integer getInterestcount(Long memberid){
		String hql = "select count(*) from Treasure where memberid=? and tag=? and action=?";
		List list = hibernateTemplate.find(hql, memberid, Treasure.TAG_MEMBER, Treasure.ACTION_COLLECT);
		return Integer.valueOf(list.get(0)+"");
	}
	
	@RequestMapping("/admin/common/qryOrderResult.xhtml")
	public String qryOrderResult(String tradeno, ModelMap model) {
		String vm = "admin/common/orderResult.vm";
		if(StringUtils.isBlank(tradeno)) return vm;
		ErrorCode<List<TakeInfo>> ticode = terminalService.getTakeInfoList(tradeno);
		model.put("takeInfoList", ticode.getRetval());
		return vm;
	}
	@RequestMapping("/admin/common/callbackOrder.xhtml")
	public String pushOrder(String tradeno, ModelMap model) {
		String vm = "admin/common/callbackOrder.vm";
		if(StringUtils.isBlank(tradeno)) return vm;
		GewaOrder order = daoService.getObjectByUkey(GewaOrder.class, "tradeNo", tradeno, false);
		if(order==null){
			model.put("msg","订单不存在，请确认输入的订单号!!");
			return vm;
		}
		CallbackOrder backOrder = daoService.getObject(CallbackOrder.class, order.getId());
		model.put("backOrder", backOrder);
		model.put("order", order);
		return vm;
	}
	
}
