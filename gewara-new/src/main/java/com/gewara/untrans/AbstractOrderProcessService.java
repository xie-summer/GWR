package com.gewara.untrans;

import java.sql.Timestamp;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import com.gewara.Config;
import com.gewara.constant.ApiConstant;
import com.gewara.constant.PayConstant;
import com.gewara.constant.sys.LogTypeConstant;
import com.gewara.constant.ticket.OpiConstant;
import com.gewara.constant.ticket.OrderConstant;
import com.gewara.helper.order.OrderContainer;
import com.gewara.jms.JmsConstant;
import com.gewara.model.drama.DramaOrder;
import com.gewara.model.drama.OpenDramaItem;
import com.gewara.model.goods.ActivityGoods;
import com.gewara.model.goods.BaseGoods;
import com.gewara.model.partner.CallbackOrder;
import com.gewara.model.pay.Discount;
import com.gewara.model.pay.ElecCard;
import com.gewara.model.pay.GewaOrder;
import com.gewara.model.pay.GoodsOrder;
import com.gewara.model.pay.GymOrder;
import com.gewara.model.pay.MemberCardOrder;
import com.gewara.model.pay.PubSaleOrder;
import com.gewara.model.pay.SMSRecord;
import com.gewara.model.pay.SpecialDiscount;
import com.gewara.model.pay.SportOrder;
import com.gewara.model.pay.TicketOrder;
import com.gewara.model.sport.MemberCardType;
import com.gewara.model.sport.OpenTimeItem;
import com.gewara.model.sport.OpenTimeTable;
import com.gewara.model.ticket.OpenPlayItem;
import com.gewara.model.ticket.SellSeat;
import com.gewara.service.DaoService;
import com.gewara.service.MessageService;
import com.gewara.service.OpenGymService;
import com.gewara.service.OperationService;
import com.gewara.service.OrderException;
import com.gewara.service.drama.DramaOrderService;
import com.gewara.service.gewapay.PaymentService;
import com.gewara.service.member.PointService;
import com.gewara.service.order.GoodsOrderService;
import com.gewara.service.order.PubSaleService;
import com.gewara.service.partner.PartnerSynchService;
import com.gewara.service.sport.MemberCardService;
import com.gewara.service.sport.SportOrderService;
import com.gewara.service.ticket.TicketOrderService;
import com.gewara.service.ticket.TicketProcessService;
import com.gewara.support.ErrorCode;
import com.gewara.untrans.drama.TheatreOrderService;
import com.gewara.untrans.monitor.MonitorService;
import com.gewara.untrans.monitor.SysLogType;
import com.gewara.untrans.sport.SportUntransService;
import com.gewara.untrans.ticket.SpecialDiscountService;
import com.gewara.untrans.ticket.TicketGoodsService;
import com.gewara.untrans.ticket.TicketOperationService;
import com.gewara.util.BeanUtil;
import com.gewara.util.GewaLogger;
import com.gewara.util.JsonUtils;
import com.gewara.util.LoggerUtils;
import com.gewara.util.StringUtil;
import com.gewara.util.VmUtils;
import com.gewara.xmlbind.ticket.TicketRemoteOrder;

public abstract class AbstractOrderProcessService {
	public static final int PROCESS_INTERVAL = 120;
	
	public static final String PROCESS_ORDER = "processOrder";
	
	protected final transient GewaLogger dbLogger = LoggerUtils.getLogger(getClass(), Config.getServerIp(), Config.SYSTEMID);
	
	@Autowired@Qualifier("daoService")
	protected DaoService daoService;
	
	@Autowired@Qualifier("operationService")
	protected OperationService operationService;
	
	@Autowired@Qualifier("ticketOrderService")
	protected TicketOrderService ticketOrderService;
	
	@Autowired@Qualifier("goodsOrderService")
	protected GoodsOrderService goodsOrderService;

	@Autowired@Qualifier("ticketGoodsService")
	protected TicketGoodsService ticketGoodsService;
	
	@Autowired@Qualifier("jmsService")
	protected JmsService jmsService;
	
	@Autowired@Qualifier("sportOrderService")
	protected SportOrderService sportOrderService;
	
	@Autowired@Qualifier("sportUntransService")
	protected SportUntransService sportUntransService;
	
	@Autowired@Qualifier("ticketOperationService")
	protected TicketOperationService ticketOperationService;

	@Autowired@Qualifier("messageService")
	protected MessageService messageService;

	@Autowired@Qualifier("untransService")
	protected UntransService untransService;
	
	@Autowired@Qualifier("monitorService")
	protected MonitorService monitorService;
	
	@Autowired@Qualifier("partnerSynchService")
	protected PartnerSynchService partnerSynchService;

