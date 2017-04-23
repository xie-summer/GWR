package com.gewara.web.action.api2mobile;

import java.util.HashMap;
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

import com.gewara.Config;
import com.gewara.api.userdevice.UserDeviceService;
import com.gewara.api.vo.UserDevice;
import com.gewara.constant.AdminCityContant;
import com.gewara.constant.ApiConstant;
import com.gewara.constant.BindConstant;
import com.gewara.constant.MemberConstant;
import com.gewara.constant.PayConstant;
import com.gewara.constant.TagConstant;
import com.gewara.constant.app.PushConstant;
import com.gewara.constant.sys.LogTypeConstant;
import com.gewara.json.AppSourceCount;
import com.gewara.model.api.ApiUser;
import com.gewara.model.pay.SMSRecord;
import com.gewara.model.user.Member;
import com.gewara.model.user.MemberInfo;
import com.gewara.model.user.OpenMember;
import com.gewara.service.bbs.BlogService;
import com.gewara.service.member.BindMobileService;
import com.gewara.support.ErrorCode;
import com.gewara.untrans.monitor.SysLogType;
import com.gewara.util.MapApiUtil;
import com.gewara.util.PKCoderUtil;
import com.gewara.util.ValidateUtil;
import com.gewara.util.VmUtils;
import com.gewara.util.WebUtils;
import com.gewara.web.action.api.ApiAuth;
import com.gewara.web.action.api.BaseApiController;
import com.gewara.web.filter.NewApiAuthenticationFilter;

/**
 * 手机客户端登录
 * @author taiqichao
 *
 */
@Controller
public class Api2MobileLoginController extends BaseApiController{
	@Autowired@Qualifier("blogService")
	private BlogService blogService ;

	@Autowired@Qualifier("config")
	private Config config;

	@Autowired@Qualifier("userDeviceService")
	private UserDeviceService userDeviceService;
	
