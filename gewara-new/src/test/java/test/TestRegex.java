package test;

import com.gewara.util.StringUtil;
public class TestRegex {
	public static void main(String[] args){
		/*		String text = null;
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
		 */
		String s = "abc script onload xxy";
		System.out.println(StringUtil.regMatch(s, "<script|onblur|onclick|onfocus|onload", true));
		
		/*String urlReg = "^/(beijing|hangzhou|fuyang|ningbo|shaoxing|jiaxing|jinhua|yuyao|cixi|huzhou|taizhou|wenzhou|guangzhou|shenzhen|zhuhai|foshan|dongguan|zhongshan|nanjing|wuxi|jangyin|changzhou|suzhou|changshu|kunshan|nantong|wuhan|chongqing|chengdu)/(.*).xhtml$";
		String urlReg2 = "^/(beijing|hangzhou|fuyang|ningbo|shaoxing|jiaxing|jinhua|yuyao|cixi|huzhou|taizhou|wenzhou|guangzhou|shenzhen|zhuhai|foshan|dongguan|zhongshan|nanjing|wuxi|jangyin|changzhou|suzhou|changshu|kunshan|nantong|wuhan|chongqing|chengdu)[/]*(.*)";
		Pattern pattern1 = Pattern.compile(urlReg);
		Pattern pattern2 = Pattern.compile(urlReg2);
		String src = "/shenzhen/xxxyykdk/eueu26jueu2y/sdkjfsldkjlkjlkjsdfkwelfjsldkjfwelsdkjflwkejflsdjflwejfoj2378w9889237897";
		String src2 = "test.xhtml";
		String[] module = new String[]{"movie", "cinema", "drama", "ktv", "xxx", "yyyy"};
		long time = System.currentTimeMillis();
		int success=0, success2=0;
		for(int i=0; i< 10000000;i++){
			Matcher matcher = pattern1.matcher(src + i + module[i%6] + i + src2 );
			if(matcher.find()) success ++;
			matcher = pattern2.matcher(src + i + module[i%6] + i + src2 );
			if(matcher.find()) success2 ++;
		}
		System.out.println(System.currentTimeMillis()-time +", success:" + success + ", success2:" + success2);*/
		
	}
}
