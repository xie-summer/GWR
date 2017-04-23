package com.gewara.web.action.inner.member;

import java.sql.Timestamp;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import com.gewara.constant.ApiConstant;
import com.gewara.constant.SmsConstant;
import com.gewara.model.pay.SMSRecord;
import com.gewara.model.user.Member;
import com.gewara.model.user.MemberInfo;
import com.gewara.service.bbs.BlogService;
import com.gewara.untrans.ShareService;
import com.gewara.util.BeanUtil;
import com.gewara.util.DateUtil;
import com.gewara.util.ValidateUtil;
import com.gewara.web.action.api.BaseApiController;

@Controller
public class InnerApiMemberController extends BaseApiController{
	
	@Autowired@Qualifier("shareService")
	private ShareService shareService;
	
	@Autowired@Qualifier("blogService")
	private BlogService blogService;

	/** * 分享微博 * @param tag * @param tagid * @param memberid * @param content * @param picURL
	*/
	@RequestMapping("/inner/member/shareInfo.xhtml")
	public String sendShareInfo(String tag, Long tagid, Long memberid, String content, String picUrl, ModelMap model){
		if(memberid == null) return getErrorXmlView(model,  ApiConstant.CODE_PARAM_ERROR, "用户ID不存在！");
		if(StringUtils.isBlank(content))  return getErrorXmlView(model,  ApiConstant.CODE_PARAM_ERROR, "哇啦内容不能为空！");
		try{
			shareService.sendShareInfo(tag, tagid, memberid, content, picUrl);
		}catch(Exception e){
			return getErrorXmlView(model, ApiConstant.CODE_DATA_ERROR, "分享微博失败！");
		}
		return getXmlView(model, "api/mobile/result.vm");
	}
	
	/**
	 * 获取用户信息
	 * @param memberId
	 * @return
	*/
	@RequestMapping("/inner/member/getMemberInfo.xhtml")
	 public String getMemberInfo(Long memberId, ModelMap model){
		 if(memberId == null) return getErrorXmlView(model,  ApiConstant.CODE_PARAM_ERROR, "参数错误！");
		 MemberInfo memberInfo = daoService.getObject(MemberInfo.class, memberId);
		 model.put("memberInfo", memberInfo);
		 return getXmlView(model, "api2/member/memberInfo.vm");
	 }
	
	/**
	 * 发送手机短信
	 * @param phones 手机号,以","分隔：18721511111,18721511112
	 * @param content
	 */
	@RequestMapping("/inner/activity/sendSMS.xhtml")
	public String sendSMS(String phones, String content, ModelMap model){
		if(phones == null) return getErrorXmlView(model, ApiConstant.CODE_PARAM_ERROR, "phones为空！");
		if(content == null) return getErrorXmlView(model, ApiConstant.CODE_PARAM_ERROR, "content为空！");
		String[] mobiles = StringUtils.split(phones, ",");
		boolean isSendMsg = StringUtils.isNotBlank(blogService.filterAllKey(content));//判断是否有标签和敏感文字
		Timestamp curtime = DateUtil.getCurFullTimestamp();
		for(int i = 0;i < mobiles.length;i++){
			if(ValidateUtil.isMobile(mobiles[i])){
				SMSRecord sms = new SMSRecord(mobiles[i]);
				if(isSendMsg){
					sms.setStatus(SmsConstant.STATUS_FILTER);
				}
				sms.setTradeNo(DateUtil.format(curtime, "yyMMddHHmmss"));
				sms.setContent(content);
				sms.setSendtime(curtime);
				sms.setSmstype(SmsConstant.SMSTYPE_MANUAL);
				sms.setValidtime(DateUtil.getLastTimeOfDay(curtime));
				sms = untransService.addMessage(sms);
				if(sms!=null)untransService.sendMsgAtServer(sms, true);
			}else{
				return getErrorXmlView(model, ApiConstant.CODE_PARAM_ERROR, "您输入的部分手机格式不正确！");
			}
		}
		return getSingleResultXmlView(model, "true");
	}
	
	/**
	 * 增加用户经验值
	 * @param memberId
	 * @param exp
	 */
	@RequestMapping("/inner/activity/addExpForMember.xhtml")
	public String addExpForMember(String memberEncode, Integer exp, ModelMap model){
		if(memberEncode == null) return getErrorXmlView(model, ApiConstant.CODE_PARAM_ERROR, "memberId为空！");
		Member member =  memberService.getMemberByEncode(memberEncode);
		if(member==null) return getErrorXmlView(model, ApiConstant.CODE_MEMBER_NOT_EXISTS, "用户不存在！");
		if(exp == null) return getErrorXmlView(model, ApiConstant.CODE_PARAM_ERROR, "exp为空！");
		memberService.addExpForMember(member.getId(), exp);
		return getSingleResultXmlView(model, true);
	}

	/**
	 * 获取用户昵称、头像信息
	 * @param ids
	 * @param model
	 * @return
	 */
	@RequestMapping("/inner/common/member/getIdList.xhtml")
	public String getMemberInfoList(String ids, ModelMap model){
		if(StringUtils.isBlank(ids)) return getErrorXmlView(model, ApiConstant.CODE_SIGN_ERROR, "参数错误！");
		List<Long> idList = BeanUtil.getIdList(ids, ",");
		addCacheMember(model, idList);
		model.put("memberIdList", idList);
		return getXmlView(model, "inner/common/cacheMemberList.vm");
	}
	
	/**
	 * 判断用户是否在黑名单中
	 * @param id
	 * @param model
	 * @return
	 */
	@RequestMapping("/inner/common/member/inBlackList.xhtml")
	public String inBlackList(Long memberId, ModelMap model){
		if(memberId == null) return getErrorXmlView(model, ApiConstant.CODE_SIGN_ERROR, "参数错误！");
		boolean result = false;
		if (blogService.isBlackMember(memberId)) result = true;
		return getSingleResultXmlView(model, result);
	}
	
}
