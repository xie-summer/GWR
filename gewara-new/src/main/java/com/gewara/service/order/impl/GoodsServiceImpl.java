package com.gewara.service.order.impl;

import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang.StringUtils;
import org.hibernate.Hibernate;
import org.hibernate.criterion.Conjunction;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projection;
import org.hibernate.criterion.ProjectionList;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.criterion.Subqueries;
import org.hibernate.type.Type;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.gewara.constant.ApiConstant;
import com.gewara.constant.GoodsConstant;
import com.gewara.constant.Status;
import com.gewara.constant.ticket.OrderConstant;
import com.gewara.helper.GoodsPriceHelper;
import com.gewara.model.acl.User;
import com.gewara.model.bbs.commu.CommuManage;
import com.gewara.model.common.BaseInfo;
import com.gewara.model.drama.TheatreRoom;
import com.gewara.model.goods.ActivityGoods;
import com.gewara.model.goods.BaseGoods;
import com.gewara.model.goods.Goods;
import com.gewara.model.goods.GoodsDisQuantity;
import com.gewara.model.goods.GoodsGift;
import com.gewara.model.goods.GoodsPrice;
import com.gewara.model.goods.SportGoods;
import com.gewara.model.goods.TicketGoods;
import com.gewara.model.pay.GoodsOrder;
import com.gewara.model.ticket.PlayRoom;
import com.gewara.model.user.Member;
import com.gewara.service.impl.BaseServiceImpl;
import com.gewara.service.order.GoodsService;
import com.gewara.support.ErrorCode;
import com.gewara.support.ServiceHelper;
import com.gewara.untrans.monitor.MonitorService;
import com.gewara.util.BeanUtil;
import com.gewara.util.BindUtils;
import com.gewara.util.ChangeEntry;
import com.gewara.util.DateUtil;
import com.gewara.util.StringUtil;
import com.gewara.util.ValidateUtil;
@Service("goodsService")
public class GoodsServiceImpl extends BaseServiceImpl implements GoodsService {
	@Autowired@Qualifier("monitorService")
	protected MonitorService monitorService;
	public void setMonitorService(MonitorService monitorService) {
		this.monitorService = monitorService;
	}
	@Override
	public <T extends BaseGoods> List<T> getCurGoodsList(Class<T> clazz, String tag, Long relatedid, int from, int maxnum) {
		DetachedCriteria query = DetachedCriteria.forClass(clazz);
		query.add(Restrictions.ne("status", Status.DEL));
		if(StringUtils.isNotBlank(tag)) query.add(Restrictions.eq("tag", tag));
		if(relatedid!=null) query.add(Restrictions.eq("relatedid", relatedid));
		Timestamp cur = new Timestamp(System.currentTimeMillis());
		query.add(Restrictions.gt("totime", cur));
		query.add(Restrictions.le("releasetime", cur));
		query.add(Restrictions.gt("goodssort", 0));
		query.addOrder(Order.asc("goodssort"));
		List<T> goodsList = hibernateTemplate.findByCriteria(query, from, maxnum);
		return goodsList;
	}

