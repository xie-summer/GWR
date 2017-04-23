/**
 * 
 */
package com.gewara.service.drama.impl;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
import org.springframework.beans.support.PropertyComparator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.ModelMap;

import com.gewara.command.SearchOrderCommand;
import com.gewara.command.TheatrePriceCommand;
import com.gewara.constant.ApiConstant;
import com.gewara.constant.GoodsConstant;
import com.gewara.constant.OdiConstant;
import com.gewara.constant.PayConstant;
import com.gewara.constant.Status;
import com.gewara.constant.TagConstant;
import com.gewara.constant.TheatreSeatConstant;
import com.gewara.constant.order.BuyItemConstant;
import com.gewara.constant.sys.ConfigConstant;
import com.gewara.constant.sys.ErrorCodeConstant;
import com.gewara.constant.ticket.OrderConstant;
import com.gewara.constant.ticket.OrderExtraConstant;
import com.gewara.constant.ticket.PartnerConstant;
import com.gewara.helper.DownOrderHelper;
import com.gewara.helper.TspHelper;
import com.gewara.helper.discount.DramaSpecialDiscountHelper;
import com.gewara.helper.discount.SpecialDiscountHelper;
import com.gewara.helper.discount.SportSpecialDiscountHelper.OrderCallback;
import com.gewara.helper.order.DramaOrderContainer;
import com.gewara.helper.order.DramaOrderHelper;
import com.gewara.helper.order.GewaOrderHelper;
import com.gewara.helper.order.OrderContainer;
import com.gewara.helper.order.OrderOther;
import com.gewara.model.acl.User;
import com.gewara.model.api.ApiUser;
import com.gewara.model.common.GewaConfig;
import com.gewara.model.common.LastOperation;
import com.gewara.model.drama.DisQuantity;
import com.gewara.model.drama.Drama;
import com.gewara.model.drama.DramaOrder;
import com.gewara.model.drama.DramaOrder2Seat;
import com.gewara.model.drama.OpenDramaItem;
import com.gewara.model.drama.OpenTheatreSeat;
import com.gewara.model.drama.SellDramaSeat;
import com.gewara.model.drama.TheatreField;
import com.gewara.model.drama.TheatreProfile;
import com.gewara.model.drama.TheatreSeatArea;
import com.gewara.model.drama.TheatreSeatPrice;
import com.gewara.model.drama.TspSaleCount;
import com.gewara.model.goods.TicketGoods;
import com.gewara.model.pay.BuyItem;
import com.gewara.model.pay.DealExpressOrder;
import com.gewara.model.pay.Discount;
import com.gewara.model.pay.ElecCard;
import com.gewara.model.pay.GewaOrder;
import com.gewara.model.pay.GoodsOrder;
import com.gewara.model.pay.OrderAddress;
import com.gewara.model.pay.OrderExtra;
import com.gewara.model.pay.OrderNote;
import com.gewara.model.pay.Spcounter;
import com.gewara.model.pay.SpecialDiscount;
import com.gewara.model.user.Member;
import com.gewara.model.user.MemberInfo;
import com.gewara.pay.PayUtil;
import com.gewara.pay.PayValidHelper;
import com.gewara.service.OrderException;
import com.gewara.service.drama.DramaOrderService;
import com.gewara.service.drama.DramaPlayItemService;
import com.gewara.service.drama.OpenDramaService;
import com.gewara.service.order.GoodsOrderService;
import com.gewara.service.order.impl.GewaOrderServiceImpl;
import com.gewara.support.ErrorCode;
import com.gewara.support.MultiPropertyComparator;
import com.gewara.untrans.drama.TheatreOrderService;
import com.gewara.untrans.monitor.OrderMonitorService;
import com.gewara.util.BeanUtil;
import com.gewara.util.DateUtil;
import com.gewara.util.JsonUtils;
import com.gewara.util.StringUtil;
import com.gewara.util.ValidateUtil;
import com.gewara.util.VmUtils;

@Service("dramaOrderService")
public class DramaOrderServiceImpl  extends GewaOrderServiceImpl implements DramaOrderService{
	@Autowired@Qualifier("theatreOrderService")
	private TheatreOrderService theatreOrderService;
	
	@Autowired@Qualifier("goodsOrderService")
	private GoodsOrderService goodsOrderService;
	
	@Autowired@Qualifier("openDramaService")
	private OpenDramaService openDramaService;
	
	@Autowired@Qualifier("dramaPlayItemService")
	private DramaPlayItemService dramaPlayItemService;
	
	@Autowired@Qualifier("orderMonitorService")
	private OrderMonitorService orderMonitorService;
	
	@Override
	public ErrorCode<DramaOrder> addDramaOrder(OpenDramaItem odi, Member member, String mobile, Integer quantity, Long disid, Long priceid, ApiUser partner, String ukey){
		if(priceid == null) return ErrorCode.getFailure(ApiConstant.CODE_SIGN_ERROR, "价格错误！");
		if(quantity == null || quantity < 1) return ErrorCode.getFailure(ApiConstant.CODE_SIGN_ERROR, "数量有错误！");
		if(!odi.isBooking()) return ErrorCode.getFailure(ApiConstant.CODE_SIGN_ERROR, "场次已关闭购票！");
		
		List<TheatrePriceCommand> commandList = new ArrayList<TheatrePriceCommand>();
		TheatrePriceCommand command = new TheatrePriceCommand();
		command.setQuantity(quantity);
		command.setItemid(odi.getDpid());
		if(disid != null){
			command.setTag(GoodsConstant.CHECK_GOODS_DISCOUNT);
			command.setTspid(disid);
		}else{
			command.setTag(GoodsConstant.CHECK_GOODS_PRICE);
			command.setTspid(priceid);
		}
		commandList.add(command);
		return addDramaOrder(commandList, member, mobile, partner, ukey);
	}
	
