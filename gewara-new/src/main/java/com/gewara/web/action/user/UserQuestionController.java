package com.gewara.web.action.user;


import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import com.gewara.model.bbs.qa.GewaAnswer;
import com.gewara.model.bbs.qa.GewaQuestion;
import com.gewara.model.drama.Drama;
import com.gewara.model.movie.Movie;
import com.gewara.model.sport.SportItem;
import com.gewara.model.user.Member;
import com.gewara.model.user.MemberInfo;
import com.gewara.service.bbs.UserQuestionService;
import com.gewara.service.member.FriendService;
import com.gewara.support.ErrorCode;
import com.gewara.support.ServiceHelper;
import com.gewara.untrans.gym.SynchGymService;
import com.gewara.util.DateUtil;
import com.gewara.util.RelatedHelper;
import com.gewara.web.action.BaseHomeController;
import com.gewara.web.util.PageUtil;
import com.gewara.xmlbind.gym.RemoteCourse;

@Controller
public class UserQuestionController extends BaseHomeController {
	@Autowired@Qualifier("userQuestionService")
	private UserQuestionService userQuestionService;
	public void setUserQuestionService(UserQuestionService userQuestionService) {
		this.userQuestionService = userQuestionService;
	}
	@Autowired@Qualifier("synchGymService")
	private SynchGymService synchGymService;
	
	@Autowired@Qualifier("friendService")
	private FriendService friendService;
	public void setFriendService(FriendService friendService) {
		this.friendService = friendService;
	}
	
	/**
	 * 用户发表的知道
	 * @param model
	 * @param pageNo
	 * @param questiontag
	 * @return
	 */
	@RequestMapping("/home/qa/questionList.xhtml")
	public String getUserQuestionList(ModelMap model,Integer pageNo, Long memberid, String type){
		Member mymember = getLogonMember();
		if(mymember == null) return showError(model, "您还没登录，请返回登录！");
		if(memberid==null){//自己
			memberid = mymember.getId();
		}else mymember = daoService.getObject(Member.class, memberid);
		if(mymember == null) return showError(model, "该用户不存在！");
		//判断访问权限
		if(memberid!=null&&!memberid.equals(mymember.getId())){
			model.putAll(friendService.isPrivate(memberid));
		}
		getUserQuestionOrAnswer(model, pageNo, memberid, type, mymember);
		return "home/qa/qaList.vm";
	}
	
	@RequestMapping("/home/qa/questionAndAnswerList.xhtml")
	public String getQuestionList(ModelMap model,Integer pageNo, Long memberid, String type){
		getUserQuestionOrAnswer(model, pageNo, memberid, type, getLogonMember());
		if("question".equals(type))
			return "home/qa/qaListTable.vm";
		return "home/qa/reqaListTable.vm";
	}
	
	//新版知道
	@RequestMapping("/home/qa/newQuestionList.xhtml")
	public String getNewUserQuestionList(ModelMap model,Integer pageNo, Long memberid, String type){
		Member logonMember = getLogonMember();
		Member member = null;
		if(memberid==null){//自己
			member = logonMember;
		}else {
			member = daoService.getObject(Member.class, memberid);
		}
		getNewUserQuestionOrAnswer(model, pageNo, member.getId(), type, logonMember);
		this.getHomeLeftNavigate(daoService.getObject(MemberInfo.class, member.getId()), model);
		return "sns/userQa/myReply.vm";
	}
	
	@RequestMapping("/home/qa/newQuestionAndAnswerList.xhtml")
	public String getNewQuestionList(ModelMap model,Integer pageNo, Long memberid, String type){
		Member logonMember = getLogonMember();
		Member member = null;
		if(memberid == null){//自己
			member = logonMember;
		}else{
			member = daoService.getObject(Member.class, memberid);
			if(member == null) return showError(model, "该用户不存在！");
		}
		getNewUserQuestionOrAnswer(model, pageNo, member.getId(), type, logonMember);
		return "sns/userQa/myQa.vm";
	}

