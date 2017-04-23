package com.gewara.web.action.gewapay;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.RequestMapping;

import com.gewara.command.GoodsCommand;
import com.gewara.constant.GoodsConstant;
import com.gewara.constant.PayConstant;
import com.gewara.constant.Status;
import com.gewara.constant.sys.CacheConstant;
import com.gewara.constant.sys.JsonDataKey;
import com.gewara.constant.sys.LogTypeConstant;
import com.gewara.constant.ticket.OrderConstant;
import com.gewara.constant.ticket.PartnerConstant;
import com.gewara.helper.GoodsPriceHelper;
import com.gewara.helper.order.GoodsOrderContainer;
import com.gewara.helper.order.OrderContainer;
import com.gewara.model.agency.TrainingGoods;
import com.gewara.model.common.JsonData;
import com.gewara.model.drama.Theatre;
import com.gewara.model.goods.Goods;
import com.gewara.model.goods.GoodsGift;
import com.gewara.model.goods.GoodsPrice;
import com.gewara.model.goods.GoodsSportGift;
import com.gewara.model.goods.GoodsTheatreGift;
import com.gewara.model.goods.SportGoods;
import com.gewara.model.movie.Cinema;
import com.gewara.model.pay.Discount;
import com.gewara.model.pay.ElecCard;
import com.gewara.model.pay.GoodsOrder;
import com.gewara.model.pay.MemberAccount;
import com.gewara.model.pay.SpecialDiscount;
import com.gewara.model.sport.Sport;
import com.gewara.model.sport.SportItem;
import com.gewara.model.user.Member;
import com.gewara.model.user.MemberInfo;
import com.gewara.pay.PayUtil;
import com.gewara.service.OrderException;
import com.gewara.service.gewapay.ElecCardService;
import com.gewara.service.order.GoodsOrderService;
import com.gewara.service.order.GoodsService;
import com.gewara.support.ErrorCode;
import com.gewara.untrans.impl.ControllerService;
import com.gewara.untrans.ticket.SpecialDiscountService;
import com.gewara.untrans.ticket.TicketGoodsService;
import com.gewara.util.BeanUtil;
import com.gewara.util.DateUtil;
import com.gewara.util.JsonUtils;
import com.gewara.util.ValidateUtil;
import com.gewara.util.VmUtils;
import com.gewara.util.WebUtils;

@Controller
public class GoodsOrderController extends BasePayController{
	
	@Autowired@Qualifier("controllerService")
	private ControllerService controllerService;
	public void setControllerService(ControllerService controllerService) {
		this.controllerService = controllerService;
	}
	@Autowired@Qualifier("goodsOrderService")
	private GoodsOrderService goodsOrderService;
	public void setGoodsOrderService(GoodsOrderService goodsOrderService) {
		this.goodsOrderService = goodsOrderService;
	}
	
	@Autowired@Qualifier("ticketGoodsService")
	private TicketGoodsService ticketGoodsService;
	
	@Autowired@Qualifier("elecCardService")
	private ElecCardService elecCardService;
	