	@Override
	public ErrorCode<DramaOrder> addDramaOrder(List<TheatrePriceCommand> commandList, Member member, String mobile, ApiUser partner, String ukey){
		if(!ValidateUtil.isMobile(mobile)) return ErrorCode.getFailure(ApiConstant.CODE_SIGN_ERROR, "手机格式错误！");
		if(CollectionUtils.isEmpty(commandList)){
			return ErrorCode.getFailure(ApiConstant.CODE_SIGN_ERROR, "场次或价格错误！");
		}
		GewaConfig config = baseDao.getObject(GewaConfig.class, ConfigConstant.CFG_PAUSE_DRAMA);
		Timestamp cur = DateUtil.getCurFullTimestamp();
		Timestamp pause = Timestamp.valueOf(config.getContent());
		if(cur.before(pause)){
			return ErrorCode.getFailure(ApiConstant.CODE_DATA_ERROR, "暂停售票至" + DateUtil.format(pause, "HH:mm"));
		}
		Long memberid = null;
		String membername = null;
		if(member!=null){
			memberid = member.getId();
			if(partner!=null){
				membername = membername + "@"+partner.getBriefname();
			}else {
				membername = member.getNickname();
			}
		}else {
			memberid = partner.getId();
		}
		if(StringUtils.isBlank(ukey)) ukey = String.valueOf(memberid);
		List<TspSaleCount> tscList = new ArrayList<TspSaleCount>();
		ErrorCode<List<BuyItem>> codeItem = getBuyItem(commandList, partner, tscList);
		if(!codeItem.isSuccess()) return ErrorCode.getFailure(codeItem.getErrcode(), codeItem.getMsg());
		List<BuyItem> itemList = codeItem.getRetval();
		TheatrePriceCommand command = commandList.get(0);
		TheatreSeatPrice seatPrice = null;
		if(StringUtils.equals(command.getTag(), OdiConstant.CHECK_THEATRE_PRICE)){
			seatPrice = baseDao.getObject(TheatreSeatPrice.class, command.getTspid());
		}else{
			DisQuantity disQuantity = baseDao.getObject(DisQuantity.class, command.getTspid());
			seatPrice = baseDao.getObject(TheatreSeatPrice.class, disQuantity.getTspid());
		}
		OpenDramaItem odi = baseDao.getObjectByUkey(OpenDramaItem.class, "dpid", seatPrice.getDpid());
		TheatreSeatArea seatArea = baseDao.getObject(TheatreSeatArea.class, seatPrice.getAreaid());
		DramaOrder order = new DramaOrder(memberid, membername, odi, ukey);
		if(odi.hasGewara()){
			order.setStatus(OrderConstant.STATUS_NEW);
		}
		Timestamp t = DateUtil.addDay(odi.getPlaytime(),1);
		String checkpass = nextRandomNum(t, 8, "0");
		order.setCategory(odi.getSeller());
		order.setAreaid(seatArea.getId());
		order.setMobile(mobile);
		order.setTradeNo(PayUtil.getDramaTradeNo());
		order.setCheckpass(checkpass);
		order.setOrdertitle(odi.getDramaname() + "演出票");
		int quantity = 0, totalcost = 0, totalfee = 0;
		
		Timestamp validtime = DateUtil.addMinute(order.getAddtime(), OdiConstant.MAX_MINUTS_TICKETS);
		//首先默认为0
		order.setTotalfee(0);
		order.setTotalcost(0);
		order.setUnitprice(0);
		order.setCostprice(0);
		order.setQuantity(0);		
		order.setValidtime(validtime);
		baseDao.saveObject(order);
		for (BuyItem item : itemList) {
			item.setOrderid(order.getId());
			item.setMemberid(order.getMemberid());
			item.setValidtime(validtime);
			quantity += item.getQuantity();
			totalcost += item.getTotalcost();
			totalfee += item.getDue();
		}
		if(partner!=null){
			order.setPartnerid(partner.getId());
		}
		//计算价格数量
		order.setTotalfee(totalfee);
		order.setTotalcost(totalcost);
		order.setUnitprice(totalfee/quantity);
		order.setCostprice(totalcost/quantity);
		order.setQuantity(quantity);
		setOrderDescription(order, odi);
		baseDao.saveObject(order);
		baseDao.saveObjectList(itemList);
		getTspSaleCount(tscList, order);
		operationService.updateLastOperation("D" + memberid + StringUtil.md5(ukey), order.getTradeNo(), order.getAddtime(), odi.getEndtime(), "drama");
		return ErrorCode.getSuccessReturn(order);
	}
	private void getTspSaleCount(List<TspSaleCount> tscList, DramaOrder order){
		for(TspSaleCount tsc : tscList){
			tsc.setOrderid(order.getId());
			tsc.setValidtime(order.getValidtime());
		}
		baseDao.saveObjectList(tscList);
	}
	private void setOrderDescription(DramaOrder order, OpenDramaItem odi){
		String ordertitle = odi.getTheatrename()+"演出票";
		order.setOrdertitle(ordertitle);
		TheatreField field = baseDao.getObject(TheatreField.class, odi.getRoomid());
		Map<String, String> descMap = VmUtils.readJsonToMap(order.getDescription2());
		descMap.put("场馆", odi.getTheatrename());
		descMap.put("项目", odi.getDramaname());
		descMap.put("厅名", odi.getRoomname());
		descMap.put("场区", field.getName());
		descMap.put("时间", DateUtil.format(odi.getPlaytime(),"yyyy-MM-dd HH:mm"));
		order.setDescription2(JsonUtils.writeMapToJson(descMap));
	}
	private ErrorCode<List<BuyItem>> getBuyItem(List<TheatrePriceCommand> commandList, ApiUser partner, List<TspSaleCount> tscList){
		Map<Long, Integer> quantityMap = new HashMap<Long, Integer>();
		Map<Long/*gspid*/, Integer> priceQuantityMap = new HashMap<Long, Integer>();
		Map<Long/*gspid*/, Integer> disQuantityMap = new HashMap<Long, Integer>();
		Map<Long, TheatreSeatArea> areaMap = new HashMap<Long, TheatreSeatArea>();
		String paymethod = null, seller = null;
		Long dramaid = null, theatreid = null;
		List<BuyItem> itemList = new ArrayList<BuyItem>();
		Set<TheatreSeatPrice> priceSet = new HashSet<TheatreSeatPrice>();
		Set<DisQuantity> disSet = new HashSet<DisQuantity>();
		for (TheatrePriceCommand command : commandList) {
			OpenDramaItem odi = null;
			TheatreSeatPrice seatPrice = null;
			DisQuantity discount = null;
			if(command.getItemid() == null || command.getTspid() == null 
					|| command.getQuantity() == null || command.getQuantity()<1 
					|| !OdiConstant.CHECK_THEATRELIST.contains(command.getTag())){
				return ErrorCode.getFailure(ApiConstant.CODE_SIGN_ERROR, "场次或价格错误！");
			}
			TheatreSeatArea seatArea = null;
			int quantity = 0;
			odi = baseDao.getObjectByUkey(OpenDramaItem.class, "dpid", command.getItemid());
			if(odi == null) return ErrorCode.getFailure(ApiConstant.CODE_SIGN_ERROR, "场次不存在或被删除！");
			String odiname = StringUtils.isBlank(odi.getName())? 
					DateUtil.formatDate(odi.getPlaytime()) + " " + DateUtil.getCnWeek(odi.getPlaytime()) + " " + DateUtil.format(odi.getPlaytime(), "HH:mm")
					: (StringUtils.equals(odi.getPeriod(), Status.Y)?
							"[" + odi.getName() +"]" + DateUtil.formatDate(odi.getPlaytime()) + " " + DateUtil.getCnWeek(odi.getPlaytime()) + " " + DateUtil.format(odi.getPlaytime(), "HH:mm")
							: odi.getName());
			if(!odi.isOpenprice()) return ErrorCode.getFailure(ApiConstant.CODE_SIGN_ERROR, odiname + "非价格场次！"); 
			if(!odi.isBooking() || odi.hasUnOpenToGewa()) return ErrorCode.getFailure(ApiConstant.CODE_SIGN_ERROR, odiname + "场次已关闭购票！");
			if(StringUtils.isBlank(seller)){
				seller = odi.getSeller();
			}else{
				if(!StringUtils.equals(seller, odi.getSeller())){
					return ErrorCode.getFailure(ApiConstant.CODE_SIGN_ERROR, "不支持多种销售渠道场次购票！");
				}
			}
			if(partner!=null) if(!odi.isPartnerBooking()) return ErrorCode.getFailure( "本场次不接受预定！");
			if(theatreid == null){
				theatreid = odi.getTheatreid();
			}else if(!theatreid.equals(odi.getTheatreid())){
				return ErrorCode.getFailure(ApiConstant.CODE_SIGN_ERROR, "不能跨场馆购票！");
			}
			if(dramaid == null){
				dramaid = odi.getDramaid();
			}else if(!dramaid.equals(odi.getDramaid())){
				return ErrorCode.getFailure(ApiConstant.CODE_SIGN_ERROR, "不能跨项目购票！");
			}
			String tmpMethod = JsonUtils.getJsonValueByKey(odi.getOtherinfo(), "defaultpaymethod");
			if(StringUtils.isNotBlank(tmpMethod)){
				if(StringUtils.isBlank(paymethod)){
					paymethod = tmpMethod;
				}else if(!StringUtils.equals(paymethod, tmpMethod)){
					return ErrorCode.getFailure(ApiConstant.CODE_SIGN_ERROR, "支付方式限制！");
				}
			}
			if(StringUtils.equals(command.getTag(), GoodsConstant.CHECK_GOODS_PRICE)){
				seatPrice = baseDao.getObject(TheatreSeatPrice.class, command.getTspid());
				if(seatPrice == null) return ErrorCode.getFailure(ApiConstant.CODE_SIGN_ERROR, "参数错误，请重选！");
				if(!seatPrice.hasAllownum()) return ErrorCode.getFailure(ApiConstant.CODE_SIGN_ERROR, odiname +",价格："+ seatPrice.getPrice() +"，库存不足！");
				if(!seatPrice.hasRetail()) return ErrorCode.getFailure(ApiConstant.CODE_SIGN_ERROR, odiname +",价格："+ seatPrice.getPrice() +",不支持零售！");
				Integer priceQuantity = priceQuantityMap.get(seatPrice.getId());
				quantity = command.getQuantity();
				if(priceQuantity == null){
					priceQuantity = quantity;
				}else{
					priceQuantity += quantity;
				}
				if(seatPrice.getMaxbuy()< priceQuantity){
					return ErrorCode.getFailure(ApiConstant.CODE_SIGN_ERROR, "”" + odiname +"“ ”"+ seatPrice.getPrice() +"元 “  每单最多购买"+ seatPrice.getMaxbuy() + "张");
				}
				String msg = TspHelper.getTheatrePriceDisabledReason(seatPrice, priceQuantity);
				if(StringUtils.isNotBlank(msg)) return ErrorCode.getFailure(ApiConstant.CODE_SIGN_ERROR, msg);
				priceQuantityMap.put(seatPrice.getId(), priceQuantity);
				priceSet.add(seatPrice);
			}else if(StringUtils.equals(command.getTag(), GoodsConstant.CHECK_GOODS_DISCOUNT)){
				discount = baseDao.getObject(DisQuantity.class, command.getTspid());
				if(discount == null) return ErrorCode.getFailure(ApiConstant.CODE_SIGN_ERROR, "参数错误，请重选！");
				if(!discount.hasBooking()){
					return ErrorCode.getFailure(ApiConstant.CODE_SIGN_ERROR, "套票未开放预订！");
				}
				seatPrice = baseDao.getObject(TheatreSeatPrice.class, discount.getTspid());
				Integer disQuantity = disQuantityMap.get(discount.getId());
				if(disQuantity == null){
					disQuantity = command.getQuantity();
				}else{
					disQuantity += command.getQuantity();
				}
				if(discount.getMaxbuy()< disQuantity){
					return ErrorCode.getFailure(ApiConstant.CODE_SIGN_ERROR, "”" + odiname +"“ ”"+ discount.getPrice() +"元 “  每单最多购买"+discount.getMaxbuy() + "张");
				}
				disQuantityMap.put(discount.getId(), disQuantity);
				quantity = command.getQuantity() * discount.getQuantity();
				disSet.add(discount);
				Integer priceQuantity = priceQuantityMap.get(seatPrice.getId());
				if(priceQuantity == null){
					priceQuantity = quantity;
				}else{
					priceQuantity += quantity;
				}
				String msg = TspHelper.getTheatrePriceDisabledReason(seatPrice, priceQuantity);
				if(StringUtils.isNotBlank(msg)) return ErrorCode.getFailure(ApiConstant.CODE_SIGN_ERROR, msg);
				priceQuantityMap.put(seatPrice.getId(), priceQuantity);
			}
			if(seatPrice == null) return ErrorCode.getFailure(ApiConstant.CODE_SIGN_ERROR, "参数错误，请重选！");
			if(!seatPrice.hasBooking()) return ErrorCode.getFailure(ApiConstant.CODE_SIGN_ERROR, "购买失败，价格已关闭购票！");
			seatArea = areaMap.get(seatPrice.getAreaid());
			if(seatArea == null){
				seatArea = baseDao.getObject(TheatreSeatArea.class, seatPrice.getAreaid());
				areaMap.put(seatPrice.getAreaid(), seatArea);
			}
			if(seatArea == null || !seatArea.hasStatus(Status.Y)) return ErrorCode.getFailure(ApiConstant.CODE_SIGN_ERROR, "场区不存在或已关闭卖票！");
			Integer odiQuantity = quantityMap.get(odi.getId());
			if(odiQuantity == null){
				odiQuantity = quantity;
			}else{
				odiQuantity += quantity;
			}
			if(odi.getMaxbuy() < odiQuantity) return ErrorCode.getFailure(ApiConstant.CODE_SIGN_ERROR, "”" + odiname +"“ 每单最多购买"+odi.getMaxbuy() + "张");
			quantityMap.put(odi.getId(), odiQuantity);
			BuyItem item = newBuyItem(odi, seatArea, seatPrice, discount, quantity); 
			String tmp = seatPrice.getPrice() + "元" ;
			if(discount != null){
				tmp = discount.getPrice() + "(" + seatPrice.getPrice() + " x " +discount.getQuantity()+")元";
			}
			String summary = odiname + "【" + tmp + (StringUtils.isBlank(seatPrice.getRemark())? "":seatPrice.getRemark()) + "】";
			item.setSummary(summary);
			itemList.add(item);
		}
		for (TheatreSeatPrice price : priceSet) {
			int quantity = priceQuantityMap.get(price.getId());
			TspHelper.updateTheatrePriceAddCounter(price, quantity);
			TspSaleCount tsc = new TspSaleCount(price, quantity);
			tscList.add(tsc);
		}
		baseDao.saveObjectList(disSet);
		baseDao.saveObjectList(priceSet);
		return ErrorCode.getSuccessReturn(itemList);
	}
	
	private BuyItem newBuyItem(OpenDramaItem odi, TheatreSeatArea seatArea, TheatreSeatPrice seatPrice, DisQuantity disQuantity, final int quantity){
		BuyItem item = new BuyItem(quantity);
		String goodsname = odi.getTheatrename() + "演出票" + seatPrice.getPrice() + "元 [" + seatArea.getAreaname() +"]";
		item.setGoodsname(goodsname);
		item.setPlacetype(TagConstant.TAG_THEATRE);
		item.setPlaceid(odi.getTheatreid());
		item.setItemid(odi.getDramaid());
		item.setItemtype(TagConstant.TAG_DRAMA);
		item.setSmallitemid(seatPrice.getId());
		item.setSmallitemtype(BuyItemConstant.SMALL_ITEMTYPE_PRICE);
		item.setCostprice(seatPrice.getCostprice());
		item.setOriprice(seatPrice.getTheatreprice());
		item.setUnitprice(seatPrice.getPrice());
		item.setQuantity(quantity);
		item.setPlaytime(odi.getPlaytime());
		item.setRemark(seatPrice.getRemark());
		item.setCitycode(odi.getCitycode());
		item.setRelatedid(odi.getDpid());
		item.setTag(BuyItemConstant.TAG_DRAMAPLAYITEM);
		item.setExpress(StringUtils.isBlank(odi.getExpressid()) ? Status.N : Status.Y);
		int totalcost = 0,	totalfee = 0;
		Map<String, String> otherInfoMap = JsonUtils.readJsonToMap(item.getOtherinfo());
		if(disQuantity != null){
			int tmpQuantity = quantity/disQuantity.getQuantity();
			item.setQuantity(quantity);
			item.setDisid(disQuantity.getId());
			item.setSettleid(disQuantity.getSettleid());
			totalcost = disQuantity.getCostprice() * tmpQuantity;
			totalfee = seatPrice.getPrice() * quantity;
			int distotalfee = disQuantity.getPrice()*tmpQuantity;
			int disfee = totalfee -distotalfee;
			item.setDisfee(disfee);
			otherInfoMap.put(BuyItemConstant.OTHERINFO_KEY_DISQUANTITY, tmpQuantity +"");
			otherInfoMap.put(BuyItemConstant.OTHERINFO_KEY_DISPRICE, disQuantity.getPrice()+"");
			otherInfoMap.put(BuyItemConstant.OTHERINFO_KEY_DISCOSTPRICE, disQuantity.getCostprice()+"");
		}else{
			totalcost = seatPrice.getCostprice() * quantity;
			totalfee = seatPrice.getPrice() * quantity;
			item.setSettleid(seatPrice.getSettleid());
		}
		item.setTotalfee(totalfee);
		item.setTotalcost(totalcost);
		String checkpass = nextRandomNum(DateUtil.addDay(odi.getPlaytime(), 1), 8, "0");
		item.setCheckpass(checkpass);
		item.setOtherinfo(JsonUtils.writeMapToJson(otherInfoMap));
		Map<String, String> descMap = new HashMap<String, String>();
		descMap.put("时间", DateUtil.format(odi.getPlaytime(),"yyyy-MM-dd HH:mm"));
		descMap.put("场区", seatArea.getName());
		item.setDescription(JsonUtils.writeMapToJson(descMap));
		return item;
	}
	
