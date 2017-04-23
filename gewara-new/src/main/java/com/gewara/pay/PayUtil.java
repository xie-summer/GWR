package com.gewara.pay;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.Random;

import org.apache.commons.lang.StringUtils;

import com.gewara.constant.PaymethodConstant;
import com.gewara.model.pay.GewaOrder;
import com.gewara.util.DateUtil;
import com.gewara.util.StringUtil;

public class PayUtil {


	//PNR网银
	private static String pnrpayReturn;
	private static String pnrpayNotify;
	
	//支付宝回调
	private static String alipayReturn;
	private static String alipayNotify;

	//支付宝手机银行
	private static String alipayMobileNotify;
	private static String alipayMobileReturn;
	//支付宝客户端
	private static String alipaySmartNotify;
	
	//招行直连
	private static String cmbpayNotifyUrl;
	//交行直连
	private static String bcpayNotifyURL;
	private static String bcpayReturnUrl;
	
	private static String cmbwapPayReturnUrl;
	
	private static String gdbpayReturnUrl;
	private static String allinpayReturnUrl;
	
	//移动手机webwap
	private static String cmNotifyUrl;
	private static String cmReturnUrl;
	private static String cmwapNotifyUrl;
	private static String cmwapReturnUrl;
	
	//号百
	private static String haobaiPayUrl;
	private static String haobaiOrderCreate;
	
	//杭州银行
	private static String hzpayNotifyUrl;
	private static String hzpayReturnUrl;
	
	private static boolean initialized = false;
	public static synchronized void init(String propertyFile){
		if(initialized) return;
		Properties props = new Properties();
		try {
			props.load(ChinapayUtil.class.getClassLoader().getResourceAsStream(propertyFile));
		} catch (Exception e) {
			throw new IllegalArgumentException("property File Error!!!!", e);
		}
		pnrpayReturn = props.getProperty("pnrpayReturn");
		pnrpayNotify = props.getProperty("pnrpayNotify");
		
		alipayReturn = props.getProperty("alipayReturn");
		alipayNotify = props.getProperty("alipayNotify");
		alipayMobileNotify = props.getProperty("alipayMobileNotify");
		alipayMobileReturn = props.getProperty("alipayMobileReturn");
		alipaySmartNotify = props.getProperty("alipaySmartNotify");
		
		cmbpayNotifyUrl = props.getProperty("cmbpayNotifyUrl");
		bcpayNotifyURL = props.getProperty("bcpayNotifyURL");
		bcpayReturnUrl = props.getProperty("bcpayReturnUrl");
		
		cmbwapPayReturnUrl = props.getProperty("cmbwapPayReturnUrl");
		
		gdbpayReturnUrl = props.getProperty("gdbpayReturnUrl");
		allinpayReturnUrl = props.getProperty("allinpayReturnUrl");
		
		cmNotifyUrl = props.getProperty("cmNotifyUrl");
		cmReturnUrl = props.getProperty("cmReturnUrl");
		cmwapNotifyUrl = props.getProperty("cmwapNotifyUrl");
		cmwapReturnUrl = props.getProperty("cmwapReturnUrl");
		
		haobaiPayUrl = props.getProperty("haobaiPayUrl");
		haobaiOrderCreate = props.getProperty("haobaiOrderCreate");
		
		hzpayNotifyUrl = props.getProperty("hzpayNotifyUrl");
		hzpayReturnUrl = props.getProperty("hzpayReturnUrl");
		
		
		initialized = true;
		
	}
	private static final Random random = new Random();
	public static final String FLAG_TICKET = "1";
	public static final String FLAG_CHARGE = "2";
	public static final String FLAG_GOODS = "3";
	public static final String FLAG_SPORT = "4";
	public static final String FLAG_DRAMA = "5";
	public static final String FLAG_PUBSALE = "6";
	public static final String FLAG_GYM = "7";
	public static final String FLAG_GUARANTEE = "8";
	public static final String FLAG_MEMBER_CARD = "9";
	
	public static final String getChargeTradeNo() {
		return FLAG_CHARGE + DateUtil.format(new Date(), "yyMMddHHmmss") + StringUtils.leftPad("" + random.nextInt(999), 3, '0'); // 订单号
	}

