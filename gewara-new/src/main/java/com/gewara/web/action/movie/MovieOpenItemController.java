package com.gewara.web.action.movie;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.collections.ListUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.support.PropertyComparator;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.RequestMapping;

import com.gewara.constant.TagConstant;
import com.gewara.json.PageView;
import com.gewara.json.bbs.MarkCountData;
import com.gewara.model.common.County;
import com.gewara.model.common.Subwayline;
import com.gewara.model.movie.Cinema;
import com.gewara.model.movie.CinemaProfile;
import com.gewara.model.movie.CinemaRoom;
import com.gewara.model.movie.Movie;
import com.gewara.model.movie.MoviePlayItem;
import com.gewara.model.ticket.OpenPlayItem;
import com.gewara.model.user.Member;
import com.gewara.model.user.Treasure;
import com.gewara.service.bbs.BlogService;
import com.gewara.service.bbs.MarkService;
import com.gewara.service.movie.MCPService;
import com.gewara.support.MultiPropertyComparator;
import com.gewara.support.VelocityTemplate;
import com.gewara.untrans.PageCacheService;
import com.gewara.untrans.PageParams;
import com.gewara.util.BeanUtil;
import com.gewara.util.DateUtil;
import com.gewara.util.WebUtils;
import com.gewara.web.action.AnnotationController;

@Controller
public class MovieOpenItemController extends AnnotationController {
	@Autowired@Qualifier("pageCacheService")
	private PageCacheService pageCacheService;
	public void setPageCacheService(PageCacheService pageCacheService) {
		this.pageCacheService = pageCacheService;
	}
	@Autowired@Qualifier("mcpService")
	private MCPService mcpService;
	public void setMcpService(MCPService mcpService) {
		this.mcpService = mcpService;
	}
	@Autowired@Qualifier("markService")
	private MarkService markService;
	public void setMarkService(MarkService markService) {
		this.markService = markService;
	}
	
	@Autowired@Qualifier("velocityTemplate")
	private VelocityTemplate velocityTemplate;
	public void setVelocityTemplate(VelocityTemplate velocityTemplate) {
		this.velocityTemplate = velocityTemplate;
	}
	
	@Autowired@Qualifier("blogService")
	private BlogService blogService;
	
