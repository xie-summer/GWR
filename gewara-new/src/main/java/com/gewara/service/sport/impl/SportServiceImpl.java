package com.gewara.service.sport.impl;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.apache.commons.lang.StringUtils;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.criterion.Subqueries;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.support.PropertyComparator;
import org.springframework.stereotype.Service;

import com.gewara.constant.OpenTimeTableConstant;
import com.gewara.constant.Status;
import com.gewara.constant.sys.CacheConstant;
import com.gewara.constant.ticket.OrderConstant;
import com.gewara.model.common.County;
import com.gewara.model.pay.SportOrder;
import com.gewara.model.sport.OpenTimeItem;
import com.gewara.model.sport.OpenTimeTable;
import com.gewara.model.sport.ProgramItemTime;
import com.gewara.model.sport.Sport;
import com.gewara.model.sport.Sport2Item;
import com.gewara.model.sport.SportItem;
import com.gewara.model.sport.SportItemPrice;
import com.gewara.model.sport.SportOrder2TimeItem;
import com.gewara.model.sport.SportPrice;
import com.gewara.model.sport.SportPriceTable;
import com.gewara.model.sport.SportProfile;
import com.gewara.service.impl.BaseServiceImpl;
import com.gewara.service.sport.SportService;
import com.gewara.support.ReadOnlyTemplate;
import com.gewara.untrans.CacheService;
import com.gewara.util.BeanUtil;
import com.gewara.util.DateUtil;
import com.gewara.util.ValidateUtil;

/**
 * @author <a href="mailto:acerge@163.com">gebiao(acerge)</a>
 * @since 2007-9-28下午02:05:17
 */
@Service("sportService")
public class SportServiceImpl extends BaseServiceImpl implements SportService {
	@Autowired@Qualifier("readOnlyTemplate")
	private ReadOnlyTemplate readOnlyTemplate;
	public void setReadOnlyHibernateTemplate(ReadOnlyTemplate readOnlyTemplate) {
		this.readOnlyTemplate = readOnlyTemplate;
	}
	@Autowired@Qualifier("cacheService")
	private CacheService cacheService;
	@Override
	public List<Sport> getHotSports(String citycode, String order, boolean isHot, int maxnum) {
		DetachedCriteria criteria=DetachedCriteria.forClass(Sport.class);
		criteria.add(Restrictions.eq("citycode", citycode));
		if(isHot) criteria.add(Restrictions.like("flag", Sport.FLAG_HOT, MatchMode.END));
		criteria.add(Restrictions.eq("citycode", citycode));
		if(StringUtils.isBlank(order)){
			criteria.addOrder(Order.desc("updatetime"));
		}else{
			criteria.addOrder(Order.desc(order));
		}
		criteria.addOrder(Order.desc("clickedtimes"));
		List sportList = readOnlyTemplate.findByCriteria(criteria, 0, maxnum);
		return sportList;
	}

	@Override
	public boolean updateSportFlag(Long sportId, String flag) {
		Sport sport = baseDao.getObject(Sport.class, sportId);
		if(sport!=null){
			sport.setFlag(flag);
			sport.setUpdatetime(new Timestamp(System.currentTimeMillis()));
			baseDao.saveObject(sport);
		}
		return true;
	}
	@Override
	public void updateSportHotValue(Long sid,Integer hotvalue){
		Sport sport = baseDao.getObject(Sport.class, sid);
		if(sport!=null){
			sport.setHotvalue(hotvalue);
			sport.setUpdatetime(new Timestamp(System.currentTimeMillis()));
			baseDao.saveObject(sport);
		}
	}
	@Override
	public boolean updateSportItemFlagValue(Long sportId, String value) {
		SportItem sportItem = baseDao.getObject(SportItem.class, sportId);
		if(sportItem!=null){
			sportItem.setFlag(value);
			sportItem.setUpdatetime(new Timestamp(System.currentTimeMillis()));
			baseDao.saveObject(sportItem);
		}
		return true;
	}

	@Override
	public int getPricetableCount() {
		String query = "select count(*) from SportPriceTable";
		List result = readOnlyTemplate.find(query);
		return Integer.parseInt("" + result.get(0));
	}

	@Override
	public List<SportPriceTable> getPricetableList(Long itemid, int from, int maxnum) {
		DetachedCriteria query = DetachedCriteria.forClass(SportPriceTable.class);
		query.add(Restrictions.eq("itemid", itemid));
		query.addOrder(Order.desc("ordernum"));
		return readOnlyTemplate.findByCriteria(query, from, maxnum);
	}

