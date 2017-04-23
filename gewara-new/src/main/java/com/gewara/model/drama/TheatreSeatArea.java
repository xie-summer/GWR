package com.gewara.model.drama;

import java.io.Serializable;
import java.sql.Timestamp;

import org.apache.commons.lang.StringUtils;

import com.gewara.constant.OdiConstant;
import com.gewara.constant.Status;
import com.gewara.model.BaseObject;
import com.gewara.util.DateUtil;

public class TheatreSeatArea extends BaseObject {
	private static final long serialVersionUID = 7040516605857737934L;
	private Long id;					//
	private Long dpid;					//场次ID
	private Long theatreid;			
	private Long dramaid;
	private String areaname;			//区域名称
	private String enname;				//
	private String seller;				//类型:对应DramaPlayItem seller
	private String sellerseq;			//区域编号（ShowArea-->saseqNo）
	
	private String fieldnum;			//场地序号
	private String roomnum;				//区域序号
	
	private String description;			//区域描述
	private String standing;			//是否站票
	private Integer total;				//站票/座位总量
	private Integer limitnum;			//限制数
	private Integer firstline;			//起始行
	private Integer firstrank;			//起始列
	private Integer linenum;			//表格宽度
	private Integer ranknum;			//表格高度
	private String hotzone;				//座标值
	private String mobilehotzone;			//坐标值
	private String status;				//状态：可用、删除
	private String seatmap;				//座位图
	private Timestamp addtime;			
	private Timestamp updatetime;		
	
	private Integer gsellnum;			//Gewa卖出数
	private Integer csellnum;			//场馆卖出数
	private Integer locknum;			//Gewa锁定数
	
	private String otherinfo;
	
	public TheatreSeatArea(){}
	
	public TheatreSeatArea(Long dpid){
		this.dpid = dpid;
		this.status = Status.Y;
		this.total = 0;
		this.limitnum = 0;
		this.gsellnum = 0;
		this.csellnum = 0;
		this.locknum = 0;
		this.addtime = DateUtil.getCurFullTimestamp();
		this.updatetime = this.addtime;
		this.otherinfo = "{}";
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

	public Long getDpid() {
		return dpid;
	}

	public void setDpid(Long dpid) {
		this.dpid = dpid;
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

	public String getAreaname() {
		return areaname;
	}

	public String getName(){
		return areaname;
	}
	
	public void setAreaname(String areaname) {
		this.areaname = areaname;
	}

	public String getEnname() {
		return enname;
	}

	public void setEnname(String enname) {
		this.enname = enname;
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

	public String getFieldnum() {
		return fieldnum;
	}

	public void setFieldnum(String fieldnum) {
		this.fieldnum = fieldnum;
	}

	public String getRoomnum() {
		return roomnum;
	}

	public void setRoomnum(String roomnum) {
		this.roomnum = roomnum;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getStanding() {
		return standing;
	}

	public void setStanding(String standing) {
		this.standing = standing;
	}

	public Integer getTotal() {
		return total;
	}

	public void setTotal(Integer total) {
		this.total = total;
	}

	public Integer getLimitnum() {
		return limitnum;
	}

	public void setLimitnum(Integer limitnum) {
		this.limitnum = limitnum;
	}

	public Integer getFirstline() {
		return firstline;
	}

	public void setFirstline(Integer firstline) {
		this.firstline = firstline;
	}

	public Integer getFirstrank() {
		return firstrank;
	}

	public void setFirstrank(Integer firstrank) {
		this.firstrank = firstrank;
	}

	public Integer getLinenum() {
		return linenum;
	}

	public void setLinenum(Integer linenum) {
		this.linenum = linenum;
	}

	public Integer getRanknum() {
		return ranknum;
	}

	public void setRanknum(Integer ranknum) {
		this.ranknum = ranknum;
	}

	public String getHotzone() {
		return hotzone;
	}

	public void setHotzone(String hotzone) {
		this.hotzone = hotzone;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public Timestamp getAddtime() {
		return addtime;
	}

	public void setAddtime(Timestamp addtime) {
		this.addtime = addtime;
	}

	public Integer getSeatnum(){
		return this.total;
	}
	
	public Integer getGsellnum() {
		return gsellnum;
	}

	public void setGsellnum(Integer gsellnum) {
		this.gsellnum = gsellnum;
	}

	public Integer getCsellnum() {
		return csellnum;
	}

	public void setCsellnum(Integer csellnum) {
		this.csellnum = csellnum;
	}

	public Integer getLocknum() {
		return locknum;
	}

	public void setLocknum(Integer locknum) {
		this.locknum = locknum;
	}

	public String getOtherinfo() {
		return otherinfo;
	}

	public void setOtherinfo(String otherinfo) {
		this.otherinfo = otherinfo;
	}

	public Timestamp getUpdatetime() {
		return updatetime;
	}

	public void setUpdatetime(Timestamp updatetime) {
		this.updatetime = updatetime;
	}
	
	public String getSeatmap() {
		return seatmap;
	}

	public void setSeatmap(String seatmap) {
		this.seatmap = seatmap;
	}

	public boolean hasStatus(String stats){
		if(StringUtils.isBlank(stats)) return false;
		return StringUtils.equals(this.status, stats);
	}
	public boolean hasSeller(String sell){
		if(StringUtils.isBlank(sell)) return false;
		return StringUtils.equals(this.seller, sell);
	}

	public boolean hasGewara(){
		return StringUtils.equals(this.seller, OdiConstant.PARTNER_GEWA);
	}
	
	public Integer getRemainnum(){
		return this.limitnum - gsellnum - csellnum - locknum;
	}
	public String getMobilehotzone() {
		return mobilehotzone;
	}

	public void setMobilehotzone(String mobilehotzone) {
		this.mobilehotzone = mobilehotzone;
	}

}
