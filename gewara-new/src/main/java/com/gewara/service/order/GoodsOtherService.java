package com.gewara.service.order;

import java.sql.Timestamp;
import java.util.List;
import java.util.Map;

import com.gewara.model.common.BaseEntity;
import com.gewara.model.goods.BaseGoods;

public interface GoodsOtherService extends GoodsService{
	
	<T extends BaseGoods> List<Long> getRelatedidList(Class<T> clazz, String citycode, String tag, String itemtype, Long itemid, boolean isBooking, boolean isGtZero, boolean cache);

	<T extends BaseGoods, S extends BaseEntity> List<Long> getRelatedidList(Class<T> clazz, String citycode, boolean isBooking, boolean isGtZero, Class<S> subClazz, String[] properties, Object[] values, boolean cache);
	
	<T extends BaseGoods, S extends BaseEntity> List<Long> getItemidList(Class<T> clazz, String citycode, boolean isBooking, boolean isGtZero, Class<S> subClazz, String[] properties, Object[] values, boolean cache);
	
	<T extends BaseGoods> Map<Long, Integer> getRelatedidByItemdCount(Class<T> clazz, String citycode, String tag, String itemtype, Timestamp starttime, Timestamp endtime, boolean isBooking, boolean isGtZero, boolean cache);

	<T extends BaseGoods> Integer getGoodsCount(Class<T> clazz, String citycode, String tag, Long relatedid, String itemtype, Long itemid, Timestamp starttime, Timestamp endtime, boolean isBooking, boolean isGtZero, boolean cache);

	<T extends BaseGoods> List<Long> getItemidList(Class<T> clazz, String citycode, String tag, Long relatedid, String itemtype, boolean isBooking, boolean isGtZero, boolean cache);
}
