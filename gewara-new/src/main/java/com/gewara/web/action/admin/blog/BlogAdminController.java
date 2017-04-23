package com.gewara.web.action.admin.blog;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.support.PropertyComparator;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.gewara.Config;
import com.gewara.api.gmail.request.SendMailByOutboxRequest;
import com.gewara.api.gmail.response.SendMailByOutboxResponse;
import com.gewara.api.gmail.service.GmailService;
import com.gewara.command.EmailRecord;
import com.gewara.constant.AdminCityContant;
import com.gewara.constant.DiaryConstant;
import com.gewara.constant.Status;
import com.gewara.constant.TagConstant;
import com.gewara.constant.sys.MongoData;
import com.gewara.model.acl.User;
import com.gewara.model.bbs.Bkmember;
import com.gewara.model.bbs.BlackMember;
import com.gewara.model.bbs.CustomerAnswer;
import com.gewara.model.bbs.CustomerQuestion;
import com.gewara.model.bbs.Diary;
import com.gewara.model.bbs.DiaryBase;
import com.gewara.model.bbs.DiaryComment;
import com.gewara.model.common.Relationship;
import com.gewara.model.drama.Drama;
import com.gewara.model.draw.DrawActivity;
import com.gewara.model.draw.Prize;
import com.gewara.model.draw.WinnerInfo;
import com.gewara.model.movie.Movie;
import com.gewara.model.pay.SMSRecord;
import com.gewara.model.sport.SportItem;
import com.gewara.model.user.Agenda;
import com.gewara.model.user.Member;
import com.gewara.model.user.MemberInfo;
import com.gewara.model.user.SysMessageAction;
import com.gewara.model.user.UserMessage;
import com.gewara.model.user.UserMessageAction;
import com.gewara.mongo.MongoService;
import com.gewara.service.bbs.AgendaService;
import com.gewara.service.bbs.BlogService;
import com.gewara.service.bbs.CustomerQuestionService;
import com.gewara.service.bbs.DiaryService;
import com.gewara.service.bbs.UserMessageService;
import com.gewara.service.drama.DrawActivityService;
import com.gewara.service.order.GoodsOrderService;
import com.gewara.support.ServiceHelper;
import com.gewara.untrans.CommentService;
import com.gewara.util.BeanUtil;
import com.gewara.util.DateUtil;
import com.gewara.util.JsonUtils;
import com.gewara.util.ValidateUtil;
import com.gewara.util.WebUtils;
import com.gewara.web.action.admin.BaseAdminController;
import com.gewara.web.util.PageUtil;
import com.gewara.xmlbind.bbs.Comment;

@Controller
public class BlogAdminController extends BaseAdminController {
	@Autowired@Qualifier("gmailService")
	private GmailService gmailService;
	@Autowired
	@Qualifier("blogService")
	private BlogService blogService = null;

	public void setBlogService(BlogService blogService) {
		this.blogService = blogService;
	}

	@Autowired
	@Qualifier("commentService")
	private CommentService commentService;

	public void setCommentService(CommentService commentService) {
		this.commentService = commentService;
	}

	@Autowired
	@Qualifier("diaryService")
	private DiaryService diaryService;

	public void setDiaryService(DiaryService diaryService) {
		this.diaryService = diaryService;
	}

	@Autowired
	@Qualifier("config")
	private Config config;

	public void setconfig(Config config) {
		this.config = config;
	}

	@Autowired
	@Qualifier("customerQuestionService")
	private CustomerQuestionService customerQuestionService;

	public void setCustomerQuestionService(CustomerQuestionService customerQuestionService) {
		this.customerQuestionService = customerQuestionService;
	}

	@Autowired
	@Qualifier("userMessageService")
	private UserMessageService userMessageService;

	public void setUserMessageService(UserMessageService userMessageService) {
		this.userMessageService = userMessageService;
	}

	@Autowired
	@Qualifier("mongoService")
	private MongoService mongoService;

	public void setMongoService(MongoService mongoService) {
		this.mongoService = mongoService;
	}

	@Autowired
	@Qualifier("drawActivityService")
	private DrawActivityService drawActivityService;

	public void setDrawActivityService(DrawActivityService drawActivityService) {
		this.drawActivityService = drawActivityService;
	}

	@Autowired
	@Qualifier("agendaService")
	private AgendaService agendaService;

	public void setAgendaService(AgendaService agendaService) {
		this.agendaService = agendaService;
	}

	@Autowired
	@Qualifier("goodsOrderService")
	protected GoodsOrderService goodsOrderService;

	public void setGoodsOrderService(GoodsOrderService goodsOrderService) {
		this.goodsOrderService = goodsOrderService;
	}

	@RequestMapping("/admin/blog/commentList.xhtml")
	public String commentList(String tag, @RequestParam(required = false, value = "relatedid") Long relatedid,
			@RequestParam(required = false, value = "keyname") String key, @RequestParam(required = false, value = "pageNo") Integer pageNo,
			ModelMap model, String atype) {
		Map<String, String> dataMap = new HashMap<String, String>();
		dataMap.put("cinema", "movie");
		dataMap.put("theatre", "drama");
		dataMap.put("sport", "sportservice");
		dataMap.put("bar", "bar");// 没项目评论
		dataMap.put("gym", "gym");// 没项目评论
		dataMap.put("ktv", "ktv");// 没项目评论

		if (StringUtils.isNotBlank(atype)) {
			tag = dataMap.get(tag);
		}
		model.put("tag", tag);
		model.put("relatedid", relatedid);
		List<Comment> commentList = new ArrayList<Comment>();
		if (StringUtils.isNotBlank(key)) {
			commentList = commentService.getCommentListByKey(tag, key);
		} else {
			if (pageNo == null)
				pageNo = 0;
			Integer count = 0;
			int rowsPerpage = 40;
			int firstRow = pageNo * rowsPerpage;
			count = commentService.getCommentCountByTag(tag);
			commentList = commentService.getCommentListByTag(tag, firstRow, rowsPerpage);
			PageUtil pageUtil = new PageUtil(count, rowsPerpage, pageNo, "admin/blog/commentList.xhtml");
			Map params = new HashMap();
			params.put("tag", new String[] { tag });
			params.put("relatedid", new String[] { relatedid == null ? " " : "" + relatedid });
			params.put("atype", atype);
			pageUtil.initPageInfo(params);
			model.put("pageUtil", pageUtil);
		}
		model.put("commentList", commentList);
		addCacheMember(model, ServiceHelper.getMemberIdListFromBeanList(commentList));
		model.put("dataMap", dataMap);
		return "admin/blog/commentList.vm";
	}

