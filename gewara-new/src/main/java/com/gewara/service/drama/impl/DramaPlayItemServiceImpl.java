package com.gewara.service.drama.impl;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.hibernate.criterion.Conjunction;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.ProjectionList;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.criterion.Subqueries;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.gewara.constant.OdiConstant;
import com.gewara.constant.Status;
import com.gewara.constant.TagConstant;
import com.gewara.constant.sys.CacheConstant;
import com.gewara.model.drama.DisQuantity;
import com.gewara.model.drama.Drama;
import com.gewara.model.drama.DramaPlayItem;
import com.gewara.model.drama.OpenDramaItem;
import com.gewara.model.drama.Theatre;
import com.gewara.model.drama.TheatreField;
import com.gewara.model.drama.TheatreRoom;
import com.gewara.model.drama.TheatreSeatArea;
import com.gewara.model.drama.TheatreSeatPrice;
import com.gewara.service.drama.DramaPlayItemService;
import com.gewara.service.impl.BaseServiceImpl;
import com.gewara.support.ErrorCode;
import com.gewara.support.MultiPropertyComparator;
import com.gewara.support.ReadOnlyTemplate;
import com.gewara.untrans.AsynchTask;
import com.gewara.untrans.AsynchTaskProcessor;
import com.gewara.untrans.AsynchTaskService;
import com.gewara.untrans.CacheService;
import com.gewara.untrans.monitor.MonitorService;
import com.gewara.util.BeanUtil;
import com.gewara.util.ChangeEntry;
import com.gewara.util.DateUtil;
import com.gewara.util.StringUtil;

@Service("dramaPlayItemService")
public class DramaPlayItemServiceImpl extends BaseServiceImpl implements DramaPlayItemService, InitializingBean {
	@Autowired@Qualifier("cacheService")
	private CacheService cacheService;
	
	@Autowired@Qualifier("monitorService")
	private MonitorService monitorService;
	
	@Autowired@Qualifier("readOnlyTemplate")
	private ReadOnlyTemplate readOnlyTemplate;
	
	@Autowired@Qualifier("asynchTaskService4Job")
	private AsynchTaskService asynchTaskService;
	
	@Override
	public Drama getDramaByName(String dramaName){
		List<Long> movieidList = hibernateTemplate.find("select id from Drama m where m.dramaname = ?",dramaName);
		if(movieidList.size()>0) return baseDao.getObject(Drama.class, movieidList.get(0));
		return null;
	}
	
	@Override
	public List<Drama> getDramaListByName(String dramaName) {
		List<Long> dramaidList = hibernateTemplate.find("select id from Drama m where m.dramaname like ?", "%" + dramaName + "%");
		return baseDao.getObjectList(Drama.class, dramaidList);
	}
	
