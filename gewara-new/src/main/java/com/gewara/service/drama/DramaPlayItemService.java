package com.gewara.service.drama;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.gewara.model.drama.DisQuantity;
import com.gewara.model.drama.Drama;
import com.gewara.model.drama.DramaPlayItem;
import com.gewara.model.drama.Theatre;
import com.gewara.model.drama.TheatreField;
import com.gewara.model.drama.TheatreRoom;
import com.gewara.model.drama.TheatreSeatPrice;
import com.gewara.support.ErrorCode;

public interface DramaPlayItemService{
	
	Drama getDramaByName(String dramaName);
	
	List<Drama> getDramaListByName(String dramaName);
	/**
	 * 保存场次
	 * @param item
	 * @return
	 */
	ErrorCode saveDramaPlayItem(DramaPlayItem item, Long userid);
	/**
	 * 删除场次
	 * @param item
	 * @return
	 */
	ErrorCode removieDramaPlayItem(Long id);
	/**
	 * 复制排片
	 * @param item
	 * @param playdate
	 * @return
	 */
	ErrorCode saveDramaPlayItem(DramaPlayItem item, String playdates, String rooms) throws Exception;
	/**
	 * 某个话剧院从开始日期
	 * @param theatreid
	 * @param starttime
	 * @return 获取场次日期对应的数量
	 */
	List<Map> getDateCount(Long theatreid, Timestamp starttime);
	/**
	 * 获取场次列表
	 * @param theatreid
	 * @param dramaid
	 * @param playstart
	 * @param playend
	 * @return
	 */
	List<DramaPlayItem> getDramaPlayItemList(String citycode, Long theatreid, Long dramaid, Timestamp playstart, Timestamp playend, Boolean isPartner);
	List<DramaPlayItem> getDramaPlayItemList(String citycode, Long theatreid, Long dramaid, Long starid, Timestamp playstart, Timestamp playend, Boolean isPartner);
	List<DramaPlayItem> getDramaPlayItemList(String citycode, Long theatreid, Long dramaid, Timestamp playstart, Timestamp playend, Boolean isPartner, Boolean isValidEndtime);
	List<DramaPlayItem> getDramaPlayItemList(String citycode, Long theatreid, Long dramaid, Long starid, Timestamp playstart, Timestamp playend, Boolean isPartner, Boolean isValidEndtime);
	List<DramaPlayItem> getUnOpenDramaPlayItemList(String citycode, Long theatreid, Long starid, Timestamp playstart, Timestamp playend, int maxnum);
	List<DramaPlayItem> getUnOpenDramaPlayItemList(String citycode, Long theatreid, Long dramaid, Long starid, Timestamp playstart, Timestamp playend, int maxnum);
	void initDramaPlayItem(DramaPlayItem item);
	void initDramaPlayItemList(List<DramaPlayItem> itemList);
	
	/**
	 * 得到放映厅列表
	 * @param theatreid
	 * @return
	 */
	List<TheatreRoom> getRoomList(Long theatreid);
	TheatreField getTheatreFieldByName(Long theatreid, String name);
	TheatreRoom getTheatreRoomByNum(Long theatreid, String fieldnum, String roomnum);
	List<TheatreSeatPrice> getTspList(Long dpid);
	List<TheatreSeatPrice> getTspList(Long dpid, Long areaid);
	TheatreSeatPrice getTsp(Long dpid, Long areaid, Integer price, String seattype);
	/**
	 * 得到价格列表
	 * @param theatreid
	 * @param dramaid
	 * @param starttime
	 * @param endtime
	 * @param isBooking
	 * @return
	 */
	List<Integer> getPriceList(Long theatreid, Long dramaid, Timestamp starttime, Timestamp endtime, boolean isBooking);
	/**
	 * 上演话剧的剧院
	 * @param dramaid
	 * @param isBooking
	 * @return
	 */
	List<Theatre> getTheatreList(String citycode, Long dramaid, boolean isBooking, int maxnum);
	/**
	 * 话剧是否能购票
	 * @param dramaid
	 * @return
	 */
	boolean isBookingByDramaId(Long dramaid);
	/**
	 * 查询有场次剧院idList
	 * @return
	 */
	List<Long> getTheatreidList(String citycode, Long dramaid, boolean isBooking);
	List<Long> getTheatreidList(String citycode, Long dramaid, String opentype, boolean isBooking);
	List<Long> getTheatreFieldIdList(String citycode, Long dramaid, boolean isBooking);
	List<Long> getCurBookingTheatreList(String citycode, String countycode);
	
	List<Long> getDramaStarDramaIdList(String citycode, Long dramaStarId);
	
	/**
	 * 查询当天演出的剧目最多的几家剧院
	 */
	List<Theatre> getTheatreidByDramaid(String citycode, int from, int maxnum);
	/**
	 * 查询一部话剧某一时间后的演出场次数量
	 */
	Integer getDramaCount(Long dramaid, Timestamp playtime);
	List<Date> getDramaPlayDateList(Long dramaid,Timestamp starttime, Timestamp endtime, Boolean isPartner);
	List<Date> getDramaPlayMonthDateList(Long dramaid, Boolean isPartner);
	Integer getDramaOpenCount();
	/**
	 * 某个价格的优化
	 * @param tspid
	 * @return
	 */
	List<DisQuantity> getDisQuantityList(Long tspid);
	/**
	 * 场次的优惠折扣
	 * @param dpid
	 * @return
	 */
	List<DisQuantity> getDisQuantityListByDpid(Long dpid);
	
	/**
	 * 指定话剧的开放场次
	 * @param dramaid
	 * @return
	 */
	List<Long> getDramaPlayIdList(List dramaid);
	
	DramaPlayItem getUniqueDpi(Long theatreid, Long dramaid, Long roomid, Timestamp playtime);
	
	List<Long> getCurDramaidList(String citycode);
	
	DramaPlayItem getDpiBySeqno(String seller, String sellerseq);
	/**
	 * 刷新项目价格
	 * @param dramaid
	 */
	void refreshDramaPrice(Long dramaid);
}
