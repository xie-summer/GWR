package test;

import com.gewara.util.HttpUtils;

public class TestHttpClient {
	public static void main(String args[]){
		String url = "http://192.168.1.5/index.xhtml";
		long cur = System.currentTimeMillis();
		try{
			//TEST timeout
			HttpUtils.getUrlAsString(url, null, 30000);
		}catch(Exception e){
			e.printStackTrace();
		}
		System.out.println(System.currentTimeMillis() - cur);
	}
}
