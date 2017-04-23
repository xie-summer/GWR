package com.gewara.untrans.ticket;

import java.util.Map;

import com.gewara.support.ErrorCode;
import com.gewara.untrans.monitor.ConfigTrigger;

/**
 * 购票排队机制
 * 1、商家数量控制
 * 2、每个用户控制
 * 3、每个IP控制
 * 4、所有用户总控制
 * @author acerge(acerge@163.com)
 * @since 9:29:14 AM Oct 26, 2011
 */
public interface TicketQueueService extends ConfigTrigger{
	ErrorCode isMemberAllowed(Long memberid, Long cinemaid, String ip);
	ErrorCode isPartnerAllowed(Long partnerid, Long cinemaid);
	Map getStatistics();
	void clearData();
}
