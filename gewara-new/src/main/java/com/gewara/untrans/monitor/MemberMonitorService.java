package com.gewara.untrans.monitor;

import javax.servlet.http.HttpServletRequest;

import com.gewara.support.ErrorCode;

public interface MemberMonitorService {
	enum CountType{getSeat}
	/**
	 * @param memberid
	 * @param type
	 * @param value
	 */
	ErrorCode increament(Long memberid, CountType type, HttpServletRequest request);
}
