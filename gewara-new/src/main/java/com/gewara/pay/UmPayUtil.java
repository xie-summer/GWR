/**
 * 
 */
package com.gewara.pay;

import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;

import com.gewara.Config;
import com.gewara.constant.AdminCityContant;
import com.gewara.constant.sys.LogTypeConstant;
import com.gewara.model.pay.GewaOrder;
import com.gewara.support.ErrorCode;
import com.gewara.util.DateUtil;
import com.gewara.util.GewaLogger;
import com.gewara.util.HttpResult;
import com.gewara.util.HttpUtils;
import com.gewara.util.LoggerUtils;
import com.umpay.SignEnc;
import com.umpay.SignEncException;

/**
 * @author Administrator
 *
 */
public class UmPayUtil {
	private static final transient GewaLogger dbLogger = LoggerUtils.getLogger(UmPayUtil.class, Config.getServerIp(), Config.SYSTEMID);
	//private static final String reqUrl = "http://211.136.93.20:8081/pay/payGateDirectBuyGoods.do";
	private static final String reqUrl_ec = "http://payment.umpay.com/hfwebbusi/pay/direct.do";//"http://114.113.159.207:8756/hfwebbusi/pay/direct.do";
	private static final String reqUrl = reqUrl_ec;//"http://payment.umpay.com/pay/payGateDirectBuyGoods.do";
	private static final String payUrl = "http://www.gewara.com/umpay/replyMsg.xhtml";
	
	private static final String qryUrl_ec = "http://payment.umpay.com/hfwebbusi/order/query.do";//"http://114.113.159.207:8756/hfwebbusi/order/query.do";
	private static final String qryUrl = qryUrl_ec;//"http://payment.umpay.com/webpay/mer3QueryOrder.do";
	public static final String accountUrl = "http://payment.umpay.com/hfwebbusi/bill/trans.dl";
	private static final String merId_sh = "7222";//格瓦拉上海  商户号
	private static final String merId_ec = "7914";//格瓦拉全国  商户号
	private static final String goodsId = "001";
	private static final String notifyUrl = "http://manage.gewara.com/pay/cmPhonePayNotify.xhtml";
	private static String merPriKeyPath = "";
	private static String platCertPath = "";
	static{
		merPriKeyPath = UmPayUtil.class.getClassLoader().getResource("com/gewara/pay/umpay.key.p8").getFile();
		platCertPath = UmPayUtil.class.getClassLoader().getResource("com/gewara/pay/umpay.crt").getFile();
		//merPriKeyPath = UmPayUtil.class.getClassLoader().getResource("com/gewara/pay/umpay_test.key.p8").getFile();
		//platCertPath = UmPayUtil.class.getClassLoader().getResource("com/gewara/pay/umpay_test.crt").getFile();
	}
	public static Map<String, String> getPayParams(GewaOrder order) {
		Map<String, String> params = new LinkedHashMap<String, String>();
		if(StringUtils.equals(AdminCityContant.CITYCODE_SH,order.getCitycode())){
			params.put("merId", merId_sh);
		}else{
			params.put("merId", merId_ec);
		}
		params.put("goodsId", goodsId);
		params.put("mobileId", order.getMobile());
		params.put("orderId", order.getTradeNo());
		params.put("merDate", DateUtil.format(order.getAddtime(), "yyyyMMdd"));
		params.put("amount", order.getDue()+"00");
		params.put("amtType", "02");
		params.put("bankType", "6"); //小额
		params.put("notifyUrl", notifyUrl);
		params.put("merPriv", "");
		params.put("expand", "");
		params.put("version", "3.0");
		String plain = getSignData(params);
		String sign = getSign(plain);
		params.put("sign", sign);
		return params;
	}
	public static String getSign(String data){
		String sign = "";
		try {
			sign = SignEnc.sign(data, merPriKeyPath);
		} catch (SignEncException e) {
			dbLogger.errorWithType(LogTypeConstant.LOG_TYPE_ORDER_PAY, "", e);
		}
		return sign;
	}
	public static String getSignData(Map<String, String> params){
		String sign = "";
		for(String key : params.keySet()){
			String value = params.get(key);
			if(value!=null){
				sign = sign+"&"+key+"=" +value ;
			}
		}
		return sign.substring(1);
	}
	public static Map<String, String> getNetPayParams(GewaOrder order) {
		Map<String, String> params = getPayParams(order);
		String umPayUrl = reqUrl_ec;
		if(StringUtils.equals(AdminCityContant.CITYCODE_SH,order.getCitycode())){
			umPayUrl = reqUrl;
		}
		HttpResult code = HttpUtils.postUrlAsString(umPayUrl, params);
		String result = "fail";
		if(code.isSuccess()){
			String res = code.getResponse();
			if(isValidRes(res,order)) result = "success";
			else {
				dbLogger.warnWithType(LogTypeConstant.LOG_TYPE_ORDER_PAY, "联动优势话费后台直连错误：" + res);
			}
		}
		params = new HashMap<String, String>();
		params.put("payurl", payUrl);
		params.put("submitMethod", "post");
		params.put("tradeNo", order.getTradeNo());
		params.put("response", result);
		return params;
	}
	public static HttpResult qryOrder(GewaOrder order) {
		Map<String, String> params = new LinkedHashMap<String, String>();
		String umQryUrl = qryUrl;
		if(StringUtils.equals(AdminCityContant.CITYCODE_SH,order.getCitycode())){
			params.put("merId", merId_sh);
		}else{
			params.put("merId", merId_ec);
			umQryUrl = qryUrl_ec;
		}
		params.put("goodsId", goodsId);
		params.put("orderId", order.getTradeNo());
		params.put("merDate", DateUtil.format(order.getAddtime(), "yyyyMMdd"));
		params.put("mobileId", order.getMobile());
		params.put("version", "3.0");
		String plain = getSignData(params);
		String sign = getSign(plain);
		params.put("sign", sign);
		
		HttpResult code = HttpUtils.postUrlAsString(umQryUrl, params, null, "gbk");
		dbLogger.error("话费查询：" + code.getMsg());
		dbLogger.error("话费查询：" + code.getResponse());
		if(code.isSuccess()){
			
		}
		return code;
	}
	
