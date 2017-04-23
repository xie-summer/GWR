package com.gewara.web.action.inner.mobile.goods;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.support.PropertyComparator;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import com.gewara.constant.ApiConstant;
import com.gewara.constant.GoodsConstant;
import com.gewara.constant.app.AppConstant;
import com.gewara.helper.GoodsFilterHelper;
import com.gewara.model.api.ApiUser;
import com.gewara.model.goods.ActivityGoods;
import com.gewara.model.goods.BaseGoods;
import com.gewara.model.goods.Goods;
import com.gewara.model.goods.GoodsPrice;
import com.gewara.model.goods.TicketGoods;
import com.gewara.model.pay.GoodsOrder;
import com.gewara.model.user.Member;
import com.gewara.service.OrderException;
import com.gewara.service.order.GoodsService;
import com.gewara.support.ErrorCode;
import com.gewara.untrans.ticket.TicketGoodsService;
import com.gewara.util.DateUtil;
import com.gewara.util.ValidateUtil;
import com.gewara.web.action.inner.mobile.BaseOpenApiMobileController;
import com.gewara.web.filter.OpenApiMobileAuthenticationFilter;
@Controller
public class OpenApiMobileGoodsController extends BaseOpenApiMobileController {
	@Autowired@Qualifier("goodsService")
	private GoodsService goodsService;
	@Autowired@Qualifier("ticketGoodsService")
	private TicketGoodsService ticketGoodsService;
	/**
	 * 获取影院能预定的套餐
	 */
	@RequestMapping("/openapi/mobile/goods/getMealListByCinemaid.xhtml")
	public String getMealListByCinemaid(Long cinemaid, ModelMap model, HttpServletRequest request) {
		ApiUser partner = OpenApiMobileAuthenticationFilter.getOpenApiAuth().getApiUser();
		List<Goods> goodsList = goodsService.getGoodsList(Goods.class, GoodsConstant.GOODS_TAG_BMH, cinemaid, true, true, true, "goodssort", true, false);
		GoodsFilterHelper.goodsFilter(goodsList, partner.getId());
		List<Map<String, Object>> resMapList = new ArrayList<Map<String, Object>>();
		for(BaseGoods goods : goodsList){
			Map<String, Object> resMap = getGoodsMap(goods);
			resMapList.add(resMap);
		}
		initField(model, request);
		putGoodsListNode(model);
		model.put("resMapList", resMapList);
		return getOpenApiXmlList(model);
	}
	
	/**
	 * 影院套餐详细
	 */
	@RequestMapping("/openapi/mobile/goods/goodsDetail.xhtml")
	public String goodsDetail(Long goodsid, ModelMap model, HttpServletRequest request) {
		Goods goods = daoService.getObject(Goods.class, goodsid);
		if(goods==null) return getErrorXmlView(model, ApiConstant.CODE_DATA_ERROR, "数据不存在");
		Map<String, Object> resMap = getGoodsMap(goods);
		initField(model, request);
		putGoodsNode(model);
		model.put("resMap", resMap);
		return getOpenApiXmlDetail(model);
	}
	
	/**
	 * 创建套餐订单
	 */
	@RequestMapping("/openapi/mobile/goods/addGoodsOrder.xhtml")
	public String addGoodsOrder(Long goodsid, Integer quantity, String mobile, ModelMap model) {
		Goods goods = daoService.getObject(Goods.class, goodsid);
		if(goods==null) return getErrorXmlView(model, ApiConstant.CODE_DATA_ERROR, "数据不存在");
		ApiUser partner = OpenApiMobileAuthenticationFilter.getOpenApiAuth().getApiUser();
		Member member = OpenApiMobileAuthenticationFilter.getOpenApiAuth().getMember();
		GoodsOrder order;
		try {
			order = goodsOrderService.addGoodsOrder(goods, member, mobile, quantity, null, partner);
		} catch (OrderException e) {
			e.printStackTrace();
			return getErrorXmlView(model, ApiConstant.CODE_DATA_ERROR, e.getMessage());
		}
		return getSingleResultXmlView(model, order.getTradeNo());
	}
	
