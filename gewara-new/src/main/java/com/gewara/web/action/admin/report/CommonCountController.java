package com.gewara.web.action.admin.report;

import java.sql.Timestamp;
import java.util.Arrays;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import com.gewara.constant.PaymethodConstant;
import com.gewara.constant.ticket.OrderConstant;
import com.gewara.model.api.ApiUser;
import com.gewara.model.pay.BuyItem;
import com.gewara.model.pay.GewaOrder;
import com.gewara.model.pay.TicketOrder;
import com.gewara.support.ReadOnlyTemplate;
import com.gewara.util.DateUtil;

@Controller
public class CommonCountController extends CountController{

	@Autowired@Qualifier("readOnlyTemplate")
	private ReadOnlyTemplate readOnlyTemplate;
	public void setReadOnlyHibernateTemplate(ReadOnlyTemplate readOnlyTemplate) {
		this.readOnlyTemplate = readOnlyTemplate;
	}
	
	@RequestMapping("/admin/common/orderDetail.xhtml")
	public String orderDetail(String tradeNo, ModelMap model){
		GewaOrder order = daoService.getObjectByUkey(GewaOrder.class, "tradeNo", tradeNo, false);
		model.put("order", order);
		List<BuyItem> itemList = daoService.getObjectListByField(BuyItem.class, "orderid", order.getId());
		model.put("itemList", itemList);
		return "admin/datacount/orderDetail.vm";
	}
	
	@RequestMapping("/admin/datacount/common/orderQry.xhtml")
	public String orderQry(ModelMap model){
		model.put("payMap", PaymethodConstant.getPayTextMap());
		model.put("partnerList", daoService.getAllObjects(ApiUser.class));
		return "admin/datacount/ticket/qryOrder.vm";
	}
	
	@RequestMapping("/admin/datacount/common/orderQryResult.xhtml")
	public String orderQry(Long movieid, Long cinemaid, Timestamp starttime, Timestamp endtime, Long partnerid, 
			String paymethod, String gewa, String status, String asc, HttpServletRequest request, ModelMap model){
		String url = "admin/datacount/ticket/qryOrder.vm";
		if(starttime==null || endtime==null)  return url;
		if(isInvalidTime(starttime, endtime)) return forwardErrorTime(model);
		String[] fields = request.getParameterValues("field");
		if(fields==null || fields.length==0) return forwardMessage(model, "请选择要查询的字段！");
		model.put("payMap", PaymethodConstant.getPayTextMap());
		model.put("partnerList", daoService.getAllObjects(ApiUser.class));
		DetachedCriteria qry = getQry(movieid, cinemaid, partnerid, paymethod, gewa, status, starttime, endtime, asc);
		List<TicketOrder> orderList = readOnlyTemplate.findByCriteria(qry, 0, 1000);
		model.put("orderList", orderList);
		model.put("fieldList", Arrays.asList(fields));
		model.put("statusMap", OrderConstant.statusMap);
		model.put("starttime", DateUtil.formatTimestamp(starttime));
		model.put("endtime", DateUtil.formatTimestamp(endtime));
		return url;
	}
	private DetachedCriteria getQry(Long movieid, Long cinemaid, Long partnerid, String paymethod, String gewa, 
			String status, Timestamp starttime, Timestamp endtime, String asc){
		DetachedCriteria qry = DetachedCriteria.forClass(TicketOrder.class, "t");
		qry.add(Restrictions.eq("t.status", status));
		if(movieid!=null) qry.add(Restrictions.eq("t.movieid", movieid));
		if(cinemaid!=null) qry.add(Restrictions.eq("t.cinemaid", cinemaid));
		if(partnerid!=null) qry.add(Restrictions.eq("t.partnerid", partnerid));
		else if(StringUtils.isNotBlank(gewa)){
			qry.add(Restrictions.eq("t.partnerid", 1L));
		}
		if(StringUtils.isNotBlank(paymethod)) qry.add(Restrictions.eq("t.paymethod", paymethod));
		qry.add(Restrictions.ge("t.addtime", starttime));
		qry.add(Restrictions.le("t.addtime", endtime));
		if(StringUtils.equals(asc, "1")){
			qry.addOrder(Order.asc("addtime"));
		}else {
			qry.addOrder(Order.desc("addtime"));
		}
		return qry;
	}
}
