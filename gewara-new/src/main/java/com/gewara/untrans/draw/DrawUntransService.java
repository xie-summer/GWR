package com.gewara.untrans.draw;

import com.gewara.model.user.Member;
import com.gewara.support.ErrorCode;


public interface DrawUntransService {
	
	/**
	 * 抽奖统一方法
	 */
	ErrorCode<String> clickDraw(Member member, String tag, String source, String pricategory, String citycode, String pointxy, String ip);
	ErrorCode<String> clickDraw(Member member, String tag, String source, String pricategory, String citycode, String pointxy, String ip, boolean isMaxCount, Integer dayCount);
}