	@Override
	public <T extends BaseGoods> List<T> getGoodsList(Class<T> clazz, String citycode, String tag, Long relatedid, boolean isTotime, boolean limitRelease, boolean isGtZero,  String order, boolean asc) {
		return getGoodsList(clazz, citycode, tag, relatedid, isTotime, limitRelease, isGtZero, order, asc, -1, -1);
	}
	@Override
	public <T extends BaseGoods> List<T> getGoodsList(Class<T> clazz, String citycode, String tag, Long relatedid, boolean isTotime, boolean limitRelease, boolean isGtZero,  String order, boolean asc, int from, int maxnum) {
		return getGoodsList(clazz, citycode, tag, relatedid, null, isTotime, limitRelease, isGtZero, order, asc, from, maxnum);
	}
	@Override
	public <T extends BaseGoods> List<T> getGoodsList(Class<T> clazz, String citycode, String tag, Long relatedid, String status, boolean isTotime, boolean limitRelease, boolean isGtZero,  String order, boolean asc, int from, int maxnum) {
		if(StringUtils.isBlank(order)) order = "addtime";
		DetachedCriteria query = DetachedCriteria.forClass(clazz);
		if(StringUtils.isNotBlank(status)){
			query.add(Restrictions.eq("status", status));
		}else{
			query.add(Restrictions.ne("status", Status.DEL));
		}
		if(StringUtils.isNotBlank(citycode)) query.add(Restrictions.eq("citycode", citycode));
		if(StringUtils.isNotBlank(tag)) query.add(Restrictions.like("tag", tag, MatchMode.ANYWHERE));
		if(relatedid!=null) query.add(Restrictions.eq("relatedid", relatedid));
		if(isTotime)query.add(Restrictions.gt("totime", DateUtil.getCurFullTimestamp()));
		if(limitRelease) query.add(Restrictions.le("releasetime", new Timestamp(System.currentTimeMillis())));
		if(isGtZero) query.add(Restrictions.gt("goodssort", 0));
		if(asc) query.addOrder(Order.asc(order));
		else query.addOrder(Order.desc(order));
		List<T> goodsList = hibernateTemplate.findByCriteria(query, from, maxnum);
		return goodsList;
	}
	@Override
	public <T extends BaseGoods> List<T> getGoodsList(Class<T> clazz, String tag, Long relatedid, boolean isTotime, boolean limitRelease, boolean isGtZero,  String order, boolean asc, boolean isGift) {
		if(StringUtils.isBlank(order)) order = "addtime";
		Timestamp curtime = new Timestamp(System.currentTimeMillis());
		DetachedCriteria query = DetachedCriteria.forClass(clazz, "g");
		query.add(Restrictions.ne("g.status", Status.DEL));
		if(StringUtils.isNotBlank(tag)) query.add(Restrictions.eq("g.tag", tag));
		if(relatedid!=null) query.add(Restrictions.eq("g.relatedid", relatedid));
		if(isTotime) query.add(Restrictions.gt("g.totime", curtime));
		if(limitRelease) query.add(Restrictions.le("g.releasetime", curtime));
		if(isGtZero) query.add(Restrictions.gt("g.goodssort", 0));
		
		DetachedCriteria sub = DetachedCriteria.forClass(GoodsGift.class, "f");
		sub.add(Restrictions.eqProperty("f.goodsid", "g.id"));
		sub.setProjection(Projections.property("f.id"));
		if(isGift){
			query.add(Subqueries.exists(sub));
		}else {
			query.add(Subqueries.notExists(sub));
		}
		
		if(asc) query.addOrder(Order.asc(order));
		else query.addOrder(Order.desc(order));
		List<T> goodsList = hibernateTemplate.findByCriteria(query);
		return goodsList;
	}
	
	@Override
	public <T extends BaseGoods> T getGoodsByTagAndRelatedid(Class<T> clazz, String tag, Long relatedid, boolean isTotime, boolean limitRelease, boolean isGtZero){
		Timestamp curtime = new Timestamp(System.currentTimeMillis());
		DetachedCriteria query = DetachedCriteria.forClass(clazz, "g");
		query.add(Restrictions.ne("g.status", Status.DEL));
		if(StringUtils.isNotBlank(tag)) query.add(Restrictions.eq("g.tag", tag));
		if(relatedid != null) query.add(Restrictions.eq("g.relatedid", relatedid));
		if(isTotime) query.add(Restrictions.gt("g.totime", curtime));
		if(limitRelease) query.add(Restrictions.le("g.releasetime", curtime));
		if(isGtZero) query.add(Restrictions.gt("g.goodssort", 0));
		query.add(Restrictions.sqlRestriction(" 1=1 order by abs(unitprice - costprice) desc"));
		List<T> goodsList = hibernateTemplate.findByCriteria(query, 0, 1);
		if(goodsList.isEmpty()) return null;
		return goodsList.get(0);
	}
	
