package com.gewara.untrans.ticket.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.orm.hibernate4.HibernateOptimisticLockingFailureException;
import org.springframework.stereotype.Service;

import com.gewara.Config;
import com.gewara.command.GoodsCommand;
import com.gewara.constant.ApiConstant;
import com.gewara.constant.GoodsConstant;
import com.gewara.helper.GoodsPriceHelper;
import com.gewara.model.api.ApiUser;
import com.gewara.model.goods.BaseGoods;
import com.gewara.model.goods.GoodsDisQuantity;
import com.gewara.model.goods.GoodsPrice;
import com.gewara.model.goods.TicketGoods;
import com.gewara.model.pay.GoodsOrder;
import com.gewara.model.user.Member;
import com.gewara.service.DaoService;
import com.gewara.service.order.GoodsOrderService;
import com.gewara.support.ErrorCode;
import com.gewara.untrans.ticket.TicketGoodsService;
import com.gewara.util.GewaLogger;
import com.gewara.util.JsonUtils;
import com.gewara.util.LoggerUtils;
import com.gewara.util.StringUtil;
import com.gewara.util.ValidateUtil;

@Service("ticketGoodsService")
public class TicketGoodsServiceImpl implements TicketGoodsService {
	private final transient GewaLogger dbLogger = LoggerUtils.getLogger(getClass(), Config.getServerIp(), Config.SYSTEMID);
	
	@Autowired@Qualifier("goodsOrderService")
	private GoodsOrderService goodsOrderService;
	
	@Autowired@Qualifier("daoService")
	private DaoService daoService;
	
	protected boolean isUpdateErrorException(Throwable e){
		if(e instanceof HibernateOptimisticLockingFailureException){
			HibernateOptimisticLockingFailureException exc = (HibernateOptimisticLockingFailureException)e;
			dbLogger.warn(exc.getPersistentClassName() + ":" + exc.getIdentifier());
			return true;
		}
		return false;
	}
	@Override
	public ErrorCode<GoodsOrder> addTicketGoodsOrder(TicketGoods goods, Member member, String mobile, Integer quantity, Long disid, Long priceid){
		return addTicketGoodsOrder(goods, member, mobile, quantity, disid, priceid, null, null);
	}
	@Override
	public ErrorCode<GoodsOrder> addTicketGoodsOrder(TicketGoods goods, Member member, String mobile, Integer quantity, Long disid, Long priceid, ApiUser partner, String ukey){
		for(int i=0; i < 3;i++){
			try{
				return goodsOrderService.addTicketGoodsOrder(goods, member, mobile, quantity, disid, priceid, partner, ukey);
			}catch(Throwable e){
				//非乐观锁异常
				if(!isUpdateErrorException(e)){
					dbLogger.warn(StringUtil.getExceptionTrace(e, 10));					
					return ErrorCode.getFailure("购买出错！");
				}else{
					dbLogger.warn("addTicketGoodsOrder:" + goods.getId() + ":" + i + "," + StringUtil.getExceptionTrace(e, 3));
				}
			}
		}
		return ErrorCode.getFailure("购买出错！");
	}

	@Override
	public ErrorCode<String> payTicketGoodsOrder(GoodsOrder order, BaseGoods goods) {
		for(int i=0; i < 5; i++){
			try{
				return goodsOrderService.processGoodsOrder(order);
			}catch(Throwable e){
				//非乐观锁异常
				if(!isUpdateErrorException(e)){
					dbLogger.warn("payTicketGoodsOrder:" + goods.getId(), e);
				}else{
					dbLogger.warn("payTicketGoodsOrder:" + goods.getId() + ":" + i + "," + StringUtil.getExceptionTrace(e, 3));
				}
			}
		}
		return ErrorCode.getFailure("处理订单出错！");
	}
	@Override
	public ErrorCode<GoodsOrder> addTicketGoodsOrder(String pricelist, Member member, String mobile) {
		return addTicketGoodsOrder(pricelist, member, mobile, null, null);
	}
	@Override
	public ErrorCode<GoodsOrder> addTicketGoodsOrder(String pricelist, Member member, String mobile, ApiUser partner, String ukey){
		if(!GoodsPriceHelper.isValidData(pricelist)) return ErrorCode.getFailure(ApiConstant.CODE_SIGN_ERROR, "场次或价格错误！");
		if(!ValidateUtil.isMobile(mobile)) return ErrorCode.getFailure(ApiConstant.CODE_SIGN_ERROR, "手机号码格式错误！");
		List<GoodsCommand> commandList = new ArrayList<GoodsCommand>();
		try{
			commandList = JsonUtils.readJsonToObjectList(GoodsCommand.class, pricelist);
		}catch (Exception e) {
			return ErrorCode.getFailure(ApiConstant.CODE_SIGN_ERROR, "场次或价格错误！");
		}
		for(int i=0; i < 3;i++){
			try{
				return goodsOrderService.addTicketGoodsOrder(commandList, member, mobile, partner, ukey);
			}catch(Throwable e){
				//非乐观锁异常
				if(!isUpdateErrorException(e)){
					dbLogger.warn(StringUtil.getExceptionTrace(e, 10));					
					return ErrorCode.getFailure("购买出错！");
				}else{
					dbLogger.warn("addTicketGoodsOrder:" + pricelist + ":" + i + "," + StringUtil.getExceptionTrace(e, 3));
				}
			}
		}
		return ErrorCode.getFailure("购买出错！");
	}
	@Override
	public ErrorCode<GoodsOrder> addTrainingGoodsOrder(Long goodsId, Long gspId, Integer quantity, String mobile, String infoList, Member member){
		return this.addTrainingGoodsOrder(goodsId, gspId, quantity, mobile, infoList, member, null, null);
	}
	@Override
	public ErrorCode<GoodsOrder> addTrainingGoodsOrder(Long goodsId, Long gspId, Integer quantity, String mobile, String infoList, Member member, ApiUser partner, String ukey){
		if(!ValidateUtil.isMobile(mobile)) return ErrorCode.getFailure(ApiConstant.CODE_SIGN_ERROR, "手机号码格式错误！");
		//if(!GoodsPriceHelper.isValidTrainingData(infoList)) return ErrorCode.getFailure(ApiConstant.CODE_SIGN_ERROR, "用户信息错误！");
		for(int i=0; i < 3;i++){
			try{
				return goodsOrderService.addTrainingGoodsOrder(goodsId, gspId, quantity, mobile, infoList, member, partner, ukey);
			}catch(Throwable e){
				//非乐观锁异常
				if(!isUpdateErrorException(e)){
					dbLogger.warn(StringUtil.getExceptionTrace(e, 10));					
					return ErrorCode.getFailure("购买出错！");
				}else{
					dbLogger.warn("addTrainingGoodsOrder:gid_" + goodsId + "_gspId_" + gspId + "_quantity_" + quantity + ":" + i + "," + StringUtil.getExceptionTrace(e, 3));
				}
			}
		}
		return ErrorCode.getFailure("购买出错！");
	}

