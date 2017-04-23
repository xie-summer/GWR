package com.gewara.service.impl;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.criterion.Subqueries;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.gewara.constant.GoodsConstant;
import com.gewara.constant.Status;
import com.gewara.constant.TagConstant;
import com.gewara.constant.TerminalConstant;
import com.gewara.constant.sys.MongoData;
import com.gewara.constant.ticket.OrderConstant;
import com.gewara.constant.ticket.OrderNoteConstant;
import com.gewara.helper.order.GewaOrderHelper;
import com.gewara.json.CustomPaper;
import com.gewara.model.api.OrderResult;
import com.gewara.model.api.Synch;
import com.gewara.model.drama.OpenDramaItem;
import com.gewara.model.goods.Goods;
import com.gewara.model.movie.Cinema;
import com.gewara.model.movie.CinemaProfile;
import com.gewara.model.pay.GewaOrder;
import com.gewara.model.pay.GoodsOrder;
import com.gewara.model.pay.OrderNote;
import com.gewara.model.pay.SportOrder;
import com.gewara.model.pay.TicketOrder;
import com.gewara.model.ticket.OpenPlayItem;
import com.gewara.model.user.Member;
import com.gewara.mongo.MongoService;
import com.gewara.pay.PayUtil;
import com.gewara.service.SynchService;
import com.gewara.support.ErrorCode;
import com.gewara.untrans.order.BroadcastOrderService;
import com.gewara.untrans.terminal.TerminalService;
import com.gewara.util.BeanUtil;
import com.gewara.util.DateUtil;
import com.gewara.util.JsonUtils;
import com.gewara.xmlbind.terminal.TakeInfo;

@Service("synchService")
public class SynchServiceImpl  extends BaseServiceImpl implements SynchService{
	
	@Autowired@Qualifier("mongoService")
	private MongoService mongoService;
	@Autowired@Qualifier("broadcastOrderService")
	private BroadcastOrderService broadcastOrderService;
	@Autowired@Qualifier("terminalService")
	private TerminalService terminalService;
	@Override
	public List<OrderResult> getToDoOrderList(String citycode) {
		Timestamp curtime = new Timestamp(System.currentTimeMillis());
		Timestamp time = DateUtil.addDay(curtime, -7);
		String sql = "from OrderResult r where r.result=? and exists" +
				"(select g.id from GewaOrder g where g.tradeNo=r.tradeno and g.status=? and g.citycode=? and g.addtime>? and (g.tradeNo like ? or g.tradeNo like ?) )";
		//单独购买附属品
		List<OrderResult> orderResultList = hibernateTemplate.find(sql, "N", OrderConstant.STATUS_PAID_SUCCESS, citycode, time, PayUtil.FLAG_TICKET + "%", PayUtil.FLAG_GOODS + "%");
		return orderResultList;
	}
	@Override
	public Map<String, Long> getGewaNumByTimeCinema(final Long cinemaid, Timestamp statrtime, Timestamp endtime) {
		Map<String, Long> result = new HashMap<String, Long>();
		result.put("count", 0L);
		result.put("quantity", 0L);
		final String hql = "select new map(count(*) as count, sum(t.quantity) as quantity) from TicketOrder t where t.status =? and t.cinemaid =? and t.playtime>=? and t.playtime<=?";
		List<Map> list = hibernateTemplate.find(hql, OrderConstant.STATUS_PAID_SUCCESS, cinemaid, statrtime, endtime );
		if(list.isEmpty()) return result;
		return list.get(0);
	}
	
	@Override
	public Map<String, Long> getSynchNumByTimeCinema(final Long cinemaid, Timestamp starttime, Timestamp endtime) {
		Map<String, Long> result = new HashMap<String, Long>();
		result.put("count", 0L);result.put("ticketnum", 0L);
		final String hql = "select new map(count(*) as count, sum(s.ticketnum) as ticketnum) from OrderResult s where s.ordertype=:ordertype and s.taketime is not null and s.tradeno in ( " +
										"select t.tradeNo from TicketOrder t " +
										"where t.status =:status and t.cinemaid =:cinemaid " +
										"and t.playtime>=:starttime and t.playtime<=:endtime)";
		List<Map> list = queryByNameParams(hql, 0, 50000, "ordertype,status,cinemaid,starttime,endtime",
				OrderResult.ORDERTYPE_TICKET, OrderConstant.STATUS_PAID_SUCCESS, cinemaid, starttime, endtime);
		if(list.isEmpty()) return result;
		return list.get(0);
	}
	
