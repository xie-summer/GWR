package com.gewara.service.sport.impl;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.hibernate.criterion.Conjunction;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Disjunction;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.criterion.Subqueries;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.gewara.constant.ApiConstant;
import com.gewara.constant.OpenTimeTableConstant;
import com.gewara.constant.SmsConstant;
import com.gewara.model.pay.SMSRecord;
import com.gewara.model.pay.SportOrder;
import com.gewara.model.sport.OpenTimeSale;
import com.gewara.model.sport.OpenTimeSaleMember;
import com.gewara.model.sport.SellDeposit;
import com.gewara.model.sport.Sport;
import com.gewara.model.user.Member;
import com.gewara.service.OrderException;
import com.gewara.service.impl.BaseServiceImpl;
import com.gewara.service.sport.OpenTimeSaleService;
import com.gewara.service.sport.SportOrderService;
import com.gewara.support.ErrorCode;
import com.gewara.util.ClassUtils;
import com.gewara.util.DateUtil;

@Service("openTimeSaleService")
public class OpenTimeSaleServiceImpl extends BaseServiceImpl implements OpenTimeSaleService {

	@Autowired
	private SportOrderService sportOrderService;
	
	@Override
	public List<OpenTimeSale> getJoinOtsList(String citycode, Long memberid, boolean valid, String orderField, boolean asc, int from, int maxnum){
		DetachedCriteria query = queryCriteria(citycode, memberid, valid);
		if(StringUtils.isNotBlank(orderField) && ClassUtils.hasMethod(OpenTimeSale.class, "get" + StringUtils.capitalize(orderField))){
			if(asc){
				query.addOrder(Order.asc(orderField));
			}else{
				query.addOrder(Order.desc(orderField));
			}
		}else{
			if(asc){
				query.addOrder(Order.asc("s.addtime"));
			}else{
				query.addOrder(Order.desc("s.addtime"));
			}
		}
		return hibernateTemplate.findByCriteria(query, from, maxnum);
	}
	
	@Override
	public List<OpenTimeSale> getNojoinOtsByPaidDeposit(String citycode, Long memberid, boolean valid){
		DetachedCriteria query = DetachedCriteria.forClass(OpenTimeSale.class, "s");
		query.add(Restrictions.eq("s.citycode", citycode));
		Timestamp cur = DateUtil.getCurFullTimestamp();
		if(valid){
			query.add(Restrictions.le("s.opentime", cur));
			query.add(Restrictions.ge("s.closetime", cur));
			query.add(Restrictions.or(Restrictions.eq("s.lockStatus", OpenTimeTableConstant.SALE_STATUS_LOCK), Restrictions.eq("s.lockStatus", OpenTimeTableConstant.SALE_STATUS_UNLOCK)));
		}else{
			query.add(Restrictions.or(Restrictions.lt("s.closetime", cur), Restrictions.like("s.lockStatus", OpenTimeTableConstant.SALE_STATUS_SUCCESS, MatchMode.START)));
		}
		DetachedCriteria subQuery = DetachedCriteria.forClass(OpenTimeSaleMember.class, "m");
		subQuery.add(Restrictions.eq("m.memberid", memberid));
		subQuery.add(Restrictions.eqProperty("m.otsid", "s.id"));
		subQuery.setProjection(Projections.property("m.otsid"));
		query.add(Subqueries.notExists(subQuery));
		
		DetachedCriteria subQueryDeposit = DetachedCriteria.forClass(SellDeposit.class, "d");
		subQueryDeposit.add(Restrictions.eq("d.memberid", memberid));
		subQueryDeposit.add(Restrictions.eqProperty("d.otsid", "s.id"));
		subQueryDeposit.setProjection(Projections.property("d.otsid"));
		subQueryDeposit.add(Restrictions.eq("d.status", SellDeposit.STATUS_PAID_SUCCESS));
		query.add(Subqueries.exists(subQueryDeposit));
		List<OpenTimeSale> ostList = hibernateTemplate.findByCriteria(query);
		return ostList;
	}
	
	@Override
	public Integer getJoinOtsCount(String citycode, Long memberid, boolean valid){
		DetachedCriteria query = queryCriteria(citycode, memberid, valid);
		query.setProjection(Projections.rowCount());
		List<Long> result = hibernateTemplate.findByCriteria(query, 0, 1);
		if(result.isEmpty()) return 0;
		return result.get(0).intValue();
	}
	
