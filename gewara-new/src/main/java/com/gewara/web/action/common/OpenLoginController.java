package com.gewara.web.action.common;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.gewara.Config;
import com.gewara.constant.MemberConstant;
import com.gewara.constant.content.OpenShareConstant;
import com.gewara.model.user.Member;
import com.gewara.model.user.MemberInfo;
import com.gewara.model.user.OpenMember;
import com.gewara.pay.AlipayUtil;
import com.gewara.pay.ChinapayUtil;
import com.gewara.pay.UnionpayWalletUtil;
import com.gewara.service.OperationService;
import com.gewara.support.ErrorCode;
import com.gewara.untrans.GewaPicService;
import com.gewara.util.DateUtil;
import com.gewara.util.HttpResult;
import com.gewara.util.HttpUtils;
import com.gewara.util.JsonUtils;
import com.gewara.util.PKCoderUtil;
import com.gewara.util.PictureUtil;
import com.gewara.util.QQOauthUtil;
import com.gewara.util.RequestCallback;
import com.gewara.util.StringUtil;
import com.gewara.util.VmUtils;
import com.gewara.util.WebUtils;
import com.gewara.util.YoukuOAuthUtils;
import com.gewara.util.YoukuOAuthUtils.Commit;
import com.gewara.util.YoukuOAuthUtils.Create;
import com.gewara.util.YoukuOAuthUtils.YoukuAccessToken;
import com.gewara.web.action.AnnotationController;
import com.google.gdata.client.douban.DoubanService;
import com.google.gdata.data.douban.UserEntry;
import com.mime.qweibo.examples.QWeiboSyncApi;

@Controller
public class OpenLoginController extends AnnotationController {
	public static final String RENREN_APIKEY = "f289c92b56204a228848d69c308daa80";
	public static final String RENREN_SECRET = "da297e0e38d340bca46b3812c7038ff7";
	public static final String KAIXIN_APPKEY = "893764559147369cb55f7bd67217a50a";
	public static final String KAIXIN_SECRET = "3d095b623657969adf2c197973c93838";
	public static final String MSN_APIKEY = "000000004005F749";
	public static final String MSN_SECRET = "uuqKeYuFKqqZrxmNUSoj9nPGbybQjFUV";
	public static final String DB_APIKEY = "08dd9988d95ea0711a98cc9a8c1be153";
	public static final String DB_SECRET = "f61d048f2ad3b892";
	public static final String TB_APIKEY = "12657056";
	public static final String TB_SECRET = "adfbf757c2494502f80cc3132be00a70";
	//139邮箱联名登录配置
	public static final String REDIRECT_139EMAIL_URL = "http://openlogin.mail.10086.cn/webopenlogin.ashx";//test:"http://openlogin.mail.10086ts.cn:9001/webopenlogin.ashx";
	public static final String CLICK_139EMAIL_ID = "1087";//test:"1077";
	public static final String KEY_139EMAIL = "e0dbe7f2-1862-46fd-9875-4faf4c20a54a";//test:"testmail139Rcx7T26K5^#L";
	// 应用获取Access_token的URL
	public static final String RENREN_OAUTH_ACCESS_TOKEN_URL = "https://graph.renren.com/oauth/token";
	// 应用获取人人网sessionkey的URL
	public static final String RENREN_API_SESSIONKEY_URL = "http://graph.renren.com/renren_api/session_key";
	// MSN登录
	public static final String MSN_API_AUTH_URL = "https://consent.live.com/Connect.aspx?wrap_client_id=" + MSN_APIKEY
			+ "&wrap_scope=WL_Profiles.View&wrap_callback=http://test5.gewala.net/login/msnCallBack.xhtml";

	@Autowired
	@Qualifier("operationService")
	private OperationService operationService;

	public void setOperationService(OperationService operationService) {
		this.operationService = operationService;
	}
	@Autowired@Qualifier("gewaPicService")
	private GewaPicService gewaPicService;

	@Autowired
	@Qualifier("config")
	private Config config;

	public void setConfig(Config config) {
		this.config = config;
	}

	@RequestMapping("/login/sdoLogin.xhtml")
	public String sdoLogin(String action, ModelMap model) throws IOException {
		String service = config.getAbsPath() + "/login/sdoCallback.xhtml";
		String location = config.getAbsPath() + "/login/sdoLogin.xhtml?action=callback";
		model.put("service", URLEncoder.encode(service, "utf-8"));
		model.put("action", action);
		model.put("location", URLEncoder.encode(location, "utf-8"));
		return "sdoLogin.vm";
	}

	@RequestMapping("/login/sdoCallback.xhtml")
	public String sdoLogin(HttpServletRequest request, HttpServletResponse response, String ticket, ModelMap model) throws IOException {
		String service = config.getAbsPath() + config.getBasePath() + "login/sdoCallback.xhtml";
		String url = "https://cas.sdo.com/cas/Validate.Ex?service=" + URLEncoder.encode(service, "utf-8") + "&ticket=" + ticket
				+ "&appId=700016700&appArea=0";
		HttpResult result = HttpUtils.getUrlAsString(url);
		String[] ret = StringUtils.split(result.getResponse(), "\n");
		if (VmUtils.isEmptyList(ret) || ret.length != 2){
			dbLogger.error(WebUtils.getParamStr(request, true) + WebUtils.getHeaderStr(request) + ",validResult:" + result.getResponse());			
			return showError(model, "error");
		}
		if (StringUtils.equals("yes", ret[0])) {
			OpenMember om = memberService.getOpenMemberByLoginname(MemberConstant.SOURCE_SDO, ret[1]);
			if (om == null) {
				om = memberService.createOpenMember(WebUtils.getAndSetDefault(request, response), MemberConstant.SOURCE_SDO,
						MemberConstant.SHORT_SDO, ret[1], WebUtils.getRemoteIp(request));
				MemberInfo memberInfo = daoService.getObject(MemberInfo.class, om.getMemberid());
				memberInfo.setOtherinfo(JsonUtils.addJsonKeyValue(memberInfo.getOtherinfo(), "openMember", MemberConstant.SOURCE_SDO));
				daoService.updateObject(memberInfo);
			}
			ErrorCode loginResult = loginService.autoLogin(request, response, om);
			if (loginResult.isSuccess()) {
				model.put("loginResult", "success");
				return "openLoginResult.vm";
			} else {
				dbLogger.error("sdoCallback ---> login is error :" + loginResult.getMsg());
				return show404(model, loginResult.getMsg());
			}
		}
		return show404(model, "登录失败，请重新登录！");
	}

