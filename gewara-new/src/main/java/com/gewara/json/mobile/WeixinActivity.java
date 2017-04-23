package com.gewara.json.mobile;


public class WeixinActivity {
	public static String TEMPLATE_ID = "20130124000000";
	private String id;
	private String replynum;
	private String activityid;
	private Integer rank;
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getReplynum() {
		return replynum;
	}
	public void setReplynum(String replynum) {
		this.replynum = replynum;
	}
	
	public String getActivityid() {
		return activityid;
	}
	public void setActivityid(String activityid) {
		this.activityid = activityid;
	}

	public Integer getRank() {
		return rank;
	}
	public void setRank(Integer rank) {
		this.rank = rank;
	}
}
