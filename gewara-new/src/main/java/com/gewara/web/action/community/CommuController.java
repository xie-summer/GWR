package com.gewara.web.action.community;

import java.io.IOException;
import java.io.Serializable;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.RequestMapping;

import com.gewara.Config;
import com.gewara.constant.DiaryConstant;
import com.gewara.constant.Status;
import com.gewara.constant.SysAction;
import com.gewara.constant.content.SignName;
import com.gewara.constant.sys.JsonDataKey;
import com.gewara.model.bbs.Diary;
import com.gewara.model.bbs.commu.Commu;
import com.gewara.model.bbs.commu.CommuManage;
import com.gewara.model.bbs.commu.CommuMember;
import com.gewara.model.bbs.commu.CommuTopic;
import com.gewara.model.bbs.commu.VisitCommuRecord;
import com.gewara.model.common.County;
import com.gewara.model.common.Indexarea;
import com.gewara.model.content.GewaCommend;
import com.gewara.model.content.Notice;
import com.gewara.model.content.Picture;
import com.gewara.model.user.Album;
import com.gewara.model.user.AlbumComment;
import com.gewara.model.user.Member;
import com.gewara.model.user.SysMessageAction;
import com.gewara.service.PlaceService;
import com.gewara.service.bbs.AlbumService;
import com.gewara.service.bbs.DiaryService;
import com.gewara.service.bbs.UserMessageService;
import com.gewara.service.content.NoticeService;
import com.gewara.service.member.FriendService;
import com.gewara.support.ReadOnlyTemplate;
import com.gewara.support.ServiceHelper;
import com.gewara.untrans.GewaPicService;
import com.gewara.util.BeanUtil;
import com.gewara.util.BindUtils;
import com.gewara.util.DateUtil;
import com.gewara.util.RelatedHelper;
import com.gewara.util.ValidateUtil;
import com.gewara.util.WebUtils;
import com.gewara.web.util.PageUtil;

/**
 * @author hxs(ncng_2006@hotmail.com)
 * @since Feb 2, 2010 10:08:18 AM
 */
@Controller
public class CommuController extends BaseCommuController {
	@Autowired@Qualifier("readOnlyTemplate")
	private ReadOnlyTemplate readOnlyTemplate;
	public void setReadOnlyHibernateTemplate(ReadOnlyTemplate readOnlyTemplate) {
		this.readOnlyTemplate = readOnlyTemplate;
	}
	@Autowired@Qualifier("config")
	private Config config;
	public void setConfig(Config config) {
		this.config = config;
	}

	@Autowired@Qualifier("friendService")
	private FriendService friendService;
	public void setFriendService(FriendService friendService) {
		this.friendService = friendService;
	}
	@Autowired@Qualifier("placeService")
   private PlaceService placeService;
	public void setPlaceService(PlaceService placeService) {
		this.placeService = placeService;
	}
	@Autowired@Qualifier("userMessageService")
	private UserMessageService userMessageService;
	public void setUserMessageService(UserMessageService userMessageService) {
		this.userMessageService = userMessageService;
	}
	@Autowired@Qualifier("albumService")
	private AlbumService albumService;
	public void setAlbumService(AlbumService albumService) {
		this.albumService = albumService;
	}
	@Autowired@Qualifier("noticeService")
	private NoticeService noticeService;
	public void setNoticeService(NoticeService noticeService) {
		this.noticeService = noticeService;
	}
	@Autowired @Qualifier("diaryService")
	private DiaryService diaryService;
	public void setDiaryService(DiaryService diaryService) {
		this.diaryService = diaryService;
	}
	
	@Autowired @Qualifier("gewaPicService")
	private GewaPicService gewaPicService;
	public void setGewaPicService(GewaPicService gewaPicService) {
		this.gewaPicService = gewaPicService;
	}
	
