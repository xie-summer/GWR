package com.gewara.service.order.impl;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.hibernate.criterion.Conjunction;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Disjunction;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projection;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.criterion.Subqueries;
import org.hibernate.type.StringType;
import org.hibernate.type.Type;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.gewara.command.OrderParamsCommand;
import com.gewara.command.SearchOrderCommand;
import com.gewara.constant.PaymethodConstant;
import com.gewara.constant.TagConstant;
import com.gewara.constant.app.AppConstant;
import com.gewara.constant.sys.CacheConstant;
import com.gewara.constant.ticket.OrderConstant;
import com.gewara.json.AppSourceCount;
import com.gewara.json.MemberStats;
import com.gewara.model.api.CooperUser;
import com.gewara.model.drama.DramaOrder;
import com.gewara.model.goods.BaseGoods;
import com.gewara.model.movie.Cinema;
import com.gewara.model.movie.Movie;
import com.gewara.model.pay.Discount;
import com.gewara.model.pay.GewaOrder;
import com.gewara.model.pay.GoodsOrder;
import com.gewara.model.pay.GymOrder;
import com.gewara.model.pay.MemberCardOrder;
import com.gewara.model.pay.OrderExtra;
import com.gewara.model.pay.PubSaleOrder;
import com.gewara.model.pay.SportOrder;
import com.gewara.model.pay.TicketOrder;
import com.gewara.service.impl.BaseServiceImpl;
import com.gewara.service.order.OrderQueryService;
import com.gewara.support.ReadOnlyTemplate;
import com.gewara.untrans.CacheService;
import com.gewara.untrans.MemberCountService;
import com.gewara.untrans.hbase.HBaseService;
import com.gewara.util.BeanUtil;
import com.gewara.util.DateUtil;


/**
 * 订单查询模块
 * @author acerge(acerge@163.com)
 * @since 7:44:11 PM Mar 16, 2011
 */
@Service("orderQueryService")
public class OrderQueryServiceImpl extends BaseServiceImpl implements OrderQueryService {
	@Autowired@Qualifier("readOnlyTemplate")
	private ReadOnlyTemplate readOnlyTemplate;
	public void setReadOnlyHibernateTemplate(ReadOnlyTemplate readOnlyTemplate) {
		this.readOnlyTemplate = readOnlyTemplate;
	}	
	@Autowired@Qualifier("memberCountService")
	private MemberCountService memberCountService;
	
	@Autowired@Qualifier("hbaseService")
	private HBaseService hbaseService;
	
	@Autowired@Qualifier("cacheService")
	private CacheService cacheService;
	
