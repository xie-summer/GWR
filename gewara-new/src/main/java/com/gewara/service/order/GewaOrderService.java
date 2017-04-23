package com.gewara.service.order;

import java.sql.Timestamp;
import java.util.Date;

import com.gewara.model.express.ExpressConfig;
import com.gewara.model.express.ExpressProvince;
import com.gewara.model.pay.GewaOrder;
import com.gewara.model.pay.OrderAddress;
import com.gewara.model.pay.OrderExtra;
import com.gewara.model.pay.SpecialDiscount;
import com.gewara.model.user.MemberUsefulAddress;
import com.gewara.service.OrderException;
import com.gewara.support.ErrorCode;

public interface GewaOrderService {
	String nextRandomNum(Timestamp validtime, int length, String leftPad);
	/**
	 * 增加订单来源
	 * @param tradeNo
	 * @param origin
	 */
	void addOrderOrigin(GewaOrder order, String origin);
	void addBuyItemList(GewaOrder order, String items);
	
	ErrorCode<GewaOrder> removeDiscount(GewaOrder order, Long discountId);
	SpecialDiscount getSpdiscountBySpflag(String spflag);
	/**
	 * 抵用券付清账目
	 * @param order
	 * @return
	 */
	void zeropayGewaOrder(GewaOrder order) throws OrderException;
	OrderExtra processOrderExtra(GewaOrder order);
	void cancelOrderExtra(GewaOrder order);
	ErrorCode cancelOrderNote(GewaOrder order);
	
	/**
	 * 算快递费更新订单价格信息 otherfee
	 * @param order
	 * @param expressConfig
	 * @param provincecode
	 * @return
	 */
	ErrorCode<Integer> computeExpressFee(GewaOrder order, ExpressConfig expressConfig, String provincecode);
	
	ErrorCode clearExpressFee(GewaOrder order);
	
	/**
	 * 算快递费
	 * @param order
	 * @param expressConfig
	 * @param provincecode
	 * @return
	 */
	ErrorCode<ExpressProvince> getExpressFee(ExpressConfig expressConfig, String provincecode);
	/**
	 * 
	 * @param order
	 * @return
	 */
	ErrorCode<Integer> computeUmpayfee(GewaOrder order);
	Integer getUmpayfee(GewaOrder order);
	
	/**
	 * 更换订单产生多余的费用
	 * @param order
	 * @return
	 */
	ErrorCode<Integer> computeChangeOrderFee(GewaOrder order);
	
	ErrorCode validMemberUserfulAddress(MemberUsefulAddress memberUsefulAddress);
	ErrorCode<OrderAddress> createOrderAddress(GewaOrder order, MemberUsefulAddress memberUsefulAddress, ExpressConfig expressConfig);
	String validOrderOrigin(String originStr);
	
	
	ErrorCode<String> syn360CPSOrderByDay(Date date);
}
