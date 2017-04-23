package com.gewara.model.common;

import java.io.Serializable;

import com.gewara.model.BaseObject;

/**
 * 地铁线路站点映射
 * @author acerge(acerge@163.com)
 * @since 4:52:30 PM Jun 17, 2011
 */
public class Line2Station extends BaseObject {
	private static final long serialVersionUID = -8200807695086215174L;
	private Long id;
	private Subwayline line;
	private Subwaystation station;
	private Integer stationorder;
	private String otherinfo;//保存线路对应站点的首末班车时间[{"方向","时间"}{"方向","时间"}]
	
	public Line2Station(){}
	
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

	public Subwayline getLine() {
		return line;
	}

	public void setLine(Subwayline line) {
		this.line = line;
	}

	public Subwaystation getStation() {
		return station;
	}

	public void setStation(Subwaystation station) {
		this.station = station;
	}

	public Integer getStationorder() {
		return stationorder;
	}

	public void setStationorder(Integer stationorder) {
		this.stationorder = stationorder;
	}

	public String getOtherinfo() {
		return otherinfo;
	}

	public void setOtherinfo(String otherinfo) {
		this.otherinfo = otherinfo;
	}
}
