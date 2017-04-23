package com.gewara.web.action.admin.blog;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

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

import com.gewara.Config;
import com.gewara.constant.PointConstant;
import com.gewara.constant.SmsConstant;
import com.gewara.constant.Status;
import com.gewara.constant.SysAction;
import com.gewara.constant.TagConstant;
import com.gewara.constant.sys.ConfigConstant;
import com.gewara.constant.sys.ConfigTag;
import com.gewara.constant.sys.MongoData;
import com.gewara.model.acl.User;
import com.gewara.model.bbs.Accusation;
import com.gewara.model.bbs.Diary;
import com.gewara.model.bbs.DiaryBase;
import com.gewara.model.bbs.DiaryComment;
import com.gewara.model.bbs.commu.Commu;
import com.gewara.model.bbs.qa.GewaAnswer;
import com.gewara.model.bbs.qa.GewaQuestion;
import com.gewara.model.common.GewaConfig;
import com.gewara.model.drama.Drama;
import com.gewara.model.movie.Movie;
import com.gewara.model.pay.SMSRecord;
import com.gewara.model.user.Member;
import com.gewara.model.user.MemberPicture;
import com.gewara.model.user.SysMessageAction;
import com.gewara.model.user.UserMessageAction;
import com.gewara.mongo.MongoService;
import com.gewara.service.bbs.BlogService;
import com.gewara.service.bbs.DiaryService;
import com.gewara.service.bbs.QaService;
import com.gewara.service.content.PictureService;
import com.gewara.service.member.PointService;
import com.gewara.support.ServiceHelper;
import com.gewara.untrans.CommentService;
import com.gewara.untrans.SearchService;
import com.gewara.untrans.WalaApiService;
import com.gewara.untrans.monitor.ConfigCenter;
import com.gewara.util.BeanUtil;
import com.gewara.util.DateUtil;
import com.gewara.util.RelatedHelper;
import com.gewara.util.StringUtil;
import com.gewara.web.action.admin.BaseAdminController;
import com.gewara.web.util.PageUtil;
import com.gewara.xmlbind.bbs.Comment;
import com.gewara.xmlbind.bbs.ReComment;

@Controller
public class AuditAdminController extends BaseAdminController {
	
	public static final Map<String, Boolean> TAG_MAP = new HashMap<String, Boolean>();
	static{
		TAG_MAP.put("movie", true);
		TAG_MAP.put("drama", true);
		TAG_MAP.put("gymcourse", true);
		TAG_MAP.put("sportservice", true);
	}
	public static String PUNCTUATION = ";:'<>?/.!@#$%^&*()_+=-\\|[]{}~·";
	@Autowired@Qualifier("configCenter")
	private ConfigCenter configCenter;
	@Autowired@Qualifier("searchService")
	private SearchService searchService;
	@Autowired@Qualifier("blogService")
	private BlogService blogService;
	public void setBlogService(BlogService blogService) {
		this.blogService = blogService;
	}
	@Autowired@Qualifier("commentService")
	private CommentService commentService;
	@Autowired@Qualifier("walaApiService")
	private WalaApiService walaApiService;
	@Autowired@Qualifier("pictureService")
	private PictureService pictureService;
	public void setPictureService(PictureService pictureService) {
		this.pictureService = pictureService;
	}
	@Autowired@Qualifier("diaryService")
	private DiaryService diaryService;
	public void setDiaryService(DiaryService diaryService) {
		this.diaryService = diaryService;
	}
	@Autowired@Qualifier("mongoService")
	private MongoService mongoService;
	public void setMongoService(MongoService mongoService) {
		this.mongoService = mongoService;
	}
	@Autowired@Qualifier("pointService")
	private PointService pointService = null;

	private static final String TYPE_MICRO = "microcomment";//哇啦
	private static final String TYPE_COMMENT = "comment";//其他点评
	private static final String TYPE_MICRORECOMMENT = "microrecomment";//哇啦回复
	
	// 夜间圈子审核
	@RequestMapping("/admin/audit/nightCommuList.xhtml")
	public String nightCommuList(Timestamp starttime, Timestamp endtime, Integer pageNo, ModelMap model) {
		List<Commu> commuList = new ArrayList<Commu>();
		if (pageNo == null)
			pageNo = 0;
		int rowsPerpage = 30;
		int firstRow = pageNo * rowsPerpage;
		DetachedCriteria query = queryCommu(starttime, endtime).setProjection(Projections.rowCount());
		int count = Integer.valueOf(hibernateTemplate.findByCriteria(query).get(0)+"");
		commuList = hibernateTemplate.findByCriteria(queryCommu(starttime, endtime),firstRow,rowsPerpage);
		Map<Long,Member> memberMap=new HashMap<Long,Member>();
		for (Commu commu : commuList) {
			memberMap.put(commu.getAdminid(), daoService.getObject(Member.class,commu.getAdminid()));
		}
		Map params = new HashMap();
		if(starttime != null) params.put("starttime", starttime);
		if(endtime != null) params.put("endtime", endtime);
		PageUtil pageUtil = new PageUtil(count, rowsPerpage, pageNo, "admin/audit/nightCommuList.xhtml");
		pageUtil.initPageInfo(params);
		model.put("pageUtil", pageUtil);
		model.put("commuList", commuList);
		model.put("memberMap", memberMap);
		return "admin/audit/nightCommuList.vm";
	}
	
	// 夜间帖子审核
	@RequestMapping("/admin/audit/nightdiaryList.xhtml")
	public String nightdiaryList(Long memberid, Timestamp starttime, Timestamp endtime, String tag, String status, String keyname, Integer pageNo, ModelMap model) {
		List<Diary> diaryList = new ArrayList<Diary>();
		if (pageNo == null)
			pageNo = 0;
		int rowsPerpage = 30;
		int firstRow = pageNo * rowsPerpage;
		if(StringUtils.isBlank(status)) status = Status.N_NIGHT;
		DetachedCriteria query = queryDiary(memberid, starttime, endtime, tag, status, keyname).setProjection(Projections.rowCount());
		int count = Integer.valueOf(hibernateTemplate.findByCriteria(query).get(0)+"");
		diaryList = hibernateTemplate.findByCriteria(queryDiary(memberid, starttime, endtime, tag, status, keyname),firstRow,rowsPerpage);
		Map<Long, Commu> commuMap = new HashMap<Long, Commu>();
		for (Diary diary : diaryList) {
			if(diary.getCommunityid()!=0)
				commuMap.put(diary.getId(), daoService.getObject(Commu.class, diary.getCommunityid()));
		}
		Map params = new HashMap();
		if(memberid != null) params.put("memberid", memberid);
		if(starttime != null) params.put("starttime", starttime);
		if(endtime != null) params.put("endtime", endtime);
		if(StringUtils.isNotBlank(tag)) params.put("tag", tag);
		if(StringUtils.isNotBlank(status)) params.put("status", status);
		if (StringUtils.isNotBlank(keyname)) params.put("keyname", keyname);
		
		PageUtil pageUtil = new PageUtil(count, rowsPerpage, pageNo, "admin/audit/nightdiaryList.xhtml");
		pageUtil.initPageInfo(params);
		model.put("pageUtil", pageUtil);
		model.put("diaryList", diaryList);
		model.put("commuMap", commuMap);
		return "admin/audit/nightdiaryList.vm";
	}
	/**
	 * 设置夜间模式开始及结束时间
	 * 设置发贴频率及该频率下的发贴次数
	 * 
	 * @author liuyunxin
	 * @date 2013/01/11
	 * 
	 * */
	@RequestMapping("/admin/audit/modifyNightTime.xhtml")
	public String modifyNightTime(String nightStart,String nightEnd,String frequency,String times,ModelMap model){
		StringBuffer content = new StringBuffer("{\"nightStart\":");
		if(StringUtils.isNotBlank(nightStart)){
			content.append(nightStart);
		}else{
			content.append(23);
		}
		content.append(",\"nightEnd\":");
		if(StringUtils.isNotBlank(nightEnd)){
			content.append(nightEnd);
		}else{
			content.append(9);
		}
		content.append(",\"frequency\":");
		if(StringUtils.isNotBlank(frequency)){
			content.append(frequency);
		}else{
			content.append(1);
		}
		content.append(",\"times\":");
		if(StringUtils.isNotBlank(times)){
			content.append(times);
		}else{
			content.append(30);
		}
		content.append("}");
		GewaConfig cfg = daoService.getObject(GewaConfig.class, ConfigConstant.CFG_BBS_TIME);
		if(cfg==null){
			return showJsonError(model,"gewaConfig中不存在ID为:"+ConfigConstant.CFG_BBS_TIME+"的相关对象，请联系技术相关人员！");
		}
		cfg.setContent(content.toString());
		daoService.saveObject(cfg);
		return showJsonSuccess(model);
	}
	
