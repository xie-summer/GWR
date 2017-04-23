package com.gewara.service.drama;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.gewara.model.drama.Drama;
import com.gewara.model.drama.OpenDramaItem;

public interface DramaService {

	/**
	 * 热门话剧
	 */
	List<Drama> getHotDrama(String citycode, String order,int from,int maxnum);
	/**
	 * 正在举行的话剧
	 * @param from
	 * @param maxnum
	 * @return
	 */
	List<Long> getNowDramaList(String citycode, int from, int maxnum);
	
	/**
	 * 查询正在举行话剧的数量
	 * @return
	 */
	Integer getNowDramaCount(String citycode);
	/**
	 * 根据dramaname查询话剧信息
	 */
	List<Drama> getDramaListByName(String citycode, String name,int from,int maxnum);
	
	/**
	 * 根据dramaid查询OpenDramaItem数据
	 * @param dramaid
	 * @return
	 */
	List<OpenDramaItem> getOpenDramaItemListBydramaid(String citycode, Long dramaid);
	List<OpenDramaItem> getOpenDramaItemListBydramaid(String citycode, Long dramaid, Long theatreid, Boolean isOpenPartner);
	/**
	 * 正在上演的话剧
	 * @param fromDate
	 * @param from
	 * @param maxnum
	 * @return
	 */
	List<Drama> getCurDramaList(String citycode, Date fromDate, String order, int from, int maxnum);
	List<Drama> getCurDramaList(Long theatreid, String order, int from, int maxnum);
	List<Drama> getCurPlayDramaList(Long theatreid, int from, int maxnum);
	/**
	 *正在上演数量
	 * @param theatreid
	 * @return
	 */
	Integer getCurPlayDramaCount(Long theatreid);

	/**
	 * 即将上演
	 * @param fromDate
	 * @param from
	 * @param maxnum
	 * @return
	 */
	List<Drama> getFutureDramaList(String citycode, Date fromDate, int from, int maxnum);
	
	List<Drama> getCurDramaList(String citycode, String orderField);
	
	Map<String/*yyyy-MM-dd*/,Integer> getMonthDramaCountGroupPlaydate(String citycode, Date playdate);
	
	List<Drama> getCurDramaByDate(Timestamp curtime, String citycode, String order, int from, int maxnum);
	/**
	 * @param citycode 城市代码
	 * @param fyrq 1.正在售票，2.正在上演，6.即将上演，8.往前剧目7.其它类型的剧目
	 * @param type 话剧类型（喜剧、爱情等）
	 * @param order 排序字段
	 * @param dramatype 剧目类型（drama话剧、other其它演出）
	 * @param searchkey 搜索关键字
	 * @return
	 */
	List<Drama> getDramaList(String citycode, String fyrq, String type, String order, String dramatype, String searchkey,int from,int maxnum);
	Integer getDramaListCount(String citycode, String fyrq, String type, String order, String dramatype, String searchkey);

	/**
	 * 来自BaseDramaController
	 * @param citycode
	 * @param dramaid
	 * @param theatreid
	 * @param itemid
	 * @param isPartner
	 * @param starttime
	 * @param endtime
	 * @return
	 */
	List<Drama> getDramaListByTroupeCompany(String citycode, Timestamp lasttime, String troupecompany);
	
	/**
	 *	动态查询演出类型 
	 */
	List<String> getDramaTypeList(String citycode);
	
	/**
	 * 获取上映开始日期在30天内的正在售票的关注人数最多的剧目
	 * @param citycode
	 * @param isPartner
	 * @param maxnum
	 * @return
	 */
	public List<Drama> getDramaListByMonthOpenDramaItem(String citycode, boolean isPartner, int maxnum);
	/**
	 * 最近开启售票的剧目,根据售票场次开放时间取最近开售
	 * @param citycode
	 * @param max
	 * @return
	 */
	public List<Drama> getDramaListLastOpenTime(String citycode, int from, int maxnum);
	
	Date getDramaMinMonthDate(String citycode);
	
	Map<String, Integer> getMonthDramaCount(String citycode, Date playdate);
	
	List<Drama> getCurDramaByDate(String citycode, Date playdate, String order, int from, int maxnum);
	
	/**
	 * 获取评论列表页的剧目
	 * @param citycode
	 * @param searchKey
	 * @param fromDate
	 * @param from
	 * @param maxnum
	 * @return
	 */
	public List<Drama> getDramaListByName(String citycode,String searchKey,Timestamp fromDate, String orderField, boolean asc, int from, int maxnum);
	public Integer getDramaCountByName(String citycode,String searchKey,Timestamp fromDate);
	
	List<OpenDramaItem> getBookingOdiList(String citycode, Date playdate, Long dramaid, Long theatreid);
}
