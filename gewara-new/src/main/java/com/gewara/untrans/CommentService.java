package com.gewara.untrans;

import java.sql.Timestamp;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.gewara.model.user.Member;
import com.gewara.support.ErrorCode;
import com.gewara.xmlbind.bbs.Comment;
import com.gewara.xmlbind.bbs.CountByMovieIdAddDate;
public interface CommentService {
	/**
	 * 活跃用户：几天内发wala数最多的用户
	 * @param maxnum
	 * @return
	 */
	List<Map> getActiveMemberList(int maxnum);
	/**
	 * 根据哇啦id查询转载的哇啦信息
	 */
	List<Comment> getCommentListByTransfer(Long commentId, int from, int maxnum);
	
	Comment getCommentById(Long commentId);
	
	List<Comment> getCommentByIdList(Collection<Long> idList);
	/**
	 * 
	 * 根据tag查询评论记录
	 */
	List<Comment> pointByFreeBackCommentList(String tag,int from, int maxnum);
	
	/**
	 * wap版我关注的
	 */
	List<Comment> getMyAttentionCommentListByMemberid(Long memberid,int from,int maxnum);
	Integer getMyAttentionCommentCountByMemberid(Long memberid);

	/**
	 * 微博中提到我的
	 */
	List<Comment> getMicroBlogListByMemberid(String nickName, Long memberid,int from,int maxnum);
	
	/**
	 * @param tag
	 * @param from
	 * @param maxnum
	 * @return 根据增加时间排序
	 */
	List<Comment> getCommentListByTag(String tag, int from, int maxnum);

	/**
	 * 根据关联的项目ID查找评论的记录数
	 * @param tag
	 * @return
	 */
	Integer getCommentCountByTag(String tag);
	List<Comment> getCommentListByRelatedId(String tag,String flag, Long relatedId, String order, int from, int maxnum);
	List<Comment> getCommentListByRelatedId(String tag,String flag, Long relatedId, String order, Long mincommentid, int from, int maxnum);
	List<Comment> getHotCommentListByRelatedId(String tag,String flag, Long relatedId, Timestamp startTime, Timestamp endTime, int from, int maxnum);
	Comment getNewCommentByRelatedid(String tag,Long relatedId, Long memberid);
	/**
	 * 根据关键字查询
	 * @param tag
	 * @param key
	 * @return
	 */
	List<Comment>  getCommentListByKey(String tag, String key);

	/**
	 * 根据关联的项目ID查找评论的记录数
	 * @param tag
	 * @return
	 */
	Integer getCommentCountByRelatedId(String tag, Long relatedId);
	/**
	 * 根据关联的项目ID查找评论的记录数,flag 为ticket 查询购票用户哇啦总数
	 * @param tag
	 * @return
	 */
	Integer getCommentCountByRelatedId(String tag,String flag, Long relatedId);

	List<Comment> getCommentListByTags(String[] tag, Long memberid, boolean isTransfer, int from, int maxnum);
	Integer getCommentCountByTags(String[] tag, Long memberid, boolean isTransfer);
	/**
	 * 查询哇啦信息(后台)
	 */
	List<Comment> getCommentList(String tag, Long relatedid, Long memberid, String body, String status, int from,int maxnum);
	List<Comment> getCommentList(String tag, Long relatedid, Long memberid, String body, String status, Timestamp beginDate, Timestamp endDate, int from,int maxnum);
	/**
	 * 查询哇啦信息数量(后台)
	 * @param id
	 * @param memberid
	 * @param tag
	 * @param relatedid
	 * @param body
	 * @return
	 */
	Integer getCommentCount(String tag, Long relatedid, Long memberid, String body, String status);
	Integer getCommentCount(String tag, Long relatedid, Long memberid, String body, String status, Timestamp beginDate, Timestamp endDate);
	/**
	 * 获取叠楼哇啦信息
	 * @return
	 */
	Integer getLongCommentCount(String tag, Long relatedid, String status);
	List<Comment> getLongCommentList(String tag, Long relatedid, String status, int from, int maxnum);	
	/**
	 * 更新哇啦数量
	 * 
	 */
	void updateCommentReplyCount(Long commentid,String type);
	/**
	 * --------------------------------
	 * 新版哇啦
	 * --------------------------------
	 */
	
