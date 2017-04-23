package com.gewara.web.action.admin.ticket;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import com.gewara.model.pay.TicketOrder;
import com.gewara.model.ticket.OpenPlayItem;
import com.gewara.service.OrderException;
import com.gewara.service.ticket.TicketOrderService;
import com.gewara.service.ticket.TicketProcessService;
import com.gewara.service.ticket.WandaService;
import com.gewara.support.ErrorCode;
import com.gewara.untrans.ticket.RemoteTicketService;
import com.gewara.util.DateUtil;
import com.gewara.util.StringUtil;
import com.gewara.util.WebUtils;
import com.gewara.web.action.admin.BaseAdminController;
import com.gewara.xmlbind.ticket.TicketRemoteOrder;

@Controller
public class WdOrderAdminController extends BaseAdminController {
	@Autowired@Qualifier("remoteTicketService")
	private RemoteTicketService remoteTicketService;
	@Autowired@Qualifier("wandaService")
	private WandaService wandaService;
	@Autowired@Qualifier("ticketOrderService")
	private TicketOrderService ticketOrderService;
	@Autowired@Qualifier("ticketProcessService")
	private TicketProcessService ticketProcessService;
	
	@RequestMapping("/admin/ticket/wanda/changeSeat.xhtml")
	public String addProxyOrder(String tradeNo, Long mpid, ModelMap model){
		if(StringUtils.isBlank(tradeNo)){
			return forwardMessage(model, "请输入订单号!");
		}
		TicketOrder order = daoService.getObjectByUkey(TicketOrder.class, "tradeNo", tradeNo);
		ErrorCode allow = wandaService.checkAllowChangeSeat(order);
		if(!allow.isSuccess()){
			return forwardMessage(model, allow.getMsg());
		}
		if(mpid==null){
			mpid = order.getMpid();
		}
		OpenPlayItem opi = daoService.getObjectByUkey(OpenPlayItem.class, "mpid", mpid);
		
		ErrorCode<String> result = wandaService.getProxySeatPage(opi.getSeqNo(), mpid, order.getMemberid(), order.getMembername(), order.getMobile(), tradeNo);
		if(!result.isSuccess()) return showError(model, result.getMsg());
		return showRedirect(result.getRetval(), model);
	}
	@RequestMapping("/admin/ticket/wanda/changeOrder.xhtml")
	public String createWdOrder(String oldTradeNo, String orderId/*wdOrderId*/, String key, 
			HttpServletRequest request, ModelMap model){
		dbLogger.warn("WANDAORDER_REPLY:" + WebUtils.getRequestMap(request));

		String[] keyPair = StringUtils.split(key, "@");
		if(StringUtils.isBlank(key) || keyPair.length!=2 || keyPair[0].equals(StringUtil.md5WithKey(keyPair[0], 10))){
			return forwardMessage(model, "订单请求有错误！");
		}
		Map<String, String> info = wandaService.getKeyResult(keyPair[0]);
		if(info==null){
			return forwardMessage(model, "订单请求已超时！");
		}
		Long mpid = Long.parseLong(info.get("mpid"));
		OpenPlayItem opi = daoService.getObjectByUkey(OpenPlayItem.class, "mpid", mpid, true);
		if(opi == null || opi.isUnOpenToGewa()) return showError(model, "本场次已停止售票！");
		String seqno = info.get("seqno");

		ErrorCode<TicketRemoteOrder> result = remoteTicketService.getWdRemoteOrder(orderId, opi.getSeqNo(), opi.getCinemaid());
		if(result.isSuccess()){
			TicketRemoteOrder wdOrder = result.getRetval();
			String randomNum = ticketOrderService.nextRandomNum(DateUtil.addDay(opi.getPlaytime(), 1), 8, "0");
			try{
				TicketOrder order = ticketProcessService.wdChangeSeat(opi, oldTradeNo, wdOrder, orderId, randomNum);
				//创建远程万达订单
				remoteTicketService.createWdRemoteOrder(seqno, orderId, order.getId(), opi.getCinemaid());
				return showMessage(model, "换座成功，请到场次页面“确认订单”！");
			}catch(OrderException e){
				remoteTicketService.unlockWandaOrder(orderId, opi.getCinemaid());
				dbLogger.warn("", e);
				return forwardMessage(model, result.getMsg());
			}
		}
		return forwardMessage(model, "换座失败：" + result.getMsg());
	}

}
