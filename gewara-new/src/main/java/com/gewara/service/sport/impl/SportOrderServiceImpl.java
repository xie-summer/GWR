package com.gewara.service.sport.impl;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.criterion.Subqueries;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.support.PropertyComparator;
import org.springframework.stereotype.Service;

import com.gewara.command.SearchOrderCommand;
import com.gewara.constant.ApiConstant;
import com.gewara.constant.MemberCardConstant;
import com.gewara.constant.OpenTimeItemConstant;
import com.gewara.constant.OpenTimeTableConstant;
import com.gewara.constant.PayConstant;
import com.gewara.constant.PaymethodConstant;
import com.gewara.constant.Status;
import com.gewara.constant.TagConstant;
import com.gewara.constant.sys.ConfigConstant;
import com.gewara.constant.ticket.OrderConstant;
import com.gewara.constant.ticket.OrderExtraConstant;
import com.gewara.constant.ticket.PartnerConstant;
import com.gewara.helper.SportOrderHelper;
import com.gewara.helper.discount.SpecialDiscountHelper;
import com.gewara.helper.discount.SportSpecialDiscountHelper;
import com.gewara.helper.discount.SportSpecialDiscountHelper.OrderCallback;
import com.gewara.helper.order.GewaOrderHelper;
import com.gewara.helper.order.OrderContainer;
import com.gewara.helper.order.SportOrderContainer;
import com.gewara.model.api.ApiUser;
import com.gewara.model.common.GewaConfig;
import com.gewara.model.common.LastOperation;
import com.gewara.model.pay.Adjustment;
import com.gewara.model.pay.BuyItem;
import com.gewara.model.pay.Charge;
import com.gewara.model.pay.Discount;
import com.gewara.model.pay.ElecCard;
import com.gewara.model.pay.MemberAccount;
import com.gewara.model.pay.OrderExtra;
import com.gewara.model.pay.Spcounter;
import com.gewara.model.pay.SpecialDiscount;
import com.gewara.model.pay.SportOrder;
import com.gewara.model.sport.CusOrder;
import com.gewara.model.sport.MemberCardInfo;
import com.gewara.model.sport.MemberCardType;
import com.gewara.model.sport.OpenTimeItem;
import com.gewara.model.sport.OpenTimeSale;
import com.gewara.model.sport.OpenTimeSaleMember;
import com.gewara.model.sport.OpenTimeTable;
import com.gewara.model.sport.SellDeposit;
import com.gewara.model.sport.SellTimeTable;
import com.gewara.model.sport.Sport;
import com.gewara.model.sport.Sport2Item;
import com.gewara.model.sport.SportField;
import com.gewara.model.sport.SportItem;
import com.gewara.model.sport.SportOrder2TimeItem;
import com.gewara.model.sport.SportProfile;
import com.gewara.model.user.Member;
import com.gewara.model.user.MemberInfo;
import com.gewara.pay.PayUtil;
import com.gewara.pay.PayValidHelper;
import com.gewara.service.OrderException;
import com.gewara.service.gewapay.ScalperService;
import com.gewara.service.order.impl.GewaOrderServiceImpl;
import com.gewara.service.sport.GuaranteeOrderService;
import com.gewara.service.sport.MemberCardService;
import com.gewara.service.sport.OpenTimeSaleService;
import com.gewara.service.sport.OpenTimeTableService;
import com.gewara.service.sport.SportOrderService;
import com.gewara.service.sport.SportService;
import com.gewara.support.ErrorCode;
import com.gewara.util.BeanUtil;
import com.gewara.util.DateUtil;
import com.gewara.util.JsonUtils;
import com.gewara.util.StringUtil;
import com.gewara.util.ValidateUtil;
import com.gewara.util.VmUtils;
import com.gewara.xmlbind.sport.RemoteMemberCardInfo;

@Service("sportOrderService")
public class SportOrderServiceImpl extends GewaOrderServiceImpl implements SportOrderService {
	
	@Autowired@Qualifier("sportService")
	public SportService sportService;
	public void setSportService(SportService sportService){
		this.sportService = sportService;
	}
	@Autowired@Qualifier("openTimeTableService")
	private OpenTimeTableService openTimeTableService;
	
	@Autowired@Qualifier("openTimeSaleService")
	private OpenTimeSaleService openTimeSaleService;
	
	@Autowired@Qualifier("guaranteeOrderService")
	private GuaranteeOrderService guaranteeOrderService;
	
	@Autowired@Qualifier("scalperService")
	private ScalperService scalperService;
	
	@Autowired@Qualifier("memberCardService")
	private MemberCardService memberCardService;
	
	@Override
	public ErrorCode validateFieldLock(List<OpenTimeItem> otiList) {
		String msg = "";
		for(OpenTimeItem oti : otiList){
			if(!oti.hasAvailable()) msg = "[" + oti.getFieldname() + oti.getHour() + "]";
		}
		if(StringUtils.isBlank(msg)) return ErrorCode.SUCCESS;
		return ErrorCode.getFailure(msg+"被占用");
	}
	@Override
	public ErrorCode checkOrderField(SportOrder order, List<OpenTimeItem> otiList) {
		String msg = "";
		for(OpenTimeItem oti : otiList){
			if(!oti.getMemberid().equals(order.getMemberid())) msg = "[" + oti.getHour() + "]";
			else if(OpenTimeItemConstant.STATUS_LOCKR.equals(oti.getStatus())){
				msg = "[" + oti.getHour() + "]";
			}
		}
		if(StringUtils.isBlank(msg)) return ErrorCode.SUCCESS;
		return ErrorCode.getFailure(ApiConstant.CODE_SEAT_OCCUPIED, msg+"被占用");
	}
	@Override
	public Map<Integer, List<Sport>> getProfileSportList() {
		List<SportProfile> spList = hibernateTemplate.find("from SportProfile p where p.booking='open' order by p.sortnum");
		Map<Integer, List<Sport>> spMap = new TreeMap<Integer, List<Sport>>();
		for(SportProfile sp : spList){
			Integer key = sp.getSortnum();
			Sport sport  = baseDao.getObject(Sport.class, sp.getId());
			List<Sport> sportList = new ArrayList<Sport>();
			if(spMap.containsKey(key)){
				sportList = spMap.get(key);
				sportList.add(sport);
			}else{
				sportList.add(sport);
			}
			spMap.put(key, sportList);
		}
		return spMap;
	}

	@Override
	public SportOrder getLastUnpaidSportOrder(Long memberid, String ukey, Long ottid) {
		DetachedCriteria query = DetachedCriteria.forClass(SportOrder.class);
		query.add(Restrictions.eq("memberid", memberid));
		if(ottid!=null) query.add(Restrictions.eq("ottid", ottid));
		query.add(Restrictions.eq("ukey", ukey));
		query.add(Restrictions.like("status", OrderConstant.STATUS_NEW, MatchMode.START));
		query.add(Restrictions.gt("validtime", new Timestamp(System.currentTimeMillis())));
		query.addOrder(Order.desc("addtime"));
		List<SportOrder> result = hibernateTemplate.findByCriteria(query);
		if(result.isEmpty()) return null;
		return result.get(0);
	}
	
