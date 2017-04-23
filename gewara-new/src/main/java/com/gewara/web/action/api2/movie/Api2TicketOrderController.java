package com.gewara.web.action.api2.movie;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import com.gewara.constant.ApiConstant;
import com.gewara.constant.GoodsConstant;
import com.gewara.constant.ticket.OpiConstant;
import com.gewara.constant.ticket.PartnerConstant;
import com.gewara.helper.GoodsFilterHelper;
import com.gewara.model.api.ApiUser;
import com.gewara.model.goods.Goods;
import com.gewara.model.goods.GoodsGift;
import com.gewara.model.pay.BuyItem;
import com.gewara.model.pay.GewaOrder;
import com.gewara.model.pay.TicketOrder;
import com.gewara.model.ticket.OpenPlayItem;
import com.gewara.model.user.Member;
import com.gewara.pay.PartnerPayUtil;
import com.gewara.service.order.GoodsService;
import com.gewara.service.order.OrderQueryService;
import com.gewara.support.ErrorCode;
import com.gewara.untrans.ticket.TicketRollCallService;
import com.gewara.util.JsonUtils;
import com.gewara.web.action.api.ApiAuth;
import com.gewara.web.action.api2mobile.ApiTicketBaseController;
import com.gewara.web.filter.NewApiAuthenticationFilter;

@Controller
public class Api2TicketOrderController extends ApiTicketBaseController{
	@Autowired@Qualifier("ticketRollCallService")
	private TicketRollCallService ticketRollCallService;
	
	@Autowired@Qualifier("orderQueryService")
	private OrderQueryService orderQueryService;

	@Autowired@Qualifier("goodsService")
	private GoodsService goodsService;

	/**
	 * 获取场次座位信息(场次座位信息)
	 */
	@RequestMapping("/api2/order/opiSeatInfo.xhtml")
	public String getOpiSeatList(Long mpid,ModelMap model){
		ApiAuth auth = NewApiAuthenticationFilter.getApiAuth();
		ApiUser partner = auth.getApiUser();
		if(mpid==null) return getErrorXmlView(model, ApiConstant.CODE_PARAM_ERROR, "参数不正确！");
		OpenPlayItem opi = daoService.getObjectByUkey(OpenPlayItem.class, "mpid", mpid, true);
		if(opi == null) return getErrorXmlView(model, ApiConstant.CODE_OPI_NOT_EXISTS, "场次不存在或已删除！");
		Map<String,String> otherInfo = JsonUtils.readJsonToMap(opi.getOtherinfo());
		String mealoption = otherInfo.get(OpiConstant.MEALOPTION);
		if(!StringUtils.equals(mealoption, "notuse")){
			GoodsGift goodsGift = goodsOrderService.getBindGoodsGift(opi, auth.getApiUser().getId());
			if(goodsGift!=null) {
				Goods goods = daoService.getObject(Goods.class, goodsGift.getGoodsid());
				model.put("goodsGift", goodsGift);
				model.put("bindGoods", goods);
			}else {
				List<Goods> goodsList = goodsService.getCurGoodsList(Goods.class, GoodsConstant.GOODS_TAG_BMH, opi.getCinemaid(), 0, 5);
				GoodsFilterHelper.goodsFilter(goodsList, auth.getApiUser().getId());
				model.put("optionalGoods", goodsList);
			}
		}
		ErrorCode code = addOpiSeatListData(opi, partner, model);
		if(!code.isSuccess()) return getErrorXmlView(model, code.getErrcode(), code.getMsg());
		return getXmlView(model, "api2/order/opiSeatInfo.vm");
	}
	/**
	 * 获取场次座位信息
	 */
	@RequestMapping("/api2/order/opiLockedSeat.xhtml")
	public String getOpiLockedSeatList(Long mpid, ModelMap model){
		ApiAuth auth = NewApiAuthenticationFilter.getApiAuth();
		if(mpid==null) return getErrorXmlView(model, ApiConstant.CODE_PARAM_ERROR, "参数不正确！");
		ApiUser partner = auth.getApiUser();
		OpenPlayItem opi = daoService.getObjectByUkey(OpenPlayItem.class, "mpid", mpid, true);
		if(opi == null) return getErrorXmlView(model, ApiConstant.CODE_OPI_NOT_EXISTS, "场次不存在或已删除！");
		if(StringUtils.contains(opi.getOtherinfo(), OpiConstant.ADDRESS) && partner.getId() >= PartnerConstant.GEWA_CLIENT){
			return getErrorXmlView(model, ApiConstant.CODE_OPI_CLOSED, "本场不接受（或暂停）预订！");
		}
		GoodsGift goodsGift = goodsOrderService.getBindGoodsGift(opi, partner.getId());
		if(goodsGift!=null){
			if(partner.getId() < PartnerConstant.GEWA_CLIENT){//Gewara商户
				model.put("goodsGift", goodsGift);
				Goods goods = daoService.getObject(Goods.class, goodsGift.getGoodsid());
				model.put("goods", goods);
			}else{
				return getErrorXmlView(model, ApiConstant.CODE_OPI_CLOSED, "本场不接受（或暂停）预订！");
			}
		}
		ErrorCode code = addOpiLockedSeatListData(opi, partner, model);
		if(!code.isSuccess()) return getErrorXmlView(model, code.getErrcode(), code.getMsg()); 
		return getXmlView(model, "api/order/opiLockedSeat.vm");
	}

