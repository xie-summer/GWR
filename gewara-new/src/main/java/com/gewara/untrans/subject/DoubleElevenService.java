package com.gewara.untrans.subject;

import java.util.Map;

import com.gewara.support.ErrorCode;

public interface DoubleElevenService {
	
	public Integer getTodayWinnerCount(Long memberid, String tag);

	// 抽奖
	public ErrorCode<String> drawClick(Long memberid, String tag, String ip, Integer dayCount);

	// 得到抽奖时间
	public ErrorCode<String> getClickTime(Long memberid, String tag);

	// 得到抽奖次数
	public ErrorCode<String> getClickCount(Long memberid, String tag);

	// 保存分享微博
	public ErrorCode<String> saveShareWeibo(Long memberid, String tag, String source);

	Map getShareStatusMap(Long memberid, String tag);
}
