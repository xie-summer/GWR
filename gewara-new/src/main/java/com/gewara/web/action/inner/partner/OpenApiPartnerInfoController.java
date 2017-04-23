package com.gewara.web.action.inner.partner;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.support.PropertyComparator;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import com.gewara.constant.AdminCityContant;
import com.gewara.constant.ApiConstant;
import com.gewara.constant.DiaryConstant;
import com.gewara.constant.TagConstant;
import com.gewara.helper.ticket.CloseRuleOpiFilter;
import com.gewara.model.api.ApiUser;
import com.gewara.model.api.ApiUserExtra;
import com.gewara.model.bbs.Diary;
import com.gewara.model.common.County;
import com.gewara.model.common.Indexarea;
import com.gewara.model.content.News;
import com.gewara.model.content.NewsPage;
import com.gewara.model.movie.Cinema;
import com.gewara.model.movie.CinemaProfile;
import com.gewara.model.movie.Movie;
import com.gewara.model.partner.PartnerCloseRule;
import com.gewara.service.bbs.BlogService;
import com.gewara.service.bbs.DiaryService;
import com.gewara.service.content.NewsService;
import com.gewara.util.BeanUtil;
import com.gewara.util.VmUtils;
import com.gewara.web.action.api.ApiAuth;
import com.gewara.web.filter.OpenApiPartnerAuthenticationFilter;
import com.gewara.xmlbind.bbs.Comment;
@Controller
public class OpenApiPartnerInfoController extends BaseOpenApiPartnerController{
	@Autowired@Qualifier("diaryService")
	private DiaryService diaryService;
	@Autowired@Qualifier("blogService")
	private BlogService blogService;
	@Autowired@Qualifier("newsService")
	private NewsService newsService;
	//城市列表API:提供城市名称与编号列表
	@RequestMapping("/openapi/partner/cityList.xhtml")
	public String cityList(ModelMap model) {
		model.put("cityMap", AdminCityContant.getCitycode2CitynameMap());
		return getXmlView(model, "inner/partner/cityList.vm");
	}
	//城市列表API:提供城市名称与编号列表
	@RequestMapping("/openapi/partner/openPartnerCityList.xhtml")
	public String partnerCityList(ModelMap model) {
		ApiAuth auth = OpenApiPartnerAuthenticationFilter.getApiAuth();
		ApiUser partner = auth.getApiUser();
		Map<String, String> allCityMap = AdminCityContant.getCitycode2CitynameMap();
		Map<String, String> cityMap = new LinkedHashMap<String, String>();
		if(StringUtils.equals(partner.getCitycode(), AdminCityContant.CITYCODE_ALL)){
			cityMap.putAll(allCityMap);
		}else if(StringUtils.isNotBlank(partner.getCitycode())){
			String[] citycodes = StringUtils.split(partner.getCitycode(), ",");
			for(String citycode : citycodes){
				cityMap.put(citycode, allCityMap.get(citycode));
			}
		}
		model.put("cityMap", cityMap);
		return getXmlView(model, "inner/partner/cityList.vm");
	}
	//区域列表API:提供输入城市的区列表的api
	@RequestMapping("/openapi/partner/countyList.xhtml")
	public String countyList(String citycode, ModelMap model) {
		List<County> countyList = placeService.getCountyByCityCode(citycode);
		model.put("countyList", countyList);
		return getXmlView(model, "inner/partner/countyList.vm");
	}
	
	//商圈列表API提供当前输入区号对应的商圈列表
	@RequestMapping("/openapi/partner/indexareaList.xhtml")
	public String indexareaList(String countycode, ModelMap model) {
		List<Indexarea> indexareaList = placeService.getIndexareaByCountyCode(countycode);
		model.put("indexareaList", indexareaList);
		return getXmlView(model, "inner/partner/indexareaList.vm");
	}
	
