package com.gewara.web.action.admin.content;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import com.gewara.constant.content.SignName;
import com.gewara.model.content.GewaCommend;
import com.gewara.util.RelatedHelper;
@Controller
public class GewaCommendAdminControllerNew extends GewaCommendBaseAdminController {
	@RequestMapping("/admin/recommend/indexCommendPic.xhtml")
	public String IndexCommendPic(Boolean isNew,HttpServletRequest request, ModelMap model) {
		RelatedHelper rh = new RelatedHelper();
		model.put("relatedHelper", rh);
		if(isNew != null && isNew){
			List<GewaCommend> gcMovieList = commonService.getGewaCommendList(getAdminCitycode(request) , SignName.INDEX_MOVIELIST_NEW, null, null, false, 0, 20);
			List<GewaCommend> gcDramaList = commonService.getGewaCommendList(getAdminCitycode(request) , SignName.INDEX_DRAMALIST_NEW, null, null, false, 0, 20);
			List<GewaCommend> newgcSportList = commonService.getGewaCommendList(getAdminCitycode(request) , SignName.INDEX_NEW_SPORTLIST_NEW, null, null, false, 0, 20);
			model.put("signnamemovelist",SignName.INDEX_MOVIELIST_NEW);
			model.put("signnamedramalist",SignName.INDEX_DRAMALIST_NEW);
			model.put("newsignnamesportlist", SignName.INDEX_NEW_SPORTLIST_NEW);
			model.put("gcMovieList", gcMovieList);
			commonService.initGewaCommendList("gcMovieList", rh, gcMovieList);
			model.put("gcDramaList", gcDramaList);
			commonService.initGewaCommendList("gcDramaList", rh, gcDramaList);
			model.put("newgcSportList", newgcSportList);
			commonService.initGewaCommendList("newgcSportList", rh, newgcSportList);
			return "admin/recommend/index/indexCommendPic.vm";
		}else{
			List<GewaCommend> gcMovieList = commonService.getGewaCommendList(getAdminCitycode(request) , SignName.INDEX_MOVIELIST, null, null, false, 0, 20);
			List<GewaCommend> gcDramaList = commonService.getGewaCommendList(getAdminCitycode(request) , SignName.INDEX_DRAMALIST, null, null, false, 0, 20);
			List<GewaCommend> gcSportList = commonService.getGewaCommendList(getAdminCitycode(request) , SignName.INDEX_SPORTAREA, null, null, false, 0, 20);
			List<GewaCommend> newgcSportList = commonService.getGewaCommendList(getAdminCitycode(request) , SignName.INDEX_NEW_SPORTLIST, null, null, false, 0, 20);
			
			model.put("signnamemovelist",SignName.INDEX_MOVIELIST);
			model.put("signnamedramalist",SignName.INDEX_DRAMALIST);
			model.put("signnamesportlist", SignName.INDEX_SPORTAREA);
			model.put("newsignnamesportlist", SignName.INDEX_NEW_SPORTLIST);
			
			model.put("gcMovieList", gcMovieList);
			commonService.initGewaCommendList("gcMovieList", rh, gcMovieList);
			model.put("gcDramaList", gcDramaList);
			commonService.initGewaCommendList("gcDramaList", rh, gcDramaList);
			model.put("gcSportList", gcSportList);
			commonService.initGewaCommendList("gcSportList", rh, gcSportList);
			model.put("newgcSportList", newgcSportList);
			commonService.initGewaCommendList("newgcSportList", rh, newgcSportList);
			model.put("isShowSportPlace", true);
			return "admin/recommend/index/indexCommendPic.vm";
		}
	}
	
	@RequestMapping("/admin/recommend/indexMobile.xhtml")
	public String IndexMobile(HttpServletRequest request, ModelMap model) {
		List<GewaCommend> gcMobileInfo = commonService.getGewaCommendList(getAdminCitycode(request) , SignName.INDEX_MOBILE, null, null, false, 0, 10);
		model.put("gcMobileInfo", gcMobileInfo);
		model.put("signname", SignName.INDEX_MOBILE);
		return "admin/recommend/index/indexMobile.vm";
	}
	
