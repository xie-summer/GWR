package com.gewara.untrans.drama.impl;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.gewara.Config;
import com.gewara.api.gpticket.vo.command.OrderItemVo;
import com.gewara.api.gpticket.vo.ticket.DramaRemoteOrderVo;
import com.gewara.constant.ApiConstant;
import com.gewara.constant.OdiConstant;
import com.gewara.constant.order.BuyItemConstant;
import com.gewara.constant.sys.ErrorCodeConstant;
import com.gewara.constant.ticket.OrderConstant;
import com.gewara.model.drama.DisQuantity;
import com.gewara.model.drama.DramaOrder;
import com.gewara.model.drama.OpenDramaItem;
import com.gewara.model.drama.QryItemResponse;
import com.gewara.model.drama.SellDramaSeat;
import com.gewara.model.drama.TheatreSeatArea;
import com.gewara.model.drama.TheatreSeatPrice;
import com.gewara.model.pay.BuyItem;
import com.gewara.service.DaoService;
import com.gewara.service.OrderException;
import com.gewara.service.drama.DramaOrderService;
import com.gewara.support.ErrorCode;
import com.gewara.untrans.drama.RemoteDramaService;
import com.gewara.untrans.drama.TheatreOperationService;
import com.gewara.untrans.monitor.MonitorService;
import com.gewara.untrans.monitor.OrderMonitorService;
import com.gewara.untrans.monitor.SysLogType;
import com.gewara.util.BeanUtil;
import com.gewara.util.DateUtil;
import com.gewara.util.GewaLogger;
import com.gewara.util.JsonUtils;
import com.gewara.util.LoggerUtils;
import com.gewara.util.StringUtil;
import com.gewara.xmlbind.ticket.TicketRemoteOrder;

@Service("theatreOperationService")
public class TheatreOperationServiceImpl implements TheatreOperationService{
	private final transient GewaLogger dbLogger = LoggerUtils.getLogger(getClass(), Config.getServerIp(), Config.SYSTEMID);
	
	private AtomicInteger hitCount = new AtomicInteger();
	private Long starttime = System.currentTimeMillis();
	
	@Autowired@Qualifier("monitorService")
	private MonitorService monitorService;
	
	@Autowired@Qualifier("daoService")
	private DaoService daoService;
	public void setDaoService(DaoService daoService) {
		this.daoService = daoService;
	}
	
	@Autowired@Qualifier("orderMonitorService")
	private OrderMonitorService orderMonitorService;

	@Autowired@Qualifier("remoteDramaService")
	private RemoteDramaService remoteDramaService;
	
	@Autowired@Qualifier("dramaOrderService")
	private DramaOrderService dramaOrderService;
	
	@Override
	public ErrorCode<DramaRemoteOrderVo> createDramaRemoteOrder(OpenDramaItem odi, DramaOrder order, String mobile, List<SellDramaSeat> seatList, List<BuyItem> itemList){
		Set<String> areaSeqnoSet = new HashSet<String>();
		if(odi.isOpenseat()){
			if(CollectionUtils.isEmpty(seatList)) return ErrorCode.getFailure(ApiConstant.CODE_SIGN_ERROR, "×ùÎ»´íÎó£¡");
			TheatreSeatArea seatArea = daoService.getObject(TheatreSeatArea.class, order.getAreaid());
			ErrorCode<List<OrderItemVo>> code = getOrderItemVo(OdiConstant.OPEN_TYPE_SEAT, seatArea, itemList, seatList, areaSeqnoSet);
			if(!code.isSuccess()) return ErrorCode.getFailure(code.getErrcode(), code.getMsg());
			return remoteDramaService.newCreateOrder(odi.getSellerseq(), order.getId(), mobile, seatArea.getSellerseq(), OdiConstant.OPEN_TYPE_SEAT, JsonUtils.writeObjectToJson(code.getRetval()));
		}else{
			if(CollectionUtils.isEmpty(itemList)) return ErrorCode.getFailure(ApiConstant.CODE_SIGN_ERROR, "¼Û¸ñ´íÎó£¡");
			ErrorCode<List<OrderItemVo>> code = getOrderItemVo(OdiConstant.OPEN_TYPE_PRICE, null, itemList, seatList, areaSeqnoSet);
			if(!code.isSuccess()) return ErrorCode.getFailure(code.getErrcode(), code.getMsg());
			return remoteDramaService.newCreateOrder(odi.getSellerseq(), order.getId(), mobile, StringUtils.join(areaSeqnoSet, ","), OdiConstant.OPEN_TYPE_PRICE, JsonUtils.writeObjectToJson(code.getRetval()));
		}
	}
	
