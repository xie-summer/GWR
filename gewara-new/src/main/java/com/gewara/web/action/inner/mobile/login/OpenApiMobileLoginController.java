package com.gewara.web.action.inner.mobile.login;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.gewara.api.userdevice.UserDeviceService;
import com.gewara.api.vo.UserDevice;
import com.gewara.bank.AliUserDetail;
import com.gewara.bank.AliUserToken;
import com.gewara.constant.AdminCityContant;
import com.gewara.constant.ApiConstant;
import com.gewara.constant.BindConstant;
import com.gewara.constant.MemberConstant;
import com.gewara.constant.PayConstant;
import com.gewara.constant.TagConstant;
import com.gewara.constant.app.PushConstant;
import com.gewara.constant.sys.LogTypeConstant;
import com.gewara.json.AppSourceCount;
import com.gewara.json.MobileLoadImage;
import com.gewara.model.api.ApiUser;
import com.gewara.model.pay.SMSRecord;
import com.gewara.model.user.Member;
import com.gewara.model.user.MemberInfo;
import com.gewara.model.user.OpenMember;
import com.gewara.pay.AlipayUtil;
import com.gewara.pay.PayOtherUtil;
import com.gewara.service.bbs.BlogService;
import com.gewara.service.member.BindMobileService;
import com.gewara.support.ErrorCode;
import com.gewara.untrans.ShareService;
import com.gewara.untrans.mobile.WeixinService;
import com.gewara.untrans.monitor.SysLogType;
import com.gewara.util.DateUtil;
import com.gewara.util.HttpResult;
import com.gewara.util.HttpUtils;
import com.gewara.util.JsonUtils;
import com.gewara.util.PKCoderUtil;
import com.gewara.util.StringUtil;
import com.gewara.util.ValidateUtil;
import com.gewara.util.VmUtils;
import com.gewara.util.WebUtils;
import com.gewara.web.action.inner.OpenApiAuth;
import com.gewara.web.action.inner.mobile.BaseOpenApiMobileController;
import com.gewara.web.filter.OpenApiMobileAuthenticationFilter;
import com.gewara.xmlbind.DataWrapper;
@Controller
public class OpenApiMobileLoginController extends BaseOpenApiMobileController{
	@Autowired@Qualifier("blogService")
	private BlogService blogService ;
	@Autowired@Qualifier("bindMobileService")
	private BindMobileService bindMobileService;
	@Autowired@Qualifier("shareService")
	private ShareService shareService;
	
	@Autowired@Qualifier("userDeviceService")
	private UserDeviceService userDeviceService;
	
	@Autowired@Qualifier("weixinService")
	private WeixinService weixinService;
	