	@Override
	public void cancelDramaOrder(String tradeNo, String ukey, String reason) {
		DramaOrder order = baseDao.getObjectByUkey(DramaOrder.class, "tradeNo", tradeNo, false);
		cancelDramaOrder(order, ukey, reason);
	}
	@Override
	public void cancelDramaOrder(DramaOrder order, String ukey, String reason) {
		if(order.isNew() && order.getUkey().equals(ukey)){
			OpenDramaItem odi = baseDao.getObjectByUkey(OpenDramaItem.class, "dpid", order.getDpid());
			Timestamp validtime = new Timestamp(System.currentTimeMillis()-1000);
			order.setStatus(OrderConstant.STATUS_REPEAT);
			order.setValidtime(validtime);
			if(odi.isOpenseat()){
				List<SellDramaSeat> seatList = getDramaOrderSeatList(order.getId());
				for(SellDramaSeat seat : seatList){
					seat.setValidtime(validtime);
					baseDao.saveObject(seat);
				}
			}else{
				Map<TheatreSeatPrice, Integer> priceQuantityMap = new Hashtable<TheatreSeatPrice, Integer>();
				Map<Long, Map<DisQuantity, Integer>> priceDisMap = new Hashtable<Long, Map<DisQuantity,Integer>>();
				Map<OpenDramaItem, Integer> odiMap = new Hashtable<OpenDramaItem, Integer>();
				getPriceObjectList(order, odi, odiMap, priceQuantityMap, priceDisMap);
				List result = TspHelper.updateTheatrePriceSubAddCounter(priceQuantityMap);
				baseDao.saveObjectList(result);
				List<TspSaleCount> tscList = baseDao.getObjectListByField(TspSaleCount.class, "orderid", order.getId());
				baseDao.removeObjectList(tscList);
			}
			baseDao.saveObject(order);
			dbLogger.warn("取消未支付订单：" + order.getTradeNo() + "," + reason);
		}
		orderMonitorService.addOrderChangeLog(order.getTradeNo(), "自动取消", order,  reason);
	}
	@Override
	public DramaOrder getLastUnpaidDramaOrder(Long memberid, String ukey, Long itemid) {
		DetachedCriteria query = DetachedCriteria.forClass(DramaOrder.class);
		query.add(Restrictions.eq("memberid", memberid));
		if(itemid!=null) query.add(Restrictions.eq("dpid", itemid));
		query.add(Restrictions.eq("ukey", ukey));
		query.add(Restrictions.like("status", OrderConstant.STATUS_NEW, MatchMode.START));
		query.add(Restrictions.gt("validtime", new Timestamp(System.currentTimeMillis())));
		query.addOrder(Order.desc("addtime"));
		List<DramaOrder> result = hibernateTemplate.findByCriteria(query);
		if(result.isEmpty()) return null;
		return result.get(0);
	}
	@Override
	public ErrorCode checkOrderSeat(DramaOrder order, ModelMap model) {
		OpenDramaItem odi = baseDao.getObjectByUkey(OpenDramaItem.class, "dpid", order.getDpid(), false);
		List<BuyItem> buyList = baseDao.getObjectListByField(BuyItem.class, "orderid", order.getId());
		List<OpenDramaItem> itemList = getOpenDramaItemList(odi, buyList);
		Map<Long, OpenDramaItem> odiMap = BeanUtil.beanListToMap(itemList, "dpid");	
		OrderOther orderOther = theatreOrderService.getDramaOrderOtherData(order, buyList, odiMap, model);
		if(StringUtils.equals(orderOther.getTakemethod(), TheatreProfile.TAKEMETHOD_E)){
			OrderAddress orderAddress = baseDao.getObject(OrderAddress.class, order.getTradeNo());
			if(orderAddress == null) return ErrorCode.getFailure("请返回上一步，填写收件人及收件地址！");
		}
		if(odi.isOpenprice()) return ErrorCode.SUCCESS;
		//TODO:价格库存没有判断
		String msg = "";
		List<SellDramaSeat> seatList = getDramaOrderSeatList(order.getId());
		for(SellDramaSeat seat : seatList){
			if(!seat.getOrderid().equals(order.getId())) msg = "[" + seat.getSeatLabel() + "]";
		}
		if(StringUtils.isBlank(msg)) return ErrorCode.SUCCESS;
		return ErrorCode.getFailure(msg+"被占用");
	}
	@Override
	public List<GewaOrder> getAllDramaOrderList(SearchOrderCommand soc){
		if(soc.hasBlankCond()) return new ArrayList<GewaOrder>();
		List<DramaOrder> dramaOrderList = getDramaOrderList(soc);
		soc.setPlacetype(TagConstant.TAG_THEATRE);
		soc.setItemtype(TagConstant.TAG_DRAMA);
		List<GoodsOrder> goodsOrderList = goodsOrderService.getGoodsOrderList(TicketGoods.class, soc);
		List<GewaOrder> orderList = new ArrayList<GewaOrder>(dramaOrderList);
		orderList.addAll(goodsOrderList);
		Collections.sort(orderList, new MultiPropertyComparator(new String[]{"addtime"}, new boolean[]{false}));
		return orderList;
	}
	
