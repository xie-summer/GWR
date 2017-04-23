package com.gewara.web.action.user;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.gewara.constant.MemberConstant;
import com.gewara.constant.SysAction;
import com.gewara.constant.TagConstant;
import com.gewara.constant.sys.CacheConstant;
import com.gewara.constant.sys.JsonDataKey;
import com.gewara.constant.sys.LogTypeConstant;
import com.gewara.constant.sys.MongoData;
import com.gewara.model.bbs.commu.Commu;
import com.gewara.model.bbs.commu.CommuMember;
import com.gewara.model.common.JsonData;
import com.gewara.model.content.Bulletin;
import com.gewara.model.user.Friend;
import com.gewara.model.user.Member;
import com.gewara.model.user.MemberInfo;
import com.gewara.model.user.SysMessageAction;
import com.gewara.model.user.UserMessage;
import com.gewara.model.user.UserMessageAction;
import com.gewara.service.JsonDataService;
import com.gewara.service.bbs.BlogService;
import com.gewara.service.bbs.CommuService;
import com.gewara.service.bbs.UserMessageService;
import com.gewara.service.gewapay.ScalperService;
import com.gewara.service.member.FriendService;
import com.gewara.support.ErrorCode;
import com.gewara.support.ReadOnlyTemplate;
import com.gewara.untrans.CacheService;
import com.gewara.untrans.activity.SynchActivityService;
import com.gewara.untrans.monitor.MonitorService;
import com.gewara.util.BeanUtil;
import com.gewara.util.BindUtils;
import com.gewara.util.DateUtil;
import com.gewara.util.JsonUtils;
import com.gewara.util.StringUtil;
import com.gewara.util.VmUtils;
import com.gewara.util.WebUtils;
import com.gewara.util.XSSFilter;
import com.gewara.web.action.BaseHomeController;
import com.gewara.web.util.PageUtil;
import com.gewara.xmlbind.activity.RemoteActivity;

@Controller
public class UserMessageController extends BaseHomeController{
	@Autowired@Qualifier("readOnlyTemplate")
	private ReadOnlyTemplate readOnlyTemplate;
	public void setReadOnlyHibernateTemplate(ReadOnlyTemplate readOnlyTemplate) {
		this.readOnlyTemplate = readOnlyTemplate;
	}

	@Autowired@Qualifier("synchActivityService")
	private SynchActivityService synchActivityService;
	public void setActivityRemoteService(SynchActivityService synchActivityService) {
		this.synchActivityService = synchActivityService;
	}
	@Autowired@Qualifier("userMessageService")
	private UserMessageService userMessageService;
	public void setUserMessageService(UserMessageService userMessageService) {
		this.userMessageService = userMessageService;
	}
	@Autowired@Qualifier("blogService")
	private BlogService blogService = null;
	public void setBlogService(BlogService blogService) {
		this.blogService = blogService;
	}
	@Autowired@Qualifier("jsonDataService")
	private JsonDataService jsonDataService;
	public void setJsonDataService(JsonDataService jsonDataService) {
		this.jsonDataService = jsonDataService;
	}
	@Autowired@Qualifier("friendService")
	private FriendService friendService;
	public void setFriendService(FriendService friendService) {
		this.friendService = friendService;
	}
	@Autowired@Qualifier("commuService")
	private CommuService commuService;
	public void setCommuService(CommuService commuService) {
		this.commuService = commuService;
	}
	@Autowired@Qualifier("cacheService")
	private CacheService cacheService;
	public void setCacheService(CacheService cacheService) {
		this.cacheService = cacheService;
	}
	
	@Autowired@Qualifier("monitorService")
	private MonitorService monitorService;
	public void setMonitorService(MonitorService monitorService) {
		this.monitorService = monitorService;
	}

