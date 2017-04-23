package com.gewara.xmlbind.ticket;

import java.util.ArrayList;
import java.util.List;

import com.gewara.xmlbind.BaseObjectListResponse;

public class WDOrderList extends BaseObjectListResponse<WdOrder> {
	private List<WdOrder> orderList = new ArrayList<WdOrder>();

	public List<WdOrder> getOrderList() {
		return orderList;
	}

	public void setOrderList(List<WdOrder> orderList) {
		this.orderList = orderList;
	}

	public void addOrderList(WdOrder wdorder){
		this.orderList.add(wdorder);
	}

	@Override
	public List<WdOrder> getObjectList() {
		return orderList;
	}

}
