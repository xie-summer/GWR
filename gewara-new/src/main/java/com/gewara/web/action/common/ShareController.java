package com.gewara.web.action.common;

import java.util.Map;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.RequestMapping;

import com.gewara.Config;
import com.gewara.constant.MemberConstant;
import com.gewara.constant.content.OpenShareConstant;
import com.gewara.model.user.Member;
import com.gewara.untrans.ShareService;
import com.gewara.util.HttpResult;
import com.gewara.util.HttpUtils;
import com.gewara.util.JsonUtils;
import com.gewara.util.PKCoderUtil;
import com.gewara.util.StringUtil;
import com.gewara.util.WebUtils;
import com.gewara.web.action.BaseHomeController;
import com.mime.qweibo.examples.QWeiboSyncApi;

@Controller
public class ShareController extends BaseHomeController {
	
	@Autowired@Qualifier("config")
	private Config config;
	public void setConfig(Config config) {
		this.config = config;
	}
	@Autowired@Qualifier("shareService")
	private ShareService shareService;
	public void setShareService(ShareService shareService) {
		this.shareService = shareService;
	}
	/**
	 * 新浪微博用户连接授权
	 */
	@RequestMapping("/home/bind/sina/userApp.xhtml")
	public String accessUser(String source, ModelMap model){
		try {
			String callbackUrl = config.getAbsPath() + config.getBasePath() + "bind/sina/accessResult.xhtml?source="+source;
			String url = OpenShareConstant.WEIBO_OAUTH_URL + "?client_id="+OpenShareConstant.WEIBO_APPKEY+"&redirect_uri=" + callbackUrl+"&forcelogin=true";
			return showRedirect(url, model);
		} catch (Exception e) {
			return showRedirect("/userapp/close.xhtml", model);
		}
	}
	
	/**
	 * 新浪授权返回
	 * 这一步是必须 不然IE浏览器拿不到登录cookie
	 */
	@RequestMapping("/bind/sina/accessResult.xhtml")
	public String accessReturnResult(String source, String code, ModelMap model){
		model.put("source", source);
		model.put("code", code);
		if(StringUtils.isBlank(code)) return showRedirect("userapp/close.xhtml",model);
		return showRedirect("/bind/sina/accessRedirectResult.xhtml", model);
	}
	
	
	@RequestMapping("/bind/sina/accessRedirectResult.xhtml")
	public String accessSinaResult(@CookieValue(value=LOGIN_COOKIE_NAME, required=false)String sessid, String source, HttpServletRequest request, ModelMap model){
		String redirectUrl = "home/memberSynchroizaInfo.xhtml";
		Map<String, String> params = WebUtils.getRequestMap(request);
		String callbackUrl = config.getAbsPath() + config.getBasePath() + "bind/sina/accessRedirectResult.xhtml";
		try {
			params.put("client_id", OpenShareConstant.WEIBO_APPKEY);
			params.put("client_secret", OpenShareConstant.WEIBO_SECRET);
			params.put("grant_type", "authorization_code");
			params.put("redirect_uri", callbackUrl);
			HttpResult result = HttpUtils.postUrlAsString(OpenShareConstant.WEIBO_OAUTH_ACCESS_URL, params);
			if(!result.isSuccess()) return show404(model, "授权失败，请重新授权！新浪返回错误！");
			Map<String, String> dataMap = JsonUtils.readJsonToMap(result.getResponse());
			Object userid = dataMap.get("uid");
			if(userid == null){
				String msg = dataMap.get("error_description") == null ? "授权失败，请重新登录！": dataMap.get("error_description");
				return show404(model, msg);
			}
			String loginname = userid + "";
			String token = dataMap.get("access_token");
			Object temp = dataMap.get("expires_in");
			String expires = String.valueOf(temp);
			String ip = WebUtils.getRemoteIp(request);
			Member member = loginService.getLogonMemberBySessid(ip, sessid);
			if(member == null) return show404(model, "绑定失败，请刷新重新绑定！");
			shareService.createShareMember(member, MemberConstant.SOURCE_SINA, loginname, token, "", expires);
			if(StringUtils.equals(source, "close")){
				model.put("isBind", "Wb");
				dbLogger.warn("isBind sina:" + "Wb");
				return showRedirect("userapp/close.xhtml",model);
			}
			return showRedirect(redirectUrl, model);
		}catch(Exception e){
			dbLogger.warn("绑定新浪微博失败："+StringUtil.getExceptionTrace(e, 5));
			return show404(model, "授权失败，请重新授权！");
		}
	}
	