	// 帖子审核
	@RequestMapping("/admin/audit/diaryList.xhtml")
	public String diaryList(Long memberid, Timestamp starttime, Timestamp endtime, String tag, String status, String keyname, Integer pageNo, ModelMap model) {
		List<Diary> diaryList = new ArrayList<Diary>();
		if (pageNo == null)
			pageNo = 0;
		int rowsPerpage = 30;
		int firstRow = pageNo * rowsPerpage;
		DetachedCriteria query = queryDiary(memberid, starttime, endtime, tag, status, keyname).setProjection(Projections.rowCount());
		int count = Integer.valueOf(hibernateTemplate.findByCriteria(query).get(0)+"");
		diaryList = hibernateTemplate.findByCriteria(queryDiary(memberid, starttime, endtime, tag, status, keyname),firstRow,rowsPerpage);
		Map<Long, Commu> commuMap = new HashMap<Long, Commu>();
		for (Diary diary : diaryList) {
			if(diary.getCommunityid()!=0)
				commuMap.put(diary.getId(), daoService.getObject(Commu.class, diary.getCommunityid()));
		}
		Map params = new HashMap();
		if(memberid != null) params.put("memberid", memberid);
		if(starttime != null) params.put("starttime", starttime);
		if(endtime != null) params.put("endtime", endtime);
		if(StringUtils.isNotBlank(tag)) params.put("tag", tag);
		if(StringUtils.isNotBlank(status)) params.put("status", status);
		if (StringUtils.isNotBlank(keyname)) params.put("keyname", keyname);
		PageUtil pageUtil = new PageUtil(count, rowsPerpage, pageNo, "admin/audit/diaryList.xhtml");
		pageUtil.initPageInfo(params);
		model.put("pageUtil", pageUtil);
		model.put("diaryList", diaryList);
		model.put("commuMap", commuMap);
		return "admin/audit/diaryList.vm";
	}
	
	@RequestMapping("/admin/audit/diaryDetail.xhtml")
	public String diaryDetail(ModelMap model, Long diaryid){
		DiaryBase diary=diaryService.getDiaryBase(diaryid);
		if(diary == null){
			return show404(model, "参数错误！");
		}
		String body = blogService.getDiaryBody(diary.getId());
		model.put("diary", diary);
		model.put("diaryBody", body);
		return "admin/audit/auditDiaryDetail.vm";
	}
	
	@RequestMapping("/admin/audit/commuDetail.xhtml")
	public String commuDetail(ModelMap model, Long id){
		Commu commu = daoService.getObject(Commu.class, id);
		if(commu == null){
			return show404(model, "参数错误！");
		}
		Member mbr = daoService.getObject(Member.class, commu.getAdminid());
		model.put("commu", commu);
		if (mbr != null) {
			model.put("adminname", mbr.getNickname());
		}
		return "admin/audit/auditCommuDetail.vm";
	}
	
