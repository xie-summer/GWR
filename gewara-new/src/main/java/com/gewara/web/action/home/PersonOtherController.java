package com.gewara.web.action.home;


import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.RequestMapping;

import com.gewara.constant.MemberConstant;
import com.gewara.constant.SmsConstant;
import com.gewara.constant.Status;
import com.gewara.constant.TagConstant;
import com.gewara.constant.content.SignName;
import com.gewara.constant.sys.MongoData;
import com.gewara.json.MemberStats;
import com.gewara.model.bbs.Diary;
import com.gewara.model.bbs.DiaryHist;
import com.gewara.model.bbs.Moderator;
import com.gewara.model.bbs.commu.Commu;
import com.gewara.model.content.GewaCommend;
import com.gewara.model.drama.Drama;
import com.gewara.model.drama.Theatre;
import com.gewara.model.movie.Cinema;
import com.gewara.model.movie.Movie;
import com.gewara.model.pay.SMSRecord;
import com.gewara.model.sport.Sport;
import com.gewara.model.sport.SportItem;
import com.gewara.model.user.Agenda;
import com.gewara.model.user.Member;
import com.gewara.model.user.MemberInfo;
import com.gewara.model.user.Point;
import com.gewara.model.user.Treasure;
import com.gewara.pay.CalendarUtil;
import com.gewara.service.MessageService;
import com.gewara.service.OperationService;
import com.gewara.service.bbs.AgendaService;
import com.gewara.service.bbs.BlogService;
import com.gewara.service.bbs.CommuService;
import com.gewara.service.bbs.DiaryService;
import com.gewara.service.member.PointService;
import com.gewara.support.ErrorCode;
import com.gewara.support.ServiceHelper;
import com.gewara.untrans.CommentService;
import com.gewara.untrans.WalaApiService;
import com.gewara.untrans.activity.SynchActivityService;
import com.gewara.untrans.gym.SynchGymService;
import com.gewara.util.BeanUtil;
import com.gewara.util.DateUtil;
import com.gewara.util.RelatedHelper;
import com.gewara.util.StringUtil;
import com.gewara.util.WebUtils;
import com.gewara.web.action.BaseHomeController;
import com.gewara.xmlbind.activity.RemoteActivity;
import com.gewara.xmlbind.bbs.Comment;
import com.gewara.xmlbind.bbs.ReComment;
import com.gewara.xmlbind.gym.RemoteCoach;
import com.gewara.xmlbind.gym.RemoteCourse;
import com.gewara.xmlbind.gym.RemoteGym;


@Controller
public class PersonOtherController extends BaseHomeController{
	@Autowired@Qualifier("pointService")
	private PointService pointService;
	@Autowired@Qualifier("commentService")
	private CommentService commentService;
	@Autowired@Qualifier("walaApiService")
	private WalaApiService walaApiService;
	@Autowired@Qualifier("synchGymService")
	private SynchGymService synchGymService;
	@Autowired@Qualifier("synchActivityService")
	private SynchActivityService synchActivityService;
	@Autowired@Qualifier("agendaService")
	private AgendaService agendaService;
	@Autowired@Qualifier("blogService")
	private BlogService blogService;
	@Autowired@Qualifier("messageService")
	private MessageService messageService;
	@Autowired@Qualifier("operationService")
	private OperationService operationService;
	@Autowired@Qualifier("commuService")
	private CommuService commuService;
	@Autowired@Qualifier("diaryService")
	private DiaryService diaryService;
	
	public static final String TYPE_MODERATOR = "moderator";//话题
	
