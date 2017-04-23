package com.gewara.web.action.gewapay;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.gewara.constant.BindConstant;
import com.gewara.constant.ChargeConstant;
import com.gewara.constant.GoodsConstant;
import com.gewara.constant.MemberConstant;
import com.gewara.constant.PaymethodConstant;
import com.gewara.constant.Status;
import com.gewara.constant.TokenType;
import com.gewara.constant.order.BuyItemConstant;
import com.gewara.constant.sys.LogTypeConstant;
import com.gewara.constant.sys.MongoData;
import com.gewara.constant.ticket.OrderConstant;
import com.gewara.model.drama.Drama;
import com.gewara.model.drama.DramaOrder;
import com.gewara.model.drama.OpenDramaItem;
import com.gewara.model.goods.BaseGoods;
import com.gewara.model.goods.Goods;
import com.gewara.model.movie.Movie;
import com.gewara.model.pay.BuyItem;
import com.gewara.model.pay.Charge;
import com.gewara.model.pay.ElecCard;
import com.gewara.model.pay.GewaOrder;
import com.gewara.model.pay.GoodsOrder;
import com.gewara.model.pay.GymOrder;
import com.gewara.model.pay.MemberAccount;
import com.gewara.model.pay.OrderExtra;
import com.gewara.model.pay.OrderNote;
import com.gewara.model.pay.PubSale;
import com.gewara.model.pay.PubSaleOrder;
import com.gewara.model.pay.SMSRecord;
import com.gewara.model.pay.SportOrder;
import com.gewara.model.pay.TicketOrder;
import com.gewara.model.sport.OpenTimeTable;
import com.gewara.model.sport.SellTimeTable;
import com.gewara.model.sport.Sport;
import com.gewara.model.ticket.OpenPlayItem;
import com.gewara.model.ticket.SellSeat;
import com.gewara.model.user.Member;
import com.gewara.model.user.MemberInfo;
import com.gewara.pay.PayUtil;
import com.gewara.service.MessageService;
import com.gewara.service.OperationService;
import com.gewara.service.drama.DramaOrderService;
import com.gewara.service.gewapay.ElecCardService;
import com.gewara.service.gewapay.InvoiceService;
import com.gewara.service.gewapay.PaymentService;
import com.gewara.service.member.BindMobileService;
import com.gewara.service.order.GoodsService;
import com.gewara.service.order.OrderQueryService;
import com.gewara.service.ticket.TicketOrderService;
import com.gewara.support.ErrorCode;
import com.gewara.untrans.gym.SynchGymService;
import com.gewara.untrans.monitor.MonitorService;
import com.gewara.util.BeanUtil;
import com.gewara.util.DateUtil;
import com.gewara.util.JsonUtils;
import com.gewara.util.StringUtil;
import com.gewara.util.ValidateUtil;
import com.gewara.util.VmUtils;
import com.gewara.util.WebUtils;
import com.gewara.web.action.BaseHomeController;
import com.gewara.web.util.PageUtil;
import com.gewara.xmlbind.gym.CardItem;

