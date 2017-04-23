package com.gewara.web.action.common;

import java.sql.Timestamp;
import java.util.ArrayList;
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

import com.gewara.constant.DiaryConstant;
import com.gewara.constant.content.SignName;
import com.gewara.constant.sys.MongoData;
import com.gewara.json.PageView;
import com.gewara.model.bbs.Diary;
import com.gewara.model.bbs.DiaryBase;
import com.gewara.model.bbs.Moderator;
import com.gewara.model.bbs.VoteOption;
import com.gewara.model.common.City;
import com.gewara.model.common.County;
import com.gewara.model.content.GewaCommend;
import com.gewara.model.drama.Theatre;
import com.gewara.model.draw.DrawActivity;
import com.gewara.model.movie.SpecialActivity;
import com.gewara.model.user.Member;
import com.gewara.model.user.MemberInfo;
import com.gewara.mongo.MongoService;
import com.gewara.service.bbs.BlogService;
import com.gewara.service.bbs.DiaryService;
import com.gewara.service.bbs.ModeratorService;
import com.gewara.service.content.RecommendService;
import com.gewara.service.drama.DramaPlayItemService;
import com.gewara.service.ticket.OpenPlayService;
import com.gewara.support.ServiceHelper;
import com.gewara.untrans.CommentService;
import com.gewara.untrans.CommonService;
import com.gewara.untrans.PageCacheService;
import com.gewara.untrans.PageParams;
import com.gewara.util.DateUtil;
import com.gewara.util.JsonUtils;
import com.gewara.util.RelatedHelper;
import com.gewara.util.StringUtil;
import com.gewara.util.VmUtils;
import com.gewara.util.WebUtils;
import com.gewara.web.action.AnnotationController;
import com.gewara.xmlbind.bbs.Comment;

/**
 * 专题模板
 * @author hubo
 *
 */
