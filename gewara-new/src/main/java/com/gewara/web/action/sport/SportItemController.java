package com.gewara.web.action.sport;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

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
import org.springframework.beans.support.PropertyComparator;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.gewara.constant.CookieConstant;
import com.gewara.constant.DiaryConstant;
import com.gewara.constant.OpenTimeTableConstant;
import com.gewara.constant.PayConstant;
import com.gewara.constant.Status;
import com.gewara.constant.TagConstant;
import com.gewara.constant.content.CommonType;
import com.gewara.constant.content.SignName;
import com.gewara.json.PageView;
import com.gewara.model.bbs.Diary;
import com.gewara.model.bbs.commu.Commu;
import com.gewara.model.bbs.qa.GewaQuestion;
import com.gewara.model.content.GewaCommend;
import com.gewara.model.content.News;
import com.gewara.model.content.Video;
import com.gewara.model.pay.SpecialDiscount;
import com.gewara.model.sport.OpenTimeTable;
import com.gewara.model.sport.Sport;
import com.gewara.model.sport.SportItem;
import com.gewara.model.sport.SportPrice;
import com.gewara.model.sport.SportPriceTable;
import com.gewara.model.user.Member;
import com.gewara.model.user.MemberPicture;
import com.gewara.model.user.Treasure;
import com.gewara.service.bbs.BlogService;
import com.gewara.service.bbs.CommuService;
import com.gewara.service.bbs.DiaryService;
import com.gewara.service.bbs.QaService;
import com.gewara.service.content.NewsService;
import com.gewara.service.content.PictureService;
import com.gewara.service.content.VideoService;
import com.gewara.service.member.TreasureService;
import com.gewara.service.sport.MemberCardService;
import com.gewara.service.sport.OpenTimeTableService;
import com.gewara.support.ErrorCode;
import com.gewara.support.ReadOnlyTemplate;
import com.gewara.support.ServiceHelper;
import com.gewara.support.VelocityTemplate;
import com.gewara.untrans.CacheDataService;
import com.gewara.untrans.CommentService;
import com.gewara.untrans.CommonService;
import com.gewara.untrans.PageCacheService;
import com.gewara.untrans.PageParams;
import com.gewara.untrans.ShareService;
import com.gewara.untrans.UntransService;
import com.gewara.untrans.order.impl.SpdiscountService;
import com.gewara.util.BeanUtil;
import com.gewara.util.DateUtil;
import com.gewara.util.WebUtils;
import com.gewara.web.action.partner.OttSpdiscountFilter;
import com.gewara.web.util.PageUtil;
import com.gewara.xmlbind.bbs.Comment;

