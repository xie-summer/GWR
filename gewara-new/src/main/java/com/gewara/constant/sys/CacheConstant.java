package com.gewara.constant.sys;

import java.sql.Timestamp;
import java.util.Date;

import com.gewara.util.DateUtil;

public abstract class CacheConstant {
	//缓存区域
	public static final String REGION_HALFMIN = "halfMin";	//30秒
	public static final String REGION_ONEMIN = "oneMin";
	public static final String REGION_TENMIN = "tenMin";
	public static final String REGION_TWENTYMIN = "twentyMin";
	public static final String REGION_HALFHOUR = "halfHour";
	public static final String REGION_ONEHOUR = "oneHour";
	public static final String REGION_TWOHOUR = "twoHour";
	public static final String REGION_HALFDAY = "halfDay";
	public static final String REGION_ONEDAY = "oneDay";

	public static final String REGION_API_LOGINAUTH = "apiLoginAuth";
	
	public static final int SECONDS_TENMIN = 60*10;
	public static final int SECONDS_TWENTYMIN = 60*20;
	public static final int SECONDS_HALFHOUR = 60*30;
	public static final int SECONDS_OPENSEAT = 60*60*8;
	public static final int SECONDS_HALFDAY = 60*60*12;
	// KEY 集合
	public static final String KEY_TICKET_VALIDTIME_ = "TICKET_VALIDTIME_";
	public static final String KEY_FILTERKEY = "BLOG_FILTER_KEY";	// 内容过滤
	public static final String KEY_MOBILEALLCINEMASLIST = "key_mobileAllCinemasList";
	public static final String KEY_MOBILESUBWAY = "key_mobileSubway";	//地铁线路API
	public static final String KEY_CUR_MPI_CACHENAME = "key_mobile_cur_mpi_list";	//根据影院影片和日期获取排片
	public static final String KEY_RECENTLYGETPOINT = "recentlygetpoint";	//最近领取红包的用户List
	public static final String KEY_LUCKGETPOINT = "luckgetpoint"; //最近领取红包大于5的用户List
	public static final String KEY_CAPTCHA = "CAP_";
	public static final String KEY_CITY_KEY = "relateCitys";
	public static final String KEY_POINTUPDATE = "pointUpdate";
	public static final String KEY_DIARYUPDATE = "diaryUpdate";
	public static final String KEY_COMMENTUPDATE = "commentUpdate";
	public static final String KEY_PARTNER_MOVIE_DATE = "pmd";
	public static final String KEY_HOT_MICROMEMBER = "hotMicroMember";
	public static final String KEY_DISABLETIME = "disableTime";
	public static final String KEY_ERRORCOUNT = "errorCount";
	public static final String KEY_SAOPI = "saopi";
	public static final String KEY_PLAYCINEMALISTBYMOVIEID = "key_playCinemaListByMovieId";
	public static final String KEY_USER_MSGCOUNT_ = "USER_MSGCOUNT_";
	public static final String KEY_WAP_BOOKINGCINEMALIST = "key_wap_booking_cinema_list";
	public static final String KEY_MOVIEPLAYITEM_MOVIEID_CITYCODE_PLAYDATE_COUNT = "key_movieplayitem_movieid_citycode_playdate_count";	//排片数量
	public static final String KEY_ACTIVITY_COUNT = "key_activity_count";		//活动数量
	public static final String KEY_NEWS_COUNT = "key_news_count";					//新闻数量
	public static final String KEY_PICTURE_COUNT = "key_picture_count";			//照片数量
	public static final String KEY_VIDEO_COUNT = "key_video_count";				//预告片数量
	public static final String KEY_COMMENT_COUNT = "key_comment_count";			//哇啦数量
	public static final String KEY_DIARY_COUNT = "key_diary_count";				//影评数量
	public static final String KEY_COMMU_COUNT = "key_commu_count";				//圈子数量
	public static final String KEY_SPORTITEM_ITEMID_PLAYDATE_COUNT = "key_sportitem_itemid_playdate_count"; //运动项目场馆开放数量
	public static final String KEY_SPORTITEM_ITEMID_SPORTCOUNT = "key_sportitem_itemid_sportcount";
	public static final String KEY_OPICINEMAIDLIST = "key_opiCinemaidList";
	public static final String KEY_CLICKTIMES = "key_clicktimes";
	public static final String KEY_MOVIECACHEPOOL = "key_moviecachepool";
	public static final String KEY_OPIGATHER = "key_opigather_"; //场次收集ID
	public static final String KEY_SPORTGOODS_TIMEOUT = "sportgoods_timeout";
	public static final String KEY_SPORT_OPENITEM = "sport_openitem";
	public static final String KEY_SPORT_INDEX_HOTMEMBER = "sport_index_hotmember";//运动首页运动达人
	public static final String KEY_HOTSPOT_CINEMA_MPI_CACHE = "hotspot_cinema_mpi_cache_new";//热门场次座位缓存
	public static final int CACHE_ = 60 * 20;
	/*private static final Map<String, Integer> regionCacheTime;
	static{
		Map<String, Integer> tmp = new HashMap<String, Integer>();
		tmp.put(CacheConstant.REGION_HALFMIN, 30);
		tmp.put(CacheConstant.REGION_ONEMIN, 60);
		tmp.put(CacheConstant.REGION_TENMIN, 60 * 10);
		tmp.put(CacheConstant.REGION_TWENTYMIN, 60 * 20);
		tmp.put(CacheConstant.REGION_HALFHOUR, 60 * 30);
		tmp.put(CacheConstant.REGION_ONEHOUR, 60 * 60);
		tmp.put(CacheConstant.REGION_TWOHOUR, 60 * 60 * 2);
		tmp.put(CacheConstant.REGION_HALFDAY, 60 * 60 * 12);
		tmp.put(CacheConstant.REGION_ONEDAY, 60 * 60 * 24);
		tmp.put(CacheConstant.REGION_OPENSEAT, 60 * 60 * 12); //12hour
		//tmp.put(CacheConstant.REGION_CINEMATICKET, 60 * 60 * 12);
		//tmp.put(CacheConstant.REGION_SPORTTICKET, 60 * 60 * 12);
		regionCacheTime = UnmodifiableMap.decorate(tmp);
	}*/
	public static String buildKey(String pre, Object... params){
		String key = pre;
		for(Object p:params){
			String v = null;
			if(p!=null){
				if(p instanceof Timestamp) v = DateUtil.format((Timestamp)p, "yyyyMMddHHmmss");
				else if(p instanceof Date) v = DateUtil.format((Date)p, "yyyyMMdd");
				else v = "" + p;
			}
			key += v + "|" ;
		}
		return key;
	}
}
