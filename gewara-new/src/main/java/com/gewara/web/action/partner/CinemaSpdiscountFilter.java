/** 
 */
package com.gewara.web.action.partner;

import java.sql.Timestamp;

import com.gewara.constant.PayConstant;
import com.gewara.model.movie.Cinema;
import com.gewara.model.pay.SpecialDiscount;

/**
 * @author Zhicheng.Peng   Johnny.Resurgam@Gmail.com
 *  
 *  Apr 26, 2013  5:49:18 PM
 */
public class CinemaSpdiscountFilter extends ObjectSpdiscountFilter{
	
	public CinemaSpdiscountFilter(Cinema cinema, Timestamp addtime){
		this(cinema.getCitycode(), PayConstant.APPLY_TAG_MOVIE, cinema.getId(), addtime);
	}
	/**
	 * @param citycode
	 * @param tag
	 * @param relatedid
	 * @param addtime
	 */
	private CinemaSpdiscountFilter(String citycode, String tag, Long relatedid,
			Timestamp addtime) {
		super(citycode, tag, relatedid, addtime);
	}
	
	
	@Override
	public boolean excludeOpi(SpecialDiscount item) {
		return super.excludeOpi(item, false);
	}
}