	@Override
	public List<DramaOrder> getDramaOrderList(SearchOrderCommand soc) {	
		DetachedCriteria query = DetachedCriteria.forClass(DramaOrder.class, "o");
		if(StringUtils.isNotBlank(soc.getMobile())) query.add(Restrictions.eq("o.mobile", soc.getMobile()));
		if(StringUtils.isNotBlank(soc.getTradeNo())) query.add(Restrictions.eq("o.tradeNo", soc.getTradeNo()));
		if(StringUtils.isNotBlank(soc.getCitycode())) query.add(Restrictions.eq("o.citycode", soc.getCitycode()));
		if(soc.getAreaid()!=null) query.add(Restrictions.eq("o.areaid", soc.getAreaid()));
		String timeChange = "o.addtime";
		//查询交易成功时用付款成功时间做判断
		if(OrderConstant.STATUS_PAID_SUCCESS.equals(soc.getOrdertype())){
			timeChange = "o.paidtime";
		}
		if(soc.getMinute()!=null){
			Timestamp from = DateUtil.addMinute(new Timestamp(System.currentTimeMillis()), -soc.getMinute());
			query.add(Restrictions.ge(timeChange, from));
		}
		if(soc.getTimeFrom() != null){
			query.add(Restrictions.ge(timeChange, soc.getTimeFrom()));
		}
		if(soc.getTimeTo() != null){
			query.add(Restrictions.le(timeChange, soc.getTimeTo()));
		}
		if(StringUtils.isNotBlank(soc.getOrdertype())){//可能有过时自动取消的账单
			if(soc.getOrdertype().equals(OrderConstant.STATUS_CANCEL)){
				query.add(Restrictions.or(Restrictions.like("o.status", soc.getOrdertype(), MatchMode.START),
						Restrictions.and(Restrictions.like("o.status", OrderConstant.STATUS_NEW, MatchMode.START), 
								Restrictions.lt("o.validtime", new Timestamp(System.currentTimeMillis())))));
			}else{
				query.add(Restrictions.like("o.status", soc.getOrdertype(), MatchMode.START));
				if(StringUtils.startsWith(soc.getOrdertype(), OrderConstant.STATUS_NEW)){//可能有过时自动取消的账单
					query.add(Restrictions.ge("o.validtime", new Timestamp(System.currentTimeMillis())));
				}
			}
		}
		if(soc.getPlaceid()!=null) query.add(Restrictions.eq("o.theatreid", soc.getPlaceid()));
		if(soc.getItemid()!=null) query.add(Restrictions.eq("o.dramaid", soc.getItemid()));
		if(soc.getOrderid()!=null) query.add(Restrictions.eq("o.id", soc.getOrderid()));
		if(soc.getMpid() != null)query.add(Restrictions.eq("o.dpid", soc.getMpid()));
		if(StringUtils.isNotBlank(soc.getSno())) query.add(Restrictions.eq("o.sno", soc.getSno()));
		if(StringUtils.isNotBlank(soc.getExpressid())){
			DetachedCriteria sub = DetachedCriteria.forClass(OpenDramaItem.class, "d");
			if(StringUtils.equals(soc.getExpressid(), Status.Y)){
				sub.add(Restrictions.isNotNull("d.expressid"));
			}else{
				sub.add(Restrictions.isNull("d.expressid"));
			}
			sub.add(Restrictions.eqProperty("d.dpid", "o.dpid"));
			sub.setProjection(Projections.property("d.id"));
			query.add(Subqueries.exists(sub));
		}
		query.addOrder(Order.desc("addtime"));
		List<DramaOrder> orderList = hibernateTemplate.findByCriteria(query);
		return orderList;
	}
	@Override
	public ErrorCode<DramaOrder> addDramaOrder(OpenDramaItem odi, TheatreSeatArea seatArea, List<Long> seatidList, Long disid, String mobile, 
			Member member, String ukey, List<String> remoteLockList) throws OrderException{
		return addDramaOrder(odi, seatArea, seatidList, disid, mobile, member, null, null, ukey, remoteLockList);
	}
	@Override
	public ErrorCode<DramaOrder> addDramaOrder(OpenDramaItem odi, TheatreSeatArea seatArea, String seatLabel, Long disid, String mobile, 
			Member member, ApiUser partner, String ukey, List<String> remoteLockList) throws OrderException{
		List<Long> seatidList = openDramaService.getSeatidListBySeatLabel(seatArea.getId(), seatLabel);
		return addDramaOrder(odi, seatArea, seatidList, disid, mobile, member, partner, null, ukey, remoteLockList);
	}
	private ErrorCode<DramaOrder> addDramaOrder(OpenDramaItem odi, TheatreSeatArea seatArea, List<Long> seatidList, Long disid, 
			String mobile, Member member, ApiUser partner, String userid, String ukey, List<String> remoteLockList) throws OrderException{
		if(odi==null) return ErrorCode.getFailure("该场次不存在！");
		if(member!=null){
			if(!odi.isBooking() || odi.hasUnOpenToGewa()) return ErrorCode.getFailure( "本场次不接受预定！");
		}
		if(partner!=null) if(!odi.isPartnerBooking()) return ErrorCode.getFailure( "本场次不接受预定！");
		if(seatArea == null) return ErrorCode.getFailure("该场区不存在！");
		if(!seatArea.hasStatus(Status.Y)) return ErrorCode.getFailure(seatArea.getAreaname() + "场区不接受预定！");
		GewaConfig config = baseDao.getObject(GewaConfig.class, ConfigConstant.CFG_PAUSE_DRAMA);
		Timestamp cur = DateUtil.getCurFullTimestamp();
		Timestamp pause = Timestamp.valueOf(config.getContent());
		if(cur.before(pause)){
			return ErrorCode.getFailure("暂停售票至" + DateUtil.format(pause, "HH:mm"));
		}
		Set<Long> seatidSet = new LinkedHashSet<Long>(seatidList);
		if(StringUtils.isNotBlank(odi.getBuylimit()) && !StringUtils.contains(odi.getBuylimit(), ""+seatidSet.size())){
			return ErrorCode.getFailure("本场次购买座位数量只能是" + StringUtil.insertStr(odi.getBuylimit(), "或") + "张");
		}
		if(!ValidateUtil.isMobile(mobile)) return ErrorCode.getFailure("手机号有错误！");
		if(seatidList.size()==0) return ErrorCode.getFailure("请选择场座位！");
		if(odi.getMaxbuy()< seatidList.size()) return ErrorCode.getFailure("每次最多选" + odi.getMaxbuy() + "个座位！");
		
		List<OpenTheatreSeat> oseatList = baseDao.getObjectList(OpenTheatreSeat.class, seatidSet);
		ErrorCode code = validateSeatPosition(seatArea, oseatList, remoteLockList);//选的位置是否有问题
		if(!code.isSuccess()) return code;
		code = validLoveSeat(oseatList); //是否单选情侣座
		if(!code.isSuccess()) return code;
		
		List<SellDramaSeat> sellSeatList = createSellDramaSeat(oseatList);
		Map<Long/*id*/, SellDramaSeat> sellSeatMap = BeanUtil.beanListToMap(sellSeatList, "id");
		code = validateSeatLock(oseatList, sellSeatMap, remoteLockList); //座位检查
		if(!code.isSuccess()) throw new OrderException(code.getErrcode(), code.getMsg());
		Long memberid = null;
		String membername = null;
		Long partnerid = null;
		if(member!=null){
			memberid = member.getId();
			membername = member.getNickname();
			partnerid = PartnerConstant.GEWA_SELF;
			if(partner!=null){ 
				membername = membername + "@"+partner.getBriefname();
				partnerid = partner.getId();
			}
		}else {
			memberid = partner.getId();
			membername = StringUtils.isBlank(userid) ? partner.getBriefname():userid+"@"+partner.getBriefname();
			partnerid = partner.getId();
		}
		if(StringUtils.isBlank(ukey)) ukey = memberid+"";
		Timestamp t = DateUtil.addDay(odi.getPlaytime(),1);
		String checkpass = nextRandomNum(t, 8, "0");
		DramaOrder order = new DramaOrder(memberid, membername, odi, partnerid, ukey);
		if(odi.hasGewara()){
			order.setStatus(OrderConstant.STATUS_NEW);
		}
		order.setCategory(odi.getSeller());
		order.setAreaid(seatArea.getId());
		order.setMobile(mobile);
		order.setTradeNo(PayUtil.getDramaTradeNo());
		order.setCheckpass(checkpass);
		order.setOrdertitle(odi.getDramaname() + "演出票");
		int quantity = 0, totalcost = 0, totalfee = 0;
		
		Timestamp validtime = DateUtil.addMinute(order.getAddtime(), OdiConstant.MAX_MINUTS_TICKETS);
		//首先默认为0
		order.setTotalfee(0);
		order.setTotalcost(0);
		order.setUnitprice(0);
		order.setCostprice(0);
		order.setQuantity(oseatList.size());
		order.setValidtime(validtime);
		
		baseDao.saveObject(order);
		ErrorCode<List<BuyItem>> itemCode = getBuyItemByOpenSeatList(order, odi, seatArea, oseatList, sellSeatMap, membername, disid);
		if(!itemCode.isSuccess()){
			throw new OrderException(itemCode.getErrcode(), itemCode.getMsg());
		}
		List<BuyItem> itemList = itemCode.getRetval();
		for (BuyItem item : itemList) {
			item.setOrderid(order.getId());
			item.setMemberid(order.getMemberid());
			item.setValidtime(validtime);
			quantity += item.getQuantity();
			totalcost += item.getTotalcost();
			totalfee += item.getDue();
		}
		if(partner!=null){
			order.setPartnerid(partner.getId());
		}
		//计算价格数量
		order.setTotalfee(totalfee);
		order.setTotalcost(totalcost);
		order.setUnitprice(totalfee/quantity);
		order.setCostprice(totalcost/quantity);
		order.setQuantity(quantity);
		setOrderDescription(order, odi);
		baseDao.saveObject(order);
		
		baseDao.saveObjectList(itemList);
		for(SellDramaSeat sellseat : sellSeatList){
			sellseat.setOrderid(order.getId());
			baseDao.saveObject(sellseat);
			baseDao.saveObject(new DramaOrder2Seat(order.getId(), sellseat.getId()));
		}
		operationService.updateLastOperation("D" + memberid + StringUtil.md5(ukey), order.getTradeNo(), order.getAddtime(), odi.getPlaytime(), "drama");
		return ErrorCode.getSuccessReturn(order);
	}
	
	private ErrorCode<List<BuyItem>> getBuyItemByOpenSeatList(DramaOrder order, OpenDramaItem odi, TheatreSeatArea seatArea, 
			List<OpenTheatreSeat> oseatList, Map<Long/*id*/, SellDramaSeat> sellSeatMap, String membername, Long disid){
		DisQuantity disQuantity = null;
		if(disid != null){
			disQuantity = baseDao.getObject(DisQuantity.class, disid);
			if(disQuantity == null){
				return ErrorCode.getFailure(ApiConstant.CODE_SIGN_ERROR, "非法套票！");
			}
			if(!disQuantity.hasBooking()){
				return ErrorCode.getFailure(ApiConstant.CODE_SIGN_ERROR, "套票未开放预订！");
			}
		}
		Map<TheatreSeatPrice/*单价*/, Integer/*数量*/> tmpMap = new HashMap<TheatreSeatPrice, Integer>();
		List<TheatreSeatPrice> tspList =dramaPlayItemService.getTspList(seatArea.getDpid(), seatArea.getId());
		TspHelper tspHelper = new TspHelper(tspList);
		Map<String, TheatreSeatPrice> tspMap = BeanUtil.beanListToMap(tspHelper.getTspBySno(), "seattype");
		Map<TheatreSeatPrice, List<String>> tspSeatMap = new Hashtable<TheatreSeatPrice, List<String>>();
		for(OpenTheatreSeat seat : oseatList){
			TheatreSeatPrice seatPrice = tspMap.get(seat.getSeattype());
			if(seatPrice == null){
				return ErrorCode.getFailure(ApiConstant.CODE_SIGN_ERROR, "价格数据错误，购票失败");
			}
			if(tmpMap.containsKey(seatPrice)) {
				tmpMap.put(seatPrice, tmpMap.get(seatPrice)+1);
			}else {
				tmpMap.put(seatPrice, 1);
			}
			SellDramaSeat sseat = sellSeatMap.get(seat.getId());
			sseat.copyFrom(seat);
			sseat.setValidtime(order.getValidtime());
			sseat.setRemark("[订" + membername + "]" + StringUtils.defaultString(sseat.getRemark()));
			List<String> seatList = tspSeatMap.get(seatPrice);
			if(seatList == null){
				seatList = new ArrayList<String>();
				tspSeatMap.put(seatPrice, seatList);
			}
			seatList.add(sseat.getKey());
			baseDao.saveObject(seat);
		}
		String odiname = DateUtil.formatDate(odi.getPlaytime()) + " " + DateUtil.getCnWeek(odi.getPlaytime()) + " " + DateUtil.format(odi.getPlaytime(), "HH:mm");
		List<BuyItem> itemList = new ArrayList<BuyItem>();
		String disMsg = "";
		for(TheatreSeatPrice tsp : tmpMap.keySet()){
			Integer tmpQuantity = tmpMap.get(tsp);
			if(tmpQuantity == 0){
				return ErrorCode.getFailure(ApiConstant.CODE_SIGN_ERROR, "锁座错误！");
			}
			List<String> seatList = new ArrayList<String>(tspSeatMap.get(tsp));
			Integer tetailQuantity = 0;
			if(disQuantity != null && tsp.getId().equals(disQuantity.getTspid())){
				if(disQuantity.getPrice() <0 ) return ErrorCode.getFailure(ApiConstant.CODE_SIGN_ERROR, "套票价格错误！");
				if(!disQuantity.hasBooking()){
					return ErrorCode.getFailure(ApiConstant.CODE_SIGN_ERROR, "套票未开放预订！");
				}
				tetailQuantity = tmpQuantity % disQuantity.getQuantity();
				int quantity = tmpQuantity - tetailQuantity;
				List<String> tmpList = BeanUtil.getSubList(seatList, 0, quantity);
				seatList.removeAll(tmpList);
				BuyItem item = newBuyItem(odi, seatArea, tsp, disQuantity, quantity);
				String tmp = disQuantity.getPrice() + "元 (含" + tsp.getPrice() + "元  × " +disQuantity.getQuantity()+")";
				String summary = odiname + "【" + tmp + (StringUtils.isBlank(tsp.getRemark())? "":tsp.getRemark()) + "】";
				item.setSummary(summary);
				item.setOtherinfo(JsonUtils.addJsonKeyValue(item.getOtherinfo(), BuyItemConstant.OTHERINFO_KEY_SEATLABEL, StringUtils.join(tmpList, ",")));
				itemList.add(item);
				disMsg = tmp;
				Map<String, String> otherInfoMap = JsonUtils.readJsonToMap(order.getOtherinfo());
				otherInfoMap.put("disid", disQuantity.getId() + "");
				order.setOtherinfo(JsonUtils.writeObjectToJson(otherInfoMap));
			}else{
				tetailQuantity = tmpQuantity;
			}
			if(tetailQuantity > 0){
				if(!tsp.hasRetail()){
					return ErrorCode.getFailure(ApiConstant.CODE_SIGN_ERROR, "套票不支持零售！");
				}
				BuyItem item = newBuyItem(odi, seatArea, tsp, null, tetailQuantity);
				String tmp = tsp.getPrice() + "元" ;
				String summary = odiname + "【" + tmp + (StringUtils.isBlank(tsp.getRemark())? "":tsp.getRemark()) + "】";
				item.setSummary(summary);
				item.setOtherinfo(JsonUtils.addJsonKeyValue(item.getOtherinfo(), BuyItemConstant.OTHERINFO_KEY_SEATLABEL, StringUtils.join(seatList, ",")));
				itemList.add(item);
			}
		}
		setOrderDescription(order, odi, seatArea, disMsg);
		Map<String, String> descMap = VmUtils.readJsonToMap(order.getDescription2());
		descMap.put("座位", DramaOrderHelper.getSeatText3(sellSeatMap.values()));
		order.setDescription2(JsonUtils.writeMapToJson(descMap));
		return ErrorCode.getSuccessReturn(itemList);
	}
	
