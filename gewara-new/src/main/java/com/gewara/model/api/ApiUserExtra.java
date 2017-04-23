package com.gewara.model.api;

import java.io.Serializable;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import com.gewara.constant.Status;
import com.gewara.model.BaseObject;
import com.gewara.util.JsonUtils;

public class ApiUserExtra extends BaseObject {
	private static final long serialVersionUID = 2229124152288076967L;
	public static final String MOVIE_HIGHFIELDS = "movie_highfields";
	public static final String CINEMA_HIGHFIELDS = "cinema_highfields";
	public static final String OTHER_KEY_WEIXIN_BANK = "weixin_bank";
	public static final String OTHER_KEY_PAYMETHOD_VERSION = "paymethod_version";
	public static final String OTHER_KEY_SPID = "spid";
	private Long id;				//和ApiUser公用一个id
	private String openDiscount;	//优惠状态
	private String paymethod;		//支持的支付方式
	private String chargemethod;	//默认的充值方式
	private String specialmethod;	//特殊的支付方式，默认不显示，特价活动的时候显示
	private String proxyqry;		//代理查询的url
	private String authFields;		//
	private String sourcemethod;
	private String otherinfo;
	public ApiUserExtra(){
		
	}
	public ApiUserExtra(Long uid){
		this.id = uid;
		this.otherinfo = "{}";
	}
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getOpenDiscount() {
		return openDiscount;
	}
	public void setOpenDiscount(String openDiscount) {
		this.openDiscount = openDiscount;
	}
	public String getPaymethod() {
		return paymethod;
	}
	public String getPaymethodByCategory(String category) {
		if(StringUtils.isBlank(category) || StringUtils.isBlank(sourcemethod)){
			return paymethod;
		}
		Map<String, String> sourceMap = JsonUtils.readJsonToMap(sourcemethod);
		if(!sourceMap.containsKey(category)){
			return paymethod;
		}
		return sourceMap.get(category);
	}
	public void setPaymethod(String paymethod) {
		this.paymethod = paymethod;
	}
	public String getChargemethod() {
		return chargemethod;
	}
	public void setChargemethod(String chargemethod) {
		this.chargemethod = chargemethod;
	}
	public String getProxyqry() {
		return proxyqry;
	}
	public void setProxyqry(String proxyqry) {
		this.proxyqry = proxyqry;
	}
	@Override
	public Serializable realId() {
		return id;
	}
	public String getSpecialmethod() {
		return specialmethod;
	}
	public void setSpecialmethod(String specialmethod) {
		this.specialmethod = specialmethod;
	}
	public String getAuthFields() {
		return authFields;
	}
	public void setAuthFields(String authFields) {
		this.authFields = authFields;
	}

	public String getSourcemethod() {
		return sourcemethod;
	}
	public void setSourcemethod(String sourcemethod) {
		this.sourcemethod = sourcemethod;
	}
	public String getOtherinfo() {
		return otherinfo;
	}
	public void setOtherinfo(String otherinfo) {
		this.otherinfo = otherinfo;
	}
	
	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	public String getAllPaymethod(){
		String pm = "";
		if(StringUtils.isNotBlank(paymethod)){
			pm = paymethod;
			if(StringUtils.isNotBlank(specialmethod)) pm = pm + "," + specialmethod;
		}
		return pm;
	}
	public String getAllPaymethodByCategory(String category){
		if(StringUtils.isBlank(category) || StringUtils.isBlank(sourcemethod)){
			return getAllPaymethod();
		}
		Map<String, String> sourceMap = JsonUtils.readJsonToMap(sourcemethod);
		if(!sourceMap.containsKey(category)){
			return getAllPaymethod();
		}
		String pm = sourceMap.get(category) + "," + specialmethod;
		return pm;
	}
	public boolean hasMovieHighFields(){
		Map<String, String> map = JsonUtils.readJsonToMap(this.authFields);
		String res = map.get(MOVIE_HIGHFIELDS);
		if(StringUtils.equals(res, Status.Y)) return true;
		return false;
	}
	public boolean hasCinemaHighFields(){
		Map<String, String> map = JsonUtils.readJsonToMap(this.authFields);
		String res = map.get(CINEMA_HIGHFIELDS);
		if(StringUtils.equals(res, Status.Y)) return true;
		return false;
	}
}
