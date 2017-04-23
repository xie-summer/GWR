package com.gewara.constant;

import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.collections.map.UnmodifiableMap;

import com.gewara.constant.content.SignName;

public class PointConstant {
	public static final Integer LOGIN_REWARDS_DAYNUM = 7; 					//积分奖励周期
	public static final String TAG_CONFIRM = "confirm"; 					//用户注册高级确认
	public static final String TAG_LOGIN_ACTIVIRY = "loginactivity";		//每日积分领
	public static final String TAG_LOGIN_ACTIVIRY_REWARDS = "loginrewards";	//每日积分奖励
	public static final String TAG_INVITED_FRIEND_TICKET = "invitefriend_ticket";	//邀请好友订票
	public static final String TAG_INVITED_FRIEND_GOODS = "invitefriend_goods";		//邀请好友买商品
	public static final String TAG_TRADE = "trade";					//交易相关
	public static final String TAG_CHARGETOWABI = "chargetowabi";	//充值瓦币
	
	/**退款交易**/
	public static final String TAG_REFUND_TRADE = "trade_refund";
	
	public static final String TAG_SHARE_ORDER = "share_order";		//分享购票
	public static final String TAG_PUBSALE="pubsale";				//竞拍
	public static final String TAG_ADD_INFO = "addinfo";			//添加信息(影片、场馆等）
	public static final String TAG_CORRECT = "correct";				//纠错(影片、场馆、项目等）
	public static final String TAG_CONTENT = "content";				//手动输入原因
	public static final String TAG_ACCUSATION ="accusation";		//举报
	
	//public static final String TAG_CHARGE = "charge";				//本日充值
	//public static final String TAG_BAY_GOODS = "bayGoods";
	//public static final String TAG_ADDMICRO_WAP = "microwap";		//发表手机wap哇啦
	//public static final String TAG_ADDMICRO_WEB = "microweb";		//发表网页版哇啦

	//public static final String TAG_ATTACHMOVIE="attachmovie";		//添加剧情加积分
	//public static final String TAG_ATTACHPICTURE="attachpicture";	//添加图片加积分
	//public static final String TAG_ATTACHVIDEO="attachvideo";		//添加视频加积分
	//public static final String TAG_ACTIVITY="activity";			//参加活动消耗积分
	//public static final String TAG_SUBJECT_ACTIVITY="subject";	//参加专题活动赠送或消耗积分后面可拼专题标识+

	//public static final String TAG_PAYMENT = "pay";				//充值
	//public static final String TAG_INVITED_FRIEND = "invite"; 	//邀请好友注册gewara
	//public static final String TAG_INVITE = "login";				//用户注册
	//public static final String TAG_NEW_TASK = "newtask";			//新手任务
	//public static final String TAG_BANKTOWABI = "banktowabi";		//账户金额转化为瓦币
	
	//public static final String TAG_OTHER = "other";
	
	public static final String FREEBACK_COMMENTCINEMA = "commentcinema"; //观后感：点评场所
	public static final String FREEBACK_COMMENTMOVIE = "commentmovie"; //一句话影评
	public static final String FREEBACK_COMMENTDRAMA = "commentdrama"; //一句话剧评
	public static final String FREEBACK_COMMENTSPORT = "commentsport"; //运动场馆点评
	public static final String REPLY_MESSAGEMOVIE = "replymessagemovie";		//电影回复短信
	public static final String REPLY_MESSAGEDRAMA = "replymessagedrama";		//话剧回复短信
	public static final String REPLY_MESSAGESPORT = "replymessagesport";		//运动回复短信
	public static final String FREEBACK_DIARYMOVIE = "diarymovie"; //观后感:发表影评
	public static final String FREEBACK_COMMENTTHEATRE = "commenttheatre"; //观后感：点评剧院
	public static final String FREEBACK_DIARYDRAMA = "diarydrama"; //观后感:发表剧评
	public static final String UPDATE_PLACE = "update_place";//更新场馆信息
	public static final String PERFECT_PLACE = "perfect_place";//完善场馆信息
	public static final String ADD_ITEM = "add_item";//添加项目信息
	public static final String UPDATE_ITEM = "update_item";//更新项目信息
	public static final String PERFECT_ITEM = "perfect_item";//完善项目信息
	public static final String POINT_EXCAHNGE = "pointexchange";