	@Override
	public Map<String, String> getOtherInfoMap(List<OpenDramaItem> itemList){
		Map<String, String>	otherInfoMap = new HashMap<String, String>();
		for (OpenDramaItem item : itemList) {
			otherInfoMap.putAll(JsonUtils.readJsonToMap(item.getOtherinfo()));
		}
		return otherInfoMap;
	}
	
	@Override
	public ErrorCode validLoveSeat(List<OpenTheatreSeat> seatList){
		Map<String, String> seatMap = new HashMap<String, String>();
		for(OpenTheatreSeat seat: seatList){
			if(!seat.getLoveInd().equals("0")){
				seatMap.put(seat.getLineno()+","+seat.getRankno(), seat.getLoveInd());
			}
		}
		if(seatMap.isEmpty()) return ErrorCode.SUCCESS;
		List<String> keyList = new ArrayList<String>(seatMap.keySet());
		String tmpInd = null;
		for(String key: keyList){
			String v = seatMap.get(key);
			String[] r = key.split(",");
			if(v.equals("1")){
				tmpInd = seatMap.get(r[0] + "," + (Integer.parseInt(r[1])+1));
			}else{
				tmpInd = seatMap.get(r[0] + "," + (Integer.parseInt(r[1])-1));
			}
			if(tmpInd == null) return ErrorCode.getFailure("情侣座不能单卖！");
		}
		return ErrorCode.SUCCESS;
	}	
	
	@Transactional(propagation=Propagation.NESTED)
	private List<SellDramaSeat> createSellDramaSeat(List<OpenTheatreSeat> oseatList){
		List<SellDramaSeat> result = new ArrayList<SellDramaSeat>();
		Timestamp validtime = new Timestamp(System.currentTimeMillis()-1000);
		for(OpenTheatreSeat oseat:oseatList){
			SellDramaSeat sellSeat = baseDao.getObject(SellDramaSeat.class, oseat.getId());
			if(sellSeat == null){ 
				sellSeat = new SellDramaSeat(oseat, validtime);
				baseDao.addObject(sellSeat);
			}
			result.add(sellSeat);
		}
		return result;
	}
	@Override
	public List<SellDramaSeat> getDramaOrderSeatList(Long orderId){
		String query = "from SellDramaSeat where id in (select t.seatid from DramaOrder2Seat t where t.orderid = ?) ";
		List<SellDramaSeat> seatList = hibernateTemplate.find(query, orderId);
		return seatList;
	}
	@Override
	public List<SellDramaSeat> getSellDramaSeatList(Long dpid, Long areaid) {
		String query = "from SellDramaSeat s where s.dpid = ? and s.areaid=? ";
		List<SellDramaSeat> result = hibernateTemplate.find(query, dpid, areaid);
		return result;
	}
	@Override
	public ErrorCode validateSeatLock(List<OpenTheatreSeat> seatList, Map<Long, SellDramaSeat> sellSeatMap, List<String> remoteLockList) {
		String msg = "";
		for(OpenTheatreSeat oseat : seatList){
			SellDramaSeat seat = sellSeatMap.get(oseat.getId());
			if(seat!=null && !seat.isAvailable() || oseat.isLocked() || remoteLockList.contains(oseat.getKey())) msg = "[" + seat.getSeatLabel() + "]";
		}
		if(StringUtils.isBlank(msg)) return ErrorCode.SUCCESS;
		return ErrorCode.getFailure(msg+"被占用");
	}
	private final ErrorCode validateSeatPosition(TheatreSeatArea seatArea, List<OpenTheatreSeat> selectedSeat, List<String> remoteLockList) {
		//1)座位要都在同一场次
		for(OpenTheatreSeat oseat:selectedSeat){
			if(!oseat.getAreaid().equals(seatArea.getId())) return ErrorCode.getFailure("不能跨场区选择座位！");
		}
		//按行号将现有的座位分组
		Map<Integer, List<OpenTheatreSeat>> lineMap = BeanUtil.groupBeanList(selectedSeat, "lineno");
		//判断每行是否合法
		ErrorCode code = ErrorCode.getFailure("选座时，请尽量选连在一起的座位，不要留下单个的空闲座位；");
		for(Integer lineno: lineMap.keySet()){
			int seatnum = lineMap.get(lineno).size();//在此行买的座位数量
			//1、场区里的一行座位状态图
			List<OpenTheatreSeat> lineseatList = getLineSeatListByAreaid(seatArea.getId(), lineno);
			int[] nowrow = new int[getMaxRank(lineseatList)+2];//0:表示座位不可用,下标0和最后一个作为哨兵元素
			for(OpenTheatreSeat oseat:lineseatList){
				SellDramaSeat seat = baseDao.getObject(SellDramaSeat.class, oseat.getId());
				if((seat==null || seat.isAvailable()) && !oseat.isLocked() && !remoteLockList.contains(oseat.getKey())){ 
					nowrow[oseat.getRankno()-seatArea.getFirstrank()+1]=1;
				}
			}
			//2、找到现有的孤点座位个数并统计总共剩余的座位数
			int now = getIsolatedSeat(nowrow);
			//3、找到填充后的孤点座位个数并统计总共剩余的座位数
			int[] laterrow = Arrays.copyOf(nowrow, nowrow.length);
			for(OpenTheatreSeat oseat:lineMap.get(lineno)) laterrow[oseat.getRankno()-seatArea.getFirstrank()+1] = 0;
			int later = getIsolatedSeat(laterrow);
			//4、对比判断结果：
			if(now >= later) continue; //1)孤点减少或不变，继续下一行
			//2)产生了孤点，判断是否有其他合适的座位选法
			List<Integer> seatnumList = getMaxBlankSeatnumList(nowrow);
			Collections.sort(seatnumList);
			//a)如果<=2个座位，则有合适的不允许产生孤点
			if(seatnum==1){//1个座位
				if(seatnumList.get(seatnumList.size()-1)>=3) return code;
			}else if(seatnum==2){
				if(seatnumList.get(seatnumList.size()-1)>=4) return code;
				if(seatnumList.contains(2)) return code;
			}else if(seatnum>=3){
				if(seatnumList.get(seatnumList.size()-1)>= seatnum+2) return code;
				if(seatnumList.contains(3)) return code;
				if(later > now + 1) return code;//只能产生一个空挡
			}
		}
		return ErrorCode.SUCCESS;
	}
	@Override
	public  ErrorCode<DramaOrder> useElecCard(DramaOrder order, ElecCard card, Long memberid){
		if(!order.isNew()) return ErrorCode.getFailure("订单状态错误（" + order.getStatusText() + "）！");
		if(!card.getEbatch().getTag().equals("drama")) return ErrorCode.getFailure("本券不能在演出板块使用！");
		OpenDramaItem item = baseDao.getObjectByUkey(OpenDramaItem.class, "dpid", order.getDpid(), false);
		List<BuyItem> buyList = baseDao.getObjectListByField(BuyItem.class, "orderid", order.getId());
		List<OpenDramaItem> itemList = getOpenDramaItemList(item, buyList);
		ErrorCode<Discount> code = getDiscount(order, itemList, buyList, card, memberid);
		if(!code.isSuccess()) return ErrorCode.getFailure(code.getMsg());
		Discount discount = code.getRetval();
		List<Discount> discountList = paymentService.getOrderDiscountList(order);
		baseDao.saveObject(discount);
		GewaOrderHelper.useDiscount(order, discountList, discount);
		baseDao.saveObject(order);
		return ErrorCode.getSuccessReturn(order);
	}
	
	private ErrorCode<List<OpenDramaItem>> checkOpenDramaItemList(ElecCard card, List<OpenDramaItem> itemList){
		ErrorCode code = ErrorCodeConstant.DATEERROR;
		List<OpenDramaItem> newItemList = new ArrayList<OpenDramaItem>();
		for (OpenDramaItem item : itemList) {
			code = checkOpenDramaItem(card, item);
			if(code.isSuccess()){
				newItemList.add(item);
			}
		}
		if(newItemList.isEmpty()) return code;
		return ErrorCode.getSuccessReturn(newItemList);
	}
	private ErrorCode checkOpenDramaItem(ElecCard card, OpenDramaItem item){
		if(!StringUtils.contains(item.getElecard(), card.getCardtype())){
			return ErrorCode.getFailure("此兑换券不可在本场次使用");
		}
		if(StringUtils.equals(item.getPeriod(), Status.Y)){
			if(StringUtils.isNotBlank(card.getWeektype())){
				String week = ""+DateUtil.getWeek(item.getPlaytime());
				if(card.getWeektype().indexOf(week) < 0){ 
					return ErrorCode.getFailure("此兑换券只能在周" + card.getWeektype() + "使用！");
				}
			}
			
			//限制场次时间段
			if(StringUtils.isNotBlank(card.getEbatch().getOpentime()) && StringUtils.isNotBlank(card.getEbatch().getClosetime())){
				String playtime = DateUtil.format(item.getPlaytime(), "HHmm");
				String opentime = card.getEbatch().getOpentime();
				String closetime = card.getEbatch().getClosetime();
				if(playtime.compareTo(opentime)<0 || playtime.compareTo(closetime)>0)
					return ErrorCode.getFailure("该券限制场次只能在" + opentime + "至" + closetime + "时段内使用！");
			}
		}
		return ErrorCode.SUCCESS;
	}
	
