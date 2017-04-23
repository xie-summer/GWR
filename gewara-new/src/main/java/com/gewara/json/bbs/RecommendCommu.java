package com.gewara.json.bbs;

/**
 * ÍÆ¼öÈ¦×Ó
 * @author user
 *
 */
public class RecommendCommu {
	private Long commumembercount;
	private String headpicUrl;
	private String communame;
	private String memberids;
	private Long commuid;
	
	public RecommendCommu(){
		
	}
	
	public RecommendCommu(Long commuid, String communame, Long commumembercount,String headpicUrl, String memberids){
		this.commuid = commuid;
		this.communame = communame;
		this.commumembercount = commumembercount;
		this.headpicUrl = headpicUrl;
		this.memberids = memberids;
	}
	
	public Long getCommumembercount() {
		return commumembercount;
	}
	public void setCommumembercount(Long commumembercount) {
		this.commumembercount = commumembercount;
	}
	public String getHeadpicUrl() {
		return headpicUrl;
	}
	public void setHeadpicUrl(String headpicUrl) {
		this.headpicUrl = headpicUrl;
	}
	public String getCommuname() {
		return communame;
	}
	public void setCommuname(String communame) {
		this.communame = communame;
	}
	public String getMemberids() {
		return memberids;
	}
	public void setMemberids(String memberids) {
		this.memberids = memberids;
	}
	public Long getCommuid() {
		return commuid;
	}
	public void setCommuid(Long commuid) {
		this.commuid = commuid;
	}
}