	@Override
	public List<OpenPlayItem> getSynchPeakOpi(Long cinemaId,Timestamp starttime, Timestamp endtime){
		DetachedCriteria query = DetachedCriteria.forClass(OpenPlayItem.class);
		query.add(Restrictions.eq("cinemaid", cinemaId));
		query.add(Restrictions.ge("playtime", starttime));
		query.add(Restrictions.le("playtime", endtime));
		query.add(Restrictions.ge("gsellnum", 300));
		query.addOrder(Order.desc("playtime"));
		return this.hibernateTemplate.findByCriteria(query);
	}
	
	@Override
	public List<OpenPlayItem> getSynchPeakOdi(Long id,Timestamp starttime, Timestamp endtime){
		DetachedCriteria query = DetachedCriteria.forClass(OpenDramaItem.class);
		query.add(Restrictions.eq("theatreid", id));
		query.add(Restrictions.ge("playtime", starttime));
		query.add(Restrictions.le("playtime", endtime));
		query.add(Restrictions.ge("gsellnum", 300));
		query.addOrder(Order.desc("playtime"));
		return this.hibernateTemplate.findByCriteria(query);
	}
	
	public List<Map> getSynchPeakPeriod(Long id,Timestamp starttime, Timestamp endtime,String tag){
		String hql = "select new map(to_char(o.playtime,'yyyy-MM-dd HH24') as pTime, sum(o.gsellnum) as ticketnum) from ";
		if(Synch.TGA_CINEMA.equals(tag)){
			hql += "OpenPlayItem o where o.cinemaid = :placeId and " ;
		}else{
			hql += "OpenDramaItem o where o.theatreid = :placeId and " ;
		}
		hql += " o.playtime>=:starttime and o.playtime<=:endtime group by to_char(o.playtime,'yyyy-MM-dd HH24') having sum(o.gsellnum) > 400";
		List<Map> list = queryByNameParams(hql, 0, 50000, "placeId,starttime,endtime",
				id, starttime, endtime);
		return list;
	}

	@Override
	public List<GewaOrder> getNotGetOrderByTimeCinema(Timestamp starttime, Timestamp endtime, Long cinemaid) {
			final String hql = "from TicketOrder t " +
			"where t.status =? and t.cinemaid = ? " +
			"and t.playtime>=? and t.playtime<? " +
			"and not exists  ("+
			"select tradeno from OrderResult s  where s.ordertype=? and t.tradeNo = s.tradeno and s.taketime is not null " +
			") " +
			"order by t.mpid";
			List list = hibernateTemplate.find(hql, OrderConstant. STATUS_PAID_SUCCESS, cinemaid, starttime, endtime, OrderResult.ORDERTYPE_TICKET);
			return list;
	}
	
	@Override
	public Integer getTicketGetNumByTimeCinema(Timestamp starttime,
			Timestamp endtime, Long cinemaid) {
		final String hql = "select sum(s.ticketnum) from OrderResult s where s.ordertype=? and s.taketime >=? and s.taketime <? and s.placeid = ?";
		List list = hibernateTemplate.find(hql, OrderResult.ORDERTYPE_TICKET, starttime, endtime, cinemaid);
		if(list.get(0) == null) return 0;
		return Integer.parseInt(list.get(0)+"");
	}

	@Override
	public Map<String,Long> getSynchTotalOrderNumByTimeCinema(final Long cinemaid, Timestamp starttime, Timestamp endtime) {
		final String hql = "select new map(count(*) as count, sum(s.ticketnum) as ticketnum) from OrderResult s where s.ordertype=:ordertype and s.tradeno in ( " +
										"select t.tradeNo from TicketOrder t " +
										"where t.status =:status and t.cinemaid =:cinemaid " +
										"and t.playtime>=:starttime and t.playtime<=:endtime" +
										")";
		
		List<Map> list = queryByNameParams(hql, 0, 50000, "ordertype,status,cinemaid,starttime,endtime", 
				OrderResult.ORDERTYPE_TICKET, OrderConstant.STATUS_PAID_SUCCESS, cinemaid, starttime, endtime);
		if(list.isEmpty()){
			Map<String, Long> result = new HashMap<String, Long>();
			result.put("count", 0l);
			result.put("ticketnum", 0l);
			return result;
		}
		return list.get(0);
	}

