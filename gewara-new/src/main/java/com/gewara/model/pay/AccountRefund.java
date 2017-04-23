/**
 * 
 */
package com.gewara.model.pay;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

import com.gewara.constant.ticket.PartnerConstant;
import com.gewara.model.BaseObject;
/**
 * 账户原路退款
 * @author gebiao(ge.biao@gewara.com)
 * @since Aug 24, 2012 1:35:57 PM
 */
public class AccountRefund extends BaseObject {
	private static final long serialVersionUID = 6029378080103301843L;
	public static final String STATUS_APPLY = "apply";			//新申请
	public static final String STATUS_ACCEPT = "accept"; 		//已接收
	public static final String STATUS_UNACCEPT = "reject"; 		//不接收退款
	public static final String STATUS_FAIL = "fail"; 			//退款失败
	public static final String STATUS_DEBIT = "debit";			//账户扣款
	public static final String STATUS_SUCCESS = "success";		//返回第三方支付成功

	private static Map<String, String> textMap = new HashMap<String, String>();
	private static Map<String, String> wayMap = new HashMap<String, String>();
	private Long id;
	private Integer version;			//版本
	private String tradeno;		//订单号
	private String reason;		//原因
	private String status;		//状态
	private String origin;		//来源：refund:订单退款, apply:客服独立申请 charge 充值退款
	private Integer amount;		//退款金额
	private Long memberid;
	private Long partnerid;		//商家退款
	private String mobile;
	private Timestamp addtime;	//增加日期
	private Timestamp dealtime; //处理时间
	private String remark;		//特别说明
	private String paymethod;	//支付方式
	private Long applyuser;		//申请人
	private Long dealuser;	//处理人
	static{
		textMap.put(STATUS_APPLY, "新申请");
		textMap.put(STATUS_ACCEPT, "已接收");
		textMap.put(STATUS_UNACCEPT, "不接收退款");
		textMap.put(STATUS_FAIL, "退款失败");
		textMap.put(STATUS_SUCCESS, "退款成功");
	}
	public AccountRefund(Long memberid, String mobile){
		this.memberid = memberid;
		this.mobile = mobile;
		this.status = STATUS_APPLY;
		this.addtime = new Timestamp(System.currentTimeMillis());
		this.dealtime = new Timestamp(System.currentTimeMillis());
	}
	public AccountRefund(){
	}
	public AccountRefund(OrderRefund refund) {
		this.tradeno = refund.getTradeno();
		this.reason = refund.getReason();
		this.status = STATUS_ACCEPT;
		this.origin = "apply";
		this.amount = refund.getGewaRetAmount();
		this.memberid = refund.getMemberid();
		this.partnerid = refund.getPartnerid();
		this.mobile = refund.getMobile();
		this.applyuser = refund.getApplyuser();
		this.remark = refund.getApplyinfo();
		this.addtime = new Timestamp(System.currentTimeMillis());
		this.version = 0;
	}
	@Override
	public Serializable realId() {
		return id;
	}
	public Timestamp getAddtime() {
		return addtime;
	}
	public void setAddtime(Timestamp addtime) {
		this.addtime = addtime;
	}
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getTradeno() {
		return tradeno;
	}
	public void setTradeno(String tradeno) {
		this.tradeno = tradeno;
	}
	public String getReason() {
		return reason;
	}
	public void setReason(String reason) {
		this.reason = reason;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public Long getMemberid() {
		return memberid;
	}
	public void setMemberid(Long memberid) {
		this.memberid = memberid;
	}
	public String getMobile() {
		return mobile;
	}
	public void setMobile(String mobile) {
		this.mobile = mobile;
	}
	public String getStatusText(String sstatus){
		return textMap.get(sstatus);
	}
	public String getWayText(String way){
		return wayMap.get(way);
	}
	public Timestamp getDealtime() {
		return dealtime;
	}
	public void setDealtime(Timestamp dealtime) {
		this.dealtime = dealtime;
	}
	public String getRemark() {
		return remark;
	}
	public void setRemark(String remark) {
		this.remark = remark;
	}
	public String getOrigin() {
		return origin;
	}
	public void setOrigin(String origin) {
		this.origin = origin;
	}
	public String getPaymethod() {
		return paymethod;
	}
	public void setPaymethod(String paymethod) {
		this.paymethod = paymethod;
	}
	public Long getApplyuser() {
		return applyuser;
	}
	public void setApplyuser(Long applyuser) {
		this.applyuser = applyuser;
	}
	public Long getDealuser() {
		return dealuser;
	}
	public void setDealuser(Long dealuser) {
		this.dealuser = dealuser;
	}
	public Integer getAmount() {
		return amount;
	}
	public void setAmount(Integer amount) {
		this.amount = amount;
	}
	public Long getPartnerid() {
		return partnerid;
	}
	public void setPartnerid(Long partnerid) {
		this.partnerid = partnerid;
	}
	public Integer getVersion() {
		return version;
	}
	public void setVersion(Integer version) {
		this.version = version;
	}
	
	public boolean isOutPartner() {
		return memberid>PartnerConstant.MAX_MEMBERID;
	}
}
