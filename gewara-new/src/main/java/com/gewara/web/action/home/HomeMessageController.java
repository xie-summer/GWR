package com.gewara.web.action.home;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.gewara.constant.MemberConstant;
import com.gewara.constant.TagConstant;
import com.gewara.constant.sys.CacheConstant;
import com.gewara.constant.sys.JsonDataKey;
import com.gewara.constant.sys.MongoData;
import com.gewara.helper.sys.RelateClassHelper;
import com.gewara.json.MemberStats;
import com.gewara.model.common.City;
import com.gewara.model.common.County;
import com.gewara.model.common.JsonData;
import com.gewara.model.user.Member;
import com.gewara.model.user.MemberInfo;
import com.gewara.model.user.SysMessageAction;
import com.gewara.model.user.Treasure;
import com.gewara.model.user.UserMessage;
import com.gewara.model.user.UserMessageAction;
import com.gewara.service.JsonDataService;
import com.gewara.service.bbs.BlogService;
import com.gewara.service.bbs.UserMessageService;
import com.gewara.service.content.RecommendService;
import com.gewara.service.member.FriendService;
import com.gewara.support.ServiceHelper;
import com.gewara.untrans.CacheService;
import com.gewara.untrans.CommentService;
import com.gewara.untrans.WalaApiService;
import com.gewara.untrans.monitor.MonitorService;
import com.gewara.util.BeanUtil;
import com.gewara.util.DateUtil;
import com.gewara.util.StringUtil;
import com.gewara.util.VmUtils;
import com.gewara.util.WebUtils;
import com.gewara.web.action.BaseHomeController;
import com.gewara.web.util.PageUtil;
import com.gewara.xmlbind.bbs.Comment;
import com.gewara.xmlbind.bbs.ReComment;


/**
 * 消息控制器
 * @author taiqichao
 *
 */
@Controller
public class HomeMessageController extends BaseHomeController{
	private static final Integer MAXNUMS = 20;
	@Autowired
	@Qualifier("userMessageService")
	private UserMessageService userMessageService;
	
	@Autowired
	@Qualifier("friendService")
	private FriendService friendService;
	
	@Autowired
	@Qualifier("jsonDataService")
	private JsonDataService jsonDataService;
	
	@Autowired
	@Qualifier("recommendService")
	private RecommendService recommendService;
	
	@Autowired
	@Qualifier("commentService")
	protected CommentService commentService;
	
	@Autowired
	@Qualifier("blogService")
	protected BlogService blogService;
	
	@Autowired
	@Qualifier("cacheService")
	private CacheService cacheService;
	
	@Autowired@Qualifier("walaApiService")
	private WalaApiService walaApiService;
	
	@Autowired@Qualifier("monitorService")
	private MonitorService monitorService;
	
	/**
	 * @ 我首页
	 * @return
	 */
	@RequestMapping("/home/message/wala/atme.xhtml")
	public String atMeWalaIndex(ModelMap model){
		Member member = getLogonMember();
		model.putAll(controllerService.getCommonData(model, member, member.getId()));
		this.getHomeLeftNavigate(daoService.getObject(MemberInfo.class, member.getId()), model);
		return "sns/message/atMeIndex.vm";
	}
	
	
	/**
	 * @ 我的哇啦
	 * @return
	 */
	@RequestMapping("/home/message/wala/loadAtMeWaLa.xhtml")
	public String loadAtMeWaLa(
			Integer pageNo,
			ModelMap model,
			@CookieValue(value=LOGIN_COOKIE_NAME, required=false)String sessid,
			HttpServletRequest request){
		if(pageNo == null) pageNo = 0;
		Integer from = pageNo * MAXNUMS;
		Member member = loginService.getLogonMemberBySessid(WebUtils.getRemoteIp(request), sessid);
		List<Comment> commentList=commentService.getMicroBlogListByMemberid("@" + member.getNickname() + " ", member.getId(), from, MAXNUMS);
		model.putAll(commentService.getAllCommentList(commentList,"commentList"));
		addCacheMember(model, ServiceHelper.getMemberIdListFromBeanList(commentList));
		model.put("logonMember", member);
		return "sns/message/wala.vm";
	}
	
