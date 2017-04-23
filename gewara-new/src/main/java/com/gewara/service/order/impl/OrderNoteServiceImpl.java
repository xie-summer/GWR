package com.gewara.service.order.impl;

import java.sql.Timestamp;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Service;

import com.gewara.constant.ticket.OrderNoteConstant;
import com.gewara.model.pay.OrderNote;
import com.gewara.service.impl.BaseServiceImpl;
import com.gewara.service.order.OrderNoteService;
import com.gewara.util.BeanUtil;

@Service("orderNoteService")
public class OrderNoteServiceImpl extends BaseServiceImpl implements OrderNoteService {

	@Override
	public List<OrderNote> getOrderNoteBySmallitemid(String smallitemtype, Long smallitemid, Timestamp fromtime, Timestamp totime, int from, int maxnum){
		DetachedCriteria query = getOrderNoteQry(null, null, null, null, null, smallitemtype, smallitemid, fromtime, totime);
		query.addOrder(Order.desc("addtime"));
		List<OrderNote> noteList = hibernateTemplate.findByCriteria(query, from, maxnum);
		return noteList;
	}
	
	@Override
	public List<OrderNote> getOrderNoteBySmallitemid(String smallitemtype, Long smallitemid, Timestamp fromtime, Timestamp totime){
		DetachedCriteria query = getOrderNoteQry(null, null, null, null, null, smallitemtype, smallitemid, fromtime, totime);
		query.addOrder(Order.desc("addtime"));
		List<OrderNote> noteList = hibernateTemplate.findByCriteria(query);
		return noteList;
	}
	
	@Override
	public Integer getOrderNoteCountBySmallitemid(String smallitemtype, Long smallitemid, Timestamp fromtime, Timestamp totime){
		DetachedCriteria query = getOrderNoteQry(null, null, null, null, null, smallitemtype, smallitemid, fromtime, totime);
		query.setProjection(Projections.rowCount());
		List<Long> result = hibernateTemplate.findByCriteria(query);
		if(result.isEmpty()) return 0;
		return Integer.parseInt(result.get(0)+"");
	}
	
	@Override
	public List<OrderNote> getOrderNoteByItemid(String itemtype, Long itemid, Timestamp fromtime, Timestamp totime, int from, int maxnum){
		DetachedCriteria query = getOrderNoteQry(null, null, null, itemtype, itemid, null, null, fromtime, totime);
		query.addOrder(Order.desc("addtime"));
		List<OrderNote> noteList = hibernateTemplate.findByCriteria(query, from, maxnum);
		return noteList;
	}
	
	@Override
	public List<OrderNote> getOrderNoteByItemid(String itemtype, Long itemid, Timestamp fromtime, Timestamp totime){
		DetachedCriteria query = getOrderNoteQry(null, null, null, itemtype, itemid, null, null, fromtime, totime);
		query.addOrder(Order.desc("addtime"));
		List<OrderNote> noteList = hibernateTemplate.findByCriteria(query);
		return noteList;
	}
	
	@Override
	public Integer getOrderNoteCountByItemid(String itemtype, Long itemid, Timestamp fromtime, Timestamp totime){
		DetachedCriteria query = getOrderNoteQry(null, null, null, itemtype, itemid, null, null, fromtime, totime);
		query.setProjection(Projections.rowCount());
		List<Long> result = hibernateTemplate.findByCriteria(query);
		if(result.isEmpty()) return 0;
		return Integer.parseInt(result.get(0)+"");
	}
	
	@Override
	public List<OrderNote> getOrderNoteByPlaceid(String placetype, Long placeid, Timestamp fromtime, Timestamp totime, int from, int maxnum){
		DetachedCriteria query = getOrderNoteQry(null, placetype, placeid, null, null, null, null, fromtime, totime);
		query.addOrder(Order.desc("addtime"));
		List<OrderNote> noteList = hibernateTemplate.findByCriteria(query, from, maxnum);
		return noteList;
	}
	
	@Override
	public List<OrderNote> getOrderNoteByPlaceid(String placetype, Long placeid, Timestamp fromtime, Timestamp totime){
		DetachedCriteria query = getOrderNoteQry(null, placetype, placeid, null, null, null, null, fromtime, totime);
		query.addOrder(Order.desc("addtime"));
		List<OrderNote> noteList = hibernateTemplate.findByCriteria(query);
		return noteList;
	}
	
	@Override
	public Integer getOrderNoteCountByPlaceid(String placetype, Long placeid, Timestamp fromtime, Timestamp totime){
		DetachedCriteria query = getOrderNoteQry(null, placetype, placeid, null, null, null, null, fromtime, totime);
		query.setProjection(Projections.rowCount());
		List<Long> result = hibernateTemplate.findByCriteria(query);
		if(result.isEmpty()) return 0;
		return Integer.parseInt(result.get(0)+"");
	}
	
