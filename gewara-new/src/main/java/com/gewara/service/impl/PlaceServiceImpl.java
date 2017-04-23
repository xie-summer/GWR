package com.gewara.service.impl;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.gewara.constant.AdminCityContant;
import com.gewara.constant.sys.CacheConstant;
import com.gewara.helper.sys.RelateClassHelper;
import com.gewara.model.BaseObject;
import com.gewara.model.common.BaseInfo;
import com.gewara.model.common.City;
import com.gewara.model.common.County;
import com.gewara.model.common.Indexarea;
import com.gewara.model.common.Line2Station;
import com.gewara.model.common.Province;
import com.gewara.model.common.Subwayline;
import com.gewara.model.common.Subwaystation;
import com.gewara.model.user.MemberInfo;
import com.gewara.service.PlaceService;
import com.gewara.support.ReadOnlyTemplate;
import com.gewara.support.ServiceHelper;
import com.gewara.untrans.CacheService;
import com.gewara.util.CachableCall;
import com.gewara.util.CachableServiceHelper;
import com.gewara.util.RandomUtil;

/**
 * @author <a href="mailto:acerge@163.com">gebiao(acerge)</a>
 * @since 2007-9-28下午02:05:17
 */
@Service("placeService")
public class PlaceServiceImpl extends BaseServiceImpl implements PlaceService, InitializingBean {
	private Map<String/*citycode*/, List<Subwayline>> subwaylineMap = new HashMap<String, List<Subwayline>>();
	private Map<String/*citycode*/, List<Subwaystation>> subwaystationMap = new HashMap<String, List<Subwaystation>>();
	private Map<Long/*lineId*/, List<Subwaystation>> stationMap = new HashMap<Long, List<Subwaystation>>();
	private Map<String/*provinceId*/, List<City>> cityMap = new HashMap<String, List<City>>();
	private Map<String/*citycode*/, List<County>> countyMap = new HashMap<String, List<County>>();
	private Map<String/*countycode*/, List<Indexarea>> indexareaMap = new HashMap<String, List<Indexarea>>();
	
	@Autowired@Qualifier("cacheService")
	private CacheService cacheService;
	
	private CachableServiceHelper helper;
	@Autowired@Qualifier("readOnlyTemplate")
	private ReadOnlyTemplate readOnlyTemplate;
	public void setReadOnlyHibernateTemplate(ReadOnlyTemplate readOnlyTemplate) {
		this.readOnlyTemplate = readOnlyTemplate;
	}	
	@Override
	public List<Subwaystation> getSubwaystationList(String stationname, int from, int maxnum){
		DetachedCriteria query = DetachedCriteria.forClass(Subwaystation.class);
		if(StringUtils.isNotBlank(stationname))query.add(Restrictions.like("stationname", stationname, MatchMode.ANYWHERE));
		query.addOrder(Order.asc("id"));
		List<Subwaystation> result = hibernateTemplate.findByCriteria(query, from, maxnum);
		return result;
	}
	@Override
	public Integer getSubwaystationCount(String stationname){
		DetachedCriteria query = DetachedCriteria.forClass(Subwaystation.class);
		query.setProjection(Projections.rowCount());
		if(StringUtils.isNotBlank(stationname))query.add(Restrictions.like("stationname", stationname, MatchMode.ANYWHERE));
		List result = hibernateTemplate.findByCriteria(query);
		if(result.isEmpty()) return 0;
		return Integer.valueOf(""+result.get(0));
	}
	@Override
	public Subwaystation getSubwaystation(String stationname){
		String hql= "from Subwaystation s where s.stationname=?";
		List<Subwaystation> subwaystationList = hibernateTemplate.find(hql, stationname);
		return subwaystationList.isEmpty()? null: subwaystationList.get(0);
	}
	@Override
	public List<Line2Station> getLine2StationByLineId(Long lineId){
		String hql = "from Line2Station l where l.line.id=? order by l.stationorder";
		List result = hibernateTemplate.find(hql, lineId);
		return result;
	}
	
	private static final String querySubwaylineList = "from Subwayline s where s.citycode = ? order by s.linename";
	
