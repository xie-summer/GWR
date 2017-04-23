package com.gewara.service.order.impl;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
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
import org.springframework.stereotype.Service;

import com.gewara.command.GoodsCommand;
import com.gewara.command.SearchOrderCommand;
import com.gewara.constant.ApiConstant;
import com.gewara.constant.GoodsConstant;
import com.gewara.constant.PayConstant;
import com.gewara.constant.PaymethodConstant;
import com.gewara.constant.Status;
import com.gewara.constant.TagConstant;
import com.gewara.constant.order.BuyItemConstant;
import com.gewara.constant.sys.ConfigConstant;
import com.gewara.constant.sys.ErrorCodeConstant;
import com.gewara.constant.sys.MongoData;
import com.gewara.constant.ticket.OrderConstant;
import com.gewara.constant.ticket.PartnerConstant;
import com.gewara.helper.ActivityGoodsHelper;
import com.gewara.helper.DownOrderHelper;
import com.gewara.helper.GoodsPriceHelper;
import com.gewara.helper.discount.GoodsSpecialDiscountHelper;
import com.gewara.helper.discount.SpecialDiscountHelper;
import com.gewara.helper.discount.SportSpecialDiscountHelper.OrderCallback;
import com.gewara.helper.order.GewaOrderHelper;
import com.gewara.helper.order.GoodsOrderContainer;
import com.gewara.helper.order.OrderContainer;
import com.gewara.model.agency.TrainingGoods;
import com.gewara.model.api.ApiUser;
import com.gewara.model.drama.Theatre;
import com.gewara.model.goods.ActivityGoods;
import com.gewara.model.goods.BaseGoods;
import com.gewara.model.goods.Goods;
import com.gewara.model.goods.GoodsDisQuantity;
import com.gewara.model.goods.GoodsGift;
import com.gewara.model.goods.GoodsPrice;
import com.gewara.model.goods.SportGoods;
import com.gewara.model.goods.TicketGoods;
import com.gewara.model.movie.Cinema;
import com.gewara.model.pay.BuyItem;
import com.gewara.model.pay.Discount;
import com.gewara.model.pay.ElecCard;
import com.gewara.model.pay.ElecCardBatch;
import com.gewara.model.pay.GewaOrder;
import com.gewara.model.pay.GoodsOrder;
import com.gewara.model.pay.OrderAddress;
import com.gewara.model.pay.OrderNote;
import com.gewara.model.pay.Spcounter;
import com.gewara.model.pay.SpecialDiscount;
import com.gewara.model.pay.TicketOrder;
import com.gewara.model.sport.Sport;
import com.gewara.model.ticket.OpenPlayItem;
import com.gewara.model.user.Member;
import com.gewara.model.user.MemberInfo;
import com.gewara.mongo.MongoService;
import com.gewara.pay.PayUtil;
import com.gewara.pay.PayValidHelper;
import com.gewara.service.OperationService;
import com.gewara.service.OrderException;
import com.gewara.service.order.GoodsOrderService;
import com.gewara.support.ErrorCode;
import com.gewara.support.MultiPropertyComparator;
import com.gewara.untrans.RelateService;
import com.gewara.untrans.sport.SportUntransService;
import com.gewara.util.BeanUtil;
import com.gewara.util.DateUtil;
import com.gewara.util.JsonUtils;
import com.gewara.util.ObjectId;
import com.gewara.util.StringUtil;
import com.gewara.util.ValidateUtil;
import com.gewara.util.VmUtils;
@Service("goodsOrderService")
public class GoodsOrderServiceImpl extends GewaOrderServiceImpl implements GoodsOrderService {

	@Autowired@Qualifier("sportUntransService")
	private SportUntransService sportUntransService;
	@Autowired@Qualifier("relateService")
	private RelateService relateService;
	@Autowired@Qualifier("mongoService")
	private MongoService mongoService;
	@Override
	public GoodsOrder addGoodsOrder(Goods goods, Member member, String mobile, int quantity, String address, ApiUser partner) throws OrderException{
		if(goods.getMaxbuy() < quantity) throw new OrderException(ApiConstant.CODE_SIGN_ERROR, "每单最多购买:"+ goods.getMaxbuy());
		ErrorCode<GoodsOrder> code = addBaseGoodsOrder(goods, member, mobile, quantity, partner, null);
		if(!code.isSuccess()) throw new OrderException(code.getErrcode(), code.getMsg());
		GoodsOrder order = code.getRetval();
		Map<String, String> descMap = VmUtils.readJsonToMap(order.getDescription2());
		if(GoodsConstant.GOODS_TAG_POINT.equals(goods.getTag()) && goods.isNeedDeliver() && StringUtils.isNotBlank(address)) {
			order.setPricategory(OrderConstant.ORDER_PRICATEGORY_POINT);
			String[] addr = address.split(",", 3);
			descMap.put("contactor", addr[0]);
			descMap.put("postcode", addr[1]);
			descMap.put("address", addr[2]);
		}else if(GoodsConstant.GOODS_TAG_BMH.equals(goods.getTag())){
			order.setPricategory(OrderConstant.ORDER_PRICATEGORY_MOVIE);
			Cinema cinema = baseDao.getObject(Cinema.class, goods.getRelatedid());
			if(cinema!=null) order.setCitycode(cinema.getCitycode());
			if(StringUtils.isNotBlank(address)) descMap.put("address", address);
			if(cinema!=null) order.setPlaceid(cinema.getId());
		}else if(GoodsConstant.GOODS_TAG_BMH_SPORT.equals(goods.getTag()) || StringUtils.equals(GoodsConstant.GOODS_TYPE_SPORT, goods.getTag())){
			order.setPricategory(OrderConstant.ORDER_PRICATEGORY_SPORT);
			Sport sport = baseDao.getObject(Sport.class, goods.getRelatedid());
			if(sport != null) order.setCitycode(sport.getCitycode());
			order.setPlaceid(sport.getId());
		}else if(GoodsConstant.GOODS_TAG_BMH_THEATRE.equals(goods.getTag())){
			order.setPricategory(OrderConstant.ORDER_PRICATEGORY_DRAMA);
			Theatre theatre = baseDao.getObject(Theatre.class, goods.getRelatedid());
			if(theatre != null) order.setCitycode(theatre.getCitycode());
			order.setPlaceid(theatre.getId());
		}
		if(StringUtils.isBlank(order.getCitycode())){
			order.setCitycode(goods.getCitycode());
		}
		order.setDescription2(JsonUtils.writeMapToJson(descMap));
		baseDao.saveObject(order);
		return order;
	}
	
	@Override
	public GoodsOrder addCardDelayOrder(Goods goods, Member member, String mobile,ElecCard card) throws OrderException{
		if(!card.canDelay()){
			throw new OrderException(ApiConstant.CODE_SIGN_ERROR, "该卡号能进行有偿延期操作！");
		}
		if(goods.getMaxbuy() < 1) {
			throw new OrderException(ApiConstant.CODE_SIGN_ERROR, "每单最多购买:"+ goods.getMaxbuy());
		}
		ErrorCode<GoodsOrder> code = addBaseGoodsOrder(goods, member, mobile, 1, null, null);
		if(!code.isSuccess()) {
			throw new OrderException(code.getErrcode(), code.getMsg());
		}
		GoodsOrder order = code.getRetval();
		if(StringUtils.isBlank(order.getCitycode())){
			order.setCitycode(goods.getCitycode());
		}
		order.setPricategory(OrderConstant.ORDER_PRICATEGORY_MOVIE);
		Map tmpMap = VmUtils.readJsonToMap(order.getOtherinfo());
		tmpMap.put(OrderConstant.OTHERKEY_DELAY_CARDNO, card.getCardno());
		order.setOtherinfo(JsonUtils.writeMapToJson(tmpMap));
		order.setTotalfee(card.getEbatch().getDelayFee());
		order.setOtherfee(0);
		order.setItemfee(0);
		order.setDiscount(0);
		baseDao.saveObject(order);
		return order;
	}
	
	@Override
	public GoodsOrder addGoodsOrder(Goods goods, Member member, String mobile, int quantity, String address) throws OrderException{
		return addGoodsOrder(goods, member, mobile, quantity, address, null);
	}
	@Override
	public ErrorCode<GoodsOrder> addSportGoodsOrder(SportGoods goods, Member member, String mobile, int quantity, String address){
		if(goods.getMaxbuy() < quantity) return ErrorCode.getFailure(ApiConstant.CODE_SIGN_ERROR, "每单最多购买:"+ goods.getMaxbuy());
		ErrorCode<GoodsOrder> code = addBaseGoodsOrder(goods, member, mobile, quantity, null, null);
		if(!code.isSuccess()) return code;
		GoodsOrder order = code.getRetval();
		Sport sport = baseDao.getObject(Sport.class, goods.getRelatedid());
		if(sport != null) order.setCitycode(sport.getCitycode());
		if(StringUtils.isBlank(order.getCitycode())){
			order.setCitycode(goods.getCitycode());
		}
		if(StringUtils.isNotBlank(address)){
			Map<String, String> descMap = JsonUtils.readJsonToMap(order.getDescription2());
			descMap.put("address", goods.getGoodsname());
			order.setDescription2(JsonUtils.writeMapToJson(descMap));
		}
		order.setPricategory(OrderConstant.ORDER_PRICATEGORY_SPORT);
		order.setPlaceid(sport.getId());
		order.setValidtime(DateUtil.addMinute(order.getAddtime(), 15));
		baseDao.saveObject(order);
		return ErrorCode.getSuccessReturn(order);
	}
	
	@Override
	public ErrorCode<GoodsOrder> addTicketGoodsOrder(TicketGoods goods, Member member, String mobile, Integer quantity, Long disid, Long priceid, ApiUser partner, String ukey){
		if(priceid == null) return ErrorCode.getFailure(ApiConstant.CODE_SIGN_ERROR, "价格错误！");
		if(quantity == null || quantity < 1) return ErrorCode.getFailure(ApiConstant.CODE_SIGN_ERROR, "数量有错误！");
		if(!goods.hasBooking()) return ErrorCode.getFailure(ApiConstant.CODE_SIGN_ERROR, "场次已关闭购票！");
		List<GoodsCommand> commandList = new ArrayList<GoodsCommand>();
		GoodsCommand command = new GoodsCommand();
		command.setQuantity(quantity);
		command.setGoodsid(goods.getId());
		if(disid != null){
			command.setTag(GoodsConstant.CHECK_GOODS_DISCOUNT);
			command.setGspid(disid);
		}else{
			command.setTag(GoodsConstant.CHECK_GOODS_PRICE);
			command.setGspid(priceid);
		}
		commandList.add(command);
		return addTicketGoodsOrder(commandList, member, mobile, partner, ukey);
	}
	
