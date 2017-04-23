/**
 * 
 */
package com.gewara.web.action.home;
import java.io.File;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.gewara.Config;
import com.gewara.constant.AdminCityContant;
import com.gewara.constant.BindConstant;
import com.gewara.constant.MemberConstant;
import com.gewara.constant.TagConstant;
import com.gewara.constant.TokenType;
import com.gewara.constant.sys.CacheConstant;
import com.gewara.constant.sys.JsonDataKey;
import com.gewara.constant.sys.LogTypeConstant;
import com.gewara.constant.sys.MongoData;
import com.gewara.json.ValidEmail;
import com.gewara.model.BaseObject;
import com.gewara.model.common.JsonData;
import com.gewara.model.common.VersionCtl;
import com.gewara.model.drama.Drama;
import com.gewara.model.drama.DramaOrder;
import com.gewara.model.drama.Theatre;
import com.gewara.model.draw.DrawActivity;
import com.gewara.model.draw.Prize;
import com.gewara.model.draw.WinnerInfo;
import com.gewara.model.goods.BaseGoods;
import com.gewara.model.movie.Cinema;
import com.gewara.model.movie.Movie;
import com.gewara.model.movie.MoviePlayItem;
import com.gewara.model.pay.BuyItem;
import com.gewara.model.pay.ElecCard;
import com.gewara.model.pay.ElecCardBatch;
import com.gewara.model.pay.GewaOrder;
import com.gewara.model.pay.GoodsOrder;
import com.gewara.model.pay.GymOrder;
import com.gewara.model.pay.MemberAccount;
import com.gewara.model.pay.MemberCardOrder;
import com.gewara.model.pay.OrderExtra;
import com.gewara.model.pay.PubSale;
import com.gewara.model.pay.PubSaleOrder;
import com.gewara.model.pay.SMSRecord;
import com.gewara.model.pay.SpCode;
import com.gewara.model.pay.SpecialDiscount;
import com.gewara.model.pay.SportOrder;
import com.gewara.model.pay.TicketOrder;
import com.gewara.model.sport.Sport;
import com.gewara.model.sport.SportItem;
import com.gewara.model.user.FavoriteTag;
import com.gewara.model.user.Jobs;
import com.gewara.model.user.JobsUp;
import com.gewara.model.user.Member;
import com.gewara.model.user.MemberInfo;
import com.gewara.model.user.MemberInfoMore;
import com.gewara.model.user.Treasure;
import com.gewara.pay.PayUtil;
import com.gewara.service.OperationService;
import com.gewara.service.PlaceService;
import com.gewara.service.bbs.BlogService;
import com.gewara.service.drama.DrawActivityService;
import com.gewara.service.gewapay.ElecCardService;
import com.gewara.service.gewapay.PaymentService;
import com.gewara.service.member.BindMobileService;
import com.gewara.service.member.FavoriteTagService;
import com.gewara.service.order.OrderQueryService;
import com.gewara.service.ticket.TicketDiscountService;
import com.gewara.support.ErrorCode;
import com.gewara.support.VelocityTemplate;
import com.gewara.untrans.CacheService;
import com.gewara.untrans.GewaPicService;
import com.gewara.untrans.gym.SynchGymService;
import com.gewara.untrans.monitor.MonitorService;
import com.gewara.util.BeanUtil;
import com.gewara.util.BindUtils;
import com.gewara.util.ChangeEntry;
import com.gewara.util.DateUtil;
import com.gewara.util.JsonUtils;
import com.gewara.util.ObjectId;
import com.gewara.util.PictureUtil;
import com.gewara.util.StringUtil;
import com.gewara.util.ValidateUtil;
import com.gewara.util.VmUtils;
import com.gewara.util.WebUtils;
import com.gewara.util.XSSFilter;
import com.gewara.web.action.BaseHomeController;
import com.gewara.web.util.PageUtil;
import com.gewara.xmlbind.gym.CardItem;
/**
 * @author hxs(ncng_2006@hotmail.com)
 * @since Jan 27, 2010 10:09:44 AM
 */
@Controller
public class MemberAccountController extends BaseHomeController {
	public static final List<String> CHECK_LIST = Arrays.asList("bind", "change");
	private static final String WARNING_TIME = "2013-05-30 00:00:00";
	@Autowired@Qualifier("monitorService")
	private MonitorService monitorService;
	public void setMonitorService(MonitorService monitorService) {
		this.monitorService = monitorService;
	}
	@Autowired@Qualifier("synchGymService")
	private SynchGymService synchGymService;
	public void setSynchGymService(SynchGymService synchGymService) {
		this.synchGymService = synchGymService;
	}
	@Autowired@Qualifier("orderQueryService")
	private OrderQueryService orderQueryService;
	public void setOrderQueryService(OrderQueryService orderQueryService) {
		this.orderQueryService = orderQueryService;
	}
	@Autowired@Qualifier("placeService")
	private PlaceService placeService;
	public void setPlaceService(PlaceService placeService) {
		this.placeService = placeService;
	}
	@Autowired@Qualifier("config")
	private Config config;
	public void setConfig(Config config) {
		this.config = config;
	}
	@Autowired@Qualifier("bindMobileService")
	private BindMobileService bindMobileService;
	public void setBindMobileService(BindMobileService bindMobileService){
		this.bindMobileService = bindMobileService;
	}
	@Autowired@Qualifier("gewaPicService")
	private GewaPicService gewaPicService;
	public void setGewaPicService(GewaPicService gewaPicService) {
		this.gewaPicService = gewaPicService;
	}
	@Autowired@Qualifier("operationService")
	private OperationService operationService;
	public void setOperationService(OperationService operationService) {
		this.operationService = operationService;
	}
	@Autowired@Qualifier("velocityTemplate")
	private VelocityTemplate velocityTemplate;
	public void setVelocityTemplate(VelocityTemplate velocityTemplate) {
		this.velocityTemplate = velocityTemplate;
	}
	 
	@Autowired@Qualifier("blogService")
	private BlogService blogService;
	public void setBlogService(BlogService blogService) {
		this.blogService = blogService;
	}

	@Autowired@Qualifier("drawActivityService")
	private DrawActivityService drawActivityService;
	public void setDrawActivityService(DrawActivityService drawActivityService) {
		this.drawActivityService = drawActivityService;
	}
	@Autowired@Qualifier("elecCardService")
	private ElecCardService elecCardService;
	public void setElecCardService(ElecCardService elecCardService) {
		this.elecCardService = elecCardService;
	}
	@Autowired@Qualifier("favoriteTagService")
	private FavoriteTagService favoriteTagService;
	public void setFavoriteTagService(FavoriteTagService favoriteTagService) {
		this.favoriteTagService = favoriteTagService;
	}
	@Autowired@Qualifier("cacheService")
	private CacheService cacheService;
	public void setCacheService(CacheService cacheService) {
		this.cacheService = cacheService;
	}
	@Autowired@Qualifier("paymentService")
	private PaymentService paymentService;
	public void setPaymentService(PaymentService paymentService) {
		this.paymentService = paymentService;
	}
	@Autowired@Qualifier("ticketDiscountService")
	private TicketDiscountService ticketDiscountService;
	
		
	//安全中心
	@RequestMapping("/home/acct/safetyCenter.xhtml")
	public String safetyCenter(ModelMap model, HttpServletRequest request) {
		Member member = getLogonMember();
		if (member == null) {
			return gotoLogin("/home/acct/safetyCenter.xhtml", request, model);
		}
		model.putAll(controllerService.getCommonData(model, member, member.getId()));
		model.put("member", member);
		MemberInfo memberInfo = (MemberInfo) model.get("memberInfo");
		if(memberInfo!=null && StringUtils.isNotBlank(memberInfo.getNewtask())){
			model.put("sendReg",StringUtils.contains(memberInfo.getNewtask(), "confirmreg"));
			model.put("bindmobile",StringUtils.contains(memberInfo.getNewtask(), "bindmobile"));
		}else {
			model.put("sendReg", false);
			model.put("bindmobile", false);
		}
		MemberAccount memberAccount = daoService.getObjectByUkey(MemberAccount.class, "memberid", member.getId(), true);
		if(memberAccount == null || memberAccount.isNopassword()) {
			model.put("noPayPass", true);
		}
		if(memberAccount != null && !memberAccount.isNopassword() && StringUtils.contains(memberInfo.getNewtask(), "bindmobile")){
			model.put("level", "h");
		}else if (StringUtils.contains(memberInfo.getNewtask(), "bindmobile")) {
			model.put("level", "m");
		}else {
			model.put("level", "l");
		}
		if (memberAccount==null||memberAccount.getUpdatetime().before(DateUtil.parseTimestamp(WARNING_TIME))) {
			model.put("strong", "weak");
		}
		return "home/acct/accountSafety/safetyCenter.vm";
	}
	
	//绑定手机
	@RequestMapping("/home/acct/bindMobile.xhtml")
	public String bindMobile(ModelMap model,HttpServletRequest request){
		boolean isNeedCaptha = bindMobileService.isNeedToken(TokenType.BindMobile, WebUtils.getRemoteIp(request), 2);
		model.put("needCaptcha", isNeedCaptha);
		Member member = getLogonMember();
		MemberAccount memberAccount = daoService.getObjectByUkey(MemberAccount.class, "memberid", member.getId(), false);
		model.put("hasPayPass", memberAccount==null?false:!memberAccount.isNopassword());
		
		if(!member.isBindMobile())
			return "home/acct/accountSafety/bindMobile.vm";
		else{
			model.put("mobile", member.getMobile());
			return "home/acct/accountSafety/successBindMobile.vm";
		}
	}
	@RequestMapping("/home/acct/bindMobilePopup.xhtml")
	public String bindMobilePopup(ModelMap model){
		Member member = getLogonMember();
		MemberAccount memberAccount = daoService.getObjectByUkey(MemberAccount.class, "memberid", member.getId(), false);
		model.put("hasPayPass", memberAccount==null?false:!memberAccount.isNopassword());
		
		if(!member.isBindMobile())
			return "home/acct/accountSafety/bindMobilePopup.vm";
		else{
			model.put("mobile", member.getMobile());
			return "home/acct/accountSafety/successBMPopup.vm";
		}
	}
	@RequestMapping("/ajax/mobile/validDMPopup.xhtml")
	public String validDM(HttpServletRequest request , ModelMap model){
		boolean isNeedCaptha = bindMobileService.isNeedToken(TokenType.DrawCheck, WebUtils.getRemoteIp(request), 2);
		model.put("needCaptcha", isNeedCaptha);
		return "home/acct/accountSafety/validDrawMobile.vm";
	}
	//修改绑定手机
	@RequestMapping("/home/acct/changeBindMobile.xhtml")
	public String changeBindMobile(String authType, ModelMap model, HttpServletRequest request){
		boolean isNeedCaptha = false;
		if (bindMobileService.isNeedToken(TokenType.ChgBMAuth, WebUtils.getRemoteIp(request), 5)
				|| bindMobileService.isNeedToken(TokenType.LoadOldMblCkPs, WebUtils.getRemoteIp(request), 2)) {
			isNeedCaptha = true;
		}
		model.put("needCaptcha", isNeedCaptha);
		Member member = getLogonMember();
		if(!memberService.canChangeMobile(member)){
			model.put("msg", "手机绑定后7天内不能修改绑定！");
			return "redirect:/home/acct/safetyCenter.xhtml";			
		}
		if(!member.isBindMobile()){
			return "home/acct/accountSafety/bindMobile.vm";
		}else{
			if (StringUtils.equals(authType, "id")) {
				return "home/acct/accountSafety/chgBMIDAuth.vm";
			}else if (StringUtils.equals(authType, "oldmobile")) {
				model.put("mobile", member.getMobile());
				MemberAccount account = daoService.getObjectByUkey(MemberAccount.class, "memberid", member.getId(), false);
				if(account != null && !account.isNopassword()){
					model.put("hasPayAccount", true);
				}else {
					model.put("hasPayAccount", false);
				}
				return "home/acct/accountSafety/chgBMOldMblAuth.vm";
			}else if(StringUtils.equals(authType, "paypass")){
				return "home/acct/accountSafety/chgBMPassAuth.vm";
			}
			return alertMessage(model, "非法请求！", "index.xhtml");
		}
	}