	private ErrorCode getDiscount(DramaOrder order, List<OpenDramaItem> itemList, List<BuyItem> buyList, ElecCard card, Long memberid){
		//1、判断卡是否有效
		if(card.needActivation()){
			Map jsonMap = new HashMap();
			jsonMap.put("activation", "true");
			jsonMap.put("msg", card.getCardno());
			return ErrorCode.getFullErrorCode(ApiConstant.CODE_SIGN_ERROR, "票券必须激活才能使用", jsonMap);
		}
		if(!card.available()) return ErrorCode.getFailure("此兑换券已经用完或失效！");
		if(!card.validTag(PayConstant.APPLY_TAG_DRAMA)) return ErrorCode.getFailure("此卡不能在演出版块使用");
		if(card.getPossessor()!=null && !card.getPossessor().equals(memberid)){
			return ErrorCode.getFailure("不能用别人的兑换券！");
		}
		
		if(StringUtils.isNotBlank(card.getValidcinema())){
			List<Long> cidList = BeanUtil.getIdList(card.getValidcinema(), ",");
			if(!cidList.contains(order.getTheatreid())){
				return ErrorCode.getFailure("此兑换券不能在此演出院使用！");
			}
		}
		
		if(StringUtils.isNotBlank(card.getValidmovie())){
			List<Long> cidList = BeanUtil.getIdList(card.getValidmovie(), ",");
			if(!cidList.contains(order.getDramaid())){
				return ErrorCode.getFailure("此演出不能使用此兑换券！");
			}
		}
		if(StringUtils.isNotBlank(card.getValiditem())){
			List<Long> cidList = BeanUtil.getIdList(card.getValiditem(), ",");
			if(!cidList.contains(order.getDpid())){
				return ErrorCode.getFailure("本场次不能使用此兑换券！");
			}
		}
		if(StringUtils.isNotBlank(card.getValidprice())){
			List<Long> priceidList = BeanUtil.getBeanPropertyList(buyList, "smallitemid", true);
			List<Long> pidList = BeanUtil.getIdList(card.getValidprice(), ",");
			if(!pidList.containsAll(priceidList)){
				return ErrorCode.getFailure("该价格不能使用兑换券！");
			}
		}
		//删除不可用的
		ErrorCode<List<OpenDramaItem>> code = checkOpenDramaItemList(card, itemList);
		if(!code.isSuccess()) return ErrorCode.getFailure("没有可兑换的场次！");
		Long batchid = card.getEbatch().getId();
		Map<String, String> otherInfo = getOtherInfoMap(code.getRetval());	
		boolean isSupportCard = new PayValidHelper(otherInfo).supportCard(batchid);
		if(!isSupportCard) return ErrorCode.getFailure("该场次不支持该券的使用！");
		Map<Long, OpenDramaItem> odiMap = BeanUtil.beanListToMap(code.getRetval(), "dpid");
		List<Discount> discountList = paymentService.getOrderDiscountList(order);
		if("D".equals(card.getCardtype()) && discountList.size() > 0){
			return ErrorCode.getFailure("此类券不能重复使用或与其他优惠方式共用！");
		}
		for(Discount discount: discountList){
			if(discount.getRelatedid().equals(card.getId()))
				return ErrorCode.getFailure("此兑换券已使用！");
			if("ABC".contains(card.getCardtype()) && !card.getCardtype().equals(discount.getCardtype())){
				return ErrorCode.getFailure("此兑换券不能与其他优惠方式共用！");
			}
		}
		int amount = 0; Long goodsid = null;
		String description = "";
		String cardtype = card.getEbatch().getCardtype();
		if(cardtype.equals(PayConstant.CARDTYPE_C) ||
			card.getEbatch().getCardtype().equals(PayConstant.CARDTYPE_D)){
			amount = card.getEbatch().getAmount();
			description = card.getCardno() + "抵用" + amount + "元";
		}else if(cardtype.equals(PayConstant.CARDTYPE_A ) || cardtype.equals(PayConstant.CARDTYPE_B)){
			int quantity = 0;
			Map<Long, Map<String, String>> itemOtherInfoMap = new HashMap<Long, Map<String,String>>();
			Map<Long,List<Discount>> disMap = BeanUtil.groupBeanList(discountList, "goodsid");
			//标记是否选座
			boolean flag = false;
			for (Iterator<BuyItem> iterator =buyList.iterator();iterator.hasNext();) {
				BuyItem item = iterator.next();
				OpenDramaItem odi = odiMap.get(item.getRelatedid());
				if(odi == null){
					//条件不满足的删除
					iterator.remove();
					continue;
				}
				if(odi.isOpenseat()){
					List<SellDramaSeat> seatList = getDramaOrderSeatList(order.getId());
					SellDramaSeat seat = DramaOrderHelper.getMaxSellSeat(seatList,discountList);
					if(seat==null) return ErrorCode.getFailure("已经没有场地可以使用兑换券！");
					goodsid = seat.getId();
					description = card.getCardno() + "抵用" + seat.getSeatLabel();
					amount = seat.getPrice();
					if(cardtype.equals(PayConstant.CARDTYPE_B)){
						if(amount > card.getEbatch().getAmount()){
							amount = card.getEbatch().getAmount();
						}
					}
					flag = true;
				}else{
					Map<String, String> otherInfoMap = JsonUtils.readJsonToMap(item.getOtherinfo());
					if(item.getDisid() != null){
						itemOtherInfoMap.put(item.getId(), otherInfoMap);
						String disQuantity = otherInfoMap.get(BuyItemConstant.OTHERINFO_KEY_DISQUANTITY);
						if(StringUtils.isNotBlank(disQuantity)){
							int tmpQuantity = Integer.parseInt(disQuantity);
							quantity += tmpQuantity;
							List<Discount> disList = disMap.get(item.getDisid());
							if(disList != null && disList.size()>= tmpQuantity){
								//已经兑换过的删除
								iterator.remove();
							}
						}
					}else{
						quantity += item.getQuantity();
						List<Discount> disList = disMap.get(item.getSmallitemid());
						if(disList != null && disList.size()>= item.getQuantity()){
							//已经兑换过的删除
							iterator.remove();
						}
					}
				}
			}
			//非选座
			if(!flag){
				if(itemList.isEmpty() || discountList.size()>= quantity){
					return ErrorCode.getFailure("已经没有票可以使用兑换券！");
				}
				
				BuyItem item = buyList.get(0);
				TheatreSeatPrice seatPrice = baseDao.getObject(TheatreSeatPrice.class, item.getSmallitemid());
				if(seatPrice == null) return ErrorCode.getFailure("兑换出错！");
				if(item.getDisid() != null){
					amount = Integer.parseInt(JsonUtils.getJsonValueByKey(item.getOtherinfo(), BuyItemConstant.OTHERINFO_KEY_DISPRICE));
					goodsid = item.getDisid();
				}else{			
					amount = seatPrice.getPrice();
					goodsid = seatPrice.getId();
				}
				if(cardtype.equals(PayConstant.CARDTYPE_B)){
					if(amount > card.getEbatch().getAmount()){
						amount = card.getEbatch().getAmount();
					}
				}
				description = card.getCardno() + "抵用" +amount;
			}
		}else{
			return ErrorCode.getFailure("此种券不能使用！");
		}
		if(amount <= 0) return ErrorCode.getFailure("使用此兑换券得不到任何优惠，请看使用说明！");
		Discount discount = new Discount(order.getId(), PayConstant.DISCOUNT_TAG_ECARD, card.getId(), card.getCardtype());
		discount.setDescription(description);
		discount.setGoodsid(goodsid);
		discount.setBatchid(card.getEbatch().getId());
		discount.setAmount(amount);
		return ErrorCode.getSuccessReturn(discount);
	}
	@Override
	public List<DramaOrder> getDramaOrderByTheatreid(Long theatreid, int from, int maxnum){
		DetachedCriteria query=DetachedCriteria.forClass(DramaOrder.class);
		query.add(Restrictions.eq("status", OrderConstant.STATUS_PAID_SUCCESS));
		List<DramaOrder> dramaOrderList = hibernateTemplate.findByCriteria(query, from, maxnum);
		return dramaOrderList;
	}
	
	@Override
	public OrderContainer processOrderPay(DramaOrder order) throws OrderException {
		return processOrderPayInternal(order);
	}
	@Override
	public ErrorCode processDramaOrder(DramaOrder order, OpenDramaItem odi) {
		if(order.isPaidUnfix()){
			Timestamp cur = new Timestamp(System.currentTimeMillis());
			order.setUpdatetime(cur);
			order.setModifytime(cur);
			order.setValidtime(DateUtil.addDay(cur, 180));
			order.setStatus(OrderConstant.STATUS_PAID_SUCCESS);
			order.setSettle(OrderConstant.SETTLE_Y);
			baseDao.saveObject(order);
			OrderExtra orderExtra = processOrderExtra(order);
			Drama drama = baseDao.getObject(Drama.class, order.getDramaid());
			orderExtra.setPretype(drama.getPretype());
			baseDao.saveObject(orderExtra);
			if(odi.isOpenseat()) {
				List<SellDramaSeat> seatList = getDramaOrderSeatList(order.getId());
				for(SellDramaSeat seat: seatList){
					if(!seat.isAvailableBy(order.getId())) return ErrorCode.getFailure(ApiConstant.CODE_SEAT_OCCUPIED, "座位已经被别人占用！");
					seat.setStatus(TheatreSeatConstant.STATUS_SOLD);
					seat.setValidtime(order.getValidtime());
					baseDao.saveObject(seat);
				}
				createOrderNoteBySeat(order, odi, seatList);
			}else{
				Map<TheatreSeatPrice, Integer> priceQuantityMap = new Hashtable<TheatreSeatPrice, Integer>();
				Map<Long, Map<DisQuantity, Integer>> priceDisMap = new Hashtable<Long, Map<DisQuantity,Integer>>();
				Map<OpenDramaItem, Integer> odiMap = new Hashtable<OpenDramaItem, Integer>();
				getPriceObjectList(order, odi, odiMap, priceQuantityMap, priceDisMap);
				List result = TspHelper.updateTheatrePriceSellCounter(priceQuantityMap, priceDisMap);
				baseDao.saveObjectList(result);
				createOrderNote(order, odiMap);
				List<TspSaleCount> tscList = baseDao.getObjectListByField(TspSaleCount.class, "orderid", order.getId());
				baseDao.removeObjectList(tscList);
			}
			return ErrorCode.SUCCESS;
		}else{
			return ErrorCode.getFailure(ApiConstant.CODE_DATA_ERROR, "订单状态不正确！");
		}
	}
	@Override
	public void getPriceObjectList(DramaOrder order, OpenDramaItem odi, final Map<OpenDramaItem, Integer> odiMap, final Map<TheatreSeatPrice, Integer> priceQuantityMap, final Map<Long, Map<DisQuantity, Integer>> priceDisMap){
		if(!odi.isOpenprice()) return;
		Map<Long, TheatreSeatPrice> priceMap = new Hashtable<Long, TheatreSeatPrice>();
		List<BuyItem> itemList = baseDao.getObjectListByField(BuyItem.class, "orderid", order.getId());
		for (BuyItem item : itemList) {
			OpenDramaItem odi2 = baseDao.getObjectByUkey(OpenDramaItem.class, "dpid",item.getRelatedid());
			Integer quantity = odiMap.get(odi2);
			if(quantity == null){
				quantity = item.getQuantity();
			}else{
				quantity += item.getQuantity();
			}
			odiMap.put(odi2, quantity);
			TheatreSeatPrice seatPrice = priceMap.get(item.getSmallitemid());
			if(seatPrice == null){
				seatPrice = baseDao.getObject(TheatreSeatPrice.class, item.getSmallitemid());
			}
			if(seatPrice == null) {
				continue;
			}
			priceMap.put(seatPrice.getId(), seatPrice);
			Integer tmpQuantity = priceQuantityMap.get(seatPrice);
			if(tmpQuantity == null){
				tmpQuantity = item.getQuantity();
			}else{
				tmpQuantity += item.getQuantity();
			}
			priceQuantityMap.put(seatPrice, tmpQuantity);
		}
	}
	
