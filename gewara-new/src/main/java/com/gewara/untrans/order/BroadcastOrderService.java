package com.gewara.untrans.order;

import com.gewara.model.pay.GewaOrder;

public interface BroadcastOrderService {
	void broadcastOrder(GewaOrder order);
	void broadcastOrder(GewaOrder order, String flag, String flagval, Long userid);
	void broadcastBarcode(String tradenos, Long cinemaid, String randcode, String machineno);
}
