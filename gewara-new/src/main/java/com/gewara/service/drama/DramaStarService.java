package com.gewara.service.drama;

import java.sql.Timestamp;
import java.util.List;
import java.util.Map;

import com.gewara.model.drama.DramaStar;


/**
 *    @function 话剧明星Service 
 * 	@author bob.hu
 *		@date	2010-12-03 10:39:50
 */
public interface DramaStarService {

	/**
	 *  明星列表 - 暂时无条件
	 *  order - 按神马字段排序
	 *  property - 按产品部需求查询, 目前传null
	 *  type - 明星/剧团
	 */
	List<DramaStar> getStarList(String order, String property, String startype, int from, int maxnum);
	List<DramaStar> getDramaStarList(String order, String startype, String searchkey, String troupecompany, int from, int maxnum, String...notNullPropertys);
	/**
	 * 通过troup 分组以数量倒序
	 * @param startype
	 * @param from
	 * @param maxnum
	 * @return
	 */
	List<DramaStar> getDramaStarListGroupTroupe(String startype, String searchKey, int from, int maxnum);
	
	Integer getStarCount(String startype,String searchkey, String...notNullPropertys);
	
	/**
	 * 根据state + name 查找
	 * state: 州名
	 * name: 明星名(支持like)
	 */
	List<DramaStar> getStarListByStateAndName(String state, String name, String type, int from, int maxnum);
	Integer getStarCountByStateAndName(String state, String name, String type);
	
	/**
	 *  根据明星ID(剧团ID)匹配剧团/明星
	 */
	List<DramaStar> getStarListByTroupid(Long starid, String orderField, boolean asc, int from, int maxnum);
	Integer getStarCountByTroupid(Long starid);
	
	/**
	 *  取得某明星的 演出数/新闻数/图片数/视频数
	 *  params: List<DramaStar>
	 *  return: Map<Long, Map<String, Integer>>
	 *  page:	eg.	dataMap.get(starid).get("newCount")
	 */
	Map getFavStarListProperty(String citycode, List<DramaStar> starlist);
	
	
	
	/*****
	 *  后台查询 - 根据类型 + name 模糊匹配
	 * */
	List<DramaStar> getStarListByName(String type, String name, String startype, int from, int maxnum);
	DramaStar getDramaStarByName(String name, String startype);
	int getStarCountByName(String type, String name, String startype);
	
	/**
	 * 查询谋剧社关联明星
	 * Long troupe 剧团ID
	 * starType
	 */
	public List<DramaStar> getDramaStarListByTroupe(String type, Long troupe, String starType, int from, int maxnum, String...notNullPropertys);
	/**
	 * 查询最新加入明星（待同步）
	 * Timestamp lasttime 上次同步时间
	 */
	List<DramaStar> getSynchStarList(Timestamp lasttime);
}
