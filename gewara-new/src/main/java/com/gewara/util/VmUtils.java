package com.gewara.util;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.net.URLEncoder;
import java.sql.Timestamp;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.collections.bidimap.DualHashBidiMap;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.RandomUtils;
import org.codehaus.jackson.type.TypeReference;

import com.gewara.constant.AdminCityContant;
import com.gewara.constant.PaymethodConstant;
import com.gewara.helper.ticket.TicketUtil;
import com.gewara.json.bbs.MarkCountData;
import com.gewara.model.common.GewaConfig;
import com.gewara.model.movie.Movie;
import com.gewara.support.ServiceHelper;

/**
 * 每个项目都有自己独立的实现
 * @author gebiao(ge.biao@gewara.com)
 * @since Jul 10, 2012 10:37:52 PM
 */
public class VmUtils extends VmBaseUtil {
	//~~~~~~~~~~~~~~~图片路径~~~~~~~~~~~~~~~~~~~~
	private List<String> imgPathList;
	private String cssJsPath;
	private String imgPath;
	public String getImgPath() {
		return imgPath;
	}
	public String getCssJsPath() {
		return cssJsPath;
	}
	private String defPath;
	public  String randomPic(String wh, String imgName){
		if(StringUtils.startsWith(imgName,"http")) return imgName;
		if(imgPathList==null || imgPathList.size()==0) return defPath + wh + "/" + imgName;
		if(StringUtils.isBlank(imgName)) imgName = "img/default_head.png";
		int hashcode = Math.abs(imgName.hashCode());
		int i = (hashcode)%(imgPathList.size());
		if(StringUtils.isBlank(wh)) return imgPathList.get(i) + imgName;
		return imgPathList.get(i) + wh + "/" + imgName;
	}
	public void initStatic(String staticPath){
		this.cssJsPath = staticPath;
	}
	public void initImg(GewaConfig rateConfig, String picPath){
		this.defPath = picPath;
		imgPathList = new ArrayList<String>();
		Map<String, String> strRateMap = VmUtils.readJsonToMap(rateConfig.getContent());
		for(String path : strRateMap.keySet()){
			String rate = strRateMap.get(path);
			int j = Integer.valueOf(rate);
			for(int x = 1;x<=j; x++){
				imgPathList.add(path);
			}
		}
		
	}
	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	public String getScriptObject(Object modelObject){
		return JsonUtils.writeObjectToJson(modelObject);
	}
	public String getScriptString(String str){
		str = JsonUtils.writeObjectToJson(str);
		return str;
	}
	private static String jsVersion = DateUtil.format(new Date(), "yyyyMMddHH");
	public static void setJsVersion(String jv) {
		jsVersion = jv;
	}
	public static String getJsVersion() {
		return jsVersion;
	}
	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	public String getTag(String category){
		return ServiceHelper.getTag(category);
	}
	public  boolean isTag(String tag){
		return ServiceHelper.isTag(tag);
	}
	public String getCityname(String citycode){
		return AdminCityContant.getCityNameByCode(citycode);
	}
	public final String[] cnweekList = new String[]{"","星期日","星期一","星期二","星期三","星期四","星期五","星期六"};
	private  final Map weekMap = new DualHashBidiMap();
	{
		weekMap.put("0","全天");
		weekMap.put("1","周一");
		weekMap.put("2","周二");
		weekMap.put("3","周三");
		weekMap.put("4","周四");
		weekMap.put("5","周五");
		weekMap.put("6","周六");
		weekMap.put("7","周日");
		weekMap.put("5,6","周五周六");
		weekMap.put("6,7","周六周日");
		weekMap.put("1,2,3","周一至周三");
		weekMap.put("1,2,3,4","周一至周四");
		weekMap.put("1,2,3,4,5","周一至周五");
		weekMap.put("1,2,3,4,7","周日至周四");
		weekMap.put("1,2,3,4,5,7","周日至周五");
		weekMap.put("1,2,3,4,5,6,7","周一至周日");
	}
	public Map getWeekMap() {
		return weekMap;
	}
	public String[] getCnweekList() {
		return cnweekList;
	}

	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	
	public static String py(String original){
		if(isAscii(original)) return original;
		String pv = substring(PinYinUtils.getPinyin(original), 0, 3) + "-" + original;
		return pv;
	}
	public static int getMovieTypeCount(List<Movie> movieList,String str){
		if(movieList==null || movieList.isEmpty()) return 0;
		int count=0;
		for(Movie movie:movieList){
			String s = movie.getType();
			if(!"".equals(s)&& s!=null && s.indexOf(str)!=-1){
				count++;
			}
		}
		return count;
	}
	public static String getUniqueString(String str, String splitter){
		if(StringUtils.isBlank(str)) return "";
		Set<String> set = new LinkedHashSet<String>();
		set.addAll(Arrays.asList(str.split(splitter)));
		return join(set.iterator(), splitter);
	}
	public static String getLight(String str, String qryStr){
		if(StringUtils.isBlank(str)) return "";
		if(StringUtils.isBlank(qryStr)) return str;
		String result = StringUtil.substitute(str, "([" + qryStr + "]+)", "<span style='background-color:#ffebad;'>$1</span>", true);
		return result;
	}
	private static String[][] replace = new String[][]{
			{"&amp;hellip;", "…"},{"&amp;mdash;", "—"},{"&amp;lsquo;", "‘"},{"&amp;rsquo;", "’"},{"&amp;ldquo;", "“"},{"&amp;rdquo;", "”"}
			,{"&amp;middot;", "·"},{"&amp;rarr;", "→"},{"&amp;larr;", "←"},{"&amp;uarr;", "↑"},{"&amp;darr;", "↓"}
	};
	public static String restoreText(String text){
		if(StringUtils.isBlank(text)) return text;
		for(String[] rp: replace){
			text = StringUtils.replace(text, rp[0], rp[1]);
		}
		return text;
	}
	