	@Override
	public List<SportPriceTable> getRandomPricetableList(Long itemid, int maxnum) {
		int count = getPricetableCount();
		int from = new Random().nextInt(count - maxnum);
		return getPricetableList(itemid, from, maxnum);
	}
	@Override
	public List<SportItem> getAllSportItem() {
		String query = "from SportItem si where si.parentid != null order by si.clickedtimes";
		List result = readOnlyTemplate.find(query);
		return result;
	}
	@Override
	public List<SportItem> getSubSportItemList(final Long sportItemId, String type) {
		return getSportItemList(null, sportItemId, type, null, 0, 50);
	}
	@Override
	public List<SportItem> getTopSportItemList() {
		return getSubSportItemList(0L, null);
	}
	@Override
	public List<SportItem> getHotSportItemList(int from, int maxnum) {
		DetachedCriteria query = DetachedCriteria.forClass(SportItem.class);
		query.addOrder(Order.desc("clickedtimes"));
		List result = readOnlyTemplate.findByCriteria(query, from, maxnum);
		return result;
	}
	@Override
	public List<SportItem> getCommendSportItemList(int from, int maxnum) {
		DetachedCriteria query = DetachedCriteria.forClass(SportItem.class);
		query.add(Restrictions.like("flag", SportItem.FLAG_RECOMMEND, MatchMode.ANYWHERE));
		query.addOrder(Order.desc("updatetime"));
		List<SportItem> list = readOnlyTemplate.findByCriteria(query, from, maxnum);
		return list;
	}
	@Override
	public List<Map> getCountyAndSportNum(Long id) {
		String hql = "select new map(sport.countycode as countycode,count(*) as num) " +
				"from SportPriceTable st, Sport sport where st.itemid = ? and st.sportid=sport.id " +
				"group by sport.countycode having count(*)>0";
		List<Map> result = readOnlyTemplate.find(hql,id);
		for (Map map : result) {
			map.put("county",baseDao.getObject(County.class,map.get("countycode")+""));
		}
		return result;
	}
	@Override
	public List<Map> getSportListByCountyCode(Long id, String countycode) {
		String hql = "select new map(sport.name as sportname,st.sportid as id) " +
				"from SportPriceTable st, Sport sport where st.itemid = ? and st.sportid=sport.id and sport.countycode = ? ";
		List<Map> result = readOnlyTemplate.find(hql, id, countycode);
		return result;
	}
	@Override
	public List<SportPriceTable> getPriceTableListBySportId(Long sportid) {
		DetachedCriteria query = DetachedCriteria.forClass(SportPriceTable.class);
		query.add(Restrictions.eq("sportid", sportid));
		query.addOrder(Order.asc("ordernum"));
		List<SportPriceTable> list = readOnlyTemplate.findByCriteria(query);
		return list;
	}
	@Override 
	public SportPriceTable getSportPriceTable(Long sportid, Long itemid){
		DetachedCriteria query = DetachedCriteria.forClass(SportPriceTable.class);
		if(null != itemid)query.add(Restrictions.eq("itemid", itemid));
		if(null != sportid)query.add(Restrictions.eq("sportid", sportid));
		List<SportPriceTable> priceTable = readOnlyTemplate.findByCriteria(query);
		if(priceTable.isEmpty()) return null;
		return priceTable.get(0);
	}
	@Override
	public List<SportPrice> getSportPriceList(Long priceTableid){
		DetachedCriteria query = DetachedCriteria.forClass(SportPrice.class);
		query.add(Restrictions.eq("pricetableid", priceTableid));
		List<SportPrice> priceList = readOnlyTemplate.findByCriteria(query);
		return priceList;
	}
	/**
	 *  根据 itemName 匹配 itemList
	 */
	public List<SportItem> getSportlistLikeItemname(String key){
		DetachedCriteria query=DetachedCriteria.forClass(SportItem.class);
		query.add(Restrictions.like("itemname", key, MatchMode.ANYWHERE));
		query.addOrder(Order.desc("clickedtimes"));
		return readOnlyTemplate.findByCriteria(query);
	}

	@Override
	public List<Sport2Item> getSport2ItemListBySportId(Long sportId) {
		String query = "from Sport2Item where sportid=? order by sortnum";
		List<Sport2Item> result = readOnlyTemplate.find(query, sportId);
		return result;
	}

	@Override
	public List<SportItem> getSportItemListBySportId(Long sportId, String booking) {
		String query = "select itemid from Sport2Item where sportid=?";
		List params = new ArrayList();
		params.add(sportId);
		if(StringUtils.isNotBlank(booking)){
			query +=" and booking=? ";
			params.add(booking);
		}
		query += " order by sortnum ";
		List<Long> idList = readOnlyTemplate.find(query, params.toArray());
		List<SportItem> result = baseDao.getObjectList(SportItem.class, idList);
		return result;
	}

