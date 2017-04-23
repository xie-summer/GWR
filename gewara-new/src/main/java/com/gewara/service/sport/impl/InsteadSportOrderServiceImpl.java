package com.gewara.service.sport.impl;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.gewara.constant.ApiConstant;
import com.gewara.constant.MemberCardConstant;
import com.gewara.constant.OpenTimeItemConstant;
import com.gewara.constant.OpenTimeTableConstant;
import com.gewara.constant.PayConstant;
import com.gewara.constant.SmsConstant;
import com.gewara.constant.Status;
import com.gewara.constant.sys.ConfigConstant;
import com.gewara.constant.sys.LogTypeConstant;
import com.gewara.constant.ticket.OrderConstant;
import com.gewara.helper.SportOrderHelper;
import com.gewara.helper.order.GewaOrderHelper;
import com.gewara.model.acl.User;
import com.gewara.model.common.GewaConfig;
import com.gewara.model.pay.BuyItem;
import com.gewara.model.pay.Discount;
import com.gewara.model.pay.ElecCard;
import com.gewara.model.pay.OtherFeeDetail;
import com.gewara.model.pay.SMSRecord;
import com.gewara.model.pay.SportOrder;
import com.gewara.model.sport.CusOrder;
import com.gewara.model.sport.MemberCardInfo;
import com.gewara.model.sport.OpenTimeItem;
import com.gewara.model.sport.OpenTimeSale;
import com.gewara.model.sport.OpenTimeTable;
import com.gewara.model.sport.SellTimeTable;
import com.gewara.model.sport.Sport2Item;
import com.gewara.model.sport.SportOrder2TimeItem;
import com.gewara.service.MessageService;
import com.gewara.service.OrderException;
import com.gewara.service.order.impl.GewaOrderServiceImpl;
import com.gewara.service.partner.PartnerSynchService;
import com.gewara.service.sport.InsteadSportOrderService;
import com.gewara.service.sport.OpenTimeTableService;
import com.gewara.service.sport.SportOrderService;
import com.gewara.service.sport.SportService;
import com.gewara.support.ErrorCode;
import com.gewara.untrans.JmsService;
import com.gewara.untrans.UntransService;
import com.gewara.untrans.sport.RemoteSportService;
import com.gewara.untrans.sport.SportUntransService;
import com.gewara.untrans.ticket.SpecialDiscountService;
import com.gewara.util.BeanUtil;
import com.gewara.util.DateUtil;
import com.gewara.util.JsonUtils;
import com.gewara.util.StringUtil;
import com.gewara.util.VmUtils;

@Service("insteadSportOrderService")
public class InsteadSportOrderServiceImpl extends GewaOrderServiceImpl implements InsteadSportOrderService {
	public static final String PROCESS_ORDER = "processOrder";
	public static final int PROCESS_INTERVAL = 120;
	@Autowired@Qualifier("sportService")
	public SportService sportService;
	@Autowired@Qualifier("openTimeTableService")
	private OpenTimeTableService openTimeTableService;
	@Autowired
	private RemoteSportService remoteSportService;
	@Autowired@Qualifier("sportUntransService")
	private SportUntransService sportUntransService;
	@Autowired@Qualifier("sportOrderService")
	private SportOrderService sportOrderService;
	@Autowired@Qualifier("specialDiscountService")
	protected SpecialDiscountService specialDiscountService;
	@Autowired@Qualifier("partnerSynchService")
	protected PartnerSynchService partnerSynchService;
	@Autowired@Qualifier("jmsService")
	protected JmsService jmsService;
	@Autowired@Qualifier("messageService")
	protected MessageService messageService;
	@Autowired@Qualifier("untransService")
	protected UntransService untransService;

