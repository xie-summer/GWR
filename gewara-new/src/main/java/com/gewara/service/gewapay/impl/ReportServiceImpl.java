package com.gewara.service.gewapay.impl;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.criterion.Subqueries;
import org.hibernate.type.StringType;
import org.hibernate.type.Type;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.gewara.constant.GoodsConstant;
import com.gewara.constant.ticket.OrderConstant;
import com.gewara.constant.ticket.RefundConstant;
import com.gewara.model.movie.Cinema;
import com.gewara.model.pay.CinemaSettle;
import com.gewara.model.pay.GoodsOrder;
import com.gewara.model.pay.OrderRefund;
import com.gewara.model.pay.TicketOrder;
import com.gewara.service.gewapay.RefundService;
import com.gewara.service.gewapay.ReportService;
import com.gewara.service.impl.BaseServiceImpl;
import com.gewara.util.BeanUtil;
import com.gewara.util.DateUtil;

@Service("reportService")
public class ReportServiceImpl extends BaseServiceImpl implements ReportService {
	@Autowired@Qualifier("refundService")
	private RefundService refundService;
	@Override
	public List<Map> getTicketOrderDataByPlaytime(Long cinemaId,Long movieId, Timestamp timefrom, Timestamp timeto, String opentype) {
		String hql = "select new map(t.movieid as movieid,t.mpid as mpid, max(t.playtime) as playtime, count(t.id) as totalcount, sum(t.quantity) as quantity, wmconcat(t.costprice) as concatprice, sum(t.costprice*quantity) as totalcost) " +
				"from TicketOrder t where t.status=? and t.cinemaid=? and t.playtime>=? and t.playtime<? ";
		if (StringUtils.isNotBlank(opentype)){
			hql = hql + "and t.category=? ";
		}
		if(movieId != null){
			hql += "and t.movieid=? ";
		}
		hql = hql + "group by t.movieid,t.mpid order by max(t.playtime)";
		List<Map> dataMap = new ArrayList<Map>();
		if (StringUtils.isNotBlank(opentype)){
			if(movieId != null){
				dataMap = hibernateTemplate.find(hql, OrderConstant.STATUS_PAID_SUCCESS, cinemaId, timefrom, timeto, opentype,movieId);
			}else{
				dataMap = hibernateTemplate.find(hql, OrderConstant.STATUS_PAID_SUCCESS, cinemaId, timefrom, timeto, opentype);
			}
		}else{
			if(movieId != null){
				dataMap = hibernateTemplate.find(hql, OrderConstant.STATUS_PAID_SUCCESS, cinemaId, timefrom, timeto,movieId);
			}else{
				dataMap = hibernateTemplate.find(hql, OrderConstant.STATUS_PAID_SUCCESS, cinemaId, timefrom, timeto);
			}
		}
		return dataMap;
	}

	@Override
	public List<Map> getTicketOrderDataByAddtime(Long cinemaId, Long movieId,Timestamp timefrom, Timestamp timeto, String opentype){
		String hql = "select new map(t.movieid as movieid, t.mpid as mpid, count(t.id) as totalcount, sum(t.quantity) as quantity, wmconcat(t.costprice) as concatprice, sum(t.costprice*quantity) as totalcost) from TicketOrder t "
				+ "where t.status=? and t.cinemaid=? and t.addtime>=? and t.addtime<? ";
		if (StringUtils.isNotBlank(opentype)){
			hql += "and t.category=? ";
		}
		if(movieId != null){
			hql += "and t.movieid=? ";
		}
		hql += "group by t.movieid, t.mpid order by t.movieid, t.mpid";
		List<Map> dataMap = new ArrayList<Map>();
		if (StringUtils.isNotBlank(opentype)){
			if(movieId != null){
				dataMap = hibernateTemplate.find(hql, OrderConstant.STATUS_PAID_SUCCESS, cinemaId, timefrom, timeto, opentype,movieId);
			}else{
				dataMap = hibernateTemplate.find(hql, OrderConstant.STATUS_PAID_SUCCESS, cinemaId, timefrom, timeto, opentype);
			}
		}else{
			if(movieId != null){
				dataMap = hibernateTemplate.find(hql, OrderConstant.STATUS_PAID_SUCCESS, cinemaId, timefrom, timeto,movieId);
			}else{
				dataMap = hibernateTemplate.find(hql, OrderConstant.STATUS_PAID_SUCCESS, cinemaId, timefrom, timeto);
			}
		}
		return dataMap;
	}

