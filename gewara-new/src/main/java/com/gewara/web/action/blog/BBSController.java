package com.gewara.web.action.blog;

import java.util.ArrayList;
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
import org.springframework.web.bind.annotation.RequestParam;

import com.gewara.constant.DiaryConstant;
import com.gewara.constant.Status;
import com.gewara.constant.TagConstant;
import com.gewara.constant.sys.JsonDataKey;
import com.gewara.json.bbs.MarkCountData;
import com.gewara.model.bbs.Bkmember;
import com.gewara.model.bbs.Diary;
import com.gewara.model.bbs.DiaryBase;
import com.gewara.model.bbs.DiaryComment;
import com.gewara.model.bbs.DiaryHist;
import com.gewara.model.bbs.VoteOption;
import com.gewara.model.bbs.commu.Commu;
import com.gewara.model.bbs.commu.CommuTopic;
import com.gewara.model.common.BaseInfo;
import com.gewara.model.common.County;
import com.gewara.model.drama.Drama;
import com.gewara.model.drama.Theatre;
import com.gewara.model.movie.Cinema;
import com.gewara.model.movie.Movie;
import com.gewara.model.sport.Sport;
import com.gewara.model.sport.SportItem;
import com.gewara.model.user.Member;
import com.gewara.model.user.MemberInfo;
import com.gewara.service.JsonDataService;
import com.gewara.service.OperationService;
import com.gewara.service.PlaceService;
import com.gewara.service.bbs.MarkService;
import com.gewara.service.drama.OpenDramaService;
import com.gewara.support.ErrorCode;
import com.gewara.support.ServiceHelper;
import com.gewara.untrans.CommentService;
import com.gewara.untrans.CommonService;
import com.gewara.untrans.activity.SynchActivityService;
import com.gewara.untrans.impl.ControllerService;
import com.gewara.util.BeanUtil;
import com.gewara.util.JsonUtils;
import com.gewara.util.RelatedHelper;
import com.gewara.util.WebUtils;
import com.gewara.web.util.PageUtil;
import com.gewara.xmlbind.activity.RemoteActivity;
import com.gewara.xmlbind.gym.RemoteCourse;
import com.gewara.xmlbind.gym.RemoteGym;

@Controller
public class BBSController extends BBSBaseController {
	@Autowired@Qualifier("synchActivityService")
	private SynchActivityService synchActivityService;
	public void setActivityRemoteService(SynchActivityService synchActivityService) {
		this.synchActivityService = synchActivityService;
	}
	@Autowired@Qualifier("markService")
	private MarkService markService;
	public void setMarkService(MarkService markService) {
		this.markService = markService;
	}
	@Autowired@Qualifier("commentService")
	private CommentService commentService;
	public void setCommentService(CommentService commentService) {
		this.commentService = commentService;
	}
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
	@Autowired@Qualifier("jsonDataService")
	private JsonDataService jsonDataService;
	public void setJsonDataService(JsonDataService jsonDataService) {
		this.jsonDataService = jsonDataService;
	}
	@Autowired@Qualifier("openDramaService")
	private OpenDramaService openDramaService;
	public void setOpenDramaService(OpenDramaService openDramaService) {
		this.openDramaService = openDramaService;
	}
	@Autowired@Qualifier("controllerService")
	private ControllerService controllerService;
	public void setControllerService(ControllerService controllerService) {
		this.controllerService = controllerService;
	}