	@Override
	public SportOrder changeSportOrderByField(OpenTimeTable ott,
			SportOrder oldSportOrder, String fields, User user)
			throws OrderException {
		GewaConfig gewaConfig = baseDao.getObject(GewaConfig.class, ConfigConstant.CFG_PAUSE_SPORT);
		Timestamp cur = DateUtil.getCurFullTimestamp();
		Timestamp pause = Timestamp.valueOf(gewaConfig.getContent());
		if(cur.before(pause)){
			throw new OrderException(ApiConstant.CODE_SIGN_ERROR, "暂停售票至" + DateUtil.format(pause, "HH:mm"));
		}
		if(ott == null) {
			throw new OrderException(ApiConstant.CODE_SIGN_ERROR, "该场次不存在！");
		}
		List<Long> fieldidList = BeanUtil.getIdList(fields, ",");
		if(fieldidList.size()==0) {
			throw new OrderException(ApiConstant.CODE_SIGN_ERROR, "请选择场地及时间！");
		}
		if(fieldidList.size()>4) {
			throw new OrderException(ApiConstant.CODE_SIGN_ERROR, "每次最多选4个时间段！");
		}
		//获取改场馆的限制时间 cpf
		Sport2Item sport2Item = sportService.getSport2Item(ott.getSportid(), ott.getItemid());
		if(!sport2Item.isOpen()) {
			throw new OrderException(ApiConstant.CODE_SIGN_ERROR, "请在开放时间内进行预订！");
		}
		//获取改场馆的限制时间 cpf
		if(!ott.isBooking()) {
			throw new OrderException(ApiConstant.CODE_SIGN_ERROR, "暂不接受预定！");
		}
		if(!ott.hasField()) {
			throw new OrderException(ApiConstant.CODE_SIGN_ERROR, "非法错误！");
		}
		if(ott.getPlaydate().compareTo(DateUtil.getBeginningTimeOfDay(new Date()))==0) { //时间过时
			Integer limitMinutes = sport2Item.getLimitminutes();
			for(Long id : fieldidList ){
				OpenTimeItem item = baseDao.getObject(OpenTimeItem.class, id);
				if(item.getHour().compareTo(item.gainZhour(limitMinutes))<0) {
					throw new OrderException(ApiConstant.CODE_SIGN_ERROR, "已过期不能购买！");
				}
			}
		}
		Set<Long> fieldidList2 = new LinkedHashSet<Long>(fieldidList);
		List<OpenTimeItem> otiList = baseDao.getObjectList(OpenTimeItem.class, fieldidList2);
		ErrorCode code = validateFieldLock(otiList);
		if(!code.isSuccess()) {
			throw new OrderException(ApiConstant.CODE_SIGN_ERROR, code.getMsg());
		}
		Map<String, List<OpenTimeItem>> saleMap = BeanUtil.groupBeanList(otiList, "itemtype");
		if(!CollectionUtils.isEmpty(saleMap.get(OpenTimeTableConstant.ITEM_TYPE_VIE))){
			List<Long> otsIdList = BeanUtil.getBeanPropertyList(saleMap.get(OpenTimeTableConstant.ITEM_TYPE_VIE), "otsid", true);
			List<OpenTimeSale> saleList = baseDao.getObjectList(OpenTimeSale.class, otsIdList);
			for (OpenTimeSale sale : saleList) {
				if(sale.hasBooking()) {
					throw new OrderException(ApiConstant.CODE_SIGN_ERROR, "竞价场次，非法购票！");
				}
			}
		}
		ErrorCode code2 = validateOpenTimeItem(ott, otiList);
		if(!code2.isSuccess()) {
			throw new OrderException(ApiConstant.CODE_SIGN_ERROR, code2.getMsg());
		}
		if(fieldidList.size() != oldSportOrder.getQuantity()){
			throw new OrderException(ApiConstant.CODE_SIGN_ERROR, "场地数与订单数不相等！");
		}
		return this.changeSportOrderByField(ott, oldSportOrder, otiList, user);
	}
	