	@Override
	public List<Subwayline> getSubwaylinesByCityCode(String citycode) {
		List<Subwayline> lineList = subwaylineMap.get(citycode);
		if(lineList==null){
			lineList = hibernateTemplate.find(querySubwaylineList, citycode);
			if(!lineList.isEmpty())subwaylineMap.put(citycode, lineList);
		}
		return lineList;
	}
	@Override
	public List<Subwaystation> getSubwaystationsByCityCode(String citycode){
		List<Subwaystation> subwaystationList = subwaystationMap.get(citycode);
		if(subwaystationList==null){
			subwaystationList = hibernateTemplate.find("from Subwaystation s where s.citycode = ? order by s.stationname", citycode);
			if(!subwaystationList.isEmpty())subwaystationMap.put(citycode, subwaystationList);
		}
		return subwaystationList;
	}
	private static final String queryStationList = "select station from Line2Station s where s.line.id = ? order by s.stationorder";
	@Override
	public List<Subwaystation> getSubwaystationsByLineId(Long lineId) {
		List<Subwaystation> stationList = stationMap.get(lineId);
		if(stationList==null){
			stationList = hibernateTemplate.find(queryStationList, lineId);
			if(!stationList.isEmpty())stationMap.put(lineId, stationList);
		}
		return stationList;
	}

	@Override
	public Subwayline getSubwaylineByCitycodeAndName(String citycode, String linename) {
		List result = hibernateTemplate.find("from Subwayline s where s.citycode = ? and s.linename=?", citycode, linename);
		if(result.size()>0) return (Subwayline) result.get(0);
		return null;
	}
	@Override
	public List<Province> getAllProvinces() {
		return hibernateTemplate.find("from Province p order by p.provincecode");
	}
	
	@Override
	public Map<String, String> getCountyPairByCityCode(String cityCode) {
		List<County> countyList = this.getCountyByCityCode(cityCode);
		Map<String,String> countyPair = new HashMap<String,String>();
		for(County county:countyList){
			countyPair.put(county.getCountycode(), county.getCountyname());
		}
		return countyPair;
	}

	private static final String queryCity = "from City c where c.province.provincecode = ? order by c.citycode";
	@Override
	public List<City> getCityByProvinceCode(String provinceCode) {
		List<City> cityList = cityMap.get(provinceCode);
		if(cityList ==null){
			cityList = hibernateTemplate.find(queryCity, provinceCode);
			if(!cityList.isEmpty()) cityMap.put(provinceCode, cityList);
		}
		return cityList;
	}
	private static final String queryCountyList = "from County c where c.citycode = ? order by c.countycode";
	@Override
	public List<County> getCountyByCityCode(String citycode) {
		List<County> countyList = countyMap.get(citycode);
		if(countyList ==null){
			countyList = hibernateTemplate.find(queryCountyList, citycode);
			if(!countyList.isEmpty()) countyMap.put(citycode, countyList);
		}
		return countyList;
	}
	private static final String queryIndexareaList = "from Indexarea ia where ia.county.countycode = ? order by ia.indexareacode";
	@Override
	public List<Indexarea> getIndexareaByCountyCode(String countyCode) {
		List<Indexarea> indexareaList = indexareaMap.get(countyCode);
		if(indexareaList==null){
			indexareaList = hibernateTemplate.find(queryIndexareaList, countyCode);
			if(!indexareaList.isEmpty()) indexareaMap.put(countyCode, indexareaList);
		}
		return indexareaList;
	}

	@Override
	public <S extends BaseInfo> List<S> getPlaceList(String citycode, Class<S> clazz, String orderField, boolean asc, int from, int maxnum) {
		DetachedCriteria  query = DetachedCriteria.forClass(clazz);
		query.add(Restrictions.eq("citycode", citycode));
		query.setProjection(Projections.id());
		if(StringUtils.isBlank(orderField)) orderField = "clickedtimes";
		if(asc) query.addOrder(Order.asc(orderField));
		else query.addOrder(Order.desc(orderField));
		List<Serializable> idList = hibernateTemplate.findByCriteria(query, from, maxnum);
		List<S> placeList = baseDao.getObjectList(clazz, idList);
		return placeList;
	}