	@Override
	public ErrorCode lockRemotePrice(OpenDramaItem odi, DramaOrder order, String mobile, List<BuyItem> itemList){
		if(odi.hasSeller(OdiConstant.PARTNER_GEWA)) return ErrorCode.SUCCESS;
		if(!odi.isOpenprice()) return ErrorCode.getFailure(ApiConstant.CODE_SIGN_ERROR, "·ÇÑ¡¼Û¸ñ³¡´Î£¡");
		try{
			ErrorCode<DramaRemoteOrderVo> orderCode = createDramaRemoteOrder(odi, order, mobile, null, itemList);
			if(!orderCode.isSuccess()) return ErrorCode.getFailure(orderCode.getErrcode(), orderCode.getMsg());
			DramaRemoteOrderVo remoteOrder = orderCode.getRetval();
			return remoteDramaService.newLockPrice(odi.getSellerseq(), order.getId(), mobile, remoteOrder.getAreaseqno(), remoteOrder.getSeatlabel());
		}catch (Exception e) {
			dbLogger.warn("", e);
			return ErrorCode.getFailure(ApiConstant.CODE_SIGN_ERROR, "¹ºÆ±Ê§°Ü£¡");
		}
	}
	
	private ErrorCode<List<String>> getPriceList(Map<Long, TheatreSeatPrice> seatPriceMap, List<BuyItem> itemList, Set<String> areaSeqnoSet){
		Map<TheatreSeatPrice, Integer> priceMap = new Hashtable<TheatreSeatPrice, Integer>();
		for (BuyItem buyItem : itemList) {
			TheatreSeatPrice seatPrice = seatPriceMap.get(buyItem.getSmallitemid());
			if(seatPrice == null) continue;
			
			Integer quantity = priceMap.get(seatPrice);
			if(quantity == null){
				quantity = buyItem.getQuantity();
			}else{
				quantity += buyItem.getQuantity();
			}
			priceMap.put(seatPrice, quantity);
		}
		Map<Long,TheatreSeatArea> seatAreaMap = new HashMap<Long, TheatreSeatArea>();
		List<String> seatnoList = new ArrayList<String>();
		for (TheatreSeatPrice seatPrice : priceMap.keySet()) {
			TheatreSeatArea seatArea = seatAreaMap.get(seatPrice.getAreaid());
			if(seatArea == null){
				seatArea = daoService.getObject(TheatreSeatArea.class, seatPrice.getAreaid());
				if(seatArea == null) return ErrorCode.getFailure(ApiConstant.CODE_SIGN_ERROR, "ÇøÓò²»´æÔÚ£¡");
			}
			String seatLabel = seatArea.getSellerseq() + ":" + seatPrice.getSispseq() + ":" + priceMap.get(seatPrice);
			seatnoList.add(seatLabel);
			areaSeqnoSet.add(seatArea.getSellerseq());
		}
		if(seatnoList.isEmpty()) return ErrorCode.getFailure(ApiConstant.CODE_SIGN_ERROR, "¶©µ¥Êý¾Ý´íÎó£¡");
		return ErrorCode.getSuccessReturn(seatnoList);
	}
	