	@Override
	public <T extends BaseGoods> Integer getGoodsCount(Class<T> clazz, String tag, Long relatedid, boolean isTotime, boolean limitRelease, boolean isGtZero) {
		DetachedCriteria query = DetachedCriteria.forClass(clazz);
		query.add(Restrictions.ne("status", Status.DEL));
		if(StringUtils.isNotBlank(tag)) query.add(Restrictions.eq("tag", tag));
		if(relatedid!=null) query.add(Restrictions.eq("relatedid", relatedid));
		if(isTotime) query.add(Restrictions.gt("totime", new Timestamp(System.currentTimeMillis())));
		if(limitRelease) query.add(Restrictions.le("releasetime", new Timestamp(System.currentTimeMillis())));
		if(isGtZero) query.add(Restrictions.gt("goodssort", 0));
		query.setProjection(Projections.rowCount());
		List<Long> list = hibernateTemplate.findByCriteria(query);
		if (list.isEmpty()) return 0;
		return Integer.parseInt(""+list.get(0));
	}
	@Override
	public Integer getBuyGoodsMemberCount(Long gid){
		DetachedCriteria query = getGoodsOrderDc(gid);
		query.setProjection(Projections.rowCount());
		List result = hibernateTemplate.findByCriteria(query);
		return Integer.valueOf(result.get(0)+"");
	}
	private DetachedCriteria getGoodsOrderDc(Long gid){
		DetachedCriteria query = DetachedCriteria.forClass(GoodsOrder.class);
		query.add(Restrictions.eq("goodsid", gid));
		query.add(Restrictions.eq("status", OrderConstant.STATUS_PAID_SUCCESS));
		return query;
	}
	@Override
	public <T extends BaseGoods> List<T>  getGoodsListByStatusAndTag(Class<T> clazz, String status, String tag, int from, int maxnum){
		DetachedCriteria query = DetachedCriteria.forClass(clazz);
		query.add(Restrictions.eq("status", status));
		query.add(Restrictions.eq("tag", tag));
		query.addOrder(Order.desc("addtime"));
		return hibernateTemplate.findByCriteria(query, from, maxnum);
	}
	@Override
	public <T extends BaseGoods> Integer countByGoodsListByStatusAndTag(Class<T> clazz, String status, String tag){
		DetachedCriteria query = DetachedCriteria.forClass(clazz);
		query.add(Restrictions.eq("status", status));
		query.add(Restrictions.eq("tag", tag));
		query.setProjection(Projections.rowCount());
		List<CommuManage> list = hibernateTemplate.findByCriteria(query);
		if (list.isEmpty()) return 0;
		return Integer.parseInt(""+list.get(0));
	}
	@Override
	public <T extends BaseGoods> Integer getBuyGoodsCount(Class<T> clazz, Long gid, Timestamp time, Long relatedid, String tag) {
		DetachedCriteria query = DetachedCriteria.forClass(GoodsOrder.class, "go");
		if(gid != null) query.add(Restrictions.eq("go.goodsid", gid));
		query.add(Restrictions.eq("go.status", OrderConstant.STATUS_PAID_SUCCESS));
		query.add(Restrictions.gt("go.paidtime", time));
		
		DetachedCriteria subQuery = DetachedCriteria.forClass(Goods.class, "g");
		subQuery.add(Restrictions.eqProperty("go.goodsid", "g.id"));
		if(relatedid != null)subQuery.add(Restrictions.eq("g.relatedid", relatedid));
		if(StringUtils.isNotBlank(tag))subQuery.add(Restrictions.eq("g.tag", tag));
		subQuery.setProjection(Projections.property("id"));
		
		query.add(Subqueries.exists(subQuery));
		query.setProjection(Projections.rowCount());
		List result = hibernateTemplate.findByCriteria(query);
		if (result.isEmpty()) return 0;
		return Integer.parseInt("" + result.get(0));
	}

	
	@Override
	public List<SportGoods> getSportGoodsList(String citycode, String tag, Long relatedid, boolean isTotime, boolean limitRelease, boolean isGtZero, String order, boolean asc) {
		if(StringUtils.isBlank(order)) order = "addtime";
		DetachedCriteria query = DetachedCriteria.forClass(SportGoods.class);
		query.add(Restrictions.ne("status", Status.DEL));
		if(StringUtils.isNotBlank(citycode)) query.add(Restrictions.eq("citycode", citycode));
		if(StringUtils.isNotBlank(tag)) query.add(Restrictions.eq("tag", tag));
		if(relatedid!=null) query.add(Restrictions.eq("relatedid", relatedid));
		if(isTotime){
			query.add(Restrictions.gt("totime", DateUtil.getCurFullTimestamp()));
		}else{
			query.add(Restrictions.lt("totime", DateUtil.getCurFullTimestamp()));
		}
		if(limitRelease) query.add(Restrictions.le("releasetime", new Timestamp(System.currentTimeMillis())));
		if(isGtZero) query.add(Restrictions.gt("goodssort", 0));
		if(asc) query.addOrder(Order.asc(order));
		else query.addOrder(Order.desc(order));
		List<SportGoods> goodsList = hibernateTemplate.findByCriteria(query);
		return goodsList;
	}

	@Override
	public List<SportGoods> getSportGoodsListBySportidAndItemid(Long sportid, Long itemid, Timestamp playDate, int from, int maxnum) {
		DetachedCriteria query = DetachedCriteria.forClass(SportGoods.class);
		if(sportid!=null) query.add(Restrictions.eq("relatedid", sportid));
		if(itemid!=null) query.add(Restrictions.eq("itemid", itemid));
		query.add(Restrictions.eq("status", Status.Y));
		query.add(Restrictions.gt("quantity", 0));
		if(playDate != null){
			query.add(Restrictions.eq("releasetime", playDate));
		}
		query.add(Restrictions.le("fromtime", DateUtil.getCurFullTimestamp()));
		query.add(Restrictions.ge("totime", DateUtil.getCurFullTimestamp()));
		query.addOrder(Order.asc("addtime"));
		List<SportGoods> list = hibernateTemplate.findByCriteria(query, from, maxnum);
		return list;
	}

