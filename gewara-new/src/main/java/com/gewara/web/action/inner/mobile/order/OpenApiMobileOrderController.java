package com.gewara.web.action.inner.mobile.order;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.betwixt.io.BeanReader;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import com.gewara.Config;
import com.gewara.constant.ApiConstant;
import com.gewara.constant.PayConstant;
import com.gewara.constant.PaymethodConstant;
import com.gewara.constant.app.AppConstant;
import com.gewara.constant.sys.ConfigConstant;
import com.gewara.constant.ticket.OrderConstant;
import com.gewara.constant.ticket.PartnerConstant;
import com.gewara.helper.GewaAppHelper;
import com.gewara.helper.api.GewaApiOrderHelper;
import com.gewara.helper.discount.DramaSpecialDiscountHelper;
import com.gewara.helper.discount.GoodsSpecialDiscountHelper;
import com.gewara.helper.discount.MovieSpecialDiscountHelper;
import com.gewara.helper.discount.SpecialDiscountHelper;
import com.gewara.helper.discount.SportSpecialDiscountHelper;
import com.gewara.helper.order.GewaOrderHelper;
import com.gewara.helper.order.OrderContainer;
import com.gewara.helper.order.SportOrderContainer;
import com.gewara.helper.order.TicketOrderContainer;
import com.gewara.json.mobile.SpShare;
import com.gewara.model.api.ApiUser;
import com.gewara.model.api.ApiUserExtra;
import com.gewara.model.drama.DramaOrder;
import com.gewara.model.drama.OpenDramaItem;
import com.gewara.model.drama.SellDramaSeat;
import com.gewara.model.goods.BaseGoods;
import com.gewara.model.goods.Goods;
import com.gewara.model.mobile.AsConfig;
import com.gewara.model.movie.Cinema;
import com.gewara.model.partner.CallbackOrder;
import com.gewara.model.pay.BuyItem;
import com.gewara.model.pay.Discount;
import com.gewara.model.pay.GewaOrder;
import com.gewara.model.pay.GoodsOrder;
import com.gewara.model.pay.OtherFeeDetail;
import com.gewara.model.pay.Spcounter;
import com.gewara.model.pay.SpecialDiscount;
import com.gewara.model.pay.SportOrder;
import com.gewara.model.pay.TicketOrder;
import com.gewara.model.sport.OpenTimeItem;
import com.gewara.model.sport.OpenTimeTable;
import com.gewara.model.ticket.OpenPlayItem;
import com.gewara.model.ticket.SellSeat;
import com.gewara.model.user.Member;
import com.gewara.model.user.MemberInfo;
import com.gewara.model.user.OpenMember;
import com.gewara.pay.PartnerPayUtil;
import com.gewara.pay.PayOtherUtil;
import com.gewara.pay.PayValidHelper;
import com.gewara.service.drama.DramaOrderService;
import com.gewara.service.gewapay.PaymentService;
import com.gewara.service.order.OrderQueryService;
import com.gewara.service.partner.PartnerSynchService;
import com.gewara.service.sport.SportOrderService;
import com.gewara.service.ticket.TicketDiscountService;
import com.gewara.support.ErrorCode;
import com.gewara.untrans.drama.TheatreOrderService;
import com.gewara.untrans.order.impl.SpdiscountService;
import com.gewara.untrans.sport.SportUntransService;
import com.gewara.untrans.ticket.SpecialDiscountService;
import com.gewara.util.ApiUtils;
import com.gewara.util.BeanUtil;
import com.gewara.util.DateUtil;
import com.gewara.util.HttpResult;
import com.gewara.util.HttpUtils;
import com.gewara.util.JsonUtils;
import com.gewara.util.RandomUtil;
import com.gewara.util.VmUtils;
import com.gewara.web.action.inner.OpenApiAuth;
import com.gewara.web.action.inner.mobile.BaseOpenApiMobileController;
import com.gewara.web.filter.OpenApiMobileAuthenticationFilter;
import com.gewara.xmlbind.pay.TenBank;
import com.gewara.xmlbind.pay.TenBankList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

