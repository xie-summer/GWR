package com.gewara.web.action.admin.order;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.RequestMapping;

import com.gewara.command.GewaOrderMap;
import com.gewara.command.OrderParamsCommand;
import com.gewara.constant.GoodsConstant;
import com.gewara.constant.PayConstant;
import com.gewara.constant.Status;
import com.gewara.constant.TagConstant;
import com.gewara.constant.ticket.OpiConstant;
import com.gewara.constant.ticket.OrderConstant;
import com.gewara.constant.ticket.OrderExtraConstant;
import com.gewara.constant.ticket.RefundConstant;
import com.gewara.model.acl.User;
import com.gewara.model.api.ApiUser;
import com.gewara.model.drama.Drama;
import com.gewara.model.drama.DramaOrder;
import com.gewara.model.drama.OpenDramaItem;
import com.gewara.model.drama.Theatre;
import com.gewara.model.goods.ActivityGoods;
import com.gewara.model.goods.BaseGoods;
import com.gewara.model.goods.Goods;
import com.gewara.model.movie.Cinema;
import com.gewara.model.movie.CinemaProfile;
import com.gewara.model.movie.Movie;
import com.gewara.model.pay.BuyItem;
import com.gewara.model.pay.Discount;
import com.gewara.model.pay.GewaOrder;
import com.gewara.model.pay.GoodsOrder;
import com.gewara.model.pay.GymOrder;
import com.gewara.model.pay.OrderAddress;
import com.gewara.model.pay.OrderExtra;
import com.gewara.model.pay.OrderNote;
import com.gewara.model.pay.OrderRefund;
import com.gewara.model.pay.OtherFeeDetail;
import com.gewara.model.pay.PayMethod;
import com.gewara.model.pay.PubSale;
import com.gewara.model.pay.PubSaleOrder;
import com.gewara.model.pay.SpecialDiscount;
import com.gewara.model.pay.SportOrder;
import com.gewara.model.pay.TicketOrder;
import com.gewara.model.sport.OpenTimeTable;
import com.gewara.model.sport.Sport;
import com.gewara.model.sport.SportItem;
import com.gewara.model.ticket.OpenPlayItem;
import com.gewara.service.drama.DramaPlayItemService;
import com.gewara.service.drama.OpenDramaService;
import com.gewara.service.gewapay.PaymentService;
import com.gewara.service.movie.MCPService;
import com.gewara.service.order.OrderQueryService;
import com.gewara.support.ErrorCode;
import com.gewara.support.FirstLetterComparator;
import com.gewara.untrans.activity.SynchActivityService;
import com.gewara.untrans.gym.SynchGymService;
import com.gewara.untrans.terminal.TerminalService;
import com.gewara.util.BeanUtil;
import com.gewara.util.DateUtil;
import com.gewara.web.action.admin.BaseAdminController;
import com.gewara.web.util.PageUtil;
import com.gewara.xmlbind.activity.RemoteActivity;
import com.gewara.xmlbind.gym.CardItem;
import com.gewara.xmlbind.gym.RemoteGym;
import com.gewara.xmlbind.terminal.TakeInfo;

@Controller
public class GewaOrderAdminController extends BaseAdminController {

	@Autowired@Qualifier("orderQueryService")
	private OrderQueryService orderQueryService;
	
	@Autowired@Qualifier("mcpService")
	private MCPService mcpService;
	
	@Autowired@Qualifier("dramaPlayItemService")
	private DramaPlayItemService dramaPlayItemService;
	
	@Autowired@Qualifier("openDramaService")
	private OpenDramaService openDramaService;

	@Autowired@Qualifier("synchGymService")
	private SynchGymService synchGymService;
	
	@Autowired@Qualifier("synchActivityService")
	private SynchActivityService synchActivityService;
	@Autowired@Qualifier("terminalService")
	private TerminalService terminalService;
	@Autowired@Qualifier("paymentService")
	private PaymentService paymentService;
	@RequestMapping("/admin/order/console.xhtml")
	public String orderList(ModelMap model){
		User user = getLogonUser();
		model.put("user", user);
		return "admin/order/console.vm";
	}
	
