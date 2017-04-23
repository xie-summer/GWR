package com.gewara.service.ticket.impl;

import java.io.IOException;
import java.io.Writer;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ColumnMapRowMapper;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

import com.gewara.constant.ApiConstant;
import com.gewara.constant.PayConstant;
import com.gewara.constant.PaymethodConstant;
import com.gewara.constant.Status;
import com.gewara.constant.sys.ConfigConstant;
import com.gewara.constant.ticket.OpiConstant;
import com.gewara.constant.ticket.PartnerConstant;
import com.gewara.helper.discount.ElecCardHelper;
import com.gewara.helper.discount.MovieSpecialDiscountHelper;
import com.gewara.helper.discount.SpecialDiscountHelper;
import com.gewara.helper.discount.SportSpecialDiscountHelper.OrderCallback;
import com.gewara.helper.order.GewaOrderHelper;
import com.gewara.helper.order.OrderContainer;
import com.gewara.helper.order.TicketOrderContainer;
import com.gewara.model.draw.Prize;
import com.gewara.model.draw.WinnerInfo;
import com.gewara.model.goods.GoodsGift;
import com.gewara.model.movie.Cinema;
import com.gewara.model.movie.MoviePrice;
import com.gewara.model.pay.BuyItem;
import com.gewara.model.pay.Discount;
import com.gewara.model.pay.ElecCard;
import com.gewara.model.pay.ElecCardBatch;
import com.gewara.model.pay.SpCode;
import com.gewara.model.pay.Spcounter;
import com.gewara.model.pay.SpecialDiscount;
import com.gewara.model.pay.TicketOrder;
import com.gewara.model.ticket.OpenPlayItem;
import com.gewara.model.ticket.SellSeat;
import com.gewara.model.user.Member;
import com.gewara.model.user.MemberInfo;
import com.gewara.model.user.TempMember;
import com.gewara.pay.PayUtil;
import com.gewara.pay.PayValidHelper;
import com.gewara.service.OrderException;
import com.gewara.service.gewapay.PaymentService;
import com.gewara.service.gewapay.ScalperService;
import com.gewara.service.impl.BaseServiceImpl;
import com.gewara.service.member.MemberService;
import com.gewara.service.member.PointService;
import com.gewara.service.order.GoodsOrderService;
import com.gewara.service.ticket.OpenPlayService;
import com.gewara.service.ticket.TicketDiscountService;
import com.gewara.service.ticket.TicketOrderService;
import com.gewara.support.ErrorCode;
import com.gewara.untrans.monitor.OrderMonitorService;
import com.gewara.util.JsonUtils;
import com.gewara.util.PKCoderUtil;
import com.gewara.util.RandomUtil;
import com.gewara.util.StringUtil;
import com.gewara.util.VmUtils;

@Service("ticketDiscountService")
public class TicketDiscountServiceImpl extends BaseServiceImpl implements TicketDiscountService {
	
	@Autowired@Qualifier("orderMonitorService")
	private OrderMonitorService orderMonitorService;

	@Autowired@Qualifier("goodsOrderService")
	private GoodsOrderService goodsOrderService;
	
	@Autowired@Qualifier("openPlayService")
	private OpenPlayService openPlayService;
	
	@Autowired@Qualifier("paymentService")
	private PaymentService paymentService;
	
	@Autowired@Qualifier("ticketOrderService")
	private TicketOrderService ticketOrderService;
	
	@Autowired@Qualifier("scalperService")
	private ScalperService scalperService;
	
	@Autowired@Qualifier("pointService")
	private PointService pointService;

	@Autowired@Qualifier("memberService")
	private MemberService memberService;

	@Value("${spcodeKey}")
	private String spcodeKey;
	
