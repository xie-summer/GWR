package com.gewara.json;


public class ViewFilmSchedule {
	
	public static final String TYPE_MOVIE_FILMFEST = "movie";
	public static final String TYPE_SCHEDULE_FILMFEST = "schedule";
	
	private String _id;
	
	private Long mpid;
	
	private Long movieId;
	
	private Long memberId;
	
	private String addTime;
	
	private String playTime;
	
	private String type;// movie ∆¨µ•  schedule »’≥Ã 
	
	private String source;

	public ViewFilmSchedule(){}
	
	public ViewFilmSchedule(String type,Long mpid,Long movieId,Long memberId){
		this.type = type;
		this.mpid = mpid;
		this.movieId = movieId;
		this.memberId = memberId;
	}
	
	public String get_id() {
		return _id;
	}

	public void set_id(String _id) {
		this._id = _id;
	}

	public Long getMpid() {
		return mpid;
	}

	public void setMpid(Long mpid) {
		this.mpid = mpid;
	}

	public Long getMovieId() {
		return movieId;
	}

	public void setMovieId(Long movieId) {
		this.movieId = movieId;
	}

	public Long getMemberId() {
		return memberId;
	}

	public void setMemberId(Long memberId) {
		this.memberId = memberId;
	}

	public String getAddTime() {
		return addTime;
	}

	public void setAddTime(String addTime) {
		this.addTime = addTime;
	}

	public String getPlayTime() {
		return playTime;
	}

	public void setPlayTime(String playTime) {
		this.playTime = playTime;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
	}
	
}
