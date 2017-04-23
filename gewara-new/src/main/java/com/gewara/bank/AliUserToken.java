package com.gewara.bank;

import java.sql.Timestamp;

import com.gewara.xmlbind.BaseInnerResponse;

public class AliUserToken extends BaseInnerResponse{
	private String userId;
	private String accessToken;
	private Integer expiresIn;
	private String refreshToken;
	private Integer reExpiresIn;
	private Timestamp begtime;
	public AliUserToken(){
		
	}
	
	public AliUserToken(String userId, String accessToken, Integer expiresIn, String refreshToken, Integer reExpiresIn) {
		super();
		this.userId = userId;
		this.accessToken = accessToken;
		this.expiresIn = expiresIn;
		this.refreshToken = refreshToken;
		this.reExpiresIn = reExpiresIn;
	}

	public String getUserId() {
		return userId;
	}
	public void setUserId(String userId) {
		this.userId = userId;
	}
	public String getAccessToken() {
		return accessToken;
	}
	public void setAccessToken(String accessToken) {
		this.accessToken = accessToken;
	}
	public Integer getExpiresIn() {
		return expiresIn;
	}
	public void setExpiresIn(Integer expiresIn) {
		this.expiresIn = expiresIn;
	}
	public String getRefreshToken() {
		return refreshToken;
	}
	public void setRefreshToken(String refreshToken) {
		this.refreshToken = refreshToken;
	}
	public Integer getReExpiresIn() {
		return reExpiresIn;
	}
	public void setReExpiresIn(Integer reExpiresIn) {
		this.reExpiresIn = reExpiresIn;
	}

	public Timestamp getBegtime() {
		return begtime;
	}

	public void setBegtime(Timestamp begtime) {
		this.begtime = begtime;
	}
	
}
