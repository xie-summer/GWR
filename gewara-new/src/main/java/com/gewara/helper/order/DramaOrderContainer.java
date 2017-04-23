package com.gewara.helper.order;

import com.gewara.model.drama.DramaOrder;
import com.gewara.model.drama.OpenDramaItem;

public class DramaOrderContainer extends OrderContainer{
	private OpenDramaItem item;
	public DramaOrderContainer(DramaOrder order, OpenDramaItem item){
		this.order = order;
		this.item = item;
	}
	public DramaOrder getDramaOrder(){
		return (DramaOrder) order;
	}
	
	public OpenDramaItem getItem() {
		return item;
	}
	public void setItem(OpenDramaItem item) {
		this.item = item;
	}
	
}
