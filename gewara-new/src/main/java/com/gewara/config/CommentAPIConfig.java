package com.gewara.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * 哇啦API配置信息
 * 
 * @author quzhuping
 * 
 */
@Component("commentAPIConfig")
public class CommentAPIConfig {
	@Value("${saveCommentApiURL}")
	private String saveCommentApiURL;

	/**
	 * 获取保存哇啦API的URL
	 * 
	 * @return
	 */
	public String getSaveCommentApiURL() {
		return saveCommentApiURL;
	}

	private @Value("${deleteCommentURL}")
	String deleteCommentURL;

	/**
	 * 删除哇啦API的URL
	 * 
	 * @return
	 */
	public String getDeleteCommentURL() {
		return deleteCommentURL;
	}

	private @Value("${saveReCommentApiURL}")
	String saveReCommentApiURL;

	/**
	 * 获取保存哇啦评论API的URL
	 * 
	 * @return
	 */
	public String getSaveReCommentApiURL() {
		return saveReCommentApiURL;
	}

	private @Value("${queryCommentByIdApiURL}")
	String queryCommentByIdApiURL;

	/**
	 * 获取单个哇啦API的URL
	 * 
	 * @return
	 */
	public String getQueryCommentByIdApiURL() {
		return queryCommentByIdApiURL;
	}

	private @Value("${queryCommentsURL}")
	String queryCommentsURL;

	/**
	 * 获取个人哇啦列表URL
	 * 
	 * @return
	 */
	public String getQueryCommentsURL() {
		return queryCommentsURL;
	}

	private @Value("${commentListURL}")
	String commentListURL;
	
	private @Value("${queryHotCommentListURL}")
	String queryHotCommentListURL;

	public String getQueryHotCommentListURL() {
		return queryHotCommentListURL;
	}
	/**
	 * 获取CommetListURL
	 * 
	 * @return
	 */
	public String getCommentListURL() {
		return commentListURL;
	}

	private @Value("${commentListByIDsURL}")
	String commentListByIDsURL;

	public String getCommentListByIDsURL() {
		return commentListByIDsURL;
	}

	private @Value("${commentCountURL}")
	String commentCountURL;

	public String getCommentCountURL() {
		return commentCountURL;
	}

	private @Value("${commentListByTagsURL}")
	String commentListByTagsURL;

	public String getCommentListByTagsURL() {
		return commentListByTagsURL;
	}

	private @Value("${commentCountByTagsURL}")
	String commentCountByTagsURL;

	public String getCommentCountByTagsURL() {
		return commentCountByTagsURL;
	}

	private @Value("${attentionCommentCountURL}")
	String attentionCommentCountURL;

	public String getAttentionCommentCountURL() {
		return attentionCommentCountURL;
	}

	private @Value("${attentionCommentListURL}")
	String attentionCommentListURL;

	public String getAttentionCommentListURL() {
		return attentionCommentListURL;
	}

	private @Value("${friendCommentCountByRelatedIdURL}")
	String friendCommentCountByRelatedIdURL;

	public String getFriendCommentCountByRelatedIdURL() {
		return friendCommentCountByRelatedIdURL;
	}

	private @Value("${myInterestCommentURL}")
	String myInterestCommentURL;

	public String getMyInterestCommentURL() {
		return myInterestCommentURL;
	}

	private @Value("${microBlogListByMemberidURL}")
	String microBlogListByMemberidURL;

	public String getMicroBlogListByMemberidURL() {
		return microBlogListByMemberidURL;
	}

	private @Value("${commentCount2URL}")
	String commentCount2URL;

	public String getCommentCount2URL() {
		return commentCount2URL;
	}

	private @Value("${commentList2URL}")
	String commentList2URL;

	public String getCommentList2URL() {
		return commentList2URL;
	}

	private @Value("${updateCommentReplyCountURL}")
	String updateCommentReplyCountURL;

	public String getUpdateCommentReplyCountURL() {
		return updateCommentReplyCountURL;
	}

	private @Value("${deleteMicroReCommentURL}")
	String deleteMicroReCommentURL;

	public String getDeleteMicroReCommentURL() {
		return deleteMicroReCommentURL;
	}

	private @Value("${hotMicroMemberListURL}")
	String hotMicroMemberListURL;

	public String getHotMicroMemberListURL() {
		return hotMicroMemberListURL;
	}

	private @Value("${hotCommentListURL}")
	String hotCommentListURL;

	public String getHotCommentListURL() {
		return hotCommentListURL;
	}

	private @Value("${hotMicroCommentListURL}")
	String hotMicroCommentListURL;

	public String getHotMicroCommentListURL() {
		return hotMicroCommentListURL;
	}

	private @Value("${searchCommentCountURL}")
	String searchCommentCountURL;

	public String getSearchCommentCountURL() {
		return searchCommentCountURL;
	}

	private @Value("${searchCommentListURL}")
	String searchCommentListURL;

	public String getSearchCommentListURL() {
		return searchCommentListURL;
	}

