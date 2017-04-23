package com.gewara.web.action.inner.order;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.gewara.constant.ApiConstant;
import com.gewara.constant.ticket.OrderConstant;
import com.gewara.constant.ticket.RefundConstant;
import com.gewara.model.drama.DramaOrder;
import com.gewara.model.drama.OpenDramaItem;
import com.gewara.model.goods.ActivityGoods;
import com.gewara.model.goods.BaseGoods;
import com.gewara.model.pay.GewaOrder;
import com.gewara.model.pay.GoodsOrder;
import com.gewara.model.pay.OrderRefund;
import com.gewara.model.pay.SettleOrder;
import com.gewara.util.DateUtil;
import com.gewara.util.JsonUtils;
import com.gewara.web.action.api.BaseApiController;

/**
 * 结算订单接口
 * 
 * @author taiqichao
 * 
 */
@Controller
public class ApiSettleOrderController extends BaseApiController {

	/**
	 * 查询结算订单信息
	 * 
	 * @param tradeNo
	 * @param model
	 * @return
	 */
	@RequestMapping("/inner/order/getSettleOrder.xhtml")
	public String getSettleOrder(@RequestParam(required = true, value = "tradeNo") String tradeNo,ModelMap model) {
		GewaOrder order = daoService.getObjectByUkey(GewaOrder.class, "tradeNo", tradeNo, false);
		if (order == null) return getErrorXmlView(model, ApiConstant.CODE_DATA_ERROR, "订单不存在!");
		String goodsType = "";// 商品类型
		Long goodsId = null;// 商品id
		String goodsName = "";// 商品名称
		String authorType = "";
		Timestamp consumeTime = null;// 商品消费时间
		if (order instanceof DramaOrder) {// 话剧订单
			goodsType = "drama";
			DramaOrder dramaOrder = (DramaOrder) order;
			goodsId = dramaOrder.getDramaid();
			OpenDramaItem odi = daoService.getObjectByUkey(OpenDramaItem.class, "dpid", dramaOrder.getDpid(), true);
			if(odi != null){
				goodsName = odi.getDramaname();
				consumeTime = odi.getPlaytime();
			}
			authorType = "partner";
		}else if(order instanceof GoodsOrder) {// 商品订单
			GoodsOrder goodsOrder = (GoodsOrder)order;
			goodsId = goodsOrder.getGoodsid();
			BaseGoods goods = daoService.getObject(BaseGoods.class, goodsId);
			if (goods != null) {
				goodsName = goods.getGoodsname();
				if(goods instanceof ActivityGoods){
					goodsType = goods.getGoodstype();
					String playtime = JsonUtils.getJsonValueByKey(order.getOtherinfo(), "playtime");
					if(StringUtils.isNotBlank(playtime)){
						consumeTime = DateUtil.parseTimestamp(playtime);
					}
					authorType = goods.getManager();
				}
			}
		}

		order.setTotalfee(order.getTotalfee() * 100);
		order.setOtherfee(order.getOtherfee() * 100);
		order.setItemfee(order.getItemfee() * 100);

		model.put("goodsId", goodsId);
		model.put("goodsType", goodsType);
		model.put("partnerType", order.getOrdertype());
		model.put("authorType", authorType);
		model.put("goodsName", goodsName);
		model.put("consumeTime", consumeTime);
		model.put("credentialesId", JsonUtils.getJsonValueByKey(order.getOtherinfo(), OrderConstant.OTHERKEY_CREDENTIALSID));
		model.put("order", order);
		return getXmlView(model, "inner/order/settleOrder.vm");
	}
	@RequestMapping("/inner/order/getRefundOrder.xhtml")
	public String getRefundOrder(@RequestParam(required=true, value= "tradeNo") String tradeNo, ModelMap model){
		OrderRefund orderRefund = daoService.getObjectByUkey(OrderRefund.class, "tradeno", tradeNo, true);
		if(orderRefund == null) return getErrorXmlView(model, ApiConstant.CODE_SIGN_ERROR, "退款信息不存在！");
		if(!(StringUtils.equals(orderRefund.getStatus(), RefundConstant.STATUS_SUCCESS) 
				|| StringUtils.equals(orderRefund.getStatus(), RefundConstant.STATUS_FINISHED)))
			return getErrorXmlView(model, ApiConstant.CODE_SIGN_ERROR, "不是退款成功状态！");
		GewaOrder order = daoService.getObjectByUkey(GewaOrder.class, "tradeNo", tradeNo, true);
		if(order == null) return getErrorXmlView(model, ApiConstant.CODE_SIGN_ERROR, "订单号不存在或被删除！");
		if(!StringUtils.equals(order.getStatus(), OrderConstant.STATUS_PAID_RETURN)) return getErrorXmlView(model, ApiConstant.CODE_SIGN_ERROR, "该订单不是退款订单！");
		model.put("refund", orderRefund);
		return getXmlView(model, "inner/order/refundOrder.vm");
	}
	
	/**
	 * 查询时间区间内结算订单列表
	 * 
	 * @param starttime
	 * @param endtime
	 * @param model
	 * @return
	 */
	@RequestMapping("/inner/order/getSettleOrderList.xhtml")
	public String getSettleOrderList(Timestamp starttime, Timestamp endtime, ModelMap model) {
		if (null == starttime || null == endtime) {
			Date cur = DateUtil.currentTime();
			starttime = DateUtil.getBeginTimestamp(cur);
			endtime = DateUtil.getEndTimestamp(cur);
		}
		if(DateUtil.getDiffDay(endtime, starttime) >7) return getErrorXmlView(model, ApiConstant.CODE_SIGN_ERROR, "时间间隔不能超过7天！");
		DetachedCriteria query = DetachedCriteria.forClass(SettleOrder.class);
		query.add(Restrictions.ge("paytime", starttime));
		query.add(Restrictions.le("paytime", endtime));
		query.setProjection(Projections.property("orderid"));
		// TODO 增加是否可结算的查询标示
		List<Long> idList = hibernateTemplate.findByCriteria(query);
		
		List<GewaOrder> orderList = daoService.getObjectList(GewaOrder.class, idList);
		model.put("orderList", orderList);
		return getXmlView(model, "inner/order/settleOrderList.vm");
	}


	@RequestMapping("/inner/order/getOrderInfo.xhtml")
	public String getOrderInfo(@RequestParam(required = true, value = "tradeNo") String tradeNo, ModelMap model){
		GewaOrder order = daoService.getObjectByUkey(GewaOrder.class, "tradeNo", tradeNo, false);
		if (order == null) return getErrorXmlView(model, ApiConstant.CODE_DATA_ERROR, "订单不存在!");
		model.put("order", order);
		return getXmlView(model, "inner/order/orderInfo.vm");
	}
}