	@Override
	public ErrorCode lockRemoteSeat(OpenDramaItem odi, DramaOrder order, String mobile, List<SellDramaSeat> seatList, List<BuyItem> itemList) {
		if(odi.hasSeller(OdiConstant.PARTNER_GEWA)) return ErrorCode.SUCCESS;
		try{
			ErrorCode<DramaRemoteOrderVo> orderCode = createDramaRemoteOrder(odi, order, mobile, seatList, itemList);
			if(!orderCode.isSuccess()) return ErrorCode.getFailure(orderCode.getErrcode(), orderCode.getMsg());
			DramaRemoteOrderVo remoteOrder = orderCode.getRetval();
			return remoteDramaService.newLockSeat(odi.getSellerseq(), order.getId(), mobile, remoteOrder.getAreaseqno(), remoteOrder.getSeatlabel());
		}catch (Exception e) {
			dbLogger.warn("", e);
			return ErrorCode.getFailure(ApiConstant.CODE_SIGN_ERROR, "¹ºÆ±Ê§°Ü£¡");
		}
	}
	
	@Override
	public ErrorCode unlockRemoteSeat(Long orderid) {
		try {
			return remoteDramaService.newUnRemoteOrder(orderid);
		} catch (Exception e) {
			dbLogger.warn("", e);
			return ErrorCodeConstant.DATEERROR;
		}
	}

	@Override
	public ErrorCode setAndFixRemoteOrder(OpenDramaItem odi, DramaOrder order) {
		if(!odi.hasSeller(OdiConstant.PARTNER_GEWA)){
			List<SellDramaSeat> seatList = dramaOrderService.getDramaOrderSeatList(order.getId());
			List<BuyItem> itemList = daoService.getObjectListByField(BuyItem.class, "orderid", order.getId());
			Long time1 = System.currentTimeMillis();
			boolean retry = false;
			ErrorCode code = null;
			String msg = "retry fixOrder: " + order.getId();
			try{
				code = fixRemoteOrder(odi, order, order.getMobile(), seatList, itemList);
				if(!code.isSuccess()) {
					retry = true;
					msg += ", " + code.getErrcode() + "," +  code.getMsg();
				}
			}catch(Exception e){
				dbLogger.warn("", e);
				retry = true;
			}
			Long time2 = System.currentTimeMillis();

			if(retry && time1 - time2 < DateUtil.m_minute) {//ÔÙÊÔÒ»´Î
				dbLogger.warn(msg);
				code = fixRemoteOrder(odi, order, order.getMobile(), seatList, itemList);
				if(!code.isSuccess()) {
					return code;
				}
			}
			if(odi.isOpenseat()){
				addLockSeatToQryItemResponse(order.getAreaid(), seatList);
			}
			return ErrorCode.SUCCESS;
		}
		return ErrorCode.SUCCESS;
	}
	
	private ErrorCode<DramaRemoteOrderVo> fixRemoteOrder(OpenDramaItem odi, DramaOrder order, String mobile, List<SellDramaSeat> seatList, List<BuyItem> itemList){
		if(!odi.hasSeller(OdiConstant.PARTNER_GEWA)){
			String greetings = JsonUtils.getJsonValueByKey(order.getOtherinfo(), OrderConstant.OTHERKEY_GREETINGS);
			if(odi.isOpenseat()){
				if(CollectionUtils.isEmpty(seatList)) return ErrorCode.getFailure(ApiConstant.CODE_SIGN_ERROR, "×ùÎ»´íÎó£¡");
				TheatreSeatArea seatArea = daoService.getObject(TheatreSeatArea.class, order.getAreaid());
				List<String> seatStrList  = getSeatId(seatArea, seatList);
				return remoteDramaService.newFixOrder(odi.getSellerseq(), order.getId(), mobile, seatArea.getSellerseq(), OdiConstant.OPEN_TYPE_SEAT, StringUtils.join(seatStrList, ","), greetings);
			}else{
				Set<Long> priceIdList = new HashSet<Long>();
				for (BuyItem item : itemList) {
					if(StringUtils.equals(item.getSmallitemtype(), BuyItemConstant.SMALL_ITEMTYPE_PRICE)){
						priceIdList.add(item.getSmallitemid());
					}
				}
				Map<Long, TheatreSeatPrice> seatPriceMap = daoService.getObjectMap(TheatreSeatPrice.class, priceIdList);
				Set<String> areaSeqnoSet = new HashSet<String>();
				ErrorCode<List<String>> code = getPriceList(seatPriceMap, itemList, areaSeqnoSet);
				if(!code.isSuccess()) return ErrorCode.getFailure(code.getErrcode(), code.getMsg());
				List<String> seatStrList = code.getRetval();
				return remoteDramaService.newFixOrder(odi.getSellerseq(), order.getId(), mobile, StringUtils.join(areaSeqnoSet, ","), OdiConstant.OPEN_TYPE_PRICE, StringUtils.join(seatStrList, ","), greetings);
			}
		}
		return ErrorCode.SUCCESS;
	}
	