@Controller
public class OpenApiMobileOrderController extends BaseOpenApiMobileController{
	@Autowired@Qualifier("specialDiscountService")
	private SpecialDiscountService specialDiscountService;
	@Autowired@Qualifier("ticketDiscountService")
	private TicketDiscountService ticketDiscountService;
	@Autowired@Qualifier("paymentService")
	private PaymentService paymentService;
	@Autowired@Qualifier("sportOrderService")
	private SportOrderService sportOrderService;
	@Autowired@Qualifier("spdiscountService")
	private SpdiscountService spdiscountService;
	@Autowired@Qualifier("theatreOrderService")
	private TheatreOrderService theatreOrderService;
	@Autowired@Qualifier("dramaOrderService")
	private DramaOrderService dramaOrderService;
	@Autowired@Qualifier("sportUntransService")
	private SportUntransService sportUntransService;
	@Autowired@Qualifier("partnerSynchService")
	private PartnerSynchService partnerSynchService;
	@Autowired@Qualifier("orderQueryService")
	private OrderQueryService orderQueryService;
	/**
	 * 订单的状态
	 */
	@RequestMapping("/openapi/mobile/order/getStatus.xhtml")
	public String getStatus(String tradeNo, ModelMap model){
		GewaOrder order = daoService.getObjectByUkey(GewaOrder.class, "tradeNo", tradeNo);
		if(order==null) return getErrorXmlView(model,ApiConstant.CODE_SIGN_ERROR,"该订单不存在");
		return getSingleResultXmlView(model, ApiConstant.getMappedOrderStatus(order.getFullStatus()));
	}
	/**
	 * 订单的有效期 
	 */
	@RequestMapping("/openapi/mobile/order/getValidTime.xhtml")
	public String getValidTime(String tradeNo, ModelMap model){
		Long valid = orderQueryService.getOrderValidTime(tradeNo);
		if(valid==null){
			return getErrorXmlView(model,ApiConstant.CODE_SIGN_ERROR,"有效时间已过");
		}
		Long cur = System.currentTimeMillis();
		Long remain = valid - cur;
		model.put("remain", remain);
		return getSingleResultXmlView(model, remain);
	}
	
	
	private Discount getDiscountByTag(List<Discount> discountList, String tag){
		for(Discount discount : discountList){
			if(StringUtils.equals(discount.getTag(), tag)){
				return discount;
			}
		}
		return null;
	}
	/**
	 * 支付时，获取订单优惠信息(获得优惠活动列表API)
	 */
	@RequestMapping("/openapi/mobile/order/getDiscountList.xhtml")
	public String getDiscountList(String tradeNo, String memberEncode,  String ukey, ModelMap model){
		if(StringUtils.isBlank(tradeNo)) return getErrorXmlView(model, ApiConstant.CODE_DATA_ERROR, "参数有误！");
		GewaOrder order = daoService.getObjectByUkey(GewaOrder.class, "tradeNo", tradeNo, false);
		if(order == null) return getErrorXmlView(model, ApiConstant.CODE_DATA_ERROR, "订单不存在！");
		if (order.isAllPaid() || order.isCancel()) return getErrorXmlView(model, ApiConstant.CODE_DATA_ERROR, "不能操作已支付或已（过时）取消的订单！");
		OpenApiAuth auth = OpenApiMobileAuthenticationFilter.getOpenApiAuth();
		ApiUser partner = auth.getApiUser();
		String opentype = "";
		Member member = null;
		boolean supportPoint = false, supportCard = false;
		//List<Long> sdidList = new ArrayList<Long>();
		if(apiMobileService.isGewaPartner(partner.getId())){
			opentype = SpecialDiscount.OPENTYPE_WAP;
			member = OpenApiMobileAuthenticationFilter.getOpenApiAuth().getMember();
			if(member==null){
				return getErrorXmlView(model, ApiConstant.CODE_DATA_ERROR, "获取登录信息失败，请刷新重试或重新登录！");
			}
			if(!order.getMemberid().equals(member.getId())) return getErrorXmlView(model, ApiConstant.CODE_DATA_ERROR, "不能操作他人订单！");
			model.put("hasMobile", member.isBindMobile()); ////防止黄牛
			if(partner.getId().equals(PartnerConstant.GEWA_HTC)) model.put("hasMobile", true); //HTC
			supportPoint = true;
			model.put("memberEncode", memberEncode);
			//sdidList =  getMemberSdid(member);
		}else {
			opentype = SpecialDiscount.OPENTYPE_PARTNER;
			model.put("hasMobile", true);
			if(!StringUtils.equals(order.getUkey(), ukey)) return getErrorXmlView(model, ApiConstant.CODE_DATA_ERROR, "不能操作他人订单！");
		}
		
		List<Discount> discountList = paymentService.getOrderDiscountList(order);
		model.put("order", order);
		model.put("cancelable", order.getStatus().equals(OrderConstant.STATUS_NEW));
		Integer minpoint = 500, maxpoint = 10000;
		if(order instanceof TicketOrder){
			OpenPlayItem opi = daoService.getObjectByUkey(OpenPlayItem.class, "mpid", ((TicketOrder)order).getMpid(), true);
			if(StringUtils.contains(opi.getElecard(), PayConstant.CARDTYPE_PARTNER)){
				List<SellSeat> seatList = ticketOrderService.getOrderSeatList(order.getId());
				List<String> limitPayList = paymentService.getLimitPayList();
				Map<String, String> otherInfo = VmUtils.readJsonToMap(opi.getOtherinfo());
				PayValidHelper valHelp = new PayValidHelper(otherInfo);
				valHelp.setLimitPay(limitPayList);
				SpecialDiscountHelper sdh = new MovieSpecialDiscountHelper(opi, (TicketOrder)order, seatList, discountList);
				boolean openSpdiscount = StringUtils.contains(opi.getElecard(), PayConstant.CARDTYPE_PARTNER);
				Map discountData = spdiscountService.getSpecialDiscountData(sdh, valHelp, order, openSpdiscount, opi.getSpflag(), discountList, opentype, PayConstant.APPLY_TAG_MOVIE);
				model.putAll(discountData);
			}
			if(supportPoint){
				if(opi.isOpenPointPay()){ 
					minpoint = opi.getMinpoint();
					maxpoint = opi.getMaxpoint();
				}else {
					supportPoint = false;
				}
				if(opi.isOpenCardPay()) supportCard = true;
			}
		}else if(order instanceof SportOrder){
			SportOrder sorder = (SportOrder) order;
			OpenTimeTable ott = daoService.getObject(OpenTimeTable.class,  sorder.getOttid());
			List<OpenTimeItem> otiList = sportOrderService.getMyOtiList(order.getId());
			SpecialDiscountHelper sdh = new SportSpecialDiscountHelper(sorder, ott, discountList, otiList);
			Map<String, String> otherInfo = VmUtils.readJsonToMap(ott.getOtherinfo());
			model.put("opiOtherinfo", otherInfo);
			List<String> limitPayList = paymentService.getLimitPayList();
			PayValidHelper valHelp = new PayValidHelper(otherInfo);
			valHelp.setLimitPay(limitPayList);
			boolean openSpdiscount = StringUtils.contains(ott.getElecard(), PayConstant.CARDTYPE_PARTNER);
			Map discountData = spdiscountService.getSpecialDiscountData(sdh, valHelp, order, openSpdiscount, ott.getSpflag(), 
					discountList, opentype, PayConstant.APPLY_TAG_SPORT);
			
			model.putAll(discountData);
			Map<String, String> orderOtherinfo = VmUtils.readJsonToMap(order.getOtherinfo());
			model.put("orderOtherinfo", orderOtherinfo);
			if(supportPoint){
				if(ott.isOpenPointPay()) { 
					minpoint = ott.getMinpoint();
					maxpoint = ott.getMaxpoint();
				}else {
					supportPoint = false;
				}
				if(ott.isOpenCardPay()) supportCard = true;
			}
		}else if(order instanceof GoodsOrder){
			GoodsOrder gorder = (GoodsOrder)order;
			BaseGoods goods = daoService.getObject(BaseGoods.class, gorder.getGoodsid());
			List<BaseGoods> goodsList = new ArrayList<BaseGoods>();
			List<BuyItem> itemList = new ArrayList<BuyItem>();
			Map<String, String> otherInfoMap = JsonUtils.readJsonToMap( goods.getOtherinfo());
			goodsList.add( goods);
			SpecialDiscountHelper sdh = new GoodsSpecialDiscountHelper(gorder, goodsList, itemList);
			PayValidHelper valHelp = new PayValidHelper(otherInfoMap);
			Map discountData = spdiscountService.getSpecialDiscountData(sdh, valHelp, order, true,  goods.getSpflag(), 
					discountList, opentype, PayConstant.APPLY_TAG_GOODS);
			model.putAll(discountData);
			if(supportPoint){
				if(goods.isOpenPointPay()) { 
					minpoint = goods.getMinpoint();
					maxpoint = goods.getMaxpoint();
				}else {
					supportPoint = false;
				}
				if(goods.isOpenCardPay()) supportCard = true;
			}
		}else if(order instanceof DramaOrder){
			DramaOrder dorder = (DramaOrder)order;
			OpenDramaItem odi = daoService.getObjectByUkey(OpenDramaItem.class, "dpid", dorder.getDpid());
			List<BuyItem> buyList = daoService.getObjectListByField(BuyItem.class, "orderid", dorder.getId());
			List<OpenDramaItem> itemList = dramaOrderService.getOpenDramaItemList(odi, buyList);
			List<SellDramaSeat> seatList = null;
			if(odi.isOpenseat()) seatList = dramaOrderService.getDramaOrderSeatList(order.getId());
			SpecialDiscountHelper sdh = new DramaSpecialDiscountHelper(dorder, itemList, buyList, discountList, seatList);
			Map<String, String> otherInfo = VmUtils.readJsonToMap(odi.getOtherinfo());
			List<String> limitPayList = paymentService.getLimitPayList();
			PayValidHelper valHelp = new PayValidHelper(otherInfo);
			valHelp.setLimitPay(limitPayList);
			boolean openSpdiscount = StringUtils.contains(odi.getElecard(), PayConstant.CARDTYPE_PARTNER);
			Map discountData = spdiscountService.getSpecialDiscountData(sdh, valHelp, order, openSpdiscount, odi.getSpflag(), 
					discountList, opentype, PayConstant.APPLY_TAG_DRAMA);
			model.putAll(discountData);
			if(supportPoint){
				if(odi.isOpenPointPay()) { 
					minpoint = odi.getMinpoint();
					maxpoint = odi.getMaxpoint();
				}else {
					supportPoint = false;
				}
				if(odi.isOpenCardPay()) supportCard = true;
			}
		}
		MemberInfo memberInfo = null;
		if(supportPoint && minpoint >0 ){
			memberInfo = daoService.getObject(MemberInfo.class, member.getId());
			if(memberInfo.getPointvalue()<minpoint){
				model.put("lessPoint", true);
			}
		}
		model.put("minpoint", minpoint);
		model.put("maxpoint", maxpoint);
		model.put("supportCard", supportCard);
		model.put("supportPoint", supportPoint);
		model.put("pointDiscount", getDiscountByTag(discountList, PayConstant.DISCOUNT_TAG_POINT));
		List<SpecialDiscount> oldspList = (List<SpecialDiscount>)model.get("spdiscountList");
		/*List<SpecialDiscount> oldspList2 = new ArrayList<SpecialDiscount>();
		if(oldspList!=null){
			for(SpecialDiscount oldsp : oldspList){
				if(StringUtils.isNotBlank(oldsp.getVerifyType())){
					if(sdidList.contains(oldsp.getId())){
						oldspList2.add(oldsp);
						model.put("hasMobile", true);
					}
				}else {
					oldspList2.add(oldsp);
				}
			}
		}*/
		ApiUserExtra extra = auth.getUserExtra();
		model.put("spdiscountList", GewaAppHelper.getDiscountList(oldspList, extra, getOpenMember(memberInfo)));
		return getXmlView(model, "inner/mobile/discountList.vm");
	}
	
