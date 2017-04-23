/**
 * 
 */
package com.gewara.pay;

import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import com.gewara.Config;
import com.gewara.constant.sys.LogTypeConstant;
import com.gewara.model.api.ApiUser;
import com.gewara.model.pay.GewaOrder;
import com.gewara.model.pay.TicketOrder;
import com.gewara.util.CAUtil;
import com.gewara.util.DateUtil;
import com.gewara.util.GewaLogger;
import com.gewara.util.HttpResult;
import com.gewara.util.HttpUtils;
import com.gewara.util.JsonUtils;
import com.gewara.util.LoggerUtils;
import com.gewara.util.VmUtils;

public class MobileTicketUtil {
	private static final transient GewaLogger dbLogger = LoggerUtils.getLogger(MobileTicketUtil.class, Config.getServerIp(), Config.SYSTEMID);
	private static final String channelCode = "100860004";
	private static final String pKey = "MIICdwIBADANBgkqhkiG9w0BAQEFAASCAmEwggJdAgEAAoGBAMFvNHDqnBdZl4pfir2cWE%2BYzvro%0D%0A1arX4BDsB41npm07zKYeJsUb2CfaVO%2BS8o5Q5mDFV53rd1hwvRIIUtz9vVmoJVpeZ9P8DNfSJywy%0D%0AMGKZa78lhSLKSxr3Kh2sVA%2FHRvdHA%2FhXahsQ4GLuPLp%2B9zEFfbQgSpGQps1EY0GW1h03AgMBAAEC%0D%0AgYA8%2BEFOShSzI3Epk13W0B3h0OeEnLkkZXDhxJPI1V%2FW1F94CM7tmA402ZOmA%2BpiQ0uqOumYBC0U%0D%0A%2BkwOEAOWVoBE4siCne08yYlCXrPBcT%2B8Z0UUsniL0%2BRhfx6LY5rct8MDmyw3EMxvPU%2Bpb2JCJCao%0D%0AsZCqqwXbl13oA95IAHku6QJBAOq6edF%2FxzEMYIL89Nv5r%2Bv5MYfzbkedZzmPJJjWVIPX9rmkbe4l%0D%0AF%2FTOsS2IWCNonatZr0Nql0aC%2BLSCMuytRmsCQQDS9r0G52WNvkCCIqRubVrchaaqxudzxG0FReCG%0D%0AInqHEKAuyvjB4ozVw%2FT1dwhq1wBoI%2BRlssXwckj1eO8Nrz9lAkAmlbKyqnN%2B747p2VUS1%2B%2BANb1b%0D%0AoLtvEEPIpWwZp5nK9nQ7PflIHVbbyiI73t7GK0tHwH1b8qidUag6W%2FCtESqzAkEAjMYBFR5L8e3G%0D%0AdhBP0TA%2FtW%2Bp68OWpvWoGiCq1lcjlAVQHmlq3VYjzHFagIHRqBmYN%2BXbftFwrCZW8Ralzmfm0QJB%0D%0AAMsI4H7249desVloO7YmUNdvghTujxp4n4COSeXca0gaVGJesrrOXEQFZWCu9N%2BYNd33yZOKSGLc%0D%0Afh%2Fra5eV5qg%3D";
	
	public static String pushSuccessOrder(TicketOrder order, ApiUser partner,String userid,String basePath){
		Map<String, String> otherMap = VmUtils.readJsonToMap(partner.getOtherinfo());
		Map<String, String> params = new HashMap<String, String>();
		Map<String, String> desMap = JsonUtils.readJsonToMap(order.getDescription2());
		String createTime = DateUtil.format(order.getAddtime(), "yyyy-MM-dd HH:mm:ss");
		params.put("channelCode", channelCode);
		params.put("outerOrderNo", order.getTradeNo());
		params.put("createTime", createTime);
		params.put("customerSeq", userid);
		params.put("orderPriceSum", (order.getDue()*100)+"");
		params.put("goodsName", desMap.get("影片"));
		params.put("ticketName", order.getOrdertitle() + desMap.get("场次") + " " +desMap.get("影票"));
		params.put("goodsNum", order.getQuantity()+"");
		params.put("goodsId", order.getMovieid()+"");
		try {
			String url = otherMap.get("pushUrl").replace("channelCode", channelCode);
			if(StringUtils.isNotBlank(basePath))url = url.replaceFirst("http://www.sh.10086.cn/ticket/", basePath);
			HttpResult result = HttpUtils.postUrlAsString(url, params);
			Map<String, String> resMap = VmUtils.readJsonToMap(result.getResponse());
			return resMap.get("resCode");
		} catch (Exception e) {
			dbLogger.warnWithType(LogTypeConstant.LOG_TYPE_ORDER_PAY, "回传订单失败:" + order.getTradeNo() + "," + e);
			return "error";
		}
	}

