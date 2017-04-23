package com.gewara.service.api.impl;

import java.sql.Timestamp;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;

import com.gewara.model.api.ApiUser;
import com.gewara.model.api.OrderResult;
import com.gewara.model.api.Synch;
import com.gewara.service.api.ApiService;
import com.gewara.service.impl.BaseServiceImpl;
import com.gewara.util.DateUtil;
@Service("apiService")
public class ApiServiceImpl extends BaseServiceImpl implements ApiService{
	@Override
	public List<ApiUser> getApiUserList(String status) {
		String query ="from ApiUser where status=? order by id";
		List<ApiUser> result = hibernateTemplate.find(query, status);
		return result;
	}
	@Override
	public Synch saveSynchWithCinema(Synch synch, Timestamp updatetime, Timestamp successtime, String ticketnum, String ip) {
		if(updatetime!=null) synch.setSynchtime(updatetime);
		if(successtime!=null) synch.setSuccesstime(successtime);
		if(ticketnum!=null) synch.setTicketnum(ticketnum);
		if(ip!=null) synch.setIp(ip);
		baseDao.saveObject(synch);
		return synch;
	}
	
	@Override
	public Synch saveSynchGoodsWithCinema(Synch synch, Timestamp updatetime, Timestamp successtime, String ticketnum, String ip) {
		if(updatetime!=null){ 
			synch.setGsyntime(updatetime);
			synch.setSynchtime(updatetime);
		}
		if(successtime!=null){
			synch.setGsuctime(successtime);
			synch.setSuccesstime(successtime);
		}
		if(ticketnum!=null) synch.setGticketnum(ticketnum);
		if(ip!=null) synch.setIp(ip);
		baseDao.saveObject(synch);
		return synch;
	}
	@Override
	public void saveOrderResult(String[] tradeMap) {
		OrderResult orderResult = baseDao.getObject(OrderResult.class, tradeMap[0]);
		if(orderResult==null){
			orderResult = new OrderResult();
			orderResult.setTradeno(StringUtils.trim(tradeMap[0]));
			orderResult.setCaption(tradeMap[1]);
			orderResult.setIstake("Y");
			orderResult.setConflict("Y");
			orderResult.setResult("N");
			orderResult.setUpdatetime(DateUtil.getMillTimestamp());
			baseDao.saveObject(orderResult);
		}else{
			orderResult.setCaption(tradeMap[1]);
			orderResult.setResult("N");
			orderResult.setConflict("Y");
			orderResult.setUpdatetime(DateUtil.getMillTimestamp());
			baseDao.saveObject(orderResult);
		}
	}
}
