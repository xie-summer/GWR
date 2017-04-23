package com.gewara.web.action.inner.mobile.member;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.gewara.constant.ApiConstant;
import com.gewara.model.user.Member;
import com.gewara.model.user.SysMessageAction;
import com.gewara.service.bbs.UserMessageService;
import com.gewara.util.StringUtil;
import com.gewara.web.action.inner.mobile.BaseOpenApiMobileController;
import com.gewara.web.filter.OpenApiMobileAuthenticationFilter;

@Controller
public class OpenApiMobileMemberMessageController extends BaseOpenApiMobileController{
private final static String MSG_REG="^(\\d+;{0,1})+$";
	
	@Autowired@Qualifier("userMessageService")
	private UserMessageService userMessageService;
	
	/**
	 * 查询未读系统消息数量
	 * 
	 * @param key
	 * @param encryptCode
	 * @param memberEncode
	 * @param model
	 * @return
	 */
	@RequestMapping("/openapi/mobile/member/unReadSysMsgCounts.xhtml")
	public String sysMsgList(String memberEncode, ModelMap model) {
		Member member = OpenApiMobileAuthenticationFilter.getOpenApiAuth().getMember();
		if (member == null){
			return getErrorXmlView(model, ApiConstant.CODE_MEMBER_NOT_EXISTS,"用户不存在！");
		}
		int count=userMessageService.getNotReadSysMessage(member.getId(),0L);
		return getSingleResultXmlView(model, count);
	}

	/**
	 * 获取系统消息列表
	 * 
	 * @param key
	 * @param encryptCode
	 * @param memberEncode
	 * @param pageNo
	 * @param model
	 * @return
	 */
	@RequestMapping("/openapi/mobile/member/sysMsgList.xhtml")
	public String sysMsgList(String memberEncode, Integer from, Integer maxnum, ModelMap model, HttpServletRequest request) {
		Member member = OpenApiMobileAuthenticationFilter.getOpenApiAuth().getMember();
		if (member == null){
			return getErrorXmlView(model, ApiConstant.CODE_MEMBER_NOT_EXISTS,"用户不存在！");
		}
		if(from==null) from = 0;
		if(maxnum==null) maxnum = 10;
		List<SysMessageAction> sysMsgList = userMessageService.getSysMsgListByMemberid(member.getId(), null, from, maxnum);
		getSysMsgListMap(sysMsgList, model, request);
		return getOpenApiXmlList(model);
	}
	
	

	/**
	 * 更新系统消息状态
	 * 
	 * @param key
	 * @param encryptCode
	 * @param memberEncode
	 * @param msgids
	 * @param status
	 * @param model
	 * @return
	 */
	@RequestMapping("/openapi/mobile/member/updateSysMsgStatus.xhtml")
	public String updateSysMsgStatus(String memberEncode,
			@RequestParam(required = true, value = "msgids") String msgids,
			@RequestParam(required = true, value = "status") Integer status,
			ModelMap model) {

		Member member = OpenApiMobileAuthenticationFilter.getOpenApiAuth().getMember();
		if (member == null){
			return getErrorXmlView(model, ApiConstant.CODE_MEMBER_NOT_EXISTS,"用户不存在！");
		}
			
		if (!StringUtil.regMatch(msgids, MSG_REG, true)) {
			return getMsgResult(model, FAIL, "消息id格式错误");
		}
		String[] ids = msgids.split(";");
		for (String id : ids) {
			Long sid = Long.valueOf(id);
			SysMessageAction sysMessage = daoService.getObject(SysMessageAction.class, sid);
			if (sysMessage != null) {
				if (status == 0) {
					sysMessage.setIsread(0L);
				} else if (status == 1) {
					sysMessage.setIsread(1L);
				}
				daoService.updateObject(sysMessage);
			}
		}
		return getSuccessXmlView(model);
	}
	
	
	/**
	 * 删除系统消息
	 * 
	 * @param key
	 * @param encryptCode
	 * @param memberEncode
	 * @param msgids
	 * @param model
	 * @return
	 */
	@RequestMapping("/openapi/mobile/member/delSysMsg.xhtml")
	public String delSysMsg(String memberEncode, String msgids, ModelMap model) {
		Member member = OpenApiMobileAuthenticationFilter.getOpenApiAuth().getMember();
		if (member == null){
			return getErrorXmlView(model, ApiConstant.CODE_MEMBER_NOT_EXISTS,"用户不存在！");
		}
			
		if (!StringUtil.regMatch(msgids, MSG_REG, true)) {
			return getMsgResult(model, FAIL, "消息id格式错误");
		}
		String[] ids = msgids.split(";");
		for (String id : ids) {
			Long sid = Long.valueOf(id);
			SysMessageAction sma = daoService.getObject(SysMessageAction.class, sid);
			if(null!=sma){
				if(sma.getFrommemberid()!=null){
					if(sma.getFrommemberid().longValue()!=member.getId().longValue() && sma.getTomemberid().longValue()!=member.getId().longValue()){
						continue;
					}
				}
				daoService.removeObject(sma);
			}
		}
		return getSuccessXmlView(model);
	}
}