	@Override
	public <S extends BaseInfo> List<S> getPlaceListByIndexareaCode(Class<S> clazz, String indexareacode, String orderField, boolean asc){
		DetachedCriteria query = DetachedCriteria.forClass(clazz);
		query.add(Restrictions.eq("indexareacode", indexareacode));
		if(StringUtils.isBlank(orderField)) orderField = "clickedtimes";
		if(asc) query.addOrder(Order.asc(orderField));
		else query.addOrder(Order.desc(orderField));
		query.setProjection(Projections.id());
		List<Serializable> idList = hibernateTemplate.findByCriteria(query);
		List<S> placeList = baseDao.getObjectList(clazz, idList);
		return placeList;
	}
	@Override
	public <S extends BaseInfo> List<S> getPlaceListByIndexareaCode(Class<S> clazz, String indexareacode, String orderField, boolean asc, int from, int maxnum) {
		DetachedCriteria query = DetachedCriteria.forClass(clazz);
		query.add(Restrictions.eq("indexareacode", indexareacode));
		if(StringUtils.isBlank(orderField)) orderField = "clickedtimes";
		if(asc) query.addOrder(Order.asc(orderField));
		else query.addOrder(Order.desc(orderField));
		query.setProjection(Projections.id());
		List<Serializable> idList = hibernateTemplate.findByCriteria(query, from, maxnum);
		List<S> placeList = baseDao.getObjectList(clazz, idList);
		return placeList;
	}
	@Override
	public <S extends BaseInfo> List<S> getPlaceListByCountyCode(Class<S> clazz, String countycode, String orderField, boolean asc) {
		DetachedCriteria query = DetachedCriteria.forClass(clazz);
		query.add(Restrictions.eq("countycode", countycode));
		if(StringUtils.isBlank(orderField)) orderField = "clickedtimes";
		if(asc) query.addOrder(Order.asc(orderField));
		else query.addOrder(Order.desc(orderField));
		query.setProjection(Projections.id());
		List<Serializable> idList = hibernateTemplate.findByCriteria(query);
		List<S> placeList = baseDao.getObjectList(clazz, idList);
		return placeList;
	}
	@Override
	public <S extends BaseInfo> List<S> getPlaceListByCountyCode(Class<S> clazz, String countycode, String orderField, boolean asc, int from, int maxnum) {
		DetachedCriteria query = DetachedCriteria.forClass(clazz);
		query.add(Restrictions.eq("countycode", countycode));
		if(StringUtils.isBlank(orderField)) orderField = "clickedtimes";
		if(asc) query.addOrder(Order.asc(orderField));
		else query.addOrder(Order.desc(orderField));
		query.setProjection(Projections.id());
		List<Serializable> idList = hibernateTemplate.findByCriteria(query, from, maxnum);
		List<S> placeList = baseDao.getObjectList(clazz, idList);
		return placeList;
	}
	@Override
	public <S extends BaseInfo> S getZbPlace(Class<S> clazz, String countycode, String indexareacode){
		List<S> placeList = null;
		if(StringUtils.isNotBlank(indexareacode)){
			placeList = getPlaceListByIndexareaCode(clazz, indexareacode, "clickedtimes", false);
			if(placeList.isEmpty()) placeList = getPlaceListByCountyCode(clazz, countycode, "clickedtimes", false);
		}else{
			placeList = getPlaceListByCountyCode(clazz, countycode, "clickedtimes", false);
		}
		S place = RandomUtil.getRandomObject(placeList);
		return place;
	}

	@Override
	public <S extends BaseInfo> Integer getPlaceCount(Class<S> clazz, String citycode){
		DetachedCriteria query=DetachedCriteria.forClass(clazz);
		query.add(Restrictions.eq("citycode",citycode));
		query.setProjection(Projections.rowCount());
		List pagenum = hibernateTemplate.findByCriteria(query);
		if(pagenum.isEmpty()) return 0;
		return Integer.parseInt(pagenum.get(0)+"");
	}
	@Override
	public <S extends BaseInfo> Integer getPlaceCountByCountyCode(Class<S> clazz, String countycode){
		DetachedCriteria query=DetachedCriteria.forClass(clazz);
		query.add(Restrictions.eq("countycode", countycode));
		query.setProjection(Projections.rowCount());
		List pagenum = hibernateTemplate.findByCriteria(query);
		if(pagenum.isEmpty()) return 0;
		return Integer.parseInt(pagenum.get(0)+"");
	}
	@Override
	public <S extends BaseInfo> Integer getPlaceCountByIndexareaCode(Class<S> clazz, String indexareacode){
		DetachedCriteria query=DetachedCriteria.forClass(clazz);
		query.add(Restrictions.eq("indexareacode",indexareacode));
		query.setProjection(Projections.rowCount());
		List pagenum = hibernateTemplate.findByCriteria(query);
		if(pagenum.isEmpty()) return 0;
		return Integer.parseInt(pagenum.get(0)+"");
	}