	private DetachedCriteria queryCommu(Timestamp starttime, Timestamp endtime){
		DetachedCriteria query = DetachedCriteria.forClass(Commu.class);
		if(starttime != null) query.add(Restrictions.ge("starttime", starttime));
		if(endtime != null) query.add(Restrictions.le("endtime", endtime));
		query.add(Restrictions.eq("status", Status.N_NIGHT));
		query.addOrder(Order.desc("updatetime"));
		return query;
	}
	private DetachedCriteria queryDiary(Long memberid, Timestamp starttime, Timestamp endtime, String tag, String status, String keyname){
		DetachedCriteria query = DetachedCriteria.forClass(Diary.class);
		if(memberid != null) query.add(Restrictions.eq("memberid", memberid));
		if(starttime != null) query.add(Restrictions.ge("starttime", starttime));
		if(endtime != null) query.add(Restrictions.le("endtime", endtime));
		if(!StringUtils.equals(status, Status.N_DELETE)){
			query.add(Restrictions.ne("status", Status.N_DELETE));
			if(StringUtils.isNotBlank(status))
				query.add(Restrictions.like("status", status, MatchMode.START));
		}else query.add(Restrictions.eq("status", Status.N_DELETE));
		if(StringUtils.isNotBlank(tag)){
			if(TAG_MAP.get(tag)!=null && TAG_MAP.get(tag)){
				query.add(Restrictions.eq("category", tag));
			}else if(StringUtils.equals(tag, "commu")){
				query.add(Restrictions.ne("communityid", 0L));
			}else query.add(Restrictions.eq("tag", tag));
		}
		if(StringUtils.isNotBlank(keyname))
			query.add(Restrictions.ilike("subject", keyname, MatchMode.ANYWHERE));
		query.addOrder(Order.desc("updatetime"));
		return query;
	}
	// 帖子回复审核
	@RequestMapping("/admin/audit/diaryCommentList.xhtml")
	public String diaryCommentList(Long did, Long memberid, Timestamp starttime, Timestamp endtime, String status, String keyname,Integer pageNo, ModelMap model) {
		List<DiaryComment> dcList = new ArrayList<DiaryComment>();
		if (pageNo == null)
			pageNo = 0;
		int rowsPerpage = 30;
		int firstRow = pageNo * rowsPerpage;
		DetachedCriteria query = queryDiaryComment(did, memberid, starttime, endtime, status, keyname).setProjection(Projections.rowCount());
		Integer count = Integer.parseInt(hibernateTemplate.findByCriteria(query).get(0)+"");
		dcList = hibernateTemplate.findByCriteria(queryDiaryComment(did, memberid, starttime, endtime,status, keyname),firstRow,rowsPerpage);
		addCacheMember(model, ServiceHelper.getMemberIdListFromBeanList(dcList));
		Map<Long, DiaryBase> diaryMap=new HashMap<Long, DiaryBase>();
		for(DiaryComment diaryComment: dcList){
			diaryMap.put(diaryComment.getId(), diaryService.getDiaryBase(diaryComment.getDiaryid()));
		}
		Map params = new HashMap();
		if(did!=null) params.put("did", did);
		if(memberid != null) params.put("memberid", memberid);
		if(starttime != null) params.put("starttime", starttime);
		if(endtime != null) params.put("endtime", endtime);
		if(StringUtils.isNotBlank(status)) params.put("status", status);
		if (StringUtils.isNotBlank(keyname)) params.put("keyname", keyname);
		PageUtil pageUtil = new PageUtil(count, rowsPerpage, pageNo, "admin/audit/diaryCommentList.xhtml");
		pageUtil.initPageInfo(params);
		model.put("diaryMap", diaryMap);
		model.put("pageUtil", pageUtil);
		model.put("dcList", dcList);
		return "admin/audit/diaryCommentList.vm";
	}
	@RequestMapping("/admin/audit/diaryCommentDetail.xhtml")
	public String diaryCommentDetail(Long cid, ModelMap model){
		DiaryComment diaryComment = daoService.getObject(DiaryComment.class, cid);
		if(diaryComment == null) return show404(model, "该贴子回复不存在或被删除！");
		Member member = daoService.getObject(Member.class, diaryComment.getMemberid());
		model.put("diaryComment", diaryComment);
		model.put("member", member);
		return "admin/audit/auditDiaryCommentDetail.vm";
	}
	private DetachedCriteria queryDiaryComment(Long did, Long memberid, Timestamp starttime, Timestamp endtime, String status, String keyname){
		DetachedCriteria query = DetachedCriteria.forClass(DiaryComment.class);
		if(did!=null){
			query.add(Restrictions.eq("diaryid", did));
		}
		if(memberid != null) query.add(Restrictions.eq("memberid", memberid));
		if(starttime != null) query.add(Restrictions.ge("addtime", starttime));
		if(endtime != null) query.add(Restrictions.le("addtime", endtime));
		if(!StringUtils.equals(status, Status.N_DELETE)){
			query.add(Restrictions.ne("status", Status.N_DELETE));
			if(StringUtils.isNotBlank(status))
				query.add(Restrictions.like("status", status, MatchMode.START));
		}else query.add(Restrictions.eq("status", Status.N_DELETE));
		if(StringUtils.isNotBlank(keyname))
			query.add(Restrictions.ilike("body", keyname, MatchMode.ANYWHERE));
		query.addOrder(Order.desc("addtime"));
		query.addOrder(Order.desc("id"));
		return query;
	}
	
	
	// 点评审核
	@RequestMapping("/admin/audit/commentList.xhtml")
	public String commentList(Long memberid, Timestamp starttime, Timestamp endtime, String status, String keyname, Integer pageNo, ModelMap model) {
		List<Comment> commentList = new ArrayList<Comment>();
		if (pageNo == null)
			pageNo = 0;
		int rowsPerpage = 30;
		int firstRow = pageNo * rowsPerpage;
		Date date = DateUtil.currentTime();
		if(starttime == null) starttime = DateUtil.getBeginTimestamp(date);
		if(endtime == null) endtime = DateUtil.getEndTimestamp(date);
		commentList = commentService.getCommentList(memberid, starttime, endtime, null, status, keyname, "false", firstRow, rowsPerpage);
		int count = commentService.getCommentCount(memberid, starttime, endtime, null, status, keyname, "false");
		Map params = new HashMap();
		if(memberid !=null) params.put("memberid", memberid);
		if(starttime !=null) params.put("starttime", starttime);
		if(endtime !=null) params.put("endtime", endtime);
		if(StringUtils.isNotBlank(status)) params.put("status", status);
		if (StringUtils.isNotBlank(keyname)) params.put("keyname", keyname);
		PageUtil pageUtil = new PageUtil(count, rowsPerpage, pageNo, "admin/audit/commentList.xhtml");
		pageUtil.initPageInfo(params);
		model.put("pageUtil", pageUtil);
		model.put("commentList", commentList);
		Map<Long,Member> lmMap = new HashMap<Long,Member>();
		model.put("lmMap", lmMap);
		model.put("commentList", commentList);
		model.put("tmp", TYPE_COMMENT);
		model.put("starttime", starttime);
		model.put("endtime", endtime);
		addCacheMember(model, ServiceHelper.getMemberIdListFromBeanList(commentList));
		return "admin/audit/commentList.vm";
	}
	@RequestMapping("/admin/audit/commentDetail.xhtml")
	public String commentDetail(Long cid, ModelMap model){
		Comment comment = this.commentService.getCommentById(cid);
		if(comment == null) return show404(model, "该点评不存在或被删除！");
		model.put("comment", comment);
		return "admin/audit/auditCommentDetail.vm";
	}
	//夜间知道审核
	@RequestMapping("/admin/audit/nightqaList.xhtml")
	public String nightQaList(ModelMap model, Long memberid, Timestamp starttime, Timestamp endtime, String status, String keyname,Integer pageNo){
		if(pageNo == null) pageNo = 0;
		if(StringUtils.isBlank(status)) status = Status.N_NIGHT;
		Integer rowsPerPage = 30;
		Integer count = Integer.valueOf(hibernateTemplate.findByCriteria(
				this.getQuestionQuery(memberid, starttime, endtime, status, keyname).setProjection(Projections.rowCount())).get(0) + "");
		PageUtil pageUtil = new PageUtil(count, rowsPerPage, pageNo, "admin/audit/nightqaList.xhtml");
		Map params = new HashMap();
		if(memberid !=null) params.put("memberid", memberid);
		if(starttime !=null) params.put("starttime", starttime);
		if(endtime !=null) params.put("endtime", endtime);
		if(StringUtils.isNotBlank(status)) params.put("status", status);
		if (StringUtils.isNotBlank(keyname)) params.put("keyname", keyname);
		pageUtil.initPageInfo(params);
		List<GewaQuestion> questionList = hibernateTemplate.findByCriteria(this.getQuestionQuery(memberid, starttime, endtime, status, keyname), pageNo
				* rowsPerPage, rowsPerPage);
		model.put("pageUtil", pageUtil);
		model.put("questionList", questionList);
		return "admin/audit/nightQaList.vm";
	}
	//知道:提问审核
	@RequestMapping("/admin/audit/qaList.xhtml")
	public String qaList(ModelMap model,Long memberid, Timestamp starttime, Timestamp endtime, String status, String keyname, Integer pageNo) throws Exception {
		if (pageNo == null)
			pageNo = 0;
		Integer rowsPerPage = 30;
		Integer count = Integer.valueOf(hibernateTemplate.findByCriteria(
				this.getQuestionQuery(memberid, starttime, endtime, status, keyname).setProjection(Projections.rowCount())).get(0)+"");
		PageUtil pageUtil = new PageUtil(count, rowsPerPage, pageNo, "admin/audit/qaList.xhtml");
		Map params = new HashMap();
		if(memberid !=null) params.put("memberid", memberid);
		if(starttime !=null) params.put("starttime", starttime);
		if(endtime !=null) params.put("endtime", endtime);
		if(StringUtils.isNotBlank(status)) params.put("status", status);
		if (StringUtils.isNotBlank(keyname)) params.put("keyname", keyname);
		pageUtil.initPageInfo(params);
		List<GewaQuestion> questionList = hibernateTemplate.findByCriteria(this.getQuestionQuery(memberid, starttime, endtime, status, keyname), pageNo
				* rowsPerPage, rowsPerPage);
		model.put("pageUtil", pageUtil);
		model.put("questionList", questionList);
		return "admin/audit/qaList.vm";
	}
	@RequestMapping("/admin/audit/qaDetail.xhtml")
	public String qaDetail(Long qid, ModelMap model){
		GewaQuestion question = daoService.getObject(GewaQuestion.class, qid);
		if(question == null) return show404(model, "该知道不存在或被删除！");
		model.put("question", question);
		return "admin/audit/auditQaDetail.vm";
	}
	private DetachedCriteria getQuestionQuery(Long memberid, Timestamp starttime, Timestamp endtime, String status, String keyname) {
		DetachedCriteria query = DetachedCriteria.forClass(GewaQuestion.class);
		if(!StringUtils.equals(status, Status.N_DELETE)){
			query.add(Restrictions.ne("status", Status.N_DELETE));
			if(StringUtils.isNotBlank(status))
				query.add(Restrictions.like("status", status, MatchMode.START));
		}else {
			query.add(Restrictions.eq("status", Status.N_DELETE));
		}
		if(memberid != null) query.add(Restrictions.eq("memberid", memberid));
		if(starttime !=null) query.add(Restrictions.ge("addtime", starttime));
		if(endtime !=null) query.add(Restrictions.le("addtime", endtime));
		if (StringUtils.isNotBlank(keyname)){
			query.add(Restrictions.ilike("title", keyname, MatchMode.ANYWHERE));
		}
		query.addOrder(Order.desc("modtime"));
		return query;
	}
	//知道:回答审核
	@RequestMapping("/admin/audit/answerList.xhtml")
	public String answerList (Long qid, Long memberid, Timestamp starttime, Timestamp endtime, String status, String keyname, Integer pageNo, ModelMap model){
		List<GewaAnswer> answerList = new ArrayList<GewaAnswer>();
		if (pageNo == null)
			pageNo = 0;
		int rowsPerPage = 30;
		int firstRow = pageNo * rowsPerPage;
		DetachedCriteria query = getAnswerQuery(qid, memberid, starttime, endtime, status, keyname).setProjection(Projections.rowCount());
		Integer count = Integer.parseInt(hibernateTemplate.findByCriteria(query).get(0)+"");
		answerList = hibernateTemplate.findByCriteria(getAnswerQuery(qid,  memberid, starttime, endtime, status, keyname),firstRow,rowsPerPage);
		PageUtil pageUtil = new PageUtil(count, rowsPerPage, pageNo, "admin/audit/answerList.xhtml");
		RelatedHelper rh = new RelatedHelper();
		model.put("relatedHelper", rh);
		Map params = new HashMap();
		if(qid != null) params.put("qid", qid);
		if(StringUtils.isNotBlank(status)) params.put("status", status);
		if (StringUtils.isNotBlank(keyname)) params.put("keyname", keyname);
		pageUtil.initPageInfo(params);
		model.put("pageUtil", pageUtil);
		model.put("answerList",answerList);
		List<Serializable> questionIdList = BeanUtil.getBeanPropertyList(answerList, Serializable.class, "questionid", true);
		relateService.addRelatedObject(1, "questionIdList", rh, TagConstant.TAG_QUESTION, questionIdList);
		return "admin/audit/answerList.vm";
	}
	@RequestMapping("/admin/audit/answerDetail.xhtml")
	public String answerDetail(Long aid, ModelMap model){
		GewaAnswer gewaAnswer = daoService.getObject(GewaAnswer.class, aid);
		if(gewaAnswer == null) return show404(model, "该回答不存在或被删除！");
		model.put("answer", gewaAnswer);
		model.put("member", daoService.getObject(Member.class, gewaAnswer.getMemberid()));
		return "admin/audit/auditAnswerDetail.vm";
	}
	private DetachedCriteria getAnswerQuery(Long qid, Long memberid, Timestamp starttime, Timestamp endtime, String status, String keyname) {
		DetachedCriteria query = DetachedCriteria.forClass(GewaAnswer.class);
		if(!StringUtils.equals(status, Status.N_DELETE)){
			query.add(Restrictions.ne("status", Status.N_DELETE));
			if(StringUtils.isNotBlank(status))
				query.add(Restrictions.like("status", status, MatchMode.START));
		}else query.add(Restrictions.eq("status", Status.N_DELETE));
		if(memberid != null) query.add(Restrictions.eq("memberid", memberid));
		if(starttime !=null) query.add(Restrictions.ge("addtime", starttime));
		if(endtime !=null) query.add(Restrictions.le("addtime", endtime));
		if(qid!=null){
			query.add(Restrictions.eq("questionid", qid));
		}
		if (StringUtils.isNotBlank(keyname)){
			query.add(Restrictions.ilike("content", keyname, MatchMode.ANYWHERE));
		}
		query.addOrder(Order.desc("addtime"));
		return query;
	}
	@RequestMapping("/admin/audit/addFilter.xhtml")
	public String addFilter(String keyword, Integer pageNo, String keytype, ModelMap model){
		if(!StringUtils.equals(keytype, ConfigConstant.KEY_MANUKEYWORDS) && 
			 !StringUtils.equals(keytype, ConfigConstant.KEY_MEMBERKEYWORDS)) return showError(model, "类型错误！！！");
		if(pageNo==null) pageNo = 0;
		String keywordsStr = (String)mongoService.getPrimitiveObject(keytype);
		String[] keywords = keywordsStr.split("\\|");
		List<String> kwList = new ArrayList();
		if(StringUtils.isNotBlank(keyword)) {
			for(String str : keywords){
				if(str.indexOf(keyword)>=0) {
					kwList.add(str);
				}
			}
		}else  {
			kwList = Arrays.asList(keywords);
		}
		Integer rowpage = 60;
		PageUtil pageUtil = new PageUtil(kwList.size(), rowpage, pageNo, "admin/audit/addFilter.xhtml");
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("keytype", keytype);
		pageUtil.initPageInfo(params);
		Integer temp = pageNo*rowpage+rowpage;
		if(pageNo*rowpage+rowpage>kwList.size()) temp = kwList.size();
		model.put("keywordsList", BeanUtil.getSubList(kwList, pageNo*rowpage, temp));
		model.put("pageUtil", pageUtil);
		model.put("keytype", keytype);
		return "admin/audit/addFilter.vm";
	}
	@RequestMapping("/admin/audit/ajax/limitEgg.xhtml")
	public String limitEgg(String type ,Integer egg, ModelMap model){
		if (StringUtils.isBlank(type) || egg == null) {
			return showJsonError(model, "参数错误！");
		}
		if (StringUtils.equals(type, "bind")) {
			type = "BIND_USR_LIMIT_EGG";
		}else if (StringUtils.equals(type, "unbind")) {
			type = "UNBIND_USR_LIMIT_EGG";
		}else {
			return showJsonError(model, "参数错误！");
		}
		Map map = mongoService.findOne(MongoData.NS_BADEGG, MongoData.DEFAULT_ID_NAME, type);
		if (map != null) {
			map.put("val", egg);
		}else if (map == null) {
			map = new HashMap();
			map.put(MongoData.DEFAULT_ID_NAME, type);
			map.put("val", egg);
		}
		mongoService.saveOrUpdateMap(map, MongoData.DEFAULT_ID_NAME, MongoData.NS_BADEGG);
		configCenter.refresh(Config.SYSTEMID, ConfigTag.KEY_ALLFILTERKEYS);
		return showJsonSuccess(model);
	}
	@RequestMapping("/admin/audit/ajax/addFilter.xhtml")
	public String addFilter(String keyword, String keytype, ModelMap model){
		if(!StringUtils.equals(keytype, ConfigConstant.KEY_MANUKEYWORDS) && 
				 !StringUtils.equals(keytype, ConfigConstant.KEY_MEMBERKEYWORDS)) return showJsonError(model, "类型错误！！！");
		String text = StringUtil.getHtmlText(keyword);
		StringBuilder sb = new StringBuilder();
		for(int i=0;i<text.length();i++) {
			if(text.charAt(i) >= 48) sb.append(text.charAt(i));
		}
		text = sb.toString();
		
		String keywordsStr = (String)mongoService.getPrimitiveObject(keytype);
		String[] keywords = keywordsStr.split("\\|");
		List<String> tmpList = new ArrayList(Arrays.asList(keywords));
		List<String> kwList = new ArrayList<String>();
		String[] keywordarray=keyword.split(",");
		List<String> repeat = new ArrayList<String>();
		for(int j=0;j<keywordarray.length;j++){
			String key=keywordarray[j];
			if(StringUtils.isNotBlank(key)){
				if(StringUtils.containsAny(key, PUNCTUATION)) return showJsonError(model, "关键字不能含标点符号！");
				if(!tmpList.contains(key)){
					for(String str : tmpList){
						if(str.indexOf(key)< 0){
							kwList.add(str.trim());
						}
					}
					kwList.add(key);
					tmpList=new ArrayList();
					tmpList.addAll(kwList);
					kwList=new ArrayList();
				} else {
					repeat.add(key);
				}
			}
		}
		if(repeat.size()>0){
			return showJsonError(model, StringUtils.join(repeat, ",") + "已经存在在数据库,其它的关键字添加成功");
		}
		mongoService.savePrimitiveObject(keytype, StringUtils.join(tmpList, "|"));
		dbLogger.warn(getLogonUser().getId() + ":addKeywords" + keyword);
		if(StringUtils.equals(keytype, ConfigConstant.KEY_MANUKEYWORDS)){
			blogService.rebuildManualFilterKey();
		}else{
			blogService.rebuildMemberRegisterFilterKey();
		}
		return showJsonSuccess(model);

	}
	@RequestMapping("/admin/audit/ajax/removeFilter.xhtml")
	public String removeFilter(String keyword, String keytype, ModelMap model){
		if(!StringUtils.equals(keytype, ConfigConstant.KEY_MANUKEYWORDS) && 
				 !StringUtils.equals(keytype, ConfigConstant.KEY_MEMBERKEYWORDS)) return showError(model, "类型错误！！！");
		String keywordsStr = (String)mongoService.getPrimitiveObject(keytype);
		String newcontent = keywordsStr.replaceAll(keyword+"\\|", "");
		newcontent = newcontent.replaceAll("\\|"+keyword, "");
		mongoService.savePrimitiveObject(keytype, newcontent);
		dbLogger.warn(getLogonUser().getId() + ":removeKeywords" + keyword);
		configCenter.refresh(Config.SYSTEMID, ConfigTag.KEY_ALLFILTERKEYS);
		return showJsonSuccess(model);
	}
	
