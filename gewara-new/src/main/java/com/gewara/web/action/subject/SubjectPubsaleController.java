package com.gewara.web.action.subject;

import java.sql.Timestamp;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.orm.hibernate4.HibernateOptimisticLockingFailureException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import com.gewara.constant.Status;
import com.gewara.constant.ticket.OrderConstant;
import com.gewara.model.pay.GoodsOrder;
import com.gewara.model.pay.PubMember;
import com.gewara.model.pay.PubSale;
import com.gewara.model.pay.PubSaleOrder;
import com.gewara.model.user.Member;
import com.gewara.service.order.GoodsOrderService;
import com.gewara.service.order.PubSaleService;
import com.gewara.support.ErrorCode;
import com.gewara.util.DateUtil;
import com.gewara.util.JsonUtils;
import com.gewara.util.StringUtil;
import com.gewara.web.action.AnnotationController;

@Controller
public class SubjectPubsaleController extends AnnotationController {

	@Autowired@Qualifier("pubSaleService")
	private PubSaleService pubSaleService;
	
	@Autowired@Qualifier("goodsOrderService")
	private GoodsOrderService goodsOrderService;
	
	@RequestMapping("/subject/proxy/pubsale/join.xhtml")
	public String joinPubsale(Long sid, Long memberid, Integer price, ModelMap model){
		if(price == null) return showJsonError(model, "价格不能为空！");
		PubSale sale = daoService.getObject(PubSale.class, sid);
		if(sale == null || sale.isClose()) return showJsonError(model, "竞拍数据不存或被删除！");
		Member member = daoService.getObject(Member.class, memberid);
		if(member == null) return showJsonError(model, "用户数据不存在！");
		Timestamp cur = DateUtil.getCurFullTimestamp();
		if(sale.isSoon()) return showJsonError(model, "竞拍未开始！");
		if(sale.isEnd(cur)) return showJsonError(model, "竞拍已结束！");
		if(Status.Y.equals(sale.getStatus())) return showJsonError(model, "竞拍已结束！");
		if(sale.isJoin()){
			for(int i=0;i<3;i++){
				try {
					ErrorCode<Map> code = pubSaleService.joinPubSale(sale.getId(), member, price, cur);
					if(!code.isSuccess()) return showJsonError(model, code.getMsg());
					Map map = code.getRetval();
					if(map.containsKey("success")){
						return showJsonSuccess(model, JsonUtils.writeMapToJson(map));
					}
				} catch (HibernateOptimisticLockingFailureException e) {
					dbLogger.warn(StringUtil.getExceptionTrace(e));
					if(i==2){
						return showJsonError(model, "竞拍失败，请重试！");
					}
				}
			}
		}
		sale = daoService.getObject(PubSale.class, sid);
		return showJsonError(model, "出价失败，最新价格已更改为“" + sale.gainRprice(sale.getCurprice()) + "元”！");
	}
	
	@RequestMapping("/subject/proxy/pubsale/pubmember.xhtml")
	public String getPubMemberList(Long sid, Long memberid, Integer from, Integer maxnum, ModelMap model){
		if(sid == null || from == null || from <0 || maxnum == null || maxnum <1) return showJsonError(model, "参数错误！");
		List<PubMember> pubMemberList = pubSaleService.getPubMemberList(sid, memberid, from, maxnum);
		return showJsonSuccess(model, JsonUtils.writeObjectToJson(pubMemberList));
	}
	
	@RequestMapping("/subject/proxy/pubsale/goodsOrder.xhtml")
	public String getPubMemberGoodsid(Long sid, Long memberid, ModelMap model){
		PubSale sale = daoService.getObject(PubSale.class, sid);
		if(sale == null || sale.isClose()) return showJsonError(model, "竞拍数据不存或被删除！");
		Member member = daoService.getObject(Member.class, memberid);
		if(member == null) return showJsonError(model, "用户数据不存在！");
		List<GoodsOrder> orderList = goodsOrderService.getGoodsOrderList(sale.getGoodsid(), member.getId(), OrderConstant.STATUS_PAID_SUCCESS, false, true, 1);
		if(orderList.isEmpty()){
			return showJsonError(model, "当前没有竞拍资格！");
		}
		return showJsonSuccess(model, "true");
	}
	
	@RequestMapping("/subject/proxy/pubsale/order.xhtml")
	public String getPubSaleOrder(Long sid,ModelMap model){
		PubSale sale = daoService.getObject(PubSale.class, sid);
		if(sale == null) return showJsonError(model, "竞拍数据不存或被删除！");
		if(!sale.saleSuccess()) return showJsonError(model, "竞拍还未结束！");
		PubSaleOrder pubSaleOrder = daoService.getObjectByUkey(PubSaleOrder.class, "pubid", sale.getId());
		if(pubSaleOrder == null) return showJsonError(model, "竞拍订单不存在！");
		return showJsonSuccess(model, JsonUtils.writeObjectToJson(pubSaleOrder));
	}
}
