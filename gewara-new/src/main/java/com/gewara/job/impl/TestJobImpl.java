package com.gewara.job.impl;


import com.gewara.Config;
import com.gewara.job.JobService;
import com.gewara.util.GewaLogger;
import com.gewara.util.LoggerUtils;

public class TestJobImpl extends JobService{
	public void doTestJob(){
		GewaLogger log = LoggerUtils.getLogger(getClass(), Config.getServerIp(), Config.SYSTEMID);
		log.warn("doTestJob1");
	}
	public void doTestJob2(){
		GewaLogger log = LoggerUtils.getLogger(getClass(), Config.getServerIp(), Config.SYSTEMID);
		log.warn("doTestJob2");
	}
}