	private ErrorCode<List<BuyItem>> getBuyItem(List<GoodsCommand> commandList){
		Map<Long, Integer> goodsQuantityMap = new HashMap<Long, Integer>();
		Map<Long/*gspid*/, Integer> priceQuantityMap = new HashMap<Long, Integer>();
		String paymethod = null;
		Long categoryid = null;
		List<BuyItem> itemList = new ArrayList<BuyItem>();
		Set<GoodsPrice> priceSet = new HashSet<GoodsPrice>();
		for (GoodsCommand command : commandList) {
			TicketGoods goods = null;
			GoodsPrice goodsPrice = null;
			GoodsDisQuantity discount = null;
			if(command.getGoodsid() == null || command.getGspid() == null 
					|| command.getQuantity() == null || command.getQuantity()<1 
					|| !GoodsConstant.CHECK_GOODSLIST.contains(command.getTag())){
				return ErrorCode.getFailure(ApiConstant.CODE_SIGN_ERROR, "场次或价格错误！");
			}
			int quantity = 0;
			goods = baseDao.getObject(TicketGoods.class, command.getGoodsid());
			if(goods == null || !goods.hasBooking()) return ErrorCode.getFailure(ApiConstant.CODE_SIGN_ERROR, goods.getGoodsname() + "场次已关闭购票！");
			if(categoryid == null){
				categoryid = goods.getCategoryid();
			}else if(!categoryid.equals(goods.getCategoryid())){
				return ErrorCode.getFailure(ApiConstant.CODE_SIGN_ERROR, "不能跨项目购票！");
			}
			String tmpMethod = JsonUtils.getJsonValueByKey(goods.getOtherinfo(), "defaultpaymethod");
			if(StringUtils.isNotBlank(tmpMethod)){
				if(StringUtils.isBlank(paymethod)){
					paymethod = tmpMethod;
				}else if(!StringUtils.equals(paymethod, tmpMethod)){
					return ErrorCode.getFailure(ApiConstant.CODE_SIGN_ERROR, "支付方式限制！");
				}
			}
			if(StringUtils.equals(command.getTag(), GoodsConstant.CHECK_GOODS_PRICE)){
				goodsPrice = baseDao.getObject(GoodsPrice.class, command.getGspid());
				quantity = command.getQuantity();
			}else if(StringUtils.equals(command.getTag(), GoodsConstant.CHECK_GOODS_DISCOUNT)){
				discount = baseDao.getObject(GoodsDisQuantity.class, command.getGspid());
				if(discount == null) return ErrorCode.getFailure(ApiConstant.CODE_SIGN_ERROR, "购买套票错误！");
				goodsPrice = baseDao.getObject(GoodsPrice.class, discount.getGspid());
				quantity = command.getQuantity() * discount.getQuantity();
			}
			if(goodsPrice == null) return ErrorCode.getFailure(ApiConstant.CODE_SIGN_ERROR, "购票价格错误！");
			if(!goodsPrice.hasBooking()) return ErrorCode.getFailure(ApiConstant.CODE_SIGN_ERROR, "购卖失败，价格库存不足！");
			Integer priceQuantity = priceQuantityMap.get(goodsPrice.getId());
			if(priceQuantity == null){
				priceQuantity = quantity;
			}else{
				priceQuantity += quantity;
			}
			Integer goodsQuantity = goodsQuantityMap.get(goods.getId());
			if(goodsQuantity == null){
				goodsQuantity = quantity;
			}else{
				goodsQuantity += quantity;
			}
			if(goods.getMaxbuy() < goodsQuantity) return ErrorCode.getFailure(ApiConstant.CODE_SIGN_ERROR, goods.getGoodsname()+"购票数不能大于"+goods.getMaxbuy());
			goodsQuantityMap.put(goods.getId(), goodsQuantity);
			priceSet.add(goodsPrice);
			priceQuantityMap.put(goodsPrice.getId(), priceQuantity);
			String msg = GoodsPriceHelper.getGoodsPriceDisabledReason(goodsPrice, discount, priceQuantity);
			if(StringUtils.isNotBlank(msg)) return ErrorCode.getFailure(ApiConstant.CODE_SIGN_ERROR, msg);
			BuyItem item = newBuyItem(goods, goodsPrice, discount, quantity); 
			String tmp = goodsPrice.getPrice() + "元" ;
			if(discount != null){
				tmp = discount.getPrice() + "(" + goodsPrice.getPrice() + " x " +discount.getQuantity()+")元";
			}
			String summary = goods.getGoodsname()+ "【" + tmp + (StringUtils.isBlank(goodsPrice.getRemark())? "":goodsPrice.getRemark()) + "】";
			item.setSummary(summary);
			itemList.add(item);
		}
		for (GoodsPrice price : priceSet) {
			int quantity = priceQuantityMap.get(price.getId());
			GoodsPriceHelper.updateGoodsPriceAddCounter(price, quantity);
		}
		baseDao.saveObjectList(priceSet);
		return ErrorCode.getSuccessReturn(itemList);
	}
	@Override
	public ErrorCode<GoodsOrder> addTicketGoodsOrder(List<GoodsCommand> commandList, Member member, String mobile, ApiUser partner, String ukey){
		if(CollectionUtils.isEmpty(commandList)){
			return ErrorCode.getFailure(ApiConstant.CODE_SIGN_ERROR, "场次或价格错误！");
		}
		Long memberid = null;
		if(member!=null){
			memberid = member.getId();
			ukey = String.valueOf(memberid);
		}else {
			memberid = partner.getId();
		}
		GoodsOrder lastPaidFailure = getLastPaidFailureOrder(memberid, ukey);
		if(lastPaidFailure != null) {
			return ErrorCode.getFailure(ApiConstant.CODE_SIGN_ERROR, "您还有一个订单等待处理，订单号为" + lastPaidFailure.getTradeNo() + "，请稍后再下新订单！");
		}
		ErrorCode<List<BuyItem>> codeItem = getBuyItem(commandList);
		if(!codeItem.isSuccess()) return ErrorCode.getFailure(codeItem.getErrcode(), codeItem.getMsg());
		List<BuyItem> itemList = codeItem.getRetval();
		List<GoodsOrder> orderList = paymentService.getUnpaidOrderList(GoodsOrder.class,memberid,ukey);
		if (!orderList.isEmpty()) cancelUnpaidOrderList(orderList);
		GoodsCommand command = commandList.get(0);
		TicketGoods goods = baseDao.getObject(TicketGoods.class, command.getGoodsid());
		ErrorCode<GoodsOrder> code = addBaseGoodsOrder(goods, member, mobile, command.getQuantity(), partner, ukey);
		if(!code.isSuccess()) return code;
		GoodsOrder order = code.getRetval();
		order.setPlaceid(goods.getRelatedid());
		order.setItemid(goods.getItemid());
		order.setPricategory(goods.getItemtype());
		baseDao.saveObject(order);
		int quantity = 0, totalcost = 0, totalfee = 0;
		Timestamp validtime = DateUtil.addMinute(order.getAddtime(), 30);
		for (BuyItem item : itemList) {
			item.setOrderid(order.getId());
			item.setMemberid(order.getMemberid());
			quantity += item.getQuantity();
			totalcost += item.getTotalcost();
			totalfee += item.getDue();
		}
		if(partner!=null){
			order.setPartnerid(partner.getId());
		}
		if(StringUtils.isBlank(order.getCitycode())){
			order.setCitycode(goods.getCitycode());
		}
		order.setTotalfee(totalfee);
		order.setTotalcost(totalcost);
		order.setUnitprice(totalfee/quantity);
		order.setCostprice(totalcost/quantity);
		order.setQuantity(quantity);
		order.setValidtime(validtime);
		baseDao.saveObject(order);
		baseDao.saveObjectList(itemList);
		return ErrorCode.getSuccessReturn(order);
	}
	
	@Override
	public String getExpressid(GoodsOrder order){
		BaseGoods goods = baseDao.getObject(BaseGoods.class, order.getGoodsid());
		String expressid = "";
		if(goods instanceof TicketGoods){
			List<BuyItem> itemList = baseDao.getObjectListByField(BuyItem.class, "orderid", order.getId());
			List<Long>	idList = BeanUtil.getBeanPropertyList(itemList, "relatedid", true);
			List<BaseGoods> goodsList = baseDao.getObjectList(BaseGoods.class, idList);
			Collections.sort(goodsList,new MultiPropertyComparator(new String[]{"fromvalidtime"}, new boolean[]{false}));
			for (BaseGoods baseGoods : goodsList) {
				if(StringUtils.isNotBlank(baseGoods.getExpressid())){
					expressid = baseGoods.getExpressid();
					break;
				}
			}
		}else{
			expressid = goods.getExpressid();
		}
		return expressid;
	}
	
