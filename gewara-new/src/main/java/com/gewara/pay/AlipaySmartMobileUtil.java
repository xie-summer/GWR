/**
 * 
 */
package com.gewara.pay;

import java.net.URLEncoder;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;

import com.gewara.Config;
import com.gewara.constant.sys.LogTypeConstant;
import com.gewara.model.pay.Charge;
import com.gewara.model.pay.GewaOrder;
import com.gewara.util.DateUtil;
import com.gewara.util.GewaLogger;
import com.gewara.util.LoggerUtils;

/**
 * @author Administrator
 * 
 */
public class AlipaySmartMobileUtil {
	private static final transient GewaLogger dbLogger = LoggerUtils.getLogger(AlipaySmartMobileUtil.class, Config.getServerIp(), Config.SYSTEMID);
	private static final String ALIPAY_PARTNER = "2088301097013387";// 合作商户ID
	private static final String ALIPAY_SELLER = "2088301097013387";// 账户ID
	public static final String ALIPAY_RSA_PRIVATE = "MIICdgIBADANBgkqhkiG9w0BAQEFAASCAmAwggJcAgEAAoGBAM0dIXJxZhpYRSgUk8sTYCZ+oaA/IJv4LO4IAUZxppbIahV8LE3wcgMTQ1Y4MtCMro5Wi3z30OpUOgk+JzLL5+W27LPkyanXCrXnBO3VX9GPEHQ9bpWSoReORTwGOtmeOeU2CEA+Tzq0Q5q0SQkpynMhTdcY6VulERa/PVkRrEf9AgMBAAECgYEAtNbrJ1BtmeqbRoQl7dSuYCIjc/pUUM8VJeQu46HOI4Cdb0Xkde75RlbUdo7j1lVCjUImh06ihgcuh/mha7q1P5DESHzYVedGHBI/LWVJF+Df3buTuTPsjIodegYOwVeBz9jkKaXax4a6XRKH2YKkgBt2KKZv/QexQxgcg/wj98ECQQD2wZBTnYs0GS3GMavGS5U3KsEIH0cSsvZkKpfjdyEAAfJzmOBBtm/zEBuFvamLKLof6SSIMor1bsSURCwSRBSRAkEA1Mw1ndA7VI3f3mNxLgz4DAe3Axi+6HcMYMEl/xhq7Xv9nHYEshrF4h/2qQEgtD1FQz0UTMqYa3uD1RXAkQ1CrQJAXdW55rIwb31rtMTKx7uSYMo3YblTM78uh2TTIcL5n6Ed6+ukzBhudgYYEUHQqYSxUtU7+TcPNMoMoz1RbRjKoQJAPPL8jGX/CFnWfCj5WtpGFZQEHDPzQjwO5IuN6YHB5qFz42N+NtEqSnkjOgbjqhw+gWK2NMR2YZqwCNFBJYqpxQJAcXcaHz9y44U2NAX7HHsxu8417TsoWBh5ExE0oVgKarl7NbcfwYwq5rgCoGHKbVA6RQENC+gw/s6oKlHgl5QVbA==";
																	 
