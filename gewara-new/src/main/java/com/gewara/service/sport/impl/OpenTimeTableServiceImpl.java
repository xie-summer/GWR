/**
 * 
 */
package com.gewara.service.sport.impl;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.criterion.Subqueries;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.gewara.constant.OpenTimeItemConstant;
import com.gewara.constant.OpenTimeTableConstant;
import com.gewara.constant.PaymethodConstant;
import com.gewara.constant.sys.CacheConstant;
import com.gewara.constant.sys.ErrorCodeConstant;
import com.gewara.constant.sys.LogTypeConstant;
import com.gewara.constant.sys.MongoData;
import com.gewara.constant.ticket.OpiConstant;
import com.gewara.helper.TimeItemHelper;
import com.gewara.model.acl.User;
import com.gewara.model.sport.OpenTimeItem;
import com.gewara.model.sport.OpenTimeSale;
import com.gewara.model.sport.OpenTimeTable;
import com.gewara.model.sport.ProgramItemTime;
import com.gewara.model.sport.Sport;
import com.gewara.model.sport.Sport2Item;
import com.gewara.model.sport.SportField;
import com.gewara.model.sport.SportItem;
import com.gewara.model.sport.SportProfile;
import com.gewara.mongo.MongoService;
import com.gewara.service.impl.BaseServiceImpl;
import com.gewara.service.sport.OpenTimeTableService;
import com.gewara.service.sport.SportOrderService;
import com.gewara.service.sport.SportService;
import com.gewara.support.ErrorCode;
import com.gewara.support.MultiPropertyComparator;
import com.gewara.untrans.CacheService;
import com.gewara.util.BeanUtil;
import com.gewara.util.BindUtils;
import com.gewara.util.ChangeEntry;
import com.gewara.util.DateUtil;
import com.gewara.util.JsonUtils;
import com.gewara.util.StringUtil;


@Service("openTimeTableService")
public class OpenTimeTableServiceImpl extends BaseServiceImpl implements OpenTimeTableService{
	@Autowired@Qualifier("cacheService")
	private CacheService cacheService;
	public void setCacheService(CacheService cacheService) {
		this.cacheService = cacheService;
	}
	@Autowired@Qualifier("sportOrderService")
	private SportOrderService sportOrderService;
	public void setSportOrderService(SportOrderService sportOrderService) {
		this.sportOrderService = sportOrderService;
	}
	@Autowired@Qualifier("sportService")
	private SportService sportService;
	public void setSportService(SportService sportService) {
		this.sportService = sportService;
	}
	@Autowired@Qualifier("mongoService")
	private MongoService mongoService;
	public void setMongoService(MongoService mongoService){
		this.mongoService = mongoService;
	}
	@Override
	public Integer getOpenTimeTableCount(Long sportid, Long itemid, Date startdate,Date enddate, String openType) {
		Timestamp cur = new Timestamp(System.currentTimeMillis());
		DetachedCriteria qry = DetachedCriteria.forClass(OpenTimeTable.class, "o");
		qry.add(Restrictions.eq("o.sportid", sportid));
		if(itemid != null)qry.add(Restrictions.eq("o.itemid", itemid));
		qry.add(Restrictions.eq("o.status", OpenTimeTableConstant.STATUS_BOOK));
		qry.add(Restrictions.le("o.opentime", cur));
		qry.add(Restrictions.ge("o.closetime", cur));
		if(startdate==null) startdate = DateUtil.getBeginningTimeOfDay(cur);
		qry.add(Restrictions.ge("playdate", startdate));
		if(enddate!=null) qry.add(Restrictions.le("playdate", enddate));
		if(StringUtils.isNotBlank(openType)){
			qry.add(Restrictions.eq("o.openType", openType));
			if(StringUtils.equals(openType, OpenTimeTableConstant.OPEN_TYPE_PERIOD)){
				DetachedCriteria subQuery = DetachedCriteria.forClass(OpenTimeItem.class, "t");
				subQuery.add(Restrictions.ge("t.validtime", cur));
				subQuery.add(Restrictions.eqProperty("t.ottid", "o.id"));
				subQuery.setProjection(Projections.property("t.ottid"));
				qry.add(Subqueries.exists(subQuery));
			}
		}
		qry.setProjection(Projections.rowCount());
		List list = hibernateTemplate.findByCriteria(qry);
		if (list.isEmpty()) return 0;
		return Integer.parseInt(""+list.get(0));
	}
	@Override
	public List<OpenTimeTable> getOpenTimeTableList(Long sportid, Long itemid, Date startdate, Date enddate, String openType) {
		Timestamp cur = new Timestamp(System.currentTimeMillis());
		DetachedCriteria qry = DetachedCriteria.forClass(OpenTimeTable.class, "o");
		if(sportid!=null) qry.add(Restrictions.eq("o.sportid", sportid));
		if(itemid!=null) qry.add(Restrictions.eq("o.itemid", itemid));
		qry.add(Restrictions.eq("o.status", OpenTimeTableConstant.STATUS_BOOK));
		qry.add(Restrictions.le("o.opentime", cur));
		qry.add(Restrictions.ge("o.closetime", cur));
		if(startdate==null) startdate = DateUtil.getBeginningTimeOfDay(new Date());
		qry.add(Restrictions.ge("o.playdate", startdate));
		if(enddate!=null) qry.add(Restrictions.le("o.playdate", enddate));
		if(StringUtils.isNotBlank(openType)){
			qry.add(Restrictions.eq("o.openType", openType));
			if(StringUtils.equals(openType, OpenTimeTableConstant.OPEN_TYPE_PERIOD)){
				DetachedCriteria subQuery = DetachedCriteria.forClass(OpenTimeItem.class, "t");
				subQuery.add(Restrictions.ge("t.validtime", cur));
				subQuery.add(Restrictions.eqProperty("t.ottid", "o.id"));
				subQuery.setProjection(Projections.property("t.ottid"));
				qry.add(Subqueries.exists(subQuery));
			}
		}
		qry.addOrder(Order.asc("o.playdate"));
		List<OpenTimeTable> list = hibernateTemplate.findByCriteria(qry);
		return list;
	}
	
