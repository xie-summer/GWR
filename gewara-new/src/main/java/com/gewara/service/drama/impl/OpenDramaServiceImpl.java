package com.gewara.service.drama.impl;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.hibernate.criterion.Conjunction;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.criterion.Subqueries;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.support.PropertyComparator;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.gewara.constant.DramaConstant;
import com.gewara.constant.OdiConstant;
import com.gewara.constant.Status;
import com.gewara.constant.TheatreSeatConstant;
import com.gewara.constant.sys.CacheConstant;
import com.gewara.constant.ticket.OpiConstant;
import com.gewara.helper.TspHelper;
import com.gewara.model.drama.Drama;
import com.gewara.model.drama.DramaOrder;
import com.gewara.model.drama.DramaPlayItem;
import com.gewara.model.drama.DramaToStar;
import com.gewara.model.drama.OpenDramaItem;
import com.gewara.model.drama.OpenTheatreSeat;
import com.gewara.model.drama.TheatreField;
import com.gewara.model.drama.TheatreRoom;
import com.gewara.model.drama.TheatreRoomSeat;
import com.gewara.model.drama.TheatreSeatArea;
import com.gewara.model.drama.TheatreSeatPrice;
import com.gewara.service.drama.DramaPlayItemService;
import com.gewara.service.drama.OpenDramaService;
import com.gewara.service.impl.BaseServiceImpl;
import com.gewara.support.ErrorCode;
import com.gewara.support.MultiPropertyComparator;
import com.gewara.untrans.CacheService;
import com.gewara.untrans.monitor.MonitorService;
import com.gewara.util.BeanUtil;
import com.gewara.util.ChangeEntry;
import com.gewara.util.DateUtil;
@Service("openDramaService")
public class OpenDramaServiceImpl extends BaseServiceImpl implements OpenDramaService{
	@Autowired@Qualifier("monitorService")
	private MonitorService monitorService;
	public void setMonitorService(MonitorService monitorService) {
		this.monitorService = monitorService;
	}
	@Autowired@Qualifier("cacheService")
	private CacheService cacheService;
	public void setCacheService(CacheService cacheService) {
		this.cacheService = cacheService;
	}
	@Autowired@Qualifier("dramaPlayItemService")
	private DramaPlayItemService dramaPlayItemService;
	
	@Override
	public List<TheatreRoomSeat> getSeatList(Long roomid) {
		String hql = "from TheatreRoomSeat s where s.roomid = ?";
		List<TheatreRoomSeat> seatList = hibernateTemplate.find(hql, roomid);
		return seatList;
	}
	@Override
	public String getTheatreRoomSeatMapStr(TheatreRoom room) {
		String[][] seatMap = new String[room.getLinenum()-room.getFirstline()+1][room.getRanknum()-room.getFirstrank()+1];
		for(int i=0;i<room.getLinenum();i++){
			for(int j=0;j<room.getRanknum();j++){
				seatMap[i][j] = "O";
			}
		}
		List<TheatreRoomSeat> seatList = getSeatList(room.getId());
		for(TheatreRoomSeat seat: seatList){
			seatMap[(seat.getLineno()-room.getFirstline()+1)-1][(seat.getRankno()-room.getFirstrank()+1)-1] = "A";
		}
		StringBuilder sb = new StringBuilder();
		for(int i=0;i<room.getLinenum();i++){
			sb.append(StringUtils.join(seatMap[i], ",") + "@@");
		}
		return sb.substring(0, sb.length() -2);
	}
	
