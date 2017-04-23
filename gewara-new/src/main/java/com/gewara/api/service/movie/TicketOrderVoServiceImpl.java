package com.gewara.api.service.movie;

import java.sql.Timestamp;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import com.gewara.api.vo.ResultCode;
import com.gewara.api.vo.order.TicketOrderVo;
import com.gewara.constant.ApiConstant;
import com.gewara.constant.ticket.OrderConstant;
import com.gewara.model.movie.CinemaProfile;
import com.gewara.model.pay.TicketOrder;
import com.gewara.model.ticket.OpenPlayItem;
import com.gewara.service.impl.BaseServiceImpl;
import com.gewara.service.order.OrderQueryService;
import com.gewara.util.DateUtil;
import com.gewara.util.VoCopyUtil;

public class TicketOrderVoServiceImpl extends BaseServiceImpl implements TicketOrderVoService{
	@Autowired@Qualifier("orderQueryService")
	private OrderQueryService orderQueryService;
	@Override
	public ResultCode<List<TicketOrderVo>> getTicketOrderListByMpid(Long mpid,Timestamp startTime,Timestamp endTime,String timeType
			,String openType) {
		OpenPlayItem opi = baseDao.getObjectByUkey(OpenPlayItem.class, "mpid", mpid);
		if(opi == null){
			return ResultCode.getFailure(ApiConstant.CODE_DATA_ERROR, "数据不存在！");
		}
		if(StringUtils.equals("SYS", openType)){
			CinemaProfile profile = baseDao.getObject(CinemaProfile.class, opi.getCinemaid());
			if(profile != null){
				openType = profile.getOpentype();
			}
		}
		List<TicketOrder> orderList = orderQueryService.getTicketOrderListByMpid(mpid, OrderConstant.STATUS_PAID_SUCCESS);
		List<TicketOrder> resultList = new LinkedList<TicketOrder>();
		if(startTime != null && endTime != null){
			for(TicketOrder order : orderList){
				Timestamp time = "addtime".equals(timeType) ? order.getAddtime() : order.getPlaytime();
				if(DateUtil.after(time, startTime) >= 0 && endTime != null && DateUtil.after(time,endTime) <= 0){
					if(StringUtils.isBlank(openType) || StringUtils.equals(order.getCategory(), openType)){
						resultList.add(order);
					}
				}
			}
		}else{
			resultList = orderList;
		}
		return VoCopyUtil.copyListProperties(TicketOrderVo.class, resultList);
	}
}
