package com.gewara.service;

import java.sql.Timestamp;
import java.util.List;
import java.util.Map;

import com.gewara.model.drama.DramaOrder;
import com.gewara.model.drama.OpenDramaItem;
import com.gewara.model.pay.GewaOrder;
import com.gewara.model.pay.GoodsOrder;
import com.gewara.model.pay.GymOrder;
import com.gewara.model.pay.OrderNote;
import com.gewara.model.pay.PubSaleOrder;
import com.gewara.model.pay.SMSRecord;
import com.gewara.model.pay.SportOrder;
import com.gewara.model.pay.TicketOrder;
import com.gewara.model.ticket.OpenPlayItem;
import com.gewara.model.ticket.SellSeat;
import com.gewara.support.ErrorCode;
import com.gewara.untrans.monitor.ConfigTrigger;

public interface MessageService extends ConfigTrigger{
	/**
	 * 将需要发送的信息记录下来，如果有需要立即发送的，则返回
	 * @param order
	 * @return
	 */
	ErrorCode<List<SMSRecord>> addMessage(GewaOrder order);
	void addCommentMsg(TicketOrder order, OpenPlayItem opi);
	void addDramaMsg(DramaOrder order, OpenDramaItem odi);
	void addSportMsg(SportOrder order);
	String getCheckpassTemplate(OpenPlayItem opi);
	ErrorCode<String> getCheckpassMsg(String msgTemplate, TicketOrder order, List<SellSeat> seatList, OpenPlayItem opi);
	SMSRecord getSMSRecordByUkey(String tradeNo, String contact, String smstype);
	SMSRecord addManualMsg(Long relatedid, String mobile, String msg, String ukey);
	
	SMSRecord addOrderNoteSms(DramaOrder order, OrderNote orderNote, Timestamp cur);
	SMSRecord addOrderNoteSms(GoodsOrder order, OrderNote orderNote, Timestamp cur);
	SMSRecord addTrainingOrderSms(GoodsOrder order, OrderNote orderNote, Timestamp cur);
	/**
	 * 查找一次都没有发送的记录
	 * @param maxnum
	 * @return
	 */
	List<SMSRecord> getUnSendMessageList(int maxnum);
	List<SMSRecord> getFailureSMSList();
	SMSRecord getSMSRecordByOttidAndMobile(Long ottid,String mobile);
	/**
	 * 查找短息未加入发送队列的订单
	 * @param ordertype
	 * @return
	 */
	List<GewaOrder> getUnSendOrderList();
	/**
	 * 查找短信未加入发送队列的话剧订单
	 * @return
	 */
	List<DramaOrder> getUnSendDramaOrderList();
	/**
	 * 查找短信未加入发送队列的运动订单
	 * @return
	 */
	List<SportOrder> getUnSendSportOrderList();
	/**
	 * 查找短信未加入发送队列的健身订单
	 * @return
	 */
	List<GymOrder> getUnSendGymOrderList();
	void addUnSendMessage(GewaOrder order);
	String getMobileList(String type, List<Long> cinemaidList, Long movieid, Long relatedid, Timestamp fromtime, Timestamp totime);
	/**
	 * 根据订单号，smstype查询取票密码
	 */
	void removeSMSRecordByTradeNo(String tradeNo, String smstype);
	/**
	 * 竞拍签收商品的短信
	 * @param porder
	 * @param company
	 * @param sno
	 * @return
	 */
	ErrorCode<SMSRecord> addPostPubSaleMessage(PubSaleOrder porder, String company,
			String sno);
	/**
	 * 得到短信发送去掉
	 * @return
	 */
	String getSmsChannel(String mobile, String smstype);
	String getSmsChannel(String mobile, String smstype, Map<String, String> map);
	/**
	 * 增加运动订单消息
	 * @param order
	 * @return
	 */
	ErrorCode<List<SMSRecord>> addSportOrderMessage(GewaOrder order);
	
	/**
	 * 根据时间及关联ID查询短信条数
	 */
	int querySmsRecord(String tradeNo, String tag, Timestamp starttime, Timestamp endtime, Long relatedid, Long memberid);
	String getOrderPassword(TicketOrder ticketOrder, List<SellSeat> seatList);
	ErrorCode<List<SMSRecord>> addMemberCardOrderMsg(GewaOrder order);
	/**
	 * 取消废弃场次定时短息
	 * @param ticketOrder订单列表
	 */
	void updateSMSRecordStatus(List<String> orderList);

}
