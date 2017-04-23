package com.gewara.web.action.admin.ajax;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import com.gewara.constant.SmsConstant;
import com.gewara.constant.sys.LogTypeConstant;
import com.gewara.model.acl.User;
import com.gewara.model.pay.GewaOrder;
import com.gewara.model.pay.SMSRecord;
import com.gewara.service.MessageService;
import com.gewara.support.ErrorCode;
import com.gewara.untrans.UntransService;
import com.gewara.util.BeanUtil;
import com.gewara.util.BindUtils;
import com.gewara.util.ChangeEntry;
import com.gewara.web.action.admin.BaseAdminController;
@Controller
public class AdminMessageAjaxController extends BaseAdminController {
	@Autowired@Qualifier("untransService")
	private UntransService untransService;
	public void setUntransService(UntransService untransService) {
		this.untransService = untransService;
	}
	@Autowired@Qualifier("messageService")
	private MessageService messageService;
	public void setMessageService(MessageService messageService) {
		this.messageService = messageService;
	}
	@RequestMapping("/admin/message/ajax/getSMS.xhtml")
	public String getSMS(Long id, ModelMap model){
		SMSRecord sms = daoService.getObject(SMSRecord.class, id);
		return showJsonSuccess(model, BeanUtil.getBeanMap(sms));
	}
	@RequestMapping("/admin/message/ajax/sendMessageById.xhtml")
	public String sendMessageById(Long smsId, String channel, ModelMap model){
		ErrorCode code = null;
		SMSRecord sms = daoService.getObject(SMSRecord.class, smsId);
		if(StringUtils.isBlank(channel)){
			code = untransService.sendMsgAtServer(sms, true);
		}else {
			code = untransService.sendMsgAtServer(sms, channel, true);
		}
		if(code.isSuccess()) return showJsonSuccess(model);
		return showJsonError(model, code.getMsg());
		
	}
	@RequestMapping("/admin/message/ajax/processMessageById.xhtml")
	public String processMessageById(Long smsId, ModelMap model){
		User user = getLogonUser();
		dbLogger.warnWithType(LogTypeConstant.LOG_TYPE_USERACTION, user.getId() + user.getRealname() + "处理短信：" + smsId);
		SMSRecord sms = daoService.getObject(SMSRecord.class, smsId);
		sms.setStatus(SmsConstant.STATUS_PROCESS);
		daoService.saveObject(sms);
		return showJsonSuccess(model);
	}
	@RequestMapping("/admin/message/ajax/saveSMS.xhtml")
	public String saveSMS(Long id, HttpServletRequest request, ModelMap model){
		SMSRecord sms = daoService.getObject(SMSRecord.class, id);
		ChangeEntry changeEntry = new ChangeEntry(sms);
		BindUtils.bindData(sms,request.getParameterMap());
		daoService.saveObject(sms);
		monitorService.saveChangeLog(getLogonUser().getId(), SMSRecord.class, sms.getId(),changeEntry.getChangeMap( sms));
		return showJsonSuccess(model);
	}
	@RequestMapping("/admin/message/ajax/addMessage.xhtml")
	public String addMessage(Long orderId, ModelMap model){
		GewaOrder order = daoService.getObject(GewaOrder.class, orderId);
		ErrorCode<List<SMSRecord>> result = messageService.addMessage(order);
		if(result.isSuccess()) return showJsonSuccess(model, "成功加入消息发送队列");
		return showJsonError(model, result.getMsg());
	}
	@RequestMapping("/admin/message/ajax/addUnSendMessage.xhtml")
	public String addUnSendMessage(Long orderId, ModelMap model){
		GewaOrder order = daoService.getObject(GewaOrder.class, orderId);
		messageService.addUnSendMessage(order);
		return showJsonSuccess(model, "成功加入消息发送队列");
	}

}
