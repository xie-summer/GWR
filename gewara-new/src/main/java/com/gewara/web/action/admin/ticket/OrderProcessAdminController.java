package com.gewara.web.action.admin.ticket;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.gewara.constant.PayConstant;
import com.gewara.constant.ticket.OpiConstant;
import com.gewara.constant.ticket.OrderConstant;
import com.gewara.model.acl.User;
import com.gewara.model.pay.TicketOrder;
import com.gewara.model.ticket.OpenPlayItem;
import com.gewara.model.ticket.OpenSeat;
import com.gewara.model.ticket.SellSeat;
import com.gewara.service.OrderException;
import com.gewara.service.partner.PartnerSynchService;
import com.gewara.service.ticket.TicketOrderService;
import com.gewara.service.ticket.TicketProcessService;
import com.gewara.support.ErrorCode;
import com.gewara.untrans.ticket.OrderProcessService;
import com.gewara.untrans.ticket.TicketOperationService;
import com.gewara.util.BeanUtil;
import com.gewara.web.action.admin.BaseAdminController;
import com.gewara.xmlbind.ticket.TicketRemoteOrder;
@Controller
public class OrderProcessAdminController extends BaseAdminController{
	@Autowired@Qualifier("ticketOperationService")
	private TicketOperationService ticketOperationService;
	public void setTicketOperationService(TicketOperationService ticketOperationService) {
		this.ticketOperationService = ticketOperationService;
	}
	@Autowired@Qualifier("ticketOrderService")
	private TicketOrderService ticketOrderService;
	public void setTicketOrderService(TicketOrderService ticketOrderService) {
		this.ticketOrderService = ticketOrderService;
	}
	@Autowired@Qualifier("ticketProcessService")
	private TicketProcessService ticketProcessService;
	public void setTicketProcessService(TicketProcessService ticketProcessService) {
		this.ticketProcessService = ticketProcessService;
	}
	@Autowired@Qualifier("partnerSynchService")
	private PartnerSynchService partnerSynchService;
	public void setPartnerSynchService(PartnerSynchService partnerSynchService) {
		this.partnerSynchService = partnerSynchService;
	}
	@Autowired@Qualifier("orderProcessService")
	private OrderProcessService orderProcessService;
	public void setProcessOrderService(OrderProcessService orderProcessService) {
		this.orderProcessService = orderProcessService;
	}
	
