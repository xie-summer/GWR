package com.gewara.web.action.admin.order;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import com.gewara.command.OrderParamsCommand;
import com.gewara.constant.ticket.OrderConstant;
import com.gewara.model.pay.GymOrder;
import com.gewara.service.order.OrderQueryService;
import com.gewara.support.ErrorCode;
import com.gewara.untrans.gym.SynchGymService;
import com.gewara.util.BeanUtil;
import com.gewara.util.DateUtil;
import com.gewara.util.JsonUtils;
import com.gewara.web.action.admin.BaseAdminController;
import com.gewara.web.util.PageUtil;
import com.gewara.xmlbind.gym.RemoteGym;


@Controller
public class GymOrderAdminController extends BaseAdminController {
	@Autowired@Qualifier("orderQueryService")
	private OrderQueryService orderQueryService;
	@Autowired@Qualifier("synchGymService")
	private SynchGymService synchGymService;
	
	@RequestMapping("/admin/order/gymOrderList.xhtml")
	public String orderList(OrderParamsCommand command, HttpServletResponse response, ModelMap model) {
		Timestamp cur = DateUtil.getCurFullTimestamp();
		checkParams(cur, command);
		if (StringUtils.isNotBlank(command.getErrorMsg())) {
			ErrorCode<List<RemoteGym>> code = synchGymService.getGymList(null, null, null, null, false, 0, 100);
			if(code.isSuccess()) {
				model.put("gymList", code.getRetval());
			}
			model.put("command", command);
			return "admin/order/gymOrderList.vm";
		}
		List<GymOrder> gymOrderList = new ArrayList<GymOrder>();
		int rowsPerPage = 100;
		int firstPre = command.getPageNo() * rowsPerPage;
		command.setOrdertype(OrderConstant.ORDER_TYPE_GYM);
		int rowsCount = orderQueryService.getOrderCount(command);
		if(rowsCount >0){
			gymOrderList = orderQueryService.getOrderList(GymOrder.class, command, firstPre, rowsPerPage);
		}
		model.put("rowsCount", rowsCount);
		PageUtil pageUtil = new PageUtil(rowsCount, rowsPerPage, command.getPageNo(), "admin/order/gymOrderList.xhtml", true, true);
		Map<String,String> params = BeanUtil.getSimpleStringMap(command);
		pageUtil.initPageInfo(params);
		model.put("pageUtil", pageUtil);
		model.put("orderList", gymOrderList);
		model.put("ordertype", StringUtils.isBlank(command.getOrdertype())?"" : command.getOrdertype());
		ErrorCode<List<RemoteGym>> code = synchGymService.getGymList(null, null, null, null, false, 0, 100);
		if(code.isSuccess()) {
			model.put("gymList", code.getRetval());
		}
		if (command.getXls() != null && command.getXls().equals("true")) {
			download("xls", response);
			model.put("xls", command.getXls());
			return "admin/gym/ticket/orders.vm";
		}
		Map<Long, String> synchStatusMap = new HashMap<Long, String>();
		for(GymOrder gymOrder : gymOrderList){
			Map map = JsonUtils.readJsonToMap(gymOrder.getOtherinfo());
			synchStatusMap.put(gymOrder.getId(), (String) map.get(GymOrder.GYM_CONFIRM));
		}
		model.put("synchStatusMap", synchStatusMap);
		model.put("command", command);
		return "admin/order/gymOrderList.vm";
	}
	
	private void checkParams(Timestamp cur, OrderParamsCommand command){
		if(StringUtils.isBlank(command.getLevel())){
			command.setStatus(OrderConstant.STATUS_PAID_FAILURE);
			command.setEndtime(cur);
			command.setStarttime(DateUtil.addDay(DateUtil.getBeginningTimeOfDay(cur), -10));
			command.setLevel("1");
		}
		if (StringUtils.isBlank(command.getOrder()) && StringUtils.isBlank(command.getMobile())) {
			if(command.getStarttime() == null || command.getEndtime() == null){
				command.setErrorMsg("交易时段范围不能为空！");
			}
			if (DateUtil.getDiffDay(command.getEndtime(), command.getStarttime()) > 5) {
				command.setErrorMsg("查询时间间隔不得大于5天！");
			}
		}
	}
}
