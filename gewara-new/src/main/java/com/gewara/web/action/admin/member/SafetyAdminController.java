package com.gewara.web.action.admin.member;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.gewara.constant.BindConstant;
import com.gewara.constant.DiaryConstant;
import com.gewara.constant.MemberConstant;
import com.gewara.constant.SmsConstant;
import com.gewara.model.acl.User;
import com.gewara.model.bbs.Diary;
import com.gewara.model.pay.Charge;
import com.gewara.model.pay.ElecCard;
import com.gewara.model.pay.GewaOrder;
import com.gewara.model.pay.MemberAccount;
import com.gewara.model.pay.SMSRecord;
import com.gewara.model.user.BindMobile;
import com.gewara.model.user.Member;
import com.gewara.model.user.MemberInfo;
import com.gewara.pay.PayUtil;
import com.gewara.service.OperationService;
import com.gewara.service.bbs.DiaryService;
import com.gewara.service.gewapay.ElecCardService;
import com.gewara.service.gewapay.PaymentService;
import com.gewara.service.member.BindMobileService;
import com.gewara.service.order.OrderQueryService;
import com.gewara.support.ErrorCode;
import com.gewara.untrans.CommentService;
import com.gewara.untrans.GewaMailService;
import com.gewara.untrans.UntransService;
import com.gewara.util.BeanUtil;
import com.gewara.util.ChangeEntry;
import com.gewara.util.DateUtil;
import com.gewara.util.StringUtil;
import com.gewara.util.ValidateUtil;
import com.gewara.util.VmUtils;
import com.gewara.util.WebUtils;
import com.gewara.web.action.admin.BaseAdminController;
import com.gewara.xmlbind.bbs.Comment;

@Controller
public class SafetyAdminController extends BaseAdminController {
	@Autowired
	@Qualifier("orderQueryService")
	private OrderQueryService orderQueryService;
	@Autowired
	@Qualifier("diaryService")
	private DiaryService diaryService;
	@Autowired
	@Qualifier("commentService")
	private CommentService commentService;
	@Autowired
	@Qualifier("bindMobileService")
	private BindMobileService bindMobileService;
	@Autowired
	@Qualifier("untransService")
	private UntransService untransService;
	@Autowired
	@Qualifier("gewaMailService")
	private GewaMailService gewaMailService;
	@Autowired
	@Qualifier("paymentService")
	private PaymentService paymentService;
	@Autowired
	@Qualifier("elecCardService")
	private ElecCardService elecCardService;

	@RequestMapping("/admin/safety/goGetPayPass.xhtml")
	public String goGetPayPass() {
		return "admin/safety/getPayPass.vm";
	}

	@RequestMapping("/admin/safety/goGetPass.xhtml")
	public String goGetPass() {
		return "admin/safety/getPass.vm";
	}

	@RequestMapping("/admin/safety/goChangeMobile.xhtml")
	public String goChangeMobile() {
		return "admin/safety/changeMobile.vm";
	}
	
	@RequestMapping("/admin/sns/getMemberInfo.xhtml")
	public String getMemberInfo(Long memberid, ModelMap model) {
		String view = "admin/sysmgr/memberInfo.vm";
		if (memberid == null)
			return view;
		Member member = daoService.getObject(Member.class, memberid);
		if (member == null){
			model.put("msg", "不存在此用户！");
		}else {
			MemberInfo memberInfo = daoService.getObject(MemberInfo.class, memberid);
			model.put("memberInfo", memberInfo);
			MemberAccount memberAccount = daoService.getObjectByUkey(MemberAccount.class, "memberid", member.getId(), false);
			Map accountMap = null;
			if (memberAccount != null) {
				if (StringUtils.isNotBlank(memberAccount.getEncryidcard())) {
					String encryidcard = paymentService.getDecryptIdcard(memberAccount.getEncryidcard());
					memberAccount.setEncryidcard(encryidcard);
				}
				accountMap = BeanUtil
						.getBeanMapWithKey(memberAccount, "realname", "certtype", "encryidcard", "emcontact", "emmobile","othercharge","bankcharge");
			}
			loadAuth(model, accountMap, member);
		}
		model.put("member", member);
		return view;
	}

