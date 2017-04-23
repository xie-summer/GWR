package com.gewara.web.action.blog;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
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
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.gewara.command.QuestionCommand;
import com.gewara.constant.DiaryConstant;
import com.gewara.constant.Status;
import com.gewara.constant.TagConstant;
import com.gewara.constant.content.SignName;
import com.gewara.json.bbs.MarkCountData;
import com.gewara.model.bbs.Diary;
import com.gewara.model.bbs.qa.GewaAnswer;
import com.gewara.model.bbs.qa.GewaQaExpert;
import com.gewara.model.bbs.qa.GewaQuestion;
import com.gewara.model.common.BaseInfo;
import com.gewara.model.common.County;
import com.gewara.model.content.GewaCommend;
import com.gewara.model.drama.Drama;
import com.gewara.model.drama.Theatre;
import com.gewara.model.movie.Cinema;
import com.gewara.model.movie.Movie;
import com.gewara.model.sport.Sport;
import com.gewara.model.sport.SportItem;
import com.gewara.model.user.Member;
import com.gewara.model.user.MemberInfo;
import com.gewara.service.OperationService;
import com.gewara.service.PlaceService;
import com.gewara.service.bbs.BlogService;
import com.gewara.service.bbs.DiaryService;
import com.gewara.service.bbs.MarkService;
import com.gewara.service.bbs.QaService;
import com.gewara.service.drama.DramaPlayItemService;
import com.gewara.service.drama.OpenDramaService;
import com.gewara.service.movie.MCPService;
import com.gewara.service.sport.OpenTimeTableService;
import com.gewara.service.sport.SportService;
import com.gewara.service.ticket.OpenPlayService;
import com.gewara.support.ErrorCode;
import com.gewara.support.MultiPropertyComparator;
import com.gewara.support.ReadOnlyTemplate;
import com.gewara.support.ServiceHelper;
import com.gewara.untrans.CacheDataService;
import com.gewara.untrans.CommonService;
import com.gewara.untrans.activity.SynchActivityService;
import com.gewara.untrans.gym.SynchGymService;
import com.gewara.util.BeanUtil;
import com.gewara.util.DateUtil;
import com.gewara.util.RelatedHelper;
import com.gewara.util.WebUtils;
import com.gewara.web.action.AnnotationController;
import com.gewara.web.util.PageUtil;
import com.gewara.xmlbind.activity.RemoteActivity;
import com.gewara.xmlbind.gym.RemoteCourse;
import com.gewara.xmlbind.gym.RemoteGym;

@Controller
public class QAIndexPageController extends AnnotationController {
	@Autowired@Qualifier("placeService")
	private PlaceService placeService;
	public void setPlaceService(PlaceService placeService) {
		this.placeService = placeService;
	}
	@Autowired@Qualifier("commonService")
	private CommonService commonService;
	public void setCommonService(CommonService commonService) {
		this.commonService = commonService;
	}
	@Autowired@Qualifier("sportService")
	private SportService sportService;
	public void setSportService(SportService sportService) {
		this.sportService = sportService;
	}
	@Autowired@Qualifier("dramaPlayItemService")
	private DramaPlayItemService dramaPlayItemService;
	
