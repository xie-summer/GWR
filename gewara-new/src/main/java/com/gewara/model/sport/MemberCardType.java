package com.gewara.model.sport;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import com.gewara.constant.MemberCardConstant;
import com.gewara.constant.OpenTimeTableConstant;
import com.gewara.constant.PaymethodConstant;
import com.gewara.constant.Status;
import com.gewara.constant.ticket.OpiConstant;
import com.gewara.model.BaseObject;
import com.gewara.util.DateUtil;
import com.gewara.util.JsonUtils;

public class MemberCardType extends BaseObject {
	private static final long serialVersionUID = -1987057577637322412L;
	private Long id;
	private String cardTypeUkey;			// 会员卡类型唯一号
	private String cardTypeCode;			// 会员卡类型编号
	private String cardType;				// 会员卡类型：1 次卡 2 会员卡
	private Integer money;					// 金额卡：金额/次卡：次数
	private Integer overNum;				// 剩余数量
	private Integer reserve;				// 总库存
	private String description;
	private Integer validTime;				// 有效时间，月为单位 如：1月，14月
	private Integer price;
	private Integer discount;				// 会员卡折扣 单位%  100是无折扣
	private String fitItem;					// 适用项目 金额卡为空
	private String belongVenue;				// 所属场馆/适用场馆 次卡所属场馆，金额卡 适用场馆
	private Long businessId;
	
	private Integer minpoint;				// 使用积分下限
	private Integer maxpoint;				// 使用积分上限
	private String spflag;					// 特殊活动标识
	