	@Override
	public Integer getbookingSportCount(Date startdate, String citycode) {
		Timestamp cur = new Timestamp(System.currentTimeMillis());
		DetachedCriteria qry = DetachedCriteria.forClass(OpenTimeTable.class, "o");
		qry.add(Restrictions.eq("o.status", OpenTimeTableConstant.STATUS_BOOK));
		qry.add(Restrictions.le("o.opentime", cur));
		qry.add(Restrictions.ge("o.closetime", cur));
		if(startdate==null) startdate = DateUtil.getBeginningTimeOfDay(cur);
		qry.add(Restrictions.ge("o.playdate", startdate));
		qry.add(Restrictions.eq("o.citycode", citycode));
		qry.addOrder(Order.asc("o.playdate"));
		qry.setProjection(Projections.countDistinct("o.sportid"));
		List<Long> list = hibernateTemplate.findByCriteria(qry);
		return Integer.parseInt(""+list.get(0));
	}
	
	@Override
	public List<OpenTimeTable> getOpenTimeTableList(Long sportid, Long itemid, Date startdate, Date enddate, String openType, boolean isBooking, int from, int maxnum) {
		Timestamp cur = new Timestamp(System.currentTimeMillis());
		DetachedCriteria qry = DetachedCriteria.forClass(OpenTimeTable.class, "o");
		if(sportid!=null) qry.add(Restrictions.eq("o.sportid", sportid));
		if(itemid!=null) qry.add(Restrictions.eq("o.itemid", itemid));
		if(isBooking){
			qry.add(Restrictions.eq("o.status", OpenTimeTableConstant.STATUS_BOOK));
		}else{
			qry.add(Restrictions.or(Restrictions.eq("o.status", OpenTimeTableConstant.STATUS_BOOK), Restrictions.eq("o.status", OpenTimeTableConstant.STATUS_NOBOOK)));
		}
		qry.add(Restrictions.le("o.opentime", cur));
		qry.add(Restrictions.ge("o.closetime", cur));
		if(startdate==null) startdate = DateUtil.getBeginningTimeOfDay(new Date());
		qry.add(Restrictions.ge("o.playdate", startdate));
		if(enddate!=null) 
			qry.add(Restrictions.le("o.playdate", enddate));
		if(StringUtils.isNotBlank(openType)){
			qry.add(Restrictions.eq("o.openType", openType));
			if(StringUtils.equals(openType, OpenTimeTableConstant.OPEN_TYPE_PERIOD)){
				DetachedCriteria subQuery = DetachedCriteria.forClass(OpenTimeItem.class, "t");
				subQuery.add(Restrictions.ge("t.validtime", cur));
				subQuery.add(Restrictions.eqProperty("t.ottid", "o.id"));
				subQuery.setProjection(Projections.property("t.ottid"));
				qry.add(Subqueries.exists(subQuery));
			}
		}
		qry.addOrder(Order.asc("o.playdate"));
		List<OpenTimeTable> list = hibernateTemplate.findByCriteria(qry, from, maxnum);
		return list;
	}
	@Override
	public List<String> getOpenTimeTableOpenTypeList(Long sportid, Long itemid, Date startdate, Date enddate, boolean isBooking) {
		DetachedCriteria qry = DetachedCriteria.forClass(OpenTimeTable.class, "o");
		if(sportid!=null) qry.add(Restrictions.eq("o.sportid", sportid));
		if(itemid!=null) qry.add(Restrictions.eq("o.itemid", itemid));
		if(isBooking){
			qry.add(Restrictions.eq("o.status", OpenTimeTableConstant.STATUS_BOOK));
		}else{
			qry.add(Restrictions.or(Restrictions.eq("o.status", OpenTimeTableConstant.STATUS_BOOK), Restrictions.eq("o.status", OpenTimeTableConstant.STATUS_NOBOOK)));
		}
		Timestamp cur = new Timestamp(System.currentTimeMillis());
		qry.add(Restrictions.le("o.opentime", cur));
		qry.add(Restrictions.ge("o.closetime", cur));
		if(startdate==null)
			startdate = DateUtil.getBeginningTimeOfDay(new Date());
		qry.add(Restrictions.ge("o.playdate", startdate));
		if(enddate!=null) 
			qry.add(Restrictions.le("o.playdate", enddate));
		qry.setProjection(Projections.distinct(Projections.property("o.openType")));
		List<String> result = hibernateTemplate.findByCriteria(qry);
		return result;
	}
	@Override
	public List<OpenTimeTable> getOpenTimeTableList(Long sportid, Long itemid, Date playdate, String openType) {
		Timestamp cur = new Timestamp(System.currentTimeMillis());
		DetachedCriteria qry = DetachedCriteria.forClass(OpenTimeTable.class, "o");
		if(sportid!=null) qry.add(Restrictions.eq("o.sportid", sportid));
		if(itemid!=null) qry.add(Restrictions.eq("o.itemid", itemid));
		qry.add(Restrictions.eq("o.status", OpenTimeTableConstant.STATUS_BOOK));
		qry.add(Restrictions.le("o.opentime", cur));
		qry.add(Restrictions.ge("o.closetime", cur));
		qry.add(Restrictions.eq("o.playdate", playdate));
		if(StringUtils.isNotBlank(openType)){
			qry.add(Restrictions.eq("o.openType", openType));
			if(StringUtils.equals(openType, OpenTimeTableConstant.OPEN_TYPE_PERIOD)){
				DetachedCriteria subQuery = DetachedCriteria.forClass(OpenTimeItem.class, "t");
				subQuery.add(Restrictions.ge("t.validtime", cur));
				subQuery.add(Restrictions.eqProperty("t.ottid", "o.id"));
				subQuery.setProjection(Projections.property("t.ottid"));
				qry.add(Subqueries.exists(subQuery));
			}
		}
		qry.addOrder(Order.asc("o.playdate"));
		List<OpenTimeTable> list = hibernateTemplate.findByCriteria(qry);
		return list;
	}
	