	/**
	 * @ 我的搭话
	 * @return
	 */
	@RequestMapping("/home/message/wala/loadAtMeReply.xhtml")
	public String loadAtMeReply(
			Integer pageNo,
			@CookieValue(value=LOGIN_COOKIE_NAME, required=false)String sessid,
			ModelMap model,
			HttpServletRequest request){
		if(pageNo == null) pageNo = 0;
		Integer from = pageNo * MAXNUMS;
		Member member = loginService.getLogonMemberBySessid(WebUtils.getRemoteIp(request), sessid);
		List<ReComment> commentList=walaApiService.getReplyCommentListByAtMe(member.getNickname(), from, MAXNUMS);
		Map<Long,ReComment> replyMap = new HashMap<Long, ReComment>();
		for (ReComment reComment : commentList) {
			if(StringUtils.equals(reComment.getTag(), ReComment.TAG_RECOMMENT)){
				ReComment re = walaApiService.getReCommentById(reComment.getTransferid());//daoService.getObject(ReComment.class, reComment.getTransferid());
				replyMap.put(reComment.getId(), re);
			}
		}
		model.put("commentList", commentList);
		model.put("replyMap", replyMap);
		addCacheMember(model, ServiceHelper.getMemberIdListFromBeanList(commentList));
		model.put("logonMember", member);
		return "sns/message/walaReply.vm";
	}
	
	/**
	 * 搭话 首页
	 * @return
	 */
	@RequestMapping("/home/message/wala/reply.xhtml")
	public String walaReplyIndex(ModelMap model){
		Member member = getLogonMember();
		model.putAll(controllerService.getCommonData(model, member, member.getId()));
		this.getHomeLeftNavigate(daoService.getObject(MemberInfo.class, member.getId()), model);
		return "sns/message/replyIndex.vm";
	}
	
	/**
	 * 收到的搭话
	 * @param pageNo
	 * @param model
	 * @param sessid
	 * @param request
	 * @return
	 */
	@RequestMapping("/home/message/wala/loadReCommentData.xhtml")
	public String loadReCommentData(Integer pageNo,
			@CookieValue(value=LOGIN_COOKIE_NAME, required=false)String sessid,
			HttpServletRequest request,ModelMap model){
		if(pageNo == null) pageNo =0;
		Integer from = pageNo *MAXNUMS;
		Member member = loginService.getLogonMemberBySessid(WebUtils.getRemoteIp(request), sessid);
		walaApiService.updateReplyCommentReadSatus(member.getId());//更新阅读状态
		List<ReComment> commentList = walaApiService.getReplyMeReCommentList(member.getId(), from, MAXNUMS);
		Map<Long,ReComment> replyMap = new HashMap<Long, ReComment>();
		for (ReComment reComment : commentList) {
			if(StringUtils.equals(reComment.getTag(), ReComment.TAG_RECOMMENT)){
				ReComment re = walaApiService.getReCommentById(reComment.getTransferid());//daoService.getObject(ReComment.class, reComment.getTransferid());
				replyMap.put(reComment.getId(), re);
			}
		}
		addCacheMember(model, ServiceHelper.getMemberIdListFromBeanList(commentList));
		model.put("commentList", commentList);
		model.put("replyMap", replyMap);
		model.put("logonMember", member);
		return "sns/message/walaReply.vm";
	}
	
	
	
	/**
	 * 我发出的搭话
	 * @param pageNo
	 * @param model
	 * @param sessid
	 * @param request
	 * @return
	 */
	@RequestMapping("/home/message/wala/loadMyReCommentData.xhtml")
	public String loadMyReCommentData(Integer pageNo,
			@CookieValue(value=LOGIN_COOKIE_NAME, required=false)String sessid,
			HttpServletRequest request,ModelMap model){
		if(pageNo == null) pageNo =0;
		Integer from = pageNo *MAXNUMS;
		Member member = loginService.getLogonMemberBySessid(WebUtils.getRemoteIp(request), sessid);
		List<ReComment> commentList = walaApiService.getMicroSendReCommentList(member.getId(), from, MAXNUMS);
		Map<Long,ReComment> replyMap = new HashMap<Long, ReComment>();
		for (ReComment reComment : commentList) {
			if(StringUtils.equals(reComment.getTag(), ReComment.TAG_RECOMMENT)){
				ReComment re = walaApiService.getReCommentById(reComment.getTransferid());//daoService.getObject(ReComment.class, reComment.getTransferid());
				replyMap.put(reComment.getId(), re);
			}
		}
		addCacheMember(model, ServiceHelper.getMemberIdListFromBeanList(commentList));
		model.put("commentList", commentList);
		model.put("replyMap", replyMap);
		model.put("logonMember", member);
		return "sns/message/walaReply.vm";
	}
	
