package com.gewara.service.partner.impl;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.gewara.constant.AdminCityContant;
import com.gewara.constant.sys.CacheConstant;
import com.gewara.constant.ticket.OpiConstant;
import com.gewara.constant.ticket.OrderConstant;
import com.gewara.model.api.ApiUser;
import com.gewara.model.movie.Cinema;
import com.gewara.model.movie.Movie;
import com.gewara.model.partner.PartnerCloseRule;
import com.gewara.model.pay.TicketOrder;
import com.gewara.model.ticket.OpenPlayItem;
import com.gewara.service.impl.BaseServiceImpl;
import com.gewara.service.partner.PartnerService;
import com.gewara.support.MultiPropertyComparator;
import com.gewara.untrans.CacheService;
import com.gewara.util.DateUtil;
/**
 * 此模块用于合作伙伴开放场次相关
 * @author acerge(acerge@163.com)
 * @since 4:19:27 PM Mar 9, 2010
 */
@Service("partnerService")
public class PartnerServiceImpl extends BaseServiceImpl implements PartnerService {
	@Autowired@Qualifier("cacheService")
	private CacheService cacheService;
	public void setCacheService(CacheService cacheService) {
		this.cacheService = cacheService;
	}
	private List<PartnerCloseRule> pcrList = null;
	@Override
	public List<OpenPlayItem> getPartnerOpiList(ApiUser partner, String citycode, Long cinemaid, Long movieid, Date playdate){
		if(!partner.isEnabled()) return new ArrayList<OpenPlayItem>(); 
		List<OpenPlayItem> opiList = getPartnerOpiList(citycode, cinemaid, movieid, playdate, true);
		return opiList;
	}
	private List<OpenPlayItem> getPartnerOpiList(String citycode, Long cinemaid, Long movieid, Date playdate, boolean booking){
		String key = CacheConstant.buildKey("partnerOpiList", citycode, cinemaid, movieid, playdate, booking);
		List<Long> idList = (List<Long>) cacheService.get(CacheConstant.REGION_TENMIN, key);
		if(idList == null){
			DetachedCriteria query = DetachedCriteria.forClass(OpenPlayItem.class, "op");
			if (cinemaid!=null) {
				query.add(Restrictions.eq("op.cinemaid", cinemaid));
			}else{
				List<String> citycodeList = Arrays.asList(StringUtils.split(citycode, ","));
				if(citycodeList.size()==1){
					if(!StringUtils.equals(citycode, AdminCityContant.CITYCODE_ALL)) query.add(Restrictions.eq("op.citycode", citycode)); 
				}else{
					query.add(Restrictions.in("op.citycode", citycodeList));
				}
			}
			query.add(Restrictions.eq("op.partner", OpiConstant.PARTNER_OPEN));
			if (movieid!=null) query.add(Restrictions.eq("op.movieid", movieid));
			Timestamp starttime = DateUtil.getBeginningTimeOfDay(new Timestamp(playdate.getTime()));
			Timestamp endtime = DateUtil.getLastTimeOfDay(starttime);
			query.add(Restrictions.ge("op.playtime", starttime));
			query.add(Restrictions.le("op.playtime", endtime));
			if(booking){
				query.add(Restrictions.eq("op.status", OpiConstant.STATUS_BOOK));
				Timestamp cur = new Timestamp(System.currentTimeMillis());
				query.add(Restrictions.le("op.opentime", cur));
				query.add(Restrictions.ge("op.closetime", cur));
				query.add(Restrictions.ltProperty("op.gsellnum", "op.asellnum"));
			}
			
			query.setProjection(Projections.property("id"));
			idList = hibernateTemplate.findByCriteria(query);
			cacheService.set(CacheConstant.REGION_TENMIN, key, idList);
		}
		List<OpenPlayItem> opiList = baseDao.getObjectList(OpenPlayItem.class, idList);
		Collections.sort(opiList, new MultiPropertyComparator(new String[]{"cinemaid", "movieid", "playtime"}, new boolean[]{true, true, true}));
		return opiList;
	}
	@Override
	public List<Date> getPlaydateList(ApiUser partner, String citycode, Long movieid){
		if(!partner.isEnabled()) return new ArrayList<Date>();
		String key = CacheConstant.buildKey("getPlayd268eM2apList" + citycode + movieid);
		List<String> playdateList = (List<String>) cacheService.get(CacheConstant.REGION_TENMIN, key);
		if(playdateList==null){
			Timestamp curtime = new Timestamp(System.currentTimeMillis());
			String query = "select distinct to_char(playtime,'yyyy-mm-dd') from OpenPlayItem opi " +
					"where partner = ? and status = ? and closetime > ? and opentime < ? " +
					"and gsellnum < asellnum and opi.citycode = ? ";
			if(movieid != null) {
				query += "and movieid = ?";
				playdateList = hibernateTemplate.find(query, OpiConstant.PARTNER_OPEN, OpiConstant.STATUS_BOOK, curtime, curtime, citycode, movieid);
			}else{
				playdateList = hibernateTemplate.find(query, OpiConstant.PARTNER_OPEN, OpiConstant.STATUS_BOOK, curtime, curtime, citycode);
			}
			
			Collections.sort(playdateList);
			cacheService.set(CacheConstant.REGION_TENMIN, key, playdateList);
		}
		List<Date> result = new ArrayList<Date>(); 
		for(String date : playdateList){
			result.add(DateUtil.parseDate(date));
		}
		return result;
	}
	@Override
	public List<Movie> getOpenMovieListByDate(ApiUser partner, String citycode, Date playdate){
		return getOpenMovieList(partner, citycode, null, playdate);
	}
	@Override
	public List<Movie> getOpenMovieList(ApiUser partner, String citycode, Long cinemaid, Date playdate){
		if(!partner.isEnabled()) return new ArrayList<Movie>();
		String key = CacheConstant.buildKey("getOpen2k8oe2ist", citycode, cinemaid, playdate);
		List<Long> movieidList = (List<Long>) cacheService.get(CacheConstant.REGION_TENMIN, key);
		if(movieidList==null){
			DetachedCriteria query = DetachedCriteria.forClass(OpenPlayItem.class, "op");
			query.add(Restrictions.eq("op.partner", OpiConstant.PARTNER_OPEN));
			if (cinemaid!=null){
				query.add(Restrictions.eq("op.cinemaid", cinemaid));
			}else{
				List<String> citycodeList = Arrays.asList(StringUtils.split(citycode, ","));
				if(citycodeList.size()==1){
					if(!StringUtils.equals(citycode, AdminCityContant.CITYCODE_ALL)) query.add(Restrictions.eq("op.citycode", citycode)); 
				}else{
					query.add(Restrictions.in("op.citycode", citycodeList));
				}
			}
			if(playdate != null){
				Timestamp starttime = DateUtil.getBeginningTimeOfDay(new Timestamp(playdate.getTime()));
				Timestamp endtime = DateUtil.getLastTimeOfDay(starttime);
				query.add(Restrictions.ge("op.playtime", starttime));
				query.add(Restrictions.lt("op.playtime", endtime));
			}
			query.add(Restrictions.eq("op.status", OpiConstant.STATUS_BOOK));
			Timestamp cur = new Timestamp(System.currentTimeMillis());
			query.add(Restrictions.lt("op.opentime", cur));
			query.add(Restrictions.gt("op.closetime", cur));
			query.add(Restrictions.ltProperty("op.gsellnum", "op.asellnum"));
			query.setProjection(Projections.distinct(Projections.property("op.movieid")));
			movieidList = hibernateTemplate.findByCriteria(query);
			cacheService.set(CacheConstant.REGION_TENMIN, key, movieidList);
		}
		List<Movie> movieList = baseDao.getObjectList(Movie.class, movieidList);
		return movieList;
	}
	@Override
	public List<TicketOrder> getPaidOrderListByPaidtime(ApiUser apiUser, Timestamp paidtimeFrom, Timestamp paidtimeTo){
		DetachedCriteria query = DetachedCriteria.forClass(TicketOrder.class, "t");
		query.add(Restrictions.like("status", OrderConstant.STATUS_PAID, MatchMode.START));
		query.add(Restrictions.ge("paidtime", paidtimeFrom));
		query.add(Restrictions.lt("paidtime", paidtimeTo));
		query.add(Restrictions.eq("t.partnerid", apiUser.getId()));
		query.addOrder(Order.desc("addtime"));
		List<TicketOrder> orderList = hibernateTemplate.findByCriteria(query);
		return orderList;
	}
	@Override
	public List<TicketOrder> getRefundOrderList(ApiUser apiUser, Timestamp refundtimeFrom, Timestamp refundtimeTo) {
		DetachedCriteria query = DetachedCriteria.forClass(TicketOrder.class, "t");
		query.add(Restrictions.eq("status", OrderConstant.STATUS_PAID_RETURN));
		query.add(Restrictions.ge("updatetime", refundtimeFrom));
		query.add(Restrictions.lt("updatetime", refundtimeTo));
		query.add(Restrictions.eq("t.partnerid", apiUser.getId()));
		query.addOrder(Order.desc("addtime"));
		List<TicketOrder> orderList = hibernateTemplate.findByCriteria(query);
		return orderList;
	}