	//我的任务
	@RequestMapping("/home/sns/myNewTasks.xhtml")
	public String myNewTasks(ModelMap model){
		Member member = getLogonMember();
		MemberInfo memberInfo = daoService.getObject(MemberInfo.class, member.getId());
		//左侧导航
		this.getHomeLeftNavigate(memberInfo ,model);
		//新手任务
		if(!memberInfo.isAllNewTaskFinished()){
			//是否绑定手机
			if(!memberInfo.isFinishedTask(MemberConstant.TASK_BINDMOBILE)){
				if(member.isBindMobile()){
					memberInfo = memberService.saveNewTask(memberInfo.getId(), MemberConstant.TASK_BINDMOBILE);
				}
			}
			//是否上传头像
			if(!memberInfo.isFinishedTask(MemberConstant.TASK_UPDATE_HEAD_PIC)){
				if(StringUtils.isNotBlank(memberInfo.getHeadpic())){
					memberInfo = memberService.saveNewTask(memberInfo.getId(), MemberConstant.TASK_UPDATE_HEAD_PIC);
				}
			}
			Map dataMap = memberCountService.getMemberCount(memberInfo.getId());
			//是否有5位好友或关注5位好友,新手任务添加一项
			if(!memberInfo.isFinishedTask(MemberConstant.TASK_FIVEFRIEND)){
				Integer friendCount = 0;
				if(dataMap != null && dataMap.get(MemberStats.FIELD_ATTENTIONCOUNT) != null){
					friendCount = (Integer)dataMap.get(MemberStats.FIELD_ATTENTIONCOUNT);
				}
				if(friendCount>=5){
					memberInfo = memberService.saveNewTask(memberInfo.getId(), MemberConstant.TASK_FIVEFRIEND);
				}
			}
			//是否发哇啦
			if(!memberInfo.isFinishedTask(MemberConstant.TASK_SENDWALA)){
				if(dataMap != null && dataMap.get(MemberStats.FIELD_COMMENTCOUNT) != null){
					int count = Integer.valueOf(dataMap.get(MemberStats.FIELD_COMMENTCOUNT)+"");
					if(count > 0) {
						memberInfo = memberService.saveNewTask(memberInfo.getId(), MemberConstant.TASK_SENDWALA);
					}
				}
			}
			//是否加入圈子
			if(!memberInfo.isFinishedTask(MemberConstant.TASK_JOINCOMMU)){
				int count = commuService.getCommuCountByMemberId(memberInfo.getId());
				if(count > 0) {
					memberInfo = memberService.saveNewTask(memberInfo.getId(), MemberConstant.TASK_JOINCOMMU);
				}
			}
			//发帖子
			if(!memberInfo.isFinishedTask(MemberConstant.TASK_MOVIE_COMMENT)){
				int count = diaryService.getDiaryCountByMemberid(Diary.class, null, TagConstant.TAG_CINEMA, memberInfo.getId());
				if(count == 0){
					count = diaryService.getDiaryCountByMemberid(DiaryHist.class, null, TagConstant.TAG_CINEMA, memberInfo.getId());
				}
				if(count > 0){
					memberInfo = memberService.saveNewTask(member.getId(), MemberConstant.TASK_MOVIE_COMMENT);
				}
			}
		}
		model.put("member", member);
		model.put("memberInfo", memberInfo);
		return "sns/userTask/myTask.vm";
	}
	//任务完成积分领取
	@RequestMapping("/home/sns/myNewTasksReward.xhtml")
	public String myNewTasksReward(String tag, ModelMap model){
		if(StringUtils.isBlank(tag)) {
			return showJsonError(model, "参数错误！");
		}
		Member member = getLogonMember();
		
		ErrorCode<Point> result = pointService.addNewTaskPoint(member.getId(), tag);
		if(!result.isSuccess()) return showJsonError(model, result.getMsg());
		return showJsonSuccess(model);
	}
	