	@Autowired@Qualifier("theatreOrderService")
	protected TheatreOrderService theatreOrderService;

	@Autowired@Qualifier("pointService")
	protected PointService pointService;

	@Autowired@Qualifier("pubSaleService")
	protected PubSaleService pubSaleService;
	
	@Autowired@Qualifier("ticketProcessService")
	protected TicketProcessService ticketProcessService;
	
	@Autowired@Qualifier("specialDiscountService")
	protected SpecialDiscountService specialDiscountService;
	
	@Autowired@Qualifier("dramaOrderService")
	protected DramaOrderService dramaOrderService;

	@Autowired@Qualifier("paymentService")
	protected PaymentService paymentService;

	@Autowired@Qualifier("openGymService")
	protected OpenGymService openGymService;
	
	@Autowired@Qualifier("memberCardService")
	protected MemberCardService memberCardService;
	
	/**
	 * 处理订单状态
	 * @param order
	 * @return
	 * @throws OrderException
	 */
	private ErrorCode<OrderContainer> processOrderConfirm(GewaOrder order) throws OrderException{
		Timestamp curtime = new Timestamp(System.currentTimeMillis());
		OrderContainer container = null;
		if (order instanceof TicketOrder) {
			TicketOrder torder = (TicketOrder) order;
			OpenPlayItem opi = daoService.getObjectByUkey(OpenPlayItem.class, "mpid", torder.getMpid(), true);
			if (opi.isExpired()){
				return ErrorCode.getFailure("场次已过期");
			}
			List<SellSeat> seatList = ticketOrderService.getOrderSeatList(order.getId());
			container = ticketOrderService.processOrderPay(torder, opi, seatList);
			if(container != null){
				container.setCostprice(opi.getCostprice());
			}
			ErrorCode check = ticketOrderService.checkOrderSeat(torder,	seatList);
			if (!check.isSuccess())
				throw new OrderException(ApiConstant.CODE_SEAT_OCCUPIED, check.getMsg());
			if (!opi.hasGewara()) {
				ErrorCode<TicketRemoteOrder> fixResult = ticketOperationService.setAndFixRemoteOrder(opi, torder, seatList);
				if (!fixResult.isSuccess()) {
					return ErrorCode.getFailure("订单错误" + order.getTradeNo() + "：" + fixResult.getMsg());
				}
				TicketRemoteOrder remoteOrder = fixResult.getRetval();
				if (remoteOrder.hasFixed()) {
					dbLogger.warn("远程订单成功：" + order.getTradeNo() + ",confirmId:" + remoteOrder.getConfirmationId());
					torder.setHfhpass(remoteOrder.getConfirmationId());
					Map<String,String> otherMap = JsonUtils.readJsonToMap(torder.getOtherinfo());
					otherMap.put("hfhId", remoteOrder.getBookingId());						
					if(StringUtils.equals(OpiConstant.OPEN_JY, remoteOrder.getOrderType())){
						Map<String,String> remoteOtherMap = JsonUtils.readJsonToMap(remoteOrder.getOtherinfo());
						otherMap.put("金逸取票密码", remoteOtherMap.get("voucherId"));
						otherMap.put("满天星取票订单号", remoteOtherMap.get("exOrderNo"));
						otherMap.put("满天星取票密码", remoteOtherMap.get("ticketNo"));
					}			
					torder.setOtherinfo(JsonUtils.writeMapToJson(otherMap));
					ticketOperationService.addLockSeatToQryResponse(opi.getMpid(), seatList);
				} else {// 不可能发生，但预防一下
					dbLogger.error("RemoteOrder Error：" + BeanUtil.buildString(remoteOrder, false));
					return ErrorCode.getFailure("确认订单失败：" + BeanUtil.buildString(remoteOrder, false));
				}
			}
			ticketOrderService.processSuccess(torder, seatList, curtime);
		} else if (order instanceof GoodsOrder) {
			GoodsOrder gorder = (GoodsOrder)order;
			container = goodsOrderService.processOrderPay(gorder);
			BaseGoods goods = daoService.getObject(BaseGoods.class, gorder.getGoodsid());
			ErrorCode<String> code = ticketGoodsService.payTicketGoodsOrder(gorder, goods);
			if(!code.isSuccess()) return ErrorCode.getFailure("确认订单失败：" + code.getMsg());
			if(goods instanceof ActivityGoods){
				jmsService.sendMsgToDst(JmsConstant.QUEUE_ORDER_ACTIVITYGOODS, JmsConstant.TAG_ORDER, "tradeNo", order.getTradeNo());
				dbLogger.warnWithType(LogTypeConstant.LOG_TYPE_ORDER_PAY, "活动订单号：" + gorder.getTradeNo() + "，加入消息队列");
			}
		} else if (order instanceof SportOrder) {
			SportOrder sorder = (SportOrder) order;
			OpenTimeTable ott = daoService.getObject(OpenTimeTable.class, sorder.getOttid());
			container = sportOrderService.processOrderPay(sorder, ott);
			List<OpenTimeItem> otiList = sportOrderService.getMyOtiList(sorder.getId());
			ErrorCode check = sportOrderService.checkOrderField(sorder,	otiList);
			if (!check.isSuccess()) {
				dbLogger.warn(sorder.getTradeNo() + "检查场地失败：" + check.getMsg());
				throw new OrderException(ApiConstant.CODE_SEAT_OCCUPIED, check.getMsg());
			}
			ErrorCode code = sportUntransService.processSportOrder(sorder, ott, otiList);
			if (!code.isSuccess()) return code;
		} else if (order instanceof DramaOrder) {
			DramaOrder dorder = (DramaOrder) order;
			OpenDramaItem odi = daoService.getObjectByUkey(OpenDramaItem.class, "dpid", dorder.getDpid(), false);
			container = dramaOrderService.processOrderPay(dorder);
			ErrorCode code = theatreOrderService.payDramaOrder(dorder, odi);
			if(!code.isSuccess()){
				dbLogger.warnWithType(LogTypeConstant.LOG_TYPE_ORDER_PAY, "处理订单号：" + dorder.getTradeNo() + "失败，" + code.getMsg());
				return code;
			}
		} else if (order instanceof GymOrder) {
			GymOrder gymOrder = (GymOrder) order;
			container = openGymService.processOrderPay(gymOrder);
			ErrorCode code = openGymService.processGymOrder(gymOrder);
			if(code.isSuccess()){
				jmsService.sendMsgToDst(JmsConstant.QUEUE_ORDER_GYM, JmsConstant.TAG_ORDER, "tradeNo", gymOrder.getTradeNo());
			}
		} else if (order instanceof PubSaleOrder) {
			PubSaleOrder porder = (PubSaleOrder) order;
			pubSaleService.processPubSaleOrder(porder);
		}else if (order instanceof MemberCardOrder){
			MemberCardOrder morder = (MemberCardOrder) order;
			container = memberCardService.processOrderPay(morder);
			MemberCardType mct = daoService.getObject(MemberCardType.class, morder.getMctid());
			memberCardService.processMemberCardOrder(morder, mct);
		}
		return ErrorCode.getSuccessReturn(container);
	}
	