	/**
	 * 20101018 将带有分隔符的字段替换分隔符
	 */
	public static String replaceSeparator(String oldString, String oldSeparator, String newSeparator){
		String[] tmp = StringUtils.split(oldString, oldSeparator);
		return StringUtils.join(tmp, newSeparator);
	}
	public static Object defalutValue(Object o, Object defaultValue){
		if(o==null) return defaultValue;
		if(o instanceof String && StringUtils.isBlank(""+o)) return defaultValue;
		return o;
	}
	public static String getTotal(List<Map<String, Object>> qryList, String key){
		if(qryList==null || StringUtils.isBlank(key)) return "";
		Long result = 0L;
		for(Map m : qryList){
			if(m.get(key) instanceof Number) {
				result += new Long(m.get(key)+"");
			}
		}
		return result+"";
	}
	public static String getPercent(Object num, Object total){
		if(StringUtils.isBlank(num+"") || StringUtils.isBlank(total+"") || (total+"").equals("0") ) return 0+"";
		if(num instanceof Number && total instanceof Number) {
			Double d = (Long.valueOf(num+"")*100.00)/Long.valueOf(total+"");
			Double d2 = Math.round(d*100)/100.0;
			return d2+"%";
		}
		return "0";
	}
	public static String formatPer(Object obj){
		NumberFormat nf = NumberFormat.getPercentInstance();
		Double number = 0.0;
		if(obj != null)
			number = Double.valueOf(obj+"");
		return nf.format(number); 
	}
	public static String getWebBodyLink(String content, String picPath) {
		if(StringUtils.isBlank(content)) return "";
		content = content.substring(0,content.lastIndexOf("#")+1);
		return getWebBody(content, picPath);
	}
	public static String getWebBody(String content, String picPath){
		if(StringUtils.isBlank(content)) return "";
		content = getfilterString(content, picPath);
		if(StringUtils.isNotBlank(content)){
			content = content.replaceAll("<script", "&lt;script").replaceAll("</script>", "&lt;/script&gt;");
			content = content.replaceAll("<iframe", "&lt;iframe").replaceAll("</iframe>", "&lt;/iframe&gt;");
		}
		return content;
	}
	public static String perlString(String content, String basePath, String picPath){
		if(StringUtils.isBlank(content))return "";
		content = StringUtil.substitute(content, "@([^(#|:|\\s)]+)(:|\\s)", "回复<a target='_blank' href='" + basePath.replace("/","/")+"home/sns/othersPersonIndex.xhtml\\?nickname=$1'>@$1</a>：", true);
		content = StringUtil.substitute(content, "#([^(#|\'|\"|\\\\)]+)#", "<a class='brown' onclick='moderatorTitle(\"$1\");return false;' href='javascript:;'>#$1#</a>", true);
		return getWebBody(content, picPath);
	}
	public static String getfilterString(String str, String picPath){
		String result = str ;
		if(StringUtils.contains(str, ".swf")) {
			result = str.replaceAll("<embed.*src=(.*?)[^>]*?</embed>","");
		}
		result = result.replaceAll("<img.*src=(.*?)[^>]*?>","").replaceAll("\\[((0[1-9]|0[1-4][0-9]|05[0-5])|[^\\]])\\]", "<img src=" + picPath + "img/minFace/$1\\.gif />");
			//.replaceAll("<embed.*src=(.*?)[^>]*?</embed>","");
		return result;
	}
	public static String getSrcString(String str){
		if(StringUtils.isNotBlank(str)){
		 str = str.replaceAll("<img.*src=\"(.*?)\"[^>]*?/>","<a href=\"$1\">$1</a>")
				.replaceAll("<embed.*src=\"(.*?)\"(.*?)[^>]*?</embed>","<a href=\"$1\">$1</a>");
		}
		return str;
	}
	public static String getBodyFirstPic(String str){
		String res = "";
		if(StringUtils.isNotBlank(str)){
			Pattern pattern = Pattern.compile("<img.*?src=\"(.*?)\"", Pattern.CASE_INSENSITIVE);
			Matcher matcher = pattern.matcher(str);
			if(matcher.find()){
				return matcher.group(1);
			}
			return res;
		}
		return res;
	}
	public static String getWapBody(String content, String picPath, String wapPath){
		if(StringUtils.isNotBlank(content)){
			content = StringUtil.getHtmlText(content);
			content = getfilterString(content, picPath);
			content = content.replace("＠", "@").replace("：",":");
			String rep = "<a href='" + wapPath.replace("/", "/") + "home/friendInfo.xhtml\\?nickname=";
			String moderator = "<a href='" + wapPath.replace("/","/")+"ground/moderatorDetail.xhtml?title=";
			content = StringUtil.substitute(content, "#([^#]+)#", moderator + "$1'>#$1#</a>", true);
			content = StringUtil.substitute(content, "@([^:]+):", rep + "$1'>@$1:</a>", true);
		}
		return content;
	}
	public static String getFirstSpell(String target){
		return PinYinUtils.getFirstSpell(target);
	}
	public static Map<String, String> readJsonToMap(String json){
		if(StringUtils.isBlank(json)) return new HashMap();
		Map<String,String> result = JsonUtils.readJsonToObject(new TypeReference<Map<String,String>>() {},json);
		if(result == null) result = new HashMap<String, String>();
		return result;
	}
	
	
	public static String appendString(String str, String separatorChars, String newstr){
		List<String> list = new ArrayList<String>();
		if(StringUtils.isBlank(str)) return newstr;
		list.addAll(Arrays.asList(StringUtils.split(str, separatorChars)));
		list.add(newstr);
		return printList(list, separatorChars);
	}
	public static Object getProperty(Object object, String property){
		try {
			return PropertyUtils.getProperty(object, property);
		} catch (IllegalAccessException e) {
		} catch (InvocationTargetException e) {
		} catch (NoSuchMethodException e) {
		} catch(Exception e){
		}
		return null;
	}
	public static Integer getSingleMarkStar(Object entity, String markname){
		return MarkHelper.getSingleMarkStar(entity, markname);
	}
	
