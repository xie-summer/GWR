package com.gewara.service.impl;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.support.PropertyComparator;
import org.springframework.stereotype.Service;

import com.gewara.constant.MemberCardConstant;
import com.gewara.constant.PayConstant;
import com.gewara.constant.SmsConstant;
import com.gewara.constant.Status;
import com.gewara.constant.TagConstant;
import com.gewara.constant.order.BuyItemConstant;
import com.gewara.constant.sys.ConfigConstant;
import com.gewara.constant.sys.ErrorCodeConstant;
import com.gewara.constant.sys.JsonDataKey;
import com.gewara.constant.sys.LogTypeConstant;
import com.gewara.constant.ticket.OrderConstant;
import com.gewara.helper.SportOrderHelper;
import com.gewara.model.agency.AgencyProfile;
import com.gewara.model.agency.TrainingGoods;
import com.gewara.model.api.ApiUser;
import com.gewara.model.common.GewaConfig;
import com.gewara.model.common.JsonData;
import com.gewara.model.common.UserOperation;
import com.gewara.model.drama.DramaOrder;
import com.gewara.model.drama.OpenDramaItem;
import com.gewara.model.drama.SellDramaSeat;
import com.gewara.model.drama.TheatreProfile;
import com.gewara.model.goods.BaseGoods;
import com.gewara.model.goods.Goods;
import com.gewara.model.goods.TicketGoods;
import com.gewara.model.movie.Cinema;
import com.gewara.model.movie.CinemaProfile;
import com.gewara.model.movie.CinemaRoom;
import com.gewara.model.movie.Movie;
import com.gewara.model.pay.BuyItem;
import com.gewara.model.pay.ElecCard;
import com.gewara.model.pay.GewaOrder;
import com.gewara.model.pay.GoodsOrder;
import com.gewara.model.pay.GymOrder;
import com.gewara.model.pay.MemberCardOrder;
import com.gewara.model.pay.OrderNote;
import com.gewara.model.pay.PubSale;
import com.gewara.model.pay.PubSaleOrder;
import com.gewara.model.pay.SMSRecord;
import com.gewara.model.pay.SportOrder;
import com.gewara.model.pay.TicketOrder;
import com.gewara.model.sport.MemberCardInfo;
import com.gewara.model.sport.MemberCardType;
import com.gewara.model.sport.OpenTimeItem;
import com.gewara.model.sport.OpenTimeTable;
import com.gewara.model.sport.SellTimeTable;
import com.gewara.model.sport.Sport;
import com.gewara.model.sport.Sport2Item;
import com.gewara.model.sport.SportItem;
import com.gewara.model.ticket.OpenPlayItem;
import com.gewara.model.ticket.SellSeat;
import com.gewara.service.MessageService;
import com.gewara.service.OperationService;
import com.gewara.service.drama.DramaOrderService;
import com.gewara.service.gewapay.ElecCardService;
import com.gewara.service.sport.SportOrderService;
import com.gewara.service.sport.SportService;
import com.gewara.support.ErrorCode;
import com.gewara.untrans.mobile.MobileService;
import com.gewara.util.DateUtil;
import com.gewara.util.JsonUtils;
import com.gewara.util.ValidateUtil;
import com.gewara.util.VmUtils;

@Service("messageService")
public class MessageServiceImpl extends BaseServiceImpl implements MessageService, InitializingBean {

	@Autowired@Qualifier("elecCardService")
	private ElecCardService elecCardService;
	public void setElecCardService(ElecCardService elecCardService) {
		this.elecCardService = elecCardService;
	}
	@Autowired@Qualifier("dramaOrderService")
	private DramaOrderService dramaOrderService;
	public void setDramaOrderService(DramaOrderService dramaOrderService) {
		this.dramaOrderService = dramaOrderService;
	}
	@Autowired@Qualifier("sportOrderService")
	private SportOrderService sportOrderService;
	public void setSportOrderService(SportOrderService sportOrderService) {
		this.sportOrderService = sportOrderService;
	}

	@Autowired@Qualifier("operationService")
	private OperationService operationService;
	public void setOperationService(OperationService operationService) {
		this.operationService = operationService;
	}
	
	@Autowired@Qualifier("sportService")
	private SportService sportService;
	public void setSportService(SportService sportService){
		this.sportService = sportService;
	}
	private Map<String, String> channelMap = new HashMap<String, String>();
	@Override
	public ErrorCode<List<SMSRecord>> addMessage(GewaOrder order) {
		if(!order.isPaidSuccess()) return ErrorCode.getFailure("未成功的订单不能增加短信！");
		if (order instanceof TicketOrder) {
			OpenPlayItem opi = baseDao.getObjectByUkey(OpenPlayItem.class, "mpid", ((TicketOrder) order).getMpid(), true);
			String query = "from SellSeat where id in (select t.seatid from Order2SellSeat t where t.orderid = ?) ";
			List<SellSeat> seatList = hibernateTemplate.find(query, order.getId());
			try{
				return addTicketOrderMessage((TicketOrder) order, seatList, opi);
			}catch(Exception e){
				dbLogger.error("", e);
			}
		}else if (order instanceof GoodsOrder){
			return addGoodsOrderMessage(order);
		}else if(order instanceof SportOrder){
			return addSportOrderMessage(order);
		}else if(order instanceof DramaOrder){
			return addDramaOrderMessage(order);
		}else if(order instanceof PubSaleOrder) {
			return addPubSaleOrderMessage(order);
		}else if(order instanceof MemberCardOrder){
			return addMemberCardOrderMsg(order);
		}
		return ErrorCodeConstant.DATEERROR;
	}

