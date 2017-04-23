package com.gewara.web.action.ajax;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.RequestMapping;

import com.gewara.constant.SmsConstant;
import com.gewara.constant.TagConstant;
import com.gewara.constant.ticket.OrderConstant;
import com.gewara.model.drama.DramaOrder;
import com.gewara.model.pay.GoodsOrder;
import com.gewara.model.pay.SMSRecord;
import com.gewara.model.pay.SportOrder;
import com.gewara.model.pay.TicketOrder;
import com.gewara.model.user.Member;
import com.gewara.service.MessageService;
import com.gewara.service.OperationService;
import com.gewara.service.bbs.BlogService;
import com.gewara.untrans.UntransService;
import com.gewara.untrans.impl.ControllerService;
import com.gewara.util.DateUtil;
import com.gewara.util.StringUtil;
import com.gewara.util.ValidateUtil;
import com.gewara.util.VmUtils;
import com.gewara.util.WebUtils;
import com.gewara.web.action.AnnotationController;

@Controller
public class OrderMessageController extends AnnotationController{

	@Autowired@Qualifier("messageService")
	private MessageService messageService;
	
	@Autowired@Qualifier("blogService")
	private BlogService blogService;
	
	@Autowired@Qualifier("untransService")
	private UntransService untransService;
	
	@Autowired@Qualifier("controllerService")
	private ControllerService controllerService;
	
	@Autowired@Qualifier("operationService")
	private OperationService operationService;

	/**
	 * @param sessid
	 * @param request
	 * @param orderId
	 * @param mobile
	 * @param msgContent
	 * @param model
	 * @param captchaId
	 * @param captcha
	 * @return
	 */
	@RequestMapping("/ajax/trade/orderResultSendMsg.xhtml")
	public String orderResultSendMsg(@CookieValue(value=LOGIN_COOKIE_NAME, required=false)String sessid,
			HttpServletRequest request, Long orderId, String mobile, String msgContent, ModelMap model, String captchaId, String captcha){
		boolean isValidCaptcha = controllerService.validateCaptcha(captchaId, captcha, WebUtils.getRemoteIp(request));
		if(!isValidCaptcha) return showJsonError(model, "验证码错误！");
		String ip = WebUtils.getRemoteIp(request);
		Member member = loginService.getLogonMemberBySessid(ip, sessid);
		if(member == null) return showJsonError(model, "请先登录！");
		TicketOrder order = daoService.getObject(TicketOrder.class, orderId);
		if(order == null) return showJsonError(model, "该订单不存在或被删除！");
		if (!order.getMemberid().equals(member.getId())) return show404(model, "该订单是他人的订单！");
		if ( order.isCancel()) return showJsonError(model, "不能对已（过时）取消的订单发送短信邀请好友！");
		if (!StringUtils.equals(order.getStatus(), OrderConstant.STATUS_PAID_SUCCESS)) return showJsonError(model, "该订单不是成功订单！");
		String[] moblies = StringUtils.split(mobile,",");
		if(moblies.length > 2) return showJsonError(model, "短信邀请好友最多不能超过2人");
		String opkey = member.getId()+"_" + order.getCinemaid();
		if(!operationService.isAllowOperation(opkey, OperationService.ONE_DAY)){
			return showJsonError(model, "每笔订单只可邀请1次！");
		}
		Timestamp curtime = DateUtil.getCurFullTimestamp();
		String tradeNo =  String.valueOf(order.getCinemaid());
		String smsTradeNo = orderId + "movie" + tradeNo;
		if(order.getPaidtime().before(DateUtil.addDay(curtime, -1))) return showJsonError(model, "订单时间已超时！");
		
		int count = messageService.querySmsRecord(smsTradeNo, TagConstant.TAG_MOVIEORDER, null, null, orderId, member.getId());
		if((count + moblies.length) > 2) return showJsonError(model, "每笔订单最多可邀请2位好友");
		if(StringUtils.isBlank(msgContent)) return showJsonError(model, "短信的内容不能为空！");
		if(VmUtils.getByteLength(msgContent) > 128) return showJsonError(model, "短信内容长度不能超过64个字，请修改！");
		boolean isSendMsg = StringUtils.isNotBlank(blogService.filterAllKey(msgContent));
		for(int i = 0;i < moblies.length;i++){
			if(moblies[i].length() > 1 && ValidateUtil.isMobile(moblies[i])){
				SMSRecord sms = new SMSRecord(moblies[i]);
				if(isSendMsg){
					sms.setStatus(SmsConstant.STATUS_FILTER);
				}
				sms.setTradeNo(smsTradeNo);
				sms.setContent(msgContent);
				sms.setSendtime(curtime);
				sms.setSmstype(SmsConstant.SMSTYPE_MANUAL);
				sms.setValidtime(DateUtil.getLastTimeOfDay(curtime));
				sms.setTag(TagConstant.TAG_MOVIEORDER);
				sms.setMemberid(member.getId());
				sms.setRelatedid(orderId);
				sms = untransService.addMessage(sms);
				if(sms!=null) untransService.addMessage(sms);
			}
		}
		operationService.updateOperation(opkey, OperationService.ONE_DAY);
		return showJsonSuccess(model);
	}
	
