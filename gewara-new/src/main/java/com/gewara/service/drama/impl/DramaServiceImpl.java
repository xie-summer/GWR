package com.gewara.service.drama.impl;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.hibernate.Hibernate;
import org.hibernate.criterion.Conjunction;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.ProjectionList;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.criterion.Subqueries;
import org.hibernate.type.Type;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.support.PropertyComparator;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.gewara.constant.OdiConstant;
import com.gewara.constant.Status;
import com.gewara.constant.sys.CacheConstant;
import com.gewara.model.drama.Drama;
import com.gewara.model.drama.DramaPlayItem;
import com.gewara.model.drama.DramaStar;
import com.gewara.model.drama.DramaToStar;
import com.gewara.model.drama.OpenDramaItem;
import com.gewara.service.drama.DramaPlayItemService;
import com.gewara.service.drama.DramaService;
import com.gewara.service.impl.BaseServiceImpl;
import com.gewara.support.NullPropertyOrder;
import com.gewara.untrans.CacheService;
import com.gewara.util.BeanUtil;
import com.gewara.util.ClassUtils;
import com.gewara.util.DateUtil;
@Service("dramaService")
public class DramaServiceImpl extends BaseServiceImpl implements DramaService {
	
	@Autowired@Qualifier("dramaPlayItemService")
	protected DramaPlayItemService dramaPlayItemService;
	public void setDramaPlayItemService(DramaPlayItemService dramaPlayItemService){
		this.dramaPlayItemService = dramaPlayItemService;
	}
	
	@Autowired@Qualifier("cacheService")
	protected CacheService cacheService;
	public void setCacheService(CacheService cacheService){
		this.cacheService = cacheService;
	}
	
