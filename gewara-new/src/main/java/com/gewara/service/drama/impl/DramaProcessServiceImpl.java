package com.gewara.service.drama.impl;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.gewara.constant.ApiConstant;
import com.gewara.constant.OdiConstant;
import com.gewara.constant.PayConstant;
import com.gewara.constant.TheatreSeatConstant;
import com.gewara.constant.ticket.OrderConstant;
import com.gewara.helper.order.GewaOrderHelper;
import com.gewara.model.drama.DramaOrder;
import com.gewara.model.drama.DramaOrder2Seat;
import com.gewara.model.drama.OpenDramaItem;
import com.gewara.model.drama.OpenTheatreSeat;
import com.gewara.model.drama.SellDramaSeat;
import com.gewara.model.pay.BuyItem;
import com.gewara.model.pay.Discount;
import com.gewara.model.pay.ElecCard;
import com.gewara.model.pay.OtherFeeDetail;
import com.gewara.service.OrderException;
import com.gewara.service.drama.DramaOrderService;
import com.gewara.service.drama.DramaProcessService;
import com.gewara.service.gewapay.impl.RefundProcessService;
import com.gewara.support.ErrorCode;
import com.gewara.util.BeanUtil;
import com.gewara.util.DateUtil;
import com.gewara.util.StringUtil;

@Service("dramaProcessService")
public class DramaProcessServiceImpl extends RefundProcessService implements DramaProcessService {

	@Autowired@Qualifier("dramaOrderService")
	private DramaOrderService dramaOrderService;
	
