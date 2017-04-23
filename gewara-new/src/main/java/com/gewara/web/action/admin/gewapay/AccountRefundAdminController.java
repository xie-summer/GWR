package com.gewara.web.action.admin.gewapay;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import com.gewara.constant.PaymethodConstant;
import com.gewara.constant.Status;
import com.gewara.constant.sys.JsonDataKey;
import com.gewara.constant.ticket.RefundConstant;
import com.gewara.model.acl.User;
import com.gewara.model.common.JsonData;
import com.gewara.model.pay.AccountRefund;
import com.gewara.model.pay.BuyItem;
import com.gewara.model.pay.Charge;
import com.gewara.model.pay.Discount;
import com.gewara.model.pay.GewaOrder;
import com.gewara.model.pay.MemberAccount;
import com.gewara.model.pay.OrderRefund;
import com.gewara.model.pay.SMSRecord;
import com.gewara.model.user.Member;
import com.gewara.pay.PayUtil;
import com.gewara.service.gewapay.AccountRefundService;
import com.gewara.service.gewapay.PaymentService;
import com.gewara.support.ErrorCode;
import com.gewara.untrans.UntransService;
import com.gewara.untrans.order.RefundOperationService;
import com.gewara.util.BeanUtil;
import com.gewara.util.BindUtils;
import com.gewara.util.ChangeEntry;
import com.gewara.util.DateUtil;
import com.gewara.util.JsonUtils;
import com.gewara.util.ValidateUtil;
import com.gewara.util.WebUtils;
import com.gewara.web.action.admin.BaseAdminController;

@Controller
public class AccountRefundAdminController extends BaseAdminController{
	@Autowired@Qualifier("accountRefundService")
	private AccountRefundService accountRefundService;
	
	@Autowired@Qualifier("refundOperationService")
	private RefundOperationService refundOperationService;
	
	@Autowired@Qualifier("untransService")
	private UntransService untransService;

	@Autowired@Qualifier("paymentService")
	private PaymentService paymentService;

	@RequestMapping("/admin/refund/account/applyList.xhtml")
	public String applyList(String tradeno, String status, Integer pageNo, Long memberid, String mobile, ModelMap model){
		if(pageNo==null) pageNo = 0;
		int rowsPerpage = 50;
		int firstRow = pageNo * rowsPerpage;
		List<AccountRefund> refundList = accountRefundService.getAccountRefundList(tradeno, status, memberid, mobile, firstRow, rowsPerpage);
		model.put("refundList", refundList);
		List<Long> idList = BeanUtil.getBeanPropertyList(refundList, Long.class, "applyuser", true);
		idList=(List<Long>) CollectionUtils.union(idList, BeanUtil.getBeanPropertyList(refundList, Long.class, "dealuser", true));
		Map<Long, String> usernameMap = daoService.getObjectPropertyMap(User.class, "id", "nickname", idList);
		model.put("usernameMap", usernameMap);
		model.put("paymethodMap", PaymethodConstant.getPayTextMap());
		return "admin/refund/account/applyList.vm";
	}
	@RequestMapping("/admin/refund/account/refundList.xhtml")
	public String ticketRefundList(String tradeno, String status, Integer pageNo, Long memberid, String mobile, ModelMap model){
		if(pageNo==null) pageNo = 0;
		int rowsPerpage = 50;
		int firstRow = pageNo * rowsPerpage;
		List<AccountRefund> refundList = accountRefundService.getAccountRefundList(tradeno, status, memberid, mobile, firstRow, rowsPerpage);
		model.put("refundList", refundList);
		List<Long> idList = BeanUtil.getBeanPropertyList(refundList, Long.class, "applyuser", true);
		idList = (List<Long>) CollectionUtils.union(idList, BeanUtil.getBeanPropertyList(refundList, Long.class, "dealuser", true));
		Map<Long, String> usernameMap = daoService.getObjectPropertyMap(User.class, "id", "nickname", idList);
		model.put("usernameMap", usernameMap);
		model.put("paymethodMap", PaymethodConstant.getPayTextMap());
		model.put("applyCount", accountRefundService.getAccountRefundCount(null, RefundConstant.STATUS_APPLY, null, null));
		model.put("acceptCount", accountRefundService.getAccountRefundCount(null, RefundConstant.STATUS_ACCEPT, null, null));
		return "admin/refund/account/refundList.vm";
	}
	
