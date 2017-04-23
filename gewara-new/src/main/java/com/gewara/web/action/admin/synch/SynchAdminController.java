package com.gewara.web.action.admin.synch;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.support.PropertyComparator;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.gewara.api.mobile.service.MobileService;
import com.gewara.constant.AdminCityContant;
import com.gewara.constant.GoodsConstant;
import com.gewara.constant.SmsConstant;
import com.gewara.constant.Status;
import com.gewara.constant.TagConstant;
import com.gewara.constant.TerminalConstant;
import com.gewara.constant.sys.ConfigConstant;
import com.gewara.constant.sys.LogTypeConstant;
import com.gewara.constant.sys.MongoData;
import com.gewara.constant.ticket.OrderConstant;
import com.gewara.json.SportUpGrade;
import com.gewara.json.machine.MachineAd;
import com.gewara.json.machine.UpMachine;
import com.gewara.model.acl.User;
import com.gewara.model.api.OrderResult;
import com.gewara.model.api.Synch;
import com.gewara.model.common.BaseInfo;
import com.gewara.model.common.GewaConfig;
import com.gewara.model.drama.Theatre;
import com.gewara.model.movie.Cinema;
import com.gewara.model.pay.GewaOrder;
import com.gewara.model.pay.GoodsOrder;
import com.gewara.model.pay.OrderNote;
import com.gewara.model.pay.SMSRecord;
import com.gewara.model.pay.SportOrder;
import com.gewara.model.pay.TicketOrder;
import com.gewara.model.sport.Sport;
import com.gewara.model.ticket.OpenPlayItem;
import com.gewara.mongo.MongoService;
import com.gewara.service.SynchService;
import com.gewara.service.order.OrderQueryService;
import com.gewara.support.ErrorCode;
import com.gewara.support.ServiceHelper;
import com.gewara.untrans.GewaPicService;
import com.gewara.untrans.UntransService;
import com.gewara.untrans.monitor.OrderMonitorService;
import com.gewara.untrans.monitor.RoleTag;
import com.gewara.untrans.order.BroadcastOrderService;
import com.gewara.util.BeanUtil;
import com.gewara.util.DateUtil;
import com.gewara.util.HttpResult;
import com.gewara.util.HttpUtils;
import com.gewara.util.JsonUtils;
import com.gewara.util.ValidateUtil;
import com.gewara.util.VmUtils;
import com.gewara.web.action.admin.BaseAdminController;
import com.gewara.web.util.PageUtil;

@Controller
public class SynchAdminController extends BaseAdminController{
	@Autowired@Qualifier("gewaPicService")
	private GewaPicService gewaPicService;
	@Autowired@Qualifier("orderMonitorService")
	private OrderMonitorService orderMonitorService;
	@Autowired@Qualifier("orderQueryService")
	private OrderQueryService orderQueryService;
	public void setOrderQueryService(OrderQueryService orderQueryService) {
		this.orderQueryService = orderQueryService;
	}
	@Autowired@Qualifier("synchService")
	private SynchService synchService;
	public void setSynchService(SynchService synchService) {
		this.synchService = synchService;
	}
	@Autowired@Qualifier("mongoService")
	private MongoService mongoService;
	public void setMongoService(MongoService mongoService) {
		this.mongoService = mongoService;
	}
	@Autowired@Qualifier("untransService")
	private UntransService untransService;
	@Autowired@Qualifier("broadcastOrderService")
	private BroadcastOrderService broadcastOrderService;
	@RequestMapping("/admin/synch/ajax/updateOrderResultValue.xhtml")
	public String updateOrderResultValue(String tradeno, String value, ModelMap model) {
		OrderResult orderResult = daoService.getObject(OrderResult.class, tradeno);
		GewaOrder order = daoService.getObjectByUkey(GewaOrder.class, "tradeNo", tradeno, false);
		if(order==null){
			return updateOrderOrderNoteValue(tradeno, value, model);
		}
		ErrorCode<String> code = isModifytime(order);
		if(!code.isSuccess()){
			return showJsonError(model, code.getMsg());
		}
		Timestamp curtime  = DateUtil.getMillTimestamp();
		if(orderResult!=null){
			orderResult.setResult(value);
			orderResult.setUpdatetime(curtime);
			daoService.saveObject(orderResult);
			if(!value.equals("O")){
				if(OrderResult.ORDERTYPE_TICKET.equals(orderResult.getOrdertype()) ||OrderResult.ORDERTYPE_DRAMA.equals(orderResult.getOrdertype())
						||OrderResult.ORDERTYPE_SPORT.equals(orderResult.getOrdertype())|| StringUtils.isBlank(orderResult.getOrdertype())) {
					order.setModifytime(curtime);
					daoService.saveObject(order);
				}else if(OrderResult.ORDERTYPE_MEAL.equals(orderResult.getOrdertype())){
					GoodsOrder goodsOrder = daoService.getObjectByUkey(GoodsOrder.class, "tradeNo", tradeno, false);
					goodsOrder.setModifytime(curtime);
					daoService.saveObject(goodsOrder);
				}
			}
		}else {
			order.setModifytime(curtime);
			daoService.saveObject(order);
		}
		List<OrderNote> onmList = daoService.getObjectListByField(OrderNote.class, "orderid", order.getId());
		updOnmList(order, onmList, value);
		Long userid = getLogonUser().getId();
		String content = userid + "["+getLogonUser().getNickname()+"]" + "同步类型：" + value +", tradeno="+tradeno;
		monitorService.saveSysWarn(GewaOrder.class, order.getId(), "后台用户同步订单", content, RoleTag.dingpiao);
		broadcastOrderService.broadcastOrder(order, TerminalConstant.FLAG_RESYNCH, value, userid);
		return showJsonSuccess(model);
	}
	@RequestMapping("/admin/synch/ajax/updateOrderOrderNoteValue.xhtml")
	public String updateOrderOrderNoteValue(String tradeno, String value, ModelMap model) {
		GewaOrder order = daoService.getObjectByUkey(GewaOrder.class, "tradeNo", tradeno, false);
		List<OrderNote> onmList = new ArrayList<OrderNote>();
		if(order==null){
			OrderNote orderNote = daoService.getObjectByUkey(OrderNote.class, "serialno", tradeno);
			if(orderNote==null){
				return showJsonError_NOT_FOUND(model);
			}
			onmList.add(orderNote);
			order = daoService.getObjectByUkey(GewaOrder.class, "tradeNo",  orderNote.getTradeno(), false);
		}else {
			onmList = daoService.getObjectListByField(OrderNote.class, "orderid", order.getId());
		}
		updOnmList(order, onmList, value);
		return showJsonSuccess(model);
	}
	private void updOnmList(GewaOrder order, List<OrderNote> onmList, String value){
		Timestamp curtime  = DateUtil.getMillTimestamp();
		for(OrderNote onm : onmList){
			Map<String, String> tmp = VmUtils.readJsonToMap(onm.getDescription());
			String synchtype = null;
			if(StringUtils.equals("U", value)){
				synchtype = "1";
			}else if(StringUtils.equals("D", value)){
				synchtype = "2";
			}else {
				synchtype = "0";
			}
			tmp.put("synchtype", synchtype);
			onm.setDescription(JsonUtils.writeMapToJson(tmp));
			onm.setModifytime(curtime);
		}
		daoService.saveObjectList(onmList);
		Long userid = getLogonUser().getId();
		String content = userid + "["+getLogonUser().getNickname()+"]" + "同步类型：" + value +", tradeno=" + order.getTradeNo();
		monitorService.saveSysWarn(GewaOrder.class, order.getId(), "后台用户同步订单", content, RoleTag.dingpiao);
		broadcastOrderService.broadcastOrder(order, TerminalConstant.FLAG_RESYNCH, value, userid);
	}
	@RequestMapping("/admin/synch/ajax/batchResultValue.xhtml")
	public String batchResultValue(String tradenos, String tradenolist, String value, ModelMap model){
		if(StringUtils.isBlank(tradenos) || StringUtils.isNotBlank(tradenolist)){
			model.put("tradenolist", tradenolist);
			return "admin/synch/batchSychOrder.vm";
		}
		List<String> tradenoList = Arrays.asList(StringUtils.split(tradenos, ","));
		for(String tradeno : tradenoList){
			updateOrderResultValue(tradeno, value, model);
		}
		return showJsonSuccess(model);
	}
	
