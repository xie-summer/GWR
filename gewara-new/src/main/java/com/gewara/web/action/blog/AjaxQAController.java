package com.gewara.web.action.blog;

import java.sql.Timestamp;
import java.util.Arrays;
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

import com.gewara.Config;
import com.gewara.constant.CookieConstant;
import com.gewara.constant.ExpGrade;
import com.gewara.constant.Status;
import com.gewara.constant.SysAction;
import com.gewara.constant.TagConstant;
import com.gewara.model.bbs.qa.GewaAnswer;
import com.gewara.model.bbs.qa.GewaQaExpert;
import com.gewara.model.bbs.qa.GewaQaPoint;
import com.gewara.model.bbs.qa.GewaQuestion;
import com.gewara.model.user.Member;
import com.gewara.model.user.MemberInfo;
import com.gewara.service.OperationService;
import com.gewara.service.bbs.BlogService;
import com.gewara.service.bbs.QaService;
import com.gewara.service.bbs.UserMessageService;
import com.gewara.support.ErrorCode;
import com.gewara.untrans.CommentService;
import com.gewara.untrans.SearchService;
import com.gewara.untrans.ShareService;
import com.gewara.untrans.impl.ControllerService;
import com.gewara.untrans.monitor.MonitorService;
import com.gewara.untrans.monitor.RoleTag;
import com.gewara.util.BeanUtil;
import com.gewara.util.BindUtils;
import com.gewara.util.DateUtil;
import com.gewara.util.JsonUtils;
import com.gewara.util.StringUtil;
import com.gewara.util.WebUtils;
import com.gewara.util.XSSFilter;
import com.gewara.web.action.AnnotationController;
import com.gewara.xmlbind.bbs.Comment;
@Controller
public class AjaxQAController extends AnnotationController {
	
	@Autowired@Qualifier("controllerService")
	private ControllerService controllerService;
	public void setControllerService(ControllerService controllerService) {
		this.controllerService = controllerService;
	}
	@Autowired@Qualifier("userMessageService")
	private UserMessageService userMessageService;
	public void setUserMessageService(UserMessageService userMessageService) {
		this.userMessageService = userMessageService;
	}
	@Autowired@Qualifier("monitorService")
	private MonitorService monitorService;
	public void setMonitorService(MonitorService monitorService) {
		this.monitorService = monitorService;
	}
	@Autowired@Qualifier("config")
	private Config config;
	public void setConfig(Config config) {
		this.config = config;
	}
	@Autowired@Qualifier("blogService")
	private BlogService blogService;
	public void setBlogService(BlogService blogService) {
		this.blogService = blogService;
	}

	@Autowired@Qualifier("qaService")
	private QaService qaService;
	public void setQaService(QaService qaService) {
		this.qaService = qaService;
	}
	@Autowired@Qualifier("operationService")
	private OperationService operationService;
	public void setOperationService(OperationService operationService) {
		this.operationService = operationService;
	}
	@Autowired@Qualifier("commentService")
	private CommentService commentService;
	public void setCommentService(CommentService commentService) {
		this.commentService = commentService;
	}
	@Autowired@Qualifier("shareService")
	private ShareService shareService;
	public void setShareService(ShareService shareService) {
		this.shareService = shareService;
	}
	
	@Autowired@Qualifier("searchService")
	private SearchService searchService;
	public void setSearchService(SearchService searchService) {
		this.searchService = searchService;
	}
	
