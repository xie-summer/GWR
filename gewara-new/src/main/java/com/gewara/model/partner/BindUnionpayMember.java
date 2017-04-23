package com.gewara.model.partner;

import java.io.Serializable;

public class BindUnionpayMember implements Serializable{
	//TODO:移走
	private static final long serialVersionUID = -4363300428929625915L;
	private String _id;
	private Long memberId;
	private String usrState;//用户状态：0:预注册用户，1:正式注册用，2:快捷支付用户
	private String notifyType;// 通知结果
	private String cardNo;//对于快捷支付用户，绑定卡之后，会将卡号进行md5后返回，用于相同卡重复注册使用（重复控制有外围机构控制） 
	private String addTime;
	
	public String getAddTime() {
		return addTime;
	}
	public void setAddTime(String addTime) {
		this.addTime = addTime;
	}
	public String get_id() {
		return _id;
	}
	public void set_id(String _id) {
		this._id = _id;
	}
	public Long getMemberId() {
		return memberId;
	}
	public void setMemberId(Long memberId) {
		this.memberId = memberId;
	}
	public String getUsrState() {
		return usrState;
	}
	public void setUsrState(String usrState) {
		this.usrState = usrState;
	}
	public String getNotifyType() {
		return notifyType;
	}
	public void setNotifyType(String notifyType) {
		this.notifyType = notifyType;
	}
	public String getCardNo() {
		return cardNo;
	}
	public void setCardNo(String cardNo) {
		this.cardNo = cardNo;
	}
	

}