	//生活安排
	@RequestMapping("/home/sns/myLifePlans.xhtml")
	public String myLifePlans(ModelMap model){
		Member member = getLogonMember();
		MemberInfo memberInfo = daoService.getObject(MemberInfo.class, member.getId());
		//左侧导航
		this.getHomeLeftNavigate(memberInfo ,model);
		model.put("member", member);
		model.put("memberInfo", memberInfo);
		model.put("mobile", member.getMobile());
		return "sns/live/myLive.vm";
	}
	//加载生活数据
	@RequestMapping("/home/sns/myLifePlansData.xhtml")
	public String myLifePlansData(String tag, Date playDate, String startdate, ModelMap model){
		if(StringUtils.isNotBlank(startdate)) model.put("startdate", startdate);
		if(StringUtils.equals(tag, "addLive")) return "sns/live/addLive.vm";
		Member member = getLogonMember();
		List<Agenda> agendaList = new ArrayList<Agenda>();
		if(playDate == null) playDate = DateUtil.currentTime();
		Date startDate = null;
		Date endDate = null;
		Map<String, List<Agenda>> agMap = new HashMap<String, List<Agenda>>();
		if(playDate.before(DateUtil.addDay(DateUtil.currentTime(), 35)) && playDate.after(DateUtil.addDay(DateUtil.currentTime(), -35))){
			startDate = DateUtil.getMonthFirstDay(playDate);
			endDate = DateUtil.getNextMonthFirstDay(playDate);
			agendaList = agendaService.getAgendaListByDate(member.getId(), startDate, endDate);
			if(agendaList != null && agendaList.size() != 0) agMap.put("playMonthAgenda", agendaList);
		}
		model.put("playDate", playDate);
		model.put("mobile", member.getMobile());
		if(StringUtils.equals(tag, "date")){
			int year = DateUtil.getYear(playDate);
			int month = DateUtil.getMonth(playDate);
			CalendarUtil calendarUtil = new CalendarUtil(year, month);
			model.put("calendarUtil", calendarUtil);
			if(playDate.before(DateUtil.addDay(DateUtil.currentTime(), 35)) && playDate.after(DateUtil.addDay(DateUtil.currentTime(), -35))){
				Map<Date,List<Agenda>> agendaMap = BeanUtil.groupBeanList(agendaList, "startdate");
				model.put("agendaMap", agendaMap);
			}
			return "sns/live/dateLive.vm";
		}
		if(endDate != null){
			Date tempDate = DateUtil.getNextMonthFirstDay(endDate);
			List<Agenda> nextAgendaList = agendaService.getAgendaListByDate(member.getId(), endDate, tempDate);
			if(nextAgendaList != null && nextAgendaList.size() != 0) agMap.put("nextMonthAgenda", nextAgendaList);
		}
		model.put("agMap", agMap);
		return "sns/live/listLive.vm";
	}
	//保存生活安排
	@RequestMapping("/home/sns/ajax/saveLiveData.xhtml")
	public String saveLiveData(HttpServletRequest request, String title, String tag, 
			String startdate, String starttime, String address, String captchaId, String captcha, ModelMap model){
		boolean isValidCaptcha = controllerService.validateCaptcha(captchaId, captcha, WebUtils.getRemoteIp(request));
		if (!isValidCaptcha) return showJsonError_CAPTCHA_ERROR(model);
		Member member = getLogonMember();
		if(StringUtils.isBlank(title)) return showJsonError(model, "主题不能为空！");
		if(StringUtil.getByteLength(title) > 100) return showJsonError(model, "主题过长！");
		if(StringUtils.isBlank(startdate) || StringUtils.isBlank(starttime)) return showJsonError(model, "开始时间不能为空！");
		Date st = DateUtil.parseDate(startdate + " " + starttime, "yyyy-MM-dd HH:mm");
		if(!DateUtil.isAfter(st)) return showJsonError(model, "开始时间不能在当前时间之前！");
		if(StringUtil.getByteLength(address) > 200) return showJsonError(model, "地址过长！");
		if(WebUtils.checkString(title + address)) return showJsonError(model,"含有非法字符!");
		String opkey = member.getId() + "saveLiveData";
		if(!operationService.updateOperation(opkey, 30)) return showJsonError(model, "你操作过于频繁，请稍后再试！");
		agendaService.addAgenda(title, member.getId(), member.getNickname(), DateUtil.parseDate(startdate), starttime, title, tag, null, null, null, DateUtil.getCurFullTimestamp(), TagConstant.AGENDA_ACTION_AGENDA, null, null, null, address, null);
		return showJsonSuccess(model);
	}
	//保存生活短信
	@RequestMapping("/home/sns/ajax/saveLiveSMS.xhtml")
	public String saveLiveSMS(HttpServletRequest request, Long aid, String nickname, String content, 
			String mobile, Integer sendtime, ModelMap model, String captchaId, String captcha){
		//TODO:暂时不可用，等待通知开放
		String t = "N";
		if(t.equals("N")) return showJsonError(model, "该短信提醒功能暂时不可用！");
		boolean isValidCaptcha = controllerService.validateCaptcha(captchaId, captcha, WebUtils.getRemoteIp(request));
		if(!isValidCaptcha) return showJsonError(model, "验证码错误！");
		if(StringUtils.isBlank(content)) return showJsonError(model, "短信内容不能为空！");
		if(StringUtil.getByteLength(content) > 120) return showJsonError(model, "短信内容不能大于60个字！");
		if(StringUtils.isBlank(mobile)) return showJsonError(model, "发送手机号码不能为空！");
		if(StringUtils.isNotBlank(blogService.filterContentKey(content))) return showJsonError(model, "短信内容包含过滤关键字");
		Agenda agenda = daoService.getObject(Agenda.class, aid);
		if(agenda == null) return showJsonError(model, "未找到此生活安排！");
		if(!DateUtil.isAfter(agenda.agendaTime())) return showJsonError(model, "当前生活安排已过期！");
		Member member = getLogonMember();
		if(blogService.isBlackMember(member.getId())) return showJsonError_BLACK_LIST(model);
		String[] mobileAll = StringUtils.split(mobile, ',');
		if(mobileAll.length > 4) return showJsonError(model, "每个生活只能安排邀请4个好友！");
		String opkey = member.getId() + "saveLiveSMS";
		if(!operationService.updateOperation(opkey, 10)) return showJsonError(model, "你操作过于频繁，请稍后再试！");
		Timestamp starttime = DateUtil.getCurTruncTimestamp();
		Timestamp endtime = DateUtil.getLastTimeOfDay(starttime);
		int count = messageService.querySmsRecord("ag", TagConstant.TAG_AGENDA, starttime, endtime, null, member.getId());
		if(count + mobileAll.length > 30) return showJsonError(model, "每个用户每天生活只能发送30条短信！");
		if(sendtime == null) sendtime = 0;
		if(StringUtils.isBlank(nickname)) content = "你安排了：" + content;
		else{
			if(StringUtil.getByteLength(nickname) > 10) return showJsonError(model, "名字不能大于5个字！");
			content = nickname + "邀你一起：" + content;
		} 
		return showJsonSuccess(model);
	}
	//删除生活安排和短信
	@RequestMapping("/home/sns/ajax/deleteMyLive.xhtml")
	public String deleteMyLive(Long aid, ModelMap model){
		Member member = getLogonMember();
		Agenda agenda = daoService.getObject(Agenda.class, aid);
		if(agenda == null) return showJsonError(model, "不存在此生活安排！");
		if(!DateUtil.isAfter(agenda.agendaTime())) return showJsonError(model, "当前生活安排已过期！");
		if(!agenda.getMemberid().equals(member.getId())) return showJsonError(model, "不是你自己的生活安排不能删除！");
		List<SMSRecord> smsList = daoService.getObjectListByField(SMSRecord.class, "tradeNo", "ag" + agenda.getId());
		List<SMSRecord> delSmsList = new ArrayList<SMSRecord>();
		for (SMSRecord smsRecord : smsList) {
			if(!StringUtils.contains(smsRecord.getStatus(), Status.Y)){
				smsRecord.setStatus(SmsConstant.STATUS_D + smsRecord.getStatus());
				delSmsList.add(smsRecord);
			}
		}
		daoService.saveObjectList(delSmsList);
		daoService.removeObject(agenda);
		return showJsonSuccess(model);
	}
	
