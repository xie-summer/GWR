package com.gewara.web.action.admin.gewapay;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.criterion.Subqueries;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.gewara.constant.PaymethodConstant;
import com.gewara.constant.Status;
import com.gewara.constant.sys.LogTypeConstant;
import com.gewara.model.acl.User;
import com.gewara.model.pay.Adjustment;
import com.gewara.model.pay.Charge;
import com.gewara.model.pay.GewaOrder;
import com.gewara.model.pay.MemberAccount;
import com.gewara.model.user.Member;
import com.gewara.service.gewapay.ElecCardService;
import com.gewara.service.gewapay.PaymentService;
import com.gewara.support.ErrorCode;
import com.gewara.untrans.monitor.OrderMonitorService;
import com.gewara.untrans.ticket.OrderLogService;
import com.gewara.util.BeanUtil;
import com.gewara.util.ChangeEntry;
import com.gewara.util.DateUtil;
import com.gewara.util.WebUtils;
import com.gewara.web.action.admin.BaseAdminController;
import com.gewara.web.util.PageUtil;
/**
 * 用户账户金额相关
 * @author acerge(acerge@163.com)
 * @since 11:16:37 AM Apr 14, 2011
 */
@Controller
public class MemberAccountAdminController extends BaseAdminController{
	@Autowired@Qualifier("paymentService")
	private PaymentService paymentService;
	public void setPaymentService(PaymentService paymentService) {
		this.paymentService = paymentService;
	}
	@Autowired@Qualifier("orderMonitorService")
	private OrderMonitorService orderMonitorService;
	@Autowired@Qualifier("orderLogService")
	private OrderLogService orderLogService;
	
	@Autowired@Qualifier("elecCardService")
	private ElecCardService elecCardService;
	
	@RequestMapping(value="/admin/account/batchAddAccountAmountWithElecCard.xhtml", method=RequestMethod.GET)
	public String bathAddAccmountAmountWithElecCard(){
		return "admin/gewapay/batchAddAccountAmountWithElecCard.vm";
	}
	@RequestMapping(value="/admin/account/batchAddAccountAmountWithElecCard.xhtml", method=RequestMethod.POST)
	public String bathAddAccmountAmountWithElecCard(HttpServletRequest request, String memberIds, String cardPasses, ModelMap model){
		User user = getLogonUser();
		if (user == null)
			return showJsonError(model, "非法操作！！！");
		String msg = "";
		try {
			if (StringUtils.isBlank(memberIds) || StringUtils.isBlank(cardPasses))
				return showJsonError(model, "请输入member ids and cardPass");
			String[] members = memberIds.split(",");
			String[] cardPa = cardPasses.split(",");
			Set<Long> mems = new HashSet<Long>();
			Set<String> cards = new HashSet<String>();
			
			for (int i = 0 ; i < members.length; i++) mems.add(Long.valueOf(StringUtils.trimToEmpty(members[i])));
			for (int i = 0 ; i < cardPa.length; i++) cards.add(StringUtils.trimToEmpty(cardPa[i]));
			
			if (mems.size() != cards.size()){
				return showJsonError(model, "用户与卡号数量不一致！！");
			}
			List<Member> memberList = daoService.getObjectList(Member.class, mems);
			if (memberList.size() != mems.size()){
				return showJsonError(model, "共输入:" + mems.size() + "用户ID，" + "其中只有：" + memberList.size() + " 个有效");
			}
			msg = elecCardService.batchAddAmountWithElecCard(mems, cards, user.getId(), WebUtils.getRemoteIp(request)).getMsg();
		} catch (Exception e) {
			dbLogger.error("erro:", e);
		}
		dbLogger.warn("批量充值完成：：" + msg);
		return showJsonSuccess(model ,msg);
	}
	
