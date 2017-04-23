package com.gewara.service.ticket;

import com.gewara.helper.order.DramaOrderContainer;
import com.gewara.helper.order.GoodsOrderContainer;
import com.gewara.helper.order.SportOrderContainer;
import com.gewara.helper.order.TicketOrderContainer;
import com.gewara.model.drama.DramaOrder;
import com.gewara.model.pay.GoodsOrder;
import com.gewara.model.pay.SportOrder;
import com.gewara.model.pay.TicketOrder;

public interface SuccessOrderService {
	/**
	 * 处理订单成功：
	 * 1）自动绑定套餐
	 * 2）增加额外积分
	 * @param order
	 * @return Container(order,smsList)
	 */
	TicketOrderContainer processTicketOrderSuccess(TicketOrder order);
	
	/**
	 * @param order
	 * @return Container(order,)
	 */
	TicketOrderContainer updateTicketOrderStats(TicketOrder order);
	
	DramaOrderContainer processDramaOrderSuccess(DramaOrder gewaOrder);

	SportOrderContainer processSportOrderSuccess(SportOrder gewaOrder);

	GoodsOrderContainer processGoodsOrderSuccess(GoodsOrder order);
}
