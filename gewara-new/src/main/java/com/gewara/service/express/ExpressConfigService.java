package com.gewara.service.express;

import java.util.List;

import com.gewara.model.express.ExpressConfig;
import com.gewara.model.express.ExpressProvince;
import com.gewara.service.BaseService;

public interface ExpressConfigService extends BaseService {

	List<ExpressConfig> getExpressConfigList(int from, int maxnum);
	
	ExpressProvince getExpress(String expressid, String provicecode);
	
	List<ExpressProvince> getExpressList(String expressid, String... provincecode);
	
	List<ExpressProvince> getExpressList(String expressid, List<String> provincecodeList);

}