	@Override
	public ErrorCode saveDramaPlayItem(DramaPlayItem item, Long userid) {
		if(userid == null) return ErrorCode.getFailure("请先登录！");
		if(item == null) return ErrorCode.getFailure("保存对象不能为空！");
		if(!item.hasGewa()) return ErrorCode.getFailure("非格瓦拉排期，不能修改！");
		if(item.getPlaytime() == null) return ErrorCode.getFailure("开演时间不能为空！");
		if(!item.hasPeriod(Status.Y)){
			if(StringUtils.isBlank(StringUtils.trim(item.getName()))){
				return ErrorCode.getFailure("非固定时间演出名称不能为空！");
			}
			if(item.getEndtime() == null){
				return ErrorCode.getFailure("非固定时间演出结束时间不能为空！");
			}
			item.setOpentype(OdiConstant.OPEN_TYPE_PRICE);
		}else{
			if(item.getEndtime() == null){
				item.setEndtime(DateUtil.addDay(item.getPlaytime(), 1));
			}
		}
		ChangeEntry changeEntry = new ChangeEntry(item);
		if(item.getId()!=null) {
			OpenDramaItem openDramaItem = baseDao.getObjectByUkey(OpenDramaItem.class, "dpid", item.getId(), false);
			if(openDramaItem != null){
				ChangeEntry changeEntry2 = new ChangeEntry(openDramaItem);
				openDramaItem.setPlaytime(item.getPlaytime());
				openDramaItem.setName(item.getName());
				openDramaItem.setPeriod(item.getPeriod());
				openDramaItem.setEndtime(item.getEndtime());
				monitorService.saveChangeLog(userid, OpenDramaItem.class, openDramaItem.getId(), changeEntry2.getChangeMap(openDramaItem));
				baseDao.saveObject(openDramaItem);
			}
		}
		TheatreField field = baseDao.getObject(TheatreField.class, item.getRoomid());
		item.setRoomname(field.getName());
		Drama drama = baseDao.getObject(Drama.class, item.getDramaid());
		Theatre theatre = baseDao.getObject(Theatre.class, item.getTheatreid());
		item.setCitycode(theatre.getCitycode());
		item.setTheatrename(theatre.getRealBriefname());
		item.setDramaname(drama.getRealBriefname());
		baseDao.saveObject(item);
		monitorService.saveChangeLog(userid,DramaPlayItem.class, item.getId(),changeEntry.getChangeMap(item));
		return ErrorCode.getSuccessReturn(item);
	}
	@Override
	public ErrorCode removieDramaPlayItem(Long id) {
		DramaPlayItem item = baseDao.getObject(DramaPlayItem.class, id);
		OpenDramaItem open = baseDao.getObjectByUkey(OpenDramaItem.class, "dpid", id, false);
		if(open!=null && open.isBooking())  return ErrorCode.getFailure("本场次正在开放预定，不能删除");
		item.setStatus(DramaPlayItem.STATUS_N);
		baseDao.saveObject(item);
		if(open != null){
			open.setStatus(OdiConstant.STATUS_DISCARD);
			baseDao.saveObject(open);
		}
		return ErrorCode.SUCCESS;
	}
	@Override
	public List<DisQuantity> getDisQuantityList(Long tspid){
		List<DisQuantity> disList = baseDao.getObjectListByField(DisQuantity.class, "tspid", tspid);
		return disList;
	}
	@Override
	public List<DisQuantity> getDisQuantityListByDpid(Long dpid){
		String qry = "from DisQuantity d where exists(select t.id from TheatreSeatPrice t where t.id=d.tspid and t.status <> 'D' and t.dpid=?)";
		List<DisQuantity> disList = hibernateTemplate.find(qry, dpid);
		return disList;
	}
	@Override
	public ErrorCode saveDramaPlayItem(DramaPlayItem item, String playdates, String rooms){
		if(item==null) return ErrorCode.getFailure("该场次不存在");
		if(!item.hasSeller(OdiConstant.PARTNER_GEWA)) return ErrorCode.getFailure("非格瓦拉场次不能复制！");
		List<String> dateList = Arrays.asList(StringUtils.split(playdates, ","));
		List<Long> roomidList = BeanUtil.getIdList(rooms, ",");
		List<TheatreField> fieldList = baseDao.getObjectList(TheatreField.class, roomidList);
		Timestamp cur = DateUtil.getCurFullTimestamp();
		try {
			List<TheatreSeatArea> seatAreaList = baseDao.getObjectListByField(TheatreSeatArea.class, "dpid", item.getId());
			List<TheatreSeatPrice> tspList = getTspList(item.getId(), null);
			Map<Long, List<TheatreSeatPrice>> tspMap = BeanUtil.groupBeanList(tspList, "areaid");
			for(String date : dateList){
				Timestamp newplaytime = DateUtil.parseTimestamp(date + " " + DateUtil.format(item.getPlaytime(), "HH:mm:ss"));
				for(TheatreField theatreField : fieldList){
					DramaPlayItem newItem = new DramaPlayItem(cur);
					PropertyUtils.copyProperties(newItem, item);
					newItem.setId(null);
					newItem.setPlaytime(newplaytime);
					newItem.setRoomid(theatreField.getId());
					newItem.setRoomname(theatreField.getName());
					baseDao.saveObject(newItem);
					for (TheatreSeatArea seatArea : seatAreaList) {
						if(seatArea.hasStatus(Status.Y)){
							TheatreSeatArea area = new TheatreSeatArea(newItem.getId());
							PropertyUtils.copyProperties(area, seatArea);
							area.setId(null);
							area.setDpid(newItem.getId());
							baseDao.saveObject(area);
							List<TheatreSeatPrice> tspList2 = tspMap.get(seatArea.getId());
							if(!CollectionUtils.isEmpty(tspList2)){
								for(TheatreSeatPrice tsp : tspList2){
									TheatreSeatPrice sp = new TheatreSeatPrice();
									PropertyUtils.copyProperties(sp, tsp);
									sp.setId(null);
									sp.setDpid(newItem.getId());
									sp.setAreaid(area.getId());
									baseDao.saveObject(sp);
								}
							}
						}
					}
				}
			}
		}  catch (Exception e) {
			dbLogger.error("", e);
			return ErrorCode.getFailure(StringUtil.getExceptionTrace(e, 5));
		}
		
		return ErrorCode.SUCCESS;
	}
	@Override
	public List<Map> getDateCount(Long theatreid, Timestamp starttime) {
		String qry = "select new map(to_char(o.playtime, 'yyyy-MM') as playdate, count(*) as count) ";
				qry = qry + "from DramaPlayItem o where o.theatreid=? and o.playtime>=? and o.status like ? ";
				qry = qry + "group by to_char(o.playtime,'yyyy-MM') ";
				qry = qry + "order by to_char(o.playtime,'yyyy-MM')";
		List<Map> dateMapList = hibernateTemplate.find(qry, theatreid, starttime, DramaPlayItem.STATUS_Y+"%");
		return dateMapList;
	}
	@Override
	public List<DramaPlayItem> getDramaPlayItemList(String citycode, Long theatreid,
			Long dramaid, Timestamp playstart, Timestamp playend, Boolean isPartner){
		return getDramaPlayItemList(citycode, theatreid, dramaid, null, playstart, playend, isPartner);
	}
	@Override
	public List<DramaPlayItem> getDramaPlayItemList(String citycode, Long theatreid,
			Long dramaid, Long starid, Timestamp playstart, Timestamp playend, Boolean isPartner){
		return getDramaPlayItemList(citycode, theatreid, dramaid, starid, playstart, playend, isPartner, true);
	}
	@Override
	public List<DramaPlayItem> getDramaPlayItemList(String citycode, Long theatreid,
			Long dramaid, Timestamp playstart, Timestamp playend, Boolean isPartner, Boolean isValidEndtime) {
		return getDramaPlayItemList(citycode, theatreid, dramaid, null, playstart, playend, isPartner, isValidEndtime);
	}
	@Override
	public List<DramaPlayItem> getDramaPlayItemList(String citycode, Long theatreid,
			Long dramaid, Long starid, Timestamp playstart, Timestamp playend, Boolean isPartner, Boolean isValidEndtime) {
		DetachedCriteria qry = DetachedCriteria.forClass(DramaPlayItem.class);
		if(theatreid!=null) qry.add(Restrictions.eq("theatreid", theatreid));
		if(dramaid!=null) qry.add(Restrictions.eq("dramaid", dramaid));
		if(starid!=null) qry.add(Restrictions.eq("dramaStarId", starid));
		if(StringUtils.isNotBlank(citycode))qry.add(Restrictions.eq("citycode", citycode));
		if(playstart != null && playend!=null){
			Conjunction con1 = Restrictions.conjunction();
			con1.add(Restrictions.ge("playtime", playstart));
			con1.add(Restrictions.le("playtime", playend));
			if(isValidEndtime){
				con1.add(Restrictions.eq("period", Status.Y));
				Conjunction con2 = Restrictions.conjunction();
				con2.add(Restrictions.ge("endtime", playstart));
				con2.add(Restrictions.le("endtime", playend));
				con2.add(Restrictions.eq("period", Status.N));
				qry.add(Restrictions.or(con1, con2));
			}else {
				qry.add(con1);
			}
		}else if(playstart != null && playend == null){
			Conjunction con1 = Restrictions.conjunction();
			con1.add(Restrictions.ge("playtime", playstart));
			con1.add(Restrictions.eq("period", Status.Y));
			Conjunction con2 = Restrictions.conjunction();
			con2.add(Restrictions.ge("endtime", playstart));
			con2.add(Restrictions.eq("period", Status.N));
			qry.add(Restrictions.or(con1, con2));
		}else if(playend!=null && playstart == null){
			Conjunction con1 = Restrictions.conjunction();
			con1.add(Restrictions.le("playtime", playend));
			con1.add(Restrictions.eq("period", Status.Y));
			Conjunction con2 = Restrictions.conjunction();
			con2.add(Restrictions.le("endtime", playend));
			con2.add(Restrictions.eq("period", Status.N));
			qry.add(Restrictions.or(con1, con2));
		}
		if(isPartner != null){
			if(isPartner){ 
				qry.add(Restrictions.like("status", DramaPlayItem.STATUS_Y, MatchMode.START));
				qry.add(Restrictions.eq("partner", Status.Y));
			}else{
				qry.add(Restrictions.eq("status", DramaPlayItem.STATUS_Y));
			}
		}else{
			qry.add(Restrictions.eq("status", DramaPlayItem.STATUS_Y));
		}
		qry.addOrder(Order.asc("sortnum"));
		qry.addOrder(Order.asc("playtime"));
		qry.addOrder(Order.asc("id"));
		List<DramaPlayItem> playList = hibernateTemplate.findByCriteria(qry);
		return playList;
	}
	@Override
	public List<DramaPlayItem> getUnOpenDramaPlayItemList(String citycode, Long theatreid,
			Long dramaid, Timestamp playstart, Timestamp playend, int maxnum) {
		return getUnOpenDramaPlayItemList(citycode, theatreid, dramaid, null, playstart, playend, maxnum);
	}
	@Override
	public List<DramaPlayItem> getUnOpenDramaPlayItemList(String citycode, Long theatreid,
			Long dramaid, Long starid, Timestamp playstart, Timestamp playend, int maxnum) {
		DetachedCriteria qry = DetachedCriteria.forClass(DramaPlayItem.class, "d");
		if(theatreid!=null) qry.add(Restrictions.eq("d.theatreid", theatreid));
		if(dramaid!=null) qry.add(Restrictions.eq("d.dramaid", dramaid));
		if(starid!=null) qry.add(Restrictions.eq("d.dramaStarId", starid));
		if(StringUtils.isNotBlank(citycode)) qry.add(Restrictions.eq("citycode", citycode));
		if(playstart != null && playend!=null){
			Conjunction con1 = Restrictions.conjunction();
			con1.add(Restrictions.ge("playtime", playstart));
			con1.add(Restrictions.le("playtime", playend));
			con1.add(Restrictions.eq("period", Status.Y));
			Conjunction con2 = Restrictions.conjunction();
			con2.add(Restrictions.ge("endtime", playstart));
			con2.add(Restrictions.le("endtime", playend));
			con2.add(Restrictions.eq("period", Status.N));
			qry.add(Restrictions.or(con1, con2));
		}else if(playstart != null && playend == null){
			Conjunction con1 = Restrictions.conjunction();
			con1.add(Restrictions.ge("playtime", playstart));
			con1.add(Restrictions.eq("period", Status.Y));
			Conjunction con2 = Restrictions.conjunction();
			con2.add(Restrictions.ge("endtime", playstart));
			con2.add(Restrictions.eq("period", Status.N));
			qry.add(Restrictions.or(con1, con2));
		}else if(playend!=null && playstart == null){
			Conjunction con1 = Restrictions.conjunction();
			con1.add(Restrictions.le("playtime", playend));
			con1.add(Restrictions.eq("period", Status.Y));
			Conjunction con2 = Restrictions.conjunction();
			con2.add(Restrictions.le("endtime", playend));
			con2.add(Restrictions.eq("period", Status.N));
			qry.add(Restrictions.or(con1, con2));
		}
		qry.add(Restrictions.eq("d.status", DramaPlayItem.STATUS_Y));
		
		DetachedCriteria sub = DetachedCriteria.forClass(OpenDramaItem.class, "o");
		sub.add(Restrictions.eqProperty("o.dpid", "d.id"));
		sub.setProjection(Projections.property("o.id"));
		qry.add(Subqueries.notExists(sub));
		qry.addOrder(Order.asc("d.sortnum"));
		qry.addOrder(Order.asc("d.playtime"));
		List<DramaPlayItem> playList = hibernateTemplate.findByCriteria(qry, 0, maxnum);
		return playList;
	}
	@Override
	public void initDramaPlayItem(DramaPlayItem item){
		if(item==null) return;
		Theatre theatre = baseDao.getObject(Theatre.class, item.getTheatreid());
		item.setRelate1(theatre);
		Drama drama = baseDao.getObject(Drama.class, item.getDramaid());
		item.setRelate2(drama);
	}
	@Override
	public void initDramaPlayItemList(List<DramaPlayItem> itemList){
		if(itemList==null || itemList.isEmpty()) return;
		for(DramaPlayItem item : itemList){
			initDramaPlayItem(item);
		}
	}
	@Override
	public List<TheatreRoom> getRoomList(Long theatreid){
		String qry = "from TheatreRoom r where r.theatreid=? order by r.num";
		List<TheatreRoom> roomList = hibernateTemplate.find(qry, theatreid);
		return roomList;
	}
	