	@Override
	public String getTheatreSeatAreaMapStr(TheatreSeatArea seatArea) {
		String[][] seatMap = new String[seatArea.getLinenum()-seatArea.getFirstline()+1][seatArea.getRanknum()-seatArea.getFirstrank()+1];
		for(int i=0;i<seatArea.getLinenum();i++){
			for(int j=0;j<seatArea.getRanknum();j++){
				seatMap[i][j] = "O";
			}
		}
		List<OpenTheatreSeat> seatList = baseDao.getObjectListByField(OpenTheatreSeat.class, "areaid", seatArea.getId());
		for(OpenTheatreSeat seat: seatList){
			seatMap[(seat.getLineno()-seatArea.getFirstline()+1)-1][(seat.getRankno()-seatArea.getFirstrank()+1)-1] = "A";
		}
		StringBuilder sb = new StringBuilder();
		for(int i=0;i<seatArea.getLinenum();i++){
			sb.append(StringUtils.join(seatMap[i], ",") + "@@");
		}
		return StringUtils.substring(sb.toString(), 0, sb.length() -2);
	}
	@Override
	public boolean addRowSeat(Long roomId) {
		TheatreRoom section = baseDao.getObject(TheatreRoom.class, roomId);
		int rowno = section.getLinenum() + 1;
		List objList = new ArrayList();
		for(int rankno=1; rankno<=section.getRanknum();rankno++){
			TheatreRoomSeat rs = new TheatreRoomSeat(roomId, rowno, rankno);
			objList.add(rs);
		}
		section.setLinenum(rowno);
		section.setSynchtime(new Timestamp(System.currentTimeMillis()));
		objList.add(section);
		baseDao.saveObjectList(objList);
		return true;
	}
	
	
	@Override
	public boolean addRankSeat(Long roomId) {
		TheatreRoom section = baseDao.getObject(TheatreRoom.class, roomId);
		int rankno = section.getRanknum() + 1;
		List objList = new ArrayList();
		for(int rowno=1; rowno<=section.getLinenum();rowno++){
			TheatreRoomSeat rs = new TheatreRoomSeat(roomId, rowno, rankno);
			objList.add(rs);
		}
		section.setRanknum(rankno);
		section.setSynchtime(new Timestamp(System.currentTimeMillis()));
		objList.add(section);
		baseDao.saveObjectList(objList);
		return true;
	}
	@Override
	public TheatreRoomSeat getRoomSeatByLocation(Long roomid, int line, int rank){
		String query = "from TheatreRoomSeat where roomid = ? and lineno = ? and rankno = ?";
		List<TheatreRoomSeat> result = hibernateTemplate.find(query, roomid, line, rank);
		if(result.isEmpty()) return null;
		return result.get(0);
	}
	@Override
	public boolean deleteRankSeat(Long roomId) {
		String update = "delete TheatreRoomSeat where roomid = ? and rankno = ? ";
		TheatreRoom section = baseDao.getObject(TheatreRoom.class,roomId);
		hibernateTemplate.bulkUpdate(update, roomId, section.getRanknum());
		section.setRanknum(section.getRanknum() - 1);
		section.setSynchtime(new Timestamp(System.currentTimeMillis()));
		baseDao.saveObject(section);
		return true;
	}
	@Override
	public boolean deleteRowSeat(Long roomId) {
		String update = "delete TheatreRoomSeat where roomid = ? and lineno = ? ";
		TheatreRoom section = baseDao.getObject(TheatreRoom.class,roomId);
		hibernateTemplate.bulkUpdate(update, roomId, section.getLinenum());
		section.setLinenum(section.getLinenum() - 1);
		section.setSynchtime(new Timestamp(System.currentTimeMillis()));
		baseDao.saveObject(section);
		return true;
	}
	@Override
	public boolean updateSeatLine(Long roomid, int lineno, String newline) {
		String update = "update TheatreRoomSeat set seatline = ? where roomid = ? and lineno = ?";
		hibernateTemplate.bulkUpdate(update, newline, roomid, lineno);
		TheatreRoom section = baseDao.getObject(TheatreRoom.class,roomid);
		section.setSynchtime(new Timestamp(System.currentTimeMillis()));
		baseDao.saveObject(section);
		return true;
	}
	@Override
	public boolean updateSeatRank(Long roomid, int rank, String newrank) {
		String update = "update TheatreRoomSeat set seatrank = ? where roomid = ? and rankno = ? ";
		hibernateTemplate.bulkUpdate(update, newrank, roomid, rank);
		TheatreRoom section = baseDao.getObject(TheatreRoom.class,roomid);
		section.setSynchtime(new Timestamp(System.currentTimeMillis()));
		baseDao.saveObject(section);
		return true;
	}
	private OpenTheatreSeat getOpenTheatreSeat(Long areaid, String seatline, String seatrank){
		String query = "from OpenTheatreSeat where areaid = ? and seatline = ? and seatrank = ? ";
		List<OpenTheatreSeat> seatList = hibernateTemplate.find(query, areaid, seatline, seatrank);
		if(seatList.size()>0) return seatList.get(0);
		return null;
	}
	@Override
	public List<Long> getSeatidListBySeatLabel(Long areaid, String seatLabel){
		List<String> slList = Arrays.asList(StringUtils.split(seatLabel, ","));
		List<Long> seatidList = new ArrayList<Long>();
		for(String sl : slList){
			String[] loc = sl.split(":");
			OpenTheatreSeat seat = getOpenTheatreSeat(areaid, loc[0], loc[1]);
			if(seat!=null){
				seatidList.add(seat.getId());
			}
		}
		return seatidList;
	}
	
	
	@Override
	public ErrorCode<String> openSeat(Long userid, OpenDramaItem odi, TheatreSeatArea seatArea, TheatreRoom room){
		if(room == null) return ErrorCode.getFailure("基础区域数据错误！");
		if(!odi.getRoomid().equals(room.getFieldid())) return ErrorCode.getFailure("场地错误！");
		if(!room.getTheatreid().equals(seatArea.getTheatreid()) || !StringUtils.equals(seatArea.getRoomnum(), room.getNum())){
			return ErrorCode.getFailure("区域错误！");
		}
		List<TheatreSeatPrice> priceList = baseDao.getObjectListByField(TheatreSeatPrice.class, "areaid", seatArea.getId());
		if(priceList.isEmpty()) return ErrorCode.getFailure("区域价格不存在！");
		Timestamp curtime = new Timestamp(System.currentTimeMillis());
		odi.setUpdatetime(curtime);
		List<TheatreRoomSeat> seatList = getSeatList(room.getId());
		baseDao.saveObject(odi);
		List<OpenTheatreSeat> oseatList = new ArrayList<OpenTheatreSeat>();
		Collections.sort(priceList, new PropertyComparator("seattype", true, true));
		for(TheatreRoomSeat seat : seatList){
			OpenTheatreSeat oseat = new OpenTheatreSeat(seat, odi.getDpid(), seatArea, priceList.get(0));
			oseatList.add(oseat);
		}
		List<OpenTheatreSeat> oldSeatList = baseDao.getObjectListByField(OpenTheatreSeat.class, "areaid", seatArea.getId());
		baseDao.removeObjectList(oldSeatList);
		hibernateTemplate.flush();
		baseDao.saveObjectList(oseatList);
		hibernateTemplate.flush();
		seatArea.setLinenum(room.getLinenum());
		seatArea.setRanknum(room.getRanknum());
		seatArea.setTotal(oseatList.size());
		seatArea.setLimitnum(oseatList.size());
		baseDao.saveObject(seatArea);
		dbLogger.warn(userid + "重设场次区域座位：" +seatArea.getDpid() + "[" + seatArea.getId() + "]"+ seatArea.getSeller() );
		return ErrorCode.SUCCESS;
	}
	
	@Override
	public List<OpenTheatreSeat> getOpenTheatreSeatListByDpid(Long dpid, Long areaid) {
		String query = "from OpenTheatreSeat s where s.dpid = ? "; 
		List params = new ArrayList();
		params.add(dpid);
		if(areaid != null){
			params.add(areaid);
			query += " and areaid=? ";
		}
		query += " order by s.lineno, s.rankno";
		List<OpenTheatreSeat> result = hibernateTemplate.find(query, params.toArray());
		return result;
	}
	@Override
	public TheatreSeatPrice getTheatreSeatPriceBySeatType(Long itemid, Long areaid, String seattype){
		String query = "from TheatreSeatPrice s where s.dpid = ? and s.areaid=? and s.seattype = ? and (s.status <> 'D' or s.status is null) ";
		List<TheatreSeatPrice> result = hibernateTemplate.find(query, itemid, areaid, seattype);
		if(result.isEmpty()) return null;
		return result.get(0);
	}
	
	@Override
	public ErrorCode addPriceSeat(Long dpid, Long areaid, String seattype, Long seatid, Long userid) {
		if(userid==null) return ErrorCode.getFailure("请先登录！");
		if(StringUtils.isBlank(seattype)||seatid == null) 
			return ErrorCode.getFailure("参数有错误，类型和座位不能为空！");
		if(StringUtils.isNotBlank(seattype) && (seattype.length()!=1 || "ABCDEFGHI".indexOf(seattype) < 0))
			return ErrorCode.getFailure("座位类型不正确！");
		TheatreSeatPrice tsp = getTheatreSeatPriceBySeatType(dpid, areaid, seattype);
		OpenTheatreSeat oseat = baseDao.getObject(OpenTheatreSeat.class, seatid);
		if(oseat.isAvailable() || oseat.isLocked()){
			ChangeEntry changeEntry = new ChangeEntry(oseat);
			oseat.setPrice(tsp.getPrice());
			oseat.setCostprice(tsp.getCostprice());
			oseat.setTheatreprice(tsp.getTheatreprice());
			oseat.setSeattype(seattype);
			oseat.setRemark("[价"+seattype+userid+"]"+StringUtils.defaultString(oseat.getRemark()));
			baseDao.saveObject(oseat);
			monitorService.saveChangeLog(userid, OpenTheatreSeat.class, oseat.getId(),changeEntry.getChangeMap( oseat));
			/*OpenDramaItem odi = baseDao.getObject(OpenDramaItem.class, oseat.getOdiid());
			odi.setSynchots(new Timestamp(System.currentTimeMillis()));
			baseDao.saveObject(odi);*/
			return ErrorCode.SUCCESS;
		}
		return ErrorCode.getFailure("座位正在出售或已售出，不能更改价格！");
	}
	@Override
	public ErrorCode removePriceSeat(Long itemid, Long areaid, Long seatid, Long userid) {
		if(userid==null) return ErrorCode.getFailure("请先登录！");
		OpenTheatreSeat oseat = baseDao.getObject(OpenTheatreSeat.class, seatid);
		OpenDramaItem odi = baseDao.getObject(OpenDramaItem.class, oseat.getOdiid());
		DramaPlayItem item = baseDao.getObject(DramaPlayItem.class, odi.getDpid());
		List<TheatreSeatPrice> tspList =dramaPlayItemService.getTspList(item.getId(), areaid);
		TspHelper tspHelper = new TspHelper(tspList);
		TheatreSeatPrice tsp = tspHelper.getFirstTsp();
		if(oseat.isAvailable() || oseat.isLocked()){
			ChangeEntry changeEntry = new ChangeEntry(oseat);
			oseat.setPrice(tsp.getPrice());
			oseat.setCostprice(tsp.getCostprice());
			oseat.setTheatreprice(tsp.getTheatreprice());
			oseat.setSeattype(tsp.getSeattype()); //默认给它A类型座位
			oseat.setRemark("[清"+userid+"]"+StringUtils.defaultString(oseat.getRemark()));
			baseDao.saveObject(oseat);
			monitorService.saveChangeLog(userid, OpenTheatreSeat.class, oseat.getId(),changeEntry.getChangeMap( oseat));
		/*	odi.setSynchots(new Timestamp(System.currentTimeMillis()));
			baseDao.saveObject(odi);*/
			return ErrorCode.SUCCESS;
		}
		return ErrorCode.getFailure("座位正在出售或已售出，不能更改价格！");
	}
	