	@RequestMapping("/login/alipayLogin.xhtml")
	public String alipayLogin(ModelMap model) {
		String returnUrl = config.getAbsPath() + config.getBasePath() + "login/alipayCallback.xhtml";
		String url = AlipayUtil.getShareUrl(returnUrl);
		return showRedirect(url, model);
	}

	@RequestMapping("/login/alipayCallback.xhtml")
	public String alipayCallback(HttpServletRequest request, HttpServletResponse response, String sign, String user_id, ModelMap model) {
		dbLogger.error(WebUtils.getParamStr(request, true));
		String responseTxt = AlipayUtil.check(request.getParameter("notify_id"));
		Map signMap = new HashMap(request.getParameterMap());
		String mysign = AlipayUtil.sign(signMap);
		dbLogger.warn("支付宝联名登录：mysign:" + mysign + ", sign:" + sign + ", responseText:" + responseTxt);
		if (mysign.equals(sign) && responseTxt.equals("true")) {
			OpenMember om = memberService.getOpenMemberByLoginname(MemberConstant.SOURCE_ALIPAY, user_id);
			if (om == null) {
				return createOpenMember(null, null , model, request, response, MemberConstant.SOURCE_ALIPAY, user_id, WebUtils.getRemoteIp(request), null);
			}
			ErrorCode loginResult = loginService.autoLogin(request, response, om);
			if (loginResult.isSuccess()) {
				return "redirect:/index.xhtml";
			} else {
				dbLogger.error("alipayCallback ---> login is error :" + loginResult.getMsg() + loginResult.getRetval());
				return show404(model, loginResult.getMsg());
			}
		} else {
			return show404(model, "登录失败，请重新登录！");
		}
	}
	
	private OpenMember creatAlipayOpenMember(HttpServletRequest request, HttpServletResponse response,String user_id){
		OpenMember om = memberService.getOpenMemberByLoginname(MemberConstant.SOURCE_ALIPAY, user_id);
		if (om == null) {
			om = memberService.createOpenMember(WebUtils.getAndSetDefault(request, response), MemberConstant.SOURCE_ALIPAY,
					MemberConstant.SHORT_ALIPAY, user_id, WebUtils.getRemoteIp(request));
			MemberInfo memberInfo = daoService.getObject(MemberInfo.class, om.getMemberid());
			memberInfo.setOtherinfo(JsonUtils.addJsonKeyValue(memberInfo.getOtherinfo(), "openMember", MemberConstant.SOURCE_ALIPAY));
			daoService.updateObject(memberInfo);
		}
		daoService.saveObject(om);
		return om;
	}
	
	@RequestMapping("/login/alipayNotify.xhtml")
	@ResponseBody
	public String alipayNotify(HttpServletRequest request, HttpServletResponse response,String sign,String userType, String user_id) {
		if(StringUtils.isBlank(user_id)){
			return "4000|userID为空";
		}
		dbLogger.error("支付宝联名登录： " + WebUtils.getParamStr(request, true));
		if (AlipayUtil.rsaSign(user_id,userType,sign)) {
			OpenMember om = creatAlipayOpenMember(request, response, user_id);
			if(om != null){
				return "9000|回调成功";
			}else{
				return "4000|系统异常";
			}
		} else {
			return "4000|验签失败";
		}
	}

	@RequestMapping("/login/sinaLogin.xhtml")
	public void sinaLogin(HttpServletResponse response, ModelMap model) {
		String callbackUrl = config.getAbsPath() + config.getBasePath() + "login/sinaLoginCallBack.xhtml";
		String url = OpenShareConstant.WEIBO_OAUTH_URL + "?client_id="+OpenShareConstant.WEIBO_APPKEY+"&redirect_uri=" + callbackUrl+"&forcelogin=true";
		try {
			response.sendRedirect(url);
		} catch (IOException e) {
			forwardMessage(model, "非法登录！");
		}
	}

	@RequestMapping("/login/sinaLoginCallBack.xhtml")
	public String sinaLogin(ModelMap model, HttpServletRequest request, HttpServletResponse response) {
		Map<String, String> params = WebUtils.getRequestMap(request);
		String callbackUrl = config.getAbsPath() + config.getBasePath() + "login/sinaLoginCallBack.xhtml";
		try {
			params.put("client_id", OpenShareConstant.WEIBO_APPKEY);
			params.put("client_secret", OpenShareConstant.WEIBO_SECRET);
			params.put("grant_type", "authorization_code");
			params.put("redirect_uri", callbackUrl);
			HttpResult result = HttpUtils.postUrlAsString(OpenShareConstant.WEIBO_OAUTH_ACCESS_URL, params);
			if(!result.isSuccess()) return show404(model, "登录失败，请重新登录！");
			Map<String, String> dataMap = JsonUtils.readJsonToMap(result.getResponse());
			String userid = dataMap.get("uid");
			if(StringUtils.isBlank(userid)){
				String msg = dataMap.get("error_description") == null ? "授权失败，请重新登录！": dataMap.get("error_description");
				return show404(model, msg);
			}
			String token = dataMap.get("access_token");
			
			//TODO: tokensecret
			String tokensecret = "";
			OpenMember om = memberService.getOpenMemberByLoginname(MemberConstant.SOURCE_SINA, userid);
			if (om == null){
				Map<String,String> qShowMap = new HashMap<String, String>();
				qShowMap.put("access_token", token);
				qShowMap.put("uid", userid);
				HttpResult infoResult = HttpUtils.getUrlAsString(OpenShareConstant.WEIBO_OAUTH_GET_USERSHOW, qShowMap);
				Map<String, Object> userInfoMap = new HashMap<String, Object>();
				if(result.isSuccess()){
					userInfoMap = JsonUtils.readJsonToMap(infoResult.getResponse());
				}
				return createOpenMember(token, tokensecret, model, request, response, MemberConstant.SOURCE_SINA, userid, WebUtils.getRemoteIp(request), userInfoMap);
			}
			Map<String, String> otherInfo = VmUtils.readJsonToMap(om.getOtherinfo());
			if(!StringUtils.equals(otherInfo.get("token"), token) || !StringUtils.equals("tokensecret", tokensecret)){
				otherInfo.put("token", token);
				otherInfo.put("tokensecret", tokensecret);
				om.setOtherinfo(JsonUtils.writeMapToJson(otherInfo));
				daoService.saveObject(om);
			}
			ErrorCode loginResult = loginService.autoLogin(request, response, om);
			if (loginResult.isSuccess()) {
				model.put("loginResult", "success");
				return "openLoginResult.vm";
			} else {
				dbLogger.error("sinaCallback ---> login is error :" + loginResult.getMsg());
				return showMessage(model, loginResult.getMsg());
			}
		} catch (Exception e) {
			dbLogger.error(StringUtil.getExceptionTrace(e, 5));
			return show404(model, "服务器忙，请重新登录！");
		}
	}

