package com.gewara.untrans.activity;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

import com.gewara.model.user.Member;
import com.gewara.support.ErrorCode;
import com.gewara.xmlbind.activity.CategoryCount;
import com.gewara.xmlbind.activity.CountyCount;
import com.gewara.xmlbind.activity.RemoteActivity;
import com.gewara.xmlbind.activity.RemoteActivityMpi;
import com.gewara.xmlbind.activity.RemoteApplyjoin;
import com.gewara.xmlbind.activity.RemoteTreasure;

public interface SynchActivityService {
	/**
	 * 获取活动的信息
	 * @param activityId
	 * @return
	 */
	ErrorCode<RemoteActivity> getRemoteActivity(Serializable activityId);
	/**
	 * 获取用户发表的活动
	 * @param memberid
	 * @param tag
	 * @param from
	 * @param maxnum
	 * @return
	 */
	ErrorCode<List<RemoteActivity>> getRemoteActivityByMemberid(Long memberid, String tag, int from, int maxnum);
	/**
	 * 获取用户收藏的活动
	 * @param memberid
	 * @param tag
	 * @param from
	 * @param maxnum
	 * @return
	 */
	ErrorCode<List<RemoteActivity>> getMemberCollActivityList(Long memberid, String tag, String order, String asc, int from, int maxnum);
	/**
	 * 根据活动id集合获取活动列表
	 * @param idList
	 * @return
	 */
	ErrorCode<List<RemoteActivity>> getRemoteActivityListByIds(List<Long> idList);
	/**
	 * 根据flag查询
	 * @param citycode
	 * @param tag
	 * @param flag
	 * @param from
	 * @param maxnum
	 * @return
	 */
	ErrorCode<List<RemoteActivity>> getActivityListByFlag(String citycode, String tag, Integer timetype, String flag, int from, int maxnum);
	/**
	 * 用户的活动列表
	 * @param memberid
	 * @param citycode
	 * @param timetype
	 * @param tag
	 * @param relatedid
	 * @return
	 */
	
	ErrorCode<List<RemoteActivity>> getMemberActivityListByMemberid(Long memberid, String citycode, int timetype, String tag, Long relatedid, int from, int maxnum);
	/**
	 * 用户参加的活动列表
	 * @param memberid
	 * @param from
	 * @param maxnum
	 * @return
	 */
	ErrorCode<List<RemoteActivity>> getMemberJoinActivityList(Long memberid, int from, int maxnum);
	/**
	 * 根据状态查询用户信息
	 * @param citycode
	 * @param status
	 * @param from
	 * @param maxnum
	 * @return
	 */
	ErrorCode<List<RemoteActivity>> getActivityListByStatus(String citycode, String status, int from, int maxnum);
	/**
	 * 根据tag查询活动
	 * @param citycode
	 * @param timeType
	 * @param tag
	 * @param countycode
	 * @param from
	 * @param maxnum
	 * @return
	 */
	ErrorCode<List<RemoteActivity>> getActivityListByTag(String citycode, String atype, String flag, 
			String tag, Long relatedid,
			String category, Long categoryid, Timestamp starttime, Timestamp endtime, String order, String asc,
			Integer from, Integer maxnum);
	/**
	 * 用户收藏的关联的活动
	 * @param memberid
	 * @param tag
	 * @param relatedidList
	 * @param from
	 * @param maxnum
	 * @return
	 */
	ErrorCode<List<RemoteActivity>> getMemberActivityList(Long memberid, String tag, List<Long> relatedidList, int from, int maxnum);
	/**
	 * 格瓦拉推荐的活动
	 * @param idList
	 * @param isClose
	 * @param from
	 * @param maxnum
	 * @return
	 */
	ErrorCode<List<RemoteActivity>> getGewaCommendActivityList(List<Long> idList, boolean isClose);
	/**
	 * 该用户朋友发起的活动列表
	 * @param tag			
	 * @param idList		朋友ID集合
	 * @param from
	 * @param maxnum
	 * @return
	 */
	ErrorCode<List<RemoteActivity>> getFriendActivityList(String tag, List<Long> idList, int from, int maxnum);

