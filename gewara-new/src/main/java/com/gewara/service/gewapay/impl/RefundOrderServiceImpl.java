package com.gewara.service.gewapay.impl;

import java.sql.Timestamp;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.gewara.constant.OpenTimeItemConstant;
import com.gewara.constant.PayConstant;
import com.gewara.constant.PointConstant;
import com.gewara.constant.TheatreSeatConstant;
import com.gewara.constant.order.ElecCardConstant;
import com.gewara.constant.ticket.OrderConstant;
import com.gewara.constant.ticket.RefundConstant;
import com.gewara.helper.TspHelper;
import com.gewara.model.api.OrderResult;
import com.gewara.model.drama.DisQuantity;
import com.gewara.model.drama.DramaOrder;
import com.gewara.model.drama.OpenDramaItem;
import com.gewara.model.drama.SellDramaSeat;
import com.gewara.model.drama.TheatreSeatPrice;
import com.gewara.model.drama.TspSaleCount;
import com.gewara.model.goods.ActivityGoods;
import com.gewara.model.goods.BaseGoods;
import com.gewara.model.goods.TicketGoods;
import com.gewara.model.pay.Discount;
import com.gewara.model.pay.ElecCard;
import com.gewara.model.pay.GewaOrder;
import com.gewara.model.pay.GoodsOrder;
import com.gewara.model.pay.OrderRefund;
import com.gewara.model.pay.SportOrder;
import com.gewara.model.sport.OpenTimeItem;
import com.gewara.model.sport.OpenTimeTable;
import com.gewara.model.user.Point;
import com.gewara.service.drama.DramaOrderService;
import com.gewara.service.gewapay.RefundOrderService;
import com.gewara.service.member.PointService;
import com.gewara.service.order.GoodsOrderService;
import com.gewara.service.sport.SportOrderService;
import com.gewara.support.ErrorCode;
import com.gewara.untrans.activity.SynchActivityService;
import com.gewara.untrans.drama.TheatreOperationService;
import com.gewara.untrans.sport.RemoteSportService;
import com.gewara.util.DateUtil;
import com.gewara.util.VmUtils;

@Service("refundOrderService")
public class RefundOrderServiceImpl extends RefundProcessService implements RefundOrderService {

	@Autowired
	private TheatreOperationService theatreOperationService;
	
	@Autowired
	private DramaOrderService dramaOrderService;
	
	@Autowired
	private GoodsOrderService goodsOrderService;
	
	@Autowired
	private SportOrderService sportOrderService;
	
	@Autowired
	private PointService pointService;
	
	@Autowired
	private RemoteSportService remoteSportService;

	@Autowired
	private SynchActivityService synchActivityService;
	
	@Override
	public ErrorCode refund(GewaOrder order, OrderRefund refund, Long userid){
		if(order instanceof DramaOrder){
			return refundDramaOrder((DramaOrder)order, refund, userid);
		}else if(order instanceof SportOrder){
			return refundSportOrder((SportOrder)order, refund, userid);
		}else if(order instanceof GoodsOrder){
			return refundGoodsOrder((GoodsOrder)order, refund, userid);
		}
		return ErrorCode.getFailure("退款订单类型错误！");
	}
	