	@RequestMapping("/home/acct/chgBMAuth.xhtml")
	public String chgBMAuth(String password, String realname, String idcard, Integer certtype,String mobile, HttpServletRequest request, String captchaId, String captcha,ModelMap model) {
		Member member = getLogonMember();
		String ip = WebUtils.getRemoteIp(request);
		Map jsonMap = new HashMap();
		Map errorMap = new HashMap();
		boolean iscaptcha = bindMobileService.isNeedToken(TokenType.ChgBMAuth, ip, 5);
		jsonMap.put("errorMap", errorMap);
		if(iscaptcha){
			jsonMap.put("refreshCaptcha", "true");
			model.put("iscaptcha", iscaptcha);
			if(StringUtils.isBlank(captcha)){
				errorMap.put("captcha", "请输入验证码！");
				return showJsonError(model, jsonMap);
			}else{
				boolean isValidCaptcha = controllerService.validateCaptcha(captchaId, captcha,ip);
				if(!isValidCaptcha){
					errorMap.put("captcha", "验证码错误！");
					return showJsonError(model, jsonMap);
				}
			}
		}
		boolean next = bindMobileService.getAndUpdateToken(TokenType.ChgBMAuth, ip, 5);
		if(next){
			jsonMap.put("refreshCaptcha", "true");
		}
		
		String opkey = BindConstant.TAG_CHGBINDMOBILE + member.getId();
		boolean allow = operationService.isAllowOperation(opkey, 5, OperationService.ONE_DAY, 10);
		if(!allow){
			dbLogger.warn(BindConstant.TAG_CHGBINDMOBILE+" ip:" + ip);
			return showJsonError(model, "您操作过于频繁，请稍后再试！");
		}
		operationService.updateOperation(opkey, 5, OperationService.ONE_DAY, 10);
		
		if(!memberService.canChangeMobile(member)){
			errorMap.put("msg", "手机绑定后7天内不能修改绑定！");
			return showJsonError(model, jsonMap);
		}
		if (!ValidateUtil.isMobile(mobile)){
			errorMap.put("mobile", "手机号格式错误！");
			return showJsonError(model,jsonMap );
		}
		if (StringUtils.isBlank(realname)){
			errorMap.put("realname", "真实姓名不能为空！");
			return showJsonError(model, jsonMap);
		}
		if (StringUtil.getByteLength(realname) > 30){
			errorMap.put("realname",  "真实姓名过长！");
			return showJsonError(model,jsonMap);
		}
		if (StringUtils.isBlank(idcard)){
			errorMap.put("idcard", "证件号码不能为空！");
			return showJsonError(model,jsonMap);
		}
		if (!StringUtil.regMatch(idcard, "^[A-Za-z0-9_]{6,30}$", true)){
			errorMap.put("idcard", "证件号码格式不正确,只能是字母，数字，下划线，长度6—30位！");
			return showJsonError(model,jsonMap );
		}
		if (certtype == null || StringUtil.getByteLength(certtype + "") != 1){
			errorMap.put("certtype", "证件类型不正确！");
			return showJsonError(model, jsonMap);
		}
		if (StringUtils.isNotBlank(password)) {
			if (!StringUtil.regMatch(password, "^[a-zA-Z0-9]{6,18}$", true)){
				errorMap.put("password", "密码格式错误！");
				return showJsonError(model, jsonMap);
			}
		} else {
			errorMap.put("password", "密码不能为空！");
			return showJsonError(model,jsonMap );
		}
		
		MemberAccount account = daoService.getObjectByUkey(MemberAccount.class, "memberid", member.getId(), false);
		if(account == null){
			return showJsonError(model, "该用户尚未设置支付密码！");
		}

		if (!StringUtils.equals(realname, account.getRealname())) {
			errorMap.put("realname", "真实姓名错误！");
			return showJsonError(model,jsonMap );
		}
		if (!StringUtils.equals(mobile,member.getMobile())) {
			errorMap.put("mobile", "原手机号错误！");
			return showJsonError(model,jsonMap );
		}
		if (certtype!=account.getCerttype()) {
			errorMap.put("certtype", "证件类型错误！");
			return showJsonError(model, jsonMap);
		}
		if (!StringUtils.equals(paymentService.getEncryptIdcard(StringUtil.ToDBC(idcard)), account.getEncryidcard())) {
			errorMap.put("idcard", "证件号错误！");
			return showJsonError(model, jsonMap);
		}
		if(!PayUtil.passEquals(password, account.getPassword())){
			errorMap.put("password", "支付密码错误！");
			return showJsonError(model, jsonMap);
		}
		
		String encode = PayUtil.md5WithKey(member.getId()+"",paymentService.getEncryptIdcard(StringUtil.ToDBC(idcard)),mobile,PayUtil.getPass(password));
		jsonMap.put("retval", encode);
		return showJsonSuccess(model,jsonMap);
	}
	@RequestMapping("/home/acct/chgBMOldMblAuth.xhtml")
	public String chgBMOldMblAuth(String checkpass,ModelMap model){
		Member member = getLogonMember();
		if(StringUtils.isBlank(checkpass)) {
			return showJsonError(model,"动态码错误！");
		}
		ErrorCode code = bindMobileService.checkBindMobile(BindConstant.TAG_CHGBINDMOBILE, member.getMobile(), checkpass);
		if(!code.isSuccess()){
			return showJsonError(model, code.getMsg());
		}
		MemberAccount account = daoService.getObjectByUkey(MemberAccount.class, "memberid", member.getId(), false);
		String encode = PayUtil.md5WithKey(member.getId()+"",account==null?null:account.getEncryidcard(),member.getMobile(),account==null?null:account.getPassword());
		return showJsonSuccess(model,encode);
	}
	@RequestMapping("/home/acct/loadOldMblCkPs.xhtml")
	public String loadOldMblCkPs(HttpServletRequest request, String captchaId, String captcha, ModelMap model){
		Member member = getLogonMember();
		String ip = WebUtils.getRemoteIp(request);
		Map jsonMap = new HashMap();
		Map errorMap = new HashMap();
		boolean iscaptcha = bindMobileService.isNeedToken(TokenType.LoadOldMblCkPs, ip, 2);
		jsonMap.put("errorMap", errorMap);
		if(iscaptcha){
			jsonMap.put("refreshCaptcha", "true");
			model.put("iscaptcha", iscaptcha);
			if(StringUtils.isBlank(captcha)){
				errorMap.put("captcha", "请输入验证码！");
				return showJsonError(model, jsonMap);
			}else{
				boolean isValidCaptcha = controllerService.validateCaptcha(captchaId, captcha,ip);
				errorMap.put("captcha", "验证码错误！");
				if(!isValidCaptcha) return showJsonError(model, jsonMap);
			}
		}
		boolean next = bindMobileService.getAndUpdateToken(TokenType.LoadOldMblCkPs, ip, 2);
		if(next){
			jsonMap.put("refreshCaptcha", "true");
		}
		
		if(!memberService.canChangeMobile(member)){
			return showJsonError(model, "手机绑定后7天内不能修改绑定！");
		}
		
		ErrorCode<SMSRecord> code = bindMobileService.refreshBindMobile(BindConstant.TAG_CHGBINDMOBILE, member.getMobile(), ip);
		if(!code.isSuccess()) return showJsonError(model, code.getMsg());
		untransService.sendMsgAtServer(code.getRetval(), false);
		jsonMap.put("retval", "成功发送动态码！");
		return showJsonSuccess(model,jsonMap);
	}
	@RequestMapping("/home/acct/chgBMPassAuth.xhtml")
	public String chgBMPassAuth(String password, HttpServletRequest request, String captchaId, String captcha,ModelMap model) {
		Member member = getLogonMember();
		String ip = WebUtils.getRemoteIp(request);
		Map jsonMap = new HashMap();
		Map errorMap = new HashMap();
		boolean iscaptcha = bindMobileService.isNeedToken(TokenType.ChgBMAuth, ip, 5);
		jsonMap.put("errorMap", errorMap);
		if(iscaptcha){
			jsonMap.put("refreshCaptcha", "true");
			model.put("iscaptcha", iscaptcha);
			if(StringUtils.isBlank(captcha)){
				errorMap.put("captcha", "请输入验证码！");
				return showJsonError(model, jsonMap);
			}else{
				boolean isValidCaptcha = controllerService.validateCaptcha(captchaId, captcha,ip);
				if(!isValidCaptcha){
					errorMap.put("captcha", "验证码错误！");
					return showJsonError(model, jsonMap);
				}
			}
		}
		boolean next = bindMobileService.getAndUpdateToken(TokenType.ChgBMAuth, ip, 5);
		if(next){
			jsonMap.put("refreshCaptcha", "true");
		}
		String opkey = BindConstant.TAG_CHGBINDMOBILE + member.getId();
		boolean allow = operationService.isAllowOperation(opkey, 5, OperationService.ONE_DAY, 10);
		if(!allow){
			dbLogger.warn(BindConstant.TAG_CHGBINDMOBILE+" ip:" + ip);
			return showJsonError(model, "您操作过于频繁，请稍后再试！");
		}
		operationService.updateOperation(opkey, 5, OperationService.ONE_DAY, 10);
		
		
		if(!memberService.canChangeMobile(member)){
			errorMap.put("msg", "手机绑定后7天内不能修改绑定！");
			return showJsonError(model, jsonMap);
		}
		if (!ValidateUtil.isPassword(password)) {
			errorMap.put("password", "密码格式错误！");
			return showJsonError(model, jsonMap);
		}
		
		MemberAccount account = daoService.getObjectByUkey(MemberAccount.class, "memberid", member.getId(), false);
		if(account == null){
			return showJsonError(model, "您尚未设置支付密码！");
		}
		if(!PayUtil.passEquals(password, account.getPassword())){
			errorMap.put("password", "支付密码错误！");
			return showJsonError(model, jsonMap);
		}
		
		String encode = PayUtil.md5WithKey(member.getId()+"",account.getEncryidcard(),member.getMobile(),PayUtil.getPass(password));
		jsonMap.put("retval", encode);
		return showJsonSuccess(model,jsonMap);
	}
	@RequestMapping("/home/acct/chgBMIDAuth.xhtml")
	public String chgBMIDAuth(String realname, String idcard, Integer certtype,String mobile, HttpServletRequest request, String captchaId, String captcha,ModelMap model) {
		Member member = getLogonMember();
		String ip = WebUtils.getRemoteIp(request);
		Map jsonMap = new HashMap();
		Map errorMap = new HashMap();
		boolean iscaptcha = bindMobileService.isNeedToken(TokenType.ChgBMAuth, ip, 5);
		jsonMap.put("errorMap", errorMap);
		if(iscaptcha){
			jsonMap.put("refreshCaptcha", "true");
			model.put("iscaptcha", iscaptcha);
			if(StringUtils.isBlank(captcha)){
				errorMap.put("captcha", "请输入验证码！");
				return showJsonError(model, jsonMap);
			}else{
				boolean isValidCaptcha = controllerService.validateCaptcha(captchaId, captcha,ip);
				if(!isValidCaptcha){
					errorMap.put("captcha", "验证码错误！");
					return showJsonError(model, jsonMap);
				}
			}
		}
		boolean next = bindMobileService.getAndUpdateToken(TokenType.ChgBMAuth, ip, 5);
		if(next){
			jsonMap.put("refreshCaptcha", "true");
		}
		
		if(!memberService.canChangeMobile(member)){
			errorMap.put("msg", "手机绑定后7天内不能修改绑定！");
			return showJsonError(model, jsonMap);
		}
		if (!ValidateUtil.isMobile(mobile)){
			errorMap.put("mobile", "手机号格式错误！");
			return showJsonError(model,jsonMap );
		}
		if (StringUtils.isBlank(realname)){
			errorMap.put("realname", "真实姓名不能为空！");
			return showJsonError(model, jsonMap);
		}
		if (StringUtil.getByteLength(realname) > 30){
			errorMap.put("realname",  "真实姓名过长！");
			return showJsonError(model,jsonMap);
		}
		if (StringUtils.isBlank(idcard)){
			errorMap.put("idcard", "证件号码不能为空！");
			return showJsonError(model,jsonMap);
		}
		if (!StringUtil.regMatch(idcard, "^[A-Za-z0-9_]{6,30}$", true)){
			errorMap.put("idcard", "证件号码格式不正确,只能是字母，数字，下划线，长度6—30位！");
			return showJsonError(model,jsonMap );
		}
		if (certtype == null || StringUtil.getByteLength(certtype + "") != 1){
			errorMap.put("certtype", "证件类型不正确！");
			return showJsonError(model, jsonMap);
		}
		
		MemberAccount account = daoService.getObjectByUkey(MemberAccount.class, "memberid", member.getId(), false);
		if(account == null){
			return showJsonError(model, "您尚未设置支付密码！");
		}

		if (!StringUtils.equals(realname, account.getRealname())) {
			errorMap.put("realname", "真实姓名错误！");
			return showJsonError(model,jsonMap );
		}
		if (!StringUtils.equals(mobile,member.getMobile())) {
			errorMap.put("mobile", "原手机号错误！");
			return showJsonError(model,jsonMap );
		}
		if (certtype!=account.getCerttype()) {
			errorMap.put("certtype", "证件类型错误！");
			return showJsonError(model, jsonMap);
		}
		if (!StringUtils.equals(paymentService.getEncryptIdcard(StringUtil.ToDBC(idcard)), account.getEncryidcard())) {
			errorMap.put("idcard", "证件号错误！");
			return showJsonError(model, jsonMap);
		}
		String opkey = BindConstant.TAG_CHGBINDMOBILE + member.getId();
		boolean allow = operationService.isAllowOperation(opkey, 5, OperationService.ONE_DAY, 10);
		if(!allow){
			dbLogger.warn(BindConstant.TAG_CHGBINDMOBILE+" ip:" + ip);
			return showJsonError(model, "您操作过于频繁，请稍后再试！");
		}
		operationService.updateOperation(opkey, 5, OperationService.ONE_DAY, 10);
		
		String encode = PayUtil.md5WithKey(member.getId()+"",paymentService.getEncryptIdcard(StringUtil.ToDBC(idcard)),mobile,account.getPassword());
		jsonMap.put("retval", encode);
		return showJsonSuccess(model,jsonMap);
	}
	@RequestMapping("/home/acct/newBindMobile.xhtml")
	public String newBindMobile(ModelMap model,String encode,HttpServletRequest request){
		boolean isNeedCaptha = bindMobileService.isNeedToken(TokenType.BindMobile, WebUtils.getRemoteIp(request), 2);
		model.put("needCaptcha", isNeedCaptha);
		Member member = getLogonMember();
		MemberAccount account = daoService.getObjectByUkey(MemberAccount.class, "memberid", member.getId(), false);
		/*if(account == null) 
			return show404(model, "请先设置支付密码！");*/
		if(!StringUtils.equals(encode, PayUtil.md5WithKey(member.getId()+"",account==null?null:account.getEncryidcard(),member.getMobile(),account==null?null:account.getPassword())))
			return show404(model, "非法操作！");
		return "home/acct/accountSafety/newBM.vm";
	}
	//更改绑定手机
	@RequestMapping("/home/acct/sendChgBm.xhtml")
	public String sendChgBm(@CookieValue(LOGIN_COOKIE_NAME)String sessid,  
			HttpServletRequest request, String mobile, String checkpass, String encode, ModelMap model){
		Member member = getLogonMember();
		if(!StringUtils.isNotBlank(encode)) return showJsonError(model, "非法操作！");
		MemberAccount account = daoService.getObjectByUkey(MemberAccount.class, "memberid", member.getId(), false);
		//TODO:什么东东？？
		if(!StringUtils.equals(encode, PayUtil.md5WithKey(member.getId()+"",account==null? null:account.getEncryidcard(), member.getMobile(), account==null?null:account.getPassword())))
			return showJsonError(model, "非法操作！");
		
		if(StringUtils.isBlank(checkpass))return showJsonError(model, "手机动态码不能为空！");
		if(!ValidateUtil.isMobile(mobile)) return showJsonError(model, "手机号码格式不正确！");
		String oldMobile = member.getMobile();

		ErrorCode code = memberService.changeBind(member, mobile, checkpass, WebUtils.getRemoteIp(request));
		if(!code.isSuccess()) return showJsonError(model, code.getMsg());
		loginService.updateMemberAuth(sessid, member);
		
		sendWarning("手机号", member, oldMobile, member.getEmail());
		return showJsonSuccess(model);
	}
	
