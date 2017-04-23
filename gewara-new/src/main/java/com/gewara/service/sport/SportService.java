package com.gewara.service.sport;

import java.sql.Timestamp;
import java.util.List;
import java.util.Map;

import com.gewara.model.sport.OpenTimeItem;
import com.gewara.model.sport.ProgramItemTime;
import com.gewara.model.sport.Sport;
import com.gewara.model.sport.Sport2Item;
import com.gewara.model.sport.SportItem;
import com.gewara.model.sport.SportItemPrice;
import com.gewara.model.sport.SportPrice;
import com.gewara.model.sport.SportPriceTable;
import com.gewara.model.sport.SportProfile;

/**
 * Movie,Sport,PlayTime Service
 * @author <a href="mailto:acerge@163.com">gebiao(acerge)</a>
 * @since 2007-9-28下午02:05:17
 */
public interface SportService{
	boolean updateSportFlag(Long sid, String flag);
	//修改热门度
	void updateSportHotValue(Long sid,Integer hotvalue);
	//end. service method
	List<Sport> getHotSports(String citycode, String order, boolean isHot, int maxnum);
	//根据城市区域商圈得到运动场馆ID
	List<Long> getSportIdByCode(String citycode, String countycode, String indexareacode, int from, int maxnum);
	int getSportCountByCode(String citycode, String countycode, String indexareacode);
	List<SportPriceTable> getPricetableList(Long itemid, int from, int maxnum) ;
	int getPricetableCount();
	List<SportPriceTable> getRandomPricetableList(Long itemid,int maxnum);
	List<SportItem> getTopSportItemList();
	/**
	 * @return all parent id is not null
	 */
	List<SportItem> getAllSportItem();
	List<SportItem> getSubSportItemList(Long sportItemId, String type);
	List<SportItem> getHotSportItemList(int from, int maxnum);
	/**
	 * 推荐运动项目
	 * @param sportId
	 * @param value
	 * @return
	 */
	boolean updateSportItemFlagValue(Long sid, String value);
	List<SportItem> getCommendSportItemList(int from, int maxnum);
	/**
	 * List<Map(countycode,num)>
	 * @param id
	 * @return
	 */
	List<Map> getCountyAndSportNum(Long id);
	/**
	 * 通过区号取有价格表的场馆列表
	 * @param countycode
	 * @return List<Sport>
	 */
	List<Map> getSportListByCountyCode(Long id, String countycode);
	List<SportPriceTable> getPriceTableListBySportId(Long sportid);
	/**
	 * 根据场馆sportid,itemid查询
	 */
	SportPriceTable getSportPriceTable(Long sportid, Long itemid);
	/**
	 * 根据sprotpriceTableid查询项目价格
	 */
	List<SportPrice> getSportPriceList(Long priceTableid);
	
	/**
	 *  根据 itemName 匹配 itemList
	 */
	List<SportItem> getSportlistLikeItemname(String key);
	List<SportItem> getSportItemListBySportId(Long sportId, String booking);
	List<Sport2Item> getSport2ItemListBySportId(Long sportId);
	Sport2Item getSport2Item(Long sportId, Long itemId);
	List<SportPrice> getPriceList(Long priceTableId);
	
	SportPrice getSportPriceByPriceTableId(Long priceTableId);
	
	List<Sport> getSportByItemAndClickTimes(Long itemdId,int from,int max);
	
	List<Sport> getBookingEqOpenSport(String citycode,String bookingstatus);
	
	List<Map> getMaxHourAndMinHour(Long sportid);
	
	List<SportItem> getSportItemBySportId(Long sportid);
	
	List<Sport> getCurSportList(String orderField);
	
	//得到最大、做小与平均的价格
	Map<String,Integer> getSportPrice(Long sportid, Long itemid);
	
	/**
	 * 得到某一场次最大最下价格
	 */
	Map<String,Integer> getSportPriceByOtt(Long sportid, Long itemid, Long ottid);
	//得到开放项目ID
	List<Long> getBookingItemList(Long itemid, String citycode);
	//得到开放项目的场馆ID
	List<Long> getBookingSportIdList(Long itemid, String citycode);

	List<Long> getNearSportList(double maxLd, double minLd, double maxLa,double minLa,Long itemid, String citycode);
	List<Long> getSportList(String type, String name,String countycode,Long subwayid,Long itemid);
	List<Long> getSportListByOrder(Long memberid, int from, int maxnum);
	List<Long> getMemberListByOrder(Long sportid, Timestamp addtime, int from, int maxnum);
	List<Long> getSportProfileListByItemId(List<Long> idList,Long itemId); 
	List<SportPrice> getPriceList(Long sportid, Long itemid);
	List<ProgramItemTime> getProgramItemTimeList(Long sportid, Long itemid); 
	
	
	List<Long> getSportBySportItem(Long itemid, String citycode);
	Integer getSportCountBySportItem(Long itemid, String citycode, boolean isopen);
	/**
	 * 查询价格表
	 */
	SportItemPrice getSportItemPriceBySportIdAndItemId(Long sportid, Long itemid, Integer week);
	List<SportItemPrice> getSportItemPriceListBySportIdAndItemId(Long sportid, Long itemid);
	List<Sport> getSportList(String flag, String key, String citycode, int from, int maxnum);
	Integer getSportCount(String flag, String key, String citycode);
	List<SportProfile> getSportProfileList(String key, String citycode, Long siId, String company, boolean isBooking, int from, int maxnum);
	Integer getSportProfileCount(String key, String citycode, Long siId, String company, boolean isBooking);
	
	/**
	 * 查询是否可预订
	 */
	List<String> getBookingSportItemList();
	//运动项目列表页
	List<SportItem> getSportItemList(String itemname, Long parentid, String type, String order, int from, int maxnum);
	//运动项目列表页数量
	int getSportItemCount(String itemname, Long parentid, String type);
	
	/**
	 * @param orderId
	 * @return
	 * 查询订单包含的OpenPlayItem
	 */
	List<OpenTimeItem> getOrderPlayItemList(Long orderId);
	/**
	 * 更新价格
	 * @param sportid
	 * @param itemid
	 * @param minprice
	 * @param avgprice
	 * @param maxprice
	 */
	void updateSportItemPrice(Long sportid, Long itemid, Integer minprice, Integer avgprice, Integer maxprice);
}
