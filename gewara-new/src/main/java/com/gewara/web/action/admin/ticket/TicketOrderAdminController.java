package com.gewara.web.action.admin.ticket;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.gewara.api.gmail.request.SendMailByOutboxRequest;
import com.gewara.api.gmail.response.SendMailByOutboxResponse;
import com.gewara.api.gmail.service.GmailService;
import com.gewara.command.EmailRecord;
import com.gewara.command.SearchOrderCommand;
import com.gewara.command.SearchRefundCommand;
import com.gewara.constant.PayConstant;
import com.gewara.constant.PaymethodConstant;
import com.gewara.constant.Status;
import com.gewara.constant.TagConstant;
import com.gewara.constant.sys.MongoData;
import com.gewara.constant.ticket.OpiConstant;
import com.gewara.constant.ticket.OrderConstant;
import com.gewara.helper.ticket.SeatStatusUtil;
import com.gewara.json.RelateToSettle;
import com.gewara.json.WDOrderContrast;
import com.gewara.model.acl.User;
import com.gewara.model.movie.Cinema;
import com.gewara.model.movie.CinemaRoom;
import com.gewara.model.movie.Movie;
import com.gewara.model.movie.RoomSeat;
import com.gewara.model.pay.GoodsOrder;
import com.gewara.model.pay.OrderRefund;
import com.gewara.model.pay.SMSRecord;
import com.gewara.model.pay.TicketOrder;
import com.gewara.model.ticket.OpenPlayItem;
import com.gewara.model.ticket.OpenSeat;
import com.gewara.model.ticket.SellSeat;
import com.gewara.mongo.MongoService;
import com.gewara.service.MessageService;
import com.gewara.service.gewapay.RefundService;
import com.gewara.service.movie.MCPService;
import com.gewara.service.order.GoodsOrderService;
import com.gewara.service.order.OrderQueryService;
import com.gewara.service.ticket.OpenPlayService;
import com.gewara.service.ticket.OpiManageService;
import com.gewara.service.ticket.TicketOrderService;
import com.gewara.service.ticket.TicketProcessService;
import com.gewara.support.ErrorCode;
import com.gewara.support.FirstLetterComparator;
import com.gewara.support.MultiPropertyComparator;
import com.gewara.support.ServiceHelper;
import com.gewara.untrans.NosqlService;
import com.gewara.untrans.UntransService;
import com.gewara.untrans.hbase.HBaseService;
import com.gewara.untrans.hbase.HbaseData;
import com.gewara.untrans.ticket.RemoteTicketService;
import com.gewara.untrans.ticket.TicketOperationService;
import com.gewara.util.BeanUtil;
import com.gewara.util.DateUtil;
import com.gewara.util.JsonUtils;
import com.gewara.util.ObjectId;
import com.gewara.util.StringUtil;
import com.gewara.util.ValidateUtil;
import com.gewara.util.VmUtils;
import com.gewara.web.action.admin.BaseAdminController;
import com.gewara.web.util.PageUtil;
import com.gewara.xmlbind.ticket.TicketRemoteOrder;
import com.gewara.xmlbind.ticket.WdOrder;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
@Controller
public class TicketOrderAdminController extends BaseAdminController {
	@Autowired@Qualifier("untransService")
	private UntransService untransService;
	@Autowired@Qualifier("goodsOrderService")
	private GoodsOrderService goodsOrderService;
	@Autowired@Qualifier("hbaseService")
	private HBaseService hbaseService;
	@Autowired@Qualifier("messageService")
	private MessageService messageService;
	@Autowired@Qualifier("ticketOrderService")
	private TicketOrderService ticketOrderService;
	@Autowired@Qualifier("mcpService")
	private MCPService mcpService;
	@Autowired@Qualifier("openPlayService")
	private OpenPlayService openPlayService;
	@Autowired@Qualifier("orderQueryService")
	private OrderQueryService orderQueryService;
	@Autowired@Qualifier("mongoService")
	private MongoService mongoService;
	@Autowired@Qualifier("ticketOperationService")
	private TicketOperationService ticketOperationService;
	@Autowired@Qualifier("opiManageService")
	private OpiManageService opiManageService;
	@Autowired@Qualifier("ticketProcessService")
	private TicketProcessService ticketProcessService;
	@Autowired@Qualifier("refundService")
	private RefundService refundService;
	@Autowired@Qualifier("nosqlService")
	private NosqlService nosqlService;
	
	@Autowired@Qualifier("gmailService")
	private GmailService gmailService;
	
	@Autowired@Qualifier("remoteTicketService")
	private RemoteTicketService remoteTicketService;
	