	/**
	 * @param tag
	 * @param relatedid
	 * @param type:
	 *           topic, see Diary.DIARY_TYPE_MAP or a: activity
	 * @param model
	 * @return
	 * @throws Exception
	 */
	@RequestMapping("/blog/bbs.xhtml")
	public String bbs(@CookieValue(value = LOGIN_COOKIE_NAME, required = false)
			String sessid, HttpServletRequest request, String tag, String searchkey, Long relatedid, String type, Integer pageNo, ModelMap model, HttpServletResponse response) throws Exception {
			String citycode = WebUtils.getAndSetDefault(request, response);
		if (StringUtils.isBlank(tag)) {
			//什么都不做
		} else if (tag.equals("movie") || tag.equals("cinema")) {
			List<Movie> movieList = mcpService.getCurMovieListByMpiCount(citycode, 0, 8);
			model.put("movieList", movieList);
			List<Cinema> cinemaList = placeService.getPlaceList(citycode, Cinema.class, "clickedtimes", false, 0, 8);
			model.put("cinemaList", cinemaList);
		} else if (tag.equals("gym") || tag.equals("gymcourse")) {
			ErrorCode<List<RemoteGym>> gymCode = synchGymService.getGymList(citycode, null, null, "clickedtimes", false, 0, 8);
			if(gymCode.isSuccess()) model.put("gymList", gymCode.getRetval());
			ErrorCode<List<RemoteCourse>> code2 = synchGymService.getCourseListByOrder("clickedtimes", false, 0, 15);
			if(code2.isSuccess())model.put("courseList", code2.getRetval());
		} else if (tag.equals("sport") || tag.equals("sportservice")) {
			List<Sport> sportList = placeService.getPlaceList(citycode, Sport.class, "clickedtimes", false, 0, 8);
			model.put("sportList", sportList);
			List<SportItem> sportItemList = sportService.getHotSportItemList(0, 15);
			model.put("sportItemList", sportItemList);
		} else if (tag.equals("drama") || tag.equals("theatre")) {
			List<Drama> dramaList = openDramaService.getCurPlayDrama(citycode, 0, 8);
			model.put("dramaList", dramaList);
			List<Theatre> theatreList = placeService.getPlaceList(citycode, Theatre.class, "clickedtimes", false, 0, 8);
			model.put("theatreList", theatreList);
		}
		Integer bwcount = 0, activitycount = 0;
		List<Map> hotTopicList = diaryService.getOneDayHotDiaryList(citycode, null); // 24小时热帖排行
		model.put("hotTopicList", hotTopicList);
		// 一周活跃用户, 实际上取3天的活跃用户
		List<Map> memberList = commentService.getActiveMemberList(9);
		model.put("memberList", memberList);
		// 整理tag，relatedid
		if (StringUtils.isBlank(type))
			type = "0"; // all topic
		model.put("type", type);
		String tmpUrl = "blog";
		if (StringUtils.isNotBlank(tag)) {
			if (relatedid != null) {
				model.put("tag", tag);
				model.put("relatedid", relatedid);
				Object relate = relateService.getRelatedObject(tag, relatedid);
				if (relate != null)
					model.put("relate", relate);
				if (tag.equals("gymcourse") || tag.equals("movie"))
					model.put("categoryid", relatedid);
			} else {
				model.put("tag", ServiceHelper.getTag(tag));
			}
			tmpUrl += "/" + tag;
		}
		int rowsPerPage = 50;
		if (pageNo == null) pageNo = 0;
		model.putAll(getReferenceDataByPageNo(type, tag, searchkey, relatedid, rowsPerPage, pageNo, citycode,model));
		PageUtil pageUtil = null;
		bwcount = diaryService.getDiaryCountByKey(Diary.class, citycode, DiaryConstant.DIARY_TYPE_MAP.get(type), tag, relatedid, searchkey, null, null);
		pageUtil = new PageUtil(bwcount, rowsPerPage, pageNo, tmpUrl, true, true);
		Map params = new HashMap();
		params.put("type", type);
		params.put("searchkey", searchkey);
		if (relatedid != null)
			params.put("relatedid", relatedid);
		pageUtil.initPageInfo(params);
		model.put("pageUtil", pageUtil);
		model.put("bwcount", bwcount);
		model.put("activitycount", activitycount);
		// 板块设置
		Member member = loginService.getLogonMemberBySessid(WebUtils.getRemoteIp(request), sessid);
		Integer permission = blogService.getMaxRights(tag, relatedid, null, member);
		model.put("permission", permission);
		// 版主
		List<Bkmember> banzhuList = blogService.getBanzhuList(tag, relatedid);
		model.put("banzhuList", banzhuList);
		addCacheMember(model, ServiceHelper.getMemberIdListFromBeanList(banzhuList));
		return "blog/topicList.vm";
	}

