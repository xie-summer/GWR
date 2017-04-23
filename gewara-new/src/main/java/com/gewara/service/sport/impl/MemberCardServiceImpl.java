package com.gewara.service.sport.impl;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.criterion.Subqueries;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.gewara.command.SearchOrderCommand;
import com.gewara.constant.ApiConstant;
import com.gewara.constant.MemberCardConstant;
import com.gewara.constant.OpenTimeTableConstant;
import com.gewara.constant.PaymethodConstant;
import com.gewara.constant.Status;
import com.gewara.constant.sys.CacheConstant;
import com.gewara.constant.sys.ConfigConstant;
import com.gewara.constant.ticket.OrderConstant;
import com.gewara.constant.ticket.PartnerConstant;
import com.gewara.helper.SportSynchHelper;
import com.gewara.helper.order.OrderContainer;
import com.gewara.model.api.ApiUser;
import com.gewara.model.common.GewaConfig;
import com.gewara.model.common.LastOperation;
import com.gewara.model.pay.GewaOrder;
import com.gewara.model.pay.MemberCardOrder;
import com.gewara.model.pay.SportOrder;
import com.gewara.model.sport.MemberCardInfo;
import com.gewara.model.sport.MemberCardType;
import com.gewara.model.sport.MemberCardTypePlace;
import com.gewara.model.sport.OpenTimeItem;
import com.gewara.model.sport.OpenTimeTable;
import com.gewara.model.sport.Sport;
import com.gewara.model.sport.SportItem;
import com.gewara.model.user.Member;
import com.gewara.pay.PayUtil;
import com.gewara.service.OrderException;
import com.gewara.service.order.impl.GewaOrderServiceImpl;
import com.gewara.service.sport.MemberCardService;
import com.gewara.service.sport.SportOrderService;
import com.gewara.support.ErrorCode;
import com.gewara.untrans.CacheService;
import com.gewara.untrans.monitor.OrderMonitorService;
import com.gewara.untrans.sport.RemoteMemberCardService;
import com.gewara.util.BeanUtil;
import com.gewara.util.DateUtil;
import com.gewara.util.JsonUtils;
import com.gewara.util.StringUtil;
import com.gewara.util.ValidateUtil;
import com.gewara.util.VmUtils;
import com.gewara.xmlbind.sport.RemoteMemberCardInfo;
import com.gewara.xmlbind.sport.RemoteMemberCardOrder;
import com.gewara.xmlbind.sport.RemoteMemberCardType;

@Service("memberCardService")
public class MemberCardServiceImpl extends GewaOrderServiceImpl implements MemberCardService {
	@Autowired@Qualifier("remoteMemberCardService")
	private RemoteMemberCardService remoteMemberCardService;
	@Autowired@Qualifier("orderMonitorService")
	private OrderMonitorService orderMonitorService;
	@Autowired@Qualifier("cacheService")
	private CacheService cacheService;
	@Autowired@Qualifier("sportOrderService")
	private SportOrderService sportOrderService;
	@Override
	public List<MemberCardType> getMemberCardTypeListBySportids(Long sportid, boolean overNum) {
		DetachedCriteria query = queryMemberCardTypeList(sportid, overNum);
		query.addOrder(Order.desc("t.addtime"));
		List<MemberCardType> mctList = hibernateTemplate.findByCriteria(query);
		return mctList;
	}
	@Override
	public int getMemberCardTypeCountBySportids(Long sportid, boolean overNum){
		DetachedCriteria query = queryMemberCardTypeList(sportid, overNum);
		query.setProjection(Projections.rowCount());
		List result = hibernateTemplate.findByCriteria(query);
		if(result.isEmpty()) return 0;
		return Integer.parseInt(result.get(0)+"");
	}
	@Override
	public int getMemberCardTypeCountBySportItemid(Long itemid){
		String key = CacheConstant.buildKey("getMemberCardTypeCountBySportItemid11111", itemid);
		Integer count = (Integer)cacheService.get(CacheConstant.REGION_HALFDAY, key);
		if(count == null){
			DetachedCriteria query = DetachedCriteria.forClass(MemberCardType.class, "t");
			query.add(Restrictions.like("fitItem", itemid+"", MatchMode.ANYWHERE));
			query.setProjection(Projections.rowCount());
			List result = hibernateTemplate.findByCriteria(query);
			if(result.isEmpty()){
				return 0;
			}
			count = Integer.parseInt(""+result.get(0));
			cacheService.set(CacheConstant.REGION_HALFDAY, key, count);
		}
		return count;
	}
	
