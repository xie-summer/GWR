package com.gewara.web.action.api;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.gewara.Config;
import com.gewara.constant.ApiConstant;
import com.gewara.constant.GoodsConstant;
import com.gewara.constant.SmsConstant;
import com.gewara.constant.Status;
import com.gewara.constant.sys.LogTypeConstant;
import com.gewara.constant.sys.MongoData;
import com.gewara.constant.ticket.OrderConstant;
import com.gewara.constant.ticket.OrderNoteConstant;
import com.gewara.constant.ticket.PartnerConstant;
import com.gewara.helper.order.GewaOrderHelper;
import com.gewara.model.api.ApiUser;
import com.gewara.model.api.OrderResult;
import com.gewara.model.api.Synch;
import com.gewara.model.api.SynchConfig;
import com.gewara.model.drama.DramaOrder;
import com.gewara.model.drama.Theatre;
import com.gewara.model.goods.Goods;
import com.gewara.model.movie.Cinema;
import com.gewara.model.pay.GewaOrder;
import com.gewara.model.pay.GoodsOrder;
import com.gewara.model.pay.OrderNote;
import com.gewara.model.pay.SMSRecord;
import com.gewara.model.pay.SportOrder;
import com.gewara.model.pay.TicketOrder;
import com.gewara.model.ticket.OpenPlayItem;
import com.gewara.model.ticket.SellSeat;
import com.gewara.service.OperationService;
import com.gewara.service.SynchService;
import com.gewara.support.ErrorCode;
import com.gewara.util.ApiUtils;
import com.gewara.util.DateUtil;
import com.gewara.util.JsonUtils;
import com.gewara.util.StringUtil;
import com.gewara.util.WebUtils;
import com.gewara.web.action.api2machine.BaseMachineApiController;
import com.gewara.web.filter.ApiAuthenticationFilter;
import com.gewara.xmlbind.api.OrderResponse;

@Controller
public class ApiSynchController extends BaseMachineApiController{

	@Autowired@Qualifier("synchService")
	private SynchService synchService;
	public void setSynchService(SynchService synchService) {
		this.synchService = synchService;
	}
	Long GEWA_TICKET = 50000040L;
	
	@RequestMapping("/api/synch/toSynchronize.xhtml")
	public String toSynchronize(/*String key, */Long cinemaid,Timestamp lasttime, ModelMap model){
		ApiAuth auth = ApiAuthenticationFilter.getApiAuth();
		ApiUser apiUser = auth.getApiUser();
		if(cinemaid==null) return getErrorXmlView(model, ApiConstant.CODE_PARAM_ERROR, "传递参数错误！");
		/**API调用判断 end**/
		Cinema cinema = daoService.getObject(Cinema.class, cinemaid);
		if(cinema == null) return getErrorXmlView(model, ApiConstant.CODE_DATA_ERROR, "没有该影院！");
		List<TicketOrder> orderList = synchService.getOrderListByCinemaIdAndLasttime(cinemaid, lasttime);
		model.put("cinema", cinema);
		Map<Long/*mpid*/,OpenPlayItem> opiMap = new HashMap<Long, OpenPlayItem>();
		Map<Long,String> checkMap = new HashMap<Long, String>();
		Map<Long, List<SellSeat>> seatMap = new HashMap<Long, List<SellSeat>>();
		for(TicketOrder order:orderList){
			if(opiMap.get(order.getMpid())==null){
				OpenPlayItem opi = daoService.getObjectByUkey(OpenPlayItem.class, "mpid", order.getMpid(), true);
				opiMap.put(order.getMpid(), opi);
			}
			List<SellSeat> seatList = ticketOrderService.getOrderSeatList(order.getId());
			seatMap.put(order.getId(), seatList);
			String checkpass = StringUtil.md5(order.getCheckpass()+apiUser.getPrivatekey());
			checkMap.put(order.getId(), checkpass);
		}
		model.put("orderList", orderList);
		model.put("opiMap", opiMap);
		model.put("checkMap", checkMap);
		model.put("seatMap", seatMap);
		model.put("GewaOrderHelper", new GewaOrderHelper());
		model.put("nowtime", DateUtil.addMinute(new Timestamp(System.currentTimeMillis()), -10));
		return getXmlView(model, "api/ticket/toSynchronize.vm");
	}
	@RequestMapping("/api/synch/downOrder.xhtml")
	public String downOrder(String key,String encryptCode,Long cinemaid, String ticketnum,String tag,Timestamp lastTime,Integer pageSize, HttpServletRequest request, ModelMap model){
		ApiAuth auth = ApiAuthenticationFilter.getApiAuth();
		ApiUser apiUser = auth.getApiUser();
		if(StringUtils.isNotBlank(encryptCode)){
			ApiAuth apiAuth = checkRights(encryptCode, ""+cinemaid, key);
			if(!apiAuth.isChecked()) return getErrorXmlView(model, apiAuth.getCode(), apiAuth.getMsg());
		}
		if(cinemaid==null) return getErrorXmlView(model, ApiConstant.CODE_PARAM_ERROR, "传递参数错误！");
		/**API调用判断 end**/
		model.put("tag", tag);
		String ip = WebUtils.getRemoteIp(request);
		return this.downTicketOrder(apiUser, cinemaid, ticketnum,lastTime,pageSize,ip, model);
	}
	
	
	
