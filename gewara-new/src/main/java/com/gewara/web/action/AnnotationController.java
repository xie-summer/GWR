package com.gewara.web.action;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Array;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.ui.ModelMap;
import org.springframework.util.ObjectUtils;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.gewara.Config;
import com.gewara.service.DaoService;
import com.gewara.service.member.MemberService;
import com.gewara.untrans.RelateService;
import com.gewara.util.DateUtil;
import com.gewara.util.GewaLogger;
import com.gewara.util.LoggerUtils;
import com.gewara.web.component.ShLoginService;
import com.gewara.web.support.GewaVelocityView;
import com.gewara.web.util.PageUtil;
/**
 * @author <a href="mailto:acerge@163.com">gebiao(acerge)</a>
 * @since 2007-9-28下午02:05:17
 */
public abstract class AnnotationController implements ApplicationContextAware{
	//此处没办法，只能写死
	public static final String LOGIN_COOKIE_NAME = "gewara_uskey_";

	public static final String SUCCESS_MESSAGES_KEY = "successMsgs";
	public static final String ERROR_MESSAGES_KEY = "errorMsgs";

	protected final transient GewaLogger dbLogger = LoggerUtils.getLogger(getClass(), Config.getServerIp(), Config.SYSTEMID);
	@Autowired@Qualifier("daoService")
	protected DaoService daoService;
	public void setDaoService(DaoService daoService) {
		this.daoService = daoService;
	}
	@Autowired@Qualifier("loginService")
	protected ShLoginService loginService;
	
	@Autowired@Qualifier("memberService")
	protected MemberService memberService;
	public void setMemberService(MemberService memberService) {
		this.memberService = memberService;
	}
	protected WebApplicationContext applicationContext;
	protected final PageUtil getScriptPageUtil(int count, int rowsPerpage,int pageNo, Map<String, Object> params){
		PageUtil pageUtil = new PageUtil(count,rowsPerpage,pageNo,null);
		pageUtil.initPageInfo(params);
		return pageUtil;
	}
	@Autowired@Qualifier("relateService")
	protected RelateService relateService;
	public void setRelateService(RelateService relateService) {
		this.relateService = relateService;
	}
	protected final String showError(ModelMap model, String errMsg){
		model.put(ERROR_MESSAGES_KEY, errMsg);
		return "redirect:/showResult.xhtml";
	}
	protected final String showError_NOT_LOGIN(ModelMap model){
		model.put(ERROR_MESSAGES_KEY, "请先登录!");
		return "redirect:/showResult.xhtml";
	}
	protected final String showMessage(ModelMap model, String msg){
		model.put(SUCCESS_MESSAGES_KEY, msg);
		return "redirect:/showResult.xhtml";
	}
	protected final String showMessage(ModelMap model, List<String> msgList){
		model.put(SUCCESS_MESSAGES_KEY, StringUtils.join(msgList.toArray(), "@@"));
		return "redirect:/showResult.xhtml";
	}
	protected final String showMessageAndReturn(ModelMap model, HttpServletRequest request, String msg){
		if(request != null){
			model.put("returnUrl",request.getHeader("Referer"));
		}
		model.put(SUCCESS_MESSAGES_KEY, msg);
		return "redirect:/showResult.xhtml";
	}
	protected final String forwardMessage(ModelMap model, String msg){
		model.put(SUCCESS_MESSAGES_KEY, msg);
		return "showResult.vm";
	}
	protected final String forwardMessage(ModelMap model, List<String> msgList){
		model.put("msgList", msgList);
		return "showResult.vm";
	}
	
