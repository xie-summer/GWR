package com.gewara.service.order.impl;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.hibernate.criterion.Conjunction;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.criterion.Subqueries;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.gewara.constant.Status;
import com.gewara.constant.sys.CacheConstant;
import com.gewara.model.common.BaseEntity;
import com.gewara.model.goods.BaseGoods;
import com.gewara.service.order.GoodsOtherService;
import com.gewara.untrans.CacheService;
import com.gewara.util.DateUtil;

@Service("goodsOtherService")
public class GoodsOtherServiceImpl extends GoodsServiceImpl implements GoodsOtherService {
	
	@Autowired@Qualifier("cacheService")
	private CacheService cacheService;

	@Override
	public <T extends BaseGoods> List<Long> getRelatedidList(Class<T> clazz, String citycode, String tag, String itemtype, Long itemid, boolean isBooking, boolean isGtZero, boolean cache) {
		String key = null;
		List<Long> idList = null;
		if(cache){
			key = CacheConstant.buildKey("getGoods02RelatedidList", clazz.getSimpleName(), citycode, tag, itemtype, itemid, isBooking, isGtZero);
			idList = (List<Long>) cacheService.get(CacheConstant.REGION_TWENTYMIN, key);
		}
		if(idList == null){
			Timestamp cur = DateUtil.getCurFullTimestamp();
			DetachedCriteria query = DetachedCriteria.forClass(clazz);
			if(StringUtils.isNotBlank(citycode)){
				query.add(Restrictions.eq("citycode", citycode));
			}
			query.add(Restrictions.eq("tag", tag));
			if(StringUtils.isNotBlank(itemtype)){
				query.add(Restrictions.eq("itemtype", itemtype));
			}
			if(itemid != null){
				query.add(Restrictions.eq("itemid", itemid));
			}
			Conjunction con1 = Restrictions.conjunction();
			Conjunction con2 = Restrictions.conjunction();
			con1.add(Restrictions.eq("period", Status.Y));
			con1.add(Restrictions.ge("fromvalidtime", cur));
			con2.add(Restrictions.eq("period", Status.N));
			con2.add(Restrictions.ge("tovalidtime", cur));
			query.add(Restrictions.or(con1, con2));
			if(isBooking){
				query.add(Restrictions.le("fromtime", cur));
				query.add(Restrictions.ge("totime", cur));
				query.add(Restrictions.eq("status", Status.Y));
			}else{
				query.add(Restrictions.ne("status", Status.DEL));
			}
			if(isGtZero) query.add(Restrictions.gt("goodssort", 0));
			query.setProjection(Projections.distinct(Projections.property("relatedid")));
			idList = hibernateTemplate.findByCriteria(query);
			if(cache) cacheService.set(CacheConstant.REGION_TWENTYMIN, key, idList);
		}
		return idList;
	}
	
	@Override
	public <T extends BaseGoods> List<Long> getItemidList(Class<T> clazz, String citycode, String tag, Long relatedid, String itemtype, boolean isBooking, boolean isGtZero, boolean cache) {
		String key = null;
		List<Long> idList = null;
		if(cache){
			key = CacheConstant.buildKey("get23234ItemidList", clazz.getSimpleName(), citycode, tag, relatedid, itemtype, isBooking, isGtZero);
			idList = (List<Long>) cacheService.get(CacheConstant.REGION_TWENTYMIN, key);
		}
		if(idList == null){
			Timestamp cur = DateUtil.getCurFullTimestamp();
			DetachedCriteria query = DetachedCriteria.forClass(clazz);
			if(StringUtils.isNotBlank(citycode)){
				query.add(Restrictions.eq("citycode", citycode));
			}
			if(StringUtils.isNotBlank(tag)){
				query.add(Restrictions.eq("tag", tag));
			}
			if(relatedid != null){
				query.add(Restrictions.eq("relatedid", relatedid));
			}
			query.add(Restrictions.eq("itemtype", itemtype));
			Conjunction con1 = Restrictions.conjunction();
			Conjunction con2 = Restrictions.conjunction();
			con1.add(Restrictions.eq("period", Status.Y));
			con1.add(Restrictions.ge("fromvalidtime", cur));
			con2.add(Restrictions.eq("period", Status.N));
			con2.add(Restrictions.ge("tovalidtime", cur));
			query.add(Restrictions.or(con1, con2));
			if(isBooking){
				query.add(Restrictions.le("fromtime", cur));
				query.add(Restrictions.ge("totime", cur));
				query.add(Restrictions.eq("status", Status.Y));
			}else{
				query.add(Restrictions.ne("status", Status.DEL));
			}
			if(isGtZero) query.add(Restrictions.gt("goodssort", 0));
			query.setProjection(Projections.distinct(Projections.property("itemid")));
			idList = hibernateTemplate.findByCriteria(query);
			if(cache) cacheService.set(CacheConstant.REGION_TWENTYMIN, key, idList);
		}
		return idList;
	}
	
