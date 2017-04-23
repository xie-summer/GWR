package com.gewara.web.action.common;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Order;
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

import com.gewara.Config;
import com.gewara.constant.AdminCityContant;
import com.gewara.constant.CityData;
import com.gewara.constant.TagConstant;
import com.gewara.constant.sys.MongoData;
import com.gewara.json.PageView;
import com.gewara.model.bbs.Accusation;
import com.gewara.model.bbs.Correction;
import com.gewara.model.bbs.DiaryBase;
import com.gewara.model.bbs.DiaryComment;
import com.gewara.model.bbs.qa.GewaAnswer;
import com.gewara.model.bbs.qa.GewaQuestion;
import com.gewara.model.common.BaseInfo;
import com.gewara.model.common.County;
import com.gewara.model.common.Subwayline;
import com.gewara.model.common.Subwaystation;
import com.gewara.model.content.Bulletin;
import com.gewara.model.drama.Drama;
import com.gewara.model.movie.Cinema;
import com.gewara.model.movie.Movie;
import com.gewara.model.sport.SportItem;
import com.gewara.model.user.Member;
import com.gewara.model.user.UserMessage;
import com.gewara.model.user.UserMessageAction;
import com.gewara.service.PlaceService;
import com.gewara.service.bbs.DiaryService;
import com.gewara.service.drama.DramaService;
import com.gewara.service.movie.MCPService;
import com.gewara.service.sport.SportService;
import com.gewara.support.ErrorCode;
import com.gewara.support.ServiceHelper;
import com.gewara.untrans.CommentService;
import com.gewara.untrans.PageCacheService;
import com.gewara.untrans.PageParams;
import com.gewara.untrans.gym.SynchGymService;
import com.gewara.util.BindUtils;
import com.gewara.util.HttpResult;
import com.gewara.util.HttpUtils;
import com.gewara.util.JsonUtils;
import com.gewara.util.StringUtil;
import com.gewara.util.WebUtils;
import com.gewara.web.action.BaseHomeController;
import com.gewara.web.support.WindowsLiveLogin;
import com.gewara.xmlbind.bbs.Comment;
import com.gewara.xmlbind.gym.RemoteCourse;

@Controller
public class CommonIndexController extends BaseHomeController{
	@Autowired@Qualifier("hibernateTemplate")
	private HibernateTemplate hibernateTemplate;
	public void setHibernateTemplate(HibernateTemplate hibernateTemplate) {
		this.hibernateTemplate = hibernateTemplate;
	}

	@Autowired@Qualifier("placeService")
	private PlaceService placeService;
	public void setPlaceService(PlaceService placeService) {
		this.placeService = placeService;
	}
	@Autowired@Qualifier("commentService")
	private CommentService commentService;
	@Autowired@Qualifier("config")
	private Config config;
	public void setConfig(Config config) {
		this.config = config;
	}
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
	@Autowired@Qualifier("diaryService")
	private DiaryService diaryService;
	public void setDiaryService(DiaryService diaryService){
		this.diaryService = diaryService;
	}
	@Autowired@Qualifier("sportService")
	private SportService sportService;
	public void setSportService(SportService sportService) {
		this.sportService = sportService;
	}
	@Autowired@Qualifier("dramaService")
	private DramaService dramaService;
	public void setDramaService(DramaService dramaService) {
		this.dramaService = dramaService;
	}
	@Autowired@Qualifier("pageCacheService")
	private PageCacheService pageCacheService;
	public void setPageCacheService(PageCacheService pageCacheService) {
		this.pageCacheService = pageCacheService;
	}
	
