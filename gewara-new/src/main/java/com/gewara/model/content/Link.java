package com.gewara.model.content;

import java.io.Serializable;
import java.sql.Timestamp;

import org.apache.commons.lang.StringUtils;

import com.gewara.model.BaseObject;

public class Link extends BaseObject {
	private static final long serialVersionUID = -3561472067887809693L;
	public static final String TYPE_TEXT = "text";	//ÎÄ×ÖÁ´½Ó
	public static final String TYPE_PICTURE = "picture";//Í¼Æ¬Á´½Ó
	private Long id;
	private String title;
	private String url;
	private String logo;
	private String type;
	private Timestamp addtime;
	private Timestamp updatetime;
	
	public Link() {}
	
	public Link(String title){
		this.addtime = new Timestamp(System.currentTimeMillis());
		this.updatetime = addtime;
		this.title = title;
	}
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

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getLogo() {
		return logo;
	}
	public String getLimg() {
		if(StringUtils.isBlank(logo)) return "img/default_head.png";
		return logo;
	}
	public void setLogo(String logo) {
		this.logo = logo;
	}

	public Timestamp getAddtime() {
		return addtime;
	}

	public void setAddtime(Timestamp addtime) {
		this.addtime = addtime;
	}

	public Timestamp getUpdatetime() {
		return updatetime;
	}

	public void setUpdatetime(Timestamp updatetime) {
		this.updatetime = updatetime;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}
	public String getRlogo(String basePath){
		if(TYPE_PICTURE.equals(type) && StringUtils.isNotBlank(logo) && logo.startsWith("images")){
			return basePath + logo;
		}
		return logo;
	}
}