	@Override
	public List<OpenTimeTable> getOpenTimeTableList(Long sportid, Long itemid, Date playdate, String openType, int from, int maxnum) {
		Timestamp cur = new Timestamp(System.currentTimeMillis());
		DetachedCriteria qry = DetachedCriteria.forClass(OpenTimeTable.class, "o");
		if(sportid!=null) qry.add(Restrictions.eq("o.sportid", sportid));
		if(itemid!=null) qry.add(Restrictions.eq("o.itemid", itemid));
		qry.add(Restrictions.eq("o.status", OpenTimeTableConstant.STATUS_BOOK));
		qry.add(Restrictions.le("o.opentime", cur));
		qry.add(Restrictions.ge("o.closetime", cur));
		qry.add(Restrictions.eq("o.playdate", playdate));
		if(StringUtils.isNotBlank(openType)){
			qry.add(Restrictions.eq("o.openType", openType));
			if(StringUtils.equals(openType, OpenTimeTableConstant.OPEN_TYPE_PERIOD)){
				DetachedCriteria subQuery = DetachedCriteria.forClass(OpenTimeItem.class, "t");
				subQuery.add(Restrictions.ge("t.validtime", cur));
				subQuery.add(Restrictions.eqProperty("t.ottid", "o.id"));
				subQuery.setProjection(Projections.property("t.ottid"));
				qry.add(Subqueries.exists(subQuery));
			}
		}
		qry.addOrder(Order.asc("o.playdate"));
		List<OpenTimeTable> list = hibernateTemplate.findByCriteria(qry, from, maxnum);
		return list;
	}
	@Override
	public List<Map<Long,Long>> getOpenTimeTableSportList(String citycode,Long itemid, Date startPlaydate,Date endPlaydate) {
		String key = CacheConstant.buildKey("getOpenTimeTableSportLisdtsdafweewre", itemid, startPlaydate,endPlaydate);
		List<Map<Long,Long>> list = (List<Map<Long,Long>>)this.cacheService.get(CacheConstant.REGION_TWENTYMIN, key);
		if(list != null){
			return list;
		}
		Timestamp cur = new Timestamp(System.currentTimeMillis());
		DetachedCriteria qry = DetachedCriteria.forClass(OpenTimeTable.class, "o");
		qry.add(Restrictions.eq("o.itemid", itemid));
		qry.add(Restrictions.eq("o.status", OpenTimeTableConstant.STATUS_BOOK));
		qry.add(Restrictions.le("o.opentime", cur));
		qry.add(Restrictions.ge("o.closetime", cur));
		qry.add(Restrictions.ge("o.playdate", startPlaydate));
		qry.add(Restrictions.le("o.playdate", endPlaydate));
		qry.add(Restrictions.eq("citycode", citycode));
		qry.setProjection(Projections.projectionList().add(Projections.groupProperty("sportid").as("sportid"))
				.add(Projections.sum("quantity").as("sportCount")));
		qry.addOrder(Order.asc("sportCount"));
		qry.setResultTransformer(DetachedCriteria.ALIAS_TO_ENTITY_MAP);
		list = hibernateTemplate.findByCriteria(qry);
		cacheService.set(CacheConstant.REGION_TWENTYMIN, key, list);
		return list;
	}
	
	public List<Integer> getTimeItemPrice(Long ottid) {
		String qry = "select distinct t.price from OpenTimeItem t where t.ottid=? order by t.price";
		List<Integer> priceList = hibernateTemplate.find(qry, ottid);
		return priceList;
	}
	
	public List<Map> getOpenTimeCountByItemid(Long itemid, Long... sportIdArray){
		Date curDate = DateUtil.getCurDate();
		Date cur=DateUtil.addDay(curDate, 14);
		List params = new ArrayList();
		params.add(itemid);
		params.add(curDate);
		params.add(cur);
		params.add(OpenTimeTableConstant.STATUS_BOOK);
		params.add(OpenTimeTableConstant.OPEN_TYPE_FIELD);
		String query="select new map(o.sportid as sportid,sum(o.remain) as remain) from OpenTimeTable o where o.itemid=? and o.playdate>=? and o.playdate<=? and o.status = ? and o.openType=? ";
		if(!ArrayUtils.isEmpty(sportIdArray)){
			int length = ArrayUtils.getLength(sportIdArray);
			if(length == 1){
				query += " and o.sportid = ? ";
			}else {
				query += " and o.sportid in ( ?" + StringUtils.repeat(",?", length- 1) + ") ";
			}
			params.addAll(Arrays.asList(sportIdArray));
		}
		query += " group by o.sportid";
		List<Map> saleslist=hibernateTemplate.find(query,params.toArray());
		//Collections.sort(saleslist, new MultiPropertyComparator(new String[]{"playdate","sportid"}, new boolean[]{true,true}));
		return saleslist;
	}

	public List<Map> getOpenTimeCountByItemid(Long itemid, Date date, Long... sportIdArray){
		List params = new ArrayList();
		params.add(itemid);
		params.add(date);
		params.add(OpenTimeTableConstant.STATUS_BOOK);
		params.add(OpenTimeTableConstant.OPEN_TYPE_FIELD);
		String query="select new map(o.sportid as sportid,o.playdate as playdate, sum(o.remain) as remain) from OpenTimeTable o where o.itemid=? and o.playdate=?  and o.status = ? and  o.openType=? ";
		if(!ArrayUtils.isEmpty(sportIdArray)){
			int length = ArrayUtils.getLength(sportIdArray);
			if(length == 1){
				query += " and o.sportid = ? ";
			}else {
				query += " and o.sportid in ( ?" + StringUtils.repeat(",?", length- 1) + ") ";
			}
			params.addAll(Arrays.asList(sportIdArray));
		}
		query +=" group by o.sportid,o.playdate order by o.playdate"; 
		List<Map> saleslist=hibernateTemplate.find(query,params.toArray());
		return saleslist;
	}
	