	@RequestMapping("/admin/blog/diaryList.xhtml")
	public String diaryList(String tag, Long relatedid, String type,/* 不为空即影评 */
			String keyname, Date fromDate, Date toDate, Integer pageNo, HttpServletRequest request, ModelMap model) {
		model.put("tag", tag);
		if (relatedid != null)
			model.put("relatedid", relatedid);
		String diarytype = DiaryConstant.DIARY_TYPE_TOPIC_DIARY;
		List<Diary> diaryList = new ArrayList<Diary>();
		List<Diary> topDiaryList = new ArrayList<Diary>();
		if (pageNo == null)
			pageNo = 0;
		int rowsPerpage = 20;
		int firstRow = pageNo * rowsPerpage;
		Integer count = 0;
		Timestamp startTime = null;
		Timestamp endTime = null;
		if (fromDate != null)
			startTime = DateUtil.parseTimestamp(DateUtil.formatDate(fromDate) + " 00:00:00");
		if (toDate != null)
			endTime = DateUtil.parseTimestamp(DateUtil.formatDate(toDate) + " 59:59:59");
		String citycode = getAdminCitycode(request);
		count = diaryService.getDiaryCountByKey(Diary.class, citycode, DiaryConstant.DIARY_TYPE_MAP.get(type), tag, relatedid, keyname, startTime,
				endTime);
		diaryList = diaryService.getDiaryListByKey(Diary.class, citycode, DiaryConstant.DIARY_TYPE_MAP.get(type), tag, relatedid, firstRow,
				rowsPerpage, keyname, startTime, endTime);
		if (pageNo == 0)
			topDiaryList = diaryService.getTopDiaryList(citycode, null, tag, false);
		diaryList.removeAll(topDiaryList);
		Map params = new HashMap();
		params.put("tag", tag);
		params.put("fromDate", DateUtil.formatDate(fromDate));
		params.put("toDate", DateUtil.formatDate(toDate));
		if (relatedid != null)
			params.put("relatedid", relatedid);
		params.put("type", new String[] { type });
		if (StringUtils.isNotBlank(keyname))
			params.put("keyname", new String[] { keyname });
		PageUtil pageUtil = new PageUtil(count, rowsPerpage, pageNo, "admin/blog/diaryList.xhtml");
		pageUtil.initPageInfo(params);
		model.put("pageUtil", pageUtil);
		model.put("topDiaryList", topDiaryList);
		model.put("diaryList", diaryList);
		model.put("diarytype", diarytype);
		// 关联城市
		model.put("citynameMapTopDiary", commonService.initRelateToCityName(topDiaryList, TagConstant.TAG_DIARY));
		model.put("citynameMapDiary", commonService.initRelateToCityName(diaryList, TagConstant.TAG_DIARY));
		// 查询帖子关联的哇啦
		initDiaryList(diaryList, model);
		model.put("cityMap", AdminCityContant.getCitycode2CitynameMap());
		return "admin/blog/diaryList.vm";
	}

	private void initDiaryList(List<Diary> diaryList, ModelMap model) {
		Map<Long, Long> cidMap = new HashMap<Long, Long>();
		for (Diary diary : diaryList) {
			List<Comment> list = commentService.getCommentList(TagConstant.TAG_DIARY, diary.getId(), diary.getMemberid(), null, null, 0, 1);
			List<Long> idList = BeanUtil.getBeanPropertyList(list, Long.class, "id", true);
			if (idList.size() > 0) {
				cidMap.put(diary.getId(), idList.get(0));
			}
		}
		model.put("cidMap", cidMap);
	}

	@RequestMapping("/admin/blog/getDiaryList.xhtml")
	public String getDiaryList(ModelMap model, Integer pageNo, String tag, String keyname, HttpServletRequest request) {
		if (StringUtils.isBlank(tag) || StringUtils.isBlank(keyname))
			return "admin/blog/searchDiaryList.vm";
		keyname = StringUtils.trim(keyname);
		String nameValue = "";
		if ("Drama".equals(tag))
			nameValue = "dramaname";
		else if ("SportItem".equals(tag))
			nameValue = "itemname";
		else {
			tag = "Movie";
			nameValue = "moviename";
		}
		String citycode = getAdminCitycode(request);
		if (pageNo == null)
			pageNo = 0;
		int rowsPerpage = 30;
		int firstRow = pageNo * rowsPerpage;
		int count = diaryService.getMDSDiaryCountByKeyname(citycode, keyname, tag, nameValue);
		List<Map> diaryListMap = diaryService.getMDSDiaryListByKeyname(citycode, keyname, tag, nameValue, firstRow, rowsPerpage);
		Map<Long, Member> memberMap = new HashMap<Long, Member>();
		String relate = "";
		if (diaryListMap.size() > 0) {
			Long categoryid = Long.parseLong(diaryListMap.get(0).get("categoryid") + "");
			if (tag.equals("Drama")) {
				Drama drama = daoService.getObject(Drama.class, categoryid);
				relate = drama.getDramaname();
			} else if (tag.equals("SportItem")) {
				SportItem sportItem = daoService.getObject(SportItem.class, categoryid);
				relate = sportItem.getItemname();
			} else {
				Movie movie = daoService.getObject(Movie.class, categoryid);
				relate = movie.getMoviename();
			}
		}
		for (Map diaryMap : diaryListMap) {
			Member member = daoService.getObject(Member.class, Long.parseLong(diaryMap.get("memberid") + ""));
			memberMap.put(Long.parseLong(diaryMap.get("id") + ""), member);
		}
		Map params = new HashMap();
		PageUtil pageUtil = new PageUtil(count, rowsPerpage, pageNo, "admin/blog/getDiaryList.xhtml");
		params.put("tag", tag);
		params.put("keyname", keyname);
		pageUtil.initPageInfo(params);
		model.put("pageUtil", pageUtil);
		model.put("memberMap", memberMap);
		model.put("relate", relate);
		model.put("diaryListMap", diaryListMap);
		return "admin/blog/searchDiaryList.vm";
	}