	@Override
	public List<String> getTradeNoListByMpid(String orderType, Long id, String status){
		DetachedCriteria query = null ;
		if(StringUtils.equals(orderType, TagConstant.TAG_MOVIE)){
			query = DetachedCriteria.forClass(TicketOrder.class);
			query.add(Restrictions.eq("mpid", id));
		}else if(StringUtils.equals(orderType, TagConstant.TAG_SPORT)){
			query = DetachedCriteria.forClass(SportOrder.class);
			query.add(Restrictions.eq("ottid", id));
		}else if(StringUtils.equals(orderType, TagConstant.TAG_DRAMA)){
			query = DetachedCriteria.forClass(DramaOrder.class);
			query.add(Restrictions.eq("dpid", id));
		}
		query.add(Restrictions.eq("status", status));
		query.setProjection(Projections.property("tradeNo"));
		List<String> orderList = readOnlyTemplate.findByCriteria(query);
		return orderList;
	}
	@Override
	public List<TicketOrder> getPaidUnfixOrderList(int from, int maxnum){
		//paidFailure、8小时之内支付的、updatetime>cur-5min 的订单
		String query = "from TicketOrder t where t.status = ? and t.paidtime > ? and t.updatetime < ? and t.changehis not like ? order by t.paidtime";
		Timestamp cur = new Timestamp(System.currentTimeMillis());
		List<TicketOrder> failureList = queryByRowsRange(query, from, maxnum, OrderConstant.STATUS_PAID_UNFIX, 
				DateUtil.addHour(cur, -8), DateUtil.addMinute(cur, - 10), "%" + OrderConstant.CHANGEHIS_KEY_CHANGESEAT + "%");
		return failureList;
	}
	@Override
	public int getPaidFailureOrderCount(){
		String query = "select count(t.id) from TicketOrder t where t.status like ? and t.paidtime > ? and t.updatetime < ? order by t.paidtime";
		Timestamp cur = new Timestamp(System.currentTimeMillis());
		List result = hibernateTemplate.find(query, OrderConstant.STATUS_PAID_FAILURE + "%", 
				DateUtil.addHour(cur, -8), DateUtil.addMinute(cur, - 10));
		return Integer.valueOf("" + result.get(0));
	}
	@Override
	public String getMemberOrderHis(Long memberid) {
		String sql = "SELECT BUYTIMES FROM WEBDATA.MEMBER_BUYTIMES WHERE MEMBERID = ?";
		List<String> orderHis = jdbcTemplate.queryForList(sql, String.class, memberid);
		if(orderHis.isEmpty()) return "";
		return orderHis.get(0);
	}
	/**
	 *    @function 根据用户ID + 时间限定 查询订单记录, 不考虑状态, 含分页 
	 * 	@author bob.hu
	 *		@date	2011-04-26 11:04:04
	 */
	@Override
	public List<GewaOrder> getOrderListByMemberId(Long memberid, Integer days, int from, int maxnum){
		Timestamp cur = new Timestamp(System.currentTimeMillis());
		Timestamp qtime = DateUtil.addDay(cur, - days);
		DetachedCriteria query = DetachedCriteria.forClass(GewaOrder.class);
		
		query.add(Restrictions.or(
				Restrictions.and(Restrictions.like("status", OrderConstant.STATUS_NEW, MatchMode.START), 
						Restrictions.gt("addtime", DateUtil.addDay(cur, -7))), 
						Restrictions.like("status", OrderConstant.STATUS_PAID, MatchMode.START)));
		query.add(Restrictions.ne("paymethod", PaymethodConstant.PAYMETHOD_SYSPAY));
		query.add(Restrictions.eq("memberid", memberid));
		if(days != 0){// days==0, 不考虑时间限定
			query.add(Restrictions.ge("addtime", qtime));
		}
		query.add(Restrictions.or(Restrictions.ne("restatus", GewaOrder.RESTATUS_DELETE), Restrictions.isNull("restatus")));
		query.addOrder(Order.desc("addtime"));
		List<GewaOrder> result = readOnlyTemplate.findByCriteria(query, from, maxnum);
		return result;
	}
	@Override
	public Integer getOrderCountByMemberId(Long memberid, Integer days){
		Timestamp cur = new Timestamp(System.currentTimeMillis());
		Timestamp qtime = DateUtil.addDay(cur, - days);
		DetachedCriteria query = DetachedCriteria.forClass(GewaOrder.class);
		query.setProjection(Projections.rowCount());
		query.add(Restrictions.or(
				Restrictions.and(Restrictions.like("status", OrderConstant.STATUS_NEW, MatchMode.START), 
						Restrictions.gt("addtime", DateUtil.addDay(cur, -7))), 
						Restrictions.like("status", OrderConstant.STATUS_PAID, MatchMode.START)));
		query.add(Restrictions.ne("paymethod", PaymethodConstant.PAYMETHOD_SYSPAY));
		query.add(Restrictions.eq("memberid", memberid));
		if(days != 0){// days==0, 不考虑时间限定
			query.add(Restrictions.ge("addtime", qtime));
		}
		query.add(Restrictions.or(Restrictions.ne("restatus", GewaOrder.RESTATUS_DELETE), Restrictions.isNull("restatus")));
		List<GewaOrder> result = readOnlyTemplate.findByCriteria(query);
		if(!result.isEmpty()) return Integer.parseInt("" + result.get(0));
		return 0;
	}
	
	/**
	 *	订单页面列表(包含未付款订单和交易成功订单) 
	 */
	@Override
	public <T extends GewaOrder> List<T> getOrderListByMemberId(Class<T> clazz, Long memberId, String status, int days, int from, int maxnum) {
		Timestamp cur = new Timestamp(System.currentTimeMillis());
		Timestamp qtime = DateUtil.addDay(cur, - days);
		DetachedCriteria query = DetachedCriteria.forClass(clazz);
		query.add(Restrictions.eq("memberid", memberId));
		if(StringUtils.equals(status, "cancel")){
			query.add(Restrictions.or(Restrictions.and(
					Restrictions.like("status", OrderConstant.STATUS_NEW, MatchMode.START), 
					Restrictions.lt("validtime", cur)), Restrictions.like("status", OrderConstant.STATUS_CANCEL)));
		}else if(StringUtils.isNotBlank(status)){
			query.add(Restrictions.like("status", status, MatchMode.START));
		}
		
		query.add(Restrictions.ge("addtime", qtime));
		query.addOrder(Order.desc("addtime"));
		List<T> result = hibernateTemplate.findByCriteria(query, from, maxnum);
		return result;
	}

