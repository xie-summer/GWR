package com.gewara.pay;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.collections.map.UnmodifiableMap;
import org.apache.commons.lang.StringUtils;

import chinapay.PrivateKey;
import chinapay.SecureLink;

import com.gewara.Config;
import com.gewara.bank.ChinaOrderQry;
import com.gewara.constant.PaymethodConstant;
import com.gewara.constant.sys.LogTypeConstant;
import com.gewara.model.api.ApiUser;
import com.gewara.model.pay.GewaOrder;
import com.gewara.model.pay.TicketOrder;
import com.gewara.util.DateUtil;
import com.gewara.util.ECBEncrypt;
import com.gewara.util.GewaLogger;
import com.gewara.util.HttpResult;
import com.gewara.util.HttpUtils;
import com.gewara.util.JsonUtils;
import com.gewara.util.LoggerUtils;
import com.gewara.util.StringUtil;
import com.gewara.util.VmUtils;

public class ChinapayUtil {
	private static final transient GewaLogger dbLogger = LoggerUtils.getLogger(ChinapayUtil.class, Config.getServerIp(), Config.SYSTEMID);
	public static final String qryOrderUrl = "http://console.chinapay.com/QueryWeb/processQuery.jsp";
	private static final transient String version = "20070129";
	private static final transient String transType ="0001";
	private static final transient String currencyCode = "156";//人民币
	private static final transient String merPriv = "ticketOrder";
	private static transient PrivateKey privateKey1 = new PrivateKey();
	private static transient PrivateKey pubKey1 = new PrivateKey();

	private static transient PrivateKey privateKey2 = new PrivateKey();
	private static transient PrivateKey pubKey2 = new PrivateKey();
	
	private static transient PrivateKey privateKeySrcb = new PrivateKey();
	private static transient PrivateKey pubKeySrcb = new PrivateKey();

	//网银
	private static String paygateway;
	private static String dbgateway;		//担保交易网关
	private static String dbConfirmUrl;	//担保交易确认
	private static String bgRetUrl;
	private static String pageRetUrl;

	private static String dbNotifyUrl;
	private static String dbReturnUrl;
	//联名登录
	//public static String CLIENT_ID = "105550149170027";//测试
	//private static final String UNIONPAY_OAUTH_PATH = "http://58.246.226.99/oauthProxy";//银联联名登录测试地址修改了20131108
	//private static final String CLIENT_SECRET = "3cf86bd3a97947a6987905354db13d99";//测试
	public static final String CLIENT_ID = "105290078320121";
	private static final String CLIENT_SECRET = "6d8eb670fbd2414f801b6a02cad5abb4";
	private static final String UNIONPAY_OAUTH_PATH = "https://online.unionpay.com/oauth";
	public static final String UNIONPAY_AUTHORIZE_CODE_URL = UNIONPAY_OAUTH_PATH+"/authorize";
	public static final String UNIONPAY_ACCESS_TOKEN_URL = UNIONPAY_OAUTH_PATH+"/token";
	public static final String UNIONPAY_USER_INFO_URL = UNIONPAY_OAUTH_PATH+"/user";
	
	private static Map<String, String> MER_MAP;
	private static Map<String, PrivateKey> PRIKEY_MAP;
	private static Map<String, PrivateKey> PUBKEY_MAP;
	//便民平台
	private static String merSysId;
	private static String shortMerSysId; //后5位
	private static String macprivatekey;
	private static String callbackUrl;
	private static String successUrl;
	//public static String BANK_CODE_SRCB =  "5724";
	public static String BANK_CODE_SRCB =  "0029";
	public static String getTransFilename(Date date){
		return merSysId + "_" + DateUtil.format(date, "yyyyMMdd") + "_01.txt";
	}
	