	@Override
	public ErrorCode lockSeat(Long seatId, String locktype, String lockreason, Long userid) {
		if(userid==null) return ErrorCode.getFailure("请先登录！");
		if(!TheatreSeatConstant.STATUS_LOCK_LIST.contains(locktype)) return ErrorCode.getFailure("锁定类型不对！");
		OpenTheatreSeat oseat = baseDao.getObject(OpenTheatreSeat.class, seatId);
		if(!oseat.isAvailable()) return ErrorCode.getFailure("此座位不能锁定！");
		ChangeEntry changeEntry = new ChangeEntry(oseat);
		oseat.setStatus(locktype);
		Timestamp cur = new Timestamp(System.currentTimeMillis());
		oseat.setRemark(StringUtils.defaultString(lockreason)+"[锁" + DateUtil.format(cur, "ddHHmmss") + "," + userid + "]," + StringUtils.defaultString(oseat.getRemark()));
		baseDao.saveObject(oseat);
		monitorService.saveChangeLog(userid, OpenTheatreSeat.class, oseat.getId(),changeEntry.getChangeMap( oseat));
	/*	OpenDramaItem odi = baseDao.getObject(OpenDramaItem.class, oseat.getOdiid());
		odi.setSynchots(cur);
		baseDao.saveObject(odi);*/
		return ErrorCode.SUCCESS;
	}
	@Override
	public ErrorCode unLockSeat(Long seatId, Long userid) {
		if(userid==null) return ErrorCode.getFailure("请先登录！");
		OpenTheatreSeat oseat = baseDao.getObject(OpenTheatreSeat.class, seatId);
		if(!oseat.isLocked()) return ErrorCode.getFailure("此座位没锁定！");
		ChangeEntry changeEntry = new ChangeEntry(oseat);
		oseat.setStatus(TheatreSeatConstant.STATUS_NEW);
		Timestamp cur = new Timestamp(System.currentTimeMillis());
		oseat.setRemark("[释" + DateUtil.format(cur,"ddHHmmss") + "," + userid + "]," + oseat.getRemark());
		baseDao.saveObject(oseat);
		monitorService.saveChangeLog(userid, OpenTheatreSeat.class, oseat.getId(),changeEntry.getChangeMap( oseat));
		/*OpenDramaItem odi = baseDao.getObject(OpenDramaItem.class, oseat.getOdiid());
		odi.setSynchots(cur);
		baseDao.saveObject(odi);*/
		return ErrorCode.SUCCESS;
	}
	
	@Override
	public ErrorCode batchUnLockSeatBySeatPrice(Long itemid, Long areaid, TheatreSeatPrice seatprice, Long userid){
		if(userid==null){
			return ErrorCode.getFailure("请先登录！");
		}
		if(areaid == null){
			return ErrorCode.getFailure("区域ID不能为空！");
		}
		if(seatprice == null){
			return ErrorCode.getFailure("价格不能为空！");
		}
		OpenDramaItem odi = baseDao.getObjectByUkey(OpenDramaItem.class, "dpid", itemid, false);
		if(odi == null) return ErrorCode.getFailure("场次不存在或未开放！");
		if(!odi.isOpenseat()){
			return ErrorCode.getFailure("非选座场次！");
		}
		if(!odi.getDpid().equals(seatprice.getDpid())){
			return ErrorCode.getFailure("价格不对应该场次！");
		}
		String locktypes="'" + TheatreSeatConstant.STATUS_LOCKB + "','" + TheatreSeatConstant.STATUS_LOCKC + "','" + TheatreSeatConstant.STATUS_LOCKD + "'";
		String update = "update OpenTheatreSeat o set o.status= ? where o.dpid = ? and o.areaid = ? and o.status in (" + locktypes + ") and o.seattype=? ";
		int locknum = hibernateTemplate.bulkUpdate(update, TheatreSeatConstant.STATUS_NEW, odi.getDpid(), areaid, seatprice.getSeattype());
		return ErrorCode.getSuccessReturn(locknum);
	}
	@Override
	public ErrorCode batchLockSeatBySeatPrice(Long itemid, Long areaid, TheatreSeatPrice seatprice, String locktype, Long userid){
		if(userid==null){
			return ErrorCode.getFailure("请先登录！");
		}
		if(areaid == null){
			return ErrorCode.getFailure("区域ID不能为空！");
		}
		if(seatprice == null){
			return ErrorCode.getFailure("价格不能为空！");
		}
		OpenDramaItem odi = baseDao.getObjectByUkey(OpenDramaItem.class, "dpid", itemid, false);
		if(odi == null) return ErrorCode.getFailure("场次不存在或未开放！");
		if(!odi.isOpenseat()){
			return ErrorCode.getFailure("非选座场次！");
		}
		if(!TheatreSeatConstant.STATUS_LOCK_LIST.contains(locktype)){
			return ErrorCode.getFailure("锁定类型不对！");
		}
		if(!odi.getDpid().equals(seatprice.getDpid())){
			return ErrorCode.getFailure("价格不对应该场次！");
		}
		String update = "update OpenTheatreSeat o set o.status=? where o.dpid=? and o.areaid=? and o.status=? and o.seattype=? ";
		update += " and not exists(select s.id from SellDramaSeat s where s.id=o.id and s.status in ('W','S'))";
		int locknum = hibernateTemplate.bulkUpdate(update, locktype, odi.getDpid(), areaid, TheatreSeatConstant.STATUS_NEW, seatprice.getSeattype());
		return ErrorCode.getSuccessReturn(locknum);
	}
	