	//社区首页头部加载
	@RequestMapping("/home/sns/ajax/loadHear.xhtml")
	public String loadHear(ModelMap model){
		Member member = getLogonMember();
		//MemberInfo memberInfo = daoService.getObject(MemberInfo.class, member.getId());
		//更新通知消息
		Integer syscount = memberService.getMemberNotReadSysMessageCount(member.getId());
		Integer lettercount =   memberService.getMemberNotReadNormalMessageCount(member.getId());
		Integer fanscount = 0;
		Map data = mongoService.getMap(MongoData.SYSTEM_ID, MongoData.NS_PROMPT_INFO, member.getId());
		if(data != null && data.get("fans") != null) fanscount = Integer.parseInt(data.get("fans")+"");
		model.put("syscount", syscount);
		model.put("lettercount", lettercount);
		model.put("fanscount", fanscount);
		model.put("tipcounts", syscount + fanscount + lettercount);
		addCacheMember(model, member.getId());
		model.put("logonMember", member);
		return "sns/common/header.vm";
	}
	
	//哇啦详情页
	@RequestMapping("/home/sns/walaDetail.xhtml")
	public String walaDetail(ModelMap model, Long cid, String RorY){
		Comment comment = commentService.getCommentById(cid);
		if(comment.getId() == null) return show404(model, "不存在该哇啦！");
		if(StringUtils.isNotBlank(comment.getTag()) && comment.getRelatedid() != null) commentTag(comment.getRelatedid(), comment.getTag(), model);
		
		Member logonMember = getLogonMember();
		Member member = daoService.getObject(Member.class, comment.getMemberid());
		MemberInfo memberInfo = null;
		if(comment.getMemberid().equals(logonMember.getId())){
			memberInfo = daoService.getObject(MemberInfo.class, logonMember.getId());
			this.getHomeLeftNavigate(memberInfo, model);
		}else{
			memberInfo = daoService.getObject(MemberInfo.class, comment.getMemberid());
			//是否被关注
			Boolean b = blogService.isTreasureMember(memberInfo.getId(), member.getId());
			model.put("b", b);
			
			String str = comment.getTopic();
			if(str != null){
				model.put("str", str);
			}
			
			//关注，哇啦，瓦丝数量
			Map dataMap = memberCountService.getMemberCount(memberInfo.getId());
			model.put("memberStats", dataMap);
			//ta关注的人
			List<Treasure> treasureList = blogService.getTreasureListByMemberId(memberInfo.getId(), new String[]{Treasure.TAG_MEMBER},null, null,0,9, Treasure.ACTION_COLLECT);
			List<Long> relatedidList = BeanUtil.getBeanPropertyList(treasureList, Long.class, "relatedid", false);
			List<MemberInfo> treasureMemberList = daoService.getObjectList(MemberInfo.class, relatedidList);
			model.put("treasureMemberList", treasureMemberList);
			//瓦丝
			List<Long> microFansIdList = blogService.getFanidListByMemberId(memberInfo.getId(), 0, 9);
			List<MemberInfo> memberInfoList = daoService.getObjectList(MemberInfo.class, microFansIdList);
			model.put("memberInfoList", memberInfoList);
			//关注他的人同时关注了
			List<Long> fansList = blogService.getTreasureListByMemberIdList(memberInfo.getId(), Treasure.TAG_MEMBER,0,9, Treasure.ACTION_COLLECT);
			List<MemberInfo> fansTreasureMemberList = daoService.getObjectList(MemberInfo.class,fansList);
			model.put("fansTreasureMemberList", fansTreasureMemberList);
			//他的圈子
			List<Commu> commuList=commuService.getCommuListByMemberId(memberInfo.getId(), 0, 3);
			model.put("commuList", commuList);
			model.put("type","other");
			model.put("isMy", "no");
		}
		model.put("RorY", Boolean.parseBoolean(RorY));
		model.put("comment", comment);
		model.put("logonMember", logonMember);
		model.put("member", member);
		model.put("memberInfo", memberInfo);
		return "sns/walaDetail.vm";
	}
	
