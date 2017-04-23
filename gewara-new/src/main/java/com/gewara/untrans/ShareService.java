package com.gewara.untrans;

import java.sql.Timestamp;
import java.util.List;

import com.gewara.model.bbs.LinkShare;
import com.gewara.model.user.Member;
import com.gewara.model.user.ShareMember;



public interface ShareService {

	/**
	 * 把内容分享到新浪微博,腾讯微博（内容可能包括：帖子，购票，电影影评，话剧剧评，生活安排，活动）
	 */
	void sendMicroInfo(LinkShare linkShare);
	/**
	 * 添加要分享的信息
	 */
	void sendShareInfo(String tag,Long tagid,Long memberid,String category);
	LinkShare addShareInfo(String tag,Long tagid,Long memberid,String category,String type);
	
	//自定义分享内容
	void sendShareInfo(String tag, Long tagid, Long memberid, String content, String picUrl);
	void sendCustomInfo(LinkShare linkShare);
	LinkShare addShareInfo(String tag, Long tagid, Long memberid, String type, String content, String picUrl);
	//得到新浪关注人的名称
	List<String> getSinaFriendList(Long memberid, int count);
	
	//用户绑定微博信息
	ShareMember getShareMemberByLoginname(String source, String loginname);
	
	//根据来源获取关联同步用户
	List<ShareMember> getShareMemberByMemberid(List<String> source, Long memberid);
	void createShareMember(Member member, String source, String loginname, String token, String tokensecret, String expires);
	void updateShareMemberRights(ShareMember shareMember);
	
	/**
	 * 根据条件查询分享的微博
	 * 
	 * */
	List searchShareSinaHisList(Timestamp starttime,Timestamp endtime,String status,String shareType,int from,int maxNum);
	
	int searchShareCount(Timestamp starttime,Timestamp endtime,String status,String shareType);
}
