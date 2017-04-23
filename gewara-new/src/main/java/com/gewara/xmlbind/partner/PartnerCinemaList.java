/**
 * 
 */
package com.gewara.xmlbind.partner;

import java.util.ArrayList;
import java.util.List;

public class PartnerCinemaList {
	private List<QQCity> cityList = new ArrayList<QQCity>();
	public List<QQCity> getCityList() {
		return cityList;
	}


	public void setCityList(List<QQCity> cityList) {
		this.cityList = cityList;
	}


	public void addCity(QQCity city){
		cityList.add(city);
	}
}