	@Override
	public List<Drama> getHotDrama(String citycode, String order,int from,int maxnum) {
		DetachedCriteria query = DetachedCriteria.forClass(Drama.class);
		query.add(Restrictions.eq("citycode", citycode));
		if(StringUtils.isNotBlank(order)){
			query.addOrder(Order.desc(order));
		}else{
			query.addOrder(Order.desc("id"));
		}
		List<Drama> dramaList = hibernateTemplate.findByCriteria(query, from, maxnum);
		return dramaList;
	}
	@Override
	public List<Long> getNowDramaList(String citycode, int from, int maxnum){
		Timestamp curtime = DateUtil.getCurTruncTimestamp();
		DetachedCriteria query = DetachedCriteria.forClass(OpenDramaItem.class, "odi");
		query.add(Restrictions.eq("odi.status", OdiConstant.STATUS_BOOK));
		query.add(Restrictions.le("odi.opentime", curtime));
		query.add(Restrictions.gt("odi.closetime", curtime));
		if(StringUtils.isNotBlank(citycode))query.add(Restrictions.eq("odi.citycode", citycode));
		query.setProjection(Projections.groupProperty("odi.dramaid"));
		
		DetachedCriteria subquery = DetachedCriteria.forClass(Drama.class, "dr");
		if(StringUtils.isNotBlank(citycode))subquery.add(Restrictions.eq("dr.citycode", citycode));
		subquery.add(Restrictions.eqProperty("odi.dramaid", "dr.id"));
		subquery.setProjection(Projections.property("dr.id"));
		query.add(Subqueries.exists(subquery));
		List<Long> dramaIdList=hibernateTemplate.findByCriteria(query, from, maxnum);
		return dramaIdList;
	}
	@Override
	public Integer getNowDramaCount(String citycode){
		Timestamp curtime = DateUtil.getCurTruncTimestamp();
		DetachedCriteria query = DetachedCriteria.forClass(OpenDramaItem.class, "odi");
		query.add(Restrictions.eq("odi.status", OdiConstant.STATUS_BOOK));
		query.add(Restrictions.le("odi.opentime", curtime));
		query.add(Restrictions.gt("odi.closetime", curtime));
		if(StringUtils.isNotBlank(citycode))query.add(Restrictions.eq("odi.citycode", citycode));
		query.setProjection(Projections.countDistinct("odi.dramaid"));
		DetachedCriteria subquery = DetachedCriteria.forClass(Drama.class, "dr");
		if(StringUtils.isNotBlank(citycode))subquery.add(Restrictions.eq("dr.citycode", citycode));
		subquery.add(Restrictions.eqProperty("odi.dramaid", "dr.id"));
		subquery.setProjection(Projections.property("dr.id"));
		query.add(Subqueries.exists(subquery));
		List<Long> dramaIdList=hibernateTemplate.findByCriteria(query);
		if(dramaIdList.isEmpty()) return 0;
		return new Integer(dramaIdList.get(0)+"");
	}
	@Override
	public List<OpenDramaItem> getOpenDramaItemListBydramaid(String citycode, Long dramaid){
		return getOpenDramaItemListBydramaid(citycode, dramaid, null, null);
	}
	@Override
	public List<OpenDramaItem> getOpenDramaItemListBydramaid(String citycode, Long dramaid, Long theatreid, Boolean isOpenPartner){
		Timestamp curtime = DateUtil.getCurFullTimestamp();
		DetachedCriteria query = DetachedCriteria.forClass(OpenDramaItem.class);
		if(theatreid!=null) {
			query.add(Restrictions.eq("theatreid", theatreid));
		}
		query.add(Restrictions.le("opentime", curtime));
		query.add(Restrictions.gt("closetime", curtime));
		Conjunction con1 = Restrictions.conjunction();
		con1.add(Restrictions.ge("playtime", curtime));
		con1.add(Restrictions.eq("period", Status.Y));
		Conjunction con2 = Restrictions.conjunction();
		con2.add(Restrictions.ge("endtime", curtime));
		con2.add(Restrictions.eq("period", Status.N));
		query.add(Restrictions.or(con1, con2));
		query.add(Restrictions.eq("dramaid", dramaid));
		query.add(Restrictions.eq("citycode", citycode));
		if(isOpenPartner != null){
			if(isOpenPartner){ 
				query.add(Restrictions.like("status", DramaPlayItem.STATUS_Y, MatchMode.START));
				query.add(Restrictions.eq("partner", Status.Y));
			}else{
				query.add(Restrictions.eq("status", DramaPlayItem.STATUS_Y));
			}
		}else{
			query.add(Restrictions.eq("status", DramaPlayItem.STATUS_Y));
		}
		query.addOrder(Order.asc("playtime"));
		List<OpenDramaItem> openDramItemList=hibernateTemplate.findByCriteria(query);
		return openDramItemList;
	}
	@Override
	public List<Drama> getDramaListByName(String citycode, String name,int from,int maxnum) {
		List<Drama> dramaList = new ArrayList<Drama>();
		if(StringUtils.isBlank(name)) return dramaList;
		String hql = "from Drama d where d.dramaname like ? and d.citycode = ? order by d.addtime desc";
		dramaList = queryByRowsRange(hql, from, maxnum, "%"+name+"%", citycode);
		return dramaList;
	}
	@Override
	public List<Drama> getCurDramaList(String citycode, Date fromDate, String order, int from, int maxnum){
		DetachedCriteria query = DetachedCriteria.forClass(Drama.class);
		query.add(Restrictions.le("releasedate", fromDate));
		query.add(Restrictions.ge("enddate", fromDate)); 
		query.add(Restrictions.eq("citycode", citycode));
		if(StringUtils.isNotBlank(order)) query.addOrder(Order.desc(order));
		query.addOrder(Order.desc("releasedate"));
		List<Drama> dramaList = hibernateTemplate.findByCriteria(query, from, maxnum);
		return dramaList;
	}
	@Override
	public List<Drama> getCurDramaList(Long theatreid, String order, int from, int maxnum) {
		Date fromDate = DateUtil.getCurDate();
		Timestamp fromtime = DateUtil.getBeginTimestamp(fromDate);
		DetachedCriteria query = DetachedCriteria.forClass(Drama.class,"d");
		query.add(Restrictions.le("releasedate", fromDate));
		query.add(Restrictions.ge("enddate", fromDate)); 
		DetachedCriteria sub = DetachedCriteria.forClass(DramaPlayItem.class, "item");
		Conjunction con1 = Restrictions.conjunction();
		con1.add(Restrictions.lt("item.playtime", fromtime));
		con1.add(Restrictions.eq("item.period", Status.Y));
		Conjunction con2 = Restrictions.conjunction();
		con2.add(Restrictions.lt("item.endtime", fromtime));
		con2.add(Restrictions.eq("item.period", Status.N));
		sub.add(Restrictions.or(con1, con2));
		sub.add(Restrictions.eq("item.theatreid", theatreid));
		sub.add(Restrictions.eqProperty("item.dramaid", "d.id"));
		sub.setProjection(Projections.property("item.id"));
		query.add(Subqueries.exists(sub));
		if(StringUtils.isNotBlank(order)) query.addOrder(Order.asc(order));
		List<Drama> dramaList = hibernateTemplate.findByCriteria(query, from, maxnum);
		return dramaList;
	}

