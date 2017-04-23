package com.gewara.web.action.admin.drama;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.gewara.model.acl.User;
import com.gewara.model.drama.Theatre;
import com.gewara.model.drama.TheatreRoom;
import com.gewara.model.drama.TheatreRoomSeat;
import com.gewara.service.drama.OpenDramaService;
import com.gewara.support.ErrorCode;
import com.gewara.util.DateUtil;
import com.gewara.web.action.admin.BaseAdminController;
@Controller
public class TheatreSeatAdminController extends BaseAdminController{
	@Autowired@Qualifier("openDramaService")
	private OpenDramaService openDramaService;
	public void setOpenDramaService(OpenDramaService openDramaService) {
		this.openDramaService = openDramaService;
	}
	@RequestMapping("/admin/dramaTicket/baseRoomseat.xhtml")
	public String roomseat(@RequestParam("rid")Long roomId, ModelMap model){
		TheatreRoom room = daoService.getObject(TheatreRoom.class, roomId);
		List<TheatreRoomSeat> seatList = openDramaService.getSeatList(roomId);
		Map<Integer, String> rowMap = new HashMap<Integer, String>();
		Map<String, TheatreRoomSeat> seatMap = new HashMap<String, TheatreRoomSeat>();
		for(TheatreRoomSeat seat:seatList){
			rowMap.put(seat.getLineno(), seat.getSeatline());
			seatMap.put("row" + seat.getLineno() + "rank" + seat.getRankno(), seat);
		}
		Theatre theatre = daoService.getObject(Theatre.class, room.getTheatreid());
		model.put("editable", true);
		model.put("theatre", theatre);
		model.put("seatMap", seatMap);
		model.put("rowMap", rowMap);
		model.put("room", room);
		return "admin/theatreticket/baseRoomseat.vm";
	}
	
	@RequestMapping("/admin/dramaTicket/updateTheatreRoomSeatMapStr.xhtml")
	public String updateTheatreRoomSeatMapStr(Long roomid, ModelMap model) {
		TheatreRoom room = daoService.getObject(TheatreRoom.class, roomid);
		if(room == null) return showJsonError(model, "该场区不存在或被删除！");
		String seatmap = openDramaService.getTheatreRoomSeatMapStr(room);
		room.setSeatmap(seatmap);
		room.setUpdatetime(DateUtil.getCurFullTimestamp());
		daoService.saveObject(room);
		return forwardMessage(model, seatmap);
	}
	
	@RequestMapping("/admin/dramaTicket/addBaseRowSeat.xhtml")
	public String addRowSeat(Long roomid, ModelMap model){
		boolean success = openDramaService.addRowSeat(roomid);
		if(!success) return showJsonError(model, "");
		return showJsonSuccess(model);
	}
	@RequestMapping("/admin/dramaTicket/batchAddBaseSeat.xhtml")
	public String batchAddBaseSeat(Long roomid, String linelist, String ranklist, ModelMap model){
		User user = getLogonUser();
		ErrorCode code = openDramaService.batchAddBaseSeat(roomid, linelist, ranklist, user.getId());
		if(!code.isSuccess()) return showJsonError(model, code.getMsg());
		return showJsonSuccess(model);
	}
	@RequestMapping("/admin/dramaTicket/batchDelBaseSeat.xhtml")
	public String batchDelBaseSeat(Long roomid, String linelist, String ranklist, ModelMap model){
		User user = getLogonUser();
		ErrorCode code = openDramaService.batchDelBaseSeat(roomid, linelist, ranklist, user.getId());
		if(!code.isSuccess()) return showJsonError(model, code.getMsg());
		return showJsonSuccess(model);
	}
	@RequestMapping("/admin/dramaTicket/addBaseRankSeat.xhtml")
	public String addRankSeat(Long roomid, ModelMap model){
		boolean success = openDramaService.addRankSeat(roomid);
		if(!success) return showJsonError(model, "");
		return showJsonSuccess(model);
	}
	@RequestMapping("/admin/dramaTicket/deleteBaseRowSeat.xhtml")
	public String deleteRowSeat(Long roomid, ModelMap model){
		boolean success = openDramaService.deleteRowSeat(roomid);
		if(!success) return showJsonError(model, "");
		return showJsonSuccess(model);
	}
	@RequestMapping("/admin/dramaTicket/deleteSeatline.xhtml")
	public String deleteSeatline(Long roomid, String seatline, ModelMap model){
		String update = "delete TheatreRoomSeat where roomid = ? and seatline = ? ";
		TheatreRoom section = daoService.getObject(TheatreRoom.class,roomid);
		int i = hibernateTemplate.bulkUpdate(update, roomid, seatline);
		section.setLinenum(section.getLinenum() - 1);
		section.setSynchtime(new Timestamp(System.currentTimeMillis()));
		daoService.saveObject(section);
		return forwardMessage(model, i+"个数据删除");
	}
	
	
	@RequestMapping("/admin/dramaTicket/deleteBaseRankSeat.xhtml")
	public String deleteRankSeat(Long roomid, ModelMap model){
		boolean success = openDramaService.deleteRankSeat(roomid);
		if(!success) return showJsonError(model, "");
		return showJsonSuccess(model);
	}
	@RequestMapping("/admin/dramaTicket/addBaseSeat.xhtml")
	public String addSeat(Long roomid, String location, String seatline, String seatrank, ModelMap model){
		int line = Integer.parseInt(location.substring(1, location.indexOf('R')));
		int rank = Integer.parseInt(location.substring(location.indexOf('R')+1));
		TheatreRoomSeat seat = openDramaService.getRoomSeatByLocation(roomid, line, rank);
		if(seat==null){
			seat = new TheatreRoomSeat(roomid, line, rank, line+"", rank+"");
			if(StringUtils.isNotBlank(seatline)) seat.setSeatline(seatline);
			if(StringUtils.isNotBlank(seatrank)) seat.setSeatrank(seatrank);
			daoService.saveObject(seat);
		}
		TheatreRoom room = daoService.getObject(TheatreRoom.class, roomid);
		room.setSynchtime(new Timestamp(System.currentTimeMillis()));
		daoService.saveObject(room);
		return showJsonSuccess(model);
	}

