/**
 * 
 */
package com.gewara.service.gewapay.impl;

import java.sql.Timestamp;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.lang.StringUtils;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.gewara.command.SearchRefundCommand;
import com.gewara.constant.ticket.OrderConstant;
import com.gewara.constant.ticket.RefundConstant;
import com.gewara.model.drama.DramaOrder;
import com.gewara.model.drama.OpenDramaItem;
import com.gewara.model.goods.BaseGoods;
import com.gewara.model.pay.AccountRefund;
import com.gewara.model.pay.GewaOrder;
import com.gewara.model.pay.GoodsOrder;
import com.gewara.model.pay.GymOrder;
import com.gewara.model.pay.OrderRefund;
import com.gewara.model.pay.PubSaleOrder;
import com.gewara.model.pay.SportOrder;
import com.gewara.model.pay.TicketOrder;
import com.gewara.model.sport.OpenTimeTable;
import com.gewara.model.sport.SellTimeTable;
import com.gewara.model.ticket.OpenPlayItem;
import com.gewara.service.gewapay.RefundService;
import com.gewara.service.impl.BaseServiceImpl;
import com.gewara.service.ticket.TicketOrderService;
import com.gewara.support.ErrorCode;
import com.gewara.util.DateUtil;
import com.gewara.util.VmUtils;

@Service("refundService")
public class RefundServiceImpl extends BaseServiceImpl implements RefundService{
	
	@Autowired@Qualifier("ticketOrderService")
	private TicketOrderService ticketOrderService;
	
	@Override
	public List<OrderRefund> getOrderRefundList(SearchRefundCommand command, String order, int from, int maxnum){
		DetachedCriteria qry = query(command);
		if(StringUtils.isNotBlank(order)) qry.addOrder(Order.desc(order));
		qry.addOrder(Order.desc("addtime"));
		List<OrderRefund> refundList = hibernateTemplate.findByCriteria(qry, from, maxnum);
		return refundList;
	}
	
	@Override
	public List<OrderRefund> getDifferentSettleOrderRefundList(SearchRefundCommand command, String order, int from, int maxnum){
		DetachedCriteria qry = query(command);
		qry.add(Restrictions.neProperty("oldSettle", "newSettle"));
		if(StringUtils.isNotBlank(order)) qry.addOrder(Order.desc(order));
		qry.addOrder(Order.desc("addtime"));
		List<OrderRefund> refundList = hibernateTemplate.findByCriteria(qry, from, maxnum);
		return refundList;
	}
	
	private DetachedCriteria query(SearchRefundCommand command){
		DetachedCriteria qry = DetachedCriteria.forClass(OrderRefund.class);
		if(StringUtils.isNotBlank(command.getTradeno())) qry.add(Restrictions.eq("tradeno", command.getTradeno()));
		if(StringUtils.isNotBlank(command.getMobile())) qry.add(Restrictions.eq("mobile", command.getMobile()));
		if(command.getMemberid() != null) qry.add(Restrictions.eq("memberid", command.getMemberid()));
		if(StringUtils.isNotBlank(command.getStatus())) qry.add(Restrictions.eq("status", command.getStatus()));
		if(StringUtils.isNotBlank(command.getRetback())) qry.add(Restrictions.eq("retback", command.getRetback()));
		if(StringUtils.isNotBlank(command.getRefundtype())) qry.add(Restrictions.eq("refundtype", command.getRefundtype()));
		if(StringUtils.isNotBlank(command.getOrdertype())) qry.add(Restrictions.eq("ordertype", command.getOrdertype()));
		if(command.getPlaceid() != null) qry.add(Restrictions.eq("placeid", command.getPlaceid()));
		if(command.getAddtimefrom() != null) qry.add(Restrictions.ge("addtime", command.getAddtimefrom()));
		if(command.getAddtimeto() != null) qry.add(Restrictions.lt("addtime", command.getAddtimeto()));
		if(command.getRefundtimefrom() != null) qry.add(Restrictions.ge("refundtime", command.getRefundtimefrom()));
		if(command.getRefundtimeto() != null) qry.add(Restrictions.lt("refundtime", command.getRefundtimeto()));
		return qry;
	}
	