	@RequestMapping("/admin/safety/getPayPsss.xhtml")
	public String getPayPass(ModelMap model, String mobile, String email) {
		Member member = null;
		if (StringUtils.isNotBlank(mobile)) {
			if (!ValidateUtil.isMobile(mobile))
				return showJsonError(model, "手机号码格式不正确！");
			member = daoService.getObjectByUkey(Member.class, "mobile", mobile, false);
		} else if (StringUtils.isNotBlank(email)) {
			if (!ValidateUtil.isEmail(email))
				return showJsonError(model, "邮箱格式不正确！");
			member = daoService.getObjectByUkey(Member.class, "email", email, false);
		}
		if (member != null) {
			MemberAccount memberAccount = daoService.getObjectByUkey(MemberAccount.class, "memberid", member.getId(), false);
			if (memberAccount == null)
				return showJsonError(model, "该用户尚未设置支付账号！请您仔细确认用户身份！");
			if (StringUtils.isNotBlank(memberAccount.getEncryidcard())) {
				String encryidcard = paymentService.getDecryptIdcard(memberAccount.getEncryidcard());
				memberAccount.setEncryidcard(encryidcard);
			}
			Map accountMap = BeanUtil
					.getBeanMapWithKey(memberAccount, "realname", "certtype", "encryidcard", "emcontact", "emmobile","othercharge","bankcharge");
			loadAuth(model, accountMap, member);
			model.put("memberId", member.getId());
		} else {
			return showJsonError(model, "您所查询用户不存在！");
		}
		return "admin/safety/authContent.vm";
	}

	@RequestMapping("/admin/safety/resetPayPass.xhtml")
	public String resetPayPass(ModelMap model, @RequestParam(value = "memberId", required = true) Long memberId) {
		User user = getLogonUser();
		MemberAccount account = daoService.getObjectByUkey(MemberAccount.class, "memberid", memberId, false);
		if (account == null)
			return showJsonError(model, "该用户尚未设置支付账号！请您仔细确认用户身份！");
		ChangeEntry changeEntry = new ChangeEntry(account);
		account.setPassword(PayUtil.getPass("123456"));
		daoService.saveObject(account);
		monitorService.saveChangeLog(user.getId(), MemberAccount.class, account.getId(), changeEntry.getChangeMap(account));
		return showJsonSuccess(model, "成功重置支付密码，请提醒用户及时设置新支付密码！");
	}
	
	@RequestMapping("/admin/safety/resetSendCount.xhtml")
	public String resetSendCount(ModelMap model, @RequestParam(value = "mobile", required = true) String mobile) {
		User user = getLogonUser();
		List<BindMobile> bindMobileList = daoService.getObjectListByField(BindMobile.class, "mobile", mobile);
		if (VmUtils.isEmptyList(bindMobileList))
			return showJsonError(model, "该用户尚未有短信发送记录！请您仔细确认用户身份！");
		ChangeEntry changeEntry = new ChangeEntry(bindMobileList.get(0));
		for (BindMobile bindMobile : bindMobileList) {
			bindMobile.setSendcount(0);
		}
		
		daoService.saveObjectList(bindMobileList);
		monitorService.saveChangeLog(user.getId(), BindMobile.class, mobile, changeEntry.getChangeMap(bindMobileList.get(0)));
		return showJsonSuccess(model);
	}
	