	@RequestMapping("/admin/account/adjustmentList.xhtml")
	public String adjustmentList(String status, Integer pageNo, Integer count, ModelMap model){
		if(StringUtils.isBlank(status)) status = Adjustment.STATUS_NEW;

		if(pageNo==null) pageNo = 0;
		int from = pageNo * 40;
		if(count==null) count = paymentService.getAdjustmentCount(status);
		PageUtil pageUtil = new PageUtil(count, 40, pageNo, "admin/account/adjustmentList.xhtml");
		Map params = new HashMap();
		if(StringUtils.isNotBlank(status)) params.put("status", status);
		params.put("count", count);
		pageUtil.initPageInfo(params);
		model.put("pageUtil", pageUtil);

		List<Adjustment> adjustmentList = paymentService.getAdjustmentList(status, from, 40);
		List<Long> accountidList = BeanUtil.getBeanPropertyList(adjustmentList, Long.class, "accountid", true);
		Map<Long, MemberAccount> accountMap = daoService.getObjectMap(MemberAccount.class, accountidList);
		model.put("adjustmentList", adjustmentList);
		model.put("accountMap", accountMap);
		return "admin/gewapay/adjustmentList.vm";
	}
	@RequestMapping("/admin/account/ajax/approveAdjustment.xhtml")
	public String approveAdjustment(Long aid, ModelMap model){
		User user = getLogonUser();
		Adjustment adjustment = daoService.getObject(Adjustment.class, aid);
		if(adjustment == null) return showJsonError_NOT_FOUND(model);
		ErrorCode code = paymentService.approveAdjustment(adjustment, user.getId());
		if(code.isSuccess()) return showJsonSuccess(model);
		return showJsonError(model, code.getMsg());
	}
	@RequestMapping("/admin/account/ajax/removeAdjustment.xhtml")
	public String removeAdjustment(Long aid, ModelMap model){
		Adjustment adjustment = daoService.getObject(Adjustment.class, aid);
		if(Adjustment.STATUS_NEW.equals(adjustment.getStatus())){
			daoService.removeObject(adjustment);
			return showJsonSuccess(model);
		}
		return showJsonError_DATAERROR(model);
	}
	@RequestMapping("/admin/account/ajax/getAdjustmentInfo.xhtml")
	public String getAdjustmentInfo(Long memberid, ModelMap model){
		Map result = new HashMap();
		Member member = daoService.getObject(Member.class, memberid);
		if(member==null) return showJsonError(model, "用户不存在！");
		MemberAccount account = null;
		account = daoService.getObjectByUkey(MemberAccount.class, "memberid", memberid, false);
		if(account==null) return showJsonError(model, "用户尚未建立账户！");
		result.put("memberid", account.getMemberid());
		result.put("amount", account.getBanlance());
		result.put("bankcharge", account.getBankcharge());
		result.put("othercharge", account.getOthercharge());
		return showJsonSuccess(model, result);
	}
	@RequestMapping("/admin/account/ajax/applyAdjustment.xhtml")
	public String applyAdjustment(Long memberid, String type, String content, Integer amount, String tradeno, Integer bankcharge, Integer othercharge, ModelMap model){
		if(StringUtils.isBlank(type)) return showJsonError(model, "请选择调整类型！");
		if(amount==null || bankcharge==null || othercharge==null) return showError(model, "请填写金额");
		if(amount*bankcharge*othercharge<0) return showJsonError(model, "金额错误！");
		if(amount!=(bankcharge+othercharge)){
			return showJsonError(model, "金额相加不相等！");
		}

		ErrorCode<Adjustment> code = applyAdjustment2(memberid, type, content, amount, tradeno, bankcharge, othercharge);
		if(!code.isSuccess()) return showJsonError(model, code.getMsg());
		return showJsonSuccess(model);
	}
	@RequestMapping("/admin/account/ajax/batchApplyAdjustment.xhtml")
	public String applyAdjustment(){
		return "admin/gewapay/batchApplyAdjustment.vm";
	}
	@RequestMapping("/admin/account/ajax/applyBatchAdjustment.xhtml")
	public String applyAdjustment(String type, String memberids, String content, Integer amount, Integer bankcharge, Integer othercharge, ModelMap model){
		if(StringUtils.isBlank(type)) return forwardMessage(model, "请选择调整类型！");
		if(StringUtils.isBlank(content)) return forwardMessage(model, "请输入用户id！");
		if(StringUtils.contains(content, "，")) return forwardMessage(model, "用户id列表包含中文逗号");
		if(amount==null || bankcharge==null || othercharge==null) return forwardMessage(model, "请填写金额");
		if(amount*bankcharge*othercharge<0) return forwardMessage(model, "金额错误！");
		if(amount!=(bankcharge+othercharge)){
			return forwardMessage(model, "金额相加不相等！");
		}
		String[] midList = StringUtils.split(memberids, ",");
		List<String> msgList = new ArrayList<String>();
		int i = 0;
		for(String mid : midList){
			ErrorCode<Adjustment> code = applyAdjustment2(Long.valueOf(mid), type, content, amount, null, bankcharge, othercharge);
			if(!code.isSuccess()){
				msgList.add(mid + "添加失败：" + code.getMsg());
			}else {
				i++;
			}
		}
		msgList.add("共有" + midList.length + "个用户添加， 其中成功：" + i + "个");
		return forwardMessage(model, msgList);
	}
	private ErrorCode<Adjustment> applyAdjustment2(Long memberid, String type, String content, Integer amount, String tradeno, Integer bankcharge, Integer othercharge){
		Member member = daoService.getObject(Member.class, memberid);
		if(member==null) return ErrorCode.getFailure("用户不存在");
		MemberAccount account = daoService.getObjectByUkey(MemberAccount.class, "memberid", memberid, false);
		if(account==null) return ErrorCode.getFailure("该用户没有创建用户！");
		if(StringUtils.equals(type, Adjustment.CORRECT_ORDER) && StringUtils.isBlank(tradeno)){
			return ErrorCode.getFailure("请输入订单号！");
		}
		if(StringUtils.isNotBlank(tradeno)){
			GewaOrder order = daoService.getObjectByUkey(GewaOrder.class, "tradeNo", tradeno, false);
			if(order==null) return ErrorCode.getFailure("订单不存在！");
		}
		if(StringUtils.startsWith(type, Adjustment.CORRECT_SUB)){//减少
			if(amount > account.getBanlance() || bankcharge > account.getBankcharge() || othercharge > account.getOthercharge()){
				return ErrorCode.getFailure("金额不足，请查看用户余额、可退余额、不可退余额！");
			}
		}

		Adjustment adjustment = new Adjustment(account.getId(), memberid, member.getRealname(), type);
		adjustment.setTradeno(tradeno);
		adjustment.setContent(content);
		adjustment.setBankcharge(bankcharge);
		adjustment.setOthercharge(othercharge);
		adjustment.setAmount(amount);
		daoService.saveObject(adjustment);
		return ErrorCode.getSuccessReturn(adjustment);
	}
	@RequestMapping(value="/admin/account/orderToPay.xhtml", method=RequestMethod.GET)
	public String orderToPay(String tradeNo, ModelMap model){
		if(StringUtils.isNotBlank(tradeNo)){
			model.put("order", daoService.getObjectByUkey(GewaOrder.class, "tradeNo", tradeNo, false));
			model.put("paytextMap", PaymethodConstant.getPayTextMap());
		}
		return "admin/gewapay/orderToPay.vm";
	}
	