	@Override
	public ErrorCode batchUnLockSeat(Long itemid, Long areaid, String lockline, String lockrank, Long userid) {
		if(userid==null) return ErrorCode.getFailure("请先登录！");
		if(areaid == null) return ErrorCode.getFailure("区域ID不能为空！");
		OpenDramaItem odi = baseDao.getObjectByUkey(OpenDramaItem.class, "dpid", itemid, false);
		if(!odi.isOpenseat()) return ErrorCode.getFailure("非选座场次！");
		Timestamp cur = new Timestamp(System.currentTimeMillis());
		String lines = StringUtils.join(BeanUtil.getIdList(lockline, ","), ",");
		String rows = StringUtils.join(BeanUtil.getIdList(lockrank, ","), ",");
		if(StringUtils.isBlank(lines)) return ErrorCode.getFailure("请输入行号！");
		String locktypes="'" + TheatreSeatConstant.STATUS_LOCKB + "','" + TheatreSeatConstant.STATUS_LOCKC + "','" + TheatreSeatConstant.STATUS_LOCKD + "'";
		String update = "update OpenTheatreSeat set status= ?, remark = ?||remark " +
				"where dpid = ? and areaid = ? and status in (" + locktypes + ") ";
		if(StringUtils.isNotBlank(lines)) update += "and lineno in (" + lines + ") ";
		if(StringUtils.isNotBlank(rows)) update +=  "and rankno in (" + rows + ") ";
		String remark = "[释" + DateUtil.format(cur,"ddHHmmss") + "," + userid + "],";
		/*odi.setSynchots(cur);
		baseDao.saveObject(odi);*/
		int locknum = hibernateTemplate.bulkUpdate(update, TheatreSeatConstant.STATUS_NEW, remark, odi.getDpid(), areaid);
		return ErrorCode.getSuccessReturn(locknum);
	}
	@Override
	public ErrorCode batchLockSeat(Long itemid, Long areaid, String locktype, String lockreason, String lockline, String lockrank, Long userid) {
		if(userid==null) return ErrorCode.getFailure("请先登录！");
		if(areaid == null) return ErrorCode.getFailure("区域ID不能为空！");
		OpenDramaItem odi = baseDao.getObjectByUkey(OpenDramaItem.class, "dpid", itemid, false);
		if(!odi.isOpenseat()) return ErrorCode.getFailure("非选座场次！");
		if(!TheatreSeatConstant.STATUS_LOCK_LIST.contains(locktype)) return ErrorCode.getFailure("锁定类型不对！");
		Timestamp cur = new Timestamp(System.currentTimeMillis());
		String lines = StringUtils.join(BeanUtil.getIdList(lockline, ","), ",");
		String rows = StringUtils.join(BeanUtil.getIdList(lockrank, ","), ",");
		if(StringUtils.isBlank(lines) && StringUtils.isBlank(lockrank)) 
			return ErrorCode.getFailure("请输入行号或列号！");
		String update = "update OpenTheatreSeat o set o.status= ?, o.remark = ?||o.remark " +
				"where o.dpid = ? and o.areaid=? and o.status = ? ";
		if(StringUtils.isNotBlank(lines)) update += "and o.lineno in (" + lines + ") ";
		if(StringUtils.isNotBlank(rows)) update +=  "and o.rankno in (" + rows + ") ";
		update += " and not exists(select s.id from SellDramaSeat s where s.id=o.id and s.status in ('W','S'))";
		String remark = StringUtils.defaultString(lockreason)+"[锁" + DateUtil.format(cur, "ddHHmmss") + "," + userid + "],";
		/*odi.setSynchots(cur);
		baseDao.saveObject(odi);*/
		int locknum = hibernateTemplate.bulkUpdate(update, locktype, remark, odi.getDpid(), areaid, TheatreSeatConstant.STATUS_NEW);
		return ErrorCode.getSuccessReturn(locknum);
	}
	
