package com.gewara.web.action.admin.ajax;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import com.gewara.constant.BindConstant;
import com.gewara.model.pay.GewaOrder;
import com.gewara.model.pay.SMSRecord;
import com.gewara.service.member.BindMobileService;
import com.gewara.support.ErrorCode;
import com.gewara.untrans.UntransService;
import com.gewara.util.ValidateUtil;
import com.gewara.util.WebUtils;
import com.gewara.web.action.admin.BaseAdminController;

@Controller
public class AgentTradeAjaxAdminController extends BaseAdminController {


	@Autowired@Qualifier("untransService")
	private UntransService untransService;
	
	@Autowired@Qualifier("bindMobileService")
	private BindMobileService bindMobileService;
	
	//赠品付费的情况
	@RequestMapping("/admin/drama/agent/modOrderMobile.xhtml")
	public String modOrderMobile(Long orderid, String mobile, ModelMap model) {
		GewaOrder order = daoService.getObject(GewaOrder.class, orderid);
		if(order==null) return showJsonError_NOT_FOUND(model);
		if(!ValidateUtil.isMobile(mobile)) return showJsonError(model, "手机格式不正确");
		if (order.isAllPaid() || order.isCancel()) return showJsonError(model, "不能修改已支付或已（过时）取消的订单！");
		order.setMobile(mobile);
		daoService.saveObject(order);
		return showJsonSuccess(model);
	}
	
	@RequestMapping("/admin/drama/agent/sendMobile.xhtml")
	public String sendMobile(HttpServletRequest request, String mobile, ModelMap model){
		if(!ValidateUtil.isMobile(mobile)){
			return showJsonError(model, "手机号格式错误！");
		}
		String ip = WebUtils.getRemoteIp(request);
		ErrorCode<SMSRecord> code = bindMobileService.refreshNoSecurityBindMobile(BindConstant.TAG_REGISTERCODE, mobile, ip, BindConstant.ADMIN_MOBILE_TEMPLATE);
		if(!code.isSuccess()) return showJsonError(model, code.getMsg());
		untransService.sendMsgAtServer(code.getRetval(), false);
		return showJsonSuccess(model);
	}
}