	private ErrorCode<List<SMSRecord>> addGoodsOrderMessage(GewaOrder order){
		String mobile = order.getMobile();
		GoodsOrder gorder = (GoodsOrder) order;
		Timestamp cur = new Timestamp(System.currentTimeMillis());
		BaseGoods goods = baseDao.getObject(BaseGoods.class, gorder.getGoodsid());
		SMSRecord sms = null;
		List<SMSRecord> smsList = new ArrayList<SMSRecord>();
		if(goods instanceof TicketGoods){
			List<OrderNote> orderNoteList = baseDao.getObjectListByField(OrderNote.class, "orderid", order.getId());
			for (OrderNote orderNote : orderNoteList) {
				sms = addOrderNoteSms(gorder, orderNote, cur);
				smsList.add(sms);
			}
		}else if(goods instanceof TrainingGoods){
			List<OrderNote> orderNoteList = baseDao.getObjectListByField(OrderNote.class, "orderid", order.getId());
			for (OrderNote orderNote : orderNoteList) {
				sms = addTrainingOrderSms(gorder, orderNote, cur);
				smsList.add(sms);
			}
		}else{
			Integer msgMinute = goods.getMsgMinute();
			if(StringUtils.isNotBlank(goods.getOrdermsg())){
				String content = goods.getOrdermsg().replace("quantity", ""+gorder.getQuantity());
				content = content.replace("tradeNo", order.getTradeNo().substring(order.getTradeNo().length() - 5));
				if(StringUtils.isNotBlank(gorder.getCheckpass())) content = content.replace("password", gorder.getCheckpass());
				if(gorder.getValidtime()!=null) content = content.replaceAll("time", DateUtil.format(gorder.getValidtime(), "yy年M月d日"));
				sms = new SMSRecord(gorder.getGoodsid(), gorder.getTradeNo(), mobile, 
						content, cur, goods.getTotime(), SmsConstant.SMSTYPE_NOW);
				sms = saveMessage(sms);
				smsList.add(sms);
			}
			if(StringUtils.isNotBlank(goods.getNotifymsg())){
				Timestamp validtime = null;
				if(goods.getFromvalidtime() != null){
					validtime = goods.getFromvalidtime();
				}else{
					validtime = goods.getTotime();
				}
				SMSRecord sms2 = new SMSRecord(gorder.getGoodsid(), gorder.getTradeNo(), mobile, 
						goods.getNotifymsg(), DateUtil.addMinute(validtime, -msgMinute), validtime, SmsConstant.SMSTYPE_3H);
				saveMessage(sms2);
			}
		}
		return ErrorCode.getSuccessReturn(smsList);
	}
	@Override
	public SMSRecord addTrainingOrderSms(GoodsOrder order, OrderNote orderNote, Timestamp cur){
		String mobile = order.getMobile();
		BaseGoods goods = baseDao.getObject(BaseGoods.class, orderNote.getSmallitemid());
		SMSRecord sms = null;
		if(StringUtils.isNotBlank(goods.getOrdermsg())){
			String content = goods.getOrdermsg();
			content = content.replace("agency", orderNote.getPlacename());
			content = content.replace("goods", order.getOrdertitle());
			content = content.replace("tradeNo", order.getTradeNo().substring(order.getTradeNo().length() - 6));
			if(StringUtils.isNotBlank(orderNote.getCheckpass())) content = content.replace("password", orderNote.getCheckpass());
			content = content.replace("quantity", ""+orderNote.getTicketnum());
			sms = new SMSRecord(goods.getId(), orderNote.getSerialno(), mobile, 
					content, cur, goods.getTotime(), SmsConstant.SMSTYPE_NOW);
			sms = saveMessage(sms);
			dbLogger.warn("发送培训机构短信：" + content);
			// 通知商户有用户购票成功
			if(StringUtils.isNotBlank(goods.getNotifymsg())){
				String notifymsg = goods.getNotifymsg();
				notifymsg = notifymsg.replace("goods", order.getOrdertitle());
				notifymsg = notifymsg.replaceAll("time", DateUtil.format(order.getAddtime(), "yy年M月d日"));
				notifymsg = notifymsg.replace("mobile", orderNote.getMobile());
				notifymsg = notifymsg.replace("quantity", ""+orderNote.getTicketnum());
				SMSRecord sms2 = new SMSRecord(goods.getId(), orderNote.getSerialno(), mobile, 
						notifymsg, cur, goods.getTotime(), SmsConstant.SMSTYPE_NOW);
				saveMessage(sms2);
			}
			orderNote.setMessage(content);
			orderNote.setUpdatetime(cur);
		}else{
			if(StringUtils.equals(goods.getTag(), TagConstant.TAG_AGENCY)){
				AgencyProfile profile = baseDao.getObject(AgencyProfile.class, goods.getRelatedid());
				if(StringUtils.isNotBlank(profile.getNotifymsg1())) {
					String result = profile.getNotifymsg1();
					result = result.replace("agency", orderNote.getPlacename());
					result = result.replace("goods", order.getOrdertitle());
					result = result.replace("tradeNo", order.getTradeNo().substring(order.getTradeNo().length() - 5));
					if(StringUtils.isNotBlank(orderNote.getCheckpass())) result = result.replace("password", orderNote.getCheckpass());
					result = result.replace("quantity", ""+orderNote.getTicketnum());
					sms = new SMSRecord(goods.getId(), orderNote.getSerialno(), mobile, result,
							cur, goods.getTotime(), SmsConstant.SMSTYPE_NOW);
					sms = saveMessage(sms);
					orderNote.setMessage(result);
					orderNote.setUpdatetime(cur);
					dbLogger.warn("发送培训机构短信：" + result);
					// 通知商户有用户购票成功
					if(StringUtils.isNotBlank(profile.getNotifymsg2()) && StringUtils.isNotBlank(profile.getMobiles())){
						String[] mobileList = StringUtils.split(profile.getMobiles(), ",");
						String msg2 = profile.getNotifymsg2();
						msg2 = msg2.replace("goods", order.getOrdertitle());
						msg2 = msg2.replaceAll("time", DateUtil.format(order.getAddtime(), "yy年M月d日"));
						msg2 = msg2.replace("mobile", orderNote.getMobile());
						msg2 = msg2.replace("quantity", ""+orderNote.getTicketnum());
						for(String m : mobileList){
							if(ValidateUtil.isMobile(m)){
								SMSRecord sms2  = new SMSRecord(goods.getId(), orderNote.getSerialno(), m, msg2, cur, goods.getTotime(), SmsConstant.SMSTYPE_NOW);
								saveMessage(sms2);
							}
						}
					}
				}
			}
		}
		baseDao.saveObject(orderNote);
		return sms;
	}
	
	@Override
	public SMSRecord addOrderNoteSms(DramaOrder order, OrderNote orderNote, Timestamp cur){
		String mobile = order.getMobile();
		SMSRecord sms = null;
		if(!StringUtils.equals(orderNote.getSmallitemtype(), BuyItemConstant.TAG_DRAMAPLAYITEM)){
			//TODO: 非场次数据短信????
			return sms;
		}
		OpenDramaItem item = baseDao.getObjectByUkey(OpenDramaItem.class, "dpid", orderNote.getSmallitemid());
		Integer msgMinute = item.getMsgMinute();
		if(StringUtils.isNotBlank(item.getNotifymsg1())) {
			Timestamp playtime = item.getPlaytime();
			final boolean takemethod = StringUtils.equals(orderNote.getExpress(), Status.Y);
			String result = "";
			if(takemethod){
				result = item.getNotifyRemark();
			}else{
				result = item.getNotifymsg1();
				result = StringUtils.replace(result, "gewapass", orderNote.getCheckpass());
			}
			if(StringUtils.isBlank(result)) return sms;
			result = StringUtils.replace(result, "date", DateUtil.format(playtime, "M月d日"));
			result = StringUtils.replace(result, "week", DateUtil.getCnWeek(playtime));
			result = StringUtils.replace(result, "time", DateUtil.format(playtime, "HH:mm"));
			if(item.isOpenseat()) {
				List<SellDramaSeat> seatList = dramaOrderService.getDramaOrderSeatList(order.getId());
				SellDramaSeat oseat = seatList.iterator().next();
				String seat = oseat.getSeatLabel();
				if(order.getQuantity() > 1) seat += "等" + order.getQuantity() + "张票";
				result = StringUtils.replace(result, "seat", seat);
			}else {
				result = StringUtils.replace(result, "seat", order.getQuantity() + "张票");
			}
			result = StringUtils.replace(result, "drama", item.getDramaname());
			sms = new SMSRecord(order.getDpid(), orderNote.getSerialno(), mobile, result,
					order.getAddtime(), playtime, SmsConstant.SMSTYPE_NOW);
			sms = saveMessage(sms);
			dbLogger.warn("发送话剧短信：" + result);
			// 记录观影前3个小时信息
			if(StringUtils.isNotBlank(item.getNotifymsg2())){
				String msg2 = item.getNotifymsg2();
				msg2 = msg2.replaceAll("date", DateUtil.format(playtime, "M月d日"));	
				msg2 = msg2.replaceAll("time", DateUtil.format(playtime, "HH:mm"));
				msg2 = msg2.replace("drama", item.getDramaname());
				Timestamp sendtime = DateUtil.addMinute(playtime, -msgMinute);
				if(StringUtils.equals(item.getPeriod(), Status.N)){
					Timestamp curtime = DateUtil.getCurFullTimestamp();
					if(curtime.after(playtime)){
						sendtime = DateUtil.addMinute(curtime, 10);
					}
				}
				SMSRecord mustsend = new SMSRecord(order.getDpid(), orderNote.getSerialno(), mobile, msg2, sendtime, playtime, SmsConstant.SMSTYPE_3H);
				saveMessage(mustsend);
			}
			orderNote.setMessage(result);
			orderNote.setUpdatetime(cur);
			baseDao.saveObject(orderNote);
			
			/*if(StringUtils.equals(item.getPeriod(), Status.Y)){
				//话剧开演后3小时信息
				addDramaMsg(order, item);
			}*/
		}
		return sms;
	}
	