	@RequestMapping("/quan/commuDetail.xhtml")
	public String commuDetail(@CookieValue(value=LOGIN_COOKIE_NAME, required=false)String sessid,
			HttpServletRequest request, ModelMap model,Long commuid,Long commutopicid, Long from, 
			HttpServletResponse response){
		Member member = loginService.getLogonMemberBySessid(WebUtils.getRemoteIp(request), sessid);
		Commu commu = daoService.getObject(Commu.class, commuid);
		if(commu==null || !commu.hasStatus(Status.Y)){
			return showError(model, "您请求的圈子已经被删除！");
		}
		//msn邀请好友start
		String invitetype="msnInviteFriend";
		if(from != null)
			WebUtils.setInviteFromCookie(response, config.getBasePath(), from, invitetype);
		//msn邀请好友end
		
		int activityCount = 5;//活动条数 20 5
		int diaryCount = 10;//话题条数30,10
		int albumCount = 8;//相册图片张数16 8
		int voteCount = 5;//投票条数10 5
		//话题
		List<Diary> diaryList = null;
		List<CommuTopic> commuTopicList = commonService.getCommuTopicList(commuid, 0, 100);
		Map<String, String> layoutMap = jsonDataService.getJsonData(JsonDataKey.KEY_COMMULAYOUT + commuid);
		String[] commuTopicType = new String[]{DiaryConstant.DIARY_TYPE_TOPIC_DIARY,DiaryConstant.DIARY_TYPE_COMMENT};
		if("1".equals(layoutMap.get("diarytemplate"))){
			activityCount =Integer.valueOf(layoutMap.get("activity"));
			diaryCount =Integer.valueOf(layoutMap.get("diary"));
			albumCount =Integer.valueOf(layoutMap.get("album"));
			voteCount = Integer.valueOf(layoutMap.get("vote"));
			diaryList = commuService.getCommuDiaryListById(Diary.class, commuid,commuTopicType,commutopicid,0,diaryCount);
			model.put("diaryList", diaryList);
			model.put("commuTopicList", commuTopicList);
			model.put("diaryCount", diaryCount);
		}else if("2".equals(layoutMap.get("diarytemplate"))) {
			activityCount = Integer.valueOf(layoutMap.get("activitys"));
			albumCount = Integer.valueOf(layoutMap.get("albums"));
			voteCount = Integer.valueOf(layoutMap.get("votes"));
			Map<Long,List<Diary>> commuTopicMap = new HashMap<Long,List<Diary>>();
			if(!commuTopicList.isEmpty()){
				for (CommuTopic ct : commuTopicList) {
					Long id = ct.getId();
					diaryList = commuService.getCommuDiaryListById(Diary.class, commuid,commuTopicType,id,0,ct.getDisplaynum());
					commuTopicMap.put(id, diaryList);
				}
				model.put("commuTopicList", commuTopicList);
				model.put("commuTopicMap", commuTopicMap);
			}else{
				diaryList = commuService.getCommuDiaryListById(Diary.class, commuid,commuTopicType,commutopicid,0, diaryCount);
				model.put("diaryList", diaryList);
				model.put("diaryCount", diaryCount);
			}
		}else{
			diaryList = commuService.getCommuDiaryListById(Diary.class, commuid,commuTopicType,commutopicid,0, diaryCount);
			model.put("diaryList", diaryList);
			model.put("commuTopicList", commuTopicList);
		}
		model.put("activityCount", activityCount);
		model.put("albumCount", albumCount);
		model.put("voteCount", voteCount);
		model.putAll(getCommuCommonData(model, commu, member));
		//投票
		String[] type = new String[]{DiaryConstant.DIARY_TYPE_TOPIC_VOTE};
		List<Diary> voteList = commuService.getCommuDiaryListById(Diary.class, commuid, type ,null,0, voteCount);
		//相册
		List<Picture> albumImageList = albumService.getPicturesByCommuidList(commuid, 0, albumCount);
		List<Notice> noticeList = noticeService.getNoticeListByCommuid(commuid, Notice.TAG_COMMU, 0, 1);
		if(!noticeList.isEmpty()) model.put("notice", noticeList.get(0));
		model.put("albumImageList", albumImageList);
		model.put("voteList", voteList);
		model.put("layout",layoutMap.get("diarytemplate"));
		
		// 浏览次数
		String[] invitefrom = WebUtils.getCookie4ProtectedPage(request, "page4pro");
		if(invitefrom == null){
			WebUtils.setCookie4ProtectedPage(request, response, "/");
			commu.setClickedtimes(commu.getClickedtimes()+1);
			daoService.saveObject(commu);
		}
		
		// 检查当前圈子的状态
		String checkstatus = commuService.getCheckStatusByIDAndMemID(commuid);
		model.put("checkstatus", checkstatus);
		if(member!=null) {
			model.put("isAdmin", member.getId().equals(commu.getAdminid()));
			//圈子成员访问的记录
			VisitCommuRecord visitCommuRecord = commuService.getVisitCommuRecordByCommuidAndMemberid(commuid, member.getId());
			if(visitCommuRecord==null){ //第一次访问
				visitCommuRecord = new VisitCommuRecord(member.getId(), commuid);
			}
			if(visitCommuRecord.getLasttime().before(DateUtil.getCurTruncTimestamp())){//今日还没有访问圈子
				visitCommuRecord.setVisitnum(visitCommuRecord.getVisitnum()+1);
			}
			visitCommuRecord.setLasttime(new Timestamp(System.currentTimeMillis()));
			try {
				daoService.saveObject(visitCommuRecord);
			} catch (Exception e) {
				dbLogger.error("", e);
				return "home/community/index.vm";
			}
		}
		return "home/community/index.vm";
	}
	
	/**
	 * 圈子申请认证 
	 */
	@RequestMapping("/home/commu/applyCertification.xhtml")
	public String applyCertification(ModelMap model, Long commuid){
		Commu commu = daoService.getObject(Commu.class, commuid);
		if(commu==null || !commu.hasStatus(Status.Y)){
			showError(model, "您请求的圈子已经被删除！");
		}
		model.put("commu", commu);
		//检查当前圈子是否申请过
		String checkstatus = commuService.getCheckStatusByIDAndMemID(commuid);
		if(CommuManage.STATUS_WAIT.equals(checkstatus)){
			return showError(model, "您的申请正在审核中...");
		}else if(CommuManage.STATUS_PASS.equals(checkstatus)){
			return showError(model, "您的申请已经通过");
		}
		model.put("logonMember", getLogonMember());
		return "home/community/customer.vm";
	}
	/**
	 * 圈子申请认证信息保存 
	 */
	@RequestMapping("/home/commu/saveapplyCertification.xhtml")
	public String saveApplyCertification(ModelMap model, Long commuid, HttpServletRequest request){
		Map<String, String[]> daMap = request.getParameterMap();
		if(commuid==null){return showJsonError(model, "请先登录圈子!");}
		String applymemberid = ServiceHelper.get(daMap, "applymemberid");
		Member member = getLogonMember();
		if(StringUtils.isBlank(applymemberid)||member==null){return showJsonError(model, "请先登录!");}
		if(!StringUtils.equals(member.getId()+"", applymemberid)){return showJsonError(model, "不能修改别人的圈子!");}
		CommuManage commuManage = new CommuManage(commuid);
		if(!ValidateUtil.isNumber(request.getParameter("contactphone"))) return showJsonError(model, "联系电话格式不正确!");
		if(!ValidateUtil.isIDCard(request.getParameter("idnumber"))) return showJsonError(model, "身份证号格式不正确!");
		if(!ValidateUtil.isEmail(request.getParameter("email"))) return showJsonError(model, "email格式不正确!");
		BindUtils.bindData(commuManage, daMap);
		commuManage.setCheckstatus(CommuManage.STATUS_WAIT);
		daoService.saveObject(commuManage);
		return showJsonSuccess(model);
	}
	