	@Override
	public List<Drama> getCurPlayDramaList(Long theatreid, int from, int maxnum){
		String key = CacheConstant.buildKey("get24Cur36Play48DramaList", theatreid, from, maxnum);
		List<Long> idList = (List<Long>) cacheService.get(CacheConstant.REGION_TWENTYMIN, key);
		if(CollectionUtils.isEmpty(idList)){
			Timestamp cur = DateUtil.getCurFullTimestamp();
			String hql = "select new map(d.dramaid as dramaid,(select count(*)  from OpenDramaItem o where o.dramaid = d.dramaid and o.status=? and (o.playtime >? and o.period=? or o.endtime>? and o.period=?)) as sumnum) " +
					" from DramaPlayItem d where d.status=? and d.theatreid=? and (d.playtime>? and d.period=? or d.endtime>? and d.period=? ) group by d.dramaid order by sumnum desc, count(*) desc ";
			List<Map> mapList = queryByRowsRange(hql, from, maxnum, Status.Y, cur, Status.Y, cur, Status.N, Status.Y, theatreid, cur, Status.Y, cur, Status.N);
			idList = BeanUtil.getBeanPropertyList(mapList, Long.class, "dramaid", true);
			cacheService.set(CacheConstant.REGION_TWENTYMIN, key, idList);
		}
		List<Drama> dramaList = baseDao.getObjectList(Drama.class, idList);
		return dramaList;
	}
	@Override
	public Integer getCurPlayDramaCount(Long theatreid){
		DetachedCriteria query = DetachedCriteria.forClass(DramaPlayItem.class);
		query.add(Restrictions.ne("status", DramaPlayItem.STATUS_N));
		query.add(Restrictions.eq("theatreid", theatreid));
		query.add(Restrictions.gt("playtime", new Timestamp(System.currentTimeMillis())));
		query.setProjection(Projections.countDistinct("dramaid"));
		List<Long> idList = hibernateTemplate.findByCriteria(query);
		if(idList.size()>0) return new Integer(idList.get(0)+"");
		return 0;
	}
	@Override
	public List<Drama> getFutureDramaList(String citycode, Date fromDate, int from, int maxnum){
		DetachedCriteria query = DetachedCriteria.forClass(Drama.class);
		if(StringUtils.isNotBlank(citycode))query.add(Restrictions.eq("citycode", citycode));
		query.add(Restrictions.gt("releasedate", fromDate));
		List<Drama> dramaList = hibernateTemplate.findByCriteria(query, from, maxnum);
		return dramaList;
	}
	@Override
	public List<Drama> getCurDramaList(String citycode, String orderField){
		Timestamp curtime = DateUtil.getCurFullTimestamp();
		String query = "select distinct dpi.dramaid from DramaPlayItem dpi where (dpi.playtime >= ? and dpi.period=? or dpi.endtime>=? and dpi.period=?) and dpi.citycode = ? ";
		List<Long> dramaidList = hibernateTemplate.find(query, curtime, Status.Y, curtime, Status.N, citycode);
		List<Drama> dramaList = baseDao.getObjectList(Drama.class, dramaidList);
		Collections.sort(dramaList, new PropertyComparator((orderField), false, false));
		return dramaList;
	}

