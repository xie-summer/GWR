package com.gewara.web.action.api2.movie;

import java.util.Arrays;
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
import org.springframework.web.bind.annotation.RequestParam;

import com.gewara.constant.AdminCityContant;
import com.gewara.constant.ApiConstant;
import com.gewara.constant.DiaryConstant;
import com.gewara.constant.Status;
import com.gewara.helper.ticket.CloseRuleOpiFilter;
import com.gewara.json.PhoneActivity;
import com.gewara.model.api.ApiUser;
import com.gewara.model.bbs.Diary;
import com.gewara.model.content.Picture;
import com.gewara.model.movie.Cinema;
import com.gewara.model.movie.Movie;
import com.gewara.model.movie.MovieVideo;
import com.gewara.model.partner.PartnerCloseRule;
import com.gewara.service.bbs.BlogService;
import com.gewara.service.bbs.DiaryService;
import com.gewara.service.content.VideoService;
import com.gewara.service.movie.MCPService;
import com.gewara.service.order.OrderQueryService;
import com.gewara.service.partner.PartnerService;
import com.gewara.support.ServiceHelper;
import com.gewara.util.BeanUtil;
import com.gewara.web.action.api.ApiAuth;
import com.gewara.web.action.api.BaseApiController;
import com.gewara.web.filter.NewApiAuthenticationFilter;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

/**
 * 电影API
 * @author taiqichao
 *
 */
@Controller
public class Api2MovieController extends BaseApiController{
	@Autowired@Qualifier("mcpService")
	private MCPService mcpService;
	
	@Autowired@Qualifier("partnerService")
	private PartnerService partnerService;
	
	@Autowired@Qualifier("diaryService")
	private DiaryService diaryService;
	
	@Autowired@Qualifier("blogService")
	private BlogService blogService;
	
	@Autowired@Qualifier("videoService")
	private VideoService videoService;
	
	@Autowired@Qualifier("orderQueryService")
	private OrderQueryService orderQueryService;
	
	/**
	 * 电影详情
	 * @param movieid
	 * @param model
	 * @return
	 */
	@RequestMapping("/api2/movie/movieDetail.xhtml")
	public String movie(Long movieid, ModelMap model){
		if(movieid==null) return getErrorXmlView(model, ApiConstant.CODE_PARAM_ERROR, "传递参数错误！");
		Movie movie = daoService.getObject(Movie.class, movieid);
		if(movie == null) return getErrorXmlView(model, ApiConstant.CODE_DATA_ERROR, "没有查询信息！");
		model.put("movie", movie);
		return getXmlView(model, "api/info/movie/movie.vm");
	}
	
	
	/**
	 * 电影详情列表
	 * @param movieids
	 * @param model
	 * @return
	 */
	@RequestMapping("/api2/movie/movieListByIds.xhtml")
	public String movieListByIds(String movieids, ModelMap model){
		if(movieids==null) return getErrorXmlView(model, ApiConstant.CODE_PARAM_ERROR, "传递参数错误！");
		List<Long> movieidList = BeanUtil.getIdList(movieids, ",");
		List<Movie> movieList = daoService.getObjectList(Movie.class, movieidList);
		model.put("movieList", movieList);
		return getXmlView(model, "api/info/movie/movieListByIds.vm");
	}
	
	/**
	 * 即将上映影片列表
	 * @param request
	 * @param from
	 * @param maxnum
	 * @param model
	 * @return
	 */
	@RequestMapping("/api2/movie/futureMovieList.xhtml")
	public String futureMovieList(HttpServletRequest request, Integer from, Integer maxnum, ModelMap model){
		ApiAuth auth = NewApiAuthenticationFilter.getApiAuth();
		if(from == null) from=0;
		if(maxnum == null) maxnum=100;
		List<Movie>  mpiList = mcpService.getFutureMovieList(from, maxnum,null);
		List<PartnerCloseRule> pcrList = partnerService.getCloseRuleList();
		CloseRuleOpiFilter filter = new CloseRuleOpiFilter(auth.getApiUser(), pcrList);
		filter.filterMovie(mpiList);
		model.put("mpiList", mpiList);
		return getXmlView(model, "api/info/movie/futureMovieList.vm");
	}
	