	@Override
	public void cancelSportOrder(SportOrder order, Long memberid, String reason) {
		if(order.isNew() && order.getMemberid().equals(memberid)){
			OpenTimeTable ott = baseDao.getObject(OpenTimeTable.class, order.getOttid());
			if(ott.hasPeriod()||ott.hasInning()){
				Timestamp validtime = new Timestamp(System.currentTimeMillis()-1000);
				order.setStatus(OrderConstant.STATUS_REPEAT);
				order.setValidtime(validtime);
				baseDao.saveObject(order);				
			}else{
				Timestamp validtime = new Timestamp(System.currentTimeMillis()-1000);
				order.setStatus(OrderConstant.STATUS_REPEAT);
				order.setValidtime(validtime);
				List<OpenTimeItem> otiList = getMyOtiList(order.getId());
				for(OpenTimeItem oti : otiList){
					oti.setValidtime(validtime);
					baseDao.saveObject(oti);
				}
				baseDao.saveObject(order);
				dbLogger.warn("取消未支付订单：" + order.getTradeNo() + "," + reason);
			}
		}
	}
	@Override
	public String getRemoteOtiids(List<OpenTimeItem> otiList){
		List<Long> ridsList = BeanUtil.getBeanPropertyList(otiList, Long.class, "rotiid", true);
		String strids = StringUtils.join(ridsList, ",");
		return strids;
	}
	@Override
	public ErrorCode<SportOrder> addSportOrder(OpenTimeTable ott, String fields, Long cardid, ErrorCode<RemoteMemberCardInfo> rmcode, String mobile, Member member, ApiUser partner)
			throws OrderException {
		GewaConfig gewaConfig = baseDao.getObject(GewaConfig.class, ConfigConstant.CFG_PAUSE_SPORT);
		Timestamp cur = DateUtil.getCurFullTimestamp();
		Timestamp pause = Timestamp.valueOf(gewaConfig.getContent());
		if(cur.before(pause)){
			return ErrorCode.getFailure("暂停售票至" + DateUtil.format(pause, "HH:mm"));
		}
		if(ott == null) return ErrorCode.getFailure("该场次不存在！");
		List<Long> fieldidList = BeanUtil.getIdList(fields, ",");
		if(fieldidList.size()==0) return ErrorCode.getFailure("请选择场地及时间！");
		if(fieldidList.size()>4) return ErrorCode.getFailure("每次最多选4个时间段！");
		//获取改场馆的限制时间 cpf
		Sport2Item sport2Item = sportService.getSport2Item(ott.getSportid(), ott.getItemid());
		if(!sport2Item.isOpen()) return ErrorCode.getFailure("请在开放时间内进行预订！");
		Integer limitMinutes = sport2Item.getLimitminutes();
		//获取改场馆的限制时间 cpf
		if(!ott.isBooking()) return ErrorCode.getFailure("暂不接受预定！");
		if(!ott.hasField()) return ErrorCode.getFailure("非法错误！");
		if(ott.getPlaydate().compareTo(DateUtil.getBeginningTimeOfDay(new Date()))==0) { //时间过时
			for(Long id : fieldidList ){
				OpenTimeItem item = baseDao.getObject(OpenTimeItem.class, id);
				if(item.getHour().compareTo(item.gainZhour(limitMinutes))<0) return ErrorCode.getFailure(item.getHour()+"已过期不能购买！");
			}
		}
		Set<Long> fieldidList2 = new LinkedHashSet<Long>(fieldidList);
		List<OpenTimeItem> otiList = baseDao.getObjectList(OpenTimeItem.class, fieldidList2);
		ErrorCode code = validateFieldLock(otiList);
		if(!code.isSuccess()) return ErrorCode.getFailure(code.getMsg());
		Map<String, List<OpenTimeItem>> saleMap = BeanUtil.groupBeanList(otiList, "itemtype");
		if(!CollectionUtils.isEmpty(saleMap.get(OpenTimeTableConstant.ITEM_TYPE_VIE))){
			List<Long> otsIdList = BeanUtil.getBeanPropertyList(saleMap.get(OpenTimeTableConstant.ITEM_TYPE_VIE), "otsid", true);
			List<OpenTimeSale> saleList = baseDao.getObjectList(OpenTimeSale.class, otsIdList);
			for (OpenTimeSale sale : saleList) {
				if(sale.hasBooking()) return ErrorCode.getFailure("竞价场次，非法购票！");
			}
		}
		ErrorCode code2 = validateOpenTimeItem(ott, otiList, cardid);
		if(!code2.isSuccess()) return ErrorCode.getFailure(code2.getMsg());
		ErrorCode<String> ccode = memberCardService.reMemberCardInfo(ott, cardid, rmcode, member);
		if(!ccode.isSuccess()){
			return ErrorCode.getFailure(ccode.getMsg());
		}
		boolean isMemberCardPay = false;
		MemberCardInfo card = null;
		if(cardid!=null){
			card = baseDao.getObject(MemberCardInfo.class, cardid);
			MemberCardType mct = baseDao.getObject(MemberCardType.class, card.getTypeid());
			ErrorCode<String> mctcode = memberCardService.validCardByOtt(mct, card, ott, otiList);
			if(!mctcode.isSuccess()){
				return ErrorCode.getFailure(mctcode.getMsg());
			}
			isMemberCardPay = true;
		}

		Long memberid = member.getId();
		String membername = member.getNickname();
		Long partnerid = PartnerConstant.GEWA_SELF; 
		if(partner!=null){
			membername = membername + "@" + partner.getBriefname();
			partnerid = partner.getId();
		}
		Timestamp t = DateUtil.addDay(new Timestamp(ott.getPlaydate().getTime()),1);
		String randomNum = nextRandomNum(t, 8, "0");
		SportOrder order = new SportOrder(memberid, membername, ott, ""+memberid);
		order.setStatus(OrderConstant.STATUS_NEW);
		order.setPartnerid(partnerid);
		String odertitle = ott.getSportname() + ott.getItemname() + "场次预订";
		Timestamp addtime = new Timestamp(System.currentTimeMillis());
		Timestamp validtime = DateUtil.addMinute(addtime, OpenTimeTableConstant.MAX_MINUTS_TICKETS);
		order.setTradeNo(PayUtil.getSportTradeNo());
		order.setOrdertitle(odertitle);
		order.setMobile(mobile);
		order.setValidtime(validtime);
		order.setCheckpass(randomNum);
		Integer total = 0;
		Integer unitprice = 0;
		Integer costprice = 0;
		Integer sumcost = 0;
		String minhour = otiList.get(0).getHour();
		for(OpenTimeItem oti : otiList){
			if(oti.getPrice()<=0) return ErrorCode.getFailure("价格有错误，请选择其它场地");
			oti.setValidtime(validtime);
			oti.setMemberid(memberid);
			total += oti.getPrice();
			unitprice = oti.getPrice();
			costprice = oti.getCostprice();
			sumcost += oti.getCostprice();
			baseDao.saveObject(oti);
			if(oti.getHour().compareTo(minhour)<0) minhour = oti.getHour();
		}
		if(total<=0) return ErrorCode.getFailure("价格有错误，请选择其它场地");
		order.setUnitprice(unitprice);
		order.setCostprice(costprice);
		order.setQuantity(otiList.size());
		order.setTotalfee(total);
		order.setUpdatetime(addtime);
		order.setModifytime(addtime);
		order.setTotalcost(sumcost);
		setOrderDescription(order, ott, card, otiList, minhour);
		setOrderOtherinfo(order, sumcost); 	//记录总成本
		order.setCitycode(ott.getCitycode());
		order.setCardid(cardid);
		if(isMemberCardPay){
			order.setPaymethod(PaymethodConstant.PAYMETHOD_MEMBERCARDPAY);
		}
		baseDao.saveObject(order);
		for(OpenTimeItem oti : otiList){
			baseDao.saveObject(new SportOrder2TimeItem(order.getId(), oti.getId()));
			newBuyItem(ott, oti, 1);
		}
		if(ott.getRemoteid() != null){
			CusOrder cusOrder = new CusOrder();
			cusOrder.setOrderid(order.getId());
			baseDao.saveObject(cusOrder);
		}
		operationService.updateLastOperation("S" + memberid + StringUtil.md5(String.valueOf(memberid)), order.getTradeNo(), order.getAddtime(), ott.getPlayTimeByHour(minhour), "sport");
		return ErrorCode.getSuccessReturn(order);
	}
	