	//手机短信审核列表
	@RequestMapping("/admin/audit/mobileMsgList.xhtml")
	public String mobileMsgList(String status, String keyname, Integer pageNo, ModelMap model){
		List<SMSRecord> smsRecordList = new ArrayList<SMSRecord>();
		if (pageNo == null)
			pageNo = 0;
		int rowsPerpage = 30;
		int firstRow = pageNo * rowsPerpage;
		DetachedCriteria query = getSMSRecordQuery(keyname).setProjection(Projections.rowCount());
		Integer count = Integer.parseInt(hibernateTemplate.findByCriteria(query).get(0)+"");
		smsRecordList = hibernateTemplate.findByCriteria(getSMSRecordQuery(keyname), firstRow, rowsPerpage);
		Map params = new HashMap();
		if(StringUtils.isNotBlank(status)) params.put("status", status);
		if (StringUtils.isNotBlank(keyname)) params.put("keyname", keyname);
		PageUtil pageUtil = new PageUtil(count, rowsPerpage, pageNo, "admin/audit/mobileMsgList.xhtml");
		pageUtil.initPageInfo(params);
		model.put("pageUtil", pageUtil);
		model.put("smsRecordList", smsRecordList);
		return "admin/audit/mobileMsgList.vm";
	}
	private DetachedCriteria getSMSRecordQuery(String keyname){
		DetachedCriteria query = DetachedCriteria.forClass(SMSRecord.class);
		query.add(Restrictions.eq("status", SmsConstant.STATUS_FILTER));
		if(StringUtils.isNotBlank(keyname)){
			query.add(Restrictions.ilike("content", keyname, MatchMode.ANYWHERE));
		}
		query.addOrder(Order.asc("id"));
		return query;
	}
	
