package com.gewara.untrans.sport;

import java.util.List;

import com.gewara.model.pay.MemberCardOrder;
import com.gewara.model.pay.SportOrder;
import com.gewara.model.sport.MemberCardInfo;
import com.gewara.model.sport.MemberCardType;
import com.gewara.support.ErrorCode;
import com.gewara.xmlbind.sport.RemoteCardPayOrder;
import com.gewara.xmlbind.sport.RemoteMemberCardInfo;
import com.gewara.xmlbind.sport.RemoteMemberCardOrder;
import com.gewara.xmlbind.sport.RemoteMemberCardType;

public interface RemoteMemberCardService {
	ErrorCode<List<RemoteMemberCardType>> getRemoteMemberCardTypeListBySportid(String sportids);
	ErrorCode<List<RemoteMemberCardInfo>> getRemoteMemberCardInfoListByCheckpass(String mobile, String checkpass);
	ErrorCode<RemoteMemberCardOrder> createRemoteMemberCardOrder(MemberCardOrder order, MemberCardType mct);
	ErrorCode<RemoteMemberCardOrder> commitRemoteMemberCardOrder(MemberCardOrder order);
	ErrorCode<String> getMobileCheckpass(String mobile, String type);
	ErrorCode<RemoteMemberCardInfo> cardPay(SportOrder order, MemberCardType mct, MemberCardInfo mci, String checkpass);
	ErrorCode<RemoteCardPayOrder> getRemoteCardPayOrder(MemberCardOrder order);
	ErrorCode<RemoteMemberCardOrder> getRemoteMemberCardOrderByTradeno(MemberCardOrder order);
	ErrorCode<RemoteMemberCardInfo> getMemberCardInfo(MemberCardInfo card);
	ErrorCode<RemoteMemberCardType> getRemoteMemberCardTypeByKey(String cardtypeKey);
}