	@Override
	public List<Timestamp> getSportGoodsReleasetime(Long sportid, Long itemid, int from, int maxnum) {
		DetachedCriteria query = DetachedCriteria.forClass(SportGoods.class);
		if(sportid!=null) query.add(Restrictions.eq("relatedid", sportid));
		if(itemid!=null) query.add(Restrictions.eq("itemid", itemid));
		query.add(Restrictions.eq("status", Status.Y));
		query.add(Restrictions.ge("releasetime", DateUtil.getCurTruncTimestamp()));
		query.setProjection(Projections.groupProperty("releasetime"));
		query.addOrder(Order.asc("releasetime"));
		List<Timestamp> list = hibernateTemplate.findByCriteria(query, from, maxnum);
		return list;
	}

	@Override
	public Integer getSportGoodsReleasetimeCount(Long sportid, Long itemid) {
		DetachedCriteria query = DetachedCriteria.forClass(SportGoods.class);
		if(sportid!=null) query.add(Restrictions.eq("relatedid", sportid));
		if(itemid!=null) query.add(Restrictions.eq("itemid", itemid));
		query.add(Restrictions.eq("status", Status.Y));
		query.add(Restrictions.eq("releasetime", DateUtil.getCurTruncTimestamp()));
		query.setProjection(Projections.projectionList()
				.add(Projections.groupProperty("releasetime")));
		List list = hibernateTemplate.findByCriteria(query);
		if (list.isEmpty()) return 0;
		return Integer.parseInt(""+list.size());
	}

	@Override
	public Integer getSportGoodsCount(){
		DetachedCriteria query = DetachedCriteria.forClass(SportGoods.class);
		query.add(Restrictions.le("fromtime", DateUtil.getCurFullTimestamp()));
		query.add(Restrictions.ge("totime", DateUtil.getCurFullTimestamp()));
		query.add(Restrictions.eq("status", Status.Y));
		query.add(Restrictions.gt("quantity", 0));
		query.setProjection(Projections.projectionList()
				.add(Projections.groupProperty("relatedid")));
		List list = hibernateTemplate.findByCriteria(query);
		if (list.isEmpty()) return 0;
		return Integer.parseInt(""+list.size());
	}
	
	@Override
	public ErrorCode<ActivityGoods> saveOrUpdateActivityGoods(Long userid, Long gid, Map<String, String> dataMap){
		ActivityGoods goods = null;
		String activityId = dataMap.get("relatedid");
		if(StringUtils.isBlank(activityId)) return ErrorCode.getFailure(ApiConstant.CODE_DATA_ERROR, "关联活动不能为空！");
		String manager = dataMap.get("manager");
		if(!GoodsConstant.MANAGER_LIST.contains(manager)) return ErrorCode.getFailure(ApiConstant.CODE_DATA_ERROR, "角色类型错误！");
		Long relatedid = new Long(activityId);
		String goodsname = dataMap.get("goodsname");
		if(StringUtils.isBlank(goodsname)) return ErrorCode.getFailure(ApiConstant.CODE_DATA_ERROR, "商品名称不能为空！");
		String fromtime = dataMap.get("fromtime");
		if(StringUtils.isBlank(fromtime)) return ErrorCode.getFailure(ApiConstant.CODE_DATA_ERROR, "预订开始时间不能为空！");
		String totime = dataMap.get("totime");
		if(StringUtils.isBlank(totime)) return ErrorCode.getFailure(ApiConstant.CODE_DATA_ERROR, "预订截止时间不能为空！");
		if(DateUtil.parseTimestamp(fromtime).after(DateUtil.parseTimestamp(totime)))  return ErrorCode.getFailure(ApiConstant.CODE_DATA_ERROR, "预订开始时间不能在预订截止时间之后！");
		String price = dataMap.get("unitprice");
		if(StringUtils.isBlank(price)) return ErrorCode.getFailure(ApiConstant.CODE_DATA_ERROR, "单价不能为空！");
		Integer unitprice = Integer.valueOf(price);
		String memberId = dataMap.get("clerkid");
		if(StringUtils.isBlank(memberId)) return ErrorCode.getFailure(ApiConstant.CODE_DATA_ERROR, "发起者不能为空！");
		Long clerkid = Long.valueOf(memberId);
		if(StringUtils.equals(manager, GoodsConstant.MANAGER_MEMBER)){
			Member member = baseDao.getObject(Member.class, clerkid);
			if(member == null) return ErrorCode.getFailure(ApiConstant.CODE_DATA_ERROR, "发起者不存在！");
		}else if(StringUtils.equals(manager, GoodsConstant.MANAGER_USER)){
			User user = baseDao.getObject(User.class, clerkid);
			if(user == null) return ErrorCode.getFailure(ApiConstant.CODE_DATA_ERROR, "发起者不存在！");
		}
		ChangeEntry changeEntry = null;
		if(gid != null){
			goods = baseDao.getObject(ActivityGoods.class, gid);
			if(goods == null) return ErrorCode.getFailure(ApiConstant.CODE_DATA_ERROR, "该商品不存在或被删除！");
			if(!goods.getRelatedid().equals(relatedid)) return ErrorCode.getFailure(ApiConstant.CODE_SIGN_ERROR, "不能更改关联活动！");
			if(!goods.getClerkid().equals(clerkid)) return ErrorCode.getFailure(ApiConstant.CODE_SIGN_ERROR, "不能修改他人商品信息！");
			changeEntry = new ChangeEntry(goods);
		}else{
			goods = new ActivityGoods(relatedid, goodsname, unitprice, clerkid);
		}
		BindUtils.bindData(goods, dataMap);
		if(goods.getReleasetime() == null) goods.setReleasetime(DateUtil.getCurFullTimestamp());
		if(StringUtils.isBlank(goods.getCitycode())) return ErrorCode.getFailure(ApiConstant.CODE_SIGN_ERROR, "城市代码不能为空！");
		if(goods.getFromtime() == null){
			goods.setFromtime(goods.getAddtime());
		}
		if(goods.getAllowaddnum()==null){
			goods.setAllowaddnum(0);
		}
		if(goods.getMaxpoint() == null){
			goods.setMaxpoint(0);
		}
		if(goods.getMinpoint() == null){
			goods.setMinpoint(0);
		}
		//验证内容
		String msg=ValidateUtil.validateNewsContent(null, goods.getDescription());
		if(StringUtils.isNotBlank(msg)) return ErrorCode.getFailure(ApiConstant.CODE_DATA_ERROR, msg);
		baseDao.saveObject(goods);
		if(changeEntry != null) monitorService.saveChangeLog(userid, ActivityGoods.class, goods.getId(), changeEntry.getChangeMap(goods));
		else monitorService.saveAddLog(userid, ActivityGoods.class, goods.getId(), goods);
		return ErrorCode.getSuccessReturn(goods);
	}
	
