package com.gewara.web.action.admin.gewapay;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import com.gewara.json.TempRoomSeat;
import com.gewara.model.acl.User;
import com.gewara.model.movie.Cinema;
import com.gewara.model.movie.CinemaRoom;
import com.gewara.model.movie.RoomSeat;
import com.gewara.service.ticket.OpenPlayService;
import com.gewara.support.ErrorCode;
import com.gewara.untrans.ticket.TempRoomSeatService;
import com.gewara.util.BeanUtil;
import com.gewara.web.action.admin.BaseAdminController;

@Controller
public class TempRoomSeatAdminController extends BaseAdminController {
	
	public static final List<String> INIT_STATUS_LIST = Arrays.asList("O", "C");
	public static final List<String> TEMP_LIST = Arrays.asList("A", "B", "C", "D", "E", "F");
	
	@Autowired@Qualifier("openPlayService")
	private OpenPlayService openPlayService;
	public void setOpenPlayService(OpenPlayService openPlayService) {
		this.openPlayService = openPlayService;
	}
	@Autowired@Qualifier("tempRoomSeatService")
	private TempRoomSeatService tempRoomSeatService;
	public void setTempRoomSeatService(TempRoomSeatService tempRoomSeatService){
		this.tempRoomSeatService = tempRoomSeatService;
	}
	
	@RequestMapping("/admin/ticket/seatTempInitstatus.xhtml")
	public String seatTempInitstatus(Long roomId, String tmpname, HttpServletRequest request, ModelMap model){
		CinemaRoom room = daoService.getObject(CinemaRoom.class, roomId);
		if(room == null) return showMessageAndReturn(model, request, "该影厅不存在或被删除！");
		model.put("room", room);
		List<RoomSeat> seatList = openPlayService.getSeatListByRoomId(roomId);
		Map<Integer, String> rowMap = new HashMap<Integer, String>();
		Map<String, RoomSeat> seatMap = new HashMap<String, RoomSeat>();
		TempRoomSeat tempRoomSeat = null;
		List<TempRoomSeat> roomSeatList = tempRoomSeatService.getRoomSeatList(roomId);
		model.put("roomSeatList", roomSeatList);
		if(StringUtils.isNotBlank(tmpname)){
			tempRoomSeat = tempRoomSeatService.getRoomSeat(roomId, tmpname);
		}else{
			if(!roomSeatList.isEmpty()){
				tempRoomSeat = roomSeatList.get(0);
			}
		}
		List<String> lockSeatList = new ArrayList<String>();
		if(tempRoomSeat != null && StringUtils.isNotBlank(tempRoomSeat.getSeatbody())){
			String seatbody = tempRoomSeat.getSeatbody();
			List<String> tmpList = Arrays.asList(StringUtils.split(seatbody, ","));
			for (String tmp : tmpList) {
				lockSeatList.add("row" + StringUtils.replace(tmp, ":", "rank"));
			}
		}
		model.put("curTempSeat", tempRoomSeat);
		model.put("lockSeatList", lockSeatList);
		for(RoomSeat seat:seatList){
			rowMap.put(seat.getLineno(), seat.getSeatline());
			seatMap.put("row" + seat.getLineno() + "rank" + seat.getRankno(), seat);
		}
		Cinema cinema = daoService.getObject(Cinema.class, room.getCinemaid());
		model.put("cinema", cinema);
		model.put("seatMap", seatMap);
		model.put("rowMap", rowMap);
		return "admin/ticket/tempSeatStatus.vm";
	}
	
	@RequestMapping("/admin/ticket/mpi/setTempSeatInitstatus.xhtml")
	public String setTempSeatInitstatus(Long roomid, String tmpname, String seatbody, String initstatus,  ModelMap model) {
		if(!INIT_STATUS_LIST.contains(initstatus)) return showJsonError(model, "开放或关闭类型错误！");
		User user = getLogonUser();
		boolean flag;
		if(StringUtils.equals(initstatus, "O")){
			flag = false;
		}else{
			flag = true;
		}
		ErrorCode code = tempRoomSeatService.updateRoomSeat(roomid, tmpname, seatbody, flag, user);
		if (code.isSuccess())
			return showJsonSuccess(model);
		return showJsonError(model, code.getMsg());
	}
	
	@RequestMapping("/admin/ticket/mpi/batchTempSeatInitstatus.xhtml")
	public String batchTempSeatInitstatus(Long roomid, String tmpname, String seatbody, String initstatus,  ModelMap model) {
		if(!INIT_STATUS_LIST.contains(initstatus)) return showJsonError(model, "开放或关闭类型错误！");
		User user = getLogonUser();
		boolean flag;
		if(StringUtils.equals(initstatus, "O")){
			flag = false;
		}else{
			flag = true;
		}
		ErrorCode code = tempRoomSeatService.batchUpdateRoomSeat(roomid, tmpname, seatbody, flag, user);
		if (code.isSuccess())
			return showJsonSuccess(model);
		return showJsonError(model, code.getMsg());
	}
	
	@RequestMapping("/admin/ticket/mpi/addTempRoomSeat.xhtml")
	public String addTempRoomSeat(Long roomid, String tmpname, ModelMap model){
		if(!TEMP_LIST.contains(tmpname)) return showJsonError(model, "模板名称错误,必须在“" + StringUtils.join(TEMP_LIST, ",") + "”之内！");
		User user = getLogonUser();
		ErrorCode<TempRoomSeat> code = tempRoomSeatService.addRoomSeat(roomid, tmpname, user);
		if(code.isSuccess()){
			return showJsonSuccess(model, BeanUtil.getBeanMap(code.getRetval()));
		}
		return showJsonError(model, code.getMsg());
	}
}
