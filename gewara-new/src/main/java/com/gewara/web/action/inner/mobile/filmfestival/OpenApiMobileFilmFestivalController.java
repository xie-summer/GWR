package com.gewara.web.action.inner.mobile.filmfestival;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import com.gewara.Config;
import com.gewara.constant.AdminCityContant;
import com.gewara.constant.ApiConstant;
import com.gewara.constant.TagConstant;
import com.gewara.constant.content.FilmFestConstant;
import com.gewara.constant.content.SignName;
import com.gewara.constant.sys.JsonDataKey;
import com.gewara.constant.sys.MongoData;
import com.gewara.constant.ticket.OpiConstant;
import com.gewara.helper.api.GewaApiMovieHelper;
import com.gewara.helper.ticket.CloseRuleOpiFilter;
import com.gewara.helper.ticket.OpiFilter;
import com.gewara.json.ViewFilmSchedule;
import com.gewara.model.api.ApiUser;
import com.gewara.model.common.JsonData;
import com.gewara.model.content.GewaCommend;
import com.gewara.model.content.News;
import com.gewara.model.movie.Cinema;
import com.gewara.model.movie.CityPrice;
import com.gewara.model.movie.Movie;
import com.gewara.model.movie.MoviePlayItem;
import com.gewara.model.movie.SpecialActivity;
import com.gewara.model.partner.PartnerCloseRule;
import com.gewara.model.ticket.OpenPlayItem;
import com.gewara.model.user.Member;
import com.gewara.service.movie.FilmFestService;
import com.gewara.service.ticket.MoviePriceService;
import com.gewara.support.MultiPropertyComparator;
import com.gewara.untrans.CommonService;
import com.gewara.util.BeanUtil;
import com.gewara.util.HttpResult;
import com.gewara.util.HttpUtils;
import com.gewara.util.VmUtils;
import com.gewara.web.action.inner.mobile.BaseOpenApiMobileMovieController;
import com.gewara.web.filter.OpenApiMobileAuthenticationFilter;
/**
 * @author gang.liu
 *
 */
