package com.gewara.untrans.express;

import com.gewara.model.express.ExpressOrder;
import com.gewara.support.ErrorCode;

public interface YunDaHttpService {
	/**
	 * 通过快递单号查询快递信息
	 * @param ExpressOrder expressOrder	快递单信息
	 * @return
	 */
	ErrorCode<ExpressOrder> qryExpress(ExpressOrder expressOrder);
}
