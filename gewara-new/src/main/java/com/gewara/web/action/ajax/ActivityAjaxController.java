package com.gewara.web.action.ajax;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.RequestMapping;

import com.gewara.constant.MemberConstant;
import com.gewara.constant.SmsConstant;
import com.gewara.constant.TagConstant;
import com.gewara.model.pay.SMSRecord;
import com.gewara.model.user.Member;
import com.gewara.model.user.MemberInfo;
import com.gewara.model.user.UserMessage;
import com.gewara.model.user.UserMessageAction;
import com.gewara.service.bbs.BlogService;
import com.gewara.service.order.OrderQueryService;
import com.gewara.support.ErrorCode;
import com.gewara.untrans.UntransService;
import com.gewara.untrans.activity.SynchActivityService;
import com.gewara.util.BindUtils;
import com.gewara.util.DateUtil;
import com.gewara.util.JsonUtils;
import com.gewara.util.ValidateUtil;
import com.gewara.util.WebUtils;
import com.gewara.web.action.AnnotationController;
import com.gewara.xmlbind.activity.RemoteActivity;
import com.gewara.xmlbind.activity.RemoteApplyjoin;
@Controller
public class ActivityAjaxController extends AnnotationController{
	
	@Autowired@Qualifier("untransService")
	private UntransService untransService;
	public void setUntransService(UntransService untransService) {
		this.untransService = untransService;
	}
	
	
	@Autowired@Qualifier("blogService")
	private BlogService blogService;
	public void setBlogService(BlogService blogService) {
		this.blogService = blogService;
	}
	
	@Autowired@Qualifier("orderQueryService")
	private OrderQueryService orderQueryService;
	public void setOrderQueryService(OrderQueryService orderQueryService) {
		this.orderQueryService = orderQueryService;
	}
	@Autowired@Qualifier("synchActivityService")
	private SynchActivityService synchActivityService;
	public void setActivityRemoteService(SynchActivityService synchActivityService) {
		this.synchActivityService = synchActivityService;
	}
	@RequestMapping("/blog/sendActivityMsg.xhtml")
	public String sendSystemMsg(@CookieValue(value=LOGIN_COOKIE_NAME, required=false)String sessid,
			HttpServletRequest request, ModelMap model, Long activityid, String body, String msgtype, String memberids){
		String ip = WebUtils.getRemoteIp(request);
		Member member = loginService.getLogonMemberBySessid(ip, sessid);
		if(member==null) return showJsonError(model, "请先登陆！");
		ErrorCode<RemoteActivity> code = synchActivityService.getRemoteActivity(activityid);
		if(!code.isSuccess()) return showJsonError(model, code.getMsg());
		if(StringUtils.isNotBlank(memberids)){
			String[] joinmembers = memberids.split(",");
			List<Long> members = new ArrayList<Long>(joinmembers.length);
			for(String memberid : joinmembers){
				members.add(Long.valueOf(memberid));
			}
			ErrorCode<List<RemoteApplyjoin>> codeJoins = synchActivityService.getApplyJoinByMemberids(members, activityid);
			if(codeJoins.isSuccess() && codeJoins.getRetval() != null){
				RemoteActivity activity = code.getRetval();
				for(RemoteApplyjoin applyJoin : codeJoins.getRetval()){
					Member joinMember = daoService.getObject(Member.class, applyJoin.getMemberid());
					if(msgtype.equals("system")){//发送站内短信
						UserMessage userMessage = new UserMessage(""); 
						userMessage.setContent(request.getParameter("systembody"));
						String message= request.getParameter("systemtitle")+"来自"+activity.getTitle()+"的活动通知";
						userMessage.setSubject(message);
						daoService.saveObject(userMessage);
						
						UserMessageAction uma = new UserMessageAction(applyJoin.getMemberid());
						BindUtils.bindData(uma, request.getParameterMap());
						uma.setFrommemberid(member.getId());
						uma.setUsermessageid(userMessage.getId());
						if(uma.getGroupid()==null) {//新发表的情况
							uma.setGroupid(userMessage.getId());
						}
						daoService.saveObject(uma);
					}else if(msgtype.equals("mobile") && ValidateUtil.isMobile(joinMember.getMobile()) && StringUtils.isNotBlank(activity.getMobilemsg())){ //发送手机短信、是否发送短信
						SMSRecord sms = new SMSRecord(joinMember.getMobile());
						boolean isSendMsg = checkMsg(body);//手机短信中含有非法字符
						Timestamp curtime = new Timestamp(System.currentTimeMillis());
						if(isSendMsg){
							sms.setStatus(SmsConstant.STATUS_FILTER);
						}
						sms.setTradeNo("ac" + activityid);
						sms.setContent(body);
						sms.setSendtime(curtime);
						sms.setSmstype(SmsConstant.SMSTYPE_ACTIVITY);
						sms.setValidtime(DateUtil.addHour(curtime, 12));
						sms.setRelatedid(member.getId());
						untransService.addMessage(sms);
					}
				}
			}
		}
		return showJsonSuccess(model);
	}
	private boolean checkMsg(String msgContent) {
		String key = blogService.filterContentKey(msgContent);
		if (StringUtils.isNotBlank(key)) {
			return true;
		} else {
			return false;
		}
	}
	