	@Override
	public Integer getOrderRefundCount(SearchRefundCommand command) {
		List result = hibernateTemplate.findByCriteria(query(command).setProjection(Projections.rowCount()));
		if(result.isEmpty()) return 0;
		return Integer.valueOf(""+result.get(0));
	}
	private ErrorCode<OrderRefund> getOtherOrderRefund(GewaOrder order, Long userid, String status) {
		if(!order.getStatus().startsWith(OrderConstant.STATUS_PAID)/*未支付订单*/ || 
				order.getStatus().equals(OrderConstant.STATUS_PAID_RETURN)/*已退款订单*/){
			return ErrorCode.getFailure("订单状态有错误！！");
		}
		OrderRefund refund = new OrderRefund(order, userid);
		if(order instanceof SportOrder) refund.setPlaceid(((SportOrder) order).getSportid());
		else if(order instanceof DramaOrder) refund.setPlaceid(((DramaOrder) order).getTheatreid());
		refund.setStatus(status);
		refund.setGewaRetAmount(order.getAlipaid() + order.getGewapaid());	//全额退款
		refund.setMerRetAmount(0);
		if(order.isPaidSuccess()){
			refund.setOldSettle(order.getTotalcost());
		}else{
			refund.setOldSettle(0);
		}
		refund.setNewSettle(0);
		refund.setRefundtype(RefundConstant.REFUNDTYPE_FULL);
		return ErrorCode.getSuccessReturn(refund);
	}
	@Override
	public ErrorCode<OrderRefund> getDramaOrderRefund(DramaOrder order, Long userid, String status){
		ErrorCode<OrderRefund> code = getOtherOrderRefund(order, userid, status);
		if(!code.isSuccess()) return code;
		OrderRefund refund = code.getRetval();
		OpenDramaItem odi = baseDao.getObjectByUkey(OpenDramaItem.class, "dpid", order.getDpid(), true);
		refund.setExpiretime(odi.getPlaytime());			//过期时间
		return ErrorCode.getSuccessReturn(refund);
	}
	
	@Override
	public ErrorCode<OrderRefund> getSportOrderRefund(SportOrder order, Long userid, String status){
		ErrorCode<OrderRefund> code = getOtherOrderRefund(order, userid, status);
		if(!code.isSuccess()) return code;
		OrderRefund refund = code.getRetval();
		OpenTimeTable ott = baseDao.getObject(OpenTimeTable.class, order.getOttid());
		String strtime = null;
		if(ott.hasField()){
			Map<String, String> descMap = VmUtils.readJsonToMap(order.getDescription2());
			strtime = descMap.get("时间");
		}else{
			SellTimeTable stt = baseDao.getObject(SellTimeTable.class, order.getId());
			strtime = DateUtil.formatTimestamp(ott.getPlayTimeByHour(stt.getStarttime()));
		}
		Timestamp playtime = DateUtil.parseTimestamp(strtime);
		refund.setExpiretime(playtime);
		return ErrorCode.getSuccessReturn(refund);
	}
	
	@Override
	public ErrorCode<OrderRefund> getGymOrderRefund(GymOrder order, Long userid, String status){
		ErrorCode<OrderRefund> code = getOtherOrderRefund(order, userid, status);
		if(!code.isSuccess()) return code;
		OrderRefund refund = code.getRetval();
		refund.setAddtime(order.getPaidtime());
		return ErrorCode.getSuccessReturn(refund);
	}
	
	public ErrorCode<OrderRefund> getGoodsOrderRefund(GoodsOrder order, Long userid, String status){
		ErrorCode<OrderRefund> code = getOtherOrderRefund(order, userid, status);
		if(!code.isSuccess()) return code;
		OrderRefund refund = code.getRetval();
		BaseGoods goods = baseDao.getObject(BaseGoods.class, order.getGoodsid());
		refund.setExpiretime(goods.getTotime());
		return ErrorCode.getSuccessReturn(refund);
	}
	