	@Override
	public ErrorCode<SportOrder> addSportOrder(OpenTimeSale ots) throws OrderException{
		if(!ots.hasLockStatus(OpenTimeTableConstant.SALE_STATUS_SUCCESS)){
			return ErrorCode.getFailure(ApiConstant.CODE_SIGN_ERROR, "竞价未成功！");
		}
		OpenTimeTable ott = baseDao.getObject(OpenTimeTable.class, ots.getOttid());
		OpenTimeSaleMember saleMember = openTimeSaleService.getLastOtsMember(ots.getId());
		if(saleMember != null && !VmUtils.eq(ots.getMemberid(), saleMember.getMemberid())){
			ots.setMemberid(saleMember.getMemberid());
			ots.setNickname(saleMember.getNickname());
			ots.setJointime(saleMember.getAddtime());
		}
		SellDeposit deposit = guaranteeOrderService.getSellDeposit(ots.getId(), ots.getMemberid(), SellDeposit.STATUS_PAID_SUCCESS);
		if(deposit == null) throw new OrderException(ApiConstant.CODE_SIGN_ERROR, "保证金错误！");
		if(!ots.getMemberid().equals(deposit.getMemberid())) return ErrorCode.getFailure(ApiConstant.CODE_SIGN_ERROR, "不能修改他人信息！");
		SportOrder order = baseDao.getObject(SportOrder.class, ots.getId());
		if(order != null) return ErrorCode.getSuccessReturn(order);
		Timestamp t = DateUtil.addDay(new Timestamp(ott.getPlaydate().getTime()),1);
		order = new SportOrder(ots.getMemberid(), ots.getNickname(), ott, String.valueOf(ots.getMemberid()));
		order.setStatus(OrderConstant.STATUS_NEW);
		order.setPartnerid(PartnerConstant.GEWA_SELF);
		String odertitle = ott.getSportname() + ott.getItemname() + "场次预订";
		Timestamp addtime = new Timestamp(System.currentTimeMillis());
		Timestamp validtime = DateUtil.addHour(addtime, OpenTimeTableConstant.MAX_HOUR_TICKETS);
		String randomNum = nextRandomNum(t, 8, "0");
		order.setTradeNo(PayUtil.getSportTradeNo());
		order.setOrdertitle(odertitle);
		order.setMobile(deposit.getMobile());
		order.setValidtime(validtime);
		order.setCheckpass(randomNum);
		int unitprice = 0, costprice = 0, sumcost = 0;
		List<OpenTimeItem> otiList = baseDao.getObjectList(OpenTimeItem.class, BeanUtil.getIdList(ots.getOtiids(), ","));
		ErrorCode code = validateFieldLock(otiList);
		if(!code.isSuccess()){
			dbLogger.warn("validateFieldLock:" + code.getMsg());
			throw new OrderException(ApiConstant.CODE_SIGN_ERROR, code.getMsg());
		}
		for(OpenTimeItem oti : otiList){
			if(oti.getPrice()<=0) return ErrorCode.getFailure("价格有错误，请选择其它场地");
			oti.setValidtime(validtime);
			oti.setMemberid(ots.getMemberid());
			unitprice = oti.getPrice();
			costprice = oti.getCostprice();
			sumcost += oti.getCostprice();
			baseDao.saveObject(oti);
		}
		order.setUnitprice(unitprice);
		order.setCostprice(costprice);
		order.setQuantity(otiList.size());
		order.setTotalfee(ots.getCurprice());
		order.setUpdatetime(addtime);
		order.setModifytime(addtime);
		order.setTotalcost(sumcost);
		setOrderDescription(order, ott, null, otiList, ots.getStarttime());
		setOrderOtherinfo(order, sumcost); 	//记录总成本
		Map<String,String> otherinfoMap = JsonUtils.readJsonToMap(order.getOtherinfo());
		otherinfoMap.put(PayConstant.DISCOUNT_TAG_DEPOSIT, String.valueOf(deposit.getPrice()));
		otherinfoMap.put("depositId", String.valueOf(deposit.getId()));
		
		order.setOtherinfo(JsonUtils.writeMapToJson(otherinfoMap));
		order.setCitycode(ott.getCitycode());
		baseDao.saveObject(order);
		ots.setOrderid(order.getId());
		ots.setPaidvalidtime(order.getValidtime());
		baseDao.saveObject(ots);
		for(OpenTimeItem oti : otiList){
			baseDao.saveObject(new SportOrder2TimeItem(order.getId(), oti.getId()));
			newBuyItem(ott, oti, 1);
		}
		if(ott.hasRemoteOtt()){
			CusOrder cusOrder = new CusOrder();
			cusOrder.setOrderid(order.getId());
			baseDao.saveObject(cusOrder);
		}
		useSellDeposit(order, ots, deposit, addtime);
		return ErrorCode.getSuccessReturn(order);
	}
	
