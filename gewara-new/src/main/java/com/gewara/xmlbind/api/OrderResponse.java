package com.gewara.xmlbind.api;

import java.util.ArrayList;
import java.util.List;

import com.gewara.model.api.OrderResult;

public class OrderResponse{
	private List<OrderResult> orderList = new ArrayList<OrderResult>();

	public List<OrderResult> getOrderList() {
		return orderList;
	}
	public void setOrderList(List<OrderResult> orderList) {
		this.orderList = orderList;
	}
	public void addOrder(OrderResult orderResult){
		this.orderList.add(orderResult);
	}
}
