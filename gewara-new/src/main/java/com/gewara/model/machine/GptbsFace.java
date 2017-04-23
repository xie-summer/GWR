package com.gewara.model.machine;
public class GptbsFace extends CommonFace{
	private static final long serialVersionUID = -4566124300545337745L;
	private String oneCode;// 条形码
	private String twoCode;// 二维码
	private String priceDescription;// 价格描述
	private String areaCnName;// 区域中文名
	private String areaEnName;// 区域英文名
	private String areaDescription;// 区域描述
	private String areaIcon;// 区域图标
	private String lineno;// 排
	private String rankno;// 座
	private String ticketPrice;// 票价
	private String discountAmount;// 折扣票价
	private String serialNum;// 站票序号
	private String siseqno;
	private String areaseqno;
	
	public String getOneCode() {
		return oneCode;
	}
	public void setOneCode(String oneCode) {
		this.oneCode = oneCode;
	}
	public String getTwoCode() {
		return twoCode;
	}
	public void setTwoCode(String twoCode) {
		this.twoCode = twoCode;
	}
	public String getPriceDescription() {
		return priceDescription;
	}
	public void setPriceDescription(String priceDescription) {
		this.priceDescription = priceDescription;
	}
	public String getAreaCnName() {
		return areaCnName;
	}
	public void setAreaCnName(String areaCnName) {
		this.areaCnName = areaCnName;
	}
	public String getAreaEnName() {
		return areaEnName;
	}
	public void setAreaEnName(String areaEnName) {
		this.areaEnName = areaEnName;
	}
	public String getAreaDescription() {
		return areaDescription;
	}
	public void setAreaDescription(String areaDescription) {
		this.areaDescription = areaDescription;
	}
	public String getAreaIcon() {
		return areaIcon;
	}
	public void setAreaIcon(String areaIcon) {
		this.areaIcon = areaIcon;
	}
	public String getLineno() {
		return lineno;
	}
	public void setLineno(String lineno) {
		this.lineno = lineno;
	}
	public String getRankno() {
		return rankno;
	}
	public void setRankno(String rankno) {
		this.rankno = rankno;
	}
	public String getTicketPrice() {
		return ticketPrice;
	}
	public void setTicketPrice(String ticketPrice) {
		this.ticketPrice = ticketPrice;
	}
	public String getDiscountAmount() {
		return discountAmount;
	}
	public void setDiscountAmount(String discountAmount) {
		this.discountAmount = discountAmount;
	}
	public String getSerialNum() {
		return serialNum;
	}
	public void setSerialNum(String serialNum) {
		this.serialNum = serialNum;
	}
	public String getSiseqno() {
		return siseqno;
	}
	public void setSiseqno(String siseqno) {
		this.siseqno = siseqno;
	}
	public String getAreaseqno() {
		return areaseqno;
	}
	public void setAreaseqno(String areaseqno) {
		this.areaseqno = areaseqno;
	}
	
}