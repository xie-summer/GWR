package com.gewara.service.drama.impl;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Service;

import com.gewara.constant.Status;
import com.gewara.model.drama.OpenDramaItem;
import com.gewara.model.drama.OpenTheatreSeat;
import com.gewara.model.drama.Theatre;
import com.gewara.model.drama.TheatreRoom;
import com.gewara.model.drama.TheatreRoomSeat;
import com.gewara.model.drama.TheatreSeatPrice;
import com.gewara.service.drama.TheatreService;
import com.gewara.service.impl.BaseServiceImpl;
import com.gewara.web.action.drama.SearchTheatreCommand;

@Service("theatreService")
public class TheatreServiceImpl extends BaseServiceImpl implements TheatreService {

	@Override
	public List<Theatre> getTheatreListByHotvalue(String citycode, Integer hotvalue, int from, int maxnum){
		DetachedCriteria query = DetachedCriteria.forClass(Theatre.class);
		query.add(Restrictions.eq("citycode", citycode));
		if(hotvalue != null) query.add(Restrictions.eq("hotvalue", hotvalue));
		query.addOrder(Order.desc("clickedtimes"));
		List<Theatre> theatreList = hibernateTemplate.findByCriteria(query, from, maxnum);
		return theatreList;
	}

	@Override
	public List<Theatre> getTheatreListBySearchComment(
			SearchTheatreCommand stc, String citycode, int from, int maxnum) {
		DetachedCriteria query = DetachedCriteria.forClass(Theatre.class);
		query.add(Restrictions.eq("citycode", citycode));
		if(StringUtils.isNotBlank(stc.getTheatrename())){
			query.add(Restrictions.ilike("name", stc.getTheatrename(), MatchMode.ANYWHERE));
		}
		if(StringUtils.isNotBlank(stc.indexareacode)){
			query.add(Restrictions.eq("indexareacode", stc.indexareacode));
		}else if(StringUtils.isNotBlank(stc.getCountycode())){
			query.add(Restrictions.eq("countycode", stc.countycode));
		}
		// 地铁沿线：和其他条件不关联
		if (stc.lineid != null) {
			query.add(Restrictions.like("lineidlist", String.valueOf(stc.lineid), MatchMode.ANYWHERE));
			if(stc.stationid!=null){
				query.add(Restrictions.eq("stationid", new Long(stc.stationid)));
			}
		}
		// 停车位
		if (StringUtils.isNotBlank(stc.park)) {
			query.add(Restrictions.isNotNull("park"));
		}
		//刷卡
		if(StringUtils.isNotBlank(stc.getVisacard()) && stc.getVisacard()!=null){
			query.add(Restrictions.isNotNull("visacard"));
		}
		if(StringUtils.isNotBlank(stc.getBooking())){
			query.add(Restrictions.eq("booking", stc.getBooking()));
		}
		// 排序
		if (StringUtils.isNotBlank(stc.order)) {
			query.addOrder(Order.desc(stc.order));
		} else {
			query.addOrder(Order.desc("hotvalue"));
			query.addOrder(Order.desc("clickedtimes"));
		}
		List<Theatre> theatreList = hibernateTemplate.findByCriteria(query, from, maxnum);
		return theatreList;
	}
	
	@Override
	public List<Theatre> getTheatreListByUpdateTime(String citycode, Timestamp updatetime){
		DetachedCriteria query = DetachedCriteria.forClass(Theatre.class);
		if(StringUtils.isNotBlank(citycode)) query.add(Restrictions.eq("citycode", citycode));
		query.add(Restrictions.ge("updatetime", updatetime));
		List<Theatre> theatreList = hibernateTemplate.findByCriteria(query);
		return theatreList;
	}
	
	@Override
	public List<TheatreRoom> getTheatreRoomList(Timestamp updatetime, String timefield){
		DetachedCriteria query = DetachedCriteria.forClass(TheatreRoom.class);
		if(updatetime != null) query.add(Restrictions.ge(timefield, updatetime));
		List<TheatreRoom>  roomList = hibernateTemplate.findByCriteria(query);
		return roomList;
	}
	
	@Override
	public List<TheatreRoomSeat> getTheatreRoomSeatList(List<Long> roomList){
		if(roomList.isEmpty()) return new ArrayList<TheatreRoomSeat>();
		DetachedCriteria query = DetachedCriteria.forClass(TheatreRoomSeat.class);
		query.add(Restrictions.in("roomid", roomList));
		query.addOrder(Order.asc("id"));
		List<TheatreRoomSeat> seatList = hibernateTemplate.findByCriteria(query);
		return seatList;
	}
	
	@Override
	public List<OpenDramaItem> getOpenDramItemList(List didList, Timestamp updatetime){
		DetachedCriteria query = DetachedCriteria.forClass(OpenDramaItem.class);
		if(updatetime != null) query.add(Restrictions.ge("updatetime", updatetime));
		if(didList != null && didList.size()>0) query.add(Restrictions.in("dramaid", didList));
		List<OpenDramaItem> odiList = hibernateTemplate.findByCriteria(query);
		return odiList;
	}
	@Override
	public List<TheatreSeatPrice> getSeatPriceList(List dpidList, Timestamp updatetime){
		if(dpidList.isEmpty()) return new ArrayList<TheatreSeatPrice>();
		DetachedCriteria query = DetachedCriteria.forClass(TheatreSeatPrice.class);
		query.add(Restrictions.in("dpid", dpidList));
		query.add(Restrictions.ne("status", Status.DEL));
		query.add(Restrictions.gt("updatetime", updatetime));
		List<TheatreSeatPrice> tspList = hibernateTemplate.findByCriteria(query);
		return tspList;
	}
	
	@Override
	public List<OpenTheatreSeat> getOpenSeatList(List odiidList){
		if(odiidList.isEmpty()) return new ArrayList<OpenTheatreSeat>();
		DetachedCriteria query = DetachedCriteria.forClass(OpenTheatreSeat.class);
		query.add(Restrictions.in("odiid", odiidList));
		List<OpenTheatreSeat> otsList = hibernateTemplate.findByCriteria(query);
		return otsList;
	}
}