	private DetachedCriteria queryCriteria(String citycode, Long memberid, boolean valid){
		DetachedCriteria query = DetachedCriteria.forClass(OpenTimeSale.class, "s");
		query.add(Restrictions.eq("s.citycode", citycode));
		Timestamp cur = DateUtil.getCurFullTimestamp();
		Conjunction con1 = Restrictions.conjunction();
		Conjunction con2 = Restrictions.conjunction();
		if(valid){
			con1.add(Restrictions.le("s.opentime", cur));
			con1.add(Restrictions.ge("s.closetime", cur));
			con1.add(Restrictions.or(Restrictions.eq("s.lockStatus", OpenTimeTableConstant.SALE_STATUS_LOCK),Restrictions.eq("s.lockStatus", OpenTimeTableConstant.SALE_STATUS_UNLOCK)));
			con1.add(Restrictions.le("s.paidvalidtime", cur));
			Conjunction con3 = Restrictions.conjunction();
			con3.add(Restrictions.eq("s.memberid", memberid));
			con3.add(Restrictions.eq("s.lockStatus", OpenTimeTableConstant.SALE_STATUS_SUCCESS));
			con3.add(Restrictions.ge("s.paidvalidtime", cur));
			con2.add(Restrictions.ge("s.validtime", cur));
			con2.add(Restrictions.eq("s.lockStatus", OpenTimeTableConstant.SALE_STATUS_SUCCESS_PAID));
			con2.add(Restrictions.eq("s.memberid", memberid));
			Disjunction dis = Restrictions.disjunction();
			dis.add(con2);
			dis.add(con3);
			query.add(Restrictions.or(con1, dis));
		}else{
			con1.add(Restrictions.ne("s.memberid", memberid));
			Disjunction dis1 = Restrictions.disjunction();
			dis1.add(Restrictions.lt("s.closetime", cur));
			con1.add(Restrictions.or(Restrictions.lt("s.closetime", cur), Restrictions.like("s.lockStatus", OpenTimeTableConstant.SALE_STATUS_SUCCESS, MatchMode.START)));
			Conjunction con3 = Restrictions.conjunction();
			con3.add(Restrictions.eq("s.memberid", memberid));
			con3.add(Restrictions.eq("s.lockStatus", OpenTimeTableConstant.SALE_STATUS_SUCCESS));
			con3.add(Restrictions.lt("s.paidvalidtime", cur));
			con2.add(Restrictions.eq("s.memberid", memberid));
			con2.add(Restrictions.eq("s.lockStatus", OpenTimeTableConstant.SALE_STATUS_SUCCESS_PAID));
			con2.add(Restrictions.lt("s.validtime", cur));
			Disjunction dis = Restrictions.disjunction();
			dis.add(con2);
			dis.add(con3);
			query.add(Restrictions.or(con1, dis));
		}
		DetachedCriteria subQueryDeposit = DetachedCriteria.forClass(SellDeposit.class, "d");
		subQueryDeposit.add(Restrictions.eq("d.memberid", memberid));
		subQueryDeposit.add(Restrictions.eqProperty("d.otsid", "s.id"));
		subQueryDeposit.setProjection(Projections.property("d.otsid"));
		subQueryDeposit.add(Restrictions.like("d.status", SellDeposit.STATUS_PAID, MatchMode.START));
		query.add(Subqueries.exists(subQueryDeposit));
		/*DetachedCriteria subQuery = DetachedCriteria.forClass(OpenTimeSaleMember.class, "m");
		subQuery.add(Restrictions.eq("m.memberid", memberid));
		subQuery.add(Restrictions.eqProperty("m.otsid", "s.id"));
		subQuery.setProjection(Projections.property("m.otsid"));
		query.add(Subqueries.exists(subQuery));*/
		return query;
	}
	
	private DetachedCriteria queryOtsMember(Long otsid, Long memberid, Timestamp addtime, boolean geAddtime){
		DetachedCriteria query = DetachedCriteria.forClass(OpenTimeSaleMember.class);
		query.add(Restrictions.eq("otsid", otsid));
		if(memberid != null) query.add(Restrictions.eq("memberid", memberid));
		if(addtime != null){
			if(geAddtime){
				query.add(Restrictions.ge("addtime", addtime));
			}else{
				query.add(Restrictions.le("addtime", addtime));
			}
		}
		return query;
	}
	
