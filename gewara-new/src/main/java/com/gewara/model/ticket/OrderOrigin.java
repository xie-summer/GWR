package com.gewara.model.ticket;

import java.io.Serializable;
import java.sql.Timestamp;

import org.apache.commons.lang.StringUtils;

import com.gewara.constant.ticket.PartnerConstant;
import com.gewara.model.BaseObject;
import com.gewara.util.DateUtil;

/**
 * ¶©µ¥À´Ô´
 * @author acerge(acerge@163.com)
 * @since 4:18:55 PM Sep 6, 2010
 */
public class OrderOrigin extends BaseObject  {
	private static final long serialVersionUID = 5237028146567241944L;
	private String tradeNo;
	private String origin;
	private Long partnerid;
	private String appSource;
	private Timestamp addtime;
	public OrderOrigin(){}
	public OrderOrigin(String tradeNo, String origin) {
		this.tradeNo = tradeNo;
		this.origin = origin;
		this.addtime = DateUtil.getCurFullTimestamp();
		if(StringUtils.isNotBlank(origin)){
			if(origin.startsWith("imax")){
				this.partnerid = PartnerConstant.PARTNER_IMAX;
			}else if(origin.startsWith("qieke")){
				this.partnerid = PartnerConstant.PARTNER_QIEKE;
			}else if(origin.startsWith("baidu")){
				this.partnerid = PartnerConstant.PARTNER_BAIDU;
			}else if(origin.startsWith("ddmap")){
				this.partnerid = 50000570L;
			}else if(origin.startsWith("souku")){
				this.partnerid = 50000502L;
			}else if(origin.startsWith("jimi")){
				this.partnerid = 50000893L;
			}
		}
	}
	@Override
	public Serializable realId() {
		return tradeNo;
	}
	public String getTradeNo() {
		return tradeNo;
	}
	public void setTradeNo(String tradeNo) {
		this.tradeNo = tradeNo;
	}
	public String getOrigin() {
		return origin;
	}
	public void setOrigin(String origin) {
		this.origin = origin;
	}
	public Timestamp getAddtime() {
		return addtime;
	}
	public void setAddtime(Timestamp addtime) {
		this.addtime = addtime;
	}
	public Long getPartnerid() {
		return partnerid;
	}
	public void setPartnerid(Long partnerid) {
		this.partnerid = partnerid;
	}
	public String getAppSource() {
		return appSource;
	}
	public void setAppSource(String appSource) {
		this.appSource = appSource;
	}
}