@Controller
public class OpenApiMobileFilmFestivalController extends
	BaseOpenApiMobileMovieController {
	
	@Autowired@Qualifier("filmFestService")
	private FilmFestService filmFestService;
	public void setFilmFestService(FilmFestService filmFestService) {
		this.filmFestService = filmFestService;
	}
	@Autowired@Qualifier("commonService")
	private CommonService commonService;
	public void setCommonService(CommonService commonService) {
		this.commonService = commonService;
	}
	@Autowired@Qualifier("moviePriceService")
	private MoviePriceService moviePriceService;
	
	private String proxy = "http://test.gewala.net";
	public boolean usproxy(){
		JsonData jd = daoService.getObject(JsonData.class, JsonDataKey.KEY_SIFFPROXY);
		String useProxy = (jd==null?"Y":VmUtils.getJsonValueByKey(jd.getData(), "useproxy"));
		return StringUtils.equals("Y", useProxy) && Config.getServerIp().startsWith("172.22");
	}
	
	@SuppressWarnings("deprecation")
	public String sendToProxy(String url, HttpServletRequest request, ModelMap model){
		Map<String, String> params = new HashMap<String, String>();
		Enumeration pnList = request.getParameterNames();
		while(pnList.hasMoreElements()){
			String pn = (String) pnList.nextElement();
			params.put(pn, request.getParameter(pn));
		}
		HttpResult result = HttpUtils.postUrlAsString(proxy + url, params);
		return getDirectXmlView(model, result.getResponse());
	}
	
	/**
	 * 电影节动态列表
	 */
	@RequestMapping("/openapi/mobile/filmfest/newsList.xhtml")
	public String filmFestNews(int from,int maxnum,ModelMap model,HttpServletRequest request){
		//if(usproxy()) return sendToProxy("/openapi/mobile/filmfest/newsList.xhtml", request, model);
		if(maxnum >100) maxnum = 100; 
		List<News> newsList = filmFestService.getFilmFestNewsList(getFestCitycode(), FilmFestConstant.TAG_FILMFEST_16 + "xc",null,null, from, maxnum);
		model.put("newsList", newsList);
		getNewsListMap(newsList,model,request);
		return getOpenApiXmlList(model);
	}
	
	/**
	 * 电影环节
	 */
	@RequestMapping("/openapi/mobile/filmfest/movieLinkList.xhtml")
	public String movieLink(ModelMap model,HttpServletRequest request){
		//if(usproxy()) return sendToProxy("/openapi/mobile/filmfest/movieLinkList.xhtml", request, model);
		//获取父节点
		List<GewaCommend> gcList = commonService.getGewaCommendListByParentid(SignName.FILM_MOVIE_LINK, 0l,true);
		Map<Long,List<GewaCommend>> childrenGcMap = new HashMap<Long, List<GewaCommend>>();
		if(!gcList.isEmpty())
		for (GewaCommend gc : gcList) {
			List<GewaCommend> childrenMovieLink = commonService.getGewaCommendListByParentid(SignName.FILM_MOVIE_LINK, gc.getId(),true);
			childrenGcMap.put(gc.getId(), childrenMovieLink);
		}
		model.put("movieLinkList", gcList);
		model.put("childrenMovieLinkMap", childrenGcMap);
		initField(model, request);
		return getXmlView(model,"inner/filmFestval/movieLinkList.vm");
	}
	
	/**
	 * 电影列表
	 */
	@RequestMapping("/openapi/mobile/filmfest/movieList.xhtml")
	public String movieList(String type,String value,int from,int maxnum,String memberEncode,ModelMap model,HttpServletRequest request){
		List<Movie> movieList = null;
		if(maxnum > 20){
			maxnum = 20;
		}
		SpecialActivity sa = filmFestService.getSpecialActivity(FilmFestConstant.TAG_FILMFEST_16);
		if(StringUtils.isNotBlank(type) && StringUtils.isNotBlank(value)){
			//TODO:有问题，老的方法有错误？？？
			movieList = filmFestService.getFilmFestMovie(FilmFestConstant.TAG_FILMFEST_16, type, value, from, maxnum);
		}else{
			List<GewaCommend> gcList = commonService.getGewaCommendList(AdminCityContant.CITYCODE_SH, "sp"+sa.getId(), null, "movie", true, from, maxnum);
			movieList = daoService.getObjectList(Movie.class,BeanUtil.getBeanPropertyList(gcList, Long.class, "relatedid", true));
		}
		List<Map<String, Object>> resMapList = new ArrayList<Map<String, Object>>();
		List<Long> movieIds = null;
		if(StringUtils.isNotBlank(memberEncode)){
			Member member = OpenApiMobileAuthenticationFilter.getOpenApiAuth().getMember();
			if(member != null){
				Map qryParams = new HashMap();
				qryParams.put("type", ViewFilmSchedule.TYPE_MOVIE_FILMFEST);
				qryParams.put("memberId", member.getId());
				List<ViewFilmSchedule> mvs = this.mongoService.getObjectList(ViewFilmSchedule.class, qryParams, "addTime", false, 0, 500);
				movieIds = BeanUtil.getBeanPropertyList(mvs, Long.class, "movieId", true);
			}
		}
		List<Long> movieIdList = filmFestService.getMPIMovieIds(getFestCitycode(), sa.getId());
		for(Movie movie : movieList){
			Map<String, Object> params = getMovieData(movie);
			params.put("reletedSchedule", (movieIds != null && movieIds.contains(movie.getId())) ? 1 : 0);
			params.put("booking", movieIdList.contains(movie.getId()) ? 1 : 0);
			resMapList.add(params);
		}
		model.put("movieList", movieList);
		initField(model, request);
		putMovieListNode(model);
		model.put("resMapList", resMapList);
		return getOpenApiXmlList(model);
	}
	
	private List<Long> getSchedule(String memberEncode){
		List<Long> mpIds = null;
		if(StringUtils.isNotBlank(memberEncode)){
			Member member = OpenApiMobileAuthenticationFilter.getOpenApiAuth().getMember();
			if(member != null){
				Map qryParams = new HashMap();
				qryParams.put("type", ViewFilmSchedule.TYPE_SCHEDULE_FILMFEST);
				qryParams.put("memberId", member.getId());
				List<ViewFilmSchedule> mvs = this.mongoService.getObjectList(ViewFilmSchedule.class, qryParams, "addTime", false, 0, 500);
				mpIds = BeanUtil.getBeanPropertyList(mvs, Long.class, "mpid", true);
			}
		}
		return mpIds;
	}
	
	/**
	 * 电影场次信息
	 */
	@RequestMapping("/openapi/mobile/filmfest/moviePlayTable.xhtml")
	public String moviePlayTable(Long movieid,ModelMap model,String memberEncode, HttpServletRequest request){
		SpecialActivity sa = filmFestService.getSpecialActivity(FilmFestConstant.TAG_FILMFEST_16);
		List<Map<String, Object>> resMapList = new ArrayList<Map<String, Object>>();
		List<Long> mpIds = this.getSchedule(memberEncode);
		if(sa != null){
			String query = "from MoviePlayItem mp where mp.batch = ? and mp.movieid = ? and mp.playdate >= ? and mp.citycode = ? order by mp.playdate asc,mp.playtime asc";
			List<MoviePlayItem> mpiList = hibernateTemplate.find(query,sa.getId(),movieid,new Date(), getFestCitycode());
			if(!mpiList.isEmpty()){
				ApiUser partner = OpenApiMobileAuthenticationFilter.getOpenApiAuth().getApiUser();
				mpiList = MoviePlayItem.getCurrent(new Date(), mpiList);
				Map<Long/*cinemaId*/,List<Map<String,Object>>> mpiMapByCinema = new HashMap<Long,List<Map<String,Object>>>();
				List<PartnerCloseRule> pcrList = partnerService.getCloseRuleList();
				OpiFilter filter = new CloseRuleOpiFilter(partner, pcrList);
				for(MoviePlayItem mpi: mpiList){
					if(mpi.isUnShowToGewa()) continue;
					OpenPlayItem opi = daoService.getObjectByUkey(OpenPlayItem.class, "mpid", mpi.getId(), true);
					Map<String, Object> resMap = GewaApiMovieHelper.getMpiData(mpi);
					resMap.put("booking", (opi!=null && opi.isOrder() && !filter.excludeOpi(opi)) ? 1 : 0);//添加可订票标识
					List<Map<String,Object>> list = null;
					if(mpiMapByCinema.get(mpi.getCinemaid()) != null){
						list = mpiMapByCinema.get(mpi.getCinemaid());
					}else{
						list = new LinkedList<Map<String,Object>>();
						mpiMapByCinema.put(mpi.getCinemaid(),list);
					}
					resMap.put("reletedSchedule", (mpIds != null && mpIds.contains(mpi.getId())) ? 1 : 0);
					list.add(resMap);
				}
				List<Cinema> cinemaList = daoService.getObjectList(Cinema.class, mpiMapByCinema.keySet());
				Map<Long/*cinemaid*/,Map<Date,List<Map<String,Object>>>> cinemaDateMap = new HashMap<Long, Map<Date,List<Map<String,Object>>>>();
				filter.filterCinema(cinemaList);
				for (Cinema cinema : cinemaList) {
					Map<String, Object> cinemaResMap = getCinemaData(cinema);
					resMapList.add(cinemaResMap);
					List<Map<String,Object>> mpiTempList = mpiMapByCinema.get(cinema.getId());
					Map<Date,List<Map<String,Object>>> cinemaMpiDateMap = BeanUtil.groupBeanList(mpiTempList, "playdate");
					cinemaDateMap.put(cinema.getId(), new TreeMap<Date, List<Map<String,Object>>>(cinemaMpiDateMap));
				}
				model.put("cinemaDateMap", cinemaDateMap);
			}
		}
		initField(model, request);
		model.put("resMapList", resMapList);
		return getXmlView(model, "inner/filmFestval/moviePlayTable.vm");
	}
	
	public static final String CINEMA_TYPE_AROUND = "around";//周边影院
	public static final String CINEMA_TYPE_COUNTY = "county";//按区域查询
	/**
	 * 影院列表（周边影院，按区域查询影院）
	 */
	@RequestMapping("/openapi/mobile/filmfest/cinemaList.xhtml")
	public String cinemaList(String type,final Double pointx,final Double pointy,String countycode,HttpServletRequest request,ModelMap model){
		List<Cinema> cinemaList = null;
		List<Map<String, Object>> resMapList = new ArrayList<Map<String, Object>>();
		if(CINEMA_TYPE_AROUND.equals(type)){
			if(pointx == null ||  pointy == null) return getErrorXmlView(model, ApiConstant.CODE_PARAM_ERROR, "pointx、pointy为必须参数，不能为空！");
			if(pointx > 120.51 && pointx <122.12 && pointy>30.40 && pointy <31.53){
				//FilmFestConstant.TAG_FILMFEST_16
				cinemaList = mcpService.getNearCinemaList(pointx, pointy, 5000, null, getFestCitycode(), null);
			}else{
				cinemaList = hibernateTemplate.findByCriteria(getCinemaListByCountyCode(null));
			}
		}else if(CINEMA_TYPE_COUNTY.equals(type)){
			cinemaList = hibernateTemplate.findByCriteria(getCinemaListByCountyCode(countycode));
		}
		ApiUser partner = OpenApiMobileAuthenticationFilter.getOpenApiAuth().getApiUser();
		List<PartnerCloseRule> pcrList = partnerService.getCloseRuleList();
		CloseRuleOpiFilter filter = new CloseRuleOpiFilter(partner, pcrList);
		filter.filterCinema(cinemaList);
		model.put("cinemaList", cinemaList);
		for(Cinema cinema : cinemaList){
			Map<String, Object> params = getCinemaData(cinema);
			resMapList.add(params);
		}
		initField(model, request);
		putCinemaListNode(model);
		model.put("resMapList", resMapList);
		return getOpenApiXmlList(model);
	}
	
	private DetachedCriteria getCinemaListByCountyCode(String countycode){
		DetachedCriteria query = DetachedCriteria.forClass(Cinema.class);
		query.add(Restrictions.like("flag",FilmFestConstant.TAG_FILMFEST_16,MatchMode.ANYWHERE));
		query.add(Restrictions.eq("citycode", getFestCitycode()));
		if(StringUtils.isNotBlank(countycode)) query.add(Restrictions.eq("countycode", countycode));
		query.addOrder(Order.asc("flag"));
		return query;
	}
	
	@RequestMapping("/openapi/mobile/filmfest/cinemaPlayTable.xhtml")
	public String cinemaPlayTable(Long cinemaid,ModelMap model,String memberEncode,HttpServletRequest request){
		SpecialActivity sa = filmFestService.getSpecialActivity(FilmFestConstant.TAG_FILMFEST_16);
		List<Map<String, Object>> resMapList = new ArrayList<Map<String, Object>>();
		List<Long> mpIds = this.getSchedule(memberEncode);
		if(sa != null){
			String query = "from MoviePlayItem mp where mp.batch = ? and mp.cinemaid = ? and mp.playdate >= ? and mp.citycode = ? order by mp.playtime asc";
			List<MoviePlayItem> mpiList = hibernateTemplate.find(query,sa.getId(),cinemaid,new Date(), getFestCitycode());
			if(!mpiList.isEmpty()){
				ApiUser partner = OpenApiMobileAuthenticationFilter.getOpenApiAuth().getApiUser();
				List<PartnerCloseRule> pcrList = partnerService.getCloseRuleList();
				OpiFilter filter = new CloseRuleOpiFilter(partner, pcrList);
				mpiList = MoviePlayItem.getCurrent(new Date(), mpiList);
				Map<Long/*movieid*/,List<Map<String,Object>>> mpiMapByMovie = new HashMap<Long,List<Map<String,Object>>>();
				for(MoviePlayItem mpi: mpiList){
					if(mpi.isUnShowToGewa()) continue;
					OpenPlayItem opi = daoService.getObjectByUkey(OpenPlayItem.class, "mpid", mpi.getId(), true);
					Map<String, Object> resMap = GewaApiMovieHelper.getMpiData(mpi);
					resMap.put("booking", (opi!=null && opi.isOrder() && !filter.excludeOpi(opi)) ? 1 : 0);//添加可订票标识
					List<Map<String,Object>> list = null;
					if(mpiMapByMovie.get(mpi.getMovieid()) != null){
						list = mpiMapByMovie.get(mpi.getMovieid());
					}else{
						list = new LinkedList<Map<String,Object>>();
						mpiMapByMovie.put(mpi.getMovieid(),list);
					}
					resMap.put("reletedSchedule", (mpIds != null && mpIds.contains(mpi.getId())) ? 1 : 0);
					list.add(resMap);
				}
				List<Movie> movieList = daoService.getObjectList(Movie.class, mpiMapByMovie.keySet());
				Map<Long/*movie*/,Map<Date,List<Map<String, Object>>>> movieDateMap = new HashMap<Long, Map<Date,List<Map<String, Object>>>>();
				for (Movie movie : movieList) {
					Map<String, Object> movieResMap = getMovieData(movie);
					movieResMap.put("smalllogo", movie.getLogo());
					resMapList.add(movieResMap);
					List<Map<String, Object>> mpiTempList = mpiMapByMovie.get(movie.getId());
					Map<Date,List<Map<String, Object>>> cinemaMpiDateMap = BeanUtil.groupBeanList(mpiTempList, "playdate");
					movieDateMap.put(movie.getId(), new TreeMap<Date, List<Map<String, Object>>>(cinemaMpiDateMap));
				}
				model.put("movieDateMap", movieDateMap);
			}
		}
		initField(model, request);
		model.put("resMapList", resMapList);
		return getXmlView(model, "inner/filmFestval/cinemaPlayTable.vm");
	}
	
	@RequestMapping("/openapi/mobile/filmfest/recommendPlayTable.xhtml")
	public String recommendPlayTable(int from,int maxnum,String memberEncode,ModelMap model,HttpServletRequest request){
		if(maxnum > 30){
			maxnum = 30;
		}
		SpecialActivity sa = filmFestService.getSpecialActivity(FilmFestConstant.TAG_FILMFEST_16);
		Map<Long, List<Map<String, Object>>> mpList = new HashMap<Long,List<Map<String, Object>>>();
		List<Long> mpIds = this.getSchedule(memberEncode);
		if(sa != null){
			List<GewaCommend> gcMpiList = commonService.getGewaCommendList(getFestCitycode(), "sp"+sa.getId(), null, "mpi", true, from, maxnum);
			List<Long> mpiIdList = BeanUtil.getBeanPropertyList(gcMpiList, Long.class, "relatedid", true);
			List<MoviePlayItem> topMpiList = daoService.getObjectList(MoviePlayItem.class, mpiIdList);
			ApiUser partner = OpenApiMobileAuthenticationFilter.getOpenApiAuth().getApiUser();
			List<PartnerCloseRule> pcrList = partnerService.getCloseRuleList();
			OpiFilter filter = new CloseRuleOpiFilter(partner, pcrList);
			List<Long> movieIds =   new ArrayList<Long>();
			for (MoviePlayItem mpi : topMpiList) {
				if(mpi.isUnShowToGewa()) continue;
				OpenPlayItem opi=daoService.getObjectByUkey(OpenPlayItem.class, "mpid", mpi.getId(), true);
				if(filter.excludeOpi(opi)){
					continue;
				}
				if(!movieIds.contains(mpi.getMovieid())){
					movieIds.add(mpi.getMovieid());
				}
				Map<String, Object> resMap = GewaApiMovieHelper.getMpiData(mpi);
				int seatStatus = 0;
				if(opi!=null && opi.getCostprice()!=null && !filter.excludeOpi(opi)) {
					resMap.put("unbookingReason",  OpiConstant.getUnbookingReason(opi));
					if(opi.isOrder()) seatStatus = 1;
					resMap.put("ticketstatus", opi.getSeatStatus());
					resMap.put("remark", opi.getRemark());
					resMap.put("cinemaname", opi.getCinemaname());
					resMap.put("moviename", opi.getMoviename());
				}else {
					resMap.put("cinemaname", daoService.getObject(Cinema.class, mpi.getCinemaid()).getName());
					resMap.put("unbookingReason",  "该场次未开放售票");
				}
				resMap.put("seatStatus", seatStatus);
				resMap.put("reletedSchedule", (mpIds != null && mpIds.contains(mpi.getId())) ? 1 : 0);
				if(mpList.get(mpi.getMovieid()) != null){
					mpList.get(mpi.getMovieid()).add(resMap);
				}else{
					List<Map<String, Object>> resMapList = new ArrayList<Map<String, Object>>();
					resMapList.add(resMap);
					mpList.put(mpi.getMovieid(),resMapList);
				}
			}
			List<Movie> movieList = daoService.getObjectList(Movie.class, movieIds);
			model.put("movieList", movieList);
			Map<Long, Map<String, Object>> movieMap = new HashMap<Long,Map<String, Object>>();
			for (Movie movie : movieList) {
				Map<String, Object> movieResMap = getMovieData(movie);
				movieMap.put(movie.getId(),movieResMap);
			}
			model.put("movieMap", movieMap);
		}
		model.put("mpList",mpList);
		initField(model, request);
		return getXmlView(model, "inner/filmFestval/recommendPlayTable.vm");
	}
	
	@RequestMapping("/openapi/mobile/filmfest/addViewFilmSchedule.xhtml")
	public String addViewFilmSchedule(Long mpid,String tag,Long movieId,String osType,ModelMap model){
		Map<String, Object> resMap = new HashMap<String, Object>();
		Member member = OpenApiMobileAuthenticationFilter.getOpenApiAuth().getMember();
		if(member==null){
			return getErrorXmlView(model, ApiConstant.CODE_MEMBER_NOT_EXISTS, "用户不存在！");
		}
		if(!(ViewFilmSchedule.TYPE_MOVIE_FILMFEST.equals(tag) || ViewFilmSchedule.TYPE_SCHEDULE_FILMFEST.equals(tag))){
			return getErrorXmlView(model, ApiConstant.CODE_MEMBER_NOT_EXISTS, "类型不正确！");
		}
		MoviePlayItem mpi = null;
		if(mpid != null){
			mpi = this.daoService.getObject(MoviePlayItem.class, mpid);
		}
		ViewFilmSchedule vfs = nosqlService.addViewFilmSchedule(mpi, tag, movieId,member.getId(),osType);
		if(vfs != null){
			resMap.put("result", true);
			resMap.put("msg", vfs.get_id());
		}else{
			return getErrorXmlView(model, ApiConstant.CODE_MEMBER_NOT_EXISTS, "已添加过！");
		}
		return this.getSuccessXmlView(model);
	}
	
	@RequestMapping("/openapi/mobile/filmfest/cancelViewFilmSchedule.xhtml")
	public String cancelViewFilmSchedule(String reletedId,ModelMap model){
		ViewFilmSchedule vfs = mongoService.getObject(ViewFilmSchedule.class, MongoData.SYSTEM_ID, reletedId);
		Member member = OpenApiMobileAuthenticationFilter.getOpenApiAuth().getMember();
		if(member==null){
			return getErrorXmlView(model, ApiConstant.CODE_MEMBER_NOT_EXISTS, "用户不存在！");
		}
		if(vfs == null){
			return getErrorXmlView(model, ApiConstant.CODE_MEMBER_NOT_EXISTS, "已添加过！");
		}
		if(!member.getId().equals(vfs.getMemberId())){
			return getErrorXmlView(model, ApiConstant.CODE_MEMBER_NOT_EXISTS, "不能操作其它人的！");
		}
		mongoService.removeObject(vfs, MongoData.SYSTEM_ID);
		return this.getSuccessXmlView(model);
	}
	
	@RequestMapping("/openapi/mobile/filmfest/viewFilmScheduleList.xhtml")
	public String viewFilmScheduleList(String tag,ModelMap model,HttpServletRequest request){
		Member member = OpenApiMobileAuthenticationFilter.getOpenApiAuth().getMember();
		List<Map<String, Object>> resMapList = new ArrayList<Map<String, Object>>();
		if(!(ViewFilmSchedule.TYPE_MOVIE_FILMFEST.equals(tag) || ViewFilmSchedule.TYPE_SCHEDULE_FILMFEST.equals(tag))){
			return getErrorXmlView(model, ApiConstant.CODE_MEMBER_NOT_EXISTS, "类型不正确！");
		}
		initField(model, request);
		Map params = new HashMap();
		params.put("memberId", member.getId());
		params.put("type", tag);
		List<ViewFilmSchedule> vsList = mongoService.getObjectList(ViewFilmSchedule.class, params, "addTime", false, 0,50);
		if(ViewFilmSchedule.TYPE_MOVIE_FILMFEST.equals(tag)){
			Map<Long,ViewFilmSchedule> movieIdList = BeanUtil.beanListToMap(vsList, "movieId");
			if(movieIdList != null && !movieIdList.isEmpty()){
				List<Long> openMovieIds = openPlayService.getOpiMovieidList(getFestCitycode(), null);
				List<Movie> movieList = daoService.getObjectList(Movie.class, movieIdList.keySet());
				for(Movie movie : movieList){
					Map<String, Object> resMap = getMovieData(movie);
					CityPrice cityPrice = moviePriceService.getCityPrice(movie.getId(), getFestCitycode(), TagConstant.TAG_MOVIE);
					if(cityPrice != null){
						resMap.put("playCinemas",cityPrice.getCquantity());//播放影片影院数量
						resMap.put("playItems",cityPrice.getQuantity());//影片排片数量
					}else{
						resMap.put("playCinemas",0);//播放影片影院数量
						resMap.put("playItems",0);//影片排片数量
					}
					resMap.put("booking", openMovieIds.contains(movie.getId()) ? 1 : 0);
					resMap.put("reletedId", movieIdList.get(movie.getId()).get_id());
					resMapList.add(resMap);
				}
				model.put("movieList", movieList);
			}
			putMovieListNode(model);
			model.put("resMapList", resMapList);
			return getOpenApiXmlList(model);
		}else{
			Map<Long,ViewFilmSchedule> mpIdList = BeanUtil.beanListToMap(vsList, "mpid");
			if(mpIdList != null && !mpIdList.isEmpty()){
				List<MoviePlayItem> mpiList = daoService.getObjectList(MoviePlayItem.class, mpIdList.keySet());
				Collections.sort(mpiList, new MultiPropertyComparator(new String[]{"playdate"}, new boolean[]{true}));
				ApiUser partner = OpenApiMobileAuthenticationFilter.getOpenApiAuth().getApiUser();
				List<PartnerCloseRule> pcrList = partnerService.getCloseRuleList();
				OpiFilter filter = new CloseRuleOpiFilter(partner, pcrList);
				for(MoviePlayItem mpi : mpiList){
					Map<String, Object> resMap = GewaApiMovieHelper.getMpiData(mpi);
					OpenPlayItem opi=daoService.getObjectByUkey(OpenPlayItem.class, "mpid", mpi.getId(), true);
					if(opi!=null && opi.isBooking() && !filter.excludeOpi(opi)) {
						resMap.put("booking", 1);
					}else{
						resMap.put("booking", 0);
					}
					Movie movie = daoService.getObject(Movie.class,mpi.getMovieid());
					resMap.put("length", movie.getLength());
					resMap.put("movieLogo", getMobilePath() + movie.getLogo());
					resMap.put("reletedId", mpIdList.get(mpi.getId()).get_id());
					resMap.put("cinemaname", daoService.getObject(Cinema.class,mpi.getCinemaid()).getBriefname());
					resMap.put("moviename", movie.getMoviename());
					resMapList.add(resMap);
				}
			}
			model.put("resMapList", resMapList);
			putPlayItemListNode(model);
			return getOpenApiXmlList(model);
		}
	}

	private String getFestCitycode(){
		return "310000";
	}

}