	/**
	 * 支付方式展示(手机客户端支付方式列表API)
	 */
	@RequestMapping("/openapi/mobile/order/showPayMethodList.xhtml")
	public String showPayMethodList(String tradeNo,Long discountId, String ukey, String appVersion, HttpServletRequest request, ModelMap model){
		if(StringUtils.isBlank(tradeNo)) return getErrorXmlView(model, ApiConstant.CODE_DATA_ERROR, "参数有误！");
		GewaOrder order = daoService.getObjectByUkey(GewaOrder.class,"tradeNo",tradeNo, false);
		if(order == null) return getErrorXmlView(model, ApiConstant.CODE_DATA_ERROR, "订单不存在！");
		if (order.isAllPaid() || order.isCancel()) return getErrorXmlView(model, ApiConstant.CODE_DATA_ERROR, "不能操作已支付或已（过时）取消的订单！");
		OpenApiAuth auth = OpenApiMobileAuthenticationFilter.getOpenApiAuth();
		MemberInfo memberInfo = null;
		String omCategory = null;
		if(apiMobileService.isGewaPartner(auth.getApiUser().getId())){
			Member member = OpenApiMobileAuthenticationFilter.getOpenApiAuth().getMember();
			if(member==null){
				return getErrorXmlView(model, ApiConstant.CODE_DATA_ERROR, "获取登录信息失败，请刷新重试或重新登录！");
			}
			memberInfo = daoService.getObject(MemberInfo.class, member.getId());
			ErrorCode<OpenMember> omCode = getOpenMember(memberInfo);
			if(omCode.isSuccess()){
				omCategory = omCode.getRetval().getCategory();
			}
			if(!order.getMemberid().equals(member.getId())) return getErrorXmlView(model, ApiConstant.CODE_DATA_ERROR, "不能操作他人订单！");
		}else {
			if(!StringUtils.equals(order.getUkey(), ukey)) return getErrorXmlView(model, ApiConstant.CODE_DATA_ERROR, "不能操作他人订单！");
		}
		SpecialDiscount sd = null;
		if(discountId != null){
			sd = daoService.getObject(SpecialDiscount.class, discountId);
			if(sd==null){
				return getErrorXmlView(model, ApiConstant.CODE_DATA_ERROR, "特价活动不存在！");
			}
			if(!SpecialDiscount.OPENTYPE_WAP.equals(sd.getOpentype()) 
					&& !SpecialDiscount.OPENTYPE_PARTNER.equals(sd.getOpentype())
					&& !SpecialDiscount.OPENTYPE_SPECIAL.equals(sd.getOpentype())){
				return getErrorXmlView(model, ApiConstant.CODE_DATA_ERROR, "选择的优惠方式当前订单不可使用！");
			}
		}
		PayValidHelper valHelp = new PayValidHelper();
		if(sd!=null){
			String ip = auth.getRemoteIp();
			ErrorCode<OrderContainer> discount = null;
			if(order instanceof TicketOrder){
				discount = specialDiscountService.useSpecialDiscount(OrderConstant.ORDER_TYPE_TICKET, order.getId(), sd, ip);
				if(discount.isSuccess()){
					OpenPlayItem opi = ((TicketOrderContainer)discount.getRetval()).getOpi();
					valHelp = new PayValidHelper(VmUtils.readJsonToMap(opi.getOtherinfo()));
				}
			}else if(order instanceof SportOrder){
				discount = specialDiscountService.useSpecialDiscount(OrderConstant.ORDER_TYPE_SPORT, order.getId(), sd, ip);
				if(discount.isSuccess()){
					OpenTimeTable ott = ((SportOrderContainer)discount.getRetval()).getOtt();
					valHelp = new PayValidHelper(VmUtils.readJsonToMap(ott.getOtherinfo()));
				}
			}else if(order instanceof DramaOrder){
				discount = specialDiscountService.useSpecialDiscount(OrderConstant.ORDER_TYPE_DRAMA, order.getId(), sd, ip);
				if(discount.isSuccess()){
					DramaOrder dorder = (DramaOrder)order;
					OpenDramaItem dpi = daoService.getObjectByUkey(OpenDramaItem.class, "dpid", dorder.getDpid());
					ErrorCode<String> code = validExpress(dorder, dpi);
					if(!code.isSuccess()){
						return getErrorXmlView(model, ApiConstant.CODE_DATA_ERROR, code.getMsg());
					}
					valHelp = new PayValidHelper(VmUtils.readJsonToMap(dpi.getOtherinfo()));
				}
			}else if(order instanceof GoodsOrder){
				discount = specialDiscountService.useSpecialDiscount(OrderConstant.ORDER_TYPE_GOODS, order.getId(), sd, ip);
				if(discount.isSuccess()){
					GoodsOrder gorder = (GoodsOrder)order;
					Goods goods = daoService.getObject(Goods.class, gorder.getGoodsid());
					valHelp = new PayValidHelper(VmUtils.readJsonToMap(goods.getOtherinfo()));
				}
			}
			if(discount!=null){
				if(!discount.isSuccess()) {
					return getErrorXmlView(model, ApiConstant.CODE_DATA_ERROR, discount.getMsg());
				}
				order = discount.getRetval().getOrder();
			}
		}else {
			valHelp = getPayValidHelper(valHelp, order);
		}
		
		Map<String, String> orderOtherinfo = VmUtils.readJsonToMap(order.getOtherinfo());
		List<Discount> discountList = paymentService.getOrderDiscountList(order);
		List<String> limitPayList = paymentService.getLimitPayList();
		String bindpay = paymentService.getBindPay(discountList, orderOtherinfo, order);
		if(StringUtils.isNotBlank(bindpay)){
			valHelp = new PayValidHelper(bindpay);
			model.put("flag", "true");
			String[] bindpayArr = StringUtils.split(bindpay, ",");
			for(String t : bindpayArr){
				limitPayList.remove(t);
			}
		}
		valHelp.setLimitPay(limitPayList);
		model.put("valHelp", valHelp);
		String orderPaymethod = order.getPaymethod();
		if(StringUtils.isNotBlank(order.getPaybank())){ 
			orderPaymethod = orderPaymethod + ":" + order.getPaybank();
		}
		ApiUserExtra extra = auth.getUserExtra();
		
		Map<String, String> payMap = new HashMap<String, String>();
		AsConfig asConfig = getAsConfig(auth.getApiUser(), request);
		if(asConfig!=null){
			payMap = GewaAppHelper.getFilterMap(asConfig, discountList);
		}else {
			payMap = GewaAppHelper.getFilterMap(extra, discountList, omCategory, appVersion);
		}
		model.put("payMethodMap", payMap);
		model.put("tradeno", order.getTradeNo());
		model.put("discountAmount", order.getDiscount());
		model.put("totalAmount", order.getTotalAmount());
		model.put("due", order.getDue());
		model.put("alimember", isAliMember(memberInfo));
		if(discountList.size()>0){
			model.put("relatedId", discountList.get(0).getRelatedid());
		}
		model.put("extra", extra);
		return getXmlView(model, "inner/mobile/showPayMethodList.vm");
	}
	private ErrorCode<String> validExpress(DramaOrder dorder, OpenDramaItem dpi){
		String takemethod = dramaOrderService.getTakemethodByOdi(dorder, dpi);
		if(StringUtils.equals(takemethod, OtherFeeDetail.FEETYPE_E)){
			List<OtherFeeDetail> feeList = daoService.getObjectListByField(OtherFeeDetail.class, "orderid", dorder.getId());
			Map<String, OtherFeeDetail> feeMap = BeanUtil.beanListToMap(feeList, "feetype");
			if(!feeMap.containsKey(OtherFeeDetail.FEETYPE_E)){
				return ErrorCode.getFailure("请确认是否选择了收货地址");
			}
		}
		return ErrorCode.SUCCESS;
	}
	private PayValidHelper getPayValidHelper(PayValidHelper valHelp, GewaOrder order){
		if(order instanceof TicketOrder){
			TicketOrder torder = (TicketOrder)order;
			OpenPlayItem opi = daoService.getObjectByUkey(OpenPlayItem.class, "mpid", torder.getMpid(), false);
			valHelp = new PayValidHelper(VmUtils.readJsonToMap(opi.getOtherinfo()));
		}else if(order instanceof SportOrder){
			SportOrder sorder = (SportOrder)order;
			OpenTimeTable ott = daoService.getObject(OpenTimeTable.class, sorder.getOttid());
			valHelp = new PayValidHelper(VmUtils.readJsonToMap(ott.getOtherinfo()));
		}else if(order instanceof DramaOrder) {
			DramaOrder dorder = (DramaOrder)order;
			OpenDramaItem dpi = daoService.getObjectByUkey(OpenDramaItem.class, "dpid", dorder.getDpid());
			valHelp = new PayValidHelper(VmUtils.readJsonToMap(dpi.getOtherinfo()));
		}else if(order instanceof GoodsOrder){
			GoodsOrder gorder = (GoodsOrder)order;
			BaseGoods goods = daoService.getObject(BaseGoods.class, gorder.getGoodsid());
			valHelp = new PayValidHelper(VmUtils.readJsonToMap(goods.getOtherinfo()));
		}
		return valHelp;
	}
	