	@Override
	public SportOrder changeSportOrderByPeriod(OpenTimeTable ott, SportOrder oldSportOrder,
			OpenTimeItem oti, String starttime, Integer quantity, Integer time, User user)
			throws OrderException {
		if((!ott.hasInning() || !oti.hasInning()) && (!ott.hasPeriod() || !oti.hasPeriod())) {
			throw new OrderException(ApiConstant.CODE_SIGN_ERROR, "场次或场地数据错误！");
		}
		Timestamp cur = DateUtil.getCurFullTimestamp();
		Date curDate = DateUtil.getDateFromTimestamp(cur);
		Sport2Item sport2Item = sportService.getSport2Item(ott.getSportid(), ott.getItemid());
		Date validDate = DateUtil.addMinute(curDate, sport2Item.getLimitminutes());
		Date curDate2 = DateUtil.getBeginningTimeOfDay(curDate);
		String hour = DateUtil.format(validDate, "HH:mm");
		if(DateUtil.getDiffDay(ott.getPlaydate(), curDate2) == 0){
			if(oti.getEndhour().compareTo(hour)<=0) {
				throw new OrderException(ApiConstant.CODE_SIGN_ERROR, "已过期不能购买！");
			}
		}
		if(StringUtils.isBlank(starttime)) {
			throw new OrderException(ApiConstant.CODE_SIGN_ERROR, "开始时间段不能为空！");
		}
		if(quantity == null) {
			throw new OrderException(ApiConstant.CODE_SIGN_ERROR, "选择人数不能为空！");
		}
		if(quantity <1 ||quantity >4) {
			throw new OrderException(ApiConstant.CODE_SIGN_ERROR, "预订人数只能是1-4个！");
		}
		List<String> timeList = SportOrderHelper.getStarttimeList(ott.getPlaydate(), oti);
		if(!timeList.contains(starttime)) {
			throw new OrderException(ApiConstant.CODE_SIGN_ERROR, "入场时间不在该场次时间段以内！");
		}
		if(ott.hasPeriod()){
			if(time == null) {
				throw new OrderException(ApiConstant.CODE_SIGN_ERROR, "时长不能为空！");
			}
			List<Integer> periodList = SportOrderHelper.getPeriodList(ott.getPlaydate(), oti);
			if(!periodList.contains(time)) {
				throw new OrderException(ApiConstant.CODE_SIGN_ERROR, "时长错误！");
			}
		}else{
			time = oti.getUnitMinute();
		}
		int count = sportOrderService.getSellTimeTableCount(oti.getId(), starttime);
		if(quantity+ count > oti.getQuantity()) {
			throw new OrderException(ApiConstant.CODE_SIGN_ERROR, "可预订人数已满！");
		}
		return this.updateSportOrderByPeriod(ott, oldSportOrder, oti, starttime, quantity, time, user);
	}
	
