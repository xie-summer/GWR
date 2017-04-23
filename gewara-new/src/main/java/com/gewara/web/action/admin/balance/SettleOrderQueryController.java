package com.gewara.web.action.admin.balance;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.gewara.constant.ticket.OrderConstant;
import com.gewara.model.movie.Cinema;
import com.gewara.model.pay.CinemaSettle;
import com.gewara.model.pay.TicketOrder;
import com.gewara.util.ReportUtil;
import com.gewara.web.action.admin.BaseAdminController;
@Controller
public class SettleOrderQueryController extends BaseAdminController{
	@RequestMapping(value="/admin/balance/settle/viewOrderList.xhtml", method=RequestMethod.GET)
	public String viewSettle(Long settleId, ModelMap model){
		CinemaSettle settle = daoService.getObject(CinemaSettle.class, settleId);
		Cinema cinema = daoService.getObject(Cinema.class, settle.getCinemaid());
		model.put("settle", settle);
		model.put("cinema", cinema);
		return "admin/balance/settle/viewOrderList.vm";
	}
	@RequestMapping(value="/admin/balance/settle/viewOrderList.xhtml", method=RequestMethod.POST)
	public String viewSettleOrderList(Long settleId, ModelMap model, String split, String timetype, String isXls, HttpServletResponse response){
		CinemaSettle settle = daoService.getObject(CinemaSettle.class, settleId);
		List<TicketOrder> orderList = getOrderListData(settle.getCinemaid(), timetype, settle.getTimefrom(), settle.getTimeto());
		Map<Long, Map> rowMap = new HashMap<Long, Map>(orderList.size());
		model.put("orderList", orderList);
		model.put("rowMap", rowMap);
		model.put("ReportUtil", new ReportUtil());
		model.put("settle", settle);
		downloadXls(isXls, response);
		if(StringUtils.equals(split, "bySeat")){
			return "admin/balance/settle/viewOrderListResultBySeat.vm";
		}
		return "admin/balance/settle/viewOrderListResult.vm";
	}
	private List<TicketOrder> getOrderListData(Long cinemaid, String timetype, Timestamp timefrom, Timestamp timeto) {
		String hql = "from TicketOrder t where t.cinemaid=? and t.addtime>=? and t.addtime<=? and t.status like ? and t.settle='Y' order by t.addtime asc, playtime asc ";
		if(StringUtils.equals("playtime", timetype)){
			hql = "from TicketOrder t where t.cinemaid=? and t.playtime>=? and t.playtime<=? and t.status like ? and t.settle='Y' order by t.addtime asc, playtime asc ";
		}
		
		List<TicketOrder> orderList = hibernateTemplate.find(hql, cinemaid, timefrom, timeto, OrderConstant.STATUS_PAID + "%");
		return orderList;
	}

}