	@Override
	public SMSRecord addOrderNoteSms(GoodsOrder order, OrderNote orderNote, Timestamp cur){
		String mobile = order.getMobile();
		BaseGoods goods = baseDao.getObject(BaseGoods.class, orderNote.getSmallitemid());
		SMSRecord sms = null;
		Integer msgMinute = goods.getMsgMinute();
		if(StringUtils.isNotBlank(goods.getOrdermsg())){
			String content = goods.getOrdermsg().replace("quantity", ""+ orderNote.getTicketnum());
			content = content.replace("tradeNo", order.getTradeNo().substring(order.getTradeNo().length() - 5));
			if(StringUtils.isNotBlank(orderNote.getCheckpass())) content = content.replace("password", orderNote.getCheckpass());
			if(orderNote.getValidtime()!=null) content = content.replaceAll("time", DateUtil.format(orderNote.getValidtime(), "yy年M月d日"));
			sms = new SMSRecord(goods.getId(), orderNote.getSerialno(), mobile, 
					content, cur, goods.getTotime(), SmsConstant.SMSTYPE_NOW);
			sms = saveMessage(sms);
			dbLogger.warn("发送话剧短信：" + content);
			if(StringUtils.isNotBlank(goods.getNotifymsg())){
				Timestamp validtime = null;
				if(goods.getFromvalidtime() != null){
					validtime = goods.getFromvalidtime();
				}else{
					validtime = goods.getTotime();
				}
				SMSRecord sms2 = new SMSRecord(goods.getId(), orderNote.getSerialno(), mobile, 
						goods.getNotifymsg(), DateUtil.addMinute(validtime, -msgMinute), validtime, SmsConstant.SMSTYPE_3H);
				saveMessage(sms2);
			}
			orderNote.setMessage(content);
			orderNote.setUpdatetime(cur);
		}else{
			if(StringUtils.equals(goods.getTag(), TagConstant.TAG_THEATRE)){
				TheatreProfile profile = baseDao.getObject(TheatreProfile.class, goods.getRelatedid());
				if(StringUtils.isNotBlank(profile.getNotifymsg1())) {
					Timestamp playtime = goods.getFromvalidtime();
					String result = profile.getNotifymsg1();
					if(goods.hasPeriod()){
						result = result.replaceAll("date", DateUtil.format(playtime, "M月d日"));
						result = result.replaceAll("week", DateUtil.getCnWeek(playtime));
						result = result.replaceAll("time", DateUtil.format(playtime, "HH:mm"));
					}else{
						result = result.replaceAll("date", goods.getGoodsname());
						result = result.replaceAll("week", "");
						result = result.replaceAll("time", "");
					}
					result = result.replace("seat", orderNote.getTicketnum() + "张票");
					result = result.replace("drama", orderNote.getItemname());
					result = result.replace("gewapass", orderNote.getCheckpass());
					sms = new SMSRecord(goods.getId(), orderNote.getSerialno(), mobile, result,
							order.getAddtime(), playtime, SmsConstant.SMSTYPE_NOW);
					sms = saveMessage(sms);
					orderNote.setMessage(result);
					orderNote.setUpdatetime(cur);
					dbLogger.warn("发送话剧短信：" + result);
					// 记录观影前3个小时信息
					if(StringUtils.isNotBlank(profile.getNotifymsg2())){
						String msg2 = profile.getNotifymsg2();
						msg2 = msg2.replaceAll("time", DateUtil.format(playtime, "HH:mm"));
						msg2 = msg2.replace("drama", orderNote.getItemname());
						Timestamp sendtime = DateUtil.addMinute(playtime, -msgMinute);
						SMSRecord sms2  = new SMSRecord(goods.getId(), orderNote.getSerialno(), mobile, msg2, sendtime, playtime, SmsConstant.SMSTYPE_3H);
						saveMessage(sms2);
					}
				}
			}
		}
		baseDao.saveObject(orderNote);
		return sms;
	}
	private ErrorCode<List<SMSRecord>> addDramaOrderMessage(GewaOrder order){
		DramaOrder dorder = (DramaOrder)order;
		List<OrderNote> noteList = baseDao.getObjectListByField(OrderNote.class, "orderid", order.getId());
		List<SMSRecord> msgList = new ArrayList<SMSRecord>();
		Timestamp cur = DateUtil.getCurFullTimestamp();
		for (OrderNote orderNote : noteList) {
			SMSRecord smsRecord = addOrderNoteSms(dorder, orderNote, cur);
			if(smsRecord != null){
				msgList.add(smsRecord);
			}
		}
		SMSRecord sms = getCreateMemberByMember(order);
		if(sms != null){
			msgList.add(sms);
		}
		if(!msgList.isEmpty()) return ErrorCode.getSuccessReturn(msgList);
		return ErrorCodeConstant.DATEERROR;
	}
	private ErrorCode<List<SMSRecord>> addPubSaleOrderMessage(GewaOrder order){
		PubSaleOrder porder = (PubSaleOrder)order;
		PubSale sale = baseDao.getObject(PubSale.class, porder.getPubid());
		List<SMSRecord> smsList = new ArrayList<SMSRecord>();
		if(sale.isCard()) {
			if(StringUtils.isNotBlank(sale.getCardpass())) {
				String[] pass = StringUtils.split(sale.getCardpass(), ",");
				ElecCard card = elecCardService.getElecCardByPass(pass[0]);
				if(card!=null) {
					Timestamp time = card.getEndtime()!=null?card.getEndtime():card.getEbatch().getTimeto();
					String msg = "兑换券密码为" + sale.getCardpass();
					msg = msg + "有效期至" + DateUtil.format(time, "M月d日 HH:mm")+",逾期作废,请及时使用";
					SMSRecord sms = new SMSRecord(porder.getPubid(), porder.getTradeNo(), porder.getMobile(), msg,
							 porder.getAddtime(), time, SmsConstant.SMSTYPE_NOW);
					SMSRecord mustsend = saveMessage(sms);
					smsList.add(mustsend);
					return ErrorCode.getSuccessReturn(smsList);
				}
			}
		}else {
			String msg = porder.getSuccessMsg();
			SMSRecord sms = new SMSRecord(porder.getPubid(), porder.getTradeNo(), porder.getMobile(), msg,
					 porder.getAddtime(), porder.getValidtime(), SmsConstant.SMSTYPE_NOW);
			SMSRecord mustsend = saveMessage(sms);
			smsList.add(mustsend);
			return ErrorCode.getSuccessReturn(smsList);
		}
		return ErrorCodeConstant.DATEERROR;
	}
	@Override
	public ErrorCode<SMSRecord> addPostPubSaleMessage(PubSaleOrder porder, String company, String sno) {
		if(!StringUtils.equals(porder.getStatus(), OrderConstant.STATUS_PAID_SUCCESS)){
			return ErrorCode.getFailure("非成功支付的订单，不能发短信！");
		}
		String msg = porder.getPostMsg(company, sno);
		Timestamp curtime = new Timestamp(System.currentTimeMillis());
		SMSRecord sms = new SMSRecord(porder.getPubid(), porder.getTradeNo(), porder.getMobile(), msg,
				curtime, DateUtil.addDay(curtime, 1), SmsConstant.SMSTYPE_MANUAL);
		SMSRecord mustsend = saveMessage(sms);
		return ErrorCode.getSuccessReturn(mustsend);
	}
	@Override
	public ErrorCode<List<SMSRecord>> addSportOrderMessage(GewaOrder order){
		SportOrder sorder = (SportOrder)order;
		if(!StringUtils.equals(sorder.getStatus(), OrderConstant.STATUS_PAID_SUCCESS)){
			return ErrorCode.getFailure("非成功支付的订单，不能发短信！");
		}
		Sport sport = baseDao.getObject(Sport.class, sorder.getSportid());
		Sport2Item sport2Item = sportService.getSport2Item(sorder.getSportid(), sorder.getItemid());
		String msg1 = sport2Item.getNotifymsg1();
		if(StringUtils.isBlank(msg1)) return ErrorCode.getFailure("短信模板为空，不能发短信！");
		OpenTimeTable table = baseDao.getObject(OpenTimeTable.class, sorder.getOttid());
		SMSRecord mustsend = null;
		Timestamp validtime = null;
		List<SMSRecord> smsList = new ArrayList<SMSRecord>();
		if(table.hasPeriod()){
			SellTimeTable stt = baseDao.getObject(SellTimeTable.class, order.getId());
			//date starttime至endtime quantity人去上海游泳馆游泳，凭手机号和取票密码password验证入场 
			msg1 = StringUtils.replace(msg1, "date", DateUtil.format(table.getPlaydate(), "M月d日"));
			msg1 = StringUtils.replace(msg1, "quantity", order.getQuantity()+"");
			msg1 = StringUtils.replace(msg1, "password", order.getCheckpass());
			msg1 = StringUtils.replace(msg1, "starttime", stt.getStarttime());
			msg1 = StringUtils.replace(msg1, "endtime", stt.getEndtime());
			msg1 = StringUtils.replace(msg1, "address", sport.getAddress());
			msg1 = StringUtils.replace(msg1, "tradeno", StringUtils.right(order.getTradeNo(), 4)); 
			validtime = table.getPlayTimeByHour(stt.getStarttime());
		}else if(table.hasInning()){
			SellTimeTable stt = baseDao.getObject(SellTimeTable.class, order.getId());
			msg1 = StringUtils.replace(msg1, "date", DateUtil.format(table.getPlaydate(), "M月d日"));
			msg1 = StringUtils.replace(msg1, "quantity", order.getQuantity()+"");
			msg1 = StringUtils.replace(msg1, "password", order.getCheckpass());
			msg1 = StringUtils.replace(msg1, "starttime", stt.getStarttime());
			msg1 = StringUtils.replace(msg1, "endtime", stt.getEndtime());
			msg1 = StringUtils.replace(msg1, "address", sport.getAddress());
			msg1 = StringUtils.replace(msg1, "tradeno", StringUtils.right(order.getTradeNo(), 4)); 
			validtime = table.getPlayTimeByHour(stt.getStarttime());
		}else{
			List<OpenTimeItem> otiList = sportOrderService.getMyOtiList(sorder.getId());
			String hour = SportOrderHelper.getMessageText(otiList);
			msg1 = msg1.replace("date", DateUtil.format(table.getPlaydate(), "M月d日"));
			msg1 = msg1.replace("item", table.getItemname());
			msg1 = msg1.replace("hour", hour);
			validtime = SportOrderHelper.getPlaytime(table, otiList);
			int beginIndex = order.getTradeNo().length()-4;
			msg1 = msg1.replace("tradeno", order.getTradeNo().substring(beginIndex)); 
			msg1 = msg1.replace("password", order.getCheckpass()); 
			String result = JsonUtils.addJsonKeyValue(sorder.getOtherinfo(), SportOrder.SPORT_CONFIRM, "Y");
			sorder.setOtherinfo(result);
			baseDao.saveObject(sorder);
		}
		
		SMSRecord smsrecord=getSMSRecordByOttidAndMobile(sorder.getOttid(),order.getMobile());
		if(smsrecord==null){
			String hours = "";
			String msg3 = sport2Item.getNotifymsg3();
			if(StringUtils.isNotBlank(msg3)){
				if(table.hasPeriod()){
					SellTimeTable stt = baseDao.getObject(SellTimeTable.class, order.getId());
					hours = stt.getStarttime();
					msg3 = StringUtils.replace(msg3, "starttime", hours);
					msg3 = StringUtils.replace(msg3, "endtime", stt.getEndtime());
					msg3 = StringUtils.replace(msg3, "address", sport.getAddress());
				}else if(table.hasInning()){
					SellTimeTable stt = baseDao.getObject(SellTimeTable.class, order.getId());
					hours = stt.getStarttime();
					msg3 = StringUtils.replace(msg3, "starttime", hours);
					msg3 = StringUtils.replace(msg3, "address", sport.getAddress());
				}else{
					hours=sportOrderService.getMyOtiHour(sorder.getId());
					msg3=msg3.replace("time", hours);
					SportItem sportitem=baseDao.getObject(SportItem.class,sorder.getItemid());
					msg3=msg3.replace("item", sportitem.getName());
				}
				Timestamp sendtimehour=DateUtil.addHour(table.getPlayTimeByHour(hours),-3);
				SMSRecord smshour = new SMSRecord(sorder.getOttid(), sorder.getTradeNo(), order.getMobile(), msg3,
						sendtimehour,table.getPlayTimeByHour(hours), SmsConstant.SMSTYPE_3H);
				saveMessage(smshour);
			}
		}
		SMSRecord sms = new SMSRecord(sorder.getOttid(), sorder.getTradeNo(), order.getMobile(), msg1,
				 sorder.getAddtime(), validtime, SmsConstant.SMSTYPE_NOW);
		mustsend = saveMessage(sms);
		smsList.add(mustsend);
		//运动后短信
		String okey = sorder.getMemberid() + TagConstant.MEMBER_SPORT + sorder.getSportid() + "_"+ sorder.getItemid();
		UserOperation uo = baseDao.getObject(UserOperation.class, okey);
		if(uo == null){
			addSportMsg(sorder);
		}
		return ErrorCode.getSuccessReturn(smsList);
	}
	@Override
	public SMSRecord getSMSRecordByOttidAndMobile(Long ottid,String mobile){
		DetachedCriteria query = DetachedCriteria.forClass(SMSRecord.class);
		query.add(Restrictions.eq("relatedid",ottid));
		query.add(Restrictions.eq("contact",mobile));
		List<SMSRecord> result= hibernateTemplate.findByCriteria(query);
		if(result.isEmpty()) return null;
		return result.get(0);
	}
	@Override
	public void addUnSendMessage(GewaOrder order) {
		Timestamp curtime = DateUtil.getCurFullTimestamp();
		Timestamp cur = new Timestamp(curtime.getTime()-1000);
		String msg = "不需发送";
		SMSRecord sms = new SMSRecord(order.getTradeNo(), order.getMobile(), msg, cur, cur, SmsConstant.SMSTYPE_NOW);
		sms.setStatus(SmsConstant.STATUS_PROCESS);
		saveMessage(sms);
		List<OrderNote> noteList = baseDao.getObjectListByField(OrderNote.class, "orderid", order.getId());
		for (OrderNote orderNote : noteList) {
			if(StringUtils.isBlank(orderNote.getMessage())){
				orderNote.setMessage(msg);
				orderNote.setUpdatetime(curtime);
			}
		}
		baseDao.saveObjectList(noteList);
	}
	