	@RequestMapping("/admin/blog/changeFlowerNum.xhtml")
	public String chanageFlowerNum(ModelMap model, Long diaryid, Integer flowernum) {
		DiaryBase diary = diaryService.getDiaryBase(diaryid);
		if (diary == null) {
			return showJsonError(model, "数据不存在！");
		}
		diary.setFlowernum(flowernum);
		daoService.saveObject(diary);
		return showJsonSuccess(model, diary.getFlowernum() + "");
	}

	// 显示帖子详细
	@RequestMapping("/admin/blog/topicDetail.xhtml")
	public String topicDetail(Long did, Integer pageNo, ModelMap model) {
		DiaryBase topic = diaryService.getDiaryBase(did);
		if (topic == null)
			return show404(model, "帖子不存在或正在审核或被删除！");
		if (Status.isHidden(topic.getStatus())) {
			return show404(model, "帖子正在审核或被删除！");
		}
		if (pageNo == null)
			pageNo = 0;
		int rowsPerpage = 30;
		int firstRow = pageNo * rowsPerpage;
		int diaryCount = diaryService.getDiaryCommentCount("", did);
		List<DiaryComment> commentList = diaryService.getDiaryCommentList(did, firstRow, rowsPerpage);
		model.put("commentList", commentList);
		model.put("firstRow", firstRow);
		Map params = new HashMap();
		PageUtil pageUtil = new PageUtil(diaryCount, rowsPerpage, pageNo, "admin/blog/topicDetail.xhtml");
		params.put("did", did);
		pageUtil.initPageInfo(params);
		model.put("pageUtil", pageUtil);
		return "admin/blog/topicDetail.vm";
	}

	// 导出回复帖子
	@RequestMapping("/admin/blog/exportTopic.xhtml")
	public String exportTopic(Long did, String ctype, ModelMap model, HttpServletResponse response) throws Exception {
		List<DiaryComment> commentList = new ArrayList<DiaryComment>();
		if (did != null) {
			commentList = diaryService.getDiaryCommentList(did);
		}
		if ("xls".equals(ctype)) {
			download("xls", response);
		}
		Collections.sort(commentList, new PropertyComparator("addtime", false, true));
		model.put("commentList", commentList);
		return "admin/blog/exportTopic.vm";
	}

	@RequestMapping("/admin/blog/banzhuList.xhtml")
	public String banzhuList(String tag, Long relatedid, ModelMap model) {
		model.put("tag", tag);
		if (relatedid != null)
			model.put("relatedid", relatedid);
		List<Bkmember> bkmemberList = blogService.getBkmemberList(tag, relatedid, 0, true, 0, 1000);
		model.put("bkmemberList", bkmemberList);
		addCacheMember(model, ServiceHelper.getMemberIdListFromBeanList(bkmemberList));
		Object relate = relateService.getRelatedObject(tag, relatedid);
		if (relate != null)
			model.put("relate", relate);
		if ("cinema".equals(tag) || "movie".equals(tag)) {
			model.put("ismovie", true);
		} else if ("gym".equals(tag) || "gymcourse".equals(tag)) {
			model.put("isgym", true);
		} else if ("ktv".equals(tag)) {
			model.put("isktv", true);
		} else if ("sport".equals(tag) || "sportservice".equals(tag)) {
			model.put("issport", true);
		} else if ("bar".equals(tag)) {
			model.put("isbar", true);
		}
		return "admin/blog/banzhuList.vm";
	}

	// 被删除的帖子
	@RequestMapping("/admin/blog/diaryListStatus.xhtml")
	public String diaryListStatus(ModelMap model, String keyname, Integer pageNo, Date fromDate, Date endDate) {
		String status = Status.N_DELETE;
		if (pageNo == null)
			pageNo = 0;
		int rowsPerpage = 20;
		int firstRow = pageNo * rowsPerpage;
		Integer count = diaryService.getDiaryCountByStatus(Diary.class, keyname, status, fromDate, endDate);
		List<Diary> diaryList = diaryService.getDiaryListByStatus(Diary.class, keyname, status, fromDate, endDate, firstRow, rowsPerpage);
		Map params = new HashMap();
		PageUtil pageUtil = new PageUtil(count, rowsPerpage, pageNo, "admin/blog/diaryListStatus.xhtml");
		params.put("keyname", keyname);
		params.put("fromDate", DateUtil.formatDate(fromDate));
		params.put("endDate", DateUtil.formatDate(endDate));
		pageUtil.initPageInfo(params);
		model.put("pageUtil", pageUtil);
		model.put("diaryList", diaryList);
		return "admin/blog/diaryListStatus.vm";
	}