	private Map getReferenceDataByPageNo(String type, String tag, String searchkey, Long relatedid, int rowsPerpage, Integer pageNo, String citycode ,ModelMap model) {
		if (pageNo == null)
			pageNo = 0;
		int firstRow = pageNo * rowsPerpage;
		List<Diary> topTopicList = new ArrayList<Diary>();
		if (pageNo == 0 && relatedid == null) {
			List<RemoteActivity> topActivityList = new ArrayList();
			ErrorCode<List<RemoteActivity>> code = synchActivityService.getTopActivityList(citycode, null, tag, relatedid);
			if(code.isSuccess()) topActivityList = code.getRetval();
			RelatedHelper rh = new RelatedHelper(); 
			model.put("relatedHelper", rh);
			controllerService.initRelate("topActivityList", rh, topActivityList);
			model.put("topActivityList", topActivityList);
			topTopicList = diaryService.getTopDiaryList(citycode, null, tag, true);
		}
		if (!("a".equals(type) || "review".equals(type))) {
			List<Diary> topicList = diaryService.getDiaryListByKey(Diary.class, citycode, DiaryConstant.DIARY_TYPE_MAP.get(type), tag, relatedid, firstRow, rowsPerpage, searchkey, null, null);
			topicList.removeAll(topTopicList);// 除去置顶项目
			model.put("topTopicList", topTopicList);
			model.put("topicList", topicList);
			addCacheMember(model, ServiceHelper.getMemberIdListFromBeanList(topicList));
		}
		addCacheMember(model, ServiceHelper.getMemberIdListFromBeanList(topTopicList));
		return model;
	}

