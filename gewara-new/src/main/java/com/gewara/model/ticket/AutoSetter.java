package com.gewara.model.ticket;

import java.io.Serializable;
import java.sql.Timestamp;

import com.gewara.model.BaseObject;

public class AutoSetter extends BaseObject{
	private static final long serialVersionUID = 1719853864165162337L;
	public static final String STATUS_OPEN = "open";
	public static final String STATUS_OPEN_A = "open_a";
	public static final String STATUS_CLOSE = "close";
	public static final String CHECK_F = "F"; //未审核
	public static final String CHECK_T = "T"; //T 审核通过
	public static final String CHECK_D = "D"; // D审核不通过
	
	public boolean opiStatus = true;//控制自动设置器，在假期时需要手动控制时，开放的场次默认不开放购票
	private Long id;
	private String name;			//名称
	private String description;		//说明
	private Integer ordernum;		//排序号
	private String status;			//状态：open，启用手动，close：停用  open_a自动
	private String checkStatus;     //审核状态： F 未审核 T 审核通过  D审核不通过
	private String nameDescription; //名称说明
	//~~~~~~~~~~~条件~~~~~~~~~~~~~~~~~
	private Long cinemaid;
	private String movies;			//影片ID列表
	private Timestamp playtime1;	//放映时段1
	private Timestamp playtime2;	//放映时段2
	private Integer price1;			//影院卖价范围
	private Integer price2;
	private String weektype;		//星期
	private String timescope;		//时段：1400~1500,1800~2000
	private String roomnum;			//影厅：1,2,3,4,5
	private String edition;			//版本: 2D,3D,数字,IMAX,IMAX3D
	private String limitScript; //限制的javascript函数。
	//~~~~~~~~~~~设置~~~~~~~~~~~~~~~~~~
	private Integer costprice;		//成本价
	private Integer gewaprice;		//卖价
	private String elecard;			//优惠活动
	private String remark;			//场次特殊说明
	private String seatmap;			//座位模板
	private String priceScript; //设置的javascript函数
	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	private Long checkUser; //审核人id。
	
	public String gainFullDesc(){
		return "成本价:" + costprice + ", 卖价:" + gewaprice + "优惠活动:" + elecard + "座位模板:" + seatmap + "其他说明：" + description;
	}

	public AutoSetter() {
		
	}
	
	public AutoSetter(Long cinemaid) {
		this.cinemaid = cinemaid;
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

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Long getCinemaid() {
		return cinemaid;
	}

	public void setCinemaid(Long cinemaid) {
		this.cinemaid = cinemaid;
	}

	public String getMovies() {
		return movies;
	}

	public void setMovies(String movies) {
		this.movies = movies;
	}

	public Integer getPrice1() {
		return price1;
	}

	public void setPrice1(Integer price1) {
		this.price1 = price1;
	}

	public Integer getPrice2() {
		return price2;
	}

	public void setPrice2(Integer price2) {
		this.price2 = price2;
	}

	public String getWeektype() {
		return weektype;
	}

	public void setWeektype(String weektype) {
		this.weektype = weektype;
	}

	public String getEdition() {
		return edition;
	}

	public void setEdition(String edition) {
		this.edition = edition;
	}

	public Integer getCostprice() {
		return costprice;
	}

	public void setCostprice(Integer costprice) {
		this.costprice = costprice;
	}

	public Integer getGewaprice() {
		return gewaprice;
	}

	public void setGewaprice(Integer gewaprice) {
		this.gewaprice = gewaprice;
	}

	public String getElecard() {
		return elecard;
	}

	public void setElecard(String elecard) {
		this.elecard = elecard;
	}

	public String getRemark() {
		return remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}

	public String getTimescope() {
		return timescope;
	}

	public void setTimescope(String timescope) {
		this.timescope = timescope;
	}

	public Timestamp getPlaytime1() {
		return playtime1;
	}

	public void setPlaytime1(Timestamp playtime1) {
		this.playtime1 = playtime1;
	}

	public Timestamp getPlaytime2() {
		return playtime2;
	}

	public void setPlaytime2(Timestamp playtime2) {
		this.playtime2 = playtime2;
	}

	public String getSeatmap() {
		return seatmap;
	}

	public void setSeatmap(String seatmap) {
		this.seatmap = seatmap;
	}

	public String getRoomnum() {
		return roomnum;
	}

	public void setRoomnum(String roomnum) {
		this.roomnum = roomnum;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public Integer getOrdernum() {
		return ordernum;
	}

	public void setOrdernum(Integer ordernum) {
		this.ordernum = ordernum;
	}

	public String getCheckStatus() {
		return checkStatus;
	}

	public void setCheckStatus(String checkStatus) {
		this.checkStatus = checkStatus;
	}

	public String getNameDescription() {
		return nameDescription;
	}

	public void setNameDescription(String nameDescription) {
		this.nameDescription = nameDescription;
	}

	public Long getCheckUser() {
		return checkUser;
	}

	public void setCheckUser(Long checkUser) {
		this.checkUser = checkUser;
	}

	public String getLimitScript() {
		return limitScript;
	}

	public void setLimitScript(String limitScript) {
		this.limitScript = limitScript;
	}

	public String getPriceScript() {
		return priceScript;
	}

	public void setPriceScript(String priceScript) {
		this.priceScript = priceScript;
	}
	
}