	// 被删除帖子留言
	@RequestMapping("/admin/blog/diaryCommentListStatus.xhtml")
	public String diaryCommentListStatus(ModelMap model, String keyname, Integer pageNo, Date fromDate, Date endDate) {
		String status = Status.N_DELETE;
		if (pageNo == null)
			pageNo = 0;
		int rowsPerpage = 20;
		int firstRow = pageNo * rowsPerpage;
		Integer count = diaryService.getDiaryCommentCountByStatus(keyname, status, fromDate, endDate);
		List<DiaryComment> diaryCommentList = diaryService.getDiaryCommentListByStatus(keyname, status, fromDate, endDate, firstRow, rowsPerpage);
		Map params = new HashMap();
		PageUtil pageUtil = new PageUtil(count, rowsPerpage, pageNo, "admin/blog/diaryCommentListStatus.xhtml");
		params.put("keyname", keyname);
		params.put("fromDate", DateUtil.formatDate(fromDate));
		params.put("endDate", DateUtil.formatDate(endDate));
		pageUtil.initPageInfo(params);
		model.put("pageUtil", pageUtil);
		model.put("diaryCommentList", diaryCommentList);
		return "admin/blog/diaryCommentListStatus.vm";
	}

	@RequestMapping("/admin/blog/blackList.xhtml")
	public String blackList(String tag, String nickname, Integer pageNo, String isexport, HttpServletResponse response, ModelMap model,
			HttpServletRequest request) {
		if (StringUtils.isNotBlank(isexport) && StringUtils.equals(tag, "blacklist")) {
			List<BlackMember> blackExpList = blogService.getBlackMemberList(null, nickname, -1, -1);
			List<Long> idList = BeanUtil.getBeanPropertyList(blackExpList, Long.class, "memberId", true);
			List<Member> relateList = daoService.getObjectList(Member.class, idList);
			List<MemberInfo> infoList = daoService.getObjectList(MemberInfo.class, idList);
			List<Member> operatorList = daoService.getObjectList(Member.class, BeanUtil.getBeanPropertyList(blackExpList, Long.class, "operatorId", true));
			
			Map<Long, Member> memberMap = BeanUtil.beanListToMap(relateList, "id");
			memberMap.putAll(BeanUtil.beanListToMap(operatorList, "id"));
			
			model.put("memberMap", memberMap);
			model.put("infoMap", BeanUtil.beanListToMap(infoList, "id"));

			download("xls", response);
			model.put("blackExpList", blackExpList);
			return "admin/blog/exportBlackList.vm";
		}
		if (pageNo == null)
			pageNo = 0;
		int rowsPerpage = 20;
		int firstRow = pageNo * rowsPerpage;
		int count = 0;
		if (StringUtils.equals(tag, "blacklist")) {
			count = blogService.getBlackMembertCount(nickname);
		} else {
			count = memberService.searchMemberCount(nickname);
		}
		PageUtil pageUtil = new PageUtil(count, rowsPerpage, pageNo, "admin/blog/blackList.xhtml");
		pageUtil.initPageInfo(request.getParameterMap());
		model.put("pageUtil", pageUtil);

		List<Member> memberList = new ArrayList<Member>();
		List<BlackMember> blackMemberList = new ArrayList<BlackMember>();
		if (StringUtils.equals(tag, "blacklist")) {
			blackMemberList = blogService.getBlackMemberList(null, nickname, firstRow, rowsPerpage);
			List<Long> idList = BeanUtil.getBeanPropertyList(blackMemberList, Long.class, "memberId", true);
			List<Member> relateList = daoService.getObjectList(Member.class, idList);
			List<MemberInfo> infoList = daoService.getObjectList(MemberInfo.class, idList);
			List<Member> operatorList = daoService.getObjectList(Member.class, BeanUtil.getBeanPropertyList(blackMemberList, Long.class, "operatorId", true));

			Map<Long, Member> memberMap = BeanUtil.beanListToMap(relateList, "id");
			memberMap.putAll(BeanUtil.beanListToMap(operatorList, "id"));
			
			model.put("memberMap", memberMap);
			model.put("infoMap", BeanUtil.beanListToMap(infoList, "id"));

		} else {
			memberList = memberService.searchMember(nickname, 0, 2000);
		}
		model.put("memberList", memberList);
		model.put("tag", tag);
		model.put("blackMemberList", blackMemberList);
		return "admin/blog/blackList.vm";
	}

	@RequestMapping("/admin/blog/saveOrUpdateRelateship.xhtml")
	public String saveOrUpdateRelateship(String tag, String category, HttpServletRequest request, ModelMap model) throws Exception {
		if (StringUtils.isBlank(category) || StringUtils.isBlank(category) || StringUtils.isBlank(tag))
			return showJsonError(model, "参错误错！");
		Map<String, String[]> requestMap = request.getParameterMap();
		String relateObject = ServiceHelper.get(requestMap, "relatedid");
		if (relateObject == null)
			return showJsonError(model, "关联的对象不能为空！");
		Long relatedid = Long.valueOf(relateObject + "");
		Object baseObject = relateService.getRelatedObject(category, relatedid);
		if (baseObject == null)
			return showJsonError(model, "关联的对象不能为空！");
		List<Relationship> resList = new ArrayList<Relationship>();
		String cinemaid = ServiceHelper.get(requestMap, "cinemaid");
		if (StringUtils.isNotBlank(cinemaid)) {
			for (String string : Arrays.asList(cinemaid.split(","))) {
				Relationship relationship = new Relationship(category, tag, relatedid, Long.valueOf(string));
				resList.add(relationship);
			}
			if (!resList.isEmpty()) {
				List<Relationship> reList = commonService.getRelationshipList(category, relatedid, tag, null, null, 1, 100);
				daoService.removeObjectList(reList);
				daoService.saveObjectList(resList);
				return showJsonSuccess(model);
			}
			return showJsonSuccess(model, "没有更新的数据！");
		} else
			return showJsonError(model, "没有选择关联数据！");
	}

