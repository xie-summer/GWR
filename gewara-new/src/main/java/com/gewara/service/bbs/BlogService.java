package com.gewara.service.bbs;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.gewara.model.bbs.Accusation;
import com.gewara.model.bbs.Bkmember;
import com.gewara.model.bbs.BlackMember;
import com.gewara.model.bbs.BlogDataEveryDay;
import com.gewara.model.bbs.commu.Commu;
import com.gewara.model.user.Member;
import com.gewara.model.user.Treasure;
import com.gewara.untrans.monitor.ConfigTrigger;

/**
 * @author <a href="mailto:acerge@163.com">gebiao(acerge)</a>
 * @since 2007-9-28下午02:05:17
 */
public interface BlogService extends ConfigTrigger{
	/**
	 * @param tag
	 * @param relatedid
	 * @param role
	 * @param includeSub
	 * @param from
	 * @param maxnum
	 * @return 获取本版及子板中角色大于role的成员(includeSub=true,则包括子版块)
	 */
	List<Bkmember> getBkmemberList(String tag, Long relatedid, int role,
			boolean includeSub, int from, int maxnum);
	/**
	 * 获取某会员的BkmemberList
	 * @param memberId
	 * @return
	 */
	List<Bkmember> getBkmemberListByMemberId(Long memberId);
	/**
	 * @param tag
	 * @param relatedid
	 * @return 版主列表
	 */
	List<Bkmember> getBanzhuList(String tag, Long relatedid);
	/**
	 * 获取用户在此板块的权限
	 * @param tag
	 * @param relatedid
	 * @return
	 */
	int getMaxRights(String tag, Long relatedid, Long ownerId, Member member);
	/**
	 * 在Member中查找bkmember
	 * @param member
	 * @param tag
	 * @param relatedid
	 * @return
	 */
	Bkmember getBkmember(Member member, String tag, Long relatedid);
	/**
	 * 获取黑名单列表
	 * @param memberId 为空则返回所有黑名单
	 * @return
	 */
	List<BlackMember> getBlackMemberList(Long memberId, int from, int maxnum);
	
    List<BlackMember> getBlackMemberList(Long memberId,String nickname, int from, int maxnum);
	/**
	 * 用户是否在黑名单中
	 * @param memberId
	 * @return
	 */
	boolean isBlackMember(Long memberId);
	/**
	 * 黑名单中的人数
	 */
	Integer getBlackMembertCount(String nickname);
	List<Commu> getCommunityList(String order,int from,int maxnum);
	Integer getCommunityCount();
	/**
	 * 举报数量
	 * @return
	 */
	Integer getAccusationCount();
	List<Accusation> getAccusationList(int from, int maxnum);
	String filterContentKey(String html);
	String filterAllKey(String html);
	boolean rebuildFilterKey();
	boolean rebuildManualFilterKey();
	boolean rebuildMemberRegisterFilterKey();
	
	void rebuildAllFilterKeys();
	/**************************************************************************
	 * 评论开始
	 **************************************************************************/
	/**
	 * 获取关注信息(我关注的人的ID)(WAP)
	 */
	List<Long> getTreasureRelatedidList(String citycode, Long memberid, String tag, String action);
	/**
	 * 是否重复关注该用户
	 */
	boolean isTreasureMember(Long fromMemberid, Long toMemberid);
	/**
	 * 取消关注信息
	 */
	boolean cancelTreasure(Long fromMemberid,Long toMemberid,String tag,String action);
	
	/**
	 * 查询用户的粉丝信息
	 */
	List<Long> getFanidListByMemberId(Long memberid,int from,int maxnum);
	/**
	 * 根据memberid查询关注信息
	 */
	List<Treasure> getTreasureListByMemberId(Long memberId, String[] tag,String[] removieTag, Long relatedid, int from,int maxnum, String... action);
	Integer getTreasureCountByMemberId(Long memberId, String[] tag,String[] removieTag, String... action);
	/**
	 * 根据memberid,relateid, tag查询treasure
	 * @param memberid
	 * @param relateid
	 * @param tag
	 * @return
	 */
	Treasure getTreasure(Long memberid, Long relateid, String tag, String action);
	/**
	 * 查询我的标签
	 * @param memberid
	 * @return
	 */
	List<Treasure> getTreasureListByMemberId(Long memberid, int from ,int maxnum);
	/**
	 * @param diaryid
	 * @return
	 */
	String getDiaryBody(long diaryid);
	/**
	 * @param diaryid
	 * @param body
	 */
	void saveDiaryBody(long diaryid, Timestamp updatetime, String body);
	List<Long> getTreasureListByMemberIdList(Long relatedid, String tag, int from, int maxnum, String action);
	boolean isNight();
	boolean allowAddContent(String flag, Long memberid);
	List<Long> getTreasureCinemaidList(String citycode, Long memberid, String action);
	
	void addBlogData(Long userid, String tag, Long relatedid);
	void saveOrUpdateBlogData(Long userid, String tag, Long relatedid, Map<String/*propertyname*/,Integer> keyValueMap);
	void saveOrUpdateBlogDateEveryDay(Long userid, String tag, Long relatedid, String blogtype, Date blogdate, int blogcount);
	
	List<Map> getDiaryMapList(Timestamp starttime, Timestamp endtime);
	
	BlogDataEveryDay getBlogDataEveryDay(String tag, Long relatedid, String blogtype, Date blogdate);
	
	List<Long> getIdListBlogDataByTag(String citycode, String tag, String searchName, String searchKey, boolean asc, String order, int from, int maxnum);
	Integer getIdCountBlogDataByTag(String citycode, String tag, String searchName, String searchKey);
	
	Integer getIdCountEveryDayByTag(String citycode, String tag, String searchName, String searchKey, String blogtype, Date startdate, Date enddate);
	List<Long> getIdListEveryDayByTag(String citycode, String tag, String searchName, String searchKey, String blogtype, Date startdate, Date enddate, int from, int maxnum);
	/**
	 * 检测是否恶意用户
	 * @param member
	 * @return
	 */
	Integer isBadEgg(Member member);
}
