package com.gewara.web.action.admin.movie;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.gewara.constant.PointConstant;
import com.gewara.constant.TagConstant;
import com.gewara.model.acl.User;
import com.gewara.model.bbs.MemberMark;
import com.gewara.model.content.Video;
import com.gewara.model.movie.Movie;
import com.gewara.model.movie.MovieVideo;
import com.gewara.model.movie.TempMovie;
import com.gewara.service.bbs.MarkService;
import com.gewara.service.content.VideoService;
import com.gewara.service.member.PointService;
import com.gewara.service.movie.MCPService;
import com.gewara.support.ServiceHelper;
import com.gewara.untrans.SearchService;
import com.gewara.util.BeanUtil;
import com.gewara.util.BindUtils;
import com.gewara.util.ChangeEntry;
import com.gewara.util.DateUtil;
import com.gewara.util.HttpResult;
import com.gewara.util.HttpUtils;
import com.gewara.util.IQiYiAuthUtils;
import com.gewara.util.JsonUtils;
import com.gewara.util.PinYinUtils;
import com.gewara.util.ValidateUtil;
import com.gewara.util.VmUtils;
import com.gewara.util.YoukuApiUtil;
import com.gewara.web.action.admin.BaseAdminController;
import com.gewara.web.util.PageUtil;
import com.gewara.xmlbind.gym.RemoteCoach;
import com.gewara.xmlbind.partner.YoukuVideo;

/**
 *    @function ku6.com 接口, 供编辑查询视频 
 * 	@author bob.hu
 *		@date	2011-10-18 18:13:17
 */
@Controller
public class MovieAdminController extends BaseAdminController {
	@Autowired@Qualifier("mcpService")
	private MCPService mcpService;
	public void setMcpService(MCPService mcpService) {
		this.mcpService = mcpService;
	}
	@Autowired@Qualifier("pointService")
	private PointService pointService;
	public void setPointService(PointService pointService) {
		this.pointService = pointService;
	}

	@Autowired@Qualifier("videoService")
	private VideoService videoService;
	public void setVideoService(VideoService videoService) {
		this.videoService = videoService;
	}
	@Autowired@Qualifier("markService")
	private MarkService markService;
	public void setMarkService(MarkService markService) {
		this.markService = markService;
	}
	@Autowired@Qualifier("searchService")
	private SearchService searchService;
	@RequestMapping("/admin/movie/movieList.xhtml")
	public String movieList(@RequestParam(required=false, value="fromDate")Date fromDate, 
			@RequestParam(required=false, value="toDate")Date toDate, 
			@RequestParam(required=false, value="hot")String hot, 
			@RequestParam(required=false, value="moviename")String moviename, ModelMap model) throws Exception {
		DetachedCriteria query = DetachedCriteria.forClass(Movie.class);
		if(StringUtils.isNotBlank(hot)){
			query.add(Restrictions.gt("hotvalue", 10));
		}else if(StringUtils.isNotBlank(moviename)){
			query.add(Restrictions.ilike("moviename", moviename, MatchMode.ANYWHERE));
		}else if(fromDate == null && toDate == null){//没有时间,只列出近半月
			fromDate = DateUtil.addDay(new Date(), -15);
			query.add(Restrictions.ge("addtime", fromDate));
		}else{
			if(fromDate != null) query.add(Restrictions.ge("addtime", fromDate));
			if(toDate != null) query.add(Restrictions.le("addtime", toDate));
		}
		query.addOrder(Order.desc("addtime"));
		query.addOrder(Order.asc("moviename"));
		List movieList = hibernateTemplate.findByCriteria(query);
		model.put("movieList", movieList);
		if(!VmUtils.isEmptyList(movieList)){
			DetachedCriteria relationsQuery = DetachedCriteria.forClass(Video.class);
			relationsQuery.add(Restrictions.eq("tag", Video.VIDEOTYPE_FILM));
			relationsQuery.add(Restrictions.in("relatedid",  BeanUtil.getBeanPropertyList(movieList, Long.class, "id", true)));
			List<Video> relations = hibernateTemplate.findByCriteria(relationsQuery);
			Map relationVideoMap = BeanUtil.beanListToMap(relations, "relatedid");
			model.put("relationVideoMap", relationVideoMap);
			model.put("relationVideoS", relationVideoMap.keySet());
		}
		return "admin/movie/movieList.vm";
	}
	