	@Override
	public ErrorCode<GoodsOrder> addTrainingGoodsOrder(Long goodsId, Long gspId, Integer quantity, String mobile, String infoList, Member member, ApiUser partner, String ukey){
		Long memberid = null;
		if(member!=null){
			memberid = member.getId();
			ukey = String.valueOf(memberid);
		}else {
			memberid = partner.getId();
		}
		//查找支付成功但订单状态不正确
		GoodsOrder lastPaidFailure = getLastPaidFailureOrder(memberid, ukey);
		if(lastPaidFailure != null) {
			return ErrorCode.getFailure(ApiConstant.CODE_SIGN_ERROR, "您还有一个订单等待处理，订单号为" + lastPaidFailure.getTradeNo() + "，请稍后再下新订单！");
		}
		//查找未支付订单并取消
		List<GoodsOrder> orderList = paymentService.getUnpaidOrderList(GoodsOrder.class,memberid,ukey);
		if (!orderList.isEmpty()) cancelUnpaidOrderList(orderList);
		GoodsPrice goodsPrice = baseDao.getObject(GoodsPrice.class, gspId);
		if(goodsPrice == null) return ErrorCode.getFailure(ApiConstant.CODE_SIGN_ERROR, "购票价格错误！");
		if(!goodsPrice.hasBooking()) return ErrorCode.getFailure(ApiConstant.CODE_SIGN_ERROR, "购卖失败，价格库存不足！");
		//添加物品订单
		TrainingGoods goods = baseDao.getObject(TrainingGoods.class, goodsId);
		if(goods == null) return ErrorCode.getFailure(ApiConstant.CODE_SIGN_ERROR, "不存在此商品！");
		ErrorCode<GoodsOrder> code = addBaseGoodsOrder(goods, member, mobile, quantity, partner, ukey);
		if(!code.isSuccess()) return code;
		if(goods.getMaxbuy() < quantity) return ErrorCode.getFailure(ApiConstant.CODE_SIGN_ERROR, goods.getGoodsname()+"购票数不能大于"+goods.getMaxbuy());
		//判断物品库存是否充足
		String msg = GoodsPriceHelper.getTrainingGoodsPriceDisabledReason(goods, goodsPrice, quantity);
		if(StringUtils.isNotBlank(msg)) return ErrorCode.getFailure(ApiConstant.CODE_SIGN_ERROR, msg);
		GoodsOrder order = code.getRetval();
		order.setPlaceid(goods.getRelatedid());
		order.setItemid(goods.getItemid());
		order.setPricategory(TagConstant.TAG_SPORT);
		Timestamp validtime = DateUtil.addMinute(order.getAddtime(), 15);
		if(partner!=null){
			order.setPartnerid(partner.getId());
		}
		if(StringUtils.isBlank(order.getCitycode())){
			order.setCitycode(goods.getCitycode());
		}
		order.setQuantity(quantity);
		order.setUnitprice(goodsPrice.getPrice());
		order.setTotalfee(order.getOrderAmount());
		int costprice = 0;
		if(goodsPrice.getCostprice()!=null) costprice = goodsPrice.getCostprice();
		order.setCostprice(costprice);
		order.setTotalcost(costprice*quantity);
		order.setValidtime(validtime);
		Map<String, String> descMap = VmUtils.readJsonToMap(order.getDescription2());
		descMap.put("价格名称", goodsPrice.getRemark());
		order.setDescription2(JsonUtils.writeMapToJson(descMap));
		baseDao.saveObject(order);
		BuyItem item = newBuyItem(goods, goodsPrice, quantity); 
		item.setOrderid(order.getId());
		item.setMemberid(order.getMemberid());
		String tmp = goodsPrice.getPrice() + "元" ;
		String summary = goods.getGoodsname()+ "【" + tmp + (StringUtils.isBlank(goodsPrice.getRemark())? "":goodsPrice.getRemark()) + "】";
		item.setSummary(summary);
		//更新价格数量
		GoodsPriceHelper.updateGoodsPriceAddCounter(goodsPrice, quantity);
		goods.setAllowaddnum(goods.getAllowaddnum() - quantity);
		baseDao.saveObjectList(item, goodsPrice, goods);
		saveInfo(goodsId, order.getId(), member.getId(), infoList);
		return ErrorCode.getSuccessReturn(order);
	}
	private void saveInfo(Long goodsId, Long orderId, Long memberId, String infoList){
		try{
			List<Map> infoMapList = JsonUtils.readJsonToObject(List.class, infoList);
			if(infoMapList != null){
				for (Map infoMap : infoMapList) {
					Map params = new HashMap();
					params.put(MongoData.SYSTEM_ID, ObjectId.uuid());
					params.put("orderId", orderId);
					params.put("goodsId", goodsId);
					params.put("memberId", memberId);
					params.put(MongoData.ACTION_ADDTIME, DateUtil.getCurFullTimestampStr());
					params.putAll(infoMap);
					mongoService.saveOrUpdateMap(params, MongoData.SYSTEM_ID, MongoData.NS_MEMBER_TRAINING_INFO);
				}
			}
		}catch(Exception e){
			dbLogger.warn("保存培训信息出错:gid_" + goodsId + "_orderId_" + orderId + "_infoList_" + infoList + StringUtil.getExceptionTrace(e, 3));
		}
	}
	private BuyItem newBuyItem(TrainingGoods goods, GoodsPrice goodsPrice, final int quantity){
		BuyItem item = new BuyItem(quantity, goods);
		item.setPlacetype(goods.getTag());
		item.setPlaceid(goods.getRelatedid());
		item.setItemid(goods.getItemid());
		item.setItemtype("sportitem");
		item.setSmallitemid(goodsPrice.getId());
		item.setSmallitemtype(BuyItemConstant.SMALL_ITEMTYPE_PRICE);
		item.setCostprice(goodsPrice.getCostprice());
		item.setOriprice(goods.getOriprice() == null ? 0 : goods.getOriprice());
		item.setUnitprice(goodsPrice.getPrice());
		item.setQuantity(quantity);
		item.setValidtime(goods.getTotime());
		int totalcost = 0,	totalfee = 0;
		Map<String, String> otherInfoMap = JsonUtils.readJsonToMap(item.getOtherinfo());
		totalcost = goodsPrice.getCostprice() * quantity;
		totalfee = goodsPrice.getPrice() * quantity;
		item.setTotalfee(totalfee);
		item.setTotalcost(totalcost);
		String checkpass = nextRandomNum(DateUtil.addDay(goods.getFromtime(), 1), 8, "0");
		item.setCheckpass(checkpass);
		item.setOtherinfo(JsonUtils.writeMapToJson(otherInfoMap));
		Map<String, String> descMap = new HashMap<String, String>();
		descMap.put("时间", DateUtil.format(goods.getTotime(),"yyyy-MM-dd HH:mm"));
		item.setDescription(JsonUtils.writeMapToJson(descMap));
		return item;
	}
	
	private BuyItem newBuyItem(TicketGoods goods, GoodsPrice goodsPrice, GoodsDisQuantity disQuantity, final int quantity){
		BuyItem item = new BuyItem(quantity, goods);
		item.setPlacetype(goods.getTag());
		item.setPlaceid(goods.getRelatedid());
		item.setItemid(goods.getCategoryid());
		item.setItemtype(goods.getCategory());
		item.setSmallitemid(goodsPrice.getId());
		item.setSmallitemtype(BuyItemConstant.SMALL_ITEMTYPE_PRICE);
		item.setCostprice(goodsPrice.getCostprice());
		item.setOriprice(goods.getOriprice() == null? 0 : goods.getOriprice());
		item.setUnitprice(goodsPrice.getPrice());
		item.setQuantity(quantity);
		item.setValidtime(goods.getFromvalidtime());
		item.setRemark(goodsPrice.getRemark());
		int totalcost = 0,	totalfee = 0;
		Map<String, String> otherInfoMap = JsonUtils.readJsonToMap(item.getOtherinfo());
		if(disQuantity != null){
			int tmpQuantity = quantity/disQuantity.getQuantity();
			item.setQuantity(quantity);
			item.setDisid(disQuantity.getId());
			totalcost = disQuantity.getCostprice() * tmpQuantity;
			totalfee = goodsPrice.getPrice() * quantity;
			int distotalfee = disQuantity.getPrice()*tmpQuantity;
			int disfee = totalfee -distotalfee;
			item.setDisfee(disfee);
			otherInfoMap.put(BuyItemConstant.OTHERINFO_KEY_DISQUANTITY, tmpQuantity +"");
			otherInfoMap.put(BuyItemConstant.OTHERINFO_KEY_DISPRICE, disQuantity.getPrice()+"");
			otherInfoMap.put(BuyItemConstant.OTHERINFO_KEY_DISCOSTPRICE, disQuantity.getCostprice()+"");
		}else{
			totalcost = goodsPrice.getCostprice() * quantity;
			totalfee = goodsPrice.getPrice() * quantity;
		}
		item.setTotalfee(totalfee);
		item.setTotalcost(totalcost);
		String checkpass = nextRandomNum(DateUtil.addDay(goods.getFromtime(), 1), 8, "0");
		item.setCheckpass(checkpass);
		item.setOtherinfo(JsonUtils.writeMapToJson(otherInfoMap));
		Map<String, String> descMap = new HashMap<String, String>();
		descMap.put("时间", DateUtil.format(goods.getFromvalidtime(),"yyyy-MM-dd HH:mm"));
		item.setDescription(JsonUtils.writeMapToJson(descMap));
		return item;
	}
	
	private ErrorCode<GoodsOrder> addBaseGoodsOrder(BaseGoods goods, Member member, String mobile, int quantity, ApiUser partner, String ukey){
		if(!goods.hasBooking()) return ErrorCode.getFailure(ApiConstant.CODE_SIGN_ERROR, "场次或物品已关闭，不能购票！");
		if(!ValidateUtil.isMobile(mobile)) return ErrorCode.getFailure(ApiConstant.CODE_SIGN_ERROR, "手机号格式错误！");
		if(quantity <= 0) return ErrorCode.getFailure(ApiConstant.CODE_SIGN_ERROR, "购卖数量错误！");
		GoodsOrder order = null;
		if(member!=null){
			order = new GoodsOrder(member.getId(), member.getNickname(), goods);
		}else {
			order = new GoodsOrder(partner.getId(), ukey + "@" + partner.getBriefname(), goods);
		}
		
		Map<String,String> goodsOtherinfo = VmUtils.readJsonToMap(goods.getOtherinfo());
		String defaultpaymethod = goodsOtherinfo != null ? goodsOtherinfo.get("defaultpaymethod") : null;
		if(StringUtils.isNotBlank(defaultpaymethod)){
			order.setPaymethod(defaultpaymethod);
		}
		order.setTradeNo(PayUtil.getGoodsTradeNo());
		order.setMobile(mobile);
		order.setQuantity(quantity);
		order.setTotalfee(order.getOrderAmount());
		order.setDiscount(0);
		if(partner!=null){
			order.setPartnerid(partner.getId());
		}else {
			order.setPartnerid(PartnerConstant.GEWA_SELF);
		}
		int costprice = 0;
		if(goods.getCostprice()!=null) costprice = goods.getCostprice();
		order.setCostprice(costprice);
		order.setTotalcost(costprice*quantity);
		String randomNum = nextRandomNum(DateUtil.addDay(goods.getFromtime(), 15), 8, "0");
		order.setCheckpass(randomNum);
		order.setCategory(goods.getGoodstype());
		order.setUkey(ukey);
		Map<String, String> descMap = VmUtils.readJsonToMap(order.getDescription2());
		descMap.put("物品名称", goods.getGoodsname());
		order.setDescription2(JsonUtils.writeMapToJson(descMap));
		return ErrorCode.getSuccessReturn(order);
	}
	
