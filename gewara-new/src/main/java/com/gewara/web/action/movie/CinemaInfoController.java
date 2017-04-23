package com.gewara.web.action.movie;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.type.TypeReference;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.ProjectionList;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.gewara.constant.CharacteristicType;
import com.gewara.constant.Flag;
import com.gewara.constant.GoodsConstant;
import com.gewara.constant.PayConstant;
import com.gewara.constant.SmsConstant;
import com.gewara.constant.Status;
import com.gewara.constant.TagConstant;
import com.gewara.constant.content.SignName;
import com.gewara.constant.ticket.PartnerConstant;
import com.gewara.helper.GoodsFilterHelper;
import com.gewara.json.PageView;
import com.gewara.model.acl.GewaraUser;
import com.gewara.model.bbs.Diary;
import com.gewara.model.common.Relationship;
import com.gewara.model.common.Subwaystation;
import com.gewara.model.common.UserOperation;
import com.gewara.model.content.Bulletin;
import com.gewara.model.content.GewaCommend;
import com.gewara.model.content.HeadInfo;
import com.gewara.model.content.News;
import com.gewara.model.content.Picture;
import com.gewara.model.goods.Goods;
import com.gewara.model.movie.Cinema;
import com.gewara.model.movie.CinemaProfile;
import com.gewara.model.movie.CinemaRoom;
import com.gewara.model.movie.Movie;
import com.gewara.model.movie.MoviePlayItem;
import com.gewara.model.pay.SMSRecord;
import com.gewara.model.pay.SpecialDiscount;
import com.gewara.model.sport.Sport;
import com.gewara.model.user.Member;
import com.gewara.model.user.MemberPicture;
import com.gewara.service.OperationService;
import com.gewara.service.PlaceService;
import com.gewara.service.bbs.DiaryService;
import com.gewara.service.bbs.MarkService;
import com.gewara.service.content.NewsService;
import com.gewara.service.content.PictureService;
import com.gewara.service.content.VideoService;
import com.gewara.service.movie.MCPService;
import com.gewara.service.order.GoodsService;
import com.gewara.service.ticket.OpenPlayService;
import com.gewara.support.ErrorCode;
import com.gewara.untrans.CacheDataService;
import com.gewara.untrans.CommentService;
import com.gewara.untrans.CommonService;
import com.gewara.untrans.MailService;
import com.gewara.untrans.PageCacheService;
import com.gewara.untrans.PageParams;
import com.gewara.untrans.UntransService;
import com.gewara.untrans.activity.SynchActivityService;
import com.gewara.untrans.impl.ControllerService;
import com.gewara.untrans.order.impl.SpdiscountService;
import com.gewara.util.BeanUtil;
import com.gewara.util.DateUtil;
import com.gewara.util.JsonUtils;
import com.gewara.util.MarkHelper;
import com.gewara.util.PKCoderUtil;
import com.gewara.util.RelatedHelper;
import com.gewara.util.ValidateUtil;
import com.gewara.util.VmUtils;
import com.gewara.util.WebUtils;
import com.gewara.web.action.AnnotationController;
import com.gewara.web.action.partner.CinemaSpdiscountFilter;
import com.gewara.xmlbind.activity.RemoteActivity;

