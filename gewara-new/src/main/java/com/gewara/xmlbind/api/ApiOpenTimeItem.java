package com.gewara.xmlbind.api;



//类似影院座位
public class ApiOpenTimeItem{
	private Long id;
	private Long ottid;			//场次ID
	private Long fieldid;		//场地ID
	private String fieldname;	//场地名
	private String hour;			//时间点
	private Integer norprice;	//标准价
	private String status;		//场地状态
	private String playtime;	
	
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (!(o instanceof ApiOpenTimeItem))
			return false;
		final ApiOpenTimeItem temp = (ApiOpenTimeItem) o;
		return !(getId() != null ? !(getId().equals(temp.getId())) : (temp
				.getId() != null));
	}

	public int hashCode() {
		return (getId() != null ? getId().hashCode() : 0);
	}
	public String getHour() {
		return hour;
	}
	public void setHour(String hour) {
		this.hour = hour;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public String getPlaytime() {
		return playtime;
	}
	public void setPlaytime(String playtime) {
		this.playtime = playtime;
	}
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}

	public Long getFieldid() {
		return fieldid;
	}
	public void setFieldid(Long fieldid) {
		this.fieldid = fieldid;
	}
	public Long getOttid() {
		return ottid;
	}
	public void setOttid(Long ottid) {
		this.ottid = ottid;
	}
	public String getFieldname() {
		return fieldname;
	}
	public void setFieldname(String fieldname) {
		this.fieldname = fieldname;
	}

	public Integer getNorprice() {
		return norprice;
	}

	public void setNorprice(Integer norprice) {
		this.norprice = norprice;
	}
}