	//绑定手机
	@RequestMapping("/home/acct/sendBindMobile.xhtml")
	public String sendBindMobile(@CookieValue(LOGIN_COOKIE_NAME)String sessid,  
			HttpServletRequest request, String mobile, String checkpass, String password, ModelMap model){
		Member member = getLogonMember();
		if(StringUtils.isBlank(checkpass))return showJsonError(model, "手机动态码不能为空！");
		if(!ValidateUtil.isMobile(mobile)) return showJsonError(model, "手机号码格式不正确！");
		if(member.isBindMobile()) return showJsonError(model,"您已绑定手机！");
		boolean exists = memberService.isMemberMobileExists(mobile);
		if(exists) return showJsonError(model, "该号码已绑定其他帐号，请更换其他手机号码！");
		MemberAccount memberAccount = daoService.getObjectByUkey(MemberAccount.class, "memberid", member.getId(), false);
		if(memberAccount != null && !memberAccount.isNopassword()){
			if(StringUtils.isBlank(password)) return showJsonError(model, "支付密码不能空！");
			if(!StringUtils.equals(memberAccount.getPassword(), PayUtil.getPass(password))) return showJsonError(model, "支付密码错误！");
		}
		ErrorCode code = memberService.bindMobile(member, mobile, checkpass, WebUtils.getRemoteIp(request));
		if(!code.isSuccess()) return showJsonError(model, code.getMsg());
		loginService.updateMemberAuth(sessid, member);

		sendWarning("手机号", member, member.getMobile(),member.getEmail());
		return showJsonSuccess(model);
	}
	//验证抽奖手机
	@RequestMapping("/ajax/mobile/validDrawMobile.xhtml")
	public String validDrawMobile(HttpServletRequest request, HttpServletResponse response, String mobile, String checkpass, ModelMap model){
		if(StringUtils.isBlank(checkpass))return showJsonError(model, "手机动态码不能为空！");
		if(!ValidateUtil.isMobile(mobile)) return showJsonError(model, "手机号码格式不正确！");
		ErrorCode code = bindMobileService.checkBindMobile(BindConstant.TAG_DRAWMOBILE, mobile, checkpass);
		if(!code.isSuccess()) return showJsonError(model, code.getMsg());
		Map<String, String> logInfo = new HashMap<String, String>();
		logInfo.put("mobile", VmUtils.getSmobile(mobile));
		
		String citycode = WebUtils.getAndSetDefault(request, response);
		ErrorCode<Member> memberCode = memberService.createMemberWithDrawMobile(mobile, checkpass, citycode, WebUtils.getRemoteIp(request));
		if(!memberCode.isSuccess()) return showJsonError(model, memberCode.getMsg());
		Member member = memberCode.getRetval();
		loginService.autoLoginByDyncode(request, response, member);
		monitorService.saveMemberLogMap(member.getId(), MemberConstant.ACTION_VDDRAWMOBILE, logInfo, WebUtils.getRemoteIp(request));
		return showJsonSuccess(model,StringUtil.md5(mobile + "drawmobileauthsh", "utf-8"));
	}
	//设置密码
	@RequestMapping("/home/acct/setPass.xhtml")
	public String setPass(ModelMap model,HttpServletRequest request) {
		Member member = getLogonMember();
		if(member==null){
			return gotoLogin("/home/acct/setPass.xhtml", request, model);
		}
		MemberInfo memberInfo = daoService.getObject(MemberInfo.class, member.getId());
		if(!StringUtils.equals(VmUtils.getJsonValueByKey(memberInfo.getOtherinfo(), MemberConstant.TAG_SOURCE), "fail")) 
		return show404(model, "该用户已设置过登录密码！");
		
		return "home/acct/accountSafety/setPass.vm";
	}
	
	//修改登录邮箱
	@RequestMapping("/home/acct/goUpEmail.xhtml")
	public String goUpEmail(ModelMap model,HttpServletRequest request) {
		Member member = getLogonMember();
		if(member==null){
			return gotoLogin("/home/acct/goUpEmail.xhtml", request, model);
		}
		String email = member.getEmail();
		if (StringUtils.isNotBlank(email)) {
			model.put("email", email);
		}
		return "home/acct/accountSafety/upEmail.vm";
	}
	
	//修改密码前置验证页面
	@RequestMapping("/home/acct/goUpPass.xhtml")
	public String goUpPass(ModelMap model,HttpServletRequest request) {
		boolean isNeedCaptha = bindMobileService.isNeedToken(TokenType.SendVDEmail, WebUtils.getRemoteIp(request), 2);
		model.put("needCaptcha", isNeedCaptha);
		Member member = getLogonMember();
		String mobile = member.getMobile();
		String email = member.getEmail();
		if (StringUtils.isNotBlank(mobile)&&StringUtils.isNotBlank(email)) {
			model.put("condition", "mobile and email");
			model.put("email", email);
		}else if (StringUtils.isNotBlank(mobile)) {
			model.put("danger", request.getParameter("danger"));
			return "redirect:/home/acct/mbrMobileAuth.xhtml";
		}else if (StringUtils.isNotBlank(email)){
			model.put("condition", "email");
			model.put("email", email);
		}else{
			model.put("condition", "none");
		}
		return "home/acct/accountSafety/goUpPass.vm";
	}

	//验证邮箱链接有效性，如有效跳转到密改页面
	@RequestMapping("/mbrIdtAuthEml.xhtml")
	public String mbrIdtAuthEml(@CookieValue(value = LOGIN_COOKIE_NAME, required = false)String sessid, ModelMap model, Long memberid, String encode, String uuid,HttpServletRequest request){
		if(StringUtils.isBlank(encode) || memberid == null|| StringUtils.isBlank(uuid)) return show404(model, "连接错误！");
		ValidEmail validEmail = mongoService.getObject(ValidEmail.class, MongoData.DEFAULT_ID_NAME, uuid);
		if(validEmail == null){
			dbLogger.error("ip:" + WebUtils.getRemoteIp(request) + ", memberid:" + memberid);
			return show404(model, "此链接无效！");
		}
		Member member = daoService.getObject(Member.class, memberid);
		if(member==null) return show404(model, "用户不存在！");
		
		Member logMember = loginService.getLogonMemberBySessid(WebUtils.getRemoteIp(request), sessid);
		if(logMember == null || !logMember.equals(member)){
			return gotoLogin("/mbrIdtAuthEml.xhtml", request, model);
		}
		
		String checkMsg = checkMemberResource(member);
		if(StringUtils.isNotBlank(checkMsg)) return show404(model, checkMsg);
		String email = member.getEmail();
		Long cur = System.currentTimeMillis();
		if(cur > validEmail.getValidtime()) return show404(model, "连接已超时，请重新获取！");
		if(!StringUtils.equals(PayUtil.md5WithKey(email, "" + member.getId(), uuid), encode)) return show404(model, "此链接无效！");
		model.put("type","email");
		return "home/acct/accountSafety/upPass.vm";
	}
	//验证动态码有效性，如有效跳转到密改页面
	@RequestMapping("/home/acct/mbrIdtAuthCkPs.xhtml")
	public String mbrIdtAuthCkPs(String checkpass,ModelMap model){
		Member member = getLogonMember();
		if(StringUtils.isBlank(checkpass)) {
			return show404(model, "非法操作！");
		}
		ErrorCode code = bindMobileService.preCheckBindMobile(BindConstant.TAG_MODIFYPASS, member.getMobile(), checkpass);
		if(!code.isSuccess()){
			return show404(model, "非法操作！");
		}
		model.put("type","mobile");
		return "home/acct/accountSafety/upPass.vm";
	}
	//手机动态码验证页面
	@RequestMapping("/home/acct/mbrMobileAuth.xhtml")
	public String mbrMobileAuth(HttpServletRequest request,ModelMap model){
		boolean isNeedCaptha = bindMobileService.isNeedToken(TokenType.LoadCheckPass, WebUtils.getRemoteIp(request), 2);
		model.put("needCaptcha", isNeedCaptha);
		Member member = getLogonMember();
		model.put("mobile", member.getMobile());
		return "home/acct/accountSafety/mobileauth.vm";
	}
	@RequestMapping("/home/acct/VDCkPs.xhtml")
	public String validateCheckPass(String checkpass,ModelMap model){
		Member member = getLogonMember();
		if(StringUtils.isBlank(checkpass)) {
			return showJsonError(model,"动态码错误！");
		}
		ErrorCode code = bindMobileService.preCheckBindMobile(BindConstant.TAG_MODIFYPASS, member.getMobile(), checkpass);
		if(!code.isSuccess()){
			return showJsonError(model, code.getMsg());
		}
		model.put("checkpass", checkpass);
		return showJsonSuccess(model);
	}
	//发送验证邮件
	@RequestMapping("/home/acct/sendVDEmail.xhtml")
	public String sendVDEmail(ModelMap model, HttpServletRequest request, String captchaId, String captcha){
		Member member = getLogonMember();
		String ip = WebUtils.getRemoteIp(request);
		Map jsonMap = new HashMap();
		Map errorMap = new HashMap();
		boolean iscaptcha = bindMobileService.isNeedToken(TokenType.SendVDEmail, ip, 2);
		jsonMap.put("errorMap", errorMap);
		if(iscaptcha){
			jsonMap.put("refreshCaptcha", "true");
			model.put("iscaptcha", iscaptcha);
			if(StringUtils.isBlank(captcha)){
				errorMap.put("captcha", "请输入验证码！");
				return showJsonError(model, jsonMap);
			}else{
				boolean isValidCaptcha = controllerService.validateCaptcha(captchaId, captcha,ip);
				errorMap.put("captcha", "验证码错误！");
				if(!isValidCaptcha) return showJsonError(model, jsonMap);
			}
		}
		boolean next = bindMobileService.getAndUpdateToken(TokenType.SendVDEmail, ip, 2);
		if(next){
			jsonMap.put("refreshCaptcha", "true");
		}
		
		String email = member.getEmail();
		if (StringUtils.isBlank(email)) {
			return showJsonError(model,"未绑定邮箱！");
		}
		Long memberid = member.getId();
		String opkey = BindConstant.TAG_VDEMAIL_BY_UPDATEPWD + memberid;
		boolean allow = operationService.isAllowOperation(opkey, 60, OperationService.ONE_DAY, 5);
		if(!allow){
			dbLogger.warn(BindConstant.TAG_VDEMAIL_BY_UPDATEPWD+" ip :" + ip + ", memberid:" + memberid);
			return showJsonError(model, "操作过于频繁，请稍后再试！");
		}
		String checkMsg = checkMemberResource(member);
		if(StringUtils.isNotBlank(checkMsg)) return showJsonError(model, checkMsg);
		Long validtime = System.currentTimeMillis() + DateUtil.m_minute*30;
		String validcode = StringUtil.md5(email + StringUtils.substring(member.getPassword(), 8, 24));
		ValidEmail validEmail = new ValidEmail(email, validtime, validcode, ValidEmail.TYPE_PASSWORD);
		mongoService.saveOrUpdateObject(validEmail, MongoData.DEFAULT_ID_NAME);
		gewaMailService.sendValidateEmail(member,validEmail.getId());
		operationService.updateOperation(opkey,60, OperationService.ONE_DAY, 5);
		dbLogger.warn("Success send ValidateEmail By Modfiy Password - memberid:" + member.getId() + ", ip:" + ip);
		jsonMap.put("retval", "认证邮件已发至您的邮箱，请点击邮件中的认证链接完成认证！");
		return showJsonSuccess(model,jsonMap);
	}
	//获取动态码
	@RequestMapping("/home/acct/loadCheckPass.xhtml")
	public String loadCheckPass(HttpServletRequest request, String captchaId, String captcha, ModelMap model){
		Member member = getLogonMember();
		String ip = WebUtils.getRemoteIp(request);
		Map jsonMap = new HashMap();
		Map errorMap = new HashMap();
		boolean iscaptcha = bindMobileService.isNeedToken(TokenType.LoadCheckPass, ip, 2);
		jsonMap.put("errorMap", errorMap);
		if(iscaptcha){
			jsonMap.put("refreshCaptcha", "true");
			model.put("iscaptcha", iscaptcha);
			if(StringUtils.isBlank(captcha)){
				errorMap.put("captcha", "请输入验证码！");
				return showJsonError(model, jsonMap);
			}else{
				boolean isValidCaptcha = controllerService.validateCaptcha(captchaId, captcha,ip);
				errorMap.put("captcha", "验证码错误！");
				if(!isValidCaptcha) return showJsonError(model, jsonMap);
			}
		}
		boolean next = bindMobileService.getAndUpdateToken(TokenType.LoadCheckPass, ip, 2);
		if(next){
			jsonMap.put("refreshCaptcha", "true");
		}
		
		String checkMsg = checkMemberResource(member);
		if(StringUtils.isNotBlank(checkMsg)) return showJsonError(model, checkMsg);
		ErrorCode<SMSRecord> code = bindMobileService.refreshBindMobile(BindConstant.TAG_MODIFYPASS, member.getMobile(), ip);
		if(!code.isSuccess()) return showJsonError(model, code.getMsg());
		untransService.sendMsgAtServer(code.getRetval(), false);
		jsonMap.put("retval", "成功发送动态码！");
		return showJsonSuccess(model,jsonMap);
	}
	
	private String checkMemberResource(Member member){
		MemberInfo memberInfo = daoService.getObject(MemberInfo.class, member.getId());
		if(StringUtils.equals(memberInfo.getSource(), MemberConstant.REGISTER_APP) || StringUtils.equals(memberInfo.getSource(), MemberConstant.REGISTER_CODE)){
			Map<String,String> otherInfoMap = JsonUtils.readJsonToMap(memberInfo.getOtherinfo());
			if(StringUtils.equals(otherInfoMap.get(MemberConstant.TAG_SOURCE), "fail")){
				return "该帐号还没有设置登录密码，请先使用手机动态码登录后设置密码！";
			}
		}
		return null;
	}