@Controller
public class CinemaInfoController extends AnnotationController {
	@Autowired@Qualifier("hibernateTemplate")
	private HibernateTemplate hibernateTemplate;
	public void setHibernateTemplate(HibernateTemplate hibernateTemplate) {
		this.hibernateTemplate = hibernateTemplate;
	}
	@Autowired@Qualifier("openPlayService")
	private OpenPlayService openPlayService;
	@Autowired@Qualifier("cacheDataService")
	private CacheDataService cacheDataService;
	@Autowired@Qualifier("pageCacheService")
	private PageCacheService pageCacheService;
	public void setPageCacheService(PageCacheService pageCacheService) {
		this.pageCacheService = pageCacheService;
	}
	@Autowired@Qualifier("commonService")
	private CommonService commonService;
	public void setCommonService(CommonService commonService) {
		this.commonService = commonService;
	}	
	@Autowired@Qualifier("mcpService")
	private MCPService mcpService;
	public void setMcpService(MCPService mcpService) {
		this.mcpService = mcpService;
	}
	@Autowired@Qualifier("pictureService")
	private PictureService pictureService;
	public void setPictureService(PictureService pictureService) {
		this.pictureService = pictureService;
	}
	@Autowired@Qualifier("untransService")
	private UntransService untransService;
	public void setUntransService(UntransService untransService) {
		this.untransService = untransService;
	}
	@Autowired@Qualifier("commentService")
	private CommentService commentService;
	public void setCommentService(CommentService commentService) {
		this.commentService = commentService;
	}
	@Autowired@Qualifier("diaryService")
	private DiaryService diaryService;
	public void setDiaryService(DiaryService diaryService) {
		this.diaryService = diaryService;
	}
	@Autowired@Qualifier("newsService")
	private NewsService newsService;
	public void setNewsService(NewsService newsService) {
		this.newsService = newsService;
	}
	@Autowired@Qualifier("synchActivityService")
	private SynchActivityService synchActivityService;
	public void setActivityRemoteService(SynchActivityService synchActivityService) {
		this.synchActivityService = synchActivityService;
	}
	@Autowired@Qualifier("placeService")
	private PlaceService placeService;
	public void setPlaceService(PlaceService placeService) {
		this.placeService = placeService;
	}
	@Autowired@Qualifier("markService")
	private MarkService markService;
	public void setMarkService(MarkService markService) {
		this.markService = markService;
	}
	@Autowired@Qualifier("mailService")
	private MailService mailService;
	
	public void setMailService(MailService mailService) {
		this.mailService = mailService;
	}
	
	@Autowired@Qualifier("goodsService")
	private GoodsService goodsService;
	public void setGoodsService(GoodsService goodsService) {
		this.goodsService = goodsService;
	}
	@Autowired@Qualifier("controllerService")
	private ControllerService controllerService;
	public void setControllerService(ControllerService controllerService) {
		this.controllerService = controllerService;
	}
	@Autowired@Qualifier("videoService")
	private VideoService videoService;
	public void setVideoService(VideoService videoService) {
		this.videoService = videoService;
	}
	@Autowired@Qualifier("operationService")
	private OperationService operationService;
	public void setOperationService(OperationService operationService) {
		this.operationService = operationService;
	}

	@Autowired@Qualifier("spdiscountService")
	private SpdiscountService spdiscountService;
	public void setSpdiscountService(SpdiscountService spdiscountService) {
		this.spdiscountService = spdiscountService;
	}

