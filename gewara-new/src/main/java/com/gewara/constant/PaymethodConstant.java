package com.gewara.constant;

import java.io.Serializable;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.map.UnmodifiableMap;
import org.apache.commons.lang.StringUtils;

public abstract class PaymethodConstant implements Serializable {
	private static final long serialVersionUID = -8289964065497333210L;
	
	//格瓦拉
	public static final String PAYMETHOD_GEWAPAY = "gewaPay";						//用户余额
	public static final String PAYMETHOD_SYSPAY = "sysPay";							//系统用户
	public static final String PAYMETHOD_ELECARDPAY = "elecardPay";					//全部使用券
	public static final String PAYMETHOD_GEWARA_OFFLINEPAY = "offlinePay";			//后台线下支付
	//充值
	public static final String PAYMETHOD_CHARGECARD = "ccardPay";					//充值卡，只用来充值
	public static final String PAYMETHOD_LAKALA = "lakalaPay";						//拉卡拉 ，只用来充值
	public static final String PAYMETHOD_ABCBANKPAY = "abcPay";						//农行合作充值，只用来充值
	public static final String PAYMETHOD_WCANPAY = "wcanPay";						//微能科技合作充值，只用来充值
	//支付----合作伙伴
	public static final String PAYMETHOD_PARTNERPAY = "partnerPay";					//合作伙伴
	public static final String PAYMETHOD_OKCARDPAY = "okcardPay";					//联华OK卡
	public static final String PAYMETHOD_SPSDOPAY1 = "spsdoPay";					//盛大即时到账支付
	//支付----第三方
	public static final String PAYMETHOD_ALIPAY = "directPay";						//支付宝PC端
	public static final String PAYMETHOD_PNRPAY = "pnrPay";							//汇付天下PC端
	public static final String PAYMETHOD_CMPAY = "cmPay";							//移动手机支付PC端
	public static final String PAYMETHOD_TEMPUSPAY = "tempusPay";					//腾付通PC端
	public static final String PAYMETHOD_SPSDOPAY2 = "spsdo2Pay";					//盛付通PC端
	public static final String PAYMETHOD_CHINAPAY1 = "chinaPay";					//银联
	public static final String PAYMETHOD_CHINAPAY2 = "china2Pay";					//ChinapayPC端
	public static final String PAYMETHOD_CHINAPAYSRCB = "srcbPay";					//Chinapay农商行--->50000547
	
	public static final String PAYMETHOD_UNIONPAY = "unionPay";						//unionPay
	public static final String PAYMETHOD_UNIONPAY_JS = "unionPay_js";				//Unionpay江苏PC端
	public static final String PAYMETHOD_UNIONPAY_ACTIVITY = "unionPay_activity";	//unionPay活动
	public static final String PAYMETHOD_UNIONPAY_ACTIVITY_JS = "unionPay_activity_js";//unionPay江苏活动
	public static final String PAYMETHOD_UNIONPAY_ZJ = "unionPay_zj";		//浙江地区专用
	public static final String PAYMETHOD_UNIONPAY_SZ = "unionPay_sz";		//深圳地区专用
	public static final String PAYMETHOD_UNIONPAY_BJ = "unionPay_bj";		//北京地区专用
	public static final String PAYMETHOD_UNIONPAY_GZ = "unionPay_gz";		//广州地区专用
	
	public static final String PAYMETHOD_UNIONPAYFAST = "unionPayFast";				//unionPay V2.0.0 版本支付
	public static final String PAYMETHOD_UNIONPAYFAST_ACTIVITY_JS = "unionPayFast_activity_js";//unionPay version 2.0.0版本 unionPay江苏活动
	public static final String PAYMETHOD_UNIONPAYFAST_ACTIVITY_BJ = "unionPayFast_activity_bj";//unionPay version 2.0.0版本 unionPay北京活动
	public static final String PAYMETHOD_UNIONPAYFAST_ACTIVITY_SZ = "unionPayFast_activity_sz";//unionPay version 2.0.0版本深圳地区活动
	public static final String PAYMETHOD_UNIONPAYFAST_ACTIVITY_GZ = "unionPayFast_activity_gz";//unionPay version 2.0.0版本广州地区活动
	public static final String PAYMETHOD_UNIONPAYFAST_ACTIVITY_ZJ = "unionPayFast_activity_zj";//unionPay version 2.0.0版本浙江地区活动
	
