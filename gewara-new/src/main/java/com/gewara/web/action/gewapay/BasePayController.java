package com.gewara.web.action.gewapay;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;

import com.gewara.Config;
import com.gewara.constant.sys.CacheConstant;
import com.gewara.constant.sys.LogTypeConstant;
import com.gewara.jms.JmsConstant;
import com.gewara.model.acl.GewaraUser;
import com.gewara.model.user.Member;
import com.gewara.service.gewapay.PaymentService;
import com.gewara.service.ticket.TicketOrderService;
import com.gewara.untrans.CacheService;
import com.gewara.untrans.JmsService;
import com.gewara.untrans.UntransService;
import com.gewara.untrans.monitor.MonitorService;
import com.gewara.untrans.monitor.OrderMonitorService;
import com.gewara.util.DateUtil;
import com.gewara.web.action.AnnotationController;

/**
 * @author acerge(acerge@163.com)
 * @since 6:18:42 PM Mar 16, 2010
 */
@Controller
public class BasePayController extends AnnotationController{
	@Autowired@Qualifier("monitorService")
	protected MonitorService monitorService;
	public void setMonitorService(MonitorService monitorService) {
		this.monitorService = monitorService;
	}
	@Autowired@Qualifier("orderMonitorService")
	protected OrderMonitorService orderMonitorService;

	@Autowired@Qualifier("config")
	protected Config config;
	public void setConfig(Config config) {
		this.config = config;
	}
	@Autowired@Qualifier("cacheService")
	protected CacheService cacheService;
	public void setCacheService(CacheService cacheService) {
		this.cacheService = cacheService;
	}
	@Autowired@Qualifier("paymentService")
	protected PaymentService paymentService;
	public void setPaymentService(PaymentService paymentService) {
		this.paymentService = paymentService;
	}

	@Autowired@Qualifier("jmsService")
	protected JmsService jmsService;
	public void setJmsService(JmsService jmsService) {
		this.jmsService = jmsService;
	}

	@Autowired@Qualifier("ticketOrderService")
	protected TicketOrderService ticketOrderService;
	public void setTicketOrderService(TicketOrderService ticketOrderService) {
		this.ticketOrderService = ticketOrderService;
	}
	@Autowired@Qualifier("untransService")
	protected UntransService untransService;
	public void setUntransService(UntransService untransService) {
		this.untransService = untransService;
	}
	protected void processPay(String tradeNo, String from){
		String key = "processOrder" + tradeNo;
		Long last = (Long) cacheService.get(CacheConstant.REGION_TENMIN, key);
		Long cur = System.currentTimeMillis();
		cacheService.set(CacheConstant.REGION_TENMIN, key, cur);
		if(last != null && last + DateUtil.m_minute * 5 > cur) {//5分钟内只处理一次
			dbLogger.warnWithType(LogTypeConstant.LOG_TYPE_ORDER_PAY, "忽略订单处理调用：" + key);
			return;
		}
		jmsService.sendMsgToDst(JmsConstant.QUEUE_PAY, JmsConstant.TAG_ORDER, "tradeNo,from", tradeNo, from);
	}
	protected void processCharge(String tradeNo, String from){
		String key = "processCharge" + tradeNo;
		Long last = (Long) cacheService.get(CacheConstant.REGION_TENMIN, key);
		Long cur = System.currentTimeMillis();
		cacheService.set(CacheConstant.REGION_TENMIN, key, cur);
		if(last != null && last + DateUtil.m_minute * 5 > cur) {//5分钟内只处理一次
			dbLogger.warnWithType(LogTypeConstant.LOG_TYPE_ORDER_PAY, "忽略订单处理调用：" + key);
			return;
		}
		jmsService.sendMsgToDst(JmsConstant.QUEUE_CHARGE, JmsConstant.TAG_CHARGE, "tradeNo,from", tradeNo, from);
	}
	protected String showWapMsg(ModelMap model, String msg){
		model.put("msg", msg);
		model.put("url", "index.xhtml");
		return "wap/wapTip.vm";
	}
	protected final Member getLogonMember(){
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		if(auth == null) return null;
		if(auth.isAuthenticated() && !auth.getName().equals("anonymous")){//登录
			GewaraUser user = (GewaraUser) auth.getPrincipal();
			//refresh(user);
			if(user instanceof Member) return (Member)user;
		}
		return null;
	}
}
