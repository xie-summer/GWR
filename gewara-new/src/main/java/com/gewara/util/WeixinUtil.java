package com.gewara.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import com.gewara.Config;
import com.gewara.support.ErrorCode;
import com.gewara.xmlbind.partner.WeixinMsg;

public class WeixinUtil {
	private static final transient GewaLogger dbLogger = LoggerUtils.getLogger(WeixinUtil.class, Config.getServerIp(), Config.SYSTEMID);
	public final static String GETTOKEN_URL = "https://api.weixin.qq.com/cgi-bin/token";
	public final static String CREATEMENU_URL = "https://api.weixin.qq.com/cgi-bin/menu/create";
	
	//关注账号
	public final static String EVENT_SUBSCRIBE = "subscribe";
	public final static String EVENT_UNSUBSCRIBE = "unsubscribe";
	
	//事件类型
	public final static String KEY_HOTMOVIE = "hotMovie";
	public final static String KEY_NEARCINEMA = "nearCinema";
	public final static String KEY_DAYWALA = "dayWala";
	public final static String KEY_WALAALL = "walaAll";
	public final static String KEY_MYORDER = "myOrder";
	public final static String KEY_TICKETPWD = "ticketPwd";
	public final static String KEY_MYACCOUNT = "myAccount";
	public final static String KEY_WEIKEFU = "weiKefu";
	public final static String KEY_KEFUTEL = "kefuTel";
	public final static String KEY_DOWNAPP = "downApp";
	
	public static final List<String> getEventList(){
		List<String> eventList = new ArrayList();
		eventList.add(KEY_MYORDER);
		eventList.add(KEY_TICKETPWD);
		eventList.add(KEY_MYACCOUNT);
		return eventList;
	}
	public static boolean isValidText(String content) {
		return StringUtil.regMatch(content, "^[\\w-\u4e00-\u9fa5]+$", true);
	}

	
	//创建菜单
	public Map<String, List<Button>> getButtonList(){
		List<SubButton> subList1 = getSubButton(new String[]{"热门电影,hotMovie", "周边影院,nearCinema", "观影指南,dayWala"});
		List<SubButton> subList2 = getSubButton(new String[]{"我的订单,myOrder", "取票密码,ticketPwd", "我的账户,myAccount"});
		List<SubButton> subList3 = getSubButton(new String[]{"微客服,weiKefu", "客服热线,kefuTel", "下载APP,downApp"});
		Button b1 = new Button("哇啦淘", subList1);
		Button b2 = new Button("个人中心", subList2);
		Button b3 = new Button("联系我们", subList3);
		List<Button> buttonList = new ArrayList();
		buttonList.add(b1);
		buttonList.add(b2);
		buttonList.add(b3);
		Map<String, List<Button>> map = new HashMap<String, List<Button>>();
		map.put("button", buttonList);
		return map;
	}
	
	private List<SubButton> getSubButton(String[] str ){
		List<SubButton> subList = new ArrayList<SubButton>();
		for(String ss : str){
			SubButton sb = new SubButton();
			String[] s = StringUtils.split(ss, ",");
			sb.setType("click");
			sb.setName(s[0]);
			sb.setKey(s[1]);
			subList.add(sb);
		}
		return subList;
	}
	// 全角转化为半角
	public static String toHalf(String input) {
		if(StringUtils.isBlank(input)) return "";
		char[] c = input.toCharArray();
		for (int i = 0; i < c.length; i++) {
			if (c[i] == 12288) {
				c[i] = (char) 32;
				continue;
			}
			if (c[i] > 65280 && c[i] < 65375) {
				c[i] = (char) (c[i] - 65248);
			}
		}
		return new String(c);
	}
	
	
	//创建菜单	
	public static ErrorCode<String> createMenu(String access_token, String body) {
		HttpResult result = HttpUtils.postBodyAsString(CREATEMENU_URL + "?access_token=" + access_token, body);
		String response = result.getResponse();
		dbLogger.warn(result.getResponse() + "," + result.getMsg());
		if(result.isSuccess()){
			return ErrorCode.getFailure(result.getMsg());
		}
		Map<String, String> map = JsonUtils.readJsonToMap(response);
		String errmsg = map.get("errmsg");
		if(!StringUtils.equalsIgnoreCase(errmsg, "ok")){
			return ErrorCode.getFailure(errmsg);
		}
		return ErrorCode.SUCCESS;
	}
	public static String getContent(WeixinMsg msg){
		String content = msg.getContent();
		if(StringUtils.isBlank(content)) content = "";
		content = content.trim();
		content = toHalf(content);
		return content;
	}
	
	public static void main(String[] args) {
		/*ErrorCode<Map<String, String>> code = getAccessToken();
		if(!code.isSuccess()){
			System.out.println(code.getMsg());
		}
		System.out.println(code.getRetval().toString());*/
		/*WeixinUtil weixin = new WeixinUtil();
		String body = JsonUtils.writeObjectToJson(weixin.getButtonList());
		createMenu("7va6Kc5RBQ1uCQ3Vq5ntHoma46mwtR8qa3fnEI1M-fUm0at1dxCZnbFytBYHNJ6TvIXv_BnisLqoTcBHTpbpH5f0QJGql3g2w4AQLZNA41X5Z3RrK_yHiJobkwRalDRGY-Cell5MIbHtO0--M-U36g", body);*/
	}
	
	class SubButton{
		private String type;
		private String name;
		private String key;
		public String getType() {
			return type;
		}
		public void setType(String type) {
			this.type = type;
		}
		public String getName() {
			return name;
		}
		public void setName(String name) {
			this.name = name;
		}
		public String getKey() {
			return key;
		}
		public void setKey(String key) {
			this.key = key;
		}
	}
	class Button{
		private String name;
		private List<SubButton> sub_button;
		public Button(){
			
		}
		public Button(String name, List<SubButton> sub_button){
			this.name = name;
			this.sub_button = sub_button;
		}
		public String getName() {
			return name;
		}
		public void setName(String name) {
			this.name = name;
		}

		public List<SubButton> getSub_button() {
			return sub_button;
		}

		public void setSub_button(List<SubButton> sub_button) {
			this.sub_button = sub_button;
		}
	}
}