	//获取绑定手机验证码
	@RequestMapping("/home/acct/bmckps.xhtml")
	public String loadBMCkPs(String captchaId, String captcha, @RequestParam("mobile")String mobile, String tag, ModelMap model, HttpServletRequest request){
		if(StringUtils.isBlank(mobile))return showJsonError(model, "手机号不能为空！");
		if(!ValidateUtil.isMobile(mobile)) return showJsonError(model, "手机号码格式不正确！");
		Member member = getLogonMember();
		String ip = WebUtils.getRemoteIp(request);
		Map jsonMap = new HashMap();
		Map errorMap = new HashMap();
		boolean iscaptcha = bindMobileService.isNeedToken(TokenType.BindMobile, ip, 2);
		jsonMap.put("errorMap", errorMap);
		if(iscaptcha){
			jsonMap.put("refreshCaptcha", "true");
			model.put("iscaptcha", iscaptcha);
			if(StringUtils.isBlank(captcha)){
				errorMap.put("captcha", "请输入验证码！");
				return showJsonError(model, jsonMap);
			}else{
				boolean isValidCaptcha = controllerService.validateCaptcha(captchaId, captcha,ip);
				errorMap.put("captcha", "验证码错误！");
				if(!isValidCaptcha) return showJsonError(model, jsonMap);
			}
		}
		boolean next = bindMobileService.getAndUpdateToken(TokenType.BindMobile, ip, 2);
		if(next){
			jsonMap.put("refreshCaptcha", "true");
		}
		
		if(StringUtils.equals(tag, "change") && member.isBindMobile()){
			if(StringUtils.equals(member.getMobile(), mobile)) 
				return showJsonError(model, "更改绑定的手机号码不能与原来的手机号相同！");
			boolean exists = memberService.isMemberMobileExists(mobile);
			if(exists) return showJsonError(model, "该号码已被其他帐号绑定，请更换号码！");
			if(!memberService.canChangeMobile(member)){
				return showJsonError(model, "手机绑定后7天内不能修改绑定！");			
			}
		}else{
			boolean exists = memberService.isMemberMobileExists(mobile);
			if(exists) return showJsonError(model, "该号码已被其他帐号绑定，请更换号码！");
		}
		
		String msgTemplate = "";
		if(StringUtils.equals(tag, "change")){
			msgTemplate = "更改手机绑定验证码：checkpass";
		}else{
			msgTemplate = "手机绑定验证码：checkpass";
		}
		ErrorCode<SMSRecord> code = bindMobileService.refreshBindMobile(BindConstant.TAG_BINDMOBILE, mobile, ip, msgTemplate);
		if(!code.isSuccess()) return showJsonError(model, code.getMsg());
		untransService.sendMsgAtServer(code.getRetval(), false);
		jsonMap.put("retval", "成功发送动态码！");
		return showJsonSuccess(model,jsonMap);
	}
	
	//获取抽奖手机验证码
	@RequestMapping("/ajax/mobile/dmckps.xhtml")
	public String loadDMCkPs(String validCode,String captchaId, String captcha, @RequestParam("mobile")String mobile,ModelMap model, HttpServletRequest request){
		if(StringUtils.isBlank(validCode)||!StringUtils.equals(validCode, StringUtil.md5(mobile + "drawmobileauthzt", "utf-8")))
			return showJsonError(model, "非指定客户！");
		if(StringUtils.isBlank(mobile))return showJsonError(model, "手机号不能为空！");
		if(!ValidateUtil.isMobile(mobile)) return showJsonError(model, "手机号码格式不正确！");
		String ip = WebUtils.getRemoteIp(request);
		Map jsonMap = new HashMap();
		Map errorMap = new HashMap();
		boolean iscaptcha = bindMobileService.isNeedToken(TokenType.DrawCheck, ip, 2);
		jsonMap.put("errorMap", errorMap);
		if(iscaptcha){
			jsonMap.put("refreshCaptcha", "true");
			model.put("iscaptcha", iscaptcha);
			if(StringUtils.isBlank(captcha)){
				errorMap.put("captcha", "请输入验证码！");
				return showJsonError(model, jsonMap);
			}else{
				boolean isValidCaptcha = controllerService.validateCaptcha(captchaId, captcha,ip);
				errorMap.put("captcha", "验证码错误！");
				if(!isValidCaptcha) return showJsonError(model, jsonMap);
			}
		}
		boolean next = bindMobileService.getAndUpdateToken(TokenType.DrawCheck, ip, 2);
		if(next){
			jsonMap.put("refreshCaptcha", "true");
		}

		ErrorCode<SMSRecord> code = bindMobileService.refreshBindMobile(BindConstant.TAG_DRAWMOBILE, mobile, ip);
		if(!code.isSuccess()) return showJsonError(model, code.getMsg());
		untransService.sendMsgAtServer(code.getRetval(), false);
		jsonMap.put("retval", "成功发送动态码！");
		return showJsonSuccess(model,jsonMap);
	}

	//用户职位
	@RequestMapping("/home/acct/position.xhtml")
	public String position(ModelMap model){
		Member member = getLogonMember();
		model.putAll(controllerService.getCommonData(model, member, member.getId()));
		MemberInfo memberInfo = daoService.getObject(MemberInfo.class, member.getId());
		if(memberInfo == null) return showError(model, "用户异常！");
		Jobs jobs = memberService.getMemberPosition(memberInfo.getExpvalue());
		Jobs nextJobs = memberService.getMemberNextPosition(memberInfo.getExpvalue());
		List<JobsUp> jobsUpList = memberService.getJobsUpByMemberId(member.getId());
		model.put("jobsUpList", jobsUpList);
		model.put("nextJobs", nextJobs);
		model.put("jobs", jobs);
		model.put("nextExp", nextJobs.getExpvalue()-memberInfo.getExpvalue());
		model.put("member", member);
		model.put("memberInfo", memberInfo);
		return "home/acct/position.vm";
	}

	//修改手机 
	@RequestMapping("/home/acct/upMobile.xhtml")
	public String upMobile(ModelMap model){
		Member member = getLogonMember();
		model.putAll(controllerService.getCommonData(model, member, member.getId()));
		model.put("member", member);
		return "home/acct/upMobile.vm";
	}
	
	//获取手机验证码
	@RequestMapping("/home/bindmobile/receive.xhtml")
	public String bindMobie(@RequestParam("mobile")String mobile, String tag, ModelMap model, HttpServletRequest request){
		if(StringUtils.isBlank(mobile))return showJsonError(model, "手机号不能为空！");
		if(!ValidateUtil.isMobile(mobile)) return showJsonError(model, "手机号码格式不正确！");
		Member member = getLogonMember();
		if(StringUtils.equals(tag, "change")&& member.isBindMobile()){
			if(StringUtils.equals(member.getMobile(), mobile)) 
				return showJsonError(model, "更改绑定的手机号码不能与原来的手机号相同！");
			boolean exists = memberService.isMemberMobileExists(mobile);
			if(exists) return showJsonError(model, "该号码已被其他帐号绑定，请更换号码！");
			if(!memberService.canChangeMobile(member)){
				return showJsonError(model, "手机绑定后7天内不能修改绑定！");			
			}
		}else{
			if(StringUtils.isBlank(member.getMobile())){
				boolean exists = memberService.isMemberMobileExists(mobile);
				if(exists) return showJsonError(model, "该号码已被其他帐号绑定，请更换号码！");
			}else{
				if(!StringUtils.equals(member.getMobile(), mobile))return showJsonError(model, "解除的用户手机不相匹配！");
				MemberInfo memberInfo = daoService.getObject(MemberInfo.class, member.getId());
				if(StringUtils.equals(memberInfo.getSource(), MemberConstant.REGISTER_MOBLIE)) return showJsonError(model, "手机注册用户不能解除绑定手机！");
				if(StringUtils.isBlank(member.getEmail())) return showJsonError(model, "请设置安全邮箱！");
				if(!memberService.canChangeMobile(member)){
					return showJsonError(model, "手机绑定后7天内不能解除绑定！");			
				}
			}
		}
		String msgTemplate = "";
		if(StringUtils.equals(tag, "change")){
			msgTemplate = "checkpass更改动态码";
		}else{
			if(StringUtils.isBlank(member.getMobile())) msgTemplate = "checkpass绑定动态码";
			else msgTemplate = "checkpass解除动态码";
		}
		msgTemplate += "，使用和30分钟过期无效；非本人或授权操作，为确保账户安全，请致电4000406506";
		ErrorCode<SMSRecord> code = bindMobileService.refreshBindMobile(BindConstant.TAG_BINDMOBILE, mobile, WebUtils.getRemoteIp(request), msgTemplate);
		if(!code.isSuccess()) return showJsonError(model, code.getMsg());

		untransService.sendMsgAtServer(code.getRetval(), false);
		return showJsonSuccess(model);
	}
	
	//绑定用户修改已绑定邮箱
	@RequestMapping("/home/acct/sendExchangeEmail.xhtml")
	public String sendExchangeEmail(HttpServletRequest request, String captchaId, String captcha, String email1, String email2, String password, ModelMap model){
		Member member = getLogonMember();
		String oldEmail = member.getEmail();
		boolean isValidCaptcha = controllerService.validateCaptcha(captchaId, captcha, WebUtils.getRemoteIp(request));
		if(!isValidCaptcha) return showJsonError(model, "验证码错误！");
		if(StringUtils.equals(oldEmail, email1)) return showJsonError(model, "更改的邮箱地址不能与当前邮箱地址一致！"); 
		if(StringUtils.isBlank(password)) return showJsonError(model, "请输入登录密码！");
		if(StringUtils.isBlank(email1) || StringUtils.isBlank(email2)) return showJsonError(model, "请输入更改邮箱地址！");
		if(!StringUtils.equals(email1, email2)) return showJsonError(model, "请确认更改邮箱一致!");
		/*MemberAccount memberAccount = daoService.getObjectByUkey(MemberAccount.class, "memberid", member.getId(), false);
		if(memberAccount == null || memberAccount.isNopassword()) return showJsonError(model, "先创建帐号或设置支付密码！");*/
		if(!StringUtils.equals(StringUtil.md5(password), member.getPassword())) return showJsonError(model, "登录密码不正确！");
		boolean match = ValidateUtil.isEmail(email1);
		if(!match) return showJsonError(model, "邮件格式不正确!");
		if(memberService.getMemberByEmail(email1)!=null)return showJsonError(model, "该邮箱已经注册！");
		gewaMailService.sendChangeEmail(member,email1);
		
		dbLogger.warnWithType(LogTypeConstant.LOG_TYPE_ACCOUNT, "[用户修改邮箱]用户:"+member.getId()+"原邮箱:"+oldEmail+"新邮箱:"+email1+"[IP:]"+WebUtils.getRemoteIp(request));
		monitorService.saveMemberLog(member.getId(), MemberConstant.ACTION_MODEMAIL, null, WebUtils.getRemoteIp(request));
		return showJsonSuccess(model, "邮件已发送，请接收邮件完成修改邮箱!");
	}
	//手机 绑定用户 设置绑定邮箱
	@RequestMapping("/home/acct/sendSecurityEmail.xhtml")
	public String sendSecurityEmail(HttpServletRequest request, String captchaId, String captcha, String email, String password, ModelMap model){
		Member member = getLogonMember();
		if(StringUtils.isBlank(password)) return showJsonError(model, "登录密码不能为空！");
		boolean isValidCaptcha = controllerService.validateCaptcha(captchaId, captcha, WebUtils.getRemoteIp(request));
		if(!isValidCaptcha) return showJsonError(model, "验证码错误！");
		boolean match = ValidateUtil.isEmail(email);
		if(!match) return showJsonError(model, "邮箱格式不正确,请重新输入!");
		if(memberService.getMemberByEmail(email) != null)return showJsonError(model, "邮箱地址已被使用，请换一个！");
	/*	MemberAccount memberAccount = daoService.getObjectByUkey(MemberAccount.class, "memberid", member.getId());
		if(memberAccount == null || memberAccount.isNopassword()) return showJsonError(model, "先创建帐号或设置支付密码！");*/
		if(!StringUtils.equals(member.getPassword(), StringUtil.md5(password))) return showJsonError(model, "登录密码错误！");
		gewaMailService.sendSecurityEmail(member,email);
		monitorService.saveMemberLog(member.getId(), MemberConstant.ACTION_MODEMAIL, null, WebUtils.getRemoteIp(request));
		return showJsonSuccess(model, "邮件已发送，请接收邮件完成安全邮箱设置!");
	}
	
	@RequestMapping("/home/acct/securityEmail.xhtml")
	public String securityEmail(@CookieValue(LOGIN_COOKIE_NAME)String sessid, 
			Long id, String email, String random, String encode, String op,HttpServletRequest request, ModelMap model){
		Member member = getLogonMember();
		String vkey = PayUtil.md5WithKey(random, email, ""+id);
		if(!StringUtils.equals(vkey, encode)) return show404(model, "连接错误！");
		if(!member.getId().equals(id)) return show404(model, "连接错误！");
		Long time = new Long(random.split("@")[1]);
		if((DateUtil.addDay(new Date(), -1)).getTime() > time) return show404(model, "连接错误！");
		MemberInfo memberInfo = daoService.getObject(MemberInfo.class, member.getId());
		if(!memberInfo.isAllNewTaskFinished() && !memberInfo.isFinishedTask(MemberConstant.TASK_CONFIRMREG)
				&&! memberInfo.isRegisterSource(MemberConstant.REGISTER_EMAIL)){
			boolean isExistsMember = memberService.isMemberExists(email, member.getId());
			if(isExistsMember) return show404(model, "该邮箱已存在！");
			String oldEmail = member.getEmail();
			member.setEmail(email.toLowerCase());
			daoService.saveObject(member);
			memberService.saveNewTask(member.getId(), MemberConstant.TASK_CONFIRMREG);
			loginService.updateMemberAuth(sessid, member);
			Map<String, String> logInfo = new HashMap<String, String>();
			if(StringUtils.isNotBlank(oldEmail)){
				logInfo.put("oldEmail", oldEmail);
				logInfo.put("newEmail", member.getEmail());
			}
			monitorService.saveMemberLogMap(member.getId(), MemberConstant.ACTION_MODEMAIL, logInfo, WebUtils.getRemoteIp(request));
			sendWarning("邮箱", member, oldEmail, member.getMobile());
		}else return show404(model, "连接错误！");
		if(StringUtils.equals(op, "mdyeml")){
			model.put("title", "修改邮箱验证");
			model.put("msg", "修改邮箱验证成功！");
		}
		if(StringUtils.equals(op, "bindeml")){
			model.put("title", "绑定邮箱验证");
			model.put("msg", "绑定邮箱验证成功！");
		}
		if (StringUtils.isNotBlank(op)) {
			return "home/acct/accountSafety/successPage.vm";			
		}
		return "redirect:/home/sns/personIndex.xhtml";
	}
	
