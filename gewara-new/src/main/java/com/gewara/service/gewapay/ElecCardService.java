package com.gewara.service.gewapay;

import java.sql.Timestamp;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.gewara.helper.order.ElecCardContainer;
import com.gewara.model.acl.User;
import com.gewara.model.drama.DramaOrder;
import com.gewara.model.drama.OpenDramaItem;
import com.gewara.model.draw.Prize;
import com.gewara.model.draw.WinnerInfo;
import com.gewara.model.goods.BaseGoods;
import com.gewara.model.pay.Discount;
import com.gewara.model.pay.ElecCard;
import com.gewara.model.pay.ElecCardBatch;
import com.gewara.model.pay.ElecCardExtra;
import com.gewara.model.pay.GoodsOrder;
import com.gewara.model.pay.GymOrder;
import com.gewara.model.pay.MemberAccount;
import com.gewara.model.pay.SportOrder;
import com.gewara.model.pay.TicketOrder;
import com.gewara.model.sport.OpenTimeTable;
import com.gewara.model.ticket.OpenPlayItem;
import com.gewara.model.user.Member;
import com.gewara.support.ErrorCode;
import com.gewara.xmlbind.gym.CardItem;

public interface ElecCardService{
	/**
	 * 获取顶层批次
	 * @param status 
	 * @param applytype 
	 * @param applydep 
	 * @return
	 */
	List<ElecCardExtra> getTopCardExtraList(String status, String applydept, String applytype);
	List<ElecCardExtra> getSubCardExtraList(String status, String applydept, String applytype, Long adduserid, Timestamp addfrom, Timestamp addto);
	/**
	 * 生成电子卡
	 * @param bid
	 * @param num
	 * @return
	 */
	ErrorCode<ElecCardExtra> genElecCard(Long bid, int num, Long userid);
	/**
	 * 预卖卡，生成子批号
	 * @param bid
	 * @return
	 */
	ErrorCode<ElecCardBatch> preSellBatch(Long bid, Long userid);
	/**
	 * 获取这批卡号
	 * @param bid
	 * @return
	 */
	List<ElecCard> getElecCardByBatchId(Long bid, String status, boolean mobile, int from, int maxrows);
	/**
	 * 废弃某张卡：只有卖出者才能废弃自己卖出的卡
	 * @param cardId
	 * @param userId
	 * @return
	 */
	ErrorCode discardElecCard(Long cardId, Long userid);
	/**
	 * 冻结某张卡：只有卖出者才能冻结自己卖出的卡
	 * @param cardId 号卡ID
	 * @param userid 管理人员ID
	 * @return 成功，返回 "success",反之返回错误信息。 
	 */
	ErrorCode lockElecCard(Long cardId, Long userid);
	/**
	 * 批量解冻卡号
	 * @param batchid
	 * @param cardFrom
	 * @param cardTo
	 * @param userid
	 * @return
	 */
	ErrorCode batchLockElecCard(Long batchid, String cardFrom, String cardTo, Long userid);
	/**
	 * 批量冻结卡号,根据用户ID或批次号
	 * @param batchid 批次号
	 * @param memberid 用户ID
	 * @param userid 用户ID
	 * @return 成功，返回 "success",反之返回错误信息。 
	 */
	ErrorCode<String> batchLockElecCard(Long batchid, Long memberid, Long userid);
	/**
	 * 解冻某张卡：只有卖出者才能解冻自己冻结的卡
	 * @param cardId 号卡ID
	 * @param userid 管理人员ID
	 * @return 成功，返回 "success",反之返回错误信息。 
	 */
	ErrorCode unlockcardElecCard(Long cardId, Long userid);
	/**
	 * 
	 * @param batchid 
	 * @param cardFrom
	 * @param cardTo
	 * @param userid
	 * @return
	 */
	ErrorCode batchUnLockElecCard(Long batchid, String cardFrom, String cardTo, Long userid);
	/**
	 * 批量解冻卡号,根据用户ID或批次号
	 * @param batchid 批次号
	 * @param memberid 用户ID
	 * @param userid 用户ID
	 * @return 成功，返回 "success",反之返回错误信息。 
	 */
	ErrorCode<String> batchUnLockElecCard(Long batchid, Long memberid, Long userid);
	/**
	 * 将卡退还到父批次
	 * @param cardId
	 * @return
	 */
	ErrorCode<String> returnElecCard(Long cardId, Long userid);
	ErrorCode<String> returnElecCard(Long batchid, String cardFrom, String cardTo, Long userid);
	/**
	 * 将售出改为待售状态
	 * @param cardId
	 * @param userid
	 * @return
	 */
	ErrorCode<String> unsellElecCard(Long cardId, Long userid);
	ErrorCode<String> unsellElecCard(Long batchid, String cardFrom, String cardTo, Long userid);
	/**
	 * 从上级卡中加入一批卡过来
	 * @param bid
	 * @param cardFrom
	 * @param cardTo
	 * @return
	 */
	ErrorCode<String> addCardFromParent(Long bid, String cardFrom, String cardTo, Long userid);
	/**
	 * 获取卡号
	 * @param bid
	 * @param cardFrom
	 * @param cardTo
	 * @param status
	 * @return
	 */
	List<ElecCard> getCardList(Long bid, String cardFrom, String cardTo, String status);
	/**
	 * 售出某批卡
	 * @param batchId
	 * @param userId
	 * @return
	 */
	ErrorCode<String> soldElecBatch(Long batchId, Long userid);
	/**
	 * 关联用户与卡
	 * @param member
	 * @param cardpass
	 * @return
	 */
	ErrorCode<String> registerCard(Member member, String cardpass, String ip);
	ErrorCode<ElecCard> chargeByCard(Member member, MemberAccount account, String cardpass, String ip);
	/**
	 * 根据卡号或密码找卡
	 * @param cardno
	 * @return
	 */
	ElecCard getElecCardByPass(String cardpass);
	ElecCard getMemberElecCardByNo(Long memberid, String cardno);
	ElecCard getHistElecCardByPass(String cardPass);
	ElecCard getHistElecCardByNo(String cardno);
	/**
	 * 获取批次数量
	 * @param bid
	 * @return
	 */
	int getElecCardCountByBatchId(Long bid, String status);
	/**
	 * 分配卡号给手机
	 * @param bid 批次
	 * @param mobiles 手机号，逗号分隔
	 * @param num 每个手机分配数量
	 * @param allowDup 是否允许重号
	 * @return
	 */
	ErrorCode<String> assignMobile(Long batchid, List<String> mobileList, Integer num, String cardFrom, String cardTo);
	/**
	 * 取消卡号分配
	 * @param batchid
	 * @param cardFrom
	 * @param cardTo
	 * @return
	 */
	ErrorCode<String> unassignMobile(Long batchid, String cardFrom, String cardTo);
	/**
	 * 取消绑定到账户
	 * @param bid
	 * @param cardFrom
	 * @param cardTo
	 * @return
	 */
	ErrorCode<String> unbindMember(Long bid, String flag, String cardFrom, String cardTo);
	/**
	 * 绑定到账户
	 * @param bid
	 * @param mobileList
	 * @param num
	 * @param cardFrom
	 * @param cardTo
	 * @return
	 */
	ErrorCode<String> bind2Member(Long bid, String flag, List<Long> memberidList, Integer num, String cardFrom, String cardTo);
	/**
	 * 此订单可用的卡
	 * @param order
	 * @param opi
	 * @param memberid
	 * @return
	 */
	ElecCardContainer getAvailableCardList(TicketOrder order, List<Discount> discountList, OpenPlayItem opi, Long memberid);
	ElecCardContainer getAvailableCardList(DramaOrder order, List<Discount> discountList, OpenDramaItem item, Long memberid);
	ElecCardContainer getAvailableCardList(SportOrder order, List<Discount> discountList, OpenTimeTable table, Long memberid);
	ElecCardContainer getAvailableCardList(GoodsOrder order, List<Discount> discountList, BaseGoods goods, Long memberid);
	ElecCardContainer getAvailableCardList(GymOrder order, List<Discount> discountList, CardItem item, Long memberid);
	/**
	 *  查询当前用户的票券
	 */
	List<ElecCard> getCardListByMemberid(Long memberid, String tag, int from, int maxnum);
	Integer getCardCountByMemberid(Long memberid, String tag);