	@RequestMapping("/home/commu/saveApplyAddCommu.xhtml")
	public String saveAddCommu(ModelMap model, Long commuid, String body){
		Commu commu = daoService.getObject(Commu.class, commuid);
		if(commu==null || !commu.hasStatus(Status.Y)){
			return showJsonError(model, "您请求的圈子已经被删除！");
		}
		Member member=this.getLogonMember();
		model.putAll(getCommuCommonData(model, commu, member));
		boolean isExistSysMessageAction = userMessageService.isExistSysMessageAction(commuid, member.getId(), SysAction.ACTION_APPLY_COMMU_JOIN, true);
		if(!isExistSysMessageAction) {//不存在，没申请加入该圈子
			//该用户是否被邀请加入该圈子了,存在先删除该条信息
			SysMessageAction sysMessageAction = userMessageService.getSysMessageAction(commuid, member.getId(), SysAction.ACTION_APPLY_COMMU_INVITE, false);
			if(sysMessageAction != null) {
				//存在，删除
				SysMessageAction sysMA = daoService.getObject(SysMessageAction.class, sysMessageAction.getId());
				daoService.removeObject(sysMA);
			}
			SysMessageAction systemAction = new SysMessageAction(member.getId(), commu.getAdminid(), body, commuid, SysAction.ACTION_APPLY_COMMU_JOIN);
			daoService.saveObject(systemAction);
			model.put("commuid", commuid);
			Map params = new HashMap();
			params.put("commuid", commuid);
			params.put("msg", "申请成功，请等待圈子管理员的审核！");
			return showJsonSuccess(model,params);
		}else{
			return showJsonError(model, "您已经申请加入圈子，请等待圈子管理员审核！");
		}
	}
	
	@RequestMapping("/home/commu/inviteAddCommu.xhtml")
	public String inviteAddCommu(ModelMap model, Long commuid, Integer pageNo){
		Commu commu = daoService.getObject(Commu.class, commuid);
		if(commu==null || !commu.hasStatus(Status.Y)){
			return showError(model, "您请求的圈子已经被删除！");
		}
		Member member = getLogonMember();
		//好友列表
		Map<Long, Member> memberMap = new HashMap<Long, Member>();
		//查询圈子所有成员
		if(pageNo == null) pageNo=0;
		Integer rowsPerPage=40;
		Integer firstRowsPage = pageNo*rowsPerPage;
		Integer friendCount = friendService.getNotJoinCommuFriendCount(member.getId(), commuid);
		//根据member.getId(), commuIdList 查询朋友中还没有参加该圈子的friendidList
		List<Long> friendidList = friendService.getNotJoinCommuFriendIdList(member.getId(), commuid, firstRowsPage, rowsPerPage);
		for(Long friendId : friendidList){
			Member memberInfo = daoService.getObject(Member.class, friendId);
			memberMap.put(friendId, memberInfo);
		}
		Map params = new HashMap(); 
		params.put("commuid", new String[]{commuid+""});
		PageUtil pageUtil = new PageUtil(friendCount, rowsPerPage, pageNo, "home/commu/inviteAddCommu.xhtml", true, true);
		pageUtil.initPageInfo(params);
		model.putAll(getCommuCommonData(model, commu, member));
		model.put("friendidList", friendidList);
		addCacheMember(model,friendidList);
		model.put("membersMap", memberMap);
		model.put("commuid", commuid);
		model.put("pageUtil", pageUtil);
		return "home/community/inviteAddCommuInfo.vm";
	}
	
	@RequestMapping("/home/commu/saveInviteAddCommu.xhtml")
	public String saveInviteAddCommu(HttpServletRequest request, ModelMap model, Long commuid){
		Commu commu = daoService.getObject(Commu.class, commuid);
		if(commu==null || !commu.hasStatus(Status.Y)){
			return showJsonError(model, "您请求的圈子已经被删除！");
		}
		Map<String, String[]> friendMap = request.getParameterMap();
		for(String mmap : friendMap.keySet()){
			if(mmap.startsWith("friendid")){
				//是否已经发送邀请了
				boolean isExistSysMessageAction = userMessageService.isExistSysMessageAction(commuid, new Long(friendMap.get(mmap)[0]), SysAction.ACTION_APPLY_COMMU_INVITE, false);
				if(!isExistSysMessageAction){
					//该用户是否申请加入该圈子了,存在先删除该条信息
					SysMessageAction sysMessageAction = userMessageService.getSysMessageAction(commuid, new Long(friendMap.get(mmap)[0]), SysAction.ACTION_APPLY_COMMU_JOIN, true);
					//存在，删除
					if(sysMessageAction != null) {
						SysMessageAction sysMA = daoService.getObject(SysMessageAction.class, sysMessageAction.getId());
						daoService.removeObject(sysMA);
					}
					SysMessageAction systemAction = new SysMessageAction(commu.getAdminid(), new Long(friendMap.get(mmap)[0]), commu.getName(), commuid, SysAction.ACTION_APPLY_COMMU_INVITE);
					daoService.saveObject(systemAction);
				}
			}
		}
		return showJsonSuccess(model);
	}

