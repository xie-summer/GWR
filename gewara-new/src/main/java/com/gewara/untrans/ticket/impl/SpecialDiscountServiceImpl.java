package com.gewara.untrans.ticket.impl;

import java.sql.Timestamp;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.criterion.Subqueries;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.orm.hibernate3.HibernateOptimisticLockingFailureException;
import org.springframework.stereotype.Service;

import com.gewara.Config;
import com.gewara.constant.PayConstant;
import com.gewara.constant.ticket.OrderConstant;
import com.gewara.helper.discount.SportSpecialDiscountHelper.OrderCallback;
import com.gewara.helper.order.OrderContainer;
import com.gewara.model.pay.Discount;
import com.gewara.model.pay.GewaOrder;
import com.gewara.model.pay.SpCode;
import com.gewara.model.pay.SpecialDiscount;
import com.gewara.model.pay.TicketOrder;
import com.gewara.service.DaoService;
import com.gewara.service.OpenGymService;
import com.gewara.service.OperationService;
import com.gewara.service.OrderException;
import com.gewara.service.drama.DramaOrderService;
import com.gewara.service.gewapay.PaymentService;
import com.gewara.service.order.GoodsOrderService;
import com.gewara.service.sport.SportOrderService;
import com.gewara.service.ticket.TicketDiscountService;
import com.gewara.support.ErrorCode;
import com.gewara.support.ReadOnlyTemplate;
import com.gewara.untrans.monitor.MonitorService;
import com.gewara.untrans.monitor.OrderMonitorService;
import com.gewara.untrans.monitor.SysLogType;
import com.gewara.untrans.ticket.SpecialDiscountService;
import com.gewara.util.DateUtil;
import com.gewara.util.GewaLogger;
import com.gewara.util.JsonUtils;
import com.gewara.util.LoggerUtils;
import com.gewara.util.StringUtil;

/**
 * 处理乐观锁重试逻辑
 * @author gebiao(ge.biao@gewara.com)
 * @since Feb 21, 2013 8:41:16 PM
 */
@Service("specialDiscountService")
public class SpecialDiscountServiceImpl implements SpecialDiscountService{
	private final transient GewaLogger dbLogger = LoggerUtils.getLogger(getClass(), Config.getServerIp(), Config.SYSTEMID);
	@Autowired@Qualifier("ticketDiscountService")
	private TicketDiscountService ticketDiscountService;
	@Autowired@Qualifier("paymentService")
	private PaymentService paymentService;
	@Autowired@Qualifier("dramaOrderService")
	private DramaOrderService dramaOrderService;
	@Autowired@Qualifier("sportOrderService")
	private SportOrderService sportOrderService;
	@Autowired@Qualifier("openGymService")
	private OpenGymService openGymService;
	@Autowired@Qualifier("goodsOrderService")
	private GoodsOrderService goodsOrderService;
	@Autowired@Qualifier("operationService")
	private OperationService operationService;
	@Autowired@Qualifier("monitorService")
	private MonitorService monitorService;
	@Autowired@Qualifier("orderMonitorService")
	private OrderMonitorService orderMonitorService;
	@Autowired@Qualifier("readOnlyTemplate")
	private ReadOnlyTemplate readOnlyTemplate;
	@Autowired@Qualifier("daoService")
	private DaoService daoService;
	
	@Override
	public ErrorCode<OrderContainer> useSpecialDiscount(String ordertype, Long orderid, SpecialDiscount sd, String clientIP){
		if(StringUtils.isNotBlank(sd.getVerifyType())){
			return ErrorCode.getFailure("请输入电子码！");
		}
		return useSpecialDiscount(ordertype, orderid, sd, null, clientIP);
	}
	@Override
	public ErrorCode<OrderContainer> useSpecialDiscountBySpCodePass(String ordertype, Long orderid, String clientIP, Long memberid, String spcodePass) {
		SpCode spcode = ticketDiscountService.getSpCodeByPass(spcodePass);
		if(spcode==null){
			return ErrorCode.getFailure("您的电子码不存在或超时");
		}
		if(spcode.getMemberid()!=null && !memberid.equals(spcode.getMemberid())){
			return ErrorCode.getFailure("此电子码不存在或被他人占用！");
		}
		SpecialDiscount sd = daoService.getObject(SpecialDiscount.class, spcode.getSdid());
		return useSpecialDiscount(ordertype, orderid, sd, clientIP, spcode);
	}
	@Override
	public ErrorCode<OrderContainer> useSpecialDiscountBySpCodeId(String ordertype, Long orderid, String clientIP, Long memberid, Long spcodeId) {
		SpCode spcode = daoService.getObject(SpCode.class, spcodeId);
		if(spcode==null || spcode.getMemberid()==null || !memberid.equals(spcode.getMemberid())){
			return ErrorCode.getFailure("此电子码不存在或被他人占用！");
		}
		SpecialDiscount sd = daoService.getObject(SpecialDiscount.class, spcode.getSdid());
		return useSpecialDiscount(ordertype, orderid, sd, clientIP, spcode);
	}
	private ErrorCode<OrderContainer> useSpecialDiscount(String ordertype, Long orderid, SpecialDiscount sd, String clientIP, final SpCode spcode) {
		if(sd.getVerifyType().equals(SpecialDiscount.VERIFYTYPE_ONLYONE)){
			if(spcode.getUsedcount() > 0){
				return ErrorCode.getFailure("此电子码已使用过，不能再次使用！");
			}
		}
		return useSpecialDiscountFull(ordertype, orderid, sd, new OrderCallback(){
			@Override
			public void processOrder(SpecialDiscount spdiscount, GewaOrder gewaOrder) {
				if(StringUtils.equals(spdiscount.getVerifyType(), SpecialDiscount.VERIFYTYPE_ONLYONE)){
					gewaOrder.setOtherinfo(JsonUtils.addJsonKeyValue(gewaOrder.getOtherinfo(), PayConstant.KEY_USE_SPCODE, ""+spcode.getId()));
				}
			}
		}, clientIP);
	}

