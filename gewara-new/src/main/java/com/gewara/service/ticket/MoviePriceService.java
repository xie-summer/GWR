package com.gewara.service.ticket;

import java.sql.Timestamp;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.gewara.model.movie.CityPrice;
import com.gewara.model.movie.MoviePrice;
import com.gewara.model.movie.MovieTierPrice;
import com.gewara.support.ErrorCode;

public interface MoviePriceService {
	List<String> PRICE_TIER = Arrays.asList("A", "B", "C", "D", "E", "F");
	/**
	 * 通过电影ID查询价格基础数据
	 * @param movieid 电影ID
	 * @return 电影所有类别的价格
	 */
	List<MovieTierPrice> getMovieTierPriceList(Long movieid);
	/**
	 * 通过电影ID集合查询价格基础数据
	 * @param movieid 电影ID集合
	 * @return 电影所有类别的价格
	 */
	List<MovieTierPrice> getMovieTierPriceList(Long... movieid);
	
	/**
	 * 通过电影ID与类别查询价格
	 * @param movieid	电影ID
	 * @param type	价格类别
	 * @return	电影类别的价格
	 */
	MovieTierPrice getMovieTierPrice(Long movieid, String type);
	
	/**
	 * 通过电影ID与价格类别增加或修改价格
	 * @param movieid	电影ID
	 * @param type	价格类别
	 * @param price 价格
	 * @return 电影类别的价格 （如报错：返回错误信息）
	 */
	ErrorCode<MovieTierPrice> saveOrUpdateMovieTierPrice(Long movieid, String type, Integer price, Integer edition3D,Integer editionJumu,
			Integer editionIMAX,Integer rangeEdition3D,Integer rangePrice,Integer rangeEditionJumu,Integer rangeEditionIMAX,
			Timestamp startTime,Timestamp endTime);
	
	/**
	 *	根据城市获取当前价格不同的电影最低价格
	 *	@param 城市代码
	 * @return 电影最低价格
	 */
	List<MoviePrice> getDiffMoviePriceList(String citycode);
	CityPrice getCityPrice(Long relatedid, String citycode, String tag);
	/**
	 * 得到PlacePrice（场馆价格）
	 * @param tag
	 * @param relatedid
	 * @param category
	 * @param categoryid
	 * @return
	 */
	Map getPlacePriceFromCache(String tag, Long relatedid, String category, Long categoryid);
	/**
	 * @param tag
	 * @param relatedid
	 * @return minprice, maxprice
	 */
	Map getMinMaxPlacePrice(String tag, Long relatedid);
	/**
	 * 更新场馆数据
	 * @param tag
	 * @param relatedid
	 * @param category
	 * @param categoryid
	 * @param avgprice
	 * @param minprice
	 * @param maxprice
	 */
	void saveOrUpdatePlacePrice(String tag, Long relatedid, String category, Long categoryid, Integer avgprice, Integer minprice, Integer maxprice);
	/**
	 * 通过经纬度获取一定范围内的场馆数据
	 * @param clazz 场馆对象
	 * @param citycode 城市代码
	 * @param countycode 城市区域代码
	 * @param bpointx 经度
	 * @param bpointy 纬度
	 * @param spaceRound 范围
	 * @return 场馆数据集合
	 */
	
}