	private String downTicketOrder(ApiUser apiUser, Long cinemaid, String ticketnum, Timestamp lastTime, Integer pageSize, String ip, ModelMap model){
		Cinema cinema = daoService.getObject(Cinema.class, cinemaid);
		if(cinema == null) return getErrorXmlView(model, ApiConstant.CODE_DATA_ERROR, "没有该影院！");
		Synch synch = daoService.getObject(Synch.class, cinemaid);
		Timestamp cur = new Timestamp(System.currentTimeMillis());
		if(synch == null) synch = new Synch(cinemaid,Synch.TGA_CINEMA);
		synch.setTag(Synch.TGA_CINEMA);
		apiService.saveSynchWithCinema(synch, cur, null, ticketnum, ip);
		model.put("nowtime", cur);
		List<TicketOrder> orderList = null;
		if(pageSize == null && lastTime != null){
			orderList = synchService.getOrderListByCinemaIdAndLasttime(cinemaid, DateUtil.addMinute(lastTime, -5));
			if(!orderList.isEmpty()) cur = orderList.get(orderList.size() -1).getModifytime();
			model.put("nowtime", cur);
		}else if(pageSize == null || lastTime == null){
			orderList = synchService.getOrderListByCinemaIdAndLasttime(cinemaid, DateUtil.addMinute(synch.getSuccesstime(), -5));
		}else{
			orderList = synchService.getOrderListByCinemaIdAndLasttime(cinemaid, DateUtil.addMinute(lastTime, -5), pageSize);
			if(!orderList.isEmpty()) cur = orderList.get(orderList.size() -1).getModifytime();
			model.put("nowtime", cur);
		}
		model.put("cinema", cinema);
		Map<Long/*mpid*/,OpenPlayItem> opiMap = new HashMap<Long, OpenPlayItem>();
		Map<String, String> checkMap = new HashMap<String, String>();
		Map<String,String> orderMap = new HashMap<String, String>();
		Map<String,String> mobileMap = new HashMap<String, String>();
		List<TicketOrder> tmpList = new ArrayList<TicketOrder>(orderList);
		Map<Long, List<SellSeat>> seatMap = new HashMap<Long, List<SellSeat>>();
		for(TicketOrder order: tmpList){
			if(opiMap.get(order.getMpid())==null){
				OpenPlayItem opi = daoService.getObjectByUkey(OpenPlayItem.class, "mpid", order.getMpid(), true);
				opiMap.put(order.getMpid(), opi);
			}
			String checkpass = StringUtil.md5(order.getCheckpass()+apiUser.getPrivatekey());
			checkMap.put(order.getCheckpass(), checkpass);
			OrderResult orderResult = daoService.getObject(OrderResult.class, order.getTradeNo());
			String flag = "0";
			if(orderResult != null) flag = orderResult.getResult();
			if(StringUtils.isBlank(flag)) flag = "0";
			List<SellSeat> seatList = ticketOrderService.getOrderSeatList(order.getId());
			seatMap.put(order.getId(), seatList);
			orderMap.put(order.getTradeNo(), flag);
			mobileMap.put(order.getTradeNo(), StringUtils.substring(order.getMobile(), 7));
		}
		model.put("mobileMap", mobileMap);
		model.put("orderMap", orderMap);
		model.put("orderList", orderList);
		model.put("opiMap", opiMap);
		model.put("checkMap", checkMap);
		model.put("seatMap", seatMap);
		model.put("GewaOrderHelper", new GewaOrderHelper());
		//商品订单
		List<GoodsOrder> goodsOrderList = synchService.getGoodsOrderListByRelatedidAndLasttime(cinemaid, DateUtil.addMinute(synch.getSuccesstime(), -5),GoodsConstant.GOODS_TAG_BMH);
		Map<Long, Goods> goodsMap = new HashMap<Long, Goods>();
		for(GoodsOrder order : goodsOrderList){
			if(goodsMap.get(order.getGoodsid())==null) {
				Goods goods = daoService.getObject(Goods.class, order.getGoodsid());
				goodsMap.put(order.getGoodsid(), goods);
			}
			String checkpass = StringUtil.md5(order.getCheckpass()+apiUser.getPrivatekey());
			checkMap.put(order.getCheckpass(), checkpass);
			if(!orderMap.containsKey(order.getTradeNo())){ //伪造订单不存在
				OrderResult orderResult = daoService.getObject(OrderResult.class, order.getTradeNo());
				String flag = "0";
				if(orderResult != null) flag = orderResult.getResult();
				if(StringUtils.isBlank(flag)) flag = "0";
				orderMap.put(order.getTradeNo(), flag);
			}
			mobileMap.put(order.getTradeNo(), StringUtils.substring(order.getMobile(), 7));
		}
		model.put("goodsOrderList", goodsOrderList);
		model.put("goodsMap", goodsMap);
		
		return getXmlView(model, "api/ticket/toSynchronize.vm");
	}
	