	//哇啦审核
	@RequestMapping("/admin/audit/microcommentList.xhtml")
	public String microcommentList(Long memberid, Timestamp starttime, Timestamp endtime, String transfer, String status, String keyname, Integer pageNo, ModelMap model) {
		List<Comment> commentList = new ArrayList<Comment>();
		if (pageNo == null)
			pageNo = 0;
		int rowsPerpage = 30;
		int firstRow = pageNo * rowsPerpage;
		Date date = DateUtil.currentTime();
		if(starttime == null) starttime = DateUtil.getBeginTimestamp(date);
		if(endtime == null) endtime = DateUtil.getEndTimestamp(date);
		commentList = commentService.getCommentList(memberid, starttime, endtime, transfer, status, keyname, "true", firstRow, rowsPerpage);
		int count = commentService.getCommentCount(memberid, starttime, endtime, transfer, status, keyname, "true");
		Map params = new HashMap();
		if(memberid !=null) params.put("memberid", memberid);
		if(starttime !=null) params.put("addtime", starttime);
		if(endtime !=null) params.put("addtime", endtime);
		if(StringUtils.isNotBlank(transfer)) params.put("transfer", transfer);
		if(StringUtils.isNotBlank(status)) params.put("status", status);
		if (StringUtils.isNotBlank(keyname)) params.put("keyname", keyname);
		PageUtil pageUtil = new PageUtil(count, rowsPerpage, pageNo, "admin/audit/microcommentList.xhtml");
		pageUtil.initPageInfo(params);
		model.put("pageUtil", pageUtil);
		model.put("commentList", commentList);
		Map<Long,Member> lmMap = new HashMap<Long,Member>();
		model.put("lmMap", lmMap);
		model.put("commentList", commentList);
		model.put("tmp", TYPE_MICRO);
		addCacheMember(model, ServiceHelper.getMemberIdListFromBeanList(commentList));
		return "admin/audit/commentList.vm";
	}
	
