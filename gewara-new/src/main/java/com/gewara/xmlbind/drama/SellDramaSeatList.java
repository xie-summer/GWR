package com.gewara.xmlbind.drama;

import java.util.ArrayList;
import java.util.List;

import com.gewara.model.drama.SellDramaSeat;

public class SellDramaSeatList {

	private List<SellDramaSeat> sellDramaSeatList = new ArrayList<SellDramaSeat>();
	
	public List<SellDramaSeat> getSellDramaSeatList(){
		return sellDramaSeatList;
	}
	public void setSellDramaSeatList(List<SellDramaSeat> sellDramaSeatList){
		this.sellDramaSeatList = sellDramaSeatList;
	}
	
	public void addSellDramaSeat(SellDramaSeat sellDramaSeat){
		sellDramaSeatList.add(sellDramaSeat);
	}
}
