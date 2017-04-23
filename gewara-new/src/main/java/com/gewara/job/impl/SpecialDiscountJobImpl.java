package com.gewara.job.impl;

import java.sql.Timestamp;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.orm.hibernate3.HibernateTemplate;

import com.gewara.constant.sys.LogTypeConstant;
import com.gewara.job.JobService;
import com.gewara.model.pay.Spcounter;
import com.gewara.service.DaoService;
import com.gewara.service.gewapay.PaymentService;
import com.gewara.support.ErrorCode;
import com.gewara.untrans.monitor.MonitorService;
import com.gewara.untrans.monitor.MonitorService.EXCEPTION_TAG;
import com.gewara.util.DateUtil;
import com.gewara.util.StringUtil;

public class SpecialDiscountJobImpl extends JobService {
	@Autowired@Qualifier("hibernateTemplate")
	protected HibernateTemplate hibernateTemplate;
	public void setHibernateTemplate(HibernateTemplate hibernateTemplate) {
		this.hibernateTemplate = hibernateTemplate;
	}
	
	@Autowired@Qualifier("daoService")
	protected DaoService daoService;
	public void setDaoService(DaoService daoService) {
		this.daoService = daoService;
	}

	@Autowired@Qualifier("monitorService")
	private MonitorService monitorService;

	@Autowired@Qualifier("paymentService")
	protected PaymentService paymentService;
	public void setPaymentService(PaymentService paymentService) {
		this.paymentService = paymentService;
	}
	
	public void restoreSpcounter() {
		Timestamp cur = DateUtil.getCurFullTimestamp();
		List<Long> spcounterIdList = hibernateTemplate.find("select distinct spcounterid from SdRecord where validtime < ?", cur);
		for(Long spcounterId: spcounterIdList){
			try{
				ErrorCode result = paymentService.restoreSdCounterBySpcounter(spcounterId);
				dbLogger.warn(result.getMsg());
			}catch(Exception e){
				monitorService.logException(EXCEPTION_TAG.JOB, "restoreSpcounter", "恢复名额错误,spcounterId:" + spcounterId, e, null);
			}
		}
	}
	
	public void resetSpcounter() {
		dbLogger.warnWithType(LogTypeConstant.LOG_TYPE_JOB, "更新SpecialDiscount的spcouter开始start...");
		try{
			Timestamp cur = DateUtil.getCurFullTimestamp();
			List<Long> spcounterIdList = hibernateTemplate.find("select distinct spcounterid from SpecialDiscount where timeto >= ? and spcounterid is not null", cur);
			List<Spcounter> spcounterList = daoService.getObjectList(Spcounter.class, spcounterIdList);
			for (Spcounter spcounter : spcounterList) {
				paymentService.resetSpcounter(spcounter, 1L);
			}
			dbLogger.warnWithType(LogTypeConstant.LOG_TYPE_JOB, "更新SpecialDiscount的spcouter,共 "+spcounterList.size()+" 条数据，结束end...");
		}catch (Exception e) {
			monitorService.logException(EXCEPTION_TAG.JOB, "resetSpcounter", "SpecialDiscountJobImpl", e, null);
			dbLogger.warn(StringUtil.getExceptionTrace(e, 10));
		}
	}


}