	@Override
	public List<OpenTimeSaleMember> getOtsMemberList(Long otsid, Long memberid, int from, int maxnum){
		DetachedCriteria query = queryOtsMember(otsid, memberid, null, false);
		query.addOrder(Order.desc("addtime"));
		return hibernateTemplate.findByCriteria(query, from, maxnum);
	}
	
	@Override
	public List<OpenTimeSaleMember> getOtsMemberList(Long otsid, Long memberid){
		DetachedCriteria query = queryOtsMember(otsid, memberid, null, false);
		query.addOrder(Order.desc("addtime"));
		return hibernateTemplate.findByCriteria(query);
	}
	
	@Override
	public Integer getOtsMemberCount(Long otsid, Long memberid){
		DetachedCriteria query = queryOtsMember(otsid, memberid, null, true);
		query.setProjection(Projections.rowCount());
		List<Long> result = hibernateTemplate.findByCriteria(query, 0, 1);
		if(result.isEmpty()) return 0;
		return result.get(0).intValue();
	}
	
	@Override
	public List<Long> getOtsIdListMemberJoin(Long memberid){
		DetachedCriteria query = DetachedCriteria.forClass(OpenTimeSaleMember.class);
		query.add(Restrictions.eq("memberid", memberid));
		query.setProjection(Projections.distinct(Projections.property("otsid")));
		List<Long> idList = hibernateTemplate.findByCriteria(query);
		return idList;
	}
	
	@Override
	public Integer getOtsMemberCount(Long otsid, Timestamp addtime){
		DetachedCriteria query = queryOtsMember(otsid, null, addtime, true);
		query.setProjection(Projections.rowCount());
		List<Long> result = hibernateTemplate.findByCriteria(query, 0, 1);
		if(result.isEmpty()) return 0;
		return result.get(0).intValue();
	}
	
	@Override
	public ErrorCode<Map> joinOpenTimeSale(Long otsid, SellDeposit deposit, Member member, Integer price, Timestamp addtime) throws OrderException {
		if(deposit == null || !member.getId().equals(deposit.getMemberid()) || !StringUtils.equals(deposit.getStatus(), SellDeposit.STATUS_PAID_SUCCESS)){
			return ErrorCode.getFullErrorCode(ApiConstant.CODE_SIGN_ERROR, "当前竞价场次未付保证金！", OpenTimeTableConstant.GUARANTEE_UNPAY);
		}
		if(price == null || price<=0) return ErrorCode.getFailure(ApiConstant.CODE_SIGN_ERROR, "出价错误！");
		OpenTimeSale ots = baseDao.getObject(OpenTimeSale.class, otsid);
		if(!ots.hasLockStatus(OpenTimeTableConstant.SALE_STATUS_LOCK)){
			return ErrorCode.getFailure(ApiConstant.CODE_SIGN_ERROR, "场馆场地锁定未成功不能竞价！");
		}
		if(!ots.hasBooking()) return ErrorCode.getFailure(ApiConstant.CODE_SIGN_ERROR, "竞价场次未开始或以过期！");
		Map jsonMap = new HashMap();
		if(ots.hasSuccess()) return ErrorCode.getSuccessReturn(jsonMap);
		if(StringUtils.equals(member.getId()+"", ots.getMemberid()+"")) {
			return ErrorCode.getFailure(ApiConstant.CODE_SIGN_ERROR,  "休息一下，请不要重复竞拍！");
		}
		if(price < ots.getCurprice() && (price-ots.getLowerprice()) % ots.getDupprice() != 0){
			return ErrorCode.getFailure(ApiConstant.CODE_SIGN_ERROR, "出价错误,必需是5的倍数！");
		}
		int curprice = ots.getCurprice();
		if(price<=curprice) return ErrorCode.getFailure(ApiConstant.CODE_SIGN_ERROR, "出价错误，当前价格已为" + curprice);
		ots.setCurprice(price);
		
		if(ots.getCurprice() >= ots.getAuctionprice()){
			ots.setCurprice(ots.getAuctionprice());
			ots.setLockStatus(OpenTimeTableConstant.SALE_STATUS_SUCCESS);
		}
		//参与者
		OpenTimeSaleMember pm = new OpenTimeSaleMember(ots.getId(), member.getId(), member.getNickname(), ots.getCurprice());
		pm.setDupprice(ots.getCurprice()-curprice);
		pm.setAddtime(addtime);
		ots.setMemberid(member.getId());
		ots.setNickname(member.getNickname());
		ots.setMobile(deposit.getMobile());
		ots.setJointime(addtime);
		int joinnum = ots.getJoinnum() + 1;
		ots.setJoinnum(joinnum);
		baseDao.saveObjectList(ots, pm);
		
		jsonMap.put("nickname", ots.getNickname());
		jsonMap.put("curprice", ots.getCurprice());
		jsonMap.put("memberid", ots.getMemberid());
		jsonMap.put("success", true);
		jsonMap.put("ots", ots);
		ErrorCode<SportOrder> code = sportOrderService.addSportOrder(ots);
		if(code.isSuccess()){
			SportOrder order = code.getRetval();
			/*SMSRecord sms = */sendMessage(order, ots);
			//FIXME:事务外发消息
			/*if(sms!=null){
				untransService.sendMsgAtServer(sms, false);
			}*/
		}
		return ErrorCode.getSuccessReturn(jsonMap);
	}