	// 显示帖子详细
	@RequestMapping("/blog/topicDetail.xhtml")
	public String topicDetail(@CookieValue(value = LOGIN_COOKIE_NAME, required = false)
			String sessid, HttpServletRequest request, @RequestParam("did")
			Long did, Integer pageNo, ModelMap model, HttpServletResponse response) {
		DiaryBase topic = daoService.addPropertyNum(Diary.class, did, "clickedtimes", 1);
		if(topic == null) topic = daoService.addPropertyNum(DiaryHist.class, did, "clickedtimes", 1);
		if (topic == null) {
			return show404(model, "帖子不存在或正在审核或被删除！");
		}
		Member member = loginService.getLogonMemberBySessid(WebUtils.getRemoteIp(request), sessid);
		if (Status.isHidden(topic.getStatus())){
			return show404(model, "帖子正在审核或被删除！");
		}
		Map<Long, Boolean> isTreasureMap = new HashMap<Long, Boolean>();
		Map<Long, MarkCountData> markCountMap = new HashMap<Long, MarkCountData>();
		model.put("topicmemberJobs", memberService.getMemberJobsInfo(topic.getMemberid()));
		Object relate = null;
		model.put("topic", topic);
		addCacheMember(model, topic.getMemberid());
		if (pageNo == null)
			pageNo = 0;
		int rowsPerpage = 30;
		int firstRow = pageNo * rowsPerpage;
		List<DiaryComment> commentList = diaryService.getDiaryCommentList(did, firstRow, rowsPerpage);
		Map mapCommentMember = new HashMap();
		for (DiaryComment diaryComment : commentList) {
			mapCommentMember.put(diaryComment.getMemberid(), memberService.getMemberJobsInfo(diaryComment.getMemberid()));
		}
		model.put("mapCommentMember", mapCommentMember);
		model.put("commentList", commentList);
		model.put("firstRow", firstRow);
		List<Long> midList = ServiceHelper.getMemberIdListFromBeanList(commentList);
		addCacheMember(model, midList);
		Map<Long, MemberInfo> signMap = daoService.getObjectMap(MemberInfo.class, midList);
		model.put("signMap", signMap);
		if (topic.getReplycount() > rowsPerpage) {
			PageUtil pageUtil = new PageUtil(topic.getReplycount(), rowsPerpage, pageNo, "blog/t" + did, true, true);
			Map params = new HashMap();
			pageUtil.initPageInfo(params);
			model.put("pageUtil", pageUtil);
		}
		String citycode = WebUtils.getAndSetDefault(request, response);
		// 24小时热帖排行
		List<Map> hotTopicList = diaryService.getOneDayHotDiaryList(citycode, null);
		model.put("hotTopicList", hotTopicList);
		if (member != null) {
			model.put("logonMember", member);
			if (member.getId().equals(topic.getMemberid())) {
				topic.setViewed(true);
				model.put("owner", true);
			}
			int maxRights = blogService.getMaxRights(topic.getDtag(), topic.getDrelatedid(), null, member);
			model.put("permission", maxRights);
		} else {
			model.put("permission", 0);
		}
		// 该用户发表的其他帖子
		List<Diary> memberTopicList = diaryService.getDiaryListByMemberid(Diary.class, DiaryConstant.DIARY_TYPE_ALL, null, topic.getMemberid(), 0, 10);
		if (!memberTopicList.isEmpty())
			model.put("memberTopicList", memberTopicList);
		if (topic.getRelatedid() != null) {
			// 关联的帖子
			relate = relateService.getRelatedObject(topic.getTag(), topic.getRelatedid());
			if (relate != null) {
				model.put("relate", relate);
				List<Diary> relateTopicList = diaryService.getDiaryList(Diary.class, citycode, DiaryConstant.DIARY_TYPE_ALL, topic.getTag(), topic.getRelatedid(), 0, 10);
				if (!relateTopicList.isEmpty())
					model.put("relateTopicList", relateTopicList);
			}
		}
		if (topic.getCategoryid() != null) {
			Object relate2 = relateService.getRelatedObject(topic.getCategory(), topic.getCategoryid());
			if (relate2 != null) {
				model.put("category", topic.getCategory());
				if (StringUtils.equals(topic.getCategory(), TagConstant.TAG_MOVIE)) {
					Movie movie = (Movie) relate2;
					model.put("relate2", movie);
					markCountMap.put(movie.getId(), markService.getMarkCountByTagRelatedid(TagConstant.TAG_MOVIE, movie.getId()));
					model.put("curMarkCountMap", markCountMap);
					model.put("markData", markService.getMarkdata(TagConstant.TAG_MOVIE));
				} else
					model.put("relate2", relate2);
				List<Diary> categoryTopicList = diaryService.getDiaryList(Diary.class, citycode, DiaryConstant.DIARY_TYPE_ALL, topic.getCategory(), topic.getCategoryid(), 0, 10);
				if (!categoryTopicList.isEmpty())
					model.put("categoryTopicList", categoryTopicList);
			}
		}
		// 关联圈子
		if (topic.getCommunityid() != null) {
			Commu commu = daoService.getObject(Commu.class, topic.getCommunityid());
			if (commu != null)
				model.put("commu", commu);
		}
		if (DiaryConstant.DIARY_TYPE_TOPIC_VOTE_MULTI.equals(topic.getType()) || DiaryConstant.DIARY_TYPE_TOPIC_VOTE_RADIO.equals(topic.getType())) {
			model.putAll(this.voteDetail(topic.getId()));
		}
		// 一周活跃用户
		List<Map> memberList = commentService.getActiveMemberList(9);
		model.put("memberList", memberList);
		String tag = topic.getTag();
		Long relatedid = topic.getRelatedid();
		// 板块设置
		Integer permission = blogService.getMaxRights(tag, relatedid, null, member);
		model.put("permission", permission);
		// 版主
		List<Bkmember> banzhuList = blogService.getBanzhuList(tag, relatedid);
		model.put("banzhuList", banzhuList);
		Map diaryPoint = JsonUtils.readJsonToMap(topic.getOtherinfo());
		model.put("diaryPoint", diaryPoint);
		if (member != null) {
			List<Long> memberidList = ServiceHelper.getMemberIdListFromBeanList(commentList);
			isTreasuredInfo(member, model, Arrays.asList(topic.getMemberid()), isTreasureMap);// 发帖人跟当前用户是否以关注
			isTreasuredInfo(member, model, memberidList, isTreasureMap);
		}
		model.put("diaryBody", blogService.getDiaryBody(did));
		return "blog/topicDetail.vm";
	}

