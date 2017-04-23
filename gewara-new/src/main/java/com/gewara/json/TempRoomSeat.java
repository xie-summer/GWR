package com.gewara.json;

import java.io.Serializable;

import com.gewara.util.DateUtil;

public class TempRoomSeat implements Serializable {
	
	private static final long serialVersionUID = 949504538567352910L;
	private String id;
	private Long roomid;
	private String tmpname;
	private String seatbody;
	private String addtime;
	private String updatetime;
	
	public TempRoomSeat(){}
	
	public TempRoomSeat(Long roomid, String tmpname){
		this.roomid = roomid;
		this.tmpname = tmpname;
		this.addtime = DateUtil.getCurFullTimestampStr();
		this.updatetime = this.addtime;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public Long getRoomid() {
		return roomid;
	}

	public void setRoomid(Long roomid) {
		this.roomid = roomid;
	}

	public String getTmpname() {
		return tmpname;
	}

	public void setTmpname(String tmpname) {
		this.tmpname = tmpname;
	}
	
	public String getAddtime() {
		return addtime;
	}

	public void setAddtime(String addtime) {
		this.addtime = addtime;
	}

	public String getUpdatetime() {
		return updatetime;
	}

	public void setUpdatetime(String updatetime) {
		this.updatetime = updatetime;
	}

	public String getSeatbody() {
		return seatbody;
	}

	public void setSeatbody(String seatbody) {
		this.seatbody = seatbody;
	}
}
