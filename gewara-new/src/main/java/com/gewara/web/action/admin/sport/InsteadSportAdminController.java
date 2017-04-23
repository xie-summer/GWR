package com.gewara.web.action.admin.sport;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.support.PropertyComparator;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.RequestMapping;

import com.gewara.Config;
import com.gewara.constant.ApiConstant;
import com.gewara.constant.OpenTimeItemConstant;
import com.gewara.constant.OpenTimeTableConstant;
import com.gewara.constant.Status;
import com.gewara.constant.sys.ConfigConstant;
import com.gewara.helper.SportOrderHelper;
import com.gewara.helper.TimeItemHelper;
import com.gewara.model.acl.User;
import com.gewara.model.common.GewaConfig;
import com.gewara.model.pay.Discount;
import com.gewara.model.pay.GewaOrder;
import com.gewara.model.pay.SportOrder;
import com.gewara.model.sport.MemberCardInfo;
import com.gewara.model.sport.MemberCardType;
import com.gewara.model.sport.OpenTimeItem;
import com.gewara.model.sport.OpenTimeSale;
import com.gewara.model.sport.OpenTimeTable;
import com.gewara.model.sport.SellTimeTable;
import com.gewara.model.sport.Sport;
import com.gewara.model.sport.Sport2Item;
import com.gewara.model.sport.SportField;
import com.gewara.model.sport.SportItem;
import com.gewara.model.sport.SportProfile;
import com.gewara.model.user.Member;
import com.gewara.service.OrderException;
import com.gewara.service.gewapay.PaymentService;
import com.gewara.service.sport.InsteadSportOrderService;
import com.gewara.service.sport.MemberCardService;
import com.gewara.service.sport.OpenTimeTableService;
import com.gewara.service.sport.SportOrderService;
import com.gewara.service.sport.SportService;
import com.gewara.support.ErrorCode;
import com.gewara.util.BeanUtil;
import com.gewara.util.DateUtil;
import com.gewara.util.VmUtils;
import com.gewara.util.WebUtils;
import com.gewara.web.action.admin.BaseAdminController;

@Controller
public class InsteadSportAdminController extends BaseAdminController {
	public static List<String> OPENTYPE_LIST = Arrays.asList(OpenTimeTableConstant.OPEN_TYPE_FIELD, OpenTimeTableConstant.OPEN_TYPE_PERIOD, OpenTimeTableConstant.OPEN_TYPE_INNING);
	@Autowired@Qualifier("sportOrderService")
	private SportOrderService sportOrderService;
	public void setSportOrderService(SportOrderService sportOrderService) {
		this.sportOrderService = sportOrderService;
	}
	@Autowired@Qualifier("openTimeTableService")
	private OpenTimeTableService openTimeTableService;
	public void setOpenTimeTableService(OpenTimeTableService openTimeTableService) {
		this.openTimeTableService = openTimeTableService;
	}
	@Autowired@Qualifier("sportService")
	private SportService sportService;
	public void setSportService(SportService sportService) {
		this.sportService = sportService;
	}
	@Autowired@Qualifier("paymentService")
	private PaymentService paymentService;
	@Autowired@Qualifier("config")
	protected Config config;
	@Autowired@Qualifier("memberCardService")
	private MemberCardService memberCardService;
	@Autowired
	private InsteadSportOrderService insteadSportOrderService;

