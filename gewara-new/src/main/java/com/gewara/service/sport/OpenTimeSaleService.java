package com.gewara.service.sport;

import java.sql.Timestamp;
import java.util.List;
import java.util.Map;

import com.gewara.model.pay.SMSRecord;
import com.gewara.model.pay.SportOrder;
import com.gewara.model.sport.OpenTimeSale;
import com.gewara.model.sport.OpenTimeSaleMember;
import com.gewara.model.sport.SellDeposit;
import com.gewara.model.user.Member;
import com.gewara.service.BaseService;
import com.gewara.service.OrderException;
import com.gewara.support.ErrorCode;

public interface OpenTimeSaleService extends BaseService {

	List<OpenTimeSale> getJoinOtsList(String citycode, Long memberid, boolean valid, String orderField, boolean asc, int from, int maxnum);
	
	List<OpenTimeSale> getNojoinOtsByPaidDeposit(String citycode, Long memberid, boolean valid);
	
	Integer getJoinOtsCount(String citycode, Long memberid, boolean valid);
	
	List<OpenTimeSaleMember> getOtsMemberList(Long otsid, Long memberid, int from, int maxnum);
	
	List<OpenTimeSaleMember> getOtsMemberList(Long otsid, Long memberid);
	
	List<Long> getOtsIdListMemberJoin(Long memberid);
	
	Integer getOtsMemberCount(Long otsid, Long memberid);
	
	Integer getOtsMemberCount(Long otsid, Timestamp addtime);
	
	ErrorCode<Map> joinOpenTimeSale(Long otsid, SellDeposit deposit, Member member, Integer pice, Timestamp addtime) throws OrderException;
	
	OpenTimeSaleMember getLastOtsMember(Long otsid);
	
	SMSRecord sendMessage(SportOrder order, OpenTimeSale ots);
	//Î´½áÊøÎ´µÄ¾ºÅÄ
	List<OpenTimeSale> getOpenOtsList(String citycode, String orderField, boolean asc, int from, int maxnum); 
}