	@Autowired@Qualifier("bindMobileService")
	private BindMobileService bindMobileService;
	/**
	 * 用户登录
	 */
	@RequestMapping("/api2/mobile/login.xhtml")
	public String login(HttpServletRequest request, String username, String password, String apptype,
			Double pointx, Double pointy, String appSource, String osType,String deviceid,String citycode, ModelMap model){
		if(StringUtils.isBlank(username) || StringUtils.isBlank(password)){
			return getErrorXmlView(model, ApiConstant.CODE_MEMBER_NOT_EXISTS, "请输入登录信息！");
		}
		String ip = WebUtils.getRemoteIp(request);
		ErrorCode code = validLoginLimit(ip);
		if(!code.isSuccess()){
			Map<String, String> entry = new HashMap<String, String>();
			entry.put("ip", ip);
			entry.put("username", username);
			entry.put("errortype", "ipLoginLimit");
			monitorService.addSysLog(SysLogType.userlogin, entry);
			return getErrorXmlView(model, ApiConstant.CODE_DATA_ERROR, code.getMsg());
		}

		ErrorCode<Member> loginResult = memberService.doLoginByEmailOrMobile(username, password);
		if(!loginResult.isSuccess()){
			validLoginLimitNum(ip);
			Map<String, String> entry = new HashMap<String, String>();
			entry.put("ip", ip);
			entry.put("username", username);
			entry.put("errortype", "ipLoginLimit");
			monitorService.addSysLog(SysLogType.userlogin, entry);
			return getErrorXmlView(model, ApiConstant.CODE_MEMBER_NOT_EXISTS, loginResult.getMsg());
		}
		Member member = loginResult.getRetval();
		ErrorCode<String> encodeResult = memberService.getAndSetMemberEncode(member);

		if(MapApiUtil.isValidPoinx(pointx)){
			String address = MapApiUtil.getBaiduMapAddress(pointx+"", pointy+"");
			nosqlService.memberSign(member.getId(), pointx, pointy, address);
		}
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

		MemberInfo memberInfo = daoService.getObject(MemberInfo.class, member.getId());
		model.put("memberEncode", encodeResult.getRetval());
		model.put("memberId", member.getId());
		model.put("mobile", member.getMobile());
		model.put("nickName", member.getNickname());
		model.put("email", member.getEmail());
		model.put("memberInfo", memberInfo);
		return getXmlView(model, "api/mobile/login.vm");
	}
	/**
	 * 手机号码注册入口
	 * @param key
	 * @param encryptCode
	 * @param nickname
	 * @param password
	 * @param dynamicNumber
	 * @param mobile
	 * @param pointx
	 * @param pointy
	 * @param appSource
	 * @param apptype
	 * @param deviceid
	 * @param appVersion
	 * @param osType
	 * @param citycode
	 * @param osVersion
	 * @param mobileType
	 * @param version
	 * @param model
	 * @param request
	 * @return
	 */
	@RequestMapping("/api2/mobile/accounts/mobileRegister.xhtml")
	public String mobileRegister(@RequestParam(required=true)String nickname,
			@RequestParam(required=true) String password,
			@RequestParam(required=true) String dynamicNumber,
			@RequestParam(required=true) String mobile,
			@RequestParam(required=true) Double pointx,
			@RequestParam(required=true) Double pointy,
			@RequestParam(required=true) String appSource,
			@RequestParam(required=true) String apptype,
			@RequestParam(required=true) String osType,
			@RequestParam(required=true) String citycode,
			String version, ModelMap model,HttpServletRequest request){
		if(!ValidateUtil.isPassword(password)){
			return getErrorXmlView(model, ApiConstant.CODE_DATA_ERROR, "密码格式不正确,只能是字母，数字，下划线，长度6—14位！");
		}
		String errChar = blogService.filterAllKey(nickname);
		if(StringUtils.isNotBlank(errChar)){
			return getErrorXmlView(model, ApiConstant.CODE_DATA_ERROR,"含有非法关键字！");
		}
		if(StringUtils.equalsIgnoreCase(citycode, "null")) citycode = AdminCityContant.CITYCODE_SH;
		if(StringUtils.isBlank(citycode)) citycode = AdminCityContant.CITYCODE_SH;
		String ip = WebUtils.getRemoteIp(request);
			
		ErrorCode<Member> code = memberService.regMemberWithMobile(nickname, mobile, password, dynamicNumber, null, null, apptype + osType, citycode, ip);
		if(!code.isSuccess()){
			return getErrorXmlView(model, ApiConstant.CODE_DATA_ERROR, code.getMsg());
		}
		Member member = code.getRetval();
		nosqlService.memberSign(member.getId(), pointx, pointy);
		if(StringUtils.isNotBlank(appSource) && StringUtils.isNotBlank(osType)) {
			logAppSource(request, citycode, member.getId(), AppSourceCount.TYPE_REG, apptype);
		}
		dbLogger.warn(version + "版手机API，用户注册，手机号为：" + mobile.substring(mobile.length() - 4,mobile.length()) + "，用户名为：" + nickname);
		return getXmlView(model, "api/mobile/result.vm");
	}
	/**
	 * 第三方用户登录
	 * @param key
	 * @param encryptCode
	 * @param userid
	 * @param source
	 * @param model
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping("/api2/mobile/accounts/openMemberLogin.xhtml")
	public String openMemberLogin(String userid,String source,ModelMap model,HttpServletRequest request,HttpServletResponse response){
		if(StringUtils.isBlank(userid) || StringUtils.isBlank(source) ){
			return getErrorXmlView(model, ApiConstant.CODE_DATA_ERROR, "userid or source is not null");
		}
		source = checkOpenLoginSource(source);
		if(source == null){
			return getErrorXmlView(model, ApiConstant.CODE_DATA_ERROR, "此第三方平台暂未进行登录合作!");
		}
		userid = WebUtils.urlDecoder(userid);
		if(StringUtils.isBlank(userid)) return getErrorXmlView(model, ApiConstant.CODE_SIGN_ERROR, "此第三方平台授权有问题!");
		OpenMember openMember = memberService.getOpenMemberByLoginname(source, userid);
		if(openMember == null){
			openMember = memberService.createOpenMember(WebUtils.getAndSetDefault(request, response), source, source.substring(0,1), userid, WebUtils.getRemoteIp(request));
		}
		Member member = daoService.getObject(Member.class, openMember.getMemberid());
		model.put("memberEncode",  memberService.getAndSetMemberEncode(member).getRetval());
		model.put("member", member);
		return getXmlView(model, "api/mobile/openMemberLogin.vm");
	}
	
	private String checkOpenLoginSource(String source){
		String[] openSources = {MemberConstant.SOURCE_ALIPAY,MemberConstant.SOURCE_SDO,
				MemberConstant.SOURCE_SINA,MemberConstant.SOURCE_QQ,
				MemberConstant.SOURCE_TENCENT,MemberConstant.SOURCE_RENREN,
				MemberConstant.SOURCE_NETEASE,MemberConstant.SOURCE_KAIXIN,
				MemberConstant.SOURCE_MSN,MemberConstant.SOURCE_DOUBAN,MemberConstant.SOURCE_TAOBAO};
		for(String openSource:openSources){
			if(openSource.equalsIgnoreCase(source)){
				return openSource;
			}
		}
		return null;
	}
	
	
	/**
	 * 发送手机动态验证码
	 * @param model
	 * @param request
	 * @return
	 */
	@RequestMapping("/api2/mobile/accounts/sendMobileCheck.xhtml")
	public String sendDynamicCaptcha(String mobile, String memberEncode, ModelMap model,HttpServletRequest request){
		if(StringUtils.isBlank(mobile)){
			return getErrorXmlView(model, ApiConstant.CODE_DATA_ERROR, "手机号不能为空");
		}
		if(!ValidateUtil.isMobile(mobile)){
			return getErrorXmlView(model, ApiConstant.CODE_DATA_ERROR, "手机号输入错误");
		}
		String ip = WebUtils.getRemoteIp(request);
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
		return getXmlView(model, "api/mobile/result.vm");
	}

