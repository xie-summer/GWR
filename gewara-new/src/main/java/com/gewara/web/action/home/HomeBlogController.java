package com.gewara.web.action.home;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import com.gewara.constant.Status;
import com.gewara.model.bbs.commu.Commu;
import com.gewara.model.user.Friend;
import com.gewara.model.user.Member;
import com.gewara.model.user.MemberInfo;
import com.gewara.service.member.FriendService;
import com.gewara.support.ErrorCode;
import com.gewara.untrans.activity.SynchActivityService;
import com.gewara.util.BeanUtil;
import com.gewara.web.action.BaseHomeController;
import com.gewara.web.util.PageUtil;
import com.gewara.xmlbind.activity.RemoteActivity;


@Controller
public class HomeBlogController extends BaseHomeController {
	@Autowired@Qualifier("friendService")
	private FriendService friendService;
	public void setMemberService(FriendService friendService) {
		this.friendService = friendService;
	}

	@Autowired@Qualifier("synchActivityService")
	private SynchActivityService synchActivityService;
	public void setActivityRemoteService(SynchActivityService synchActivityService) {
		this.synchActivityService = synchActivityService;
	}
	@RequestMapping("/home/activity/activityList.xhtml")
	public String getSnsActivityList(ModelMap model,Integer pageNo,String activityTag, Long memberid){
		Member logonMember = getLogonMember();
		Member member = null;
		if(memberid==null){//自己
			member = logonMember;
		}else {
			member = daoService.getObject(Member.class, memberid);
		}
		if(member == null) return showError(model, "该用户不存在！");
		//判断访问权限
		if(memberid!=null && !memberid.equals(logonMember.getId())){
			model.putAll(friendService.isPrivate(memberid));
		}
		model.putAll(controllerService.getCommonData(model, logonMember, member.getId()));
		if(pageNo==null) pageNo = 0;
		int rowsPerPage=10;
		int count=0;
		List<RemoteActivity> listActivity = new ArrayList<RemoteActivity>();
		if("adviseActivity".equals(activityTag)){//当前用户发起的活动
			ErrorCode<Integer> code = synchActivityService.getMemberActivityCount(member.getId(), null, RemoteActivity.TIME_ALL, null, null);
			if(code.isSuccess()) count = code.getRetval();
			ErrorCode<List<RemoteActivity>> code2 = synchActivityService.getMemberActivityListByMemberid(member.getId(), null, RemoteActivity.TIME_ALL, null, null, pageNo*rowsPerPage, rowsPerPage);
			if(code2.isSuccess()) listActivity = code2.getRetval();
		}else if("joinActivity".equals(activityTag)){//当前用户参加的活动
			ErrorCode<Integer> code = synchActivityService.getMemberJoinActivityCount(member.getId());
			if(code.isSuccess()) count = code.getRetval();
			ErrorCode<List<RemoteActivity>> code2 = synchActivityService.getMemberJoinActivityList(member.getId(),  pageNo*rowsPerPage, rowsPerPage);
			if(code2.isSuccess()) listActivity = code2.getRetval();
		}else if("friendActivity".equals(activityTag)){//当前用户好友的活动
			List<Friend> friendList = daoService.getObjectListByField(Friend.class, "memberfrom", memberid);
			List<Long> idList = BeanUtil.getBeanPropertyList(friendList, Long.class, "memberto", true);
			ErrorCode<Integer> code = synchActivityService.getFriendActivityCount(null, idList);
			if(code.isSuccess()) count = code.getRetval();
			ErrorCode<List<RemoteActivity>> code2 = synchActivityService.getFriendActivityList(null, idList, pageNo*rowsPerPage, rowsPerPage);
			if(code2.isSuccess()) listActivity = code2.getRetval();
		}
		//存储活动所属圈子信息
		Map<Long,Commu> mapCommu=new HashMap<Long, Commu>();
		if(listActivity!=null){
			for (RemoteActivity activity : listActivity) {
				Commu commu = daoService.getObject(Commu.class, activity.getCommunityid());
				if(commu == null || !commu.hasStatus(Status.Y)) continue;
				mapCommu.put(activity.getId(), commu);
			}
		}
		PageUtil pageUtil=new PageUtil(count,rowsPerPage,pageNo,"home/activity/activityList.xhtml", true, true);
		Map params=new HashMap();
		params.put("activityTag", activityTag);
		params.put("memberid", memberid);
		pageUtil.initPageInfo(params);
		model.put("mapCommu", mapCommu);
		model.put("listCommuActivity", listActivity);
		model.put("pageUtil",pageUtil);
		return "home/activity/activityList.vm";
	}
	
	//新版帖子入口
	@RequestMapping("/home/comment/newTopicList.xhtml")
	public String getNewTopicAndCommentList(ModelMap model, Long memberid) {
		Member logonMember = getLogonMember();
		Member member = null;
		if(memberid==null){//自己
			member = logonMember;
		}else {
			member = daoService.getObject(Member.class, memberid);
		}
		if(member == null) return showError(model, "该用户不存在！");
		//判断访问权限
		if(memberid!=null && !memberid.equals(logonMember.getId())){
			model.putAll(friendService.isPrivate(memberid));
		}
		model.putAll(controllerService.getCommonData(model, logonMember, member.getId()));
		this.getHomeLeftNavigate(daoService.getObject(MemberInfo.class, member.getId()), model);
		return "sns/userComment/mypost.vm";
	}
}
