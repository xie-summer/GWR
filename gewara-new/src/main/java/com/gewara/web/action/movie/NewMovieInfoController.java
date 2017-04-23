package com.gewara.web.action.movie;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.criterion.Subqueries;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.gewara.constant.DiaryConstant;
import com.gewara.constant.PayConstant;
import com.gewara.constant.Status;
import com.gewara.constant.TagConstant;
import com.gewara.constant.order.AddressConstant;
import com.gewara.constant.sys.MongoData;
import com.gewara.json.PageView;
import com.gewara.json.PlayItemMessage;
import com.gewara.json.bbs.MarkCountData;
import com.gewara.model.bbs.Diary;
import com.gewara.model.bbs.qa.GewaQuestion;
import com.gewara.model.common.County;
import com.gewara.model.content.News;
import com.gewara.model.content.Picture;
import com.gewara.model.content.Video;
import com.gewara.model.movie.Cinema;
import com.gewara.model.movie.Movie;
import com.gewara.model.pay.SpecialDiscount;
import com.gewara.model.ticket.OpenPlayItem;
import com.gewara.model.user.Member;
import com.gewara.model.user.MemberPicture;
import com.gewara.mongo.MongoService;
import com.gewara.service.OperationService;
import com.gewara.service.bbs.DiaryService;
import com.gewara.service.bbs.MarkService;
import com.gewara.service.bbs.QaService;
import com.gewara.service.content.NewsService;
import com.gewara.service.content.PictureService;
import com.gewara.service.content.VideoService;
import com.gewara.service.drama.DramaService;
import com.gewara.service.movie.MCPService;
import com.gewara.service.ticket.OpenPlayService;
import com.gewara.support.ErrorCode;
import com.gewara.support.MultiPropertyComparator;
import com.gewara.support.ServiceHelper;
import com.gewara.support.VelocityTemplate;
import com.gewara.untrans.CacheDataService;
import com.gewara.untrans.CommentService;
import com.gewara.untrans.PageCacheService;
import com.gewara.untrans.PageParams;
import com.gewara.untrans.PictureComponent;
import com.gewara.untrans.UntransService;
import com.gewara.untrans.activity.SynchActivityService;
import com.gewara.untrans.impl.ControllerService;
import com.gewara.untrans.order.impl.SpdiscountService;
import com.gewara.util.BeanUtil;
import com.gewara.util.DateUtil;
import com.gewara.util.MarkHelper;
import com.gewara.util.PKCoderUtil;
import com.gewara.util.RelatedHelper;
import com.gewara.util.ValidateUtil;
import com.gewara.util.VmUtils;
import com.gewara.util.WebUtils;
import com.gewara.web.action.AnnotationController;
import com.gewara.web.action.partner.ObjectSpdiscountFilter;
import com.gewara.web.action.partner.OpiSpdiscountFilter;
import com.gewara.web.util.PageUtil;
import com.gewara.xmlbind.activity.RemoteActivity;
import com.gewara.xmlbind.bbs.Comment;

@Controller
public class NewMovieInfoController extends AnnotationController {
	@Autowired@Qualifier("hibernateTemplate")
	private HibernateTemplate hibernateTemplate;
	public void setHibernateTemplate(HibernateTemplate hibernateTemplate) {
		this.hibernateTemplate = hibernateTemplate;
	}
	@Autowired@Qualifier("markService")
	private MarkService markService;
	public void setMarkService(MarkService markService) {
		this.markService = markService;
	}
	@Autowired@Qualifier("diaryService")
	private DiaryService diaryService;
	public void setDiaryService(DiaryService diaryService) {
		this.diaryService = diaryService;
	}
	@Autowired@Qualifier("untransService")
	private UntransService untransService;
	public void setUntransService(UntransService untransService) {
		this.untransService = untransService;
	}
	@Autowired@Qualifier("pictureService")
	private PictureService pictureService;
	public void setPictureService(PictureService pictureService) {
		this.pictureService = pictureService;
	}
	@Autowired@Qualifier("commentService")
	private CommentService commentService;
	public void setCommentService(CommentService commentService) {
		this.commentService = commentService;
	}
	@Autowired@Qualifier("pictureComponent")
	private PictureComponent pictureComponent = null;
	public void setPictureComponent(PictureComponent pictureComponent) {
		this.pictureComponent = pictureComponent;
	}
	@Autowired@Qualifier("synchActivityService")
	private SynchActivityService synchActivityService;
	public void setActivityRemoteService(SynchActivityService synchActivityService) {
		this.synchActivityService = synchActivityService;
	}
	@Autowired@Qualifier("qaService")
	private QaService qaService;
	public void setQaService(QaService qaService) {
		this.qaService = qaService;
	}
	