	@RequestMapping("/ajax/trade/acOrderResultSendMsg.xhtml")
	public String acOrderResultSendMsg(@CookieValue(value=LOGIN_COOKIE_NAME, required=false)String sessid,
			HttpServletRequest request, Long orderId, String mobile, String msgContent, ModelMap model){
		String ip = WebUtils.getRemoteIp(request);
		Member member = loginService.getLogonMemberBySessid(ip, sessid);
		if(member == null) return showJsonError(model, "请先登录！");
		GoodsOrder order = daoService.getObject(GoodsOrder.class, orderId);
		if(order == null) return showJsonError(model, "该订单不存在或被删除！");
		if (!order.getMemberid().equals(member.getId())) return show404(model, "该订单是他人的订单！");
		if ( order.isCancel()) return showJsonError(model, "不能对已（过时）取消的订单发送短信邀请好友！");
		if (!StringUtils.equals(order.getStatus(), OrderConstant.STATUS_PAID_SUCCESS)) return showJsonError(model, "该订单不是成功订单！");
		String[] moblies = StringUtils.split(mobile,",");
		if(moblies.length > 5) return showJsonError(model, "短信邀请好友最多不能超过5人");
		String opkey = member.getId()+"_" + order.getGoodsid();
		if(!operationService.updateOperation(opkey, 10)) return showJsonError(model, "你操作过于频繁，请稍后再试！");
		Timestamp curtime = DateUtil.getCurFullTimestamp();
		String tradeNo =  String.valueOf(order.getGoodsid());
		String smsTradeNo = orderId + "activity" + tradeNo;
		if(order.getPaidtime().before(DateUtil.addDay(curtime, -1))) return showJsonError(model, "订单时间已超时！");
		int count = messageService.querySmsRecord(smsTradeNo, TagConstant.TAG_ACTIVITYORDER, null, null, orderId, member.getId());
		if((count + moblies.length) > 5) return showJsonError(model, "每笔订单最多可邀请5位好友");
		if(StringUtils.isBlank(msgContent)) return showJsonError(model, "短信的内容不能为空！");
		if(VmUtils.getByteLength(msgContent) > 128) return showJsonError(model, "短信内容长度不能超过64个字，请修改！");
		boolean isSendMsg = StringUtils.isNotBlank(blogService.filterAllKey(msgContent));
		for(int i = 0;i < moblies.length;i++){
			if(moblies[i].length() > 1 && ValidateUtil.isMobile(moblies[i])){
				SMSRecord sms = new SMSRecord(moblies[i]);
				if(isSendMsg){
					sms.setStatus(SmsConstant.STATUS_FILTER);
				}
				sms.setTradeNo(smsTradeNo);
				sms.setContent(msgContent);
				sms.setSendtime(curtime);
				sms.setSmstype(SmsConstant.SMSTYPE_MANUAL);
				sms.setValidtime(DateUtil.getLastTimeOfDay(curtime));
				sms.setTag(TagConstant.TAG_ACTIVITYORDER);
				sms.setMemberid(member.getId());
				sms.setRelatedid(orderId);
				sms = untransService.addMessage(sms);
				if(sms!=null) {
					untransService.sendMsgAtServer(sms, true);
				}
			}
		}
		return showJsonSuccess(model);
	}

	
	@RequestMapping("/drama/ajax/dramaSendMsg.xhtml")
	public String dramaSendMsg(@CookieValue(value=LOGIN_COOKIE_NAME, required=false)String sessid,
			HttpServletRequest request, Long orderId, String mobile, String msgContent,ModelMap model, String captchaId, String captcha){
		boolean isValidCaptcha = controllerService.validateCaptcha(captchaId, captcha, WebUtils.getRemoteIp(request));
		if(!isValidCaptcha) return showJsonError(model, "验证码错误！");
		String ip = WebUtils.getRemoteIp(request);
		Member member = loginService.getLogonMemberBySessid(ip, sessid);
		if(member == null) return showJsonError(model, "请先登录！");
		DramaOrder order = daoService.getObject(DramaOrder.class, orderId);
		if(order == null) return showJsonError(model, "该订单不存在或被删除！");
		if(!member.getId().equals(order.getMemberid())) return show404(model, "该订单是他人的订单！");
		if (order.isCancel()) return showJsonError(model, "不能对已（过时）取消的订单发送短信邀请好友！");
		if(!StringUtils.equals(order.getStatus(), OrderConstant.STATUS_PAID_SUCCESS))  return showJsonError(model, "非成功支付的订单，不能发短信");
		String[] moblies = StringUtils.split(mobile,",");
		if(moblies.length > 2) return showJsonError(model, "短信邀请好友每次不能超过2人");
		Timestamp curtime = DateUtil.getCurFullTimestamp();
		if(order.getPaidtime().before(DateUtil.addDay(curtime, -3))) return showJsonError(model, "订单时间已超时！");
		String opkey = member.getId()+"_" + order.getDramaid();
		if(!operationService.isAllowOperation(opkey, OperationService.ONE_DAY)){
			return showJsonError(model, "每笔订单只可邀请1次！");
		}
		String tradeNo =  String.valueOf(order.getDramaid());
		String smsTradeNo = orderId + "drama" + tradeNo;
		int count = messageService.querySmsRecord(smsTradeNo, TagConstant.TAG_DRAMAORDER, null, null, orderId, member.getId());
		if((count + moblies.length) > 2) return showJsonError(model, "每笔订单最多可邀请2位好友");
		if(StringUtils.isBlank(msgContent)) return showJsonError(model, "短信的内容不能为空！");
		if(!member.isBindMobile()) return showJsonError(model, "请绑定手机后再试！");
		if(VmUtils.getByteLength(msgContent) > 128) return showJsonError(model, "短信内容长度不能超过64个字，请修改！");
		boolean isSendMsg = StringUtils.isNotBlank(blogService.filterAllKey(msgContent));
		for(int i = 0;i < moblies.length;i++){
			if(moblies[i].length() > 1 && ValidateUtil.isMobile(moblies[i])){
				SMSRecord sms = new SMSRecord(moblies[i]);
				if(isSendMsg){
					sms.setStatus(SmsConstant.STATUS_FILTER);
				}
				sms.setTradeNo(smsTradeNo+StringUtil.getRandomString(5));
				sms.setContent(msgContent);
				sms.setSendtime(curtime);
				sms.setSmstype(SmsConstant.SMSTYPE_MANUAL);
				sms.setValidtime(DateUtil.getLastTimeOfDay(curtime));
				sms.setTag(TagConstant.TAG_DRAMAORDER);
				sms.setMemberid(member.getId());
				sms.setRelatedid(orderId);
				sms = untransService.addMessage(sms);
				if(sms!=null){
					untransService.sendMsgAtServer(sms, true);
				}
			}
		}
		operationService.updateOperation(opkey, OperationService.ONE_DAY);
		return showJsonSuccess(model);
	}
	