	@Override
	public ErrorCode batchAddBaseSeat(Long roomid, String linelist, String ranklist, Long userid){
		if(userid == null) return ErrorCode.getFailure("请先登录！");
		TheatreRoom room = baseDao.getObject(TheatreRoom.class, roomid);
		if(room == null) return ErrorCode.getFailure("厅不存在或被删除！");
		if(StringUtils.isBlank(linelist) && StringUtils.isBlank(ranklist) ) return ErrorCode.getFailure("参数错误！");
		List<Integer> lineList = BeanUtil.getIntgerList(linelist, ",");
		List<Integer> rankList = BeanUtil.getIntgerList(ranklist, ",");
		List<TheatreRoomSeat> theatreRooSeatList = new ArrayList<TheatreRoomSeat>();
		TheatreRoomSeat seat = null;
		if(!lineList.isEmpty() && !rankList.isEmpty()){
			for (Integer line : lineList) {
				for (Integer rank : rankList) {
					seat = new TheatreRoomSeat(roomid, line, rank);
					seat.setSeatline(String.valueOf(line));
					seat.setSeatrank(String.valueOf(rank));
					theatreRooSeatList.add(seat);
				}
			}
		}else if(lineList.isEmpty() && !rankList.isEmpty()){
			for (Integer rank : rankList) {
				for (int line = 1; line <= room.getLinenum(); line++) {
					seat = new TheatreRoomSeat(roomid, line, rank);
					seat.setSeatline(String.valueOf(line));
					seat.setSeatrank(String.valueOf(rank));
					theatreRooSeatList.add(seat);
				}
			}
		}else if(!lineList.isEmpty() && rankList.isEmpty()){
			for (Integer line : lineList) {
				for (int rank = 1; rank <= room.getRanknum(); rank++) {
					seat = new TheatreRoomSeat(roomid, line, rank);
					seat.setSeatline(String.valueOf(line));
					seat.setSeatrank(String.valueOf(rank));
					theatreRooSeatList.add(seat);
				}
			}
		}
		try {
			baseDao.saveObjectList(theatreRooSeatList);
		} catch (Exception e) {
			return ErrorCode.getFailure("不能重复添加相同座位！");
		}
		room.setSynchtime(new Timestamp(System.currentTimeMillis()));
		baseDao.saveObject(room);
		return ErrorCode.getSuccessReturn(theatreRooSeatList.size());
	}
	@Override
	public ErrorCode batchDelBaseSeat(Long roomid, String linelist, String ranklist, Long userid){
		if(userid == null) return ErrorCode.getFailure("请先登录！");
		TheatreRoom room = baseDao.getObject(TheatreRoom.class, roomid);
		if(room == null) return ErrorCode.getFailure("厅不存在或被删除！");
		if(StringUtils.isBlank(linelist) && StringUtils.isBlank(ranklist)) return ErrorCode.getFailure("参数错误！");
		String lines = StringUtils.join(BeanUtil.getIntgerList(linelist, ","), ",");
		String rows = StringUtils.join(BeanUtil.getIntgerList(ranklist, ","), ",");
		String delelte = "delete TheatreRoomSeat where roomid = ? " ;
		if(StringUtils.isNotBlank(lines)) delelte += " and lineno in (" + lines + ") ";
		if(StringUtils.isNotBlank(rows)) delelte += " and rankno in (" + rows + ") ";
		int locknum = hibernateTemplate.bulkUpdate(delelte, roomid);
		room.setSynchtime(new Timestamp(System.currentTimeMillis()));
		baseDao.saveObject(room);
		return ErrorCode.getSuccessReturn(locknum);
	}
	@Override
	public ErrorCode updateSeatInitStatus(Long seatid, String initstatus) {
		if(!TheatreRoomSeat.isValidStatus(initstatus)) return ErrorCode.getFailure("状态不合法！");
		TheatreRoomSeat seat = baseDao.getObject(TheatreRoomSeat.class, seatid);
		seat.setInitstatus(initstatus);
		return ErrorCode.SUCCESS;
	}
	@Override
	public ErrorCode updateSeatLoveInd(Long seatid, String loveInd) {
		TheatreRoomSeat seat = baseDao.getObject(TheatreRoomSeat.class, seatid);
		TheatreRoomSeat another = null;
		if("0".equals(seat.getLoveInd()) || "1".equals(seat.getLoveInd())){
			another = getAnotherLoveSeat(seat, 1); 
		}else{
			another = getAnotherLoveSeat(seat, -1);
		}
		if("0".equals(loveInd)){//清除
			if(!"0".equals(seat.getLoveInd())){//原来设置过了
				seat.setLoveInd("0");
				another.setLoveInd("0");
				baseDao.saveObjectList(seat, another);
				return ErrorCode.getSuccessReturn(""+another.getId());
			}
		}else{
			if("0".equals(seat.getLoveInd())){//没设置
				if(another == null || !"0".equals(another.getLoveInd())) return ErrorCode.getFailure("旁边座位状态错误！");
				seat.setLoveInd("1");
				another.setLoveInd("2");
				baseDao.saveObjectList(seat, another);
				return ErrorCode.getSuccessReturn(""+another.getId());
			}
		}
		return ErrorCode.getFailure("状态未改变");
	}
	private TheatreRoomSeat getAnotherLoveSeat(TheatreRoomSeat seat, Integer add){
		String query = "from TheatreRoomSeat t where t.roomid=? and t.lineno=? and t.rankno=? ";
		List<TheatreRoomSeat> seatList = hibernateTemplate.find(query, seat.getRoomid(), seat.getLineno(), seat.getRankno()+add);
		if(seatList.size() > 0) return seatList.get(0);
		return null;
	}
	@Override
	public List<OpenDramaItem> getOdiList(String citycode, Long theatreid, Long dramaid,
			Timestamp from, Timestamp to, boolean open) {
		DetachedCriteria query = DetachedCriteria.forClass(OpenDramaItem.class);
		if(theatreid != null) query.add(Restrictions.eq("theatreid", theatreid));
		if(dramaid != null) query.add(Restrictions.eq("dramaid", dramaid));
		if(StringUtils.isNotBlank(citycode)) query.add(Restrictions.eq("citycode", citycode));
		if(from != null) query.add(Restrictions.ge("playtime", from));
		if(to != null) query.add(Restrictions.le("playtime", to));
		if(open){
			Timestamp cur = new Timestamp(System.currentTimeMillis());
			query.add(Restrictions.le("opentime", cur));
			query.add(Restrictions.ge("closetime", cur));
			query.add(Restrictions.eq("status", OdiConstant.STATUS_BOOK));
		}
		query.addOrder(Order.asc("sortnum"));
		query.addOrder(Order.asc("playtime"));
		List<OpenDramaItem> result = hibernateTemplate.findByCriteria(query);
		return result;
	}
	
	@Override
	public List<Drama> getCurTheatreDrama(Long theatreid, int from, int maxnum) {
		List<Long> idList = getCurDramaidList(theatreid);
		Collections.sort(idList);
		idList = BeanUtil.getSubList(idList, from, maxnum);
		List<Drama> dramaList = baseDao.getObjectList(Drama.class, idList);
		return dramaList;
	}
	
	@Override
	public List<Long> getCurDramaidList(Long theatreid){
		Timestamp cur = DateUtil.getCurFullTimestamp();
		return getDramaidByTheatreid(theatreid, cur);
	}
	
	private List<Long> getDramaidByTheatreid(Long theatreid, Timestamp playtime){
		DetachedCriteria query = DetachedCriteria.forClass(OpenDramaItem.class);
		if(theatreid != null) query.add(Restrictions.eq("theatreid", theatreid));
		query.setProjection(Projections.distinct(Projections.property("dramaid")));
		query.add(Restrictions.ge("playtime",playtime));
		query.add(Restrictions.ne("status",OdiConstant.STATUS_DISCARD));
		List<Long> dramaIdList = hibernateTemplate.findByCriteria(query);
		return dramaIdList;
	}
	
	@Override
	public List<Drama> getCurPlayDrama(String citycode, int from, int maxnum) {
		List<Long> idList = getCurDramaidList(citycode);
		Collections.sort(idList);
		idList = BeanUtil.getSubList(idList, from, maxnum);
		List<Drama> dramaList = baseDao.getObjectList(Drama.class, idList);
		return dramaList;
	}
	
	@Override
	public List<Long> getCurDramaidList(String citycode){
		return getCurDramaidList(citycode, null);
	}
	
	@Override
	public List<Long> getCurDramaidList(String citycode, String opentype){
		List<Long> dramaIdList = getDramaIdList(citycode, opentype);
		return dramaIdList;
	}
	
	@Override
	public List<String> getCitycodeList(){
		String key = CacheConstant.buildKey("getCitycodeList");
		List<String> citycodeList = (List<String>) cacheService.get(CacheConstant.REGION_ONEMIN, key);
		if(CollectionUtils.isEmpty(citycodeList)){
			Timestamp curtime = DateUtil.getCurFullTimestamp();
			DetachedCriteria qry = DetachedCriteria.forClass(OpenDramaItem.class, "d");
			Conjunction con1 = Restrictions.conjunction();
			con1.add(Restrictions.gt("d.playtime", curtime));
			con1.add(Restrictions.eq("d.period", Status.Y));
			Conjunction con2 = Restrictions.conjunction();
			con2.add(Restrictions.gt("d.endtime", curtime));
			con2.add(Restrictions.eq("d.period", Status.N));
			qry.add(Restrictions.or(con1, con2));
			qry.add(Restrictions.eq("d.status", OdiConstant.STATUS_BOOK));
			qry.add(Restrictions.le("d.opentime", curtime));
			qry.add(Restrictions.ge("d.closetime", curtime));
			qry.setProjection(Projections.projectionList().add(Projections.groupProperty("citycode"),"citycode")
					.add(Projections.count("citycode"), "citycodenum"));
			qry.setResultTransformer(DetachedCriteria.ALIAS_TO_ENTITY_MAP);
	 		List<Map> result = hibernateTemplate.findByCriteria(qry);
	 		Collections.sort(result, new MultiPropertyComparator(new String[]{"citycodenum"}, new boolean[]{false}));
			citycodeList = BeanUtil.getBeanPropertyList(result, String.class, "citycode", true);
			cacheService.set(CacheConstant.REGION_ONEMIN, key, citycodeList);
		}
		return citycodeList;
	}
	