	@Override
	public List<CinemaSettle> getLastSettleList() {
		String query = "select max(id) from CinemaSettle group by cinemaid ";
		List<Long> idList = hibernateTemplate.find(query);
		List<CinemaSettle> settleList = baseDao.getObjectList(CinemaSettle.class, idList);
		return settleList;
	}
	@Override
	public List<Cinema> getBookingCinemaList(){
		DetachedCriteria query = DetachedCriteria.forClass(Cinema.class);
		query.add(Restrictions.eq("booking", Cinema.BOOKING_OPEN));
		query.setProjection(Projections.id());
		query.addOrder(Order.desc("avggeneral"));
		List<Long> idList = hibernateTemplate.findByCriteria(query);
		List<Cinema> result = baseDao.getObjectList(Cinema.class, idList);
		return result;
	}

	@Override
	public CinemaSettle getLastSettle(Long cinemaid) {
		String query = "select max(id) from CinemaSettle where cinemaid=? ";
		List<Long> idList = hibernateTemplate.find(query, cinemaid);
		if(idList.isEmpty()) return null;
		return baseDao.getObject(CinemaSettle.class, idList.get(0));
	}
	/**
	 * 时间轴
	 * ^----------------------------^------^-----------------------^-------^-------------|
	 * |                            |      |                       |       |
	 * timefrom(last)        timeto(last) curtime(last)            |       |
	 *                              |      |                       |       |
	 *                          timefrom  lasttime              timeto    curtime
	 * @param cinemaid
	 * @param timefrom
	 * @param timeto
	 * @param lasttime
	 * @param curtime
	 * @param model
	 */
	public Map getRefundData(Long cinemaid, Timestamp timefrom/*本次结账周期*/, Timestamp timeto, Timestamp lasttime/*上次结账时间*/, Timestamp curtime/*本次结账时间*/){
		List<OrderRefund> refundList = refundService.getSettleRefundList("ticket", timefrom, curtime, cinemaid);
		List<OrderRefund> lastRefundList = new ArrayList<OrderRefund>();	//上期退款：影院要补给我们钱！
		List<OrderRefund> curRefundList = new ArrayList<OrderRefund>();		//本期退款：从本期金额中扣除差额
		List<OrderRefund> futureRefundList = new ArrayList<OrderRefund>();	//未来退款：应该不存在！！！！！
		Map<String, TicketOrder> orderMap = new HashMap<String, TicketOrder>();//订单
		Integer curRefundQuantity = 0, lastRefundQuantity = 0;
		TicketOrder tmp;
		for(OrderRefund refund: refundList){
			tmp = baseDao.getObjectByUkey(TicketOrder.class, "tradeNo", refund.getTradeno(), false);
			orderMap.put(refund.getTradeno(), tmp);
			if(tmp.getAddtime().before(timefrom)){//上期订单
				if(refund.getRefundtime().after(lasttime) && refund.getRefundtime().before(curtime)){
					//在本期退款的上期订单
					lastRefundList.add(refund);
					lastRefundQuantity += tmp.getQuantity();
				}
			}else if(tmp.getAddtime().after(timeto)){
				//未来订单，忽略
				futureRefundList.add(refund);
			}else{//当前订单
				curRefundList.add(refund);
				curRefundQuantity += tmp.getQuantity();
			}
		}
		Map model = new HashMap();
		model.put("lastRefundList", lastRefundList);
		model.put("curRefundList", curRefundList);
		model.put("futureRefundList", futureRefundList);
		model.put("orderMap", orderMap);
		model.put("curRefundQuantity", curRefundQuantity);
		model.put("lastRefundQuantity", lastRefundQuantity);
		return model;
	}
	@Override
	public Map getCinemaSummaryByPlaytime(Long cinemaId, Timestamp timefrom, Timestamp timeto, Long movieid, String opentype) {
		List params = new ArrayList();
		params.add(OrderConstant.STATUS_PAID_SUCCESS);
		params.add(cinemaId);
		params.add(timefrom);
		params.add(timeto);
		String hql = "select new map(cinemaid as cinemaid, sum(t.quantity) as totalquantity, count(*) as totalcount, sum(costprice * quantity) as totalcost, count(distinct mpid) as mpicount) from TicketOrder t " +
				"where settle='Y' and t.status = ? and t.cinemaid=? and t.playtime>=? and t.playtime<? " ;
		
		if (StringUtils.isNotBlank(opentype)){
			hql += "and t.category=? ";
			params.add(opentype);
		}
		if(movieid!=null){
			hql += "and t.movieid=? ";
			params.add(movieid);
		}
		hql +="group by cinemaid";
		List<Map> dataMap = hibernateTemplate.find(hql, params.toArray());
		if(dataMap.isEmpty()) return null;
		return dataMap.get(0);
	}
	@Override
	public Map getCinemaSummaryByAddtime(Long cinemaId, Timestamp timefrom, Timestamp timeto, Long movieid, String opentype) {
		String hql = "select new map(cinemaid as cinemaid, sum(t.quantity) as totalquantity, count(*) as totalcount, sum(costprice * quantity) as totalcost, count(distinct mpid) as mpicount) from TicketOrder t " +
				"where t.addtime>= ? and t.addtime<? and t.status = ? and settle='Y' and t.cinemaid=? ";
		List params = new ArrayList();
		params.add(timefrom);
		params.add(timeto);
		params.add(OrderConstant.STATUS_PAID_SUCCESS);
		params.add(cinemaId);

		if (StringUtils.isNotBlank(opentype)){
			params.add(opentype);
			hql = hql + "and t.category=? ";
		}
		if(movieid!=null){
			hql += "and t.movieid=? ";
			params.add(movieid);
		}
		hql +="group by cinemaid";
		List<Map> dataMap = hibernateTemplate.find(hql, params.toArray());
		if(dataMap.isEmpty()) return null;
		return dataMap.get(0);
	}
	@Override
	public List<Map> getCinemaSummaryByPlaytime(List<Long> cinemaIds, Timestamp timefrom, Timestamp timeto, Long movieid, String opentype) {
		if(cinemaIds == null || cinemaIds.isEmpty()){
			return new ArrayList<Map>();
		}
		List params = new ArrayList();
		params.add(OrderConstant.STATUS_PAID_SUCCESS);
		params.add(timefrom);
		params.add(timeto);
		String hql = "select new map(cinemaid as cinemaid, sum(t.quantity) as totalquantity, count(*) as totalcount, sum(costprice * quantity) as totalcost, count(distinct mpid) as mpicount) from TicketOrder t " +
				"where settle='Y' and t.status = ? and t.cinemaid in (" + StringUtils.join(cinemaIds, ",") + ") and t.playtime>=? and t.playtime<? " ;
		
		if (StringUtils.isNotBlank(opentype)){
			hql += "and t.category=? ";
			params.add(opentype);
		}
		if(movieid!=null){
			hql += "and t.movieid=? ";
			params.add(movieid);
		}
		hql +="group by cinemaid";
		List<Map> dataMap = hibernateTemplate.find(hql, params.toArray());
		return dataMap;
	}
	@Override
	public List<Map> getCinemaSummaryByAddtime(List<Long> cinemaIds, Timestamp timefrom, Timestamp timeto, Long movieid, String opentype) {
		if(cinemaIds == null || cinemaIds.isEmpty()){
			return new ArrayList<Map>();
		}
		String hql = "select new map(cinemaid as cinemaid, sum(t.quantity) as totalquantity, count(*) as totalcount, sum(costprice * quantity) as totalcost, count(distinct mpid) as mpicount) from TicketOrder t " +
				"where t.addtime>= ? and t.addtime<? and t.status = ? and settle='Y' and t.cinemaid in (" + StringUtils.join(cinemaIds, ",") + ") ";
		List params = new ArrayList();
		params.add(timefrom);
		params.add(timeto);
		params.add(OrderConstant.STATUS_PAID_SUCCESS);

		if (StringUtils.isNotBlank(opentype)){
			params.add(opentype);
			hql = hql + "and t.category=? ";
		}
		if(movieid!=null){
			hql += "and t.movieid=? ";
			params.add(movieid);
		}
		hql +="group by cinemaid";
		List<Map> dataMap = hibernateTemplate.find(hql, params.toArray());
		return dataMap;
	}