@Controller
public class SubjectTemplateController extends AnnotationController {
	@Autowired@Qualifier("recommendService")
	private RecommendService recommendService;
	@Autowired@Qualifier("commonService")
	private CommonService commonService;
	public void setCommonService(CommonService commonService) {
		this.commonService = commonService;
	}
	@Autowired@Qualifier("moderatorService")
	private ModeratorService moderatorService;
	public void setModeratorService(ModeratorService moderatorService) {
		this.moderatorService = moderatorService;
	}
	@Autowired@Qualifier("diaryService")
	private DiaryService diaryService;
	public void setDiaryService(DiaryService diaryService) {
		this.diaryService = diaryService;
	}
	@Autowired@Qualifier("pageCacheService")
	private PageCacheService pageCacheService;
	public void setPageCacheService(PageCacheService pageCacheService) {
		this.pageCacheService = pageCacheService;
	}
	@Autowired@Qualifier("commentService")
	private CommentService commentService;
	public void setCommentService(CommentService commentService) {
		this.commentService = commentService;
	}
	@Autowired@Qualifier("mongoService")
	private MongoService mongoService;
	public void setMongoService(MongoService mongoService) {
		this.mongoService = mongoService;
	}
	@Autowired@Qualifier("blogService")
	private BlogService blogService;
	public void setBlogService(BlogService blogService) {
		this.blogService = blogService;
	}
	@Autowired@Qualifier("openPlayService")
	private OpenPlayService openPlayService;
	public void setOpenPlayService(OpenPlayService openPlayService) {
		this.openPlayService = openPlayService;
	}
	@Autowired@Qualifier("dramaPlayItemService")
	private DramaPlayItemService dramaPlayItemService;
	public void setDramaPlayItemService(DramaPlayItemService dramaPlayItemService) {
		this.dramaPlayItemService = dramaPlayItemService;
	}
	/***
	 * 前台模板首页的加载
	 * @param tid
	 * @return
	 */
	@RequestMapping("/common/show.xhtml")
	public String templatesShow(Long tid, ModelMap model, 
			HttpServletRequest request, HttpServletResponse response){
		if(tid == null)return showError(model, "数据错误");
		SpecialActivity specialActivity = daoService.getObject(SpecialActivity.class, tid);
		if(specialActivity == null)return showError(model, "数据错误");
		String citycode = WebUtils.getAndSetDefault(request, response);
		if(pageCacheService.isUseCache(request)){
			PageParams pageParams = new PageParams();
			pageParams.addLong("tid", tid);
			PageView pageView = pageCacheService.getPageView(request, "common/show.xhtml", pageParams, citycode);
			if(pageView!=null){
				model.put("pageView", pageView);
				return "pageView.vm";
			}
		}
		model.put("specialActivity", specialActivity);
		// 头图与论坛图片
		Long headid = specialActivity.getHeadpic();
		if(headid != null){
			GewaCommend gewaCommend_head = daoService.getObject(GewaCommend.class, headid);
			model.put("gewaCommend_head", gewaCommend_head);
		}
		Long blogid = specialActivity.getBlogpic();
		if(blogid != null){
			GewaCommend gewaCommend_blog = daoService.getObject(GewaCommend.class, blogid);
			model.put("gewaCommend_blog", gewaCommend_blog);
		}
		// 组图
		List<GewaCommend> teampicList = new ArrayList<GewaCommend>();
		teampicList = commonService.getGewaCommendList(citycode, SignName.TPL_TEAMPIC, specialActivity.getId(),null, false,0, 200);
		model.put("teampicList", teampicList);
		RelatedHelper rh = new RelatedHelper();
		model.put("relatedHelper", rh);
		// 查找关联新闻(有图)
		List<GewaCommend> picnewsList = commonService.getGewaCommendList(citycode, "sp" + specialActivity.getId(), null, "news", true, 0, 5);
		commonService.initGewaCommendList("picnewsList", rh, picnewsList);
		model.put("picnewsList", picnewsList);
		// 查找关联新闻(无图)
		List<GewaCommend> newsList = commonService.getGewaCommendList(citycode, "sp" + specialActivity.getId(), null, "news", true, 5, 15);
		commonService.initGewaCommendList("newsList", rh, newsList);
		model.put("newsList", newsList);
		// 关联哇啦
		List<GewaCommend> gcwaraList = commonService.getGewaCommendList(citycode, "sp" + specialActivity.getId(), null, "moderator", true, 0, 1);
		if(gcwaraList != null && gcwaraList.size() > 0){
			Moderator moderator = daoService.getObject(Moderator.class, gcwaraList.get(0).getRelatedid());
			if(moderator != null){
				model.put("gcwara", moderator);
				List<Comment> commentList = commentService.getModeratorDetailList(moderator.getTitle(), false, 0, 12);
				model.put("gcwaracomment", commentList);
				addCacheMember(model, ServiceHelper.getMemberIdListFromBeanList(commentList));
			}
		}
		
		// 关联调查
		List<GewaCommend> voteList = commonService.getGewaCommendList(citycode, "sp" + specialActivity.getId(), null, "diary_vote", true, 0, 1);
		if(voteList != null && voteList.size() > 0){
			DiaryBase vote = diaryService.getDiaryBase(voteList.get(0).getRelatedid());
			if(vote != null){
				model.put("gawavote", vote);
				Integer votecount = diaryService.getVotecount(vote.getId());
				model.put("votecount", votecount);
				List<VoteOption> voList = diaryService.getVoteOptionByVoteid(vote.getId());
				model.put("voList", voList);
			}
		}
		
		// 关联调查(临时添加)
		List<GewaCommend> voteTmpList = commonService.getGewaCommendList(citycode, "sp" + specialActivity.getId(), null, "diary_vote_tmp", true, 0, 1);
		if(voteTmpList != null && voteTmpList.size() > 0){
			DiaryBase vote = diaryService.getDiaryBase(voteTmpList.get(0).getRelatedid());
			if(vote != null){
				model.put("gawavoteTmp", vote);
				Integer votecount = diaryService.getVotecount(vote.getId());
				model.put("votecountTmp", votecount);
				List<VoteOption> voList = diaryService.getVoteOptionByVoteid(vote.getId());
				model.put("voListTmp", voList);
			}
		}
		
		// 关联活动
		List<GewaCommend> gcactivityList = commonService.getGewaCommendList(citycode, "sp" + specialActivity.getId(), null, "activity", true, 0, 1);
		if(gcactivityList != null && gcactivityList.size() > 0){
			commonService.initGewaCommendList("gcactivity", rh, gcactivityList);
			model.put("gcactivity", gcactivityList);
		}
		
		// 关联帖子
		List<GewaCommend> gcdiaryList = commonService.getGewaCommendList(citycode, "sp" + specialActivity.getId(), null, "diary", true, 0, 8);
		commonService.initGewaCommendList("gcdiaryList", rh, gcdiaryList);
		model.put("gcdiaryList", gcdiaryList);
		
		// 关联知道
		List<GewaCommend> gcquestionList = commonService.getGewaCommendList(citycode, "sp" + specialActivity.getId(), null,"gewaquestion", true, 0, 8);
		commonService.initGewaCommendList("gcquestionList", rh, gcquestionList);
		model.put("gcquestionList", gcquestionList);
		return "common/modifyNews.vm";
	}

