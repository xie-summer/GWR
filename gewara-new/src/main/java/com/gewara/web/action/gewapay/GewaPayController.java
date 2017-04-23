package com.gewara.web.action.gewapay;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import com.gewara.model.pay.GewaOrder;
import com.gewara.model.user.Member;
import com.gewara.support.ErrorCode;
import com.gewara.untrans.ticket.OrderProcessService;

@Controller
public class GewaPayController extends BasePayController {
	@Autowired@Qualifier("orderProcessService")
	private OrderProcessService orderProcessService;
	public void setProcessOrderService(OrderProcessService orderProcessService) {
		this.orderProcessService = orderProcessService;
	}
	@RequestMapping("/partner/payOrder.xhtml")
	public String payOrder(Long orderId, Long partnerid, ModelMap model) {
		return webPayOrder(orderId, partnerid, null, model);
	}
	@RequestMapping("/gewapay/payOrder.xhtml")
	public String webPayOrder(Long orderId, Long partnerid, String checkvalue, ModelMap model) {
		if(partnerid!=null){
			ErrorCode<GewaOrder> code = orderProcessService.gewaPayOrderAtServer(orderId, partnerid, checkvalue, false);
			if(code.isSuccess()) {
				model.put("tradeNo", code.getRetval().getTradeNo());
				return "redirect:/partner/orderResult.xhtml";
			}
			return alertMessage(model, code.getMsg(), "gewapay/order.xhtml?orderId=" + orderId);
		}else{
			Member member = getLogonMember();
			ErrorCode<GewaOrder> code = orderProcessService.gewaPayOrderAtServer(orderId, member.getId(), checkvalue, false);
			if(code.isSuccess()) {
				model.put("orderId", orderId);
				return "redirect:/gewapay/orderResult.xhtml";
			}
			return alertMessage(model, code.getMsg(), "gewapay/order.xhtml?orderId=" + orderId);
		}
	}
}