	@Override
	public TheatreField getTheatreFieldByName(Long theatreid, String name){
		String qry = "from TheatreField r where r.theatreid=? and r.roomname=? order by r.num";
		List<TheatreField> roomList = queryByRowsRange(qry, 0, 1, theatreid, name);
		if(roomList.isEmpty()) return null;
		return roomList.get(0);
	}
	
	@Override
	public TheatreRoom getTheatreRoomByNum(Long theatreid, String fieldnum, String roomnum){
		String qry = "from TheatreRoom r where r.theatreid=? and r.num=? and exists(select f.id from TheatreField f where f.id=r.fieldid and f.theatreid=? and f.fieldnum=?) order by r.num";
		List<TheatreRoom> roomList = queryByRowsRange(qry, 0, 1, theatreid, roomnum, theatreid, fieldnum);
		if(roomList.isEmpty()) return null;
		return roomList.get(0);
	}
	
	@Override
	public TheatreSeatPrice getTsp(Long dpid, Long areaid, Integer price, String seattype) {
		String qry = "from TheatreSeatPrice t where t.dpid=? and t.areaid=? and t.price=? and (t.status <> 'D' or t.status is null) and t.seattype=?";
		List<TheatreSeatPrice> tspList = hibernateTemplate.find(qry, dpid, areaid, price, seattype);
		if(tspList.size()==0) return null;
		return tspList.get(0);
	}
	
