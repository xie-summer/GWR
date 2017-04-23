package com.gewara.web.action.admin.gym;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import com.gewara.command.SearchOrderCommand;
import com.gewara.constant.ticket.OrderConstant;
import com.gewara.model.pay.GymOrder;
import com.gewara.service.OpenGymService;
import com.gewara.support.ErrorCode;
import com.gewara.untrans.gym.SynchGymService;
import com.gewara.untrans.ticket.OrderProcessService;
import com.gewara.util.JsonUtils;
import com.gewara.web.action.admin.BaseAdminController;
import com.gewara.xmlbind.gym.RemoteGym;

@Controller
public class GymCardOrderAdminController extends BaseAdminController {

	@Autowired@Qualifier("synchGymService")
	private SynchGymService synchGymService;
	public void setSynchGymService(SynchGymService synchGymService){
		this.synchGymService = synchGymService;
	}
	@Autowired@Qualifier("orderProcessService")
	private OrderProcessService orderProcessService;
	public void setProcessOrderService(OrderProcessService orderProcessService) {
		this.orderProcessService = orderProcessService;
	}
	@Autowired@Qualifier("openGymService")
	private OpenGymService openGymService;
	public void setOpenGymService(OpenGymService openGymService){
		this.openGymService = openGymService;
	}
	
	@RequestMapping("/admin/gymTicket/orderList.xhtml")
	public String orderList(SearchOrderCommand soc, String xls,String isOneGym, HttpServletRequest request, HttpServletResponse res, ModelMap model){
		String citycode = getAdminCitycode(request);
		if(soc.getGymid() != null) {
			ErrorCode<RemoteGym> code = synchGymService.getRemoteGym(soc.getGymid(), true);
			if(code.isSuccess()) model.put("gym", code.getRetval());
		}
		List<GymOrder> gymOrderList = openGymService.getGymOrderList(soc, 0, 500);
		model.put("orderList", gymOrderList);
		model.put("ordertype", StringUtils.isBlank(soc.getOrdertype())?"" : soc.getOrdertype());
		if(!StringUtils.equals(isOneGym, "Y")){
			ErrorCode<List<RemoteGym>> code = synchGymService.getGymList(citycode, null, null, null, false, 0, 100);
			if(code.isSuccess()) model.put("gymList", code.getRetval());
		}
		if(StringUtils.isNotBlank(xls)) {
			download("xls", res);
			model.put("xls", xls);
			return "admin/gym/ticket/orders.vm";
		}
		Map<Long, String> synchStatusMap = new HashMap<Long, String>();
		for(GymOrder gymOrder : gymOrderList){
			Map map = JsonUtils.readJsonToMap(gymOrder.getOtherinfo());
			synchStatusMap.put(gymOrder.getId(), (String) map.get(GymOrder.GYM_CONFIRM));
		}
		model.put("synchStatusMap", synchStatusMap);
		return "admin/gym/ticket/orderList.vm";
	}
	
	@RequestMapping("/admin/gymTicket/failConfirm.xhtml")
	public String reConfirm(String tradeNo, ModelMap model){
		GymOrder order = daoService.getObjectByUkey(GymOrder.class, "tradeNo", tradeNo, false);
		if(!StringUtils.equals(order.getStatus(), OrderConstant.STATUS_PAID_FAILURE)) return showJsonError(model, "非待处理的订单，不能确认！");
		ErrorCode result = orderProcessService.processOrder(order, "重新确认", null);
		return showJsonSuccess(model, result.getMsg());
	}

}
