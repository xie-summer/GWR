/**
 * 
 */
package com.gewara.job.impl;

import java.sql.Timestamp;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import com.gewara.constant.OpenTimeTableConstant;
import com.gewara.constant.ticket.OrderConstant;
import com.gewara.job.JobService;
import com.gewara.job.SportOrderJob;
import com.gewara.model.pay.SMSRecord;
import com.gewara.model.pay.SportOrder;
import com.gewara.model.sport.CusOrder;
import com.gewara.model.sport.OpenTimeSale;
import com.gewara.service.DaoService;
import com.gewara.service.OrderException;
import com.gewara.service.api.ApiSportService;
import com.gewara.service.sport.OpenTimeSaleService;
import com.gewara.service.sport.SportOrderService;
import com.gewara.support.ErrorCode;
import com.gewara.untrans.UntransService;
import com.gewara.untrans.monitor.MonitorService;
import com.gewara.untrans.monitor.MonitorService.EXCEPTION_TAG;
import com.gewara.untrans.sport.SportUntransService;
import com.gewara.util.DateUtil;

/**
 * @author Administrator
 *
 */
public class SportOrderJobImpl extends JobService implements SportOrderJob{
	@Autowired@Qualifier("daoService")
	private DaoService daoService;
	public void setDaoService(DaoService daoService) {
		this.daoService = daoService;
	}
	@Autowired@Qualifier("untransService")
	private UntransService untransService;
	@Autowired@Qualifier("apiSportService")
	public ApiSportService apiSportService;
	public void setApiSportService(ApiSportService apiSportService) {
		this.apiSportService = apiSportService;
	}
	
	@Autowired@Qualifier("sportOrderService")
	private SportOrderService sportOrderService;
	public void setSportOrderService(SportOrderService sportOrderService) {
		this.sportOrderService = sportOrderService;
	}
	@Autowired@Qualifier("sportUntransService")
	private SportUntransService sportUntransService;
	@Autowired@Qualifier("monitorService")
	private MonitorService monitorService;
	@Autowired
	private OpenTimeSaleService openTimeSaleService;
	
	@Override
	public void sendOrder() {
		String qry = "from CusOrder c where c.status=? and c.response is null";
		List<CusOrder> orderList = daoService.queryByRowsRange(qry, 0, 10, OrderConstant.STATUS_PAID_SUCCESS);
		for(CusOrder order : orderList){
			SportOrder sorder = daoService.getObject(SportOrder.class, order.getOrderid());
			sportUntransService.updateCuOrder(sorder, null);
		}
	}
	
	/**
	 * 每5分钟跑一次
	 */
	@Override
	public void openTimeSaleToSuccess(){
		Timestamp cur = DateUtil.getCurFullTimestamp();
		dbLogger.warn("运动竞拍场次开始:" + DateUtil.formatTimestamp(cur));
		try{
			//查询当前时间到十分钟前区间段的数据
			String qry = "from OpenTimeSale o where o.lockStatus=? and o.memberid is not null and o.closetime>=? and o.closetime<? order by closetime";
			List<OpenTimeSale> otsList =  daoService.queryByRowsRange(qry, 0, 20, OpenTimeTableConstant.SALE_STATUS_LOCK, DateUtil.addMinute(cur, -10),cur);
			for (OpenTimeSale ots : otsList) {
				try {
					ots.setLockStatus(OpenTimeTableConstant.SALE_STATUS_SUCCESS);
					ErrorCode<SportOrder> code = sportOrderService.addSportOrder(ots);
					if(code.isSuccess()){
						if(StringUtils.isBlank(ots.getMessage())){
							SportOrder order = code.getRetval();
							SMSRecord sms = openTimeSaleService.sendMessage(order, ots);
							if(sms!=null){
								untransService.sendMsgAtServer(sms, false);
							}
						}
					}else{
						dbLogger.warn("openTimeSaleToSuccess:" + code.getErrcode() +"," + code.getMsg());
					}
				} catch (OrderException e) {
					dbLogger.warn("ots:" + ots.getId(), e);
				}
			}
			
		}catch (Exception e) {
			monitorService.logException(EXCEPTION_TAG.JOB, "openTimeSaleToSuccess", "SportOrderJobImpl", e, null);
			dbLogger.warn("openTimeSale", e);
		}
		dbLogger.warn("运动竞拍场次结束:" + DateUtil.getCurFullTimestampStr());
	}
}
