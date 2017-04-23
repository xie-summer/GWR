package com.gewara.web.action.movie;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.util.ClassUtils;
import org.springframework.web.bind.annotation.RequestMapping;

import com.gewara.constant.DiaryConstant;
import com.gewara.constant.TagConstant;
import com.gewara.json.PageView;
import com.gewara.json.bbs.MarkCountData;
import com.gewara.model.bbs.Diary;
import com.gewara.model.movie.CityPrice;
import com.gewara.model.movie.Movie;
import com.gewara.service.bbs.DiaryService;
import com.gewara.service.bbs.MarkService;
import com.gewara.service.movie.MCPService;
import com.gewara.service.ticket.MoviePriceService;
import com.gewara.service.ticket.OpenPlayService;
import com.gewara.support.ErrorCode;
import com.gewara.support.MultiPropertyComparator;
import com.gewara.untrans.NosqlService;
import com.gewara.untrans.PageCacheService;
import com.gewara.untrans.PageParams;
import com.gewara.untrans.activity.SynchActivityService;
import com.gewara.untrans.order.impl.SpdiscountService;
import com.gewara.util.BeanUtil;
import com.gewara.util.VmUtils;
import com.gewara.util.WebUtils;
import com.gewara.web.action.AnnotationController;
import com.gewara.web.util.PageUtil;
import com.gewara.web.util.QueryStrBuilder;
import com.gewara.xmlbind.activity.RemoteActivity;

@Controller
public class NewSearchMovieController extends AnnotationController {
	@Autowired@Qualifier("markService")
	private MarkService markService;
	public void setMarkService(MarkService markService) {
		this.markService = markService;
	}
	@Autowired@Qualifier("pageCacheService")
	private PageCacheService pageCacheService;
	public void setPageCacheService(PageCacheService pageCacheService) {
		this.pageCacheService = pageCacheService;
	}
	
