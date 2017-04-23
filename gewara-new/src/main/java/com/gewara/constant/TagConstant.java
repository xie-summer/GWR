package com.gewara.constant;

import java.util.Arrays;
import java.util.List;


public class TagConstant {
	public static final String TAG_BARSINGER = "barsinger";			//酒吧歌手
	public static final String TAG_TOPIC = "topic";					//普通无关联哇啦
	public static final String TAG_DIARY = "diary";					//帖子_哇啦
	public static final String TAG_DIARY_MEMBER = "member_diary";
	public static final String TAG_QA = "qa";						//QA_哇啦
	public static final String TAG_QA_MEMBER = "member_qa";			//用户知道
	public static final String TAG_VIDEO = "video";					//上传视频哇啦
	public static final String TAG_PICTURE = "picture";				//上传图片哇啦
	public static final String TAG_MOVIE = "movie";
	public static final String TAG_MOVIE_DOWN= "movie_down"; 		//电影下映
	public static final String TAG_MOVIE_RELEASE= "movie_release"; 	//电影上映
	public static final String TAG_MOVIE_COMMENT= "movie_comment";
	public static final String TAG_CINEMA = "cinema";
	public static final String TAG_GYMCOACH= "gymcoach";			//健身教练
	public static final String TAG_DRAMA = "drama";					//话剧
	public static final String TAG_DRAMASTAR = "dramastar";			//社团、导演、明星
	public static final String TAG_SPORT = "sport";					//运动
	public static final String TAG_AGENCY = "agency";				//机构培训
	public static final String TAG_SPORT_ACTIVITY = "sport_activity";				//场馆活动
	public static final String TAG_CINEMA_ACTIVITY = "cinema_activity";				//影院活动
	public static final String TAG_THEATRE_ACTIVITY = "theatre_activity";			//剧院活动
	public static final String TAG_KTV_ACTIVITY = "ktv_activity";					//ktv活动
	public static final String TAG_BAR_ACTIVITY = "bar_activity";					//bar活动
	public static final String TAG_GYM_ACTIVITY = "gym_activity";					//健身场馆活动
	public static final String TAG_SPORTITEM = "sportservice";						//运动项目
	public static final String TAG_THEATRE = "theatre";         		//剧院
	public static final String TAG_ACTIVITY = "activity";				//活动
	public static final String TAG_ACTIVITY_MEMBER = "member_activity";	//活动
	public static final String TAG_PICTURE_MEMBER = "member_picture";	//管理员传图片
	public static final String TAG_MEMBERPICTURE_MEMBER = "member_memberpicture";	//用户传图片
	public static final String TAG_CONACTIVITY = "conllectactivity";	//关注活动
	public static final String TAG_KTV = "ktv";						//KTV
	public static final String TAG_BAR = "bar";						//酒吧
	public static final String TAG_GYM = "gym";						//健身
	public static final String TAG_GYMCOURSE = "gymcourse";		//健身项目
	public static final String TAG_GYMCARD = "gymcard";		//健身卡
	public static final String TAG_SPORTTRAIN = "sportTrain";		//运动培训
	public static final Integer MULTIPLE_EXPVALUE= 10000;//最低经验值基数
	public static final Integer EXPVALUE_TO_POINT = 1000;//经验值转换成积分
	public static final String TAG_QUESTION = "gewaquestion";						//	问题
	public static final String TAG_COMMU_MEMBER = "member_commu";						//	用户圈子
	public static final String TAG_COMMENT = "member_comment";						//	哇啦
	public static final String TAG_COMMU_ACTIVITY = "commu_activity"; //圈子活动
	public static final String TAG_COMMU = "commu"; //圈子
	public static final String TAG_MEMBER_CINEMA = "member_cinema";						//用户评论影院哇啦
	public static final String TAG_MEMBER_SPORT = "member_sport";						//用户评论运动场馆哇啦
	public static final String TAG_MEMBER_THEATRE = "member_theatre";						//用户评论剧院哇啦
	public static final String TAG_MEMBER_GYM = "member_gym";						//用户评论健身场馆哇啦
	public static final String TAG_POINT = "everyPoint";//每日红包
	
	public static final String TAG_MEMBERCARD = "membercard";	
	