	public void addSportTight(Long sportid,Long itemid,String timetemp){
		if(timetemp == null) timetemp = "17:00";
		Map params = new HashMap();
		params.put("sportid",sportid);
		params.put("itemid",itemid);
		params.put("timetemp",timetemp);
		mongoService.removeObjectList(OpenTimeItemConstant.TIGHT_SPORT_TIGHT, params);
		Date date = new Date();
		List<OpenTimeTable> opentimeList = getOpenTimeTableList(sportid, itemid, date,DateUtil.addDay(date, 7), OpenTimeTableConstant.OPEN_TYPE_FIELD);
		for(OpenTimeTable ott : opentimeList){
			int beforeTimeNum = 0;
			int afterTimeNum= 0;
			int beforeTimeCount = 0;
			int afterTimeCount = 0;
			boolean validOver = false;
			List<OpenTimeItem> otiList = getOpenItemList(ott.getId());
			TimeItemHelper tih = new TimeItemHelper(otiList);
			Map<String,OpenTimeItem> otiMap = tih.getOtiMap();
			Sport2Item sport2Item = sportService.getSport2Item(ott.getSportid(), ott.getItemid());
			if(ott.getPlaydate().compareTo(DateUtil.getBeginningTimeOfDay(new Date()))==0) validOver = true;
			List<SportField> sportFieldList = sportOrderService.getSportFieldList(ott.getSportid(), ott.getItemid());
			for(SportField sf : sportFieldList){
				for(String str : tih.getPlayHourList()){
					OpenTimeItem otitem = otiMap.get(sf.getId()+str);
					if(DateUtil.parseDate(str,"HH:mm").before(DateUtil.parseDate(timetemp,"HH:mm"))){
						if(otitem != null){
							if(otitem.hasAvailable() && sport2Item !=null){
								if(validOver && otitem.hasOver(sport2Item.getLimitminutes())){
									beforeTimeCount++;
								}
							}else{
								beforeTimeCount++;
							}
						}
						beforeTimeNum++;
					}else{
						if(otitem != null){
							if(otitem.hasAvailable() && sport2Item !=null){
								if(validOver && otitem.hasOver(sport2Item.getLimitminutes())){
									afterTimeCount++;
								}
							}else{
								afterTimeCount++;
							}
						}
						afterTimeNum++;
					}
				}
			}
			Map sportTightMap = new HashMap();
			sportTightMap.put(OpenTimeItemConstant.TIGHT_SPORT_ID, ott.getSportid());
			sportTightMap.put(OpenTimeItemConstant.TIGHT_ITEM_ID, ott.getItemid());
			sportTightMap.put(OpenTimeItemConstant.TIGHT_PLAY_DATE,ott.getPlaydate());
			sportTightMap.put(MongoData.ACTION_ADDTIME, DateUtil.getCurFullTimestamp());
			sportTightMap.put(OpenTimeItemConstant.TIGHT_BEFORE_TIME_NUM, beforeTimeNum);
			sportTightMap.put(OpenTimeItemConstant.TIGHT_AFTER_TIME_NUM, afterTimeNum);
			sportTightMap.put(OpenTimeItemConstant.TIGHT_BEFORE_TIME_COUNT, beforeTimeCount);
			sportTightMap.put(OpenTimeItemConstant.TIGHT_AFTER_TIME_COUNT, afterTimeCount);
			sportTightMap.put(MongoData.DEFAULT_ID_NAME, System.currentTimeMillis() + StringUtil.getRandomString(5));
			sportTightMap.put(OpenTimeItemConstant.TIGHT_TIME_TEMP, timetemp);
			mongoService.saveOrUpdateMap(sportTightMap, MongoData.DEFAULT_ID_NAME, OpenTimeItemConstant.TIGHT_SPORT_TIGHT);
		}
	}
	