	@Override
	public List<TheatreSeatPrice> getTspList(Long dpid){
		return getTspList(dpid, null);
	}
	
	@Override
	public List<TheatreSeatPrice> getTspList(Long dpid, Long areaid) {
		String qry = "from TheatreSeatPrice t where t.dpid=? ";
		List params = new ArrayList();
		params.add(dpid);
		if(areaid != null){
			params.add(areaid);
			qry += " and t.areaid=? ";
		}else{
			qry += " and exists(select a.id from TheatreSeatArea a where a.id=t.areaid and a.status='Y' )";
		}
		 qry += " and (t.status <> 'D' or t.status is null) order by t.seattype ";
		List<TheatreSeatPrice> tspList = hibernateTemplate.find(qry, params.toArray());
		return tspList;
	}
	@Override
	public List<Integer> getPriceList(Long theatreid, Long dramaid, Timestamp starttime, Timestamp endtime, boolean isBooking){
		List<Integer> seatPriceList = getTheatrePriceList(theatreid, dramaid, starttime, endtime, isBooking);
		Collections.sort(seatPriceList);
		return seatPriceList;
	}

	private List<Integer> getTheatrePriceList(Long theatreid, Long dramaid, Timestamp starttime, Timestamp endtime, boolean isBooking){
		String key = CacheConstant.buildKey("getTheatrePriceList235", theatreid, dramaid, starttime, endtime, isBooking);
		List<Integer> priceList = (List<Integer>) cacheService.get(CacheConstant.REGION_ONEMIN, key);
		if(CollectionUtils.isEmpty(priceList)){
			DetachedCriteria query = DetachedCriteria.forClass(TheatreSeatPrice.class, "t");
			query.add(Restrictions.gt("t.price", 1));
			query.add(Restrictions.ne("t.status", Status.DEL));
			Class clazz = null;
			if(isBooking) clazz = OpenDramaItem.class;
			else clazz = DramaPlayItem.class;
			DetachedCriteria sub = DetachedCriteria.forClass(clazz, "i");
			if(theatreid!=null) sub.add(Restrictions.eq("i.theatreid", theatreid));		
			if(dramaid!=null) sub.add(Restrictions.eq("i.dramaid", dramaid));
			if(starttime == null ) starttime = DateUtil.getCurFullTimestamp();
			Conjunction con1 = Restrictions.conjunction();
			con1.add(Restrictions.ge("i.playtime", starttime));
			con1.add(Restrictions.eq("i.period", Status.Y));
			Conjunction con2 = Restrictions.conjunction();
			con2.add(Restrictions.ge("i.endtime", starttime));
			con2.add(Restrictions.eq("i.period", Status.N));
			if(endtime!=null){
				con1.add(Restrictions.le("i.playtime", endtime));
			}
			sub.add(Restrictions.or(con1, con2));
			sub.add(Restrictions.eq("i.status", OdiConstant.STATUS_BOOK));
			if(isBooking) {
				sub.add(Restrictions.eqProperty("i.dpid", "t.dpid"));
				sub.setProjection(Projections.property("i.dpid"));
				query.setProjection(Projections.distinct(Projections.property("t.price")));
			}else {
				sub.add(Restrictions.eqProperty("i.id", "t.dpid"));
				sub.setProjection(Projections.property("i.id"));
				query.setProjection(Projections.distinct(Projections.property("t.theatreprice")));
			}
			query.add(Subqueries.exists(sub));
			priceList = hibernateTemplate.findByCriteria(query);
			cacheService.set(CacheConstant.REGION_ONEMIN, key, priceList);
		}
		Collections.sort(priceList);
		return priceList;
	}
	@Override
	public List<Theatre> getTheatreList(String citycode, Long dramaid, boolean isBooking, int maxnum){
		List<Long> idList = getTheatreidList(citycode, dramaid, isBooking);
		idList = BeanUtil.getSubList(idList, 0, maxnum);
		List<Theatre> theatreList = baseDao.getObjectList(Theatre.class, idList);
		return theatreList;
	}
	@Override
	public List<Long> getTheatreidList(String citycode, Long dramaid, boolean isBooking){
		return getTheatreidList(citycode, dramaid, null, isBooking);
	}
	