	@Override
	public List<GoodsOrder> getCinemaGoodsOrderByTaketime(Long cinemaId, Timestamp timefrom, Timestamp timeto) {
		//只能查询两月内的套餐
		Timestamp minaddtime = DateUtil.addDay(timefrom, -15); 
		String qry = "from GoodsOrder o where o.status=? and o.addtime>=? and o.addtime<=?" +
				"and exists(select g.id from Goods g where g.relatedid=? and g.tag=? and g.id=o.goodsid) " +
				"and exists(select r.tradeno from OrderResult r where r.istake=? and r.taketime>= ? and taketime< ? and r.tradeno=o.tradeNo) " +
				"order by o.addtime desc";
		List<GoodsOrder> orderList = hibernateTemplate.find(qry, OrderConstant.STATUS_PAID_SUCCESS, minaddtime, timeto, cinemaId, GoodsConstant.GOODS_TAG_BMH, "Y", timefrom, timeto);
		return orderList;
	}
	@Override
	public List<GoodsOrder> getCinemaGoodsOrderByAddtime(Long cinemaId, Timestamp timefrom, Timestamp timeto,boolean isTake) {
		String qry = "from GoodsOrder o where o.status=? and o.addtime>=? and o.addtime<? and o.placeid=?" +
				"and exists(select g.id from Goods g where g.relatedid=? and g.tag=? and g.id=o.goodsid) ";
		if(isTake){
			qry += "and exists(select r.tradeno from OrderResult r where r.taketime is not null and r.istake='Y' and r.tradeno=o.tradeNo) ";
		}
		qry += "order by o.addtime desc";
		List<GoodsOrder> orderList = hibernateTemplate.find(qry, OrderConstant.STATUS_PAID_SUCCESS, timefrom, timeto, cinemaId, cinemaId, GoodsConstant.GOODS_TAG_BMH);
		return orderList;
	}

