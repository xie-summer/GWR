package com.gewara.web.action.movie;

import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import com.gewara.json.PageView;
import com.gewara.model.movie.Cinema;
import com.gewara.model.movie.Movie;
import com.gewara.untrans.PageParams;
import com.gewara.util.DateUtil;
import com.gewara.util.WebUtils;


@Controller
public class MoviePlayItemController extends BasePlayItemController {
	
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
	/**
	 * 只用于缓存更新
	 * @param playdate
	 * @param movieid
	 * @param cid
	 * @param model
	 * @return
	 */
	@RequestMapping("/movie/ajax/getPlayItemPage.xhtml")
	public String getPlayItemPage(HttpServletRequest request, String fyrq, Long movieid, Long cid, ModelMap model){
		Date playdate = getPlaydate(fyrq);
		String ip = WebUtils.getRemoteIp(request);
		PageView pageView = getPlayItemPageStr(pageCacheService.isUseCache(request), playdate, movieid, cid, ip);
		model.put("pageView", pageView);
		return "pageView.vm";
	}
	@RequestMapping("/movie/ajax/getPlayItem.xhtml")
	public String getPlayItem(String fyrq, Long movieid, Long cid, boolean isView, ModelMap model,
			HttpServletRequest request, HttpServletResponse response){
		Cinema cinema = daoService.getObject(Cinema.class, cid);
		if(cinema == null) return showJsonError(model, "该影院不存在或被删除！");
		Movie movie = daoService.getObject(Movie.class, movieid);
		if (movie == null) return show404(model, "电影不存在或已经删除！");
		String citycode = cinema.getCitycode();
		if(StringUtils.isBlank(citycode)){
			citycode = WebUtils.getAndSetDefault(request, response);
		}
		Date playdate = getPlaydate(fyrq);
		String ip = WebUtils.getRemoteIp(request);
		PageView pageView = getPlayItemPageStr(pageCacheService.isUseCache(request), playdate, movieid, cid, ip);
		if(isView){
			return pageView.getContent();
		}
		return showJsonSuccess(model, pageView.getContent());
	}
	
	@RequestMapping("/movie/ajax/item.xhtml")
	public String movieItem(String fyrq, Long movieid, Long cid, ModelMap model, HttpServletRequest request, HttpServletResponse response){
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
		if (useCache) {// 先使用缓存
			PageParams params = new PageParams();
			params.addDateStr("fyrq", fyrq);
			params.addLong("movieid", movieid);
			PageView pageView = pageCacheService.getPageView(request, "movie/ajax/item.xhtml", params, citycode);
			if (pageView != null) {
				model.put("pageView", pageView);
				return "pageView.vm";
			}
		}
		Date playdate = getPlaydate(fyrq);
		List<Date> dateList = mcpService.getCurMoviePlayDate2(citycode, movieid);
		if (!dateList.isEmpty() && playdate.before(dateList.get(0))) {
			playdate = dateList.get(0);
		}
		model.put("dateList", dateList);
		model.put("fyrq", DateUtil.formatDate(playdate));
		model.put("fyrqDate", playdate);
		String ip = WebUtils.getRemoteIp(request);
		addMoviePlayItem(useCache, playdate, citycode, movieid, ip, model);
		return "movie/moviePlayItemList.vm";
	}
}
