package com.gewara.constant.content;

import java.util.Arrays;
import java.util.List;



public class OpenShareConstant {
	
	public static final String WEIBO_APPKEY = "2536251945";
	public static final String WEIBO_SECRET = "0157289cf7bf64f72c31ba3415991d7e";
	public static final String WEIBO_OAUTH_URL = "https://api.weibo.com/oauth2/authorize";
	public static final String WEIBO_OAUTH_ACCESS_URL = "https://api.weibo.com/oauth2/access_token";
	public static final String WEIBO_OAUTH_UPLOAD_TEXT = "https://api.weibo.com/2/statuses/update.json";
	public static final String WEIBO_OAUTH_UPLOAD_PIC_TEXT = "https://upload.api.weibo.com/2/statuses/upload.json";
	public static final String WEIBO_OAUTH_GET_FRIENDS = "https://api.weibo.com/2/friendships/friends.json";
	public static final String WEIBO_OAUTH_GET_USERSHOW = "https://api.weibo.com/2/users/show.json";
	
	
	//绑定微博状态start
	public static final int WEIBO_BIND_STATUS_UNDEFINED = 0; //未绑定微博
	public static final int WEIBO_BIND_STATUS_EXPIRED = 1;	//绑定时间已过期
	public static final int WEIBO_BIND_STATUS_SUCCESS = 2;	//绑定成功在有效期内
	//绑定微博状态end
	
	
	//分享管理标签start
	public static final String TAG_SHARE_DIARY_MOVIE = "share_diary_movie";							//帖子电影
	public static final String TAG_SHARE_DIARY_DRAMA = "share_diary_drama";							//帖子话剧
	public static final String TAG_SHARE_DIARY_TOPIC = "share_diary_topic";							//帖子普通
	
	public static final String TAG_SHARE_ACTIVITY_JOIN = "share_activity_join";					//活动参加
	public static final String TAG_SHARE_ACTIVITY_LAUNCH = "share_activity_launch";				//活动发起
	
	public static final String TAG_SHARE_TICKET_DRAMA = "share_ticket_drama";						//购票话剧
	public static final String TAG_SHARE_TICKET_MOVIE = "share_ticket_movie";						//购票电影
	
	public static final String TAG_SHARE_WALA_TRANSFER = "share_wala_transfer";					//wala转载
	public static final String TAG_SHARE_WALA_TOPIC = "share_wala_topic";							//wala普通
	public static final String TAG_SHARE_WALA_DRAMA = "share_wala_drama";							//wala话剧
	public static final String TAG_SHARE_WALA_MOVIE = "share_wala_movie";							//wala电影
	public static final String TAG_SHARE_WALA_OTHER = "share_wala_other";							//wala其他
	
	public static final String TAG_SHARE_AGENDA_OTHER = "share_agenda_other";						//生活
	
	public static final String TAG_SHARE_POINT_FESTIVAL = "share_point_festival";					//节日红包
	public static final String TAG_SHARE_POINT_REWARDS = "share_point_rewards";					//连续领取
	public static final String TAG_SHARE_POINT_BIT_POSITIVE = "share_point_bit_positive";		//冒险正分
	public static final String TAG_SHARE_POINT_BIT_NEGATIVE = "share_point_bit_negative";		//冒险负分
	public static final String TAG_SHARE_POINT_BRT = "share_point_brt";								//微博控
	public static final String TAG_SHARE_POINT_STABLE = "share_point_stable";						//稳定型
	public static final String TAG_SHARE_POINT_ORDER = "share_point_order";							//购票领积分分享
	//分享管理标签end
	
	public static final List<String> SHARETAGLIST =  Arrays.asList(new String[]{TAG_SHARE_DIARY_MOVIE, TAG_SHARE_DIARY_DRAMA, TAG_SHARE_DIARY_TOPIC, TAG_SHARE_ACTIVITY_JOIN,
			TAG_SHARE_ACTIVITY_LAUNCH, TAG_SHARE_TICKET_DRAMA, TAG_SHARE_TICKET_MOVIE, TAG_SHARE_WALA_TRANSFER, TAG_SHARE_WALA_TOPIC, TAG_SHARE_WALA_DRAMA, TAG_SHARE_WALA_MOVIE,
			TAG_SHARE_WALA_OTHER, TAG_SHARE_AGENDA_OTHER, TAG_SHARE_POINT_FESTIVAL,TAG_SHARE_POINT_REWARDS, TAG_SHARE_POINT_BIT_POSITIVE, TAG_SHARE_POINT_BIT_NEGATIVE,
			TAG_SHARE_POINT_BRT, TAG_SHARE_POINT_STABLE, TAG_SHARE_POINT_ORDER});
}