	// 帖子详细
	@RequestMapping("/admin/blog/diaryDetail.xhtml")
	public String diaryDetail(Long did, ModelMap model) {
		DiaryBase diary = diaryService.getDiaryBase(did);
		model.put("diary", diary);
		return "admin/blog/diaryDetail.vm";
	}

	// 帖子,帖子回复数,被删除的主帖数、被删除的跟帖数
	@RequestMapping("/admin/blog/report/diary.xhtml")
	public String getDiaryReportList(ModelMap model, Date datefrom, Date dateto) {
		if (datefrom == null || dateto == null)
			return "admin/blog/report/diary.vm";
		String counthql = "select count(t.id) from Diary t where t.addtime>=? and t.addtime<=? and t.status like ?";
		String membercounhql = "select count(distinct t.memberid)  from Diary t where t.addtime>=? and t.addtime<=? and t.status like ?";
		String countcommenthql = "select count(t.id) from DiaryComment t where t.addtime>=? and t.addtime<=? and t.status like ?";
		String membercommenthql = "select count(distinct t.memberid ) from DiaryComment t where t.addtime>=? and t.addtime<=? and t.status like ?";
		String diaryDelhql = "select count(t.id) from Diary t where t.addtime>=? and t.addtime<=? and t.status like ?";
		String commentDelhql = "select count(t.id) from DiaryComment t where t.addtime>=? and t.addtime<=? and t.status like ?";
		List diaryList = hibernateTemplate.find(counthql, DateUtil.getBeginningTimeOfDay(datefrom), DateUtil.getLastTimeOfDay(dateto), "Y%");
		List diaryMemberList = hibernateTemplate.find(membercounhql, DateUtil.getBeginningTimeOfDay(datefrom), DateUtil.getLastTimeOfDay(dateto),
				"Y%");
		List commentList = hibernateTemplate.find(countcommenthql, DateUtil.getBeginningTimeOfDay(datefrom), DateUtil.getLastTimeOfDay(dateto), "Y%");
		List memberCommentList = hibernateTemplate.find(membercommenthql, DateUtil.getBeginningTimeOfDay(datefrom),
				DateUtil.getLastTimeOfDay(dateto), "Y%");
		List diaryDelList = hibernateTemplate.find(diaryDelhql, DateUtil.getBeginningTimeOfDay(datefrom), DateUtil.getLastTimeOfDay(dateto), "N%");
		List commentDelList = hibernateTemplate
				.find(commentDelhql, DateUtil.getBeginningTimeOfDay(datefrom), DateUtil.getLastTimeOfDay(dateto), "N%");
		model.put("diary", diaryList.get(0));
		model.put("diaryMember", diaryMemberList.get(0));
		model.put("comment", commentList.get(0));
		model.put("commentMember", memberCommentList.get(0));
		model.put("diaryDel", diaryDelList.get(0));
		model.put("commentDel", commentDelList.get(0));
		return "admin/blog/report/diary.vm";
	}

	// ktv,bar,sport,gym,cinema的点评数量和点评被删除数
	@RequestMapping("/admin/blog/report/comment.xhtml")
	public String commentReportList(ModelMap model, Date datefrom, Date dateto) {
		if (datefrom == null || dateto == null)
			return "admin/blog/report/comment.vm";
		List commentList = new ArrayList();
		List commentDelList = new ArrayList();
		String[] tmp = { "cinema", "bar", "ktv", "sport", "gym" };
		for (int i = 0; i < tmp.length; i++) {
			commentList.add(commentService.getCommentCount(tmp[i], null, null, "Y", null, null, null, DateUtil.getBeginTimestamp(datefrom),
					DateUtil.getEndTimestamp(dateto)));
			commentDelList.add(commentService.getCommentCount(tmp[i], null, null, "N", null, null, null, DateUtil.getBeginTimestamp(datefrom),
					DateUtil.getEndTimestamp(dateto)));
		}
		model.put("commentList", commentList);
		model.put("commentDelList", commentDelList);
		return "admin/blog/report/comment.vm";
	}

	/**
	 * 版主管理
	 */
	@RequestMapping("/admin/blog/moderators.xhtml")
	public String moderators(String tag, Long relatedid, ModelMap model) {
		if (StringUtils.isBlank(tag)) {
			tag = "customer"; // 论坛用户意见/投诉版块专用
		}
		List<Bkmember> bkmemberList = blogService.getBkmemberList(tag, relatedid, 0, true, 0, 50);
		model.put("bkmemberList", bkmemberList);
		return "admin/blog/moderators.vm";
	}

	@RequestMapping("/admin/blog/saveModerators.xhtml")
	public String saveModerators(ModelMap model, Long memberid) {
		Member member = daoService.getObject(Member.class, memberid);
		if (member == null)
			return showJsonError_DATAERROR(model);
		Bkmember bkmember = new Bkmember("customer");
		bkmember.setMemberid(member.getId());
		bkmember.setRole(Bkmember.ROLE_BANZHU);
		daoService.saveObject(bkmember);
		return showJsonSuccess(model);
	}

	@RequestMapping("/admin/blog/delModerators.xhtml")
	public String delModerators(ModelMap model, Long id) {
		Bkmember bkmember = daoService.getObject(Bkmember.class, id);
		if (bkmember == null)
			return showJsonError_DATAERROR(model);
		daoService.removeObject(bkmember);
		return showJsonSuccess(model);
	}

	/**
	 * 提建议
	 **/
	@RequestMapping("/admin/sns/customerQList.xhtml")
	public String customerQList() {
		return "admin/blog/customerQ.vm";
	}

	/**
	 * 提建议常用语
	 */
	@RequestMapping("/admin/sns/customerBackList.xhtml")
	public String customerBackList(ModelMap model) {
		List<Map> backList = mongoService.getMapList(MongoData.NS_KEFU_REPLYTEMPLATE);
		model.put("backList", backList);
		return "admin/blog/customerBackList.vm";
	}