	@RequestMapping("/admin/ticket/orderList.xhtml")
	public String orderList(SearchOrderCommand soc, HttpServletRequest request, String append, ModelMap model){
		String citycode = getAdminCitycode(request);
		List<Cinema> cinemaList = mcpService.getBookingCinemaList(citycode);
		Collections.sort(cinemaList, new FirstLetterComparator());
		if(soc.isBlankCond()) model.put("msg", "请精确查询条件！");
		List<TicketOrder> orderList = orderQueryService.getTicketOrderList(soc);
		List<Long> mpidList = BeanUtil.getBeanPropertyList(orderList, Long.class, "mpid", true);
		List<OpenPlayItem> opiList = new ArrayList<OpenPlayItem>();
		for(Long mpid: mpidList) {
			opiList.add(daoService.getObjectByUkey(OpenPlayItem.class, "mpid", mpid, true));
		}
		List<Movie> movieList = mcpService.getCurMovieList();
		Collections.sort(movieList, new FirstLetterComparator());
		
		Map<Long, OpenPlayItem> opiMap = BeanUtil.beanListToMap(opiList, "mpid");
		model.put("opiMap", opiMap);
		model.put("movieList", movieList);
		model.put("cinemaList", cinemaList);
		model.put("orderList", orderList);
		model.put("opiList", opiList);
		model.put("ordertype", StringUtils.isBlank(soc.getOrdertype())?" " : soc.getOrdertype());
		if(StringUtils.isNotBlank(append)){
			List<String> fields = new ArrayList<String>(Arrays.asList(append.split(",")));
			fields.remove("mobile");
			model.put("fields", fields);
		}
		return "admin/ticket/orderList.vm";
	}
	@RequestMapping("/admin/gewapay/exportOrderByMpid.xhtml")
	public String exportOrder(@RequestParam("mpid")Long mpid, String append, ModelMap model) {
		OpenPlayItem opi = daoService.getObjectByUkey(OpenPlayItem.class, "mpid", mpid, true);
		SearchOrderCommand soc = new SearchOrderCommand();
		soc.setMpid(opi.getMpid());
		soc.setStatus(OrderConstant.STATUS_PAID_SUCCESS);
		soc.setOrdertype(OrderConstant.STATUS_PAID_SUCCESS);
		List<TicketOrder> orderList = orderQueryService.getTicketOrderList(soc);
		model.put("orderList", orderList);
		model.put("opi", opi);
		if(StringUtils.isNotBlank(append)){
			List<String> fields = new ArrayList<String>(Arrays.asList(append.split(",")));
			fields.remove("mobile");
			model.put("fields", fields);
		}
		return "admin/gewapay/exportOrder.vm";
	}
	@RequestMapping("/admin/ticket/booking.xhtml")
	public String booking(@RequestParam("mpid")Long mpid, SearchOrderCommand soc, String append, ModelMap model) {		
		OpenPlayItem opi = daoService.getObjectByUkey(OpenPlayItem.class, "mpid", mpid, true);
		if(opi == null) return showMessage(model, "本场不存在！不接受预订！");
		List<TicketOrder> orderList = orderQueryService.getTicketOrderList(soc);
		CinemaRoom room = daoService.getObject(CinemaRoom.class, opi.getRoomid());
		model.put("room", room);
		model.put("orderList", orderList);
		model.put("ordertype", StringUtils.isBlank(soc.getOrdertype())?" " : soc.getOrdertype());
		model.put("opi", opi);
		Timestamp cur = new Timestamp(System.currentTimeMillis());
		if(!opi.getPlaytime().before(DateUtil.addHour(cur, -23))){//1天前的场次
			Map<Integer, String> rowMap = new HashMap<Integer, String>();
			List<SellSeat> sellSeatList = openPlayService.getSellSeatListByMpid(mpid);
			SeatStatusUtil seatStatusUtil = new SeatStatusUtil(sellSeatList);
			model.put("seatStatusUtil", seatStatusUtil);
			List<String> hfhLockList = new ArrayList<String>();
			if(StringUtils.equals(opi.getOpentype(), OpiConstant.OPEN_WD)){//万达，特殊
				Map<String, RoomSeat> seatMap = new HashMap<String, RoomSeat>();
				List<RoomSeat> rseatList = openPlayService.getSeatListByRoomId(room.getId());
				for(RoomSeat seat:rseatList){
					rowMap.put(seat.getLineno(), seat.getSeatline());
					seatMap.put("row" + seat.getLineno() + "rank" + seat.getRankno(), seat);
				}
				model.put("seatMap", seatMap);
				ErrorCode<List<String>> remoteLockList = ticketOperationService.updateRemoteLockSeat(opi, OpiConstant.SECONDS_FORCEUPDATE_SEAT, true);
				if(remoteLockList.isSuccess()){
					hfhLockList = remoteLockList.getRetval();
				}
				openPlayService.updateOpiSeatMap4Wd(mpid, room, rseatList, new ArrayList<String>(0), seatStatusUtil);
			}else{
				Map<String, OpenSeat> seatMap = new HashMap<String, OpenSeat>();
				List<OpenSeat> openSeatList = openPlayService.getOpenSeatList(opi.getMpid());
				for(OpenSeat seat:openSeatList){
					rowMap.put(seat.getLineno(), seat.getSeatline());
					seatMap.put("row" + seat.getLineno() + "rank" + seat.getRankno(), seat);
				}
				if(DateUtil.addDay(new Timestamp(System.currentTimeMillis()), -2).before(opi.getPlaytime())){
					//两天内
					ErrorCode<List<String>> remoteLockList = ticketOperationService.updateRemoteLockSeat(opi, OpiConstant.SECONDS_FORCEUPDATE_SEAT, true);
					if(remoteLockList.isSuccess()){
						hfhLockList = remoteLockList.getRetval();
						opiManageService.updateOpiStats(opi.getId(), hfhLockList, false);
						openPlayService.updateOpiSeatMap(mpid, room, openSeatList, hfhLockList, seatStatusUtil);
					}else{
						model.put("hfherror", "影院服务器连接不正常：" + remoteLockList.getMsg());
					}
				}
				model.put("seatMap", seatMap);
			}
			Integer seatcount = room.getSeatnum();
			model.put("hfhLockList", hfhLockList);
			model.put("seatcount", seatcount);
			model.put("rowMap", rowMap);
			if(StringUtils.isNotBlank(append)){
				List<String> fields = new ArrayList<String>(Arrays.asList(append.split(",")));
				fields.remove("mobile");
				model.put("fields", fields);
			}
		}	
		return "admin/ticket/booking.vm";
	}

	@RequestMapping("/admin/ticket/sellReport.xhtml")
	public String sellReport(@RequestParam("mpid")Long mpid, ModelMap model){
		OpenPlayItem opi = daoService.getObjectByUkey(OpenPlayItem.class, "mpid", mpid, true);
		if(opi == null) return showMessage(model, "本场不接受预订！");
		CinemaRoom room = daoService.getObject(CinemaRoom.class, opi.getRoomid());
		Cinema cinema = daoService.getObject(Cinema.class, room.getCinemaid());
		
		Integer seatcount = openPlayService.getSeatCountByRoomId(opi.getRoomid());
		if(seatcount==0) return showMessage(model, "此影厅没有座位图！");
		List<OpenSeat> soldList = openPlayService.getOpenSeatList(opi.getMpid());
		List<SellSeat> sellSeatList = openPlayService.getSellSeatListByMpid(mpid);
		SeatStatusUtil seatStatusUtil = new SeatStatusUtil(sellSeatList);
		model.put("seatStatusUtil", seatStatusUtil);

		model.put("cinema", cinema);
		model.put("opi", opi);
		model.put("seatcount", seatcount);
		model.put("soldList", soldList);
		model.put("room", room);
		
		return "admin/ticket/sellReport.vm";
	}
	
