package com.gewara.service.ticket.impl;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.ArrayUtils;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.criterion.Subqueries;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.gewara.constant.sys.CacheConstant;
import com.gewara.model.movie.CityPrice;
import com.gewara.model.movie.MoviePrice;
import com.gewara.model.movie.MovieTierPrice;
import com.gewara.model.movie.PlacePrice;
import com.gewara.service.impl.BaseServiceImpl;
import com.gewara.service.ticket.MoviePriceService;
import com.gewara.support.ErrorCode;
import com.gewara.untrans.CacheService;
import com.gewara.util.BeanUtil;
import com.gewara.util.DateUtil;

@Service("moviePriceService")
public class MoviePriceServiceImpl extends BaseServiceImpl implements MoviePriceService {
	@Autowired@Qualifier("cacheService")
	private CacheService cacheService;

	@Override
	public List<MovieTierPrice> getMovieTierPriceList(Long movieid){
		DetachedCriteria qry = DetachedCriteria.forClass(MovieTierPrice.class);
		qry.add(Restrictions.eq("movieid", movieid));
		List<MovieTierPrice> movieTierPriceList = hibernateTemplate.findByCriteria(qry);
		return movieTierPriceList;
	}
	
	@Override
	public List<MovieTierPrice> getMovieTierPriceList(Long... movieid){
		DetachedCriteria qry = DetachedCriteria.forClass(MovieTierPrice.class);
		if(!ArrayUtils.isEmpty(movieid)){
			qry.add(Restrictions.in("movieid", movieid));
		}
		List<MovieTierPrice> movieTierPriceList = hibernateTemplate.findByCriteria(qry);
		return movieTierPriceList;
	}
	
	@Override
	public MovieTierPrice getMovieTierPrice(Long movieid, String type){
		DetachedCriteria qry = DetachedCriteria.forClass(MovieTierPrice.class);
		qry.add(Restrictions.eq("movieid", movieid));
		qry.add(Restrictions.eq("type", type));
		List<MovieTierPrice> movieTierPriceList = hibernateTemplate.findByCriteria(qry, 0, 1);
		if(movieTierPriceList.isEmpty()) return null;
		return movieTierPriceList.get(0);
	}

	@Override
	public ErrorCode<MovieTierPrice> saveOrUpdateMovieTierPrice(Long movieid, String type, Integer price, Integer edition3D,
			Integer editionJumu,Integer editionIMAX,Integer rangeEdition3D,Integer rangePrice,Integer rangeEditionJumu,
			Integer rangeEditionIMAX,Timestamp startTime,Timestamp endTime) {
		if(movieid == null || !PRICE_TIER.contains(type)) return ErrorCode.getFailure("参数错误！");
		MovieTierPrice movieTierPrice = getMovieTierPrice(movieid, type);
		if(movieTierPrice == null){
			if( price == null || price<=0) return ErrorCode.getFailure("价格设置错误,请先设置2D价格");
			movieTierPrice = new MovieTierPrice(movieid, type, price);
		}else{
			if( price != null && price > 0) movieTierPrice.setPrice(price);
			if(edition3D != null && edition3D > 0)	movieTierPrice.setEdition3D(edition3D);
			if(editionJumu != null && editionJumu > 0)	movieTierPrice.setEditionJumu(editionJumu);
			if(editionIMAX != null && editionIMAX > 0)	movieTierPrice.setEditionIMAX(editionIMAX);
			if(startTime != null)	movieTierPrice.setStartTime(startTime);
			if(endTime != null)	movieTierPrice.setEndTime(endTime);
			if(rangeEdition3D != null && rangeEdition3D > 0)	movieTierPrice.setRangeEdition3D(rangeEdition3D);
			if(rangePrice != null && rangePrice > 0)	movieTierPrice.setRangePrice(rangePrice);
			if(rangeEditionJumu != null && rangeEditionJumu > 0)	movieTierPrice.setRangeEditionJumu(rangeEditionJumu);
			if(rangeEditionIMAX != null && rangeEditionIMAX > 0)	movieTierPrice.setRangeEditionIMAX(rangeEditionIMAX);
		}
		baseDao.saveObject(movieTierPrice);
		return ErrorCode.getSuccessReturn(movieTierPrice);
	}