	@Override
	public DramaOrder changeSeat(OpenDramaItem odi, List<OpenTheatreSeat> newseatList, DramaOrder oldOrder, boolean reChange, List<String> remoteLockList) throws OrderException{
		if(!odi.isBooking() || odi.isExpired() || odi.isClosed()){
			throw new OrderException(ApiConstant.CODE_DATA_ERROR, "场次不可订票，不能重下订单");
		}
		if(!odi.isOpenseat()) throw new OrderException(ApiConstant.CODE_DATA_ERROR, "非选座场次，修改座位错误！");
		if(oldOrder.isSeatChanged() && !reChange) throw new OrderException(ApiConstant.CODE_DATA_ERROR, "不能第二次重下订单！");
		//释放新座位
		Timestamp invalid = new Timestamp(System.currentTimeMillis() - 1000);
		Timestamp cur = new Timestamp(System.currentTimeMillis());
		List<SellDramaSeat> seatList = new ArrayList<SellDramaSeat>();
		for(OpenTheatreSeat oseat: newseatList){
			if(!oseat.getAreaid().equals(oldOrder.getAreaid())){
				throw new OrderException(ApiConstant.CODE_DATA_ERROR, "不能跨区更改座位！");
			}
			SellDramaSeat sSeat = baseDao.getObject(SellDramaSeat.class, oseat.getId());
			if(sSeat == null) sSeat = new SellDramaSeat(oseat, invalid);
			else if(!sSeat.isAvailableBy(oldOrder.getId())) throw new OrderException(ApiConstant.CODE_DATA_ERROR, "座位被他人占用！");
			sSeat.setStatus(TheatreSeatConstant.STATUS_NEW);
			sSeat.setValidtime(invalid);
			seatList.add(sSeat);
		}
		ErrorCode code = dramaOrderService.validateSeatLock(newseatList, BeanUtil.beanListToMap(seatList, "id"), remoteLockList);
		if(!code.isSuccess()) throw new OrderException(ApiConstant.CODE_SEAT_OCCUPIED, code.getMsg());
		//1)是否单选情侣座
		code = dramaOrderService.validLoveSeat(newseatList);
		if(!code.isSuccess()) throw new OrderException(ApiConstant.CODE_SEAT_POS_ERROR, code.getMsg());
		
		DramaOrder order = new DramaOrder(oldOrder.getMemberid());
		try {
			PropertyUtils.copyProperties(order, oldOrder);
		} catch (Exception e) {
			dbLogger.error("", e);
		}
		Timestamp validtime = DateUtil.addMinute(cur, OdiConstant.MAX_MINUTS_TICKETS);
		//先将老订单号改掉
		oldOrder.setTradeNo(oldOrder.getTradeNo() + StringUtil.getRandomString(3, true, true, false) + "X");
		oldOrder.setStatus(OrderConstant.STATUS_SYS_CANCEL);
		oldOrder.setGewapaid(0);
		oldOrder.setAlipaid(0);
		oldOrder.setValidtime(invalid);
		List<SellDramaSeat> oldSeatList = dramaOrderService.getDramaOrderSeatList(oldOrder.getId());
		oldSeatList.removeAll(seatList);
		for(SellDramaSeat seat: oldSeatList){
			seat.setValidtime(invalid);
			baseDao.saveObject(seat);
		}
		baseDao.saveObject(oldOrder);
		hibernateTemplate.flush(); //强制先执行更新老订单
		
		order.setId(null);
		order.setValidtime(validtime);
		order.setAddtime(cur);
		order.setUpdatetime(cur);
		order.setModifytime(cur);
		
		order.addChangehis(OrderConstant.CHANGEHIS_KEY_CHANGESEAT, oldOrder.getTradeNo());
		order.setAreaid(oldOrder.getAreaid());		//区域信息
		baseDao.saveObject(order);
		List<DramaOrder2Seat> o2sList = new ArrayList<DramaOrder2Seat>();
		for(SellDramaSeat seat :seatList){
			seat.setValidtime(validtime);
			seat.setRemark("[订" + order.getMembername() +"]" + StringUtils.defaultString(seat.getRemark()));
			seat.setOrderid(order.getId());
			o2sList.add(new DramaOrder2Seat(order.getId(), seat.getId()));
		}
		baseDao.saveObjectList(seatList);
		baseDao.saveObjectList(o2sList);

		List<Discount> discountList = paymentService.getOrderDiscountList(oldOrder);
		if(discountList.size() > 0){
			List<Discount> newList = new ArrayList<Discount>();
			for(Discount discount: discountList){
				Discount nd = new Discount(order.getId(), discount.getTag(), 
						discount.getRelatedid(), discount.getCardtype());
				nd.setAmount(discount.getAmount());
				nd.setDescription(discount.getDescription());
				nd.setGoodsid(discount.getGoodsid());
				nd.setBatchid(discount.getBatchid());
				newList.add(nd);
				if(PayConstant.DISCOUNT_TAG_ECARD.equals(nd.getTag())){
					ElecCard card = baseDao.getObject(ElecCard.class, nd.getRelatedid());
					card.setOrderid(order.getId());
					baseDao.saveObject(card);
				}
				baseDao.saveObject(nd);
				GewaOrderHelper.useDiscount(order, newList, nd);
			}
		}
		baseDao.saveObject(order);
		List<BuyItem> itemList = baseDao.getObjectListByField(BuyItem.class, "orderid", oldOrder.getId());
		if(itemList.size() > 0){
			List<BuyItem> newItemList = new ArrayList<BuyItem>();
			for(BuyItem item: itemList){
				BuyItem newitem = new BuyItem(item);
				newitem.setOrderid(order.getId());
				baseDao.saveObject(newitem);
				newItemList.add(newitem);
			}
			//TODO: 附属品已加入 ？？？
			//GewaOrderHelper.refreshItemfee(order, newItemList);
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
		return order;
	}
	
	@Override
	public Integer getPaidFailureOrderCount(){
		String query = "select count(t.id) from DramaOrder t where t.status like ? and t.paidtime > ? and t.updatetime < ? order by t.paidtime";
		Timestamp cur = new Timestamp(System.currentTimeMillis());
		List result = hibernateTemplate.find(query, OrderConstant.STATUS_PAID_FAILURE + "%", 
				DateUtil.addHour(cur, -8), DateUtil.addMinute(cur, - 10));
		return Integer.valueOf("" + result.get(0));
	}
	
	@Override
	public List<DramaOrder> getPaidUnfixOrderList(int from, int maxnum){
		//paidFailure、8小时之内支付的、updatetime>cur-5min 的订单
		String query = "from DramaOrder t where t.status = ? and t.paidtime > ? and t.updatetime < ? and t.changehis not like ? order by t.paidtime";
		Timestamp cur = new Timestamp(System.currentTimeMillis());
		List<DramaOrder> failureList = queryByRowsRange(query, from, maxnum, OrderConstant.STATUS_PAID_UNFIX, 
				DateUtil.addHour(cur, -8), DateUtil.addMinute(cur, - 10), "%" + OrderConstant.CHANGEHIS_KEY_CHANGESEAT + "%");
		return failureList;
	}
}
