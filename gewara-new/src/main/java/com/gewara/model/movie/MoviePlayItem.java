package com.gewara.model.movie;
import java.io.Serializable;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.gewara.constant.ticket.OpiConstant;
import com.gewara.model.BaseObject;
import com.gewara.util.DateUtil;

/**
 * @author <a href="mailto:acerge@163.com">gebiao(acerge)</a>
 * @since 2007-9-28下午02:05:17
 */
public class MoviePlayItem extends BaseObject implements Comparable<MoviePlayItem> {
	private static final long serialVersionUID = -4016785855588367848L;
	private Long id;			//场次ID
	private Long movieid;		//影片ID
	private Long cinemaid;		//影院ID
	private String language;	//语言
	private Date playdate;		//放映日期
	private String playtime;	//放映时间
	private Integer price;		//影院价
	private Integer lowest;		//最低票价
	private Integer gewaprice;	//格瓦卖价
	private String pricemark;	//价格说明
	private String edition;		//版本
	private String remark;		//备注
	private Long roomid;		//影厅
	private String roomnum;		//影厅序号
	private String playroom;	//放映厅名称
	private String opentype;	//开放类型
	private String citycode;	//城市
	private String seqNo;		//外部关联ID
	private Long batch;				//批次标识
	private Timestamp createtime;	//创建时间
	private Timestamp updatetime;	//更新时间
	private String openStatus;		//开放状态：init：初始状态，open：已开放，close：以后也不开放
	private String otherinfo;
	public MoviePlayItem(){}
	public MoviePlayItem(Timestamp createtime){
		this.createtime = createtime;
		this.updatetime = createtime;
		this.openStatus = OpiConstant.MPI_OPENSTATUS_INIT;
	}
	public MoviePlayItem(Long cinemaid, String citycode, Long movieid, Date playdate, CinemaRoom room, String language, String edition){
		this.cinemaid = cinemaid;
		this.movieid = movieid;
		this.playdate =  playdate;
		this.language = language;
		this.edition = edition;
		this.citycode = citycode;
		this.createtime = new Timestamp(System.currentTimeMillis());
		this.updatetime = createtime;
		this.openStatus = OpiConstant.MPI_OPENSTATUS_INIT;
		if(room!=null){
			this.roomid = room.getId();
			this.roomnum = room.getNum();
			this.playroom = room.getRoomname();
		}
	}
	public MoviePlayItem(MoviePlayItem mpi){
		copyFrom(mpi);
	}
	public void copyFrom(MoviePlayItem mpi){
		this.cinemaid = mpi.cinemaid;
		this.movieid = mpi.movieid;
		this.playdate = mpi.playdate;
		this.playtime = mpi.playtime;
		this.price = mpi.price;
		this.lowest = mpi.lowest;
		this.pricemark = mpi.pricemark;
		this.edition = mpi.edition;
		this.language = mpi.language;
		this.remark = mpi.remark;
		this.batch = mpi.batch;
		this.citycode = mpi.citycode;
		this.createtime = mpi.createtime;
		this.openStatus = mpi.openStatus;
		this.roomid = mpi.roomid;
		this.roomnum = mpi.roomnum;
		this.playroom = mpi.playroom;
	}
	public static List<MoviePlayItem> copy(List<MoviePlayItem> playitemList, Date newDate) {
		List<MoviePlayItem> result = new ArrayList<MoviePlayItem>(playitemList.size());
		MoviePlayItem newMpi = null;
		for(MoviePlayItem mpi:playitemList){
			newMpi = new MoviePlayItem(mpi);
			newMpi.setPlaydate(newDate);
			result.add(newMpi);
		}
		return result;
	}
	/**
	 * @param playItemList
	 * @return 获取未过时的排片
	 */
	public static List<MoviePlayItem> getCurrent(Date date, List<MoviePlayItem> playItemList) {
		Date today = DateUtil.getBeginningTimeOfDay(new Date());
		if(date.after(today)) return playItemList;
		if(date.before(today)) return new ArrayList<MoviePlayItem>();
		String time = DateUtil.format(new Date(), "HH:mm");
		List<MoviePlayItem> result = new ArrayList<MoviePlayItem>();
		for(MoviePlayItem mpi:playItemList){
			if(mpi.playtime.compareTo(time) > 0) result.add(mpi);
		}
		return result;
	}
	public boolean isHfh() {
		return StringUtils.isNotBlank(seqNo);
	}
	@Override
	public Serializable realId() {
		return id;
	}
	public boolean isValid(){
		return StringUtils.isNotBlank(playroom) && this.playdate != null && StringUtils.isNotBlank(this.playtime);
	}
	public Long getId() {
		return this.id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Date getPlaydate() {
		return playdate;
	}

	public void setPlaydate(Date playdate) {
		this.playdate = playdate;
	}

	public String getPlaytime() {
		return playtime;
	}

	public void setPlaytime(String playtime) {
		this.playtime = playtime;
	}

	public Integer getPrice() {
		return this.price;
	}

	public void setPrice(Integer price) {
		this.price = price;
	}

	public String getRemark() {
		return this.remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}

	public String getPlayroom() {
		return playroom;
	}

	public void setPlayroom(String playroom) {
		this.playroom = playroom;
	}
	public int compareTo(MoviePlayItem another) {
		if(this == another) return 0;
		if(another==null) return 1;
		if(this.playdate.after(another.playdate)) return 1;
		if(this.playdate.before(another.playdate)) return -1;
		if(!this.playtime.equals(another.playtime)) return this.playtime.compareTo(another.playtime);
		return StringUtils.isBlank(this.playroom) ? (StringUtils.isBlank(another.playroom) ? 0 : -1) : (StringUtils.isBlank(another.playroom) ? 1 : this.playroom.compareTo(another.playroom));
	}
	public Long getBatch() {
		return batch;
	}
	public void setBatch(Long batch) {
		this.batch = batch;
	}
	public String getPricemark() {
		return pricemark;
	}
	public void setPricemark(String pricemark) {
		this.pricemark = pricemark;
	}
	public String getEdition() {
		return edition;
	}
	public void setEdition(String edition) {
		this.edition = edition;
	}

	public String getLanguage() {
		return language;
	}

	public void setLanguage(String language) {
		this.language = language;
	}

	public Long getRoomid() {
		return roomid;
	}

	public void setRoomid(Long roomid) {
		this.roomid = roomid;
	}

	public Long getMovieid() {
		return movieid;
	}

	public void setMovieid(Long movieid) {
		this.movieid = movieid;
	}

	public Long getCinemaid() {
		return cinemaid;
	}

	public void setCinemaid(Long cinemaid) {
		this.cinemaid = cinemaid;
	}

	public String getFullPlaytime(){
		return DateUtil.formatDate(playdate) + " " + playtime + ":00";
	}
	public Integer getGewaprice() {
		return gewaprice;
	}
	public void setGewaprice(Integer gewaprice) {
		this.gewaprice = gewaprice;
	}
	public String getSeqNo() {
		return seqNo;
	}
	public void setSeqNo(String seqNo) {
		this.seqNo = seqNo;
	}
	public Integer getLowest() {
		return lowest;
	}
	public void setLowest(Integer lowest) {
		this.lowest = lowest;
	}
	public String getCitycode() {
		return citycode;
	}
	public void setCitycode(String citycode) {
		this.citycode = citycode;
	}
	public Timestamp getCreatetime() {
		return createtime;
	}
	public void setCreatetime(Timestamp createtime) {
		this.createtime = createtime;
	}
	public String getOpenStatus() {
		return openStatus;
	}
	public void setOpenStatus(String openStatus) {
		this.openStatus = openStatus;
	}
	public String getRoomnum() {
		return roomnum;
	}
	public void setRoomnum(String roomnum) {
		this.roomnum = roomnum;
	}
	public Timestamp getUpdatetime() {
		return updatetime;
	}
	public void setUpdatetime(Timestamp updatetime) {
		this.updatetime = updatetime;
	}
	public String getOpentype() {
		return opentype;
	}
	public void setOpentype(String opentype) {
		this.opentype = opentype;
	}
	
	public boolean hasGewara(){
		return StringUtils.isBlank(this.opentype) || StringUtils.equals(this.opentype, OpiConstant.OPEN_GEWARA);
	}
	
	public boolean hasOpentype(String type){
		if(StringUtils.isBlank(type)) return false;
		return StringUtils.equals(this.opentype, type);
	}
	
	public boolean hasOpenStatus(String status){
		return StringUtils.equals(this.openStatus, status);
	}
	public String getOtherinfo() {
		return otherinfo;
	}
	public void setOtherinfo(String otherinfo) {
		this.otherinfo = otherinfo;
	}
	public boolean isUnOpenToGewa(){
		return StringUtils.contains(otherinfo, OpiConstant.UNOPENGEWA);
	}
	public boolean isUnShowToGewa(){
		return StringUtils.contains(otherinfo, OpiConstant.UNSHOWGEWA) || StringUtils.contains(otherinfo, OpiConstant.UNOPENGEWA);
	}
}