	@Override
	public <T extends BaseGoods, S extends BaseEntity> List<Long> getRelatedidList(Class<T> clazz, String citycode, boolean isBooking, boolean isGtZero, Class<S> subClazz, String[] properties, Object[] values, boolean cache){
		String key = null;
		List<Long> idList = null;
		if(cache){
			List params = new ArrayList();
			params.add(clazz.getSimpleName());
			params.add(citycode);
			params.add(isBooking);
			params.add(isGtZero);
			params.add(subClazz.getSimpleName());
			if(!ArrayUtils.isEmpty(properties)){
				params.addAll(Arrays.asList(properties));
			}
			if(!ArrayUtils.isEmpty(values)){
				params.addAll(Arrays.asList(values));
			}
			key = CacheConstant.buildKey("get3843RelatedidList", params.toArray());
			idList = (List<Long>) cacheService.get(CacheConstant.REGION_HALFHOUR, key);
		}
		if(idList == null) {
			Timestamp cur = DateUtil.getCurFullTimestamp();
			DetachedCriteria query = DetachedCriteria.forClass(clazz, "i");
			if(StringUtils.isNotBlank(citycode)){
				query.add(Restrictions.eq("i.citycode", citycode));
			}
			DetachedCriteria subQuery = DetachedCriteria.forClass(subClazz, "t");
			subQuery.add(Restrictions.eqProperty("i.relatedid", "t.id"));
			int length = Math.min(ArrayUtils.getLength(properties), ArrayUtils.getLength(values));
			for (int i = 0; i< length; i++) {
				String tmpKey = properties[i];
				Object tmpValue = values[i];
				if(StringUtils.isNotBlank(tmpKey) && tmpValue != null){
					subQuery.add(Restrictions.eq("t." + tmpKey, tmpValue));
				}
			}
			subQuery.setProjection(Projections.property("t.id"));
			query.add(Subqueries.exists(subQuery));
			Conjunction con1 = Restrictions.conjunction();
			Conjunction con2 = Restrictions.conjunction();
			con1.add(Restrictions.eq("i.period", Status.Y));
			con1.add(Restrictions.ge("i.fromvalidtime", cur));
			con2.add(Restrictions.eq("i.period", Status.N));
			con2.add(Restrictions.ge("i.tovalidtime", cur));
			query.add(Restrictions.or(con1, con2));
			if(isBooking){
				query.add(Restrictions.le("i.fromtime", cur));
				query.add(Restrictions.ge("i.totime", cur));
				query.add(Restrictions.eq("i.status", Status.Y));
			}else{
				query.add(Restrictions.ne("i.status", Status.DEL));
			}
			if(isGtZero) query.add(Restrictions.gt("i.goodssort", 0));
			query.setProjection(Projections.distinct(Projections.property("i.relatedid")));
			idList = hibernateTemplate.findByCriteria(query);
			if(cache) cacheService.set(CacheConstant.REGION_HALFHOUR, key, idList);
		}
		return idList;
	}
	