	@RequestMapping("/cinema/cinemaDetail.xhtml")
	public String newCinemaDetail(@RequestParam("cid") Long cid, HttpServletRequest request, HttpServletResponse response, ModelMap model) {
		Cinema cinema = daoService.getObject(Cinema.class, cid);
		if(cinema == null)return show404(model, "电影院不存在或已经删除！");
		cacheDataService.getAndSetIdsFromCachePool(Cinema.class, cid);
		cacheDataService.getAndSetClazzKeyCount(Cinema.class, cid);
		String citycode = cinema.getCitycode();
		WebUtils.setCitycode(request, citycode, response);
		if (pageCacheService.isUseCache(request)) {// 先使用缓存
			PageParams pageParams = new PageParams();
			pageParams.addLong("cid", cid);
			PageView pageView = pageCacheService.getPageView(request, "cinema/cinemaDetail.xhtml", pageParams, citycode);
			if (pageView != null) {
				model.put("pageView", pageView);
				return "pageView.vm";
			}
		}
		CinemaProfile cinemaProfile = daoService.getObject(CinemaProfile.class, cid);
		model.put("cinemaProfile", cinemaProfile);
		model.put("cinema", cinema);
		List<Date> playdateList = mcpService.getCurCinemaPlayDate(cid);
		Date cur = new Date();
		Date fyrq = null;
		if (DateUtil.getHour(cur) < 21)
			fyrq = DateUtil.getBeginningTimeOfDay(cur);
		else
			fyrq = DateUtil.addDay(DateUtil.getCurDate(), 1);
		if(!playdateList.isEmpty() && !playdateList.contains(fyrq)){
			fyrq = playdateList.get(0);
		}
		model.put("fyrq",DateUtil.formatDate(fyrq));
		model.put("playdateList", playdateList);
		Map<String, String> otherinfoMap = VmUtils.readJsonToMap(cinema.getOtherinfo());
		//推荐放映厅
		String rooms = otherinfoMap.get("roomList");
		if(StringUtils.isNotBlank(rooms)){
			String roomids[] = StringUtils.split(rooms, ",");
			List<CinemaRoom> roomList = new ArrayList<CinemaRoom>();
			for(String roomid : roomids){
				roomList.add(daoService.getObject(CinemaRoom.class, Long.parseLong(roomid)));
			}
			model.put("roomList", roomList);
		}
		//影院图片数量
		int picCount = pictureService.getPictureCountByRelatedid("cinema", cid);
		model.put("picCount", picCount);
		//影院图片
		List<Picture> pictureList = pictureService.getPictureListByRelatedid(TagConstant.TAG_CINEMA, cid, 0, 3);
		model.put("pictureList", pictureList);
		//最近购票用户
		List<Map> payMemberList = untransService.getPayMemberListByTagAndId(TagConstant.TAG_CINEMA, cid, 0, 10);
		if (payMemberList.size() == 10) {
			model.put("payMemberList", payMemberList);
			Map<String, Map> memberMap = new HashMap<String, Map>();
			for (Map order : payMemberList) {
				memberMap.put(order.get("tradeNo") + "", memberService.getCacheMemberInfoMap((Long) order.get("memberid")));
			}
			model.put("memberMap", memberMap);
		}
		//影院套餐
		List<Goods> goodsList = goodsService.getGoodsList(Goods.class, GoodsConstant.GOODS_TAG_BMH, cid, true, true, true, "goodssort", true, false);
		GoodsFilterHelper.goodsFilter(goodsList, PartnerConstant.GEWA_SELF);
		model.put("goodsList", goodsList);
		model.put("buyGoodsCount", goodsService.getBuyGoodsCount(Goods.class, null, DateUtil.addDay(DateUtil.getCurFullTimestamp(), -30), cid, GoodsConstant.GOODS_TAG_BMH));
		//影院热贴
		List<Diary> topTopicList = diaryService.getDiaryList(Diary.class, citycode, null, TagConstant.TAG_CINEMA, cid, 0, 10, "addtime");
		model.put("topTopicList", topTopicList);
		int topicCount = diaryService.getDiaryCountByKey(Diary.class, citycode, null, TagConstant.TAG_CINEMA, cid, null, null, null);
		model.put("topicCount", topicCount);
		RelatedHelper rh = new RelatedHelper();
		// 电影论坛
		List<GewaCommend> gcList = commonService.getGewaCommendList(citycode, SignName.MOVIEINDEX_HOT_DIARY, null, "", true, 0, 5);
		commonService.initGewaCommendList("gcList", rh, gcList);
		model.put("gcList", gcList);
		//影院资讯
		List<News> newsList = newsService.getNewsListByTagAndRelatedId(citycode, TagConstant.TAG_CINEMA, cid, null, new String[] { "1", "2", "3" });
		model.put("newsList", newsList);
		model.put("relatedHelper", rh);
		//相关活动
		getActivity(rh, cid, model, citycode);
		//周边场所
		List<Sport> sportList = placeService.getPlaceListByCountyCode(Sport.class, cinema.getCountycode(), "", false, 0, 4);
		model.put("sportList", sportList);
		//最受关注影片
		List<Movie> futureMovieList = mcpService.getFutureMovieList(0, 3, "xiangqu");
		Map<Long, Integer> videoCountMap = new HashMap<Long, Integer>();
		if (futureMovieList.size() > 0) {
			for (Movie movie : futureMovieList) {
				videoCountMap.put(movie.getId(), videoService.getVideoCountByTag(TagConstant.TAG_MOVIE, movie.getId()));
			}
		}
		model.put("moreLikeMovieList",futureMovieList );
		model.put("opiMovieList", openPlayService.getOpiMovieidList(citycode, null));
		//影院公告
		List<Bulletin> bulletinList = commonService.getBulletinListByTagAndTypeAndRelatedid(null, TagConstant.TAG_CINEMA, Bulletin.BULLETION_COMMON, true, cid);
		model.put("bulletinList", bulletinList);
		model.put("videoCountMap", videoCountMap);
		model.put("commentCountMap", commonService.getCommentCount());
		model.put("markData", markService.getMarkdata(TagConstant.TAG_MOVIE));
		model.put("subwaylineMap", placeService.getSubwaylineMap(citycode));
		List<String> tmpList = mcpService.getCharacteristicCinemaRoomByCinema(cinema.getId());
		List<String> ctypeList = new LinkedList<String>();
		for(String c : CharacteristicType.cTypeList){
			if(tmpList.contains(c)){
				ctypeList.add(c);
			}
		}
		model.put("ctypeList",ctypeList);
		model.put("ctypeNameMap", CharacteristicType.characteristicNameMap);
		getHeadInfo(cid, model, citycode);
		//套头
		model.putAll(getHeadData(cid));
		//影院优惠信息
		spdiscountList(model, cinema);
		if(StringUtils.isNotBlank(cinema.getSubwayTransport())){
			Map<Long,List<Map<String,String>>> subwayTransportMap = JsonUtils.readJsonToObject(new TypeReference<Map<Long,List<Map<String,String>>>>() {},cinema.getSubwayTransport());
			List<Subwaystation> stationList = this.daoService.getObjectList(Subwaystation.class, subwayTransportMap.keySet());
			model.put("subwayTransportMap",subwayTransportMap);
			model.put("stationList", stationList);
		}
		return "movie/wide_cinemaDetail.vm";
	}
	