	/**
	 * 获取圈子话题信息
	 * 
	 * @return
	 */
	@RequestMapping("/quan/getCommuDiaryList.xhtml")
	public String getCommuDiary(@CookieValue(value=LOGIN_COOKIE_NAME, required=false)String sessid, HttpServletRequest request, ModelMap model, Long commuid, Integer pageNo,Long commutopicid) {
		Commu commu = daoService.getObject(Commu.class, commuid);
		if(commu==null || !commu.hasStatus(Status.Y)){
			return show404(model, "您请求的圈子已经被删除！");
		}
		Member member = loginService.getLogonMemberBySessid(WebUtils.getRemoteIp(request), sessid);
		if(member==null) return showError(model, "请先登录！");
		if(pageNo == null)pageNo = 0;
		int rowsPerPage = 20;
		int firstPerPage = pageNo * rowsPerPage;
		List<CommuTopic> commuTopicList = commonService.getCommuTopicList(commuid, 0, 100);
		// 圈子话题信息
		List<Diary> listCommuDiary = commuService.getCommuDiaryListById(Diary.class, commuid,null,commutopicid,
				firstPerPage, rowsPerPage);
		Integer countCommuDiary = commuService.getCommuDiaryCount(Diary.class, commuid,null,commutopicid);
		PageUtil pageUtil = new PageUtil(countCommuDiary, rowsPerPage, pageNo,
				"quan/getCommuDiaryList.xhtml", true, true);

		//当前用户好友帖子信息
		List<Diary> listFriendDiary=diaryService.getFriendDiaryList(DiaryConstant.DIARY_TYPE_TOPIC_DIARY, null, null, member.getId(), 0, 5);
		model.put("listFriendDiary", listFriendDiary);
		//当前用户好友的活动
			
		//热门圈子
		List<Commu> hotCommuList=commuService.getHotCommuList(0,6);
		model.put("hotCommuList", hotCommuList);
		Map params=new HashMap();
		params.put("commuid",commuid);
		params.put("commutopicid", commutopicid);
		pageUtil.initPageInfo(params);
		model.put("listCommuDiary", listCommuDiary);
		model.putAll(getCommuCommonData(model, commu, member));
		model.put("pageUtil", pageUtil);
		model.put("commuTopicList", commuTopicList);
		return "home/community/commentList.vm";
	}

	
	/**
	 * 获取圈子成员信息
	 */
	@RequestMapping("/quan/getCommuMemberList.xhtml")
	public String getCommumemberInfo(@CookieValue(value=LOGIN_COOKIE_NAME, required=false)String sessid,
			HttpServletRequest request, ModelMap model, Long commuid, Integer pageNo) {
		Commu commu = daoService.getObject(Commu.class, commuid);
		if(commu==null || !commu.hasStatus(Status.Y)){
			return show404(model, "您请求的圈子已经被删除！");
		}
		if (pageNo == null) pageNo = 0;
		Integer rowsPerPage = 21;
		Integer firstPerPage = pageNo * rowsPerPage;
		List<Commu> listCommuMemberLoveCommu=commuService.getCommuMemberLoveToCommuList(commuid,0,6);
		List<CommuMember> listCommuMember = commuService.getCommuMemberById(commuid, null, null, "", 
				firstPerPage, rowsPerPage);
		addCacheMember(model, ServiceHelper.getMemberIdListFromBeanList(listCommuMember));
		Integer countcommumember = commuService.getCommumemberCount(commuid, null);
		PageUtil pageUtil = new PageUtil(countcommumember, rowsPerPage, pageNo, "quan/getCommuMemberList.xhtml", true, true);
		Map params=new HashMap();
		params.put("commuid", commuid);
		pageUtil.initPageInfo(params);
		//邀请好友
		model.put("listCommuMemberLoveCommu", listCommuMemberLoveCommu);
		model.put("listCommuMember", listCommuMember);
		model.put("pageUtil", pageUtil);
		String ip = WebUtils.getRemoteIp(request);
		Member member = loginService.getLogonMemberBySessid(ip, sessid);
		model.putAll(getCommuCommonData(model, commu, member));
		return "home/community/commuMemberList.vm";
	}
	/**
	 * 获取圈子成员信息,进行管理
	 */
	@RequestMapping("/home/commu/manageCommuMemberList.xhtml")
	public String manageCommumemberInfo(ModelMap model, Long commuid, Integer pageNo) {
		Commu commu = daoService.getObject(Commu.class, commuid);
		if(commu==null || !commu.hasStatus(Status.Y)){
			return show404(model, "您请求的圈子已经被删除！");
		}
		Member member = getLogonMember();
		if(!member.getId().equals(commu.getAdminid())&&!member.getId().equals(commu.getSubadminid())) return showError(model, "只有管理员才有权限操作!");
		if (pageNo == null) pageNo = 0;
		Integer rowsPerPage = 20;
		Integer firstPerPage = pageNo * rowsPerPage;
		//管理员
		Map<Long, Member> adminMemberMap=new HashMap<Long, Member>();
		Map<Long, VisitCommuRecord> adminVCRMap=new HashMap<Long, VisitCommuRecord>();
		Map<Long ,CommuMember> adminCommuMemberMap=new HashMap<Long, CommuMember>();
		List<Long> adminIdList=new ArrayList<Long>();
		adminIdList.add(commu.getAdminid());
		if(commu.getSubadminid()!=0) adminIdList.add(commu.getSubadminid());
		for(Long adminid: adminIdList){
			Member adminMember = daoService.getObject(Member.class, adminid);
			VisitCommuRecord adminvcr = commuService.getVisitCommuRecordByCommuidAndMemberid(commuid, adminid);
			adminMemberMap.put(adminid, adminMember);
			adminVCRMap.put(adminid, adminvcr);
			adminCommuMemberMap.put(adminid, daoService.getObject(CommuMember.class, adminid));
		}
		model.put("adminIdList", adminIdList);
		model.put("adminMemberMap", adminMemberMap);
		model.put("adminVCRMap", adminVCRMap);
		model.put("adminCommuMemberMap", adminCommuMemberMap);
		Map<Long, Member> commuMemberMap = new HashMap<Long, Member>();//存储圈子成员信息
		Map<Long, VisitCommuRecord> visitCommuRecordMap = new HashMap<Long, VisitCommuRecord>();
		Integer countCommumember = commuService.getCommumemberCount(commuid, commu.getAdminid());
		List<CommuMember> commonCommuMemberList = commuService.getCommuMemberById(commuid, commu.getAdminid(), commu.getSubadminid(), "",
				firstPerPage, rowsPerPage);
		for (CommuMember cb : commonCommuMemberList){
			commuMemberMap.put(cb.getId(), daoService.getObject(Member.class, cb.getMemberid()));
			VisitCommuRecord visitMemberRecord=commuService.getVisitCommuRecordByCommuidAndMemberid(commuid, cb.getMemberid());
			visitCommuRecordMap.put(cb.getId(), visitMemberRecord);
		}
		//未被批准加入圈子的用户
		List<SysMessageAction> sysMessageActionList = userMessageService.getSysMessageActionListByActionidAndActionAndStatus(commuid,
				SysAction.ACTION_APPLY_COMMU_JOIN, SysAction.STATUS_APPLY, 0, 500);
		List<Long> upApporeMemberidList = BeanUtil.getBeanPropertyList(sysMessageActionList, Long.class, "frommemberid", true);
		addCacheMember(model, upApporeMemberidList);
		//圈子黑名单
		Map<Long, Member> blackMemberMap=new HashMap<Long, Member>();
		Map<Long, VisitCommuRecord> bVisitCommuRecordMap = new HashMap<Long, VisitCommuRecord>();
		List<CommuMember> commuBlackMemberList = commuService.getCommuMemberById(commuid, null, null, Status.Y, 0, 1000);
		for(CommuMember blackMembers :commuBlackMemberList){
			Member blackMember=daoService.getObject(Member.class, blackMembers.getMemberid());
			blackMemberMap.put(blackMembers.getId(), blackMember);
			VisitCommuRecord bVisitMemberRecord=commuService.getVisitCommuRecordByCommuidAndMemberid(commuid, blackMembers.getMemberid());
			bVisitCommuRecordMap.put(blackMembers.getId(), bVisitMemberRecord);
		}
		PageUtil pageUtil = new PageUtil(countCommumember, rowsPerPage, pageNo,"home/commu/manageCommuMemberList.xhtml", true, true);
		model.put("commuBlackMemberList", commuBlackMemberList);
		model.put("blackMemberMap", blackMemberMap);
		model.put("bVisitCommuRecordMap", bVisitCommuRecordMap);
		Map params=new HashMap();
		params.put("commuid", commuid);
		pageUtil.initPageInfo(params);
		model.put("visitCommuRecordMap", visitCommuRecordMap);
		model.put("commonCommuMemberList", commonCommuMemberList);
		model.put("pageUtil", pageUtil);
		model.put("commuMemberMap", commuMemberMap);
		model.put("sysMessageActionList", sysMessageActionList);
		if(pageNo>0) model.put("commumember", "member");
		model.putAll(getCommuCommonData(model, commu, member));
		return "home/community/manage/memberManage.vm";
	}
	