	public static Integer getNewMarkStar(Object bean, String markname, MarkCountData markCount, String avgMarktimes) {
	    return MarkHelper.getNewMarkStar(bean, markname, markCount, avgMarktimes);
	}
	public static Integer getLastMarkStar(Object bean, String markname, MarkCountData markCount, Map markdata) {
	    return MarkHelper.getLastMarkStar(bean, markname, markCount, markdata);
	}
	public static boolean isValidCaptchaId(String captchaId) {
		if(StringUtils.length(captchaId) != 24) return false;
		return StringUtil.md5(StringUtils.substring(captchaId, 0, 16) + "sk#8Kr", 8).equals(StringUtils.substring(captchaId, 16));
	}
	public static String getRandomCaptchaId() {
		String s = StringUtil.getRandomString(16) ;
		s += StringUtil.md5(s + "sk#8Kr", 8);
		return s;
	}
	
	public static Map groupSimplePropertyList(final Collection beanList, String... propertys) {
		Map result = new HashMap();
		if(ArrayUtils.isEmpty(propertys)) return result;
		for(Object bean: beanList){
			String keyvalue = "";
			try {
				for (String property : propertys) {
					Object value = PropertyUtils.getNestedProperty(bean, property);
					if(value !=null){
						if(!BeanUtil.isSimpleProperty(value.getClass())) continue;
						if(value instanceof Timestamp) keyvalue += DateUtil.format((Timestamp)value, "yyyyMMddHHmmss");
						else if(value instanceof Date) keyvalue += DateUtil.format((Date)value, "yyyyMMdd");
						else  keyvalue += value.toString();
					}
				}
				if(StringUtils.isNotBlank(keyvalue)){
					List tmpList = (List) result.get(keyvalue);
					if(tmpList==null){
						tmpList = new ArrayList();
						result.put(keyvalue, tmpList);
					}
					tmpList.add(bean);
				}
			} catch (Exception e) {
			}
		}
		return result;
	}
	