	private static final String GEWA_CINEMA_OWNER_RECENT_LOOK = "owner_recent_look_cinemaList";
	@RequestMapping("/ajax/cinema/recentCinemaList.xhtml")
	public String recentCinemaList( @CookieValue(required=false,value=GEWA_CINEMA_OWNER_RECENT_LOOK) String cinemaIdArr,Long cinemaId,
			HttpServletRequest request,HttpServletResponse response, ModelMap model) {
		String citycode = WebUtils.getAndSetDefault(request, response);
		if(cinemaId == null){
			return "movie/wide_ajax_recentCinemaList.vm";
		}
		if(StringUtils.isNotBlank(cinemaIdArr)){
			String[] idList = StringUtils.split(cinemaIdArr, ",");
			List<Long> cIdList = new LinkedList<Long>();
			for(String id : idList){
				if(cIdList.size() >= 5){
					break;
				}
				if(ValidateUtil.isNumber(id)){
					Long cId = Long.parseLong(id);
					if(!cId.equals(cinemaId)){
						cIdList.add(cId);
					}
				}
			}
			model.put("cinemaList",this.daoService.getObjectList(Cinema.class, cIdList));
			model.put("opiCinemaList", openPlayService.getOpiCinemaidList(citycode, null));
		}
		return "movie/wide_ajax_recentCinemaList.vm";
	}
	
	// 影院介绍
	@RequestMapping("/cinema/cinemaIntroduce.xhtml")
	public String getCinemaIntroduce(@RequestParam("cid")Long cid, ModelMap model) {
		Cinema cinema = daoService.getObject(Cinema.class, cid);
		if(cinema == null)return show404(model, "电影院不存在或已经删除！");
		return showRedirect("/cinema/" + cid + "?cinemaIntruduceTab=true", model);
	}
	
	//哇啦
	@RequestMapping("/cinema/cinemaCommentList.xhtml")
	public String getCinemaCommentList(@RequestParam("cid")
		Long cid, ModelMap model, HttpServletRequest request, HttpServletResponse response) {
		Cinema cinema = daoService.getObject(Cinema.class, cid);
		if(cinema == null)return show404(model, "电影院不存在或已经删除！");
		String citycode = cinema.getCitycode();
		WebUtils.setCitycode(request, citycode, response);
		model.put("cinema", cinema);
		RelatedHelper rh = new RelatedHelper();
		model.put("relatedHelper", rh);
		model.put("keytag", "cinema");
		//相关活动
		getActivity(rh, cid, model, citycode);
		//影院图片
		getCinemaPic(cid, model);
		//正在热映
		getHotMovie(model, citycode);
		getHeadInfo(cid, model, citycode);
		model.putAll(getHeadData(cid));
		return "cinema/mod_ajax_theatreWala.vm";
	}
	