	public void commentTag(Long id, String tag, ModelMap model){
		String name = "";
		if(tag.equals("movie")){
			Movie movie = daoService.getObject(Movie.class, id);
			name = movie.getMoviename();
		}else if(tag.equals("cinema")){
			Cinema cinema = daoService.getObject(Cinema.class, id);
			name = cinema.getName();
		}else if(tag.equals("theatre")){
			Theatre theatre = daoService.getObject(Theatre.class, id);
			name = theatre.getName();
		}else if(tag.equals("drama")){
			Drama drama = daoService.getObject(Drama.class, id);
			name = drama.getDramaname();
		}else if(tag.equals("sport")){
			Sport sport = daoService.getObject(Sport.class, id);
			name = sport.getName();
		}else if(tag.equals("sportitem")){
			SportItem item = daoService.getObject(SportItem.class, id);
			name = item.getItemname();
		}else if(tag.equals("gym")){
			ErrorCode<RemoteGym> code = synchGymService.getRemoteGym(id, true);
			if(code.isSuccess()){
				RemoteGym gym = code.getRetval();
				name = gym.getName();
			}
		}else if(tag.equals("gymcourse")){
			ErrorCode<RemoteCourse> code = synchGymService.getRemoteCourse(id, true);
			if(code.isSuccess()){
				RemoteCourse course = code.getRetval();
				name = course.getCoursename();
			}
		}else if(tag.equals("gymcoach")){
			ErrorCode<RemoteCoach> code = synchGymService.getRemoteCoach(id, true);
			if(code.isSuccess()){
				RemoteCoach coach = code.getRetval();
				name = coach.getCoachname();
			}
		}else if(tag.equals("activity") || tag.equals("member_activity")){
			ErrorCode<RemoteActivity> code = synchActivityService.getRemoteActivity(id);
			if(code.isSuccess()){
				name = code.getRetval().getTitle();
			}
		}
		model.put("tag", tag);
		model.put("name", name);
	}
	