@Controller
public class SportItemController extends BaseSportController {
	@Autowired@Qualifier("readOnlyTemplate")
	private ReadOnlyTemplate readOnlyTemplate;
	public void setReadOnlyHibernateTemplate(ReadOnlyTemplate readOnlyTemplate) {
		this.readOnlyTemplate = readOnlyTemplate;
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
	@Autowired@Qualifier("blogService")
	private BlogService blogService = null;
	public void setBlogService(BlogService blogService) {
		this.blogService = blogService;
	}
	@Autowired@Qualifier("commuService")
	private CommuService commuService = null;
	public void setCommuService(CommuService commuService) {
		this.commuService = commuService;
	}
	@Autowired@Qualifier("treasureService")
	private TreasureService treasureService;
	@Autowired@Qualifier("qaService")
	private QaService qaService;
	public void setQaService(QaService qaService) {
		this.qaService = qaService;
	}
	@Autowired@Qualifier("shareService")
	private ShareService shareService;
	public void setShareService(ShareService shareService) {
		this.shareService = shareService;
	}
	@Autowired@Qualifier("openTimeTableService")
	private OpenTimeTableService openTimeTableService;
	public void setOpenTimeTableService(OpenTimeTableService openTimeTableService) {
		this.openTimeTableService = openTimeTableService;
	}
	@Autowired@Qualifier("commonService")
	private CommonService commonService;
	public void setCommonService(CommonService commonService) {
		this.commonService = commonService;
	}
	@Autowired@Qualifier("newsService")
	private NewsService newsService;
	public void setNewsService(NewsService newsService) {
		this.newsService = newsService;
	}
	@Autowired@Qualifier("untransService")
	private UntransService untransService;
	public void setUntransService(UntransService untransService) {
		this.untransService = untransService;
	}
	@Autowired@Qualifier("videoService")
	private VideoService videoService;
	public void setVideoService(VideoService videoService) {
		this.videoService = videoService;
	}
	@Autowired@Qualifier("pictureService")
	private PictureService pictureService;
	public void setPictureService(PictureService pictureService) {
		this.pictureService = pictureService;
	}
	@Autowired@Qualifier("cacheDataService")
	private CacheDataService cacheDataService;
	@Autowired@Qualifier("pageCacheService")
	private PageCacheService pageCacheService;
	public void setPageCacheService(PageCacheService pageCacheService) {
		this.pageCacheService = pageCacheService;
	}
	@Autowired@Qualifier("memberCardService")
	private MemberCardService memberCardService;
	@Autowired@Qualifier("spdiscountService")
	protected SpdiscountService spdiscountService;
	
	@Autowired@Qualifier("velocityTemplate")
	private VelocityTemplate velocityTemplate;
	//初手必读
	@RequestMapping("/sport/itemRookieList.xhtml")
	public String sportRookie(ModelMap model, Long itemid, Integer pageNo, HttpServletRequest request, HttpServletResponse response){
		SportItem sportItem = daoService.getObject(SportItem.class, itemid);
		if(sportItem==null) return showError(model, "参数出错！");
		String citycode = WebUtils.getAndSetDefault(request, response);
		if(pageNo==null) pageNo = 0;
		Integer rowsPerPage = 20;
		Integer count = 0;
		Integer firstRows = rowsPerPage*pageNo;
		//新手必读
		count = newsService.getNewsCount(citycode, TagConstant.TAG_SPORTITEM, CommonType.NEWSTYPE_SPORTROOKIE, itemid, "");
		List<News> newsList = newsService.getNewsList(citycode, TagConstant.TAG_SPORTITEM, itemid, CommonType.NEWSTYPE_SPORTROOKIE, firstRows, rowsPerPage);
		model.put("newsList", newsList);
		PageUtil pageUtil=new PageUtil(count, rowsPerPage,pageNo,"sport/itemRookieList.xhtml", true, true);
		Map parmas = new HashMap();
		parmas.put("itemid", itemid);
		pageUtil.initPageInfo(parmas);
		model.putAll(getCommonData(itemid, citycode));
		model.put("pageUtil",pageUtil);
		model.put("sportItem", sportItem);
		return "sport/item/sportJackeroo.vm";
	}
	//高手进阶
	@RequestMapping("/sport/itemMasterList.xhtml")
	public String sportMaster(ModelMap model, Long itemid, Integer pageNo, HttpServletRequest request, HttpServletResponse response){
		SportItem sportItem = daoService.getObject(SportItem.class, itemid);
		if(sportItem==null) return showError(model, "参数出错！");
		String citycode = WebUtils.getAndSetDefault(request, response);
		if(pageNo==null) pageNo = 0;
		Integer rowsPerPage = 20;
		Integer count = 0;
		Integer firstRows = rowsPerPage*pageNo;
		//高手进阶
		count = newsService.getNewsCount(citycode, TagConstant.TAG_SPORTITEM, CommonType.NEWSTYPE_SPORTMASTER, itemid, "");
		List<News> newsList = newsService.getNewsList(citycode, TagConstant.TAG_SPORTITEM, itemid, CommonType.NEWSTYPE_SPORTMASTER, firstRows, rowsPerPage);
		model.put("newsList", newsList);
		PageUtil pageUtil=new PageUtil(count, rowsPerPage, pageNo, "sport/itemMasterList.xhtml", true, true);
		Map parmas =new HashMap();
		parmas.put("itemid", itemid);
		pageUtil.initPageInfo(parmas);
		model.put("pageUtil",pageUtil);
		model.putAll(getCommonData(itemid, citycode));
		model.put("sportItem", sportItem);
		return "sport/item/sportSuperior.vm";
	}
	//资料库
	@RequestMapping("/sport/itemLibraryList.xhtml")
	public String sportLibrary(ModelMap model, Long itemid, Integer pageNo, String orderType, HttpServletRequest request, HttpServletResponse response){
		SportItem sportItem=daoService.getObject(SportItem.class, itemid);
		if(sportItem==null) return showError(model, "参数出错！");
		String citycode = WebUtils.getAndSetDefault(request, response);
		if(pageNo==null) pageNo = 0;
		Integer rowsPerPage = 20;
		Integer count = 0;
		//资料库信息
		List<News> newsList = new ArrayList<News>();
		if(StringUtils.isBlank(orderType)){//资料库包含新手入门、高级进阶、装备、保健、赛事
			count = newsService.getNewsCountByNewstype(citycode, TagConstant.TAG_SPORTITEM, itemid,  
					new String[]{CommonType.NEWSTYPE_SPORT_HEALTH, CommonType.NEWSTYPE_SPORT_MATCH, CommonType.NEWSTYPE_SPORTEQUIPMENT, CommonType.NEWSTYPE_SPORTMASTER, CommonType.NEWSTYPE_SPORTROOKIE});
			newsList = newsService.getNewsListByNewstype(citycode, TagConstant.TAG_SPORTITEM , itemid, 
					new String[]{CommonType.NEWSTYPE_SPORT_HEALTH, CommonType.NEWSTYPE_SPORT_MATCH, CommonType.NEWSTYPE_SPORTEQUIPMENT, CommonType.NEWSTYPE_SPORTMASTER, CommonType.NEWSTYPE_SPORTROOKIE}, pageNo*rowsPerPage, rowsPerPage);
		}else if(StringUtils.isNotBlank(orderType) && !orderType.equals("newsall")){
			count = newsService.getNewsCountByNewstype(citycode, TagConstant.TAG_SPORTITEM, itemid, new String[]{orderType});
			newsList = newsService.getNewsListByNewstype(citycode, TagConstant.TAG_SPORTITEM, itemid, new String[]{orderType}, pageNo*rowsPerPage, rowsPerPage);
		}else if(orderType.equals("newsall")) {//该项的全部新闻
			count = newsService.getNewsCount(citycode, TagConstant.TAG_SPORTITEM, "", itemid, "");
			newsList=newsService.getNewsList(citycode, TagConstant.TAG_SPORTITEM, itemid, "", pageNo*rowsPerPage, rowsPerPage);
		}
		model.put("newsList", newsList);
		PageUtil pageUtil=new PageUtil(count, rowsPerPage, pageNo, "sport/itemLibraryList.xhtml", true, true);
		Map params = new HashMap();
		params.put("itemid", itemid);
		params.put("orderType", orderType);
		pageUtil.initPageInfo(params);
		model.put("pageUtil",pageUtil);
		model.putAll(getCommonData(itemid, citycode));
		model.put("sportItem", sportItem);
		return "sport/item/itemData.vm";
	}
	
	@RequestMapping("/sport/itemDiaryList.xhtml")
	public String newItemDiaryList(@CookieValue(value=LOGIN_COOKIE_NAME, required=false)String sessid,ModelMap model, Long itemid, Integer pageNo, String myOrder
			, HttpServletRequest request, HttpServletResponse response){
		String citycode = WebUtils.getAndSetDefault(request, response);
		if(pageNo==null) pageNo = 0;
		Integer rowsPerPage = 10;
		Integer firstRows = rowsPerPage*pageNo;
		Integer rowsCount = diaryService.getDiaryCount(Diary.class, citycode, DiaryConstant.DIARY_TYPE_COMMENT, TagConstant.TAG_SPORTITEM, itemid);
		PageUtil pageUtil = new PageUtil(rowsCount, rowsPerPage, pageNo, "sport/itemDiaryList.xhtml", true, true);
		model.put("rowsCount", rowsCount);
		Map params = new HashMap();
		params.put("itemid", itemid==null?"":""+itemid);
		params.put("myOrder", myOrder);
		pageUtil.initPageInfo(params);
		if(StringUtils.isBlank(myOrder)) myOrder = "addtime";
		//运动心得
		List<Diary> diaryList = new ArrayList<Diary>();
		Member member = loginService.getLogonMemberBySessid(WebUtils.getRemoteIp(request), sessid);
		if(itemid!=null){
			SportItem sportItem = daoService.getObject(SportItem.class, itemid);
			model.put("sportItem", sportItem);
		}
		//待解决问题
		List<GewaQuestion> qnList = qaService.getQuestionListByQsAndTagAndRelatedid(TagConstant.TAG_SPORT, null, GewaQuestion.QS_STATUS_N, "addtime", 5);
		model.put("qnList", qnList);
		//运动圈子
		List<Commu> communityList = commuService.getCommunityListByTag(TagConstant.TAG_SPORT, "hotvalue", 0, 6);
		if(communityList.size() <6 ){
			communityList.addAll(commuService.getCommuListOrderByProperty(TagConstant.TAG_SPORT,0,6-communityList.size(),"commumembercount"));
		}
		model.put("communityList", communityList);
		//运动哇啦
		List<Comment> microcommentList = getCommentList(TagConstant.TAG_SPORTITEM,  Status.Y , null ,0 ,4);
		model.put("microcommentList", microcommentList);
		addCacheMember(model, ServiceHelper.getMemberIdListFromBeanList(microcommentList));
		//相关新闻
		List<News> newsList = newsService.getNewsList(citycode, TagConstant.TAG_SPORT, null, "", 0, 5);
		model.put("newsList2", newsList);
		diaryList = diaryService.getDiaryList(Diary.class, citycode, DiaryConstant.DIARY_TYPE_COMMENT, TagConstant.TAG_SPORTITEM, itemid, firstRows, rowsPerPage, myOrder);
		
		model.put("diaryList", diaryList);
		addCacheMember(model, ServiceHelper.getMemberIdListFromBeanList(diaryList));
		model.put("pageUtil", pageUtil);
		model.put("myOrder", myOrder);
		model.put("logonMember", member);
		return "sport/item/new_itemDiary.vm";
	}
	//价格表
	@RequestMapping("/sport/priceTableDetail.xhtml")
	public String priceTableDetail(ModelMap model, Long itemid, String countycode, Long sid, Long tid, 
			HttpServletRequest request, HttpServletResponse response){
		SportItem sportItem = daoService.getObject(SportItem.class, itemid);
		String citycode = WebUtils.getAndSetDefault(request, response);
		if(sportItem == null)return show404(model, "该项目不存在!");
		//价格表关联区域
		List<Map> countyList = sportService.getCountyAndSportNum(itemid);
		model.put("countyList", countyList);
		if (StringUtils.isBlank(countycode)) {
			if (!countyList.isEmpty() && countyList.size()>0) {
				countycode = countyList.get(0).get("countycode")+"";
				if(StringUtils.isBlank(countycode) && countyList.size()>1){
					countycode = countyList.get(1).get("countycode")+"";
				}
			}
		}
		List<Map> sportList = sportService.getSportListByCountyCode(itemid, countycode);
		model.put("sportList", sportList);
		model.put("countycode", countycode);
		if(sid==null && sportList.size()>0) {
			sid = (Long) sportList.get(0).get("id");
		}
		Sport sport = daoService.getObject(Sport.class, sid);
		model.put("sport", sport);
		SportPriceTable sportPriceTable = null;
		if(tid != null){
			sportPriceTable = daoService.getObject(SportPriceTable.class, tid);
		}else {
			if(sport != null){
				List<SportPriceTable> priceTableList = sportService.getPriceTableListBySportId(sport.getId());
				for(SportPriceTable priceTable: priceTableList){
					if(StringUtils.equals(priceTable.getItemid()+"", sportItem.getId()+"")){
						sportPriceTable = priceTable;
						break;
					}
				}
			}
		}
		model.put("sportPriceTable", sportPriceTable);
		model.putAll(getCommonData(itemid, citycode));
		model.put("countyMap", placeService.getCountyPairByCityCode(citycode));
		model.put("sportItem", sportItem);
		return "sport/item/itemPrice.vm";
	}
	//发表一句话项目点评
	@RequestMapping("/sport/addItemComment.xhtml")
	public String sendItemComment(@CookieValue(value=LOGIN_COOKIE_NAME, required=false)String sessid,
			@CookieValue(value=CookieConstant.MEMBER_POINT,required=false)String pointxy,
			HttpServletRequest request, Long id, String tag,String commentText, String stype, ModelMap model){
		String ip = WebUtils.getRemoteIp(request);
		Member member = loginService.getLogonMemberBySessid(ip, sessid);
		
		if(member == null){return showJsonError_NOT_LOGIN(model);}
		String moderator = "";
		SportItem sportItem = daoService.getObject(SportItem.class, id);
		moderator = "#"+sportItem.getName()+"#";
		if (StringUtils.isBlank(commentText)) return showJsonError(model, "评论的内容不能为空！");
		if (StringUtils.length(commentText) > 140) return showJsonError(model, "评论的内容不能超过140个字符！");
		String body = moderator+commentText;
		String pointx = null, pointy = null;
		if(StringUtils.isNotBlank(pointxy)){
			List<String> pointList = Arrays.asList(StringUtils.split(pointxy, ":"));
			if(pointList.size() == 2){
				pointx = pointList.get(0);
				pointy = pointList.get(1);
			}
		}
		ErrorCode<Comment> result = commentService.addComment(member, tag, id, body, null, false, pointx, pointy, WebUtils.getIpAndPort(ip, request));
		if(result.isSuccess()) {
			shareService.sendShareInfo("wala",result.getRetval().getId(), result.getRetval().getMemberid(), null);
		}
		Treasure treasure = new Treasure(member.getId(), TagConstant.TAG_SPORTITEM, id, stype);
		if(!treasureService.isExistsTreasure(treasure)) {
			if(stype.equals("played")) {
				sportItem.setPlayed(sportItem.getPlayed()+1);
			}else if(stype.equals("playing")) {
				sportItem.setPlaying(sportItem.getPlaying()+1);
			}
			daoService.saveObject(sportItem);
			daoService.saveObject(treasure);
		}
		return showJsonSuccess(model);
	}
	//项目介绍
	@RequestMapping("/sport/itemIntroduce.xhtml")
	public String itemIntroduce(ModelMap model, @RequestParam("itemid")Long itemid){
		SportItem sportItem = daoService.getObject(SportItem.class, itemid);
		if(sportItem == null) return show404(model, "该项目不存在!");
		//关联圈子
		List<Commu> communityList = commuService.getCommunityListByRelatedId(TagConstant.TAG_SPORTITEM, itemid, 0, 6);
		//关注此项目的用户
		//带解决问题
		List<GewaQuestion> qnList = qaService.getQuestionListByQsAndTagAndRelatedid(TagConstant.TAG_SPORTITEM, itemid, GewaQuestion.QS_STATUS_N, "addtime", 6);
		model.put("qnList", qnList);
		model.put("communityList", communityList);
		model.put("sportItem", sportItem);
		return "sport/item/itemIntroduce.vm";
	}
	//根据countycode取运动场馆
	@RequestMapping("/sport/getSportVenueList.xhtml")
	public String getSportVenueList(ModelMap model, Long itemid, String countycode){
		List<Map> sportList = sportService.getSportListByCountyCode(itemid, countycode);
		model.put("sportList", sportList);
		model.put("countycode", countycode);
		model.put("sportItem", daoService.getObject(SportItem.class, itemid));
		return "sport/item/sportPrice.vm";
	}
	//根据itemid、sid查询数据
	@RequestMapping("/sport/getSportItemPrice.xhtml")
	public String getSportItemPrice(ModelMap model, Long sid, Long itemid){
		SportPriceTable sportPriceTable = null;
		Sport sport = daoService.getObject(Sport.class, sid);
		SportItem sportItem = daoService.getObject(SportItem.class, itemid);
		if(sport != null){
			sportPriceTable = sportService.getSportPriceTable(sid, itemid);
			if(sportPriceTable!=null){
				List<SportPrice> priceList = sportService.getPriceList(sportPriceTable.getId());
				model.put("sportPriceTable", sportPriceTable);
				model.put("priceList", priceList);
			}
		}
		model.put("sport", sport);
		model.put("itemid", sportItem.getId());
		
		model.putAll(getBookingMap(sport, sportItem));
		return "sport/item/sportItemPrice.vm";
	}
	private Map getBookingMap(Sport sport, SportItem item){
		Map model = new HashMap();
		if(!StringUtils.equals(Sport.BOOKING_OPEN, sport.getBooking())) return model;
		Map<Long, Integer> bookingMap = new HashMap<Long, Integer>();
		int count = openTimeTableService.getOpenTimeTableCount(sport.getId(), item.getId(), DateUtil.getBeginningTimeOfDay(new Date()), null, null);
		bookingMap.put(item.getId(), count);
		model.put("bookingMap", bookingMap);
		return model;
	}
	//公共的方法
	private Map getCommonData(Long itemid, String citycode){
		Map model = new HashMap();
		//关联圈子
		Map<Long, Member> commuMemberMap = new HashMap<Long, Member>();
		List<Commu> communityList = commuService.getCommuListByTagAndRelatedid(citycode, TagConstant.TAG_SPORTITEM, itemid, 0, 3);
		model.put("communityList", communityList);
		for(Commu commu:communityList){
			commuMemberMap.put(commu.getId(), daoService.getObject(Member.class, commu.getAdminid()));
		}
		model.put("commuMemberMap", commuMemberMap);
		//运动装备
		List<News> sportEquipmentList = newsService.getNewsList(citycode, TagConstant.TAG_SPORTITEM, itemid, CommonType.NEWSTYPE_SPORTEQUIPMENT, 0, 4);
		model.put("sportEquipmentList", sportEquipmentList);
		//关注用户
		Map<Long, Member> treasureMap = new HashMap<Long, Member>();
		List<Treasure> treasureList = blogService.getTreasureListByMemberId(null, new String[] {TagConstant.TAG_SPORTITEM},null, itemid, 0, 6, Treasure.ACTION_COLLECT);
			for(Treasure treasure: treasureList){
				treasureMap.put(treasure.getId(), daoService.getObject(Member.class, treasure.getMemberid()));
			}
			model.put("treasureList", treasureList);
			model.put("treasureMap", treasureMap);
			//关联哇啦
		List<Comment> microcommentList = commentService.getCommentListByRelatedId(TagConstant.TAG_SPORTITEM,null, itemid ,"flowernum",0 ,4);
		model.put("microcommentList", microcommentList);
		//待解决问题
		List<GewaQuestion> qnList = qaService.getQuestionListByQsAndTagAndRelatedid(TagConstant.TAG_SPORTITEM, itemid, GewaQuestion.QS_STATUS_N, "addtime", 3);
		model.put("qnList", qnList);
		//相关新闻
		List<News> newsList2 = newsService.getNewsList(citycode, TagConstant.TAG_SPORTITEM, itemid, "", 0, 5);
		model.put("newsList2", newsList2);
			return model;
	}
	
	private List<Comment> getCommentList(String tag, String status, Long relatedid, int from, int maxnum){
		DetachedCriteria query = DetachedCriteria.forClass(Comment.class);
		if(StringUtils.isNotBlank(tag)) query.add(Restrictions.eq("tag", tag));
		if(StringUtils.isNotBlank(status)) query.add(Restrictions.like("status",status,MatchMode.START));
		if(null != relatedid) query.add(Restrictions.eq("relatedid", relatedid));
		query.add(Restrictions.ilike("body","#",MatchMode.ANYWHERE));
		query.addOrder(Order.desc("addtime"));
		List<Comment> commentList = readOnlyTemplate.findByCriteria(query, from, maxnum);
		return commentList;
	}
	
	//运动项目列表
	@RequestMapping("/sport/itemList.xhtml")
	public String sportItemList(ModelMap model, String searchItem, String orderby, String type, Integer pageNo,
			HttpServletRequest request, HttpServletResponse response){
		String citycode = WebUtils.getAndSetDefault(request, response);
		String spkey = request.getParameter("spkey");
		model.put("spkey", spkey);
		if (pageCacheService.isUseCache(request)) {// 先使用缓存
			PageParams params = new PageParams();
			params.addSingleString("searchItem", searchItem);
			params.addSingleString("orderby", orderby);
			params.addSingleString("type", type);
			params.addInteger("pageNo", pageNo);
			if (StringUtils.isNotBlank(spkey)) {
				params.addSingleString("spkey", spkey);
			}
			PageView pageView = pageCacheService.getPageView(request, "sport/itemList.xhtml", params, citycode);
			if (pageView != null) {
				model.put("pageView", pageView);
				return "pageView.vm";
			}
		}
		//左侧导航
		this.setheadData(citycode, model);
		if(null == pageNo) pageNo = 0;
		int maxnum = 10;
		int from = maxnum * pageNo;
		int count = sportService.getSportItemCount(searchItem, 0L, type);
		model.put("count", count);
		if(count > 0){
			List<SportItem> sportItemList = sportService.getSportItemList(searchItem, 0L, type, orderby, from, maxnum);
			Map<Long, Integer> itemSportBookingMap = new HashMap<Long, Integer>();
			Map<Long, Integer> itemMemberCardCountMap = new HashMap<Long, Integer>();
			for (SportItem sportItem : sportItemList) {
				List<Long> sportIdList = openTimeTableService.getCurOttSportIdList(sportItem.getId(), citycode);
				Integer bookingCount = 0;
				if(sportIdList != null && !sportIdList.isEmpty()){
					bookingCount = sportIdList.size();
				} 
				itemSportBookingMap.put(sportItem.getId(), bookingCount);
				int mccount = memberCardService.getMemberCardTypeCountBySportItemid(sportItem.getId());
				itemMemberCardCountMap.put(sportItem.getId(), mccount);
			}
			model.put("sportItemList", sportItemList);
			model.put("itemMemberCardCountMap", itemMemberCardCountMap);
			model.put("itemSportBookingMap", itemSportBookingMap);
			List<Long> openItemIdList = getOpenItemIdList();
			model.put("openItemIdList", openItemIdList);
			PageUtil pageUtil = new PageUtil(count, maxnum, pageNo, "sport/itemList.xhtml", true, true);
			Map params = new HashMap();
			params.put("orderby", orderby);
			params.put("type", type);
			params.put("searchItem", searchItem);
			if (StringUtils.isNotBlank(spkey)) {
				params.put("spkey", spkey);
			}
			pageUtil.initPageInfo(params);
			model.put("pageUtil", pageUtil);
		}
		//运动项目搜索推荐
		List<GewaCommend> itemSearchList = commonService.getGewaCommendList(citycode, SignName.SPORTITEM_SEARCH, null, null, true,0, 3);
		model.put("itemSearchList", itemSearchList);
		//右上角推荐
		List<GewaCommend> adList = commonService.getGewaCommendList(citycode, SignName.SPORTLIST_GEWAACTIVITY, null, null, true,0, 6);
		model.put("adList", adList); 
		model.put("searchItem", searchItem);
		model.put("orderby", orderby);
		model.put("type", type);
		//活动
		Map<String, Integer> indexMovieActivityCountMap = commonService.getActivityCount();
		model.put("activityCountMap", indexMovieActivityCountMap);
		//场馆
		Map<String, Integer> sportItemOpenCount = commonService.getSportItemSportCount();
		model.put("sportItemOpenCount", sportItemOpenCount);
		//圈子
		Map<String, Integer> sportCommuCount = commonService.getCommuCount();
		model.put("sportCommuCount", sportCommuCount);
		//资讯
		Map<String, Integer> sportNewsCount = commonService.getNewsCount();
		model.put("sportNewsCount", sportNewsCount);
		return "sport/wide_itemList.vm";
	}
	//查询是否可预订
	private List<Long> getOpenItemIdList(){
		Timestamp cur = new Timestamp(System.currentTimeMillis());
		Date startdate = DateUtil.getBeginningTimeOfDay(cur);
		DetachedCriteria subQuery = DetachedCriteria.forClass(OpenTimeTable.class, "o");
		subQuery.add(Restrictions.eq("o.status", OpenTimeTableConstant.STATUS_BOOK));
		subQuery.add(Restrictions.eq("o.rstatus", "Y"));
		subQuery.add(Restrictions.le("o.opentime", cur));
		subQuery.add(Restrictions.ge("o.closetime", cur));
		subQuery.add(Restrictions.ge("o.playdate", startdate));
		subQuery.setProjection(Projections.property("o.itemid"));
		List<Long> list = readOnlyTemplate.findByCriteria(subQuery);
		return list;
	}
	//新运动项目详细
	@RequestMapping("/sport/itemDetail.xhtml")
	public String newSportItemDetail(ModelMap model, Long itemid, HttpServletRequest request, HttpServletResponse response){
		
		String spkey = request.getParameter("spkey");
		model.put("spkey", spkey);
		
		SportItem sportItem = daoService.getObject(SportItem.class, itemid);
		if(sportItem == null) return show404(model, "参数错误！");
		model.put("sportItem", sportItem);
		cacheDataService.getAndSetIdsFromCachePool(SportItem.class, itemid);
		cacheDataService.getAndSetClazzKeyCount(SportItem.class, itemid);
		String citycode = WebUtils.getAndSetDefault(request, response);
		if(pageCacheService.isUseCache(request)){//先使用缓存
			PageParams params = new PageParams();
			params.addLong("itemid", itemid);
			if (StringUtils.isNotBlank(spkey)) {
				params.addSingleString("spkey", spkey);
			}
			PageView pageView = pageCacheService.getPageView(request, "sport/itemDetail.xhtml", params, citycode);
			if(pageView!=null){
				model.put("pageView", pageView);
				return "pageView.vm";
			}
		}
		//新闻资讯
		List<News> newsList = newsService.getNewsList(citycode, TagConstant.TAG_SPORTITEM, itemid, "", 0, 4);
		model.put("newsList", newsList);
		//参与该运动人数
		int countPayMember = untransService.countPayMemberListByTagAndId(TagConstant.TAG_SPORTITEM, itemid);
		model.put("countPayMember",countPayMember);
		if(countPayMember > 0){
			//参加该运动的人
			List<Map> payMemberList = untransService.getPayMemberListByTagAndId(TagConstant.TAG_SPORTITEM, itemid, 0, 9);
			model.put("payMemberList", payMemberList);
			List<Long> memberidList = BeanUtil.getBeanPropertyList(payMemberList, Long.class, "memberid", true);
			addCacheMember(model, memberidList);
		}
		//右侧教学视频
		List<Video> videoList = videoService.getVideoListByTag(TagConstant.TAG_SPORTITEM, itemid, 0, 1);
		model.put("videoList", videoList);
		int videoListCount = videoService.getVideoCountByTag(TagConstant.TAG_SPORTITEM, itemid);
		model.put("videoListCount", videoListCount);
		//右侧热门圈子
		List<Commu> commuList = commuService.getCommuListByTagAndRelatedid(citycode, TagConstant.TAG_SPORTITEM, itemid, 0, 5);
		model.put("commuList", commuList);
		//知识问答
		List<GewaQuestion> qaList = qaService.getQuestionByCategoryAndCategoryid(citycode, TagConstant.TAG_SPORTITEM, sportItem.getId(), 0, 10);
		Collections.sort(qaList, new PropertyComparator("addtime", false, false));
		model.put("qaList", qaList);
		//区域
		List<Map> countyList = placeService.getPlaceCountyCountMap(Sport.class, citycode);
		model.put("countyList",countyList);
		return "sport/wide_itemDetail.vm";
	}
	@RequestMapping("/sport/ajax/getSportItemOttList.xhtml")
	public String getSportItemOttList(String countycode, Long itemid, HttpServletRequest request, HttpServletResponse response, ModelMap model){
		String spkey = request.getParameter("spkey");
		model.put("spkey", spkey);
		String citycode = WebUtils.getAndSetDefault(request, response);
		if (pageCacheService.isUseCache(request)) {// 先使用缓存
			PageParams params = new PageParams();
			params.addSingleString("countycode", countycode);
			params.addLong("itemid", itemid);
			if (StringUtils.isNotBlank(spkey)) {
				params.addSingleString("spkey", spkey);
			}
			PageView pageView = pageCacheService.getPageView(request, "sport/ajax/getSportItemOttList.xhtml", params, citycode);
			if (pageView != null) {
				model.put("pageView", pageView);
				return "pageView.vm";
			}
		}
		if(itemid != null && StringUtils.isNotBlank(countycode)){
			List<Map> sportList = sportService.getSportListByCountyCode(itemid, countycode);
			List<Long> sportidList = new ArrayList<Long>();
			if(sportList != null && !sportList.isEmpty()){
				if(sportList.size() > 6) sportList = sportList.subList(0, 6);
				Map<Long, OpenTimeTable> ottMap = new HashMap<Long, OpenTimeTable>();
				Map<Long, Map<String, Integer>> priceMap = new HashMap<Long, Map<String,Integer>>();
				for (Map sportMap : sportList) {
					Long sportid = Long.parseLong(sportMap.get("id")+"");
					List<OpenTimeTable> ottList = openTimeTableService.getOpenTimeTableList(sportid, itemid, DateUtil.getCurDate(), null, 0, 1);
					if(!ottList.isEmpty()){
						Map<String, Integer> priceMapList = sportService.getSportPrice(sportid, itemid);
						ottMap.put(sportid, ottList.get(0));
						priceMap.put(sportid, priceMapList);
					}
					sportidList.add(sportid);
				}
				model.put("ottMap", ottMap);
				model.put("priceMap", priceMap);
				model.put("sportList", daoService.getObjectList(Sport.class, sportidList));
			}
		}
		return "sport/wide_itemOttList.vm";
	}
	//异步加载区域
	@RequestMapping("/sport/ajax/areaList.xhtml")
	public String getAreaList(HttpServletRequest request, HttpServletResponse response, ModelMap model){
		String citycode = WebUtils.getAndSetDefault(request, response);
		PageParams params = new PageParams();
		PageView pageView = pageCacheService.getPageView(request, "sport/ajax/areaList.xhtml", params, citycode);
		if(pageView!=null){
			model.put("pageView", pageView);
			return "pageView.vm";
		}
		//区域
		List<Map> countyList = placeService.getPlaceCountyCountMap(Sport.class, citycode);
		model.put("countyList",countyList);
		//商圈
		Map<String, List<Map>> areaGroupMap = new HashMap<String, List<Map>>();
		for(Map map : countyList){
			List<Map> indexareaGroup = placeService.getPlaceIndexareaCountMap(Sport.class, map.get("countycode")+"");
			areaGroupMap.put(map.get("countycode")+"", indexareaGroup);
		}
		model.put("areaGroupMap", areaGroupMap);
		return "sport/module/ajaxAreaList.vm";
	}
	
	//项目视频
	@RequestMapping("/sport/itemVideo.xhtml")
	public String dramaVideo(Long itemid, Long vid, ModelMap model){
		if(itemid == null && vid == null) return show404(model, "参数错误！");
		SportItem sportitem = null;
		Video video = null;
		MemberPicture memberPicture = null;
		if(itemid != null && vid != null){
			sportitem = daoService.getObject(SportItem.class, itemid);
			if(sportitem == null) return show404(model, "运动项目不存在或已经删除！");
			video = daoService.getObject(Video.class, vid);
			if(video == null){
				memberPicture  = daoService.getObject(MemberPicture.class, vid);
				if(memberPicture == null) return show404(model, "该视频不存在或已删除！");
				SportItem curSportItem = daoService.getObject(SportItem.class,memberPicture.getRelatedid());
				if(curSportItem == null) return show404(model, "该视频不是关联该运动项目！");
				if(!sportitem.getId().equals(curSportItem.getId())) return show404(model, "参数错误！");
				model.put("vtag", "member");
			}else{
				SportItem curSportItem = daoService.getObject(SportItem.class, video.getRelatedid());
				if(curSportItem == null) return show404(model, "该视频不是关联该运动项目！");
				if(!sportitem.getId().equals(curSportItem.getId())) return show404(model, "参数错误！");
			}
		}else if(vid != null){
			video = daoService.getObject(Video.class, vid);
			if(video == null){
				memberPicture  = daoService.getObject(MemberPicture.class, vid);
				if(memberPicture == null)	return show404(model, "该视频不存在或已删除！");
				SportItem curSportItem = daoService.getObject(SportItem.class, memberPicture.getRelatedid());
				if(curSportItem == null) return show404(model, "该视频不是关联该运动项目！");
				sportitem = curSportItem;
				model.put("vtag", "member");
			}else {
				SportItem curSportItem = daoService.getObject(SportItem.class, video.getRelatedid());
				if(curSportItem == null) return show404(model, "该视频不是关联该运动项目！");
				sportitem = curSportItem;
				model.put("vtag", "user");
			}
		}else{
			sportitem = daoService.getObject(SportItem.class, itemid);
			if (sportitem == null) return show404(model, "运动项目不存在或已经删除！");
		}
		if(itemid == null) itemid = sportitem.getId();
		List<Video> videoList = videoService.getVideoListByTag(TagConstant.TAG_SPORTITEM, itemid, 0, 1000);
		List<Map<String,String>> videos = new LinkedList<Map<String,String>>();
		if(video != null){
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
		List<MemberPicture> memberVideoList = pictureService.getMemberPictureList(itemid, TagConstant.TAG_SPORTITEM, null, TagConstant.FLAG_VIDEO,
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
		jsonMap.put("itemid", itemid);
		jsonMap.put("vid", vid);
		jsonMap.put("videoList", videos);
		return showJsonSuccess(model, jsonMap);
	}
	@RequestMapping("/sport/ajax/getSpdiscountList.xhtml")
	public String spdiscountList(ModelMap model,Long ottid){
		OpenTimeTable ott = daoService.getObject(OpenTimeTable.class, ottid);
		if (ott == null) return showJsonError(model, "优惠方式不存在！");
		OttSpdiscountFilter ottSpdiscountFilter = new OttSpdiscountFilter(ott, DateUtil.getCurFullTimestamp());
		List<SpecialDiscount> sdList= spdiscountService.getSpecialDiscountData(ottSpdiscountFilter, SpecialDiscount.OPENTYPE_GEWA, PayConstant.APPLY_TAG_SPORT);
		Map dataMap = new HashMap();
		dataMap.put("sdList", sdList);
		String result = velocityTemplate.parseTemplate("movie/ajaxMovieDiscount.vm", dataMap);
		return showJsonSuccess(model, result);
	}
}