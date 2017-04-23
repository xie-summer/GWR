package com.gewara.model.pay;

import java.io.Serializable;
import java.sql.Timestamp;

import org.apache.commons.lang.StringUtils;

import com.gewara.constant.Status;
import com.gewara.model.BaseObject;

/**
 * @author <a href="mailto:acerge@163.com">gebiao(acerge)</a>
 * @since 2007-9-28下午02:05:17
 */
public class MemberAccount extends BaseObject{
	private static final long serialVersionUID = 3832626162173359411L;
	private Long id;
	private Long memberid;
	private String realname;			//真实姓名
	private String phone;				//电话
	private Integer banlance;			//余额
	private String password;			//密码
	private String idcard; 				//身份证号
	private Timestamp addtime;			//增加时间
	private Timestamp updatetime;		//更新时间
	private Integer version;			//版本
	private Integer bankcharge;			//当前可退款金额
	private Integer othercharge;		//当前不可退款金额
	private Integer depositcharge;		//保证金
	private String encryidcard;			//加密证件号
	private String forbid;				//禁止
	private Integer certtype;			//证件类型
	private String emcontact;			//紧急联系人
	private String emmobile;			//紧急联系人手机号
	public String getEncryidcard() {
		return encryidcard;
	}
	public void setEncryidcard(String encryidcard) {
		this.encryidcard = encryidcard;
	}
	public MemberAccount() {			//只为默认使用
	}
	public MemberAccount(Long memberid){
		this.banlance = 0;
		this.addtime = new Timestamp(System.currentTimeMillis());
		this.updatetime = this.addtime;
		this.version = 0;
		this.memberid = memberid;
		this.bankcharge = 0;
		this.othercharge = 0;
		this.depositcharge = 0;
		this.forbid = Status.N;
	}
	public MemberAccount(Long memberid, String realname, String phone) {
		this(memberid);
		this.realname = realname;
		this.phone = phone;
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
	public Long getMemberid() {
		return memberid;
	}
	public void setMemberid(Long memberid) {
		this.memberid = memberid;
	}
	public String getPhone() {
		return phone;
	}
	public void setPhone(String phone) {
		this.phone = phone;
	}
	public Integer getBanlance() {
		return banlance;
	}
	public void setBanlance(Integer banlance) {
		this.banlance = banlance;
	}
	
	public Integer getDepositcharge() {
		return depositcharge;
	}
	public void setDepositcharge(Integer depositcharge) {
		this.depositcharge = depositcharge;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public Timestamp getAddtime() {
		return addtime;
	}
	public void setAddtime(Timestamp addtime) {
		this.addtime = addtime;
	}
	public Timestamp getUpdatetime() {
		return updatetime;
	}
	public void setUpdatetime(Timestamp updatetime) {
		this.updatetime = updatetime;
	}
	public String getRealname() {
		return realname;
	}
	public void setRealname(String realname) {
		this.realname = realname;
	}
	public boolean isIncomplete() {
		return "10470c3b4b1fed12c3baac014be15fac67c6e815".equals(password) ;
	}
	public void addBanlance(int fee) {
		banlance += fee;
	}
	public void addWabicharge(int fee) {
		othercharge += fee;
	}
	public void addBankcharge(int fee) {
		bankcharge += fee;
	}
	public void addDepositcharge(int fee){
		depositcharge += fee;
	}
	public String getIdcard() {
		return idcard;
	}
	public void setIdcard(String idcard) {
		this.idcard = idcard;
	}
	public boolean isNopassword(){
		return "10470c3b4b1fed12c3baac014be15fac67c6e815".equals(password) || StringUtils.isBlank(password); 
	}
	public Integer getVersion() {
		return version;
	}
	public void setVersion(Integer version) {
		this.version = version;
	}
	public Integer getBankcharge() {
		return bankcharge;
	}
	public void setBankcharge(Integer bankcharge) {
		this.bankcharge = bankcharge;
	}
	public Integer getOthercharge() {
		return othercharge;
	}
	public void setOthercharge(Integer othercharge) {
		this.othercharge = othercharge;
	}
	public String getForbid() {
		return forbid;
	}
	public void setForbid(String forbid) {
		this.forbid = forbid;
	}
	public Integer getCerttype() {
		return certtype;
	}
	public void setCerttype(Integer certtype) {
		this.certtype = certtype;
	}
	public String getEmcontact() {
		return emcontact;
	}
	public void setEmcontact(String emcontact) {
		this.emcontact = emcontact;
	}
	public String getEmmobile() {
		return emmobile;
	}
	public void setEmmobile(String emmobile) {
		this.emmobile = emmobile;
	}
	public boolean hasRights(){
		return !StringUtils.equals(forbid, Status.Y);
	}
}