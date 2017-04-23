package test;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import com.gewara.util.HttpResult;
import com.gewara.util.HttpUtils;

public class PartnerTest {

	/**
	 * 
	 * @param args
	 *
	 * @author leo.li
	 * Modify Time May 28, 2013 10:49:50 AM
	 * @throws UnsupportedEncodingException 
	 */
	public static void main(String[] args) throws UnsupportedEncodingException {
		String url = "http://gzunionpay.piaobaobao.com:8383/req/gewala/state.aspx";
		
		Map<String, String> params = new HashMap<String, String>();
		params.put("tradeno", "");
		params.put("paidAmount", "60");
		params.put("payseqno", "");
		params.put("checkvalue", "");
		HttpResult result = HttpUtils.postUrlAsString(url, params);
		
		System.out.println(result.getResponse());
	}

}