	private ErrorCode refundDramaOrder(DramaOrder order, OrderRefund refund, Long userid) {
		if(!order.getStatus().startsWith(OrderConstant.STATUS_PAID)/*未支付订单*/ || 
				order.getStatus().equals(OrderConstant.STATUS_PAID_RETURN)) 
			return ErrorCode.getFailure("订单状态不对！");
		String oldStatus = order.getStatus();
		OpenDramaItem odi = baseDao.getObjectByUkey(OpenDramaItem.class, "dpid", order.getDpid(), false);
		ErrorCode remoteCode = theatreOperationService.backDramaRemoteOrder(userid, order, odi);
		if(!remoteCode.isSuccess()) return remoteCode;
		Timestamp cur = new Timestamp(System.currentTimeMillis());
		if(order.isPaidSuccess()){
			if(!odi.isExpired()){
				if(odi.isOpenseat()){
					List<SellDramaSeat> seatList = dramaOrderService.getDramaOrderSeatList(order.getId());
					for(SellDramaSeat seat: seatList){
						seat.setValidtime(cur);
						seat.setStatus(TheatreSeatConstant.STATUS_NEW);
						baseDao.saveObject(seat);
					}
				}else if(odi.isOpenprice() && StringUtils.equals(order.getStatus(), OrderConstant.STATUS_PAID_SUCCESS)){
					Map<TheatreSeatPrice, Integer> priceQuantityMap = new Hashtable<TheatreSeatPrice, Integer>();
					Map<Long, Map<DisQuantity, Integer>> priceDisMap = new Hashtable<Long, Map<DisQuantity,Integer>>();
					Map<OpenDramaItem, Integer> odiMap = new Hashtable<OpenDramaItem, Integer>();
					dramaOrderService.getPriceObjectList(order, odi, odiMap, priceQuantityMap, priceDisMap);
					List result = TspHelper.updateTheatrePriceSubSellCounter(priceQuantityMap, priceDisMap);
					baseDao.saveObjectList(result);
				}
			}
		}
		
		ErrorCode code = dramaOrderService.cancelOrderNote(order);
		if(!code.isSuccess()) return code;
		order.setUpdatetime(cur);
		order.setStatus(OrderConstant.STATUS_PAID_RETURN);
		baseDao.saveObject(order);
		String msg = "";
		List<Discount> discountList = paymentService.getOrderDiscountList(order);
		for(Discount discount: discountList){
			if(PayConstant.DISCOUNT_TAG_ECARD.equals(discount.getTag())){
				ElecCard card = baseDao.getObject(ElecCard.class, discount.getRelatedid());
				if(card.isUsed()){//恢复卡的使用
					if(order.getId().equals(card.getOrderid())){
						card.setStatus(ElecCardConstant.STATUS_SOLD);
						card.setOrderid(null);
						baseDao.saveObject(card);
					} else{
						msg +="[卡号" + card.getCardno() + "被使用]";
					}
				}
			}
			discount.setStatus(OrderConstant.DISCOUNT_STATUS_N);
			baseDao.saveObject(discount);
		}
		//2)恢复积分
		List<Point> pointList = pointService.getPointListByTradeNo(order.getTradeNo());
		int deduct = 0;
		for(Point point:pointList){
			deduct += point.getPoint();
		}
		if(deduct != 0){
			pointService.addPointInfo(order.getMemberid(), -deduct, order.getTradeNo(), PointConstant.TAG_REFUND_TRADE, null, userid);
			dbLogger.warn(order.getTradeNo() + "退回积分....");
		}
		int amount = order.getGewapaid() + order.getAlipaid();
		refund.setRefundtime(cur);
		refund.setGewaRetAmount(amount);
		refund.setMerRetAmount(0);
		if(StringUtils.equals(oldStatus, OrderConstant.STATUS_PAID_FAILURE) || StringUtils.equals(oldStatus, OrderConstant.STATUS_PAID_UNFIX)){
			refund.setStatus(RefundConstant.STATUS_FINISHED);
		}else{
			refund.setStatus(RefundConstant.STATUS_SUCCESS);
		}
		baseDao.saveObject(refund);
		//
		//3)恢复余额
		if(order.sureOutPartner()) {//商户订单
			msg += "[商户订单，无法退款到余额，请注意退款到其相应账户]";
		}else{
			String content = "订单退款";
			refund2Account(order, userid, refund.getGewaRetAmount(), cur, content);
			List<TspSaleCount> tscList = baseDao.getObjectListByField(TspSaleCount.class, "orderid", order.getId());
			baseDao.removeObjectList(tscList);
			dbLogger.warn(order.getTradeNo() + "退款到余额....");
		}
		return ErrorCode.getSuccess(msg);
	}
	
