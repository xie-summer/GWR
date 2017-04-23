package test;


import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import com.gewara.util.StringUtil;
import com.gewara.util.VmUtils;
public class CopyOfTestRegex {
	public static void main(String[] args){
		String text = null;
		//String reg = "\\[(0[1-9]|0[1-2][0-9]|030)\\]";
		//text = "EETT[01]AABBMMEESS[05]TT[01]MMSS";
		//System.out.println(StringUtil.substitute(text, reg, "{$1}", true));
		//String result = StringUtil.substitute(text, "([MM]+)", "<font class='fs14'>$1<\\/font>", true);
		//System.out.println(result);
		//System.out.println(StringUtil.regMatch("w23h44/images/xxx.jpg", "^w[0-9]+h[0-9]+"));
		//System.out.println(StringUtil.regMatch("ww23h44/images/xxx.jpg", "^w[0-9]+h[0-9]+"));
		text = "xk 352#dk中国中国加工dke98中国54637*（";
		String result = StringUtil.substitute(text, "[^ \\w$\\u4e00-\\u9fa5]+", "", true);
		System.out.println(result);
		result = VmUtils.getLight(text, "中国");
		System.out.println(result);
		String emails = "aa@bb.com，cc@dd.com;dkdk@die.com,111@222.com";
		result = StringUtil.substitute(emails, "[， ;]", ",", false);
		System.out.println(result);
		text = "<a href=\"xxxx.html?s=1&b=2&amp;ss=ttt\">xxx</a>"; 
		result = StringUtil.substitute(text, "&(?!amp;)","&amp;", true);
		System.out.println(result);
	
		List<File> fileList = new ArrayList<File>();
		java.util.Collections.sort(fileList, new Comparator<File>(){

			@Override
			public int compare(File o1, File o2) {
				return (int) (o2.lastModified() - o1.lastModified());
			}
		});
	}
}
