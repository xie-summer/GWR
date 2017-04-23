package com.gewara.web.action.movie;


import java.io.Serializable;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
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
import org.hibernate.criterion.Subqueries;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.util.ClassUtils;
import org.springframework.web.bind.annotation.RequestMapping;

import com.gewara.command.SearchMovieStoreCommand;
import com.gewara.constant.TagConstant;
import com.gewara.json.PageView;
import com.gewara.json.bbs.MarkCountData;
import com.gewara.model.movie.Movie;
import com.gewara.model.ticket.OpenPlayItem;
import com.gewara.service.bbs.MarkService;
import com.gewara.service.movie.MCPService;
import com.gewara.service.ticket.OpenPlayService;
import com.gewara.support.ErrorCode;
import com.gewara.support.NullPropertyOrder;
import com.gewara.untrans.PageCacheService;
import com.gewara.untrans.PageParams;
import com.gewara.untrans.activity.SynchActivityService;
import com.gewara.util.BeanUtil;
import com.gewara.util.DateUtil;
import com.gewara.util.RelatedHelper;
import com.gewara.util.WebUtils;
import com.gewara.web.action.AnnotationController;
import com.gewara.web.util.PageUtil;
import com.gewara.xmlbind.activity.RemoteActivity;

@Controller
public class SearchMovieStoreController extends AnnotationController {
	@Autowired@Qualifier("hibernateTemplate")
	private HibernateTemplate hibernateTemplate;
	public void setHibernateTemplate(HibernateTemplate hibernateTemplate) {
		this.hibernateTemplate = hibernateTemplate;
	}
	@Autowired@Qualifier("markService")
	private MarkService markService;
	public void setMarkService(MarkService markService) {
		this.markService = markService;
	}
	@Autowired@Qualifier("synchActivityService")
	private SynchActivityService synchActivityService;
	public void setActivityRemoteService(SynchActivityService synchActivityService) {
		this.synchActivityService = synchActivityService;
	}
	
	@Autowired@Qualifier("pageCacheService")
	private PageCacheService pageCacheService;