	//影片详情
	@RequestMapping("/openapi/partner/movieDetail.xhtml")
	public String movieDetail(Long movieid, ModelMap model, HttpServletRequest request) {
		Movie movie = daoService.getObject(Movie.class, movieid);
		if(movie == null) return getErrorXmlView(model, ApiConstant.CODE_DATA_ERROR, "影片信息不存在！");
		ApiAuth auth = OpenApiPartnerAuthenticationFilter.getApiAuth();
		ApiUserExtra extra = auth.getUserExtra();
		getMovieMap(movie, extra, model, request);
		putMovieNode(model);
		return getOpenApiXmlDetail(model);
	}
	//影院详情接口
	@RequestMapping("/openapi/partner/cinemaDetail.xhtml")
	public String cinemaDetail(Long cinemaid, ModelMap model, HttpServletRequest request) {
		Cinema cinema = daoService.getObject(Cinema.class, cinemaid);
		if(cinema == null) return getErrorXmlView(model, ApiConstant.CODE_DATA_ERROR, "影院信息不存在！");
		ApiAuth auth = OpenApiPartnerAuthenticationFilter.getApiAuth();
		ApiUserExtra extra = auth.getUserExtra();
		CinemaProfile profile = daoService.getObject(CinemaProfile.class, cinema.getId());
		getCinemaMap(cinema, profile, extra, model, request);
		putCinemaNode(model);
		return getOpenApiXmlDetail(model);
	}
	
	//即将上映的影片
	@RequestMapping("/openapi/partner/futureMovieList.xhtml")
	public String futureMovieList(Integer from, Integer maxnum, ModelMap model, HttpServletRequest request) {
		if(from == null) from=0;
		if(maxnum == null) maxnum=100;
		ApiAuth auth = OpenApiPartnerAuthenticationFilter.getApiAuth();
		ApiUser partner = auth.getApiUser();
		List<Movie>  movieList = mcpService.getFutureMovieList(from, maxnum,null);
		List<PartnerCloseRule> pcrList = partnerService.getCloseRuleList();
		CloseRuleOpiFilter filter = new CloseRuleOpiFilter(partner, pcrList);
		filter.filterMovie(movieList);
		ApiUserExtra extra = auth.getUserExtra();
		getMovieListMap(movieList, extra, model, request);
		putMovieListNode(model);
		return getOpenApiXmlList(model);
	}
	//根据影片名称搜索电影
	@RequestMapping("/openapi/partner/getMovieListByMoviename.xhtml")
	public String futureMovieList(String moviename, Integer from, Integer maxnum, ModelMap model, HttpServletRequest request) {
		if(from == null) from=0;
		if(maxnum == null) maxnum=100;
		ApiAuth auth = OpenApiPartnerAuthenticationFilter.getApiAuth();
		List<Long>  movieidList = mcpService.getMovieIdByMoviename(moviename);
		List<Movie> movieList = daoService.getObjectList(Movie.class, movieidList);
		Collections.sort(movieList, new PropertyComparator("id", true, false));
		movieList = BeanUtil.getSubList(movieList, from, maxnum);
		ApiUserExtra extra = auth.getUserExtra();
		getMovieListMap(movieList, extra, model, request);
		putMovieListNode(model);
		return getOpenApiXmlList(model);
	}
	//影评列表
	@RequestMapping("/openapi/partner/movieDiaryList.xhtml")
	public String movieDiaryList(Long movieid, String citycode, Integer from, Integer maxnum, ModelMap model, HttpServletRequest request) {
		if(from == null) from = 0;
		if(maxnum == null) maxnum = 50;
		List<Diary> diaryList = diaryService.getDiaryList(Diary.class, citycode, DiaryConstant.DIARY_TYPE_COMMENT, TagConstant.TAG_MOVIE, movieid, from, maxnum);
		getMovieDiaryListMap(diaryList, model, request);
		putDiaryListNode(model);
		return getOpenApiXmlList(model);
	}
	
	//影评
	@RequestMapping("/openapi/partner/movieDiaryDetail.xhtml")
	public String movieDiaryDetail(Long diaryid, ModelMap model, HttpServletRequest request) {
		Diary diary = daoService.getObject(Diary.class, diaryid);
		if(diary==null){
			return getErrorXmlView(model, ApiConstant.CODE_DATA_ERROR, "影评不存在！"); 
		}
		String content = blogService.getDiaryBody(diaryid);
		getMovieDiaryMap(diary, content, model, request);
		putDiaryNode(model);
		return getOpenApiXmlDetail(model);
	}
	
