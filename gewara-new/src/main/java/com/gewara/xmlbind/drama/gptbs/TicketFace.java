package com.gewara.xmlbind.drama.gptbs;

public class TicketFace {
	
	private Long seatid;			//座位ID
	private String lineno;			//排
	private String rankno;			//座
	private Integer x;			// 座位坐标X
	private Integer y;			// 座位坐标Y
	private String oneCode;			//一维码（10位）
	private String twoCode;			//二维码（10位）
	private String tempPrientInfo;			//临时打印信息
	private String priceDescription;			//价格描述
	private String voucherNo;			//凭证编号
	private Long voucherId;			//凭证ID
	private String ticketType;			//出票类型
	private String areaCnName;			//区域中文名
	private String areaEnName;			//区域英文名
	private String areaDescription;			//区域描述
	private String areaIcon;			//区域图标
	private String playDate;			//场次日期
	private String playTime;			//场次时间
	private String scheduleCnName;			//场次中文名
	private String scheduleEnName;			//场次英文名
	private String venueCnName;			//场地名称
	private String venueEnName;			//场地名称
	private String stadiumCnName;			//场馆名称
	private String stadiumEnName;			//场馆名称
	private String programCnName;			//项目中文名
	private String programEnName;			//项目英文名
	private String programStartTime;			//项目开始日期
	private Double ticketPrice;			//票价
	private String showPrice;			//票价是否显示
	private Double discountAmount;			//折扣票价
	private String userName;			//用户简称
	private String serialNum;			//序号
	
	public Long getSeatid() {
		return seatid;
	}
	public void setSeatid(Long seatid) {
		this.seatid = seatid;
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
	public Integer getX() {
		return x;
	}
	public void setX(Integer x) {
		this.x = x;
	}
	public Integer getY() {
		return y;
	}
	public void setY(Integer y) {
		this.y = y;
	}
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
	public String getTempPrientInfo() {
		return tempPrientInfo;
	}
	public void setTempPrientInfo(String tempPrientInfo) {
		this.tempPrientInfo = tempPrientInfo;
	}
	public String getPriceDescription() {
		return priceDescription;
	}
	public void setPriceDescription(String priceDescription) {
		this.priceDescription = priceDescription;
	}
	public String getVoucherNo() {
		return voucherNo;
	}
	public void setVoucherNo(String voucherNo) {
		this.voucherNo = voucherNo;
	}
	public Long getVoucherId() {
		return voucherId;
	}
	public void setVoucherId(Long voucherId) {
		this.voucherId = voucherId;
	}
	public String getTicketType() {
		return ticketType;
	}
	public void setTicketType(String ticketType) {
		this.ticketType = ticketType;
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
	public String getPlayDate() {
		return playDate;
	}
	public void setPlayDate(String playDate) {
		this.playDate = playDate;
	}
	public String getPlayTime() {
		return playTime;
	}
	public void setPlayTime(String playTime) {
		this.playTime = playTime;
	}
	public String getScheduleCnName() {
		return scheduleCnName;
	}
	public void setScheduleCnName(String scheduleCnName) {
		this.scheduleCnName = scheduleCnName;
	}
	public String getScheduleEnName() {
		return scheduleEnName;
	}
	public void setScheduleEnName(String scheduleEnName) {
		this.scheduleEnName = scheduleEnName;
	}
	public String getVenueCnName() {
		return venueCnName;
	}
	public void setVenueCnName(String venueCnName) {
		this.venueCnName = venueCnName;
	}
	public String getVenueEnName() {
		return venueEnName;
	}
	public void setVenueEnName(String venueEnName) {
		this.venueEnName = venueEnName;
	}
	public String getStadiumCnName() {
		return stadiumCnName;
	}
	public void setStadiumCnName(String stadiumCnName) {
		this.stadiumCnName = stadiumCnName;
	}
	public String getStadiumEnName() {
		return stadiumEnName;
	}
	public void setStadiumEnName(String stadiumEnName) {
		this.stadiumEnName = stadiumEnName;
	}
	public String getProgramCnName() {
		return programCnName;
	}
	public void setProgramCnName(String programCnName) {
		this.programCnName = programCnName;
	}
	public String getProgramEnName() {
		return programEnName;
	}
	public void setProgramEnName(String programEnName) {
		this.programEnName = programEnName;
	}
	public String getProgramStartTime() {
		return programStartTime;
	}
	public void setProgramStartTime(String programStartTime) {
		this.programStartTime = programStartTime;
	}
	public Double getTicketPrice() {
		return ticketPrice;
	}
	public void setTicketPrice(Double ticketPrice) {
		this.ticketPrice = ticketPrice;
	}
	public String getShowPrice() {
		return showPrice;
	}
	public void setShowPrice(String showPrice) {
		this.showPrice = showPrice;
	}
	public Double getDiscountAmount() {
		return discountAmount;
	}
	public void setDiscountAmount(Double discountAmount) {
		this.discountAmount = discountAmount;
	}
	public String getUserName() {
		return userName;
	}
	public void setUserName(String userName) {
		this.userName = userName;
	}
	public String getSerialNum() {
		return serialNum;
	}
	public void setSerialNum(String serialNum) {
		this.serialNum = serialNum;
	}

}