	@Override
	public OpenTimeSaleMember getLastOtsMember(Long otsid) {
		DetachedCriteria query = DetachedCriteria.forClass(OpenTimeSaleMember.class , "o");
		query.add(Restrictions.eq("o.otsid", otsid));
		query.addOrder(Order.desc("o.addtime"));
		List<OpenTimeSaleMember> saleMemberList = hibernateTemplate.findByCriteria(query, 0, 1);
		if(saleMemberList.isEmpty()) return null;
		return saleMemberList.get(0);
	}
	
	@Override
	public SMSRecord sendMessage(SportOrder order, OpenTimeSale ots){
		if(StringUtils.isBlank(ots.getMessage())){
			Sport sport = baseDao.getObject(Sport.class, ots.getSportid());
			String msg = "你竞价" + sport.getRealBriefname() + DateUtil.formatDate(ots.getPlaydate()) + 
				"(" + DateUtil.getCnWeek(ots.getPlaydate()) + ") " + ots.getStarttime() + "-" + ots.getEndtime() + "已成功，请在" + DateUtil.formatTimestamp(order.getValidtime()) + "前支付！";
			SMSRecord sms = new SMSRecord(ots.getId(), order.getTradeNo() + "_" + ots.getId(), ots.getMobile(), msg,
					 order.getAddtime(), order.getValidtime(), SmsConstant.SMSTYPE_NOW);
			baseDao.saveObject(sms);
			if(sms != null){
				ots.setMessage(msg);
				baseDao.saveObject(ots);
			}
			return sms;
		}
		return null;
	}
	
	@Override
	public List<OpenTimeSale> getOpenOtsList(String citycode, String orderField, boolean asc, int from, int maxnum){
		DetachedCriteria query = DetachedCriteria.forClass(OpenTimeSale.class, "s");
		query.add(Restrictions.eq("s.citycode", citycode));
		Timestamp cur = DateUtil.getCurFullTimestamp();
		query.add(Restrictions.le("s.opentime", cur));
		query.add(Restrictions.ge("s.closetime", cur));
		query.add(Restrictions.or(Restrictions.eq("s.lockStatus", OpenTimeTableConstant.SALE_STATUS_UNLOCK), 
				Restrictions.eq("s.lockStatus", OpenTimeTableConstant.SALE_STATUS_LOCK)));
		query.add(Restrictions.ge("s.validtime", cur));
		if(StringUtils.isNotBlank(orderField) && ClassUtils.hasMethod(OpenTimeSale.class, "get" + StringUtils.capitalize(orderField))){
			if(asc){
				query.addOrder(Order.asc(orderField));
			}else{
				query.addOrder(Order.desc(orderField));
			}
		}else{
			if(asc){
				query.addOrder(Order.asc("s.addtime"));
			}else{
				query.addOrder(Order.desc("s.addtime"));
			}
		}
		return hibernateTemplate.findByCriteria(query, from, maxnum);
	}
}
