package com.gewara.service.gewapay;

import com.gewara.model.pay.GewaOrder;
import com.gewara.model.pay.OrderRefund;
import com.gewara.support.ErrorCode;

public interface RefundOrderService {

	ErrorCode refund(GewaOrder order, OrderRefund refund, Long userid);
}