	@RequestMapping("/admin/refund/account/modifyRefund.xhtml")
	public String modifyRefund(Long rid, ModelMap model){
		AccountRefund refund = daoService.getObject(AccountRefund.class, rid);
		model.put("refund", refund);
		if(refund != null){
			if(!refund.isOutPartner()){
				MemberAccount account = daoService.getObjectByUkey(MemberAccount.class, "memberid", refund.getMemberid(), false);
				model.put("account", account);
			}
			if(StringUtils.isNotBlank(refund.getTradeno())){
				if(PayUtil.isChargeTrade(refund.getTradeno())){
					Charge charge = daoService.getObjectByUkey(Charge.class, "tradeNo", refund.getTradeno(), true);
					model.put("charge", charge);
				}else{
					GewaOrder order = daoService.getObjectByUkey(GewaOrder.class, "tradeNo", refund.getTradeno(), true);
					if(order != null){
						List<Discount> discountList = paymentService.getOrderDiscountList(order);
						List<BuyItem> itemList = daoService.getObjectListByField(BuyItem.class, "orderid", order.getId());
						model.put("itemList", itemList);
						model.put("discountList", discountList);
						model.put("order", order);
					}
				}
			}
		}
		model.put("paymethodMap", PaymethodConstant.getPayTextMap());
		return "admin/refund/account/modifyRefund.vm";
	}
	
	@RequestMapping("/admin/refund/account/saveRefund.xhtml")
	public String saveRefund(Long rid, String mobile, Long memberid,HttpServletRequest request, ModelMap model){
		AccountRefund refund = null;
		if(rid!=null) refund = daoService.getObject(AccountRefund.class, rid);
		if(refund != null){
			if(!AccountRefund.STATUS_APPLY.equals(refund.getStatus())){
				return showJsonError(model, "状态不正确，不能修改！");
			}
		}else{
			refund = new AccountRefund(memberid, mobile);
			refund.setOrigin("apply");
		}
		User user = getLogonUser();
		ChangeEntry entry = new ChangeEntry(refund);
		Map<String,String> dataMap = WebUtils.getRequestMap(request);
		BindUtils.bindData(refund, dataMap);
		if(StringUtils.isBlank(refund.getRemark())) return showJsonError(model, "特别说明不能为空！");
		if(refund.getMemberid() == null) return showJsonError(model, "用户ID不能为空！");
		if(refund.getAmount() == null) return showJsonError(model, "退款金额错误！");
		if(!ValidateUtil.isMobile(mobile)) return showJsonError(model, "手机号错误！");
		if(StringUtils.isBlank(refund.getPaymethod())) return showJsonError(model, "支付方式不能为空！");
		if(!PaymethodConstant.isValidPayMethod(refund.getPaymethod())) return showJsonError(model, "支付方式错误！");
		Member member = daoService.getObject(Member.class, refund.getMemberid());
		if(member == null) return showJsonError(model, "该用户不存在或被删除！");
		MemberAccount account = daoService.getObjectByUkey(MemberAccount.class, "memberid", refund.getMemberid(), false);
		if(refund.getAmount()>account.getBankcharge()) return showJsonError(model, "退款金额不能大于可退金额：" +account.getBankcharge());
		refund.setApplyuser(user.getId());
		daoService.saveObject(refund);
		monitorService.saveChangeLog(user.getId(), AccountRefund.class, refund.getId(), entry.getChangeMap(refund));
		return showJsonSuccess(model, refund.getId()+"");
	}
	
