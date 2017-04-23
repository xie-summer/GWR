package com.gewara.web.action.ajax;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
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
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;

import com.gewara.Config;
import com.gewara.constant.AdminCityContant;
import com.gewara.constant.CookieConstant;
import com.gewara.constant.MemberConstant;
import com.gewara.constant.TagConstant;
import com.gewara.model.bbs.Bkmember;
import com.gewara.model.bbs.Correction;
import com.gewara.model.bbs.MemberMark;
import com.gewara.model.common.BaseEntity;
import com.gewara.model.common.BaseInfo;
import com.gewara.model.common.City;
import com.gewara.model.common.County;
import com.gewara.model.common.GewaCity;
import com.gewara.model.common.Indexarea;
import com.gewara.model.common.Place;
import com.gewara.model.common.Province;
import com.gewara.model.common.Subwaystation;
import com.gewara.model.common.UserOperation;
import com.gewara.model.content.Advertising;
import com.gewara.model.content.Bulletin;
import com.gewara.model.content.Picture;
import com.gewara.model.drama.Drama;
import com.gewara.model.drama.Theatre;
import com.gewara.model.movie.Cinema;
import com.gewara.model.movie.Movie;
import com.gewara.model.sport.Sport;
import com.gewara.model.sport.SportItem;
import com.gewara.model.user.Member;
import com.gewara.model.user.MemberInfo;
import com.gewara.model.user.MemberPicture;
import com.gewara.model.user.Treasure;
import com.gewara.service.GewaCityService;
import com.gewara.service.OperationService;
import com.gewara.service.PlaceService;
import com.gewara.service.bbs.BlogService;
import com.gewara.service.bbs.MarkService;
import com.gewara.service.content.AdService;
import com.gewara.service.content.PictureService;
import com.gewara.service.member.TreasureService;
import com.gewara.service.movie.MCPService;
import com.gewara.service.order.OrderQueryService;
import com.gewara.support.ErrorCode;
import com.gewara.untrans.CommentService;
import com.gewara.untrans.WalaApiService;
import com.gewara.untrans.activity.SynchActivityService;
import com.gewara.untrans.gym.SynchGymService;
import com.gewara.untrans.impl.ControllerService;
import com.gewara.util.BeanUtil;
import com.gewara.util.BindUtils;
import com.gewara.util.DateUtil;
import com.gewara.util.JsonUtils;
import com.gewara.util.MarkHelper;
import com.gewara.util.StringUtil;
import com.gewara.util.WebUtils;
import com.gewara.web.action.AnnotationController;
import com.gewara.xmlbind.activity.RemoteActivity;
import com.gewara.xmlbind.bbs.Comment;
import com.gewara.xmlbind.gym.RemoteCoach;
import com.gewara.xmlbind.gym.RemoteCourse;

/**
 * @author <a href="mailto:acerge@163.com">gebiao(acerge)</a>
 * @since Apr 14, 2008 AT 6:32:00 PM
 */
@Controller
public class CommonAjaxController extends AnnotationController {
	@Autowired@Qualifier("synchActivityService")
	private SynchActivityService synchActivityService;
	public void setActivityRemoteService(SynchActivityService synchActivityService) {
		this.synchActivityService = synchActivityService;
	}
	@Autowired@Qualifier("mcpService")
	private MCPService mcpService;
	@Autowired@Qualifier("synchGymService")
	private SynchGymService synchGymService;
	@Autowired@Qualifier("gewaCityService")
	private GewaCityService gewaCityService;
	
