package com.gewara.web.action.gewapay;

import java.sql.Timestamp;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.RequestMapping;

import com.alibaba.dubbo.common.utils.StringUtils;
import com.gewara.constant.ticket.OrderConstant;
import com.gewara.helper.order.TicketOrderContainer;
import com.gewara.model.pay.TicketOrder;
import com.gewara.model.ticket.OpenPlayItem;
import com.gewara.model.user.Member;
import com.gewara.service.OrderException;
import com.gewara.service.ticket.TicketOrderService;
import com.gewara.service.ticket.WandaService;
import com.gewara.support.ErrorCode;
import com.gewara.untrans.ticket.RemoteTicketService;
import com.gewara.util.DateUtil;
import com.gewara.util.StringUtil;
import com.gewara.util.ValidateUtil;
import com.gewara.util.WebUtils;
import com.gewara.web.action.AnnotationController;
import com.gewara.xmlbind.ticket.TicketRemoteOrder;

@Controller
public class WdTicketController extends AnnotationController{
	@Autowired@Qualifier("remoteTicketService")
	private RemoteTicketService remoteTicketService;
	@Autowired@Qualifier("wandaService")
	private WandaService wandaService;
	@Autowired@Qualifier("ticketOrderService")
	private TicketOrderService ticketOrderService;
	
	@RequestMapping("/gewapay/gotoWd.xhtml")
	public String gotoWd(@CookieValue(value=LOGIN_COOKIE_NAME, required=false)String sessid, HttpServletRequest request, 
			String seqNo, Long mpid, String mobile, ModelMap model){
		if(ValidateUtil.isMobile(mobile)){
			Member member = loginService.getLogonMemberBySessid(WebUtils.getRemoteIp(request), sessid);
			ErrorCode<String> wdUser = remoteTicketService.getWdUserId(member.getId().toString());
			if(!wdUser.isSuccess()){
				return showJsonError(model, "影院系统繁忙，请稍候重试！");
			}
			String realUserId = wdUser.getRetval();
			ErrorCode<String> result = wandaService.getWebSeatPage(realUserId, seqNo, mpid, member.getId(), member.getNickname(), mobile);
			if(!result.isSuccess()) return showJsonError(model, result.getMsg());
			return showJsonSuccess(model, result.getRetval());
		}else{
			return showJsonError(model, "手机号不正确！");
		}
	}
		
	@RequestMapping("/gewapay/wdOrder.xhtml")
	public String createWdOrder(@CookieValue(value=LOGIN_COOKIE_NAME, required=false)String sessid,
			HttpServletRequest request, String orderId/*wdOrderId*/, String snid, String key, ModelMap model){
		Member member = loginService.getLogonMemberBySessid(WebUtils.getRemoteIp(request), sessid);
		dbLogger.warn("WANDAORDER_REPLY:" + WebUtils.getRequestMap(request));
		if(StringUtils.isBlank(orderId)){
			orderId = snid;//备份方案
		}
		ErrorCode<TicketOrder> result = createWdOrder(member, key, orderId);
		if(result.isSuccess()){
			model.put("orderId", result.getRetval().getId().toString());
			return "redirect:/gewapay/order.xhtml";
		}else{
			return forwardMessage(model, result.getMsg());
		}
	}
	private ErrorCode<TicketOrder> createWdOrder(Member member, String key, String wdOrderId){
		ErrorCode<Map<String, Object>> retCode = wandaService.validCreateWdOrder(key, member.getId(), wdOrderId);
		if(!retCode.isSuccess()) {
			return ErrorCode.getFailure(retCode.getMsg());
		}
		Map<String, Object> retMap = retCode.getRetval();
		String mobile = retMap.get("mobile")+"";
		String seqno = retMap.get("seqno")+"";
		OpenPlayItem opi = (OpenPlayItem)retMap.get("opi");
		String tradeNo = ticketOrderService.getTicketTradeNo();
		if(retMap.get("gotime")!=null){
			Timestamp gotime = DateUtil.parseTimestamp((String) retMap.get("gotime"));
			if(gotime!=null) {
				dbLogger.warn("WANDA-RETURN,elapsed:" + (System.currentTimeMillis() - gotime.getTime()) + " ms");
			}
		}

		ErrorCode<TicketRemoteOrder> result = remoteTicketService.getWdRemoteOrder(wdOrderId, opi.getSeqNo(), opi.getCinemaid());
		if(result.isSuccess()){
			TicketRemoteOrder wdOrder = result.getRetval();
			
			String randomNum = ticketOrderService.nextRandomNum(DateUtil.addDay(opi.getPlaytime(), 1), 8, "0");
			try{
				TicketOrderContainer orderContainer = wandaService.createTicketOrder(opi, member.getId(), member.getNickname(), ""+member.getId(), mobile, wdOrder, randomNum, tradeNo, null);
				TicketOrder order = orderContainer.getTicketOrder();
				ErrorCode<TicketRemoteOrder> remoteOrder = remoteTicketService.createWdRemoteOrder(seqno, wdOrderId, order.getId(), opi.getCinemaid());
				if(remoteOrder.isSuccess()){
					order.setStatus(OrderConstant.STATUS_NEW);
					daoService.saveObject(order);
					return ErrorCode.getSuccessReturn(order);
				}else{
					//TODO:重试？？
					return ErrorCode.getFailure(remoteOrder.getMsg());
				}
			}catch(OrderException e){
				remoteTicketService.unlockWandaOrder(wdOrderId, opi.getCinemaid());
				dbLogger.warn(StringUtil.getExceptionTrace(e, 5));
				return ErrorCode.getFailure(result.getMsg());
			}
		}else{
			remoteTicketService.unlockWandaOrder(wdOrderId, opi.getCinemaid());
		}
		return ErrorCode.getFailure(result.getMsg());
	}
}
