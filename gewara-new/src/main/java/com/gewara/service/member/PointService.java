/**
 * 
 */
package com.gewara.service.member;

import java.sql.Timestamp;
import java.util.List;
import java.util.Map;

import com.gewara.model.bbs.Diary;
import com.gewara.model.pay.GewaOrder;
import com.gewara.model.user.Member;
import com.gewara.model.user.Point;
import com.gewara.model.user.PointHist;
import com.gewara.support.ErrorCode;

/**
 * @author hxs(ncng_2006@hotmail.com)
 * @since Feb 2, 2010 10:10:58 AM
 */
public interface PointService {
	
	/**
	 * 根据memberid查询积分列表
	 * @param memberid
	 * @return
	 */
	List<Point> getPointListByMemberid(Long memberid, String tag, Timestamp addtime, Timestamp endtime, String order, int from, int maxnum);
	Integer getPointCountByMemberid(Long memberid, String tag, Timestamp addtime, Timestamp endtime);
	List<Point> getPointListByMemberidAndPointValue(Long memberid, String tag, int pointvalue, Timestamp addtime, Timestamp endtime, String order, int from, int maxnum);
	Integer getPointCountByMemberidAndPointValue(Long memberid, String tag, int pointvalue, Timestamp addtime, Timestamp endtime);
	
	/**
	 * 添加积分信息
	 */
	Point addPointInfo(Long memberid, Integer pointvalue, String reason, String tag);
	Point addPointInfo(Long memberid, Integer pointvalue, String reason, String tag, Long tagid, Long adminid);
	Point addPointInfo(Long memberid, Integer pointvalue, String reason, String tag, Long tagid, Long adminid, Timestamp addtime);
	Point addPointInfo(Long memberid, Integer pointvalue, String reason, String tag, Long tagid, Long adminid, Timestamp addtime, String uniquetag, String statflag);
	/**
	 * 是否已经获取过积分
	 * @return
	 */
	boolean isGetLoginPoint(Long memberId, String date);
	
	/**
	 * 后台积分查询
	 * @param startTime
	 * @param endTime
	 * @param tag
	 * @param valueStart
	 * @param valueEnd
	 * @param pageNo
	 * @param maxNum
	 * @return List<Map(memberid, gainpoint, paypoint)> 
	 */
	List<Map> getPointVariableList(Timestamp startTime, Timestamp endTime, String tag, int valueStart, int valueEnd, int from, int maxnum);
	/**
	 * @param startTime
	 * @param endTime
	 * @param tag
	 * @param valueStart
	 * @param valueEnd
	 * @return Map(memberid, gainpoint, paypoint)
	 */
	Map getPointVariableMap(Timestamp startTime, Timestamp endTime, String tag, int valueStart, int valueEnd);
	//前台查询积分总数
	List<Map> getTopPointByDateMap(Timestamp startTime, Timestamp endTime, List<String> tagList, int from,int maxNum);
	/**
	 * 后台积分查询数量
	 */
	Integer getPointVariableCount(Timestamp startTime, Timestamp endTime, String tag, int valueStart, int valueEnd);
	/**
	 * 查询当前系统总积分
	 */
	Integer getSumPoint();
	
	/**
	 * 查询积分tag
	 */
	List getPointTagList();
	
	/**
	 * 根据id查询他所获取和消耗的积分信息
	 */
	List<Point> getPointListByIdAndType(Long memberid, Timestamp startTime, Timestamp endTime, String tag, String type, int from, int maxnum);
	
	/**
	 * 根据id查询他所获取和消耗的积分信息数量
	 */
	Integer getPointByIdAndTypeCount(Long memberid, Timestamp startTime, Timestamp endTime, String tag, String type);
	/**
	 * 每日加积分
	 * @param member
	 * @return
	 */
	ErrorCode<Point> addLoginPoint(Member member, String type, Timestamp cur);
	ErrorCode<Map> addLoginPointInFestival(Member member);

	ErrorCode<Point> addOrderPoint(GewaOrder order);
	
	List getPointExpendDetail(String startTime, String endTime, String tag);
	
	/**
	 * 查询三个月内观影用户所发的影评
	 */
	List<Diary> getDiaryList(String tag,String pointTag,int from,int maxnum);
	Integer getDiaryCount(String tag,String pointTag);
	/**
	 * 查询当前系统中用户的积分是否有异常
	 */
	boolean isLoginPointRewards(Long memberid, Timestamp cur);
	Integer getPointRewardsDay(Long memberid, Timestamp cur);
	/**
	 根据tag,tagid,memberid查询积分记录*/
	Point getPointByMemberiAndTagid(Long memberid, String tag, Long tagid);
	
	/**
	 *  根据 memberid查询 uniquetag标识
	 * */
	int countUniquetagByCombine(String combUniquetag);
	
	/**
	 *  最近领取红包的用户
	 * */
	List<Map> getRecentlyGetPointList(int maxnum);
	/**
	 * 获取领取红包大于5的用户 
	 */
	List<Map> getLuckGetPointList(int maxnum);
	/**
	 *  邀请好友买票送积分
	 * */
	void addPointToInvite(Long memberid, Integer point);
	
	/**
	 * 添加积分信息到积分统计表
	 */
	void addPointStats(Timestamp curTimestamp);
	/**
	 * 用户用积分支付的记录
	 * @param tradeNo
	 * @return
	 */
	List<Point> getPointListByTradeNo(String tradeNo);
	//用户完成新手任务加积分
	ErrorCode<Point> addNewTaskPoint(Long memberid, String tag);
	ErrorCode<String> validUsePoint(Long memberid);
	/**
	 * 验证微博账户领取积分
	 * @param member
	 * @return
	 */
	ErrorCode validWeiboPoint(Member member, boolean isApp);
	ErrorCode validWeiboPoint(Member member);
	/**
	 * 查询积分历史表记录
	 * @param memberid
	 * @param tag
	 * @param pointvalue
	 * @param addtime
	 * @param endtime
	 * @param order
	 * @param from
	 * @param maxnum
	 * @return
	 */
	List<PointHist> getPointHistListByMemberidAndPointValue(Long memberid, String tag, int pointvalue, Timestamp addtime, Timestamp endtime,String order, int from, int maxnum);
	/**
	 * 查询积分历史表记录数
	 * @param memberid
	 * @param tag
	 * @param pointvalue
	 * @param addtime
	 * @param endtime
	 * @return
	 */
	Integer getPointHistCountByCondition(Long memberid, String tag, int pointvalue, Timestamp addtime, Timestamp endtime);
}