	@Override
	public ErrorCode backDramaRemoteOrder(Long userid, DramaOrder order, OpenDramaItem odi){
		if(odi.hasSeller(OdiConstant.PARTNER_GEWA)) return ErrorCode.SUCCESS;
		ErrorCode<DramaRemoteOrderVo> code = remoteDramaService.backOrder(order.getId(), "¶©µ¥ÍËÆ±-³¡¹ÝÍËÆ±");
		if(!code.isSuccess()) return ErrorCode.getFailure(code.getErrcode(), code.getMsg());
		DramaRemoteOrderVo remoteOrder = code.getRetval();
		if(!remoteOrder.hasStatus(OrderConstant.REMOTE_STATUS_CANCEL)) return ErrorCode.getFailure(ApiConstant.CODE_SIGN_ERROR, "ÍËÆ±Ê§°Ü:" + remoteOrder.getStatus());
		orderMonitorService.addOrderChangeLog(order.getTradeNo(), "¶©µ¥ÍËÆ±-³¡¹ÝÍËÆ±", JsonUtils.writeObjectToJson(remoteOrder), userid);
		return ErrorCode.SUCCESS;
	}
	
	@Override
	public void addLockSeatToQryItemResponse(Long areaid, List<SellDramaSeat> seatList) {
		QryItemResponse res = daoService.getObject(QryItemResponse.class, "lock" + areaid);
		List<String> seatStrList = new ArrayList<String>();
		if(res == null){
			res = new QryItemResponse("lock" + areaid);
		}else if(StringUtils.isNotBlank(res.getResponse())){
			seatStrList = new ArrayList<String>(Arrays.asList(StringUtils.split(res.getResponse(),"@")));
		}
		Set<String> seatStrSet = new HashSet<String>(seatStrList);
		for(SellDramaSeat seat: seatList){
			seatStrSet.add(seat.getSeatline() + ":" + seat.getSeatrank());
		}
		res.setResponse(StringUtils.join(seatStrSet, "@"));
		daoService.saveObject(res); 
	}
	
	@Override
	public ErrorCode releasePaidFailureOrderSeat(DramaOrder order, List<SellDramaSeat> seatList) {
		try {
			OpenDramaItem odi = daoService.getObjectByUkey(OpenDramaItem.class, "dpid", order.getDpid(), true);
			if(!odi.hasGewara()){
				ErrorCode<TicketRemoteOrder> code = unlockRemoteSeat(order.getId());
				if(!code.isSuccess()) throw new OrderException(code.getErrcode(), code.getMsg());
				removeLockSeatFromQryItemResponse(order.getAreaid(), seatList);
			}
			Timestamp validtime = new Timestamp(System.currentTimeMillis() - 1000);
			for(SellDramaSeat oseat: seatList){
				if(oseat.getOrderid().equals(order.getId())){
					oseat.setValidtime(validtime);
					daoService.saveObject(oseat);
				}
			}
			return ErrorCode.SUCCESS;
		} catch (OrderException e) {
			dbLogger.error("¶©µ¥´íÎó£º" + StringUtil.getExceptionTrace(e));
			return ErrorCode.getFailure(e.getCode(), e.getMsg());
		}
	}
	
