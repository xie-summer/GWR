package com.gewara.web.action.admin.ticket;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.support.PropertyComparator;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import com.gewara.command.SearchOrderCommand;
import com.gewara.constant.GoodsConstant;
import com.gewara.constant.Status;
import com.gewara.constant.ticket.OrderConstant;
import com.gewara.model.acl.User;
import com.gewara.model.api.OrderResult;
import com.gewara.model.common.BaseInfo;
import com.gewara.model.goods.BaseGoods;
import com.gewara.model.goods.Goods;
import com.gewara.model.goods.GoodsGift;
import com.gewara.model.movie.Cinema;
import com.gewara.model.pay.BuyItem;
import com.gewara.model.pay.GewaOrder;
import com.gewara.model.pay.GoodsOrder;
import com.gewara.model.pay.SMSRecord;
import com.gewara.model.pay.TicketOrder;
import com.gewara.model.sport.Sport;
import com.gewara.model.user.Member;
import com.gewara.pay.PayUtil;
import com.gewara.service.MessageService;
import com.gewara.service.order.GoodsOrderService;
import com.gewara.support.ErrorCode;
import com.gewara.support.ServiceHelper;
import com.gewara.untrans.UntransService;
import com.gewara.untrans.ticket.OrderProcessService;
import com.gewara.util.DateUtil;
import com.gewara.web.action.admin.BaseAdminController;