	// 编辑帖子
	@RequestMapping("/blog/modifyTopic.xhtml")
	public String modifyTopic(@CookieValue(value = LOGIN_COOKIE_NAME, required = false)
	String sessid, HttpServletRequest request, String tag, String type, Long relatedid, Long did, Long commutopicid, Long cid, ModelMap model, HttpServletResponse response) {
		Member member = loginService.getLogonMemberBySessid(WebUtils.getRemoteIp(request), sessid);
		if (member == null) return gotoLogin("/blog/modifyTopic.xhtml", request, model);
		DiaryBase topic = null;
		if (did != null) {
			topic = daoService.getObject(Diary.class, did);
			if (topic == null) {
				topic = daoService.getObject(DiaryHist.class, did);
			}
			if (blogService.getMaxRights(tag, relatedid, topic.getMemberid(), member) < Bkmember.ROLE_BANZHU) {
				return showError(model, "您没有权限编辑此帖子！");
			}
			tag = topic.getTag();
			relatedid = topic.getRelatedid();
			if (DiaryConstant.DIARY_TYPE_COMMENT.equals(topic.getType()))
				model.put("type", "1");
		} else {
			topic = new Diary("");
			String opkey = OperationService.TAG_ADDCONTENT + member.getId();
			if (!operationService.isAllowOperation(opkey, OperationService.HALF_HOUR, 30)) {
				return showJsonError(model, "发帖频率不能太快！");
			}
			if (ServiceHelper.isTag(tag)) {
				topic.setTag(tag);
				topic.setRelatedid(relatedid);
			} else if (ServiceHelper.isCategory(tag)) {
				topic.setTag(ServiceHelper.getTag(tag));
				topic.setCategory(tag);
				topic.setCategoryid(relatedid);
			}
			model.put("type", type);
		}
		if (topic.getRelatedid() != null) {
			Object relate = relateService.getRelatedObject(topic.getTag(), topic.getRelatedid());
			if (relate != null) {
				String countycode = (String) BeanUtil.get(relate, "countycode");
				if (relate instanceof BaseInfo) {
					if (StringUtils.isNotBlank(countycode)) {
						model.put("indexareaList", placeService.getIndexareaByCountyCode(countycode));
						model.put("countycode", countycode);
						String indexareacode = (String) BeanUtil.get(relate, "indexareacode");
						model.put("indexareacode", indexareacode);
						List placeList = placeService.getPlaceListByTag(tag, countycode, indexareacode);
						model.put("placeList", placeList);
					}
					topic.setCitycode(((BaseInfo) (relate)).getCitycode());
				}
			model.put("relate", relate);
			} else {
				String citycode = WebUtils.getAndSetDefault(request, response);
				topic.setCitycode(citycode);
			}
			if (StringUtils.isNotBlank(topic.getCountycode())) {
				County county = daoService.getObject(County.class, topic.getCountycode());
				model.put("county", county);
			}
		}
		
		if (topic.getCategoryid()!= null) {
			Object relate2 = relateService.getRelatedObject(topic.getCategory(), topic.getCategoryid());
			model.put("relate2", relate2);
		}
		
		model.put("topic", topic);
		if (topic.getId() != null)
			model.put("diaryBody", blogService.getDiaryBody(topic.getId()));
		// 版主
		List<Bkmember> banzhuList = blogService.getBanzhuList(tag, relatedid);
		model.put("banzhuList", banzhuList);
		// 关联活动
		List<RemoteActivity> activityList = new ArrayList<RemoteActivity>();
		ErrorCode<Integer> code = synchActivityService.getMemberActivityCount(member.getId(), null, RemoteActivity.TIME_ALL, null, null);
		if(code.isSuccess()){
			Integer activityCount = code.getRetval();
			ErrorCode<List<RemoteActivity>> code2 =  synchActivityService.getMemberActivityListByMemberid(member.getId(), null, RemoteActivity.TIME_ALL, null, null, 0, activityCount);
			if(code2.isSuccess()) activityList = code2.getRetval();
		}
		model.put("activityList", activityList);
		if (cid != null) {
			getCommuRightData2(model, cid, member);
			Map<String, String> layoutMap = jsonDataService.getJsonData(JsonDataKey.KEY_COMMULAYOUT + cid);
			if (!"2".equals(layoutMap.get("diarytemplate"))) {
				List<CommuTopic> commuTopicList = commonService.getCommuTopicList(cid, 0, 100);
				model.put("commuTopicList", commuTopicList);
			}
			model.put("logonMember", member);
			model.put("commutopicid", commutopicid);
			getCommuRightData2(model, cid, member);
			return "home/community/modifyTopic.vm";
		} else {
			return "blog/modifyTopic.vm";
		}
	}