	private DetachedCriteria queryMemberCardTypeList(Long sportid, boolean overNum){
		DetachedCriteria query = DetachedCriteria.forClass(MemberCardType.class, "t");
		if(overNum) query.add(Restrictions.ge("t.overNum", MemberCardConstant.MIN_OVERNUM));
		
		DetachedCriteria sub = DetachedCriteria.forClass(MemberCardTypePlace.class, "p");
		sub.add(Restrictions.eq("p.placeid", sportid));
		sub.add(Restrictions.eqProperty("p.mctid", "t.id"));
		sub.setProjection(Projections.property("p.id"));
		
		query.add(Subqueries.exists(sub));
		return query;
	}
	@Override
	public List<MemberCardType> getBookingMemberCardTypeListBySportids(Long sportid) {
		DetachedCriteria query = queryMemberCardTypeList(sportid, true);
		Timestamp curtime = new Timestamp(System.currentTimeMillis());
		query.add(Restrictions.eq("t.status", OpenTimeTableConstant.STATUS_BOOK));
		query.add(Restrictions.le("t.opentime", curtime));
		query.add(Restrictions.gt("t.closetime", curtime));
		query.addOrder(Order.desc("t.addtime"));
		return hibernateTemplate.findByCriteria(query);
	}
	@Override
	public int getBookingMemberCardTypeCountBySportids(Long sportid){
		DetachedCriteria query = queryMemberCardTypeList(sportid, true);
		Timestamp curtime = new Timestamp(System.currentTimeMillis());
		query.add(Restrictions.eq("t.status", OpenTimeTableConstant.STATUS_BOOK));
		query.add(Restrictions.le("t.opentime", curtime));
		query.add(Restrictions.gt("t.closetime", curtime));
		query.setProjection(Projections.rowCount());
		List result = hibernateTemplate.findByCriteria(query);
		if(result.isEmpty()){
			return 0;
		}
		return Integer.parseInt(""+result.get(0));
	}
	@Override
	public String getFitItem(String items) {
		List<Long> idList = BeanUtil.getIdList(items, ",");
		List<SportItem> itemList = baseDao.getObjectList(SportItem.class, idList);
		List<String> nameList = BeanUtil.getBeanPropertyList(itemList, String.class, "itemname", true);
		return StringUtils.join(nameList, ",");
	}
	@Override
	public String getFitPlace(String venues) {
		List<Long> idList = BeanUtil.getIdList(venues, ",");
		List<Sport> sportList = baseDao.getObjectList(Sport.class, idList);
		List<String> nameList = BeanUtil.getBeanPropertyList(sportList, String.class, "name", true);
		return StringUtils.join(nameList, ",");
	}
	@Override
	public List<Sport> getFitSportList(String venues) {
		List<Long> idList = BeanUtil.getIdList(venues, ",");
		List<Sport> sportList = baseDao.getObjectList(Sport.class, idList);
		return sportList;
	}
	@Override
	public List<MemberCardInfo> getMemberCardInfoListByMemberid(Long memberid) {
		DetachedCriteria query = DetachedCriteria.forClass(MemberCardInfo.class);
		query.add(Restrictions.eq("memberid", memberid));
		query.add(Restrictions.eq("bindStatus", Status.Y));
		query.addOrder(Order.desc("addtime"));
		List<MemberCardInfo> mctList = hibernateTemplate.findByCriteria(query);
		return mctList;
	}
	@Override
	public List<MemberCardInfo> getValidMemberCardInfoListByMemberid(Long memberid, OpenTimeTable ott) {
		List<MemberCardInfo> result = new ArrayList<MemberCardInfo>();
		if(!ott.isBooking()){
			return result;
		}
		if(!ott.hasField()) {
			return result;
		}
		DetachedCriteria query = DetachedCriteria.forClass(MemberCardInfo.class);
		query.add(Restrictions.eq("memberid", memberid));
		query.add(Restrictions.eq("cardStatus", MemberCardConstant.CARD_STATUS_Y));
		query.add(Restrictions.or(Restrictions.gt("validtime", DateUtil.getMillTimestamp()), Restrictions.isNull("validtime")));
		query.addOrder(Order.desc("addtime"));
		List<MemberCardInfo> mciList = hibernateTemplate.findByCriteria(query);
		for(MemberCardInfo mci : mciList){
			if(mci.hasAvailableByOtt(ott)){
				result.add(mci);
			}
		}
		return result;
	}
	
