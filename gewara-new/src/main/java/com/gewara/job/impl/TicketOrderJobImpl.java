package com.gewara.job.impl;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.orm.hibernate3.HibernateTemplate;

import com.gewara.Config;
import com.gewara.bank.ChinaOrderQry;
import com.gewara.constant.PaymethodConstant;
import com.gewara.constant.sys.ConfigConstant;
import com.gewara.constant.sys.LogTypeConstant;
import com.gewara.constant.ticket.OpiConstant;
import com.gewara.constant.ticket.OrderConstant;
import com.gewara.job.JobService;
import com.gewara.job.TicketOrderJob;
import com.gewara.model.common.GewaConfig;
import com.gewara.model.partner.CallbackOrder;
import com.gewara.model.pay.GewaOrder;
import com.gewara.model.pay.SMSRecord;
import com.gewara.model.pay.TicketOrder;
import com.gewara.model.ticket.OpenPlayItem;
import com.gewara.pay.ChinapayUtil;
import com.gewara.pay.UmPayUtil;
import com.gewara.service.DaoService;
import com.gewara.service.MessageService;
import com.gewara.service.OperationService;
import com.gewara.service.order.OrderQueryService;
import com.gewara.service.partner.PartnerSynchService;
import com.gewara.support.ErrorCode;
import com.gewara.untrans.UntransService;
import com.gewara.untrans.monitor.MonitorService;
import com.gewara.untrans.monitor.RoleTag;
import com.gewara.untrans.ticket.BaseOpenService;
import com.gewara.untrans.ticket.MpiOpenService;
import com.gewara.untrans.ticket.OrderProcessService;
import com.gewara.util.DateUtil;
import com.gewara.util.HttpResult;
import com.gewara.util.HttpUtils;
import com.gewara.util.StringUtil;

public class TicketOrderJobImpl extends JobService implements TicketOrderJob {
	
	@Autowired
	@Qualifier("orderQueryService")
	private OrderQueryService orderQueryService;
	@Autowired
	@Qualifier("monitorService")
	private MonitorService monitorService;

	@Autowired
	@Qualifier("hibernateTemplate")
	private HibernateTemplate hibernateTemplate;

	@Autowired
	@Qualifier("operationService")
	private OperationService operationService;

	public void setOperationService(OperationService operationService) {
		this.operationService = operationService;
	}

	@Autowired
	@Qualifier("messageService")
	private MessageService messageService;

	public void setMessageService(MessageService messageService) {
		this.messageService = messageService;
	}

	@Autowired
	@Qualifier("config")
	private Config config;

	public void setConfig(Config config) {
		this.config = config;
	}

	@Autowired
	@Qualifier("daoService")
	private DaoService daoService;

	public void setDaoService(DaoService daoService) {
		this.daoService = daoService;
	}

	@Autowired
	@Qualifier("partnerSynchService")
	private PartnerSynchService partnerSynchService;

	public void setPartnerSynchService(PartnerSynchService partnerSynchService) {
		this.partnerSynchService = partnerSynchService;
	}

	@Autowired
	@Qualifier("untransService")
	private UntransService untransService;
	public void setUntransService(UntransService untransService) {
		this.untransService = untransService;
	}

	@Autowired
	@Qualifier("orderProcessService")
	private OrderProcessService orderProcessService;
	public void setProcessOrderService(OrderProcessService orderProcessService) {
		this.orderProcessService = orderProcessService;
	}
	@Autowired
	@Qualifier("mpiOpenService")
	private MpiOpenService mpiOpenService;
	@Autowired
	@Qualifier("mtxOpenService")
	private BaseOpenService mtxOpenService;	

	public void updateOpiStats() {
		//更新火凤凰快要过期的场次订票量
		Timestamp cur = new Timestamp(System.currentTimeMillis());
		String query = "select p.id from OpenPlayItem p where p.opentype = ? and p.playtime > ? and p.playtime <= ? and (p.otherinfo not like ? or p.otherinfo is null) order by p.playtime";//
		List<Long> opidList = daoService.queryByRowsRange(query, 0, 20000, OpiConstant.OPEN_HFH, DateUtil.addHour(cur, -48),
				DateUtil.addMinute(cur, -30), "%" + OpiConstant.STATISTICS + "%");
		int errorCount = 0; 
		for (Long opid : opidList) {
			try{
				OpenPlayItem opi = daoService.getObject(OpenPlayItem.class, opid);
				mpiOpenService.asynchUpdateOpiStats(opi, true);
			}catch(Exception e){
				errorCount ++;
			}
		}
		if(errorCount > 0){
			dbLogger.warn("更新场次统计，总共：" + opidList.size() + "错误：" + errorCount);
		}
	}
	
