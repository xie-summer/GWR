package com.gewara.json.bbs;

/**
 * ÍÆ¼ö»î¶¯
 * @author user
 *
 */
public class RecommendActivity {
	
	private String logo;
	private String activityname;
	private Long activityid;
	
	public RecommendActivity(){
		
	}
	
	public RecommendActivity(Long activityid, String activityname, String logo){
		this.activityid = activityid;
		this.activityname = activityname;
		this.logo = logo;
	}
	
	public String getLogo() {
		return logo;
	}
	public void setLogo(String logo) {
		this.logo = logo;
	}
	public String getActivityname() {
		return activityname;
	}
	public void setActivityname(String activityname) {
		this.activityname = activityname;
	}
	public Long getActivityid() {
		return activityid;
	}
	public void setActivityid(Long activityid) {
		this.activityid = activityid;
	}

}