	/**
	 * 粉丝列表
	 * @param memberid
	 * @param pageNo
	 * @param sessid
	 * @param model
	 * @param request
	 * @return
	 */
	@RequestMapping("/home/wala/fans.xhtml")
	public String microFansList(Long memberid, @RequestParam(defaultValue="0",required=false,value="pageNo") Integer pageNo, ModelMap model){
		Integer maxNum = 15;
		Integer from = pageNo * maxNum;
		
		Member logonMember = getLogonMember();
		model.put("logonMember", logonMember);
		
		Member member = null;
		if(memberid != null) {
			member = daoService.getObject(Member.class, memberid);
		}else{
			member = logonMember;
		}
		
		//用户关注/微博/粉丝数量
		Map dataMap = memberCountService.getMemberCount(member.getId());
		
		//关注数
		Integer attentionCount = 0;
		if(dataMap!=null){
			attentionCount = (Integer)dataMap.get(MemberStats.FIELD_ATTENTIONCOUNT);
			if(attentionCount==null){
				attentionCount=0;
			} 
		}
		model.put("attentionCount", attentionCount);
		
		// 粉丝数
		Integer fansCount = 0;
		if(dataMap!=null){
			fansCount = (Integer)dataMap.get(MemberStats.FIELD_FANSCOUNT);
			if(fansCount==null){
				fansCount = 0;
			} 
		}
		model.put("myFansCount", fansCount);
		
		//粉丝列表
		List<Long> microFansIdList = blogService.getFanidListByMemberId(member.getId(), from, maxNum);
		
		List<MemberInfo> memberInfoList = daoService.getObjectList(MemberInfo.class, microFansIdList);
		treasureMemberInfoMap(model, memberInfoList, member, true);
		PageUtil pageUtil = new PageUtil(fansCount,maxNum,pageNo,"home/wala/fans.xhtml", true, true);
		Map params = new HashMap();
		params.put("memberid",memberid);
		pageUtil.initPageInfo(params);
		model.put("pageUtil",pageUtil);
		model.put("microFansList", microFansIdList);
		addCacheMember(model, microFansIdList);
		model.put("member", member);
		MemberInfo memberinfo = daoService.getObject(MemberInfo.class, member.getId());
		this.getHomeLeftNavigate(memberinfo, model);
		model.put("memberInfo", memberinfo);
		//清除粉丝提示信息
		recommendService.memberAddFansCount(member.getId(), "remove", MongoData.MESSAGE_FANS, null);
		return "sns/message/fans.vm";
	}
	
