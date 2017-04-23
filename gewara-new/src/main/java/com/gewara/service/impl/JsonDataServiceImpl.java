package com.gewara.service.impl;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Service;

import com.gewara.model.BaseObject;
import com.gewara.model.common.JsonData;
import com.gewara.service.JsonDataService;
import com.gewara.util.DateUtil;
import com.gewara.util.JsonUtils;
import com.gewara.util.VmUtils;
@Service("jsonDataService")
public class JsonDataServiceImpl extends BaseServiceImpl implements JsonDataService {

	@Override
	public void saveJsonData(String dkey, String tag, Map<String, String> dataMap) {
		try {
			JsonData json = baseDao.getObject(JsonData.class, dkey);
			if(json==null) json = new JsonData(dkey);
			String data = JsonUtils.writeMapToJson(dataMap);
			json.setData(data);
			json.setTag(tag);
			baseDao.saveObject(json);
		} catch (Exception e) {
			dbLogger.error("", e);
		}
	}
	@Override
	public Map<String, String> getJsonData(String dkey){
		Map<String, String> map = new HashMap<String, String>();
		JsonData json = baseDao.getObject(JsonData.class, dkey);
		if(json!=null) {
			map = VmUtils.readJsonToMap(json.getData());
		}
		return map;
	}
	
	@Override
	public List<JsonData> getListByTag(String tag, Timestamp validtime, int from, int maxnum){
		return getListByTag(tag, validtime, "dkey", true, from, maxnum);
	}
	@Override
	public List<JsonData> getListByTag(String tag, Timestamp validtime, String orderProperty, boolean isdesc, int from, int maxnum){
		DetachedCriteria query = DetachedCriteria.forClass(JsonData.class);
		query.add(Restrictions.like("tag", tag, MatchMode.START));
		if(validtime != null) query.add(Restrictions.gt("validtime", validtime));
		if(isdesc){
			query.addOrder(Order.desc(orderProperty));
		}else{
			query.addOrder(Order.asc(orderProperty));
		}
		List<JsonData> result = hibernateTemplate.findByCriteria(query, from, maxnum);
		return result;
	}
	@Override
	public Integer countListByTag(String tag, Timestamp validtime){
		DetachedCriteria query = DetachedCriteria.forClass(JsonData.class);
		query.setProjection(Projections.rowCount());
		query.add(Restrictions.like("tag", tag, MatchMode.START));
		if(validtime != null) query.add(Restrictions.gt("validtime", validtime));
		List<JsonData> result = hibernateTemplate.findByCriteria(query);
		if(result.isEmpty()) return 0;
		return Integer.parseInt("" + result.get(0));
	}
	@Override
	public List<JsonData> getJsonDataListByDkey(String pre_dKey, String tag){
		DetachedCriteria query = DetachedCriteria.forClass(JsonData.class);
		query.add(Restrictions.like("dkey", pre_dKey, MatchMode.START));
		if(StringUtils.isNotBlank(tag)) query.add(Restrictions.eq("tag", tag));
		List<JsonData> dataList=hibernateTemplate.findByCriteria(query);
		return dataList;
	}
	@Override
	public <T  extends BaseObject> void addRemoveObject(Class<T> clazz, Serializable id) {
		String key = "remove_" + clazz.getSimpleName() + DateUtil.format(new Date(), "yyyyMMddHHmm");
		JsonData jsonData = baseDao.getObject(JsonData.class, key);
		if(jsonData==null){
			jsonData = new JsonData(key, ""+id);
			jsonData.setTag("remove");
			jsonData.setValidtime(DateUtil.addDay(new Timestamp(System.currentTimeMillis()), 30));
		}else{
			jsonData.setData(jsonData.getData() + "," + id);
		}
		baseDao.saveObject(jsonData);
	}
}