	//资讯
	@RequestMapping("/cinema/cinemaNewsList.xhtml")
	public String getCinemaNews(@RequestParam("cid")Long cid,ModelMap model) {
		Cinema cinema = daoService.getObject(Cinema.class, cid);
		if(cinema == null)return show404(model, "影院不存在或已经删除！");
		return showRedirect("/cinema/" + cid + "?cinemaIntruduceTab=true", model);
	}
	
	//影院图片列表
	@RequestMapping("/cinema/cinemaPictureList.xhtml")
	public String getCinemaPictureList(@RequestParam("cid")Long cid, ModelMap model) {
		Cinema cinema = daoService.getObject(Cinema.class, cid);
		if(cinema == null)return show404(model, "电影院不存在或已经删除！");
		return showRedirect("/cinema/" + cid + "?cinemaIntruduceTab=true", model);
	}
	
	//影院图片详细
	@RequestMapping("/cinema/cinemaPictureDetail.xhtml")
	public String cinemaPictureDetial(ModelMap model, @RequestParam("cid")Long cid, String pvtype, Long pid) {
		if(StringUtils.isBlank(pvtype))pvtype = "apic";
		List<Map> mapList = new ArrayList<Map>();
		Cinema cinema = null;
		long pictureid = 0;
		if (pvtype.equals("apic")) {// 管理员图片信息
			List<Picture> pictureList = new ArrayList<Picture>();
			if (pid != null) {
				Picture picture = daoService.getObject(Picture.class, pid);
				if (picture == null)
					return show404(model, "图片不存在或已经删除！");
				cinema = daoService.getObject(Cinema.class, picture.getRelatedid());
				if (cinema == null)
					return show404(model, "影院不存在或已经删除！");
				pictureList = pictureService.getPictureListByRelatedid(TagConstant.TAG_CINEMA, cinema.getId(), 0, 100);
				pictureList.remove(picture);
				pictureList.add(picture);
				pictureid = pid;
			} else {
				cinema = daoService.getObject(Cinema.class, cid);
				if (cinema == null)
					return show404(model, "电影不存在或已经删除！");
				pictureList = pictureService.getPictureListByRelatedid(TagConstant.TAG_CINEMA, cinema.getId(), 0, 100);
				if(!pictureList.isEmpty()){
					pictureid = pictureList.get(0).getId();
				}
			}
			mapList = BeanUtil.getBeanMapList(pictureList, "id", "picturename", "posttime", "memberid", "description", "memberType");
			for (Map map : mapList) {
				if(StringUtils.equals((String)map.get("memberType"), GewaraUser.USER_TYPE_MEMBER)){
					Member member = daoService.getObject(Member.class, new Long(map.get("memberid") + ""));
					if(member != null){
						map.put("membername", member.getNickname());
					}
				}
			}
		} else if (pvtype.equals("mpic")) {// 网友图片信息
			List<MemberPicture> pictureList = new ArrayList<MemberPicture>();
			if (pid != null) {
				MemberPicture memberPicture = daoService.getObject(MemberPicture.class, pid);
				if (memberPicture == null)
					return show404(model, "图片不存在或已经删除！");
				cinema = daoService.getObject(Cinema.class, memberPicture.getRelatedid());
				if (cinema == null)
					return show404(model, "影院不存在或已经删除！");
				pictureList = pictureService.getMemberPictureList(cinema.getId(), TagConstant.TAG_CINEMA, null, TagConstant.FLAG_PIC, Status.Y, 0, 100);
				pictureList.remove(memberPicture);
				pictureList.add(memberPicture);
				pictureid = pid;
				cid = cinema.getId();
			} else {
				cinema = daoService.getObject(Cinema.class, cid);
				if (cinema == null)
					return show404(model, "电影不存在或已经删除！");
				pictureList = pictureService.getMemberPictureList(cinema.getId(), TagConstant.TAG_CINEMA, null, TagConstant.FLAG_PIC, Status.Y, 0, 100);
				if(!pictureList.isEmpty()){
					pictureid = pictureList.get(0).getId();
				}
			}
			mapList = BeanUtil.getBeanMapList(pictureList, "id", "picturename", "addtime", "memberid", "description");
			for (Map map : mapList) {
				Member member = daoService.getObject(Member.class, new Long(map.get("memberid") + ""));
				map.put("membername", member.getNickname());
				map.put("posttime", map.get("addtime"));
			}
		}
		if(cinema == null)return show404(model, "该图片不是关联该影院！");
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
		jsonMap.put("cid", cid);
		jsonMap.put("vid", pid);
		jsonMap.put("pvtype", pvtype);
		jsonMap.put("pictureid", pictureid);
		jsonMap.put("pictureList", pics);
		return showJsonSuccess(model, jsonMap);
	}
	