	@Override
	public List<GewaOrder> getNoSynchOrderByCinema(Long cinemaid) {
		Timestamp curtime = new Timestamp(System.currentTimeMillis());
		final String hql = "from TicketOrder t " +
			"where t.status =? and t.cinemaid = ? " +
			"and t.playtime>? and not exists  ("+
			"select tradeno from OrderResult s  where s.ordertype=? and t.tradeNo = s.tradeno" +
			") " +
			"order by t.mpid";
		List list = hibernateTemplate.find(hql, OrderConstant.STATUS_PAID_SUCCESS, cinemaid, curtime, OrderResult.ORDERTYPE_TICKET);
		return list;
	}

	@Override
	public List<Synch> getSynchListByCityCode(String citycode, Class clazz) {
		DetachedCriteria query = DetachedCriteria.forClass(Synch.class, "s");
		query.add(Restrictions.eq("s.monitor", Status.Y));
		
		DetachedCriteria subquery = DetachedCriteria.forClass(clazz, "c");
		subquery.add(Restrictions.eq("c.citycode", citycode));
		subquery.add(Restrictions.eq("c.booking", Cinema.BOOKING_OPEN));
		subquery.add(Restrictions.eqProperty("s.cinemaid", "c.id"));
		subquery.setProjection(Projections.property("c.id"));
		
		query.add(Subqueries.exists(subquery));
		query.add(Restrictions.ge("s.successtime",DateUtil.addDay(DateUtil.getMillTimestamp(), -1)));
		List<Synch> synchList = hibernateTemplate.findByCriteria(query);
		return synchList;
	}
	
	@Override
	public List<Synch> getOffNetworkSynchListByCityCode(String citycode, Class clazz){
		DetachedCriteria query = DetachedCriteria.forClass(Synch.class, "s");
		query.add(Restrictions.eq("s.monitor", Status.Y));
		
		DetachedCriteria subquery = DetachedCriteria.forClass(clazz, "c");
		subquery.add(Restrictions.eq("c.citycode", citycode));
		subquery.add(Restrictions.eq("c.booking", Cinema.BOOKING_OPEN));
		subquery.add(Restrictions.eqProperty("s.cinemaid", "c.id"));
		subquery.setProjection(Projections.property("c.id"));
		
		query.add(Subqueries.exists(subquery));
		query.add(Restrictions.lt("s.successtime", DateUtil.addMinute(DateUtil.getMillTimestamp(), -10)));
		List<Synch> synchList = hibernateTemplate.findByCriteria(query);
		return synchList;
	}
	@Override
	public List<GoodsOrder> getGoodsOrderListByRelatedidAndLasttime(Long relatedid, Timestamp lasttime,String goodsTag) {
		DetachedCriteria query = DetachedCriteria.forClass(GoodsOrder.class, "o");
		query.add(Restrictions.eq("o.status", OrderConstant.STATUS_PAID_SUCCESS));
		if (lasttime!=null) {
			query.add(Restrictions.ge("o.modifytime", lasttime));
		} else {
			List<Long> goodsidList = getGoodsidListByRelatedid(relatedid);
			if(goodsidList.size()==0) return new ArrayList<GoodsOrder>();
			query.add(Restrictions.in("goodsid", goodsidList));
		}
		DetachedCriteria subquery = DetachedCriteria.forClass(Goods.class, "g");
		subquery.add(Restrictions.eq("g.tag", goodsTag));
		subquery.add(Restrictions.eq("g.relatedid", relatedid));
		subquery.add(Restrictions.eqProperty("g.id", "o.goodsid"));
		subquery.setProjection(Projections.property("g.id"));
		query.add(Subqueries.exists(subquery));
		List<GoodsOrder> orderlist = hibernateTemplate.findByCriteria(query);
		return orderlist;
	}
	
