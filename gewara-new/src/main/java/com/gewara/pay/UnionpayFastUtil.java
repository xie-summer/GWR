/**
 * 
 */
package com.gewara.pay;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import com.gewara.Config;
import com.gewara.constant.PaymethodConstant;
import com.gewara.model.pay.GewaOrder;
import com.gewara.util.GewaLogger;
import com.gewara.util.HttpResult;
import com.gewara.util.HttpUtils;
import com.gewara.util.LoggerUtils;
import com.gewara.util.StringUtil;

/**
 * @author gang.liu@gewara.com
 *
 */
public class UnionpayFastUtil {
	private static final transient GewaLogger dbLogger = LoggerUtils.getLogger(UnionpayFastUtil.class, Config.getServerIp(), Config.SYSTEMID);
	
	private static String version = "1.0.0";
	private static String charset = "UTF-8";
	private static String signMethod = "MD5";
	private static String orderCurrency = "156";//交易币种  人民币
	
	private static String merId;
	private static String prikey;
	
	private static String merId_activity_js;
	private static String prikey_activity_js;
	
	private static String merId_activity_bj;
	private static String prikey_activity_bj;
	
	private static String merId_activity_sz;
	private static String prikey_activity_sz;
	
	private static String merId_activity_gz;
	private static String prikey_activity_gz;
	
	private static String merId_activity_zj;
	private static String prikey_activity_zj;
	
	private static String smsUrl;
	private static String toPayUrl;
	private static String cardActivateStatusUrl;
	private static String bindCardUrl;
	private static final String merAbbr = "格瓦拉生活网";
	private static String proxyDoUnionpayFastUrl;
	private static String unionPayFastSendSmsUrl;
	
	private static boolean initialized = false;
	public static synchronized void init(String propertyFile){
		if(initialized) return;
		Properties props = new Properties();
		try {
			props.load(UnionpayFastUtil.class.getClassLoader().getResourceAsStream(propertyFile));
		} catch (Exception e) {
			throw new IllegalArgumentException("property File Error!!!!", e);
		}
		merId = props.getProperty("merId");
		prikey = props.getProperty("prikey");
		
		merId_activity_js = props.getProperty("merId_activity_js");
		prikey_activity_js = props.getProperty("prikey_activity_js");
		
		merId_activity_bj = props.getProperty("merId_activity_bj");
		prikey_activity_bj = props.getProperty("prikey_activity_bj");
		
		merId_activity_sz = props.getProperty("merId_activity_sz");
		prikey_activity_sz = props.getProperty("prikey_activity_sz");
		
		merId_activity_gz = props.getProperty("merId_activity_gz");
		prikey_activity_gz = props.getProperty("prikey_activity_gz");
		
		merId_activity_zj = props.getProperty("merId_activity_zj");
		prikey_activity_zj = props.getProperty("prikey_activity_zj");
		
		smsUrl = props.getProperty("smsUrl");
		cardActivateStatusUrl = props.getProperty("cardActivateStatusUrl");
		toPayUrl = props.getProperty("toPayUrl");
		bindCardUrl = props.getProperty("bindCardUrl");
		
		proxyDoUnionpayFastUrl = props.getProperty("proxyDoUnionpayFastUrl");
		unionPayFastSendSmsUrl = props.getProperty("unionPayFastSendSmsUrl");
	}
	
	public static String getProxyDoUnionpayFastUrl(){
		return proxyDoUnionpayFastUrl;
	}
	public static String getUnionPayFastSendSmsUrl(){
		return unionPayFastSendSmsUrl;
	}
	
	public static String getBindCardUrl(){
		return bindCardUrl;
	}
	public static HttpResult sendSms(GewaOrder order,String cardNumber,String phoneNumber){
		String[] prik = getPrik(order.getPaymethod());
		Map<String, String> params = new HashMap<String, String>();
		params.put("acqCode", "");//收单机构当商户直接与银联互联网系统相连时，该域可不出现当商户通过其他系统间接与银联互联网系统相连时，该域必须出现
		params.put("charset", charset);
		params.put("version", version);
		params.put("merAbbr", merAbbr);
		params.put("merId", prik[0]);
		params.put("merReserved", "{cardNumber=" + cardNumber + "&phoneNumber=" + phoneNumber + "}");
		params.put("orderAmount", order.getDue() + "00");
		params.put("orderCurrency",orderCurrency);//人民币
		params.put("orderNumber", order.getTradeNo());
		
		params.put("signature", getSign(params,prik[1]));
		params.put("signMethod", signMethod);
		HttpResult result = HttpUtils.postUrlAsString(smsUrl, params);
		dbLogger.warn("send sms response: " + result.getResponse());
		return result;
	}
	