	@RequestMapping("/admin/ticket/forceRelockOrder.xhtml")
	public String forceRelockOrder(String tradeNo, ModelMap model) {
		TicketOrder order = daoService.getObject(TicketOrder.class, tradeNo);
		OpenPlayItem opi = daoService.getObjectByUkey(OpenPlayItem.class, "mpid", order.getMpid(), true);
		List<SellSeat> seatList = ticketOrderService.getOrderSeatList(order.getId());
		ErrorCode lockResult = ticketOperationService.lockRemoteSeat(opi, order, seatList);
		if(!lockResult.isSuccess()) {
			return forwardMessage(model, lockResult.getMsg());
		}
		return forwardMessage(model, BeanUtil.buildString(lockResult.getRetval(), false));
	}
	//实时订票数量统计
	@RequestMapping("/admin/ticket/intensiveOpiList.xhtml")
	public String intensiveOpiList(Integer seatnum, ModelMap model) {
		if(seatnum==null) seatnum = 5;
		List<OpenPlayItem> opiList = openPlayService.getIntensiveOpiList(seatnum);
		Collections.sort(opiList, new MultiPropertyComparator<OpenPlayItem>(new String[]{"cinemaid", "playtime"}, new boolean[]{true, false}));
		model.put("opiList", opiList);
		return "admin/ticket/intensiveOpiList.vm";
	}

	//实时订票数量统计
	@RequestMapping("/admin/ticket/opiStats.xhtml")
	public String opiStat(Long cid, Date date, Long rid, Long mid, HttpServletRequest request, ModelMap model) {
		Date cur = DateUtil.getCurDate();
		if(date == null) date = cur;

		Timestamp from = new Timestamp(date.getTime());
		Timestamp to = DateUtil.getLastTimeOfDay(from);
		String countCinemaQuery = "select new map(opi.cinemaid as cid, max(cinemaname) as cname, count(opi.id) as opicount) " +
				"from OpenPlayItem opi where opi.playtime > ? and opi.playtime < ? and opi.citycode=? " +
				"group by opi.cinemaid";
		String citycode = getAdminCitycode(request);
		List<Map> cinemaMapList = hibernateTemplate.find(countCinemaQuery, from, to, citycode);
		model.put("cinemaMapList", cinemaMapList);
		if(cid == null && cinemaMapList.size()>0) cid = (Long) cinemaMapList.get(0).get("cid");
		Map<String, String> dateMap = new HashMap<String, String>();
		List<String> dateList = new ArrayList<String>();
		Map<String, Integer> countMap = new HashMap<String, Integer>();
		for(int i=0;i< 7; i++){
			String d = DateUtil.formatDate(DateUtil.addDay(cur, i));
			dateList.add(d);
			String dateStr = DateUtil.format(DateUtil.addDay(cur, i), "M月d日");
			dateMap.put(d, dateStr + " " + DateUtil.getCnWeek(DateUtil.addDay(cur, i)));
			Integer count = mcpService.getCinemaMpiCountByDate(cid, DateUtil.addDay(cur, i));
			countMap.put(d, count);
		}
		model.put("dateList", dateList);
		model.put("dateMap", dateMap);
		model.put("countMap", countMap);
		Cinema cinema = daoService.getObject(Cinema.class, cid);
		model.put("cinema", cinema);
	
		List<Movie> movieList = mcpService.getCurMovieListByCinemaIdAndDate(cid, date);
		if(rid==null && mid==null && movieList.size() > 0){
			mid = movieList.get(0).getId();
		}
		model.put("movieList", movieList);
		List<OpenPlayItem> opiList = openPlayService.getOpiList(null, cid, null, from, to, false);
		List<Long> movieidList = BeanUtil.getBeanPropertyList(opiList, Long.class, "movieid", true);
		model.put("opiList", opiList);
		model.put("movieidList", movieidList);
		Map movienameMap = BeanUtil.getKeyValuePairMap(opiList, "movieid", "moviename");
		model.put("movienameMap", movienameMap);
		model.put("curDate", DateUtil.formatDate(date));
		model.put("curTime", DateUtil.formatTimestamp(new Timestamp(System.currentTimeMillis())));
		return "admin/ticket/opiStats.vm";
	}
	@RequestMapping("/admin/ticket/order/preCrossMpiChangeSeat.xhtml")
	public String preCrossMpiChangeSeat(String tradeNo, Long newmpid, String newseat, ModelMap model){
		if(StringUtils.isBlank(tradeNo) || newmpid==null || StringUtils.isBlank(newseat)){
			model.put("showForm", true);
			return "admin/ticket/crossMpiChangeSeat.vm";
		}
		TicketOrder order = daoService.getObjectByUkey(TicketOrder.class, "tradeNo", tradeNo, false);
		try {
			OpenPlayItem oldopi = daoService.getObjectByUkey(OpenPlayItem.class, "mpid", order.getMpid(), true);
			OpenPlayItem newopi = daoService.getObjectByUkey(OpenPlayItem.class, "mpid", newmpid, true);
			List<SellSeat> seatList = ticketOrderService.getOrderSeatList(order.getId());
			List<OpenSeat> newseatList = ticketProcessService.getNewSeatList(order, newopi, seatList, newseat);
			model.put("newseatList", newseatList);
			model.put("oldopi", oldopi);
			model.put("newopi", newopi);
			model.put("oldOrder", order);
			model.put("newseat", newseat);
			model.put("tradeNo", tradeNo);
			model.put("newmpid", newmpid);
			return "admin/ticket/crossMpiChangeSeat.vm";
		} catch (Exception e) {
			dbLogger.error("错误", e);
			return showError(model, e.getMessage());
		}
	}
	