	@RequestMapping("/blog/modifyVote.xhtml")
	public String modifyVote(@CookieValue(value = LOGIN_COOKIE_NAME, required = false)
	String sessid, HttpServletRequest request, String tag, Long relatedid, ModelMap model, Long cid) {
		Member member = loginService.getLogonMemberBySessid(WebUtils.getRemoteIp(request), sessid);
		if (member == null){
			return gotoLogin("/blog/modifyVote.xhtml", request, model);
		}
		String opkey = OperationService.TAG_ADDCONTENT + member.getId();
		if (!operationService.isAllowOperation(opkey, 40))
			return showError(model, "发帖频率不能太快！");
		Diary topic = new Diary("");
		model.put("topic", topic);
		if (ServiceHelper.isTag(tag)) {
			topic.setTag(tag);
			if (relatedid != null) {
				topic.setRelatedid(relatedid);
				Object relate = relateService.getRelatedObject(tag, relatedid);
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
			}
		} else if (ServiceHelper.isCategory(tag)) {
			topic.setTag(ServiceHelper.getTag(tag));
			topic.setCategory(tag);
			topic.setCategoryid(relatedid);
		}
		if (topic.getCategoryid()!= null) {
			Object relate2 = relateService.getRelatedObject(topic.getCategory(), topic.getCategoryid());
			model.put("relate2", relate2);
		}
		// 版主
		List<Bkmember> banzhuList = blogService.getBanzhuList(tag, relatedid);
		model.put("banzhuList", banzhuList);
		if (cid != null) {
			getCommuRightData2(model, cid, member);
			return "home/community/modifyCommuVote.vm";
		} else {
			return "blog/modifyVote.vm";
		}
	}

	// 投票细节
	private Map voteDetail(Long vid) {
		Map model = new HashMap();
		List<VoteOption> voList = diaryService.getVoteOptionByVoteid(vid);
		Integer votecount = diaryService.getVotecount(vid);
		Map<Long, Long> perMap = new HashMap();
		if (votecount == 0) {
			for (VoteOption vo : voList) {
				perMap.put(vo.getId(), 0L);
			}
		} else {
			for (VoteOption vo : voList) {
				Double per = (vo.getSelectednum() / new Double(votecount)) * 100;
				perMap.put(vo.getId(), Math.round(per));
			}
		}
		model.put("voList", voList);
		model.put("votecount", votecount);
		model.put("perMap", perMap);
		return model;
	}
}