	//短版URL
	@RequestMapping("/wala/ht.xhtml")
	public String moderatorDetailht(String t, ModelMap model){
		return showRedirect("/home/sns/moderatorDetail.xhtml?title="+t, model);
	}
	//话题详情页
	@RequestMapping("/home/sns/moderatorDetail.xhtml")
	public String moderatorList(String title, ModelMap model, HttpServletRequest request, HttpServletResponse response){
		Member member = getLogonMember();
		MemberInfo memberInfo = daoService.getObject(MemberInfo.class, member.getId());
		this.getHomeLeftNavigate(memberInfo, model);
		String citycode = WebUtils.getAndSetDefault(request, response);
		if(StringUtils.isNotBlank(title)){
			int count = commentService.searchCommentCount(title, TYPE_MODERATOR);
			model.put("count", count);
		}
		model.put("member", member);
		model.put("memberInfo", memberInfo);
		model.put("title", title);
		
		RelatedHelper rh = new RelatedHelper();
		model.put("relatedHelper", rh);
		//个人中心推荐话题
		List<GewaCommend> recommendModerator = commonService.getGewaCommendList(citycode, SignName.MEMBER_RECOMMEND_MODERATOR, null, null, true, 0, 2);
		Map<Long, Integer> modertorMap = new HashMap<Long, Integer>();
		for (GewaCommend gewaCommend : recommendModerator) {
			Moderator moderator = daoService.getObject(Moderator.class, gewaCommend.getRelatedid());
			modertorMap.put(gewaCommend.getId(), commentService.getModeratorDetailCount(moderator.getTitle()));
		}
		commonService.initGewaCommendList("recommendModerator", rh, recommendModerator);
		model.put("modertorMap", modertorMap);
		model.put("recommendModerator", recommendModerator);
		return "sns/topic.vm";
	}
	