	@Override
	public Sport2Item getSport2Item(Long sportId, Long itemId) {
		String query = "from Sport2Item where sportid=? and itemid=?";
		List<Sport2Item> result = hibernateTemplate.find(query, sportId, itemId);
		if(result.isEmpty()) return null;
		return result.get(0);
	}

	@Override
	public int getSportCountByCode(String citycode, String countycode, String indexareacode){
		List result = readOnlyTemplate.findByCriteria(getSportCriteria(citycode, countycode, indexareacode).setProjection(Projections.rowCount()));
		if(result.isEmpty()) return 0;
		return Integer.valueOf(String.valueOf(result.get(0)));
	}
	@Override
	public List<Long> getSportIdByCode(String citycode, String countycode, String indexareacode, int from, int maxnum){
		DetachedCriteria query = getSportCriteria(citycode, countycode, indexareacode);
		query.setProjection(Projections.property("id"));
		return readOnlyTemplate.findByCriteria(query, from, maxnum);
	}
	
	private DetachedCriteria getSportCriteria(String citycode, String countycode, String indexareacode){
		DetachedCriteria query = DetachedCriteria.forClass(Sport.class);
		query.add(Restrictions.eq("citycode", citycode));
		if(StringUtils.isNotBlank(countycode)) query.add(Restrictions.eq("countycode", countycode));
		if(StringUtils.isNotBlank(indexareacode)) query.add(Restrictions.eq("indexareacode", indexareacode));
		query.add(Restrictions.or(Restrictions.ne("flag", "H"),Restrictions.isNull("flag")));
		query.addOrder(Order.desc("hotvalue"));
		query.addOrder(Order.desc("clickedtimes"));
		return query;
	}
	
	@Override
	public List<SportPrice> getPriceList(Long priceTableId) {
		String query = "from SportPrice where pricetableid = ? order by ordernum asc";
		List<SportPrice> result = readOnlyTemplate.find(query, priceTableId);
		return result;
	}

	@Override
	public SportPrice getSportPriceByPriceTableId(Long priceTableId) {
		DetachedCriteria query = DetachedCriteria.forClass(SportPrice.class);
		query.add(Restrictions.eq("pricetableid", priceTableId));
		List<SportPrice> sportpriceList=readOnlyTemplate.findByCriteria(query, 0, 1);
		if(sportpriceList.isEmpty()) return null;
		return sportpriceList.get(0);
	}

	@Override
	public List<Sport> getSportByItemAndClickTimes(Long itemdId, int from, int max) {
		DetachedCriteria query  = DetachedCriteria.forClass(Sport.class,"s");
		DetachedCriteria subquery = DetachedCriteria.forClass(Sport2Item.class,"si");
		subquery.add(Restrictions.eq("itemid", itemdId));
		subquery.add(Restrictions.eqProperty("s.id", "si.sportid"));
		subquery.setProjection(Projections.property("si.id"));
		query.addOrder(Order.desc("clickedtimes"));
		query.add(Subqueries.exists(subquery));
		List<Sport> sportList=null;
		if(from==0&&max==0){
			sportList = readOnlyTemplate.findByCriteria(query);
		}else{
			sportList = readOnlyTemplate.findByCriteria(query,from, max);
		}
		return sportList;
	}

	@Override
	public List<Sport> getBookingEqOpenSport(String citycode,
			String bookingstatus) {
		DetachedCriteria query = DetachedCriteria.forClass(Sport.class);
		if(StringUtils.isNotBlank(citycode))
			query.add(Restrictions.eq("citycode", citycode));
		query.add(Restrictions.eq("booking", bookingstatus));
		query.add(Restrictions.or(Restrictions.ne("flag", "H"),Restrictions.isNull("flag")));
		List<Sport> sportlist = readOnlyTemplate.findByCriteria(query);
		return sportlist;
	}

	@Override
	public List<Map> getMaxHourAndMinHour(Long sportid) {
		String hql="select new map(min(oti.hour) as hourmin,max(oti.hour) as hourmax) from OpenTimeItem oti where oti.sportid="+sportid;
		List<Map> hoursMap=readOnlyTemplate.find(hql);
		return hoursMap;
	}