	@Override
	public ErrorCode<OrderRefund> getPubSaleOrderRefund(PubSaleOrder order, Long userid, String status){
		ErrorCode<OrderRefund> code = getOtherOrderRefund(order, userid, status);
		if(!code.isSuccess()) return code;
		OrderRefund refund = code.getRetval();
		refund.setAddtime(order.getPaidtime());
		return ErrorCode.getSuccessReturn(refund);
	}

	@Override
	public ErrorCode<OrderRefund> getCurFullTicketOrderRefund(TicketOrder order, OpenPlayItem opi, Long userid, String status){
		if(opi.isExpired()) return ErrorCode.getFailure("场次已过期，请申请过期订单退款！");
		return getTicketOrderRefund(order, opi, userid, status, RefundConstant.REFUNDTYPE_FULL);
	}
	@Override
	public ErrorCode<OrderRefund> getExpFullTicketOrderRefund(TicketOrder order, OpenPlayItem opi, Long userid, String status) {
		if(!opi.isExpired()) return ErrorCode.getFailure("场次未过期，请申请未过期订单退款！");
		return getTicketOrderRefund(order, opi, userid, status, RefundConstant.REFUNDTYPE_FULL);
	}
	@Override
	public ErrorCode<OrderRefund> getSupplementTicketOrderRefund(TicketOrder order, OpenPlayItem opi, Long userid, String status) {
		if(!order.getStatus().equals(OrderConstant.STATUS_PAID_SUCCESS)){
			return ErrorCode.getFailure("订单状态有错误: 只有成功的订单才能申请差价退款！" );
		}
		return getTicketOrderRefund(order, opi, userid, status, RefundConstant.REFUNDTYPE_SUPPLEMENT);
	}
	@Override
	public ErrorCode<OrderRefund> getPartTicketOrderRefund(TicketOrder order, OpenPlayItem opi, Long userid, String status) {
		if(!order.getStatus().equals(OrderConstant.STATUS_PAID_SUCCESS)){
			return ErrorCode.getFailure("订单状态有错误: 只有成功的订单才能申请部分退款！" );
		}
		if(!opi.isExpired()){
			return ErrorCode.getFailure("订单时间有错误：只有过期的订单才能申请部分退款！");
		}
		return getTicketOrderRefund(order, opi, userid, status, RefundConstant.REFUNDTYPE_PART);
	}
	private ErrorCode<OrderRefund> getTicketOrderRefund(TicketOrder order, OpenPlayItem opi, Long userid, String status, String refundtype){
		if(!order.getStatus().startsWith(OrderConstant.STATUS_PAID)/*未支付订单*/ || 
				order.getStatus().equals(OrderConstant.STATUS_PAID_RETURN)/*已退款订单*/){
			return ErrorCode.getFailure("订单状态有错误！！");
		}
		OrderRefund refund = new OrderRefund(order, userid);
		refund.setStatus(status);
		refund.setRefundtype(refundtype);
		refund.setOrdertype("ticket");
		refund.setPlaceid(order.getCinemaid());
		Set<String> opmarkList = new TreeSet<String>();
		
		if(order.sureOutPartner()){//商家订单
			opmarkList.add(RefundConstant.OP_RET2PARTNER);
		}
		refund.setExpiretime(opi.getPlaytime());			//过期时间
		
		if(refundtype.equals(RefundConstant.REFUNDTYPE_SUPPLEMENT)){
			refund.setReason(RefundConstant.REASON_PRICE);
			refund.setGewaRetAmount(order.getUnitprice() - opi.getGewaprice());	//补偿退款
			refund.setMerRetAmount(0);
			refund.setOldSettle(order.getCostprice()*order.getQuantity());
			refund.setNewSettle(order.getCostprice()*order.getQuantity());
		}else if(refundtype.equals(RefundConstant.REFUNDTYPE_PART)){
			opmarkList.add(RefundConstant.OP_COMPENSATE);						//部分退款：用户补偿
			opmarkList.add(RefundConstant.OP_ADJUST_SETTLE);					//需要调整与影院结算
			refund.setReason(RefundConstant.REASON_MERCHANT);					//商家退款
			refund.setOldSettle(order.getCostprice()*order.getQuantity());
			refund.setNewSettle(order.getCostprice()*order.getQuantity());
		}else if(refundtype.equals(RefundConstant.REFUNDTYPE_FULL)){
			refund.setGewaRetAmount(order.getAlipaid() + order.getGewapaid());	//全额退款
			refund.setMerRetAmount(0);
			if(order.getStatus().equals(OrderConstant.STATUS_PAID_SUCCESS)){	//成功订单退款
				refund.setOldSettle(order.getCostprice()*order.getQuantity());
				if(opi.isExpired()){
					refund.setReason(RefundConstant.REASON_MERCHANT);			//影院原因
					refund.setNewSettle(order.getCostprice()*order.getQuantity());
					if(!opi.hasGewara()){
						opmarkList.add(RefundConstant.OP_ADJUST_SETTLE);		//需要调整与影院结算
					}
				}else{
					refund.setReason(RefundConstant.REASON_UNKNOWN);			//成功订单，不知道退款原因
					refund.setNewSettle(0);										//未过期，设置结算为0
					if(!opi.hasGewara()){
						opmarkList.add(RefundConstant.OP_CANCEL_TICKET);		//需要退票操作
						opmarkList.add(RefundConstant.OP_ADJUST_SETTLE);		//需要调整与影院结算
					}
				}
			}else{//待处理
				refund.setReason(RefundConstant.REASON_GEWA);
				refund.setOldSettle(0);
				refund.setNewSettle(0);
			}
		}
		refund.setOpmark(StringUtils.join(opmarkList, ","));
		return ErrorCode.getSuccessReturn(refund);
	}
	