	private static final String PLAY_TYPE_FUTURE = "future";//即将上映
	private static final String PLAY_TYPE_HOT = "hot";//正在热映
	private static final String PLAY_TYPE_CLASSIC = "classic";//下线电影
	
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
	/**
	 * 新版电影库
	 */
	@RequestMapping("/movie/searchMovieStore.xhtml")
	public String newMovieStore(ModelMap model, HttpServletRequest request,HttpServletResponse response,SearchMovieStoreCommand smsc){
		String citycode =  WebUtils.getAndSetDefault(request, response);
		if(StringUtils.isBlank(smsc.movietime)){
			String movietime = DateUtil.getCurrentYear().toString()+"-01-01," + DateUtil.getCurrentYear().toString()+"-12-31";
			smsc.movietime = movietime;
			smsc.playtype = "classic";
		}
		PageParams pageParams = new PageParams();
		if (pageCacheService.isUseCache(request)) {// 先使用缓存
			pageParams.addSingleString("movietype", smsc.movietype);
			pageParams.addSingleString("movietime", smsc.movietime);
			pageParams.addSingleString("order", smsc.order);
			pageParams.addSingleString("moviestate", smsc.moviestate);
			pageParams.addSingleString("playtype",smsc.playtype);
			pageParams.addSingleString("searchkey", smsc.searchkey);
			pageParams.addInteger("pageNo", smsc.getPageNo());
			PageView pageView = pageCacheService.getPageView(request, "movie/searchMovieStore.xhtml", pageParams, citycode);
			if (pageView != null) {
				model.put("pageView", pageView);
				return "pageView.vm";
			}
		}
		
		//根据条件查询影片
		List<Movie> searchMovieList = hibernateTemplate.findByCriteria(newSearchStoreDetached(smsc, citycode,smsc.getSearchkey()),smsc.pageNo*smsc.maxNum,smsc.maxNum);
		model.put("searchMovieList", searchMovieList);
		model.put("movietime", smsc.movietime);
		model.put("playtype", smsc.playtype);
		Map<Long, MarkCountData> markCountMap = new HashMap<Long, MarkCountData>();
		for(Movie movie:searchMovieList){
			markCountMap.put(movie.getId(), markService.getMarkCountByTagRelatedid(TagConstant.TAG_MOVIE, movie.getId()));
		}
		
		//查询的参数条件
		Map params = new HashMap();
		params.put("movietype", smsc.movietype);
		params.put("movietime", smsc.movietime);
		params.put("order", smsc.order);
		params.put("moviestate", smsc.moviestate);
		params.put("playtype",smsc.playtype);
		params.put("searchkey", smsc.searchkey);
		//查询的参数条件
		
		//影片总量和分页
		Integer count = new Integer(hibernateTemplate.findByCriteria(newSearchStoreDetached(smsc, citycode,smsc.getSearchkey()).setProjection(Projections.rowCount())).get(0)+"");
		PageUtil pageUtil = new PageUtil(count, smsc.maxNum, smsc.pageNo, "movie/searchMovieStore.xhtml", true, true);
		pageUtil.initPageInfo(params);
		model.put("pageUtil", pageUtil);
		//影片总量和分页
		
		//查询时间条件列表
		Integer futureDate=DateUtil.getYear(new Date())+1;
		model.put("currentDate",DateUtil.getYear(new Date())+"");
		model.put("currentDateValue", (DateUtil.getYear(new Date()))+"-01-01,"+(DateUtil.getYear(new Date()))+"-12-31");
		model.put("futureDate",futureDate+"");
		model.put("futureDateValue", futureDate+"-01-01,"+futureDate+"-12-31");
		
		//分别为：排片电影ID list，放映电影ID list ，  热映电影ID list
		List<Long> openMovieIdList = openPlayService.getOpiMovieidList(citycode, null);
		model.put("opiMovieList", openMovieIdList);
		model.put("playMovieIdList",mcpService.getCurMovieIdList(citycode));
		model.put("hotPlayMovieIdList", openPlayService.getHotPlayMovieIdList(citycode, "releasedate"));
		
		//正在热映
		List<Movie> movieList = mcpService.getCurMovieList(citycode);
		model.put("curMovieListCount", openMovieIdList.size());
		mcpService.sortMoviesByMpiCount(citycode, movieList);
		movieList = BeanUtil.getSubList(movieList, 0, 3);
		for (Movie movie : movieList) {
			Long mid = movie.getId();
			if(markCountMap.get(mid) == null){
				markCountMap.put(mid, markService.getMarkCountByTagRelatedid(TagConstant.TAG_MOVIE, mid));
			}
		}
		model.put("hotMovieList", movieList);
		model.put("curMarkCountMap", markCountMap);
		model.put("markData",markService.getMarkdata(TagConstant.TAG_MOVIE));
		//电影活动
		RelatedHelper rh = new RelatedHelper();
		model.put("relatedHelper", rh);
		ErrorCode<List<RemoteActivity>> code = synchActivityService.getActivityListByOrder(citycode, null, RemoteActivity.TIME_CURRENT, TagConstant.TAG_CINEMA, null, null, null, "duetime", 0, 4);
		if(code.isSuccess()){
			List<RemoteActivity> activityList = code.getRetval();
			List<Serializable> cinemaIdList = BeanUtil.getBeanPropertyList(activityList, Serializable.class, "relatedid", true);
			relateService.addRelatedObject(1, "cinemaIdList", rh, TagConstant.TAG_CINEMA, cinemaIdList);
			model.put("activityList", activityList);
		}
		model.put("cinemaCount",openPlayService.getOpiCinemaidList(citycode, null).size());
		model.put("fetureMovieCount",mcpService.getFutureMovieList(0, 200, null).size());
		model.put("activityCount",synchActivityService.getActivityCount(citycode, null,RemoteActivity.TIME_CURRENT, TagConstant.TAG_CINEMA, null).getRetval());
		model.put("searchMovieStore", true);
		model.put("movieIdList", new ArrayList<Long>());
		return "movie/wide_movieBox.vm";
	}
	