	@Override
	public <T extends GewaOrder> T getLastUnpaidOrder(Long memberid) {
		DetachedCriteria query = DetachedCriteria.forClass(GewaOrder.class);
		query.add(Restrictions.eq("memberid", memberid));
		query.add(Restrictions.eq("ukey", ""+memberid));
		query.add(Restrictions.like("status", OrderConstant.STATUS_NEW, MatchMode.START));
		query.add(Restrictions.gt("validtime", new Timestamp(System.currentTimeMillis())));
		query.addOrder(Order.desc("addtime"));
		List<T> result = hibernateTemplate.findByCriteria(query);
		if(result.isEmpty()) return null;
		return result.get(0);
	}

	@Override
	public List<TicketOrder> getTicketOrderList(SearchOrderCommand soc) {
		if(soc.isBlankCond()) return new ArrayList<TicketOrder>();		
		DetachedCriteria query = DetachedCriteria.forClass(TicketOrder.class);
		if(StringUtils.isNotBlank(soc.getTradeNo())) query.add(Restrictions.eq("tradeNo", soc.getTradeNo()));
		if(StringUtils.isNotBlank(soc.getMobile())) query.add(Restrictions.eq("mobile", soc.getMobile())); 
		if(soc.getMinute()!=null){
			Timestamp from = DateUtil.addMinute(new Timestamp(System.currentTimeMillis()), -soc.getMinute());
			query.add(Restrictions.ge("addtime", from));
		}
		if(StringUtils.isNotBlank(soc.getOrdertype())){//可能有过时自动取消的账单
			if(soc.getOrdertype().equals(OrderConstant.STATUS_CANCEL)){
				query.add(Restrictions.or(Restrictions.like("status", soc.getOrdertype(), MatchMode.START),
						Restrictions.and(Restrictions.like("status", OrderConstant.STATUS_NEW, MatchMode.START), 
								Restrictions.lt("validtime", new Timestamp(System.currentTimeMillis())))));
			}else{
				query.add(Restrictions.like("status", soc.getOrdertype(), MatchMode.START));
				if(StringUtils.startsWith(soc.getOrdertype(), OrderConstant.STATUS_NEW)){//可能有过时自动取消的账单
					query.add(Restrictions.ge("validtime", new Timestamp(System.currentTimeMillis())));
				}
			}
		}
		if(soc.getCid()!=null) query.add(Restrictions.eq("cinemaid", soc.getCid()));
		if(soc.getOrderid()!=null) query.add(Restrictions.eq("id", soc.getOrderid()));
		if(soc.getMpid()!=null) query.add(Restrictions.eq("mpid", soc.getMpid()));
		query.addOrder(Order.desc("addtime"));
		List<TicketOrder> orderList = hibernateTemplate.findByCriteria(query);
		return orderList;
	}
	@Override
	public List<TicketOrder> getTicketOrderListByMpid(Long mpid, String status){
		DetachedCriteria query = DetachedCriteria.forClass(TicketOrder.class);
		query.add(Restrictions.eq("mpid", mpid));
		if(StringUtils.isNotBlank(status)) query.add(Restrictions.like("status", status, MatchMode.START));
		List<TicketOrder> orderList = hibernateTemplate.findByCriteria(query);
		return orderList;
	}
	@Override
	public Integer getTicketOrderCountByMpid(Long mpid) {
		DetachedCriteria query = DetachedCriteria.forClass(TicketOrder.class);
		query.add(Restrictions.eq("mpid", mpid));
		query.setProjection(Projections.rowCount());
		List result = hibernateTemplate.findByCriteria(query);
		if(result.isEmpty()) return 0;
		return Integer.valueOf(result.get(0)+"");
	}
	@Override
	public List<Movie> getMemberOrderMovieList(Long memberid, int maxnum){
		List<Movie> movieList = new ArrayList<Movie>();
		Map m = memberCountService.getMemberCount(memberid);
		if(m!=null){
			String movieidList = (String) m.get(MemberStats.FIELD_LASTMOVIEID);
			List<Long> idList = BeanUtil.getIdList(movieidList, ",");
			idList = BeanUtil.getSubList(idList, 0, maxnum);
			movieList.addAll(baseDao.getObjectList(Movie.class, idList));
		}
		return movieList;
	}
	
