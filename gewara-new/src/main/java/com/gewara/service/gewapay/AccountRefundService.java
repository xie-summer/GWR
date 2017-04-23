package com.gewara.service.gewapay;

import java.util.List;

import com.gewara.model.pay.AccountRefund;
import com.gewara.support.ErrorCode;

public interface AccountRefundService {
	List<AccountRefund> getAccountRefundList(String tradeno, String status, Long memberid,String mobile, int from, int maxnum);
	int getAccountRefundCount(String tradeno, String status, Long memberid, String mobile);
	ErrorCode deductAccountRefund(AccountRefund refund, Long userid);
}