	@Autowired@Qualifier("pageCacheService")
	private PageCacheService pageCacheService;
	public void setPageCacheService(PageCacheService pageCacheService) {
		this.pageCacheService = pageCacheService;
	}
	@Autowired@Qualifier("mcpService")
	private MCPService mcpService;
	public void setMcpService(MCPService mcpService) {
		this.mcpService = mcpService;
	}
	@Autowired@Qualifier("openPlayService")
	private OpenPlayService openPlayService;
	public void setOpenPlayService(OpenPlayService openPlayService) {
		this.openPlayService = openPlayService;
	}
	@Autowired@Qualifier("mongoService")
	private MongoService mongoService;
	public void setMongoService(MongoService mongoService) {
		this.mongoService = mongoService;
	}
	@Autowired@Qualifier("operationService")
	private OperationService operationService;
	public void setOperationService(OperationService operationService) {
		this.operationService = operationService;
	}
	@Autowired@Qualifier("videoService")
	private VideoService videoService;
	public void setVideoService(VideoService videoService) {
		this.videoService = videoService;
	}
	@Autowired@Qualifier("newsService")
	private NewsService newsService;
	public void setNewsService(NewsService newsService) {
		this.newsService = newsService;
	}
	@Autowired@Qualifier("cacheDataService")
	private CacheDataService cacheDataService;
	@Autowired@Qualifier("dramaService")
	protected DramaService dramaService;
	public void setDramaService(DramaService dramaService) {
		this.dramaService = dramaService;
	}
	@Autowired@Qualifier("spdiscountService")
	protected SpdiscountService spdiscountService;
	public void setSpdiscountService(SpdiscountService spdiscountService) {
		this.spdiscountService = spdiscountService;
	}
	@Autowired@Qualifier("velocityTemplate")
	private VelocityTemplate velocityTemplate;
	public void setVelocityTemplate(VelocityTemplate velocityTemplate) {
		this.velocityTemplate = velocityTemplate;
	}
	@Autowired@Qualifier("controllerService")
	private ControllerService controllerService;
	public void setControllerService(ControllerService controllerService) {
		this.controllerService = controllerService;
	}
	// 电影新页面
	@RequestMapping("/movie/movieDetail.xhtml")
	public String newMovieDetail(@RequestParam("mid")Long mid, ModelMap model, 
			HttpServletRequest request, HttpServletResponse response) {
		Movie movie = daoService.getObject(Movie.class, mid);
		if (movie == null){
			return show404(model, "电影不存在或已经删除！");
		}
		cacheDataService.getAndSetIdsFromCachePool(Movie.class, mid);
		cacheDataService.getAndSetClazzKeyCount(Movie.class, mid);
		String citycode = WebUtils.getAndSetDefault(request, response);
		List<Long> playMovieidList = mcpService.getCurMovieIdList(citycode);
		if (pageCacheService.isUseCache(request) && playMovieidList.contains(movie.getId())) {// 先使用缓存
			PageParams params = new PageParams();
			params.addLong("mid", mid);
			PageView pageView = pageCacheService.getPageView(request, "movie/movieDetail.xhtml", params, citycode);
			if (pageView != null) {
				//Map<String, String> reqMap = WebUtils.getRequestMap(request);
				//model.put("reqMap", reqMap);
				model.put("pageView", pageView);
				return "pageView.vm";
			}
		}
		Long movieid = movie.getId();
		model.putAll(pictureComponent.getHeadData(TagConstant.TAG_MOVIE, mid));
		//排片日期
		List<Date> playdateList = mcpService.getCurMoviePlayDate2(citycode, movieid);
		if (playdateList.size() > 4) playdateList = playdateList.subList(0, 4);
		model.put("playdateList", playdateList);
		// 最近买票用户
		model.putAll(getTicketOrderMemberMapByMovieid(movieid, 10));

		// 同类热映影片
		List<Movie> hotMovies = mcpService.getCurMovieListByMpiCount(citycode, 0, 4);
		hotMovies.remove(movie);
		if (hotMovies.size() > 3) hotMovies = hotMovies.subList(0, 3);
		Map<Long, MarkCountData> markCountMap = new HashMap<Long, MarkCountData>();
		markCountMap.put(movie.getId(), markService.getMarkCountByTagRelatedid(TagConstant.TAG_MOVIE, movie.getId()));
		if (hotMovies.size() > 0) {
			for (Movie cmovie : hotMovies) {
				markCountMap.put(cmovie.getId(), markService.getMarkCountByTagRelatedid(TagConstant.TAG_MOVIE, cmovie.getId()));
			}
		}
		model.put("hotMovieList", hotMovies);
		model.put("movie", movie);
		model.put("markCountMap", markCountMap);
		model.put("curMarkCountMap", markCountMap);
		
		getQaList(mid, model);
		getActivityList(mid, citycode, model);
		getNewsList(mid, citycode, model);
		getVideoList(mid, model);
		getPictureList(mid, model);
		getMovieInfo(mid, citycode, model);
		spdiscountList(model,citycode,movie.getId());
		getDiaryList(mid, citycode, model);
		pictureComponent.pictureDetail(model, TagConstant.TAG_MOVIE, mid, null, "apic");
		if(VmUtils.isEmptyList(playdateList)){
			model.put("videoPlay", isPlayFilm(mid));
		}
		model.put("movieIdList", new ArrayList<Long>());
		return "movie/wide_movieDetail.vm";
	}
	
	private boolean isPlayFilm(long mid){
		List<Video> videos = videoService.getVideoListByTag(Video.VIDEOTYPE_FILM, mid,null,"orderNum",true, 0, 1);
		if(VmUtils.isEmptyList(videos)){
			return false;
		}
		return true;
	}

