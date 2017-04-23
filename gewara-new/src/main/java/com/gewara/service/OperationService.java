package com.gewara.service;

import java.sql.Timestamp;

import com.gewara.model.common.LastOperation;
import com.gewara.support.ErrorCode;

/**
 * 用户限制操作接口
 * @author acerge(acerge@163.com)
 * @since 7:06:35 PM Sep 7, 2010
 */
public interface OperationService {
	int ONE_HOUR = 60*60;
	int HALF_HOUR = 60 * 30;
	int ONE_MINUTE = 60;
	int HALF_MINUTE = 30;
	int ONE_DAY = ONE_HOUR * 24;//seconds
	int HALF_DAY = ONE_HOUR * 12;
	int ONE_WEEK = ONE_DAY * 7;
	String TAG_ADDCONTENT = "addContent"; 	//用户发表帖子、知道、活动等
	String TAG_REPLYCONTENT = "replyContent"; 	//回复哇啦
	String TAG_REPLY = "reply";				//用户回复帖子、知道、活动等
	String TAG_SENDEAIL = "sendEail"; 		//用户发邮件
	String TAG_ADVISE = "advise"; 			//投诉建议
	String TAG_TREASURE_ADD = "treasure_add";  //添加关注
	String TAG_TREASURE_CANCEL = "treasure_cancel"; //取消关注
	String TAG_ATTACHMOVIE="attachmovie";//添加剧情
	String TAG_SENDTICKETPWD = "sendTicketPWD";	// 发送取票密码
	String TAG_MEMBERMARK = "member_mark"; //用户评分
	String TAG_SUBJECTAGENDA = "subject_agenda"; //5元抢票短信提醒
	
	String TAG_MOBILE_SMS_INVITE="mobile_sms_invite";//手机短信邀请
	String TAG_MOBILE_REG="mobile_reg_limits";//手机注册
	
	/**
	 * 间隔24小时允许一次
	 * @param opkey
	 * @return
	 */
	boolean updateOperationOneDay(String opkey, boolean update);
	/**
	 * 每次操作必须间隔allowIntervalSecond
	 * @param opkey
	 * @param allowIntervalSecond 时间间隔（秒）
	 * @return
	 */
	boolean isAllowOperation(String opkey, int allowIntervalSecond);
	boolean updateOperation(String opkey, int allowIntervalSecond);
	boolean updateOperation(String opkey, int allowIntervalSecond, String secondkey);
	/**
	 * scopeSecond这段时间内（秒）最多允许操作allowNum次
	 * @param opkey
	 * @param scopeSecond 时间范围（秒）
	 * @return
	 */
	boolean isAllowOperation(String opkey, int scopeSecond, int allowNum);
	boolean updateOperation(String opkey, int scopeSecond, int allowNum);
	boolean updateOperation(String opkey, int scopeSecond, int allowNum, String secondkey);
	/**
	 * 每次操作必须间隔allowIntervalSecond且scopeSecond这段时间内（秒）最多允许操作allowNum次
	 * implied condition: allowIntervalSecond * allowNum < scopeSecond
	 * @param opkey
	 * @param allowIntervalSecond 时间间隔（秒）
	 * @param scopeSecond 时间范围（秒）
	 * @param allowNum
	 * @return
	 */
	boolean isAllowOperation(String opkey, int allowIntervalSecond, int scopeSecond, int allowNum);
	boolean updateOperation(String opkey, int allowIntervalSecond, int scopeSecond, int allowNum);
	boolean updateOperation(String opkey, int allowIntervalSecond, int scopeSecond, int allowNum, String secondkey);
	/**
	 * 重置时间间隔
	 * @param opkey
	 * @param secondNum
	 */
	void resetOperation(String opkey, int secondNum);
	/**
	 * 检查Key允许操作的次数，只用于短时间的激烈竞争检测
	 * 用于特价活动时，key=待检查ip + 特殊优惠id
	 * @param key 
	 * @param limitedCount 限制的最大数量
	 * @return
	 */
	ErrorCode<String> checkLimitInCache(String key, int limitedCount);
	ErrorCode<String> updateLoginLimitInCache(String key, int maxnum);
	ErrorCode<String> checkLoginLimitNum(String key, int maxnum);
	LastOperation updateLastOperation(String lastkey, String lastvalue, Timestamp lasttime, Timestamp validtime, String tag);
}