	/*
	 * 订单处理：跨场次换座位
	 */
	@RequestMapping("/admin/ticket/order/crossMpiChangeSeat.xhtml")
	public String crossMpiChangeSeat(Long orderid, Long newmpid, String newseat, String forceReConfirm, ModelMap model){
		User user = getLogonUser();
		boolean allow = operationService.updateOperation("procRep" + orderid, 60);
		if(!allow) return showJsonError(model, "他人(系统)正在处理，请等待1分钟！");
		TicketOrder order = daoService.getObject(TicketOrder.class, orderid);
		if(StringUtils.equals(OpiConstant.OPEN_WD, order.getCategory())){
			return showJsonError(model, "万达影院暂不支持！");
		}

		if(!order.getStatus().equals(OrderConstant.STATUS_PAID_UNFIX)) {
			return showJsonError(model, "只有“座位待处理”订单才可重新确认！");
		}
		boolean reChange = StringUtils.isNotBlank(forceReConfirm);
		OpenPlayItem opi = daoService.getObjectByUkey(OpenPlayItem.class, "mpid", newmpid, true);
		List<SellSeat> seatList = ticketOrderService.getOrderSeatList(order.getId());
		ErrorCode result = ticketOperationService.releasePaidFailureOrderSeat(opi, order, seatList);
		if(result.isSuccess()){
			try{
				//TODO:处理一下Exception！！！
				List<OpenSeat> oseatList = ticketProcessService.getNewSeatList(order, opi, seatList, newseat);
				ticketProcessService.changeSeat(opi, oseatList, order, reChange);
				
				dbLogger.warn(user.getNickname()+user.getId()+ "更改座位");
				return showJsonSuccess(model);
			}catch(Exception e){
				return showJsonError(model, e.getMessage());
			}
		}else{
			return showJsonError(model, result.getMsg());
		}
	}
	@RequestMapping(value="/admin/ticket/order/synchCostprice.xhtml", method=RequestMethod.GET)
	public String showRefund(String tradeNo, ModelMap model){
		if(StringUtils.isNotBlank(tradeNo)){
			TicketOrder order = daoService.getObjectByUkey(TicketOrder.class, "tradeNo", tradeNo, true);
			if(order!=null){
				model.put("order", order);
				OpenPlayItem opi = daoService.getObjectByUkey(OpenPlayItem.class, "mpid", order.getMpid(), true);
				model.put("opi", opi);
			}
		}
		return "admin/gewapay/synchCostprice.vm";
	}
	@RequestMapping(value="/admin/ticket/order/synchCostprice.xhtml", method=RequestMethod.POST)
	public String synchCostprice(String tradeNo, ModelMap model){
		TicketOrder order = daoService.getObjectByUkey(TicketOrder.class, "tradeNo", tradeNo, true);
		User user = getLogonUser();
		ErrorCode code = ticketProcessService.synchCostprice(order, user.getId());
		dbLogger.warn(user.getNickname() + user.getId() + "同步成本价：" + code.isSuccess()); 
		model.put("order", order);
		model.put("code", code);
		return "admin/gewapay/synchCostprice.vm";
	}
	@RequestMapping("/admin/ticket/order/getRemoteOrder.xhtml")
	public String getRemoteOrder(Long orderid, ModelMap model){
		TicketOrder order = daoService.getObject(TicketOrder.class, orderid);
		ErrorCode<TicketRemoteOrder> code = ticketOperationService.getRemoteOrder(order, true);
		if(!code.isSuccess()) return forwardMessage(model, code.getMsg());
		if(!code.getRetval().hasFixed()) return forwardMessage(model, BeanUtil.buildString(code, true));
		return forwardMessage(model, "订单状态:" + BeanUtil.buildString(code, true));
	}
	@RequestMapping("/admin/ticket/order/getBindGoodsOrder.xhtml")
	public String getBindGoodsOrder(Long orderid, ModelMap model){
		TicketOrder order = daoService.getObject(TicketOrder.class, orderid);
		if(order!=null) {
			//1、判断是否自动关联套餐
			Map<String, String> otherinfoMap = VmUtils.readJsonToMap(order.getOtherinfo());
			String bindTradeNo = otherinfoMap.get(PayConstant.KEY_BIND_TRADENO);
			if(StringUtils.isNotBlank(bindTradeNo)) {
				model.put("tradeNo", bindTradeNo);
				return "redirect:/admin/gewapay/orderDetail.xhtml";
			}else{
				return showError(model, "未绑定套餐！");
			}
		}else{
			return showError(model, "订单不存在！");
		}
	}
	@RequestMapping("/admin/ticket/order/addBindGoodsOrder.xhtml")
	public String addBindGoodsOrder(Long orderid, ModelMap model){
		TicketOrder order = daoService.getObject(TicketOrder.class, orderid);
		if(order!=null) {
			if(order.getItemfee()>0){
				try{
					ErrorCode<GoodsOrder> goodsOrder = goodsOrderService.addGoodsOrderByBuyItem(order);
					if(goodsOrder.isSuccess()){
						List<SMSRecord> smsList = messageService.addMessage(goodsOrder.getRetval()).getRetval();
						if(!CollectionUtils.isEmpty(smsList)){
							for (SMSRecord sms : smsList) {
								if(sms!=null) untransService.sendMsgAtServer(sms, false);
							}
						}
						model.put("tradeNo", goodsOrder.getRetval().getTradeNo());
						return "redirect:/admin/gewapay/orderDetail.xhtml";
					}else{
						return showError(model, goodsOrder.getMsg());
					}
				}catch(Exception e){
					dbLogger.error(StringUtil.getExceptionTrace(e, 5));
					return showError(model, "加入出错！");
				}
			}else{
				//1、判断是否自动关联套餐
				Map<String, String> otherinfoMap = VmUtils.readJsonToMap(order.getOtherinfo());
				String bindTradeNo = otherinfoMap.get(PayConstant.KEY_BIND_TRADENO);
				if(StringUtils.isNotBlank(bindTradeNo)) {
					model.put("tradeNo", bindTradeNo);
					return "redirect:/admin/gewapay/orderDetail.xhtml";
				}
				Map<String, String> otherMap = VmUtils.readJsonToMap(order.getOtherinfo());
				String bindgoods = otherMap.get(PayConstant.KEY_BINDGOODS);
				String goodsgift = otherMap.get(PayConstant.KEY_GOODSGIFT);
				if(StringUtils.isNotBlank(bindgoods) || StringUtils.isNotBlank(goodsgift)) {
					String randomNum = ticketOrderService.nextRandomNum(DateUtil.addDay(new Timestamp(System.currentTimeMillis()), 60), 8, "0");
					ErrorCode<GoodsOrder> gorder = ticketOrderService.addBindGoodsOrder(order, randomNum);
					if(gorder.isSuccess()){
						List<SMSRecord> smsList = messageService.addMessage(gorder.getRetval()).getRetval();//只在notify中发短信
						if(!CollectionUtils.isEmpty(smsList)){
							for (SMSRecord sms : smsList) {
								if(sms!=null) untransService.sendMsgAtServer(sms, false);
							}
						}
						model.put("tradeNo", gorder.getRetval().getTradeNo());
						return "redirect:/admin/gewapay/orderDetail.xhtml";
					}else{
						return showError(model, gorder.getMsg());
					}
					
				}else{
					return showError(model, "未绑定套餐！");
				}
			}
		}else{
			return showError(model, "订单不存在！");
		}
	}
	@RequestMapping("/admin/ticket/order/unlock.xhtml")
	public String unlockRemoteOrder(Long orderid, ModelMap model){
		TicketOrder order = daoService.getObject(TicketOrder.class, orderid);
		ticketOperationService.unlockRemoteSeat(order, null);
		return getRemoteOrder(orderid, model);
	}
	@RequestMapping("/admin/ticket/orderRemark.xhtml")
	public String orderMemark(String tradeno, ModelMap model){
		if(StringUtils.isNotBlank(tradeno)) {
			TicketOrder order = daoService.getObjectByUkey(TicketOrder.class, "tradeNo", tradeno, false);
			model.put("order", order);
		}
		return "admin/gewapay/orderRemark.vm";
	}
	