	/**
	 * 处理订单异常后处理信息
	 * @param order
	 * @param from
	 * @param retry
	 * @param e
	 * @return
	 */
	private String processOrderException(GewaOrder order, String from, String retry, OrderException e){
		dbLogger.error(from + "订单错误" + order.getTradeNo() + "："	+ StringUtil.getExceptionTrace(e, 5));
		String processtimes = VmUtils.getJsonValueByKey(order.getChangehis(), OrderConstant.CHANGEHIS_KEY_PROCESSTIMES);
		if (ApiConstant.CODE_SEAT_OCCUPIED.equals(e.getCode())// 座位被占用
				|| ApiConstant.CODE_PAY_ERROR.equals(e.getCode())) {// 支付错误
			processtimes = "5";//次数直接达到
			order.setOtherinfo(JsonUtils.addJsonKeyValue(order.getOtherinfo(),	OrderConstant.OTHERKEY_PROCESSERROR, e.getMsg()));
		} else {
			if (StringUtils.isBlank(processtimes)) {
				processtimes = "1";
			} else {
				processtimes = (Integer.parseInt(processtimes) + 1) + "";
			}
		}
		order.addChangehis(OrderConstant.CHANGEHIS_KEY_PROCESSTIMES, processtimes);
		daoService.saveObject(order);
		if (!StringUtils.equals(retry, "0") && order instanceof TicketOrder	&& order.isAllPaid()) {
			try {
				boolean allowSendSms = operationService.updateOperation("sendFailure" + order.getTradeNo(),
						OperationService.ONE_HOUR * 12);
				if (allowSendSms && order.getDiscount() <= 0) {// 参加优惠的不发
					// 第二次处理没成功，发送一次短信
					dbLogger.warn("第一次处理失败，发短信：" + order.getTradeNo());
					String msg = "您的订单已付款，因影院售票系统繁忙，取票短信可能延迟，客服人员正在处理，请您耐心等待！";
					SMSRecord sms = messageService.addManualMsg(((TicketOrder) order).getMpid(), order.getMobile(), msg, order.getTradeNo()	+ order.getMobile());
					untransService.sendMsgAtServer(sms, false);
				}
			} catch (Exception e1) {
				dbLogger.warn(StringUtil.getExceptionTrace(e1, 4));
			}
		}
		String msg = "订单错误" + order.getTradeNo() + "：" + e.getMessage() + ", " + e.getDetailMsg();
		return msg;
	}
	
