/**
 * 
 */
package com.gewara.service.bbs;

import java.util.Date;
import java.util.List;
import java.util.Map;

import com.gewara.model.bbs.Diary;
import com.gewara.model.bbs.DiaryBase;
import com.gewara.model.bbs.commu.Commu;
import com.gewara.model.bbs.commu.CommuCard;
import com.gewara.model.bbs.commu.CommuManage;
import com.gewara.model.bbs.commu.CommuMember;
import com.gewara.model.bbs.commu.VisitCommuRecord;
import com.gewara.model.user.Album;
import com.gewara.model.user.Member;


/**
 * @author chenhao(sky_stefanie@hotmail.com)
 */
public interface CommuService {
	/**
	 * 根据圈子id查询圈子帖子信息
	 */
	<T extends DiaryBase> List<T> getCommuDiaryListById(Class<T> clazz, Long id, String[] type,Long commuTopicId, int from, int maxnum);
	
	/**
	 *  根据搜索条件查询帖子
	 */
	<T extends DiaryBase> List<T> getCommuDiaryListBySearch(Class<T> clazz, Long id, String type, Long commuTopicId, Date fromDate, Integer flag, String text, int from, int maxnum);
	<T extends DiaryBase> Integer getCommuDiaryCountBySearch(Class<T> clazz, Long id,String type,Long commuTopicId, Date fromDate, Integer flag, String text);
	
	/**
	 * 根据圈子id查询圈子帖子信息数量
	 */
	<T extends DiaryBase> Integer getCommuDiaryCount(Class<T> clazz, Long id,String[] type,Long commuTopicId);
	
	/**
	 * 根据圈子id查询圈子人员信息
	 */
	List<CommuMember> getCommuMemberById(Long id, Long adminid, Long subadminid, String blackmember, int from, int maxnum);
	/**
	 * 获取最新加入圈子的用户
	 */
	List<CommuMember> getCommuMemberByCommu(String tag, Long relatedid, Long adminid, Long subadminid, String blackmember, int from, int maxnum);
	/**
	 * 根据圈子id查询圈子人员信息数量
	 */
	Integer getCommumemberCount(Long id, Long adminid);
 
	/**
	 * 根据当前用户id查询他加入的所有圈子的话题信息
	 */
	List<Diary> getAllCommuDiaryById(Long id, int from, int maxnum);
	
	

	/**
	 * 根据当前用户id查询他加入的所有圈子的话题信息数量
	 */
	Integer getAllCommuDiaryCountById(Long id);
	
	
	
	/**
	 * 根据当前用户id查询他加入的所有圈子的成员信息
	 */
	List<Member> getAllCommuMemberById(Long id, int from, int maxnum);
	
	/**
	 * 根据当前用户id查询他加入的所有圈子的成员信息数量
	 */
	Integer getAllCommuMemberCountById(Long id);
	
	
	
	/**
	 * 分类查询圈子大类别信息
	 */
	List<Map> getCommuType();
	
	/**
	 * 根据圈子大类别的tag查询圈子小类别的数量信息
	 */
	List<Map> getCommuSmallByTag(String tag);
	
	

	/**
	 * 判段是否是圈子成员
	 */
	boolean isCommuMember(Long commuid, Long memberid);
	
	List<Commu> getCommunityListByMemberid(Long memberid, int from, int maxnum);

	List<Commu> getCommunityListByHotvalue(String tag, Long relatedid, boolean memberNum, Long hotvalue, int from, int maxnum);

	List<Commu> getCommunityListByHotvalue(Long hotvalue, int from, int maxnum);
	/**
	 * 根据commuid查询加入的所有圈子的成员memberid
	 */
	List<Long> getCommuMemberIdListByCommuId(Long commuid);
	/**
	 * 热门圈子信息
	 */
	List<Commu> getHotCommuList(int from, int maxnum);
	/**
	 * 根据当前用户查询加入的所有圈子信息
	 */
	List<Commu> getCommuListByMemberId(Long memberid, int from, int maxnum);
	
	/**
	 * 根据当前用户查询加入的所有圈子信息数量
	 */
	Integer getCommuCountByMemberId(Long memberid);
	
	/**
	 * 查询当前圈子的成员也喜欢去的圈子信息
	 */
	List<Commu> getCommuMemberLoveToCommuList(Long commuid, int from, int maxnum);
	/**
	 * 根据用户id查询其加入的圈子的相册列表
	 */
	List<Album> getJoinedCommuAlbumList(Long id, int from, int maxnum);
	/**
	 * 根据用户id查询其加入的圈子的相册数量
	 */
	Integer getJoinedCommuAlbumCount(Long memberid);
	
	/**
	 * 根据圈子id查询圈子相册信息
	 */
	List<Album> getCommuAlbumById(Long id, int from, int maxnum);
	/**
	 * 根据圈子id查询圈子相册数量
	 */
	Integer getCommuAlbumCountById(Long id);

	/**
	 *  根据圈子ID 查询当前圈子总共的图片数量
	 */
	Integer getPictureCountByCommuid(Long commuid);
	
	/**
	 * 加入圈子
	 */
	void joinCommuMember(Long memberid,Long commuid);
	
