package com.gewara.web.action.admin.blog;

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.gewara.command.QuestionCommand;
import com.gewara.constant.Status;
import com.gewara.constant.content.SignName;
import com.gewara.model.bbs.qa.GewaAnswer;
import com.gewara.model.bbs.qa.GewaQaExpert;
import com.gewara.model.bbs.qa.GewaQuestion;
import com.gewara.model.content.GewaCommend;
import com.gewara.model.user.Member;
import com.gewara.service.bbs.QaService;
import com.gewara.support.MultiPropertyComparator;
import com.gewara.support.ReadOnlyTemplate;
import com.gewara.util.DateUtil;
import com.gewara.web.action.admin.BaseAdminController;
import com.gewara.web.util.PageUtil;

@Controller
public class QaAdminController extends BaseAdminController {
	@Autowired@Qualifier("qaService")
	private QaService qaService;
	public void setQaService(QaService qaService){
		this.qaService = qaService;
	}
	@Autowired@Qualifier("readOnlyTemplate")
	private ReadOnlyTemplate readOnlyTemplate;
	public void setReadOnlyHibernateTemplate(ReadOnlyTemplate readOnlyTemplate) {
		this.readOnlyTemplate = readOnlyTemplate;
	}
	// 后台首页
	@RequestMapping("/admin/blog/qaList.xhtml")
	public String qaList(ModelMap model, QuestionCommand qc) throws Exception {

		if (qc.pageNo == null)
			qc.pageNo = 0;
		Integer count = Integer.valueOf(readOnlyTemplate.findByCriteria(
				this.getQaQuery(qc).setProjection(Projections.rowCount())).get(0)+"");
		PageUtil pageUtil = new PageUtil(count, qc.rowsPerPage, qc.pageNo, "admin/blog/qaList.xhtml");
		Map params = new HashMap();
		params.put("keyname", qc.getKeyname());
		pageUtil.initPageInfo(params);
		List<GewaQuestion> questionList = readOnlyTemplate.findByCriteria(this.getQaQuery(qc), qc.pageNo
				* qc.rowsPerPage, qc.rowsPerPage);
		Map<Long, Member> lmMap = new HashMap<Long, Member>();
		for (GewaQuestion question : questionList) {
			Member m = daoService.getObject(Member.class, question.getMemberid());
			lmMap.put(question.getId(), m);
		}
		model.put("lmMap", lmMap);
		model.put("pageUtil", pageUtil);
		model.put("questionList", questionList);
		return "admin/blog/qa/qaList.vm";
	}

	private DetachedCriteria getQaQuery(QuestionCommand qc) {
		DetachedCriteria query = DetachedCriteria.forClass(GewaQuestion.class);
		if (StringUtils.isNotBlank(qc.getKeyname())) {
			query.add(Restrictions.ilike("title", qc.getKeyname(), MatchMode.ANYWHERE));
		}
		query.add(Restrictions.like("status", Status.Y, MatchMode.START));
		query.addOrder(Order.desc("addtime"));
		return query;
	}
	
	//查询该Id所有的回答信息
	@RequestMapping("/admin/blog/qaDetail.xhtml")
	public String answerList (@RequestParam("qid") Long qid,@RequestParam(required = false, value = "pageNo")
			Integer pageNo,ModelMap model){
		if (pageNo == null) pageNo=0;
		if (qid == null) return null;
		Integer count = qaService.getAnswerCount(qid);
		int rowsPerPage = 15;
		PageUtil pageUtil = new PageUtil(count, rowsPerPage, pageNo, "admin/blog/qaDetail.xhtml");
		Map params = new HashMap();
		params.put("qid", qid);
		pageUtil.initPageInfo(params);
		List<GewaAnswer> answerList = qaService.getAnswerListByQuestionId(pageNo * rowsPerPage, rowsPerPage,qid);
		Map<Long,Member> lmMap = new HashMap<Long,Member>();
		for(GewaAnswer answer:answerList){
			Member m = daoService.getObject(Member.class, answer.getMemberid());
			lmMap.put(answer.getId(), m);
		}
		model.put("lmMap", lmMap);
		model.put("pageUtil", pageUtil);
		model.put("answerList",answerList);
		return "admin/blog/qa/answerList.vm";
	}
	
