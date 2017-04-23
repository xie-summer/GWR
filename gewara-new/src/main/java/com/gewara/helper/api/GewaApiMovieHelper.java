package com.gewara.helper.api;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang.StringUtils;

import com.gewara.model.content.Video;
import com.gewara.model.movie.Cinema;
import com.gewara.model.movie.Movie;
import com.gewara.model.movie.MoviePlayItem;
import com.gewara.model.movie.MovieVideo;
import com.gewara.model.ticket.OpenPlayItem;
import com.gewara.util.DateUtil;
import com.gewara.util.VmUtils;

public class GewaApiMovieHelper extends GewaApiHelper{
	private static Map<Long, String> contentMap = new ConcurrentHashMap<Long, String>();
	//电影
	public static Map<String, Object> getMovieData(Movie movie, String logo){
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("movieid", movie.getId());
		params.put("moviename", movie.getMoviename());
		params.put("englishname", movie.getEnglishname());
		params.put("language", movie.getLanguage());
		params.put("length", movie.getLength());
		params.put("logo", logo);
		params.put("actors", movie.getActors());
		params.put("director", movie.getDirector());
		params.put("type", movie.getType());
		params.put("state", movie.getState());
		params.put("imdbid", movie.getImdbid());
		params.put("highlight", movie.getHighlight());
		params.put("minprice", movie.getMinprice());
		params.put("releasedate", movie.getReleasedate());
		params.put("clickedtimes", movie.getClickedtimes());
		params.put("collectedtimes", movie.getCollectedtimes());
		String content = contentMap.get(movie.getId());
		if(content==null){
			content = VmUtils.getHtmlText(movie.getContent(), 10000);
			if(content == null){
				contentMap.put(movie.getId(), "");
			}else {
				contentMap.put(movie.getId(), content);
			}
		}
		params.put("content", content);
		return params;
	}
	//影院
	public static Map<String, Object> getCinemaData(Cinema cinema, String logo, String firstpic){
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("cinemaid", cinema.getId());
		params.put("cinemaname", cinema.getName());
		params.putAll(getBaseInfo(cinema, logo, firstpic));
		return params;
	}
	//电影场次
	public static Map<String, Object> getOpiData(OpenPlayItem opi){
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("mpid", opi.getMpid());
		params.put("movieid", opi.getMovieid());
		params.put("cinemaid", opi.getCinemaid());
		params.put("cinemaname", opi.getCinemaname());
		params.put("moviename", opi.getMoviename());
		params.put("language", opi.getLanguage());
		params.put("edition", opi.getEdition());
		params.put("roomid", opi.getRoomid());
		params.put("roomname", opi.getRoomname());
		params.put("playtime", DateUtil.formatTimestamp(opi.getPlaytime()));
		params.put("closetime", DateUtil.formatTimestamp(opi.getClosetime()));
		params.put("dayotime", opi.getDayotime());
		params.put("dayctime", opi.getDayctime());
		params.put("price", opi.getPrice());
		params.put("gewaprice", opi.getGewaprice());
		params.put("opentype", opi.getOpentype());
		params.put("lockminute", opi.gainLockMinute());
		params.put("maxseat", opi.gainLockSeat());
		int servicefee = opi.getGewaprice() - opi.getCostprice();
		params.put("servicefee", servicefee<0?0:servicefee);
		params.put("remark", VmUtils.getHtmlText(opi.getRemark(), 500));
		putRoomtype(params, opi);
		return params;
	}
	private static void putRoomtype(Map<String, Object> params, OpenPlayItem opi){
		if(opi==null || opi.getSeatnum()==null) return;
		String roomtype = "";
		if(opi.getSeatnum()>200){
			roomtype = "大厅";
		}else if(opi.getSeatnum()>120){
			roomtype = "中厅";
		}
		params.put("roomtype", roomtype);
	}
	public static Map<String, Object> getMpiData(MoviePlayItem mpi){
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("mpid", mpi.getId());
		params.put("movieid", mpi.getMovieid());
		params.put("cinemaid", mpi.getCinemaid());
		params.put("language", mpi.getLanguage());
		params.put("edittion", mpi.getEdition());
		params.put("roomid", mpi.getRoomid());
		params.put("roomname", mpi.getPlayroom());
		params.put("playtime", mpi.getPlaytime());
		params.put("playdate", mpi.getPlaydate());
		params.put("playweek", DateUtil.getCnWeek(mpi.getPlaydate()));
		params.put("price", mpi.getPrice());
		params.put("gewaprice", mpi.getGewaprice());
		params.put("opentype", mpi.getOpentype());
		return params;
	}
	public static Map<String, Object> getMpiData(MoviePlayItem mpi, OpenPlayItem opi){
		Map<String, Object> params = getMpiData(mpi);
		if(opi!=null){
			Map<String, String> otherMap = VmUtils.readJsonToMap(opi.getOtherinfo());
			String title = otherMap.get("sptitle");
			if(StringUtils.equals(title, "明星见面会")){
				params.put("signtype", "starmeet");
			}
		}
		putRoomtype(params, opi);
		return params;
	}
	//电影视频
	public static Map<String, Object> getMovieVideoData(MovieVideo movieVideo){
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("movieid", movieVideo.getMovieid());
		params.put("videoid", movieVideo.getVideoid());
		params.put("img", movieVideo.getImg());
		return params;
	}
	//电影视频
	public static Map<String, Object> getVideoData(Video video){
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("addtime", video.getAddtime());
		params.put("url", video.getUrl());
		params.put("logo", video.getLogo());
		return params;
	}
}
