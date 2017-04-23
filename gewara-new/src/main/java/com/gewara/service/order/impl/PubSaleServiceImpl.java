package com.gewara.service.order.impl;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
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
import com.gewara.constant.PointConstant;
import com.gewara.constant.Status;
import com.gewara.constant.ticket.OrderConstant;
import com.gewara.model.common.VersionCtl;
import com.gewara.model.pay.GewaOrder;
import com.gewara.model.pay.GoodsOrder;
import com.gewara.model.pay.PubMember;
import com.gewara.model.pay.PubSale;
import com.gewara.model.pay.PubSaleOrder;
import com.gewara.model.user.Member;
import com.gewara.model.user.MemberInfo;
import com.gewara.pay.PayUtil;
import com.gewara.service.order.GoodsOrderService;
import com.gewara.service.order.PubSaleService;
import com.gewara.support.ErrorCode;
import com.gewara.support.ReadOnlyTemplate;
import com.gewara.util.BeanUtil;
import com.gewara.util.DateUtil;
import com.gewara.util.JsonUtils;
@Service("pubSaleService")
public class PubSaleServiceImpl extends GewaOrderServiceImpl implements PubSaleService{
	@Autowired@Qualifier("readOnlyTemplate")
	private ReadOnlyTemplate readOnlyTemplate;
	public void setReadOnlyHibernateTemplate(ReadOnlyTemplate readOnlyTemplate) {
		this.readOnlyTemplate = readOnlyTemplate;
	}	
	
	@Autowired@Qualifier("goodsOrderService")
	private GoodsOrderService goodsOrderService;
	
	@Override
	public ErrorCode<Map> joinPubSale(Long sid, Member member, Integer pice, Timestamp addtime) {
		PubSale sale = baseDao.getObject(PubSale.class, sid);
		Map map = new HashMap();
		if(Status.Y.equals(sale.getStatus())) return ErrorCode.getSuccessReturn(map);
		if(StringUtils.equals(member.getId()+"", sale.getMemberid()+"")) {
			return ErrorCode.getFailure(ApiConstant.CODE_SIGN_ERROR,  "休息一下，请不要重复竞拍！");
		}
		final boolean pubnum = sale.getPubperiod() != null && sale.getPubperiod()> 0 && sale.getPubnumber() != null && sale.getPubnumber()>0;
		String opkey = "pubsale_" + sale.getId() + "_"+ member.getId();
		if(pubnum){
			boolean allow = operationService.isAllowOperation(opkey, sale.getPubperiod()*60, sale.getPubnumber());
			if(!allow){
				return ErrorCode.getFailure(ApiConstant.CODE_SIGN_ERROR, "本次竞拍，每个用户只能参与" + sale.getPubnumber() + "次！");
			}
		}
		if(sale.getGoodsid() != null){
			List<GoodsOrder> orderList = goodsOrderService.getGoodsOrderList(sale.getGoodsid(), member.getId(), OrderConstant.STATUS_PAID_SUCCESS, false, true, 1);
			if(orderList.isEmpty()){
				return ErrorCode.getFailure(ApiConstant.CODE_SIGN_ERROR, "当前没有竞拍资格！");
			}
		}
		MemberInfo info = baseDao.getObject(MemberInfo.class, member.getId());
		if(info.getPointvalue()<sale.getNeedpoint()) return ErrorCode.getFailure(ApiConstant.CODE_SIGN_ERROR, "你的积分不够！");
		//积分
		if(sale.getNeedpoint() != 0){
			Integer point = -sale.getNeedpoint();
			pointService.addPointInfo(member.getId(), point, "竞拍", PointConstant.TAG_PUBSALE, sale.getId(), null, addtime, null, null);
		}
		//竞拍
		List<Integer>  tmpPriceList = BeanUtil.getIntgerList(sale.getDupprice(), ",");
		if(!tmpPriceList.isEmpty()){
			if(!tmpPriceList.contains(pice)){
				return ErrorCode.getFailure(ApiConstant.CODE_SIGN_ERROR, "出价错误！");
			}
			sale.addCurprice(pice);
		}
		sale.setMemberid(member.getId());
		sale.setNickname(member.getNickname());
		sale.setLasttime(DateUtil.addSecond(addtime, sale.getCountdown()));
		
		//参与者
		PubMember pm = new PubMember(sale.getId(), member.getId(), sale.getCurprice());
		pm.setPointvalue(info.getPointvalue());
		pm.setAddtime(addtime);
		if(pubnum){
			operationService.updateOperation(opkey, sale.getPubperiod()*60, sale.getPubnumber());
		}
		baseDao.saveObjectList(sale, pm);
		
		map.put("point", info.getPointvalue());
		map.put("nickname", sale.getNickname());
		map.put("curprice", sale.gainRprice(sale.getCurprice()));
		map.put("memberPic", info.getHeadpicUrl());
		map.put("memberid", sale.getMemberid());
		map.put("success", true);
		map.put("pubSale", sale);
		
		return ErrorCode.getSuccessReturn(map);
	}
	@Override
	public Long stopPubSale(PubSale sale) {
		PubSaleOrder order = baseDao.getObjectByUkey(PubSaleOrder.class, "pubid", sale.getId(), false);
		if(order!=null) return order.getId();
		VersionCtl vc = baseDao.getObject(VersionCtl.class, "pub" + sale.getId());
		if("unsale".equals(vc.getCtldata())){
			vc.setCtldata("sale");
			baseDao.saveObject(vc);
			if(sale.getMemberid()!=null){ //生成订单
				try {
					Member member = baseDao.getObject(Member.class, sale.getMemberid());
					order = new PubSaleOrder(sale);
					order.setMobile(member.getMobile());
					order.setTradeNo(PayUtil.getPubSaleTradeNo());
					order.setCostprice(0);
					order.setTotalcost(0);
					order.setCitycode(sale.getCitycode());
					order.setDescription2(JsonUtils.addJsonKeyValue(order.getDescription2(), "竞拍", sale.getName()));
					baseDao.saveObject(order);
					return order.getId();
				} catch (Exception e) {
					dbLogger.error("生产竞拍订单错误：" + e.getMessage(), e);
					return null;
				}
			}
		}
		return null;
	}
	@Override
	public void cancelPubSaleOrder(String tradeNo, Long memberid, String reason) {
		PubSaleOrder order = baseDao.getObjectByUkey(PubSaleOrder.class, "tradeNo", tradeNo, false);
		cancelPubSaleOrder(order, memberid, reason);
	}
	@Override
	public void cancelPubSaleOrder(PubSaleOrder order, Long memberid, String reason) {
		if(order.isNew() && order.getMemberid().equals(memberid)){
			Timestamp validtime = new Timestamp(System.currentTimeMillis()-1000);
			order.setStatus(OrderConstant.STATUS_REPEAT);
			order.setValidtime(validtime);
			baseDao.saveObject(order);
			dbLogger.warn("取消未支付订单：" + order.getTradeNo() + "," + reason);
		}
	}
	@Override
	public void processPubSaleOrder(PubSaleOrder order){
		if(order.isAllPaid()){
			Timestamp cur = new Timestamp(System.currentTimeMillis());
			order.setUpdatetime(cur);
			order.setModifytime(cur);
			order.setValidtime(DateUtil.addDay(cur, 180));
			order.setStatus(OrderConstant.STATUS_PAID_SUCCESS);
			baseDao.saveObject(order);
			processOrderExtra(order);
		}
	}
	@Override
	public Integer getPubSaleCount(String name, String status){
		DetachedCriteria query = DetachedCriteria.forClass(PubSale.class);
		query.setProjection(Projections.rowCount());
		if(StringUtils.isNotBlank(name)) query.add(Restrictions.like("name", name, MatchMode.ANYWHERE));
		if(!StringUtils.equals(status, Status.N_DELETE)){
			if(StringUtils.isNotBlank(status))query.add(Restrictions.eq("status", status));
			else query.add(Restrictions.ne("status", Status.N_DELETE));
		}else query.add(Restrictions.eq("status", Status.N_DELETE));
		query.addOrder(Order.desc("id"));
		List<Long> result= readOnlyTemplate.findByCriteria(query);
		if(!result.isEmpty()) return Integer.valueOf(result.get(0)+"");
		return 0;
	}
	