	/**
	 * 获取用户市，区信息,粉丝数量，与当前登陆用户是否有关注关系
	 */
	protected void treasureMemberInfoMap(ModelMap model,List<MemberInfo> memberInfoList, Member member, boolean isLoadNewComment){
		Map<Long,String> memberAreaMap = new HashMap<Long, String>();//市，区
		Map<Long,Integer> moderatorFansMap = new HashMap<Long, Integer>();//粉丝数量
		Map<Long,Integer> modertorFollowMap = new HashMap<Long, Integer>();//关注数量
		Map<Long,Integer> modertorWalaMap = new HashMap<Long, Integer>();//哇啦数量
		Map<Long,Boolean> isTreasureMember = new HashMap<Long, Boolean>();//是否关注过
		Map<Long, Comment> commentMap = new HashMap<Long, Comment>();// 关注的对象，所关联的最新的一条评论信息
		Map<Long,String> sexMap = new HashMap<Long,String>();//标识用户性别
		Map<Long,MemberInfo> modertorMemberInfoMap = new HashMap<Long, MemberInfo>();//会员信息
		for (MemberInfo memberInfo : memberInfoList) {
			modertorMemberInfoMap.put(memberInfo.getId(), memberInfo);
			sexMap.put(memberInfo.getId(),memberInfo.getSex());
			Map dataMap = memberCountService.getMemberCount(memberInfo.getId());
			
			//粉丝数
			Integer fansCount = 0;
			if(dataMap!=null){
				 fansCount = (Integer)dataMap.get(MemberStats.FIELD_FANSCOUNT);
				 if(fansCount==null) fansCount = 0;
			}
			moderatorFansMap.put(memberInfo.getId(),fansCount);
			
			//关注数
			Integer attentionCount = 0;
			if(dataMap!=null){
				attentionCount = (Integer)dataMap.get(MemberStats.FIELD_ATTENTIONCOUNT);
				if(attentionCount==null){
					attentionCount=0;
				} 
			}
			modertorFollowMap.put(memberInfo.getId(), attentionCount);
			
			//哇啦数
			Integer walaCount=0;
			if(dataMap!=null){
				walaCount = (Integer)dataMap.get(MemberStats.FIELD_COMMENTCOUNT);
				if(walaCount==null){
					walaCount=0;
				} 
			}
			modertorWalaMap.put(memberInfo.getId(), walaCount);
			
			City city = daoService.getObject(City.class, memberInfo.getLivecity());
			County county = daoService.getObject(County.class, memberInfo.getLivecounty());
			memberAreaMap.put(memberInfo.getId(), (city !=null?city.getCityname():"")+(county != null?"，"+county.getCountyname():""));
			isTreasureMember.put(memberInfo.getId(), blogService.isTreasureMember(member.getId(),memberInfo.getId()));
			if(isLoadNewComment){
				Comment comment = commentService.getNewCommentByRelatedid(TagConstant.TAG_TOPIC, null, memberInfo.getId());
				commentMap.put(memberInfo.getId(), comment);
				model.put("treasureMemberNewCommentMap", commentMap);
			}
		}
		model.put("sexMap", sexMap);
		model.put("moderatorFansMap", moderatorFansMap);
		model.put("modertorFollowMap", modertorFollowMap);
		model.put("modertorWalaMap", modertorWalaMap);
		model.put("isTreasureMember", isTreasureMember);
		model.put("modertorMemberInfoMap", modertorMemberInfoMap);
		model.put("memberAreaMap", memberAreaMap);
	}
	
	
	