	@RequestMapping("/admin/order/orderList.xhtml")
	public String orderList(OrderParamsCommand command, ModelMap model){
		Timestamp cur = DateUtil.getCurFullTimestamp();
		checkParams(cur, command);
		if (StringUtils.isNotBlank(command.getErrorMsg())) {
			model.put("statusMap", OrderConstant.statusMap);
			model.put("command", command);
			return "admin/order/orderList.vm";
		}
		List<GewaOrder> orderList = new ArrayList<GewaOrder>();
		int rowsPerPage = 100;
		int firstPre = command.getPageNo() * rowsPerPage;
		if(command.getStarttime() == null && command.getEndtime() == null){
			command.setStarttime(DateUtil.addDay(DateUtil.getBeginningTimeOfDay(cur), -5));
			command.setEndtime(cur);
		}
		int rowsCount = orderQueryService.getOrderCount(command);
		if(rowsCount >0){
			orderList = orderQueryService.getOrderList(command, firstPre, rowsPerPage);
		}
		model.put("rowsCount", rowsCount);
		model.put("statusMap", OrderConstant.statusMap);
		PageUtil pageUtil = new PageUtil(rowsCount, rowsPerPage, command.getPageNo(), "admin/order/orderList.xhtml", true, true);
		Map<String,String> params = BeanUtil.getSimpleStringMap(command);
		pageUtil.initPageInfo(params);
		model.put("pageUtil", pageUtil);
		model.put("command", command);
		List<GewaOrderMap> orderMapList = getGewaOrderMapList(orderList);
		model.put("orderMapList", orderMapList);
		return "admin/order/orderList.vm";
	}
	
	private List<GewaOrderMap> getGewaOrderMapList(List<GewaOrder> orderList){
		List<GewaOrderMap> orderMapList = new ArrayList<GewaOrderMap>();
		if(CollectionUtils.isEmpty(orderList)) return orderMapList;
		for (GewaOrder order : orderList) {
			GewaOrderMap orderMap = new GewaOrderMap(order);
			setOrderMapPropertiy(orderMap, false);
			orderMapList.add(orderMap);
		}
		return orderMapList;
	}
	