	// 教育经历
	public static final String TAG_EDU = "edu";
	// 工作经历
	public static final String TAG_JOB = "job";
	//适合人群
	public static final String TAG_CROWD = "crowd";
	
	public static final List<String> TAGList = Arrays.asList(new String[]{"cinema", "movie", "theatre", "drama", "dramastar", "bar", "ktv", "gym", "sport", "agency"});
	public static final String FLAG_PIC = "pic";
	public static final String FLAG_VIDEO = "video";

	public static final Integer READ_YES = 1;
	public static final Integer READ_NO = 0;
	public static final Integer READ_STATUS_ALL = -1;
	public static final String STATUS_FDEL = "fdel";
	public static final String STATUS_TDEL = "tdel";
	public static final String STATUS_TOALL = "toall";//管理员发给全站用户的帖子(暂未使用)
	public static final Long ADMIN_FROMMEMBERID = 0L;	// 管理员发送给全站用户, 设置管理员ID为   0
	public static final Long ADMIN_TOMEMBERID = 0L;	// 管理员发送给全站用户, 设置全站用户ID为 0
	
	public static final String DEFAULT_SUBJECT = "站内信";
	public static final Integer MAX_SECOND = 20; //发短信时间间隔，防止刷机
	
	public static final String DATETYPE_LASTWEEK = "lastweek";//上周
	public static final String DATETYPE_THISWEEK = "thisweek";//本周
	public static final String DATETYPE_NEXTWEEK = "nextweek";//下周
	
	public static final String LAST_WEEK_DIR = "lastweekdir";
	public static final String NEXT_WEEK_DIR = "nextweekdir";
	
	public static final String AGENDA_ACTION_TICKET = "ticket";						//买电影票安排生活
	public static final String AGENDA_ACTION_DRAMA = "drama";						//买话剧票安排生活
	public static final String AGENDA_ACTION_SPORT = "sport";						//买运动场馆安排生活
	public static final String AGENDA_ACTION_AGENDA = "agenda";						//自己安排生活
	public static final String AGENDA_ACTION_JOIN_ACTIVITY = "joinactivity";		//参加活动安排生活
	public static final String AGENDA_ACTION_CREATE_ACTIVITY = "createactivity";	//创建活动安排生活
	public static final String AGENDA_ACTION_CREATE_RESERVE = "createreserve";		//发起约战安排生活
	public static final String AGENDA_ACTION_JOIN_RESERVE = "joinreserve";			//参加约战安排生活
	public static final String AGENDA_ACTION_PUBSALE="pubsale";//竞拍
	public static final String AGENDA_ACTION_PRICE5="price5";//5元抢票
	
	
	public static final String RIGHTS_ALBUM_PUBLIC = "album_public";
	public static final String RIGHTS_ALBUM_FRIEND = "album_friend";
	public static final String RIGHTS_ALBUM_PRIVATE = "album_private";
	public static final String ALBUM_PUBLIC = "public";
	public static final String ALBUM_PRIVATE = "private";
	public static final String ALBUM_FRIEND = "friend";
	
	public static final String TAG_DRAMAORDER = "dramaOrder"; //运动购票
	public static final String TAG_SPORTORDER = "sportOrder"; //运动购票
	public static final String TAG_MOVIEORDER = "movieOrder"; //电影购票
	public static final String TAG_JOINACTIVITY = "joinActivity";//短信预约参加活动
	public static final String TAG_ACTIVITYORDER = "activityOrder"; //活动收费
	public static final String TAG_ZHUANTI = "zhuanti";

	public static final String TAG_AGENDA = "agenda";
	public static final String TAG_MOVIEAGENDA_MEMBER = "member_movieagenda"; //电影生活安排
	public static final String TAG_SPORTAGENDA_MEMBER = "member_sportagenda"; //运动生活安排
	public static final String TAG_DRAMAAGENDA_MEMBER = "member_dramaagenda"; //话剧生活安排
	public static final String TAG_AGENDA_MEMBER = "member_agenda";//用户生活安排
	public static final String TAG_SUBJECTACTIVITY = "subjectActivity"; 	//活动专题
	
	public static final String SUBJECT_CHRISTMAS="christmas";
	
	public static final String MEMBER_SPORT = "member_sport";//判断用户一个月内是否预订同一运动场馆用

}
