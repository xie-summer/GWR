package com.gewara.service.sport.impl;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.gewara.constant.ApiConstant;
import com.gewara.constant.GoodsConstant;
import com.gewara.constant.PaymethodConstant;
import com.gewara.constant.Status;
import com.gewara.constant.TagConstant;
import com.gewara.constant.sys.CacheConstant;
import com.gewara.constant.ticket.OpiConstant;
import com.gewara.model.acl.User;
import com.gewara.model.agency.Agency;
import com.gewara.model.agency.AgencyToVenue;
import com.gewara.model.agency.Curriculum;
import com.gewara.model.agency.TrainingGoods;
import com.gewara.model.common.BaseInfo;
import com.gewara.model.goods.TicketGoods;
import com.gewara.model.sport.Sport;
import com.gewara.model.sport.SportItem;
import com.gewara.service.impl.BaseServiceImpl;
import com.gewara.service.sport.AgencyService;
import com.gewara.support.ErrorCode;
import com.gewara.support.ServiceHelper;
import com.gewara.untrans.CacheService;
import com.gewara.untrans.monitor.MonitorService;
import com.gewara.util.BeanUtil;
import com.gewara.util.ChangeEntry;
import com.gewara.util.DateUtil;
import com.gewara.util.JsonUtils;
import com.gewara.util.ValidateUtil;
import com.gewara.util.VmUtils;

@Service("agencyService")
public class AgencyServiceImpl extends BaseServiceImpl implements AgencyService {

	@Autowired@Qualifier("monitorService")
	private MonitorService monitorService;
	@Autowired@Qualifier("cacheService")
	private CacheService cacheService;
	