	@Autowired@Qualifier("specialDiscountService")
	private SpecialDiscountService specialDiscountService;
	@Autowired@Qualifier("goodsService")
	private GoodsService goodsService;
	public void setGoodsService(GoodsService goodsService) {
		this.goodsService = goodsService;
	}
	@RequestMapping("/gewapay/showGoods.xhtml")
	public String showGoods(Long gid, ModelMap model){
		Goods goods = daoService.getObject(Goods.class, gid);
		model.put("goods", goods);
		if(goods!=null){
			if(StringUtils.equals(GoodsConstant.GOODS_TYPE_ACTIVITY, goods.getTag())) return showRedirect("/activity/" + goods.getRelatedid(), model);
		}
		model.put("logonMember", getLogonMember());
		return "gewapay/showGoods.vm";
	}
	@RequestMapping("/gewapay/buyGoods.xhtml")
	public String buyGoods(Long gid, ModelMap model){
		Member member = getLogonMember();
		if(member==null) return showError(model, "请先登录！");
		model.put("logonMember", member);
		Goods goods = daoService.getObject(Goods.class, gid);
		if(goods==null) return showError(model, "物品不存在,不能购买");
		if(Status.DEL.equals(goods.getStatus())) return showError(model, "物品已删除,不能购买");
		if(!goods.hasBooking()) return showError(model, "物品已过期,不能购买");
		model.put("goods", goods);
		if(StringUtils.equals(GoodsConstant.GOODS_TAG_BMH, goods.getTag())){
			if(StringUtils.isNotBlank(goods.getPartners())){
				List<String> partneridList = Arrays.asList(goods.getPartners().split(","));
				if(!partneridList.contains(PartnerConstant.GEWA_SELF+"")) return forwardMessage(model, "此商品不能购买！");
			}
			GoodsGift gift = daoService.getObjectByUkey(GoodsGift.class, "goodsid", goods.getId(), true);
			if(gift!=null) return forwardMessage(model, "此商品不能购买！");
			Cinema cinema = daoService.getObject(Cinema.class, goods.getRelatedid());
			if(cinema != null){
			   	List<Goods> goodsList = goodsService.getCurGoodsList(Goods.class, GoodsConstant.GOODS_TAG_BMH, cinema.getId(), 0 ,2);
			   	if(goodsList.size() == 2){
			   		goodsList.remove(goods);
			   		model.put("otherGoods", goodsList.get(0));
			   	}
			   	model.put("relate", cinema);
			}
		}else if(StringUtils.equals(GoodsConstant.GOODS_TAG_BMH_SPORT, goods.getTag())){
			Sport sport = daoService.getObject(Sport.class, goods.getRelatedid());
			if(sport != null) model.put("relate", sport);
		}else if(StringUtils.equals(GoodsConstant.GOODS_TAG_BMH_THEATRE, goods.getTag())){
			Theatre theatre = daoService.getObject(Theatre.class, goods.getRelatedid());
			if(theatre != null) model.put("relate", theatre);
		}
		if(StringUtils.equals(GoodsConstant.GOODS_TYPE_ACTIVITY, goods.getTag())) return showRedirect("/activity/" + goods.getRelatedid(), model);
		return "gewapay/goods/buyGoods.vm";
	}
	
	@RequestMapping("/gewapay/addCardDelayOrder.xhtml")
	public String addCardDelayOrder(String cardNo,ModelMap model){
		Member member = getLogonMember();
		if(StringUtils.isBlank(member.getMobile())){
			return this.show404(model, "未绑定手机的用户，不能进行卡号有偿延期操作！");
		}
		ElecCard card = elecCardService.getMemberElecCardByNo(member.getId(), cardNo);
		if(card == null || card.getPossessor()==null || !card.getPossessor().equals(member.getId())){
			return this.show404(model, "该卡号不存在！");
		}
		if(!card.canDelay()){
			return this.show404(model, "该卡号不能进行有偿延期操作！");
		}
		JsonData jd = daoService.getObject(JsonData.class, JsonDataKey.KEY_ELECCARD_DELAY);
		if(jd != null){
			String cards = VmUtils.readJsonToMap(jd.getData()).get(card.getEbatch().getId() + "");
			if(StringUtils.isNotBlank(cards) && cards.contains(card.getCardno())){
				return this.show404(model, "该卡号已经申请过，不能再进行有偿延期操作！");
			}
		}
		//1、检查该用户有没有未付款的订单，如果有，将所有为付款的订单取消
		List<GoodsOrder> orderList = paymentService.getUnpaidOrderList(GoodsOrder.class, member.getId(),null);
		if(orderList.size() > 0) {
			goodsOrderService.cancelUnpaidOrderList(orderList);
		}
		Goods goods = daoService.getObject(Goods.class, OrderConstant.CARD_DELAY_GOODSID);
		if(goods == null || !goods.hasBooking()){
			return this.show404(model, "有效期已过，不能进行有偿延期操作！");
		}
		GoodsGift gift = daoService.getObjectByUkey(GoodsGift.class, "goodsid", goods.getId(), true);
		if(gift!=null) {
			return show404(model, "此商品不能购买！");
		}
		GoodsOrder order = null;
		try {
			order = goodsOrderService.addCardDelayOrder(goods, member, member.getMobile(), card);
		} catch (OrderException e) {
			return this.show404(model, e.getMessage());
		}
		model.put("orderId", order.getId());
		return this.showRedirect("gewapay/confirmOrder.xhtml", model);
	}
	
