package com.gewara.json;

import java.util.Date;

public class PageView {
	private Date validtime;
	private String content;
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
	public PageView(Long cachedtime, String content) {
		this.content = content;
		this.validtime = new Date(cachedtime);
	}
	public Date getValidtime() {
		return validtime;
	}
	public void setValidtime(Date validtime) {
		this.validtime = validtime;
	}
}
