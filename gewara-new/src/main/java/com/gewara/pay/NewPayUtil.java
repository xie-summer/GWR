/**
 * 
 */
package com.gewara.pay;

import java.util.Properties;

/**
 * @author qilun
 *
 */
public class NewPayUtil {
	private static String merid;
	private static String payurl;
	private static String qryurl;
	private static String returnurl;
	private static String notifyurl;
	private static String merprikey;
	private static String gewapubkey;
	private static String downReconciliationFileUrl;//下载银行对账文件api地址，支付系统目前就江苏银行下载对账文件
	
	private static String newPayurl;
	private static String newQryurl;
	private static String newDownReconciliationFileUrl;
	
	public static synchronized void init(String propertyFile){
		Properties props = new Properties();
		try {
			props.load(ChinapayUtil.class.getClassLoader().getResourceAsStream(propertyFile));
		} catch (Exception e) {
			throw new IllegalArgumentException("property File Error!!!!", e);
		}
		merid = props.getProperty("merid");
		payurl = props.getProperty("payurl");
		qryurl = props.getProperty("qryurl");
		returnurl = props.getProperty("returnurl");
		notifyurl = props.getProperty("notifyurl");
		merprikey = props.getProperty("merprikey");
		gewapubkey = props.getProperty("gewapubkey");
		downReconciliationFileUrl = props.getProperty("downReconciliationFileUrl");		

		newPayurl = props.getProperty("new.payurl");
		newQryurl = props.getProperty("new.qryurl");
		newDownReconciliationFileUrl = props.getProperty("new.downReconciliationFileUrl");
	}
	public static String getMerid() {
		return merid;
	}
	public static String getMerprikey() {
		return merprikey;
	}
	public static String getGewapubkey() {
		return gewapubkey;
	}
	public static String getPayurl() {
		return payurl;
	}
	public static String getReturnurl() {
		return returnurl;
	}
	public static String getNotifyurl() {
		return notifyurl;
	}
	public static String getQryurl() {
		return qryurl;
	}
	
	public static String getDownReconciliationFileUrl(){
		return downReconciliationFileUrl;
	}
	public static String getNewPayurl() {
		return newPayurl;
	}
	public static void setNewPayurl(String newPayurl) {
		NewPayUtil.newPayurl = newPayurl;
	}
	public static String getNewQryurl() {
		return newQryurl;
	}
	public static void setNewQryurl(String newQryurl) {
		NewPayUtil.newQryurl = newQryurl;
	}
	public static String getNewDownReconciliationFileUrl() {
		return newDownReconciliationFileUrl;
	}
	public static void setNewDownReconciliationFileUrl(
			String newDownReconciliationFileUrl) {
		NewPayUtil.newDownReconciliationFileUrl = newDownReconciliationFileUrl;
	}
}
