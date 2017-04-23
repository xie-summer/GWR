package com.gewara.command;

public class SearchOpiCommand {
	public Long movieid;
	public String fyrq;// 放映日期代码

	public Long getMovieid() {
		return movieid;
	}

	public void setMovieid(Long movieid) {
		this.movieid = movieid;
	}

	public String getFyrq() {
		return fyrq;
	}

	public void setFyrq(String fyrq) {
		this.fyrq = fyrq;
	}

}