	private SportOrder changeSportOrderByField(OpenTimeTable ott,
			SportOrder oldSportOrder, List<OpenTimeItem> otiList, User user)
			throws OrderException {
		SportOrder sportOrder = new SportOrder();
		try {
			PropertyUtils.copyProperties(sportOrder, oldSportOrder);
		} catch (Exception e) {
			dbLogger.error("", e);
		}
		Timestamp invalid = new Timestamp(System.currentTimeMillis() - 1000);
		Timestamp cur = new Timestamp(System.currentTimeMillis());
		Timestamp validtime = DateUtil.addMinute(cur, OpenTimeTableConstant.MAX_MINUTS_TICKETS);
		//先将老订单号改掉
		oldSportOrder.setTradeNo(oldSportOrder.getTradeNo() + StringUtil.getRandomString(3, true, true, false) + "X");
		oldSportOrder.setStatus(OrderConstant.STATUS_SYS_CANCEL);
		oldSportOrder.setGewapaid(0);
		oldSportOrder.setAlipaid(0);
		oldSportOrder.setValidtime(invalid);
		
		List<OpenTimeItem> oldItemList = sportService.getOrderPlayItemList(oldSportOrder.getId());
		oldItemList.removeAll(otiList);
		for(OpenTimeItem item: oldItemList){
			item.setValidtime(invalid);
			item.setStatus(OpenTimeItemConstant.STATUS_LOCKR);
			baseDao.saveObject(item);
		}
		if(!sportOrder.getOttid().equals(ott.getId())){
			oldSportOrder.addChangehis(OrderConstant.CHANGEHIS_KEY_MPITO, ""+ott.getId());
		}
		baseDao.saveObject(oldSportOrder);
		//强制先执行更新老订单
		hibernateTemplate.flush();

		//取消发短信和发邮件
		List<SMSRecord> smsList = baseDao.getObjectListByField(SMSRecord.class, "tradeNo", oldSportOrder.getTradeNo());
		List<SMSRecord> delSmsList = new ArrayList<SMSRecord>();
		for (SMSRecord smsRecord : smsList) {
			smsRecord.setTradeNo(oldSportOrder.getTradeNo());
			if(!StringUtils.contains(smsRecord.getStatus(), Status.Y)){
				smsRecord.setStatus( SmsConstant.STATUS_D + smsRecord.getStatus());
				delSmsList.add(smsRecord);
				dbLogger.warnWithType(LogTypeConstant.LOG_TYPE_ORDER_PAY, "订单更换后取消短信ID："+smsRecord.getId()+"  订单号："+smsRecord.getTradeNo());
			}
		}
		baseDao.saveObjectList(delSmsList);
		Timestamp t = DateUtil.addDay(new Timestamp(ott.getPlaydate().getTime()),1);
		String randomNum = nextRandomNum(t, 8, "0");
		sportOrder.setId(null);
		sportOrder.setValidtime(validtime);
		sportOrder.setAddtime(cur);
		sportOrder.setUpdatetime(cur);
		sportOrder.setModifytime(cur);
		sportOrder.addChangehis(OrderConstant.CHANGEHIS_KEY_CHANGESEAT, oldSportOrder.getTradeNo());
		sportOrder.setCheckpass(randomNum);
		baseDao.saveObject(sportOrder);
		Integer total = 0;
		Integer unitprice = 0;
		Integer costprice = 0;
		Integer sumcost = 0;
		String minhour = otiList.get(0).getHour();
//		List<SportOrder2TimeItem> o2s = baseDao.getObjectListByField(SportOrder2TimeItem.class, "orderid", oldSportOrder.getId());
//		baseDao.removeObjectList(o2s);
		List<SportOrder2TimeItem> o2tList = new ArrayList<SportOrder2TimeItem>();
		for(OpenTimeItem oti : otiList){
			if(oti.getPrice()<=0) {
				throw new OrderException(ApiConstant.CODE_SIGN_ERROR, "价格有错误，请选择其它场地");
			}
			oti.setValidtime(validtime);
			oti.setRemark(StringUtils.substring("[订" + sportOrder.getMembername() +"]" + StringUtils.defaultString(oti.getRemark()), 0, 500));
			oti.setMemberid(sportOrder.getMemberid());
			total += oti.getPrice();
			unitprice = oti.getPrice();
			costprice = oti.getCostprice();
			sumcost += oti.getCostprice();
			if(oti.getHour().compareTo(minhour)<0) {
				minhour = oti.getHour();
			}
			o2tList.add(new SportOrder2TimeItem(sportOrder.getId(), oti.getId()));
		}
		if(total<=0) {
			throw new OrderException(ApiConstant.CODE_SIGN_ERROR, "价格有错误，请选择其它场地");
		}
		baseDao.saveObjectList(otiList);
		baseDao.saveObjectList(o2tList);
		sportOrder.setUnitprice(unitprice);
		sportOrder.setCostprice(costprice);
		sportOrder.setQuantity(otiList.size());
		sportOrder.setTotalfee(total);
		sportOrder.setTotalcost(sumcost);
		setOrderDescription(sportOrder, ott, null, otiList, minhour);
		setOrderOtherinfo(sportOrder, sumcost); 	//记录总成本
		sportOrder.setCitycode(ott.getCitycode());
		if(!sportOrder.getOttid().equals(ott.getId())){
			//场次变换
			sportOrder.addChangehis(OrderConstant.CHANGEHIS_KEY_MPIFROM, ""+sportOrder.getOttid());
			sportOrder.setOttid(ott.getId());
			sportOrder.setSportid(ott.getSportid());
			sportOrder.setCitycode(ott.getCitycode());
			sportOrder.setItemid(ott.getItemid());
		}
		baseDao.saveObject(sportOrder);
		if(ott.getRemoteid() != null){
			CusOrder cusOrder = new CusOrder();
			cusOrder.setOrderid(sportOrder.getId());
			baseDao.saveObject(cusOrder);
		}
		List<Discount> discountList = paymentService.getOrderDiscountList(oldSportOrder);
		if(discountList.size() > 0){
			List<Discount> newList = new ArrayList<Discount>();
			for(Discount discount: discountList){
				Discount nd = new Discount(sportOrder.getId(), discount.getTag(), 
						discount.getRelatedid(), discount.getCardtype());
				nd.setAmount(discount.getAmount());
				nd.setDescription(discount.getDescription());
				nd.setGoodsid(discount.getGoodsid());
				nd.setBatchid(discount.getBatchid());
				nd.setStatus(discount.getStatus());
				newList.add(nd);
				if(PayConstant.DISCOUNT_TAG_ECARD.equals(nd.getTag())){
					ElecCard card = baseDao.getObject(ElecCard.class, nd.getRelatedid());
					card.setOrderid(sportOrder.getId());
					baseDao.saveObject(card);
				}
				//如使用成功，要改变状态
				if(StringUtils.equals(discount.getStatus(), OrderConstant.DISCOUNT_STATUS_Y)){
					discount.setStatus(OrderConstant.DISCOUNT_STATUS_N);
					baseDao.saveObject(discount);
				}
				baseDao.saveObject(nd);
				GewaOrderHelper.useDiscount(sportOrder, newList, nd);
			}
		}
		List<BuyItem> itemList = baseDao.getObjectListByField(BuyItem.class, "orderid", oldSportOrder.getId());
		if(itemList.size() > 0){
			List<BuyItem> newItemList = new ArrayList<BuyItem>();
			for(BuyItem item: itemList){
				BuyItem newitem = new BuyItem(item);
				newitem.setOrderid(sportOrder.getId());
				baseDao.saveObject(newitem);
				newItemList.add(newitem);
			}
			GewaOrderHelper.refreshItemfee(sportOrder, newItemList);
		}
		List<OtherFeeDetail> otherFeeList = baseDao.getObjectListByField(OtherFeeDetail.class, "orderid", sportOrder.getId());
		if(!otherFeeList.isEmpty()){
			List<OtherFeeDetail> newOtherFeeList = new ArrayList<OtherFeeDetail>();
			for(OtherFeeDetail otherFee: otherFeeList){
				OtherFeeDetail newOtherFee = new OtherFeeDetail(otherFee);
				newOtherFee.setOrderid(sportOrder.getId());
				baseDao.saveObject(newOtherFee);
				newOtherFeeList.add(newOtherFee);
			}
			GewaOrderHelper.refreshOtherfee(sportOrder, newOtherFeeList);
		}
		baseDao.saveObject(sportOrder);
		sportOrder.setClerkid(user.getId());
		sportOrder.addChangehis(OrderConstant.CHANGEHIS_KEY_SUCCESSCHANGE, "true");
		sportOrder.setStatus(OrderConstant.STATUS_PAID_FAILURE);
		computeChangeOrderFee(sportOrder);
		oldSportOrder.setStatus(OrderConstant.STATUS_SYS_CHANGE_CANCEL);
		baseDao.saveObjectList(sportOrder, oldSportOrder);
		if(ott.hasRemoteOtt()){
			List<Long> remoteIdList = BeanUtil.getBeanPropertyList(otiList, "rotiid", true);
			ErrorCode<String> codeRemote = remoteSportService.lockOrder(ott, remoteIdList, OpenTimeTableConstant.ITEM_TYPE_COM);
			if(!codeRemote.isSuccess()){
				sportUntransService.cancelSportOrder(sportOrder, sportOrder.getMemberid(), "远程锁定失败取消订单");
				throw new OrderException(ApiConstant.CODE_SIGN_ERROR, codeRemote.getMsg());
			}
		}
		return sportOrder;
	}
	
