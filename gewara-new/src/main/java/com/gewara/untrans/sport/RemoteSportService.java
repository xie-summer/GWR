package com.gewara.untrans.sport;

import java.util.Date;
import java.util.List;

import com.gewara.model.pay.SportOrder;
import com.gewara.model.sport.OpenTimeTable;
import com.gewara.support.ErrorCode;
import com.gewara.xmlbind.sport.GstOtt;
import com.gewara.xmlbind.sport.GstSportField;

public interface RemoteSportService {
	
	ErrorCode<List<GstOtt>> getGstOttList(Long sportid, Long itemid, Date playdate, String ge);
	
	ErrorCode<List<GstSportField>> getGstSportFieldList(Long sportid, Long itemid);
	
	ErrorCode<String> getGstItemIdList(Long sportid);
	
	ErrorCode<String> lockOrder(OpenTimeTable ott, List<Long> remoteIdList, String type);
	
	ErrorCode<String> unLockOrder(OpenTimeTable ott, List<Long> remoteIdList);
	
	ErrorCode<String> fixOrder(SportOrder order, OpenTimeTable ott, List<Long> remoteIdList, String vipCard, String cname);
	
	ErrorCode<String> refundOrder(SportOrder order);
	
	ErrorCode<List<Long>> getRemoteLockItem(OpenTimeTable ott);
	
	ErrorCode checkOrder(SportOrder order);
}