	@RequestMapping("/admin/movie/iQiYiMovieList.xhtml")
	public String iQiYiMovieList(@RequestParam(required=false, value="fromDate")Date fromDate, 
			@RequestParam(required=false, value="toDate")Date toDate,  ModelMap model) throws Exception {
		if(fromDate == null){//没有时间,只列出近半月
			fromDate = DateUtil.addDay(new Date(), -15);
		}
		if(toDate == null){
			toDate = new Date();
		}
		String result = IQiYiAuthUtils.getVideoList(fromDate, toDate, 0, 100, "1", "1");
		if(StringUtils.isNotBlank(result)){
			Map vedions = JsonUtils.readJsonToMap(result);
			model.put("vedions", vedions.get("data"));
			model.put("iQiYi", "true");
		}
		return "admin/movie/movieList.vm";
	}
	
	@RequestMapping("/admin/movie/modifyMovieDetail.xhtml")
	public String modifyMovie(@RequestParam(required=false, value="mid")Long mid, ModelMap model){
		if (mid != null) {
			Movie movie=daoService.getObject(Movie.class, mid);
			model.put("movie", movie);
			List<MovieVideo> mvideoList = daoService.getObjectListByField(MovieVideo.class, "movieid", mid);
			if(mvideoList.size()>0) model.put("movieVideoId", mvideoList.get(0).getVideoid());
		}
		return "admin/movie/movieDetailForm.vm";
	}
	@RequestMapping("/admin/movie/copyTempMovie.xhtml")
	public String copyToNewMovie(@RequestParam("tmId")Long tmId, ModelMap model){
		TempMovie tm = daoService.getObject(TempMovie.class, tmId);
		Movie movie = new Movie(tm.getMoviename());
		movie.setAddtime(tm.getAddtime());
		movie.setContent(tm.getContent());
		movie.setMovietype(tm.getType());
		movie.setState(tm.getState());
		movie.setActors(tm.getActors());
		movie.setDirector(tm.getDirector());
		movie.setLogo(tm.getLogo());
		movie.setPlaydate(DateUtil.formatDate(tm.getReleaseDate()));
		model.put("movie", movie);
		return "admin/movie/movieDetailForm.vm";
	}
	
	@RequestMapping("/admin/movie/setMovieHotValue.xhtml")
	public String setMovieHotValue(Long movieId, Integer value, ModelMap model) {
		mcpService.updateMovieHotValue(movieId, value);
		return showJsonSuccess(model);
	}
	//保存电影
	@RequestMapping("/admin/movie/saveMovie.xhtml")
	public String saveMovie(Long movieId, String moviename, String videoid, String crowd, HttpServletRequest request, ModelMap model){
		Movie movie = null;
		if(movieId!=null) {
			movie = daoService.getObject(Movie.class, movieId);
		}else{
			movie = new Movie(moviename);
		}
		if(!StringUtils.equals(movie.getMoviename(), moviename) || movieId == null){
			Integer count = mcpService.getMovieCountByName(movieId,moviename);
			if(count > 0) return showJsonError(model, "电影名重复！");
		}
		ChangeEntry changeEntry = new ChangeEntry(movie);
		BindUtils.bindData(movie, request.getParameterMap());
		if(StringUtils.isNotBlank(movie.getPlaydate())){
			Date year = DateUtil.parseDate(movie.getPlaydate(), "yyyy");
			Date ym = DateUtil.parseDate(movie.getPlaydate(), "yyyy-MM");
			int pdLength = movie.getPlaydate().length();
			if(!(DateUtil.isValidDate(movie.getPlaydate()) || (ym != null && pdLength == 7) || (year != null && pdLength == 4))){
				return showJsonError(model, "电影放映日期格式不正确！");
			}
		}
		//验证内容
		String msg = ValidateUtil.validateNewsContent(null, movie.getContent());
		if(StringUtils.isNotBlank(msg))return showJsonError(model, msg);
		if(movie.getAddtime()==null) movie.setAddtime(new Timestamp(System.currentTimeMillis()));
		if(movie.getPinyin()==null) {
			movie.setPinyin(PinYinUtils.getPinyin(movie.getMoviename()));
		}
		movie.setUpdatetime(new Timestamp(System.currentTimeMillis()));
		if(StringUtils.isNotBlank(crowd)) {
			movie.setOtherinfo(JsonUtils.addJsonKeyValue(movie.getOtherinfo(), TagConstant.TAG_CROWD, crowd));
		}else {
			movie.setOtherinfo(JsonUtils.removeJsonKeyValue(movie.getOtherinfo(), TagConstant.TAG_CROWD));
		}
		if(StringUtils.isNotBlank(movie.getFlag())){
			movie.setFlag(movie.getFlag().trim().replaceAll("，", ","));
		}
		try{
			daoService.saveObject(movie);
		}catch(Exception e){
			dbLogger.warn("parameters: " + request.getParameterMap());
			return showJsonError(model,e.getMessage());
		}
		if(StringUtils.isNotBlank(videoid)){//保存视频ID
			MovieVideo mv = videoService.getMovieVideo(movie.getId());
			if(mv == null){
				mv = new MovieVideo(movie.getId(), videoid);
			}else{
				mv.setVideoid(videoid);
			}
			YoukuVideo ykvideo = YoukuApiUtil.getYoukuImg(videoid);
			if(ykvideo!=null){
				mv.setImg(ykvideo.getImg());
			}
			daoService.saveObject(mv);
		}
		monitorService.saveChangeLog(getLogonUser().getId(), Movie.class, movie.getId(),changeEntry.getChangeMap(movie));
		searchService.pushSearchKey(movie);//更新索引至索引服务器
		return showJsonSuccess(model, movie.getId()+"");
	}
	