@Controller
public class AccountController extends BaseHomeController {
	@Autowired@Qualifier("ticketOrderService")
	private TicketOrderService ticketOrderService;
	public void setTicketOrderService(TicketOrderService ticketOrderService) {
		this.ticketOrderService = ticketOrderService;
	}
	@Autowired@Qualifier("elecCardService")
	private ElecCardService elecCardService;
	public void setElecCardService(ElecCardService elecCardService) {
		this.elecCardService = elecCardService;
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
	@Autowired@Qualifier("paymentService")
	private PaymentService paymentService;
	public void setPaymentService(PaymentService paymentService) {
		this.paymentService = paymentService;
	}
	@Autowired@Qualifier("invoiceService")
	private InvoiceService invoiceService;
	
	public void setInvoiceService(InvoiceService invoiceService) {
		this.invoiceService = invoiceService;
	}
	@Autowired@Qualifier("messageService")
	private MessageService messageService;
	public void setMessageService(MessageService messageService) {
		this.messageService = messageService;
	}
	@Autowired@Qualifier("operationService")
	private OperationService operationService;
	public void setOperationService(OperationService operationService) {
		this.operationService = operationService;
	}
	@Autowired@Qualifier("monitorService")
	private MonitorService monitorService;
	public void setMonitorService(MonitorService monitorService) {
		this.monitorService = monitorService;
	}
	@Autowired@Qualifier("bindMobileService")
	private BindMobileService bindMobileService;
	public void setBindMobileService(BindMobileService bindMobileService){
		this.bindMobileService = bindMobileService;
	}
	@Autowired@Qualifier("goodsService")
	private GoodsService goodsService;
	public void setGoodsService(GoodsService goodsService) {
		this.goodsService = goodsService;
	}
	
	@Autowired@Qualifier("dramaOrderService")
	private DramaOrderService dramaOrderService;
	
	
	// 设置支付密码前置手机动态码验证页面
	@RequestMapping("/home/acct/mbrMobileAuthPay.xhtml")
	public String mbrMobileAuth(ModelMap model,String op,HttpServletRequest request) {
		boolean isNeedCaptha = bindMobileService.isNeedToken(TokenType.PayCheckPass, WebUtils.getRemoteIp(request), 2);
		model.put("needCaptcha", isNeedCaptha);
		Member member = getLogonMember();
		if(!member.isBindMobile()){
			model.put("flag", "pass");
			return "redirect:/home/acct/bindMobile.xhtml";
		}
		model.put("mobile", member.getMobile());
		String urlString = "";
		if (StringUtils.equals(op, "mobileAuthPayGet")) {
			urlString = "home/acct/accountSafety/mobileAuthPayGet.vm";
		}else if (StringUtils.equals(op, "mobileAuthPayMdy")) {
			urlString = "home/acct/accountSafety/mobileAuthPayMdy.vm";
		}else if (StringUtils.equals(op, "mobileAuthPaySet")) {
			urlString = "home/acct/accountSafety/mobileAuthPaySet.vm";
		}else{
			return show404(model, "404");
		}
		return urlString;
	}
	@RequestMapping("/home/acct/VDPayCkPs.xhtml")
	public String validateCheckPass(String checkpass,String operation,ModelMap model){
		Member member = getLogonMember();
		if(StringUtils.isBlank(checkpass)) {
			return showJsonError(model,"动态码错误！");
		}
		ErrorCode code = bindMobileService.preCheckBindMobile(operation, member.getMobile(), checkpass);
		if(!code.isSuccess()){
			return showJsonError(model, code.getMsg());
		}
		model.put("checkpass", checkpass);
		return showJsonSuccess(model);
	}
	//验证动态码有效性，返回设置支付密码页面
	@RequestMapping("/home/acct/mbrIdtAuthPayCkPs.xhtml")
	public String mbrIdtAuthCkPs(String checkpass,String operation,ModelMap model){
		Member member = getLogonMember();
		if(StringUtils.isBlank(checkpass)) {
			return show404(model, "非法操作！");
		}
		ErrorCode code = bindMobileService.preCheckBindMobile(operation, member.getMobile(), checkpass);
		if(!code.isSuccess()){
			return show404(model, "非法操作！");
		}
		if (operation.equals("setpaypass"))
			return "home/acct/accountSafety/setPayPass.vm";
		else if (operation.equals("mdypaypass"))
			return "home/acct/accountSafety/mdyPayPass.vm";
		else if (operation.equals("getpaypass"))
			return "home/acct/accountSafety/getPayPass.vm";
		return null;
	}
	//获取动态码
	@RequestMapping("/home/acct/loadPayCheckPass.xhtml")
	public String loadCheckPass(HttpServletRequest request, String captchaId, String captcha,String operation, ModelMap model){
		Member member = getLogonMember();
		Map jsonMap = new HashMap();
		Map errorMap = new HashMap();
		String ip = WebUtils.getRemoteIp(request);
		boolean iscaptcha = bindMobileService.isNeedToken(TokenType.PayCheckPass, ip, 2);
		jsonMap.put("errorMap", errorMap);
		if(iscaptcha){
			jsonMap.put("refreshCaptcha", "true");
			model.put("iscaptcha", iscaptcha);
			if(StringUtils.isBlank(captcha)){
				errorMap.put("captcha", "请输入验证码！");
				return showJsonError(model, jsonMap);
			}else{
				boolean isValidCaptcha = controllerService.validateCaptcha(captchaId, captcha,WebUtils.getRemoteIp(request));
				errorMap.put("captcha", "验证码错误！");
				if(!isValidCaptcha) return showJsonError(model, jsonMap);
			}
		}
		boolean next = bindMobileService.getAndUpdateToken(TokenType.PayCheckPass, ip, 2);
		if(next){
			jsonMap.put("refreshCaptcha", "true");
		}

		String checkMsg = checkMemberResource(member);
		if(StringUtils.isNotBlank(checkMsg)) return showJsonError(model, checkMsg);
		ErrorCode<SMSRecord> code = bindMobileService.refreshBindMobile(operation, member.getMobile(), ip);
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

	@RequestMapping(method = RequestMethod.POST, value = "/home/acct/saveAccountCkPs.xhtml")
	public String saveAccountCkPs(String checkpass, String realname, String password,
			String confirmPassword, String idcard, Integer certtype, String emcontact, String emmobile, ModelMap model, HttpServletRequest request) {
		Member member = getLogonMember();
		if(StringUtils.isBlank(checkpass)) {
			return showJsonError(model,"动态码不能为空！");
		}

		if(StringUtils.isBlank(realname)) return showJsonError(model, "真实姓名不能为空！");
		if(StringUtil.getByteLength(realname)>30) return showJsonError(model, "真实姓名过长！");
		if(StringUtils.isBlank(emcontact)) return showJsonError(model, "紧急联系人姓名不能为空！");
		if(StringUtil.getByteLength(emcontact)>30) return showJsonError(model, "紧急联系人姓名过长！");
		if(StringUtils.isBlank(idcard)) return showJsonError(model, "证件号码不能为空！");
		if(!StringUtil.regMatch(idcard, "^[A-Za-z0-9_]{6,30}$", true)) return showJsonError(model, "证件号码格式不正确,只能是字母，数字，下划线，长度6—30位！");
		if(certtype==null || StringUtil.getByteLength(certtype+"")!=1) return showJsonError(model, "证件类型不正确！");
		if(!ValidateUtil.isMobile(emmobile)) return showJsonError(model, "手机号码格式不正确！");
		if(StringUtils.isNotBlank(password) || StringUtils.isNotBlank(confirmPassword)){
			if(!StringUtil.regMatch(password, "^[a-zA-Z0-9]{6,18}$", true)) return showJsonError(model, "密码格式错误，请重新设置！");
			if("123456".equals(password) || StringUtils.length(password) < 6) return showJsonError(model, "密码过于简单，请重新设置！");
			if(!StringUtils.equals(password, confirmPassword)) return showJsonError(model, "两次输入的密码不一致！");
		}else{
			return showJsonError(model, "密码不能为空！");
		}
		
		ErrorCode code = bindMobileService.preCheckBindMobile(BindConstant.TAG_SETPAYPASS, member.getMobile(), checkpass);
		if(!code.isSuccess()){
			return showJsonError(model, code.getMsg());
		}
		
		MemberAccount account = daoService.getObjectByUkey(MemberAccount.class, "memberid", member.getId(), false);
		if(account == null) account = paymentService.createNewAccount(member);
		else{
			if(!account.isNopassword()) return showJsonError(model, "该用户已经设置过支付密码！");
		}
		if(StringUtils.equals(StringUtil.md5(password), member.getPassword())){
			return showJsonError(model, "支付密码不能跟登录密码相同！");
		}
		bindMobileService.checkBindMobile(BindConstant.TAG_SETPAYPASS, member.getMobile(), checkpass);
		account.setPassword(PayUtil.getPass(password));
		account.setRealname(realname);
		String encryCard = paymentService.getEncryptIdcard(StringUtil.ToDBC(idcard));
		account.setEncryidcard(encryCard);//后台经常报错(用户输入全角)
		account.setCerttype(certtype);
		account.setEmcontact(emcontact);
		account.setEmmobile(emmobile);
		account.setUpdatetime(new Timestamp(System.currentTimeMillis()));
		daoService.saveObject(account);
		
		monitorService.saveMemberLog(member.getId(), MemberConstant.ACTION_SETPAYPWD, null, WebUtils.getRemoteIp(request));
		return showJsonSuccess(model);
	}
	@RequestMapping(method = RequestMethod.POST, value = "/home/acct/mdyActCkPs.xhtml")
	public String mdyActCkPs(String checkpass, String oldpass, String password,
			String confirmPassword, ModelMap model, HttpServletRequest request) {
		Member member = getLogonMember();
		if(member == null) {
			return showJsonError(model, "请先登录！");
		}
		if(StringUtils.isBlank(checkpass)) {
			return showJsonError(model,"动态码不能为空！");
		}
		if(StringUtils.isNotBlank(oldpass) && StringUtils.isNotBlank(password) && StringUtils.isNotBlank(confirmPassword)){
			if(!StringUtil.regMatch(password, "^[a-zA-Z0-9]{6,18}$", true)) return showJsonError(model, "密码格式错误，请重新设置！");
			if("123456".equals(password) || StringUtils.length(password) < 6) return showJsonError(model, "密码过于简单，请重新设置！");
			if(!StringUtils.equals(password, confirmPassword)) return showJsonError(model, "两次输入的密码不一致！");
		}else{
			return showJsonError(model, "密码不能为空！");
		}
		
		ErrorCode result = bindMobileService.preCheckBindMobile(BindConstant.TAG_MDYPAYPASS, member.getMobile(), checkpass);
		if(!result.isSuccess()){
			return showJsonError(model, result.getMsg());
		}
		
		MemberAccount account = daoService.getObjectByUkey(MemberAccount.class, "memberid", member.getId(), false);
		if(account == null)  return showJsonError(model, "该用户尚未设置过支付密码！");
		if(StringUtils.equals(StringUtil.md5(password), member.getPassword())){
			return showJsonError(model, "支付密码不能跟登录密码相同！");
		}
		if(!PayUtil.passEquals(oldpass, account.getPassword())){
			return showJsonError(model, "旧密码错误！");
		}
		ErrorCode code = bindMobileService.checkBindMobile(BindConstant.TAG_MDYPAYPASS, member.getMobile(), checkpass);
		if(!code.isSuccess()){
			return showJsonError(model, code.getMsg());
		}
		account.setPassword(PayUtil.getPass(password));
		account.setUpdatetime(new Timestamp(System.currentTimeMillis()));
		daoService.saveObject(account);
		monitorService.saveMemberLog(member.getId(), MemberConstant.ACTION_MDYPAYPWD, null, WebUtils.getRemoteIp(request));
		return showJsonSuccess(model);
	}
	@RequestMapping(method = RequestMethod.POST, value = "/home/acct/getActCkPs.xhtml")
	public String getActCkPs(String checkpass, String password,
			String confirmPassword,ModelMap model, HttpServletRequest request) {
		Member member = getLogonMember();
		if(StringUtils.isBlank(checkpass)) {
			return showJsonError(model,"动态码不能为空！");
		}
		if(StringUtils.isNotBlank(password) && StringUtils.isNotBlank(confirmPassword)){
			if(!ValidateUtil.isPassword(confirmPassword)) return showJsonError(model, "密码格式错误，请重新设置！");
			if("123456".equals(password)) return showJsonError(model, "密码过于简单，请重新设置！");
			if(!StringUtils.equals(password, confirmPassword)) return showJsonError(model, "两次输入的密码不一致！");
		}else{
			return showJsonError(model, "密码不能为空！");
		}
		
		ErrorCode code = bindMobileService.preCheckBindMobile(BindConstant.TAG_GETPAYPASS, member.getMobile(), checkpass);
		if(!code.isSuccess()){
			return showJsonError(model, code.getMsg());
		}
		
		MemberAccount account = daoService.getObjectByUkey(MemberAccount.class, "memberid", member.getId(), false);
		if(account == null) return showJsonError(model, "您尚未设置支付密码！");

		if(StringUtils.equals(StringUtil.md5(password), member.getPassword())){
			return showJsonError(model, "支付密码不能和登录密码相同！");
		}

		ErrorCode result = bindMobileService.checkBindMobile(BindConstant.TAG_GETPAYPASS, member.getMobile(), checkpass);
		if(!result.isSuccess()){
			return showJsonError(model, result.getMsg());
		}
		account.setPassword(PayUtil.getPass(password));
		account.setUpdatetime(new Timestamp(System.currentTimeMillis()));
		daoService.saveObject(account);
		monitorService.saveMemberLog(member.getId(), MemberConstant.ACTION_GETPAYPWD, null, WebUtils.getRemoteIp(request));
		return showJsonSuccess(model);
	}

	@RequestMapping("/home/charge.xhtml")
	public String charge(Long cid, HttpServletRequest request, ModelMap model){
		Member member = getLogonMember();
		MemberAccount account = daoService.getObjectByUkey(MemberAccount.class, "memberid", member.getId(), false);
		if(account == null){ 
			model.put("notPassCheck", true);
			model.putAll(controllerService.getCommonData(model, member, member.getId()));
			model.put("account", account);
			model.put("member", member);
			return "home/acct/charge.vm";
			//account = paymentService.createNewAccount(member);
		}
		if(account.isIncomplete() || account.isNopassword()) {
			model.put("notPassCheck", true);
		}else{
			model.put("notPassCheck", false);
		}
		model.putAll(controllerService.getCommonData(model, member, member.getId()));
		model.put("account", account);
		model.put("member", member);
		if(cid != null){
			Charge charge = daoService.getObject(Charge.class, cid);
			if(charge != null && charge.isNew() && charge.getMemberid().equals(member.getId())){
				return showRedirect(paymentService.getChargePayUrl(charge, WebUtils.getRemoteIp(request)), model);
			}else{
				return showError(model, "此信息无效，不能修改！");
			}
		}
		Map param = new HashMap();
		param.put(MongoData.ACTION_TYPE, "recharge");
		param.put(MongoData.ACTION_TAG, "recharge");
		Map map = mongoService.findOne(MongoData.NS_INTEGRAL, param);
		if(map != null) model.put("map", map);
		return "home/acct/charge.vm";
	}
	@RequestMapping("/home/bankToWabi.xhtml")
	public String bankToWabi(HttpServletRequest request, Integer bank, ModelMap model){
		Member member = getLogonMember();
		if(bank==null || bank<=0) return showJsonError(model, "输入数字错误，请输入大于0的整数");
		MemberAccount account = daoService.getObjectByUkey(MemberAccount.class, "memberid", member.getId(), false);
		ErrorCode<Integer> code = paymentService.bankToWaBi(account, bank);
		if(!code.isSuccess()) return showJsonError(model, code.getMsg());
		String result = "恭喜，系统已成功将<b>" + bank + "</b>账户金额转化为<b>" + bank + "</b>瓦币";
		Integer point = code.getRetval(); 
		if(point>0) result = result + "<br/>同时已为你的账户增加" + point + "积分";
		monitorService.saveMemberLog(member.getId(), MemberConstant.ACTION_TOWABI, ""+bank, WebUtils.getRemoteIp(request));
		return showJsonSuccess(model, result);
	}
	@RequestMapping("/home/toChangeBank.xhtml")
	public String toChangeBank( ModelMap model){
		Member member = getLogonMember();
		MemberAccount account = daoService.getObjectByUkey(MemberAccount.class, "memberid", member.getId(), false);
		model.put("account", account);
		return "home/acct/bankToWabi.vm";
	}
	@RequestMapping("/home/confirmCharge.xhtml")
	public String chargeConfirm(Integer totalfee, Long cid, String paymethod, String paybank, String cardpass, String chargeto, 
			String mbank, ModelMap model, HttpServletRequest request){
		List<String> limitList = paymentService.getLimitPayList();
		if(limitList.contains(paymethod)) return showJsonError(model, "暂不支持该方式充值，请联系客服");
		Member member = getLogonMember();
		if(paybank == null) paybank = "";
		Map jsonMap = new HashMap();
		//检查账户信息是否完整
		MemberAccount account = daoService.getObjectByUkey(MemberAccount.class, "memberid", member.getId(), false);
		if(account == null || account.isNopassword()){
			jsonMap.put("account", "false");
			return showJsonError(model, jsonMap);
		}
		Charge charge = null;
		
		// 检测充值方式是否有效 pnrPay, directPay, lakalaPay, ccardPay
		List<String> paymethods = Arrays.asList("pnrPay", "directPay", "lakalaPay", "ccardPay","telecomPay");
		if(!paymethods.contains(paymethod)) return showJsonError(model, "请选择正确的充值方式！");
		
		if(StringUtils.equals(paymethod, PaymethodConstant.PAYMETHOD_CHARGECARD)){
			try{
				ErrorCode<ElecCard> code = elecCardService.chargeByCard(member, account, cardpass, WebUtils.getRemoteIp(request));
				if(code.isSuccess()){
					ElecCard card = code.getRetval();
					//TODO:发短信功能暂时隐藏
					//添加发送短信功能提示
					/*String sendMsg = "你的贵宾卡"+card.getCardno()+"已激活，已为你的账号充值"+card.getEbatch().getAmount()+"瓦币（1瓦币=1元），可以用于在格瓦拉平台消费，如有疑问请联系客服：4000-406-506";
					if(StringUtils.isNotBlank(member.getMobile())){
						 messageService.addManualMsg(member.getId(), member.getMobile(), sendMsg, null);
					}*/
					return showJsonSuccess(model, "充值成功，充值金额为" +  card.getEbatch().getAmount());
				}else {
					return showJsonError(model, code.getMsg());
				}
			}catch(Exception e){
				dbLogger.errorWithType(LogTypeConstant.LOG_TYPE_ACCOUNT, StringUtil.getExceptionTrace(e));
				return showJsonError(model, "充值错误！");
			}
		}else if(StringUtils.equals(paymethod, PaymethodConstant.PAYMETHOD_TELECOM)){
			int payAmount = 0;
			if(StringUtils.isNotBlank(request.getParameter("payAmount"))){
				payAmount = Integer.parseInt(request.getParameter("payAmount"));
			}
			totalfee = payAmount;
			paybank = mbank;
		}
		if(totalfee==null) return showJsonError(model, "请输入充值金额！");
		if(totalfee < 1) return showJsonError(model, "充值金额无效！");
		if(totalfee > 500) return showJsonError(model, "充值金额不能超过500元！");
		if(account.getBanlance() > 2000) return showJsonError(model, "你的站内余额已超过2000，暂不支持充值！");
		if(cid != null){
			charge = daoService.getObject(Charge.class, cid);
			if(charge != null && !charge.getMemberid().equals(member.getId())){
				charge = null;
			}
		}
		if(!StringUtils.equals(chargeto, ChargeConstant.BANKPAY) && !StringUtils.equals(chargeto, ChargeConstant.WABIPAY)){
			chargeto = ChargeConstant.WABIPAY;
		}
		if(StringUtils.equals(paymethod, PaymethodConstant.PAYMETHOD_ALIPAY)) paybank = "";
		//if(StringUtils.equals(paymethod, PayUtil.PAYMETHOD_PNRPAY) && StringUtils.length(paybank)!=2) paybank="";

		if(charge == null){
			charge = new Charge(PayUtil.getChargeTradeNo(), chargeto);
			charge.setMemberid(member.getId());
			charge.setMembername(member.getNickname());
			charge.setValidtime(DateUtil.addHour(charge.getAddtime(), 2));
		}
		charge.setChargeto(chargeto);
		charge.setPaymethod(paymethod);
		charge.setPaybank(paybank);
		charge.setTotalfee(totalfee);
		daoService.saveObject(charge);
		jsonMap.put("redirectUrl", paymentService.getChargePayUrl(charge, WebUtils.getRemoteIp(request)));
		return showJsonSuccess(model, jsonMap);
	}
	
	@RequestMapping("/home/lakala/sendSms.xhtml")
	public String resend(String captchaId, String captcha, HttpServletRequest request, ModelMap model){
		boolean isValid = controllerService.validateCaptcha(captchaId, captcha, WebUtils.getRemoteIp(request));
		if(!isValid) return showJsonError(model, "验证码错误！");
		
		Member member = getLogonMember();
		if(!member.isBindMobile()) return showJsonError(model, "请先进入你的用户资料绑定你的手机号码！");
		//间隔120秒，1小时最多发送3次
		boolean allow = operationService.updateOperation("320103"+member.getMobile(), 120, 60*60, 3);
		if(!allow) return showJsonError(model, "请不要重复操作！");
		String msg = "格瓦拉账户充值交易号【320103 "+ member.getMobile()+"】，请至附近便利店使用拉卡拉快速充值。";
		SMSRecord sms = messageService.addManualMsg(member.getId(), member.getMobile(), msg, "320103"+member.getMobile());
		untransService.sendMsgAtServer(sms, true);
		return showJsonSuccess(model);
	}
	
	@RequestMapping("/home/lakala/showlakalaIntroduce.xhtml")
	public String showLaKaLaIntroduce(ModelMap model){
		Member member = getLogonMember();
		model.putAll(controllerService.getCommonData(model, member, member.getId()));
		return "home/acct/lakalaPayIntroduce.vm";
	}
	
	@RequestMapping("/home/cancelCharge.xhtml")
	public String cancelCharge(@RequestParam("cid") Long cid, ModelMap model){
		Member member = getLogonMember();
		Charge charge = daoService.getObject(Charge.class, cid);
		if(charge != null){
			if(charge.getMemberid().equals(member.getId()) && charge.isNew()){
				charge.setStatus(Charge.STATUS_CANCEL);
				daoService.saveObject(charge);
				model.put("msg", "取消成功！");
			}else{
				model.put("msg", "非新充值，不能取消！");
			}
		}else{
			model.put("msg", "不存在的账单，可能是之前取消成功了！");
		}
		return showRedirect("/home/myAccount.xhtml", model);
	}
			
	@RequestMapping("/home/modifyAccount.xhtml")
	public String modifyAccount(String password, ModelMap model){
		Member member = getLogonMember();
		if(member==null)  return showError(model, "您还没登录，请返回登录！");
		model.put("member", member);
		model.putAll(controllerService.getCommonData(model, member, member.getId()));
		MemberAccount account = daoService.getObjectByUkey(MemberAccount.class, "memberid", member.getId(), false);
		if(account == null){ //建立一个新账号
			account = paymentService.createNewAccount(member);
		}
		//解密账号身份证号
		if(StringUtils.isNotBlank(account.getEncryidcard())){
			String encryidcard = paymentService.getDecryptIdcard(account.getEncryidcard());
			account.setEncryidcard(encryidcard);
		}
		model.put("account", account);
		if(account.isIncomplete()) {
			model.put("msg", "账户信息不完整（提示：没设置新密码？）");
			if(account.isNopassword()) model.put("passCheck", true);
		}
		if(PayUtil.passEquals(password, account.getPassword())) model.put("passCheck", true);
		else model.put("errorPass", "支付密码不正确！");
		return "home/acct/modifyAccount.vm";
	}
	
	/***
	 * 20101018 bobo 添加订单管理 
	 */
	@RequestMapping("/home/myOrderManage.xhtml")
	public String showMyOrderManage(ModelMap model, String status) {
		if(StringUtils.isBlank(status)) status = OrderConstant.STATUS_PAID;
		Member member = getLogonMember();
		model.putAll(controllerService.getCommonData(model, member, member.getId()));
		model.put("member", member);
		return "home/acct/myorderManage.vm";
	}
	/***
	 * 导航ajax加载3条订单信息
	 */
	@RequestMapping("/home/navigationOrderManageStatus.xhtml")
	public String navigationOrderManageStatus(ModelMap model){
		Member member = getLogonMember();
		List<GewaOrder> orderList = orderQueryService.getOrderListByMemberId(member.getId(), 180, 0, 3);
		Timestamp currentTime = DateUtil.getMillTimestamp();
		List<Long> orderIdList = new ArrayList<Long>();
		List<Long> orderDelList = new ArrayList<Long>();
		orderList = BeanUtil.getSubList(orderList, 0, 3);
		for (GewaOrder gewaOrder : orderList) {
			if(gewaOrder instanceof TicketOrder){
				OpenPlayItem openPlayItem = daoService.getObjectByUkey(OpenPlayItem.class, "mpid", ((TicketOrder)gewaOrder).getMpid(), true);
				if(openPlayItem!=null){
					if(!currentTime.after(DateUtil.addHour(openPlayItem.getPlaytime(), 2))){
						orderIdList.add(gewaOrder.getId());
					}
					if(currentTime.after(DateUtil.addHour(openPlayItem.getPlaytime(), 2))){
						orderDelList.add(gewaOrder.getId());
					}
				}
			}else if(gewaOrder instanceof DramaOrder){
				if(gewaOrder.isPaidSuccess()){
					OpenDramaItem odi = daoService.getObjectByUkey(OpenDramaItem.class, "dpid", ((DramaOrder)gewaOrder).getDpid(), true);
					List<BuyItem> buyList = daoService.getObjectListByField(BuyItem.class, "orderid", gewaOrder.getId());
					List<OpenDramaItem> itemList = dramaOrderService.getOpenDramaItemList(odi, buyList);
					Timestamp endtime = null;
					for (OpenDramaItem item : itemList) {
						Timestamp tmp = null;
						if(item.hasPeriod(Status.Y)){
							tmp = item.getPlaytime();
						}else{
							tmp = item.getEndtime();
						}
						if(endtime == null ) endtime = tmp;
						else if(endtime.before(tmp)) endtime = tmp;
					}
					if(!currentTime.after(DateUtil.addHour(endtime, 2))){
						orderIdList.add(gewaOrder.getId());
					}
				}
			}
		}
		model.put("orderDelList", orderDelList);
		// 订单关联
		initRelatedOrders(orderList, model);
		initDeleteOrderStatus(orderList,model);
		model.put("orderList", orderList);
		List<Long> idList = BeanUtil.getBeanPropertyList(orderList, "id", true);
		Map<Long, OrderExtra> orderExtraMap = daoService.getObjectMap(OrderExtra.class, idList);
		model.put("orderExtraMap", orderExtraMap);
		model.put("member", member);
		model.put("orderIdList", orderIdList);
		return "home/acct/myorderNavigationTable.vm";
	}
	/***
	 * 20101018 bobo 订单状态ajax 
	 */
	@RequestMapping("/home/myOrderManageQueryDateTable.xhtml")
	public String myOrderManageStatus(String queryDate, Integer pageNo, ModelMap model){
		Member member = getLogonMember();
		if(StringUtils.isBlank(queryDate)) queryDate = "60";
		
		if(pageNo == null) pageNo = 0;
		Integer rowsPerPage = 10;
		Integer from = pageNo * rowsPerPage;
		Integer days = new Integer(queryDate);
		List<GewaOrder> orderList = orderQueryService.getOrderListByMemberId(member.getId(), days, from, rowsPerPage);
		Timestamp currentTime = DateUtil.getMillTimestamp();
		List<Long> orderIdList = new ArrayList<Long>();
		List<Long> orderDelList = new ArrayList<Long>();
		Map<Long, Long> orderCinema = new HashMap<Long, Long>();
		for (GewaOrder gewaOrder : orderList) {
			if(gewaOrder instanceof TicketOrder){
				OpenPlayItem openPlayItem = daoService.getObjectByUkey(OpenPlayItem.class, "mpid", ((TicketOrder)gewaOrder).getMpid(), true);
				if(openPlayItem!=null){
					if(!currentTime.after(DateUtil.addHour(openPlayItem.getPlaytime(), 2))){
						orderIdList.add(gewaOrder.getId());
						orderCinema.put(gewaOrder.getId(), openPlayItem.getCinemaid());
					}
					if(currentTime.after(DateUtil.addHour(openPlayItem.getPlaytime(), 2))){
						orderDelList.add(gewaOrder.getId());
					}
				}else if (openPlayItem == null) {
					orderDelList.add(gewaOrder.getId());
				}
			}else if(gewaOrder instanceof DramaOrder){
				if(gewaOrder.isPaidSuccess()){
					OpenDramaItem odi = daoService.getObjectByUkey(OpenDramaItem.class, "dpid", ((DramaOrder)gewaOrder).getDpid(), true);
					List<BuyItem> buyList = daoService.getObjectListByField(BuyItem.class, "orderid", gewaOrder.getId());
					List<OpenDramaItem> itemList = dramaOrderService.getOpenDramaItemList(odi, buyList);
					Timestamp endtime = null;
					for (OpenDramaItem item : itemList) {
						Timestamp tmp = null;
						if(item.hasPeriod(Status.Y)){
							tmp = item.getPlaytime();
						}else{
							tmp = item.getEndtime();
						}
						if(endtime == null ) endtime = tmp;
						else if(endtime.before(tmp)) endtime = tmp;
					}
					if(!currentTime.after(DateUtil.addHour(endtime, 2))){
						orderIdList.add(gewaOrder.getId());
					}
				}
			}
		}
		//订单所在影院是否有卖品出售
		Map<Long, Boolean> hasGoods = new HashMap<Long, Boolean>();
		Iterator<Long> cinemaIds = orderCinema.values().iterator();
		while (cinemaIds.hasNext()){
			Long goodsCinemaId = cinemaIds.next();
			Integer goodsCount = goodsService.getGoodsCount(Goods.class, GoodsConstant.GOODS_TAG_BMH, goodsCinemaId, true, true, true);
			if (goodsCount > 0)
				hasGoods.put(goodsCinemaId, true);
		}
		
		model.put("orderCinema", orderCinema);
		model.put("orderIdList", orderIdList);
		model.put("orderDelList", orderDelList);
		model.put("hasGoods", hasGoods);
		Integer count =  orderQueryService.getOrderCountByMemberId(member.getId(), days);
		List<Long> idList = BeanUtil.getBeanPropertyList(orderList, "id", true);
		Map<Long, OrderExtra> orderExtraMap = daoService.getObjectMap(OrderExtra.class, idList);
		model.put("orderExtraMap", orderExtraMap);
		
		// 订单关联
		initRelatedOrders(orderList, model);
		initDeleteOrderStatus(orderList,model);
		PageUtil pageUtil = new PageUtil(count, rowsPerPage, pageNo, "/home/myOrderManageQueryDateTable.xhtml", true, true);
		Map params = new HashMap();
		params.put("queryDate", queryDate);
		pageUtil.initPageInfo(params);
		model.put("member", member);
		model.put("pageUtil", pageUtil);
		model.put("orderList", orderList);
		model.put("queryDate", queryDate);
		return "home/acct/myorderManagetable.vm";
	}
	//查询是否可以删除订单的状态
	private void initDeleteOrderStatus(List<GewaOrder> orderList, ModelMap model){
		if(orderList!=null){
			Map delStatusMap = new HashMap();
			for(GewaOrder gewaOrder: orderList){
				Map descOrder = VmUtils.readJsonToMap(gewaOrder.getDescription2());
				if(descOrder!=null){
					String orderDate = (String) descOrder.get("时间");
					if(gewaOrder instanceof SportOrder){
						OpenTimeTable ott = daoService.getObject(OpenTimeTable.class, ((SportOrder)gewaOrder).getOttid());
						if(!ott.hasField()){
							SellTimeTable stt = daoService.getObject(SellTimeTable.class, gewaOrder.getId());
							orderDate = DateUtil.formatTimestamp(ott.getPlayTimeByHour(stt.getStarttime()));
						}
					}
					double dateNum =  DateUtil.getDiffHour(DateUtil.getCurFullTimestamp(),DateUtil.parseDate(orderDate,"yyyy-MM-dd HH:mm"));
					if(dateNum >2){
						delStatusMap.put(gewaOrder.getId(),'Y');
					}else{
						delStatusMap.put(gewaOrder.getId(),'N');
					}
				}
			}
			model.put("delStatusMap",delStatusMap );
		}
	}
	private void initRelatedOrders(List<GewaOrder> orderList, ModelMap model){
		Map<Long, Map<String, String>> dataMap = new HashMap<Long, Map<String, String>>();
		for(GewaOrder gewaOrder: orderList){
			Map<String, String> cMap = new HashMap<String, String>();
			if(gewaOrder instanceof TicketOrder) {
				TicketOrder order = (TicketOrder)gewaOrder;
				Movie object = daoService.getObject(Movie.class, order.getMovieid());
				if(object != null){
					cMap.put("id", ""+object.getId());
					cMap.put("name", object.getName());
					cMap.put("img", object.getLimg());
					cMap.put("type", order.getOrdertype());
				}
			}else if(gewaOrder instanceof DramaOrder){
				DramaOrder order = (DramaOrder)gewaOrder;
				Drama object = daoService.getObject(Drama.class, order.getDramaid());
				if(object != null){
					cMap.put("id", ""+object.getId());
					cMap.put("name", object.getName());
					cMap.put("img", object.getLimg());
					cMap.put("type", order.getOrdertype());
				}
			}else if(gewaOrder instanceof SportOrder){
				SportOrder order = (SportOrder)gewaOrder;
				Sport object = daoService.getObject(Sport.class, order.getSportid());
				if(object != null){
					cMap.put("id", ""+object.getId());
					cMap.put("name", object.getName());
					cMap.put("img", object.getLimg());
					cMap.put("type", order.getOrdertype());
				}
			}else if(gewaOrder instanceof GoodsOrder){
				GoodsOrder order = (GoodsOrder)gewaOrder;
				BaseGoods object = daoService.getObject(BaseGoods.class, order.getGoodsid());
				if(object != null){
					cMap.put("id", ""+object.getRelatedid());
					cMap.put("name", object.getGoodsname());
					cMap.put("img", object.getLimg());
					cMap.put("type", order.getOrdertype());
				}
			}else if(gewaOrder instanceof PubSaleOrder){
				PubSaleOrder order = (PubSaleOrder)gewaOrder;
				PubSale object = daoService.getObject(PubSale.class, order.getPubid());
				if(object != null){
					cMap.put("id", ""+object.getId());
					cMap.put("name", object.getName());
					cMap.put("img", object.getLimg());
					cMap.put("type", order.getOrdertype());
				}
			}else if(gewaOrder instanceof GymOrder){
				GymOrder order = (GymOrder)gewaOrder;
				ErrorCode<CardItem>  code = synchGymService.getGymCardItem(order.getGci(), true);
				if(code.isSuccess()){
					CardItem object = code.getRetval();
					cMap.put("id", ""+object.getId());
					cMap.put("name", object.getName());
					cMap.put("img", object.getLogo());
					cMap.put("type", order.getOrdertype());
				}
			}
			dataMap.put(gewaOrder.getId(), cMap);
		}
		model.put("dataMap", dataMap);
	}
	
	@RequestMapping("/home/myAccount.xhtml")
	public String showAccount(ModelMap model) {
		Member member = getLogonMember();
		if(member==null)  return alertMessage(model, "您还没登录，请返回登录！");
		model.putAll(controllerService.getCommonData(model, member, member.getId()));
		MemberAccount account = daoService.getObjectByUkey(MemberAccount.class, "memberid", member.getId(), false);
		if(account == null) //account = paymentService.createNewAccount(member);
			return alertMessage(model, "你还没有建立账户!");
		model.put("member", member);
		model.put("account", account);
		return "home/acct/orderManage.vm";
	}
	
	@RequestMapping("/home/myAccountTable.xhtml")
	public String ajaxMyAccountTable(Timestamp startTime, Timestamp endTime, Integer pageNo, ModelMap model){
		Member member = getLogonMember();
		if(pageNo == null) pageNo = 0;
		Integer rowsPerPage = 10;
		Integer from = pageNo * rowsPerPage;
		Timestamp compareTime = DateUtil.parseTimestamp("2010-01-01 00:00:00");
		if(startTime != null){
			if(startTime.getTime() < compareTime.getTime()){
				startTime = compareTime;
			}
		}
		List<Charge> chargeList = paymentService.getChargeListByMemberId(member.getId(), startTime, endTime, from, rowsPerPage);
		Integer count = paymentService.getChargeCountByMemberId(member.getId(), true, startTime,endTime);
		PageUtil pageUtil = new PageUtil(count,rowsPerPage,pageNo,"home/myAccountTable.xhtml", true, true);
		List<String> openedIdList = invoiceService.getOpenedRelatedidList(member.getId());
		model.put("openedIdList", openedIdList);
		Map params = new HashMap();
		params.put("startTime", startTime);
		params.put("endTime", endTime);
		pageUtil.initPageInfo(params);
		model.put("pageUtil", pageUtil);
		model.put("chargeList", chargeList);
		return "home/acct/orderManageTable.vm";
	}
	@RequestMapping(method=RequestMethod.POST,value="/home/saveAccount.xhtml")
	public String saveAccount(String realname, String password, String confirmPassword, String idcard, String isAjax, ModelMap model){
		if(StringUtils.isBlank(realname)) return showJsonError(model, "真实姓名不能为空！");
		if(StringUtil.getByteLength(realname)>30) return showJsonError(model, "真实姓名过长！");
		Member member = getLogonMember();
		ErrorCode<MemberAccount> result = paymentService.createOrUpdateAccount(member, realname, password, confirmPassword, idcard);
		if(!result.isSuccess()){
			return showJsonError(model, result.getMsg());
		}
		if(StringUtils.isNotBlank(isAjax)){
			return showJsonSuccess(model);
		}
		return "redirect:/home/modifyAccount.xhtml";
	}
	
	// 修改账户密码
	@RequestMapping("/home/saveAccountPwd.xhtml")
	public String saveAccountPwd(String oldPassword, String password, String confirmPassword, HttpServletRequest request, ModelMap model){
		Member member = getLogonMember();
		ErrorCode<MemberAccount> result = paymentService.updateAccountPassword(member, oldPassword, password, confirmPassword);
		if(!result.isSuccess()) return showJsonError(model, result.getMsg());
		monitorService.saveMemberLog(member.getId(), MemberConstant.ACTION_MDYPAYPWD, null, WebUtils.getRemoteIp(request));
		return showJsonSuccess(model);
	}
	
	//查询订单取票密码
	@RequestMapping("/home/qryOrderCheckpass.xhtml")
	public String qryOrderCheckpass(Long orderid, ModelMap model){
		Member member = getLogonMember();
		Long memberid = member.getId();
		GewaOrder order = daoService.getObject(GewaOrder.class, orderid);
		if(order == null) return showJsonError(model, "订单不存在！");
		if(!order.getMemberid().equals(memberid)) return showJsonError(model, "你没有权限！");
		if(!StringUtils.equals(order.getStatus(), OrderConstant.STATUS_PAID_SUCCESS)) return showJsonError(model, "该订单不是成功的订单！");
		String result = "";
		if(!order.isPaidSuccess()) return showJsonError(model, "未成功的订单！");
		if(order instanceof TicketOrder){
			OpenPlayItem opi = daoService.getObjectByUkey(OpenPlayItem.class, "mpid", ((TicketOrder)order).getMpid(), true);
			String msgTemplate = messageService.getCheckpassTemplate(opi);
			List<SellSeat> seatList = ticketOrderService.getOrderSeatList(order.getId());
			ErrorCode<String> msg = messageService.getCheckpassMsg(msgTemplate, ((TicketOrder)order), seatList, opi);
			result = msg.getRetval();
			List<BuyItem> itemList = daoService.getObjectListByField(BuyItem.class, "orderid", order.getId());
			if(itemList.size() >0 ) {
				for(BuyItem item : itemList){
					result = result + "," + item.getGoodsname()+"密码：" + item.getCheckpass();
				}
			}
		}else if(order instanceof GoodsOrder){
			result = order.getCheckpass();
		}else if(order instanceof SportOrder){
			result = "无需密码";
		}else if(order instanceof DramaOrder){
			List<OrderNote> noteList = daoService.getObjectListByField(OrderNote.class, "orderid", order.getId());
			model.put("noteList", noteList);
			Map<Long, OpenDramaItem> odiMap = new HashMap<Long, OpenDramaItem>();
			for (OrderNote orderNote : noteList) {
				if(StringUtils.equals(orderNote.getSmallitemtype(), BuyItemConstant.TAG_DRAMAPLAYITEM)){
					OpenDramaItem odi = daoService.getObjectByUkey(OpenDramaItem.class, "dpid", orderNote.getSmallitemid(), true);
					odiMap.put(orderNote.getSmallitemid(), odi);
				}
			}
			model.put("odiMap", odiMap);
		}
		model.put("result", result);
		model.put("order", order);
		model.put("member", member);
		return "include/queryOrderPass.vm";
	}
	
	@RequestMapping("/home/repeatSendOrderSMS.xhtml")
	public String sendOrderSMS(Long orderid, ModelMap model){
		Member member = getLogonMember();
		if(member == null) return showJsonError_NOT_LOGIN(model);
		Long memberid = member.getId();
		GewaOrder order = daoService.getObject(GewaOrder.class, orderid);
		if(order == null) return showJsonError(model, "订单不存在！");
		if(!order.getMemberid().equals(memberid)) return showJsonError(model, "你没有权限！");
		if(!StringUtils.equals(order.getStatus(), OrderConstant.STATUS_PAID_SUCCESS)) return showJsonError(model, "该订单不是成功的订单！");
		if(!order.isPaidSuccess()) return showJsonError(model, "未成功的订单！");
		
		String opkey = OperationService.TAG_SENDTICKETPWD + member.getId() + order.getId();
		if(!operationService.isAllowOperation(opkey, OperationService.ONE_DAY * 3, 3)){
			return showJsonError(model, "同一订单最多只能发送3次！");
		}
		if(order instanceof TicketOrder){
			OpenPlayItem opi = daoService.getObjectByUkey(OpenPlayItem.class, "mpid", ((TicketOrder)order).getMpid(), true);
			String msgTemplate = messageService.getCheckpassTemplate(opi);
			List<SellSeat> seatList = ticketOrderService.getOrderSeatList(order.getId());
			ErrorCode<String> msg = messageService.getCheckpassMsg(msgTemplate, ((TicketOrder)order), seatList, opi);
			if(msg.isSuccess()){
				String result = msg.getRetval();
				SMSRecord sms = messageService.addManualMsg(order.getMemberid(), order.getMobile(), result, null);
				if(sms != null){
					ErrorCode code = untransService.sendMsgAtServer(sms, true);
					operationService.updateOperation(opkey, OperationService.ONE_DAY * 3, 3);
					String resultMsg = "你的取票短信已发送成功，若15分钟内没有收到短信，请联系客服：4000-406-506";
					if(code.isSuccess()) return showJsonSuccess(model, resultMsg);
					return showJsonError(model, code.getMsg());
				}
			}
		}
		return showJsonError_DATAERROR(model);
	}
	
	@RequestMapping("/home/order/repeatSendOrderSMS.xhtml")
	public String repeatSendOrderSMS(Long noteid, ModelMap model){
		Member member = getLogonMember();
		if(member == null) return showJsonError_NOT_LOGIN(model);
		Long memberid = member.getId();
		OrderNote orderNote = daoService.getObject(OrderNote.class, noteid);
		if(orderNote == null) return showJsonError(model, "数据错误！");
		GewaOrder order = daoService.getObject(GewaOrder.class, orderNote.getOrderid());
		if(!order.getMemberid().equals(memberid)) return showJsonError(model, "你没有权限！");
		if(!StringUtils.equals(order.getStatus(), OrderConstant.STATUS_PAID_SUCCESS)) return showJsonError(model, "该订单不是成功的订单！");
		if(!order.isPaidSuccess()) return showJsonError(model, "未成功的订单！");
		
		String opkey = OperationService.TAG_SENDTICKETPWD + member.getId() + order.getId() + orderNote.getId();
		if(!operationService.isAllowOperation(opkey, OperationService.ONE_DAY * 3, 3)){
			return showJsonError(model, "同一数据最多只能发送3次！");
		}
		Timestamp cur = DateUtil.getCurFullTimestamp();
		if(order instanceof DramaOrder){
			SMSRecord sms = messageService.addOrderNoteSms((DramaOrder)order, orderNote, cur);
			if(sms != null){
				ErrorCode code = untransService.sendMsgAtServer(sms, true);
				operationService.updateOperation(opkey, OperationService.ONE_DAY * 3, 3);
				String resultMsg = "你的取票短信已发送成功，若15分钟内没有收到短信，请联系客服：4000-406-506";
				if(code.isSuccess()) return showJsonSuccess(model, resultMsg);
				return showJsonError(model, code.getMsg());
			}
		}
		return showJsonError_DATAERROR(model);
	}
	
	@RequestMapping("/home/removeMyOrder.xhtml")
	public String removeOrder(Long orderid, ModelMap model){
		Member member = getLogonMember();
		GewaOrder order = daoService.getObject(GewaOrder.class, orderid);
		if(order == null) return showJsonError(model, "订单不存在！");
		if(!order.getMemberid().equals(member.getId())) return showJsonError(model, "你没有权限！");
		order.setRestatus(GewaOrder.RESTATUS_DELETE);
		daoService.saveObject(order);
		return showJsonSuccess(model);
	}
}