	@RequestMapping("/movie/moviePlay.xhtml")
	public String moviePlayVideo(@RequestParam("mid")Long mid,ModelMap model,HttpServletRequest request,
			HttpServletResponse response){
		String citycode = WebUtils.getAndSetDefault(request, response);
		Movie movie = daoService.getObject(Movie.class, mid);
		if (movie == null){
			return show404(model, "电影不存在或已经删除！");
		}
		List<Video> videos = videoService.getVideoListByTag(Video.VIDEOTYPE_FILM, mid,null,"orderNum",true,0, 1);
		if(VmUtils.isEmptyList(videos)){
			return show404(model, "播放影片不存在或已经删除！");
		}
		Video video = videos.get(0);
		int clickedTimes = video.getClickedtimes() == null ? 0:video.getClickedtimes();
		video.setClickedtimes(++clickedTimes);
		daoService.updateObject(video);
		if(StringUtils.isBlank(video.getUrl()) || video.getUrl().indexOf("autoPlay=1&") == -1){
			return show404(model, "播放影片不存在或已经删除！");
		}
		String[] vids = video.getUrl().split("autoPlay=1&");
		if(vids.length != 2){
			return show404(model, "播放影片不存在或已经删除！");
		}
		String[] tvids = StringUtils.split(vids[1],"&");
		if(tvids.length != 2){
			return show404(model, "播放影片不存在或已经删除！");
		}
		model.put("tvId", tvids[0]);
		model.put("vid", tvids[1]);
		//热映购票排行榜
		List<Movie> rankMovieList= mcpService.getHotPlayMovieList(citycode);
		Collections.sort(rankMovieList, new MultiPropertyComparator(new String[]{"boughtcount"}, new boolean[]{false}));
		rankMovieList =BeanUtil.getSubList(rankMovieList, 0, 10);
		model.put("rankMovieList", rankMovieList);
		//热门活动
		ErrorCode<List<RemoteActivity>> result = synchActivityService.getActivityListByOrder(citycode, null, RemoteActivity.TIME_CURRENT, TagConstant.TAG_CINEMA, null, null, null, "duetime", 0, 4);
		if(result.isSuccess()) {
			model.put("activityList", result.getRetval());
		}
		model.put("video", video);
		model.put("movie", movie);
		model.put("commentCount",commentService.getCommentCountByRelatedId("movie", mid));
		List<Diary> diaryList = diaryService.getDiaryList(Diary.class, citycode, DiaryConstant.DIARY_TYPE_COMMENT, "movie", mid, 0, 5, "poohnum");
		model.put("diaryList",diaryList);
		addCacheMember(model, ServiceHelper.getMemberIdListFromBeanList(diaryList));
		model.put("diaryCount",diaryService.getDiaryCount(Diary.class, citycode, DiaryConstant.DIARY_TYPE_COMMENT, "movie", mid));
		moreLikeMovie(movie,model);
		return "movie/onlinePlay/play.vm";
	}
	
	private void moreLikeMovie(Movie movie,ModelMap model){
		DetachedCriteria query = DetachedCriteria.forClass(Movie.class,"m");
		String searchMovieName = movie.getName();
		query.add(Restrictions.ne("m.id", movie.getId()));
		Criterion nameOrCriterion = Restrictions.or(Restrictions.like("m.moviename", searchMovieName,MatchMode.ANYWHERE), 
				  Restrictions.or(Restrictions.like("m.director", searchMovieName,MatchMode.ANYWHERE), 
				  		  Restrictions.or(Restrictions.like("m.actors", searchMovieName,MatchMode.ANYWHERE), 
				  				  		  Restrictions.or(Restrictions.like("m.highlight", searchMovieName,MatchMode.ANYWHERE),
				  				  				  		  Restrictions.like("m.flag", searchMovieName,MatchMode.ANYWHERE)))));
		Criterion typeOrCriterion = null;
		String[] types = StringUtils.split(movie.getType(),"/");
		if(types != null && types.length > 0){
			int length = types.length;
			if(length == 1){
				typeOrCriterion = Restrictions.like("m.type",types[0],MatchMode.ANYWHERE);
			}else if(length == 2){
				typeOrCriterion = Restrictions.or(Restrictions.like("m.type",types[0],MatchMode.ANYWHERE),Restrictions.like("m.type",types[1],MatchMode.ANYWHERE));
			}else if(length == 3){
				typeOrCriterion = Restrictions.or(Restrictions.like("m.type",types[0],MatchMode.ANYWHERE),
						Restrictions.or(Restrictions.like("m.type",types[1],MatchMode.ANYWHERE),Restrictions.like("m.type",types[2],MatchMode.ANYWHERE)));
			}else{
				typeOrCriterion = Restrictions.or(Restrictions.like("m.type",types[0],MatchMode.ANYWHERE),
						Restrictions.or(Restrictions.like("m.type",types[1],MatchMode.ANYWHERE),
						Restrictions.or(Restrictions.like("m.type",types[2],MatchMode.ANYWHERE),Restrictions.like("m.type",types[3],MatchMode.ANYWHERE))));
			}
		}
		if(typeOrCriterion != null){
			query.add(Restrictions.or(typeOrCriterion,nameOrCriterion));
		}else{
			query.add(nameOrCriterion);
		}
		DetachedCriteria subQuery = DetachedCriteria.forClass(Video.class,"v");
		subQuery.add(Restrictions.eq("v.tag", Video.VIDEOTYPE_FILM));
		subQuery.setProjection(Projections.property("v.relatedid"));
		query.add(Subqueries.propertyIn("m.id",subQuery));
		List<Movie> movies = hibernateTemplate.findByCriteria(query, 0, 4);
		model.put("moreLikeMovies",movies);
	}

	@RequestMapping("/movie/movieDiaryList.xhtml")
	public String movieDiaryList(Long mid,ModelMap model){
		return showRedirect("/movie/" + mid + "?diaryList=true", model);
	}
	