	//发知道
	@RequestMapping("/qa/saveQa.xhtml")
	public String saveQuestion(@CookieValue(value=LOGIN_COOKIE_NAME, required=false)String sessid,
			@CookieValue(value=CookieConstant.MEMBER_POINT,required=false)String pointxy, HttpServletRequest request,
			Long id, String title, String reward, String captchaId, String captcha, 
			HttpServletResponse response, ModelMap model) {
		boolean isValidCaptcha = controllerService.validateCaptcha(captchaId, captcha, WebUtils.getRemoteIp(request));
		if(!isValidCaptcha) return showJsonError(model, "验证码错误！");
		String ip = WebUtils.getRemoteIp(request);
		Member member = loginService.getLogonMemberBySessid(ip, sessid);
		String msg = "";
		if (member == null) return showJsonError(model, "请先登录！");
		if (blogService.isBlackMember(member.getId())) return showJsonError(model, "黑名单中");
		if (!qaService.isQuestion(member.getId(), GewaQuestion.MAXDAYS)) {
			msg = "你上次的提问超过"+GewaQuestion.MAXDAYS+"天内没有处理,暂时不能发起新提问!";
			return showJsonError(model, msg);
		}
		Integer eggs = blogService.isBadEgg(member);
		if (eggs != 777) {
			return showJsonError(model, eggs+"");
		}
		MemberInfo mi = daoService.getObject(MemberInfo.class, member.getId());
		if (mi != null && mi.getExpvalue() < Integer.parseInt(reward)){
			msg = "你现在的经验值是" + mi.getExpvalue() + "大于" + reward;
			return showJsonError(model, msg);
		}else if (mi == null && !"0".equals(reward)) {
			msg = "你现在的经验值是0,还不能给经验值!";
			return showJsonError(model, msg);
		}
		if (StringUtils.isBlank(title)) return showJsonError(model, "标题不能为空");
		if (WebUtils.checkString(title)) return showJsonError(model, "标题含有非法字符！");
		if (StringUtil.getByteLength(title)>100) return showJsonError(model, "标题太长,不能超过50个字");
		GewaQuestion question = null;
		String opkey = OperationService.TAG_ADDCONTENT + member.getId();
		
		boolean isnew = false;
		if (id != null) {// 修改日志
			question = daoService.getObject(GewaQuestion.class, id);
			if (!question.getMemberid().equals(member.getId())) return showJsonError(model, "你没有权限");
			question.setModtime(new Timestamp(System.currentTimeMillis()));
		} else {
			if(!blogService.allowAddContent(OperationService.TAG_ADDCONTENT, member.getId())){
				msg = "你发起提问的时间间隔太短, 歇会再发!";
				return showJsonError(model, msg);
			}
			question = new GewaQuestion(member.getId());
			question.setMembername(member.getNickname());
			isnew = true;
		}
		Map dataMap = request.getParameterMap();
		BindUtils.bindData(question, dataMap);
		if(WebUtils.checkPropertyAll(question)) return showJsonError(model, "含有非法字符！");
		if(StringUtil.getByteLength(question.getContent())>8000) return showJsonError(model, "内容字符过长！");
		question = XSSFilter.filterObjAttrs(question, "title","content");
		String key = blogService.filterContentKey(question.getTitle() + question.getContent());
		boolean filter = false;
		boolean isNight = false;
		if(StringUtils.isNotBlank(key)){
			question.setStatus(Status.N_FILTER);
			filter = true;
		}else{
			if(blogService.isNight()){
				question.setStatus(Status.N_NIGHT); //夜间提问
				filter = true;
				isNight = true;
			}
		}
		String citycode = WebUtils.getAndSetDefault(request, response);
		question.setCitycode(citycode);
		question.setIp(WebUtils.getIpAndPort(ip, request));
		daoService.saveObject(question);
		if (isnew) {
			GewaQaPoint qaPoint = new GewaQaPoint(question.getId(), member.getId(), ExpGrade.EXP_QUESTION_ADD, GewaQaPoint.TAG_SENDQUESTION);
			daoService.saveObject(qaPoint);
			memberService.addExpForMember(member.getId(), ExpGrade.EXP_QUESTION_ADD-question.getReward());
			operationService.updateOperation(opkey, 40);
		}
		if(filter){
			if(!isNight){
				String etitle = "有人发恶意知道！" + key;
				String content = "有人恶意问题，包含过滤关键字memberId = " + member.getId()+",[用户IP:" + WebUtils.getRemoteIp(request) + "]" + question.getTitle() + "\n" + question.getContent();
				monitorService.saveSysWarn(GewaQuestion.class, question.getId(), etitle, content, RoleTag.bbs);
			}else{
				Map jsMap = new HashMap();
				jsMap.put("retcode", 403);
				jsMap.put("returl", config.getBasePath()+"qa/");
				jsMap.put("retmsg", "夜间发表的知道需要通过管理员审核后才能显示！");
				return showJsonError(model, jsMap);
			}
		}else{// 如果选中发表到微博,
			String link = config.getBasePath() + "qa/q" + question.getId();
			String linkstr = "提问：<a href=\""+link+"\" target=\"_blank\" rel=\"nofollow\">"+question.getTitle()+"</a>";
			String pointx = null, pointy = null;
			if(StringUtils.isNotBlank(pointxy)){
				List<String> pointList = Arrays.asList(StringUtils.split(pointxy, ":"));
				if(pointList.size() == 2){
					pointx = pointList.get(0);
					pointy = pointList.get(1);
				}
			}
			String timeinfo = "";
			if (GewaQuestion.QS_STATUS_N.equals(question.getQuestionstatus()) || GewaQuestion.QS_STATUS_Z.equals(question.getQuestionstatus())) {
				Timestamp starttime = question.getAddtime();
				Timestamp endtime = new Timestamp(System.currentTimeMillis());
				long milliseconds = (DateUtil.addDay(starttime, GewaQuestion.MAXDAYS).getTime() - endtime.getTime()) / 1000;
				if (milliseconds > 0) {
					long days = milliseconds / (24 * 60 * 60);// 相差的天数
					long hours = (milliseconds - days * 24 * 60 * 60) / (60 * 60);// 相差的小时数
					timeinfo = "离问题结束还有" + days + "天" + hours + "小时";
				} else {
					timeinfo = "该问题需要处理了";
				}
			} else {
				if (question.getDealtime() != null && GewaQuestion.QS_STATUS_Y.equals(question.getQuestionstatus()))
					timeinfo ="解决时间：" + DateUtil.format(question.getDealtime(), "yyyy-MM-dd HH:mm");
			}
			Map otherinfoMap = new HashMap();
			otherinfoMap.put("timeinfo", timeinfo);
			otherinfoMap.put("reward", question.getReward());
			String otherinfo = JsonUtils.writeObjectToJson(otherinfoMap);
			ErrorCode<Comment> ec = commentService.addMicroComment(member, TagConstant.TAG_QA_MEMBER, question.getId(),  linkstr, "", null, null, false, null, otherinfo,pointx,pointy, WebUtils.getIpAndPort(ip, request), null);
			if(ec.isSuccess()){
				shareService.sendShareInfo("wala",ec.getRetval().getId(), ec.getRetval().getMemberid(), null);
			}
		}
		searchService.pushSearchKey(question);//更新索引至索引服务器
		return showJsonSuccess(model, ""+question.getId());
	}
	//回答
	@RequestMapping("/qa/saveAnswer.xhtml")
	public String answer(@CookieValue(value=LOGIN_COOKIE_NAME, required=false)String sessid,
			HttpServletRequest request, ModelMap model, String captchaId, String captcha) {
		boolean isValidCaptcha = controllerService.validateCaptcha(captchaId, captcha, WebUtils.getRemoteIp(request));
		if(!isValidCaptcha) return showJsonError(model, "验证码错误！");
		String ip = WebUtils.getRemoteIp(request);
		Member member = loginService.getLogonMemberBySessid(ip, sessid);
		if (member == null) return showJsonError_NOT_LOGIN(model);
		if (blogService.isBlackMember(member.getId())) return showJsonError_BLACK_LIST(model);
		String opkey = OperationService.TAG_ADDCONTENT + member.getId();
		boolean allow = operationService.isAllowOperation(opkey, 40);
		if(!allow) return showJsonError(model, "你操作的太频繁了, 歇会再发吧!");
		Integer eggs = blogService.isBadEgg(member);
		if (eggs != 777) {
			return showJsonError(model, eggs+"");
		}
		GewaAnswer answer = new GewaAnswer("");
		Map<String, String[]> answerMap = request.getParameterMap();
		BindUtils.bindData(answer, answerMap);
		answer.setMemberid(member.getId());
		answer.setIp(WebUtils.getIpAndPort(ip, request));
		answer.setContent(XSSFilter.filterAttr(answer.getContent()));
		if (StringUtils.isBlank(answer.getContent())) return showJsonError(model, "回答内容不能为空！");
		if (StringUtil.getByteLength(answer.getContent())>4000)return showJsonError(model, "回答内容不能超过4000个字符！，当前你已输入"+StringUtil.getByteLength(answer.getContent())+"字符");
		if (WebUtils.checkPropertyAll(answer)) return showJsonError(model, "含有非法字符！");
		GewaQuestion question = daoService.getObject(GewaQuestion.class, answer.getQuestionid());
		boolean addPoint = false;
		Integer point = ExpGrade.EXP_ANSWER_ADD_COMMON;
		if (question.getMemberid().longValue() != member.getId().longValue() && !qaService.isAnswerQuestion(answer.getQuestionid(), member.getId())) { // 给回答问题的用户增加经验值(不包含自己)
			addPoint = true;
			GewaQaExpert expert = qaService.getQaExpertByMemberid(member.getId());
			if (expert != null && GewaQaExpert.STATUS_Y.equals(expert.getStatus())) {
				memberService.addExpForMember(member.getId(), ExpGrade.EXP_ANSWER_ADD_EXPERT); // 专家
				point = ExpGrade.EXP_ANSWER_ADD_EXPERT;
			} else {
				memberService.addExpForMember(member.getId(), ExpGrade.EXP_ANSWER_ADD_COMMON); // 普通网友
			}
		}
		Long mid = member.getId();
		Member questionMember = daoService.getObject(Member.class, question.getMemberid());
		question.setReplymemberid(mid);
		question.setUpdatetime(new Timestamp(System.currentTimeMillis()));
		question.addReplycount();
		if (GewaQuestion.QS_STATUS_Z.equals(question.getQuestionstatus()))
			question.setQuestionstatus(GewaQuestion.QS_STATUS_N);
		String key = blogService.filterContentKey(answer.getContent());
		if(StringUtils.isNotBlank(key)){
			answer.setStatus(Status.N_FILTER);
			daoService.saveObject(answer);
			String title = "有人发恶意知道回复！" + key;
			String content = "有人恶意发知道回复，包含过滤关键字memberId = " + member.getId() +",[用户IP:" + WebUtils.getRemoteIp(request) + "]" + question.getTitle() + "\n" + answer.getContent();
			monitorService.saveSysWarn(title, content, RoleTag.bbs);
		}else {
			daoService.saveObject(answer);
		}
		daoService.saveObject(question);
		operationService.updateOperation(opkey, 40);
		if (addPoint) {
			GewaQaPoint sendquestion = new GewaQaPoint(question.getId(), answer.getId(), member.getId(), point, GewaQaPoint.TAG_REPLYQUESTION);
			daoService.saveObject(sendquestion);
		}
		if (!question.getMemberid().equals(mid) && (GewaQuestion.QS_STATUS_N.equals(question.getQuestionstatus()) || 
				GewaQuestion.QS_STATUS_Z.equals(question.getQuestionstatus()))) {// 发送邮件
			model.put("questionnickname", questionMember.getNickname());
			model.put("nickname", member.getNickname());
			model.put("question", BeanUtil.getBeanMapWithKey(question, "id", "title"));
			model.put("content", answer.getContent());
			String body = "你在格瓦拉知道上的提问： <a href='"+config.getBasePath()+"qa/qaDetail.xhtml?qid="+question.getId()+"'>"+question.getTitle()+"</a> 已有新的回答了。";
			userMessageService.sendSiteMSG(question.getMemberid(), SysAction.ACTION_QUESTION_NEW_ANSWER, question.getId(), body);
		}
		if(StringUtils.equals(answer.getStatus(), Status.N_FILTER)) return showJsonError_KEYWORD(model);
		return showJsonSuccess(model);
	}
	// 无满意答案
	@RequestMapping("/qa/noproper.xhtml")
	public String noproper(@CookieValue(value=LOGIN_COOKIE_NAME, required=false)String sessid,
			HttpServletRequest request, Long qid, ModelMap model) {
		String ip = WebUtils.getRemoteIp(request);
		Member member = loginService.getLogonMemberBySessid(ip, sessid);
		if (member == null) return showJsonError_NOT_LOGIN(model);
		if (blogService.isBlackMember(member.getId())) return showJsonError_BLACK_LIST(model);
		GewaQuestion question = daoService.getObject(GewaQuestion.class, qid);
		if (question.getMemberid().longValue() != member.getId().longValue()) return showJsonError_NORIGHTS(model);
		question.setQuestionstatus(GewaQuestion.QS_STATUS_NOPROPER);
		question.setDealtime(new Timestamp(System.currentTimeMillis()));
		daoService.saveObject(question);
		return showJsonSuccess(model);
	}
	// 删除
	@RequestMapping("/qa/delQuestion.xhtml")
	public String delQuestion(@CookieValue(value=LOGIN_COOKIE_NAME, required=false)String sessid,
			HttpServletRequest request, Long qid, ModelMap model) {
		String ip = WebUtils.getRemoteIp(request);
		Member member = loginService.getLogonMemberBySessid(ip, sessid);
		if (member == null) return showJsonError_NOT_LOGIN(model);
		if (blogService.isBlackMember(member.getId())) return showJsonError_BLACK_LIST(model);
		GewaQuestion question = daoService.getObject(GewaQuestion.class, qid);
		if (question.getMemberid().longValue() != member.getId().longValue()) return showJsonError_NORIGHTS(model);
		question.setStatus(Status.N_DELETE);
		GewaQaPoint qaPoint = qaService.getGewaQaPointByQuestionidAndTag(qid, GewaQaPoint.TAG_SENDQUESTION);
		daoService.saveObject(question);
		daoService.removeObject(qaPoint);
		List<GewaAnswer> answerList = qaService.getAnswerListByQuestionid(qid);
		for (GewaAnswer answer : answerList) {
			answer.setStatus(Status.N_DELETE);
		}
		daoService.saveObjectList(answerList);
		memberService.addExpForMember(member.getId(), -ExpGrade.EXP_DIARY_ADD);
		searchService.pushSearchKey(question);//更新索引至索引服务器
		return showJsonSuccess(model);
	}
	// 补充
	@RequestMapping("/qa/addInfo.xhtml")
	public String addInfo(@CookieValue(value=LOGIN_COOKIE_NAME, required=false)String sessid,
			HttpServletRequest request, Long qid, ModelMap model) {
		String ip = WebUtils.getRemoteIp(request);
		Member member = loginService.getLogonMemberBySessid(ip, sessid);
		if (member == null) return showJsonError_NOT_LOGIN(model);
		if (blogService.isBlackMember(member.getId())) return showJsonError_BLACK_LIST(model);
		GewaQuestion question = daoService.getObject(GewaQuestion.class, qid);
		Map<String, String[]> questionMap = request.getParameterMap();
		BindUtils.bindData(question, questionMap);
		if (StringUtils.isBlank(question.getAddinfo())) return showJsonError(model, "补充内容不能为空！");
		if (StringUtil.getByteLength(question.getAddinfo()) > 400) return showJsonError(model, "补充内容不能超过200个字！");
		question.setAddinfotime(new Timestamp(System.currentTimeMillis()));
		question.setModtime(new Timestamp(System.currentTimeMillis()));
		question.setAddinfo(XSSFilter.filterAttr(question.getAddinfo()));
		daoService.saveObject(question);
		return showJsonSuccess(model);
	}