	/**
	 * @param model
	 * @param retval
	 * @return
	 */
	protected final String showJsonSuccess(ModelMap model){
		return showJsonSuccess(model, "");
	}
	protected final String showJsonSuccess(ModelMap model, String retval){
		return showJsonSuccess(model, retval, "data");
	}
	protected final String showJsonSuccess(ModelMap model, Map jsonMap){
		return showJsonSuccess(model, jsonMap, "data");
	}
	protected final String showJsonSuccess(ModelMap model, Map jsonMap, String jsname){
		jsonMap.put("success", true);
		model.put("jsonMap", jsonMap);
		model.put("jsname", jsname);
		return "common/json.vm";
	}
	protected final String showJsonSuccess(ModelMap model, String retval, String jsname){
		Map jsonMap = new HashMap();
		jsonMap.put("success", true);
		jsonMap.put("retval", retval);
		model.put("jsonMap", jsonMap);
		model.put("jsname", jsname);
		return "common/json.vm";
	}
	protected final String showDirectJson(ModelMap model, String jsonStr){
		model.put("jsonStr", jsonStr);
		return "common/directJson.vm";
	}
	protected final String showJsonError(ModelMap model, String msg){
		return showJsonError(model, msg, "data");
	}
	protected final String showJsonError(ModelMap model, Map jsonMap){
		return showJsonError(model, jsonMap, "data");
	}
	protected final String showJsonError(ModelMap model, Map jsonMap, String jsname){
		if(jsonMap == null) jsonMap = new HashMap();
		jsonMap.put("success", false);
		model.put("jsonMap", jsonMap);
		model.put("jsname", jsname);
		return "common/json.vm";
	}
	protected final String showJsonError(ModelMap model, String msg, String jsname){
		Map jsonMap = new HashMap();
		jsonMap.put("success", false);
		jsonMap.put("msg", msg);
		model.put("jsonMap", jsonMap);
		model.put("jsname", jsname);
		return "common/json.vm";
	}
	protected final String showJsonError_CAPTCHA_ERROR(ModelMap model){
		return showJsonError(model, "验证码错误！", "data");
	}
	protected final String showJsonError_NOT_LOGIN(ModelMap model){
		return showJsonError(model, "您还没有登录，请先登录！", "data");
	}
	protected final String showJsonError_NORIGHTS(ModelMap model){
		return showJsonError(model, "您没有权限！", "data");
	}
	protected final String showJsonError_REPEATED(ModelMap model){
		return showJsonError(model, "不能重复操作！", "data");
	}
	protected final String showJsonError_NOT_FOUND(ModelMap model){
		return showJsonError(model, "未找到相关数据！", "data");
	}
	protected final String showJsonError_DATAERROR(ModelMap model){
		return showJsonError(model, "数据有错误！请刷新重试！", "data");
	}
	protected final String showJsonError_PARAMSERROR(ModelMap model){
		return showJsonError(model, "参数错误！", "data");
	}
	protected final String showJsonError_BLACK_LIST(ModelMap model){
		return showJsonError(model, "你在黑名单中！如有疑问请联系格瓦拉客服！", "data");
	}
	protected final String showJsonError_SOFAST(ModelMap model){
		return showJsonError(model, "提交内容频率不能太快！", "data");
	}
	protected final String showJsonError_KEYWORD(ModelMap model){
		return showJsonError(model, "你发表的帖子包含“敏感关键词”，通过管理员审核后显示!", "data");
	}
	
	protected final String showJsonInfo(ModelMap model, String keys, Object...values){
		Map jsonMap = new HashMap();
		String[] keyList = keys.split(",");
		for(int i=0,len=Math.min(keyList.length, values.length);i<len;i++){
			jsonMap.put(keyList[i], values[i]);
		}
		jsonMap.put("success", true);
		model.put("jsonMap", jsonMap);
		model.put("jsname", "data");
		return "common/json.vm";
	}

	
	/**
	 * @param model
	 * @param msg
	 * @param returnUrl  /home/xxx.xhtm
	 * @return
	 */
	protected final String alertMessage(ModelMap model, String msg){
		return alertMessageError(model, msg);
	}
	protected final String alertMessageSuccess(ModelMap model, String msg){
		Map jsonMap = new HashMap();
		jsonMap.put("success", true);
		jsonMap.put("msg", msg);
		model.put("jsonMap", jsonMap);
		return "showMessage.vm";
	}
	protected final String alertMessageError(ModelMap model, String msg){
		Map jsonMap = new HashMap();
		jsonMap.put("success", false);
		jsonMap.put("msg", msg);
		model.put("jsonMap", jsonMap);
		return "showMessage.vm";
	}
	protected final String alertMessage(ModelMap model, String msg, String returnUrl){
		Map jsonMap = new HashMap();
		jsonMap.put("msg", msg);
		jsonMap.put("returnUrl", returnUrl);
		model.put("jsonMap", jsonMap);
		return "showMessage.vm";
	}
	protected final String goBack(ModelMap model, String msg){
		Map jsonMap = new HashMap();
		jsonMap.put("msg", msg);
		jsonMap.put("goback", true);
		model.put("jsonMap", jsonMap);
		return "showMessage.vm";
	}
	
