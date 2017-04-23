package com.gewara.json.bbs;

/**
 * ÍÆ¼öÓÃ»§
 * 
 * @author user
 * 
 */
public class RecommendPerson {
	private String headpic;
	private Long memberid;
	private String reason;
	private String membername;

	public RecommendPerson() {

	}

	public RecommendPerson(Long memberid, String membername, String headpic, String reason) {
		this.memberid = memberid;
		this.membername = membername;
		this.headpic = headpic;
		this.reason = reason;
	}

	public String getHeadpic() {
		return headpic;
	}

	public void setHeadpic(String headpic) {
		this.headpic = headpic;
	}

	public Long getMemberid() {
		return memberid;
	}

	public void setMemberid(Long memberid) {
		this.memberid = memberid;
	}

	public String getReason() {
		return reason;
	}

	public void setReason(String reason) {
		this.reason = reason;
	}

	public String getMembername() {
		return membername;
	}

	public void setMembername(String membername) {
		this.membername = membername;
	}
}