	@Override
	public ErrorCode<String> reMemberCardInfo(OpenTimeTable ott, Long cardid, ErrorCode<RemoteMemberCardInfo> rmcode, Member member) {
		if(!ott.hasField()){
			return ErrorCode.SUCCESS;
		}
		if(cardid==null){
			return ErrorCode.SUCCESS;
		}
		if(rmcode==null){
			return ErrorCode.getFailure("数据有误！");
		}
		if(!rmcode.isSuccess()){
			return ErrorCode.getFailure(rmcode.getMsg());
		}
		RemoteMemberCardInfo rcard = rmcode.getRetval();
		MemberCardInfo mci = baseDao.getObject(MemberCardInfo.class, cardid);
		if(mci==null){
			return ErrorCode.getFailure("没有查询到该会员卡");
		}
		if(!member.getId().equals(mci.getMemberid())){
			return ErrorCode.getFailure("不能操作他人的会员卡");
		}
		MemberCardType mct = baseDao.getObject(MemberCardType.class, mci.getTypeid());
		SportSynchHelper.copyMemberCardInfo(mci, rcard);
		mci.setTypetitle(mct.getTitle());
		baseDao.saveObject(mci);
		return ErrorCode.SUCCESS;
	}
	
	@Override
	public ErrorCode<List<MemberCardInfo>> bindMemberCard(Member member, List<RemoteMemberCardInfo> rmciList) {
		List<MemberCardInfo> rmiList = new ArrayList();
		boolean isAdd = true;
		Long memberid = member.getId();
		for(RemoteMemberCardInfo rmci : rmciList){
			MemberCardType mct = baseDao.getObjectByUkey(MemberCardType.class, "cardTypeUkey", rmci.getCardTypeUkey());
			if(mct==null){
				isAdd = false;
				break;
			}
			MemberCardInfo mci = baseDao.getObjectByUkey(MemberCardInfo.class, "memberCardCode", rmci.getMemberCardCode());
			if(mci==null){
				mci = new MemberCardInfo();
				mci.setAddtime(DateUtil.getMillTimestamp());
				mci.setTypeid(mct.getId());
				mci.setBindStatus(Status.N);
			}else {
				if(!mci.getMemberid().equals(member.getId())){
					mci.setBindStatus(Status.N);
				}
			}
			SportSynchHelper.copyMemberCardInfo(mci, rmci);
			mci.setTypetitle(mct.getTitle());
			mci.setMemberid(memberid);
			rmiList.add(mci);
		}
		if(!isAdd){
			return ErrorCode.getFailure("卡的类型查询不到，请联系管理员");
		}
		baseDao.saveObjectList(rmiList);
		return ErrorCode.getSuccessReturn(rmiList);
	}
	@Override
	public List<MemberCardInfo> getUnBindMemberCard(Member member, String mobile) {
		DetachedCriteria query = DetachedCriteria.forClass(MemberCardInfo.class);
		query.add(Restrictions.eq("memberid", member.getId()));
		query.add(Restrictions.eq("mobile", mobile));
		query.add(Restrictions.eq("bindStatus", Status.N));
		query.addOrder(Order.desc("addtime"));
		List<MemberCardInfo> mctList = hibernateTemplate.findByCriteria(query);
		return mctList;
	}
	@Override
	public ErrorCode<MemberCardOrder> addMemberCardOrder(MemberCardType mct, Long placeid, String mobile, Member member) throws OrderException {
		return addMemberCardOrder(mct, placeid, mobile, member, null);
	}
	@Override
	public ErrorCode<MemberCardOrder> addMemberCardOrder(MemberCardType mct, Long placeid, String mobile, Member member, ApiUser partner) throws OrderException {
		if(member == null) return ErrorCode.getFailure("请先登录！");
		if(!ValidateUtil.isMobile(mobile)) return ErrorCode.getFailure("手机号有错误！");
		if(mct==null) return ErrorCode.getFailure("卡类型不存在！");
		if(!mct.hasBooking()) return ErrorCode.getFailure("该类型会员卡不接受预定");
		GewaConfig gewaConfig = baseDao.getObject(GewaConfig.class, ConfigConstant.CFG_PAUSE_MEMBERCARD);
		Timestamp cur = DateUtil.getCurFullTimestamp();
		Timestamp pause = Timestamp.valueOf(gewaConfig.getContent());
		if(cur.before(pause)){
			return ErrorCode.getFailure("暂停购买至" + DateUtil.format(pause, "HH:mm"));
		}
		Long memberid = member.getId();
		String membername = member.getNickname();
		Long partnerid = PartnerConstant.GEWA_SELF; 
		if(partner!=null){
			membername = membername + "@" + partner.getBriefname();
			partnerid = partner.getId();
		}
		String ukey = memberid+"";
		Timestamp t = DateUtil.addDay(mct.getOpentime(), 3);
		String randomNum = nextRandomNum(t, 8, "0");
		MemberCardOrder order = new MemberCardOrder(mct, memberid, membername, ukey);
		order.setPartnerid(partnerid);
		String odertitle = "购买" + mct.getTitle() + "会员卡";
		Timestamp addtime = new Timestamp(System.currentTimeMillis());
		order.setTradeNo(PayUtil.getMemberCardTradeNo());
		order.setPlaceid(placeid);
		order.setOrdertitle(odertitle);
		order.setMobile(mobile);
		order.setCheckpass(randomNum);
		Integer unitprice = mct.getGewaprice();
		Integer costprice = mct.getCostprice();
		Integer totalcost = costprice*order.getQuantity();
		order.setUnitprice(unitprice);
		order.setCostprice(costprice);
		order.setTotalfee(unitprice);
		order.setUpdatetime(addtime);
		order.setModifytime(addtime);
		order.setTotalcost(totalcost);
		setOrderDescription(order, mct, baseDao.getObject(Sport.class, placeid));
		baseDao.saveObject(order);
		
		operationService.updateLastOperation("MC" + memberid + StringUtil.md5(ukey), order.getTradeNo(), order.getAddtime(), mct.getClosetime(), "sport");
		return ErrorCode.getSuccessReturn(order);
	}
	private void setOrderDescription(MemberCardOrder order, MemberCardType mct, Sport sport){
		Map<String, String> descMap = VmUtils.readJsonToMap(order.getDescription2());
		descMap.put("场馆名称", sport.getName());
		descMap.put("类型", mct.getCardtypeText());
		descMap.put("详细", mct.getTitle());
		order.setDescription2(JsonUtils.writeMapToJson(descMap));
	}
	@Override
	public void cancelLockFailureOrder(MemberCardOrder order){
		cancelMemberCardOrder(order, order.getMemberid(), OrderConstant.STATUS_REPEAT, "远程锁定失败");
	}
	