	@RequestMapping("/gewapay/addGoodsOrder.xhtml")
	public String addGoodsOrder(String captchaId, String captcha, Long gid, String mobile, int quantity, String address, HttpServletRequest request, ModelMap model){
		Member member = getLogonMember();
		boolean isValidCaptcha = controllerService.validateCaptcha(captchaId, captcha, WebUtils.getRemoteIp(request));
		if(!isValidCaptcha) return showJsonError(model, "验证码错误！");
		Goods goods = daoService.getObject(Goods.class, gid);
		if(goods == null || !goods.hasBooking()) return showJsonError(model, "有效期已过，不能购买！");
		if(goods.getMaxbuy() < quantity) return showJsonError(model, "每次购买数量不能大于" + goods.getMaxbuy() );
		if(GoodsConstant.GOODS_TAG_POINT.equals(goods.getTag())){ //积分兑换
			MemberInfo memberInfo = daoService.getObject(MemberInfo.class, member.getId());
			if(goods.getQuantity()<quantity){
				return showJsonError(model, "库存量"+goods.getQuantity()+"小于"+quantity+"不能兑换");
			}
			if(memberInfo.getPointvalue() < quantity*goods.getRealpoint()){
				return showJsonError(model, "你的积分"+memberInfo.getPointvalue()+"小于" + quantity*goods.getRealpoint()+"不能兑换！");
			}
			if(!goods.hasBooking()) return showJsonError(model, "已过期不能兑换");
		}else if(GoodsConstant.GOODS_TAG_BMH.equals(goods.getTag())){ //爆米花
			if(StringUtils.isNotBlank(goods.getPartners())){
				List<String> partneridList = Arrays.asList(goods.getPartners().split(","));
				if(!partneridList.contains(PartnerConstant.GEWA_SELF+"")) return showJsonError(model, "此商品不能购买！");
			}
			GoodsGift gift = daoService.getObjectByUkey(GoodsGift.class, "goodsid", goods.getId(), true);
			if(gift!=null) return showJsonError(model, "此商品不能购买！");
		}else if(GoodsConstant.GOODS_TAG_BMH_SPORT.equals(goods.getTag())){
			GoodsSportGift  sportGift = daoService.getObjectByUkey(GoodsSportGift.class, "goodsid", goods.getId(), true);
			if(sportGift != null) return showJsonError(model, "此商品不能购买！");
		}else if(GoodsConstant.GOODS_TAG_BMH_THEATRE.equals(goods.getTag())){
			GoodsTheatreGift  theatreGift = daoService.getObjectByUkey(GoodsTheatreGift.class, "goodsid", goods.getId(), true);
			if(theatreGift != null) return showJsonError(model, "此商品不能购买！");
		}
		//1、检查该用户有没有未付款的订单，如果有，将所有为付款的订单取消
		List<GoodsOrder> orderList = paymentService.getUnpaidOrderList(GoodsOrder.class, member.getId(),null);
		if(orderList.size() > 0) goodsOrderService.cancelUnpaidOrderList(orderList);
		if(StringUtils.isBlank(mobile)) return showJsonError(model, "手机号必填！");
		if(goodsOrderService.isOverQuantity(goods)) {
			dbLogger.warnWithType(LogTypeConstant.LOG_TYPE_ORDER_PAY, "商品库存数量不足: "  + goods.getGoodsname());
			return showJsonError(model, "超过库存数量");
		}
		GoodsOrder order = null;
		try {
			order = goodsOrderService.addGoodsOrder(goods, member, mobile, quantity, address);
		} catch (OrderException e) {
			return showJsonError(model, e.getMessage());
		}
		if(goods.isPointType()) {
			ErrorCode code = goodsOrderService.usePoint(order.getId(), member.getId(), quantity*goods.getRealpoint());
			if(!code.isSuccess()) return showJsonError(model, code.getMsg());
		}
		model.put("orderId", order.getId());
		return showJsonSuccess(model, order.getId()+"");
	}
	
