package com.gewara.json.gewacup;

import java.io.Serializable;
import java.util.Date;


public class Players  implements Serializable{
	
	private static final long serialVersionUID = -5712451392142636506L;
	
	private String id;
	private String idcards;		//身份证
	private String player;		//参与人
	private String idcardslogo;	//身份证复印件
	private String phone;		
	private String sex;			//性别
	private Date addtime;		
	private String yearstype;	//举办年份
	private Long clubInfoId;	//俱乐部外键
	private Long memberid;		//报名人ID
	private String membername;
	private Long orderid;		//订单ID
	private String source;		//来源
	private String status;		//支付状态:专门为IPTV服务
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public Long getOrderid() {
		return orderid;
	}
	public String getSource() {
		return source;
	}
	public void setSource(String source) {
		this.source = source;
	}
	public void setOrderid(Long orderid) {
		this.orderid = orderid;
	}
	public String getMembername() {
		return membername;
	}
	public void setMembername(String membername) {
		this.membername = membername;
	}
	public Long getMemberid() {
		return memberid;
	}
	public void setMemberid(Long memberid) {
		this.memberid = memberid;
	}
	public String getPlayer() {
		return player;
	}
	public void setPlayer(String player) {
		this.player = player;
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
	public Long getClubInfoId() {
		return clubInfoId;
	}
	public void setClubInfoId(Long clubInfoId) {
		this.clubInfoId = clubInfoId;
	}
	public String getSex() {
		return sex;
	}
	public void setSex(String sex) {
		this.sex = sex;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
}