	public static Map<String, String> getPayParams(GewaOrder order, ApiUser partner, String notify) {
		Map<String, String> params = new HashMap<String, String>();
		Map<String, String> desMap = JsonUtils.readJsonToMap(order.getDescription2());
		String createTime = DateUtil.format(order.getAddtime(), "yyyy-MM-dd HH:mm:ss");
		TicketOrder tOrder = (TicketOrder)order;
		params.put("channelCode", channelCode);
		params.put("outerOrderNo", order.getTradeNo());
		params.put("createTime", createTime);
		params.put("actualPriceSum", (order.getDue()*100)+"");
		params.put("orderPriceSum", (order.getTotalAmount()*100)+"");
		params.put("goodsId", tOrder.getMovieid()+"");
		params.put("goodsName", desMap.get("影片"));
		params.put("ticketName", order.getOrdertitle() + desMap.get("场次") + " " +desMap.get("影票"));
		params.put("goodsNum", order.getQuantity()+"");
		Map<String, String> dataMap = JsonUtils.readJsonToMap(order.getOtherinfo());
		String basePath = dataMap.get("basePath");
		params.put("payurl", partner.getAddOrderUrl());
		if(StringUtils.isNotBlank(basePath))params.put("payurl",partner.getAddOrderUrl().replaceFirst("http://www.sh.10086.cn/ticket/", basePath));
		params.put("notifyUrl", notify);
		return params;
	}

	public static Map<String, String> getUserLoginStatus(ApiUser partner,String tokenid,String basePath){
		if(StringUtils.isBlank(tokenid)) return null;
		try {
			Map<String, String> otherMap = VmUtils.readJsonToMap(partner.getOtherinfo());
			String url = StringUtils.replace(otherMap.get("loginUrl").replace("channelCode", channelCode), "tokenid", tokenid);
			if(StringUtils.isNotBlank(basePath))url = url.replaceFirst("http://www.sh.10086.cn/ticket/", basePath);
			Map<String, String> params = new HashMap<String, String>();
			String pri = URLDecoder.decode(pKey, "UTF-8");
			String mac = CAUtil.doSign(tokenid, pri, "UTF-8");
			params.put("mac", mac);
			HttpResult code = HttpUtils.postUrlAsString(url, params);
			dbLogger.warnWithType(LogTypeConstant.LOG_TYPE_ORDER_PAY, "获取ticket_mobile用户tokenId:" + code.getResponse());
			Map jsonMap = JsonUtils.readJsonToMap(code.getResponse());
			String resCode = (String) jsonMap.get("resCode");
			if(StringUtils.equals(resCode,"0000")){
				Map<String,String> userMap = new HashMap<String, String>();
				Map cusMap = (Map) jsonMap.get("customerBasic");
				userMap.put("userId", cusMap.get("customerSeq")==null?"":cusMap.get("customerSeq").toString());
				userMap.put("username", cusMap.get("loginName")==null?"":cusMap.get("loginName").toString());
				userMap.put("mobile", cusMap.get("mobile")==null?"":cusMap.get("mobile").toString());
				return userMap;
			}
		} catch (Exception e) {
			dbLogger.warnWithType(LogTypeConstant.LOG_TYPE_ORDER_PAY, "获取ticket_mobile用户失败tokenId:" + tokenid + "," + e);
		}
		return null;
	}

}