	@RequestMapping("/gewapay/sportGoods/step1.xhtml")
	public String newchooseField(@CookieValue(value=LOGIN_COOKIE_NAME, required=false)String sessid,
			HttpServletRequest request, Long sid, Long tid, String fyrq, ModelMap model) {
		if(sid==null || tid==null) return show404(model, "参数传递错误！");
		Sport sport = daoService.getObject(Sport.class, sid);
		if(sport == null) return show404(model, "该运动场管不存在或被删除！");
		model.put("sport", sport);
		SportItem item = daoService.getObject(SportItem.class, tid);
		if(item == null) return show404(model, "该项目不存在或被删除！");
		model.put("item", item);
		if(!DateUtil.isValidDate(fyrq)) fyrq = "";
		Timestamp playDate = null;
		if(StringUtils.isBlank(fyrq)){
			playDate = DateUtil.getCurTruncTimestamp();
		}else{
			playDate = DateUtil.getBeginTimestamp(DateUtil.parseDate(fyrq));
		}
		model.put("playDate", playDate);
		model.put("fyrq", fyrq);
		List<Timestamp> timeList = goodsService.getSportGoodsReleasetime(sid, tid, 0, 7);
		Integer timeCount = goodsService.getSportGoodsReleasetimeCount(sid, tid);
		List<SportGoods> sportGoodsList = goodsService.getSportGoodsListBySportidAndItemid(sid, tid, playDate, 0, 20);
		Map<Long, Integer> surplusMap = new HashMap<Long, Integer>();
		List<SportGoods> rSportGoodsList = new ArrayList<SportGoods>();
		Timestamp time = DateUtil.getCurFullTimestamp();
		Map<Long, Long> secMap = new HashMap<Long, Long>();
		Map<Long, Integer> saleMap = new HashMap<Long, Integer>();
		for(SportGoods goods : sportGoodsList){
			Integer sum = goodsOrderService.getGoodsOrderQuantity(goods.getId(), OrderConstant.STATUS_PAID_SUCCESS);
			saleMap.put(goods.getId(), sum);
			if(goods.getQuantity() != null && sum < goods.getQuantity()){
				surplusMap.put(goods.getId(), goods.getQuantity() - sum);
			}else{
				rSportGoodsList.add(goods);
			}
			Long sec = DateUtil.getDiffSecond(goods.getTotime(), time);
			secMap.put(goods.getId(), sec);
		}
		sportGoodsList.removeAll(rSportGoodsList);
		model.put("timeList", timeList);
		model.put("timeCount", timeCount);
		model.put("sportGoodsList", sportGoodsList);
		model.put("surplusMap", surplusMap);
		model.put("secMap", secMap);
		model.put("saleMap", saleMap);
		String ip = WebUtils.getRemoteIp(request);
		Member member = loginService.getLogonMemberBySessid(ip, sessid);
		if(member != null){
			List<GoodsOrder> orderList = paymentService.getUnpaidOrderList(GoodsOrder.class, member.getId(),null);
			if(orderList.size() > 0){
				model.put("myGoodsOrder", orderList.get(0));
				SportGoods myGodos = daoService.getObject(SportGoods.class, orderList.get(0).getGoodsid());
				model.put("myGodos", myGodos);
			}
			model.put("member", member);
		}
		return "sport/new_chooseTimes_2.vm";
	}
	