	@Override
	public List<Cinema> getMemberOrderCinemaList(Long memberid, int maxnum){
		List<Cinema> cinemaList = new ArrayList<Cinema>();
		Map m = memberCountService.getMemberCount(memberid);
		if(m!=null){
			String lastcinemaList = (String)m.get(MemberStats.FIELD_LASTCINEMAID);
			List<Long> idList = BeanUtil.getIdList(lastcinemaList, ",");
			idList = new LinkedList<Long>(new LinkedHashSet<Long>(idList));
			idList = BeanUtil.getSubList(idList, 0, maxnum);
			cinemaList = baseDao.getObjectList(Cinema.class, idList);
		}
		return cinemaList;
	}
	@Override
	public Integer getMemberOrderCinemaCount(Long memberid){
		Map m = memberCountService.getMemberCount(memberid);
		int count = 0;
		if(m!=null){
			String lastcinemaList = (String)m.get(MemberStats.FIELD_LASTCINEMAID);
			List<Long> idList = BeanUtil.getIdList(lastcinemaList, ",");
			count = idList.size();
		}
		return count;
	}
	@Override
	public Integer getMemberOrderCountByMemberid(Long memberid, Long relatedid){
		DetachedCriteria query = DetachedCriteria.forClass(GewaOrder.class);
		query.add(Restrictions.eq("memberid", memberid));
		query.add(Restrictions.eq("movieid", relatedid));
		query.add(Restrictions.like("status", OrderConstant.STATUS_PAID,MatchMode.START));
		query.setProjection(Projections.rowCount());
		List<GewaOrder> list = hibernateTemplate.findByCriteria(query);
		if (list.isEmpty()) return 0;
		return Integer.parseInt(""+list.get(0));
	}
	
	@Override
	public Integer getMemberOrderCountByMemberid(Long memberid, Long relatedid, Timestamp fromtime, Timestamp totime, String citycode, String pricategory){
		DetachedCriteria query = DetachedCriteria.forClass(GewaOrder.class);
		query.add(Restrictions.eq("memberid", memberid));
		if(relatedid != null) query.add(Restrictions.eq("movieid", relatedid));
		if(StringUtils.isNotBlank(pricategory)) query.add(Restrictions.eq("pricategory", pricategory));
		if(StringUtils.isNotBlank(citycode)) query.add(Restrictions.eq("citycode", citycode));
		query.add(Restrictions.like("status", OrderConstant.STATUS_PAID,MatchMode.START));
		query.add(Restrictions.ge("addtime", fromtime));
		query.add(Restrictions.le("addtime", totime));
		query.setProjection(Projections.rowCount());
		List<GewaOrder> list = hibernateTemplate.findByCriteria(query);
		if (list.isEmpty()) return 0;
		return Integer.parseInt(""+list.get(0));
	}
	
