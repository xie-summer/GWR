/** 
 */
package com.gewara.service.gewapay;

import com.gewara.model.pay.RepeatingPayorder;

/**
 * @author Zhicheng.Peng   Johnny.Resurgam@Gmail.com
 *  
 *  Jun 28, 2013  2:41:24 PM
 */
public interface RepeatingPayOrderService {
	RepeatingPayorder getRepeatingOrder(String payseqNo, String tradeno);
}