	public float getSportTight(Long sportid,Long itemid,String timetemp){
		String key = sportid + "2012X01A04L13900" + itemid;
		Object cacheTight = cacheService.get(CacheConstant.REGION_TWENTYMIN,key);
		float tight = 0;
		if(cacheTight == null){
			if(timetemp == null) timetemp = "17:00";
			Map params = new HashMap();
			params.put("sportid",sportid);
			params.put("itemid",itemid);
			params.put("timetemp",timetemp);
			List<Map> sportTightList = mongoService.find(OpenTimeItemConstant.TIGHT_SPORT_TIGHT, params);
			int bookingCount = 0;
			int bookingNum = 0;
			for(Map sporttight : sportTightList){
				bookingCount += Integer.valueOf(sporttight.get(OpenTimeItemConstant.TIGHT_AFTER_TIME_COUNT)+"");
				bookingNum += Integer.valueOf(sporttight.get(OpenTimeItemConstant.TIGHT_AFTER_TIME_NUM)+"");
				Date playdate =(Date)sporttight.get(OpenTimeItemConstant.TIGHT_PLAY_DATE);
				if(DateUtil.getCnWeek(playdate).equals("周六") || DateUtil.getCnWeek(playdate).equals("周日")){
					bookingCount += Integer.valueOf(sporttight.get(OpenTimeItemConstant.TIGHT_BEFORE_TIME_COUNT)+"");
					bookingNum += Integer.valueOf(sporttight.get(OpenTimeItemConstant.TIGHT_BEFORE_TIME_NUM)+"");
				}
			}
			tight = (float)(bookingCount)/(float)(bookingNum);
			cacheService.set(CacheConstant.REGION_TWENTYMIN, key, tight);
		}else{
			tight = Float.valueOf(cacheTight.toString());
		}
		return tight;
	}
	@Override
	public ErrorCode<ProgramItemTime> saveOrUpdateProgramItem(Long id, Long sportid, Long itemid, Integer week, String fieldids, Map dataMap, User user,String citycode){
		Sport2Item sport2Item = sportService.getSport2Item(sportid,itemid);
		Sport sport = baseDao.getObject(Sport.class, sportid);
		if(sport == null) return ErrorCode.getFailure("场馆不存在或被删除！");
		SportItem sportItem = baseDao.getObject(SportItem.class, itemid);
		if(sportItem == null) return ErrorCode.getFailure("项目不存在或被删除！");
		if(week == null || week <1 || week >7) return ErrorCode.getFailure("星期错误！");
		List<Long> fieldIdList = BeanUtil.getIdList(fieldids, ",");
		if(fieldIdList.isEmpty()) return ErrorCode.getFailure("场地数据不能为空！");
		ProgramItemTime programItemTime = null;
		if(id != null){
			programItemTime = baseDao.getObject(ProgramItemTime.class, id);
			if(programItemTime == null) return ErrorCode.getFailure("该对象不存在或被删除！");
		}else{
			programItemTime = new ProgramItemTime(sportid, itemid, week, citycode);
		}
		BindUtils.bindData(programItemTime, dataMap);
		if(StringUtils.isBlank(programItemTime.getStarttime()))return ErrorCode.getFailure("开始时间不能为空！");
		if(StringUtils.isBlank(programItemTime.getEndtime())) return ErrorCode.getFailure("结束时间不能为空！");
		if(StringUtils.isBlank(programItemTime.getUnitType())) return ErrorCode.getFailure("计价方式不能为空！");
		if(StringUtils.equals(sport2Item.getOpentype(), OpenTimeTableConstant.OPEN_TYPE_FIELD)){
			if(programItemTime.getUnitMinute()<1 || programItemTime.getUnitMinute()>=1440 || programItemTime.getUnitMinute()%60!=0){
				return ErrorCode.getFailure("开放类型为场地时1-1440之间60的倍数！");
			}
			int diff = Integer.valueOf(programItemTime.getEndtime().substring(0, 2)) - Integer.valueOf(programItemTime.getStarttime().substring(0, 2));
			if(diff <= 0) return ErrorCode.getFailure("结束时间不能小于开始时间！");
			if(diff % (programItemTime.getUnitMinute()/60) != 0) return ErrorCode.getFailure("时间段必须是单位时间的倍数！");
			programItemTime.setUnitType(OpenTimeTableConstant.UNIT_TYPE_WHOLE);
		}else{
			if(StringUtils.equals(sport2Item.getOpentype(), OpenTimeTableConstant.OPEN_TYPE_PERIOD) && StringUtils.equals(programItemTime.getUnitType(), OpenTimeTableConstant.UNIT_TYPE_TIME)){
				if(programItemTime.getUnitMinute() == null) return ErrorCode.getFailure("计价单位时长不能为空！");
				if(programItemTime.getUnitMinute()<1 || programItemTime.getUnitMinute()>=1440 || programItemTime.getUnitMinute()%5!=0)
					return ErrorCode.getFailure("计价单价必需是1-1440之间5的倍数！");
			}else{
				String dateStr = DateUtil.getCurDateStr();
				Date starDate = DateUtil.parseDate(dateStr + " "+ programItemTime.getStarttime() + ":00", "yyyy-MM-dd HH:mm:ss");
				Date endDate = DateUtil.parseDate(dateStr + " "+ programItemTime.getEndtime() + ":00", "yyyy-MM-dd HH:mm:ss");
				int unitMinute = Double.valueOf(DateUtil.getDiffMinu(endDate, starDate)).intValue();
				programItemTime.setUnitMinute(unitMinute);
				programItemTime.setUnitType(OpenTimeTableConstant.UNIT_TYPE_WHOLE);
			}
		}
		if(programItemTime.getPrice() == null) return ErrorCode.getFailure("网站售价不能为空！");
		if(programItemTime.getSportprice() == null) return ErrorCode.getFailure("场馆价不能为空！");
		if(programItemTime.getCostprice() == null) return ErrorCode.getFailure("成本价不能为空！");
		if(!StringUtils.equals(sport2Item.getOpentype(), OpenTimeTableConstant.OPEN_TYPE_FIELD)){
			if(programItemTime.getQuantity() == null) return ErrorCode.getFailure("预约人数不能为空！");
		}
		if(programItemTime.getQuantity() == null) programItemTime.setQuantity(1);
		if(sport2Item.getOpentype() == null){
			programItemTime.setOpenType(sportItem.getOpenType());
		}else{
			programItemTime.setOpenType(sport2Item.getOpentype());
		}
		try{
			List<ProgramItemTime> itemList = new ArrayList<ProgramItemTime>();
			for (Long fieldid : fieldIdList) {
				ProgramItemTime newPit = new ProgramItemTime();
				programItemTime.setFieldid(fieldid);
				PropertyUtils.copyProperties(newPit, programItemTime);
				if(programItemTime.getId()== null){
					programItemTime(newPit, 1);
				}
				itemList.add(newPit);
			}
			if(programItemTime.getId()== null){
				baseDao.saveObjectList(itemList);
			}else{
				baseDao.saveObject(programItemTime);
			}
		}catch (Exception e) {
			dbLogger.error("", e);
			return ErrorCode.getFailure(StringUtil.getExceptionTrace(e, 5));
		}
		return ErrorCode.getSuccessReturn(programItemTime);
	}
	
