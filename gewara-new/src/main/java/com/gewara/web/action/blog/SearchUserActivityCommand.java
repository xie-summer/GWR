package com.gewara.web.action.blog;

import java.util.Date;

public class SearchUserActivityCommand {
	public String keyword;
	public String countycode;
	public Date from;
	public Date to;
	public String tag;
	public Long relatedid;
	public int pageNo = 0;
	public int rowsPerPage = 30; 
	public int getPageNo() {
		return pageNo;
	}
	public void setPageNo(int pageNo) {
		this.pageNo = pageNo;
	}
	public String getCountycode() {
		return countycode;
	}
	public void setCountycode(String countycode) {
		this.countycode = countycode;
	}
	public void setKeyword(String keyword) {
		if(keyword!=null && keyword.contains(" ‰»Î")) this.keyword=null;
		else this.keyword = keyword;
	}
	public String getKeyword() {
		return keyword;
	}
	public int getRowsPerPage() {
		return rowsPerPage;
	}
	public void setRowsPerPage(int rowsPerPage) {
		this.rowsPerPage = rowsPerPage;
	}
	public Date getFrom() {
		return from;
	}
	public void setFrom(Date from) {
		this.from = from;
	}
	public Date getTo() {
		return to;
	}
	public void setTo(Date to) {
		this.to = to;
	}
	public String getTag() {
		return tag;
	}
	public void setTag(String tag) {
		this.tag = tag;
	}
	public Long getRelatedid() {
		return relatedid;
	}
	public void setRelatedid(Long relatedid) {
		this.relatedid = relatedid;
	}
}
