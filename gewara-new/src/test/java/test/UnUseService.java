package test;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;

public class UnUseService {
	public static void main(String[] args) {
		Map<String, String> payParams = new HashMap<String, String>();
		String[] paramNames = payParams.get("paramNames").split(",");
		for(String key : paramNames){
			System.out.println("key=" + key);
		}
	}
	public static void regMatch(String src, Pattern pattern) {
		Matcher matcher = pattern.matcher(src);
		if(matcher.find()){
			String result = matcher.replaceAll("$2");
			if(StringUtils.startsWith(result.trim(), "public")){
				System.out.println("\t\t" + result);
			}
			
		}
	}
}
