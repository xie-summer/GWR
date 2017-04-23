package com.gewara.web.action.admin.drama;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.gewara.model.drama.OpenDramaItem;
import com.gewara.model.drama.TheatreSeatArea;
import com.gewara.support.ErrorCode;
import com.gewara.support.VelocityTemplate;
import com.gewara.untrans.drama.impl.DramaControllerService;
import com.gewara.web.action.admin.BaseAdminController;

@Controller
public class InsteadDramaAdminController extends BaseAdminController{
	
	@Autowired@Qualifier("dramaControllerService")
	private DramaControllerService dramaControllerService;
	
	@Autowired@Qualifier("velocityTemplate")
	private VelocityTemplate velocityTemplate;
	
	@RequestMapping("/admin/drama/chooseSeat.shtml")
	public String chooseSeat(HttpServletRequest request, HttpServletResponse response, 
			@RequestParam("itemid")Long itemid, @RequestParam(value="areaid", required=false) Long areaid, ModelMap model){
		OpenDramaItem odi = daoService.getObjectByUkey(OpenDramaItem.class, "dpid", itemid, false);
		ErrorCode code = dramaControllerService.addSeatData(model, odi, areaid, response, request, null);
		if(!code.isSuccess()) return showMessageAndReturn(model, request, code.getMsg());
		return "admin/drama/wide_chooseSeat.vm";
	}
	
	@RequestMapping("/admin/drama/getSeatPage.shtml")
	public String getSeatPage(Long areaid, ModelMap model){
		TheatreSeatArea seatArea = daoService.getObject(TheatreSeatArea.class, areaid);
		ErrorCode<Map> code = dramaControllerService.getSeatPage(seatArea);
		if(!code.isSuccess()){
			return showJsonError(model, code.getMsg());
		}
		Map model2 = code.getRetval();
		Map jsonMap = new HashMap();
		final String template = "admin/drama/wide_seatPage.vm";
		String seatPage = velocityTemplate.parseTemplate(template, model2);
		jsonMap.put("seatPage", seatPage);
		return showJsonSuccess(model, jsonMap);
	}
	
}
