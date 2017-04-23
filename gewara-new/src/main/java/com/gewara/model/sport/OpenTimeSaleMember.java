package com.gewara.model.sport;

import java.io.Serializable;
import java.sql.Timestamp;

import com.gewara.model.BaseObject;
import com.gewara.util.DateUtil;

public class OpenTimeSaleMember extends BaseObject {
	private static final long serialVersionUID = -4076325803242996881L;
	private Long id;
	private Long otsid;
	private Long memberid;
	private String nickname;
	private Integer price;
	private Integer dupprice;
	private Timestamp addtime;
	
	public OpenTimeSaleMember() {}
	
	public OpenTimeSaleMember(Long memberid){
		this.addtime = DateUtil.getCurFullTimestamp();
		this.memberid = memberid;
	}
	public OpenTimeSaleMember(Long otsid, Long memberid, String nickname, Integer price){
		this(memberid);
		this.nickname = nickname;
		this.otsid = otsid;
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


	public Long getOtsid() {
		return otsid;
	}

	public void setOtsid(Long otsid) {
		this.otsid = otsid;
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
	public String getNickname() {
		return nickname;
	}

	public void setNickname(String nickname) {
		this.nickname = nickname;
	}

	public Integer getPrice() {
		return price;
	}
	public void setPrice(Integer price) {
		this.price = price;
	}

	public Integer getDupprice() {
		return dupprice;
	}

	public void setDupprice(Integer dupprice) {
		this.dupprice = dupprice;
	}
}
