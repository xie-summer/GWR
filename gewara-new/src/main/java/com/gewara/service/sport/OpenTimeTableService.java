/**
 * 
 */
package com.gewara.service.sport;

import java.util.Date;
import java.util.List;
import java.util.Map;

import com.gewara.model.acl.User;
import com.gewara.model.sport.OpenTimeItem;
import com.gewara.model.sport.OpenTimeTable;
import com.gewara.model.sport.ProgramItemTime;
import com.gewara.model.sport.Sport2Item;
import com.gewara.model.sport.SportProfile;
import com.gewara.support.ErrorCode;

public interface OpenTimeTableService {
	Integer getOpenTimeTableCount(Long sportid, Long itemid, Date startdate, Date enddate, String openType);
	List<OpenTimeTable> getOpenTimeTableList(Long sportid, Long itemid, Date startdate, Date enddate, String openType);
	List<OpenTimeTable> getOpenTimeTableList(Long sportid, Long itemid, Date startdate, Date enddate, String openType, boolean isBooking, int from, int maxnum) ;
	List<OpenTimeTable> getOpenTimeTableList(Long sportid, Long itemid, Date playdate, String openType);
	List<OpenTimeTable> getOpenTimeTableList(Long sportid, Long itemid, Date playdate, String openType, int from, int maxnum);
	
	/**
	 * 某个运动项目某时间内开放预订的场馆和场次数
	 * @param itemid
	 * @param startPlaydate
	 * @param endPlaydate
	 * @return
	 */
	List<Map<Long,Long>> getOpenTimeTableSportList(String citycode,Long itemid, Date startPlaydate,Date endPlaydate);
	/**
	 * 场次价格
	 * @param ottid
	 * @return
	 */
	List<Integer> getTimeItemPrice(Long ottid);
	Integer getbookingSportCount(Date startdate, String citycode) ;
	List<Map> getOpenTimeCountByItemid(Long itemid, Long... sportidList);
	List<Map> getOpenTimeCountByItemid(Long itemid, Date date, Long... sportIdArray);
	//根据条件得到开放类型
	List<String> getOpenTimeTableOpenTypeList(Long sportid, Long itemid, Date startdate, Date enddate, boolean isBooking);
	
	ErrorCode<ProgramItemTime> saveOrUpdateProgramItem(Long id, Long sportid, Long itemid, Integer week, String fieldids, Map dataMap, User user, String citycode);
	ErrorCode<String> programItemTime(ProgramItemTime programItemTime, int week);
	ErrorCode<String> batchProgramItemTime();
	ErrorCode<String> batchProgramItemTime(Sport2Item sport2Item);
	void clearOttPreferential(OpenTimeTable ott, SportProfile sp);
	List<Long> getCurOttSportIdList(Long itemid, String citycode);
	/**
	 * 根据场次获取场地图
	 * @param ottid
	 * @return
	 */
	List<OpenTimeItem> getOpenItemList(Long ottid);
	/**
	 * 根据运动预订排期表
	 * @param ottid
	 * @return
	 */
	List<OpenTimeTable> getOttList(Long sportid, Long itemid, String playdate);
	/**
	 * 更新运动场次卖出数量，提供给报表用
	 * @param ott	场次数据
	 */
	void updateOpenTimeTable(OpenTimeTable ott);
	
	/**
	 * 更新运动场次统计数理
	 * @param ott	场次数据
	 */
	void updateOttOtherData(OpenTimeTable ott);
	
	void refreshOpenTimeSale(List<OpenTimeItem> itemList, Integer dupprice, List<String> msgList);
}
