package com.gewara.api.service.movie;

import java.sql.Timestamp;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import com.gewara.api.vo.ResultCode;
import com.gewara.api.vo.movie.McpPlayDateVo;
import com.gewara.api.vo.movie.MoviePlayItemVo;
import com.gewara.api.vo.movie.MovieVo;
import com.gewara.api.vo.movie.OpenPlayItemVo;
import com.gewara.constant.AdminCityContant;
import com.gewara.constant.TagConstant;
import com.gewara.constant.sys.CacheConstant;
import com.gewara.model.movie.Cinema;
import com.gewara.model.movie.Movie;
import com.gewara.model.movie.MoviePlayItem;
import com.gewara.model.ticket.OpenPlayItem;
import com.gewara.service.bbs.MarkService;
import com.gewara.service.impl.BaseServiceImpl;
import com.gewara.service.movie.MCPService;
import com.gewara.service.ticket.OpenPlayService;
import com.gewara.support.MultiPropertyComparator;
import com.gewara.untrans.CacheService;
import com.gewara.util.BeanUtil;
import com.gewara.util.DateUtil;
import com.gewara.util.VmUtils;
import com.gewara.util.VoCopyUtil;

public class McpVoServiceImpl extends BaseServiceImpl implements McpVoService {
	@Autowired@Qualifier("mcpService")
	private MCPService mcpService;
	
	@Autowired@Qualifier("cacheService")
	private CacheService cacheService;
	
	@Autowired@Qualifier("markService")
	private MarkService markService;
	
	@Autowired@Qualifier("openPlayService")
	private OpenPlayService openPlayService;
	@Override
	public ResultCode<OpenPlayItemVo> getOpenPlayItemVoById(Long mpid) {
		OpenPlayItem opi = baseDao.getObjectByUkey(OpenPlayItem.class, "mpid", mpid,true);
		return VoCopyUtil.copyProperties(OpenPlayItemVo.class, opi);
	}
	@Override
	public ResultCode<List<McpPlayDateVo>> getCurCinemaPlayDate(long cinemaId,
			boolean includeMcpCount) {
		Cinema cinema = baseDao.getObject(Cinema.class, cinemaId);
		if(cinema == null){
			return ResultCode.getFailure("场馆不存在：" + cinemaId);
		}
		List<Date> playdateList = mcpService.getCurCinemaPlayDate(cinemaId);
		List<McpPlayDateVo> vos = new LinkedList<McpPlayDateVo>();
		for(Date date : playdateList){
			McpPlayDateVo vo = new McpPlayDateVo();
			vo.setPlayDate(date);
			if(includeMcpCount){
				vo.setMcpCount(mcpService.getCinemaMpiCountByDate(cinemaId, date));
			}
			vos.add(vo);
		}
		return ResultCode.getSuccessReturn(vos);
	}

	@Override
	public ResultCode<List<MovieVo>> getMovieList(Long cinemaId, Date playDate) {
		List<Map<Long,Long>> movieIdList = this.getCurMovieIdByCinemaIdAndDate(cinemaId, playDate);
		List<MovieVo> vos = new LinkedList<MovieVo>();
		Map markData = markService.getMarkdata(TagConstant.TAG_MOVIE);
		for(Map<Long,Long> map : movieIdList){
			Movie movie = baseDao.getObject(Movie.class, map.get("movieid"));
			ResultCode<MovieVo> ResultCode = VoCopyUtil.copyProperties(MovieVo.class, movie);
			if(!ResultCode.isSuccess()){
				continue;
			}
			MovieVo vo = ResultCode.getRetval();
			vo.setGeneralmark(VmUtils.getLastMarkStar(movie, "general", markService.getMarkCountByTagRelatedid(TagConstant.TAG_MOVIE, movie.getId()),
					markData));
			vo.setMcpCount(Integer.parseInt(map.get("mpiCount") + ""));
			vos.add(vo);
		}
		Collections.sort(vos, new MultiPropertyComparator<MovieVo>(new String[]{"mcpCount"}, new boolean[]{false}));
		return ResultCode.getSuccessReturn(vos);
	}
	
	private List<Map<Long,Long>> getCurMovieIdByCinemaIdAndDate(long cinemaId, Date playDate){
		String key = CacheConstant.buildKey("plcxjwyinYgetCinemaMpiCountVoInteface", cinemaId, playDate);
		List<Map<Long,Long>> list = (List<Map<Long,Long>>)this.cacheService.get(CacheConstant.REGION_HALFHOUR, key);
		if(list != null){
			return list;
		}
		DetachedCriteria query = DetachedCriteria.forClass(MoviePlayItem.class);
		query.add(Restrictions.eq("cinemaid", cinemaId));
		query.add(Restrictions.eq("playdate", playDate));
		query.setProjection(Projections.projectionList().add(Projections.groupProperty("movieid"), "movieid")
				.add(Projections.count("movieid"),"mpiCount"));
		query.setResultTransformer(DetachedCriteria.ALIAS_TO_ENTITY_MAP);
		list = hibernateTemplate.findByCriteria(query);
		this.cacheService.set(CacheConstant.REGION_HALFHOUR, key, list);
		return list;
	}

	@Override
	public ResultCode<List<MoviePlayItemVo>> getCinemaMoviePlayItem(long cinemaId, Long movieId, Date playDate) {
		Cinema cinema = baseDao.getObject(Cinema.class, cinemaId);
		if(cinema == null){
			return ResultCode.getFailure("场馆不存在：" + cinemaId);
		}
		List<MoviePlayItem> mpiList = mcpService.getCurMpiList(cinemaId, movieId, playDate);
		Timestamp starttime = new Timestamp(DateUtil.getBeginningTimeOfDay(playDate).getTime());
		Timestamp endtime = new Timestamp(DateUtil.getLastTimeOfDay(playDate).getTime());
		List<OpenPlayItem> opiList = openPlayService.getOpiList(null, cinemaId, movieId, starttime, endtime, true);
		ResultCode<List<MoviePlayItemVo>> result = VoCopyUtil.copyListProperties(MoviePlayItemVo.class, mpiList);
		if(!result.isSuccess()){
			return result;
		}
		List<MoviePlayItemVo> list = result.getRetval();
		List<Long> mpidList = BeanUtil.getBeanPropertyList(opiList, Long.class, "mpid", true);
		if(!mpidList.isEmpty()){
			for(MoviePlayItemVo vo : list){
				if(mpidList.contains(vo.getId())){
					vo.setOpenStatus("openSeller");
				}
			}
		}
		return result;
	}

	@Override
	public ResultCode<List<MovieVo>> getCurMovieList() {
		List<Movie> movieList = mcpService.getCurMovieList();
		mcpService.sortMoviesByMpiCount(AdminCityContant.CITYCODE_SH, movieList);
		return VoCopyUtil.copyListProperties(MovieVo.class, movieList);
	}
	
}
