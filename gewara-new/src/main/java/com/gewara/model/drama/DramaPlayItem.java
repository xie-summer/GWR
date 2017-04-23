package com.gewara.model.drama;

import java.io.Serializable;
import java.sql.Timestamp;

import org.apache.commons.lang.StringUtils;

import com.gewara.constant.OdiConstant;
import com.gewara.model.BaseObject;
import com.gewara.util.DateUtil;

public class DramaPlayItem extends BaseObject{
	private static final long serialVersionUID = -1349390602375562527L;
	public static final String STATUS_Y = "Y";
	public static final String STATUS_N = "N";
	private Long id;
	private String name;
	private Long dramaid;
	private Long theatreid;
	private String dramaname;
	private String theatrename;
	private Long roomid;
	private String roomname;
	private Timestamp addtime;		
	private Timestamp playtime;		//演出时间
	private Timestamp endtime;		//演出结束时间
	private String language;		//
	private String status;			//是否有效
	private String partner;			//是否对外开放
	private Long dramaStarId;		//
	private String citycode;		//城市编码
	private BaseObject relate1;
	private BaseObject relate2;		
	private Long batch;				//批次标识
	private String opentype;		//开放类型：选座，价格
	private String period;			//是否固定时间
	private String seller;			//类型：GEWA,GPTBS
	private String sellerseq;		//场次ID(showItem-->siseq)
	private Integer sortnum;		//排序字段
	
	public DramaPlayItem(){}
	
	public DramaPlayItem(Timestamp createtime){
		this.addtime = createtime;
		this.status = STATUS_Y;
		this.partner = STATUS_N;
		this.seller = OdiConstant.PARTNER_GEWA;
		this.sortnum = 1;
	}
	
	public DramaPlayItem(Long dramaid){
		this.dramaid = dramaid;
		this.status = STATUS_Y;
		this.partner = STATUS_N;
		this.addtime = DateUtil.getCurFullTimestamp();
		this.seller = OdiConstant.PARTNER_GEWA;
		this.sortnum = 1;
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
	public Long getTheatreid() {
		return theatreid;
	}
	public void setTheatreid(Long theatreid) {
		this.theatreid = theatreid;
	}
	public Long getDramaid() {
		return dramaid;
	}
	public void setDramaid(Long dramaid) {
		this.dramaid = dramaid;
	}
	public Long getRoomid() {
		return roomid;
	}
	public void setRoomid(Long roomid) {
		this.roomid = roomid;
	}
	public String getRoomname() {
		return roomname;
	}
	public void setRoomname(String roomname) {
		this.roomname = roomname;
	}
	public String getLanguage() {
		return language;
	}
	public void setLanguage(String language) {
		this.language = language;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public String getPartner() {
		return partner;
	}

	public void setPartner(String partner) {
		if(StringUtils.isBlank(partner)) partner = STATUS_N;
		this.partner = partner;
	}
	
	public String getOpentype() {
		return opentype;
	}

	public Timestamp getEndtime() {
		return endtime;
	}

	public void setEndtime(Timestamp endtime) {
		this.endtime = endtime;
	}

	public void setOpentype(String opentype) {
		this.opentype = opentype;
	}

	public Timestamp getPlaytime() {
		return playtime;
	}
	public void setPlaytime(Timestamp playtime) {
		this.playtime = playtime;
	}
	public BaseObject getRelate1() {
		return relate1;
	}
	public void setRelate1(BaseObject relate1) {
		this.relate1 = relate1;
	}
	public BaseObject getRelate2() {
		return relate2;
	}
	public void setRelate2(BaseObject relate2) {
		this.relate2 = relate2;
	}
	
	public String getCitycode(){
		return this.citycode;
	}
	public void setCitycode(String citycode){
		this.citycode = citycode;
	}

	public Long getDramaStarId() {
		return dramaStarId;
	}

	public void setDramaStarId(Long dramaStarId) {
		this.dramaStarId = dramaStarId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Timestamp getAddtime() {
		return addtime;
	}

	public void setAddtime(Timestamp addtime) {
		this.addtime = addtime;
	}

	public Long getBatch() {
		return batch;
	}

	public void setBatch(Long batch) {
		this.batch = batch;
	}

	public String getPeriod() {
		return period;
	}

	public void setPeriod(String period) {
		this.period = period;
	}

	public String getSeller() {
		return seller;
	}

	public void setSeller(String seller) {
		this.seller = seller;
	}

	public String getSellerseq() {
		return sellerseq;
	}

	public void setSellerseq(String sellerseq) {
		this.sellerseq = sellerseq;
	}
	
	public Integer getSortnum() {
		return sortnum;
	}

	public void setSortnum(Integer sortnum) {
		this.sortnum = sortnum;
	}

	public boolean hasPeriod(String perid){
		if(StringUtils.isBlank(perid)) return false;
		return StringUtils.equals(this.period, perid);
	}
	
	public boolean hasSeller(String sell){
		if(StringUtils.isBlank(sell)) return false;
		return StringUtils.equals(this.seller, sell);
	}
	
	public boolean hasGewa(){
		return StringUtils.equals(this.seller, OdiConstant.PARTNER_GEWA);
	}
	public boolean isOpenseat(){
		return OdiConstant.OPEN_TYPE_SEAT.equals(this.opentype);
	}
	public boolean isOpenprice(){
		return OdiConstant.OPEN_TYPE_PRICE.equals(this.opentype);
	}

	public String getDramaname() {
		return dramaname;
	}

	public void setDramaname(String dramaname) {
		this.dramaname = dramaname;
	}

	public String getTheatrename() {
		return theatrename;
	}

	public void setTheatrename(String theatrename) {
		this.theatrename = theatrename;
	}
}