	@Override
	public ErrorCode<String> batchProgramItemTime(){
		Date curDate = DateUtil.currentTime();
		String hql = "from ProgramItemTime where week=? ";
		List<ProgramItemTime> timeList = hibernateTemplate.find(hql, DateUtil.getWeek(curDate));
		ErrorCode<String> code = null;
		for (ProgramItemTime item : timeList) {
			Sport2Item sport2Item = sportService.getSport2Item(item.getSportid(), item.getItemid());
			int week = 2;
			if(sport2Item != null && StringUtils.equals(sport2Item.getCreatetype(), Sport2Item.RANGE)){
				if(sport2Item.getCycle() != null) week = sport2Item.getCycle();
			}
			code = programItemTime(item, week);
			if(!code.isSuccess()){
				dbLogger.warnWithType(LogTypeConstant.LOG_TYPE_JOB, code.getMsg());
			}
		}
		if(code == null) return ErrorCodeConstant.NOT_FOUND;
		return code;
	}
	
	@Override
	public ErrorCode<String> batchProgramItemTime(Sport2Item sport2Item){
		String hql = "from ProgramItemTime where week=? and sportid=? and itemid=? ";
		Date curDate = DateUtil.currentTime();
		List<ProgramItemTime> timeList = hibernateTemplate.find(hql, DateUtil.getWeek(curDate), sport2Item.getSportid(), sport2Item.getItemid());
		ErrorCode<String> code = null;
		for (ProgramItemTime item : timeList) {
			int week = 2;
			if(sport2Item != null && StringUtils.equals(sport2Item.getCreatetype(), Sport2Item.RANGE)){
				if(sport2Item.getCycle() != null) week = sport2Item.getCycle();
			}
			code = programItemTime(item, week);
			if(!code.isSuccess()){
				dbLogger.warnWithType(LogTypeConstant.LOG_TYPE_JOB, code.getMsg());
			}
		}
		if(code == null) return ErrorCodeConstant.NOT_FOUND;
		return code;
	}
	
	@Override
	public void clearOttPreferential(OpenTimeTable ott, SportProfile sp){
		if(sp.hasPretype(SportProfile.PRETYPE_ENTRUST)){
			ott.setMinpoint(0);
			ott.setMaxpoint(0);
			ott.setElecard("");
			Map<String,String> otherinfoMap = JsonUtils.readJsonToMap(ott.getOtherinfo());
			String payoption = otherinfoMap.get(OpiConstant.PAYOPTION);
			String paymethod = otherinfoMap.get(OpiConstant.PAYCMETHODLIST);
			List<String> paymethodList = new ArrayList<String>();
			if(StringUtils.isNotBlank(paymethod)){
				paymethodList.addAll(Arrays.asList(StringUtils.split(paymethod, ",")));
			}
			if(StringUtils.isNotBlank(payoption)){
				if(StringUtils.equals(payoption, "notuse")){
					if(!paymethodList.contains(PaymethodConstant.PAYMETHOD_GEWAPAY)){
						paymethodList.add(PaymethodConstant.PAYMETHOD_GEWAPAY);
					}
				}else{
					if(paymethodList.contains(PaymethodConstant.PAYMETHOD_GEWAPAY)){
						paymethodList.remove(PaymethodConstant.PAYMETHOD_GEWAPAY);
					}
				}
			}else{
				otherinfoMap.put(OpiConstant.PAYOPTION, "notuse");
				if(!paymethodList.contains(PaymethodConstant.PAYMETHOD_GEWAPAY)){
					paymethodList.add(PaymethodConstant.PAYMETHOD_GEWAPAY);
				}
			}
			otherinfoMap.put(OpiConstant.PAYCMETHODLIST, StringUtils.join(paymethodList, ","));
			ott.setOtherinfo(JsonUtils.writeMapToJson(otherinfoMap));
		}
	}
	
	@Override
	public ErrorCode<String> programItemTime(ProgramItemTime programItemTime, int week){
		Sport sport = baseDao.getObject(Sport.class, programItemTime.getSportid());
		SportItem sportItem = baseDao.getObject(SportItem.class, programItemTime.getItemid());
		Date curDate = DateUtil.getCurDate();
		Date playdate = DateUtil.getBeginningTimeOfDay(DateUtil.addDay(DateUtil.getCurDateByWeek(programItemTime.getWeek()), 1));
		playdate = playdate.before(curDate) ? DateUtil.addDay(playdate, 7) : playdate;
		String query = "from OpenTimeTable o where o.sportid=? and o.itemid=? and o.playdate=? and o.openType=? ";
		SportField sportField = baseDao.getObject(SportField.class, programItemTime.getFieldid());
		for(int i = 1; i <= week; i++){
			if(i>1) playdate = DateUtil.addDay(playdate, (i-1)*7);
			List<OpenTimeTable> ottList = queryByRowsRange(query, 0, 1, programItemTime.getSportid(), programItemTime.getItemid(), playdate, programItemTime.getOpenType());
			OpenTimeTable ott = null;
			SportProfile sp = baseDao.getObject(SportProfile.class, programItemTime.getSportid());
			if(sp == null) return ErrorCode.getFailure("基础数据没有设置完整！");
			if(ottList.isEmpty()){
				ott = new OpenTimeTable(programItemTime, playdate);
				ott.setSportname(sport.getName());
				ott.setItemname(sportItem.getItemname());
				ott.setOpentime(DateUtil.getCurFullTimestamp());
				ott.setClosetime(DateUtil.parseTimestamp(DateUtil.format(playdate, "yyyy-MM-dd") + " 22:00:00"));
				baseDao.saveObject(ott);
				clearOttPreferential(ott, sp);
			}else{
				ott = ottList.get(0);
			}
			ott.setCitycode(programItemTime.getCitycode());
			Sport2Item sport2Item = sportService.getSport2Item(programItemTime.getSportid(), programItemTime.getItemid());
			if(sport2Item == null){
				sport2Item = new Sport2Item(programItemTime.getSportid(), programItemTime.getItemid());
				baseDao.saveObject(sport2Item);
			}
			String hql = "select count(*) from OpenTimeItem i where i.ottid=? and i.hour=? and i.sportid=? and i.itemid=? and i.fieldid=? ";
			List<Long> countList = hibernateTemplate.find(hql, ott.getId(), programItemTime.getStarttime(), programItemTime.getSportid(), programItemTime.getItemid(), programItemTime.getFieldid());
			if(!countList.isEmpty() && countList.get(0)>0){
				if(i == week) return ErrorCode.getFailure("已存在该场次信息！");
				else continue;
			}
			if(ott.hasField()){
				String starttime = programItemTime.getStarttime();
				String endtime = programItemTime.getEndtime();
				Timestamp starTimestamp = ott.getPlayTimeByHour(starttime);
				Timestamp endTimestamp = ott.getPlayTimeByHour(endtime);
				int date = Double.valueOf(DateUtil.getDiffHour(endTimestamp, starTimestamp)).intValue();
				List<OpenTimeItem>  itemList = new ArrayList<OpenTimeItem>();
				Timestamp timestamp = starTimestamp;
				for(int j=0; j<date; j++){
					if(j>0) timestamp = DateUtil.addHour(starTimestamp, j);
					String hour = DateUtil.format(timestamp, "HH:mm");
					String endhour = DateUtil.format(DateUtil.addHour(timestamp, 1), "HH:mm");
					OpenTimeItem openTimeItem = new OpenTimeItem(ott.getId(), programItemTime);
					openTimeItem.setHour(hour);
					openTimeItem.setEndhour(endhour);
					openTimeItem.setFieldname(sportField.getName());
					itemList.add(openTimeItem);
				}
				ott.setQuantity(itemList.size());
				baseDao.saveObject(ott);
				baseDao.saveObjectList(itemList);
			}else{
				OpenTimeItem openTimeItem = new OpenTimeItem(ott.getId(), programItemTime);
				int quantity = ott.getQuantity();
				ott.setQuantity(quantity + openTimeItem.getQuantity());
				openTimeItem.setFieldname(sportField.getName());
				Timestamp playtime = ott.getPlayTimeByHour(openTimeItem.getEndhour());
				int minutes = 30;
				if(openTimeItem.hasUnitTime()){
					minutes = openTimeItem.getUnitMinute();
				}
				Timestamp validtime = DateUtil.addMinute(playtime, -minutes);
				openTimeItem.setValidtime(validtime);
				baseDao.saveObjectList(ott, openTimeItem);
			}
		}
		return ErrorCode.SUCCESS;
	}
	
