package com.gewara.service.impl;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.gewara.constant.ApiConstant;
import com.gewara.constant.order.SettleConfigConstant;
import com.gewara.model.drama.DramaSettle;
import com.gewara.model.pay.SettleConfig;
import com.gewara.model.sport.SportSettle;
import com.gewara.service.SettleService;
import com.gewara.support.ErrorCode;
import com.gewara.untrans.monitor.MonitorService;
import com.gewara.util.BeanUtil;
@Service("settleService")
public class SettleServiceImpl extends BaseServiceImpl implements SettleService {
	@Autowired@Qualifier("monitorService")
	protected MonitorService monitorService;
	
	@Override
	public ErrorCode<SettleConfig> addSettleConfig(Long userid, Double discount, String distype){
		if(discount == null){
			return ErrorCode.getFailure(ApiConstant.CODE_SIGN_ERROR, "结算金额不能为空！");
		}
		if(StringUtils.isBlank(distype)){
			return ErrorCode.getFailure(ApiConstant.CODE_SIGN_ERROR, "结算类型错误！");
		}
		if(StringUtils.isBlank(SettleConfigConstant.DISCOUNT_TYPEMAP.get(distype))){
			return ErrorCode.getFailure(ApiConstant.CODE_SIGN_ERROR, "结算类型错误！");
		}
		SettleConfig settle = getSettleConfig(discount, distype);
		if(settle != null) return ErrorCode.getSuccessReturn(settle);
		settle = new SettleConfig(discount, distype);
		baseDao.saveObject(settle);
		monitorService.saveAddLog(userid, SettleConfig.class, settle.getId(), settle);
		return ErrorCode.getSuccessReturn(settle);
	}
	
	private SettleConfig getSettleConfig(Double discount, String distype){
		DetachedCriteria query = DetachedCriteria.forClass(SettleConfig.class, "s");
		query.add(Restrictions.eq("s.discount", discount));
		query.add(Restrictions.eq("s.distype", distype));
		query.addOrder(Order.asc("s.addtime"));
		List<SettleConfig> settleConfigList = hibernateTemplate.findByCriteria(query, 0, 1);
		if(settleConfigList.isEmpty()) return null;
		return settleConfigList.get(0);
	}
	
	@Override
	public ErrorCode<DramaSettle> addDramaSettle(Long userid, Long dramaid, Double discount, String distype){
		ErrorCode<SettleConfig> code = addSettleConfig(userid, discount, distype);
		if(!code.isSuccess()) return ErrorCode.getFailure(code.getErrcode(), code.getMsg());
		List<DramaSettle> settleList = baseDao.getObjectListByField(DramaSettle.class, "dramaid", dramaid);
		Map<SettleConfig,DramaSettle> settleMap = BeanUtil.beanListToMap(settleList, "settle");
		DramaSettle settle = settleMap.get(code.getRetval());
		if(settle != null) return ErrorCode.getSuccessReturn(settle);
		settle = new DramaSettle(dramaid, code.getRetval());
		baseDao.saveObject(settle);
		monitorService.saveAddLog(userid, DramaSettle.class, settle.getId(), settle);
		return ErrorCode.getSuccessReturn(settle);
	}
	
	@Override
	public List<SportSettle> getSportSettleList(Long sportid, Long itemid){
		DetachedCriteria query = DetachedCriteria.forClass(SportSettle.class);
		query.add(Restrictions.eq("sportid", sportid));
		query.add(Restrictions.eq("itemid", itemid));
		List result = hibernateTemplate.findByCriteria(query);
		return result;
	}
	
	@Override
	public ErrorCode<SportSettle> addSportSettle(Long userid, Long sportid, Long itemid, Double discount, String distype, String remark){
		if(itemid == null || sportid == null) return ErrorCode.getFailure("sportid和itemid不能为空！ ");
		ErrorCode<SettleConfig> code = addSettleConfig(userid, discount, distype);
		if(!code.isSuccess()) return ErrorCode.getFailure(code.getErrcode(), code.getMsg());
		List<SportSettle> settleList = getSportSettleList(sportid, itemid);
		Map<SettleConfig,SportSettle> settleMap = BeanUtil.beanListToMap(settleList, "settle");
		SportSettle settle = settleMap.get(code.getRetval());
		if(settle != null) return ErrorCode.getSuccessReturn(settle);
		settle = new SportSettle(sportid, itemid, code.getRetval());
		if(StringUtils.isNotBlank(remark)) settle.setRemark(remark);
		baseDao.saveObject(settle);
		monitorService.saveAddLog(userid, SportSettle.class, settle.getId(), settle);
		return ErrorCode.getSuccessReturn(settle);
	}
}