	@RequestMapping("/home/bind/qq/userApp.xhtml")
	public String qqUserApp(String source, HttpServletResponse response, ModelMap model){
		QWeiboSyncApi qApi=new QWeiboSyncApi();
		String callBackUrl=config.getAbsPath()+config.getBasePath()+"bind/qq/accessRights.xhtml?source=" +source;
		String resStr=qApi.getRequestToken(callBackUrl);
		String[] StrArray=resStr.split("&");
		String strTokenKey="",strTokenScrect="";
		if(StrArray.length>=2){
			strTokenKey=StrArray[0].split("=")[1];
			strTokenScrect=StrArray[1].split("=")[1];
		}
		String cookieStr=PKCoderUtil.encodeString(strTokenKey+"@@"+strTokenScrect);
		Cookie cookie=new Cookie("rToken", cookieStr);
		cookie.setPath("/bind/qq/accessRights.xhtml");
		cookie.setMaxAge(60*5);
		cookie.setHttpOnly(true);
		response.addCookie(cookie);
		try {
			return showRedirect("http://open.t.qq.com/cgi-bin/authorize?oauth_token="+strTokenKey, model);
		} catch (Exception e) {
			return showRedirect("/userapp/close.xhtml", model);
		}
	}
	
	@RequestMapping("/bind/qq/accessRights.xhtml")
	public String qqAccessRights(String source, String oauth_verifier, @CookieValue(value="rToken", required=false) String rToken, ModelMap model){
		if(StringUtils.isBlank(rToken)) return show404(model, "请稍后刷新页面重试!");
		model.put("rtoken", rToken);
		model.put("oauth_verifier", oauth_verifier);
		model.put("source", source);
		dbLogger.warn("redirect qq:" + "bind/qq/accessRedirectRights.xhtml");
		return showRedirect("/bind/qq/accessRedirectRights.xhtml", model);
	}
	
	@RequestMapping("/bind/qq/accessRedirectRights.xhtml")
	public String qqAccessQQRights(@CookieValue(value=LOGIN_COOKIE_NAME, required=false)String sessid,String source, String oauth_verifier, String rtoken, HttpServletRequest request, ModelMap model){
		dbLogger.warn("rtoken qq:" + rtoken);
		if(rtoken == null) return show404(model, "请稍后刷新页面重试!");
		dbLogger.warn("source qq:" + source);
		try{
			String[] rEoder=PKCoderUtil.decodeString(rtoken).split("@@");
			String resTokenKey = rEoder[0];
			String resTokenSerect = rEoder[1];
			QWeiboSyncApi qApi=new QWeiboSyncApi();
			String accesToken = qApi.getAccessToken(resTokenKey, resTokenSerect, oauth_verifier);
			String[] aToken=accesToken.split("&");
			String aTokenKey="",aTokenSerect="",loginName="";
			if(aToken.length>=3){
				aTokenKey=aToken[0].split("=")[1];
				aTokenSerect=aToken[1].split("=")[1];
				loginName=aToken[2].split("=")[1];
			}
			String ip = WebUtils.getRemoteIp(request);
			Member member = loginService.getLogonMemberBySessid(ip, sessid);
			if(member == null) return show404(model, "绑定失败，请刷新重新绑定！");
			
			shareService.createShareMember(member, MemberConstant.SOURCE_QQ, loginName, aTokenKey, aTokenSerect, null);
			if(StringUtils.equals(source, "close")){
				model.put("isBind", "Qb");
				dbLogger.warn("isBind qq:" + "Qb");
				return showRedirect("userapp/close.xhtml", model);
			}
			//model.put("t", ""+System.currentTimeMillis());
			return showRedirect("home/memberSynchroizaInfo.xhtml", model);
		}catch (Exception e) {
			dbLogger.error("bind_QQ", e);
			return show404(model, "绑定失败，请刷新重新绑定！");
		}
	}
	
	@RequestMapping("/userapp/close.xhtml")
	public String userAppClose(String isBind, ModelMap model){
		model.put("isBind", isBind);
		return "home/member/userAppClose.vm";
	}
}