	@RequestMapping("/admin/recommend/indexLeftMenuPic.xhtml")
	public String indexLeftMenuPic(HttpServletRequest request, ModelMap model) {
		List<GewaCommend> movieGcInfo = commonService.getGewaCommendList(getAdminCitycode(request) , SignName.INDEX_MOVIE_AD, null, null, false, 0, 10);
		model.put("movieGcInfo", movieGcInfo);
		List<GewaCommend> dramaGcInfo = commonService.getGewaCommendList(getAdminCitycode(request) , SignName.INDEX_DRAMA_AD, null, null, false, 0, 10);
		model.put("dramaGcInfo", dramaGcInfo);
		List<GewaCommend> sportGcInfo = commonService.getGewaCommendList(getAdminCitycode(request) , SignName.INDEX_SPORT_AD, null, null, false, 0, 10);
		model.put("sportGcInfo", sportGcInfo);
		return "admin/recommend/index/indexLeftMenuPic.vm";
	}
	
	@RequestMapping("/admin/recommend/indexDiscount.xhtml")
	public String IndexDiscount(HttpServletRequest request,String signName,ModelMap model) {
		if(StringUtils.isBlank(signName)){
			signName = SignName.INDEX_DISCOUNT;
		}
		List<GewaCommend> gcDiscountList = commonService.getGewaCommendList(getAdminCitycode(request) , signName, null, null, false, 0, 10);
		model.put("gcDiscountList", gcDiscountList);
		model.put("signname", signName);
		return "admin/recommend/index/indexDiscount.vm";
	}
	
	@RequestMapping("/admin/recommend/indexNewMovie.xhtml")
	public String IndexNewMovie(HttpServletRequest request, ModelMap model) {
		List<GewaCommend> gcMovieChosenInfo = commonService.getGewaCommendList(getAdminCitycode(request) , SignName.INDEX_MOVIE_CHOSEN, null, null, false, 0, 20);
		List<GewaCommend> gcActivityList = commonService.getGewaCommendList(getAdminCitycode(request) , SignName.INDEX_MOVIEACTIVITY_NEW, null, null, false, 0, 10);
		List<GewaCommend> futuremovieList = commonService.getGewaCommendList(getAdminCitycode(request) , SignName.MOVIEINDEX_FUTUREMOVIE, null, null, false, 0, 10);
		RelatedHelper rh = new RelatedHelper();
		model.put("relatedHelper", rh);
		model.put("futuremovieList", futuremovieList);
		model.put("gcMovieChosenInfo",gcMovieChosenInfo);
		commonService.initGewaCommendList("futuremovieList", rh, futuremovieList);
		model.put("gcActivityList", gcActivityList);
		commonService.initGewaCommendList("gcActivityList", rh, gcActivityList);
		model.put("signnameactivity", SignName.INDEX_MOVIEACTIVITY_NEW);
		model.put("signnameamoviechosen", SignName.INDEX_MOVIE_CHOSEN);
		model.put("adminCitycode", getAdminCitycode(request));
		return "admin/recommend/index/indexMovieArea_new.vm";
	}
	