	@RequestMapping("/gewapay/sportGoods/step2.xhtml")
	public String addOrder(@CookieValue(value=LOGIN_COOKIE_NAME, required=false)String sessid, 
			HttpServletRequest request, String captchaId, String captcha, String mobile, Long gid, Integer quantity, String address, ModelMap model){
		String ip = WebUtils.getRemoteIp(request);
		Member member = loginService.getLogonMemberBySessid(ip, sessid);
		if(member == null) return showJsonError(model, "请先登录！");
		boolean isValidCaptcha = controllerService.validateCaptcha(captchaId, captcha, WebUtils.getRemoteIp(request));
		if(!isValidCaptcha) return showJsonError(model, "验证码错误！");
		if(!ValidateUtil.isMobile(mobile)) return showJsonError(model, "手机号有错误！");
		SportGoods sportGoods = daoService.getObject(SportGoods.class, gid);
		if(!sportGoods.hasBooking()){
			return showJsonError(model, "部分套餐有效期已过，不能购买！");
		}else if(sportGoods.getAllowaddnum() < quantity){
			dbLogger.warnWithType(LogTypeConstant.LOG_TYPE_ORDER_PAY, "商品库存数量不足: "  + sportGoods.getGoodsname());
			return showJsonError(model, "部分套餐购买人数超过限制，不能购买！");
		}
		List<GoodsOrder> orderList = paymentService.getUnpaidOrderList(GoodsOrder.class, member.getId(),null);
		if(orderList.size() > 0) goodsOrderService.cancelUnpaidOrderList(orderList);
		ErrorCode<GoodsOrder> code = goodsOrderService.addSportGoodsOrder(sportGoods, member, mobile, quantity, address);
		if(!code.isSuccess()) return showJsonError(model, code.getMsg());
		GoodsOrder order = code.getRetval();
		model.put("orderId", order.getId());
		return showJsonSuccess(model, order.getId()+"");
	}
	
	@RequestMapping("/ajax/trade/useSportGoodsDiscount.xhtml")
	public String saveOrderInfo(@CookieValue(value=LOGIN_COOKIE_NAME, required=false)String sessid,
			HttpServletRequest request,Long orderId, String discounttype, Integer usepoint, String mobile, ModelMap model){
		String ip = WebUtils.getRemoteIp(request);
		Member member = loginService.getLogonMemberBySessid(ip, sessid);
		if(member == null) return showJsonError(model, "请先登录！");
		if(!ValidateUtil.isMobile(mobile)) return showJsonError(model, "手机格式不正确");
		GoodsOrder order = daoService.getObject(GoodsOrder.class, orderId);
		if(StringUtils.equals(discounttype, "point")){
			if(usepoint == null) return showJsonError(model, "积分不正确!");
			return usePoint(sessid, request, orderId, usepoint, model);
		}else if(StringUtils.equals(discounttype, "none")){
			if(order == null) order = daoService.getObject(GoodsOrder.class, orderId);
			if(order.getDiscount() > 0) return showJsonError(model, "您选择了不使用优惠，但订单中使用了其他优惠!");
		}else if(StringUtils.isNotBlank(discounttype)){
			Long spid = Long.parseLong(discounttype);
			SpecialDiscount sd = daoService.getObject(SpecialDiscount.class, spid);
			ErrorCode<OrderContainer> discount = specialDiscountService.useSpecialDiscount(OrderConstant.ORDER_TYPE_GOODS, orderId, sd, ip);
			if(discount.isSuccess()) return showJsonSuccess(model, ""+discount.getRetval().getCurUsedDiscount().getAmount());
			return showJsonError(model, discount.getMsg());
		}
		return showJsonSuccess(model);
	}
	
	@RequestMapping("/gewapay/sportGoods/usePoint.xhtml")
	public String usePoint(@CookieValue(value=LOGIN_COOKIE_NAME, required=false)String sessid,
			HttpServletRequest request, Long orderId, int pointvalue, ModelMap model){
		String ip = WebUtils.getRemoteIp(request);
		Member member = loginService.getLogonMemberBySessid(ip, sessid);
		if(member == null) return showJsonError(model, "请先登录！");
		
		ErrorCode code = goodsOrderService.usePoint(orderId, member.getId(), pointvalue);
		if(!code.isSuccess()){
			return showJsonError(model, code.getMsg());
		}
		return showJsonSuccess(model);
	}

