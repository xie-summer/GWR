package com.gewara.web.action.partner;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.support.PropertyComparator;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.RequestMapping;

import com.gewara.constant.AdminCityContant;
import com.gewara.constant.CityData;
import com.gewara.constant.ticket.PartnerConstant;
import com.gewara.model.api.ApiUser;
import com.gewara.model.movie.Cinema;
import com.gewara.model.movie.Movie;
import com.gewara.util.WebUtils;

@Controller
public class PartnerSXFilmController extends BasePartnerController {
	
	private static final List<Long> cinemaIds =  new ArrayList<Long>();
	static {
		cinemaIds.add(37795461L);
		cinemaIds.add(38583741L);
		cinemaIds.add(37795503L);
	}
	private ApiUser getSXFilm(){
		return daoService.getObject(ApiUser.class, PartnerConstant.PARTNER_SXFILM);
	}
	@RequestMapping("/partner/sxdyw/movieDetail.xhtml")
	public String opiList(HttpServletResponse response, Date fyrq, Long movieid,String citycode,
			@CookieValue(required=false,value="ukey") String ukey, ModelMap model, HttpServletRequest request) {
		if(StringUtils.isBlank(ukey)) PartnerUtil.setUkCookie(response, config.getBasePath() + "partner/sxdyw/");
		ApiUser partner = getSXFilm();
		if(StringUtils.isNotBlank(citycode)){
			if(!partner.supportsCity(citycode))citycode = partner.getDefaultCity();
			WebUtils.setCitycode(request,citycode, response);
			model.put("cityname", AdminCityContant.getCitycode2CitynameMap().get(citycode));
		}else{
			citycode = this.getCitycodeByPartner(partner, request, response);
		}
		addOpiListData(partner, movieid, fyrq, null, null, model, citycode);
		List<Cinema> cinemas = (List<Cinema>)model.remove("cinemaList");
		List<Cinema> cinemaList = new ArrayList<Cinema>();
		if(cinemas != null){
			for(Cinema cinema : cinemas){
				if(cinemaIds.contains(cinema.getId())){
					cinemaList.add(cinema);
				}
			}
		}
		Collections.sort(cinemaList, new PropertyComparator("name", false, false));
		model.put("cinemaList", cinemaList);
		model.put("partnerCityMap", CityData.getCityNameMap(partner.getCitycode()));
		if(movieid == null){
			if(model.get("movieList") != null && ((List<Movie>)model.get("movieList")).size() > 0){
				model.put("movieid",((List<Movie>)model.get("movieList")).get(0).getId());
			}
		}else{
			model.put("movieid", movieid);
		}
		return "partner/sxdyw/opiList.vm";
	}
}