	/**
	 * 
	 * 查看影片评分明细
	 * 
	 * */
	@RequestMapping("/admin/movie/cinemaScoreDetail.xhtml")
	public String cinemaScoreDetail(@RequestParam(required=false, value="movieId")Long movieId,String tag,String flag, ModelMap model){
		List<MemberMark> markList =  markService.getMarkList(tag, movieId, "generalmark", flag);
		model.put("list", markList);
		model.put("tag", tag);
		model.put("movieId", movieId);
		model.put("sumNum", markList.size());
		return "admin/movie/movieScoreDetail.vm";
	}
	@RequestMapping("/admin/movie/searchKu6AV.xhtml")
	public String searchKu6AV(String tag, Long relatedid, Integer pageNo, ModelMap model){
		String ku6AVURL = "http://so.ku6.com/api/v";
		String returnURL = "admin/movie/searchKu6AVList.vm";
		Object baseObject = relateService.getRelatedObject(tag, relatedid);
		String name=null;
		if(baseObject == null) return returnURL;
		if((baseObject.getClass()).equals(Movie.class)){
			name= ""+BeanUtil.get(baseObject, "moviename");
		}else if((baseObject.getClass()).equals(RemoteCoach.class)){
			name= ""+BeanUtil.get(baseObject, "coachname");
		}
		if(pageNo == null) pageNo = 0;
		
		Map params = new HashMap();
		params.put("format", "json");
		params.put("cid", "104000");
		params.put("order", "date");
		params.put("q", name);
		params.put("p", ""+pageNo);
		HttpResult code  = HttpUtils.getUrlAsString(ku6AVURL, params);
		if(code.isSuccess()){
			Map ku6data = JsonUtils.readJsonToMap(code.getResponse());
			String return_code = ""+ku6data.get("return_code");
			if(StringUtils.equals(return_code, "-1")){
				model.put("error", "出错或没有结果！");
				return returnURL;
			}
			List<Map> dataMap = (List<Map>)((Map)ku6data.get("data")).get("list");
			model.put("dataMap", dataMap);
		}
		
		// tag + relatedid = 唯一确定已关联视频
		List<Video> videoList = videoService.getVideoListByTag(tag, relatedid, 0, 200);
		List<String> urlList = BeanUtil.getBeanPropertyList(videoList, String.class, "url", true);
		model.put("urlList", urlList);
		
		model.put("name", name);
		model.put("pageNo", pageNo);
		return returnURL;
	}
	
