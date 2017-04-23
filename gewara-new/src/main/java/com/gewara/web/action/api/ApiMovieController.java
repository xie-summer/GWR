package com.gewara.web.action.api;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import com.gewara.constant.ApiConstant;
import com.gewara.helper.ticket.CloseRuleOpiFilter;
import com.gewara.helper.ticket.OpiFilter;
import com.gewara.json.PhoneActivity;
import com.gewara.model.api.ApiUser;
import com.gewara.model.bbs.Diary;
import com.gewara.model.movie.Cinema;
import com.gewara.model.movie.Movie;
import com.gewara.model.movie.MoviePlayItem;
import com.gewara.model.partner.PartnerCloseRule;
import com.gewara.model.ticket.OpenPlayItem;
import com.gewara.service.bbs.BlogService;
import com.gewara.service.bbs.DiaryService;
import com.gewara.service.movie.MCPService;
import com.gewara.service.order.OrderQueryService;
import com.gewara.service.partner.PartnerService;
import com.gewara.util.BeanUtil;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

@Controller
public class ApiMovieController extends BaseApiController{

	@Autowired@Qualifier("mcpService")
	private MCPService mcpService;
	public void setMcpService(MCPService mcpService) {
		this.mcpService = mcpService;
	}
	@Autowired@Qualifier("orderQueryService")
	private OrderQueryService orderQueryService;
	@Autowired@Qualifier("partnerService")
	private PartnerService partnerService;
	public void setPartnerService(PartnerService partnerService) {
		this.partnerService = partnerService;
	}
	@Autowired
	@Qualifier("blogService")
	protected BlogService blogService;
	public void setBlogService(BlogService blogService) {
		this.blogService = blogService;
	}
	@Autowired@Qualifier("diaryService")
	private DiaryService diaryService;
	public void setDiaryService(DiaryService diaryService) {
		this.diaryService = diaryService;
	}
	@RequestMapping("/api/movie/movie.xhtml")
	public String movie(String key, Long movieid, String encryptCode, String citycode, ModelMap model){
		ApiAuth auth = checkRights(encryptCode, key);
		if(!auth.isChecked()) return getErrorXmlView(model, auth.getCode(), auth.getMsg());
		if(movieid==null) return getErrorXmlView(model, ApiConstant.CODE_PARAM_ERROR, "传递参数错误！");
		
		Movie movie = daoService.getObject(Movie.class, movieid);
		if(movie == null) return getErrorXmlView(model, ApiConstant.CODE_DATA_ERROR, "没有查询信息！");
		ApiUser partner = auth.getApiUser();
		if(StringUtils.isNotBlank(citycode)){
			if(!partner.supportsCity(citycode)) return getErrorXmlView(model, ApiConstant.CODE_PARTNER_NORIGHTS, "不支持城市" + citycode);
		}else{
			citycode = partner.getDefaultCity();
		}
		Integer cinemaNum = mcpService.getPlayCinemaCount(citycode, movieid);
		Integer orderCinemaNum = mcpService.getOrderPlayCinemaCount(citycode, movieid);
		model.put("cinemaNum", cinemaNum);
		model.put("orderCinemaNum", orderCinemaNum);
		model.put("movie", movie);
		model.put("generalmark", getMovieMark(movie));
		return getXmlView(model, "api/info/movie/movie.vm");
	}
	@RequestMapping("/api/movie/movieListByIds.xhtml")
	public String movieListByIds(String key, String movieids, String encryptCode, ModelMap model){
		ApiAuth auth = checkRights(encryptCode, key);
		if(!auth.isChecked()) return getErrorXmlView(model, auth.getCode(), auth.getMsg());
		if(movieids==null) return getErrorXmlView(model, ApiConstant.CODE_PARAM_ERROR, "传递参数错误！");
		List<Long> movieidList = BeanUtil.getIdList(movieids, ",");
		List<Movie> movieList = daoService.getObjectList(Movie.class, movieidList);
		model.put("movieList", movieList);
		return getXmlView(model, "api/info/movie/movieListByIds.vm");
	}
	@RequestMapping("/api/movie/playMovieList.xhtml")
	public String playMovieList(String key, Long cinemaid, Long movieid, Date playdate, String encryptCode, ModelMap model){
		ApiAuth auth = checkRights(encryptCode, key);
		if(!auth.isChecked()) return getErrorXmlView(model, auth.getCode(), auth.getMsg());
		if(cinemaid==null || playdate==null) return getErrorXmlView(model, ApiConstant.CODE_PARAM_ERROR, "影院ID、日期不能为空！");

		List<MoviePlayItem>  mpiList = null;
		if(movieid!= null){
			mpiList = mcpService.getCurMpiList(cinemaid, movieid, playdate);
		} else {
			mpiList = mcpService.getCinemaCurMpiListByDate(cinemaid, playdate);
		}
		ApiUser partner = auth.getApiUser();
		Cinema cinema = daoService.getObject(Cinema.class, cinemaid);
		List<OpenPlayItem> opiList = partnerService.getPartnerOpiList(partner, cinema.getCitycode(), cinemaid, movieid, playdate);
		List<PartnerCloseRule> pcrList = partnerService.getCloseRuleList();
		OpiFilter filter = new CloseRuleOpiFilter(partner, pcrList);
		filter.applyFilter(opiList);
		
		List<Long> mpidList = BeanUtil.getBeanPropertyList(opiList, Long.class, "mpid", true);
		Map<Long, Movie> movieMap = daoService.getObjectMap(Movie.class, BeanUtil.getBeanPropertyList(mpiList, Long.class, "movieid", true));
		if(movieMap.isEmpty()) return getErrorXmlView(model, ApiConstant.CODE_DATA_ERROR, "没有查询信息！");
		model.put("mpidList", mpidList);
		model.put("movieMap", movieMap);
		model.put("mpiList", mpiList);
		return getXmlView(model, "api/info/movie/playMovieList.vm");
	}
	@RequestMapping("/api/movie/playNowMovieList.xhtml")
	public String playNowMovieList(String key, String citycode, ModelMap model, String encryptCode){
		ApiAuth auth = checkRights(encryptCode, key);
		if(!auth.isChecked()) return getErrorXmlView(model, auth.getCode(), auth.getMsg());
		ApiUser partner = auth.getApiUser();
		if(StringUtils.isNotBlank(citycode)){
			if(!partner.supportsCity(citycode)) return getErrorXmlView(model, ApiConstant.CODE_PARTNER_NORIGHTS, "不支持城市" + citycode);
		}else{
			citycode = partner.getDefaultCity();
		}
		List<Movie>  pnmList = mcpService.getCurMovieListByMpiCount(citycode, 0, 100);
		Map<Long, Integer> cinemaNumMap = new HashMap<Long, Integer>();
		Map<Long, Integer> orderCinemaNumMap = new HashMap<Long, Integer>();
		for(Movie movie:pnmList){
			Integer cinemaNum = mcpService.getPlayCinemaCount(citycode, movie.getId());
			Integer orderCinemaNum = mcpService.getOrderPlayCinemaCount(citycode, movie.getId());
			cinemaNumMap.put(movie.getId(), cinemaNum);
			orderCinemaNumMap.put(movie.getId(), orderCinemaNum);
		}
		List<PartnerCloseRule> pcrList = partnerService.getCloseRuleList();
		
		CloseRuleOpiFilter filter = new CloseRuleOpiFilter(auth.getApiUser(), pcrList);
		filter.filterMovie(pnmList);
		
		model.put("pnmList", pnmList);
		model.put("cinemaNumMap", cinemaNumMap);
		model.put("orderCinemaNumMap", orderCinemaNumMap);
		return getXmlView(model, "api/info/movie/playNowMovieList.vm");
	}
	@RequestMapping("/api/movie/futureMovieList.xhtml")
	public String futureMovieList(String key, Integer from, Integer maxnum, String encryptCode, ModelMap model){
		ApiAuth auth = checkRights(encryptCode, key);
		if(!auth.isChecked()) return getErrorXmlView(model, auth.getCode(), auth.getMsg());
		if(from == null) from=0;
		if(maxnum == null) maxnum=100;
		List<Movie>  mpiList = mcpService.getFutureMovieList(from, maxnum,null);
		List<PartnerCloseRule> pcrList = partnerService.getCloseRuleList();
		
		CloseRuleOpiFilter filter = new CloseRuleOpiFilter(auth.getApiUser(), pcrList);
		filter.filterMovie(mpiList);
		model.put("mpiList", mpiList);
		return getXmlView(model, "api/info/movie/futureMovieList.vm");
	}
	@RequestMapping("/api/movie/playCinemaList.xhtml")
	public String playCinemaList(String key, Integer from, Integer maxnum, Long movieid, Date playdate, String citycode, String encryptCode, ModelMap model){
		ApiAuth auth = checkRights(encryptCode, key);
		if(!auth.isChecked()) return getErrorXmlView(model, auth.getCode(), auth.getMsg());
		if(from==null) from = 0;
		if(maxnum==null) maxnum = 100; 
		/**API调用判断 end**/
		ApiUser partner = auth.getApiUser();
		if(StringUtils.isNotBlank(citycode)){
			if(!partner.supportsCity(citycode)) return getErrorXmlView(model, ApiConstant.CODE_PARTNER_NORIGHTS, "不支持城市" + citycode);
		}else{
			citycode = partner.getDefaultCity();
		}
		List<Cinema> cinemaList = mcpService.getPlayCinemaList(citycode, movieid, playdate, from, maxnum);
		List<Cinema> opcList = mcpService.getBookingCinemaList(citycode);
		Map<Long, Boolean> isOpenMap = new HashMap<Long, Boolean>();
		for(Cinema cinema:cinemaList){
			if(opcList.contains(cinema)) isOpenMap.put(cinema.getId(), true);
		}
		model.put("isOpenMap", isOpenMap);
		model.put("cinemaList", cinemaList);
		return getXmlView(model, "api/info/movie/playCinemaList.vm");
	}
	/**
	 * 获取订单有效时间
	 * @param key
	 * @param encryptCode
	 * @param tradeNo
	 * @param model
	 * @param request
	 * @return
	 */
	@RequestMapping("/api/common/getTicketValidTime.xhtml")
	public String getTicketHelp(String key, String encryptCode,String tradeNo,ModelMap model,HttpServletRequest request){
		Long valid = orderQueryService.getOrderValidTime(tradeNo);
		if(valid==null){
			return getErrorXmlView(model,ApiConstant.CODE_SIGN_ERROR,"有效时间已过");
		}
		Long cur = System.currentTimeMillis();
		Long remain = valid - cur;
		model.put("remain", remain);
		return getXmlView(model,"api/order/orderValidTime.vm");
	}
	/**
	 *  电影影评列表
	 * @param key
	 * @param encryptCode
	 * @param tag
	 * @param relateid
	 * @param type
	 * @param orderField
	 * @param returnField
	 * @return
	 */
	@RequestMapping("/api/common/diaryList.xhtml")
	public String diaryList(String key, String encryptCode,String tag,Long relateid,String type,String orderField,Integer from,Integer maxnum,
			String returnField,ModelMap model,HttpServletRequest request){
		ApiAuth auth = this.check(key,encryptCode,request);
		if (!auth.isChecked()){
			return getErrorXmlView(model, auth.getCode(), auth.getMsg());
		}
		if(relateid == null){
			return getErrorXmlView(model,ApiConstant.CODE_SIGN_ERROR, "relateid is not null");
		}
		if(StringUtils.isBlank(returnField)){
			returnField = "id,subject";
		}
		model.put("returnField", returnField);
		if("diary".equals(type)){
			type = "%diary";
		}
		if(from == null || maxnum == null){
			return getErrorXmlView(model,ApiConstant.CODE_SIGN_ERROR, "from and maxnum is not null");
		}
		List<Diary> diarys = diaryService.getDiaryListByOrder(Diary.class, null, type, tag, relateid, null, null, orderField, false, from, maxnum);
		model.put("diarys", diarys);
 		return getXmlView(model,"api/mobile/diaryList.vm");
	}
	@RequestMapping("/api/common/diaryDetail.xhtml")
	public String diaryDetail(String key, String encryptCode,Long diaryid,String returnField,ModelMap model,HttpServletRequest request){
		ApiAuth auth = this.check(key,encryptCode,request);
		if (!auth.isChecked()){
			return getErrorXmlView(model, auth.getCode(), auth.getMsg());
		}
		if(diaryid == null){
			return getErrorXmlView(model, ApiConstant.CODE_SIGN_ERROR, "diaryid is not null");
		}
		if(StringUtils.isBlank(returnField)){
			returnField = "id,subject";
		}
		model.put("returnField", returnField);
		model.put("diary", this.daoService.getObject(Diary.class, diaryid));
		model.put("content", blogService.getDiaryBody(diaryid));
		return getXmlView(model,"api/mobile/diaryDetail.vm");
	}
	@RequestMapping("/api/common/phoneActivityList.xhtml")
	public String phoneActivityList(String key, String encryptCode,String apptype,String osType,String citycode,
			int from,int maxnum,ModelMap model,HttpServletRequest request){
		ApiAuth auth = this.check(key,encryptCode,request);
		if (!auth.isChecked()){
			return getErrorXmlView(model, auth.getCode(), auth.getMsg());
		}
		if(!(PhoneActivity.OS_TYPE_ANDROID.equals(osType) || PhoneActivity.OS_TYPE_IPHONE.equals(osType))){
			return getErrorXmlView(model, ApiConstant.CODE_SIGN_ERROR, "ostype 应为ANDROID或IPHONE");
		}
		DBObject params = new BasicDBObject();
		DBObject inparams = new BasicDBObject();
		inparams.put("$in", new String[]{osType, PhoneActivity.OS_TYPE_ALL});
		params.put("status", PhoneActivity.STATUS_NEW);
		params.put("apptype", apptype);
		params.put("ostype", inparams);
		if(StringUtils.isNotBlank(citycode)){
			Pattern pattern = Pattern.compile(citycode,Pattern.CASE_INSENSITIVE);
			params.put("citycode", pattern);
		}
		if(maxnum > 20){
			maxnum = 20;
		}
		List<PhoneActivity> phoneActivityList = mongoService.getObjectList(PhoneActivity.class, params,"rank",true,from,maxnum);
		model.put("phoneActivityList", phoneActivityList);
		return getXmlView(model,"api/mobile/phoneActivityList.vm");
	}
	
	@RequestMapping("/api/common/addUpcomingFilmRemind.xhtml")
	public String addUpcomingFilmRemind(ModelMap model){
		return getErrorXmlView(model, ApiConstant.CODE_NOT_EXISTS, "电影节已过期！");
	}
	
	@RequestMapping("/api/common/delUpcomingFilmRemind.xhtml")
	public String delUpcomingFilmRemind(ModelMap model){
		return getErrorXmlView(model, ApiConstant.CODE_NOT_EXISTS, "电影节已过期！");
	}
}