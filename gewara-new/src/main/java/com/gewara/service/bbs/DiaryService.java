package com.gewara.service.bbs;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.gewara.model.bbs.Diary;
import com.gewara.model.bbs.DiaryBase;
import com.gewara.model.bbs.DiaryComment;
import com.gewara.model.bbs.VoteChoose;
import com.gewara.model.bbs.VoteOption;

/**
 * @author <a href="mailto:acerge@163.com">gebiao(acerge)</a>
 * @since 2007-9-28下午02:05:17
 */
public interface DiaryService {
	/**
	 * 评论的数量
	 * @param type
	 * @param tag
	 * @param relatedid
	 * @return
	 */
	DiaryBase getDiaryBase(Long id);
	<T extends DiaryBase> Integer getDiaryCount(Class<T> clazz, String citycode, String type, String tag, Long relatedid);
	<T extends DiaryBase> Integer getDiaryCountByKey(Class<T> clazz, String citycode, String type, String tag, Long relatedid, String key, Timestamp startTime, Timestamp endTime);
	<T extends DiaryBase> List<T> getDiaryList(Class<T> clazz, String citycode, String type, String tag, Long relatedid, int start, int maxnum);
	/**
	 * 有排序字段
	 */
	<T extends DiaryBase> List<T> getDiaryList(Class<T> clazz, String citycode, String type, String tag, Long relatedid, int start, int maxnum, String order);
	/**
	 * 查询一段时间内的帖子信息
	 */
	<T extends DiaryBase> List<T> getDiaryListByOrder(Class<T> clazz, String citycode, String type, String tag, Long relatedid, Timestamp startTime,Timestamp endTime, String order, boolean asc, int start, int maxnum);
	/**
	 * 有搜索字段
	 */
	<T extends DiaryBase> List<T> getDiaryListByKey(Class<T> clazz, String citycode, String type, String tag, Long relatedid, int start, int maxnum, String key, Timestamp startTime,Timestamp endTime);
	/**
	 * 有Flag字段 
	 */
	<T extends DiaryBase> List<T> getDiaryListByFlag(Class<T> clazz, String citycode, String type, String tag, String flag, int from, int maxnum);
	/**
	 * @param type
	 * @param tag
	 * @return 置顶的Diary
	 */
	List<Diary> getTopDiaryList(String citycode, String type, String tag, boolean isCache);
	/**
	 * 获取24小时排行帖子（根据24小时内回复个数）
	 * @param tag
	 * @return
	 */
	List<Map> getOneDayHotDiaryList(String citycode, String tag);
	/**
	 * @param type
	 * @param tag
	 * @param memberid
	 * @param from
	 * @param maxnum
	 * @return
	 */
	<T extends DiaryBase> List<T> getDiaryListByMemberid(Class<T> clazz, String type, String tag, Long memberid, int from, int maxnum);
	/**
	 * 某个用户的评论数量
	 * @param type
	 * @param tag
	 * @param mid
	 * @return
	 */
	<T extends DiaryBase> Integer getDiaryCountByMemberid(Class<T> clazz, String type, String tag, Long memberId);
	/**
	 * 得到某个Diary的回复
	 * @param diaryId
	 * @return
	 */
	List<DiaryComment> getDiaryCommentList(Long diaryId, int from, int maxnum); 
	List<DiaryComment> getDiaryCommentList(Long diaryId); 
	/**
	 * @param type
	 * @param category
	 * @param categoryid
	 * @param memberid
	 * @param from
	 * @param maxnum
	 * @return
	 */
	List<Diary> getFriendDiaryList(String type, String category, Long categoryid, Long memberid, int from, int maxnum);
	/**
	 * 根据投票的id，它的选项
	 * @param vid
	 * @return
	 */
	List<VoteOption> getVoteOptionByVoteid(Long vid);
	/**
	 * 投票的数量
	 * @param did
	 * @return
	 */
	Integer getVotecount(Long did);
	/**
	 * 我对某个帖子的投票详情
	 * @return
	 */
	List<VoteChoose> getVoteChooseByDiaryidAndMemberid(Long did,Long mid);
	Integer getDiaryCommentCount(String tag, Long diaryId);
	/**
	 * 是否已经投票(准对杜拉拉活动)
	 * @param memberid
	 * @return
	 */
	boolean isMemberVoted(Long memberid, Long diaryid);
	
	//最近一周最热影评
	List<Diary> getHotCommentDiary(String citycode, String type, String tag, Long relateid, int from,int maxnum);
	/**
	 * 查询圈内热门话题
	 */
	<T extends DiaryBase> List<T> getHotCommuDiary(Class<T> clazz, String citycode, boolean isCommu,String type,int from,int maxnum);
	
	/**
	 * 根据status查询帖子
	 * @param status
	 * @return
	 */
	<T extends DiaryBase> List<T> getDiaryListByStatus(Class<T> clazz, String keyname, String status, Date fromDate, Date endDate, int from, int maxnum);
	<T extends DiaryBase> Integer getDiaryCountByStatus(Class<T> clazz, String keyname, String status, Date fromDate, Date endDate);
	/**
	 * 根据status查询帖子留言
	 * @param status
	 * @return
	 */
	List<DiaryComment> getDiaryCommentListByStatus(String keyname, String status, Date fromDate, Date endDate, int from, int maxnum);
	Integer getDiaryCommentCountByStatus(String keyname, String status, Date fromDate, Date endDate);
	<T extends DiaryBase> List<T> getRepliedDiaryList(Class<T> clazz, Long memberid, int from, int maxnum);
	<T extends DiaryBase> Integer getRepliedDiaryCount(Class<T> clazz, Long memberid);

	/**
	 * 查询电影、话剧、运动项目的点评列表
	 * @param keyname
	 * @param from
	 * @param maxnum
	 * @return
	 */
	List<Map> getMDSDiaryListByKeyname(String citycode, String keyname, String tag, String name, int from, int maxnum);
	Integer getMDSDiaryCountByKeyname(String citycode, String keyname, String tag, String name);
	/**
	 * 分页查询演出评论列表数据
	 * @param citycode
	 * @param key
	 * @param starttime
	 * @param endtime
	 * @param order
	 * @param from
	 * @param maxnum
	 * @return
	 */
	List<Diary> getDiaryBySearchkeyAndOrder(String citycode,String key,Timestamp starttime, Timestamp endtime, String order,int from,int maxnum);
}