	private static final Map<String, String> headURLMap = new HashMap<String, String>();
	private static final Map<String, String> ADVERTISINGMAP = new HashMap<String, String>();
	static {
		headURLMap.put("header", "include/headright.vm"); // navigation.vm
		headURLMap.put("ticketHeader", "include/ticketHeadright.vm");
		headURLMap.put("walaheader", "wala/walaHeadright.vm"); // header.vm
		headURLMap.put("newUserHeader", "include/home/newUserHeadright.vm");// newUserHeader.vm
		headURLMap.put("subject", "include/subject/subjectHeaderRight.vm"); // newUserHeader.vm
		ADVERTISINGMAP.put("index", "common/new_adloadFlash.vm");
		ADVERTISINGMAP.put("cityIndex", "common/adloadCityIndex.vm");
		ADVERTISINGMAP.put("sportOrder", "common/adLoadRight.vm");
	}
	@Autowired@Qualifier("markService")
	private MarkService markService;
	public void setMarkService(MarkService markService) {
		this.markService = markService;
	}
	@Autowired@Qualifier("controllerService")
	private ControllerService controllerService;
	public void setControllerService(ControllerService controllerService) {
		this.controllerService = controllerService;
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
	@Autowired@Qualifier("blogService")
	private BlogService blogService = null;
	public void setBlogService(BlogService blogService) {
		this.blogService = blogService;
	}
	@Autowired@Qualifier("config")
	private Config config;
	public void setConfig(Config config) {
		this.config = config;
	}
	@Autowired@Qualifier("adService")
	private AdService adService;

	public void setAdService(AdService adService) {
		this.adService = adService;
	}
	
	@Autowired@Qualifier("operationService")
	private OperationService operationService;
	public void setOperationService(OperationService operationService) {
		this.operationService = operationService;
	}
	@Autowired@Qualifier("orderQueryService")
	private OrderQueryService orderQueryService;
	public void setOrderQueryService(OrderQueryService orderQueryService) {
		this.orderQueryService = orderQueryService;
	}
	@Autowired@Qualifier("placeService")
	private PlaceService placeService;
	public void setPlaceService(PlaceService placeService) {
		this.placeService = placeService;
	}
	@Autowired@Qualifier("treasureService")
	private TreasureService treasureService;
	@RequestMapping("/ajax/common/getRandomAd.xhtml")
	public String getRandomAd(String pid, ModelMap model, HttpServletRequest request, HttpServletResponse response) {
		String citycode = WebUtils.getAndSetDefault(request, response);
		Advertising ad = adService.getRandomAd(citycode, pid);
		Map jsonMap = new HashMap();
		if (ad != null) {
			jsonMap.putAll(BeanUtil.getBeanMapWithKey(ad, "title", "link", "logicaldir"));
			jsonMap.put("adpath", config.getBasePath() + ad.getAd());
			jsonMap.put("pid", pid);
		}
		return showJsonSuccess(model, jsonMap);
	}

	@RequestMapping("/ajax/common/getMovieAd.xhtml")
	public String getMovieAd(String tag, Long relatedid,String ptag, ModelMap model) {
		if(StringUtils.isBlank(ptag)){
			ptag = "ticketsuccess";
		}
		Advertising ad = adService.getAdvertising(tag, relatedid,ptag);
		Map jsonMap = new HashMap();
		if (ad != null) {
			jsonMap.putAll(BeanUtil.getBeanMapWithKey(ad, "title", "link", "logicaldir"));
			jsonMap.put("adpath", config.getBasePath() + ad.getAd());
			jsonMap.put("pid", "ticketsuccess");
		}
		return showJsonSuccess(model, jsonMap);
	}

	// 首页其他广告位
	@RequestMapping("/ajax/common/getIndexOtherRandomAd.xhtml")
	public String IndexOtherRandomAd(String pid, String tag, Long relatedid, String isIndex, ModelMap model, HttpServletRequest request, HttpServletResponse response) {
		String citycode = WebUtils.getAndSetDefault(request, response);
		GewaCity gewaCity = daoService.getObject(GewaCity.class, citycode);
		if(gewaCity.hasAuto()){//无人管理的分站首页广告调用上海的
			citycode = AdminCityContant.CITYCODE_SH;
		}
		List<Advertising> ads = new ArrayList<Advertising>();
		if (StringUtils.isNotBlank(tag) && relatedid != null) {
			ads = adService.getAdListByPid(citycode, pid, tag, relatedid);
		} else
			ads = adService.getAdListByPid(citycode, pid);
		if(gewaCity.hasAuto()){
			ads = BeanUtil.getSubList(ads, 0, 3);
		}
		if (ads != null && ads.size() > 0) {
			List<Map> jsonMapList = new ArrayList<Map>();
			for (Advertising advertising : ads) {
				Map jsonMap = new HashMap();
				jsonMap.putAll(BeanUtil.getBeanMapWithKey(advertising, "title", "link", "logicaldir", "adtype", "description", "track"));
				jsonMap.put("adpath", config.getBasePath() + advertising.getAd());
				jsonMap.put("pid", pid);
				jsonMapList.add(jsonMap);
			}
			model.put("jsonMapList", jsonMapList);
		}
		String viewPage = ADVERTISINGMAP.get(isIndex);
		if(StringUtils.isNotBlank(viewPage)) return viewPage;
		return "common/adLoadFlash.vm";
	}

	// 首页的右下角的广告
	@RequestMapping("/ajax/common/getIndexRandomAd.xhtml")
	public String getIndexRandomAd(String pid, ModelMap model, HttpServletRequest request, HttpServletResponse response) {
		String citycode = WebUtils.getAndSetDefault(request, response);
		Advertising ad = adService.getRandomAd(citycode, pid);
		Map jsonMap = new HashMap();
		if (ad != null) {
			jsonMap.putAll(BeanUtil.getBeanMapWithKey(ad, "title", "link", "logicaldir"));
			jsonMap.put("adpath", config.getBasePath() + ad.getAd());
			jsonMap.put("pid", pid);
		}
		return showJsonSuccess(model, jsonMap);
	}

	@RequestMapping("/ajax/common/addQuguo.xhtml")
	public String addQuguo(@CookieValue(value = LOGIN_COOKIE_NAME, required = false)
	String sessid, HttpServletRequest request, Long relatedid, String tag, ModelMap model) {
		String ip = WebUtils.getRemoteIp(request);
		Member member = loginService.getLogonMemberBySessid(ip, sessid);
		if (member == null)
			return showJsonError(model, "请先登录！");
		BaseEntity relate = (BaseEntity)relateService.getRelatedObject(tag, relatedid);
		Treasure treasure = new Treasure(member.getId(), tag, relatedid, Treasure.ACTION_QUGUO);
		if (treasureService.isExistsTreasure(treasure))
			return showJsonError(model, "你已经去过，请不要重复操作！");
		// gym, cinema, movie, ktv, bar, sport
		if (relate != null) {
			relate.addQuguo();
			daoService.saveObject(relate);
			daoService.saveObject(treasure);
			return showJsonSuccess(model, "" + relate.getQuguo());
		} else {
			return showJsonError(model, "场所不存在");
		}
	}

	@RequestMapping("/ajax/common/addXiangqu.xhtml")
	public String addCMXiangqu(@CookieValue(value = LOGIN_COOKIE_NAME, required = false)
	String sessid, HttpServletRequest request, Long treasureid, Long relatedid, String tag, String actionlabel, ModelMap model) {
		String ip = WebUtils.getRemoteIp(request);
		Member member = loginService.getLogonMemberBySessid(ip, sessid);
		if (member == null)
			return showJsonError(model, "请先登录！");
		actionlabel = StringUtils.trim(actionlabel);
		if (StringUtil.getByteLength(actionlabel) > 30)
			return showJsonError(model, "标签的长度超过15位！ ");
		Treasure treasure = daoService.getObject(Treasure.class, treasureid);
		if (treasure == null)
			treasure = new Treasure(member.getId(), tag, relatedid, Treasure.ACTION_XIANGQU);
		treasure.setActionlabel(actionlabel);
		// gym, cinema, movie, ktv, bar, sport
		BaseEntity relate = (BaseEntity)relateService.getRelatedObject(tag, relatedid);
		Treasure mytreasure = treasureService.getTreasureByTagMemberidRelatedid(tag, member.getId(), relatedid, Treasure.ACTION_XIANGQU);
		if (relate != null && mytreasure == null) {
			relate.addXiangqu();
			daoService.saveObjectList(relate, treasure);
			return showJsonSuccess(model);
		} else {
			daoService.saveObject(treasure);
			return showJsonSuccess(model);
		}
	}

	/**
	 * 话剧明星版块 :成为Fans
	 */
	@RequestMapping("/ajax/common/tobeFans.xhtml")
	public String tobeFans(@CookieValue(value = LOGIN_COOKIE_NAME, required = false)
	String sessid, HttpServletRequest request, Long relatedid, String tag, ModelMap model) {
		String ip = WebUtils.getRemoteIp(request);
		Member member = loginService.getLogonMemberBySessid(ip, sessid);
		if (member == null)
			return showJsonError(model, "请先登录！");
		Treasure treasure = new Treasure(member.getId(), tag, relatedid, Treasure.ACTION_FANS);
		if (treasureService.isExistsTreasure(treasure))
			return showJsonError(model, "已经是粉丝了!");
		BaseEntity relate = (BaseEntity) relateService.getRelatedObject(tag, relatedid);
		if (relate != null) {
			relate.addXiangqu();
			daoService.saveObjectList(relate, treasure);
			return showJsonSuccess(model, "" + relate.getXiangqu());
		} else {
			return showJsonError(model, "该明星不存在!");
		}
	}

	@RequestMapping("/ajax/common/asynchLogin.dhtml")
	public String asynchLogin(HttpServletRequest request, HttpServletResponse response, String username, String password, String captchaId, String captcha, ModelMap model) {
		boolean isValid = controllerService.validateCaptcha(captchaId, captcha, WebUtils.getRemoteIp(request));
		if(!isValid){
			Map jsonMap = new HashMap();
			Map errorMap = new HashMap();
			jsonMap.put("errorMap", errorMap);
			errorMap.put("captcha", "验证码错误！");
			return showJsonError(model, jsonMap);
		}
		ErrorCode<Map> code = loginService.autoLogin(request, response, username, password);
		if (code.isSuccess()) {
			return showJsonSuccess(model, code.getRetval());
		} else {
			Map jsonMap = new HashMap();
			jsonMap.put("errorMap", code.getRetval());
			return showJsonError(model, jsonMap);
		}
	}

	// 是否以前台身份登录
	@RequestMapping("/ajax/common/checkLogon.xhtml")
	public String checkLogon(@CookieValue(value = LOGIN_COOKIE_NAME, required = false)
	String sessid, HttpServletRequest request, ModelMap model) {
		String ip = WebUtils.getRemoteIp(request);
		Member member = loginService.getLogonMemberBySessid(ip, sessid);
		if (member != null) {
			Map result = new HashMap();
			Map jsonMap = BeanUtil.getBeanMapWithKey(member, "id", "nickname");
			if(member.isBindMobile()){
				jsonMap.put("isMobile", true);
			}else jsonMap.put("isMobile", false);
			Map memberHeadPicMap = daoService.getObjectPropertyMap(MemberInfo.class, "id", "headpicUrl", Arrays.asList(member.getId()));
			jsonMap.put("headUrl", memberHeadPicMap.get(member.getId()));
			result.putAll(jsonMap);
			return showJsonSuccess(model, result);
		}
		return showJsonError(model, "未登录");
	}

	/**
	 * 申请当版主
	 * 
	 * @param tag
	 * @param relatedid
	 * @return
	 */
	@RequestMapping("/ajax/common/applyBanzhu.xhtml")
	public String applyBanzhu(@CookieValue(value = LOGIN_COOKIE_NAME, required = false)
	String sessid, HttpServletRequest request, String tag, Long relatedid, ModelMap model) {
		String ip = WebUtils.getRemoteIp(request);
		Member member = loginService.getLogonMemberBySessid(ip, sessid);
		if (member == null)
			return showJsonError(model, "请先登录！");
		Integer rights = blogService.getMaxRights(tag, relatedid, null, member);

		if (rights >= Bkmember.ROLE_BANZHU)
			return showJsonError(model, "您已申请过");
		// return "你已经有版主的权利或已申请！";
		Bkmember bkmember = blogService.getBkmember(member, tag, relatedid);
		if (bkmember == null) {
			bkmember = new Bkmember(member.getId(), tag, relatedid);
		}
		bkmember.setApplyrole(Bkmember.ROLE_BANZHU);
		daoService.saveObject(bkmember);
		return showJsonSuccess(model);
	}

	/**
	 * @param relatedid
	 * @param tag
	 * @return 增加收藏
	 */
	@RequestMapping("/ajax/common/addCollection.xhtml")
	public String addCollection(@CookieValue(value = LOGIN_COOKIE_NAME, required = false)
			String sessid, HttpServletRequest request, Long relatedid, String tag, ModelMap model) {
		String ip = WebUtils.getRemoteIp(request);
		Member member = loginService.getLogonMemberBySessid(ip, sessid);
		if (member == null){
			return showJsonError(model, "请先登录！");
		}
		String opkey = OperationService.TAG_TREASURE_ADD + member.getId();
		if (!operationService.isAllowOperation(opkey, 5))
			return showJsonError(model, "操作不能太频繁，请稍后再试！");
		String collectionCount = "";
		Treasure treasure = new Treasure(member.getId(), tag, relatedid, "collect");
		if (treasureService.isExistsTreasure(treasure)) {
			List tagList = Arrays.asList("movie", "theatre", "sport", "cinema", "sportservice", "picture");
			if (tagList.contains(tag))
				return showJsonError(model, "您已关注过");
			else
				return showJsonError(model, "您已收藏过");
		}
		if ("sportservice".equals(tag)) {// tag=sportservice
			SportItem sportItem = daoService.getObject(SportItem.class, relatedid);
			if (sportItem == null)
				return showJsonError(model, "收藏的项目不存在");
			sportItem.addCollectedtimes();
			daoService.saveObjectList(sportItem, treasure);
			collectionCount = sportItem.getCollectedtimes() + "";
		}else if("activity".equals(tag)){//收藏活动
			ErrorCode<RemoteActivity> code = synchActivityService.getRemoteActivity(relatedid);
			if(!code.isSuccess()) return showJsonError(model, "收藏的活动不存在");
			synchActivityService.collectActivity(member.getId(), relatedid);
			daoService.saveObject(treasure);
		}else if("picture".equals(tag)){
			Picture picture = daoService.getObject(Picture.class, relatedid);
			if(picture == null) return showJsonError(model, "你喜欢的图片不存在");
			daoService.saveObject(treasure);
		}else{// gym, cinema, movie, ktv, bar, sport
			Object obj = relateService.getRelatedObject(tag, relatedid);
			if (obj == null) return showJsonError(model, "收藏的项目不存在");
			if(obj instanceof BaseEntity){
				BaseEntity relate = (BaseEntity) obj;
				relate.addCollection();
				daoService.saveObjectList(relate, treasure);
				collectionCount = relate.getCollectedtimes() + "";
			}
		}
		walaApiService.addTreasure(treasure);
		return showJsonSuccess(model, collectionCount);
	}
	/**
	 * 赞同哇啦，
	 * 1.每个用户ID每天最多只能赞同5条哇啦 
	 * 2.点击赞同图标+1再次点击赞同是减1，不是提示重复操作，只有+1和减1的循环
	 * @param sessid
	 * @param request
	 * @param commentId
	 * @param model
	 * @return
	 */
	@RequestMapping("/ajax/common/addCommentFlower.xhtml")
	public String addCommentFlower(@CookieValue(value = LOGIN_COOKIE_NAME, required = false)
	String sessid, HttpServletRequest request, Long commentId, ModelMap model) {
		String ip = WebUtils.getRemoteIp(request);
		Member member = loginService.getLogonMemberBySessid(ip, sessid);
		if (member == null)
			return showJsonError(model, "请先登录！");
		if (!operationService.updateOperation("commentFlower_add" + member.getId(), 5)){
			return showJsonError(model, "操作不能太频繁，请稍后再试！");
		}
		String opkey = "acf" + commentId + member.getId();
		String memberOpkey = "oneDayWalaOp" + member.getId();
		if(operationService.isAllowOperation(opkey, OperationService.ONE_DAY)){
			boolean allow = operationService.updateOperation(memberOpkey, OperationService.ONE_DAY,5);
			if(!allow){
				return showJsonError(model, "每个用户每天最多只能赞同5条哇啦！");
			}
		}
		Comment comment = commentService.getCommentById(commentId);
		if(comment == null){
			return showJsonError(model, "此条哇啦已删除！");
		}
		UserOperation op = daoService.getObject(UserOperation.class, opkey);
		if(op == null || op.getOpnum() == null || op.getOpnum()%2 == 0){
			comment.addFlowernum();
			comment.setOrderTime(DateUtil.addMinute(comment.getOrderTime(), 144));
		}else{
			comment.setFlowernum(comment.getFlowernum() == null || comment.getFlowernum() <= 0 ? 0 : comment.getFlowernum() - 1);
			comment.setOrderTime(DateUtil.addMinute(comment.getOrderTime(), -144));
		}
		operationService.updateOperation(opkey, OperationService.ONE_DAY,op == null || op.getOpnum() == null ? 1 : op.getOpnum() + 1);
		commentService.updateComment(comment);
		return showJsonSuccess(model, "" + comment.getFlowernum());
	}

	/**
	 * 获取某场所的地址
	 * 
	 * @param tag
	 * @param relatedid
	 * @return
	 */
	@RequestMapping("/ajax/common/getAddress.xhtml")
	public String getAddress(String tag, Long relatedid, ModelMap model) {
		Object relate = relateService.getRelatedObject(tag, relatedid);
		if (relate instanceof BaseInfo)
			return showJsonSuccess(model, ((BaseInfo) relate).getAddress());
		return showJsonError(model, "场所不存在");
	}

	@RequestMapping("/ajax/common/addPrintnum.xhtml")
	public String addPrintnum(Long bid, ModelMap model) {
		Bulletin b = daoService.getObject(Bulletin.class, bid);
		if (b == null)
			return showJsonError(model, "没有该项目");
		b.addPrintnum();
		daoService.saveObject(b);
		return showJsonSuccess(model, "" + b.getPrintnum());
	}

	@RequestMapping("/ajax/common/addDownnum.xhtml")
	public String addDownnum(Long bid, ModelMap model) {
		Bulletin b = daoService.getObject(Bulletin.class, bid);
		if (b == null)
			return showJsonError(model, "没有该项目");
		b.addDownnum();
		daoService.saveObject(b);
		return showJsonSuccess(model, "" + b.getDownnum());
	}

	@RequestMapping("/ajax/common/addCorrection.xhtml")
	public String addCorrection(@CookieValue(value = LOGIN_COOKIE_NAME, required = false)
	String sessid, HttpServletRequest request, @RequestHeader(value = "Referer", required = false)
	String referer, String corrcontent, ModelMap model) {
		String ip = WebUtils.getRemoteIp(request);
		Member member = loginService.getLogonMemberBySessid(ip, sessid);
		Correction corre = new Correction(corrcontent);
		if (member != null)
			corre.setMemberid(member.getId());
		else
			corre.setMemberid(0L);
		corre.setReferer(referer);
		BindUtils.bindData(corre, request.getParameterMap());
		daoService.saveObject(corre);
		return showJsonSuccess(model);
	}

	// 保存场馆
	@RequestMapping("/ajax/common/savePlace.xhtml")
	public String savePlace(@CookieValue(value = LOGIN_COOKIE_NAME, required = false)
	String sessid, HttpServletRequest request, String countyCode, String id, Long stationid, ModelMap model, HttpServletResponse response) {
		String ip = WebUtils.getRemoteIp(request);
		Member member = loginService.getLogonMemberBySessid(ip, sessid);
		if (member == null)
			return showJsonError(model, "请先登录！");
		Place place = new Place("");
		String citycode = WebUtils.getAndSetDefault(request, response);
		City city = daoService.getObject(City.class, citycode);
		place.setCitycode(city.getCitycode());
		if (StringUtils.isNotBlank(countyCode)) {
			County county = daoService.getObject(County.class, countyCode);
			place.setCountycode(county.getCountycode());
		}
		if (stationid != null) {
			Subwaystation subwaystation = daoService.getObject(Subwaystation.class, stationid);
			place.setStationname(subwaystation.getStationname());
		} else {
			place.setStationname(null);
		}
		if (StringUtils.isNotBlank(id))
			place = daoService.getObject(Place.class, new Long(id));
		place.setUserid(member.getId());
		BindUtils.bindData(place, request.getParameterMap());
		if (WebUtils.checkPropertyAll(place))
			return showJsonError(model, "含有非法字符！");
		daoService.saveObject(place);
		return showJsonSuccess(model, "" + place.getId());
	}


	@RequestMapping("/ajax/common/getDetail.xhtml")
	public String getDetail(@CookieValue(value = LOGIN_COOKIE_NAME, required = false)
	String sessid, HttpServletRequest request, ModelMap model, Long relatedid, String tag, HttpServletResponse response) {
		String ip = WebUtils.getRemoteIp(request);
		Member member = loginService.getLogonMemberBySessid(ip, sessid);
		if (member == null) {
			return showJsonError_NOT_LOGIN(model);
		}
		String citycode = WebUtils.getAndSetDefault(request, response);
		if (relatedid == null)
			return showJsonError(model, "参数出错！");
		String msg = "", label = "";
		Long treasureid = 0L;
		Map jsonMap = new HashMap();
		if (tag.equals("movie")) {
			List<Cinema> cinemaList = orderQueryService.getMemberOrderCinemaList(member.getId(), 1);
			if (!cinemaList.isEmpty()) {
				Movie movie = daoService.getObject(Movie.class, relatedid);
				boolean isHotMovie = this.isHotMovieByMovieid(relatedid, citycode);
				if (movie.getReleasedate() != null) {
					if (movie.getReleasedate().after(new Date())) {
						msg = "此影片即将上映，开始售票后，我们将发送站内信通知你观影。";
					}
					if (isHotMovie) {
						msg = "此影片正在热映，";
						msg += "格瓦拉推荐你到" + cinemaList.get(0).getCname() + "观影";
						if (movie.getAvgprice() > 0)
							msg += "，均价 " + movie.getAvgprice() + " 元";
					}
				}
			}
		}
		Treasure treasure = null;
		if (tag.equals("gymcourse") || tag.equals("sportservice")) {
			treasure = blogService.getTreasure(member.getId(), relatedid, tag, Treasure.ACTION_TOGETHER);
		} else if (tag.equals("gymcoach")) {
			treasure = blogService.getTreasure(member.getId(), relatedid, tag, Treasure.ACTION_XIANGXUE);
		} else {
			treasure = blogService.getTreasure(member.getId(), relatedid, tag, Treasure.ACTION_XIANGQU);
		}
		if (treasure != null) {
			if (StringUtils.isNotBlank(treasure.getActionlabel()))
				label = treasure.getActionlabel();
			treasureid = treasure.getId();
		}
		List<String> aList = new ArrayList<String>();
		// 我的标签
		List<Treasure> myTreasureList = blogService.getTreasureListByMemberId(member.getId(), 0, 5);
		for (Treasure treasure2 : myTreasureList) {
			if (StringUtils.isNotBlank(treasure2.getActionlabel()))
				aList.add(treasure2.getActionlabel() + " ");
		}
		jsonMap.put("aList", aList);
		jsonMap.put("treasureid", treasureid);
		jsonMap.put("alabel", label);
		jsonMap.put("showMsg", msg);
		return showJsonSuccess(model, jsonMap);
	}

	@RequestMapping("/ajax/common/cancelTreasure.xhtml")
	public String cancelTreasure(@CookieValue(value = LOGIN_COOKIE_NAME, required = false)
	String sessid, HttpServletRequest request, ModelMap model, String tag, Long relatedid) {
		String ip = WebUtils.getRemoteIp(request);
		Member member = loginService.getLogonMemberBySessid(ip, sessid);
		if (member == null)
			return showJsonError(model, "请先登录！");
		String opkey = OperationService.TAG_TREASURE_CANCEL + member.getId();
		if (!operationService.updateOperation(opkey, 5))
			return showJsonError(model, "操作不能太频繁，请稍后再试！");
		Treasure treasure = blogService.getTreasure(member.getId(), relatedid, tag, Treasure.ACTION_COLLECT);
		if (treasure == null)
			return showJsonError(model, "已取消关注！");
		walaApiService.delTreasure(treasure.getMemberid(), treasure.getRelatedid(), treasure.getTag(), treasure.getAction());
		daoService.removeObject(treasure);
		Object obj = relateService.getRelatedObject(tag, relatedid);
		if(obj instanceof BaseEntity){
			BaseEntity relate = (BaseEntity) obj;
			int collectedtimes = 0;
			if(relate.getCollectedtimes() != null && relate.getCollectedtimes() > 0){
				collectedtimes = relate.getCollectedtimes() - 1;
			}
			relate.setCollectedtimes(collectedtimes);
			daoService.updateObject(relate);
			return showJsonSuccess(model,collectedtimes + "");
		}else if(obj instanceof SportItem){
			SportItem relate = (SportItem) obj;
			int collectedtimes = 0;
			if(relate.getCollectedtimes() != null && relate.getCollectedtimes() > 0){
				collectedtimes = relate.getCollectedtimes() - 1;
			}
			relate.setCollectedtimes(collectedtimes);
			daoService.updateObject(relate);
			return showJsonSuccess(model,collectedtimes + "");
		}
		return showJsonSuccess(model);
	}

	/**
	 * gym、sport项目 想练
	 * 
	 * @param relatedid
	 * @param stattype：together
	 * @return
	 */
	@RequestMapping("/ajax/gym/updateGymAndSportTreasure.xhtml")
	public String updateGymAndSportTreasure(@CookieValue(value = LOGIN_COOKIE_NAME, required = false)
	String sessid, HttpServletRequest request, Long treasureid, Long relatedid, String stattype, String actionlabel, ModelMap model) {
		String ip = WebUtils.getRemoteIp(request);
		Member member = loginService.getLogonMemberBySessid(ip, sessid);
		if (member == null)
			return showJsonError(model, "请先登录！");
		Treasure treasure = daoService.getObject(Treasure.class, treasureid);
		if (treasure == null) {
			List<String> STATYPE_LIST = Arrays.asList("together", "playing", "played", "xiangxue");
			if(!STATYPE_LIST.contains(stattype)) return showJsonError(model, "操作类型错误！");
			ErrorCode<RemoteCourse> courseCode = synchGymService.getRemoteCourse(relatedid, true);
			if(courseCode.isSuccess()){
				RemoteCourse course = courseCode.getRetval();
				if (course != null) {
					ErrorCode<String> code = synchGymService.updateCourseByField(relatedid, stattype, 1, false);
					if(code.isSuccess()){
						treasure = new Treasure(member.getId(), "gymcourse", relatedid, stattype);
						treasure.setActionlabel(actionlabel);
						daoService.saveObject(treasure);
					}
				}
			}
			SportItem item = daoService.getObject(SportItem.class, relatedid);
			if (item != null) {
				treasure = new Treasure(member.getId(), "sportservice", relatedid, stattype);
				item.updateMembertype(stattype);
				treasure.setActionlabel(actionlabel);
				daoService.saveObjectList(item, treasure);
			}
			ErrorCode<RemoteCoach> coachCode = synchGymService.getRemoteCoach(relatedid, true);
			if(coachCode.isSuccess()){
				RemoteCoach coach = coachCode.getRetval();
				if (coach != null) {
					ErrorCode<String> code = synchGymService.updateCoachByField(relatedid, "xiangxue", 1, false);
					if(code.isSuccess()){
						treasure = new Treasure(member.getId(), "gymcoach", relatedid, Treasure.ACTION_XIANGXUE);
						treasure.setActionlabel(actionlabel);
						daoService.saveObject(treasure);
					}
				}
			}
		} else {
			treasure.setActionlabel(actionlabel);
			daoService.saveObject(treasure);
		}
		return showJsonSuccess(model);
	}

	@RequestMapping("/ajax/common/delInterest.xhtml")
	public String delInterest(@CookieValue(value = LOGIN_COOKIE_NAME, required = false)
	String sessid, HttpServletRequest request, ModelMap model, Long treasureid) {
		String ip = WebUtils.getRemoteIp(request);
		Member member = loginService.getLogonMemberBySessid(ip, sessid);
		Treasure treasure = daoService.getObject(Treasure.class, treasureid);
		if (treasure == null)
			return showJsonError(model, "参数出错");
		if (!member.getId().equals(treasure.getMemberid()))
			return showJsonError(model, "你没权限删除");
		if (!treasure.getTag().equals("sportservice") && !treasure.getTag().equals("gymcourse") && !treasure.getTag().equals("gymcoach")) {
			BaseEntity relate = (BaseEntity) relateService.getRelatedObject(treasure.getTag(), treasure.getRelatedid());
			relate.setXiangqu(relate.getXiangqu() - 1);
			daoService.saveObject(relate);
		}
		if (treasure.getTag().equals("sportservice")) {
			SportItem sportItem = daoService.getObject(SportItem.class, treasure.getRelatedid());
			sportItem.setTogether(sportItem.getTogether() - 1);
			daoService.saveObject(sportItem);
		}
		if (treasure.getTag().equals("gymcourse")) {
			ErrorCode<RemoteCourse> code = synchGymService.getRemoteCourse(treasure.getRelatedid(), true);
			if(code.isSuccess()){
				RemoteCourse course = code.getRetval();
				synchGymService.updateCoachByField(course.getId(), "together", -1, false);
			}
		}
		if (treasure.getTag().equals("gymcoach")) {
			ErrorCode<RemoteCoach> code = synchGymService.getRemoteCoach(treasure.getRelatedid(), true);
			if(code.isSuccess()){
				RemoteCoach gymCoach = code.getRetval();
				synchGymService.updateCoachByField(gymCoach.getId(), "xiangxue", -1, false);
			}
		}
		daoService.removeObject(treasure);
		return showJsonSuccess(model);
	}

	@RequestMapping("/ajax/common/getMemberTreasure.xhtml")
	public String getMemberTreasure(@CookieValue(value = LOGIN_COOKIE_NAME, required = false)
			String sessid, HttpServletRequest request, ModelMap model, 
			Long relatedid, String tag) {
		if(relatedid==null || StringUtils.isBlank(tag)){
			return show404(model, "缺少参数！");
		}
		String ip = WebUtils.getRemoteIp(request);
		Member member = loginService.getLogonMemberBySessid(ip, sessid);
		Map jsonMap = new HashMap();
		List<String> markList = MarkHelper.getMarkList();
		if(member == null){
			jsonMap.put("cTreasure", false);
			jsonMap.put("xqTreasure", false);
			Integer total = 0;
			if (!"sportservice".equals(tag) && !"gymcourse".equals(tag)) {
				Object relate = relateService.getRelatedObject(tag, relatedid);
				Integer generalMark = (Integer) BeanUtil.get(relate, "generalmark");
				Integer generalMarkTimes = (Integer) BeanUtil.get(relate, "generalmarkedtimes");
				if (generalMark != null && generalMarkTimes != null && generalMarkTimes > 0)
					total = generalMark * 10 / generalMarkTimes;
				jsonMap.put("totalGeneralMark", total);
			}
			if(markList != null){
				for(String mark : markList){
					jsonMap.put(mark, 0);
				}
			}
		}else{
			if(markList != null){
				for(String mark : markList){
					MemberMark memberMark = markService.getLastMemberMark(tag, relatedid, mark, member.getId());
					if (memberMark != null)
						jsonMap.put(mark, memberMark.getMarkvalue());
					else
						jsonMap.put(mark, 0);
				}
			}
			Treasure cTreasure = blogService.getTreasure(member.getId(), relatedid, tag, Treasure.ACTION_COLLECT);
			Treasure xqTreasure = null;
			if (tag.equals("gymcourse") || tag.equals("sportservice"))
				xqTreasure = blogService.getTreasure(member.getId(), relatedid, tag, Treasure.ACTION_TOGETHER);
			else if (tag.equals("gymcoach"))
				xqTreasure = blogService.getTreasure(member.getId(), relatedid, tag, Treasure.ACTION_XIANGXUE);
			else
				xqTreasure = blogService.getTreasure(member.getId(), relatedid, tag, Treasure.ACTION_XIANGQU);
			if (!"sportservice".equals(tag) && !"gymcourse".equals(tag) && !"activity".equals(tag)) {
				Object relate = relateService.getRelatedObject(tag, relatedid);
				Integer generalMark = (Integer) BeanUtil.get(relate, "generalmark");
				Integer generalMarkTimes = (Integer) BeanUtil.get(relate, "generalmarkedtimes");
				Integer total = generalMarkTimes == 0 ? 0 : (generalMark * 10 / generalMarkTimes);
				jsonMap.put("totalGeneralMark", total);
			}
			jsonMap.put("cTreasure", cTreasure == null ? false : true);
			jsonMap.put("xqTreasure", xqTreasure == null ? false : true);
		}
		return showJsonSuccess(model, jsonMap);
	}

	private boolean isHotMovieByMovieid(Long movieid, String citycode) {
		return mcpService.getCurMovieIdList(citycode).contains(movieid);
	}

	@RequestMapping("/showSlimbox.xhtml")
	public String showSlimbox(ModelMap model, String tag, String relatedid) {
		List<Picture> pictureList = pictureService.getPictureListByRelatedid(tag, new Long(relatedid), 0, 200);
		model.put("pictureList", pictureList);
		return "slimboxPics.vm";
	}

	/**
	 * Ajax 加载省市县
	 */
	@RequestMapping("/ajaxLoadAddress.xhtml")
	public String ajaxLoadAddress(String tag, String provincecode, String citycode, String countycode, String agtag, ModelMap model) {
		if (StringUtils.isBlank(tag)) {
			List<Province> list = placeService.getAllProvinces();
			List<Map> provinceMap = BeanUtil.getBeanMapList(list, "provincecode", "provincename");
			model.put("provinceMap", provinceMap);
		}
		if (StringUtils.equals(tag, "province") || StringUtils.isNotBlank(provincecode)) {
			List<City> list = placeService.getCityByProvinceCode(provincecode);
			List<Map> cityMap = BeanUtil.getBeanMapList(list, "citycode", "cityname");
			model.put("cityMap", cityMap);
		}
		if (StringUtils.equals(tag, "city") || StringUtils.isNotBlank(citycode)) {
			List<County> list = placeService.getCountyByCityCode(citycode);
			List<Map> countyMap = BeanUtil.getBeanMapList(list, "countycode", "countyname");
			model.put("countyMap", countyMap);
		}
		if (StringUtils.equals(tag, "county")) {
			List<Indexarea> list = placeService.getIndexareaByCountyCode(countycode);
			List<Map> indexareaMap = BeanUtil.getBeanMapList(list, "indexareacode", "indexareaname");
			model.put("indexareaMap", indexareaMap);
		}
		model.put("provincecode", provincecode);
		model.put("ctcode", citycode);
		model.put("countycode", countycode);
		model.put("agtag", agtag);
		return "common/locationAddress.vm";
	}

/*	*//********************************************************************************************************************
	 * 发表一句话影评后Ajax刷新
	 *//*
	@DERequestMapping("/ajaxLoadSeeMember.xhtml")
	public String ajaxLoadSeeMember(String tag, Long relatedid, ModelMap model) {
		List<MemberMark> markList = markService.getMarkList(tag, relatedid, "generalmark", 5);
		model.put("markList", markList);
		addCacheMember(model, ServiceHelper.getMemberIdListFromBeanList(markList));
		return "common/seeMember.vm";
	}
*/
	@RequestMapping("/ajax/common/saveAttachVideo.xhtml")
	public String saveAttachVideo(@CookieValue(value = LOGIN_COOKIE_NAME, required = false)
	String sessid, HttpServletRequest request, ModelMap model, String title, String videourl, String tag, Long relatedid, String captchaId,
			String captcha) {
		boolean isValidCaptcha = controllerService.validateCaptcha(captchaId, captcha, WebUtils.getRemoteIp(request));
		if (!isValidCaptcha)
			return showJsonError_CAPTCHA_ERROR(model);
		String ip = WebUtils.getRemoteIp(request);
		Member member = loginService.getLogonMemberBySessid(ip, sessid);
		videourl = StringUtils.trim(videourl);
		if (member == null)
			return showJsonError(model, "请先登陆！");
		if (StringUtils.equals(tag, TagConstant.TAG_MOVIE)) {
			Movie movie = daoService.getObject(Movie.class, relatedid);
			if (movie == null)
				return showJsonError(model, " 参数错误！");
		} else if (StringUtils.equals(tag, TagConstant.TAG_DRAMA)) {
			Drama drama = daoService.getObject(Drama.class, relatedid);
			if (drama == null)
				return showJsonError(model, " 参数错误！");
		}else if (StringUtils.equals(tag, TagConstant.TAG_GYMCOURSE)) {
			ErrorCode<RemoteCourse> code = synchGymService.getRemoteCourse(relatedid, true);
			if(!code.isSuccess()) return showJsonError(model, " 参数错误！");
		}
		String opkey = OperationService.TAG_ADDCONTENT + member.getId();
		if (!operationService.updateOperation(opkey, OperationService.HALF_HOUR, 30)) {
			return showJsonError(model, "添加剧情频率不能太快！");
		}
		if (StringUtils.isBlank(title))
			return showJsonError(model, "视频标题不能为空！");
		if (WebUtils.checkString(title))
			return showJsonError(model, "视频标题含有非法字符！");
		if (StringUtils.isBlank(videourl))
			return showJsonError(model, "视频地址不能为空！");
		String ext = StringUtil.getFilenameExtension(videourl);
		if (!StringUtils.equals(ext, "swf"))
			return showJsonError(model, "视频地址格式不对！");
		MemberPicture mp = new MemberPicture(tag, relatedid, member.getId(), member.getNickname(), "", "video");
		mp.setName(title);
		mp.setDescription(videourl);
		daoService.saveObject(mp);
		return showJsonSuccess(model);
	}

	@RequestMapping("/ajax/common/saveAttachPicture.xhtml")
	public String saveAttachPicture(@CookieValue(value = LOGIN_COOKIE_NAME, required = false)
	String sessid, HttpServletRequest request, ModelMap model, Long relatedid, String logo, String pname, String tag, String captchaId, String captcha) {
		boolean isValidCaptcha = controllerService.validateCaptcha(captchaId, captcha, WebUtils.getRemoteIp(request));
		if (!isValidCaptcha)
			return showJsonError_CAPTCHA_ERROR(model);
		String ip = WebUtils.getRemoteIp(request);
		Member member = loginService.getLogonMemberBySessid(ip, sessid);
		if (member == null)
			return showJsonError(model, "请先登陆！");
		String opkey = OperationService.TAG_ADDCONTENT + member.getId();
		if (!operationService.updateOperation(opkey, OperationService.HALF_HOUR, 30)) {
			return showJsonError(model, "添加剧情频率不能太快！");
		}
		if (StringUtils.isBlank(pname))
			return showJsonError(model, "图片名称不能为空！");
		if (WebUtils.checkString(pname))
			return showJsonError(model, "图片名称含有非法字符！");
		if (StringUtil.getByteLength(pname) > 60)
			return showJsonError(model, "图片名称长度不能超过60个字符");
		if (StringUtils.isBlank(logo))
			return showJsonError(model, "图片不能为空！");
		MemberPicture mp = null;
		if (StringUtils.equals(tag, TagConstant.TAG_MOVIE)) {
			Movie movie = daoService.getObject(Movie.class, relatedid);
			if (movie == null)
				return showJsonError(model, "参数错误！");
			mp = new MemberPicture(TagConstant.TAG_MOVIE, relatedid, member.getId(), member.getNickname(), logo, "pic");
		} else if (StringUtils.equals(tag, TagConstant.TAG_CINEMA)) {
			Cinema cinema = daoService.getObject(Cinema.class, relatedid);
			if (cinema == null)
				return showJsonError(model, "参数错误！");
			mp = new MemberPicture(TagConstant.TAG_CINEMA, relatedid, member.getId(), member.getNickname(), logo, "pic");
		} else if (StringUtils.equals(tag, TagConstant.TAG_DRAMA)) {
			Drama drama = daoService.getObject(Drama.class, relatedid);
			if (drama == null)
				return showJsonError(model, "参数错误！");
			mp = new MemberPicture(TagConstant.TAG_DRAMA, relatedid, member.getId(), member.getNickname(), logo, "pic");
		} else if (StringUtils.equals(tag, TagConstant.TAG_THEATRE)) {
			Theatre theatre = daoService.getObject(Theatre.class, relatedid);
			if (theatre == null)
				return showJsonError(model, "参数错误！");
			mp = new MemberPicture(TagConstant.TAG_THEATRE, relatedid, member.getId(), member.getNickname(), logo, "pic");
		} else if (StringUtils.equals(tag, TagConstant.TAG_SPORT)) {
			Sport sport = daoService.getObject(Sport.class, relatedid);
			if (sport == null)
				return showJsonError(model, "参数错误！");
			mp = new MemberPicture(TagConstant.TAG_SPORT, relatedid, member.getId(), member.getNickname(), logo, "pic");
		} else if(StringUtils.equals(tag, TagConstant.TAG_GYMCOACH)){
			ErrorCode<RemoteCoach> code = synchGymService.getRemoteCoach(relatedid, true);
			if(!code.isSuccess()) return showJsonError(model, "参数错误！");
			mp = new MemberPicture(TagConstant.TAG_GYMCOACH, relatedid, member.getId(), member.getNickname(), logo, "pic");
		} else if(StringUtils.equals(tag, TagConstant.TAG_GYMCOURSE)){
			ErrorCode<RemoteCourse> code = synchGymService.getRemoteCourse(relatedid, true);
			if (!code.isSuccess())	return showJsonError(model, "参数错误！");
			mp = new MemberPicture(TagConstant.TAG_GYMCOURSE, relatedid, member.getId(), member.getNickname(), logo, "pic");
		}else {
			return showJsonError(model, "参数出错！");
		}
		mp.setName(pname);
		daoService.saveObject(mp);
		return showJsonSuccess(model);
	}

	@RequestMapping("/ajax/common/loadSubjectHead.xhtml")
	public String loadSubjectHead(String head, ModelMap model, @CookieValue(value = LOGIN_COOKIE_NAME, required = false)
			String sessid, HttpServletRequest request) {
		Member member = loginService.getLogonMemberBySessid(WebUtils.getRemoteIp(request), sessid);
		if (member != null) {
			Integer notReadCount = memberService.getMemberNotReadMessageCount(member.getId());
			MemberInfo memberInfo = daoService.getObject(MemberInfo.class, member.getId());
			model.put("headUrl", memberInfo.getLogo());
			model.put("danger", JsonUtils.getJsonValueByKey(memberInfo.getOtherinfo(), MemberConstant.TAG_DANGER));
			model.put("notReadCount", notReadCount);
		}
		model.put("logonMember", member);
		if (headURLMap.containsKey(head)) {
			model.put("headpage", headURLMap.get(head));
		}
		return "container.vm";
	}
	@RequestMapping("/cityList.xhtml")
	public String cityList(ModelMap model){
		List list = gewaCityService.getIdxList();
		Map cityInitialsMap = BeanUtil.groupBeanList(list, "firstInitials");
		model.put("cityInitialsMap", cityInitialsMap);
		model.put("cityProMap", gewaCityService.getIdxCityMap());
		return "index/cityList.vm";
	}
	@RequestMapping("/ajax/common/loadHeadCity.xhtml")
	public String loadHeadCity(HttpServletRequest request, HttpServletResponse response, ModelMap model){
		String citycode = WebUtils.getAndSetDefault(request, response);
		model.put("hotCityList", gewaCityService.getHotCityList());
		Map cityInitialsMap = BeanUtil.groupBeanList(gewaCityService.getIdxList(), "firstInitials");
		model.put("cityInitialsMap", cityInitialsMap);
		model.put("citycode", citycode);
		return "include/headCityList.vm";
	}
	@RequestMapping("/ajax/common/getCityList.xhtml")
	public String getCityList(String pro, ModelMap model){
		Map idxProMap = gewaCityService.getIdxProCityMap();
		model.put("cityList", idxProMap.get(pro));
		return "include/cityByProList.vm";
	}
	@RequestMapping("/ajax/common/searchCity.xhtml")
	public String searchCity(String cityKey, ModelMap model){
		if(StringUtils.isNotBlank(cityKey)){
			cityKey = cityKey.toLowerCase();
			Map<GewaCity, List<GewaCity>> idxMap = gewaCityService.getIdxCityMap();
			List<GewaCity> gewaCityList = new ArrayList<GewaCity>();
			for (GewaCity gewaCity : idxMap.keySet()) {
				List<GewaCity> gcList = idxMap.get(gewaCity);
				for (GewaCity city : gcList) {
					if(StringUtils.contains(city.getCityname(), cityKey) || StringUtils.contains(city.getPinyin(), cityKey) || StringUtils.contains(city.getPy(), cityKey)){
						gewaCityList.add(city);
					}
				}
			}
			model.put("gewaCityList", gewaCityList);
		}
		return "common/citySearch.vm";
	}
	@RequestMapping("/ajax/common/isCollection.xhtml")
	public String isCollection(HttpServletRequest request, @CookieValue(value=LOGIN_COOKIE_NAME, required=false)String sessid, String tag, Long relatedid, ModelMap model){
		Member member = loginService.getLogonMemberBySessid(WebUtils.getRemoteIp(request), sessid);
		String retval = "false";
		if(member != null){
			Treasure treasure = treasureService.getTreasureByTagMemberidRelatedid(tag, member.getId(), relatedid, "collect");
			if(treasure != null){
				model.put("isCollection", true);
				retval = "true";
			}
		}
		return showJsonSuccess(model, retval);
	}
	
	@RequestMapping("/ajax/common/setMemberPoint.xhtml")
	public String memberPoint(HttpServletResponse response, String bpointx,String bpointy, ModelMap model){		
		String point = bpointx + ":" + bpointy;
		WebUtils.addCookie(response, CookieConstant.MEMBER_POINT, point, "/", 15*60*60*24);
		return showJsonSuccess(model);
	}
}