	@RequestMapping("/quan/albumList.xhtml")
	public String albumList(@CookieValue(value=LOGIN_COOKIE_NAME, required=false)String sessid, HttpServletRequest request, ModelMap model, Long commuid, Integer pageNo) {
		Commu commu = daoService.getObject(Commu.class, commuid);
		if(commu==null || !commu.hasStatus(Status.Y)){
			return show404(model, "您请求的圈子已经被删除！");
		}
		Member member = loginService.getLogonMemberBySessid(WebUtils.getRemoteIp(request), sessid);
		if(pageNo==null) pageNo=0;
		int rowsPerPage=12;
		int start = pageNo * rowsPerPage;
		int count=0;
		List<Album> albumList=commuService.getCommuAlbumById(commuid, start, rowsPerPage);
		Map<Long,Integer> imageNum = new HashMap<Long, Integer>();
		for(Album album:albumList){
			Integer num = albumService.getPictureountByAlbumId(album.getId());
			imageNum.put(album.getId(), num);
		}
		count=commuService.getCommuAlbumCountById(commuid);
		PageUtil pageUtil=new PageUtil(count,rowsPerPage,pageNo,"quan/albumList.xhtml", true, true);
		Map params = new HashMap(); 
		params.put("commuid", commuid);
		pageUtil.initPageInfo(params);
		model.put("imageNum",imageNum);
		model.put("pageUtil",pageUtil);
		model.put("albumList", albumList);
		model.put("member",member);
		model.putAll(getCommuCommonData(model, commu, member));
		return "home/community/albumList.vm";
	}
	/**
	 * 圈子相册权限控制
	 * @param model
	 * @param albumid
	 * @param pageNo
	 * @return
	 */
	private String commuShowController(Member member,Album album,Commu commu,ModelMap model){
		if(member!=null){
			if(member.getId().equals(album.getMemberid()) || commu.getAdminid().equals(member.getId()) || commu.getSubadminid().equals(member.getId())){
				model.put("isShowCommuAlbum", true);
			}else{
				model.put("isShowCommuAlbum", false);
			}
		}else{
			model.put("isShowCommuAlbum", false);
		}
		return null;
	}
	