	@Override
	public List<GewaOrder> getPreferentialOrder(Long memberid,List<Long> spIdList){
		DetachedCriteria subQuery = DetachedCriteria.forClass(Discount.class);
		subQuery.add(Restrictions.in("relatedid", spIdList));
		subQuery.setProjection(Projections.property("orderid"));
		DetachedCriteria query = DetachedCriteria.forClass(GewaOrder.class);
		query.add(Subqueries.propertyIn("id",subQuery));
		query.add(Restrictions.eq("status", OrderConstant.STATUS_PAID_SUCCESS));
		query.add(Restrictions.eq("memberid", memberid));
		return hibernateTemplate.findByCriteria(query);
	}
	@Override
	public List<TicketOrder> getTicketOrderList(CooperUser partner, SearchOrderCommand soc) {
		DetachedCriteria query = DetachedCriteria.forClass(TicketOrder.class, "t");
		if(soc.getTimeFrom()==null || soc.getTimeTo()==null){//按时间范围
			return new ArrayList<TicketOrder>();
		}
		query.add(Restrictions.ge("t.addtime", soc.getTimeFrom()));
		query.add(Restrictions.le("t.addtime", soc.getTimeTo()));
		if(StringUtils.isNotBlank(soc.getMobile())) query.add(Restrictions.like("t.mobile", soc.getMobile(), MatchMode.ANYWHERE));
		if(StringUtils.isNotBlank(soc.getTradeNo())) query.add(Restrictions.eq("t.tradeNo", soc.getTradeNo()));
		if(StringUtils.isNotBlank(soc.getStatus())) query.add(Restrictions.like("t.status", soc.getStatus(), MatchMode.START));
		List<Long> partnerids = BeanUtil.getIdList(partner.getPartnerids(), ",");
		query.add(Restrictions.in("partnerid", partnerids));
		query.addOrder(Order.desc("addtime"));
		List<TicketOrder> orderList = hibernateTemplate.findByCriteria(query);
		return orderList;
	}
	@Override
	public List<Map> getTicketOrderListByDate(CooperUser partner, SearchOrderCommand soc){
		DetachedCriteria query = DetachedCriteria.forClass(TicketOrder.class, "t");
		if(soc.getTimeFrom()==null || soc.getTimeTo()==null){//按时间范围
			return new ArrayList<Map>();
		} else{
			if(soc.getTimeFrom()==null) soc.setTimeFrom(DateUtil.addDay(soc.getTimeTo(), -1));
			if(soc.getTimeTo()==null) soc.setTimeTo(DateUtil.addDay(soc.getTimeFrom(), 1));
			query.add(Restrictions.ge("addtime", soc.getTimeFrom()));
			query.add(Restrictions.le("addtime", soc.getTimeTo()));
		}
		query.add(Restrictions.eq("status", OrderConstant.STATUS_PAID_SUCCESS));
		query.add(Restrictions.eq("partnerid", partner.getId()));
		Projection pro = Projections.sqlGroupProjection(
				"to_char({alias}.addtime,'yyyy-mm-dd') as adddate", 
				"to_char({alias}.addtime,'yyyy-mm-dd')", new String[]{"adddate"}, 
				new Type[]{new StringType()});
		query.setProjection(Projections.projectionList()
				.add(Projections.count("id"), "totalcount")
				.add(Projections.sum("totalfee"),"totalfee")
				.add(Projections.sum("quantity"),"quantity")
				.add(Projections.alias(pro, "vatime")));
		query.addOrder(Order.asc("vatime"));
		query.setResultTransformer(DetachedCriteria.ALIAS_TO_ENTITY_MAP);
		List orderList = hibernateTemplate.findByCriteria(query);
		return orderList;
	}
	@Override
	public List<GewaOrder> getOrderOriginListByDate(CooperUser partner,SearchOrderCommand soc) {
		List<GewaOrder> orderList = new ArrayList<GewaOrder>();
		if(soc.getTimeFrom() == null || soc.getTimeTo()==null || StringUtils.isBlank(partner.getOrigin())){//按时间范围
			return orderList;
		}
		if(StringUtils.isNotBlank(soc.getTradeNo())){
			GewaOrder order = baseDao.getObjectByUkey(GewaOrder.class, "tradeNo", soc.getTradeNo());
			orderList.add(order);
			return orderList;
		}
		List<String> originList = Arrays.asList(StringUtils.split(partner.getOrigin(), ","));
		List<Long> orderidList = new ArrayList<Long>();
		for(String origin : originList){
			Map<String, String> query = new HashMap<String, String>();
			query.put("orderOrigin", origin);
			//query.put("type", AppSourceCount.TYPE_ORDER);
			List<Map<String, String>> qryMapList = hbaseService.getRowListByRange(AppConstant.TABLE_APPSOURCE, query, soc.getTimeFrom().getTime(), soc.getTimeTo().getTime(), 1000);
			for(Map<String, String> qryMap : qryMapList){
				Long id = Long.valueOf(qryMap.get("orderid"));
				orderidList.add(id);
			}
		}
		orderList = baseDao.getObjectList(GewaOrder.class, orderidList);
		return orderList;
	}
	@Override
	public List<GewaOrder> getOrderAppsourceListByDate(CooperUser partner,Timestamp dateFrom, Timestamp dateTo, String appsource) {
		List<GewaOrder> orderList = new ArrayList<GewaOrder>();
		if(dateFrom == null || dateTo==null || StringUtils.isBlank(partner.getAppsource())){//按时间范围
			return orderList;
		}
		List<String> partneridList = Arrays.asList(StringUtils.split(partner.getPartnerids(), ","));
		List<Long> orderidList = new ArrayList<Long>();
		for(String partnerid : partneridList){
			Map<String, String> query = new HashMap<String, String>();
			query.put("partnerid", partnerid);
			query.put("appSource", appsource);
			query.put("type", AppSourceCount.TYPE_ORDER);
			//TODO
			List<Map<String, String>> qryMapList = hbaseService.getRowListByRange(AppConstant.TABLE_APPSOURCE, query, dateFrom.getTime(), dateTo.getTime(), 10000);
			for(Map<String, String> qryMap : qryMapList){
				String stroid = qryMap.get("orderid");
				Long id = null;
				if(StringUtils.isNotBlank(stroid) && !StringUtils.equals(stroid, "null")){
					id = Long.valueOf(stroid);
				}
				if(id!=null) orderidList.add(id);
			}
		}
		orderList = baseDao.getObjectList(GewaOrder.class, orderidList);
		return orderList;
	}
	@Override
	public Integer getMemberTicketCountByMemberid(Long memberid, Timestamp fromtime, Timestamp totime, String status, String citycode){
		DetachedCriteria query = DetachedCriteria.forClass(TicketOrder.class);
		query.add(Restrictions.eq("memberid", memberid));
		if (StringUtils.isNotBlank(citycode)) {
			query.add(Restrictions.eq("citycode", citycode));
		}
		query.add(Restrictions.eq("status", status));
		query.add(Restrictions.ge("addtime", fromtime));
		query.add(Restrictions.le("addtime", totime));
		query.setProjection(Projections.rowCount());
		List<GewaOrder> list = hibernateTemplate.findByCriteria(query);
		if (list.isEmpty()) return 0;
		return Integer.parseInt(""+list.get(0));
	}
	