	private @Value("${replyMeCommentListURL}")
	String replyMeCommentListURL;

	public String getReplyMeCommentListURL() {
		return replyMeCommentListURL;
	}

	private @Value("${reCommentListByMemberidURL}")
	String reCommentListByMemberidURL;

	public String getReCommentListByMemberidURL() {
		return reCommentListByMemberidURL;
	}

	private @Value("${myAttentionCommentCountByMemberidURL}")
	String myAttentionCommentCountByMemberidURL;

	public String getMyAttentionCommentCountByMemberidURL() {
		return myAttentionCommentCountByMemberidURL;
	}

	private @Value("${allCommentListURL}")
	String allCommentListURL;

	public String getAllCommentListURL() {
		return allCommentListURL;
	}

	private @Value("${commentsByTagAndAddressURL}")
	String commentsByTagAndAddressURL;

	public String getCommentsByTagAndAddressURL() {
		return commentsByTagAndAddressURL;
	}

	private @Value("${countCommentsByTagAndAddressURL}")
	String countCommentsByTagAndAddressURL;

	public String getCountCommentsByTagAndAddressURL() {
		return countCommentsByTagAndAddressURL;
	}

	private @Value("${commentCountByTopicURL}")
	String commentCountByTopicURL;

	public String getCommentCountByTopicURL() {
		return commentCountByTopicURL;
	}

	private @Value("${moderatorDetailListURL}")
	String moderatorDetailListURL;

	public String getModeratorDetailListURL() {
		return moderatorDetailListURL;
	}

	private @Value("${moderatorDetailCountTopicURL}")
	String moderatorDetailCountTopicURL;

	public String getModeratorDetailCountTopicURL() {
		return moderatorDetailCountTopicURL;
	}

	private @Value("${commentListByTagMemberidsURL}")
	String commentListByTagMemberidsURL;

	public String getCommentListByTagMemberidsURL() {
		return commentListByTagMemberidsURL;
	}

	private @Value("${lastCommentURL}")
	String lastCommentURL;

	public String getLastCommentURL() {
		return lastCommentURL;
	}

	private @Value("${moderatorDetailCountBodyURL}")
	String moderatorDetailCountBodyURL;

	public String getModeratorDetailCountBodyURL() {
		return moderatorDetailCountBodyURL;
	}

	private @Value("${pointByFreeBackCommentListURL}")
	String pointByFreeBackCommentListURL;

	public String getPointByFreeBackCommentListURL() {
		return pointByFreeBackCommentListURL;
	}

	private @Value("${relateItemCommentListURL}")
	String relateItemCommentListURL;

	public String getRelateItemCommentListURL() {
		return relateItemCommentListURL;
	}

	private @Value("${replyMeReCommentListURL}")
	String replyMeReCommentListURL;

	public String getReplyMeReCommentListURL() {
		return replyMeReCommentListURL;
	}

	private @Value("${updateReCommentReadSatusURL}")
	String updateReCommentReadSatusURL;

	public String getUpdateReCommentReadSatusURL() {
		return updateReCommentReadSatusURL;
	}

	private @Value("${updateReplyCommentReadSatusURL}")
	String updateReplyCommentReadSatusURL;

	public String getUpdateReplyCommentReadSatusURL() {
		return updateReplyCommentReadSatusURL;
	}

	private @Value("${reCommentCountByMemberidURL}")
	String reCommentCountByMemberidURL;

	public String getReCommentCountByMemberidURL() {
		return reCommentCountByMemberidURL;
	}

	private @Value("${recommentBycommentidURL}")
	String recommentBycommentidURL;

	public String getRecommentBycommentidURL() {
		return recommentBycommentidURL;
	}

	private @Value("${replyMeCommentCountURL}")
	String replyMeCommentCountURL;

	public String getReplyMeCommentCountURL() {
		return replyMeCommentCountURL;
	}

	private @Value("${replyMeReCommentCountURL}")
	String replyMeReCommentCountURL;

	public String getReplyMeReCommentCountURL() {
		return replyMeReCommentCountURL;
	}

	private @Value("${reCommentByRelatedidAndTomemberidURL}")
	String reCommentByRelatedidAndTomemberidURL;

	public String getReCommentByRelatedidAndTomemberidURL() {
		return reCommentByRelatedidAndTomemberidURL;
	}

	private @Value("${reCommentByRelatedidAndTomemberidCountURL}")
	String reCommentByRelatedidAndTomemberidCountURL;

	public String getReCommentByRelatedidAndTomemberidCountURL() {
		return reCommentByRelatedidAndTomemberidCountURL;
	}

	private @Value("${microSendReCommentCountURL}")
	String microSendReCommentCountURL;

	public String getMicroSendReCommentCountURL() {
		return microSendReCommentCountURL;
	}

	private @Value("${microSendReCommentListURL}")
	String microSendReCommentListURL;

	public String getMicroSendReCommentListURL() {
		return microSendReCommentListURL;
	}