	private DetachedCriteria newSearchStoreDetached(SearchMovieStoreCommand smsc, String citycode,String searchMovieName){
		DetachedCriteria query = DetachedCriteria.forClass(Movie.class,"m");
		if(StringUtils.isBlank(smsc.order)){
			smsc.order = "releasedate";
		}
		if(StringUtils.isNotBlank(searchMovieName))
		query.add(Restrictions.or(Restrictions.like("m.moviename", searchMovieName,MatchMode.ANYWHERE), 
				  				  Restrictions.or(Restrictions.like("m.director", searchMovieName,MatchMode.ANYWHERE), 
				  						  		  Restrictions.or(Restrictions.like("m.actors", searchMovieName,MatchMode.ANYWHERE), 
				  						  				  		  Restrictions.or(Restrictions.like("m.highlight", searchMovieName,MatchMode.ANYWHERE),
				  						  				  				  		  Restrictions.like("m.flag", searchMovieName,MatchMode.ANYWHERE))))));
		query.add(Restrictions.isNotNull("m.logo"));
		if(StringUtils.isNotBlank(smsc.movietype)) query.add(Restrictions.like("m.type", smsc.movietype,MatchMode.ANYWHERE));
		if(StringUtils.isNotBlank(smsc.moviestate)){
			if(StringUtils.equals("其它", smsc.moviestate)){
				query.add(Restrictions.not(Restrictions.like("m.state", "中国", MatchMode.ANYWHERE)));
				query.add(Restrictions.not(Restrictions.like("m.state", "美国", MatchMode.ANYWHERE)));
				query.add(Restrictions.not(Restrictions.like("m.state", "欧洲", MatchMode.ANYWHERE)));
				query.add(Restrictions.not(Restrictions.like("m.state", "韩国", MatchMode.ANYWHERE)));
				query.add(Restrictions.not(Restrictions.like("m.state", "日本", MatchMode.ANYWHERE)));
			}else
			query.add(Restrictions.like("m.state", smsc.moviestate,MatchMode.ANYWHERE));
		}
		if(StringUtils.isNotBlank(smsc.movietime) && !smsc.movietime.equals("all")) {
			String[] times = smsc.movietime.split(",");
			Date startTime = DateUtil.parseDate(times[0]);
			Date endTime = startTime;
			if(times.length >1) endTime = DateUtil.parseDate(times[1]);
			query.add(Restrictions.between("m.releasedate", startTime, endTime));
		}
		Timestamp curTime = new Timestamp(System.currentTimeMillis());
		if(PLAY_TYPE_HOT.equals(smsc.playtype)){
			DetachedCriteria queryHotMovie = DetachedCriteria.forClass(OpenPlayItem.class,"opi");
			queryHotMovie.add(Restrictions.ge("opi.playtime", DateUtil.addHour(curTime, 1)));
			queryHotMovie.add(Restrictions.eq("opi.citycode", citycode));
			queryHotMovie.setProjection(Projections.distinct(Projections.property("opi.movieid")));
			queryHotMovie.add(Restrictions.eqProperty("m.id", "opi.movieid"));
			query.add(Subqueries.exists(queryHotMovie));
		}else if(PLAY_TYPE_FUTURE.equals(smsc.playtype)){
			query.add(Restrictions.ge("m.releasedate",curTime));
		}else if(PLAY_TYPE_CLASSIC.equals(smsc.playtype)){
			List<Long> ids = mcpService.getCurMovieIdList(citycode);
			query.add(Restrictions.lt("m.releasedate",curTime));
			if(ids.size()!=0)
			query.add(Restrictions.not(Restrictions.in("id", ids)));
		}
		if(StringUtils.isNotBlank(smsc.order)&&ClassUtils.hasMethod(Movie.class, "get" + StringUtils.capitalize(smsc.order))){
			query.addOrder(NullPropertyOrder.desc(smsc.order));
			query.addOrder(Order.desc("addtime"));
		}
		query.addOrder(Order.asc("m.id"));
		return query;
	}
	
}
