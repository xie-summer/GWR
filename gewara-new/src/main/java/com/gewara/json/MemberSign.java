package com.gewara.json;

import java.io.Serializable;

public class MemberSign implements Serializable{
	private static final long serialVersionUID = -8263369965712270305L;
	private Long memberid;
	private double pointx;
	private double pointy;		
	private double bpointx;		//百度坐标
	private double bpointy;		//百度坐标
	private String address;
	private Long signtime;
	public MemberSign(){}
	public MemberSign(Long memberid){
		this.memberid = memberid;
		this.signtime = System.currentTimeMillis();
	}
	public Long getMemberid() {
		return memberid;
	}
	public void setMemberid(Long memberid) {
		this.memberid = memberid;
	}
	public double getPointx() {
		return pointx;
	}
	public void setPointx(double pointx) {
		this.pointx = pointx;
	}
	public double getPointy() {
		return pointy;
	}
	public void setPointy(double pointy) {
		this.pointy = pointy;
	}
	public Long getSigntime() {
		return signtime;
	}
	public void setSigntime(Long signtime) {
		this.signtime = signtime;
	}
	public double getBpointx() {
		return bpointx;
	}
	public void setBpointx(double bpointx) {
		this.bpointx = bpointx;
	}
	public double getBpointy() {
		return bpointy;
	}
	public void setBpointy(double bpointy) {
		this.bpointy = bpointy;
	}
	public String getAddress() {
		return address;
	}
	public void setAddress(String address) {
		this.address = address;
	}
}
