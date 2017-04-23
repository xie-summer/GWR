package com.gewara.web.action.movie;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.gewara.constant.Flag;
import com.gewara.constant.TagConstant;
import com.gewara.helper.ticket.OpiFilter;
import com.gewara.helper.ticket.SdOpiFilter;
import com.gewara.json.PageView;
import com.gewara.json.bbs.MarkCountData;
import com.gewara.model.common.County;
import com.gewara.model.common.Subwayline;
import com.gewara.model.movie.Cinema;
import com.gewara.model.movie.CinemaProfile;
import com.gewara.model.movie.Movie;
import com.gewara.model.pay.SpecialDiscount;
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
import com.gewara.util.JsonUtils;
import com.gewara.util.PKCoderUtil;
import com.gewara.util.VmUtils;
import com.gewara.util.WebUtils;
import com.gewara.web.action.AnnotationController;

@Controller
public class SearchOpiController extends AnnotationController {
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
	@Autowired@Qualifier("openPlayService")
	private OpenPlayService openPlayService;
	public void setOpenPlayService(OpenPlayService openPlayService) {
		this.openPlayService = openPlayService;
	}
	@Autowired@Qualifier("markService")
	private MarkService markService;
	public void setMarkService(MarkService markService) {
		this.markService = markService;
	}
	
	@Autowired@Qualifier("velocityTemplate")
	private VelocityTemplate velocityTemplate;

	// 影片购票
	@RequestMapping("/movie/opi.xhtml")
	public String movieOpi(ModelMap model, String fyrq, @RequestParam("mid")Long mid) {
		if (StringUtils.isNotBlank(fyrq))model.put("fyrq", fyrq);
		if (mid != null) model.put("movieid", mid);
		return "redirect:/cinema/searchOpi.xhtml";
	}

