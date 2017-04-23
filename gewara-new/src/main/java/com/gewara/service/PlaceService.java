package com.gewara.service;

import java.util.List;
import java.util.Map;

import com.gewara.model.common.BaseInfo;
import com.gewara.model.common.City;
import com.gewara.model.common.County;
import com.gewara.model.common.Indexarea;
import com.gewara.model.common.Line2Station;
import com.gewara.model.common.Province;
import com.gewara.model.common.Subwayline;
import com.gewara.model.common.Subwaystation;

/**
 * @author <a href="mailto:acerge@163.com">gebiao(acerge)</a>
 * @since 2007-9-28下午02:05:17
 */
public interface PlaceService{
	List<Subwayline> getSubwaylinesByCityCode(String citycode);
	List<Subwaystation> getSubwaystationsByCityCode(String citycode);
	List<Subwaystation> getSubwaystationsByLineId(Long lineId);
	Integer getSubwaystationCount(String stationname);
	List<Subwaystation> getSubwaystationList(String stationname, int from, int maxnum);
	Subwaystation getSubwaystation(String stationname);
	List<Line2Station> getLine2StationByLineId(Long lineId);
	Subwayline getSubwaylineByCitycodeAndName(String citycode, String linename);
	List<Province> getAllProvinces();
	/**
	 * @return Map<CountyCode, CountyName>
	 */
	Map<String,String> getCountyPairByCityCode(String cityCode);
	
	List<City> getCityByProvinceCode(String provinceCode);
	List<County> getCountyByCityCode(String cityCode);
	List<Indexarea> getIndexareaByCountyCode(String countyCode);
	<S extends BaseInfo> List<S> getPlaceList(String citycode, Class<S> clazz, String orderField, boolean asc, int from, int maxnum);
	<T extends BaseInfo> T getZbPlace(Class<T> clazz, String countycode, String indexareacode);
	
	<T extends BaseInfo> List<T> getPlaceListByIndexareaCode(Class<T> clazz, String indexareacode, String orderField, boolean asc);
	<T extends BaseInfo> List<T> getPlaceListByIndexareaCode(Class<T> clazz, String indexareacode, String orderField, boolean asc, int from, int maxrow);
	<T extends BaseInfo> List<T> getPlaceListByCountyCode(Class<T> clazz, String countycode, String orderField, boolean asc);
	<T extends BaseInfo> List<T> getPlaceListByCountyCode(Class<T> clazz, String countycode, String orderField, boolean asc, int from, int maxrow);

	<T extends BaseInfo> Integer getPlaceCount(Class<T> clazz, String citycode);
	<T extends BaseInfo> Integer getPlaceCountByCountyCode(Class<T> clazz, String countycode);
	<T extends BaseInfo> Integer getPlaceCountByIndexareaCode(Class<T> clazz, String indexareacode);
	
	<T extends BaseInfo> void updateHotValue(Class<T> clazz, Long placeId, Integer hotvalue);
	<T extends BaseInfo> List<T> getPlaceListByHotvalue(String citycode, Class<T> clazz, int from,int maxnum);
	<T extends BaseInfo> List<T> getPlaceListByHotvalue(String citycode, Class<T> clazz, int hotvalue, int from,int maxnum);
	
	<T extends BaseInfo> List<T> searchPlaceByName(String citycode, Class<T> clazz, String name);
	<T extends BaseInfo> T getPlaceByName(String citycode, Class<T> clazz, String name);
	<T extends BaseInfo> List<Map> getPlaceCountyCountMap(Class<T> clazz, String citycode);
	<T extends BaseInfo> List<Map> getPlaceIndexareaCountMap(Class<T> clazz, String countycode);
	/**
	 * 根据tag、countycode、indexarea获取场所列表
	 * @param tag
	 * @param countycode
	 * @param indexareacode
	 * @return
	 */
	List<BaseInfo> getPlaceListByTag(String tag, String countycode, String indexareacode);
	List<BaseInfo> getPlaceListByTag(String tag, String countycode, String indexareacode, int from, int maxnum);
	
	List<Map<String, Object>> getPlaceGroupMapByCitySubwayline(String citycode, String tag);
	String getIndexareaname(String  indexareacode);
	List<Indexarea> getUsefulIndexareaList(String countycode,String tag);
	Map<String, String> getSubwaylineMap(String citycode);
	
	/**
	 *  取得所在城市, 所在区的中文
	 * */
	public String getLocationPair(Long memberid, String joinChar);
	
	/**
	 *  标识 + code, 返回Map<String, String>
	 *  eg. getPlaceMapBycode("citycode", "310000") => "上海"
	 */
	String getPlaceNameBycode(String tag, String code);
	List<Map> getSubwaystationList(String citycode, String tag, Long lineid);
	String getCountyname(String countycode);
	/**
	 * 通过区域编码更新区域名称，简称，城市编码
	 * @param county		区域对象
	 */
	void updateCounty(County county);
	
	/**
	 * 通过线路ID更新线路名称，说明，城市编码
	 * @param lid			线路ID
	 * @param citycode		城市编码
	 * @param linename		线路名称
	 * @param remark		线路说明
	 */
	void updateSubwayline(Long lid, String citycode, String linename, String remark);
	
	/**
	 * 通过站点ID更新站点名称
	 * @param sid				站点ID
	 * @param stationname		站点名称
	 */
	void updateSubwaystation(Long sid, String stationname);
	/**
	 * 通过线路与站点关联ID更新线路ID与站点ID
	 * @param recordid		线路与站点关联ID
	 * @param lid			线路ID
	 * @param sid			站点ID
	 */
	void updateLine2Station(Long recordid, Long lid, Long sid);
	
	/**
	 * 通过线路与站点关联ID更新排序字段
	 * @param recordid			线路与站点关联ID
	 * @param stationorder		排序字段
	 */
	void updateLine2StationOrder(Long recordid, Integer stationorder);
	/**
	 * 更新线路站点的 首末班车时间
	 * @param otherinfo
	 * @param lineId
	 * @param stationId
	 */
	void updateLine2StationOtherinfo(Long recordid, String otherinfo);
	Line2Station getLine2StationListByLineIdAndStationId(Long lineId, Long stationId);
}