	private SportOrder updateSportOrderByPeriod(OpenTimeTable ott, SportOrder oldSportOrder,
			OpenTimeItem oti, String starttime, Integer quantity, Integer time, User user)
			throws OrderException {
		if(quantity != oldSportOrder.getQuantity()){
			throw new OrderException(ApiConstant.CODE_SIGN_ERROR, "人数与订单数不相等！");
		}
		SportOrder sportOrder = new SportOrder();
		try {
			PropertyUtils.copyProperties(sportOrder, oldSportOrder);
		} catch (Exception e) {
			dbLogger.error("", e);
		}
		Timestamp invalid = new Timestamp(System.currentTimeMillis() - 1000);
		Timestamp cur = new Timestamp(System.currentTimeMillis());
		Timestamp validtime = DateUtil.addMinute(cur, OpenTimeTableConstant.MAX_MINUTS_TICKETS);
		//先将老订单号改掉
		oldSportOrder.setTradeNo(oldSportOrder.getTradeNo() + StringUtil.getRandomString(3, true, true, false) + "X");
		oldSportOrder.setStatus(OrderConstant.STATUS_SYS_CANCEL);
		oldSportOrder.setGewapaid(0);
		oldSportOrder.setAlipaid(0);
		oldSportOrder.setValidtime(invalid);
		if(!sportOrder.getOttid().equals(ott.getId())){
			oldSportOrder.addChangehis(OrderConstant.CHANGEHIS_KEY_MPITO, ""+ott.getId());
		}
		baseDao.saveObject(oldSportOrder);
		//强制先执行更新老订单
		hibernateTemplate.flush();
		Timestamp t = DateUtil.addDay(new Timestamp(ott.getPlaydate().getTime()),1);
		String randomNum = nextRandomNum(t, 8, "0");

		//取消发短信和发邮件
		List<SMSRecord> smsList = baseDao.getObjectListByField(SMSRecord.class, "tradeNo", oldSportOrder.getTradeNo());
		List<SMSRecord> delSmsList = new ArrayList<SMSRecord>();
		for (SMSRecord smsRecord : smsList) {
			smsRecord.setTradeNo(oldSportOrder.getTradeNo());
			if(!StringUtils.contains(smsRecord.getStatus(), Status.Y)){
				smsRecord.setStatus( SmsConstant.STATUS_D + smsRecord.getStatus());
				delSmsList.add(smsRecord);
				dbLogger.warnWithType(LogTypeConstant.LOG_TYPE_ORDER_PAY, "订单更换后取消短信ID："+smsRecord.getId()+"  订单号："+smsRecord.getTradeNo());
			}
		}
		baseDao.saveObjectList(delSmsList);

		Integer unitprice = oti.getPrice();
		Integer total = unitprice * quantity;
		Integer costprice = oti.getCostprice();
		Integer sumcost = costprice * quantity;
		if(oti.hasUnitTime() && oti.hasPeriod()){
			int tmpHour = time/oti.getUnitMinute();
			total = tmpHour * total;
			sumcost = tmpHour * sumcost;
		}
		if(total<=0) {
			throw new OrderException(ApiConstant.CODE_SIGN_ERROR, "价格有错误，请选择其它场地");
		}
		sportOrder.setId(null);
		sportOrder.setUnitprice(oti.getPrice());
		sportOrder.setCostprice(costprice);
		sportOrder.setQuantity(quantity);
		sportOrder.setTotalfee(total);
		sportOrder.setUpdatetime(cur);
		sportOrder.setModifytime(cur);
		sportOrder.setTotalcost(sumcost);
		String endtime = setOrderDescription(sportOrder, ott, oti, starttime, time);
		setOrderOtherinfo(sportOrder, sumcost); 	//记录总成本
		sportOrder.setValidtime(validtime);
		sportOrder.setAddtime(cur);
		sportOrder.setUpdatetime(cur);
		sportOrder.setModifytime(cur);
		sportOrder.addChangehis(OrderConstant.CHANGEHIS_KEY_CHANGESEAT, oldSportOrder.getTradeNo());
		sportOrder.setCheckpass(randomNum);
		baseDao.saveObject(sportOrder);
		if(!sportOrder.getOttid().equals(ott.getId())){
			//场次变换
			sportOrder.addChangehis(OrderConstant.CHANGEHIS_KEY_MPIFROM, ""+sportOrder.getOttid());
			sportOrder.setOttid(ott.getId());
			sportOrder.setSportid(ott.getSportid());
			sportOrder.setCitycode(ott.getCitycode());
			sportOrder.setItemid(ott.getItemid());
			baseDao.saveObject(sportOrder);
		}
		if(ott.getRemoteid() != null){
			CusOrder cusOrder = new CusOrder();
			cusOrder.setOrderid(sportOrder.getId());
			baseDao.saveObject(cusOrder);
		}
		SellTimeTable stt = baseDao.getObject(SellTimeTable.class, oldSportOrder.getId());
		if (stt != null) {
			stt.setValidtime(invalid);
			baseDao.saveObject(stt);
		}
		createSellTimeTable(sportOrder, oti, starttime, endtime, time);
		List<Discount> discountList = paymentService.getOrderDiscountList(oldSportOrder);
		if(discountList.size() > 0){
			List<Discount> newList = new ArrayList<Discount>();
			for(Discount discount: discountList){
				Discount nd = new Discount(sportOrder.getId(), discount.getTag(), 
						discount.getRelatedid(), discount.getCardtype());
				nd.setAmount(discount.getAmount());
				nd.setDescription(discount.getDescription());
				nd.setGoodsid(discount.getGoodsid());
				nd.setBatchid(discount.getBatchid());
				nd.setStatus(discount.getStatus());
				newList.add(nd);
				if(PayConstant.DISCOUNT_TAG_ECARD.equals(nd.getTag())){
					ElecCard card = baseDao.getObject(ElecCard.class, nd.getRelatedid());
					card.setOrderid(sportOrder.getId());
					baseDao.saveObject(card);
				}
				//如使用成功，要改变状态
				if(StringUtils.equals(discount.getStatus(), OrderConstant.DISCOUNT_STATUS_Y)){
					discount.setStatus(OrderConstant.DISCOUNT_STATUS_N);
					baseDao.saveObject(discount);
				}
				baseDao.saveObject(nd);
				GewaOrderHelper.useDiscount(sportOrder, newList, nd);
			}
		}
		List<BuyItem> itemList = baseDao.getObjectListByField(BuyItem.class, "orderid", oldSportOrder.getId());
		if(itemList.size() > 0){
			List<BuyItem> newItemList = new ArrayList<BuyItem>();
			for(BuyItem item: itemList){
				BuyItem newitem = new BuyItem(item);
				newitem.setOrderid(sportOrder.getId());
				baseDao.saveObject(newitem);
				newItemList.add(newitem);
			}
			GewaOrderHelper.refreshItemfee(sportOrder, newItemList);
		}
		List<OtherFeeDetail> otherFeeList = baseDao.getObjectListByField(OtherFeeDetail.class, "orderid", sportOrder.getId());
		if(!otherFeeList.isEmpty()){
			List<OtherFeeDetail> newOtherFeeList = new ArrayList<OtherFeeDetail>();
			for(OtherFeeDetail otherFee: otherFeeList){
				OtherFeeDetail newOtherFee = new OtherFeeDetail(otherFee);
				newOtherFee.setOrderid(sportOrder.getId());
				baseDao.saveObject(newOtherFee);
				newOtherFeeList.add(newOtherFee);
			}
			GewaOrderHelper.refreshOtherfee(sportOrder, newOtherFeeList);
		}
		baseDao.saveObject(sportOrder);
		sportOrder.setClerkid(user.getId());
		sportOrder.addChangehis(OrderConstant.CHANGEHIS_KEY_SUCCESSCHANGE, "true");
		sportOrder.setStatus(OrderConstant.STATUS_PAID_UNFIX);
		computeChangeOrderFee(sportOrder);
		oldSportOrder.setStatus(OrderConstant.STATUS_SYS_CHANGE_CANCEL);
		baseDao.saveObjectList(sportOrder, oldSportOrder);
		return sportOrder;
	}

	private ErrorCode validateFieldLock(List<OpenTimeItem> otiList) {
		String msg = "";
		for(OpenTimeItem oti : otiList){
			if(!oti.hasAvailable()) msg = "[" + oti.getFieldname() + oti.getHour() + "]";
		}
		if(StringUtils.isBlank(msg)) return ErrorCode.SUCCESS;
		return ErrorCode.getFailure(msg+"被占用");
	}
	
	private ErrorCode validateOpenTimeItem(OpenTimeTable ott, List<OpenTimeItem> otiList){
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
		return ErrorCode.SUCCESS;
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
	
	private void createSellTimeTable(SportOrder order, OpenTimeItem oti, String starttime, String endtime, Integer time){
		SellTimeTable stt = new SellTimeTable(order.getId(), oti, order.getValidtime());
		stt.setStarttime(starttime);
		stt.setEndtime(endtime);
		stt.setSumMinute(time);
		stt.setOtiid(oti.getId());
		stt.setQuantity(order.getQuantity());
		baseDao.saveObject(stt);
	}
}
