package com.gewara.util;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.lang.StringUtils;


public class QQOauthUtil {
	public static final int ONE = 1;
   public static final int TWO = 2;
   public static final int THREE = 3;
   public static final int FOUR = 4;
   public static final int SIX = 6;
   public static final int EIGHT = 8;
   public static final double TEMP = 1.34;
   private static final char LAST2BYTE = (char) Integer.parseInt("00000011", TWO);
   private static final char LAST4BYTE = (char) Integer.parseInt("00001111", TWO);
   private static final char LAST6BYTE = (char) Integer.parseInt("00111111", TWO);
   private static final char LEAD6BYTE = (char) Integer.parseInt("11111100", TWO);
   private static final char LEAD4BYTE = (char) Integer.parseInt("11110000", TWO);
   private static final char LEAD2BYTE = (char) Integer.parseInt("11000000", TWO);
   private static final char[] ENCODE_TABLE = new char[] {'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P',
           'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o',
           'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '+', '/' };

   public static final String APP_ID = "215338"; //"215074";
   public static final String APP_KEY ="b9d8408f5b1d781f127eaa35262a3cc3"; // "77198f328e7d234b267b9c3fcadf2585";
   public static final String REQUEST_URL = "http://openapi.qzone.qq.com/oauth/qzoneoauth_request_token";
   public static final String ACCESS_URL = "http://openapi.qzone.qq.com/oauth/qzoneoauth_access_token";
   public static final String INFO_URL = "http://openapi.qzone.qq.com/user/get_user_info";
   public static String encode(byte[] from) {
       StringBuilder to = new StringBuilder((int) (from.length * TEMP) + THREE);
       int num = 0;
       char currentByte = 0;
       for (int i = 0; i < from.length; i++) {
           num = num % EIGHT;
           while (num < EIGHT) {
               switch (num) {
               case 0:
                   currentByte = (char) (from[i] & LEAD6BYTE);
                   currentByte = (char) (currentByte >>> TWO);
                   break;
               case TWO:
                   currentByte = (char) (from[i] & LAST6BYTE);
                   break;
               case FOUR:
                   currentByte = (char) (from[i] & LAST4BYTE);
                   currentByte = (char) (currentByte << TWO);
                   if ((i + ONE) < from.length) {
                       currentByte |= (from[i + ONE] & LEAD2BYTE) >>> SIX;
                   }
                   break;
               case SIX:
                   currentByte = (char) (from[i] & LAST2BYTE);
                   currentByte = (char) (currentByte << FOUR);
                   if ((i + ONE) < from.length) {
                       currentByte |= (from[i + ONE] & LEAD4BYTE) >>> FOUR;
                   }
                   break;
               default:
                   break;
               }
               to.append(ENCODE_TABLE[currentByte]);
               num += SIX;
           }
       }
       if (to.length() % FOUR != 0) {
           for (int i = FOUR - to.length() % FOUR; i > 0; i--) {
               to.append("=");
           }
       }
       return to.toString();
   }
   
	public static Map<String, String> parseTokenString(String request_token) {
      Map<String, String> tokens = new HashMap<String, String>();
      request_token += "&";
      while (request_token.length() > 0) {
      	 String key = "";
      	 String key_value = request_token.substring(0, request_token.indexOf("&"));
      	 int ind = key_value.indexOf("=");
          if(StringUtils.isNotBlank(key_value) && ind!=-1){
	          key = key_value.substring(0, ind);
	          String value = key_value.substring(ind + 1, key_value.length());
	          tokens.put(key, value);
          }
          request_token = request_token.substring(request_token.indexOf("&") + 1, request_token.length());
      }
      return tokens;
	}
	public static String getParameters(Map<String,String> params, boolean isEncode) throws Exception{
		StringBuilder str = new StringBuilder();
		if(params==null) return "";
		List keyList = Arrays.asList(params.keySet().toArray());
		Collections.sort(keyList);
		for (int i = 0; i < keyList.size(); i++) {
			if(isEncode)
				str.append(keyList.get(i) + "=" + URLEncoder.encode(params.get(keyList.get(i)),"utf-8"));
			else
				str.append(keyList.get(i) + "=" + params.get(keyList.get(i)));
			if(i<keyList.size()-1) str.append("&");
		}
		if(!isEncode) return URLEncoder.encode(str.toString(), "utf-8");
		return str.toString();
	}
	
   public static String getOauthSignature(String method, String url, Map<String,String> params, String oauth_token_secret) throws Exception{
		String stepA1 = method;
		String stepA2 = URLEncoder.encode(url, "UTF-8");
		String stepA3 = getParameters(params, false);
		String stepA = stepA1 + "&" + stepA2 + "&" + stepA3;
		String stepB = APP_KEY + "&" + oauth_token_secret;
		return getBase64Mac(stepA, stepB);
   }
   
   public static String getBase64Mac(String stepA, String stepB) throws Exception{
		byte[] oauthSignature = null;
		Mac mac = Mac.getInstance("HmacSHA1");
		SecretKeySpec spec = new SecretKeySpec(stepB.getBytes("US-ASCII"), "HmacSHA1");
		mac.init(spec);
		oauthSignature = mac.doFinal(stepA.getBytes("US-ASCII"));
		return encode(oauthSignature);
	}
   
   public static boolean verifyOpenID(String openid, String timestamp, String oauth_signature) throws Exception{
   	String str = openid + timestamp;
   	String signature = getBase64Mac(str, APP_KEY);
   	if(StringUtils.isBlank(oauth_signature)) return false;
   	return StringUtils.equals(signature,URLDecoder.decode(oauth_signature, "UTF-8"));
	}
   
   public static Map<String,String> getRequestToken() throws Exception{
		Map<String,String> params = new HashMap<String, String>();
		params.put("oauth_consumer_key", QQOauthUtil.APP_ID);
		params.put("oauth_nonce", String.valueOf(Math.random()).replaceFirst("^\\d.", ""));
		params.put("oauth_signature_method", "HMAC-SHA1");
		params.put("oauth_timestamp", String.valueOf(System.currentTimeMillis()/1000));
		params.put("oauth_version", "1.0");
		params.put("oauth_signature",getOauthSignature("GET", QQOauthUtil.REQUEST_URL, params, ""));
		return params;
   }
   
   public static Map<String,String> getAccessToken(String oauth_token, String oauth_token_secret, String oauth_vericode) throws Exception{
		Map<String,String> params = new HashMap<String, String>();
		params.put("oauth_consumer_key", QQOauthUtil.APP_ID);
		params.put("oauth_nonce", String.valueOf(Math.random()).replaceFirst("^\\d.", ""));
		params.put("oauth_signature_method", "HMAC-SHA1");
		params.put("oauth_timestamp", String.valueOf(System.currentTimeMillis()/1000));
		params.put("oauth_token", oauth_token);
		params.put("oauth_vericode", oauth_vericode);
		params.put("oauth_version", "1.0");
		params.put("oauth_signature", getOauthSignature("GET", QQOauthUtil.ACCESS_URL, params, oauth_token_secret));
		return params;
   }
   
   public static Map<String,String> getInfoToken(String oauth_token, String oauth_token_secret, String openid, String format) throws Exception{
   	Map<String,String> params = new HashMap<String, String>();
   	params.put("format", format);
		params.put("oauth_consumer_key", QQOauthUtil.APP_ID);
		params.put("oauth_nonce", String.valueOf(Math.random()).replaceFirst("^\\d.", ""));
		params.put("oauth_signature_method", "HMAC-SHA1");
		params.put("oauth_timestamp", String.valueOf(System.currentTimeMillis()/1000));
		params.put("oauth_token", oauth_token);
		params.put("oauth_version", "1.0");
		params.put("openid", openid);
		params.put("oauth_signature",getOauthSignature("GET", QQOauthUtil.INFO_URL, params, oauth_token_secret));
		return params;
   }
}
