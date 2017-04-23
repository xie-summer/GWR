package com.gewara.web.action.inner.mobile.order;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
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

import com.gewara.constant.ApiConstant;
import com.gewara.constant.PaymethodConstant;
import com.gewara.constant.ticket.OrderConstant;
import com.gewara.model.goods.BaseGoods;
import com.gewara.model.movie.Cinema;
import com.gewara.model.pay.BuyItem;
import com.gewara.model.pay.GewaOrder;
import com.gewara.model.pay.GoodsOrder;
import com.gewara.model.pay.TicketOrder;
import com.gewara.model.user.Member;
import com.gewara.pay.PayUtil;
import com.gewara.service.SynchService;
import com.gewara.support.ErrorCode;
import com.gewara.untrans.order.BroadcastOrderService;
import com.gewara.untrans.terminal.TerminalService;
import com.gewara.util.DateUtil;
import com.gewara.util.JsonUtils;
import com.gewara.util.VmUtils;
import com.gewara.web.action.inner.mobile.BaseOpenApiMobileController;
import com.gewara.web.filter.OpenApiMobileAuthenticationFilter;
import com.gewara.xmlbind.terminal.TakeInfo;
@Controller
public class OpenApiMobileBarcodeController extends BaseOpenApiMobileController{
	public static final String BARCODE_ERROR = "4100";
	public static final String DATA_NONE = "4200";
	@Autowired@Qualifier("broadcastOrderService")
	private BroadcastOrderService broadcastOrderService;
	@Autowired@Qualifier("terminalService")
	private TerminalService terminalService;
	@Autowired@Qualifier("synchService")
	private SynchService synchService;
	@RequestMapping("/openapi/mobile/order/ticketOrderPrintMsg.xhtml")
	public String ticketOrderPrintMsg(String tradeNo,  String content, ModelMap model){
		Member member = OpenApiMobileAuthenticationFilter.getOpenApiAuth().getMember();
		ErrorCode code = synchService.selfTicket(tradeNo, member, content);
		if(!code.isSuccess()){
			return getErrorXmlView(model,ApiConstant.CODE_DATA_ERROR, code.getMsg());
		}
		return getSuccessXmlView(model);
	}
	
	@RequestMapping("/openapi/mobile/order/qryTicketOrderListByBarcode.xhtml")
	public String orderList(Long cinemaid, String randcode, String machineno, ModelMap model, HttpServletRequest request){
		dbLogger.warn(":"+cinemaid+", randcode:" + randcode + ", machineno:" + machineno);
		String opkey = cinemaid+randcode+machineno;
		boolean result = operationService.updateOperationOneDay(opkey, true);
		if(!result){
			return getErrorXmlView(model, BARCODE_ERROR, "不能重复操作，请重新获取二维码！");
		}
		Member member = OpenApiMobileAuthenticationFilter.getOpenApiAuth().getMember();
		Timestamp curtime = DateUtil.getCurFullTimestamp();
		Timestamp playtime = DateUtil.addHour(curtime, -2);
		String qry = "from TicketOrder t where t.memberid=? and cinemaid=? and t.playtime>? and t.status=? order by playtime";
		List<TicketOrder> ticketOrderList = hibernateTemplate.find(qry, member.getId(), cinemaid, playtime, OrderConstant.STATUS_PAID_SUCCESS);
		
		qry = "from GoodsOrder t where t.memberid=? and placeid=? and t.addtime>? and t.status=? and t.paymethod!=? order by addtime";
		List<GoodsOrder> goodsOrderList = hibernateTemplate.find(qry, member.getId(), cinemaid, DateUtil.addDay(curtime, -14), OrderConstant.STATUS_PAID_SUCCESS,
				PaymethodConstant.PAYMETHOD_SYSPAY);
		if(ticketOrderList.size()==0 && goodsOrderList.size()==0){
			Cinema cinema = daoService.getObject(Cinema.class, cinemaid);
			return getErrorXmlView(model, DATA_NONE, cinema.getName());
		}
		List<Map<String, Object>> resMapList = new ArrayList<Map<String, Object>>();
		for(TicketOrder order : ticketOrderList){
			Map<String, String> otherMap = VmUtils.readJsonToMap(order.getOtherinfo());
			if(otherMap.containsKey(OrderConstant.ORDER_TAKETIME)){
				continue;
			}
			Map<String, Object> resMap = getMovieOrderMap(order, true);
			if(order.getItemfee()>0){
				BuyItem item = daoService.getObjectByUkey(BuyItem.class, "orderid", order.getId(), false);
				resMap.put("goodsname", item.getGoodsname());
			}
			resMapList.add(resMap);
		}
		for(GoodsOrder order : goodsOrderList){
			Map<String, String> otherMap = VmUtils.readJsonToMap(order.getOtherinfo());
			if(otherMap.containsKey(OrderConstant.ORDER_TAKETIME)){
				continue;
			}
			Map<String, Object> resMap = new HashMap<String, Object>();
			resMap = getGoodsOrderMap(order);
			BaseGoods goods = daoService.getObject(BaseGoods.class, order.getGoodsid());
			if(goods!=null) resMap.put("shortname", goods.getShortname());
			resMap.put("goodslogo", getMobilePath() + goods.getLimg());
			resMap.put("tradeNo", order.getTradeNo());
			resMap.put("ordertitle", order.getOrdertitle());
			resMapList.add(resMap);
		}
		if(resMapList.size()==0){
			Cinema cinema = daoService.getObject(Cinema.class, cinemaid);
			return getErrorXmlView(model, DATA_NONE, cinema.getName());
		}
		return getOpenApiXmlList(resMapList, "ticketOrderList,ticketOrder", model, request);
	}
	
