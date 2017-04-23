package com.gewara.json;

import java.io.Serializable;
import java.util.Date;

public abstract class SeeOrder implements Serializable{
    private static final long serialVersionUID = 5774773240715376538L;
    protected String id;
    protected Long relatedid;
    protected String tag;
    protected Long memberid;
    protected String tradeNo;
    protected String adddate;
    protected Date paidtime;
    protected Date playDate;
    
    public SeeOrder(){}
    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }
    public Long getRelatedid() {
        return relatedid;
    }
    public void setRelatedid(Long relatedid) {
        this.relatedid = relatedid;
    }
    public String getTag() {
        return tag;
    }
    public void setTag(String tag) {
        this.tag = tag;
    }
    public Long getMemberid() {
        return memberid;
    }
    public void setMemberid(Long memberid) {
        this.memberid = memberid;
    }
    public String getTradeNo() {
        return tradeNo;
    }
    public void setTradeNo(String tradeNo) {
        this.tradeNo = tradeNo;
    }
    public String getAdddate() {
        return adddate;
    }
    public void setAdddate(String adddate) {
        this.adddate = adddate;
    }
    public Date getPaidtime() {
        return paidtime;
    }
    public void setPaidtime(Date paidtime) {
        this.paidtime = paidtime;
    }
	public Date getPlayDate() {
		return playDate;
	}
	public void setPlayDate(Date playDate) {
		this.playDate = playDate;
	}
    
}