	@RequestMapping("/admin/recommend/indexMovie.xhtml")
	public String IndexMovie(HttpServletRequest request, ModelMap model) {
		List<GewaCommend> gcMovieInfo = commonService.getGewaCommendList(getAdminCitycode(request) , SignName.INDEX_MOVIEAREA, null, null, false, 0, 10);
		List<GewaCommend> gcNewsSubList = commonService.getGewaCommendList(getAdminCitycode(request) , SignName.INDEX_MOVIESUBJECT, null, null, false, 0, 10);
		List<GewaCommend> gcNewsList = commonService.getGewaCommendList(getAdminCitycode(request) , SignName.INDEX_MOVIENEWS, null, null, false, 0, 10);
		List<GewaCommend> gcWeekList = commonService.getGewaCommendList(getAdminCitycode(request) , SignName.INDEX_MOVIEWEEK, null, null, false, 0, 10);
		List<GewaCommend> gcDiaryList = commonService.getGewaCommendList(getAdminCitycode(request) , SignName.INDEX_MOVIEDIARY, null, null, false, 0, 10);
		List<GewaCommend> gcActivityList = commonService.getGewaCommendList(getAdminCitycode(request) , SignName.INDEX_MOVIEACTIVITY, null, null, false, 0, 10);
		List<GewaCommend> gcMovieMember = commonService.getGewaCommendList(getAdminCitycode(request) , SignName.INDEX_MOVIEMEMBER, null, null, false, 0, 10);
		List<GewaCommend> futuremovieList = commonService.getGewaCommendList(getAdminCitycode(request) , SignName.MOVIEINDEX_FUTUREMOVIE, null, null, false, 0, 10);
		List<GewaCommend> gcMovieChosenInfo = commonService.getGewaCommendList(getAdminCitycode(request) , SignName.INDEX_MOVIE_CHOSEN, null, null, false, 0, 20);
		RelatedHelper rh = new RelatedHelper();
		model.put("relatedHelper", rh);
		model.put("gcMovieInfo", gcMovieInfo);
		model.put("gcNewsSubList", gcNewsSubList);
		model.put("gcNewsList", gcNewsList);
		model.put("gcWeekList", gcWeekList);
		model.put("gcDiaryList", gcDiaryList);
		model.put("futuremovieList", futuremovieList);
		commonService.initGewaCommendList("futuremovieList", rh, futuremovieList);
		commonService.initGewaCommendList("gcDiaryList", rh, gcDiaryList);
		model.put("gcActivityList", gcActivityList);
		commonService.initGewaCommendList("gcActivityList", rh, gcActivityList);
		model.put("gcMovieMember", gcMovieMember);
		model.put("signnamemovie", SignName.INDEX_MOVIEAREA);
		model.put("signnamesubject", SignName.INDEX_MOVIESUBJECT);
		model.put("signnamenews", SignName.INDEX_MOVIENEWS);
		model.put("signnameweek", SignName.INDEX_MOVIEWEEK);
		model.put("signnamediary", SignName.INDEX_MOVIEDIARY);
		model.put("signnameactivity", SignName.INDEX_MOVIEACTIVITY);
		model.put("signnamemember", SignName.INDEX_MOVIEMEMBER);
		model.put("adminCitycode", getAdminCitycode(request));
		model.put("gcMovieChosenInfo",gcMovieChosenInfo);
		model.put("signnameamoviechosen", SignName.INDEX_MOVIE_CHOSEN);
		return "admin/recommend/index/indexMovieArea.vm";
	}
	
	@RequestMapping("/admin/recommend/indexNewDrama.xhtml")
	public String indexNewDrama(HttpServletRequest request, ModelMap model) {
		List<GewaCommend> gcDramaList = commonService.getGewaCommendList(getAdminCitycode(request) , SignName.INDEX_DRAMAAREA_NEW, null, null, false, 0, 10);
		List<GewaCommend> gcDramaChosenList = commonService.getGewaCommendList(getAdminCitycode(request) , SignName.INDEX_DRAMA_CHOSEN, null, null, false, 0, 10);
		List<GewaCommend> gcDiaryList = commonService.getGewaCommendList(getAdminCitycode(request) , SignName.INDEX_DRAMADIARY_NEW, null, null, false, 0, 10);
		List<GewaCommend> gcHotActivityList = commonService.getGewaCommendList(getAdminCitycode(request) , SignName.INDEX_DRAMA_HOTACTIVITY, null, null, false, 0, 10);
		
		RelatedHelper rh = new RelatedHelper();
		model.put("relatedHelper", rh);
		model.put("gcDramaList", gcDramaList);
		commonService.initGewaCommendList("gcDramaList", rh, gcDramaList);
		model.put("gcDramaChosenList", gcDramaChosenList);
		model.put("gcDiaryList", gcDiaryList);
		commonService.initGewaCommendList("gcDiaryList", rh, gcDiaryList);
		model.put("gcHotActivityList", gcHotActivityList);
		model.put("signnamedrama", SignName.INDEX_DRAMAAREA_NEW);
		model.put("signnamechosen", SignName.INDEX_DRAMA_CHOSEN);
		model.put("signnamediary", SignName.INDEX_DRAMADIARY_NEW);
		model.put("signnameahotctivity", SignName.INDEX_DRAMA_HOTACTIVITY);
		return "admin/recommend/index/indexDramaArea_new.vm";
	}
	