	@Override
	public void removeLockSeatFromQryItemResponse(Long areaid, List<SellDramaSeat> seatList) {
		QryItemResponse res = daoService.getObject(QryItemResponse.class, "lock" + areaid);
		List<String> seatStrList = new ArrayList<String>();
		if(res == null){
			res = new QryItemResponse("lock" + areaid);
		}else if(StringUtils.isNotBlank(res.getResponse())){
			seatStrList = new ArrayList<String>(Arrays.asList(StringUtils.split(res.getResponse(),"@")));
		}
		for(SellDramaSeat seat: seatList){
			seatStrList.remove(seat.getSeatline() + ":" + seat.getSeatrank());
		}
		res.setResponse(StringUtils.join(seatStrList, "@"));
		daoService.saveObject(res);
	}
	
	@Override
	public ErrorCode<List<String>> updateRemoteLockSeat(TheatreSeatArea seatArea, int expireSeconds, boolean refresh) {
		if(!seatArea.hasSeller(OdiConstant.PARTNER_GEWA)){
			QryItemResponse res = daoService.getObject(QryItemResponse.class, "lock" + seatArea.getId());
			List<String> result = new ArrayList<String>(0);
			if(res!=null && !refresh && !res.isExpired(expireSeconds)){
				if(Config.isDebugEnabled()){
					dbLogger.warn("LockSeatFromCache:" + seatArea.getId());
				}
				int count = hitCount.incrementAndGet();
				if(count >= 200){//Í³¼ÆÃüÖÐÂÊ
					hitCount.set(0);
					Map<String, String> entry = new HashMap<String, String>();
					entry.put("start", "" + starttime);
					starttime = System.currentTimeMillis();
					entry.put("endtime", "" + starttime);
					entry.put("hittype", "qryGptbs");
					monitorService.addSysLog(SysLogType.hitCache, entry);
				}
				if(StringUtils.isBlank(res.getResponse())) return ErrorCode.getSuccessReturn(result);
				return ErrorCode.getSuccessReturn(Arrays.asList(StringUtils.split(res.getResponse(), "@")));	
			}
			ErrorCode<String> code = remoteDramaService.getRemoteLockSeat(seatArea.getSellerseq());
			if(!code.isSuccess()) {
				return ErrorCode.getFailure(code.getErrcode(), code.getMsg());
			}
			if(res==null) res = new QryItemResponse("lock" + seatArea.getId());
			res.setResponse(code.getRetval());
			res.setUpdatetime(new Timestamp(System.currentTimeMillis()));
			daoService.saveObject(res);
			if(StringUtils.isBlank(code.getRetval())){
				return ErrorCode.getSuccessReturn(result);
			}
			return ErrorCode.getSuccessReturn(Arrays.asList(StringUtils.split(code.getRetval(), "@")));	
		}else{
			List<String> result = new ArrayList<String>(0);
			return ErrorCode.getSuccessReturn(result);
		}
	}
	
	@Override
	public ErrorCode<List<String>> updateRemoteLock(OpenDramaItem odi, TheatreSeatArea seatArea, int expireSeconds, boolean refresh){
		if(odi.isOpenseat()){
			return updateRemoteLockSeat(seatArea, expireSeconds, refresh);
		}else{
			return updateRemoteLockPrice(seatArea, expireSeconds, refresh);
		}
	}
	
