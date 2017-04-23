package com.gewara.model.common;

import java.io.Serializable;
import java.sql.Timestamp;

import com.gewara.model.BaseObject;

/**
 * 用来记录某种操作的时间戳
 * @author acerge(acerge@163.com)
 * @since 3:09:24 PM Oct 22, 2009
 */
public class UserOperation extends BaseObject{
	private static final long serialVersionUID = 540789762703761934L;
	public static final String TAG_EMAIL = "email";	//给用户发邮件
	public static final String TAG_FINDPASS = "findpass";
	private String opkey;
	private Integer opnum;									//操作次数
	private Integer refused;								//拒绝次数
	private Timestamp addtime;								//增加时间
	private Timestamp updatetime;							//最后一次时间	
	private Timestamp validtime;							//有效时间
	private String tag;										//分类
	private String secondkey;								//第二KEY，update不增加次数，直接返回
	public UserOperation(){}
	public UserOperation(String opkey, Timestamp addtime){
		this.opkey = opkey;
		this.refused = 0;
		this.opnum = 1;
		this.addtime = addtime;
		this.updatetime = addtime;
	}
	public UserOperation(String opkey, Timestamp addtime, String tag){
		this(opkey, addtime);
		this.tag = tag;
	}
	public UserOperation(String opkey, Timestamp addtime, Timestamp validtime, String tag){
		this.opkey = opkey;
		this.refused = 0;
		this.opnum = 1;
		this.addtime = addtime;
		this.updatetime = addtime;
		this.validtime = validtime;
		this.tag = tag;
	}
	public Integer getOpnum() {
		return opnum;
	}
	public void setOpnum(Integer opnum) {
		this.opnum = opnum;
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
	public String getTag() {
		return tag;
	}
	public void setTag(String tag) {
		this.tag = tag;
	}
	public String getOpkey() {
		return opkey;
	}
	public void setOpkey(String opkey) {
		this.opkey = opkey;
	}
	@Override
	public Serializable realId() {
		return opkey;
	}
	public Timestamp getValidtime() {
		return validtime;
	}
	public void setValidtime(Timestamp validtime) {
		this.validtime = validtime;
	}
	public Integer getRefused() {
		return refused;
	}
	public void setRefused(Integer refused) {
		this.refused = refused;
	}
	public String getSecondkey() {
		return secondkey;
	}
	public void setSecondkey(String secondkey) {
		this.secondkey = secondkey;
	}
}