	@RequestMapping("/admin/blog/saveCustomerBack.xhtml")
	public String saveCustomerBack(String body, ModelMap model) {
		if (StringUtils.isBlank(body))
			return showJsonError(model, "模版内容不能为空！");
		Map params = new HashMap();
		params.put(MongoData.SYSTEM_ID, MongoData.buildId());
		params.put(MongoData.DEFAULT_ID_NAME, "reply" + System.currentTimeMillis());
		params.put(MongoData.ACTION_BODY, body);
		mongoService.saveOrUpdateMap(params, MongoData.DEFAULT_ID_NAME, MongoData.NS_KEFU_REPLYTEMPLATE);
		return showJsonSuccess(model);
	}

	@RequestMapping("/admin/blog/delCustomerBack.xhtml")
	public String delCustomerBack(String id, ModelMap model) {
		Map params = new HashMap();
		params.put(MongoData.DEFAULT_ID_NAME, id);
		mongoService.removeObjectList(MongoData.NS_KEFU_REPLYTEMPLATE, params);
		return showJsonSuccess(model);
	}

	// 按分类加载
	@RequestMapping("/admin/blog/customerQTable.xhtml")
	public String customerQTable(String tag, String status, String searchkey, Integer pageNo, ModelMap model, HttpServletRequest request,
			HttpServletResponse response, String xls) {
		String citycode = WebUtils.getAndSetDefault(request, response);
		if (pageNo == null)
			pageNo = 0;
		int rowsPerPage = 50;
		int firstRow = pageNo * rowsPerPage;
		List<CustomerQuestion> qList = customerQuestionService.getQuestionsBykey(null, tag, searchkey, status, firstRow, rowsPerPage);
		Integer count = customerQuestionService.getQuestionCountBykey(citycode, tag, searchkey, status);

		PageUtil pageUtil = new PageUtil(count, rowsPerPage, pageNo, "/admin/blog/customerQTable.xhtml");
		Map params = new HashMap();
		params.put("searchkey", searchkey);
		params.put("status", status);
		params.put("tag", tag);
		pageUtil.initPageInfo(params);
		model.put("pageUtil", pageUtil);
		model.put("questionList", qList);
		Map<Long, String> mapbody = new HashMap<Long, String>();
		Map<Long, CustomerAnswer> answerMap = new HashMap<Long, CustomerAnswer>();
		for (CustomerQuestion gq : qList) {
			mapbody.put(gq.getId(), blogService.getDiaryBody(gq.getId()));
			List<CustomerAnswer> list = customerQuestionService.getAnswersByQid(gq.getId(), 0, 1);
			if (list.size() > 0) {
				CustomerAnswer customerAnswer = list.get(0);
				if (customerAnswer != null)
					answerMap.put(gq.getId(), customerAnswer);
			}
		}
		addCacheMember(model, ServiceHelper.getMemberIdListFromBeanList(qList));
		model.put("mapbody", mapbody);
		if (StringUtils.equals(xls, "xls")) {
			download("xls", response);
		}
		model.put("answerMap", answerMap);
		return "admin/blog/customerQtable.vm";
	}

	@RequestMapping("/admin/blog/updateQuestionStatus.xhtml")
	public String updateQuestionStatus(Long qid, String status, ModelMap model) {
		CustomerQuestion customerQuestion = daoService.getObject(CustomerQuestion.class, qid);
		customerQuestion.setStatus(status);
		daoService.saveObject(customerQuestion);
		return showJsonSuccess(model);
	}

	@RequestMapping("/admin/blog/customerQDetail.xhtml")
	public String customerQDetail(@RequestParam("qid") Long qid, ModelMap model) {
		CustomerQuestion customerQuestion = daoService.getObject(CustomerQuestion.class, qid);
		List<Map> backList = mongoService.getMapList(MongoData.NS_KEFU_REPLYTEMPLATE);
		model.put("customerQuestion", customerQuestion);
		addCacheMember(model, customerQuestion.getMemberid());
		model.put("backList", backList);
		return "admin/blog/customerQDetail.vm";
	}

	@RequestMapping("/admin/blog/customerAnswerList.xhtml")
	public String customerAnswerList(@RequestParam("qid") Long qid, Integer pageNo, ModelMap model) {
		CustomerQuestion customerQuestion = daoService.getObject(CustomerQuestion.class, qid);
		if (customerQuestion == null)
			return showJsonError(model, "该反馈建议不存在！");
		if (pageNo == null)
			pageNo = 0;
		int rowsPerpage = 15;
		int firstRow = pageNo * rowsPerpage;
		Integer count = customerQuestionService.getAnswerCountByQid(qid);
		List<CustomerAnswer> answerList = customerQuestionService.getAnswersByQid(qid, firstRow, rowsPerpage);
		model.put("answerList", answerList);
		PageUtil pageUtil = new PageUtil(count, rowsPerpage, pageNo, "/admin/blog/customerAnswerList.xhtml");
		Map params = new HashMap();
		params.put("qid", qid);
		pageUtil.initPageInfo(params);
		model.put("pageUtil", pageUtil);
		return "admin/blog/customerAnswerList.vm";
	}