	@Override
	public Map getGoodsSummaryByAddtime(Long cinemaId, Timestamp timefrom, Timestamp timeto,boolean isTake) {
		String qry = "select new map(placeid as cinemaid, count(id) as totalcount, sum(quantity) as totalquantity, sum(costprice) as totalcost) " +
				"from GoodsOrder o where o.status=? and o.addtime>=? and o.addtime<? and o.placeid= ? " +
				"and exists(select g.id from Goods g where g.relatedid=? and g.tag=? and g.id=o.goodsid) ";
		if(isTake){
			qry += "and exists(select r.tradeno from OrderResult r where r.taketime is not null and r.istake='Y' and r.tradeno=o.tradeNo) ";
		}
		qry += "group by o.placeid";
		List<Map> result = hibernateTemplate.find(qry, OrderConstant.STATUS_PAID_SUCCESS, timefrom, timeto, cinemaId, cinemaId, GoodsConstant.GOODS_TAG_BMH);
		if(result.isEmpty()) return null;
		return result.get(0);
	}

	@Override
	public Map getGoodsSummaryByTaketime(Long cinemaId, Timestamp timefrom, Timestamp timeto) {
		//只能查询两月内的套餐
		Timestamp minaddtime = DateUtil.addDay(timefrom, -15); 
		String qry = "select new map(placeid as cinemaid, count(id) as totalcount, sum(quantity) as totalquantity, sum(costprice) as totalcost) " +
				"from GoodsOrder o where o.status=? and o.addtime>=? and o.addtime<? " +
				"and exists(select g.id from Goods g where g.relatedid=? and g.tag=? and g.id=o.goodsid) " +
				"and exists(select r.tradeno from OrderResult r where r.taketime is not null and r.taketime>= ? and taketime<? and r.istake=? and r.tradeno=o.tradeNo) " +
				"group by o.placeid";
		List<Map> result = hibernateTemplate.find(qry, OrderConstant.STATUS_PAID_SUCCESS, minaddtime, timeto, cinemaId, GoodsConstant.GOODS_TAG_BMH, timefrom, timeto, "Y");
		if(result.isEmpty()) return null;
		return result.get(0);
	}

