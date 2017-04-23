
package test.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import com.gewara.util.DateUtil;
import com.gewara.util.HttpUtils;

public class GetLogger {
	private static Map<String, String> ipMap = new HashMap<String, String>();
	static{
		ipMap.put("37", "180.153.146.137"); //houtai
		ipMap.put("39", "180.153.146.139"); //pay
		ipMap.put("40", "180.153.146.140"); //pay

		ipMap.put("43", "114.80.171.243"); //web
		ipMap.put("44", "114.80.171.244"); //web
		ipMap.put("45", "114.80.171.245"); //web
		ipMap.put("46", "114.80.171.246"); //web
		ipMap.put("33", "180.153.146.133"); //web
		
		ipMap.put("47", "114.80.171.247"); //API
		ipMap.put("48", "114.80.171.248"); //API
		ipMap.put("49", "114.80.171.249"); //API
		
		ipMap.put("50", "114.80.171.250"); //houtai
		ipMap.put("51", "114.80.171.251"); //houtai
		
		ipMap.put("24", "180.153.135.124");
		ipMap.put("25", "180.153.135.125");
	}
	public static void main(String[] args) throws Exception{
		String base = "domain1";
		System.out.println("输入日志日期(当前不需输入，多个用“,”隔开，昨天：" + DateUtil.formatDate(DateUtil.addDay(new Date(), -1)) + "):");
		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
		String dateStr = in.readLine();
		
		System.out.println("输入服务器：web, api, houtai, webapi, 37,39,40,43,44,45,46,47,48,49,50,51");
		String servers = in.readLine();
		if(StringUtils.equals(servers, "web")){
			servers = "43,44,45,46,33";
		}else if(StringUtils.equals(servers, "api")){
			servers = "48,49";
		}else if(StringUtils.equals(servers, "houtai")){
			servers = "50,51";
		}else if(StringUtils.equals(servers, "webapi")){
			servers = "43,44,45,46,47,48,49";
		}else if(StringUtils.equals(servers, "ticket")){
			servers = "43,44,45,46";
		}
		for(String server: servers.split(",")){
			String name = "E:\\logger\\" + base + server;
			String url = "http://" + ipMap.get(server) + ":8080/log/" + base + ".log";
			if(StringUtils.isNotBlank(dateStr)) {
				String[] dateList = StringUtils.split(dateStr, ",");
				for(String date: dateList){
					url += "." + date;
					name += "." + date;
					name += ".log";
					System.out.println("date:"+DateUtil.getCurFullTimestamp() + " reading:" + name );
					HttpUtils.getUrlAsInputStream(url, null, new HttpUtils.FileRequestCallback(new File(name)));
					System.out.println("date:"+DateUtil.getCurFullTimestamp() + " complete: " + name );
				}
			}else{
				String tmpdateStr = DateUtil.formatDate(new Date());
				name += "." + tmpdateStr;
				name += ".log";
				System.out.println("date:"+DateUtil.getCurFullTimestamp() + " reading:" + name);
				HttpUtils.getUrlAsInputStream(url, null, new HttpUtils.FileRequestCallback(new File(name)));
				System.out.println("date:"+DateUtil.getCurFullTimestamp() + " complete: " + name );
			}
		}
		System.out.println("*************************All Done!");
	}
}
