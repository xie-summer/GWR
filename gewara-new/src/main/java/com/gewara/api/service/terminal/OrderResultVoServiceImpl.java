package com.gewara.api.service.terminal;

import java.sql.Timestamp;
import java.util.List;

import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Restrictions;

import com.gewara.api.terminal.service.OrderResultVoService;
import com.gewara.api.vo.ResultCode;
import com.gewara.api.vo.order.OrderResultVo;
import com.gewara.model.api.OrderResult;
import com.gewara.service.impl.BaseServiceImpl;
import com.gewara.util.VmUtils;
import com.gewara.util.VoCopyUtil;

public class OrderResultVoServiceImpl extends BaseServiceImpl implements OrderResultVoService{
	@Override
	public ResultCode<OrderResultVo> getOrderResult(String tradeno) {
		OrderResult res = baseDao.getObject(OrderResult.class, tradeno);
		return VoCopyUtil.copyProperties(OrderResultVo.class, res);
	}

	@Override
	public ResultCode<List<OrderResultVo>> getOrderResultListByTaketime(Long placeid, Timestamp taketime) {
		String hql = "from OrderResult o where o.placeid=? and o.taketime is not null and o.taketime>=?";
		List<OrderResult> resultList = hibernateTemplate.find(hql, placeid, taketime);
		return VoCopyUtil.copyListProperties(OrderResultVo.class, resultList);
	}

	@Override
	public ResultCode<List<OrderResultVo>> getOrderResultListByUpdatetime(Long placeid, Timestamp updatetime) {
		String hql = "from OrderResult o where o.placeid=? and o.updatetime is not null and o.updatetime>=?";
		List<OrderResult> resultList = hibernateTemplate.find(hql, placeid, updatetime);
		return VoCopyUtil.copyListProperties(OrderResultVo.class, resultList);
	}

	@Override
	public ResultCode<List<OrderResultVo>> getOrderResultListByPlaceids(List<Long> placeidList, Timestamp updatetime) {
		if(VmUtils.size(placeidList)==0){
			return ResultCode.getFailure("传递场馆集合不能为空！");
		}
		DetachedCriteria query = DetachedCriteria.forClass(OrderResult.class);
		query.add(Restrictions.in("placeid", placeidList));
		query.add(Restrictions.ge("updatetime", updatetime));
		List<OrderResult> resultList = hibernateTemplate.findByCriteria(query);
		return VoCopyUtil.copyListProperties(OrderResultVo.class, resultList);
	}

	@Override
	public ResultCode<List<OrderResultVo>> getOrderResultListByPlaceids(List<Long> placeidList, Timestamp starttime, Timestamp endtime) {
		if(VmUtils.size(placeidList)==0){
			return ResultCode.getFailure("传递场馆集合不能为空！");
		}
		if(starttime==null || endtime==null){
			return ResultCode.getFailure("传递日期不能为空！");
		}
		DetachedCriteria query = DetachedCriteria.forClass(OrderResult.class);
		query.add(Restrictions.in("placeid", placeidList));
		query.add(Restrictions.ge("updatetime", starttime));
		query.add(Restrictions.le("updatetime", endtime));
		List<OrderResult> resultList = hibernateTemplate.findByCriteria(query);
		return VoCopyUtil.copyListProperties(OrderResultVo.class, resultList);
	}

	@Override
	public ResultCode<List<OrderResultVo>> getOrderResultListByPlaceids(List<Long> placeidList, String ordertype, Timestamp starttime,
			Timestamp endtime) {
		if(VmUtils.size(placeidList)==0){
			return ResultCode.getFailure("传递场馆集合不能为空！");
		}
		if(starttime==null || endtime==null){
			return ResultCode.getFailure("传递日期不能为空！");
		}
		DetachedCriteria query = DetachedCriteria.forClass(OrderResult.class);
		query.add(Restrictions.in("placeid", placeidList));
		query.add(Restrictions.eq("ordertype", ordertype));
		query.add(Restrictions.ge("updatetime", starttime));
		query.add(Restrictions.le("updatetime", endtime));
		List<OrderResult> resultList = hibernateTemplate.findByCriteria(query);
		return VoCopyUtil.copyListProperties(OrderResultVo.class, resultList);
	}
}