	@RequestMapping("/cinema/searchOpi.xhtml")
	public String searchOpi(String fyrq, Long movieid, Long cid, Long mpid, String spkey/* 商家活动 */, ModelMap model,
			HttpServletRequest request, HttpServletResponse response) {
		if (mpid != null) {
			model.put("mpid", mpid);
			return "redirect:/cinema/order/step1.shtml";
		}
		String citycode = null;
		if (cid != null) {
			Cinema cinema = daoService.getObject(Cinema.class, cid);
			citycode = cinema.getCitycode();
			WebUtils.setCitycode(request, citycode, response);
		} else {
			citycode = WebUtils.getAndSetDefault(request, response);
		}
		model.put("cid", cid);
		if (!DateUtil.isValidDate(fyrq)) fyrq = "";
		Date cur = new Date();
		Date playdate = null;
		if (StringUtils.isNotBlank(fyrq)) {
			playdate = DateUtil.parseDate(fyrq);
			if (playdate.before(DateUtil.getBeginningTimeOfDay(cur))) {
				playdate = DateUtil.getBeginningTimeOfDay(cur);
			}
			model.put("dFyrq", playdate);
		} else {
			if (DateUtil.getHour(cur) < 21)
				playdate = DateUtil.getBeginningTimeOfDay(cur);
			else{
				playdate = DateUtil.addDay(DateUtil.getCurDate(), 1);
			}
		}
		fyrq = DateUtil.formatDate(playdate);
		model.put("fyrq",fyrq);
		SpecialDiscount sd = null;
		boolean useCache = pageCacheService.isUseCache(request);
		if (StringUtils.isNotBlank(spkey)) {
			String spid = PKCoderUtil.decryptString(spkey, SpecialDiscount.ENCODE_KEY);
			if (StringUtils.isNotBlank(spid)) {
				sd = daoService.getObject(SpecialDiscount.class, new Long(spid));
				if(sd!=null){
					if(Math.abs(DateUtil.getMillDiffMinu(sd.getTimefrom().getTime(), System.currentTimeMillis()))<20 || 
							Math.abs(DateUtil.getMillDiffMinu(sd.getTimeto().getTime(), System.currentTimeMillis()))< 20 ){//时间前后不使用缓存
						useCache = false;
					}else{
						int addmin = Integer.parseInt(sd.getAddtime1().substring(0,2))*60+Integer.parseInt(sd.getAddtime1().substring(2));
						String hmin = DateUtil.format(cur, "HHmm");
						int curmin = Integer.parseInt(hmin.substring(0,2))*60+Integer.parseInt(hmin.substring(2));
						if(Math.abs(addmin - curmin)<20){//下单时段前后20分钟不使用缓存
							useCache = false;
						}
					}
				}
			}
		}
		if (useCache) {// 先使用缓存
			PageParams params = new PageParams();
			params.addDateStr("fyrq", fyrq);
			params.addSingleString("spkey", spkey);
			params.addLong("movieid", movieid);
			params.addLong("cid", cid);
			PageView pageView = pageCacheService.getPageView(request, "cinema/searchOpi.xhtml", params, citycode);
			if (pageView != null) {
				model.put("pageView", pageView);
				return "pageView.vm";
			}
		}
		model.put("spkey", spkey);
		addOpiData(playdate, citycode, sd, movieid, spkey, model);
		return "cinema/wide_opiList.vm";
	}
	private void addOpiData(Date playdate, String citycode, SpecialDiscount sd, Long movieid, String spkey/* 商家活动 */,ModelMap model) {
		List<Movie> movieList = mcpService.getOpenMovieList(citycode);
		model.put("movieList", movieList);
		mcpService.sortMoviesByMpiCount(citycode, movieList);
		Map<Long, MarkCountData> markCountMap = new HashMap<Long, MarkCountData>();
		if (movieList.size() == 0) return;
		Movie curmovie = null;
		if (movieid == null) {
			curmovie = movieList.get(0);
			movieid = curmovie.getId();
		} else {
			curmovie = daoService.getObject(Movie.class, movieid);
			if (!movieList.contains(curmovie)){
				movieList.add(0, curmovie);
			}
		}
		markCountMap.put(curmovie.getId(), markService.getMarkCountByTagRelatedid(TagConstant.TAG_MOVIE, curmovie.getId()));
		model.put("curmovie", curmovie);
		List<Date> dateList = openPlayService.getMovieOpenDateList(citycode, movieid);
		model.put("movieid", movieid);
		model.put("dateList", dateList);
		model.put("moviecount", movieList.size());
		model.put("curMarkCountMap", markCountMap);
		model.put("markData", markService.getMarkdata(TagConstant.TAG_MOVIE));
		if(!dateList.contains(playdate)) return;
		Timestamp starttime = new Timestamp(playdate.getTime());
		Timestamp endtime = new Timestamp(DateUtil.getLastTimeOfDay(playdate).getTime());
		List<OpenPlayItem> opiList = openPlayService.getOpiList(citycode, null, movieid, starttime, endtime, true);
		if (sd != null) {
			OpiFilter filter = new SdOpiFilter(sd, new Timestamp(System.currentTimeMillis()));
			filter.applyFilter(opiList);
			model.put("adspdiscount", sd);
			model.put("spkey", spkey);
		}
		List<String> languageList = new ArrayList<String>();BeanUtil.getBeanPropertyList(opiList, String.class, "language", true);
		List<String> editionList = new ArrayList<String>();BeanUtil.getBeanPropertyList(opiList, String.class, "edition", true);
		Map<String,List<Long>> languageCinema = new HashMap<String,List<Long>>();
		Map<String,List<Long>> editionCinema = new HashMap<String,List<Long>>();
		for(OpenPlayItem o:opiList){
			String language = o.getLanguage();
			String edition = o.getEdition();
			if(StringUtils.isNotBlank(language)){
				if(!languageList.contains(language)){
					languageList.add(language);
				}
				List<Long> cIds = languageCinema.get(language);
				if(cIds == null){
					cIds = new ArrayList<Long>();
					languageCinema.put(language, cIds);
				}
				if(!cIds.contains(o.getCinemaid())){
					cIds.add(o.getCinemaid());
				}
			}
			if(StringUtils.isNotBlank(edition)){
				if(!editionList.contains(edition)){
					editionList.add(edition);
				}
				List<Long> cIds = editionCinema.get(edition);
				if(cIds == null){
					cIds = new ArrayList<Long>();
					editionCinema.put(edition, cIds);
				}
				if(!cIds.contains(o.getCinemaid())){
					cIds.add(o.getCinemaid());
				}
			}
		}
		model.put("languageList", languageList);
		model.put("editionList", editionList);
		model.put("languageCinema",JsonUtils.writeObjectToJson(languageCinema));
		model.put("editionCinema", JsonUtils.writeObjectToJson(editionCinema));
		Map<Long/* cinemaid */, List<OpenPlayItem>> opiMap = BeanUtil.groupBeanList(opiList, "cinemaid");
		List<Cinema> cinemaList = daoService.getObjectList(Cinema.class, opiMap.keySet());
		List<String> lineIds = BeanUtil.getBeanPropertyList(cinemaList, String.class, "lineidlist", true);
		String lineId = StringUtils.join(lineIds,",");
		List<Long> lineIdList = new ArrayList<Long>();
		String[] lines = StringUtils.split(lineId, ",");
		for(String id : lines){
			if(StringUtils.isNotBlank(id) && !lineIdList.contains(Long.parseLong(id))){
				lineIdList.add(Long.parseLong(id));
			}
		}
		List<Subwayline> subwayLineList = daoService.getObjectList(Subwayline.class,lineIdList);
		Collections.sort(subwayLineList, new Comparator() {
			public int compare(Object o1, Object o2) {
				String s1 = ((Subwayline)o1).getLinename();
				String s2 = ((Subwayline)o2).getLinename();
				return StringUtils.leftPad(s1, 4, "0").compareTo(StringUtils.leftPad(s2, 4, "0"));
			}
		});
		model.put("subwayLineList",subwayLineList);
		List<County> countyList = daoService.getObjectList(County.class, BeanUtil.getBeanPropertyList(cinemaList, String.class, "countycode", true));
		Map<Long, CinemaProfile> cinemaPMap = daoService.getObjectMap(CinemaProfile.class, opiMap.keySet());
		Collections.sort(cinemaList, new MultiPropertyComparator(new String[] {"hotvalue", "clickedtimes" }, new boolean[] {false, false }));
		Map<Long, String> cinemaOtherinfoMap = new HashMap<Long, String>();
		Set<String> fetures = new HashSet<String>();
		for (Cinema cinema : cinemaList) {
			Map<String, String> otherinfo = VmUtils.readJsonToMap(cinema.getOtherinfo());
			CinemaProfile cp = daoService.getObject(CinemaProfile.class, cinema.getId());
			if (cp != null && CinemaProfile.POPCORN_STATUS_Y.equals(cp.getPopcorn())){
				otherinfo.put("popcorn", "true");
			}
			if(StringUtils.contains(otherinfo.get(Flag.SERVICE_PARK_RECOMMEND),"free")){
				otherinfo.put("freePack", "true");
			}
			if(StringUtils.contains(otherinfo.get(Flag.SERVICE_3D_RECOMMEND),"free")){
				otherinfo.put("free3D", "true");
			}
			Set<String> keySet = otherinfo.keySet();
			cinemaOtherinfoMap.put(cinema.getId(), StringUtils.join(otherinfo.keySet(), ","));
			fetures.addAll(keySet);
		}
		model.put("countyList", countyList);
		model.put("cinemaOtherinfoMap",cinemaOtherinfoMap);
		model.put("opiMap", opiMap);
		model.put("cinemaList",cinemaList);
		model.put("cpmap", cinemaPMap);
		model.put("fetures",fetures);
	}
	
