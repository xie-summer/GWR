package com.gewara.web.action.partner;

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.support.PropertyComparator;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import com.gewara.constant.AdminCityContant;
import com.gewara.constant.TagConstant;
import com.gewara.constant.sys.CacheConstant;
import com.gewara.constant.ticket.PartnerConstant;
import com.gewara.json.bbs.MarkCountData;
import com.gewara.model.api.ApiUser;
import com.gewara.model.common.County;
import com.gewara.model.movie.Cinema;
import com.gewara.model.movie.Movie;
import com.gewara.model.movie.MoviePlayItem;
import com.gewara.model.ticket.OpenPlayItem;
import com.gewara.service.PlaceService;
import com.gewara.service.bbs.MarkService;
import com.gewara.untrans.CacheService;
import com.gewara.util.BeanUtil;
import com.gewara.util.DateUtil;
import com.gewara.util.VmUtils;
import com.gewara.web.util.PageUtil;

@Controller
public class PartnerBestvController  extends BasePartnerController {
	@Autowired@Qualifier("placeService")
	protected PlaceService placeService;
	public void setPlaceService(PlaceService placeService) {
		this.placeService = placeService;
	}
	@Autowired@Qualifier("markService")
	private MarkService markService;
	private ApiUser getBestv(){
		return daoService.getObject(ApiUser.class, PartnerConstant.PARTNER_BESTV);
	}
	
	@Autowired@Qualifier("cacheService")
	private CacheService cacheService;
	
	@RequestMapping("/partner/bestv/index.xhtml")
	public String index(){
		return "partner/bestv/index.vm";
	}
	
	@RequestMapping("/partner/bestv/cityList.xhtml")
	public String cityList(long movieid,ModelMap model){
		model.put("movieid", movieid);
		model.put("cityMap", AdminCityContant.getCitycode2CitynameMap());
		model.put("citys",getBestv().getCitycode().split(","));
		return "partner/bestv/cityList.vm";
	}
	
	@RequestMapping("/partner/bestv/movieDetail.xhtml")
	public String movieDetail(long movieid,ModelMap model){
		Movie movie = this.daoService.getObject(Movie.class, movieid);
		model.put("movie", movie);
		model.put("generalmark", this.getMovieMark(movie));
		return "partner/bestv/movieDetail.vm";
	}
	
	private String getMovieMark(Movie movie) {
		MarkCountData markCount = markService.getMarkCountByTagRelatedid(TagConstant.TAG_MOVIE, movie.getId());
		Integer general = VmUtils.getLastMarkStar(movie, "general", markCount, markService.getMarkdata(TagConstant.TAG_MOVIE));
		return general / 10 + "." + general % 10;
	}
	
	@RequestMapping("/partner/bestv/areaList.xhtml")
	public String county(String citycode,long movieid,Date playdate,ModelMap model){
		if(StringUtils.isBlank(citycode)){
			citycode = this.getBestv().getDefaultCity();
		}
		if(playdate == null) playdate = DateUtil.getCurDate();
		List<County> countyList = placeService.getCountyByCityCode(citycode);
		List<Long> cinemaIdList = mcpService.getPlayCinemaIdList(citycode, movieid, playdate);
		List<Cinema> cinemas = daoService.getObjectList(Cinema.class, cinemaIdList);
		Map<String,Integer> countyCinemasCount = new HashMap<String,Integer>();
		for(Cinema cinema:cinemas){
			if(countyCinemasCount.get(cinema.getCountycode()) == null){
				countyCinemasCount.put(cinema.getCountycode(), 1);
			}else{
				countyCinemasCount.put(cinema.getCountycode(), countyCinemasCount.get(cinema.getCountycode()) + 1);
			}
		}
		model.put("countyList", countyList);
		model.put("countyCinemasCount", countyCinemasCount);
		model.put("movieid", movieid);
		model.put("playdate", DateUtil.format(playdate, "yyyy-MM-dd"));
		return "partner/bestv/areaList.vm";
	}
	
