package com.gewara.json;

import java.io.Serializable;

public class MovieTrendsCount implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 4983321452494264694L;
	private String id;
	// 统计日期
	private String countDate;
	private String countTime;
	// 电影ID
	private Long movieId;
	private String moviename;
	// 上映日期
	private Long releasedate;
	// 关注数
	private Long clicktimesCount;

	// 想看数（待上映）&
	private Long collectedtimesCountN;
	// 感兴趣数（上映的）
	private Long collectedtimesCountY;
	// 影片购票数
	private Long boughtcount;
	// 用户评分（未）
	private Long memberMarkCountN;
	// 用户评分（购）
	private Long memberMarkCountY;
	// 哇啦数
	private Long walaCount;
	// 影评
	private Long diaryCount;
	// 更新时间
	private Long updatetime;
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getCountDate() {
		return countDate;
	}
	public void setCountDate(String countDate) {
		this.countDate = countDate;
	}
	public String getCountTime() {
		return countTime;
	}
	public void setCountTime(String countTime) {
		this.countTime = countTime;
	}
	public Long getMovieId() {
		return movieId;
	}
	public void setMovieId(Long movieId) {
		this.movieId = movieId;
	}
	public String getMoviename() {
		return moviename;
	}
	public void setMoviename(String moviename) {
		this.moviename = moviename;
	}
	public Long getReleasedate() {
		return releasedate;
	}
	public void setReleasedate(Long releasedate) {
		this.releasedate = releasedate;
	}
	public Long getClicktimesCount() {
		return clicktimesCount;
	}
	public void setClicktimesCount(Long clicktimesCount) {
		this.clicktimesCount = clicktimesCount;
	}
	public Long getCollectedtimesCountN() {
		return collectedtimesCountN;
	}
	public void setCollectedtimesCountN(Long collectedtimesCountN) {
		this.collectedtimesCountN = collectedtimesCountN;
	}
	public Long getCollectedtimesCountY() {
		return collectedtimesCountY;
	}
	public void setCollectedtimesCountY(Long collectedtimesCountY) {
		this.collectedtimesCountY = collectedtimesCountY;
	}
	public Long getBoughtcount() {
		return boughtcount;
	}
	public void setBoughtcount(Long boughtcount) {
		this.boughtcount = boughtcount;
	}
	public Long getMemberMarkCountN() {
		return memberMarkCountN;
	}
	public void setMemberMarkCountN(Long memberMarkCountN) {
		this.memberMarkCountN = memberMarkCountN;
	}
	public Long getMemberMarkCountY() {
		return memberMarkCountY;
	}
	public void setMemberMarkCountY(Long memberMarkCountY) {
		this.memberMarkCountY = memberMarkCountY;
	}
	public Long getWalaCount() {
		return walaCount;
	}
	public void setWalaCount(Long walaCount) {
		this.walaCount = walaCount;
	}
	public Long getDiaryCount() {
		return diaryCount;
	}
	public void setDiaryCount(Long diaryCount) {
		this.diaryCount = diaryCount;
	}
	public Long getUpdatetime() {
		return updatetime;
	}
	public void setUpdatetime(Long updatetime) {
		this.updatetime = updatetime;
	}

	}