	@Override
	public ErrorCode<GoodsOrder> addActivityGoodsOrder(ActivityGoods goods, Member member, ApiUser partner, String mobile, int quantity, String realname, String address, Timestamp jointime){
		if(StringUtils.isBlank(realname)) return ErrorCode.getFailure(ApiConstant.CODE_SIGN_ERROR, "联系人不能为空！");
		if(goods.isNeedDeliver() && StringUtils.isBlank(address)) return ErrorCode.getFailure(ApiConstant.CODE_SIGN_ERROR, "地址不能为空！"); 
		if(isOverQuantity(goods)) return ErrorCode.getFailure(ApiConstant.CODE_SIGN_ERROR, "商品库存数量不足，请联系管理员！");
		if(jointime == null) return ErrorCode.getFailure(ApiConstant.CODE_SIGN_ERROR, "参加日期不能为空！");
		if(goods.getMaxbuy() < quantity) return ErrorCode.getFailure(ApiConstant.CODE_SIGN_ERROR, "每单最多购买:"+ goods.getMaxbuy());
		Integer allowaddnum = goods.getAllowaddnum();
		if(allowaddnum != null){
			if(allowaddnum < quantity) return ErrorCode.getFailure(ApiConstant.CODE_SIGN_ERROR, "下单人数过多，您可等15分钟内未支付的订单释放名额！"); 
			goods.setAllowaddnum(allowaddnum-quantity);
		}
		int limitnum = goods.getLimitnum() + quantity;
		if(limitnum > goods.getQuantity()) return ErrorCode.getFailure(ApiConstant.CODE_SIGN_ERROR, "支付名额已满，活动已结束！");
		Long memberid = null;
		memberid = member.getId();
		String ukey = String.valueOf(memberid);
		GoodsOrder lastPaidFailure = getLastPaidFailureOrder(memberid, ukey);
		if(lastPaidFailure != null) {
			return ErrorCode.getFailure(ApiConstant.CODE_SIGN_ERROR, "您还有一个订单等待处理，订单号为" + lastPaidFailure.getTradeNo() + "，请稍后再下新订单！");
		}
		List<GoodsOrder> orderList = paymentService.getUnpaidOrderList(
				GoodsOrder.class, member.getId(),null);
		if (!orderList.isEmpty()) cancelUnpaidOrderList(orderList);
		ErrorCode<GoodsOrder> code = addBaseGoodsOrder(goods, member, mobile, quantity, partner, null);
		if(!code.isSuccess()) return code;
		GoodsOrder order = code.getRetval();
		order.setPricategory(OrderConstant.ORDER_PRICATEGORY_ACTIVITY);
		Map<String, String> decMap = JsonUtils.readJsonToMap(order.getDescription2());
		decMap.put("真实姓名", realname);
		decMap.put("参加人数", quantity+"");
		decMap.put("参加日期", DateUtil.format(jointime, "yyyy-MM-dd"));
		order.setOtherinfo(JsonUtils.addJsonKeyValue(order.getOtherinfo(), "playtime", DateUtil.format(jointime, "yyyy-MM-dd HH:mm:ss")));
		if(StringUtils.isNotBlank(address)) decMap.put("address", address);
		order.setDescription2(JsonUtils.writeMapToJson(decMap));
		order.setCitycode(goods.getCitycode());
		order.setValidtime(DateUtil.addMinute(order.getAddtime(), 15));
		String[] opkeyList = ActivityGoodsHelper.getUniqueKey(goods, order);
		for(String opkey: opkeyList){
			if(!operationService.isAllowOperation(opkey, OperationService.ONE_MINUTE * 30*24*60, 1)){
				return ErrorCode.getFailure(30 + "天内最多优惠" + 1 + "次！");
			}
		}
		order.setOtherinfo(JsonUtils.addJsonKeyValue(order.getOtherinfo(), OrderConstant.OTHERKEY_CREDENTIALSID, goods.getClerkid()+""));
		baseDao.saveObjectList(order, goods);
		return ErrorCode.getSuccessReturn(order);
	}
	@Override
	public List<GoodsOrder> getGoodsOrderList(Long goodsId, Long memberId, String status, boolean like, boolean order, int maxnum){
		DetachedCriteria query = DetachedCriteria.forClass(GoodsOrder.class);
		query.add(Restrictions.eq("goodsid", goodsId));
		if(memberId != null) query.add(Restrictions.eq("memberid", memberId));
		if(StringUtils.isNotBlank(status)) {
			if(like){
				query.add(Restrictions.like("status", status, MatchMode.START));
			}else {
				query.add(Restrictions.eq("status", status));
			}
		}
		if(order){
			query.addOrder(Order.desc("addtime"));
		}else{
			query.addOrder(Order.desc("paidtime"));
		}
		List<GoodsOrder> result = new ArrayList<GoodsOrder>();
		if(maxnum > 0){
			result = hibernateTemplate.findByCriteria(query, 0 , maxnum);
		}else{
			result = hibernateTemplate.findByCriteria(query);
		}
		return result;
	}
	@Override
	public Integer getGoodsOrderQuantity(Long goodsId, String status){
		DetachedCriteria query = DetachedCriteria.forClass(GoodsOrder.class);
		query.add(Restrictions.eq("goodsid", goodsId));
		if(StringUtils.isNotBlank(status)) query.add(Restrictions.eq("status", status));
		query.setProjection(Projections.sum("quantity"));
		List result = hibernateTemplate.findByCriteria(query);
		if(result.get(0)==null) return 0;
		return Integer.parseInt(result.get(0)+"");
	}
	