	private SMSRecord saveMessage(SMSRecord record){
		SMSRecord old = getSMSRecordByUkey(record.getTradeNo(), record.getContact(), record.getSmstype());
		if(old == null) {
			baseDao.saveObject(record);
			return record;
		}
		return old;
	}
	@Override
	public SMSRecord getSMSRecordByUkey(String tradeNo, String contact, String smstype){
		String query = "from SMSRecord where tradeNo=? and contact=? and smstype=?";
		List<SMSRecord> result = hibernateTemplate.find(query, tradeNo, contact, smstype);
		if(result.isEmpty()) return null;
		return result.get(0);
	}
	private ErrorCode<List<SMSRecord>> addTicketOrderMessage(TicketOrder order, List<SellSeat> seatList, OpenPlayItem opi) {
		String mobile = order.getMobile();
		String notifymsg1 = "", notifymsg2 = "";
		JsonData msgTemplate = baseDao.getObject(JsonData.class, JsonDataKey.KEY_SMSTEMPLATE + opi.getId());
		if(msgTemplate!=null){
			notifymsg1 = VmUtils.getJsonValueByKey(msgTemplate.getData(), "notifymsg1");
			notifymsg2 = VmUtils.getJsonValueByKey(msgTemplate.getData(), "notifymsg2");
		}
		if(StringUtils.isBlank(notifymsg1) || StringUtils.isBlank(notifymsg2)){
			CinemaRoom room = baseDao.getObject(CinemaRoom.class, opi.getRoomid());
			Map map = JsonUtils.readJsonToMap(room.getOtherinfo());
			if(StringUtils.isBlank(notifymsg1) && map.get("message1") != null && StringUtils.isNotBlank((String)map.get("message1"))){
				notifymsg1 = (String)map.get("message1");
			}
			if(StringUtils.isBlank(notifymsg2) && map.get("message2") != null && StringUtils.isNotBlank((String)map.get("message2"))){
				notifymsg2 = (String)map.get("message2");
			}
		}
		String status1 = SmsConstant.STATUS_N;
		String status2 = SmsConstant.STATUS_N;
		CinemaProfile profile = baseDao.getObject(CinemaProfile.class, opi.getCinemaid());
		List<SMSRecord> smsList = new ArrayList<SMSRecord>();
		if(order.sureOutPartner()){
			ApiUser partner = baseDao.getObject(ApiUser.class, order.getPartnerid());
			Map<String, String> otherinfo = VmUtils.readJsonToMap(partner.getOtherinfo());
			if(StringUtils.isNotBlank(otherinfo.get("msg1"))){
				if(StringUtils.equals("disable", otherinfo.get("msg1"))){
					status1 = SmsConstant.STATUS_Y_IGNORE;
				}else if(StringUtils.isBlank(notifymsg1)) {
					notifymsg1 = otherinfo.get("msg1");
					if(profile.getNotifymsg1().contains("lorder")){
						notifymsg1 = StringUtils.replace(notifymsg1, "lorder", order.getTradeNo());
					}else if(profile.getNotifymsg1().contains("sorder")){
						notifymsg1 = StringUtils.replace(notifymsg1, "sorder", StringUtils.right(order.getTradeNo(), 6));
					}
					if(profile.getNotifymsg1().contains("hfhpass")){
						notifymsg1 = StringUtils.replace(notifymsg1, "checkpass", "hfhpass");
					}else if(profile.getNotifymsg1().contains("gewapass")){
						notifymsg1 = StringUtils.replace(notifymsg1, "checkpass", "gewapass");
					}
				}
			}
			if(StringUtils.isNotBlank(otherinfo.get("msg2"))){
				if(StringUtils.equals("disable", otherinfo.get("msg2"))) status2 = SmsConstant.STATUS_Y_IGNORE;
				else if(StringUtils.isBlank(notifymsg2)) notifymsg2 = otherinfo.get("msg2");
			}
		}
		if(StringUtils.isBlank(notifymsg1) || StringUtils.isBlank(notifymsg2)){
			if(StringUtils.isBlank(notifymsg1)) notifymsg1 = profile.getNotifymsg1();
			if(StringUtils.isBlank(notifymsg2)) notifymsg2 = profile.getNotifymsg2();
		}
		ErrorCode<String> checkmsg = getCheckpassMsg(notifymsg1, order, seatList, opi);
		if(!checkmsg.isSuccess()) return ErrorCode.getFailure(checkmsg.getMsg());
		String msg = checkmsg.getRetval();
		
		Timestamp playtime = opi.getPlaytime();
		SMSRecord sms = new SMSRecord(order.getMpid(), order.getTradeNo(), mobile, msg, order.getAddtime(), playtime, SmsConstant.SMSTYPE_NOW);
		sms.setStatus(status1);
		SMSRecord mustsend = saveMessage(sms);
		// 场次上映时间大于11点，按照提前3小时提醒；
		// 场次上映时间小于11点，按照提前1小时提醒；
		// 购票时间距上映时间小于3个小时，不发短信提醒；
		// 加入验证: 同一场次+同一个手机号. 不重复发送
		String opkey = "" + opi.getMpid() + mobile;
		if(operationService.isAllowOperation(opkey, OperationService.ONE_DAY * 2, 1)){
			if(DateUtil.getDiffHour(playtime, order.getPaidtime()) > 3){
				int hour = DateUtil.getHour(playtime);
				int addhour = 0;
				if(hour > 2 && hour < 11) addhour = 1;		
				else addhour = 3;
				Timestamp sendtime = DateUtil.addHour(playtime, -addhour);
				Cinema cinema = baseDao.getObject(Cinema.class, opi.getCinemaid());
				sms = new SMSRecord(order.getMpid(), order.getTradeNo(), mobile, getMsg(notifymsg2, opi, cinema), sendtime, playtime, SmsConstant.SMSTYPE_3H);
				sms.setStatus(status2);
				saveMessage(sms);
			}
			addCommentMsg(order, opi);
			operationService.updateOperation(opkey, OperationService.ONE_DAY * 2, 1);
		}
		if(mustsend.getStatus().equals(SmsConstant.STATUS_Y_IGNORE)) return ErrorCode.getSuccess("商家不需发送");
		smsList.add(mustsend);
		return ErrorCode.getSuccessReturn(smsList);
	}
	
