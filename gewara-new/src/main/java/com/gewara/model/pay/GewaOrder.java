package com.gewara.model.pay;

import java.io.Serializable;
import java.sql.Timestamp;

import org.apache.commons.lang.StringUtils;

import com.gewara.constant.PaymethodConstant;
import com.gewara.constant.ticket.OrderConstant;
import com.gewara.constant.ticket.PartnerConstant;
import com.gewara.helper.order.GewaOrderHelper;
import com.gewara.model.BaseObject;
import com.gewara.util.DateUtil;
import com.gewara.util.JsonUtils;

public abstract class GewaOrder extends BaseObject{
	private static final long serialVersionUID = 4914995483381697551L;
	public static final String RESTATUS_DELETE = "D";// 订单删除状态
	//交易状态
	protected Long id;					//ID
	protected Integer version;			//更新版本
	protected String ordertitle;		//订单标题
	protected String tradeNo;			//订单号
	protected String mobile;			//联系手机
	protected Timestamp createtime;		//用户下单时间
	protected Timestamp addtime;		//增加时间：第一次创建时间、待处理创建时间、火凤凰锁定时间
	protected Timestamp updatetime;		//用户修改时间
	protected Timestamp validtime;		//有效时间
	protected Timestamp paidtime;		//付款时间
	protected Timestamp modifytime;		//工作人员修改
	protected Timestamp playtime;		//场次时间
	protected String status;			//付款状态
	protected Long memberid;			//关联用户
	protected Long partnerid;			//关联商家
	protected String membername;		//用户名/单位代码
	protected String paymethod;			//支付方法:站内账户、淘宝余额、银行支付
	protected String paybank;			//支付银行
	protected String payseqno;			//外部订单号
	protected String description2;		//商品描述
	protected Long clerkid;				//订单经办人
	protected String remark;			//特别说明
	protected Integer gewapaid;			//账户余额支付的金额
	protected Integer alipaid;			//淘宝或汇付支付的金额
	protected Integer wabi;				//瓦币消费
	protected Integer totalcost;		//总成本价
	protected Integer totalfee;			//订单总金额
	protected Integer discount;			//订单优惠
	protected String disreason;			//优惠理由
	protected String changehis;			//操作历史记录
	protected Integer unitprice;		//单价
	protected Integer quantity;			//数量
	protected String ukey;				//标识Partner订单唯一用户
	protected String checkpass;			//取票密码
	protected Integer itemfee;			//订单附属品总价
	protected String otherinfo;			//其他信息
	protected String citycode;			//城市代码
	protected Integer otherfee;			//手续费
	protected String settle;			//是否与第三方结算：Y，N
	protected String restatus;			//是否删除
	protected String pricategory;		//订单分类（模块）
	protected String category;			//订单类别
	protected String otherFeeRemark;	//手续费明细
	protected String express;
	
	private String gatewayCode;//支付网关代码	
	private String merchantCode;//商户号标识
	
	@Override
	public Serializable realId() {
		return id;
	}
	
	public String getCitycode() {
		return citycode;
	}
	public void setCitycode(String citycode) {
		this.citycode = citycode;
	}
	public Integer getItemfee() {
		return itemfee;
	}
	public void setItemfee(Integer itemfee) {
		this.itemfee = itemfee;
	}
	public void setCheckpass(String checkpass) {
		this.checkpass = checkpass;
	}

	public Integer getUnitprice() {
		return unitprice;
	}
	public void setUnitprice(Integer unitprice) {
		this.unitprice = unitprice;
	}
	public Integer getQuantity() {
		return quantity;
	}
	public void setQuantity(Integer quantity) {
		this.quantity = quantity;
	}
	public String getMobile() {
		return mobile;
	}
	public void setMobile(String mobile) {
		this.mobile = mobile;
	}
	public Timestamp getAddtime() {
		return addtime;
	}
	public void setAddtime(Timestamp addtime) {
		this.addtime = addtime;
	}
	public String getTradeNo() {
		return tradeNo;
	}
	public void setTradeNo(String tradeNo) {
		this.tradeNo = tradeNo;
	}
	public Long getMemberid() {
		return memberid;
	}
	public void setMemberid(Long memberid) {
		this.memberid = memberid;
	}
	public Timestamp getUpdatetime() {
		return updatetime;
	}
	public void setUpdatetime(Timestamp updatetime) {
		this.updatetime = updatetime;
	}
	public String getPricategory() {
		return pricategory;
	}