	public static final String getGoodsTradeNo() {
		return FLAG_GOODS + DateUtil.format(new Date(), "yyMMddHHmmss") + StringUtils.leftPad("" + random.nextInt(999), 3, '0'); // 订单号
	}
	public static final String getSportTradeNo() {
		return FLAG_SPORT+DateUtil.format(new Date(), "yyMMddHHmmss") + StringUtils.leftPad("" + random.nextInt(999), 3, '0'); // 订单号
	}
	public static final String getDramaTradeNo() {
		return FLAG_DRAMA+DateUtil.format(new Date(), "yyMMddHHmmss") + StringUtils.leftPad("" + random.nextInt(999), 3, '0'); // 订单号
	}
	public static final String getPubSaleTradeNo() {
		return FLAG_PUBSALE+DateUtil.format(new Date(), "yyMMddHHmmss") + StringUtils.leftPad("" + random.nextInt(999), 3, '0'); // 订单号
	}
	public static final String getGymTradeNo() {
		return FLAG_GYM+DateUtil.format(new Date(), "yyMMddHHmmss") + StringUtils.leftPad("" + random.nextInt(999), 3, '0'); // 订单号
	}
	
	public static final String getMemberCardTradeNo(){
		return FLAG_MEMBER_CARD+DateUtil.format(new Date(), "yyMMddHHmmss") + StringUtils.leftPad("" + random.nextInt(999), 3, '0'); // 订单号
	}
	
	public static final boolean isTicketTrade(String tradeNo) {
		return StringUtils.startsWith(tradeNo, FLAG_TICKET);
	}
	public static final boolean isChargeTrade(String tradeNo) {
		return StringUtils.startsWith(tradeNo, FLAG_CHARGE);
	}
	public static final boolean isGoodsTrade(String tradeNo) {
		return StringUtils.startsWith(tradeNo, FLAG_GOODS);
	}
	public static final boolean isSportTrade(String tradeNo) {
		return StringUtils.startsWith(tradeNo, FLAG_SPORT);
	}
	public static final boolean isDramaOrder(String tradeNo) {
		return StringUtils.startsWith(tradeNo, FLAG_DRAMA);
	}
	public static final boolean isPubSaleOrder(String tradeNo) {
		return StringUtils.startsWith(tradeNo, FLAG_PUBSALE);
	}
	public static final boolean isGymTrade(String tradeNo){
		return StringUtils.startsWith(tradeNo, FLAG_GYM);
	}
	
	public static final boolean passEquals(String textPass, String enPass) {
		return StringUtil.sha(StringUtil.md5(StringUtils.defaultString(textPass), "utf-8"), "utf-8").equals(enPass);
	}

	public static final String getPass(String textPass) {
		return StringUtil.sha(StringUtil.md5(StringUtils.defaultString(textPass),"utf-8"), "utf-8");
	}
	public static final String md5WithKey(String... strList){
		return StringUtil.md5(StringUtils.join(strList, "") + "gewarakey2934ehdoiwuyr", "utf-8");
	}
	
	public static String getPnrpayReturn() {
		return pnrpayReturn;
	}
	public static String getPnrpayNotify() {
		return pnrpayNotify;
	}
	public static String getAlipayReturn() {
		return alipayReturn;
	}
	public static String getAlipayNotify() {
		return alipayNotify;
	}
	public static String getAlipayMobileNotify() {
		return alipayMobileNotify;
	}
	public static String getAlipayMobileReturn() {
		return alipayMobileReturn;
	}
	public static String getCmbpayNotifyUrl() {
		return cmbpayNotifyUrl;
	}
	public static String getBcpayNotifyURL() {
		return bcpayNotifyURL;
	}
	public static String getBcpayReturnUrl() {
		return bcpayReturnUrl;
	}
	public static String getCmbwapPayReturnUrl() {
		return cmbwapPayReturnUrl;
	}
	public static String getGdbpayReturnUrl() {
		return gdbpayReturnUrl;
	}
	public static String getAllinpayReturnUrl() {
		return allinpayReturnUrl;
	}
	public static String getCmwapNotifyUrl() {
		return cmwapNotifyUrl;
	}
	public static String getCmwapReturnUrl() {
		return cmwapReturnUrl;
	}
	public static String getCmNotifyUrl() {
		return cmNotifyUrl;
	}
	public static String getCmReturnUrl() {
		return cmReturnUrl;
	}
	public static String getAlipaySmartNotify() {
		return alipaySmartNotify;
	}
	public static String getHaobaiPayUrl() {
		return haobaiPayUrl;
	}
	public static String getHaobaiOrderCreate() {
		return haobaiOrderCreate;
	}
	public static String getHzpayNotifyUrl() {
		return hzpayNotifyUrl;
	}
	public static String getHzpayReturnUrl() {
		return hzpayReturnUrl;
	}

	public static String getBindPay(GewaOrder order, List<String> bindpayList){
		List<String> mobilePayList = PaymethodConstant.getMobilePayList();
		if(order.sureGewaPartner()){
			List<String> retainList = new ArrayList<String>(bindpayList);
			retainList.retainAll(mobilePayList);
			if(retainList.size()>0) return retainList.get(0);
		}else {
			for(String bindpay : bindpayList){
				if(!mobilePayList.contains(bindpay)) return bindpay;
			}
		}
		return null;
	}
}