	@RequestMapping("/showResult.xhtml")
	public String showResult(){
		//showResult.vm
		return "wide_backPage.vm";
	}
	@RequestMapping("/common/picturePrint.xhtml")
	public String toPrintCoupon(@RequestParam("cid")Long cid,ModelMap model){
		Bulletin bulletin = daoService.getObject(Bulletin.class, cid);
		model.put("bulletin",bulletin);
		return "common/print.vm";
	}
	@RequestMapping("/common/googleMap.xhtml")
	public String googleMap(Long id, String tag, String from, HttpServletRequest request, HttpServletResponse response, ModelMap model){
		if(id==null || StringUtils.isBlank(tag)) return showError(model, "缺少参数！");
		Object object = relateService.getRelatedObject(tag, id);
		String citycode = WebUtils.getAndSetDefault(request, response);
		if(StringUtils.isNotBlank(from)) {
			String cityname = AdminCityContant.getCitycode2CitynameMap().get(citycode);
			if(from.indexOf(cityname)==-1) from =cityname + "市" + from;
		}
		model.put("object", object);
		model.put("from", from);
		model.put("cityData", new CityData());
		model.put("citycode", citycode);
		return "common/googlemap.vm";
	}
	/**
	 * 实现百度地图的查询
	 * @param id
	 * @param tag
	 * @param from
	 * @param request
	 * @param response
	 * @param model
	 * @return
	 */
	@RequestMapping("/common/baiduMap.xhtml")
	public String baiduMap(Long id, String tag, String from, String end, String title, HttpServletRequest request, HttpServletResponse response, ModelMap model){
		if(id==null || StringUtils.isBlank(tag)) return showError(model, "缺少参数！");
		Object object = relateService.getRelatedObject(tag, id);
		String citycode = WebUtils.getAndSetDefault(request, response);
		String cityname = AdminCityContant.getCitycode2CitynameMap().get(citycode);
		String[] cityew = AdminCityContant.citycodeBPointMap.get(citycode);
		if(StringUtils.isNotBlank(from)) {
			cityname = AdminCityContant.getCitycode2CitynameMap().get(citycode);
			if(from.indexOf(cityname)==-1) from =cityname + "市" + from;
		}
		model.put("object", object);
		model.put("end", end);
		model.put("title", title);
		model.put("from", from);
		model.put("cityname", cityname);
		model.put("cityew", cityew);
		model.put("cityData", new CityData());
		model.put("citycode", citycode);
		model.put("tag", tag);
		return "common/wide_baiduMap.vm";
	}
	/**
	 * 查询所有场馆的通用方法
	 * @param tag
	 * @param countycode
	 * @param model
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping("/common/relateMap.xhtml")
	public String relateMap(String tag, String countycode, ModelMap model, HttpServletRequest request, HttpServletResponse response){
		Class clazz = ServiceHelper.getPalceClazz(tag);
		if(clazz == null){
			clazz = Cinema.class;
			tag = TagConstant.TAG_CINEMA;
		}
		List<BaseInfo>	baseInfoList = getPalceQuery(clazz, countycode);
		String citycode = WebUtils.getAndSetDefault(request, response);
		List<County> countyList = placeService.getCountyByCityCode(citycode);
		model.put("countyList", countyList);
		model.put("cinemaList", baseInfoList);
		model.put("cityData", new CityData());
		model.put("tag", tag);
		return "common/relateMap.vm";
	}
	/**
	 * 得到baseInfo实体类集合
	 * @param tag
	 * @param countycode
	 * @return
	 */
	private <T extends BaseInfo> List<T> getPalceQuery(Class<T>	clazz, String countycode){
		DetachedCriteria query = DetachedCriteria.forClass(clazz);
		if(clazz.equals(Cinema.class)) {
			query.add(Restrictions.eq("booking", Cinema.BOOKING_OPEN));
		}
		if(StringUtils.isNotBlank(countycode)){
			query.add(Restrictions.eq("countycode", countycode));
		}
		query.setProjection(Projections.property("id"));
		query.addOrder(Order.desc("id"));
		List<Long> idList = hibernateTemplate.findByCriteria(query);
		List<T> baseInfoList = daoService.getObjectList(clazz, idList);
		return baseInfoList;
	}
	@RequestMapping("/common/addPlace.xhtml")
	public String addPlace(ModelMap model, HttpServletRequest request, HttpServletResponse response){
		Long defaultLine = 0L;
		String citycode = WebUtils.getAndSetDefault(request, response);
		List<County> countyList = placeService.getCountyByCityCode(citycode);
		List<Subwayline> lineList = placeService.getSubwaylinesByCityCode(citycode);
		List<Subwaystation> stationList =  new ArrayList<Subwaystation>();
		Subwayline subwayline = daoService.getObject(Subwayline.class, defaultLine);
		if(subwayline!=null) stationList = placeService.getSubwaystationsByLineId(subwayline.getId());
		model.put("lineList", lineList);
		model.put("stationList", stationList);
		model.put("countyList", countyList);
		String referer = request.getHeader("Referer");
		model.put("referer", referer);
		return "common/addPlace.vm";
	}
	@RequestMapping("/common/saveAccusation.xhtml")
	public String saveAccusation(@CookieValue(value=LOGIN_COOKIE_NAME, required=false)String sessid, HttpServletRequest request, String captchaId, String captcha, ModelMap model){
		boolean isValidCaptcha = controllerService.validateCaptcha(captchaId, captcha, WebUtils.getRemoteIp(request));
		if(!isValidCaptcha) return showJsonError(model, "验证码错误！");
		Member member = loginService.getLogonMemberBySessid(WebUtils.getRemoteIp(request), sessid);
		Map dataMap = request.getParameterMap();
		Accusation acc = new Accusation("");
		BindUtils.bindData(acc, dataMap);
		if(member!=null) {
			acc.setMemberid(member.getId());
			acc.setEmail(member.getEmail());
		}
		acc.setRelatedid(acc.getRelatedid2());
		acc.setTag(acc.getTag2());
		acc.setReferer(request.getHeader("Referer"));
		if(Accusation.TAG_DIARY.equals(acc.getTag())) {
			DiaryBase diary =  diaryService.getDiaryBase(acc.getRelatedid());
			if(diary!=null)acc.setMessage(diary.getSubject());
		}else if(Accusation.TAG_DIARYCOMMENT.equals(acc.getTag())) {
			DiaryComment comment = daoService.getObject(DiaryComment.class, acc.getRelatedid());
			if(comment!=null) acc.setMessage(StringUtil.getHtmlText(comment.getBody(), 200));
		}else if(Accusation.TAG_GEWAQUESTION.equals(acc.getTag())) {
			GewaQuestion question = daoService.getObject(GewaQuestion.class, acc.getRelatedid());
			if(question!=null)acc.setMessage(question.getTitle());
		}else if(Accusation.TAG_GEWAANSWER.equals(acc.getTag())) {
			GewaAnswer answer= daoService.getObject(GewaAnswer.class, acc.getRelatedid());
			if(answer!=null)acc.setMessage(StringUtil.getHtmlText(answer.getContent(), 200));
		}else if(Accusation.TAG_COMMENT.equals(acc.getTag())) {
			Comment comment= commentService.getCommentById(acc.getRelatedid());
			if(comment!=null)acc.setMessage(StringUtil.getHtmlText(comment.getBody(), 200));
		}else if(Accusation.TAG_USERMESSAGE.equals(acc.getTag())){
			UserMessageAction userMessageAction=daoService.getObject(UserMessageAction.class, acc.getRelatedid());
			UserMessage userMessage=daoService.getObject(UserMessage.class, userMessageAction.getUsermessageid());
			if(userMessage!=null)acc.setMessage(StringUtil.getHtmlText(userMessage.getContent(), 200));
		}
		daoService.saveObject(acc);
		return showJsonSuccess(model);
	}
	@RequestMapping("/common/saveCorr.xhtml")
	public String saveCorr(@CookieValue(value=LOGIN_COOKIE_NAME, required=false)String sessid, HttpServletRequest request, String captchaId, String captcha, String mistag, ModelMap model){
		boolean isValidCaptcha = controllerService.validateCaptcha(captchaId, captcha, WebUtils.getRemoteIp(request));
		if(!isValidCaptcha) return showJsonError(model, "验证码错误！");
		Member member = loginService.getLogonMemberBySessid(WebUtils.getRemoteIp(request), sessid);
		String content = request.getParameter("corrcontent");
		Correction corr = new Correction(content);
		Map dataMap = request.getParameterMap();
		BindUtils.bindData(corr, dataMap);
		if(StringUtil.getByteLength(corr.getContent())>20000) return showError(model, "内容字符过长！");
		if(member!=null) {
			corr.setMemberid(member.getId());
			corr.setEmail(member.getEmail());
		}
		corr.setTag(mistag);
		corr.setReferer(request.getHeader("Referer"));
		daoService.saveObject(corr);
		return showJsonSuccess(model);
	}
	@RequestMapping("/common/subwayList.xhtml")
	public String bussbulletinList(ModelMap model, HttpServletRequest request, HttpServletResponse response) {
		String citycode = WebUtils.getAndSetDefault(request, response);
		List<Subwayline> lineList = placeService.getSubwaylinesByCityCode(citycode);
		Map<Long, List<Subwaystation>> stationMap = new HashMap<Long, List<Subwaystation>>();
		for(Subwayline line: lineList){
			stationMap.put(line.getId(), placeService.getSubwaystationsByLineId(line.getId()));
		}
		model.put("stationMap", stationMap);
		model.put("lineList", lineList);
		return "admin/common/subwayList.vm";
	}