	@Override
	public List<OrderNote> getOrderNoteByOrdertype(String ordertype, Timestamp fromtime, Timestamp totime, int from, int maxnum){
		DetachedCriteria query = getOrderNoteQry(ordertype, null, null, null, null, null, null, fromtime, totime);
		query.addOrder(Order.desc("addtime"));
		List<OrderNote> noteList = hibernateTemplate.findByCriteria(query, from, maxnum);
		return noteList;
	}
	
	@Override
	public List<OrderNote> getOrderNoteByOrdertype(String ordertype, Timestamp fromtime, Timestamp totime){
		DetachedCriteria query = getOrderNoteQry(ordertype, null, null, null, null, null, null, fromtime, totime);
		query.addOrder(Order.desc("addtime"));
		List<OrderNote> noteList = hibernateTemplate.findByCriteria(query);
		return noteList;
	}
	@Override
	public List<OrderNote> getOrderNoteListByPlaceids(String ordertype, String mobile, String checkpass, Timestamp fromtime, Timestamp totime, 
			String placeid, String itemids, int from, int maxnum){
		List<Long> placeidList = BeanUtil.getIdList(placeid, ",");
		List<Long> itemidList = BeanUtil.getIdList(itemids, ",");
		DetachedCriteria query = getOrderNoteQry(ordertype, null, null, null, null, null, null, fromtime, totime);
		query.add(Restrictions.eq("status", OrderNoteConstant.STATUS_P));
		if(StringUtils.isNotBlank(mobile)){
			query.add(Restrictions.eq("mobile", mobile));
		}
		if(StringUtils.isNotBlank(checkpass)){
			query.add(Restrictions.eq("checkpass", checkpass));
		}
		query.add(Restrictions.in("placeid", placeidList));
		if(itemidList.size()>0){
			query.add(Restrictions.in("itemid", itemidList));
		}
		query.addOrder(Order.desc("addtime"));
		List<OrderNote> noteList = hibernateTemplate.findByCriteria(query, from, maxnum);
		return noteList;
	}
	@Override
	public int getOrderNoteCountByPlaceids(String ordertype, String mobile, String checkpass, Timestamp fromtime, Timestamp totime, String placeid, String itemids){
		List<Long> placeidList = BeanUtil.getIdList(placeid, ",");
		List<Long> itemidList = BeanUtil.getIdList(itemids, ",");
		DetachedCriteria query = getOrderNoteQry(ordertype, null, null, null, null, null, null, fromtime, totime);
		query.add(Restrictions.eq("status", OrderNoteConstant.STATUS_P));
		if(StringUtils.isNotBlank(mobile)){
			query.add(Restrictions.eq("mobile", mobile));
		}
		if(StringUtils.isNotBlank(checkpass)){
			query.add(Restrictions.eq("checkpass", checkpass));
		}
		query.add(Restrictions.in("placeid", placeidList));
		if(itemidList.size()>0){
			query.add(Restrictions.in("itemid", itemidList));
		}
		query.setProjection(Projections.rowCount());
		List<Long> result = hibernateTemplate.findByCriteria(query);
		if(result.isEmpty()) return 0;
		return Integer.parseInt(result.get(0)+"");
	}
	@Override
	public Integer getOrderNoteCountByOrdertype(String ordertype, Timestamp fromtime, Timestamp totime){
		DetachedCriteria query = getOrderNoteQry(ordertype, null, null, null, null, null, null, fromtime, totime);
		query.setProjection(Projections.rowCount());
		List<Long> result = hibernateTemplate.findByCriteria(query);
		if(result.isEmpty()) return 0;
		return Integer.parseInt(result.get(0)+"");
	}
	
	private DetachedCriteria getOrderNoteQry(String ordertype, String placetype, Long placeid, String itemtype, Long itemid, String smallitemtype, Long smallitemid, Timestamp fromtime, Timestamp totime){
		DetachedCriteria query = DetachedCriteria.forClass(OrderNote.class);
		if(StringUtils.isNotBlank(ordertype)){
			query.add(Restrictions.eq("ordertype", ordertype));
		}
		if(StringUtils.isNotBlank(placetype)){
			query.add(Restrictions.eq("placetype", placetype));
		}
		if(placeid != null){
			query.add(Restrictions.eq("placeid", placeid));
		}
		if(StringUtils.isNotBlank(itemtype)){
			query.add(Restrictions.eq("itemtype", itemtype));
		}
		if(itemid != null){
			query.add(Restrictions.eq("itemid", itemid));
		}
		if(StringUtils.isNotBlank(smallitemtype)){
			query.add(Restrictions.eq("smallitemtype", smallitemtype));
		}
		if(smallitemid != null){
			query.add(Restrictions.eq("smallitemid", smallitemid));
		}
		if(fromtime != null){
			query.add(Restrictions.ge("addtime", fromtime));
		}
		if(totime != null){
			query.add(Restrictions.le("addtime", totime));
		}
		return query;
	}
}
