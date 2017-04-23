package com.gewara.model.user;

import java.io.Serializable;
import java.sql.Timestamp;

import org.apache.commons.lang.StringUtils;

import com.gewara.constant.InvoiceConstant;
import com.gewara.model.BaseObject;

public class Invoice extends BaseObject {
	private static final long serialVersionUID = 3884062931033192928L;
	private Long id;
	private String title;//发票抬头
	private Long memberid;//用户id
	private Long adminid;//审核管理员id
	private String address;//邮寄地址
	private Integer amount;//开票金额
	private String phone;//电话
	private String postcode;//邮政编码
	private String contactor;//收件人
	private String invoicestatus;//发票领取状态
	private Timestamp addtime;//加入时间
	private Timestamp opentime;//开票时间
	private String invoicecontent;//备注
	private String invoicetype;//类型
	private String relatedid;//订单号
	private String postnumber;//邮寄号
	private String citycode;//发票的归属地
	//20110903
	private Timestamp posttime;//邮寄时间
	private String applytype;//申请类型
	
	private String pretype;			//跟订单orderExtra中 pretype一样
	
	public Invoice() {}
	
	public Invoice(Long memberid){
		this.addtime = new Timestamp(System.currentTimeMillis());
		this.invoicestatus = InvoiceConstant.STATUS_APPLY;
		this.memberid = memberid;
	}
	public Invoice(Long memberid, String address, Integer amount, String title, String phone, 
			String postcode, String contactor, String invoicetype, String orderid){
		this(memberid);
		this.address = address;
		this.amount = amount;
		this.title = title;
		this.phone = phone;
		this.postcode = postcode;
		this.contactor = contactor;
		this.invoicetype = invoicetype;
		this.relatedid = orderid;
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

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public Integer getAmount() {
		return amount;
	}

	public void setAmount(Integer amount) {
		this.amount = amount;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public String getPostcode() {
		return postcode;
	}

	public void setPostcode(String postcode) {
		this.postcode = postcode;
	}

	public String getContactor() {
		return contactor;
	}

	public void setContactor(String contactor) {
		this.contactor = contactor;
	}

	public Timestamp getAddtime() {
		return addtime;
	}

	public void setAddtime(Timestamp addtime) {
		this.addtime = addtime;
	}
	public String getInvoicestatus() {
		return invoicestatus;
	}
	public void setInvoicestatus(String invoicestatus) {
		this.invoicestatus = invoicestatus;
	}
	public Long getMemberid() {
		return memberid;
	}
	public void setMemberid(Long memberid) {
		this.memberid = memberid;
	}
	public Long getAdminid() {
		return adminid;
	}
	public void setAdminid(Long adminid) {
		this.adminid = adminid;
	}
	public String getInvoicecontent() {
		return invoicecontent;
	}
	public void setInvoicecontent(String invoicecontent) {
		this.invoicecontent = invoicecontent;
	}
	public String getRelatedid() {
		return relatedid;
	}
	public void setRelatedid(String relatedid) {
		this.relatedid = relatedid;
	}
	public String getPostnumber() {
		return postnumber;
	}
	public void setPostnumber(String postnumber) {
		this.postnumber = postnumber;
	}
	public String getStatusText(){
		return InvoiceConstant.STATUSDESC_MAP.get(invoicestatus);
	}
	public String getEnmobile(){
		if(StringUtils.length(phone)<=4) return phone;
		else return "*******" + phone.substring(phone.length()-4);
	}
	public String getCitycode() {
		return citycode;
	}
	public void setCitycode(String citycode) {
		this.citycode = citycode;
	}
	public Timestamp getPosttime() {
		return posttime;
	}
	public void setPosttime(Timestamp posttime) {
		this.posttime = posttime;
	}
	public String getApplytype() {
		return applytype;
	}
	public void setApplytype(String applytype) {
		this.applytype = applytype;
	}

	public String getInvoicetype() {
		return invoicetype;
	}
	public void setInvoicetype(String invoicetype) {
		this.invoicetype = invoicetype;
	}
	public Timestamp getOpentime() {
		return opentime;
	}
	public void setOpentime(Timestamp opentime) {
		this.opentime = opentime;
	}

	public String getPretype() {
		return pretype;
	}

	public void setPretype(String pretype) {
		this.pretype = pretype;
	}
	
}