	@RequestMapping("/admin/safety/resetMobileOperation.xhtml")
	public String resetMobileOperation(String mobileOrIp, ModelMap model){
		if(StringUtils.isBlank(mobileOrIp)) return showJsonError(model, "手机号码不正确！");
		operationService.resetOperation(BindConstant.TAG_REGISTERCODE+mobileOrIp, OperationService.ONE_DAY);
		operationService.resetOperation(BindConstant.TAG_BINDMOBILE+mobileOrIp, OperationService.ONE_DAY);
		operationService.resetOperation(BindConstant.TAG_ACCOUNT_BACKPASS+mobileOrIp, OperationService.ONE_DAY);
		operationService.resetOperation(BindConstant.TAG_MODIFYPASS+mobileOrIp, OperationService.ONE_DAY);
		operationService.resetOperation(BindConstant.TAG_CHGBINDMOBILE+mobileOrIp, OperationService.ONE_DAY);
		operationService.resetOperation(BindConstant.TAG_MDYPAYPASS+mobileOrIp, OperationService.ONE_DAY);
		operationService.resetOperation(BindConstant.TAG_SETPAYPASS+mobileOrIp, OperationService.ONE_DAY);
		operationService.resetOperation(BindConstant.TAG_GETPAYPASS+mobileOrIp, OperationService.ONE_DAY);
		return showJsonSuccess(model);
	}

	@RequestMapping("/admin/safety/getPass.xhtml")
	public String getPass(ModelMap model, String email) {
		if (!ValidateUtil.isEmail(email))
			return showJsonError(model, "邮箱格式不正确！");
		Member member = daoService.getObjectByUkey(Member.class, "email", email, false);

		if (member != null) {
			if (member.isBindMobile())
				return showJsonError(model, "该用户已绑定手机！请在切换手机号码菜单修改绑定！");
			MemberAccount memberAccount = daoService.getObjectByUkey(MemberAccount.class, "memberid", member.getId(), false);
			Map accountMap = null;
			if (memberAccount != null) {
				if (StringUtils.isNotBlank(memberAccount.getEncryidcard())) {
					String encryidcard = paymentService.getDecryptIdcard(memberAccount.getEncryidcard());
					memberAccount.setEncryidcard(encryidcard);
				}
				accountMap = BeanUtil
						.getBeanMapWithKey(memberAccount, "realname", "certtype", "encryidcard", "emcontact", "emmobile","othercharge","bankcharge");
			}
			loadAuth(model, accountMap, member);
		} else {
			return showJsonError(model, "您所查询用户不存在！");
		}
		return "admin/safety/authContent.vm";
	}

	@RequestMapping("/admin/safety/sendBindMobile.xhtml")
	public String sendBindMobile(ModelMap model, String email, String mobile, String checkpass, HttpServletRequest request) {
		if (StringUtils.isBlank(checkpass))
			return showJsonError(model, "手机动态码不能为空！");
		if (!ValidateUtil.isMobile(mobile))
			return showJsonError(model, "手机号码格式不正确！");
		if (!ValidateUtil.isEmail(email))
			return showJsonError(model, "邮箱格式不正确！");
		User user = getLogonUser();
		Member member = daoService.getObjectByUkey(Member.class, "email", email, false);
		if (member != null) {
			if (member.isBindMobile()){
				return showJsonError(model, "该用户已绑定手机！请在切换手机号码菜单修改绑定！");
			}
			ErrorCode code = memberService.bindMobile(member, mobile, checkpass, user, WebUtils.getRemoteIp(request));
			if (!code.isSuccess()){
				return showJsonError(model, code.getMsg());
			}
		} else {
			return showJsonError(model, "您所指定的用户不存在！");
		}
		return showJsonSuccess(model, "绑定成功！");
	}

	@RequestMapping("/admin/safety/changeMobile.xhtml")
	public String changeMobile(ModelMap model, String oldmobile) {
		if (!ValidateUtil.isMobile(oldmobile))
			return showJsonError(model, "手机号码格式不正确！");
		Member member = daoService.getObjectByUkey(Member.class, "mobile", oldmobile, false);
		if (member != null) {
			MemberAccount memberAccount = daoService.getObjectByUkey(MemberAccount.class, "memberid", member.getId(), false);
			Map accountMap = null;
			if (memberAccount != null) {
				if (StringUtils.isNotBlank(memberAccount.getEncryidcard())) {
					String encryidcard = paymentService.getDecryptIdcard(memberAccount.getEncryidcard());
					memberAccount.setEncryidcard(encryidcard);
				}
				accountMap = BeanUtil
						.getBeanMapWithKey(memberAccount, "realname", "certtype", "encryidcard", "emcontact", "emmobile","othercharge","bankcharge");
			}
			loadAuth(model, accountMap, member);
		} else {
			return showJsonError(model, "您所查询用户不存在！");
		}
		return "admin/safety/authContent.vm";
	}
	