	@RequestMapping("/login/taobaoLogin.xhtml")
	public void taobaoLogin(HttpServletResponse response) {
		String redirect = "http://www.gewara.com/login/taobaoCallBack.xhtml";
		String url = "https://oauth.taobao.com/authorize?response_type=user&client_id=" + TB_APIKEY + "&redirect_uri=" + redirect;
		try {
			response.sendRedirect(url);
		} catch (Exception e) {
			dbLogger.error("", e);
		}
	}

	@RequestMapping("/login/taobaoCallBack.xhtml")
	public String taobaoCallBack(String top_parameters, String top_sign, HttpServletRequest request, HttpServletResponse response, ModelMap model){
		if (StringUtils.isBlank(top_parameters) || StringUtils.isBlank(top_sign))
			return showMessageAndReturn(model, request, "登录失败，请重新登录！");
		Map<String, String> map = new HashMap<String, String>();
		String mycheck = getCheckString(Arrays.asList(top_parameters, TB_SECRET));
		if(!StringUtils.equalsIgnoreCase(mycheck, top_sign)) 
			return showMessageAndReturn(model, request, "登录失败，请重新登录！");
		try {
			String keyvalues = new String(Base64.decodeBase64(URLDecoder.decode(top_parameters, "UTF-8").getBytes("UTF-8")));
			String[] keyvalueArray = keyvalues.split("\\&");
			for (String keyvalue : keyvalueArray) {
				String[] s = keyvalue.split("\\=");
				if (s == null || s.length != 2)
					return showMessageAndReturn(model, request, "登录失败，请重新登录！");
				map.put(s[0], s[1]);
			}
		}catch(Exception e){
			dbLogger.warn("", e);
		}
		if(map.get("nick") == null) showMessageAndReturn(model, request, "登录失败，请重新登录！");
		String loginName = map.get("nick");
		OpenMember om = memberService.getOpenMemberByLoginname(MemberConstant.SOURCE_TAOBAO, loginName);
		if (om == null){
			String citycode = WebUtils.getAndSetDefault(request, response);
			om = memberService.createOpenMember(citycode, MemberConstant.SOURCE_TAOBAO, MemberConstant.SOURCE_TAOBAO, loginName, WebUtils.getRemoteIp(request));
			MemberInfo memberInfo = daoService.getObject(MemberInfo.class, om.getMemberid());
			Map<String, String> otherInfoMap = VmUtils.readJsonToMap(memberInfo.getOtherinfo());
			otherInfoMap.put("openMember", MemberConstant.SOURCE_TAOBAO);
			memberInfo.setOtherinfo(JsonUtils.writeMapToJson(otherInfoMap));
			daoService.updateObject(memberInfo);
		}	
		ErrorCode loginResult = loginService.autoLogin(request, response, om);
		if (loginResult.isSuccess()) {
			return "redirect:/index.xhtml";
		} else {
			dbLogger.error("taobaoCallback ---> login is error :" + loginResult.getMsg());
			return showMessage(model, loginResult.getMsg());
		}
	}
	
	private String getCheckString(List<String> strList){
		String result = "";
		if(strList == null) return result;
		StringBuilder buffer = new StringBuilder();
		for(String str : strList){
			buffer.append(str);
		}
		byte[] bytes;
		try {
			bytes = MessageDigest.getInstance("MD5").digest(buffer.toString().getBytes("UTF-8"));
			result = Base64.encodeBase64String(bytes);
		} catch (Exception e) {
			dbLogger.error("", e);
		} 
		return result;
	}

	// tencent(QQ) 同步登入
	@RequestMapping("/login/qqWebLogin.xhtml")
	public void qqWebLogin(HttpServletResponse response) {
		QWeiboSyncApi qApi = new QWeiboSyncApi();
		String callBackUrl = config.getAbsPath() + config.getBasePath() + "login/qqWebCallBack.xhtml";
		String resStr = qApi.getRequestToken(callBackUrl);
		if (StringUtils.isBlank(resStr))
			return;
		String[] StrArray = resStr.split("&");
		String strTokenKey = "", strTokenScrect = "";
		if (StrArray.length >= 2) {
			strTokenKey = StrArray[0].split("=")[1];
			strTokenScrect = StrArray[1].split("=")[1];
		}
		String cookieStr = PKCoderUtil.encodeString(strTokenKey + "@@" + strTokenScrect);
		Cookie cookie = new Cookie("rToken", cookieStr);
		cookie.setMaxAge(60 * 5);
		response.addCookie(cookie);
		try {
			response.sendRedirect("http://open.t.qq.com/cgi-bin/authorize?oauth_token=" + strTokenKey);
		} catch (IOException e) {
			dbLogger.error("", e);
		}
	}

