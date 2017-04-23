package com.gewara.helper.ticket;

import java.sql.Timestamp;
import java.util.List;
import java.util.Map;

import com.gewara.constant.ticket.SeatConstant;
import com.gewara.model.BaseObject;
import com.gewara.model.movie.RoomSeat;
import com.gewara.model.ticket.OpenSeat;
import com.gewara.model.ticket.SellSeat;
import com.gewara.util.BeanUtil;

public class SeatStatusUtil {
	private Map<String/*key*/, SellSeat> sellSeatMap;
	private Timestamp cur;
	public SeatStatusUtil(List<SellSeat> sellSeatList){
		this.sellSeatMap = BeanUtil.beanListToMap(sellSeatList, "key");
		this.cur = new Timestamp(System.currentTimeMillis());	
	}
	public String getFullStatus(BaseObject seat) {
		if(seat instanceof OpenSeat){
			return getOpenSeatFullStatus((OpenSeat) seat);
		}else{
			return getRoomSeatStatus((RoomSeat) seat);
		}
		
	}
	public String getOpenSeatFullStatus(OpenSeat oseat) {
		SellSeat sSeat = sellSeatMap.get(oseat.getKey());
		if(sSeat!=null){
			if(sSeat.getStatus().equals(SeatConstant.STATUS_SOLD)) return SeatConstant.STATUS_SOLD;
			else if(sSeat.getStatus().equals(SeatConstant.STATUS_NEW) && sSeat.getValidtime().after(cur)) return SeatConstant.STATUS_SELLING;//´ý¸¶¿î
		}
		return oseat.getStatus();
	}
	public String getRemark(BaseObject seat){
		String remark = getFullStatus(seat) + "," + BeanUtil.get(seat, "seatLabel");
		String key = (String) BeanUtil.get(seat, "key");
		if(sellSeatMap.get(key)!=null) remark +=","+ sellSeatMap.get(key).getRemark();
		return remark;
	}
	public String getRoomSeatStatus(RoomSeat rseat){
		SellSeat sSeat = sellSeatMap.get(rseat.getSeatline()+","+rseat.getSeatrank());
		if(sSeat!=null){
			if(sSeat.getStatus().equals(SeatConstant.STATUS_SOLD)) return SeatConstant.STATUS_SOLD;
		}
		return SeatConstant.STATUS_NEW;
	}
}