	private @Value("${microReceiveReCommentCountURL}")
	String microReceiveReCommentCountURL;

	public String getMicroReceiveReCommentCountURL() {
		return microReceiveReCommentCountURL;
	}

	private @Value("${microReceiveReCommentListURL}")
	String microReceiveReCommentListURL;

	public String getMicroReceiveReCommentListURL() {
		return microReceiveReCommentListURL;
	}

	private @Value("${reCommentCountByRelatedidAndTomemberidURL}")
	String reCommentCountByRelatedidAndTomemberidURL;

	public String getReCommentCountByRelatedidAndTomemberidURL() {
		return reCommentCountByRelatedidAndTomemberidURL;
	}

	private @Value("${updateCommentURL}")
	String updateCommentURL;

	public String getUpdateCommentURL() {
		return updateCommentURL;
	}

	private @Value("${friendCommentListByRelatedIdURL}")
	String friendCommentListByRelatedIdURL;

	public String getFriendCommentListByRelatedIdURL() {
		return friendCommentListByRelatedIdURL;
	}

	private @Value("${reCommentByIdURL}")
	String reCommentByIdURL;

	public String getReCommentByIdURL() {
		return reCommentByIdURL;
	}

	private @Value("${addTreasureURL}")
	String addTreasureURL;

	public String getAddTreasureURL() {
		return addTreasureURL;
	}

	private @Value("${delTreasureURL}")
	String delTreasureURL;

	public String getDelTreasureURL() {
		return delTreasureURL;
	}

	private @Value("${addFriendURL}")
	String addFriendURL;

	public String getAddFriendURL() {
		return addFriendURL;
	}

	private @Value("${delFriendURL}")
	String delFriendURL;

	public String getDelFriendURL() {
		return delFriendURL;
	}

	private @Value("${updateReCommentURL}")
	String updateReCommentURL;

	public String getUpdateReCommentURL() {
		return updateReCommentURL;
	}

	private @Value("${commentListByMemberIdAndTagsURL}")
	String commentListByMemberIdAndTagsURL;

	public String getCommentListByMemberIdAndTagsURL() {
		return commentListByMemberIdAndTagsURL;
	}

	private @Value("${longCommentListURL}")
	String longCommentListURL;

	public String getLongCommentListURL() {
		return longCommentListURL;
	}

	private @Value("${longCommentCountURL}")
	String longCommentCountURL;

	public String getLongCommentCountURL() {
		return longCommentCountURL;
	}

	private @Value("${replyCommentListByAtMeURL}")
	String replyCommentListByAtMeURL;

	public String getReplyCommentListByAtMeURL() {
		return replyCommentListByAtMeURL;
	}

	private @Value("${adminGetCommentListURL}")
	String adminGetCommentListURL;

	public String getAdminGetCommentListURL() {
		return adminGetCommentListURL;
	}

	private @Value("${adminGetCommentCountURL}")
	String adminGetCommentCountURL;

	public String getAdminGetCommentCountURL() {
		return adminGetCommentCountURL;
	}

	private @Value("${adminGetReCommentListURL}")
	String adminGetReCommentListURL;

	public String adminGetReCommentListURL() {
		return adminGetReCommentListURL;
	}

	private @Value("${adminGetReCommentCountURL}")
	String adminGetReCommentCountURL;

	public String getAdminGetReCommentCountURL() {
		return adminGetReCommentCountURL;
	}

	private @Value("${taskCommentListURL}")
	String taskCommentListURL;

	public String getTaskCommentListURL() {
		return taskCommentListURL;
	}

	private @Value("${microModeratorListURL}")
	String microModeratorListURL;

	public String getMicroModeratorListURL() {
		return microModeratorListURL;
	}

	private @Value("${toFilmfestIntresetMemberURL}")
	String toFilmfestIntresetMemberURL;

	public String getToFilmfestIntresetMemberURL() {
		return toFilmfestIntresetMemberURL;
	}

	private @Value("${pointByFreeBackCommentCountURL}")
	String pointByFreeBackCommentCountURL;

	public String getPointByFreeBackCommentCountURL() {
		return pointByFreeBackCommentCountURL;
	}

	@Value("${commentCountByTransferURL}")
	private String commentCountByTransferURL;

	public String getCommentCountByTransferURL() {
		return commentCountByTransferURL;
	}

	@Value("${commentListByTransferURL}")
	private String commentListByTransferURL;

	public String getCommentListByTransferURL() {
		return commentListByTransferURL;
	}
	@Value("${topAddMemberUrl}")
	private String topAddMemberUrl;
	public String getTopAddMemberUrl() {
		return topAddMemberUrl;
	}
	// 根据日期区间、影片ID，获取影片每天的哇啦数
	@Value("${countByMovieIdAddDateUrl}")
	private String countByMovieIdAddDateUrl;
	public String getCountByMovieIdAddDateUrl() {
		return countByMovieIdAddDateUrl;
	}
}