	@Override
	public List<Cinema> getOpenCinemaList(ApiUser partner, String citycode, Long movieid, Date playdate) {
		String key = CacheConstant.buildKey("getO3en28nem5List", citycode, movieid, playdate);
		List<Long> cinemaidList = (List<Long>) cacheService.get(CacheConstant.REGION_TENMIN, key);
		if(cinemaidList==null){
			DetachedCriteria query = DetachedCriteria.forClass(OpenPlayItem.class, "op");
			query.add(Restrictions.eq("partner", OpiConstant.PARTNER_OPEN));
			if(playdate != null){
				Timestamp starttime = DateUtil.getBeginningTimeOfDay(new Timestamp(playdate.getTime()));
				Timestamp endtime = DateUtil.getLastTimeOfDay(starttime);
				query.add(Restrictions.ge("op.playtime", starttime));
				query.add(Restrictions.lt("op.playtime", endtime));
			}
			query.add(Restrictions.eq("op.status", OpiConstant.STATUS_BOOK));
			Timestamp cur = new Timestamp(System.currentTimeMillis());
			query.add(Restrictions.lt("op.opentime", cur));
			query.add(Restrictions.gt("op.closetime", cur));
			query.add(Restrictions.ltProperty("op.gsellnum", "op.asellnum"));
			List<String> citycodeList = Arrays.asList(StringUtils.split(citycode, ","));
			if(citycodeList.size()==1){
				if(!StringUtils.equals(citycode, AdminCityContant.CITYCODE_ALL)){ 
					query.add(Restrictions.eq("op.citycode", citycode)); 
				}
			}else{
				query.add(Restrictions.in("op.citycode", citycodeList));
			}
			if(movieid!=null) query.add(Restrictions.eq("op.movieid", movieid));
			query.setProjection(Projections.distinct(Projections.property("op.cinemaid")));
			cinemaidList = hibernateTemplate.findByCriteria(query);
			cacheService.set(CacheConstant.REGION_TENMIN, key, cinemaidList);
		}
		return baseDao.getObjectList(Cinema.class, cinemaidList);
	}
	
	@Override
	public List<PartnerCloseRule> getCloseRuleList() {
		if(pcrList==null) {
			refreshCurrent(null);
		}
		Timestamp cur = DateUtil.getCurTruncTimestamp();
		List<PartnerCloseRule> result = new ArrayList<PartnerCloseRule>();
		for(PartnerCloseRule pcr: pcrList){
			if(pcr.getOpentime2().after(cur)){
				result.add(pcr);
			}
		}
		return result;
	}
	@Override
	public void refreshCurrent(String newConfig) {
		Timestamp cur = DateUtil.getCurTruncTimestamp();
		String query = "select id from PartnerCloseRule where opentime2 > ?";
		List<Long> idList = hibernateTemplate.find(query, cur);
		pcrList = baseDao.getObjectList(PartnerCloseRule.class, idList);
	}
}