	@RequestMapping("/home/message/saveUserMessage.xhtml")
	public String saveUserMessage(HttpServletRequest request, Long tomemberid, String memberidList, String userKey,
			String captchaId, String captcha, ModelMap model){
		boolean isValidCaptcha = controllerService.validateCaptcha(captchaId, captcha, WebUtils.getRemoteIp(request));
		if(!isValidCaptcha) return showJsonError(model, "验证码错误！");
		Member member = this.getLogonMember();
		if (blogService.isBlackMember(member.getId())) return showJsonError(model, "黑名单中");
		Integer eggs = blogService.isBadEgg(member);
		if (eggs != 777) {
			return showJsonError(model, eggs+"");
		}
		if(!userMessageService.isSendMsg(member.getId())) {
			return showJsonError(model, "发送短消息不能太频繁！");
		}
		Member toMember = null;
		String content=XSSFilter.filterAttr(request.getParameter("content"));
		String subject=XSSFilter.filterAttr(request.getParameter("subject"));
		String key = blogService.filterContentKey(content+subject);
		if(StringUtils.isNotBlank(key)){
			return showJsonError(model, "内容含有非法关键字符！");
		}
		if (WebUtils.checkString(content)) return showJsonError(model, "内容含有非法字符！");
		List<Long> midList = BeanUtil.getIdList(memberidList, ",");
		if(tomemberid!=null) midList.add(tomemberid);
		// 输入框直接输入用户的xx
		if(StringUtils.isNotBlank(userKey)){
			List<String> userKeyList = Arrays.asList(StringUtils.split(userKey, ","));
			if(userKeyList.size()> 10) return showJsonError(model, "批量操作不能超过10个！");
			for (String nickname : userKeyList) {
				// email
				Member sendToMember = daoService.getObjectByUkey(Member.class, "email", nickname, false);
				if(sendToMember == null){
					// mobile
					sendToMember = daoService.getObjectByUkey(Member.class, "mobile", nickname, false);
				}
				if(sendToMember == null){
					// nickname
					sendToMember = daoService.getObjectByUkey(Member.class, "nickname", nickname, false);
				}
				if(sendToMember != null && !midList.contains(sendToMember.getId())){
					midList.add(sendToMember.getId());
				}
			}
		}
		List<UserMessageAction> userMessageActionList = new ArrayList<UserMessageAction>();
		for(Long mid: midList){
			toMember = daoService.getObject(Member.class, mid);
			if(toMember != null){
				UserMessage userMessage = new UserMessage(""); 
				userMessage.setContent(content);
				userMessage.setSubject(subject);
				if(StringUtils.isBlank(userMessage.getSubject())) 
					userMessage.setSubject(TagConstant.DEFAULT_SUBJECT);
				daoService.saveObject(userMessage);
				
				UserMessageAction uma = new UserMessageAction(toMember.getId());
				BindUtils.bindData(uma, request.getParameterMap());
				uma.setTomemberid(toMember.getId());
				uma.setFrommemberid(member.getId());
				uma.setUsermessageid(userMessage.getId());
				if(uma.getGroupid()==null) { //新发表的情况
					uma.setGroupid(userMessage.getId());
				}
				userMessageActionList.add(uma);
				// 记录添加私信发送记录，关联私信ID
				monitorService.saveAddLog(member.getId(), UserMessageAction.class, uma.getUsermessageid(), uma);
			}
		}
		// 记录发送私信的ip log
		dbLogger.errorWithType(LogTypeConstant.LOG_TYPE_USER_MESSAGE, "saveUserMessage:" + member.getId() + "," + WebUtils.getIpAndPort(WebUtils.getRemoteIp(request), request));
		if(userMessageActionList.isEmpty()) return showJsonError(model, "接收人为空，请确认输入的格式为Email、用户昵称或手机号！");
		daoService.saveObjectList(userMessageActionList);
		return showJsonSuccess(model);
	}
	//信息转发
	@RequestMapping("/home/message/sendMessage.xhtml")
	public String sendMessage(Long messageid, Long memberid, ModelMap model){
		Member member = this.getLogonMember();
		model.putAll(controllerService.getCommonData(model, member,member.getId()));
		if(messageid!=null){//转发
			UserMessage userMessage = daoService.getObject(UserMessage.class, messageid);
			model.put("userMessage", userMessage);
		}
		if(memberid!=null){
			Member toMember = daoService.getObject(Member.class, memberid);
			model.put("toMember", toMember);
		}
		List<Member> friendList = friendService.getFriendMemberList(member.getId(), 0, 60);
		model.put("friendList", friendList);
		addCacheMember(model, BeanUtil.getBeanPropertyList(friendList, Long.class, "id", true));
		List<Long> idList = friendService.getFriendIdList(member.getId(), 0, 100);
		ErrorCode<List<RemoteActivity>> code = synchActivityService.getFriendActivityList(null, idList, 0, 3);
		if(code.isSuccess()) model.put("activityList", code.getRetval());
		return "home/message/sendMessage.vm";
	}
	//我的收件箱 (站内短信)
	@RequestMapping("/home/message/receUserMsgList.xhtml")
	public String receiveMessageList(Integer pageNo, ModelMap model){
		Member member = getLogonMember();
		if(member==null) return  "redirect:/index.xhtml";
		model.putAll(controllerService.getCommonData(model, member, member.getId()));
		if(pageNo==null) pageNo=0;
		Integer rowsPerPage = 15;
		Integer count = userMessageService.getUMACountByMemberid(member.getId());
		List<UserMessageAction> umaList = userMessageService.getUMAListByMemberid(member.getId(), pageNo*rowsPerPage, rowsPerPage);
		Map<Long, Integer> messageNumMap = new HashMap<Long, Integer>();
		Map<Long, UserMessage> userMessageMap=new HashMap<Long, UserMessage>();
		for(UserMessageAction uma : umaList){
			Integer messageNum = userMessageService.getCountMessageByMessageActionId(uma.getGroupid());
			messageNumMap.put(uma.getId(), messageNum);
			userMessageMap.put(uma.getId(), daoService.getObject(UserMessage.class, uma.getUsermessageid()));
		}
		addCacheMember(model, BeanUtil.getBeanPropertyList(umaList, Long.class, "frommemberid", true));
		PageUtil pageUtil = new PageUtil(count, rowsPerPage, pageNo, "home/message/receUserMsgList.xhtml", true, true);
		pageUtil.initPageInfo();
		List<Member> friendList = friendService.getFriendMemberList(member.getId(), 0, 60);
		model.put("userMessageMap", userMessageMap);
		model.put("friendList", friendList);
		addCacheMember(model, BeanUtil.getBeanPropertyList(friendList, Long.class, "id", true));
		model.put("umaList", umaList);
		model.put("messageNumMap", messageNumMap);
		model.put("pageUtil", pageUtil);
		model.putAll(getNotReadMessage(model, member.getId()));
		
		String key = "USER_MSGCOUNT_" + member.getId();
		cacheService.remove(CacheConstant.REGION_TWOHOUR, key);
		memberService.getMemberNotReadMessageCount(member.getId());
		return "home/message/receMessageList.vm";
	}
	