	@Override
	public List<SportItem> getSportItemBySportId(Long sportid) {
		List<SportItem> sportitemList=new ArrayList<SportItem>();
		if(sportid==null) {
				DetachedCriteria query = DetachedCriteria.forClass(SportItem.class);
				sportitemList=readOnlyTemplate.findByCriteria(query);
		}else{
				sportitemList=getSportItemListBySportId(sportid, null);
		}
		return sportitemList;
	}
	@Override
	public Map<String,Integer> getSportPrice(Long sportid, Long itemid) {
		List<Map<String,Integer>> result = null;
		if(itemid != null){
			String query = "select new map(minprice as pricemin,maxprice as pricemax,avgprice as avgprice) from Sport2Item where sportid = ? and itemid=? and minprice is not null and maxprice is not null and avgprice is not null ";
			result = queryByRowsRange(query, 0, 1, sportid, itemid);
		}else{
			String query = "select new map(min(minprice) as pricemin,max(maxprice) as pricemax,min(avgprice) as avgprice) from Sport2Item where sportid = ? and minprice is not null and maxprice is not null and avgprice is not null group by sportid";
			result = queryByRowsRange(query, 0, 1, sportid);
		}
		if(result.isEmpty())  return new HashMap<String, Integer>();
		return result.get(0);
	}
	
	@Override
	public Map<String,Integer> getSportPriceByOtt(Long sportid, Long itemid, Long ottid){
		String query = "select new map(min(price) as pricemin, max(price) as pricemax, min(norprice) as norpricemin, max(norprice) as norpricemax) from OpenTimeItem where sportid=? and itemid=? and ottid=? and price is not null and norprice is not null";
		List<Map<String,Integer>> result = queryByRowsRange(query, 0, 1, sportid, itemid, ottid);
		if(result.isEmpty())  return new HashMap<String, Integer>();
		return result.get(0);
	}
	@Override
	public List<Sport> getCurSportList(String orderField) {
		String query = "select distinct oti.sportid from OpenTimeItem oti where oti.status='A'" +
				" and exists(select s.id from Sport s where s.id = oti.sportid)";
		List<Long> sportidList = hibernateTemplate.find(query);
		List<Sport> sportList = baseDao.getObjectList(Sport.class, sportidList);
		Collections.sort(sportList, new PropertyComparator((orderField), false, false));
		return sportList;
	}

	@Override
	public List<Long> getBookingItemList(Long itemid, String citycode) {
		String key = CacheConstant.buildKey("get123Booking2342Item12341List45645", itemid, citycode);
		List<Long> idList = (List<Long>) cacheService.get(CacheConstant.REGION_TWENTYMIN, key);
		if(idList == null){
			Timestamp cur = new Timestamp(System.currentTimeMillis());
			DetachedCriteria qry = DetachedCriteria.forClass(OpenTimeTable.class, "o");
			qry.add(Restrictions.eq("o.status", OpenTimeTableConstant.STATUS_BOOK));
			qry.add(Restrictions.eq("o.rstatus", "Y"));
			qry.add(Restrictions.le("o.opentime", cur));
			qry.add(Restrictions.ge("o.closetime", cur));
			Date startdate = DateUtil.getBeginningTimeOfDay(cur);
			qry.add(Restrictions.ge("o.playdate", startdate));
			if(itemid != null){
				qry.add(Restrictions.eq("o.itemid", itemid));
			}
			if(StringUtils.isNotBlank(citycode)){
				qry.add(Restrictions.eq("o.citycode", citycode));
			}
			qry.setProjection(Projections.property("o.itemid"));
			qry.setProjection(Projections.distinct(Projections.property("o.itemid")));
			idList=hibernateTemplate.findByCriteria(qry);
			cacheService.set(CacheConstant.REGION_TWENTYMIN, key, idList);
		}
		
		return idList;
	}
	
