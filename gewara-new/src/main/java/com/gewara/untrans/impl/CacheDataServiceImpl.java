package com.gewara.untrans.impl;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.gewara.constant.sys.CacheConstant;
import com.gewara.constant.sys.ConfigConstant;
import com.gewara.model.BaseObject;
import com.gewara.model.common.GewaConfig;
import com.gewara.service.DaoService;
import com.gewara.untrans.CacheDataService;
import com.gewara.untrans.CacheService;
import com.gewara.util.DateUtil;
import com.gewara.util.JsonUtils;
@Service("cacheDataService")
public class CacheDataServiceImpl implements CacheDataService{
	@Autowired@Qualifier("cacheService")
	private CacheService cacheService;
	public void setCacheService(CacheService cacheService) {
		this.cacheService = cacheService;
	}
	@Autowired@Qualifier("daoService")
	private DaoService daoService;
	public void setDaoService(DaoService daoService) {
		this.daoService = daoService;
	}
	
	@Override
	public Timestamp getHistoryUpdateTime(String key){
		Timestamp addTime = DateUtil.parseTimestamp(""+cacheService.get(CacheConstant.REGION_ONEDAY, key));
		if(addTime == null){
			GewaConfig gewaConfig = daoService.getObject(GewaConfig.class, ConfigConstant.CFG_HISTORY_UPDATE);
			Map map = JsonUtils.readJsonToMap(gewaConfig.getContent());
			if(!map.containsKey(key)) throw new IllegalArgumentException("getTableUpdateTime() key error!");
			String updatetime = ""+map.get(key);
			cacheService.set(CacheConstant.REGION_ONEDAY, key, updatetime);
			addTime = DateUtil.parseTimestamp(updatetime);
		}
		return addTime;
	}
	@Override
	public <T extends BaseObject> String getAndSetIdsFromCachePool(Class<T> clazz, Serializable id){
		String key = clazz.getCanonicalName();
		String ids = (String) cacheService.get(CacheConstant.REGION_ONEDAY, key);
		if(StringUtils.isBlank(ids)) ids = ",";
		if(id == null) return ids;
		
		String tmpid = id + ",";
		if(!ids.contains(","+tmpid)) ids += tmpid;
		cacheService.set(CacheConstant.REGION_ONEDAY, key, ids);
		return ids;
	}
	@Override
	public <T extends BaseObject> void cleanIdsFromCachePool(Class<T> clazz){
		cacheService.set(CacheConstant.REGION_TWOHOUR, clazz.getCanonicalName(), "");
	}
	
	@Override
	public <T extends BaseObject> Integer getAndSetClazzKeyCount(Class<T> clazz, Serializable id){
		String key = clazz.getCanonicalName() + "." + id;
		Integer count = (Integer)cacheService.get(CacheConstant.REGION_TWOHOUR, key);
		if(count == null) count = 0;
		count++;
		cacheService.set(CacheConstant.REGION_TWOHOUR, key, count);
		return count;
	}
	@Override
	public <T extends BaseObject> void cleanClazzKeyCount(Class<T> clazz, Serializable id){
		String key = clazz.getCanonicalName() + "." + id;
		cacheService.set(CacheConstant.REGION_TWOHOUR, key, 0);
	}
}