	@RequestMapping("/admin/refund/account/changeStatus.xhtml")
	public String changeStatus(Long rid, String status, String reson, ModelMap model){
		AccountRefund refund = daoService.getObject(AccountRefund.class, rid);
		OrderRefund orderRefund = daoService.getObjectByUkey(OrderRefund.class, "tradeno", refund.getTradeno(), true);
		Map<String, String> otherinfo = new HashMap<String, String>();
		if(orderRefund != null){
			otherinfo = JsonUtils.readJsonToMap(orderRefund.getOtherinfo()); 
		}
		ChangeEntry entry = new ChangeEntry(refund);
		String msg = "状态转换成功:";
		if(AccountRefund.STATUS_FAIL.equals(status)){//转为退款失败
			if(!AccountRefund.STATUS_ACCEPT.equals(refund.getStatus())) return showJsonError(model, "状态错误：只有接受才能转为退款失败！");
			refund.setStatus(status);
			msg +="接受--->退款失败";
			otherinfo.put(RefundConstant.REFUND_FINANCE_STATUS, Status.N);
			otherinfo.put(RefundConstant.REFUND_FINANCE_RESON, reson);
		}else if(AccountRefund.STATUS_ACCEPT.equals(status)){//转为接受
			if(!AccountRefund.STATUS_APPLY.equals(refund.getStatus())) return showJsonError(model, "状态错误：只有申请才能转为接受！");
			refund.setStatus(status);
			msg +="退款申请--->退款接受";
			otherinfo.put(RefundConstant.REFUND_FINANCE_STATUS, Status.Y);
		}else if(AccountRefund.STATUS_UNACCEPT.equals(status)){//转为不接受
			if(!AccountRefund.STATUS_APPLY.equals(refund.getStatus())) return showJsonError(model, "状态错误：只有申请才能转为不接受！");
			refund.setStatus(status);
			msg +="退款申请--->退款不接受";
			otherinfo.put(RefundConstant.REFUND_FINANCE_STATUS, Status.N);
			otherinfo.put(RefundConstant.REFUND_FINANCE_RESON, reson);
		}else if(AccountRefund.STATUS_DEBIT.equals(status)){//转为扣款成功
			if(!AccountRefund.STATUS_ACCEPT.equals(refund.getStatus())) return showJsonError(model, "状态错误：只有接受才能转为扣款成功！");
			refund.setStatus(status);
			msg +="接受--->扣款成功";
			otherinfo.put(RefundConstant.REFUND_FINANCE_STATUS, Status.Y);
		}else if(AccountRefund.STATUS_SUCCESS.equals(status)){
			if(!AccountRefund.STATUS_DEBIT.equals(refund.getStatus())) return showJsonError(model, "状态错误：只有扣款成功才能转为银行成功！");
			refund.setStatus(status);
			msg +="扣款成功--->银行成功";
			otherinfo.put(RefundConstant.REFUND_FINANCE_STATUS, Status.Y);
		}else{
			return showJsonError(model, "状态错误！");
		}
		User user = getLogonUser();
		refund.setDealuser(user.getId());
		refund.setDealtime(DateUtil.getCurFullTimestamp());
		daoService.saveObject(refund);
		monitorService.saveChangeLog(user.getId(), AccountRefund.class, refund.getId(), entry.getChangeMap(refund));
		
		if(orderRefund != null){
			otherinfo.put(RefundConstant.REFUND_FINANCE_DEAL, user.getId() +","+ user.getUsername());
			orderRefund.setOtherinfo(JsonUtils.writeMapToJson(otherinfo));
			daoService.saveObject(orderRefund);
		}
		if(StringUtils.equals(refund.getStatus(),AccountRefund.STATUS_SUCCESS)){
			SMSRecord sms = refundOperationService.bankTemplateMsg(getLogonUser().getId(), refund);
			if(sms != null){
				untransService.sendMsgAtServer(sms, false);
			}
		}
		return showJsonSuccess(model, msg);
	}
	