	@RequestMapping("/login/qqWebCallBack.xhtml")
	public String qqWebCallBack(ModelMap model, HttpServletRequest request, HttpServletResponse response,
			@CookieValue(required = false) String rToken, String oauth_verifier) {
		Cookie cookie = new Cookie("rToken", "");
		cookie.setMaxAge(0);
		response.addCookie(cookie);
		if (StringUtils.isBlank(rToken))
			return show404(model, "登录失败，请重新登录！");
		String[] rEoder = PKCoderUtil.decodeString(rToken).split("@@");
		String resTokenKey = rEoder[0];
		String resTokenSerect = rEoder[1];
		QWeiboSyncApi qApi = new QWeiboSyncApi();
		try {
			String accesToken = qApi.getAccessToken(resTokenKey, resTokenSerect, oauth_verifier);
			String[] aToken = accesToken.split("&");
			String aTokenKey = "", aTokenSerect = "", loginName = "";
			if (aToken.length >= 3) {
				aTokenKey = aToken[0].split("=")[1];
				aTokenSerect = aToken[1].split("=")[1];
				loginName = aToken[2].split("=")[1];
			}
			OpenMember om = memberService.getOpenMemberByLoginname(MemberConstant.SOURCE_QQ, loginName);
			if (om == null){
				String citycode = WebUtils.getAndSetDefault(request, response);
				om = memberService.createOpenMember(citycode, MemberConstant.SOURCE_QQ, MemberConstant.SHORT_QQ, loginName, WebUtils.getRemoteIp(request));
				MemberInfo memberInfo = daoService.getObject(MemberInfo.class, om.getMemberid());
				Map<String, String> otherInfoMap = VmUtils.readJsonToMap(memberInfo.getOtherinfo());
				otherInfoMap.put("openMember", MemberConstant.SOURCE_QQ);
				memberInfo.setOtherinfo(JsonUtils.writeMapToJson(otherInfoMap));
				daoService.updateObject(memberInfo);
			}
			Map<String, String> otherInfo = VmUtils.readJsonToMap(om.getOtherinfo());
			if(!StringUtils.equals(otherInfo.get("token"), aTokenKey) || 
					!StringUtils.equals(otherInfo.get("tokensecret"), aTokenSerect)){
				otherInfo.put("token", aTokenKey);
				otherInfo.put("tokensecret", aTokenSerect);
				om.setOtherinfo(JsonUtils.writeMapToJson(otherInfo));
				daoService.saveObject(om);
			}
			ErrorCode loginResult = loginService.autoLogin(request, response, om);
			if (loginResult.isSuccess()) {
				model.put("loginResult", "success");
				return "openLoginResult.vm";
			} else {
				dbLogger.error("qqWebCallback ---> login is error :" + loginResult.getMsg());
				return show404(model, loginResult.getMsg());
			}
		} catch (Exception e) {
			dbLogger.error(e.getMessage());
			return show404(model, "服务器忙，请重新登录！");
		}
	}

	public String getSignature(List<String> paramList, String secret) {
		Collections.sort(paramList);
		StringBuilder buffer = new StringBuilder();
		for (String param : paramList) {
			buffer.append(param); // 将参数键值对，以字典序升序排列后，拼接在一起
		}
		buffer.append(secret); // 符串末尾追加上应用的Secret Key
		try {// 下面是将拼好的字符串转成MD5值，然后返回
			java.security.MessageDigest md = java.security.MessageDigest.getInstance("MD5");
			StringBuilder result = new StringBuilder();
			try {
				for (byte b : md.digest(buffer.toString().getBytes("UTF-8"))) {
					result.append(Integer.toHexString((b & 0xf0) >>> 4));
					result.append(Integer.toHexString(b & 0x0f));
				}
			} catch (UnsupportedEncodingException e) {
				for (byte b : md.digest(buffer.toString().getBytes())) {
					result.append(Integer.toHexString((b & 0xf0) >>> 4));
					result.append(Integer.toHexString(b & 0x0f));
				}
			}
			return result.toString();
		} catch (java.security.NoSuchAlgorithmException ex) {
			ex.printStackTrace();
		}
		return "";
	}

	// kaixin登录
	@RequestMapping("/login/kaixinLogin.xhtml")
	public void kaixinLogin(HttpServletResponse response) {
		String redirect = config.getBasePath() + "login/kaixinBackCall.xhtml";
		String url = "http://www.kaixin001.com/login/connect.php?appkey=" + KAIXIN_APPKEY + "&re=" + redirect + "&t=7";
		try {
			response.sendRedirect(url);
		} catch (IOException e) {
			dbLogger.error("", e);
		}
	}

	@RequestMapping("/login/kaixinBackCall.xhtml")
	public String kaixinCallBack(String session_key, HttpServletRequest request, HttpServletResponse response, ModelMap model) {
		String url = "http://rest.kaixin001.com/api/rest.php";
		Map<String, String> params = new HashMap<String, String>();
		params.put("api_key", KAIXIN_APPKEY);
		params.put("call_id", System.currentTimeMillis() + "");
		params.put("method", "users.getLoggedInUser");
		params.put("v", "1.0");
		params.put("session_key", session_key);
		List<String> paramsKeyList = Arrays.asList("api_key", "call_id", "method", "session_key", "v");
		String request_str = "";
		for (String string : paramsKeyList) {
			request_str += string + "=" + params.get(string);
		}
		String sig = StringUtil.md5(request_str + KAIXIN_SECRET);
		params.put("sig", sig);
		HttpResult result = HttpUtils.postUrlAsString(url, params, null, "GB2312");
		Map dataMap = JsonUtils.readJsonToMap(result.getResponse());
		if (StringUtils.isNotBlank(dataMap.get("result") + "")) {// 成功抓取数据
			OpenMember om = memberService.getOpenMemberByLoginname(MemberConstant.SOURCE_KAIXIN, dataMap.get("result") + "");
			if (om == null){
				om = memberService.createOpenMember(WebUtils.getAndSetDefault(request, response), MemberConstant.SOURCE_KAIXIN,
						MemberConstant.SHORT_KAIXIN, dataMap.get("result") + "", WebUtils.getRemoteIp(request));
				MemberInfo memberInfo = daoService.getObject(MemberInfo.class, om.getMemberid());
				Map<String, String> otherInfoMap = VmUtils.readJsonToMap(memberInfo.getOtherinfo());
				otherInfoMap.put("openMember", MemberConstant.SOURCE_KAIXIN);
				memberInfo.setOtherinfo(JsonUtils.writeMapToJson(otherInfoMap));
				daoService.updateObject(memberInfo);
			}
			ErrorCode loginResult = loginService.autoLogin(request, response, om);
			if (loginResult.isSuccess()) {
				return "redirect:/index.xhtml";
			} else {
				dbLogger.error("kaixinCallback ---> login is error :" + loginResult.getMsg());
				return show404(model, loginResult.getMsg());
			}
		}
		return "";
	}


