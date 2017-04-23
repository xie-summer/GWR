package com.gewara.api.service.movie;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import com.gewara.api.vo.ResultCode;
import com.gewara.api.vo.movie.CinemaVo;
import com.gewara.api.vo.movie.MovieGeneralmarkDetailVo;
import com.gewara.api.vo.movie.MovieVo;
import com.gewara.constant.TagConstant;
import com.gewara.model.movie.Cinema;
import com.gewara.model.movie.Movie;
import com.gewara.service.bbs.MarkService;
import com.gewara.service.impl.BaseServiceImpl;
import com.gewara.util.VmUtils;
import com.gewara.util.VoCopyUtil;

public class MovieVoServiceImpl extends BaseServiceImpl implements MovieVoService{
	@Autowired@Qualifier("markService")
	private MarkService markService;
	
	@Override
	public ResultCode<CinemaVo> getCinemaVoById(Long cinemaid) {
		Cinema cinema = baseDao.getObject(Cinema.class, cinemaid);
		return VoCopyUtil.copyProperties(CinemaVo.class, cinema);
	}

	@Override
	public ResultCode<MovieVo> getMovieVoById(Long movieid) {
		Movie movie = baseDao.getObject(Movie.class, movieid);
		ResultCode<MovieVo> result = VoCopyUtil.copyProperties(MovieVo.class, movie);
		if(!result.isSuccess()){
			return result;
		}
		Map markData = markService.getMarkdata(TagConstant.TAG_MOVIE);
		result.getRetval().setGeneralmark(VmUtils.getLastMarkStar(movie, "general", markService.getMarkCountByTagRelatedid(TagConstant.TAG_MOVIE, movie.getId()),
				markData));
		return result;
	}

	@Override
	public ResultCode<List<MovieGeneralmarkDetailVo>> getMovieGeneralmarkDetail(long movieId) {
		Movie movie = baseDao.getObject(Movie.class, movieId);
		if(movie == null){
			return ResultCode.getFailure(ResultCode.CODE_DATA_ERROR, "电影数据不存在！"); 
		}
		List<Map> detailMap = markService.getGradeDetail(TagConstant.TAG_MOVIE, movie.getId());
		return VoCopyUtil.copyListProperties(MovieGeneralmarkDetailVo.class, detailMap);
	}
}