	@Override
	public ErrorCode<OrderRefund> getFullDramaOrderRefund(DramaOrder order, OpenDramaItem odi, Long userid, String status){
		//if(odi.isExpired()) return ErrorCode.getFailure("场次已过期，请申请过期订单退款！");
		return getDramaOrderRefund(order, odi, userid, status, RefundConstant.REFUNDTYPE_FULL);
	}
	@Override
	public ErrorCode<OrderRefund> getSupplementDramaOrderRefund(DramaOrder order, OpenDramaItem odi, Long userid, String status) {
		if(!order.getStatus().equals(OrderConstant.STATUS_PAID_SUCCESS)){
			return ErrorCode.getFailure("订单状态有错误: 只有成功的订单才能申请差价退款！" );
		}
		return getDramaOrderRefund(order, odi, userid, status, RefundConstant.REFUNDTYPE_SUPPLEMENT);
	}
	/*@Override
	public ErrorCode<OrderRefund> getPartDramaOrderRefund(DramaOrder order, OpenDramaItem odi, Long userid, String status) {
		if(!order.getStatus().equals(OrderConstant.STATUS_PAID_SUCCESS)){
			return ErrorCode.getFailure("订单状态有错误: 只有成功的订单才能申请部分退款！" );
		}
		if(!odi.isExpired()){
			return ErrorCode.getFailure("订单时间有错误：只有过期的订单才能申请部分退款！");
		}
		return getDramaOrderRefund(order, odi, userid, status, RefundConstant.REFUNDTYPE_PART);
	}*/
	private ErrorCode<OrderRefund> getDramaOrderRefund(DramaOrder order, OpenDramaItem odi, Long userid, String status, String refundtype){
		if(!order.getStatus().startsWith(OrderConstant.STATUS_PAID)/*未支付订单*/ || 
				order.getStatus().equals(OrderConstant.STATUS_PAID_RETURN)/*已退款订单*/){
			return ErrorCode.getFailure("订单状态有错误！！");
		}
		OrderRefund refund = new OrderRefund(order, userid);
		refund.setStatus(status);
		refund.setRefundtype(refundtype);
		refund.setOrdertype(order.getOrdertype());
		refund.setPlaceid(order.getTheatreid());
		Set<String> opmarkList = new TreeSet<String>();
		
		if(order.sureOutPartner()){//商家订单
			opmarkList.add(RefundConstant.OP_RET2PARTNER);
		}
		refund.setExpiretime(odi.getPlaytime());			//过期时间
		
		if(refundtype.equals(RefundConstant.REFUNDTYPE_SUPPLEMENT)){
			refund.setReason(RefundConstant.REASON_PRICE);
			//TODO: 补偿退款 	refund.setGewaRetAmount(order.getUnitprice() - odi.getGewaprice());	//补偿退款
			refund.setGewaRetAmount(order.getUnitprice());
			refund.setMerRetAmount(0);
			refund.setOldSettle(order.getCostprice()*order.getQuantity());
			refund.setNewSettle(order.getCostprice()*order.getQuantity());
		}/*else if(refundtype.equals(RefundConstant.REFUNDTYPE_PART)){
			opmarkList.add(RefundConstant.OP_COMPENSATE);						//部分退款：用户补偿
			opmarkList.add(RefundConstant.OP_ADJUST_SETTLE);					//需要调整与影院结算
			refund.setReason(RefundConstant.REASON_MERCHANT);					//商家退款
			refund.setOldSettle(order.getCostprice()*order.getQuantity());
			refund.setNewSettle(order.getCostprice()*order.getQuantity());
		}*/else if(refundtype.equals(RefundConstant.REFUNDTYPE_FULL)){
			refund.setGewaRetAmount(order.getAlipaid() + order.getGewapaid());	//全额退款
			refund.setMerRetAmount(0);
			if(order.isPaidSuccess()){
				refund.setOldSettle(order.getTotalcost());
			}else{
				refund.setOldSettle(0);
			}
			refund.setNewSettle(0);
		}
		refund.setOpmark(StringUtils.join(opmarkList, ","));
		return ErrorCode.getSuccessReturn(refund);
	}
	