	@RequestMapping("/admin/sport/order/choosefield.xhtml")
	public String chooseField(@CookieValue(value=LOGIN_COOKIE_NAME, required=false)String sessid,
			HttpServletRequest request, Long ottid, Long sid, Long tid, String fyrq, String openType, ModelMap model) {
		String spkey = request.getParameter("spkey");
		model.put("spkey", spkey);
		
		Sport sport = null;
		SportItem item = null;
		OpenTimeTable ott = null;
		if(ottid==null && (sid==null || tid==null)) return show404(model, "参数传递错误！");
		if(ottid == null){
			sport = daoService.getObject(Sport.class, sid);
			if(sport == null) return show404(model, "该运动场管不存在或被删除！");
			item = daoService.getObject(SportItem.class, tid);
			if(item == null) return show404(model, "该项目不存在或被删除！");
			if(!DateUtil.isValidDate(fyrq)) fyrq = "";
			Date curDate = DateUtil.getCurDate();
			Date playDate = null;
			List<OpenTimeTable> itemList = new ArrayList<OpenTimeTable>();
			if(StringUtils.isBlank(fyrq)){ 
				playDate = DateUtil.getCurDate();
				itemList = openTimeTableService.getOpenTimeTableList(sid, tid, playDate, null, 0, 1);
			}else{
				if(!OPENTYPE_LIST.contains(openType)) return show404(model, "参数传递错误！");
				playDate = DateUtil.parseDate(fyrq);
				if(playDate.before(curDate)){
					playDate = curDate;
					fyrq = DateUtil.format(curDate, "yyyy-MM-dd");
				}
				itemList = openTimeTableService.getOpenTimeTableList(sid, tid, playDate, null, openType, true, 0, 1);
			}
			if(!itemList.isEmpty()){
				ott = itemList.get(0);
			}
		}else{ 
			ott = daoService.getObject(OpenTimeTable.class, ottid);
			if(ott == null) return show404(model, "场次不存在！");
			sport = daoService.getObject(Sport.class, ott.getSportid());
			if(sport == null) return show404(model, "该运动场管不存在或被删除！");
			item = daoService.getObject(SportItem.class, ott.getItemid());
			if(item == null) return show404(model, "该项目不存在或被删除！");
		}
		model.put("fyrq", fyrq);
		model.put("item", item);
		model.put("sport", sport);
		String viewPage = "";
		if(ott != null){
			if(!ott.isBooking())  return showMessageAndReturn(model, request, "本场不接受预定！");
			Member member = loginService.getLogonMemberBySessid(WebUtils.getRemoteIp(request), sessid);
			Sport2Item sport2Item = sportService.getSport2Item(ott.getSportid(), ott.getItemid());
			model.put("opentime", (StringUtils.substring(sport2Item.getOpentime(), 0, 2) + ":" + StringUtils.substring(sport2Item.getOpentime(), 2, 4)));
			model.put("closetime", (StringUtils.substring(sport2Item.getClosetime(), 0, 2) + ":" + StringUtils.substring(sport2Item.getClosetime(), 2, 4)));
			model.put("isOpen", sport2Item.isOpen());
			sportOttData(ott, member, model);
			List<MemberCardInfo> cardInfoList = new ArrayList<MemberCardInfo>();
			Map<Long, MemberCardType> mctMap = new HashMap<Long, MemberCardType>();
			if(ott.hasField()){//场地
				if(member!=null){
					cardInfoList = memberCardService.getValidMemberCardInfoListByMemberid(member.getId(), ott);
					List<Long> typidList = BeanUtil.getBeanPropertyList(cardInfoList, "typeid", true);
					mctMap = daoService.getObjectMap(MemberCardType.class, typidList);
				}
				viewPage = "admin/sport/wide_choosebyInning.vm";
				model.put("cardInfoList", cardInfoList);
				model.put("mctMap", mctMap);
			}else if(ott.hasPeriod()){//时间段
				viewPage = "admin/sport/wide_choosebyTime.vm";
			}else if(ott.hasInning()){//局数
				viewPage = "admin/sport/wide_choosebyRound.vm";
			}
		}else{
			if(StringUtils.equals(openType, OpenTimeTableConstant.OPEN_TYPE_FIELD)){//场地
				viewPage = "admin/sport/wide_choosebyInning.vm";
			}else if(StringUtils.equals(openType, OpenTimeTableConstant.OPEN_TYPE_PERIOD)){//时间段
				viewPage = "admin/sport/wide_choosebyTime.vm";
			}else if(StringUtils.equals(openType, OpenTimeTableConstant.OPEN_TYPE_INNING)){//局数
				viewPage = "admin/sport/wide_choosebyRound.vm";
			}
		}
		if(StringUtils.isNotBlank(viewPage)) return viewPage;
		else return show404(model, "参数传递错误！");
	}
	
