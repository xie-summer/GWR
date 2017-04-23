package com.gewara.untrans.express;

import com.gewara.model.express.ExpressOrder;
import com.gewara.support.ErrorCode;

public interface ExpressService {

	/**
	 * 通过快递单号获取物流信息
	 * @param expressOrder		快递单号信息
	 * @return
	 */
	ErrorCode<ExpressOrder> qryExpress(ExpressOrder expressOrder);

}