	@Override
	public List<Long> getCurOttSportIdList(Long itemid, String citycode){
		String key = CacheConstant.buildKey("get12CurOtt20SportIdList", itemid, citycode);
		List<Long> idList = (List<Long>) cacheService.get(CacheConstant.REGION_TENMIN, key);
		if(idList == null){
			Timestamp cur = DateUtil.getCurFullTimestamp();
			Date curDate = DateUtil.getBeginningTimeOfDay(DateUtil.getDateFromTimestamp(cur));
			List params = new ArrayList();
			params.add(cur);
			params.add(cur);
			params.add(OpenTimeTableConstant.STATUS_BOOK);
			params.add(curDate);
			params.add(citycode);
			String query = "select distinct o.sportid as playdate from OpenTimeTable o " +
			" where o.opentime<? and o.closetime>? and o.status=? and o.playdate >=? and o.citycode=?";
			if(itemid != null){
				query += " and o.itemid=? ";
				params.add(itemid);
			}
			idList = hibernateTemplate.find(query, params.toArray());
			cacheService.set(CacheConstant.REGION_TENMIN, key, idList);
		}
		return idList;
	}
	@Override
	public List<OpenTimeItem> getOpenItemList(Long ottid) {
		List<OpenTimeItem> otiList = baseDao.getObjectListByField(OpenTimeItem.class, "ottid", ottid);
		return otiList;
	}
	@Override
	public List<OpenTimeTable> getOttList(Long sportid, Long itemid,
			String playdate) {
		DetachedCriteria qry = DetachedCriteria.forClass(OpenTimeTable.class, "o");
		if(sportid!=null) qry.add(Restrictions.eq("o.sportid", sportid));
		if(itemid!=null) qry.add(Restrictions.eq("o.itemid", itemid));
		qry.add(Restrictions.eq("o.playdate", DateUtil.parseDate(playdate)));
		qry.addOrder(Order.asc("o.playdate"));
		List<OpenTimeTable> list = hibernateTemplate.findByCriteria(qry);
		return list;
	}
	
	@Override
	public void updateOpenTimeTable(OpenTimeTable ott){
		//说明：这个两个更新语句中的字段没有在映射对象，缓存不会影响
		String logstr = "更新场次统计:sportid:" + ott.getSportid() + ", playdate:" + DateUtil.formatDate(ott.getPlaydate());
		int week = DateUtil.getWeek(ott.getPlaydate());
		if(week==6 || week==7){
			String sql = "update WEBDATA.open_timetable o set o.psales=o.sales where psales=0 and o.recordid=?";
			int count = jdbcTemplate.update(sql, ott.getId());
			dbLogger.warn(logstr + "-->psales：" + count);
		}else {
			String sql = "update WEBDATA.open_timetable o set o.psales=(select count(*) from WEBDATA.open_timeitem i where i.ottid=o.recordid and i.status=? and i.hour>='18:00' and i.hour<='23:50') where psales=0 and o.recordid=?";
			int count = jdbcTemplate.update(sql, OpenTimeItemConstant.STATUS_SOLD, ott.getId());
			dbLogger.warn(logstr + "-->psales：" + count);
		}
	}
	