	@RequestMapping("/admin/safety/sendChangeMobile.xhtml")
	public String sendChangeMobile(ModelMap model, String oldmobile, String mobile, String checkpass, HttpServletRequest request) {
		if (StringUtils.isBlank(checkpass))
			return showJsonError(model, "手机动态码不能为空！");
		if (!ValidateUtil.isMobile(mobile))
			return showJsonError(model, "新手机号码格式不正确！");
		if (!ValidateUtil.isMobile(oldmobile))
			return showJsonError(model, "旧手机号码格式不正确！");
		boolean exists = memberService.isMemberMobileExists(mobile);
		if (exists)
			return showJsonError(model, "新手机号码已被其他帐号绑定，请更换号码！");
		User user = getLogonUser();
		Member member = daoService.getObjectByUkey(Member.class, "mobile", oldmobile, false);
		if (member != null) {
			ErrorCode code = memberService.bindMobile(member, mobile, checkpass, user, WebUtils.getRemoteIp(request));
			if (!code.isSuccess()){
				return showJsonError(model, code.getMsg());
			}
			sendWarning("手机号", member, member.getMobile(), member.getEmail());
		} else {
			return showJsonError(model, "您所指定的用户不存在！");
		}
		return showJsonSuccess(model, "切换绑定成功！");
	}

	private void loadAuth(ModelMap model, Map accountMap, Member member) {
		List<GewaOrder> orderList = orderQueryService.getOrderListByMemberId(member.getId(), 0, 0, 3);
		List<Diary> bbsDiaryList = diaryService.getDiaryListByMemberid(Diary.class, DiaryConstant.DIARY_TYPE_TOPIC_DIARY, null, member.getId(), 0, 3);
		List<Diary> movieDiaryList = diaryService.getDiaryListByMemberid(Diary.class, DiaryConstant.DIARY_TYPE_COMMENT, null, member.getId(), 0, 3);
		List<Comment> commentList = commentService.getCommentListByMemberid(member.getId(), 0, 3);
		List<ElecCard> cardList = elecCardService.getCardListByMemberid(member.getId(), null, 0, 50);
		List<Charge> chargeList = paymentService.getChargeListByMemberId(member.getId(), null, null, 0, 50);
		if (accountMap != null)
			model.put("account", accountMap);
		model.put("orderlist", orderList);
		model.put("bbslist", bbsDiaryList);
		model.put("mdiarylist", movieDiaryList);
		model.put("cardList", cardList);
		model.put("chargeList", chargeList);
		model.put("commentlist", commentList);
	}

	@RequestMapping("/admin/safety/loadCheckPass.xhtml")
	public String loadCheckPass(HttpServletRequest request, ModelMap model, String mobile) {
		if (!ValidateUtil.isMobile(mobile)){
			return showJsonError(model, "手机号码格式不正确！");
		}
		boolean exists = memberService.isMemberMobileExists(mobile);
		if (exists){
			return showJsonError(model, "该号码已被其他帐号绑定，请更换号码！");
		}
		ErrorCode<SMSRecord> code = bindMobileService.refreshBMByAdmin(BindConstant.TAG_BINDMOBILE, mobile, WebUtils.getRemoteIp(request),BindConstant.getMsgTemplate(BindConstant.TAG_BINDMOBILE));
		if (!code.isSuccess()){
			return showJsonError(model, code.getMsg());
		}
		untransService.sendMsgAtServer(code.getRetval(), false);
		return showJsonSuccess(model, "成功发送动态码！");
	}
	