	@RequestMapping("/openapi/mobile/order/printTicketOrderListByTradenos.xhtml")
	public String orderList(Long cinemaid, String tradenos, String randcode, String machineno, ModelMap model){
		Member member = OpenApiMobileAuthenticationFilter.getOpenApiAuth().getMember();
		dbLogger.warn(":"+cinemaid+", randcode:" + randcode + ", machineno:" + machineno);
		List<String> tradenoList = new ArrayList<String>();
		List<String> noList = Arrays.asList(tradenos.split(","));
		for(String no : noList){
			GewaOrder order = daoService.getObjectByUkey(GewaOrder.class, "tradeNo", no);
			if(order!=null){
				Map<String, String> otherMap = VmUtils.readJsonToMap(order.getOtherinfo());
				if(!otherMap.containsKey(OrderConstant.ORDER_TAKETIME)){
					TakeInfo takeInfo = terminalService.getTakeInfo(order);
					if(takeInfo!=null && takeInfo.getTaketime()!=null){
						otherMap.put(OrderConstant.ORDER_TAKETIME, DateUtil.formatTimestamp(takeInfo.getTaketime()));
						order.setOtherinfo(JsonUtils.writeMapToJson(otherMap));
						daoService.saveObject(order);
					}
				}
				if(!order.getMemberid().equals(member.getId())){
					return getErrorXmlView(model, ApiConstant.CODE_DATA_ERROR, "不能操作他人订单！");
				}
				String tmp = "";
				if(order instanceof TicketOrder){
					TicketOrder torder = (TicketOrder)order;
					if(!torder.getCinemaid().equals(cinemaid)){
						return getErrorXmlView(model, ApiConstant.CODE_DATA_ERROR, "非该场馆的订单！");
					}
					tmp = getTnos(torder);
				}else{
					tmp = order.getTradeNo();
				}
				tradenoList.add(tmp);
			}
		}
		if(tradenoList.size()==0){
			return getErrorXmlView(model, ApiConstant.CODE_DATA_ERROR, "没有查询到需要打印的订单！");
		}
		String newtradenos = StringUtils.join(tradenoList, "|");
		dbLogger.warn("newtradenos" + newtradenos);
		broadcastOrderService.broadcastBarcode(newtradenos, cinemaid, randcode, machineno);
		return getSuccessXmlView(model);
	}
	private String getTnos(TicketOrder order){
		String tmp = order.getTradeNo();
		if(order.getItemfee()>0){
			GoodsOrder gorder = daoService.getObjectByUkey(GoodsOrder.class, "tradeNo", PayUtil.FLAG_GOODS + order.getTradeNo().substring(1));
			if(order!=null){
				tmp = tmp + "@" + gorder.getTradeNo();
			}
		}
		return tmp;
	}
	@RequestMapping("/openapi/mobile/order/validPrintListByTradenos.xhtml")
	public String validPrintListByTradenos(String tradenos, ModelMap model){
		List<String> noList = Arrays.asList(tradenos.split(","));
		for(String no : noList){
			GewaOrder order = daoService.getObjectByUkey(GewaOrder.class, "tradeNo", no);
			if(order!=null){
				Map<String, String> otherMap = VmUtils.readJsonToMap(order.getOtherinfo());
				if(otherMap.containsKey(OrderConstant.ORDER_TAKETIME)){
					return getSingleResultXmlView(model, 1);
				}else {
					TakeInfo takeInfo = terminalService.getTakeInfo(order);
					if(takeInfo!=null && takeInfo.getTaketime()!=null){
						otherMap.put(OrderConstant.ORDER_TAKETIME, DateUtil.formatTimestamp(takeInfo.getTaketime()));
						order.setOtherinfo(JsonUtils.writeMapToJson(otherMap));
						daoService.saveObject(order);
						return getSingleResultXmlView(model, 1);
					}else {
						return getSingleResultXmlView(model, 0);
					}
				}
			}
		}
		return getSingleResultXmlView(model, 0);
	}
}