	@RequestMapping("/admin/synch/qryOrderNote.xhtml")
	public String qryOrderNote(Long placeid, String order, Timestamp starttime, Timestamp endtime, ModelMap model){
		List<Theatre> theatreList = daoService.getObjectList(Theatre.class, "pinyin", true, 0, 5000);
		model.put("theatreList", theatreList);
		String vm = "admin/synch/qryNoteList.vm";
		if(placeid==null || starttime==null || endtime==null){
			return vm;
		}
		if(StringUtils.isBlank(order)) order = "addtime";
		DetachedCriteria qry = DetachedCriteria.forClass(OrderNote.class);
		qry.add(Restrictions.eq("placeid", placeid));
		qry.add(Restrictions.ge("addtime", starttime));
		qry.add(Restrictions.le("addtime", endtime));
		qry.addOrder(Order.desc(order));
		List<OrderNote> noteList = hibernateTemplate.findByCriteria(qry);
		model.put("noteList", noteList);
		return vm;
	}
	
	private ErrorCode<String> isModifytime(GewaOrder order){
		if(order==null){
			return ErrorCode.getFailure("订单不存在！");
		}
		if(order instanceof TicketOrder){
			OpenPlayItem opi = daoService.getObjectByUkey(OpenPlayItem.class, "mpid", ((TicketOrder)order).getMpid(), false);
			if(opi==null) {
				return ErrorCode.getFailure("场次不存在不能同步");
			}else {
				Timestamp curtime = DateUtil.getMillTimestamp();
				Timestamp ptime = DateUtil.addDay(opi.getPlaytime(), 30);
				if(ptime.before(curtime)){
					return ErrorCode.getFailure("场次已经过期不能同步");
				}
			}
		}
		return ErrorCode.SUCCESS;
	}
	@RequestMapping("/admin/synch/ajax/updateOrderMobile.xhtml")
	public String updateOrderMobile(String tradeno, String mobile, ModelMap model){
		GewaOrder order = daoService.getObjectByUkey(GewaOrder.class, "tradeNo", tradeno, false);
		if(order == null) return showJsonError_NOT_FOUND(model);
		if(!ValidateUtil.isMobile(mobile)) return showJsonError(model, "手机号不正确！");
		Long userid = getLogonUser().getId();
		String content = userid + "修改订单[" + tradeno + "]手机号："+ order.getMobile() + "-->" + mobile;
		order.setMobile(mobile);
		daoService.saveObject(order);
		List<SMSRecord> smsList = daoService.getObjectListByField(SMSRecord.class, "tradeNo", tradeno);
		Set<SMSRecord> smsSet = new HashSet<SMSRecord>(smsList);
		List<OrderNote> noteList = daoService.getObjectListByField(OrderNote.class, "orderid", order.getId());
		for(OrderNote note : noteList){
			String sm = StringUtils.substring(mobile, 7);
			Map<String, String> descMap = JsonUtils.readJsonToMap(note.getDescription());
			descMap.put("shortmobile", sm);
			descMap.put("mobile", sm);
			note.setMobile(mobile);
			note.setDescription(JsonUtils.writeMapToJson(descMap));
			smsSet.addAll(daoService.getObjectListByField(SMSRecord.class, "tradeNo", note.getSerialno()));
		}
		for(SMSRecord sms : smsSet){
			sms.setContact(mobile);
		}
		daoService.saveObjectList(smsSet);
		daoService.saveObjectList(noteList);
		orderMonitorService.addOrderChangeLog(order.getTradeNo(), "修改手机", content, userid);
		broadcastOrderService.broadcastOrder(order, TerminalConstant.FLAG_MODMOBILE, mobile.substring(7), userid);
		return showJsonSuccess(model);
	}
	@RequestMapping("/admin/synch/ajax/reSendSms.xhtml")
	public String reSendSms(String tradeno, String channel, ModelMap model){
		GewaOrder order = daoService.getObjectByUkey(GewaOrder.class, "tradeNo", tradeno, false);
		if(order == null) return showJsonError_NOT_FOUND(model);
		if(!order.isPaidSuccess()){
			return showJsonError(model, "订单状态不正确:" + order.getStatus());
		}
		List<SMSRecord> smsList = daoService.getObjectListByField(SMSRecord.class, "tradeNo", tradeno);
		List<OrderNote> noteList = daoService.getObjectListByField(OrderNote.class, "orderid", order.getId());
		for(OrderNote note : noteList){
			smsList.addAll(daoService.getObjectListByField(SMSRecord.class, "tradeNo", note.getSerialno()));
		}
		ErrorCode code = null;
		for(SMSRecord sms : smsList){
			if(!StringUtils.equals(sms.getSmstype(), SmsConstant.SMSTYPE_NOW)){
				continue;
			}
			if(StringUtils.isBlank(channel)){
				code = untransService.sendMsgAtServer(sms, true);
			}else {
				code = untransService.sendMsgAtServer(sms, channel, true);
			}
			if(code.isSuccess()) return showJsonSuccess(model);
		}
		if(!code.isSuccess()) return showJsonError(model, code.getMsg());
		return showJsonSuccess(model);
	}
	
	
	@RequestMapping("/admin/synch/synchManager.xhtml")
	public String synchManager(Timestamp starttime, Timestamp endtime, String tag,Boolean isSynch, ModelMap model,
			String citycode, HttpServletRequest request){
		if(StringUtils.isBlank(tag))tag = Synch.TGA_CINEMA;
		Timestamp nowtime = new Timestamp(System.currentTimeMillis());
		if(starttime==null) starttime = DateUtil.getBeginningTimeOfDay(nowtime);
		Timestamp stime = DateUtil.addDay(starttime, 4);
		if(endtime==null) {
			endtime = DateUtil.getLastTimeOfDay(nowtime);
		}else if(endtime.after(stime)){
			return this.showError(model, "时间跨度不能超过4天");
		}
		if(isSynch == null){
			isSynch = true;
		}
		Class clazz = ServiceHelper.getPalceClazz(tag);
		List<Synch> synchList = new ArrayList<Synch>();
		if(StringUtils.isBlank(citycode)){
			citycode = getDefaultCitycode(request);
		}
		model.put("citycode",citycode);
		model.put("cityMap", AdminCityContant.getCitycode2CitynameMap());
		if(isSynch){
			synchList = synchService.getSynchListByCityCode(citycode, clazz);
		}else{
			synchList = synchService.getOffNetworkSynchListByCityCode(citycode, clazz);
		}
		Timestamp curtime = new Timestamp(System.currentTimeMillis());
		Map<Long,Object> nameList = new HashMap<Long, Object>();
		//订单数量
		Map<Long,Long> orderNumMap = new HashMap<Long, Long>();
		//订单票数
		Map<Long,Long> ticketNumMap = new HashMap<Long, Long>();
		Map<Long,Long> synchOrderNumMap = new HashMap<Long, Long>();
		Map<Long,Long> synchTicketNumMap = new HashMap<Long, Long>();
		Map<Long,Integer> ticketGetNumMap = new HashMap<Long, Integer>();
		Map<Long,Long> overTicketNumMap = new HashMap<Long, Long>();
		Map<Long,Long> totalOrderNumMap = new HashMap<Long, Long>();//影院实际应有订单数
		Map<Long,Long> totalTicketNumMap = new HashMap<Long, Long>();//影院实际应有订单票数
		Map<Long,Integer> synchTotalOrderNumMap = new HashMap<Long, Integer>();//实际同步订单数
		Map<Long,List> peakOpiMap = new HashMap<Long, List>();//高峰场次
		Map<Long,List> peakPeriodMap = new HashMap<Long, List>();//高峰时段
		for(Synch synch:synchList){
			if(StringUtils.isBlank(synch.getTicketnum())) { 
				synch.setTicketnum("0,0,0,0,0,0");
			}else if(synch.getTicketnum().split(",").length<6) {
				synch.setTicketnum(synch.getTicketnum()+",0,0,0,0,0");
			}
			Long placeid = synch.getCinemaid();
			if(StringUtils.equals(synch.getTag(),Synch.TGA_CINEMA)){
				Cinema cinema = daoService.getObject(Cinema.class, placeid);
				nameList.put(placeid, cinema);
				//这段时间内的订单，交易成功的订单数量和票数
				Map<String, Long> map1 = synchService.getGewaNumByTimeCinema(placeid, starttime, endtime);
				Long orderNum = map1.get("count");
				orderNumMap.put(placeid, orderNum);
				Long ticketNum = map1.get("quantity");
				ticketNumMap.put(placeid, ticketNum);
				
				//这段时间内的订单，已经取过票的数量和票数
				Map<String, Long> map2 = synchService.getSynchNumByTimeCinema(placeid, starttime, endtime);
				Long synchOrderNum = map2.get("count");
				synchOrderNumMap.put(placeid, synchOrderNum);
				Long synchTicketNum = map2.get("ticketnum");
				synchTicketNumMap.put(placeid, synchTicketNum);
				//从现在往后60天，所有交易成功的订单【应用订单】
				//Timestamp endtime2 = DateUtil.addDay(curtime, 60);
				map1 = synchService.getGewaNumByTimeCinema(placeid, starttime, endtime);
				totalOrderNumMap.put(placeid, map1.get("count"));
				totalTicketNumMap.put(placeid,map1.get("quantity"));
				//这段时间内容取过票的数量
				Integer ticketGetNum = synchService.getTicketGetNumByTimeCinema(starttime, endtime, placeid);
				ticketGetNumMap.put(placeid, ticketGetNum);
				List<OpenPlayItem> opiList = synchService.getSynchPeakOpi(placeid, curtime, DateUtil.addDay(curtime, 2));
				peakOpiMap.put(placeid, opiList);
				peakPeriodMap.put(placeid, synchService.getSynchPeakPeriod(placeid,curtime, DateUtil.addDay(curtime, 2),tag));
				Map<String, Long> synchTotalOrderNum = synchService.getSynchTotalOrderNumByTimeCinema(placeid, starttime, endtime);
				synchTotalOrderNumMap.put(placeid,synchTotalOrderNum.get("count").intValue());
				overTicketNumMap.put(placeid, synchTotalOrderNum.get("ticketnum"));
			}else if(StringUtils.equals(synch.getTag(),TagConstant.TAG_DRAMA)){
				Theatre theatre = daoService.getObject(Theatre.class, placeid);
				nameList.put(placeid, theatre);
				
				Map<String, Long> map1 = synchService.getOrderNoteCountByPlaceid(placeid, starttime, endtime);
				Long orderNum = map1.get("count");
				orderNumMap.put(placeid, orderNum);
				Long ticketNum = map1.get("quantity");
				ticketNumMap.put(placeid, ticketNum);
				
				//这段时间内的订单，已经取过票的数量和票数
				Map<String, Long> map2 = synchService.getOrderNoteSynchNumByPlaceid(placeid, starttime, endtime);
				Long synchOrderNum = map2.get("count");
				synchOrderNumMap.put(placeid, synchOrderNum);
				Long synchTicketNum = map2.get("ticketnum");
				synchTicketNumMap.put(placeid, synchTicketNum);
				
				//这段时间内容取过票的数量
				Integer ticketGetNum = synchService.getOrderNoteGetNumByPlaceid(placeid, starttime, endtime);
				ticketGetNumMap.put(placeid, ticketGetNum);
				
				//
				//从现在往后60天，所有交易成功的订单【应用订单】
				//Timestamp endtime2 = DateUtil.addDay(curtime, 60);
				map1 = synchService.getOrderNoteNumByPlaceid(placeid, starttime, endtime);
				totalOrderNumMap.put(placeid, map1.get("count"));
				totalTicketNumMap.put(placeid,map1.get("quantity"));
				//从现在往后60天，所有交易成功的订单，已经同步的订单数量
				Map<String, Long> synchTotalOrderNum = synchService.getOrderNoteSynchTotalNumByPlaceid(placeid, starttime, endtime);
				synchTotalOrderNumMap.put(placeid, synchTotalOrderNum.get("count").intValue());
				overTicketNumMap.put(placeid, synchTotalOrderNum.get("ticketnum"));
				peakOpiMap.put(placeid, synchService.getSynchPeakOdi(placeid, curtime, DateUtil.addDay(curtime, 2)));
				peakPeriodMap.put(placeid, synchService.getSynchPeakPeriod(placeid,curtime, DateUtil.addDay(curtime, 2),tag));
			}
		}
		model.put("isSynch",isSynch);
		model.put("ticketGetNumMap", ticketGetNumMap);
		model.put("orderNumMap", orderNumMap);
		model.put("ticketNumMap", ticketNumMap);
		model.put("synchOrderNumMap", synchOrderNumMap);
		model.put("synchTicketNumMap", synchTicketNumMap);
		model.put("overTicketNumMap", overTicketNumMap);
		model.put("synchTotalOrderNumMap", synchTotalOrderNumMap);
		model.put("peakOpiMap",peakOpiMap);
		model.put("peakPeriodMap",peakPeriodMap);
		model.put("starttime", starttime);
		model.put("endtime", endtime);
		model.put("nameList", nameList);
		model.put("synchList", synchList);
		model.put("user", getLogonUser());
		model.put("totalOrderNumMap", totalOrderNumMap);
		model.put("totalTicketNumMap",totalTicketNumMap);
		return "admin/synch/synchManager.vm";
		
	}
	