	@RequestMapping("/movie/ajax/movieDiaryList.xhtml")
	public String movieDiaryList(@CookieValue(value = LOGIN_COOKIE_NAME, required = false)
			String sessid, @RequestParam("mid")Long mid, String myOrder, String friend, Integer pageNo,Integer maxnum, 
			ModelMap model, HttpServletRequest request, HttpServletResponse response) {
		Movie movie = daoService.getObject(Movie.class, mid);
		String citycode = WebUtils.getAndSetDefault(request, response);
		if (movie == null)
			return show404(model, "电影不存在或已经删除！");
		model.put("movie", movie);
		if (pageNo == null)
			pageNo = 0;
		if(maxnum == null || maxnum > 20){
			maxnum = 20;
		}
		int rowsPerPage = maxnum;
		int first = rowsPerPage * pageNo;
		int rowsCount = 0;
		List<Diary> diaryList = new ArrayList<Diary>();
		if (StringUtils.isBlank(myOrder))
			myOrder = "poohnum";//addtime
		Member member = loginService.getLogonMemberBySessid(WebUtils.getRemoteIp(request), sessid);
		if (StringUtils.isNotBlank(friend) && member != null) {
			diaryList = diaryService.getFriendDiaryList(DiaryConstant.DIARY_TYPE_COMMENT, "movie", mid, member.getId(), first, rowsPerPage);
			rowsCount = diaryList.size();
			model.put("friend", true);
		} else {
			rowsCount = diaryService.getDiaryCount(Diary.class, citycode, DiaryConstant.DIARY_TYPE_COMMENT, "movie", mid);
			diaryList = diaryService.getDiaryList(Diary.class, citycode, DiaryConstant.DIARY_TYPE_COMMENT, "movie", mid, first, rowsPerPage, myOrder);
		}
		addCacheMember(model, ServiceHelper.getMemberIdListFromBeanList(diaryList));
		model.put("diaryList", diaryList);
		PageUtil pageUtil = new PageUtil(rowsCount, rowsPerPage, pageNo, "/movie/ajax/movieDiaryList.xhtml", true, true);
		Map params = new HashMap();
		params.put("mid", mid);
		params.put("myOrder", myOrder);
		params.put("maxnum", rowsPerPage);
		params.put("friend", friend);
		params.put("movieDetail", request.getParameter("movieDetail"));
		pageUtil.initPageInfo(params);
		model.put("pageUtil", pageUtil);
		model.put("rowsCount", rowsCount);
		getHeadNav(citycode,mid,model);
		return "movie/wide_ajax_cinecism.vm";
	}
	
	@RequestMapping("/movie/movieWala.xhtml")
	public String movieWala(@RequestParam("mid")Long mid, ModelMap model, HttpServletRequest request, HttpServletResponse response) {
		Movie movie = daoService.getObject(Movie.class, mid);
		String citycode = WebUtils.getAndSetDefault(request, response);
		if (movie == null){
			return show404(model, "电影不存在或已经删除！");
		}
		model.put("movie", movie);
		getHeadNav(citycode,mid,model);
		return "movie/wide_ajax_movieWala.vm";
	}
	
	//电影剧情介绍
	@RequestMapping("/movie/movieInfoDetail.xhtml")
	public String movieInfoDetail(@RequestParam("mid")Long mid, ModelMap model, HttpServletRequest request, HttpServletResponse response){
		Movie movie = daoService.getObject(Movie.class, mid);
		String citycode = WebUtils.getAndSetDefault(request, response);
		model.put("movie", movie);
		getHeadNav(citycode,mid,model);
		int pictureCount = pictureService.getPictureCountByRelatedid("movie", movie.getId());
		int memberPictureCount = pictureService.getMemberPictureCount(mid, "movie", null, TagConstant.FLAG_PIC, Status.Y);
		model.putAll(pictureComponent.getCommonData(TagConstant.TAG_MOVIE,citycode, movie.getId()));
		model.put("movie", movie);
		model.put("pictureCount", pictureCount);
		model.put("memberPictureCount", memberPictureCount);
		getHeadNav(citycode,mid,model);
		return "movie/wide_ajax_movieSynopsis.vm";
	}
	/**
	 * 电影详情中部导航
	 * @param citycode
	 * @param mId
	 * @param model
	 */
	private void getHeadNav(String citycode,long mId,ModelMap model){
		Integer commentCount = commentService.getCommentCountByRelatedId("movie", mId);
		model.put("commentCount", commentCount);
		Integer diaryCount = diaryService.getDiaryCount(Diary.class, citycode, DiaryConstant.DIARY_TYPE_COMMENT, "movie", mId);
		model.put("diaryCount", diaryCount);
		int newscount = newsService.getNewsCount(citycode, TagConstant.TAG_MOVIE, "", mId, null);
		model.put("newsCount", newscount);
	}
	
	
	@RequestMapping("/movie/movieNewsList.xhtml")
	public String newMovieNewsDetail(@RequestParam("mid")Long mid, Integer pageNo, 
			ModelMap model, HttpServletRequest request, HttpServletResponse response) {
		Movie movie = daoService.getObject(Movie.class, mid);
		if (movie == null)
			return show404(model, "电影不存在或已经删除！");
		model.putAll(pictureComponent.getHeadData(TagConstant.TAG_MOVIE, mid));
		String citycode = WebUtils.getAndSetDefault(request, response);
		if (pageNo == null)
			pageNo = 0;
		int rowsPerPage = 10;
		int firstPerPage = pageNo * rowsPerPage;
		List<News> newsList = newsService.getNewsList(citycode, "movie", movie.getId(), "", firstPerPage, rowsPerPage);
		int count = newsService.getNewsCount(citycode, "movie", "", movie.getId(), null);
		PageUtil pageUtil = new PageUtil(count, rowsPerPage, pageNo, "/movie/movieNewsList.xhtml", true, true);
		Map params = new HashMap();
		params.put("pageNo", pageNo);
		params.put("mid", movie.getId());
		pageUtil.initPageInfo(params);
		model.put("pageUtil", pageUtil);
		model.put("newsList", newsList);
		model.put("movie", movie);
		this.getHeadNav(citycode, mid, model);
		return "movie/wide_ajax_movieInformation.vm";
	}
	