	// msn登录
	@RequestMapping("/login/msnLogin.xhtml")
	public void msnLogin(HttpServletResponse response) {
		try {
			response.sendRedirect(MSN_API_AUTH_URL);
		} catch (Exception e) {
			dbLogger.error(StringUtil.getExceptionTrace(e, 5));
		}
	}

	@RequestMapping("/login/msnCallBack.xhtml")
	public String msnCallBack(String wrap_verification_code, HttpServletRequest request, HttpServletResponse response, ModelMap model) {
		String post_url = "https://consent.live.com/AccessToken.aspx";
		Map<String, String> params = new HashMap<String, String>();
		params.put("wrap_client_id", MSN_APIKEY);
		params.put("wrap_client_secret", MSN_SECRET);
		params.put("wrap_callback", "http://www.gewara.com/login/msnCallBack.xhtml");
		params.put("idtype", "CID");
		params.put("wrap_verification_code", wrap_verification_code);
		HttpResult result = HttpUtils.postUrlAsString(post_url, params);
		if (result.getMsg() != null)
			return show404(model, "服务器忙，请重新登录！");
		String[] aValue = result.getResponse().split("&");
		String access_Token = aValue[0].split("=")[1];
		String get_url = "https://apis.live.net/v5.0/" + aValue[4].split("=")[1];
		Map<String, String> params2 = new HashMap<String, String>();
		params2.put("wrap_access_token", access_Token);
		HttpResult userData = HttpUtils.getUrlAsString(get_url, params2);
		if (userData.getMsg() == null)
			return show404(model, "服务器忙，请重新登录！");
		Map dataMap = JsonUtils.readJsonToMap(userData.getResponse());
		try {
			OpenMember om = memberService.getOpenMemberByLoginname(MemberConstant.SOURCE_MSN, dataMap.get("id") + "");
			if (om == null){
				om = memberService.createOpenMember(WebUtils.getAndSetDefault(request, response), MemberConstant.SOURCE_MSN,
						MemberConstant.SHORT_MSN, dataMap.get("id") + "", WebUtils.getRemoteIp(request));
				om.setOtherinfo("userid:" + dataMap.get("id"));
				daoService.saveObject(om);
				MemberInfo memberInfo = daoService.getObject(MemberInfo.class, om.getMemberid());
				Map<String, String> otherInfoMap = VmUtils.readJsonToMap(memberInfo.getOtherinfo());
				otherInfoMap.put("openMember", MemberConstant.SOURCE_MSN);
				memberInfo.setOtherinfo(JsonUtils.writeMapToJson(otherInfoMap));
				daoService.updateObject(memberInfo);
			}
			ErrorCode loginResult = loginService.autoLogin(request, response, om);
			if (loginResult.isSuccess()) {
				return "redirect:/index.xhtml";
			} else {
				dbLogger.error("msnCallback ---> login is error :" + loginResult.getMsg());
				return showMessage(model, loginResult.getMsg());
			}
		} catch (Exception e) {
			dbLogger.error(e.getMessage());
			return show404(model, "服务器忙，请重新登录！");
		}
	}

	// QQ 登录
	@RequestMapping("/login/qqLogin.xhtml")
	public void qqLogin(HttpServletResponse response, HttpServletRequest request) throws Exception {
		String redirect_url = "http://openapi.qzone.qq.com/oauth/qzoneoauth_authorize";
		final String callback = config.getAbsPath() + config.getBasePath() + "login/qqCallback.xhtml";
		HttpResult result = HttpUtils.getUrlAsString(QQOauthUtil.REQUEST_URL, QQOauthUtil.getRequestToken());
		Map<String, String> tokens = QQOauthUtil.parseTokenString(result.getResponse());
		String oauth_token = tokens.get("oauth_token");
		String oauth_token_secret = tokens.get("oauth_token_secret");
		Cookie cookie = WebUtils.getCookie(request, "oauth_token_secret");
		if (cookie == null || !StringUtils.equals(cookie.getValue(), oauth_token_secret)) {
			cookie = new Cookie("oauth_token_secret", oauth_token_secret);
		}
		cookie.setPath("/");
		cookie.setMaxAge(60 * 60 * 24);
		cookie.setHttpOnly(true);
		response.addCookie(cookie);
		redirect_url += "?oauth_consumer_key=" + QQOauthUtil.APP_ID;
		redirect_url += "&oauth_token=" + oauth_token;
		redirect_url += "&oauth_callback=" + URLEncoder.encode(callback, "UTF-8");
		response.sendRedirect(redirect_url);
	}