	@RequestMapping("/api/synch/downSingleOrder.xhtml")
	public String downSingleOrder(String key,String encryptCode, String tradeNo, ModelMap model){
		ApiAuth auth = ApiAuthenticationFilter.getApiAuth();
		if(StringUtils.isNotBlank(encryptCode)){
			ApiAuth apiAuth = checkRights(encryptCode, key, tradeNo);
			if(!apiAuth.isChecked()) return getErrorXmlView(model, apiAuth.getCode(), apiAuth.getMsg());
		}
		if(StringUtils.isBlank(tradeNo)) return getErrorXmlView(model, ApiConstant.CODE_PARAM_ERROR, "传递参数错误！");
		GewaOrder order1 = daoService.getObjectByUkey(GewaOrder.class, "tradeNo", tradeNo, false);
		if(order1 == null) return getErrorXmlView(model, ApiConstant.CODE_DATA_ERROR, "没有该订单！");
		
		if(order1 instanceof TicketOrder){
			TicketOrder ticketOrder = (TicketOrder)order1;
			Cinema cinema = daoService.getObject(Cinema.class, ticketOrder.getCinemaid());
			model.put("cinema", cinema);
			List<TicketOrder> orderList = new ArrayList<TicketOrder>();
			orderList.add(ticketOrder);
			
			Map<Long/*mpid*/,OpenPlayItem> opiMap = new HashMap<Long, OpenPlayItem>();
			Map<String, String> checkMap = new HashMap<String, String>();
			Map<String,String> orderMap = new HashMap<String, String>();
			Map<String,String> mobileMap = new HashMap<String, String>();
			Map<Long, List<SellSeat>> seatMap = new HashMap<Long, List<SellSeat>>();
			
			if(opiMap.get(ticketOrder.getMpid())==null){
				OpenPlayItem opi = daoService.getObjectByUkey(OpenPlayItem.class, "mpid", ticketOrder.getMpid(), true);
				opiMap.put(ticketOrder.getMpid(), opi);
			}
			ApiUser synchUser = auth.getApiUser();
			String checkpass = StringUtil.md5(ticketOrder.getCheckpass()+synchUser.getPrivatekey());
			checkMap.put(ticketOrder.getCheckpass(), checkpass);
			OrderResult orderResult = daoService.getObject(OrderResult.class, ticketOrder.getTradeNo());
			String flag = "0";
			if(orderResult != null) flag = orderResult.getResult();
			if(StringUtils.isBlank(flag)) flag = "0";
			boolean removeTicket = false;
			if(flag.endsWith("T")){ //只操作Ticket
				flag = flag.substring(0,1);
			}else if(flag.endsWith("M")){ //只操作Meal
				removeTicket = true;
				flag = flag.substring(0,1);
			}
			List<SellSeat> seatList = ticketOrderService.getOrderSeatList(ticketOrder.getId());
			seatMap.put(ticketOrder.getId(), seatList);
			if(removeTicket){
				orderList.remove(ticketOrder);
			}else{
				orderMap.put(ticketOrder.getTradeNo(), flag);
			}
			mobileMap.put(ticketOrder.getTradeNo(), StringUtils.substring(ticketOrder.getMobile(), 7));
			
			Timestamp cur = new Timestamp(System.currentTimeMillis());
			model.put("order", ticketOrder);
			model.put("mobileMap", mobileMap);
			model.put("orderMap", orderMap);
			model.put("orderList", orderList);
			model.put("opiMap", opiMap);
			model.put("checkMap", checkMap);
			model.put("nowtime", cur);
			model.put("seatMap", seatMap);
			model.put("GewaOrderHelper", new GewaOrderHelper());
			String status = ApiConstant.getMappedOrderStatus(ticketOrder.getFullStatus());
			model.put("status", status);
		}
		return getXmlView(model, "api/ticket/synchTicketOrder.vm");
	}
	