	@Override
	public List<Long> getTheatreidList(String citycode, Long dramaid, String opentype, boolean isBooking){
		String key = CacheConstant.buildKey("getTheatreidList112", citycode, dramaid, opentype, isBooking);
		List<Long> idList = (List<Long>) cacheService.get(CacheConstant.REGION_TWENTYMIN, key);
		if(idList == null){
			Timestamp cur = DateUtil.getCurFullTimestamp();
			Class clazz = null;
			if(isBooking) clazz = OpenDramaItem.class;
			else clazz = DramaPlayItem.class;
			DetachedCriteria query = DetachedCriteria.forClass(clazz, "i");
			if(dramaid != null){
				query.add(Restrictions.eq("i.dramaid", dramaid));
			}
			if(StringUtils.isNotBlank(opentype)){
				query.add(Restrictions.eq("i.opentype", opentype));
			}
			Conjunction con1 = Restrictions.conjunction();
			con1.add(Restrictions.ge("i.playtime", cur));
			con1.add(Restrictions.eq("i.period", Status.Y));
			Conjunction con2 = Restrictions.conjunction();
			con2.add(Restrictions.ge("i.endtime", cur));
			con2.add(Restrictions.eq("i.period", Status.N));
			query.add(Restrictions.eq("i.citycode", citycode));
			query.add(Restrictions.or(con1, con2));
			query.setProjection(Projections.distinct(Projections.property("i.theatreid")));
			idList = hibernateTemplate.findByCriteria(query);
			cacheService.set(CacheConstant.REGION_TWENTYMIN, key, idList);
		}
		return idList;
	}
	
	@Override
	public List<Long> getTheatreFieldIdList(String citycode, Long dramaid, boolean isBooking){
		String key = CacheConstant.buildKey("getTheatreFieldIdList32112", citycode, dramaid, isBooking);
		List<Long> idList = (List<Long>) cacheService.get(CacheConstant.REGION_ONEMIN, key);
		if(CollectionUtils.isEmpty(idList)){
			Timestamp cur = DateUtil.getCurFullTimestamp();
			Class clazz = null;
			if(isBooking) clazz = OpenDramaItem.class;
			else clazz = DramaPlayItem.class;
			DetachedCriteria query = DetachedCriteria.forClass(clazz, "i");
			if(dramaid != null){
				query.add(Restrictions.eq("i.dramaid", dramaid));
			}
			query.add(Restrictions.eq("i.status", DramaPlayItem.STATUS_Y));
			Conjunction con1 = Restrictions.conjunction();
			con1.add(Restrictions.ge("i.playtime", cur));
			con1.add(Restrictions.eq("i.period", Status.Y));
			Conjunction con2 = Restrictions.conjunction();
			con2.add(Restrictions.ge("i.endtime", cur));
			con2.add(Restrictions.eq("i.period", Status.N));
			query.add(Restrictions.eq("i.citycode", citycode));
			query.add(Restrictions.or(con1, con2));
			ProjectionList list = Projections.projectionList().add(Projections.groupProperty("i.theatreid"),"theatreid");
			list.add(Projections.min("i.roomid"),"roomid");
			list.add(Projections.max("i.sortnum"),"sortnum");
			query.setProjection(list);
			query.setResultTransformer(DetachedCriteria.ALIAS_TO_ENTITY_MAP);
			List<Map> resultMap = hibernateTemplate.findByCriteria(query);
			Collections.sort(resultMap, new MultiPropertyComparator(new String[]{"sortnum"}, new boolean[]{true}));
			idList = new ArrayList<Long>();
			for (Map result : resultMap) {
				Long roomid = Long.parseLong(result.get("roomid")+"");
				if(!idList.contains(roomid)){
					idList.add(roomid);
				}
			}
			cacheService.set(CacheConstant.REGION_ONEMIN, key, idList);
		}
		return idList;
	}
	
	@Override
	public List<Long> getDramaStarDramaIdList(String citycode, Long dramaStarId){
		String key = CacheConstant.buildKey("getDramaStarDramaIdList32112", citycode, dramaStarId);
		List<Long> idList = (List<Long>) cacheService.get(CacheConstant.REGION_TWENTYMIN, key);
		if(CollectionUtils.isEmpty(idList)){
			Timestamp cur = DateUtil.getCurFullTimestamp();
			DetachedCriteria query = DetachedCriteria.forClass(DramaPlayItem.class, "i");
			if(dramaStarId != null){
				query.add(Restrictions.eq("i.dramaStarId", dramaStarId));
			}
			Conjunction con1 = Restrictions.conjunction();
			con1.add(Restrictions.ge("i.playtime", cur));
			con1.add(Restrictions.eq("i.period", Status.Y));
			Conjunction con2 = Restrictions.conjunction();
			con2.add(Restrictions.ge("i.endtime", cur));
			con2.add(Restrictions.eq("i.period", Status.N));
			query.add(Restrictions.eq("i.citycode", citycode));
			query.add(Restrictions.or(con1, con2));
			query.setProjection(Projections.distinct(Projections.property("i.dramaid")));
			idList = hibernateTemplate.findByCriteria(query);
			cacheService.set(CacheConstant.REGION_TWENTYMIN, key, idList);
		}
		return idList;
	}
	@Override
	public boolean isBookingByDramaId(Long dramaid){
		int count = getOpenDramaItemCountByDramaId(dramaid);
		return count > 0;
	}
	private int getOpenDramaItemCountByDramaId(Long dramaid){
		Timestamp curtime = new Timestamp(System.currentTimeMillis());
		DetachedCriteria query = DetachedCriteria.forClass(OpenDramaItem.class, "odi");
		query.add(Restrictions.eq("dramaid", dramaid));
		query.add(Restrictions.eq("status", OdiConstant.STATUS_BOOK));
		Conjunction con1 = Restrictions.conjunction();
		con1.add(Restrictions.ge("playtime", curtime));
		con1.add(Restrictions.eq("period", Status.Y));
		Conjunction con2 = Restrictions.conjunction();
		con2.add(Restrictions.ge("endtime", curtime));
		con2.add(Restrictions.eq("period", Status.N));
		query.add(Restrictions.or(con1, con2));
		query.setProjection(Projections.rowCount());
		List result = hibernateTemplate.findByCriteria(query);
		if (result.isEmpty()) return 0;
		else return Integer.parseInt(""+result.get(0));
	}
	