	public static final String SIGN_ALGORITHMS = "SHA1WithRSA";
	public static final String RSA_ALIPAY_PUBLIC = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCENYmUCi3/A80RzeOMZuTUs7PbvrOthMlSwBAv 5QgukiOs27r/6Dh1GPmJroGjYP/iGRz5ivxuWXjPRqVpX1ZCtinZULLgy5H8NJuQULAl2mQUrp/5 JYqxW4/t7EwsyRfmUCGaiEy+Mh4FS8B5wZCHM4rfh5tydko4rudPHHGl1QIDAQAB";
	//私钥转换成PKCS8格式。
	/**
	 * RSA验签名检查
	 * 
	 * @param content
	 *           待签名数据
	 * @param sign
	 *           签名值
	 * @return 布尔值
	 */
	public static Map<String, String> getAlipaySmartPayParams(GewaOrder order) {
		Map<String, String> params = new LinkedHashMap<String, String>();
		String tradeNo = order.getTradeNo();
		String amount = order.getDue() + "";
		String orderDesc = order.getOrdertitle();
		params.put("partner", ALIPAY_PARTNER);
		params.put("seller", ALIPAY_SELLER);
		params.put("out_trade_no", tradeNo);
		params.put("subject", orderDesc);
		params.put("body", "请在" + DateUtil.formatTimestamp(order.getValidtime()) + "前付款");
		params.put("total_fee", amount);
		params.put("notify_url", PayUtil.getAlipaySmartNotify());
		Map<String, String> payParams = new LinkedHashMap<String, String>();
		payParams.putAll(params);
		String sign = doSign(getOrderInfo(params), "UTF-8");
		dbLogger.warnWithType(LogTypeConstant.LOG_TYPE_ORDER_PAY, sign);
		try {
			payParams.put("sign", URLEncoder.encode(sign, "UTF-8"));
		} catch (Exception e) {
			e.printStackTrace();
		}
		payParams.put("sign_type", "RSA");
		return payParams;
	}
	public static Map<String, String> getAlipaySmartChargeParams(GewaOrder order, Charge charge) {
		Map<String, String> params = new LinkedHashMap<String, String>();
		String tradeNo = charge.getTradeNo();
		String amount = charge.getTotalfee()+"";
		String orderDesc = order.getOrdertitle();
		params.put("partner", ALIPAY_PARTNER);
		params.put("seller", ALIPAY_SELLER);
		params.put("out_trade_no", tradeNo);
		params.put("subject", orderDesc);
		params.put("body", "请在" + DateUtil.formatTimestamp(order.getValidtime()) + "前付款");
		params.put("total_fee", amount);
		params.put("notify_url", PayUtil.getAlipaySmartNotify());
		Map<String, String> payParams = new LinkedHashMap<String, String>();
		payParams.putAll(params);
		String sign = doSign(getOrderInfo(params), "UTF-8");
		dbLogger.warnWithType(LogTypeConstant.LOG_TYPE_ORDER_PAY, sign);
		try {
			payParams.put("sign", URLEncoder.encode(sign, "UTF-8"));
		} catch (Exception e) {
			e.printStackTrace();
		}
		payParams.put("sign_type", "RSA");
		return payParams;
	}
	private static String getOrderInfo(Map<String, String> params) {
		Set<String> set = params.keySet();
		StringBuilder sb = new StringBuilder();
		for (String key : set) {
			String value = params.get(key);
			if(StringUtils.isNotBlank(value)){
				sb.append("&" + key + "=\"" + value.trim() + "\"");
			}
		}
		return sb.toString().replaceFirst("&", "");
	}

	private static String doSign(String content, String encoding) {
		try {
			KeyFactory keyFactory = KeyFactory.getInstance("RSA");
			byte[] encodedKey = ALIPAY_RSA_PRIVATE.getBytes(encoding);
			// 先base64解码
			encodedKey = Base64.decodeBase64(encodedKey);
			PrivateKey priKey = keyFactory
					.generatePrivate(new PKCS8EncodedKeySpec(encodedKey));
			Signature signature = Signature.getInstance("SHA1WithRSA");
			signature.initSign(priKey);
			signature.update(content.getBytes(encoding));
			byte[] signed = signature.sign();
			return new String(Base64.encodeBase64(signed));
		} catch (Exception e) {
			e.printStackTrace();
			return "";
		}
	}

	public static String sign(String content) {
		String charset = "utf-8";
		try {
			byte[] b2 = Base64.decodeBase64(ALIPAY_RSA_PRIVATE);
			PKCS8EncodedKeySpec priPKCS8 = new PKCS8EncodedKeySpec(b2);
			KeyFactory keyf = KeyFactory.getInstance("RSA");
			PrivateKey priKey = keyf.generatePrivate(priPKCS8);
			
			java.security.Signature signature = java.security.Signature.getInstance(SIGN_ALGORITHMS);
			signature.initSign(priKey);
			signature.update(content.getBytes(charset));
			byte[] signed = signature.sign();
			return Base64.encodeBase64String(signed);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	public static boolean doCheck(String content, String sign) {
		try {
			content = "notify_data=" + content;
			KeyFactory keyFactory = KeyFactory.getInstance("RSA");
			byte[] encodedKey = Base64.decodeBase64(RSA_ALIPAY_PUBLIC);
			PublicKey pubKey = keyFactory.generatePublic(new X509EncodedKeySpec(
					encodedKey));
			java.security.Signature signature = java.security.Signature
					.getInstance(SIGN_ALGORITHMS);
			signature.initVerify(pubKey);
			signature.update(content.getBytes("utf-8"));
			boolean bverify = signature
					.verify(org.apache.commons.codec.binary.Base64
							.decodeBase64(sign));
			return bverify;
		} catch (Exception e) {
			dbLogger.errorWithType(LogTypeConstant.LOG_TYPE_ORDER_PAY, "", e);
		}
		return false;
	}
}