	@Override
	public List<PubSale> getPubSaleList(String name, String status, int from, int maxnum) {
		DetachedCriteria query = DetachedCriteria.forClass(PubSale.class);
		if(StringUtils.isNotBlank(name)) query.add(Restrictions.like("name", name, MatchMode.ANYWHERE));
		if(!StringUtils.equals(status, Status.N_DELETE)){
			if(StringUtils.isNotBlank(status))query.add(Restrictions.eq("status", status));
			else query.add(Restrictions.ne("status", Status.N_DELETE));
		}else query.add(Restrictions.eq("status", Status.N_DELETE));
		query.addOrder(Order.desc("id"));
		return readOnlyTemplate.findByCriteria(query, from, maxnum);
	}
	
	@Override
	public List<PubMember> getPubMemberList(Long sid, Long memberid, int from, int maxnum){
		return getPubMemberList(sid, memberid, true, from, maxnum);
	}
	
	@Override
	public List<PubMember> getPubMemberList(Long sid, Long memberid){
		return getPubMemberList(sid, memberid, false, 0, 0);
	}
	
	private List<PubMember> getPubMemberList(Long sid, Long memberid, boolean isPage, int from, int maxnum){
		DetachedCriteria query = DetachedCriteria.forClass(PubMember.class);
		query.add(Restrictions.eq("pubid", sid));
		if(memberid != null) query.add(Restrictions.eq("memberid", memberid));
		List<PubMember> pubMemberList = new ArrayList<PubMember>();
		query.addOrder(Order.desc("addtime"));
		if(isPage){
			pubMemberList = hibernateTemplate.findByCriteria(query, from, maxnum);
		}else{
			pubMemberList = hibernateTemplate.findByCriteria(query);
		}
		return pubMemberList;
	}
	@Override
	public Integer getPubMemberCount(Long sid, Timestamp addtime) {
		String qry = "select count(*) from PubMember m where m.pubid=? and m.addtime>=? order by m.addtime desc";
		List list = hibernateTemplate.find(qry, sid, addtime);
		return Integer.parseInt(list.get(0)+"");
	}
	
	@Override
	public Integer getMemberPubSaleOrderCountByMemberid(Long memberid, Long relatedid, Timestamp fromtime, Timestamp totime, String citycode){
		DetachedCriteria query = DetachedCriteria.forClass(PubSaleOrder.class);
		query.add(Restrictions.eq("memberid", memberid));
		if(relatedid != null) query.add(Restrictions.eq("pubid", relatedid));
		query.add(Restrictions.eq("pricategory", OrderConstant.ORDER_PRICATEGORY_PUBSALE));
		if(StringUtils.isNotBlank(citycode)) query.add(Restrictions.eq("citycode", citycode));
		query.add(Restrictions.like("status", OrderConstant.STATUS_PAID,MatchMode.START));
		query.add(Restrictions.ge("addtime", fromtime));
		query.add(Restrictions.le("addtime", totime));
		query.setProjection(Projections.rowCount());
		List<GewaOrder> list = hibernateTemplate.findByCriteria(query);
		if (list.isEmpty()) return 0;
		return Integer.parseInt(""+list.get(0));
	}
}