	@RequestMapping("/api/order/notifyWarnCallback.xhtml")
	public String notifyWarnCallback(String key, String encryptCode, String tradeNo, String remark, ModelMap model){
		ApiAuth auth = ApiAuthenticationFilter.getApiAuth();
		ApiUser apiUser = auth.getApiUser();
		if(StringUtils.isNotBlank(encryptCode)){
			ApiAuth apiAuth = checkRights(encryptCode, key, tradeNo);
			if(!apiAuth.isChecked()) return getErrorXmlView(model, apiAuth.getCode(), apiAuth.getMsg());
		}
		if(StringUtils.isBlank(tradeNo)) return getErrorXmlView(model, ApiConstant.CODE_PARAM_ERROR, "传递参数错误！");
		if(StringUtils.isNotBlank(remark) && StringUtils.length(remark) > 100) return getErrorXmlView(model, ApiConstant.CODE_PARAM_ERROR, "备注字数不能超过100！");
		GewaOrder order1 = daoService.getObjectByUkey(GewaOrder.class, "tradeNo", tradeNo, false);
		if(order1 == null) return getErrorXmlView(model, ApiConstant.CODE_DATA_ERROR, "没有该订单！");
		
		Map params = new HashMap();
		params.put(MongoData.SYSTEM_ID, MongoData.buildId());
		params.put(MongoData.ACTION_ADDTIME, DateUtil.getCurFullTimestamp());
		params.put(MongoData.ACTION_REMARK, remark);
		params.put(MongoData.ACTION_STATUS, Status.N);
		params.put(MongoData.ACTION_TRADENO, tradeNo);
		params.put(MongoData.ACTION_TYPE, "order");	// 订单问题
		params.put(MongoData.ACTION_API_PARTNERNAME, apiUser.getPartnername());
		params.put(MongoData.ACTION_API_PARTNERKEY, apiUser.getPartnerkey());
		mongoService.saveOrUpdateMap(params, MongoData.SYSTEM_ID, MongoData.NS_API_WARNCALLBACK);
		
		model.put("successtime", DateUtil.getCurFullTimestamp());
		return getXmlView(model, "api/ticket/ticketStatus.vm");
	}
	