	//我的发件箱 (站内短信)
	@RequestMapping("/home/message/sendUserMsgList.xhtml")
	public String sendMessageList(Integer pageNo, ModelMap model){
		Member member = getLogonMember();
		if(member==null) return "redirect:/index.xhtml";
		model.putAll(controllerService.getCommonData(model, member, member.getId()));
		if(pageNo==null) pageNo=0;
		Integer rowsPerPage = 15;
		Integer count = userMessageService.getSendUserMessageCountByMemberid(member.getId());
		List<UserMessageAction> umaList = userMessageService.getSendUserMessageListByMemberid(member.getId(), pageNo*rowsPerPage, rowsPerPage);
		Map<Long, UserMessage> userMessageMap=new HashMap<Long, UserMessage>();
		for(UserMessageAction userMessage : umaList){
			userMessageMap.put(userMessage.getId(), daoService.getObject(UserMessage.class, userMessage.getUsermessageid()));
		}
		addCacheMember(model, BeanUtil.getBeanPropertyList(umaList, Long.class, "frommemberid", true));
		addCacheMember(model, BeanUtil.getBeanPropertyList(umaList, Long.class, "tomemberid", true));
		PageUtil pageUtil = new PageUtil(count, rowsPerPage, pageNo, "home/message/sendUserMsgList.xhtml", true, true);
		pageUtil.initPageInfo();
		List<Member> friendList = friendService.getFriendMemberList(member.getId(), 0, 60);
		model.put("friendList", friendList);
		addCacheMember(model, BeanUtil.getBeanPropertyList(friendList, Long.class, "id", true));
		model.put("umaList", umaList);
		model.put("userMessageMap", userMessageMap);
		model.put("pageUtil", pageUtil);
		return "home/message/sendMessageList.vm";
	}
	//系统信息
	@RequestMapping("/home/message/sysMsgList.xhtml")
	public String receiveSystemMessageList(Integer pageNo, ModelMap model){
		Member member = this.getLogonMember();
		if(pageNo==null) pageNo=0;
		Integer rowsPerPage = 10;
		Integer count = userMessageService.getSysMsgCountByMemberid(member.getId(), null);
		List<SysMessageAction> sysMsgList = userMessageService.getSysMsgListByMemberid(member.getId(),null, pageNo*rowsPerPage, rowsPerPage);
		List<Long> memberidList = BeanUtil.getBeanPropertyList(sysMsgList, Long.class, "frommemberid", true);
		addCacheMember(model, memberidList);
		userMessageService.initSysMsgList(sysMsgList);
		PageUtil pageUtil = new PageUtil(count, rowsPerPage, pageNo, "/home/message/sysMsgList.xhtml", true, true);
		pageUtil.initPageInfo();
		model.putAll(controllerService.getCommonData(model, member, member.getId()));
		List<Member> friendList = friendService.getFriendMemberList(member.getId(), 0, 60);
		model.put("friendList", friendList);
		addCacheMember(model, BeanUtil.getBeanPropertyList(friendList, Long.class, "id", true));
		model.put("sysMember",daoService.getObject(Member.class, 1l));
		model.put("sysMsgList", sysMsgList);
		model.put("pageUtil", pageUtil);
		model.putAll(getNotReadMessage(model, member.getId()));
		
		// 查询群发系统消息
		List<JsonData> wsjlist = jsonDataService.getListByTag(JsonDataKey.KEY_WEBSITEMSG, DateUtil.getCurTruncTimestamp(), -1, -1);
		initJsonMap(wsjlist, model);
		model.put("wsjlist", wsjlist);
		MemberInfo memberInfo = daoService.getObject(MemberInfo.class, member.getId());
		String maxKey = VmUtils.getJsonValueByKey(memberInfo.getOtherinfo(), "maxKey");
		if(maxKey != null){
			model.put("maxKey", maxKey);
		}
		// 查询1vN系统消息
		Map<String, Object> params = new HashMap<String, Object>();
		params.put(MongoData.ACTION_MEMBERID, member.getId());
		params.put(MongoData.ACTION_MULTYWSMSG_ISDEL, "0");
		List<Map> multyMsgs = mongoService.find(MongoData.NS_ACTION_MULTYWSMSG, params);
		model.put("multyMsgs", multyMsgs);
		
		return "home/message/sysMessageList.vm";
	}
	private void initJsonMap(List<JsonData> list, ModelMap model){
		Map<String, Map<String, String>> dataMap = new HashMap<String, Map<String,String>>();
		for(JsonData jsonData : list){
			Map<String, String> map = VmUtils.readJsonToMap(jsonData.getData());
			dataMap.put(jsonData.getDkey(), map);
		}
		model.put("dataMap", dataMap);
	}
	@RequestMapping("/home/message/sysMessageDetail.xhtml")
	public String sysMessageDetail(ModelMap model,Long sid,Long memberid){
		Member member = getLogonMember();
		if(member==null) return "redirect:/index.xhtml";
		if(member.getId().equals(memberid)){
			SysMessageAction sysMessage=daoService.getObject(SysMessageAction.class, sid);
			if(sysMessage!=null){
				sysMessage.setIsread(1L);
				daoService.updateObject(sysMessage);
				model.put("sysMember", daoService.getObject(Member.class, sysMessage.getFrommemberid()));
			}else{
				return show404(model,"你查看的系统消息不存在或已被删除！");
			}
			List<Member> friendList = friendService.getFriendMemberList(member.getId(), 0, 60);
			model.put("friendList", friendList);
			model.put("sysMessage", sysMessage);
		}else{
			return show404(model,"不能查看他人的系统消息！");
		}
		model.putAll(controllerService.getCommonData(model, member, member.getId()));
		model.putAll(getNotReadMessage(model, member.getId()));
		return "home/message/sysMessage.vm";
	}
	
