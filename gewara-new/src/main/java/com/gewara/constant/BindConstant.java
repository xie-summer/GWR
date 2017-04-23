package com.gewara.constant;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.map.UnmodifiableMap;

public class BindConstant {
	public static final String KEY_BINDTIME = "bindTime";	//绑定手机时间，手机注册直接是注册时间

	public static final String TAG_REGISTERCODE = "registercode"; 	//获取手机注册、快速登录动态码
	//public static final String TAG_DYNAMICCODE = "dynamiccode"; 	//与TAG_REGISTERCODE合并
	
	public static final String TAG_BINDMOBILE = "bindMobile"; 		//手机解绑
	
	public static final String TAG_ACCOUNT_BACKPASS = "account_backpass"; //手机找回支付密码
	public static final String TAG_DYNAMICCODE_CARD = "dynamiccode_card"; //激活票券的手机动态码
	public static final String TAG_BACKPASS = "backpass"; 		//手机找回密码
	public static final String TAG_MODIFYPASS = "modifypass"; 	//修改密码

	public static final String TAG_SETPAYPASS = "setpaypass"; 	//设置支付密码
	public static final String TAG_MDYPAYPASS = "mdypaypass"; 	//修改支付密码
	public static final String TAG_CHGBINDMOBILE = "chgbindMobile"; //修改手机绑定
	public static final String TAG_DRAWMOBILE = "drawMobile"; 		//抽奖手机验证
	public static final String TAG_CCBANKCODE = "ccbankcode";	//建设银行动态码
	public static final String TAG_GETPAYPASS = "getpaypass"; 	//找回支付密码 TODO:与TAG_ACCOUNT_BACKPASS的区别？？
	public static final String TAG_VDEMAIL_BY_UPDATEPWD = "vdemailbyuppwd"; //修改密码前置邮箱安全验证

	
	//TODO:组织专门常量或存入数据库
	public static final List<String> VALID_TAG_LIST = Arrays.asList(
			TAG_REGISTERCODE,
			TAG_BINDMOBILE,
			TAG_ACCOUNT_BACKPASS,
			TAG_DYNAMICCODE_CARD,
			TAG_BACKPASS,
			TAG_MODIFYPASS,
			TAG_SETPAYPASS,
			TAG_MDYPAYPASS,
			TAG_CHGBINDMOBILE,
			TAG_DRAWMOBILE,
			TAG_CCBANKCODE,
			TAG_GETPAYPASS,
			TAG_VDEMAIL_BY_UPDATEPWD
		);

	//默认短信
	public static final String DEFAULT_TEMPLATE = "checkpass动态码，使用和30分钟过期无效；非本人或授权操作，为确保账户安全，请致电4000406506";
	public static final String ADMIN_MOBILE_TEMPLATE = "checkpass感谢使用格瓦拉电话购票服务，此验证码用于校验用户信息是否合法，如非本人操作请忽略此信息或联系格瓦拉客服咨询：4000-406-506";
	public static final int VALID_MIN = 30;			//有效时长(MINUTE)
	public static final int MAX_CHECKNUM = 5;
	public static final int MAX_SENDNUM = 99999;
	
	private static final Map<String, Integer> SENDNUM_MAP;
	private static final Map<String, Integer> MAXCHECK_MAP;
	private static final Map<String, String> TEMPLATE_MAP;
	//默认最大发送数量
	
	static{
		Map<String, String> tmp = new HashMap<String, String>();
		tmp.put(TAG_REGISTERCODE, "checkpass注册动态码，使用和30分钟过期无效；非本人或授权操作，为确保账户安全，请致电4000406506");
		tmp.put(TAG_CCBANKCODE, "checkpass支付动态码，使用和30分钟过期无效；非本人或授权操作，为确保账户安全，请致电4000406506");
		TEMPLATE_MAP = UnmodifiableMap.decorate(tmp);
		
		Map<String, Integer> tmp2 = new HashMap<String, Integer>();
		tmp2.put(TAG_REGISTERCODE, 20);		//注册码一手机只允许发20次
		tmp2.put(TAG_MODIFYPASS, 20);
		tmp2.put(TAG_BINDMOBILE, 20);
		SENDNUM_MAP = UnmodifiableMap.decorate(tmp2);
		
		tmp2 = new HashMap<String, Integer>();
		tmp2.put(TAG_MODIFYPASS, 8);
		tmp2.put(TAG_SETPAYPASS, 8);
		MAXCHECK_MAP = UnmodifiableMap.decorate(tmp2);
	}

	public static String getMsgTemplate(String tag) {
		if(TEMPLATE_MAP.containsKey(tag)) return TEMPLATE_MAP.get(tag);
		return DEFAULT_TEMPLATE;
	}
	public static int getMaxSendnum(String tag){
		if(SENDNUM_MAP.containsKey(tag)) return SENDNUM_MAP.get(tag);
		return MAX_SENDNUM;
	}
	public static int getMaxCheck(String tag){
		if(MAXCHECK_MAP.containsKey(tag)) return MAXCHECK_MAP.get(tag);
		return MAX_CHECKNUM;
	}

}
