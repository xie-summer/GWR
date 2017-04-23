package com.gewara.helper;

import java.sql.Timestamp;
import java.util.List;
import java.util.Map;

import com.gewara.constant.TheatreSeatConstant;
import com.gewara.model.drama.OpenTheatreSeat;
import com.gewara.model.drama.SellDramaSeat;
import com.gewara.util.BeanUtil;

public class DramaSeatStatusUtil {
	private Map<Long/*id*/, SellDramaSeat> sellSeatMap;
	private Timestamp cur;
	public DramaSeatStatusUtil(List<SellDramaSeat> sellSeatList){
		this.sellSeatMap = BeanUtil.beanListToMap(sellSeatList, "id");
		this.cur = new Timestamp(System.currentTimeMillis());
		
	}
	public String getFullStatus(OpenTheatreSeat oseat) {
		SellDramaSeat sSeat = sellSeatMap.get(oseat.getId());
		if(sSeat!=null){
			if(sSeat.getStatus().equals(TheatreSeatConstant.STATUS_SOLD)) return TheatreSeatConstant.STATUS_SOLD;
			else if(sSeat.getStatus().equals(TheatreSeatConstant.STATUS_NEW) && sSeat.getValidtime().after(cur)) return TheatreSeatConstant.STATUS_SELLING;//´ý¸¶¿î
		}
		return oseat.getStatus();
	}
	
	public String getRemark(OpenTheatreSeat oseat){
		String remark = getFullStatus(oseat) + "," + oseat.getSeatLabel();
		if(sellSeatMap.get(oseat.getId())!=null) remark +=","+ sellSeatMap.get(oseat.getId()).getRemark();
		return remark;
	}
}