	@RequestMapping("/quan/commu/albumImageList.xhtml")
	public String albumImageList(@CookieValue(value=LOGIN_COOKIE_NAME, required=false)String sessid,
			HttpServletRequest request, ModelMap model, Long albumid, Integer pageNo) {
		String ip = WebUtils.getRemoteIp(request);
		Member member = loginService.getLogonMemberBySessid(ip, sessid);
		Album album = daoService.getObject(Album.class, albumid);
		if(album == null) return show404(model, "错误的相册信息！");
		Commu commu = daoService.getObject(Commu.class, album.getCommuid());
		if(commu == null) return show404(model, "你访问的数据有误！");
		if(!commu.hasStatus(Status.Y)) return show404(model, "该相册的圈子已被删了！");
		model.putAll(getCommuCommonData(model, commu, member));
		if(pageNo==null) pageNo=0;
		int rowsPerPage=20;
		int start = pageNo * rowsPerPage;
		int count=0;
		commuShowController(member, album, commu, model);
		List<Picture> albumImageList = albumService.getPictureByAlbumId(albumid, start, rowsPerPage);
		count = albumService.getPictureountByAlbumId(albumid);
		PageUtil pageUtil = new PageUtil(count,rowsPerPage,pageNo,"quan/commu/albumImageList.xhtml", true, true);
		Member albumMember = daoService.getObject(Member.class, album.getMemberid());
		Map params = new HashMap(); 
		params.put("albumid", albumid);
		params.put("commuid", album.getCommuid());
		pageUtil.initPageInfo(params);
		if(member!=null){
			if (album.getMemberid().equals(member.getId())) model.put("ismycommu",true);
			model.put("mymember",member);
		}
		model.put("pageUtil",pageUtil);
		model.put("albumid", albumid);
		model.put("albumImageList",albumImageList);
		model.put("albumMember",albumMember);
		model.put("album",album);
		model.put("commuid", album.getCommuid());
		return "home/community/albumImageList.vm";
	}
	@RequestMapping("/home/commu/deleteCommuMember.xhtml")
	public String deleteCommuMember(ModelMap model,Long commuid,Long memberid){
		Member member=getLogonMember();
		Commu commu = daoService.getObject(Commu.class, commuid);
		if(commu == null || !commu.hasStatus(Status.Y)) return show404(model, "该圈子已经被删除！");
		Map map = getCommuCommonData(model, commu, member);
		Member admin = (Member)map.get("adminMember");
		if((member.getId()+"").equals(admin.getId()+"")){//判断当前用户是否是这个圈子的管理员
			friendService.deleteCommueMember(memberid, commuid);
			//发送系统消息
			String title = "管理员"+member.getNickname()+"把你从圈子"+commu.getName()+"中踢出了";
			SysMessageAction sysmessage=new SysMessageAction(SysAction.STATUS_RESULT);
			sysmessage.setFrommemberid(member.getId());
			sysmessage.setBody(title);
			sysmessage.setTomemberid(memberid);
			daoService.saveObject(sysmessage);
		}
		model.put("commuid", commuid);
		return showRedirect("/quan/getCommuMemberList.xhtml", model);
	}
	// 背景图 删除
	@RequestMapping("/home/commu/delPic.xhtml")
	public String commuDelPic(Long commuid, ModelMap model){
		Commu commu = daoService.getObject(Commu.class, commuid);
		if(commu == null || !commu.hasStatus(Status.Y)){
			return showJsonError(model, "你请求的圈子已经被删除！");
		}
		boolean issuc = false;
		try {
			issuc = gewaPicService.removePicture(commu.getCommubg());
			commu.setCommubg(null);
			daoService.saveObject(commu);
		} catch (IOException e) {
			dbLogger.error("", e);
			return showJsonError_DATAERROR(model);
		}
		if(issuc){
			return showJsonSuccess(model);
		}else{
			return showJsonError_DATAERROR(model);
		}
	}
	
	@RequestMapping("/home/commu/manageLoading.xhtml")
	public String manageLoading(Long commuid, String mtag, ModelMap model){
		String str = null;
		if("logo".equals(mtag)){
			str = "home/community/manage/logo.vm";
		}else if("layout".equals(mtag)) {
			Map<String, String> layoutMap = jsonDataService.getJsonData(JsonDataKey.KEY_COMMULAYOUT + commuid);
			model.put("layoutMap", layoutMap);
			str = "home/community/manage/layout.vm";
		}else if("color".equals(mtag)){
			Map<String, String> colorMap = jsonDataService.getJsonData(JsonDataKey.KEY_COMMUCOLOR + commuid);
			model.put("colorMap", colorMap);
			str = "home/community/manage/color.vm";
		}else if("bgpic".equals(mtag)) {
			// 当前圈子的背景 唯一
			Commu commu = daoService.getObject(Commu.class, commuid);
			String commubgpic = commu.getCommubg();
			if(StringUtils.isNotBlank(commubgpic)){
				model.put("commubgpic", commubgpic);
			}
			str = "home/community/manage/bgpic.vm";
		}
		Commu commu = daoService.getObject(Commu.class, commuid);
		model.put("commu", commu);
		return str;
	}
	@RequestMapping("/home/commu/saveColor.xhtml")
	public String saveColor(Long commuid, String colors, ModelMap model){
		Map<String, String> dataMap = WebUtils.parseQueryStr(colors, "UTF-8");
		Member member=getLogonMember();
		Commu commu = daoService.getObject(Commu.class, commuid);
		if(!commu.hasStatus(Status.Y)) return showJsonError(model, "该圈子已经被删除！");
		if(!member.getId().equals(commu.getAdminid())) return showJsonError(model, "你没有这个权限");
		jsonDataService.saveJsonData(JsonDataKey.KEY_COMMUCOLOR + commuid, JsonDataKey.KEY_COMMUCOLOR, dataMap);
		return showJsonSuccess(model);
	}
	@RequestMapping("/home/commu/saveLayout.xhtml")
	public String saveLayout(Long commuid, String layouts, ModelMap model){
		Map<String, String> dataMap = WebUtils.parseQueryStr(layouts, "UTF-8");
		String template = dataMap.get("diarytemplate");
		if(template == null) return showJsonError(model, "请选择话题布局方式!");
		try{
			int layTemplate =Integer.valueOf(template+"");
			int activity = 0;
			int vote = 0;
			int album = 0;
			int diary = 0;
			if(layTemplate ==1){
				activity = Integer.valueOf(dataMap.get("activity")+"");
				vote = Integer.valueOf(dataMap.get("vote")+"");
				album = Integer.valueOf(dataMap.get("album")+"");
				diary = Integer.valueOf(dataMap.get("diary")+"");
			}else{
				activity = Integer.valueOf(dataMap.get("activitys")+"");
				vote = Integer.valueOf(dataMap.get("votes")+"");
				album = Integer.valueOf(dataMap.get("albums")+"");
			}
			if(activity>20||activity<0) return showJsonError(model, "活动条数只能在0-20条之间！");
			if(vote>10||vote<0) return showJsonError(model, "投票条数只能在0-10条之间！");
			if(album>16||album<0) return showJsonError(model, "相册图片条数只能在0-16条之间！");
			if(diary>30||diary<0) return showJsonError(model, "相册图片条数只能在0-30条之间！");
		}catch(NumberFormatException e){
			return showJsonError(model, "只能输入正整数！");
		}
		Member member=getLogonMember();
		Commu commu = daoService.getObject(Commu.class, commuid);
		if(!commu.hasStatus(Status.Y)) return showJsonError(model, "该相册的圈子已经被删除！");
		if(!member.getId().equals(commu.getAdminid())) return showJsonError(model, "你没有这个权限");
		jsonDataService.saveJsonData(JsonDataKey.KEY_COMMULAYOUT + commuid, JsonDataKey.KEY_COMMULAYOUT, dataMap);
		return showJsonSuccess(model);
	}
	