	/**
	 * 置顶的活动列表
	 * @param citycode
	 * @param atype
	 * @param tag
	 * @param relatedid
	 * @return
	 */
	ErrorCode<List<RemoteActivity>> getTopActivityList(String citycode, final String atype, String tag, Long relatedid);

	/**
	 * 收藏活动
	 * @param memberid
	 * @param activityId
	 * @return
	 */
	ErrorCode<String> collectActivity(Long memberid, Long activityId);
	/**
	 * 获取用户参加的活动信息
	 * @param memberid
	 * @param activityid
	 * @return
	 */
	ErrorCode<RemoteApplyjoin> getApplyJoin(Long memberid, Long activityid);
	
	/**
	 * 获取用户组参加的活动信息
	 * @param memberids
	 * @param activityid
	 * @return
	 */
	ErrorCode<List<RemoteApplyjoin>> getApplyJoinByMemberids(List<Long> memberids, Long activityid);
	/**
	 * 获取活动参加的信息
	 * @param activityid
	 * @return
	 */
	ErrorCode<List<RemoteApplyjoin>> getApplyJoinListByActivityid(Long activityid);
	
	/**
	 * 获取用户参加的活动信息
	 * @param memberid
	 * @param from
	 * @param maxnum
	 * @return
	 */
	ErrorCode<List<RemoteApplyjoin>> getApplyJoinListByMemberid(Long memberid, int from, int maxnum);
	/**
	 * 增加活动点击数量
	 * @param activityid
	 * @return
	 */
	ErrorCode addClickedtimes(Long activityid);
	/**
	 * 根据relateid集合获取活动列表
	 * @param citycode
	 * @param atype
	 * @param tag
	 * @param relatedidList
	 * @param from
	 * @param maxnum
	 * @return
	 */
	ErrorCode<List<RemoteActivity>> getActivityListByRelatedidList(String citycode, String atype, String tag, 
			List<Long> relatedidList, int from, int maxnum);
	/**
	 * 根据区域分组
	 * @param tag
	 * @param date
	 * @param citycode
	 * @return
	 */
	ErrorCode<List<CountyCount>> getGroupActivityByTag(String tag, Date date, String citycode);
	
	/**
	 * 用户的活动数量
	 * @param memberid
	 * @param citycode
	 * @param timetype
	 * @param tag
	 * @param relatedid
	 * @return
	 */
	
	ErrorCode<Integer> getMemberActivityCount(Long memberid, String citycode, int timetype, String tag, Long relatedid);
	
	
	/**
	 * 查询当前活动数量
	 * @param citycode
	 * @param atype
	 * @param timetype
	 * @param tag
	 * @param relatedid
	 * @return
	 */
	ErrorCode<Integer> getActivityCount(String citycode, String atype, int timetype, String tag, Long relatedid);
	
	
	/**
	 * 根据关联活动的对象查询其参与数量
	 * @param citycode
	 * @param tag
	 * @param relatedid
	 * @param from
	 * @param maxnum
	 * @return
	 */
	ErrorCode<List<RemoteApplyjoin>> getApplyJoinList(String citycode, String tag, Long relatedid, int from, int maxnum);
	