	private SMSRecord getCreateMemberByMember(GewaOrder order){
		Map<String, String> otherInfoMap = JsonUtils.readJsonToMap(order.getOtherinfo());
		String mobile = otherInfoMap.get(OrderConstant.OTHERKEY_BINDMOBILE);
		String createMember = otherInfoMap.get(OrderConstant.OTHERKEY_CREATEMEMBER);
		SMSRecord mustsend = null;
		if(Boolean.parseBoolean(createMember)){
			String msg = "恭喜你完成电话购票，此次消费获得等值消费金额的的积分已帮你添加至账户，使用手机号登录www.gewara.com即可查看（100积分=1元可用于下次消费抵值）";
			if(ValidateUtil.isMobile(mobile)){
				mustsend = addManualMsg(order.getMemberid(), mobile, msg, order.getTradeNo() + "createMember");
			}else{
				dbLogger.warn("getCreateMemberByMember :" + mobile + " error mobile");
			}
		}
		return mustsend;
	}
	
	private String getMsg(String template, OpenPlayItem opi, Cinema cinema){
		String date = DateUtil.format(opi.getPlaytime(), "M月d日");
		String time = DateUtil.format(opi.getPlaytime(), "HH:mm");
		String week = DateUtil.getCnWeek(opi.getPlaytime());
		String msg = template;
		
		msg = msg.replaceAll("date", date).replaceAll("time", time)
				.replaceAll("cinema", opi.getCinemaname())
				.replaceAll("movie", opi.getMoviename()).replaceAll("week", week)
				.replaceAll("address", StringUtils.defaultString(cinema.getAddress()));
		
		return msg;
	}

