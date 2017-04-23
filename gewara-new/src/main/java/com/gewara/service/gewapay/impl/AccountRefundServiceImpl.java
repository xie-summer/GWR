package com.gewara.service.gewapay.impl;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.gewara.model.pay.AccountRefund;
import com.gewara.model.pay.Adjustment;
import com.gewara.model.pay.MemberAccount;
import com.gewara.model.user.Member;
import com.gewara.service.gewapay.AccountRefundService;
import com.gewara.service.gewapay.PaymentService;
import com.gewara.service.impl.BaseServiceImpl;
import com.gewara.support.ErrorCode;

@Service("accountRefundService")
public class AccountRefundServiceImpl extends BaseServiceImpl implements AccountRefundService {
	@Autowired@Qualifier("paymentService")
	private PaymentService paymentService;
	
	@Override
	public List<AccountRefund> getAccountRefundList(String tradeno, String status, Long memberid, String mobile, int from, int maxnum) {
		DetachedCriteria qry = DetachedCriteria.forClass(AccountRefund.class);
		if(StringUtils.isNotBlank(tradeno)) qry.add(Restrictions.eq("tradeno", tradeno));
		if(StringUtils.isNotBlank(status)) qry.add(Restrictions.eq("status", status));
		if(memberid != null) qry.add(Restrictions.eq("memberid", memberid));
		if(StringUtils.isNotBlank(mobile)) qry.add(Restrictions.eq("mobile", mobile));
		qry.addOrder(Order.desc("addtime"));
		List<AccountRefund> refundList = hibernateTemplate.findByCriteria(qry, from, maxnum);
		return refundList;
	}
	
	@Override
	public int getAccountRefundCount(String tradeno, String status, Long memberid, String mobile) {
		DetachedCriteria qry = DetachedCriteria.forClass(AccountRefund.class);
		if(StringUtils.isNotBlank(tradeno)) qry.add(Restrictions.eq("tradeno", tradeno));
		if(StringUtils.isNotBlank(status)) qry.add(Restrictions.eq("status", status));
		if(memberid != null) qry.add(Restrictions.eq("memberid", memberid));
		if(StringUtils.isNotBlank(mobile)) qry.add(Restrictions.eq("mobile", mobile));
		qry.setProjection(Projections.rowCount());
		List result = hibernateTemplate.findByCriteria(qry);
		return Integer.valueOf(""+result.get(0));
	}

	@Override
	public ErrorCode deductAccountRefund(AccountRefund refund, Long userid) {
		if(refund == null) return ErrorCode.getFailure("退款信息不存在！");
		if(!StringUtils.equals(refund.getStatus(), AccountRefund.STATUS_ACCEPT)) return ErrorCode.getFailure("只有已接受状态才能操作扣款！");
		if(!refund.isOutPartner()){
			Member member = baseDao.getObject(Member.class, refund.getMemberid());
			MemberAccount account = baseDao.getObjectByUkey(MemberAccount.class, "memberid", member.getId(), false);
			if(refund.getAmount()>account.getBankcharge()) return ErrorCode.getFailure("当前金额大于可退余额！");
			Adjustment adjustment = new Adjustment(account.getId(),  member.getId(),  member.getNickname(), Adjustment.CORRECT_REFUND);
			adjustment.setContent((StringUtils.isBlank(refund.getTradeno())?"":refund.getTradeno()) + "退款到银行");
			adjustment.setAmount(refund.getAmount());
			adjustment.setBankcharge(refund.getAmount());
			ErrorCode code = paymentService.approveAdjustment(adjustment, account, userid);
			if(!code.isSuccess()) return code;
		}
		refund.setStatus(AccountRefund.STATUS_DEBIT);
		baseDao.saveObject(refund);
		return ErrorCode.SUCCESS;
	}

}