	@RequestMapping("/sport/ajax/newsportSendMsg.xhtml")
	public String newsportSendMsg(@CookieValue(value=LOGIN_COOKIE_NAME, required=false)String sessid,
			HttpServletRequest request, Long orderId, String mobile, String msgContent,
			ModelMap model, String captchaId, String captcha){
		String ip = WebUtils.getRemoteIp(request);
		Member member = loginService.getLogonMemberBySessid(ip, sessid);
		if(member == null) return showJsonError(model, "请先登录！");
		Map jsonMap = new HashMap<String, String>();
		boolean isValidCaptcha = controllerService.validateCaptcha(captchaId, captcha, WebUtils.getRemoteIp(request));
		if(!isValidCaptcha) {
			jsonMap.put("msg", "验证码错误！");
			jsonMap.put("refreshCaptcha", true);
			return showJsonError(model, jsonMap);
		}
		SportOrder order = daoService.getObject(SportOrder.class, orderId);
		if(order == null) return showJsonError(model, "该订单不存在或被删除！");
		if (!order.getMemberid().equals(member.getId())) return show404(model, "该订单是他人的订单！");
		if ( order.isCancel()) return showJsonError(model, "不能对已（过时）取消的订单发送短信邀请好友！");
		if(!StringUtils.equals(order.getStatus(), OrderConstant.STATUS_PAID_SUCCESS)) return showJsonError(model, "非成功支付的订单，不能发短信");
		mobile = StringUtils.substring(mobile, 1);
		String[] moblies = StringUtils.split(mobile,",");
		if(moblies.length > 2) return showJsonError(model, "短信邀请好友每次不能超过2人");
		String opkey = member.getId()+"_" + order.getSportid();
		if(!operationService.isAllowOperation(opkey, OperationService.ONE_DAY)){
			return showJsonError(model, "每笔订单只可邀请1次！");
		}
		Timestamp curtime = DateUtil.getCurFullTimestamp();
		if(order.getPaidtime().before(DateUtil.addDay(curtime, -1))) return showJsonError(model, "订单时间已超时！");
		String tradeNo =  String.valueOf(order.getSportid());
		String smsTradeNo = orderId + "sport" + tradeNo;
		int count = messageService.querySmsRecord(smsTradeNo, TagConstant.TAG_SPORTORDER, null, null, orderId, member.getId());
		if((count + moblies.length) > 2) return showJsonError(model, "每笔订单最多可邀请2位好友");
		if(StringUtils.isBlank(msgContent)) return showJsonError(model, "短信的内容不能为空！");
		if(!member.isBindMobile()) return showJsonError(model, "请绑定手机后再试！");
		if(msgContent.length() > 60) return showJsonError(model, "短信内容长度不能超过60个字，请修改！");
		boolean isSendMsg = StringUtils.isNotBlank(blogService.filterAllKey(msgContent));
		for(int i = 0;i < moblies.length;i++){
			if(ValidateUtil.isMobile(moblies[i])){
				SMSRecord sms = new SMSRecord(moblies[i]);
				if(isSendMsg){
					sms.setStatus(SmsConstant.STATUS_FILTER);
				}
				sms.setTradeNo(smsTradeNo+StringUtil.getRandomString(5));
				sms.setContent(msgContent);
				sms.setSendtime(curtime);
				sms.setSmstype(SmsConstant.SMSTYPE_MANUAL);
				sms.setValidtime(DateUtil.getLastTimeOfDay(curtime));
				sms.setTag(TagConstant.TAG_SPORTORDER);
				sms.setMemberid(member.getId());
				sms.setRelatedid(orderId);
				sms = untransService.addMessage(sms);
				if(sms!=null){
					untransService.sendMsgAtServer(sms, true);
				}
			}
		}
		operationService.updateOperation(opkey, OperationService.ONE_DAY);
		return showJsonSuccess(model);
	}

}