	public static int getItemTimeCount(String detail){
		String[] detailArray=detail.split("\\)");
		int count=0;
		for(int i=0;i<detailArray.length;i++){
			String detailArrays=detailArray[i];
			String[] detailArrayCount=(detailArrays.substring(detailArrays.indexOf(":")+1).split(","));
			count=count+detailArrayCount.length;
		}
		return count;
	}
	public static Integer getFeeByRate(Integer totalAmount, Integer discount, String strRate) {
		int rate = Integer.valueOf(strRate);
		int result = (totalAmount - discount)*rate/100;
		return result ==0?1:result;
	}
	
	public static String joinStr(String str, String joinStr, int index){
		if(StringUtils.isBlank(str)) return "";
		int length = StringUtils.length(str);
		if(StringUtils.isBlank(joinStr) || index >= length) return str;
		StringBuilder sb = new StringBuilder();
		sb.append(StringUtils.substring(str, 0, index));
		sb.append(joinStr);
		sb.append(StringUtils.substring(str, index, length));
		return sb.toString();
	}
	
	public static String getPinYin(String cityname){
		String pinyin = PinYinUtils.getPinyin(cityname);
		return pinyin;
	}
	public static List<Long> getIdList(String idListStr, String spliter){
		return BeanUtil.getIdList(idListStr, spliter);
	}
	public static <T> List<List<T>> groupList(List<T> beanList, int group){
		if(beanList==null || group <=0) return new ArrayList<List<T>>();
		if(beanList.size()<= group) return BeanUtil.partition(beanList, 1);
		if(beanList.size()%group==0){
			int length = beanList.size()/group;
			List<List<T>> result = BeanUtil.partition(beanList, length);
			return result;
		}
		int length = (beanList.size()-1)/group;
		List<T> head = new ArrayList<T>(beanList.subList(0, length * group));
		List<T> tail = new ArrayList<T>();
		if((length+1) * group > beanList.size()) {
			tail = beanList.subList(length * group, beanList.size());
		}
		
		List<List<T>> result = BeanUtil.partition(head, length);
		for(int i=0;i< tail.size();i++){
			result.get(i).add(tail.get(i));
		}
		return result;
	}
	public String urlEncode(String str){
		try {
			return URLEncoder.encode(str, "utf-8");
		} catch (UnsupportedEncodingException e) {
			return str;
		}
	}
	public static String getString(Object value){
		if(value==null) return "";
		if(value instanceof Timestamp) return DateUtil.formatTimestamp((Timestamp)value);
		if(value instanceof Date) return DateUtil.formatDate((Date)value);
		return value.toString();
	}
	public static String getRandom(){
		String result = DateUtil.timeMillis()+"";
		result += RandomUtils.nextInt(10000);
		return result;
	}
	public static String urlDecoder(String str){
		return WebUtils.urlDecoder(str);
	}
	public static String md5(String srcText){
		return StringUtil.md5(srcText + "@Dd^7BkO");
	}
	public static String xmlOutputList(String root, String nextroot, List<Map<String, Object>> resMapList, boolean hasField, List<String> fieldList){
		StringBuilder sb = new StringBuilder();
		if(StringUtils.isNotBlank(root)) sb.append("<" + root + ">");
		if(resMapList!=null){
			for(Map<String, Object> resMap : resMapList){
				sb.append(xmlOutput(nextroot, resMap, hasField, fieldList));
			}
		}
		if(StringUtils.isNotBlank(root)) sb.append("</" + root + ">");
		return sb.toString();
	}
	