	//支付----直连
	public static final String PAYMETHOD_BCPAY = "bcPay";							//交行直连PC端
	public static final String PAYMETHOD_SPDPAY = "spdPay";							//浦发直连PC端
	public static final String PAYMETHOD_CMBPAY = "cmbPay";							//招行直连PC端
	public static final String PAYMETHOD_GDBPAY = "gdbPay";							//广发直连PC端
	public static final String PAYMETHOD_PSBCPAY = "psbcPay";						//邮储直连PC端
	public static final String PAYMETHOD_HZBANKPAY = "hzbankPay";					//杭州银行直连
	public static final String PAYMETHOD_CCBPOSPAY = "ccbposPay";					//建行直连PC端-信用卡
	public static final String PAYMETHOD_JSBCHINA = "jsbChina";						//江苏银行直连PC端-信用卡
	public static final String PAYMETHOD_SPDPAY_ACTIVITY = "spdPay_activity";		//浦发直连PC端-活动
	public static final String PAYMETHOD_BOCPAY = "bocPay";							//中国银行直连PC端
	public static final String PAYMETHOD_BOCWAPPAY = "bocWapPay";					//中国银行直连WAP端
	public static final String PAYMETHOD_BOCAGRMTPAY = "bocAgrmtPay";				//中国银行协议支付
	//支付----话费
	public static final String PAYMETHOD_UMPAY = "umPay";							//移动话费支付(联动优势)
	public static final String PAYMETHOD_UMPAY_SH = "umPay_sh";						//移动话费支付(联动优势) 上海地区
	public static final String PAYMETHOD_TELECOM= "telecomPay";						//电信固话话费支付，包括充值
	public static final String PAYMETHOD_MOBILE_TELECOM= "telecomMobilePay";		//电信手机话费支付
	//支付----行业卡
	public static final String PAYMETHOD_YAGAO = "yagaoPay";						//雅高
	public static final String PAYMETHOD_ONETOWN = "onetownPay";					//一城卡支付(新华传媒)
	//支付----手机端（直连 + 第三方）
	public static final String PAYMETHOD_ALIWAPPAY = "aliwapPay";					//支付宝手机端-WAP支付
	public static final String PAYMETHOD_CMWAPPAY = "cmwapPay";						//移动手机支付手机端-WAP支付
	public static final String PAYMETHOD_CMBWAPPAY = "cmbwapPay";					//招行直连手机端
	public static final String PAYMETHOD_CMBWAPSTOREPAY = "cmbwapStorePay";			//招行直连手机端CMSTORE
	public static final String PAYMETHOD_SPDWAPPAY = "spdWapPay";					//浦发直连手机端-WAP
	public static final String PAYMETHOD_CMSMARTPAY = "cmSmartPay";					//移动手机支付安卓版
	public static final String PAYMETHOD_SPDWAPPAY_ACTIVITY = "spdWapPay_activity";	//浦发直连手机端-活动
	public static final String PAYMETHOD_CHINASMARTMOBILEPAY = "chinaSmartMobilePay";//银联智能手机支付
	public static final String PAYMETHOD_CHINASMARTJSPAY = "chinaSmartJsPay";		//银联智能手机支付-江苏
	public static final String PAYMETHOD_ALISMARTMOBILEPAY = "aliSmartMobilePay";	//支付宝手机端-安全支付
	public static final String PAYMETHOD_HZWAPPAY = "hzwapPay";						//杭州银行WAP
	public static final String PAYMETHOD_YEEPAY = "yeePay";       					//易宝支付
	public static final String PAYMETHOD_PAYECO_DNA = "payecoDNAPay";       		// 易联DNA支付
	public static final String PAYMETHOD_MEMBERCARDPAY = "memberCardPay";       	// 会员卡支付
	public static final String PAYMETHOD_ICBCPAY = "icbcPay";       // 工商银行直连支付PC端
	public static final String PAYMETHOD_NJCBPAY = "njcbPay";       // 南京银行直连支付PC端
	public static final String PAYMETHOD_ABCHINAPAY = "abchinaPay";       // 农业银行直连支付PC端
	
