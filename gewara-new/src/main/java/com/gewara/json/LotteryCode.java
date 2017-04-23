package com.gewara.json;

import java.io.Serializable;

public class LotteryCode implements Serializable{
	private static final long serialVersionUID = 8243025802888248477L;
	private Long id;
	private Long memberid;
	private String lotteryno;
	private String status;

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getMemberid() {
		return memberid;
	}

	public void setMemberid(Long memberid) {
		this.memberid = memberid;
	}

	public String getLotteryno() {
		return lotteryno;
	}

	public void setLotteryno(String lotteryno) {
		this.lotteryno = lotteryno;
	}
}
