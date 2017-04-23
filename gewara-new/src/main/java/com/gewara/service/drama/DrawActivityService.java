package com.gewara.service.drama;

import java.sql.Timestamp;
import java.util.List;
import java.util.Map;

import com.gewara.model.common.VersionCtl;
import com.gewara.model.draw.DrawActivity;
import com.gewara.model.draw.Prize;
import com.gewara.model.draw.WinnerInfo;
import com.gewara.model.pay.SMSRecord;
import com.gewara.model.user.Festival;
import com.gewara.model.user.Member;
import com.gewara.model.user.MemberInfo;
import com.gewara.support.ErrorCode;

public interface DrawActivityService {
	/**
	 * 查询抽奖活动
	 */
	List<DrawActivity> getDrawActivityList(String order, int from, int maxnum);
	/**
	 * 查询抽奖活动数量
	 */
	Integer getDrawActivityCount();
	
	/**
	 * 根据抽奖活动id查询，该活动的奖品信息
	 */
	List<Prize> getPrizeListByDid(Long did,String[] ptype);
	List<Prize> getAvailablePrizeList(Long did, int minleft);
	
	/**
	 * 根据不同活动信息，奖品类型，查询获奖信息
	 */
	List<WinnerInfo> getWinnerList(Long activityid,List<Long> prizeid, Timestamp startTime,Timestamp endTime, String tag,Long memberid,String mobile,String nickname,int from,int maxnum);
	Integer getMemberWinnerCount(Long memberid, Long activityid, Timestamp startTime,Timestamp endTime);
	/**
	 * 根据不同活动信息，奖品类型，查询获奖信息数量
	 */
	Integer getWinnerCount(Long activityid, List<Long> prizeid, Timestamp startTime, Timestamp endTime, String tag, Long memberid, String mobile,String nickname);
	
	/**
	 * 根据抽奖活动id，奖品id，查询此奖品已送出多少
	 */
	Integer getSendOutPrizeCount(Long activityid,Long prizeid);
	
	/**
	 * 分组查询领取各种奖品的人数
	 */
	List<Map> getPrizeCountInfo(Long activityid,Timestamp startTime,Timestamp endTime);
	
	/**
	 * 查询参与抽奖活动用户数量
	 */
	Integer getJoinDrawActivityCount(Long activityid);
	Integer getCurDrawActivityNum(DrawActivity da, Long memberid, int totalChance);
	Integer getCurChanceNum(DrawActivity da, Long memberid);
	Integer getUsedChanceNum(DrawActivity da, Long memberid);
	Integer getExtraChanceNum(DrawActivity da, Long memberid);
	Prize runDrawPrize(DrawActivity da, VersionCtl vc, boolean scalper);
	//void updatePrize(Prize prize, VersionCtl vc);
	WinnerInfo addWinner(Prize prize, Long memberId, String nickname, String mobile, String ip);
	SMSRecord sendPrize(Prize prize, WinnerInfo winner, boolean sendsms);
	/**
	 * 根据
	 * @param activityid
	 * @param mobile
	 * @return
	 */
	int getWinnerCountByMobile(Long activityid, String mobile);
	WinnerInfo getWinnerInfoByMemberid(Long activityid, Long memberid);
	List<WinnerInfo> getWinnerInfoByMemberid(Long activityid, Long memberid, int from, int maxnum);
	
	/**
	 * 根据手机号查询这个手机号在当前是否已经领取过兑换券
	 */
	boolean isSendPrize(String mobile,Long memberid,Long activityid,Timestamp gainPrizeTime);
	
	/**
	 * 查询当前用户，在这个活动中邀请的人数
	 */
	Integer getInviteMemberCount(Long memberId, String invitetype, boolean validMobile, Timestamp startTime,Timestamp endTime);
	List<Long> getInviteMemberList(Long memberId, String invitetype, boolean validMobile, Timestamp starttime, Timestamp endtime);
	
	/**
	 * 查询当前用户，在这个活动中邀请的人所下订单数和当前用户所下订单数
	 */
	Integer getInviteOrderNum(DrawActivity da,Long memberid);
	
	//ErrorCode<WinnerInfo> baseClickDraw(DrawActivity da, VersionCtl mvc, boolean scalper, Member member, BuziCallback callback);
	/**
	 * 抽奖基础方法
	 */
	ErrorCode<WinnerInfo> baseClickDraw(DrawActivity da, VersionCtl mvc, boolean scalper, Member member);
	ErrorCode<WinnerInfo> baseClickDraw(DrawActivity da, VersionCtl mvc, boolean scalper, Long memberId, String nickname, String mobile);
	ErrorCode<WinnerInfo> baseClickDraw(DrawActivity da, VersionCtl mvc, boolean scalper, Long memberId, String nickname, String mobile, String ip);
	VersionCtl gainMemberVc(String memberId);
	VersionCtl gainPrizeVc(Long prizeId);
	ErrorCode sendNewTaskCardPrize(MemberInfo info, Member member);
	
	/**
	 * 获取节日红包（类似抽奖）
	 */
	Prize clickPointPrize(Festival festival, Member member);
	/**
	 * Origins合作商户-获取彩球短信
	 * @param memberid
	 * @param mobile
	 * @return
	 */
	ErrorCode<SMSRecord> getColorBall(Long memberid , String mobile , String msgContent);
	/**
	 * Origins合作商户-获取优惠券
	 * @param memberid
	 * @param mobile
	 * @param msgContent
	 * @return
	 */
	ErrorCode<SMSRecord> getCouponCode(Long memberid , String mobile , String msgContent);
}