	@RequestMapping("movie/videoList.xhtml")
	public String newMovieVideoList(Long mid, Long vid, ModelMap model){
		if (mid == null && vid == null)
			return this.showJsonError(model, "参数错误");
		Movie movie = null;
		Video video = null;
		MemberPicture memberPicture = null;
		if (mid != null && vid != null) {
			movie = daoService.getObject(Movie.class, mid);
			if (movie == null)
				return showJsonError(model, "电影不存在或已经删除！");
			video = daoService.getObject(Video.class, vid);
			if (video == null) {
				memberPicture = daoService.getObject(MemberPicture.class, vid);
				if (memberPicture == null)
					return showJsonError(model,  "该视频不存在或已删除！");
				Movie curMovie = daoService.getObject(Movie.class, memberPicture.getRelatedid());
				if (curMovie == null)
					return showJsonError(model,  "该视频关联不是关联该电影！");
				if (!movie.getId().equals(curMovie.getId()))
					return showJsonError(model,  "参数错误！");
				model.put("vtag", "member");
			} else {
				Movie curMovie = daoService.getObject(Movie.class, video.getRelatedid());
				if (curMovie == null)
					return showJsonError(model,  "该视频关联不是关联该电影！");
				if (!movie.getId().equals(curMovie.getId()))
					return showJsonError(model,  "参数错误！");
			}
		} else if (vid != null) {
			video = daoService.getObject(Video.class, vid);
			if (video == null) {
				memberPicture = daoService.getObject(MemberPicture.class, vid);
				if (memberPicture == null)
					return showJsonError(model,  "该视频不存在或已删除！");
				movie = daoService.getObject(Movie.class, memberPicture.getRelatedid());
				if (movie == null)
					return showJsonError(model,  "该视频关联不是关联该电影！");
				model.put("vtag", "member");
			} else {
				movie = daoService.getObject(Movie.class, video.getRelatedid());
				if (movie == null)
					return showJsonError(model,  "该视频关联不是关联该电影！");
				model.put("vtag", "user");
			}
		} else {
			movie = daoService.getObject(Movie.class, mid);
			if (movie == null)
				return showJsonError(model,  "电影不存在或已经删除！");
		}
		if (mid == null){
			mid = movie.getId();
		}
		// 视频列表
		List<Video> videoList = videoService.getVideoListByTag("movie", movie.getId(),null,"orderNum",true, 0, 1000);
		List<Map<String,String>> videos = new LinkedList<Map<String,String>>();
		if (video != null) {
			videoList.remove(video);
			videoList.add(0, video);
		}
		for(Video v : videoList){
			Map<String,String> vm = new HashMap<String,String>();
			vm.put("picturename",v.getLimg());
			vm.put("minpic",(StringUtils.indexOf(v.getLimg(), "http") == -1 ? "cw96h72/" : "" )+v.getLimg());
			vm.put("description", v.getContent());
			vm.put("url", v.getUrl());
			vm.put("id", v.getId() + "");
			vm.put("titile",v.getVideotitle());
			vm.put("addTime",DateUtil.format(v.getAddtime(), "yyyy-MM-dd"));
			videos.add(vm);
		}
		List<MemberPicture> memberVideoList = pictureService.getMemberPictureList(movie.getId(), TagConstant.TAG_MOVIE, null, TagConstant.FLAG_VIDEO,
				Status.Y, 0, 1000);
		if (memberPicture != null) {
			memberVideoList.remove(memberPicture);
			memberVideoList.add(0, memberPicture);
		}
		for(MemberPicture v : memberVideoList){
			Map<String,String> vm = new HashMap<String,String>();
			vm.put("picturename",v.getLimg());
			vm.put("minpic",(StringUtils.indexOf(v.getLimg(), "http") == -1 ? "cw96h72/" : "" ) + v.getLimg());
			vm.put("description", v.getMembername());
			vm.put("url", v.getDescription());
			vm.put("id", v.getId() + "");
			vm.put("titile",v.getName());
			vm.put("addTime",DateUtil.format(v.getAddtime(), "yyyy-MM-dd"));
			videos.add(vm);
		}
		Map jsonMap = new HashMap();
		jsonMap.put("mid", mid);
		jsonMap.put("vid", vid);
		jsonMap.put("videoList", videos);
		return showJsonSuccess(model, jsonMap);
	}
	
	@RequestMapping("/movie/videoDetail.xhtml")
	public String newMovieVideoDetail(Long mid, Long vid, ModelMap model) {
		if (mid == null && vid == null)
			return this.show404(model, "参数错误");
		if(mid == null){
			Video video = daoService.getObject(Video.class, vid);
			if(video == null){
				return this.show404(model, "参数错误");	
			}
			mid = video.getRelatedid();
		}
		return showRedirect("/movie/" + mid + "?videoList=true&vid=" + (vid == null ? "":vid), model);
	}

	// 电影剧照详细
	@RequestMapping("/movie/moviePictureDetail.xhtml")
	public String newMoviePicture(ModelMap model, Long mid, Long pid, String pvtype) {
		Movie movie = daoService.getObject(Movie.class, mid);
		pictureComponent.pictureDetail(model, TagConstant.TAG_MOVIE, mid, pid, pvtype);
		if (movie == null) {
			Object obj = model.get(TagConstant.TAG_MOVIE);
			if (obj == null)
				return show404(model, "该电影不存在或被删除！");
			movie = (Movie) obj;
		}
		List<Map> mapList =  (List<Map>)model.get("mapList");
		List<Map<String,Object>> pics = new LinkedList<Map<String,Object>>();
		if(mapList != null){
			for(Map m : mapList){
				Map<String,Object> vm = new HashMap<String,Object>();
				vm.put("picturename",m.get("picturename"));
				vm.put("minpic","cw96h72/"+m.get("picturename"));
				vm.put("description", m.get("description"));
				vm.put("membername", m.get("membername"));
				vm.put("posttime", m.get("posttime"));
				vm.put("id",m.get("id"));
				pics.add(vm);
			}
		}
		Map jsonMap = new HashMap();
		jsonMap.put("mid", mid);
		jsonMap.put("vid", pid);
		jsonMap.put("pvtype", pvtype);
		jsonMap.put("pictureid", model.get("pictureid"));
		jsonMap.put("type", model.get("type"));
		jsonMap.put("pictureList", pics);
		return showJsonSuccess(model, jsonMap);
	}

