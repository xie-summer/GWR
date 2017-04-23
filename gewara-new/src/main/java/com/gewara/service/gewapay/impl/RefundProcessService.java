package com.gewara.service.gewapay.impl;

import java.sql.Timestamp;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import com.gewara.constant.PaymethodConstant;
import com.gewara.model.pay.Adjustment;
import com.gewara.model.pay.GewaOrder;
import com.gewara.model.pay.MemberAccount;
import com.gewara.model.user.Member;
import com.gewara.service.gewapay.PaymentService;
import com.gewara.service.impl.BaseServiceImpl;

public class RefundProcessService extends BaseServiceImpl{
	@Autowired@Qualifier("paymentService")
	protected PaymentService paymentService;
	public void setPaymentService(PaymentService paymentService) {
		this.paymentService = paymentService;
	}
	protected final void refund2Account(GewaOrder order, Long userid, Integer gewaRetAmount, Timestamp cur, String content){
		MemberAccount account = baseDao.getObjectByUkey(MemberAccount.class, "memberid", order.getMemberid(), false);
		if(account==null){
			Member member = baseDao.getObject(Member.class, order.getMemberid());
			account = paymentService.createNewAccount(member);
		}
		Adjustment adjustment = new Adjustment(account.getId(), order.getMemberid(), order.getMembername(), Adjustment.CORRECT_ORDER);
		adjustment.setTradeno(order.getTradeNo());
		adjustment.setAmount(gewaRetAmount);
		
		adjustment.setContent(order.getTradeNo() + content);
		adjustment.setUpdatetime(cur);
		adjustment.setClerkid(userid);
		adjustment.setStatus(Adjustment.STATUS_SUCCESS);
		
		account.addBanlance(adjustment.getAddAmount());
		Integer bankcharge = 0, othercharge=0, oldbankcharge = account.getBankcharge(), oldothercharge = account.getOthercharge();
		if(StringUtils.equals(order.getPaymethod(), PaymethodConstant.PAYMETHOD_GEWAPAY)){
			int bankfee = order.getDue()-order.getWabi();
			if(order.getWabi()>0){
				othercharge = Math.min(gewaRetAmount, order.getWabi());
				bankcharge = gewaRetAmount - othercharge;
			}else if(bankfee>0){
				 bankcharge = Math.min(gewaRetAmount, bankfee);
				 othercharge = gewaRetAmount - bankcharge;
			}else{
				othercharge = gewaRetAmount;
			}
		}else {
			bankcharge = gewaRetAmount;
		}
		if(bankcharge + othercharge != gewaRetAmount) {
			throw new IllegalArgumentException("订单金额错误，总金额必须为：" + gewaRetAmount);
		}
		dbLogger.warn(order.getTradeNo() + "退款到余额:瓦币：由"+ oldothercharge + "增加"+othercharge+",账户金额：由"+oldbankcharge+"增加"+bankcharge);
		account.addBankcharge(bankcharge);
		account.addWabicharge(othercharge);
		adjustment.setBankcharge(bankcharge);
		adjustment.setOthercharge(othercharge);
		
		baseDao.saveObject(account);
		baseDao.saveObject(adjustment);
		dbLogger.warn(order.getTradeNo() + "退款到余额....");
	}

}