	private void sportOttData(OpenTimeTable ott, Member member, ModelMap model){
		Timestamp cur = new Timestamp(System.currentTimeMillis());
		Date curDate = DateUtil.getBeginningTimeOfDay(cur);
		List<OpenTimeTable> itemList = new ArrayList<OpenTimeTable>();
		itemList = openTimeTableService.getOpenTimeTableList(ott.getSportid(), ott.getItemid(), curDate, null, ott.getOpenType());
		model.put("itemList", itemList);
		List<OpenTimeItem> otiList = openTimeTableService.getOpenItemList(ott.getId());
		List<OpenTimeItem> myOtiList = new ArrayList<OpenTimeItem>();
		if(member != null){
			SportOrder order = sportOrderService.getLastUnpaidSportOrder(member.getId(), member.getId()+"", ott.getId());
			if(order != null){
				if(ott.hasField()){
					SellTimeTable sellTimeTable = daoService.getObject(SellTimeTable.class, order.getId());
					model.put("sellTimeTable", sellTimeTable);
				}else myOtiList.addAll(sportOrderService.getMyOtiList(order.getId()));
			}
			model.put("member", member);
		}
		model.put("playDate", ott.getPlaydate());
		SportProfile profile = daoService.getObject(SportProfile.class, ott.getSportid());
		model.put("sp", profile);
		if(ott.hasField()){//按场地
			List<SportField> fieldList = sportOrderService.getSportFieldList(ott.getId());
			boolean validOver = false;
			List<Long> otiidList = new ArrayList<Long>();
			int price = 0;
			for (OpenTimeItem openTimeItem : myOtiList) {
				otiidList.add(openTimeItem.getId());
				price += openTimeItem.getPrice();
			}
			if(ott.getPlaydate().compareTo(DateUtil.getBeginningTimeOfDay(new Date()))==0) validOver = true;
			//List<String> playHourList = sportOrderService.getPlayHourList(ottid, OpenTimeItem.STATUS_DELETE);
			List<Integer> priceList = openTimeTableService.getTimeItemPrice(ott.getId());
			model.put("validOver", validOver);
			model.put("priceList", priceList);
			model.put("fieldList", fieldList);
			model.put("myOtiList", myOtiList);
			model.put("otiidList", StringUtils.join(otiidList, ","));
			model.put("price", price);
			//获取改场馆的限制时间 cpf
			Sport2Item sport2Item = sportService.getSport2Item(ott.getSportid(), ott.getItemid());
			model.put("sportlimitMinutes", sport2Item.getLimitminutes());
			Map<String, List<OpenTimeItem>> bindOtiMap = BeanUtil.groupBeanList(otiList, "bindInd");
			for (String key : bindOtiMap.keySet()) {
				List<OpenTimeItem> tempList = bindOtiMap.get(key);
				Collections.sort(tempList, new PropertyComparator("hour", false, true));
				bindOtiMap.put(key, tempList);
			}
			model.put("bindOtiMap", bindOtiMap);
			Map<String, List<OpenTimeItem>> saleIndOtiMap = BeanUtil.groupBeanList(otiList, "saleInd");
			for (String key : saleIndOtiMap.keySet()) {
				List<OpenTimeItem> tempList = saleIndOtiMap.get(key);
				Collections.sort(tempList, new PropertyComparator("hour", false, true));
				saleIndOtiMap.put(key, tempList);
			}
			model.put("saleIndOtiMap", saleIndOtiMap);
			List<OpenTimeItem> overotiList = new ArrayList<OpenTimeItem>();
			List<String> playHourList = new ArrayList<String>();
			TimeItemHelper itemHelper = new TimeItemHelper(otiList);
			int remain = 0;
			List<Long> filedidList = BeanUtil.getBeanPropertyList(fieldList, Long.class, "id", true);
			Map<String, OpenTimeItem> otiMap = itemHelper.getOtiMap();
			for(String key : otiMap.keySet()){
				OpenTimeItem oti = otiMap.get(key);
				if(validOver && oti.hasOver(sport2Item.getLimitminutes())){
					overotiList.add(oti);
				}else{
					if(oti.hasAvailable() && oti.getFieldid()!=null && filedidList.contains(oti.getFieldid())) remain++;
					if(!VmUtils.contains(playHourList, oti.getHour()) && !StringUtils.equals(oti.getStatus(), OpenTimeItemConstant.STATUS_DELETE)) { 
						playHourList.add(oti.getHour());
					}
				}
			}
			ott.setRemain(remain);
			for (OpenTimeItem overoti : overotiList) {
				if(StringUtils.isNotBlank(overoti.getBindInd()) && !StringUtils.equals(overoti.getBindInd(), "0")){
					List<OpenTimeItem> bindotiList = bindOtiMap.get(overoti.getBindInd());
					if(bindotiList != null){
						bindOtiMap.remove(overoti.getBindInd());
						otiList.removeAll(bindotiList);
					}
				}else{
					otiList.remove(overoti);
				}
			}
			//otiList.removeAll(overotiList);
			Collections.sort(playHourList);
			model.put("playHourList", playHourList);
			model.put("ott", ott);
			model.put("otiList", otiList);
			model.put("itemHelper", itemHelper);
			model.put("otsMap", daoService.getObjectMap(OpenTimeSale.class, BeanUtil.getBeanPropertyList(otiList, Long.class, "otsid", true)));
			Map<Long, Integer> otiCountMap = new HashMap<Long, Integer>();
			otiCountMap.put(ott.getId(), sportOrderService.getOpenTimeItemCount(ott.getId(), OpenTimeItemConstant.STATUS_NEW, DateUtil.format(DateUtil.addMinute(ott.getPlaydate(), sport2Item.getLimitminutes()), "HH:mm")));
			for(OpenTimeTable otts : itemList){
				Date ottDate = otts.getPlaydate();
				if(otts.getPlaydate().equals(DateUtil.getCurDate()))
					ottDate = DateUtil.addMinute(DateUtil.currentTime(), sport2Item.getLimitminutes());
				Integer otiCount = sportOrderService.getOpenTimeItemCount(otts.getId(), OpenTimeItemConstant.STATUS_NEW, DateUtil.format(ottDate, "HH:mm"));
				otiCountMap.put(otts.getId(), otiCount);
			}
			model.put("otiCountMap", otiCountMap);
			daoService.saveObject(ott);
		}else{ //按人次、局数
			List<OpenTimeItem> newOtiList = new ArrayList<OpenTimeItem>();
			for (OpenTimeItem oti : otiList) {
				if(oti.hasStatusNew() && cur.before(oti.getValidtime())){
					SportField sp = daoService.getObject(SportField.class, oti.getFieldid());
					if(StringUtils.equals(sp.getStatus(), Status.Y)){
						newOtiList.add(oti);
					}
				}
			}
			Collections.sort(newOtiList, new PropertyComparator("hour", false, true));
			if(!newOtiList.isEmpty()){ 
				OpenTimeItem openTimeItem = newOtiList.get(0);
				List<String> timeList = SportOrderHelper.getStarttimeList(ott.getPlaydate(),openTimeItem);
				if(!timeList.isEmpty()){
					model.put("defaultStartTime", timeList.get(0));
				}
				if(ott.hasPeriod()){
					List<Integer> periodList = SportOrderHelper.getPeriodList(ott.getPlaydate(),openTimeItem);
					model.put("defaultEndTime", SportOrderHelper.getDefalutEndTime(ott.getPlaydate(),openTimeItem));
					model.put("periodList", periodList);
				}else if(ott.hasInning()){
					List<Long> fieldIdList = BeanUtil.getBeanPropertyList(newOtiList, Long.class, "fieldid", true);
					Map<Long, SportField> fieldMap = daoService.getObjectMap(SportField.class, fieldIdList);
					model.put("fieldMap", fieldMap);
				}
				model.put("ott", ott);
				model.put("timeList", timeList);
				model.put("curOti", openTimeItem);
			}
			model.put("otiList", newOtiList);
		}
	}
	