	@Override
	public List<Long> getBookingSportIdList(Long itemid, String citycode) {
		String key = CacheConstant.buildKey("get123Booking12313SportId1233111List98089", itemid, citycode);
		List<Long> idList = (List<Long>) cacheService.get(CacheConstant.REGION_TWENTYMIN, key);
		if(idList == null){
			Timestamp cur = new Timestamp(System.currentTimeMillis());
			DetachedCriteria qry = DetachedCriteria.forClass(OpenTimeTable.class, "o");
			qry.add(Restrictions.eq("o.status", OpenTimeTableConstant.STATUS_BOOK));
			qry.add(Restrictions.eq("o.rstatus", "Y"));
			qry.add(Restrictions.le("o.opentime", cur));
			qry.add(Restrictions.ge("o.closetime", cur));
			Date startdate = DateUtil.getBeginningTimeOfDay(cur);
			qry.add(Restrictions.ge("o.playdate", startdate));
			if(itemid != null){
				qry.add(Restrictions.eq("o.itemid", itemid));
			}
			if(StringUtils.isNotBlank(citycode)){
				qry.add(Restrictions.eq("o.citycode", citycode));
			}
			qry.setProjection(Projections.property("o.sportid"));
			qry.setProjection(Projections.distinct(Projections.property("o.sportid")));
			idList=hibernateTemplate.findByCriteria(qry);
			cacheService.set(CacheConstant.REGION_TWENTYMIN, key, idList);
		}
		return idList;
	}
	@Override
	public List<Long> getNearSportList(double maxLd, double minLd,
			double maxLa, double minLa,Long itemid, String citycode) {
		DetachedCriteria query =this.getNearSportListQuery(maxLd, minLd, maxLa, minLa, citycode);
		query.addOrder(Order.desc("avggeneral"));
		DetachedCriteria subquery = DetachedCriteria.forClass(Sport2Item.class, "t");
		if(itemid != null) subquery.add(Restrictions.eq("t.itemid", itemid));
		subquery.add(Restrictions.eqProperty("t.sportid", "c.id"));
		subquery.setProjection(Projections.property("t.sportid"));
		query.add(Subqueries.exists(subquery));
		query.setProjection(Projections.property("id"));
		List<Long> sportList = hibernateTemplate.findByCriteria(query);
		return sportList;
	}
	
	private DetachedCriteria getNearSportListQuery(double maxLd, double minLd,
			double maxLa, double minLa,String citycode){
		DetachedCriteria query = DetachedCriteria.forClass(Sport.class, "c");
		query.add(Restrictions.ge("c.pointx", ""+minLd));
		query.add(Restrictions.le("c.pointx", ""+maxLd));
		query.add(Restrictions.ge("c.pointy", ""+minLa));
		query.add(Restrictions.le("c.pointy", ""+maxLa));
		query.add(Restrictions.isNotNull("c.pointx"));
		query.add(Restrictions.isNotNull("c.pointy"));
		if(StringUtils.isNotBlank(citycode))query.add(Restrictions.eq("citycode", citycode));
		return query;
	}

	@Override
	public List<Long> getSportList(String type, String name,String countycode,Long subwayid,Long itemid) {
		DetachedCriteria query = DetachedCriteria.forClass(Sport.class, "c");
		if(StringUtils.isNotBlank(name))query.add(Restrictions.like("name", name, MatchMode.ANYWHERE));
		else if(StringUtils.equals("region", type))query.add(Restrictions.eq("countycode", countycode));
		else if(StringUtils.equals("subway", type))query.add(Restrictions.like("lineidlist", subwayid+"", MatchMode.ANYWHERE));
		DetachedCriteria subquery = DetachedCriteria.forClass(Sport2Item.class, "t");
		if(itemid != null) subquery.add(Restrictions.eq("t.itemid", itemid));
		subquery.add(Restrictions.eqProperty("t.sportid", "c.id"));
		subquery.setProjection(Projections.property("t.sportid"));
		query.add(Subqueries.exists(subquery));
		query.setProjection(Projections.property("id"));
		List<Long> sportidList = hibernateTemplate.findByCriteria(query);
		return sportidList;
	}
	@Override
	public List<Long> getSportProfileListByItemId(List<Long> idList,Long itemId){
		DetachedCriteria query = DetachedCriteria.forClass(Sport.class,"s");
		DetachedCriteria subquery = DetachedCriteria.forClass(SportProfile.class,"sp");
		subquery.add(Restrictions.eq("booking", Sport.BOOKING_OPEN));
		subquery.setProjection(Projections.property("id"));
		subquery.add(Restrictions.eqProperty("s.id", "sp.id"));
		if(idList != null && !idList.isEmpty())subquery.add(Restrictions.not(Restrictions.in("sp.id", idList)));
		query.add(Subqueries.exists(subquery));
		DetachedCriteria subitem = DetachedCriteria.forClass(Sport2Item.class, "t");
		subitem.add(Restrictions.eq("t.itemid", itemId));
		subitem.add(Restrictions.eqProperty("t.sportid", "s.id"));
		subitem.setProjection(Projections.property("t.sportid"));
		query.add(Subqueries.exists(subitem));
		query.setProjection(Projections.property("id"));
		List<Long> list = readOnlyTemplate.findByCriteria(query);
		return list;
	}
	@Override
	public List<Long> getSportListByOrder(Long memberid, int from, int maxnum){
		DetachedCriteria query = DetachedCriteria.forClass(SportOrder.class);
		query.add(Restrictions.eq("memberid", memberid));
		query.add(Restrictions.eq("status", OrderConstant.STATUS_PAID_SUCCESS));
		query.addOrder(Order.desc("addtime"));
		List<SportOrder> orderList = this.hibernateTemplate.findByCriteria(query, from, maxnum);
		List<Long> idList = BeanUtil.getBeanPropertyList(orderList, Long.class, "sportid", true);
		return idList;
	}
	
