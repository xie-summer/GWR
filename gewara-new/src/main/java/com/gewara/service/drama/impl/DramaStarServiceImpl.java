package com.gewara.service.drama.impl;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.type.StringType;
import org.hibernate.type.Type;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.gewara.constant.TagConstant;
import com.gewara.constant.content.CommonType;
import com.gewara.constant.sys.CacheConstant;
import com.gewara.model.drama.DramaStar;
import com.gewara.service.content.NewsService;
import com.gewara.service.content.PictureService;
import com.gewara.service.content.VideoService;
import com.gewara.service.drama.DramaStarService;
import com.gewara.service.drama.DramaToStarService;
import com.gewara.service.impl.BaseServiceImpl;
import com.gewara.support.MultiPropertyComparator;
import com.gewara.untrans.CacheService;
import com.gewara.util.BeanUtil;
import com.gewara.util.ClassUtils;

@Service("dramaStarService")
public class DramaStarServiceImpl extends BaseServiceImpl implements DramaStarService {
	@Autowired
	private DramaToStarService dramaToStarService;
	public void setDramaToStarService(DramaToStarService dramaToStarService) {
		this.dramaToStarService = dramaToStarService;
	}
	@Autowired@Qualifier("newsService")
	private NewsService newsService;
	public void setNewsService(NewsService newsService) {
		this.newsService = newsService;
	}
	@Autowired
	private PictureService pictureService;
	public void setPictureService(PictureService pictureService) {
		this.pictureService = pictureService;
	}
	@Autowired
	private VideoService videoService;
	public void setVideoService(VideoService videoService) {
		this.videoService = videoService;
	}
	@Autowired
	private CacheService cacheService;
	
	private static final String TAG_DRAMASTAR = DramaStar.TAG_DRAMASTAR;
	
	/**
	 *  明星列表 - 暂时无条件
	 *  2010-12-09 列表显示附加详细信息的明星 (显示条件 xx属性 不为空)
	 */
	@Override
	public List<DramaStar> getStarList(String order, String property, String startype, int from, int maxnum) {
		DetachedCriteria query = DetachedCriteria.forClass(DramaStar.class);
		if(StringUtils.isNotBlank(property)){
			query.add(Restrictions.isNotNull(property));
		}
		if(StringUtils.isNotBlank(order)){
			query.addOrder(Order.desc(order));
			query.addOrder(Order.desc("id"));
		}else{
			query.addOrder(Order.desc("addtime"));
		}
		if(StringUtils.isNotBlank(startype)){
			query.add(Restrictions.like("startype", startype, MatchMode.ANYWHERE));
		}
		return hibernateTemplate.findByCriteria(query, from, maxnum);
	}
	
	/**
	 * 	
	 * 新版人物、社团查询
	 */
	@Override
	public List<DramaStar> getDramaStarList(String order, String startype, String searchkey, String troupecompany, int from, int maxnum, String...notNullPropertys) {
		DetachedCriteria query = DetachedCriteria.forClass(DramaStar.class);
		if(notNullPropertys != null){
			for(String property : notNullPropertys){
				query.add(Restrictions.isNotNull(property));
			}
		}
		if(StringUtils.isNotBlank(searchkey)){
			query.add(Restrictions.like("name", searchkey, MatchMode.ANYWHERE));
		}
		if(StringUtils.isNotBlank(troupecompany)){
			query.add(Restrictions.eq("name", troupecompany));
		}
		if(StringUtils.isNotBlank(order)){
			query.addOrder(Order.desc(order));
		}else{
			query.addOrder(Order.desc("hotvalue"));
			query.addOrder(Order.desc("clickedtimes"));
		}
		query.add(Restrictions.eq("tag", TagConstant.TAG_DRAMA));
		query.addOrder(Order.asc("id"));
		if(StringUtils.isNotBlank(startype)){
			query.add(Restrictions.like("startype", startype, MatchMode.ANYWHERE));
		}else{
			query.add(Restrictions.sqlRestriction(" startype not like ?",new Object[]{"%" + DramaStar.TYPE_TROUPE + "%"},new Type[]{new StringType()}));
		}
		return hibernateTemplate.findByCriteria(query, from, maxnum);
	}
	
