package com.gewara.web.action.api2.bbs;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.gewara.Config;
import com.gewara.constant.ApiConstant;
import com.gewara.constant.ExpGrade;
import com.gewara.constant.Status;
import com.gewara.constant.SysAction;
import com.gewara.model.bbs.qa.GewaAnswer;
import com.gewara.model.bbs.qa.GewaQaExpert;
import com.gewara.model.bbs.qa.GewaQaPoint;
import com.gewara.model.bbs.qa.GewaQuestion;
import com.gewara.model.user.Member;
import com.gewara.service.bbs.BlogService;
import com.gewara.service.bbs.QaService;
import com.gewara.service.bbs.UserMessageService;
import com.gewara.untrans.monitor.RoleTag;
import com.gewara.util.BeanUtil;
import com.gewara.util.StringUtil;
import com.gewara.util.WebUtils;
import com.gewara.web.action.api.BaseApiController;

/**
 * 问答API
 * 
 * @author taiqichao
 * 
 */
@Controller
public class Api2QaController extends BaseApiController {

	@Autowired
	@Qualifier("qaService")
	private QaService qaService;

	@Autowired
	@Qualifier("blogService")
	private BlogService blogService;

	@Autowired
	@Qualifier("config")
	private Config config;

	@Autowired
	@Qualifier("userMessageService")
	private UserMessageService userMessageService;

	/**
	 * 问答列表
	 * 
	 * @param tag
	 *            关联标签
	 * @param relatedid
	 *            关联对象id
	 * @param from
	 *            页码
	 * @param maxnum
	 *            页大小
	 * @param citycode
	 *            城市代码
	 * @param status
	 *            状态，可选值:N(待解决),Y(已解决),Z(零解决),noproper(无满意答案)
	 * @param orderby
	 *            排序，可选值:clickedtimes(关注数)，hotvalue(热度)，addtime(添加时间)，updatetime
	 *            (更新时间)
	 * @param model
	 * @return
	 */
	@RequestMapping("/api2/qa/questionList.xhtml")
	public String questionList(
			@RequestParam("tag") String tag,
			Long relatedid,
			@RequestParam(defaultValue = "0", required = false, value = "from") Integer from,
			@RequestParam(defaultValue = "20", required = false, value = "maxnum") Integer maxnum,
			@RequestParam(required = false, value = "citycode") String citycode,
			@RequestParam(required = false, value = "status", defaultValue = "N") String status,
			@RequestParam(required = false, value = "orderby") String orderby,
			ModelMap model) {
		if (maxnum > 50) {
			maxnum = 50;
		}
		List<GewaQuestion> questionList = qaService.getQuestionList(citycode,tag, relatedid, status, orderby, from, maxnum);
		int count = qaService.getQuestionCount(citycode, tag, relatedid, status);
		model.put("count", count);
		model.put("questionList", questionList);
		return getXmlView(model, "api2/qa/questionList.vm");
	}
	
	/**
	 * 查询最新已解决问题以及最佳答案
	 * @return
	 */
	@RequestMapping("/api2/qa/newQaList.xhtml")
	public String newQaList(
			String tag,
			Long relatedid,
			@RequestParam(defaultValue = "5", required = false, value = "maxnum") Integer maxnum,
			@RequestParam(required = false, value = "citycode") String citycode,
			ModelMap model) {
		if (maxnum > 50) {
			maxnum = 50;
		}
		List<Long> memberidList=new ArrayList<Long>();
		List<GewaQuestion> questionList =qaService.getQuestionList(citycode,tag,relatedid,GewaQuestion.QS_STATUS_Y,"addtime",0,maxnum);
		//最佳回答
		Map<Long, GewaAnswer> answerMap = new HashMap<Long, GewaAnswer>();
		for (GewaQuestion gewaQuestion : questionList) {
			GewaAnswer gewaAnswer = qaService.getBestAnswerByQuestionid(gewaQuestion.getId());
			answerMap.put(gewaQuestion.getId(), gewaAnswer);
			memberidList.add(gewaAnswer.getMemberid());
			memberidList.add(gewaQuestion.getMemberid());
		}
		model.put("answerMap", answerMap);
		addCacheMember(model, memberidList);
		model.put("questionList", questionList);
		return getXmlView(model, "api2/qa/newQaList.vm");
	}
	

	/**
	 * 添加健身场馆咨询
	 * 
	 * @param citycode
	 * @param memberEncode
	 * @param tag
	 * @param category
	 * @param relatedid
	 * @param categoryid
	 * @param title
	 * @param model
	 * @return
	 */
	@SuppressWarnings("deprecation")
	@RequestMapping("/api2/qa/addQuestion.xhtml")
	public String addQuestion(String citycode,String memberEncode,
			String tag,Long relatedid,
			@RequestParam(required = true, value = "tag")  String category,
			@RequestParam(required = true, value = "relatedid") Long categoryid, String title, ModelMap model) {
		Member member = memberService.getMemberByEncode(memberEncode);
		if (member == null) {
			return getErrorXmlView(model, ApiConstant.CODE_MEMBER_NOT_EXISTS,"用户不存在！");
		}
		if (StringUtils.isBlank(title)) {
			return getErrorXmlView(model, ApiConstant.CODE_DATA_ERROR,"咨询内容不能为空！");
		}
		if (StringUtils.length(title) > 100) {
			return getErrorXmlView(model, ApiConstant.CODE_DATA_ERROR,"咨询内容不能超过100个字符！");
		}
		GewaQuestion q = new GewaQuestion(member.getId());
		q.setTag(tag);
		q.setRelatedid(relatedid);
		if (category != null)
			q.setCategory(category);
		if (category != null)
			q.setCategoryid(categoryid);
		q.setTitle(title);
		q.setCitycode(citycode);
		q.setMembername(member.getNickname());
		daoService.saveObject(q);
		return this.getDirectXmlView(model, "<data><result>true</result><questionid>"+q.getId()+"</questionid></data>");
	}