	@RequestMapping("/api2/mobile/accounts/checkNickname.xhtml")
	public String checkNickname(String nickname, ModelMap model){
		if(StringUtils.isBlank(nickname)) return getErrorXmlView(model, ApiConstant.CODE_DATA_ERROR,"昵称不能为空！");
		String errChar = blogService.filterAllKey(nickname);
		if(StringUtils.isNotBlank(errChar)){
			return getErrorXmlView(model, ApiConstant.CODE_DATA_ERROR,"含有非法关键字！");
		}
		boolean nickExist = memberService.isMemberExists(nickname, null);
		if(nickExist) return getErrorXmlView(model, ApiConstant.CODE_DATA_ERROR, "该昵称已经存在！");
		model.put("result", "true");
		return getXmlView(model, "api/mobile/result.vm");
	}
	
	
	@RequestMapping("/api2/openmember/createOpenMember.xhtml")
	public String getApiUser(String content_encrypt, HttpServletRequest request, ModelMap model){
		//TODO:"htc"使用
		ApiAuth auth = NewApiAuthenticationFilter.getApiAuth();
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
				om = memberService.createOpenMember(AdminCityContant.CITYCODE_SH, source, shortsource, userid, WebUtils.getRemoteIp(request));
			}
		}
		if(om==null) {
			return getErrorXmlView(model, ApiConstant.CODE_MEMBER_NOT_EXISTS, "创建用户失败！");
		}
		Member member = daoService.getObject(Member.class, om.getMemberid());
		ErrorCode<String> memberEncode =  memberService.getAndSetMemberEncode(member);
		model.put("memberEncode", memberEncode.getRetval());
		model.put("memberId", member.getId());
		model.put("nickName", om.getLoginname());
		return getXmlView(model, "api2/member/openMember.vm");
	}

}
