package com.gewara.service.sport;

import com.gewara.model.pay.Charge;
import com.gewara.model.sport.OpenTimeSale;
import com.gewara.model.sport.SellDeposit;
import com.gewara.model.user.Member;
import com.gewara.service.BaseService;
import com.gewara.support.ErrorCode;

public interface GuaranteeOrderService extends BaseService {

	ErrorCode<Charge> saveDepositCharge(OpenTimeSale ots, Member member, String mobile);
	
	SellDeposit getSellDeposit(Long otsid, Long memberid, String status);
}
