package com.gewara.web.action.api2mobile.goods;

import java.util.Collections;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.support.PropertyComparator;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import com.gewara.constant.ApiConstant;
import com.gewara.model.api.ApiUser;
import com.gewara.model.goods.BaseGoods;
import com.gewara.model.goods.GoodsPrice;
import com.gewara.model.goods.TicketGoods;
import com.gewara.model.pay.GoodsOrder;
import com.gewara.model.user.Member;
import com.gewara.service.order.GoodsService;
import com.gewara.support.ErrorCode;
import com.gewara.untrans.ticket.TicketGoodsService;
import com.gewara.util.ValidateUtil;
import com.gewara.web.action.inner.mobile.BaseOpenApiMobileController;
import com.gewara.web.filter.NewApiAuthenticationFilter;
@Controller
public class Api2GoodsController extends BaseOpenApiMobileController{
	@Autowired@Qualifier("ticketGoodsService")
	private TicketGoodsService ticketGoodsService;
	@Autowired@Qualifier("goodsService")
	protected GoodsService goodsService;
	@RequestMapping("/api2/mobile/goods/getGoodsPriceList.xhtml")
	public String price8(Long goodsid, ModelMap model, HttpServletRequest request) {
		List<GoodsPrice> goodsPriceList = goodsService.getGoodsPriceList(goodsid);
		Collections.sort(goodsPriceList, new PropertyComparator("price", false, true));
		getGoodsPriceListMap(goodsPriceList, model, request);
		model.put("root", "goodsPriceList");
		model.put("nextroot", "goodsPrice");
		return getXmlView(model, "inner/partner/list.vm");
	}
	@RequestMapping("/api2/mobile/goods/addTicketGoodsOrder.xhtml")
	public String addSportOrder(String memberEncode, Long goodsid, String mobile, 
			Long priceid, Integer quantity,ModelMap model, HttpServletRequest request){
		if(!ValidateUtil.isMobile(mobile)) return getErrorXmlView(model, ApiConstant.CODE_DATA_ERROR, "手机号有错误！");
		Member member = memberService.getMemberByEncode(memberEncode);
		if(member == null) return getErrorXmlView(model, ApiConstant.CODE_SIGN_ERROR, "用户不存在！");
		ApiUser partner = NewApiAuthenticationFilter.getApiAuth().getApiUser();
		TicketGoods ticketGoods = daoService.getObject(TicketGoods.class, goodsid);
		if(ticketGoods == null) return getErrorXmlView(model, ApiConstant.CODE_DATA_ERROR, "场次不存在或删除！");
		ErrorCode<GoodsOrder> code = ticketGoodsService.addTicketGoodsOrder(ticketGoods, member, mobile, quantity, null, priceid, partner, String.valueOf(member.getId()));
		if(!code.isSuccess()) return getErrorXmlView(model, ApiConstant.CODE_DATA_ERROR, code.getMsg());
		getGoodsOrderData(code.getRetval(), model, request);
		model.put("root", "goodsOrder");
		return getXmlView(model, "inner/partner/detail.vm");
	}
	
	@RequestMapping("/api2/mobile/goods/getGoodsOrderDetail.xhtml")
	public String addSportOrder(String memberEncode, String tradeno, ModelMap model, HttpServletRequest request){
		Member member = memberService.getMemberByEncode(memberEncode);
		if(member == null) return getErrorXmlView(model, ApiConstant.CODE_SIGN_ERROR, "用户不存在！");
		GoodsOrder order = daoService.getObjectByUkey(GoodsOrder.class, "tradeNo", tradeno, false);
		if(order==null) return getErrorXmlView(model, ApiConstant.CODE_DATA_ERROR, "订单不存在!");
		if(!order.getMemberid().equals(member.getId())) return getErrorXmlView(model, ApiConstant.CODE_DATA_ERROR, "不能他人订单!");
		getGoodsOrderData(order, model, request);
		model.put("root", "goodsOrder");
		return getXmlView(model, "inner/partner/detail.vm");
	}
	
	@RequestMapping("/api2/mobile/goods/getGoodsDetail.xhtml")
	public String addSportOrder(Long goodsid, ModelMap model, HttpServletRequest request){
		BaseGoods goods = daoService.getObject(BaseGoods.class, goodsid);
		if(goods==null) return getErrorXmlView(model, ApiConstant.CODE_DATA_ERROR, "物品不存在!");
		getGoodsData(goods, model, request);
		model.put("root", "goods");
		return getXmlView(model, "inner/partner/detail.vm");
	}
}
