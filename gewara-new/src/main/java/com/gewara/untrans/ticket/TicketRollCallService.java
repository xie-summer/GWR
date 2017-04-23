package com.gewara.untrans.ticket;

import java.util.Date;
import java.util.List;

import org.springframework.data.mongodb.core.query.Query;

import com.gewara.json.TicketRollCallMember;
import com.gewara.support.ErrorCode;


public interface TicketRollCallService {
	/**
	 * 记录每天购票用户信息, 购票数限制在小于等于checkCount,大于checkCount则列入黑名单
	 * @param memberid	用户ID
	 * @param mobile	手机号	
	 * @param tag	关联类型（例：tag = 'cinema',则 relatedid 为影院ID）	
	 * @param relatedid	关联类型ID
	 * @param quantity 购票数
	 * @param checkCount 限制购票数
	 * @return 
	 */
	ErrorCode<String> saveOrUpdateTicketRollCall(Long memberid, String mobile, String tag, Long relatedid, int quantity, int checkCount);
	
	/**
	 * 用户或手机号限制列表数量查询
	 * @param status	状态	
	 * @param startDate	开始时间段
	 * @param endDate 结束时间段
	 * @param mobiles	用户ID或手机号数组
	 * @return 
	 */
	Integer getTicketRollCallMemberCount(String status, Date startDate, Date endDate, String... mobiles);
	
	/**
	 * 用户或手机号限制列表查询
	 * @param status	状态 (status ='D' 为黑名单	, status = 'Y' 为白名单)
	 * @param startDate	开始时间段
	 * @param endDate 结束时间段 
	 * @param mobiles	用户ID或手机号数组
	 * @return 
	 */
	List<TicketRollCallMember>  getTicketRollCallMemberList(String status, Date startDate, Date endDate, int from, int maxnum, String... mobiles);
	/**
	 * 
	 * @param memberid 用户ID
	 * @prama mobile 手机号
	 * @return 
	 */
	boolean isTicketRollCallMember(Long memberid, String mobile);
	
	/**
	 * 通过类型、类型ID、时间段、手机或用户ID集合获得TicketRollCall 对象查询
	 * @param tag	对象类型（如：cinema 等，可为空）
	 * @param relatedid	对象类型ID （可为空）
	 * @param startDate	开始时间	（可为空）
	 * @param endDate	结束时间	 （可为空）
	 * @param mobiles	手机、用户ID集合 
	 * @return	TicketRollCall 对象查询
	 */
	Query getTicketRollCallQuery(String tag, Long relatedid, Date startDate, Date endDate, String... mobiles);

	/**
	 * 移除黄牛名单
	 * @param id
	 * @return
	 */
	boolean removeRollCallMember(String id);

	/**
	 * 增加黄牛名单
	 * @param mobile
	 * @param status
	 * @param reason
	 * @param userid
	 * @return
	 */
	ErrorCode addTicketRollMember(String mobile, String status, String reason, Long userid);
}