	/**
	 * 取消优惠活动(取消优惠API)
	 */
	@RequestMapping("/openapi/mobile/order/cancelDiscount.xhtml")
	public String cancelDisCount(String tradeNo,Long discountId, String ukey, ModelMap model){
		if(StringUtils.isBlank(tradeNo)) return getErrorXmlView(model, ApiConstant.CODE_DATA_ERROR, "参数有误！");
		GewaOrder order = daoService.getObjectByUkey(GewaOrder.class,"tradeNo",tradeNo, false);
		if(order == null) return getErrorXmlView(model, ApiConstant.CODE_DATA_ERROR, "订单不存在！");
		if (order.isAllPaid() || order.isCancel()) return getErrorXmlView(model, ApiConstant.CODE_DATA_ERROR, "不能操作已支付或已（过时）取消的订单！");
		OpenApiAuth auth = OpenApiMobileAuthenticationFilter.getOpenApiAuth();
		if(apiMobileService.isGewaPartner(auth.getApiUser().getId())){
			Member member = OpenApiMobileAuthenticationFilter.getOpenApiAuth().getMember();
			if(!order.getMemberid().equals(member.getId())) return getErrorXmlView(model, ApiConstant.CODE_DATA_ERROR, "不能操作他人订单！");
		}else {
			if(!StringUtils.equals(order.getUkey(), ukey)) return getErrorXmlView(model, ApiConstant.CODE_DATA_ERROR, "不能操作他人订单！");
		}
		ErrorCode<GewaOrder> code = ticketOrderService.removeDiscount(order, discountId);
		if(!code.isSuccess()) return getErrorXmlView(model, ApiConstant.CODE_DATA_ERROR, code.getMsg());
		return getSuccessXmlView(model);
	}
	