	@RequestMapping("/admin/synch/todo.xhtml")
	public String todo(String tradeno, HttpServletRequest request, ModelMap model){
		todoSynch(tradeno, request, model);
		return "admin/synch/todo.vm";
	}
	
	private void todoSynch(String tradeno, HttpServletRequest request, ModelMap model){
		String citycode = getAdminCitycode(request);
		dbLogger.warn(citycode);
		List<OrderResult> orderResultList = synchService.getToDoOrderList(citycode);
		if(StringUtils.isNotBlank(tradeno)) {
			OrderResult orderResult = daoService.getObject(OrderResult.class, tradeno);
			if(orderResult!=null) orderResultList.add(orderResult);
		}
		Map<String, GewaOrder> gewaOrderMap = new HashMap<String, GewaOrder>();
		for(OrderResult orderResult:orderResultList){
			gewaOrderMap.put(orderResult.getTradeno(), daoService.getObjectByUkey(GewaOrder.class, "tradeNo", orderResult.getTradeno(), false));
		}
		model.put("gewaOrderMap", gewaOrderMap);
		model.put("orderResultList", orderResultList);
	}
	
	@RequestMapping("/admin/synch/synchTradeNo.xhtml")
	public String synchTradeNo(String tradenos, HttpServletRequest request, ModelMap model){
		String[] tradeNoArr = StringUtils.split(tradenos,",");
		String errMsg = null;
		for (String tradeNo : tradeNoArr) {
			if(StringUtils.isBlank(tradeNo))continue;
			errMsg = this.tradeNoSearch(tradeNo, model);
			if(StringUtils.isNotBlank(errMsg))this.showJsonError(model, errMsg);
			this.todoSynch(tradeNo, request, model);
		}
		return this.showJsonSuccess(model);
	}
	
