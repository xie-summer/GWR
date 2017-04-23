/**
 * 
 */
package com.gewara.pay;

import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;

import com.gewara.Config;
import com.gewara.constant.sys.LogTypeConstant;
import com.gewara.helper.order.GewaOrderHelper;
import com.gewara.model.api.ApiUser;
import com.gewara.model.pay.GewaOrder;
import com.gewara.model.pay.TicketOrder;
import com.gewara.util.GewaLogger;
import com.gewara.util.HttpResult;
import com.gewara.util.HttpUtils;
import com.gewara.util.LoggerUtils;
import com.gewara.util.StringUtil;
import com.gewara.util.VmUtils;


public class TaoBaoUtil {
	private static final transient GewaLogger dbLogger = LoggerUtils.getLogger(TaoBaoUtil.class, Config.getServerIp(), Config.SYSTEMID);
	public static Map<String, String> getPayParams(GewaOrder gorder, ApiUser apiUser) {
		Map<String, String> params = new LinkedHashMap<String, String>();
		TicketOrder order = (TicketOrder)gorder;
		String seatInfo = "";
		try {
			seatInfo = Base64.encodeBase64String(order.gainSeatTextFromDesc().getBytes("GBK"));
		} catch (Exception e) {
			dbLogger.errorWithType(LogTypeConstant.LOG_TYPE_ORDER_PAY, "", e);
			seatInfo = "";
		}
		String[] ukey = GewaOrderHelper.getPartnerUkey(order).split("_");
		params.put("userId", ukey[0]);
		if(ukey.length>1) params.put("nickname", ukey[1]);
		params.put("extScheduleId", order.getMpid()+"");
		params.put("extOrderId", order.getTradeNo());
		params.put("amount", order.getDue()+"");
		params.put("userPhone", order.getMobile());
		params.put("seatCount", order.getQuantity()+"");
		params.put("seatInfo", seatInfo);
		params.put("sign", getSign(order, apiUser.getPrivatekey()));
		params.put("checkcode", "91af68e94330e00004d7143994077c65");
		HttpResult code = HttpUtils.postUrlAsString(apiUser.getAddOrderUrl(), params);
		if(code.isSuccess()){
			String res = code.getResponse();
			Map<String, String> resMap = VmUtils.readJsonToMap(res);
			params = new LinkedHashMap<String, String>();
			String payurl = resMap.get("payUrl");
			dbLogger.warnWithType(LogTypeConstant.LOG_TYPE_ORDER_PAY, "Ã‘±¶÷ß∏∂¡¥Ω”: tradeNo:" + order.getTradeNo()+ ", orderid:" + order.getId() +", payurl:" + payurl);
			if(StringUtils.isBlank(payurl)) throw new IllegalArgumentException(resMap.get("msg"));
			params.put("payurl", payurl);
			if(StringUtils.isBlank(payurl)) params.put("msg", resMap.get("msg"));
			return params;
		}
		return params;
	}
	private static String getSign(TicketOrder order, String privatekey){
		return StringUtil.md5(order.getTradeNo()+order.getDue()+order.getMobile()+privatekey);
	}
}