	@Override
	public List<Long> getMemberListByOrder(Long sportid, Timestamp addtime, int from, int maxnum){
		DetachedCriteria query = DetachedCriteria.forClass(SportOrder.class);
		query.add(Restrictions.eq("sportid", sportid));
		query.add(Restrictions.eq("status", OrderConstant.STATUS_PAID_SUCCESS));
		query.addOrder(Order.desc("addtime"));
		List<SportOrder> orderList = this.hibernateTemplate.findByCriteria(query, from, maxnum);
		List<Long> idList = BeanUtil.getBeanPropertyList(orderList, Long.class, "memberid", true);
		return idList;
	}

	@Override
	public List<SportPrice> getPriceList(Long sportid, Long itemid) {
		DetachedCriteria query = DetachedCriteria.forClass(SportPriceTable.class);
		query.add(Restrictions.eq("sportid", sportid));
		query.add(Restrictions.eq("itemid", itemid));
		query.setProjection(Projections.property("id"));
		List<Long> idList =  readOnlyTemplate.findByCriteria(query);
		if(idList.isEmpty())return null;
		query = DetachedCriteria.forClass(SportPrice.class);
		query.add(Restrictions.eq("pricetableid", idList.get(0)));
		List<SportPrice> list = readOnlyTemplate.findByCriteria(query);
		return list;
	}
	
	@Override
	public List<ProgramItemTime> getProgramItemTimeList(Long sportid, Long itemid){
		DetachedCriteria query = DetachedCriteria.forClass(ProgramItemTime.class);
		query.add(Restrictions.eq("sportid", sportid));
		query.add(Restrictions.eq("itemid", itemid));
		query.addOrder(Order.asc("week"));
		List<ProgramItemTime> programItemList = hibernateTemplate.findByCriteria(query);
		return programItemList;
	}
	
	//新版运动改版用
	@Override
	public List<Long> getSportBySportItem(Long itemid, String citycode){
		DetachedCriteria query = DetachedCriteria.forClass(Sport.class, "s");
		DetachedCriteria subquery = DetachedCriteria.forClass(Sport2Item.class, "si");
		subquery.add(Restrictions.eq("si.itemid", itemid));
		subquery.add(Restrictions.eq("si.booking", SportProfile.STATUS_OPEN));
		subquery.add(Restrictions.eqProperty("si.sportid", "s.id"));
		subquery.setProjection(Projections.property("si.sportid"));
		query.add(Subqueries.exists(subquery));
		query.add(Restrictions.eq("s.citycode", citycode));
		query.setProjection(Projections.property("s.id"));
		List<Long> result = readOnlyTemplate.findByCriteria(query);
		return result;
	}
	
	@Override
	public Integer getSportCountBySportItem(Long itemid, String citycode, boolean isopen){
		DetachedCriteria query = DetachedCriteria.forClass(Sport.class, "s");
		query.add(Restrictions.or(Restrictions.ne("s.flag", "H"),Restrictions.isNull("s.flag")));
		DetachedCriteria subquery = DetachedCriteria.forClass(Sport2Item.class, "si");
		subquery.add(Restrictions.eq("si.itemid", itemid));
		if(isopen)subquery.add(Restrictions.eq("si.booking", SportProfile.STATUS_OPEN));
		subquery.add(Restrictions.eqProperty("si.sportid", "s.id"));
		subquery.setProjection(Projections.property("si.sportid"));
		query.add(Subqueries.exists(subquery));
		query.add(Restrictions.eq("s.citycode", citycode));
		query.setProjection(Projections.countDistinct("s.id"));
		List<Long> result = readOnlyTemplate.findByCriteria(query);
		if(result.isEmpty()) return 0;
		return Integer.valueOf(String.valueOf(result.get(0)));
	}

	@Override
	public List<Sport> getSportList(String flag, String key, String citycode,int from, int maxnum) {
		return readOnlyTemplate.findByCriteria(getCriteria(flag, key,citycode, false),from,maxnum);
	}
	@Override
	public Integer getSportCount(String flag, String key,String citycode) {
		List resultList = readOnlyTemplate.findByCriteria(getCriteria(flag, key, citycode, true));
		if(resultList.isEmpty()) return 0;
		return Integer.valueOf(resultList.get(0)+"");
	}
	
