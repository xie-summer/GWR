package com.gewara.untrans.express.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.gewara.constant.ApiConstant;
import com.gewara.constant.ticket.OrderExtraConstant;
import com.gewara.model.express.ExpressOrder;
import com.gewara.support.ErrorCode;
import com.gewara.untrans.express.ExpressService;
import com.gewara.untrans.express.YunDaHttpService;

@Service("expressService")
public class ExpressServiceImpl implements ExpressService {
	@Autowired@Qualifier("yunDaHttpService")
	private YunDaHttpService yunDaHttpService;
	
	@Override
	public ErrorCode<ExpressOrder> qryExpress(ExpressOrder expressOrder){
		ErrorCode code = null;
		if(expressOrder.hasExpressType(OrderExtraConstant.EXPRESS_YUNDA)){
			code = yunDaHttpService.qryExpress(expressOrder);
		}
		if(code == null){
			return ErrorCode.getFailure(ApiConstant.CODE_SIGN_ERROR, "快递单类型错误！");
		}
		if(!code.isSuccess()){
			return ErrorCode.getFailure(code.getErrcode(), code.getMsg()); 
		}
		return ErrorCode.getSuccessReturn(expressOrder);
	}
}
