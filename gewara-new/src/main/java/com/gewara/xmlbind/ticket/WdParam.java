package com.gewara.xmlbind.ticket;

import com.gewara.xmlbind.BaseInnerResponse;

public class WdParam extends BaseInnerResponse{
	private String userCode;
	private String cinemaId;
	private String showDate;
	private String filmPK;
	private String showId;
	private String wdUserId;
	public String getUserCode() {
		return userCode;
	}
	public void setUserCode(String userCode) {
		this.userCode = userCode;
	}
	public String getCinemaId() {
		return cinemaId;
	}
	public void setCinemaId(String cinemaId) {
		this.cinemaId = cinemaId;
	}
	public String getShowDate() {
		return showDate;
	}
	public void setShowDate(String showDate) {
		this.showDate = showDate;
	}
	public String getFilmPK() {
		return filmPK;
	}
	public void setFilmPK(String filmPK) {
		this.filmPK = filmPK;
	}
	public String getShowId() {
		return showId;
	}
	public void setShowId(String showId) {
		this.showId = showId;
	}
	public String getWdUserId() {
		return wdUserId;
	}
	public void setWdUserId(String wdUserId) {
		this.wdUserId = wdUserId;
	}
	
}