	//绑定手机
	@RequestMapping("/home/acct/sendExchangeMobile.xhtml")
	public String sendExchangeMobile(@CookieValue(LOGIN_COOKIE_NAME)String sessid, 
			HttpServletRequest request, String mobile, String mobile2, 
			String tag, String password/*支付密码*/, ModelMap model){
		Member member = getLogonMember();
		boolean allow = operationService.updateOperation(BindConstant.TAG_BINDMOBILE + "_" + member.getId(), 30);
		if(!allow){
			dbLogger.warn(BindConstant.TAG_BINDMOBILE + "_" + member.getId() + ":" + WebUtils.getRemoteIp(request));
			return showJsonError(model, "操作过于频繁！");
		}
		boolean checked = StringUtils.isNotBlank(tag)?!CHECK_LIST.contains(tag):false;
		if(checked){
			dbLogger.warn("用户ID:" + member.getId() + ",绑定或修改手机号码");
			return showJsonError(model, "参数错误！");
		}
		if(StringUtils.isBlank(mobile2))return showJsonError(model, "手机动态码不能为空！");
		if(!ValidateUtil.isMobile(mobile)) return showJsonError(model, "手机号码格式不正确！");
		
		boolean exists = memberService.isMemberMobileExists(mobile);
		if(exists) return showJsonError(model, "该号码已绑定其他帐号，请更换其他手机号码！");
		MemberAccount memberAccount = daoService.getObjectByUkey(MemberAccount.class, "memberid", member.getId(), false);
		if(StringUtils.equals(tag, "change")){
			if(StringUtils.isBlank(password)) return showJsonError(model, "支付密码不能空！");
			if(memberAccount == null || memberAccount.isNopassword()) return showJsonError(model, "先创建帐号或设置支付密码！");
			if(!StringUtils.equals(memberAccount.getPassword(), PayUtil.getPass(password))) return showJsonError(model, "支付密码错误！");
		}else{
			/*if(memberAccount != null){
				if(StringUtils.isBlank(password)) return showJsonError(model, "支付密码不能空！");
				if(!StringUtils.equals(memberAccount.getPassword(), PayUtil.getPass(password))) return showJsonError(model, "支付密码错误！");
			}*/
		}
		String oldMobile = member.getMobile();
		ErrorCode bindResult = memberService.bindMobile(member, mobile, mobile2, WebUtils.getRemoteIp(request));
		if(!bindResult.isSuccess()) {
			return showJsonError(model, bindResult.getMsg());
		}
		
		daoService.saveObject(member);
		loginService.updateMemberAuth(sessid, member);
		
		sendWarning("手机号", member, oldMobile, member.getEmail());
		return showJsonSuccess(model);
	}
	
	//解除手机绑定
	@RequestMapping("/home/acct/relieveMobile.xhtml")
	public String unBindMobile(@CookieValue(LOGIN_COOKIE_NAME)String sessid, 
			HttpServletRequest request, String mobile, String mobile2, ModelMap model){
		Member member = getLogonMember();
		if(StringUtils.equals(mobile, member.getMobile())){
			return showJsonError(model, "手机号不匹配！");
		}
		ErrorCode result = memberService.unbindMobile(member, mobile2, WebUtils.getRemoteIp(request));
		if(result.isSuccess()){
			loginService.updateMemberAuth(sessid, member);
			return showJsonSuccess(model);
		}else{
			return showJsonError(model, result.getMsg());
		}
	}
	//修改邮箱信息
	@RequestMapping("/home/acct/exchangeEmail.xhtml")
	public String exchangeEmail(@CookieValue(LOGIN_COOKIE_NAME)String sessid, 
			Long id, String email, String random, String encode, HttpServletRequest request, ModelMap model){
		String vkey = PayUtil.md5WithKey(random, email, ""+id);
		if(!StringUtils.equals(vkey, encode)) return showError(model, "连接错误。");
		Long time = new Long(random.split("@")[1]);
		if((DateUtil.addDay(new Date(), -1)).getTime() > time) return showError(model, "连接错误。");
		if(memberService.getMemberByEmail(email)!=null)return showJsonError(model, "连接错误，该邮箱已经注册！");
		Member member = daoService.getObject(Member.class,id);
		String oldEmail=member.getEmail();
		member.setEmail(email.toLowerCase());
		daoService.saveObject(member);
		loginService.updateMemberAuth(sessid, member);
		model.put("msg", "修改邮箱成功，请重新登录！");
		if(StringUtils.equals(email.split("@")[1], "139.com")){
			Map paraMap = new HashMap();
			paraMap.put(MongoData.ACTION_MEMBERID, member.getId());
			Map map = mongoService.findOne(MongoData.NS_MEMBER_MOBILE, paraMap);
			if(map == null){
				map = new HashMap();
				map.put(MongoData.SYSTEM_ID, ObjectId.uuid());
				map.put(MongoData.ACTION_ADDTIME, DateUtil.getCurFullTimestampStr());
				map.put(MongoData.ACTION_MEMBERID, member.getId());
				map.put("oldEmail", oldEmail);
				map.put("newEmail", email);
				mongoService.addMap(map, MongoData.SYSTEM_ID, MongoData.NS_MEMBER_MOBILE);
			}else{
				map.put(MongoData.ACTION_ADDTIME, DateUtil.getCurFullTimestampStr());
				map.put("oldEmail", oldEmail);
				map.put("newEmail", email);
				mongoService.saveOrUpdateMap(map, MongoData.SYSTEM_ID, MongoData.NS_MEMBER_MOBILE);
			}
		}
		Map<String, String> logInfo = new HashMap<String, String>();
		if(StringUtils.isNotBlank(oldEmail)){
			logInfo.put("oldEmail", oldEmail);
			logInfo.put("newEmail", member.getEmail());
		}
		monitorService.saveMemberLogMap(member.getId(), MemberConstant.ACTION_MODEMAIL, logInfo, WebUtils.getRemoteIp(request));
		sendWarning("邮箱", member, oldEmail, member.getMobile());
//		return "redirect:/login.xhtml";
		return alertMessage(model,"修改邮箱成功，请重新登录！","login.xhtml");
	}
	//手机动态码验证修改密码
	@RequestMapping("/home/acct/sendExchangePassByCkPs.xhtml")
	public String sendExchangePassByCkPs(@CookieValue(LOGIN_COOKIE_NAME)String sessid,  
			String checkpass,String password, String password1, String password2, ModelMap model, HttpServletRequest request){
		Member member = getLogonMember();
		if(member == null) {
			return showJsonError(model, "请先登录！");
		}
		if(StringUtils.isBlank(checkpass)) {
			return showJsonError(model,"动态码不能为空！");
		}
		if(StringUtils.isBlank(password1)) {
			return showJsonError(model,"当前密码不能为空！");
		}
		if(StringUtils.isBlank(password1)) {
			return showJsonError(model,"修改密码不能为空！");
		}
		if(StringUtils.isBlank(password2)) {
			return showJsonError(model,"确认密码不能为空！");
		}
		if(!StringUtils.equals(password1, password2)) {
			return showJsonError(model,"确认密码与修改密码不一致！");
		}
		if(!ValidateUtil.isPassword(password1)){
			return showJsonError(model,"密码格式不正确,只能是字母，数字，下划线，长度6—14位！");
		}
		if(StringUtils.isNotBlank(member.getEmail())){
			boolean danger = baoKuService.isDanger(member.getEmail(), password1);
			if(danger) return showJsonError(model, "该帐户设置的密码存在安全风险，不能设置为该密码！");
		}
		ErrorCode code = bindMobileService.preCheckBindMobile(BindConstant.TAG_MODIFYPASS, member.getMobile(), checkpass);
		if(!code.isSuccess()){
			return showJsonError(model, code.getMsg());
		}
		String md5pass = StringUtil.md5(password);
		if(!StringUtils.equals(md5pass, member.getPassword())) return showJsonError(model, "原密码错误！");
		if(StringUtils.equals(password, password1)) {
			return showJsonError(model,"修改密码不能与当前密码一致！");
		}
		bindMobileService.checkBindMobile(BindConstant.TAG_MODIFYPASS, member.getMobile(), checkpass);
		MemberInfo memberInfo = daoService.getObject(MemberInfo.class, member.getId());
		Map<String, String> otherInfoMap = JsonUtils.readJsonToMap(memberInfo.getOtherinfo());
		otherInfoMap.remove(MemberConstant.TAG_DANGER);
		memberInfo.setOtherinfo(JsonUtils.writeMapToJson(otherInfoMap));
		member.setPassword(StringUtil.md5(password1));
		daoService.saveObjectList(member, memberInfo);
		monitorService.saveMemberLog(member.getId(), MemberConstant.ACTION_MODPWD, null, WebUtils.getRemoteIp(request));
		loginService.updateMemberAuth(sessid, member);
		return showJsonSuccess(model);
	}
	//邮件验证修改密码
	@RequestMapping("/home/acct/sendExchangePassByEml.xhtml")
	public String sendExchangePass(@CookieValue(LOGIN_COOKIE_NAME)String sessid, 
			String password, String password1, String password2, HttpServletRequest request, ModelMap model,String encode, String uuid){
		Member member = getLogonMember();
		if(member == null) return showJsonError(model, "请先登录！");
		String email = member.getEmail();
		String ip = WebUtils.getRemoteIp(request);
		ValidEmail validEmail = mongoService.getObject(ValidEmail.class, "id", uuid);
		if(validEmail == null){
			dbLogger.error("ip: " + ip + ", memberid: " + member.getId());
			return showJsonError(model, "非法操作！");
		}
		Long cur = System.currentTimeMillis();
		if(cur > validEmail.getValidtime()) return showJsonError(model, "该链接已失效，请重新获取修改密码链接！");
		if(!StringUtils.equals(StringUtil.md5(email + StringUtils.substring(member.getPassword(), 8, 24)), validEmail.getValidcode())){
			return showJsonError(model, "校验错误！");
		}
		String checkMsg = checkMemberResource(member);
		if(StringUtils.isNotBlank(checkMsg)) return showJsonError(model, checkMsg);
		if(!StringUtils.equals(PayUtil.md5WithKey(email, "" + member.getId(), uuid),encode)) return showJsonError(model, "此链接无效！");
		
		String md5pass = StringUtil.md5(password);
		if(password1==null||password2==null||!StringUtils.equals(password1, password2))return showJsonError(model, "数据错误！");
		if(!StringUtils.equals(md5pass,member.getPassword())) return showJsonError(model, "原密码错误！");
		boolean match = ValidateUtil.isVariable(password1, 6, 14);
		if(!match)return showJsonError(model, "密码格式不正确!");
		if(StringUtils.isNotBlank(email)){
			boolean danger = baoKuService.isDanger(email, password1);
			if(danger) return showJsonError(model, "该帐户设置的密码存在安全风险，不能设置为该密码！");
		}
		MemberInfo memberInfo = daoService.getObject(MemberInfo.class, member.getId());
		Map<String, String> otherInfoMap = JsonUtils.readJsonToMap(memberInfo.getOtherinfo());
		otherInfoMap.remove(MemberConstant.TAG_DANGER);
		memberInfo.setOtherinfo(JsonUtils.writeMapToJson(otherInfoMap));
		member.setPassword(StringUtil.md5(password1));
		daoService.saveObjectList(member, memberInfo);
		monitorService.saveMemberLog(member.getId(), MemberConstant.ACTION_MODPWD, null, ip);
		loginService.updateMemberAuth(sessid, member);
		return showJsonSuccess(model);
	}
	@RequestMapping("/home/acct/successPage.xhtml")
	public String successPage(ModelMap model,String title,String msg, HttpServletRequest request){
		Member member = getLogonMember();
		if (member == null) {
			return gotoLogin("/home/acct/successPage.xhtml", request, model);
		}
		MemberInfo memberInfo = daoService.getObject(MemberInfo.class, member.getId());
		MemberAccount memberAccount = daoService.getObjectByUkey(MemberAccount.class, "memberid", member.getId(), true);
		model.put("msg", msg);
		model.put("title", title);
		if(memberAccount != null && !memberAccount.isNopassword() && StringUtils.contains(memberInfo.getNewtask(), "bindmobile")){
			model.put("level", "h");
		}else if (StringUtils.contains(memberInfo.getNewtask(), "bindmobile")) {
			model.put("level", "m");
		}else {
			model.put("level", "l");
		}
		return "home/acct/accountSafety/successPage.vm";
	}
	/**
	 *  20110221 修改用户中心页面, 使用ajaxLoad, 加载所有资料页面
	 */
	@RequestMapping("/home/acct/memberinfo.xhtml")
	public String memberinfoall(ModelMap model, String tag){
		Member member = getLogonMember();
		model.putAll(controllerService.getCommonData(model, member, member.getId()));
		// 基本信息
		if("base".equals(tag)){
			addMemberBaseData(model);
			return "home/acct/memberinfo_base.vm";
		}
		// 联系方式
		else if(("contact").equals(tag)){
			return "home/acct/memberinfo_contact.vm";
		}
		// 兴趣爱好
		else if(("favor").equals(tag)){
			favorInfo(model, member);
			return "home/acct/memberinfo_favor.vm";
		}
		// 教育信息
		else if(("edu").equals(tag)){
			eduInfo(model, member);
			return "home/acct/memberinfo_edu.vm";
		}
		// 职业信息
		else if(("job").equals(tag)){
			jobInfo(model, member);
			return "home/acct/memberinfo_job.vm";
		}
		return "home/acct/memberinfoNew.vm";
	}
	
	
	