	@RequestMapping("/admin/synch/tradeSearch.xhtml")
	public String tradeSearch(String tradeno, HttpServletRequest request, ModelMap model){
		if(StringUtils.isBlank(tradeno)) return todo(null, request, model);
		tradeno = StringUtils.trim(tradeno);
		String errMsg = this.tradeNoSearch(tradeno, model);
		if(errMsg != null)return this.showError(model, errMsg);
		return todo(tradeno, request, model);
	}
	
	private String tradeNoSearch(String tradeno, ModelMap model){
		OrderResult orderResult = daoService.getObject(OrderResult.class, tradeno);
		if(orderResult==null) {
			GewaOrder order = daoService.getObjectByUkey(GewaOrder.class, "tradeNo", tradeno, false);
			if(order==null) return "订单不存在！";
			if(!order.getStatus().equals("paid_success")) return "非支付成功订单！";
			OrderResult orderResult2 = new OrderResult(tradeno, null);
			orderResult2.setResult("N");
			orderResult2.setIstake("N");
			if(order instanceof TicketOrder) {
				orderResult2.setOrdertype(OrderResult.ORDERTYPE_TICKET);
			}else if(order instanceof GoodsOrder){
				orderResult2.setOrdertype(OrderResult.ORDERTYPE_MEAL);
			}
			orderResult2.setUpdatetime(DateUtil.getMillTimestamp());
			daoService.saveObject(orderResult2);
		}else{
			orderResult.setResult("N");
			orderResult.setUpdatetime(DateUtil.getMillTimestamp());
			daoService.saveObject(orderResult);
		}
		model.put("orderResult", orderResult);
		return null;
	}
	