	/**
	 * 选择支付方式，返回对应支付跳转链接或者信息(手机客户端支付跳转API)
	 */
	@RequestMapping("/openapi/mobile/order/selectPayMethod.xhtml")
	public String selectPayMethod(String tradeNo, String ukey, String payMethod, String apptype, String appVersion, 
			HttpServletRequest request, ModelMap model){
		OpenApiAuth auth = OpenApiMobileAuthenticationFilter.getOpenApiAuth();
		if(StringUtils.isBlank(tradeNo)) return getErrorXmlView(model, ApiConstant.CODE_DATA_ERROR, "参数有误！");
		GewaOrder order = daoService.getObjectByUkey(GewaOrder.class, "tradeNo", tradeNo, false);
		if(order == null) return getErrorXmlView(model, ApiConstant.CODE_DATA_ERROR, "订单不存在！");
		if (order.isAllPaid() || order.isCancel()) return getErrorXmlView(model, ApiConstant.CODE_DATA_ERROR, "不能操作已支付或已（过时）取消的订单！");
		MemberInfo memberInfo = null;
		if(apiMobileService.isGewaPartner(auth.getApiUser().getId())){
			Member member = OpenApiMobileAuthenticationFilter.getOpenApiAuth().getMember();
			if(member==null){
				return getErrorXmlView(model, ApiConstant.CODE_DATA_ERROR, "获取登录信息失败，请刷新重试或重新登录！");
			}
			if(!order.getMemberid().equals(member.getId())) return getErrorXmlView(model, ApiConstant.CODE_DATA_ERROR, "不能操作他人订单！");
			memberInfo = daoService.getObject(MemberInfo.class, member.getId());
		}else {
			if(!StringUtils.equals(order.getUkey(), ukey)) return getErrorXmlView(model, ApiConstant.CODE_DATA_ERROR, "不能操作他人订单！");
		}
		if(order instanceof DramaOrder){
			DramaOrder dorder = (DramaOrder)order;
			OpenDramaItem dpi = daoService.getObjectByUkey(OpenDramaItem.class, "dpid", dorder.getDpid());
			ErrorCode<String> code = validExpress(dorder, dpi);
			if(!code.isSuccess()){
				return getErrorXmlView(model, ApiConstant.CODE_DATA_ERROR, code.getMsg());
			}
		}
		ApiUser partner = auth.getApiUser();
		Map paramsData = new LinkedHashMap();
		String[] paypair = StringUtils.split(payMethod, ":");
		String method = METHOD_GET;
		if(!PaymethodConstant.isValidPayMethod(paypair[0])) return getErrorXmlView(model, ApiConstant.CODE_DATA_ERROR, "支付方式错误！");
		String mainPaymethod = paypair[0];
		String paybank = paypair.length>1?paypair[1]:"";
		if((!StringUtils.equals(mainPaymethod, order.getPaymethod()) || !StringUtils.equals(paybank, order.getPaybank()))){ 
			ErrorCode code = paymentService.isAllowChangePaymethod(order, mainPaymethod, paybank);
			if(!code.isSuccess()) return getErrorXmlView(model, ApiConstant.CODE_DATA_ERROR, code.getMsg());
		}
		List<String> payServerList = paymentService.getPayserverMethodList();
		boolean isRemoveParams = true;
		if(payServerList.contains(mainPaymethod)){
			String otherinfo = GewaAppHelper.getOrderOther(mainPaymethod, order, getOpenMember(memberInfo));
			order.setOtherinfo(otherinfo);
			order.setPaybank(paybank);
			order.setPaymethod(payMethod);
			order.setStatus(OrderConstant.STATUS_NEW_CONFIRM);
			orderMonitorService.addOrderChangeLog(order.getTradeNo(), "API去支付", order, payMethod + ",host=" + Config.getServerIp());
			daoService.saveObject(order);
			String version = GewaAppHelper.getChinaSmartPayVersion(order, apptype, order.getPaymethod(), appVersion);
			paymentService.usePayServer(order, auth.getRemoteIp(), paramsData, version, request, model);
			isRemoveParams = false;
		}else {
			if(StringUtils.equals(mainPaymethod, PaymethodConstant.PAYMETHOD_PARTNERPAY)){
				paramsData = PartnerPayUtil.getNetPayParams(order, partner);
				order.setPaymethod(payMethod);
				model.put("payUrl", paramsData.remove("payurl")+"");
			}else{
				return getErrorXmlView(model, ApiConstant.CODE_DATA_ERROR, "支付方式错误！");
			}
		}
		if(isRemoveParams) model.put("method", method);
		model.put("paramsData", paramsData);
		model.put("tradeno", order.getTradeNo());
		model.put("discountAmount", order.getDiscount());
		model.put("totalAmount", order.getTotalAmount());
		model.put("due", order.getDue());
		order.setPaybank(paybank);
		order.setStatus(OrderConstant.STATUS_NEW_CONFIRM);
		orderMonitorService.addOrderChangeLog(order.getTradeNo(), "API去支付", order, payMethod + ",host=" + Config.getServerIp());
		daoService.saveObject(order);
		CallbackOrder corder = partnerSynchService.addCallbackOrder(order, PayConstant.PUSH_FLAG_NEW, false);
		if(corder!=null) partnerSynchService.pushCallbackOrder(corder);
		return getXmlView(model, "inner/mobile/selectPayMethod.vm");
	}
	