	@RequestMapping("/partner/bestv/cinemaList.xhtml")
	public String county(String countyCode,Date playdate,long movieid,String countyname,Integer pageNo,int pageSize,ModelMap model){
		if(pageNo == null){
			pageNo = 0;
		}
		if(pageSize > 3){
			pageSize = 3;
		}
		List<Long> cinemaIdList = mcpService.getPlayCinemaIdListByCountycode(countyCode, movieid, playdate);
		List<Cinema> cinemas = daoService.getObjectList(Cinema.class, cinemaIdList);
		PageUtil pageUtil = new PageUtil(cinemas.size(),pageSize,pageNo,"/partner/bestv/cinemaList.xhtml", true, true);
		Map<String,String> params = new HashMap<String,String>();
		params.put("pageSize", pageSize + "");
		params.put("movieid", movieid + "");
		params.put("countyCode", countyCode);
		params.put("countyname", countyname);
		params.put("playdate", DateUtil.format(playdate, "yyyy-MM-dd"));
		pageUtil.initPageInfo(params);
		Collections.sort(cinemas, new PropertyComparator("generalmark", false, false));
		model.put("cinemaSize", cinemas.size());
		model.put("cinemas", BeanUtil.getSubList(cinemas, pageNo * pageSize, pageSize));
		model.put("countyname", countyname);
		model.put("countyCode", countyCode);
		model.put("movieid", movieid);
		model.put("playdate", DateUtil.format(playdate, "yyyy-MM-dd"));
		model.put("pageUtil", pageUtil);
		return "partner/bestv/cinemaList.vm";
	}
	
	@RequestMapping("/partner/bestv/playList.xhtml")
	public String playList(long cinemaId,long movieid,ModelMap model,Date playdate,String countyCode,String countyname){
		Cinema cinema = this.daoService.getObject(Cinema.class, cinemaId);
		String cacheKey = CacheConstant.KEY_CUR_MPI_CACHENAME + cinemaId + movieid + DateUtil.format(playdate, "yyyy-MM-dd");
		List<MoviePlayItem> playItemList = (List<MoviePlayItem>) cacheService.get(CacheConstant.REGION_TENMIN, cacheKey);
		List<Date> dateList = openPlayService.getMovieOfCinemaOpenDateList(cinemaId, movieid);
		if (VmUtils.isEmptyList(playItemList)) {
			playItemList = mcpService.getCurMpiList(cinemaId, movieid, playdate);
			while(VmUtils.isEmptyList(playItemList)) {
				for(Date curDate : dateList){
					if(DateUtil.format(curDate, "yyyy-MM-dd").equals(DateUtil.format(playdate, "yyyy-MM-dd"))){
						continue;
					}
					playItemList = mcpService.getCurMpiList(cinemaId, movieid, curDate);
					playdate = curDate;
				}
			}
			cacheKey = CacheConstant.KEY_CUR_MPI_CACHENAME + cinemaId + movieid + DateUtil.format(playdate, "yyyy-MM-dd");
			cacheService.set(CacheConstant.REGION_TENMIN, cacheKey, playItemList);
		}
		playItemList = MoviePlayItem.getCurrent(playdate, playItemList);
		Map<Long, OpenPlayItem> opiMap = new HashMap<Long, OpenPlayItem>();
		for (MoviePlayItem mpi : playItemList) {
			opiMap.put(mpi.getId(), daoService.getObjectByUkey(OpenPlayItem.class, "mpid", mpi.getId(), true));
		}
		model.put("opiMap", opiMap);
		model.put("curMpiList", playItemList);
		model.put("openDateList", dateList);
		model.put("cinema", cinema);
		model.put("movieid", movieid);
		model.put("playdate", DateUtil.format(playdate, "yyyy-MM-dd"));
		model.put("countyname", countyname);
		model.put("countyCode", countyCode);
		return "partner/bestv/playList.vm";
	}
	
	@RequestMapping("/partner/bestv/unopened.xhtml")
	public String unopened(String type,String movieid,long cinemaId,Date playdate,String countyCode,String countyname,ModelMap model){
		model.put("movieid", movieid);
		model.put("cinemaId", cinemaId);
		model.put("countyname", countyname);
		model.put("countyCode", countyCode);
		model.put("playdate", DateUtil.format(playdate, "yyyy-MM-dd"));
		if("iphone".equals(type)){
			return "partner/bestv/iphone.vm";
		}else if("android".equals(type)){
			return "partner/bestv/android.vm";
		}
		return "partner/bestv/unopened.vm";
	}
}
