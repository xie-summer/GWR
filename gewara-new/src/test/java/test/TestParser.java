package test;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.lang.StringUtils;
import org.lobobrowser.html.UserAgentContext;
import org.lobobrowser.html.parser.DocumentBuilderImpl;
import org.lobobrowser.html.test.SimpleUserAgentContext;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;


public class TestParser {
	public static void main(String[] args) throws Exception{
		String html = "<div>DivText人民<span id='xxxxyy'><img src='http://xxxx.yyy.com/2.jpg' /><img src='' />SPANtext</span></div><img src=\"http://xxxx.yyy.com/1.jpg\" />BODYTEXT中国</div>";
		html = "<ROOTHTML>" + html + "</ROOTHTML>";
		UserAgentContext context = new SimpleUserAgentContext();  
		DocumentBuilderImpl dbi = new DocumentBuilderImpl(context);
		List<String> result = new ArrayList<String>();
		try {
			Document doc = dbi.parse(new InputSource(new StringReader(html)));
			NodeList nodeList = doc.getElementsByTagName("img");
			for (int i = 0, len = nodeList.getLength(); i < len; i++) {
				try {
					result.add(nodeList.item(i).getAttributes().getNamedItem("src").getNodeValue());
				} catch (Exception e) {// ignore
				}
			}
			System.out.println(doc.getDocumentElement());
			
			removeNodeByTag(doc, "img", true);
			TransformerFactory   tFactory=TransformerFactory.newInstance();
			Transformer transformer=tFactory.newTransformer();
			//设置输出的encoding为改变gb2312
			//transformer.setOutputProperty("encoding", "gb2312");  
			DOMSource source = new DOMSource(doc);
			StringWriter sw = new StringWriter();
			StreamResult out = new StreamResult(sw);
			transformer.transform(source, out); 
			System.out.println(sw);
		} catch (SAXException e) {//ignore
		} catch (IOException e) {//ignore
		}
		
		System.out.println(StringUtils.join(result, "\n"));
	}
	protected static void removeNodeByTag(Node node, String tag, boolean recusive){
		NodeList nodes = node.getChildNodes();
		List<Node> removeList = new ArrayList<Node>();
		for(int i=0,len=nodes.getLength(); i<len; i++){
			if(StringUtils.equalsIgnoreCase(nodes.item(i).getNodeName(), tag)){
				removeList.add(nodes.item(i));
			}else{
				if(recusive) removeNodeByTag(nodes.item(i), tag, recusive);
			}
		}
		for(Node remove: removeList){
			node.removeChild(remove);
		}
	}
}