	@SuppressWarnings("deprecation")
	@Override
	public Map<String/*yyyy-MM-dd*/,Integer> getMonthDramaCountGroupPlaydate(String citycode, Date playdate){
		Timestamp cur = DateUtil.getCurFullTimestamp();
		Timestamp startime = DateUtil.getBeginTimestamp(playdate);
		if(cur.after(startime)) startime = cur;
		Timestamp endtime = DateUtil.getMonthLastDay(startime);
		String key = CacheConstant.buildKey("getDayOfDramaCountByOdixxx", citycode, startime, endtime);
		Map<String,Integer> result = (Map<String, Integer>) cacheService.get(CacheConstant.REGION_HALFHOUR, key);
		if(CollectionUtils.isEmpty(result)){
			DetachedCriteria query = DetachedCriteria.forClass(OpenDramaItem.class, "odi");
			query.add(Restrictions.eq("odi.status", OdiConstant.STATUS_BOOK));
			query.add(Restrictions.eq("odi.partner", Status.Y));
			query.add(Restrictions.eq("odi.citycode", citycode));
			Conjunction con1 = Restrictions.conjunction();
			con1.add(Restrictions.ge("odi.playtime", startime));
			con1.add(Restrictions.le("odi.playtime", endtime));
			con1.add(Restrictions.eq("odi.period", Status.Y));
			Conjunction con2 = Restrictions.conjunction();
			con2.add(Restrictions.ge("odi.playtime", startime));
			con2.add(Restrictions.ge("odi.endtime", cur));
			con2.add(Restrictions.eq("odi.period", Status.N));
			query.add(Restrictions.or(con1, con2));
			ProjectionList projectionList = Projections.projectionList();
			projectionList.add(Projections.sqlGroupProjection("to_char({alias}.playtime,'yyyy-MM-dd') as x","to_char({alias}.playtime,'yyyy-MM-dd')",new String[] { "x" },new Type[]{Hibernate.STRING}),"playdate");
			projectionList.add(Projections.rowCount(),"sumnum");
			query.setProjection(projectionList);
			query.setResultTransformer(DetachedCriteria.ALIAS_TO_ENTITY_MAP);
			List<Map> dramaCountList = hibernateTemplate.findByCriteria(query);
			result = new HashMap<String, Integer>();
			for (Map dataMap : dramaCountList) {
				result.put(String.valueOf(dataMap.get("playdate")+""), Integer.valueOf(dataMap.get("sumnum")+""));
			}
			cacheService.set(CacheConstant.REGION_HALFHOUR, key, result);
		}
		return result;
	}
	
	@Override
	public List<Drama> getCurDramaByDate(Timestamp curtime, String citycode, String order, int from, int maxnum) {
		DetachedCriteria query = DetachedCriteria.forClass(DramaPlayItem.class, "dpi");
		query.add(Restrictions.eq("citycode", citycode));
		final Timestamp cur = DateUtil.getCurFullTimestamp();
		if(curtime.before(cur)){
			curtime = cur;
		}
		Conjunction con1 = Restrictions.conjunction();
		con1.add(Restrictions.ge("dpi.playtime", curtime));
		con1.add(Restrictions.le("dpi.playtime", DateUtil.getLastTimeOfDay(curtime)));
		con1.add(Restrictions.eq("dpi.period", Status.Y));
		Conjunction con2 = Restrictions.conjunction();
		con2.add(Restrictions.ge("dpi.endtime", cur));
		con2.add(Restrictions.eq("dpi.period", Status.N));
		query.add(Restrictions.or(con1, con2));
		query.add(Restrictions.eq("dpi.status", OdiConstant.STATUS_BOOK));
		query.setProjection(Projections.distinct(Projections.property("dpi.dramaid")));
		List<Long> idList = hibernateTemplate.findByCriteria(query);
		List<Drama> dramaList = baseDao.getObjectList(Drama.class, idList);
		Collections.sort(dramaList, new PropertyComparator(order, false, true));
		return BeanUtil.getSubList(dramaList, from, maxnum);
	}
	@Override
	public List<Drama> getDramaList(String citycode, String fyrq, String type, String order, String dramatype, String searchkey,int from,int maxnum){
		DetachedCriteria query = getQuery(citycode,fyrq,type,order,dramatype,searchkey);
		List<Drama> dramaList = hibernateTemplate.findByCriteria(query, from, maxnum);
		return dramaList;
	}
	
	public Integer getDramaListCount(String citycode, String fyrq, String type, String order, String dramatype, String searchkey){
		DetachedCriteria query = getQuery(citycode,fyrq,type,order,dramatype,searchkey);
		return Integer.valueOf(hibernateTemplate.findByCriteria(query.setProjection(Projections.rowCount())).get(0).toString());
	}
	
