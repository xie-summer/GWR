package com.gewara.service.member;

import com.gewara.model.pay.SMSRecord;
import com.gewara.support.ErrorCode;

public interface BindMobileService {
	/**
	 * 重新刷新验证码
	 * @param tag
	 * @param mobile
	 * @return
	 */
	ErrorCode<SMSRecord> refreshBindMobile(String tag, String mobile, String ip);
	ErrorCode<SMSRecord> refreshBindMobile(String tag, String mobile, String ip, String msgTemplate);
	/**
	 * 后台管理人员使用发短信
	 * @param tag
	 * @param mobile
	 * @param ip
	 * @param msgTemplate
	 * @return
	 */
	ErrorCode<SMSRecord> refreshNoSecurityBindMobile(String tag, String mobile, String ip, String msgTemplate);
	/**
	 * 发送手机动态码，无IP限制，“电商后台专用”
	 * @param tag
	 * @param mobile
	 * @param ip
	 * @param msgTemplate
	 * @return
	 */
	ErrorCode<SMSRecord> refreshBMByAdmin(String tag, String mobile, String ip, String msgTemplate);
	
	/**
	 * 测试成功后次数直接用完
	 * @param tag
	 * @param mobile
	 * @param checkpass
	 * @return ERRORCODE分两种：可重试或失效，前台区分
	 */
	ErrorCode checkBindMobile(String tag, String mobile, String checkpass);
	/**
	 * 预先检测，次数增1
	 * @param tag
	 * @param mobile
	 * @param checkpass
	 * @return
	 */
	ErrorCode preCheckBindMobile(String tag, String mobile, String checkpass);

	boolean getAndUpdateToken(String type, String ip, int checkcount);
	boolean isNeedToken(String type, String ip, int checkcount);
}