	/**
	 * 影片播放影院
	 * @param request
	 * @param from
	 * @param maxnum
	 * @param movieid
	 * @param playdate
	 * @param citycode
	 * @param model
	 * @return
	 */
	@RequestMapping("/api2/movie/playCinemaList.xhtml")
	public String playCinemaList(HttpServletRequest request, Integer from, Integer maxnum, Long movieid, Date playdate, String citycode, ModelMap model){
		ApiAuth auth = NewApiAuthenticationFilter.getApiAuth();
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
	 * @param tradeNo
	 * @param model
	 * @param request
	 * @return
	 */
	@RequestMapping("/api2/movie/getTicketValidTime.xhtml")
	public String getTicketHelp(String tradeNo, ModelMap model,HttpServletRequest request){
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
	 * 获取影片长影评
	 * @param mid 影片id
	 * @param citycode 城市代码
	 * @param order 排序,可选值:poohnum,addtime
	 * @param pageNo 当前页码
	 * @param model 
	 * @param request
	 * @return
	 */
	@RequestMapping("/api2/movie/movieDiaryList.xhtml")
	public String movieDiaryList(
			@RequestParam(required=true,value="mid")Long mid,
			String citycode,
			@RequestParam(defaultValue="poohnum",required=false,value="order") String order, 
			@RequestParam(defaultValue="0",required=false,value="from")Integer from,
			@RequestParam(defaultValue="20",required=false,value="maxnum")Integer maxnum, String yfrom, 
			ModelMap model, HttpServletRequest request) {
		if(maxnum>100){
			maxnum=100;
		}
		int first = maxnum * from;
		if(StringUtils.equals(yfrom, Status.Y)){
			first = from;
		}
		List<Diary> diaryList = diaryService.getDiaryList(Diary.class, citycode, DiaryConstant.DIARY_TYPE_COMMENT, "movie", mid, first, maxnum, order);
		Map<Long, String> bodyMap=new HashMap<Long, String>();
		for (Diary diary : diaryList) {
			String diaryBody = blogService.getDiaryBody(diary.getId());
			//图片固定尺寸
			diaryBody = diaryBody.replace("style", "css");
			diaryBody = diaryBody.replace("href", "link");
			diaryBody = diaryBody.replaceAll("<img[^>]+src=\"([^\"]+)\"([^>]+)>","<img src=\"$1?w=300&h=300\" />");
			bodyMap.put(diary.getId(), diaryBody);
		}
		addCacheMember(model, ServiceHelper.getMemberIdListFromBeanList(diaryList));
		model.put("diaryList", diaryList);
		model.put("bodyMap", bodyMap);
		return getXmlView(model,"api2/movie/movie/movieDiaryList.vm");
	}
	
	/**
	 * 手机客户端活动列表
	 * @param apptype
	 * @param osType
	 * @param citycode
	 * @param from
	 * @param maxnum
	 * @param model
	 * @return
	 */
	@RequestMapping("/api2/common/phoneActivityList.xhtml")
	public String phoneActivityList(String apptype,String osType,String citycode,
			int from,int maxnum,ModelMap model){
		if(!(PhoneActivity.OS_TYPE_ANDROID.equals(osType) || PhoneActivity.OS_TYPE_IPHONE.equals(osType))){
			return getErrorXmlView(model, ApiConstant.CODE_SIGN_ERROR, "ostype 应为ANDROID或IPHONE");
		}
		if(StringUtils.isBlank(citycode)){
			citycode = AdminCityContant.CITYCODE_ALL;
		}
		DBObject params = new BasicDBObject();
		DBObject inparams = new BasicDBObject();
		inparams.put("$in", new String[]{osType, PhoneActivity.OS_TYPE_ALL});
		params.put("status", PhoneActivity.STATUS_NEW);
		params.put("apptype", apptype);
		params.put("ostype", inparams);
		Pattern pattern = Pattern.compile(citycode,Pattern.CASE_INSENSITIVE);
		params.put("citycode", pattern);
		if(maxnum > 20){
			maxnum = 20;
		}
		List<PhoneActivity> phoneActivityList = mongoService.getObjectList(PhoneActivity.class, params,"rank",true,from,maxnum);
		model.put("phoneActivityList", phoneActivityList);
		return getXmlView(model,"api/mobile/phoneActivityList.vm");
	}
	@RequestMapping("/api2/common/pictureList.xhtml")
	public String pictureList(String tag,Long relatedid,
			int from,int maxnum, ModelMap model){
		if(StringUtils.isBlank(tag)) return getErrorXmlView(model, ApiConstant.CODE_SIGN_ERROR, "tag不能为空！");
		if(relatedid == null) return getErrorXmlView(model, ApiConstant.CODE_SIGN_ERROR, "relateid不能为空！");
		if(maxnum > 20) maxnum = 20;
		List<Picture> picList = pictureService.getPictureListByRelatedid(tag, relatedid, from, maxnum);
		model.put("picList", picList);
		return getXmlView(model, "api2/mobile/pictureList.vm");
	}
	

	@RequestMapping("/api2/movie/movieVideoList.xhtml")
	public String getTicketHelp(Long movieid, /*int from, int maxnum, */ModelMap model){
		if(movieid==null) return getErrorXmlView(model, ApiConstant.CODE_SIGN_ERROR, "movieid不能为空！");
		//FIXME:错误的逻辑
		List<MovieVideo> videoList = Arrays.asList(videoService.getMovieVideo(movieid));
		model.put("videoList", videoList);
		return getXmlView(model, "api2/mobile/movieVideoList.vm");
	}
}