	private DetachedCriteria getQuery(String citycode, String fyrq, String type, String order, String dramatype, String searchkey){
		if(StringUtils.isBlank(fyrq)) fyrq = "1";
		Timestamp curtime = new Timestamp(System.currentTimeMillis());
		Date curdate = new Date();
		DetachedCriteria query = DetachedCriteria.forClass(Drama.class, "d");
		if(StringUtils.isNotBlank(dramatype)) query.add(Restrictions.eq("d.dramatype", dramatype));
		if(StringUtils.isNotBlank(citycode)) query.add(Restrictions.eq("d.citycode", citycode));
		if(StringUtils.equals("1", fyrq)){//正在售票
			DetachedCriteria sub = DetachedCriteria.forClass(OpenDramaItem.class, "odi");
			sub.add(Restrictions.eq("odi.status", OdiConstant.STATUS_BOOK));
			Conjunction con1 = Restrictions.conjunction();
			con1.add(Restrictions.ge("odi.playtime", curtime));
			con1.add(Restrictions.eq("odi.period", Status.Y));
			Conjunction con2 = Restrictions.conjunction();
			con2.add(Restrictions.ge("odi.endtime", curtime));
			con2.add(Restrictions.eq("odi.period", Status.N));
			sub.add(Restrictions.or(con1, con2));
			sub.add(Restrictions.le("odi.opentime", curtime));
			sub.add(Restrictions.gt("odi.closetime", curtime));
			if(StringUtils.isNotBlank(citycode)){
				sub.add(Restrictions.eq("odi.citycode", citycode));
			}
			sub.add(Restrictions.eqProperty("odi.dramaid", "d.id"));
			sub.setProjection(Projections.property("odi.id"));
			query.add(Subqueries.exists(sub));
		}else if(StringUtils.equals("2", fyrq)){ //正在上映
			DetachedCriteria sub = DetachedCriteria.forClass(DramaPlayItem.class, "item");
			sub.add(Restrictions.lt("item.playtime", curtime));
			sub.add(Restrictions.ne("item.status", DramaPlayItem.STATUS_N));
			if(StringUtils.isNotBlank(citycode)) sub.add(Restrictions.eq("item.citycode", citycode));
			sub.add(Restrictions.eqProperty("item.dramaid", "d.id"));
			sub.setProjection(Projections.property("item.id"));
			query.add(Subqueries.exists(sub));
			query.add(Restrictions.lt("d.releasedate", curdate));
			query.add(Restrictions.gt("d.enddate", curdate));
		}else if(StringUtils.equals("6", fyrq)){
			query.add(Restrictions.gt("d.releasedate", curdate));
			DetachedCriteria sub = DetachedCriteria.forClass(DramaPlayItem.class, "item");
			Conjunction con1 = Restrictions.conjunction();
			con1.add(Restrictions.ge("item.playtime", curtime));
			con1.add(Restrictions.eq("item.period", Status.Y));
			Conjunction con2 = Restrictions.conjunction();
			con2.add(Restrictions.ge("item.endtime", curtime));
			con2.add(Restrictions.eq("item.period", Status.N));
			sub.add(Restrictions.or(con1, con2));
			sub.add(Restrictions.eq("item.status", DramaPlayItem.STATUS_Y));
			if(StringUtils.isNotBlank(citycode)){
				sub.add(Restrictions.eq("item.citycode", citycode));
			}
			sub.add(Restrictions.eqProperty("item.dramaid", "d.id"));
			sub.setProjection(Projections.property("item.id"));
			query.add(Subqueries.notExists(sub));
		}else if(StringUtils.equals("8", fyrq)){ 
			query.add(Restrictions.lt("d.enddate", curdate));
		}else { //其他情况
			if(!StringUtils.equals(fyrq, "7")){
				Map<String, Integer[]> dateMap = new HashMap<String, Integer[]>();
				dateMap.put("3", new Integer[]{0 ,7});
				dateMap.put("4", new Integer[]{0 ,30});
				dateMap.put("5", new Integer[]{0 ,90});
				query.add(Restrictions.ge("d.releasedate", curdate));
				query.add(Restrictions.le("d.enddate", DateUtil.addDay(curdate, dateMap.get(fyrq)[1])));
			}
		}
		if(StringUtils.isNotBlank(searchkey)){
			DetachedCriteria sub = DetachedCriteria.forClass(DramaToStar.class, "t");
			sub.add(Restrictions.eqProperty("t.dramaid", "d.id"));
			sub.setProjection(Projections.property("t.dramaid"));
			DetachedCriteria subQuery = DetachedCriteria.forClass(DramaStar.class, "s");
			subQuery.add(Restrictions.ilike("s.name", StringUtils.trim(searchkey), MatchMode.ANYWHERE));
			subQuery.add(Restrictions.eqProperty("s.id", "t.starid"));
			subQuery.setProjection(Projections.property("s.id"));
			sub.add(Subqueries.exists(subQuery));
			query.add(Restrictions.or(Restrictions.ilike("d.dramaname", StringUtils.trim(searchkey), MatchMode.ANYWHERE), Subqueries.exists(sub)));
		}
		if(StringUtils.isNotBlank(type)) query.add(Restrictions.like("type", type, MatchMode.ANYWHERE));
		if(StringUtils.isNotBlank(order) &&("avggeneral".equals(order) || "releasedate".equals(order) || "clickedtimes".equals(order) || "boughtcount".equals(order) /*|| "diarycount".equals(order)*/)){
			if(StringUtils.equals(order, "releasedate")){
				query.addOrder(NullPropertyOrder.desc(order));
			}else{
				query.addOrder(NullPropertyOrder.desc(order));
			}
		}else {
			if(StringUtils.equals("8", fyrq) || StringUtils.equals("7", fyrq)){
				query.addOrder(NullPropertyOrder.desc("d.releasedate"));
			}else{
				query.addOrder(NullPropertyOrder.asc("d.releasedate"));
				query.addOrder(NullPropertyOrder.desc("d.hotvalue"));
			}
		}
		return query;
	}
	
