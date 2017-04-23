package com.gewara.untrans;

import java.util.Map;

public interface MemberCountService {
	int getFansCountByMemberId(Long memberid);

	/**
	 * 获取用户聚合数据（）
	 * 
	 * @param memberid
	 * @return
	 */
	Map getMemberCount(Long memberid);

	/**
	 * 
	 * @param id
	 *            用户ID
	 * @param key
	 *            聚合数据KEY
	 * @param value
	 *            聚合数据值
	 * @param isAdd
	 *            isAdd = true 为增加 value, 反之减去 value, 结果小于0则为0;
	 */
	void updateMemberCount(Long memberid, String key, int value, boolean isAdd);

	/**
	 * 用户行为统计
	 * 
	 * @param memberid
	 * @param key
	 * @param value
	 * @param isReplace
	 */
	void saveMemberCount(Long memberid, String key, String value, boolean isReplace);

	/**
	 * @param memberid
	 * @param key
	 * @param value
	 * @param isReplace
	 * @return
	 */
	void saveMemberCount(Long memberid, String key, String value, Integer maxnum, boolean isReplace);

	/**
	 * 获取用户聚合数据
	 * @param memberid
	 * @return
	 */
	Map getMemberInfoStats(Long memberid);
	/**
	 * 保存用户的最后一次下单数据
	 * @param mobile
	 * @param tradeNo
	 * @param orderType
	 * @param time
	 */
	void saveMobileLastTicket(String mobile,String tradeNo,String orderType,String time);
	/**
	 * 获取指定手机号的最近一次订单
	 * @param mobile
	 * @return
	 */
	String getMobileLastTrade(String mobile);
	/**
	 * 保存用户下的第一笔订单
	 * @param memberid
	 * @param tradeNO
	 * @param orderType
	 */
	void saveMbrFirstTicket(Long memberid, String tradeNo, String orderType);
}
