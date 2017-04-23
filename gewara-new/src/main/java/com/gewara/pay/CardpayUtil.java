package com.gewara.pay;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import com.gewara.Config;
import com.gewara.constant.sys.LogTypeConstant;
import com.gewara.model.pay.GewaOrder;
import com.gewara.util.DateUtil;
import com.gewara.util.GewaLogger;
import com.gewara.util.HttpResult;
import com.gewara.util.HttpUtils;
import com.gewara.util.JsonUtils;
import com.gewara.util.LoggerUtils;
import com.gewara.util.StringUtil;

/**
 * 百联E城
 * @author acerge(acerge@163.com)
 * @since 2:00:21 PM Jan 12, 2011
 */
public class CardpayUtil {
	private static final transient GewaLogger dbLogger = LoggerUtils.getLogger(CardpayUtil.class, Config.getServerIp(), Config.SYSTEMID);
	private static final String paygateway = "https://reg.blemall.com/jfservice/gwr_order.php";
	private static final String QUERY_URL = "https://reg.blemall.com/jfservice/gwr_query.php";
	private static final String NOTIFY_URL = "http://manage.gewara.com/pay/shokwNotify.xhtml";
	private static final String RETURN_URL = "http://www.gewara.com/pay/shokwReturn.xhtml";
	private static final String DSHH = "000209";//"999999";//商户号，百联提供
	private static final String DSPHH = "00237636";//商品货号，百联提供
	private static final String PRIVATEKEY = "iej89sjh12";
	private static final String ENCODEING = "GBK";
	public static Map getNetPayParams(GewaOrder order){
		Map params = new HashMap();
		params.put("payurl", paygateway);
		params.put("dshh", DSHH);
		params.put("dsphh", DSPHH);
		params.put("dddbh", order.getTradeNo());
		params.put("dje", order.getDue());
		
		Map<String, String> desMap = JsonUtils.readJsonToMap(order.getDescription2());
		String desc = desMap.get("影片") + ";" + order.getOrdertitle().replaceAll("电影票", "");
		desc += ";" + desMap.get("场次") + ";" + order.getQuantity();
		desc += ";" + desMap.get("影票") + ";" + DateUtil.formatTimestamp(order.getValidtime()); 
		params.put("returnUrl", RETURN_URL);
		params.put("notifyUrl", NOTIFY_URL);
		params.put("dxgxx", desc);

		params.put("checkvalue", getCheckValue(order.getTradeNo(), "" + order.getDue()));
		return params;
	}
	public static String getCheckValue(String tradeNo, String payamount) {
		return StringUtil.md5(DSHH + DSPHH + tradeNo + payamount + PRIVATEKEY, ENCODEING);
	}
	/**
	 * 回查订单是否存在
	 * @param tradeNo
	 * @param payamount
	 * @param payamount2
	 * @param checkvalue
	 * @return
	 */
	public static String validate(String tradeNo, String payamount, String checkvalue) {
		if(!StringUtils.equals(checkvalue, getCheckValue(tradeNo, payamount))){
			return "false";
		}
		Map<String, String> params = new HashMap<String, String>();
		params.put("dddbh", tradeNo);
		params.put("dje", payamount);
		String qcheck = StringUtil.md5(tradeNo + payamount + PRIVATEKEY, ENCODEING);
		params.put("checkvalue", qcheck);
		HttpResult result = HttpUtils.postUrlAsString(QUERY_URL, params);
		if(!result.isSuccess()) dbLogger.warnWithType(LogTypeConstant.LOG_TYPE_ORDER_PAY, ""+params);
		return result.getResponse();
	}
}
