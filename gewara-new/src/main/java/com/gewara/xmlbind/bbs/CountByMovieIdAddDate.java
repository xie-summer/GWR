package com.gewara.xmlbind.bbs;

/**
 * 影片ID、日期区间获取影片每天哇啦数
 */
public class CountByMovieIdAddDate {
	private String movieId;
	private String addDay;
	private Long count;
	public String getAddDay() {
		return addDay;
	}
	public void setAddDay(String addDay) {
		this.addDay = addDay;
	}
	public Long getCount() {
		return count;
	}
	public void setCount(Long count) {
		this.count = count;
	}
	public String getMovieId() {
		return movieId;
	}
	public void setMovieId(String movieId) {
		this.movieId = movieId;
	}
}