	/**
	 * 订单反查
	 * @param order
	 * @return
	 */
	public static ErrorCode queryOrder(GewaOrder order){
		HttpResult  hr = qryOrder(order);
		if(hr.isSuccess()){
			String response = hr.getResponse();
			String[] arr = StringUtils.splitPreserveAllTokens(StringUtils.substringBetween(response, "CONTENT=\"", "\">"), "|");
			if(arr != null && arr.length == 18){
				if(StringUtils.equals(arr[11], "1") && StringUtils.equals(arr[15], "0000")){
					return ErrorCode.getSuccess("联动优势umPay支付反查成功");
				}else{
					return ErrorCode.getFailure("联动优势umPay反查结果失败:" + response);
				}
			}
			return ErrorCode.getFailure("联动优势umPay反查结果解析有误：" + response);
		}
		return ErrorCode.getFailure("联动优势umPay反查http请求错误：" + hr.getMsg());
	}
	
	public static Map<String, String> parseQryResult(String httpResult){
		Map<String, String> map = new HashMap<String, String>();
		String[] arr = StringUtils.splitPreserveAllTokens(StringUtils.substringBetween(httpResult, "CONTENT=\"", "\">"), "|");
		if(arr != null && arr.length == 18){
			map.put("payDate", arr[4]);
			String tranState = "初始";
			if(StringUtils.equals(arr[11], "1")){
				tranState = "成功";
			}else if(StringUtils.equals(arr[11], "2")){
				tranState = "失败";
			}
			map.put("transState", tranState);
		}
		return map;
	}
	
	public static HttpResult getAccount(Date date,GewaOrder order) {
		Map<String, String> params = new LinkedHashMap<String, String>();
		if(StringUtils.equals(AdminCityContant.CITYCODE_SH,order.getCitycode())){
			params.put("merId", merId_sh);
		}else{
			params.put("merId", merId_ec);
		}
		params.put("payDate", DateUtil.format(date, "yyyyMMdd"));
		params.put("version", "3.0");
		String plain = getSignData(params);
		String sign = getSign(plain);
		params.put("sign", sign);
		HttpResult code = HttpUtils.postUrlAsString(accountUrl, params, null, "gbk");
		return code;
	}
	public static boolean verify(String plain, String sign){
		try {
			return SignEnc.verify(plain, sign, platCertPath);
		} catch (SignEncException e) {
			dbLogger.errorWithType(LogTypeConstant.LOG_TYPE_ORDER_PAY, "", e);
			return false;
		}
	}
	private static Map<String,String> getResMap(String response) throws Exception{
		Map<String, String> resMap = new LinkedHashMap<String, String>();
		Pattern p = Pattern.compile("CONTENT=\"(.*?)\">");
      Matcher m = p.matcher(response);
      if(m.find()) {
          String res = m.group(1);
          if(StringUtils.isNotBlank(res)){
         	 String[] params = StringUtils.split(res, "|");
         	 String retMsg = new String(Base64.decodeBase64(params[5].getBytes("GBK")));
         	 resMap.put("merId", params[0]);
         	 resMap.put("goodsId", params[1]);
         	 resMap.put("orderId", params[2]);
         	 resMap.put("merDate", params[3]);
         	 resMap.put("retCode", params[4]);
         	 resMap.put("retMsg", retMsg);
         	 resMap.put("version", params[6]);
         	 resMap.put("sign", params[7]);
          }
      }
      return resMap;
	}
	public static boolean isValidRes(String response,GewaOrder order){
		Map<String, String> resMap = new HashMap<String, String>();
		try {
			resMap = getResMap(response);
		} catch (Exception e) {
			dbLogger.errorWithType(LogTypeConstant.LOG_TYPE_ORDER_PAY, "", e);
		}
		String merId = "";
		if(StringUtils.equals(AdminCityContant.CITYCODE_SH,order.getCitycode())){
			merId = merId_sh;
		}else{
			merId = merId_ec;
		}
		if(StringUtils.equals(resMap.get("merId"), merId) 
			&& StringUtils.equals(resMap.get("goodsId"), goodsId)
			&& StringUtils.equals(resMap.get("retCode"), "0000")){
			return true;
		}
		return false;
	}
	
	public static String getMerIdSH(){
		return merId_sh;
	}
}