	@RequestMapping(value="/admin/account/reValidBankWabi.xhtml")
	public String reValidBankWabi(Long memberid, Integer bankcharge, Integer othercharge, ModelMap model){
		MemberAccount account = daoService.getObjectByUkey(MemberAccount.class, "memberid", memberid, false);
		if(account == null) return showJsonError(model, "账户不存在！");
		if(bankcharge*othercharge<0)  return showJsonError(model, "金额不能为0！");
		if(account.getBanlance()!=(bankcharge+othercharge)) return showJsonError(model, "可退金额+不可退金额不等于账户余额");
		account.setBankcharge(bankcharge);
		account.setOthercharge(othercharge);
		daoService.saveObject(account);
		User user = getLogonUser();
		dbLogger.warnWithType(LogTypeConstant.LOG_TYPE_ORDER_PAY, user.getRealname() +  user.getId() + "重置用户的账户内金额和瓦币:" + bankcharge + ", " + othercharge);
		return showJsonSuccess(model);
	}
	@RequestMapping(value="/admin/account/getBankWabi.xhtml")
	public String getBankWabi(){
		return "admin/gewapay/bankwabi.vm";
	}
	
	@RequestMapping(value="/admin/account/orderToPay.xhtml", method=RequestMethod.POST)
	public String orderToPay(String tradeNo, String paySeq, String paymethod, int paidAmount, ModelMap model){
		if(StringUtils.isBlank(tradeNo)||StringUtils.isBlank(paySeq)||StringUtils.isBlank(paymethod)){
			return forwardMessage(model, "请求参数不正确！");
		}
		if(!PaymethodConstant.isValidPayMethod(paymethod)){
			return forwardMessage(model, "支付方式不正确！");
		}
		GewaOrder order = daoService.getObjectByUkey(GewaOrder.class, "tradeNo", tradeNo, false);
		if(order.getDue() < paidAmount){
			return forwardMessage(model, "支付金额不正确！");
		}
		User user = getLogonUser();
		//TODO:加入处理流程，让财务审核！！
		orderMonitorService.addOrderChangeLog(tradeNo, "人工更改", "更改支付状态：" + user.getRealname() +  user.getId(), user.getId());
		orderLogService.addSysLog(tradeNo, paymethod, OrderLogService.ACTION_MANUAL2PAY, user.getId());

		paymentService.netPayOrder(order.getTradeNo(), paySeq, paidAmount, paymethod, "manual", "手工支付");
		if(order.isAllPaid()){
			return showMessage(model, "支付成功！");
		}else{
			return showMessage(model, "支付失败！");
		}
	}
	@RequestMapping("/admin/account/ajax/createAccount.xhtml")
	public String createAccount(Long memberid, ModelMap model){
		MemberAccount account = daoService.getObjectByUkey(MemberAccount.class, "memberid", memberid, false);
		if(account == null){ //建立一个新账号
			Member member = daoService.getObject(Member.class, memberid);
			if(member==null) return showJsonError(model, "用户不存在！");
			ChangeEntry changeEntry = new ChangeEntry(account);
			account = paymentService.createNewAccount(member);
			monitorService.saveChangeLog(member.getId(), MemberAccount.class, account.getId(),changeEntry.getChangeMap( account));
			return showJsonSuccess(model, "成功为" + member.getNickname() + "[" + member.getUsername() +"]建立了账户！");
		}
		return showJsonError(model, "用户账户已存在！");
	}
	
