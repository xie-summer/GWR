package com.gewara.web.action.admin.ticket;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import com.gewara.constant.AdminCityContant;
import com.gewara.constant.sys.LogTypeConstant;
import com.gewara.helper.ticket.UpdateMpiContainer;
import com.gewara.model.acl.User;
import com.gewara.model.common.GewaCity;
import com.gewara.model.movie.Cinema;
import com.gewara.model.movie.CinemaProfile;
import com.gewara.model.movie.CinemaRoom;
import com.gewara.service.GewaCityService;
import com.gewara.service.ticket.TicketSynchService;
import com.gewara.support.ErrorCode;
import com.gewara.support.MultiPropertyComparator;
import com.gewara.untrans.ticket.MpiOpenService;
import com.gewara.untrans.ticket.TicketOperationService;
import com.gewara.util.BeanUtil;
import com.gewara.util.DateUtil;
import com.gewara.web.action.admin.BaseAdminController;
@Controller
public class RemoteTicketAdminController  extends BaseAdminController {
	@Autowired@Qualifier("ticketOperationService")
	private TicketOperationService ticketOperationService;
	
	@Autowired@Qualifier("ticketSynchService")
	private TicketSynchService ticketSynchService;
	public void setTicketSynchService(TicketSynchService ticketSynchService) {
		this.ticketSynchService = ticketSynchService;
	}
	@Autowired@Qualifier("mpiOpenService")
	private MpiOpenService mpiOpenService;
	@Autowired@Qualifier("gewaCityService")
	private GewaCityService gewaCityService;
	@RequestMapping("/admin/ticket/remote/refreshPlayItem.xhtml")
	public String refreshPlayItem(Long cid, ModelMap model){
		Date cur = DateUtil.getCurDate();
		List<String> msgList = new ArrayList<String>();
		for(int i=0; i < 7; i++){
			Date playdate = DateUtil.addDay(cur, i);
			ticketOperationService.updateMoviePlayItem(cid, playdate, msgList);
		}
		ticketSynchService.updateOpenPlayItem(cid, msgList);
		if(msgList.isEmpty()){
			msgList.add("没有要更新的数据！");
		}
		return forwardMessage(model, msgList);
	}
	
	@RequestMapping("/admin/ticket/remote/refreshPlayItemBydate.xhtml")
	public String refreshPlayItemBydate(Long cid, Date playdate, ModelMap model){
		if(playdate == null) playdate = DateUtil.getCurDate();
		List<String> msgList = new ArrayList<String>();
		ticketOperationService.updateMoviePlayItem(cid, playdate, msgList);
		ticketSynchService.updateOpenPlayItem(cid, msgList);
		if(msgList.isEmpty()){
			msgList.add("没有要更新的数据！");
		}
		return forwardMessage(model, msgList);
	}
	
	@RequestMapping("/admin/ticket/remote/updatePlayItem.xhtml")
	public String updatePlayItem(Long cid, ModelMap model){
		List<String> msgList = new ArrayList<String>();
		try{
			UpdateMpiContainer container = new UpdateMpiContainer();
			ticketOperationService.updateMoviePlayItem(container, cid, msgList, 0);
			mpiOpenService.asynchAutoOpenMpiList(container.getInsertList());

			ticketSynchService.updateOpenPlayItem(msgList);
		}catch (Exception e) {
			dbLogger.errorWithType(LogTypeConstant.LOG_TYPE_ACCOUNT, "手工增量更新排片", e);
			msgList.add("手工增量更新排片：" + cid + ", 错误！" );
		}
		if(msgList.isEmpty()){
			msgList.add("没有要更新的数据！");
		}
		return forwardMessage(model, msgList);
	}
	
	@RequestMapping("/admin/ticket/remote/qryCinema.xhtml")
	public String qryCinema(HttpServletRequest request, ModelMap model,String provincecode){
		User user = getLogonUser();
		String citycode = null;
		String citycodes = null;
		Map<GewaCity, List<GewaCity>> proMap = gewaCityService.getAdmCityMap();
		List<String> cityList = new LinkedList<String>();
		if(StringUtils.isBlank(provincecode)) {
			citycode = getAdminCitycode(request);
			if(StringUtils.isBlank(citycode)){
				citycode =   AdminCityContant.CITYCODE_SH ;
			}
			GewaCity c = daoService.getObjectByUkey(GewaCity.class,"citycode", citycode, true);
			provincecode = c.getProvincecode();
		}
		Map<String , GewaCity> cityMap = new HashMap<String, GewaCity>();
		for(GewaCity city : proMap.keySet()){
			if(StringUtils.equals(provincecode, city.getProvincecode())){
				List<GewaCity> gewaCityList = proMap.get(city);
				for(GewaCity c : gewaCityList){
					cityList.add("'" + c.getCitycode() + "'");
				}
				cityMap = BeanUtil.beanListToMap(gewaCityList, "citycode");
			}
		}
		citycodes = StringUtils.join(cityList, ",");
		String hql = "select id from CinemaProfile cp where exists(select c.id from Cinema c where c.id=cp.id and c.citycode in (" + citycodes + ")) and cp.opentype is not null"; 
		List<Long> cinemaIdList = hibernateTemplate.find(hql);
		List<Cinema> cinemaList = daoService.getObjectList(Cinema.class, cinemaIdList);
		Collections.sort(cinemaList, new MultiPropertyComparator(new String[]{"citycode"}, new boolean[]{true}));
		Map<Long, CinemaProfile> profileMap = daoService.getObjectMap(CinemaProfile.class, cinemaIdList);
		model.put("cinemaList", cinemaList);
		model.put("profileMap", profileMap);
		model.put("user", user);
		model.put("proMap", proMap);
		model.put("provincecode",provincecode);
		model.put("cityMap", cityMap);
		return "admin/ticket/remote/qryCinema.vm";
	}
	
	@RequestMapping("/admin/ticket/remote/updateCinemaRoom.xhtml")
	public String updateCinemaRoom(Long cid, ModelMap model){
		User user = getLogonUser();
		Cinema cinema = daoService.getObject(Cinema.class, cid);
		if(cinema == null) return showJsonError(model, "影院没有关联！");
		ErrorCode code = ticketSynchService.updateCinemaRoom(cinema.getId(), user.getId());
		if(!code.isSuccess()) return showJsonError(model, code.getMsg());
		return showJsonSuccess(model);
	}
	
	@RequestMapping("/admin/ticket/remote/updateCinemaRoomSeat.xhtml")
	public String updateRoomSeat(Long cid, ModelMap model){
		Cinema cinema = daoService.getObject(Cinema.class, cid);
		if(cinema == null) return forwardMessage(model, "影院没有关联！");
		List<CinemaRoom> roomList = daoService.getObjectListByField(CinemaRoom.class, "cinemaid", cinema.getId());
		if(roomList.isEmpty()) return forwardMessage(model, "没有要下载的影厅！");
		List<String> msgList = new ArrayList<String>();
		for (CinemaRoom cinemaRoom : roomList) {
			msgList.addAll(ticketSynchService.updateRoomSeatList(cinemaRoom, true));
		}
		if(msgList.isEmpty()){
			msgList.add("没有要更新的数据！");
		}
		return forwardMessage(model, msgList);
	}
}