	private List<Long> getDramaIdList(String citycode, String opentype){
		String key = CacheConstant.buildKey("getDramaIdList20130604", citycode, opentype);
		List<Long> idList = (List<Long>) cacheService.get(CacheConstant.REGION_ONEMIN, key);
		if(idList == null){
			Timestamp curtime = DateUtil.getCurFullTimestamp();
			DetachedCriteria qry = DetachedCriteria.forClass(OpenDramaItem.class, "d");
			Conjunction con1 = Restrictions.conjunction();
			con1.add(Restrictions.gt("d.playtime", curtime));
			con1.add(Restrictions.eq("d.period", Status.Y));
			Conjunction con2 = Restrictions.conjunction();
			con2.add(Restrictions.gt("d.endtime", curtime));
			con2.add(Restrictions.eq("d.period", Status.N));
			qry.add(Restrictions.or(con1, con2));
			qry.add(Restrictions.eq("d.status", OdiConstant.STATUS_BOOK));
			if(StringUtils.isNotBlank(opentype)){
				qry.add(Restrictions.eq("d.opentype", StringUtils.trim(opentype)));
			}
			qry.add(Restrictions.le("d.opentime", curtime));
			qry.add(Restrictions.ge("d.closetime", curtime));
			qry.add(Restrictions.eq("d.citycode", citycode));
			qry.setProjection(Projections.distinct(Projections.property("d.dramaid")));
			idList = hibernateTemplate.findByCriteria(qry); 
			cacheService.set(CacheConstant.REGION_ONEMIN, key, idList);
		}
		return idList;
	}
	
	@Override
	public List<Drama> getDramaByStarid(Long starid, int from, int maxnum){
		DetachedCriteria query =  DetachedCriteria.forClass(Drama.class, "d");
		DetachedCriteria subquery = DetachedCriteria.forClass(DramaToStar.class, "dts");
		subquery.add(Restrictions.eqProperty("dts.dramaid", "d.id"));
		subquery.add(Restrictions.eq("dts.starid", starid));
		subquery.setProjection(Projections.property("dts.starid"));
		query.add(Subqueries.exists(subquery));
		query.addOrder(Order.desc("releasedate"));
		List<Drama> dramaList = hibernateTemplate.findByCriteria(query, from, maxnum);
		return dramaList;
	}
	
	@Override
	public List<Drama> getPlayDramaByStarid(Long starid){
		return getPlayDramaByStarid(starid, true, -1, -1);
	}
	@Override
	public List<Drama> getPlayDramaByStarid(Long starid, boolean iscurrent, int from, int maxnum){
		Timestamp current = DateUtil.getCurTruncTimestamp();
		DetachedCriteria query = DetachedCriteria.forClass(DramaPlayItem.class, "op");
		DetachedCriteria subquery = DetachedCriteria.forClass(DramaToStar.class, "dts");
		subquery.add(Restrictions.eqProperty("dts.dramaid", "op.dramaid"));
		subquery.add(Restrictions.eq("dts.starid", starid));
		subquery.setProjection(Projections.property("dts.dramaid"));
		query.add(Subqueries.exists(subquery));
		if(iscurrent){
			// 当前正在上映
			query.add(Restrictions.ne("status", OdiConstant.STATUS_DISCARD));
			query.add(Restrictions.ge("op.playtime", current));
		}else{
			// 历史话剧
			query.add(Restrictions.le("op.playtime", current));
		}
		query.setProjection(Projections.groupProperty("op.dramaid"));
		List<Long> idlist = hibernateTemplate.findByCriteria(query, from, maxnum);
		List<Drama> dramalist = baseDao.getObjectList(Drama.class, idlist);
		return dramalist;
	}
	@Override
	public Integer getDramaOrderCount(Long itemid){
		DetachedCriteria qry = DetachedCriteria.forClass(DramaOrder.class);
		qry.add(Restrictions.eq("dpid", itemid));
		qry.setProjection(Projections.rowCount());
		List list = hibernateTemplate.findByCriteria(qry);
		if (list.isEmpty()) return 0;
		return Integer.parseInt(""+list.get(0));
	}
	@Override
	public ErrorCode removeOpenDramaItem(Long itemid){
		OpenDramaItem item = baseDao.getObjectByUkey(OpenDramaItem.class, "dpid", itemid, false);
		if(item==null) return ErrorCode.getFailure("场次不存在！");
		int count = getDramaOrderCount(itemid);
		if(count>0) return ErrorCode.getFailure("本场次已经有订单，不能删除！");
		DramaPlayItem dpi = baseDao.getObject(DramaPlayItem.class, itemid);
		dpi.setStatus(Status.N);
		item.setStatus(Status.DEL);
		baseDao.saveObject(item);
		baseDao.saveObject(dpi);
		return ErrorCode.SUCCESS;
	}
	@Override
	public boolean isSupportBooking(String citycode, Long theatreid, Long dramaid, Timestamp starttime,
			Timestamp endtime) {
		int count = getSupportBookingCount(citycode, theatreid, dramaid, starttime, endtime);
		return count> 0;
	}
	
	private int getSupportBookingCount(String citycode, Long theatreid, Long dramaid, Timestamp starttime, Timestamp endtime){
		Timestamp cur = new Timestamp(System.currentTimeMillis());
		DetachedCriteria qry = DetachedCriteria.forClass(OpenDramaItem.class);
		if(theatreid!=null) qry.add(Restrictions.eq("theatreid", theatreid));
		if(dramaid!=null) qry.add(Restrictions.eq("dramaid", dramaid));
		if(StringUtils.isNotBlank(citycode)) qry.add(Restrictions.eq("citycode", citycode));
		if(starttime!=null) qry.add(Restrictions.gt("playtime", starttime));
		else qry.add(Restrictions.gt("playtime", cur));
		
		if(endtime!=null) qry.add(Restrictions.le("playtime", endtime));
		
		qry.add(Restrictions.le("opentime", cur));
		qry.add(Restrictions.ge("closetime", cur));
		qry.add(Restrictions.eq("status", OdiConstant.STATUS_BOOK));
		qry.setProjection(Projections.rowCount());
		List list = hibernateTemplate.findByCriteria(qry);
		if (list.isEmpty()) return 0;
		return Integer.parseInt(""+list.get(0));
	}
	
