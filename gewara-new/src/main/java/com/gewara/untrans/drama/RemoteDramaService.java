package com.gewara.untrans.drama;

import com.gewara.api.gpticket.vo.ticket.DramaRemoteOrderVo;
import com.gewara.support.ErrorCode;

public interface RemoteDramaService {
	
	ErrorCode<String> getRemoteLockSeat(String areaseqno);
	
	ErrorCode<String> getRemoteLockPrice(String areaseqno);

	ErrorCode<DramaRemoteOrderVo> backOrder(Long orderid, String description);

	/**
	 *	创建远程订单
	 * @param seqno
	 * @param orderid
	 * @param mobile
	 * @param areaseqno
	 * @param opentype
	 * @param seatLabel
	 * @return
	 */
	ErrorCode<DramaRemoteOrderVo> newCreateOrder(String seqno, Long orderid, String mobile, String areaseqno, String opentype, String seatLabel);
	
	/**
	 * 锁定第三方区域座位
	 * @param seqno
	 * @param orderid
	 * @param mobile
	 * @param areaseqno
	 * @param seatLabel
	 * @return
	 */
	ErrorCode<DramaRemoteOrderVo> newLockSeat(String seqno, Long orderid, String mobile, String areaseqno, String seatLabel);
	
	/**
	 * 锁定第三方价格数量
	 * @param seqno
	 * @param orderid
	 * @param mobile
	 * @param areaseqno
	 * @param seatLabel
	 * @return
	 */
	ErrorCode<DramaRemoteOrderVo> newLockPrice(String seqno, Long orderid, String mobile, String areaseqno, String seatLabel);
	
	/**
	 * 确认订单，成功出票
	 * @param seqno
	 * @param orderid
	 * @param mobile
	 * @param areaseqno
	 * @param opentype
	 * @param seatLabel
	 * @return
	 */
	ErrorCode<DramaRemoteOrderVo> newFixOrder(String seqno, Long orderid, String mobile, String areaseqno, String opentype, String seatLabel, String greetings);
	
	/**
	 * 取消订单
	 * @param orderid
	 * @return
	 */
	ErrorCode newUnRemoteOrder(Long orderid);
	
	ErrorCode<DramaRemoteOrderVo> qryOrder(Long orderid, boolean forceRefresh);
	
	ErrorCode<DramaRemoteOrderVo> checkOrder(Long orderid, boolean forceRefresh);

	ErrorCode<String> qryTicketPrice(String seqno);

	ErrorCode<String> qryOrderPrintInfo(Long orderid);
}