	private static final String METHOD_GET = "get";
	
	/**
	 * 使用积分
	 */
	@RequestMapping("/openapi/mobile/order/usePoint.xhtml")
	public String usePoint(String tradeNo, Integer pointvalue,ModelMap model){
		if(pointvalue==null){ 
			return getErrorXmlView(model, ApiConstant.CODE_DATA_ERROR, "积分值不能为空！");
		}
		GewaOrder order = daoService.getObjectByUkey(GewaOrder.class, "tradeNo", tradeNo, false);
		if(order == null) return getErrorXmlView(model, ApiConstant.CODE_DATA_ERROR, "订单不存在！");
		if (order.isAllPaid() || order.isCancel()) return getErrorXmlView(model, ApiConstant.CODE_DATA_ERROR, "不能操作已支付或已（过时）取消的订单！");
		Member member = OpenApiMobileAuthenticationFilter.getOpenApiAuth().getMember();
		if(!order.getMemberid().equals(member.getId())) return getErrorXmlView(model, ApiConstant.CODE_DATA_ERROR, "不能操作他人订单！");
		int amount = pointvalue/ConfigConstant.POINT_RATIO;
		if(order.getDue()<amount) {
			return getErrorXmlView(model, ApiConstant.CODE_DATA_ERROR, "你使用的积分大于实际需要的积分");
		}
		ErrorCode code = null;
		if(order instanceof TicketOrder){
			code = ticketDiscountService.usePoint(order.getId(), member.getId(), pointvalue);
		}else if(order instanceof SportOrder){
			code = sportOrderService.usePoint(order.getId(), member.getId(), pointvalue);
		}else if(order instanceof GoodsOrder){
			code = goodsOrderService.usePoint(order.getId(),  member.getId(), pointvalue);
		}else if(order instanceof DramaOrder){
			code = dramaOrderService.usePoint(order.getId(),  member.getId(), pointvalue);
		}else {
			return getErrorXmlView(model, ApiConstant.CODE_DATA_ERROR, "该订单类型不支持积分支付");
		}
		if(!code.isSuccess()) return getErrorXmlView(model, ApiConstant.CODE_DATA_ERROR, code.getMsg());
		return getSuccessXmlView(model);
	}
	