	@Override
	public ErrorCode<OrderContainer> useSpecialDiscount(String ordertype, Long orderid, SpecialDiscount sd, OrderCallback callback, String clientIP){
		if(StringUtils.isNotBlank(sd.getVerifyType())){
			return ErrorCode.getFailure("请输入电子码！");
		}
		return useSpecialDiscountFull(ordertype, orderid, sd, callback, clientIP);
	}
	
	private ErrorCode<OrderContainer> useSpecialDiscountFull(String ordertype, Long orderid, SpecialDiscount sd, OrderCallback callback, String clientIP){
		ErrorCode<OrderContainer> result = null;
		String key = clientIP + (DateUtil.getCurDateStr() + sd.getId()).hashCode();
		ErrorCode ipLimited = operationService.checkLimitInCache(key, sd.getIpLimitedOrderCount());
		if(!ipLimited.isSuccess()){
			Map<String, String> map = new HashMap<String, String>();
			map.put("orderId", String.valueOf(orderid));
			map.put("sdId", String.valueOf(sd.getId()));
			map.put("ip", clientIP);
			map.put("msg", ipLimited.getMsg());
			map.put("tag", "ipInvalid");
			map.put("category", "special");
			monitorService.addSysLog(SysLogType.monitor, map);
			return ErrorCode.getFailure("系统繁忙，请重试!");
		}
		
		for(int i=0; i < 3;i++){
			try{
				result = useSpecialDiscountRepeat(ordertype, orderid, sd, callback);
				return result;
			}catch(OrderException e){
				return ErrorCode.getFailure(e.getMsg());
			}catch(Throwable e){
				//非乐观锁异常
				if(!isUpdateErrorException(e)){
					dbLogger.warn(StringUtil.getExceptionTrace(e, 10));					
					return ErrorCode.getFailure("使用出错！");
				}else{
					dbLogger.warn("retrySpecialDiscount:" + sd.getId() + ":" + i + "," + StringUtil.getExceptionTrace(e, 3));
				}
			}
		}
		return ErrorCode.getFailure("使用出错！");
	}
	
	private ErrorCode<OrderContainer> useSpecialDiscountRepeat(String ordertype, Long orderid, SpecialDiscount sd, OrderCallback callback) throws Throwable {
		ErrorCode<OrderContainer> result = null;
		if(ordertype.equals(OrderConstant.ORDER_TYPE_TICKET)){
			result = ticketDiscountService.useSpecialDiscount(orderid, sd, callback);
		}else if(ordertype.equals(OrderConstant.ORDER_TYPE_DRAMA)){
			result = dramaOrderService.useSpecialDiscount(orderid, sd, callback);
		}else if(ordertype.equals(OrderConstant.ORDER_TYPE_GOODS)){
			result = goodsOrderService.useSpecialDiscount(orderid, sd, callback);
		}else if(ordertype.equals(OrderConstant.ORDER_TYPE_GYM)){
			result = openGymService.useSpecialDiscount(orderid, sd, callback);
		}else if(ordertype.equals(OrderConstant.ORDER_TYPE_SPORT)){
			result = sportOrderService.useSpecialDiscount(orderid, sd, callback);
		}
		
		Map<String, String> map = new HashMap<String, String>();
		map.put("orderId", String.valueOf(orderid));
		map.put("sdId", String.valueOf(sd.getId()));
		map.put("spflag", sd.getFlag());
		if(result.isSuccess()){
			GewaOrder order = result.getRetval().getOrder();		
			map.put("memberid", String.valueOf(order.getMemberid()));
			map.put("quantity", String.valueOf(order.getQuantity()));
			map.put("result", "Y");
			orderMonitorService.addOrderChangeLog(order.getTradeNo(), "使用特价", map, order.getMemberid());
		}else{
			map.put("result", "N");
			map.put("message", result.getMsg());
			map.put("tag", "useFailure");
			map.put("category", "special");
			monitorService.addSysLog(SysLogType.monitor, map);
		}
		
		return result;
	}
	
