package com.gewara.xmlbind.drama.gptbs;


public class PlaceFieldArea {

	private Long id;
	
	private String cnName;				//区域中文名
	private String enName;				//区域英文名
	private String description;			//区域描述
	private Long venueId;				//所属场地编号
	private Integer gridWidth;			//表格宽度
	private Integer gridHeight;			//表格高度
	private String standing;
	private String hotZone;
	private Integer total;
	private Integer limit;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}
	
	public String getCnName() {
		return cnName;
	}

	public void setCnName(String cnName) {
		this.cnName = cnName;
	}

	public String getEnName() {
		return enName;
	}

	public void setEnName(String enName) {
		this.enName = enName;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Long getVenueId() {
		return venueId;
	}

	public void setVenueId(Long venueId) {
		this.venueId = venueId;
	}

	public Integer getGridWidth() {
		return gridWidth;
	}

	public void setGridWidth(Integer gridWidth) {
		this.gridWidth = gridWidth;
	}

	public Integer getGridHeight() {
		return gridHeight;
	}

	public void setGridHeight(Integer gridHeight) {
		this.gridHeight = gridHeight;
	}

	public String getStanding() {
		return standing;
	}

	public void setStanding(String standing) {
		this.standing = standing;
	}

	public String getHotZone() {
		return hotZone;
	}

	public void setHotZone(String hotZone) {
		this.hotZone = hotZone;
	}

	public Integer getTotal() {
		return total;
	}

	public void setTotal(Integer total) {
		this.total = total;
	}

	public Integer getLimit() {
		return limit;
	}

	public void setLimit(Integer limit) {
		this.limit = limit;
	}
	

}