	/**
	 * 增加订单（Gewa）(订票下订单API)
	 * @param key
	 * @param encryptCode
	 * @param memberEncode
	 * @param mpid
	 * @param mobile
	 * @param seatLabel
	 * @param model
	 * @return
	 */
	@RequestMapping("/api2/order/addTicketOrder.xhtml")
	public String addTicketOrder(HttpServletRequest request, String memberEncode, String ukey, String origin, String apptype,
			Long mpid, String mobile, String seatLabel, Long goodsid, Integer quantity, String paymethod, String paybank, ModelMap model){
		ApiAuth auth = NewApiAuthenticationFilter.getApiAuth();
		ApiUser partner = auth.getApiUser();
		Member member = null;
		Long memberid = null;
		if(StringUtils.isNotBlank(memberEncode)){
			member = memberService.getMemberByEncode(memberEncode);
			if(member == null) return getErrorXmlView(model, ApiConstant.CODE_DATA_ERROR, "请重新登录！");
			memberid = member.getId();
			ukey = memberid+"";
		}else{
			return getErrorXmlView(model, ApiConstant.CODE_DATA_ERROR, "请重新登录！");
		}
		OpenPlayItem opi = daoService.getObjectByUkey(OpenPlayItem.class, "mpid", mpid, true);
		if(ticketRollCallService.isTicketRollCallMember(member.getId(), mobile)){
			return getErrorXmlView(model, ApiConstant.CODE_DATA_ERROR, "你的帐号购票受限，请联系客服：4000-406-506！");
		}
		ErrorCode code = addTicketOrder(partner, member, memberid, ukey, opi, mobile, seatLabel, paymethod, paybank, model,goodsid,quantity);
		if(!code.isSuccess()) return getErrorXmlView(model, code.getErrcode(), code.getMsg());
		TicketOrder order = (TicketOrder)model.get("order");
		logAppSourceOrder(request, order, apptype, origin);
		return getXmlView(model, "api/order/order.vm");
	}
	/**
	 * 增加订单（Partner）
	 * @param key
	 * @param encryptCode
	 * @param mpid
	 * @param mobile
	 * @param ukey
	 * @param seatLabel
	 * @param model
	 * @return
	 */
	@RequestMapping("/api2/order/addPartnerOrder.xhtml")
	public String addPartnerOrder(Long mpid, String mobile, String ukey, String seatLabel, String apptype,
			String resv, String paymethod, String paybank, HttpServletRequest request, ModelMap model){
		//TODO: hangzhouapp
		ApiAuth auth = NewApiAuthenticationFilter.getApiAuth();
		ApiUser partner = auth.getApiUser();
		if(StringUtils.isNotBlank(resv)) model.put("resv", resv);
		OpenPlayItem opi = daoService.getObjectByUkey(OpenPlayItem.class, "mpid", mpid, true);
		ErrorCode code = addTicketOrder(partner, null, partner.getId(), StringUtils.isBlank(ukey)? StringUtils.reverse(partner.getId()+mobile):ukey, opi, mobile, seatLabel, paymethod, paybank, model,null,null);
		if(!code.isSuccess()) return getErrorXmlView(model, code.getErrcode(), code.getMsg());
		logAppSourceOrder(request, (GewaOrder)model.get("order"), apptype, null);
		return getXmlView(model, "api/order/order.vm");
	}
	