	private Integer getGoodsOrderQuantity(Long goodsId, String status,Timestamp startTime,Timestamp endTime){
		DetachedCriteria query = DetachedCriteria.forClass(GoodsOrder.class);
		query.add(Restrictions.ge("addtime", startTime));
		query.add(Restrictions.lt("addtime", endTime));
		query.add(Restrictions.eq("goodsid", goodsId));
		if(StringUtils.isNotBlank(status)) query.add(Restrictions.eq("status", status));
		query.setProjection(Projections.sum("quantity"));
		List result = hibernateTemplate.findByCriteria(query);
		if(result.get(0)==null) return 0;
		return Integer.parseInt(result.get(0)+"");
	}
	@Override
	public boolean cancelUnpaidOrderList(List<GoodsOrder> orderList) {
		for(GoodsOrder order:orderList){
			if(order.isNew()){
				Timestamp validtime = new Timestamp(System.currentTimeMillis()-1000);
				order.setStatus(OrderConstant.STATUS_REPEAT);
				order.setValidtime(validtime);
				baseDao.saveObject(order);
				dbLogger.warn("取消未支付订单：" + order.getTradeNo());
			}
		}
		return true;
	}
	@Override
	public GoodsOrder getLastPaidFailureOrder(Long memberid, String ukey){
		DetachedCriteria query = DetachedCriteria.forClass(GoodsOrder.class);
		query.add(Restrictions.eq("memberid", memberid));
		query.add(Restrictions.eq("ukey", ukey));
		query.add(Restrictions.like("status", OrderConstant.STATUS_PAID_FAILURE, MatchMode.START));
		query.add(Restrictions.gt("validtime", DateUtil.getCurFullTimestamp()));
		query.addOrder(Order.desc("addtime"));
		List<GoodsOrder> result = hibernateTemplate.findByCriteria(query);
		if(result.isEmpty()) return null;
		return result.get(0);
	}
	@Override
	public ErrorCode cancelGoodsOrder(String tradeNo, Member member) {
		GoodsOrder order = baseDao.getObjectByUkey(GoodsOrder.class, "tradeNo", tradeNo, false);
		return cancelGoodsOrder(order, member);
	}
	@Override
	public ErrorCode cancelGoodsOrder(GoodsOrder order, Member member) {
		if(order.getMemberid().equals(member.getId()) && !order.isAllPaid()){
			Timestamp validtime = new Timestamp(System.currentTimeMillis()-1000);
			order.setStatus(OrderConstant.STATUS_USER_CANCEL);
			order.setValidtime(validtime);
			Timestamp curtime = new Timestamp(System.currentTimeMillis());
			order.setUpdatetime(curtime);
			order.setModifytime(curtime);
			baseDao.saveObject(order);
			dbLogger.warn("用户取消订单：" + order.getTradeNo());
			return ErrorCode.SUCCESS;
		}else{
			return ErrorCodeConstant.NORIGHTS;
		}
	}
	@Override
	public ErrorCode<String> processGoodsOrder(GoodsOrder order){
		if(order.isPaidUnfix()){
			order.setStatus(OrderConstant.STATUS_PAID_SUCCESS);
			order.setSettle(OrderConstant.SETTLE_Y);
			BaseGoods goods = baseDao.getObject(BaseGoods.class, order.getGoodsid());
			List<BuyItem> itemList = baseDao.getObjectListByField(BuyItem.class, "orderid", order.getId());
			List<Long> idList = BeanUtil.getBeanPropertyList(itemList, "relatedid", true);
			List<BaseGoods> goodsList = baseDao.getObjectList(BaseGoods.class, idList);
			if(!goodsList.contains(goods)) goodsList.add(goods);
			int maxday = 60;
			if(goods instanceof Goods){
				if(GoodsConstant.GOODS_TAG_BMH.equals(goods.getTag())){ 
					maxday = 7;
				}else if(StringUtils.equals(GoodsConstant.GOODS_TAG_BMH_SPORT, goods.getTag())){
					Sport sport = baseDao.getObject(Sport.class, goods.getRelatedid());
					if(sport != null){
						ErrorCode<String> code = sportUntransService.updateCuOrder(order, null);
						if(!code.isSuccess()) return code;
					}else return ErrorCode.getFailure("物品关联运动场馆有误");
				}
			}else if(goods instanceof ActivityGoods){
				Integer limitnum = goods.getLimitnum();
				goods.setLimitnum(limitnum + order.getQuantity());
				baseDao.saveObject(goods);
				String[] opkeyList = ActivityGoodsHelper.getUniqueKey((ActivityGoods)goods, order);
				for(String opkey: opkeyList){
					if(!operationService.updateOperation(opkey, OperationService.ONE_MINUTE * 30*24*60, 1)){
						return ErrorCode.getFailure(30 + "天内最多优惠" + 1 + "次！");
					}
				}
			}else if(goods instanceof TicketGoods){
				Map<GoodsPrice, Integer> priceQuantityMap = new Hashtable<GoodsPrice, Integer>();
				Map<Long, GoodsPrice> priceMap = new Hashtable<Long, GoodsPrice>();
				Map<Long, Map<GoodsDisQuantity, Integer>> priceDisMap = new Hashtable<Long, Map<GoodsDisQuantity,Integer>>();
				Map<TicketGoods, Integer> goodsMap = new Hashtable<TicketGoods, Integer>();
				for (BuyItem item : itemList) {
					TicketGoods ticketGoods = baseDao.getObject(TicketGoods.class, item.getRelatedid());
					Integer goodsQuantity = goodsMap.get(ticketGoods);
					if(goodsQuantity == null){
						goodsQuantity = item.getQuantity();
					}else{
						goodsQuantity += item.getQuantity();
					}
					goodsMap.put(ticketGoods, goodsQuantity);
					GoodsPrice goodsPrice = priceMap.get(item.getSmallitemid());
					if(goodsPrice == null){
						goodsPrice = baseDao.getObject(GoodsPrice.class, item.getSmallitemid());
						priceMap.put(goodsPrice.getId(), goodsPrice);
					}
					Integer tmpQuantity = priceQuantityMap.get(goodsPrice);
					if(tmpQuantity == null){
						tmpQuantity = item.getQuantity();
					}else{
						tmpQuantity += item.getQuantity();
					}
					priceQuantityMap.put(goodsPrice, tmpQuantity);
					if(item.getDisid() != null){
						GoodsDisQuantity discount = baseDao.getObject(GoodsDisQuantity.class, item.getDisid());
						if(discount != null){
							Map<GoodsDisQuantity, Integer> disQuantityMap = priceDisMap.get(goodsPrice.getId());
							if(disQuantityMap == null){
								disQuantityMap = new Hashtable<GoodsDisQuantity, Integer>();
								priceDisMap.put(goodsPrice.getId(), disQuantityMap);
							}
							disQuantityMap.put(discount, item.getQuantity()/discount.getQuantity());
						}
					}
				}
				
				//更新卖出数量
				List result = GoodsPriceHelper.updateGoodsPriceSellCounter(priceQuantityMap, priceDisMap);
				baseDao.saveObjectList(result);
				createOrderNote(order, goodsMap);
			}else if (goods instanceof TrainingGoods) {
				Map<GoodsPrice, Integer> priceQuantityMap = new Hashtable<GoodsPrice, Integer>();
				Map<Long, GoodsPrice> priceMap = new Hashtable<Long, GoodsPrice>();
				Map<TrainingGoods, Integer> goodsMap = new Hashtable<TrainingGoods, Integer>();
				for (BuyItem item : itemList) {
					TrainingGoods trainingGoods = baseDao.getObject(TrainingGoods.class, item.getRelatedid());
					Integer goodsQuantity = goodsMap.get(trainingGoods);
					if(goodsQuantity == null){
						goodsQuantity = item.getQuantity();
					}else{
						goodsQuantity += item.getQuantity();
					}
					goodsMap.put(trainingGoods, goodsQuantity);
					GoodsPrice goodsPrice = priceMap.get(item.getSmallitemid());
					if(goodsPrice == null){
						goodsPrice = baseDao.getObject(GoodsPrice.class, item.getSmallitemid());
						priceMap.put(goodsPrice.getId(), goodsPrice);
					}
					Integer tmpQuantity = priceQuantityMap.get(goodsPrice);
					if(tmpQuantity == null){
						tmpQuantity = item.getQuantity();
					}else{
						tmpQuantity += item.getQuantity();
					}
					priceQuantityMap.put(goodsPrice, tmpQuantity);
				}
				//更新卖出数量
				List result = GoodsPriceHelper.updateGoodsPriceSellCounter(priceQuantityMap, new HashMap());
				baseDao.saveObjectList(result);
				createOrderNote(order, goods, itemList);
			}
			order.setValidtime(DateUtil.addDay(order.getAddtime(), maxday));
			baseDao.saveObject(order);
			processOrderExtra(order);
			return ErrorCode.SUCCESS;
		}else{
			return ErrorCode.getFailure(ApiConstant.CODE_DATA_ERROR, "订单状态不正确！");
		}
	}
	private List<OrderNote> createOrderNote(GewaOrder order,Map<TicketGoods, Integer> goodsMap){
		List<OrderNote> orderNoteList = new ArrayList<OrderNote>();
		int i=1;
		List<BuyItem> buyItemList = baseDao.getObjectListByField(BuyItem.class, "orderid", order.getId());
		for (TicketGoods goods : goodsMap.keySet()) {
			OrderNote orderNote = new OrderNote(order);
			orderNote.setTicketnum(goodsMap.get(goods));
			Object relate = relateService.getRelatedObject(goods.getTag(), goods.getRelatedid());
			orderNote.setPlacetype(goods.getTag());
			orderNote.setPlaceid(goods.getRelatedid());
			orderNote.setPlacename((String)BeanUtil.get(relate, "realBriefname"));
			Object category = relateService.getRelatedObject(goods.getCategory(), goods.getCategoryid());
			orderNote.setItemtype(goods.getCategory());
			orderNote.setItemid(goods.getCategoryid());
			orderNote.setItemname((String)BeanUtil.get(category, "realBriefname"));
			orderNote.setSmallitemid(goods.getId());
			orderNote.setSmallitemtype(BuyItemConstant.TAG_GOODS);
			String checkpass = nextRandomNum(DateUtil.addDay(goods.getTovalidtime(), 1), 8, "0");
			String serialno = order.getTradeNo() + "0" + i;
			orderNote.setSerialno(serialno);
			orderNote.setCheckpass(checkpass);
			Map<String, String> downmap = getDownOrderMap(order, orderNote, goods, buyItemList);
			String result = JsonUtils.writeMapToJson(downmap);
			orderNote.setDescription(result);
			orderNoteList.add(orderNote);
			i++;
		}
		baseDao.saveObjectList(orderNoteList);
		return orderNoteList;
	}
	private OrderNote createOrderNote(GewaOrder order, BaseGoods goods, List<BuyItem> buyItemList){
		OrderNote orderNote = new OrderNote(order);
		orderNote.setTicketnum(order.getQuantity());
		Object relate = relateService.getRelatedObject(goods.getTag(), goods.getRelatedid());
		orderNote.setPlacetype(goods.getTag());
		orderNote.setPlaceid(goods.getRelatedid());
		orderNote.setPlacename((String)BeanUtil.get(relate, "realBriefname"));
		Object category = relateService.getRelatedObject(goods.getItemtype(), goods.getItemid());
		orderNote.setItemtype(goods.getItemtype());
		orderNote.setItemid(goods.getItemid());
		orderNote.setItemname((String)BeanUtil.get(category, "realBriefname"));
		orderNote.setSmallitemid(goods.getId());
		orderNote.setSmallitemtype(BuyItemConstant.TAG_GOODS);
		String checkpass = nextRandomNum(DateUtil.addDay(goods.getTovalidtime(), 1), 8, "0");
		orderNote.setSerialno(order.getTradeNo());
		orderNote.setCheckpass(checkpass);
		Map<String, String> downmap = getDownOrderMap(order, orderNote, goods, buyItemList);
		String result = JsonUtils.writeMapToJson(downmap);
		orderNote.setDescription(result);
		baseDao.saveObject(orderNote);
		return orderNote;
	}
	@Override
	public Map<String, String> getDownOrderMap(GewaOrder order, OrderNote orderNote, BaseGoods goods, List<BuyItem> buyItemList){
		Map<String, String> downmap = DownOrderHelper.downOrder(order, orderNote, goods, buyItemList);
		return downmap;
	}
	@Override
	public ErrorCode usePoint(Long orderId, Long memberId, int usePoint){
		ErrorCode<String> pcode = pointService.validUsePoint(memberId);
		if(!pcode.isSuccess()) return ErrorCode.getFailure(pcode.getMsg());
		GoodsOrder order = baseDao.getObject(GoodsOrder.class, orderId);
		ErrorCode code = paymentService.validUse(order);
		if(!code.isSuccess()) return code;
		List<Discount> discountList = paymentService.getOrderDiscountList(order);
		MemberInfo info = baseDao.getObject(MemberInfo.class, memberId);
		if(info.getPointvalue() < usePoint) return ErrorCode.getFailure("您的积分不够！");
		List<BuyItem> itemList = baseDao.getObjectListByField(BuyItem.class, "orderid", order.getId());
		List<Long> idList = BeanUtil.getBeanPropertyList(itemList, "relatedid", true);
		List<BaseGoods> goodsList = baseDao.getObjectList(BaseGoods.class, idList);
		BaseGoods bgoods = baseDao.getObject(BaseGoods.class, order.getGoodsid());
		if(!goodsList.contains(bgoods)) goodsList.add(bgoods);
		boolean openPointPay = false;
		int minpoint = 0, maxpoint = 0;
		for (BaseGoods goods : goodsList) {
			if(goods.isOpenPointPay()){
				openPointPay = true;
				maxpoint = Math.max(maxpoint, goods.getMaxpoint());
				minpoint = Math.max(minpoint, goods.getMinpoint());
			}
		}
		if(!openPointPay) return ErrorCode.getFailure("你选择的商品不支持积分优惠！");
		if(maxpoint < usePoint) return ErrorCode.getFailure("您使用的积分超出上限" + maxpoint);
		int amount = usePoint/ConfigConstant.POINT_RATIO;
		usePoint = amount * ConfigConstant.POINT_RATIO;
		if(usePoint < minpoint || amount == 0){
			return ErrorCode.getFailure("您使用的积分少于下限" + minpoint);
		}
		
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
		GewaOrderHelper.useDiscount(order, discountList, discount);//加入
		baseDao.saveObject(order);
		return ErrorCode.SUCCESS;
	}
	
