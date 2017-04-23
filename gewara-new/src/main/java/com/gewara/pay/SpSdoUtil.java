package com.gewara.pay;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.collections.map.UnmodifiableMap;
import org.apache.commons.lang.StringUtils;

import com.gewara.Config;
import com.gewara.bank.OrderQuery;
import com.gewara.constant.PaymethodConstant;
import com.gewara.constant.sys.LogTypeConstant;
import com.gewara.model.pay.GewaOrder;
import com.gewara.model.pay.TicketOrder;
import com.gewara.util.DateUtil;
import com.gewara.util.GewaLogger;
import com.gewara.util.HttpResult;
import com.gewara.util.HttpUtils;
import com.gewara.util.LoggerUtils;
import com.gewara.util.StringUtil;

/**
 * 盛大即时到账支付
 * @author acerge(acerge@163.com)
 * @since 3:20:16 PM Sep 1, 2010
 */
public class SpSdoUtil{
	private static final transient GewaLogger dbLogger = LoggerUtils.getLogger(SpSdoUtil.class, Config.getServerIp(), Config.SYSTEMID);
	private static final String version = "3.0";
	private static final String currencyType = "RMB";
	private static final String notifyUrlType ="http";
	private static final String signType ="2";
	private static final Map<String, String> BANK_MAP;
	private static final Map<String, String> KEY_MAP;
	private static final Map<String, String> PAY_MAP;
	private static final List<String> VALID_CHANNEL = Arrays.asList(
			"03"/*盛大卡支付*/,"04"/*银行卡支付*/,"07"/*一点充支付*/,"12"/*大额b2c支付*/,
			"13"/*－b2b支付*/,"14"/*－钱包余额支付*/,"18"/*－手机卡支*/
	);
	private static final String bankGateway = "http://netpay.sdo.com/paygate/ibankpay.aspx";
	private static final String merchantNo1;
	private static final String privatekey1;
	private static final String merchantNo2;
	private static final String privatekey2;
	private static final String paygateway;
	private static final String notifyUrl;
	private static final String postBackUrl;
	private static final String callbackUrl;
	private static final String callbackKey;
	static{
		Properties props = new Properties();
		try {
			props.load(ChinapayUtil.class.getClassLoader().getResourceAsStream("com/gewara/pay/spsdo.properties"));
		} catch (IOException e) {
			dbLogger.errorWithType(LogTypeConstant.LOG_TYPE_ORDER_PAY, "", e);
		}
		merchantNo1 = props.getProperty("merchantNo1");
		privatekey1 = props.getProperty("privatekey1");
		merchantNo2 = props.getProperty("merchantNo2");
		privatekey2 = props.getProperty("privatekey2");
		paygateway = props.getProperty("paygateway");
		notifyUrl = props.getProperty("notifyUrl");
		postBackUrl = props.getProperty("postBackUrl");
		callbackUrl = props.getProperty("callbackUrl");
		callbackKey = props.getProperty("callbackKey");
	}
	//正式

	public static String getCheckValue(String spliter, String merchantNo, String... params){
		String privatekey = KEY_MAP.get(merchantNo);
		String signstr = StringUtils.join(params, spliter) + spliter + privatekey;
		String result = StringUtil.md5(signstr, "gbk");
		return result;
	}
	public static String getPaymethod(String merchantNo){
		return PAY_MAP.get(merchantNo);
	}
	public static boolean isValid(String mac, String merchantNo, String... params){
		String mycheck = getCheckValue("|", merchantNo, params);
		if(StringUtils.equalsIgnoreCase(mycheck, mac)) return true;
		dbLogger.errorWithType(LogTypeConstant.LOG_TYPE_ORDER_PAY, "验证签名错误" + StringUtils.join(params, "|"));
		return false;
	}