	@Override
	public String getTakemethodByOdi(DramaOrder order, OpenDramaItem odi){
		return getTakemethodByOdi(order.getAddtime(), odi);
	}
	@Override
	public String getTakemethodByOdi(Timestamp addtime, OpenDramaItem odi){
		String takemethod = "";
		if(StringUtils.equals(odi.getTakemethod(), TheatreProfile.TAKEMETHOD_E) 
			|| StringUtils.equals(odi.getTakemethod(), TheatreProfile.TAKEMETHOD_A)){
			takemethod = odi.getTakemethod();
		}else{
			int week = DateUtil.getWeek(addtime);
			String hour = DateUtil.format(addtime, "HH:mm");
			Timestamp endTakeTime = DateUtil.addHour(addtime, odi.getEticketHour());
			//判断是否到周末了
			if(week==5 && hour.compareTo("12:00")>0 || week>5){
				endTakeTime = DateUtil.addHour(addtime, odi.getEticketWeekHour());
			}
			Timestamp playdate = DateUtil.getBeginningTimeOfDay(odi.getPlaytime());
			if(endTakeTime.after(playdate) ){
				takemethod = TheatreProfile.TAKEMETHOD_A;
			}else{
				takemethod = odi.getTakemethod();
			}
		}
		return takemethod;
	}
	private void setOrderDescription(DramaOrder order, OpenDramaItem odi, TheatreSeatArea seatArea, String disMsg){
		String ordertitle = odi.getTheatrename()+"演出票";
		order.setOrdertitle(ordertitle);
		TheatreField section = baseDao.getObject(TheatreField.class, odi.getRoomid());
		Map<String, String> descMap = VmUtils.readJsonToMap(order.getDescription2());
		descMap.put("场馆", odi.getTheatrename());
		descMap.put("项目", odi.getDramaname());
		descMap.put("场地", section.getName());
		descMap.put("场区", seatArea.getAreaname());
		descMap.put("时间", DateUtil.format(odi.getPlaytime(),"yyyy-MM-dd HH:mm"));
		if(disMsg != null){
			descMap.put("套票", disMsg);
		}
		order.setDescription2(JsonUtils.writeMapToJson(descMap));
	}
	
	private List<OrderNote> createOrderNote(DramaOrder order,Map<OpenDramaItem, Integer> odiMap){
		List<OrderNote> orderNoteList = new ArrayList<OrderNote>();
		int i=1;
		List<BuyItem> buyItemList = baseDao.getObjectListByField(BuyItem.class, "orderid", order.getId());
		Map<Long, List<BuyItem>> itemMap = BeanUtil.groupBeanList(buyItemList, "relatedid");
		for (OpenDramaItem odi : odiMap.keySet()) {
			OrderNote orderNote = new OrderNote(order);
			orderNote.setTicketnum(odiMap.get(odi));
			orderNote.setPlacetype(TagConstant.TAG_THEATRE);
			orderNote.setPlaceid(odi.getTheatreid());
			orderNote.setPlacename(odi.getTheatrename());
			orderNote.setItemtype(TagConstant.TAG_DRAMA);
			orderNote.setItemid(odi.getDramaid());
			orderNote.setItemname(odi.getDramaname());
			orderNote.setSmallitemid(odi.getDpid());
			orderNote.setSmallitemtype(BuyItemConstant.TAG_DRAMAPLAYITEM);
			String checkpass = nextRandomNum(DateUtil.addDay(odi.getEndtime(), 1), 8, "0");
			String serialno = order.getTradeNo() + "0" + i;
			orderNote.setSerialno(serialno);
			orderNote.setCheckpass(checkpass);
			Map<String, String> downmap = getDownOrderMapByPrice(order, orderNote, odi, buyItemList);
			String result = JsonUtils.writeMapToJson(downmap);
			orderNote.setDescription(result);
			orderNoteList.add(orderNote);
			String takemethod = getTakemethodByOdi(order, odi);
			if(StringUtils.equals(order.getExpress(), Status.Y) && StringUtils.equals(takemethod, TheatreProfile.TAKEMETHOD_A) ){
				orderNote.setExpress(Status.N);
			}
			i++;
			List<BuyItem> itemList = itemMap.get(odi.getDpid());
			if(!CollectionUtils.isEmpty(itemList)){
				for (BuyItem buyItem : itemList) {
					buyItem.setExpress(orderNote.getExpress());
				}
			}
		}
		baseDao.saveObjectList(buyItemList);
		baseDao.saveObjectList(orderNoteList);
		return orderNoteList;
	}
	
	private void createOrderNoteBySeat(DramaOrder order, OpenDramaItem odi, List<SellDramaSeat> seatList){
		OrderNote orderNote = new OrderNote(order);
		orderNote.setTicketnum(order.getQuantity());
		orderNote.setPlacetype(TagConstant.TAG_THEATRE);
		orderNote.setPlaceid(order.getTheatreid());
		orderNote.setPlacename(odi.getTheatrename());
		orderNote.setItemtype(TagConstant.TAG_DRAMA);
		orderNote.setItemid(order.getDramaid());
		orderNote.setItemname(odi.getDramaname());
		orderNote.setSmallitemid(odi.getDpid());
		orderNote.setSmallitemtype(BuyItemConstant.TAG_DRAMAPLAYITEM);
		orderNote.setCheckpass(order.getCheckpass());
		orderNote.setSerialno(order.getTradeNo());
		orderNote.setValidtime(odi.getPlaytime());
		Map<String, String> map = getDownOrderMapBySeat(order, orderNote, odi, seatList);
		String result = JsonUtils.writeMapToJson(map);
		orderNote.setDescription(result);
		String takemethod = getTakemethodByOdi(order, odi);
		if(StringUtils.equals(order.getExpress(), Status.Y) && StringUtils.equals(takemethod, TheatreProfile.TAKEMETHOD_A) ){
			orderNote.setExpress(Status.N);
		}
		baseDao.saveObject(orderNote);
	}
	private Map<String, String> getDownOrderMapBySeat(DramaOrder order, OrderNote orderNote, OpenDramaItem odi, List<SellDramaSeat> seatList){
		Map<String, String> map = DownOrderHelper.downDramaOrder(order, orderNote, odi);
		String seatText = DramaOrderHelper.getDramaOrderSeatText(seatList);
		map.put("seatprice", seatText);
		return map;
	}
	
	private Map<String, String> getDownOrderMapByPrice(DramaOrder order, OrderNote orderNote, OpenDramaItem odi, List<BuyItem> buyItemList){
		Map<String, String> map = DownOrderHelper.downDramaOrder(order, orderNote, odi);
		String seatprice = "";
		String pricetype = "";
		for(BuyItem buyItem : buyItemList){
			if(buyItem.getRelatedid().equals(odi.getDpid())){
				for(int i =1; i<=buyItem.getQuantity();i++){
					seatprice +="," + buyItem.getUnitprice();
					pricetype +="," + (buyItem.getDisid()!=null?"Y":"N");
				}
			}
		}
		map.put("seatprice", seatprice.substring(1));
		map.put("pricetype", pricetype.substring(1));
		return map;
	}
	
	private final List<OpenTheatreSeat> getLineSeatListByAreaid(Long areaid, int lineno) {
		String hql = "from OpenTheatreSeat s where s.areaid=? and s.lineno=?";
		List<OpenTheatreSeat> result = hibernateTemplate.find(hql, areaid, lineno);
		return result;
	}
	private final int getMaxRank(List<OpenTheatreSeat> oseatList){
		int max = 0;
		for(OpenTheatreSeat oseat:oseatList) max=Math.max(max, oseat.getRankno());
		return max;
	}
	private final int getIsolatedSeat(int[] seatrow){
		int isolatedNum = 0;
		for(int i=0, length = seatrow.length -1;i<length;i++){
			if(seatrow[i]==1){//没被占用
				if(seatrow[i-1]==0 && seatrow[i+1]==0) isolatedNum++;
			}
		}
		return isolatedNum;
	}
	/**
	 * 连续空白座的座位为一组，找到所有组的个数
	 * @param seatrow
	 * @return
	 */
	private final List<Integer> getMaxBlankSeatnumList(int[] seatrow){
		List<Integer> result = new ArrayList<Integer>();
		for(int i=0,length = seatrow.length -1;i<length;){
			if(seatrow[i]==0) i++;
			else{
				int num=0;
				while(seatrow[i]==1 && i < length){
					num++;i++;
				}
				result.add(num);
			}
		}
		return result;
	}
	@Override
	public ErrorCode<OrderContainer> useSpecialDiscount(Long orderId, SpecialDiscount sd, OrderCallback callback){
		DramaOrder order = baseDao.getObject(DramaOrder.class, orderId);
		OpenDramaItem item = baseDao.getObjectByUkey(OpenDramaItem.class, "dpid", order.getDpid(), false);
		List<Discount> discountList = paymentService.getOrderDiscountList(order);
		Spcounter spcounter = paymentService.getSpdiscountCounter(sd);
		ErrorCode<Discount> discount = getSpdiscount(spcounter, order, discountList, item, sd);
		if(discount.isSuccess()){
			paymentService.updateSpdiscountAddCount(sd, spcounter, order);
			baseDao.saveObject(discount.getRetval());
			GewaOrderHelper.useDiscount(order, discountList, discount.getRetval());
			if(StringUtils.isNotBlank(sd.getPaymethod())){
				String[] pay = StringUtils.split(sd.getPaymethod(), ":");
				order.setPaymethod(pay[0]);
				if(pay.length >1) order.setPaybank(pay[1]);
			}
			if(callback != null) callback.processOrder(sd, order);
			baseDao.saveObject(order);
			OrderContainer container = new DramaOrderContainer(order, item);
			container.setDiscountList(discountList);
			container.setCurUsedDiscount(discount.getRetval());
			container.setSpdiscount(sd);
			return ErrorCode.getSuccessReturn(container);
		}else{
			return ErrorCode.getFailure(discount.getMsg());
		}
	}
	private ErrorCode<Discount> getSpdiscount(Spcounter spcounter, DramaOrder order, List<Discount> discountList, OpenDramaItem item, SpecialDiscount sd) {
		if(!order.sureOutPartner()){
			if(StringUtils.equals(sd.getBindmobile(), Status.Y)){
				Member member = baseDao.getObject(Member.class, order.getMemberid());
				if(!member.isBindMobile()){
					return ErrorCode.getFailure("该活动必须绑定手机才能使用！");
				}
			}
		}
		List<SellDramaSeat> seatList = new ArrayList<SellDramaSeat>();
		List<BuyItem> buyList = baseDao.getObjectListByField(BuyItem.class, "orderid", order.getId());
		List<OpenDramaItem> itemList = getOpenDramaItemList(item, buyList);
		if(item.isOpenseat()) seatList = getDramaOrderSeatList(order.getId());
		SpecialDiscountHelper sdh = new DramaSpecialDiscountHelper(order, itemList, buyList, discountList, seatList);
		List<String> limitPayList = paymentService.getLimitPayList();
		Map<String, String> otherinfoMap = getOtherInfoMap(itemList);
		PayValidHelper valHelp = new PayValidHelper(otherinfoMap);
		valHelp.setLimitPay(limitPayList);
		ErrorCode<Integer> result = paymentService.getSpdiscountAmount(sdh, order, sd, spcounter, valHelp);
		if(!result.isSuccess()) return ErrorCode.getFailure(result.getMsg());
		Discount discount = new Discount(order.getId(), PayConstant.DISCOUNT_TAG_PARTNER, sd.getId(), PayConstant.CARDTYPE_PARTNER);
		discount.setAmount(result.getRetval());
		discount.setDescription(sd.getDescription());
		return ErrorCode.getSuccessReturn(discount);
	}
	
	
	@Override
	public List<OpenDramaItem> getOpenDramaItemList(OpenDramaItem odi, List<BuyItem> buyList){
		List<OpenDramaItem> odiList = new ArrayList<OpenDramaItem>();
		for (BuyItem item : buyList) {
			if(StringUtils.equals(item.getTag(), BuyItemConstant.TAG_DRAMAPLAYITEM)){
				OpenDramaItem item2 = baseDao.getObjectByUkey(OpenDramaItem.class, "dpid", item.getRelatedid());
				odiList.add(item2);
			}
		}
		if(!odiList.contains(odi)) odiList.add(odi);
		return odiList;
	}
	@Override
	public ErrorCode usePoint(Long orderId, Long memberId, int usePoint){
		ErrorCode<String> pcode = pointService.validUsePoint(memberId);
		if(!pcode.isSuccess()) return ErrorCode.getFailure(pcode.getMsg());
		DramaOrder order = baseDao.getObject(DramaOrder.class, orderId);
		MemberInfo info = baseDao.getObject(MemberInfo.class, memberId);
		if(info.getPointvalue() < usePoint) return ErrorCode.getFailure("您的积分不够！");
		int amount = 0;
		OpenDramaItem item = baseDao.getObjectByUkey(OpenDramaItem.class, "dpid", order.getDpid(), false);
		if(item.isOpenseat()){
			if(item.getMaxpoint() < usePoint) return ErrorCode.getFailure("您使用的积分超出上限" + item.getMaxpoint());
			amount = usePoint/ConfigConstant.POINT_RATIO;
			usePoint = amount * ConfigConstant.POINT_RATIO;
			if(usePoint < item.getMinpoint() || amount == 0){
				return ErrorCode.getFailure("您使用的积分少于下限" + item.getMinpoint());
			}
		}else{
			List<BuyItem> buyList = baseDao.getObjectListByField(BuyItem.class, "orderid", order.getId());
			List<OpenDramaItem> itemList = getOpenDramaItemList(item, buyList);
			boolean openPointPay = false;
			int minpoint = 0, maxpoint = 0;
			for (OpenDramaItem odi : itemList) {
				if(odi.isOpenPointPay()){
					openPointPay = true;
					maxpoint = Math.max(maxpoint, odi.getMaxpoint());
					minpoint = Math.max(minpoint, odi.getMinpoint());
				}
			}
			if(!openPointPay) return ErrorCode.getFailure("你选择的场次不支持积分优惠！");
			if(maxpoint < usePoint) return ErrorCode.getFailure("您使用的积分超出上限" + maxpoint);
			amount = usePoint/ConfigConstant.POINT_RATIO;
			usePoint = amount * ConfigConstant.POINT_RATIO;
			if(usePoint < minpoint || amount == 0){
				return ErrorCode.getFailure("您使用的积分少于下限" + minpoint);
			}
		}
		List<Discount> discountList = paymentService.getOrderDiscountList(order);
		for(Discount discount: discountList){
			if(discount.getTag().equals(PayConstant.DISCOUNT_TAG_POINT))
				return ErrorCode.getFailure("您已经使用过积分，如有改变，请先取消！");
			if(PayConstant.CARDTYPE_D.equals(discount.getCardtype())){
				return ErrorCode.getFailure("积分不能和优惠券一起使用！");
			}
			if(PayConstant.CARDTYPE_PARTNER.equals(discount.getCardtype())){
				return ErrorCode.getFailure("已经使用了其他优惠，不能同时使用积分！");
			}
		}
		
		Discount discount = new Discount(order.getId(), PayConstant.DISCOUNT_TAG_POINT, memberId, PayConstant.CARDTYPE_POINT);
		discount.setDescription(usePoint + "积分抵用" + amount + "元");
		discount.setAmount(amount);
		baseDao.saveObject(discount);
		GewaOrderHelper.useDiscount(order, discountList, discount);
		baseDao.saveObject(order);
		
		return ErrorCode.SUCCESS;
	}