	private void setOrderMapPropertiy(GewaOrderMap orderMap, boolean isItem){
		GewaOrder order = orderMap.getOrder();
		if(isItem){
			List<BuyItem> buyItemList = daoService.getObjectListByField(BuyItem.class, "orderid", order.getId());
			orderMap.setBuyItemList(buyItemList);
		}
		if(order instanceof TicketOrder){
			TicketOrder ticketOrder = (TicketOrder) order;
			OpenPlayItem item = daoService.getObjectByUkey(OpenPlayItem.class, "mpid", ticketOrder.getMpid(), true);
			orderMap.setSchedule(item);
			Cinema cinema = daoService.getObject(Cinema.class, ticketOrder.getCinemaid());
			orderMap.setPlace(cinema);
			CinemaProfile profile = daoService.getObject(CinemaProfile.class, ticketOrder.getCinemaid());
			orderMap.setProfile(profile);
			Movie movie = daoService.getObject(Movie.class, ticketOrder.getMovieid());
			orderMap.setItem(movie);
		}else if(order instanceof DramaOrder){
			DramaOrder dramaOrder = (DramaOrder) order;
			OpenDramaItem item = daoService.getObjectByUkey(OpenDramaItem.class, "dpid", dramaOrder.getDpid(), true);
			orderMap.setSchedule(item);
			Theatre theatre = daoService.getObject(Theatre.class, dramaOrder.getTheatreid());
			orderMap.setPlace(theatre);
			Drama drama = daoService.getObject(Drama.class, dramaOrder.getDramaid());
			orderMap.setItem(drama);
		}else if(order instanceof SportOrder){
			SportOrder sportOrder = (SportOrder)order;
			OpenTimeTable ott = daoService.getObject(OpenTimeTable.class, sportOrder.getOttid());
			orderMap.setSchedule(ott);
			Sport sport = daoService.getObject(Sport.class, sportOrder.getSportid());
			orderMap.setPlace(sport);
			SportItem item = daoService.getObject(SportItem.class, sportOrder.getItemid());
			orderMap.setItem(item);
		}else if(order instanceof PubSaleOrder){
			PubSaleOrder pubSaleOrder = (PubSaleOrder) order;
			PubSale pubSale = daoService.getObject(PubSale.class, pubSaleOrder.getPubid());
			orderMap.setSchedule(pubSale);
			if(pubSale != null){
				BaseGoods goods = daoService.getObject(BaseGoods.class, pubSale.getGoodsid());
				orderMap.setRelate(goods);
			}
		}else if(order instanceof GymOrder){
			GymOrder gymOrder = (GymOrder)order;
			ErrorCode<RemoteGym> gymCode = synchGymService.getRemoteGym(gymOrder.getGymid(), true);
			if(gymCode.isSuccess()){
				orderMap.setPlace(gymCode.getRetval());
			}
			ErrorCode<CardItem> cardCode = synchGymService.getGymCardItem(gymOrder.getGci(), true);
			if(cardCode.isSuccess()){
				CardItem cardItem = cardCode.getRetval();
				orderMap.setSchedule(cardItem);
			}
		}else if(order instanceof GoodsOrder){
			GoodsOrder goodsOrder = (GoodsOrder) order;
			BaseGoods goods = daoService.getObject(BaseGoods.class, goodsOrder.getGoodsid());
			orderMap.setSchedule(goods);
			if(goods instanceof ActivityGoods){
				ErrorCode<RemoteActivity> activity = synchActivityService.getRemoteActivity(goods.getRelatedid());
				if(activity.isSuccess()){
					orderMap.setRelate(activity.getRetval());
				}
			}else if(goods instanceof Goods){
				if(StringUtils.equals(goods.getTag(), TagConstant.TAG_CINEMA)){
					Cinema cinema = daoService.getObject(Cinema.class, goods.getRelatedid());
					orderMap.setPlace(cinema);
					Movie movie = daoService.getObject(Movie.class, goods.getItemid());
					orderMap.setItem(movie);
				}else if(StringUtils.equals(goods.getTag(), TagConstant.TAG_THEATRE)){
					Theatre theatre = daoService.getObject(Theatre.class, goods.getRelatedid());
					orderMap.setPlace(theatre);
					Drama drama = daoService.getObject(Drama.class, goods.getItemid());
					orderMap.setItem(drama);
				}else if(StringUtils.equals(goods.getTag(), TagConstant.TAG_SPORT)){
					Sport sport = daoService.getObject(Sport.class, goods.getRelatedid());
					orderMap.setPlace(sport);
					SportItem item = daoService.getObject(SportItem.class, goods.getItemid());
					orderMap.setItem(item);
				}
			}
		}
	}
	
	@RequestMapping("/admin/order/ticketOrderList.xhtml")
	public String ticketOrderList(OrderParamsCommand command, String place, String item, ModelMap model){
		Timestamp cur = DateUtil.getCurFullTimestamp();
		command.setOrdertype(OrderConstant.ORDER_TYPE_TICKET);
		checkParams(cur, command);
		if (StringUtils.isNotBlank(command.getErrorMsg())) {
			model.put("place", place);
			model.put("item", item);
			model.put("command", command);
			return "admin/order/ticketOrderList.vm";
		}
		List<TicketOrder> orderList = new ArrayList<TicketOrder>();
		int rowsPerPage = 100;
		int firstPre = command.getPageNo() * rowsPerPage;
		int rowsCount = orderQueryService.getOrderCount(command);
		if(rowsCount >0){
			orderList = orderQueryService.getTicketOrderList(command, place, item, firstPre, rowsPerPage);
		}
		model.put("rowsCount", rowsCount);
		PageUtil pageUtil = new PageUtil(rowsCount, rowsPerPage, command.getPageNo(), "admin/order/ticketOrderList.xhtml", true, true);
		Map<String,String> params = BeanUtil.getSimpleStringMap(command);
		pageUtil.initPageInfo(params);
		model.put("pageUtil", pageUtil);
		
		
		List<Long> mpidList = BeanUtil.getBeanPropertyList(orderList, Long.class, "mpid", true);
		List<OpenPlayItem> opiList = new ArrayList<OpenPlayItem>();
		for(Long mpid: mpidList) {
			opiList.add(daoService.getObjectByUkey(OpenPlayItem.class, "mpid", mpid, true));
		}
		List<Movie> movieList = mcpService.getCurMovieList();
		Collections.sort(movieList, new FirstLetterComparator());
		
		Map<Long, OpenPlayItem> opiMap = BeanUtil.beanListToMap(opiList, "mpid");
		model.put("orderList", orderList);
		model.put("opiMap", opiMap);
		model.put("place", place);
		model.put("item", item);
		model.put("command", command);
		return "admin/order/ticketOrderList.vm";
	}
	