	@Override
	public ErrorCode<TicketGoods> saveCommonTicket(Long gid, String citycode, String goodsname, String tag, Long relatedid, String itemtype,Long itemid,
			Long starid, Long roomid, Timestamp fromvalidtime, Timestamp tovalidtime, String language, String summary, String description,
			Integer maxbuy, String period, User user){
		if(StringUtils.isBlank(period)) return ErrorCode.getFailure(ApiConstant.CODE_DATA_ERROR, "场次类型不能为空！");
		if(StringUtils.equals(period, Status.N)){
			if(StringUtils.isBlank(goodsname))
			return ErrorCode.getFailure(ApiConstant.CODE_DATA_ERROR, "请输入物品名称！");
		}else{
			goodsname = DateUtil.format(fromvalidtime, "yyyy-MM-dd") + " " + DateUtil.getCnWeek(fromvalidtime) + " " + DateUtil.format(fromvalidtime, "HH:mm");
		}
		if(StringUtils.isBlank(citycode)) return ErrorCode.getFailure(ApiConstant.CODE_DATA_ERROR, "城市编码不能为空！");
		if(fromvalidtime == null) return ErrorCode.getFailure(ApiConstant.CODE_DATA_ERROR, "有效开始时间不能为空！");
		if(tovalidtime == null) return ErrorCode.getFailure(ApiConstant.CODE_DATA_ERROR, "有效结束时间不能为空！");
		if(roomid == null) return ErrorCode.getFailure(ApiConstant.CODE_DATA_ERROR, "演出厅不能为空！");
		if(maxbuy == null ||maxbuy < 1) return ErrorCode.getFailure(ApiConstant.CODE_DATA_ERROR, "单次购买数不能为空或不能小于1！");
		ChangeEntry changeEntry = null;
		TicketGoods goods = null;
		if(gid != null){
			goods = baseDao.getObject(TicketGoods.class, gid);
			if(goods == null) return ErrorCode.getFailure(ApiConstant.CODE_DATA_ERROR, "该商品不存在或被删除！");
			changeEntry = new ChangeEntry(goods);
		}else{
			goods = new TicketGoods(tag, relatedid, itemtype, itemid);
			goods.setClerkid(user.getId());
			goods.setManager(GoodsConstant.MANAGER_USER);
			BaseInfo baseInfo = baseDao.getObject(ServiceHelper.getPalceClazz(tag), relatedid);
			String palceCitycode = (String) BeanUtil.get(baseInfo, "citycode");
			if(StringUtils.isNotBlank(palceCitycode)){
				goods.setCitycode(palceCitycode);
			}else{
				goods.setCitycode(citycode);
			}
			goods.setFromtime(fromvalidtime);
			goods.setTotime(tovalidtime);
			if(goods.getReleasetime() == null){
				goods.setReleasetime(DateUtil.getCurFullTimestamp());
			}
			goods.setGoodssort(1);
			goods.setPeriod(period);
		}
		PlayRoom room = baseDao.getObject(PlayRoom.class, roomid);
		if(room == null) return ErrorCode.getFailure(ApiConstant.CODE_DATA_ERROR, "演出厅不存在或被删除！");
		goods.setStarid(starid);
		goods.setRoomid(roomid);
		goods.setRoomname(room.getRoomname());
		goods.setFromvalidtime(fromvalidtime);
		goods.setTovalidtime(tovalidtime);
		goods.setGoodsname(goodsname);
		goods.setLanguage(language);
		goods.setSummary(summary);
		goods.setMaxbuy(maxbuy);
		goods.setDescription(description);
		String msg=ValidateUtil.validateNewsContent(null, goods.getDescription());
		if(StringUtils.isNotBlank(msg)) return ErrorCode.getFailure(ApiConstant.CODE_DATA_ERROR, msg);
		baseDao.saveObject(goods);
		if(changeEntry != null){
			monitorService.saveChangeLog(user.getId(), TicketGoods.class, goods.getId(), changeEntry.getChangeMap(goods));
		}else{
			monitorService.saveAddLog(user.getId(), TicketGoods.class, goods.getId(), goods);
		}
		return ErrorCode.getSuccessReturn(goods);
	}
	
