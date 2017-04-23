package com.gewara.untrans.subject;

import com.gewara.model.user.TempMember;
import com.gewara.support.ErrorCode;

public interface BaiFuBaoService {

	// 抽奖
	ErrorCode<String> drawClick(Long memberid, String ip);
	// 参加人数
	long joinCount();
	// 是否已有资格码
	boolean hasQualifications(Long memberid);
	ErrorCode<String> getPayUrl(TempMember tm);
	String queryOrder(String tradeNo);
	void refreshCounter();
	ErrorCode<TempMember> processPaySuccess(Long tmid);
	ErrorCode<String> checkStatus(String mobile, String password);

}
