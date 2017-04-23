/**  
 * @Project: shanghai
 * @Title: SearchService.java
 * @Package com.gewara.service
 * @author shenyanghong paul.wei2011@gmail.com
 * @date Aug 10, 2012 5:57:19 PM
 * @version V1.0  
 */

package com.gewara.untrans;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.gewara.json.GewaSearchKey;
import com.gewara.model.BaseObject;
import com.gewara.model.common.BaseInfo;

/**
 * @ClassName SearchService
 * @Description 站内搜索服务
 * @author weihonglin pau.wei2011@gmail.com
 * @date Aug 10, 2012
 */

public interface SearchService {
	// 默认每页展示条数
	static final int ROWS_PER_PAGE = 20;
	static final int TOP_SK_COUNT = 10;
	static final String CITY_CODE_ALL = "000000";
	static final String ROWS_COUNT = "count";
	static final String ROWS_SK_LIST = "skList";
	static final String ROWS_INFO = "info";
	static final String TOP_SK_LIST = "topSkList";
	static final String API_SEARCH_SAVESEARCHKEY = "/api/saveSearchkey.xhtml";
	static final String API_SEARCH_SEARCHKEY = "/api/searchKey.xhtml";
	static final String API_SEARCH_SEARCHKEY_NUM = "/api/searchKeyNum.xhtml";
	static final String API_SEARCH_TOPSEARCHKEY = "/api/topSearchKey.xhtml";

	/**
	 * s
	 * 
	 * @Method: searchKey
	 * @Description: 根据关键字搜索，返回索引相关信息
	 * @param skey
	 * @param tag
	 * @param category
	 * @param pageNo
	 * @return Map<String,Object>
	 */
	Map<String, Object> searchKey(String ip,String citycode, String skey,String channel, String tag, String category, Integer pageNo);
	Map<String, Object> searchKey(String ip,String citycode,String skey, String channel,String tag, String category, Integer pageNo, Integer rowsPerPage);
	/**
	 * @Method: searchKey
	 * @Description: 自动完成，匹配检索关键字提示
	 * @param tag
	 * @param skey
	 * @param maxnum
	 * @return Map<String,Object>
	 */
	Set<String> searchKey(String citycode,String channel,String tag,String category, String skey, int maxnum);

	/**
	 * @Method: getSearchLight
	 * @Description: 根据展示内容高亮显示
	 * @param content
	 * @param skey
	 * @param length
	 * @return String
	 */
	Map<String, Object> getBeanSearchLight(Object bean, String skey);

	/**
	 * @Method: saveBatchSearchKey
	 * @Description: timenum时间之后批量迁移数据，用于数据同步
	 * @return String
	 */
	String saveBatchSearchKey(Long timenum);

	/**
	 * @Method: saveBatchSearchKey
	 * @Description: 批量请求数据，用于数据同步
	 * @return String
	 */
	void saveSearchKeyList(List<GewaSearchKey> list);

	/**
	 * @Method: getTopSearchKeyList
	 * @Description: 热门搜索
	 * @param count
	 * @return List<String>
	 */
	List<String> getTopSearchKeyList(Integer count);
	/**
	 * @Description: 推送更新实体索引
	 * @param object
	 */
	void pushSearchKey(Object object);
	
	/** 
	* @Method: reBuildIndex 
	* @Description: 根据新的索引内容重构索引
	* @param clazz
	* @return String
	*/
	
	<T extends BaseObject> int reBuildIndex(Class<T> clazz);
	
	/** 
	* @Method: isCurrentCity 
	* @Description: 判断检索内容是否属于当前城市
	* @param bean
	* @param citycode
	* @return boolean
	*/
	boolean isCurrentCity(BaseObject bean,String citycode);
	/** 
	* @Method: getSearchKey 
	* @Description: 场馆searchkey
	* @param baseInfo
	* @return String
	*/
	<T extends BaseInfo> String getSearchKey(T baseInfo);
	
	
	/** 
	* @Method: sortSK 
	* @Description: 关键字关联对象页面排序map分组
	* @param List<GewaSearchKey> list
	* @return Map
	*/
	List<GewaSearchKey> sortSK(List<GewaSearchKey> list);

	/**
	 * 搜索
	 * @param tag
	 * @param skey
	 * @param maxnum
	 * @return
	 */
	Set<String> getSearchKeyList(String tag, String skey, int maxnum);
}
