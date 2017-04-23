package com.gewara.helper.order;

import java.util.List;

import com.gewara.model.movie.Movie;
import com.gewara.model.pay.GoodsOrder;
import com.gewara.model.pay.TicketOrder;
import com.gewara.model.ticket.OpenPlayItem;
import com.gewara.model.ticket.OpenSeat;
import com.gewara.model.ticket.SellSeat;

public class TicketOrderContainer extends OrderContainer{
	private List<SellSeat> seatList;
	private List<OpenSeat> oseatList;
	private OpenPlayItem opi;
	private GoodsOrder goodsOrder;
	private Movie movie;
	
	public TicketOrderContainer(TicketOrder order){
		this.order = order;
	}
	public TicketOrderContainer(TicketOrder order, OpenPlayItem opi, List<SellSeat> seatList, List<OpenSeat> oseatList){
		this.order = order;
		this.opi = opi;
		this.seatList = seatList;
		this.oseatList = oseatList;
	}
	
	public TicketOrder getTicketOrder() {
		return (TicketOrder) order;
	}
	public List<SellSeat> getSeatList() {
		return seatList;
	}
	public void setSeatList(List<SellSeat> seatList) {
		this.seatList = seatList;
	}
	public List<OpenSeat> getOseatList() {
		return oseatList;
	}
	public void setOseatList(List<OpenSeat> oseatList) {
		this.oseatList = oseatList;
	}
	public OpenPlayItem getOpi() {
		return opi;
	}
	public void setOpi(OpenPlayItem opi) {
		this.opi = opi;
	}
	public Movie getMovie() {
		return movie;
	}
	public void setMovie(Movie movie) {
		this.movie = movie;
	}
	public GoodsOrder getGoodsOrder() {
		return goodsOrder;
	}
	public void setGoodsOrder(GoodsOrder goodsOrder) {
		this.goodsOrder = goodsOrder;
	}
}