	@Override
	public void addCommentMsg(TicketOrder order, OpenPlayItem opi){
		//观影后发短信  所有零点以后的短信提醒延迟9小时发送
		if(order.sureOutPartner()) return;//商户不用，status2 = SmsConstant.STATUS_Y_IGNORE;
		Movie movie = baseDao.getObject(Movie.class, opi.getMovieid());
		int len = movie.getVideolen()==null?100:movie.getVideolen();
		Timestamp sendtime = DateUtil.addMinute(opi.getPlaytime(), len);
		int hour = DateUtil.getHour(sendtime); 
		if(hour >= 23 || hour < 3) sendtime = DateUtil.addHour(sendtime, 9);
		Timestamp validtime = DateUtil.addMinute(sendtime, 120);
		String dkey = JsonDataKey.KEY_SMSMOVIE + order.getMovieid();
		JsonData data = baseDao.getObject(JsonData.class, dkey);
		String smsContent = "";
		if(data!=null) {
			Map<String, String> m = VmUtils.readJsonToMap(data.getData());
			smsContent = m.get("msg");
		}
		if(StringUtils.isBlank(smsContent)) {
			GewaConfig smsConfig = baseDao.getObject(GewaConfig.class, ConfigConstant.CFG_COMMENTMSG);
			smsContent = smsConfig.getContent();
		}
		String result = smsContent.replace("moviename", movie.getRealBriefname());
		SMSRecord sms = new SMSRecord(order.getMpid(), order.getTradeNo(), order.getMobile(), result, sendtime, validtime, SmsConstant.SMSTYPE_10M);
		sms.setStatus(SmsConstant.STATUS_N);
		saveMessage(sms);
	}
	@Override
	public void addDramaMsg(DramaOrder order, OpenDramaItem odi){
		Timestamp sendtime = DateUtil.addHour(odi.getPlaytime(), 3);
		Timestamp validtime = DateUtil.addMinute(sendtime, 480);
		String dkey = JsonDataKey.KEY_SMSDRAMA + order.getDramaid();
		JsonData data = baseDao.getObject(JsonData.class, dkey);
		String smsContent = "";
		if(data != null){
			Map<String, String> m = VmUtils.readJsonToMap(data.getData());
			smsContent = m.get("msg");
		}
		if(StringUtils.isBlank(smsContent)){
			GewaConfig smsConfig = baseDao.getObject(GewaConfig.class, ConfigConstant.CFG_DRAMAMSG);
			if(smsConfig != null){
				smsContent = smsConfig.getContent();
			}
		}
		if(StringUtils.isNotBlank(smsContent)){
			SMSRecord sms = new SMSRecord(order.getDpid(), order.getTradeNo(), order.getMobile(), smsContent, sendtime, validtime, SmsConstant.SMSTYPE_10M);
			String status2 = SmsConstant.STATUS_N;
			if(order.sureOutPartner()) status2 = SmsConstant.STATUS_Y_IGNORE;
			sms.setStatus(status2);
			saveMessage(sms);
		}
	}
	@Override
	public void addSportMsg(SportOrder order){
		OpenTimeTable ott = baseDao.getObject(OpenTimeTable.class,  order.getOttid());
		String strDate = DateUtil.format(ott.getPlaydate(), "yyyy-MM-dd");
		String hours = "";
		if(ott.hasPeriod()||ott.hasInning()){
			SellTimeTable stt = baseDao.getObject(SellTimeTable.class, order.getId());
			hours = stt.getStarttime();
		}else{
			List<OpenTimeItem> otiList = sportOrderService.getMyOtiList(order.getId());
			Collections.sort(otiList, new PropertyComparator("hour", false, false));
			hours = otiList.get(0).getHour();
		}
		strDate = strDate + " " + hours + ":00";
		Map<String, String> otherinfoMap = VmUtils.readJsonToMap(ott.getOtherinfo());
		String sendTimeStr = otherinfoMap.get("sendTime");
		Integer sendTime = 120;
		SportItem si = null;
		if(StringUtils.isBlank(sendTimeStr)){
			si = baseDao.getObject(SportItem.class, ott.getItemid());
			otherinfoMap = VmUtils.readJsonToMap(si.getOtherinfo());
			sendTimeStr = otherinfoMap.get("sendTime");
		}
		if(StringUtils.isNotBlank(sendTimeStr)) sendTime = Integer.parseInt(sendTimeStr);
		Timestamp date = DateUtil.parseTimestamp(strDate);
		Timestamp sendtime = DateUtil.addMinute(date, sendTime);
		Timestamp validtime = DateUtil.addMinute(sendtime, 480);
		Sport2Item sport2Item = sportService.getSport2Item(order.getSportid(), order.getItemid());
		String smsContent = sport2Item.getOvermsg();
		if(StringUtils.isBlank(smsContent)){
			if(si == null) si = baseDao.getObject(SportItem.class, ott.getItemid());
			otherinfoMap = VmUtils.readJsonToMap(si.getOtherinfo());
			String defaultMsg = otherinfoMap.get("defaultMsg");
			if(StringUtils.isNotBlank(defaultMsg)) smsContent = defaultMsg;
			else{
				GewaConfig smsConfig = baseDao.getObject(GewaConfig.class, ConfigConstant.CFG_SPORTMSG);
				if(smsConfig != null){
					smsContent = smsConfig.getContent();
				}
			}
		}
		if(StringUtils.isNotBlank(smsContent)){
			SMSRecord sms = new SMSRecord(order.getSportid(), order.getTradeNo(), order.getMobile(), smsContent, sendtime, validtime, SmsConstant.SMSTYPE_10M);
			String status2 = SmsConstant.STATUS_N;
			if(order.sureOutPartner()) status2 = SmsConstant.STATUS_Y_IGNORE;
			sms.setStatus(status2);
			saveMessage(sms);
			String opkey = order.getMemberid() + TagConstant.MEMBER_SPORT + order.getSportid();
			Timestamp add = DateUtil.getCurFullTimestamp();
			Timestamp valid = DateUtil.addDay(add, 30);
			UserOperation uo = new UserOperation(opkey, add, valid, TagConstant.MEMBER_SPORT);
			baseDao.saveObject(uo);
		}
	}
	@Override
	public ErrorCode<List<SMSRecord>> addMemberCardOrderMsg(GewaOrder gorder){
		JsonData msgTemplate = baseDao.getObject(JsonData.class, MemberCardConstant.KEY_MEMBERCARDMSG);
		List<SMSRecord> smsList = new ArrayList<SMSRecord>();
		if(msgTemplate==null){
			return ErrorCode.getSuccessReturn(smsList);
		}
		MemberCardOrder order = (MemberCardOrder)gorder;
		String msg = msgTemplate.getData();
		Sport sport = baseDao.getObject(Sport.class, order.getPlaceid());
		MemberCardInfo card = baseDao.getObject(MemberCardInfo.class, order.getCardid());
		MemberCardType type = baseDao.getObject(MemberCardType.class, card.getTypeid());
		msg = msg.replace("placename", sport.getRealBriefname()).replace("cardno", card.getMemberCardCode());
		if(type.hasNumCard()){
			msg = msg.replace("money", type.getMoney()+"次卡");
		}else if(type.hasAmountCard()){
			msg = msg.replace("money", type.getMoney()+"金额卡");
		}else {
			return ErrorCode.getSuccessReturn(smsList);
		}
		if(StringUtils.isNotBlank(msg)){
			Timestamp sendtime = DateUtil.getCurFullTimestamp();
			Timestamp validtime = DateUtil.addMinute(sendtime, 480);
			SMSRecord sms = new SMSRecord(order.getCardid(), order.getTradeNo(), order.getMobile(), msg, sendtime, validtime, SmsConstant.SMSTYPE_NOW);
			String status2 = SmsConstant.STATUS_N;
			if(order.sureOutPartner()) status2 = SmsConstant.STATUS_Y_IGNORE;
			sms.setStatus(status2);
			saveMessage(sms);
			smsList.add(sms);
		}
		return ErrorCode.getSuccessReturn(smsList);
	}
	@Override
	public String getCheckpassTemplate(OpenPlayItem opi) {
		String msgTemplate = null;
		JsonData template = baseDao.getObject(JsonData.class, JsonDataKey.KEY_SMSTEMPLATE + opi.getId());
		if(template!=null){
			msgTemplate = VmUtils.getJsonValueByKey(template.getData(), "notifymsg1");
		}
		if(StringUtils.isBlank(msgTemplate)){
			CinemaProfile cp = baseDao.getObject(CinemaProfile.class, opi.getCinemaid());
			msgTemplate = cp.getNotifymsg1();
		}
		return msgTemplate;
	}
	@Override
	public ErrorCode<String> getCheckpassMsg(String msgTemplate, TicketOrder order, List<SellSeat> seatList, OpenPlayItem opi) {
		if(seatList == null || seatList.size() <= 0){
			return ErrorCode.getFailure("数据错误, 请重试!");
		}
		String seat =seatList.get(0).getSeatLabel();
		String seatall = "";
		for(SellSeat sellSeat : seatList){
			if(StringUtils.isNotBlank(seatall)){
				seatall += "、" + sellSeat.getSeatLabel();
			}else{
				seatall += sellSeat.getSeatLabel();
			}
		}
		if(order.getQuantity() > 1) seat += "等" + order.getQuantity() + "张票";
		String result = msgTemplate;
		if(result.indexOf("hfhpass") >= 0){
			if(StringUtils.isBlank(order.getHfhpass())) return ErrorCode.getFailure("加入短信失败，密码错误！");
			result = result.replace("hfhpass", order.getHfhpass());
		}
		String date = DateUtil.format(opi.getPlaytime(), "M月d日");
		String time = DateUtil.format(opi.getPlaytime(), "HH:mm");
		result = result.replaceAll("date", date).replaceAll("time", time)
				.replaceAll("cinema", opi.getCinemaname())
				.replaceAll("movie", opi.getMoviename());
		result = StringUtils.replace(result, "seatall", seatall);
		result = StringUtils.replace(result, "seat", seat);
		if(StringUtils.contains(result, "lorder")){
			result = StringUtils.replace(result, "lorder", order.getTradeNo());
		}else if(StringUtils.contains(result, "sorder")){
			result = StringUtils.replace(result, "sorder", StringUtils.right(order.getTradeNo(), 6));
		}
		result = StringUtils.replace(result, "gewapass", order.getCheckpass());

		String week = "";
		if(!order.sureOutPartner()) week = DateUtil.getCnWeek(opi.getPlaytime());
		result = StringUtils.replace(result, "week", week);
		result = StringUtils.replace(result, "endMobile", StringUtils.right(order.getMobile(), 4));
		//GewaConfig smsConfig = baseDao.getObject(GewaConfig.class, ConfigConstant.CFG_TICKET_GOODS_MSG);
		CinemaProfile profile = baseDao.getObject(CinemaProfile.class, opi.getCinemaid());
		/*if(smsConfig != null && StringUtils.isNotBlank(smsConfig.getContent())
				&& Arrays.asList(StringUtils.split(smsConfig.getContent(), ",")).contains(order.getCinemaid() + "")){*/
		if(profile != null && profile.hasDefinePaper()){
			Map<String, String> otherinfoMap = VmUtils.readJsonToMap(order.getOtherinfo());
			String bindgoods = otherinfoMap.get(PayConstant.KEY_BINDGOODS);
			String goodsgift = otherinfoMap.get(PayConstant.KEY_GOODSGIFT);
			if(StringUtils.isNotBlank(bindgoods) || StringUtils.isNotBlank(goodsgift) || order.getItemfee() > 0){
				result = StringUtils.replace(result, "containGoods","含卖品");
			}else{
				result = StringUtils.replace(result, "containGoods","");
			}
		}else{
			result = StringUtils.replace(result, "containGoods","");
		}
		return ErrorCode.getSuccessReturn(result);
	}
	@Override
	public List<SMSRecord> getUnSendMessageList(int maxnum) {
		Timestamp curtime = new Timestamp(System.currentTimeMillis());
		DetachedCriteria query = DetachedCriteria.forClass(SMSRecord.class);
		query.add(Restrictions.ge("validtime", curtime));
		query.add(Restrictions.le("sendtime", DateUtil.addMinute(curtime, -10)));
		query.add(Restrictions.eq("status", SmsConstant.STATUS_N));
		query.add(Restrictions.lt("sendnum", 2));
		query.addOrder(Order.desc("smstype"));
		query.addOrder(Order.asc("sendnum"));
		query.addOrder(Order.asc("sendtime"));
		List<SMSRecord> result = hibernateTemplate.findByCriteria(query, 0, maxnum);
		return result;
	}
	@Override
	public List<SMSRecord> getFailureSMSList() {
		Timestamp nowTime = new Timestamp(System.currentTimeMillis());
		DetachedCriteria query = DetachedCriteria.forClass(SMSRecord.class);
		query.add(Restrictions.eq("status", "N"));
		query.add(Restrictions.ge("sendnum", 2));
		query.add(Restrictions.ge("validtime", nowTime));
		query.add(Restrictions.isNotNull("tradeNo"));
		List<SMSRecord> smsList = hibernateTemplate.findByCriteria(query);
		return smsList;
	}
	@Override
	public List<GewaOrder> getUnSendOrderList(){
		String hql = "from TicketOrder t where t.status = ? " +
				"and t.playtime>? and not exists(select s.id from SMSRecord s where s.tradeNo=t.tradeNo)";
		Timestamp curtime = new Timestamp(System.currentTimeMillis());
		List orderList = hibernateTemplate.find(hql, OrderConstant.STATUS_PAID_SUCCESS, DateUtil.addDay(curtime, -1));
		return orderList;
	}
	@Override
	public List<DramaOrder> getUnSendDramaOrderList(){
		DetachedCriteria query = DetachedCriteria.forClass(DramaOrder.class, "g");
		query.add(Restrictions.ilike("g.status", OrderConstant.STATUS_PAID, MatchMode.START));
		DetachedCriteria subQuery = DetachedCriteria.forClass(OrderNote.class, "o");
		subQuery.add(Restrictions.eqProperty("o.orderid", "g.id"));
		subQuery.setProjection(Projections.property("o.orderid"));
		subQuery.add(Restrictions.isNull("o.message"));
		
		List<DramaOrder> orderList = hibernateTemplate.findByCriteria(query);
		return orderList;
	}
	
