package com.gewara.web.action.api2.movie;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import com.gewara.Config;
import com.gewara.constant.ApiConstant;
import com.gewara.constant.sys.CacheConstant;
import com.gewara.helper.ticket.CloseRuleOpiFilter;
import com.gewara.helper.ticket.OpiFilter;
import com.gewara.model.movie.Movie;
import com.gewara.model.movie.MoviePlayItem;
import com.gewara.model.partner.PartnerCloseRule;
import com.gewara.model.ticket.OpenPlayItem;
import com.gewara.service.movie.MCPService;
import com.gewara.service.partner.PartnerService;
import com.gewara.untrans.CacheService;
import com.gewara.util.DateUtil;
import com.gewara.web.action.api.ApiAuth;
import com.gewara.web.action.api.BaseApiController;
import com.gewara.web.filter.NewApiAuthenticationFilter;

/**
 * 影院API 
 * @author taiqichao
 *
 */
@Controller
public class Api2BaiDuCinemaController extends BaseApiController{
	@Autowired
	private Config config;
	
	@Autowired
	@Qualifier("cacheService")
	private CacheService cacheService;

	@Autowired
	@Qualifier("mcpService")
	private MCPService mcpService;
	@Autowired@Qualifier("partnerService")
	private PartnerService partnerService;
	/**
	 * 某影院热映影片2天排片信息
	 * @param cinemaid
	 * @param model
	 * @return
	 */
	@RequestMapping("/api2/cinema/hotMoivePlayItem.xhtml")
	public String cinemaHotMoviePlayItem(Long cinemaid, Timestamp playtime, ModelMap model) {
		String view="common/directJson.vm";
		if(playtime==null){
			String errorStr="{\"code\":\""+ApiConstant.CODE_PARAM_ERROR+"\",\"error\":\"请传入playtime\"}";
			model.put("jsonStr", errorStr);
			return view;
		}
		if (cinemaid == null) {
			String errorStr="{\"code\":\""+ApiConstant.CODE_PARAM_ERROR+"\",\"error\":\"请传入影院id\"}";
			model.put("jsonStr", errorStr);
			return view;
		}
		//缓存
		String key = CacheConstant.buildKey("api2CinemaHotMoviePlayItem", cinemaid, DateUtil.formatDate(playtime));
		String jsonCache=(String)cacheService.get(CacheConstant.REGION_HALFHOUR, key);
		if(StringUtils.isNotBlank(jsonCache)){
			model.put("jsonStr", jsonCache);
			return view;
		}

		CinemaPlayItem playItem = new CinemaPlayItem();
		List<CinemaPlayItem.Moive> data=new ArrayList<CinemaPlayItem.Moive>();
		Date startDate = DateUtil.getBeginningTimeOfDay(playtime);
		List<Movie> movieList = mcpService.getCurMovieListByCinemaIdAndDate(cinemaid, startDate);
		List<PartnerCloseRule> pcrList = partnerService.getCloseRuleList();
		ApiAuth auth = NewApiAuthenticationFilter.getApiAuth();
		OpiFilter filter = new CloseRuleOpiFilter(auth.getApiUser(), pcrList);
		for (Movie movie : movieList) {
			CinemaPlayItem.Moive vmovie = new CinemaPlayItem.Moive();
			vmovie.setId(movie.getId());
			vmovie.setActors(movie.getActors());
			vmovie.setDirector(movie.getDirector());
			vmovie.setLogo(config.getString("picPath")+ "cw96h128/" + movie.getLimg());
			vmovie.setMoviename(movie.getMoviename());
			vmovie.setReleasedate(movie.getReleasedate());
			vmovie.setType(movie.getType());
			vmovie.setVideolen(movie.getVideolen());

			List<MoviePlayItem> mpiList = mcpService.getCinemaMpiList(cinemaid, movie.getId(), startDate);
			List<CinemaPlayItem.TimeTable> timeTables = new ArrayList<CinemaPlayItem.TimeTable>();
			for (MoviePlayItem item : mpiList) {
				OpenPlayItem opi = daoService.getObjectByUkey(OpenPlayItem.class, "mpid", item.getId(), true);
				if (opi == null || !opi.isOrder() || filter.excludeOpi(opi)) {
					continue;
				}
				CinemaPlayItem.TimeTable timeTable = new CinemaPlayItem.TimeTable();
				timeTable.setId(item.getId());
				timeTable.setEdition(item.getEdition());
				timeTable.setLanguage(item.getLanguage());
				timeTable.setOpiurl(config.getAbsPath()+ config.getBasePath()+ "cinema/order/step1.shtml?mpid="+ opi.getMpid());
				timeTable.setPrice(opi.getGewaprice());
				timeTable.setPlaydate(item.getPlaydate());
				timeTable.setPlayroom(item.getPlayroom());
				timeTable.setPlaytime(item.getPlaytime());
				timeTable.setOrigin_price(opi.getPrice());
				timeTables.add(timeTable);
			}
			if(timeTables.size()>0){
				vmovie.setTimeTables(timeTables);
				data.add(vmovie);
			}
		}
		playItem.setData(data);
		ObjectMapper mapper=new ObjectMapper();
		mapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd"));
		try {
			String jsonStr=mapper.writeValueAsString(playItem);
			cacheService.set(CacheConstant.REGION_HALFHOUR, key, jsonStr);
			model.put("jsonStr", jsonStr);
		} catch (Exception e) {
		}
		return view;
	}
}