	public static String xmlOutput(String root, Map<String, Object> resMap, boolean hasField, List<String> fieldList){
		StringBuilder sb = new StringBuilder();
		if(StringUtils.isNotBlank(root)) sb.append("<" + root + ">");
		if(resMap!=null) {
			for(String key : resMap.keySet()){
				if(hasField) {
					if(fieldList!=null && fieldList.contains(key)) sb.append(getNodeOutput(key, resMap.get(key)));
				}else {
					sb.append(getNodeOutput(key, resMap.get(key)));
				}
			}
		}
		if(StringUtils.isNotBlank(root)) sb.append("</" + root + ">");
		return sb.toString();
	}
	private static String getNodeOutput(String key, Object value){
		String output ="";
		boolean isCdata = false;
		if(value==null){ 
			output = "";
		}else if(value instanceof Timestamp){ 
			output =  DateUtil.formatTimestamp((Timestamp)value);
		}else if(value instanceof Date){ 
			output = DateUtil.formatDate((Date)value);
		}else if(value instanceof String) {
			output = value+"";
			isCdata = true;
		}else {
			output = String.valueOf(value);
		}
		StringBuilder sb = new StringBuilder("<" + key + ">");
		if(isCdata){
			sb.append("<![CDATA[" + output + "]]>");
		}else {
			sb.append(output);
		}
		sb.append("</" + key + ">");
		return sb.toString();
	}
	
	public static String getUnionPayPaymethod(String orderPaymethod,String orderCitycode){
		if(StringUtils.isNotBlank(orderPaymethod) && (orderPaymethod.startsWith("unionPay_") 
				|| StringUtils.equals(orderPaymethod, PaymethodConstant.PAYMETHOD_UNIONPAY))){
			return orderPaymethod;
		}else if(StringUtils.startsWith(orderCitycode, "320")){
			return PaymethodConstant.PAYMETHOD_UNIONPAY_JS;
		}else if(StringUtils.startsWith(orderCitycode, "330")){
			return PaymethodConstant.PAYMETHOD_UNIONPAY_ZJ;
		}else if(StringUtils.equals(orderCitycode, "440300")){
			return PaymethodConstant.PAYMETHOD_UNIONPAY_SZ;
		}
		/** 暂时不上，等手续好再上
		else if(StringUtils.equals(orderCitycode, "110000")){
			return PayUtil.PAYMETHOD_UNIONPAY_BJ;
		}else if(StringUtils.equals(orderCitycode, "440100")){
			return PayUtil.PAYMETHOD_UNIONPAY_GZ;
		}*/
		else{
			return PaymethodConstant.PAYMETHOD_UNIONPAY;
		}		
	}
	
	public static String getUnionPayFastPaymethod(String orderPaymethod,String orderCitycode){
		if(StringUtils.isNotBlank(orderPaymethod) && (orderPaymethod.startsWith("unionPayFast_") || orderPaymethod.equals(PaymethodConstant.PAYMETHOD_UNIONPAYFAST))){
			return orderPaymethod;
		}else if(StringUtils.startsWith(orderCitycode, "320")){
			return PaymethodConstant.PAYMETHOD_UNIONPAYFAST_ACTIVITY_JS;
		}else if(StringUtils.startsWith(orderCitycode, "330")){
			return PaymethodConstant.PAYMETHOD_UNIONPAYFAST_ACTIVITY_ZJ;
		}else if(StringUtils.equals(orderCitycode, "440300")){
			return PaymethodConstant.PAYMETHOD_UNIONPAYFAST_ACTIVITY_SZ;
		}else if(StringUtils.equals(orderCitycode, "110000")){
			return PaymethodConstant.PAYMETHOD_UNIONPAYFAST_ACTIVITY_BJ;
		}else if(StringUtils.equals(orderCitycode, "440100")){
			return PaymethodConstant.PAYMETHOD_UNIONPAYFAST_ACTIVITY_GZ;
		}else{
			return PaymethodConstant.PAYMETHOD_UNIONPAYFAST;
		}		
	}
	public static String getStep1Url(Long mpid, String spkey){
		return TicketUtil.getStep1Url(mpid, spkey);
	}
	
	public static String getRandomStr(int length){
		return RandomStringUtils.random(length, "ABCDEFGHIJKLMNPQRSTUVWXYZabcedfghijklmnpqrstuvwxyz");
	}
	
	public static String reverse(String src){
		return StringUtils.reverse(src);
	}
	
	public static boolean isExistsInvalidSymbol(String html){
		if(StringUtils.isBlank(html)) return false;
		char[] charArray = html.toCharArray();
		for(char a : charArray){
			if(a > 40869 && a < 61440 || a == 12288){ // 特殊不显示字符
      		return true;
      	}
		}
		return false;
	}
}