	@Override
	public ErrorCode<OrderRefund> getFullSportOrderRefund(SportOrder order, OpenTimeTable ott, Long userid, String status){
		//if(ott.isExpired()) return ErrorCode.getFailure("场次已过期，请申请过期订单退款！");
		return getSportOrderRefund(order, ott, userid, status, RefundConstant.REFUNDTYPE_FULL);
	}
	@Override
	public ErrorCode<OrderRefund> getSupplementSportOrderRefund(SportOrder order, OpenTimeTable ott, Long userid, String status){
		if(!order.getStatus().equals(OrderConstant.STATUS_PAID_SUCCESS)){
			return ErrorCode.getFailure("订单状态有错误: 只有成功的订单才能申请差价退款！" );
		}
		return getSportOrderRefund(order, ott, userid, status, RefundConstant.REFUNDTYPE_SUPPLEMENT);
	}
	/*@Override
	public ErrorCode<OrderRefund> getPartSportOrderRefund(SportOrder order, OpenTimeTable ott, Long userid, String status){
		if(!order.getStatus().equals(OrderConstant.STATUS_PAID_SUCCESS)){
			return ErrorCode.getFailure("订单状态有错误: 只有成功的订单才能申请部分退款！" );
		}
		if(!ott.isExpired()){
			return ErrorCode.getFailure("订单时间有错误：只有过期的订单才能申请部分退款！");
		}
		return getSportOrderRefund(order, ott, userid, status, RefundConstant.REFUNDTYPE_PART);
	}*/
	
