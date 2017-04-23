package com.gewara.service.express;

import java.util.List;

import com.gewara.model.acl.User;
import com.gewara.service.BaseService;
import com.gewara.support.ErrorCode;

public interface ExpressOrderService extends BaseService {

	/**
	 * 新增快递单号信息
	 * @param expressnote		快递单号
	 * @param expresstype		快递类型
	 * @param tradeNoList		订单集合
	 * @param user				用户信息
	 * @return
	 */
	ErrorCode saveExpressOrder(String expressnote, String expresstype, List<String> tradeNoList, User user);

}