	@Override
	public <S extends BaseInfo> void updateHotValue(Class<S> clazz, Long placeId, Integer hotvalue) {
		S place = baseDao.getObject(clazz, placeId);
		place.setHotvalue(hotvalue);
		baseDao.saveObject(place);
	}

	@Override
	public <T extends BaseInfo> List<T> getPlaceListByHotvalue(String citycode, Class<T> clazz, int hotvalue, int from, int maxnum) {
		DetachedCriteria query = DetachedCriteria.forClass(clazz);
		query.add(Restrictions.eq("citycode", citycode));
		query.add(Restrictions.eq("hotvalue", hotvalue));
		query.addOrder(Order.desc("clickedtimes"));
		query.setProjection(Projections.id());
		List<Serializable> idList = hibernateTemplate.findByCriteria(query, from, maxnum);
		List<T> placeList = baseDao.getObjectList(clazz, idList);
		return placeList;
	}
	@Override
	public <T extends BaseInfo> List<T> getPlaceListByHotvalue(String citycode, Class<T> clazz, int from, int maxnum) {
		DetachedCriteria query=DetachedCriteria.forClass(clazz);
		query.add(Restrictions.eq("citycode", citycode));
		query.addOrder(Order.desc("hotvalue"));
		query.addOrder(Order.desc("clickedtimes"));
		query.setProjection(Projections.id());
		List<Serializable> idList = hibernateTemplate.findByCriteria(query, from, maxnum);
		List<T> placeList = baseDao.getObjectList(clazz, idList);
		return placeList;
	}
	