	@Override
	public Integer getNoPreferentialSportOrderCount(Long memberid, Timestamp fromtime, Timestamp totime, String status){
		DetachedCriteria query = DetachedCriteria.forClass(SportOrder.class);
		query.add(Restrictions.eq("discount", 0));//TODO:逻辑有问题：discount=0，但有其他类似加wabi的返利活动
		query.add(Restrictions.eq("memberid", memberid));
		query.add(Restrictions.eq("status", status));
		query.add(Restrictions.ge("addtime", fromtime));
		query.add(Restrictions.le("addtime", totime));
		query.setProjection(Projections.rowCount());
		List list = hibernateTemplate.findByCriteria(query);
		if (list.isEmpty()) return 0;
		return Integer.parseInt(""+list.get(0));
	}

	@Override
	public List<GewaOrder> getTicketOrderListByPayMethod(Timestamp starttime, Timestamp endtime, String paymethod, String tradeNo){
		DetachedCriteria query=DetachedCriteria.forClass(GewaOrder.class);
		query.add(Restrictions.eq("status", OrderConstant.STATUS_PAID_SUCCESS));
		if(StringUtils.isNotBlank(tradeNo)){
			query.add(Restrictions.eq("tradeNo", tradeNo));
		}else{
			query.add(Restrictions.gt("addtime", starttime));
			query.add(Restrictions.le("addtime", endtime));
		}
		query.add(Restrictions.eq("paymethod", paymethod));
		query.addOrder(Order.desc("addtime"));
		List<GewaOrder> orderList=hibernateTemplate.findByCriteria(query);
		return orderList;
	}
	
	@Override
	public Integer getMUOrderCountByMbrids(List<Long> memberids, Long movieid, Timestamp fromtime, Timestamp totime){
		DetachedCriteria query = DetachedCriteria.forClass(GewaOrder.class);
		query.add(Restrictions.in("memberid",memberids));
		query.add(Restrictions.eq("movieid", movieid));
		query.add(Restrictions.like("status", OrderConstant.STATUS_PAID,MatchMode.START));
		query.add(Restrictions.ge("addtime", fromtime));
		query.add(Restrictions.le("addtime", totime));
		query.setProjection(Projections.rowCount());
		List list = hibernateTemplate.findByCriteria(query);
		if (list.isEmpty()) return 0;
		return Integer.parseInt(""+list.get(0));
	}
	
	@Override
	public Integer getTicketOrderCountByCinema(long cinemaId, Timestamp fromtime, Timestamp totime, String status){
		DetachedCriteria query = DetachedCriteria.forClass(TicketOrder.class);
		query.add(Restrictions.eq("cinemaid", cinemaId));
		query.add(Restrictions.eq("status", status));
		query.add(Restrictions.ge("addtime", fromtime));
		query.add(Restrictions.le("addtime", totime));
		query.setProjection(Projections.rowCount());
		List list = hibernateTemplate.findByCriteria(query);
		if (list.isEmpty()) return 0;
		return Integer.parseInt(""+list.get(0));
	}
	
	@Override
	public List<GewaOrder> getOrderList(OrderParamsCommand command, int from, int maxnum){
		Class clazz = getQueryClazz(command.getOrdertype());
		return getOrderList(clazz, command, from, maxnum);
	}
	
	@Override
	public Integer getOrderCount(OrderParamsCommand command){
		Class clazz = getQueryClazz(command.getOrdertype());
		return getOrderCount(clazz, command);
		
	}
	
	private Class getQueryClazz(String ordertype){
		Class clazz = GewaOrder.class;
		if(StringUtils.equals(ordertype, OrderConstant.ORDER_TYPE_TICKET)){
			clazz = TicketOrder.class;
		}else if(StringUtils.equals(ordertype, OrderConstant.ORDER_TYPE_DRAMA)){
			clazz = DramaOrder.class;
		}else if(StringUtils.equals(ordertype, OrderConstant.ORDER_TYPE_GOODS)){
			clazz = GoodsOrder.class;
		}else if(StringUtils.equals(ordertype, OrderConstant.ORDER_TYPE_SPORT)){
			clazz = SportOrder.class;
		}else if(StringUtils.equals(ordertype, OrderConstant.ORDER_TYPE_PUBSALE)){
			clazz = PubSaleOrder.class;
		}else if(StringUtils.equals(ordertype, OrderConstant.ORDER_TYPE_MEMBERCARD)){
			clazz = MemberCardOrder.class;
		}else if(StringUtils.equals(ordertype, OrderConstant.ORDER_TYPE_GYM)){
			clazz = GymOrder.class;
		}
		return clazz;
	}
	
	@Override
	public <T extends GewaOrder> List<T> getOrderList(Class<T> clazz, OrderParamsCommand command){
		return this.getOrderList(clazz, command, 0, -1);
	}
	