	@RequestMapping("/admin/synch/getNoSynchOrder.xhtml")
	public String getNoSynchOrder(Long synchid, ModelMap model){
		Synch synch = this.daoService.getObject(Synch.class, synchid);
		model.put("cinemaid", synch.getCinemaid());
		if(StringUtils.equals(Synch.TGA_DRAMA, synch.getTag())){
			List<OrderNote> orderList = synchService.getOrderNoteNoSynchOrderByPlaceid(synch.getCinemaid());
			model.put("orderList", orderList);
			return "admin/synch/getNoSynchOrder2.vm";
		}else{
			List<GewaOrder> orderList = synchService.getNoSynchOrderByCinema(synch.getCinemaid());
			model.put("orderList", orderList);
			return "admin/synch/getNoSynchOrder.vm";
		}
	}
	@RequestMapping("/admin/synch/getOrderResult.xhtml")
	public String getOrderResult(Timestamp starttime, Timestamp endtime, Long synchid, ModelMap model){
		Synch synch = this.daoService.getObject(Synch.class, synchid);
		model.put("starttime", starttime);
		model.put("endtime", endtime);
		model.put("cinemaid", synch.getCinemaid());
		if(StringUtils.equals(Synch.TGA_DRAMA, synch.getTag())){
			List<OrderNote> orderList = synchService.getOrderNoteNotGetOrderByPlaceid(starttime, endtime, synch.getCinemaid());
			model.put("orderList", orderList);
			return "admin/synch/getOrderResult2.vm";
		}else{
			List<GewaOrder> orderList = synchService.getNotGetOrderByTimeCinema(starttime, endtime,synch.getCinemaid());
			model.put("orderList", orderList);
			return "admin/synch/getOrderResult.vm";
		}
	}
	@RequestMapping("/admin/synch/synchGoodsOrderManager.xhtml")
	public String synchGoodsOrderManager(Timestamp starttime, HttpServletRequest request, ModelMap model){
		String citycode = getAdminCitycode(request);
		if(starttime==null) {
			Timestamp curtime = DateUtil.getBeginTimestamp(new Date());
			starttime = DateUtil.addDay(curtime, -7);
		}
		String qry = "from Synch s where s.successtime>=? and exists(select g.id from Goods g where g.tag=? and g.relatedid=s.cinemaid)" +
				" and exists(select c.id from Cinema c where c.citycode=? and c.id=s.cinemaid and c.booking=?)";
		List<Synch> synchList = hibernateTemplate.find(qry, starttime, GoodsConstant.GOODS_TAG_BMH, citycode, Cinema.BOOKING_OPEN);
		Map<Long, Cinema> cinemaMap = new HashMap<Long, Cinema>();
		Map<Long, Integer> ordernumMap = new HashMap<Long, Integer>();
		Map<Long, Integer> synchnumMap = new HashMap<Long, Integer>();
		Map<Long, Integer> untakenumMap = new HashMap<Long, Integer>();
		Long cinemaid = null;
		for(Synch synch:synchList){
			cinemaid = synch.getCinemaid();
			cinemaMap.put(cinemaid, daoService.getObject(Cinema.class, cinemaid));
			ordernumMap.put(cinemaid, getOrderNumByCinemaid(cinemaid));
			synchnumMap.put(cinemaid, getSynchNumByTimeCinemaid(cinemaid));
			untakenumMap.put(cinemaid, getUnTakenumByCinemaid(cinemaid));
		}
		model.put("synchList", synchList);
		model.put("cinemaMap", cinemaMap);
		model.put("ordernumMap", ordernumMap);
		model.put("synchnumMap", synchnumMap);
		model.put("untakenumMap", untakenumMap);
		model.put("starttime", starttime);
		return "admin/synch/synchGoodsOrderManager.vm";
	}
	//总订单数量
	public Integer getOrderNumByCinemaid(Long cinemaid) {
		String hql = "select count(*) from GoodsOrder t where t.status =? and t.placeid=?";
		List list1 = hibernateTemplate.find(hql, OrderConstant.STATUS_PAID_SUCCESS, cinemaid);
		return Integer.parseInt(list1.get(0)+"");
	}
	//已同步的数量
	public Integer getSynchNumByTimeCinemaid(Long cinemaid) {
		String hql = "select count(*) from OrderResult s where s.ordertype=? " +
				 		 "and exists(select t.id from GoodsOrder t where t.tradeNo = s.tradeno and t.placeid=?)";
		List list1 = hibernateTemplate.find(hql, OrderResult.ORDERTYPE_MEAL, cinemaid);
		return Integer.parseInt(list1.get(0)+"");
	}
	//未取票的订单
	public Integer getUnTakenumByCinemaid(Long cinemaid) {
		String hql = "select count(*) from OrderResult s where s.ordertype=? and s.taketime is null " +
						 "and exists(select t.id from GoodsOrder t where s.tradeno = t.tradeNo and t.placeid=?)";
		List list1 = hibernateTemplate.find(hql, OrderResult.ORDERTYPE_MEAL, cinemaid);
		return Integer.parseInt(list1.get(0)+"");
	}
	@RequestMapping("/admin/synch/getUnTakeGoodsOrderList.xhtml")
	public String getUnTakeGoodsOrderList(Long cinemaid, ModelMap model){
		List<GewaOrder> orderList = new ArrayList<GewaOrder>();
		String hql = "from GoodsOrder t where t.status=? and t.placeid=? " +
							"and not exists(select r.tradeno from OrderResult r " +
										"where r.taketime!=null and r.ordertype=? and r.tradeno=t.tradeNo)";
		List<GoodsOrder> goodsOrderList = hibernateTemplate.find(hql, OrderConstant.STATUS_PAID_SUCCESS, cinemaid, OrderResult.ORDERTYPE_MEAL);
		orderList.addAll(goodsOrderList);
		Collections.sort(orderList, new PropertyComparator("addtime", false, false));
		model.put("orderList", orderList);
		model.put("cinemaid", cinemaid);
		return "admin/synch/unTakeGoodsOrderList.vm";
	}
	@RequestMapping("/admin/synch/getBatchOrderList.xhtml")
	public String getBatchOrderList(String idListStr, ModelMap model){
		List<Long> mpidList = BeanUtil.getIdList(idListStr, ",");
		List<TicketOrder> orderList = new ArrayList<TicketOrder>();
		for(Long mpid : mpidList){
			List<TicketOrder> list = orderQueryService.getTicketOrderListByMpid(mpid, OrderConstant.STATUS_PAID_SUCCESS);
			orderList.addAll(list);
		}
		model.put("orderList", orderList);
		return "admin/synch/orderList.vm";
	}
	@RequestMapping("/admin/synch/batchSysnch.xhtml")
	public String batchSysnch(String idListStr, ModelMap model){
		model.put("tmp", true);	
		model.put("idListStr", idListStr);
		return "admin/synch/batchSynch.vm";
	}
	@RequestMapping("/admin/synch/queryCinemaList.xhtml")
	public String queryMpi(HttpServletRequest request, ModelMap model){
		String citycode = getAdminCitycode(request);
		String qry = "select new map(c.id as cinemaid, c.name as name) from Cinema c " +
				"where c.citycode=? and c.booking=? and " +
				"exists(select p.id from CinemaProfile p where p.id=c.id))";
		List<Map> cinemaList = hibernateTemplate.find(qry, citycode, Cinema.BOOKING_OPEN);
		model.put("cinemaList", cinemaList);
		return "admin/synch/cinemaList.vm";
	}
	@RequestMapping("/admin/synch/queryMpi.xhtml")
	public String queryMpi(@RequestParam Long cinemaid, String datetype, Date playdate, ModelMap model){
		if(playdate==null) return forwardMessage(model, "请选择日期！");
		Timestamp time1 = DateUtil.getBeginTimestamp(playdate);
		Timestamp time2 = DateUtil.getLastTimeOfDay(time1);
		List<Long> mpidList = new ArrayList<Long>();
		String qry = "select o.mpid from OpenPlayItem o where o.cinemaid=? and o.playtime>=? and o.playtime<=?";
		if(StringUtils.equals(datetype, "add")){
			qry = "select distinct t.mpid from TicketOrder t where t.cinemaid=? and t.status=? and t.addtime>=? and t.addtime<=?";
			mpidList = hibernateTemplate.find(qry, cinemaid, OrderConstant.STATUS_PAID_SUCCESS, time1, time2);
		}else {
			mpidList = hibernateTemplate.find(qry, cinemaid, time1, time2);
		}
		
		model.put("idListStr", StringUtils.join(mpidList, ","));
		return "redirect:/admin/synch/batchSysnch.xhtml";
	}
	@RequestMapping("/admin/synch/queryGoodsOrderList.xhtml")
	public String queryGoodsOrderList(@RequestParam Long goodsid, @RequestParam Date adddate, ModelMap model){
		if(adddate==null) return forwardMessage(model, "请选择日期！");
		Timestamp time1 = DateUtil.getBeginTimestamp(adddate);
		Timestamp time2 = DateUtil.getLastTimeOfDay(time1);
		String qry = "from GoodsOrder g where g.goodsid=? and g.status=? and g.addtime>=? and g.addtime<=?";
		List<GoodsOrder> orderList = hibernateTemplate.find(qry, goodsid, OrderConstant.STATUS_PAID_SUCCESS, time1, time2);
		model.put("orderList", orderList);
		return "admin/synch/goodsOrderList.vm";
	}
	
	@RequestMapping("/admin/synch/saveMoviePicUrl.xhtml")
	public String savePic(String successFile,String remark,String display,String id) throws IOException{
		if(StringUtils.isBlank(successFile))return "redirect:/admin/synch/getMoviePic.xhtml";
		User user = this.getLogonUser();
		if(StringUtils.isEmpty(id)){
			id = "pic"+System.currentTimeMillis();
		} else {
			mongoService.removeObjectById(MongoData.NS_TICKET_MACHINE_IMAGES, "id", id);
		}
		Map toSave = new HashMap();
		String filePath = "images/machine/" + DateUtil.format(new Date(), "yyyyMM") + "/";
		String filename = gewaPicService.moveRemoteTempTo(user.getId(), "machine", null, filePath, successFile);//将文件移动到正式文件夹
		filename = StringUtils.startsWith(filename, "/") ? filename.replaceFirst("/", "") : filename;
		toSave.put(MongoData.SYSTEM_ID, MongoData.buildId());
		toSave.put("id", id);
		toSave.put("url", filename);
		toSave.put("remark", remark);
		toSave.put("display", display);
		toSave.put("updatetime", DateUtil.formatTimestamp(DateUtil.getMillTimestamp()));
		mongoService.saveOrUpdateMap(toSave, "id", MongoData.NS_TICKET_MACHINE_IMAGES, false, true);
		return "redirect:/admin/synch/getMoviePic.xhtml";
	}
	
	@RequestMapping("/admin/synch/getMoviePic.xhtml")
	public String getMoviePicList(ModelMap model){
		Map<String, Object> qparam = new HashMap<String, Object>();
		List<Map> map = mongoService.find(MongoData.NS_TICKET_MACHINE_IMAGES, qparam);
		model.put("picMap", map);
		return "admin/synch/moviePicList.vm";
	}
	
	@RequestMapping("/admin/synch/delMoviePicUrl.xhtml")
	public String delMoviePicList(String id,ModelMap model){
		mongoService.removeObjectById(MongoData.NS_TICKET_MACHINE_IMAGES, "id", id);
		return this.showJsonSuccess(model, id);
	}