	@RequestMapping("/login/qqCallback.xhtml")
	public String qqCallBack(ModelMap model, HttpServletRequest request, HttpServletResponse response) throws Exception {
		String ip = WebUtils.getRemoteIp(request);
		String opkey = "qqLogin" + ip;
		if (!operationService.updateOperation(opkey, 40))
			show404(model, "操作不能太频繁，请稍后再试！");
		Cookie cookie = WebUtils.getCookie(request, "oauth_token_secret");
		if (cookie == null)
			return alertMessage(model, "服务器忙，请重新登录！", "login/qqLogin.xhtml");
		String oauth_token = request.getParameter("oauth_token");
		String openid = request.getParameter("openid");
		String oauth_signature = request.getParameter("oauth_signature");
		String oauth_vericode = request.getParameter("oauth_vericode");
		String timestamp = request.getParameter("timestamp");
		if (!QQOauthUtil.verifyOpenID(openid, timestamp, oauth_signature))
			return alertMessage(model, "服务器忙，请重新登录！", "login/qqLogin.xhtml");
		String oauth_token_secret = cookie.getValue();
		HttpResult result = HttpUtils.getUrlAsString(QQOauthUtil.ACCESS_URL,
				QQOauthUtil.getAccessToken(oauth_token, oauth_token_secret, oauth_vericode));
		Map<String, String> tokens = QQOauthUtil.parseTokenString(result.getResponse());
		if (StringUtils.isNotBlank(tokens.get("error_code")))
			return alertMessage(model, "服务器忙，请重新登录！", "login/qqLogin.xhtml");
		if (!QQOauthUtil.verifyOpenID(tokens.get("openid"), tokens.get("timestamp"), tokens.get("oauth_signature")))
			return alertMessage(model, "服务器忙，请重新登录！", "login/qqLogin.xhtml");
		oauth_token = tokens.get("oauth_token");
		oauth_token_secret = tokens.get("oauth_token_secret");
		openid = tokens.get("openid");
		result = HttpUtils.getUrlAsString(QQOauthUtil.INFO_URL, QQOauthUtil.getInfoToken(oauth_token, oauth_token_secret, openid, "JSON"));
		Map<String, Object> jsonInfo = JsonUtils.readJsonToMap(result.getResponse());
		if (StringUtils.equals(String.valueOf(jsonInfo.get("ret")), "0")) {
			OpenMember om = memberService.getOpenMemberByLoginname(MemberConstant.SOURCE_TENCENT, openid);
			if (om == null){
				return createOpenMember(null, null , model, request, response, MemberConstant.SOURCE_TENCENT, openid, WebUtils.getRemoteIp(request), jsonInfo);
			}
			ErrorCode loginResult = loginService.autoLogin(request, response, om);
			if (loginResult.isSuccess()) {
				model.put("loginResult", "success");
				return "openLoginResult.vm";
			} else {
				dbLogger.error("qqCallback ---> login is error :" + loginResult.getMsg());
				return alertMessage(model, loginResult.getMsg(), "login/qqLogin.xhtml");
			}
		} else{
			return alertMessage(model, String.valueOf(jsonInfo.get("msg")), "login/qqLogin.xhtml");
		}
	}
	
	private String createOpenMember(String token,String tokensecret,ModelMap model,HttpServletRequest request,HttpServletResponse response,
			String source,String openid,String ip,Map<String,Object> jsonInfo){
		OpenMember om = new OpenMember();
		if (StringUtils.equals(source, MemberConstant.SOURCE_SINA)||StringUtils.equals(source, MemberConstant.SOURCE_TENCENT)) {
			if(jsonInfo!=null&&jsonInfo.get("nickname")!=null&&StringUtils.isNotBlank(String.valueOf(jsonInfo.get("nickname")))){
				om = memberService.createOpenMemberWithBaseInfo(WebUtils.getAndSetDefault(request, response), source, openid,ip,jsonInfo);
				processHeadPic(om.getMemberid());
			}else {
				om = memberService.createOpenMember(WebUtils.getAndSetDefault(request, response), source,
						StringUtils.equals(source, MemberConstant.SOURCE_SINA) ? MemberConstant.SHORT_SINA : MemberConstant.SHORT_TENCENT, openid,ip);
			}
		}else if (StringUtils.equals(source, MemberConstant.SOURCE_ALIPAY)||StringUtils.equals(source, MemberConstant.SOURCE_CHINAPAY)) {
			om = memberService.createOpenMember(WebUtils.getAndSetDefault(request, response), source,
					StringUtils.equals(source, MemberConstant.SOURCE_ALIPAY) ? MemberConstant.SHORT_ALIPAY : MemberConstant.SHORT_CHINAPAY, openid,ip);
		}
		if (StringUtils.equals(source, MemberConstant.SOURCE_SINA)) {
			addTokenToSinaMbr(openid,token,tokensecret);
		}
		ErrorCode loginResult = loginService.autoLogin(request, response, om);
		if (loginResult.isSuccess()) {
			return "openMbrUpBase.vm";
		} else {
			dbLogger.error(source+"Callback ---> login is error :" + loginResult.getMsg());
			return alertMessage(model, loginResult.getMsg(), "login/"+source+"Login.xhtml");
		}
	}
	private void processHeadPic(Long memberid){
		final MemberInfo info = daoService.getObject(MemberInfo.class, memberid);
		if(StringUtils.startsWith(info.getHeadpic(), "http")){
			HttpUtils.getUrlAsInputStream(info.getHeadpic(), null, new RequestCallback(){
				@Override
				public boolean processResult(InputStream stream) {
					try {
						String fromPath = gewaPicService.saveToTempPic(stream, "png");
						gewaPicService.saveTempFileToRemote(fromPath);
						String filepath =  gewaPicService.moveRemoteTempTo(info.getId(), "member", info.getId() , PictureUtil.getHeadPicpath(), fromPath);
						filepath = filepath.replaceFirst("/","");
						File f = new File(fromPath);
						if(f.exists()) f.delete();
						info.setHeadpic(filepath);
						daoService.saveObject(info);
						return true;
					} catch (IOException e) {
						return false;
					}
				}
			});
		}
	}

	private void addTokenToSinaMbr(String openid,String token ,String tokensecret){
		OpenMember om = memberService.getOpenMemberByLoginname(MemberConstant.SOURCE_SINA, openid);
		Map<String, String> otherInfo = VmUtils.readJsonToMap(om.getOtherinfo());
		if(!StringUtils.equals(otherInfo.get("token"), token) || !StringUtils.equals("tokensecret", tokensecret)){
			otherInfo.put("token", token);
			otherInfo.put("tokensecret", tokensecret);
			om.setOtherinfo(JsonUtils.writeMapToJson(otherInfo));
			daoService.saveObject(om);
		}
	}

	@RequestMapping("/login/dbLogin.xhtml")
	public void dbLogin(HttpServletResponse response) throws Exception {
		final String callback_url = config.getAbsPath() + config.getBasePath() + "login/dbCallback.xhtml";
		DoubanService myService = new DoubanService("subApplication", DB_APIKEY, DB_SECRET);

		String redirect = myService.getAuthorizationUrl(callback_url);
		Cookie c = new Cookie("secret", myService.getRequestTokenSecret());
		response.addCookie(c);
		response.sendRedirect(redirect);
	}