	private void cancelMemberCardOrder(MemberCardOrder order, Long memberid, String status, String reason){
		if(order.isNew() && order.getMemberid().equals(memberid)){
			Timestamp validtime = new Timestamp(System.currentTimeMillis()-1000);
			order.setStatus(status);
			order.setValidtime(validtime);
			baseDao.saveObject(order);	
			orderMonitorService.addOrderChangeLog(order.getTradeNo(),"自动取消", order,  reason);
		}
	}

	@Override
	public ErrorCode<MemberCardOrder> processLastOrder(Long memberid, String ukey) {
		MemberCardOrder lastOrder = getLastMemberCardOrder(memberid, ukey);
		if(lastOrder==null) return ErrorCode.SUCCESS;
		if(lastOrder.getStatus().startsWith(OrderConstant.STATUS_PAID_FAILURE)){
			return ErrorCode.getFailure("您还有一个订单等待处理，订单号为" + lastOrder.getTradeNo() + "，请稍后再下新订单！");
		}
		if(lastOrder.isNew()){//取消未支付
			cancelMemberCardOrder(lastOrder, memberid, OrderConstant.STATUS_REPEAT, "重复订单");
		}
		return ErrorCode.getSuccessReturn(lastOrder);
	}
	private MemberCardOrder getLastMemberCardOrder(Long memberid, String ukey){
		LastOperation last = baseDao.getObject(LastOperation.class, "MC" + memberid + StringUtil.md5(ukey));
		if(last==null) return null;
		MemberCardOrder order = baseDao.getObjectByUkey(MemberCardOrder.class, "tradeNo", last.getLastvalue());
		return order;
	}
	