	@Override
	public List<SportOrder> getUnSendSportOrderList(){
		String hql = "from SportOrder t where t.status like ? " +
				"and exists(select ott.id from OpenTimeTable ott where ott.playdate>? and ott.id=t.ottid) " +
				"and not exists(select s.id from SMSRecord s where s.tradeNo=t.tradeNo)";
		List orderList = hibernateTemplate.find(hql, OrderConstant.STATUS_PAID+"%", DateUtil.addDay(new Date(), -1));
		return orderList;
	}
	@Override
	public List<GymOrder> getUnSendGymOrderList(){
		String hql = "from GymOrder t where t.status like ? " +
				"and exists(select gci.id from GymCardItem gci where gci.id=t.gci) " +
				"and not exists(select s.id from SMSRecord s where s.tradeNo=t.tradeNo)";
		List orderList = hibernateTemplate.find(hql, OrderConstant.STATUS_PAID+"%");
		return orderList;
	}
	
	@Override
	public String getMobileList(String type, List<Long> cinemaidList, Long movieid,
			Long relatedid, Timestamp fromtime, Timestamp totime) {
		String mobileList = "";
		DetachedCriteria countQuery = getMobileCriteria(type, cinemaidList, movieid, relatedid, fromtime, totime);
		countQuery.setProjection(Projections.countDistinct("mobile"));
		Integer count = Integer.parseInt(""+hibernateTemplate.findByCriteria(countQuery).get(0));
		DetachedCriteria query = getMobileCriteria(type, cinemaidList, movieid, relatedid, fromtime, totime);
		query.setProjection(Projections.distinct(Projections.property("mobile")));
		query.addOrder(Order.asc("mobile"));
		List<String> mobiles = null;
		for(int i=0, page=(count+1)/1000; i<=page; i++){
			mobiles = hibernateTemplate.findByCriteria(query, i*1000, 1000);
			mobileList += StringUtils.join(mobiles, ",") + ",";
		}
		return mobileList;
	}
	private DetachedCriteria getMobileCriteria(String type, List<Long> cinemaidList, Long movieid,
			Long relatedid, Timestamp fromtime, Timestamp totime){
		DetachedCriteria query = null;
		if("ticket".equals(type)){
			query = DetachedCriteria.forClass(TicketOrder.class, "o");
			if(relatedid!=null) query.add(Restrictions.eq("mpid", relatedid));
			if(cinemaidList!=null && cinemaidList.size()>0) query.add(Restrictions.in("cinemaid", cinemaidList));
			if(movieid!=null) query.add(Restrictions.eq("movieid", movieid));
		}else if("goods".equals(type)){
			query = DetachedCriteria.forClass(GoodsOrder.class, "o");
			if(relatedid!=null) query.add(Restrictions.eq("goodsid", relatedid));
		}else if("tg".equals(type)){
			query = DetachedCriteria.forClass(GoodsOrder.class, "o");
			DetachedCriteria subquery = DetachedCriteria.forClass(Goods.class, "g");
			if(relatedid!=null) subquery.add(Restrictions.eq("g.relatedid", relatedid));
			subquery.setProjection(Projections.property("g.id"));
			List<Long> list = hibernateTemplate.findByCriteria(subquery);
			if(list.isEmpty()) return null;
			else {
				query.add(Restrictions.in("o.goodsid", list));
			}
		}
		if(query != null){
			query.add(Restrictions.eq("o.status", OrderConstant.STATUS_PAID_SUCCESS));
			if(fromtime!=null) query.add(Restrictions.ge("o.addtime", fromtime));
			if(totime!=null) query.add(Restrictions.le("o.addtime", totime));
		}
		return query;
	}
	