	// 通用简单模板
	@RequestMapping("/subject/simpleSubjectTemplate.xhtml")
	public String simpleSubjectTemplate(@CookieValue(value=LOGIN_COOKIE_NAME, required=false)String sessid,
			HttpServletRequest request, ModelMap model, String tag){
		// 取得大专题
		Map mainsubject = mongoService.findOne(MongoData.NS_MAINSUBJECT, MongoData.DEFAULT_ID_NAME, tag);
		if(mainsubject == null) return show404(model, "");
		model.put("mainsubject", mainsubject);
		String parentid = (String) mainsubject.get(MongoData.DEFAULT_ID_NAME);
		String board = (String) mainsubject.get(MongoData.ACTION_BOARD);
		Long relatedid = (Long) mainsubject.get(MongoData.ACTION_RELATEDID);
		Object object = relateService.getRelatedObject(board, relatedid);
		model.put("object", object);
		model.put("keywords", mainsubject.get(MongoData.ACTION_SEOKEYWORDS));
		model.put("description", mainsubject.get(MongoData.ACTION_SEODESCRIPTION));
		
		// 子版块
		List<Map> daohangList = recommendService.getRecommendMap(null, MongoData.SIGNNAME_DAOHANG, parentid);
		model.put("daohangList", daohangList);
		List<Map> xintuList = recommendService.getRecommendMap(null, MongoData.SIGNNAME_XINTU, parentid);
		model.put("xintuList", xintuList);
		List<Map> xinzhuanList = recommendService.getRecommendMap(null, MongoData.SIGNNAME_XINZHUAN, parentid);
		model.put("xinzhuanList", xinzhuanList);
		List<Map> xinwenList = recommendService.getRecommendMap(null, MongoData.SIGNNAME_XINWEN, parentid);
		model.put("xinwenList", xinwenList);
		List<Map> shipinList = recommendService.getRecommendMap(null, MongoData.SIGNNAME_SHIPIN, parentid);
		model.put("shipinList", shipinList);
		List<Map> tupianList = recommendService.getRecommendMap(null, MongoData.SIGNNAME_TUPIAN, parentid);
		model.put("tupianList", tupianList);
		List<Map> hudongList = recommendService.getRecommendMap(null, MongoData.SIGNNAME_HUDONG, parentid);
		model.put("hudongList", hudongList);
		List<Map> luntanList = recommendService.getRecommendMap(null, MongoData.SIGNNAME_LUNTAN, parentid);
		model.put("luntanList", luntanList);
		List<Map> zhidaoList = recommendService.getRecommendMap(null, MongoData.SIGNNAME_ZHIDAO, parentid);
		model.put("zhidaoList", zhidaoList);
		
		// 哇啦及关注
		List<Moderator> hotModeratorList = moderatorService.getModeratorByType(Moderator.SHOW_TYPE_WEB, Moderator.TYPE_HOT, 0, 4, false);
		model.put("hotModeratorList", hotModeratorList);
		// 人气用户
		Member member = loginService.getLogonMemberBySessid(WebUtils.getRemoteIp(request), sessid);
		hotMicroMemberList(member, model);
		
		return "subject/simpleSubject/simpleSubjectIndex.vm";
	}
	/**
	 * 人气用户
	 */
	private void hotMicroMemberList(Member member, ModelMap model){
		List<Map> hotMemberList = commentService.getHotMicroMemberList(null, null, 12);
		Map<Long,Boolean> isTreasureHotMember = new HashMap<Long, Boolean>();
		Map<Long,String> hotMicroMemberAreaMap = new HashMap<Long, String>();//市，区
		for (Map minfo: hotMemberList) {
			Long mid = Long.parseLong(""+minfo.get("id"));
			MemberInfo memberinfo = daoService.getObject(MemberInfo.class, mid);
			if(member!=null) isTreasureHotMember.put(mid, blogService.isTreasureMember(member.getId(), mid));
			City city = daoService.getObject(City.class, memberinfo.getLivecity());
			County county = daoService.getObject(County.class, memberinfo.getLivecounty());
			hotMicroMemberAreaMap.put(memberinfo.getId(), (city !=null?city.getCityname():"")+(county != null?"，"+county.getCountyname():""));
		}
		model.put("hotMicroMemberAreaMap", hotMicroMemberAreaMap);
		model.put("isTreasureHotMember", isTreasureHotMember);
		model.put("hotMemberList", hotMemberList);
	}
	