	@RequestMapping("/login/dbCallback.xhtml")
	public String dbCallback(ModelMap model, HttpServletRequest request, HttpServletResponse response) {
		String requestToken = request.getParameter("oauth_token");
		if (StringUtils.isBlank(requestToken))
			return "redirect:/login/dbLogin.xhtml";

		Cookie cookie = WebUtils.getCookie(request, "secret");
		if (cookie == null)
			return alertMessage(model, "服务器忙，请重新登录！", "login/dbLogin.xhtml");
		try {
			DoubanService myService = new DoubanService("subApplication", DB_APIKEY, DB_SECRET);
			myService.setRequestTokenSecret(cookie.getValue());
			myService.setRequestToken(requestToken);
			myService.getAccessToken();
			UserEntry user = myService.getAuthorizedUser();
			if (user != null) {
				String uid = user.getUid();
				OpenMember om = memberService.getOpenMemberByLoginname(MemberConstant.SOURCE_DOUBAN, uid);
				if (om == null){
					String citycode = WebUtils.getAndSetDefault(request, response);
					om = memberService.createOpenMember(citycode, MemberConstant.SOURCE_DOUBAN, MemberConstant.SHORT_DOUBAN, uid, WebUtils.getRemoteIp(request));
					MemberInfo memberInfo = daoService.getObject(MemberInfo.class, om.getMemberid());
					memberInfo.setOtherinfo(JsonUtils.addJsonKeyValue(memberInfo.getOtherinfo(), "openMember", MemberConstant.SOURCE_DOUBAN));
					daoService.updateObject(memberInfo);
				}
				ErrorCode loginResult = loginService.autoLogin(request, response, om);
				if (loginResult.isSuccess()) {
					model.put("loginResult", "success");
					return "openLoginResult.vm";
				} else {
					dbLogger.error("dbCallback ---> login is error :" + loginResult.getMsg());
					return alertMessage(model, loginResult.getMsg(), "login/dbLogin.xhtml");
				}
			}
		} catch (Exception e) {
			return "redirect:/index.xhtml";
		}
		return "redirect:/login/dbLogin.xhtml";
	}
	/**
	 * 银联钱包联名登录
	 * @param model
	 * @return
	 */
	@RequestMapping("/login/unionpayWalletLogin.xhtml")
	public String chinapayWalletLogin(ModelMap model) {
		String returnUrl = config.getAbsPath() + config.getBasePath() + "login/unionpayWalletCallback.xhtml";
		model.put("sysIdStr", UnionpayWalletUtil.UNIONPAY_WALLET_SERVICE);
		model.put("userType", "cardholder");
		model.put("loginPage", "cardholder_small");
		model.put("registerurl",config.getAbsPath() + config.getBasePath() + "zhuanti/ajaxUnionpayWalletReg.xhtml");
		model.put("service", returnUrl);
		return showRedirect(UnionpayWalletUtil.UNIONPAY_WALLET_AUTHORIZE_CODE_URL + "sso/login", model);
	}

	@RequestMapping("/login/unionpayWalletCallback.xhtml")
	public String unionpayWalletCallback(String ticket, ModelMap model, 
			@CookieValue(value=LOGIN_COOKIE_NAME, required=false)String sessid,
			HttpServletRequest request, HttpServletResponse response) {
		String returnUrl = config.getAbsPath() + config.getBasePath() + "login/unionpayWalletCallback.xhtml";
		String userId = UnionpayWalletUtil.getUnionpayWalletUser(UnionpayWalletUtil.UNIONPAY_WALLET_AUTHORIZE_CODE_URL + "sso/proxyValidate", ticket, returnUrl);
		if(userId != null){
			OpenMember om = memberService.getOpenMemberByLoginname(MemberConstant.SOURCE_CHINAPAY,userId);
			if (om == null) {
				om = memberService.createOpenMember(WebUtils.getAndSetDefault(request, response), MemberConstant.SOURCE_CHINAPAY,
						MemberConstant.SHORT_CHINAPAY,userId, WebUtils.getRemoteIp(request));
				MemberInfo memberInfo = daoService.getObject(MemberInfo.class, om.getMemberid());
				memberInfo.setOtherinfo(JsonUtils.addJsonKeyValue(memberInfo.getOtherinfo(), "openMember", MemberConstant.SOURCE_CHINAPAY));
				daoService.updateObject(memberInfo);
			}
			Member member = loginService.getLogonMemberBySessid(WebUtils.getRemoteIp(request), sessid);
			if(member==null){
				ErrorCode loginResult = loginService.autoLogin(request, response, om);
				if (!loginResult.isSuccess()) {
					dbLogger.error("unionpayWalletCallback ---> login is error :" + loginResult.getMsg());
					return this.show404(model, loginResult.getMsg());
				}
			}
			Cookie cookie = new Cookie(LOGIN_COOKIE_NAME + "_wallet_user", userId);
			int duration = 60 * 60 * 12;
			cookie.setMaxAge(duration);
			cookie.setPath("/");
			cookie.setSecure(false);
			cookie.setHttpOnly(true);
			response.addCookie(cookie);
		}else{
			dbLogger.error("unionpayWalletCallback ---> login is error : 银联用户id获取失败");
			return this.show404(model,"银联用户标识获取失败");
		}
		return showRedirect(config.getAbsPath() + config.getBasePath() +  "zhuanti/ajaxUnionpayWallet.xhtml", model);
	}
	
	@RequestMapping("/login/chinapayLogin.xhtml")
	public String chinapayLogin(ModelMap model) {
		String returnUrl = config.getAbsPath() + config.getBasePath() + "login/unionpayCallback.xhtml";
		model.put("client_id", ChinapayUtil.CLIENT_ID);
		model.put("response_type", "code");
		model.put("redirect_uri", returnUrl);
		return showRedirect(ChinapayUtil.UNIONPAY_AUTHORIZE_CODE_URL, model);
	}