	public void setPricategory(String pricategory) {
		this.pricategory = pricategory;
	}

	public String getCheckpass() {
		return checkpass;
	}
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public void setPaymethod(String paymethod) {
		this.paymethod = paymethod;
	}
	public String getPaymethod() {
		return paymethod;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public Timestamp getValidtime() {
		return validtime;
	}
	public void setValidtime(Timestamp validtime) {
		this.validtime = validtime;
	}
	public String getOrdertitle() {
		return ordertitle;
	}
	public void setOrdertitle(String ordertitle) {
		this.ordertitle = ordertitle;
	}
	public Long getClerkid() {
		return clerkid;
	}
	public void setClerkid(Long clerkid) {
		this.clerkid = clerkid;
	}
	public String getRemark() {
		return remark;
	}
	public void setRemark(String remark) {
		this.remark = remark;
	}
	public Integer getAlipaid() {
		return alipaid;
	}
	public void setAlipaid(Integer alipaid) {
		this.alipaid = alipaid;
	}

	public String getPaymethodText(){
		return PaymethodConstant.getPaymethodText(paymethod);
	}
	public Integer getGewapaid() {
		return gewapaid;
	}
	public void setGewapaid(Integer gewapaid) {
		this.gewapaid = gewapaid;
	}
	
	public Timestamp getPaidtime() {
		return paidtime;
	}
	public void setPaidtime(Timestamp paidtime) {
		this.paidtime = paidtime;
	}
	public Timestamp getModifytime() {
		return modifytime;
	}
	public void setModifytime(Timestamp modifytime) {
		this.modifytime = modifytime;
	}
	public boolean isNetPaid(){
		return alipaid > 0;
	}
	//应付款
	public Integer getDue(){
		return totalfee + itemfee  + otherfee  - discount < 0? 0:totalfee + itemfee + otherfee - discount;
	}
	public Integer getTotalAmount(){
		return totalfee + itemfee + otherfee;
	}
	public String getPaybank() {
		return paybank;
	}
	public void setPaybank(String paybank) {
		this.paybank = paybank;
	}
	public abstract String getOrdertype();
	public String getPayseqno() {
		return payseqno;
	}
	public void setPayseqno(String payseqno) {
		this.payseqno = payseqno;
	}
	public Integer getDiscount() {
		return discount;
	}
	public void setDiscount(Integer discount) {
		this.discount = discount;
	}
	public String getChangehis() {
		return changehis;
	}
	public void setChangehis(String changehis) {
		this.changehis = changehis;
	}
	public void addChangehis(String name, String change) {
		String result = JsonUtils.addJsonKeyValue(changehis, name, change);
		this.changehis = result;
	}
	public Integer getTotalfee() {
		return totalfee;
	}
	public void setTotalfee(Integer totalfee) {
		this.totalfee = totalfee;
	}
	public String getDisreason() {
		return disreason;
	}
	public void setDisreason(String disreason) {
		this.disreason = disreason;
	}
	public String getMembername() {
		return membername;
	}
	public void setMembername(String membername) {
		this.membername = membername;
	}
	public String getUkey() {
		return ukey;
	}
	public void setUkey(String ukey) {
		this.ukey = ukey;
	}
	public Long getPartnerid() {
		return partnerid;
	}
	public Integer getTotalcost() {
		return totalcost;
	}
	public void setTotalcost(Integer totalcost) {
		this.totalcost = totalcost;
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
	public String getOtherinfo() {
		return otherinfo;
	}
	public void setOtherinfo(String otherinfo) {
		this.otherinfo = otherinfo;
	}
	public String getDescription2() {
		return description2;
	}
	public void setDescription2(String description2) {
		this.description2 = description2;
	}
	public Timestamp getCreatetime() {
		return createtime;
	}
	public void setCreatetime(Timestamp createtime) {
		this.createtime = createtime;
	}
	public Integer getOtherfee() {
		return otherfee;
	}
	public void setOtherfee(Integer otherfee) {
		this.otherfee = otherfee;
	}

	public String getSettle() {
		return settle;
	}

	public void setSettle(String settle) {
		this.settle = settle;
	}

	public Integer getWabi() {
		return wabi;
	}

	public void setWabi(Integer wabi) {
		this.wabi = wabi;
	}
	public String getRestatus() {
		return restatus;
	}

	public void setRestatus(String restatus) {
		this.restatus = restatus;
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public String getOtherFeeRemark() {
		return otherFeeRemark;
	}

	public void setOtherFeeRemark(String otherFeeRemark) {
		this.otherFeeRemark = otherFeeRemark;
	}

	public Timestamp getPlaytime() {
		return playtime;
	}

	public void setPlaytime(Timestamp playtime) {
		this.playtime = playtime;
	}

	public String getExpress() {
		return express;
	}

	public void setExpress(String express) {
		this.express = express;
	}

	public String getGatewayCode() {
		return gatewayCode;
	}

	public void setGatewayCode(String gatewayCode) {
		this.gatewayCode = gatewayCode;
	}

	public String getMerchantCode() {
		return merchantCode;
	}

	public void setMerchantCode(String merchantCode) {
		this.merchantCode = merchantCode;
	}
	
	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	public String getStatusText(){
		return OrderConstant.statusMap.get(getFullStatus());
	}
	public String getFullStatus(){
		if(status.startsWith(OrderConstant.STATUS_NEW) && isTimeout()) return OrderConstant.STATUS_TIMEOUT;
		return status;
	}
	public String getStatusText2(){//显示给用户看
		if(isCancel()) return "超时取消";
		if(isPaidSuccess()) return "交易成功";
		if(StringUtils.equals(status, OrderConstant.STATUS_PAID_RETURN)) return "已退款";
		if(isAllPaid()) return "已支付";
		return OrderConstant.statusMap.get(status);
	}
	public boolean isNew(){
		return status.startsWith(OrderConstant.STATUS_NEW) && !isTimeout();
	}
	public boolean isPaidFailure(){
		return OrderConstant.STATUS_PAID_FAILURE.equals(status) && (getDue() - gewapaid - alipaid <= 0);
	}
	public boolean isPaidUnfix(){
		return (OrderConstant.STATUS_PAID_UNFIX.equals(status)) && (getDue() - gewapaid - alipaid <= 0);
	}
	public boolean isPaidSuccess(){
		return OrderConstant.STATUS_PAID_SUCCESS.equals(status) && (getDue() - gewapaid - alipaid <= 0);
	}
	public boolean isAllPaid(){
		return StringUtils.startsWith(status, OrderConstant.STATUS_PAID) && (getDue() - gewapaid - alipaid <= 0) ;
	}
	public boolean isNotAllPaid(){
		return StringUtils.startsWith(status, OrderConstant.STATUS_PAID) && (getDue() - gewapaid - alipaid > 0) ;
	}
	public Integer getRealPaid(){
		return gewapaid+alipaid;
	}
	/**
	 * 可用抵用券直接支付金额，抵用金额可超过订单金额
	 * @return
	 */
	public boolean isZeroPay() {
		return getDue()<= 0 && discount > 0;
	}
	public boolean isCancel(){
		return StringUtils.startsWith(status, OrderConstant.STATUS_CANCEL) || 
				StringUtils.startsWith(status, OrderConstant.STATUS_NEW) && isTimeout();
	}
	public boolean isTimeout(){
		return validtime!=null && validtime.before(new Timestamp(System.currentTimeMillis()));
	}
	/**
	 * 可以处理
	 * @return
	 */
	public boolean canProcess(){
		return this.updatetime.before(DateUtil.addMinute(new Timestamp(System.currentTimeMillis()), -3));
	}
	public boolean isTimeoutCancel(){
		return isTimeout() && status.equals(OrderConstant.STATUS_NEW);
	}
	public Integer gainInvoiceDue(){
		Integer due = this.getAlipaid() + this.getGewapaid() - this.getWabi();
		return due;
	}
	public String gainUkey(){
		return GewaOrderHelper.getPartnerUkey(this);
	}
	public Integer gainRealUnitprice(){
		return totalfee/quantity;
	}
	//TODO:业务方法重写，取消硬编码
	public boolean surePartner(){
		return this.partnerid>1;
	}
	public boolean sureOutPartner(){//外部商家
		return this.memberid > PartnerConstant.MAX_MEMBERID;
	}
	public boolean sureGewaPartner(){//内部WAP,IPHONE...
		return this.partnerid>1 && this.memberid < PartnerConstant.MAX_MEMBERID;
	}

}
