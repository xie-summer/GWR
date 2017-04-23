package com.gewara.web.action.admin.gptbs;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import com.gewara.api.gpticket.vo.ticket.FieldAreaSeatVo;
import com.gewara.constant.AdminCityContant;
import com.gewara.constant.OdiConstant;
import com.gewara.helper.UpdateDpiContainer;
import com.gewara.helper.sys.AdminCityHelper;
import com.gewara.model.acl.User;
import com.gewara.model.drama.DramaPlayItem;
import com.gewara.model.drama.Theatre;
import com.gewara.model.drama.TheatreField;
import com.gewara.model.drama.TheatreProfile;
import com.gewara.model.drama.TheatreRoom;
import com.gewara.model.drama.TheatreSeatArea;
import com.gewara.support.ErrorCode;
import com.gewara.untrans.drama.RemoteTheatreService;
import com.gewara.util.DateUtil;
import com.gewara.web.action.admin.BaseAdminController;

@Controller
public class RemoteDramaAdminController extends BaseAdminController {
	
	@Autowired@Qualifier("remoteTheatreService")
	private RemoteTheatreService remoteTheatreService;
	
	@RequestMapping("/admin/drama/remote/qryTheatre.xhtml")
	public String qryTheatre(HttpServletRequest request, ModelMap model){
		User user = getLogonUser();
		String citycode = getAdminCitycode(request);
		if(StringUtils.isBlank(citycode)) citycode = AdminCityContant.CITYCODE_SH;
		String hql = "select id from TheatreProfile tp where exists(select t.id from Theatre t where t.id=tp.id and t.citycode=?) and tp.opentype is not null and tp.opentype <>? "; 
		List<Long> theatreIdList = hibernateTemplate.find(hql, citycode, OdiConstant.PARTNER_GEWA);
		List<Theatre> theatreList = daoService.getObjectList(Theatre.class, theatreIdList);
		Map<Long, TheatreProfile> profileMap = daoService.getObjectMap(TheatreProfile.class, theatreIdList);
		model.put("theatreList", theatreList);
		model.put("profileMap", profileMap);
		model.put("user", user);
		List<AdminCityHelper> province2CityList = AdminCityHelper.province2CityListMap();
		model.put("province2CityList", province2CityList);
		return "admin/drama/remote/qryTheatre.vm";
	}
	
	private ErrorCode validTheatre(Long theatreid){
		TheatreProfile profile = daoService.getObject(TheatreProfile.class, theatreid);
		if(profile==null) return ErrorCode.getFailure("基础信息未设置");
		if(!profile.hasOpentype(OdiConstant.PARTNER_GPTBS)) return ErrorCode.getFailure("非gptbs开放");
		return ErrorCode.SUCCESS;
	}
	@RequestMapping("/admin/drama/remote/refreshField.xhtml")
	public String qryTheatre(Long theatreid, ModelMap model){
		User user = getLogonUser();
		ErrorCode code = validTheatre(theatreid);
		if(!code.isSuccess()){
			return forwardMessage(model, code.getMsg());
		}
		Theatre theatre = daoService.getObject(Theatre.class, theatreid);
		List<String> msgList = new ArrayList<String>();
		ErrorCode<List<TheatreField>> remoteCode = remoteTheatreService.updateTheatreField(user.getId(), theatre, msgList);
		if(!code.isSuccess()) return forwardMessage(model, remoteCode.getErrcode() + "," + remoteCode.getMsg());
		return forwardMessage(model, msgList);
	}
	
