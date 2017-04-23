package com.gewara.pay;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.betwixt.io.BeanReader;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;

import com.gewara.Config;
import com.gewara.util.ApiUtils;
import com.gewara.util.GewaLogger;
import com.gewara.util.HttpResult;
import com.gewara.util.HttpUtils;
import com.gewara.util.JsonUtils;
import com.gewara.util.LoggerUtils;
import com.gewara.xmlbind.partner.ChinapayWalletLogin;

public class UnionpayWalletUtil {
	
	public static String UNIONPAY_WALLET_AUTHORIZE_CODE_URL = "https://sso.95516.net/";//"https://member.9588.com:8001/sso/login";
	public static String UNIONPAY_WALLET_SERVICE = "8c8f61b3512ae844a130e75335187ae1";
	public static String UNIONPAY_WALLET_VENDERID = "T00000000000045";//"915441972980004";
	public static String UNIONPAY_WALLET_WEBSERVICE_PASSWORD = "11111111111111111111111111111111";//"111111                          ";
	public static String UNIONPAY_WALLET_WEBSERVICE_SIGN_PRIVATEKEY = "MIICeAIBADANBgkqhkiG9w0BAQEFAASCAmIwggJeAgEAAoGBAMcqMF47VCw/dkSZR7YUOddrNmBqPKn4nFNjlOpwd0/X+XaM2u9CZyopQLuD0ujDdV7SCLI16rYNSexmUaNSRxruV8ezQPQ/z2+euCJypaqBf1vuAAOXnGlcoZJlMMUgsaF/cq3Agu4k+am5fg1PW3L0mt7cuMEb/qoJdqKFm6E/AgMBAAECgYEAigxSLl7NP0ZVwcLFFA388bYcoMPCfMYYBSz3IXEHIk6/WU7Uyhaqz2x8s/zxielBR5Ip+OyqXbnkTXm2iJAVO/nML2INA4SnwPtkxMvyl00Au7wJJHFwDW0PJTp2mPHb+BQ4AsrAC073OBoCtyE5JK35O2e7F8riq13X0KkfT8ECQQDxt0e7YJsQNXFDQ50qHxZR4oFllqvq/nU8aPbKKa/Gj2mA5gTW0jJRHc4VrkCAY+ihY2k+CqfFyDm6/65NMNcRAkEA0u8wGZ6m1ZKAF5fpJvyGDel4ZCikz246HO0/btLPvUTYBSGZ9o6mTi5MxaSUjgtD6IlQQxam9T610tkcaGMTTwJBAMAoODpZ/mvQUI5svhCyTggizUN5mIpkZ8bymt70bOypLfJu6gHtcFrUGvVZZaWrx1Alu5C63813840ZnfN3lEECQEbXIbZY+/raOr6xvBHl2Teu40nwBHnNsSKp4xXbIv2Ts2D1yfluKxgyoIH4JhQWYHYmN5hwcR4IM5ReahX8wC0CQQCZtgGJbcIZHaOGKPib97oHhqlbtQxFpVP9GB6DnQtplvJnCTlJBhC8CFjzXL+Gkp0+YW4IKTwmM9sS8MOVDdT4";
			//"MIICdgIBADANBgkqhkiG9w0BAQEFAASCAmAwggJcAgEAAoGBAO9F1ub3EmcROVb9TuwbvGEwZaDeldcDtQNYvHpC5Xm+0v32pGCCJ05qK3zEpumz8bpHcBqVw94cZVia4iVaEWkFnWm710b7NdNeMBFhC/L9NYl0jDsoRhCL/57OxbttvMPi0YbpQc0Qfn80QnYZYcwic640UVUz5DzJ/sVOrleFAgMBAAECgYBUSTfQmIxE/k5ClGyuw35yhgfmyUHjQg0LpsCOGO6ZGl1c1PtGe9K4zrGO+/8IKDkos22MD+G1Zi9VLQooujeTJtpsfHsp9DhGgglfhOwH8kkCtgVaH6sovgzIj5plln6la/GDAcRe5kGn1xoTDusnVqw9OqC27gybn/hM3lFOaQJBAP14rXdpap1EKbigSSEGP0PwiA0c2yu1EknxQ8fRJWS7DyIRn9K1vsxCbxfLY2PYlPWFz8fCPMRJhqO3BP4OxacCQQDxqOavBS0UT4iqJ1+Wzz7dV6sTd/p3gbVBy5It7wGWnDiBa9Z2beLm1k84oc7mb56Mf6VDCuAeILEs3jJ1PLbzAkBvVzc7oP7IHk0FYMM+0nOv8FSTDf3ocR2bhXN0rpZybQj0ujEuac9qAjSyixEZpuWoBCOFZ/kxb+rIt3hl8S85AkEA6TZEmTb3kBhJHVwuBY4vbtBCCuHIVzhXwg1BHw7+i2hrp4p4R4Y4aOj9Pvv4fa3OZmxxAkgmjSyjj1dHfph/PQJAbmRVNakH+18qzzh7budS3A1kPTDx4xeT+Rtt6bhz0nfmuBuUGRa2Mt4CVNVspkAXMU7j+0mFsp5Ykcw0iwSbUA==";
	public static String UNIONPAY_WALLET_QUERY_USER_VENDERID = "";
	public static String UNIONPAY_WALLET_QUERY_USER_PASSWORD = "";
	private static final transient GewaLogger dbLogger = LoggerUtils.getLogger(UnionpayWalletUtil.class, Config.getServerIp(), Config.SYSTEMID);
	
