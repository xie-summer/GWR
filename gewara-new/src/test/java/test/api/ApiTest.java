package test.api;

import java.util.HashMap;
import java.util.Map;

import org.hibernate.criterion.Restrictions;
import org.hibernate.criterion.SimpleExpression;

import com.gewara.util.HttpResult;
import com.gewara.util.HttpUtils;

public class ApiTest {
	public static String testPath = "http://180.153.146.137:82/";
	public static String apiPath = "http://api.gewara.com/";
	public static String apiSearchPath = "http://localhost/";
	public static void testCinemaList(String from,String pointx,String pointy,String specialfield,String type,String orderField,String movieid
			,String subwayid,String countycode){
		String api = "/api2/mobile/cinemaList.xhtml";
		Map<String, String> params = new HashMap<String, String>();
		params.put("key", "android2009");
		params.put("encryptCode", "224b258184c2930e378acf7c7dfc4f7b");
		params.put("citycode", "310000");
		params.put("maxnum","100");
		params.put("appVersion", "1.6.0");
		params.put("version", "1.0");
		params.put("from", from);
		params.put("pointx", pointx);
		params.put("pointy", pointy);
		params.put("specialfield", specialfield);
		params.put("type", type);
		params.put("orderField", orderField);
		params.put("movieid", movieid);
		params.put("subwayid", subwayid);
		params.put("countycode", countycode);
		test(api, params);
	}
	public static void test(String api, Map<String, String> params){
		String url1 = testPath + api;
		String url2 = apiPath + api;
		//......
		HttpResult testResult = HttpUtils.getUrlAsString(url1, params);
		System.out.println(testResult.getResponse());
		
		System.out.println("----------------------------------------------------------------------------");
		
		HttpResult apiResult = HttpUtils.getUrlAsString(url2, params);
		System.out.println(apiResult.getResponse());
	}
	
	public  static void testSearch(){
		Map<String, String> params = new HashMap<String, String>();
		params.put("skey", "杭州百老汇城");
		HttpResult testResult = HttpUtils.postUrlAsString(apiSearchPath+"/newSearchKey.xhtml", params);
	    System.out.println(testResult.getResponse());
	}
	
	public  static void testGetLight(){
		/*String text = "The quick brown fox jumps over the lazy dog";
		TermQuery query = new TermQuery(new Term("field", "fox"));
		QueryScorer scorer = new QueryScorer(query);
		Highlighter highlighter = new Highlighter(scorer);
		TokenStream tokenStream =
		new  StandardAnalyzer(Version.LUCENE_30).tokenStream("field",
		new StringReader(text));
		System.out.println(highlighter.getBestFragment(tokenStream,text));*/
//		
//		String[] pros = new String[]{"name", "skey"};
//		try {
//			MultiFieldQueryParser parser = new MultiFieldQueryParser(Version.LUCENE_30, pros, new StandardAnalyzer(Version.LUCENE_30));
//			parser.setDefaultOperator(QueryParser.AND_OPERATOR);
//			Query skeyQuery = parser.parse("诞京");
//			QueryScorer scorer = new QueryScorer(skeyQuery);
//			SimpleHTMLFormatter formatter = new SimpleHTMLFormatter("<span style='color:red'>", "</span>");
//			Highlighter highlighter = new Highlighter(formatter,scorer);
//			highlighter.setTextFragmenter(new SimpleFragmenter(50));
//			String lightStr = highlighter.getBestFragment(new StandardAnalyzer(Version.LUCENE_30), "field", txt);
//			System.out.println(lightStr);
//		} catch (Exception e) {
//			e.printStackTrace();
//		} 

		
		
	}
	
	public static void main(String[] args){
		SimpleExpression ss = Restrictions.eq("s", "yyy");
		//ss.toString(criteria, criteriaQuery)
		//Criterion yy = Restrictions.in("test", values);
		System.out.println(ss.toString());
		//testGetLight();
	}
}