	private ErrorCode refundGoodsOrder(GoodsOrder order, OrderRefund refund, Long userid) {
		if(!order.getStatus().startsWith(OrderConstant.STATUS_PAID)/*未支付订单*/ || 
				order.getStatus().equals(OrderConstant.STATUS_PAID_RETURN)) 
			return ErrorCode.getFailure("订单状态不对！");
		Timestamp cur = new Timestamp(System.currentTimeMillis());
		BaseGoods goods = baseDao.getObject(BaseGoods.class, order.getGoodsid());
		if(goods instanceof TicketGoods){
			ErrorCode code = goodsOrderService.cancelOrderNote(order);
			if(!code.isSuccess()) return code;
		}else if(goods instanceof ActivityGoods){
			ErrorCode code = synchActivityService.activityOrderReturn(order.getTradeNo());
			if(!code.isSuccess()) return code;
			if(!StringUtils.equals(code.getRetval()+"", "true")) return ErrorCode.getFailure(code.getRetval()+"");
		}else{
			OrderResult orderResult = baseDao.getObject(OrderResult.class, order.getTradeNo());
			if(orderResult!=null){ //一体机的取票的信息也删除
				orderResult.setResult(OrderResult.RESULTD);
				orderResult.setUpdatetime(DateUtil.getMillTimestamp());
				baseDao.saveObject(orderResult);
				order.setModifytime(new Timestamp(System.currentTimeMillis()));
			}
		}
		order.setUpdatetime(cur);
		order.setStatus(OrderConstant.STATUS_PAID_RETURN);
		baseDao.saveObject(order);
		String msg = "";
		List<Discount> discountList = paymentService.getOrderDiscountList(order);
		for(Discount discount: discountList){
			if(PayConstant.DISCOUNT_TAG_ECARD.equals(discount.getTag())){
				ElecCard card = baseDao.getObject(ElecCard.class, discount.getRelatedid());
				if(card.isUsed()){//恢复卡的使用
					if(order.getId().equals(card.getOrderid())){
						card.setStatus(ElecCardConstant.STATUS_SOLD);
						card.setOrderid(null);
						baseDao.saveObject(card);
					} else{
						msg +="[卡号" + card.getCardno() + "被使用]";
					}
				}
			}
			discount.setStatus(OrderConstant.DISCOUNT_STATUS_N);
			baseDao.saveObject(discount);
		}
		//2)恢复积分
		List<Point> pointList = pointService.getPointListByTradeNo(order.getTradeNo());
		int deduct = 0;
		for(Point point:pointList){
			deduct += point.getPoint();
		}
		if(deduct != 0){
			pointService.addPointInfo(order.getMemberid(), -deduct, order.getTradeNo(), PointConstant.TAG_REFUND_TRADE, null, userid);
			dbLogger.warn(order.getTradeNo() + "退回积分....");
		}
		int amount = order.getGewapaid() + order.getAlipaid();
		refund.setRefundtime(cur);
		refund.setGewaRetAmount(amount);
		refund.setMerRetAmount(0);
		refund.setStatus(RefundConstant.STATUS_SUCCESS);
		baseDao.saveObject(refund);
		//恢复库存
		goodsOrderService.updateGoodsprice(order);
		//3)恢复余额
		if(order.sureOutPartner()) {//商户订单
			msg += "[商户订单，无法退款到余额，请注意退款到其相应账户]";
		}else{
			String content = "订单退款";
			refund2Account(order, userid, refund.getGewaRetAmount(), cur, content);
			dbLogger.warn(order.getTradeNo() + "退款到余额....");
		}

		dbLogger.warn(order.getTradeNo() + "退款到余额....");
		return ErrorCode.getSuccess(msg);
	}
	
	
	private ErrorCode refundSportOrder(SportOrder order, OrderRefund refund, Long userid) {
		if(!order.getStatus().startsWith(OrderConstant.STATUS_PAID)/*未支付订单*/ || 
				order.getStatus().equals(OrderConstant.STATUS_PAID_RETURN)) 
			return ErrorCode.getFailure("订单状态不对！");
		List<OpenTimeItem> otiList = sportOrderService.getMyOtiList(order.getId());
		String oldStatus = order.getStatus();
		Timestamp cur = new Timestamp(System.currentTimeMillis());
		if(order.isPaidSuccess()){
			OpenTimeTable ott = baseDao.getObject(OpenTimeTable.class, order.getOttid());
			//TODO 远程cus订单改变状态
			ErrorCode<String> codeRemote = remoteSportService.refundOrder(order);
			if(!codeRemote.isSuccess()) dbLogger.warn("api取消远程运动订单有错误！"+order.getTradeNo());
			if(ott.hasField()){
				Map<String, String> descMap = VmUtils.readJsonToMap(order.getDescription2());
				String strtime = descMap.get("时间");
				Timestamp playtime = DateUtil.parseTimestamp(strtime);
				if(playtime.after(cur)){
					for(OpenTimeItem item : otiList){
						item.setValidtime(cur);
						item.setStatus(OpenTimeItemConstant.STATUS_NEW);
						baseDao.saveObject(item);
					}
				}
			}
		}
		OrderResult orderResult = baseDao.getObject(OrderResult.class, order.getTradeNo());
		if(orderResult!=null){ //一体机的取票的信息也删除
			orderResult.setResult(OrderResult.RESULTD);
			orderResult.setUpdatetime(DateUtil.getMillTimestamp());
			baseDao.saveObject(orderResult);
			order.setModifytime(new Timestamp(System.currentTimeMillis()));
		}
		order.setUpdatetime(cur);
		order.setStatus(OrderConstant.STATUS_PAID_RETURN);
		baseDao.saveObject(order);
		String msg = "";
		List<Discount> discountList = paymentService.getOrderDiscountList(order);
		for(Discount discount: discountList){
			if(PayConstant.DISCOUNT_TAG_ECARD.equals(discount.getTag())){
				ElecCard card = baseDao.getObject(ElecCard.class, discount.getRelatedid());
				if(card.isUsed()){//恢复卡的使用
					if(order.getId().equals(card.getOrderid())){
						card.setStatus(ElecCardConstant.STATUS_SOLD);
						card.setOrderid(null);
						baseDao.saveObject(card);
					} else{
						msg +="[卡号" + card.getCardno() + "被使用]";
					}
				}
			}
			discount.setStatus(OrderConstant.DISCOUNT_STATUS_N);
			baseDao.saveObject(discount);
		}
		//2)恢复积分
		List<Point> pointList = pointService.getPointListByTradeNo(order.getTradeNo());
		int deduct = 0;
		for(Point point:pointList){
			deduct += point.getPoint();
		}
		if(deduct != 0){
			pointService.addPointInfo(order.getMemberid(), -deduct, order.getTradeNo(), PointConstant.TAG_REFUND_TRADE, null, userid);
			dbLogger.warn(order.getTradeNo() + "退回积分....");
		}
		//3)恢复余额
		int amount = order.getGewapaid() + order.getAlipaid();
		refund.setRefundtime(cur);
		refund.setGewaRetAmount(amount);
		refund.setMerRetAmount(0);
		if(StringUtils.equals(oldStatus, OrderConstant.STATUS_PAID_FAILURE) || StringUtils.equals(oldStatus, OrderConstant.STATUS_PAID_UNFIX)){
			refund.setStatus(RefundConstant.STATUS_FINISHED);
		}else{
			refund.setStatus(RefundConstant.STATUS_SUCCESS);
		}
		
		baseDao.saveObject(refund);
		if(order.sureOutPartner()) {//商户订单
			msg += "[商户订单，无法退款到余额，请注意退款到其相应账户]";
		}else{
			String content = "订单退款";
			refund2Account(order, userid, refund.getGewaRetAmount(), cur, content);
			dbLogger.warn(order.getTradeNo() + "退款到余额....");
		}

		dbLogger.warn(order.getTradeNo() + "退款到余额....");
		return ErrorCode.getSuccess(msg);
	}
}