	private static Map<String, String> bankMap;
	private static boolean initialized = false;
	public static synchronized void init(String propertyFile){
		if(initialized) return;
		Map<String, String> banktmp = new HashMap<String, String>();
		banktmp.put("0001", "上海农商行");
		banktmp.put("0026", "广发银行");
		banktmp.put("8607", "快捷支付");
		banktmp.put("1010", "建设银行");
		banktmp.put("0005", "工商银行");
		banktmp.put("2624", "平安银行");
		banktmp.put("5724", "上海农商行2");
		 
		bankMap = UnmodifiableMap.decorate(banktmp);

		Properties props = new Properties();
		try {
			props.load(ChinapayUtil.class.getClassLoader().getResourceAsStream(propertyFile));
		} catch (Exception e) {
			throw new IllegalArgumentException("property File Error!!!!", e);
		}
		paygateway = props.getProperty("paygateway");
		dbgateway = props.getProperty("dbgateway");
		bgRetUrl = props.getProperty("bgRetUrl");
		pageRetUrl = props.getProperty("pageRetUrl");
		dbNotifyUrl = props.getProperty("dbNotifyUrl");
		dbReturnUrl = props.getProperty("dbReturnUrl");
		dbConfirmUrl = "http://payment.chinapay.com/upop_auth/Confirm"; 
		String merId1 = props.getProperty("merId1");
		String merKeyFile1 = ChinapayUtil.class.getClassLoader().getResource(props.getProperty("merKeyFile1")).getFile();
		String pubKeyFile1 = ChinapayUtil.class.getClassLoader().getResource(props.getProperty("pubKeyFile1")).getFile();

		String merId2 = props.getProperty("merId2");
		String merKeyFile2 = ChinapayUtil.class.getClassLoader().getResource(props.getProperty("merKeyFile2")).getFile();
		String pubKeyFile2 = ChinapayUtil.class.getClassLoader().getResource(props.getProperty("pubKeyFile2")).getFile();
		
		String merIdSrcb = props.getProperty("merIdSrcb");
		String merKeyFileSrcb = ChinapayUtil.class.getClassLoader().getResource(props.getProperty("merKeyFileSrcb")).getFile();
		String pubKeyFileSrcb = ChinapayUtil.class.getClassLoader().getResource(props.getProperty("pubKeyFileSrcb")).getFile();
		
		
		boolean sign = pubKey1.buildKey("999999999999999", 0, pubKeyFile1);
		if(!sign) {
			dbLogger.warnWithType(LogTypeConstant.LOG_TYPE_ORDER_PAY, "chinapayError: buildKey public");
			pubKey1 = null;
		}
		sign = privateKey1.buildKey(merId1, 0, merKeyFile1);
		if(!sign) {
			dbLogger.warnWithType(LogTypeConstant.LOG_TYPE_ORDER_PAY, "chinapayError: buildKey private");
			privateKey1 = null;
		}
		sign = pubKey2.buildKey("999999999999999", 0, pubKeyFile2);
		if(!sign) {
			dbLogger.warnWithType(LogTypeConstant.LOG_TYPE_ORDER_PAY, "chinapayError: buildKey public");
			pubKey2 = null;
		}
		sign = privateKey2.buildKey(merId2, 0, merKeyFile2);
		if(!sign) {
			dbLogger.warnWithType(LogTypeConstant.LOG_TYPE_ORDER_PAY, "chinapayError: buildKey private");
			privateKey2 = null;
		}
		sign = pubKeySrcb.buildKey("999999999999999", 0, pubKeyFileSrcb);
		if(!sign) {
			dbLogger.warnWithType(LogTypeConstant.LOG_TYPE_ORDER_PAY, "chinapayError: buildKey public");
			pubKeySrcb = null;
		}
		sign = privateKeySrcb.buildKey(merIdSrcb, 0, merKeyFileSrcb);
		if(!sign) {
			dbLogger.warnWithType(LogTypeConstant.LOG_TYPE_ORDER_PAY, "chinapayError: buildKey private");
			privateKeySrcb = null;
		}
		Map<String, PrivateKey> tmp = new HashMap<String, PrivateKey>();
		tmp.put(merId1, privateKey1);
		tmp.put(merId2, privateKey2);
		tmp.put(merIdSrcb, privateKeySrcb);
		PRIKEY_MAP = UnmodifiableMap.decorate(tmp);
		
		tmp = new HashMap<String, PrivateKey>();
		tmp.put(merId1, pubKey1);
		tmp.put(merId2, pubKey2);
		tmp.put(merIdSrcb, pubKeySrcb);
		PUBKEY_MAP = UnmodifiableMap.decorate(tmp);
		
		Map<String, String> tmp2 = new HashMap<String, String>();
		tmp2.put(PaymethodConstant.PAYMETHOD_CHINAPAY1, merId1);
		tmp2.put(PaymethodConstant.PAYMETHOD_CHINAPAY2, merId2);
		tmp2.put(PaymethodConstant.PAYMETHOD_CHINAPAYSRCB, merIdSrcb);
		tmp2.put(merId1, PaymethodConstant.PAYMETHOD_CHINAPAY1);
		tmp2.put(merId2, PaymethodConstant.PAYMETHOD_CHINAPAY2);
		tmp2.put(merIdSrcb, PaymethodConstant.PAYMETHOD_CHINAPAYSRCB);
		MER_MAP = UnmodifiableMap.decorate(tmp2);
		
		merSysId = props.getProperty("merSysId");
		shortMerSysId = props.getProperty("shortMerSysId");
		macprivatekey = props.getProperty("macprivatekey");
		callbackUrl = props.getProperty("callbackUrl");
		successUrl = props.getProperty("successUrl");
		initialized = true;
	}
	/**
	 * @param ordId
	 * @param merDate yyyyMMdd
	 * @param transAmt 
	 * @return
	 */
	private static String getCheckValue(String merId, String ordId, String merDate, String transAmt){
		SecureLink sl = new SecureLink(PRIKEY_MAP.get(merId));
		String chkValue = sl.signOrder(merId, ordId, transAmt, currencyCode, merDate, transType);
		return chkValue;
	}
	public static boolean verifyTransResponse(String MerId, String OrdId, String TransAmt, 
			String TransDate, String TransType, String OrderStatus, String CheckValue){
		SecureLink sl = new SecureLink(PUBKEY_MAP.get(MerId));
		return sl.verifyTransResponse(MerId, OrdId, TransAmt, currencyCode, TransDate, TransType, OrderStatus, CheckValue);
	}
	public static boolean verifyAuthToken(String MerId, String ChkValue, String plainData){
		SecureLink sl = new SecureLink(PUBKEY_MAP.get(MerId));
		return sl.verifyAuthToken(plainData, ChkValue);
	}
	public static Map<String, String> getNetPayParams(GewaOrder order){
		String merDate = DateUtil.format(order.getAddtime(),"yyyyMMdd");
		String transAmt = StringUtils.leftPad(order.getDue() + "00", 12, "0");
		String ordId = order.getTradeNo();
		String merId = MER_MAP.get(order.getPaymethod());
		String chkValue = getCheckValue(merId, ordId, merDate, transAmt);
		
		Map<String, String> params = new HashMap<String, String>();
		params.put("payurl", paygateway);
		params.put("MerId", merId);
		params.put("OrdId", ordId);
		params.put("TransAmt", transAmt);
		params.put("CuryId", currencyCode);
		params.put("TransDate", merDate);
		params.put("TransType", transType);
		params.put("Version", version);
		params.put("BgRetUrl", bgRetUrl);
		params.put("PageRetUrl", pageRetUrl);
		params.put("MerPriv", merPriv);
		params.put("ChkValue", chkValue);
		if(isValidBank(order.getPaybank())) params.put("GateId", order.getPaybank());
		return params;
	}
	public static String getDanbaoSign(String merId, String... params){
		SecureLink sl = new SecureLink(PRIKEY_MAP.get(merId));
		String chkValue = sl.Sign(StringUtils.join(params));
		return chkValue;
	}
	public static Map<String, String> getDanbaoParams(GewaOrder order, String clientIp){
		String merDate = DateUtil.format(order.getAddtime(),"yyyyMMdd");
		String transAmt = StringUtils.leftPad(order.getDue() + "00", 12, "0");
		String ordId = order.getTradeNo();
		String merId = MER_MAP.get(order.getPaymethod());
		String chkValue = getDanbaoSign(merId, "1000", merDate, merId, ordId, transAmt, currencyCode, clientIp, ""+order.getId());
		Map<String, String> params = new HashMap<String, String>();
		params.put("Version", "20110531");	//担保支付特定
		params.put("TransCode", "1000");		//担保支付特定
		params.put("TransDate", merDate);
		params.put("MerId", merId);
		params.put("OrdId", ordId);
		params.put("TransAmt", transAmt);
		params.put("CuryId", currencyCode);
		params.put("BgRetUrl", dbNotifyUrl);
		params.put("PageRetUrl", dbReturnUrl);
		params.put("ClientIp", clientIp);
		params.put("GateId", "8605");
		params.put("OrdNote", "test");
		params.put("Priv1", ""+order.getId());
		params.put("ChkValue", chkValue);
		params.put("payurl", dbgateway);
		params.put("submitMethod", "post");
		//担保交易
		if(StringUtils.equals("8607", order.getPaybank())) {
			params.put("GateId", order.getPaybank());
		}
		return params;
	}
	public static boolean confirmDanbao(GewaOrder order){
		String transDate = DateUtil.format(order.getAddtime(),"yyyyMMdd");
		String transAmt = StringUtils.leftPad(order.getDue() + "00", 12, "0");
		String ordId = order.getTradeNo();
		String merId = MER_MAP.get(order.getPaymethod());
		String chkValue = getDanbaoSign(merId, "1000", transDate, merId, ordId, transAmt, currencyCode, ""+order.getId());
		Map<String, String> params = new HashMap<String, String>();
		params.put("Version", "20110531");	//担保支付特定
		params.put("TransCode", "1000");		//担保支付特定
		params.put("TransDate", transDate);
		params.put("OrigiTransDate", transDate);
		
		params.put("MerId", merId);
		params.put("OrdId", ordId);
		params.put("TransAmt", transAmt);
		params.put("CuryId", currencyCode);
		params.put("BgRetUrl", dbNotifyUrl);

		params.put("GateId", "8605");
		params.put("Priv1", ""+order.getId());
		params.put("ChkValue", chkValue);
		HttpResult result = HttpUtils.postUrlAsString(dbConfirmUrl, params);
		return result.isSuccess();
	}

