package com.gewara.model.pay;

import java.io.Serializable;
import java.sql.Timestamp;

import com.gewara.model.BaseObject;

public class PubMember extends BaseObject {
	private static final long serialVersionUID = -1233410827294999301L;
	private Long id;
	private Long pubid;
	private Long memberid;
	private Integer price;
	private Timestamp addtime;
	private Integer pointvalue;
	
	public PubMember() {}
	
	public PubMember(Long memberid){
		this.addtime = new Timestamp(System.currentTimeMillis());
		this.memberid = memberid;
	}
	public PubMember(Long pubid, Long memberid, Integer price){
		this(memberid);
		this.pubid = pubid;
		this.price = price;
	}
	@Override
	public Serializable realId() {
		return id;
	}
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getPubid() {
		return pubid;
	}

	public void setPubid(Long pubid) {
		this.pubid = pubid;
	}

	public Timestamp getAddtime() {
		return addtime;
	}

	public void setAddtime(Timestamp addtime) {
		this.addtime = addtime;
	}
	public Long getMemberid() {
		return memberid;
	}

	public void setMemberid(Long memberid) {
		this.memberid = memberid;
	}
	public Integer getPrice() {
		return price;
	}
	public void setPrice(Integer price) {
		this.price = price;
	}
	public Double getRprice(){
		Double d = price/100.00;
		return d;
	}

	public Integer getPointvalue() {
		return pointvalue;
	}

	public void setPointvalue(Integer pointvalue) {
		this.pointvalue = pointvalue;
	}
}
