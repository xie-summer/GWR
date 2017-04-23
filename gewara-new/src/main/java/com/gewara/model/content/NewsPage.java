package com.gewara.model.content;

import java.io.Serializable;

import com.gewara.model.BaseObject;



/**
 * @author <a href="mailto:acerge@163.com">gebiao(acerge)</a>
 * @since 2007-9-28ÏÂÎç02:05:17
 */
public class NewsPage extends BaseObject {

	private static final long serialVersionUID = 2547978194899662583L;
	private Long id;
	private Integer pageno;
	private Long newsid;
	private String content;
	
	public NewsPage(){}
	
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

	public Integer getPageno() {
		return pageno;
	}

	public void setPageno(Integer pageno) {
		this.pageno = pageno;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public Long getNewsid() {
		return newsid;
	}

	public void setNewsid(Long newsid) {
		this.newsid = newsid;
	}
}
