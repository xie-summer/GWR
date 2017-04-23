package com.gewara.web.action.api2.movie;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class CinemaPlayItem implements Serializable {

	private static final long serialVersionUID = 1L;

	private List<Moive> data = new ArrayList<Moive>();

	public CinemaPlayItem() {
	}

	public List<Moive> getData() {
		return data;
	}

	public void setData(List<Moive> data) {
		this.data = data;
	}

	public static class Moive {
		private Long id;
		private String moviename;
		private String logo;
		private String type;
		private String director;
		private String actors;
		private Integer videolen;
		private Date releasedate;
		private List<TimeTable> timeTables = new ArrayList<TimeTable>();

		public Moive() {
		}

		public Long getId() {
			return id;
		}

		public void setId(Long id) {
			this.id = id;
		}

		public String getMoviename() {
			return moviename;
		}

		public void setMoviename(String moviename) {
			this.moviename = moviename;
		}

		public String getLogo() {
			return logo;
		}

		public void setLogo(String logo) {
			this.logo = logo;
		}

		public String getType() {
			return type;
		}

		public void setType(String type) {
			this.type = type;
		}

		public String getDirector() {
			return director;
		}

		public void setDirector(String director) {
			this.director = director;
		}

		public String getActors() {
			return actors;
		}

		public void setActors(String actors) {
			this.actors = actors;
		}

		public Integer getVideolen() {
			return videolen;
		}

		public void setVideolen(Integer videolen) {
			this.videolen = videolen;
		}

		public Date getReleasedate() {
			return releasedate;
		}

		public void setReleasedate(Date releasedate) {
			this.releasedate = releasedate;
		}

		public List<TimeTable> getTimeTables() {
			return timeTables;
		}

		public void setTimeTables(List<TimeTable> timeTables) {
			this.timeTables = timeTables;
		}

	}

	public static class TimeTable {
		private Long id;
		private String language; // 语言
		private Date playdate; // 放映日期
		private String playtime; // 放映时间
		private Integer price; // 现价
		private Integer origin_price;
		private String edition; // 版本
		private String playroom; // 放映厅名称
		private String opiurl;

		public TimeTable() {
		}

		public Long getId() {
			return id;
		}

		public void setId(Long id) {
			this.id = id;
		}

		public String getLanguage() {
			return language;
		}

		public void setLanguage(String language) {
			this.language = language;
		}

		public Date getPlaydate() {
			return playdate;
		}

		public void setPlaydate(Date playdate) {
			this.playdate = playdate;
		}

		public String getPlaytime() {
			return playtime;
		}

		public void setPlaytime(String playtime) {
			this.playtime = playtime;
		}

		public Integer getPrice() {
			return price;
		}

		public void setPrice(Integer price) {
			this.price = price;
		}

		public String getEdition() {
			return edition;
		}

		public void setEdition(String edition) {
			this.edition = edition;
		}

		public String getPlayroom() {
			return playroom;
		}

		public void setPlayroom(String playroom) {
			this.playroom = playroom;
		}

		public String getOpiurl() {
			return opiurl;
		}

		public void setOpiurl(String opiurl) {
			this.opiurl = opiurl;
		}

		public Integer getOrigin_price() {
			return origin_price;
		}

		public void setOrigin_price(Integer origin_price) {
			this.origin_price = origin_price;
		}

	}

}