	@Override
	public ErrorCode<Map> getGoodsPriceInfo(List<GoodsCommand> commandList){
		Map jsonMap = new HashMap();
		Map<Long, Integer> goodsQuantityMap = new HashMap<Long, Integer>();
		Map<Long/*gspid*/, Integer> priceQuantityMap = new HashMap<Long, Integer>();
		String paymethod = null;
		Long relatedid = null, categoryid = null;
		Map<Long, GoodsDisQuantity> disMap = new HashMap<Long, GoodsDisQuantity>();
		Map<Long, GoodsPrice> priceMap = new HashMap<Long, GoodsPrice>();
		Map<Long, BaseGoods> goodsMap = new HashMap<Long, BaseGoods>();
		BaseGoods baseGoods = null;
		for (GoodsCommand command : commandList) {
			if(command.getGoodsid() == null || command.getGspid() == null 
					|| command.getQuantity() == null || command.getQuantity()<1 
					|| !GoodsConstant.CHECK_GOODSLIST.contains(command.getTag())){
				return ErrorCode.getFailure(ApiConstant.CODE_SIGN_ERROR, "场次或价格错误！");
			}
			TicketGoods goods = null;
			GoodsPrice goodsPrice = null;
			GoodsDisQuantity discount = null;
			int quantity = 0;
			goods = daoService.getObject(TicketGoods.class, command.getGoodsid());
			if(goods == null || !goods.hasBooking()) return ErrorCode.getFailure(ApiConstant.CODE_SIGN_ERROR, goods.getGoodsname() + "场次已关闭购票！");
			baseGoods = goods;
			if(relatedid == null){
				relatedid = goods.getRelatedid();
			}else if(!relatedid.equals(goods.getRelatedid())){
				return ErrorCode.getFailure(ApiConstant.CODE_SIGN_ERROR, "不能跨场馆购票！");
			}
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
				goodsPrice = daoService.getObject(GoodsPrice.class, command.getGspid());
				quantity = command.getQuantity();
			}else if(StringUtils.equals(command.getTag(), GoodsConstant.CHECK_GOODS_DISCOUNT)){
				discount = daoService.getObject(GoodsDisQuantity.class, command.getGspid());
				if(discount == null) return ErrorCode.getFailure(ApiConstant.CODE_SIGN_ERROR, "购买套票错误！");
				goodsPrice = daoService.getObject(GoodsPrice.class, discount.getGspid());
				quantity = command.getQuantity() * discount.getQuantity();
				disMap.put(discount.getId(), discount);
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
			Integer allowaddnum = goodsPrice.getAllowaddnum();
			if(allowaddnum != null){
				if(allowaddnum < quantity) return ErrorCode.getFailure(ApiConstant.CODE_SIGN_ERROR, "下单人数过多，您可等15分钟内未支付的订单释放名额！"); 
			}
			goodsQuantityMap.put(goods.getId(), goodsQuantity);
			priceMap.put(goodsPrice.getId(), goodsPrice);
			priceQuantityMap.put(goodsPrice.getId(), priceQuantity);
			goodsMap.put(goods.getId(), goods);
			String msg = GoodsPriceHelper.getGoodsPriceDisabledReason(goodsPrice, discount, priceQuantity);
			if(StringUtils.isNotBlank(msg)) return ErrorCode.getFailure(ApiConstant.CODE_SIGN_ERROR, msg);
		}
		jsonMap.put("priceMap", priceMap);
		jsonMap.put("disMap", disMap);
		jsonMap.put("goodsMap", goodsMap);
		jsonMap.put("goods", baseGoods);
		jsonMap.put("commandList", commandList);
		return ErrorCode.getSuccessReturn(jsonMap);
	}
}