	@Override
	public List<GoodsOrder> getGoodsOrderList(Long relatedid, String status, String tradeNo, String mobile, Timestamp starttime, Timestamp endtime) {
		return getGoodsOrderList(relatedid, null, status, tradeNo, mobile, starttime, endtime);
	}
	@Override
	public List<GoodsOrder> getGoodsOrderList(Long relatedid, Long placeid, String status, String tradeNo, String mobile, Timestamp starttime, Timestamp endtime){
		DetachedCriteria query = DetachedCriteria.forClass(GoodsOrder.class);
		if(relatedid != null) query.add(Restrictions.eq("goodsid", relatedid));
		if(placeid != null) query.add(Restrictions.eq("placeid", placeid));
		if(StringUtils.isNotBlank(status)) query.add(Restrictions.eq("status", status));
		if(StringUtils.isNotBlank(tradeNo)) query.add(Restrictions.eq("tradeNo", tradeNo));
		if(StringUtils.isNotBlank(mobile)) query.add(Restrictions.eq("mobile", mobile));
		if(starttime != null) query.add(Restrictions.ge("addtime", starttime));
		if(endtime != null) query.add(Restrictions.le("addtime", endtime));
		query.addOrder(Order.desc("addtime"));
		List<GoodsOrder> orderList = hibernateTemplate.findByCriteria(query);
		return orderList;
	}
	
	@Override
	public Integer getGewaorderCountByMobile(Long memberid, String mobile, String ordertype){
		DetachedCriteria query = DetachedCriteria.forClass(GewaOrder.class);
		query.add(Restrictions.eq("status", OrderConstant.STATUS_PAID_SUCCESS));
		if(StringUtils.isNotBlank(ordertype)) {
			if("ticket".equals(ordertype)) {
				query.add(Restrictions.like("tradeNo", "1", MatchMode.START));
			}else if("goods".equals(ordertype)){
				query.add(Restrictions.or(Restrictions.like("tradeNo", "3", MatchMode.START), Restrictions.gt("itemfee", 0)));
			}
		}
		if(memberid!=null) query.add(Restrictions.eq("memberid", memberid));
		if(StringUtils.isNotBlank(mobile)) query.add(Restrictions.eq("mobile", mobile));
		query.setProjection(Projections.rowCount());
		List list = hibernateTemplate.findByCriteria(query);
		if(list.size()==0) return 0;
		return Integer.valueOf(list.get(0)+"");
	}
	
	@Override
	public boolean isValidGoodsGift(OpenPlayItem opi, GoodsGift gift, Long partnerid){
		if(partnerid!=null){
			Goods goods = baseDao.getObject(Goods.class, gift.getGoodsid());
			if(StringUtils.isNotBlank(goods.getPartners())){
				List<String> partneridList = Arrays.asList(goods.getPartners().split(","));
				if(!partneridList.contains(partnerid+"")) return false;
			}
		}
		if(StringUtils.isNotBlank(gift.getWeek())){
			String w = DateUtil.getWeek(opi.getPlaytime())+"";
			if(!gift.getWeek().contains(w)) {
				return false;
			}
		}
		String cur = DateUtil.format(DateUtil.currentTime(), "HHmm");
		if(StringUtils.isNotBlank(gift.getStartTime()) && cur.compareTo(gift.getStartTime()) < 0){
			return false;
		}
		if(StringUtils.isNotBlank(gift.getEndTime()) && cur.compareTo(gift.getEndTime()) >= 0){
			return false;
		}
		Goods goods = baseDao.getObject(Goods.class, gift.getGoodsid());
		if(gift.getEverydayLimit() != null && gift.getEverydayLimit() > 0){
			Timestamp now = DateUtil.getCurFullTimestamp();
			Integer num = getGoodsOrderQuantity(goods.getId(), OrderConstant.STATUS_PAID_SUCCESS,DateUtil.getBeginTimestamp(now),
					DateUtil.getLastTimeOfDay(now));
			if(num >= gift.getEverydayLimit()){
				return false;
			}
		}
		if(StringUtils.isNotBlank(gift.getMpidlist())) {
			if(gift.getMpidlist().contains(opi.getMpid().toString())) {
				return true;
			}else {
				return false;
			}
		}else if(gift.getMovieid()!=null){
			if(!gift.getMovieid().equals(opi.getMovieid())) return false;
		}
		boolean isOver = isOverQuantity(goods);
		return !isOver;
	}
	@Override
	public GoodsGift getBindGoodsGift(OpenPlayItem opi, Long partnerid){
		List<GoodsGift> goodsGiftList = getBindGoodsGift(opi.getCinemaid(), partnerid);
		if(goodsGiftList.size()==0) return null;
		GoodsGift gift = null;
		for(GoodsGift g : goodsGiftList){
			if(isValidGoodsGift(opi, g, partnerid)) return g;
		}
		return gift;
	}
	@Override
	public List<GoodsGift> getBindGoodsGift(Long cinemaid, Long partnerid){
		Timestamp curtime = new Timestamp(System.currentTimeMillis());
		String qry = "from GoodsGift g where g.cinemaid=? and g.fromtime<=? and g.totime>? order by g.id desc";
		List<GoodsGift> goodsGiftList = hibernateTemplate.find(qry, cinemaid, curtime, curtime);
		return goodsGiftList;
	}
	@Override
	public GoodsGift getBindGoodsGift(List<GoodsGift> goodsGiftList, OpenPlayItem opi, Long partnerid){
		if(goodsGiftList==null || goodsGiftList.size()==0) return null;
		GoodsGift gift = null;
		for(GoodsGift g : goodsGiftList){
			if(isValidGoodsGift(opi, g, partnerid)) return g;
		}
		return gift;
	}
	@Override
	public boolean isOverQuantity(BaseGoods goods){
		if(goods.getQuantity()==null) return false;
		Integer sum = getGoodsOrderQuantity(goods.getId(), OrderConstant.STATUS_PAID_SUCCESS);
		return sum>=goods.getQuantity();
	}
	//物品折扣
	@Override
	public ErrorCode<OrderContainer> useSpecialDiscount(Long orderId, SpecialDiscount sd, OrderCallback callback){
		GoodsOrder order = baseDao.getObject(GoodsOrder.class, orderId);
		BaseGoods goods = baseDao.getObject(BaseGoods.class, order.getGoodsid());
		List<BuyItem> itemList = baseDao.getObjectListByField(BuyItem.class, "orderid", order.getId());
		List<Long>	idList = BeanUtil.getBeanPropertyList(itemList, "relatedid", true);
		List<BaseGoods> goodsList = baseDao.getObjectList(BaseGoods.class, idList);
		if(!goodsList.contains(goods)) goodsList.add(goods);
		Spcounter spcounter = paymentService.getSpdiscountCounter(sd);
		ErrorCode<Discount> discount = getSpdiscount(spcounter, order, sd, goodsList, itemList);
		if(discount.isSuccess()){
			paymentService.updateSpdiscountAddCount(sd, spcounter, order);
			baseDao.saveObject(discount.getRetval());
			List<Discount> discountList = paymentService.getOrderDiscountList(order);
			GewaOrderHelper.useDiscount(order, discountList, discount.getRetval());
			if(StringUtils.isNotBlank(sd.getPaymethod())){
				String[] pay = StringUtils.split(sd.getPaymethod(), ":");
				order.setPaymethod(pay[0]);
				if(pay.length >1) order.setPaybank(pay[1]);
			}
			if(callback != null) callback.processOrder(sd, order);			
			baseDao.saveObject(order);
			OrderContainer container = new GoodsOrderContainer(order, goodsList);
			container.setOrder(order);
			container.setDiscountList(discountList);
			container.setCurUsedDiscount(discount.getRetval());
			container.setSpdiscount(sd);
			return ErrorCode.getSuccessReturn(container);
		}
		return ErrorCode.getFailure(discount.getMsg());
	}
	//物品折扣
	private ErrorCode<Discount> getSpdiscount(Spcounter spcounter, GoodsOrder order, SpecialDiscount sd, List<BaseGoods> goodsList, List<BuyItem> itemList) {
		SpecialDiscountHelper sdh = new GoodsSpecialDiscountHelper(order, goodsList, itemList);
		Map<String, String> otherInfoMap = getOtherInfoMap(goodsList);
		PayValidHelper valHelp = new PayValidHelper(otherInfoMap);
		ErrorCode<Integer> discountAmount = paymentService.getSpdiscountAmount(sdh, order, sd, spcounter, valHelp);
		if(!discountAmount.isSuccess()) return ErrorCode.getFailure(discountAmount.getMsg());
		Discount discount = new Discount(order.getId(), PayConstant.DISCOUNT_TAG_PARTNER, sd.getId(), PayConstant.CARDTYPE_PARTNER);
		discount.setAmount(discountAmount.getRetval());
		discount.setGoodsid(order.getGoodsid());
		discount.setDescription(sd.getDescription());
		return ErrorCode.getSuccessReturn(discount);
		
	}
	@Override
	public Map<String, String> getOtherInfoMap(List<BaseGoods> goodsList){
		Map<String, String>	otherInfoMap = new HashMap<String, String>();
		for (BaseGoods goods : goodsList) {
			otherInfoMap.putAll(JsonUtils.readJsonToMap(goods.getOtherinfo()));
		}
		return otherInfoMap;
	}
	@Override
	public ErrorCode<GoodsOrder> addGoodsOrderByBuyItem(TicketOrder torder) {
		if(torder.getItemfee()==0) return ErrorCode.getFailure("没有购票套餐！");
		if(!StringUtils.equals(torder.getStatus(), OrderConstant.STATUS_PAID_SUCCESS)) return ErrorCode.getFailure("非成功的订单！");
		//TODO:重新处理，多个BuyItem
		BuyItem buyitem = baseDao.getObjectByUkey(BuyItem.class, "orderid", torder.getId(), false);
		if(buyitem==null) return ErrorCode.getFailure("购买的套餐不存在！");
		String tradeNo = PayUtil.FLAG_GOODS + StringUtils.substring(torder.getTradeNo(), PayUtil.FLAG_GOODS.length());
		GewaOrder order = baseDao.getObjectByUkey(GewaOrder.class, "tradeNo", tradeNo, false);
		if(order!=null) return ErrorCode.getFailure("已经添加过！");
		BaseGoods goods = baseDao.getObject(BaseGoods.class, buyitem.getRelatedid());
		int cp = Integer.valueOf(torder.getCheckpass());
		cp++;
		String newpass = StringUtils.leftPad(cp+"", 8, "0");
		GoodsOrder goodsOrder = new GoodsOrder(torder, buyitem, buyitem.getValidtime());
		goodsOrder.setGoodsid(buyitem.getRelatedid());
		goodsOrder.setCitycode(torder.getCitycode());
		goodsOrder.setTradeNo(tradeNo);
		goodsOrder.setItemfee(0);
		goodsOrder.setDiscount(0);
		goodsOrder.setPaybank(OrderConstant.SYSBANK_BUY);
		goodsOrder.setPayseqno(torder.getTradeNo());
		goodsOrder.setCheckpass(newpass);
		goodsOrder.setPartnerid(torder.getPartnerid());
		goodsOrder.setPaymethod(PaymethodConstant.PAYMETHOD_SYSPAY);
		goodsOrder.setAlipaid(buyitem.getDue());
		goodsOrder.setGewapaid(0);
		//TODO:movie
		goodsOrder.setPricategory(OrderConstant.ORDER_PRICATEGORY_MOVIE);
		goodsOrder.setCategory(goods.getGoodstype());
		int costprice = 0;
		if(goods.getCostprice()!=null) costprice = goods.getCostprice();
		goodsOrder.setCostprice(costprice);
		goodsOrder.setTotalcost(costprice*buyitem.getQuantity());
		goodsOrder.setRemark("系统生成套餐订单,关联订单号"+torder.getTradeNo());
		baseDao.saveObject(goodsOrder);
		
		buyitem.setCheckpass(newpass);
		baseDao.saveObject(buyitem);
		
		dbLogger.warn("系统生成套餐订单: " + tradeNo);
		return ErrorCode.getSuccessReturn(goodsOrder);
	}
	
