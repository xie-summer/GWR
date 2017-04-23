package com.gewara.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component("sportAPIConfig")
public class SportAPIConfig {
	
	@Value("${merId}")
	private String merId;
	@Value("${memberCardtypeListUrl}")
	private String memberCardtypeListUrl;
	@Value("${memberCardtypeUrl}")
	private String memberCardtypeUrl;
	@Value("${qryMemberCardListUrl}")
	private String qryMemberCardListUrl;
	@Value("${createMemberCardOrderUrl}")
	private String createMemberCardOrderUrl;
	@Value("${commitMemberCardOrderUrl}")
	private String commitMemberCardOrderUrl;
	@Value("${mobileCheckpassUrl}")
	private String mobileCheckpassUrl;
	@Value("${cardPayUrl}")
	private String cardPayUrl;
	@Value("${memberCardOrderByTradeNoUrl}")
	private String memberCardOrderByTradeNoUrl;
	@Value("${memberCardInfoUrl}")
	private String memberCardInfoUrl;
	@Value("${cardPayResultUrl}")
	private String cardPayResultUrl;
	@Value("${fieldListUrl}")
	private String fieldListUrl;
	@Value("${ottListUrl}")
	private String ottListUrl;
	@Value("${toLockOtiUrl}")
	private String toLockOtiUrl;
	@Value("${unLockOtiUrl}")
	private String unLockOtiUrl;
	@Value("${lockOtiListUrl}")
	private String lockOtiListUrl;
	@Value("${fixOrderUrl}")
	private String fixOrderUrl;
	@Value("${queryOrderUrl}")
	private String queryOrderUrl;
	@Value("${refundUrl}")
	private String refundUrl;
	@Value("${itemListUrl}")
	private String itemListUrl;
	public String getMemberCardtypeListUrl() {
		return memberCardtypeListUrl;
	}
	public String getMemberCardtypeUrl() {
		return memberCardtypeUrl;
	}
	public String getQryMemberCardListUrl() {
		return qryMemberCardListUrl;
	}
	public String getCreateMemberCardOrderUrl() {
		return createMemberCardOrderUrl;
	}
	public String getCommitMemberCardOrderUrl() {
		return commitMemberCardOrderUrl;
	}
	public String getMobileCheckpassUrl() {
		return mobileCheckpassUrl;
	}
	public String getCardPayUrl() {
		return cardPayUrl;
	}
	public String getMemberCardOrderByTradeNoUrl() {
		return memberCardOrderByTradeNoUrl;
	}
	public String getMemberCardInfoUrl() {
		return memberCardInfoUrl;
	}
	public String getCardPayResultUrl() {
		return cardPayResultUrl;
	}
	public String getFieldListUrl() {
		return fieldListUrl;
	}
	public String getOttListUrl() {
		return ottListUrl;
	}
	public String getToLockOtiUrl() {
		return toLockOtiUrl;
	}
	public String getUnLockOtiUrl() {
		return unLockOtiUrl;
	}
	public String getLockOtiListUrl() {
		return lockOtiListUrl;
	}
	public String getFixOrderUrl() {
		return fixOrderUrl;
	}
	public String getQueryOrderUrl() {
		return queryOrderUrl;
	}
	public String getRefundUrl() {
		return refundUrl;
	}
	public String getItemListUrl() {
		return itemListUrl;
	}
	public String getMerId() {
		return merId;
	}
	
}