	@Override
	public List<Drama> getDramaListByTroupeCompany(String citycode, Timestamp lasttime, String troupecompany){
		DetachedCriteria query = DetachedCriteria.forClass(Drama.class);
		query.add(Restrictions.eq("citycode", citycode));
		query.add(Restrictions.gt("updatetime", lasttime));
		query.add(Restrictions.like("troupecompany", troupecompany, MatchMode.ANYWHERE));
		List<Drama> dramaList = hibernateTemplate.findByCriteria(query);
		return dramaList;
	}
	
	@Override
	public List<String> getDramaTypeList(String citycode){
		String key = CacheConstant.buildKey("get12313DramaType234511List", citycode);
		List<String> dramaTypeList = (List<String>) cacheService.get(CacheConstant.REGION_TWOHOUR, key);
		if(dramaTypeList == null){
			String qry = "select distinct dramatype from Drama where citycode = ?";
			dramaTypeList = hibernateTemplate.find(qry, citycode);
			cacheService.set(CacheConstant.REGION_TWOHOUR, key, dramaTypeList);
		}
		return dramaTypeList;
	}
	
	@SuppressWarnings("unchecked")
	public List<Drama> getDramaListByMonthOpenDramaItem(String citycode, boolean isPartner, int maxnum){
		Timestamp cur = DateUtil.getCurFullTimestamp();
		DetachedCriteria query = DetachedCriteria.forClass(Drama.class,"d");
		if(StringUtils.isNotBlank(citycode))query.add(Restrictions.eq("citycode", citycode));
		DetachedCriteria sub = DetachedCriteria.forClass(OpenDramaItem.class, "odi");
		sub.add(Restrictions.le("odi.opentime", cur));
		sub.add(Restrictions.gt("odi.closetime",  cur));
		if(StringUtils.isNotBlank(citycode)) sub.add(Restrictions.eq("odi.citycode", citycode));
		if(isPartner){
			sub.add(Restrictions.eq("odi.partner", Status.Y));
		}
		sub.add(Restrictions.eq("odi.status", OdiConstant.STATUS_BOOK));
		sub.add(Restrictions.eqProperty("odi.dramaid", "d.id"));
		sub.setProjection(Projections.property("odi.id"));
		query.add(Subqueries.exists(sub));
		query.addOrder(Order.desc("clickedtimes"));
		List<Drama> idList = hibernateTemplate.findByCriteria(query, 0, maxnum);
		return idList;
	}
	