	private static boolean initialized = false;
	public static synchronized void init(String propertyFile){
		if(initialized){
			return;
		}
		Properties props = new Properties();
		try {
			props.load(UnionpayWalletUtil.class.getClassLoader().getResourceAsStream(propertyFile));
		} catch (Exception e) {
			throw new IllegalArgumentException("property File Error!!!!", e);
		}
		UNIONPAY_WALLET_AUTHORIZE_CODE_URL = props.getProperty("unionpayWalletAuthorizeCodeUrl");
		UNIONPAY_WALLET_SERVICE = props.getProperty("unionpayWalletService");
		UNIONPAY_WALLET_VENDERID = props.getProperty("unionpayWalletVenderid");
		UNIONPAY_WALLET_WEBSERVICE_PASSWORD = props.getProperty("unionpayWalletWebservicePassword");
		UNIONPAY_WALLET_WEBSERVICE_SIGN_PRIVATEKEY = props.getProperty("unionpayWalletWebserviceSignPriveteKey");
		UNIONPAY_WALLET_QUERY_USER_VENDERID = props.getProperty("unionpayWalletQueryUserVenderid");
		UNIONPAY_WALLET_QUERY_USER_PASSWORD = props.getProperty("unionpayWalletQueryUserPassword");
	}
	
	public static String getUnionpayWalletUser(String url,String ticket,String returnUrl){
		Map<String,String> params = new HashMap<String,String>();
		params.put("service",returnUrl);
		params.put("ticket", ticket);
		HttpResult result = HttpUtils.postUrlAsString(url , params);
		if(result.isSuccess()){
			String response = result.getResponse();
			dbLogger.warn("china pay getChinapayWallet success :"+response);
			BeanReader beanReader = ApiUtils.getBeanReader("cas:serviceResponse/cas:authenticationSuccess", ChinapayWalletLogin.class);
			ChinapayWalletLogin login = (ChinapayWalletLogin)ApiUtils.xml2Object(beanReader, response);
			if(login != null && login.getUser() != null){
				String[] user = StringUtils.split(login.getUser(), ":");
				if(user.length > 0){
					return user[0];
				}
			}
			return null;
		}
		dbLogger.warn("china pay getChinapayWallet error :"+result.getMsg());
		return null;
	}
	
	public static HttpResult getUserbyMobile(String mobile){
		Map<String,String> params = new HashMap<String,String>();
		params.put("venderId", UNIONPAY_WALLET_QUERY_USER_VENDERID);
		params.put("onlTransPwd", UNIONPAY_WALLET_QUERY_USER_PASSWORD);
		params.put("data", "{\"mobile\":\"" + mobile + "\"}");
		HttpResult result = HttpUtils.postUrlAsString("https://member.95516.net/umcardholderweb/rest/user/getuserbymobile", params);
		if(result.isSuccess()){
			String response = result.getResponse();
			dbLogger.warn("unionpay wallet getuserByMobile response:"+response);
			if(StringUtils.isBlank(response)){
				return HttpResult.getFailure("fail");
			}
			try {
				String r = new String(Base64.decodeBase64(response),"UTF-8");
				Map rMap = JsonUtils.readJsonToMap(r);
				if(StringUtils.equals(rMap.get("respCd").toString(),"0000")){
					return HttpResult.getSuccessReturn(((Map)rMap.get("data")).get("cdhdUsrId").toString());
				}
				return HttpResult.getFailure(rMap.get("respCd").toString());
			} catch (UnsupportedEncodingException e) {
				return HttpResult.getFailure("fail");
			}
		}
		dbLogger.warn("unionpay wallet getuserByMobile error :"+result.getMsg());
		return HttpResult.getFailure(result.getMsg());
	}
	
	public static String byteArr2HexStr(byte[] arrB) throws Exception {   
		int iLen = arrB.length;   
		// 每个byte用两个字符才能表示，所以字符串的长度是数组长度的两倍   
		StringBuffer sb = new StringBuffer(iLen * 2);   
		for (int i = 0; i < iLen; i++) {    
			int intTmp = arrB[i];    
			// 把负数转换为正数    
			while (intTmp < 0) {    
				intTmp = intTmp + 256;   
			}    
			// 小于0F的数需要在前面补0    
			if (intTmp < 16) {     
				sb.append("0");    
			}    
			sb.append(Integer.toString(intTmp, 16));   
		}   
		return sb.toString();  
	}  
}
