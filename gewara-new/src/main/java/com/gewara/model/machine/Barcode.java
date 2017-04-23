package com.gewara.model.machine;

import java.io.Serializable;
import java.sql.Timestamp;

import com.gewara.constant.Status;
import com.gewara.model.BaseObject;
import com.gewara.model.drama.OpenDramaItem;
import com.gewara.model.goods.BaseGoods;
import com.gewara.model.ticket.OpenPlayItem;
import com.gewara.util.DateUtil;
//条形码
public class Barcode extends BaseObject{
	private static final long serialVersionUID = -6748209192667235170L;
	public static final Integer BARCODE_MAXNUM = 1000;
	public static final Integer BARCODE_HANDMAXNUM = 50000;
	private Long id;
	private String barcode;			//条形码
	private String serialno;		//流水号
	private Long relatedid;			//关联场次id
	private Long placeid;			//场馆id
	private Long itemid;			//项目id
	private String tradeno;			//订单号
	private String status;			//状态		//N:默认生成状态 ,Y:已同步, T:取票
	private String flag;			//标识		
	private Timestamp validtime;	//有效时间
	private Timestamp taketime;		//取票时间
	private Timestamp updatetime;	//更新时间
	private Timestamp addtime;		//增加时间
	public Barcode(){
		
	}
	public Barcode(Long placeid){
		this.placeid = placeid;
		this.status = Status.N;
		this.flag = Status.Y;
		this.addtime = DateUtil.getMillTimestamp();
		this.updatetime = this.addtime;
	}
	public Barcode(OpenPlayItem opi){
		this.relatedid = opi.getMpid();
		this.placeid = opi.getCinemaid();
		this.itemid = opi.getMovieid();
		this.status = Status.N;
		this.flag = Status.Y;
		this.addtime = DateUtil.getMillTimestamp();
		this.updatetime = this.addtime;
		this.validtime = DateUtil.addHour(opi.getPlaytime(), 3);
	}
	public Barcode(OpenDramaItem odi){
		this.relatedid = odi.getDpid();
		this.placeid = odi.getTheatreid();
		this.itemid = odi.getDramaid();
		this.status = Status.N;
		this.flag = Status.Y;
		this.addtime = DateUtil.getMillTimestamp();
		this.updatetime = this.addtime;
		this.validtime = DateUtil.addHour(odi.getPlaytime(), 3);
	}
	
	public Barcode(BaseGoods goods){
		this.relatedid = goods.getId();
		this.placeid = goods.getRelatedid();
		this.status = Status.N;
		this.flag = Status.Y;
		this.addtime = DateUtil.getMillTimestamp();
		this.updatetime = this.addtime;
		this.validtime = DateUtil.addHour(goods.getTovalidtime(), 3);
	}
	
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public Long getRelatedid() {
		return relatedid;
	}
	public void setRelatedid(Long relatedid) {
		this.relatedid = relatedid;
	}
	public Long getPlaceid() {
		return placeid;
	}
	public void setPlaceid(Long placeid) {
		this.placeid = placeid;
	}
	public Long getItemid() {
		return itemid;
	}
	public void setItemid(Long itemid) {
		this.itemid = itemid;
	}
	public String getTradeno() {
		return tradeno;
	}
	public void setTradeno(String tradeno) {
		this.tradeno = tradeno;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public Timestamp getTaketime() {
		return taketime;
	}
	public void setTaketime(Timestamp taketime) {
		this.taketime = taketime;
	}
	public Timestamp getAddtime() {
		return addtime;
	}
	public void setAddtime(Timestamp addtime) {
		this.addtime = addtime;
	}
	public String getBarcode() {
		return barcode;
	}
	public void setBarcode(String barcode) {
		this.barcode = barcode;
	}
	@Override
	public Serializable realId() {
		return id;
	}
	public Timestamp getValidtime() {
		return validtime;
	}
	public void setValidtime(Timestamp validtime) {
		this.validtime = validtime;
	}
	public Timestamp getUpdatetime() {
		return updatetime;
	}
	public void setUpdatetime(Timestamp updatetime) {
		this.updatetime = updatetime;
	}
	public String getFlag() {
		return flag;
	}
	public void setFlag(String flag) {
		this.flag = flag;
	}
	public String getSerialno() {
		return serialno;
	}
	public void setSerialno(String serialno) {
		this.serialno = serialno;
	}
}