	@RequestMapping("/movie/attachVedio.xhtml")
	public String attachVedio(ModelMap model, Long mid, HttpServletRequest request, HttpServletResponse response) {
		String citycode = WebUtils.getAndSetDefault(request, response);
		Movie movie = daoService.getObject(Movie.class, mid);
		if (movie == null) return show404(model, "电影不存在或已经删除！");
		model.put("movie", movie);
		model.putAll(pictureComponent.getCommonData(TagConstant.TAG_MOVIE,citycode, mid));
		model.putAll(pictureComponent.getHeadData(TagConstant.TAG_MOVIE, mid));
		List<Date> playdateList = mcpService.getCurMoviePlayDate2(citycode, mid);
		model.put("playdateList", playdateList);
		getMovieInfo(mid, citycode, model);
		return "movie/new_attachVideo.vm";
	}


	private Map getTicketOrderMemberMapByMovieid(Long movieid, Integer maxnum) {
		Map map = new HashMap();
		List<Map> payMemberList = untransService.getPayMemberListByTagAndId(TagConstant.TAG_MOVIE, movieid, 0, maxnum);
		if (payMemberList.size() == maxnum) {
			map.put("payMemberList", payMemberList);
			Map<String, Map> memberMap = new HashMap<String, Map>();
			for (Map order : payMemberList) {
				Map memMap = memberService.getCacheMemberInfoMap((Long) order.get("memberid"));
				memberMap.put(order.get("tradeNo") + "", memMap);
			}
			map.put("memberMap", memberMap);
		}
		return map;
	}

	// 网友增加电影
	@RequestMapping("/movie/userAddMovie.xhtml")
	public String userAddMovie(HttpServletRequest request, ModelMap model) {
		String referer = request.getHeader("Referer");
		model.put("referer", referer);
		return "movie/new_addMovie.vm";
	}

	@RequestMapping("/movie/ajax/getCountyBooking.xhtml")
	public String getCountBooking(@CookieValue(value = LOGIN_COOKIE_NAME, required = false)
	String sessid, Long movieid, ModelMap model, HttpServletRequest request, HttpServletResponse response) {
		Movie movie = daoService.getObject(Movie.class, movieid);
		if (movie == null)
			return showJsonError(model, "该电影不存在或被删除！");
		String citycode = WebUtils.getAndSetDefault(request, response);
		// 区域
		List<County> countyList = getCountyByCinemaBooking(citycode);
		model.put("countyList", countyList);
		model.put("movie", movie);
		Member logonMember = loginService.getLogonMemberBySessid(WebUtils.getRemoteIp(request), sessid);
		if(logonMember != null){
			model.put("logonMember", logonMember);
			addCacheMember(model, logonMember.getId());
		}
		return "movie/moviePlayItemTemplate.vm";
	}

	@RequestMapping("/movie/getCinemaByCounty.xhtml")
	public String getCinemaByCounty(Long movieid, String countycode, ModelMap model, HttpServletRequest request, HttpServletResponse response) {
		Movie movie = daoService.getObject(Movie.class, movieid);
		if (movie == null)
			return showJsonError(model, "该影片不存在或被删除！");
		String citycode = WebUtils.getAndSetDefault(request, response);
		List<Cinema> cinemaList = getCinemaList(citycode, countycode);
		List<Map> cinemaMapList = BeanUtil.getBeanMapList(cinemaList, false, "id", "realBriefname");
		Map result = new HashMap();
		result.put("cinemaMap", cinemaMapList);
		return showJsonSuccess(model, result);
	}

	private List<Cinema> getCinemaList(String citycode, String countycode) {
		DetachedCriteria query = DetachedCriteria.forClass(Cinema.class);
		query.add(Restrictions.eq("citycode", citycode));
		query.add(Restrictions.eq("countycode", countycode));
		query.add(Restrictions.eq("booking", Cinema.BOOKING_OPEN));
		query.addOrder(Order.desc("generalmark"));
		List<Cinema> cinemaList = hibernateTemplate.findByCriteria(query);
		return cinemaList;
	}

	private List<County> getCountyByCinemaBooking(String citycode) {
		DetachedCriteria query = DetachedCriteria.forClass(County.class, "c");
		DetachedCriteria subQuery = DetachedCriteria.forClass(Cinema.class, "m");
		subQuery.add(Restrictions.eqProperty("m.countycode", "c.countycode"));
		subQuery.add(Restrictions.eq("m.citycode", citycode));
		subQuery.add(Restrictions.eq("m.booking", Cinema.BOOKING_OPEN));
		subQuery.add(Restrictions.isNotNull("m.countycode"));
		subQuery.setProjection(Projections.property("m.countycode"));
		query.add(Subqueries.exists(subQuery));
		List<County> countyList = hibernateTemplate.findByCriteria(query);
		return countyList;
	}

	@RequestMapping("/movie/getDateByCinema.xhtml")
	public String getDateByCinema(Long movieid, Long cinemaid, ModelMap model) {
		Movie movie = daoService.getObject(Movie.class, movieid);
		if (movie == null)
			return showJsonError(model, "该影片不存在或被删除！");
		Cinema cinema = daoService.getObject(Cinema.class, cinemaid);
		if (cinema == null)
			return showJsonError(model, "该影院不存在或被删除！");
		List<Date> dateList = openPlayService.getCinemaAndMovieOpenDateList(cinemaid, movieid);
		List<Map> dateMap = new ArrayList<Map>();
		Date cur = DateUtil.getCurDate();
		for (int i = 0; i < 10; i++) {
			Date tmp = DateUtil.addDay(cur, i);
			if (!dateList.contains(tmp)) {
				Map map = new HashMap();
				map.put("playtime", DateUtil.format(tmp, "yyyy-MM-dd"));
				map.put("playdate", DateUtil.format(tmp, "M月d日 " + DateUtil.getCnWeek(tmp)));
				dateMap.add(map);
			}
		}
		Map result = new HashMap();
		result.put("dateMap", dateMap);
		return showJsonSuccess(model, result);
	}