	@Override
	public void updateGoodsprice(GoodsOrder order){
		List<BuyItem> itemList = baseDao.getObjectListByField(BuyItem.class, "orderid", order.getId());
		Map<Long, List<BuyItem>> itemMap = BeanUtil.groupBeanList(itemList, "smallitemid");
		Map<Long, Map<GoodsDisQuantity, Integer>> priceDisMap = new Hashtable<Long, Map<GoodsDisQuantity,Integer>>();
		Map<GoodsPrice, Integer> priceMap = new Hashtable<GoodsPrice, Integer>();
		for (Long priceid : itemMap.keySet()) {
			GoodsPrice price = baseDao.getObject(GoodsPrice.class, priceid);
			int quantity = 0;
			for (BuyItem item : itemMap.get(priceid)) {
				quantity += item.getQuantity();
				if(item.getDisid() != null){
					GoodsDisQuantity discount = baseDao.getObject(GoodsDisQuantity.class, item.getDisid());
					if(discount != null){
						Map<GoodsDisQuantity, Integer> disQuantityMap = priceDisMap.get(price.getId());
						if(disQuantityMap == null){
							disQuantityMap = new Hashtable<GoodsDisQuantity, Integer>();
							priceDisMap.put(price.getId(), disQuantityMap);
						}
						disQuantityMap.put(discount, item.getQuantity()/discount.getQuantity());
					}
				}
			}
			priceMap.put(price, quantity);
		}
		List result = GoodsPriceHelper.updateGoodsPriceSubSellCounter(priceMap, priceDisMap);
		baseDao.saveObjectList(result);
	}
	@Override
	public OrderContainer processOrderPay(GewaOrder order) throws OrderException {
		return processOrderPayInternal(order);//无积分
	}
	