	@RequestMapping("/admin/blog/saveCustomerAnser.xhtml")
	public String saveCustomerAnser(@RequestParam("qid") Long qid, Long aid, String body, ModelMap model, HttpServletRequest request,
			String feedbackType) {
		User user = getLogonUser();
		CustomerQuestion customerQuestion = daoService.getObject(CustomerQuestion.class, qid);
		if (customerQuestion == null)
			return showJsonError(model, "该反馈建议不存在！");
		if (StringUtils.isBlank(body))
			return showJsonError(model, "回复内容不能为空！");
		CustomerAnswer answer = null;
		if (aid == null) {
			answer = new CustomerAnswer(customerQuestion.getId(), user.getId(), body);
		} else {
			answer = daoService.getObject(CustomerAnswer.class, aid);
			answer.setBody(body);
		}
		answer.setIsAdmin(CustomerAnswer.IS_ADMIN);
		answer.setCitycode(getAdminCitycode(request));
		answer.setNickname(user.getNickname());
		daoService.saveObject(answer);
		customerQuestion.setFeedbackType(feedbackType);
		customerQuestion.setUpdatetime(DateUtil.getCurFullTimestamp());
		customerQuestion.setStatus(CustomerQuestion.Y_TREAT);
		daoService.updateObject(customerQuestion);
		if (customerQuestion.getId() != null) {
			userMessageService.sendSiteMSG(customerQuestion.getMemberid(), "result", customerQuestion.getId(),
					"你提交的反馈客服已回复，点此<a href='" + config.getBasePath() + "home/acct/customerDetail.xhtml?qid=" + qid
							+ "' class='ml5' target='_blank'>查看</a>");
		}
		if(ValidateUtil.isEmail(customerQuestion.getEmail())){
			Map map = new HashMap();
			map.put("nickname", customerQuestion.getMembername());
			map.put("body", body);
			map.put("email", customerQuestion.getEmail());
			String jsonData = JsonUtils.writeObjectToJson(map);
			SendMailByOutboxRequest sendMailRequest = new SendMailByOutboxRequest(SendMailByOutboxRequest.OUTBOX_SERVICE,EmailRecord.SENDER_GEWARA,
					customerQuestion.getEmail().replaceAll(",", ";"),"反馈建议信息回复",jsonData,"mail/adviseEmail.vm",new Timestamp(System.currentTimeMillis()));
			SendMailByOutboxResponse response = gmailService.sendMailByOutbox(sendMailRequest);
			if(response.isSuccess()){
				return showJsonSuccess(model);
			}else{
				return showJsonError(model, response.getMsg());
			}
		}
		//gewaMailService.sendAdviseEmail(customerQuestion.getMembername(), body, customerQuestion.getEmail());
		return showJsonSuccess(model);
	}

	// 查询系统消息
	@RequestMapping("/admin/blog/systemMessageReportList.xhtml")
	public String uSerMessageList(ModelMap model, Long memberid, Integer pageNo) {
		if (memberid == null)
			return "admin/blog/sysMessageListReport.vm";
		if (pageNo == null)
			pageNo = 0;
		int rowsPerPage = 10;
		int firstPerPage = pageNo * rowsPerPage;
		Map<Long, Member> memberMap = new HashMap<Long, Member>();
		List<SysMessageAction> sysMessageActionList = userMessageService.getSysMsgListByMemberid(memberid, "", firstPerPage, rowsPerPage);
		int count = userMessageService.getSysMsgCountByMemberid(memberid, "");
		for (SysMessageAction uma : sysMessageActionList) {
			memberMap.put(uma.getId(), daoService.getObject(Member.class, uma.getTomemberid()));
		}
		model.put("memberMap", memberMap);
		PageUtil pageUtil = new PageUtil(count, rowsPerPage, pageNo, "/admin/blog/systemMessageReportList.xhtml");
		Map params = new HashMap();
		params.put("memberid", memberid);
		pageUtil.initPageInfo(params);
		model.put("pageUtil", pageUtil);
		model.put("sysMessageActionList", sysMessageActionList);
		return "admin/blog/sysMessageListReport.vm";
	}

	// 查询站内信(收件箱)
	@RequestMapping("/admin/blog/receiveMessageListReport.xhtml")
	public String receiveMessageList(ModelMap model, Long memberid, Integer pageNo) {
		if (memberid == null)
			return "admin/blog/receiveMessageListReport.vm";
		if (pageNo == null)
			pageNo = 0;
		int rowsPerPage = 10;
		int firstPerPage = pageNo * rowsPerPage;
		Map<Long, Member> memberMap = new HashMap<Long, Member>();
		Map<Long, UserMessage> userMessageMap = new HashMap<Long, UserMessage>();
		int count = userMessageService.getReceiveUserMessageCountByMemberid(memberid, null);
		List<UserMessageAction> receiveMessageActionList = userMessageService.getReceiveUserMessageListByMemberid(memberid, null, firstPerPage,
				rowsPerPage);
		for (UserMessageAction uma : receiveMessageActionList) {
			memberMap.put(uma.getId(), daoService.getObject(Member.class, uma.getTomemberid()));
			userMessageMap.put(uma.getId(), daoService.getObject(UserMessage.class, uma.getUsermessageid()));
		}
		model.put("userMessageMap", userMessageMap);
		model.put("memberMap", memberMap);
		PageUtil pageUtil = new PageUtil(count, rowsPerPage, pageNo, "/admin/blog/receiveMessageListReport.xhtml");
		Map params = new HashMap();
		params.put("memberid", memberid);
		pageUtil.initPageInfo(params);
		model.put("pageUtil", pageUtil);
		model.put("receiveMessageActionList", receiveMessageActionList);
		return "admin/blog/receiveMessageListReport.vm";
	}

	// 查询站内信(发件箱)
	@RequestMapping("/admin/blog/sendMessageListReport.xhtml")
	public String sendMessageList(ModelMap model, Long memberid, Integer pageNo) {
		if (memberid == null)
			return "admin/blog/sendMessageListReport.vm";
		if (pageNo == null)
			pageNo = 0;
		int rowsPerPage = 10;
		int firstPerPage = pageNo * rowsPerPage;
		Map<Long, Member> memberMap = new HashMap<Long, Member>();
		Map<Long, UserMessage> userMessageMap = new HashMap<Long, UserMessage>();
		int count = userMessageService.getSendUserMessageCountByMemberid(memberid);
		List<UserMessageAction> sendMessageActionList = userMessageService.getSendUserMessageListByMemberid(memberid, firstPerPage, rowsPerPage);
		for (UserMessageAction uma : sendMessageActionList) {
			memberMap.put(uma.getId(), daoService.getObject(Member.class, uma.getTomemberid()));
			userMessageMap.put(uma.getId(), daoService.getObject(UserMessage.class, uma.getUsermessageid()));
		}
		model.put("userMessageMap", userMessageMap);
		model.put("memberMap", memberMap);
		PageUtil pageUtil = new PageUtil(count, rowsPerPage, pageNo, "/admin/blog/sendMessageListReport.xhtml");
		Map params = new HashMap();
		params.put("memberid", memberid);
		pageUtil.initPageInfo(params);
		model.put("pageUtil", pageUtil);
		model.put("sendMessageActionList", sendMessageActionList);
		return "admin/blog/sendMessageListReport.vm";
	}

