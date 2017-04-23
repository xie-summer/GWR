package com.gewara.service.partner;

import java.util.List;

import com.gewara.model.partner.CallbackOrder;
import com.gewara.model.pay.GewaOrder;
import com.gewara.support.ErrorCode;

/**
 * 合作伙伴订单同步
 * @author acerge(acerge@163.com)
 * @since 6:03:24 PM May 19, 2010
 */
public interface PartnerSynchService {
	/**
	 * 加入到回传队列，并执行一次推送
	 * @param order
	 * @param pushflag: 传送标记
	 * @param renew 订单状态改变，不管之前是否传送，现在重新传送
	 */
	CallbackOrder addCallbackOrder(GewaOrder order, String pushflag, boolean renew);
	/**
	 * 推送订单信息给合作商
	 * @param order
	 * @return
	 */
	ErrorCode pushCallbackOrder(CallbackOrder order);
	String writeChinapayTransFile();
	List<CallbackOrder> getCallbackOrderList(int maxtimes);
	ErrorCode pushCallbackOrder(String tradeNo, String pushflag);
}