	/**
	 * 我关注的人
	 * @param model
	 * @param memberid
	 * @param pageNo
	 * @param type
	 * @param sessid
	 * @param request
	 * @return
	 */
	@RequestMapping("/home/wala/follow.xhtml")
	public String microFriendsList(ModelMap model, Long memberid,
			@RequestParam(defaultValue="0",required=false,value="pageNo") Integer pageNo){
		Integer maxNum = 15;
		
		Member logonMember = getLogonMember();
		Member member = null;
		model.put("logonMember", logonMember);
		if(memberid != null){
			member = daoService.getObject(Member.class, memberid);
		}else{ 
			member = logonMember;
		}
		
		//用户关注/微博/粉丝数量
		Map dataMap = memberCountService.getMemberCount(member.getId());
		//关注数
		Integer attentionCount = 0;
		if(dataMap!=null){
			attentionCount = (Integer)dataMap.get(MemberStats.FIELD_ATTENTIONCOUNT);
			if(attentionCount==null){
				attentionCount=0;
			} 
		}
		model.put("attentionCount", attentionCount);
		
		// 粉丝数
		Integer fansCount = 0;
		if(dataMap!=null){
			fansCount = (Integer)dataMap.get(MemberStats.FIELD_FANSCOUNT);
			if(fansCount==null){
				fansCount = 0;
			} 
		}
		model.put("myFansCount", fansCount);
		
		List<Treasure> treasureList = blogService.getTreasureListByMemberId(member.getId(), new String[]{Treasure.TAG_MEMBER},null, null, pageNo * maxNum,maxNum, Treasure.ACTION_COLLECT);
		Map params = new HashMap();
		params.put("memberid", memberid);
		PageUtil pageUtil = new PageUtil(attentionCount, maxNum, pageNo, "home/wala/follow.xhtml", true, true);
		pageUtil.initPageInfo(params);
		
		List<Long> treasureIds=new ArrayList<Long>();
		Map<Long, Integer> fansMap = new HashMap<Long, Integer>();// 粉丝数量
		Map<Long,Integer> modertorFollowMap = new HashMap<Long, Integer>();//关注数量
		Map<Long,Integer> modertorWalaMap = new HashMap<Long, Integer>();//哇啦数量
		Map<Long,MemberInfo> modertorMemberInfoMap = new HashMap<Long, MemberInfo>();//会员信息
		Map<Long, Comment> commentMap = new HashMap<Long, Comment>();// 关注的对象，所关联的最新的一条评论信息
		Map<Long,String> microMemberAreaMap = new HashMap<Long, String>();//市，区
		Map<Long,Boolean> isAttentionMeMap = new HashMap<Long, Boolean>();//判断粉丝是否关注了当前登录用户
		Map<Long,String> sexMap = new HashMap<Long,String>();//标识用户性别
		Map<Long, Treasure> memberTreasure = new HashMap<Long, Treasure>();// 粉丝数量
		for (Treasure treasure : treasureList) {
			if (RelateClassHelper.getRelateClazz(treasure.getTag()) != null) {
				Comment comment = commentService.getNewCommentByRelatedid(TagConstant.TAG_TOPIC, null, treasure.getRelatedid());
				MemberInfo memberInfo = daoService.getObject(MemberInfo.class, treasure.getRelatedid());
				if(memberInfo != null){
					memberTreasure.put(memberInfo.getId(), treasure);
					modertorMemberInfoMap.put(memberInfo.getId(), memberInfo);
					treasureIds.add(memberInfo.getId());
					City city = daoService.getObject(City.class, memberInfo.getLivecity());
					County county = daoService.getObject(County.class, memberInfo.getLivecounty());
					microMemberAreaMap.put(memberInfo.getId(), (city !=null?city.getCityname():"")+(county != null?","+county.getCountyname():""));
					sexMap.put(memberInfo.getId(), memberInfo.getSex());
					commentMap.put(memberInfo.getId(), comment);
					
					Map dataMapFans = memberCountService.getMemberCount(memberInfo.getId());
					Integer fansct = 0;
					Integer ac = 0;
					Integer walaCount=0;
					if(dataMapFans!=null){
						fansct = (Integer)dataMapFans.get(MemberStats.FIELD_FANSCOUNT);
						if(fansct==null) fansct = 0;
						if(dataMapFans.get(MemberStats.FIELD_ATTENTIONCOUNT) != null){
							//关注数
							ac = (Integer)dataMapFans.get(MemberStats.FIELD_ATTENTIONCOUNT);
						}
						if(dataMapFans.get(MemberStats.FIELD_COMMENTCOUNT) != null) {
							//哇啦数
							walaCount = (Integer)dataMapFans.get(MemberStats.FIELD_COMMENTCOUNT);
						}
					}
					modertorFollowMap.put(memberInfo.getId(), ac);
					fansMap.put(memberInfo.getId(), fansct);
					modertorWalaMap.put(memberInfo.getId(), walaCount);
				}
			}
		}
		model.put("sexMap", sexMap);
		model.put("isAttentionMeMap", isAttentionMeMap);
		model.put("microMemberAreaMap",microMemberAreaMap);
		model.put("pageUtil", pageUtil);
		model.put("microFansMap", fansMap);
		model.put("modertorFollowMap", modertorFollowMap);
		model.put("modertorWalaMap", modertorWalaMap);
		model.put("commentMap", commentMap);
		model.put("treasureIds", treasureIds);
		model.put("modertorMemberInfoMap", modertorMemberInfoMap);
		model.put("memberTreasure", memberTreasure);
		model.put("member", member);
		addCacheMember(model, treasureIds);
		MemberInfo memberInfo = daoService.getObject(MemberInfo.class, member.getId());
		this.getHomeLeftNavigate(memberInfo, model);
		model.put("memberInfo", memberInfo);
		return "sns/message/follow.vm";
	}
	
	/**
	 * 收件箱
	 * @param pageNo
	 * @param model
	 * @return
	 */
	@RequestMapping("/home/sns/message/receUserMsgList.xhtml")
	public String receiveMessageList(Integer pageNo, ModelMap model){
		Member member = getLogonMember();
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
		PageUtil pageUtil = new PageUtil(count, rowsPerPage, pageNo, "home/sns/message/receUserMsgList.xhtml", true, true);
		pageUtil.initPageInfo();
		List<Member> friendList = friendService.getFriendMemberList(member.getId(), 0, 60);
		model.put("userMessageMap", userMessageMap);
		model.put("friendList", friendList);
		addCacheMember(model, BeanUtil.getBeanPropertyList(friendList, Long.class, "id", true));
		model.put("umaList", umaList);
		model.put("messageNumMap", messageNumMap);
		model.put("pageUtil", pageUtil);
		//model.putAll(getNotReadMessage(model, member.getId()));
		
		//cacheService.remove(CacheConstant.REGION_TWOHOUR, "USER_MSGCOUNT_" + member.getId());
		/*String key = "USER_MSGCOUNT_" + member.getId();
		cacheService.remove(CacheConstant.REGION_TWOHOUR, key);
		memberService.getMemberNotReadMessageCount(member.getId());*/
		this.getHomeLeftNavigate(daoService.getObject(MemberInfo.class, member.getId()), model);
		
		return "sns/message/receMessageList.vm";
	}
		
