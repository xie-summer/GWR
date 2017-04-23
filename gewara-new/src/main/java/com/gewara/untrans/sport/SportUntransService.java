package com.gewara.untrans.sport;

import java.util.List;
import java.util.Map;

import com.gewara.model.api.ApiUser;
import com.gewara.model.pay.GewaOrder;
import com.gewara.model.pay.SportOrder;
import com.gewara.model.sport.OpenTimeItem;
import com.gewara.model.sport.OpenTimeTable;
import com.gewara.model.user.Member;
import com.gewara.service.OrderException;
import com.gewara.support.ErrorCode;
import com.gewara.xmlbind.sport.RemoteMemberCardInfo;

public interface SportUntransService {
	
	/**
	 * 参加竞价
	 * @param otsid
	 * @param member
	 * @param price
	 * @param jointype
	 * @return
	 */
	ErrorCode<Map> openTimeSaleJoin(Long otsid, Member member, Integer price, String jointype);
	
	
	ErrorCode<Map> openTimeSaleCountdown(Long otsid);
	/**
	 *	取消订单 
	 */
	void cancelSportOrder(SportOrder order, Long memberid, String reason);

	ErrorCode<SportOrder> addSportOrder(OpenTimeTable ott, String fields, Long cardid, ErrorCode<RemoteMemberCardInfo> rmcode, String mobile, Member member) throws OrderException;
	ErrorCode<SportOrder> addSportOrder(OpenTimeTable ott, String fields, Long cardid, ErrorCode<RemoteMemberCardInfo> rmcode, String mobile, Member member,	ApiUser partner);
	ErrorCode<SportOrder> addSportOrder(Long otiid, String starttime, Integer time, Integer quantity, String mobile, Member member, ApiUser partner);
	ErrorCode<SportOrder> addSportOrder(Long otiid, String starttime, Integer time, Integer quantity, String mobile, Member member);
	ErrorCode<SportOrder> addSportOrder(Long otiid, String starttime, Integer quantity, String mobile, Member member, ApiUser parnter);
	ErrorCode<SportOrder> addSportOrder(Long otiid, String starttime, Integer quantity, String mobile, Member member);
	
	ErrorCode processSportOrder(SportOrder order, OpenTimeTable ott, List<OpenTimeItem> otiList) throws OrderException;
	int sysLockOti(OpenTimeTable ott);
	/**
	 * 确认订单
	 * */
	ErrorCode<String> fixOrder(GewaOrder order, OpenTimeTable ott);
	
	ErrorCode<String> updateCuOrder(GewaOrder order, OpenTimeTable ott);

}
