package com.gewara.service.bbs;

import java.util.List;
import java.util.Map;



/**
 * 2012-12-10 下午7:18:14
 * @author bob
 * @desc Mongo专题投票通用
 */
public interface CommonVoteService {
	
	/**
	 *  del
	 * */
	void delVote(String id);
	
	/**
	 * 2012-12-10 下午7:19:27
	 * @author bob
	 * @desc tag + memberid + itemid = 唯一确认一条投票记录
	 */
	Map<String, Object> getSingleVote(String tag, Long memberid, String itemid);
	void addVoteMap(String tag, String itemid, Long memberid, String flag);
	
	/**
	 * 2012-12-10 下午7:20:53
	 * @author bob
	 * @desc tag + itemidList = 参与投票的所有项目 
	 */
	List<Map> getItemVoteList(String flag);
	
	/**
	 * 2012-12-11 上午11:07:11
	 * @author bob
	 * @desc (造假)对某item增加投票量
	 * flag = 当前tag+"virtual"
	 */
	void addCommonVote(String flag, String itemid, Integer support);
	/**
	 * 2012-12-11 下午1:39:54
	 * @author bob
	 * @desc 查找当前项目的投票值 
	 */
	Integer getSupportCount(String flag, String itemid);
	
	//获取投票用户
	List<Map> getVoteInfo(String tag, int from, int maxnum);
	int getVoteInfoCount(String tag);
}