	private static boolean isValidBank(String bank){
		return bankMap.containsKey(bank);
	}
	/**
	 * 便民调用接口
	 * @param order
	 * @return
	 */
	public static boolean callbackBianmin(TicketOrder order){
		Map<String, String> params = new LinkedHashMap<String, String>();
		String transAmt = StringUtils.leftPad(order.getDue()+"00", 12, "0");
		String transDate = DateUtil.format(order.getAddtime(),"yyyyMMdd");
		String transTime = DateUtil.format(order.getAddtime(),"HHmmss");
		String ordId = shortMerSysId + StringUtils.substring(order.getTradeNo(), 5);//StringUtils.leftPad(""+order.getId(),11,'0');
		String transState = order.isAllPaid()?"00":"02";//成功
		String billState = order.isPaidSuccess()?"00":(order.isAllPaid()?"02":"03");//成功
		params.put("userId", order.getMembername().split("@")[0]);
		params.put("merSysId", merSysId);
		params.put("transAmt", transAmt);
		params.put("transDate", transDate);
		params.put("transTime", transTime);
		params.put("ordId", ordId);
		params.put("transState", transState);//
		params.put("billNum", "0");
		params.put("billOrgId_0", ""+order.getCinemaid());
		params.put("billOrgName_0", order.getOrdertitle().replaceAll("电影票", ""));
		params.put("billType_0", "01");
		params.put("billNo_0", order.getTradeNo());
		params.put("billDate_0", transDate);
		params.put("billAmt_0", transAmt);
		params.put("billState_0", billState);
		Map<String, String> descMap = JsonUtils.readJsonToMap(order.getDescription2());
		params.put("billDesc_0",descMap.get("影片") + order.getQuantity() + "张");
		params.put("billResv_0", "");
		params.put("resv", "");

		String macStr = "";
		for(String s: params.values()){
			macStr += s;
		}
		String mac = ECBEncrypt.ecbEncrypt(macStr, macprivatekey);
		params.put("mac", mac);
		HttpResult result = HttpUtils.postUrlAsString(callbackUrl, params);
		if(StringUtils.contains(result.getResponse(), "success")) return true;
		dbLogger.warnWithType(LogTypeConstant.LOG_TYPE_ORDER_PAY, callbackUrl + ":" + params);
		dbLogger.warnWithType(LogTypeConstant.LOG_TYPE_ORDER_PAY, "ChinaPayUtil.callbackBianmin:result=" + result.getResponse());
		return false;
	}
	public static String getTransFileRow(TicketOrder order){
		String transAmt = StringUtils.leftPad(order.getDue()+"00", 12, "0");
		String transDate = DateUtil.format(order.getAddtime(),"yyyyMMdd");
		String transTime = DateUtil.format(order.getAddtime(),"HHmmss");
		String ordId = shortMerSysId + StringUtils.substring(order.getTradeNo(), 5);	//;StringUtils.leftPad(""+order.getId(),11,'0');
		String transState = order.isPaidFailure()||order.isPaidSuccess()?"00":"02";//成功
		String billState = order.isPaidFailure()?"02":(order.isPaidSuccess()?"00":"03");//成功
		List<String> params = new ArrayList<String>();
		params.add(merSysId);
		params.add(transDate);
		params.add(transTime);
		params.add(ordId);
		params.add(transAmt);
		params.add(transState);//
		params.add(""+order.getCinemaid());
		params.add("01");
		params.add(order.getTradeNo());
		params.add(transDate);
		params.add(transAmt);
		params.add(billState);
		params.add(order.getMembername().split("@")[0]);
		String macStr = StringUtils.join(params, "");
		String mac = ECBEncrypt.ecbEncrypt(macStr, macprivatekey);
		String result = StringUtils.join(params, "|");
		Map<String, String> descMap = JsonUtils.readJsonToMap(order.getDescription2());
		result += "|" + descMap.get("影片") + order.getQuantity() + "张";
		result += "|" + mac + "\n";
		return result;
	}
	public static String getEncryptStr(String str){
		return ECBEncrypt.ecbEncrypt(str, macprivatekey);
	}
	public static String getMerSysId() {
		return merSysId;
	}
	public static String getSuccessUrl() {
		return successUrl;
	}
	public static String getPaymethod(String merId) {
		return MER_MAP.get(merId);
	}
	/**
	 * 1 808080580202753  0001 启用 上海农商行
		2 808080580202753  0004 启用 中信网银 
		3 808080580202753  0005 启用 工商银行 
		4 808080580202753  0008 启用 农业银行 
		5 808080580202753  0009 启用 兴业银行 
		6 808080580202753  0015 启用 民生银行(非签约) 
		7 808080580202753  0016 启用 广州商行 
		8 808080580202753  0021 启用 华夏银行 
		9 808080580202753  0024 启用 邮储银行 
		10 808080580202753  0025 启用 交通银行 
		11 808080580202753  0026 启用 广发银行 
		12 808080580202753  0027 启用 光大银行 
		13 808080580202753  0051 启用 东亚银行 
		14 808080580202753  0124 启用 珠海市农村信用合作社 
		15 808080580202753  0624 启用 渤海银行 
		16 808080580202753  0724 启用 温州商行 
		17 808080580202753  0924 启用 晋城商行 
		18 808080580202753  1010 启用 建设银行 
		19 808080580202753  1022 启用 浦发银行2 
		20 808080580202753  1023 启用 中国银行 
		21 808080580202753  1124 启用 尧都商行 
		22 808080580202753  2324 启用 宁波银行 
		23 808080580202753  2424 启用 江苏省农村信用社联合社 
		24 808080580202753  2524 启用 富滇银行 
		25 808080580202753  2624 启用 平安银行 
		26 808080580202753  2724 启用 重庆农商行 
		27 808080580202753  2824 启用 晋中商行 
		28 808080580202753  2924 启用 湖南农信 
		29 808080580202753  3024 启用 周口市商行 
		30 808080580202753  3124 启用 海南省农村信用社联合社 
		31 808080580202753  4007 启用 cmb 
		32 808080580202753  4008 启用 农业银行 
		33 808080580202753  4205 启用 中国工商银行 
	 * */
	private static String getQryCheckValue(String merId, String transDate, String orderid){
		SecureLink sl = new SecureLink(PRIKEY_MAP.get(merId));
		String chkValue = sl.Sign(merId+transDate+orderid+transType);
		return chkValue;
	}
	public static Map<String, String> qryOrder(GewaOrder order){
		String merId = MER_MAP.get(order.getPaymethod());
		String ordId = order.getTradeNo();
		String merDate = DateUtil.format(order.getAddtime(), "yyyyMMdd");
		Map<String, String> params = new HashMap<String, String>();
		params.put("MerId", merId);
		params.put("TransType", transType);
		params.put("OrdId", ordId);
		params.put("TransDate", merDate);
		params.put("Version", version);
		params.put("Resv", "");
		params.put("ChkValue", getQryCheckValue(merId, merDate, ordId));
		return params;
	}
	public static String getQryRes(Map<String, String> params){
		HttpResult code = HttpUtils.postUrlAsString(qryOrderUrl, params);
		String response = code.getResponse();
		response = StringUtil.getHtmlText(response);
		return response;
	}
	public static ChinaOrderQry getQryToObject(String response){
		ChinaOrderQry orderQry = null;
		if(StringUtils.isNotBlank(response)){
			String[] strList = response.split("&amp;");
			orderQry = new ChinaOrderQry();
			for(String str : strList){
				String[] s = str.split("=");
				if(StringUtils.equals(s[0], "ResponeseCode")) {
					orderQry.setResponseCode(s[1]);
				}else if(StringUtils.equals(s[0], "merid")){
					orderQry.setMerid(s[1]);
				}else if(StringUtils.equals(s[0], "orderno")){
					orderQry.setOrderno(s[1]);
				}else if(StringUtils.equals(s[0], "amount")){
					orderQry.setAmount(s[1]);
				}else if(StringUtils.equals(s[0], "transdate")){
					orderQry.setTransdate(s[1]);
				}else if(StringUtils.equals(s[0], "transtype")){
					orderQry.setTranstype(s[1]);
				}else if(StringUtils.equals(s[0], "status")){
					orderQry.setStatus(s[1]);
				}
			}
		}
		return orderQry;
	}

