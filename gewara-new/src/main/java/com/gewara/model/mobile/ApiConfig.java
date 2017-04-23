package com.gewara.model.mobile;

import java.io.Serializable;
import java.sql.Timestamp;

import com.gewara.model.BaseObject;

public class ApiConfig extends BaseObject{
	private static final long serialVersionUID = 174545364169043717L;
	public static final Long API2_PARTNERS = 1L;		//支持api2接口的所有合作商的id
	public static final Long API2_GEWAMEMBER_PARTNERS = 2L; //支持api2接口的所有格瓦拉用户商户id
	public static final Long API2_MOBILE_PARTNERS_POINT_MOVIE = 3L; //电影--api2接口支持积分支付
	public static final Long API2_MOBILE_PARTNERS_POINT_SPORT = 4L; //运动--api2接口支持积分支付
	private Long id;
	private String content;
	private Timestamp addtime;
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public Timestamp getAddtime() {
		return addtime;
	}
	public void setAddtime(Timestamp addtime) {
		this.addtime = addtime;
	}
	@Override
	public Serializable realId() {
		return id;
	}
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
	
}