	//哇啦回复
	@RequestMapping("/admin/audit/microrecommentList.xhtml")
	public String microReCommentList(Long cid, Long memberid, Timestamp starttime, Timestamp endtime, String status, String keyname, Integer pageNo, ModelMap model) {
		List<ReComment> recommentList = new ArrayList<ReComment>();
		if (pageNo == null) pageNo = 0;
		int rowsPerpage = 30;
		int firstRow = pageNo * rowsPerpage;
		recommentList= walaApiService.getReCommentList(cid, memberid, starttime, endtime, status, keyname, firstRow, rowsPerpage);
		Integer count = walaApiService.getReCommentCount(cid, memberid, starttime, endtime, status, keyname);
		Map<Long,Comment> commentMap = new HashMap<Long, Comment>();
		for (ReComment reComment : recommentList) {
			Comment comment = commentService.getCommentById(reComment.getRelatedid());
			commentMap.put(reComment.getId(), comment);
			addCacheMember(model, reComment.getMemberid());
		}
		Map params = new HashMap();
		if(cid !=null) params.put("cid", cid);
		if(memberid !=null) params.put("memberid", memberid);
		if(starttime !=null) params.put("addtime", starttime);
		if(endtime !=null) params.put("addtime", endtime);
		if(StringUtils.isNotBlank(status)) params.put("status", status);
		if (StringUtils.isNotBlank(keyname)) params.put("keyname", keyname);
		PageUtil pageUtil = new PageUtil(count, rowsPerpage, pageNo, "admin/audit/microrecommentList.xhtml");
		pageUtil.initPageInfo(params);
		model.put("pageUtil", pageUtil);
		model.put("recommentList", recommentList);
		model.put("commentMap",commentMap);
		model.put("tmp", TYPE_MICRORECOMMENT);
		return "admin/audit/recommentList.vm";
	}
	@Autowired@Qualifier("qaService")
	private QaService qaService;
	public void setQaService(QaService qaService){
		this.qaService = qaService;
	}
	
	@RequestMapping("/admin/audit/exprotDelData.xhtml")
	public String exprotDelData(){
		
		return "/admin/audit/exprotDelData.vm";
	}
	//查询删除的帖子
	@RequestMapping("/admin/audit/getDelDiary.xhtml")
	public String getDelDiary(Date fromDate, Date endDate, Integer pageNo, String isexport, HttpServletResponse response, ModelMap model){
		// excel导出
		if(StringUtils.isNotBlank(isexport)){
			List<Diary> diaryExpList = diaryService.getDiaryListByStatus(Diary.class, "", Status.N_DELETE, fromDate, endDate, -1, -1);
			download("xls", response);
			List<Map> diaryMapExpList = new ArrayList<Map>();
			Map<Long, String> mobileExpMap = new HashMap<Long, String>();
			for(Diary diary : diaryExpList){
				Map diaryMap = new HashMap();
				diaryMap.put("subject", diary.getSubject());
				diaryMap.put("addtime", diary.getAddtime());
				diaryMap.put("nickname", diary.getMembername());
				diaryMap.put("diaryBody", blogService.getDiaryBody(diary.getId()));
				diaryMap.put("ip", diary.getIp());
				diaryMapExpList.add(diaryMap);
				Member member = daoService.getObject(Member.class, diary.getMemberid());
				if(member != null) mobileExpMap.put(diary.getMemberid(), member.getMobile());
			}
			model.put("mobileExpMap", mobileExpMap);
			model.put("diaryMapExpList", diaryMapExpList);
			return "/admin/audit/exportDelDiary.vm";
		}
		if(pageNo == null) pageNo = 0;
		Integer maxNum = 30;
		Integer from = pageNo * maxNum;
		if(fromDate == null)fromDate = getLastMonthFirstDay(new Date());
		if(endDate == null) endDate = DateUtil.getMonthFirstDay(new Date());
		List<Diary> diaryList = diaryService.getDiaryListByStatus(Diary.class, "", Status.N_DELETE, fromDate, endDate, from, maxNum);
		Integer count = diaryService.getDiaryCountByStatus(Diary.class, "", Status.N_DELETE, fromDate, endDate);
		Map params = new HashMap();
		params.put("fromDate", DateUtil.format(fromDate, "yyyy-MM-dd HH:mm:ss"));
		params.put("endDate", DateUtil.format(endDate, "yyyy-MM-dd HH:mm:ss"));
		model.put("fromDate", DateUtil.format(fromDate, "yyyy-MM-dd HH:mm:ss"));
		model.put("endDate", DateUtil.format(endDate, "yyyy-MM-dd HH:mm:ss"));
		PageUtil pageUtil = new PageUtil(count,maxNum,pageNo,"admin/audit/getDelDiary.xhtml");
		pageUtil.initPageInfo(params);
		List<Map> diaryMapList = new ArrayList<Map>();
		Map<Long, String> mobileMap = new HashMap<Long, String>();
		for(Diary diary : diaryList){
			Map diaryMap = new HashMap();
			diaryMap.put("subject", diary.getSubject());
			diaryMap.put("addtime", diary.getAddtime());
			diaryMap.put("nickname", diary.getMembername());
			diaryMap.put("diaryBody", blogService.getDiaryBody(diary.getId()));
			diaryMap.put("ip", diary.getIp());
			diaryMapList.add(diaryMap);
			Member member = daoService.getObject(Member.class, diary.getMemberid());
			if(member != null)mobileMap.put(diary.getMemberid(), member.getMobile());
		}
		model.put("mobileMap", mobileMap);
		model.put("pageUtil", pageUtil);
		model.put("diaryMapList", diaryMapList);
		return "/admin/audit/delDiary.vm";
	}
	