	@Override
	public List<GoodsOrder> getGoodsOrderListByPlaceidAndLasttime(Long placeid, Timestamp lasttime, String category) {
		DetachedCriteria query = DetachedCriteria.forClass(GoodsOrder.class);
		query.add(Restrictions.eq("placeid", placeid));
		query.add(Restrictions.eq("status", OrderConstant.STATUS_PAID_SUCCESS));
		query.add(Restrictions.ge("modifytime", lasttime));
		query.add(Restrictions.eq("category", category));
		List<GoodsOrder> orderlist = hibernateTemplate.findByCriteria(query);
		return orderlist;
	}
	
	private List<Long> getGoodsidListByRelatedid(Long relatedid) {
		DetachedCriteria query = DetachedCriteria.forClass(GoodsOrder.class, "o");
		query.add(Restrictions.eq("o.status", OrderConstant.STATUS_PAID_SUCCESS));
		query.add(Restrictions.ge("o.validtime", new Timestamp(System.currentTimeMillis())));
		DetachedCriteria subquery = DetachedCriteria.forClass(Goods.class, "g");
		subquery.add(Restrictions.eq("g.tag", GoodsConstant.GOODS_TAG_BMH));
		subquery.add(Restrictions.eq("g.relatedid", relatedid));
		subquery.add(Restrictions.eqProperty("g.id", "o.goodsid"));
		subquery.setProjection(Projections.property("g.id"));
		query.add(Subqueries.exists(subquery));
		query.setProjection(Projections.property("o.goodsid"));
		List<Long> goodsidList = hibernateTemplate.findByCriteria(query);
		return goodsidList;
	}

	@Override
	public List<OrderNote> getOrderNoteList(Long placeid, Timestamp lasttime, int pageSize) {
		DetachedCriteria query = DetachedCriteria.forClass(OrderNote.class, "o");
		query.add(Restrictions.eq("o.placeid", placeid));
		query.add(Restrictions.eq("o.status", OrderNoteConstant.STATUS_P));
		if (lasttime==null) lasttime = DateUtil.addDay(new Timestamp(System.currentTimeMillis()), -7);
		
		//query.add(Restrictions.or(Restrictions.isNull("result"), Restrictions.ne("result", Status.DEL)));
		query.add(Restrictions.eq("express", Status.N));
		query.add(Restrictions.ge("modifytime", lasttime));
		query.addOrder(Order.desc("addtime"));
		List<OrderNote> orderlist = hibernateTemplate.findByCriteria(query, 0, pageSize);
		return orderlist;
	}
	@Override
	public List<SportOrder> getOrderListBySportIdAndLasttime(Long sportid, Long sportitemid, Timestamp lasttime) {
		DetachedCriteria query = DetachedCriteria.forClass(SportOrder.class);
		query.add(Restrictions.eq("sportid", sportid));
		if (sportitemid!=null) query.add(Restrictions.eq("itemid", sportitemid));
		query.add(Restrictions.eq("status", OrderConstant.STATUS_PAID_SUCCESS));
		if (lasttime==null) lasttime = DateUtil.addDay(new Timestamp(System.currentTimeMillis()), -7);
		query.add(Restrictions.ge("modifytime", lasttime));
		List<SportOrder> orderlist = hibernateTemplate.findByCriteria(query);
		return orderlist;
	}