	@Autowired@Qualifier("mcpService")
	private MCPService mcpService;
	public void setMcpService(MCPService mcpService) {
		this.mcpService = mcpService;
	}
	@Autowired@Qualifier("synchGymService")
	private SynchGymService synchGymService;
	public void setSynchGymService(SynchGymService synchGymService) {
		this.synchGymService = synchGymService;
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
	@Autowired@Qualifier("openDramaService")
	private OpenDramaService openDramaService;
	public void setOpenDramaService(OpenDramaService openDramaService) {
		this.openDramaService = openDramaService;
	}
	@Autowired@Qualifier("blogService")
	private BlogService blogService;
	public void setBlogService(BlogService blogService){
		this.blogService = blogService;
	}
	@Autowired@Qualifier("cacheDataService")
	private CacheDataService cacheDataService;
	
	@Autowired@Qualifier("synchActivityService")
	private SynchActivityService synchActivityService;
	public void setActivityRemoteService(SynchActivityService synchActivityService) {
		this.synchActivityService = synchActivityService;
	}
	
	@Autowired@Qualifier("openTimeTableService")
	private OpenTimeTableService openTimeTableService;
	
	@Autowired@Qualifier("markService")
	private MarkService markService;
	
	@Autowired@Qualifier("diaryService")
	private DiaryService diaryService;
	
	@Autowired@Qualifier("openPlayService")
	private OpenPlayService openPlayService;

	@Autowired@Qualifier("readOnlyTemplate")
	private ReadOnlyTemplate readOnlyTemplate;
	public void setReadOnlyHibernateTemplate(ReadOnlyTemplate readOnlyTemplate) {
		this.readOnlyTemplate = readOnlyTemplate;
	}
	
	
	// 首页
	@RequestMapping("/qa/index.xhtml")
	public String index(ModelMap model, HttpServletRequest request, HttpServletResponse response) throws Exception {
		String citycode = WebUtils.getAndSetDefault(request, response);
		model.put("cinemamovieMap", getLeftData("cinema", "movie", 8));
		model.put("theatredramaMap", getLeftData("theatre", "drama", 8));
		model.put("gymcourseMap", getLeftData("gym", "gymcourse", 8));
		model.put("sportserviceMap", getLeftData("sport", "sportservice", 8));
		model.put("activityMap", getLeftData("activity", "activity", 8));
		List<GewaQuestion> qRList = qaService.getQuestionListByHotvalue(citycode, GewaQuestion.HOTVALUE_RECOMMEND, 0, 6);
		List<GewaQuestion> qNList = qaService.getQuestionListByQuestionstatus(citycode, GewaQuestion.QS_STATUS_N, "addtime", 0, 20);
		Integer qNCount = qaService.getQuestionCountByQuestionstatus(citycode, "N");
		List<GewaQuestion> qYList = qaService.getQuestionListByQuestionstatus(citycode, GewaQuestion.QS_STATUS_Y, "dealtime", 0, 20);
		Integer qYCount = qaService.getQuestionCountByQuestionstatus(citycode, "Y");
		List<GewaCommend> giList = commonService.getGewaCommendListByRelatedid(null, SignName.TAG_QACOMMENDPIC, null, null, true, 0, 1);
		Collections.sort(giList, new MultiPropertyComparator(new String[]{"ordernum","addtime"}, new boolean[]{true,false}));
		Map<Long,Integer> qYCountMap = new HashMap<Long,Integer>();
		for(GewaQuestion ques : qYList){
			Integer acount = qaService.getAnswerCountByQuestionId(ques.getId());
			qYCountMap.put(ques.getId(), acount);
		}
		Map<Long,Integer> qNCountMap = new HashMap<Long,Integer>();
		for(GewaQuestion ques : qNList){
			Integer acount = qaService.getAnswerCountByQuestionId(ques.getId());
			qNCountMap.put(ques.getId(), acount);
		}
		model.put("qYCountMap", qYCountMap);
		model.put("qNCountMap", qNCountMap);	
		model.put("qNList", qNList);
		model.put("qYList", qYList);
		model.put("qRList", qRList);
		model.put("qNCount", qNCount);
		model.put("qYCount", qYCount);
		model.put("giList", giList);
		model.putAll(getRightData());
		return "qa/index.vm";
	}
	@RequestMapping("/qa/questionList.xhtml")
	public String questionList(ModelMap model, QuestionCommand qc, HttpServletRequest request, HttpServletResponse response) {
		String citycode = WebUtils.getAndSetDefault(request, response);
		if (qc.getRelatedid() != null) {
			Object relate = relateService.getRelatedObject(qc.getTag(), qc.getRelatedid());
			model.put("relate", relate);
		}
		if (qc.getCategoryid() != null) {
			Object relate2 = relateService.getRelatedObject(qc.getCategory(), qc.getCategoryid());
			model.put("relate2", relate2);
			model.put("category", qc.getCategory());
		}
		if (qc.pageNo == null) qc.pageNo = 0;
		Integer count = Integer.valueOf(readOnlyTemplate.findByCriteria(getQuestionQuery(qc, citycode).setProjection(Projections.rowCount())).get(0)+"");
		PageUtil pageUtil = new PageUtil(count, qc.rowsPerPage, qc.pageNo, "qa/questionList.xhtml", true, true);
		Map params = new HashMap();
		params.put("order",  qc.getOrder());
		params.put("status",  qc.status);
		params.put("tag",  qc.getTag());
		params.put("keyname",  qc.getKeyname());
		if (qc.getRelatedid() != null)
			params.put("relatedid",  qc.getRelatedid());
		params.put("category",  qc.getCategory());
		if (qc.getCategoryid() != null)
			params.put("categoryid",  qc.getCategoryid());
		pageUtil.initPageInfo(params);
		List<GewaQuestion> questionList = readOnlyTemplate.findByCriteria(getQuestionQuery(qc, citycode), qc.pageNo * qc.rowsPerPage, qc.rowsPerPage);
		model.put("pageUtil", pageUtil);
		model.put("questionList", questionList);
		model.put("count", count);
		model.putAll(getReferenceData(qc.getTag(), citycode));
		model.putAll(getRightData());
		return "qa/questionList.vm";
	}
	
	/**
	 * 问题详细页面
	 * @param sessid
	 * @param qid
	 * @param model
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping("/qa/qaDetail.xhtml")
	public String qaDetail(@CookieValue(value=LOGIN_COOKIE_NAME, required=false)String sessid, @RequestParam("qid")Long qid, ModelMap model, HttpServletRequest request, HttpServletResponse response) {
		
		//获取并设置城市
		String citycode = WebUtils.getAndSetDefault(request, response);
		//问题
		GewaQuestion question = daoService.getObject(GewaQuestion.class, qid);
		
		cacheDataService.getAndSetIdsFromCachePool(GewaQuestion.class, qid);
		cacheDataService.getAndSetClazzKeyCount(GewaQuestion.class, qid);
		
		if (question == null || Status.N_DELETE.equals(question.getStatus())) return show404(model, "问题不存在或正在审核或已经删除！");
		if(Status.N_FILTER.equals(question.getStatus())) return showMessage(model, "问题正在等待审核");
		
		//回答次数
		model.put("answerNum", qaService.getAnswerCountByQuestionId(question.getId()));
		
		//关联场馆(影院/剧院)
		if (question.getRelatedid() != null) {
			Object relate = relateService.getRelatedObject(question.getTag(), question.getRelatedid());
			model.put("relate", relate);
			
			//相关问题
			List<GewaQuestion> questionList_Tag = qaService.getQuestionByTagAndRelatedid(citycode, question.getTag(), question.getRelatedid(), 0, 6);
			questionList_Tag.remove(question);
			model.put("questionList_Tag", questionList_Tag);
		}
		
		//关联内容(电影/话剧等)
		if (question.getCategoryid() != null) {
			Object relate2 = relateService.getRelatedObject(question.getCategory(), question.getCategoryid());
			model.put("relate2", relate2);
			model.put("category", question.getCategory());
			
			//相关影评
			if(StringUtils.equals(TagConstant.TAG_MOVIE, question.getCategory())){
				model.put("markCount", markService.getMarkCountByTagRelatedid(TagConstant.TAG_MOVIE, question.getCategoryid()));
				model.put("diaryCount", getDiaryCountByMovieid(question.getCategoryid()));
				List<Diary> diaryList =diaryService.getDiaryList(Diary.class, citycode, DiaryConstant.DIARY_TYPE_COMMENT, TagConstant.TAG_MOVIE, question.getCategoryid(), 0, 3, "addtime");
				model.put("diaryList", diaryList);
				addCacheMember(model, ServiceHelper.getMemberIdListFromBeanList(diaryList));
			}
			
			//相关问题
			List<GewaQuestion> questionList_Category = qaService.getQuestionByCategoryAndCategoryid(citycode, question.getCategory(), question.getCategoryid(), 0, 6);
			questionList_Category.remove(question);
			model.put("questionList_Category", questionList_Category);
		}
		
		//回答
		List<GewaAnswer> answerList = qaService.getAnswerListByQuestionid(question.getId());
		
		//回答会员下级还差积分，职位，下级职位
		Map mapGewaAnswerMember=new HashMap();
		for (GewaAnswer gewaanser : answerList) {
			mapGewaAnswerMember.put(gewaanser.getMemberid(), memberService.getMemberJobsInfo(gewaanser.getMemberid()));
		}
		model.put("mapGewaAnswerMemberPosition",mapGewaAnswerMember);
		
		//问题发布者职位信息
		model.put("questionMemberPosition", memberService.getMemberJobsInfo(question.getMemberid()));
		
		//问题最佳回答
		GewaAnswer bestAnswer = qaService.getBestAnswerByQuestionid(question.getId());
		if(bestAnswer!=null){
			//最佳回答会员职位信息
			model.put("bestMemberPosition",  memberService.getMemberJobsInfo(bestAnswer.getMemberid()));
			//缓存用户
			addCacheMember(model, bestAnswer.getMemberid());
		}
		if (answerList.size() > 0 && bestAnswer != null) {
			answerList.remove(bestAnswer);
		}
		//缓存用户
		addCacheMember(model, ServiceHelper.getMemberIdListFromBeanList(answerList));
		model.put("question", question);
		//缓存用户
		addCacheMember(model, question.getMemberid());
		model.put("answerList", answerList);
		model.put("bestAnswer", bestAnswer);
		
		//权限相关
		String ip = WebUtils.getRemoteIp(request);
		Member member = loginService.getLogonMemberBySessid(ip, sessid);
		if ((member != null) && (member.getId().longValue() == question.getMemberid().longValue())) {
			if ((bestAnswer == null) && (!GewaQuestion.QS_STATUS_NOPROPER.equals(question.getQuestionstatus()))) {
				model.put("modright", true); // 修改的权限
				model.put("bestright", true); // 设置正确答案的权限
				model.put("noproper", true); // 无满意答案的权限
			}
			if (GewaQuestion.QS_STATUS_Z.equals(question.getQuestionstatus())) {
				model.put("delright", true); // 删除的权限
			}
			model.put("addinforight", true); // 补充内容的权限
		}
		if (GewaQuestion.QS_STATUS_N.equals(question.getQuestionstatus()) || GewaQuestion.QS_STATUS_Z.equals(question.getQuestionstatus())) {
			Timestamp starttime = question.getAddtime();
			Timestamp endtime = new Timestamp(System.currentTimeMillis());
			long milliseconds = (DateUtil.addDay(starttime, GewaQuestion.MAXDAYS).getTime() - endtime.getTime()) / 1000;
			if (milliseconds > 0) {
				long days = milliseconds / (24 * 60 * 60);// 相差的天数
				long hours = (milliseconds - days * 24 * 60 * 60) / (60 * 60);// 相差的小时数
				model.put("timeinfo", "离问题结束还有：" + days + "天" + hours + "小时");
			} else {
				model.put("timeinfo", "该问题需要处理了");
			}
		} else {
			if (question.getDealtime() != null && GewaQuestion.QS_STATUS_Y.equals(question.getQuestionstatus()))
				model.put("timeinfo", "解决时间：" + DateUtil.format(question.getDealtime(), "yyyy-MM-dd HH:mm"));
		}
		
		// 相关话题
		model.putAll(getInterestData(question, question.getTag(), question.getRelatedid(), question.getCategory(), question.getCategoryid(), 5, citycode));
		
		//每周之星
		model.putAll(getRightData());
		
		//关注校验
		if(member != null){
			model.put("logonMember", member);
			List<Long> memberidList = new ArrayList<Long>();
			memberidList = ServiceHelper.getMemberIdListFromBeanList(answerList);
			memberidList.add(question.getMemberid());
			if(bestAnswer!= null)memberidList.add(bestAnswer.getMemberid());
			Map<Long,Boolean> isTreasureMap = new HashMap<Long, Boolean>();
			for (Long memberid : memberidList) {
				if(!isTreasureMap.keySet().contains(memberid)){
					isTreasureMap.put(memberid,blogService.isTreasureMember(member.getId(),memberid));
				}
			}
			model.put("isTreasureMap", isTreasureMap);
		}
		
		getRelateList(citycode, question, model);
		//热门电影
		// 评分统计
		model.put("markData", markService.getMarkdata(TagConstant.TAG_MOVIE));
		RelatedHelper rh = new RelatedHelper();
		Map dataMap = new HashMap();
		dataMap.put("relatedHelper", rh);
		dataMap.putAll(getHotMovie(rh, 4, citycode));
		model.putAll(dataMap);
		
		
		return "qa/wide_qaDetail.vm";
	}
	
	private void getRelateList(String citycode, GewaQuestion question, ModelMap model){
		List<RemoteActivity> hotActivityList = new ArrayList<RemoteActivity>();
		String tag = "";
		if(StringUtils.equals(question.getTag(), TagConstant.TAG_CINEMA) || StringUtils.equals(question.getCategory(), TagConstant.TAG_MOVIE)){
			tag = TagConstant.TAG_CINEMA;
			List<Movie> movieList = mcpService.getCurMovieList(citycode);
			movieList = BeanUtil.getSubList(movieList, 0, 6);
			model.put("movieList", movieList);
		}else if(StringUtils.equals(question.getTag(), TagConstant.TAG_THEATRE) || StringUtils.equals(question.getCategory(), TagConstant.TAG_DRAMA)){
			tag = TagConstant.TAG_THEATRE;
			List<Long> bookingIdList = dramaPlayItemService.getCurDramaidList(citycode);
			bookingIdList = BeanUtil.getSubList(bookingIdList, 0, 6);
			List<Drama> dramaList = daoService.getObjectList(Drama.class, bookingIdList);
			model.put("dramaList", dramaList);
		}else if(StringUtils.equals(question.getTag(), TagConstant.TAG_SPORT) || StringUtils.equals(question.getCategory(), TagConstant.TAG_SPORTITEM)){
			tag = TagConstant.TAG_SPORT;
			List<Long> sportIdList = openTimeTableService.getCurOttSportIdList(null, citycode);
			sportIdList = BeanUtil.getSubList(sportIdList, 0, 6);
			List<Sport> sportList = daoService.getObjectList(Sport.class, sportIdList);
			model.put("sportList", sportList);
		}else if(StringUtils.equals(question.getTag(), TagConstant.TAG_GYM) || StringUtils.equals(question.getCategory(), TagConstant.TAG_GYMCOURSE)
			|| StringUtils.equals(question.getCategory(), TagConstant.TAG_GYMCARD) || StringUtils.equals(question.getCategory(), TagConstant.TAG_GYMCOACH)){
			tag = TagConstant.TAG_GYM;
			ErrorCode<List<RemoteGym>> code = synchGymService.getGymList(citycode, null, null, "clickedtimes", false, 0, 6);
			if(code.isSuccess()){
				List<RemoteGym> gymList = code.getRetval();
				model.put("gymList", gymList);
			}
		}
		if(StringUtils.isNotBlank(tag)){
			ErrorCode<List<RemoteActivity>> code = synchActivityService.getActivityListByOrder(citycode, RemoteActivity.ATYPE_GEWA, RemoteActivity.TIME_CURRENT, tag, null, null, null, null, 0, 3);
			if(code.isSuccess()){
				List<RemoteActivity> activityList = code.getRetval();
				if(!CollectionUtils.isEmpty(activityList)){
					hotActivityList.addAll(activityList);
				}
			}
		}else{
			tag = TagConstant.TAG_CINEMA;
		}
		if(CollectionUtils.isEmpty(hotActivityList)){
			hotActivityList = synchActivityService.getGewaCommendActivityList(citycode, SignName.INDEX_MOVIEACTIVITY, null,0, 3);
		}
		model.put("hotActivityList", hotActivityList);
	}
	
	private Integer getDiaryCountByMovieid(Long movieid){
		String query = "select count(*) from Diary d where d.category='movie' and d.categoryid=? and d.type = ?";
		List list = readOnlyTemplate.find(query, movieid, DiaryConstant.DIARY_TYPE_COMMENT);
		if (list.isEmpty()) return 0;
		return Integer.parseInt(""+list.get(0));
	}
	@RequestMapping("/qa/modQuestion.xhtml")
	public String topic(@CookieValue(value=LOGIN_COOKIE_NAME, required=false)String sessid,
			HttpServletRequest request, ModelMap model, String tag, Long relatedid, 
			@RequestParam(required = false, value = "qid")Long qid) {
		String ip = WebUtils.getRemoteIp(request);
		Member member = loginService.getLogonMemberBySessid(ip, sessid);
		if (member == null) return showError(model, "您还没登录，请返回登录！");
		GewaQuestion question = new GewaQuestion(member.getId());
		if (qid != null) {
			question = daoService.getObject(GewaQuestion.class, qid);
			if (question == null)
				return showError(model, "该问题不存在或已被删除！");
			if (member.getId().longValue() != question.getMemberid())
				return showError(model, "您没有权限编辑此问题！");
			if (GewaQuestion.QS_STATUS_NOPROPER.equals(question.getQuestionstatus()) || GewaQuestion.QS_STATUS_Y.equals(question.getQuestionstatus()))
				return showError(model, "该问题已近被处理过,不能编辑！");
			tag = question.getTag();
			relatedid = question.getRelatedid();
		} else {
			String opkey = OperationService.TAG_ADDCONTENT + member.getId(); 
			if(!operationService.isAllowOperation(opkey, 40))
				return showError(model, "发帖频率不能太快！");

			if (ServiceHelper.isTag(tag)) {
				question.setTag(tag);
				question.setRelatedid(relatedid);
			} else if (ServiceHelper.isCategory(tag)) {
				question.setTag(ServiceHelper.getTag(tag));
				question.setCategory(tag);
				question.setCategoryid(relatedid);
			}
		}
		MemberInfo mi = daoService.getObject(MemberInfo.class, member.getId());
		if(mi!=null) model.put("expvalue", mi.getExpvalue());
		if (question.getRelatedid() != null) {
			Object relate = relateService.getRelatedObject(question.getTag(), question.getRelatedid());
			model.put("relate", relate);
			String countycode = (String) BeanUtil.get(relate, "countycode");
			
			if (relate instanceof BaseInfo && StringUtils.isNotBlank(countycode)) {
				model.put("indexareaList", placeService.getIndexareaByCountyCode(countycode));
				model.put("countycode", countycode);
				String indexareacode = (String) BeanUtil.get(relate, "indexareacode");
				model.put("indexareacode", indexareacode);
				List placeList = placeService.getPlaceListByTag(tag, countycode, indexareacode);
				model.put("placeList", placeList);
			}
			if(StringUtils.isNotBlank(question.getCountycode())){
				County county = daoService.getObject(County.class, question.getCountycode());
				model.put("county", county);
			}
		}
		if(question.getCategoryid()!=null){
			Object relate2 = relateService.getRelatedObject(question.getCategory(), question.getCategoryid());
			model.put("relate2", relate2);
		}
		model.put("question", question);
		model.putAll(getRightData());
		model.put("logonMember", member);
		return "qa/modQuestion.vm";
	}

	private DetachedCriteria getQuestionQuery(QuestionCommand qc, String citycode) {
		DetachedCriteria query = DetachedCriteria.forClass(GewaQuestion.class);
		query.add(Restrictions.eq("citycode", citycode));
		query.add(Restrictions.like("status", Status.Y, MatchMode.START));
		// query.add(Restrictions.ne("questionstatus",
		// GewaQuestion.QS_STATUS_NOPROPER));
		// 其他
		if ("other".equals(qc.getTag())) {
			query.add(Restrictions.isNull("tag"));
			query.add(Restrictions.isNull("category"));
		} else {
			if (StringUtils.isNotBlank(qc.getTag())) {
				query.add(Restrictions.eq("tag", qc.getTag()));
				if (qc.getRelatedid() != null) {
					query.add(Restrictions.eq("relatedid", qc.getRelatedid()));
				}
			}
			if (StringUtils.isNotBlank(qc.getCategory())) {
				query.add(Restrictions.eq("category", qc.getCategory()));
				if (qc.getCategoryid() != null) {
					query.add(Restrictions.eq("categoryid", qc.getCategoryid()));
				}
			}
		}
		if (StringUtils.isNotBlank(qc.getKeyname()))
			query.add(Restrictions.ilike("title", qc.getKeyname(), MatchMode.ANYWHERE));
		if (StringUtils.isNotBlank(qc.status)) {
			if (GewaQuestion.QS_STATUS_N.equals(qc.status))
				query.add(Restrictions.or(Restrictions.eq("questionstatus", GewaQuestion.QS_STATUS_N), Restrictions.eq("questionstatus",
						GewaQuestion.QS_STATUS_Z)));
			else
				query.add(Restrictions.eq("questionstatus", qc.status));
		}
		if (StringUtils.isNotBlank(qc.getOrder())){
			query.addOrder(Order.desc(qc.getOrder()));
			query.addOrder(Order.asc("id"));
		}
		else
			query.addOrder(Order.desc("addtime"));
		return query;
	}

	private Map getReferenceData(String tag, String citycode) {
		Map model = new HashMap();
		if (StringUtils.isBlank(tag)) {
		} else if (tag.equals("movie") || tag.equals("cinema")) {
			List<Movie> movieList = mcpService.getCurMovieListByMpiCount(citycode, 0, 8);
			model.put("movieList", movieList);
			List<Cinema> cinemaList = placeService.getPlaceList(citycode, Cinema.class, "clickedtimes", false, 0, 8);
			model.put("cinemaList", cinemaList);
		} else if (tag.equals("gym") || tag.equals("gymcourse")) {
			ErrorCode<List<RemoteGym>> gymCode = synchGymService.getGymList(citycode, null, null, "clickedtimes", false, 0, 8);
			if(gymCode.isSuccess()){
				List<RemoteGym> gymList = gymCode.getRetval();
				model.put("gymList", gymList);
			}
			ErrorCode<List<RemoteCourse>> courseCode = synchGymService.getHotCourseList(0, 2);
			if(courseCode.isSuccess()) model.put("courseList", courseCode.getRetval());
		} else if (tag.equals("sport") || tag.equals("sportservice")) {
			List<Sport> sportList = placeService.getPlaceList(citycode, Sport.class, "clickedtimes", false, 0, 8);
			model.put("sportList", sportList);
			List<SportItem> sportItemList = sportService.getHotSportItemList(0, 15);
			model.put("sportItemList", sportItemList);
		} else if(tag.equals("drama") || "theatre".equals(tag)){
			List<Drama> dramaList = openDramaService.getCurPlayDrama(citycode, 0, 8);
			model.put("dramaList", dramaList);
			List<Theatre> theatreList = placeService.getPlaceList(citycode, Theatre.class,"clickedtimes", false, 0, 8);
			model.put("theatreList", theatreList);
		}
		return model;
	}

	private Map getLeftData(String tag, String category, Integer maxnum) {
		Map<Map<Object, String>, Integer> tagMap = qaService.getQuestionListByTagGroup(tag, 0, maxnum);
		Map<Map<Object, String>, Integer> categoryMap = qaService.getQuestionListByCategoryGroup(category, 0, maxnum);
		Map<Map<Object, String>, Integer> tcmap = new HashMap();
		tcmap.putAll(tagMap);
		tcmap.putAll(categoryMap);
		List<Map.Entry<Map<Object, String>, Integer>> tcQuestionMap = new ArrayList<Map.Entry<Map<Object, String>, Integer>>(tcmap.entrySet());
		Collections.sort(tcQuestionMap, new ValueComparator());
		if (tcQuestionMap.size() > maxnum)
			tcQuestionMap = tcQuestionMap.subList(0, maxnum);
		Map<Object, String> tagcategoryMap = new LinkedHashMap();
		for (Map.Entry<Map<Object, String>, Integer> entry : tcQuestionMap) {
			tagcategoryMap.putAll(entry.getKey());
		}
		return tagcategoryMap;
	}

	private Map getRightData() {
		Map model = new HashMap();
		List<Map> miMap = qaService.getTopMemberListByPoint(0, 5);
		List<GewaQaExpert> commendExpertList = qaService.getCommendExpertList(GewaQaExpert.HOTVALUE_RECOMMEND, 0, 1);
		Map commendMember = null;
		if (commendExpertList.size() > 0) {
			GewaQaExpert commendExpert = commendExpertList.get(0);
			Long mid = commendExpert.getMemberid();
			commendMember = memberService.getCacheMemberInfoMap(mid);
			List<GewaQuestion> mquestionList = qaService.getQuestionListByMemberid(mid, 0, 5);
			Integer mpoint = qaService.getPointByMemberid(mid);
			Integer answerCount = qaService.getAnswerCountByMemberid(mid);
			Integer bestAnswerCount = qaService.getBestAnswerCountByMemberid(mid);
			model.put("mquestionList", mquestionList);
			model.put("mpoint", mpoint);
			model.put("commendExpert", commendExpert);
			model.put("answerCount", answerCount);
			model.put("rate", Math.round((bestAnswerCount.doubleValue() / answerCount) * 100));
		}
		model.put("miMap", miMap);
		model.put("commendMember", commendMember);

		return model;
	}

	private Map getInterestData(GewaQuestion question, String tag, Long relatedid, String category, Long categoryid, Integer maxnum, String citycode) {
		Map model = new HashMap();
		if (StringUtils.isNotBlank(tag) || StringUtils.isNotBlank(category)) {
			Set<GewaQuestion> questionSet = new HashSet<GewaQuestion>();
			if (StringUtils.isNotBlank(category)) {
				if (categoryid != null) {
					List<GewaQuestion> cList = qaService.getQuestionByCategoryAndCategoryid(citycode, category, categoryid, 0, maxnum);
					cList.remove(question);
					questionSet.addAll(cList);
				}
				if (questionSet.size() < maxnum) {
					List<GewaQuestion> cList = qaService.getQuestionByCategoryAndCategoryid(citycode, category, null, 0, maxnum);
					cList.remove(question);
					questionSet.addAll(cList);
				}
			}
			if (questionSet.size() < maxnum && StringUtils.isNotBlank(tag)) {
				if (relatedid != null) {
					List<GewaQuestion> tList = qaService.getQuestionByTagAndRelatedid(citycode, tag, relatedid, 0, maxnum);
					tList.remove(question);
					questionSet.addAll(tList);
				}
				if (questionSet.size() < maxnum) {
					List<GewaQuestion> tList = qaService.getQuestionByTagAndRelatedid(citycode, tag, null, 0, maxnum);
					tList.remove(question);
					questionSet.addAll(tList);
				}
			}
			List<GewaQuestion> questionList = new ArrayList<GewaQuestion>(questionSet);
			if (questionSet.size() > maxnum) {
				questionList = questionList.subList(0, maxnum);
			}
			Map<Long,Integer> mapCount = new HashMap<Long, Integer>();
			for(GewaQuestion ques : questionList){
				mapCount.put(ques.getId(), qaService.getAnswerCountByQuestionId(ques.getId()));
			}
			model.put("mapCount", mapCount);
			model.put("questionList", questionList);
		}
		return model;
	}
	
	
	/**
	 * 热映电影
	 * @param rh
	 * @param maxSize 条数
	 * @param citycode 城市代码
	 * @return
	 */
	private Map getHotMovie(RelatedHelper rh, Integer maxSize, String citycode) {
		if(maxSize==null){
			maxSize=4;
		}
		Map dataMap = new HashMap();
		Map<Long, MarkCountData> markCountMap = new HashMap<Long, MarkCountData>();
		List<GewaCommend> movieIndexList = commonService.getGewaCommendList(citycode, SignName.MOVIEINDEX_MOVIE, null, null, true, 0, maxSize);
		commonService.initGewaCommendList("movieIndexList", rh, movieIndexList);
		if (movieIndexList.size() > 0) {
			for (GewaCommend gewaCommend : movieIndexList) {
				Movie movie = daoService.getObject(Movie.class, gewaCommend.getRelatedid());
				if(null!=movie){
					markCountMap.put(movie.getId(), markService.getMarkCountByTagRelatedid(TagConstant.TAG_MOVIE, movie.getId()));
				}
			}
		}
		dataMap.put("movieIndexList", movieIndexList);
		dataMap.put("opiMovieList", openPlayService.getOpiMovieidList(citycode, null));
		dataMap.put("markCountMap", markCountMap);
		return dataMap;
	}
	
	
	
}

class ValueComparator implements Comparator<Map.Entry<Map<Object, String>, Integer>> {
	public int compare(Map.Entry<Map<Object, String>, Integer> arg0, Map.Entry<Map<Object, String>, Integer> arg1) {
		return (arg1.getValue() - arg0.getValue());
	}
}