	//头部信息
	private void getHeadInfo(Long cid, ModelMap model, String citycode) {
		//评分统计
		model.putAll(markService.getGradeCount(TagConstant.TAG_CINEMA, cid));
		model.put("goCount", markService.getMarkValueCount(TagConstant.TAG_CINEMA, cid, "generalmark", 5, 10));
		int commnetCount = commentService.getCommentCountByRelatedId(TagConstant.TAG_CINEMA, cid);
		model.put("commnetCount", commnetCount);
		int piccount = pictureService.getPictureCountByRelatedid(TagConstant.TAG_CINEMA, cid);
		model.put("piccount", piccount);
		int newscount = newsService.getNewsCount(citycode, TagConstant.TAG_CINEMA, "", cid, null);
		model.put("newscount", newscount);
		model.put("markHelper", new MarkHelper());
	}
	
	//头部套头
	private Map getHeadData(Long cinemaid) {
		Relationship relationship = commonService.getRelationship(Flag.FLAG_HEAD, TagConstant.TAG_CINEMA, cinemaid, DateUtil.getCurFullTimestamp());
		Map headDataMap = new HashMap();
		HeadInfo headInfo = null;
		if (relationship != null) {
			headInfo = daoService.getObject(HeadInfo.class, relationship.getRelatedid1());
		}
		headDataMap.put("headInfo", headInfo);
		return headDataMap;
	}
	
	//相关活动
	private void getActivity(RelatedHelper rh, Long cid, ModelMap model, String citycode) {
		ErrorCode<List<RemoteActivity>> code = synchActivityService.getActivityListByOrder(citycode, RemoteActivity.ATYPE_GEWA, RemoteActivity.TIME_CURRENT, TagConstant.TAG_CINEMA, cid, null,null,null, 0, 2);
		if(code.isSuccess()){
			List<RemoteActivity> activityList = code.getRetval();
			controllerService.initRelate("activityList", rh, activityList); 
			model.put("activityList", activityList);
		}
	
	}
	
	//影院图片
	private void getCinemaPic(Long cid, ModelMap model) {
		List<Picture> pictureList = pictureService.getPictureListByRelatedid(TagConstant.TAG_CINEMA, cid, 0, 12);
		model.put("pictureList", pictureList);
	}
	
	//正在热映
	private void getHotMovie(ModelMap model, String citycode) {
		List<Long> hotMovieId = getMovieIdList(citycode);
		List<Movie> hotMovieList = daoService.getObjectList(Movie.class, hotMovieId);
		model.put("hotMovieList", hotMovieList);
	}
	
	private List<Long> getMovieIdList(String citycode){
		List<Long> resultList = new ArrayList<Long>();
		DetachedCriteria query = DetachedCriteria.forClass(MoviePlayItem.class, "m");
		query.add(Restrictions.eq("m.citycode", citycode));
		query.add(Restrictions.ge("playdate",DateUtil.getBeginningTimeOfDay(new Date())));
		ProjectionList list = Projections.projectionList()
									.add(Projections.groupProperty("m.movieid"),"movieid")
									.add(Projections.rowCount(),"count");
		
		query.setProjection(list);
		query.addOrder(Order.desc("count"));
		query.setResultTransformer(DetachedCriteria.ALIAS_TO_ENTITY_MAP);
		List<Map> resultMap = hibernateTemplate.findByCriteria(query, 0, 3);
		for(Map map : resultMap){
			resultList.add(new Long(map.get("movieid").toString()));
		}
		return resultList;
	}
	