	@Override
	public List<TicketOrder> getOrderListByCinemaIdAndLasttime(Long cinemaid, Timestamp lasttime) {
		Timestamp curtime = DateUtil.getCurFullTimestamp();
		DetachedCriteria query = DetachedCriteria.forClass(TicketOrder.class);
		query.add(Restrictions.eq("cinemaid", cinemaid));
		query.add(Restrictions.eq("status", OrderConstant.STATUS_PAID_SUCCESS));
		if (lasttime==null) {
			lasttime = DateUtil.addDay(curtime, -7);
		}
		query.add(Restrictions.ge("modifytime", lasttime));
		query.add(Restrictions.gt("playtime", DateUtil.addDay(curtime, -1)));
		List<TicketOrder> orderlist = hibernateTemplate.findByCriteria(query);
		return orderlist;
	}
	@Override
	public List<TicketOrder> getOrderListByCinemaIdAndLasttime(Long cinemaid, Timestamp lasttime,int pageSize) {
		Timestamp curtime = DateUtil.getCurFullTimestamp();
		DetachedCriteria query = DetachedCriteria.forClass(TicketOrder.class);
		query.add(Restrictions.eq("cinemaid", cinemaid));
		query.add(Restrictions.eq("status", OrderConstant.STATUS_PAID_SUCCESS));
		if (lasttime==null) lasttime = DateUtil.addDay(new Timestamp(System.currentTimeMillis()), -7);
		query.add(Restrictions.ge("modifytime", lasttime));
		query.add(Restrictions.gt("playtime", DateUtil.addDay(curtime, -1)));
		query.addOrder(Order.asc("modifytime"));
		List<TicketOrder> orderlist = hibernateTemplate.findByCriteria(query,0,pageSize);
		return orderlist;
	}

	@Override
	public Map<String, Long> getGewaNumByTimeSport(final Long sportid, Timestamp starttime, Timestamp endtime) {
		Map<String, Long> result = new HashMap<String, Long>();
		result.put("count", 0L);result.put("quantity", 0L);
		final String hql = "select new map(count(*) as count, sum(t.quantity) as quantity) from SportOrder " +
				"t where t.status =:status and t.ottid " +
				" in(select s.id from OpenTimeTable s where s.sportid=:sportid and s.playdate>=:starttime and s.playdate<=:endtime)";
		List<Map> list = queryByNameParams(hql, 0, 50000, "status,sportid,starttime,endtime", 
				OrderConstant.STATUS_PAID_SUCCESS, sportid, starttime,endtime);
		if(list.isEmpty()) return result;
		return list.get(0);
	}
	
	@Override
	public Integer getSynchTotalOrderNumByTimeSport(final Long sportid, Timestamp starttime, Timestamp endtime) {
		final String hql = "select count(*) from OrderResult s where s.ordertype=:ordertype and s.tradeno in ( " +
										"select t.tradeNo from SportOrder t " +
										"where t.status =:status " +
										"and t.ottid in(select s.id from OpenTimeTable s where s.sportid=:sportid and s.playdate>=:starttime and s.playdate<=:endtime)" +
										") " +
										"order by s.taketime desc";
		
		List list = queryByNameParams(hql, 0, 50000, "ordertype,status,sportid,starttime,endtime", 
				OrderResult.ORDERTYPE_SPORT, OrderConstant.STATUS_PAID_SUCCESS, sportid, starttime,endtime);
		if(list.isEmpty()) return 0;
		return Integer.parseInt(list.get(0)+"");
	}
	
	@Override
	public List<GewaOrder> getNotGetOrderByTimeSport(Long sportid,Timestamp starttime,Timestamp endtime) {
			final String hql = "from SportOrder t " +
			"where t.status =? and t.sportid = ? " +
			"and t.ottid in (select s.id from OpenTimeTable s where s.playdate>=? and s.playdate<=?) " +
			"and not exists  ("+
			"select tradeno from OrderResult s  where s.ordertype=? and t.tradeNo = s.tradeno and s.taketime is not null " +
			") " +
			"order by t.ottid";
			List list = hibernateTemplate.find(hql, OrderConstant. STATUS_PAID_SUCCESS, sportid, starttime, endtime, OrderResult.ORDERTYPE_SPORT);
			return list;
	}
	
	@Override
	public List<GewaOrder> getNoSynchOrderBySport(Long sportid,Timestamp starttime,Timestamp endtime) {
		final String hql = "from SportOrder t " +
			"where t.status =? and t.sportid = ? " +
			"and t.ottid in (select s.id from OpenTimeTable s where s.playdate>=? and s.playdate<=?) " +
			"and not exists  ("+
			"select tradeno from OrderResult s  where s.ordertype=? and t.tradeNo = s.tradeno" +
			") " +
			"order by t.ottid";
		List list = hibernateTemplate.find(hql, OrderConstant.STATUS_PAID_SUCCESS, sportid, starttime, endtime,OrderResult.ORDERTYPE_SPORT);
		return list;
	}
	