	@Override
	public void updateOttOtherData(OpenTimeTable ott){
		String logstr = "更新场次统计:sportid:" + ott.getSportid() + ", playdate:" + DateUtil.formatDate(ott.getPlaydate());
		//总场地数量
		String sql = "update WEBDATA.open_timetable o set o.fields=(select count(*) from WEBDATA.open_timeitem i where i.ottid=o.recordid and i.status!=?) where o.fields=0 and o.recordid=?";
		int count = jdbcTemplate.update(sql, OpenTimeItemConstant.STATUS_DELETE, ott.getId());
		dbLogger.warn(logstr + "-->fields：" + count);
		//格瓦拉可卖的场地
		sql = "update WEBDATA.open_timetable o set o.canasales=(select count(*) from WEBDATA.open_timeitem i where i.ottid=o.recordid and i.status=?) where canasales=0 and o.recordid=?";
		count = jdbcTemplate.update(sql, OpenTimeItemConstant.STATUS_NEW, ott.getId());
		dbLogger.warn(logstr + "-->canasales：" + count);
		//格瓦拉可卖的黄金场地
		int week = DateUtil.getWeek(ott.getPlaydate());
		if(week==6 || week==7){
			sql = "update WEBDATA.open_timetable o set o.canpsales=o.canasales where canpsales=0 and o.recordid=?";
			count = jdbcTemplate.update(sql, ott.getId());
			dbLogger.warn(logstr + "-->canpsales：" + count);
		}else {
			sql = "update WEBDATA.open_timetable o set o.canpsales=(select count(*) from WEBDATA.open_timeitem i where i.ottid=o.recordid and i.status=? and i.hour>='18:00' and i.hour<='23:50') where canpsales=0 and o.recordid=?";
			count = jdbcTemplate.update(sql, OpenTimeItemConstant.STATUS_NEW, ott.getId());
			dbLogger.warn(logstr + "-->canpsales：" + count);
		}
		//所有的黄金场地
		if(week==6 || week==7){
			sql = "update WEBDATA.open_timetable o set o.primetime=o.fields where o.primetime=0 and o.recordid=?";
			count = jdbcTemplate.update(sql, ott.getId());
			dbLogger.warn(logstr + "-->primetime：" + count);
		}else {
			sql = "update WEBDATA.open_timetable o set o.primetime=(select count(*) from WEBDATA.open_timeitem i where i.ottid=o.recordid and i.hour>='18:00' and i.hour<='23:50') where o.primetime=0 and o.recordid=?";
			count = jdbcTemplate.update(sql, ott.getId());
			dbLogger.warn(logstr + "-->primetime：" + count);
		}
	}
	@Override
	public void refreshOpenTimeSale(List<OpenTimeItem> itemList, Integer dupprice, List<String> msgList) {
		if(CollectionUtils.isEmpty(itemList)){
			msgList.add("场地数据为空！");
			return;
		}
		Collections.sort(itemList, new MultiPropertyComparator(new String[]{"hour"}, new boolean[]{true}));
		List<Long> ottidList = new ArrayList();
		Long otsid = null;
		int lowerprice = 0, auctionprice = 0;
		List<Long> idList = new ArrayList();
		String starttime = null, endtime = null, bindInd = null;
		Long fieldid = null;
		for (int i= 0 ; i< itemList.size(); i++) {
			OpenTimeItem oti = itemList.get(i);
			if(i == 0){
				starttime = oti.getHour();
				bindInd = oti.getSaleInd();
				fieldid = oti.getFieldid();
			}
			endtime = oti.getEndhour();
			if(StringUtils.isNotBlank(bindInd)){
				if(!StringUtils.equals(bindInd, oti.getSaleInd())){
					msgList.add("场地有不是同一组竞价数据！");
					return;
				}
			}
			idList.add(oti.getId());
			if(!StringUtils.equals(oti.getItemtype(), OpenTimeTableConstant.ITEM_TYPE_VIE)){
				msgList.add("ottid:" +oti.getOttid() + "starttime:" + oti.getHour() + "非竞拍场次！");
				return;
			}
			if(!ottidList.contains(oti.getOttid())){
				ottidList.add(oti.getOttid());
			}
			otsid = oti.getOtsid();
			lowerprice += oti.getPrice();
			auctionprice = oti.getAuctionprice();
			
		}
		OpenTimeTable ott = baseDao.getObject(OpenTimeTable.class, ottidList.get(0));
		if(lowerprice == 0 || auctionprice == 0){
			msgList.add("场次：" + DateUtil.formatDate(ott.getPlaydate()) + "价格有为 0数据！");
			return;
		}
		if(ottidList.size()!=1){
			msgList.add("场地不能跨场次！");
			return;
		}
		OpenTimeSale openTimeSale = null;
		if(otsid == null){
			openTimeSale = new OpenTimeSale(ott, lowerprice, dupprice, auctionprice);
			
			openTimeSale.setCitycode(ott.getCitycode());
		}else{
			openTimeSale = baseDao.getObject(OpenTimeSale.class, otsid);
			if(openTimeSale == null){
				msgList.add("数据不存在！");
				return;
			}
		}
		ChangeEntry changeEntry = new ChangeEntry(openTimeSale);
		openTimeSale.setFieldid(fieldid);
		openTimeSale.setOttid(ott.getId());
		openTimeSale.setSportid(ott.getSportid());
		openTimeSale.setItemid(ott.getItemid());
		openTimeSale.setOtiids(StringUtils.join(idList, ","));
		openTimeSale.setStarttime(starttime);
		Timestamp closetime = ott.getPlayTimeByHour(starttime);
		openTimeSale.setClosetime(DateUtil.addDay(closetime, -7));
		openTimeSale.setValidtime(closetime);
		openTimeSale.setBindInd(bindInd);
		openTimeSale.setEndtime(endtime);
		baseDao.saveObject(openTimeSale);
		if(!changeEntry.getChangeMap(openTimeSale).isEmpty()){
			msgList.add("场次：" + DateUtil.formatDate(ott.getPlaydate()) + "," + BeanUtil.buildString(openTimeSale, true));
		}
		for (OpenTimeItem oti : itemList) {
			oti.setOtsid(openTimeSale.getId());
		}
		baseDao.saveObjectList(itemList);
	}
}