	//信息详细内容
	@RequestMapping("/home/message/userMessDetail.xhtml")
	public String userMessageDetail(Long mid, ModelMap model){
		Member member = getLogonMember();
		if(member==null) return "redirect:/index.xhtml";
		model.putAll(controllerService.getCommonData(model, member, member.getId()));
		UserMessageAction uma = userMessageService.getUserMessageActionByUserMessageid(mid);
		/**
		 * 修改显示内容
		 */
		if(TagConstant.ADMIN_FROMMEMBERID.equals(uma.getFrommemberid())){
			UserMessage userMessage = daoService.getObject(UserMessage.class, uma.getUsermessageid());
			userMessage.setContent(StringUtil.parse2HTML(userMessage.getContent()));
			uma.setUsermessageid(userMessage.getId());
		}
		if(!"toall".equals(uma.getStatus()) && !uma.getFrommemberid().equals(member.getId()) && !uma.getTomemberid().equals(member.getId())) return show404(model, "你无权查看他人的信息");
		List<UserMessageAction> umaList = userMessageService.getUserMessageListByGroupid(uma.getGroupid());
		Map<Long, UserMessage> userMessageMap=new HashMap<Long, UserMessage>();
		for(UserMessageAction ua : umaList){
			if(ua.getIsread().equals(TagConstant.READ_NO)) {
				ua.setIsread(TagConstant.READ_YES);
				daoService.saveObject(ua);
			}
			userMessageMap.put(ua.getId(), daoService.getObject(UserMessage.class, ua.getUsermessageid()));
		}
		model.put("userMessageMap", userMessageMap);
		model.put("umaList", umaList);
		addCacheMember(model, BeanUtil.getBeanPropertyList(umaList, Long.class, "frommemberid", true));
		UserMessage userMessage = daoService.getObject(UserMessage.class, umaList.get(0).getUsermessageid());
		model.put("userMeSubject",userMessage.getSubject());
		model.put("uma", uma);
		model.putAll(getNotReadMessage(model, member.getId()));
		
		cacheService.remove(CacheConstant.REGION_TWOHOUR, "USER_MSGCOUNT_" + member.getId());
		return "home/message/messageDetail.vm";
	}
	//系统信息审核
	@RequestMapping("/home/message/checkst.xhtml")
	public String checkst(Long sid, String st, ModelMap model){
		Member member = getLogonMember();
		if(member==null) return showJsonError(model, "请先登录");
		SysMessageAction sma = daoService.getObject(SysMessageAction.class, sid);
		if(!sma.getTomemberid().equals(member.getId())) return showJsonError(model, "你没有权限");
		if(StringUtils.equals("a", st)) {
			sma.setStatus(SysAction.STATUS_AGREE);
		}else if(StringUtils.equals("r", st)){
			sma.setStatus(SysAction.STATUS_REFUSE);
		}
		daoService.saveObject(sma);
		String str = null;
		SysMessageAction newsma = new SysMessageAction(SysAction.STATUS_RESULT);
		newsma.setFrommemberid(member.getId());
		newsma.setTomemberid(sma.getFrommemberid());
		newsma.setActionid(sma.getId());
		if(sma.getAction().equals(SysAction.ACTION_APPLY_FRIEND_ADD)){ //加好友
			if("a".equals(st)) {
				Friend f1 = new Friend(member.getId(), sma.getFrommemberid());
				Friend f2 = new Friend(sma.getFrommemberid(), member.getId());
				daoService.saveObjectList(f1, f2);
				str = member.getNickname() + "同意你加好友的请求!" ;
			}else if("r".equals(st)) {
				str = member.getNickname() + "拒绝你加好友的请求!" ;
			}
		}else if(sma.getAction().equals(SysAction.ACTION_APPLY_COMMU_INVITE)){ //圈子邀请
			Commu c = daoService.getObject(Commu.class, sma.getActionid());
			if("a".equals(st)) {
				c.addCommumembercount();
				daoService.updateObject(c);
				CommuMember cm = new CommuMember(member.getId());
				cm.setCommuid(sma.getActionid());
				daoService.saveObject(cm);
				str = member.getNickname() + " 同意加入圈子 " + c.getName();
			}else if("r".equals(st)) {
				str = member.getNickname() + " 拒绝加入圈子 " + c.getName();
			}
		}else if(sma.getAction().equals(SysAction.ACTION_APPLY_C_ADMIN)){//圈子管理员
			Commu c = daoService.getObject(Commu.class, sma.getActionid());
			if("a".equals(st)) {
				boolean isE = commuService.isCommuMember(sma.getActionid(), c.getAdminid());
				if(!isE) {
					CommuMember cm = new CommuMember(member.getId());
					cm.setCommuid(sma.getActionid());
					daoService.saveObject(cm);
				}
				c.setAdminid(member.getId());
				daoService.saveObject(c);
				str = member.getNickname() + " 同意你为圈子 " + c.getName() +" 管理员";
				memberService.saveNewTask(member.getId(), MemberConstant.TASK_JOINCOMMU);
			}else {
				str = member.getNickname() + " 拒绝你为圈子 " + c.getName() +" 管理员";
			}
			
		}else if(sma.getAction().equals(SysAction.ACTION_APPLY_COMMU_JOIN)){ //申请加入圈子
			Commu c = daoService.getObject(Commu.class, sma.getActionid());
			if("a".equals(st)) {
				SysMessageAction sys = daoService.getObject(SysMessageAction.class, sid);
				CommuMember commuMember = new CommuMember(sys.getFrommemberid());
				commuMember.setCommuid(sys.getActionid());
				daoService.saveObject(commuMember);
				c.addCommumembercount();
				daoService.updateObject(c);
				str = "圈主 " + member.getNickname() + " 同意你加入圈子 " + c.getName();
			}else if("r".equals(st)) {
				str = "圈主 " + member.getNickname() + " 拒绝你加入圈子 " + c.getName();
			}
		}
		if(StringUtils.isNotBlank(str)) {
			newsma.setBody(str);
			daoService.saveObject(newsma);
		}
		return showJsonSuccess(model);
	}
	//删除系统消息
	@RequestMapping("/home/message/delSysMsg.xhtml")
	public String delSysMsg(Long sid, ModelMap model){
		Member member = getLogonMember();
		if(member==null) return showJsonError(model, "请先登录");
		SysMessageAction sma = daoService.getObject(SysMessageAction.class, sid);
		if(sma != null){
			if(sma.getFrommemberid()!=null){
				if(sma.getFrommemberid().longValue()!=member.getId().longValue() 
						&& sma.getTomemberid().longValue()!=member.getId().longValue()){
					return showJsonError(model, "你没有权限");
				}
			}
			daoService.removeObject(sma);
		}
		return showJsonSuccess(model);
	}
	//删除信息
	@RequestMapping("/home/message/delUserMsg.xhtml")
	public String delUserMsg(Long sid, ModelMap model){
		Member member = getLogonMember();
		if(member==null) return showJsonError(model, "请先登录");
		UserMessageAction uma = daoService.getObject(UserMessageAction.class, sid);
		if(uma.getFrommemberid().longValue()!=member.getId().longValue() 
				&& uma.getTomemberid().longValue()!=member.getId().longValue()){
			return showJsonError(model, "你没有权限");
		}
		if("0".equals(uma.getStatus())){
			if(uma.getFrommemberid().equals(member.getId())){
				uma.setStatus(TagConstant.STATUS_FDEL);
				daoService.saveObject(uma);
			}else if(uma.getTomemberid().equals(member.getId())){
				uma.setStatus(TagConstant.STATUS_TDEL);
				daoService.saveObject(uma);
			}
		}else if(uma.getStatus().indexOf("del")>=0){
			UserMessage userMessage = daoService.getObject(UserMessage.class, uma.getUsermessageid());
			daoService.removeObject(userMessage);
			daoService.removeObject(uma);
		}
		
		return showJsonSuccess(model);
	}
	@RequestMapping("/home/message/sysmsgBulletin.xhtml")
	public String sysmsgBulletin(ModelMap model){
		String query = "from Bulletin n where n.bulletintype=? and n.relatedid=? and (n.validtime>=? or n.validtime=null) order by n.posttime desc";
		List<Bulletin> bulletinList = readOnlyTemplate.find(query, Bulletin.BULLETION_SYSMSG, 0L, DateUtil.getCurDate());
		model.put("bulletinList", bulletinList);
		return "";
	}
	
