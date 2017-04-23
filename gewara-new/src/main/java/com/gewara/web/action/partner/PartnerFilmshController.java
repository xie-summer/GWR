package com.gewara.web.action.partner;

import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import com.gewara.constant.ticket.PartnerConstant;
import com.gewara.model.api.ApiUser;
import com.gewara.model.movie.Cinema;
import com.gewara.model.movie.Movie;
import com.gewara.model.movie.MoviePlayItem;
import com.gewara.model.ticket.OpenPlayItem;
import com.gewara.service.movie.FilmFestService;
import com.gewara.util.BeanUtil;
import com.gewara.util.DateUtil;

/**
 * 联合院线
 * @author gang.liu
 *
 */
@Controller
public class PartnerFilmshController extends BasePartnerController{

	@Autowired@Qualifier("filmFestService")
	private FilmFestService filmFestService;
	public void setFilmFestService(FilmFestService filmFestService) {
		this.filmFestService = filmFestService;
	}
	
	private final String flag = "AFA";
	
	private ApiUser getFilmsh(){
		return daoService.getObject(ApiUser.class, PartnerConstant.PARTNER_FILMSH);
	}
	
	@RequestMapping("/partner/filmsh/movieDetail.xhtml")
	public String opiList(boolean booking,ModelMap model){
		ApiUser partner = this.getFilmsh();
		if(partner == null || !ApiUser.STATUS_OPEN.equals(partner.getStatus())){
			return show404(model, "没有相应的权限进行此操作！");
		}
		List<Long> movieIds = filmFestService.getSpecialActivityMovieIds(flag);
		if(!movieIds.isEmpty()){
			String ids = StringUtils.join(movieIds, ",");
			String query = "select mp.id from MoviePlayItem mp where mp.movieid in (" + ids + ")  and mp.playdate >= ?  order by mp.playdate asc,mp.playtime asc";
			List<Long> mpiIdList = daoService.queryByRowsRange(query, 0, 2000, DateUtil.getBeginningTimeOfDay(new Date()));//只取2000
			List<MoviePlayItem> mpiList = new LinkedList<MoviePlayItem>();
			if(!mpiIdList.isEmpty()){
				Map<Long/*mpid*/, Boolean> opiMap = new HashMap<Long, Boolean>();
				for(Long mpiid: mpiIdList){
					MoviePlayItem mpi = daoService.getObject(MoviePlayItem.class, mpiid);
					mpiList.add(mpi);
					OpenPlayItem opi = daoService.getObjectByUkey(OpenPlayItem.class, "mpid", mpi.getId(), true);
					if(opi!=null && opi.isOrder()) opiMap.put( mpi.getId(), true);//添加可订票标识
				}
				List<Long> movieidList = BeanUtil.getBeanPropertyList(mpiList, "movieid", true);
				List<Long> cinemaidList = BeanUtil.getBeanPropertyList(mpiList, "cinemaid", true);
				Map<Long,Cinema> cinemas = daoService.getObjectMap(Cinema.class, cinemaidList);
				Map<Long,Movie> movies = daoService.getObjectMap(Movie.class, movieidList);
				model.put("opiMap", opiMap);
				model.put("mpiList", mpiList);
				model.put("cinemas", cinemas);
				model.put("movies", movies);
			}
		}
		if(booking){
			return "partner/filmsh/bookingOpiList.vm";
		}
		return "partner/filmsh/opiList.vm";
	}
	
}