	@Override
	public <T extends BaseGoods,S extends BaseEntity> List<Long> getItemidList(Class<T> clazz, String citycode, boolean isBooking, boolean isGtZero, Class<S> subClazz, String[] properties, Object[] values, boolean cache){
		String key = null;
		List<Long> idList = null;
		if(cache){
			List params = new ArrayList();
			params.add(clazz.getSimpleName());
			params.add(citycode);
			params.add(isBooking);
			params.add(isGtZero);
			params.add(subClazz.getSimpleName());
			if(!ArrayUtils.isEmpty(properties)){
				params.addAll(Arrays.asList(properties));
			}
			if(!ArrayUtils.isEmpty(values)){
				params.addAll(Arrays.asList(values));
			}
			key = CacheConstant.buildKey("get3843RelatedidList", params.toArray());
			idList = (List<Long>) cacheService.get(CacheConstant.REGION_HALFHOUR, key);
		}
		if(idList == null) {
			Timestamp cur = DateUtil.getCurFullTimestamp();
			DetachedCriteria query = DetachedCriteria.forClass(clazz, "i");
			if(StringUtils.isNotBlank(citycode)){
				query.add(Restrictions.eq("i.citycode", citycode));
			}
			DetachedCriteria subQuery = DetachedCriteria.forClass(subClazz, "t");
			subQuery.add(Restrictions.eqProperty("i.itemid", "t.id"));
			int length = Math.min(ArrayUtils.getLength(properties), ArrayUtils.getLength(values));
			for (int i = 0; i< length; i++) {
				String tmpKey = properties[i];
				Object tmpValue = values[i];
				if(StringUtils.isNotBlank(tmpKey) && tmpValue != null){
					subQuery.add(Restrictions.eq("t." + tmpKey, tmpValue));
				}
			}
			subQuery.setProjection(Projections.property("t.id"));
			query.add(Subqueries.exists(subQuery));
			Conjunction con1 = Restrictions.conjunction();
			Conjunction con2 = Restrictions.conjunction();
			con1.add(Restrictions.eq("i.period", Status.Y));
			con1.add(Restrictions.ge("i.fromvalidtime", cur));
			con2.add(Restrictions.eq("i.period", Status.N));
			con2.add(Restrictions.ge("i.tovalidtime", cur));
			query.add(Restrictions.or(con1, con2));
			if(isBooking){
				query.add(Restrictions.le("i.fromtime", cur));
				query.add(Restrictions.ge("i.totime", cur));
				query.add(Restrictions.eq("i.status", Status.Y));
			}else{
				query.add(Restrictions.ne("i.status", Status.DEL));
			}
			if(isGtZero) query.add(Restrictions.gt("goodssort", 0));
			query.setProjection(Projections.distinct(Projections.property("i.itemid")));
			idList = hibernateTemplate.findByCriteria(query);
			if(cache) cacheService.set(CacheConstant.REGION_HALFHOUR, key, idList);
		}
		return idList;
	}