	@RequestMapping("/admin/movie/searchAQIYIAV.xhtml")
	public String searchAQIYIAV(String tag, Long relatedid, Integer pageNo,String categoryIds, ModelMap model){
		String returnURL = "admin/movie/searchIQiYiAVList.vm";
		Object baseObject = relateService.getRelatedObject(tag, relatedid);
		String name=null;
		if(baseObject == null) return returnURL;
		if((baseObject.getClass()).equals(Movie.class)){
			name= ""+BeanUtil.get(baseObject, "moviename");
		}else if((baseObject.getClass()).equals(RemoteCoach.class)){
			name= ""+BeanUtil.get(baseObject, "coachname");
		}
		if(pageNo == null) pageNo = 0;
		String result = IQiYiAuthUtils.searchAlbum(name,null, 0, 20,categoryIds);
		if(StringUtils.isNotBlank(result)){
			Map ablums = JsonUtils.readJsonToMap(result);
			if(ablums.get("code") != null && StringUtils.equals("E00004", ablums.get("code").toString())){
				model.put("error", ablums.get("message"));
				return returnURL;
			}
			List<Map> dataMap = new ArrayList<Map>();
			List<Map> datas = (List<Map>)ablums.get("data");
			for(Map map : datas){
				List<Integer> tvIds = (List<Integer>)map.get("tvIds");
				for(Integer tvId : tvIds){
					String vedioInfo = IQiYiAuthUtils.getVedioInfo(tvId + "");
					if(StringUtils.isNotBlank(vedioInfo)){
						Map vedioInfos = JsonUtils.readJsonToMap(vedioInfo);
						dataMap.add((Map)vedioInfos.get("data"));
					}
				}
			}
			model.put("dataMap", dataMap);
			PageUtil pageUtil=new PageUtil((Integer)ablums.get("total"),20,pageNo,"/admin/movie/searchAQIYIAV.xhtml");
			Map<String,String> params = new HashMap<String,String>();
			params.put("tag", tag);
			params.put("relatedid", relatedid + "");
			pageUtil.initPageInfo(params);
			model.put("pageUtil", pageUtil);
		}
		
		// tag + relatedid = 唯一确定已关联视频
		List<Video> videoList = videoService.getVideoListByTag(tag, relatedid, 0, 200);
		List<String> urlList = BeanUtil.getBeanPropertyList(videoList, String.class, "url", true);
		model.put("urlList", urlList);
		
		model.put("name", name);
		model.put("pageNo", pageNo);
		return returnURL;
	}
	@RequestMapping("/admin/movie/passTempMovie.xhtml")
	public String passTempMovie(HttpServletRequest request, ModelMap model) {
		Map<String, String[]> dataMap = request.getParameterMap();
		Set<String>  keySet = dataMap.keySet();
		User user = getLogonUser();
		for(String string : keySet){
			Long tmId = Long.valueOf(string);
			String value = ServiceHelper.get(dataMap, string);
			if(StringUtils.isNotBlank(value)){
				String[] values = value.split(",");
				if(!ArrayUtils.isEmpty(values)){
					TempMovie tm = daoService.getObject(TempMovie.class, tmId);
					Integer pointnum = Integer.parseInt(values[0]);
					if(pointnum == -1)continue;
					if(values.length>1){
						String reason = String.valueOf(values[1]);
						tm.setReason(reason);
					}
					ChangeEntry changeEntry = new ChangeEntry(tm);
					tm.setStatus(TempMovie.STATUS_PASSED);
					tm.setPoint(pointnum);
					
					daoService.saveObject(tm);
					monitorService.saveChangeLog(user.getId(), TempMovie.class, tm.getId(),changeEntry.getChangeMap( tm));
					pointService.addPointInfo(tm.getMemberid(), pointnum, tm.getMoviename(), PointConstant.TAG_ADD_INFO, null, user.getId());
				}
			}
		 }
		return showJsonSuccess(model);
	}
	@RequestMapping("/admin/movie/removeTempMovie.xhtml")
	public String passTempMovie(Long id, ModelMap model) {
		TempMovie tm = daoService.getObject(TempMovie.class, id);
		daoService.removeObject(tm);
		return showJsonSuccess(model);
	}
	
	@RequestMapping("/admin/movie/searchVideoMovieList.xhtml")
	public String movieList(String movieName,String videoPlayUrl,String videoName,String videoImage, ModelMap model) throws Exception {
		DetachedCriteria query = DetachedCriteria.forClass(Movie.class);
		query.add(Restrictions.ilike("moviename", movieName, MatchMode.ANYWHERE));
		query.addOrder(Order.desc("addtime"));
		query.addOrder(Order.asc("moviename"));
		List movieList = hibernateTemplate.findByCriteria(query);
		if(!VmUtils.isEmptyList(movieList)){
			DetachedCriteria relationsQuery = DetachedCriteria.forClass(Video.class);
			relationsQuery.add(Restrictions.eq("tag", Video.VIDEOTYPE_FILM));
			relationsQuery.add(Restrictions.in("relatedid",  BeanUtil.getBeanPropertyList(movieList, Long.class, "id", true)));
			List<Video> relations = hibernateTemplate.findByCriteria(relationsQuery);
			Map relationVideoMap = BeanUtil.beanListToMap(relations, "relatedid");
			model.put("relationVideoMap", relationVideoMap);
			model.put("relationVideoS", relationVideoMap.keySet());
		}
		model.put("movieList", movieList);
		model.put("videoPlayUrl",videoPlayUrl);
		model.put("videoName", videoName);
		model.put("videoImage", videoImage);
		return "admin/movie/ajaxMovieList.vm";
	}
}