	@Override
	public List<MoviePrice> getDiffMoviePriceList(String citycode) {
		DetachedCriteria query = DetachedCriteria.forClass(MoviePrice.class, "mp");
		query.add(Restrictions.eq("mp.citycode", citycode));
		query.add(Restrictions.isNotNull("mp.type"));
		DetachedCriteria subQuery = DetachedCriteria.forClass(MovieTierPrice.class, "mtp");
		subQuery.add(Restrictions.eqProperty("mp.movieid", "mtp.movieid"));
		subQuery.add(Restrictions.eqProperty("mp.type", "mtp.type"));
		subQuery.add(Restrictions.neProperty("mp.price", "mtp.price"));
		subQuery.setProjection(Projections.property("mtp.id"));
		query.add(Subqueries.exists(subQuery));
		List<MoviePrice> movieidList = hibernateTemplate.findByCriteria(query);
		return movieidList;
	}
	@Override
	public CityPrice getCityPrice(Long relatedid, String citycode, String tag){
		String key = CacheConstant.buildKey("getCityPriceXXyd", tag, relatedid,citycode);
		CityPrice cityPrice = (CityPrice)cacheService.get(CacheConstant.REGION_TWENTYMIN, key);
		if(cityPrice != null) return cityPrice;
		DetachedCriteria query = DetachedCriteria.forClass(CityPrice.class);
		query.add(Restrictions.eq("tag", tag));
		query.add(Restrictions.eq("citycode", citycode));
		query.add(Restrictions.eq("relatedid", relatedid));
		List<CityPrice> priceList = hibernateTemplate.findByCriteria(query);
		if(priceList.isEmpty()) return null;
		cityPrice = priceList.get(0);
		cacheService.set(CacheConstant.REGION_TWENTYMIN, key, cityPrice);
		return cityPrice;
	}
	@Override
	public Map getPlacePriceFromCache(String tag, Long relatedid, String category, Long categoryid) {
		String key = (relatedid+tag+category+categoryid);
		Map result = (Map) cacheService.get(CacheConstant.REGION_TWOHOUR, key);
		if(result==null){
			PlacePrice pp = getPlacePrice2(tag, relatedid, category, categoryid);
			if(pp!=null) {
				result = BeanUtil.getBeanMap(pp);
				cacheService.set(CacheConstant.REGION_TWOHOUR, key, result);
			}
		}
		return result;
	}
	@Override
	public Map getMinMaxPlacePrice(String tag, Long relatedid) {
		String key = CacheConstant.buildKey("miMxPlaceXYxx", tag, relatedid);
		Map result = (Map) cacheService.get(CacheConstant.REGION_TWOHOUR, key);
		if(result==null){
			String hql = "select new map(min(minprice) as minprice, max(maxprice) as maxprice) from PlacePrice where tag=? and relatedid=?";
			List<Map<String, Long>> priceMapList = hibernateTemplate.find(hql, tag, relatedid);
			for(Map<String,Long> priceMap : priceMapList){
				if(priceMap.get("minprice")!=null){
					result = new HashMap<String, Integer>();
					Integer minprice = Integer.valueOf(priceMap.get("minprice")+"");
					Integer maxprice = Integer.valueOf(priceMap.get("maxprice")+"");
					result.put("minprice", minprice);
					result.put("maxprice", maxprice);
					cacheService.set(CacheConstant.REGION_TWOHOUR, key, result);
				}else {
					return null;	
				}
			}
		}
		return result;
	}
	@Override
	public void saveOrUpdatePlacePrice(String tag, Long relatedid, String category, Long categoryid, Integer avgprice, Integer minprice, Integer maxprice){
		PlacePrice placePrice = getPlacePrice2(tag, relatedid, category, categoryid);
		if(placePrice == null){
			placePrice = new PlacePrice(tag, relatedid, category, categoryid, avgprice, minprice, maxprice);
		}else{
			placePrice.setAvgprice(avgprice);
			placePrice.setMinprice(minprice);
			placePrice.setMaxprice(maxprice);
			placePrice.setUpdatetime(DateUtil.getCurFullTimestamp());
		}
		baseDao.saveObject(placePrice);
	}
	private PlacePrice getPlacePrice2(String tag, Long relatedid, String category, Long categoryid) {
		DetachedCriteria query = DetachedCriteria.forClass(PlacePrice.class);
		query.add(Restrictions.eq("tag", tag));
		query.add(Restrictions.eq("relatedid", relatedid));
		query.add(Restrictions.eq("category", category));
		query.add(Restrictions.eq("categoryid", categoryid));
		List<PlacePrice> placePriceList = hibernateTemplate.findByCriteria(query, 0, 1);
		if(placePriceList.isEmpty()) return null;
		return placePriceList.get(0);
	}
}