	protected final String show404(ModelMap model, String msg){
		model.put("msg", msg);
		model.put(GewaVelocityView.KEY_HTTP_STATUS, 404);
		return "wide_404.vm";
	}
	protected final String getRealPath(String filename){
		return applicationContext.getServletContext().getRealPath(filename);
	}
	protected final String getRealPath(String filename, boolean addSplash){
		String result = applicationContext.getServletContext().getRealPath(filename);
		if(addSplash){
			if(!result.endsWith("/") && !result.endsWith("\\")) result += "/";
		}
		return result;
	}
	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = (WebApplicationContext)applicationContext;
	}
	private final List<String> VALID_REPORT_FORMAT = Arrays.asList("csv","html", "pdf", "xls", "image");
	protected final String showReportView(ModelMap model, String viewname){
		return showReportView(model, "pdf", viewname);
	}
	protected final String REPORT_DATA_KEY = "REPORT_DATA";//报表循环数据
	protected final String showReportView(ModelMap model, String format, String viewname){
		if(format == null || !VALID_REPORT_FORMAT.contains(format)){
			dbLogger.warn("没有指定报表格式或格式不正确[" + format + "]：" + viewname + ", 使用默认的pdf格式！");
			format = "pdf";
		}
		model.put("format", format);
		return viewname;
	}
	private final String supportOtherCharset = "gbk,gb2312";
	protected void useCharset(ModelMap model, String charset){
		if(StringUtils.containsIgnoreCase(supportOtherCharset, charset)){
			model.put(GewaVelocityView.USE_OTHER_CHARSET, charset);
		}
	}
	protected final String showRedirect(String path, ModelMap model){
		if(StringUtils.startsWith(path, "/")) path = path.substring(1);
		StringBuilder targetUrl = new StringBuilder(path);
		appendQueryProperties(targetUrl, model, "utf-8");
		model.put("redirectUrl", targetUrl.toString());
		return "tempRedirect.vm";
	}
	private void appendQueryProperties(StringBuilder targetUrl, ModelMap model, String encoding) {
		boolean first = (targetUrl.indexOf("?") < 0);
		for (Map.Entry<String, Object> entry : queryProperties(model).entrySet()) {
			Object rawValue = entry.getValue();
			Iterator valueIter = null;
			if (rawValue != null && rawValue.getClass().isArray()) {
				valueIter = Arrays.asList(ObjectUtils.toObjectArray(rawValue)).iterator();
			}
			else if (rawValue instanceof Collection) {
				valueIter = ((Collection) rawValue).iterator();
			}
			else {
				valueIter = Collections.singleton(rawValue).iterator();
			}
			while (valueIter.hasNext()) {
				Object value = valueIter.next();
				if (first) {
					targetUrl.append('?');
					first = false;
				}
				else {
					targetUrl.append('&');
				}
				String encodedKey = urlEncode(entry.getKey(), encoding);
				String encodedValue = (value != null ? urlEncode(value.toString(), encoding) : "");
				targetUrl.append(encodedKey).append('=').append(encodedValue);
			}
		}
	}
	private Map<String, Object> queryProperties(Map<String, Object> model) {
		Map<String, Object> result = new LinkedHashMap<String, Object>();
		for (Map.Entry<String, Object> entry : model.entrySet()) {
			if (isEligibleProperty(entry.getValue())) {
				result.put(entry.getKey(), entry.getValue());
			}
		}
		return result;
	}
	private boolean isEligibleProperty(Object value) {
		if (value == null) return false;
		if (isEligibleValue(value)) return true;

		if (value.getClass().isArray()) {
			int length = Array.getLength(value);
			if (length == 0) {
				return false;
			}
			for (int i = 0; i < length; i++) {
				Object element = Array.get(value, i);
				if (!isEligibleValue(element)) {
					return false;
				}
			}
			return true;
		}

		if (value instanceof Collection) {
			Collection coll = (Collection) value;
			if (coll.isEmpty()) {
				return false;
			}
			for (Object element : coll) {
				if (!isEligibleValue(element)) {
					return false;
				}
			}
			return true;
		}

		return false;
	}
	private String urlEncode(String input, String charsetName) {
		try {
			return (input != null ? URLEncoder.encode(input, charsetName) : null);
		} catch (UnsupportedEncodingException e) {
			return null;
		}
	}
	private boolean isEligibleValue(Object value) {
		return (value != null && BeanUtils.isSimpleValueType(value.getClass()));
	}
	
	protected void downloadXls(String downloadType, HttpServletResponse response){
		if(StringUtils.isNotBlank(downloadType)) download("xls", response);
	}
	protected void download(String downloadType, HttpServletResponse response){
		if(StringUtils.equals(downloadType, "xls")){
			response.setContentType("application/xls");
		}else if (StringUtils.equals(downloadType, "jpg")) {
			response.setContentType("image/jpeg");
		}else{
			response.setContentType("application/x-download");
		}
		response.addHeader("Content-Disposition", "attachment;filename=gewara"+DateUtil.format(new Date(), "yyMMdd_HHmmss")+ "." + downloadType);
	}
	/**
	 * 只接受Get提交的参数跳转
	 * @param targetUrl
	 * @param request
	 * @param model
	 * @return
	 */
	protected String gotoLogin(String targetUrl, HttpServletRequest request, ModelMap model){
		try {
			if(StringUtils.isNotBlank(targetUrl)){
				String queryStr = request.getQueryString();//只接受Get方法
				String paramStr = ""; 
				if(StringUtils.isNotBlank(queryStr) && StringUtils.length(queryStr) < 300){//300以下的转发
					paramStr = URLDecoder.decode(queryStr, "utf-8");
				}
				targetUrl += targetUrl.indexOf('?')>0?"&" + paramStr:"?" + paramStr;
				
				return showRedirect("login.xhtml?TARGETURL=" + URLEncoder.encode(targetUrl, "utf-8"), model);
			}
		} catch (UnsupportedEncodingException e) {//ignore
		}
		return showRedirect("login.xhtml", model);
	}
	protected void addCacheMember(ModelMap model, List<Long> memberidList) {
		Map<Long, Map> cacheMemberMap = (Map<Long, Map>) model.get("cacheMemberMap");
		if(cacheMemberMap==null){
			cacheMemberMap = new HashMap<Long, Map>();
			model.put("cacheMemberMap", cacheMemberMap);
		}
		Set<Long> idSet = new HashSet<Long>(memberidList);
		idSet.removeAll(cacheMemberMap.keySet());
		Map<Long, Map> newinfoMap = memberService.getCacheMemberInfoMap(idSet);
		cacheMemberMap.putAll(newinfoMap);
	}
	protected void addCacheMember(ModelMap model, Long memberid) {
		Map<Long, Map> cacheMemberMap = (Map<Long, Map>) model.get("cacheMemberMap");
		if(cacheMemberMap==null){
			cacheMemberMap = new HashMap<Long, Map>();
			model.put("cacheMemberMap", cacheMemberMap);
		}
		Map singleInfo = memberService.getCacheMemberInfoMap(memberid);
		cacheMemberMap.put(memberid, singleInfo);
	}
	protected HttpServletRequest getRequest(){
		ServletRequestAttributes holder = (ServletRequestAttributes)RequestContextHolder.getRequestAttributes();
		if(holder!=null){
			HttpServletRequest request = holder.getRequest();
			if(request!=null){
				return request;
			}
		}
		return null;
	}
}