	@RequestMapping("/sport/sportgoods/time.xhtml")
	public String time(String idListStr, ModelMap model){
		List<Long> idList = BeanUtil.getIdList(idListStr, ",");
		Map<String, String> timeMap = new HashMap<String, String>();
		for(Long id : idList){
			String key = CacheConstant.KEY_SPORTGOODS_TIMEOUT + id;
			Timestamp valid = (Timestamp) cacheService.get(CacheConstant.REGION_HALFHOUR, key);
			if(valid==null){
				SportGoods sportGoods = daoService.getObject(SportGoods.class, id);
				if(sportGoods != null){
					String remainTimeStr = DateUtil.getDiffDayStr(sportGoods.getTotime(), DateUtil.getCurFullTimestamp());
					cacheService.set(CacheConstant.REGION_HALFHOUR, key, valid);
					timeMap.put(""+id, ""+remainTimeStr);
				}
			}else{
				String remainTimeStr = DateUtil.getDiffDayStr(valid, DateUtil.getCurFullTimestamp());
				timeMap.put(""+id, ""+remainTimeStr);
			}
		}
		return showJsonSuccess(model, timeMap);
	}
	@RequestMapping("/goods/buyGoods.xhtml")
	public String buysGoods(@CookieValue(value = LOGIN_COOKIE_NAME, required = false)
	String sessid,HttpServletRequest request,Long gid, ModelMap model){
		Member member = loginService.getLogonMemberBySessid(WebUtils.getRemoteIp(request), sessid);
		if(member!=null) model.put("logonMember", member);
		Goods goods = daoService.getObject(Goods.class, gid);
		if(goods==null) return showError(model, "物品不存在,不能购买");
		if(Status.DEL.equals(goods.getStatus())) return showError(model, "物品已删除,不能购买");
		if(!goods.hasBooking()) return showError(model, "物品已过期,不能购买");
		model.put("goods", goods);
		if(StringUtils.equals(GoodsConstant.GOODS_TAG_BMH, goods.getTag())){
			if(StringUtils.isNotBlank(goods.getPartners())){
				List<String> partneridList = Arrays.asList(goods.getPartners().split(","));
				if(!partneridList.contains(PartnerConstant.GEWA_SELF+"")) return forwardMessage(model, "此商品不能购买！");
			}
			GoodsGift gift = daoService.getObjectByUkey(GoodsGift.class, "goodsid", goods.getId(), true);
			if(gift!=null) return forwardMessage(model, "此商品不能购买！");
			Cinema cinema = daoService.getObject(Cinema.class, goods.getRelatedid());
			if(cinema != null){
			   	List<Goods> goodsList = goodsService.getCurGoodsList(Goods.class, GoodsConstant.GOODS_TAG_BMH, cinema.getId(), 0 ,2);
			   	if(goodsList.size() == 2){
			   		goodsList.remove(goods);
			   		model.put("otherGoods", goodsList.get(0));
			   	}
			   	model.put("relate", cinema);
			}
		}else if(StringUtils.equals(GoodsConstant.GOODS_TAG_BMH_SPORT, goods.getTag())){
			Sport sport = daoService.getObject(Sport.class, goods.getRelatedid());
			if(sport != null) model.put("relate", sport);
		}else if(StringUtils.equals(GoodsConstant.GOODS_TAG_BMH_THEATRE, goods.getTag())){
			Theatre theatre = daoService.getObject(Theatre.class, goods.getRelatedid());
			if(theatre != null) model.put("relate", theatre);
		}
		if(StringUtils.equals(GoodsConstant.GOODS_TYPE_ACTIVITY, goods.getTag())) return showRedirect("/activity/" + goods.getRelatedid(), model);
		return "gewapay/goods/buyGoods.vm";
	}
	