	protected final ErrorCode processOrderInternal(GewaOrder order, String from, String retry) {
		try {
			ErrorCode<OrderContainer> code = processOrderConfirm(order);
			if(!code.isSuccess()){
				return ErrorCode.getFailure(code.getMsg(), code.getMsg());
			}
			OrderContainer container = code.getRetval();
			if(container!=null && container.getUseCardMap()!=null){
				logCardUsage(container, order, container.getCostprice());
			}
			return postProcess(order, container);
		} catch (OrderException e) {
			String msg = processOrderException(order, from, retry, e);
			return ErrorCode.getFailure(msg);
		} catch (Throwable e) {
			// 支付成功，但订单有错
			dbLogger.error(from + "订单错误" + order.getTradeNo() + "："	+ StringUtil.getExceptionTrace(e, 5));
			return ErrorCode.getFailure("订单错误" + order.getTradeNo() + "：" + e.getMessage());
		}
	}
	
	protected final void logCardUsage(OrderContainer container, GewaOrder order, Integer costprice) {
		Map<Long, ElecCard> cardMap = container.getUseCardMap();
		for(Discount discount: container.getDiscountList()){
			ElecCard card = cardMap.get(discount.getId());
			if(card!=null){
				Map entry = BeanUtil.getBeanMapWithKey(card, "cardno", "orderid", "status");
				entry.put("discount", discount.getAmount());
				if(card.getCardtype().equals(PayConstant.CARDTYPE_A)){//
					if(costprice!=null) {
						entry.put("costprice", costprice);
					}else{
						//TODO：非订票订单这里有问题
						entry.put("costprice", discount.getAmount());
					}
				}else{
					entry.put("costprice", discount.getAmount());
				}
				entry.put("memberid", order.getMemberid());
				entry.put("membername", order.getMembername());
				entry.put("batchid", card.getEbatch().getId());
				monitorService.addSysLog(SysLogType.useElecCard, BeanUtil.toSimpleStringMap(entry));
			}
		}
		
	}
	protected final ErrorCode postProcess(GewaOrder order, OrderContainer container) throws Throwable{
		if (order.isPaidSuccess()) {
			Map<String, String> changeHisMap = JsonUtils.readJsonToMap(order.getChangehis());
			String successChange = changeHisMap.get(OrderConstant.CHANGEHIS_KEY_SUCCESSCHANGE);
			//是否是成功订单代处理 success = "true" 
			final boolean checkChange = Boolean.parseBoolean(successChange);
			if(!checkChange){
				//1)额外积分
				pointService.addOrderPoint(order);
				List<Discount> discountList = null;
				if(container == null){
					discountList = paymentService.getOrderDiscountList(order);
				}else{
					discountList = container.getDiscountList();
				}
				//2)特价活动总体名额减少
				for (Discount discount : discountList) {
					if (PayConstant.DISCOUNT_TAG_PARTNER.equals(discount.getTag())) {
						SpecialDiscount sd = null;
						if(container==null){
							sd = daoService.getObject(SpecialDiscount.class, discount.getRelatedid());
						}else{
							sd = container.getSd();
						}
						specialDiscountService.updateSpdiscountPaidCount(order, sd);
						break;
					}
				}
				
				//3)商家订单回调
				CallbackOrder corder = partnerSynchService.addCallbackOrder(order, PayConstant.PUSH_FLAG_SUCCESS, true);
				if (corder != null){
					partnerSynchService.pushCallbackOrder(corder);
				}
			}
			//4)发送成功消息
			try {
				jmsService.sendMsgToDst(JmsConstant.QUEUE_SUCCORDER, JmsConstant.TAG_ORDER, "tradeNo", order.getTradeNo());
			} catch (Exception e) {
			}
			//5)发送短信
			try {
				List<SMSRecord> smsList = messageService.addMessage(order).getRetval();// 只在notify中发短信
				if(!CollectionUtils.isEmpty(smsList)){
					for (SMSRecord sms : smsList) {
						if(sms!=null) untransService.sendMsgAtServer(sms, false);
					}
				}
			} catch (Exception e) {
				dbLogger.error("立即处理：发送消息有错误：" + order.getTradeNo(), e);
			}
			return ErrorCode.getSuccess("处理成功！");
		} else {
			return ErrorCode.getFailure("订单状态有错误：" + order.getStatus());
		}
	}
}