	//查询删除的发布的知道
	@RequestMapping("/admin/audit/getDelQa.xhtml")
	public String getDelQa(Date fromDate, Date endDate, Integer pageNo, String isexport, HttpServletResponse response, ModelMap model){
		// excel导出
		if(StringUtils.isNotBlank(isexport)){
			List<GewaQuestion> questionExpList = qaService.getQuestionListByStatus(Status.N_DELETE, fromDate, endDate, -1, -1);
			List<Long> idExpList = ServiceHelper.getMemberIdListFromBeanList(questionExpList);
			Map<Long, String> mobileExpMap = new HashMap<Long, String>();
			for(Long id : idExpList){
				Member member = daoService.getObject(Member.class, id);
				if(member != null){
					mobileExpMap.put(id, member.getMobile());
				}
			}
			model.put("mobileExpMap", mobileExpMap);
			download("xls", response);
			model.put("questionExpList", questionExpList);
			return "/admin/audit/exportDelQuestion.vm";
		}
		if(pageNo == null)pageNo=0;
		Integer maxNum = 30;
		Integer from = pageNo * maxNum;
		if(fromDate == null)fromDate = getLastMonthFirstDay(new Date());
		if(endDate == null) endDate = DateUtil.getMonthFirstDay(new Date());
		List<GewaQuestion> questionList = qaService.getQuestionListByStatus(Status.N_DELETE, fromDate, endDate, from, maxNum);
		Integer count = qaService.getQuestionCountByStatus(Status.N_DELETE, fromDate, endDate);
		List<Long> idList = ServiceHelper.getMemberIdListFromBeanList(questionList);
		Map<Long, String> mobileMap = new HashMap<Long, String>();
		for(Long id : idList){
			Member member = daoService.getObject(Member.class, id);
			if(member != null){
				mobileMap.put(id, member.getMobile());
			}
		}
		model.put("mobileMap", mobileMap);
		model.put("fromDate", DateUtil.format(fromDate, "yyyy-MM-dd HH:mm:ss"));
		model.put("endDate", DateUtil.format(endDate, "yyyy-MM-dd HH:mm:ss"));
		Map params = new HashMap();
		params.put("fromDate", DateUtil.format(fromDate, "yyyy-MM-dd HH:mm:ss"));
		params.put("endDate", DateUtil.format(endDate, "yyyy-MM-dd HH:mm:ss"));
		PageUtil pageUtil = new PageUtil(count,maxNum,pageNo,"admin/audit/getDelQa.xhtml");
		pageUtil.initPageInfo(params);
		model.put("pageUtil", pageUtil);
		model.put("questionList", questionList);
		return "/admin/audit/delQuestion.vm";
	}
	//删除的哇啦
	@RequestMapping("/admin/audit/getDelWala.xhtml")
	public String getDelWala(Timestamp startTime, Timestamp endTime, Integer pageNo, String isexport, HttpServletResponse response, ModelMap model){
		// excel导出
		if(StringUtils.isNotBlank(isexport)){
			List<Comment> walaExpList = commentService.getCommentList(null, startTime, endTime, null, Status.N_DELETE, null, "true", -1, -1);
			List<Long> idExpList = ServiceHelper.getMemberIdListFromBeanList(walaExpList);
			Map<Long, String> mobileExpMap = new HashMap<Long, String>();
			for(Long id : idExpList){
				Member member = daoService.getObject(Member.class, id);
				if(member != null) mobileExpMap.put(id, member.getMobile());
			}
			model.put("mobileExpMap", mobileExpMap);
			download("xls", response);
			model.put("walaExpList", walaExpList);
			return "/admin/audit/exportDelWala.vm";
		}
		if(pageNo == null)pageNo=0;
		Integer maxNum = 30;
		Integer from = pageNo * maxNum;
		if(startTime == null)startTime = DateUtil.parseTimestamp(DateUtil.format(getLastMonthFirstDay(new Date()),"yyyy-MM-dd HH:mm:ss"));
		if(endTime == null) endTime = DateUtil.parseTimestamp(DateUtil.format(DateUtil.getMonthFirstDay(new Date()),"yyyy-MM-dd HH:mm:ss"));
		List<Comment> walaList = commentService.getCommentList(null, startTime, endTime, null, Status.N_DELETE, null, "true", from, maxNum);
		int count = commentService.getCommentCount(null, startTime, endTime, null, Status.N_DELETE, null, "true");
		List<Long> idList = ServiceHelper.getMemberIdListFromBeanList(walaList);
		Map<Long, String> mobileMap = new HashMap<Long, String>();
		for(Long id : idList){
			Member member = daoService.getObject(Member.class, id);
			if(member != null) mobileMap.put(id, member.getMobile());
		}
		model.put("mobileMap", mobileMap);
		model.put("startTime", DateUtil.format(startTime, "yyyy-MM-dd HH:mm:ss"));
		model.put("endTime", DateUtil.format(endTime, "yyyy-MM-dd HH:mm:ss"));
		Map params = new HashMap();
		params.put("startTime", startTime);
		params.put("endTime", endTime);
		PageUtil pageUtil = new PageUtil(count,maxNum,pageNo,"admin/audit/getDelWala.xhtml");
		pageUtil.initPageInfo(params);
		model.put("pageUtil", pageUtil);
		model.put("walaList", walaList);
		return "/admin/audit/delWala.vm";
	}
	//删除的相关点评
	@RequestMapping("/admin/audit/getDelComment.xhtml")
	public String getDelComment(Timestamp startTime, Timestamp endTime, Integer pageNo, String isexport, HttpServletResponse response, ModelMap model){
		// excel导出
		if(StringUtils.isNotBlank(isexport)){
			List<Comment> commentExpList = commentService.getCommentList(null, startTime, endTime, null, Status.N_DELETE, null, "false", -1, -1);
			List<Long> idExpList = ServiceHelper.getMemberIdListFromBeanList(commentExpList);
			Map<Long, String> mobileExpMap = new HashMap<Long, String>();
			for(Long id : idExpList){
				Member member = daoService.getObject(Member.class, id);
				if(member != null) mobileExpMap.put(id, member.getMobile());
			}
			model.put("mobileExpMap", mobileExpMap);
			download("xls", response);
			model.put("commentExpList", commentExpList);
			return "/admin/audit/exportDelComment.vm";
		}
		if(pageNo == null)pageNo=0;
		Integer maxNum = 30;
		Integer from = pageNo * maxNum;
		if(startTime == null)startTime = DateUtil.parseTimestamp(DateUtil.format(getLastMonthFirstDay(new Date()),"yyyy-MM-dd HH:mm:ss"));
		if(endTime == null) endTime = DateUtil.parseTimestamp(DateUtil.format(DateUtil.getMonthFirstDay(new Date()),"yyyy-MM-dd HH:mm:ss"));
		List<Comment> commentList = commentService.getCommentList(null, startTime, endTime, null, Status.N_DELETE, null, "false", from, maxNum);
		int count = commentService.getCommentCount(null, startTime, endTime, null, Status.N_DELETE, null, "false");
		List<Long> idList = ServiceHelper.getMemberIdListFromBeanList(commentList);
		Map<Long, String> mobileMap = new HashMap<Long, String>();
		for(Long id : idList){
			Member member = daoService.getObject(Member.class, id);
			if(member != null) mobileMap.put(id, member.getMobile());
		}
		model.put("mobileMap", mobileMap);
		model.put("startTime", DateUtil.format(startTime, "yyyy-MM-dd HH:mm:ss"));
		model.put("endTime", DateUtil.format(endTime, "yyyy-MM-dd HH:mm:ss"));
		Map params = new HashMap();
		params.put("startTime", startTime);
		params.put("endTime", endTime);
		PageUtil pageUtil = new PageUtil(count,maxNum,pageNo,"admin/audit/getDelComment.xhtml");
		pageUtil.initPageInfo(params);
		model.put("pageUtil", pageUtil);
		model.put("commentList", commentList);
		return "/admin/audit/delComment.vm";
	}
	
	
	private  <T extends Date> T getLastMonthFirstDay(T day) {
		if(day==null) return null;
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(day);
		int month = calendar.get(Calendar.MONTH);
		calendar.set(Calendar.MONTH, month -1);
		calendar.set(Calendar.DAY_OF_MONTH, 1);
		String datefor = DateUtil.format(calendar.getTime(), "yyyy-MM-dd");
		Long mill = DateUtil.parseDate(datefor).getTime();
		T another = (T) day.clone();
		another.setTime(mill);
		return  another;
	}
	//举报数据
	@RequestMapping("/admin/audit/accusationList.xhtml")
	public String accusationList(Integer pageNo, ModelMap model){
		if (pageNo == null) pageNo = 0;
		int rowsPerpage = 10;
		int firstRows = pageNo * rowsPerpage;
		Integer count = blogService.getAccusationCount();
		List<Accusation> accList = blogService.getAccusationList(firstRows, rowsPerpage);
		for (Accusation accusation : accList) {
			if(StringUtils.equals(accusation.getTag(), Accusation.TAG_USERMESSAGE)){
				UserMessageAction userMessageAction=daoService.getObject(UserMessageAction.class, accusation.getRelatedid());
				accusation.setRelatedid2(userMessageAction.getFrommemberid());
			}
		}
		PageUtil pageUtil = new PageUtil(count, rowsPerpage, pageNo, "admin/audit/accusationList.xhtml");
		pageUtil.initPageInfo();
		model.put("accList", accList);
		model.put("pageUtil", pageUtil);
		List<Long> memberidList = ServiceHelper.getMemberIdListFromBeanList(accList);
		List<Long> memberidList2 = BeanUtil.getBeanPropertyList(accList, Long.class, "relatedid2", true);
		memberidList.addAll(memberidList2);
	    addCacheMember(model, memberidList);
		return "admin/audit/accusationList.vm";
	}
	// ~~~~~~~~~~~~~~~~~~~~~~举报~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	@RequestMapping("/admin/audit/ajax/removeAccusation.xhtml")
	public String removeAccusation(Long accid, ModelMap model) {
		Accusation acc = daoService.getObject(Accusation.class, accid);
		daoService.removeObject(acc);
		return showJsonSuccess(model);
	}

