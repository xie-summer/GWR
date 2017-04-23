package com.gewara.web.action.admin.gewapay;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.criterion.Subqueries;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.support.PropertyComparator;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import com.gewara.constant.ChargeConstant;
import com.gewara.constant.sys.JsonDataKey;
import com.gewara.constant.ticket.PartnerConstant;
import com.gewara.model.common.JsonData;
import com.gewara.model.pay.Adjustment;
import com.gewara.model.pay.BaseOrderExtra;
import com.gewara.model.pay.BuyItem;
import com.gewara.model.pay.Charge;
import com.gewara.model.pay.Discount;
import com.gewara.model.pay.GewaOrder;
import com.gewara.model.pay.GoodsOrder;
import com.gewara.model.pay.MemberAccount;
import com.gewara.model.pay.OrderExtra;
import com.gewara.model.pay.OrderExtraHis;
import com.gewara.model.pay.TicketOrder;
import com.gewara.model.ticket.OpenPlayItem;
import com.gewara.model.user.InvoiceRelate;
import com.gewara.model.user.Member;
import com.gewara.service.gewapay.PaymentService;
import com.gewara.support.ServiceHelper;
import com.gewara.util.BeanUtil;
import com.gewara.util.JsonUtils;
import com.gewara.web.action.admin.BaseAdminController;
@Controller
public class PayAdminController extends BaseAdminController{
	@Autowired@Qualifier("paymentService")
	private PaymentService paymentService;
	public void setPaymentService(PaymentService paymentService) {
		this.paymentService = paymentService;
	}
	@RequestMapping("/admin/gewapay/couponOrderList.xhtml")
	public String couponOrderList(Long cid, Timestamp timeFrom, Timestamp timeTo, String status, String mobile, String tradeNo, ModelMap model){
		String qry = "select d.orderid from Discount d where d.relatedid=?";
		List<Long> orderidList = hibernateTemplate.find(qry, cid);
		List<TicketOrder> orderList = new ArrayList<TicketOrder>();
		if(orderidList.size() > 0) {
			DetachedCriteria query = DetachedCriteria.forClass(TicketOrder.class, "t");
			
			DetachedCriteria disQuery = DetachedCriteria.forClass(Discount.class, "d");
			disQuery.add(Restrictions.eq("d.relatedid", cid));
			disQuery.add(Restrictions.eqProperty("d.orderid", "t.id"));
			disQuery.setProjection(Projections.property("d.id"));
			query.add(Subqueries.exists(disQuery));
			
			if(StringUtils.isNotBlank(status)) query.add(Restrictions.eq("t.status", status));
			if(timeFrom!=null) query.add(Restrictions.ge("t.addtime", timeFrom));
			if(timeTo!=null) query.add(Restrictions.le("t.addtime", timeTo));
			if(StringUtils.isNotBlank(mobile)) query.add(Restrictions.eq("t.mobile", mobile));
			if(StringUtils.isNotBlank(tradeNo)) query.add(Restrictions.eq("t.tradeNo", tradeNo));
			query.addOrder(Order.desc("t.addtime"));
			orderList = hibernateTemplate.findByCriteria(query);
		}
		List<Long> memberidList = ServiceHelper.getMemberIdListFromBeanList(orderList);
		Map<Long, Member> memberMap = daoService.getObjectMap(Member.class, memberidList);
		model.put("memberMap", memberMap);
		model.put("orderList", orderList);
		model.put("cid", cid);
		return "admin/gewapay/couponOrderList.vm";
	}
	//送货
	@RequestMapping("/admin/gewapay/goodsDeliver.xhtml")
	public String goodsDeliver(Long orderId, ModelMap model){
		GoodsOrder order = daoService.getObject(GoodsOrder.class, orderId);
		order.setDescription2(JsonUtils.addJsonKeyValue(order.getDescription2(), "deliver", "entity"));
		daoService.saveObject(order);
		return showJsonSuccess(model);
	}

