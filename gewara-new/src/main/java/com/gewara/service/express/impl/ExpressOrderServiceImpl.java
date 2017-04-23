package com.gewara.service.express.impl;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.gewara.constant.ticket.OrderExtraConstant;
import com.gewara.model.acl.User;
import com.gewara.model.express.ExpressOrder;
import com.gewara.model.pay.OrderExtra;
import com.gewara.service.express.ExpressOrderService;
import com.gewara.service.impl.BaseServiceImpl;
import com.gewara.support.ErrorCode;
import com.gewara.untrans.monitor.MonitorService;

@Service("expressOrderService")
public class ExpressOrderServiceImpl extends BaseServiceImpl implements ExpressOrderService {

	@Autowired@Qualifier("monitorService")
	protected MonitorService monitorService;
	
	private String getExpressOrderId(String expresstype, String expressnote){
		return expresstype + "_" + expressnote;
	}
	
	@Override
	public ErrorCode saveExpressOrder(String expressnote, String expresstype, List<String> tradeNoList, User user){
		if(StringUtils.isBlank(expressnote)) return ErrorCode.getFailure("快递单号不能为空！");
		if(StringUtils.isBlank(expresstype)) return ErrorCode.getFailure("快递类型不能为空！");
		if(CollectionUtils.isEmpty(tradeNoList)) return ErrorCode.getFailure("订单号不能为空！");
		if(!OrderExtraConstant.EXPRESS_TYPE_LIST.contains(expresstype)){
			return ErrorCode.getFailure("快递类型错误！");
		}
		List<OrderExtra> extraList = new ArrayList<OrderExtra>();
		for (String tradeno : tradeNoList) {
			String tmp = StringUtils.trim(tradeno);
			OrderExtra extra = baseDao.getObjectByUkey(OrderExtra.class, "tradeno", tmp);
			if(extra == null) return ErrorCode.getFailure("请确认订单号："+ tmp +"，是否成功订单或存在！");
			if(StringUtils.isNotBlank(extra.getExpressnote()) && StringUtils.isNotBlank(extra.getExpresstype())){
				return ErrorCode.getFailure("订单号：" + tmp + "已使用快递商："+ OrderExtraConstant.getExpressTypeText(extra.getExpresstype())+ "，快递单号：" + extra.getExpressnote());
			}
			dbLogger.warn("userid:" + user.getId() + "order_extra:" + extra.getTradeno() +", expressnote:" + expressnote + ", expresstype:" + expresstype);
			extra.setExpressnote(expressnote);
			extra.setExpresstype(expresstype);
			extraList.add(extra);
		}
		String express = getExpressOrderId(expresstype, expressnote);
		ExpressOrder expressOrder = baseDao.getObject(ExpressOrder.class, express);
		if(expressOrder == null){
			expressOrder = new ExpressOrder(expressnote, expresstype);
			baseDao.saveObject(expressOrder);
			monitorService.saveAddLog(user.getId(), ExpressOrder.class, expressOrder.getId(), expressOrder);
		}
		baseDao.saveObjectList(extraList);
		return ErrorCode.SUCCESS;
	}
}