	@RequestMapping("/admin/audit/ajax/clerkAccusation.xhtml")
	public String clerkAccusation(Long accid, String p, ModelMap model) {
		User user = getLogonUser();
		Accusation acc = daoService.getObject(Accusation.class, accid);
		if (acc != null) {
			acc.setClerk(acc.getId());
			acc.setUpdatetime(new Timestamp(System.currentTimeMillis()));
			if ("n".equals(p)) {
				this.dealAccusaction(acc, user, false);
				acc.setStatus(Accusation.STATUS_PROCESSED_N);
				daoService.saveObject(acc);
				return showJsonSuccess(model);
			} else {
				acc.setStatus(Accusation.STATUS_PROCESSED_Y);
			}
			daoService.saveObject(acc);
			// 积分、发送在内短信
			this.dealAccusaction(acc, user, true);
			if (Accusation.TAG_DIARY.equals(acc.getTag())) {
				DiaryBase diary = diaryService.getDiaryBase(acc.getRelatedid());
				if (diary != null) {
					diary.setStatus(Status.N_ACCUSE);
					daoService.saveObject(diary);
					searchService.pushSearchKey(diary);
				}
			} else if (Accusation.TAG_DIARYCOMMENT.equals(acc.getTag())) {
				DiaryComment comment = daoService.getObject(DiaryComment.class, acc.getRelatedid());
				if (comment != null) {
					comment.setStatus(Status.N_ACCUSE);
					daoService.saveObject(comment);
				}
			}else if (Accusation.TAG_GEWAQUESTION.equals(acc.getTag())) {
				GewaQuestion question = daoService.getObject(GewaQuestion.class, acc.getRelatedid());
				if (question != null) {
					question.setStatus(Status.N_ACCUSE);
					daoService.saveObject(question);
				}
			} else if (Accusation.TAG_GEWAANSWER.equals(acc.getTag())) {
				GewaAnswer answer = daoService.getObject(GewaAnswer.class, acc.getRelatedid());
				if (answer != null) {
					answer.setStatus(Status.N_ACCUSE);
					daoService.saveObject(answer);
				}
			} else if (Accusation.TAG_COMMENT.equals(acc.getTag())) {
				Comment comment = commentService.getCommentById(acc.getRelatedid());
				if (comment != null) {
					comment.setStatus(Status.N_ACCUSE);
					commentService.saveComment(comment);
				}
			} else if (Accusation.TAG_USERMESSAGE.equals(acc.getTag())) {
				UserMessageAction userMessageAction=daoService.getObject(UserMessageAction.class, acc.getRelatedid());
				if (userMessageAction != null) {
					userMessageAction.setStatus("tdel");
					daoService.saveObject(userMessageAction);
				}
			}
			return showJsonSuccess(model);
		}
		return showJsonSuccess(model, "数据不存在！");
	}
	@RequestMapping("/admin/audit/videoList.xhtml")
	public String auditMPVideoList(ModelMap model, Integer pageNo, String astatus, Long memberid, String tag){
		if(StringUtils.isBlank(astatus)) astatus=Status.N;
		if(pageNo == null) pageNo = 0;
		int rowsPerPage = 10;
		int firstRow = pageNo*rowsPerPage;
		int count=pictureService.getMemberPictureCount(null, tag, memberid, TagConstant.FLAG_VIDEO, astatus);//网友上传视频，在memberpicture
		List<MemberPicture> mVideoList=pictureService.getMemberPictureList(null, tag, memberid, TagConstant.FLAG_VIDEO, astatus, firstRow, rowsPerPage);
		Map<Long, Member> memberMap=new HashMap<Long, Member>();
		Map<Long, Object> objMap=new HashMap<Long, Object>();
		Map<Long, String> objTagMap=new HashMap<Long, String>();
		for(MemberPicture mp: mVideoList){
			memberMap.put(mp.getId(), daoService.getObject(Member.class, mp.getMemberid()));
			if(mp.getTag().equals(TagConstant.TAG_MOVIE)){
				objMap.put(mp.getId(), daoService.getObject(Movie.class, mp.getRelatedid()));
			}else if(mp.getTag().equals(TagConstant.TAG_DRAMA)){
				objMap.put(mp.getId(), daoService.getObject(Drama.class, mp.getRelatedid()));
			}
			objTagMap.put(mp.getId(), mp.getTag());
		}
		model.put("objMap", objMap);
		model.put("objTagMap", objTagMap);
		model.put("memberMap", memberMap);
		Map params=new HashMap();
		params.put("astatus", astatus);
		params.put("tag", tag);
		params.put("memberid", memberid);
		PageUtil pageUtil=new PageUtil(count,rowsPerPage,pageNo,"/admin/audit/videoList.xhtml");
		pageUtil.initPageInfo(params);
		model.put("astatus", astatus);
		model.put("tag", tag);
		model.put("pageUtil", pageUtil);
		model.put("mVideoList", mVideoList);
		return "admin/audit/attachMPVideoList.vm";
	}
	@RequestMapping("/admin/audit/videoDetail.xhtml")
	public String auditMPVideoDetail(ModelMap model, Long mpictureid){
		MemberPicture memberPicture=daoService.getObject(MemberPicture.class, mpictureid);
		model.put("memberPicture", memberPicture);
		return "admin/audit/attachMPVideoDetail.vm";
	}
	
	private void dealAccusaction(Accusation acc, User user, boolean message) {
		if (acc.getMemberid() != null) {
			SysMessageAction sysmessage = new SysMessageAction(SysAction.STATUS_RESULT);
			sysmessage.setFrommemberid(1l);
			sysmessage.setTomemberid(acc.getMemberid());
			if (message) {// 举报属实
				pointService.addPointInfo(acc.getMemberid(), PointConstant.SCORE_ACCUSATION, "举报属实", PointConstant.TAG_ACCUSATION, acc.getId(), user.getId());
				if (acc.getMessage() != null) {
					sysmessage.setBody("您举报的【" + acc.getMessage().substring(0, acc.getMessage().length() > 50 ? 50 : acc.getMessage().length())
							+ "】已被审核,给予5个积分奖励。");
				} else if (acc.getBody() != null) {
					sysmessage.setBody("您举报的【" + acc.getBody().substring(0, acc.getBody().length() > 50 ? 50 : acc.getBody().length())
							+ "】已被审核,给予5个积分奖励。");
				}
			} else {// 举报错误
				if (acc.getMessage() != null) {
					sysmessage.setBody("您举报的【" + acc.getMessage().substring(0, acc.getMessage().length() > 50 ? 50 : acc.getMessage().length())
							+ "】举报的内容不违反相应的规定，故暂时没有受理。");
				} else if (acc.getBody() != null) {
					sysmessage.setBody("您举报的【" + acc.getBody().substring(0, acc.getBody().length() > 50 ? 50 : acc.getBody().length())
							+ "】举报的内容不违反相应的规定，故暂时没有受理。");
				}
			}
			daoService.saveObject(sysmessage);
		}
	}
}