	@RequestMapping("/openapi/mobile/goods/getGoodsPriceList.xhtml")
	public String price8(Long goodsid, ModelMap model, HttpServletRequest request) {
		List<GoodsPrice> goodsPriceList = goodsService.getGoodsPriceList(goodsid);
		Collections.sort(goodsPriceList, new PropertyComparator("price", false, true));
		getGoodsPriceListMap(goodsPriceList, model, request);
		model.put("root", "goodsPriceList");
		model.put("nextroot", "goodsPrice");
		return getXmlView(model, "inner/partner/list.vm");
	}
	@RequestMapping("/openapi/mobile/goods/addTicketGoodsOrder.xhtml")
	public String addSportOrder(Long goodsid, String mobile, 
			Long priceid, Integer quantity,ModelMap model, HttpServletRequest request){
		if(!ValidateUtil.isMobile(mobile)) return getErrorXmlView(model, ApiConstant.CODE_DATA_ERROR, "手机号有错误！");
		Member member = OpenApiMobileAuthenticationFilter.getOpenApiAuth().getMember();
		if(member == null) return getErrorXmlView(model, ApiConstant.CODE_SIGN_ERROR, "用户不存在！");
		ApiUser partner = OpenApiMobileAuthenticationFilter.getOpenApiAuth().getApiUser();
		TicketGoods ticketGoods = daoService.getObject(TicketGoods.class, goodsid);
		if(ticketGoods == null) return getErrorXmlView(model, ApiConstant.CODE_DATA_ERROR, "场次不存在或删除！");
		ErrorCode<GoodsOrder> code = ticketGoodsService.addTicketGoodsOrder(ticketGoods, member, mobile, quantity, null, priceid, partner, String.valueOf(member.getId()));
		if(!code.isSuccess()) return getErrorXmlView(model, ApiConstant.CODE_DATA_ERROR, code.getMsg());
		getGoodsOrderData(code.getRetval(), model, request);
		model.put("root", "goodsOrder");
		return getXmlView(model, "inner/partner/detail.vm");
	}
	@RequestMapping("/openapi/mobile/goods/getActivityGoodsList.xhtml")
	public String getActivityGoodsList(Long activityid, String appVersion, ModelMap model, HttpServletRequest request){
		List<ActivityGoods> goodsList = daoService.getObjectListByField(ActivityGoods.class, "relatedid", activityid);
		List<Map<String, Object>> resMapList = new ArrayList<Map<String, Object>>();
		for(ActivityGoods goods : goodsList){
			if(isBooingActivityGoods(goods, appVersion)){
				Map<String, Object> resMap = getGoodsMap(goods);
				if(appVersion.compareTo(AppConstant.MOVIE_APPVERSION_4_6)>=0){
					int booking = 0;
					String unbookingReason = "";
					ErrorCode code = GoodsConstant.getBookingStatusStr(goods);
					if(code.isSuccess()){
						booking = 1;
					}else {
						unbookingReason = code.getMsg();
					}
					resMap.put("unbookingReason", unbookingReason);
					resMap.put("booking", booking);
				}
				resMapList.add(resMap);
			}
		}
		initField(model, request);
		putGoodsListNode(model);
		model.put("resMapList", resMapList);
		return getOpenApiXmlList(model);
	}
	
	@RequestMapping("/openapi/mobile/goods/payActivityGoodsInfo.xhtml")
	public String payActivityGoodsInfo(Long goodsid, ModelMap model, HttpServletRequest request){
		ActivityGoods goods = daoService.getObject(ActivityGoods.class, goodsid);
		if(goods==null){
			return getErrorXmlView(model, ApiConstant.CODE_DATA_ERROR, "数据不存在！");
		}
		Map<String, Object> resMap = getGoodsMap(goods);
		List<String> formList = new ArrayList();
		formList.add("realname");
		formList.add("mobile");
		if(goods.isNeedDeliver()){
			formList.add("address");
		}
		resMap.put("formInput", StringUtils.join(formList, ","));
		initField(model, request);
		model.put("root", "goods");
		model.put("resMap", resMap);
		return getOpenApiXmlDetail(model);
	}
	@RequestMapping("/openapi/mobile/goods/addActivityGoodsOrder.xhtml")
	public String addActivityGoodsOrder(Long goodsid, String realname, String address, String mobile, Integer quantity, ModelMap model){
		ActivityGoods goods = daoService.getObject(ActivityGoods.class, goodsid);
		if(goods==null){
			return getErrorXmlView(model, ApiConstant.CODE_DATA_ERROR, "数据不存在！");
		}
		ApiUser partner  = OpenApiMobileAuthenticationFilter.getOpenApiAuth().getApiUser();
		Member member = OpenApiMobileAuthenticationFilter.getOpenApiAuth().getMember();
		ErrorCode<GoodsOrder> code = goodsOrderService.addActivityGoodsOrder(goods, member, partner, mobile, quantity, realname, address, DateUtil.getMillTimestamp());
		if(!code.isSuccess()){
			return getErrorXmlView(model, ApiConstant.CODE_DATA_ERROR, code.getMsg());
		}
		return getSingleResultXmlView(model, code.getRetval().getTradeNo());
	}
}