	@Override
	public List<TicketGoods> getTicketGoodsList(String citycode, String tag, Long relatedid, String itemtype, Long itemid, String period, Timestamp fromtime, Timestamp totime, boolean isTovaltime, boolean isGtZero){
		DetachedCriteria query = queryCommonTicketGoods(citycode, tag, relatedid, itemtype, itemid, isTovaltime, isGtZero);
		if(StringUtils.isNotBlank(period)){
			query.add(Restrictions.eq("period", period));
		}
		if(fromtime != null){
			if(StringUtils.equals(period, Status.Y)){
				query.add(Restrictions.ge("fromvalidtime", fromtime));
			}else{
				query.add(Restrictions.ge("tovalidtime", fromtime));
			}
		}
		if(totime != null){
			if(StringUtils.equals(period, Status.Y)){
				query.add(Restrictions.le("fromvalidtime", totime));
			}else{
				query.add(Restrictions.le("tovalidtime", totime));
			}
		}
		query.addOrder(Order.desc("goodssort"));
		query.addOrder(Order.asc("fromvalidtime"));
		return hibernateTemplate.findByCriteria(query);
	}
	
	@Override
	public List<TicketGoods> getTicketGoodsList(String citycode, String tag, Long relatedid, String itemtype, Long itemid, boolean isTovaltime, boolean isGtZero){
		return getTicketGoodsList(citycode, tag, relatedid, itemtype, itemid, null, null, null, isTovaltime, isGtZero);
	}
	@SuppressWarnings("deprecation")
	@Override
	public List<Map<String, String>> getTicketGoodsMapList(String citycode, String tag, Long relatedid, String itemtype, Long itemid, String period, Timestamp fromvalidtime, boolean isGtZero){
		DetachedCriteria query = queryCommonTicketGoods(citycode, tag, relatedid, itemtype, itemid, false, isGtZero);
		if(StringUtils.isNotBlank(period)){
			query.add(Restrictions.eq("period", period));
		}
		if(fromvalidtime != null){
			if(StringUtils.equals(period, Status.Y)){
				query.add(Restrictions.ge("fromvalidtime", fromvalidtime));
			}else{
				query.add(Restrictions.ge("tovalidtime", fromvalidtime));
			}
		}
	
		Projection pro = Projections.sqlGroupProjection(
				"to_char({alias}.fromvalidtime, 'yyyy-MM') as playdate", 
				"to_char({alias}.fromvalidtime, 'yyyy-MM')", new String[]{"playdate"}, 
				new Type[]{Hibernate.STRING});
		
		Projection pro2 = Projections.sqlGroupProjection(
				"to_char({alias}.tovalidtime, 'yyyy-MM') as playdate", 
				"to_char({alias}.tovalidtime, 'yyyy-MM')", new String[]{"playdate"}, 
				new Type[]{Hibernate.STRING});
		ProjectionList projectList = Projections.projectionList().add(Projections.rowCount(), "count");
		if(StringUtils.equals(period, Status.Y)){
			projectList.add(Projections.alias(pro, "playdate"));
		}else{
			projectList.add(Projections.alias(pro2, "playdate"));
		}
		query.setProjection(projectList);
		query.setResultTransformer(DetachedCriteria.ALIAS_TO_ENTITY_MAP);
		List<Map<String, String>> playdateList = hibernateTemplate.findByCriteria(query);
		return playdateList;
	}
	@Override
	public List<TicketGoods> getTicketGoodsList(String citycode, String tag, Long relatedid, String itemtype, Long itemid, boolean isTovaltime, boolean isGtZero, int from, int maxnum){
		DetachedCriteria query = queryCommonTicketGoods(citycode, tag, relatedid, itemtype, itemid, isTovaltime, isGtZero);
		query.addOrder(Order.desc("goodssort"));
		query.addOrder(Order.asc("fromvalidtime"));
		List<TicketGoods> ticketGoodsList = hibernateTemplate.findByCriteria(query, from, maxnum);
		return ticketGoodsList;
	}
	