	@Override
	public <T extends BaseInfo> List<T> searchPlaceByName(String citycode, Class<T> clazz, String name) {
		DetachedCriteria query = DetachedCriteria.forClass(clazz);
		query.add(Restrictions.eq("citycode", citycode));
		query.add(Restrictions.or(
				Restrictions.ilike("name", name, MatchMode.ANYWHERE),
				Restrictions.ilike("pinyin", name, MatchMode.ANYWHERE)));
		List placeList = hibernateTemplate.findByCriteria(query);
		return placeList;
	}
	@Override
	public <T extends BaseInfo> T getPlaceByName(String citycode, Class<T> clazz, String name) {
		DetachedCriteria query = DetachedCriteria.forClass(clazz);
		query.add(Restrictions.eq("citycode", citycode));
		query.add(Restrictions.eq("name", name));
		List<T> placeList = hibernateTemplate.findByCriteria(query);
		if(placeList.isEmpty()) return null;
		return placeList.get(0);
	}
	@Override
	public List<BaseInfo> getPlaceListByTag(String tag, String countycode, String indexareacode){
		if(!ServiceHelper.isTag(tag)) return null;
		List result = null;
		Class<? extends BaseInfo> clazz = ServiceHelper.getPalceClazz(tag);
		if(clazz == null) return new ArrayList();

		if(StringUtils.isNotBlank(indexareacode)){
			result = getPlaceListByIndexareaCode(clazz, indexareacode, "clickedtimes", false);
		}else if(StringUtils.isNotBlank(countycode)){
			result = getPlaceListByCountyCode(clazz, countycode, "clickedtimes", false);
		}
		return result;
	}
	@Override
	public List getPlaceListByTag(String tag, String countycode, String indexareacode, int from, int maxnum){
		if(!ServiceHelper.isTag(tag)) return null;
		List result = null;
		Class<? extends BaseInfo> clazz = ServiceHelper.getPalceClazz(tag);
		if(clazz == null) return new ArrayList();

		if(StringUtils.isNotBlank(indexareacode)){
			result = getPlaceListByIndexareaCode(clazz, indexareacode, "clickedtimes", false, from, maxnum);
		}else if(StringUtils.isNotBlank(countycode)){
			result = getPlaceListByCountyCode(clazz, countycode, "clickedtimes", false, from, maxnum);
		}
		return result;
	}
	@Override
	public List<Map<String, Object>> getPlaceGroupMapByCitySubwayline(String citycode, final String tag){
		final String code = AdminCityContant.zxsList.contains(citycode)?citycode.substring(0, 2):citycode.substring(0, 4);
		String key = CacheConstant.buildKey("getPlaXupMapBySubLine", code);
		List<Map<String, Object>> list = helper.cacheCall(key, CacheConstant.SECONDS_HALFDAY, new CachableCall<List<Map<String, Object>>>() {
			@Override
			public List<Map<String, Object>> call() {
				List<Map<String, Object>> ret = jdbcTemplate.queryForList("select sum(placecount) as placecount, lineid, linename from WEBDATA.PLACE_STATION_COUNT pc where pc.tag=? and pc.countycode like ? group by lineid, linename", tag, code+"%");
				Collections.sort(ret, new Comparator() {
					public int compare(Object o1, Object o2) {
						String s1 = (String) ((Map)o1).get("LINENAME");
						String s2 = (String) ((Map)o2).get("LINENAME");
						return StringUtils.leftPad(s1, 4, "0").compareTo(StringUtils.leftPad(s2, 4, "0"));
					}
				});
				return ret;
			}
		});
		return list;
	}
	public String getCountyname(String countycode){
		County county = baseDao.getObject(County.class, countycode);
		if(county!=null) return county.getCountyname();
		return "";
	}
	@Override
	public <T extends BaseInfo> List<Map> getPlaceCountyCountMap(Class<T> clazz, String citycode){
		String sql = "select new map(countycode as countycode, count(id) as placecount) from " + clazz.getSimpleName() +
				" where citycode=? and countycode is not null group by countycode having count(id) > 0 order by count(id) desc";
		List<Map> list = hibernateTemplate.find(sql, citycode);
		for(Map entry:list){
			County county = baseDao.getObject(County.class, (String)entry.get("countycode"));
			entry.put("county", county);
		}
		return list;
	}
	@Override
	public <T extends BaseInfo> List<Map> getPlaceIndexareaCountMap(Class<T> clazz, String countycode){
		String query = "select new map(indexareacode as indexareacode, count(id) as placecount) from " + clazz.getSimpleName() +
						" where countycode=? and indexareacode is not null group by indexareacode having count(id) >0 order by count(id) desc";
		List<Map> indexareMap = hibernateTemplate.find(query, countycode);
		for(Map entry: indexareMap){
			Indexarea indexarea = baseDao.getObject(Indexarea.class, (String)entry.get("indexareacode"));
			entry.put("indexarea", indexarea);
		}
		return indexareMap;
	}
	@Override
	public String getIndexareaname(String indexareacode) {
		Indexarea indexarea = baseDao.getObject(Indexarea.class, indexareacode);
		return indexarea==null?"":indexarea.getIndexareaname();
	}
	/**
	 *  根据 区/县 取得相关商圈
	 */
	@Override
	public List<Indexarea> getUsefulIndexareaList(String countycode, String tag){
		String query = "select distinct t.indexareacode from " + RelateClassHelper.getRelateClazz(tag).getSimpleName()+" t where t.countycode = ?";
		List<String> result = hibernateTemplate.find(query, countycode);
		return baseDao.getObjectList(Indexarea.class, result);
	}
	@Override
	public Map<String, String> getSubwaylineMap(String citycode){
		Map<String, String> linenameMap = new HashMap<String, String>();
		List<Subwayline> lineList = getSubwaylinesByCityCode(citycode);
		for(Subwayline line : lineList){
			linenameMap.put(line.getId()+"", line.getLinename());
		}
		return linenameMap;
	}
	