	public static final String PAYMETHOD_WXAPPTENPAY = "wxAppTenPay";       //财付通微信支付（App间支付）
	public static final String PAYMETHOD_WXSCANTENPAY = "wxScanTenPay";       //财付通微信支付（WEB扫码）
	public static final String PAYMETHOD_WXWCPAY = "wxWCPay";				//微信公众号支付
	public static final String PAYMETHOD_CCBWAPPAY = "ccbWapPay";	//建行手机wap支付
	public static final String PAYMETHOD_ONECLICKTENPAY = "oneClickTenPay";	//财付通移动终端一键支付
	public static final String PAYMETHOD_BESTPAY = "bestPay";	//翼支付
	public static final String PAYMETHOD_BFBWAPPAY = "bfbWapPay";	//百度钱包wap支付
	public static final String PAYMETHOD_BFBPAY = "bfbPay";		//百度钱包支付
	
	//支付----以下支付方式不在使用
	public static final String PAYMETHOD_SDOPAY = "sdoPay";							//盛大积分+现金
	public static final String PAYMETHOD_TENPAY = "tenPay";							//财富通
	public static final String PAYMETHOD_IPSPAY= "ipsPay";							//环讯PC端-信用卡支付
	public static final String PAYMETHOD_BCWAPPAY = "bcwapPay";						//交通WAP银行
	public static final String PAYMETHOD_ALLINPAY = "allinPay";						//通联支付
	public static final String PAYMETHOD_ALIBANKPAY = "alibankPay";					//支付宝手机银行
	public static final String PAYMETHOD_HANDWAPPAY = "handwapPay";					//瀚银手机
	public static final String PAYMETHOD_HANDWEBPAY = "handwebPay";					//瀚银手机
	public static final String PAYMETHOD_PNRFASTPAY = "pnrfastPay";					//汇付快捷支付 --华夏银行信用
	public static final String PAYMETHOD_PNRFASTPAY2 = "pnrfastPay2";				//汇付快捷支付2--建设银行信用
	public static final String PAYMETHOD_PNRFASTABCPAY = "pnrfastabcPay";			//汇付快捷支付--农业银行信用卡
	
	//已废弃
	//public static final String PAYMETHOD_HAOBAIPAY = "haobaiPay";					//号百手机端-客户端
	
	public static final List<String> PAYMETHOD_LIST = 
			Arrays.asList(/*废弃:PAYMETHOD_HAOBAIPAY,*/
					PAYMETHOD_GEWAPAY, PAYMETHOD_CHARGECARD, PAYMETHOD_PNRPAY, 
					PAYMETHOD_ALIPAY, PAYMETHOD_ALIWAPPAY, PAYMETHOD_ALIBANKPAY, PAYMETHOD_LAKALA, 
					PAYMETHOD_SDOPAY, PAYMETHOD_CHINAPAY1, PAYMETHOD_CHINAPAY2, PAYMETHOD_CHINAPAYSRCB, 
					PAYMETHOD_OKCARDPAY, PAYMETHOD_TENPAY, PAYMETHOD_PARTNERPAY, PAYMETHOD_SPSDOPAY1, PAYMETHOD_SPSDOPAY2, 
					PAYMETHOD_CMPAY, PAYMETHOD_IPSPAY, PAYMETHOD_YAGAO, PAYMETHOD_ONETOWN, PAYMETHOD_HANDWEBPAY, 
					PAYMETHOD_HANDWAPPAY, PAYMETHOD_CMBPAY, PAYMETHOD_CMBWAPPAY, PAYMETHOD_BCPAY, PAYMETHOD_BCWAPPAY, 
					PAYMETHOD_GDBPAY, PAYMETHOD_ALLINPAY, PAYMETHOD_ELECARDPAY, PAYMETHOD_SYSPAY, PAYMETHOD_CMWAPPAY, 
					PAYMETHOD_CHINASMARTMOBILEPAY, PAYMETHOD_CHINASMARTJSPAY, PAYMETHOD_ALISMARTMOBILEPAY, PAYMETHOD_UMPAY,PAYMETHOD_UMPAY_SH, 
					PAYMETHOD_SPDPAY, PAYMETHOD_SPDPAY_ACTIVITY,PAYMETHOD_PSBCPAY, PAYMETHOD_SPDWAPPAY, PAYMETHOD_SPDWAPPAY_ACTIVITY, PAYMETHOD_HZBANKPAY, PAYMETHOD_ABCBANKPAY, 
					PAYMETHOD_WCANPAY,PAYMETHOD_UNIONPAY, PAYMETHOD_UNIONPAY_JS, PAYMETHOD_UNIONPAY_ACTIVITY,PAYMETHOD_UNIONPAY_ACTIVITY_JS,PAYMETHOD_UNIONPAY_ZJ,PAYMETHOD_UNIONPAY_SZ,PAYMETHOD_UNIONPAY_BJ,PAYMETHOD_UNIONPAY_GZ,
					PAYMETHOD_TELECOM,PAYMETHOD_MOBILE_TELECOM,
					PAYMETHOD_JSBCHINA, PAYMETHOD_TEMPUSPAY, PAYMETHOD_CCBPOSPAY,PAYMETHOD_PNRFASTPAY,PAYMETHOD_PNRFASTPAY2,PAYMETHOD_YEEPAY,
					PAYMETHOD_CMSMARTPAY, PAYMETHOD_PNRFASTABCPAY,PAYMETHOD_UNIONPAYFAST,PAYMETHOD_UNIONPAYFAST_ACTIVITY_JS,PAYMETHOD_UNIONPAYFAST_ACTIVITY_BJ,
					PAYMETHOD_BOCPAY,PAYMETHOD_BOCWAPPAY,PAYMETHOD_BOCAGRMTPAY, PAYMETHOD_HZWAPPAY, PAYMETHOD_PAYECO_DNA, PAYMETHOD_MEMBERCARDPAY, PAYMETHOD_UNIONPAYFAST_ACTIVITY_SZ,
					PAYMETHOD_UNIONPAYFAST_ACTIVITY_GZ,PAYMETHOD_UNIONPAYFAST_ACTIVITY_ZJ,PAYMETHOD_ICBCPAY, PAYMETHOD_CMBWAPSTOREPAY,PAYMETHOD_NJCBPAY,PAYMETHOD_ABCHINAPAY,PAYMETHOD_WXAPPTENPAY,PAYMETHOD_WXSCANTENPAY,
					PAYMETHOD_CCBWAPPAY, PAYMETHOD_WXWCPAY, PAYMETHOD_ONECLICKTENPAY, PAYMETHOD_BESTPAY, PAYMETHOD_BFBWAPPAY, PAYMETHOD_GEWARA_OFFLINEPAY, PAYMETHOD_BFBPAY);
		