	@Override
	public OrderContainer processOrderPay(MemberCardOrder order) throws OrderException{
		return processOrderPayInternal(order);
	}
	@Override
	public void processMemberCardOrder(MemberCardOrder order, MemberCardType mct) throws OrderException{
		if(order.isPaidUnfix()){
			ErrorCode<RemoteMemberCardOrder> code = remoteMemberCardService.commitRemoteMemberCardOrder(order);
			if(!code.isSuccess()){
				code = remoteMemberCardService.getRemoteMemberCardOrderByTradeno(order);
				if(!code.isSuccess()){
					throw new OrderException(ApiConstant.CODE_DATA_ERROR, code.getMsg());
				}
			}
			Timestamp cur = DateUtil.getMillTimestamp();
			RemoteMemberCardOrder rorder = code.getRetval();
			MemberCardInfo card = new MemberCardInfo();
			card.setAddtime(cur);
			mct.addSales(order.getQuantity());
			SportSynchHelper.copyMemberCardInfo(card, order, rorder);
			card.setBindStatus(Status.Y);
			baseDao.saveObject(card);
			
			order.setUpdatetime(cur);
			order.setModifytime(cur);
			order.setValidtime(DateUtil.addDay(cur, 180));
			order.setStatus(OrderConstant.STATUS_PAID_SUCCESS);
			order.setSettle(OrderConstant.SETTLE_Y);
			order.setCardid(card.getId());
			String des2 = JsonUtils.addJsonKeyValue(order.getDescription2(), "卡号", card.getMemberCardCode());
			order.setDescription2(des2);
			
			baseDao.saveObject(mct);
			baseDao.saveObject(order);
			processOrderExtra(order);
		}else{
			throw new OrderException(ApiConstant.CODE_DATA_ERROR, "订单状态不正确！");
		}
	}
	@Override
	public List<MemberCardOrder> getMemberCardOrderList(SearchOrderCommand soc) {
		DetachedCriteria query = DetachedCriteria.forClass(MemberCardOrder.class, "o");
		if(StringUtils.isNotBlank(soc.getMobile())) query.add(Restrictions.eq("o.mobile", soc.getMobile()));
		if(StringUtils.isNotBlank(soc.getTradeNo())) query.add(Restrictions.eq("o.tradeNo", soc.getTradeNo()));
		if(StringUtils.isNotBlank(soc.getCitycode())) query.add(Restrictions.eq("o.citycode", soc.getCitycode()));
		if(soc.getMctid()!=null){
			query.add(Restrictions.eq("o.mctid", soc.getMctid()));
		}
		if(soc.getMinute()!=null){
			Timestamp from = DateUtil.addMinute(new Timestamp(System.currentTimeMillis()), -soc.getMinute());
			query.add(Restrictions.ge("o.addtime", from));
		}
		if(soc.getTimeFrom() != null){
			query.add(Restrictions.ge("o.addtime", soc.getTimeFrom()));
		}
		if(soc.getTimeTo() != null){
			query.add(Restrictions.le("o.addtime", soc.getTimeTo()));
		}
		if(StringUtils.isNotBlank(soc.getOrdertype())){
			if(soc.getOrdertype().equals(OrderConstant.STATUS_CANCEL)){
				query.add(Restrictions.or(Restrictions.like("o.status", soc.getOrdertype(), MatchMode.START),
						Restrictions.and(Restrictions.like("o.status", OrderConstant.STATUS_NEW, MatchMode.START), 
								Restrictions.lt("o.validtime", new Timestamp(System.currentTimeMillis())))));
			}else{
				query.add(Restrictions.like("o.status", soc.getOrdertype(), MatchMode.START));
				if(StringUtils.startsWith(soc.getOrdertype(), OrderConstant.STATUS_NEW)){
					query.add(Restrictions.ge("o.validtime", new Timestamp(System.currentTimeMillis())));
				}
			}
		}
		query.addOrder(Order.desc("addtime"));
		List<MemberCardOrder> orderList = hibernateTemplate.findByCriteria(query);
		return orderList;
	}
	private List<MemberCardTypePlace> getMemberCardTypePlaceList(MemberCardType mct){
		List<MemberCardTypePlace> result = baseDao.getObjectListByField(MemberCardTypePlace.class, "mctid", mct.getId());
		return result;
	}
	@Override
	public List<MemberCardType> synchMemberCardOrderList(List<RemoteMemberCardType> rmctList) {
		List<MemberCardType> mctList = new ArrayList<MemberCardType>();
		for(RemoteMemberCardType rmct : rmctList){
			MemberCardType mct = baseDao.getObjectByUkey(MemberCardType.class, "cardTypeUkey", rmct.getCardTypeUkey());
			boolean isAdd = false;
			if(mct==null){
				mct = new MemberCardType();
				mct.intiMemberCardType();
				isAdd = true;
			}
			SportSynchHelper.copyMemberCardType(mct, rmct);
			baseDao.saveObjectList(mct);
			mctList.add(mct);
			List<Long> idList = BeanUtil.getIdList(rmct.getBelongVenue(), ",");
			if(!isAdd){
				List<MemberCardTypePlace> result = getMemberCardTypePlaceList(mct);
				baseDao.removeObjectList(result);
				hibernateTemplate.flush();
			}
			for(Long placeid : idList){
				MemberCardTypePlace place = new MemberCardTypePlace();
				place.setMctid(mct.getId());
				place.setPlaceid(placeid);
				baseDao.saveObject(place);
			}
		}
		return mctList;
	}
	@Override
	public ErrorCode<String> validCardByOtt(MemberCardInfo card, OpenTimeTable ott) {
		if(!ott.hasRemoteOtt()){
			return ErrorCode.SUCCESS;
		}
		if(!ott.hasField()){
			return ErrorCode.SUCCESS;
		}
		if(!StringUtils.equals(card.getCardStatus(), MemberCardConstant.CARD_STATUS_Y)){
			return ErrorCode.getFailure("该卡的状态不可用！");
		}
		if(card.getValidtime()!=null){
			if(card.getValidtime().before(DateUtil.getMillTimestamp())){
				return ErrorCode.getFailure("该卡已过期！");
			}
		}
		if(StringUtils.isNotBlank(card.getFitItem())){
			List<Long> itemList = BeanUtil.getIdList(card.getFitItem(), ",");
			if(!itemList.contains(ott.getItemid())){
				return ErrorCode.getFailure("该卡不适用该项目！");
			}
		}
		List<Long> sportidList = BeanUtil.getIdList(card.getBelongVenue(), ",");
		if(!sportidList.contains(ott.getSportid())){
			return ErrorCode.getFailure("该卡不适用该运动场馆！");
		}
		return ErrorCode.SUCCESS;
	}
	@Override
	public ErrorCode<String> validCardByOtt(MemberCardType mct, MemberCardInfo card, OpenTimeTable ott, List<OpenTimeItem> otiList) {
		ErrorCode<String> code = validCardByOtt(card, ott);
		if(!code.isSuccess()){
			return ErrorCode.getFailure(code.getMsg());
		}
		if(!mct.hasBooking()){
			return ErrorCode.getFailure("该卡类型暂不接受预定！");
		}
		if(!StringUtils.equals(card.getBindStatus(), Status.Y)){
			return ErrorCode.getFailure("该卡未绑定，请先绑定！");
		}
		if(mct.hasNumCard()){
			if(card.getOverMoney()<otiList.size()){
				return ErrorCode.getFailure("该卡的次数("+card.getOverMoney()+")已少于选择场次的数量！");
			}
		}else if(mct.hasAmountCard()){
			int amount = 0;
			for(OpenTimeItem oti : otiList){
				amount = amount + oti.getPrice();
			}
			if(card.getOverMoney()<amount){
				return ErrorCode.getFailure("该卡的金额("+card.getOverMoney()+")已少于选择场次的总金额！");
			}
		}else {
			return ErrorCode.getFailure("该卡的类型的错误，请联系管理员！");
		}
		return ErrorCode.SUCCESS;
	}
	@Override
	public void memberCardPayOrder(GewaOrder order, Long memberId) throws OrderException {
		if(order != null){
			if(order.isAllPaid()) return;
			if(order.isCancel()){
				throw new OrderException(ApiConstant.CODE_SEAT_OCCUPIED, "不能操作已取消的订单！");
			}
			if(!order.getMemberid().equals(memberId)){
				throw new OrderException(ApiConstant.CODE_SEAT_OCCUPIED, "不能操作他人的订单");
			}
			SportOrder sorder = (SportOrder)order;
			if(sorder.getDiscount()>0){
				throw new OrderException(ApiConstant.CODE_SEAT_OCCUPIED, "暂不支持折扣！");
			}
			if(!sorder.hasMemberCardPay()){
				throw new OrderException(ApiConstant.CODE_SEAT_OCCUPIED, "支付方式错误！");
			}
			MemberCardInfo memberCard = baseDao.getObject(MemberCardInfo.class, sorder.getCardid());
			MemberCardType mct = baseDao.getObject(MemberCardType.class, memberCard.getTypeid());
			OpenTimeTable ott = baseDao.getObject(OpenTimeTable.class, sorder.getOttid());
			List<OpenTimeItem> otiList = sportOrderService.getMyOtiList(order.getId());
			ErrorCode<String> vcode = validCardByOtt(mct, memberCard, ott, otiList);
			if(!vcode.isSuccess()){
				throw new OrderException(ApiConstant.CODE_SEAT_OCCUPIED, vcode.getMsg());
			}
			String smspass =JsonUtils.getJsonValueByKey(order.getOtherinfo(), MemberCardConstant.SMSPASS);
			ErrorCode<RemoteMemberCardInfo> code = remoteMemberCardService.cardPay(sorder, mct, memberCard, smspass);
			if(!code.isSuccess()){
				throw new OrderException(ApiConstant.CODE_SEAT_OCCUPIED, code.getMsg());
			}
			RemoteMemberCardInfo rcard = code.getRetval();
			memberCard.setOverMoney(rcard.getOverMoney());
			memberCard.setCardStatus(rcard.getCardStatus());
			baseDao.saveObject(memberCard);
			Timestamp curTime = DateUtil.getCurFullTimestamp();
			dbLogger.warn(order.getTradeNo() + "memberCardPay支付前状态：" + order.getStatus());
			order.setStatus(OrderConstant.STATUS_PAID_FAILURE); 
			order.setPaymethod(PaymethodConstant.PAYMETHOD_MEMBERCARDPAY);
			order.setAlipaid(order.getDue());
			order.setPaidtime(curTime);
			order.setUpdatetime(curTime);
			order.setModifytime(curTime);
			if(mct.getDiscount()!=null && mct.hasAmountCard()){
				Integer paidAmount = Math.round(order.getDue()*mct.getDiscount()/100f);
				String otherinfo = JsonUtils.addJsonKeyValue(order.getOtherinfo(), "实付金额", paidAmount+"");
				order.setOtherinfo(otherinfo);
			}
			baseDao.saveObject(order);
		}else{
			throw new OrderException(ApiConstant.CODE_SEAT_OCCUPIED, "不存在的订单！");
		}
	}
}
