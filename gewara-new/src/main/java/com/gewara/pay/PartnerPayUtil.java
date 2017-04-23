package com.gewara.pay;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.lang.StringUtils;

import com.gewara.Config;
import com.gewara.constant.sys.LogTypeConstant;
import com.gewara.constant.ticket.PartnerConstant;
import com.gewara.model.api.ApiUser;
import com.gewara.model.pay.GewaOrder;
import com.gewara.model.pay.TicketOrder;
import com.gewara.util.DateUtil;
import com.gewara.util.GewaLogger;
import com.gewara.util.HttpResult;
import com.gewara.util.HttpUtils;
import com.gewara.util.JsonUtils;
import com.gewara.util.LoggerUtils;
import com.gewara.util.StringUtil;

public class PartnerPayUtil {
	public static final String SHORT_NOTIFY_URL = "pay/partnerPayNotify.xhtml";
	public static final String NOTIFY_URL = "http://manage.gewara.com/pay/partnerPayNotify.xhtml";
	public static final String RETURN_URL = "http://www.gewara.com/pay/partnerPayReturn.xhtml";
	public static final String CHECK_URL = "http://www.gewara.com/pay/partnerPayCheck.xhtml";//反查为空的合作商校验地址
	
	public static final String qqPriKey = "0915tg()!xkl";
	
