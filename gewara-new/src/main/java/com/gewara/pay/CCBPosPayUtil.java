package com.gewara.pay;

import com.gewara.model.common.GewaConfig;

public class CCBPosPayUtil {
	public static String getPayUrl(GewaConfig gconfig){
		return gconfig.getContent() + "posweb/posorder";
	}
	public static String getRefundUrl(GewaConfig gconfig){
		return gconfig.getContent() + "posweb/refund";
	}
	public static String getPaidResultUrl(GewaConfig gconfig){
		return gconfig.getContent() + "posweb/paidqry";
	}
	public static String getUnlockUrl(GewaConfig gconfig){
		return gconfig.getContent() + "posweb/unlock";
	}
	public static String getLoginUrl(GewaConfig gconfig){
		return gconfig.getContent() + "posweb/login";
	}
	public static String getSettleUrl(GewaConfig gconfig){
		return gconfig.getContent() + "posweb/settle";
	}
	public static String getGewaSettleUrl(GewaConfig gconfig){
		return gconfig.getContent() + "posweb/gewaSettle";
	}
	public static String getGetTradenoUrl(GewaConfig gconfig){
		return gconfig.getContent() + "posweb/getTradeno";
	}
	public static String getPrenoUrl(GewaConfig gconfig){
		return gconfig.getContent() + "posweb/preno";
	}
	public static String getQuickyQryUrl(GewaConfig gconfig){
		return gconfig.getContent() + "posweb/quickyQry";
	}
	public static String getCardQryUrl(GewaConfig gconfig){
		return gconfig.getContent() + "posweb/cardQry";
	}
	public static String getUpdateSecUrl(GewaConfig gconfig){
		return gconfig.getContent() + "posweb/updateSec";
	}
}