	@RequestMapping("/admin/ticket/order/reConfirmOrder.xhtml")
	public String userConfirmSuccess(ModelMap model, Long orderId, String forceReConfirm){
		boolean reChange = StringUtils.isNotBlank(forceReConfirm);
		User user = getLogonUser();
		TicketOrder order = daoService.getObject(TicketOrder.class, orderId);
		ErrorCode result = orderProcessService.reconfirmOrder(order, user.getId(), false, reChange);
		if(result.isSuccess()) return showMessage(model, "成功处理！");
		else return forwardMessage(model, result.getMsg());
	}
	@RequestMapping("/admin/ticket/order/confirmSuccess.xhtml")
	public String confirmSuccess(Long orderId, ModelMap model){
		TicketOrder order = daoService.getObject(TicketOrder.class, orderId);
		OpenPlayItem opi = daoService.getObjectByUkey(OpenPlayItem.class, "mpid", order.getMpid(), true);
		if(order.isPaidFailure()){//订单待处理，很少出现
			List<SellSeat> seatList = ticketOrderService.getOrderSeatList(order.getId());
			try {
				ticketOrderService.processOrderPay(order, opi, seatList);
			} catch (OrderException e) {
				return showJsonError(model, "付款失败：" + e.getMsg());
			}
		}
		User user = getLogonUser();
		ErrorCode code = orderProcessService.confirmSuccess(order, opi, user.getId(), false);
		if(code.isSuccess()) return showJsonSuccess(model);
		return showJsonError(model, code.getMsg());
		
	}
	@RequestMapping("/admin/ticket/order/changeSeat.xhtml")
	public String changeSeat(Long orderid, String newseat, String forceReConfirm, ModelMap model){
		User user = getLogonUser();
		TicketOrder order = daoService.getObject(TicketOrder.class, orderid);
		//万达先特殊处理
		if(StringUtils.equals(OpiConstant.OPEN_WD, order.getCategory())){
			return showJsonError(model, "万达影院暂不支持！");
		}

		boolean reChange = StringUtils.isNotBlank(forceReConfirm);
		try {
			OpenPlayItem opi = daoService.getObjectByUkey(OpenPlayItem.class, "mpid", order.getMpid(), true);
			List<SellSeat> seatList = ticketOrderService.getOrderSeatList(order.getId());
			ticketOperationService.releasePaidFailureOrderSeat(opi, order, seatList);
			//座位价格处理
			List<OpenSeat> oseatList = ticketProcessService.getNewSeatList(order, opi, seatList, newseat);
			ticketProcessService.changeSeat(opi, oseatList, order, reChange);
			ticketOperationService.createRemoteOrder(opi, order, seatList);
			dbLogger.warn(user.getNickname()+user.getId()+ "更改座位");
			return showJsonSuccess(model);
		} catch (Exception e) {
			dbLogger.error("错误", e);
			return showJsonError(model, e.getMessage());
		}
	}
	@RequestMapping("/admin/ticket/order/createRemoteOrder.xhtml")
	public String createRemoteOrder(String tradeNo, ModelMap model){
		TicketOrder order = daoService.getObjectByUkey(TicketOrder.class, "tradeNo", tradeNo, false);
		List<SellSeat> seatList = ticketOrderService.getOrderSeatList(order.getId());
		OpenPlayItem opi = daoService.getObjectByUkey(OpenPlayItem.class, "mpid", order.getMpid(), true);
		ErrorCode result = ticketOperationService.createRemoteOrder(opi, order, seatList);
		if(result.isSuccess()){
			return forwardMessage(model, "创建远程订单成功！");
		}else{
			return forwardMessage(model, "创建远程订单失败：" + result.getMsg());
		}
	}
	/**
	 * 检查与火凤凰状态是否一致
	 * @param orderid
	 * @param model
	 * @return
	 */
	@RequestMapping("/admin/ticket/order/checkSeat.xhtml")
	public String checkOrderSeat(Long orderid, ModelMap model){
		TicketOrder order = daoService.getObject(TicketOrder.class, orderid);
		ErrorCode<TicketRemoteOrder> remoteOrder = ticketOperationService.checkRemoteOrder(order);
		if(!remoteOrder.isSuccess()) return forwardMessage(model, remoteOrder.getMsg());
		if(!remoteOrder.getRetval().hasFixed()) return forwardMessage(model, BeanUtil.buildString(remoteOrder.getRetval(), false));
		if(order.isPaidFailure() || order.isPaidUnfix()){
			ErrorCode result = orderProcessService.processOrder(order, "重新确认", null);
			if(result.isSuccess()) {
				dbLogger.warn(getLogonUser().getUsername() + "转换订单状态为交易成功：" + order.getTradeNo());
				return forwardMessage(model, "检查成功,转换成功！");
			}else{
				return forwardMessage(model, "检查成功,转换失败：" + result);
			}
		}
		return forwardMessage(model, "检查成功");
	}
	@RequestMapping("/notifyOrderPaid.dhtml")
	@ResponseBody
	public String notifyOrderPaid(String tradeNo){
		ErrorCode code = partnerSynchService.pushCallbackOrder(tradeNo, PayConstant.PUSH_FLAG_PAID);
		if(code.isSuccess()) return "success";
		return "error";
	}
	@RequestMapping("/admin/ticket/order/addAgentOrder.xhtml")
	public String addAgentOrder(Long mpid, Long memberid, String oldTradeNo, String seatno, ModelMap model){
		if((StringUtils.isBlank(oldTradeNo) && memberid==null) || StringUtils.isBlank(seatno) || mpid==null){
			return showError(model, "参数错误！");
		}
		TicketOrder oldOrder = daoService.getObjectByUkey(TicketOrder.class, "tradeNo", oldTradeNo);
		if(oldOrder==null || !oldOrder.getStatus().equals(OrderConstant.STATUS_PAID_RETURN)){
			return showError(model, "订单不存在或状态错误！");
		}
		return "admin/ticket/order/addAgentOrder.vm";
	}
	
}