	private static final transient GewaLogger dbLogger = LoggerUtils.getLogger(PartnerPayUtil.class, Config.getServerIp(), Config.SYSTEMID);
	public static String getOrderPayUrl(GewaOrder order, ApiUser partner){
		StringBuilder params = new StringBuilder(partner.getAddOrderUrl());
		try {
			params.append("?tradeno=" + order.getTradeNo());
			params.append("&ordertitle=" + URLEncoder.encode(order.getOrdertitle(), "utf-8"));
			params.append("&amount=" + order.getDue());
			params.append("&mobile=" + order.getMobile());
			params.append("&description=" + URLEncoder.encode(""+JsonUtils.readJsonToMap(order.getDescription2()), "utf-8"));
			params.append("&addtime=" + DateUtil.format(order.getAddtime(), "yyyyMMddHHmmss"));
			params.append("&checkvalue=" + getCheckValue(partner.getPrivatekey(), order.getTradeNo(), order.getDue()+"", order.getMobile()));
			String notify = StringUtils.isNotBlank(partner.getNotifyurl())? partner.getNotifyurl(): NOTIFY_URL;
			params.append("&notifyUrl=" + URLEncoder.encode(notify,"UTF-8"));
			//params.append("&returnUrl=" + URLEncoder.encode(RETURN_URL,"UTF-8"));
			if(PartnerConstant.PARTNER_ONLINE.equals(order.getPartnerid())){
				params.append("&mpid=" + ((TicketOrder)order).getMpid());
				params.append("&quantity=" + order.getQuantity());
			}
		} catch (UnsupportedEncodingException e) {
			dbLogger.errorWithType(LogTypeConstant.LOG_TYPE_ORDER_PAY, "", e);
		}
		return params.toString();
	}
	public static Map<String, String> getNetPayParams(GewaOrder order, ApiUser partner){
		Map<String, String> params = new HashMap<String, String>();
		params.put("payurl", partner.getAddOrderUrl());
		params.put("tradeno", order.getTradeNo());
		params.put("ordertitle", order.getOrdertitle());
		params.put("amount", ""+order.getDue());
		params.put("mobile", order.getMobile());
		params.put("quantity", ""+order.getQuantity());
		params.put("description", ""+JsonUtils.readJsonToMap(order.getDescription2()));
		params.put("addtime", DateUtil.format(order.getAddtime(), "yyyyMMddHHmmss"));
		String checkvalue = getCheckValue(partner.getPrivatekey(), order.getTradeNo(), order.getDue()+"", order.getMobile());
		params.put("checkvalue", checkvalue);
		String notify = StringUtils.isNotBlank(partner.getNotifyurl())? partner.getNotifyurl(): NOTIFY_URL;
		params.put("notifyUrl", notify);
		//params.put("returnUrl", RETURN_URL);
		if(PartnerConstant.PARTNER_ONLINE.equals(order.getPartnerid())){
			params.put("mpid", ""+((TicketOrder)order).getMpid());
		}else if(PartnerConstant.PARTNER_POINTPARK.equals(order.getPartnerid())){
			return PointParkUtil.getSign(params, order, partner);
		}else if(PartnerConstant.PARTNER_TAOBAO.equals(order.getPartnerid())){
			return TaoBaoUtil.getPayParams(order, partner);
		}else if(PartnerConstant.PARTNER_SAND.equals(order.getPartnerid())){
			return SandPayUtil.getPayParams(order,partner,notify);
		}else if(PartnerConstant.PARTNER_VERYCD.equals(order.getPartnerid())){
			return VeryCDUtil.getPayParams(order,partner, notify);
		}else if(PartnerConstant.PARTNER_12580.equals(order.getPartnerid())){
			return Pay12580Util.getPayParams(order, partner, notify);
		}else if(PartnerConstant.PARTNER_MOBILETICKET.equals(order.getPartnerid())){
			return MobileTicketUtil.getPayParams(order,partner,notify);
		}else if(PartnerConstant.PARTNER_CE9.equals(order.getPartnerid())){
			return Ce9Util.getPayParams(order, partner, notify);
		}else if(PartnerConstant.PARTNER_UNIONPAY.equals(order.getPartnerid())){
			return UnionpayUtil.getPayParams(order, partner, notify);
		}
		return params;
		
	}
	public static String getCheckValue(String privatekey, String... params) {
		return StringUtil.md5(StringUtils.join(params, "") + privatekey, "UTF-8");
	}
	/**
	 * 回查订单是否存在
	 * @param partner
	 * @param payseqno
	 * @param tradeno
	 * @param paidAmount
	 * @param checkvalue
	 * @return
	 */
	public static String validate(ApiUser partner, String tradeno, String payseqno, String paidAmount, String checkvalue) {
		if(StringUtils.equalsIgnoreCase(checkvalue, getCheckValue(partner.getPrivatekey(), tradeno, payseqno, paidAmount))){
			Map<String, String> params = new HashMap<String, String>();
			params.put("tradeno", tradeno);
			if(StringUtils.isNotBlank(partner.getQryurl())){
				params.put("paidAmount", paidAmount);
				if(StringUtils.isNotBlank(payseqno)) params.put("payseqno", payseqno);
				params.put("checkvalue", checkvalue);
				HttpResult result = HttpUtils.postUrlAsString(partner.getQryurl(), params);
				if(result.isSuccess()){
					if(StringUtils.contains(result.getResponse(), "paid")||StringUtils.contains(result.getResponse(), "success")){
						dbLogger.warn(partner.getQryurl() + "," + params + result.getResponse());
						return "success";
					}else{
						dbLogger.errorWithType(LogTypeConstant.LOG_TYPE_ORDER_PAY, partner.getQryurl() + "," + params + result.getResponse());
						return "query error:" + result.getResponse();
					}
				}else{
					return "http error:" + result.getStatus();
				}
			}else{
				HttpResult result = HttpUtils.postUrlAsString(CHECK_URL, params);
				if(result.isSuccess()){
					return result.getResponse();	
				}else{
					return "http error:" + result.getStatus();
				}
				
			}
		}
		return "checkvalue error";
	}
	
	public static String validateOpenApiPay(ApiUser partner, String tradeno, String payseqno, String paidAmount) {
		TreeMap<String, String> params = new TreeMap<String, String>();
		params.put("tradeno", tradeno);
		params.put("paidAmount", paidAmount);
		params.put("payseqno", payseqno);
		params.put("checkvalue", StringUtil.md5(tradeno+payseqno+paidAmount+partner.getPrivatekey()));
		HttpResult result = HttpUtils.postUrlAsString(partner.getQryurl(), params);
		if(StringUtils.contains(result.getResponse(), "paid")||StringUtils.contains(result.getResponse(), "success")){
			dbLogger.warn(partner.getQryurl() + "," + params + result.getResponse());
			return "success";
		}else{
			dbLogger.errorWithType(LogTypeConstant.LOG_TYPE_ORDER_PAY, partner.getQryurl() + "," + params + result.getResponse());
			return "query error:" + result.getResponse() + ", msg:" + result.getMsg();
		}
	}
	
	public static boolean isValidIp(String ip, ApiUser partner){
		return StringUtils.isBlank(partner.getPartnerip()) || 
				StringUtils.contains(partner.getPartnerip(), ip);
	}
	
}