	@Override
	public List<Date> getDramaOpenDateList(Long dramaid, Timestamp starttime, Timestamp endtime, boolean isPartner){
		String key = CacheConstant.buildKey("getDramaOpenDateList", dramaid, starttime, endtime, isPartner);
		List<String> playdateList = (List<String>) cacheService.get(CacheConstant.REGION_TWENTYMIN, key);
		if(playdateList == null){
			Timestamp cur = new Timestamp(System.currentTimeMillis());
			Timestamp playtime = cur;
			if(cur.before(starttime)){
				playtime = starttime;
			}else {
				endtime = DateUtil.getNextMonthFirstDay(cur);
			}
			String query = "select distinct to_char(o.playtime,'yyyy-mm-dd') from OpenDramaItem o " +
					"where o.dramaid=? and o.playtime >= ? and o.playtime < ? " +
					"and o.opentime< ? and o.closetime >? ";
			if(isPartner) { 
				query = query + " and o.status like ? and o.partner=? ";
				playdateList = hibernateTemplate.find(query, dramaid, playtime, endtime, cur, cur, Status.Y+"%", OpiConstant.STATUS_BOOK);
			}else {
				query = query + " and o.status=? ";
				playdateList = hibernateTemplate.find(query, dramaid,  playtime, endtime, cur, cur, OpiConstant.STATUS_BOOK);
			}
			Collections.sort(playdateList);
			cacheService.set(CacheConstant.REGION_TWENTYMIN, key, playdateList);
		}
		List<Date> result = new ArrayList<Date>();
		for(String date:playdateList) result.add(DateUtil.parseDate(date));
		return result;
	}
	@Override
	public ErrorCode batchAddPriceSeat(Long itemid, Long areaid, String seattype, String rankno, String lineno, Long userid) {
		if(userid==null) return ErrorCode.getFailure("请先登录！");
		if(StringUtils.isNotBlank(seattype) && (seattype.length()!=1 || "ABCDEFGHI".indexOf(seattype) < 0))
			return ErrorCode.getFailure("座位类型不正确！");
		List<Integer> lines = null;
		List<Integer> ranks = null;
		if(StringUtils.isNotBlank(lineno)) lines = BeanUtil.getIntgerList(lineno, ",");
		if(StringUtils.isNotBlank(rankno)) ranks = BeanUtil.getIntgerList(rankno, ",");
		if(lines == null && ranks == null) 
			return ErrorCode.getFailure("请输入行号与列号！");
		TheatreSeatPrice tsp = getTheatreSeatPriceBySeatType(itemid, areaid, seattype);
		DetachedCriteria query = DetachedCriteria.forClass(OpenTheatreSeat.class);
		query.add(Restrictions.eq("dpid", itemid));
		query.add(Restrictions.eq("areaid", areaid));
		if(lines != null) query.add(Restrictions.in("lineno", lines));
		if(ranks != null) query.add(Restrictions.in("rankno", ranks));
		List<OpenTheatreSeat> seatList = hibernateTemplate.findByCriteria(query);
		if(seatList.size() == 0) return ErrorCode.getFailure("没有可更改价格的座位！");
		List<OpenTheatreSeat> newseatList = new ArrayList<OpenTheatreSeat>();
//		Set<Long> idSet = new HashSet<Long>();
		for(OpenTheatreSeat seat : seatList){
			if(seat.isAvailable() || seat.isLocked()){
				ChangeEntry changeEntry = new ChangeEntry(seat);
				seat.setPrice(tsp.getPrice());
				seat.setCostprice(tsp.getCostprice());
				seat.setTheatreprice(tsp.getTheatreprice());
				seat.setSeattype(seattype);
				seat.setRemark("[价"+seattype+userid+"]"+StringUtils.defaultString(seat.getRemark()));
				newseatList.add(seat);
		//		idSet.add(seat.getOdiid());
				monitorService.saveChangeLog(userid, OpenTheatreSeat.class, seat.getId(),changeEntry.getChangeMap(seat));
			}
		}
		baseDao.updateObjectList(newseatList);
		/*for(Long id : idSet){
			OpenDramaItem odi = daoService.getObject(OpenDramaItem.class, id);
			odi.setSynchots(new Timestamp(System.currentTimeMillis()));
			daoService.saveObject(odi);
		}*/
		return ErrorCode.SUCCESS;
	}
	@Override
	public ErrorCode<OpenDramaItem> updateStatus(OpenDramaItem odi, String status, Long userid){
		if(userid == null) return ErrorCode.getFailure("请先登录！");
		if(odi == null || status == null) return ErrorCode.getFailure("参数错误！");
		DramaPlayItem item = null;
		if(StringUtils.contains(status, OdiConstant.STATUS_BOOK)){
			item = baseDao.getObject(DramaPlayItem.class, odi.getDpid());
			List<TheatreSeatPrice> tspList =dramaPlayItemService.getTspList(item.getId(), null);
			TspHelper tspHelper = new TspHelper(tspList);
			if(tspHelper.getTspListBySno().isEmpty()){
				TheatreField section = baseDao.getObject(TheatreField.class, item.getRoomid());
				return ErrorCode.getFailure(section.getName()+"没有加入价格，不能开放！");
			}
			ErrorCode<String> code = validOpenDramaItem(odi);
			if(!code.isSuccess()) return ErrorCode.getFailure(code.getErrcode(), code.getMsg());
		}else if(StringUtils.equals(status, Status.N)){
			item = baseDao.getObject(DramaPlayItem.class, odi.getDpid());
		}
		ChangeEntry changeEntry = new ChangeEntry(odi);
		odi.setStatus(status);
		odi.setUpdatetime(new Timestamp(System.currentTimeMillis()));
		baseDao.saveObject(odi);
		if(item != null){
			ChangeEntry changeEntry2 = new ChangeEntry(item);
			if(StringUtils.equals(status, Status.N)) status = Status.Y;
			item.setStatus(status);
			baseDao.saveObject(item);
			monitorService.saveChangeLog(userid, DramaPlayItem.class, item.getId(), changeEntry2.getChangeMap(item));
		}else if(StringUtils.equals(status, Status.DEL)){
			item = baseDao.getObject(DramaPlayItem.class, odi.getDpid());
			ChangeEntry changeEntry2 = new ChangeEntry(item);
			item.setStatus(Status.N);
			baseDao.saveObject(item);
			monitorService.saveChangeLog(userid, DramaPlayItem.class, item.getId(), changeEntry2.getChangeMap(item));
		}
		monitorService.saveChangeLog(userid, OpenDramaItem.class, odi.getId(),changeEntry.getChangeMap( odi));
		return ErrorCode.getSuccessReturn(odi);
	}
	
