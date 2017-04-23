package com.gewara.pay;

import java.util.Map;

import org.apache.commons.lang.StringUtils;

import com.buybal.util.signers.SignVer;
import com.buybal.util.signers.SignVerException;
import com.gewara.Config;
import com.gewara.constant.sys.LogTypeConstant;
import com.gewara.util.GewaLogger;
import com.gewara.util.LoggerUtils;


/**
 * 拉卡拉支付
 * @author acerge(acerge@163.com)
 * @since 7:11:08 PM Sep 28, 2010
 */
public class LakapayUtil {
	private static final transient GewaLogger dbLogger = LoggerUtils.getLogger(LakapayUtil.class, Config.getServerIp(), Config.SYSTEMID);
	public static final transient String merid = "320103";//商户号
	private static final transient String priKeyFile = LakapayUtil.class.getClassLoader().getResource("com/gewara/pay/3E090002301.key.p8").getFile();
	private static final transient String pubKeyFile = LakapayUtil.class.getClassLoader().getResource("com/gewara/pay/3E090002301la.cert.der").getFile();

	public static String getSign(String str){
		String sign="";
		try {
			sign = SignVer.sign(str, priKeyFile);
		} catch (SignVerException e) {
			dbLogger.errorWithType(LogTypeConstant.LOG_TYPE_ORDER_PAY, "", e);
		}
		return sign;
	}
	public static boolean verifySign(String datastr, String sign){
		try {
			return SignVer.verify(datastr, sign, pubKeyFile);
		} catch (SignVerException e) {
			dbLogger.errorWithType(LogTypeConstant.LOG_TYPE_ORDER_PAY, "", e);
		}
		return false;
	}
	public static String getJoinParam(Map<String, String> params) {
		String result = "";
		for(String key: params.keySet()){
			result += key+"=" + params.get(key) + "&";
		}
		result = StringUtils.substring(result, 0, result.length() -1);
		return result;
	}
}