	@Override
	public ErrorCode usePoint(Long orderId, Long memberId, int usePoint){
		ErrorCode<String> pcode = pointService.validUsePoint(memberId);
		if(!pcode.isSuccess()) return ErrorCode.getFailure(pcode.getMsg());
		TicketOrder order = baseDao.getObject(TicketOrder.class, orderId);
		ErrorCode code = paymentService.validUse(order);
		if(!code.isSuccess()) return code;
		List<Discount> discountList = paymentService.getOrderDiscountList(order);
		MemberInfo info = baseDao.getObject(MemberInfo.class, memberId);
		if(info.getPointvalue() < usePoint) return ErrorCode.getFailure("您的积分不够！");

		OpenPlayItem opi = baseDao.getObjectByUkey(OpenPlayItem.class, "mpid", order.getMpid(), true);
		if(opi.getMaxpoint() < usePoint) return ErrorCode.getFailure("您使用的积分超出上限" + opi.getMaxpoint());
		int amount = usePoint/ConfigConstant.POINT_RATIO;
		usePoint = amount * ConfigConstant.POINT_RATIO;
		if(usePoint < opi.getMinpoint() || amount == 0){
			return ErrorCode.getFailure("您使用的积分少于下限" + opi.getMinpoint());
		}
		if(amount > order.getDue()){
			return ErrorCode.getFailure("您使用的积分过多！");
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
		String s = usePoint + "积分抵用" + amount + "元";
		discount.setDescription(s);
		discount.setAmount(amount);
		baseDao.saveObject(discount);
		GewaOrderHelper.useDiscount(order, discountList, discount);//加入
		baseDao.saveObject(order);
		orderMonitorService.addOrderChangeLog(order.getTradeNo(), "使用积分", s, order.getMemberid());
		return ErrorCode.SUCCESS;
	}
	@Override
	public ErrorCode<OrderContainer> useSpecialDiscount(Long orderId, SpecialDiscount sd, OrderCallback callback) throws OrderException{
		TicketOrder order = baseDao.getObject(TicketOrder.class, orderId);
		if(sd == null) return ErrorCode.getFailure("本活动不存在");
		if(!order.sureOutPartner()){
			if(StringUtils.equals(sd.getBindmobile(), Status.Y)){
				Member member = baseDao.getObject(Member.class, order.getMemberid());
				if(!member.isBindMobile()){
					return ErrorCode.getFailure("该活动必须绑定手机才能使用！");
				}
				ErrorCode<String> scalper = this.scalperService.checkScalperLimited(member.getId(), member.getMobile(), sd.getId());
				if(!scalper.isSuccess()){
					dbLogger.error("orderId:" + orderId + " memberID:" + member.getId() + " mobile:" + member.getMobile() + scalper.getMsg());
					return ErrorCode.getFailure("系统繁忙，请重试!");
				}
			}
		}
		OpenPlayItem opi = baseDao.getObjectByUkey(OpenPlayItem.class, "mpid", order.getMpid(), true);
		List<SellSeat> seatList = ticketOrderService.getOrderSeatList(order.getId());
		List<Discount> discountList = paymentService.getOrderDiscountList(order);
		Spcounter spcounter = paymentService.getSpdiscountCounter(sd);
		ErrorCode<Discount> discount = getSpdiscount(spcounter, order, seatList, discountList, opi, sd);
		if(discount.isSuccess()){
			//更新下单数控制
			paymentService.updateSpdiscountAddCount(sd, spcounter, order);
			baseDao.saveObject(discount.getRetval());
			GewaOrderHelper.useDiscount(order, discountList, discount.getRetval());
			if(StringUtils.isNotBlank(sd.getPaymethod())){
				String[] payList = StringUtils.split(sd.getPaymethod(), ",");
				String[] pay = StringUtils.split(payList[0], ":");
				order.setPaymethod(pay[0]);
				if(pay.length >1) order.setPaybank(pay[1]);
			}
			//更改成本价
			if(StringUtils.isNotBlank(sd.getCosttype())){
				ErrorCode<Integer> changeCost = getCostPrice(order, opi, sd);
				if(!changeCost.isSuccess()){
					throw new OrderException(ApiConstant.CODE_DATA_ERROR, changeCost.getMsg());
				}
				int costprice = changeCost.getRetval();
				for(SellSeat seat: seatList){
					seat.setPrice(costprice);
					baseDao.saveObject(seat);
				}
				order.setCostprice(costprice);
				order.setUnitprice(costprice);
				order.setTotalcost(costprice * order.getQuantity());
				order.setTotalfee(costprice * order.getQuantity());
				order.setDescription2(JsonUtils.addJsonKeyValue(order.getDescription2(), "影票", GewaOrderHelper.getSeatText3(seatList)));
				order.setOtherinfo(JsonUtils.addJsonKeyValue(order.getOtherinfo(), PayConstant.KEY_CHANGECOST, order.getUnitprice() + ":" + order.getCostprice() + ":" + costprice));
			}
			if(callback != null) callback.processOrder(sd, order);
			baseDao.saveObject(order);
			OrderContainer container = new TicketOrderContainer(order, opi, seatList, null);
			container.setDiscountList(discountList);
			container.setCurUsedDiscount(discount.getRetval());
			container.setSpdiscount(sd);
			return ErrorCode.getSuccessReturn(container);
		}
		return ErrorCode.getFailure(discount.getMsg());	
	}
	/**
	 * costprice=[0],sellprice=[1]
	 * @param order
	 * @param opi
	 * @param batch
	 * @return
	 */
	private ErrorCode<Integer> getCostPrice(TicketOrder order, OpenPlayItem opi, SpecialDiscount sd){
		Cinema cinema = baseDao.getObject(Cinema.class, order.getCinemaid());
		MoviePrice mp = openPlayService.getMoviePrice(order.getMovieid(), cinema.getCitycode());
		int costnum = sd.getCostnum();
		if(mp != null){
			ErrorCode<Integer> codePrice = OpiConstant.getLowerPrice(opi.getEdition(), mp, opi.getPlaytime());
			if(!codePrice.isSuccess()) {
				//不可能存在这种情况
				throw new IllegalArgumentException(codePrice.getMsg());
			}
			int lowerPrice = codePrice.getRetval();
			if(lowerPrice > costnum){
				return ErrorCode.getFailure("本场次片方最低价限制，不支持此优惠！");
			}
		}
		return ErrorCode.getSuccessReturn(costnum);
	}
	
	private ErrorCode<Discount> getSpdiscount(Spcounter spcounter, TicketOrder order, List<SellSeat> seatList, List<Discount> discountList, OpenPlayItem opi, SpecialDiscount sd) {
		PayValidHelper pvh = new PayValidHelper(VmUtils.readJsonToMap(opi.getOtherinfo()));
		SpecialDiscountHelper sdh = new MovieSpecialDiscountHelper(opi, order, seatList, discountList);
		ErrorCode<Integer> result = paymentService.getSpdiscountAmount(sdh, order, sd, spcounter, pvh);
		if(!result.isSuccess()) return ErrorCode.getFailure(result.getMsg());
		Discount discount = new Discount(order.getId(), PayConstant.DISCOUNT_TAG_PARTNER, sd.getId(), PayConstant.CARDTYPE_PARTNER);
		discount.setAmount(result.getRetval());
		discount.setDescription(sd.getDescription());
		return ErrorCode.getSuccessReturn(discount);
	}
	private ErrorCode<TicketOrderContainer> useElecCard(TicketOrder order, OpenPlayItem opi, List<SellSeat> seatList, List<Discount> discountList, ElecCard card, Long memberid){
		/**
		 * 1）不能多种券类、多种优惠混用，除A、B券外，其他券不能用多张
		 * 2）对于改变成本价的券，只能使用同一批次的券，一经使用不能取消
		 * 3）对于更改成本价的B券，要满足条件：用户购买金额+补差金额>=成本价,补差的金额=调整后的价格-券amount
		 * 4）对于绑定支付方式的券，使用多张必须是同一批次的券
		 * 5）对于绑定套餐的券，使用后会取消之前购买的套餐，使用多张必须是同一批次的券
		 */
		//1)通用规则
		ErrorCode code = ElecCardHelper.getDisableReason(card, opi);
		if(!code.isSuccess()) return code;
		if(order.sureOutPartner()){//非Gewa商家
			if(memberid!=null && !memberid.equals(card.getPossessor()))
				return ErrorCode.getFailure("不能用别人的兑换券！");
			if(card.getPossessor() != null) return ErrorCode.getFailure("此卡必须登录后使用");
		}else/*Gewa*/ if(card.getPossessor()!=null && !card.getPossessor().equals(memberid)){
			return ErrorCode.getFailure("不能用别人的兑换券！");
		}
		String validpartner = card.getEbatch().getValidpartner();
		if(StringUtils.isNotBlank(validpartner)){
			if(!VmUtils.contains(validpartner.split(","), order.getPartnerid()+"")) {
				return ErrorCode.getFailure("此兑换券不适用于该订单！");
			}
		}
		if("D".equals(card.getCardtype()) && discountList.size() > 0){//D券只能使用一次
			return ErrorCode.getFailure("本券限制为每个订单最多使用一张");
		}
		for(Discount discount: discountList){
			if(discount.getRelatedid().equals(card.getId()))
				return ErrorCode.getFailure("此兑换券已使用！");
			if("ABC".contains(card.getCardtype()) && !card.getCardtype().equals(discount.getCardtype())){
				return ErrorCode.getFailure("此兑换券不能与其他优惠方式共用！");
			}
		}
		Map<String, String> otherinfoMap = VmUtils.readJsonToMap(order.getOtherinfo());

		//2)成本价、支付方式更改
		ElecCardBatch batch = card.getEbatch();
		String bindpay = batch.getBindpay();
		boolean changecost = false;	//是否改变成本价
		if(StringUtils.isNotBlank(batch.getCosttype())){
			//改变成本价
			if(order.sureOutPartner()) return ErrorCode.getFailure("本券仅限在格瓦拉网站或手机上使用！");
			bindpay = PaymethodConstant.PAYMETHOD_ELECARDPAY + ":" + batch.getId(); //改变成本价的必固定支付方式！
			changecost = true;
		}
		//绑定套餐的必固定支付方式！
		if(batch.getBindgoods()!=null){
			if(order.getQuantity()%batch.getBindratio()!=0){ 
				return ErrorCode.getFailure("本券为套餐券，选择的座位数量必须为“" + batch.getBindratio() + "”的倍数才能使用此券");
			}
			bindpay = PaymethodConstant.PAYMETHOD_ELECARDPAY + ":" + batch.getId();
		}
		if(StringUtils.isNotBlank(bindpay) ){//绑定支付
			//所有使用过的券必须是同一批次
			for(Discount discount: discountList){
				ElecCard c = baseDao.getObject(ElecCard.class, discount.getRelatedid());
				if(c==null || !c.getEbatch().equals(batch)) return ErrorCode.getFailure("只能使用同一批次的券！");
			}
		}else if(StringUtils.isNotBlank(otherinfoMap.get(PayConstant.KEY_CARDBINDPAY))){
			//此券没绑定支付方式，但前面绑定了，不可用
			return ErrorCode.getFailure("订单绑定的支付方式与券绑定的支付方式不相容！");
		}
		
		//3)计算折扣
		int amount = 0;
		int costprice = opi.getCostprice();
		Long goodsid = null;
		//int supplement = 0;	//B券补差金额
		String description = "";
		SellSeat maxSeat = null;
		
		if(batch.getCardtype().equals(PayConstant.CARDTYPE_C) ||
				batch.getCardtype().equals(PayConstant.CARDTYPE_D)){
			amount = card.getEbatch().getAmount();
			if(amount > order.getTotalfee()){//只能抵用票款，超出部分不抵用
				amount = order.getTotalfee();
			}
			description = card.getCardno() + "抵值" + amount + "元";
		}else if(batch.getCardtype().equals(PayConstant.CARDTYPE_A)) { 
			maxSeat = GewaOrderHelper.getMaxSellSeat(seatList, discountList);
			if(maxSeat==null) return ErrorCode.getFailure("已经没有座位可以使用兑换券！");
			goodsid = maxSeat.getId();
			if(order.surePartner()){
				amount = order.getUnitprice();
			}else{
				amount = maxSeat.getPrice();
			}
			if(amount > batch.getAmount()){
				String msg = StringUtils.isNotBlank(batch.getLimitdesc())?batch.getLimitdesc(): 
						"本券限制为只能抵用" + batch.getAmount() + "元内的座位！";
				return ErrorCode.getFailure(msg);
			}
			if(changecost && batch.getCostnum()!=null){
				costprice = getCostPriceByCard(order, opi, batch);
				amount = costprice;
			}
			description = card.getCardno() + "抵用" + maxSeat.getSeatLabel();
		}else if(batch.getCardtype().equals(PayConstant.CARDTYPE_B)){//补差券
			if(discountList.size() >= order.getQuantity()){
				return ErrorCode.getFailure("已经没有座位可以使用兑换券！");
			}
			//忽略5元抢票并买两张的的情况，直接场次价格
			amount = opi.getGewaprice(); 
			
			int supplement = 0; //补差金额
			if(amount > batch.getAmount()){
				supplement = amount - batch.getAmount();
				amount = batch.getAmount();
			}
			if(changecost && batch.getCostnum()!=null){
				amount = getCostPriceByCard(order, opi, batch);//重算抵用金额
				costprice = amount + supplement;
			}
			description = card.getCardno() + "抵值" + amount + "元";
		}else{
			return ErrorCode.getFailure("此种券不能使用！");
		}
		if(changecost && batch.getCostnum()!=null/*设置了价格*/){
			// 改变成本价和卖价，折扣也改变！
			for(SellSeat seat: seatList){
				if(!seat.getPrice().equals(costprice)) {
					seat.setPrice(costprice);
					baseDao.saveObject(seat);
				}
			}
			order.setCostprice(costprice);
			order.setUnitprice(costprice);
			order.setTotalcost(costprice * order.getQuantity());
			order.setTotalfee(costprice * order.getQuantity());
			order.setDescription2(JsonUtils.addJsonKeyValue(order.getDescription2(), "影票", GewaOrderHelper.getSeatText3(seatList)));
			otherinfoMap.put(PayConstant.KEY_CHANGECOST, order.getUnitprice() + ":" + order.getCostprice() + ":" + costprice);
		}
		if(amount <= 0) return ErrorCode.getFailure("使用此兑换券得不到任何优惠，请看使用说明！");
		Discount discount = new Discount(order.getId(), PayConstant.DISCOUNT_TAG_ECARD, card.getId(), card.getCardtype());
		discount.setDescription(description);
		discount.setGoodsid(goodsid);
		discount.setAmount(amount);
		discount.setBatchid(batch.getId());
		baseDao.saveObject(discount);
		GewaOrderHelper.useDiscount(order, discountList, discount);//加入
		if(batch.getBindgoods()!=null){
			otherinfoMap.put(PayConstant.KEY_BINDGOODS, ""+batch.getBindgoods());
			int bindGoodsNum = discountList.size()/batch.getBindratio();
			otherinfoMap.put(PayConstant.KEY_GOODSNUM, "" + bindGoodsNum);
			//取消其他套餐
			otherinfoMap.remove(PayConstant.KEY_GOODSGIFT);
			List<BuyItem> itemList = baseDao.getObjectListByField(BuyItem.class, "orderid", order.getId());
			baseDao.removeObjectList(itemList);
			order.setItemfee(0);
		}
		if(StringUtils.isNotBlank(bindpay) ){
			String[] payss = StringUtils.split(bindpay, ",");
			String paymethod = null;
			if(payss.length>1){
				paymethod = PayUtil.getBindPay(order, Arrays.asList(payss));
				if(StringUtils.isBlank(paymethod)) return ErrorCode.getFailure("绑定支付方式错误！");
			}else {
				paymethod = payss[0];
			}
			String[] pay = StringUtils.split(paymethod, ":");
			order.setPaymethod(pay[0]);
			if(pay.length>1) order.setPaybank(pay[1]);
			otherinfoMap.put(PayConstant.KEY_CARDBINDPAY, paymethod);
		}
		order.setOtherinfo(JsonUtils.writeMapToJson(otherinfoMap));
		baseDao.saveObject(order);
		TicketOrderContainer result = new TicketOrderContainer(order, opi, seatList, null);
		result.setCurUsedDiscount(discount);
		result.setDiscountList(discountList);
		result.setOrder(order);
		result.setSeatList(seatList);
		result.setOpi(opi);
		orderMonitorService.addOrderChangeLog(order.getTradeNo(), "使用电子券", description, order.getMemberid());
		return ErrorCode.getSuccessReturn(result);
	}
	/**
	 * costprice=[0],sellprice=[1]
	 * @param order
	 * @param opi
	 * @param batch
	 * @return
	 */
	private Integer getCostPriceByCard(TicketOrder order, OpenPlayItem opi, ElecCardBatch batch){
		Cinema cinema = baseDao.getObject(Cinema.class, order.getCinemaid());
		MoviePrice mp = openPlayService.getMoviePrice(order.getMovieid(), cinema.getCitycode());
		//2D和3D的不一样
		int costnum = batch.getCostnum();
		if(batch.getCostnum3D()!=null && OpiConstant.EDITIONS_3D.contains(opi.getEdition())){
			costnum = batch.getCostnum3D();
		}
		if(mp != null){
			ErrorCode<Integer> codePrice = OpiConstant.getLowerPrice(opi.getEdition(), mp, opi.getPlaytime());
			if(!codePrice.isSuccess()) {
				//不可能存在这种情况
				throw new IllegalArgumentException(codePrice.getMsg());
			}
			int lowerPrice = codePrice.getRetval();
			if(StringUtils.equals(batch.getCosttype(), ElecCardBatch.COSTTYPE_FIXED)){
				return Math.max(lowerPrice, costnum);
			}else {
				return Math.max(lowerPrice, lowerPrice + costnum);
			}
		}else {//未设置最低价
			if(StringUtils.equals(batch.getCosttype(), ElecCardBatch.COSTTYPE_FIXED)){
				return costnum;
			}else{//TODO:最低价+x，但未设置最低价,throw Exception? 出错
				return opi.getCostprice();
			}
		}
	}
	@Override
	public ErrorCode<TicketOrderContainer> useElecCard(Long orderId, ElecCard card, Long memberid) {
		TicketOrder order = baseDao.getObject(TicketOrder.class, orderId);
		return useElecCard(order, card, memberid); 
	}
	@Override
	public ErrorCode<TicketOrderContainer> useElecCardByTradeNo(String tradeNo, ElecCard card, Long memberid){
		TicketOrder order = baseDao.getObjectByUkey(TicketOrder.class, "tradeNo", tradeNo, false);
		return useElecCard(order, card, memberid); 
	}
	private ErrorCode<TicketOrderContainer> useElecCard(TicketOrder order, ElecCard card, Long memberid) {
		if(card==null) return ErrorCode.getFailure(ApiConstant.CODE_DATA_ERROR, "券不存在！");
		if(order == null) return ErrorCode.getFailure(ApiConstant.CODE_DATA_ERROR, "订单不存在！");
		if(!order.isNew()) return ErrorCode.getFailure(ApiConstant.CODE_DATA_ERROR, "订单状态错误（" + order.getStatusText() + "）！");

		ErrorCode validCode = paymentService.validUse(order);
		if(!validCode.isSuccess()) return validCode;

		if(StringUtils.equals(card.getEbatch().getActivation(), ElecCardBatch.ACTIVATION_Y)){
			if(card.getPossessor() == null){
				if(card.expiredUnused()){
					if(!card.canDelay()){
						return ErrorCode.getFailure("该券已过期！");
					}
				}
				return ErrorCode.getFailure(ApiConstant.CODE_USER_NORIGHTS,"您所使用的票券需要绑定后才能使用，请至个人中心绑定！");
			}
		}
		if(card.expiredUnused()){
			if(card.canDelay()){
				return ErrorCode.getFailure(ApiConstant.CODE_USER_NORIGHTS,"该券已过期，如需继续使用，可至个人中心进行绑定后申请有偿延期！");
			}else{
				return ErrorCode.getFailure("该券已过期！");
			}
		}

		List<SellSeat> seatList = ticketOrderService.getOrderSeatList(order.getId());
		List<Discount> discountList = paymentService.getOrderDiscountList(order);
		OpenPlayItem opi = baseDao.getObjectByUkey(OpenPlayItem.class, "mpid", order.getMpid(), true);
		if(card.getEbatch().getBindgoods()!=null) {
			GoodsGift goodsGift = goodsOrderService.getBindGoodsGift(opi, PartnerConstant.GEWA_SELF);
			if(goodsGift!=null) {
				return ErrorCode.getFailure("该卡不适用于本场次！");
			}else {
				//TODO:重新处理，多个BuyItem
				BuyItem item = baseDao.getObjectByUkey(BuyItem.class, "orderid", order.getId(), true);
				if(item != null) return ErrorCode.getFailure("已购买套餐，不推荐使用该卡！");
			}
		}

		return useElecCard(order, opi, seatList, discountList, card, memberid);
	}
	@Override
	public ErrorCode<String> randomSendPrize(Prize prize, WinnerInfo winner) {
		Long spid = Long.parseLong(prize.getTag());
		SpCode spcode = null;
		SpecialDiscount sd = baseDao.getObject(SpecialDiscount.class, spid);
		DetachedCriteria query = DetachedCriteria.forClass(SpCode.class);
		query.add(Restrictions.eq("sdid", spid));
		if(StringUtils.equals(sd.getVerifyType(), SpecialDiscount.VERIFYTYPE_ONLYONE)){
			query.add(Restrictions.isNull("mobile"));
			query.add(Restrictions.isNull("memberid"));
			query.add(Restrictions.eq("usedcount", 0));
		}
		List<SpCode> result = hibernateTemplate.findByCriteria(query, 0, 50);
		if(result.isEmpty()){
			result = genSpCode(sd, 100);
			//FIXME:warn("电子码数量不足，请重试！");
		}
		spcode = RandomUtil.getRandomObject(result);
		
		if(StringUtils.equals(sd.getVerifyType(), SpecialDiscount.VERIFYTYPE_ONLYONE)){
			spcode.setMobile(winner.getMobile());
			if(winner.getMemberid() != null) {
				spcode.setMemberid(winner.getMemberid());
			}
			baseDao.saveObject(spcode);
		}
		String codePass = PKCoderUtil.decryptString(spcode.getCodepass(), spcodeKey);
		winner.setRemark(codePass);
		winner.setRelatedid(spcode.getId());
		baseDao.saveObject(winner);
		return ErrorCode.SUCCESS;
	}
	@Override
	public ErrorCode<String> randomBindCard(Member member, Long spid){
		SpCode spcode = null;
		SpecialDiscount sd = baseDao.getObject(SpecialDiscount.class, spid);
		DetachedCriteria query = DetachedCriteria.forClass(SpCode.class);
		query.add(Restrictions.eq("sdid", spid));
		if(StringUtils.equals(sd.getVerifyType(), SpecialDiscount.VERIFYTYPE_ONLYONE)){
			query.add(Restrictions.isNull("mobile"));
			query.add(Restrictions.isNull("memberid"));
			query.add(Restrictions.eq("usedcount", 0));
		}
		List<SpCode> result = hibernateTemplate.findByCriteria(query, 0, 50);
		if(result.isEmpty()){
			result = genSpCode(sd, 100);
			//FIXME:warn("电子码数量不足，请重试！");
		}
		spcode = RandomUtil.getRandomObject(result);
		spcode.setMobile(member.getMobile());
		spcode.setMemberid(member.getId());
		baseDao.saveObject(spcode);
		return ErrorCode.SUCCESS;
	}
	@Override
	public List<SpCode> genSpCode(SpecialDiscount sd, int maxnum) {
		if(maxnum > 10000) maxnum = 10000;
		List<SpCode> codeList = new ArrayList<SpCode>();
		for(int i=0; i< maxnum; i++){
			String random = StringUtil.getRandomString(8, true, false, true);
			String codepass = SpCode.PASSPRE + random + StringUtil.md5(random + spcodeKey, 3);
			String encode = PKCoderUtil.encryptString(codepass.toUpperCase(), spcodeKey);
			SpCode code = new SpCode(encode, sd.getId());
			codeList.add(code);
		}
		baseDao.saveObjectList(codeList);
		return codeList;
	}
	@Override
	public SpCode getSpCodeByPass(String pass){
		if(!StringUtils.startsWith(pass, SpCode.PASSPRE)){
			return null;
		}
		
		String encode = PKCoderUtil.encryptString(pass.toUpperCase(), spcodeKey);
		SpCode result = baseDao.getObjectByUkey(SpCode.class, "codepass", encode);
		return result;
	}
	@Override
	public List<SpCode> getSpCodeList(Long memberid, Long spid, int fromnum, int maxnum){
		//TODO:只有存在未过期的特价活动才查找
		DetachedCriteria query = DetachedCriteria.forClass(SpCode.class);
		query.add(Restrictions.eq("memberid", memberid));
		if(spid!=null){
			query.add(Restrictions.eq("sdid", spid));
		}
		query.addOrder(Order.asc("usedcount"));
		List<SpCode> result = hibernateTemplate.findByCriteria(query, fromnum, maxnum);
		for (SpCode spCode : result) {
			spCode.setTemppass(PKCoderUtil.decryptString(spCode.getCodepass(), spcodeKey));
		}
		return result;
	}
	@Override
	public void exportSpCodePassBySd(SpecialDiscount sd, final Writer writer, Long userid) throws IOException{
		String query = "select count(1) from webdata.spcode where sdid=? and usedcount=0 ";
		int count = jdbcTemplate.queryForInt(query, sd.getId());
		
		writer.write("spdiscountId:" + sd.getId() + ", total unused:" + count + "! \n");
		final String[] columns = new String[]{"codepass", "memberid", "mobile"};
		writer.write(StringUtils.join(columns, "\t") + "\n");
		
		dbLogger.warn(userid + "导出卡密码,exportSpCode:bid=" + sd.getId());

		query = "select codepass, memberid, mobile from webdata.spcode where sdid=" + sd.getId() + " and usedcount=0";
		jdbcTemplate.query(query, new ResultSetExtractor<String>(){
			@Override
			public String extractData(ResultSet rs) throws SQLException, DataAccessException {
				RowMapper<Map<String, Object>> mapper = new ColumnMapRowMapper(); 
				try{
					int rowcount=0;
					while(rs.next()){
						Map<String, Object> row = mapper.mapRow(rs, rowcount ++);
						String pass = (String) row.get("codepass");
						pass = PKCoderUtil.decryptString(pass, spcodeKey);
						writer.write(pass + "\t" + row.get("memberid") + "\t" + row.get("mobile") + "\n");
					}
				}catch(Exception e){
					return StringUtil.getExceptionTrace(e, 50);
				}
				return null;
			}
		});

	}
	@Override
	public Integer getSpCodeCountByMemberid(Long memberid){
		DetachedCriteria query = DetachedCriteria.forClass(SpCode.class);
		query.add(Restrictions.eq("memberid", memberid));
		query.setProjection(Projections.rowCount());
		List result = hibernateTemplate.findByCriteria(query);
		if(result.isEmpty()) return 0;
		return new Integer(result.get(0)+"");
	}
	@Override
	public Map getSpCodeCountStats(Long sdid) {
		String query = "select count(1) as totalcount, " +
				"sum(case when memberid is null then 0 else 1 end) bindcount, " +
				"sum(case when mobile is null then 0 else 1 end) mobilecount, " + 
				"sum(case when usedcount>0 then 1 else 0 end) usedcount " +
				"from webdata.spcode where sdid=? ";
		Map<String, Object> row = jdbcTemplate.queryForMap(query, sdid);
		return row;
	}
	
	
	@Override
	public ErrorCode<Member> processBaiduPaySuccess(TempMember tm, Long spid){
		if(StringUtils.equals(tm.getStatus(), Status.Y)){//已处理过
			return ErrorCode.getFailure("已经处理过！");
		}
		ErrorCode<Member> result = null;
		if(tm.getMemberid()!=null){//只是绑定
			result = memberService.bindMobileFromTempMember(tm);
		}else{
			result = memberService.createMemberFromTempMember(tm);
		}
		if(result.isSuccess()){
			randomBindCard(result.getRetval(), spid);	
		}
		return result;
	}
}
