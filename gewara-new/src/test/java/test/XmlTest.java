package test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringEscapeUtils;

import com.gewara.util.JsonUtils;
import com.gewara.util.StringUtil;


public class XmlTest {
	public static void main(String[] args) {
		List<Map<String, String>> rows = new ArrayList<Map<String, String>>();
		Map<String, String> row = new HashMap<String, String>();
		row.put("citycode","320100");
		row.put("allownum", "6");
		rows.add(row);
		row = new HashMap<String, String>();
		row.put("citycode","310000");
		row.put("allownum", "8");
		rows.add(row);
		row = new HashMap<String, String>();
		row.put("citycode","330100");
		row.put("allownum", "4");
		rows.add(row);
		System.out.println(JsonUtils.writeObjectToJson(rows));
		String s = "xx&abc$6yÖÐ¹úaaa";
		System.out.println(StringEscapeUtils.escapeHtml(s));
		System.out.println(StringUtil.getEscapeText(s));
		
	}
}
