package com.gewara.web.action.api2mobile;

import java.util.List;

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
import com.gewara.util.BeanUtil;
import com.gewara.util.RelatedHelper;
import com.gewara.util.StringUtil;
import com.gewara.util.VmUtils;
import com.gewara.web.action.api.BaseApiController;

/**
 * 手机客户端站内信控制相关
 * 
 * @author taiqichao
 * 
 */
@Controller
public class Api2MobileMessageController extends BaseApiController {
	
	private final static String MSG_REG="^(\\d+;{0,1})+$";
	
	@Autowired
	@Qualifier("userMessageService")
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
	@RequestMapping("/api2/mobile/message/unReadSysMsgCounts.xhtml")
	public String sysMsgList(String memberEncode, ModelMap model) {
		Member member = memberService.getMemberByEncode(memberEncode);
		if (member == null){
			return getErrorXmlView(model, ApiConstant.CODE_MEMBER_NOT_EXISTS,"用户不存在！");
		}
		int count=userMessageService.getNotReadSysMessage(member.getId(),0L);
		return getMsgResult(model, "true", String.valueOf(count));
	}

	/**
	 * 拉取系统消息列表
	 * 
	 * @param key
	 * @param encryptCode
	 * @param memberEncode
	 * @param pageNo
	 * @param model
	 * @return
	 */
	@RequestMapping("/api2/mobile/message/sysMsgList.xhtml")
	public String sysMsgList(String memberEncode,
			@RequestParam(required = false, defaultValue = "0", value = "pageNo") Integer pageNo,
			ModelMap model) {

		Member member = memberService.getMemberByEncode(memberEncode);
		if (member == null){
			return getErrorXmlView(model, ApiConstant.CODE_MEMBER_NOT_EXISTS,"用户不存在！");
		}
		Integer rowsPerPage = 1000;
		List<SysMessageAction> sysMsgList = userMessageService.getSysMsgListByMemberid(member.getId(), null, pageNo* rowsPerPage, rowsPerPage);
		userMessageService.initSysMsgList(sysMsgList);
		List<Long> memberidList = BeanUtil.getBeanPropertyList(sysMsgList, Long.class, "frommemberid", true);
		addCacheMember(model, memberidList);
		for (SysMessageAction message : sysMsgList) {
			message.setBody(VmUtils.getText(message.getBody()));
		}
		RelatedHelper rh = new RelatedHelper(); 
		model.put("relatedHelper", rh);
		model.put("sysMsgList", sysMsgList);
		return getXmlView(model, "/api/mobile/message/sysMsgList.vm");
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
	@RequestMapping("/api2/mobile/message/updateSysMsgStatus.xhtml")
	public String updateSysMsgStatus(String memberEncode,
			@RequestParam(required = true, value = "msgids") String msgids,
			@RequestParam(required = true, value = "status") Integer status,
			ModelMap model) {

		Member member = memberService.getMemberByEncode(memberEncode);
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
		return getMsgResult(model, SUCCESS, "更新成功");
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
	@RequestMapping("/api2/mobile/message/delSysMsg.xhtml")
	public String delSysMsg(String memberEncode,
			@RequestParam(required = true, value = "msgids") String msgids,
			ModelMap model) {
		Member member = memberService.getMemberByEncode(memberEncode);
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
		return getMsgResult(model, SUCCESS, "删除成功");
	}

}