	protected final List<String[]> parseSeat(String seatText){
		List<String[]> result = new ArrayList<String[]>();
		for(String seat:seatText.split(",")) result.add(seat.split(":"));
		return result;
	}
	protected final OpenTheatreSeat getOpenSeatByLoc(Long dpid, Long areaid, String seatline, String seatrank){
		String query = "from OpenTheatreSeat where dpid= ? and areaid=? and seatline = ? and seatrank = ? ";
		List<OpenTheatreSeat> result = hibernateTemplate.find(query, dpid, areaid, seatline, seatrank);
		if(result.isEmpty()) return null;
		return result.get(0);
	}
	@Override
	public List<OpenTheatreSeat> getNewSeatList(DramaOrder order, List<SellDramaSeat> seatList, String newseat, boolean validprice) throws OrderException{
		if(!order.isPaidFailure() && !order.isPaidUnfix()) throw new OrderException(ApiConstant.CODE_DATA_ERROR, "只能修改交易失败的订单！");
		String str = "0123456789,:ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
		if(StringUtils.isBlank(newseat) || !StringUtils.containsOnly(newseat, str)){
			throw new OrderException(ApiConstant.CODE_DATA_ERROR, "座位格式不正确：" + newseat);
		}
		OpenDramaItem odi = baseDao.getObjectByUkey(OpenDramaItem.class, "dpid", order.getDpid(), false);
		if(!odi.isOpenseat()){
			throw new OrderException(ApiConstant.CODE_DATA_ERROR, "非选座订单不能修改座位！");
		}
		List<Long> oldseatidList = BeanUtil.getBeanPropertyList(seatList, Long.class, "id", true);
		List<String[]> seatLocList = parseSeat(newseat);
		List<OpenTheatreSeat> newseatList = new ArrayList<OpenTheatreSeat>();
		for(String[] loc: seatLocList){
			OpenTheatreSeat oseat = getOpenSeatByLoc(odi.getDpid(), order.getAreaid(), loc[0], loc[1]);
			if(oseat != null) {
				SellDramaSeat sellSeat = baseDao.getObject(SellDramaSeat.class, oseat.getId());
				if((sellSeat==null || sellSeat.isAvailableBy(order.getId())) && !oseat.isLocked()){
					newseatList.add(oseat);
				}else{
					throw new OrderException(ApiConstant.CODE_DATA_ERROR, "座位不可用!");
				}
			}else{
				throw new OrderException(ApiConstant.CODE_DATA_ERROR, "座位不存在：" + loc);
			}
		}
		if(oldseatidList.size() != newseatList.size()) throw new OrderException(ApiConstant.CODE_DATA_ERROR, "座位数量不一致！");
		if(validprice){
			Collections.sort(seatList, new PropertyComparator("price", false, true));
			Collections.sort(newseatList, new PropertyComparator("price", false, true));
			String price1 = StringUtils.join(BeanUtil.getBeanPropertyList(seatList, Integer.class, "price", false), ",");
			String price2 = StringUtils.join(BeanUtil.getBeanPropertyList(newseatList, Integer.class, "price", false), ",");
			if(!price1.equals(price2)) throw new OrderException(ApiConstant.CODE_DATA_ERROR, "座位与原座位价格不一致！");
		}
		return newseatList;
	}
	
	@Override
	public ErrorCode<DramaOrder> processLastOrder(Long memberid, String ukey) {
		DramaOrder lastOrder = getLastDramaOrder(memberid, ukey);
		if(lastOrder==null) return ErrorCode.SUCCESS;
		if(lastOrder.getStatus().startsWith(OrderConstant.STATUS_PAID_FAILURE)){
			return ErrorCode.getFailure("您还有一个订单等待处理，订单号为" + lastOrder.getTradeNo() + "，请稍后再下新订单！");
		}
		return ErrorCode.getSuccessReturn(lastOrder);
	}
	private DramaOrder getLastDramaOrder(Long memberid, String ukey){
		LastOperation last = baseDao.getObject(LastOperation.class, "D" + memberid + StringUtil.md5(ukey));
		if(last==null) return null;
		DramaOrder order = baseDao.getObjectByUkey(DramaOrder.class, "tradeNo", last.getLastvalue());
		return order;
	}

	@Override
	public ErrorCode updateOrderExpress(String tradeNo, String expressNo,
			String expressStatus, User user, String dealType) {
		if(StringUtils.isBlank(expressStatus)){
			return ErrorCode.getFailure("更新状态不能为空！");
		}
		OrderExtra oe = baseDao.getObjectByUkey(OrderExtra.class, "tradeno", tradeNo);
		if (oe == null) {
			return ErrorCode.getFailure("订单:" + tradeNo + "不存在,请校验!");
		}
		ErrorCode code = this.checkExpressStatus(expressStatus, oe);
		if (!code.isSuccess()) {
			return code;
		}
		oe.setDealStatus(expressStatus);
		if (expressStatus.equals(OrderExtraConstant.EXPRESS_STATUS_TRANSIT) || expressStatus.equals(OrderExtraConstant.EXPRESS_STATUS_SIGNED)) {
			oe.setExpressStatus(expressStatus);
		}
		oe.setDealUser(user.getId());
		if (!StringUtils.isBlank(expressNo)) {
			oe.setExpressnote(expressNo);
		}
		baseDao.saveObject(oe);
		DealExpressOrder deo = new DealExpressOrder(oe.getId(), oe.getTradeno(), user.getNickname());
		deo.setDealtype(dealType);
		deo.setDealStatus(expressStatus);
		deo.setDealuser(user.getId());
		baseDao.saveObject(deo);
		return ErrorCode.SUCCESS;
	}

	@Override
	public ErrorCode<List<GewaOrder>> checkAndUpdateExpress(String expressNo, User user, String dealType) {
		String query = "from OrderExtra where expressnote= ? and ordertype=?";
		List<OrderExtra> oeList = hibernateTemplate.find(query, expressNo, TagConstant.TAG_DRAMA);
		if (oeList.size() == 0) {
			return ErrorCode.getFailure("快递单:" + expressNo + "不存在,请校验!");
		}
		final String expressStatus = OrderExtraConstant.EXPRESS_STATUS_TRANSIT;
		List<GewaOrder> mobileList = new ArrayList<GewaOrder>();
		for (OrderExtra oe : oeList) {
			ErrorCode code = this.checkExpressStatus(expressStatus, oe);
			if (!code.isSuccess()) {
				return code;
			}
			oe.setDealStatus(expressStatus);
			oe.setExpressStatus(expressStatus);
			oe.setDealUser(user.getId());
			baseDao.saveObject(oe);
			DealExpressOrder deo = new DealExpressOrder(oe.getId(), oe.getTradeno(), user.getNickname());
			deo.setDealtype(dealType);
			deo.setDealStatus(expressStatus);
			deo.setDealuser(user.getId());
			baseDao.saveObject(deo);
			GewaOrder order = baseDao.getObjectByUkey(GewaOrder.class, "tradeNo", oe.getTradeno());
			mobileList.add(order);
		}
		return ErrorCode.getSuccessReturn(mobileList);
	}
	
	private ErrorCode checkExpressStatus(String expressStatus, OrderExtra oe) {
		final String dealstatus = oe.getDealStatus();
		String previousStatus = OrderExtraConstant.EXPRESS_DEALSTATUS_STEP.get(expressStatus);
		if (!StringUtils.equals(previousStatus, dealstatus)) {
			return ErrorCode.getFailure("订单" + oe.getTradeno() + "快递流程错误！");
		}
		return ErrorCode.SUCCESS;
	}
	
}