	@Override
	public ErrorCode<GoodsOrderContainer> useElecCard(Long orderId, ElecCard card, Long memberid){
		GoodsOrder order = baseDao.getObject(GoodsOrder.class, orderId);
		return useElecCard(order, card, memberid);
	}
	private ErrorCode<GoodsOrderContainer> useElecCard(GoodsOrder order, ElecCard card, Long memberid){
		if(!order.isNew()) return ErrorCode.getFailure("订单状态错误（" + order.getStatusText() + "）！");
		BaseGoods goods = baseDao.getObject(BaseGoods.class, order.getGoodsid());
		if(goods instanceof TicketGoods){
			TicketGoods ticketGoods = (TicketGoods) goods;
			if(!card.validTag(ticketGoods.getCategory())) return ErrorCode.getFailure("本券不能在该版块使用！");
		}else if(goods instanceof TrainingGoods){
			//运动劵票
			if(!card.validTag(TagConstant.TAG_SPORT)) return ErrorCode.getFailure("本券不能在该版块使用！");
		}else{
			if(!card.validTag(PayConstant.APPLY_TAG_GOODS)) return ErrorCode.getFailure("本券不能在该版块使用！");
		}
		ErrorCode validCode = paymentService.validUse(order);
		if(!validCode.isSuccess()) return validCode;
		List<BuyItem> itemList = baseDao.getObjectListByField(BuyItem.class, "orderid", order.getId());
		List<Long> idList = BeanUtil.getBeanPropertyList(itemList, "relatedid", true);
		List<BaseGoods> goodsList = baseDao.getObjectList(BaseGoods.class, idList);
		if(!goodsList.contains(goods)) goodsList.add(goods);
		ErrorCode<List<BaseGoods>> codeUse = checkGoodsList(card, goodsList);
		if(!codeUse.isSuccess()) return ErrorCode.getFailure(codeUse.getMsg());
		goodsList = codeUse.getRetval();
		Long batchid = card.getEbatch().getId();
		Map<String, String> otherInfoMap = getOtherInfoMap(goodsList);	
		boolean isSupportCard = new PayValidHelper(otherInfoMap).supportCard(batchid);
		if(!isSupportCard) return ErrorCode.getFailure("该场次不支持该券的使用！");
		
		ErrorCode<Discount> code = getDiscount(order, itemList, goodsList, card, memberid);
		if(!code.isSuccess()) return ErrorCode.getFailure(code.getMsg());
		Discount discount = code.getRetval();
		baseDao.saveObject(discount);
		List<Discount> discountList = paymentService.getOrderDiscountList(order);
		GewaOrderHelper.useDiscount(order, discountList, discount);
		baseDao.saveObject(order);
		GoodsOrderContainer result = new GoodsOrderContainer(order, goodsList);
		result.setCurUsedDiscount(discount);
		result.setDiscountList(discountList);
		result.setOrder(order);
		return ErrorCode.getSuccessReturn(result);
	}
	private ErrorCode<List<BaseGoods>> checkGoodsList(ElecCard card, List<BaseGoods> goodsList){
		ErrorCode code = ErrorCodeConstant.DATEERROR;
		List<BaseGoods> newGoodsList = new ArrayList<BaseGoods>();
		for (BaseGoods goods : goodsList) {
			code = checkBaseGoods(card, goods);
			if(code.isSuccess()){
				newGoodsList.add(goods);
			}
		}
		if(newGoodsList.isEmpty()) return code;
		return ErrorCode.getSuccessReturn(newGoodsList);
	}
	private ErrorCode checkBaseGoods(ElecCard card, BaseGoods goods){
		if(!StringUtils.contains(goods.getElecard(), card.getCardtype())){
			return ErrorCode.getFailure("此兑换券不可在本场次使用");
		}
		if(StringUtils.isNotBlank(card.getWeektype())){
			String week = ""+DateUtil.getWeek(goods.getFromvalidtime());
			if(card.getWeektype().indexOf(week) < 0){ 
				return ErrorCode.getFailure("此兑换券只能在周" + card.getWeektype() + "使用！");
			}
		}
		return ErrorCode.SUCCESS;
	}
	private ErrorCode getDiscount(GoodsOrder order,List<BuyItem> itemList, List<BaseGoods> goodsList, ElecCard card, Long memberid){
		//1、判断卡是否有效
		if(!card.available()) return ErrorCode.getFailure("此兑换券已经用完或失效！");
		if(order.sureOutPartner()){//非Gewa商家
			if(memberid!=null && !memberid.equals(card.getPossessor()))
				return ErrorCode.getFailure("不能用别人的兑换券！");
			if(card.getPossessor() != null) return ErrorCode.getFailure("此卡必须登录后使用");
		}else/*Gewa*/ if(card.getPossessor()!=null && !card.getPossessor().equals(memberid)){
			return ErrorCode.getFailure("不能用别人的兑换券！");
		}
		ElecCardBatch batch = card.getEbatch();
		if(StringUtils.isNotBlank(card.getValidcinema()) && order.getPlaceid() != null){
			List<Long> cidList = BeanUtil.getIdList(card.getValidcinema(), ",");
			if(!cidList.contains(order.getPlaceid())){
				return ErrorCode.getFailure("此兑换券不能在此场馆使用！");
			}
		}
		if(!card.isUseCurTime()){//时间段限制
			String opentime = batch.getAddtime1();
			String closetime = batch.getAddtime2();
			return ErrorCode.getFailure("此兑换券只能在" + opentime + "至" +  closetime + "时段内使用！");
		}
		
		if(StringUtils.isNotBlank(card.getValidmovie()) && order.getItemid() != null){
			List<Long> cidList = BeanUtil.getIdList(card.getValidmovie(), ",");
			if(!cidList.contains(order.getItemid())){
				return ErrorCode.getFailure("此项目不能使用此兑换券！");
			}
		}
		if(StringUtils.isNotBlank(card.getValiditem())){
			List<Long> cidList = BeanUtil.getIdList(card.getValiditem(), ",");
			if(!cidList.contains(order.getGoodsid())){
				return ErrorCode.getFailure("本场次不能使用此兑换券！");
			}
		}
		List<Discount> discountList = paymentService.getOrderDiscountList(order);
		String validpartner = batch.getValidpartner();
		if(StringUtils.isNotBlank(validpartner)){
			if(!VmUtils.contains(validpartner.split(","), order.getPartnerid()+"")) {
				return ErrorCode.getFailure("此兑换券不适用于该订单！");
			}
		}
		//2)成本价、支付方式更改
		if("D".equals(card.getCardtype()) && discountList.size() > 0){
			return ErrorCode.getFailure("此类券不能重复使用或与其他优惠方式共用！");
		}
		for(Discount discount: discountList){
			if(discount.getRelatedid().equals(card.getId()))
				return ErrorCode.getFailure("此兑换券已使用！");
			if("ABC".contains(discount.getCardtype()) && !card.getCardtype().equals(discount.getCardtype())){
				return ErrorCode.getFailure("此兑换券不能与其他优惠方式共用！");
			}
		}
		int amount = 0; Long goodsid = null;
		String description = "";
		if(batch.getCardtype().equals(PayConstant.CARDTYPE_C) ||
			batch.getCardtype().equals(PayConstant.CARDTYPE_D)){
			amount = batch.getAmount();
			description = card.getCardno() + "抵用" + amount + "元";
		}else if(batch.getCardtype().equals(PayConstant.CARDTYPE_A) || 
				batch.getCardtype().equals(PayConstant.CARDTYPE_B)){
			Map<Long, BaseGoods> goodsMap = BeanUtil.beanListToMap(goodsList, "id");
			int quantity = 0;
			Map<Long, Map<String, String>> itemOtherInfoMap = new HashMap<Long, Map<String,String>>();
			Map<Long,List<Discount>> disMap = BeanUtil.groupBeanList(discountList, "goodsid");
			for (Iterator<BuyItem> iterator =itemList.iterator();iterator.hasNext();) {
				BuyItem item = iterator.next();
				BaseGoods goods = goodsMap.get(item.getRelatedid());
				if(goods == null){
					//条件不满足的删除
					iterator.remove();
				}
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
			if(itemList.isEmpty() || discountList.size()>= quantity){
				return ErrorCode.getFailure("已经没有票可以使用兑换券！");
			}
			BuyItem item = itemList.get(0);
			GoodsPrice goodsPrice = baseDao.getObject(GoodsPrice.class, item.getSmallitemid());
			if(goodsPrice == null) return ErrorCode.getFailure("兑换出错！");
			if(item.getDisid() != null){
				amount = Integer.parseInt(JsonUtils.getJsonValueByKey(item.getOtherinfo(), BuyItemConstant.OTHERINFO_KEY_DISPRICE));
				goodsid = item.getDisid();
			}else{			
				amount = goodsPrice.getPrice();
				goodsid = goodsPrice.getId();
			}
			if(amount > card.getEbatch().getAmount()){
				if(batch.getCardtype().equals(PayConstant.CARDTYPE_B)){
					//补差券
					amount = batch.getAmount();
					description = card.getCardno() + "抵值" + amount + "元";
				}else{//A券超额
					String msg = StringUtils.isNotBlank(card.getEbatch().getLimitdesc())?card.getEbatch().getLimitdesc(): 
						"本券限制为只能抵用" + card.getEbatch().getAmount() + "元内的票！";
					return ErrorCode.getFailure(msg);
				}
			}else{
				description = card.getCardno() + "抵用一张票"; 
			}
		}else {
			return ErrorCode.getFailure("此种券不能使用！");
		}
		if(amount <= 0) return ErrorCode.getFailure("使用此兑换券得不到任何优惠，请看使用说明！");
		Discount discount = new Discount(order.getId(), PayConstant.DISCOUNT_TAG_ECARD, card.getId(), card.getCardtype());
		discount.setDescription(description);
		discount.setGoodsid(goodsid);
		discount.setBatchid(batch.getId());
		discount.setAmount(amount);
		return ErrorCode.getSuccessReturn(discount);
	}
	
	@Override
	public ErrorCode<BaseGoods> checkOrder(GoodsOrder order){
		BaseGoods goods = baseDao.getObject(BaseGoods.class, order.getGoodsid());
		List<BuyItem> itemList = baseDao.getObjectListByField(BuyItem.class, "orderid", order.getId());
		List<Long> idList = BeanUtil.getBeanPropertyList(itemList, "relatedid", true);
		List<BaseGoods> goodsList = baseDao.getObjectList(BaseGoods.class, idList);
		if(!goodsList.contains(goods)) goodsList.add(goods);
		for (BaseGoods baseGoods : goodsList) {
			if(StringUtils.isNotBlank(baseGoods.getExpressid())){
				OrderAddress orderAddress = baseDao.getObject(OrderAddress.class, order.getTradeNo());
				if(orderAddress == null) return ErrorCode.getFailure("请返回上一步，填写收件人及收件地址！");
				else break;
			}
		}
		if(goods instanceof TicketGoods){
			Map<Long, Integer> priceQuantityMap = new HashMap<Long, Integer>();
			Map<Long, GoodsPrice> priceMap = new HashMap<Long, GoodsPrice>();
			for (BuyItem item : itemList) {
				Integer tmpQuantity = priceQuantityMap.get(item.getSmallitemid());
				if(tmpQuantity == null){
					tmpQuantity = item.getQuantity();
				}else{
					tmpQuantity += item.getQuantity();
				}
				priceQuantityMap.put(item.getSmallitemid(), tmpQuantity);
				GoodsPrice goodsPrice = priceMap.get(item.getSmallitemid());
				if(goodsPrice == null){
					goodsPrice = baseDao.getObject(GoodsPrice.class, item.getSmallitemid());
					priceMap.put(goodsPrice.getId(), goodsPrice);
				}
				GoodsDisQuantity discount = null; 
				if(item.getDisid() != null){
					discount = baseDao.getObject(GoodsDisQuantity.class, item.getDisid());
				}
				int limitnum = goodsPrice.getAllowaddnum() + tmpQuantity;
				if(goodsPrice.getQuantity() <= goodsPrice.getSellquantity() || limitnum < tmpQuantity){
					return ErrorCode.getFailure(ApiConstant.CODE_SIGN_ERROR, goodsPrice.getPrice() + "元商品库存数量不足，请联系管理员！");
				}
				if(discount != null){
					int disQuantity = tmpQuantity /discount.getQuantity();
					if(discount.getAllownum() < discount.getSellordernum()+disQuantity){
						return ErrorCode.getFailure(ApiConstant.CODE_SIGN_ERROR, discount.getPrice() + "元("+ goodsPrice.getPrice() + " x " +discount.getQuantity()+ ")优惠库存数量不足，不能购票优惠！");
					}
				}
				/* 生成订单前这样判断没问题，生成订单之后allowaddnum数量被减少了，此处还这样判断会出现临界点问题
				String msg = GoodsPriceHelper.getGoodsPriceDisabledReason(goodsPrice, discount, tmpQuantity);
				if(StringUtils.isNotBlank(msg)){
					return ErrorCode.getFailure(ApiConstant.CODE_SIGN_ERROR, msg);
				}*/
			}
		}else {
			int limitnum = goods.getLimitnum() + order.getQuantity();
			if(goods.getQuantity()!=null && goods.getQuantity()!=0 && limitnum > goods.getQuantity()) return ErrorCode.getFailure(ApiConstant.CODE_SIGN_ERROR, "支付名额已满！");
		}
		
		return ErrorCode.getSuccessReturn(goods);
	}
	
	@Override
	public <T extends BaseGoods> List<GoodsOrder> getGoodsOrderList(Class<T> clazz, SearchOrderCommand soc) {	
		if(soc.hasBlankCond()) return new ArrayList<GoodsOrder>();
		DetachedCriteria query = DetachedCriteria.forClass(GoodsOrder.class, "o");
		if(StringUtils.isNotBlank(soc.getMobile())) query.add(Restrictions.eq("o.mobile", soc.getMobile()));
		if(StringUtils.isNotBlank(soc.getTradeNo())) query.add(Restrictions.eq("o.tradeNo", soc.getTradeNo()));
		if(StringUtils.isNotBlank(soc.getCitycode())){
			query.add(Restrictions.or(Restrictions.eq("o.citycode", soc.getCitycode()),Restrictions.isNull("o.citycode")));
		}
		if(StringUtils.isNotBlank(soc.getPricategory())) query.add(Restrictions.eq("o.pricategory", soc.getPricategory()));
		if(StringUtils.isNotBlank(soc.getCategory())) query.add(Restrictions.eq("o.category", soc.getCategory()));
		if(soc.getMinute()!=null){
			Timestamp from = DateUtil.addMinute(new Timestamp(System.currentTimeMillis()), -soc.getMinute());
			query.add(Restrictions.ge("o.addtime", from));
		}
		if(soc.getTimeFrom() != null){
			query.add(Restrictions.ge("o.addtime", soc.getTimeFrom()));
		}
		if(soc.getTimeTo() != null){
			query.add(Restrictions.le("o.addtime", soc.getTimeTo()));
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
		if(soc.getPlaceid()!=null) query.add(Restrictions.eq("o.placeid", soc.getPlaceid()));
		if(soc.getItemid()!=null) query.add(Restrictions.eq("o.itemid", soc.getItemid()));
		if(soc.getOrderid()!=null) query.add(Restrictions.eq("o.id", soc.getOrderid()));
		if(soc.getMpid()!=null) query.add(Restrictions.eq("o.goodsid", soc.getMpid()));
		DetachedCriteria sub = DetachedCriteria.forClass(clazz, "b");
		if(StringUtils.isNotBlank(soc.getExpressid()) || StringUtils.isNotBlank(soc.getPlacetype()) || StringUtils.isNotBlank(soc.getPlacetype())){
			if(StringUtils.isNotBlank(soc.getExpressid())){
				if(StringUtils.equals(soc.getExpressid(), Status.Y)){
					sub.add(Restrictions.isNotNull("b.expressid"));
				}else{
					sub.add(Restrictions.isNull("b.expressid"));
				}
			}
			if(StringUtils.isNotBlank(soc.getPlacetype())){
				sub.add(Restrictions.eq("b.tag", soc.getPlacetype()));
			}
			if(StringUtils.isNotBlank(soc.getItemtype())){
				sub.add(Restrictions.eq("b.itemtype", soc.getItemtype()));
			}
		}
		sub.add(Restrictions.eqProperty("b.id", "o.goodsid"));
		sub.setProjection(Projections.property("b.id"));
		query.add(Subqueries.exists(sub));
		query.addOrder(Order.desc("addtime"));
		List<GoodsOrder> orderList = hibernateTemplate.findByCriteria(query);
		return orderList;
	}

}