	@Override
	public <T extends GewaOrder> List<T> getOrderList(Class<T> clazz, OrderParamsCommand command, int from, int maxnum){
		DetachedCriteria query = getQueryOrderCriteria(clazz, command);
		if (StringUtils.isNotBlank(command.getExpressstatus())) {
			DetachedCriteria sub = DetachedCriteria.forClass(OrderExtra.class, "b");
			sub.add(Restrictions.eq("dealStatus", command.getExpressstatus()));
			sub.add(Restrictions.eqProperty("b.tradeno", "o.tradeNo"));
			sub.setProjection(Projections.property("b.tradeno"));
			query.add(Subqueries.exists(sub));
		}
		if(command.getAsc()){
			query.addOrder(Order.asc("addtime"));
		}else{
			query.addOrder(Order.desc("addtime"));
		}
		return readOnlyTemplate.findByCriteria(query, from, maxnum);
	}
	
	@Override
	public <T extends GewaOrder> Integer getOrderCount(Class<T> clazz, OrderParamsCommand command){
		DetachedCriteria query = getQueryOrderCriteria(clazz, command);
		query.setProjection(Projections.rowCount());
		List<Long> result = readOnlyTemplate.findByCriteria(query, 0, 1);
		if(result.isEmpty()) return 0;
		return result.get(0).intValue();
	}
	
	private <T extends GewaOrder> DetachedCriteria getQueryOrderCriteria(Class<T> clazz, OrderParamsCommand command){
		DetachedCriteria query = DetachedCriteria.forClass(clazz, "o");
		Disjunction disjunction = Restrictions.disjunction();
		boolean flag = false;
		if(StringUtils.isNotBlank(command.getMobile())){
			disjunction.add(Restrictions.eq("mobile", command.getMobile()));
			flag = true;
		}
		if(command.getMemberid() != null){
			disjunction.add(Restrictions.eq("memberid", command.getMemberid()));
			flag = true;
		}
		if(StringUtils.isNotBlank(command.getTradeno())){
			disjunction.add(Restrictions.eq("tradeNo", command.getTradeno()));
			flag = true;
		}
		Conjunction conjunction = Restrictions.conjunction();
		if(StringUtils.isNotBlank(command.getStatus())){
			conjunction.add(Restrictions.like("status", command.getStatus(), MatchMode.START));
		}
		if(flag){
			if(command.getStarttime() != null ){
				conjunction.add(Restrictions.ge("addtime", command.getStarttime()));
			}
			if(command.getEndtime() != null){
				conjunction.add(Restrictions.le("addtime", command.getEndtime()));
			}
		}else{
			conjunction.add(Restrictions.ge("addtime", command.getStarttime()));
			conjunction.add(Restrictions.le("addtime", command.getEndtime()));
		}
		Map<String, Object> keyByValueMap = queryOrderKeyByValue(clazz, command);
		if(!MapUtils.isEmpty(keyByValueMap)){
			for (String key : keyByValueMap.keySet()) {
				Object value = keyByValueMap.get(key);
				if(StringUtils.isNotBlank(key) && value != null){
					conjunction.add(Restrictions.eq(key, value));
				}
			}
		}
		if(StringUtils.isNotBlank(command.getExpress())){
			conjunction.add(Restrictions.eq("express", command.getExpress()));
		}
		if(StringUtils.equals(command.getOrdertype(), OrderConstant.ORDER_TYPE_GOODS) && StringUtils.isNotBlank(command.getCategory())){
			conjunction.add(Restrictions.eq("category", command.getCategory()));
		}
		query.add(conjunction);
		if(flag){
			query.add(disjunction);
		}
		return query;
	}
	
	private <T extends GewaOrder> Map<String, Object> queryOrderKeyByValue(Class<T> clazz, OrderParamsCommand command){
		Map<String, Object> dataMap = new HashMap<String, Object>();
		final boolean placeFlag = command.getPlaceid() != null, itemFlag = command.getItemid() != null, relateFlag = command.getRelatedid() != null;
		final boolean flag = placeFlag || itemFlag || relateFlag;
		if(flag){
			if(clazz.equals(TicketOrder.class)){
				if(placeFlag){
					dataMap.put("cinemaid", command.getPlaceid());
				}
				if(itemFlag){
					dataMap.put("movieid", command.getItemid());
				}
				if(relateFlag){
					dataMap.put("mpid", command.getRelatedid());
				}
			}else if(clazz.equals(DramaOrder.class)){
				if(placeFlag){
					dataMap.put("theatreid", command.getPlaceid());
				}
				if(itemFlag){
					dataMap.put("dramaid", command.getItemid());
				}
				if(relateFlag){
					dataMap.put("dpid", command.getRelatedid());
				}
			}else if(clazz.equals(SportOrder.class)){
				if(placeFlag){
					dataMap.put("sportid", command.getPlaceid());
				}
				if(itemFlag){
					dataMap.put("itemid", command.getItemid());
				}
				if(relateFlag){
					dataMap.put("ottid", command.getRelatedid());
				}
			}else if(clazz.equals(GymOrder.class)){
				if(placeFlag){
					dataMap.put("gymid", command.getPlaceid());
				}
			}else if(clazz.equals(MemberCardOrder.class)){
				if(placeFlag){
					dataMap.put("placeid", command.getPlaceid());
				}
			}else if(clazz.equals(GoodsOrder.class)){
				if(placeFlag){
					dataMap.put("placeid", command.getPlaceid());
				}
				if(itemFlag){
					dataMap.put("itemid", command.getItemid());
				}
			}
		}
		return dataMap;
	}
	
