package com.gewara.untrans;

import java.sql.Timestamp;
import java.util.List;

import com.gewara.model.user.Friend;
import com.gewara.model.user.Treasure;
import com.gewara.xmlbind.bbs.ReComment;

/**
 * 哇啦评论服务客户端
 * @author quzhuping
 *
 */
public interface WalaApiService {
	
	/**
	 * 保存哇啦评论
	 * @param reComment 评论实体
	 * @return reCommentID
	 */
	Long saveReComment(ReComment reComment);
	
	void deleteMicroReComment(Long mid);
	
	ReComment getReCommentById(Long reCommentId);
	
	void updateReComment(ReComment recomment);
	
	List<ReComment> getReplyMeReCommentList(Long memberid, int from, int maxnum);
	
	void updateReCommentReadSatus(Long memberid);
	
	void updateReplyCommentReadSatus(Long memberid);
	
	Integer getReCommentCountByMemberid(Long memberid, String status);
	
	List<ReComment> getRecommentBycommentid(Long commentid, int from, int maxnum);
	
	Integer getReplyMeCommentCount(Long memberid);
	
	Integer getReplyMeReCommentCount(Long memberid);
	
	List<ReComment> getReCommentByRelatedidAndTomemberid(Long relatedid, Long tomemberid, Long memberid, int from, int maxnum);
	
	Integer getReCommentByRelatedidAndTomemberidCount(Long relatedid, Long tomemberid, Long memberid);
	
	Integer getMicroSendReCommentCount(Long memberid);
	
	List<ReComment> getMicroSendReCommentList(Long memberid, int from, int maxnum);
	
	Integer getMicroReceiveReCommentCount(Long memberid);
	
	List<ReComment> getMicroReceiveReCommentList(Long memberid, int from, int maxnum);

	Integer getReCommentCountByRelatedidAndTomemberid(Long relatedid, Long tomemberid, Long memberid);
	
	List<ReComment> getReplyCommentListByAtMe(String nickName, int from, int maxnum);
	
	//后台哇啦回复审核
	List<ReComment> getReCommentList(Long cid, Long memberid, Timestamp starttime, Timestamp endtime, String status, String keyname, int from, int maxnum);
	Integer getReCommentCount(Long cid, Long memberid, Timestamp starttime, Timestamp endtime, String status, String keyname);

	void addTreasure(Treasure treasure);
	void delTreasure(Long memberId, Long relatedId, String tag, String action);
	void addFriend(Friend friend);
	void delFriend(Long memberFrom, Long memberTo);

}