	@Override
	public ErrorCode<SportOrder> addSportOrder(Long otiid, String starttime, Integer time, Integer quantity, String mobile, Member member, ApiUser partner){
		GewaConfig gewaConfig = baseDao.getObject(GewaConfig.class, ConfigConstant.CFG_PAUSE_SPORT);
		Timestamp cur = DateUtil.getCurFullTimestamp();
		Timestamp pause = Timestamp.valueOf(gewaConfig.getContent());
		if(cur.before(pause)){
			return ErrorCode.getFailure(ApiConstant.CODE_OPI_CLOSED, "暂停售票至" + DateUtil.format(pause, "HH:mm"));
		}
		OpenTimeItem oti = baseDao.getObject(OpenTimeItem.class, otiid);
		if(oti == null) return ErrorCode.getFailure("场地该时段不存在！");
		if(!oti.hasStatusNew() || cur.after(oti.getValidtime())) return ErrorCode.getFailure("本场地时段不接受预订！");
		/*if(oti.hasItemType(OpenTimeTableConstant.ITEM_TYPE_VIP)) return ErrorCode.getFailure("本场地为会员场地，请走会员通道！");
		if(oti.hasItemType(OpenTimeTableConstant.ITEM_TYPE_VIE)){
			//OpenTimeSale
			return ErrorCode.getFailure("本场地为竞拍场地，请走竞拍通道！");
		} */
		OpenTimeTable ott = baseDao.getObject(OpenTimeTable.class, oti.getOttid());
		if(ott == null) return ErrorCode.getFailure("场次不存在！");
		Sport2Item sport2Item = sportService.getSport2Item(ott.getSportid(), ott.getItemid());
		if(!sport2Item.isOpen()) return ErrorCode.getFailure("请在开放时间内进行预订！");
		if(!ott.isBooking()) return ErrorCode.getFailure("本场不接受预订！");
		if(!ott.hasPeriod() || !oti.hasPeriod()) return ErrorCode.getFailure("非时间段场次或场地！");
		return createSportOrder(ott, oti, starttime, mobile, quantity, time, member, partner);
	}
	@Override
	public ErrorCode<SportOrder> addSportOrder(Long otiid, String starttime, Integer quantity, String mobile, Member member, ApiUser parnter){
		GewaConfig gewaConfig = baseDao.getObject(GewaConfig.class, ConfigConstant.CFG_PAUSE_SPORT);
		Timestamp cur = DateUtil.getCurFullTimestamp();
		Timestamp pause = Timestamp.valueOf(gewaConfig.getContent());
		if(cur.before(pause)){
			return ErrorCode.getFailure(ApiConstant.CODE_OPI_CLOSED, "暂停售票至" + DateUtil.format(pause, "HH:mm"));
		}
		OpenTimeItem oti = baseDao.getObject(OpenTimeItem.class, otiid);
		if(oti == null) return ErrorCode.getFailure("场地该时段不存在！");
		if(!oti.hasStatusNew() || cur.after(oti.getValidtime())) return ErrorCode.getFailure("本场地时段不接受预订！");
		OpenTimeTable ott = baseDao.getObject(OpenTimeTable.class, oti.getOttid());
		if(ott == null) return ErrorCode.getFailure("场次不存在！");
		Sport2Item sport2Item = sportService.getSport2Item(ott.getSportid(), ott.getItemid());
		if(!sport2Item.isOpen()) return ErrorCode.getFailure("请在开放时间内进行预订！");
		if(!ott.isBooking()) return ErrorCode.getFailure("本场不接受预订！");
		if(!ott.hasInning() || !oti.hasInning()) return ErrorCode.getFailure("非局数场次或场地！");
		return createSportOrder(ott, oti, starttime, mobile, quantity, null, member, parnter);
	}
	private ErrorCode<SportOrder> createSportOrder(OpenTimeTable ott, OpenTimeItem oti, String starttime, String mobile, Integer quantity, Integer time, Member member, ApiUser partner){
		if((!ott.hasInning() || !oti.hasInning()) && (!ott.hasPeriod() || !oti.hasPeriod())) return ErrorCode.getFailure("场次或场地数据错误！");
		Timestamp cur = DateUtil.getCurFullTimestamp();
		Date curDate = DateUtil.getDateFromTimestamp(cur);
		Sport2Item sport2Item = sportService.getSport2Item(ott.getSportid(), ott.getItemid());
		Date validDate = DateUtil.addMinute(curDate, sport2Item.getLimitminutes());
		Date curDate2 = DateUtil.getBeginningTimeOfDay(curDate);
		String hour = DateUtil.format(validDate, "HH:mm");
		if(DateUtil.getDiffDay(ott.getPlaydate(), curDate2) == 0){
			if(oti.getEndhour().compareTo(hour)<=0) return ErrorCode.getFailure(ott.getEndtime() + "已过期不能购买！");
		}
		if(StringUtils.isBlank(starttime)) ErrorCode.getFailure("开始时间段不能为空！");
		if(quantity == null) return ErrorCode.getFailure("选择人数不能为空！");
		if(quantity <1 ||quantity >4) return ErrorCode.getFailure("预订人数只能是1-4个！");
		if(StringUtils.isBlank(mobile)) return ErrorCode.getFailure("手机号不能为空！");
		if(!ValidateUtil.isMobile(mobile)) return ErrorCode.getFailure("手机号格式错误！");
		List<String> timeList = SportOrderHelper.getStarttimeList(ott.getPlaydate(), oti);
		if(!timeList.contains(starttime)) return ErrorCode.getFailure("入场时间不在该场次时间段以内！");
		if(ott.hasPeriod()){
			if(time == null) return ErrorCode.getFailure("时长不能为空！");
			List<Integer> periodList = SportOrderHelper.getPeriodList(ott.getPlaydate(), oti);
			if(!periodList.contains(time)) return ErrorCode.getFailure("时长错误！");
		}else{
			time = oti.getUnitMinute();
		}
		int count = getSellTimeTableCount(oti.getId(), starttime);
		if(quantity+ count > oti.getQuantity()) return ErrorCode.getFailure("可预订人数已满！");
		Long memberid = member.getId();
		String membername = member.getNickname();
		Long partnerid = PartnerConstant.GEWA_SELF;
		if(partner!=null) { 
			membername = membername + "@" + partner.getBriefname();
			partnerid = partner.getId();
		}
		/*SportOrder lastUnpaidOrder = getLastUnpaidSportOrder(memberid, member.getId()+"", ott.getId());
		if(lastUnpaidOrder != null) cancelSportOrder(lastUnpaidOrder, member.getId(), "重复订单");*/
		SportOrder order = new SportOrder(memberid, membername, ott, ""+memberid);
		order.setStatus(OrderConstant.STATUS_NEW);
		order.setPartnerid(partnerid);
		String odertitle = ott.getSportname() + ott.getItemname() + "场次预订";
		Timestamp validtime = DateUtil.addMinute(cur, OpenTimeTableConstant.MAX_MINUTS_TICKETS);
		order.setTradeNo(PayUtil.getSportTradeNo());
		order.setOrdertitle(odertitle);
		order.setMobile(mobile);
		order.setValidtime(validtime);
		String randomNum = nextRandomNum(cur, 8, "0");
		order.setCheckpass(randomNum);
		Integer unitprice = oti.getPrice();
		Integer total = unitprice * quantity;
		Integer costprice = oti.getCostprice();
		Integer sumcost = costprice * quantity;
		if(oti.hasUnitTime() && oti.hasPeriod()){
			int tmpHour = time/oti.getUnitMinute();
			total = tmpHour * total;
			sumcost = tmpHour * sumcost;
		}
		if(total<=0) return ErrorCode.getFailure("价格有错误，请选择其它场地");
		order.setUnitprice(oti.getPrice());
		order.setCostprice(costprice);
		order.setQuantity(quantity);
		order.setTotalfee(total);
		order.setUpdatetime(cur);
		order.setModifytime(cur);
		order.setTotalcost(sumcost);
		String endtime = setOrderDescription(order, ott, oti, starttime, time);
		setOrderOtherinfo(order, sumcost); 	//记录总成本
		order.setCitycode(ott.getCitycode());
		baseDao.saveObject(order);
		if(ott.getRemoteid() != null){
			CusOrder cusOrder = new CusOrder();
			cusOrder.setOrderid(order.getId());
			baseDao.saveObject(cusOrder);
		}
		newBuyItem(ott, oti, quantity);
		createSellTimeTable(order, oti, starttime,endtime, time);
		operationService.updateLastOperation("S" + memberid + StringUtil.md5(String.valueOf(memberid)), order.getTradeNo(), order.getAddtime(), ott.getPlayTimeByHour(starttime), "sport");
		return ErrorCode.getSuccessReturn(order);
	}
	public Integer getSellTimeTableCount(Long otiid, String starttime){
		String hql = "select count(*) from SellTimeTable stt where stt.otiid=? and stt.starttime=? and (stt.status=? or stt.status=? and stt.validtime>? )";
		List<Long> countList = hibernateTemplate.find(hql, otiid, starttime, SellTimeTable.STATUS_SOLD, SellTimeTable.STATUS_NEW, DateUtil.getCurFullTimestamp());
		if(countList.isEmpty()) return 0;
		return Long.valueOf(countList.get(0)).intValue();
	}
	private void createSellTimeTable(SportOrder order, OpenTimeItem oti, String starttime, String endtime, Integer time){
		SellTimeTable stt = new SellTimeTable(order.getId(), oti, order.getValidtime());
		stt.setStarttime(starttime);
		stt.setEndtime(endtime);
		stt.setSumMinute(time);
		stt.setOtiid(oti.getId());
		stt.setQuantity(order.getQuantity());
		baseDao.saveObject(stt);
	}
	private String setOrderDescription(SportOrder order, OpenTimeTable ott, OpenTimeItem oti, String starttime, Integer time){
		Map<String, String> descMap = VmUtils.readJsonToMap(order.getDescription2());
		descMap.put("运动馆名", ott.getSportname());
		descMap.put("运动项目", ott.getItemname());
		String startDate = DateUtil.format(ott.getPlaydate(),"yyyy-MM-dd") + " " + starttime + ":00";
		descMap.put("预计到达时间", startDate);
		String otiDate = DateUtil.format(ott.getPlaydate(),"yyyy-MM-dd") + " " + oti.getHour() + " - " +
										DateUtil.format(ott.getPlaydate(),"yyyy-MM-dd") + " " + oti.getEndhour();
		descMap.put("时间", otiDate);
		if((ott.hasPeriod() || ott.hasInning()) && oti.hasUnitTime()){
			descMap.put("时长", time + "分钟");
		}else{
			descMap.put("时长", "不限时");
		}
		Timestamp curTime = DateUtil.parseTimestamp(startDate);
		String endtime = "";
		String remark = "";
		if(oti.hasUnitTime()){
			Timestamp endTime = DateUtil.addMinute(curTime, time);
			endtime = DateUtil.format(endTime, "HH:mm");
		}else{
			Timestamp endTime = ott.getPlayTimeByHour(oti.getEndhour());
			endtime = DateUtil.format(endTime, "HH:mm");
		}
		if(oti.hasPeriod()){
			remark = starttime + "-" + endtime + " " + order.getQuantity() +"人  "+ order.getTotalfee()+"元";
		}else if(oti.hasInning()){
			remark = starttime + "-" + endtime + " " + order.getQuantity() +"局 "+ order.getTotalfee()+"元";
		}
		descMap.put("详细", remark);
		order.setDescription2(JsonUtils.writeMapToJson(descMap));
		return endtime;
	}
	private void setOrderDescription(SportOrder order, OpenTimeTable ott, MemberCardInfo card, List<OpenTimeItem> otiList, String minhour){
		Map<String, String> descMap = VmUtils.readJsonToMap(order.getDescription2());
		descMap.put("运动馆名", ott.getSportname());
		descMap.put("运动项目", ott.getItemname());
		descMap.put("时间", DateUtil.format(ott.getPlaydate(),"yyyy-MM-dd") + " " + minhour + ":00");
		descMap.put("详细", SportOrderHelper.getFieldText(otiList));
		if(card!=null){
			descMap.put(MemberCardConstant.VIPCARD, card.getMemberCardCode());
		}
		order.setDescription2(JsonUtils.writeMapToJson(descMap));
	}
	private void setOrderOtherinfo(SportOrder order, Integer sumcost) {
		Map<String, String> infoMap = VmUtils.readJsonToMap(order.getOtherinfo());
		infoMap.put("sumcost", sumcost+"");
		order.setOtherinfo(JsonUtils.writeMapToJson(infoMap));
	}
	@Override
	public OrderContainer processOrderPay(SportOrder order, OpenTimeTable ott) throws OrderException{
		return processOrderPayInternal(order);
	}
	@Override
	public void processSportOrder(SportOrder order, OpenTimeTable ott, List<OpenTimeItem> otiList) throws OrderException{
		if(order.isPaidUnfix()){
			Timestamp cur = new Timestamp(System.currentTimeMillis());
			if(ott.hasPeriod()||ott.hasInning()){
				SellTimeTable stt = baseDao.getObject(SellTimeTable.class, order.getId());
				stt.setStatus(SellTimeTable.STATUS_SOLD);
				baseDao.saveObject(stt);
			}else{
				//再次检查是否有抢位情况（场地冲突）
				String msg = "";
				for(OpenTimeItem oti: otiList){
					if(!oti.getMemberid().equals(order.getMemberid()) && !oti.hasAvailable()){
						msg += "[" + oti.getHour() + "]";
					}
				}
				if(StringUtils.isNotBlank(msg)) throw new OrderException(ApiConstant.CODE_DATA_ERROR, "时间" + msg + "被占用！");
				for(OpenTimeItem oti : otiList){
					oti.setStatus(OpenTimeItemConstant.STATUS_SOLD);
					oti.setValidtime(order.getValidtime());
					oti.setMemberid(order.getMemberid());
					oti.setValidtime(order.getValidtime());
					baseDao.saveObject(oti);
				}
				OpenTimeSale ots = baseDao.getObjectByUkey(OpenTimeSale.class, "orderid", order.getId());
				if(ots != null){
					ots.setLockStatus(OpenTimeTableConstant.SALE_STATUS_SUCCESS_PAID);
					baseDao.saveObject(ots);
				}
			}
			ott.addSales(order.getQuantity());
			order.setUpdatetime(cur);
			order.setModifytime(cur);
			order.setValidtime(DateUtil.addDay(cur, 180));
			order.setStatus(OrderConstant.STATUS_PAID_SUCCESS);
			order.setSettle(OrderConstant.SETTLE_Y);
			baseDao.saveObject(ott);
			baseDao.saveObject(order);
			OrderExtra orderExtra = processOrderExtra(order);
			SportProfile sp = baseDao.getObject(SportProfile.class, ott.getSportid());
			sp.setPretype(sp.getPretype());
			if(sp.hasPretype(SportProfile.PRETYPE_ENTRUST)){
				orderExtra.setInvoice(OrderExtraConstant.INVOICE_F);
				baseDao.saveObject(orderExtra);
			}
		}else{
			throw new OrderException(ApiConstant.CODE_DATA_ERROR, "订单状态不正确！");
		}
	}
	@Override
	public List<OpenTimeTable> getOttList(Long sportid, Long itemid, Date from, Date to, boolean open){
		DetachedCriteria query = DetachedCriteria.forClass(OpenTimeTable.class);
		if(sportid != null) query.add(Restrictions.eq("sportid", sportid));
		if(itemid != null) query.add(Restrictions.eq("itemid", itemid));
		if(from != null) query.add(Restrictions.ge("playdate", from));
		if(to != null) query.add(Restrictions.le("playdate", to));
		query.add(Restrictions.ne("status", OpenTimeTableConstant.STATUS_DISCARD));
		if(open) query.add(Restrictions.eq("status", OpenTimeTableConstant.STATUS_BOOK));
		query.addOrder(Order.asc("itemid"));
		query.addOrder(Order.asc("playdate"));
		query.addOrder(Order.asc("id"));
		List<OpenTimeTable> result = hibernateTemplate.findByCriteria(query);
		return result;
	}
	@Override
	public Integer getOttCount(Long sportid, Long itemid, Date from, Date to, boolean open){
		DetachedCriteria query = DetachedCriteria.forClass(OpenTimeTable.class);
		if(sportid != null) query.add(Restrictions.eq("sportid", sportid));
		if(itemid != null) query.add(Restrictions.eq("itemid", itemid));
		if(from != null) query.add(Restrictions.ge("playdate", from));
		if(to != null) query.add(Restrictions.le("playdate", to));
		query.add(Restrictions.ne("status", OpenTimeTableConstant.STATUS_DISCARD));
		if(open) query.add(Restrictions.eq("status", OpenTimeTableConstant.STATUS_BOOK));
		query.setProjection(Projections.rowCount());
		List<Long> result = hibernateTemplate.findByCriteria(query);
		return Integer.valueOf(result.get(0)+"");
	}
	@Override
	public List<SportItem> getOpenSportItemList(Long sportid, Date from, Date to, boolean open){
		DetachedCriteria query = DetachedCriteria.forClass(OpenTimeTable.class);
		if(sportid != null) query.add(Restrictions.eq("sportid", sportid));
		if(from != null) query.add(Restrictions.ge("playdate", from));
		if(to != null) query.add(Restrictions.le("playdate", to));
		query.add(Restrictions.ne("status", OpenTimeTableConstant.STATUS_DISCARD));
		if(open) query.add(Restrictions.eq("status", OpenTimeTableConstant.STATUS_BOOK));
		query.setProjection(Projections.distinct(Projections.property("itemid")));
		List<Long> itemidList = hibernateTemplate.findByCriteria(query);
		List<SportItem> itemList = baseDao.getObjectList(SportItem.class, itemidList);
		return itemList;
	}
	@Override
	public OpenTimeTable getOtt(Long sportid, Long itemid, Date playdate){
		DetachedCriteria query = DetachedCriteria.forClass(OpenTimeTable.class);
		query.add(Restrictions.eq("sportid", sportid));
		query.add(Restrictions.eq("itemid", itemid));
		query.add(Restrictions.eq("playdate", playdate));
		query.add(Restrictions.ne("status", OpenTimeTableConstant.STATUS_DISCARD));
		List<OpenTimeTable> ottList = hibernateTemplate.findByCriteria(query, 0, 1);
		if(ottList.isEmpty()) return null;
		return ottList.get(0);
	}
	@Override
	public List<SportOrder> getSportOrderList(SearchOrderCommand soc) {
		return getSportOrderList(soc, 0, 500);
	}
	@Override
	public List<SportOrder> getSportOrderList(SearchOrderCommand soc, int from, int maxnum) {
		DetachedCriteria query = DetachedCriteria.forClass(SportOrder.class);
		if(StringUtils.isNotBlank(soc.getMobile())) query.add(Restrictions.eq("mobile", soc.getMobile()));
		if(StringUtils.isNotBlank(soc.getTradeNo())) query.add(Restrictions.eq("tradeNo", soc.getTradeNo()));
		if(soc.getMinute()!=null){
			Timestamp fromtime = DateUtil.addMinute(new Timestamp(System.currentTimeMillis()), -soc.getMinute());
			query.add(Restrictions.ge("addtime", fromtime));
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
		if(soc.getMemberid()!=null) query.add(Restrictions.eq("memberid", soc.getMemberid()));
		if(soc.getSportid()!=null) query.add(Restrictions.eq("sportid", soc.getSportid()));
		if(soc.getItemid()!=null)query.add(Restrictions.eq("itemid", soc.getItemid()));
		if(soc.getOrderid()!=null) query.add(Restrictions.eq("id", soc.getOrderid()));
		if(soc.getOttid()!=null) query.add(Restrictions.eq("ottid", soc.getOttid()));
		query.addOrder(Order.desc("addtime"));
		List<SportOrder> orderList = hibernateTemplate.findByCriteria(query,from,maxnum);
		return orderList;
	}
	@Override
	public List<SportOrder> getValidSportOrderList(SearchOrderCommand soc, int from, int maxnum) {
		DetachedCriteria query = DetachedCriteria.forClass(SportOrder.class);
		if(StringUtils.isNotBlank(soc.getMobile())) query.add(Restrictions.eq("mobile", soc.getMobile()));
		if(StringUtils.isNotBlank(soc.getTradeNo())) query.add(Restrictions.eq("tradeNo", soc.getTradeNo()));
		if(soc.getMinute()!=null){
			Timestamp fromtime = DateUtil.addMinute(new Timestamp(System.currentTimeMillis()), -soc.getMinute());
			query.add(Restrictions.ge("addtime", fromtime));
		}
		query.add(Restrictions.not(Restrictions.like("status", OrderConstant.STATUS_CANCEL, MatchMode.START)));
		if(soc.getMemberid()!=null) query.add(Restrictions.eq("memberid", soc.getMemberid()));
		if(soc.getSportid()!=null) query.add(Restrictions.eq("sportid", soc.getSportid()));
		if(soc.getItemid()!=null)query.add(Restrictions.eq("itemid", soc.getItemid()));
		if(soc.getOrderid()!=null) query.add(Restrictions.eq("id", soc.getOrderid()));
		if(soc.getOttid()!=null) query.add(Restrictions.eq("ottid", soc.getOttid()));
		query.addOrder(Order.desc("addtime"));
		List<SportOrder> orderList = hibernateTemplate.findByCriteria(query,from,maxnum);
		return orderList;
	}
	@Override
	public List<SportField> getSportFieldList(Long sportid, Long itemid){
		String qry = "from SportField s where s.sportid=? and s.itemid=? order by s.ordernum asc";
		return hibernateTemplate.find(qry, sportid, itemid);
	}
	@Override
	public List<SportField> getSportFieldList(Long ottid){
		String qry = "select distinct oti.fieldid from OpenTimeItem oti where oti.ottid=? and oti.status!=? and exists(select f.id from SportField f where f.id=oti.fieldid and f.status=?)";
		List<Long> fieldidList = hibernateTemplate.find(qry, ottid, OpenTimeItemConstant.STATUS_DELETE, "Y");
		List<SportField> fieldList = baseDao.getObjectList(SportField.class, fieldidList);
		Collections.sort(fieldList, new PropertyComparator("ordernum", false, true));
		return fieldList;
	}
	@Override
	public List<SportField> getAllSportFieldList(Long ottid){
		String qry = "select distinct oti.fieldid from OpenTimeItem oti where oti.ottid=?";
		List<Long> fieldidList = hibernateTemplate.find(qry, ottid);
		List<SportField> fieldList = baseDao.getObjectList(SportField.class, fieldidList);
		Collections.sort(fieldList, new PropertyComparator("ordernum", false, true));
		return fieldList;
	}
	@Override
	public List<String> getPlayHourList(Long ottid, String status) {
		if(StringUtils.isNotBlank(status)){
			String qry = "select distinct oti.hour from OpenTimeItem oti where oti.ottid=? and oti.status<>? order by oti.hour";
			List<String> list = hibernateTemplate.find(qry, ottid, status);
			return list;
		}
		String qry = "select distinct oti.hour from OpenTimeItem oti where oti.ottid=? order by oti.hour";
		List<String> list = hibernateTemplate.find(qry, ottid);
		return list;
	}
	@Override
	public List<Long> getMemberidListBySportid(Long sportid, Timestamp addtime, int from, int maxnum) {
		DetachedCriteria qry = DetachedCriteria.forClass(SportOrder.class);
		qry.add(Restrictions.eq("sportid", sportid));
		qry.add(Restrictions.ne("status", OrderConstant.STATUS_PAID_SUCCESS));
		qry.add(Restrictions.gt("addtime", addtime));
		qry.addOrder(Order.desc("addtime"));
		List<SportOrder> orderList = hibernateTemplate.findByCriteria(qry, 0, maxnum);
		List<Long> memberidList = BeanUtil.getBeanPropertyList(orderList, Long.class, "memberid", true);
		return memberidList;
	}
	@Override
	public List<SportOrder> getOrderListByMemberid(Long memberid){
		DetachedCriteria qry = DetachedCriteria.forClass(SportOrder.class);
		qry.add(Restrictions.eq("memberid", memberid));
		qry.add(Restrictions.eq("status", OrderConstant.STATUS_PAID_SUCCESS));
		List<SportOrder> orderList = hibernateTemplate.findByCriteria(qry);
		return orderList;
	}
	private ErrorCode getDiscount(SportOrder order, OpenTimeTable table, ElecCard card, Long memberid){
		//1、判断卡是否有效
		if(!card.available()) return ErrorCode.getFailure("此兑换券已经用完或失效！");
		if(!card.validTag(PayConstant.APPLY_TAG_SPORT)) return ErrorCode.getFailure("此卡不能在运动版块使用");
		if(card.getPossessor()!=null && !card.getPossessor().equals(memberid)){
			return ErrorCode.getFailure("不能用别人的兑换券！");
		}
		if(StringUtils.isNotBlank(card.getWeektype())){
			String week = ""+DateUtil.getWeek(table.getPlaydate());
			if(card.getWeektype().indexOf(week) < 0){ 
				return ErrorCode.getFailure("此兑换券只能在周" + card.getWeektype() + "使用！");
			}
		}
		if(StringUtils.isNotBlank(card.getValidcinema())){
			List<Long> cidList = BeanUtil.getIdList(card.getValidcinema(), ",");
			if(!cidList.contains(order.getSportid())){
				return ErrorCode.getFailure("此兑换券不能在此场馆使用！");
			}
		}
		if(!card.isUseCurTime()){//时间段限制
			String opentime = card.getEbatch().getAddtime1();
			String closetime = card.getEbatch().getAddtime2();
			return ErrorCode.getFailure("此兑换券只能在" + opentime + "至" +  closetime + "时段内使用！");
		}
		
		if(StringUtils.isNotBlank(card.getValidmovie())){
			List<Long> cidList = BeanUtil.getIdList(card.getValidmovie(), ",");
			if(!cidList.contains(order.getItemid())){
				return ErrorCode.getFailure("此运动项目不能使用此兑换券！");
			}
		}
		if(StringUtils.isNotBlank(card.getValiditem())){
			List<Long> cidList = BeanUtil.getIdList(card.getValiditem(), ",");
			if(!cidList.contains(order.getOttid())){
				return ErrorCode.getFailure("本场次不能使用此兑换券！");
			}
		}
		List<Discount> discountList = paymentService.getOrderDiscountList(order);
		if("D".equals(card.getCardtype()) && discountList.size() > 0){
			return ErrorCode.getFailure("此类券不能重复使用或与其他优惠方式共用！");
		}
		for(Discount discount: discountList){
			if(discount.getRelatedid().equals(card.getId()))
				return ErrorCode.getFailure("此兑换券已使用！");
			if(("C".equals(card.getCardtype()) || "A".equals(card.getCardtype())) 
					&& !card.getCardtype().equals(discount.getCardtype())){
				return ErrorCode.getFailure("此兑换券不能与其他优惠方式共用！");
			}
		}
		int amount = 0; Long goodsid = null;
		String description = "";
		OpenTimeItem item = null;
		if(table.hasField()){
			List<OpenTimeItem> otiList = getMyOtiList(order.getId());
			item = SportOrderHelper.getMaxOpenTimeItem(otiList, discountList, card.getEbatch().getOpentime(), card.getEbatch().getClosetime());
			if(item==null) return ErrorCode.getFailure("已经没有场地可以使用兑换券！");
		}else{
			SellTimeTable stt = baseDao.getObject(SellTimeTable.class, order.getId());
			int num = stt.getSumMinute()/stt.getUnitMinute();
			if( num == 0) num = 1;
			if(discountList.size()>= num * order.getQuantity()) return ErrorCode.getFailure("已经没有时间段可以使用兑换券！");
			item = baseDao.getObject(OpenTimeItem.class,  stt.getOtiid());
			if(table.hasPeriod()&&StringUtils.isNotBlank(card.getEbatch().getOpentime()) && StringUtils.isNotBlank(card.getEbatch().getClosetime())){
				if(card.getEbatch().getClosetime().compareTo(StringUtils.replace(item.getEndhour(),":",""))<=0 || card.getEbatch().getOpentime().compareTo(StringUtils.replace(item.getHour(),":",""))>0)
					return ErrorCode.getFailure("此兑换券不能在该时间段使用！");
			}
		}
		if(card.getEbatch().getCardtype().equals(PayConstant.CARDTYPE_C) ||
			card.getEbatch().getCardtype().equals(PayConstant.CARDTYPE_D)){
			amount = card.getEbatch().getAmount();
			description = card.getCardno() + "抵用" + amount + "元";
		}else if(card.getEbatch().getCardtype().equals(PayConstant.CARDTYPE_A)){
			Integer maxAmount = card.getEbatch().getAmount();
			if(table.hasPeriod() || table.hasInning()){
				SellTimeTable stt = baseDao.getObject(SellTimeTable.class, order.getId());
				if(maxAmount != null && stt.getPrice()>maxAmount) return ErrorCode.getFailure("兑换场次的价格高于券的使用限额，不能兑换！");
				goodsid = stt.getOtiid();
				description = card.getCardno() + "抵用" + stt.getStarttime()+ "-" + stt.getEndtime() + ",单位为" + stt.getUnitMinute() + "的一个时段";
				amount = stt.getPrice();
			}else if(table.hasField()){
				if(maxAmount != null && item.getPrice()>maxAmount) return ErrorCode.getFailure("兑换场地的价格高于券的使用限额，不能兑换！");
				goodsid = item.getId();
				description = card.getCardno() + "抵用" + item.getHour();
				amount = item.getPrice();
			}
		}else {
			return ErrorCode.getFailure("此种券不能使用！");
		}
		if(amount <= 0) return ErrorCode.getFailure("使用此兑换券得不到任何优惠，请看使用说明！");
		Discount discount = new Discount(order.getId(), PayConstant.DISCOUNT_TAG_ECARD, card.getId(), card.getCardtype());
		discount.setDescription(description);
		discount.setGoodsid(goodsid);
		discount.setBatchid(card.getEbatch().getId());
		discount.setAmount(amount);
		return ErrorCode.getSuccessReturn(discount);
	}
	@Override
	public ErrorCode usePoint(Long orderId, Long memberId, int usePoint){
		ErrorCode<String> pcode = pointService.validUsePoint(memberId);
		if(!pcode.isSuccess()) return ErrorCode.getFailure(pcode.getMsg());
		SportOrder order = baseDao.getObject(SportOrder.class, orderId);
		ErrorCode code = paymentService.validUse(order);
		if(!code.isSuccess()) return code;
		if(order.hasMemberCardPay()){
			return ErrorCode.getFailure("会员卡支付不支持使用积分！");
		}
		MemberInfo info = baseDao.getObject(MemberInfo.class, memberId);
		if(info.getPointvalue() < usePoint) return ErrorCode.getFailure("您的积分不够！");

		OpenTimeTable table = baseDao.getObject(OpenTimeTable.class, order.getOttid());
		if(table.getMaxpoint() < usePoint) return ErrorCode.getFailure("您使用的积分超出上限" + table.getMaxpoint());
		int amount = usePoint/ConfigConstant.POINT_RATIO;
		usePoint = amount * ConfigConstant.POINT_RATIO;
		if(usePoint < table.getMinpoint() || amount == 0){
			return ErrorCode.getFailure("您使用的积分少于下限" + table.getMinpoint());
		}
		List<Discount> discountList = paymentService.getOrderDiscountList(order);
		for(Discount discount: discountList){
			if(discount.getTag().equals(PayConstant.DISCOUNT_TAG_POINT))
				return ErrorCode.getFailure("您已经使用过积分，如有改变，请先取消！");
			if(PayConstant.CARDTYPE_D.equals(discount.getCardtype())){
				return ErrorCode.getFailure("积分不能和优惠券一起使用！");
			}
			if(PayConstant.CARDTYPE_PARTNER.equals(discount.getCardtype())){
				return ErrorCode.getFailure("已经使用了其他优惠，不能同时使用积分！");
			}
		}
		
		Discount discount = new Discount(order.getId(), PayConstant.DISCOUNT_TAG_POINT, memberId, PayConstant.CARDTYPE_POINT);
		discount.setDescription(usePoint + "积分抵用" + amount + "元");
		discount.setAmount(amount);
		baseDao.saveObject(discount);
		GewaOrderHelper.useDiscount(order, discountList, discount);
		baseDao.saveObject(order);
		
		return ErrorCode.SUCCESS;
	}
	@Override
	public ErrorCode<SportOrderContainer> useElecCard(Long orderId, ElecCard card, Long memberid){
		SportOrder order = baseDao.getObject(SportOrder.class, orderId);
		return useElecCard(order, card, memberid);
	}
	private ErrorCode<SportOrderContainer> useElecCard(SportOrder order, ElecCard card, Long memberid){
		if(!order.isNew()) return ErrorCode.getFailure("订单状态错误（" + order.getStatusText() + "）！");
		if(!card.getEbatch().getTag().equals("sport")) return ErrorCode.getFailure("本券不能在运动板块使用！");
		ErrorCode validCode = paymentService.validUse(order);
		if(!validCode.isSuccess()) return validCode;
		OpenTimeTable table = baseDao.getObject(OpenTimeTable.class, order.getOttid());
		if(!StringUtils.contains(table.getElecard(), card.getCardtype())){
			return ErrorCode.getFailure("此兑换券不可在本场次使用");
		}
		if(order.hasMemberCardPay()){
			return ErrorCode.getFailure("会员卡支付不支持券！");
		}
		Long batchid = card.getEbatch().getId();
		boolean isSupportCard = new PayValidHelper(VmUtils.readJsonToMap(table.getOtherinfo())).supportCard(batchid);
		if(!isSupportCard) return ErrorCode.getFailure("该场次不支持该券的使用！");
		
		ErrorCode<Discount> code = getDiscount(order, table, card, memberid);
		if(!code.isSuccess()) return ErrorCode.getFailure(code.getMsg());
		Discount discount = code.getRetval();
		baseDao.saveObject(discount);
		List<Discount> discountList = paymentService.getOrderDiscountList(order);
		GewaOrderHelper.useDiscount(order, discountList, discount);
		baseDao.saveObject(order);
		SportOrderContainer soc = new SportOrderContainer(order);
		soc.setCurUsedDiscount(discount);
		return ErrorCode.getSuccessReturn(soc);
	}
	
	
	@Override
	public List<OpenTimeItem> getMyOtiList(Long orderid) {
		String qry = "from OpenTimeItem o where o.id in(select st.otiid from SportOrder2TimeItem st where st.orderid=?)";
		List<OpenTimeItem> otiList = hibernateTemplate.find(qry, orderid);
		return otiList;
	}
    @Override
	public String getMyOtiHour(Long orderid) {
		String qry = "select min(o.hour) from OpenTimeItem o where o.id in(select st.otiid from SportOrder2TimeItem st where st.orderid=?)";
		List<String> otiList = hibernateTemplate.find(qry, orderid);
		return otiList.get(0);
	}
	
	private void useSellDeposit(SportOrder order, OpenTimeSale ots, SellDeposit deposit, Timestamp cur) throws OrderException{
		if(deposit == null) throw new OrderException(ApiConstant.CODE_SIGN_ERROR, "竞价保证金错误！");
		if(deposit.hasStatus(SellDeposit.STATUS_PAID_USE)) return;
		MemberAccount account = baseDao.getObjectByUkey(MemberAccount.class, "memberid", ots.getMemberid());
		Charge charge = baseDao.getObject(Charge.class, deposit.getChargeid());
		Adjustment adjustment = new Adjustment(account.getId(), deposit.getMemberid(), ots.getNickname(), Adjustment.CORRECT_DEPOSIT);
		adjustment.setContent("保证金扣除");
		adjustment.setAmount(deposit.getPrice());
		adjustment.setDepositcharge(deposit.getPrice());
		adjustment.setContent(charge.getTradeNo()+"保证金扣除");
		adjustment.setTradeno(charge.getTradeNo());
		adjustment.setUpdatetime(cur);
		adjustment.setClerkid(0L);
		ErrorCode code = paymentService.approveAdjustment(adjustment, account, adjustment.getClerkid());
		if(!code.isSuccess()){
			throw new OrderException(code.getErrcode(), code.getMsg());
		}
		deposit.setStatus(SellDeposit.STATUS_PAID_USE);
		baseDao.saveObject(deposit);
		dbLogger.warn("SellDeposit id:" + deposit.getId() + "," + SellDeposit.STATUS_PAID_SUCCESS + "==>" +SellDeposit.STATUS_PAID_USE);
		List<Discount> discountList = paymentService.getOrderDiscountList(order);
		Discount discount = new Discount(order.getId(), PayConstant.DISCOUNT_TAG_DEPOSIT, order.getMemberid(), PayConstant.CARDTYPE_DEPOSIT);
		discount.setDescription("保证金抵用" + deposit.getPrice() + "元");
		discount.setAmount(deposit.getPrice());
		discount.setGoodsid(deposit.getId());
		baseDao.saveObject(discount);
		GewaOrderHelper.useDiscount(order, discountList, discount);
		baseDao.saveObject(order);
	}
	
	@Override
	public ErrorCode<OrderContainer> useSpecialDiscount(Long orderId, SpecialDiscount sd, OrderCallback callback){
		SportOrder order = baseDao.getObject(SportOrder.class, orderId);
		if(sd == null) return ErrorCode.getFailure("本活动不存在");
		if(!order.sureOutPartner()){
			if(StringUtils.equals(sd.getBindmobile(), Status.Y)){
				Member member = baseDao.getObject(Member.class, order.getMemberid());
				if(!member.isBindMobile()){
					return ErrorCode.getFailure("该活动必须绑定手机才能使用！");
				}
				
				ErrorCode<String> scalper = this.scalperService.checkScalperLimited(member.getId(), member.getMobile(), sd.getId());
				if(!scalper.isSuccess()){
					dbLogger.error("orderId:" + orderId +  " memberID:" + member.getId() + " mobile:" + member.getMobile() + scalper.getMsg());
					return ErrorCode.getFailure("系统繁忙，请重试!");
				}
			}
		}
		OpenTimeTable ott = baseDao.getObject(OpenTimeTable.class, order.getOttid());
		List<OpenTimeItem> otiList = getMyOtiList(order.getId());
		List<Discount> discountList = paymentService.getOrderDiscountList(order);
		Spcounter spcounter = paymentService.getSpdiscountCounter(sd);
		ErrorCode<Discount> discount = getSpdiscount(spcounter, order, otiList, discountList, ott, sd);
		if(discount.isSuccess()){
			paymentService.updateSpdiscountAddCount(sd, spcounter, order);
			baseDao.saveObject(discount.getRetval());
			GewaOrderHelper.useDiscount(order, discountList, discount.getRetval());
			if(StringUtils.isNotBlank(sd.getPaymethod())){
				String[] payList = StringUtils.split(sd.getPaymethod(), ",");
				String[] pay = StringUtils.split(payList[0], ":");
				order.setPaymethod(pay[0]);
				if(pay.length >1) order.setPaybank(pay[1]);
			}
			if(callback != null) callback.processOrder(sd, order);
			baseDao.saveObject(order);
			OrderContainer container = new SportOrderContainer(order);
			container.setDiscountList(discountList);
			container.setCurUsedDiscount(discount.getRetval());
			container.setSpdiscount(sd);
			return ErrorCode.getSuccessReturn(container);
		}
		return ErrorCode.getFailure(discount.getMsg());
	}
	private ErrorCode<Discount> getSpdiscount(Spcounter spcounter, SportOrder order, List<OpenTimeItem> otiList, List<Discount> discountList, OpenTimeTable ott, SpecialDiscount sd) {
		SpecialDiscountHelper sdh = new SportSpecialDiscountHelper(order, ott, discountList, otiList);
		List<String> limitPayList = paymentService.getLimitPayList();
		PayValidHelper pvh = new PayValidHelper(VmUtils.readJsonToMap(ott.getOtherinfo()));
		pvh.setLimitPay(limitPayList);
		ErrorCode<Integer> result = paymentService.getSpdiscountAmount(sdh, order, sd, spcounter, pvh);
		if(!result.isSuccess()) return ErrorCode.getFailure(result.getMsg());
		Discount discount = new Discount(order.getId(), PayConstant.DISCOUNT_TAG_PARTNER, sd.getId(), PayConstant.CARDTYPE_PARTNER);
		discount.setAmount(result.getRetval());
		discount.setDescription(sd.getDescription());
		return ErrorCode.getSuccessReturn(discount);
	}

	@Override
	public Integer getSportOpenTimeTableCount(Long sportid) {
		Integer week=DateUtil.getWeek(new Date());
		DetachedCriteria query = DetachedCriteria.forClass(OpenTimeTable.class,"s");
		query.add(Restrictions.eq("s.status",Status.Y));
		query.add(Restrictions.eq("s.rstatus",Status.Y));
		query.add(Restrictions.ge("s.playdate", DateUtil.getBeginTimestamp(DateUtil.addDay(new Date(), -(week-1)))));
		query.add(Restrictions.le("s.playdate", DateUtil.getBeginTimestamp(DateUtil.addDay(new Date(), 7-week))));
		query.add(Restrictions.eq("s.sportid", sportid));
		query.setProjection(Projections.rowCount());
		List result= hibernateTemplate.findByCriteria(query);
		if(result.isEmpty()) return 0;
		return Integer.parseInt("" + result.get(0));
	}
	
	@Override
	public Integer getOpenTimeItemCount(Long ottid, String status, String hour){
		DetachedCriteria query = DetachedCriteria.forClass(OpenTimeItem.class, "o");
		DetachedCriteria subquery = DetachedCriteria.forClass(SportField.class, "s");
		subquery.add(Restrictions.eqProperty("o.fieldid", "s.id"));
		subquery.add(Restrictions.eq("s.status", Status.Y));
		subquery.setProjection(Projections.property("s.id"));
		query.add(Subqueries.exists(subquery));
		query.add(Restrictions.eq("o.ottid", ottid));
		query.add(Restrictions.eq("o.status", status));
		query.add(Restrictions.gt("o.hour", hour));
		query.setProjection(Projections.rowCount());
		List result= hibernateTemplate.findByCriteria(query);
		if(result.isEmpty()) return 0;
		return Integer.parseInt("" + result.get(0));
	}
	
	private ErrorCode validateOpenTimeItem(OpenTimeTable ott, List<OpenTimeItem> otiList, Long cardid){
		Map<String, List<OpenTimeItem>> bindOtiMap = BeanUtil.groupBeanList(otiList, "bindInd");
		if(!bindOtiMap.isEmpty()){
			List<OpenTimeItem> otiAllList = openTimeTableService.getOpenItemList(ott.getId());
			Map<String, List<OpenTimeItem>> bindOtiAllMap = BeanUtil.groupBeanList(otiAllList, "bindInd");
			for (String bindKey : bindOtiMap.keySet()) {
				if(!bindKey.equals("0")){
					List<OpenTimeItem> boaList = bindOtiAllMap.get(bindKey);
					if(boaList == null || boaList.isEmpty()) return ErrorCode.getFailure("场地设置有误，请选其他场地！");
					if(!otiList.containsAll(boaList)) return ErrorCode.getFailure("必须预订同一场地的"+boaList.size()+"个连续的时间段！");
				}
			}
		}
		if(cardid==null){
			for(OpenTimeItem oti : otiList){
				if(oti.needMemberCardPay()){
					return ErrorCode.getFailure(oti.getHour()+"需要会员卡支付！");
				}
			}
		}
		return ErrorCode.SUCCESS;
	}
	@Override
	public ErrorCode<String> delOtt(Long ottid){
		String qry = "select count(*) from SportOrder s where s.ottid=?";
		List result = hibernateTemplate.find(qry, ottid);
		int count = Integer.valueOf(result.get(0)+"");
		if(count>0) return ErrorCode.getFailure("该场次已经有订单不能删除");
		OpenTimeTable ott = baseDao.getObject(OpenTimeTable.class, ottid);
		List<OpenTimeItem> otiList = openTimeTableService.getOpenItemList(ott.getId());
		baseDao.removeObject(ott);
		baseDao.removeObjectList(otiList);
		return ErrorCode.SUCCESS;
	}
	
	@Override
	public ErrorCode<SportOrder> processLastOrder(Long memberid, String ukey) {
		SportOrder lastOrder = getLastSportOrder(memberid, ukey);
		if(lastOrder==null) return ErrorCode.SUCCESS;
		if(lastOrder.getStatus().startsWith(OrderConstant.STATUS_PAID_FAILURE)){
			return ErrorCode.getFailure("您还有一个订单等待处理，订单号为" + lastOrder.getTradeNo() + "，请稍后再下新订单！");
		}
		return ErrorCode.getSuccessReturn(lastOrder);
	}
	private SportOrder getLastSportOrder(Long memberid, String ukey){
		LastOperation last = baseDao.getObject(LastOperation.class, "S" + memberid + StringUtil.md5(ukey));
		if(last==null) return null;
		SportOrder order = baseDao.getObjectByUkey(SportOrder.class, "tradeNo", last.getLastvalue());
		return order;
	}
	
	private BuyItem newBuyItem(OpenTimeTable ott, OpenTimeItem oti, final int quantity){
		BuyItem item = new BuyItem(quantity);
		Timestamp playtime = ott.getPlayTimeByHour(oti.getHour());
		String goodsname = oti.getFieldname() + oti.getPrice() + "元 [" + oti.getHour() + "-" + oti.getEndhour() +"]";
		item.setGoodsname(goodsname);
		item.setPlacetype(TagConstant.TAG_SPORT);
		item.setPlaceid(oti.getSportid());
		item.setItemid(oti.getItemid());
		item.setItemtype("sportitem");
		item.setCostprice(oti.getCostprice());
		item.setOriprice(oti.getNorprice());
		item.setUnitprice(oti.getPrice());
		item.setQuantity(quantity);
		item.setPlaytime(playtime);
		item.setRemark(oti.getRemark());
		item.setCitycode(oti.getCitycode());
		item.setRelatedid(oti.getOttid());
		item.setTag(oti.getOpenType());
		int totalcost = 0,	totalfee = 0;
		totalcost = oti.getCostprice() * quantity;
		totalfee = oti.getPrice() * quantity;
		item.setSettleid(oti.getSettleid());
		item.setTotalfee(totalfee);
		item.setTotalcost(totalcost);
		String checkpass = nextRandomNum(DateUtil.addDay(playtime, 1), 8, "0");
		item.setCheckpass(checkpass);
		Map<String, String> otherInfoMap = JsonUtils.readJsonToMap(item.getOtherinfo());
		otherInfoMap.put("upsetprice", oti.getUpsetprice()+"");
		item.setOtherinfo(JsonUtils.writeMapToJson(otherInfoMap));
		Map<String, String> descMap = new HashMap<String, String>();
		descMap.put("时间", DateUtil.format(playtime,"yyyy-MM-dd HH:mm"));
		descMap.put("场地", oti.getFieldname());
		descMap.put("otiid", oti.getId()+"");
		item.setDescription(JsonUtils.writeMapToJson(descMap));
		return item;
	}
}