	private void getUserQuestionOrAnswer(ModelMap model, Integer pageNo, Long memberid, String type, Member logonMember) {
		model.putAll(controllerService.getCommonData(model, logonMember, memberid));
		Member member = daoService.getObject(Member.class, memberid);
		if(pageNo==null) pageNo = 0;
		int rowsPerPage=20;
		int count=0;
		List<GewaQuestion> listQuestion=new ArrayList<GewaQuestion>();
		Map<Long, String> dayMap = new HashMap<Long, String>();
		List<GewaQuestion> listAnswer=new ArrayList<GewaQuestion>();
		Map<Long, Integer> adoptMap = new HashMap<Long, Integer>();
		RelatedHelper rh = new RelatedHelper(); 
		model.put("relatedHelper", rh);
		if("question".equals(type)){
			//当前用户发表的知道
			listQuestion=userQuestionService.getQuestionByMemberid(member.getId(), pageNo*rowsPerPage,rowsPerPage);
			if(listQuestion!=null){
				for(GewaQuestion  question : listQuestion){
					Timestamp starttime = question.getAddtime();
					Timestamp endtime = new Timestamp(System.currentTimeMillis());
					long milliseconds = (DateUtil.addDay(starttime, GewaQuestion.MAXDAYS).getTime() - endtime.getTime()) / 1000;
					if (milliseconds > 0) {
						long days = milliseconds / (24 * 60 * 60);// 相差的天数
						long hours = (milliseconds - days * 24 * 60 * 60) / (60 * 60);// 相差的小时数
						dayMap.put(question.getId(), days + "天" + hours + "小时");
					}else {
						dayMap.put(question.getId(), "问题需要处理");
					}
				}
			}
			count=userQuestionService.getQuestionCountByMemberid(member.getId());
		}else{
			//当前用户回复的知道
			listAnswer=userQuestionService.getAnswerByMemberid(member.getId(), pageNo*rowsPerPage, rowsPerPage);
			if(listAnswer!=null){
				for(GewaQuestion question : listAnswer){
					GewaAnswer gewaAnswer = userQuestionService.getGewaAnswerByAnswerid(question.getId(),member.getId());
					if(GewaAnswer.AS_STATUS_Y.equals(gewaAnswer.getAnswerstatus())){
						adoptMap.put(question.getId(), 1);
					}else {
						adoptMap.put(question.getId(), 0);
					}
				}
			}
			count=userQuestionService.getAnswerCountByMemberid(member.getId());
		}
		//获取当前用户发表的知道所关联的信息
		Map<Long,Object> mapQuestion=new HashMap<Long, Object>();
		Map<Long,String> mapQuestionTag=new HashMap<Long, String>();
		this.getCommonGuanLian(listQuestion, mapQuestion, mapQuestionTag);
		//当前用户好友的知道
		//List<GewaQuestion> listQuestionFriend=userQuestionService.getQuestionFriendByMemberId(mymember.getId(), 0, 6);
		//待解决的问题
		//List<GewaQuestion> listNoAnswer=userQuestionService.getNoAnswerQuestionList(0, 6);
		//热门知道
		//List<GewaQuestion> hotQuestionList = userQuestionService.getGewaQuestionByHotValue(0, 6);
		//最近的知道
		//List<GewaQuestion> newQuestionList = userQuestionService.getGewaQuestionList(0, 6);
		
		PageUtil pageUtil=new PageUtil(count,rowsPerPage,pageNo,"/home/qa/questionAndAnswerList.xhtml", true, true);
		Map params = new HashMap();
		params.put("type", type);
		params.put("memberid", member.getId());
		pageUtil.initPageInfo(params);
		//model.put("hotQuestionList", hotQuestionList);
		//model.put("newQuestionList", newQuestionList);
		model.put("listQuestion", listQuestion);
		model.put("dayMap", dayMap);
		model.put("listAnswer", listAnswer);
		model.put("adoptMap", adoptMap);
		//model.put("listQuestionFriend", listQuestionFriend);
		//model.put("listNoAnswer", listNoAnswer);
		model.put("mapQuestion",mapQuestion );
		model.put("mapQuestionTag",mapQuestionTag );
		model.put("count", count);
		model.put("pageUtil", pageUtil);
	}
	