@Controller
public class GoodsOrderAdminController extends BaseAdminController{
	@Autowired@Qualifier("untransService")
	private UntransService untransService;
	public void setUntransService(UntransService untransService) {
		this.untransService = untransService;
	}
	@Autowired@Qualifier("goodsOrderService")
	private GoodsOrderService goodsOrderService;
	public void setGoodsOrderService(GoodsOrderService goodsOrderService) {
		this.goodsOrderService = goodsOrderService;
	}
	@Autowired@Qualifier("orderProcessService")
	private OrderProcessService orderProcessService;
	public void setProcessOrderService(OrderProcessService orderProcessService) {
		this.orderProcessService = orderProcessService;
	}
	@Autowired@Qualifier("messageService")
	private MessageService messageService;
	public void setMessageService(MessageService messageService) {
		this.messageService = messageService;
	}
	@RequestMapping("/admin/goods/order/goodsOrderList.xhtml")
	public String mealOrderList(Long gid, String status, String tradeNo, String mobile, Timestamp timeFrom, Timestamp timeTo, ModelMap model){
		if(timeFrom==null && timeTo==null) {
			timeTo = new Timestamp(System.currentTimeMillis());
			timeFrom = DateUtil.addDay(timeTo, -10);
		}
		BaseGoods goods = daoService.getObject(BaseGoods.class, gid);
		List<GoodsOrder> goodsOrderList = goodsOrderService.getGoodsOrderList(gid, status, tradeNo, mobile, timeFrom, timeTo);
		Map<Long, Member> memberMap = new HashMap<Long, Member>();
		Map<String, Integer> buynumMap = new HashMap<String, Integer>();
		for(GoodsOrder order : goodsOrderList){
			if(!memberMap.containsKey(order.getMemberid())){
				memberMap.put(order.getMemberid(), daoService.getObject(Member.class, order.getMemberid()));
			}
			if(!buynumMap.containsKey(order.getMobile()))
				buynumMap.put(order.getMobile(), goodsOrderService.getGewaorderCountByMobile(null, order.getMobile(), "goods"));
		}
		model.put("goods", goods);
		model.put("buynumMap", buynumMap);
		model.put("memberMap", memberMap);
		model.put("orderList", goodsOrderList);
		model.put("timeFrom", timeFrom);
		model.put("timeTo", timeTo);
		addCacheMember(model, ServiceHelper.getMemberIdListFromBeanList(goodsOrderList));
		return "admin/goods/orderList_bmh.vm";
	}
	@RequestMapping("/admin/goods/order/unaddOrderList.xhtml")
	public String unAddOrderList(ModelMap model){
		Timestamp times = DateUtil.getBeginTimestamp(new Date());
		times = DateUtil.addDay(times, -7);
		String qry = "from BuyItem b where b.addtime>? and b.tradeno is null " +
				"and exists(select t.id from TicketOrder t where t.status=? and t.id=b.orderid and t.itemfee>0)";
		List<BuyItem> buyItemList = hibernateTemplate.find(qry, times, OrderConstant.STATUS_PAID_SUCCESS);
		model.put("buyItemList", buyItemList);
		return "admin/goods/unaddOrderList.vm";
	}
	@RequestMapping("/admin/goods/order/addGoodsOrderByItem.xhtml")
	public String addGoodsOrderByItem(Long orderid, ModelMap model){
		TicketOrder order = daoService.getObject(TicketOrder.class, orderid);
		if(order==null) return showJsonError(model, "订单不存在！");
		ErrorCode<GoodsOrder> code = goodsOrderService.addGoodsOrderByBuyItem(order);
		if(!code.isSuccess()) return showJsonError(model, code.getMsg());
		List<SMSRecord> smsList = messageService.addMessage(code.getRetval()).getRetval();
		if(!CollectionUtils.isEmpty(smsList)){
			for (SMSRecord sms : smsList) {
				if(sms!=null) untransService.sendMsgAtServer(sms, false);
			}
		}
		return showJsonSuccess(model);
	}
	@RequestMapping("/admin/goods/order/giftOrderList.xhtml")
	public String giftOrderList(Long gid, String status, String tradeNo, String mobile, Timestamp timeFrom, Timestamp timeTo, ModelMap model){
		Goods goods = daoService.getObject(Goods.class, gid);
		GoodsGift gift = daoService.getObjectByUkey(GoodsGift.class, "goodsid", gid, true);
		List<GoodsOrder> orderList = goodsOrderService.getGoodsOrderList(gid, status, tradeNo, mobile, timeFrom, timeTo);
		Map<Long, Member> memberMap = new HashMap<Long, Member>();
		Map<String, Integer> buynumMap = new HashMap<String, Integer>();
		for(GoodsOrder order : orderList){
			if(!memberMap.containsKey(order.getMemberid())){
				memberMap.put(order.getMemberid(), daoService.getObject(Member.class, order.getMemberid()));
			}
			if(!buynumMap.containsKey(order.getMobile()))
				buynumMap.put(order.getMobile(), goodsOrderService.getGewaorderCountByMobile(null, order.getMobile(), "goods"));
		}
		model.put("gift", gift);
		model.put("goods", goods);
		model.put("buynumMap", buynumMap);
		model.put("memberMap", memberMap);
		model.put("orderList", orderList);
		
		return "admin/goods/orderList_gift.vm";
	}
	//套餐订单
	@RequestMapping("/admin/goods/order/mealOrderList.xhtml")
	public String mealOrderList(SearchOrderCommand soc, ModelMap model){
		List<Long> cinemaidList = hibernateTemplate.find("select distinct g.relatedid from Goods g where g.tag=? and g.status!=?", GoodsConstant.GOODS_TAG_BMH, Status.DEL);
		List<BaseInfo> cinemaList = new ArrayList<BaseInfo>();
		BaseInfo info = null;
		for(Long cid : cinemaidList){
			info = daoService.getObject(Cinema.class, cid);
			if(info==null){
				info = daoService.getObject(Sport.class, cid);
			}
			if(info!=null)cinemaList.add(info);
		}
		Map<Long, List<Goods>> goodsMap = new HashMap<Long, List<Goods>>();
		for(BaseInfo bi : cinemaList){
			goodsMap.put(bi.getId(), hibernateTemplate.find("from Goods g where g.tag=? and g.relatedid=? order by g.goodssort", GoodsConstant.GOODS_TAG_BMH, bi.getId()));
		}
		model.put("goodsMap", goodsMap);
		model.put("cinemaList", cinemaList);
		List<GewaOrder> orderList = new ArrayList<GewaOrder>();
		orderList.addAll(goodsOrderService.getGoodsOrderList(BaseGoods.class, soc));
		Collections.sort(orderList, new PropertyComparator("addtime", false, false));
		Map<Long, Boolean> takeMap = new HashMap<Long, Boolean>();
		Map<String, Integer> buynumMap = new HashMap<String, Integer>();
		String ordertype = OrderResult.ORDERTYPE_MEAL;
		BaseGoods goods = null;
		Map<Long, BaseInfo> cinemaMap = new HashMap<Long, BaseInfo>();
		for(GewaOrder order : orderList){
			goods = daoService.getObject(BaseGoods.class, ((GoodsOrder)order).getGoodsid());
			info = daoService.getObject(Cinema.class, goods.getRelatedid());
			if(info==null){
				info = daoService.getObject(Sport.class, goods.getRelatedid());
			}
			cinemaMap.put(order.getId(), info);
			takeMap.put(order.getId(), isTakeByTradeno(order.getTradeNo(), ordertype));
			if(!buynumMap.containsKey(order.getMobile()))
				buynumMap.put(order.getMobile(), goodsOrderService.getGewaorderCountByMobile(null, order.getMobile(), "goods"));
		}
		model.put("takeMap", takeMap);
		model.put("cinemaMap", cinemaMap);
		model.put("orderList", orderList);
		model.put("buynumMap", buynumMap);
		model.put("ordertype", StringUtils.isBlank(soc.getOrdertype())?" " : soc.getOrdertype());
		return "admin/goods/mealOrderList.vm";
	}
	@RequestMapping("/admin/ticket/takeTicket.xhtml")
	public String takeTicket(Long orderid, ModelMap model){
		GewaOrder order = daoService.getObject(GewaOrder.class, orderid);
		if(!order.getStatus().equals(OrderConstant.STATUS_PAID_SUCCESS)) {
			return showJsonSuccess(model, "该订单不是成功订单！");
		}
		String result = "";
		if(order instanceof TicketOrder){
			if(isTakeByTradeno(order.getTradeNo(), OrderResult.ORDERTYPE_TICKET)){
				result ="电影票纸已经取出";
			}else {
				result ="电影票纸没有取出";
			}
			List<BuyItem> itemList = daoService.getObjectListByField(BuyItem.class, "orderid", order.getId());
			for(BuyItem item : itemList){
				if(isTakeByTradeno(PayUtil.FLAG_GOODS + order.getTradeNo().substring(1), OrderResult.ORDERTYPE_MEAL)){
					result += "," + item.getGoodsname()+"票纸已经取出";
				}else {
					result += "," + item.getGoodsname()+"票纸没有取出";
				}
			}
		}else if(order instanceof GoodsOrder){
			if(isTakeByTradeno(order.getTradeNo(), OrderResult.ORDERTYPE_MEAL)){
				result = "已经取票！";
			}else {
				result = "没有取票或者该订单还没有被同步！";
			}
		}
		return showJsonSuccess(model, result);
	}
	private boolean isTakeByTradeno(String tradeno, String ordertype){
		String qry = "from OrderResult o where o.tradeno=? and o.istake=? and o.ordertype=?";
		List list = hibernateTemplate.find(qry, tradeno, "Y", ordertype);
		return list.size()>0;
	}
	@RequestMapping("/admin/ticket/confirmGoodsOrderSuccess.xhtml")
	public String confirmSuccess(Long orderid, ModelMap model){
		User user = getLogonUser();
		if(user==null) return showJsonError(model, "请先登录");
		GoodsOrder order = daoService.getObject(GoodsOrder.class, orderid);
		if(order==null) return showJsonError(model, "该订单不存在！");
		if(!order.isPaidFailure() && !order.isPaidUnfix()) return showJsonError(model, "状态有错误！");
		String username = "";
		ErrorCode result = orderProcessService.processOrder(order, "重新确认", null);
		if(result.isSuccess()) {
			dbLogger.warn(username + "转换订单状态为交易成功：" + order.getTradeNo());	
			return showJsonSuccess(model);
		}else{
			return showJsonError(model, "转换失败：" + result.getMsg());
		}
	}
}
