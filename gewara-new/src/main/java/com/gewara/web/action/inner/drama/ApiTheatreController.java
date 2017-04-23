package com.gewara.web.action.inner.drama;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import com.gewara.constant.ApiConstant;
import com.gewara.model.drama.DramaStar;
import com.gewara.model.drama.OpenDramaItem;
import com.gewara.model.drama.OpenTheatreSeat;
import com.gewara.model.drama.Theatre;
import com.gewara.model.drama.TheatreRoom;
import com.gewara.model.drama.TheatreRoomSeat;
import com.gewara.model.drama.TheatreSeatPrice;
import com.gewara.service.drama.DramaPlayItemService;
import com.gewara.service.drama.DramaStarService;
import com.gewara.service.drama.OpenDramaService;
import com.gewara.service.drama.TheatreService;
import com.gewara.util.BeanUtil;
import com.gewara.util.DateUtil;
import com.gewara.web.action.api.BaseApiController;

@Controller
public class ApiTheatreController extends BaseApiController{

	@Autowired@Qualifier("theatreService")
	private TheatreService theatreService;
	public void setTheatreService(TheatreService theatreService){
		this.theatreService = theatreService;
	}
	
	@Autowired@Qualifier("dramaStarService")
	private DramaStarService dramaStarService;
	public void setDramaStarService(DramaStarService dramaStarService){
		this.dramaStarService = dramaStarService;
	}
	@Autowired@Qualifier("dramaPlayItemService")
	private DramaPlayItemService dramaPlayItemService;
	public void setDramaPlayItemService(DramaPlayItemService dramaPlayItemService){
		this.dramaPlayItemService = dramaPlayItemService;
	}
	@Autowired@Qualifier("openDramaService")
	private OpenDramaService openDramaService;
	public void setOpenDramaService(OpenDramaService openDramaService){
		this.openDramaService  = openDramaService;
	}
	//场馆信息同步
	@RequestMapping("/inner/drama/theatreInfo.xhtml")
	public String theatreInfo(String citycode, String lastTime, ModelMap model){
		if(StringUtils.isEmpty(lastTime)) return getErrorXmlView(model, ApiConstant.CODE_PARAM_ERROR, "参数错误！");
		List<Theatre> theatreList = theatreService.getTheatreListByUpdateTime(citycode, DateUtil.parseTimestamp(lastTime));
		model.put("theatreList", theatreList);
		return getXmlView(model, "api2/drama/theatreInfoList.vm");
	}
	
	//厅信息同步
	@RequestMapping("/inner/drama/roomInfo.xhtml")
	public String roomInfo(String lastTime, ModelMap model){
		if(StringUtils.isEmpty(lastTime)) return getErrorXmlView(model, ApiConstant.CODE_PARAM_ERROR, "参数错误！");
		List<TheatreRoom> roomList = theatreService.getTheatreRoomList(DateUtil.parseTimestamp(lastTime), "updatetime");
		model.put("roomList", roomList);
		return getXmlView(model, "api2/drama/roomInfoList.vm");
	}
	
	//座位基础信息同步
	@RequestMapping("/inner/drama/roomSeatInfo.xhtml")
	public String roomSeatInfo(String lastTime, ModelMap model){
		if(StringUtils.isEmpty(lastTime)) return getErrorXmlView(model, ApiConstant.CODE_PARAM_ERROR, "参数错误！");
		List<TheatreRoom> roomList = theatreService.getTheatreRoomList(DateUtil.parseTimestamp(lastTime), "synchtime");
		List<Long> idList = BeanUtil.getBeanPropertyList(roomList, Long.class, "id", true);
		List<TheatreRoomSeat> seatList = theatreService.getTheatreRoomSeatList(idList);
		model.put("seatList", seatList);
		return getXmlView(model, "api2/drama/roomSeatList.vm");
	}
	//话剧明星、导演、编剧信息同步
	@RequestMapping("/inner/drama/dramastarInfo.xhtml")
	public String dramastarInfo(String lastTime, ModelMap model){
		if(StringUtils.isEmpty(lastTime)) return getErrorXmlView(model, ApiConstant.CODE_PARAM_ERROR, "参数错误！");
		List<DramaStar> dramaStarList = dramaStarService.getSynchStarList(DateUtil.parseTimestamp(lastTime));
		model.put("dramaStarList", dramaStarList);
		return getXmlView(model, "api2/drama/dramaStarList.vm");
	}
	//开放场次
	@RequestMapping("/inner/drama/getodiInfo.xhtml")
	public String getodiInfo(String idList, String lastTime, ModelMap model){
		List<Long> did = BeanUtil.getIdList(idList, ",");
		List<OpenDramaItem> odiList = theatreService.getOpenDramItemList(did, DateUtil.parseTimestamp(lastTime));
		model.put("odiList", odiList);
		return getXmlView(model, "api2/drama/odiList.vm");
	}
	
	//价格表
	@RequestMapping("/inner/drama/seatPriceInfo.xhtml")
	public String seatPriceInfo(String idList, String lastTime, ModelMap model){
		List<Long> didList = BeanUtil.getIdList(idList, ",");
		List<Long> dpidList = dramaPlayItemService.getDramaPlayIdList(didList);
		List<TheatreSeatPrice> priceList = theatreService.getSeatPriceList(dpidList, DateUtil.parseTimestamp(lastTime));
		Map<Long, Long> odiMap = new HashMap<Long, Long>();
		for(TheatreSeatPrice tsp : priceList){
			OpenDramaItem odi = daoService.getObjectByUkey(OpenDramaItem.class, "dpid", tsp.getDpid(), false);
			if(odi != null)
			odiMap.put(tsp.getId(), odi.getId());
		}
		model.put("odiMap", odiMap);
		model.put("priceList", priceList);
		return getXmlView(model, "api2/drama/seatPriceList.vm");
	}
	
	//座位价格信息
	@RequestMapping("/inner/drama/openSeatInfo.xhtml")
	public String openSeatInfo(String idList, String lastTime, ModelMap model){
		List<Long> did = BeanUtil.getIdList(idList, ",");
		List<Long> odiidList = openDramaService.getOpenDramaItemId(did, DateUtil.parseTimestamp(lastTime));
		List<OpenTheatreSeat> theareSeatList = theatreService.getOpenSeatList(odiidList);
		model.put("theareSeatList", theareSeatList);
		return getXmlView(model, "api2/drama/theatreSeatList.vm");
	}
	
}