	//哇啦列表
	@RequestMapping("/openapi/partner/movieCommentList.xhtml")
	public String commentList(Long movieid, Integer from, Integer maxnum, Timestamp addtime, ModelMap model, HttpServletRequest request) {
		if(from == null) from = 0;
		if(maxnum == null) maxnum = 50;
		if(maxnum>50) maxnum = 50;
		List<Comment> commentList = new ArrayList<Comment>();
		if(addtime!=null){
			commentList = commentService.getCommentList(TagConstant.TAG_MOVIE, movieid, null, null, null, addtime, null, from, maxnum);
		}else {
			commentList = commentService.getCommentList(TagConstant.TAG_MOVIE, movieid, null, null, null, from, maxnum);
		}
		getCommentListMap(commentList, model, request);
		putCommentListNode(model);
		return getOpenApiXmlList(model);
	}
	@RequestMapping("/openapi/partner/cinemaCommentList.xhtml")
	public String cinemaCommentList(Long cinemaid, Integer from, Integer maxnum, Timestamp addtime, ModelMap model, HttpServletRequest request) {
		if(from == null) from = 0;
		if(maxnum == null) maxnum = 50;
		if(maxnum>50) maxnum = 50;
		List<Comment> commentList = new ArrayList<Comment>();
		if(addtime!=null){
			commentList = commentService.getCommentList(TagConstant.TAG_CINEMA, cinemaid, null, null, null, addtime, null, from, maxnum);
		}else {
			commentList = commentService.getCommentList(TagConstant.TAG_CINEMA, cinemaid, null, null, null, from, maxnum);
		}
		getCommentListMap(commentList, model, request);
		putCommentListNode(model);
		return getOpenApiXmlList(model);
	}
	//哇啦
	@RequestMapping("/openapi/partner/comment.xhtml")
	public String comment(Long commentid, ModelMap model, HttpServletRequest request) {
		Comment comment = commentService.getCommentById(commentid);
		if(comment==null){
			return getErrorXmlView(model, ApiConstant.CODE_DATA_ERROR, "评论不存在！"); 
		}
		getCommentMap(comment, model, request);
		putCommentNode(model);
		return getOpenApiXmlDetail(model);
	}
	
	//新闻详情
	@RequestMapping("/openapi/partner/newsDetail.xhtml")
	public String newsDetail(Long newsid, ModelMap model, HttpServletRequest request) {
		News news = daoService.getObject(News.class, newsid);
		if(news == null) return getErrorXmlView(model, ApiConstant.CODE_DATA_ERROR, "咨询信息不存在！");
		String content = news.getContent();
		if(news.getPagesize()>1){
			for(int i=2;i<=news.getPagesize();i++){
				NewsPage np = newsService.getNewsPageByNewsidAndPageno(news.getId(), i);
				if(np!=null && StringUtils.isNotBlank(np.getContent())) {
					content = content + "<br /><br />" +np.getContent();
				}
			}
		}
		Map<String, Object> resMap = getNewsMap(news);
		model.put("content", VmUtils.getHtmlText(content, 10000));
		resMap.put("content", content);
		initField(model, request);
		model.put("root", "news");
		model.put("resMap", resMap);
		return getOpenApiXmlDetail(model);
	}
	//新闻列表
	@RequestMapping("/openapi/partner/newsList.xhtml")
	public String newsList(String citycode, String tag, Long relatedid, String category, Long categoryid, 
			Integer from, Integer maxnum, ModelMap model, HttpServletRequest request){
		if(from == null) from = 0;
		if(maxnum == null || maxnum>100) maxnum = 20;
		List<News> newsList = newsService.getNewsList(citycode, tag, relatedid, category, categoryid, null, from, maxnum);
		initField(model, request);
		getNewsListMap(newsList, model, request);
		return getOpenApiXmlDetail(model);
	}
}