	@RequestMapping("/api/message/notifySuccessCallback.xhtml")
	public String notifySuccessCallback(String key, String encryptCode, String tradeNo, ModelMap model){
		ApiAuth auth = ApiAuthenticationFilter.getApiAuth();
		ApiUser apiUser = auth.getApiUser();
		if(StringUtils.isNotBlank(encryptCode)){
			ApiAuth apiAuth = checkRights(encryptCode, key, tradeNo);
			if(!apiAuth.isChecked()) return getErrorXmlView(model, apiAuth.getCode(), apiAuth.getMsg());
		}
		if(StringUtils.isBlank(tradeNo)) return getErrorXmlView(model, ApiConstant.CODE_PARAM_ERROR, "传递参数错误！");
		GewaOrder order1 = daoService.getObjectByUkey(GewaOrder.class, "tradeNo", tradeNo, false);
		if(order1 == null) return getErrorXmlView(model, ApiConstant.CODE_DATA_ERROR, "没有该订单！");
		
		if(order1.isPaidSuccess()){
			String opkey = key + tradeNo;
			if(!operationService.isAllowOperation(opkey, OperationService.ONE_DAY, 3)){
				return getErrorXmlView(model, ApiConstant.CODE_REPEAT_OPERATION, "同一订单24小时内最多允许发送3次！");
			}
			// 发送短信
			Map jsonmap = JsonUtils.readJsonToMap(apiUser.getOtherinfo());
			String msgTemplate = ""+jsonmap.get("msgTemplate");
			if(StringUtils.isBlank(msgTemplate)){
				return getErrorXmlView(model, ApiConstant.CODE_DATA_ERROR, "短信模板未设置！");
			}
			TicketOrder order = (TicketOrder)order1;
			OpenPlayItem opi = daoService.getObjectByUkey(OpenPlayItem.class, "mpid", order.getMpid(), true);
			List<SellSeat> seatList = ticketOrderService.getOrderSeatList(order.getId());
			ErrorCode<String> msg = messageService.getCheckpassMsg(msgTemplate, order, seatList, opi);
			if(!msg.isSuccess()) return getErrorXmlView(model, ApiConstant.CODE_DATA_ERROR, msg.getMsg());
			Timestamp curtime = DateUtil.getCurTruncTimestamp();
			SMSRecord sms = new SMSRecord(tradeNo, order1.getMobile(), msg.getRetval(), DateUtil.addMinute(curtime, -5), DateUtil.addHour(curtime, 3), SmsConstant.SMSTYPE_NOW_API);
			untransService.sendMsgAtServer(sms, false);
			operationService.updateOperation(opkey, OperationService.ONE_DAY, 3);
			model.put("successtime", DateUtil.getCurFullTimestamp());
		}else{
			return getErrorXmlView(model, ApiConstant.CODE_DATA_ERROR, "订单未成功！不能发送短信");
		}
		
		return getXmlView(model, "api/ticket/ticketStatus.vm");
	}
	
	@RequestMapping("/api/synch/synchReport.xhtml")
	public String synchReport(/*String key, */Long cinemaid, Timestamp successtime, String isTakeTradeNo, String mctype, String macid, ModelMap model){
		Synch synch = daoService.getObject(Synch.class, cinemaid);
		if(StringUtils.equals(mctype, "gewara")){//测试
			apiService.saveSynchWithCinema(synch, null, null, null, null);
		}else{
			apiService.saveSynchWithCinema(synch, null, successtime, null, null);
		}
		//冲突订单信息
		if(StringUtils.isNotBlank(isTakeTradeNo)){ //老的一体机同步程序在用
			String[] isTakeTrade = isTakeTradeNo.split("%");
			for(String trade:isTakeTrade){
				String[] tradeMap = trade.split("@");
				apiService.saveOrderResult(tradeMap);
			}
		}
		if(StringUtils.isNotBlank(macid)){
			getMachineSynch(cinemaid, null, macid, successtime);
		}
		model.put("nowtime", successtime);
		return getXmlView(model, "api/ticket/toSynchronize.vm");
	}
	
