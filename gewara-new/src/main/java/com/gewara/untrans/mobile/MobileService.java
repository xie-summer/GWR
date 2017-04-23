package com.gewara.untrans.mobile;

import com.gewara.model.pay.SMSRecord;
import com.gewara.support.ErrorCode;

public interface MobileService {
	String CHANNEL_MAS = "mas";
	String CHANNEL_MLINK = "mlink";
	//String CHANNEL_XUANWU = "xuanwu";
	String CHANNEL_GEWAMAIL = "gewamail";
	ErrorCode sendMessage(SMSRecord sms);
}