	private void checkParams(Timestamp cur, OrderParamsCommand command){
		if (StringUtils.isBlank(command.getLevel())){
			command.setStatus(OrderConstant.STATUS_PAID_FAILURE);
			command.setEndtime(cur);
			command.setStarttime(DateUtil.addDay(DateUtil.getBeginningTimeOfDay(cur), -5));
			command.setLevel("1");
		}else if (StringUtils.equals(command.getLevel(), "2")){
			if(StringUtils.isBlank(command.getTradeno()) && StringUtils.isBlank(command.getMobile())){
				command.setErrorMsg("订单号,手机号不能同时为空！");
			}
		}
		if (StringUtils.isBlank(command.getTradeno()) && StringUtils.isBlank(command.getMobile()) && command.getMemberid() == null) {
			if(command.getStarttime() == null || command.getEndtime() == null){
				command.setErrorMsg("交易时段范围不能为空！");
			}
			if (DateUtil.getDiffDay(command.getEndtime(), command.getStarttime()) > 5) {
				command.setErrorMsg("查询时间间隔不得大于5天！");
			}
		}
	}
	
	@RequestMapping("/admin/order/dramaOrderList.xhtml")
	public String orderList(OrderParamsCommand command, HttpServletRequest request, HttpServletResponse response, ModelMap model){
		final String viewPage = "admin/order/dramaOrderList.vm";
		Timestamp cur = DateUtil.getCurFullTimestamp();
		command.setOrdertype(OrderConstant.ORDER_TYPE_DRAMA);
		if (StringUtils.isBlank(command.getTradeno()) && StringUtils.isBlank(command.getMobile())) {
			checkParams(cur, command);
			if (StringUtils.isNotBlank(command.getErrorMsg())) {
				String citycode = getAdminCitycode(request);
				command.setCitycode(citycode);
				model.put("theatre", daoService.getObject(Theatre.class, command.getPlaceid()));
				List<Long> dramaIdList = openDramaService.getCurDramaidList(command.getPlaceid());
				Map<Long, Drama> dramaMap = daoService.getObjectMap(Drama.class, dramaIdList);
				List<Long> idList = dramaPlayItemService.getCurBookingTheatreList(citycode, null);
				Map<Long, Theatre> theatreMap = daoService.getObjectMap(Theatre.class, idList);
				model.put("theatreMap", theatreMap);
				model.put("dramaMap", dramaMap);
				model.put("command", command);
				return viewPage;
			}
		}
		String citycode = getAdminCitycode(request);
		List<Long> idList = dramaPlayItemService.getCurBookingTheatreList(citycode, null);
		Map<Long, Theatre> theatreMap = daoService.getObjectMap(Theatre.class, idList);
		model.put("theatreMap", theatreMap);
		model.put("command", command);
		if(StringUtils.isNotBlank(command.getErrorMsg())){
			return viewPage;
		}
		List<DramaOrder> orderList = new ArrayList<DramaOrder>();
		int rowsPerPage = 100;
		int firstPre = command.getPageNo() * rowsPerPage;
		int rowsCount = orderQueryService.getOrderCount(command);
		if(rowsCount >0){
			orderList = orderQueryService.getOrderList(DramaOrder.class, command, firstPre, rowsPerPage);
		}
		model.put("rowsCount", rowsCount);
		PageUtil pageUtil = new PageUtil(rowsCount, rowsPerPage, command.getPageNo(), "admin/order/dramaOrderList.xhtml", true, true);
		Map<String,String> params = BeanUtil.getSimpleStringMap(command);
		pageUtil.initPageInfo(params);
		model.put("pageUtil", pageUtil);

		int totalQuantity = 0;
		int tTotalAmount = 0;
		if(Boolean.parseBoolean(command.getXls())){
			downLoadXls(orderList, response, model);
			return "admin/theatreticket/orders.vm";
		}else{
			if(command.getPlaceid() != null){
				model.put("theatre", daoService.getObject(Theatre.class, command.getPlaceid()));
				List<Long> dramaIdList = openDramaService.getCurDramaidList(command.getPlaceid());
				Map<Long, Drama> dramaMap = daoService.getObjectMap(Drama.class, dramaIdList);
				model.put("dramaMap", dramaMap);
				List<OpenDramaItem> opiList = openDramaService.getOdiList(citycode, command.getPlaceid(), command.getItemid(), cur, null, false);
				model.put("opiList", opiList);
			}
			StringBuffer sb = new StringBuffer();
			for (GewaOrder order : orderList) {
				totalQuantity+=order.getQuantity();
				tTotalAmount+=order.getTotalAmount();
				sb.append(order.getId()).append(",");
			}
			model.put("orderList", orderList);
			model.put("totalQuantity", totalQuantity);
			model.put("tTotalAmount", tTotalAmount);
			model.put("orderids", sb.toString());
			return viewPage;
		}
	}
	