	@RequestMapping("/admin/refund/account/qryAccountRefund.xhtml")
	public String qryAccountRefund(Long rid, ModelMap model){
		AccountRefund refund = daoService.getObject(AccountRefund.class, rid);
		model.put("refund", refund);
		if(StringUtils.isNotBlank(refund.getTradeno())){
			if(PayUtil.isChargeTrade(refund.getTradeno())){
				Charge charge = daoService.getObjectByUkey(Charge.class, "tradeNo", refund.getTradeno(), true);
				model.put("charge", charge);
			}else{
				GewaOrder order = daoService.getObjectByUkey(GewaOrder.class, "tradeNo", refund.getTradeno(), true);
				if(order != null){
					List<Discount> discountList = paymentService.getOrderDiscountList(order);
					List<BuyItem> itemList = daoService.getObjectListByField(BuyItem.class, "orderid", order.getId());
					model.put("itemList", itemList);
					model.put("discountList", discountList);
					model.put("order", order);
				}
			}
		}
		if(!refund.isOutPartner()){
			Member member = daoService.getObject(Member.class, refund.getMemberid());
			MemberAccount account = daoService.getObjectByUkey(MemberAccount.class, "memberid", member.getId(), false);
			model.put("member", member);
			model.put("account", account);
		}
		model.put("paymethodText", PaymethodConstant.getPaymethodText(refund.getPaymethod()));
		return "admin/refund/account/qryAccountRefund.vm";
	}
	
	@RequestMapping("/admin/refund/account/deductAccount.xhtml")
	public String deductAccount(Long rid, ModelMap model){
		User user = getLogonUser();
		AccountRefund refund = daoService.getObject(AccountRefund.class, rid);
		if(refund == null) return showJsonError(model, "退款信息不存在！");
		ErrorCode code = accountRefundService.deductAccountRefund(refund, user.getId());
		if(!code.isSuccess()) return showJsonError(model, code.getMsg());
		OrderRefund orderRefund = daoService.getObjectByUkey(OrderRefund.class, "tradeno", refund.getTradeno(), true);
		Map otherinfo = JsonUtils.readJsonToMap(orderRefund.getOtherinfo()); 
		if(orderRefund != null){
			otherinfo.put(RefundConstant.REFUND_FINANCE_STATUS, Status.Y);
			otherinfo.put(RefundConstant.REFUND_FINANCE_DEAL, user.getId() +","+ user.getUsername());
			orderRefund.setOtherinfo(JsonUtils.writeMapToJson(otherinfo));
			daoService.saveObject(orderRefund);
		}
		return showJsonSuccess(model);
	}
	
	
	@RequestMapping("/admin/refund/account/refundMessage.xhtml")
	public String refundMessage(ModelMap model){
		JsonData accountMessage = daoService.getObject(JsonData.class, JsonDataKey.KEY_REFUNDACCOUNT);
		model.put("accountMessage", accountMessage);
		JsonData bankMessage = daoService.getObject(JsonData.class, JsonDataKey.KEY_REFUNDBANK);
		model.put("bankMessage", bankMessage);
		return "admin/refund/account/refundMessage.vm";
	}
	
	@RequestMapping("/admin/refund/account/getMessage.xhtml")
	public String getMessage(String dkey, ModelMap model){
		JsonData template = daoService.getObject(JsonData.class, dkey);
		model.put("template", template);
		model.put("dkey", dkey);
		return "admin/refund/account/templateMessage.vm";
	}
	
	@RequestMapping("/admin/refund/account/saveMessage.xhtml")
	public String saveMessage(String dkey, String notifymsg, ModelMap model){
		if(!(StringUtils.equals(dkey, JsonDataKey.KEY_REFUNDACCOUNT) 
			|| StringUtils.equals(dkey, JsonDataKey.KEY_REFUNDBANK))){
			return showJsonError(model, "短信模板错误！");
		}
		if(StringUtils.isBlank(notifymsg)){
			return showJsonError(model, "短信内容不能为空！");
		}
		JsonData template = daoService.getObject(JsonData.class, dkey);
		if(template == null){
			template = new JsonData(dkey);
		}
		ChangeEntry changeEntry = new ChangeEntry(template);
		Map<String, String> dataMap = new HashMap<String, String>();
		if(StringUtils.isNotBlank(notifymsg)) dataMap.put("notifymsg", notifymsg.trim());
		template.setValidtime(DateUtil.addDay(DateUtil.getCurFullTimestamp(), 365));
		template.setData(JsonUtils.writeMapToJson(dataMap));
		daoService.saveObject(template);
		monitorService.saveChangeLog(getLogonUser().getId(), JsonData.class, template.getDkey(), changeEntry.getChangeMap(template));
		return showJsonSuccess(model);
	}
}