	private ErrorCode<OrderRefund> getSportOrderRefund(SportOrder order, OpenTimeTable ott, Long userid, String status, String refundtype){
		if(!order.getStatus().startsWith(OrderConstant.STATUS_PAID)/*未支付订单*/ || 
				order.getStatus().equals(OrderConstant.STATUS_PAID_RETURN)/*已退款订单*/){
			return ErrorCode.getFailure("订单状态有错误！！");
		}
		OrderRefund refund = new OrderRefund(order, userid);
		refund.setStatus(status);
		refund.setRefundtype(refundtype);
		refund.setOrdertype(order.getOrdertype());
		refund.setPlaceid(order.getSportid());
		Set<String> opmarkList = new TreeSet<String>();
		
		if(order.sureOutPartner()){//商家订单
			opmarkList.add(RefundConstant.OP_RET2PARTNER);
		}
		refund.setExpiretime(DateUtil.parseTimestamp(DateUtil.formatTimestamp(ott.getPlaydate())));			//过期时间
		
		if(refundtype.equals(RefundConstant.REFUNDTYPE_SUPPLEMENT)){
			refund.setReason(RefundConstant.REASON_PRICE);
			int gewaprice = 0;
			if(ott.getGewaprice() != null) gewaprice = ott.getGewaprice();
			refund.setGewaRetAmount(order.getUnitprice() - gewaprice);	//补偿退款
			refund.setMerRetAmount(0);
			refund.setOldSettle(order.getCostprice()*order.getQuantity());
			refund.setNewSettle(order.getCostprice()*order.getQuantity());
		}/*else if(refundtype.equals(RefundConstant.REFUNDTYPE_PART)){
			opmarkList.add(RefundConstant.OP_COMPENSATE);						//部分退款：用户补偿
			opmarkList.add(RefundConstant.OP_ADJUST_SETTLE);					//需要调整与影院结算
			refund.setReason(RefundConstant.REASON_MERCHANT);					//商家退款
			refund.setOldSettle(order.getCostprice()*order.getQuantity());
			refund.setNewSettle(order.getCostprice()*order.getQuantity());
		}*/else if(refundtype.equals(RefundConstant.REFUNDTYPE_FULL)){
			refund.setGewaRetAmount(order.getAlipaid() + order.getGewapaid());	//全额退款
			refund.setMerRetAmount(0);
			if(order.isPaidSuccess()){
				refund.setOldSettle(order.getTotalcost());
			}else{
				refund.setOldSettle(0);
			}
			refund.setNewSettle(0);
		}
		refund.setOpmark(StringUtils.join(opmarkList, ","));
		return ErrorCode.getSuccessReturn(refund);
	}


	@Override
	public ErrorCode<AccountRefund> submit2Financial(OrderRefund refund) {
		if(refund.getStatus().equals(RefundConstant.STATUS_SUCCESS) 
				|| refund.getStatus().equals(RefundConstant.STATUS_FINISHED)){
			if(!RefundConstant.RETBACK_Y.equals(refund.getRetback())){
				return ErrorCode.getFailure("返还状态不对！"); 
			}
			refund.setRetback(RefundConstant.RETBACK_SUBMIT);
			AccountRefund accountRefund = new AccountRefund(refund);
			GewaOrder order = baseDao.getObjectByUkey(GewaOrder.class, "tradeNo", accountRefund.getTradeno(), true);
			accountRefund.setPaymethod(order.getPaymethod());
			baseDao.saveObject(refund);
			baseDao.saveObject(accountRefund);
			ticketOrderService.cancelOrderExtra(order);
			return ErrorCode.getSuccessReturn(accountRefund);
		}else{
			return ErrorCode.getFailure("未成功退款，不能账户退款！");
		}
	}

	@Override
	public List<OrderRefund> getSettleRefundList(String ordertype, Timestamp timefrom, Timestamp timeto, Long placeid) {
		String query = "from OrderRefund where refundtime>=? and refundtime<=? and ordertype=? and placeid=? and (status=? or status=?) and orderstatus = ? ";
		List<OrderRefund> refundList = hibernateTemplate.find(query, timefrom, timeto, ordertype, placeid, RefundConstant.STATUS_SUCCESS, RefundConstant.STATUS_FINISHED, OrderConstant.STATUS_PAID_SUCCESS);
		return refundList;
	}

	@Override
	public List<Map> getRefundReason(SearchRefundCommand command) {
		List result = hibernateTemplate.findByCriteria(query(command).setProjection(Projections.projectionList()
				.add(Projections.groupProperty("reason"),"reason")
				.add(Projections.count("reason"),"resasonCount"))
				.setResultTransformer(DetachedCriteria.ALIAS_TO_ENTITY_MAP));
		return result;
	}
}