	private String checkOpenLoginSource(String source){
		String[] openSources = {MemberConstant.SOURCE_ALIPAY,
				MemberConstant.SOURCE_SINA,MemberConstant.SOURCE_QQ,MemberConstant.SOURCE_TAOBAO, MemberConstant.SOURCE_TENCENT};
		for(String openSource:openSources){
			if(openSource.equalsIgnoreCase(source)) return openSource;
		}
		return null;
	}
	/**
	 * 用户注册(邮件注册)
	 */
	@RequestMapping("/openapi/mobile/login/emailReg.xhtml")
	public String savereg(HttpServletRequest request,String nickname, String email, String password, String citycode, ModelMap model){
		OpenApiAuth auth = OpenApiMobileAuthenticationFilter.getOpenApiAuth();
	
		if(StringUtils.isBlank(citycode)) citycode = AdminCityContant.CITYCODE_SH;
		ErrorCode<Member> code = memberService.regMember(nickname, email, password, null, null, auth.getApiUser().getBriefname(), citycode, auth.getRemoteIp());
		if(code.isSuccess()){
			Member member = code.getRetval();
			gewaMailService.sendRegEmail(member);
			logAppSource(request, citycode, member.getId(),AppSourceCount.TYPE_REG, "wap");
			return getSuccessXmlView(model);
		}else{
			return getErrorXmlView(model, ApiConstant.CODE_DATA_ERROR, code.getMsg());
		}
	}
	private ErrorCode<Member> validLogin(String username, String password){
		if(StringUtils.isBlank(username) || StringUtils.isBlank(password)){
			return ErrorCode.getFailure(ApiConstant.CODE_DATA_ERROR, "请输入登录信息！");
		}
		String ip = OpenApiMobileAuthenticationFilter.getOpenApiAuth().getRemoteIp();
		
		ErrorCode code = validLoginLimit(ip);
		if(!code.isSuccess()){
			Map<String, String> entry = new HashMap<String, String>();
			entry.put("ip", ip);
			entry.put("username", username);
			entry.put("errortype", "ipLoginLimit");
			monitorService.addSysLog(SysLogType.userlogin, entry);
			return ErrorCode.getFailure(ApiConstant.CODE_DATA_ERROR, code.getMsg());
		}

		ErrorCode<Member> loginResult = memberService.doLoginByEmailOrMobile(username, password);
		if(!loginResult.isSuccess()){
			ErrorCode<String> mcode = validLoginLimitNum(ip);
			if(!mcode.isSuccess()){
				Map<String, String> entry = new HashMap<String, String>();
				entry.put("ip", ip);
				entry.put("username", username);
				entry.put("errortype", "ipLoginLimit");
				monitorService.addSysLog(SysLogType.userlogin, entry);
				return ErrorCode.getFailure(ApiConstant.CODE_DATA_ERROR, mcode.getMsg());
			}
			return ErrorCode.getFailure(ApiConstant.CODE_MEMBER_NOT_EXISTS, loginResult.getMsg());
		}
		Member member = loginResult.getRetval();
		if (blogService.isBlackMember(member.getId())){
			return ErrorCode.getFailure(ApiConstant.CODE_DATA_ERROR, "你在黑名单中，如有疑问请联系格瓦拉客服！");
		}
		
		return ErrorCode.getSuccessReturn(member);
	}
	/**
	 * 用户登录
	 */
	@RequestMapping("/openapi/mobile/login/login.xhtml")
	public String login(HttpServletRequest request, String username, String password, String apptype,
			String appSource, String osType,String deviceid,String citycode, ModelMap model){
		ErrorCode<Member> code = validLogin(username, password);
		if(!code.isSuccess()){
			return getErrorXmlView(model, code.getErrcode(), code.getMsg());
		}
		Member member = code.getRetval();
		if(StringUtils.isNotBlank(appSource) && StringUtils.isNotBlank(osType)) {
			if(StringUtils.isBlank(citycode)) citycode = AdminCityContant.CITYCODE_SH;
			logAppSource(request, citycode, member.getId(),AppSourceCount.TYPE_LOGIN, apptype);
		}
		apptype = StringUtils.isBlank(apptype)?TagConstant.TAG_CINEMA:apptype;
		if(StringUtils.equals(config.getString("sendPushServerFlag"),PushConstant.SEND_PUSH_SERVER_FLAG)){
			UserDevice ud = new UserDevice(member.getId(),deviceid,"",osType,apptype,"","");
			try {
				userDeviceService.saveUserDevice(ud);
			} catch (Exception e) {
				dbLogger.error(e.getMessage(), e);
			}
		}
		getLoginMap(member, model, request);
		return getOpenApiXmlDetail(model);
	}
	/**
	 * 手机号码注册入口
	 */
	@RequestMapping("/openapi/mobile/login/mobileRegister.xhtml")
	public String mobileRegister(@RequestParam(required=true)String nickname,
			@RequestParam(required=true) String password,
			@RequestParam(required=true) String dynamicNumber,
			@RequestParam(required=true) String mobile,
			@RequestParam(required=true) Double pointx,
			@RequestParam(required=true) Double pointy,
			@RequestParam(required=true) String appSource,
			@RequestParam(required=true) String apptype,
			@RequestParam(required=true) String osType,
			@RequestParam(required=true) String citycode, ModelMap model,HttpServletRequest request){
		
		if(!ValidateUtil.isPassword(password)){
			return getErrorXmlView(model, ApiConstant.CODE_DATA_ERROR, "密码格式不正确,只能是字母，数字，下划线，长度6—14位！");
		}
		String errChar = blogService.filterAllKey(nickname);
		if(StringUtils.isNotBlank(errChar)){
			return getErrorXmlView(model, ApiConstant.CODE_DATA_ERROR,"含有非法关键字！");
		}
		if(StringUtils.equalsIgnoreCase(citycode, "null")) citycode = AdminCityContant.CITYCODE_SH;
		if(StringUtils.isBlank(citycode)) citycode = AdminCityContant.CITYCODE_SH;
		OpenApiAuth auth = OpenApiMobileAuthenticationFilter.getOpenApiAuth();
		ErrorCode<Member> code = memberService.regMemberWithMobile(nickname, mobile, password, dynamicNumber, null, null, apptype + osType, citycode, auth.getRemoteIp());
		if(!code.isSuccess()){
			return getErrorXmlView(model, ApiConstant.CODE_DATA_ERROR, code.getMsg());
		}
		Member member = code.getRetval();
		nosqlService.memberSign(member.getId(), pointx, pointy);
		if(StringUtils.isNotBlank(appSource) && StringUtils.isNotBlank(osType)) {
			logAppSource(request, citycode, member.getId(), AppSourceCount.TYPE_REG, apptype);
		}
		return getSuccessXmlView(model);
	}
	
	/**
	 * 手机找回密码，发送验证码
	 */
	@RequestMapping("/openapi/mobile/login/getMobileCodeByFindpass.xhtml")
	public String getValidateCode(String mobile, ModelMap model) {
		if(!ValidateUtil.isMobile(mobile)){
			return getErrorXmlView(model, ApiConstant.CODE_DATA_ERROR, "手机号输入错误！");
		}
		Member member = daoService.getObjectByUkey(Member.class, "mobile", mobile, false);
		if(member == null) {
			return getErrorXmlView(model, ApiConstant.CODE_DATA_ERROR, "此手机号还未注册，请重新填写！");
		}
		OpenApiAuth auth = OpenApiMobileAuthenticationFilter.getOpenApiAuth();
		String ip = auth.getRemoteIp();
		ErrorCode<SMSRecord> code = bindMobileService.refreshBindMobile(BindConstant.TAG_BACKPASS, mobile, ip);
		if(!code.isSuccess()) {
			return getErrorXmlView(model, ApiConstant.CODE_DATA_ERROR, code.getMsg());
		}
		untransService.sendMsgAtServer(code.getRetval(), false);
		return getSuccessXmlView(model);
	}
	