	@RequestMapping("/admin/recommend/indexDrama.xhtml")
	public String IndexDrama(HttpServletRequest request, ModelMap model) {
		List<GewaCommend> gcDramaList = commonService.getGewaCommendList(getAdminCitycode(request) , SignName.INDEX_DRAMAAREA, null, null, false, 0, 10);
		List<GewaCommend> gcNewsSubList = commonService.getGewaCommendList(getAdminCitycode(request) , SignName.INDEX_DRAMASUBJECT, null, null, false, 0, 10);
		List<GewaCommend> gcNewsList = commonService.getGewaCommendList(getAdminCitycode(request) , SignName.INDEX_DRAMANEWS, null, null, false, 0, 10);
		List<GewaCommend> gcDiaryList = commonService.getGewaCommendList(getAdminCitycode(request) , SignName.INDEX_DRAMADIARY, null, null, false, 0, 10);
		List<GewaCommend> gcActivityList = commonService.getGewaCommendList(getAdminCitycode(request) , SignName.INDEX_DRAMAACTIVITY, null, null, false, 0, 10);
		List<GewaCommend> gcDramaMember = commonService.getGewaCommendList(getAdminCitycode(request) , SignName.INDEX_DRAMAMEMBER, null, null, false, 0, 10);
		
		RelatedHelper rh = new RelatedHelper();
		model.put("relatedHelper", rh);
		model.put("gcDramaList", gcDramaList);
		commonService.initGewaCommendList("gcDramaList", rh, gcDramaList);
		model.put("gcNewsSubList", gcNewsSubList);
		model.put("gcNewsList", gcNewsList);
		model.put("gcDiaryList", gcDiaryList);
		commonService.initGewaCommendList("gcDiaryList", rh, gcDiaryList);
		model.put("gcActivityList", gcActivityList);
		commonService.initGewaCommendList("gcActivityList", rh, gcActivityList);
		model.put("gcDramaMember", gcDramaMember);
		model.put("signnamedrama", SignName.INDEX_DRAMAAREA);
		model.put("signnamesubject", SignName.INDEX_DRAMASUBJECT);
		model.put("signnamenews", SignName.INDEX_DRAMANEWS);
		model.put("signnamediary", SignName.INDEX_DRAMADIARY);
		model.put("signnameactivity", SignName.INDEX_DRAMAACTIVITY);
		model.put("signnamemember", SignName.INDEX_DRAMAMEMBER);
		return "admin/recommend/index/indexDramaArea.vm";
	}
	