	@RequestMapping("/goods/order/useElecCard.xhtml")
	public String useElecCardCheckGoods(@CookieValue(value=LOGIN_COOKIE_NAME, required=false)String sessid,
			HttpServletRequest request, Long orderId, String tag, String cardno,String password, ModelMap model) {
		String ip = WebUtils.getRemoteIp(request);
		Member member = loginService.getLogonMemberBySessid(ip, sessid);
		if(member == null) return showJsonError(model, "请先登录！");
		ElecCard card = null;
		if(StringUtils.equals("cardno", tag)) {
			if(StringUtils.isBlank(cardno)) return showJsonError(model, "请输入卡号！");
			card = elecCardService.getMemberElecCardByNo(member.getId(), cardno);
			if(card==null) return showJsonError(model, "卡密码不存在，重新输入！");
			if(card.getPossessor()==null || !card.getPossessor().equals(member.getId())){
				return showJsonError(model, "卡号有错误，重新输入！");
			}
		}else {
			if(StringUtils.isBlank(cardno)) return showJsonError(model, "请输入卡密码！");
			card = elecCardService.getElecCardByPass(StringUtils.upperCase(cardno));
			
		}
		if(StringUtils.isNotBlank(password)) {
			MemberAccount memberAccount = daoService.getObjectByUkey(MemberAccount.class, "memberid", member.getId(), true);
			if(!StringUtils.equals(PayUtil.getPass(password), memberAccount.getPassword())) return showJsonError(model, "支付密码错误！");
		}
		if(card==null) return showJsonError(model, "卡密码不存在，重新输入！");
		return useElecCard(orderId, card, member.getId(), model);
	}
	private String useElecCard(Long orderId, ElecCard card, Long memberid, ModelMap model){
		if(card.needActivation()){
			Map jsonMap = new HashMap();
			jsonMap.put("activation", "true");
			jsonMap.put("msg", card.getCardno());
			return showJsonError(model, jsonMap);
		}
		ErrorCode<GoodsOrderContainer> code = goodsOrderService.useElecCard(orderId, card, memberid);
		if(code.isSuccess()) {
			Map jsonMap = new HashMap<String, String>(); 
			jsonMap.put("cardno", card.getCardno());
			jsonMap.put("validtime", DateUtil.format(card.getTimeto(), "yyyy-MM-dd"));
			List<Discount> discountList = code.getRetval().getDiscountList();
			Discount curDiscount = code.getRetval().getCurUsedDiscount();
			jsonMap.put("description", curDiscount.getDescription());
			jsonMap.put("discountId", curDiscount.getId());
			jsonMap.put("discount", curDiscount.getAmount());
			jsonMap.put("usage", card.gainUsage());

			GoodsOrder order = code.getRetval().getGoodsOrder();
			jsonMap.put("count", discountList.size());
			jsonMap.put("due", order.getDue());
			jsonMap.put("totalDiscount", order.getDiscount());
			jsonMap.put("totalAmount", order.getTotalAmount());
			jsonMap.put("type", card.getCardtype());
			jsonMap.put("exchangetype", card.getEbatch().getExchangetype());
			String bindgoods = VmUtils.getJsonValueByKey(order.getOtherinfo(), PayConstant.KEY_BINDGOODS);
			if(StringUtils.isNotBlank(bindgoods)){
				Goods goods = daoService.getObject(Goods.class, new Long(bindgoods));
				if(goods!=null) jsonMap.put("bindGoods", goods.getGoodsname());
			}
			return showJsonSuccess(model, jsonMap);
		}
		return showJsonError(model, code.getMsg()+"<br/>如有疑问请联系客服：4000-406-506");
	}
	@RequestMapping("/goods/order/ticketStep1.xhtml")
	public String ticketStep1(@CookieValue(value = LOGIN_COOKIE_NAME, required = false)String sessid, 
			HttpServletRequest request, String pricelist, ModelMap model){
		if(StringUtils.isBlank(pricelist)) showJsonError(model, "场次或价格错误！");
		String ip = WebUtils.getRemoteIp(request);
		Member member = loginService.getLogonMemberBySessid(ip, sessid);
		if(member == null) return showJsonError(model, "请先登录！");
		if(!GoodsPriceHelper.isValidData(pricelist)) return showJsonError(model, "场次或价格错误！");
		List<GoodsCommand> commandList = new ArrayList<GoodsCommand>();
		try{
			commandList = JsonUtils.readJsonToObjectList(GoodsCommand.class, pricelist);
		}catch (Exception e) {
			return showJsonError(model, "场次或价格错误！");
		}
		ErrorCode<Map> code = ticketGoodsService.getGoodsPriceInfo(commandList);
		if(!code.isSuccess()) return showJsonError(model, code.getMsg());
		model.putAll(code.getRetval());
		model.put("group", backString(commandList));
		model.put("commandJson", JsonUtils.writeObjectToJson(commandList));
		model.put("logonMember", member);
		return "drama/ticket/cur_innerConfirmOrder.vm";
	}
	private String backString(List<GoodsCommand> commandList){
		String tmp = "";
		Map<Long, List<GoodsCommand>> commandMap = BeanUtil.groupBeanList(commandList, "goodsid");
		for (Long goodsid : commandMap.keySet()) {
			tmp += "_" + goodsid + ".";
			List<GoodsCommand> tmpList = commandMap.get(goodsid);
			String tmpPrice = "";
			for (GoodsCommand command : tmpList) {
				tmpPrice += "," + command.getGspid() + ":" + command.getQuantity();
			}
			tmp += StringUtils.substring(tmpPrice, 1);
		}
		return tmp;
	}
	
