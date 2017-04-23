/**
 * 
 */
package com.gewara.untrans;

import com.gewara.model.pay.GewaOrder;
import com.gewara.support.ErrorCode;

/**
 * 合作方
 * @author Administrator
 *
 */
public interface CooperateService {
	/**
	 * 特价活动银行卡号支付记录
	 * @param tradeno
	 * @param otherinfo
	 * @param spid
	 * @return
	 */
	boolean addCardnumOperation(String tradeno,String otherinfo,Long spid);
	/**
	 * 通用卡bin验证，银行卡使用次数验证
	 * @param order
	 * @param spid
	 * @param cardNumber
	 * @return
	 */
	ErrorCode<String> checkCommonCardbinOrCardNumLimit(GewaOrder order,Long spid,String cardNumber);
	/**
	 * 上海银行抢购电影票活动
	 * @param preCardno
	 * @return
	 */
	ErrorCode<String> checkShbankCode(Long orderid, String preCardno, String endCardno);

	/**
	 * 上海银行返回验证
	 * @param otherinfo
	 * @return
	 */
	ErrorCode checkShbankBack(Long orderid, String otherinfo, String isSave);
	
	/**
	 * 兴业银行抢购电影票活动
	 * @param orderid
	 * @param preCardno
	 * @param endCardno
	 * @return
	 */
	ErrorCode<String> checkXybankCode(Long orderid, String preCardno, String endCardno);
	/**
	 * 华夏银行返回验证
	 * @param orderid
	 * @param preCardno
	 * @param endCardno
	 * @return
	 */
	ErrorCode<String> checkHxbankCode(Long orderid, String preCardno, String endCardno);
	/**
	 * 上海银行返回验证
	 * @param otherinfo
	 * @return
	 */
	ErrorCode checkXybankBack(Long orderid, String otherinfo, String isSave);
	/**
	 * 银联2.0快捷接口支付卡bin校验
	 * @param orderid
	 * @param cardNumber
	 * @return
	 */
	ErrorCode checkUnionPayFastCode(GewaOrder order,String payBank,String cardNumber,Long spid);
	
	/**
	 * 
	 * 及苏洲中国银行，银联2.0快捷接口支付卡bin校验，且一张卡一个星期只能使用一次
	 * 
	 * @param order
	 * @param paybank
	 * @param cardNumber
	 * @param spid
	 * @return
	 *
	 * @author leo.li
	 * Modify Time May 7, 2013 5:23:37 PM
	 */
	public ErrorCode<String> checkUnionPayFastCodeForSZ(GewaOrder order,String paybank,String cardNumber,Long spid);
	
	public ErrorCode<String> checkUnionPayFastCodeForNyyh(GewaOrder order,String paybank, String cardNumber, Long spid);
	
	/**
	 * 重庆农商行活动，卡bin验证， 每卡每周只能参加一次活动
	 * 
	 * @param order
	 * @param paybank
	 * @param cardNumber
	 * @param spid
	 * @return
	 *
	 * @author leo.li
	 * Modify Time May 28, 2013 5:58:27 PM
	 */
	public ErrorCode<String> checkUnionPayFastCodeForCqnsyh(GewaOrder order,String paybank, String cardNumber, Long spid);
	
	/**
	 * 银联卡友节活动 ,支持银联所有的卡，限制条件为62卡，每卡只能参加一次活动
	 * 
	 * @param order
	 * @param paybank
	 * @param cardNumber
	 * @param spid
	 * @return
	 *
	 * @author leo.li
	 * Modify Time May 28, 2013 5:45:59 PM
	 */
	public ErrorCode<String> checkUnionPayFastCodeForYouJie(GewaOrder order,String paybank, String cardNumber, Long spid);
	
	/**
	 * 温州银行信用卡开出来走上海的商户号，活动用卡的限制为每卡每周限使用一次。
	 * 
	 * @param paybank
	 * @param cardNumber
	 * @param spid
	 * @return
	 *
	 * @author leo.li
	 * Modify Time Jun 9, 2013 11:55:12 AM
	 */
	public ErrorCode<String> checkUnionPayFastCodeForWzcb(GewaOrder order,String paybank,String cardNumber, Long spid);
	
	/**
	 * 渣打银行，验证卡bin
	 * 
	 * @param order
	 * @param paybank
	 * @param cardNumber
	 * @param spid
	 * @return
	 *
	 * @author leo.li
	 * Modify Time Jul 2, 2013 5:36:38 PM
	 */
	public ErrorCode<String> checkUnionPayFastCodeForZdcb(GewaOrder order,String paybank,String cardNumber,Long spid);
	
	/**
	 * 江苏银联2.0快捷接口支付卡bin校验，包括使用次数
	 * 
	 * @param order
	 * @param cardNumber
	 * @return
	 *
	 * @author leo.li
	 * Modify Time Mar 19, 2013 7:40:07 PM
	 */
	public ErrorCode<String> checkUnionPayFastAJS(GewaOrder order,String cardNumber,Long spid);
	
	/**
	 * 北京银联2.0活动卡bin校验，包括使用次数
	 * @param order
	 * @param cardNumber
	 * @param spid
	 * @return
	 */
	public ErrorCode<String> checkUnionPayFastBJ(GewaOrder order,String cardNumber,Long spid);

	ErrorCode<String> checkUnionPayFastShenZhenCodeForPingAn(GewaOrder order, String paybank, String cardNumber, Long spid);

	ErrorCode<String> checkUnionPayFastGuangzhouCodeForBocByWeekone(GewaOrder order, String paybank, String cardNumber, Long spid);
	
	ErrorCode<String> checkUnionPayFastGuangzhouCodeForBocByMonthTwo(GewaOrder order, String paybank, String cardNumber, Long spid);
	
	

	

	
}