	//新版知道
	private void getNewUserQuestionOrAnswer(ModelMap model, Integer pageNo, Long memberid, String type, Member logonMember) {
		model.putAll(controllerService.getCommonData(model, logonMember, memberid));
		Member member = daoService.getObject(Member.class, memberid);
		//判断访问权限
		if(memberid!=null&&!memberid.equals(logonMember.getId())){
			model.putAll(friendService.isPrivate(memberid));
		}
		if(pageNo==null) pageNo = 0;
		int rowsPerPage=20;
		int count=0;
		List<GewaQuestion> listQuestion=new ArrayList<GewaQuestion>();
		Map<Long, String> dayMap = new HashMap<Long, String>();
		Map<Long, Integer> adoptMap = new HashMap<Long, Integer>();
		Map<Long, MemberInfo> memberMap = new HashMap<Long, MemberInfo>();
		if("question".equals(type)){
			//当前用户发表的知道
			listQuestion=userQuestionService.getQuestionByMemberid(member.getId(), pageNo*rowsPerPage,rowsPerPage);
			if(listQuestion!=null){
				for(GewaQuestion  question : listQuestion){
					memberMap.put(question.getId(), daoService.getObject(MemberInfo.class, question.getReplymemberid()));
					Timestamp starttime = question.getAddtime();
					Timestamp endtime = new Timestamp(System.currentTimeMillis());
					long milliseconds = (DateUtil.addDay(starttime, GewaQuestion.MAXDAYS).getTime() - endtime.getTime()) / 1000;
					if (milliseconds > 0) {
						long days = milliseconds / (24 * 60 * 60);// 相差的天数
						long hours = (milliseconds - days * 24 * 60 * 60) / (60 * 60);// 相差的小时数
						dayMap.put(question.getId(), days + "天" + hours + "小时");
					}else {
						dayMap.put(question.getId(), "已结束");
					}
				}
			}
			count=userQuestionService.getQuestionCountByMemberid(member.getId());
		}else{
			//当前用户回复的知道
			listQuestion=userQuestionService.getAnswerByMemberid(member.getId(), pageNo*rowsPerPage, rowsPerPage);
			if(listQuestion!=null){
				for(GewaQuestion question : listQuestion){
					memberMap.put(question.getId(), daoService.getObject(MemberInfo.class, question.getReplymemberid()));
					GewaAnswer gewaAnswer = userQuestionService.getGewaAnswerByAnswerid(question.getId(),member.getId());
					if(GewaAnswer.AS_STATUS_Y.equals(gewaAnswer.getAnswerstatus())){
						adoptMap.put(question.getId(), 1);
					}else {
						adoptMap.put(question.getId(), 0);
					}
					Timestamp starttime = question.getAddtime();
					Timestamp endtime = new Timestamp(System.currentTimeMillis());
					long milliseconds = (DateUtil.addDay(starttime, GewaQuestion.MAXDAYS).getTime() - endtime.getTime()) / 1000;
					if (milliseconds > 0) {
						long days = milliseconds / (24 * 60 * 60);// 相差的天数
						long hours = (milliseconds - days * 24 * 60 * 60) / (60 * 60);// 相差的小时数
						dayMap.put(question.getId(), "剩余 " + days + "天" + hours + "小时");
					}else {
						dayMap.put(question.getId(), "已结束");
					}
				}
			}
			count=userQuestionService.getAnswerCountByMemberid(member.getId());
		}
		List<Long> memberidList = ServiceHelper.getMemberIdListFromBeanList(listQuestion);
	    addCacheMember(model, memberidList);
		//获取当前用户发表的知道所关联的信息
		Map<Long,Object> mapQuestion=new HashMap<Long, Object>();
		Map<Long,String> mapQuestionTag=new HashMap<Long, String>();
		this.getCommonGuanLian(listQuestion, mapQuestion, mapQuestionTag);
		
		PageUtil pageUtil=new PageUtil(count,rowsPerPage,pageNo,"/home/qa/newQuestionAndAnswerList.xhtml", true, true);
		Map params = new HashMap();
		params.put("type", type);
		params.put("memberid", member.getId());
		pageUtil.initPageInfo(params);
		model.put("listQuestion", listQuestion);
		model.put("dayMap", dayMap);
		model.put("mapQuestion",mapQuestion );
		model.put("mapQuestionTag",mapQuestionTag );
		model.put("pageUtil", pageUtil);
		model.put("memberMap", memberMap);
		model.put("mymember", member);
	}
	