	// 设置正确答案
	@RequestMapping("/qa/bestAnswer.xhtml")
	public String bestAnswer(@CookieValue(value=LOGIN_COOKIE_NAME, required=false)String sessid,
			HttpServletRequest request, Long qid, Long aid, ModelMap model) {
		String ip = WebUtils.getRemoteIp(request);
		Member member = loginService.getLogonMemberBySessid(ip, sessid);
		if (member == null) return showJsonError_NOT_LOGIN(model);
		if (blogService.isBlackMember(member.getId())) return showJsonError_BLACK_LIST(model);
		GewaQuestion question = daoService.getObject(GewaQuestion.class, qid);
		if (GewaQuestion.QS_STATUS_Y.equals(question.getQuestionstatus()))
			return showJsonError(model, "问题已经有最佳答案,不能设置!");
		GewaAnswer answer = daoService.getObject(GewaAnswer.class, aid);
		if (member.getId().longValue() == answer.getMemberid().longValue())
			return  showJsonError(model, "不能设置自己的回答为最佳答案!");
		int reward = question.getReward();
		memberService.addExpForMember(answer.getMemberid(), reward); // 增加经验值
		//blogService.addMemberExperience(question.getMemberid(), -reward); // 减掉经验值
		answer.setAnswerstatus(GewaAnswer.AS_STATUS_Y);
		question.setQuestionstatus(GewaQuestion.QS_STATUS_Y);
		question.setDealtime(new Timestamp(System.currentTimeMillis()));
		GewaQaPoint bestAnswer = new GewaQaPoint(question.getId(), answer.getId(), answer.getMemberid(), question.getReward(),
				GewaQaPoint.TAG_BESTANSWER);
		daoService.saveObject(bestAnswer);
		daoService.saveObject(answer);
		daoService.saveObject(question);
		Member answermember = daoService.getObject(Member.class, answer.getMemberid());
		model.put("answernickname", answermember.getNickname());
		model.put("nickname", member.getNickname());
		model.put("question", BeanUtil.getBeanMapWithKey(question, "id", "reward"));
		String body = "你在格瓦拉知道的回复 <a href='"+config.getBasePath()+"qa/qaDetail.xhtml?qid="+question.getId()+"'>"+question.getTitle()+"</a> 被采纳为最佳答案了。";
		userMessageService.sendSiteMSG(answer.getMemberid(), SysAction.ACTION_ANSWER_IS_BEST, answer.getId(), body);
		return showJsonSuccess(model);
	}
	
	// 申请专家
	@RequestMapping("/qa/applyExpert.xhtml")
	public String applyExpert(@CookieValue(value=LOGIN_COOKIE_NAME, required=false)String sessid,
			HttpServletRequest request, String reason, ModelMap model) {
		String ip = WebUtils.getRemoteIp(request);
		Member member = loginService.getLogonMemberBySessid(ip, sessid);
		if (member == null) return showJsonError_NOT_LOGIN(model);
		if (blogService.isBlackMember(member.getId())) return showJsonError_BLACK_LIST(model);
		Map<String, String[]> expertMap = request.getParameterMap();
		if (StringUtil.getByteLength(reason) > 400) return showJsonError(model, "我的优势不能超过200个字！");
		GewaQaExpert expert = qaService.getQaExpertByMemberid(member.getId());
		if (expert != null) {
			if (GewaQaExpert.STATUS_Y.equals(expert.getStatus()))
				return showJsonError(model, "你已经是知道专家!");
			else {
				expert.setAddtime(new Timestamp(System.currentTimeMillis()));
			}
		} else {
			expert = new GewaQaExpert(member.getId());
		}
		BindUtils.bindData(expert, expertMap);
		daoService.saveObject(expert);
		return showJsonSuccess(model);
	}
}
