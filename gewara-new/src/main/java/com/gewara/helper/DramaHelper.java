package com.gewara.helper;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.gewara.model.drama.Drama;
import com.gewara.util.BeanUtil;

public abstract class DramaHelper implements Serializable {
	private static final long serialVersionUID = -3810605886869730408L;
	
	private static boolean excludeDramaByDate(Drama drama, Date date){
		if(date == null) return true;
		final boolean releasedate = drama.getReleasedate() == null;
		final boolean enddate = drama.getEnddate() == null;
		if(releasedate && enddate){
			return true;
		}
		return releasedate && date.before(drama.getEnddate()) 
			|| enddate && date.after(drama.getReleasedate())
			|| date.after(drama.getReleasedate()) && date.before(drama.getEnddate());
	}
	
	private static boolean excludeDramaByStarid(Drama drama, Long starid){
		if(starid == null) return true;
		if(StringUtils.isBlank(drama.getActors())) return false;
		List<Long> idList = BeanUtil.getIdList(drama.getActors(), ",");
		if(idList.isEmpty()) return false;
		return idList.contains(starid);
	}
	
	private static boolean excludeDramaByPrice(Drama drama, Integer minprice, Integer maxprice){
		final boolean minFlag = (minprice == null), maxFlag = (maxprice == null);
		if( minFlag && maxFlag) return true;
		if(StringUtils.isBlank(drama.getPrices())) return false;
		List<Integer> priceList = BeanUtil.getIntgerList(drama.getPrices(), ",");
		if(priceList.isEmpty()) return false;
		for (Integer price : priceList) {
			if(minFlag && price >= maxprice 
				|| maxFlag && price <= minprice 
				|| (!minFlag && !maxFlag && price>= minprice && price <= maxprice)){
				return true;
			}
		}
		return false;
	}

	public static List<Drama> dateFilter(List<Drama> dramaList, Date date){
		if(date == null) return dramaList;
		List<Drama> tmpList = new ArrayList<Drama>();
		for (Drama drama : dramaList) {
			if(excludeDramaByDate(drama, date)) tmpList.add(drama);
		}
		return tmpList;
	}
	
	public static List<Drama> dramaStarFilter(List<Drama> dramaList, Long starid){
		if(starid == null) return dramaList;
		List<Drama> tmpList = new ArrayList<Drama>();
		for (Drama drama : dramaList) {
			if(excludeDramaByStarid(drama, starid)) tmpList.add(drama);
		}
		return tmpList;
	}
	
	public static List<Drama> priceFilter(List<Drama> dramaList, Integer minprice, Integer maxprice){
		if(minprice == null && maxprice == null) return dramaList;
		List<Drama> tmpList = new ArrayList<Drama>();
		for (Drama drama : dramaList) {
			if(excludeDramaByPrice(drama, minprice, maxprice)) tmpList.add(drama);
		}
		return tmpList;
	}
	
	public static List<Drama> dramaListFilter(List<Drama> dramaList, Date date, Long starid, Integer minprice, Integer maxprice){
		if(date == null && starid == null && minprice == null && maxprice == null) return dramaList;
		List<Drama> tmpList = new ArrayList<Drama>();
		for (Drama drama : dramaList) {
			if(excludeDramaByDate(drama, date)
				&& excludeDramaByStarid(drama, starid)
				&& excludeDramaByPrice(drama, minprice, maxprice)){
				tmpList.add(drama);
			}
		}
		return tmpList;
	}
}
