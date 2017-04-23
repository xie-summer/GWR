package com.gewara.web.action.inner.util;

import org.apache.commons.lang.StringUtils;

import com.gewara.api.gpticket.vo.ticket.FieldAreaVo;
import com.gewara.api.gpticket.vo.ticket.FieldVo;
import com.gewara.api.gpticket.vo.ticket.ShowAreaVo;
import com.gewara.api.gpticket.vo.ticket.ShowPackPriceVo;
import com.gewara.api.gpticket.vo.ticket.ShowPriceVo;
import com.gewara.api.gpticket.vo.ticket.ShowSeatVo;
import com.gewara.constant.OdiConstant;
import com.gewara.constant.Status;
import com.gewara.constant.TheatreSeatConstant;
import com.gewara.model.drama.DisQuantity;
import com.gewara.model.drama.DramaPlayItem;
import com.gewara.model.drama.OpenDramaItem;
import com.gewara.model.drama.OpenTheatreSeat;
import com.gewara.model.drama.TheatreField;
import com.gewara.model.drama.TheatreRoom;
import com.gewara.model.drama.TheatreSeatArea;
import com.gewara.model.drama.TheatreSeatPrice;

public abstract class DramaRemoteUtil {

	public static OpenTheatreSeat createOpenTheatreSeat(ShowSeatVo seatVo, TheatreSeatPrice seatPrice) {
		OpenTheatreSeat seat = new OpenTheatreSeat();
		seat.setLineno(seatVo.getLineno());
		seat.setAreaid(seatPrice.getAreaid());
		seat.setCostprice(seatPrice.getCostprice());
		seat.setPrice(seatPrice.getPrice());
		seat.setTheatreprice(seatPrice.getTheatreprice());
		seat.setDpid(seatPrice.getDpid());
		seat.setOdiid(seat.getDpid());
		seat.setRankno(seatVo.getRankno());
		seat.setSeatline(seatVo.getSeatline());
		seat.setSeatrank(seatVo.getSeatrank());
		seat.setSeattype(seatPrice.getSeattype());
		seat.setStatus(TheatreSeatConstant.STATUS_NEW);
		seat.setLoveInd(seatVo.getLoveInd());
		return seat;
	}
	
	public static void copyTheatreField(TheatreField field, FieldVo fieldVo){
		field.setName(fieldVo.getName());
		field.setFieldnum(fieldVo.getFieldnum());
		field.setFieldtype(fieldVo.getFieldtype());
		field.setMobilelogo(fieldVo.getLogo());
	}
	
	public static void copyTheatreRoom(TheatreRoom room, FieldAreaVo areaVo){
		room.setLinenum(areaVo.getLinenum());
		room.setRanknum(areaVo.getRanknum());
		room.setRoomname(areaVo.getName());
		room.setFirstline(areaVo.getFirstline());
		room.setFirstrank(areaVo.getFirstrank());
		room.setSeatnum(areaVo.getSeatnum());
		room.setNum(areaVo.getAreanum());
		room.setRoomtype(areaVo.getAreatype());
	}
	
	public static void copyTheatreSeatPrice(TheatreSeatPrice seatPrice, ShowPriceVo priceVo){
		seatPrice.setTheatreprice(priceVo.getPrice());
		seatPrice.setSispseq(priceVo.getSispseq());
		seatPrice.setQuantity(priceVo.getTicketTotal());
		seatPrice.setDramaid(priceVo.getDramaid());
		if(StringUtils.equals(priceVo.getStatus(), Status.N)){
			seatPrice.setStatus(Status.DEL);
		}
		seatPrice.setCsellnum(priceVo.getTicketTotal()-priceVo.getTicketLimit()-seatPrice.getSales());
	}
	
	public static void copyTheateSeatArea(TheatreSeatArea seatArea, DramaPlayItem dpi, ShowAreaVo areaVo){
		seatArea.setDramaid(dpi.getDramaid());
		seatArea.setTheatreid(dpi.getTheatreid());
		seatArea.setFieldnum(areaVo.getFieldnum());
		seatArea.setSellerseq(areaVo.getSaseqNo());
		seatArea.setStanding(areaVo.getStanding());
		seatArea.setTheatreid(dpi.getTheatreid());
		seatArea.setRoomnum(areaVo.getAreanum());
		seatArea.setSeller(dpi.getSeller());
		seatArea.setAreaname(areaVo.getAreaname());
		seatArea.setFirstline(areaVo.getFirstline());
		seatArea.setFirstrank(areaVo.getFirstrank());
		seatArea.setLinenum(areaVo.getLinenum());
		seatArea.setRanknum(areaVo.getRanknum());
		if(dpi.isOpenprice()){
			seatArea.setTotal(areaVo.getTotalnum());
			seatArea.setLimitnum(areaVo.getLimitnum());
			seatArea.setCsellnum(seatArea.getTotal()-seatArea.getLimitnum());
		}
		seatArea.setMobilehotzone(areaVo.getHotzone());
	}
	
	public static void copyTheatreSeatArea(TheatreSeatArea seatArea, DramaPlayItem item, TheatreField field, TheatreRoom room){
		seatArea.setAreaname(room.getRoomname());
		seatArea.setDescription(room.getContent());
		seatArea.setDramaid(item.getDramaid());
		seatArea.setTheatreid(item.getTheatreid());
		seatArea.setFieldnum(field.getFieldnum());
		seatArea.setFirstline(room.getFirstline());
		seatArea.setRoomnum(room.getNum());
		seatArea.setFirstrank(room.getFirstrank());
		seatArea.setLinenum(room.getLinenum());
		seatArea.setRanknum(room.getRanknum());
		seatArea.setTotal(room.getSeatnum());
		seatArea.setLimitnum(room.getSeatnum());
		seatArea.setSeller(item.getSeller());
		seatArea.setSellerseq(item.getSellerseq());
		seatArea.setStanding(StringUtils.equals(item.getOpentype(), OdiConstant.OPEN_TYPE_SEAT)?Status.N:Status.Y);
		seatArea.setHotzone(room.getHotzone());
	}
	
	public static void copyDisQuantity(DisQuantity disQuantity, ShowPackPriceVo packPriceVo){
		disQuantity.setTheatreprice(packPriceVo.getPrice());
		disQuantity.setQuantity(packPriceVo.getQuantity());
		disQuantity.setSeller(packPriceVo.getPartner());
		disQuantity.setSispseq(packPriceVo.getPackpseq());
		disQuantity.setStarttime(packPriceVo.getStarttime());
		disQuantity.setEndtime(packPriceVo.getEndtime());
		disQuantity.setRetail(packPriceVo.getRetail());
		disQuantity.setStatus(packPriceVo.getStatus());
		disQuantity.setName(packPriceVo.getName());
	}
	
	public static void copyDramPlayItem(OpenDramaItem odi, DramaPlayItem item){
		odi.setName(item.getName());
		odi.setDramaid(item.getDramaid());
		odi.setTheatreid(item.getTheatreid());
		odi.setRoomid(item.getRoomid());
		odi.setRoomname(item.getRoomname());
		odi.setPlaytime(item.getPlaytime());
		odi.setEndtime(item.getEndtime());
		odi.setLanguage(item.getLanguage());
		odi.setPeriod(item.getPeriod());
		odi.setOpentype(item.getOpentype());
		odi.setSeller(item.getSeller());
		odi.setSellerseq(item.getSellerseq());
	}
}