	@RequestMapping("/admin/drama/remote/refreshBaseSeat.xhtml")
	public String refreshSeat(Long theatreid, ModelMap model){
		User user = getLogonUser();
		ErrorCode code = validTheatre(theatreid);
		if(!code.isSuccess()){
			return forwardMessage(model, code.getMsg());
		}
		List<String> msgList = new ArrayList<String>();
		List<TheatreRoom> roomList = daoService.getObjectListByField(TheatreRoom.class, "theatreid", theatreid);
		for (TheatreRoom theatreRoom : roomList) {
			ErrorCode<List<FieldAreaSeatVo>> remoteCode = remoteTheatreService.updateTheatreRoomSeat(user.getId(), theatreRoom, true, msgList);
			if(!remoteCode.isSuccess()){
				msgList.add("场区：" + theatreRoom.getRoomname() +"," + remoteCode.getErrcode()+ ":" + remoteCode.getMsg());
			}
		}
		return forwardMessage(model, msgList);
	}
	
	@RequestMapping("/admin/drama/remote/refreshDramaPlayItem.xhtml")
	public String refreshDramaPlayItem(Long theatreid, Date playdate, ModelMap model){
		User user = getLogonUser();
		ErrorCode code = validTheatre(theatreid);
		if(!code.isSuccess()){
			return forwardMessage(model, code.getMsg());
		}
		List<String> msgList = new ArrayList<String>();
		if(playdate == null){
			Date curDate = DateUtil.getCurDate();
			for (int i= 0; i<7; i++) {
				playdate = DateUtil.addDay(curDate, i);
				remoteTheatreService.updateDramaPlayItem(user.getId(), theatreid, playdate, msgList);
			}
		}else{
			remoteTheatreService.updateDramaPlayItem(user.getId(), theatreid, playdate, msgList);
		}
		if(msgList.isEmpty()){
			msgList.add("没有更新到数据！");
		}
		return forwardMessage(model, msgList);
	}
	
	@RequestMapping("/admin/drama/remote/updateDramaPlayItem.xhtml")
	public String updateDramaPlayItem(Long theatreid, ModelMap model){
		User user = getLogonUser();
		ErrorCode code = validTheatre(theatreid);
		if(!code.isSuccess()){
			return forwardMessage(model, code.getMsg());
		}
		List<String> msgList = new ArrayList<String>();
		ErrorCode itemCode = remoteTheatreService.updateDramaPlayItem(user.getId(), theatreid, msgList, 0);
		if(!itemCode.isSuccess()) return forwardMessage(model, itemCode.getErrcode() +"," + itemCode.getMsg());
		if(msgList.isEmpty()){
			msgList.add("没有更新到数据！");
		}
		return forwardMessage(model, msgList);
	}
	
	@RequestMapping("/admin/drama/remote/refreshTheatreSeatArea.xhtml")
	public String refreshTheatreSeatArea(Long dpid, ModelMap model){
		User user = getLogonUser();
		DramaPlayItem item = daoService.getObject(DramaPlayItem.class, dpid);
		if(item == null) return forwardMessage(model, "场次不存在！");
		UpdateDpiContainer container = new UpdateDpiContainer();
		List<String> msgList = new ArrayList<String>();
		ErrorCode code = remoteTheatreService.updateTheateSeatArea(user.getId(), container, item, msgList);
		if(!code.isSuccess()) return forwardMessage(model, code.getErrcode() + "," + code.getMsg());
		return forwardMessage(model, code.getMsg());
	}
	
	@RequestMapping("/admin/drama/remote/refreshTheatreSeatPrice.xhtml")
	public String refreshTheatreSeatPrice(Long dpid, ModelMap model){
		User user = getLogonUser();
		DramaPlayItem item = daoService.getObject(DramaPlayItem.class, dpid);
		if(item == null) return forwardMessage(model, "场次不存在！");
		UpdateDpiContainer container = new UpdateDpiContainer();
		List<TheatreSeatArea> seatAreaList = daoService.getObjectListByField(TheatreSeatArea.class, "dpid", item.getId());
		List<String> msgList = new ArrayList<String>();
		remoteTheatreService.updateSeatPrice(user.getId(), container, item, seatAreaList, msgList);
		return forwardMessage(model, msgList);
	}
}
