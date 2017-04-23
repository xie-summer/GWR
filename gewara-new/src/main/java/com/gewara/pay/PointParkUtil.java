/**
 * 
 */
package com.gewara.pay;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import com.gewara.Config;
import com.gewara.constant.sys.LogTypeConstant;
import com.gewara.helper.order.GewaOrderHelper;
import com.gewara.model.api.ApiUser;
import com.gewara.model.pay.GewaOrder;
import com.gewara.model.pay.TicketOrder;
import com.gewara.support.ErrorCode;
import com.gewara.util.DateUtil;
import com.gewara.util.GewaLogger;
import com.gewara.util.HttpResult;
import com.gewara.util.HttpUtils;
import com.gewara.util.JsonUtils;
import com.gewara.util.LoggerUtils;
import com.gewara.util.StringUtil;
import com.gewara.util.VmUtils;

/**
 * @author Administrator
 *
 */
public class PointParkUtil {
	private static final transient GewaLogger dbLogger = LoggerUtils.getLogger(JFTUtil.class, Config.getServerIp(), Config.SYSTEMID);
	public static final String TPI = "000001";
	public static final String FORMAT = "JSON";
	public static Map<String, String> getParamsMap(ApiUser partner){
		return VmUtils.readJsonToMap(partner.getOtherinfo());
	}
	public static Map<String, String> getSign(Map<String, String> params, GewaOrder order, ApiUser partner){
		params.remove("payurl");
		params.remove("checkvalue");
		params.remove("addtime");
		params.put("tpi", PointParkUtil.TPI);
		params.put("timestamp", order.getAddtime().getTime()+"");
		params.put("addtime", DateUtil.format(order.getAddtime(), "yyyy-MM-dd HH:mm:ss"));
		params.put("amount", order.getDue()+".00");
		params.put("memberid", GewaOrderHelper.getPartnerUkey(order));
		String plaintext = JsonUtils.writeMapToJson(params);
		params = new HashMap<String, String>();
		params.put("payurl", partner.getAddOrderUrl());
		params.put("format", PointParkUtil.FORMAT);
		params.put("sign", StringUtil.md5(plaintext+partner.getPrivatekey()));
		params.put("plaintext", plaintext);
		return params;
	}
	
	public static Map<String, String> getLoginParams(Long mpid, String absPath, String privatekey){
		Map<String, String> params = new HashMap<String, String>();
		params.put("tpi", PointParkUtil.TPI);
		params.put("timetamp", System.currentTimeMillis()+"");
		if(mpid!=null) {
			params.put("backURL", absPath+"/partner/pointpark/chooseSeat.xhtml?mpid="+mpid);
		}else {
			params.put("backURL", absPath+"/partner/pointpark/opiList.xhtml");
		}
		String plaintext = JsonUtils.writeMapToJson(params);
		params = new HashMap<String, String>();
		params.put("format", PointParkUtil.FORMAT);
		params.put("plaintext", plaintext);
		params.put("sign", StringUtil.md5(plaintext+privatekey));
		return params;
	}
	public static String getQryResult(TicketOrder order, String payseqno, ApiUser partner){
		String paidAmount = order.getDue()+".00";
		String tradeno = order.getTradeNo();
		Map<String, String> params = new LinkedHashMap<String, String>();
		params.put("tpi", TPI);
		params.put("timetamp", System.currentTimeMillis()+"");
		params.put("tradeno", tradeno);
		params.put("paidAmount", paidAmount);
		params.put("payseqno", payseqno);
		String plaintext = JsonUtils.writeMapToJson(params);
		params = new HashMap<String, String>();
		params.put("format", FORMAT);
		params.put("plaintext", plaintext);
		params.put("sign", StringUtil.md5(plaintext+partner.getPrivatekey()));
		String qryUrl = getParamsMap(partner).get("qryUrl");
		HttpResult code = HttpUtils.postUrlAsString(qryUrl, params);
		if(code.isSuccess()) return code.getResponse();
		dbLogger.errorWithType(LogTypeConstant.LOG_TYPE_ORDER_PAY, "PayError" + params);
		return "qry pointpark error";
 	}
	public static ErrorCode<String> validParams(String format, String plaintext, String sign){
		if(!StringUtils.equalsIgnoreCase(format, FORMAT)) return ErrorCode.getFailure("");
		if(StringUtils.isBlank(plaintext) || StringUtils.isBlank(sign)) return ErrorCode.getFailure("");
		Map<String, String> plainMap = VmUtils.readJsonToMap(plaintext);
		String tpi = plainMap.get("tpi");
		if(!StringUtils.equalsIgnoreCase(tpi, TPI)) return ErrorCode.getFailure("");
		return ErrorCode.getSuccessReturn(plainMap.get("memberId"));
 	}
}