	@Override
	public ErrorCode<List<String>> updateRemoteLockPrice(TheatreSeatArea seatArea, int expireSeconds, boolean refresh){
		if(!seatArea.hasSeller(OdiConstant.PARTNER_GEWA)){
			QryItemResponse res = daoService.getObject(QryItemResponse.class, "lock" + seatArea.getId());
			List<String> result = new ArrayList<String>(0);
			if(res!=null && !refresh && !res.isExpired(expireSeconds)){
				if(Config.isDebugEnabled()){
					dbLogger.warn("LockSeatFromCache:" + seatArea.getId());
				}
				int count = hitCount.incrementAndGet();
				if(count >= 200){//Í³¼ÆÃüÖÐÂÊ
					hitCount.set(0);
					Map<String, String> entry = new HashMap<String, String>();
					entry.put("start", "" + starttime);
					starttime = System.currentTimeMillis();
					entry.put("endtime", "" + starttime);
					entry.put("hittype", "qryGptbs");
					monitorService.addSysLog(SysLogType.hitCache, entry);
				}
				if(StringUtils.isBlank(res.getResponse())) return ErrorCode.getSuccessReturn(result);
				return ErrorCode.getSuccessReturn(Arrays.asList(StringUtils.split(res.getResponse(), "@")));	
			}
			ErrorCode<String> code = remoteDramaService.getRemoteLockPrice(seatArea.getSellerseq());
			if(!code.isSuccess()) {
				return ErrorCode.getFailure(code.getErrcode(), code.getMsg());
			}
			if(res==null) res = new QryItemResponse("lock" + seatArea.getId());
			res.setResponse(code.getRetval());
			res.setUpdatetime(new Timestamp(System.currentTimeMillis()));
			daoService.saveObject(res);
			if(StringUtils.isBlank(code.getRetval())){
				return ErrorCode.getSuccessReturn(result);
			}
			return ErrorCode.getSuccessReturn(Arrays.asList(StringUtils.split(code.getRetval(), "@")));	
		}else{
			List<String> result = new ArrayList<String>(0);
			return ErrorCode.getSuccessReturn(result);
		}
	}
	