	@RequestMapping("/admin/gewapay/orderDetail.xhtml")
	public String orderDetail(String tradeNo, Long orderid, ModelMap model){
		GewaOrder order = null;
		if(StringUtils.isNotBlank(tradeNo)){
			order = daoService.getObjectByUkey(GewaOrder.class, "tradeNo", tradeNo, false);
		}else if(orderid!=null){
			order = daoService.getObject(GewaOrder.class, orderid);
		}
		if(order == null) {
			model.put("msg", "订单信息不存在!");
		}else{
			model.put("order", order);
			if(order instanceof TicketOrder && order.isPaidFailure()){
				OpenPlayItem opi = daoService.getObjectByUkey(OpenPlayItem.class, "mpid", ((TicketOrder)order).getMpid(), true);
				model.put("opi", opi);
			}
			List<Discount> discountList = paymentService.getOrderDiscountList(order);
			model.put("discountList", discountList);
			List<BuyItem> itemList = daoService.getObjectListByField(BuyItem.class, "orderid", order.getId());
			model.put("itemList", itemList);
			List<String> paymethodList = paymentService.getPayserverMethodList();
			if(paymethodList.contains(order.getPaymethod())){
				model.put("payserver", true);
			}
		}
		return "admin/gewapay/showOrder.vm";
	}
	@RequestMapping("/admin/gewapay/chargeDetail.xhtml")
	public String chargeDetail(String tradeNo, ModelMap model){
		Charge charge = daoService.getObjectByUkey(Charge.class, "tradeNo", tradeNo, false);
		model.put("charge", charge);
		model.put("member", daoService.getObject(Member.class, charge.getMemberid()));
		return "admin/gewapay/showCharge.vm";
	}
	@RequestMapping("/admin/gewapay/queryMemberAccount.xhtml")
	public String queryMemberAccount(Long mid, ModelMap model){
		if(mid!=null){
			if(mid > PartnerConstant.MAX_MEMBERID) return showError(model, "这是商家，请查询商家订单！");
			List<GewaOrder> orderList = paymentService.getGewaOrderListByMemberId(mid);
			List<BaseOrderExtra> extraList = new ArrayList<BaseOrderExtra>();
			if(!orderList.isEmpty()){
				List<OrderExtra> orderExtraList = daoService.getObjectListByField(OrderExtra.class, "memberid", mid);
				extraList.addAll(orderExtraList);
				List<OrderExtraHis> orderExtraHisList = daoService.getObjectListByField(OrderExtraHis.class, "memberid", mid);
				extraList.addAll(orderExtraHisList);
			}
			List<Charge> chargeList = paymentService.getChargeListByMemberId(mid, false, null, null, 0, 100);
			Map<Long, GewaOrder> chargeOrderMap = new HashMap<Long, GewaOrder>();
			for(Charge charge : chargeList){
				if(ChargeConstant.isBankPay(charge.getChargetype())){
					chargeOrderMap.put(charge.getId(), daoService.getObject(GewaOrder.class, charge.getOutorderid()));
				}
			}
			List<InvoiceRelate> invoiceRelateList = daoService.getObjectListByField(InvoiceRelate.class, "memberid", mid);
			Map<String,InvoiceRelate> invoiceRelateMap = BeanUtil.beanListToMap(invoiceRelateList, "tradeNo");
			model.put("invoiceRelateMap", invoiceRelateMap);
			Map<Long, BaseOrderExtra> extraMap = BeanUtil.beanListToMap(extraList, "id");
			model.put("extraMap", extraMap);
			MemberAccount account = daoService.getObjectByUkey(MemberAccount.class, "memberid", mid, false);
			List<Adjustment> adjustmentList = paymentService.getAdjustmentListByMemberId(mid, Adjustment.STATUS_SUCCESS);
			Collections.sort(orderList, new PropertyComparator("status", false, false));
			Collections.sort(chargeList, new PropertyComparator("status", false, false));
			model.put("orderList", orderList);
			model.put("chargeList", chargeList);
			model.put("chargeOrderMap", chargeOrderMap);
			model.put("adjustmentList", adjustmentList);
			if(account!=null){
				model.put("account", account);
				addCacheMember(model, account.getMemberid());
			}
		}
		return "admin/gewapay/memberAccount.vm";
	}

	@RequestMapping("/admin/gewapay/umpayfee.xhtml")
	public String umpayfee(ModelMap model){
		JsonData data = daoService.getObject(JsonData.class, JsonDataKey.KEY_UMPAYFEE);
		model.put("data", data);
		return "admin/gewapay/umPayFee.vm";
	}
	@RequestMapping("/admin/gewapay/saveUmpayfee.xhtml")
	public String saveUmpayfee(Integer fee, ModelMap model){
		if(fee==null || fee<1) return showJsonError(model, "填写的手续费率不正确！");
		JsonData data = daoService.getObject(JsonData.class, JsonDataKey.KEY_UMPAYFEE);
		if(data==null) data = new JsonData(JsonDataKey.KEY_UMPAYFEE);
		data.setData(fee+"");
		daoService.saveObject(data);
		return showJsonSuccess(model);
	}
}