	@Override
	public <T extends BaseGoods> List<GoodsOrder> getGoodsOrderList(Class<T> clazz, OrderParamsCommand command, int from, int maxnum) {
		DetachedCriteria query = getQueryOrderCriteria(GoodsOrder.class, command);
		DetachedCriteria sub = DetachedCriteria.forClass(clazz, "b");
		sub.add(Restrictions.eqProperty("b.id", "o.goodsid"));
		sub.setProjection(Projections.property("b.id"));
		query.add(Subqueries.exists(sub));
		query.addOrder(Order.desc("addtime"));
		if(command.getAsc()){
			query.addOrder(Order.asc("addtime"));
		}else{
			query.addOrder(Order.desc("addtime"));
		}
		return readOnlyTemplate.findByCriteria(query, from, maxnum);
	}
	
	@Override
	public List<TicketOrder> getTicketOrderList(OrderParamsCommand command, String place, String item, int from, int maxnum) {
		DetachedCriteria query = getQueryOrderCriteria(TicketOrder.class, command);
		if (StringUtils.isNotBlank(place)) {
			DetachedCriteria cinemaQuery = DetachedCriteria.forClass(Cinema.class, "c");
			cinemaQuery.add(Restrictions.like("name", place, MatchMode.ANYWHERE));
			cinemaQuery.add(Restrictions.eqProperty("c.id", "o.cinemaid"));
			cinemaQuery.setProjection(Projections.property("c.id"));
			query.add(Subqueries.exists(cinemaQuery));
		}
		if (StringUtils.isNotBlank(item)) {
			DetachedCriteria movieQuery = DetachedCriteria.forClass(Movie.class, "m");
			movieQuery.add(Restrictions.like("moviename", item, MatchMode.ANYWHERE));
			movieQuery.add(Restrictions.eqProperty("m.id", "o.movieid"));
			movieQuery.setProjection(Projections.property("m.id"));
			query.add(Subqueries.exists(movieQuery));
		}
		if(command.getAsc()){
			query.addOrder(Order.asc("addtime"));
		}else{
			query.addOrder(Order.desc("addtime"));
		}
		return readOnlyTemplate.findByCriteria(query, from, maxnum);
	}
	
	@Override
	public TicketOrder getFirstTicketOrder(long memberid) {
		DetachedCriteria query = DetachedCriteria.forClass(TicketOrder.class);
		query.add(Restrictions.like("status", OrderConstant.STATUS_PAID, MatchMode.START));
		query.add(Restrictions.eq("memberid", memberid));
		query.addOrder(Order.asc("addtime"));
		List<GewaOrder> result = readOnlyTemplate.findByCriteria(query, 0, 1);
		if(!result.isEmpty()) return (TicketOrder) result.get(0);
		return null;
	}
	@Override
	public Long getOrderValidTime(String tradeNo) {
		String cacheKey = CacheConstant.KEY_TICKET_VALIDTIME_ + tradeNo;
		Long valid = (Long) cacheService.get(CacheConstant.REGION_HALFHOUR, cacheKey);
		if(valid==null){
			GewaOrder order = baseDao.getObjectByUkey(GewaOrder.class, "tradeNo", tradeNo);
			valid = order.getValidtime().getTime();
			cacheService.set(CacheConstant.REGION_HALFHOUR, cacheKey, valid);
		}
		return valid;
	}
	@Override
	public Long getOrderValidTimeById(Long orderid) {
		String cacheKey = CacheConstant.KEY_TICKET_VALIDTIME_ + orderid;
		Long valid = (Long) cacheService.get(CacheConstant.REGION_HALFHOUR, cacheKey);
		if(valid==null){
			GewaOrder order = baseDao.getObject(GewaOrder.class, orderid);
			valid = order.getValidtime().getTime();
			cacheService.set(CacheConstant.REGION_HALFHOUR, cacheKey, valid);
		}
		return valid;
	}
}