	@RequestMapping("/admin/synch/getMachineError.xhtml")
	public String getReportError(Timestamp startTime, Timestamp endTime,
			Long cinemaid,String errorCode,ModelMap model,Integer pageNo){
		if(pageNo == null)pageNo = 0;
		int rowsPerPage = 20;
		if(endTime==null){
			endTime = DateUtil.getMillTimestamp();
		}
		Query query =  new Query();
		if(startTime != null){ 
			Criteria criteria1 = new Criteria("addtime").gte(startTime).lte(endTime);
			query.addCriteria(criteria1);
		}
		if(cinemaid != null) {
			Criteria criteria1 = new Criteria("cinemaid").is(cinemaid);
			query.addCriteria(criteria1);
		}
		if(StringUtils.isNotBlank(errorCode)) { 
			Criteria criteria1 = new Criteria("errorCode").is(errorCode);
			query.addCriteria(criteria1);
		}
		List<Map> errorList = mongoService.find(MongoData.NS_TICKET_MACHINE_ERROR, query.getQueryObject(),"addtime",true,pageNo*rowsPerPage,rowsPerPage);
		int count = mongoService.getCount(MongoData.NS_TICKET_MACHINE_ERROR, query.getQueryObject());
		PageUtil pageUtil=new PageUtil(count,rowsPerPage,pageNo,"/admin/synch/getMachineError.xhtml");
		Map params=new HashMap();
		params.put("startTime", startTime);
		params.put("endTime", endTime);
		params.put("cinemaid", cinemaid);
		params.put("errorCode", errorCode);
		pageUtil.initPageInfo(params);
		model.put("pageUtil", pageUtil);
		model.put("errorList", errorList);
		return "admin/synch/machineErrorList.vm";
	}
	@RequestMapping("/admin/synch/delMachineError.xhtml")
	@ResponseBody
	public String getReportError(Integer day){
		if(day==null) day = 30;
		Query query =  new Query();
		Timestamp startTime = DateUtil.addDay(DateUtil.getMillTimestamp(), -day);
		query.addCriteria(new Criteria("addtime").lt(startTime));
		mongoService.removeObjectList(MongoData.NS_TICKET_MACHINE_ERROR, query.getQueryObject());
		return "success";
	}
	@RequestMapping("/admin/synch/delEmptyEquipment.xhtml")
	@ResponseBody
	public String delEmptyEquipment(){
		Map<String,Object> map = new HashMap<String, Object>();
		List<Map> mapList = this.mongoService.find(MongoData.NS_EQUIPMENTSTATUS,map);
		int i = 0;
		for(Map mapObj : mapList){
			String str = mapObj.get("id")+"";
			if((StringUtils.isBlank(mapObj.get("sportid")+"") || StringUtils.equalsIgnoreCase(mapObj.get("sportid")+"", "null"))&&
				(StringUtils.isBlank(mapObj.get("venueId")+"") || StringUtils.equalsIgnoreCase(mapObj.get("venueId")+"", "null"))){
				this.mongoService.removeObjectById(MongoData.NS_EQUIPMENTSTATUS, "id", str);
				i++;
			}
		}
		return i+"";
	}
	@RequestMapping("/admin/synch/getEquipmentStatus.xhtml")
	public String getEquipmentStatus(ModelMap model,Timestamp starttime,Timestamp endtime,String equipmentType){
		if(StringUtils.isBlank(equipmentType)) equipmentType="pos";
		Timestamp curtime = new Timestamp(System.currentTimeMillis());
		if(starttime==null) starttime = DateUtil.getBeginningTimeOfDay(curtime);
		Timestamp stime = DateUtil.addDay(starttime,3);
		if(endtime==null) {
			endtime = DateUtil.getLastTimeOfDay(curtime);
		}else if(endtime.after(stime)){
			return this.showError(model, "时间跨度不能超过3天");
		}
		model.put("starttime", starttime);
		model.put("endtime", endtime);
		Map<String,Object> map = new HashMap<String, Object>();
		map.put("equipmentType", equipmentType);
		List<Map> mapList = this.mongoService.find(MongoData.NS_EQUIPMENTSTATUS,map);
		long totalOrderNum =0;//总订单数
		int synchTotalOrderNum =0;//已同步订单数
		int noTakeOrderNum=0;//未取票订单数
		for(Map mapObj : mapList){
			if(StringUtils.equals(TagConstant.TAG_GYM, (String)mapObj.get("type"))){
				return forwardMessage(model, "健身不支持");
			}else{
				String str = mapObj.get("sportid")+"";
				if(StringUtils.isBlank(str) || StringUtils.equalsIgnoreCase(str, "null")){
					str = mapObj.get("venueId")+"";
				}
				Long gid = Long.parseLong(str);
				totalOrderNum = synchService.getGewaNumByTimeSport(gid, starttime, endtime).get("count");
				synchTotalOrderNum = synchService.getSynchTotalOrderNumByTimeSport(gid, starttime, endtime);
				List<GewaOrder> noTakeOrderList = synchService.getNotGetOrderByTimeSport(gid, starttime, endtime);
				if(noTakeOrderList!=null)
					noTakeOrderNum= noTakeOrderList.size();
			}
			String st=(String) mapObj.get("synchTime");
			Date syTime=DateUtil.parseDate(st, "yyyy-MM-dd HH:mm:ss");
			if(DateUtil.addHour(DateUtil.currentTime(), -1).after(syTime)){
				mapObj.put("overTime", true);
			}
			mapObj.put("totalOrderNum", totalOrderNum);
			mapObj.put("synchTotalOrderNum", synchTotalOrderNum);
			mapObj.put("noTakeOrderNum", noTakeOrderNum);
		}
		model.put("mapList", mapList);
		model.put("equipmentType", equipmentType);
		return "admin/synch/equipmentStatusList.vm";
	}
	
	@RequestMapping("/admin/synch/getNoSynchSportOrder.xhtml")
	public String getNoSynchSportOrder(ModelMap model,Long sportid,String tag,Timestamp starttime,Timestamp endtime){
		if(StringUtils.isNotEmpty(String.valueOf(sportid))){
			List<GewaOrder> orderList = null;
			if(StringUtils.equals(OrderResult.ORDERTYPE_GYM, tag)){
				return forwardMessage(model, "暂时不支持健身！");
			}else{
				orderList = synchService.getNoSynchOrderBySport(sportid,starttime,endtime);
			}
			model.put("tag", tag);
			model.put("orderList", orderList);
		}
		return "admin/synch/getNoSynchSportOrder.vm";
	}
	
	@RequestMapping("/admin/synch/getNoTakeSportOrder.xhtml")
	public String getNoTakeSportOrder(ModelMap model,Long sportid,String tag,Timestamp starttime,Timestamp endtime){
		if(StringUtils.isNotEmpty(String.valueOf(sportid))){
			List<GewaOrder> orderList = null;
			if(StringUtils.equals(OrderResult.ORDERTYPE_GYM, tag)){
				return forwardMessage(model, "暂时不支持健身！");
			}else {
				orderList = synchService.getNotGetOrderByTimeSport(sportid,starttime,endtime);
			}
			model.put("tag", tag);
			model.put("orderList", orderList);
		}
		return "admin/synch/getNoTakeSportOrder.vm";
	}
	
	@RequestMapping("/admin/synch/delEquipmentStatus.xhtml")
	public String delEquipmentStatus(String id, ModelMap model){
		mongoService.removeObjectById(MongoData.NS_EQUIPMENTSTATUS, "id", id);
		return this.showJsonSuccess(model);
	}
	
	@RequestMapping("/admin/synch/todoSportOrder.xhtml")
	public String todoSportOrder(){
		return "admin/synch/todoSportOrder.vm";
	}
	