	/**
	 * 用户回复的知道
	 * @param model
	 * @param pageNo
	 * @return
	 */
	@RequestMapping("/home/qa/answerList.xhtml")
	public String getUserAnswerList(ModelMap model,Integer pageNo, Long memberid, String type){
		Member member=getLogonMember();
		if(member==null) return showError(model, "您还没登录，请返回登录！");
		getUserQuestionOrAnswer(model, pageNo, memberid, type, member);
		if(pageNo==null) pageNo = 0;
		int rowsPerPage=20;
		int count=0;
		Map<Long, Integer> adoptMap = new HashMap<Long, Integer>();
		//当前用户回复的知道
		List<GewaQuestion> listAnswer=new ArrayList<GewaQuestion>();
		listAnswer=userQuestionService.getAnswerByMemberid(member.getId(), pageNo*rowsPerPage, rowsPerPage);
		if(listAnswer!=null){
			for(GewaQuestion question : listAnswer){
				GewaAnswer gewaAnswer = userQuestionService.getGewaAnswerByAnswerid(question.getId(),member.getId());
				if(GewaAnswer.AS_STATUS_Y.equals(gewaAnswer.getAnswerstatus())){
					adoptMap.put(question.getId(), 1);
				}else {
					adoptMap.put(question.getId(), 0);
				}
			}
		}
		count=userQuestionService.getAnswerCountByMemberid(member.getId());
		//获取当前用户回复的知道所关联的信息
		Map<Long,Object> mapAnswer=new HashMap<Long, Object>();
		Map<Long,String> mapAnswerTag=new HashMap<Long, String>();
		this.getCommonGuanLian(listAnswer, mapAnswer, mapAnswerTag);
		//当前用户好友的知道
		//List<GewaQuestion> listQuestionFriend=userQuestionService.getQuestionFriendByMemberId(member.getId(), 0, 6);
		//待解决的问题
		//List<GewaQuestion> listNoAnswer=userQuestionService.getNoAnswerQuestionList(0, 6);
		//热门知道
		//List<GewaQuestion> hotQuestionList = userQuestionService.getGewaQuestionByHotValue(0, 6);
		//最近的知道
		//List<GewaQuestion> newQuestionList = userQuestionService.getGewaQuestionList(0, 6);
		
		PageUtil pageUtil=new PageUtil(count,rowsPerPage,pageNo,"home/qa/answerList.xhtml", true, true);
		pageUtil.initPageInfo();
		//model.put("hotQuestionList", hotQuestionList);
		//model.put("newQuestionList", newQuestionList);
		model.put("listAnswer", listAnswer);
		model.put("adoptMap", adoptMap);
		//model.put("listQuestionFriend", listQuestionFriend);
		//model.put("listNoAnswer", listNoAnswer);
		model.put("count", count);
		model.put("pageUtil", pageUtil);
		model.put("logonMember", member);
		return "home/qa/reqaList.vm";
	}
	
	/**
	 * 获取知道所关联的信息
	 * @return
	 */
	private void getCommonGuanLian(List<GewaQuestion> list,Map<Long,Object> map,Map<Long,String> mapTag){
		for (GewaQuestion question : list) {
			if(question.getCategoryid()!=null){
				if("movie".equals(question.getCategory())){
					map.put(question.getId(), daoService.getObject(Movie.class, question.getCategoryid()));
					mapTag.put(question.getId(),question.getCategory());
				}else if("gymcourse".equals(question.getCategory())){
					ErrorCode<RemoteCourse> code = synchGymService.getRemoteCourse(question.getCategoryid(), true);
					if(code.isSuccess()){
						map.put(question.getId(), code.getRetval());
					}
					mapTag.put(question.getId(),question.getCategory());
				}else if("sportservice".equals(question.getCategory())){
					map.put(question.getId(), daoService.getObject(SportItem.class, question.getCategoryid()));
					mapTag.put(question.getId(),question.getCategory());
				}else if("drama".equals(question.getCategory())){
					map.put(question.getId(), daoService.getObject(Drama.class, question.getCategoryid()));
					mapTag.put(question.getId(), question.getCategory());
				}
			}
		}
	}
}