	@RequestMapping("/cinema/ajax/sendMessage.xhtml")
	public String sendCinemaMessage(@CookieValue(value=LOGIN_COOKIE_NAME, required=false)String sessid,
			HttpServletRequest request, Long relatedid, String mobile, String captchaId, String captcha, ModelMap model){
		boolean isValidCaptcha = controllerService.validateCaptcha(captchaId, captcha, WebUtils.getRemoteIp(request));
		if(!isValidCaptcha) return showJsonError(model, "验证码错误！");
		String ip = WebUtils.getRemoteIp(request);
		Member member = loginService.getLogonMemberBySessid(ip, sessid);
		if(member == null) return showError(model, "请先登录！");
		if(!ValidateUtil.isMobile(mobile)) return showJsonError(model, "手机号码格式不正确！");
		Cinema cinema = daoService.getObject(Cinema.class, relatedid);
		if(cinema == null) return showJsonError(model, "电影院不存在或被删除！");
		String opkey = "cinema_" + member.getId() + "_" + cinema.getId();
		boolean allow = operationService.updateOperation(opkey, 10);
		if(!allow) return showJsonError(model, "你操作过于频繁，请稍后再试！");
		String tradeNo = "cinema_" + cinema.getId();
		Timestamp curtime = DateUtil.getCurFullTimestamp();
		Timestamp endtime = DateUtil.getLastTimeOfDay(curtime);
		String opsmskey = "cinema_" + member.getId() + "_" + cinema.getId()+"_sms"+DateUtil.formatDate(curtime);
		UserOperation op = daoService.getObject(UserOperation.class, opsmskey);
		if(op!=null && op.getOpnum() >= 3) return showJsonError(model, "短信发送次数已超过限制！");
		String cinemaPhone = "";
		if(StringUtils.isNotBlank(cinema.getContactphone())){
			String[] strs = StringUtils.split(cinema.getContactphone(), " ");
			cinemaPhone = strs[0];
		}
		String msgContent = cinema.getRealBriefname()+"  地址:"+cinema.getAddress()+"  电话:"+cinemaPhone;
		if(msgContent.length()>60){
			msgContent = msgContent.substring(0, 60);
			mailService.sendEmail("www.gewara.com", "场馆短信过长", "场馆短信超出60个字符，请及时进行修改！", "sandy.chen@gewara.com");
		}
		SMSRecord sms = new SMSRecord(mobile);
		sms.setTradeNo(tradeNo);
		sms.setContent(msgContent);
		sms.setSendtime(curtime);
		sms.setSmstype(SmsConstant.SMSTYPE_MANUAL);
		sms.setValidtime(endtime);
		sms.setTag(TagConstant.TAG_CINEMA);
		sms.setMemberid(member.getId());
		sms.setRelatedid(cinema.getId());
		sms = untransService.addMessage(sms);
		if(sms != null) untransService.sendMsgAtServer(sms, true);
		operationService.updateOperation(opsmskey, OperationService.ONE_DAY, 3);
		return showJsonSuccess(model);
	}
	
	@RequestMapping("/ajax/getLogonMemberMobile.xhtml")
	public String getMemberMobile(@CookieValue(value=LOGIN_COOKIE_NAME, required=false)String sessid, 
			HttpServletRequest request, ModelMap model){
		String ip = WebUtils.getRemoteIp(request);
		Member member = loginService.getLogonMemberBySessid(ip, sessid);
		Map result = new HashMap();
		result.put("mobile", member.getMobile());
		return showJsonSuccess(model, result);
	}
	
	private void spdiscountList(ModelMap model, Cinema cinema){
		CinemaSpdiscountFilter osf = new CinemaSpdiscountFilter(cinema, DateUtil.getCurFullTimestamp());
		List<SpecialDiscount> spdiscountList  =  spdiscountService.getSpecialDiscountData(osf, SpecialDiscount.OPENTYPE_GEWA, PayConstant.APPLY_TAG_MOVIE);		
		Map<Long, String> spidMap = new HashMap<Long, String>();
		for(SpecialDiscount sd:spdiscountList){
			spidMap.put(sd.getId(), PKCoderUtil.encryptString(""+sd.getId(), SpecialDiscount.ENCODE_KEY));
		}
		model.put("spidMap", spidMap);
		model.put("spdiscountList", spdiscountList);	
	}
}