	public static boolean sendOrder(TicketOrder order) {
		String orderID = order.getMembername().split("@")[0];
		String ret = order.isPaidSuccess()?"1":"2";
		String paySerialNo = order.getPayseqno();
		String ticketNum = ""+order.getQuantity();
		String amount = ""+order.getDue();
		String spOrderid = order.getTradeNo();
		String mac = StringUtil.md5(orderID + ret + paySerialNo + ticketNum + amount + spOrderid + callbackKey);
		
		Map<String, String> params = new HashMap<String, String>();
		params.put("orderID", orderID);
		params.put("ret", ret);
		params.put("paySerialNo", paySerialNo);
		params.put("ticketNum", ticketNum);
		params.put("amount", amount);
		params.put("spOrderid", spOrderid);
		params.put("mac", mac);
		
		HttpResult result = HttpUtils.postUrlAsString(callbackUrl, params);
		if(StringUtils.containsIgnoreCase(result.getResponse(), "ok") || 
				StringUtils.containsIgnoreCase(result.getResponse(), "0k"/*兼容一下,是零,不是欧*/)){
			return true;
		}else{
			dbLogger.errorWithType(LogTypeConstant.LOG_TYPE_ORDER_PAY, result.getResponse() + ", " + params);
			return false;
		}
	}
	public static String getMac(String... params) {
		return StringUtil.md5(StringUtils.join(params, "") + callbackKey);
	}
	public static Map getNetPayParams(GewaOrder order){
		String merchantNo = KEY_MAP.get(order.getPaymethod());
		String orderTime = DateUtil.format(order.getAddtime(), "yyyyMMddHHmmss");
		String payChannel = "", bankCode="";
		if(StringUtils.isNotBlank(order.getPaybank())){
			String[] pay = StringUtils.split(order.getPaybank(), ":");
			if(isValidPayChannel(pay[0])) payChannel = pay[0];
			if(pay.length==2 && isValidBankCode(pay[1])){
				bankCode = pay[1];
			}
		}
		String chkValue = getCheckValue("", merchantNo, version, order.getDue()+".00", order.getTradeNo(), merchantNo, 
				payChannel, postBackUrl, notifyUrl, orderTime, currencyType, notifyUrlType, signType, bankCode);
		//Origin＝Version + Amount + OrderNo + MerchantNo + PostBackUrl + NotifyUrl + OrderTime + CurrencyType + NotifyUrlType + SignType；
		//Origin＝Version + Amount + OrderNo + MerchantNo + PayChannel + PostBackUrl + NotifyUrl + OrderTime + CurrencyType + NotifyUrlType + SignType + BankCode + DefaultChannel;
		Map params = new HashMap();
		params.put("Version", version);
		params.put("Amount", order.getDue()+".00");
		params.put("OrderNo", order.getTradeNo());
		params.put("MerchantNo", merchantNo);
		params.put("PostBackUrl", postBackUrl);
		params.put("NotifyUrl", notifyUrl);
		params.put("OrderTime", orderTime);
		params.put("CurrencyType", currencyType);
		params.put("NotifyUrlType", notifyUrlType);
		params.put("SignType", signType);
		
		if(StringUtils.isNotBlank(payChannel)) {
			params.put("PayChannel", payChannel);
		}
		if(StringUtils.isNotBlank(bankCode)){
			params.put("BankCode", bankCode);
			params.put("payurl", bankGateway);
		}else{
			params.put("payurl", paygateway);
		}
		params.put("MAC", chkValue);
		return params;
	}
	private static boolean isValidBankCode(String bankCode) {
		return StringUtils.isNotBlank(bankCode) && BANK_MAP.containsKey(bankCode);
	}
	private static boolean isValidPayChannel(String payChannel) {
		String[] channels = StringUtils.split(payChannel);
		for(String channel:channels) if(!VALID_CHANNEL.contains(channel)) return false;
		return true;
	}
	static{
		Map<String, String> tmp = new HashMap<String, String>();
		tmp.put("ICBC","中国工商银行");
		tmp.put("HXB","华夏银行");
		tmp.put("CCB","中国建设银行");
		tmp.put("GNXS","广州市农村信用合作社");
		tmp.put("ABC","中国农业银行");
		tmp.put("GZCB","广州市商业银行");
		tmp.put("CMB","招商银行");
		tmp.put("SHRCB","上海农村商业银行");
		tmp.put("COMM","交通银行");
		tmp.put("CBHB","渤海银行");
		tmp.put("CMBC","中国民生银行");
		tmp.put("HKBEA","东亚银行");
		tmp.put("CIB","兴业银行");
		tmp.put("HKBCHINA","汉口银行");
		tmp.put("SPDB","浦东发展银行");
		tmp.put("SZPAB","平安银行");
		tmp.put("ZHNX","珠海市农村信用合作联社");
		tmp.put("BOC","中国银行");
		tmp.put("WZCB","温州银行");
		tmp.put("GDB","广东发展银行");
		tmp.put("SDE","顺德农信社");
		tmp.put("CEB","光大银行");
		tmp.put("NBCB","宁波银行");
		tmp.put("BOS","上海银行");
		tmp.put("NJCB","南京银行");
		tmp.put("CITIC","中信银行");
		tmp.put("BCCB","北京银行");
		tmp.put("SDB","深圳发展银行");
		tmp.put("BJRCB","北京农村商业银行");
		BANK_MAP = UnmodifiableMap.decorate(tmp);
		tmp = new HashMap<String, String>();
		tmp.put(merchantNo1, privatekey1);
		tmp.put(merchantNo2, privatekey2);
		tmp.put(PaymethodConstant.PAYMETHOD_SPSDOPAY1, merchantNo1);
		tmp.put(PaymethodConstant.PAYMETHOD_SPSDOPAY2, merchantNo2);
		tmp.put(merchantNo2, privatekey2);
		KEY_MAP = UnmodifiableMap.decorate(tmp);
		tmp = new HashMap<String, String>();
		tmp.put(merchantNo1, PaymethodConstant.PAYMETHOD_SPSDOPAY1);
		tmp.put(merchantNo2, PaymethodConstant.PAYMETHOD_SPSDOPAY2);
		PAY_MAP = UnmodifiableMap.decorate(tmp);
	}
	public static OrderQuery qryOrder(GewaOrder order){
		String merchantNo = KEY_MAP.get(order.getPaymethod());
		String privatekey = KEY_MAP.get(merchantNo);
		String signStr = order.getTradeNo() + "|" + merchantNo + "|" + signType;
		String mac = StringUtil.md5(signStr + privatekey, "gb2312");
		OrderQuery query = new OrderQuery();
		query.setOrderNo(order.getTradeNo());
		query.setSignType(Integer.valueOf(signType));
		query.setMerchantNo(merchantNo);
		query.setMac(mac);
		return query;
	}
}