	/**
	 * 修改密码
	 */
	@RequestMapping("/openapi/mobile/login/modifyPassword.xhtml")
	public String modifyPassword(String mobile, String password, String checkCode, ModelMap model, HttpServletRequest request) {
		if(!ValidateUtil.isMobile(mobile)) {
			return getErrorXmlView(model, ApiConstant.CODE_DATA_ERROR, "手机号输入错误！");
		}
		ErrorCode bindCode = bindMobileService.checkBindMobile(BindConstant.TAG_BACKPASS, mobile, checkCode);
		if(!bindCode.isSuccess()) {
			return getErrorXmlView(model, ApiConstant.CODE_DATA_ERROR, bindCode.getMsg());
		}
		Member member = daoService.getObjectByUkey(Member.class, "mobile", mobile, false);
		if(member == null) {
			return getErrorXmlView(model, ApiConstant.CODE_DATA_ERROR, "此手机号还未注册，请重新填写！");
		}
		member.setPassword(StringUtil.md5(password));
		daoService.saveObject(member);
		monitorService.saveMemberLog(member.getId(), MemberConstant.ACTION_MODPWD, null, WebUtils.getRemoteIp(request));
		return getSuccessXmlView(model); 
	}
	
	/**
	 * 支付宝用户快捷注册
	 */
	@RequestMapping("/openapi/mobile/login/alipayUserFastRegByMobile.xhtml")
	public String alipayUserFastRegByMobile(String authcode, String userid, String appId, String mobile, String password,
			String appSource, String apptype, String osType, String citycode, ModelMap model, HttpServletRequest request) {
		if (StringUtils.isBlank(userid) || StringUtils.isBlank(mobile) || StringUtils.isBlank(password) 
				|| StringUtils.isBlank(authcode) || StringUtils.isBlank(appId)) {
			return getErrorXmlView(model, ApiConstant.CODE_DATA_ERROR, "缺少参数！");
		}
		if (!ValidateUtil.isMobile(mobile)) {
			return getErrorXmlView(model, ApiConstant.CODE_DATA_ERROR, "手机号输入错误！");
		}
		boolean match = ValidateUtil.isVariable(password, 6, 14);
		if(!match) {
			return getErrorXmlView(model, ApiConstant.CODE_DATA_ERROR, "密码格式不正确！");
		}
		String alisource = MemberConstant.SOURCE_ALIPAY;
		OpenMember openMember = memberService.getOpenMemberByLoginname(alisource, userid);
		if (openMember == null) {
			return getErrorXmlView(model, ApiConstant.CODE_DATA_ERROR, "非法注册！");
		}
		
		ErrorCode<AliUserToken> tokenCode = getAliUserToken(userid, authcode, appId, false);
		if(!tokenCode.isSuccess()) {
			return getErrorXmlView(model, ApiConstant.CODE_DATA_ERROR, tokenCode.getMsg()); 
		}
		String singleMobile = StringUtils.substring(mobile, mobile.length() - 4);
		String nickname = singleMobile + StringUtil.getRandomString(10);
		boolean nickExist = memberService.isMemberExists(nickname, null);
		while (nickExist) {
			nickname = singleMobile + StringUtil.getRandomString(10);
			nickExist = memberService.isMemberExists(nickname, null);
		}
		String ip = OpenApiMobileAuthenticationFilter.getOpenApiAuth().getRemoteIp();
		ErrorCode<Member> code = memberService.regMemberWithMobile(nickname, mobile, password, null, null, apptype + osType, citycode, ip);
		if (!code.isSuccess()) {
			return getErrorXmlView(model, ApiConstant.CODE_DATA_ERROR, code.getMsg()); 
		}
		Member member = code.getRetval();
		if(StringUtils.isNotBlank(appSource) && StringUtils.isNotBlank(osType)) {
			logAppSource(request, citycode, member.getId(), AppSourceCount.TYPE_REG, apptype);
		}
		openMember.setRelateid(member.getId());
		daoService.saveObject(openMember);
		getLoginMap(member, model, request);
		return getOpenApiXmlDetail(model);
	}
	
