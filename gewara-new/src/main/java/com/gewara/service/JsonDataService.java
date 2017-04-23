package com.gewara.service;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.List;
import java.util.Map;

import com.gewara.model.BaseObject;
import com.gewara.model.common.JsonData;

public interface JsonDataService{
	void saveJsonData(String dkey, String tag, Map<String, String> dataMap);
	Map<String, String> getJsonData(String dkey);
	
	/**
	 *    @function 根据TAG 查询
	 * 	@author bob.hu
	 *		@date	2011-04-28 14:04:43
	 */
	List<JsonData> getListByTag(String tag, Timestamp validtime, String orderProperty, boolean isdesc, int from, int maxnum);
	List<JsonData> getListByTag(String tag, Timestamp validtime, int from, int maxnum);
	Integer countListByTag(String tag, Timestamp validtime);
	/**
	 * 根据dkey 前缀 + tag 查询列表
	 * @param dkey
	 * @return
	 */
	List<JsonData> getJsonDataListByDkey(String pre_dKey, String tag);
	<T  extends BaseObject> void addRemoveObject(Class<T> clazz, Serializable id);
}