	@Override
	public List<OrderResult> getToDoSportOrderList(String tradeno) {
		final String hql = "from OrderResult t " +
				"where t.tradeno =? and  exists  ("+
				"select  s.tradeNo from SportOrder s  where t.tradeno = s.tradeNo" +
				") ";
		List list = hibernateTemplate.find(hql, tradeno);
			return list;
	}
	@Override
	public List<Map<String, Object>> getOrderListByPlaceidAndLasttime(Long placeid, Timestamp lasttime) {
		String sql = "select trade_no as tradeno, quantity as quantity from webdata.ticket_order t where t.status=? and t.cinemaid=? and t.modifytime>=?";
		List<Map<String, Object>> qryMap = jdbcTemplate.queryForList(sql, OrderConstant.STATUS_PAID_SUCCESS, placeid, lasttime);
		return qryMap;
	}
	
	@Override
	public List<String> getSerialnoList(String tradenos) {
		List<String> result = new ArrayList<String>();
		if(StringUtils.isBlank(tradenos)) return result;
		List<String> allColl = Arrays.asList(tradenos.split(","));
		String hql = "select serialno from OrderNote where result=(:result) and serialno in(:serialno)";
		List<List<String>> groups = BeanUtil.partition(allColl, 500);
		
		for(List<String> group : groups){
			List<String> sstr = queryByNameParams(hql, 0, 500, "result,serialno", Status.Y, group);
			result.addAll(sstr);
		}
		return result;
	}
	@Override
	public ErrorCode selfTicket(String tradeNo, Member member, String specialComents) {
		if (StringUtils.isBlank(specialComents) || StringUtils.length(specialComents) > 15){
			return ErrorCode.getFailure("定制类容不符合长度！");
		}
		if(member==null) return  ErrorCode.getFailure("请登录！");
		ErrorCode<String> code = GewaOrderHelper.validGreetings(specialComents);
		if(!code.isSuccess()){
			return ErrorCode.getFailure(code.getMsg());
		}
		specialComents = code.getRetval();
		GewaOrder gorder = baseDao.getObjectByUkey(GewaOrder.class, "tradeNo", tradeNo);
		if(gorder == null) return ErrorCode.getFailure("订单信息不存在！");
		if(!gorder.isAllPaid()){
			return ErrorCode.getFailure("订单不是成功订单！");
		}
		if(!DateUtil.isAfter(gorder.getPlaytime())){
			return ErrorCode.getFailure("订单已经过期！！");
		}
		if (!gorder.getMemberid().equals(member.getId())) return ErrorCode.getFailure("不能修改他人的订单！");
		Map<String,Object> params = new HashMap<String,Object>();
		params.put("tradeno", tradeNo);
		params.put("memberid", member.getId());
		params.put("tag", TagConstant.TAG_CINEMA);
		int paperCount = mongoService.getObjectCount(CustomPaper.class, params);
		if (paperCount > 0){
			return ErrorCode.getFailure("您已自定义过票纸内容！");
		}
		CinemaProfile profile = baseDao.getObject(CinemaProfile.class, ((TicketOrder)gorder).getCinemaid());
		if(!profile.hasDefinePaper()){
			return ErrorCode.getFailure("该影院不支持自定义票纸内容！");
		}
		OrderResult orderResult = baseDao.getObject(OrderResult.class, gorder.getTradeNo());
		if(orderResult != null){
			orderResult.setResult("Y");
			orderResult.setUpdatetime(DateUtil.getMillTimestamp());
			baseDao.saveObject(orderResult);
		}
		CustomPaper cp = new CustomPaper(tradeNo, TagConstant.TAG_CINEMA, member.getId(), specialComents);
		mongoService.saveOrUpdateObject(cp, MongoData.DEFAULT_ID_NAME);
		broadcastOrderService.broadcastOrder(gorder, TerminalConstant.FLAG_PAPER, "", null);
		return ErrorCode.SUCCESS;
	}
	
