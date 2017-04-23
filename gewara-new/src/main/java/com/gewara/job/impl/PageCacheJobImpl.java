package com.gewara.job.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import com.gewara.job.JobService;
import com.gewara.job.PageCacheJob;
import com.gewara.service.DaoService;
import com.gewara.untrans.PageCacheService;
import com.gewara.untrans.PageParams;
import com.gewara.util.DateUtil;

public class PageCacheJobImpl  extends JobService implements PageCacheJob{
	@Autowired@Qualifier("daoService")
	private DaoService daoService;
	public void setDaoService(DaoService daoService) {
		this.daoService = daoService;
	}
	@Autowired@Qualifier("pageCacheService")
	private PageCacheService pageCacheService;
	public void setPageCacheService(PageCacheService pageCacheService) {
		this.pageCacheService = pageCacheService;
	}
	private long validtime = System.currentTimeMillis();
	private List<Long> cinemaidList = new ArrayList<Long>();
	private List<Long> movieidList = null;
	private String DEFAULT_CITYCODE = "310000";
	
	public void refreshPageView(){
		refreshCinemaPageView();
		refreshMoviePageView();
	}
	@Override
	public void refreshCinemaPageView() {
		if(System.currentTimeMillis() > validtime || cinemaidList == null){
			validtime = System.currentTimeMillis() + DateUtil.m_hour * 2;
			cinemaidList = daoService.queryByRowsRange("select c.id from Cinema c where c.citycode=?", 0, 5000, DEFAULT_CITYCODE);
		}
		int count = 0;
		String pageUrl = "cinema/cinemaDetail.xhtml";
		Long cur = System.currentTimeMillis();
		for(Long cinemaid : cinemaidList){
			PageParams pageParams = new PageParams();
			pageParams.addLong("cid", cinemaid);
			if(pageCacheService.isUpdated(pageUrl, DEFAULT_CITYCODE, cur, pageParams)){
				count ++;
			}else{
				pageCacheService.refreshPageView(pageUrl, pageParams, DEFAULT_CITYCODE);
			}
		}
		dbLogger.warn("更新上海影院页面缓存：" + cinemaidList.size() + ", 命中缓存不需要更新共：" + count);
	}
	@Override
	public void refreshMoviePageView() {
		if(System.currentTimeMillis() > validtime || movieidList == null){
			validtime = System.currentTimeMillis() + DateUtil.m_hour * 2;
			movieidList = daoService.queryByRowsRange("select distinct mpi.movieid from MoviePlayItem mpi where mpi.playdate >= ?", 0, 100, DateUtil.getBeginningTimeOfDay(new Date()));
		}
		int count = 0;
		String pageUrl = "movie/movieDetail.xhtml";
		Long cur = System.currentTimeMillis();
		for(Long movieid : movieidList){
			PageParams pageParams = new PageParams();
			pageParams.addLong("mid", movieid);
			if(pageCacheService.isUpdated(pageUrl, DEFAULT_CITYCODE, cur, pageParams)){
				count ++;
			}else{
				pageCacheService.refreshPageView(pageUrl, pageParams, DEFAULT_CITYCODE);
			}
		}
		dbLogger.warn("更新上海影片页面缓存：" + movieidList.size() + ", 命中缓存不需要更新共：" + count);
	}

}