	@RequestMapping("/admin/ticket/saveOrderRemark.xhtml")
	public String saveOrderRemark(String tradeno, String remark, ModelMap model){
		TicketOrder order = daoService.getObjectByUkey(TicketOrder.class, "tradeNo", tradeno, false);
		order.setRemark(remark);
		daoService.saveObject(order);
		return showJsonSuccess(model);
	}
	@RequestMapping("/admin/ticket/remarkOrderList.xhtml")
	public String remarkOrderList(Integer pageNo, ModelMap model){
		if(pageNo==null) pageNo = 0;
		Object[] params = new Object[]{OrderConstant.STATUS_PAID_SUCCESS, OrderConstant.STATUS_PAID_RETURN, DateUtil.addDay(new Timestamp(System.currentTimeMillis()), -60)};
		String qry = "select count(*) from TicketOrder t where t.remark is not null and (t.status=? or t.status=?) and t.addtime>=?";
		List list = hibernateTemplate.find(qry, params);
		int count = Integer.parseInt(list.get(0)+"");
		qry = "from TicketOrder t where t.remark is not null and (t.status=? or t.status=?) and t.addtime>=? order by t.addtime desc";
		int rowsPerPage = 20;
		List<TicketOrder> orderList = daoService.queryByRowsRange(qry, pageNo*rowsPerPage, rowsPerPage,  params);
		PageUtil pageUtil = new PageUtil(count, rowsPerPage, pageNo, "admin/ticket/remarkOrderList.xhtml");
		pageUtil.initPageInfo();
		model.put("pageUtil", pageUtil);
		model.put("orderList", orderList);
		return "admin/gewapay/remarkOrderList.vm";
	}
	@RequestMapping("/admin/ticket/orderLoghis.xhtml")
	public String orderLog(String tradeNo, ModelMap model){
		if(StringUtils.isNotBlank(tradeNo)) {
			Map<Long, Map<String, String>> changeMap = hbaseService.getMultiVersionRow(HbaseData.TABLE_GEWAORDER, tradeNo.getBytes(), 100);
			model.put("logList", changeMap.values());
			model.put("payTextMap", PaymethodConstant.getPayTextMap());
		}
		return "admin/gewapay/changeList.vm";
	}
	@RequestMapping("/admin/ticket/settleList.xhtml")
	public String settleList(Long cid, Integer pageNo, String param, ModelMap model){
		Cinema cinema = daoService.getObject(Cinema.class, cid);
		if(cinema == null) return show404(model, "该影院不存在或被删除!");
		if(pageNo == null) pageNo = 0;
		int rowsPerPage = 1;
		int fisrtRowPer = 0;
		int count = 0;
		Map params = new HashMap();
		params.put("tag", TagConstant.TAG_CINEMA);
		params.put("relatedid", cid);
		if(StringUtils.isNotBlank(param)){
			rowsPerPage = 5;
			fisrtRowPer = pageNo*rowsPerPage;
			count = mongoService.getObjectCount(RelateToSettle.class, params);
			
			PageUtil pageUtil = new PageUtil(count, rowsPerPage, pageNo, "admin/ticket/settleList.xhtml");
			Map params1 = new HashMap();
			params1.put("cid", cid);
			params1.put("param", param);
			pageUtil.initPageInfo(params1);
			model.put("pageUtil", pageUtil);
		
		}
		List<RelateToSettle> settleList = mongoService.getObjectList(RelateToSettle.class, params, "updatetime", false, fisrtRowPer, rowsPerPage);
		model.put("settleList", settleList);
		model.put("cinema", cinema);
		return "admin/gewapay/settleList.vm";
	}
	@RequestMapping("/admin/ticket/getSettle.xhtml")
	public String getSettle(String id, ModelMap model){
		if(StringUtils.isBlank(id)) return showJsonError(model, "参数错误！");
		RelateToSettle settle = mongoService.getObject(RelateToSettle.class, MongoData.DEFAULT_ID_NAME, id);
		if(settle == null) return showJsonError(model, "该数据不存在或被删除！");
		Map result = BeanUtil.getBeanMap(settle);
		return showJsonSuccess(model, result);
	}
	@RequestMapping("/admin/ticket/saveSettle.xhtml")
	public String saveOrUpdateSettle(String id, Long cid, String content, ModelMap model){
		User user = getLogonUser();
		RelateToSettle settle = null;
		if(StringUtils.isBlank(content)) return showJsonError(model, "内容不能为空！");
		Cinema cinema = daoService.getObject(Cinema.class, cid);
		if(cinema == null) return showJsonError(model, "参数错误！");
		if(StringUtils.isNotBlank(id)){
			settle = mongoService.getObject(RelateToSettle.class, MongoData.DEFAULT_ID_NAME, id);
			if(settle == null) return showJsonError(model, "该数据不存在或被删除！");
			if(!user.getId().equals(settle.getUserid())) return showJsonError(model, "不能修改他人信息！");
			settle.setContent(content);
			settle.setUpdatetime(DateUtil.formatTimestamp(System.currentTimeMillis()));
			settle.setUsername(user.getNickname());
		}else{
			settle = new RelateToSettle(TagConstant.TAG_CINEMA, cid, user.getId(), content);
			settle.setId(ServiceHelper.assignID(TagConstant.TAG_CINEMA));
			settle.setUsername(user.getNickname());
		}
		mongoService.saveOrUpdateObject(settle, MongoData.DEFAULT_ID_NAME);
		return showJsonSuccess(model);
	}
	
