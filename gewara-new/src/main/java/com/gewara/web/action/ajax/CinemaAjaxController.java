package com.gewara.web.action.ajax;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

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

import com.gewara.constant.CharacteristicType;
import com.gewara.constant.TagConstant;
import com.gewara.json.PageView;
import com.gewara.json.bbs.MarkCountData;
import com.gewara.model.content.Bulletin;
import com.gewara.model.content.Picture;
import com.gewara.model.movie.Cinema;
import com.gewara.model.movie.CinemaProfile;
import com.gewara.model.movie.CinemaRoom;
import com.gewara.model.movie.Movie;
import com.gewara.model.movie.MoviePlayItem;
import com.gewara.model.movie.TempMovie;
import com.gewara.model.ticket.OpenPlayItem;
import com.gewara.model.user.Member;
import com.gewara.service.bbs.MarkService;
import com.gewara.service.content.PictureService;
import com.gewara.service.movie.MCPService;
import com.gewara.service.order.OrderQueryService;
import com.gewara.service.ticket.OpenPlayService;
import com.gewara.support.ServiceHelper;
import com.gewara.untrans.CommonService;
import com.gewara.untrans.PageCacheService;
import com.gewara.untrans.PageParams;
import com.gewara.untrans.impl.ControllerService;
import com.gewara.util.BeanUtil;
import com.gewara.util.BindUtils;
import com.gewara.util.DateUtil;
import com.gewara.util.OuterSorter;
import com.gewara.util.ValidateUtil;
import com.gewara.util.WebUtils;
import com.gewara.web.action.AnnotationController;

/**
 * @author bob.hu
 * @date Aug 31, 2010 11:00:47 AM
 */
@Controller
public class CinemaAjaxController extends AnnotationController {
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

