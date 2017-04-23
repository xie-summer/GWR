package com.gewara.service.sport.impl;

import java.sql.Timestamp;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Service;

import com.gewara.constant.ApiConstant;
import com.gewara.constant.ChargeConstant;
import com.gewara.constant.PaymethodConstant;
import com.gewara.model.pay.Charge;
import com.gewara.model.sport.OpenTimeSale;
import com.gewara.model.sport.OpenTimeTable;
import com.gewara.model.sport.SellDeposit;
import com.gewara.model.user.Member;
import com.gewara.pay.PayUtil;
import com.gewara.service.impl.BaseServiceImpl;
import com.gewara.service.sport.GuaranteeOrderService;
import com.gewara.support.ErrorCode;
import com.gewara.util.DateUtil;
import com.gewara.util.ValidateUtil;

@Service("guaranteeOrderService")
public class GuaranteeOrderServiceImpl extends BaseServiceImpl implements GuaranteeOrderService {

	@Override
	public ErrorCode<Charge> saveDepositCharge(OpenTimeSale ots, Member member, String mobile){
		if(!ValidateUtil.isMobile(mobile)) return ErrorCode.getFailure(ApiConstant.CODE_SIGN_ERROR, "手机号格式错误！");
		if(ots.hasSuccess()) return ErrorCode.getFailure(ApiConstant.CODE_SIGN_ERROR, "竞价场次已卖出不能支付保证金！");
		if(!ots.hasBooking()) return ErrorCode.getFailure(ApiConstant.CODE_SIGN_ERROR, "竞价场次没有开始！");
		OpenTimeTable ott = baseDao.getObject(OpenTimeTable.class, ots.getOttid());
		if(!ott.isBooking()) return ErrorCode.getFailure(ApiConstant.CODE_SIGN_ERROR, "场次未开放，不能支付保证金！");
		SellDeposit deposit = getSellDeposit(ots.getId(), member.getId(), null, null);
		Charge charge = null;
		if(deposit != null){
			charge = baseDao.getObject(Charge.class, deposit.getChargeid());
			if(charge.isPaid()) return ErrorCode.getFailure(ApiConstant.CODE_SIGN_ERROR, "保证金已支付！");
		}else{
			charge = new Charge(PayUtil.getChargeTradeNo(), ChargeConstant.DEPOSITPAY);
			charge.setMemberid(member.getId());
			charge.setMembername(member.getNickname());
			charge.setValidtime(DateUtil.addHour(charge.getAddtime(), 2));
		}
		charge.setPaymethod(PaymethodConstant.PAYMETHOD_ALIPAY);
		charge.setPaybank("");
		charge.setTotalfee(20);
		baseDao.saveObject(charge);
		if(deposit == null){
			deposit = new SellDeposit(charge, ots);
			deposit.setMobile(mobile);
			baseDao.saveObject(deposit);
		}
		dbLogger.warn("saveBailCharge =>memberid:" + member.getId() + "ots:" + ots.getOttid() +"," + ots.getOtiids());
		return ErrorCode.getSuccessReturn(charge);
	}
	
	@Override
	public SellDeposit getSellDeposit(Long otsid, Long memberid, String status){
		return getSellDeposit(otsid, memberid, status, null);
	}
	
	private SellDeposit getSellDeposit(Long otsid, Long memberid, String status, Timestamp validtime){
		DetachedCriteria query = queryCriteria(otsid, memberid, status, validtime);
		query.addOrder(Order.desc("addtime"));
		List<SellDeposit> sellList = hibernateTemplate.findByCriteria(query, 0, 1);
		if(sellList.isEmpty()) return null;
		return sellList.get(0);
	}
	
	private DetachedCriteria queryCriteria(Long otsid, Long memberid, String status, Timestamp validtime){
		DetachedCriteria query = DetachedCriteria.forClass(SellDeposit.class);
		query.add(Restrictions.eq("otsid", otsid));
		query.add(Restrictions.eq("memberid", memberid));
		if(StringUtils.isNotBlank(status)){
			query.add(Restrictions.eq("status", status));
		}
		if(validtime != null){
			query.add(Restrictions.ge("validtime", validtime));
		}
		return query;
	}
}