	public static HttpResult getCardActivateStatus(String payMethod, String cardNumber){
		String[] prik = getPrik(payMethod);
		Map<String, String> params = new HashMap<String, String>();
		params.put("acqCode", "");//收单机构当商户直接与银联互联网系统相连时，该域可不出现当商户通过其他系统间接与银联互联网系统相连时，该域必须出现
		params.put("charset", charset);
		params.put("merId", prik[0]);
		params.put("merReserved", "{cardNumber=" + cardNumber + "}");
		params.put("version", version);
		params.put("signature", getSign(params,prik[1]));
		params.put("signMethod", signMethod);
		HttpResult result = HttpUtils.postUrlAsString(cardActivateStatusUrl, params);
		dbLogger.warn("getCardActivateStatus response: " + result.getResponse());
		return result;
	}
	
	public static Map<String,String> getToBindCardParams(String payMethod,String cardNumber){
		String[] prik = getPrik(payMethod);
		Map<String, String> params = new HashMap<String, String>();
		params.put("acqCode", "");//收单机构当商户直接与银联互联网系统相连时，该域可不出现当商户通过其他系统间接与银联互联网系统相连时，该域必须出现
		params.put("charset", charset);
		params.put("merId", prik[0]);
		params.put("merReserved", "{cardNumber=" + cardNumber + "}");
		params.put("version", version);
		params.put("signature", getSign(params,prik[1]));
		params.put("signMethod", signMethod);
		return params;
	}
	
	public static Map<String,String> getUnionFastaJSBindCardParams(String cardNumber){		
		return getToBindCardParams(PaymethodConstant.PAYMETHOD_UNIONPAYFAST_ACTIVITY_JS,cardNumber);
	}
	
	public static Map<String,String> getUnionFastaBJBindCardParams(String cardNumber){		
		return getToBindCardParams(PaymethodConstant.PAYMETHOD_UNIONPAYFAST_ACTIVITY_BJ,cardNumber);
	}
	
	public static HttpResult sendPay(Map<String, String> params){
		HttpResult result = HttpUtils.postUrlAsString(toPayUrl, params);
		dbLogger.warn("sendPay response: " + result.getResponse());
		return result;
	}
	
	public static boolean checkSign(String payMethod, Map<String, String> params){
		String[] prik = getPrik(payMethod);
		String signature = params.remove("signature");
		String signatureMethod = params.remove("signMethod");
		String sign = getSign(params,prik[1]);
		if(StringUtils.equalsIgnoreCase(signMethod, signatureMethod)){
			if(StringUtils.equals(signature, sign)){
				return true;
			}
		}
		return false;
	}
	
	public static Map<String, String> parseUnionpayResponse(String str){
		Map<String, String> responsees = new HashMap<String, String>();
		if(StringUtils.isNotBlank(str)){
			String cupReserved = "";
			if(StringUtils.indexOf(str, "{") != -1){
				cupReserved =  str.substring(StringUtils.indexOf(str, "{"),StringUtils.indexOf(str, "}") + 1);
			}
			String[] responseStr = StringUtils.split(StringUtils.replace(str, "cupReserved=" + cupReserved, ""),"&");
			for(String response : responseStr){
					String[] values = StringUtils.split(response,"=");
					if(values.length == 2){
						responsees.put(values[0], values[1]);
					}else{
						responsees.put(values[0], "");
					}
			}
			if(StringUtils.indexOf(str,"cupReserved") != -1){
				responsees.put("cupReserved", cupReserved);
			}
		}
		return responsees;
	}
	
	private static String getSign(Map<String, String> payMap, String privatekey){
		return StringUtil.md5(joinMapValue(payMap, '&') + StringUtil.md5(privatekey));
	}
	
	private static String joinMapValue(Map<String, String> map, char connector) {
		StringBuffer b = new StringBuffer();
		Set<String> keys = map.keySet();
		Object[] keysArray = keys.toArray();
		Arrays.sort(keysArray);
		for (Object key : keysArray) {
			b.append(key);
			b.append('=');
			b.append(map.get(key));
			b.append(connector);
		}
		return b.toString();
	}
	
	private static String[] getPrik(String payMethod){
		if(StringUtils.equals(payMethod, PaymethodConstant.PAYMETHOD_UNIONPAYFAST_ACTIVITY_JS)){
			return new String[]{merId_activity_js, prikey_activity_js};
		}else if(StringUtils.equals(payMethod, PaymethodConstant.PAYMETHOD_UNIONPAYFAST_ACTIVITY_BJ)){
			return new String[]{merId_activity_bj, prikey_activity_bj};
		}else if(StringUtils.equals(payMethod, PaymethodConstant.PAYMETHOD_UNIONPAYFAST_ACTIVITY_SZ)){
			return new String[]{merId_activity_sz, prikey_activity_sz};
		}else if(StringUtils.equals(payMethod, PaymethodConstant.PAYMETHOD_UNIONPAYFAST_ACTIVITY_GZ)){
			return new String[]{merId_activity_gz, prikey_activity_gz};
		}else if(StringUtils.equals(payMethod, PaymethodConstant.PAYMETHOD_UNIONPAYFAST_ACTIVITY_ZJ)){
			return new String[]{merId_activity_zj, prikey_activity_zj};
		}
		return new String[]{merId, prikey};
	}
}