	@Override
	public List<Long> getCurBookingTheatreList(String citycode, String countycode){
		String key = CacheConstant.buildKey("get30CurBookingTheatreList", citycode, countycode);
		List<Long> idList = (List<Long>) cacheService.get(CacheConstant.REGION_HALFHOUR, key);
		if(idList == null) {
			Timestamp cur = DateUtil.getCurFullTimestamp();
			DetachedCriteria query = DetachedCriteria.forClass(OpenDramaItem.class, "i");
			DetachedCriteria subQuery = DetachedCriteria.forClass(Theatre.class, "t");
			subQuery.add(Restrictions.eqProperty("i.theatreid", "t.id"));
			if(StringUtils.isNotBlank(countycode)) subQuery.add(Restrictions.eq("t.countycode", countycode));
			if(StringUtils.isNotBlank(citycode)) subQuery.add(Restrictions.eq("t.citycode", citycode));
			subQuery.setProjection(Projections.property("t.id"));
			query.add(Subqueries.exists(subQuery));
			Conjunction con1 = Restrictions.conjunction();
			con1.add(Restrictions.ge("i.playtime", cur));
			con1.add(Restrictions.eq("i.period",Status.Y));
			Conjunction con2 = Restrictions.conjunction();
			con2.add(Restrictions.ge("i.endtime", cur));
			con2.add(Restrictions.eq("i.period",Status.N));
			query.add(Restrictions.or(con1, con2));
			query.setProjection(Projections.distinct(Projections.property("i.theatreid")));
			idList = hibernateTemplate.findByCriteria(query);
			cacheService.set(CacheConstant.REGION_HALFHOUR, key, idList);
		}
		return idList;
	}
	
	@Override
	public List<Theatre> getTheatreidByDramaid(String citycode, int from, int maxnum){
		Timestamp cur = DateUtil.getCurFullTimestamp();
		Timestamp starttime = DateUtil.getBeginningTimeOfDay(cur);
		Timestamp endtime = DateUtil.getLastTimeOfDay(cur);
		Map<Long, Integer> theatreMap = getTheatreidOrderDramaid(citycode, starttime, endtime);
		List<Map.Entry<Long, Integer>> entityList = new ArrayList<Map.Entry<Long,Integer>>(theatreMap.entrySet());
		Collections.sort(entityList, new Comparator<Map.Entry<Long, Integer>>() {
			@Override
			public int compare(Entry<Long, Integer> o1, Entry<Long, Integer> o2) {
				return -(o1.getValue().compareTo(o2.getValue()));
			}
		});
		entityList = BeanUtil.getSubList(entityList, from, maxnum);
		List<Long> idList = new ArrayList<Long>();
		for (Map.Entry<Long, Integer> entry : entityList) {
			idList.add(entry.getKey());
		}
		List<Theatre> result = baseDao.getObjectList(Theatre.class, idList);
		return result;
	}
	private Map<Long,Integer> getTheatreidOrderDramaid(String citycode, Timestamp starttime, Timestamp endtime){
		String key = CacheConstant.buildKey("TheatrePic", citycode, starttime, endtime);
		Map<Long,Integer> rowList = (Map<Long,Integer>) cacheService.get(CacheConstant.REGION_HALFMIN, key);
		if(rowList == null){
			DetachedCriteria query = DetachedCriteria.forClass(DramaPlayItem.class, "c");
			Conjunction con1 = Restrictions.conjunction();
			con1.add(Restrictions.ge("playtime", starttime));
			con1.add(Restrictions.le("playtime", endtime));
			con1.add(Restrictions.eq("period", Status.Y));
			Conjunction con2 = Restrictions.conjunction();
			con2.add(Restrictions.ge("endtime", starttime));
			con2.add(Restrictions.le("endtime", endtime));
			con2.add(Restrictions.eq("period", Status.N));
			query.add(Restrictions.or(con1, con2));
			query.add(Restrictions.eq("citycode", citycode));
			query.setProjection(Projections.projectionList()
					.add(Projections.groupProperty("theatreid"), "theatreid")
					.add(Projections.countDistinct("dramaid"),"num"));
			query.setResultTransformer(DetachedCriteria.ALIAS_TO_ENTITY_MAP);
			query.addOrder(Order.desc("num"));
			rowList = new Hashtable<Long,Integer>();
			List<Map> tmpMapList = readOnlyTemplate.findByCriteria(query);
			for (Map tmpMap : tmpMapList) {
				rowList.put((Long)tmpMap.get("theatreid"), Integer.parseInt(tmpMap.get("num")+""));
			}
			cacheService.set(CacheConstant.REGION_HALFMIN, key, rowList);
		}
		return rowList;
	}
	@Override
	public Integer getDramaCount(Long dramaid, Timestamp playtime) {
		int result = getDramaPlayItemCount(dramaid, playtime);
		return result;
	}
	