	@RequestMapping("/api/synch/synchGoodsReport.xhtml")
	public String synchReport(Long cinemaid, Timestamp successtime, ModelMap model){
		Synch synch = daoService.getObject(Synch.class, cinemaid);
		apiService.saveSynchGoodsWithCinema(synch, null, successtime, null, null);
		model.put("nowtime", successtime);
		return getXmlView(model, "api/ticket/toSynchronize.vm");
	}
	@RequestMapping("/api/synch/ticketStatus.xhtml")
	public String ticketStatus(/*String key, */String xml,String sportId, ModelMap model){
		if(StringUtils.isBlank(xml)) return getErrorXmlView(model, ApiConstant.CODE_PARAM_ERROR, "参数错误");
		if(Config.isDebugEnabled()){
			dbLogger.warnWithType(LogTypeConstant.LOG_TYPE_API, "XML:"+xml);
		}
		Timestamp curtime = DateUtil.getMillTimestamp();
		OrderResponse result = (OrderResponse) ApiUtils.xml2Object(ApiUtils.getBeanReader("ticketstatus",OrderResponse.class), xml);
		for(OrderResult orderResult:result.getOrderList()){
			if(orderResult.getTaketime() != null) orderResult.setIstake("Y");
			GewaOrder order = daoService.getObjectByUkey(GewaOrder.class, "tradeNo", StringUtils.trim(orderResult.getTradeno()), false);
			if(order != null) {
				if(order instanceof TicketOrder) {
					orderResult.setOrdertype(OrderResult.ORDERTYPE_TICKET);
					orderResult.setPlaceid(((TicketOrder) order).getCinemaid());
				}else if(order instanceof GoodsOrder){
					orderResult.setOrdertype(OrderResult.ORDERTYPE_MEAL);
					orderResult.setPlaceid(((GoodsOrder) order).getPlaceid());
				}else if(order instanceof DramaOrder){
					orderResult.setOrdertype(OrderResult.ORDERTYPE_DRAMA);
				}else if(order instanceof SportOrder || StringUtils.isNotBlank(sportId)){
					orderResult.setOrdertype(OrderResult.ORDERTYPE_SPORT);
					orderResult.setCaption(sportId);
				}
				orderResult.setTradeno(StringUtils.trim(orderResult.getTradeno()));
				orderResult.setTicketnum(order.getQuantity());
			}
			if(StringUtils.isNotBlank(sportId)){
				orderResult.setOrdertype(OrderResult.ORDERTYPE_SPORT);
				orderResult.setCaption(sportId);
			}
			orderResult.setUpdatetime(curtime);
			OrderNote onm = daoService.getObjectByUkey(OrderNote.class, "serialno", StringUtils.trim(orderResult.getTradeno()), false);
			if(onm!=null){
				if(orderResult.getTaketime() != null) { 
					onm.setTaketime(orderResult.getTaketime());
					onm.setResult(Status.Y);
				}else {
					onm.setResult(OrderNoteConstant.RESULT_S);
				}
				daoService.saveObject(onm);
			}
		}
		daoService.saveObjectList(result.getOrderList());
		model.put("result", true);
		Timestamp cur = new Timestamp(System.currentTimeMillis());
		model.put("successtime", cur);
		return getXmlView(model, "api/ticket/ticketStatus.vm");
	}
	

	@SuppressWarnings("deprecation")
	@RequestMapping("/api/synch/cancelTicketOrder.xhtml")
	public String cancelTicketOrder(String tradeNo, String key, String encryptCode, ModelMap model){
		ApiAuth auth = ApiAuthenticationFilter.getApiAuth();
		ApiUser apiUser = auth.getApiUser();
		if(StringUtils.isNotBlank(encryptCode)){
			ApiAuth apiAuth = checkRights(encryptCode, key, tradeNo);
			if(!apiAuth.isChecked()) return getErrorXmlView(model, apiAuth.getCode(), apiAuth.getMsg());
		}
		if(StringUtils.isBlank(tradeNo)) return getErrorXmlView(model, ApiConstant.CODE_PARAM_ERROR, "传递参数错误！");
		TicketOrder order = daoService.getObjectByUkey(TicketOrder.class, "tradeNo", tradeNo, false);
		if(order == null) return getErrorXmlView(model, ApiConstant.CODE_DATA_ERROR, "没有该订单！");
		if(StringUtils.contains(order.getStatus(), OrderConstant.STATUS_PAID) || !StringUtils.equals(order.getPartnerid()+"", PartnerConstant.PARTNER_MACBUY+"")) 
			return getErrorXmlView(model, ApiConstant.CODE_DATA_ERROR, "此订单不能被取消！");
		if(!StringUtils.equals(apiUser.getId()+"", GEWA_TICKET+"")){
			String status = OrderConstant.STATUS_TIMEOUT;
			ticketOrderService.cancelTicketOrder2(tradeNo, order.getMemberid(), status, "终端支付超时取消");
		}
		return this.getDirectXmlView(model, "<result>success</result>");
	}
	
