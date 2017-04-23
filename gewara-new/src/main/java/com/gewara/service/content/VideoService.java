package com.gewara.service.content;

import java.util.List;

import com.gewara.model.content.Video;
import com.gewara.model.movie.MovieVideo;

/**
 * @author <a href="mailto:acerge@163.com">gebiao(acerge)</a>
 * @since 2007-9-28下午02:05:17
 */
public interface VideoService {

	/**
	 * @function tag 必填 relatedid hotvalue
	 * @author bob.hu
	 * @date 2011-10-14 16:35:30
	 */

	List<Video> getVideoListByTag(String tag, Long relatedid, int from, int maxnum);

	Integer getVideoCountByTag(String tag, Long relatedid);

	List<Video> getVideoListByTag(String tag, Long relatedid, Integer hotvalue, String orderField, boolean asc, int from, int maxnum);

	Integer getVideoCountByTag(String tag, Long relatedid, Integer hotvalue);
	/**
	 * 获取当前电影关联的视频ID
	 */
	MovieVideo getMovieVideo(Long movieid);
	
	//List<MovieVideo> getMovieVideoList(Long movieid, int from, int maxnum);

	MovieVideo getMovieVideoByVideoid(String videoid);
}