	private Integer getDramaPlayItemCount(Long dramaid, Timestamp playtime){
		DetachedCriteria query = DetachedCriteria.forClass(DramaPlayItem.class, "dpi");
		query.add(Restrictions.eq("dramaid", dramaid));
		Conjunction con1 = Restrictions.conjunction();
		con1.add(Restrictions.ge("playtime", playtime));
		con1.add(Restrictions.eq("period", Status.Y));
		Conjunction con2 = Restrictions.conjunction();
		con2.add(Restrictions.ge("endtime", playtime));
		con2.add(Restrictions.eq("period", Status.N));
		query.add(Restrictions.or(con1, con2));
		query.add(Restrictions.eq("status", Status.Y));
		query.setProjection(Projections.rowCount());
		List list = readOnlyTemplate.findByCriteria(query, 0, 1);
		if(list.isEmpty()) return 0;
		return Integer.parseInt("" + list.get(0));
	}
	
	@Override
	public List<Date> getDramaPlayDateList(Long dramaid,  Timestamp starttime, Timestamp endtime,  Boolean isPartner){
		String key = CacheConstant.buildKey("getDramaPlayDate24List", dramaid, starttime, endtime, isPartner);
		List<String> playdateList = (List<String>) cacheService.get(CacheConstant.REGION_TWENTYMIN, key);
		if(CollectionUtils.isEmpty(playdateList)){
			List params = new ArrayList();
			params.add(dramaid);
			Timestamp cur = DateUtil.getCurFullTimestamp();
			if(starttime != null){
				if(cur.before(starttime)){
					cur = starttime;
				}
			}else endtime = DateUtil.getNextMonthFirstDay(cur);
			params.add(cur);
			params.add(endtime);
			params.add(Status.Y);
			params.add(cur);
			params.add(endtime);
			params.add(Status.N);
			String query = "select distinct to_char(o.playtime,'yyyy-mm-dd') from DramaPlayItem o " +
					"where o.dramaid=? and ( o.playtime >= ? and o.playtime < ? and o.period=? or o.endtime>= ? and o.endtime<? and o.period=? )";
			if(isPartner != null){
				if(isPartner){ 
					query += " and o.status like ? and o.partner =? ";
					params.add(DramaPlayItem.STATUS_Y + "%");
					params.add(Status.Y);
				}else{
					query += " and o.status = ? ";
					params.add(DramaPlayItem.STATUS_Y);
				}
			}else{
				query += " and o.status != ? ";
				params.add(DramaPlayItem.STATUS_N);
			}
			playdateList = hibernateTemplate.find(query, params.toArray());
			Collections.sort(playdateList);
			cacheService.set(CacheConstant.REGION_TWENTYMIN, key, playdateList);
		}
		List<Date> result = new ArrayList<Date>();
		for(String date:playdateList) result.add(DateUtil.parseDate(date));
		return result;
	}
	@Override
	public List<Date> getDramaPlayMonthDateList(Long dramaid, Boolean isPartner){
		String key = CacheConstant.buildKey("getDramaPlayMonthDateList", dramaid, isPartner);
		List<String> playdateList = (List<String>) cacheService.get(CacheConstant.REGION_TWENTYMIN, key);
		if(CollectionUtils.isEmpty(playdateList)){
			List params = new ArrayList();
			params.add(dramaid);
			Timestamp cur = DateUtil.getCurFullTimestamp();
			params.add(cur);
			params.add(Status.Y);
			params.add(cur);
			params.add(Status.N);
			String query = "select distinct to_char(o.playtime,'yyyy-mm') from DramaPlayItem o " +
					"where o.dramaid=? and (o.playtime > ? and o.period = ? or o.endtime >? and o.period=? )";
			if(isPartner != null){
				if(isPartner){ 
					query += " and o.status like ? and o.partner =? ";
					params.add(DramaPlayItem.STATUS_Y + "%");
					params.add(Status.Y);
				}else{
					query += " and o.status = ? ";
					params.add(DramaPlayItem.STATUS_Y);
				}
			}else{
				query += " and o.status != ? ";
				params.add(DramaPlayItem.STATUS_N);
			}
			playdateList = hibernateTemplate.find(query, params.toArray());
			Collections.sort(playdateList);
			cacheService.set(CacheConstant.REGION_TWENTYMIN, key, playdateList);
		}
		List<Date> result = new ArrayList<Date>();
		for(String date:playdateList) result.add(DateUtil.parseDate(date,"yyyy-MM"));
		return result;
	}
	
	@Override
	public Integer getDramaOpenCount() {
		return getOpenDramaItemCount();
	}
	