	private Map getNotReadMessage(ModelMap model, Long memberid){
		model.put("messageCount", memberService.getMemberNotReadNormalMessageCount(memberid));
		model.put("sysMessageCount", memberService.getMemberNotReadSysMessageCount(memberid));
		return model;
	}

	/**
	 *  设置所有未读系统消息为已读
	 */
	public void setAllSysMsgRead(Long memberid){
		List<SysMessageAction> list = userMessageService.getNotReadSysMessageList(memberid, 0L);
		if(list != null && list.size() > 0){
			for(SysMessageAction sysMessageAction : list){
				sysMessageAction.setIsread(1L);
				daoService.saveObject(sysMessageAction);
			}
		}
		
		// 2, 更新群发站内信未读数
		MemberInfo memberInfo = daoService.getObject(MemberInfo.class, memberid);
		Map<String, String> map = VmUtils.readJsonToMap(memberInfo.getOtherinfo());
		List<JsonData> list2 = jsonDataService.getListByTag(JsonDataKey.KEY_WEBSITEMSG, DateUtil.getCurTruncTimestamp(), 0, 1);
		if(list2 != null && list2.size() > 0){
			map.put("maxKey", list2.get(0).getDkey());
		}
		memberInfo.setOtherinfo(JsonUtils.writeMapToJson(map));
		
		// 3, 更新1vN系统消息
		// 查找用户所有isread为0, 设置为1
		Map<String, Object> params = new HashMap<String, Object>();
		params.put(MongoData.ACTION_MEMBERID, memberid);
		params.put(MongoData.ACTION_MULTYWSMSG_ISREAD, "0");
		List<Map> multyMsgs = mongoService.find(MongoData.NS_ACTION_MULTYWSMSG, params);
		for(Map data : multyMsgs){
			data.put(MongoData.ACTION_MULTYWSMSG_ISREAD, "1");
			mongoService.saveOrUpdateMap(data, MongoData.DEFAULT_ID_NAME, MongoData.NS_ACTION_MULTYWSMSG);
		}
		daoService.saveObject(memberInfo);
	}
	
