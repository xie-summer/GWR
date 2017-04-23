package com.gewara.untrans.ticket.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.gewara.constant.sys.MongoData;
import com.gewara.json.TempRoomSeat;
import com.gewara.model.acl.User;
import com.gewara.model.movie.CinemaRoom;
import com.gewara.mongo.MongoService;
import com.gewara.service.DaoService;
import com.gewara.support.ErrorCode;
import com.gewara.support.MultiPropertyComparator;
import com.gewara.untrans.monitor.MonitorService;
import com.gewara.untrans.ticket.TempRoomSeatService;
import com.gewara.util.ChangeEntry;
import com.gewara.util.DateUtil;
import com.gewara.util.StringUtil;

@Service("tempRoomSeatService")
public class TempRoomSeatServiceImpl implements TempRoomSeatService {
	
	@Autowired@Qualifier("mongoService")
	private MongoService mongoService;
	public void setMongoService(MongoService mongoService){
		this.mongoService = mongoService;
	}
	@Autowired@Qualifier("monitorService")
	private MonitorService monitorService;
	public void setMonitorService(MonitorService monitorService) {
		this.monitorService = monitorService;
	}
	@Autowired@Qualifier("daoService")
	private DaoService daoService;

	@Override
	public ErrorCode<TempRoomSeat> addRoomSeat(Long roomid, String tmpname, User user){
		CinemaRoom room = daoService.getObject(CinemaRoom.class, roomid);
		if(room == null) return ErrorCode.getFailure("该影厅不存在或被删除！");
		if(StringUtils.isBlank(tmpname)) return ErrorCode.getFailure("模板名称不能为空！");
		String id = roomid + "_" + tmpname;
		TempRoomSeat tempRoomSeat = mongoService.getObject(TempRoomSeat.class, MongoData.DEFAULT_ID_NAME, id);
		if(tempRoomSeat != null) return ErrorCode.getFailure("重复影厅模板！");
		tempRoomSeat = new TempRoomSeat(roomid, tmpname);
		tempRoomSeat.setId(id);
		mongoService.saveOrUpdateObject(tempRoomSeat, MongoData.DEFAULT_ID_NAME);
		monitorService.saveAddLog(user.getId(), TempRoomSeat.class, tempRoomSeat.getId(), tempRoomSeat);
		return ErrorCode.getSuccessReturn(tempRoomSeat);
	}
	
	@Override
	public ErrorCode<String> updateRoomSeat(Long roomid, String tmpname, String seatbody, boolean add, User user){
		CinemaRoom room = daoService.getObject(CinemaRoom.class, roomid);
		if(room == null) return ErrorCode.getFailure("该影厅不存在或被删除！");
		if(StringUtils.isBlank(tmpname)) return ErrorCode.getFailure("模板名称不能为空！");
		if(StringUtils.isBlank(seatbody)) return ErrorCode.getFailure("座位数据不能为空！");
		String id = roomid + "_" + tmpname;
		TempRoomSeat tempRoomSeat = mongoService.getObject(TempRoomSeat.class, MongoData.DEFAULT_ID_NAME, id);
		if(tempRoomSeat == null) return ErrorCode.getFailure("该影厅模板数据不存");
		ChangeEntry changeEntry = new ChangeEntry(tempRoomSeat);
		tempRoomSeat.setUpdatetime(DateUtil.getCurFullTimestampStr());
		String tmp = StringUtil.findFirstByRegex(seatbody, SINGLE_PEX);
		if(StringUtils.isBlank(tmp)) return ErrorCode.getFailure("座位数据格式错误！");
		List<String> seatList = new ArrayList<String>(0);
		if(StringUtils.isNotBlank(tempRoomSeat.getSeatbody())){
			seatList.addAll(Arrays.asList(StringUtils.split(tempRoomSeat.getSeatbody(), ",")));
		}
		if(add){
			if(seatList.contains(tmp)) return ErrorCode.getFailure("重复座位数据！");
			seatList.add(tmp);
		}else{
			if(!seatList.contains(tmp)) return ErrorCode.getFailure("座位数据不存在！");
			seatList.remove(tmp);
		}
		String tmpBody = StringUtils.join(seatList, ",");
		tempRoomSeat.setSeatbody(tmpBody);
		mongoService.saveOrUpdateObject(tempRoomSeat, MongoData.DEFAULT_ID_NAME);
		monitorService.saveChangeLog(user.getId(), TempRoomSeat.class, tempRoomSeat.getId(), changeEntry.getChangeMap(tempRoomSeat));
		return ErrorCode.SUCCESS;
	}
	
	@Override
	public ErrorCode<String> batchUpdateRoomSeat(Long roomid, String tmpname, String seatbody, boolean add, User user){
		CinemaRoom room = daoService.getObject(CinemaRoom.class, roomid);
		if(room == null) return ErrorCode.getFailure("该影厅不存在或被删除！");
		if(StringUtils.isBlank(tmpname)) return ErrorCode.getFailure("模板名称不能为空！");
		if(StringUtils.isBlank(seatbody)) return ErrorCode.getFailure("座位数据不能为空！");
		Collection<String> tmpSeatList = StringUtil.findByRegex(seatbody, BATCH_PEX, true);
		if(tmpSeatList.isEmpty()) return ErrorCode.getFailure("座位数据格式错误！");
		String id = roomid + "_" + tmpname;
		TempRoomSeat tempRoomSeat = mongoService.getObject(TempRoomSeat.class, MongoData.DEFAULT_ID_NAME, id);
		if(tempRoomSeat == null) return ErrorCode.getFailure("该影厅模板数据不存");
		ChangeEntry changeEntry = new ChangeEntry(tempRoomSeat);
		tempRoomSeat.setUpdatetime(DateUtil.getCurFullTimestampStr());
		Collection<String> seatList = new ArrayList<String>(0);
		if(StringUtils.isNotBlank(tempRoomSeat.getSeatbody())){
			seatList.addAll(Arrays.asList(StringUtils.split(tempRoomSeat.getSeatbody(), ",")));
		}
		if(add){
			seatList = CollectionUtils.union(seatList,tmpSeatList);
		}else{
			seatList = CollectionUtils.disjunction(seatList, tmpSeatList);
		}
		String tmpBody = StringUtils.join(seatList, ",");
		tempRoomSeat.setSeatbody(tmpBody);
		tempRoomSeat.setSeatbody(seatbody);
		mongoService.saveOrUpdateObject(tempRoomSeat, MongoData.DEFAULT_ID_NAME);
		monitorService.saveChangeLog(user.getId(), TempRoomSeat.class, tempRoomSeat.getId(), changeEntry.getChangeMap(tempRoomSeat));
		return ErrorCode.SUCCESS;
	}
	
	
	
	@Override
	public TempRoomSeat getRoomSeat(Long roomid, String tmpname){
		String id = roomid + "_" + tmpname;
		TempRoomSeat tempRoomSeat = mongoService.getObject(TempRoomSeat.class, MongoData.DEFAULT_ID_NAME, id);
		return tempRoomSeat;
	}
	
	@Override
	public List<TempRoomSeat> getRoomSeatList(Long roomid){
		Map params = new HashMap();
		params.put("roomid", roomid);
		List<TempRoomSeat> roomSeatList = mongoService.find(TempRoomSeat.class, params);
		Collections.sort(roomSeatList, new MultiPropertyComparator(new String[]{"tmpname"}, new boolean[]{true}));
		return roomSeatList;
	}
}