	/**
	 * 添加回复
	 * 
	 * @param memberEncode
	 * @param questionid
	 * @param ip
	 * @param content
	 * @param model
	 * @return
	 */
	@RequestMapping("/api2/qa/addAnswer.xhtml")
	public String answer(String memberEncode,
			@RequestParam(required = true, value = "questionid") Long questionid,
			String ip, String content, ModelMap model) {
		Member member = memberService.getMemberByEncode(memberEncode);
		if (member == null) {
			return getErrorXmlView(model, ApiConstant.CODE_MEMBER_NOT_EXISTS,"用户不存在！");
		}
		GewaAnswer answer = new GewaAnswer("");
		answer.setQuestionid(questionid);
		answer.setContent(content);
		answer.setMemberid(member.getId());
		answer.setIp(ip);
		GewaQuestion question = daoService.getObject(GewaQuestion.class,answer.getQuestionid());
		if(null==question){
			return getErrorXmlView(model, ApiConstant.CODE_DATA_ERROR,"问题不存在！");
		}
		if (StringUtils.isBlank(answer.getContent())) {
			return getErrorXmlView(model, ApiConstant.CODE_DATA_ERROR,"回答内容不能为空！");
		}
		if (StringUtil.getByteLength(answer.getContent()) > 4000) {
			return getErrorXmlView(model,ApiConstant.CODE_DATA_ERROR,"回答内容不能超过4000个字符！，当前你已输入"+ StringUtil.getByteLength(answer.getContent())+ "字符");
		}
		if (WebUtils.checkPropertyAll(answer)) {
			return getErrorXmlView(model, ApiConstant.CODE_DATA_ERROR,"含有非法字符！");
		}
		boolean addPoint = false;
		Integer point = ExpGrade.EXP_ANSWER_ADD_COMMON;
		if (question.getMemberid().longValue() != member.getId().longValue()
				&& !qaService.isAnswerQuestion(answer.getQuestionid(),
						member.getId())) { // 给回答问题的用户增加经验值(不包含自己)
			addPoint = true;
			GewaQaExpert expert = qaService.getQaExpertByMemberid(member
					.getId());
			if (expert != null
					&& GewaQaExpert.STATUS_Y.equals(expert.getStatus())) {
				memberService.addExpForMember(member.getId(),ExpGrade.EXP_ANSWER_ADD_EXPERT); // 专家
				point = ExpGrade.EXP_ANSWER_ADD_EXPERT;
			} else {
				memberService.addExpForMember(member.getId(),ExpGrade.EXP_ANSWER_ADD_COMMON); // 普通网友
			}
		}
		Long mid = member.getId();
		Member questionMember = daoService.getObject(Member.class,question.getMemberid());
		question.setReplymemberid(mid);
		question.setUpdatetime(new Timestamp(System.currentTimeMillis()));
		question.addReplycount();
		if (GewaQuestion.QS_STATUS_Z.equals(question.getQuestionstatus()))
			question.setQuestionstatus(GewaQuestion.QS_STATUS_N);
		String key = blogService.filterContentKey(answer.getContent());
		if (StringUtils.isNotBlank(key)) {
			answer.setStatus(Status.N_FILTER);
			daoService.saveObject(answer);
			String title = "有人发恶意知道回复！" + key;
			String value = "有人恶意发知道回复，包含过滤关键字memberId = " + member.getId()+ ",[用户IP:" + ip + "]" + question.getTitle() + "\n"+ answer.getContent();
			monitorService.saveSysWarn(title, value, RoleTag.bbs);
		} else {
			daoService.saveObject(answer);
		}
		daoService.saveObject(question);
		if (addPoint) {
			GewaQaPoint sendquestion = new GewaQaPoint(question.getId(),answer.getId(), member.getId(), point,GewaQaPoint.TAG_REPLYQUESTION);
			daoService.saveObject(sendquestion);
		}
		if (!question.getMemberid().equals(mid)
				&& (GewaQuestion.QS_STATUS_N.equals(question
						.getQuestionstatus()) || GewaQuestion.QS_STATUS_Z
						.equals(question.getQuestionstatus()))) {// 发送邮件
			model.put("questionnickname", questionMember.getNickname());
			model.put("nickname", member.getNickname());
			model.put("question",
					BeanUtil.getBeanMapWithKey(question, "id", "title"));
			model.put("content", answer.getContent());
			String body = "你在格瓦拉知道上的提问： <a href='" + config.getBasePath()+ "qa/qaDetail.xhtml?qid=" + question.getId() + "'>"+ question.getTitle() + "</a> 已有新的回答了。";
			userMessageService.sendSiteMSG(question.getMemberid(),SysAction.ACTION_QUESTION_NEW_ANSWER, question.getId(),body);
		}
		if (StringUtils.equals(answer.getStatus(), Status.N_FILTER)) {
			return getErrorXmlView(model, ApiConstant.CODE_DATA_ERROR,"你发表的帖子包含“敏感关键词”，通过管理员审核后显示!");
		}
		return getXmlView(model, "api/mobile/result.vm");
	}

}