	public void updateOpiStatsByMtx() {
		//更新满天星快要过期的场次订票量
		Timestamp cur = new Timestamp(System.currentTimeMillis());
		String query = "select p.id from OpenPlayItem p where p.opentype = ? and p.playtime > ? and p.playtime <= ? and (p.otherinfo not like ? or p.otherinfo is null) order by p.playtime";//
		List<Long> opidList = daoService.queryByRowsRange(query, 0, 20000, OpiConstant.OPEN_MTX, DateUtil.addHour(cur, -48),
				DateUtil.addMinute(cur, -30), "%" + OpiConstant.STATISTICS + "%");
		int errorCount = 0; 
		for (Long opid : opidList) {
			try{
				OpenPlayItem opi = daoService.getObject(OpenPlayItem.class, opid);
				mtxOpenService.asynchUpdateOpiStats(opi, true);
			}catch(Exception e){
				errorCount ++;
			}
		}
		if(errorCount > 0){
			dbLogger.warn("更新场次统计，总共：" + opidList.size() + "错误：" + errorCount);
		}
	}

	private Map<String, Integer> retryMap = new HashMap<String, Integer>();// 处理次数

	@Override
	public void correctOrder() {
		Long cur = System.currentTimeMillis();
		List<TicketOrder> failureList = orderQueryService.getPaidUnfixOrderList(0, 50);
		if (failureList.size() == 0)
			return;
		dbLogger.warnWithType(LogTypeConstant.LOG_TYPE_JOB, "自动确认“待处理”订单，共" + failureList.size() + "个");
		List<String> msgList = new ArrayList<String>();
		List<TicketOrder> processList = new ArrayList<TicketOrder>();
		List<TicketOrder> laterProcessList = new ArrayList<TicketOrder>();
		for (TicketOrder order : failureList) {
			if (retryMap.containsKey(order.getTradeNo())) {
				laterProcessList.add(order);
			} else {
				processList.add(order);
			}
		}
		processList.addAll(laterProcessList);
		for (TicketOrder order : processList) {
			Integer retried = retryMap.get(order.getTradeNo());
			if (retried == null) {
				retried = 0;
			}
			retried++;
			retryMap.put(order.getTradeNo(), retried);
			if (retried > 8) {
				dbLogger.warnWithType(LogTypeConstant.LOG_TYPE_JOB, "重试次数过多，忽略:" + order.getTradeNo());
				continue;
			}
			try {
				ErrorCode<String> result = null;
				if(order.needChangeSeat()){
					result = orderProcessService.reconfirmOrder(order, null, true, false);
				}else{
					OpenPlayItem opi = daoService.getObjectByUkey(OpenPlayItem.class, "mpid", order.getMpid());
					result = orderProcessService.confirmSuccess(order, opi, 1L, true);
				}
				if (result.isSuccess()) {
					String msg = "自动确认“待处理”订单：" + order.getTradeNo() + "成功！";
					dbLogger.warnWithType(LogTypeConstant.LOG_TYPE_JOB, msg);
					msgList.add(msg);
				} else {
					String msg = "自动确认“待处理”订单失败：" + order.getTradeNo() + result.getMsg();
					dbLogger.warnWithType(LogTypeConstant.LOG_TYPE_JOB, msg);
					msgList.add(msg);
				}
			} catch (Exception e) {
				dbLogger.warnWithType(LogTypeConstant.LOG_TYPE_JOB, StringUtil.getExceptionTrace(e, 5));
			}
			if (System.currentTimeMillis() - cur > DateUtil.m_minute * 13)
				break;
		}
	}

	@Override
	public void checkHfhOrder() {
		String url = "http://localhost:8080" + config.getBasePath() + "markHfhOrder.xhtml?check=";
		String check = StringUtil.md5WithKey(DateUtil.getCurDateStr());
		HttpResult result = HttpUtils.getUrlAsString(url + check);
		dbLogger.error("检查订单：" + result.getResponse());
	}