	@Override
	public void removeSMSRecordByTradeNo(String tradeNo, String smstype) {
		String hql = "delete SMSRecord where tradeNo = ? and smstype = ? and status =? ";
		hibernateTemplate.bulkUpdate(hql, tradeNo, smstype, SmsConstant.STATUS_N);
	}
	@Override
	public SMSRecord addManualMsg(Long relatedid, String mobile, String msg, String ukey) {
		Timestamp cur = new Timestamp(System.currentTimeMillis());
		if(StringUtils.isBlank(ukey)) ukey = "M" + DateUtil.format(cur, "yyMMddHHmmss");
		SMSRecord sms = new SMSRecord(relatedid, ukey, mobile, msg, cur, DateUtil.addDay(cur, 2), SmsConstant.SMSTYPE_MANUAL);
		saveMessage(sms);
		return sms;
	}
	@Override
	public String getSmsChannel(String mobile, String smstype) {
		return getSmsChannel(mobile, smstype, channelMap);
	}
	@Override
	public String getSmsChannel(String mobile, String smstype, Map<String, String> map) {
		String result = map.get(smstype);
		if(StringUtils.isBlank(result)){
			result = map.get("default");
		}
		if(StringUtils.isBlank(result)){
			result = MobileService.CHANNEL_MLINK;
		}else if(StringUtils.equals(result, MobileService.CHANNEL_MAS)){
			if(!ValidateUtil.isYdMobile(mobile)) result = map.get("notmobile");
			if(result == null) result = MobileService.CHANNEL_MLINK;
		}
		return result;
	}
	
	@Override
	public int querySmsRecord(String tradeNo, String tag, Timestamp starttime, Timestamp endtime, Long relatedid, Long memberid){
		DetachedCriteria query = DetachedCriteria.forClass(SMSRecord.class);
		if(StringUtils.isNotBlank(tradeNo))query.add(Restrictions.like("tradeNo", tradeNo, MatchMode.START));
		if(StringUtils.isNotBlank(tag)) query.add(Restrictions.eq("tag", tag));
		if(starttime != null) query.add(Restrictions.ge("sendtime", starttime));
		if(endtime != null) query.add(Restrictions.le("sendtime", endtime));
		query.add(Restrictions.like("status", SmsConstant.STATUS_Y, MatchMode.START));
		if(relatedid != null)query.add(Restrictions.eq("relatedid", relatedid));
		if(memberid != null) query.add(Restrictions.eq("memberid", memberid));
		query.setProjection(Projections.sum("sendnum"));
		List<Long> result = hibernateTemplate.findByCriteria(query);
		if(result.get(0)==null) return 0;
		return Integer.parseInt(result.get(0)+"");
		
	}

	@Override
	public String getOrderPassword(TicketOrder ticketOrder, List<SellSeat> seatList){
		OpenPlayItem opi = baseDao.getObjectByUkey(OpenPlayItem.class, "mpid", ticketOrder.getMpid(), false);
		if(opi==null || opi.getPlaytime().before(DateUtil.addDay(new Timestamp(System.currentTimeMillis()), -1))) {
			//查询过期场次，不需密码
			return "";
		}
		String msgTemplate = getCheckpassTemplate(opi);
		ErrorCode<String> msg = getCheckpassMsg(msgTemplate, ticketOrder, seatList, opi);
		String password = msg.getRetval();
		List<BuyItem> itemList = baseDao.getObjectListByField(BuyItem.class, "orderid", ticketOrder.getId());
		if(itemList.size() >0 ) {
			for(BuyItem item : itemList){
				password = password + "\n套餐：" + item.getGoodsname()+"\n套餐密码：" + item.getCheckpass();
			}
		}
		return password;
	}

	@Override
	public void refreshCurrent(String newConfig) {
		JsonData data = baseDao.getObject(JsonData.class, JsonDataKey.KEY_SMSCHANNEL);
		channelMap = VmUtils.readJsonToMap(data.getData());
	}
	@Override
	public void updateSMSRecordStatus(List<String> orderList){
		List<SMSRecord> delSmsList = new ArrayList<SMSRecord>();
		for(String tradeNo : orderList){
			List<SMSRecord> smsList = baseDao.getObjectListByField(SMSRecord.class, "tradeNo", tradeNo);
			for (SMSRecord smsRecord : smsList) {
				if(!StringUtils.contains(smsRecord.getStatus(), SmsConstant.STATUS_Y)){
					smsRecord.setStatus( SmsConstant.STATUS_D + smsRecord.getStatus());
					delSmsList.add(smsRecord);
					dbLogger.warnWithType(LogTypeConstant.LOG_TYPE_ORDER_PAY, "废弃场次成功后取消短信ID："+smsRecord.getId()+"  订单号："+smsRecord.getTradeNo());
				}
			}
		}
		baseDao.saveObjectList(delSmsList);
	}
	@Override
	public void afterPropertiesSet() throws Exception {
		refreshCurrent(null);
	}
	
}