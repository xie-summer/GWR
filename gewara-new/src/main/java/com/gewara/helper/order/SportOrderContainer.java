package com.gewara.helper.order;

import com.gewara.model.pay.SportOrder;
import com.gewara.model.sport.OpenTimeTable;

public class SportOrderContainer extends OrderContainer{
	public SportOrderContainer(SportOrder order){
		this.order = order;
	}
	private OpenTimeTable ott;
	
	public SportOrder getSportOrder(){
		return (SportOrder) order;
	}

	public OpenTimeTable getOtt() {
		return ott;
	}

	public void setOtt(OpenTimeTable ott) {
		this.ott = ott;
	}
}