	@RequestMapping("/admin/blog/qaExpertList.xhtml")
	public String ExpertList(@RequestParam(required = false,value="pageNo") Integer pageNo,ModelMap model) throws Exception{
		if (pageNo == null) pageNo=0;
		Integer count = qaService.getQAExpertCount();
		int rowsPerPage=15;
		PageUtil pageUtil = new PageUtil(count,rowsPerPage,pageNo,"admin/blog/qaExpertList.xhtml");
		pageUtil.initPageInfo();
		List<GewaQaExpert> qaExpertList = qaService.getQaExpertList();
		Map<Long,Member> lmMap = new HashMap<Long,Member>();
		for(GewaQaExpert qaexpert:qaExpertList){
			Member m = daoService.getObject(Member.class, qaexpert.getMemberid());
			lmMap.put(qaexpert.getId(), m);
		}
		model.put("lmMap", lmMap);
		model.put("qaExpertList",qaExpertList);
		model.put("pageUtil", pageUtil);
		return "admin/blog/qa/expertList.vm";
	}
	//他的答案信息
	@RequestMapping("/admin/blog/answerDetail.xhtml")
	public String AnswerDetail(@RequestParam("memberid")Long memberid,ModelMap model){
		List<GewaAnswer> answerList = qaService.getAnswerByMemberId(memberid);
		//他的答案信息
		Map<GewaAnswer,GewaQuestion> ggMap = new LinkedHashMap();
		Map<Long,Member> lmMap = new HashMap<Long,Member>();
		for (GewaAnswer answer:answerList){
			GewaQuestion question = daoService.getObject(GewaQuestion.class, answer.getQuestionid());
			ggMap.put(answer, question);
			Member m = daoService.getObject(Member.class, question.getMemberid());
			lmMap.put(answer.getId(), m);
		}
		model.put("lmMap", lmMap);
		model.put("ggMap", ggMap);
		return "admin/blog/qa/memberAnswerList.vm";
	}
	//他的最佳答案
	@RequestMapping("/admin/blog/bestAnswer.xhtml")
	public String BestAnswer(@RequestParam("memberid") Long memberid,ModelMap model){
		DetachedCriteria query = DetachedCriteria.forClass(GewaAnswer.class);
		query.add(Restrictions.eq("memberid", memberid));
		query.add(Restrictions.eq("answerstatus", GewaAnswer.AS_STATUS_Y));
		List<GewaAnswer> answerList = readOnlyTemplate.findByCriteria(query);
		Map<Long,Member> lmMap = new HashMap<Long,Member>();
		for(GewaAnswer answer:answerList){
			Member m = daoService.getObject(Member.class, answer.getMemberid());
			lmMap.put(answer.getId(), m);
		}
		model.put("lmMap", lmMap);
		model.put("answerList", answerList);
		return "admin/blog/qa/bestAnswer.vm";
	}
	//用户回答问题多少排行
	@RequestMapping("/admin/blog/topMemberListByAnswer.xhtml")
	public String topMemberListByAnswer(@RequestParam(required = false,value="pageNo") Integer pageNo,ModelMap model){
		if (pageNo == null) pageNo=0;
		Integer count = qaService.getTopMemberCountByAnswer();
		int rowsPerPage=15;
		PageUtil pageUtil = new PageUtil(count,rowsPerPage,pageNo,"admin/blog/topMemberListByAnswer.xhtml");
		pageUtil.initPageInfo();
		Map<Member,Integer> miMap = qaService.getTopMemberListByAnswer(pageNo*rowsPerPage, rowsPerPage);
		model.put("pageUtil", pageUtil);
		model.put("miMap", miMap);
		return "admin/blog/qa/memberListByAnswer.vm";
	}
	//用户正确答案多少排行
	@RequestMapping("/admin/blog/topMemberListByBestAnswer.xhtml")
	public String topMemberListByBestAnswer(@RequestParam(required = false,value="pageNo") Integer pageNo,ModelMap model){
		if (pageNo == null) pageNo=0;
		Integer count = qaService.getTopMemberCountByBestAnswer();
		int rowsPerPage=15;
		PageUtil pageUtil = new PageUtil(count,rowsPerPage,pageNo,"admin/blog/topMemberListByBestAnswer.xhtml");
		pageUtil.initPageInfo();
		Map<Member,Integer> miMap = qaService.getTopMemberListByBestAnswer(pageNo*rowsPerPage, rowsPerPage);
		model.put("pageUtil", pageUtil);
		model.put("miMap", miMap);
		return "admin/blog/qa/memberListByBestAnswer.vm";
	}
	//用户经验值多少排行
	@RequestMapping("/admin/blog/topMemberListByPoint.xhtml")
	public String topMemberListByPonit(@RequestParam(required = false,value="pageNo") Integer pageNo,ModelMap model){
		if (pageNo == null) pageNo=0;
		Integer count = qaService.getTopMemberCountByPoint();
		int rowsPerPage=15;
		PageUtil pageUtil = new PageUtil(count,rowsPerPage,pageNo,"admin/blog/topMemberListByPoint.xhtml");
		pageUtil.initPageInfo();
		List<Map> miMap = qaService.getTopMemberListByPoint(pageNo*rowsPerPage, rowsPerPage);
		model.put("pageUtil", pageUtil);
		model.put("miMap", miMap);
		return "admin/blog/qa/memberListByPoint.vm";
	}
	//专题
	@RequestMapping("/admin/blog/commendpic.xhtml")
	public String indexSpecial(ModelMap model) {
		List<GewaCommend> giList1 = daoService.getObjectListByField(GewaCommend.class, "signname", SignName.TAG_QACOMMENDPIC);
		Collections.sort(giList1, new MultiPropertyComparator(new String[]{"addtime"}, new boolean[]{false}));
		model.put("giList1", giList1);
		return "admin/blog/qa/commendpic.vm";
	}
	@RequestMapping("/admin/blog/report/qa.xhtml")
	public String getQaReportLst(ModelMap model, Date datefrom, Date dateto){
		if(datefrom == null || dateto == null) return "admin/blog/qa/qaReportList.vm";
		String answerhql ="select count(t.id) from GewaAnswer t where t.addtime>=? and t.addtime<=? and t.status like ?";
		String countAnswerhql ="select count(distinct t.memberid) from GewaAnswer t where t.addtime>=? and t.addtime<=? and t.status like ?";
		String questionhql ="select count(t.id) from GewaQuestion t where t.addtime>=? and t.addtime<=? and t.status like ?";
		String countQuestionhql ="select count(distinct t.memberid) from GewaQuestion t where t.addtime>=? and t.addtime<=? and t.status like ?";
		String questionDelhql ="select count(t.id) from GewaAnswer t where t.addtime>=? and t.addtime<=? and t.status like ?";
		String countQuestionDelhql ="select count(t.id) from GewaQuestion t where t.addtime>=? and t.addtime<=? and t.status like ?";
		List answerList = readOnlyTemplate.find(answerhql, DateUtil.getBeginningTimeOfDay(datefrom), DateUtil.getLastTimeOfDay(dateto), "Y%");
		List answerMemberList = readOnlyTemplate.find(countAnswerhql, DateUtil.getBeginningTimeOfDay(datefrom), DateUtil.getLastTimeOfDay(dateto), "Y%");
		List questionList = readOnlyTemplate.find(questionhql, DateUtil.getBeginningTimeOfDay(datefrom), DateUtil.getLastTimeOfDay(dateto), "Y%");
		List questionMemberList = readOnlyTemplate.find(countQuestionhql, DateUtil.getBeginningTimeOfDay(datefrom), DateUtil.getLastTimeOfDay(dateto), "Y%");
		List answerDelList = readOnlyTemplate.find(questionDelhql, DateUtil.getBeginningTimeOfDay(datefrom), DateUtil.getLastTimeOfDay(dateto), "N%");
		List questionDelList = readOnlyTemplate.find(countQuestionDelhql, DateUtil.getBeginningTimeOfDay(datefrom), DateUtil.getLastTimeOfDay(dateto), "N%");
		model.put("answer", answerList.get(0));
		model.put("answerMember", answerMemberList.get(0));
		model.put("question", questionList.get(0));
		model.put("questionMember", questionMemberList.get(0));
		model.put("answerDel", answerDelList.get(0));
		model.put("questionDel", questionDelList.get(0));
		return "admin/blog/report/qa.vm";
	}
	//热门数据
	@RequestMapping("/admin/blog/qaHotDataList.xhtml")
	public String hotDataList(HttpServletRequest request, ModelMap model, Integer pageNo, Integer hotvalue){
		if(hotvalue==null) hotvalue = GewaQuestion.HOTVALUE_HOT;
		if (pageNo == null) pageNo=0;
		String citycode = getAdminCitycode(request);
		Integer count = qaService.getQuestionCountByHotvalue(citycode, hotvalue);
		Integer rowsPerPage = 20;
		Integer firstRows = pageNo*rowsPerPage;
		List<GewaQuestion> qaList=qaService.getQuestionListByHotvalue(citycode, hotvalue, firstRows, rowsPerPage);
		PageUtil pageUtil = new PageUtil(count,rowsPerPage,pageNo,"admin/blog/qaHotDataList.xhtml");
		Map params = new HashMap();
		params.put("hotvalue", hotvalue);
		pageUtil.initPageInfo(params);
		model.put("qaList", qaList);
		model.put("pageUtil", pageUtil);
		return "admin/blog/qa/qaHotDataList.vm";
	}
}