	//加载哇啦信息
	@RequestMapping("/home/sns/lazeDetail.xhtml")
	public String lazeDatail(String title, Long cid, String tag, boolean RorY, Integer pageNumber, ModelMap model){
		Member member = getLogonMember();
		MemberInfo memberInfo = daoService.getObject(MemberInfo.class, member.getId());
		if(pageNumber == null) pageNumber = 0;
		Integer from = pageNumber *10;
		boolean display = false;
		//话题详细页信息
		if(StringUtils.isNotBlank(title)){
			List<Comment> topicList = commentService.searchCommentList(title, TYPE_MODERATOR, from, 15);
			int topicCount = commentService.searchCommentCount(title, TYPE_MODERATOR);
			List<Long> topicMemberidList = ServiceHelper.getMemberIdListFromBeanList(topicList);
			addCacheMember(model, topicMemberidList);
			model.put("topicCount", topicCount);
			model.put("title", title);
			model.put("topicList", topicList);
			if(topicList.size()>0){
				display = true;
			}
		}
		//哇啦详细页信息
		if(cid != null && tag.equals("")){
			Comment comment = commentService.getCommentById(cid);
			if(comment.getId() == null) return show404(model, "不存在该哇啦！");
			List<Comment> commentList = null;
			List<ReComment> reCommentList = null;
			if(RorY){
				//wala转载信息
				commentList = commentService.getCommentListByTransfer(cid, from, 15);
				List<Long> commentMemberIdList = ServiceHelper.getMemberIdListFromBeanList(commentList);
				addCacheMember(model, commentMemberIdList);
			}else {
				//wala回复信息
				reCommentList = walaApiService.getRecommentBycommentid(cid, from, 15);
				List<Long> reCommentMemberidList = ServiceHelper.getMemberIdListFromBeanList(reCommentList);
				addCacheMember(model, reCommentMemberidList);
			}
			model.put("reCommentList", reCommentList);
			model.put("commentList", commentList);
			model.put("RorY", RorY);
			model.put("comment", comment);
			if(commentList != null){
				if(commentList.size() > 0){
					display = true;
				}
			}
			if(reCommentList != null){
				if(reCommentList.size() > 0){
					display = true;
				}
			}
		}
		//通过tag来获得哇啦信息
		if(tag != null && tag != "" && cid != null){
			List<Comment> tagCommentList = commentService.getCommentListByRelatedId(tag,null, cid, null, from, 10);
			List<Long> tagMemberIdList = ServiceHelper.getMemberIdListFromBeanList(tagCommentList);
			addCacheMember(model, tagMemberIdList);
			model.put("tagCommentList", tagCommentList);
			if(tagCommentList.size() > 0){
				display = true;
			}
		}
		model.put("display", display);
		model.put("pageNumber", pageNumber);
		model.put("member", member);
		model.put("memberInfo", memberInfo);
		model.put("replyOne", "recommentMap");
		return "sns/lazeDetail.vm";
	}
	
	@RequestMapping("/ajax/sns/reCommentList.xhtml")
	public String reCommentList(@CookieValue(value = LOGIN_COOKIE_NAME, required = false)
	String sessid, HttpServletRequest request, Integer pageNo, Integer rows, Long cid, ModelMap model){
		if(pageNo == null) pageNo = 0;
		if(rows == null || rows <= 0) rows = 5;
		int firstPre = pageNo * rows;
		Comment comment = commentService.getCommentById(cid);
		List<ReComment> reCommentList = walaApiService.getRecommentBycommentid(cid, firstPre, rows);
		List<Long> MemberidList = ServiceHelper.getMemberIdListFromBeanList(reCommentList);
		addCacheMember(model, MemberidList);
		model.put("reCommentList", reCommentList);
		model.put("replyOne", "recomment");
		model.put("comment", comment);
		Member member = loginService.getLogonMemberBySessid(WebUtils.getRemoteIp(request), sessid);
		if(member != null){
			MemberInfo logonMember = daoService.getObject(MemberInfo.class, member.getId());
			model.put("member", member);
			model.put("logonMember", logonMember);
		}
		return "sns/replyComment.vm";
	}
}