	// 需要处理的order警告, 权限最高
	@RequestMapping("/admin/ticket/errorOrders.xhtml")
	public String errorOrders(ModelMap model){
		Map params = new HashMap();
		params.put(MongoData.ACTION_STATUS, Status.N);
		int count = mongoService.getCount(MongoData.NS_API_WARNCALLBACK, params);
		return showJsonSuccess(model, ""+count);
	}
	/***
	 *  API 回调的订单异常状况
	 * */
	@RequestMapping("/admin/ticket/warnOrdersList.xhtml")
	public String warnOrdersList(String tradeNo, String status, ModelMap model){
		Map params = new HashMap();
		if(StringUtils.isNotBlank(tradeNo)){
			params.put(MongoData.ACTION_TRADENO, tradeNo);
		}else{
			if(StringUtils.isBlank(status)){
				status = Status.N;
			}
			params.put(MongoData.ACTION_STATUS, status);
		}
		List<Map> notifyList = mongoService.find(MongoData.NS_API_WARNCALLBACK, params, MongoData.ACTION_ADDTIME, false);
		model.put("notifyList", notifyList);
		model.put("notifyCount", notifyList.size());
		return "admin/ticket/warnOrdersList.vm";
	}
	// 设置消息为已解决
	@RequestMapping("/admin/ticket/processWarnOrder.xhtml")
	public String processWarnOrder(String id, ModelMap model){
		Map data = mongoService.findOne(MongoData.NS_API_WARNCALLBACK, MongoData.SYSTEM_ID, id);
		Map newdata = new HashMap(data);
		newdata.put(MongoData.ACTION_STATUS, Status.Y);
		// 解决人
		User user = getLogonUser();
		newdata.put("adminid", user.getId());
		newdata.put("adminname", user.getNickname());
		mongoService.update(MongoData.NS_API_WARNCALLBACK, data, newdata);
		return showJsonSuccess(model);
	}
	
	//结算确认
	@RequestMapping("/admin/ticket/settleConfirm.xhtml")
	public String settleConfirm(Long cid, ModelMap model){
		Cinema cinema = daoService.getObject(Cinema.class, cid);
		if(cinema == null) return show404(model, "该影院不存在或被删除!");
		model.put("cinema", cinema);
		//查询结算影片
		//getAutoSetterInfo(cinema.getId(), model);
		Map emailMap = mongoService.findOne(MongoData.NS_AUTO_SETTER_SEND_EMAIL, MongoData.SYSTEM_ID, cid + "");
		if(emailMap != null){
			if(emailMap.get("startTime") != null){
				Timestamp startTime = DateUtil.addDay(DateUtil.getBeginningTimeOfDay(DateUtil.parseTimestamp(emailMap.get("startTime").toString())),7);
				Timestamp endTime = DateUtil.addDay(DateUtil.getLastTimeOfDay(startTime), 6);
				model.put("startTime", DateUtil.formatTimestamp(startTime));
				model.put("endTime", DateUtil.formatTimestamp(endTime));
			}
		}
		model.put("emailMap",emailMap);
		return "admin/gewapay/settleConfirm.vm";
	}
	
	@RequestMapping("/admin/ticket/settleRefundOrderList.xhtml")
	public String settleRefundOrderList(Long cid,Timestamp startTime, ModelMap model){
		SearchRefundCommand command = new SearchRefundCommand();
		command.setAddtimefrom(DateUtil.addDay(startTime,-7));
		command.setAddtimeto(startTime);
		command.setPlaceid(cid);
		command.setStatus("finish");
		List<OrderRefund> refundList = refundService.getDifferentSettleOrderRefundList(command, null, 0, 500);
		Map refundMap = BeanUtil.beanListToMap(refundList, "tradeno");
		Set<String> tradeNos = refundMap.keySet();
		List<TicketOrder> orderList = new ArrayList<TicketOrder>();
		for(String tradeNo : tradeNos){
			orderList.add(daoService.getObjectByUkey(TicketOrder.class,"tradeNo", tradeNo, true));
		}
		model.put("orderList",orderList);
		model.put("refundMap", refundMap);
		return "admin/gewapay/settleRefundOrder.vm";
	}
	
