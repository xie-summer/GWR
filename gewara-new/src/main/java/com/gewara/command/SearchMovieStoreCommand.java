package com.gewara.command;

public class SearchMovieStoreCommand {
	public String movietype;
	public String moviestate;
	public String movietime;
	public String order;
	public String playtype;
	public Integer pageNo=0;
	public Integer maxNum = 10;
	public String searchkey;
	public Integer getPageNo() {
		return pageNo;
	}
	public void setPageNo(Integer pageNo) {
		this.pageNo = pageNo;
	}
	public String getMovietype() {
		return movietype;
	}
	public void setMovietype(String movietype) {
		this.movietype = movietype;
	}
	public String getMoviestate() {
		return moviestate;
	}
	public void setMoviestate(String moviestate) {
		this.moviestate = moviestate;
	}
	public String getMovietime() {
		return movietime;
	}
	public void setMovietime(String movietime) {
		this.movietime = movietime;
	}
	public String getOrder() {
		return order;
	}
	public void setOrder(String order) {
		this.order = order;
	}
	public String getPlaytype() {
		return playtype;
	}
	public void setPlaytype(String playtype) {
		this.playtype = playtype;
	}
	public String getSearchkey() {
		return searchkey;
	}
	public void setSearchkey(String searchkey) {
		this.searchkey = searchkey;
	}

	
}