	@Override
	public List<DramaStar> getDramaStarListGroupTroupe(String startype, String searchKey, int from, int maxnum){
		String key = CacheConstant.buildKey("getStarIdListGroupTroupe", startype);
		List<Long> idList = (List<Long>) cacheService.get(CacheConstant.REGION_HALFDAY, key);
		if(idList == null){
			DetachedCriteria query = DetachedCriteria.forClass(DramaStar.class);
			query.add(Restrictions.like("startype", startype, MatchMode.ANYWHERE));
			query.setProjection(Projections.projectionList().add(Projections.groupProperty("troupe"),"troupeid")
					.add(Projections.count("troupe"), "starCount"));
			query.setResultTransformer(DetachedCriteria.ALIAS_TO_ENTITY_MAP);
	 		List<Map> result = hibernateTemplate.findByCriteria(query);
	 		Collections.sort(result, new MultiPropertyComparator(new String[]{"starCount"}, new boolean[]{false}));
	 		idList = BeanUtil.getBeanPropertyList(result, Long.class, "troupeid", true);
	 		cacheService.set(CacheConstant.REGION_HALFDAY, key, idList);
		}
		List<DramaStar> starList = new ArrayList<DramaStar>();
		if(StringUtils.isNotBlank(searchKey)){
			List<DramaStar> dramaStarList = baseDao.getObjectList(DramaStar.class, idList);
			for (DramaStar dramaStar : dramaStarList) {
				if(StringUtils.containsAny(dramaStar.getName(), StringUtils.trim(searchKey))){
					starList.add(dramaStar);
				}
			}
			starList = BeanUtil.getSubList(starList, from, maxnum);
		}else{
			List<Long> tmpIdList = BeanUtil.getSubList(idList, from, maxnum);
			starList = baseDao.getObjectList(DramaStar.class, tmpIdList);
		}
		return starList;
	}
	
	@Override
	public Integer getStarCount(String startype,String searchkey, String...notNullPropertys){
		DetachedCriteria query = DetachedCriteria.forClass(DramaStar.class);
		query.setProjection(Projections.rowCount());
		if(notNullPropertys != null){
			for(String property : notNullPropertys){
				query.add(Restrictions.isNotNull(property));
			}
		}
		if(StringUtils.isNotBlank(startype)){
			query.add(Restrictions.like("startype", startype, MatchMode.ANYWHERE));
		}else{
			query.add(Restrictions.sqlRestriction(" startype not like ?", new Object[]{"%" + DramaStar.TYPE_TROUPE + "%"}, new Type[]{new StringType()}));
		}
		if(StringUtils.isNotBlank(searchkey)){
			query.add(Restrictions.like("name", searchkey, MatchMode.ANYWHERE));
		}
		query.add(Restrictions.eq("tag", TagConstant.TAG_DRAMA));
		List result = hibernateTemplate.findByCriteria(query);
		if(result.isEmpty()) return 0;
		return Integer.parseInt("" + result.get(0)); 
	}
	
	/**
	 *  根据场馆ID查找明星或教练
	 */
	@Override
	public List<DramaStar> getStarListByTroupid(Long starid, String orderField, boolean asc, int from, int maxnum){
		DetachedCriteria query = DetachedCriteria.forClass(DramaStar.class);
		query.add(Restrictions.eq("troupe", starid));
		if(ClassUtils.hasMethod(DramaStar.class, "get" + StringUtils.capitalize(orderField))){
			if(asc){
				query.addOrder(Order.asc(orderField));
			}else{
				query.addOrder(Order.desc(orderField));
			}
		}
		query.addOrder(Order.desc("clickedtimes"));
		return hibernateTemplate.findByCriteria(query, from, maxnum);
	}
	@Override
	public Integer getStarCountByTroupid(Long starid){
		DetachedCriteria query = DetachedCriteria.forClass(DramaStar.class);
		query.setProjection(Projections.rowCount());
		query.add(Restrictions.eq("troupe", starid));
		List result = hibernateTemplate.findByCriteria(query);
		if(result.isEmpty()) return 0;
		return Integer.parseInt("" + result.get(0));
	}
	
	/**
	 * 根据state + name + type查找
	 * state: 州名
	 * name: 明星名(支持like)
	 * type: 个人/剧团
	 */
	@Override
	public List<DramaStar> getStarListByStateAndName(String state, String name, String startype, int from, int maxnum){
		DetachedCriteria query = DetachedCriteria.forClass(DramaStar.class);
		if(StringUtils.isNotBlank(startype))query.add(Restrictions.like("startype", startype, MatchMode.ANYWHERE));
		if(StringUtils.isNotBlank(state)){
			query.add(Restrictions.eq("state", state));
		}
		if(StringUtils.isNotBlank(name)){
			//query.add(Restrictions.like("name", name, MatchMode.ANYWHERE)); // 只适用于单个人
			name = StringUtils.trim(name);
			String[] names = StringUtils.split(name, " ");
			StringBuilder sb = new StringBuilder();
			for(String s : names){
				sb.append("NAME LIKE '%"+s+"%' OR ");
			}
			sb.append(" 1=0 ");
			query.add(Restrictions.sqlRestriction(sb.toString()));
		}
		query.add(Restrictions.eq("tag", TagConstant.TAG_DRAMA));
		List list = hibernateTemplate.findByCriteria(query, from, maxnum); 
		return list;
	}
	@Override
	public Integer getStarCountByStateAndName(String state, String name, String startype){
		DetachedCriteria query = DetachedCriteria.forClass(DramaStar.class);
		query.setProjection(Projections.rowCount());
		if(StringUtils.isBlank(startype)){
			startype = DramaStar.TYPE_STAR;	// 个人
		}
		if(StringUtils.isNotBlank(state)){
			query.add(Restrictions.eq("state", state));
		}
		if(StringUtils.isNotBlank(name)){
			query.add(Restrictions.like("name", name, MatchMode.ANYWHERE));
		}
		query.add(Restrictions.like("startype", startype, MatchMode.ANYWHERE));
		query.add(Restrictions.eq("tag", TagConstant.TAG_DRAMA));
		List result = hibernateTemplate.findByCriteria(query);
		if(result.isEmpty()) return 0;
		return Integer.parseInt("" + result.get(0));
	}