	public List<Drama> getDramaListLastOpenTime(String citycode, int from, int maxnum){
		String key = CacheConstant.buildKey("getDramaListLastOpenTime1223", citycode);
		List<Long> idList = (List<Long>) cacheService.get(CacheConstant.REGION_TENMIN, key);
		if(idList ==  null || idList.isEmpty()){
			Timestamp cur = DateUtil.getCurFullTimestamp();
			DetachedCriteria query = DetachedCriteria.forClass(OpenDramaItem.class,"odi");
			Conjunction con1 = Restrictions.conjunction();
			con1.add(Restrictions.ge("odi.playtime", cur));
			con1.add(Restrictions.eq("odi.period", Status.Y));
			Conjunction con2 = Restrictions.conjunction();
			con2.add(Restrictions.ge("odi.endtime", cur));
			con2.add(Restrictions.eq("odi.period", Status.N));
			query.add(Restrictions.or(con1, con2));
			query.add(Restrictions.le("odi.opentime", cur));
			query.add(Restrictions.gt("odi.closetime", cur));
			query.add(Restrictions.eq("odi.citycode", citycode));
			query.add(Restrictions.eq("odi.status", OdiConstant.STATUS_BOOK));
			query.setProjection(Projections.projectionList().add(Projections.groupProperty("odi.dramaid"), "dramaid").add(Projections.alias(Projections.min("odi.opentime"), "opentime_ali" )));
			query.setResultTransformer(DetachedCriteria.ALIAS_TO_ENTITY_MAP);
			query.addOrder(Order.desc("opentime_ali"));
			List<Map> mapList = hibernateTemplate.findByCriteria(query);
			idList = new ArrayList<Long>();
			for(Map dataMap :mapList){
				idList.add(Long.parseLong(dataMap.get("dramaid")+""));
			}
			cacheService.set(CacheConstant.REGION_TENMIN, key, idList);
		}
		idList = BeanUtil.getSubList(idList, from, maxnum);
		List<Drama> dramaList = baseDao.getObjectList(Drama.class, idList);
		return dramaList;
	}
	@Override
	public Date getDramaMinMonthDate(String citycode){
		String key = CacheConstant.buildKey("getDramaMinMonthDate", citycode);
		Date minMonthDate = (Date) cacheService.get(CacheConstant.REGION_HALFHOUR, key);
		Date curDate = DateUtil.getCurDate();
		Date curMonthDate = DateUtil.getMonthFirstDay(curDate);
		if(minMonthDate == null || curMonthDate.after(minMonthDate)){
			DetachedCriteria query = DetachedCriteria.forClass(Drama.class, "d");
			query.add(Restrictions.eq("d.citycode", citycode));
			query.add(Restrictions.ge("d.enddate", curDate));
			query.setProjection(Projections.min("d.releasedate"));
			List<Date> dateList = hibernateTemplate.findByCriteria(query, 0, 1);
			if(dateList.isEmpty()) minMonthDate = DateUtil.getMonthFirstDay(curDate);
			else minMonthDate = DateUtil.getMonthFirstDay(dateList.get(0));
			cacheService.set(CacheConstant.REGION_HALFHOUR, key, minMonthDate);
		}
		return minMonthDate;
	}
	
	@Override
	public Map<String, Integer> getMonthDramaCount(String citycode, Date playdate){
		playdate = DateUtil.getMonthFirstDay(playdate);
		Date curDate = DateUtil.getCurDate(), startdate = curDate;
		if(curDate.after(playdate)) startdate = curDate;
		else startdate = playdate;
		Date enddate = DateUtil.getNextMonthFirstDay(startdate);
		String key = CacheConstant.buildKey("getMonthDramaCount24", citycode, startdate, enddate);
		Map<String,Integer> result = (Map<String, Integer>) cacheService.get(CacheConstant.REGION_HALFHOUR, key);
		if(CollectionUtils.isEmpty(result)){
			DetachedCriteria query = DetachedCriteria.forClass(Drama.class, "d");
			query.add(Restrictions.eq("d.citycode", citycode));
			query.add(Restrictions.ge("d.enddate", curDate));
			List<Drama> dramaList = hibernateTemplate.findByCriteria(query);
			result = new HashMap<String, Integer>();
			while (startdate.before(enddate)) {
				String date = DateUtil.formatDate(startdate);
				Integer count = result.get(date);
				count = (count == null ? 0 : count);
				for (Drama drama : dramaList) {
					if(startdate.after(drama.getReleasedate()) && startdate.before(drama.getEnddate())){
						count ++;
					}
				}
				result.put(date, count);
				startdate = DateUtil.addDay(startdate, 1);
			}
			cacheService.set(CacheConstant.REGION_HALFHOUR, key, result);
		}
		return result;
	}
	@Override
	public List<Drama> getCurDramaByDate(String citycode, Date playdate, String order, int from, int maxnum) {
		playdate = DateUtil.getBeginningTimeOfDay(playdate);
		DetachedCriteria query = DetachedCriteria.forClass(Drama.class, "d");
		query.add(Restrictions.eq("citycode", citycode));
		query.add(Restrictions.le("d.releasedate", playdate));
		query.add(Restrictions.ge("d.enddate", playdate));
		List<Drama> dramaList = hibernateTemplate.findByCriteria(query);
		Collections.sort(dramaList, new PropertyComparator(order, false, true));
		return BeanUtil.getSubList(dramaList, from, maxnum);
	}
	
