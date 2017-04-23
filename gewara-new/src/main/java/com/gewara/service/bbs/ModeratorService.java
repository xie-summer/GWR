package com.gewara.service.bbs;

import java.util.List;
import java.util.Map;

import com.gewara.model.bbs.Moderator;


public interface ModeratorService {
	/**
	 * 查询哇啦话题列表
	 */
	List<Moderator> getModeratorList(String type, List showaddress, String mstatus, int from,int maxnum);
	Integer getModeratorCount(String type, Long memberid);
	
	/**
	 * 查询话题信息
	 */
	List<Moderator> getModeratorByType(Integer showAddress,String type);
	List<Moderator> getModeratorByType(Integer showAddress,String type, int from, int maxnum, boolean isRule);
	
	/**
	 * 查询用户关注的话题信息(哇啦)
	 */
	List<Moderator> getModeratorList(Long memberid,String type,int from ,int maxnum);
	/**
	 *  @function 从缓存中取得数据(同时更新数据库)
	 * 	@author bob.hu
	 *	@date	2011-12-02 17:46:23
	 */
	List<Map> updateHotModeratorFromCache(int from,int max);
	
}