	@Autowired@Qualifier("diaryService")
	private DiaryService diaryService;
	public void setDiaryService(DiaryService diaryService) {
		this.diaryService = diaryService;
	}
	@Autowired@Qualifier("synchActivityService")
	private SynchActivityService synchActivityService;
	public void setActivityRemoteService(SynchActivityService synchActivityService) {
		this.synchActivityService = synchActivityService;
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
	
	@Autowired@Qualifier("spdiscountService")
	protected SpdiscountService spdiscountService;
	
	@Autowired@Qualifier("nosqlService")
	private NosqlService nosqlService;
	@Autowired@Qualifier("moviePriceService")
	private MoviePriceService moviePriceService;
	private List<String> typeList = Arrays.asList("动作","喜剧","爱情","科幻","魔幻","灾难","恐怖","纪录","犯罪","战争","冒险","剧情","其他");
	private int rowsPerpage = 10;//分页
	@RequestMapping("/movie/searchMovie.xhtml")
	public String newSearchMovie(HttpServletRequest request, HttpServletResponse response, Long movieid,
			String type, String order,String keyW, Integer pageNo, ModelMap model) {
		String citycode = WebUtils.getAndSetDefault(request, response);
		if (movieid != null) {
			return showRedirect("/movie/" + movieid, model);
		}
		if(pageNo==null) pageNo = 0;
		int from = pageNo * rowsPerpage;
		//1、验证参数
		
		PageParams pparams = new PageParams();
		pparams.addInteger("pageNo", pageNo);
		if(StringUtils.isNotBlank(type) && typeList.contains(type)){
			pparams.addSingleString("type", type);
		}else{
			type="";
		}
		if(ClassUtils.hasMethod(Movie.class, "get" + StringUtils.capitalize(order))){
			pparams.addSingleString("order", order);
		}else{
			order = "";
		}
		
		if(StringUtils.isBlank(keyW) && pageCacheService.isUseCache(request)){
			PageView pageView = pageCacheService.getPageView(request, "movie/searchMovie.xhtml", pparams, citycode);
			if(pageView!=null){
				model.put("pageView", pageView);
				return "pageView.vm";
			}
		}
		//2、影片数据
		List<Movie> movieList = mcpService.getCurMovieList(citycode);
		if(StringUtils.isNotBlank(type) || StringUtils.isNotBlank(keyW)){//过滤类型
			Iterator<Movie> iterator = movieList.iterator();
			while(iterator.hasNext()){
				Movie movie = iterator.next();
				if(StringUtils.isNotBlank(type) && !StringUtils.contains(movie.getType(), type)){
					iterator.remove();
				}else if(StringUtils.isNotBlank(keyW) && !StringUtils.contains(movie.getName(), keyW)){
					iterator.remove();
				}
			}
		}
		if (StringUtils.isNotBlank(order)) {
			Collections.sort(movieList, new MultiPropertyComparator(new String[]{order}, new boolean[]{false}));
		}else{//按排片数
			mcpService.sortMoviesByMpiCount(citycode, movieList);
		}
		int count = movieList.size();
		model.put("count", count);
		movieList = BeanUtil.getSubList(movieList, from, rowsPerpage);

		//3、关联数据
		Map<Long, MarkCountData> markCountMap = new HashMap<Long, MarkCountData>();
		Map<Long, CityPrice> cityPriceMap = new HashMap<Long, CityPrice>();
		Map markData = markService.getMarkdata(TagConstant.TAG_MOVIE);
		final Map<Long, Integer> movieMark = new HashMap<Long, Integer>();
		model.put("markData",markData);
		for (Movie movie : movieList) {
			Long mid = movie.getId();
			if(markCountMap.get(mid) == null){
				markCountMap.put(mid, markService.getMarkCountByTagRelatedid(TagConstant.TAG_MOVIE, mid));
			}
			if(StringUtils.equals(order, "avggeneral")){
				movieMark.put(movie.getId(), VmUtils.getLastMarkStar(movie, "general", markCountMap.get(mid), markData));
			}
			CityPrice cityPrice = moviePriceService.getCityPrice(mid, citycode, TagConstant.TAG_MOVIE);
			if(cityPrice != null){
				cityPriceMap.put(mid,cityPrice);//
			}
		}
		if(StringUtils.equals(order, "avggeneral")){
			Collections.sort(movieList, new Comparator<Movie>() {      
	            public int compare(Movie o1, Movie o2) {      
	                return movieMark.get(o2.getId()) - movieMark.get(o1.getId());      
	            }      
	        }); 
		}
		model.put("cityPriceMap", cityPriceMap);
		model.put("movieList", movieList);
		PageUtil pageUtil = new PageUtil(count, rowsPerpage, pageNo, "movie/searchMovie.xhtml", true, true);
		pageUtil.initPageInfo(pparams.getParams());
		model.put("pageUtil", pageUtil);
		
		//热门活动
		ErrorCode<List<RemoteActivity>> result = synchActivityService.getActivityListByOrder(citycode, null, RemoteActivity.TIME_CURRENT, TagConstant.TAG_CINEMA, null, null, null, "duetime", 0, 4);
		if(result.isSuccess()) model.put("activityList", result.getRetval());
		
		// 最新热片影评推荐
		List<Diary> recommendDiaryList = diaryService.getHotCommentDiary(citycode, DiaryConstant.DIARY_TYPE_COMMENT, TagConstant.TAG_CINEMA, null, 0, 5);
		Map<Long, Movie> moveMap = new HashMap<Long, Movie>();
		for(Diary diary : recommendDiaryList){
			Movie movie = daoService.getObject(Movie.class, diary.getCategoryid());
			moveMap.put(diary.getId(), movie);
			addCacheMember(model, diary.getMemberid());
		}
		model.put("recommendDiaryList", recommendDiaryList);
		model.put("moveMap", moveMap);
		List<Long> openMovieIdList = openPlayService.getOpiMovieidList(citycode, null);
		model.put("opiMovieList", openMovieIdList);
		model.put("curMovieListCount", openMovieIdList.size());
		model.put("rowsCount", count);
		//评分统计
		model.put("curMarkCountMap", markCountMap);
		QueryStrBuilder qb = new QueryStrBuilder();
		qb.buildQueryStr(request.getParameterMap(), "order,type".split(","));
		model.put("qb", qb);
		getHotSaleList(model);
		model.put("cinemaCount",openPlayService.getOpiCinemaidList(citycode, null).size());
		model.put("fetureMovieCount",mcpService.getFutureMovieList(0, 200, null).size());
		model.put("activityCount",synchActivityService.getActivityCount(citycode, null,RemoteActivity.TIME_CURRENT, TagConstant.TAG_CINEMA, null).getRetval());
		model.put("searchMovie",true);
		model.put("movieIdList", new ArrayList<Long>());
		return "movie/wide_hotMovieList.vm";
	}
	
	private void getHotSaleList(ModelMap model){
		Map<Long, MarkCountData> markCountMap = (Map<Long, MarkCountData>)model.get("curMarkCountMap");
		List<Map> saleMovie = nosqlService.getBuyTicketRanking();
		List<Movie> saleMovieList = new LinkedList<Movie>();
		Map<Long,Map> saleMovieMap = new HashMap<Long,Map>();
		for(Map map : saleMovie){
			Movie movie = this.daoService.getObject(Movie.class,(Long)map.get("movieId"));
			saleMovieList.add(movie);
			saleMovieMap.put(movie.getId(),map);
			if(markCountMap.get(movie.getId()) == null){
				markCountMap.put(movie.getId(), markService.getMarkCountByTagRelatedid(TagConstant.TAG_MOVIE,movie.getId()));
			}
		}
		model.put("saleMovieList", BeanUtil.getSubList(saleMovieList, 0, 5));
		model.put("saleMovieMap", saleMovieMap);
		model.put("markCountMap", markCountMap);
	}
}