	@Override
	public List<Drama> getDramaListByName(String citycode, String searchKey, Timestamp fromDate, String orderField, boolean asc, int from, int maxnum) {
		DetachedCriteria query = getQueryToName(citycode,searchKey,fromDate);
		if(StringUtils.isNotBlank(orderField) && ClassUtils.hasMethod(Drama.class, "get" + StringUtils.capitalize(orderField))){
			if(asc){
				query.addOrder(Order.asc(orderField));
			}else{
				query.addOrder(Order.desc(orderField));
			}
		}else{
			if(asc){
				query.addOrder(Order.asc("d.avggeneral"));
			}else{
				query.addOrder(Order.desc("d.avggeneral"));
			}
		}
		query.addOrder(Order.desc("d.id"));
		return hibernateTemplate.findByCriteria(query, from, maxnum);
	}

	@Override
	public Integer getDramaCountByName(String citycode, String searchKey, Timestamp fromDate) {
		DetachedCriteria query = getQueryToName(citycode,searchKey,fromDate);
		query.setProjection(Projections.rowCount());
		return Integer.valueOf(hibernateTemplate.findByCriteria(query).get(0).toString());
	}
	private DetachedCriteria getQueryToName(String citycode, String searchKey, Date fromDate){
		DetachedCriteria query = DetachedCriteria.forClass(Drama.class,"d");
		if(StringUtils.isNotBlank(citycode))query.add(Restrictions.eq("d.citycode",citycode));
		if(StringUtils.isNotBlank(searchKey))query.add(Restrictions.like("d.dramaname", searchKey, MatchMode.ANYWHERE));
		if(fromDate!=null)query.add(Restrictions.ge("d.releasedate",fromDate));
		return query;
	}
	
	@SuppressWarnings("deprecation")
	@Override
	public List<OpenDramaItem> getBookingOdiList(String citycode, Date playdate, Long dramaid, Long theatreid){
		String key = CacheConstant.buildKey("qryOpenDramaItemxxxx", citycode, playdate, dramaid, theatreid);
		List<OpenDramaItem> odiList = (List<OpenDramaItem>) cacheService.get(CacheConstant.REGION_TENMIN, key);
		if(CollectionUtils.isEmpty(odiList)){
			Timestamp curtime = DateUtil.getCurFullTimestamp();
			DetachedCriteria query = DetachedCriteria.forClass(OpenDramaItem.class, "odi");
			if(dramaid!=null){
				query.add(Restrictions.eq("odi.dramaid", dramaid));
			}
			if(theatreid!=null){
				query.add(Restrictions.eq("odi.theatreid", theatreid));
			}
			query.add(Restrictions.eq("odi.citycode", citycode));
			query.add(Restrictions.eq("odi.status", OdiConstant.STATUS_BOOK));
			query.add(Restrictions.le("odi.opentime", curtime));
			query.add(Restrictions.gt("odi.closetime", curtime));
			if(playdate!=null) {
				query.add(Restrictions.sqlRestriction("to_char(playtime,'yyyy-MM-dd')=?", new Object[]{DateUtil.formatDate(playdate)}, new Type[]{Hibernate.STRING}));
			}
			
			Conjunction con1 = Restrictions.conjunction();
			con1.add(Restrictions.ge("odi.playtime", curtime));
			con1.add(Restrictions.eq("odi.period", Status.Y));
			Conjunction con2 = Restrictions.conjunction();
			con2.add(Restrictions.ge("odi.endtime", curtime));
			con2.add(Restrictions.eq("odi.period", Status.N));
			
			query.add(Restrictions.or(con1, con2));
			query.addOrder(Order.asc("playtime"));
			odiList = hibernateTemplate.findByCriteria(query);
			cacheService.set(CacheConstant.REGION_TENMIN, key, odiList);
		}
		return odiList;
	}
}