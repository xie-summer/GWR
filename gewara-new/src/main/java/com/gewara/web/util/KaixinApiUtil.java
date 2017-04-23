package com.gewara.web.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.type.TypeReference;

import com.gewara.Config;
import com.gewara.util.BeanUtil;
import com.gewara.util.HttpResult;
import com.gewara.util.HttpUtils;
import com.gewara.util.JsonUtils;
import com.gewara.util.StringUtil;
import com.gewara.util.VmUtils;


/**
 * 开心API调用
 * @author acerge(acerge@163.com)
 * @since 4:53:06 PM Sep 14, 2010
 */
public abstract class KaixinApiUtil {
	//private static final String APP_ID = "100001944";
	private static final String API_URL = "http://rest.kaixin001.com/api/rest.php";
	/**
	 * 获取当前登录用户ID
	 * @param sessionKey
	 * @return userid
	 */
	public static String getLoggedInUser(String sessionKey, Config config){
		Map<String, String> params = new HashMap<String, String>();
		params.put("method", "users.getLoggedInUser");
		params.put("session_key", sessionKey);
		addSign(params, config);
		HttpResult result = HttpUtils.postUrlAsString(API_URL, params);
		Map user = VmUtils.readJsonToMap(result.getResponse());
		if(user == null) return null;
		Object userid = user.get("result");
		if(userid==null) return null;
		if(StringUtils.equals("0", ""+userid)) return null;
		return ""+userid;
	}
	/**
	 * 查询用户信息
	 * @param sessionKey
	 * @param uidList 用户ID
	 * @return List<Map(uid,online,gender,logo50,name....)>
	 */
	public static Map<String/*uid*/, Map> getUserInfoMap(String sessionKey, List<String> uidList, Config config){
		List<Map> userList = getUserInfoList(sessionKey, uidList, config);
		Map<String, Map> result = new HashMap<String, Map>();
		for(Map user: userList){
			result.put(""+user.get("uid"), user);
		}
		return result;
	}
	/**
	 * 查询用户信息
	 * @param sessionKey
	 * @param uidList 用户ID
	 * @return List<Map(uid,online,gender,logo50,name....)>
	 */
	public static List<Map> getUserInfoList(String sessionKey, List<String> uidList, Config config){
		List<List<String>> groupList = BeanUtil.partition(uidList, 45);
		List<Map> result = new ArrayList<Map>(), tmp;
		for(List<String> group:groupList){
			tmp = getUserInfoList(sessionKey, StringUtils.join(group, ","), config);
			if(tmp!=null) result.addAll(tmp);
		}
		return result;
	}
	/**
	 * 查询用户信息
	 * @param sessionKey
	 * @param uids 英文逗号分隔,最多50个
	 * @return List<Map(uid,online,gender,logo50,name....)>
	 */
	public static List<Map> getUserInfoList(String sessionKey, String uids, Config config){
		Map<String, String> params = new HashMap<String, String>();
		params.put("method", "users.getInfo");
		params.put("uids", uids);
		params.put("session_key", sessionKey);
		addSign(params, config);
		HttpResult result = HttpUtils.postUrlAsString(API_URL, params);
		List<Map> userList = JsonUtils.readJsonToObject(new TypeReference<List<Map>>(){}, result.getResponse());
		return userList;
	}
	/**
	 * 获取当前登录用户的朋友ID列表
	 * @param sessionKey
	 * @return
	 */
	public static List<String> getFriendidList(String sessionKey, Config config){
		Map<String, String> params = new HashMap<String, String>();
		params.put("method", "friends.get");
		params.put("session_key", sessionKey);
		addSign(params, config);
		HttpResult result = HttpUtils.postUrlAsString(API_URL, params);
		List<String> friendidList = JsonUtils.readJsonToObject(new TypeReference<List<String>>(){}, result.getResponse());
		return friendidList;
	}
	/**
	 * 查询当前登录用户的朋友列表
	 * @param sessionKey
	 * @param uids 英文逗号分隔,最多50个
	 * @return List<Map(uid,online,gender,logo50,....)>
	 */
	public static List<Map> getFriendList(String sessionKey, Config config){
		Map<String, String> params = new HashMap<String, String>();
		params.put("method", "friends.getFriends");
		params.put("session_key", sessionKey);
		addSign(params, config);
		HttpResult result = HttpUtils.postUrlAsString(API_URL, params);
		List<Map> userList = JsonUtils.readJsonToObject(new TypeReference<List<Map>>(){}, result.getResponse());
		return userList;
	}
	public static boolean isAppUser(){
		//don't implement it 
		return true;
	}
	private static void addSign(Map<String, String> params, Config config){
		params.put("api_key", config.getString("kaixinapikey"));
		params.put("call_id", System.currentTimeMillis()+"");
		params.put("v", "1.0");
		List<String> keyList = new ArrayList(params.keySet());
		Collections.sort(keyList);
		String pstr="";
		for (String key: keyList){
			if(StringUtils.isNotBlank(params.get(key))){
				pstr += key + "=" + params.get(key);
			}
		}
		pstr += config.getString("kaixinsecretkey");
		//生成签名
		String sign = StringUtil.md5(pstr);
		params.put("sig", sign);
	}
	public static String replaceAllString(String str){
		if(StringUtils.isNotBlank(str))
			return str.replace("+", "*").replace("/","-").replace("=","");
		return str;
	}
}
