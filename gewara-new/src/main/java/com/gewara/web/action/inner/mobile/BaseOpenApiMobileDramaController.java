package com.gewara.web.action.inner.mobile;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.ui.ModelMap;

import com.gewara.helper.api.GewaApiDramaHelper;
import com.gewara.model.drama.Drama;
import com.gewara.model.drama.Theatre;
import com.gewara.service.drama.DramaOrderService;
import com.gewara.service.drama.DramaPlayItemService;
import com.gewara.service.drama.DramaService;
import com.gewara.service.drama.OpenDramaService;
import com.gewara.untrans.drama.TheatreOperationService;
import com.gewara.untrans.drama.TheatreOrderService;

public class BaseOpenApiMobileDramaController extends BaseOpenApiMobileController{
	@Autowired@Qualifier("dramaService")
	protected DramaService dramaService;
	@Autowired@Qualifier("dramaPlayItemService")
	protected DramaPlayItemService dramaPlayItemService;
	@Autowired@Qualifier("dramaOrderService")
	protected DramaOrderService dramaOrderService;
	@Autowired@Qualifier("theatreOperationService")
	protected TheatreOperationService theatreOperationService;
	@Autowired@Qualifier("openDramaService")
	protected OpenDramaService openDramaService;
	@Autowired@Qualifier("theatreOrderService")
	protected TheatreOrderService theatreOrderService;
	protected void putDramaListNode(ModelMap model){
		model.put("root", "dramaList");
		model.put("nextroot", "drama");
	}
	protected void putDramaNode(ModelMap model){
		model.put("root", "drama");
	}
	protected void putTheatreListNode(ModelMap model){
		model.put("root", "theatreList");
		model.put("nextroot", "theatre");
	}
	protected void putTheatreNode(ModelMap model){
		model.put("root", "theatre");
	}
	protected Map<String, Object> getTheatreData(Theatre theatre){
		String mobilePath = getMobilePath();
		Map<String, Object> params = GewaApiDramaHelper.getTheatreData(theatre, mobilePath + theatre.getLimg(), mobilePath + theatre.getFirstpic());
		Map<String, String> subwaylineMap = placeService.getSubwaylineMap(theatre.getCitycode());
		params.put("feature", theatre.getFeature());
		params.put("stationname", theatre.getStationname());
		params.put("exitnumber", theatre.getExitnumber());
		params.put("transport", theatre.getTransport());
		params.put("generalmark", getPlaceGeneralmark(theatre));
		params.put("linename", theatre.getLineName(subwaylineMap));
		return params;
	}
	protected Map<String, Object> getDramaData(Drama drama){
		String mobilePath = getMobilePath();
		Map<String, Object> params = GewaApiDramaHelper.getDramaData(drama, mobilePath + drama.getLimg());
		return params;
	}
	protected void putDramaPlayItemListNode(ModelMap model){
		model.put("root", "dramaPlayItemList");
		model.put("nextroot", "dramaPlayItem");
	}
}