	@RequestMapping("/login/unionpayCallback.xhtml")
	public String unionpayCallback(String code, ModelMap model, HttpServletRequest request, HttpServletResponse response) {
		String returnUrl = config.getAbsPath() + config.getBasePath() + "login/unionpayCallback.xhtml";
		try {
			String accessToken = ChinapayUtil.getAccessToken(code, returnUrl);
			if (StringUtils.isBlank(accessToken))
				return "redirect:/index.xhtml";
			Map<String, String> userInfo = ChinapayUtil.getUserInfo(accessToken);
			if (userInfo == null)
				return "redirect:/index.xhtml";
			OpenMember om = memberService.getOpenMemberByLoginname(MemberConstant.SOURCE_CHINAPAY, userInfo.get("uid"));
			if (om == null) {
				return createOpenMember(null, null, model, request, response, MemberConstant.SOURCE_CHINAPAY, userInfo.get("uid"), WebUtils.getRemoteIp(request), null);
			}
			ErrorCode loginResult = loginService.autoLogin(request, response, om);
			if (loginResult.isSuccess()) {
				model.put("loginResult", "success");
				return "openLoginResult.vm";
			} else {
				dbLogger.error("unionpay ---> login is error :" + loginResult.getMsg());
				return alertMessage(model, loginResult.getMsg(), "login/unionpayLogin.xhtml");
			}
		} catch (Exception e) {
			return "redirect:/index.xhtml";
		}
	}
	@RequestMapping("/login/139Login.xhtml")
	public String email139Login(ModelMap model) {
		String returnUrl = config.getAbsPath() + config.getBasePath() + "login/139emailCallback.xhtml";
		model.put("clickSysId", CLICK_139EMAIL_ID);
		model.put("rType", 0);
		model.put("rUrl", returnUrl);
		long timestamp = System.currentTimeMillis()/1000 + 60 * 5 - DateUtil.parseTimestamp("2000-01-01 00:00:00").getTime()/1000;
		model.put("timestamp",timestamp);
		model.put("mKey",StringUtil.md5(CLICK_139EMAIL_ID + "0" + urlencode_rfc3986(returnUrl) + timestamp + KEY_139EMAIL).toUpperCase());
		return showRedirect(REDIRECT_139EMAIL_URL, model);
	}
	
	@RequestMapping("/login/139emailCallback.xhtml")
	public String email139Callback(String clickSysId,String userAccount,String rType,String rUrl,String mKey,String timestamp,ModelMap model, HttpServletRequest request, HttpServletResponse response){
		//usertoken
		if(StringUtils.isBlank(userAccount)){
			return "redirect:/index.xhtml";
		}
		if(!CLICK_139EMAIL_ID.equals(clickSysId)){
			return alertMessage(model, "非法来源", "login/139Login.xhtml");
		}
		if(!StringUtil.md5(clickSysId + userAccount + rType + urlencode_rfc3986(rUrl) + timestamp + KEY_139EMAIL).toUpperCase().equals(mKey)){
			return alertMessage(model, "非法来源,请求信息被篡改", "login/139Login.xhtml");
		}
		OpenMember om = memberService.getOpenMemberByLoginname(MemberConstant.SOURCE_139EMAIL, userAccount);
		if (om == null) {
			om = memberService.createOpenMember(WebUtils.getAndSetDefault(request, response), MemberConstant.SOURCE_139EMAIL,
					MemberConstant.SHORT_139EMAIL, userAccount, WebUtils.getRemoteIp(request));
			MemberInfo memberInfo = daoService.getObject(MemberInfo.class, om.getMemberid());
			memberInfo.setOtherinfo(JsonUtils.addJsonKeyValue(memberInfo.getOtherinfo(), "openMember", MemberConstant.SOURCE_139EMAIL));
			daoService.updateObject(memberInfo);
		}
		ErrorCode loginResult = loginService.autoLogin(request, response, om);
		if (loginResult.isSuccess()) {
			model.put("loginResult", "success");
			return "redirect:/index.xhtml";
			//return "openLoginResult.vm";
		} else {
			dbLogger.error("139email ---> login is error :" + loginResult.getMsg());
			return alertMessage(model, loginResult.getMsg(), "login/139emailCallback.xhtml");
		}
	}
	
	private String  urlencode_rfc3986(String value) {
        String encoded = null;
        try {
            encoded = URLEncoder.encode(value, "gb2312");
        } catch (UnsupportedEncodingException ignore) {
        }
        StringBuffer buf = new StringBuffer(encoded.length());
        char focus;
        for (int i = 0; i < encoded.length(); i++) {
            focus = encoded.charAt(i);
            if (focus == '*') {
                buf.append("%2A");
            } else if (focus == '+') {
                buf.append("%20");
            } else if (focus == '%' && (i + 1) < encoded.length()
                    && encoded.charAt(i + 1) == '7' && encoded.charAt(i + 2) == 'E') {
                buf.append('~');
                i += 2;
            } else {
                buf.append(focus);
            }
        }
        return buf.toString();
	}
	
	
	/**
	 * 转到优酷视频授权页
	 * @param model
	 * @return
	 */
	@RequestMapping("/oauth/youkuOAuth.xhtml")
	public String youkuOAuth(ModelMap model) {
		model.put("oauth_url", YoukuOAuthUtils.getOAuthRequestUrl(null));
		return "admin/common/youkuOAuth.vm";
	}
	
	
	/**
	 * 优酷授权回调
	 * @param model
	 * @return
	 */
	@RequestMapping("/oauth/youkuOAuthCallBack.xhtml")
	public String youkuOAuthCallBack(
			@RequestParam(required=true,value="code")String code,
			ModelMap model) {
		YoukuAccessToken token=YoukuOAuthUtils.getAccessToken(code);
		if(null!=token){
			return showMessage(model, token.getAccess_token());
		}
		return showMessage(model, "授权失败");
	}
	
	@RequestMapping("/movie/youkuVideoUpload.xhtml")
	public String youkuVideoUpload(){
		return "admin/common/youkuVideoUpload.vm";
	}
	
	@RequestMapping("/movie/youkuWebCreate.xhtml")
	public String youkuWebCreate(String access_token, String title, String tags, String category, String copyright_type,
			String public_type, String watch_password, String description, String file_name, ModelMap model){
		Create create = YoukuOAuthUtils.getWebCreate(access_token, title, tags, category, copyright_type, public_type, watch_password, description, file_name);
		if(create != null){
			return showJsonSuccess(model, create.getUpload_token());
		}
		return showJsonSuccess(model, "上传失败");
	}
	
	@RequestMapping("/movie/youkuWebCommit.xhtml")
	public String youkuWebCommit(String access_token, String upload_token, String upload_server_name, ModelMap model){
		Commit commit = YoukuOAuthUtils.getWebCommit(access_token, upload_token, upload_server_name);
		if(commit != null){
			return showJsonSuccess(model, commit.getVideo_id());
		}
		return showJsonSuccess(model, "上传失败");
	}
}