	/**
	 * 用户资料 - 基本信息
	 */
	private void addMemberBaseData(ModelMap model){
		//是否需要发送高级确认邮件
		MemberInfo memberInfo = (MemberInfo) model.get("memberInfo");
		if(memberInfo!=null && StringUtils.isNotBlank(memberInfo.getNewtask())){
			model.put("sendReg",StringUtils.contains(memberInfo.getNewtask(), "confirmreg"));
		}else {
			model.put("sendReg", false);
		}
		if(memberInfo != null){
			String[] dates = {"1985","01","01"};
			String birthday = memberInfo.getBirthday();
			if(StringUtils.isNotBlank(birthday)){
				if(StringUtils.contains(birthday, "-")){
					dates = StringUtils.split(birthday, "-");
				}
			}
			model.put("dates", dates);
			
			// 获取用户居住地
			String liveplace = placeService.getLocationPair(memberInfo.getId(), " - ");
			model.put("liveplace", liveplace);
		}
		
	}
	@RequestMapping("/home/acct/updateMemberinfo.xhtml")
	public String updateMemberInfo(@CookieValue(value=LOGIN_COOKIE_NAME,required=false)String sessid, String mobile, 
			String nickname, String year,String month,String day, String realname, String sign, String sex, String liveprovince,
			String livecity, String livecounty, String liveindexarea, ModelMap model){
		Member member = getLogonMember();
		if(StringUtils.isBlank(nickname)) return showJsonError(model, "用户昵称不能为空！");
		String key = blogService.filterAllKey(nickname);
		if(StringUtils.isNotBlank(key))return showJsonError(model, "含有非法关键字!");
		boolean matchNickname = ValidateUtil.isCNVariable(nickname, 2, 15);
		if(!matchNickname) return showJsonError(model,"用户昵称格式不正确，不能包含特殊符号！");
		boolean bNickname=memberService.isMemberExists(nickname, member.getId());
		if(bNickname) return showJsonError(model, "您输入的昵称已被占用！");
		if(StringUtils.isNotBlank(mobile)){
			boolean match = ValidateUtil.isMobile(mobile);
			if(!match) return showJsonError(model,"您输入的手机号格式不正确！");
			boolean b = memberService.isMemberMobileExists(mobile);
			if(b) return showJsonError(model, "您输入的手机号已经存在");
		}
		if(StringUtils.isBlank(realname)) return showJsonError(model, "真实姓名不能为空！");
		boolean matchRealname = ValidateUtil.isCNVariable(realname, 2, 15);
		if(!matchRealname) return showJsonError(model,"真实姓名格式不正确，不能包含特殊符号！");
		if(StringUtils.isBlank(sex)) return showJsonError(model, "性别不能为空！");
		MemberInfo memberInfo = daoService.getObject(MemberInfo.class, member.getId());
		ChangeEntry changeEntry = new ChangeEntry(memberInfo);
		//这里不用StringUtils.isNotBlank判断的原因是页面js加载省市，当不做修改时就会找不到这些元素;
		//可以为空白是可能只选了省后面就不选了
		if(liveprovince != null) memberInfo.setLiveprovince(liveprovince);
		if(livecity != null) memberInfo.setLivecity(livecity);
		if(livecounty != null) memberInfo.setLivecounty(livecounty);
		if(liveindexarea != null) memberInfo.setLiveindexarea(liveindexarea);
		memberInfo.setUpdatetime(new Timestamp(System.currentTimeMillis()));
		String birthDay = year+"-"+month+"-"+day;
		if(!DateUtil.isValidDate(birthDay)){
			return showJsonError(model, "生日格试有错误！");
		}
		memberInfo.setBirthday(birthDay);
		member.setNickname(XSSFilter.filterAttr(nickname));
		memberInfo.setNickname(nickname);
		memberInfo.setRealname(realname);
		memberInfo.setSign(sign);
		memberInfo.setSex(sex);
		memberInfo = XSSFilter.filterObjAttrs(memberInfo, "liveprovince","livecity","livecounty","liveindexarea","nickname","realname","sign","sex");
		if(WebUtils.checkPropertyAll(memberInfo)) {
			return showJsonError(model, "含有非法字符！");
		}
		//TODO:用户前台领取：修改后的用户资料,email,昵称、生日、手机、真实名称不能为空 新手任务
/*		if(StringUtils.isNotBlank(memberInfo.getBirthday()) && StringUtils.isNotBlank(memberInfo.getRealname())
				&& StringUtils.isNotBlank(member.getMobile())  && StringUtils.isNotBlank(member.getEmail()) 
				&& StringUtils.isNotBlank(member.getNickname()) ){
			memberService.saveNewTask(memberInfo, MemberConstant.TASK_MEMBERINFO);
		}
*/
		try{
			daoService.saveObjectList(member, memberInfo);
		}catch(Exception e){
			return showJsonError(model, "您填写的资料有误，保存资料失败！");
		}
		// 获取用户居住地
		String liveplace = placeService.getLocationPair(memberInfo.getId(), " - ");
		
		// 清除已有Member缓存
		cacheService.remove(CacheConstant.REGION_ONEHOUR, "MI" + memberInfo.getId());
		addCacheMember(model, memberInfo.getId());
		loginService.updateMemberAuth(sessid, member);
		// 添加修改log
		monitorService.saveChangeLog(member.getId(), MemberInfo.class, memberInfo.getId(), changeEntry.getChangeMap(memberInfo));
		return showJsonSuccess(model, liveplace);
	}
	@RequestMapping("/home/acct/updateContachInfo.xhtml")
	public String updateMemberContachInfo(ModelMap model,String address,String phone,String msn,String qq,String postcode){
		if(StringUtils.isNotBlank(postcode)){
			boolean matchPostCode = ValidateUtil.isNumber(postcode);
			if(!matchPostCode || (postcode.length() == 6? false: true)) return showJsonError(model, "您输入的邮编格式不对,只能是6位数字");
		}
		if(StringUtils.isNotBlank(phone)){
			boolean matchPhone = ValidateUtil.isPhone(phone);
			if(!matchPhone ){
				 matchPhone = ValidateUtil.isMobile(phone);
				 if(!matchPhone) return showJsonError(model, "您输入的电话格式不正确!");
			}
		}
		if(StringUtils.isNotBlank(qq)){
			boolean matchQq = ValidateUtil.isNumber(qq, 5, 11);
			if(!matchQq) return showJsonError(model, "您输入的QQ号格式不正确");
		}
		if(StringUtils.isNotBlank(msn)){
			boolean matchMsn = ValidateUtil.isEmail(msn);
			if(!matchMsn)return showJsonError(model, "MSN格式不正确!");
		}
		if(WebUtils.checkString(address)) return showJsonError(model, "地址含有非法字符！");
		Member member = getLogonMember();
		MemberInfo memberInfo = daoService.getObject(MemberInfo.class, member.getId());
		memberInfo.setAddress(XSSFilter.filterAttr(address));
		memberInfo.setPhone(phone);
		memberInfo.setMsn(msn);
		memberInfo.setQq(qq);
		memberInfo.setPostcode(postcode);
		daoService.saveObject(memberInfo);
		return showJsonSuccess(model);
	}
	/**
	 * 用户资料 - 兴趣爱好
	 */
	private void favorInfo(ModelMap model, Member member){
		// 兴趣爱好标签列表(取出随机N个)
		List<FavoriteTag> list = favoriteTagService.getRandomFavorList(12);
		model.put("list", list);
		
		// 我的兴趣爱好, 分割后存入List
		MemberInfo memberInfo = daoService.getObject(MemberInfo.class, member.getId());
		String favorTags = memberInfo.getFavortag() == null ? "" : memberInfo.getFavortag();
		if(StringUtils.isNotBlank(favorTags)){
			List<String> myfavTags = Arrays.asList(StringUtils.split(favorTags, "|"));
			model.put("myfavTags", myfavTags);
		}
	}
	// 随机列表
	@RequestMapping("/home/acct/randomintrest.xhtml")
	public String randomintrest(ModelMap model){
		Member member = getLogonMember();
		favorInfo(model, member);
		return "home/acct/memberinfo_favor.vm";
	}
	
	
	@RequestMapping("/home/acct/updateFavorInfo.xhtml")
	public String updateFavorInfo(ModelMap model, String tag){
		if(StringUtils.isBlank(tag)) return showJsonError_DATAERROR(model);
		if(WebUtils.checkString(tag)) return showJsonError(model, "标签含有非法字符！");
		tag = cleanProperty(tag);
		Member member = getLogonMember();
		// 用户自己录入
		MemberInfo memberInfo = daoService.getObject(MemberInfo.class, member.getId());
		// 过滤 '|', 'null' 等字符
		String favorTags = memberInfo.getFavortag();
		if(StringUtils.isBlank(favorTags)){
			favorTags = "";
		}else{
			if(StringUtils.equals(favorTags, "|")) favorTags = "";
			if(StringUtils.equals(favorTags, "null")) favorTags = "";
		}
		List<String> myfavTags = Arrays.asList(StringUtils.split(favorTags, "|"));
		if(!myfavTags.contains(tag)){
			favorTags += tag + "|";
			memberInfo.setFavortag(favorTags);
			daoService.saveObject(memberInfo);
		}
		// 点击公用录入: 首先判断数据库中是否有有tag, 有则查出来 count++, 没有则new一个放入
		FavoriteTag favoriteTag = daoService.getObject(FavoriteTag.class, tag);
		if(favoriteTag == null){
			favoriteTag = new FavoriteTag(tag);
			daoService.saveObject(favoriteTag);
		}else{
			if(!myfavTags.contains(tag)){
				favoriteTagService.updateFavoriteTagCount(tag);
			}
		}
		randomintrest(model);
		return "home/acct/memberinfo_favor.vm";
	}
	@RequestMapping("/home/acct/delFavorInfo.xhtml")
	public String delFavorInfo(ModelMap model, String tag){
		Member member = getLogonMember();
		MemberInfo memberInfo = daoService.getObject(MemberInfo.class, member.getId());
		String favorTags = memberInfo.getFavortag() == null ? "" : memberInfo.getFavortag();
		if(StringUtils.isNotBlank(favorTags)){
			List<String> myfavTags = new ArrayList<String>(Arrays.asList(StringUtils.split(favorTags, "|")));
			myfavTags.remove(tag);
			String end = StringUtils.join(myfavTags, "|");
			favorTags = "".equals(end) ? "" : end + "|";
			memberInfo.setFavortag(favorTags);
			daoService.saveObject(memberInfo);
		}
		return showJsonSuccess(model);
	}
	/**
	 * 用户资料 - 教育信息
	 */
	private void eduInfo(ModelMap model, Member member){
		// 查询列表
		List<MemberInfoMore> memberinfomoreList = memberService.getMemberinfoMoreList(member.getId(), TagConstant.TAG_EDU);
		model.put("memberinfomoreList", memberinfomoreList);
		model.put("provinceList",placeService.getAllProvinces());
	}
	private <T extends BaseObject> T cleanPropertyAll(T entity) {
		XSSFilter filter = new XSSFilter();
		try {
			Map result = PropertyUtils.describe(entity);
			for (Object key : result.keySet()) {
				if (result.get(key) instanceof String) {
					String cleanString = filter.filter(String.valueOf(result.get(key)));
					result.put(key, cleanString);
				}
			}
			BeanUtil.copyProperties(entity, result);
		} catch (Exception ex) {
		}
		return entity;
	}
	private String cleanProperty(String input){
		return  new XSSFilter().filter(input);
	}
	@RequestMapping("/home/acct/updateMemberinfomore.xhtml")
	public String saveMemberinfoMore(ModelMap model, HttpServletRequest request){
		Map<String, String[]> dataMap = request.getParameterMap();
		MemberInfoMore memberInfoMore = new MemberInfoMore();
		BindUtils.bindData(memberInfoMore, dataMap);
		if(WebUtils.checkPropertyAll(memberInfoMore)) return showJsonError(model, "含有非法字符！");
		memberInfoMore = cleanPropertyAll(memberInfoMore);
		Member member = getLogonMember();
		memberInfoMore.setMemberid(member.getId());
		if(StringUtils.isNotBlank(memberInfoMore.getJobcompanyemail())){
			memberInfoMore.setJobcompanyemail(StringUtils.split(memberInfoMore.getJobcompanyemail(), "@")[1]);
		}
		memberInfoMore.setJobprovince(placeService.getPlaceNameBycode("province", memberInfoMore.getJobprovince()));
		memberInfoMore.setJobcity(placeService.getPlaceNameBycode("city", memberInfoMore.getJobcity()));
		daoService.saveObject(memberInfoMore);
		if(TagConstant.TAG_EDU.equals(memberInfoMore.getTag())){
			eduInfo(model, member);
			return "home/acct/memberinfo_edu.vm";
		}
		jobInfo(model, member);
		return "home/acct/memberinfo_job.vm";
	}
	@RequestMapping("/home/acct/delMemberinfomore.xhtml")
	public String delMemberinfoMore(ModelMap model, Long id){
		Member member = getLogonMember();
		MemberInfoMore memberInfoMore = daoService.getObject(MemberInfoMore.class, id);
		if(!VmUtils.eq(memberInfoMore.getMemberid(), member.getId())){
			return showJsonError(model, "非法操作！");
		}
		if(memberInfoMore != null){
			daoService.removeObject(memberInfoMore);
		}
		return showJsonSuccess(model);
	}
	
	/**
	 * 用户资料 - 职业信息
	 */
	private void jobInfo(ModelMap model, Member member){
		// 查询列表
		List<MemberInfoMore> memberinfomoreList = memberService.getMemberinfoMoreList(member.getId(), TagConstant.TAG_JOB);
		model.put("memberinfomoreList", memberinfomoreList);
		model.put("provinceList",placeService.getAllProvinces());
	}
	