	@RequestMapping("/home/commu/applyAddCommuInfo.xhtml")
	public String addCommuInfo(ModelMap model, Long commuid){
		Commu commu = daoService.getObject(Commu.class, commuid);
		if(commu == null) return show404(model, "你访问的圈子已删除或你没有访问权限访问！");
		if(!commu.hasStatus(Status.Y)) return show404(model, "该圈子已经被删除！");
		model.put("commuid", commuid);
		model.putAll(getCommuCommonData(model, commu, getLogonMember()));
		return "home/community/applyAddCommuInfo.vm";
	}
	
	@RequestMapping("/home/commu/isCommuBlack.xhtml")
	public String isCommuBlack(Long commuid,ModelMap model){
		Member member = getLogonMember();
		CommuMember commuMember=commuService.getCommuMemberByMemberidAndCommuid(member.getId(), commuid);
		if(commuMember == null) return showJsonError(model, "你不是圈子成员，不能进行此操作！");
		if(CommuMember.FLAG_BLACK.equals(commuMember.getFlag())){
			return showJsonError(model,"你被关入小黑屋，暂时不能作此操作！");
		}
		return showJsonSuccess(model);
	}
	//圈子列表
	@RequestMapping("/quan/index.xhtml")
	public String commuIndex(ModelMap model, Integer pageNo, String tag, Long relatedid, String keyword, 
			String type, String countycode, HttpServletRequest request, HttpServletResponse response){
		if(pageNo==null) pageNo = 0;
		Integer rowsPerPage = 10;
		Integer firstRows = rowsPerPage*pageNo;
		Integer commuCount = 0;
		String stag= tag;
		String scountycode= countycode;
		Map<Long, Integer> activityCountMap = new HashMap<Long, Integer>();
		Map<Long, County> countyMap = new HashMap<Long, County>();
		Map<Long, Indexarea> indexareaMap = new HashMap<Long, Indexarea>();
		//圈子推荐
		List<Commu> commuList = new ArrayList<Commu>();
		String citycode = WebUtils.getAndSetDefault(request, response);
		if(StringUtils.isBlank(tag) && StringUtils.isBlank(keyword) && StringUtils.isBlank(type) && StringUtils.isBlank(countycode)){//圈子推荐数据
			List<GewaCommend> gewaCommendCommuList = commonService.getGewaCommendList(citycode, SignName.COMMU_INDEX, null, "commu", true, firstRows, rowsPerPage);
			commuCount = commonService.getGewaCommendCount(citycode, SignName.COMMU_INDEX, null, "commu", true);
			for(GewaCommend gcc: gewaCommendCommuList){
				Commu commu = daoService.getObject(Commu.class, gcc.getRelatedid());
				if(commu ==null || !commu.hasStatus(Status.Y)) continue;
				commuList.add(commu);
				countyMap.put(commu.getId(), daoService.getObject(County.class, commu.getCountycode()));
				indexareaMap.put(commu.getId(), daoService.getObject(Indexarea.class, commu.getIndexareacode()));
			}
		}else {//搜索数据
			if("all".equals(countycode)) countycode="";
			if("all".equals(tag))tag = "";
			commuList = commuService.getCommuBySearch(tag, citycode, relatedid, keyword, "", countycode, firstRows, rowsPerPage);
			commuCount = commuService.getCommuCountBySearch(tag, citycode, relatedid, keyword, "", countycode);
			for(Commu commu: commuList){
				countyMap.put(commu.getId(), daoService.getObject(County.class, commu.getCountycode()));
				indexareaMap.put(commu.getId(), daoService.getObject(Indexarea.class, commu.getIndexareacode()));
			}
		}
		PageUtil pageUtil = new PageUtil(commuCount,rowsPerPage,pageNo,"quan/index.xhtml", true, true);
		model.put("activityCountMap", activityCountMap);
		model.put("countyMap", countyMap);
		model.put("indexareaMap", indexareaMap);
		RelatedHelper rh = new RelatedHelper(); 
		model.put("relatedHelper", rh);
		Map<Serializable, String> relatedMap = BeanUtil.getKeyValuePairMap(commuList,"relatedid", "tag");
		Map<Serializable, String> categoryMap = BeanUtil.getKeyValuePairMap(commuList, "smallcategoryid", "smallcategory");
		relateService.addRelatedObject(1,"relatedMap",rh,relatedMap);
		relateService.addRelatedObject(1,"categoryMap",rh,categoryMap);
		//加载圈子大分类信息
		List<Map> tagList = commuService.getCommuType();
		model.put("tagList", tagList);
		List<County> countyList = placeService.getCountyByCityCode(citycode);
		Map<String, Integer> countyCountMap=new HashMap<String, Integer>();
		for(County county: countyList){
			Integer countyCount = getCommuCountByCountycode(county.getCountycode(), tag, relatedid);
			countyCountMap.put(county.getCountycode(), countyCount);
		}
		model.put("countyListNum", commuService.getCommuCountBySearch(tag, citycode, relatedid, null, "", null));
		model.put("countyList", countyList);
		model.put("countyCountMap", countyCountMap);
		//最新圈子
		List<Commu> newCommuList = commuService.getCommuList(0, 9);
		model.put("newCommuList", newCommuList);
		//热门话题
		List<Diary> hotDiaryList = diaryService.getHotCommuDiary(Diary.class, citycode, true,DiaryConstant.DIARY_TYPE_TOPIC_DIARY,0,10);
		model.put("hotDiaryList", hotDiaryList);
		Map params = new HashMap();
		params.put("tag", stag);
		params.put("type", type);
		params.put("keyword", keyword);
		params.put("relatedid", relatedid);
		params.put("countycode", scountycode);
		model.put("pageUtil", pageUtil);
		pageUtil.initPageInfo(params);
		model.put("count", commuCount);
		model.put("commuList", commuList);
		return "home/community/searchCommunity.vm";
	}
	