	@Override
	public Integer getTicketGoodsCount(String citycode, String tag, Long relatedid, String itemtype, Long itemid, String period, Timestamp fromtime, Timestamp totime, boolean isTovaltime, boolean isGtZero){
		DetachedCriteria query = queryCommonTicketGoods(citycode, tag, relatedid, itemtype, itemid, isTovaltime, isGtZero);
		if(StringUtils.isNotBlank(period)){
			query.add(Restrictions.eq("period", period));
		}
		if(fromtime != null){
			if(StringUtils.equals(period, Status.Y)){
				query.add(Restrictions.ge("fromvalidtime", fromtime));
			}else{
				query.add(Restrictions.ge("tovalidtime", fromtime));
			}
		}
		if(totime != null){
			if(StringUtils.equals(period, Status.Y)){
				query.add(Restrictions.le("fromvalidtime", totime));
			}else{
				query.add(Restrictions.le("tovalidtime", totime));
			}
		}
		query.setProjection(Projections.rowCount());
		List result = hibernateTemplate.findByCriteria(query, 0, 1);
		if(result.isEmpty()) return 0;
		return Integer.parseInt(String.valueOf(result.get(0)));
	}
	@Override
	public Integer getTicketGoodsCount(String citycode, String tag, Long relatedid, String itemtype, Long itemid, boolean isTovaltime, boolean isGtZero){
		return getTicketGoodsCount(citycode, tag, relatedid, itemtype, itemid, null, null, null, isTovaltime, isGtZero);
	}
	
	private DetachedCriteria queryCommonTicketGoods(String citycode, String tag, Long relatedid, String itemtype, Long itemid, boolean isTovaltime, boolean isGtZero){
		DetachedCriteria query = DetachedCriteria.forClass(TicketGoods.class);
		if(isTovaltime){
			Timestamp cur = DateUtil.getCurFullTimestamp();
			query.add(Restrictions.le("fromtime",cur));
			query.add(Restrictions.ge("totime", cur));
			Conjunction con1 = Restrictions.conjunction();
			Conjunction con2 = Restrictions.conjunction();
			con1.add(Restrictions.ge("fromvalidtime", cur));
			con1.add(Restrictions.eq("period", Status.Y));
			con2.add(Restrictions.ge("tovalidtime", cur));
			con2.add(Restrictions.eq("period", Status.N));
			query.add(Restrictions.or(con1, con2));
			query.add(Restrictions.ne("status", Status.DEL));
		}
		if(StringUtils.isNotBlank(citycode)) query.add(Restrictions.eq("citycode", citycode));
		if(StringUtils.isNotBlank(tag)) query.add(Restrictions.eq("tag", tag));
		if(relatedid != null) query.add(Restrictions.eq("relatedid", relatedid));
		if(StringUtils.isNotBlank(itemtype)) query.add(Restrictions.eq("itemtype", itemtype));
		if(itemid != null) query.add(Restrictions.eq("itemid", itemid));
		if(isGtZero) query.add(Restrictions.gt("goodssort", 0));
		return query;
	}
	
	@Override
	public List<GoodsPrice> getGoodsPriceList(Long goodsid){
		String qry = "from GoodsPrice where goodsid= ? and status <> 'D'";
		List<GoodsPrice> priceList = hibernateTemplate.find(qry, goodsid);
		return priceList;
	}
	
	@Override
	public List<GoodsDisQuantity> getGoodsDisList(Long goodspriceId){
		List<GoodsDisQuantity> disQuantityList = baseDao.getObjectListByField(GoodsDisQuantity.class, "gspid", goodspriceId);
		return disQuantityList;
	}
	
	@Override
	public List<GoodsDisQuantity> getGoodsDisListByGoodsid(Long goodsid){
		String qry = "from GoodsDisQuantity d where exists(select t.id from GoodsPrice t where t.id=d.gspid and (t.status <> 'D' or t.status is null) and t.goodsid=?)";
		List<GoodsDisQuantity> disList = hibernateTemplate.find(qry, goodsid);
		return disList;
	}
	