	public static final Integer SCORE_CORR = 20;//纠错加分
	public static final Integer SCORE_ADDMOVIE = 20;//添加电影加分
	public static final Integer SCORE_ADDPLACE = 20;//添加场馆加分
	public static final Integer SCORE_FREEBACK = 30;//观后感
	
	public static final Integer SCORE_USERCONFIRM = 50;		//用户高级确认加积分 验证邮箱
	public static final Integer SCORE_USERSENDWALA = 2;		//新手任务发哇啦加积分
	public static final Integer SCORE_USERJOINCOMMU = 3;	//新手任务加圈子加积分
	public static final Integer SCORE_USERFIVEFRIEND = 5;	//新手任务加关注加积分
	public static final Integer SCORE_USERHEADPIC = 10;		//新手任务更新头像加积分
	public static final Integer SCORE_USERBINDMOBILE = 20;	//新手任务绑定手机加积分
	public static final Integer SCORE_USERMOVIECOMMENT = 5;	//新手任务写影评加积分
	
	public static final Integer SCORE_USERREGISTER = 100;	//用户注册
	public static final Integer SCORE_LOGIN_REWARDS = 10;	//连续登录奖励
	public static final Integer SCORE_ACCUSATION = 5; 		//举报
	public static final Map<String, String> pointTagMap;
	
	static{
		Map<String, String> tmp = new LinkedHashMap<String, String>();
		tmp = new LinkedHashMap<String, String>();
		tmp.put(POINT_EXCAHNGE, "积分兑换");
		tmp.put(TAG_CONTENT, "手动输入原因");
		tmp.put(TAG_LOGIN_ACTIVIRY_REWARDS, "每日积分奖励");
		tmp.put(TAG_TRADE, "订单成交");
		tmp.put(TagConstant.AGENDA_ACTION_PUBSALE, "竞拍");
		tmp.put(FREEBACK_COMMENTCINEMA, "点评场所");
		tmp.put(SignName.DRAWACTIVITY, "抽奖活动");
		tmp.put(TAG_LOGIN_ACTIVIRY, "每日积分领取");
		tmp.put(FREEBACK_COMMENTTHEATRE, "观后感：点评剧院");
		tmp.put(TAG_CONFIRM, "高级确认");
		tmp.put(TAG_INVITED_FRIEND_TICKET, "邀请好友订票");
		tmp.put(FREEBACK_COMMENTDRAMA, "一句话剧评");
		tmp.put(TAG_ACCUSATION, "举报");
		tmp.put(FREEBACK_DIARYDRAMA, "观后感:发表剧评");
		tmp.put(FREEBACK_DIARYMOVIE, "观后感:发表影评");
		//tmp.put(TAG_ATTACHPICTURE, "上传图片");
		//tmp.put(TAG_ADDMICRO_WAP, "发表手机wap哇啦");
		//tmp.put(TAG_ATTACHMOVIE, "添加剧情加积分");
		//tmp.put(TAG_ATTACHVIDEO, "添加视频加积分");
		//tmp.put(TAG_CHARGE, "本日充值");
		//tmp.put(TAG_NEW_TASK, "新手任务");
		//tmp.put(TAG_INVITE, "用户注册");
		//tmp.put(TAG_INVITED_FRIEND, "邀请好友注册gewara");
		//tmp.put(TAG_PAYMENT, "充值");
		tmp.put(TAG_CORRECT, "纠错(影片、场馆、项目等）");
		tmp.put(TAG_ADD_INFO, "添加信息(影片、场馆等）");
		pointTagMap = UnmodifiableMap.decorate(tmp);
	}
	
}