	/**
	 * 获取活动的参与信息
	 * @param activityid
	 * @param from
	 * @param maxnum
	 * @return
	 */
	ErrorCode<List<RemoteApplyjoin>> getApplyJoinListByActivityid(Long activityid, int from, int maxnum);
	/**
	 * 获取某个版块参加活动信息
	 * @param citycode
	 * @param tag
	 * @return
	 */
	ErrorCode<Integer> getApplyJoinCountByTag(String citycode, String tag);
	/**
	 * 用户参加的活动数量
	 * @param memberid
	 * @return
	 */
	ErrorCode<Integer> getMemberJoinActivityCount(Long memberid);
	/**
	 * 该用户朋友发起的活动数量
	 * @param tag			
	 * @param idList		朋友ID集合
	 * @return
	 */
	ErrorCode<Integer> getFriendActivityCount(String tag, List<Long> idList);
	
	
	/**
	 * 参加活动
	 * @param memberid
	 * @param activityid
	 * @param sex
	 * @param realname
	 * @param mobile
	 * @param joinnum
	 * @param joinDate
	 * @param walaAddress
	 * @return
	 */
	ErrorCode<RemoteActivity> joinActivity(Long memberid, Long activityid, String sex, String realname, String mobile, Integer joinnum, Date joinDate, String walaAddress);
	/**
	 * 取消参加活动
	 * @param activityid
	 * @param sex
	 * @param realname
	 * @param mobile
	 * @param joinnum
	 * @param joinDate
	 * @param address
	 * @return
	 */
	ErrorCode<RemoteActivity> cancelActivity(Long activityid, Long memberid);
	
	/**
	 * 取消收藏
	 * @param activityid
	 * @param memberid
	 * @return
	 */
	ErrorCode<String> cancelCollection(Long activityid, Long memberid);
	/**
	 * 根据时间类型查询活动列表
	 * @param citycode
	 * @param atype
	 * @param timetype
	 * @param flag
	 * @param tag
	 * @param relatedid
	 * @param category
	 * @param categoryid
	 * @param from
	 * @param maxnum
	 * @return
	 */
	ErrorCode<List<RemoteActivity>> getActivityListByTimetype(String citycode, String atype, Integer timetype, String flag, String tag,
			Long relatedid, String category, Long categoryid, Integer from, Integer maxnum);
	ErrorCode<List<RemoteActivity>> getActivityList(String citycode, String atype, Integer timetype, String tag,
			Long relatedid, String category, Long categoryid, Integer from, Integer maxnum);
	ErrorCode<List<RemoteActivity>> getActivityListByOrder(String citycode, String atype, Integer timetype, String tag,
			Long relatedid, String category, Long categoryid, String order, Integer from, Integer maxnum);
	ErrorCode<List<RemoteActivity>> getActivityListByTime(String citycode, String atype, Timestamp starttime, Timestamp endtime, String flag,
			String tag, Long relatedid, String category, Long categoryid, String order, String asc, Integer from, Integer maxnum);
	ErrorCode<Integer> getActivityCountByTime(String citycode, String atype, Timestamp starttime, Timestamp endtime, String flag, String tag,
			Long relatedid, String category, Long categoryid);
	ErrorCode<Integer> getCurrActivityCount(String citycode, String atype, String flag, String tag, Long relatedid,
			String category, Long categoryid, Timestamp starttime, Timestamp endtime);
	ErrorCode<List<RemoteTreasure>> getTreasureList(Long activityid, String asc, int from, int maxnum);
	ErrorCode<RemoteActivity> addActivity(Member member, String citycode, Long activityid, String tag, Long relatedid, String category, Long categoryid,
			String title, Integer price, Timestamp starttime, Timestamp endtime, String contentdetail, String address);
	/**
	 * 推荐活动
	 * @param citycode
	 * @param signname
	 * @param tag
	 * @param isClose
	 * @param from
	 * @param maxnum
	 * @return
	 */
	List<RemoteActivity> getGewaCommendActivityList(String citycode, String signname, String tag, int from, int maxnum);
	/**
	 * 获取统计的数据
	 * @return
	 */
	ErrorCode<List<CategoryCount>> getCategoryCountList();
	/**
	 * 获取活动ID
	 * @param begintime
	 * @param endtime
	 * @return
	 */
	List<String> getActivityIdList(Timestamp begintime, Timestamp endtime);
	/**
	 * 获取一段时间内参加活动的数量
	 * @param begintime
	 * @param endtime
	 * @return
	 */
	Integer getJoinCountByAddtime(Timestamp begintime, Timestamp endtime);
	/**
	 * 获取发表活动top的用户
	 * @param citycode
	 * @param tag
	 * @param maxnum
	 * @return
	 */
	List<Long> getTopAddMemberidList(String citycode, String tag, int maxnum);
	/**
	 * 得到当前的活动
	 * @param citycode
	 * @param atype
	 * @param tag
	 * @param relatedidList
	 * @param from
	 * @param maxnum
	 * @return
	 */
	ErrorCode<List<RemoteActivity>> getCurrActivityListByRelatedidList(String citycode, String atype, String tag, List<Long> relatedidList, int from,
			int maxnum);
	ErrorCode<List<RemoteActivity>> getCurrActivityList(String citycode, String atype, String tag, Long relatedid, String category, Long categoryid,
			Timestamp starttime, Timestamp endtime, int from, int maxnum);
	/**
	 * 活动的活动
	 * @param citycode
	 * @param tag
	 * @param relatedid
	 * @param cateogory
	 * @param categoryid
	 * @param from
	 * @param maxnum
	 * @return
	 */
	ErrorCode<List<RemoteActivity>> getCommendActivityList(String citycode, String tag, Long relatedid, String cateogory, Long categoryid, int from,
			int maxnum);
	/**
	 * 参与人数比较多的活动
	 * @param citycode
	 * @param tag
	 * @param relatedid
	 * @param cateogory
	 * @param categoryid
	 * @param from
	 * @param maxnum
	 * @return
	 */
	ErrorCode<List<RemoteActivity>> getHotActivityList(String citycode, String tag, Long relatedid, String cateogory, Long categoryid, int from,
			int maxnum);
	/**
	 * 推荐的活动
	 * @param citycode
	 * @param signname
	 * @param from
	 * @param maxnum
	 * @return
	 */
	ErrorCode<List<RemoteActivity>> getActivityListBySignname(String citycode, String signname, int from, int maxnum);
	/**
	 * 根据时间类型查询
	 * @param citycode
	 * @param datetype
	 * @param tag
	 * @param relatedid
	 * @param category
	 * @param categoryid
	 * @param from
	 * @param maxnum
	 * @return
	 */
	ErrorCode<List<RemoteActivity>> getActivityListByDatetype(String citycode, String atype, String datetype,  String isFee, String tag, Long relatedid, String category,
			Long categoryid, int from, int maxnum);
	/**
	 * 获取活动中场次id的接口
	 * @param activityid
	 * @return
	 */
	List<String> getActivityMpidList(Long activityid);
	ErrorCode<List<RemoteActivity>> getActivityListByMemberidList(String citycode, String atype, String datetype, List<Long> memberidList, String isFee, String tag, Long relatedid, String category,
			Long categoryid, int from, int maxnum);
	
