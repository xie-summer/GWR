package com.gewara.pay;

import java.util.HashMap;
import java.util.Map;

import com.gewara.model.pay.Charge;
import com.gewara.model.pay.GewaOrder;
import com.gewara.util.HttpResult;
import com.gewara.util.HttpUtils;
import com.gewara.util.StringUtil;

public class TelecomPayUtil {
	
	public static final String GOHONGLIANURL = "http://211.136.153.24/web24/Pay/GWLRsub.asp";
	public static final String QUERYURL = "http://211.136.153.24/web24/Pay/GWLFind.asp";
	public static final String KEY = "sksdwes12dfxfegv";
	public static final String OBJID = "0210323373";
	
	//一下为支付时的跳转配置
	public static final String PAY_KEY = "oewv5awmw8pxfadc";
	public static final String PAY_GOHONGLIANURL = "http://211.136.153.24/web24/Pay/GWLShRsub.asp";
	public static final String PAY_OBJID = "0210328824";
	public static final String PAY_QUERYURL = "http://211.136.153.24/web24/Pay/GWLShFind.asp";
	
	public static final String PAY_MOBILE_GOHONGLIANURL = "http://211.136.153.24/web24/MPay/GWLShRsub.asp";
	public static final String WEB_ID = "02000018202";
	public static final String PAY_MOBILE_KEY = "khn91ke04hanc88s";
	public static final String PAY_MOBILE_FINDURL = "http://211.136.153.24/web24/MPay/GWLShFind.asp";
	
	public static Map<String, String> getNetPayParams(GewaOrder order){
		Map<String, String> params = new HashMap<String,String>();
		params.put("objid",PAY_OBJID);
		params.put("money",order.getDue() + "");
		params.put("transid",PAY_OBJID+order.getTradeNo());
		params.put("username",order.getMemberid() + "");
		StringBuilder sb = new StringBuilder();
		sb.append("objid="+PAY_OBJID+"&");
		sb.append("money="+order.getDue() + "&");
		sb.append("transid="+ PAY_OBJID+order.getTradeNo()+"&");
		sb.append("username="+order.getMemberid()+"&");
		String sign = StringUtil.md5(sb.toString()+"key="+PAY_KEY);
		params.put("phonemode","3");
		params.put("sign",sign);
		params.put("payurl", PAY_GOHONGLIANURL);
		params.put("submitMethod", "post");
		return params;
	}
	
	public static Map<String,String> getNetMobilePayParams(GewaOrder order){
		Map<String, String> params = new HashMap<String,String>();
		params.put("WebId",WEB_ID);
		params.put("TransactionID",WEB_ID + order.getTradeNo());
		params.put("Fee",order.getDue() + "");
		params.put("TransactionName","");
		String sign = StringUtil.md5("WebId="+ WEB_ID + "&TransactionID=" + WEB_ID + order.getTradeNo() + "&Fee=" +order.getDue() + "&key="+PAY_MOBILE_KEY);
		params.put("sign",sign);
		params.put("payurl", PAY_MOBILE_GOHONGLIANURL);
		params.put("submitMethod", "post");
		return params;
	}
	
	public static String getChargePayUrl(Charge charge) {
		/**
		 * 
		username		用户名，例如CP网站上的用户登录名（建议使用字母或数字ID，中文需要GB2312编码传递，MD5加密时按中文组合字符串加密）
		objid			产品ID，申报业务时分配，10位数字
		money		产品价格，申报业务时确定，单位元
		transid		合作方生成的交易流水号（规则：objid+10到16位数字或字母，唯一性）
		phonemode	仅固话支付=3，仅手机支付=4（可选参数），参数不带则让用户选择
		sign			验证码（32位字母小写，MD5加密，key值另行分配）
		规则：sign=MD5(objid=&money=&transid=&username=&key=)
		 */
		StringBuilder params = new StringBuilder();
		params.append("objid="+OBJID+"&");
		params.append("money="+charge.getTotalfee() + "&");
		params.append("transid="+ OBJID+charge.getTradeNo()+"&");
		params.append("username="+charge.getMemberid()+"&");
		String sign = StringUtil.md5(params.toString()+"key="+KEY);
		params.append("phonemode="+charge.getPaybank()+"&");
		params.append("sign="+sign);
		String parameter = GOHONGLIANURL + "?" + params.toString();
		return parameter;
	}
	
	public static String getQueryOrderResult(String money, String transid,String objid,String key,String queryUrl){
		StringBuilder params = new StringBuilder();
		params.append("objid=");
		params.append(objid).append("&money=").append(money);
		params.append("&transid=").append(transid);
		String sign = StringUtil.md5(params.toString() + "&key=" + key);
		params.append("&sign=").append(sign);
		HttpResult code = HttpUtils.getUrlAsString(queryUrl + "?" + params.toString());
		if(code.isSuccess()){
			return code.getResponse();
		}
		return "fail";
	}
	
	public static String getTelecomMobilePayQueryOrderResult(String money, String transid){
		StringBuilder params = new StringBuilder();
		params.append("WebId=");
		params.append(WEB_ID).append("&Fee=").append(money);
		params.append("&TransactionID=").append(transid);
		String sign = StringUtil.md5(params.toString() + "&key=" + PAY_MOBILE_KEY);
		params.append("&Sign=").append(sign);
		HttpResult code = HttpUtils.getUrlAsString(PAY_MOBILE_FINDURL + "?" + params.toString());
		if(code.isSuccess()){
			return code.getResponse();
		}
		return "fail";
	}
	
	public static String sign(Map<String, String[]> requestMap){
		StringBuilder sb = new StringBuilder("sys=");
		sb.append(requestMap.get("sys") != null ? requestMap.get("sys")[0]:"");
		sb.append("&objid=").append(requestMap.get("objid") != null ? requestMap.get("objid")[0]:"");
		sb.append("&money=").append(requestMap.get("money") != null ? requestMap.get("money")[0]:"");
		sb.append("&transid=").append(requestMap.get("transid") != null ? requestMap.get("transid")[0]:"");
		sb.append("&username=").append(requestMap.get("username") != null ? requestMap.get("username")[0]:"");
		sb.append("&msg=").append(requestMap.get("msg") != null ? requestMap.get("msg")[0]:"");
		sb.append("&payphone=").append(requestMap.get("payphone") != null ? requestMap.get("payphone")[0]:"");
		sb.append("&key=").append(KEY);
		return StringUtil.md5(sb.toString());
	}
	
	public static String paySign(Map<String, String[]> requestMap){
		StringBuilder sb = new StringBuilder("sys=");
		sb.append(requestMap.get("sys") != null ? requestMap.get("sys")[0]:"");
		sb.append("&objid=").append(requestMap.get("objid") != null ? requestMap.get("objid")[0]:"");
		sb.append("&money=").append(requestMap.get("money") != null ? requestMap.get("money")[0]:"");
		sb.append("&transid=").append(requestMap.get("transid") != null ? requestMap.get("transid")[0]:"");
		sb.append("&username=").append(requestMap.get("username") != null ? requestMap.get("username")[0]:"");
		sb.append("&msg=").append(requestMap.get("msg") != null ? requestMap.get("msg")[0]:"");
		sb.append("&payphone=").append(requestMap.get("payphone") != null ? requestMap.get("payphone")[0]:"");
		sb.append("&key=").append(PAY_KEY);
		return StringUtil.md5(sb.toString());
	}

}