	@RequestMapping("/ajax/movie/isAddPlayItemMessage.xhtml")
	public String isAddPlayItemMessage(@CookieValue(value = LOGIN_COOKIE_NAME, required = false)String sessid, HttpServletRequest request,
			String mptag, Long mpcategoryid, ModelMap model) {
		String ip = WebUtils.getRemoteIp(request);
		Member member = loginService.getLogonMemberBySessid(ip, sessid);
		if(member == null){
			return showJsonError(model,"未登录");
		}
		Map params = new HashMap();
		params.put("tag", mptag);
		params.put("categoryid", mpcategoryid);
		params.put("memberid", member.getId());
		List<PlayItemMessage> playItemList = mongoService.find(PlayItemMessage.class, params);
		if (!playItemList.isEmpty()){
			return showJsonSuccess(model,"true");
		}
		return showJsonError(model,"未添加");
	}
	// 保存短信排片
	@RequestMapping("/ajax/movie/savePlayItemMessage.xhtml")
	public String savePlayItemMessage(@CookieValue(value = LOGIN_COOKIE_NAME, required = false)String sessid, HttpServletRequest request,
			String mptag, Long mprelatedid, Long mpcategoryid, String playdate, String mobile, String captchaId, String captcha, ModelMap model,String type) {
		boolean isValidCaptcha = controllerService.validateCaptcha(captchaId, captcha, WebUtils.getRemoteIp(request));
		if(!isValidCaptcha) return showJsonError_CAPTCHA_ERROR(model);
		String ip = WebUtils.getRemoteIp(request);
		Member member = loginService.getLogonMemberBySessid(ip, sessid);
		String msg = null;
		if (member == null) return showJsonError(model, "请先登录！");
		if (!ValidateUtil.isMobile(mobile))return showJsonError(model, "手机号格式不合法！");
		if (!PlayItemMessage.TAG_LIST.contains(mptag))return showJsonError(model, "参数错误！");	
		Date playDate = null;
		if(type == null){
			if(!DateUtil.isValidDate(playdate))return showJsonError(model, "时间格式不对或不能为空！");
			playDate = DateUtil.parseDate(playdate);
			if (playDate.after(DateUtil.addDay(DateUtil.getCurDate(), 30)))return showJsonError(model, "时间错误！");		
		}
		String opkey = "playitem" + WebUtils.getRemoteIp(request);
		boolean allow = operationService.isAllowOperation(opkey, 30, OperationService.ONE_DAY, 3);
		if (!allow)	return showJsonError(model, "你操作过于频繁，请稍后再试！");
		Cinema cinema = null;
		if (StringUtils.equals(mptag, "cinema")) {
			Movie movie = daoService.getObject(Movie.class, mpcategoryid);
			if (movie == null)return showJsonError(model, "关联电影不存在！");
			Map params = new HashMap();
			params.put("tag", mptag);
			params.put("categoryid", mpcategoryid);
			params.put("memberid", member.getId());
			if(type == null){
				cinema = daoService.getObject(Cinema.class, mprelatedid);
				if (cinema == null)return showJsonError(model, "关联电影院不存在！");
				Timestamp starttime = new Timestamp(playDate.getTime());
				Timestamp endtime = new Timestamp(DateUtil.getLastTimeOfDay(playDate).getTime());
				List<OpenPlayItem> opiList = openPlayService.getOpiList(null, mprelatedid, mpcategoryid, starttime, endtime, true, 1);
				if (!opiList.isEmpty())	return showJsonError(model, "当前时期已有排片！");	
				params.put("relatedid", mprelatedid);
				params.put("playdate", playDate);
				msg = DateUtil.format(playDate, "MM月dd日") + " " + cinema.getRealBriefname() + " " + movie.getRealBriefname()
						+ "已开放购票，手机登陆 http://t.cn/Sb2z2G 查看，安装客户端买票更方便";
			}else{
				playDate = movie.getReleasedate();
				msg = movie.getName()+ "已开放购票，可登陆 http://www.gewara.com 或下载电影客户端购票 http://t.cn/Sb2z2G ";
			}
			List<PlayItemMessage> playItemList = mongoService.find(PlayItemMessage.class, params);
			if (!playItemList.isEmpty())return showJsonError(model, "不能重复添加！");
			int random = (int)(Math.random()*5)+1;
			movie.setQuguo(movie.getQuguo()+1);
			movie.setXiangqu(movie.getXiangqu()+random);
			daoService.saveObject(movie);
			PlayItemMessage playItemMessage = new PlayItemMessage(mptag, mprelatedid, mpcategoryid, playDate, mobile, AddressConstant.ADDRESS_WEB);
			playItemMessage.setId(ServiceHelper.assignID(mobile));
			playItemMessage.setMemberid(member.getId());
			playItemMessage.setMsg(msg);
			mongoService.saveOrUpdateObject(playItemMessage, MongoData.DEFAULT_ID_NAME);
			operationService.updateOperation(opkey, 30, OperationService.ONE_DAY, 3);
			return showJsonSuccess(model);
		}
		return showJsonError(model, "添加失败！");
	}