	private ErrorCode<List<OrderItemVo>> getOrderItemVo(String opentype, TheatreSeatArea seatArea, List<BuyItem> itemList, List<SellDramaSeat> seatList, Set<String> areaSeqnoSet){
		List<OrderItemVo> orderItemList = new ArrayList<OrderItemVo>();
		Set<Long> priceIdList = new HashSet<Long>();
		Map<Long, OpenDramaItem> odiMap = new HashMap<Long, OpenDramaItem>();
		for (BuyItem item : itemList) {
			if(StringUtils.equals(item.getTag(), BuyItemConstant.TAG_DRAMAPLAYITEM) && 
				StringUtils.equals(item.getSmallitemtype(), BuyItemConstant.SMALL_ITEMTYPE_PRICE)){
				priceIdList.add(item.getSmallitemid());
				OpenDramaItem odi = odiMap.get(item.getRelatedid());
				if(odi == null){
					odi = daoService.getObjectByUkey(OpenDramaItem.class, "dpid", item.getRelatedid(), true);
					if(odi != null){
						odiMap.put(item.getRelatedid(), odi);
					}
				}
			}
		}
		List<TheatreSeatPrice> seatPriceList = daoService.getObjectList(TheatreSeatPrice.class, priceIdList);
		Map<Long, TheatreSeatPrice> seatPriceMap = BeanUtil.beanListToMap(seatPriceList, "id");
		Map<Long, BuyItem> itemMap = BeanUtil.beanListToMap(itemList, "disid");
		if(StringUtils.equals(opentype, OdiConstant.OPEN_TYPE_SEAT)){
			if(!MapUtils.isEmpty(itemMap)){
				Map<String,SellDramaSeat> sellSeatMap = BeanUtil.beanListToMap(seatList, "key");
				for (Long discountid : itemMap.keySet()) {
					DisQuantity discount = daoService.getObject(DisQuantity.class, discountid);
					if(discount == null || StringUtils.isBlank(discount.getSispseq())) continue;
					BuyItem buyItem = itemMap.get(discountid);
					if(buyItem == null){
						return ErrorCode.getFailure(ApiConstant.CODE_SIGN_ERROR, "¹ºÂò³ö´í£¡");
					}
					TheatreSeatPrice tmpPrice = seatPriceMap.get(discount.getTspid());
					if(tmpPrice == null) {
						return ErrorCode.getFailure(ApiConstant.CODE_SIGN_ERROR, "¹ºÂò³ö´í£¡");
					}
					OpenDramaItem odi = odiMap.get(buyItem.getRelatedid());
					if(odi == null){
						return ErrorCode.getFailure(ApiConstant.CODE_SIGN_ERROR, "¹ºÂò³ö´í£¡");
					}
					String seatLabel = JsonUtils.getJsonValueByKey(buyItem.getOtherinfo(), BuyItemConstant.OTHERINFO_KEY_SEATLABEL);
					if(StringUtils.isBlank(seatLabel)){
						return ErrorCode.getFailure(ApiConstant.CODE_SIGN_ERROR, "¹ºÂò³ö´í£¡");
					}
					List<String> seatKeyList = Arrays.asList(StringUtils.split(seatLabel, ","));
					List<SellDramaSeat> sellSeatList = getSellDramaSeat(sellSeatMap, seatKeyList);
					seatList.removeAll(sellSeatList);
					List<String> seatStrList = getSeatId(seatArea, sellSeatList);
					OrderItemVo orderItem = new OrderItemVo(OdiConstant.OPEN_TYPE_SEAT);
					orderItem.setSiseqno(odi.getSellerseq());
					orderItem.setSeatIds(StringUtils.join(seatStrList, ","));
					Map<String, Integer> priceMap = new HashMap<String, Integer>();
					priceMap.put(tmpPrice.getSispseq(), tmpPrice.getPrice());
					orderItem.setPriceMap(priceMap);
					orderItem.setPackTicketId(discount.getSispseq());
					orderItem.setDiscountTotal(buyItem.getDisfee());
					orderItemList.add(orderItem);
				}
			}
			if(!CollectionUtils.isEmpty(seatList)){
				OpenDramaItem odi = odiMap.get(seatArea.getDpid());
				if(odi == null){
					return ErrorCode.getFailure(ApiConstant.CODE_SIGN_ERROR, "¹ºÂò³ö´í£¡");
				}
				Map<String, Integer> priceMap = BeanUtil.getKeyValuePairMap(seatPriceList, "sispseq", "price");
				OrderItemVo orderItem = new OrderItemVo(OdiConstant.OPEN_TYPE_SEAT);
				orderItem.setSiseqno(odi.getSellerseq());
				List<String> seatStrList = getSeatId(seatArea, seatList);
				orderItem.setSeatIds(StringUtils.join(seatStrList, ","));
				orderItem.setPriceMap(priceMap);
				orderItemList.add(orderItem);
			}
		}else{
			List<BuyItem> tmpList = new ArrayList<BuyItem>(itemList);
			if(!MapUtils.isEmpty(itemMap)){
				tmpList.removeAll(itemMap.values());
				for (Long discountid : itemMap.keySet()) {
					DisQuantity discount = daoService.getObject(DisQuantity.class, discountid);
					if(discount == null || StringUtils.isBlank(discount.getSispseq())) continue;
					BuyItem buyItem = itemMap.get(discountid);
					if(buyItem == null){
						return ErrorCode.getFailure(ApiConstant.CODE_SIGN_ERROR, "¹ºÂò³ö´í£¡");
					}
					OpenDramaItem odi = odiMap.get(buyItem.getRelatedid());
					if(odi == null){
						return ErrorCode.getFailure(ApiConstant.CODE_SIGN_ERROR, "¹ºÂò³ö´í£¡");
					}
					List<BuyItem> tmpItemList = new ArrayList();
					tmpItemList.add(buyItem);
					TheatreSeatPrice tmpPrice = seatPriceMap.get(discount.getTspid());
					if(tmpPrice == null) {
						return ErrorCode.getFailure(ApiConstant.CODE_SIGN_ERROR, "¹ºÂò³ö´í£¡");
					}
					ErrorCode<List<String>> code = getPriceList(seatPriceMap, tmpItemList, areaSeqnoSet);
					if(!code.isSuccess()) return ErrorCode.getFailure(code.getErrcode(), code.getMsg());
					List<String> seatStrList = code.getRetval();
					OrderItemVo orderItem = new OrderItemVo(OdiConstant.OPEN_TYPE_PRICE);
					orderItem.setSiseqno(odi.getSellerseq());
					orderItem.setTicketRange(StringUtils.join(seatStrList, ","));
					Map<String, Integer> priceMap = new HashMap<String, Integer>();
					priceMap.put(tmpPrice.getSispseq(), tmpPrice.getPrice());
					orderItem.setPriceMap(priceMap);
					orderItem.setDiscountTotal(buyItem.getDisfee());
					orderItem.setPackTicketId(discount.getSispseq());
					orderItemList.add(orderItem);
				}
			}
			if(!CollectionUtils.isEmpty(tmpList)){
				Map<Long,List<BuyItem>> buyItemMap = BeanUtil.groupBeanList(tmpList, "relatedid");
				Map<Long,List<TheatreSeatPrice>> odiPriceMap = BeanUtil.groupBeanList(seatPriceList, "dpid");
				for (Long relatedid : buyItemMap.keySet()) {
					OpenDramaItem odi = odiMap.get(relatedid);
					if(odi == null){
						return ErrorCode.getFailure(ApiConstant.CODE_SIGN_ERROR, "¹ºÂò³ö´í£¡");
					}
					OrderItemVo orderItem = new OrderItemVo(OdiConstant.OPEN_TYPE_PRICE);
					orderItem.setSiseqno(odi.getSellerseq());
					List<BuyItem> tmpItemList = buyItemMap.get(relatedid);
					if(CollectionUtils.isEmpty(tmpItemList)){
						return ErrorCode.getFailure(ApiConstant.CODE_SIGN_ERROR, "¹ºÂò³ö´í£¡");
					}
					ErrorCode<List<String>> code = getPriceList(seatPriceMap, tmpItemList, areaSeqnoSet);
					if(!code.isSuccess()) return ErrorCode.getFailure(code.getErrcode(), code.getMsg());
					List<String> seatStrList = code.getRetval();
					orderItem.setTicketRange(StringUtils.join(seatStrList, ","));
					List<TheatreSeatPrice> tmpPriceList = odiPriceMap.get(relatedid);
					if(CollectionUtils.isEmpty(tmpPriceList)){
						return ErrorCode.getFailure(ApiConstant.CODE_SIGN_ERROR, "¹ºÂò³ö´í£¡");
					}
					Map<String, Integer> priceMap = BeanUtil.getKeyValuePairMap(tmpPriceList, "sispseq", "price");
					orderItem.setPriceMap(priceMap);
					orderItemList.add(orderItem);
				}
			}
		}
		return ErrorCode.getSuccessReturn(orderItemList);
	}
	
	private List<SellDramaSeat> getSellDramaSeat(Map<String, SellDramaSeat> sellSeatMap, List<String> seatKeyList){
		List<SellDramaSeat> sellSeatList = new ArrayList<SellDramaSeat>();
		for (String key : seatKeyList) {
			SellDramaSeat sellSeat = sellSeatMap.get(key);
			if(sellSeat != null && !sellSeatList.contains(sellSeat)){
				sellSeatList.add(sellSeat);
			}
		}
		return sellSeatList;
	}
	
	private List<String> getSeatId(TheatreSeatArea seatArea, List<SellDramaSeat> seatList){
		List<String> seatStrList = new ArrayList<String>();
		for(SellDramaSeat seat: seatList){
			seatStrList.add(seatArea.getSellerseq() + ":" + seat.getSeatline() + ":" + seat.getSeatrank());
		}
		return seatStrList;
	}
}
