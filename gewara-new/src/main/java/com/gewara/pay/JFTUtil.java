/**
 * 
 */
package com.gewara.pay;

import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import com.gewara.Config;
import com.gewara.constant.sys.LogTypeConstant;
import com.gewara.model.api.ApiUser;
import com.gewara.model.pay.TicketOrder;
import com.gewara.util.GewaLogger;
import com.gewara.util.HttpResult;
import com.gewara.util.HttpUtils;
import com.gewara.util.LoggerUtils;
import com.gewara.util.StringUtil;
import com.gewara.util.VmUtils;

/**
 * @author Administrator
 *
 */
public class JFTUtil {
	private static final transient GewaLogger dbLogger = LoggerUtils.getLogger(JFTUtil.class, Config.getServerIp(), Config.SYSTEMID);
	private static Map<String, String> getOtherMap(ApiUser partner){
		return VmUtils.readJsonToMap(partner.getOtherinfo());
	}
	public static String getQryResult(TicketOrder order, String payseqno, ApiUser partner){
		Map<String, String> otherMap = getOtherMap(partner);
		String userid = otherMap.get("userid");
		String key = otherMap.get("key");
		String pass = otherMap.get("pass");
		String modetype = otherMap.get("modetype");
		String chaserialnumber = otherMap.get("chaserialnumber");
		String terserialnumber = otherMap.get("terserialnumber");
		String action = otherMap.get("action");
		String qryUrl = otherMap.get("qryUrl");
		
		Map<String, String> params = new LinkedHashMap<String, String>();
		params.put("userid", userid);
		params.put("key", StringUtil.md5(md5By16(StringUtil.md5(pass))+key));
		params.put("modetype", modetype);
		params.put("chaserialnumber", chaserialnumber);
		params.put("terserialnumber", terserialnumber);
		params.put("action", action);
		
		params.put("tradeno", order.getTradeNo());
		params.put("paidamount", order.getDue()+"");
		params.put("payseqno", payseqno);
		params.put("checkvalue", StringUtil.md5(order.getTradeNo()+payseqno+order.getDue()+partner.getPrivatekey()));
		HttpResult code = HttpUtils.postUrlAsString(qryUrl, params);		
		if(code.isSuccess()) return code.getResponse();
		dbLogger.errorWithType(LogTypeConstant.LOG_TYPE_ORDER_PAY, "" + params + code.getMsg());
		return "qry jifutong error";
	}
	private static String md5By16(String str){
		return StringUtils.substring(str, 8, 24);
	}
	
}