	private void downLoadXls(List<DramaOrder> orderList, HttpServletResponse response, ModelMap model){
		List<String> tradeNoList = BeanUtil.getBeanPropertyList(orderList, "tradeNo", true);
		Map<String, OrderAddress> addressMap = daoService.getObjectMap(OrderAddress.class, tradeNoList);
		Map<Long, Map<Long, OrderNote>> noteMap = new HashMap<Long, Map<Long, OrderNote>>();
		Map<Long, List<BuyItem>> itemMap = new HashMap<Long, List<BuyItem>>();
		Set<Long> theatreIdSet = new HashSet<Long>();
		Set<Long> dramaIdSet = new HashSet<Long>();
		for (GewaOrder order : orderList) {
			List<BuyItem> itemList = daoService.getObjectListByField(BuyItem.class, "orderid", order.getId());
			itemMap.put(order.getId(), itemList);
			for (BuyItem buyItem : itemList) {
				theatreIdSet.add(buyItem.getPlaceid());
				dramaIdSet.add(buyItem.getItemid());
			}
			if(order.isPaidSuccess()){
				List<OrderNote> noteList = daoService.getObjectListByField(OrderNote.class, "orderid", order.getId());
				Map<Long,OrderNote> tmpMap = BeanUtil.beanListToMap(noteList, "smallitemid");
				noteMap.put(order.getId(), tmpMap);
			}
		}
		model.put("noteMap", noteMap);
		model.put("itemMap", itemMap);
		model.put("addressMap", addressMap);
		Map<Long, Theatre> orderTheatreMap = daoService.getObjectMap(Theatre.class, theatreIdSet);
		Map<Long, Drama> orderDramaMap = daoService.getObjectMap(Drama.class, dramaIdSet);
		model.put("orderTheatreMap", orderTheatreMap);
		model.put("orderDramaMap", orderDramaMap);
		model.put("orderList", orderList);
		downloadXls("xls", response);
	}
	