	@RequestMapping("/movie/ajax/getSearchOpiItem.xhtml")
	public String getOpiItem(String fyrq, Long movieid, Long cid,String spkey, ModelMap model,
			HttpServletRequest request, HttpServletResponse response){
		if(StringUtils.isBlank(fyrq) || !DateUtil.isValidDate(fyrq)) return showJsonError(model, "请选择购票日期！");
		SpecialDiscount sd = null;
		boolean useCache = pageCacheService.isUseCache(request);
		Cinema cinema = daoService.getObject(Cinema.class, cid);
		if(cinema == null) return showJsonError(model, "该影院不存在或被删除！");
		if (StringUtils.isNotBlank(spkey)) {
			String spid = PKCoderUtil.decryptString(spkey, SpecialDiscount.ENCODE_KEY);
			if (StringUtils.isNotBlank(spid)) {
				sd = daoService.getObject(SpecialDiscount.class, new Long(spid));
				if(sd!=null){
					if(Math.abs(DateUtil.getMillDiffMinu(sd.getTimefrom().getTime(), System.currentTimeMillis()))<20 || 
							Math.abs(DateUtil.getMillDiffMinu(sd.getTimeto().getTime(), System.currentTimeMillis()))< 20 ){//时间前后不使用缓存
						useCache = false;
					}else{
						int addmin = Integer.parseInt(sd.getAddtime1().substring(0,2))*60+Integer.parseInt(sd.getAddtime1().substring(2));
						String hmin = DateUtil.format(new Date(), "HHmm");
						int curmin = Integer.parseInt(hmin.substring(0,2))*60+Integer.parseInt(hmin.substring(2));
						if(Math.abs(addmin - curmin)<20){//下单时段前后20分钟不使用缓存
							useCache = false;
						}
					}
				}
			}
		}
		String citycode = cinema.getCitycode();
		if(StringUtils.isBlank(citycode)){
			citycode = WebUtils.getAndSetDefault(request, response);
		}
		Date playdate = DateUtil.parseDate(fyrq);
		String ip = WebUtils.getRemoteIp(request);
		PageView pageView = getOpiItemStr(useCache, playdate, movieid, cinema, ip,spkey,citycode,sd);
		model.put("pageView", pageView);
		return "pageView.vm";
	}
	