	@Autowired@Qualifier("orderQueryService")
	private OrderQueryService orderQueryService;
	public void setOrderQueryService(OrderQueryService orderQueryService) {
		this.orderQueryService = orderQueryService;
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
	@Autowired@Qualifier("commonService")
	private CommonService commonService;
	public void setCommonService(CommonService commonService) {
		this.commonService = commonService;
	}
	@Autowired@Qualifier("pictureService")
	private PictureService pictureService;
	public void setPictureService(PictureService pictureService) {
		this.pictureService = pictureService;
	}
	@Autowired@Qualifier("pageCacheService")
	private PageCacheService pageCacheService;

	// 保存电影
	@RequestMapping("/ajax/movie/saveTempMovie.xhtml")
	public String saveTempMovie(@CookieValue(value = LOGIN_COOKIE_NAME, required = false)
	String sessid, HttpServletRequest request,String captchaId,String captcha, ModelMap model) {
		boolean validate = controllerService.validateCaptcha(captchaId, captcha, WebUtils.getRemoteIp(request));
		if(!validate){
			return showJsonError(model, "验证码错误！");
		}
		Map<String, String[]> movieMap = request.getParameterMap();
		String ip = WebUtils.getRemoteIp(request);
		Member member = loginService.getLogonMemberBySessid(ip, sessid);
		if (member == null)
			return showJsonError_NOT_LOGIN(model);
		TempMovie movie = new TempMovie(member.getId());
		String id = ServiceHelper.get(movieMap, "id");
		if (StringUtils.isNotBlank(id))
			movie = daoService.getObject(TempMovie.class, new Long(id));
		BindUtils.bindData(movie, movieMap);
		if(StringUtils.isBlank(movie.getMoviename())) return showJsonError(model, "电影名称不能为空！");
		if(StringUtils.isBlank(movie.getState())) return showJsonError(model, "请选择国家或地区！");
		if(movie.getMoviename().length() > 30) return  showJsonError(model, "电影名称不能超过30个字！");
		//验证内容
		String msg = ValidateUtil.validateNewsContent(null, movie.getContent());
		if(StringUtils.isNotBlank(msg))return showJsonError(model, "剧情简介有非法字符，请重新编辑！");

		daoService.saveObject(movie);
		return showJsonSuccess(model, movie.getId().toString());
	}
	/**
	 * 校验电影名在电影库中是否存在
	 * @param movieName
	 * @param model
	 * @return
	 */
	@RequestMapping("/ajax/movie/checkTempMovieName.xhtml")
	public String checkTempMovieName(String movieName, ModelMap model){
		if(StringUtils.isBlank(movieName)){
			return this.showJsonError(model, "影片名称不能为空");
		}
		Movie movie = daoService.getObjectByUkey(Movie.class, "moviename", movieName,true);
		if(movie != null){
			return showJsonSuccess(model, "影片已存在，请不要重复添加！");
		}
		return showJsonSuccess(model, "影片不存在，可以添加！");
	}

	@RequestMapping("/ajax/cinema/getMemberAllOrderCinemaid.xhtml")
	public String ajaxgetUserAllOrderCinemaid(@CookieValue(value = LOGIN_COOKIE_NAME, required = false)
	String sessid, HttpServletRequest request, ModelMap model) {
		String ip = WebUtils.getRemoteIp(request);
		Member member = loginService.getLogonMemberBySessid(ip, sessid);
		if (member != null) {
			List<Cinema> cinemaList = orderQueryService.getMemberOrderCinemaList(member.getId(), 20);
			List<Long> cinemaidlist = BeanUtil.getBeanPropertyList(cinemaList, Long.class, "id", true);
			Map jsonMap = new HashMap();
			jsonMap.put("cinemaidlist", cinemaidlist);
			return showJsonSuccess(model, jsonMap);
		}
		return showJsonError(model, "");
	}
	private Date getPlaydate(Date fyrq){
		Date cur = new Date();
		Date playdate = null;
		if (fyrq != null) {
			playdate = fyrq;
			if (playdate.before(DateUtil.getBeginningTimeOfDay(cur))) {
				playdate = DateUtil.getBeginningTimeOfDay(cur);
			}
		} else {
			if (DateUtil.getHour(cur) < 21)
				playdate = DateUtil.getBeginningTimeOfDay(cur);
			else
				playdate = DateUtil.addDay(DateUtil.getCurDate(), 1);
		}
		return playdate;
	}
	@RequestMapping("/cinema/ajax/getCinemaSpeRoomPics.xhtml")
	public String getCinemaSpeRoomPics(Long cid, String ctype,ModelMap model) {
		if(!CharacteristicType.cTypeList.contains(ctype)){
			return this.show404(model, "特色厅类型错误!");
		}
		if(cid == null){
			return this.show404(model, "影院不存在");
		}
		Cinema cinema = daoService.getObject(Cinema.class, cid);
		if(cinema == null){
			return this.show404(model, "影院不存在");
		}
		List<Picture> pictureList = pictureService.getPictureListByRelatedid(ctype, 0L, 0, 200);
		List<Long> roomIdList = mcpService.getRoomIdListByCinemaAndCtype(cinema.getId(), ctype);
		for(Long id : roomIdList){
			pictureList.addAll(pictureService.getPictureListByRelatedid("characterroom", id, 0, 200));
		}
		if(pictureList.isEmpty()){
			return this.show404(model, "暂未上传特色影厅图片!");
		}
		model.put("pictureList", pictureList);
		model.put("ctype",ctype);
		return "cinema/wide_ajax_speRoomPics.vm";
	}
	@RequestMapping("/cinema/ajax/getSpePlayItem.xhtml")
	public String getCinemaSpePlayItem(Long cid, String ctype, ModelMap model, HttpServletRequest request, HttpServletResponse response) {
		Cinema cinema = daoService.getObject(Cinema.class, cid);
		if(cinema == null) {
			return showJsonError_NOT_FOUND(model);
		}
		String citycode = WebUtils.getAndSetDefault(request, response);
		if (pageCacheService.isUseCache(request)) {// 先使用缓存
			PageParams pageParams = new PageParams();
			pageParams.addSingleString("ctype", ctype);
			pageParams.addLong("cid",cid);
			PageView pageView = pageCacheService.getPageView(request, "cinema/ajax/getSpePlayItem.xhtml", pageParams, citycode);
			if (pageView != null) {
				model.put("pageView", pageView);
				return "pageView.vm";
			}
		}
		List<OpenPlayItem> opiList = openPlayService.getOpiList(citycode, cid, null, null, null, true);
		List<Long> roomIdList = mcpService.getRoomIdListByCinemaAndCtype(cid, ctype);
		Map<Date,List<OpenPlayItem>> opiMap = new HashMap<Date,List<OpenPlayItem>>();
		Set<Long> movieIdList = new HashSet<Long>();
		for(OpenPlayItem opi : opiList){
			if(roomIdList.contains(opi.getRoomid())){
				Date date = DateUtil.parseDate(DateUtil.formatDate(opi.getPlaytime()));
				List<OpenPlayItem> tmpList = opiMap.get(date);
				if(tmpList == null){
					tmpList = new LinkedList<OpenPlayItem>();
					opiMap.put(date, tmpList);
				}
				tmpList.add(opi);
				movieIdList.add(opi.getMovieid());
			}
		}
		List<Date> dateList = new ArrayList<Date>(opiMap.keySet());
		Collections.sort(dateList);
		List<Movie> movieList = daoService.getObjectList(Movie.class, movieIdList);
		model.put("movieMap", BeanUtil.beanListToMap(movieList, "id"));
		model.put("dateList",dateList);
		model.put("opiMap",opiMap);
		model.put("cp", daoService.getObject(CinemaProfile.class, cinema.getId()));
		return "cinema/wide_ajax_spePlayItem.vm";
	}
	@RequestMapping("/cinema/ajax/getCinemaPlayItem.xhtml")
	public String getCinemaPlayItem(@RequestParam("cid")Long cid, Date fyrq, ModelMap model, HttpServletRequest request, HttpServletResponse response) {
		Cinema cinema = daoService.getObject(Cinema.class, cid);
		if(cinema == null) return showJsonError_NOT_FOUND(model);
		
		String citycode = WebUtils.getAndSetDefault(request, response);
		if(pageCacheService.isUseCache(request)){//先使用缓存
			PageParams params = new PageParams(); 
			params.addLong("cid", cid);
			params.addDate("fyrq", fyrq);
			PageView pageView = pageCacheService.getPageView(request, "cinema/ajax/getCinemaPlayItem.xhtml", params , citycode);
			if(pageView!=null){
				model.put("pageView", pageView);
				return "pageView.vm";
			}
		}
		List<Date> playdateList = mcpService.getCurCinemaPlayDate(cid);
		Map<Long, List<MoviePlayItem>> mpiMap = new HashMap<Long, List<MoviePlayItem>>();
		Map<Long, MarkCountData> markCountMap = new HashMap<Long, MarkCountData>();
		List<MoviePlayItem> playItemList = null;
		fyrq = getPlaydate(fyrq);
		model.put("fyrq", fyrq);
		if (fyrq == null && !playdateList.isEmpty()){
			fyrq = playdateList.get(0);
		}
		if (!playdateList.isEmpty()) {
			playItemList = mcpService.getCinemaMpiList(cid, fyrq);
			Map<Long, OpenPlayItem> opiMap = new HashMap<Long, OpenPlayItem>();
			for (MoviePlayItem mpi : playItemList) {
				List<MoviePlayItem> mpiList = mpiMap.get(mpi.getMovieid());
				if (mpiList == null) {
					mpiList = new ArrayList<MoviePlayItem>();
					mpiMap.put(mpi.getMovieid(), mpiList);
				}
				OpenPlayItem opi = daoService.getObjectByUkey(OpenPlayItem.class, "mpid", mpi.getId(), true);
				if (opi != null) {
					if (!opi.isClosed()) {
						opiMap.put(mpi.getId(), opi);
						mpiList.add(mpi);
					}
				} else
					mpiList.add(mpi);
			}
			model.put("opiMap", opiMap);
		}
		Iterator iter = mpiMap.keySet().iterator();
		OuterSorter sorter = new OuterSorter<Movie>(false);
		while (iter.hasNext()) {
			Long id = (Long) iter.next();
			Movie movie = daoService.getObject(Movie.class, id);
			if (!mpiMap.get(id).isEmpty()){
				sorter.addBean(mpiMap.get(id).size(), movie);
			}
		}
		List<Movie> movieList = sorter.getDescResult();
		if (movieList.size() > 0) {
			for (Movie movie : movieList) {
				markCountMap.put(movie.getId(), markService.getMarkCountByTagRelatedid(TagConstant.TAG_MOVIE, movie.getId()));
			}
		}
		List<CinemaRoom> roomList = daoService.getObjectListByField(CinemaRoom.class, "cinemaid", cid);
		List<Long> roomidList = BeanUtil.getBeanPropertyList(roomList, Long.class, "id", true);
		Map<Long, CinemaRoom> roomMap = daoService.getObjectMap(CinemaRoom.class, roomidList);
		model.put("roomMap", roomMap);
		model.put("cinema", cinema);
		model.put("movieList", movieList);
		model.put("playdateList", playdateList);
		model.put("mpiMap", mpiMap);
		model.put("curMarkCountMap", markCountMap);
		model.put("markData", markService.getMarkdata(TagConstant.TAG_MOVIE));
		CinemaProfile cp = daoService.getObject(CinemaProfile.class, cinema.getId());
		model.put("cp", cp);
		return "cinema/wide_ajax_cinemaPlayItem.vm";
	}

	// 加在场次
	@RequestMapping("/cinemaOpi.xhtml")
	public String cinemaOpi(Long cinemaid, Long movieid, ModelMap model, HttpServletRequest request, HttpServletResponse response) {
		if (cinemaid == null || movieid == null)
			return showJsonSuccess(model);
		Timestamp from = new Timestamp(System.currentTimeMillis());
		// 未来10天可购票
		String citycode = WebUtils.getAndSetDefault(request, response);
		List<OpenPlayItem> opiList = openPlayService.getOpiList(citycode, cinemaid, movieid, from, null, true, 50);
		Map<Long, String> opiMap = new LinkedHashMap<Long, String>();
		
		for (OpenPlayItem opi : opiList) {
			StringBuilder dec = new StringBuilder();
			dec.append(DateUtil.format(opi.getPlaytime(), "d日"));
			dec.append("(" + DateUtil.getCnWeek(opi.getPlaytime()) + ")");
			dec.append(DateUtil.format(opi.getPlaytime(), "HH:mm"));
			dec.append("--" + opi.getGewaprice() + "元");
			opiMap.put(opi.getMpid(), dec.toString());
		}
		Map jsonReturn = new HashMap();
		if (opiList.size() > 0){
			jsonReturn.put("opiMap", opiMap);
		}
		return showJsonSuccess(model, jsonReturn);
	}
	// 影院介绍
	@RequestMapping("/cinema/ajax/cinemaIntroduce.xhtml")
	public String getCinemaIntroduce(@RequestParam("cid")Long cid, ModelMap model, HttpServletRequest request, HttpServletResponse response) {
		Cinema cinema = daoService.getObject(Cinema.class, cid);
		if(cinema == null)return show404(model, "电影院不存在或已经删除！");
		String citycode = cinema.getCitycode();
		WebUtils.setCitycode(request, citycode, response);
		model.put("cinema", cinema);
		//影院公告
		List<Bulletin> bulletinList = commonService.getBulletinListByTagAndTypeAndRelatedid(null, TagConstant.TAG_CINEMA, Bulletin.BULLETION_COMMON, true, cid);
		model.put("bulletinList", bulletinList);
		//影院图片
		List<Picture> pictureList = pictureService.getPictureListByRelatedid(TagConstant.TAG_CINEMA, cid, 0, 12);
		model.put("pictureList", pictureList);
		//影院图片数量
		int picCount = pictureService.getPictureCountByRelatedid("cinema", cid);
		model.put("picCount", picCount);
		return"cinema/wide_ajax_theatreIntroduce.vm";
	}
}
