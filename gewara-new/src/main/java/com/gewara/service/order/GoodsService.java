package com.gewara.service.order;

import java.sql.Timestamp;
import java.util.List;
import java.util.Map;

import com.gewara.model.acl.User;
import com.gewara.model.goods.ActivityGoods;
import com.gewara.model.goods.BaseGoods;
import com.gewara.model.goods.GoodsDisQuantity;
import com.gewara.model.goods.GoodsPrice;
import com.gewara.model.goods.SportGoods;
import com.gewara.model.goods.TicketGoods;
import com.gewara.support.ErrorCode;

public interface GoodsService {
	<T extends BaseGoods> List<T> getCurGoodsList(Class<T> clazz, String tag, Long relatedid, int from, int maxnum);
	<T extends BaseGoods> List<T> getGoodsList(Class<T> clazz, String citycode, String tag, Long relatedid, boolean isTotime, boolean limitRelease, boolean isGtZero, String order, boolean asc);
	<T extends BaseGoods> List<T> getGoodsList(Class<T> clazz, String citycode, String tag, Long relatedid, boolean isTotime, boolean limitRelease, boolean isGtZero, String order, boolean asc, int from, int maxnum);
	<T extends BaseGoods> List<T> getGoodsList(Class<T> clazz, String citycode, String tag, Long relatedid, String status, boolean isTotime, boolean limitRelease, boolean isGtZero, String order, boolean asc, int from, int maxnum);
	<T extends BaseGoods> List<T> getGoodsList(Class<T> clazz, String tag, Long relatedid, boolean isTotime, boolean limitRelease, boolean isGtZero, String order, boolean asc, boolean isGift);
	/**
	 * 购买某物品用户的数量
	 * @param gid
	 * @return
	 */
	Integer getBuyGoodsMemberCount(Long gid);
	<T extends BaseGoods> Integer getGoodsCount(Class<T> clazz, String tag, Long relatedid, boolean isTotime, boolean limitRelease, boolean isGtZero);
	<T extends BaseGoods> List<T> getGoodsListByStatusAndTag(Class<T> clazz, String status, String tag, int from, int maxnum);
	<T extends BaseGoods> Integer countByGoodsListByStatusAndTag(Class<T> clazz, String status, String tag);
	
	/**
	 *  根据Activityid 匹配 goods
	 */
	<T extends BaseGoods> Integer getBuyGoodsCount(Class<T> clazz, Long gid, Timestamp time, Long relatedid, String tag);
	
	List<SportGoods> getSportGoodsList(String citycode, String tag, Long relatedid, boolean isTotime, boolean limitRelease, boolean isGtZero, String order, boolean asc);
	
	List<SportGoods> getSportGoodsListBySportidAndItemid(Long sportid, Long itemid, Timestamp playDate, int from, int maxnum);
	List<Timestamp> getSportGoodsReleasetime(Long sportid, Long itemid, int from, int maxnum);
	Integer getSportGoodsReleasetimeCount(Long sportid, Long itemid);
	Integer getSportGoodsCount();
	
	//根据影院查询最优惠的商品
	<T extends BaseGoods> T  getGoodsByTagAndRelatedid(Class<T> clazz, String tag, Long relatedid, boolean isTotime, boolean limitRelease, boolean isGtZero);
	
	/**
	 * 新增或修改活动商品数据
	 * @param userid	管理员ID
	 * @param gid		商品ID
	 * @param dataMap	数据
	 * @return	
	 */
	ErrorCode<ActivityGoods> saveOrUpdateActivityGoods(Long userid, Long gid, Map<String, String> dataMap);
	ErrorCode<TicketGoods> saveCommonTicket(Long gid, String citycode, String goodsname, String tag, Long relatedid, String itemtype, Long itemid,Long starid, 
			Long roomid, Timestamp fromvalidtime, Timestamp tovalidtime, String language, String summary, String description, Integer maxbuy, String period, User user);
	/**
	 * 根据城市编码查询通票
	 * @param citycode		城市编码
	 * @param tag			场馆类型
	 * @param relatedid		场馆ID	
	 * @param itemtype		项目类型
	 * @param itemid	项日ID
	 * @param isTovaltime	是否有效
	 * @param isGtZero		是否大于0
	 * @return
	 */
	List<TicketGoods> getTicketGoodsList(String citycode, String tag, Long relatedid, String itemtype, Long itemid, boolean isTovaltime, boolean isGtZero);
	List<Map<String, String>> getTicketGoodsMapList(String citycode, String tag, Long relatedid, String itemtype, Long itemid, String period, Timestamp fromvalidtime, boolean isGtZero);
	/**
	 * 根据城市编码查询通票，根据goodssort顺序排序
	 * @param citycode		城市编码
	 * @param tag			场馆类型
	 * @param relatedid		场馆ID	
	 * @param itemtype		项目类型
	 * @param itemid	项日ID
	 * @param isTovaltime	是否有效
	 * @param isGtZero		是否大于0
	 * @param from			查询行
	 * @param maxnum		最大数据
	 * @return
	 */
	List<TicketGoods> getTicketGoodsList(String citycode, String tag, Long relatedid, String itemtype, Long itemid, boolean isTovaltime, boolean isGtZero, int from, int maxnum);
	Integer getTicketGoodsCount(String citycode, String tag, Long relatedid, String itemtype, Long itemid, boolean isTovaltime, boolean isGtZero);
	
	List<GoodsPrice> getGoodsPriceList(Long goodsid);
	/**
	 * 某价格的优惠
	 * @param goodspriceId  价格ID
	 * @return 返回优惠集合
	 */
	List<GoodsDisQuantity> getGoodsDisList(Long goodspriceId);
	List<GoodsDisQuantity> getGoodsDisListByGoodsid(Long goodsid);
	
	Map<Long/*goodsPriceid*/, List<GoodsDisQuantity>> getGoodsDisMapByPriceId(List<Long> priceIdList);
	Map<Long/*goodsPriceid*/, List<GoodsDisQuantity>> getGoodsDisMap(List<GoodsPrice> goodsPriceList);
	List<TicketGoods> getTicketGoodsList(String citycode, String tag, Long relatedid, String itemtype, Long itemid, String period, Timestamp fromtime, Timestamp totime, boolean isTovaltime, boolean isGtZero);
	Integer getTicketGoodsCount(String citycode, String tag, Long relatedid, String itemtype, Long itemid, String period, Timestamp fromtime, Timestamp totime, boolean isTovaltime, boolean isGtZero);
	ErrorCode saveTicketGoods(TicketGoods goods, String playdates, String rooms);
	<T extends BaseGoods> List<Integer> getGoodsPriceList(Class<T> clazz, String tag, Long relatedid, String itemtype, Long itemid, Timestamp starttime, Timestamp endtime);
}