	@RequestMapping("/admin/order/orderDetail.xhtml")
	public String orderDetail(String tradeNo, ModelMap model){
		GewaOrder order = daoService.getObjectByUkey(GewaOrder.class, "tradeNo", tradeNo);
		final String viewPage = "admin/order/orderInfo.vm";
		if(order == null){
			return viewPage;
		}
		GewaOrderMap orderMap = new GewaOrderMap(order);
		setOrderMapPropertiy(orderMap, true);
		model.put("orderMap", orderMap);
		if(order.getDiscount()>0){
			List<Discount> discountList = paymentService.getOrderDiscountList(order);
			model.put("discountList", discountList);
			Map<String,List<Discount>> disMap = BeanUtil.groupBeanList(discountList, "tag");
			List<Discount> partnerDisList = disMap.get(PayConstant.DISCOUNT_TAG_PARTNER);
			if(!CollectionUtils.isEmpty(partnerDisList)){
				SpecialDiscount sd = daoService.getObject(SpecialDiscount.class, partnerDisList.get(0).getRelatedid());
				model.put("sd", sd);
			}
		}
		if(order.getOtherfee()>0){
			List<OtherFeeDetail> feeList = daoService.getObjectListByField(OtherFeeDetail.class, "orderid", order.getId());
			model.put("feeList", feeList);
		}
		OrderRefund refund = daoService.getObjectByUkey(OrderRefund.class, "tradeno", order.getTradeNo());
		model.put("refund", refund);
		if(refund != null){
			User applyUser = daoService.getObject(User.class, refund.getApplyuser());
			model.put("applyUser", applyUser);
		}
		if(order.isPaidSuccess()){
			List<OrderNote> noteList = daoService.getObjectListByField(OrderNote.class, "orderid", order.getId());
			Map<Long, OrderNote> noteMap = BeanUtil.beanListToMap(noteList, "smallitemid");
			model.put("noteMap", noteMap);
		}
		if(StringUtils.equals(order.getExpress(), Status.Y)){
			OrderAddress orderAddress = daoService.getObject(OrderAddress.class, order.getTradeNo());
			model.put("orderAddress", orderAddress);
		}
		OrderExtra orderExtra = daoService.getObjectByUkey(OrderExtra.class, "tradeno", order.getTradeNo());
		model.put("orderExtra", orderExtra);
		model.put("expressStatusMap", OrderExtraConstant.EXPRESS_STATUS_TEXT);
		ApiUser apiUser = daoService.getObject(ApiUser.class, order.getPartnerid());
		model.put("apiUser", apiUser);
		model.put("refundTypeMap", RefundConstant.refundTypeMap);
		model.put("reasonTypeMap", RefundConstant.reasonTypeMap);
		model.put("retbackMap", RefundConstant.retbackMap);
		model.put("takemethodMap", OpiConstant.takemethodMap);
		model.put("feetypeMap", GoodsConstant.feetypeMap);
		PayMethod payMethod = daoService.getObject(PayMethod.class, order.getPaymethod());
		model.put("payMethod", payMethod);
		return viewPage;
	}
	
	@RequestMapping("/admin/order/orderTakeInfo.xhtml")
	public String orderTakeInfo(String tradeNo, ModelMap model){
		String vm = "admin/order/orderTakeInfo.vm";
		tradeNo = StringUtils.trim(tradeNo);
		if(StringUtils.isBlank(tradeNo)) return vm;
		GewaOrder order = daoService.getObjectByUkey(GewaOrder.class, "tradeNo", tradeNo, false);
		if(order==null){
			OrderNote orderNote = daoService.getObjectByUkey(OrderNote.class, "serialno", tradeNo);
			if(orderNote==null){
				return showJsonError_NOT_FOUND(model);
			}
			order = daoService.getObjectByUkey(GewaOrder.class, "tradeNo",  orderNote.getTradeno(), false);
		}
		if(order instanceof TicketOrder){
			CinemaProfile profile = daoService.getObject(CinemaProfile.class, ((TicketOrder)order).getCinemaid());
			model.put("profile", profile);
		}
		ErrorCode<List<TakeInfo>> ticode = terminalService.getTakeInfoList(tradeNo);
		model.put("takeInfoList", ticode.getRetval());
		model.put("takemethodMap", OpiConstant.takemethodMap);
		model.put("order", order);
		return vm;
	}
}
