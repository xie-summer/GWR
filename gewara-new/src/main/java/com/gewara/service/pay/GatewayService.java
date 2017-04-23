package com.gewara.service.pay;

import java.util.List;

import com.gewara.api.pay.domain.Gateway;
import com.gewara.model.pay.PayMerchant;
import com.gewara.support.ErrorCode;

public interface GatewayService {

	/**
	 * 同步所有支付网关、商户号、银行等信息
	 * 
	 * @param response
	 *
	 * @author leo.li
	 * Modify Time Oct 17, 2013 4:22:29 PM
	 */
	public void synAllGateway(List<Gateway> gatewayList);

	/**
	 * 判断该网关是否已切换到新的接口
	 * 
	 * @param gatewayCode 支付网关代码
	 * @return
	 *
	 * @author leo.li
	 * Modify Time Oct 18, 2013 4:43:57 PM
	 */
	public boolean isSwitch(String gatewayCode);
	

	/**
	 * 查找商户信息，只查根据城市路由、或查找默认
	 * 
	 * @param cityCode
	 * @param gatewayCode
	 * @return
	 *
	 * @author leo.li
	 * Modify Time Dec 3, 2013 9:58:51 PM
	 */
	public ErrorCode<PayMerchant> findMerchant(String cityCode, String gatewayCode);
	
	
	/**
	 * 查找商户信息，可以根据城市路由、指定商户标识、默认查找
	 * 
	 * @param cityCode      城市
	 * @param gatewayCode   网关
	 * @param merchantCode 	商户标识，指定商户标识时用到，如邮储、农行；
	 * @return
	 *
	 * @author leo.li
	 * Modify Time Oct 18, 2013 6:24:52 PM
	 */
	public ErrorCode<PayMerchant> findMerchant(String cityCode,String gatewayCode,String merchantCode);

	/**
	 * 
	 * 获取商户标识
	 * 商户先按id，从小到大排序
	 * 如果设了默认，取第一个设了默认的商户，返回
	 * 如果没有设默认，直接取第一个，返回
	 * 
	 * @param gatewayCode
	 * @return
	 *
	 * @author leo.li
	 * Modify Time Oct 29, 2013 2:11:52 PM
	 */
	public ErrorCode<PayMerchant> findDefaultMerchant(String gatewayCode);
}