	@RequestMapping("/admin/account/ajax/forbidAccountList.xhtml")
	public String forbidAccount(ModelMap model){
		List<MemberAccount> accountList = daoService.getObjectListByField(MemberAccount.class, "forbid", Status.Y);
		model.put("accountList", accountList);
		return "admin/gewapay/forbidAccountList.vm";
	}
	
	@RequestMapping("/admin/account/ajax/operForbidAccount.xhtml")
	public String forbidAccount(String memberids, String status, ModelMap model){
		if(StringUtils.isBlank(memberids)){
			return showJsonSuccess(model, "缺少参数memerids");
		}
		String[] ms = StringUtils.split(memberids, ",");
		int c = 0;
		for(String mid : ms){
			MemberAccount account = daoService.getObjectByUkey(MemberAccount.class, "memberid", Long.valueOf(mid), false);
			if(null != account){
				ChangeEntry changeEntry = new ChangeEntry(account);
				account.setForbid(status);
				daoService.saveObject(account);
				monitorService.saveChangeLog(getLogonUser().getId(), MemberAccount.class, account.getId(),changeEntry.getChangeMap( account));
				c++;
			}
		}
		return showJsonSuccess(model, c+"");
	}
	
	@RequestMapping("/admin/account/qryCharge.xhtml")
	public String qryAccount(Long memberid, String membername, String email, String status, Integer totalfee, Date startdate, Date enddate, Integer pageNo, ModelMap model){
		if(pageNo==null) pageNo = 0;
		List list = hibernateTemplate.findByCriteria(getChargeQry(memberid, membername, email, status, totalfee, startdate, enddate).setProjection(Projections.rowCount()));
		int count = 0;
		if(!list.isEmpty()) count = Integer.parseInt(list.get(0)+"");
		int rows = 20;
		List<Charge> chargeList = hibernateTemplate.findByCriteria(getChargeQry(memberid, membername, email, status, totalfee, startdate, enddate), pageNo*rows, rows);
		PageUtil pageUtil = new PageUtil(count, rows, pageNo, "admin/account/qryCharge.xhtml");
		Map params = new HashMap();
		if(memberid!=null) params.put("memberid", memberid);
		if(StringUtils.isNotBlank(status)) params.put("status", status);
		if(totalfee!=null) params.put("totalfee", totalfee);
		if(StringUtils.isNotBlank(email)) params.put("email", email);
		if(StringUtils.isNotBlank(membername)) params.put("membername", membername);
		if(startdate!=null) params.put("startdate", DateUtil.formatDate(startdate));
		if(enddate!=null) params.put("enddate", DateUtil.formatDate(enddate));
		pageUtil.initPageInfo(params);
		model.put("pageUtil", pageUtil);
		model.put("chargeList", chargeList);
		return "admin/gewapay/qryCharge.vm";
	}
	private DetachedCriteria getChargeQry(Long memberid, String membername, String email, String status, Integer totalfee, Date startdate, Date enddate){
		DetachedCriteria qry = DetachedCriteria.forClass(Charge.class, "c");
		if(memberid!=null) qry.add(Restrictions.eq("c.memberid", memberid));
		if(StringUtils.isNotBlank(membername)) qry.add(Restrictions.eq("c.membername", membername));
		if(StringUtils.isNotBlank(status)) qry.add(Restrictions.eq("c.status", status));
		if(totalfee!=null) qry.add(Restrictions.eq("totalfee", totalfee));
		if(startdate!=null) qry.add(Restrictions.ge("addtime", new Timestamp(startdate.getTime())));
		if(enddate!=null) qry.add(Restrictions.le("addtime", DateUtil.getLastTimeOfDay(enddate)));
		if(StringUtils.isNotBlank(email)) {
			DetachedCriteria sub = DetachedCriteria.forClass(Member.class, "m");
			sub.add(Restrictions.eq("m.email", email));
			sub.add(Restrictions.eqProperty("m.id", "c.memberid"));
			sub.setProjection(Projections.property("m.id"));
			qry.add(Subqueries.exists(sub));
		}
		qry.addOrder(Order.desc("c.addtime"));
		return qry;
	}
}