	@Override
	public List<Agency> getAgencyList(String name, String citycode, String orderField, boolean asc, int from, int maxnum){
		DetachedCriteria query = getQuery(name, citycode);
		if(StringUtils.isNotBlank(orderField)){
			if(asc){
				query.addOrder(Order.asc(orderField));
			}else{
				if(StringUtils.equals(orderField, "isHot")){
					query.addOrder(Order.desc("hotvalue"));
					query.addOrder(Order.desc("generalmark"));
				}else{
					query.addOrder(Order.desc(orderField));
				}
			}
		}
		query.addOrder(Order.asc("id"));
		return hibernateTemplate.findByCriteria(query, from, maxnum);
	}
	@Override
	public int getAgencyCount(String name, String citycode){
		DetachedCriteria query = getQuery(name, citycode);
		query.setProjection(Projections.rowCount());
		List result = hibernateTemplate.findByCriteria(query);
		if(result.isEmpty()) return 0;
		return Integer.parseInt("" + result.get(0));
	}
	@Override
	public List<TrainingGoods> getTrainingGoodsList(String citycode, String tag, Long relatedid, String itemtype, Long itemid, Long placeid, String order, boolean asc, boolean isTovaltime, int from, int maxnum){
		DetachedCriteria query = queryCommonTrainingGoods(citycode, tag, relatedid, itemtype, itemid, placeid, null, null, isTovaltime);
		if(StringUtils.isNotBlank(order)){
			if(asc){
				query.addOrder(Order.asc(order));
			}else{
				query.addOrder(Order.desc(order));
			}
		}
		query.addOrder(Order.asc("id"));
		return hibernateTemplate.findByCriteria(query, from, maxnum);
	}
	@Override
	public int getTrainingGoodsCount(String citycode, String tag, Long relatedid, String itemtype, Long itemid, Long placeid, boolean isTovaltime){
		DetachedCriteria query = queryCommonTrainingGoods(citycode, tag, relatedid, itemtype, itemid, placeid, null, null, isTovaltime);
		query.setProjection(Projections.rowCount());
		List result = hibernateTemplate.findByCriteria(query);
		if(result.isEmpty()) return 0;
		return Integer.parseInt("" + result.get(0));
	}
	@Override
	public List<TrainingGoods> getTrainingGoodsList(String citycode, Long relatedid, Long itemid, String fitcrowd, String timetype, List<Long> sportIdList,
			Integer fromprice, Integer toprice, String searchKey, String order, boolean asc, int from, int maxnum){
		DetachedCriteria query = queryCommonTrainingGoods(citycode, TagConstant.TAG_AGENCY, relatedid, "sportitem", itemid, null, fitcrowd, timetype, true);
		if(!VmUtils.isEmptyList(sportIdList)) query.add(Restrictions.in("placeid", sportIdList));
		if(StringUtils.isNotBlank(searchKey)) query.add(Restrictions.like("goodsname", searchKey, MatchMode.ANYWHERE));
		if(fromprice != null && toprice != null){
			query.add(Restrictions.ge("minprice", fromprice));
			query.add(Restrictions.le("minprice", toprice));
		}
		if(StringUtils.isNotBlank(order)){
			if(asc){
				query.addOrder(Order.asc(order));
			}else{
				query.addOrder(Order.desc(order));
			}
		}
		query.addOrder(Order.desc("addtime"));
		return hibernateTemplate.findByCriteria(query, from, maxnum);
	}
	@Override
	public  int getTrainingGoodsCount(String citycode, Long relatedid, Long itemid, String fitcrowd, String timetype, List<Long> sportIdList,
			Integer fromprice, Integer toprice, String searchKey){
		DetachedCriteria query = queryCommonTrainingGoods(citycode, TagConstant.TAG_AGENCY, relatedid, "sportitem", itemid, null, fitcrowd, timetype, true);
		if(!VmUtils.isEmptyList(sportIdList)) query.add(Restrictions.in("placeid", sportIdList));
		if(StringUtils.isNotBlank(searchKey)) query.add(Restrictions.like("goodsname", searchKey, MatchMode.ANYWHERE));
		if(fromprice != null && toprice != null){
			query.add(Restrictions.ge("minprice", fromprice));
			query.add(Restrictions.le("minprice", toprice));
		}
		query.setProjection(Projections.rowCount());
		List result = hibernateTemplate.findByCriteria(query);
		if(result.isEmpty()) return 0;
		return Integer.parseInt("" + result.get(0));
	}
	@Override
	public List<SportItem> getAgencySportItemList(Long agencyId, String citycode){
		String key = CacheConstant.buildKey("getAgencySportItemListxa23sz11", agencyId, citycode);
		List<Long> siIdList = (List<Long>) cacheService.get(CacheConstant.REGION_ONEHOUR, key);
		if(siIdList == null){
			DetachedCriteria query = queryCommonTrainingGoods(citycode, TagConstant.TAG_AGENCY, agencyId, null, null, null, null, null, true);
			query.setProjection(Projections.distinct(Projections.property("itemid")));
			siIdList = hibernateTemplate.findByCriteria(query);
			cacheService.set(CacheConstant.REGION_ONEHOUR, key, siIdList);
		}
		return baseDao.getObjectList(SportItem.class, siIdList);
	}
	@Override
	public  ErrorCode<TrainingGoods> saveTrainingGoods(Long gid, String citycode, String goodsname, String tag, Long relatedid, String itemtype, 
			Long itemid, Long placeid, Timestamp fromvalidtime, Timestamp tovalidtime, String summary, String description,String fitcrowd, String timetype,
			String seotitle, String seodescription, Integer quantity, String showtime, Integer minquantity, User user){
		if(StringUtils.isBlank(goodsname)) return ErrorCode.getFailure(ApiConstant.CODE_DATA_ERROR, "请课程名称！");
		if(StringUtils.isBlank(citycode)) return ErrorCode.getFailure(ApiConstant.CODE_DATA_ERROR, "城市编码不能为空！");
		if(quantity == null) return ErrorCode.getFailure(ApiConstant.CODE_DATA_ERROR, "招生数量不能为空！");
		if(relatedid == null) return ErrorCode.getFailure(ApiConstant.CODE_DATA_ERROR, "培训机构不能为空！");
		if(itemid == null) return ErrorCode.getFailure(ApiConstant.CODE_DATA_ERROR, "培训项目不能为空！");
		
		ChangeEntry changeEntry = null;
		TrainingGoods training = null;
		if(gid != null){
			training = baseDao.getObject(TrainingGoods.class, gid);
			if(training == null) return ErrorCode.getFailure(ApiConstant.CODE_DATA_ERROR, "该课程不存在或被删除！");
			changeEntry = new ChangeEntry(training);
		}else{
			training = new TrainingGoods(tag, relatedid, itemtype, itemid);
			training.setClerkid(user.getId());
			training.setManager(GoodsConstant.MANAGER_USER);
			BaseInfo baseInfo = baseDao.getObject(ServiceHelper.getPalceClazz(tag), relatedid);
			String palceCitycode = (String) BeanUtil.get(baseInfo, "citycode");
			if(StringUtils.isNotBlank(palceCitycode)){
				training.setCitycode(palceCitycode);
			}else{
				training.setCitycode(citycode);
			}
			if(training.getReleasetime() == null){
				training.setReleasetime(DateUtil.getCurFullTimestamp());
			}
			if(fromvalidtime == null){
				training.setFromtime(DateUtil.getCurFullTimestamp());
			}else{
				training.setFromtime(fromvalidtime);
			}
			if(tovalidtime == null){
				training.setTotime(DateUtil.getCurFullTimestamp());
			}else{
				training.setTotime(tovalidtime);
			}
		}
		clearTrainingGoodsPreferential(training);
		training.setFeetype(GoodsConstant.FEETYPE_T);
		training.setServicetype(TagConstant.TAG_SPORT);
		training.setFromvalidtime(fromvalidtime);
		training.setTovalidtime(tovalidtime);
		training.setPlaceid(placeid);
		if(placeid != null){
			Sport sport = baseDao.getObject(Sport.class, placeid);
			if(sport == null) return ErrorCode.getFailure(ApiConstant.CODE_DATA_ERROR, "运动场馆不存在或被删除！");
			training.setPlacename(sport.getName());
		}
		if(StringUtils.isNotBlank(showtime)){
			Map otherinfoMap = JsonUtils.readJsonToMap(training.getOtherinfo());
			otherinfoMap.put("showtime", showtime);
			training.setOtherinfo(JsonUtils.writeMapToJson(otherinfoMap));
		}
		training.setMinquantity(minquantity);
		training.setQuantity(quantity);
		training.setAllowaddnum(quantity);
		training.setGoodsname(goodsname);
		training.setSummary(summary);
		training.setDescription(description);
		training.setFitcrowd(fitcrowd);
		training.setTimetype(timetype);
		training.setSeotitle(seotitle);
		training.setSeodescription(seodescription);
		String msg=ValidateUtil.validateNewsContent(null, training.getDescription());
		if(StringUtils.isNotBlank(msg)) return ErrorCode.getFailure(ApiConstant.CODE_DATA_ERROR, msg);
		baseDao.saveObject(training);
		if(changeEntry != null){
			monitorService.saveChangeLog(user.getId(), TicketGoods.class, training.getId(), changeEntry.getChangeMap(training));
		}else{
			monitorService.saveAddLog(user.getId(), TicketGoods.class, training.getId(), training);
		}
		return ErrorCode.getSuccessReturn(training);
	}
	@Override
	public List<Curriculum> getCurriculumList(Long relatedid, Date playDate){
		DetachedCriteria query = DetachedCriteria.forClass(Curriculum.class);
		query.add(Restrictions.eq("relatedid", relatedid));
		query.add(Restrictions.le("fromdate", DateUtil.getMonthLastDay(playDate)));
		query.add(Restrictions.ge("todate", playDate));
		query.addOrder(Order.asc("classtime"));
		return hibernateTemplate.findByCriteria(query); 
	}
	@Override
	public List<AgencyToVenue> getATVList(Long agencyId, Long venueId){
		DetachedCriteria query = DetachedCriteria.forClass(AgencyToVenue.class);
		query.add(Restrictions.eq("agencyId", agencyId));
		if(venueId != null) query.add(Restrictions.eq("venueId", venueId));
		query.add(Restrictions.gt("numsort", 0));
		query.addOrder(Order.asc("numsort"));
		return hibernateTemplate.findByCriteria(query);
	}
	@Override
	public void clearTrainingGoodsPreferential(TrainingGoods goods){
		goods.setMinpoint(0);
		goods.setMaxpoint(0);
		goods.setElecard("");
		Map<String,String> otherinfoMap = JsonUtils.readJsonToMap(goods.getOtherinfo());
		String payoption = otherinfoMap.get(OpiConstant.PAYOPTION);
		String paymethod = otherinfoMap.get(OpiConstant.PAYCMETHODLIST);
		List<String> paymethodList = new ArrayList<String>();
		if(StringUtils.isNotBlank(paymethod)){
			paymethodList.addAll(Arrays.asList(StringUtils.split(paymethod, ",")));
		}
		if(StringUtils.isNotBlank(payoption)){
			if(StringUtils.equals(payoption, "notuse")){
				if(!paymethodList.contains(PaymethodConstant.PAYMETHOD_GEWAPAY)){
					paymethodList.add(PaymethodConstant.PAYMETHOD_GEWAPAY);
				}
			}else{
				if(paymethodList.contains(PaymethodConstant.PAYMETHOD_GEWAPAY)){
					paymethodList.remove(PaymethodConstant.PAYMETHOD_GEWAPAY);
				}
			}
		}else{
			otherinfoMap.put(OpiConstant.PAYOPTION, "notuse");
			if(!paymethodList.contains(PaymethodConstant.PAYMETHOD_GEWAPAY)){
				paymethodList.add(PaymethodConstant.PAYMETHOD_GEWAPAY);
			}
		}
		otherinfoMap.put(OpiConstant.PAYCMETHODLIST, StringUtils.join(paymethodList, ","));
		goods.setOtherinfo(JsonUtils.writeMapToJson(otherinfoMap));
	}
	