	public static final List<String> MOBILE_PAYMETHOD_LIST = Arrays.asList(PAYMETHOD_ALIWAPPAY, PAYMETHOD_CMWAPPAY, PAYMETHOD_CMBWAPPAY, PAYMETHOD_CMBWAPSTOREPAY, PAYMETHOD_SPDWAPPAY, 
			PAYMETHOD_CMSMARTPAY, PAYMETHOD_SPDWAPPAY_ACTIVITY, PAYMETHOD_CHINASMARTMOBILEPAY, PAYMETHOD_CHINASMARTJSPAY, PAYMETHOD_BOCWAPPAY, 
			PAYMETHOD_ALISMARTMOBILEPAY, PAYMETHOD_HZWAPPAY ,PAYMETHOD_WXAPPTENPAY,PAYMETHOD_WXSCANTENPAY, PAYMETHOD_CCBWAPPAY, PAYMETHOD_WXWCPAY, PAYMETHOD_ONECLICKTENPAY, PAYMETHOD_BFBWAPPAY);
	private static Map<String, String> payTextMap;	
	static{
		Map<String, String> tmp = new LinkedHashMap<String, String>();
		tmp.put(PAYMETHOD_SYSPAY, "系统");
		tmp.put(PAYMETHOD_GEWAPAY, "格瓦余额");
		tmp.put(PAYMETHOD_ELECARDPAY, "电子券");
		tmp.put(PAYMETHOD_GEWARA_OFFLINEPAY, "后台下线支付");
		tmp.put(PAYMETHOD_CHARGECARD, "格瓦充值卡");
		tmp.put(PAYMETHOD_LAKALA, "拉卡拉");
		tmp.put(PAYMETHOD_ABCBANKPAY,"农行合作");
		tmp.put(PAYMETHOD_WCANPAY,"微能科技积分兑换");
		
		tmp.put(PAYMETHOD_PNRPAY, "汇付天下PC端");
		tmp.put(PAYMETHOD_ALIPAY, "支付宝PC端");
		tmp.put(PAYMETHOD_CMPAY, "移动手机支付PC端");
		tmp.put(PAYMETHOD_CHINAPAY1, "银联便民");
		tmp.put(PAYMETHOD_CHINAPAY2, "ChinapayPC端");
		tmp.put(PAYMETHOD_SPSDOPAY2, "盛付通PC端");
		tmp.put(PAYMETHOD_PAYECO_DNA, "易联DNA支付");
		
		tmp.put(PAYMETHOD_CMBPAY, "招行直连PC端");
		tmp.put(PAYMETHOD_CMBWAPPAY, "招行直连手机端");
		tmp.put(PAYMETHOD_CMBWAPSTOREPAY, "招行手机端-STORE");
		tmp.put(PAYMETHOD_BCPAY, "交行直连PC端");
		tmp.put(PAYMETHOD_GDBPAY, "广发直连PC端");
		tmp.put(PAYMETHOD_BOCPAY, "中国银行直连PC端");
		tmp.put(PAYMETHOD_BOCAGRMTPAY, "中国银行协议支付");
		tmp.put(PAYMETHOD_SPDPAY, "浦发直连PC端");
		tmp.put(PAYMETHOD_SPDPAY_ACTIVITY, "浦发直连PC端-活动");
		tmp.put(PAYMETHOD_SPDWAPPAY_ACTIVITY, "浦发直连手机端-活动");
		tmp.put(PAYMETHOD_PSBCPAY, "邮储直连PC端");
		tmp.put(PAYMETHOD_HZBANKPAY, "杭州银行");
		tmp.put(PAYMETHOD_HZWAPPAY, "杭州银行WAP");
		tmp.put(PAYMETHOD_JSBCHINA, "江苏银行直连PC端-信用卡");
		tmp.put(PAYMETHOD_TEMPUSPAY, "腾付通PC端");
		tmp.put(PAYMETHOD_YEEPAY, "易宝支付PC端");
		tmp.put(PAYMETHOD_CCBPOSPAY, "建行直连PC端-信用卡");
		tmp.put(PAYMETHOD_ICBCPAY, "工商银行直连支付PC端");	
		tmp.put(PAYMETHOD_NJCBPAY, "南京银行直连支付PC端");
		tmp.put(PAYMETHOD_ABCHINAPAY, "农业银行直连支付PC端");	
		
		tmp.put(PAYMETHOD_ALIWAPPAY, "支付宝手机端-WAP支付");
		tmp.put(PAYMETHOD_BOCWAPPAY, "中国银行直连WAP端");
		tmp.put(PAYMETHOD_CMWAPPAY, "移动手机支付手机端-WAP支付");
		tmp.put(PAYMETHOD_SPDWAPPAY, "浦发直连手机端-WAP");
		tmp.put(PAYMETHOD_CHINASMARTMOBILEPAY, "银联手机在线支付");
		tmp.put(PAYMETHOD_CHINASMARTJSPAY, "江苏银联手机端-江苏银商收单");
		tmp.put(PAYMETHOD_ALISMARTMOBILEPAY, "支付宝手机端-安全支付");
		tmp.put(PAYMETHOD_CMSMARTPAY, "移动手机支付安卓版");
		
		tmp.put(PAYMETHOD_UNIONPAY, "unionPay银联支付");
		tmp.put(PAYMETHOD_UNIONPAY_JS, "unionPay江苏");
		tmp.put(PAYMETHOD_UNIONPAY_ACTIVITY, "unionPay活动");
		tmp.put(PAYMETHOD_UNIONPAY_ACTIVITY_JS, "unionPay江苏活动");
		tmp.put(PAYMETHOD_UNIONPAY_ZJ, "unionPay浙江");
		tmp.put(PAYMETHOD_UNIONPAY_SZ, "unionPay深圳");
		tmp.put(PAYMETHOD_UNIONPAY_BJ, "unionPay北京");
		tmp.put(PAYMETHOD_UNIONPAY_GZ, "unionPay广州");
		
		tmp.put(PAYMETHOD_UNIONPAYFAST, "unionPayFast银联快捷支付");
		tmp.put(PAYMETHOD_UNIONPAYFAST_ACTIVITY_JS, "unionPayFast江苏活动");	
		tmp.put(PAYMETHOD_UNIONPAYFAST_ACTIVITY_BJ, "银联认证2.0北京活动");
		tmp.put(PAYMETHOD_UNIONPAYFAST_ACTIVITY_SZ, "银联认证2.0深圳活动");
		tmp.put(PAYMETHOD_UNIONPAYFAST_ACTIVITY_GZ, "银联认证2.0广州活动");
		tmp.put(PAYMETHOD_UNIONPAYFAST_ACTIVITY_ZJ, "银联认证2.0浙江");
		
		
		tmp.put(PAYMETHOD_PNRFASTPAY, "汇付快捷支付--华夏信用卡");
		tmp.put(PAYMETHOD_PNRFASTPAY2, "汇付快捷支付--建行信用卡");
		tmp.put(PAYMETHOD_PNRFASTABCPAY, "汇付快捷支付--农行信用卡");
		
		
		tmp.put(PAYMETHOD_TELECOM, "电信固话话费支付");
		tmp.put(PAYMETHOD_MOBILE_TELECOM, "电信手机话费支付");
		tmp.put(PAYMETHOD_UMPAY, "移动话费支付(联动优势)");
		tmp.put(PAYMETHOD_UMPAY_SH, "移动话费支付(联动优势)_上海");
		tmp.put(PAYMETHOD_YAGAO, "雅高卡支付(艾登瑞德)");
		tmp.put(PAYMETHOD_ONETOWN, "一城卡支付(新华传媒)");
		
		tmp.put(PAYMETHOD_PARTNERPAY, "合作商");
		tmp.put(PAYMETHOD_OKCARDPAY, "联华OK");
		tmp.put(PAYMETHOD_SPSDOPAY1, "盛大合作");
		tmp.put(PAYMETHOD_CHINAPAYSRCB, "Chinapay农商行");
		tmp.put(PAYMETHOD_MEMBERCARDPAY, "会员卡支付");
		
		tmp.put(PAYMETHOD_WXAPPTENPAY, "财付通微信支付（App间支付）");
		tmp.put(PAYMETHOD_WXSCANTENPAY, "财付通微信支付（WEB扫码）");
		tmp.put(PAYMETHOD_WXWCPAY, "微信公众号支付");
		tmp.put(PAYMETHOD_CCBWAPPAY, "建行手机wap支付");
		tmp.put(PAYMETHOD_ONECLICKTENPAY, "财付通移动终端一键支付");
		tmp.put(PAYMETHOD_BESTPAY, "翼支付");
		tmp.put(PAYMETHOD_BFBWAPPAY, "百度钱包wap支付");
		tmp.put(PAYMETHOD_BFBPAY, "百度钱包支付");
		
		//tmp.put(PAYMETHOD_HAOBAIPAY, "号百手机端-客户端");
		//不在使用的支付方式
		tmp.put(PAYMETHOD_ALIBANKPAY, "支付宝招商银行WAP");
		tmp.put(PAYMETHOD_SDOPAY, "盛大积分");
		tmp.put(PAYMETHOD_BCWAPPAY, "交通WAP");
		tmp.put(PAYMETHOD_ALLINPAY, "通联");
		tmp.put(PAYMETHOD_TENPAY, "财付通");
		tmp.put(PAYMETHOD_HANDWEBPAY, "翰银WEB");
		tmp.put(PAYMETHOD_HANDWAPPAY, "翰银WAP");
		tmp.put(PAYMETHOD_IPSPAY, "环讯PC端-信用卡支付");
		
		
		payTextMap = UnmodifiableMap.decorate(tmp);
	}
	
	public static String getPaymethodText(String paymethod){
		if(payTextMap.get(paymethod)!=null) return payTextMap.get(paymethod);
		if(StringUtils.equals("card", paymethod)) return "兑换券";
		return "未知";
	}
	public static final boolean isValidPayMethod(String paymethod){
		return StringUtils.isNotBlank(paymethod) && PaymethodConstant.PAYMETHOD_LIST.contains(paymethod);
	}
	public static Map<String, String> getPayTextMap(){
		return payTextMap;
	}
	
	public static List<String> getMobilePayList(){
		String[] pays = new String[]{PAYMETHOD_ALIWAPPAY,PAYMETHOD_CMWAPPAY,PAYMETHOD_CMBWAPPAY,PAYMETHOD_SPDWAPPAY,
				PAYMETHOD_SPDWAPPAY_ACTIVITY,PAYMETHOD_CHINASMARTMOBILEPAY,PAYMETHOD_CHINASMARTMOBILEPAY,
				PAYMETHOD_CHINASMARTJSPAY,PAYMETHOD_ALISMARTMOBILEPAY};
		return Arrays.asList(pays);
	}
}