	/**
	 * 个人资料中的隐私设置
	 */
	@RequestMapping("/home/acct/updateHideSet.xhtml")
	public String updateMemberInfoHideSet(HttpServletRequest request,ModelMap model){
		Member member=getLogonMember();
		MemberInfo memberinfo=daoService.getObject(MemberInfo.class, member.getId());
		Map<String,String[]> map=request.getParameterMap();
		String rights=null;
		if(map != null){
			if(map.size()==1){
				rights=map.get("indexrights")[0]+",album_private,friend_private,commu_private,topic_private,activity_private,qa_private,agenda_private";
			}else{
				rights=map.get("indexrights")[0]+","+map.get("albumrights")[0]+","+map.get("friendrights")[0]+
				","+map.get("commurights")[0]+","+map.get("topicrights")[0]+","+map.get("activityrights")[0]+","+map.get("qarights")[0]+","+map.get("agendarights")[0];
			}
		}
		memberinfo.setRights(rights);
		try{
			daoService.updateObject(memberinfo);
		}catch(Exception e){
			return showJsonError(model,"数据更新失败！");
		}
		return showJsonSuccess(model);
	}
	
	/**
	 * 查询个人隐私设置资料
	 */
	@RequestMapping("/home/acct/searchHideSet.xhtml")
	public String searchMemberInfoHideSet(ModelMap model){
		Member member=getLogonMember();
		MemberInfo memberinfo=daoService.getObject(MemberInfo.class, member.getId());
		List rigths = Arrays.asList(StringUtils.split(memberinfo.getRights(), ","));
		model.putAll(controllerService.getCommonData(model, member, member.getId()));
		model.put("rights", rigths);
		return "home/acct/updateHideSet.vm";
	}
	
	/**
	 * 修改邮箱地址
	 */
	@RequestMapping("/home/acct/updateMemberEmail.xhtml")
	public String updateMemberEmail(@CookieValue(LOGIN_COOKIE_NAME)String sessid, String email, HttpServletRequest request, ModelMap model){
		boolean match = ValidateUtil.isEmail(email);
		Member member = getLogonMember();
		if(!match) return showJsonError(model, "Email格式不合法");
		boolean exists = memberService.isMemberExists(email, member.getId());
		if(exists) return showJsonError(model, "failure");
		String oldEmail = member.getEmail();
		member.setEmail(email);
		daoService.saveObject(member);
		loginService.updateMemberAuth(sessid, member);
		Map<String, String> logInfo = new HashMap<String, String>();
		if(StringUtils.isNotBlank(oldEmail)){
			logInfo.put("oldEmail", oldEmail);
			logInfo.put("newEmail", member.getEmail());
		}
		monitorService.saveMemberLogMap(member.getId(), MemberConstant.ACTION_MODEMAIL, logInfo, WebUtils.getRemoteIp(request));
		sendWarning("邮箱", member, oldEmail, member.getMobile());
		return showJsonError(model, "updatefailure");
	}
	/**
	 * 上传头像图片
	 */
	@RequestMapping("/home/acct/uploadHeadLogo.xhtml")
	public String uploadHeadLogo(String paramchk, String successFile, ModelMap model) throws Exception{
		String mycheck = StringUtil.md5(successFile + config.getString("uploadKey"));
		if(!mycheck.equals(paramchk)) return forwardMessage(model, "校验错误");
		Map jsonMap = new HashMap();
		jsonMap.put("filename", successFile);
		jsonMap.put("success", true);
		model.put("jsonMap", jsonMap);
		return "common/showUploadResult.vm";
	}
	/**
	 * 头像剪切
	 */
	@RequestMapping("/home/acct/updateHead.xhtml")
	public String updateHead(double imgW, double imgH, double imgleft, double imgtop, String filename, ModelMap model,
			HttpServletRequest request) throws Exception{
		Member member=getLogonMember();
		String headPath = PictureUtil.getHeadPicpath();
		String picname = "/images/temp/"  + filename;
		String fromPath = gewaPicService.getTempFilePath(filename);
		gewaPicService.saveToLocal(new File(fromPath), picname);
		String tmpPath = gewaPicService.getTempFilePath("wh_"+filename);
		PictureUtil.resize(fromPath, tmpPath, (int)imgW, (int)imgH); //改变大小
		PictureUtil.crop(tmpPath, fromPath, 90, 90, (int)imgleft, (int)imgtop); //剪切
		gewaPicService.addToRemoteFile(new File(fromPath), member.getId(), "member", member.getId(), headPath + filename);
		MemberInfo mi = daoService.getObject(MemberInfo.class, member.getId());
		mi.setHeadpic(headPath + filename);
		mi.setUpdatetime(new Timestamp(System.currentTimeMillis()));
		
		//TODO:用户前台认领
		/*
		if(!mi.isFinishedTask(MemberConstant.TASK_UPDATE_HEAD_PIC)){
			memberService.saveNewTask(mi, MemberConstant.TASK_UPDATE_HEAD_PIC);
		}*/
		daoService.saveObject(mi);
		cacheService.remove(CacheConstant.REGION_ONEHOUR, "MI" + mi.getId());
		addCacheMember(model, mi.getId());
		dbLogger.warn("用户更换头像："+member.getNickname()+"("+member.getId()+")" + " ip:"+ WebUtils.getIpAndPort(WebUtils.getRemoteIp(request), request));
		File f = new File(fromPath);
		if(f.exists()) f.delete();
		f = new File(tmpPath);
		if(f.exists()){
			f.delete();
		}
		return showRedirect("/home/acct/chanageHead.xhtml", model);
	}
	
	@RequestMapping("/home/acct/chanageHead.xhtml")
	public String changeHead(ModelMap model) {
		Member member = getLogonMember();
		model.putAll(controllerService.getCommonData(model, member, member.getId()));
		return "home/acct/changeHead.vm";
	}
	/*//发送修改支付密码邮件
	@RequestMapping("/home/acct/changePayPass.xhtml")
	public String changePayPass(ModelMap model, String tag){
		Member member = getLogonMember();
		String time = "" + (System.currentTimeMillis() + DateUtil.m_hour * 4);
		if(StringUtils.isBlank(member.getEmail())) return showJsonError(model, "请设置安全邮箱！");
		MemberInfo memberInfo = daoService.getObject(MemberInfo.class, member.getId());
		if(!StringUtils.equals(memberInfo.getSource(),MemberConstant.REGISTER_EMAIL) && !memberInfo.isBindSuccess())
			return showJsonError(model, "请设置安全邮箱！");
		String encode = PayUtil.md5WithKey(time+PayUtil.md5WithKey(member.getEmail())+member.getId());
		String queryStr = "validtime=" + time + "&encode="+encode + "&tag="+tag;
		model.put("nickname", member.getNickname());
		model.put("queryStr", queryStr);
		model.put("tag", tag);
		String title = "你的找回支付密码申请";
		if(StringUtils.equals(tag, "idcard")){
			title = "你的修改身份验证申请";
		}
		mailService.sendTemplateEmail(EmailRecord.SENDER_GEWARA, title, "mail/changePayPass.vm", model, member.getEmail());
		return showJsonSuccess(model);
	}*/
	//修改支付密码
/*	@RequestMapping(value="/home/acct/upPayPass.xhtml", method=RequestMethod.GET)
	public String upPayPass(ModelMap model, String validtime, String encode, String tag){
		Member member = getLogonMember();
		Long s = 0L;
		try {
			s = Long.valueOf(validtime);
		} catch (Exception e) {
			return showError(model, "参数错误，请确认你的链接来源！");
		}
		if(System.currentTimeMillis() > s) return "home/acct/forgetPasswordstep2.vm";
		if(StringUtils.isBlank(member.getEmail())) return show404(model, "请设置安全邮箱！");
		String encode2 = PayUtil.md5WithKey(validtime+PayUtil.md5WithKey(member.getEmail())+member.getId());
		if(!encode.equals(encode2)) return "home/acct/forgetPasswordstep2.vm";
		model.putAll(controllerService.getCommonData(model, member, member.getId()));
		MemberAccount memberAccount = daoService.getObjectByUkey(MemberAccount.class, "memberid", member.getId(), false);
		//解密账号身份证号
		if(memberAccount!=null&&StringUtils.isNotBlank(memberAccount.getEncryidcard())){
			String encryidcard = paymentService.getDecryptIdcard(memberAccount.getEncryidcard());
			memberAccount.setEncryidcard(encryidcard);
		}
		model.put("memberAccount", memberAccount);
		model.put("encode", encode);
		model.put("validtime", validtime);
		model.put("tag", tag);
		return "home/acct/upPayPass.vm";
	}*/
/*	@RequestMapping(value="/home/acct/upPayPass.xhtml", method=RequestMethod.POST)
	public String upPayPass(ModelMap model, String validtime, String encode, String password, String password2, String realname, String idcard, HttpServletRequest request){
		Member member = getLogonMember();
		if(System.currentTimeMillis() > Long.valueOf(validtime)) return showJsonError(model, "此链接已失效！");
		if(StringUtils.isBlank(member.getEmail())) return showJsonError(model, "请设置安全邮箱！");
		String encode2 = PayUtil.md5WithKey(validtime+PayUtil.md5WithKey(member.getEmail())+member.getId());
		if(!encode.equals(encode2)) return showJsonError(model, "此链接已失效！");
		if(StringUtils.isBlank(password)) {
			return showJsonError(model, "支付密码不能为空！");
		}
		if(StringUtils.isBlank(password2)) {
			return showJsonError(model, "确认支付密码不能为空！");
		}
		if(!StringUtils.equals(password, password2)) {
			return showJsonError(model, "支付密码与确认支付密码不一致！");
		}
		if(StringUtils.equals(password, "123456")){
			return showJsonError(model, "支付密码过于简单！");
		}
		if(!ValidateUtil.isPassword(password)){
			return showJsonError(model, "支付密码格式不正确,只能是字母，数字，下划线，长度6—14位！");
		}
		if(StringUtils.isNotBlank(idcard)&&(idcard.length()<6||idcard.length()>30)){
			return showJsonError(model, "证件号码格式不正确,只能是字母，数字，下划线，长度6—30位！");
		}
		MemberAccount memberAccount = daoService.getObjectByUkey(MemberAccount.class, "memberid", member.getId(), false);
		if(memberAccount == null) return showJsonError(model, "请先创建支付帐号！");
		if(StringUtils.equals(StringUtil.md5(password), member.getPassword())){
			return showJsonError(model, "支付密码不能跟登录密码相同！");
		}
		memberAccount.setPassword(PayUtil.getPass(password));
		if(StringUtils.isNotBlank(realname) && StringUtils.isNotBlank(idcard)){
			memberAccount.setRealname(realname);
			String encryCard = paymentService.setIncodeEncryIdcard(StringUtil.ToDBC(idcard));
			memberAccount.setEncryidcard(encryCard);
			memberAccount.setUpdatetime(DateUtil.getCurTruncTimestamp());
		}
		daoService.saveObject(memberAccount);
		monitorService.saveMemberLog(member.getId(), MemberConstant.ACTION_MODPAYPWD, null, WebUtils.getRemoteIp(request));
		return showJsonSuccess(model);
	}*/
	//新手购票成功领取5元优惠券  
	@RequestMapping("/home/acct/receivePoint.xhtml")
	public String receivePoint(ModelMap model){
		Member member = getLogonMember();
		Long memberid = member.getId();
		MemberInfo info = daoService.getObject(MemberInfo.class, memberid);
		boolean allow = false;
		try{
			allow = operationService.updateOperation("finish" + memberid, 15);
			if(allow){
				ErrorCode code = drawActivityService.sendNewTaskCardPrize(info, member);
				if(code.isSuccess()){
					return showJsonSuccess(model, "优惠券领取成功,有效期为90天！");
				}
				return showJsonError(model, code.getMsg());
			}else{
				return showJsonError(model, "操作过于频繁!");
			}
		}catch(Exception e){
			return showJsonError(model, "出现错误!");
		}
	}
	
	/**
	 *  partner领取5元优惠券  
	 *  2011-02-14
	 *  Administrator
	 */
	private static final String TAG_PARTNER = "partner5Coupon";//抽奖活动标识
	@RequestMapping("/home/receive5Coupon.xhtml")
	public String receive5Coupon(ModelMap model, HttpServletRequest request, HttpServletResponse response){
		Member member = getLogonMember();
		if(member==null)  return showError(model, "您还没登录，请返回登录！");
		model.put("member", member);
		
		String[] cookies = WebUtils.getCookie4ProtectedPage(request, "partnerReg");
		if(cookies != null){
			// 判断是否已经领取过
			String opkey = TAG_PARTNER + cookies[1] + member.getId();
			if(!operationService.isAllowOperation(opkey, OperationService.ONE_DAY)){
				return showJsonError(model, "您已经领取过了！");
			}
			// 绑定优惠券
			GewaOrder order = daoService.getObject(GewaOrder.class, new Long(cookies[1]));
			if(order != null){
				bindCoupon(member, order.getMobile());
				// 设置UserOperation, 保存用户操作记录
				operationService.updateOperation(opkey, OperationService.ONE_DAY);
				// 清空cookie
				String path1 = config.getBasePath() + "home/member/register2.xhtml";
				String path2 = config.getBasePath() + "home/receive5Coupon.xhtml";
				WebUtils.clearCookie(response, path1, "partnerReg");
				WebUtils.clearCookie(response, path2, "partnerReg");
			}
		}
		return "redirect:/home/acct/cardList.xhtml";
	}
	private void bindCoupon(Member member, String mobile){
		// 绑定优惠券
		DrawActivity da = daoService.getObjectByUkey(DrawActivity.class, "tag", TAG_PARTNER, true);
		VersionCtl mvc = daoService.getObject(VersionCtl.class, TAG_PARTNER + member.getId());
		if(mvc == null) {
			mvc = new VersionCtl(TAG_PARTNER + member.getId());
			daoService.saveObject(mvc);
		}
		Prize prize = drawActivityService.runDrawPrize(da, mvc, false);//抽出奖品
		WinnerInfo winner = drawActivityService.addWinner(prize, member.getId(), member.getNickname(), member.getMobile(), null);//给奖品
		winner.setMobile(mobile);
		drawActivityService.sendPrize(prize, winner, false);
	}
	