	public static String isAllowDiscount(ApiUser partner,String usercode){
		Map<String, String> otherMap = VmUtils.readJsonToMap(partner.getOtherinfo());
		String url = otherMap.get("approveUserUrl");
		String privateKey = otherMap.get("privateKey");
		if(StringUtils.isBlank(url) || StringUtils.isBlank(privateKey))
			return "url or key is null";
		if(StringUtils.isBlank(usercode)) return "userid is required";
		Map<String,String> params = new HashMap<String, String>();
		params.put("appSysId", "90001");
		params.put("serviceType", "1040");
		params.put("usrSysId", usercode);
		String values = "appSysId=90001&serviceType=1040&usrSysId="+usercode;
		String signature = StringUtil.md5(values+privateKey);
		params.put("signature", signature);
		HttpResult result = HttpUtils.postUrlAsString(url, params);
		if(result.isSuccess()){
			if(StringUtils.contains(result.getResponse(), "respCode=0000&")){
				return "success";
			}
		}
		dbLogger.warn("chianpay:"+result.getResponse() + result.getMsg());
		return "not allow";
	}

	public static String getAccessToken(String code,String returnUrl){
		Map<String, String> params = new HashMap<String, String>();
		params.put("grant_type", "authorization_code");
		params.put("code", code);
		params.put("client_id", CLIENT_ID);
		params.put("client_secret", CLIENT_SECRET);
		params.put("redirect_uri", returnUrl);
		HttpResult result = HttpUtils.postUrlAsString(UNIONPAY_ACCESS_TOKEN_URL, params);
		if(result.isSuccess()){
			return JsonUtils.readJsonToMap(result.getResponse()).get("access_token")+"";
		}
		dbLogger.warn("china pay getAccessToken error :"+result.getMsg());
		return null;
	}

	public static Map getUserInfo(String accessToken){
		Map<String, String> params = new HashMap<String, String>();
		params.put("access_token", accessToken);
		HttpResult result = HttpUtils.postUrlAsString(UNIONPAY_USER_INFO_URL, params);
		if(result.isSuccess()){
			try {
				return JsonUtils.readJsonToMap(URLDecoder.decode(result.getResponse(), "UTF-8"));
			} catch (UnsupportedEncodingException e) {
				dbLogger.warn("china pay getUserInfo URLDecoder error .");
			}
		}
		dbLogger.warn("china pay getUserInfo error :"+result.getMsg());
		return null;
	}
	
}