	@Override
	public Map getRefundOrderData(Long cinemaId, Long movieId,Timestamp timefrom, Timestamp timeto, String timeType) {
		DetachedCriteria subQuery = DetachedCriteria.forClass(OrderRefund.class);
		subQuery.add(Restrictions.eq("ordertype", "ticket"));
		subQuery.add(Restrictions.eq("placeid",cinemaId));
		subQuery.add(Restrictions.or(Restrictions.eq("status", RefundConstant.STATUS_FINISHED), Restrictions.eq("status", RefundConstant.STATUS_SUCCESS)));
		subQuery.add(Restrictions.eq("orderstatus", OrderConstant.STATUS_PAID_SUCCESS));
		if("addtime".equals(timeType)){
			subQuery.add(Restrictions.ge("addtime", timefrom));
			subQuery.add(Restrictions.le("addtime", timeto));
		}else{
			subQuery.add(Restrictions.ge("refundtime", timefrom));
			subQuery.add(Restrictions.le("refundtime", timeto));
		}
		subQuery.setProjection(Projections.property("tradeno"));
		DetachedCriteria reFund = DetachedCriteria.forClass(TicketOrder.class).add(Subqueries.propertyIn("tradeNo", subQuery));
		reFund.add(Restrictions.eq("settle", "Y"));
		if(movieId != null){
			reFund.add(Restrictions.eq("movieid", movieId));
		}
		reFund.setProjection(Projections.projectionList().add(Projections.count("id").as("orderCount"))
				.add(Projections.sum("quantity").as("quantity"))
				.add(Projections.sqlProjection("sum(costprice*quantity) as totalcost", new String[]{"totalcost"}, new Type[]{ StringType.INSTANCE})));
		reFund.setResultTransformer(DetachedCriteria.ALIAS_TO_ENTITY_MAP);
		List<Map> result = hibernateTemplate.findByCriteria(reFund);
		Map resultMap = new HashMap();
		if(result.isEmpty() || (Long)result.get(0).get("orderCount") == 0){
			return null;
		}
		resultMap.putAll(result.get(0));
		DetachedCriteria allQuery = DetachedCriteria.forClass(TicketOrder.class);
		if("addtime".equals(timeType)){
			allQuery.add(Restrictions.ge("addtime", timefrom));
			allQuery.add(Restrictions.le("addtime", timeto));
		}else if("playtime".equals(timeType)){
			allQuery.add(Restrictions.ge("playtime", timefrom));
			allQuery.add(Restrictions.le("playtime", timeto));
		}else{
			allQuery.add(Restrictions.ge("addtime", timefrom));
			allQuery.add(Restrictions.le("addtime", DateUtil.addDay(timeto, 2)));
		}
		allQuery.add(Restrictions.eq("settle", "Y"));
		if(movieId != null){
			allQuery.add(Restrictions.eq("movieid", movieId));
		}
		allQuery.add(Restrictions.eq("cinemaid", cinemaId));
		allQuery.add(Restrictions.or(Restrictions.eq("status", OrderConstant.STATUS_PAID_RETURN),Restrictions.eq("status", OrderConstant.STATUS_PAID_SUCCESS)));
		allQuery.setProjection(Projections.projectionList().add(Projections.groupProperty("movieid").as("movieid"))
				.add(Projections.groupProperty("mpid").as("mpid"))
				.add(Projections.count("id").as("orderCount")));
		allQuery.setResultTransformer(DetachedCriteria.ALIAS_TO_ENTITY_MAP);
		result = hibernateTemplate.findByCriteria(allQuery);
		resultMap.put("totalCount", 0);
		resultMap.put("mpidCount", BeanUtil.getBeanPropertyList(result, "mpid", true).size());
		resultMap.put("movieIds", BeanUtil.getBeanPropertyList(result, "movieid", true));
		for(Map map : result){
				resultMap.put("totalCount", Integer.parseInt(map.get("orderCount").toString()) + (Integer)resultMap.get("totalCount"));
		}
		return resultMap;
	}
}
