/**
 * 
 */
package com.gewara.model.partner;

import java.io.Serializable;

import com.gewara.model.BaseObject;

public class PartnerCinema extends BaseObject {
	private static final long serialVersionUID = 1555548142266706539L;
	private Long id;
	private Long partnerid;	//合作商id
	private Long cinemaid; 	// 格瓦拉影院ID
	private String pcid; 		// 合作伙伴影院ID
	private String pcname;
	private String cityname;
	@Override
	public Serializable realId() {
		return id;
	}
	public PartnerCinema() {

	}

	public Long getPartnerid() {
		return partnerid;
	}

	public void setPartnerid(Long partnerid) {
		this.partnerid = partnerid;
	}

	public String getPcid() {
		return pcid;
	}

	public void setPcid(String pcid) {
		this.pcid = pcid;
	}

	public Long getCinemaid() {
		return cinemaid;
	}

	public void setCinemaid(Long cinemaid) {
		this.cinemaid = cinemaid;
	}
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}


	public String getCityname() {
		return cityname;
	}

	public void setCityname(String cityname) {
		this.cityname = cityname;
	}

	public String getPcname() {
		return pcname;
	}

	public void setPcname(String pcname) {
		this.pcname = pcname;
	}
}