	@RequestMapping("/api/synch/synchConfig.xhtml")
	public String synchConfig(/*String key, */String tag,ModelMap model){
		ApiAuth auth = ApiAuthenticationFilter.getApiAuth();
		ApiUser apiUser = auth.getApiUser();
		if(StringUtils.isBlank(tag))tag = Synch.TGA_CINEMA;
		List<SynchConfig> synchConfigList = daoService.getAllObjects(SynchConfig.class);
		//List<SynchConfig> synchConfigList = this.getSynchConfigList(tag);
		Map<String, String> passwordconfig = new HashMap<String, String>();
		for (SynchConfig synchConfig:synchConfigList) {
			if(StringUtils.contains(synchConfig.getTtype(), "password"))passwordconfig.put(synchConfig.getTtype(), StringUtil.md5(synchConfig.getTvalue()+apiUser.getPrivatekey()));
		}
		
		model.put("passwordconfig", passwordconfig);
		model.put("synchConfigList", synchConfigList);
		return getXmlView(model, "api/ticket/synchConfig.vm");
	}
	/**
	private List<SynchConfig> getSynchConfigList(String tag){
		DetachedCriteria query = DetachedCriteria.forClass(SynchConfig.class);
		query.add(Restrictions.like("tag", tag,MatchMode.ANYWHERE));
		List<SynchConfig> configList = this.hibernateTemplate.findByCriteria(query);
		return configList;
	}*/

	@RequestMapping("/api/synch/getMoviePic.xhtml")
	public String hotMoviePic(String key,String encryptCode,ModelMap model){
		ApiAuth apiAuth = checkRights(encryptCode, key);
		if(!apiAuth.isChecked()) return getErrorXmlView(model, apiAuth.getCode(), apiAuth.getMsg());
		Map<String, Object> qparam = new HashMap<String, Object>();
		List<Map> map = mongoService.find(MongoData.NS_TICKET_MACHINE_IMAGES, qparam);
		model.put("picMap", map);
		return getXmlView(model, "api/info/synch/moviePicList.vm");
	}
	
	@RequestMapping("/api/synch/reportError.xhtml")
	public String reportError(String key,String encryptCode,long cinemaid,
			String errorCode,String remark,String machineid,ModelMap model){
		return saveError(key, encryptCode, cinemaid, machineid, errorCode, remark,"0", model);
	}

	@RequestMapping("/api/synch/machineError.xhtml")
	public String machineError(String key,String encryptCode,long cinemaid,
			String machineId,String errorType,String errorMessage,ModelMap model){
		return saveError(key, encryptCode, cinemaid, machineId, errorType, errorMessage,"1", model);
	}
	
	@SuppressWarnings("deprecation")
	private String saveError(String key,String encryptCode,long cinemaid,String machineId,
			String errorType,String errorMessage,String type,ModelMap model){
		ApiAuth apiAuth = checkRights(encryptCode, key);
		if(!apiAuth.isChecked()) return getErrorXmlView(model, apiAuth.getCode(), apiAuth.getMsg());
		if(machineId == null || errorType == null)return getErrorXmlView(model, ApiConstant.CODE_PARAM_ERROR, "参数错误");
		Map<String, Object> toSave = new HashMap<String, Object>();
		toSave.put(MongoData.SYSTEM_ID, MongoData.buildId());
		toSave.put("id", cinemaid+System.currentTimeMillis());
		Cinema cinema = this.daoService.getObject(Cinema.class, cinemaid);
		String name = "";
		if(cinema != null) {
			name = cinema.getName();
		}else {
			Theatre theatre = daoService.getObject(Theatre.class, cinemaid);
			if(theatre!=null){
				name = theatre.getName();
			}
		}
		toSave.put("cinemaid", cinemaid);
		toSave.put("name", name);
		toSave.put("errorCode", errorType);
		toSave.put("machineid", machineId);
		toSave.put("remark", errorMessage);
		toSave.put("type", type);
		toSave.put("addtime", new Timestamp(System.currentTimeMillis()));
		mongoService.saveOrUpdateMap(toSave, "id", MongoData.NS_TICKET_MACHINE_ERROR, false, true);
		return this.getDirectXmlView(model, "<result>success</result>");
	}
	
	
	