	@RequestMapping("/admin/recommend/indexSport.xhtml")
	public String IndexSport(HttpServletRequest request,Boolean isNew, ModelMap model) {
		if(isNew != null && isNew){
			List<GewaCommend> gcSportNews = commonService.getGewaCommendList(getAdminCitycode(request) , SignName.INDEX_SPORTAREA_NEW, null, null, false, 0, 10);
			List<GewaCommend> gcSportVenues = commonService.getGewaCommendList(getAdminCitycode(request) , SignName.INDEX_SPORT_VENUES, null, null, false, 0, 10);
			List<GewaCommend> gcActivityList = commonService.getGewaCommendList(getAdminCitycode(request) , SignName.INDEX_SPORTACTIVITY_NEW, null, null, false, 0, 10);
			
			RelatedHelper rh = new RelatedHelper();
			model.put("relatedHelper", rh);
			model.put("gcSportNews", gcSportNews);
			commonService.initGewaCommendList("gcSportNews", rh, gcSportNews);
			model.put("gcSportVenues", gcSportVenues);
			commonService.initGewaCommendList("gcSportVenues", rh, gcSportVenues);
			model.put("gcActivityList", gcActivityList);
			commonService.initGewaCommendList("gcActivityList", rh, gcActivityList);
			model.put("signnamesport", SignName.INDEX_SPORTAREA_NEW);
			model.put("signnamesportplace", SignName.INDEX_SPORT_VENUES);
			model.put("signnameactivity", SignName.INDEX_SPORTACTIVITY_NEW);
			return "admin/recommend/index/indexSportArea_new.vm";
		}
		List<GewaCommend> gcSportNews = commonService.getGewaCommendList(getAdminCitycode(request) , SignName.INDEX_SPORTAREA, null, null, false, 0, 10);
		List<GewaCommend> gcSportSearch = commonService.getGewaCommendList(getAdminCitycode(request) , SignName.INDEX_SPORT_SEARCH, null, null, false, 0, 10);
		List<GewaCommend> gcOpenPlayList = commonService.getGewaCommendList(getAdminCitycode(request) , SignName.INDEX_SPORTOPI, null, null, false, 0, 10);
		List<GewaCommend> gcItemInfo = commonService.getGewaCommendList(getAdminCitycode(request) , SignName.INDEX_SPORTPLACE, null, null, false, 0, 10);
		List<GewaCommend> gcActivityList = commonService.getGewaCommendList(getAdminCitycode(request) , SignName.INDEX_SPORTACTIVITY, null, null, false, 0, 10);
		List<GewaCommend> gcSportMember = commonService.getGewaCommendList(getAdminCitycode(request) , SignName.INDEX_SPORTMEMBER, null, null, false, 0, 10);
		
		RelatedHelper rh = new RelatedHelper();
		model.put("relatedHelper", rh);
		model.put("gcSportNews", gcSportNews);
		commonService.initGewaCommendList("gcSportNews", rh, gcSportNews);
		model.put("gcSportSearch", gcSportSearch);
		model.put("gcOpenPlayList", gcOpenPlayList);
		model.put("gcItemInfo", gcItemInfo);
		commonService.initGewaCommendList("gcItemInfo", rh, gcItemInfo);
		model.put("gcActivityList", gcActivityList);
		commonService.initGewaCommendList("gcActivityList", rh, gcActivityList);
		model.put("gcSportMember", gcSportMember);
		model.put("signnamesport", SignName.INDEX_SPORTAREA);
		model.put("signnamesearch", SignName.INDEX_SPORT_SEARCH);
		model.put("signnamesubject", SignName.INDEX_SPORTOPI);
		model.put("signnameplace", SignName.INDEX_SPORTPLACE);
		model.put("signnameactivity", SignName.INDEX_SPORTACTIVITY);
		model.put("signnamemember", SignName.INDEX_SPORTMEMBER);
		return "admin/recommend/index/indexSportArea.vm";
	}
	
	@RequestMapping("/admin/recommend/indexCommunity.xhtml")
	public String IndexCommu(HttpServletRequest request, ModelMap model) {
		List<GewaCommend> gcCommunity = commonService.getGewaCommendList(getAdminCitycode(request) , SignName.INDEX_COMMUNITY, null, null, false, 0, 10);
		
		model.put("gcCommunity", gcCommunity);
		
		model.put("signname", SignName.INDEX_COMMUNITY);
		return "admin/recommend/index/indexCommunity.vm";
	}
	
	@RequestMapping("/admin/recommend/indexBottomPic.xhtml")
	public String IndexBottomPic(HttpServletRequest request, ModelMap model) {
		List<GewaCommend> gcPictureList = commonService.getGewaCommendList(getAdminCitycode(request) , SignName.INDEX_BOTTOMPIC, null, null, false, 0, 10);
		
		model.put("gcPictureList", gcPictureList);
		model.put("signname", SignName.INDEX_BOTTOMPIC);
		return "admin/recommend/index/indexBottomPic.vm";
	}
}
