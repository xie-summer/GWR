/** 
 */
package com.gewara.service.gewapay.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.jdom.IllegalDataException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.gewara.constant.PaymethodConstant;
import com.gewara.model.pay.PayMethod;
import com.gewara.service.DaoService;
import com.gewara.service.gewapay.PayMethodService;

/**
 * @author Zhicheng.Peng   Johnny.Resurgam@Gmail.com
 *  
 *  Jul 9, 2013  11:22:26 AM
 */
@Service("payMethodService")
public class PayMethodServiceImpl implements PayMethodService, InitializingBean{
	
	@Autowired
	private DaoService daoService;

	/* (non-Javadoc)
	 * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
	 */
	@Override
	public void afterPropertiesSet() throws Exception {
		Map<String, String> payMap = PaymethodConstant.getPayTextMap();
		List<String> payMethodsSet = new ArrayList<String>(payMap.keySet());
		Map<String, PayMethod> dbPayMethods = daoService.getObjectMap(PayMethod.class, payMethodsSet);
		payMethodsSet.removeAll(dbPayMethods.keySet());
		
		if (!payMethodsSet.isEmpty()){
			throw new IllegalDataException("number of pay methods in pay util is not equals number of pay methods in db.. pls check:" + StringUtils.join(payMethodsSet, ","));
		}
	}
}
