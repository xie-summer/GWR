/** 
 */
package com.gewara.service.gewapay.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.gewara.model.pay.RepeatingPayorder;
import com.gewara.service.DaoService;
import com.gewara.service.gewapay.RepeatingPayOrderService;


/**
 * @author Zhicheng.Peng   Johnny.Resurgam@Gmail.com
 *  
 *  Jun 28, 2013  2:42:45 PM
 */
@Service("repeatingPayOrderService")
public class RepeatingPayOrderServiceImpl implements RepeatingPayOrderService {
	@Autowired
	private DaoService daoService;
	@Override
	public RepeatingPayorder getRepeatingOrder(String payseqNo, String tradeno) {
		return daoService.getObject(RepeatingPayorder.class, "" + tradeno + "," + payseqNo);
	}
}