	/**
	 * 用户操作活动的结果
	 * @param activityid
	 * @param memberid
	 * @return
	 */
	List<String> memberOperActivityResult(Long activityid, Long memberid);
	/**
	 * 活动的场次信息列表
	 * @param activityid
	 * @return
	 */
	ErrorCode<List<RemoteActivityMpi>> getRemoteActiviyMpiList(Long activityid);
	/**
	 * 根据类型（日期类型，时间类型）查询活动，和活动首页保持一致
	 * @param citycode
	 * @param countycode
	 * @param atype
	 * @param datetype
	 * @param timetype
	 * @param tag
	 * @param relatedid
	 * @param category
	 * @param categoryid
	 * @param isFee
	 * @param order
	 * @param from
	 * @param maxnum
	 * @return
	 */
	ErrorCode<List<RemoteActivity>> getActivityListByType(String citycode, String countycode, String atype, String datetype, Integer timetype,
			String tag, Long relatedid, String category, Long categoryid, String isFee, String order, Integer from, Integer maxnum);
	//订单修改手机号时通知activity里订单修改手机号
	void updateActiviyOrderMobile(String tradeNo, String mobile);
	//订单退款通知activity修改订单状态
	ErrorCode<String> activityOrderReturn(String tradeNo);
	//根据tag拿到关联的影院等
	List<Long> getActivityRelatedidByTag(String citycode, Integer timetype, String tag);
}
