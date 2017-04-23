package com.gewara.web.action.movie;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import com.gewara.constant.TagConstant;
import com.gewara.json.PageView;
import com.gewara.json.bbs.MarkCountData;
import com.gewara.model.movie.Movie;
import com.gewara.model.user.Treasure;
import com.gewara.service.bbs.MarkService;
import com.gewara.service.movie.MCPService;
import com.gewara.service.ticket.OpenPlayService;
import com.gewara.support.ErrorCode;
import com.gewara.untrans.CommonService;
import com.gewara.untrans.PageCacheService;
import com.gewara.untrans.PageParams;
import com.gewara.untrans.activity.SynchActivityService;
import com.gewara.util.BeanUtil;
import com.gewara.util.WebUtils;
import com.gewara.web.action.AnnotationController;
import com.gewara.web.util.PageUtil;
import com.gewara.web.util.QueryStrBuilder;
import com.gewara.xmlbind.activity.RemoteActivity;

@Controller
public class NewFutureMovieController extends AnnotationController {
	@Autowired@Qualifier("pageCacheService")
	private PageCacheService pageCacheService;
	public void setPageCacheService(PageCacheService pageCacheService) {
		this.pageCacheService = pageCacheService;
	}
	@Autowired
	@Qualifier("markService")
	private MarkService markService;

	public void setMarkService(MarkService markService) {
		this.markService = markService;
	}

	@Autowired
	@Qualifier("commonService")
	private CommonService commonService;

	public void setCommonService(CommonService commonService) {
		this.commonService = commonService;
	}

	@Autowired
	@Qualifier("mcpService")
	private MCPService mcpService;

	public void setMcpService(MCPService mcpService) {
		this.mcpService = mcpService;
	}

	@Autowired
	@Qualifier("openPlayService")
	private OpenPlayService openPlayService;
	
	@Autowired@Qualifier("synchActivityService")
	private SynchActivityService synchActivityService;
	
	public void setOpenPlayService(OpenPlayService openPlayService) {
		this.openPlayService = openPlayService;
	}

	private static List<String> orderList = Arrays.asList("clickedtimes", "releasedate");
	
	private List<String> typeList = Arrays.asList("动作","喜剧","爱情","科幻","魔幻","灾难","恐怖","纪录","犯罪","战争","冒险","剧情","其他");
	
	private Integer rowsPerPage = 10;
	@RequestMapping("/movie/futureMovie.xhtml")
	public String futureMovie(String keyW,String type,ModelMap model, HttpServletRequest request, HttpServletResponse response, 
			String order, Integer pageNo) {
		//1、参数
		String citycode = WebUtils.getAndSetDefault(request, response);
		if(pageNo==null) pageNo=0;
		int from = pageNo * rowsPerPage;
		
		if(StringUtils.isBlank(order) || !orderList.contains(order)){
			order = "";
		}
		PageParams queryMap = new PageParams();
		queryMap.addSingleString("order", order);
		queryMap.addSingleString("pageNo", "" + pageNo);
		queryMap.addSingleString("keyW",keyW);
		if(StringUtils.isNotBlank(type) && typeList.contains(type)){
			queryMap.addSingleString("type", type);
		}else{
			type="";
		}
		if(StringUtils.isBlank(keyW) && pageCacheService.isUseCache(request)){
			PageView pageView = pageCacheService.getPageView(request, "movie/futureMovie.xhtml", queryMap, citycode);
			if(pageView!=null){
				model.put("pageView", pageView);
				return "pageView.vm";
			}
		}		
		//2、查询影片
		Map<Long, MarkCountData> markCountMap = new HashMap<Long, MarkCountData>();
		List<Movie> movieList = mcpService.getFutureMovieList(0, 200, order);
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
		QueryStrBuilder qb = new QueryStrBuilder();
		qb.buildQueryStr(request.getParameterMap(), "order,type".split(","));
		model.put("qb", qb);
		model.put("fetureMovieCount",movieList.size());
		Integer searchrowsCount = movieList.size();
		model.put("searchrowsCount", searchrowsCount);
		movieList = BeanUtil.getSubList(movieList, from, rowsPerPage);
		PageUtil pageUtil = new PageUtil(searchrowsCount, rowsPerPage, pageNo, "/movie/futureMovie.xhtml", true, true);
		
		Map params = new HashMap();
		params.put("order", order);
		pageUtil.initPageInfo(params);
		model.put("pageUtil", pageUtil);
		if (searchrowsCount > 0) {
			model.put("movieList", movieList);
		}
		Map<String, Integer> videoCountMap = commonService.getVideoCount();// 预告片
		Map<String, Integer> pictureCountMap = commonService.getPictureCount();// 剧照
		model.put("videoCountMap", videoCountMap);
		model.put("pictureCountMap", pictureCountMap);

		List<Movie> hotMovieList = mcpService.getCurMovieListByMpiCount(citycode, 0, 5);
		for (Movie movie : hotMovieList) {
			if(markCountMap.get(movie.getId()) == null){
				markCountMap.put(movie.getId(), markService.getMarkCountByTagRelatedid(TagConstant.TAG_MOVIE, movie.getId()));
			}
		}
		// 正在热映电影
		model.put("hotMovieList", hotMovieList);
		model.put("curMarkCountMap", markCountMap);
		model.put("markData", markService.getMarkdata(TagConstant.TAG_MOVIE));
		// 可购票电影
		List<Long> opiMovieIdList = openPlayService.getOpiMovieidList(citycode, null);
		model.put("opiMovieList", opiMovieIdList);
		model.put("curMovieListCount",opiMovieIdList.size());
		//热门活动
		ErrorCode<List<RemoteActivity>> result = synchActivityService.getActivityListByOrder(citycode, null, RemoteActivity.TIME_CURRENT, TagConstant.TAG_CINEMA, null, null, null, "duetime", 0, 4);
		if(result.isSuccess()) model.put("activityList", result.getRetval());
		model.put("futureMovieP", true);
		model.put("cinemaCount",openPlayService.getOpiCinemaidList(citycode,null).size());
		model.put("activityCount",synchActivityService.getActivityCount(citycode, null,RemoteActivity.TIME_CURRENT, TagConstant.TAG_CINEMA, null).getRetval());
		// 右侧最受期待电影排行
		List<Movie> ranMoviekList = mcpService.getFutureMovieList(0, 10, Treasure.ACTION_XIANGQU);
		model.put("ranMoviekList", ranMoviekList);
		model.put("movieIdList", new ArrayList<Long>());
		return "movie/wide_futureMovie.vm";
	}
}
