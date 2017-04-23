package test;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
public class TestHtmlParser {
	public static void main(String[] args) throws Exception{
/*		String s = "<div>DivText人民<span id='xxxxyy'>SPANtext</span></div>BODYTEXT中国&ldquo;&ldquo;&ldquo;</div>";
		s += "<script type=\"text/javascript\">function test(){return \"ssssss\"}</script>";
		s = "<ROOT>" + s + "</ROOT>";
		
		UserAgentContext context = new SimpleUserAgentContext();  
		DocumentBuilderImpl dbi = new DocumentBuilderImpl(context);
		//指定文档URI和字符集合  
		Document document = dbi.parse(new InputSource(new StringReader(s)));
		System.out.println(document.getDocumentElement().getTextContent());
		//NodeList node = document.getElementsByTagName("body");
		
		//System.out.println(node.item(0).getTextContent());

		HtmlParser parser = new MozillaHtmlParser();
		System.out.println("-----------------------------------------------------------------------------");
		System.out.println(parser.getHtmlText(s));
		String html = "<div>" +
				"aaaaaaaaaaaaaa<a href=\"/aaaa.xhtml\" config=\"/sss.xml\">bbb<a> " +
				"bbbbbb<a href=\"/bbbb.xhtml\">bbb<a>" +
				"ccccc<a href=\"/cccc.xhtml\">ccc<a>  " +
				"</div>";
		//HtmlParser parser = new SimpleHtmlParser();
		List<String> result = parser.getNodeAttrList(html, "a", "href");
		for(String str:result){
			System.out.println(str);
		}*/
		HttpClient base = new org.apache.http.impl.client.DefaultHttpClient();
		try {
			SSLContext ctx = SSLContext.getInstance("TLS");
			X509TrustManager tm = new X509TrustManager() {
				public void checkClientTrusted(X509Certificate[] xcs, String string) throws CertificateException {
				}

				public void checkServerTrusted(X509Certificate[] xcs, String string) throws CertificateException {
				}

				public X509Certificate[] getAcceptedIssuers() {
					return null;
				}
			};
			ctx.init(null, new TrustManager[] { tm }, null);
			SSLSocketFactory ssf = new SSLSocketFactory(ctx, SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
			ClientConnectionManager ccm = base.getConnectionManager();
			SchemeRegistry sr = ccm.getSchemeRegistry();
			sr.register(new Scheme("https", 443, ssf));
			HttpGet get = new HttpGet("https://openapi.youku.com/v2/oauth2/authorize");
			DefaultHttpClient client = new org.apache.http.impl.client.DefaultHttpClient(ccm, base.getParams());
			HttpResponse response = client.execute(get);
			System.out.println(response.getStatusLine().getStatusCode());
			String resultXML = EntityUtils.toString(response.getEntity(), "UTF-8");
			System.out.println(resultXML);
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		//HttpResult sresult = HttpUtils.getUrlAsString("https://www.alipay.com/index.html");
		//System.out.println(sresult.getResponse());
	}
}
