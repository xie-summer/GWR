package com.gewara.service.sport;

import java.util.List;

import com.gewara.command.SearchOrderCommand;
import com.gewara.helper.order.OrderContainer;
import com.gewara.model.api.ApiUser;
import com.gewara.model.pay.GewaOrder;
import com.gewara.model.pay.MemberCardOrder;
import com.gewara.model.sport.MemberCardInfo;
import com.gewara.model.sport.MemberCardType;
import com.gewara.model.sport.OpenTimeItem;
import com.gewara.model.sport.OpenTimeTable;
import com.gewara.model.sport.Sport;
import com.gewara.model.user.Member;
import com.gewara.service.OrderException;
import com.gewara.support.ErrorCode;
import com.gewara.xmlbind.sport.RemoteMemberCardInfo;
import com.gewara.xmlbind.sport.RemoteMemberCardType;

public interface MemberCardService {
	List<MemberCardType> getMemberCardTypeListBySportids(Long sportid, boolean overNum);
	//根据场馆ID得到会员卡数量
	int getMemberCardTypeCountBySportids(Long sportid, boolean overNum);
	//根据项目ID得到会员卡数量
	int getMemberCardTypeCountBySportItemid(Long itemid);
	List<MemberCardInfo> getMemberCardInfoListByMemberid(Long memberid);
	ErrorCode<List<MemberCardInfo>> bindMemberCard(Member member, List<RemoteMemberCardInfo> rmciList);
	String getFitItem(String items);
	String getFitPlace(String venues);
	ErrorCode<MemberCardOrder> addMemberCardOrder(MemberCardType mct, Long placeid, String mobile, Member member, ApiUser partner) throws OrderException;
	ErrorCode<MemberCardOrder> addMemberCardOrder(MemberCardType mct, Long placeid, String mobile, Member member) throws OrderException;
	void cancelLockFailureOrder(MemberCardOrder order);
	ErrorCode<MemberCardOrder> processLastOrder(Long memberid, String ukey);
	void processMemberCardOrder(MemberCardOrder order, MemberCardType mct) throws OrderException;
	OrderContainer processOrderPay(MemberCardOrder order) throws OrderException;
	List<MemberCardOrder> getMemberCardOrderList(SearchOrderCommand soc);
	List<MemberCardType> synchMemberCardOrderList(List<RemoteMemberCardType> rmctList);
	List<MemberCardType> getBookingMemberCardTypeListBySportids(Long sportid);
	int getBookingMemberCardTypeCountBySportids(Long sportid);
	List<Sport> getFitSportList(String venues);
	List<MemberCardInfo> getValidMemberCardInfoListByMemberid(Long memberid, OpenTimeTable ott);
	ErrorCode<String> validCardByOtt(MemberCardInfo card, OpenTimeTable ott);
	ErrorCode<String> validCardByOtt(MemberCardType mct, MemberCardInfo card, OpenTimeTable ott, List<OpenTimeItem> otiList);
	ErrorCode<String> reMemberCardInfo(OpenTimeTable ott, Long cardid, ErrorCode<RemoteMemberCardInfo> rmcode, Member member);
	void memberCardPayOrder(GewaOrder order, Long memberId) throws OrderException;
	List<MemberCardInfo> getUnBindMemberCard(Member member, String mobile);
}
