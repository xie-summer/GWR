package com.gewara.service.drama;

import java.util.List;

import com.gewara.model.drama.Drama;
import com.gewara.model.drama.DramaStar;
import com.gewara.model.drama.DramaToStar;

/**
 *    @function 话剧明星 关系 
 * 	@author bob.hu
 *		@date	2010-12-08 10:16:18
 */
public interface DramaToStarService {

	/**
	 *	保存话剧明星关系 (starids 为多个明星的ID)
	 */
	void saveDramaToStar(Long dramaid, String starids);
	
	/**
	 *  查询某话剧关联的明星
	 */
	List<DramaStar> getDramaStarListByDramaid(Long dramaid, String starType, int from, int maxnum, String...notNullPropertys);
	
	/**
	 *  查询某明星关联的话剧
	 *  starid : 明星ID
	 *  isCurrent: 是否正在上映
	 */
	List<Drama> getDramaListByStarid(Long starid);
	List<Drama> getDramaListByStarid(Long starid, boolean isCurrent, int from, int maxnum);
	Integer getDramaCountByStarid(Long starid);
	Integer getDramaCountByStarid(Long starid, boolean isCurrent);

	/**
	 *	根据话剧ID取得关联到Star的  DramaToStar List
	 */
	List<DramaToStar> getDramaToStarListByDramaid(String type, Long dramaid, boolean isGtZero);
	Integer getStarCount(Long relatedid, Long starid);
}