	private DetachedCriteria queryCommonTrainingGoods(String citycode, String tag, Long relatedid, String itemtype, Long itemid, Long placeid, String fitcrowd, String timetype, boolean isTovaltime){
		DetachedCriteria query = DetachedCriteria.forClass(TrainingGoods.class);
		if(isTovaltime){
			Timestamp cur = DateUtil.getCurFullTimestamp();
			query.add(Restrictions.le("fromtime",cur));
			query.add(Restrictions.ge("totime", cur));
			query.add(Restrictions.eq("status", Status.Y));
		}
		if(StringUtils.isNotBlank(citycode)) query.add(Restrictions.eq("citycode", citycode));
		if(StringUtils.isNotBlank(tag)) query.add(Restrictions.eq("tag", tag));
		if(relatedid != null) query.add(Restrictions.eq("relatedid", relatedid));
		if(StringUtils.isNotBlank(itemtype)) query.add(Restrictions.eq("itemtype", itemtype));
		if(itemid != null) query.add(Restrictions.eq("itemid", itemid));
		if(placeid != null) query.add(Restrictions.eq("placeid", placeid));
		if(StringUtils.isNotBlank(fitcrowd)) query.add(Restrictions.eq("fitcrowd", fitcrowd));
		if(StringUtils.isNotBlank(timetype)) query.add(Restrictions.eq("timetype", timetype));
		return query;
	}
	
	private DetachedCriteria getQuery(String name, String citycode){
		DetachedCriteria query = DetachedCriteria.forClass(Agency.class);
		if(StringUtils.isNotBlank(citycode)) query.add(Restrictions.eq("citycode", citycode));
		if(StringUtils.isNotBlank(name)) query.add(Restrictions.like("name", name, MatchMode.ANYWHERE));
		query.add(Restrictions.eq("status", Status.Y));
		return query;
	}
}
