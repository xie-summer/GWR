package com.gewara.service.member;

import java.util.List;

import com.gewara.model.user.Treasure;


/**
 * Service for News，DiscountInfo，Bulletin
 * @author <a href="mailto:acerge@163.com">gebiao(acerge)</a>
 * @since 2007-9-28下午02:05:17
 */
public interface TreasureService {
	/**
	 * 判断Treasure是否存在（根据memberId，tag，relatedid查询）
	 * @param treasure
	 * @return
	 */
	boolean isExistsTreasure(Treasure treasure);
	/**
	 * 查找某用户的收藏
	 * @param memberId
	 * @param string
	 * @return
	 */
	List<Long> getTreasureIdList(Long memberId, String tag, String action);
	List<Long> getTreasureIdList(Long memberId, String tag, String action, int from, int maxnum);
	/**
	 * 获取同一的动作的人
	 * @param action
	 * @param tag
	 * @param relatedid
	 * @param from
	 * @param maxnum
	 * @return
	 */
	List<Long> getTreasureMemberList(String action, String tag, Long relatedid, int from, int maxnum);
	List<Long> getTreasureMemberList(String action, String tag, Long relatedid, String order, boolean asc, int from, int maxnum);
	List<Treasure> getTreasureList(String action, Long memberid, String tag, Long relatedid, int from, int maxnum);
	Treasure getTreasureByTagMemberidRelatedid(String tag, Long memberid, Long relatedid, String action);
	void saveTreasure(Long memberid, String tag, Long relatedid, String action);
}
