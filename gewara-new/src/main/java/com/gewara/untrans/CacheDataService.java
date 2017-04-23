package com.gewara.untrans;

import java.io.Serializable;
import java.sql.Timestamp;

import com.gewara.model.BaseObject;

public interface CacheDataService {
	<T extends BaseObject> String getAndSetIdsFromCachePool(Class<T> clazz, Serializable id);
	<T extends BaseObject> Integer getAndSetClazzKeyCount(Class<T> clazz, Serializable id);
	Timestamp getHistoryUpdateTime(String keyPointupdate);
	<T extends BaseObject> void cleanIdsFromCachePool(Class<T> clazz);
	<T extends BaseObject> void cleanClazzKeyCount(Class<T> clazz, Serializable id);

}
