package com.gewara.web.action.drama;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.gewara.constant.PayConstant;
import com.gewara.json.PageView;
import com.gewara.model.drama.OpenDramaItem;
import com.gewara.model.drama.Theatre;
import com.gewara.model.pay.SpecialDiscount;
import com.gewara.support.ErrorCode;
import com.gewara.support.VelocityTemplate;
import com.gewara.untrans.drama.impl.DramaControllerService;
import com.gewara.untrans.order.impl.SpdiscountService;
import com.gewara.util.DateUtil;
import com.gewara.web.action.partner.OdiSpdiscountFilter;

@Controller
public class DramaPlayItemController extends BaseDramaController {

	@Autowired@Qualifier("dramaControllerService")
	private DramaControllerService dramaControllerService;
	
	@Autowired@Qualifier("velocityTemplate")
	private VelocityTemplate velocityTemplate;
	
	@Autowired@Qualifier("spdiscountService")
	private SpdiscountService spdiscountService;
	
	@RequestMapping("/ajax/drama/getDramaPrice.xhtml")
	public String getDramaPrice(@RequestParam("itemid")Long itemid, ModelMap model){
		OpenDramaItem odi = daoService.getObjectByUkey(OpenDramaItem.class, "dpid", itemid, false);
		ErrorCode<Map> code = dramaControllerService.getPriceList(odi);
		if(!code.isSuccess()){
			return showJsonError(model, code.getMsg());
		}
		model.putAll(code.getRetval());
		if(odi.isOpenseat()){
			return "drama/ticket/seatPrice.vm";
		}
		return "drama/ticket/choosePrice.vm";
	}

	@RequestMapping("/ajax/drama/getDramaPlayItemList.xhtml")
	public String getDramaPriceList(Long dramaid, Long fieldid, ModelMap model, HttpServletRequest request, HttpServletResponse response){
		final boolean cache = true;
		ErrorCode<Map> code = dramaControllerService.getItemList(dramaid, fieldid, request, response, cache);
		if(!code.isSuccess()){
			return showJsonError(model, code.getMsg());
		}
		Map jsonMap = code.getRetval();
		model.putAll(jsonMap);
		PageView pageView = (PageView)model.get("pageView");
		if(pageView != null){
			return "pageView.vm";
		}
		final String viewPage = "drama/ticket/dramaPlayItem.vm";
		return viewPage;
	}
	

	@RequestMapping("/drama/ajax/innerTheatre.xhtml")
	public String innerTheatre(Long theatreid, ModelMap model){
		Theatre curTheatre = daoService.getObject(Theatre.class, theatreid);
		model.put("curTheatre", curTheatre);
		List<Long> idList = openDramaService.getCurDramaidList(theatreid);
		model.put("curTheatrePlayCount", idList.size());
		return "drama/mod_dramaDeR_yc.vm";
	}
	
	@RequestMapping("/drama/ajax/getSpdiscountList.xhtml")
	public String spdiscountList(ModelMap model,Long itemid){
		OpenDramaItem odi = daoService.getObjectByUkey(OpenDramaItem.class, "dpid", itemid, true);
		if (odi == null) return showJsonError(model, "优惠方式不存在！");
		OdiSpdiscountFilter odiSpdiscountFilter = new OdiSpdiscountFilter(odi, DateUtil.getCurFullTimestamp());
		List<SpecialDiscount> sdList= spdiscountService.getSpecialDiscountData(odiSpdiscountFilter, SpecialDiscount.OPENTYPE_GEWA, PayConstant.APPLY_TAG_DRAMA);
		Map dataMap = new HashMap();
		dataMap.put("sdList", sdList);
		String result = velocityTemplate.parseTemplate("movie/ajaxMovieDiscount.vm", dataMap);
		return showJsonSuccess(model, result);
	}
	
	@RequestMapping("/ajax/drama/getSearchOdi.xhtml")
	public String getSearchOdi(Long dramaid, Long fieldid, ModelMap model, HttpServletRequest request, HttpServletResponse response){
		final boolean cache = true;
		ErrorCode<Map> code = dramaControllerService.getItemList(dramaid, fieldid, request, response, cache);
		if(!code.isSuccess()){
			return showJsonError(model, code.getMsg());
		}
		Map jsonMap = code.getRetval();
		model.putAll(jsonMap);
		PageView pageView = (PageView)model.get("pageView");
		if(pageView != null){
			return "pageView.vm";
		}
		final String viewPage = "drama/ticket/odiDetail.vm";
		return viewPage;
	}
}