	/**
	 * 第三方注册
	 */
	@RequestMapping("/openapi/mobile/login/saveOpenMember.xhtml")
	public String saveOpenMember(String citycode, String source, String shortsource, String userid, ModelMap model){
		if(StringUtils.isBlank(citycode) || StringUtils.isBlank(source) || StringUtils.isBlank(userid)){
			return getErrorXmlView(model, ApiConstant.CODE_DATA_ERROR, "citycode 或者souce或者userid为空！");
		}
		source = checkOpenLoginSource(source);
		if(source == null){
			return getErrorXmlView(model, ApiConstant.CODE_DATA_ERROR, "此第三方平台暂未进行登录合作!");
		}
		userid = WebUtils.urlDecoder(userid);
		if(StringUtils.isBlank(userid)) return getErrorXmlView(model, ApiConstant.CODE_SIGN_ERROR, "此第三方平台授权有问题!");
		OpenMember om = memberService.getOpenMemberByLoginname(source, userid);
		if(om == null){ 
			if(StringUtils.isBlank(shortsource)) shortsource = source;
			OpenApiAuth auth = OpenApiMobileAuthenticationFilter.getOpenApiAuth();
			om = memberService.createOpenMember(citycode, source, shortsource, userid, auth.getRemoteIp());
			daoService.saveObject(om);
		}
		MemberInfo memberInfo = daoService.getObject(MemberInfo.class, om.getMemberid());
		Map<String,String> otherInfoMap = JsonUtils.readJsonToMap(memberInfo.getOtherinfo());
		String openMemberTag = otherInfoMap.get("openMember");
		if(StringUtils.isNotBlank(openMemberTag) && !openMemberTag.contains(source)){
			otherInfoMap.put("openMember", openMemberTag+=","+source);
		}else{
			otherInfoMap.put("openMember", source);
		}
		memberInfo.setOtherinfo(JsonUtils.writeMapToJson(otherInfoMap));
		daoService.updateObject(memberInfo);
		return getSuccessXmlView(model);
	}
	
	/**
	 * 第三方用户登录
	 */
	@RequestMapping("/openapi/mobile/login/openMemberLogin.xhtml")
	public String openMemberLogin(String userid,String source, String nickname, String token, ModelMap model,HttpServletRequest request,HttpServletResponse response){
		if(StringUtils.isBlank(userid) || StringUtils.isBlank(source) ){
			return getErrorXmlView(model, ApiConstant.CODE_DATA_ERROR, "userid or source is not null");
		}
		source = checkOpenLoginSource(source);
		if(source == null){
			return getErrorXmlView(model, ApiConstant.CODE_DATA_ERROR, "此第三方平台暂未进行登录合作!");
		}
		userid = WebUtils.urlDecoder(userid);
		if(StringUtils.isBlank(userid)) {
			return getErrorXmlView(model, ApiConstant.CODE_SIGN_ERROR, "此第三方平台授权有问题!");
		}
		
		OpenMember openMember = memberService.getOpenMemberByLoginname(source, userid);
		boolean isNew = false;
		if(openMember == null){
			OpenApiAuth auth = OpenApiMobileAuthenticationFilter.getOpenApiAuth();
			openMember = memberService.createOpenMember(WebUtils.getAndSetDefault(request, response), source, source.substring(0,1), userid, auth.getRemoteIp());
			isNew = true;
		}
		Member member = daoService.getObject(Member.class, openMember.getMemberid());
		MemberInfo info = daoService.getObject(MemberInfo.class, openMember.getMemberid());
		Map<String, String> otherMap = VmUtils.readJsonToMap(info.getOtherinfo());
		otherMap.put(MemberConstant.OMSOURCE, source);
		if(StringUtils.isNotBlank(nickname) && StringUtils.length(nickname)<=20) {
			String key = blogService.filterAllKey(nickname);
			if(StringUtils.isBlank(key)){
				openMember.setNickname(nickname);
			}
		}
		if(StringUtils.equals(source, MemberConstant.SOURCE_ALIPAY)){
			openMember.setCategory(MemberConstant.CATEGORY_ALIKUAIJIE);
			setOpenMemberOtherInfo(openMember, token, isNew);
		}
		info.setOtherinfo(JsonUtils.writeMapToJson(otherMap));
		daoService.saveObjectList(info, openMember);
		getLoginMap(member, model, request);
		return getOpenApiXmlDetail(model);
	}
	
