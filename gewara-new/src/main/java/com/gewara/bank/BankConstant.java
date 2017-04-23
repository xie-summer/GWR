package com.gewara.bank;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.map.UnmodifiableMap;

public class BankConstant {
	private static Map<String, String> alipayBankMap = null;		//支付宝PC银行
	private static Map<String, String> alipayCreditMap = null;		//支付宝PC大额信用卡
	private static Map<String, String> alipayKjCreditMap = null;	//支付宝PC快捷信用卡
	private static Map<String, String> alipayMotoCreditMap = null;	//支付宝PC快捷支付信用卡前置
	private static List<String> alipayWapCreditList = null;			//支付宝WAP信用卡
	private static List<String> alipayWapDebitList = null;			//支付宝WAP借记卡
	private static Map<String, String> pnrBankMap = null;			//汇付PC银行
	static{
		alipayInit();
		alipayWapInit();
		pnrInit();
	}
	public static void alipayWapInit(){
		alipayWapCreditList = new LinkedList<String>();
		alipayWapCreditList.add("建设银行,aliwapPay:CREDITCARD_CCB");
		alipayWapCreditList.add("工商银行,aliwapPay:CREDITCARD_ICBC");
		alipayWapCreditList.add("广发银行,aliwapPay:CREDITCARD_GDB");
		alipayWapCreditList.add("中国银行,aliwapPay:CREDITCARD_BOC");
		alipayWapCreditList.add("华夏银行,aliwapPay:CREDITCARD_HXBANK");
		alipayWapCreditList.add("更多银行,aliwapPay:CREDITCARD");
		alipayWapDebitList = new LinkedList<String>();
		alipayWapDebitList.add("建设银行,aliwapPay:DEBITCARD_CCB");
		alipayWapDebitList.add("交通银行,aliwapPay:DEBITCARD_COMM");
		alipayWapDebitList.add("农业银行,aliwapPay:DEBITCARD_ABC");
		alipayWapDebitList.add("中国银行,aliwapPay:DEBITCARD_BOC");
		alipayWapDebitList.add("更多银行,aliwapPay:DEBITCARD");
	}
	public static void alipayInit(){
		Map<String, String> tmpMap = new LinkedHashMap<String, String>();
		tmpMap.put("BOCB2C", "中国银行");
		tmpMap.put("ICBCB2C", "中国工商银行");
		tmpMap.put("CMB", "招商银行");
		tmpMap.put("CCB", "中国建设银行");
		tmpMap.put("ABC", "中国农业银行");
		tmpMap.put("SPDB", "上海浦东发展银行");
		tmpMap.put("COMM", "交通银行");
		tmpMap.put("SPABANK", "平安银行");
		tmpMap.put("SHBANK", "上海银行");
		tmpMap.put("CIB", "兴业银行");
		tmpMap.put("CMBC", "中国民生银行");
		tmpMap.put("CITIC", "中信银行");
		tmpMap.put("CEBBANK", "光大银行");
		alipayBankMap = UnmodifiableMap.decorate(tmpMap);
	
		//信用卡支持银行
		tmpMap = new LinkedHashMap<String, String>();
		tmpMap.put("DEicbc301","中国工商银行");
		//tmpMap.put("DEccb301","中国建设银行");
		tmpMap.put("DEcmb301","招商银行");
		tmpMap.put("DEboc301","中国银行");
		tmpMap.put("DEcomm301","交通银行");
		tmpMap.put("DEgdb301","广东发展银行");
		tmpMap.put("DEcib301","兴业银行");
		tmpMap.put("DEceb301","中国光大银行");
		tmpMap.put("DEspabank301","平安银行");
		tmpMap.put("DEcitic302","中信银行");
		tmpMap.put("DEspdb301","浦发银行");
		//tmpMap.put("DEshbank301","上海银行");
		alipayCreditMap = UnmodifiableMap.decorate(tmpMap);
		
		tmpMap = new LinkedHashMap<String, String>();
		tmpMap.put("KJICBC", "工商银行");
		tmpMap.put("KJABC", "农业银行");
		tmpMap.put("KJCMB", "招商银行");
		tmpMap.put("KJCCB", "建设银行");
		tmpMap.put("KJBOC", "中国银行");
		tmpMap.put("KJSDB", "深圳发展银行");
		tmpMap.put("KJCEB", "光大银行");
		//tmpMap.put("KJSPABANK", "平安银行");
		alipayKjCreditMap = UnmodifiableMap.decorate(tmpMap);
		
		tmpMap = new LinkedHashMap<String, String>();
		tmpMap.put("MOCCB-MOTO-CREDIT", "中国建设银行");
		tmpMap.put("MOICBC-MOTO-CREDIT", "中国工商银行");
		tmpMap.put("MOBOC-MOTO-CREDIT", "中国银行");
		tmpMap.put("MOHXBANK-EXPRESS-CREDIT", "华夏银行");
		tmpMap.put("MOABC-EXPRESS-CREDIT", "中国农业银行");
		alipayMotoCreditMap = UnmodifiableMap.decorate(tmpMap);
	}
	public static void pnrInit(){
		Map<String, String> tmp = new HashMap<String, String>();
		tmp.put("07","招商银行");
		tmp.put("10","中国建设银行");
		tmp.put("12","中国民生银行");
		tmp.put("25","中国工商银行");
		tmp.put("45","中国银行");
		tmp.put("50","平安银行");
		tmp.put("41","交通银行");
		tmp.put("09","兴业银行");
		tmp.put("29","中国农业银行");
		tmp.put("36","中国光大银行");
		tmp.put("16","浦东发展银行");
		tmp.put("33","中信银行");
		tmp.put("13","华夏银行");
		tmp.put("49","南京银行");
		tmp.put("53","浙商银行");
		tmp.put("51","杭州银行");
		tmp.put("52","宁波银行");
		tmp.put("14","深圳发展银行");
		pnrBankMap = UnmodifiableMap.decorate(tmp);
	}
	public static Map<String, String> getAlipayBankMap() {
		return alipayBankMap;
	}
	public static Map<String, String> getAlipayCreditMap() {
		return alipayCreditMap;
	}
	public static Map<String, String> getAlipayKjCreditMap() {
		return alipayKjCreditMap;
	}
	public static Map<String, String> getAlipayMotoCreditMap() {
		return alipayMotoCreditMap;
	}
	public static Map<String, String> getPnrBankMap() {
		return pnrBankMap;
	}
}