	// 动态加载图片列表
	@RequestMapping("/movie/ajax/moviePictureList.xhtml")
	public String moviePictureList(ModelMap model, Long relatedid, Integer pageNo, String type) {
		pictureComponent.pictureList(model, pageNo, TagConstant.TAG_MOVIE, relatedid, type, "/movie/ajax/moviePictureList.xhtml");
		return "movie/wide_ajaxMoviePictureList.vm";
	}
	//知道
	private void getQaList(Long mid, ModelMap model){
		List<GewaQuestion> qnList = qaService.getQuestionListByQsAndTagAndRelatedid("movie", mid, GewaQuestion.QS_STATUS_N, "addtime", 5);
		model.put("qnList", qnList);
	}
	//视频
	private void getVideoList(Long mid, ModelMap model){
		List<Video> videoList = videoService.getVideoListByTag("movie", mid,null,"orderNum",true, 0, 3);
		model.put("videoRList", videoList);
	}
	//剧照
	private void getPictureList(Long mid, ModelMap model){
		List<Picture> pictureList = pictureService.getPictureListByRelatedid("movie", mid, 0, 5);
		model.put("pictureList", pictureList);
	}
	//新闻
	private void getNewsList(Long mid, String citycode, ModelMap model){
		List<News> newsList = newsService.getNewsList(citycode, "movie", mid, "", 0, 5);
		model.put("movieNewsListFirst",true);
		model.put("movieNewsList", newsList);
	}
	//活动
	private void getActivityList(Long mid, String citycode, ModelMap model){
		ErrorCode<List<RemoteActivity>> code = synchActivityService.getActivityListByOrder(citycode, RemoteActivity.ATYPE_GEWA, RemoteActivity.TIME_CURRENT, null, null, TagConstant.TAG_MOVIE, mid, null, 0, 3);
		if(code.isSuccess()){
			List<RemoteActivity> activityList = code.getRetval();
			model.put("activityList", activityList);
			RelatedHelper rh = new RelatedHelper();
			model.put("relatedHelper", rh);
			List<Serializable> cinemaIdList = BeanUtil.getBeanPropertyList(activityList, Serializable.class, "relatedid", true);
			relateService.addRelatedObject(1, "activityList", rh, TagConstant.TAG_CINEMA, cinemaIdList);
		}
	}
	
	//电影论坛
	private void getDiaryList(Long mid, String citycode,ModelMap model){
		List<Diary> diaryList = diaryService.getDiaryList(Diary.class, citycode, DiaryConstant.DIARY_TYPE_COMMENT, "movie", mid, 0, 5, "poohnum");
		model.put("gcList",diaryList);
		addCacheMember(model, ServiceHelper.getMemberIdListFromBeanList(diaryList));
	}
	private void getMovieInfo(Long mid, String citycode, ModelMap model) {
		model.put("movieDetail", true);
		Movie movie = daoService.getObject(Movie.class, mid);
		if(movie != null){
			if(movie.getReleasedate() != null && movie.getReleasedate().after(DateUtil.getCurDate()) 
					&& !openPlayService.getOpiMovieidList(citycode, null).contains(mid)){
				model.put("isFutureMovie", true);
			}else{
				model.put("isFutureMovie", false);
			}
			Integer diaryCount = diaryService.getDiaryCount(Diary.class, citycode, DiaryConstant.DIARY_TYPE_COMMENT, "movie", mid);
			model.put("diaryCount", diaryCount);
			Integer commentCount = commentService.getCommentCountByRelatedId("movie", mid);
			model.put("commentCount", commentCount);
			List<Comment> commentList = commentService.getCommentListByRelatedId("movie",null,mid, null, 0, 6);
			addCacheMember(model, ServiceHelper.getMemberIdListFromBeanList(commentList));
			model.put("commentList", commentList);
			model.putAll(pictureComponent.getCommonData(TagConstant.TAG_MOVIE,citycode, movie.getId()));
			MarkCountData markCount = markService.getMarkCountByTagRelatedid(TagConstant.TAG_MOVIE, movie.getId());
			// 评分统计
			model.put("markCount", markCount);
			model.put("gradeDetailList",markService.getGradeDetail(TagConstant.TAG_MOVIE, movie.getId()));
			model.put("markData", markService.getMarkdata(TagConstant.TAG_MOVIE));
			model.put("markHelper", new MarkHelper());
			model.putAll(markService.getPercentCount("movie", movie.getId()));
		}
	}
	@RequestMapping("/movie/ajax/getSpdiscountList.xhtml")
	public String spdiscountList(ModelMap model,Long mpid){
		OpenPlayItem openPlayItem = daoService.getObjectByUkey(OpenPlayItem.class, "mpid", mpid, true);
		if (openPlayItem == null) return showJsonError(model, "优惠方式不存在！");
		OpiSpdiscountFilter opiSpdiscountFilter = new OpiSpdiscountFilter(openPlayItem, DateUtil.getCurFullTimestamp());
		List<SpecialDiscount> sdList= spdiscountService.getSpecialDiscountData(opiSpdiscountFilter, SpecialDiscount.OPENTYPE_GEWA, PayConstant.APPLY_TAG_MOVIE);
		Map dataMap = new HashMap();
		dataMap.put("sdList", sdList);
		String result = velocityTemplate.parseTemplate("movie/ajaxMovieDiscount.vm", dataMap);
		return showJsonSuccess(model, result);
	}
	private void spdiscountList(ModelMap model,String citycode ,Long movieId){
		ObjectSpdiscountFilter osf = new ObjectSpdiscountFilter(citycode, PayConstant.APPLY_TAG_MOVIE, movieId, DateUtil.getCurFullTimestamp());
		List<SpecialDiscount> spdiscountList  =  spdiscountService.getSpecialDiscountData(osf, SpecialDiscount.OPENTYPE_GEWA, PayConstant.APPLY_TAG_MOVIE);		
		Map<Long, String> spidMap = new HashMap<Long, String>();
		for(SpecialDiscount sd:spdiscountList){
			spidMap.put(sd.getId(), PKCoderUtil.encryptString(""+sd.getId(), SpecialDiscount.ENCODE_KEY));
		}
		model.put("spidMap", spidMap);
		model.put("spdiscountList", spdiscountList);	
	}
}
