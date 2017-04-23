package com.gewara.web.action.inner.mobile.order;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.hibernate.type.StringType;
import org.hibernate.type.Type;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import com.gewara.constant.ApiConstant;
import com.gewara.constant.PaymethodConstant;
import com.gewara.constant.TagConstant;
import com.gewara.constant.app.AppConstant;
import com.gewara.constant.ticket.OrderConstant;
import com.gewara.model.api.ApiUser;
import com.gewara.model.drama.DramaOrder;
import com.gewara.model.goods.BaseGoods;
import com.gewara.model.goods.Goods;
import com.gewara.model.movie.Cinema;
import com.gewara.model.pay.BuyItem;
import com.gewara.model.pay.GewaOrder;
import com.gewara.model.pay.GoodsOrder;
import com.gewara.model.pay.TicketOrder;
import com.gewara.model.ticket.OpenPlayItem;
import com.gewara.model.user.Member;
import com.gewara.support.ErrorCode;
import com.gewara.untrans.ticket.TicketRollCallService;
import com.gewara.util.DateUtil;
import com.gewara.web.action.inner.OpenApiAuth;
import com.gewara.web.action.inner.mobile.BaseOpenApiMobileController;
import com.gewara.web.filter.OpenApiMobileAuthenticationFilter;

@Controller
public class OpenApiMobileTicketOrderController extends BaseOpenApiMobileController{
	@Autowired@Qualifier("ticketRollCallService")
	private TicketRollCallService ticketRollCallService;
	/**
	 * 下电影订单
	 */
	@RequestMapping("/openapi/mobile/order/addTicketOrder.xhtml")
	public String addTicketOrder(String memberEncode, HttpServletRequest request,  String ukey, String origin,
			Long mpid, String mobile, String seatLabel, Long goodsid, Integer quantity, String paymethod, String paybank, ModelMap model){
		OpenApiAuth auth = OpenApiMobileAuthenticationFilter.getOpenApiAuth();
		ApiUser partner = auth.getApiUser();
		Member member = null;
		Long memberid = null;
		if(StringUtils.isNotBlank(memberEncode)){
			member = OpenApiMobileAuthenticationFilter.getOpenApiAuth().getMember();
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
		logAppSourceOrder(request, order, TagConstant.TAG_CINEMA, origin);
		return getSingleResultXmlView(model, order.getTradeNo());
	}
	
	private <T extends GewaOrder> List<T> getOrderListByMemberId(Class<T> clazz, Long memberId, Criterion cri, int days, int from, int maxnum) {
		Timestamp cur = new Timestamp(System.currentTimeMillis());
		Timestamp qtime = DateUtil.addDay(cur, - days);
		DetachedCriteria query = DetachedCriteria.forClass(clazz);
		query.add(Restrictions.eq("memberid", memberId));
		query.add(Restrictions.or(Restrictions.and(
						Restrictions.like("status", OrderConstant.STATUS_NEW, MatchMode.START), 
						Restrictions.gt("validtime", cur)), 
					Restrictions.like("status", OrderConstant.STATUS_PAID, MatchMode.START)));
		query.add(Restrictions.or(Restrictions.isNull("restatus"), Restrictions.ne("restatus", GewaOrder.RESTATUS_DELETE)));
		query.add(Restrictions.ne("paymethod", PaymethodConstant.PAYMETHOD_SYSPAY));
		query.add(Restrictions.ge("addtime", qtime));
		query.add(cri);
		query.addOrder(Order.desc("addtime"));
		
		List<T> result = hibernateTemplate.findByCriteria(query, from, maxnum);
		return result;
	}
	/**
	 * 我的电影、物品、活动订单
	 */
	@RequestMapping("/openapi/mobile/order/myTicketOrderList.xhtml")
	public String orderList(String ordertype,  Integer from,Integer maxnum, String appVersion, ModelMap model, HttpServletRequest request){
		if(maxnum>50) maxnum = 50;
		if(StringUtils.isBlank(ordertype)) ordertype = OrderConstant.ORDER_TYPE_TICKET;
		String[] objs = StringUtils.split(ordertype, ",");
		String[] params = new String[objs.length];
		Type[] type = new Type[objs.length];
		for(int i = 0; i<objs.length;i++){
			type[i] = new StringType();
			params[i] = "?";
		}
		Criterion cri = Restrictions.sqlRestriction("order_type in(" + StringUtils.join(params, ",") + ")", objs, type);
		
		Member member = OpenApiMobileAuthenticationFilter.getOpenApiAuth().getMember();
		List<GewaOrder> ticketOrderList = getOrderListByMemberId(GewaOrder.class, member.getId(), cri, 360, from, maxnum);
		Map<Long, BuyItem> buyItemMap = new HashMap<Long, BuyItem>();
		Map<String, Map<String, Object>> orderMap = new HashMap<String, Map<String, Object>>();
		if(ticketOrderList!=null){
			boolean isNewVersion = appVersion.compareTo(AppConstant.MOVIE_APPVERSION_4_5)>=0;
			for(GewaOrder order : ticketOrderList){
				if(order.getItemfee()>0){
					BuyItem item = daoService.getObjectByUkey(BuyItem.class, "orderid", order.getId(), false);
					if(item!=null) buyItemMap.put(order.getId(), item);
				}
				Map<String, Object> resMap = null;
				if(order instanceof TicketOrder){
					resMap = getMovieOrderMap((TicketOrder)order, isNewVersion);
				}else if(order instanceof GoodsOrder){
					GoodsOrder gorder = (GoodsOrder)order;
					resMap = getGoodsOrderMap(gorder);
					BaseGoods goods = daoService.getObject(BaseGoods.class, gorder.getGoodsid());
					if(goods!=null) resMap.put("shortname", goods.getShortname());
					resMap.put("goodslogo", getMobilePath() + goods.getLimg());
					if(goods instanceof Goods){
						Cinema cinema = daoService.getObject(Cinema.class, goods.getRelatedid());
						if(cinema!=null){
							resMap.put("placename", cinema.getName());
							resMap.putAll(getBaseInfoMap(cinema));
						}
					}
				}else if(order instanceof DramaOrder){
					DramaOrder dorder = (DramaOrder)order;
					resMap = getDramaOrderMap(dorder);
				}
				orderMap.put(order.getTradeNo(), resMap);
			}
		}
		initField(model, request);
		model.put("orderList", ticketOrderList);
		model.put("orderMap", orderMap);
		model.put("buyItemMap", buyItemMap);
		return getXmlView(model, "inner/mobile/ticketOrderList.vm");
	}
}
