package test;


import java.util.Properties;

import com.gewara.util.DateUtil;
import com.gewara.util.StringUtil;

public class TestUrlEncode {
	static double time1 = DateUtil.parseDate("2012-03-02").getTime();
	static double time2 = DateUtil.parseDate("2012-03-14").getTime();
	static double per = (time2 - time1)/31400;
	static double c = 19989;
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Properties props = new Properties();
		try{
			props.load(TestUrlEncode.class.getClassLoader().getResourceAsStream("config/appkey.local.properties"));
			System.out.println(props.get("memchached.servers") + "d");
		}catch(Exception e){
			
		}
		System.out.println(StringUtil.md5("9856213135698563252:10bkm2011kris62#Ke@cnd2mD"));
	}
	public static double getCur(long cur){
		double result = (cur-time1)*fn()/per;
		return result;
	}
	public static double fn(){
		return 1; 
	}
}
