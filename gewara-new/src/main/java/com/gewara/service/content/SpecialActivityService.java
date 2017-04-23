package com.gewara.service.content;

import java.util.List;

import com.gewara.model.movie.SpecialActivity;



/**
 *  @function 专题列表
 * 	@author bob.hu
 *	@date	2011-05-31 15:12:08
 */
public interface SpecialActivityService {
	List<SpecialActivity> getSpecialActivityList(String status, String flag, String relatedid, int from, int maxnum);
	int getSpecialActivityCount(String status, String flag, String relatedid);	
	/**
	 * 根据flag在范围内查找活动，flag对应的不止一个
	 * @param status
	 * @param flag
	 * @param relatedid
	 * @param searchKey
	 * @param from
	 * @param maxnum
	 * @return
	 */
	List<SpecialActivity> getSpecialActivityList(String status, String flag, String relatedid, String searchKey, int from, int maxnum);
	int getSpecialActivityCount(String status, String flag, String relatedid, String searchKey);
}