	private Date getPlaydate(String fyrq){
		if (!DateUtil.isValidDate(fyrq)) fyrq = "";
		Date cur = new Date();
		Date playdate = null;
		if (StringUtils.isNotBlank(fyrq)) {
			playdate = DateUtil.parseDate(fyrq);
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
	
	@RequestMapping("/movie/ajax/opi.xhtml")
	public String movieOpi(@CookieValue(value=LOGIN_COOKIE_NAME, required=false)String sessid, 
			String fyrq, Long movieid, Long cid, ModelMap model, HttpServletRequest request, HttpServletResponse response) {
		Movie movie = daoService.getObject(Movie.class, movieid);
		if (movie == null) return show404(model, "电影不存在或已经删除！");
		String citycode = null;
		Cinema cinema = daoService.getObject(Cinema.class, cid);
		if(cinema != null){
			citycode = cinema.getCitycode();
			WebUtils.setCitycode(request, citycode, response);
		}
		if(StringUtils.isBlank(citycode)) citycode = WebUtils.getAndSetDefault(request, response);
		boolean useCache = pageCacheService.isUseCache(request);
		Member member = loginService.getLogonMemberBySessid(WebUtils.getRemoteIp(request), sessid);
		if (useCache && member == null) {// 先使用缓存
			PageParams pageParams = new PageParams();
			pageParams.addDateStr("fyrq", fyrq);
			pageParams.addLong("movieid", movieid);
			PageView pageView = pageCacheService.getPageView(request, "movie/ajax/opi.xhtml", pageParams, citycode);
			if (pageView != null) {
				model.put("pageView", pageView);
				return "pageView.vm";
			}
		}
		Date playdate = getPlaydate(fyrq);
		fyrq = DateUtil.formatDate(playdate);
		List<Date> dateList = mcpService.getCurMoviePlayDate2(citycode, movieid);
		if (!dateList.isEmpty() && playdate.before(dateList.get(0))) {
			playdate = dateList.get(0);
		}
		if(!dateList.isEmpty() && !dateList.contains(playdate)){
			playdate = dateList.get(0);
		}
		model.put("fyrq", DateUtil.formatDate(playdate));
		model.put("fyrqDate", playdate);
		model.put("dateList", dateList);
		List<Cinema> orderCinemaList = new ArrayList<Cinema>();
		if(member != null){
			List<Treasure> tList = blogService.getTreasureListByMemberId(member.getId(), new String[]{"cinema"} ,null, null, 0, 10, Treasure.ACTION_COLLECT);
			if(tList != null && !tList.isEmpty()){
				List<Long> cinemaIdList = BeanUtil.getBeanPropertyList(tList, Long.class, "relatedid", true);
				orderCinemaList = daoService.getObjectList(Cinema.class, cinemaIdList);
			}
			//orderCinemaList = orderQueryService.getMemberOrderCinemaList(member.getId(), 4);
		}
		addMovieData(playdate, citycode, movieid, orderCinemaList, model);
		return "movie/wide_movieDetailPlayItem.vm";
	}
	@RequestMapping("/movie/ajax/getOpiItem.xhtml")
	public String getOpiItem(String fyrq, Long movieid, Long cid, boolean isView, ModelMap model,
			HttpServletRequest request, HttpServletResponse response){
		Cinema cinema = daoService.getObject(Cinema.class, cid);
		if(cinema == null) return showJsonError(model, "该影院不存在或被删除！");
		String citycode = cinema.getCitycode();
		if(StringUtils.isBlank(citycode)){
			citycode = WebUtils.getAndSetDefault(request, response);
		}
		Date playdate = getPlaydate(fyrq);
		String ip = WebUtils.getRemoteIp(request);
		PageView result = getOpiItemStr(pageCacheService.isUseCache(request), playdate, movieid, cid, ip);
		if(isView){
			model.put("result", result.getContent());
			return "movie/movieOpi.vm";
		}
		return showJsonSuccess(model, result.getContent());
	}
	/**
	 * 只用于缓存更新
	 * @param playdate
	 * @param movieid
	 * @param cid
	 * @param model
	 * @return
	 */
	@RequestMapping("/movie/ajax/getOpiItemPage.xhtml")
	public String getOpiItemPage(HttpServletRequest request, String fyrq, Long movieid, Long cid, ModelMap model){
		Date playdate = getPlaydate(fyrq);
		String ip = WebUtils.getRemoteIp(request);
		PageView pageView = getOpiItemStr(pageCacheService.isUseCache(request), playdate, movieid, cid, ip);
		model.put("pageView", pageView);
		return "pageView.vm";
	}
	private PageView getOpiItemStr(boolean useCache, Date playdate, Long movieid, Long cid, String ip){
		Cinema cinema = daoService.getObject(Cinema.class, cid);
		String fyrq = DateUtil.formatDate(playdate);
		PageParams pageParams = new PageParams();
		pageParams.addDateStr("fyrq", fyrq);
		pageParams.addLong("cid", cid);
		pageParams.addLong("movieid", movieid);
		String pageUrl = "movie/ajax/getOpiItemPage.xhtml";
		if (useCache) {// 先使用缓存
			PageView pageView = pageCacheService.getPageView(pageUrl, pageParams, cinema.getCitycode(), ip);
			if(pageView != null) {
				return pageView;
			}
		}
		
		Map dataMap = new HashMap();
		Movie movie = daoService.getObject(Movie.class, movieid);
		dataMap.put("curmovie", movie);
		dataMap.put("cinema", cinema);
		
		MarkCountData movieMarkCount = markService.getMarkCountByTagRelatedid(TagConstant.TAG_MOVIE, movieid);
		dataMap.put("markData", markService.getMarkdata(TagConstant.TAG_MOVIE));
		dataMap.put("movieMarkCount", movieMarkCount);
		/*Timestamp timeFrom = new Timestamp(playdate.getTime());
		Timestamp timeTo = DateUtil.addHour(DateUtil.getLastTimeOfDay(timeFrom),6);
		List<OpenPlayItem> opiList = openPlayService.getOpiList(cinema.getCitycode(), cid, movieid, timeFrom, timeTo, true);*/
		List<MoviePlayItem> moviePlayItemList = mcpService.getCurMpiList(cid, movieid, playdate);
		List<MoviePlayItem> mpiList = new ArrayList<MoviePlayItem>();
		Map<Long, OpenPlayItem> opiMap = new HashMap<Long, OpenPlayItem>();
		Date curTimeDate =DateUtil.currentTime();
		String hour = DateUtil.format(curTimeDate, "HH:mm");
		Date curDate = DateUtil.getBeginningTimeOfDay(curTimeDate);
		boolean isCurDate = DateUtil.getDiffDay(playdate, curDate) == 0;
		Map<Long, CinemaRoom> roomMap = new HashMap<Long, CinemaRoom>();
		for (MoviePlayItem item : moviePlayItemList) {
			if(!isCurDate || hour.compareTo(item.getPlaytime())<=0){
				OpenPlayItem opi = daoService.getObjectByUkey(OpenPlayItem.class, "mpid", item.getId(), true);
				if (opi != null && !opi.isClosed()) {
					opiMap.put(item.getId(), opi);
				}
				mpiList.add(item);
				if(roomMap.get(item.getRoomid()) == null){
					roomMap.put(item.getRoomid(), daoService.getObject(CinemaRoom.class, item.getRoomid()));
				}
			}
		}
		Collections.sort(mpiList, new PropertyComparator("playtime", false, true));
		dataMap.put("opiMap", opiMap);
		dataMap.put("mpiList", mpiList);
		dataMap.put("roomMap", roomMap);
		
		dataMap.put("cp", daoService.getObject(CinemaProfile.class, cid));
		dataMap.put("fyrq", fyrq);
		String result = velocityTemplate.parseTemplate("movie/wide_movieOpiItem.vm", dataMap);
		return new PageView(DateUtil.addMinute(new Date(), pageCacheService.getCacheMin(pageUrl)).getTime(), result);
	}
	
	private void addMovieData(Date playdate, String citycode, Long movieid, List<Cinema> orderCinemaList, ModelMap model) {
		List<Movie> movieList = mcpService.getOpenMovieList(citycode);
		mcpService.sortMoviesByMpiCount(citycode, movieList);
		if (movieList.size() == 0) return;
		Movie curmovie = null;
		if (movieid == null) {
			curmovie = movieList.get(0);
			movieid = curmovie.getId();
		} else {
			curmovie = daoService.getObject(Movie.class, movieid);
			if(curmovie == null){
				curmovie = movieList.get(0);
				movieid = curmovie.getId();
			}else if (!movieList.contains(curmovie)){
				movieList.add(0, curmovie);
			}
		}
		model.put("curmovie", curmovie);
		List<Long> cidList = mcpService.getCurCinemaIdList(citycode, movieid, playdate); 
		List<Cinema> cinemaList = daoService.getObjectList(Cinema.class, cidList);
		Collections.sort(cinemaList, new MultiPropertyComparator(new String[] { "hotvalue", "clickedtimes" }, new boolean[] { false, false }));
		List<County> countyList = daoService.getObjectList(County.class, BeanUtil.getBeanPropertyList(cinemaList, String.class, "countycode", true));
		Map countyMap = BeanUtil.groupBeanProperty(cinemaList, "countycode", "id");
		model.put("movieid", movieid);
		model.put("movieList", movieList);
		model.put("moviecount", movieList.size());
		model.put("cinemaList", cinemaList);
		model.put("countyList", countyList);
		model.put("countyMap", countyMap);
		List<Cinema> orderList = ListUtils.intersection(orderCinemaList, cinemaList);
		model.put("orderList", orderList);
		//特设影厅
		List<CinemaRoom> cinemaRoomList = mcpService.getCurCinemaRoomByMovieId(citycode, movieid, playdate);
		List<String> characteristicList = new ArrayList<String>();
		Map<Long, List<String>> cinemaCMap = new HashMap<Long, List<String>>();
		if(cinemaRoomList != null && !cinemaRoomList.isEmpty()){
			for (CinemaRoom cinemaRoom : cinemaRoomList) {
				if(!characteristicList.contains(cinemaRoom.getCharacteristic())){
					characteristicList.add(cinemaRoom.getCharacteristic());
				}
				if(cinemaCMap.get(cinemaRoom.getCinemaid()) != null){
					List<String> cList = cinemaCMap.get(cinemaRoom.getCinemaid());
					if(!cList.contains(cinemaRoom.getCharacteristic())){
						cList.add(cinemaRoom.getCharacteristic());
					}
				}else{
					List<String> cList = new ArrayList<String>();
					cList.add(cinemaRoom.getCharacteristic());
					cinemaCMap.put(cinemaRoom.getCinemaid(), cList);
				}
			}
		}
		model.put("characteristicList", characteristicList);
		model.put("cinemaCMap", cinemaCMap);
		Set<Long> lineIdList = new HashSet<Long>();
		for(Cinema c : cinemaList){
			if(StringUtils.isNotBlank(c.getLineidlist())){
				lineIdList.addAll(BeanUtil.getIdList(c.getLineidlist(), ","));
			}
		}
		List<Subwayline> lineList = daoService.getObjectList(Subwayline.class, lineIdList);
		if(lineList != null && !lineList.isEmpty()){
			for (Iterator iterator = lineList.iterator(); iterator.hasNext();) {
				Subwayline subwayline = (Subwayline) iterator.next();
				if(!StringUtils.equals(subwayline.getCitycode(), citycode)){
					iterator.remove();
				}
			}
			Collections.sort(lineList, new Comparator() {
				public int compare(Object o1, Object o2) {
					String s1 = ((Subwayline)o1).getLinename();
					String s2 = ((Subwayline)o2).getLinename();
					return StringUtils.leftPad(s1, 4, "0").compareTo(StringUtils.leftPad(s2, 4, "0"));
				}
			});
		}
		model.put("lineList", lineList);
	}
}