	@Override
	public Map getFavStarListProperty(String citycode, List<DramaStar> starlist){
		List<Long> idlist = BeanUtil.getBeanPropertyList(starlist, Long.class, "id", true);
		Map<Long, Map<String, Integer>> dataMap = new HashMap<Long, Map<String,Integer>>();
		for(Long id : idlist){
			Map<String, Integer> subMap = new HashMap<String, Integer>();
			subMap.put("showCount", dramaToStarService.getDramaCountByStarid(id));
			subMap.put("newCount", newsService.getNewsCount(citycode, TAG_DRAMASTAR, CommonType.NEWSTYPE_NEWS, id, null));
			subMap.put("videoCount", videoService.getVideoCountByTag(TAG_DRAMASTAR, id));
			subMap.put("picCount", pictureService.getPictureCountByRelatedid(TAG_DRAMASTAR, id));
			dataMap.put(id, subMap);
		}
		return dataMap;
	}
	
	
	
	/****
	 * 	后台查询 - 根据类型 + name 模糊匹配
	 * */
	@Override
	public List<DramaStar> getStarListByName(String type, String name, String startype, int from, int maxnum){
		DetachedCriteria query = DetachedCriteria.forClass(DramaStar.class);
		if(StringUtils.isBlank(type)) type = TagConstant.TAG_DRAMA;
		if (StringUtils.isNotBlank(name))
			query.add(Restrictions.like("name", name, MatchMode.ANYWHERE));
		if(StringUtils.isNotBlank(startype))
			query.add(Restrictions.like("startype", startype, MatchMode.ANYWHERE));
		query.add(Restrictions.eq("tag", type));
		query.addOrder(Order.desc("addtime"));
		return hibernateTemplate.findByCriteria(query, from, maxnum);
	}
	@Override
	public DramaStar getDramaStarByName(String name, String startype){
		List<DramaStar> dramaStarList = getStarListByName(TagConstant.TAG_DRAMA ,name, startype, 0, 1);
		if(dramaStarList.isEmpty()) return null;
		return dramaStarList.get(0);
	}
	@Override
	public int getStarCountByName(String type, String name, String startype){
		DetachedCriteria query = DetachedCriteria.forClass(DramaStar.class);
		query.setProjection(Projections.rowCount());
		if(StringUtils.isBlank(type)) type = TagConstant.TAG_DRAMA;
		if (StringUtils.isNotBlank(name))
			query.add(Restrictions.like("name", name, MatchMode.ANYWHERE));
		if(StringUtils.isNotBlank(startype))
			query.add(Restrictions.like("startype", startype, MatchMode.ANYWHERE));
		query.add(Restrictions.eq("tag", type));
		List starList = hibernateTemplate.findByCriteria(query);
		if(starList.isEmpty()) return 0;
		return new Integer(starList.get(0)+"");
	}
	@Override
	public List<DramaStar> getDramaStarListByTroupe(String type, Long troupe, String starType, int from, int maxnum, String...notNullPropertys){
		DetachedCriteria query = DetachedCriteria.forClass(DramaStar.class,"st");
		if(StringUtils.isBlank(type)) type = TagConstant.TAG_DRAMA;
		if(notNullPropertys != null){
			for(String property : notNullPropertys){
				query.add(Restrictions.isNotNull(property));
			}
		}
		query.add(Restrictions.eq("tag", type));
		query.add(Restrictions.eq("st.troupe", troupe));
		if(StringUtils.isNotBlank(starType))query.add(Restrictions.like("st.startype", starType,MatchMode.ANYWHERE));
		return hibernateTemplate.findByCriteria(query,from, maxnum);
	}
	@Override
	public List<DramaStar> getSynchStarList(Timestamp lasttime) {
		DetachedCriteria query = DetachedCriteria.forClass(DramaStar.class);
		query.add(Restrictions.eq("tag", TagConstant.TAG_DRAMA));
		query.add(Restrictions.ge("updatetime", lasttime));
		return hibernateTemplate.findByCriteria(query);
	}
}