	@RequestMapping("/cmpCss.xhtml")
	public void cmpCss(HttpServletRequest req, HttpServletResponse res, String css) throws Exception{
		dbLogger.warn("执行css压缩:"+req.getHeader("REFERER"));
		res.setContentType("text/css; charset=UTF-8");
		cmp(css, res);
	}
	@RequestMapping("/cmpJs.xhtml")
	public void cmpJs(HttpServletRequest req, HttpServletResponse res, String js) throws Exception{
		dbLogger.warn("执行JS压缩:"+req.getHeader("REFERER"));
		res.setContentType("application/x-javascript; charset=UTF-8");
		res.setCharacterEncoding("utf-8");
		cmp(js, res);
	}
	private void cmp(String params, HttpServletResponse res) throws Exception{
		if(StringUtils.isNotBlank(params)) {
			PrintWriter out = res.getWriter();
			String[] cs = params.split("\\?")[0].split(",");
			for(String name : cs){
				HttpResult str = HttpUtils.getUrlAsString("http://localhost:8080" + config.getBasePath() + ""+name, null);
				if(StringUtils.isNotBlank(str.getResponse())) out.print(str.getResponse());
			}
			out.flush();
			out.close();
		}
	}
	@RequestMapping("/service/job.dhtml")
	public String job(ModelMap model){
		List<Map> jobMapList =	mongoService.getMapList(MongoData.NS_JOB_NAMESPACE);
		if(jobMapList!=null) {
			Comparator<Map> comparator = new Comparator<Map>() {
				 public int compare(Map o1, Map o2) {
						return o2.get("jobid").toString().compareTo(o1.get("jobid").toString());
				 }
			};
			Collections.sort(jobMapList, comparator);
		}
		model.put("jobMapList", jobMapList);
		return "common/job.vm";
	}
	@RequestMapping("/common/inviteByMsn.xhtml")
	public String inviteByMsn(String backurl, HttpServletRequest request, HttpServletResponse response, ModelMap model){
		WindowsLiveLogin wll = new WindowsLiveLogin("msnAuth.xml");
		String action = request.getParameter("action");
		String delauthtoken = "";
		if ("delauth".equals(action)) {
			WindowsLiveLogin.ConsentToken token = wll.processConsent(request.getParameterMap());
			if (token != null) {
				delauthtoken = token.getToken();
				Cookie cookie = new Cookie("delauthtoken", token.getToken());
				cookie.setPath("/");
				cookie.setMaxAge(60 * 60);//24 hour
				response.addCookie(cookie);
			}
		}
		Map<String, String> msnMap = setMsnData(delauthtoken);
		Map<String, String> params = new HashMap<String, String>();
		params.put("jsonMsnMap", JsonUtils.writeMapToJson(msnMap));
		model.put("submitParams", params);
		model.put("method", "post");
		model.put("submitUrl", backurl);
		return "tempSubmitForm.vm";
	}
	public Map<String, String> setMsnData(@CookieValue(value="delauthtoken", required=false)String delauthtoken ){
		WindowsLiveLogin wll = new WindowsLiveLogin("msnAuth.xml");
		WindowsLiveLogin.ConsentToken token = wll.processConsentToken(delauthtoken);
		if ((token != null) && !token.isValid()) {
			token = null;
		}
		Map<String, String> m = new HashMap<String, String>();
		if(token!=null){
			m = WindowsLiveLogin.getContact(token);
		}
		return m;
	}
	@RequestMapping("/common/selectModel.xhtml")
	public String selectModel(String tag, Long relatedid, Long categoryid, String countycode, String indexareacode, 
			ModelMap model, HttpServletRequest request, HttpServletResponse response){
		String citycode = WebUtils.getAndSetDefault(request, response);
		if(pageCacheService.isUseCache(request) && categoryid==null){//先使用缓存
			PageParams pageParams = new PageParams();
			pageParams.addSingleString("tag", tag);
			pageParams.addSingleString("countycode", countycode);
			pageParams.addSingleString("indexareacode", indexareacode);
			pageParams.addLong("relatedid", relatedid);

			PageView pageView = pageCacheService.getPageView(request, "common/selectModel.xhtml", pageParams, citycode);
			if(pageView!=null){
				model.put("pageView", pageView);
				return "pageView.vm";
			}
		}
		String category = "";
		if(!("activity".equals(tag))){
			List<County> countyList = placeService.getCountyByCityCode(citycode);
			model.put("countyList", countyList);
			if(StringUtils.isNotBlank(countycode)){
				List<Map> indexareaList = placeService.getPlaceIndexareaCountMap(ServiceHelper.getPalceClazz(tag), countycode);
				model.put("indexareaList", indexareaList);
			}
		}
		List baseList = new ArrayList<BaseInfo>();
		if(StringUtils.isNotBlank(tag)){
			if("cinema".equals(tag)){
				List<Movie> futureMovieList = mcpService.getFutureMovieList(0, 10,null);
				List<Movie> movieList = mcpService.getCurMovieListByMpiCount(citycode, 0, 40);
				movieList.removeAll(futureMovieList);
				movieList.addAll(futureMovieList);
				if(categoryid!=null){
					Movie movie = daoService.getObject(Movie.class, relatedid);
					if(!movieList.contains(movie)) movieList.add(0, movie);
				}
				category = "movie";
				model.put("objectzList", movieList);
			}else if("gym".equals(tag)){
				ErrorCode<List<RemoteCourse>> code = synchGymService.getSubCourseListById(0L);//gymService.getTopCourse();
				if(code.isSuccess()){
					Map<Long, List<RemoteCourse>> courseMap = new HashMap<Long, List<RemoteCourse>>();
					List<RemoteCourse> courseList = code.getRetval();
					for (RemoteCourse course : courseList) {
						ErrorCode<List<RemoteCourse>> code2 = synchGymService.getSubCourseListById(course.getId());
						if(code2.isSuccess())courseMap.put(course.getId(), code2.getRetval());
					}
					category = "gymcourse";
					model.put("objectzList", courseList);
					model.put("courseMap", courseMap);
				}
			}else if("sport".equals(tag)){
				List<SportItem> sportItemList = sportService.getTopSportItemList();
				model.put("objectzList", sportItemList);
				category = "sportservice";
			}else if("theatre".equals(tag)){
				Date cur = new Date();
				category = "drama";
				List<Drama> dramaList = dramaService.getCurDramaList(citycode, cur, null, 0, 50);
				dramaList.addAll(dramaService.getFutureDramaList(citycode, cur, 0, 30));
				if(categoryid!=null) {
					Drama drama = daoService.getObject(Drama.class, relatedid);
					if(!dramaList.contains(drama))dramaList.add(0, drama);
				}
				model.put("objectzList", dramaList);
			}
			if(StringUtils.isNotBlank(countycode) && !StringUtils.equals(countycode, "defaultCode")){
				baseList = placeService.getPlaceListByTag(tag, countycode, indexareacode, 0, 200);
			}else {
				if("cinema".equals(tag)){
					baseList = mcpService.getBookingCinemaList(citycode);
				}else {
					baseList = placeService.getPlaceListByHotvalue(citycode, ServiceHelper.getPalceClazz(tag), 0, 100);
				}
			}
		}
		model.put("tag", tag);
		model.put("category", category);
		model.put("countycode", countycode);
		model.put("indexareacode", indexareacode);
		model.put("relatedid", relatedid);
		model.put("category", category);
		model.put("categoryid", categoryid);
		model.put("baseList", baseList);
		return "common/selectModel.vm";
	}
	@RequestMapping("/gewa/newuser/helpInfo.xhtml")
	public String newUserHelp(){
		return "footer/help/userHelp.vm";
	}
	@RequestMapping("/common/getLineidList.xhtml")
	public String getLineidList(Long sid, ModelMap model){
		String qry = "select s.line.id from Line2Station s where s.station.id=?";
		List<Long> lineidList = hibernateTemplate.find(qry, sid);
		return showJsonSuccess(model, StringUtils.join(lineidList, ","));
	}
	@RequestMapping("/common/getPolice.xhtml")
	public String getPolice(){
		return "/common/networkPolice.vm";
	}
}
