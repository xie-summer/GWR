/**
 * 
 */
package com.gewara.service.member;

import java.util.List;

import com.gewara.model.user.FavoriteTag;


/**
 *  @function 兴趣标签Service
 * 	@author bob.hu
 *	@date	2011-02-22 18:12:14
 */
public interface FavoriteTagService {
 	/**
	 * 随机查询
	 * */
	List<FavoriteTag> getRandomFavorList(int count);
	
	/**
	 *  根据tag 设置FavoriteTag.count++ 
	 * */
	void updateFavoriteTagCount(String tag);
}