	/**
	 * 发件箱
	 * @param pageNo
	 * @param model
	 * @return
	 */
	@RequestMapping("/home/sns/message/sendUserMsgList.xhtml")
	public String sendMessageList(Integer pageNo, ModelMap model){
		Member member = getLogonMember();
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
		PageUtil pageUtil = new PageUtil(count, rowsPerPage, pageNo, "home/sns/message/sendUserMsgList.xhtml", true, true);
		pageUtil.initPageInfo();
		List<Member> friendList = friendService.getFriendMemberList(member.getId(), 0, 60);
		model.put("friendList", friendList);
		addCacheMember(model, BeanUtil.getBeanPropertyList(friendList, Long.class, "id", true));
		model.put("umaList", umaList);
		model.put("userMessageMap", userMessageMap);
		model.put("pageUtil", pageUtil);
		this.getHomeLeftNavigate(daoService.getObject(MemberInfo.class, member.getId()), model);
		return "sns/message/sendMessageList.vm";
	}
	
	/**
	 * 消息详细内容
	 * @param mid
	 * @param model
	 * @return
	 */
	@RequestMapping("/home/sns/message/userMessDetail.xhtml")
	public String userMessageDetail(Long mid, ModelMap model){
		Member member = getLogonMember();
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
		this.getHomeLeftNavigate(daoService.getObject(MemberInfo.class, member.getId()), model);
		return "sns/message/messageDetail.vm";
	}
	
	@RequestMapping("/home/sns/message/userMessDrop.xhtml")
	public String userMessageDrop(Long mid, ModelMap model,HttpServletRequest request){
		Member member = getLogonMember();
		UserMessageAction uma = daoService.getObject(UserMessageAction.class, mid);
		if(ObjectUtils.notEqual(uma.getTomemberid() , member.getId()))
			return showJsonError(model, "非法操作！");
		uma.setStatus("tdel");
		daoService.saveObject(uma);
		monitorService.saveMemberLog(member.getId(), MemberConstant.ACTION_DROPMESS, null, WebUtils.getRemoteIp(request));
		return showJsonSuccess(model);
	}
	
	/**
	 * 系统消息
	 * @param pageNo
	 * @param model
	 * @return
	 */
	@RequestMapping("/home/message/new/sysMsgList.xhtml")
	public String receiveSystemMessageList(Integer pageNo, ModelMap model){
		Member member = this.getLogonMember();
		if(pageNo==null) pageNo=0;
		Integer rowsPerPage = 10;
		Integer count = userMessageService.getSysMsgCountByMemberid(member.getId(), null);
		List<SysMessageAction> sysMsgList = userMessageService.getSysMsgListByMemberid(member.getId(),null, pageNo*rowsPerPage, rowsPerPage);
		List<Long> memberidList = BeanUtil.getBeanPropertyList(sysMsgList, Long.class, "frommemberid", true);
		addCacheMember(model, memberidList);
		userMessageService.initSysMsgList(sysMsgList);
		PageUtil pageUtil = new PageUtil(count, rowsPerPage, pageNo, "/home/message/new/sysMsgList.xhtml", true, true);
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
		this.getHomeLeftNavigate(memberInfo, model);
		return "sns/message/sysMsgList.vm";
	}

	
	
	
	
	
	private void initJsonMap(List<JsonData> list, ModelMap model){
		Map<String, Map<String, String>> dataMap = new HashMap<String, Map<String,String>>();
		for(JsonData jsonData : list){
			Map<String, String> map = VmUtils.readJsonToMap(jsonData.getData());
			dataMap.put(jsonData.getDkey(), map);
		}
		model.put("dataMap", dataMap);
	}
	
	private Map getNotReadMessage(ModelMap model, Long memberid){
		model.put("messageCount", memberService.getMemberNotReadNormalMessageCount(memberid));
		model.put("sysMessageCount", memberService.getMemberNotReadSysMessageCount(memberid));
		return model;
	}

	

}