	List<ElecCardBatch> getSubBatchListByMerchantid(Long merchantid);
	ErrorCode addCardFromParent(ElecCardBatch batch, int addnum, Long userid);
	/**
	 * 增加新批次
	 * @param batch
	 * @return
	 */
	void addCardBatch(ElecCardBatch batch, Long userid);
	/**
	 * 更新卡批次信息
	 * @param bid
	 * @return
	 */
	void updateBatchExtra(ElecCardExtra extra);
	List<ElecCardExtra> getSubCardExtraListByStatus(String status);
	List<ElecCardExtra> getSubCardExtraListByIssuerId(Long issuserid);
	List<ElecCardExtra> getAllSubCardExtraList();
	/**
	 * 冻结批次
	 * @param bid
	 * @return
	 */
	ErrorCode frozenBatch(Long bid);
	
	
	/**
	 * 根据影院id得到适用于这个影院的电子券统计信息
	 * @param cinemaId
	 * @return
	 */
	Map<String,Object> getTopCardExtraListByValidcinema(Long cinemaId);
	
	/**
	 * 检查动态码并激活票券
	 * @param member
	 * @param cardPass 
	 * @param checkpass
	 * @param checkcount
	 * @return
	 */
	ErrorCode<ElecCard> checkActivationCard(Member member, String cardPass, String checkpass);
	/**
	 * 获取券密码
	 * @param elecCard
	 * @return
	 */
	String getElecCardpass(ElecCard elecCard);
	/**
	 * 获取添加批次的用户列表
	 * @return
	 */
	List<User> getAddBatchUserList();
	/**
	 * 此批次的券中是否有被使用过的
	 * @param batchId
	 * @return
	 */
	boolean hasUsed(ElecCardBatch batch);
	List<String> batchDiscard(Long userId, String[] cardPassOrNoList, boolean byCardno);
	/**
	 * 只是后台用来查询
	 * @param cardno
	 * @return
	 */
	ElecCard queryCardByNo(String cardno);
	
	ErrorCode batchAddAmountWithElecCard(Set<Long> mems, Set<String> cards, Long operatorId, String ip);
	ErrorCode randomSendPrize(Prize prize, WinnerInfo winner);
	int upgradeElecCard();
}