	@Override
	public ErrorCode<OpenDramaItem> updatePartner(OpenDramaItem odi, String partner, Long userid){
		if(userid == null) return ErrorCode.getFailure("请先登录！");
		if(odi == null || partner == null) return ErrorCode.getFailure("参数错误！");
		ChangeEntry changeEntry = new ChangeEntry(odi);
		DramaPlayItem item = baseDao.getObject(DramaPlayItem.class, odi.getDpid());
		ChangeEntry changeEntry2 = new ChangeEntry(item);
		odi.setPartner(partner);
		baseDao.saveObject(odi);
		item.setPartner(partner);
		baseDao.saveObject(item);
		monitorService.saveChangeLog(userid, OpenDramaItem.class, odi.getId(),changeEntry.getChangeMap( odi));
		monitorService.saveChangeLog(userid, DramaPlayItem.class, item.getId(), changeEntry2.getChangeMap(item));
		return ErrorCode.getSuccessReturn(odi);
	}
	
	@Override
	public Integer getOdiCountByTheatreid(Long theatreid, Long dramaid, String opentype){
		int count = getOdiCount(theatreid, dramaid, opentype);
		return count;
	}
	
	private int getOdiCount(Long theatreid, Long dramaid, String opentype){
		Timestamp cur = new Timestamp(System.currentTimeMillis());
		DetachedCriteria query = DetachedCriteria.forClass(OpenDramaItem.class);
		if(StringUtils.isNotBlank(opentype)) query.add(Restrictions.eq("opentype", opentype));
		if(theatreid != null) query.add(Restrictions.eq("theatreid", theatreid));
		if(dramaid != null) query.add(Restrictions.eq("dramaid", dramaid));
		query.add(Restrictions.le("opentime", cur));
		query.add(Restrictions.ge("closetime", cur));
		query.add(Restrictions.eq("status", OdiConstant.STATUS_BOOK));
		query.setProjection(Projections.rowCount());
		List list = hibernateTemplate.findByCriteria(query);
		if (list.isEmpty()) return 0;
		return Integer.parseInt(""+list.get(0));
	}
	
	//TODO: 类别??????
	@Override
	public List<Drama> getCurPlayDramaByType(String citycode, String dramatype, int from, int maxnum) {
		Timestamp curtime = new Timestamp(System.currentTimeMillis());
		DetachedCriteria qry = DetachedCriteria.forClass(Drama.class, "d");
		DetachedCriteria subQry = DetachedCriteria.forClass(OpenDramaItem.class, "o");
		subQry.add(Restrictions.gt("o.playtime", curtime));
		subQry.add(Restrictions.ne("o.status", OdiConstant.STATUS_DISCARD));
		subQry.add(Restrictions.eqProperty("o.dramaid", "d.id"));
		subQry.setProjection(Projections.property("o.id"));
		
		qry.add(Subqueries.exists(subQry));
		if(StringUtils.isNotBlank(dramatype)){
			if(StringUtils.equals(dramatype, DramaConstant.TYPE_DRAMA)){
				qry.add(Restrictions.eq("d.dramatype", DramaConstant.TYPE_DRAMA));
			}else{
				qry.add(Restrictions.ne("d.dramatype", DramaConstant.TYPE_DRAMA));
			}
		}
		qry.add(Restrictions.eq("d.citycode", citycode));
		List<Drama> dramaList = hibernateTemplate.findByCriteria(qry, from, maxnum);
		return dramaList;
	}
	
	@Override
	public List<Long> getOpenDramaItemId(List dramaid, Timestamp lasttime){
		if(dramaid.isEmpty()) return new ArrayList<Long>();
		DetachedCriteria query = DetachedCriteria.forClass(OpenDramaItem.class);
		query.add(Restrictions.in("dramaid", dramaid));
		query.add(Restrictions.gt("synchots", lasttime));
		query.setProjection(Projections.property("id"));
		List<Long> odiidList = hibernateTemplate.findByCriteria(query);
		return odiidList;
	}
	@Override
	public ErrorCode<String> validOpenDramaItem(OpenDramaItem odi){
		Long dpid = odi.getDpid();
		List<TheatreSeatPrice> priceList = baseDao.getObjectListByField(TheatreSeatPrice.class, "dpid", dpid);
		String name = odi.getDramaname();
		if(StringUtils.equals(odi.getPeriod(), Status.Y)) name = name + ","+ DateUtil.formatTimestamp(odi.getPlaytime());
		if(priceList.size()==0){
			return ErrorCode.getFailure(name + "没有设置价格");
		}
		for(TheatreSeatPrice price : priceList){
			if((price.getPrice()<=0 || price.getTheatreprice()<=0 || price.getCostprice()<=0) && !StringUtils.equals(price.getStatus(), Status.DEL)){
				return ErrorCode.getFailure(name + ", 价格类型：" + price.getSeattype()  + ", 卖价: " + price.getPrice() + ", 成本价：" + price.getCostprice() + ", 场馆价：" + price.getTheatreprice() + "有价格为0, priceid=" + price.getId());
			}
		}
		if(odi.isOpenseat()){
			List<OpenTheatreSeat> seatList = baseDao.getObjectListByField(OpenTheatreSeat.class, "dpid", dpid);
			if(seatList.size()==0) return ErrorCode.getFailure(name + "没有设置座位");
			String qry = "select count(*) from OpenTheatreSeat s where s.dpid=? and (costprice<=0 or price<=0 or theatreprice<=0)";
			List list = hibernateTemplate.find(qry, dpid);
			if(Integer.valueOf(list.get(0)+"")>0){
				return ErrorCode.getFailure(name + "座位价格为0");
			}
		}
		return ErrorCode.SUCCESS;
	}

	@Override
	public ErrorCode updateTheatreSeatPrice(TheatreSeatPrice seatprice, String status, Timestamp updatetime, Long userid){
		if(seatprice == null) return ErrorCode.getFailure("价格不能为空！");
		if(StringUtils.isBlank(status)){
			return ErrorCode.getFailure("状态不能为空！");
		}
		if(!OdiConstant.STATUS_LIST.contains(status)){
			return ErrorCode.getFailure("状态不对！");
		}
		ChangeEntry changeEntry = new ChangeEntry(seatprice);
		final String oldStatus = seatprice.getStatus();
		seatprice.setStatus(status);
		if(!changeEntry.getChangeMap(seatprice).isEmpty()){
			seatprice.setUpdatetime(updatetime);
			baseDao.saveObject(seatprice);
			monitorService.saveChangeLog(userid, TheatreSeatPrice.class, seatprice.getId(),changeEntry.getChangeMap( seatprice));
			dbLogger.warn("用户：" + userid + "修改场次价格:" + seatprice.getId() +"," + oldStatus + "--->" + seatprice.getStatus());
			ErrorCode<Integer> code = null;
			if(StringUtils.equals(seatprice.getStatus(), OdiConstant.STATUS_BOOK)){
				code = batchUnLockSeatBySeatPrice(seatprice.getDpid(), seatprice.getAreaid(), seatprice, userid);
			}else{
				code = batchLockSeatBySeatPrice(seatprice.getDpid(), seatprice.getAreaid(), seatprice, TheatreSeatConstant.STATUS_LOCKB, userid);
			}
			if(code.isSuccess()){
				dbLogger.warn("修改场次价格：更新座位数" + code.getRetval());
			}
		}
		return ErrorCode.SUCCESS;
	}
}