	@RequestMapping("/admin/dramaTicket/clearBaseSeat.xhtml")
	public String clearSeat(Long roomid, String location, ModelMap model){
		int line = Integer.parseInt(location.substring(1, location.indexOf('R')));
		int rank = Integer.parseInt(location.substring(location.indexOf('R')+1));
		TheatreRoomSeat seat = openDramaService.getRoomSeatByLocation(roomid, line, rank);
		if(seat!=null) daoService.removeObject(seat);
		TheatreRoom room = daoService.getObject(TheatreRoom.class, roomid);
		room.setSynchtime(new Timestamp(System.currentTimeMillis()));
		daoService.saveObject(room);
		return showJsonSuccess(model);
	}
	@RequestMapping("/admin/dramaTicket/setBaseSeatRankNo.xhtml")
	public String setSeatRankNo(Long roomid, String location, String rankno, ModelMap model){
		int line = Integer.parseInt(location.substring(1, location.indexOf('R')));
		int rank = Integer.parseInt(location.substring(location.indexOf('R')+1));
		TheatreRoomSeat seat = openDramaService.getRoomSeatByLocation(roomid, line, rank);
		if(seat!=null){
			seat.setSeatrank(rankno);
			daoService.saveObject(seat);
		}
		TheatreRoom room = daoService.getObject(TheatreRoom.class, roomid);
		room.setSynchtime(new Timestamp(System.currentTimeMillis()));
		daoService.saveObject(room);
		return showJsonSuccess(model);
	}
	@RequestMapping("/admin/dramaTicket/changeBaseSeatLine.xhtml")
	public String changeSeatLine(Long roomid, int lineno, String newline, ModelMap model){
		boolean success = openDramaService.updateSeatLine(roomid, lineno, newline);
		if(!success) return showJsonError(model, "");
		return showJsonSuccess(model);
	}
	
	@RequestMapping("/admin/dramaTicket/changeBaseSeatRank.xhtml")
	public String changeSeatRank(Long roomid, int rankno, String newrank, ModelMap model){
		boolean success = openDramaService.updateSeatRank(roomid, rankno, newrank);
		if(!success) return showJsonError(model, "");
		return showJsonSuccess(model);
	}
	
	@RequestMapping("/admin/dramaTicket/seatInitstatus.xhtml")
	public String seatInitstatus(@RequestParam("rid")Long roomId, ModelMap model){
		TheatreRoom room = daoService.getObject(TheatreRoom.class, roomId);
		model.put("room", room);
		List<TheatreRoomSeat> seatList = openDramaService.getSeatList(roomId);
		Map<Integer, String> rowMap = new HashMap<Integer, String>();
		Map<String, TheatreRoomSeat> seatMap = new HashMap<String, TheatreRoomSeat>();
		for(TheatreRoomSeat seat:seatList){
			rowMap.put(seat.getLineno(), seat.getSeatline());
			seatMap.put("row" + seat.getLineno() + "rank" + seat.getRankno(), seat);
		}
		Theatre theatre = daoService.getObject(Theatre.class, room.getTheatreid());
		model.put("theatre", theatre);
		model.put("seatMap", seatMap);
		model.put("rowMap", rowMap);
		return "admin/theatreticket/seatStatus.vm";
	}
	@RequestMapping("/admin/dramaTicket/setSeatInitstatus.xhtml")
	public String setSeatInitstatus(Long seatid, String initstatus, ModelMap model){
		ErrorCode code = openDramaService.updateSeatInitStatus(seatid, initstatus);
		if(!code.isSuccess()) return showJsonError(model, code.getMsg());
		return showJsonSuccess(model);
	}
	@RequestMapping("/admin/dramaTicket/setLoveInd.xhtml")
	public String setLoveInd(Long seatid, String loveInd, ModelMap model){
		ErrorCode code = openDramaService.updateSeatLoveInd(seatid, loveInd);
		if(!code.isSuccess()) return showJsonError(model, code.getMsg());
		return showJsonSuccess(model, code.getRetval()+"");
	}
}
