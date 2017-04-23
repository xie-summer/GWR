package com.gewara.helper.ticket;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import com.gewara.model.api.ApiUser;
import com.gewara.model.movie.Cinema;
import com.gewara.model.movie.Movie;
import com.gewara.model.partner.PartnerCloseRule;
import com.gewara.model.ticket.OpenPlayItem;
import com.gewara.util.BeanUtil;
import com.gewara.util.DateUtil;

public class CloseRuleOpiFilter extends OpiFilter {
	private List<PartnerCloseRule> pcrList = null;
	private ApiUser partner = null;
	private Set<Long> includeMovies = new HashSet<Long>();
	private Set<Long> includeCinemas = new HashSet<Long>();
	private Set<Long> excludeCinemas = new HashSet<Long>();
	private boolean hasFilter = false;
	public CloseRuleOpiFilter(ApiUser partner, List<PartnerCloseRule> pcrList){
		this.partner = partner;
		this.pcrList = new ArrayList<PartnerCloseRule>();
		init(pcrList);
	}
	@Override
	public boolean hasFilter() {
		return hasFilter;
	}
	private void init(List<PartnerCloseRule> pcrs){
		Timestamp cur = new Timestamp(System.currentTimeMillis());
		for(PartnerCloseRule pcr: pcrs){
			if(!pcr.matchPartner(partner.getId())) continue;
			if(StringUtils.equals(pcr.getRuletype(), PartnerCloseRule.RULETYPE_OPI)){
				//过滤场次
				hasFilter = true;
				this.pcrList.add(pcr);
			}else if(pcr.getOpentime1().before(cur) && pcr.getOpentime2().after(cur)) {
				if(StringUtils.equals(pcr.getRuletype(), PartnerCloseRule.RULETYPE_MOVIE)){//只过滤影片
					if(StringUtils.isNotBlank(pcr.getMovieids())){
						hasFilter = true;
						includeMovies.addAll(BeanUtil.getIdList(pcr.getMovieids(), ","));
					}
				}else if(StringUtils.isNotBlank(pcr.getCinemaids())){//只过滤影院
					List<Long> cinemas = BeanUtil.getIdList(pcr.getCinemaids(), ",");
					if(StringUtils.equals(pcr.getCmatch(), PartnerCloseRule.MATCH_EXCLUDE)){
						hasFilter = true;
						excludeCinemas.addAll(cinemas);
					}else{
						hasFilter = true;
						includeCinemas.addAll(cinemas);
					}
				}
			}
		}
	}
	@Override
	public boolean excludeOpi(OpenPlayItem opi) {
		if(includeMovies.contains(opi.getMovieid())) return true;
		if(includeCinemas.contains(opi.getCinemaid())) return true;
		if(!excludeCinemas.isEmpty() && !excludeCinemas.contains(opi.getCinemaid())) return true;
		
		for(PartnerCloseRule pcr: pcrList){
			if(isCloseOpi(opi, pcr)) return true;
		}
		return false;
	}
	private boolean isCloseOpi(OpenPlayItem opi, PartnerCloseRule pcr){
		if(opi.getPlaytime().before(pcr.getOpentime1()) || opi.getPlaytime().after(pcr.getOpentime2())) return false;
		if(!pcr.matchMovie(opi.getMovieid())) return false;
		if(!pcr.matchCinema(opi.getCinemaid())) return false;

		if(StringUtils.isNotBlank(pcr.getTime1()) && StringUtils.isNotBlank(pcr.getTime2())){
			String playtime = DateUtil.format(opi.getPlaytime(), "HHmm");
			if(playtime.compareTo(pcr.getTime1()) < 0 || playtime.compareTo(pcr.getTime2()) >0) return false;
		}
		if(opi.getGewaprice()< pcr.getPrice1() || opi.getGewaprice() > pcr.getPrice2()) return false;
		if(opi.getGewaprice() - opi.getCostprice() > pcr.getPricegap()) return false;
		if(StringUtils.isNotBlank(pcr.getWeektype())){
			String week = ""+DateUtil.getWeek(opi.getPlaytime());
			if(!pcr.getWeektype().contains(week)) return false;
		}
		if(StringUtils.isNotBlank(pcr.getMpids())){
			List<Long> mpids = BeanUtil.getIdList(pcr.getMpids(), ",");
			if(!mpids.contains(opi.getMpid())) return false;
		}
		return true;
	}
	@Override
	public void filterMovie(List<Movie> movieList){
		if(!hasFilter) return;
		List<Movie> removeList = new ArrayList<Movie>();
		for(Movie movie: movieList){
			if(includeMovies.contains(movie.getId())) removeList.add(movie);
		}
		movieList.removeAll(removeList);
	}
	@Override
	public void filterCinema(List<Cinema> cinemaList){
		if(!hasFilter) return;
		List<Cinema> removeList = new ArrayList<Cinema>();
		for(Cinema cinema: cinemaList){
			if(includeCinemas.contains(cinema.getId()) || !excludeCinemas.isEmpty() && !excludeCinemas.contains(cinema.getId())) removeList.add(cinema);
		}
		cinemaList.removeAll(removeList);
	}
}