	@RequestMapping("/admin/sport/order/checkChangeOrderByField.xhtml")
	public String checkChangeTicketOrder(Long ottid, String tradeno, String fieldid, ModelMap model) {
		if(ottid==null) return showError(model, "请重新选择场次！");
		List<Long> fieldidList = BeanUtil.getIdList(fieldid, ",");
		if(fieldidList.size()==0) {
			return showJsonError(model, "请选择场地！");
		}
		GewaOrder oldSportOrder = daoService.getObjectByUkey(GewaOrder.class, "tradeNo", tradeno);
		if (oldSportOrder == null) {
			return showJsonError(model, "订单不存在！");
		}
		OpenTimeTable ott = daoService.getObject(OpenTimeTable.class, ottid);
		if(ott == null) {
			return showJsonError(model, "场次不存在！");
		}
		return showJsonSuccess(model, ""+oldSportOrder.getId());
	}
	
	@RequestMapping("/admin/sport/order/checkChangeOrderByPeriod.xhtml")
	public String checkChangeOrderByPeriod(Long otiid, String tradeno, ModelMap model) {
		GewaConfig gewaConfig = daoService.getObject(GewaConfig.class, ConfigConstant.CFG_PAUSE_SPORT);
		Timestamp cur = DateUtil.getCurFullTimestamp();
		Timestamp pause = Timestamp.valueOf(gewaConfig.getContent());
		if(cur.before(pause)){
			return showJsonError(model, "暂停售票至" + DateUtil.format(pause, "HH:mm"));
		}
		OpenTimeItem oti = daoService.getObject(OpenTimeItem.class, otiid);
		if(oti == null) {
			return showJsonError(model, "场地该时段不存在！");
		}
		if(!oti.hasStatusNew() || cur.after(oti.getValidtime())) {
			return showJsonError(model, "本场地时段不接受预订！");
		}
		OpenTimeTable ott = daoService.getObject(OpenTimeTable.class, oti.getOttid());
		if(ott == null) {
			return showJsonError(model, "场次不存在！");
		}
		Sport2Item sport2Item = sportService.getSport2Item(ott.getSportid(), ott.getItemid());
		if(!sport2Item.isOpen()) {
			return showJsonError(model, "请在开放时间内进行预订！");
		}
		if(!ott.isBooking()) {
			return showJsonError(model, "本场不接受预订！");
		}
		if(!ott.hasPeriod() || !oti.hasPeriod()) {
			return showJsonError(model, "非时间段场次或场地！");
		}
		GewaOrder oldSportOrder = daoService.getObjectByUkey(GewaOrder.class, "tradeNo", tradeno);
		if (oldSportOrder == null) {
			return showJsonError(model, "订单不存在！");
		}
		return showJsonSuccess(model, ""+oldSportOrder.getId());
	}
	
