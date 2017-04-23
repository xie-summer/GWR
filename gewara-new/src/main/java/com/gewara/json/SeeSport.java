package com.gewara.json;

import java.util.Date;

import com.gewara.util.DateUtil;

public class SeeSport extends SeeOrder {
	private static final long serialVersionUID = 4636123810289609605L;
	public SeeSport() {
	}
	public SeeSport(Long relatedid, String tag, Long memberid, String tradeNo, Date paidtime, Date playDate) {
		this.relatedid = relatedid;
		this.tag = tag;
		this.memberid = memberid;
		this.tradeNo = tradeNo;
		this.paidtime = paidtime;
		this.adddate = DateUtil.formatDate(paidtime);
		this.playDate = playDate;
	}
}
