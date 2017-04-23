package com.gewara.helper.order;

import com.gewara.model.pay.GymOrder;

public class GymOrderContainer extends OrderContainer{
	public GymOrder getGymOrder(){
		return (GymOrder) order;
	}
}