	/**
	 * 取消订单
	 */
	@RequestMapping("/openapi/mobile/order/cancelOrder.xhtml")
	public String cancel(Long orderid,String tradeNo,ModelMap model){
		GewaOrder order = null;
		if((orderid == null && StringUtils.isNotBlank(tradeNo))||(orderid != null && StringUtils.isNotBlank(tradeNo))){
			order = daoService.getObjectByUkey(GewaOrder.class, "tradeNo", tradeNo, false);
		}else if(orderid != null && StringUtils.isBlank(tradeNo)){
			order = daoService.getObject(GewaOrder.class, orderid);
		}else return getErrorXmlView(model, ApiConstant.CODE_DATA_ERROR, "参数有误！");
		if(order == null) return getErrorXmlView(model,ApiConstant.CODE_DATA_ERROR, "订单不存在！");
		Member member = OpenApiMobileAuthenticationFilter.getOpenApiAuth().getMember();
		if(!member.getId().equals(order.getMemberid())) return getErrorXmlView(model,ApiConstant.CODE_DATA_ERROR,"你无权限操作此订单！");
		Long memberid = member.getId();
		
		if(!order.isNew()) return getErrorXmlView(model, ApiConstant.CODE_DATA_ERROR, "不能取消该订单");
		if(order instanceof TicketOrder) {
			ticketOrderService.cancelTicketOrder2(order.getTradeNo(), memberid, OrderConstant.STATUS_USER_CANCEL, "用户取消");
		}else if(order instanceof SportOrder){
			SportOrder sorder = (SportOrder)order;
			sportUntransService.cancelSportOrder(sorder, memberid, "用户取消");
		}else if(order instanceof DramaOrder){
			theatreOrderService.cancelDramaOrder(tradeNo, memberid+"", "用户取消");
		}else if(order instanceof GoodsOrder){
			goodsOrderService.cancelGoodsOrder(tradeNo, member);
		}
		return getSuccessXmlView(model);
	}
	@RequestMapping("/openapi/mobile/order/deleteOrder.xhtml")
	public String deleteOrder( String tradeNo,ModelMap model){
		GewaOrder order = daoService.getObjectByUkey(GewaOrder.class, "tradeNo", tradeNo, false);
		if(order == null) return getErrorXmlView(model,ApiConstant.CODE_DATA_ERROR, "订单不存在！");
		Member member = OpenApiMobileAuthenticationFilter.getOpenApiAuth().getMember();
		if(!member.getId().equals(order.getMemberid())) return getErrorXmlView(model,ApiConstant.CODE_DATA_ERROR,"你无权限操作此订单！");
		ErrorCode<String> result = GewaOrderHelper.validDelGewaOrder(order, order.getPlaytime());
		if(!result.isSuccess()){
			return getErrorXmlView(model,ApiConstant.CODE_DATA_ERROR, result.getMsg()); 
		}
		order.setRestatus(GewaOrder.RESTATUS_DELETE);
		daoService.saveObject(order);
		return getSuccessXmlView(model);
	}
	
	@RequestMapping("/openapi/mobile/order/orderDetail.xhtml")
	public String orderDetail( String tradeNo, String appVersion, ModelMap model, HttpServletRequest request){
		GewaOrder order = daoService.getObjectByUkey(GewaOrder.class, "tradeNo", tradeNo, false);
		if(order == null) return getErrorXmlView(model,ApiConstant.CODE_DATA_ERROR, "订单不存在！");
		Member member = OpenApiMobileAuthenticationFilter.getOpenApiAuth().getMember();
		if(!member.getId().equals(order.getMemberid())) return getErrorXmlView(model,ApiConstant.CODE_DATA_ERROR,"你无权限操作此订单！");
		Map<String, Object> resMap = GewaApiOrderHelper.getOrderMap(order);
		if(order instanceof TicketOrder){
			boolean isNewVersion = appVersion.compareTo(AppConstant.MOVIE_APPVERSION_4_5)>=0;
			if(order.getItemfee()>0){
				BuyItem item = daoService.getObjectByUkey(BuyItem.class, "orderid", order.getId(), false);
				model.put("buyItem", item);
			}
			resMap.putAll(getMovieOrderMap((TicketOrder)order, isNewVersion));
			String root = "ticketOrder";
			model.put("root", root);
			initField(model, request);
			model.put("resMap", resMap);
			return getXmlView(model, "inner/mobile/ticketOrder.vm");
		}else if(order instanceof DramaOrder){
			resMap.putAll(getDramaOrderMap((DramaOrder)order));
			return getOpenApiXmlDetail(resMap, "dramaOrder", model, request);
		}else if(order instanceof GoodsOrder){
			GoodsOrder gorder = (GoodsOrder)order;
			BaseGoods baseGoods  = daoService.getObject(BaseGoods.class, gorder.getGoodsid());
			resMap.put("goodslogo", getMobilePath() + baseGoods.getLimg());
			if(baseGoods instanceof Goods){
				Cinema cinema = daoService.getObject(Cinema.class, baseGoods.getRelatedid());
				if(cinema!=null){
					resMap.put("placename", cinema.getName());
					resMap.putAll(getBaseInfoMap(cinema));
				}
			}
			resMap.put("goodstype", baseGoods.getGoodstype());
			resMap.put("goodstag", baseGoods.getTag());
			resMap.put("summary", baseGoods.getSummary());
			resMap.putAll(getGoodsOrderMap(gorder));
			return getOpenApiXmlDetail(resMap, "goodsOrder", model, request);
		}else {
			return getErrorXmlView(model,ApiConstant.CODE_DATA_ERROR,"订单类型不支持");
		}
	}
	
