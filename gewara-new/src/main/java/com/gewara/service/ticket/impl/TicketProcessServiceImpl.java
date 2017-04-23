package com.gewara.service.ticket.impl;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.gewara.constant.ApiConstant;
import com.gewara.constant.PayConstant;
import com.gewara.constant.PointConstant;
import com.gewara.constant.order.ElecCardConstant;
import com.gewara.constant.ticket.OpiConstant;
import com.gewara.constant.ticket.OrderConstant;
import com.gewara.constant.ticket.RefundConstant;
import com.gewara.constant.ticket.SeatConstant;
import com.gewara.helper.order.GewaOrderHelper;
import com.gewara.helper.ticket.TicketUtil;
import com.gewara.model.api.OrderResult;
import com.gewara.model.partner.CallbackOrder;
import com.gewara.model.pay.BuyItem;
import com.gewara.model.pay.Discount;
import com.gewara.model.pay.ElecCard;
import com.gewara.model.pay.GoodsOrder;
import com.gewara.model.pay.OrderRefund;
import com.gewara.model.pay.OtherFeeDetail;
import com.gewara.model.pay.TicketOrder;
import com.gewara.model.ticket.OpenPlayItem;
import com.gewara.model.ticket.OpenSeat;
import com.gewara.model.ticket.Order2SellSeat;
import com.gewara.model.ticket.SellSeat;
import com.gewara.model.user.Point;
import com.gewara.service.OrderException;
import com.gewara.service.gewapay.impl.RefundProcessService;
import com.gewara.service.member.PointService;
import com.gewara.service.partner.PartnerSynchService;
import com.gewara.service.ticket.OpenPlayService;
import com.gewara.service.ticket.TicketOrderService;
import com.gewara.service.ticket.TicketProcessService;
import com.gewara.service.ticket.WandaService;
import com.gewara.support.ErrorCode;
import com.gewara.untrans.ticket.TicketOperationService;
import com.gewara.util.BeanUtil;
import com.gewara.util.DateUtil;
import com.gewara.util.JsonUtils;
import com.gewara.util.StringUtil;
import com.gewara.xmlbind.ticket.TicketRemoteOrder;
@Service("ticketProcessService")
public class TicketProcessServiceImpl extends RefundProcessService implements TicketProcessService {
	@Autowired@Qualifier("ticketOperationService")
	protected TicketOperationService ticketOperationService;
	public void setTicketOperationService(TicketOperationService ticketOperationService) {
		this.ticketOperationService = ticketOperationService;
	}
	@Autowired@Qualifier("ticketOrderService")
	protected TicketOrderService ticketOrderService;
	public void setTicketOrderService(TicketOrderService ticketOrderService) {
		this.ticketOrderService = ticketOrderService;
	}
	@Autowired@Qualifier("partnerSynchService")
	private PartnerSynchService partnerSynchService;
	public void setPartnerSynchService(PartnerSynchService partnerSynchService) {
		this.partnerSynchService = partnerSynchService;
	}
	@Autowired@Qualifier("pointService")
	private PointService pointService;
	public void setPointService(PointService pointService) {
		this.pointService = pointService;
	}
	@Autowired@Qualifier("openPlayService")
	protected OpenPlayService openPlayService;
	public void setOpenPlayService(OpenPlayService openPlayService) {
		this.openPlayService = openPlayService;
	}
	@Autowired@Qualifier("wandaService")
	private WandaService wandaService;
	@Override
	public ErrorCode<CallbackOrder> refundFullCurOrder(OrderRefund refund, OpenPlayItem opi, TicketOrder order, Long userid) {
		if(opi.isExpired()) return ErrorCode.getFailure("过期的场次不能在此退款！");
		if(!refund.getRefundtype().equals(RefundConstant.REFUNDTYPE_FULL)) return ErrorCode.getFailure("退款类型有错误！");
		if(order.isPaidSuccess()){
			if(!opi.hasGewara()) return ErrorCode.getFailure("订单状态不对，" + OpiConstant.getParnterText(opi.getOpentype()) + "订单先处理退票！");
		}else if(!order.isPaidFailure() && !order.isPaidUnfix() && !order.isNotAllPaid()){
			return ErrorCode.getFailure("订单状态不对！");
		}
		if(!opi.hasGewara()){
			ErrorCode<TicketRemoteOrder> result = ticketOperationService.getRemoteOrder(order, false);
			if(!result.isSuccess()) return ErrorCode.getFailure(OpiConstant.getParnterText(opi.getOpentype()) + "当前有错误，不能退款！"); 
			if(result.getRetval().hasFixed()) return ErrorCode.getFailure("已经确认的订单，不能退款！");
		}
		refund.setNewSettle(0);//当前成功订单退款，结算自动归“0”
		return refundInternal(order, refund, true, userid, false);
	}
	@Override
	public ErrorCode refundPartExpiredOrder(OrderRefund refund, TicketOrder order, Long userid) {
		OpenPlayItem opi = baseDao.getObjectByUkey(OpenPlayItem.class, "mpid", order.getMpid(), false);
		if(!opi.isExpired()) return ErrorCode.getFailure("非过期的场次不能在此退款！");
		if(!refund.getRefundtype().equals(RefundConstant.REFUNDTYPE_PART)) return ErrorCode.getFailure("退款类型有错误！");
		return refundInternal(order, refund, false, userid, true);
	}
	@Override
	public ErrorCode<CallbackOrder> refundFullExpiredOrder(OrderRefund refund, OpenPlayItem opi, TicketOrder order, Long userid) {
		if(!opi.isExpired()) return ErrorCode.getFailure("非过期的场次不能在此退款！");
		if(!refund.getRefundtype().equals(RefundConstant.REFUNDTYPE_FULL)) return ErrorCode.getFailure("退款类型有错误！");
		if(!order.getStatus().startsWith(OrderConstant.STATUS_PAID) || order.getStatus().equals(OrderConstant.STATUS_PAID_RETURN)){
			return ErrorCode.getFailure("订单状态不对！");
		}
		if(StringUtils.contains(refund.getOpmark(), RefundConstant.OP_CANCEL_TICKET)){
			List<String> opmarks = new ArrayList<String>(Arrays.asList(StringUtils.split(refund.getOpmark(), ",")));
			opmarks.remove(RefundConstant.OP_CANCEL_TICKET);	//防止延误导致的状态
			refund.setOpmark(StringUtils.join(opmarks, ","));
		}
		return refundInternal(order, refund, true, userid, true);
	}
	@Override
	public ErrorCode<CallbackOrder> hfhRefund(OrderRefund refund, OpenPlayItem opi, TicketOrder order, Long userid) {
		if(!order.isPaidSuccess() || opi.hasGewara()){
			return ErrorCode.getFailure("订单状态不对！");
		}
		ErrorCode<TicketRemoteOrder> result = ticketOperationService.checkRemoteOrder(order);
		if(result.isSuccess() && result.getRetval().hasFixed()) return ErrorCode.getFailure("已经确认的订单，不能退款！");
		refund.setNewSettle(0);
		List<String> opmarks = new ArrayList<String>(Arrays.asList(StringUtils.split(refund.getOpmark(), ",")));
		
		opmarks.remove(RefundConstant.OP_ADJUST_SETTLE);
		opmarks.remove(RefundConstant.OP_CANCEL_TICKET);
		
		opmarks.add(RefundConstant.OP_RESULT_CANCEL_SUCCESS);
		refund.setOpmark(StringUtils.join(opmarks, ","));
		
		return refundInternal(order, refund, true, userid, false);
	}
	@Override
	public ErrorCode<CallbackOrder> forceHfhRefund(OrderRefund refund, OpenPlayItem opi, TicketOrder order, Long userid) {
		if(!order.isPaidSuccess() || opi.hasGewara()){
			return ErrorCode.getFailure("订单状态不对！");
		}
		if(!opi.isExpired()) return ErrorCode.getFailure("非过期的场次不能在此退款！");

		ErrorCode<TicketRemoteOrder> result = ticketOperationService.checkRemoteOrder(order);
		List<String> opmarks = new ArrayList<String>(Arrays.asList(StringUtils.split(refund.getOpmark(), ",")));
		if(result.isSuccess() && result.getRetval().hasFixed()) {//转为火凤凰正常退款
			refund.setOldSettle(order.getCostprice() * order.getQuantity());
			refund.setNewSettle(0);
			opmarks.remove(RefundConstant.OP_ADJUST_SETTLE);
			opmarks.remove(RefundConstant.OP_CANCEL_TICKET);
			opmarks.add(RefundConstant.OP_RESULT_CANCEL_SUCCESS);
		}else{
			refund.setOldSettle(order.getCostprice() * order.getQuantity());
			refund.setNewSettle(order.getCostprice() * order.getQuantity());
			opmarks.remove(RefundConstant.OP_CANCEL_TICKET);
			opmarks.add(RefundConstant.OP_RESULT_CANCEL_FAILURE);
		}
		return refundInternal(order, refund, true, userid, false);
	}
	private ErrorCode<CallbackOrder> refundInternal(TicketOrder order, OrderRefund refund, boolean resetDiscount, Long userid, boolean expired) {
		Timestamp cur = new Timestamp(System.currentTimeMillis());
		if(order.isPaidSuccess()){
			if(!expired){//非过期场次取消座位
				List<SellSeat> seatList = ticketOrderService.getOrderSeatList(order.getId());
				for(SellSeat seat: seatList){
					seat.setValidtime(cur);
					seat.setStatus(SeatConstant.STATUS_NEW);
					baseDao.saveObject(seat);
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
		//如绑定套餐，套餐订单也要退
		Map<String,String> otherinfoMap = JsonUtils.readJsonToMap(order.getOtherinfo());
		String goodsTradeNo = otherinfoMap.get(PayConstant.KEY_BIND_TRADENO);
		if(StringUtils.isNotBlank(goodsTradeNo)){
			GoodsOrder goodsOrder = baseDao.getObject(GoodsOrder.class, goodsTradeNo);
			if(goodsOrder != null){
				goodsOrder.setUpdatetime(cur);
				goodsOrder.setStatus(OrderConstant.STATUS_PAID_RETURN);
				baseDao.saveObject(order);
			}
		}
		baseDao.saveObject(order);
		//1)卡退出状态
		String msg = "";
		if(resetDiscount){//只有部分退款不走此操作
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
		}
		//3.2)恢复余额
		if(order.sureOutPartner()) {//商户订单
			msg += "[商户订单，无法退款到余额，请注意退款到其相应账户]";
		}else{
			String content = "退款";
			if(refund.getMerRetAmount() > 0) {
				content += ", 影院代付" + refund.getMerRetAmount();
			}
			refund2Account(order, userid, refund.getGewaRetAmount(), cur, content);
		}
		refund.setRefundtime(cur);
		if(StringUtils.contains(refund.getOpmark(), RefundConstant.OP_ADJUST_SETTLE) || 
				StringUtils.contains(refund.getOpmark(), RefundConstant.OP_CANCEL_TICKET) || 
				StringUtils.contains(refund.getOpmark(), RefundConstant.OP_RESULT_CANCEL_FAILURE) ||
				StringUtils.contains(refund.getOpmark(), RefundConstant.OP_COMPENSATE)){
			refund.setStatus(RefundConstant.STATUS_SUCCESS);
		}else{
			refund.setStatus(RefundConstant.STATUS_FINISHED);
		}
		baseDao.saveObject(refund);
		CallbackOrder callbackOrder = partnerSynchService.addCallbackOrder(order, PayConstant.PUSH_FLAG_REFUND, true);
		return ErrorCode.getFullErrorCode(ApiConstant.CODE_SUCCESS, msg, callbackOrder);
	}

	@Override
	public void updateBuytimes(Long memberid, String mobile, String orderType, Timestamp addtime) {
		String update = "UPDATE WEBDATA.MOBILE_BUYTIMES SET BUYTIMES=BUYTIMES+1, LASTTIME= ? WHERE MOBILE=? AND ORDER_TYPE=?";
		int success = jdbcTemplate.update(update, addtime, mobile, orderType);
		if(success==0){
			String insert = "INSERT INTO WEBDATA.MOBILE_BUYTIMES(MOBILE,BUYTIMES,LASTTIME,ORDER_TYPE) VALUES(?, ?, ?, ?)";
			jdbcTemplate.update(insert, mobile, 1, addtime, orderType);
		}
		String updateMember = "UPDATE WEBDATA.MEMBER_BUYTIMES SET BUYTIMES=BUYTIMES+1, LASTTIME= ? WHERE MEMBERID=? AND ORDER_TYPE=?";
		int successMember = jdbcTemplate.update(updateMember, addtime, memberid, orderType);
		if(successMember == 0){
			String insert = "INSERT INTO WEBDATA.MEMBER_BUYTIMES(MEMBERID,BUYTIMES,LASTTIME,ORDER_TYPE) VALUES(?, ?, ?, ?)";
			jdbcTemplate.update(insert, memberid, 1, addtime, orderType);
		}
	}
	@Override
	public ErrorCode synchCostprice(TicketOrder order, Long userid){
		if(!order.isPaidFailure() && !order.isPaidUnfix()) return ErrorCode.getFailure("订单状态不对！");
		OpenPlayItem opi = baseDao.getObjectByUkey(OpenPlayItem.class, "mpid", order.getMpid(), true);
		order.setCostprice(opi.getCostprice());
		baseDao.saveObject(order);
		return ErrorCode.SUCCESS;
	}
	/**
	 * 更改订单的座位
	 * 
	 * 2、新座位的价格必须和老座位的价格保持一致
	 * 3、同步更新优惠券
	 * 4、同步更新老座位的状态（如果被此订单占用的话）
	 * @param orderid
	 * @param newseat
	 * @return
	 * @throws OrderException 
	 */
	@Override
	public List<OpenSeat> getNewSeatList(TicketOrder order, OpenPlayItem opi, List<SellSeat> seatList, String newseat) throws OrderException{
		//1、只有状态为paid_unfix||paid_的订单才能更改座位
		if(!order.getStatus().equals(OrderConstant.STATUS_PAID_UNFIX)) throw new OrderException(ApiConstant.CODE_DATA_ERROR, "“订单待处理”不能换座，只有“座位待处理”才可以换座！");
		String str = "0123456789,:ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
		if(StringUtils.isBlank(newseat) || !StringUtils.containsOnly(newseat, str)){
			throw new OrderException(ApiConstant.CODE_DATA_ERROR, "座位格式不正确：" + newseat);
		}
		List<Long> oldseatidList = BeanUtil.getBeanPropertyList(seatList, Long.class, "id", true);
		List<String[]> seatLocList = TicketUtil.parseSeat(newseat);
		List<OpenSeat> newseatList = new ArrayList<OpenSeat>();
		Timestamp cur = new Timestamp(System.currentTimeMillis());
		for(String[] loc: seatLocList){
			OpenSeat oseat = ticketOrderService.getOpenSeatByLoc(opi.getMpid(), loc[0], loc[1]);
			if(oseat != null) {
				SellSeat sellSeat = baseDao.getObject(SellSeat.class, oseat.getId());
				if((sellSeat==null || sellSeat.isAvailableBy(order.getId(), cur)) && !oseat.isLocked()){
					newseatList.add(oseat);
					
				}else{
					throw new OrderException(ApiConstant.CODE_DATA_ERROR, "座位不可用!");
				}
			}else{
				throw new OrderException(ApiConstant.CODE_DATA_ERROR, "座位不存在：" + loc);
			}
		}
		
		if(oldseatidList.size() != newseatList.size()) throw new OrderException(ApiConstant.CODE_DATA_ERROR, "座位数量不一致！");
		//FIXME:价格与原订单不同的问题：采用系统优惠？？
		return newseatList;
	}
	@Override
	public List<OpenSeat> getOriginalSeat(TicketOrder order, List<SellSeat> seatList) throws OrderException{
		if(!order.isPaidUnfix()) throw new OrderException(ApiConstant.CODE_DATA_ERROR, "只能修改座位待处理订单！");
		ticketOperationService.unlockRemoteSeat(order, seatList);
		List<OpenSeat> oseatList = baseDao.getObjectList(OpenSeat.class, BeanUtil.getBeanPropertyList(seatList, Long.class, "id", true));
		return oseatList;
	}
	@Override
	public TicketOrder changeSeat(OpenPlayItem opi, List<OpenSeat> newseatList, TicketOrder oldOrder, boolean reChange) throws OrderException {
		if(!opi.hasGewara()){
			ErrorCode<TicketRemoteOrder> checkResult = ticketOperationService.checkRemoteOrder(oldOrder);
			if(!checkResult.isSuccess()) throw new OrderException(checkResult.getErrcode(), checkResult.getMsg());
			if(checkResult.getRetval().hasFixed()) throw new OrderException(ApiConstant.CODE_DATA_ERROR, "已经确认的订单，不能更改座位！");
		}
		if(!opi.isBooking() || opi.isExpired() || opi.isClosed()){
			throw new OrderException(ApiConstant.CODE_DATA_ERROR, "场次不可订票，不能重下订单");
		}
		if(!oldOrder.getStatus().equals(OrderConstant.STATUS_PAID_UNFIX)) throw new OrderException(ApiConstant.CODE_DATA_ERROR, "订单待处理不能换座！");
		if(!oldOrder.needChangeSeat() && !reChange) throw new OrderException(ApiConstant.CODE_DATA_ERROR, "当前不能重下订单！");
		return changeSeat(opi, newseatList, oldOrder);
	}
	@Override
	public TicketOrder wdChangeSeat(OpenPlayItem opi, String oldTradeNo, TicketRemoteOrder wdOrder, String wdOrderId, String randomNum) throws OrderException{
		TicketOrder oldOrder = baseDao.getObjectByUkey(TicketOrder.class, "tradeNo", oldTradeNo);
		ErrorCode allow = wandaService.checkAllowChangeSeat(oldOrder);
		if(!allow.isSuccess()){
			throw new OrderException(allow.getErrcode(), allow.getMsg());
		}
		List<SellSeat> seatList = wandaService.checkAndCreateSeat(opi, wdOrder.getSeatno());
		Timestamp invalid = new Timestamp(System.currentTimeMillis() - 1000);
		Timestamp cur = new Timestamp(System.currentTimeMillis());
		TicketOrder order = changeSeat(opi, seatList, oldOrder, cur, invalid);
		order.setOtherinfo(JsonUtils.addJsonKeyValue(order.getOtherinfo(), "wdOrderId", wdOrderId));
		baseDao.saveObject(order);
		return order;
	}

	protected TicketOrder changeSeat(OpenPlayItem opi, List<OpenSeat> newseatList, TicketOrder oldOrder) throws OrderException {
		//释放新座位
		Timestamp invalid = new Timestamp(System.currentTimeMillis() - 1000);
		Timestamp cur = new Timestamp(System.currentTimeMillis());
		List<SellSeat> seatList = new ArrayList<SellSeat>();
		int price = opi.getGewaprice();
		for(OpenSeat oseat: newseatList){
			int price2 = price;
			SellSeat sSeat = baseDao.getObject(SellSeat.class, oseat.getId());
			if(sSeat == null) sSeat = new SellSeat(oseat, price2, invalid);
			else if(!sSeat.isAvailableBy(oldOrder.getId(), cur)) throw new OrderException(ApiConstant.CODE_DATA_ERROR, "座位被他人占用！");
			sSeat.setStatus(SeatConstant.STATUS_NEW);
			sSeat.setValidtime(invalid);
			seatList.add(sSeat);
		}

		ErrorCode<List<String>> remoteLockList = ticketOperationService.updateRemoteLockSeat(opi, OpiConstant.SECONDS_UPDATE_SEAT, true);
		if(!remoteLockList.isSuccess()) throw new OrderException(remoteLockList.getErrcode(), remoteLockList.getMsg());
		ErrorCode code = ticketOrderService.validateSeatLock(newseatList, BeanUtil.beanListToMap(seatList, "id"), remoteLockList.getRetval());
		if(!code.isSuccess()) throw new OrderException(ApiConstant.CODE_SEAT_OCCUPIED, code.getMsg());
		//1)是否单选情侣座
		code = ticketOrderService.validLoveSeat(newseatList);
		if(!code.isSuccess()) throw new OrderException(ApiConstant.CODE_SEAT_POS_ERROR, code.getMsg());
		return changeSeat(opi, seatList, oldOrder, cur, invalid);
	}
	
	private TicketOrder changeSeat(OpenPlayItem opi, List<SellSeat> seatList, TicketOrder oldOrder, Timestamp cur, Timestamp invalid) {
		TicketOrder order = new TicketOrder(oldOrder.getMemberid());
		try {
			PropertyUtils.copyProperties(order, oldOrder);
		} catch (Exception e) {
			dbLogger.error("", e);
		}
		Timestamp validtime = DateUtil.addMinute(cur, opi.gainLockMinute());
		//先将老订单号改掉
		oldOrder.setTradeNo(oldOrder.getTradeNo() + StringUtil.getRandomString(3, true, true, false) + "X");
		oldOrder.setStatus(OrderConstant.STATUS_SYS_CANCEL);
		oldOrder.setGewapaid(0);
		oldOrder.setAlipaid(0);
		oldOrder.setValidtime(invalid);
		List<SellSeat> oldSeatList = ticketOrderService.getOrderSeatList(oldOrder.getId());
		oldSeatList.removeAll(seatList);
		for(SellSeat seat: oldSeatList){
			seat.setValidtime(invalid);
			baseDao.saveObject(seat);
		}
		if(!order.getMpid().equals(opi.getMpid())){
			oldOrder.addChangehis(OrderConstant.CHANGEHIS_KEY_MPITO, ""+opi.getMpid());
		}
		baseDao.saveObject(oldOrder);
		//强制先执行更新老订单
		hibernateTemplate.flush();
		
		order.setId(null);
		order.setValidtime(validtime);
		order.setAddtime(cur);
		order.setUpdatetime(cur);
		order.setModifytime(cur);
		order.addChangehis(OrderConstant.CHANGEHIS_KEY_CHANGESEAT, oldOrder.getTradeNo());
		if(!order.getMpid().equals(opi.getMpid())){
			//场次变换
			order.addChangehis(OrderConstant.CHANGEHIS_KEY_MPIFROM, ""+order.getMpid());
			order.setMpid(opi.getMpid());
			order.setCinemaid(opi.getCinemaid());
			order.setMovieid(opi.getMovieid());
			order.setPlaytime(opi.getPlaytime());
		}
		baseDao.saveObject(order);//获取orderId
		List<Order2SellSeat> o2sList = new ArrayList<Order2SellSeat>();
		for(SellSeat seat :seatList){
			seat.setValidtime(validtime);
			seat.setRemark(StringUtils.substring("[订" + order.getMembername() +"]" + StringUtils.defaultString(seat.getRemark()), 0, 500));
			seat.setOrderid(order.getId());
			o2sList.add(new Order2SellSeat(order.getId(), seat.getId()));
		}
		TicketUtil.setOrderDescription(order, seatList, opi);
		baseDao.saveObjectList(seatList);
		baseDao.saveObjectList(o2sList);

		List<Discount> discountList = paymentService.getOrderDiscountList(order);
		if(discountList.size() > 0){
			List<Discount> newList = new ArrayList<Discount>();
			for(Discount discount: discountList){
				Discount nd = new Discount(order.getId(), discount.getTag(), 
						discount.getRelatedid(), discount.getCardtype());
				nd.setAmount(discount.getAmount());
				nd.setDescription(discount.getDescription());
				nd.setGoodsid(discount.getGoodsid());
				nd.setBatchid(discount.getBatchid());
				nd.setStatus(discount.getStatus());
				newList.add(nd);
				if(PayConstant.DISCOUNT_TAG_ECARD.equals(nd.getTag())){
					ElecCard card = baseDao.getObject(ElecCard.class, nd.getRelatedid());
					card.setOrderid(order.getId());
					baseDao.saveObject(card);
				}
				//如使用成功，要改变状态
				if(StringUtils.equals(discount.getStatus(), OrderConstant.DISCOUNT_STATUS_Y)){
					discount.setStatus(OrderConstant.DISCOUNT_STATUS_N);
					baseDao.saveObject(discount);
				}
				baseDao.saveObject(nd);
				GewaOrderHelper.useDiscount(order, newList, nd);
			}
		}
		List<BuyItem> itemList = baseDao.getObjectListByField(BuyItem.class, "orderid", oldOrder.getId());
		if(itemList.size() > 0){
			List<BuyItem> newItemList = new ArrayList<BuyItem>();
			for(BuyItem item: itemList){
				BuyItem newitem = new BuyItem(item);
				newitem.setOrderid(order.getId());
				baseDao.saveObject(newitem);
				newItemList.add(newitem);
			}
			GewaOrderHelper.refreshItemfee(order, newItemList);
		}
		List<OtherFeeDetail> otherFeeList = baseDao.getObjectListByField(OtherFeeDetail.class, "orderid", order.getId());
		if(!otherFeeList.isEmpty()){
			List<OtherFeeDetail> newOtherFeeList = new ArrayList<OtherFeeDetail>();
			for(OtherFeeDetail otherFee: otherFeeList){
				OtherFeeDetail newOtherFee = new OtherFeeDetail(otherFee);
				newOtherFee.setOrderid(order.getId());
				baseDao.saveObject(newOtherFee);
				newOtherFeeList.add(newOtherFee);
			}
			GewaOrderHelper.refreshOtherfee(order, newOtherFeeList);
		}
		baseDao.saveObject(order);
		ticketOperationService.createRemoteOrder(opi, order, seatList);
		return order;
	}
}