	private Timestamp opentime; 			// 开放预定时间
	private Timestamp closetime; 			// 关闭预定时间
	private String remark;					// 描述
	private String otherinfo;				// 其他信息
	private Integer sales = 0;				// 售出场地数
	private String status; 					// 状态
	private String elecard;					// 优惠券
	private Integer gewaprice; 				// 卖价
	private Integer costRate;				// 结算比率
	private Integer mingain;				// 保底金额
	private String notifymsg;				// 取票短信
	private Timestamp addtime;				// 创建时间
	public MemberCardType(){
		
	}
	public void intiMemberCardType(){
		this.opentime = DateUtil.getCurFullTimestamp();
		this.closetime = DateUtil.addDay(opentime, 60);
		this.gewaprice = 0;
		this.mingain = 0;
		this.addtime = opentime;
		this.status = Status.N;
		this.minpoint = 500;
		this.minpoint = 500;
		this.maxpoint = 10000;
		this.sales = 0;
		this.elecard = "M";
		Map<String, String> map = new HashMap<String, String>();
		map.put(OpiConstant.PAYOPTION, "notuse");
		map.put(OpiConstant.DEFAULTPAYMETHOD, PaymethodConstant.PAYMETHOD_PNRPAY);
		map.put(OpiConstant.PAYCMETHODLIST, PaymethodConstant.PAYMETHOD_GEWAPAY);
		this.otherinfo = JsonUtils.writeMapToJson(map);
	}
	public String getCardTypeUkey() {
		return cardTypeUkey;
	}
	public void setCardTypeUkey(String cardTypeUkey) {
		this.cardTypeUkey = cardTypeUkey;
	}
	public String getCardTypeCode() {
		return cardTypeCode;
	}
	public void setCardTypeCode(String cardTypeCode) {
		this.cardTypeCode = cardTypeCode;
	}
	public String getCardType() {
		return cardType;
	}
	public void setCardType(String cardType) {
		this.cardType = cardType;
	}
	public Integer getMoney() {
		return money;
	}
	public void setMoney(Integer money) {
		this.money = money;
	}
	public Integer getOverNum() {
		return overNum;
	}
	public void setOverNum(Integer overNum) {
		this.overNum = overNum;
	}
	public Integer getReserve() {
		return reserve;
	}
	public void setReserve(Integer reserve) {
		this.reserve = reserve;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public Integer getValidTime() {
		return validTime;
	}
	public void setValidTime(Integer validTime) {
		this.validTime = validTime;
	}
	public Integer getPrice() {
		return price;
	}
	public void setPrice(Integer price) {
		this.price = price;
	}
	public Integer getDiscount() {
		return discount;
	}
	public void setDiscount(Integer discount) {
		this.discount = discount;
	}
	public String getFitItem() {
		return fitItem;
	}
	public void setFitItem(String fitItem) {
		this.fitItem = fitItem;
	}
	public String getBelongVenue() {
		return belongVenue;
	}
	public void setBelongVenue(String belongVenue) {
		this.belongVenue = belongVenue;
	}
	public Long getBusinessId() {
		return businessId;
	}
	public void setBusinessId(Long businessId) {
		this.businessId = businessId;
	}
	@Override
	public Serializable realId() {
		return id;
	}

	public Timestamp getOpentime() {
		return opentime;
	}

	public void setOpentime(Timestamp opentime) {
		this.opentime = opentime;
	}

	public Timestamp getClosetime() {
		return closetime;
	}

	public void setClosetime(Timestamp closetime) {
		this.closetime = closetime;
	}

	public String getRemark() {
		return remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}

	public String getOtherinfo() {
		return otherinfo;
	}

	public void setOtherinfo(String otherinfo) {
		this.otherinfo = otherinfo;
	}

	public Integer getSales() {
		return sales;
	}

	public void setSales(Integer sales) {
		this.sales = sales;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getElecard() {
		return elecard;
	}

	public void setElecard(String elecard) {
		this.elecard = elecard;
	}

	public Integer getCostprice() {
		return gewaprice-getGain();
	}
	public Integer getGain(){
		if(costRate!=null) {
			int gain = Math.round((gewaprice*costRate)/100);
			if(gain<mingain) gain = mingain;
			return gain;
		}
		return 0;
	}
	public Integer getGewaprice() {
		return gewaprice;
	}

	public void setGewaprice(Integer gewaprice) {
		this.gewaprice = gewaprice;
	}
	public Integer getCostRate() {
		return costRate;
	}
	public void setCostRate(Integer costRate) {
		this.costRate = costRate;
	}
	public Integer getMingain() {
		return mingain;
	}
	public void setMingain(Integer mingain) {
		this.mingain = mingain;
	}
	public Integer getMinpoint() {
		return minpoint;
	}
	public void setMinpoint(Integer minpoint) {
		this.minpoint = minpoint;
	}
	public Integer getMaxpoint() {
		return maxpoint;
	}
	public void setMaxpoint(Integer maxpoint) {
		this.maxpoint = maxpoint;
	}
	public String getSpflag() {
		return spflag;
	}
	public void setSpflag(String spflag) {
		this.spflag = spflag;
	}
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public Timestamp getAddtime() {
		return addtime;
	}
	public void setAddtime(Timestamp addtime) {
		this.addtime = addtime;
	}
	public String getCardtypeText(){
		return MemberCardConstant.cardtypeMap.get(cardType);
	}
	public boolean hasBooking() {
		Timestamp curtime = new Timestamp(System.currentTimeMillis());
		if(opentime == null || closetime == null) return false;
		return status.equals(OpenTimeTableConstant.STATUS_BOOK) && opentime.before(curtime)&& closetime.after(curtime) ;
	}
	public String getTitle(){
		return money+getCardtypeText();
	}
	public String getNotifymsg() {
		return notifymsg;
	}
	public void setNotifymsg(String notifymsg) {
		this.notifymsg = notifymsg;
	}
	public boolean isOpenPointPay(){
		return maxpoint !=null && maxpoint > 0;
	}
	public boolean isOpenCardPay(){
		return StringUtils.containsAny(this.elecard, "ABD");
	}
	public boolean isDisCountPay(){
		return StringUtils.contains(this.elecard, "M");
	}
	public Timestamp getBuyValidtime(Timestamp addTime){
		return DateUtil.addDay(addTime, validTime*30);
	}
	public void addSales(Integer quantity){
		this.sales = this.sales + quantity;
	}
	public boolean hasNumCard(){
		return StringUtils.equals(cardType, MemberCardConstant.CARD_TYPE_NUMBER);
	}
	public boolean hasAmountCard(){
		return StringUtils.equals(cardType, MemberCardConstant.CARD_TYPE_AMOUNT);
	}
}
