package com.gewara.web.action.movie;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.support.PropertyComparator;
import org.springframework.ui.ModelMap;

import com.gewara.json.PageView;
import com.gewara.model.common.County;
import com.gewara.model.movie.Cinema;
import com.gewara.model.movie.CinemaProfile;
import com.gewara.model.movie.Movie;
import com.gewara.model.movie.MoviePlayItem;
import com.gewara.model.ticket.OpenPlayItem;
import com.gewara.service.bbs.MarkService;
import com.gewara.service.movie.MCPService;
import com.gewara.service.ticket.OpenPlayService;
import com.gewara.support.MultiPropertyComparator;
import com.gewara.support.VelocityTemplate;
import com.gewara.untrans.PageCacheService;
import com.gewara.untrans.PageParams;
import com.gewara.util.BeanUtil;
import com.gewara.util.DateUtil;
import com.gewara.web.action.AnnotationController;

public class BasePlayItemController extends AnnotationController {
	@Autowired@Qualifier("pageCacheService")
	protected PageCacheService pageCacheService;
	public void setPageCacheService(PageCacheService pageCacheService) {
		this.pageCacheService = pageCacheService;
	}
	@Autowired@Qualifier("mcpService")
	protected MCPService mcpService;
	public void setMcpService(MCPService mcpService) {
		this.mcpService = mcpService;
	}
	@Autowired@Qualifier("openPlayService")
	protected OpenPlayService openPlayService;
	public void setOpenPlayService(OpenPlayService openPlayService) {
		this.openPlayService = openPlayService;
	}
	@Autowired@Qualifier("markService")
	protected MarkService markService;
	public void setMarkService(MarkService markService) {
		this.markService = markService;
	}
	
	@Autowired@Qualifier("velocityTemplate")
	protected VelocityTemplate velocityTemplate;
	public void setVelocityTemplate(VelocityTemplate velocityTemplate) {
		this.velocityTemplate = velocityTemplate;
	}	
	protected PageView getPlayItemPageStr(boolean useCache, Date playdate, Long movieid, Long cid, String ip){
		Cinema cinema = daoService.getObject(Cinema.class, cid);
		String fyrq = DateUtil.formatDate(playdate);
		PageParams pageParams = new PageParams();
		if(cid!=null){
			pageParams.addNumberStr("cid", ""+cid);
		}
		if(movieid!=null){
			pageParams.addNumberStr("movieid", ""+movieid);
		}
		pageParams.addDateStr("fyrq", fyrq);
		//Map<String,String> params = new HashMap<String, String>();
		String pageUrl = "movie/ajax/getPlayItemPage.xhtml";
		if (useCache) {// œ» π”√ª∫¥Ê
			PageView pageView = pageCacheService.getPageView(pageUrl, pageParams, cinema.getCitycode(), ip);
			if(pageView!=null){
				return pageView;
			}
		}
		
		Map dataMap = new HashMap();

		Movie movie = daoService.getObject(Movie.class, movieid);
		dataMap.put("curmovie", movie);
		dataMap.put("cinema", cinema);
		dataMap.put("cp", daoService.getObject(CinemaProfile.class, cid));
		List<MoviePlayItem> moviePlayItemList = mcpService.getCurMpiList(cid, movieid, playdate);
		List<MoviePlayItem> mpiList = new ArrayList<MoviePlayItem>();
		Map<Long, OpenPlayItem> opiMap = new HashMap<Long, OpenPlayItem>();
		Date curTimeDate =DateUtil.currentTime();
		String hour = DateUtil.format(curTimeDate, "HH:mm");
		Date curDate = DateUtil.getBeginningTimeOfDay(curTimeDate);
		boolean isCurDate = DateUtil.getDiffDay(playdate, curDate) == 0;
		for (MoviePlayItem item : moviePlayItemList) {
			if(!isCurDate || hour.compareTo(item.getPlaytime())<=0){
				OpenPlayItem opi = daoService.getObjectByUkey(OpenPlayItem.class, "mpid", item.getId(), true);
				if (opi != null && !opi.isClosed()) {
					opiMap.put(item.getId(), opi);
				}
				mpiList.add(item);
			}
		}
		Collections.sort(mpiList, new PropertyComparator("playtime", false, true));
		dataMap.put("opiMap", opiMap);
		dataMap.put("mpiList", mpiList);
		String result = velocityTemplate.parseTemplate("movie/moviePlayItem.vm", dataMap);
		return new PageView(DateUtil.addMinute(new Date(), pageCacheService.getCacheMin(pageUrl)).getTime(), result);
	}
	
	protected void addMoviePlayItem(boolean useCache, Date playdate, String citycode, Long movieid, String ip, ModelMap model) {
		List<Long> cidList = mcpService.getCurCinemaIdList(citycode, movieid, playdate); 
		List<Cinema> cinemaList = daoService.getObjectList(Cinema.class, cidList);
		Collections.sort(cinemaList, new MultiPropertyComparator(new String[] { "hotvalue", "clickedtimes" }, new boolean[] { false, false }));
		List<County> countyList = daoService.getObjectList(County.class, BeanUtil.getBeanPropertyList(cinemaList, String.class, "countycode", true));
		Map countyMap = BeanUtil.groupBeanProperty(cinemaList, "countycode", "id");
		model.put("movieid", movieid);
		model.put("countyList", countyList);
		model.put("countyMap", countyMap);
		model.put("cinemaList", cinemaList);
		if(!cidList.isEmpty()){
			Map pageMap = new HashMap();
			for(Long cid: cidList){
				PageView page = getPlayItemPageStr(useCache, playdate, movieid, cid, ip);
				pageMap.put("cinema"+cid, page.getContent());
			}
			model.put("pageMap", pageMap);
		}	
	}

}