	/**
	 * 判断用户是否已经加入了他想要加入的圈子
	 */
	boolean isJoinCommuMember(Long memberid,Long commuid);
	
	/**
	 * 查询圈子信息
	 */
	List<Commu> getCommuBySearch(String tag, String citycode,Long smallcategoryid,String value,String sort, String countycode, int from, int maxnum);
	
	/**
	 * 查询圈子的人数
	 */
	Integer getCommuCountBySearch(String tag, String citycode,Long smallcategoryid,String value,String sort, String countycode);
	/**
	 * 查询最新圈子
	 * @param from
	 * @param maxnum
	 * @return
	 */
	List<Commu> getCommuList(int from, int maxnum);
	/**
	 * 根据commuid,memberid查询CommuCard
	 * @param memberid
	 * @param commuid
	 * @return
	 */
	CommuCard getCommuCardByCommuidAndMemberid(Long memberid, Long commuid);
	/**
	 * 根据memberid,commuid查询成员访问圈子的次数、和时间
	 * @param memberid
	 * @param commuid
	 * @return
	 */
	VisitCommuRecord getVisitCommuRecordByCommuidAndMemberid(Long commuid, Long memberid);
	/**
	 * 根据memberid,commuid查询圈子成员
	 * @param memberid
	 * @param commuid
	 * @return
	 */
	CommuMember getCommuMemberByMemberidAndCommuid(Long memberid, Long commuid);
	
	/**
	 * 判断圈子名称是否重复
	 */
	boolean isExistCommuName(Long commuid,String communame);
	/**
	 * 根据memberid, date来查询date时间是否已经访问圈子
	 * @param memberid
	 * @param date
	 * @return
	 */
	boolean isHadVisitCommuByMemberidAndDate(Long memberid ,String date);
	
	/**
	 * 根据圈子relatedid查询圈子列表，按圈子成员排序
	 * @param tag
	 * @param relatedid
	 * @param from
	 * @param maxnum
	 * @return
	 */
	List<Commu> getCommuListByTagAndRelatedid(String citycode, String tag, Long relatedid, int from, int maxnum);
	
	/**
	 *  根据 commuid,  匹配 CommuManage, 查询状态
	 *  调用时 Assert.notNull()
	 */
	String getCheckStatusByIDAndMemID(Long commuid);
	
	void initCommuRelate(List<Commu> commuList);
	
	/**
	 * 是否是圈子管理员
	 * @param commuid
	 * @param memberid
	 * @return
	 */
	boolean isCommuAdminByMemberid(Long commuid, Long memberid);
	/**
	 * 根据countycode查询圈子数量
	 * @param countycode
	 * @return
	 */
	Integer getCommuCountByCountycode(String countycode);
	/**
	 * 根据commuid查询圈子话题数量
	 * @param commuid
	 * @return
	 */
	Integer getCommuDiaryCountByCommuid(Long commuid);
	
	/**
	 *  根据圈子ID 查找对应的commuManage
	 */
	CommuManage getCommuManageByCommuid(Long commuid);
	
	/**
	 * @param memberid
	 * @param from
	 * @param maxnum
	 * @return Map<friendid, commu>
	 */
	Map<Long, Commu> getFriendCommuMap(Long memberid, int from, int maxnum);
	Integer getFriendCommuCount(Long memberid);

	/**
	 * 根据hotvalue查询圈子数量
	 * @param hotvalue
	 * @return
	 */
	Integer getCommunityCountByHotvalue(Long hotvalue);
	List<CommuMember> getCommuMemberListByMemberid(Long memberid, int from, int maxnum);
	List<CommuCard> getCommuCardListByMemberid(Long memberid, int from, int maxnum);
	
	/**
	 * 查找拥有的圈子
	 * @param memberid
	 * @return
	 */
	List<Commu> getOwnerCommuList(Long memberid);
/**
	 * 查询memberid管理的圈子
	 * @param memberid
	 * @return
	 */
	List<Commu> getManagedCommuList(Long memberid);

	/**
	 * 加入此圈子的成员还加入哪些圈子？
	 * @param commuid
	 * @param from
	 * @param maxnum
	 * @return
	 */
	List<Commu> getAlikeCommuList(Long commuid, int from, int maxnum);
	/**
	 * @param tag
	 * @param from
	 * @param maxnum
	 * @return 根据增加时间排序
	 */
	List<Commu> getCommunityListByTag(String tag, String order, int from, int maxnum);
	Integer getCommunityCountByTag(String tag);
	
	/**
	 * 查询圈子列表按属性排序
	 */
	List<Commu> getCommuListOrderByProperty(String tag ,int from, int maxnum, String order);
	
	/**
	 * @param tag
	 * @param relatedId
	 * @param from
	 * @param maxnum
	 * @return 根据增加时间排序
	 */
	List<Commu> getCommunityListByRelatedId(String tag, Long relatedid, int from, int maxnum);

	Integer getCommunityCountByRelatedId(String tag, Long relatedid);

	/**
	 * 设置圈子的关联
	 * @param communityList
	 */
	void initCommunityRelate(List<Commu> communityList);
	/**
	 * 根据标签tag得到圈子的id
	 * @param tag
	 * @return
	 */
	List<Long> getCommuIdByTag(String tag);

}