	/**
	 * 获得一体机关机时间
	 * @param key
	 * @param encryptCode
	 * @param venueId
	 * @param model
	 * @return
	 */
	@RequestMapping("/api/synch/getMachineShutDownTime.xhtml")
	public String getMachineConfig(
			String key,String encryptCode,
			@RequestParam(required=true,value="venueId")Long venueId,
			//@RequestParam(required=false,value="type",defaultValue="moive")String type,//类型(moive/sport/drama)
			ModelMap model){
		//身份校验
		ApiAuth apiAuth = checkRights(encryptCode, key);
		if(!apiAuth.isChecked()) return getErrorXmlView(model, apiAuth.getCode(), apiAuth.getMsg());
		
		String vm="api/info/synch/machineShutDownTime.vm";
		String finalShutDownTime=null;//返回关机时间 yyyy-MM-dd HH:mm:ss
		
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("venueId", venueId);
		List<Map> map = mongoService.find(MongoData.NS_MACHINECONFIG, params);
		
		if(map==null||(map!=null&&map.size()==0)){
			model.put("msg", "关机时间配置不存在");
			return getXmlView(model, vm);
		}
			
		//获取配置
		Map cfg=map.get(0);
		String defTime=(String)cfg.get("defShutDownTime");//默认关机时间 HH:mm
		Integer unitTime=(Integer)cfg.get("unitTime");//单位时间
		
		//校验配置
		if(StringUtils.isBlank(defTime)){
			model.put("msg", "没有配置默认关机时间");
			return getXmlView(model, vm);
		}
		//格式校验
		SimpleDateFormat sdfHM=new SimpleDateFormat("HH:mm");
		try {
			sdfHM.parse(defTime);
		} catch (ParseException e) {
			model.put("msg", "默认关机时间格式配置错误(正确格式HH:mm)");
			return getXmlView(model, vm);
		}
		if(unitTime==null){
			model.put("msg", "没有配置单位时间");
			return getXmlView(model, vm);
		}
		
		//组装默认关机时间
		SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd");
		SimpleDateFormat sdfFull=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String defShutDownDate=sdf.format(new Date())+" "+defTime+":00";//yyyy-MM-dd HH:mm:ss
		Date defShutDownTime=null;
		try {
			defShutDownTime = sdfFull.parse(defShutDownDate);
		} catch (ParseException e) {
			model.put("msg", "默认关机时间格式配置错误(正确格式HH:mm)");
			return getXmlView(model, vm);
		}
		//查询当日18点至次日6点间最晚场次时间
		Timestamp curtime = new Timestamp(System.currentTimeMillis());
		int hour = DateUtil.getHour(curtime);
		if(hour>=0 && hour <=5){
			return getErrorXmlView(model, ApiConstant.CODE_DATA_ERROR, "凌晨到5点不做同步");
		}
		Timestamp begtime = DateUtil.getBeginningTimeOfDay(curtime);
		Timestamp startTime = DateUtil.addHour(begtime, 18);
		Timestamp endTime = DateUtil.addHour(startTime, 12);
		Timestamp lastPlayTime=null;
		String hql="select new map( max(o.playtime) as lasttime ) from OpenPlayItem o where o.cinemaid=? and o.playtime>=? and o.playtime<=? ";
		List<Map<String, Object>> resultList=hibernateTemplate.find(hql, venueId,startTime,endTime);
		if(resultList!=null&&resultList.size()>0){
			lastPlayTime=(Timestamp) resultList.get(0).get("lasttime");
		}
		
		if(null!=lastPlayTime){//查询关机时间=当日18点至次日6点间最晚场次时间+单位时间
			//关机时间
			Timestamp tempTime = DateUtil.addMinute(lastPlayTime, unitTime);
			//如果关机时间小于默认自动关机时间，则取默认自动关机时间给终端机
			if(tempTime.before(defShutDownTime)){
				finalShutDownTime=defShutDownDate;
			}else{
				finalShutDownTime=sdfFull.format(tempTime);
			}
		}else{//当日没有场次,关机时间取默认关机时间
			finalShutDownTime=defShutDownDate;
		}
		
		model.put("shutdowntime", finalShutDownTime);
		return getXmlView(model, vm);
	}

	
	/**
	 * 获得一体机关机时间
	 * @param model 
	 * @param key
	 * @param encryptCode
	 * @param venueId
	 * @param model
	 * @return
	 */
	@SuppressWarnings("deprecation")
	@RequestMapping("/api/synch/getCurrentTime.xhtml")
	public String getCurrentTime(ModelMap model){
		return this.getDirectXmlView(model, "<currentTime>"+DateUtil.formatTimestamp(DateUtil.getMillTimestamp())+"</currentTime>");
	}
			
	
	
}