	@RequestMapping("/admin/sport/order/changeOrderByField.xhtml")
	public String changeOrderByField(Long ottid, String tradeno, String fieldid, ModelMap model) {
		if(ottid==null) return showJsonError(model, "请重新选择场次！");
		List<Long> fieldidList = BeanUtil.getIdList(fieldid, ",");
		if(fieldidList.size()==0) {
			return showJsonError(model, "请选择场地！");
		}
		SportOrder oldSportOrder = daoService.getObjectByUkey(SportOrder.class, "tradeNo", tradeno);
		if (oldSportOrder == null) {
			return showJsonError(model, "订单不存在！");
		}
		OpenTimeTable ott = daoService.getObject(OpenTimeTable.class, ottid);
		if(ott == null) {
			return showJsonError(model, "场次不存在！");
		}
		User user = getLogonUser();
		try {
			SportOrder newSportOrder = insteadSportOrderService.changeSportOrderByField(ott, oldSportOrder, fieldid, user);
			return showJsonSuccess(model, ""+newSportOrder.getId());
		} catch (OrderException e) {
			return showJsonError(model, e.getMsg());
		}
		//return "admin/sport/wide_confirmOrder.vm";
	}
	
	@RequestMapping("/admin/sport/confirmOrder.xhtml")
	public String confirmOrder(Long orderId, HttpServletRequest request, ModelMap model){
		SportOrder order = daoService.getObject(SportOrder.class, orderId);
		if(order == null) return showMessageAndReturn(model, request, "订单不存在！");
		addConfirmOrderData(order, model);
		return "admin/ticket/wide_confirmOrder.vm";
	}
	
