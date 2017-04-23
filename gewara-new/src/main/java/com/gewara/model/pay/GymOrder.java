package com.gewara.model.pay;


public class GymOrder extends GewaOrder {
	private static final long serialVersionUID = -7145452092164851319L;
	public static final String KEY_REMOTE_GYM = "remoteGymId";
	public static final String KEY_SPECAILID = "specialid";
	public static final String KEY_STARTDATE = "startdate";
	public static final String GYM_CONFIRM = "gymconfirm";
	public static final String KEY_ENDDATE = "enddate";
	private Long gymid;
	private Long gci;
	private Long oci;
	private Integer costprice;
	
	public GymOrder(){}
	
	public Long getGymid() {
		return gymid;
	}

	public void setGymid(Long gymid) {
		this.gymid = gymid;
	}

	public Long getGci() {
		return gci;
	}

	public void setGci(Long gci) {
		this.gci = gci;
	}

	public Integer getCostprice() {
		return costprice;
	}

	public void setCostprice(Integer costprice) {
		this.costprice = costprice;
	}

	public Long getOci() {
		return oci;
	}

	public void setOci(Long oci) {
		this.oci = oci;
	}

	@Override
	public String getOrdertype() {
		return "gym";
	}
}
