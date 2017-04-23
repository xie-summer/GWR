package com.gewara.job.impl;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;

import com.gewara.constant.sys.LogTypeConstant;
import com.gewara.job.JobService;
import com.gewara.util.DateUtil;

public class CleanLockJob extends JobService{
	@Autowired@Qualifier("jobJdbcTemplate")
	private JdbcTemplate jobJdbcTemplate;
	public void setJobJdbcTemplate(JdbcTemplate jobJdbcTemplate) {
		this.jobJdbcTemplate = jobJdbcTemplate;
	}
	public void cleanJoblock(){
		String delete = "delete FROM joblock where firetime < ? and status='Y'";
		String date =  DateUtil.format(DateUtil.addDay(new Date(), -3), "yyyyMMddHHmmss");
		int count = jobJdbcTemplate.update(delete, date);
		dbLogger.warnWithType(LogTypeConstant.LOG_TYPE_JOB, "cleanJobLock:" + count);
	}
}
