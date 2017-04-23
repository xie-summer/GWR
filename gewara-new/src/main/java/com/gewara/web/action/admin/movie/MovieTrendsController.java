package com.gewara.web.action.admin.movie;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import com.gewara.json.MovieTrendsCount;
import com.gewara.model.bbs.MemberMark;
import com.gewara.model.movie.Movie;
import com.gewara.untrans.MovieTrendsService;
import com.gewara.util.DateUtil;
import com.gewara.web.action.admin.BaseAdminController;
import com.gewara.web.util.PageUtil;

/**
 * 预测即将上映电影在未来月的情况的基础数据需求
 */
@Controller
public class MovieTrendsController extends BaseAdminController {
	// TODO 20130621 影评数、用户评分改为增量，待优化

	@Autowired
	@Qualifier("movieTrendsService")
	private MovieTrendsService movieTrendsService;
	
	
	private static final int ROWS_PER_PAGE = 5000;
	private static final int EXPROT_ROWS_PER_PAGE = 20;

	// 根据日期查询影片基础数据-首页
	@RequestMapping("/admin/trends/index.xhtml")
	public String queryMovieTrendsCountByDate(Date queryDate, ModelMap model) {
		model.put("rowsPerPage", EXPROT_ROWS_PER_PAGE);
		String queryDateStr = movieTrendsService.getDefaultQueryDate(queryDate);
		List<Map>/*List<MovieTrendsCount>*/ trendsCountList = movieTrendsService.queryMovieTrendsCountByDate(queryDateStr);
		model.put("queryDate", queryDateStr);
		model.put("trendsCountList", trendsCountList);
		return "admin/movie/trends/index.vm";
	}

	// 用户评分明细
	@RequestMapping("/admin/trends/memberMarkList.xhtml")
	public String queryMemberMarkByMovieid(Long movieid, String flag, String queryModel,Date queryDate, Integer pageNo, String ctype, HttpServletResponse response, ModelMap model) {
		String vmPage = "admin/movie/trends/memberMark.vm";
		model.put("movieid", movieid);
		model.put("queryModel", queryModel);
		Movie movie = daoService.getObject(Movie.class, movieid);
		if (movie == null) {
			model.put("msg", "影片不存在");
			return vmPage;
		}
		model.put("movie", movie);
		
		if(pageNo == null)pageNo = 0;
		int rowsPerPage = ROWS_PER_PAGE;
		if(StringUtils.isNotBlank(ctype)){
			pageNo = pageNo -1;
			rowsPerPage = EXPROT_ROWS_PER_PAGE;
		}
		int firstPerPage = pageNo * rowsPerPage;
		
		// 初始不显示数据
		if (StringUtils.equals(queryModel, "N")) {
			return vmPage;
		}
		Long memberMarkCount = movieTrendsService.getMemberMarkCountByMovieid(movieid, flag, queryDate);
		int memberMarkCountInt = Integer.parseInt(String.valueOf(memberMarkCount));
		
		List<MemberMark> memberMarkList = movieTrendsService.getMemberMarkListByMovieid(movieid, flag, queryDate,firstPerPage, rowsPerPage);
		
		model.put("memberMarkList", memberMarkList);
		PageUtil pageUtil = new PageUtil(memberMarkCountInt, rowsPerPage, pageNo, "admin/movie/trends/memberMarkList.xhtml", true, true);
		Map params = new HashMap();
		params.put("pageNo", pageNo);
		params.put("movieid", movieid);
		params.put("flag", flag);
		params.put("queryDate", DateUtil.formatDate(queryDate));
		pageUtil.initPageInfo(params);
		model.put("pageUtil", pageUtil);
		
		model.put("memberMarkCount", memberMarkCount);
		model.put("flag", flag);
		
		if(StringUtils.isNotBlank(ctype)){
			download("xls", response);
			return "admin/movie/trends/exportsMemberMark.vm";
		}
		return vmPage;
	}
	
	// 影片ID数据详情页
	@RequestMapping("/admin/trends/movieTrends.xhtml")
	public String movieTrendsDetail(Long movieid, Date startDate, Date endDate, ModelMap model) {
		model.put("movieid", movieid);
		String resultVm = "admin/movie/trends/movieTrends.vm";
		if (movieid == null) {
			return resultVm;
		}
		if (startDate == null) {
			if (endDate == null) {
				endDate = new Date();
			}
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(endDate);
			calendar.set(Calendar.MONTH, calendar.get(Calendar.MONTH) - 1);
			startDate = calendar.getTime();
		} 
		if (startDate != null && endDate == null) {
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(startDate);
			calendar.set(Calendar.MONTH, calendar.get(Calendar.MONTH) + 1);
			endDate = calendar.getTime();
		}
		model.put("startDate", startDate);
		model.put("endDate", endDate);
		model.put("movieid", movieid);
		List<MovieTrendsCount> resultList =  movieTrendsService.getMovieTrendsListByMovieIdDate(movieid, startDate, endDate);
		model.put("trendsCountList", resultList);
		return resultVm;
	}
}