	@RequestMapping("/admin/safety/encryValidate.xhtml")
	public String encryValidate(ModelMap model){
		List<MemberAccount> accountList = paymentService.encryAccounts();
		List<Map<String,String>> mapList = new ArrayList<Map<String,String>>();
		for (int i = 0; i < accountList.size(); i++) {
			MemberAccount account = accountList.get(i);
			Map<String, String> map = new HashMap<String, String>();
			map.put("idcard", account.getIdcard());
//			map.put("decry", account.getEncryidcard()));
			map.put("encryold", account.getEncryidcard());
			String encrynewString = paymentService.getEncryptIdcard(StringUtil.ToDBC(account.getIdcard()));
			map.put("encrynew", encrynewString);
			map.put("decry", paymentService.getDecryptIdcard(encrynewString));
			mapList.add(map);
		}
		model.put("total", paymentService.anlyEncryAccounts());
		model.put("list", mapList);
		return "admin/safety/encryValidate.vm";
	}
	
/*	@RequestMapping("/admin/safety/updateOfficialAccount.xhtml")
	public String updateOfficialAccount( ModelMap model) {
		String OAEmlString = "huodong021@gewara.com,huodong0571@geawra.com,huodong023@gewara.com,huodong027@gewara.com,huodong028@gewara.com,huodong025@gewara.com,huodong020@gewara.com,huodong010@gewara.com,pianfang@gewara.com,huodong0755@gewara.com,bbs@gewara.com,000000@163.com,xingkai.gao@gewara.com,yanchu@gewara.com,000000@163.com,xiaobianwala@sina.com,gewaracandy@qq.com,700@gamil.com,gewaralxt@126.com,gewara22@163.com,363778129@qq.com,duanqianqian@126.com,1313@163.com,479735514@qq.com,weiliusi1112@126.com,sj.xing@gewara.com,yy8023yu@126.com,116758423@qq.com,bianji@gewara.com";
		String[] OAEmlAry = OAEmlString.split(",");
		String validateAccount = "成功更改以下账号：";
		String invalidateAccount = "以下账号更改失败：";
		for (String email : OAEmlAry) {
			if (ValidateUtil.isEmail(email)) {
				Member mbr = daoService.getObjectByUkey(Member.class, "email", email);
				if (mbr == null) {
					invalidateAccount += email + ",";
				}else {
					MemberInfo mbi = daoService.getObject(MemberInfo.class, mbr.getId());
					mbi.finishTask(MemberConstant.TASK_CONFIRMREG);
					mbi.finishTaskOtherInfo(MemberConstant.TASK_CONFIRMREG);
					daoService.saveObject(mbi);
					validateAccount += email + ",";
				}
			}else {
				invalidateAccount += email + ",";
			}
		}
		return showJsonSuccess(model , validateAccount +"<br/>"+ invalidateAccount);
	}*/
	
	private void sendWarning(String msg, Member member, String... mobiles) {
		if (ArrayUtils.isEmpty(mobiles))
			return;
		Timestamp cur = DateUtil.getCurFullTimestamp();
		for (String mobile : mobiles) {
			if (ValidateUtil.isMobile(mobile)) {
				String tmp = "你于" + DateUtil.getCurTimeStr() + " 在格瓦拉生活网设置了绑定" + msg + "，如果是你本人操作，请不必理会此短信！";
				SMSRecord sms = new SMSRecord(MemberConstant.ACTION_BINDMOBILE + "_" + member.getId(), mobile, tmp, cur, DateUtil.addHour(cur, 2),
						SmsConstant.SMSTYPE_NOW);
				untransService.sendMsgAtServer(sms, false);
			} else if (ValidateUtil.isEmail(mobile)) {
				String tmp = "你于" + DateUtil.formatTime(cur) + "在格瓦拉生活网设置了绑定" + msg + "，如果是你本人操作，请不必理会此邮件！";
				gewaMailService.sendAdviseEmail(member.getNickname(), tmp, mobile);
			}
		}
	}
}
