package com.gewara.command;

public class SearchFutureMovieCommand {
	public String thisWeek;
	public String nearlyTwoWeeks;
	public String type;
	public String order;
	public int pageNo=0;
	public int rowsPerPage = 10;
	public String moviename;
	public String getMoviename() {
		return moviename;
	}

	public void setMoviename(String moviename) {
		this.moviename = moviename;
	}

	public int getPageNo() {
		return pageNo;
	}

	public void setPageNo(int pageNo) {
		this.pageNo = pageNo;
	}

	public int getRowsPerPage() {
		return rowsPerPage;
	}

	public void setRowsPerPage(int rowsPerPage) {
		this.rowsPerPage = rowsPerPage;
	}

	public String getOrder() {
		return order;
	}

	public void setOrder(String order) {
		this.order = order;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getThisWeek() {
		return thisWeek;
	}

	public void setThisWeek(String thisWeek) {
		this.thisWeek = thisWeek;
	}

	public String getNearlyTwoWeeks() {
		return nearlyTwoWeeks;
	}

	public void setNearlyTwoWeeks(String nearlyTwoWeeks) {
		this.nearlyTwoWeeks = nearlyTwoWeeks;
	}
}