	@RequestMapping("/admin/synch/searchSportOrder.xhtml")
	public String searchSportOrder(ModelMap model,String tradeno){
		if(StringUtils.isBlank(tradeno)) {
			return "admin/synch/todoSportOrder.vm";
		}
		tradeno = StringUtils.trim(tradeno);
		List<OrderResult> orderResultList = synchService.getToDoSportOrderList(tradeno);
		model.put("orderResultList", orderResultList);
		Map<String, GewaOrder> gewaOrderMap = new HashMap<String, GewaOrder>();
		for(OrderResult orderResult:orderResultList){
			gewaOrderMap.put(orderResult.getTradeno(), daoService.getObjectByUkey(GewaOrder.class, "tradeNo", orderResult.getTradeno(), false));
		}
		model.put("gewaOrderMap", gewaOrderMap);
		return "admin/synch/todoSportOrder.vm";
	}
	
	@RequestMapping("/admin/synch/synchOrderByTradeNo.xhtml")
	public String synchSportOrder(ModelMap model,String tradeno,String tag){
		tradeno = StringUtils.trim(tradeno);
		Map params = new HashMap();
		params.put("tradeno", tradeno);
		String PRIFIX = this.daoService.getObject(GewaConfig.class, ConfigConstant.CFG_CUS).getContent();
		String url = PRIFIX + "/api/synchTradeNo.xhtml";
		OrderResult or = daoService.getObject(OrderResult.class, tradeno);
		if(or==null){
			or = new OrderResult();
		}
		if(StringUtils.equals(OrderResult.ORDERTYPE_GYM, tag)){
			or.setTradeno(tradeno);
			or.setUpdatetime(DateUtil.getMillTimestamp());
			or.setIstake(Status.N);
			or.setOrdertype(OrderResult.ORDERTYPE_GYM);
			HttpResult result = HttpUtils.postUrlAsString(url, params);
			if(!result.isSuccess())this.showJsonError(model,result.getMsg());
			dbLogger.warn("/api/synchTradeNo.xhtml："+result.getMsg());
		}else{
			GewaOrder order = daoService.getObjectByUkey(GewaOrder.class, "tradeNo",tradeno, false);
			or.setTradeno(tradeno);
			or.setUpdatetime(DateUtil.getMillTimestamp());
			or.setIstake(Status.N);
			if(order != null) {
				if(order instanceof GoodsOrder){
					or.setOrdertype(OrderResult.ORDERTYPE_MEAL);
				}else if(order instanceof SportOrder){
					or.setOrdertype(OrderResult.ORDERTYPE_SPORT);
					HttpResult result = HttpUtils.postUrlAsString(url, params);
					if(!result.isSuccess())this.showJsonError(model,result.getMsg());
					dbLogger.warn("/api/synchTradeNo.xhtml："+result.getMsg());
				}
			}
		}
		this.daoService.saveObject(or);
		return this.showJsonSuccess(model);
	}
	private DetachedCriteria getSynchQuery(String monitor, String newsys){
		DetachedCriteria query = DetachedCriteria.forClass(Synch.class);
		if(StringUtils.isNotBlank(monitor)){
			query.add(Restrictions.eq("monitor", monitor));
		}
		if(StringUtils.isNotBlank(newsys)){
			query.add(Restrictions.eq("newsys", newsys));
		}
		return query;
	}
	//影院校验KEY维护 
	@RequestMapping("/admin/synch/getSynchList.xhtml")
	public String getSynchList(Long itemid, String monitor, String newsys, Integer pageNo, ModelMap model){
		List<Synch> synchList = new ArrayList<Synch>();
		if(itemid == null){
			if(pageNo == null) pageNo = 0;
			int rows = 20;
			int from = pageNo * rows;
			DetachedCriteria query = getSynchQuery(monitor, newsys);
			query.addOrder(Order.asc("cinemaid"));
			synchList = hibernateTemplate.findByCriteria(query, from, rows);
			int rowsCount = Integer.valueOf(hibernateTemplate.findByCriteria(getSynchQuery(monitor, newsys).setProjection(Projections.rowCount())).get(0)+"");
			PageUtil pageUtil = new PageUtil(rowsCount, rows, pageNo, "/admin/synch/getSynchList.xhtml", true, true);
			Map params = new HashMap();
			if(StringUtils.isNotBlank(monitor)){
				params.put("monitor", monitor);
			}
			if(StringUtils.isNotBlank(newsys)){
				params.put("newsys", newsys);
			}
			pageUtil.initPageInfo(params);
			model.put("pageUtil", pageUtil);
		}else{
			Synch synch = daoService.getObject(Synch.class, itemid);
			synchList.add(synch);
		}
		model.put("synchList", synchList);
		model.put("itemid", itemid);
		return "admin/synch/getSynchList.vm";
	}
	
	@RequestMapping("/admin/synch/updateSynchKey.xhtml")
	public String updateSynchKey(Long itemid, String synchkey, ModelMap model){
		if(itemid == null) return showJsonError(model, "参数错误！");
		Synch synch = daoService.getObject(Synch.class, itemid);
		synch.setSynchkey(synchkey);
		daoService.saveObject(synch);
		return showJsonSuccess(model);
	}
	
	@RequestMapping("/admin/synch/updateSynchMonitor.xhtml")
	public String updateSynchMonitor(Long itemid, String monitor, ModelMap model){
		if(itemid == null) return showJsonError(model, "参数错误！");
		Synch synch = daoService.getObject(Synch.class, itemid);
		synch.setMonitor(monitor);
		daoService.saveObject(synch);
		return showJsonSuccess(model);
	}
	@RequestMapping("/admin/synch/updateSynchNewsys.xhtml")
	public String updateSynchNewsys(Long itemid, String newsys, ModelMap model){
		if(itemid == null) return showJsonError(model, "参数错误！");
		Synch synch = daoService.getObject(Synch.class, itemid);
		synch.setNewsys(newsys);
		daoService.saveObject(synch);
		return showJsonSuccess(model);
	}
	@RequestMapping("/admin/synch/searchVenues.xhtml")
	public String searchVenues(String venueid,String type,ModelMap model){
		if(StringUtils.isNotBlank(venueid)){
			List<String> idLists = Arrays.asList(venueid.split(","));
			List<Long> idList = new ArrayList<Long>();
			for(String id : idLists){
				idList.add(Long.parseLong(id));
			}
			StringBuilder sb = new StringBuilder();
			if("cinema".equals(type)){
				List<Cinema> cinemas = daoService.getObjectList(Cinema.class, idList);
				for(Cinema cinema : cinemas){
					sb.append(cinema.getName());
					sb.append(",");
				}
			}else{
				List<Sport> sports = daoService.getObjectList(Sport.class, idList);
				for(Sport sport : sports){
					sb.append(sport.getName());
					sb.append(",");
				}
			}
			return showJsonSuccess(model,sb.toString());
		}
		return showJsonError(model,"参数错误！");
	}
	@RequestMapping("/admin/synch/machineAdList.xhtml")
	public String machineAdList(String venueid,ModelMap model){
		List<MachineAd> tmpList = mongoService.getObjectList(MachineAd.class, "addtime", false);
		if(StringUtils.isNotBlank(venueid)){
			List<MachineAd> adList = new ArrayList<MachineAd>();
			for(MachineAd ad : tmpList){
				if(StringUtils.isBlank(ad.getVenueid())){
					adList.add(ad);
				}else {
					List<String> idsList = Arrays.asList(ad.getVenueid().split(","));
					if(idsList.contains(venueid+"")) adList.add(ad);
				}
			}
			model.put("adList", adList);
		}else{
			model.put("adList", tmpList);
		}
		return "admin/synch/machineAdList.vm";
	}
	@RequestMapping("/admin/synch/getMachineAd.xhtml")
	public String getMachineAd(Long id, ModelMap model){
		MachineAd ad = mongoService.getObject(MachineAd.class, "id", id);
		return showJsonSuccess(model, BeanUtil.getBeanMap(ad));
	}
	/**
	 * @param id
	 * @param venueid
	 * @param adversion
	 * @param zipurl
	 * @param remark
	 * @param model
	 * @return
	 */
	@RequestMapping("/admin/synch/saveMachineAd.xhtml")
	public String saveMachineAd(Long id, String venueid, String adversion, String zipurl, String remark,String startTime,String endTime,
			String type,ModelMap model){
		MachineAd ad = null;
		if(id!=null){
			ad = mongoService.getObject(MachineAd.class, "id", id);
		}else {
			Timestamp curtime = DateUtil.getMillTimestamp();
			ad = new MachineAd();
			ad.setId(curtime.getTime());
			ad.setAddtime(DateUtil.formatTimestamp(DateUtil.getMillTimestamp()));
		}
		ad.setAdversion(adversion);
		ad.setVenueid(venueid);
		ad.setZipurl(zipurl);
		ad.setRemark(remark);
		ad.setType(type);
		ad.setStartTime(startTime);
		ad.setEndTime(endTime);
		ad.setNickName(getLogonUser().getNickname());
		mongoService.saveOrUpdateObject(ad, "id");
		return showJsonSuccess(model);
	}
	
	
	@RequestMapping("/admin/synch/deleteMachineAd.xhtml")
	public String deleteMachineAd(Long id, ModelMap model){
		mongoService.removeObjectById(MachineAd.class, "id", id);
		return showJsonSuccess(model);
	}
	