	@RequestMapping("/subject/unionTemplate.xhtml")
	public String unionTemplateIndex(String tag, ModelMap model, HttpServletRequest request, HttpServletResponse response){
		String citycode = WebUtils.getAndSetDefault(request, response);
		String parentid = tag;
		// 取得大专题
		Map mainsubject = mongoService.findOne(MongoData.NS_MAINSUBJECT, MongoData.DEFAULT_ID_NAME, parentid);
		if(mainsubject == null) return show404(model, null);
		model.put("mainsubject", mainsubject);
		model.put("keywords", mainsubject.get(MongoData.ACTION_SEOKEYWORDS));
		model.put("description", mainsubject.get(MongoData.ACTION_SEODESCRIPTION));
		// 头图背景等
		String board = ""+mainsubject.get(MongoData.ACTION_BOARD);
		Long relatedid = (Long) mainsubject.get(MongoData.ACTION_RELATEDID);
		Object object = relateService.getRelatedObject(board, relatedid);
		if(object != null) model.put("headInfo", object);

		// 左版块
		List<Map> leftlist = getSubboard("L", parentid);
		for(Map data : leftlist){
			String nid = ""+data.get(MongoData.DEFAULT_ID_NAME);
			if(StringUtils.equals(""+data.get(MongoData.ACTION_SUBJECTTYPE), MongoData.L_UNIONSUB_XINWEN1)){
				List<Map> xinwen01List = recommendService.getRecommendMap(null, MongoData.L_UNIONSUB_XINWEN1, nid);
				model.put("Lxinwen01List", xinwen01List);
			}else if(StringUtils.equals(""+data.get(MongoData.ACTION_SUBJECTTYPE), MongoData.L_UNIONSUB_XINWEN2)){
				List<Map> xinwen02List = recommendService.getRecommendMap(null, MongoData.L_UNIONSUB_XINWEN2, nid);
				model.put("Lxinwen02List", xinwen02List);
			}else if(StringUtils.equals(""+data.get(MongoData.ACTION_SUBJECTTYPE), MongoData.L_UNIONSUB_XINWEN3)){
				List<Map> xinwen03List = recommendService.getRecommendMap(null, MongoData.L_UNIONSUB_XINWEN3, nid);
				model.put("Lxinwen03List", xinwen03List);
			}else if(StringUtils.equals(""+data.get(MongoData.ACTION_SUBJECTTYPE), MongoData.L_UNIONSUB_SHIPIN)){
				List<Map> shipinList = recommendService.getRecommendMap(null, MongoData.L_UNIONSUB_SHIPIN, nid);
				model.put("LshipinList", shipinList);
			}else if(StringUtils.equals(""+data.get(MongoData.ACTION_SUBJECTTYPE), MongoData.L_UNIONSUB_SHIPIN2)){
				List<Map> shipin02List = recommendService.getRecommendMap(null, MongoData.L_UNIONSUB_SHIPIN2, nid);
				model.put("Lshipin02List", shipin02List);
			}else if(StringUtils.equals(""+data.get(MongoData.ACTION_SUBJECTTYPE), MongoData.L_UNIONSUB_JUZHAO)){
				List<Map> juzhaoList = recommendService.getRecommendMap(null, MongoData.L_UNIONSUB_JUZHAO, nid);
				model.put("LjuzhaoList", juzhaoList);
			}else if(StringUtils.equals(""+data.get(MongoData.ACTION_SUBJECTTYPE), MongoData.L_UNIONSUB_JUZHAO2)){
				List<Map> juzhao02List = recommendService.getRecommendMap(null, MongoData.L_UNIONSUB_JUZHAO2, nid);
				for(Map map : juzhao02List){
					String newssubject = (String) map.get(MongoData.ACTION_NEWSSUBJECT);
					map.put(MongoData.ACTION_NEWSSUBJECT, StringUtil.parse2HTML(newssubject));
				}
				model.put("Ljuzhao02List", juzhao02List);
			}else if(StringUtils.equals(""+data.get(MongoData.ACTION_SUBJECTTYPE), MongoData.L_UNIONSUB_HUODONG)){
				List<Map> huodongList = recommendService.getRecommendMap(null, MongoData.L_UNIONSUB_HUODONG, nid);
				for(Map map : huodongList){
					String newsboard = (String) map.get(MongoData.ACTION_NEWSBOARD);
					Long newsboardrelatedid = new Long(""+map.get(MongoData.ACTION_BOARDRELATEDID));
					Object newobject = relateService.getRelatedObject(newsboard, newsboardrelatedid);
					map.put(""+newsboardrelatedid, newobject);
				}
				model.put("LhuodongList", huodongList);
			}else if(StringUtils.equals(""+data.get(MongoData.ACTION_SUBJECTTYPE), MongoData.L_UNIONSUB_BIANJI)){
				List<Map> bianjiList = recommendService.getRecommendMap(null, MongoData.L_UNIONSUB_BIANJI, nid);
				model.put("LbianjiList", bianjiList);
			}else if(StringUtils.equals(""+data.get(MongoData.ACTION_SUBJECTTYPE), MongoData.L_UNIONSUB_DAOHANG)){
				List<Map> daohangList = recommendService.getRecommendMap(null, MongoData.L_UNIONSUB_DAOHANG, nid);
				model.put("LdaohangList", daohangList);
			}else if(StringUtils.equals(""+data.get(MongoData.ACTION_SUBJECTTYPE), MongoData.L_UNIONSUB_CHOUJIANG)){
				List<Map> choujiangList = recommendService.getRecommendMap(null, MongoData.L_UNIONSUB_CHOUJIANG, nid);
				DrawActivity da = null;
				if(!VmUtils.isEmptyList(choujiangList)){
					String title = "";
					for(Map choujiang : choujiangList){
						if(!StringUtils.equals(choujiang.get("ordernum").toString(), "0")){
							title = choujiang.get("newstitle").toString();
							da = daoService.getObjectByUkey(DrawActivity.class, "tag", title, false);
							break;
						}
					}
				}
				if(da != null){
					Map<String, String> daMap = JsonUtils.readJsonToMap(da.getOtherinfo());
					model.put("flashTag", daMap.get("flashTag"));
					Timestamp timestamp = DateUtil.getCurFullTimestamp();
					if(timestamp.after(da.getEndtime()))
					model.put("picurl", choujiangList.get(0).get("newslogo").toString());
				}
			}else if(StringUtils.equals(""+data.get(MongoData.ACTION_SUBJECTTYPE), MongoData.L_UNIONSUB_MINGXINPIAN)){
				List<Map> dianyingList = recommendService.getRecommendMap(null, MongoData.L_UNIONSUB_MINGXINPIAN, nid);
				model.put("dianyingList", dianyingList);
				model.put("postcardCount", mongoService.getCount(MongoData.NS_POSTCARD_INFO));
			}else if(StringUtils.equals(""+data.get(MongoData.ACTION_SUBJECTTYPE), MongoData.L_UNIONSUB_TOUPIAO)){
				List<Map> toupiaoList = recommendService.getRecommendMap(null, MongoData.L_UNIONSUB_TOUPIAO, nid);
				model.put("toupiaoList", toupiaoList);
				for(Map map : toupiaoList){
					String newsboard = (String) map.get(MongoData.ACTION_NEWSBOARD);
					Long newsboardrelatedid = new Long(""+map.get(MongoData.ACTION_BOARDRELATEDID));
					Object newobject = relateService.getRelatedObject(newsboard, newsboardrelatedid);
					if(newobject instanceof Diary){
						Diary topic = (Diary) newobject;
						if (DiaryConstant.DIARY_TYPE_TOPIC_VOTE_MULTI.equals(topic.getType()) || DiaryConstant.DIARY_TYPE_TOPIC_VOTE_RADIO.equals(topic.getType())) {
							model.putAll(this.voteDetail(topic.getId()));
						}
					}
					map.put(""+newsboardrelatedid, newobject);
				}
			}
		}
		model.put("leftSublist", leftlist);
		
		// 右版块
		List<Map> rightlist = getSubboard("R", parentid);
		for(Map data : rightlist){
			String nid = ""+data.get(MongoData.DEFAULT_ID_NAME);
			if(StringUtils.equals(""+data.get(MongoData.ACTION_SUBJECTTYPE), MongoData.R_UNIONSUB_XINWEN4)){
				List<Map> xinwen04List = recommendService.getRecommendMap(null, MongoData.R_UNIONSUB_XINWEN4, nid);
				model.put("Rxinwen04List", xinwen04List);
			}else if(StringUtils.equals(""+data.get(MongoData.ACTION_SUBJECTTYPE), MongoData.R_UNIONSUB_YINGPIAN)){
				List<Map> yingpianList = recommendService.getRecommendMap(null, MongoData.R_UNIONSUB_YINGPIAN, nid);
				Map<Long, Boolean> movieBookingMap = new HashMap<Long, Boolean>();
				for(Map map : yingpianList){
					String newsboard = (String) map.get(MongoData.ACTION_NEWSBOARD);
					Long newsboardrelatedid = new Long(""+map.get(MongoData.ACTION_BOARDRELATEDID));
					Object newobject = relateService.getRelatedObject(newsboard, newsboardrelatedid);
					List<java.util.Date> dateList = openPlayService.getMovieOpenDateList(citycode, newsboardrelatedid);
					if(dateList.isEmpty()) movieBookingMap.put(newsboardrelatedid, false);
					else movieBookingMap.put(newsboardrelatedid, true);
					map.put(""+newsboardrelatedid, newobject);
				}
				model.put("RyingpianList", yingpianList);
				model.put("movieBookingMap", movieBookingMap);
			}else if(StringUtils.equals(""+data.get(MongoData.ACTION_SUBJECTTYPE), MongoData.R_UNIONSUB_HUAJU)){
				List<Map> huajuList = recommendService.getRecommendMap(null, MongoData.R_UNIONSUB_HUAJU, nid);
				Map<Long, Boolean> dramaBookingMap = new HashMap<Long, Boolean>();
				Map<Long, List<Integer>> dramaPriceMap = new HashMap<Long, List<Integer>>();
				Map<Long, List<Theatre>> theatreMap = new HashMap<Long, List<Theatre>>();
				for(Map map : huajuList){
					String newsboard = (String) map.get(MongoData.ACTION_NEWSBOARD);
					Long newsboardrelatedid = new Long(""+map.get(MongoData.ACTION_BOARDRELATEDID));
					Object newobject = relateService.getRelatedObject(newsboard, newsboardrelatedid);
					boolean isBooking = dramaPlayItemService.isBookingByDramaId(newsboardrelatedid);
					dramaBookingMap.put(newsboardrelatedid, isBooking);
					theatreMap.put(newsboardrelatedid, dramaPlayItemService.getTheatreList(citycode, newsboardrelatedid, isBooking, 2));
					dramaPriceMap.put(newsboardrelatedid, dramaPlayItemService.getPriceList(null, newsboardrelatedid, null, null, false));
					map.put(""+newsboardrelatedid, newobject);
				}
				model.put("RhuajuList", huajuList);
				model.put("dramaBookingMap", dramaBookingMap);
				model.put("dramaPriceMap", dramaPriceMap);
				model.put("theatreMap", theatreMap);
			}else if(StringUtils.equals(""+data.get(MongoData.ACTION_SUBJECTTYPE), MongoData.R_UNIONSUB_SHIPIN)){
				List<Map> shipinList = recommendService.getRecommendMap(null, MongoData.R_UNIONSUB_SHIPIN, nid);
				model.put("RshipinList", shipinList);
			}else if(StringUtils.equals(""+data.get(MongoData.ACTION_SUBJECTTYPE), MongoData.R_UNIONSUB_JUZHAO2)){
				List<Map> juzhao02List = recommendService.getRecommendMap(null, MongoData.R_UNIONSUB_JUZHAO2, nid);
				for(Map map : juzhao02List){
					String newssubject = (String) map.get(MongoData.ACTION_NEWSSUBJECT);
					map.put(MongoData.ACTION_NEWSSUBJECT, StringUtil.parse2HTML(newssubject));
				}
				model.put("Rjuzhao02List", juzhao02List);
			}else if(StringUtils.equals(""+data.get(MongoData.ACTION_SUBJECTTYPE), MongoData.R_UNIONSUB_HUODONG)){
				List<Map> huodongList = recommendService.getRecommendMap(null, MongoData.R_UNIONSUB_HUODONG, nid);
				for(Map map : huodongList){
					String newsboard = (String) map.get(MongoData.ACTION_NEWSBOARD);
					Long newsboardrelatedid = new Long(""+map.get(MongoData.ACTION_BOARDRELATEDID));
					Object newobject = relateService.getRelatedObject(newsboard, newsboardrelatedid);
					map.put(""+newsboardrelatedid, newobject);
				}
				model.put("RhuodongList", huodongList);
			}else if(StringUtils.equals(""+data.get(MongoData.ACTION_SUBJECTTYPE), MongoData.R_UNIONSUB_ZHIDAO)){
				List<Map> zhidaoList = recommendService.getRecommendMap(null, MongoData.R_UNIONSUB_ZHIDAO, nid);
				model.put("RzhidaoList", zhidaoList);
			}else if(StringUtils.equals(""+data.get(MongoData.ACTION_SUBJECTTYPE), MongoData.R_UNIONSUB_LUNTAN)){
				List<Map> luntanList = recommendService.getRecommendMap(null, MongoData.R_UNIONSUB_LUNTAN, nid);
				model.put("RluntanList", luntanList);
			}else if(StringUtils.equals(""+data.get(MongoData.ACTION_SUBJECTTYPE), MongoData.R_UNIONSUB_BIANJI)){
				List<Map> bianjiList = recommendService.getRecommendMap(null, MongoData.R_UNIONSUB_BIANJI, nid);
				model.put("RbianjiList", bianjiList);
			}
		}
		model.put("rightSublist", rightlist);
		
		// 上通栏版块
		List<Map> toplist = getSubboard("T", parentid);
		for(Map data : toplist){
			String nid = ""+data.get(MongoData.DEFAULT_ID_NAME);
			if(StringUtils.equals(""+data.get(MongoData.ACTION_SUBJECTTYPE), MongoData.T_UNIONSUB_JUZHAO)){
				List<Map> juzhaoList = recommendService.getRecommendMap(null, MongoData.T_UNIONSUB_JUZHAO, nid);
				model.put("TjuzhaoList", juzhaoList);
			}else if(StringUtils.equals(""+data.get(MongoData.ACTION_SUBJECTTYPE), MongoData.T_UNIONSUB_JUZHAO2)){
				List<Map> juzhaoList2 = recommendService.getRecommendMap(null, MongoData.T_UNIONSUB_JUZHAO2, nid);
				model.put("Tjuzhao2List", juzhaoList2);
			}else if(StringUtils.equals(""+data.get(MongoData.ACTION_SUBJECTTYPE), MongoData.T_UNIONSUB_BIANJI)){
				List<Map> bianjiList = recommendService.getRecommendMap(null, MongoData.T_UNIONSUB_BIANJI, nid);
				model.put("TbianjiList", bianjiList);
			}
		}
		model.put("topSublist", toplist);
		
		// 下通栏版块
		List<Map> bottomlist = getSubboard("B", parentid);
		for(Map data : bottomlist){
			String nid = ""+data.get(MongoData.DEFAULT_ID_NAME);
			if(StringUtils.equals(""+data.get(MongoData.ACTION_SUBJECTTYPE), MongoData.B_UNIONSUB_JUZHAO)){
				List<Map> juzhaoList = recommendService.getRecommendMap(null, MongoData.B_UNIONSUB_JUZHAO, nid);
				model.put("BjuzhaoList", juzhaoList);
			}else if(StringUtils.equals(""+data.get(MongoData.ACTION_SUBJECTTYPE), MongoData.B_UNIONSUB_JUZHAO2)){
				List<Map> juzhaoList2 = recommendService.getRecommendMap(null, MongoData.B_UNIONSUB_JUZHAO2, nid);
				model.put("Bjuzhao2List", juzhaoList2);
			}else if(StringUtils.equals(""+data.get(MongoData.ACTION_SUBJECTTYPE), MongoData.B_UNIONSUB_BIANJI)){
				List<Map> bianjiList = recommendService.getRecommendMap(null, MongoData.B_UNIONSUB_BIANJI, nid);
				model.put("BbianjiList", bianjiList);
			}
		}
		model.put("bottomSublist", bottomlist);
		
		return "common/newsTemplate/index.vm";
	}
	private List<Map> getSubboard(String board, String parentid){
		Map params = new HashMap();
		params.put(MongoData.ACTION_BOARD, board);
		params.put(MongoData.ACTION_PARENTID, parentid);
		params.put(MongoData.ACTION_TYPE, MongoData.ACTION_TYPE_SUBUNIONTEMPLATE);
		return mongoService.find(MongoData.NS_MAINSUBJECT, params, MongoData.ACTION_ORDERNUM, true);
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