	@Override
	public Map<String, Long> getOrderNoteCountByPlaceid(Long placeid, Timestamp starttime, Timestamp endtime) {
		String hql = "select new map(count(*) as count, sum(s.ticketnum) as quantity) from OrderNote s where s.status =? and s.placeid=? and s.playtime>=? and s.playtime<=?";
		List<Map> list = hibernateTemplate.find(hql, OrderNoteConstant.STATUS_P, placeid, starttime, endtime );
		return list.get(0);
	}
	
	@Override
	public Map<String, Long> getOrderNoteSynchNumByPlaceid(Long placeid, Timestamp starttime, Timestamp endtime) {
		String hql = "select new map(count(*) as count, sum(s.ticketnum) as ticketnum) " +
				"from OrderNote s where s.taketime is not null and s.status=? and s.placeid=? and s.playtime>=? and s.playtime<=?";
		List<Map> list = hibernateTemplate.find(hql, OrderNoteConstant.STATUS_P, placeid, starttime, endtime );
		return list.get(0);
	}
	
	@Override
	public Integer getOrderNoteGetNumByPlaceid(Long placeid, Timestamp starttime, Timestamp endtime) {
		final String hql = "select sum(s.ticketnum) from OrderNote s where s.status=? and s.taketime >=? and s.taketime <? and s.placeid = ?";
		List list = hibernateTemplate.find(hql, OrderNoteConstant.STATUS_P, starttime, endtime, placeid);
		if(list.get(0) == null) return 0;
		return Integer.parseInt(list.get(0)+"");
	}
	
	@Override
	public Map<String, Long> getOrderNoteNumByPlaceid(Long placeid, Timestamp starttime, Timestamp endtime) {
		final String hql = "select new map(count(*) as count, sum(s.ticketnum) as quantity) from OrderNote s where s.status=? and s.placeid =? and s.playtime>=? and s.playtime<=?";
		List<Map> list = hibernateTemplate.find(hql, OrderNoteConstant.STATUS_P, placeid, starttime, endtime );
		return list.get(0);
	}
	
	@Override
	public Map<String,Long> getOrderNoteSynchTotalNumByPlaceid(Long placeid, Timestamp starttime, Timestamp endtime) {
		final String hql = "select new map(count(*) as count, sum(s.ticketnum) as ticketnum) from OrderNote s where s.status =? and (result=? or result=?) and s.placeid =? and s.playtime>=? and s.playtime<=?";
		List<Map> list = hibernateTemplate.find(hql, OrderNoteConstant.STATUS_P, "S","Y", placeid, starttime, endtime );
		return list.get(0);
	}
	
	@Override
	public List<OrderNote> getOrderNoteNotGetOrderByPlaceid(Timestamp starttime, Timestamp endtime, Long placeid) {
			String hql = "from OrderNote s where s.status=? and s.placeid=? and s.taketime is null and s.addtime>? order by addtime desc";
			List<OrderNote> list = hibernateTemplate.find(hql, OrderNoteConstant.STATUS_P, placeid, DateUtil.addDay(DateUtil.getMillTimestamp(), -60));
			return list;
	}
	@Override
	public List<OrderNote> getOrderNoteNoSynchOrderByPlaceid(Long placeid) {
		String hql = "from OrderNote s where (s.result is null or s.result=?) and s.status=? and s.placeid=? and s.taketime is null and s.addtime>? order by addtime desc";
		List<OrderNote> list = hibernateTemplate.find(hql, Status.N, OrderNoteConstant.STATUS_P, placeid, DateUtil.addDay(DateUtil.getMillTimestamp(), -60));
		return list;
	}
	@Override
	public Timestamp getTakeTime(GewaOrder order) {
		String time = JsonUtils.getJsonValueByKey(order.getOtherinfo(), "taketime");
		if(StringUtils.isNotBlank(time)){
			return DateUtil.parseTimestamp(time);
		}
		TakeInfo ti = terminalService.getTakeInfo(order);
		if(ti!=null && ti.getTaketime()!=null){
			String otherinfo = JsonUtils.addJsonKeyValue(order.getOtherinfo(), "taketime", DateUtil.formatTimestamp(ti.getTaketime()));
			order.setOtherinfo(otherinfo);
			baseDao.saveObject(order);
			return ti.getTaketime();
		}
		return null;
	}
}