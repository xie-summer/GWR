package com.gewara.service.sport;

import java.sql.Timestamp;
import java.util.List;

import com.gewara.model.sport.Guarantee;
import com.gewara.service.BaseService;



public interface GuaranteeService extends BaseService {


	Integer getGuaranteeCount(String citycode, Timestamp starttime, Timestamp endtime, String status);
	List<Guarantee> getGuaranteeList(String citycode, Timestamp starttime, Timestamp endtime, String status, String orderField, boolean asc, int from, int maxnum);

}