	public Integer getCommuCountByCountycode(String countycode, String tag, Long relatedid){
		DetachedCriteria query = DetachedCriteria.forClass(Commu.class);
		query.add(Restrictions.eq("countycode", countycode));
		query.add(Restrictions.like("status", Status.Y, MatchMode.START));
		if("qita".equals(tag)) query.add(Restrictions.isNull("tag"));
		if(StringUtils.isNotBlank(tag) && !"qita".equals(tag)) query.add(Restrictions.eq("tag", tag));
		if(relatedid!=null && StringUtils.equals(tag, "cinema")){
			query.add(Restrictions.eq("smallcategory", "movie"));
			query.add(Restrictions.eq("smallcategoryid", relatedid));
		}else if(relatedid!=null && StringUtils.equals(tag, "gym")){
			query.add(Restrictions.eq("smallcategory", "gymcourse"));
			query.add(Restrictions.eq("smallcategoryid", relatedid));
		}else if(relatedid!=null && StringUtils.equals(tag, "sport")){
			query.add(Restrictions.eq("smallcategory", "sportservice"));
			query.add(Restrictions.eq("smallcategoryid", relatedid));
		}
		query.setProjection(Projections.rowCount());
		List<Commu> commuList=readOnlyTemplate.findByCriteria(query);
		if(commuList.isEmpty()) return 0;
		return new Integer(commuList.get(0)+"");
	}
	
	/**
	 * 圈子相册图片详细
	 */
	@RequestMapping("/quan/commu/imageDetailList.xhtml")
	public String imageDetailList(@CookieValue(value=LOGIN_COOKIE_NAME, required=false)String sessid,
			HttpServletRequest request, ModelMap model,Long albumid,Long curAlbumPicId){
		Album album = daoService.getObject(Album.class, albumid);
		if(album == null) return show404(model, "相册不存在！");
		Commu commu = daoService.getObject(Commu.class, album.getCommuid());
		if(commu == null) return showError(model, "数据有误！");
		if(!commu.hasStatus(Status.Y)){
			return show404(model, "该圈子已被删除！");
		}
		String ip = WebUtils.getRemoteIp(request);
		Member member = loginService.getLogonMemberBySessid(ip, sessid);
		Map<Long,String[]> albumImageMap = new LinkedHashMap<Long, String[]>();
		List<Picture> albumImageList = albumService.getPictureByAlbumId(albumid, 0, 500);
		for (Picture ai : albumImageList) {
			String[] albumimages = new String[3];
			albumimages[0] = ai.getLogo();
			albumimages[1] = ai.getDescription()==null?"这家伙很懒，什么都没留下！":ai.getDescription();
			albumimages[2] = ai.getName()==null?"这家伙很懒，什么都没留下！":ai.getName();
			albumImageMap.put(ai.getId(), albumimages);
		}
		model.put("albumImageMap", albumImageMap);
		model.put("curAlbumImage", daoService.getObject(Picture.class, curAlbumPicId));
		model.put("albumid", albumid);
		model.put("curAlbum", album);
		model.putAll(getCommuCommonData(model, commu, member));
		model.put("member", member);
		List<AlbumComment> imageCommentList = albumService.getPictureComment(curAlbumPicId, 0,30);
		if(imageCommentList.size()>0){
			Map<Long, Member> memberMap = new HashMap<Long, Member>();
			List<Long> commentMemberIdList = BeanUtil.getBeanPropertyList(imageCommentList, Long.class, "memberid",true);
			List<Member> memberList = daoService.getObjectList(Member.class, commentMemberIdList);
			memberMap = BeanUtil.beanListToMap(memberList, "id");
			model.put("memberMap",memberMap);
		}
		if(member != null){
			commuShowController(member, album, commu, model);
		}
		model.put("curAlbumMember", daoService.getObject(Member.class, album.getMemberid()));
		model.put("imageCommentList", imageCommentList);
		addCacheMember(model, ServiceHelper.getMemberIdListFromBeanList(imageCommentList));
		model.put("logonMember", member);
		return "home/community/commuImageDetail.vm";
	}
	/**
	 * 设置相册封面
	 */
	@RequestMapping("/home/commu/setAlbumCover.xhtml")
	public String setAlbumCover(Long albumId,String imageUrl,ModelMap model){
		Member member = getLogonMember();
		Album album = daoService.getObject(Album.class, albumId);
		if(album == null) return showError(model, "相册不存在！");
		Commu commu = daoService.getObject(Commu.class, album.getCommuid());
		if(commu == null) return showJsonError(model, "数据有误！");
		if(!commu.hasStatus(Status.Y)) return showJsonError(model, "该相册的圈子已经被删除！");
		if(member.getId().equals(album.getMemberid()) || member.getId().equals(commu.getAdminid())
				|| member.getId().equals(commu.getSubadminid())){
			album.setLogo(imageUrl);
			daoService.updateObject(album);
			return showJsonSuccess(model);
		}else{
			return showJsonError(model, "你无权限做此操作！");
		}
	
	}
	
	/**
	 * 设置圈子背景
	 */
	@RequestMapping("/home/commu/updatecommubgpic.xhtml")
	public String updatecommubgpic(Long commuid, String picpath, ModelMap model){
		Commu commu = daoService.getObject(Commu.class, commuid);
		if(commu != null){
			if(!commu.hasStatus(Status.Y)) return showJsonError(model, "该圈子已经被删除！");
			commu.setCommubg(picpath);
			commu.setUpdatetime(DateUtil.getCurFullTimestamp());
			daoService.saveObject(commu);
			return showJsonSuccess(model);
		}
		return showJsonError_DATAERROR(model);
	}
}