	//结算价信息保存
	@RequestMapping("/admin/ticket/saveEmailPriceContent.xhtml")
	public String saveEmailPriceContent(Long cid,String priceContent,String key,ModelMap model){
		Cinema cinema = daoService.getObject(Cinema.class, cid);
		if(cinema == null) {
			return show404(model, "该影院不存在或被删除!");
		}
		Map sMap = mongoService.findOne(MongoData.NS_AUTO_SETTER_SEND_EMAIL, MongoData.SYSTEM_ID, "" + cid);
		if(sMap == null){
			sMap = new HashMap();
			sMap.put(MongoData.SYSTEM_ID, cid + "");
		}
		sMap.put(key, priceContent);
		mongoService.saveOrUpdateMap(sMap, MongoData.SYSTEM_ID, MongoData.NS_AUTO_SETTER_SEND_EMAIL);
		return showJsonSuccess(model);
	}
	@RequestMapping("/admin/ticket/sendAllConfirmEmail.xhtml")
	public String sendAllConfirmEmail(ModelMap modelMap){
		List<Map> mapList = mongoService.find(MongoData.NS_AUTO_SETTER_SEND_EMAIL, new HashMap<String,Object>());
		int sendMail = 0;
		if(!VmUtils.isEmptyList(mapList)){
			String template ="mail/settleConfirmMail.vm";
			for(Map map : mapList){
				String _id = map.get(MongoData.SYSTEM_ID).toString();
				if(!ValidateUtil.isNumber(_id)){
					continue;
				}
				Long id = Long.parseLong(map.get(MongoData.SYSTEM_ID).toString());
				Cinema cinema = daoService.getObject(Cinema.class, id);
				if(cinema != null){
					String title =cinema.getName()+"每周结算退票确认及瓦友反馈意见";
					String email = map.get("email1") == null ? "" : map.get("email1").toString();
					if(map.get("email2") != null && StringUtils.isNotBlank(map.get("email2").toString())) {
						email = email + "," + map.get("email2").toString();
					}
					if(StringUtils.isBlank(email)){
						continue;
					}
					Map model = new HashMap();
					model.put("formMap", map);
					model.put("cinameName", cinema.getName());
					if(map.get("startTime") != null){
						Timestamp startTime = DateUtil.addDay(DateUtil.getBeginningTimeOfDay(DateUtil.parseTimestamp(map.get("startTime").toString())),7);
						Timestamp endTime = DateUtil.addDay(DateUtil.getLastTimeOfDay(startTime), 6);
						model.put("startTime", DateUtil.formatTimestamp(startTime));
						model.put("endTime", DateUtil.formatTimestamp(endTime));
						SearchRefundCommand command = new SearchRefundCommand();
						command.setAddtimefrom(DateUtil.addDay(startTime,-7));
						command.setAddtimeto(startTime);
						command.setPlaceid(cinema.getId());
						command.setStatus("finish");
						List<OrderRefund> refundList = refundService.getDifferentSettleOrderRefundList(command, null, 0, 500);
						Map refundMap = BeanUtil.beanListToMap(refundList, "tradeno");
						Set<String> tradeNos = refundMap.keySet();
						//查询结算影片
						Map orderMap = new HashMap();
						int index = 1;
						for(String tradeNo : tradeNos){
							TicketOrder order = daoService.getObjectByUkey(TicketOrder.class,"tradeNo", tradeNo, true);
							if(order != null){
								Map<String,String> des = VmUtils.readJsonToMap(order.getDescription2());
								orderMap.put("moviename" + index, new String[]{des.get("影片")});
								orderMap.put("playdate" + index, new String[]{des.get("场次")});
								orderMap.put("cinemaseat" + index, new String[]{des.get("影厅") + " " + des.get("影票")});
								orderMap.put("allcostprice" + index, new String[]{order.getQuantity()*order.getCostprice() + ""});
								orderMap.put("totalprice" + index, new String[]{order.getTotalfee() + ""});
								index++;
							}
						}
						model.put("formMap", orderMap);
						map.put("startTime", DateUtil.formatTimestamp(startTime));
						map.put("endTime", DateUtil.formatTimestamp(endTime));
					}
					model.put("priceContent", map.get("priceContent"));
					model.put("content", map.get("content"));
					model.put("content1", map.get("content1"));
					String jsonData = JsonUtils.writeObjectToJson(model);
					SendMailByOutboxRequest request = new SendMailByOutboxRequest(SendMailByOutboxRequest.OUTBOX_OPERATION021,EmailRecord.SENDER_GEWARA,
							email.replaceAll(",", ";"),title,jsonData,template,new Timestamp(System.currentTimeMillis()));
					//mailService.sendTemplateEmail(EmailRecord.SENDER_GEWARA, title, template, model, email);
					SendMailByOutboxResponse response = gmailService.sendMailByOutbox(request);
					if(response.isSuccess()){
						sendMail++;
						mongoService.saveOrUpdateMap(map, MongoData.SYSTEM_ID, MongoData.NS_AUTO_SETTER_SEND_EMAIL);
					}else{
						this.dbLogger.warn("邮件调用发送失败：" + response.getCode() + ",失败原因：" + response.getMsg());
					}
				}
			}
		}
		return this.showJsonSuccess(modelMap, sendMail + "");
	}
	//结算说明发送邮件
	@RequestMapping("/admin/ticket/sendConfirmEmail.xhtml")
	public String sendConfirmEmail(HttpServletRequest request, Long cid, String email1, String email2, String othercontext, 
			String cinameName,String startTime,String endTime,String priceContent,String content,String content1,
			ModelMap model){
		Cinema cinema = daoService.getObject(Cinema.class, cid);
		if(cinema == null) return show404(model, "该影院不存在或被删除!");
		if(StringUtils.isBlank(email1)) return showJsonError(model, "请填写收件人邮箱！");
		String email = email1;
		if(StringUtils.isNotBlank(email2)) email = email1 + "," + email2;
		Map<String, String[]> map = request.getParameterMap();
		model.put("formMap", map);
		//查询结算影片
		//getAutoSetterInfo(cinema.getId(), model);
		model.put("othercontext", othercontext);
		model.put("cinameName", cinameName);
		model.put("startTime", startTime);
		model.put("endTime", endTime);
		model.put("priceContent", priceContent);
		model.put("content", content);
		model.put("content1", content1);
		//发送邮件
		String title =cinema.getName()+"每周结算退票确认及瓦友反馈意见";
		String template ="mail/settleConfirmMail.vm";
		String jsonData = JsonUtils.writeObjectToJson(model);
		SendMailByOutboxRequest sendMailRequest = new SendMailByOutboxRequest(SendMailByOutboxRequest.OUTBOX_OPERATION021,EmailRecord.SENDER_GEWARA,
				email.replaceAll(",", ";"),title,jsonData,template,new Timestamp(System.currentTimeMillis()));
		SendMailByOutboxResponse response = gmailService.sendMailByOutbox(sendMailRequest);
		//mailService.sendTemplateEmail(EmailRecord.SENDER_GEWARA, title, template, model, email);
		Map sMap = mongoService.findOne(MongoData.NS_AUTO_SETTER_SEND_EMAIL, MongoData.SYSTEM_ID, "" + cid);
		if(sMap == null){
			sMap = new HashMap();
			sMap.put(MongoData.SYSTEM_ID, cid + "");
		}
		for(String key : map.keySet()){
			if(StringUtils.isNotBlank(key) && map.get(key) != null){
				sMap.put(key,StringUtils.join(map.get(key)));
			}
		}
		if(response.isSuccess()){
			mongoService.saveOrUpdateMap(sMap, MongoData.SYSTEM_ID, MongoData.NS_AUTO_SETTER_SEND_EMAIL);
			return showJsonSuccess(model);
		}else{
			this.dbLogger.warn("邮件调用发送失败：" + response.getCode() + ",失败原因：" + response.getMsg());
			return this.showJsonError(model, response.getMsg());
		}
	}
	
	@RequestMapping("/admin/ticket/unionpayWalletMapping.xhtml")
	public String unionpayWalletMapping(ModelMap model){
		List<Map> mappings = mongoService.getMapList(MongoData.NS_UNIONPAY_WALLET_MAPPING);
		model.put("mappings",mappings);
		return "admin/ticket/unionpayWalletMapping.vm";
	}
	
	@RequestMapping("/admin/ticket/addUnionpayWalletMapping.xhtml")
	public String addUnionpayWalletMapping(String billId,long mpid,String tag,ModelMap model){
		Map map = new HashMap();
		map.put(MongoData.SYSTEM_ID,ObjectId.uuid());
		map.put("billId",billId);
		map.put("mpid",mpid);
		map.put("tag",tag);
		mongoService.saveOrUpdateMap(map, MongoData.SYSTEM_ID, MongoData.NS_UNIONPAY_WALLET_MAPPING);
		return showJsonSuccess(model);
	}
	