	// 迪士尼修改的统一报表
	@RequestMapping("/admin/blog/activityReport.xhtml")
	public String activityReport(ModelMap model, String isXls, String tag, String activity, Long relatedid, HttpServletResponse res) {
		if (relatedid == null)
			relatedid = 47145677L;
		if (StringUtils.isBlank(tag))
			tag = "drama";
		if (StringUtils.isBlank(activity))
			activity = "mickey";
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("activity", activity);
		List<Map> dataMap = mongoService.find(MongoData.NS_DISNEY_JOIN, params);
		Map<Object, Member> memberMap = new HashMap<Object, Member>();
		Map<Object, String> mobileMap = new HashMap<Object, String>();
		Map<Object, Map> dataInfoMap = new HashMap<Object, Map>();
		Map<Object, List<Prize>> prizeListMap = new HashMap<Object, List<Prize>>();
		Map infoData = null;
		DrawActivity da = daoService.getObjectByUkey(DrawActivity.class, "tag", activity, true);
		if (da == null)
			return show404(model, "活动参数出错！");
		List<Prize> prizeList = drawActivityService.getPrizeListByDid(da.getId(), new String[] { "A", "D", "P", Prize.PRIZE_TYPE_DRAMA,
				Prize.PRIZE_REMARK });
		List<Long> prizeIdList = BeanUtil.getBeanPropertyList(prizeList, Long.class, "id", true);
		for (Map data : dataMap) {
			memberMap.put(data.get("memberid"), daoService.getObject(Member.class, new Long(data.get("memberid") + "")));
			String mobile = getMobile(tag, Long.valueOf(data.get("memberid") + ""), relatedid);
			mobileMap.put(data.get("memberid"), mobile);
			infoData = mongoService.findOne(MongoData.NS_DISNEY_MEMBER, "memberid", new Long(data.get("memberid").toString()));
			dataInfoMap.put(data.get("memberid"), infoData);
			List<WinnerInfo> winnerList = drawActivityService.getWinnerList(da.getId(), prizeIdList, null, null, "",
					Long.valueOf(data.get("memberid") + ""), "", "", 0, 30);
			List<Long> idList = BeanUtil.getBeanPropertyList(winnerList, Long.class, "prizeid", true);
			prizeListMap.put(data.get("memberid"), daoService.getObjectList(Prize.class, idList));
		}
		model.put("dataInfoMap", dataInfoMap);
		model.put("dataMap", dataMap);
		model.put("memberMap", memberMap);
		model.put("mobileMap", mobileMap);
		model.put("prizeListMap", prizeListMap);
		model.put("da", da);
		if (StringUtils.isNotBlank(isXls)) {
			download("xls", res);
		}
		return "admin/blog/activityReport.vm";
	}

	private String getMobile(String tag, Long memberid, Long relatedid) {
		String query = "";
		if (StringUtils.equals(tag, "cinema"))
			query = "select mobile from TicketOrder where memberid=? and movieid=?";
		if (StringUtils.equals(tag, "drama"))
			query = "select mobile from DramaOrder where memberid=? and dramaid=?";
		List<String> result = hibernateTemplate.find(query, memberid, relatedid);
		if (result.size() > 0)
			return result.get(0);
		return "";
	}

	/**
	 * 后台显示生活列表
	 * 
	 * @param model
	 * @param pageNo
	 * @param status
	 * @param type
	 * @param fromDate
	 * @param toDate
	 * @param keyName
	 * @return
	 */
	@RequestMapping("/admin/blog/agendaList.xhtml")
	public String agendaList(ModelMap model, Integer pageNo, String status, String type, Date fromDate, Date toDate, String keyName) {
		if (pageNo == null)
			pageNo = 0;
		int rowsPerpage = 20;
		int firstRow = pageNo * rowsPerpage;
		int rowCount = 0;
		List<Agenda> agendaList = null;
		agendaList = this.agendaService.getAgendaList(status, firstRow, rowsPerpage, fromDate, toDate, keyName);
		rowCount = this.agendaService.getAgendaListCount(status, fromDate, toDate, keyName);
		PageUtil pageUtil = new PageUtil(rowCount, rowsPerpage, pageNo, "/admin/blog/agendaList.xhtml");
		Map params = new HashMap();
		params.put("fromDate", DateUtil.formatDate(fromDate));
		params.put("toDate", DateUtil.formatDate(toDate));
		params.put("type", type);
		if (StringUtils.isNotBlank(keyName))
			params.put("keyName", new String[] { keyName });
		pageUtil.initPageInfo(params);
		model.put("agendaList", agendaList);
		model.put("pageUtil", pageUtil);
		return "admin/blog/agendaList.vm";
	}

	/**
	 * 生活列表相关的邀请信息
	 * 
	 * @param model
	 * @param recordid
	 * @return
	 */
	@RequestMapping("/admin/blog/agendaJoin.xhtml")
	public String agendaJoin(ModelMap model, Long recordid) {
		List<SMSRecord> smsList = this.agendaService.getFriendListFromSMS(recordid);
		model.put("smsList", smsList);
		return "admin/blog/agendaJoin.vm";
	}
}