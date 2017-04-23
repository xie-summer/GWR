/**
 * 
 */
package com.gewara.service.member;

import java.util.List;
import java.util.Map;

import com.gewara.model.user.Friend;
import com.gewara.model.user.FriendInfo;
import com.gewara.model.user.HiddenMember;
import com.gewara.model.user.Member;
/**
 * @author hxs(ncng_2006@hotmail.com)
 * @since Jan 27, 2010 12:18:48 PM
 */
public interface FriendService {
	/**
	 * 根据memberid查询朋友Id
	 */
	List<Long> getFriendIdList(Long memberid, int from, int maxnum);
	/**
	 * 朋友数，去除黑名单
	 * @param memberid
	 * @return
	 */
	Integer getFriendCount(Long memberid);
	/**
	 * 根据memberfrom,memberto查询是否已经发送加好好友邀请
	 */
	boolean isInvitedFriend(Long memberidfrom, Long memberidto);
	/**
	 * 根据memberfrom,memberto查询是否已经好友
	 */
	boolean isFriend(Long memberidfrom, Long memberidto);
	
	/**
	 * 根据memberid查询朋友们id列表
	 * @return
	 */
	List<Friend> getFriendList(Long memberid, int from, int manxnum);

	/**
	 * 根据memberid和commuid删除圈子成员
	 */
	void deleteCommueMember(Long memberid,Long commuid);
	/**
	 * 判断当前用户是否有权限查看当前好友的信息
	 * @return
	 */
	Map isPrivate(Long memberid);
	Integer getNotJoinCommuFriendCount(Long memberid, Long commuid);
	List<Long> getNotJoinCommuFriendIdList(Long memberid, Long commuid, int from, int maxnum);
	/**
	 * 查询添加好友
	 * @param memberid
	 * @return
	 */
	List<HiddenMember> getHiddenMemberListByMemberid(Long memberid);
	/**
	 * 根据memberid, addmemberid查询friendInfoList
	 * @param memberid
	 * @return
	 */
	List<FriendInfo> getFriendInfoListByAddMemberidAndMemberid(Long addmemberid, Long memberid);
	
	/**
	 * 判断用户通过email，msg邀请的用户是否已经记录过
	 */
	boolean isExistsEmail(Long memberid,String email);
	
	/**
	 * 通过用户昵称检查用户是否存在
	 */
	Member checkUserName(String nickname);

	void deleteFriend(Long memberid1, Long memberid2);
	List<Member> getFriendMemberList(Long memberid, int from, int maxnum);
}