	/**
	 * 人气用户
	 */
	List<Map> getHotMicroMemberList(String tag, Long memberid, int maxnum);
	
	/**
	 * 哇啦搜索(哇啦，人，视频，图片，评论)
	 * 
	 */
	List<Comment> searchCommentList(String searchkey,String type,int from,int maxnum);
	List<Comment> searchCommentList(String searchkey,String type, List<Long> memberidList, int from,int maxnum);
	Integer searchCommentCount(String searchkey,String type);
	/**
	 * 获取全部哇啦信息（包括转载,图片，视频）
	 * @param commentList
	 * @param name
	 * @return
	 */
	Map getAllCommentList(List<Comment> commentList,String name);
	
	ErrorCode<Comment> addComment(Member member, String tag, Long relatedid, String body, String link, boolean ignoreCheck, String pointx, String pointy, String ip);
	ErrorCode<Comment> addComment(Member member, String tag, Long relatedid, String body, String link, boolean ignoreCheck, Integer generalmark, String pointx, String pointy, String ip);
	ErrorCode<Comment> addMicroComment(Member member, String tag, Long relatedid, String body, String link, String address, boolean ignoreCheck, String pointx, String pointy, String ip);
	ErrorCode<Comment> addMicroComment(Member member, String tag, Long relatedid, String body, String link, String address, Long transferid, boolean ignoreCheck, Integer generalmark, String pointx, String pointy, String ip);
	
	/**
	 *  根据tag + address 查询 (For MAS)
	 * **/
	List<Comment> getCommentsByTagAndAddress(String tag, String address, Timestamp starttime, Timestamp endtime, String topic, String handle, int from, int maxnum);
	Integer getCommentCountByTagAndAddress(String tag, String address, Timestamp starttime, Timestamp endtime, String topic, String handle);
	List<Comment> getModeratorDetailList(String topic, boolean asc, int from, int maxnum);
	
	List<Comment> getCommentListByTagMemberids(String[] tag,List<Long> ids,Timestamp startTime,Timestamp endTime, int from, int maxnum);
	/**
	 * 查询用户最近购票影院的哇啦
	 * @param memberid 用户ID
	 * @param from 开始行数
	 * @param maxnum 最大行数据
	 * @return Comment 集合，如没有则size = 0 或 isEmpty = true
	 */
	List<Comment> getCommentListByMemberid(Long memberid, int from, int maxnum);
	
	/**
	 * 根据话题名称查询话题数量
	 * @param name
	 * @return
	 */	
	Integer getModeratorDetailCount(String topic);
	
	ErrorCode<Comment> addMicroComment(Member member, String tag, Long relatedid, String body, String link, 
			String address, Long transferid, boolean ignoreInterval, Integer generalmark, String otherInfo,String pointx, String pointy, String ip, String apptype);

	//后台哇啦审核
	List<Comment> getCommentList(Long memberid, Timestamp starttime, Timestamp endtime, String transfer, String status, String keyname, String isMicro, int from, int maxnum);
	Integer getCommentCount(Long memberid, Timestamp starttime, Timestamp endtime, String transfer, String status, String keyname, String isMicro);
	
	List<Long> getTopAddMemberidList(String tag, int maxnum);
	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	Long saveComment(Comment comment);
	Long updateComment(Comment comment);
	List<Comment> getCommentList(String[] tags, Long memberId, Date beginDate, Date endDate, int from, int maxnum);
	Integer getCommentCount(String tag,String flag,Long relatedid, String status, Long memberid, Long transferid, String body, Timestamp startTime,	Timestamp endTime);
	Integer pointByFreeBackCommentCount(String tag);
	List<Comment> getCommentListByMemberIdAndTags(String[] tags, Long memberId, Date beginDate, Date endDate, int from, int maxnum);
	void deleteComment(Long commentId);
	List<CountByMovieIdAddDate> getCountByMovieIdAddDate(String movieIds, String type, Date date);
	List<HashMap> getTaskCommentList();
	void addReplyToComment(String mobile, String msg, String ip);
	List<Comment> getHotCommentListByTopic(String topic, Timestamp startTime, Timestamp endTime, String order, int from, int maxnum);
}