	private DetachedCriteria getCriteria(String flag, String key,String citycode, boolean queryCount){
		DetachedCriteria query = DetachedCriteria.forClass(Sport.class);
		query.add(Restrictions.eq("citycode", citycode));
		if(StringUtils.isNotBlank(key)){
			if(ValidateUtil.isNumber(key)){
				query.add(Restrictions.or(Restrictions.ilike("name", key, MatchMode.ANYWHERE)
						,Restrictions.or(Restrictions.ilike("pinyin", key, MatchMode.ANYWHERE)
						,Restrictions.or(Restrictions.eq("id", Long.parseLong(key))
						,Restrictions.or(Restrictions.ilike("countyname", key, MatchMode.ANYWHERE),Restrictions.ilike("address", key ,MatchMode.ANYWHERE))))));
			}else{
				query.add(Restrictions.or(Restrictions.ilike("name", key, MatchMode.ANYWHERE)
						,Restrictions.or(Restrictions.ilike("pinyin", key, MatchMode.ANYWHERE)
						,Restrictions.or(Restrictions.ilike("countyname", key, MatchMode.ANYWHERE),Restrictions.ilike("address", key ,MatchMode.ANYWHERE)))));
			}
		}
		if(StringUtils.isNotBlank(flag)) query.add(Restrictions.isNotNull("flag"));
		if(queryCount){
			query.setProjection(Projections.rowCount());
		}
		query.addOrder(Order.asc("name"));
		return query;
	}

	@Override
	public List<SportProfile> getSportProfileList(String key, String citycode,Long siId, String company, boolean isBooking, int from, int maxnum) {
		 return readOnlyTemplate.findByCriteria(getSportProfile(key, citycode, siId, company, isBooking), from, maxnum);
	}

	@Override
	public Integer getSportProfileCount(String key, String citycode, Long siId, String company, boolean isBooking) {
		List resultList = readOnlyTemplate.findByCriteria(getSportProfile(key, citycode, siId, company, isBooking).setProjection(Projections.rowCount()));
		if(resultList.isEmpty()) return 0;
		return Integer.valueOf(resultList.get(0)+"");
	}
	private DetachedCriteria getSportProfile(String key,String citycode, Long siId, String company, boolean isBooking){
		DetachedCriteria query = DetachedCriteria.forClass(SportProfile.class,"sp");
		if(isBooking){
			query.add(Restrictions.eq("booking", Sport.BOOKING_OPEN));
		}else{
			query.add(Restrictions.eq("booking", Sport.BOOKING_CLOSE));
		} 
		query.add(Restrictions.eq("citycode", citycode));
		if(StringUtils.isNotBlank(company)) query.add(Restrictions.ilike("company", company, MatchMode.ANYWHERE));
		if(StringUtils.isNotBlank(key)){
			DetachedCriteria subquery = DetachedCriteria.forClass(Sport.class,"s");
			subquery.add(Restrictions.eq("citycode", citycode));
			if(ValidateUtil.isNumber(key)){
				subquery.add(Restrictions.or(Restrictions.ilike("name", key, MatchMode.ANYWHERE)
						,Restrictions.or(Restrictions.eq("id", Long.parseLong(key))
						,Restrictions.or(Restrictions.ilike("countyname", key, MatchMode.ANYWHERE),Restrictions.ilike("address", key ,MatchMode.ANYWHERE)))));
			}else{
				subquery.add(Restrictions.or(Restrictions.ilike("name", key, MatchMode.ANYWHERE)
						,Restrictions.or(Restrictions.ilike("countyname", key, MatchMode.ANYWHERE),Restrictions.ilike("address", key ,MatchMode.ANYWHERE))));
			}
			subquery.add(Restrictions.eqProperty("sp.id", "s.id"));
			subquery.setProjection(Projections.property("id"));
			query.add(Subqueries.exists(subquery));
		}
		if(siId != null){
			DetachedCriteria subitem = DetachedCriteria.forClass(Sport2Item.class, "t");
			subitem.add(Restrictions.eq("t.itemid", siId));
			subitem.add(Restrictions.eqProperty("sp.id", "t.sportid"));
			subitem.add(Restrictions.eq("t.booking", Sport.BOOKING_OPEN));
			subitem.setProjection(Projections.property("t.sportid"));
			query.add(Subqueries.exists(subitem));
		}
		query.addOrder(Order.asc("id"));
		return query;
	}
	@Override
	public SportItemPrice getSportItemPriceBySportIdAndItemId(Long sportid, Long itemid, Integer week){
		DetachedCriteria query = DetachedCriteria.forClass(SportItemPrice.class);
		query.add(Restrictions.eq("sportid", sportid));
		query.add(Restrictions.eq("itemid", itemid));
		query.add(Restrictions.eq("week", week));
		List<SportItemPrice> sportItemPriceList = hibernateTemplate.findByCriteria(query);
		if(sportItemPriceList.isEmpty()) return null;
		return sportItemPriceList.get(0);
	}
	@Override
	public List<SportItemPrice> getSportItemPriceListBySportIdAndItemId(Long sportid, Long itemid){
		DetachedCriteria query = DetachedCriteria.forClass(SportItemPrice.class);
		query.add(Restrictions.eq("sportid", sportid));
		query.add(Restrictions.eq("itemid", itemid));
		query.add(Restrictions.eq("status", Status.Y));
		List<SportItemPrice> sportItemPriceList = hibernateTemplate.findByCriteria(query);
		return sportItemPriceList;
	}
	
