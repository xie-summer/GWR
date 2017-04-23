/**
 * 
 */
package com.gewara.model.partner;

import java.io.Serializable;

import com.gewara.model.BaseObject;

public class PartnerMovie extends BaseObject {
	private static final long serialVersionUID = 4628750156257305466L;
	private Long id;
	private Long partnerid;
	private Long movieid;
	private String pmid;
	private String pmname;
	
	@Override
	public Serializable realId() {
		return id;
	}
	public Long getPartnerid() {
		return partnerid;
	}
	public void setPartnerid(Long partnerid) {
		this.partnerid = partnerid;
	}
	public String getPmid() {
		return pmid;
	}
	public void setPmid(String pmid) {
		this.pmid = pmid;
	}
	public PartnerMovie(){
		
	}
	public Long getMovieid() {
		return movieid;
	}
	public void setMovieid(Long movieid) {
		this.movieid = movieid;
	}
	
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getPmname() {
		return pmname;
	}
	public void setPmname(String pmname) {
		this.pmname = pmname;
	}
	
}
