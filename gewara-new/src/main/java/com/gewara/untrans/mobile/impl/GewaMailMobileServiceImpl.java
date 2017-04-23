/*
 * $Id: SimpleClientExample.java,v 1.3 2008/11/13 07:12:47 hyyang Exp $
 * $Revision: 1.3 $
 * $Date: 2008/11/13 07:12:47 $
 */

package com.gewara.untrans.mobile.impl;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.gewara.api.sms.request.MTRequest;
import com.gewara.api.sms.response.MTResponse;
import com.gewara.api.sms.service.SmsService;
import com.gewara.constant.SmsConstant;
import com.gewara.model.pay.SMSRecord;
import com.gewara.support.ErrorCode;
import com.gewara.untrans.mobile.MobileService;

@Service("gewaMailMobileService")
public class GewaMailMobileServiceImpl implements MobileService ,InitializingBean{
	
	@Autowired@Qualifier("smsService")
	private SmsService smsService;

	/**非移动手机*/
	public final static String BUSTYPE_SHANGHAI_NOTMOBILE = "shanghai_notmobile";
	/**默认通道*/
	public final static String BUSTYPE_SHANGHAI_DEFAULT = "shanghai_default";
	/**立即*/
	public final static String BUSTYPE_SHANGHAI_NOW = "shanghai_now";
	/**手机动态码*/
	public final static String BUSTYPE_SHANGHAI_DYNCODE = "shanghai_dyncode";
	/**手工*/
	public final static String BUSTYPE_SHANGHAI_MANU = "shanghai_manu";
	/**活动手机短信*/
	public final static String BUSTYPE_SHANGHAI_ACTIVITY = "shanghai_activity";
	/**电子卡*/
	public final static String BUSTYPE_SHANGHAI_EC = "shanghai_ec";
	/**提前3小时*/
	public final static String BUSTYPE_SHANGHAI_3H = "shanghai_3h";
	/**观影后10分钟*/
	public final static String BUSTYPE_SHANGHAI_10M = "shanghai_10m";
	/**优惠券过期提醒*/
	public final static String BUSTYPE_SHANGHAI_CO = "shanghai_co";
	/**退款提醒*/
	public final static String BUSTYPE_SHANGHAI_FB = "shanghai_fb";
	/**发票邮寄发送短信*/
	public final static String BUSTYPE_SHANGHAI_INVOICE = "shanghai_invoice";
	/**API立即发送*/
	public final static String BUSTYPE_SHANGHAI_NOW_API = "shanghai_now_api";
	/**手机客户端邀请好友短信*/
	public final static String BUSTYPE_SHANGHAI_INVITE = "shanghai_invite";
	
	private Map<String,String> smstypeMap = new HashMap<String,String>();
	
	@Override
	public final void afterPropertiesSet() throws Exception {
		smstypeMap.put("notmobile", BUSTYPE_SHANGHAI_NOTMOBILE);		
		smstypeMap.put("default", BUSTYPE_SHANGHAI_DEFAULT);
		smstypeMap.put(SmsConstant.SMSTYPE_NOW, BUSTYPE_SHANGHAI_NOW);		
		smstypeMap.put(SmsConstant.SMSTYPE_DYNCODE, BUSTYPE_SHANGHAI_DYNCODE);
		smstypeMap.put(SmsConstant.SMSTYPE_MANUAL, BUSTYPE_SHANGHAI_MANU);		
		smstypeMap.put(SmsConstant.SMSTYPE_ACTIVITY, BUSTYPE_SHANGHAI_ACTIVITY);	
		smstypeMap.put(SmsConstant.SMSTYPE_ECARD, BUSTYPE_SHANGHAI_EC);	
		smstypeMap.put(SmsConstant.SMSTYPE_3H, BUSTYPE_SHANGHAI_3H);	
		smstypeMap.put(SmsConstant.SMSTYPE_10M, BUSTYPE_SHANGHAI_10M);	
		smstypeMap.put(SmsConstant.SMSTYPE_CO, BUSTYPE_SHANGHAI_CO);	
		smstypeMap.put(SmsConstant.SMSTYPE_FB, BUSTYPE_SHANGHAI_FB);	
		smstypeMap.put(SmsConstant.SMSTYPE_INVOICE, BUSTYPE_SHANGHAI_INVOICE);	
		smstypeMap.put(SmsConstant.SMSTYPE_NOW_API, BUSTYPE_SHANGHAI_NOW_API);	
	}
	
	@Override
	public ErrorCode sendMessage(SMSRecord sms) {
		String busType = smstypeMap.get(sms.getSmstype());
		if(StringUtils.isBlank(busType)){
			busType = BUSTYPE_SHANGHAI_DEFAULT;
		}
		
		MTRequest mt = new MTRequest(sms.getId() == null ? "" : String.valueOf(sms.getId()), sms.getContact(), sms.getContent(), busType);
		MTResponse mtResponse = smsService.sendSMS(mt);
		if(mtResponse.isSuccess()) {
			//sms.setSeqno(result.getRetval());
			return ErrorCode.SUCCESS;
		}else{
			return ErrorCode.getFailure(mtResponse.getMsg());
		}
	}
}
