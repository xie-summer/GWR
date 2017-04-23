package com.gewara.web.action.inner.partner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.collections.map.UnmodifiableMap;
import org.apache.commons.lang.StringUtils;
import org.springframework.ui.ModelMap;

import com.gewara.helper.api.GewaApiBbsHelper;
import com.gewara.helper.api.GewaApiMovieHelper;
import com.gewara.model.api.ApiUserExtra;
import com.gewara.model.bbs.Diary;
import com.gewara.model.movie.Cinema;
import com.gewara.model.movie.CinemaProfile;
import com.gewara.model.movie.Movie;
import com.gewara.model.ticket.OpenPlayItem;
import com.gewara.model.user.MemberInfo;
import com.gewara.web.action.inner.mobile.BaseOpenApiMobileMovieController;
import com.gewara.xmlbind.bbs.Comment;

public class BaseOpenApiPartnerController extends BaseOpenApiMobileMovieController{
	protected String getPartnerPath(){
		return config.getString("mobilePath");
	}
	//设置图片的大小
	private static Map<String, String> moiveSizeMap = null;
	private static Map<String, String> cinemaSizeMap = null;
	private static Map<String, String> memberSizeMap = null;
	static{ 
		Map<String, String> tmp = new HashMap<String, String>();
		tmp.put("72", "96");
		tmp.put("96", "128");
		tmp.put("120", "160");
		tmp.put("150", "200");
		tmp.put("210", "280");
		tmp.put("360", "480");
		tmp.put("450", "600");
		moiveSizeMap = UnmodifiableMap.decorate(tmp);
		
		tmp = new HashMap<String, String>();
		tmp.put("80", "60");
		tmp.put("120", "60");
		tmp.put("200", "160");
		tmp.put("260", "180");
		cinemaSizeMap = UnmodifiableMap.decorate(tmp);
		
		tmp = new HashMap<String, String>();
		tmp.put("50", "50");
		tmp.put("60", "60");
		tmp.put("90", "90");
		tmp.put("100", "100");
		memberSizeMap = UnmodifiableMap.decorate(tmp);
	}
	private String getWHPicPath(String picwidth, String picheight){
		String picPath = getPartnerPath() + "w" + picwidth + "h" + picheight + "/";
		return picPath;
	}
	//影片的logo的宽度和高度
	private String getMoviePicPath(HttpServletRequest request){
		String picwidth = request.getParameter("picwidth");
		String picheight = request.getParameter("picheight");
		if(StringUtils.isBlank(picwidth) || StringUtils.isBlank(picheight) || !moiveSizeMap.containsKey(picwidth)){
			picwidth = "96";
			picheight = "128";
		}
		return getWHPicPath(picwidth, picheight);
	}
	//影院的logo的宽度和高度
	private String getCinemaPicPath(HttpServletRequest request){
		String picwidth = request.getParameter("picwidth");
		String picheight = request.getParameter("picheight");
		if(StringUtils.isBlank(picwidth) || StringUtils.isBlank(picheight) || !cinemaSizeMap.containsKey(picwidth)){
			picwidth = "120";
			picheight = "60";
		}
		return getWHPicPath(picwidth, picheight);
	}
	//用户的头像
	protected String getMemberLogoPath(HttpServletRequest request, MemberInfo info){
		String picwidth = request.getParameter("memwidth");
		String picheight = request.getParameter("memheight");
		if(StringUtils.isBlank(picwidth) || StringUtils.isBlank(picheight) || !memberSizeMap.containsKey(picwidth)){
			picwidth = "50";
			picheight = "50";
		}
		return getWHPicPath(picwidth, picheight) + info.getHeadpicUrl();
	}
	//电影
	protected void getMovieMap(Movie movie, ApiUserExtra extra, ModelMap model, HttpServletRequest request){
		if(movie==null) return;
		String picPath = getMoviePicPath(request);
		boolean hasMovieHighFields = false;
		if(extra!=null && extra.hasMovieHighFields()) hasMovieHighFields = true;
		Map<String, Object> resMap = getMovieData(movie, picPath + movie.getLimg(), hasMovieHighFields);
		putDetail(resMap, model, request);
	}
	protected void getMovieListMap(List<Movie> movieList,ApiUserExtra extra, ModelMap model, HttpServletRequest request){
		String picPath = getMoviePicPath(request);
		boolean hasMovieHighFields = false;
		if(extra!=null && extra.hasMovieHighFields()) hasMovieHighFields = true;
		List<Map<String, Object>> resMapList = new ArrayList<Map<String,Object>>();
		for(Movie movie : movieList){
			Map<String, Object> params = getMovieData(movie, picPath + movie.getLimg(), hasMovieHighFields);
			resMapList.add(params);
		}
		putList(resMapList, model, request);
	}
	//影院
	protected void getCinemaMap(Cinema cinema, CinemaProfile profile, ApiUserExtra extra, ModelMap model, HttpServletRequest request){
		if(cinema==null) return;
		boolean hasCinemaHighFields = false;
		if(extra!=null && extra.hasCinemaHighFields()) hasCinemaHighFields = true;
		String picPath = getCinemaPicPath(request);
		Map<String, Object> resMap = getCinemaData(cinema, picPath + cinema.getLimg(), null, hasCinemaHighFields);
		if(profile!=null){
			resMap.put("diaryid", profile.getTopicid());
		}
		putDetail(resMap, model, request);
	}
	protected void getCienmaListMap(List<Cinema> cinemaList, boolean hasCinemaHighFields, ModelMap model, HttpServletRequest request){
		String picPath = getCinemaPicPath(request);
		List<Map<String, Object>> resMapList = new ArrayList<Map<String,Object>>();
		for(Cinema cinema : cinemaList){
			Map<String, Object> params = getCinemaData(cinema, picPath + cinema.getLimg(), null, hasCinemaHighFields);
			resMapList.add(params);
		}
		putList(resMapList, model, request);
	}
	//影评
	protected void getMovieDiaryMap(Diary diary, String content, ModelMap model, HttpServletRequest request){
		if(diary==null) return;
		MemberInfo member  = daoService.getObject(MemberInfo.class, diary.getMemberid());
		Map<String, Object> resMap = GewaApiBbsHelper.getMovieDiary(diary, getMemberLogoPath(request, member), content);
		putDetail(resMap, model, request);
	}
	protected void getMovieDiaryListMap(List<Diary> diaryList, ModelMap model, HttpServletRequest request){
		List<Map<String, Object>> resMapList = new ArrayList<Map<String,Object>>();
		for(Diary diary : diaryList){
			MemberInfo member  = daoService.getObject(MemberInfo.class, diary.getMemberid());
			Map<String, Object> params = GewaApiBbsHelper.getMovieDiary(diary, getMemberLogoPath(request, member), null);
			resMapList.add(params);
		}
		putList(resMapList, model, request);
	}
	//哇啦数据
	protected void getCommentMap(Comment comment, ModelMap model, HttpServletRequest request){
		if(comment==null) return;
		MemberInfo member  = daoService.getObject(MemberInfo.class, comment.getMemberid());
		Map<String, Object> resMap = GewaApiBbsHelper.getComment(comment, getMemberLogoPath(request, member));
		putDetail(resMap, model, request);
	}
	protected void getCommentListMap(List<Comment> commentList, ModelMap model, HttpServletRequest request){
		List<Map<String, Object>> resMapList = new ArrayList<Map<String,Object>>();
		for(Comment comment : commentList){
			MemberInfo member  = daoService.getObject(MemberInfo.class, comment.getMemberid());
			if(member!=null){
				Map<String, Object> params = GewaApiBbsHelper.getComment(comment, getMemberLogoPath(request, member));
				resMapList.add(params);
			}
		}
		putList(resMapList, model, request);
	}
	//场次
	protected void getOpiMap(OpenPlayItem opi, ModelMap model, HttpServletRequest request){
		if(opi==null) return;
		Map<String, Object> resMap = GewaApiMovieHelper.getOpiData(opi);
		putDetail(resMap, model, request);
	}
	protected void getOpiListMap(List<OpenPlayItem> opiList, ModelMap model, HttpServletRequest request){
		List<Map<String, Object>> resMapList = new ArrayList<Map<String,Object>>();
		for(OpenPlayItem opi : opiList){
			Map<String, Object> params = GewaApiMovieHelper.getOpiData(opi);
			resMapList.add(params);
		}
		putList(resMapList, model, request);
	}
}