	@Override
	public void sendCallbackOrder() {
		List<CallbackOrder> callList = partnerSynchService.getCallbackOrderList(20);
		String msg = "本次共" + callList.size() + "个";
		int success = 0, failure = 0;
		String failureMsg = "";
		for (CallbackOrder call : callList) {
			ErrorCode code = partnerSynchService.pushCallbackOrder(call);
			if (code.isSuccess()) {
				success++;
			} else {
				failure++;
				failureMsg += code.getMsg();
			}
		}
		msg += "，成功" + success + "，失败" + failure + "，失败消息：" + failureMsg;
		if (callList.size() > 0)
			dbLogger.warnWithType(LogTypeConstant.LOG_TYPE_JOB, msg);
	}

	@Override
	public void unNotifyOrder() {
		Timestamp curtime = new Timestamp(System.currentTimeMillis());
		Timestamp validtime1 = DateUtil.addMinute(curtime, -75);
		Timestamp validtime2 = DateUtil.addMinute(curtime, -15);
		List<String> paymethodList = Arrays.asList(PaymethodConstant.PAYMETHOD_CHINAPAY1, PaymethodConstant.PAYMETHOD_CHINAPAY2, PaymethodConstant.PAYMETHOD_GDBPAY,
				PaymethodConstant.PAYMETHOD_CMBPAY, PaymethodConstant.PAYMETHOD_CMBWAPPAY, PaymethodConstant.PAYMETHOD_UMPAY, PaymethodConstant.PAYMETHOD_UMPAY_SH);
		DetachedCriteria query = DetachedCriteria.forClass(GewaOrder.class);
		query.add(Restrictions.ne("status", OrderConstant.STATUS_SYS_CANCEL));
		query.add(Restrictions.not(Restrictions.like("status", OrderConstant.STATUS_PAID, MatchMode.START)));
		query.add(Restrictions.ge("createtime", validtime1));
		query.add(Restrictions.le("createtime", validtime2));
		query.add(Restrictions.lt("validtime", curtime));
		query.add(Restrictions.in("paymethod", paymethodList));
		List<GewaOrder> orderList = hibernateTemplate.findByCriteria(query, 0, 100);
		dbLogger.warnWithType(LogTypeConstant.LOG_TYPE_JOB, "未通知订单任务执行，共" + orderList.size() + "个");
		String title = "订单支付成功但未收到通知！";
		for (GewaOrder order : orderList) {
			String paymethod = order.getPaymethod();
			boolean isPaid = false;
			if (StringUtils.equals(paymethod, PaymethodConstant.PAYMETHOD_CHINAPAY1) || StringUtils.equals(paymethod, PaymethodConstant.PAYMETHOD_CHINAPAY2)) {
				Map<String, String> params = ChinapayUtil.qryOrder(order);
				String response = ChinapayUtil.getQryRes(params);
				ChinaOrderQry orderQry = ChinapayUtil.getQryToObject(response);
				isPaid = orderQry != null && orderQry.isPaid();
			}else if (StringUtils.equals(paymethod, PaymethodConstant.PAYMETHOD_UMPAY) || StringUtils.equals(paymethod, PaymethodConstant.PAYMETHOD_UMPAY_SH)){
				ErrorCode ec = UmPayUtil.queryOrder(order);
				if(ec.isSuccess()){
					isPaid = true;
				}
			}
			if (isPaid) {
				monitorService.saveSysWarn(title, "订单号:" + order.getTradeNo(), RoleTag.dingpiao);
				dbLogger.warnWithType(LogTypeConstant.LOG_TYPE_JOB, title + order.getTradeNo());
			}
		}
	}

	public void sendWarningMsg() {
		try {
			int failureCount = orderQueryService.getPaidFailureOrderCount();
			dbLogger.warn("执行查询支付8小时内的，10分钟无更新的订单数量为：" + failureCount);
			if (failureCount > 30) {
				boolean allowSendSMS = operationService.updateOperation("failureOrderWarning", 60 * 60);
				if (allowSendSMS) {
					GewaConfig cfg = daoService.getObject(GewaConfig.class, ConfigConstant.CFG_YUNYIN_MOBILE);
					if (cfg != null && StringUtils.isNotBlank(cfg.getContent())) {
						String[] mobiles = StringUtils.split(cfg.getContent(), ",");
						for (String mobile : mobiles) {
							SMSRecord sms = messageService.addManualMsg(0L, mobile, "目前待处理订单过多，为" + failureCount + "笔", null);
							if (sms != null)
								untransService.sendMsgAtServer(sms, false);
						}
					}
				}
			}
		} catch (Exception e) {
			dbLogger.warn("待处理订单：", e);
		}
	}
}
