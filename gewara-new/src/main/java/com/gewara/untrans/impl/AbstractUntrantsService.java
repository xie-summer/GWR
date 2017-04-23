package com.gewara.untrans.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.orm.hibernate3.HibernateOptimisticLockingFailureException;

import com.gewara.Config;
import com.gewara.service.DaoService;
import com.gewara.util.GewaLogger;
import com.gewara.util.LoggerUtils;

public class AbstractUntrantsService {
	
	@Autowired@Qualifier("daoService")
	protected DaoService daoService;
	
	protected final transient GewaLogger dbLogger = LoggerUtils.getLogger(getClass(), Config.getServerIp(), Config.SYSTEMID);
	protected boolean isUpdateErrorException(Throwable e){
		if(e instanceof HibernateOptimisticLockingFailureException){
			HibernateOptimisticLockingFailureException exc = (HibernateOptimisticLockingFailureException)e;
			dbLogger.warn(exc.getPersistentClassName() + ":" + exc.getIdentifier());
			return true;
		}
		return false;
	}
}
