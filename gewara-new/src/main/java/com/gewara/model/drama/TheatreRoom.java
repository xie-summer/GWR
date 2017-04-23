package com.gewara.model.drama;

import java.sql.Timestamp;

import org.apache.commons.lang.StringUtils;

import com.gewara.model.ticket.PlayRoom;
import com.gewara.util.DateUtil;

public class TheatreRoom extends PlayRoom implements Comparable<TheatreRoom>{
	private static final long serialVersionUID = -6684419397825316722L;
	private Long theatreid;
	private String seatmap;
	private String hotzone;
	private Long fieldid;
	private Timestamp synchtime; //同步theatre_room_seat时间条件
	//num-->(FieldArea-->areanum)
	public TheatreRoom(){}
	
	public TheatreRoom(Long theatreid, Long fielid){
		this.theatreid = theatreid;
		this.fieldid = fielid;
		this.seatnum = 0;
		this.updatetime = DateUtil.getCurFullTimestamp();
	}
	
	public Long getTheatreid() {
		return theatreid;
	}
	public void setTheatreid(Long theatreid) {
		this.theatreid = theatreid;
	}
	@Override
	public int compareTo(TheatreRoom o) {
		return StringUtils.leftPad(""+num, 3, '0').compareTo(StringUtils.leftPad(""+o.num, 3, '0'));
	}
	
	public String getSeatmap() {
		return seatmap;
	}
	public void setSeatmap(String seatmap) {
		this.seatmap = seatmap;
	}
	public Long getFieldid() {
		return fieldid;
	}
	public void setFieldid(Long fieldid) {
		this.fieldid = fieldid;
	}
	public String getHotzone() {
		return hotzone;
	}
	public void setHotzone(String hotzone) {
		this.hotzone = hotzone;
	}
	public Timestamp getSynchtime(){
		return synchtime;
	}
	public void setSynchtime(Timestamp synchtime){
		this.synchtime = synchtime;
	}
}