	/**
	 *  1vN系统消息 删除
	 * */
	@RequestMapping("/home/message/delMultySysMsg.xhtml")
	public String delMultySysMsg(ModelMap model, String id){
		Map<String, Object> dataMap = mongoService.findOne(MongoData.NS_ACTION_MULTYWSMSG, MongoData.DEFAULT_ID_NAME, id);
		if(dataMap == null) return showJsonError(model, "找不到该记录，可能已被删除！");
		dataMap.put(MongoData.ACTION_MULTYWSMSG_ISDEL, "1");
		mongoService.saveOrUpdateMap(dataMap, MongoData.DEFAULT_ID_NAME, MongoData.NS_ACTION_MULTYWSMSG);
		return showJsonSuccess(model);
	}
	
	/**
	 * 当系统消息请求完成后, 页面Ajax 请求所有消息为已读
	 */
	@RequestMapping("/home/message/sysMsgAllRead.xhtml")
	public String sysMsgAllRead(ModelMap model){
		Member member = this.getLogonMember();
		if(member==null) return showJsonError_NOT_LOGIN(model);
		// 用户成功请求之后, 设置所有的未读系统信息为已读
		setAllSysMsgRead(member.getId());
		
		String key = "USER_MSGCOUNT_" + member.getId();
		cacheService.remove(CacheConstant.REGION_TWOHOUR, key);
		memberService.getMemberNotReadMessageCount(member.getId());
		return showJsonSuccess(model);
	}
	
	@Autowired@Qualifier("scalperService")
	private ScalperService scalperService;
	@RequestMapping("/testIPScalper.xhtml")
	@ResponseBody
	public String testIPScalper(){
		Map<String, List<Map>> map = scalperService.getSuspectScalperByIp(-730*24, 2);
		return map.toString();
	}
}