	/**
	 * 根据特价活动ID，获取计数器信息
	 */
	@RequestMapping("/openapi/mobile/order/spcounterDetail.xhtml")
	public String getSpcounterBySpids(String spids,HttpServletRequest request,  ModelMap model) {
		if (StringUtils.isBlank(spids)) {
			return getErrorXmlView(model,ApiConstant.CODE_DATA_ERROR,"spids不能为空");
		}
		List<Long> spidList = BeanUtil.getIdList(spids, ",");
		List<Spcounter> spcounterList = paymentService.getSpcounterBySpids(spidList);
		List<Map> resMapList = BeanUtil.getBeanMapList(spcounterList, false);
		model.put("resMapList", resMapList);
		initField(model, request);
		model.put("root", "spcounterList");
		model.put("nextroot", "spcounter");
		return getOpenApiXmlList(model);
	}
	/**
	 * 获取RSA签名
	 */
	@RequestMapping("/openapi/mobile/order/getRsaSign.xhtml")
	public String getRsaSign(String payMethod, String oristr, ModelMap model) {
		Map<String, String> params = new HashMap<String, String>();
		params.put("payMethod", payMethod);
		params.put("src", Base64.encodeBase64String(oristr.getBytes()));
		HttpResult result = HttpUtils.postUrlAsString(PayOtherUtil.getRsaSignUrl(), params);
		if(!result.isSuccess()){
			return getErrorXmlView(model,ApiConstant.CODE_DATA_ERROR, result.getMsg());
		}
		return getSingleResultXmlView(model, result.getResponse());
	}
	
	/**
	 * 获取财付通支付前置银行
	 */
	@RequestMapping("/openapi/mobile/order/tentPayPaybankList.xhtml")
	public String weixinPaybankList(String tradeNo, ModelMap model) {
		TicketOrder order = daoService.getObjectByUkey(TicketOrder.class,"tradeNo",tradeNo, false);
		if(order == null) return getErrorXmlView(model, ApiConstant.CODE_DATA_ERROR, "订单不存在！");
		if (order.isAllPaid() || order.isCancel()) return getErrorXmlView(model, ApiConstant.CODE_DATA_ERROR, "不能操作已支付或已（过时）取消的订单！");
		PayValidHelper valHelp = new PayValidHelper();
		if(order.getDiscount()>0){
			OpenPlayItem opi = daoService.getObjectByUkey(OpenPlayItem.class, "mpid", order.getMpid());
			valHelp = new PayValidHelper(VmUtils.readJsonToMap(opi.getOtherinfo()));
		}
		Map<String, String> map = new HashMap<String, String>();
		HttpResult result = HttpUtils.postUrlAsString(PayOtherUtil.getTenPayBankUrl(), map);
		if(!result.isSuccess()){
			return getErrorXmlView(model,ApiConstant.CODE_DATA_ERROR, result.getMsg());
		}
		BeanReader beanReader = ApiUtils.getBeanReader("root", TenBankList.class);
		TenBankList banks = (TenBankList)ApiUtils.xml2Object(beanReader, result.getResponse());
		List<TenBank> bankList = banks.getBanks();
		Map<String, List<TenBank>> banktypeMap = BeanUtil.groupBeanList(bankList, "type");
		model.put("banktypeMap", banktypeMap);
		model.put("valHelp", valHelp);
		return getXmlView(model, "inner/mobile/pay/tenBankList.vm");
	}
	
	/**
	 * 交易成功订单分享
	 */
	@RequestMapping("/openapi/mobile/order/spOrderShare.xhtml")
	public String spOrderShare(String tradeNo, String appVersion, ModelMap model) {
		GewaOrder order = daoService.getObjectByUkey(GewaOrder.class, "tradeNo", tradeNo, false);
		if(order == null) {
			return getErrorXmlView(model,ApiConstant.CODE_DATA_ERROR, "订单不存在！");
		}
		if(!order.isAllPaid()){
			return getErrorXmlView(model,ApiConstant.CODE_DATA_ERROR, "非交易成功的订单！");
		}
		if(appVersion.compareTo(AppConstant.MOVIE_APPVERSION_4_6)<=0){
			return getErrorXmlView(model,ApiConstant.CODE_DATA_ERROR, "不支持！");
		}
		Member member = OpenApiMobileAuthenticationFilter.getOpenApiAuth().getMember();
		if(!member.getId().equals(order.getMemberid())) {
			return getErrorXmlView(model,ApiConstant.CODE_DATA_ERROR,"你无权限操作此订单！");
		}
		String curtime = DateUtil.getCurFullTimestampStr();
		DBObject queryCondition = new BasicDBObject();
		DBObject relate2 = mongoService.queryAdvancedDBObject("starttime", new String[]{"<="}, new String[]{curtime});
		DBObject relate3 = mongoService.queryAdvancedDBObject("endtime", new String[]{">"}, new String[]{curtime});
		queryCondition.putAll(relate2);
		queryCondition.putAll(relate3);
		List<SpShare> shList = mongoService.getObjectList(SpShare.class, queryCondition, "addtime", false, 0, 300);
		List<SpShare> list = new ArrayList<SpShare>();
		Map<String, List<SpShare>> spMap = BeanUtil.groupBeanList(shList, "msgtype");
		for(String key : spMap.keySet()){
			SpShare spShare  = RandomUtil.getRandomObject(spMap.get(key));
			if(spShare!=null){
				list.add(spShare);
			}
		}
		List<Map<String, Object>> resMapList = new ArrayList<Map<String, Object>>();
		for(SpShare spShare : list){
			Map<String, Object> resMap = new HashMap();
			resMap.put("logo", getMobilePath() + spShare.getLogo());
			resMap.put("content", spShare.getContent());
			resMap.put("link", spShare.getLink());
			resMap.put("shortlink", spShare.getShortlink());
			resMap.put("msgtype", spShare.getMsgtype());
			resMap.put("title", spShare.getTitle());
			resMapList.add(resMap);
		}
		model.put("resMapList", resMapList);
		return getXmlView(model, "inner/mobile/orderShare.vm");
	}
}