	@RequestMapping("/admin/ticket/unionpayWalletUrl.xhtml")
	public String unionpayWalletUrl(ModelMap model){
		Map urls = mongoService.findOne(MongoData.NS_UNIONPAY_WALLET_URL, MongoData.SYSTEM_ID , MongoData.NS_UNIONPAY_WALLET_URL);
		model.put("urlMap",urls);
		return "admin/ticket/unionpayWalletUrl.vm";
	}
	
	@RequestMapping("/admin/ticket/addUnionpayWalletUrl.xhtml")
	public String addUnionpayWalletUrl(String movieUrl,String sportUrl,String dramaUrl,ModelMap model){
		Map urls = mongoService.findOne(MongoData.NS_UNIONPAY_WALLET_URL, MongoData.SYSTEM_ID , MongoData.NS_UNIONPAY_WALLET_URL);
		if(urls == null){
			urls = new HashMap();
			urls.put(MongoData.SYSTEM_ID ,  MongoData.NS_UNIONPAY_WALLET_URL);
		}
		urls.put("movieUrl", movieUrl);
		urls.put("sportUrl", sportUrl);
		urls.put("dramaUrl", dramaUrl);
		mongoService.saveOrUpdateMap(urls, MongoData.SYSTEM_ID, MongoData.NS_UNIONPAY_WALLET_URL);
		return showJsonSuccess(model);
	}
	
	@RequestMapping("/admin/ticket/deleteUnionpayWalletMapping.xhtml")
	public String deleteUnionpayWalletMapping(String id,ModelMap model){
		mongoService.removeObjectById( MongoData.NS_UNIONPAY_WALLET_MAPPING, MongoData.SYSTEM_ID, id);
		return showJsonSuccess(model);
	}
	@Autowired@Qualifier("mongoTemplate")
	private MongoTemplate mongoTemplate;
	@RequestMapping("/admin/ticket/wdOrderList.xhtml")
	public String wdOrderOrGewaOrder(Long cinemaId,Date startDate,Date endDate,Boolean isRealTime,ModelMap model){
		if(startDate == null || endDate == null){
			endDate = DateUtil.currentTime();
			startDate = DateUtil.addDay(endDate, -30);
		}
		Date date = startDate;
		if(isRealTime != null && isRealTime){
			ErrorCode<List<WdOrder>> result = remoteTicketService.getWDOrderList(date);
			List<WdOrder> wdOrderList = null;
			if(result.isSuccess()){
				wdOrderList = result.getRetval();
				if(wdOrderList == null){
					wdOrderList = new ArrayList<WdOrder>();
				}
				Map<String, Object> params = new HashMap<String, Object>();
				params.put("date", DateUtil.formatDate(date));
				mongoService.removeObjectList(WDOrderContrast.class.getCanonicalName(), params);
				nosqlService.saveWDOrderContrast(ticketOrderService.wdOrderContrast(date, wdOrderList),wdOrderList, date);
			}else{
				return this.showMessage(model, "强制同步时，拉去万达系统订单出错，原因：" + result.getMsg());
			}
		}
		DBObject queryCondition = new BasicDBObject();
		queryCondition.putAll(mongoService.queryBasicDBObject("addTime", "<=",DateUtil.formatTimestamp(DateUtil.getLastTimeOfDay(endDate))));
		queryCondition.putAll(mongoService.queryBasicDBObject("date", ">=",DateUtil.formatDate(startDate)));
		if(cinemaId != null){
			queryCondition.putAll(mongoService.queryBasicDBObject("cinemaId", "=",cinemaId));
		}
		
		DBObject cinemaGroup = new BasicDBObject("cinemaId", "true");
		DBObject dateGroup = new BasicDBObject("date", "true");
		DBObject groupDB = new BasicDBObject();
		groupDB.putAll(cinemaGroup);
		groupDB.putAll(dateGroup);
		
		DBCollection collection = mongoTemplate.getDb().getCollection(WDOrderContrast.class.getCanonicalName());
		BasicDBObject initial = new BasicDBObject();   
		initial.put("gOrderTotal", 0);
		initial.put("gTicketTotal", 0);
		initial.put("gMoneyTotal", 0);
		initial.put("wOrderTotal", 0);
		initial.put("wTicketTotal", 0);
		initial.put("wMoneyTotal", 0);
		String reduce = "function(obj,prev) {if(obj.orderType == 'WD'){prev.wOrderTotal++;prev.wTicketTotal += obj.seatNum;prev.wMoneyTotal += obj.ticketMoney;} " +
				"if(obj.orderType == 'GEWA'){prev.gOrderTotal++;prev.gTicketTotal += obj.seatNum;prev.gMoneyTotal += obj.ticketMoney;}}";
		List<Map> object = (List<Map>)collection.group(groupDB, queryCondition, initial, reduce, null); 
		Collections.sort(object,new MultiPropertyComparator<Map>(new String[]{"date"}, new boolean[]{false}));
		List<Cinema> cinemaList = daoService.getObjectList(Cinema.class,BeanUtil.getBeanPropertyList(object, Long.class,"cinemaId", true));
		model.put("cinemaMap", BeanUtil.beanListToMap(cinemaList, "id"));
		model.put("cinemaList", cinemaList);
		model.put("contrastList", object);
		model.put("cinemaId", cinemaId);
		model.put("startDate", DateUtil.formatDate(startDate));
		model.put("endDate", DateUtil.formatDate(endDate));
		//this.mongoService.getGroupBy(WDOrderContrast.class.getCanonicalName(), "cinemaId", queryCondition, initial, reduce);
		return "admin/ticket/wdOrderContrastList.vm";
	}
	
	@RequestMapping("/admin/ticket/wdOrderContrastDetail.xhtml")
	public String wdOrderContrastDetail(Long cinemaId,Date date,ModelMap model){
		Map params = new HashMap();
		params.put("cinemaId", cinemaId);
		params.put("date", DateUtil.formatDate(date));
		List<WDOrderContrast> cList = this.mongoService.getObjectList(WDOrderContrast.class, params, "cinemaId", true, 0, 500);
		List<Cinema> cinemaList = daoService.getObjectList(Cinema.class,BeanUtil.getBeanPropertyList(cList, Long.class,"cinemaId", true));
		model.put("cinemaMap", BeanUtil.beanListToMap(cinemaList, "id"));
		model.put("cinemaList", cinemaList);
		model.put("contrastList", cList);
		return "admin/ticket/wdOrderContrastDetail.vm";
	}

}