	@RequestMapping("/blog/checkJoinInfo.xhtml")
	public String checkJoinInfo(@CookieValue(value=LOGIN_COOKIE_NAME, required=false)String sessid,
			HttpServletRequest request, Long relatedid, ModelMap model){
		Member member = loginService.getLogonMemberBySessid(WebUtils.getRemoteIp(request), sessid);
		if(member == null) return showJsonError_NOT_LOGIN(model);
		ErrorCode<RemoteActivity> code = synchActivityService.getRemoteActivity(relatedid);
		if(!code.isSuccess()) return showJsonError(model, code.getMsg());
		RemoteActivity activity = code.getRetval();
		Map<String, String> otherMap = JsonUtils.readJsonToMap(activity.getOtherinfo());
		MemberInfo memberInfo = daoService.getObject(MemberInfo.class, member.getId());
		if(StringUtils.isNotBlank(otherMap.get("bindMobile")) && !member.isBindMobile()){
			return showJsonError(model, "bindmobile");
		}
		if(StringUtils.isNotBlank(otherMap.get("bindEmail")) && !memberInfo.isFinishedTask(MemberConstant.TASK_CONFIRMREG)){
			return showJsonError(model, "bindEmail");
		}
		if(StringUtils.isNotBlank(otherMap.get("hasHeadUrl")) && StringUtils.isBlank(memberInfo.getHeadpic())){
			return showJsonError(model, "hasHeadUrl");
		}
		if(StringUtils.isNotBlank(otherMap.get("hasAddress"))&& StringUtils.isBlank(memberInfo.getAddress())){
			return showJsonError(model, "hasAddress");
		}
		if(StringUtils.isNotBlank(otherMap.get("newMember"))){
			String orderHis = orderQueryService.getMemberOrderHis(memberInfo.getId());
			if(StringUtils.isNotBlank(orderHis)) return showJsonError(model, "该活动仅限未购票用户参加！");
		}
		if(StringUtils.isNotBlank(otherMap.get("ticket"))){
			Long movieid = Long.parseLong(otherMap.get("ticket"));
			boolean bought = untransService.isPlayMemberByTagAndId(member.getId(), TagConstant.TAG_MOVIE, movieid);	
			if(!bought) return showJsonError(model, "该活动仅限购买指定电影的用户参加！");
		}
		if(StringUtils.isNotBlank(otherMap.get("usePoint"))){
			int usePoint = Integer.parseInt(otherMap.get("usePoint"));
			if(usePoint > memberInfo.getPointvalue()){
				return showJsonError(model, "参加该活动需要"+usePoint+"积分，您现有的积分不足！");
			}
		}
		return showJsonSuccess(model);
	}
	
	
}