	@RequestMapping("/admin/sport/order/changeOrderByPeriod.xhtml")
	public String changeOrderByPeriod(Long otiid, String tradeno, String starttime, Integer time, Integer quantity, ModelMap model) {
		User user = getLogonUser();
		SportOrder oldSportOrder = daoService.getObjectByUkey(SportOrder.class, "tradeNo", tradeno);
		OpenTimeItem oti = daoService.getObject(OpenTimeItem.class, otiid);
		OpenTimeTable ott = daoService.getObject(OpenTimeTable.class, oti.getOttid());
		try {
			SportOrder newSportOrder = insteadSportOrderService.changeSportOrderByPeriod(ott, oldSportOrder, oti, starttime, quantity, time, user);
			ErrorCode code = addConfirmOrderData(newSportOrder, model);
			if(!code.isSuccess()) {
				throw new OrderException(ApiConstant.CODE_SIGN_ERROR, "订单确认失败！");
			}
		} catch (OrderException e) {
			return forwardMessage(model, e.getMsg());
		}
		return "admin/sport/wide_confirmOrder.vm";
	}
	
//	@RequestMapping("/admin/sport/order/confirmSuccess.xhtml")
//	public String confirmSuccess(Long orderId, ModelMap model){
//		SportOrder order = daoService.getObject(SportOrder.class, orderId);
//		OpenTimeTable ott = daoService.getObject(OpenTimeTable.class, order.getOttid());
////		if(order.isPaidFailure()){//订单待处理，很少出现
////			List<SellSeat> seatList = ticketOrderService.getOrderSeatList(order.getId());
////			try {
////				ticketOrderService.processOrderPay(order, opi, seatList);
////			} catch (OrderException e) {
////				return showJsonError(model, "付款失败：" + e.getMsg());
////			}
////		}
//		User user = getLogonUser();
//		ErrorCode code = insteadSportOrderService.confirmSuccess(order, ott, user.getId(), false);
//		if(code.isSuccess()) return showJsonSuccess(model);
//		return showJsonError(model, code.getMsg());
//		
//	}
	
	private ErrorCode addConfirmOrderData(GewaOrder order, ModelMap model){
		model.put("order", order);
		SportOrder sorder = (SportOrder) order;
		Sport sport = daoService.getObject(Sport.class, sorder.getSportid());
		OpenTimeTable ott = daoService.getObject(OpenTimeTable.class,  sorder.getOttid());
		SportItem item = daoService.getObject(SportItem.class, sorder.getItemid());
		model.put("sport", sport);
		model.put("item", item);
		model.put("ott", ott);
		if(sorder.hasMemberCardPay()){
			MemberCardInfo memberCard = daoService.getObject(MemberCardInfo.class, sorder.getCardid());
			MemberCardType mct = daoService.getObject(MemberCardType.class, memberCard.getTypeid());
			model.put("memberCard", memberCard);
			model.put("mct", mct);
			Integer cardDue = order.getDue();
			if(mct.hasAmountCard() && mct.getDiscount()!=null){
				cardDue = Math.round(cardDue*mct.getDiscount()/100f);
			}
			model.put("cardDue", cardDue);
		}
		List<Discount> discountList = paymentService.getOrderDiscountList(order);
		model.put("discountList", discountList);
		Map<String, String> orderOtherinfo = VmUtils.readJsonToMap(order.getOtherinfo());
		model.put("orderOtherinfo", orderOtherinfo);
		return ErrorCode.SUCCESS;
	}
	
}