	@RequestMapping("/home/acct/ext.xhtml")
	public String ext(ModelMap model){
		Member member=getLogonMember();
		model.putAll(controllerService.getCommonData(model, member, member.getId()));
		model.put("curtime", new Timestamp(System.currentTimeMillis()));
		return "home/acct/ext.vm";
	}
	@RequestMapping("/home/acct/cardList.xhtml")
	public String cardList(ModelMap model, HttpServletRequest request){
		Member member=getLogonMember();
		model.putAll(controllerService.getCommonData(model, member, member.getId()));
		
		String key = "USER_MSGCOUNT_" + member.getId();
		cacheService.remove(CacheConstant.REGION_TWOHOUR, key);
		memberService.getMemberNotReadMessageCount(member.getId());
		MemberAccount memberAccount = daoService.getObjectByUkey(MemberAccount.class, "memberid", member.getId(), false);
		if(memberAccount == null || memberAccount.isNopassword()){
			model.put("needPayPass", "true");
		}
		String rel_tag = request.getParameter("rel_tag");
		if (StringUtils.isBlank(rel_tag)) {
			rel_tag = "movie";
		}
		model.put("rel_tag", rel_tag);
		return "home/acct/cardList.vm";
	}
	@RequestMapping("/home/acct/ajaxCardListTable.xhtml")
	public String ajaxCardListTable(String tag, ModelMap model, Integer pageNo){
		if(pageNo == null) pageNo = 0;
		int rowsPerPage = 20;
		int firstRow = pageNo*rowsPerPage;
		Integer count = 0;
		Member member=getLogonMember();
		if(StringUtils.equals(tag, "preferential")){
			count = ticketDiscountService.getSpCodeCountByMemberid(member.getId());
			if(count > 0){
				List<SpCode> spCodeList = ticketDiscountService.getSpCodeList(member.getId(), null, firstRow, rowsPerPage);
				List<Long> sdidList = BeanUtil.getBeanPropertyList(spCodeList, Long.class, "sdid", true);
				Map spdiscountMap = daoService.getObjectMap(SpecialDiscount.class, sdidList);
				model.put("spCodeList", spCodeList);
				model.put("spdiscountMap", spdiscountMap);
			}
		}else{
			count = elecCardService.getCardCountByMemberid(member.getId(), tag);
			if(count > 0){
				List<ElecCard> cardList = elecCardService.getCardListByMemberid(member.getId(), tag, firstRow, rowsPerPage);
				model.put("cardList", cardList);
			}
		}
		PageUtil pageUtil=new PageUtil(count,rowsPerPage,pageNo,"/home/acct/ajaxCardListTable.xhtml", true, true);
		Map params = new HashMap();
		params.put("tag", tag);
		pageUtil.initPageInfo(params);
		model.put("pageUtil", pageUtil);
		model.put("cardtag", tag);
		JsonData jd = daoService.getObject(JsonData.class, JsonDataKey.KEY_ELECCARD_DELAY);
		if(jd != null){
			model.put("delayElecCardNo",VmUtils.readJsonToMap(jd.getData()));
		}
		return "home/acct/cardListTable.vm";
	}
	//通过票券密码查找券
	@RequestMapping("/home/acct/ajaxCardTableByCardPass.xhtml")
	public String ajaxCardTableByCardPass(String cardpass, String captchaId, String captcha, HttpServletRequest request, ModelMap model){
		if(StringUtils.isBlank(captcha)) return showJsonError(model, "请输入验证码！");
		boolean isValidCaptcha = controllerService.validateCaptcha(captchaId, captcha, WebUtils.getRemoteIp(request));
		if(!isValidCaptcha) return showJsonError(model, "验证码错误！");
		ElecCard card = elecCardService.getElecCardByPass(StringUtils.trim(cardpass));
		if(card == null) return showJsonError(model, "票券查询错误！");
		if(card.needActivation()){
			Map jsonMap = new HashMap();
			jsonMap.put("activation", "true");
			jsonMap.put("msg", card.getCardno());
			return showJsonError(model, jsonMap);
		}
		boolean isMovieCard = false, isDramaCard = false, isSportCard = false;
		ElecCardBatch elecCardBatch = card.getEbatch();
		List<Long> cinemaidList = BeanUtil.getIdList(elecCardBatch.getValidcinema(), ",");
		List<Long> movieidList = BeanUtil.getIdList(elecCardBatch.getValidmovie(), ",");
		if(StringUtils.equals(card.getEbatch().getTag(), TagConstant.TAG_MOVIE)) {
			List<Cinema> cinemaList = daoService.getObjectList(Cinema.class, cinemaidList);
			List<Movie> movieList = daoService.getObjectList(Movie.class, movieidList);
			isMovieCard = true;
			model.put("cinemaList", cinemaList);
			model.put("movieList", movieList);
		}else if(StringUtils.equals(card.getEbatch().getTag(), TagConstant.TAG_DRAMA)){
			List<Theatre> theatreList = daoService.getObjectList(Theatre.class, cinemaidList);
			List<Drama> dramaList = daoService.getObjectList(Drama.class, movieidList);
			isDramaCard = true;
			model.put("cinemaList", theatreList);
			model.put("movieList", dramaList);
		}else if(StringUtils.equals(card.getEbatch().getTag(), TagConstant.TAG_SPORT)){
			List<Sport> sportList = daoService.getObjectList(Sport.class, cinemaidList);
			List<SportItem> itemList = daoService.getObjectList(SportItem.class, movieidList);
			isSportCard = true;
			model.put("cinemaList", sportList);
			model.put("movieList", itemList);
		}
		if(StringUtils.isNotBlank(elecCardBatch.getWeektype())){
			model.put("strweek", "周" + elecCardBatch.getWeektype());
		}else{
			model.put("strweek", "不限");
		}
		model.put("isMovieCard", isMovieCard);
		model.put("isDramaCard", isDramaCard);
		model.put("isSportCard", isSportCard);
		model.put("cityMap", AdminCityContant.getCitycode2CitynameMap());
		model.put("card", card);
		return "home/acct/card.vm";
	}
	
	//票券激活页
	@RequestMapping("/home/acct/activationCard.xhtml")
	public String activationCard(String cardpass, ModelMap model){
		Member member = getLogonMember();
		model.put("cardpass", cardpass);
		model.put("logonMember", member);
		return "home/acct/cardActivation.vm";
	}
	//激活票券
	@RequestMapping("/home/acct/ajaxActivationCard.xhtml")
	public String activationCard(String checkpass, String cardPass, ModelMap model){
		Member member=getLogonMember();
		if(StringUtils.isBlank(checkpass)) {
			return showJsonError(model,"动态码不能为空！");
		}
		ErrorCode code = elecCardService.checkActivationCard(member, cardPass, checkpass);
		if(!code.isSuccess()) return showJsonError(model,code.getMsg());
		return showJsonSuccess(model, code.getMsg());
	}
	@RequestMapping("/gewapay/showOrder.xhtml")
	public String showOrder(long orderId, String msg, ModelMap model) {
		Member member = getLogonMember();
		GewaOrder order = daoService.getObject(GewaOrder.class, orderId);
		if(order == null) return show404(model, "该订单不存在！");
		if (!order.getMemberid().equals(member.getId())) return show404(model, "不能查看他人的订单！");
		model.put("order", order);
		initRelatedOrder(order, model);
		model.putAll(controllerService.getCommonData(model, member, member.getId()));
		model.put("msg", msg);
		model.put("itemList", daoService.getObjectListByField(BuyItem.class, "orderid", order.getId()));
		OrderExtra orderExtra = daoService.getObject(OrderExtra.class, order.getId());
		model.put("orderExtra", orderExtra);
		return "home/acct/orderDetail.vm";
	}
	
	private void initRelatedOrder(GewaOrder gewaOrder, ModelMap model){
		Map<Long, Object> cMap = new HashMap<Long, Object>();
		if(gewaOrder instanceof TicketOrder) {
			TicketOrder order = (TicketOrder)gewaOrder;
			Movie object = daoService.getObject(Movie.class, order.getMovieid());
			MoviePlayItem moviePlayItem = daoService.getObject(MoviePlayItem.class, order.getMpid());
			if(moviePlayItem != null){
				object.setRemark(moviePlayItem.getLanguage() + " / " + moviePlayItem.getEdition());
			}
			cMap.put(gewaOrder.getId(), object);
		}else if(gewaOrder instanceof DramaOrder){
			DramaOrder order = (DramaOrder)gewaOrder;
			Drama object = daoService.getObject(Drama.class, order.getDramaid());
			cMap.put(gewaOrder.getId(), object);
		}else if(gewaOrder instanceof SportOrder){
			SportOrder order = (SportOrder)gewaOrder;
			Sport object = daoService.getObject(Sport.class, order.getSportid());
			cMap.put(gewaOrder.getId(), object);
		}else if(gewaOrder instanceof GoodsOrder){
			GoodsOrder order = (GoodsOrder)gewaOrder;
			BaseGoods object = daoService.getObject(BaseGoods.class, order.getGoodsid());
			cMap.put(gewaOrder.getId(), object);
		}else if(gewaOrder instanceof PubSaleOrder){
			PubSaleOrder order = (PubSaleOrder)gewaOrder;
			PubSale object = daoService.getObject(PubSale.class, order.getPubid());
			cMap.put(gewaOrder.getId(), object);
		}else if(gewaOrder instanceof GymOrder){
			GymOrder order = (GymOrder)gewaOrder;
			ErrorCode<CardItem> code = synchGymService.getGymCardItem(order.getGci(), true);
			if(code.isSuccess()){
				cMap.put(gewaOrder.getId(), code.getRetval());
			}
		}else if(gewaOrder instanceof MemberCardOrder){
			MemberCardOrder order = (MemberCardOrder)gewaOrder;
			Sport object = daoService.getObject(Sport.class, order.getPlaceid());
			cMap.put(gewaOrder.getId(), object);
		}
		model.put("cMap", cMap);
	}
	
	// Ajax 改写上面 showOrder() 方法
	@RequestMapping("/gewapay/ajaxShowOrder.xhtml")
	public String ajaxShowOrder(long orderId, ModelMap model) {
		Member member = getLogonMember();
		GewaOrder order = daoService.getObject(GewaOrder.class, orderId);
		if (!order.getMemberid().equals(member.getId()))
			return show404(model, "不能查看他人的订单！");
		model.put("order", order);
		return "home/acct/ajaxorderDetail.vm";
	}
	
	//Ajax 获取我感兴趣的电影院
	@RequestMapping("/ajax/acct/getInterestCinemaList.xhtml")
	public String getInterestCinemaList(@CookieValue(value=LOGIN_COOKIE_NAME, required=false)String sessid, ModelMap model, HttpServletRequest request, HttpServletResponse response) throws Exception{
		Member member = loginService.getLogonMemberBySessid(WebUtils.getRemoteIp(request), sessid);
		Map jsonMap = new HashMap();
		if(member==null) return showJsonError(model, "请登录");
		Long memberid = member.getId();
		String citycode = WebUtils.getAndSetDefault(request, response);
		if(StringUtils.isBlank(citycode)) citycode = "310000";
		List<Cinema> cinemaList = orderQueryService.getMemberOrderCinemaList(memberid, 4);
		if(cinemaList.size()<4){
			List<Long> cinemaIdList = blogService.getTreasureRelatedidList(citycode, memberid, "cinema", Treasure.ACTION_XIANGQU);
			List<Cinema> treasureCinemaList = daoService.getObjectList(Cinema.class, cinemaIdList);
			treasureCinemaList.removeAll(cinemaList);
			cinemaList.addAll(treasureCinemaList);
			if(cinemaList.size()>4) cinemaList.subList(0, 4);
		}
		List<Map> cinemaMapList = BeanUtil.getBeanMapList(cinemaList, "id", "name", "booking", "realBriefname");
		if(cinemaMapList.isEmpty())return showJsonError(model, "没有感兴趣影院");
		jsonMap.put("interest", cinemaMapList);
		return showJsonSuccess(model, jsonMap);
	}
	
	@RequestMapping("/home/ajax/getBindMobile.xhtml")
	public String getBindMobile(ModelMap model, String tag){
		Member member = getLogonMember();
		MemberInfo memberInfo = daoService.getObject(MemberInfo.class, member.getId());
		model.put("member", member);
		model.put("memberInfo", memberInfo);
		String msg = null;
		MemberAccount memberAccount = daoService.getObjectByUkey(MemberAccount.class, "memberid", member.getId(), false);
		model.put("memberAccount", memberAccount);
		if(StringUtils.equals(tag, "remove")&&member.isBindMobile()){
			if(StringUtils.equals(memberInfo.getSource(), MemberConstant.REGISTER_MOBLIE)) return showJsonError(model, "手机注册用户不能解除绑定手机！");
			if(StringUtils.isBlank(member.getEmail())||!memberInfo.isBindSuccess()) return showJsonError(model, "请设置安全邮箱！");
			if(!memberService.canChangeMobile(member)){
				return showJsonError(model, "手机绑定后7天内不能解除绑定！");			
			}
			msg = velocityTemplate.parseTemplate("home/action/removeBindMobile.vm", model);
		}else if(StringUtils.equals(tag, "change")&&StringUtils.isNotBlank(member.getMobile())){
			if(memberAccount== null || memberAccount.isNopassword()) return showJsonError(model, "先创建帐号或设置支付密码！");
			if(!memberService.canChangeMobile(member)){
				return showJsonError(model, "手机绑定后7天内不能修改绑定！");			
			}
			msg = velocityTemplate.parseTemplate("home/action/changeBindMobile.vm", model);
		}else{
			if(StringUtils.isNotBlank(member.getMobile()))
				msg = velocityTemplate.parseTemplate("home/action/successBindMobile.vm", model);
			else
				msg = velocityTemplate.parseTemplate("home/action/bindMobile.vm", model);
		}
		return showJsonSuccess(model, msg);
	}
}