	private void setOpenMemberOtherInfo(OpenMember openMember, String token, boolean isNew){
		if(StringUtils.isNotBlank(token)){
			Map<String, String> omotherMap = VmUtils.readJsonToMap(openMember.getOtherinfo());
			if(!omotherMap.containsKey(MemberConstant.OM_MOBILE)){
				String url = null;
				Map<String, String> params = new HashMap<String, String>();
				params.put("token", token);
				if(isNew){
					params.put("isNew", "Y");
				}
				params.put("isNew", "Y");
				url = PayOtherUtil.getAliUserInfoUrl();
				cbAliUserDetail(url, params, openMember);
			}
		}
	}
	private void cbAliUserDetail(String url, Map<String, String> params, OpenMember openMember){
		Map<String, String> omotherMap = VmUtils.readJsonToMap(openMember.getOtherinfo());
		if(omotherMap.containsKey(MemberConstant.OM_MOBILE)) return;
		HttpResult hr = HttpUtils.postUrlAsString(url, params);
		if(hr.isSuccess()){
			ErrorCode<DataWrapper> wcode = getDataWrapper(hr);
			if(wcode.isSuccess()){
				 AliUserDetail userDetail = wcode.getRetval().getAliUserDetail();
				 if(StringUtils.isNotBlank(userDetail.getMobile())){
					 omotherMap.put(MemberConstant.OM_MOBILE, userDetail.getMobile());
					 openMember.setOtherinfo(JsonUtils.writeMapToJson(omotherMap));
				 }
			}
		}
	}
	/**
	 * 支付宝联名登录获取签名
	 */
	@RequestMapping("/openapi/mobile/login/alipayLoginSign.xhtml")
	public String alipayLoginSign(ModelMap model,HttpServletRequest request){
		StringBuilder sb = new StringBuilder();
		sb.append("app_name=\"mc\"&biz_type=\"trust_login\"&partner=\"");
		sb.append("loginPartnerId");
		sb.append("\"&notify_url=\"");
		try {
			sb.append(URLEncoder.encode(config.getAbsPath() + config.getBasePath() + "login/alipayNotify.xhtml","UTF-8"));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		sb.append("\"");
		String info = sb.toString();
		info = AlipayUtil.getRASSign(info);
		info += "\"&sign_type=\"RSA\"";
		Map<String, Object> resMap = new HashMap<String,Object>();
		resMap.put("dataString", info);
		model.put("resMap", resMap);
		model.put("root", "loginData");
		initField(model, request);
		return getOpenApiXmlDetail(model);
	}
	/**
	 * 发送手机动态验证码
	 */
	@RequestMapping("/openapi/mobile/login/sendMobileCheck.xhtml")
	public String sendDynamicCaptcha(String mobile, String memberEncode, ModelMap model){
		if(StringUtils.isBlank(mobile)){
			return getErrorXmlView(model, ApiConstant.CODE_DATA_ERROR, "手机号不能为空");
		}
		if(!ValidateUtil.isMobile(mobile)){
			return getErrorXmlView(model, ApiConstant.CODE_DATA_ERROR, "手机号输入错误");
		}
		String ip = OpenApiMobileAuthenticationFilter.getOpenApiAuth().getRemoteIp();
		Member member = daoService.getObjectByUkey(Member.class, "mobile", mobile, false);
		if(member != null){
			return getErrorXmlView(model, ApiConstant.CODE_DATA_ERROR, "您的手机号已进行过注册，不能重复注册！");
		}
		String tag = BindConstant.TAG_REGISTERCODE;
		if(StringUtils.isNotBlank(memberEncode)){
			tag = BindConstant.TAG_BINDMOBILE;
		}
		ErrorCode<SMSRecord> code = bindMobileService.refreshBindMobile(tag, mobile, ip);
		if(!code.isSuccess()){
			return getErrorXmlView(model, ApiConstant.CODE_DATA_ERROR, code.getMsg());
		}
		untransService.sendMsgAtServer(code.getRetval(), false);
		return getSuccessXmlView(model);
	}
	
	/**
	 * 校验用户名
	 */
	@RequestMapping("/openapi/mobile/login/checkNickname.xhtml")
	public String checkNickname(String nickname, ModelMap model){
		if(StringUtils.isBlank(nickname)) return getErrorXmlView(model, ApiConstant.CODE_DATA_ERROR,"昵称不能为空！");
		String errChar = blogService.filterAllKey(nickname);
		if(StringUtils.isNotBlank(errChar)){
			return getErrorXmlView(model, ApiConstant.CODE_DATA_ERROR,"含有非法关键字！");
		}
		boolean nickExist = memberService.isMemberExists(nickname, null);
		if(nickExist) return getErrorXmlView(model, ApiConstant.CODE_DATA_ERROR, "该昵称已经存在！");
		return getSuccessXmlView(model);
	}
	
	
	/*
	 * 第三方用户联名登录
	 */
	@RequestMapping("/openapi/mobile/login/createOpenMember.xhtml")
	public String getApiUser(String content_encrypt, HttpServletRequest request, ModelMap model){
		OpenApiAuth auth = OpenApiMobileAuthenticationFilter.getOpenApiAuth();
		ApiUser partner = auth.getApiUser();
		String partnerkey = partner.getPartnerkey();
		Map<String, String> otherMap = VmUtils.readJsonToMap(partner.getOtherinfo());
		String userid = "", source = "", shortsource = "";
		if(StringUtils.equals(partnerkey, PayConstant.WAPORG_QIEKE)){
			String iv = otherMap.get("iv");
			String des3key = otherMap.get("des3key");
			String json = PKCoderUtil.triDesDecrypt(content_encrypt, des3key, iv);
			dbLogger.warnWithType(LogTypeConstant.LOG_TYPE_PARTNER, PayConstant.WAPORG_QIEKE + json);
			Map<String, String> map = VmUtils.readJsonToMap(json);
			userid = map.get("user_id");
			source = PayConstant.WAPORG_QIEKE;
			shortsource = PayConstant.WAPORG_QIEKE;
		}else if(StringUtils.equals(partnerkey, PayConstant.WAPORG_BST)){
			String des3key = otherMap.get("des3key");
			String json = PKCoderUtil.decryptWithThiDES(des3key, content_encrypt, "UTF-8");
			dbLogger.warnWithType(LogTypeConstant.LOG_TYPE_PARTNER, PayConstant.WAPORG_BST + json);
			Map<String, String> map = VmUtils.readJsonToMap(json);
			userid = map.get("user_id");
			source = PayConstant.WAPORG_BST;
			shortsource = PayConstant.WAPORG_BST;
		}else if(StringUtils.equals(partnerkey, PayConstant.WAPORG_HTC)){
			String des3key = otherMap.get("des3key");
			String json = PKCoderUtil.decryptWithThiDES(des3key, content_encrypt, "UTF-8");
			dbLogger.warnWithType(LogTypeConstant.LOG_TYPE_PARTNER, PayConstant.WAPORG_HTC + json);
			Map<String, String> map = VmUtils.readJsonToMap(json);
			userid = map.get("user_id");
			source = PayConstant.WAPORG_HTC;
			shortsource = PayConstant.WAPORG_HTC;
		}else {
			String des3key = otherMap.get("des3key");
			String json = PKCoderUtil.decryptWithThiDES(des3key, content_encrypt, "UTF-8");
			dbLogger.warnWithType(LogTypeConstant.LOG_TYPE_PARTNER, PayConstant.WAPORG_BAIDU + json);
			Map<String, String> map = VmUtils.readJsonToMap(json);
			userid = map.get("user_id");
			source = partnerkey;
			shortsource = partnerkey;
		}
		OpenMember om = null;
		if(StringUtils.isNotBlank(userid)){
			userid = WebUtils.urlDecoder(userid);
			if(StringUtils.isBlank(userid)) return getErrorXmlView(model, ApiConstant.CODE_SIGN_ERROR, "此第三方平台授权有问题!");
			om = memberService.getOpenMemberByLoginname(source, userid);
			if(om == null){ 
				om = memberService.createOpenMember(AdminCityContant.CITYCODE_SH, source, shortsource, userid, auth.getRemoteIp());
			}
		}
		if(om==null) {
			return getErrorXmlView(model, ApiConstant.CODE_MEMBER_NOT_EXISTS, "创建用户失败！");
		}
		Member member = daoService.getObject(Member.class, om.getMemberid());
		getLoginMap(member, model, request);
		return getOpenApiXmlDetail(model);
	}
	
	/**
	 * 登录海报
	 */
	@RequestMapping("/openapi/mobile/login/loginImg.xhtml")
	public String loginImg(String apptype, ModelMap model) {
		Map params = new HashMap();
		params.put("status", MobileLoadImage.STATUS_Y);
		apptype = StringUtils.isBlank(apptype) ? TagConstant.TAG_CINEMA : apptype;
		params.put("apptype", apptype);
		List<MobileLoadImage> list = mongoService.getObjectList(MobileLoadImage.class, params, "addTime", false, 0, 5);
		String imgsrc = "";
		for(MobileLoadImage image : list) {
			if(image.hasProgress()){
				imgsrc = getMobilePath() + image.getImagesrc();
				break;
			}
		}
		return getSingleResultXmlView(model, imgsrc);
	}
	

	/**
	 * 微博分享
	 */
	@RequestMapping("/openapi/mobile/login/shareMemberByWeibo.xhtml")
	public String shareMember(ModelMap model, HttpServletRequest request) {
		String loginname = request.getParameter("userid");
		String token = request.getParameter("token");
		String expires = request.getParameter("expires"); 
		if(StringUtils.isBlank(loginname)){
			return getErrorXmlView(model, ApiConstant.CODE_DATA_ERROR, "缺少参数userid" ); 
		}
		if(StringUtils.isBlank(token)){
			return getErrorXmlView(model, ApiConstant.CODE_DATA_ERROR, "缺少参数token" ); 
		}
		if(StringUtils.isBlank(expires)){
			return getErrorXmlView(model, ApiConstant.CODE_DATA_ERROR, "缺少参数expires" ); 
		}
		Member member = OpenApiMobileAuthenticationFilter.getOpenApiAuth().getMember();
		shareService.createShareMember(member,MemberConstant.SOURCE_SINA, loginname, token, "", expires);
		Map<String, String> resMap = new HashMap<String, String>();
		resMap.put("token", token);
		resMap.put("userid", loginname);
		resMap.put("expires", expires);
		model.put("root", "weiBoAuth");
		model.put("resMap", resMap);
		initField(model, request);
		return getOpenApiXmlDetail(model);
	}
	
	@RequestMapping("/openapi/mobile/login/aliWalletLogin.xhtml")
	public String aliUserDetail(String authcode, String userid, String appId, ModelMap model, HttpServletRequest request, HttpServletResponse response) {
		if(StringUtils.isBlank(authcode) || StringUtils.isBlank(userid) || StringUtils.isBlank(appId)){
			return getErrorXmlView(model, ApiConstant.CODE_DATA_ERROR, "缺少参数！"); 
		}
		String source = MemberConstant.SOURCE_ALIPAY;
		OpenMember openMember = memberService.getOpenMemberByLoginname(source, userid);
		ErrorCode<AliUserToken> code = null;
		if(openMember==null){
			//根据code获取token
			code = getAliUserToken(userid, authcode, appId, false);
		}else {
			 code = getAliUserToken(userid, authcode, appId, openMember);
		}
		if(!code.isSuccess()){
			dbLogger.warn("aliwallet login failure:" + code.getMsg());
			return getErrorXmlView(model, ApiConstant.CODE_DATA_ERROR, code.getMsg()); 
		}
		//根据token获取用户信息
		AliUserToken token = code.getRetval();
		Map<String, String> params = new HashMap<String, String>();
		params.put("shortToken", token.getAccessToken());
		params.put("appId", appId);
		String url = PayOtherUtil.getAliUserDetailUrl();
		HttpResult hr = HttpUtils.postUrlAsString(url, params);
		if(!hr.isSuccess()){
			return getErrorXmlView(model, ApiConstant.CODE_DATA_ERROR, hr.getMsg()); 
		}
		ErrorCode<DataWrapper> wcode = getDataWrapper(hr);
		if(!wcode.isSuccess()){
			return getErrorXmlView(model, ApiConstant.CODE_DATA_ERROR, wcode.getMsg()); 
		}
		AliUserDetail detail = wcode.getRetval().getAliUserDetail();
		if(openMember == null){
			OpenApiAuth auth = OpenApiMobileAuthenticationFilter.getOpenApiAuth();
			openMember = memberService.createOpenMember(WebUtils.getAndSetDefault(request, response), source, source.substring(0,1), userid, auth.getRemoteIp());
		}
		// 支付宝钱包启动用户登录后，如未该手机号未注册过格瓦拉账户，提示输入密码，生成账户
		// 0表示未注册，1已注册
		String alreadyRegistered = "0";
		if (StringUtils.isNotBlank(detail.getMobile())) {
			Member tempMember = daoService.getObjectByUkey(Member.class, "mobile", detail.getMobile());
			if (tempMember != null) {
				alreadyRegistered = "1";
			}
		}
		Member member = daoService.getObject(Member.class, openMember.getMemberid());

		MemberInfo info = daoService.getObject(MemberInfo.class, openMember.getMemberid());
		Timestamp begtime = token.getBegtime();
		if(begtime==null){
			begtime = info.getAddtime();
		}
		reSetOm(openMember, begtime, token, detail);
		if(StringUtils.isNotBlank(detail.getRealName())){
			String key = blogService.filterAllKey(detail.getRealName());
			if(StringUtils.isBlank(key)){
				openMember.setNickname(detail.getRealName());
			}
		}
		daoService.saveObjectList(openMember, info);
		Map<String, Object> resMap = getLoginMap(member, info);
		String mobile = StringUtils.isNotBlank(member.getMobile())?member.getMobile():detail.getMobile();
		String nickname = detail.getRealName();
		if(StringUtils.isBlank(nickname)){
			nickname = member.getNickname();
		}
		resMap.put("mobile", mobile);
		resMap.put("nickname", nickname);
		resMap.put("alreadyRegistered", alreadyRegistered);
		return getOpenApiXmlDetail(resMap, "member", model, request);
	}
	private void reSetOm(OpenMember om, Timestamp begtime, AliUserToken token, AliUserDetail detail){
		Map<String, String> omMap = JsonUtils.readJsonToMap(om.getOtherinfo());
		omMap.put(MemberConstant.ALIWALLET_SHORTTOKEN, token.getAccessToken());
		omMap.put(MemberConstant.ALIWALLET_LONGTOKEN, token.getRefreshToken());
		omMap.put(MemberConstant.ALIWALLET_EXPIRESIN, token.getExpiresIn()+"");
		omMap.put(MemberConstant.ALIWALLET_REEXPIRESIN, token.getReExpiresIn()+"");
		omMap.put(MemberConstant.ALIWALLET_SHORTVALIDTIME, DateUtil.formatTimestamp(DateUtil.addSecond(begtime, token.getExpiresIn())));
		omMap.put(MemberConstant.ALIWALLET_LONGVALIDTIME, DateUtil.formatTimestamp(DateUtil.addSecond(begtime, token.getReExpiresIn())));
		if(StringUtils.isNotBlank(detail.getMobile())){
			omMap.put(MemberConstant.OM_MOBILE, detail.getMobile());
		}
		String otherinfo = JsonUtils.writeMapToJson(omMap);
		om.setOtherinfo(otherinfo);
		om.setCategory(MemberConstant.CATEGORY_ALIWALLET);
	}
	private ErrorCode<AliUserToken> getAliUserToken(String userid, String authcode, String appId, boolean isRefresh){
		Map<String, String> params = new HashMap<String, String>();
		params.put("codeOrtoken", authcode);
		if(isRefresh){
			params.put("grantType", "refresh_token");
		}else {
			params.put("grantType", "authorization_code");
		}
		params.put("appId", appId);
		String url = PayOtherUtil.getAliUserTokenUrl();
		HttpResult hr = HttpUtils.postUrlAsString(url, params);
		dbLogger.warn("getAliUserToken="+params.toString()+", res=" + hr.getResponse());
		if(!hr.isSuccess()){
			return ErrorCode.getFailure(hr.getMsg()); 
		}
		ErrorCode<DataWrapper> wcode = getDataWrapper(hr);
		if(!wcode.isSuccess()){
			return ErrorCode.getFailure(wcode.getMsg()); 
		}
		AliUserToken token = wcode.getRetval().getAliUserToken();
		if(!StringUtils.equals(token.getUserId(), userid)){
			return ErrorCode.getFailure("登录信息已过期，请重新登录！"); 
		}
		return ErrorCode.getSuccessReturn(token);
	}
	
	private ErrorCode<AliUserToken> getAliUserToken(String userid, String authcode, String appId, OpenMember om){
		Map<String, String> omMap = JsonUtils.readJsonToMap(om.getOtherinfo());
		String longtime = omMap.get(MemberConstant.ALIWALLET_LONGVALIDTIME);
		Timestamp curtime = DateUtil.getCurFullTimestamp();
		if(StringUtils.isBlank(longtime)){
			ErrorCode<AliUserToken> code = getAliUserToken(userid, authcode, appId, false);
			if(!code.isSuccess()){
				return ErrorCode.getFailure(code.getMsg()); 
			}
			return ErrorCode.getSuccessReturn(code.getRetval());
		}
		Timestamp longValidtime = DateUtil.parseTimestamp(longtime);
		AliUserToken token = null;
		if(longValidtime.before(curtime)){ //长Token过期
			ErrorCode<AliUserToken> code = getAliUserToken(userid, authcode, appId, false);
			if(!code.isSuccess()){
				return ErrorCode.getFailure(code.getMsg()); 
			}
			token = code.getRetval();
			token.setBegtime(curtime);
			return ErrorCode.getSuccessReturn(code.getRetval());
		}else {
			String shorttime = omMap.get(MemberConstant.ALIWALLET_SHORTVALIDTIME);
			Timestamp shortValidtime = DateUtil.parseTimestamp(shorttime);
			if(shortValidtime==null || shortValidtime.before(curtime)){ //短Token过期
				String longToken = omMap.get(MemberConstant.ALIWALLET_LONGTOKEN);
				ErrorCode<AliUserToken> code = getAliUserToken(userid, longToken, appId, true);
				if(!code.isSuccess()){
					return ErrorCode.getFailure(code.getMsg()); 
				}
				token = code.getRetval();
				token.setBegtime(curtime);
				return ErrorCode.getSuccessReturn(token);
			}
		}
		String shortToken = omMap.get(MemberConstant.ALIWALLET_SHORTTOKEN);
		String longToken = omMap.get(MemberConstant.ALIWALLET_LONGTOKEN);
		Integer expiresIn = Integer.valueOf(omMap.get(MemberConstant.ALIWALLET_EXPIRESIN));
		Integer reExpiresIn = Integer.valueOf(omMap.get(MemberConstant.ALIWALLET_REEXPIRESIN));
		token = new AliUserToken(userid, shortToken, expiresIn, longToken, reExpiresIn);
		return ErrorCode.getSuccessReturn(token);
	}
	
	@RequestMapping("/openapi/mobile/login/weixinLogin.xhtml")
	public String weixinLogin(String username, String password, String userid, String tms, String wxs, ModelMap model, HttpServletRequest request) {
		if(StringUtils.isBlank(userid)){
			return getErrorXmlView(model, ApiConstant.CODE_DATA_ERROR, "缺少参数！"); 
		}
		ErrorCode wxcode = weixinService.validReq(tms, wxs);
		if(!wxcode.isSuccess()){
			return getErrorXmlView(model, ApiConstant.CODE_DATA_ERROR, wxcode.getMsg());
		}
		ErrorCode<Member> code = validLogin(username, password);
		if(!code.isSuccess()){
			return getErrorXmlView(model, code.getErrcode(), code.getMsg());
		}
		Member member = code.getRetval();
		memberService.createOpenMemberByMember(member, MemberConstant.SOURCE_WEIXIN, userid);
		getLoginMap(member, model, request);
		return getOpenApiXmlDetail(model);
	}
	
	/**
	 * 微信用户注册
	 */
	@RequestMapping("/openapi/mobile/login/weixinUserReg.xhtml")
	public String weixinUserRegister(String username, String nickname, String password, String userid, String tms, String wxs, String regForm, String citycode, ModelMap model, HttpServletRequest request) {
		if(StringUtils.isBlank(userid)){
			return getErrorXmlView(model, ApiConstant.CODE_DATA_ERROR, "缺少参数！"); 
		}
		ErrorCode wxcode = weixinService.validReq(tms, wxs);
		if(!wxcode.isSuccess()){
			return getErrorXmlView(model, ApiConstant.CODE_DATA_ERROR, wxcode.getMsg());
		}
		if (!ValidateUtil.isEmail(username) && !ValidateUtil.isMobile(username)) {
			return getErrorXmlView(model, ApiConstant.CODE_DATA_ERROR, "注册帐号只能是手机号或者邮箱！");
		}
		if (!ValidateUtil.isPassword(password)) {
			return getErrorXmlView(model, ApiConstant.CODE_DATA_ERROR, "密码格式不正确,只能是字母，数字，下划线，长度6—14位！");
		}
		
		String ip = WebUtils.getRemoteIp(request);
		if (StringUtils.isBlank(citycode)) {
			citycode = WebUtils.getCitycodeByIp(ip);
		}
		ErrorCode<Member> memberCode = memberService.createMemberWithPartner(userid, username,nickname, password, citycode, regForm, MemberConstant.SOURCE_WEIXIN, ip);
		if (!memberCode.isSuccess()) {
			return getErrorXmlView(model, ApiConstant.CODE_DATA_ERROR, memberCode.getMsg()); 
		}
		Member member = memberCode.getRetval();
		getLoginMap(member, model, request);
		return getOpenApiXmlDetail(model);
	}
}
