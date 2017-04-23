package com.gewara.web.action.admin.ticket;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import com.gewara.constant.ticket.OrderConstant;
import com.gewara.json.TicketRollCallMember;
import com.gewara.model.acl.User;
import com.gewara.model.user.Member;
import com.gewara.support.ErrorCode;
import com.gewara.untrans.ticket.TicketRollCallService;
import com.gewara.util.ValidateUtil;
import com.gewara.web.action.admin.BaseAdminController;
import com.gewara.web.util.PageUtil;

@Controller
public class TicketRollCallAdminController extends BaseAdminController{
	public static final List<String> ROLL_MEMBER_TYPE = Arrays.asList(OrderConstant.UNIQUE_BY_MEMBERID, OrderConstant.UNIQUE_BY_MOBILE);
	
	@Autowired@Qualifier("ticketRollCallService")
	private TicketRollCallService ticketRollCallService;
	public void setTicketRollCallService(TicketRollCallService ticketRollCallService){
		this.ticketRollCallService = ticketRollCallService;
	}
	@RequestMapping("/admin/rollcall/getTicketRollCallMemberList.xhtml")
	public String getTicketRollCallMemberList(Integer pageNo, String mobile, String status, Date startdate, Date enddate, ModelMap model){
		if(pageNo == null) pageNo = 0;
		int rowsPerPage = 10;
		int firstPerPage = pageNo * rowsPerPage;
		int count = ticketRollCallService.getTicketRollCallMemberCount(status, startdate, enddate, mobile);
		List<TicketRollCallMember> rollCallMemberList = ticketRollCallService.getTicketRollCallMemberList(status, startdate, enddate, firstPerPage, rowsPerPage, mobile);
		Map<String,String> tickMemberMap = new HashMap<String, String>();
		for(TicketRollCallMember ticketRollCallMember : rollCallMemberList){
			User user = daoService.getObject(User.class, ticketRollCallMember.getUserid());
			if(user != null) tickMemberMap.put(ticketRollCallMember.getId(), user.getNickname());
		}
		model.put("tickMemberMap", tickMemberMap);
		PageUtil pageUtil = new PageUtil(count, rowsPerPage, firstPerPage, "/admin/rollcall/getTicketRollCallMemberList.xhtml");
		Map params = new HashMap();
		params.put("mobile", mobile);
		params.put("status", status);
		params.put("startdate", startdate);
		params.put("enddate", enddate);
		pageUtil.initPageInfo(params);
		model.put("rollCallMemberList", rollCallMemberList);
		model.put("pageUtil", pageUtil);
		return "admin/common/rollCallMemberList.vm";
	}
	
	@RequestMapping("/admin/rollcall/delTicketRollCallMember.xhtml")
	public String delTicketRollCallMember(String id, ModelMap model){
		boolean result = ticketRollCallService.removeRollCallMember(id);
		if(!result) return showJsonError(model, "该数据不存在或被删除！");
		return showJsonSuccess(model);
	}
	
	@RequestMapping("/admin/rollcall/saveTicketRollCallMember.xhtml")
	public String saveTicketRollCallMember(String mobile, String status, String reason, ModelMap model){
		User user = getLogonUser();
		if(!ValidateUtil.isNumber(mobile)) return showJsonError(model, "用户ID或手机号输入有误！");
		if(!ValidateUtil.isMobile(mobile)){
			Member member = daoService.getObject(Member.class, Long.valueOf(mobile));
			if(member == null) return showJsonError(model, "用户不存在");
		}
		ErrorCode result = ticketRollCallService.addTicketRollMember(mobile, status, reason, user.getId());
		if(result.isSuccess()) return showJsonSuccess(model);
		return showJsonError(model, result.getMsg());
	}
}