	@RequestMapping("/admin/synch/upMachineList.xhtml")
	public String upMachineList(ModelMap model){
		Map<Long, BaseInfo> baseMap = new HashMap<Long, BaseInfo>();
		List<UpMachine> upList = mongoService.getObjectList(UpMachine.class, "addtime", false);
		for(UpMachine up : upList){
			if(StringUtils.equals(up.getTag(), TagConstant.TAG_CINEMA)){
				Cinema cinema = daoService.getObject(Cinema.class, up.getPlaceid());
				baseMap.put(up.getPlaceid(), cinema);
			}else if(StringUtils.equals(up.getTag(), TagConstant.TAG_SPORT)){
				Sport sport = daoService.getObject(Sport.class, up.getPlaceid());
				baseMap.put(up.getPlaceid(), sport);
			}else if(StringUtils.equals(up.getTag(), TagConstant.TAG_THEATRE)){
				Theatre Theatre = daoService.getObject(Theatre.class, up.getPlaceid());
				baseMap.put(up.getPlaceid(), Theatre);
			} 
		}
		model.put("upList", upList);
		model.put("baseMap", baseMap);
		return "admin/synch/upMachineList.vm";
	}
	
	/**
	 * 保存一体机版本升级
	 */
	@RequestMapping("/admin/synch/saveUpMachine.xhtml")
	public String saveUpMachine(Long id, Long placeid, String tag, String version, ModelMap model){
		if(placeid==null || StringUtils.isBlank(tag) || StringUtils.isBlank(version)){
			return showJsonError(model, "数据不能为空！");
		}
		UpMachine ad = null;
		if(id!=null){
			ad = mongoService.getObject(UpMachine.class, "id", id);
		}else {
			Timestamp curtime = DateUtil.getMillTimestamp();
			ad = new UpMachine();
			ad.setId(curtime.getTime());
			ad.setAddtime(DateUtil.formatTimestamp(DateUtil.getMillTimestamp()));
		}
		ad.setVersion(version);
		ad.setPlaceid(placeid);
		ad.setTag(tag);
		mongoService.saveOrUpdateObject(ad, "id");
		return showJsonSuccess(model);
	}
	@RequestMapping("/admin/synch/getUpMachine.xhtml")
	public String getUpMachine(Long id, ModelMap model){
		UpMachine ad = mongoService.getObject(UpMachine.class, "id", id);
		return showJsonSuccess(model, BeanUtil.getBeanMap(ad));
	}
	@RequestMapping("/admin/synch/deleteUpMachine.xhtml")
	public String deleteUpMachine(Long id, ModelMap model){
		mongoService.removeObjectById(UpMachine.class, "id", id);
		return showJsonSuccess(model);
	}
	//TODO:确认功能在此处？
	@RequestMapping("/admin/synch/getSportUpGradeList.xhtml")
	public String getUpGradeList(ModelMap model){
		List<SportUpGrade> upGradeList = mongoService.getObjectList(SportUpGrade.class, "addTime", false, 0, 100);
		model.put("upGradeList", upGradeList);
		model.put("appSourcesMap", getAppSourceMap());
		return "admin/recommend/sport/upGradeList.vm";
	}
	
	@Autowired@Qualifier("mobileService")
	private MobileService mobileService;
	@RequestMapping("/admin/synch/pushMobileToGewamial.xhtml")
	@ResponseBody
	public String pushMobileToGewamail(Timestamp pushDate){
		String msg = "";
		try{
			Timestamp startTime = DateUtil.getBeginTimestamp(pushDate);
			Timestamp endTime = DateUtil.getEndTimestamp(pushDate);
			String hql = "select distinct mobile from GewaOrder where addtime >= ? and addtime < ? and status like ?";
			List<String> moblieList = hibernateTemplate.find(hql, startTime, endTime, OrderConstant.STATUS_PAID + "%");
			if(!moblieList.isEmpty()){
				mobileService.saveMobiles(moblieList);
				msg = "共推送手机数：" + moblieList.size();
				dbLogger.warnWithType(LogTypeConstant.LOG_TYPE_API, "共推送手机数：" + moblieList.size());
			}
		}catch(Exception e){
			msg = "推送手机号异常";
			dbLogger.errorWithType(LogTypeConstant.LOG_TYPE_API, "推送手机号异常", e);
		}
		return msg;
	}
	
	@RequestMapping("/admin/synch/getOrderListByTradeno.xhtml")
	public String deleteUpMachine(String tradeno, ModelMap model){
		List<OrderNote> noteList = daoService.getObjectListByField(OrderNote.class, "tradeno", tradeno);
		model.put("noteList", noteList);
		return "admin/synch/noteList.vm";
	}
	
	@RequestMapping("/admin/synch/updateNote.xhtml")
	public String deleteUpMachine(Long id, String description, ModelMap model){
		OrderNote note = daoService.getObject(OrderNote.class, id);
		note.setDescription(description);
		daoService.saveObject(note);
		User user = this.getLogonUser();
		dbLogger.warn("用户修改"+user.getNickname()+"修改orderNote:" + description);
		return showJsonSuccess(model);
	}
	
	@RequestMapping("/admin/synch/reSynchSuccesstime.xhtml")
	public String reSynchSuccesstime(Long cinemaid, Timestamp successtime, ModelMap model){
		Synch synch = daoService.getObject(Synch.class, cinemaid);
		synch.setSuccesstime(successtime);
		daoService.saveObject(synch);
		User user = this.getLogonUser();
		dbLogger.warn("用户" + user.getUsername()+ "修改synch successtime " + DateUtil.formatTimestamp(successtime));
		return showJsonSuccess(model);
	}
}