	private PageView getOpiItemStr(boolean useCache, Date playdate, Long movieid, Cinema cinema, String ip,String spkey,String citycode,
			SpecialDiscount sd ){
		String fyrq = DateUtil.formatDate(playdate);
		String pageUrl = "movie/ajax/getSearchOpiItem.xhtml";
		if (useCache) {// 先使用缓存
			PageParams pageParams = new PageParams();
			pageParams.addDateStr("fyrq", fyrq);
			pageParams.addSingleString("spkey", spkey);
			pageParams.addLong("movieid", movieid);
			pageParams.addLong("cid", cinema.getId());
			PageView pageView = pageCacheService.getPageView(pageUrl, pageParams,citycode, ip);
			if(pageView != null) {
				return pageView;
			}
		}
		Map dataMap = new HashMap();
		dataMap.put("cinema", cinema);
		Timestamp timeFrom = new Timestamp(playdate.getTime());
		Timestamp timeTo = DateUtil.addHour(DateUtil.getLastTimeOfDay(timeFrom),6);
		List<OpenPlayItem> opiList = openPlayService.getOpiList(cinema.getCitycode(), cinema.getId(), movieid, timeFrom, timeTo, true);
		if (sd != null) {
			OpiFilter filter = new SdOpiFilter(sd, new Timestamp(System.currentTimeMillis()));
			filter.applyFilter(opiList);
		}
		dataMap.put("opiList", opiList);
		dataMap.put("curmovie", daoService.getObject(Movie.class, movieid));
		dataMap.put("cp", daoService.getObject(CinemaProfile.class, cinema.getId()));
		dataMap.put("fyrq", fyrq);
		dataMap.put("spkey",spkey);
		String result = velocityTemplate.parseTemplate("movie/wide_ajax_cinemaOpi.vm", dataMap);
		return new PageView(DateUtil.addMinute(new Date(), pageCacheService.getCacheMin(pageUrl)).getTime(), result);
	}
}
