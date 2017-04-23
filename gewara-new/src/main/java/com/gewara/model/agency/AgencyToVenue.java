package com.gewara.model.agency;

import java.io.Serializable;

import com.gewara.model.BaseObject;

/**
 * @author wkxyl9
 * @since 2013-03-14 16:20:00
 */
public class AgencyToVenue extends BaseObject{
	private static final long serialVersionUID = 4222466018689845698L;
	private Long id;
	private Long agencyId;
	private Long venueId;
	private String agencytype;
	private Integer numsort;

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

	public Long getAgencyId() {
		return agencyId;
	}

	public void setAgencyId(Long agencyId) {
		this.agencyId = agencyId;
	}

	public Long getVenueId() {
		return venueId;
	}

	public void setVenueId(Long venueId) {
		this.venueId = venueId;
	}

	public Integer getNumsort() {
		return numsort;
	}

	public void setNumsort(Integer numsort) {
		this.numsort = numsort;
	}

	public String getAgencytype() {
		return agencytype;
	}

	public void setAgencytype(String agencytype) {
		this.agencytype = agencytype;
	}

}