	@Override
	public List<String> getBookingSportItemList(){
		Timestamp cur = new Timestamp(System.currentTimeMillis());
		Date startdate = DateUtil.getBeginningTimeOfDay(cur);
		DetachedCriteria subQuery = DetachedCriteria.forClass(OpenTimeTable.class, "o");
		subQuery.add(Restrictions.eq("o.status", OpenTimeTableConstant.STATUS_BOOK));
		subQuery.add(Restrictions.eq("o.rstatus", "Y"));
		subQuery.add(Restrictions.le("o.opentime", cur));
		subQuery.add(Restrictions.ge("o.closetime", cur));
		subQuery.add(Restrictions.ge("o.playdate", startdate));
		subQuery.setProjection(Projections.property("o.itemname"));
		List<String> list = readOnlyTemplate.findByCriteria(subQuery);
		return list;
	}
	@Override
	public List<SportItem> getSportItemList(String itemname, Long parentid, String type, String order, int from, int maxnum){
		DetachedCriteria query = itemQuery(itemname, parentid, type);
		if(StringUtils.isNotBlank(order)){
			query.addOrder(Order.desc(order));
		}else{
			query.addOrder(Order.asc("ordernum"));
		}
		return hibernateTemplate.findByCriteria(query, from, maxnum);
	}
	@Override
	public int getSportItemCount(String itemname, Long parentid, String type){
		DetachedCriteria query = itemQuery(itemname, parentid, type);
		query.setProjection(Projections.rowCount());
		List result = hibernateTemplate.findByCriteria(query);
		if(result.isEmpty()) return 0;
		return Integer.parseInt(result.get(0)+"");
	}
	private DetachedCriteria itemQuery(String itemname, Long parentid, String type){
		DetachedCriteria query = DetachedCriteria.forClass(SportItem.class);
		if(StringUtils.isNotBlank(itemname)) query.add(Restrictions.like("itemname", itemname, MatchMode.ANYWHERE));
		if(parentid != null) query.add(Restrictions.eq("parentid", parentid));
		if(StringUtils.isNotBlank(type)) {
			query.add(Restrictions.eq("type", type));
		}
		if(parentid != null) query.add(Restrictions.eq("parentid", parentid));
		return query;
	}

	@Override
	public List<OpenTimeItem> getOrderPlayItemList(Long orderId) {
		DetachedCriteria query = DetachedCriteria.forClass(OpenTimeItem.class,"o");
		DetachedCriteria subquery = DetachedCriteria.forClass(SportOrder2TimeItem.class, "s");
		subquery.add(Restrictions.eq("s.orderid", orderId));
		subquery.add(Restrictions.eqProperty("s.otiid", "o.id"));
		subquery.setProjection(Projections.property("s.otiid"));
		query.add(Subqueries.exists(subquery));
		List<OpenTimeItem> itemIds = hibernateTemplate.findByCriteria(query);
		return itemIds;
	}

	@Override
	public void updateSportItemPrice(Long sportid, Long itemid, Integer minprice, Integer avgprice, Integer maxprice) {
		Sport2Item sport2Item = getSport2Item(sportid, itemid);
		if (sport2Item != null) {
			sport2Item.setAvgprice(avgprice);
			if (minprice == null || minprice < 5) {
				minprice = 5;
			} else
				sport2Item.setMinprice(minprice);
			if (maxprice == null || maxprice < 5) {
				maxprice = 5;
			}
			sport2Item.setMinprice(minprice);
			sport2Item.setMaxprice(maxprice);
			baseDao.saveObject(sport2Item);
		}	
	}
}

