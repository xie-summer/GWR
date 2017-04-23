package com.gewara.json.gewacup;

import java.io.Serializable;
import java.util.Date;


public class ClubInfo implements Serializable{
	
	private static final long serialVersionUID = 4045168091886382589L;
	private Long id;			//圈子ID主键
	private String communame;	//圈子名字
	private String contact;		//联系人
	private String idcards;		//身份证
	private String idcardslogo;	//身份证复印件
	private String phone;		//联系人电话
	private Date addtime;	//添加时间
	private String yearstype;	//举办年份
	private Long memberid;		//报名人ID
	private String membername;
	private Long orderid;		//订单号
	public Long getMemberid() {
		return memberid;
	}
	public void setMemberid(Long memberid) {
		this.memberid = memberid;
	}
	public String getMembername() {
		return membername;
	}
	public void setMembername(String membername) {
		this.membername = membername;
	}
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getContact() {
		return contact;
	}
	public void setContact(String contact) {
		this.contact = contact;
	}
	public String getIdcards() {
		return idcards;
	}
	public void setIdcards(String idcards) {
		this.idcards = idcards;
	}
	public String getIdcardslogo() {
		return idcardslogo;
	}
	public void setIdcardslogo(String idcardslogo) {
		this.idcardslogo = idcardslogo;
	}
	public String getPhone() {
		return phone;
	}
	public void setPhone(String phone) {
		this.phone = phone;
	}
	public Date getAddtime() {
		return addtime;
	}
	public void setAddtime(Date addtime) {
		this.addtime = addtime;
	}
	public String getYearstype() {
		return yearstype;
	}
	public void setYearstype(String yearstype) {
		this.yearstype = yearstype;
	}
	public Long getOrderid() {
		return orderid;
	}
	public void setOrderid(Long orderid) {
		this.orderid = orderid;
	}
	public String getCommuname() {
		return communame;
	}
	public void setCommuname(String communame) {
		this.communame = communame;
	}
}