	@RequestMapping("/goods/order/ticketStep2.xhtml")
	public String ticketStep1(@CookieValue(value = LOGIN_COOKIE_NAME, required = false)String sessid, 
			HttpServletRequest request, String pricelist, String mobile, String captchaId, String captcha, ModelMap model){
		String ip = WebUtils.getRemoteIp(request);
		Member member = loginService.getLogonMemberBySessid(ip, sessid);
		if(member == null) return showJsonError(model, "请先登录！");
		boolean isValidCaptcha = controllerService.validateCaptcha(captchaId, captcha, WebUtils.getRemoteIp(request));
		if(!isValidCaptcha) return showJsonError(model, "验证码错误！");
		ErrorCode<GoodsOrder> code = ticketGoodsService.addTicketGoodsOrder(pricelist, member, mobile);
		if(!code.isSuccess()) return showJsonError(model, code.getMsg());
		return showJsonSuccess(model, code.getRetval().getId() + "");
	}
	
	//运动机构物品
	@RequestMapping("/goods/order/trainingStep1.xhtml")
	public String trainingStep1(@CookieValue(value = LOGIN_COOKIE_NAME, required = false)String sessid, Long goodsId, Long gspId,
			HttpServletRequest request, ModelMap model){
		String ip = WebUtils.getRemoteIp(request);
		Member member = loginService.getLogonMemberBySessid(ip, sessid);
		if(member == null) return showJsonError(model, "请先登录！");
		TrainingGoods trainingGoods = daoService.getObject(TrainingGoods.class, goodsId);
		if(trainingGoods == null || !trainingGoods.hasBooking()) return showJsonError(model, "场次已关闭购票！");
		GoodsPrice goodsPrice = daoService.getObject(GoodsPrice.class, gspId);
		if(goodsPrice == null) return showJsonError(model, "购票价格错误！");
		if(!goodsPrice.hasBooking()) return showJsonError(model, "购卖失败，价格库存不足！");
		model.put("goodsPrice", goodsPrice);
		model.put("trainingGoods", trainingGoods);
		model.put("logonMember", member);
		return "sport/agency/ticket/innerConfirmOrder.vm";
	}
	@RequestMapping("/goods/order/trainingStep2.xhtml")
	public String trainingStep2(@CookieValue(value = LOGIN_COOKIE_NAME, required = false)String sessid, HttpServletRequest request,
			Long goodsId, Long gspId, Integer quantity, String mobile, String infoList, String captchaId, String captcha, ModelMap model){
		String ip = WebUtils.getRemoteIp(request);
		Member member = loginService.getLogonMemberBySessid(ip, sessid);
		if(member == null) return showJsonError(model, "请先登录！");
		boolean isValidCaptcha = controllerService.validateCaptcha(captchaId, captcha, WebUtils.getRemoteIp(request));
		if(!isValidCaptcha) return showJsonError(model, "验证码错误！");
		List<Map> infoMapList = JsonUtils.readJsonToObject(List.class, infoList);
		if(infoMapList == null || infoMapList.isEmpty()) return showJsonError(model, "用户信息不能为空！");
		for (Map map : infoMapList) {
			if(map.keySet() == null || map.keySet().size() != 3) return showJsonError(model, "用户信息错误！");
			if(map.get("name") == null || StringUtils.isBlank(map.get("name")+"")) return showJsonError(model, "姓名不能为空！");
			if(map.get("sex") == null || StringUtils.isBlank(map.get("sex")+"")) return showJsonError(model, "性别不能为空！");
			if(map.get("age") == null || StringUtils.isBlank(map.get("age")+"")) return showJsonError(model, "年龄不能为空！");
		}
		ErrorCode<GoodsOrder> code = ticketGoodsService.addTrainingGoodsOrder(goodsId, gspId, quantity, mobile, infoList, member);
		if(!code.isSuccess()) return showJsonError(model, code.getMsg());
		return showJsonSuccess(model, code.getRetval().getId() + "");
	}
	
}
