package com.gewara.untrans.order;

import com.gewara.model.acl.User;
import com.gewara.model.pay.AccountRefund;
import com.gewara.model.pay.GewaOrder;
import com.gewara.model.pay.OrderRefund;
import com.gewara.model.pay.SMSRecord;
import com.gewara.support.ErrorCode;

public interface RefundOperationService {

	/**
	 * 确认订单退票
	 * @param rid
	 * @param user
	 * @return
	 */
	ErrorCode confirmRefund(OrderRefund refund, Long userid, String username);
	
	/**
	 * 确认订单退票
	 * @param rid
	 * @param user
	 * @return
	 */
	ErrorCode confirmRefund(OrderRefund refund, GewaOrder order, Long userid, String username);
	
	/**
	 * 取消退款订单短信及邮件发送
	 * @param order
	 */
	void refundOtherOperation(GewaOrder order);
	
	/**
	 * 电影订单确认退票
	 * @param refund
	 * @param force
	 * @param user
	 * @return
	 */
	ErrorCode cancelTicket(OrderRefund refund, boolean force, User user);
	
	/**
	 * 退款到银行短信
	 * @param userid
	 * @param accountRefund
	 * @return
	 */
	SMSRecord bankTemplateMsg(Long userid, AccountRefund accountRefund);

}
