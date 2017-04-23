package com.gewara.service.bbs;

import java.sql.Timestamp;
import java.util.List;

import com.gewara.model.pay.TicketOrder;
import com.gewara.model.ticket.OpenPlayItem;
import com.gewara.model.user.SysMessageAction;
import com.gewara.model.user.UserMessageAction;

public interface UserMessageService {
	Integer getReceiveUserMessageCountByMemberid(Long memberid, Integer isread);
	List<UserMessageAction> getReceiveUserMessageListByMemberid(Long memberid, Integer isread, int from, int maxnum);
	Integer getSendUserMessageCountByMemberid(Long memberid);
	List<UserMessageAction> getSendUserMessageListByMemberid(Long memberid, int from, int maxnum);
	Integer getSysMsgCountByMemberid(Long memberid, String status);
	List<SysMessageAction> getSysMsgListByMemberid(Long memberid, String status, int from, int maxnum);
	UserMessageAction getUserMessageActionByUserMessageid(Long mid);
	List<UserMessageAction> getUserMessageListByGroupid(Long groupid);
	Integer getUMACountByMemberid(Long memberid);
	List<UserMessageAction> getUMAListByMemberid(Long memberid, Integer first, int maxnum);
	boolean isSendMsg(Long memberid);
	void initSysMsgList(List<SysMessageAction> sysMsgList);
	/**
	 * 用户与用户之间的私信
	 * @param frommemberid
	 * @param tomemberid
	 * @param from
	 * @param maxnum
	 * @return
	 */
	List<UserMessageAction> getUserMessageActionByFromidToid(Long frommemberid, Long tomemberid, Timestamp addtime, int from, int maxnum);
	/**
	 * 我的全部私信
	 * @param memberid
	 * @param from
	 * @param maxnum
	 * @return
	 */
	List<UserMessageAction> getUserMessageActionByMemberid(Long memberid, int from, int maxnum);
	
	/**
	 *  根据用户Id, 状态 分页查询
	 * @param memberId
	 * @param status
	 * @param first
	 * @param max
	 * @return List<UserMessageAction>
	 */
	Integer countMessagesByMemIdAndStatus(Long memberId, String status);
	List<UserMessageAction> getMessagesByMemIdAndStatus(Long memberId, String status, int isread, int from, int maxnum);
	/**
	 *  公告列表(单条记录)
	 */
	UserMessageAction getPublicNotice();
	
	/**
	 * 是否已经发送申请、邀请
	 * @param commuid
	 * @param memberid
	 * @return
	 */
	boolean isExistSysMessageAction(Long commuid, Long memberid, String action, boolean flag);
	/**
	 * 根据commuid,memberid,action,status查询是存在数据
	 * @param commuid
	 * @param memberid
	 * @param action
	 * @param flag
	 * @return
	 */
	SysMessageAction getSysMessageAction(Long commuid, Long memberid, String action, boolean flag);
	Integer getCountMessageByMessageActionId(Long id);
	
	/**
	 * 查询收件箱未读信息数量
	 */
	Integer getNotReadMessage(Long memberid,Integer isRead);
	
	/**
	 * 查询系统信息未读数量
	 */
	Integer getNotReadSysMessage(Long memberid,Long isRead);
	List<SysMessageAction> getNotReadSysMessageList(Long memberid,Long isRead);
	/**
	 * 根据actionid,action,status查询系统消息列表
	 * @param actionid
	 * @param action
	 * @param stauts
	 * @return
	 */
	List<SysMessageAction> getSysMessageActionListByActionidAndActionAndStatus(Long actionid, String action, String stauts, int from, int maxnum);
	
	/**
	 *  20101109 添加发送系统消息模板
	 */
	void sendSiteMSG(Long tomemberid, String action, Long actionid, String body);
	
	void addMsgAction(TicketOrder order, OpenPlayItem opi);
}