	private Integer getOpenDramaItemCount(){
		String key = CacheConstant.buildKey("odi24hour", TagConstant.TAG_DRAMA);
		Integer count = (Integer) cacheService.get(CacheConstant.REGION_TWENTYMIN, key);
		if(count == null){
			Timestamp curtime = DateUtil.getCurFullTimestamp();
			DetachedCriteria query = DetachedCriteria.forClass(Drama.class, "d");
			DetachedCriteria sub = DetachedCriteria.forClass(OpenDramaItem.class, "odi");
			sub.add(Restrictions.eq("odi.status", OdiConstant.STATUS_BOOK));
			sub.add(Restrictions.le("odi.opentime", curtime));
			sub.add(Restrictions.gt("odi.closetime", curtime));
			sub.add(Restrictions.eqProperty("odi.dramaid", "d.id"));
			sub.setProjection(Projections.property("odi.id"));
			query.add(Subqueries.exists(sub));
			count = Integer.valueOf(hibernateTemplate.findByCriteria(query.setProjection(Projections.rowCount())).get(0)+"");
			cacheService.set(CacheConstant.REGION_TWENTYMIN, key, count);
		}
		return count;
	}
	@Override
	public List<Long> getDramaPlayIdList(List dramaid){
		DetachedCriteria query = DetachedCriteria.forClass(DramaPlayItem.class, "dpi");
		query.add(Restrictions.in("dpi.dramaid", dramaid));
		Timestamp cur = DateUtil.getCurFullTimestamp();
		Conjunction con1 = Restrictions.conjunction();
		con1.add(Restrictions.ge("dpi.playtime", cur));
		con1.add(Restrictions.eq("dpi.period", Status.Y));
		Conjunction con2 = Restrictions.conjunction();
		con2.add(Restrictions.ge("dpi.endtime", cur));
		con2.add(Restrictions.eq("dpi.period", Status.N));
		query.add(Restrictions.or(con1, con2));
		DetachedCriteria subquery = DetachedCriteria.forClass(OpenDramaItem.class, "odi");
		subquery.add(Restrictions.eqProperty("odi.dpid", "dpi.id"));
		subquery.setProjection(Projections.property("odi.id"));
		query.add(Subqueries.exists(subquery));
		query.setProjection(Projections.property("dpi.id"));
		List<Long> dpidList = hibernateTemplate.findByCriteria(query);
		return dpidList;
	}
	@Override
	public DramaPlayItem getUniqueDpi(Long theatreid, Long dramaid, Long roomid, Timestamp playtime){
		DetachedCriteria query = DetachedCriteria.forClass(DramaPlayItem.class);
		query.add(Restrictions.eq("theatreid", theatreid));
		query.add(Restrictions.eq("dramaid", dramaid));
		query.add(Restrictions.eq("roomid", roomid));
		query.add(Restrictions.eq("playtime", playtime));
		query.addOrder(Order.asc("id"));
		List<DramaPlayItem> itemList = hibernateTemplate.findByCriteria(query);
		if(itemList.size() > 0) return itemList.get(0);
		return null;
	}
	
	@Override
	public List<Long> getCurDramaidList(String citycode){
		List<Long> dramaIdList = getDramaidByCitycode(citycode, true);
		return dramaIdList;
	}
	
	private List<Long> getDramaidByCitycode(String citycode, boolean cache){
		String key = "";
		List<Long> idList = null;
		if(cache){
			key = CacheConstant.buildKey("getDramaidByDramaPlayItem3243", citycode);
			idList = (List<Long>) cacheService.get(CacheConstant.REGION_ONEHOUR, key);
		}
		if(!cache || CollectionUtils.isEmpty(idList)){
			Timestamp curtime = DateUtil.getCurFullTimestamp();
			DetachedCriteria qry = DetachedCriteria.forClass(DramaPlayItem.class, "d");
			DetachedCriteria subQry = DetachedCriteria.forClass(Drama.class, "o");
			Conjunction con1 = Restrictions.conjunction();
			con1.add(Restrictions.gt("d.playtime", curtime));
			con1.add(Restrictions.eq("d.period", Status.Y));
			Conjunction con2 = Restrictions.conjunction();
			con2.add(Restrictions.gt("d.playtime", curtime));
			con2.add(Restrictions.eq("d.period", Status.N));
			qry.add(Restrictions.or(con1, con2));
			qry.add(Restrictions.ne("d.status", OdiConstant.STATUS_DISCARD));
			qry.add(Restrictions.eq("d.citycode", citycode));
			subQry.add(Restrictions.eqProperty("d.dramaid", "o.id"));
			subQry.setProjection(Projections.property("o.id"));
			qry.add(Subqueries.exists(subQry));
			qry.setProjection(Projections.distinct(Projections.property("d.dramaid")));
			idList = hibernateTemplate.findByCriteria(qry); 
			cacheService.set(CacheConstant.REGION_ONEHOUR, key, idList);
		}
		return idList;
	}
	
	@Override
	public DramaPlayItem getDpiBySeqno(String seller, String sellerseq) {
		String query = "from DramaPlayItem where seller=? and sellerseq=? ";
		List<DramaPlayItem> dpiList = hibernateTemplate.find(query, seller, sellerseq);
		if(dpiList.isEmpty()) return null;
		return dpiList.get(0);
	}

	private static final String KEY_DRAMAPRICE_REFRESH = "dramaPriceRefresh";//演出价格刷新
	
	@Override
	public void refreshDramaPrice(Long dramaid){
		AsynchTask task = new AsynchTask(KEY_DRAMAPRICE_REFRESH, String.valueOf(dramaid), 600, true);
		task.addInfo("dramaid", dramaid);
		asynchTaskService.addTask(task);
	}
	
	private class  DramaTaskProcessor implements AsynchTaskProcessor{
		@Override
		public void processTask(AsynchTask task) {
			Long dramaid = (Long) task.getInfo("dramaid");
			Drama drama = baseDao.getObject(Drama.class, dramaid);
			if(drama!= null){
				List<Integer> priceList = getPriceList(null, drama.getId(), null, null, true);
				for (Iterator iterator = priceList.iterator(); iterator.hasNext();) {
					Integer price = (Integer) iterator.next();
					if(price<=0) iterator.remove();
				}
				if(!priceList.isEmpty()){
					Collections.sort(priceList);
					drama.setPrices(StringUtils.join(priceList, ","));
					baseDao.saveObject(drama);
				}
			}
		}
		@Override
		public int getLockSize() {
			return 5000;
		}
	}
	
	@Override
	public void afterPropertiesSet() throws Exception {
		asynchTaskService.registerTaskProcessor(KEY_DRAMAPRICE_REFRESH, new DramaTaskProcessor());
	}
}