	@RequestMapping("/api2/order/qryPartnerOrder.xhtml")
	public String qryPartnerOrder(String tradeno, ModelMap model){
		//TODO: hangzhouapp
		ApiAuth auth = NewApiAuthenticationFilter.getApiAuth();
		TicketOrder order = daoService.getObjectByUkey(TicketOrder.class, "tradeNo", tradeno, false);
		if(order==null) return getErrorXmlView(model, ApiConstant.CODE_DATA_ERROR, "订单不存在!");
		if(!order.getPartnerid().equals(auth.getApiUser().getId())) return getErrorXmlView(model, ApiConstant.CODE_DATA_ERROR, "不能查询其他商家订单!");
		OpenPlayItem opi = daoService.getObjectByUkey(OpenPlayItem.class, "mpid", order.getMpid(), true);
		if(order.getItemfee()>0){//商家无赠品
			//TODO:重新处理，多个BuyItem
			BuyItem item = daoService.getObjectByUkey(BuyItem.class, "orderid", order.getId());
			if(item!=null) {
				Goods goods = daoService.getObject(Goods.class, item.getRelatedid());
				model.put("goods", goods);
			}
		}
		model.put("partner", auth.getApiUser());
		model.put("payUrl", PartnerPayUtil.SHORT_NOTIFY_URL);
		model.put("order", order);
		model.put("opi", opi);
		model.put("orderStatus", ApiConstant.getMappedOrderStatus(order.getFullStatus()));
		return getXmlView(model, "api/order/order.vm");
	}
	
	/**
	 * 订单详情
	 * @param request
	 * @param memberEncode
	 * @param tradeno
	 * @param model
	 * @return
	 */
	@RequestMapping("/api2/order/qryTicketOrder.xhtml")
	public String queryTicketOrder(HttpServletRequest request, String memberEncode, String tradeno, ModelMap model){
		Member member = memberService.getMemberByEncode(memberEncode);
		if(member == null) return getErrorXmlView(model, ApiConstant.CODE_DATA_ERROR, "身份验证错误");
		TicketOrder order = daoService.getObjectByUkey(TicketOrder.class, "tradeNo", tradeno, false);
		OpenPlayItem opi = daoService.getObjectByUkey(OpenPlayItem.class, "mpid", order.getMpid(), true);
		model.put("order", order);
		model.put("opi", opi);
		model.put("orderStatus", ApiConstant.getMappedOrderStatus(order.getFullStatus()));
		return getXmlView(model, "api/order/order.vm");
	}

	@RequestMapping("/api2/common/getTicketValidTime.xhtml")
	public String getTicketHelp(String tradeNo, ModelMap model){
		Long valid = orderQueryService.getOrderValidTime(tradeNo);
		if(valid==null){
			return getErrorXmlView(model,ApiConstant.CODE_SIGN_ERROR,"有效时间已过");
		}
		Long cur = System.currentTimeMillis();
		Long remain = valid - cur;
		model.put("remain", remain);
		return getXmlView(model,"api/order/orderValidTime.vm");
	}
}