	@Override
	public void updateSpdiscountPaidCount(GewaOrder order, SpecialDiscount sd) throws Throwable {
		for(int i=0; i < 5; i++){
			try{
				paymentService.updateSpdiscountPaidCount(sd, order);
				return;
			}catch(Throwable e){
				//非乐观锁异常
				if(!isUpdateErrorException(e)){
					throw e;
				}else{
					dbLogger.warn("retrySpecialDiscount:" + sd.getId() + ":" + i + "," + StringUtil.getExceptionTrace(e, 3));
				}
			}
		}
	}

	protected boolean isUpdateErrorException(Throwable e){
		if(e instanceof HibernateOptimisticLockingFailureException){
			HibernateOptimisticLockingFailureException exc = (HibernateOptimisticLockingFailureException)e;
			dbLogger.warn(exc.getPersistentClassName() + ":" + exc.getIdentifier());
			return true;
		}
		return false;
	}
	
	/**
	 * 特价活动订单展示后台,为了处理方便所有订单统一用TicketOrder在Controller层做判断
	 * 
	 * @param discountIdList
	 *            特价活动ID
	 * @param fromTime
	 *            查询开始时间
	 * @param endTime
	 *            查询结束时间
	 * @return
	 */
	@Override
	public <T extends GewaOrder> List<T> getOrderListByDiscountIds(Class<T> clazz, List<Long> discountIdList, Date fromTime, Date endTime, int from, int maxnum) {
		DetachedCriteria query = getOrderByDiscountIdsCriteria(clazz, discountIdList, fromTime, endTime);
		List<T> ticketOrderList = readOnlyTemplate.findByCriteria(query, from, maxnum); 
		return ticketOrderList;
	}
	
	@Override
	public <T extends GewaOrder>  Integer getOrderCountByDiscountIds(Class<T> clazz, List<Long> discountIdList, Date fromTime, Date endTime) {
		DetachedCriteria query = getOrderByDiscountIdsCriteria(clazz, discountIdList, fromTime, endTime);
		query.setProjection(Projections.rowCount());
		Integer resultCount = new Integer("0");
		List<Long> list = readOnlyTemplate.findByCriteria(query);
		if (CollectionUtils.isNotEmpty(list)) {
			resultCount = Integer.valueOf(list.get(0).toString());
		}
		return resultCount;
	}
	
	/**
	 * 特价活动订单展示后台的查询
	 */
	private <T extends GewaOrder> DetachedCriteria getOrderByDiscountIdsCriteria(Class<T> clazz, List<Long> discountIdList, Date fromTime, Date endTime) {
		DetachedCriteria query = DetachedCriteria.forClass(clazz, "t");
		DetachedCriteria sub = DetachedCriteria.forClass(Discount.class, "d");
		sub.add(Restrictions.in("d.relatedid", discountIdList));
		sub.add(Restrictions.eqProperty("t.id", "d.orderid"));
		sub.setProjection(Projections.property("d.orderid"));
		query.add(Subqueries.exists(sub));
		query.add(Restrictions.ge("t.addtime", fromTime));
		query.add(Restrictions.le("t.addtime", endTime));
		query.add(Restrictions.like("t.status", OrderConstant.STATUS_PAID, MatchMode.START));
		query.addOrder(Order.desc("t.id"));
		return query;
	}
	
	public List<TicketOrder> specialDiscountOrderList(Timestamp starttime, Timestamp endtime, Long sid, String citycode, String status) {
		DetachedCriteria query = DetachedCriteria.forClass(TicketOrder.class, "t");
		DetachedCriteria sub = DetachedCriteria.forClass(Discount.class, "d");
		sub.add(Restrictions.eq("d.relatedid", sid));
		sub.add(Restrictions.eqProperty("t.id", "d.orderid"));
		sub.add(Restrictions.eq("d.tag", PayConstant.DISCOUNT_TAG_PARTNER));
		sub.add(Restrictions.eq("d.cardtype", PayConstant.CARDTYPE_PARTNER));
		sub.setProjection(Projections.property("d.orderid"));
		query.add(Subqueries.exists(sub));
		query.add(Restrictions.ge("t.addtime", starttime));
		query.add(Restrictions.le("t.addtime", endtime));
		if (StringUtils.isNotBlank(citycode)) {
			query.add(Restrictions.eq("t.citycode", citycode));
		}
		if (StringUtils.isBlank(status)) {
			query.add(Restrictions.eq("t.status", OrderConstant.STATUS_PAID_SUCCESS));
		}
		query.addOrder(Order.asc("t.id"));
		List<TicketOrder> ticketOrderList = readOnlyTemplate.findByCriteria(query);
		return ticketOrderList;
	}
}