	@Override
	public Map<Long/*goodsPriceid*/, List<GoodsDisQuantity>> getGoodsDisMapByPriceId(List<Long> priceIdList){
		Map<Long, List<GoodsDisQuantity>> discountMap = new HashMap<Long, List<GoodsDisQuantity>>();
		for (Long priceId : priceIdList) {
			List<GoodsDisQuantity> disQuantityList = baseDao.getObjectListByField(GoodsDisQuantity.class, "gspid", priceId);
			discountMap.put(priceId, disQuantityList);
		}
		return discountMap;
	}
	@Override
	public Map<Long/*goodsPriceid*/, List<GoodsDisQuantity>> getGoodsDisMap(List<GoodsPrice> goodsPriceList){
		List<Long> idList = BeanUtil.getBeanPropertyList(goodsPriceList, "id", true);
		return getGoodsDisMapByPriceId(idList);
	}
	
	@Override
	public ErrorCode saveTicketGoods(TicketGoods goods, String playdates, String rooms){
		if(goods==null) return ErrorCode.getFailure("该场次不存在");
		List<String> dateList = Arrays.asList(StringUtils.split(playdates, ","));
		List<Long> roomidList = BeanUtil.getIdList(rooms, ",");
		List<TheatreRoom> roomList = baseDao.getObjectList(TheatreRoom.class, roomidList);
		try {
			for(String date : dateList){
				Timestamp newplaytime = DateUtil.parseTimestamp(date + " " + DateUtil.format(goods.getFromvalidtime(), "HH:mm:ss"));
				for(TheatreRoom theatreRoom : roomList){
					TicketGoods newGoods = new TicketGoods();
					PropertyUtils.copyProperties(newGoods, goods);
					newGoods.setId(null);
					newGoods.setGoodsname(date + " " + DateUtil.getCnWeek(newplaytime) + " " + DateUtil.format(newplaytime, "HH:mm"));
					newGoods.setFromvalidtime(newplaytime);
					newGoods.setTovalidtime(DateUtil.addHour(newplaytime, 2));
					newGoods.setRoomid(theatreRoom.getId());
					newGoods.setRoomname(theatreRoom.getRoomname());
					baseDao.saveObject(newGoods);
					
					List<GoodsPrice> tspList2 = getGoodsPriceList(goods.getId());
					GoodsPriceHelper tspHelper = new GoodsPriceHelper(tspList2);
					List<GoodsPrice> tspList = tspHelper.getGoodsPriceList();
					for(GoodsPrice tsp : tspList){
						GoodsPrice sp = new GoodsPrice();
						PropertyUtils.copyProperties(sp, tsp);
						sp.setId(null);
						sp.setGoodsid(newGoods.getId());
						baseDao.saveObject(sp);
					}
				}
			}
		}  catch (Exception e) {
			dbLogger.error("", e);
			return ErrorCode.getFailure(StringUtil.getExceptionTrace(e, 5));
		}
		
		return ErrorCode.SUCCESS;
	}
	
	@Override
	public <T extends BaseGoods> List<Integer> getGoodsPriceList(Class<T> clazz, String tag, Long relatedid, String itemtype, Long itemid, Timestamp starttime, Timestamp endtime){
		DetachedCriteria query = DetachedCriteria.forClass(GoodsPrice.class, "t");
		query.setProjection(Projections.distinct(Projections.property("t.price")));
		DetachedCriteria sub = DetachedCriteria.forClass(clazz, "i");
		if(StringUtils.isNotBlank(tag)){
			sub.add(Restrictions.eq("i.tag", tag));
		}
		if(relatedid != null){
			sub.add(Restrictions.eq("i.relatedid", relatedid));
		}
		if(StringUtils.isNotBlank(itemtype)){
			sub.add(Restrictions.eq("i.itemtype", itemtype));
		}
		if(itemid != null){
			sub.add(Restrictions.eq("i.itemid", itemid));
		}
		Conjunction con1 = Restrictions.conjunction();
		Conjunction con2 = Restrictions.conjunction();
		Timestamp cur = DateUtil.getCurFullTimestamp();
		if(starttime == null) starttime = cur;
		con1.add(Restrictions.ge("i.fromvalidtime", starttime));
		con1.add(Restrictions.eq("i.period", Status.Y));
		con2.add(Restrictions.ge("i.tovalidtime", starttime));
		con2.add(Restrictions.eq("i.period", Status.N));
		if(endtime != null){
			con1.add(Restrictions.le("i.fromvalidtime", endtime));
			con2.add(Restrictions.le("i.tovalidtime", endtime));
		}
		sub.add(Restrictions.or(con1, con2));
		sub.add(Restrictions.eq("i.status", Status.Y));
		sub.add(Restrictions.eqProperty("i.id", "t.goodsid"));
		sub.setProjection(Projections.property("i.id"));
		query.add(Subqueries.exists(sub));
		List<Integer> priceList = hibernateTemplate.findByCriteria(query);
		Collections.sort(priceList);
		return priceList;
	}
}