	@Override
	public <T extends BaseGoods> Map<Long, Integer> getRelatedidByItemdCount(Class<T> clazz, String citycode, String tag, String itemtype, Timestamp starttime, Timestamp endtime, boolean isBooking, boolean isGtZero, boolean cache) {
		String key = null;
		Map<Long, Integer> rowMap = null;
		Timestamp cur = DateUtil.getCurFullTimestamp();
		if(starttime == null || endtime == null){
			starttime = DateUtil.getBeginningTimeOfDay(cur);
			endtime = DateUtil.getLastTimeOfDay(cur);
		}
		if(cache){
			key = CacheConstant.buildKey("get231RelatedidByItemdCount", clazz.getSimpleName(), citycode, tag, itemtype, starttime, endtime, isBooking, isGtZero);
			rowMap = (Map<Long, Integer>) cacheService.get(CacheConstant.REGION_HALFDAY, key);
		}
		if(rowMap == null){
			DetachedCriteria query = DetachedCriteria.forClass(clazz);
			if(StringUtils.isNotBlank(citycode)){
				query.add(Restrictions.eq("citycode", citycode));
			}
			query.add(Restrictions.eq("tag", tag));
			query.add(Restrictions.eq("itemtype", itemtype));
			Conjunction con1 = Restrictions.conjunction();
			Conjunction con2 = Restrictions.conjunction();
			con1.add(Restrictions.ge("fromvalidtime", starttime));
			con1.add(Restrictions.le("fromvalidtime", endtime));
			con1.add(Restrictions.eq("period", Status.Y));
			con2.add(Restrictions.ge("tovalidtime", starttime));
			con2.add(Restrictions.le("tovalidtime", endtime));
			con2.add(Restrictions.eq("period", Status.Y));
			query.add(Restrictions.or(con1, con2));
			if(isBooking){
				query.add(Restrictions.le("fromtime", cur));
				query.add(Restrictions.ge("totime", cur));
				query.add(Restrictions.eq("status", Status.Y));
			}else{
				query.add(Restrictions.ne("status", Status.DEL));
			}
			if(isGtZero) query.add(Restrictions.gt("goodssort", 0));
			query.setProjection(Projections.projectionList()
					.add(Projections.groupProperty("relatedid"), "relatedid")
					.add(Projections.countDistinct("itemid"),"num"));
			query.setResultTransformer(DetachedCriteria.ALIAS_TO_ENTITY_MAP);
			query.addOrder(Order.desc("num"));
			rowMap = new Hashtable<Long, Integer>();
			List<Map> mapList = hibernateTemplate.findByCriteria(query);
			for (Map tmpMap : mapList) {
				rowMap.put((Long)tmpMap.get("relatedid"), Integer.parseInt(tmpMap.get("num")+ ""));
			}
			if(cache) cacheService.set(CacheConstant.REGION_HALFDAY, key, rowMap);
		}
		return rowMap;
	}
	
	@Override
	public <T extends BaseGoods> Integer getGoodsCount(Class<T> clazz, String citycode, String tag, Long relatedid, String itemtype, Long itemid, Timestamp starttime, Timestamp endtime, boolean isBooking, boolean isGtZero, boolean cache){
		String key = null;
		Integer result = null;
		if(cache){
			key = CacheConstant.buildKey("getGoods2351Count", clazz.getSimpleName(), citycode, tag, relatedid, itemtype, itemid, starttime, endtime, isBooking, isGtZero);
			result = (Integer) cacheService.get(CacheConstant.REGION_TWENTYMIN, key);
		}
		if(result == null){
			DetachedCriteria query = DetachedCriteria.forClass(clazz);
			if(StringUtils.isNotBlank(citycode)){
				query.add(Restrictions.eq("citycode", citycode));
			}
			query.add(Restrictions.eq("tag", tag));
			if(relatedid != null){
				query.add(Restrictions.eq("relatedid", relatedid));
			}
			query.add(Restrictions.eq("itemtype", itemtype));
			if(itemid != null){
				query.add(Restrictions.eq("itemid", itemid));
			}
			Conjunction con1 = Restrictions.conjunction();
			Conjunction con2 = Restrictions.conjunction();
			con1.add(Restrictions.eq("period", Status.Y));
			con1.add(Restrictions.ge("fromvalidtime", starttime));
			if(endtime != null){
				con1.add(Restrictions.le("fromvalidtime", endtime));
			}
			con2.add(Restrictions.eq("period", Status.N));
			con2.add(Restrictions.ge("tovalidtime", starttime));
			if(endtime != null){
				con2.add(Restrictions.le("tovalidtime", endtime));
			}
			query.add(Restrictions.or(con1, con2));
			if(isBooking){
				Timestamp cur = DateUtil.getCurFullTimestamp();
				query.add(Restrictions.le("fromtime", cur));
				query.add(Restrictions.ge("totime", cur));
				query.add(Restrictions.eq("status", Status.Y));
			}else{
				query.add(Restrictions.ne("status", Status.DEL));
			}
			if(isGtZero) query.add(Restrictions.gt("goodssort", 0));
			query.setProjection(Projections.rowCount());
			List<Long> resultList = hibernateTemplate.findByCriteria(query);
			if(resultList.isEmpty()) result = 0;
			else result = Integer.valueOf(resultList.get(0) + "");
			if(cache) cacheService.set(CacheConstant.REGION_TWENTYMIN, key, result);
		}
		return result;
	}
}