	/**
	 *  取得所在省份 - 城市 - 区 - 商圈, 所在区的中文
	 * */
	@Override
	public String getLocationPair(Long memberid, String joinChar){
		String location = " ";
		if(memberid != null){
			MemberInfo memberInfo = baseDao.getObject(MemberInfo.class, memberid);
			Province province = baseDao.getObject(Province.class, memberInfo.getLiveprovince());
			if(province != null){
				String provincename = province.getProvincename();
				if(StringUtils.isNotBlank(provincename)){
					location = provincename + joinChar;
				}
			}
			City city = baseDao.getObject(City.class, memberInfo.getLivecity());
			if(city != null){
				String cityname = city.getCityname();
				if(StringUtils.isNotBlank(cityname) && !AdminCityContant.zxsNameList.contains(cityname)){
					location += cityname + joinChar;
				}
			}
			County county = baseDao.getObject(County.class, memberInfo.getLivecounty());
			if(county != null){
				String countyname = county.getCountyname();
				if(StringUtils.isNotBlank(countyname)){
					location += countyname + joinChar;
				}
			}
			Indexarea indexarea = baseDao.getObject(Indexarea.class, memberInfo.getLiveindexarea());
			if(indexarea != null){
				String indexareaname = indexarea.getIndexareaname();
				if(StringUtils.isNotBlank(indexareaname)){
					location += indexareaname + joinChar;
				}
			}
		}
		return StringUtils.substring(location, 0, location.length()-joinChar.length());
	}
	
	/**
	 *  标识 + code, 返回Map<String, String>
	 *  eg. getPlaceMapBycode("city", "310000") => Map<"310000", "上海">
	 */
	public String getPlaceNameBycode(String tag, String code){
		BaseObject baseObject = baseDao.getObject(ServiceHelper.getPlaceLocationClazz(tag), code);
		if(baseObject instanceof City){
			return ((City)baseObject).getCityname();
		}else if(baseObject instanceof Province){
			return ((Province)baseObject).getProvincename();
		}else if(baseObject instanceof County){
			return ((County)baseObject).getCountyname();
		}else if(baseObject instanceof Indexarea){
			return ((Indexarea)baseObject).getIndexareaname();
		}
		return "";
	}
	@Override
	public List<Map> getSubwaystationList(String citycode, String tag, Long lineId) {
		String hql = "select new map(stationid as stationid, count(*) as count) from " 
			+ RelateClassHelper.getRelateClazz(tag).getSimpleName() + " where citycode=? and lineidlist like ? and stationid is not null group by stationid having count(*)>=0";
		List<Map> subwaystationList = readOnlyTemplate.find(hql, citycode, "%" + lineId + "%");
		for(Map m : subwaystationList){
			Subwaystation station = baseDao.getObject(Subwaystation.class, Long.valueOf(m.get("stationid")+""));
			m.put("subwaystation", station);
		}
		return null;
	}
	
	
	@Override
	public void updateCounty(County county){
		String sql = "update WEBDATA.county set countyname=?,briefname=?,citycode=? where countycode=?";
		jdbcTemplate.update(sql, county.getCountyname(), county.getBriefname(), county.getCitycode(), county.getCountycode());
	}
	
	@Override
	public void updateSubwayline(Long lid, String citycode, String linename, String remark){
		String sql = "update WEBDATA.SUBWAYLINE set linename=?,remark=?,citycode=? where RECORDID=?";
		jdbcTemplate.update(sql, linename, remark, citycode, lid);
	}
	
	@Override
	public void updateSubwaystation(Long sid, String stationname){
		String sql = "update WEBDATA.SUBWAYSTATION set stationname=? where RECORDID=?";
		jdbcTemplate.update(sql, stationname, sid);
	}
	
	@Override
	public void updateLine2Station(Long recordid, Long lid, Long sid){
		String sql = "update WEBDATA.line2station set sid=? where recordid=? and lid=?";
		jdbcTemplate.update(sql, sid, recordid, lid);
	}
	
	public void updateLine2StationOrder(Long recordid, Integer stationorder){
		String sql = "update WEBDATA.line2station set stationorder=? where recordid=?";
		jdbcTemplate.update(sql, stationorder, recordid);
	}
	@Override
	public void updateLine2StationOtherinfo(Long recordid, String otherinfo){
		String sql = "update WEBDATA.line2station set otherinfo=? where recordid=?";
		jdbcTemplate.update(sql, otherinfo, recordid);
	}
	@Override
	public Line2Station getLine2StationListByLineIdAndStationId(Long lineId, Long stationId){
		String sql = "from Line2Station s where s.line.id=? and s.station.id = ?";
		List<Line2Station> list = hibernateTemplate.find(sql, lineId,stationId);
		return list.isEmpty() ? null : list.get(0);
	}
	@Override
	public void afterPropertiesSet() throws Exception {
		helper =  new CachableServiceHelper("placeService", "ps", cacheService);
	}
}
