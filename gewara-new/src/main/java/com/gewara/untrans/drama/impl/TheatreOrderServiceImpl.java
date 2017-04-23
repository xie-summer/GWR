package com.gewara.untrans.drama.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.ui.ModelMap;

import com.gewara.command.TheatrePriceCommand;
import com.gewara.constant.ApiConstant;
import com.gewara.constant.GoodsConstant;
import com.gewara.constant.OdiConstant;
import com.gewara.constant.PaymethodConstant;
import com.gewara.constant.Status;
import com.gewara.constant.order.BuyItemConstant;
import com.gewara.constant.sys.ErrorCodeConstant;
import com.gewara.constant.ticket.OpiConstant;
import com.gewara.constant.ticket.OrderConstant;
import com.gewara.constant.ticket.PartnerConstant;
import com.gewara.helper.GoodsPriceHelper;
import com.gewara.helper.TspHelper;
import com.gewara.helper.discount.DramaSpecialDiscountHelper;
import com.gewara.helper.order.GewaOrderHelper;
import com.gewara.helper.order.OrderOther;
import com.gewara.model.acl.User;
import com.gewara.model.api.ApiUser;
import com.gewara.model.drama.DisQuantity;
import com.gewara.model.drama.DramaOrder;
import com.gewara.model.drama.OpenDramaItem;
import com.gewara.model.drama.OpenTheatreSeat;
import com.gewara.model.drama.SellDramaSeat;
import com.gewara.model.drama.TheatreProfile;
import com.gewara.model.drama.TheatreSeatArea;
import com.gewara.model.drama.TheatreSeatPrice;
import com.gewara.model.pay.BuyItem;
import com.gewara.model.pay.SpecialDiscount;
import com.gewara.model.user.Member;
import com.gewara.pay.PayValidHelper;
import com.gewara.service.OrderException;
import com.gewara.service.drama.DramaOrderService;
import com.gewara.service.member.MemberService;
import com.gewara.support.ErrorCode;
import com.gewara.untrans.drama.TheatreOperationService;
import com.gewara.untrans.drama.TheatreOrderService;
import com.gewara.untrans.impl.AbstractUntrantsService;
import com.gewara.untrans.monitor.MonitorService;
import com.gewara.util.BeanUtil;
import com.gewara.util.DateUtil;
import com.gewara.util.JsonUtils;
import com.gewara.util.PKCoderUtil;
import com.gewara.util.StringUtil;
import com.gewara.util.ValidateUtil;
import com.gewara.util.VmUtils;

@Service("theatreOrderService")
public class TheatreOrderServiceImpl extends AbstractUntrantsService implements TheatreOrderService {
	
	@Autowired@Qualifier("dramaOrderService")
	private DramaOrderService dramaOrderService;
	
	@Autowired@Qualifier("theatreOperationService")
	private TheatreOperationService theatreOperationService;
	
	@Autowired@Qualifier("monitorService")
	protected MonitorService monitorService;
	
	@Autowired@Qualifier("memberService")
	protected MemberService memberService;
	
	@Override
	public ErrorCode<DramaOrder> addDramaOrder(OpenDramaItem odi, Member member, String mobile, Integer quantity, Long disid, Long priceid, String spkey){
		return addDramaOrder(odi, member, mobile, quantity, disid, priceid, null, null, spkey);
	}
	@Override
	public ErrorCode<DramaOrder> addDramaOrder(OpenDramaItem odi, Member member, String mobile, Integer quantity, Long disid, Long priceid, ApiUser partner, String ukey, String spkey){
		//检查该用户有没有未付款的订单，如果有，将所有未付款的订单取消
		try{
			Long memberid = null;
			if(member != null){
				memberid = member.getId();
			}else{
				memberid = partner.getId();
			}
			if(StringUtils.isBlank(ukey)) ukey = String.valueOf(memberid);
			ErrorCode lastOrder = processLastOrder(memberid, ukey);
			if(!lastOrder.isSuccess()){
				return ErrorCode.getFailure(lastOrder.getMsg());
			}
		}catch(Exception e){
			dbLogger.warn(StringUtil.getExceptionTrace(e, 5));
		}
		ErrorCode<DramaOrder> code = addDramaOrder(odi, member, mobile, quantity, disid, priceid, partner, ukey);
		if(!code.isSuccess()) return code;
		DramaOrder order = code.getRetval();
		return lockRemotePrice(order, odi, spkey);
	}

	private ErrorCode<DramaOrder> addDramaOrder(OpenDramaItem odi, Member member, String mobile, Integer quantity, Long disid, Long priceid, ApiUser partner, String ukey){
		for(int i=0; i < 3;i++){
			try{
				return dramaOrderService.addDramaOrder(odi, member, mobile, quantity, disid, priceid, partner, ukey);
			}catch(Throwable e){
				//非乐观锁异常
				if(!isUpdateErrorException(e)){
					dbLogger.warn(StringUtil.getExceptionTrace(e, 10));					
					return ErrorCode.getFailure("购买出错！");
				}else{
					dbLogger.warn("addDramaOrder:" + odi.getId() + ":" + i + "," + StringUtil.getExceptionTrace(e, 3));
				}
			}
		}
		return ErrorCode.getFailure("购买出错！");
	}
	
	@Override
	public ErrorCode<String> payDramaOrder(DramaOrder order, OpenDramaItem odi) {
		ErrorCode code = theatreOperationService.setAndFixRemoteOrder(odi, order);
		if(!code.isSuccess()) return ErrorCode.getFailure(code.getErrcode(), code.getMsg());
		if(odi.isOpenseat()){
			List<SellDramaSeat> seatList = dramaOrderService.getDramaOrderSeatList(order.getId());
			theatreOperationService.addLockSeatToQryItemResponse(order.getAreaid(), seatList);
		}
		for(int i=0; i < 5; i++){
			try{
				return dramaOrderService.processDramaOrder(order, odi);
			}catch(Throwable e){
				//非乐观锁异常
				if(!isUpdateErrorException(e)){
					dbLogger.warn("payDramaOrder:" + odi.getId(), e);
				}else{
					dbLogger.warn("payDramaOrder:" + odi.getId() + ":" + i + "," + StringUtil.getExceptionTrace(e, 3));
				}
			}
		}
		return ErrorCode.getFailure("处理订单出错！");
	}
	@Override
	public ErrorCode<DramaOrder> addDramaOrder(String pricelist, Member member, String mobile, String spkey) {
		return addDramaOrder(pricelist, member, mobile, null, null, spkey);
	}
	@Override
	public ErrorCode<DramaOrder> addDramaOrder(String pricelist, Member member, String mobile, ApiUser partner, String ukey) {
		return addDramaOrder(pricelist, member, mobile, partner, ukey, null);
	}
	
	@Override
	public ErrorCode<DramaOrder> addDramaOrder(String pricelist, Member member, String mobile, ApiUser partner, String ukey, String spkey){
		if(!GoodsPriceHelper.isValidData(pricelist)) return ErrorCode.getFailure(ApiConstant.CODE_SIGN_ERROR, "场次或价格错误！");
		if(!ValidateUtil.isMobile(mobile)) return ErrorCode.getFailure(ApiConstant.CODE_SIGN_ERROR, "手机号码格式错误！");
		List<TheatrePriceCommand> commandList = new ArrayList<TheatrePriceCommand>();
		try{
			commandList = JsonUtils.readJsonToObjectList(TheatrePriceCommand.class, pricelist);
		}catch (Exception e) {
			return ErrorCode.getFailure(ApiConstant.CODE_SIGN_ERROR, "场次或价格错误！");
		}
		//检查该用户有没有未付款的订单，如果有，将所有未付款的订单取消
		try{
			Long memberid = null;
			if(member != null){
				memberid = member.getId();
			}else{
				memberid = partner.getId();
			}
			if(StringUtils.isBlank(ukey)) ukey = String.valueOf(memberid);
			ErrorCode lastOrder = processLastOrder(memberid, ukey);
			if(!lastOrder.isSuccess()){
				return ErrorCode.getFailure(lastOrder.getMsg());
			}
		}catch(Exception e){
			dbLogger.warn(StringUtil.getExceptionTrace(e, 5));
		}
		ErrorCode<DramaOrder> code = addDramaOrder(commandList, member, mobile, partner, ukey);
		if(!code.isSuccess()) return code;
		DramaOrder order = code.getRetval();
		OpenDramaItem odi = daoService.getObjectByUkey(OpenDramaItem.class, "dpid", order.getDpid());
		return lockRemotePrice(order, odi, spkey);
	}

	private ErrorCode<DramaOrder> addDramaOrder(List<TheatrePriceCommand> commandList, Member member, String mobile, ApiUser partner, String ukey){
		for(int i=0; i < 3;i++){
			try{
				return dramaOrderService.addDramaOrder(commandList, member, mobile, partner, ukey);
			}catch(Throwable e){
				//非乐观锁异常
				if(!isUpdateErrorException(e)){
					dbLogger.warn(StringUtil.getExceptionTrace(e, 10));
					return ErrorCode.getFailure("购买出错！");
				}else{
					dbLogger.warn("addDramaOrder:" + JsonUtils.writeObjectToJson(commandList) + ":" + i + "," + StringUtil.getExceptionTrace(e, 3));
				}
			}
		}
		return ErrorCode.getFailure("购买出错！");
	}
	
	@Override
	public ErrorCode<Map> getTheatreSeatPriceInfo(List<TheatrePriceCommand> commandList){
		Map jsonMap = new HashMap();
		Map<Long, Integer> quantityMap = new HashMap<Long, Integer>();
		Map<Long/*tspid*/, Integer> priceQuantityMap = new HashMap<Long, Integer>();
		Map<Long/*tspid*/, Integer> disQuantityMap = new HashMap<Long, Integer>();
		String paymethod = null, seller = null;
		Long theatreid = null, dramaid = null;
		Map<Long, DisQuantity> disMap = new HashMap<Long, DisQuantity>();
		Map<Long, TheatreSeatPrice> priceMap = new HashMap<Long, TheatreSeatPrice>();
		Map<Long, TheatreSeatArea> areaMap = new HashMap<Long, TheatreSeatArea>();
		Map<Long, OpenDramaItem> odiMap = new HashMap<Long, OpenDramaItem>();
		OpenDramaItem baseOdi = null;
		for (TheatrePriceCommand command : commandList) {
			if(command.getItemid() == null || command.getTspid() == null 
					|| command.getQuantity() == null || command.getQuantity()<1 
					|| !GoodsConstant.CHECK_GOODSLIST.contains(command.getTag())){
				return ErrorCode.getFailure(ApiConstant.CODE_SIGN_ERROR, "场次或价格错误！");
			}
			OpenDramaItem odi = null;
			TheatreSeatPrice seatPrice = null;
			DisQuantity discount = null;
			TheatreSeatArea seatArea = null;
			int quantity = 0;
			odi = daoService.getObjectByUkey(OpenDramaItem.class, "dpid", command.getItemid());
			if(odi == null) return ErrorCode.getFailure(ApiConstant.CODE_SIGN_ERROR, "场次不存在或被删除！");
			String odiname = StringUtils.isBlank(odi.getName())? 
					DateUtil.formatDate(odi.getPlaytime()) + " " + DateUtil.getCnWeek(odi.getPlaytime()) + " " + DateUtil.format(odi.getPlaytime(), "HH:mm")
					: (StringUtils.equals(odi.getPeriod(), Status.Y)?
							"[" + odi.getName() +"]" + DateUtil.formatDate(odi.getPlaytime()) + " " + DateUtil.getCnWeek(odi.getPlaytime()) + " " + DateUtil.format(odi.getPlaytime(), "HH:mm")
							: odi.getName());
			if(!odi.isOpenprice()) return ErrorCode.getFailure(ApiConstant.CODE_SIGN_ERROR, odiname + "非价格场次！"); 
			if(!odi.isBooking()) return ErrorCode.getFailure(ApiConstant.CODE_SIGN_ERROR, odiname + "场次已关闭购票！");
			if(odi.hasUnOpenToGewa()) return ErrorCode.getFailure(ApiConstant.CODE_SIGN_ERROR, odiname +"场次已停止售票！");
			if(StringUtils.isBlank(seller)){
				seller = odi.getSeller();
			}else{
				if(!StringUtils.equals(seller, odi.getSeller())){
					return ErrorCode.getFailure(ApiConstant.CODE_SIGN_ERROR, "不支持多种销售渠道场次购票！");
				}
			}
			baseOdi = odi;
			if(theatreid == null){
				theatreid = odi.getTheatreid();
			}else if(!theatreid.equals(odi.getTheatreid())){
				return ErrorCode.getFailure(ApiConstant.CODE_SIGN_ERROR, "不能跨场馆购票！");
			}
			if(dramaid == null){
				dramaid = odi.getDramaid();
			}else if(!dramaid.equals(odi.getDramaid())){
				return ErrorCode.getFailure(ApiConstant.CODE_SIGN_ERROR, "不能跨项目购票！");
			}
			String tmpMethod = JsonUtils.getJsonValueByKey(odi.getOtherinfo(), "defaultpaymethod");
			if(StringUtils.isNotBlank(tmpMethod)){
				if(StringUtils.isBlank(paymethod)){
					paymethod = tmpMethod;
				}else if(!StringUtils.equals(paymethod, tmpMethod)){
					return ErrorCode.getFailure(ApiConstant.CODE_SIGN_ERROR, "支付方式限制！");
				}
			}
			if(StringUtils.equals(command.getTag(), GoodsConstant.CHECK_GOODS_PRICE)){
				seatPrice = daoService.getObject(TheatreSeatPrice.class, command.getTspid());
				if(seatPrice == null) return ErrorCode.getFailure(ApiConstant.CODE_SIGN_ERROR, "参数错误，请重选！");
				if(!seatPrice.hasAllownum()) return ErrorCode.getFailure(ApiConstant.CODE_SIGN_ERROR, odiname +",价格："+ seatPrice.getPrice() +"，库存不足！");
				if(!seatPrice.hasRetail()) return ErrorCode.getFailure(ApiConstant.CODE_SIGN_ERROR, odiname +",价格："+ seatPrice.getPrice() +",不支持零售！");
				Integer priceQuantity = priceQuantityMap.get(seatPrice.getId());
				quantity = command.getQuantity();
				if(priceQuantity == null){
					priceQuantity = quantity;
				}else{
					priceQuantity += quantity;
				}
				if(seatPrice.getMaxbuy()< priceQuantity){
					return ErrorCode.getFailure(ApiConstant.CODE_SIGN_ERROR, "”" + odiname +"“ ”"+ seatPrice.getPrice() +"元 “  每单最多购买"+ seatPrice.getMaxbuy() + "张");
				}
				String msg = TspHelper.getTheatrePriceDisabledReason(seatPrice, priceQuantity);
				if(StringUtils.isNotBlank(msg)) return ErrorCode.getFailure(ApiConstant.CODE_SIGN_ERROR, msg);
				priceQuantityMap.put(seatPrice.getId(), priceQuantity);
			}else if(StringUtils.equals(command.getTag(), GoodsConstant.CHECK_GOODS_DISCOUNT)){
				discount = daoService.getObject(DisQuantity.class, command.getTspid());
				if(discount == null) return ErrorCode.getFailure(ApiConstant.CODE_SIGN_ERROR, "参数错误，请重选！");
				if(!discount.hasBooking()){
					return ErrorCode.getFailure(ApiConstant.CODE_SIGN_ERROR, "套票未开放预订！");
				}
				seatPrice = daoService.getObject(TheatreSeatPrice.class, discount.getTspid());
				if(seatPrice == null) return ErrorCode.getFailure(ApiConstant.CODE_SIGN_ERROR, "购票价格错误！");
				Integer disQuantity = disQuantityMap.get(discount.getId());
				if(disQuantity == null){
					disQuantity = command.getQuantity();
				}else{
					disQuantity += command.getQuantity();
				}
				if(discount.getMaxbuy()< disQuantity){
					return ErrorCode.getFailure(ApiConstant.CODE_SIGN_ERROR, "”" + odiname +"“ ”"+ discount.getPrice() +"元 “  每单最多购买"+discount.getMaxbuy() + "张");
				}
				quantity = command.getQuantity() * discount.getQuantity();
				disMap.put(discount.getId(), discount);
				Integer priceQuantity = priceQuantityMap.get(seatPrice.getId());
				if(priceQuantity == null){
					priceQuantity = quantity;
				}else{
					priceQuantity += quantity;
				}
				String msg = TspHelper.getTheatrePriceDisabledReason(seatPrice, priceQuantity);
				if(StringUtils.isNotBlank(msg)) return ErrorCode.getFailure(ApiConstant.CODE_SIGN_ERROR, msg);
				priceQuantityMap.put(seatPrice.getId(), priceQuantity);
			}
			if(seatPrice == null) return ErrorCode.getFailure(ApiConstant.CODE_SIGN_ERROR, "参数错误，请重选！");
			if(!seatPrice.hasBooking()) return ErrorCode.getFailure(ApiConstant.CODE_SIGN_ERROR, "购买失败，价格已关闭购票！");
			seatArea = areaMap.get(seatPrice.getAreaid());
			if(seatArea == null){
				seatArea = daoService.getObject(TheatreSeatArea.class, seatPrice.getAreaid());
				areaMap.put(seatPrice.getAreaid(), seatArea);
			}
			if(seatArea == null || !seatArea.hasStatus(Status.Y)) return ErrorCode.getFailure(ApiConstant.CODE_SIGN_ERROR, "场区不存在或已关闭卖票！");
			Integer odiQuantity = quantityMap.get(odi.getId());
			if(odiQuantity == null){
				odiQuantity = quantity;
			}else{
				odiQuantity += quantity;
			}
			if(odi.getMaxbuy() < odiQuantity) return ErrorCode.getFailure(ApiConstant.CODE_SIGN_ERROR, "”" + odiname +"“ 每单最多购买"+odi.getMaxbuy() + "张");
			quantityMap.put(odi.getId(), odiQuantity);
			priceMap.put(seatPrice.getId(), seatPrice);
			odiMap.put(odi.getDpid(), odi);
		}
		jsonMap.put("priceMap", priceMap);
		jsonMap.put("disMap", disMap);
		jsonMap.put("odiMap", odiMap);
		jsonMap.put("baseOdi", baseOdi);
		jsonMap.put("commandList", commandList);
		return ErrorCode.getSuccessReturn(jsonMap);
	}
	
	@Override
	public ErrorCode<DramaOrder> addDramaOrder(OpenDramaItem odi, TheatreSeatArea seatArea, List<Long> seatidList,Long disid, String mobile, 
			Member member, String spkey){
		try{
			ErrorCode lastOrder = processLastOrder(member.getId(), member.getId().toString());
			if(!lastOrder.isSuccess()){
				return ErrorCode.getFailure(ApiConstant.CODE_SIGN_ERROR, lastOrder.getMsg());
			}
		}catch(Exception e){
			dbLogger.warn(StringUtil.getExceptionTrace(e, 5));
		}
		ErrorCode<DramaOrder> code = addDramaOrder(odi, seatArea, seatidList, disid, mobile, member);
		if(!code.isSuccess()) return code;
		DramaOrder order = code.getRetval();
		return lockRemoteSeat(order, odi, spkey);
	}
	
	private ErrorCode<DramaOrder> addDramaOrder(OpenDramaItem odi, TheatreSeatArea seatArea, List<Long> seatidList,Long disid, String mobile, Member member){
		ErrorCode<List<String>> remoteLockList = theatreOperationService.updateRemoteLockSeat(seatArea, OpiConstant.SECONDS_ADDORDER, false);
		if(!remoteLockList.isSuccess()){
			return ErrorCode.getFailure(ApiConstant.CODE_SIGN_ERROR, "订单失败，场馆网络异常！");
		}
		for(int i=0; i < 3;i++){
			try{
				return dramaOrderService.addDramaOrder(odi, seatArea, seatidList, disid, mobile, member, null, remoteLockList.getRetval());
			}catch (Exception e) {
				//非乐观锁异常
				if(!isUpdateErrorException(e)){
					dbLogger.warn(StringUtil.getExceptionTrace(e, 10));	
					String msg = "购买出错！";
					if(e instanceof OrderException){
						msg = ((OrderException)e).getMsg();
					}
					return ErrorCode.getFailure(msg);
				}else{
					dbLogger.warn("addDramaOrder:" + StringUtils.join(seatidList, ",") + ":" + i + "," + StringUtil.getExceptionTrace(e, 3));
				}
			}
		}
		return ErrorCode.getFailure("购买出错！");
	}
	
	@Override
	public ErrorCode<DramaOrder> addDramaOrder(String pricelist, String mobile, ApiUser partner, User user, String telephone, final boolean isBind, String bindmobile, String checkpass){
		ErrorCode apiUserCode = validateApiUser(partner);
		if(!apiUserCode.isSuccess()){
			return ErrorCode.getFailure(apiUserCode.getMsg());
		}
		ErrorCode bindMemberCode = validateBindMember(telephone, isBind, bindmobile, checkpass);
		if(!bindMemberCode.isSuccess()){
			return ErrorCode.getFailure(bindMemberCode.getMsg());
		}
		final boolean offlineFlag = PartnerConstant.GEWA_DRAMA_ADMIN_OFFLINE.equals(partner.getId());
		ErrorCode<DramaOrder> code = this.addDramaOrder(pricelist, null, mobile, partner, mobile, null);
		if(!code.isSuccess()) return code;
		DramaOrder order = code.getRetval();
		Map<String, String> otherInfoMap = JsonUtils.readJsonToMap(order.getOtherinfo());
		otherInfoMap.put(OrderConstant.OTHERKEY_TELEPHONE, telephone);
		order.setOtherinfo(JsonUtils.writeMapToJson(otherInfoMap));
		if(isBind){
			ErrorCode<Member> memberCode = bindMember(order, telephone, bindmobile, checkpass, user);
			if(!memberCode.isSuccess()){
				return ErrorCode.getFailure(memberCode.getMsg());
			}
			Member member = memberCode.getRetval();
			order.setMemberid(member.getId());
		}
		order.setClerkid(user.getId());
		//后台线下订单
		if(offlineFlag){
			order.setPaymethod(PaymethodConstant.PAYMETHOD_GEWARA_OFFLINEPAY);
		}else{
			order.setPaymethod(PaymethodConstant.PAYMETHOD_PAYECO_DNA);
		}
		daoService.saveObject(order);
		monitorService.saveAddLog(user.getId(), DramaOrder.class, order.getId(), order);
		return ErrorCode.getSuccessReturn(order);
	}
	
	@Override
	public ErrorCode<DramaOrder> addDramaOrder(OpenDramaItem odi, TheatreSeatArea seatArea, List<Long> seatidList, Long disid, String mobile,ApiUser partner, User user, String telephone, final boolean isBind, String bindmobile, String checkpass){
		ErrorCode apiUserCode = validateApiUser(partner);
		if(!apiUserCode.isSuccess()){
			return ErrorCode.getFailure(apiUserCode.getMsg());
		}
		ErrorCode bindMemberCode = validateBindMember(telephone, isBind, bindmobile, checkpass);
		if(!bindMemberCode.isSuccess()){
			return ErrorCode.getFailure(bindMemberCode.getMsg());
		}
		final boolean offlineFlag = PartnerConstant.GEWA_DRAMA_ADMIN_OFFLINE.equals(partner.getId());
		List<OpenTheatreSeat> oSeatList = daoService.getObjectList(OpenTheatreSeat.class, seatidList);
		List<String> seatLableList = BeanUtil.getBeanPropertyList(oSeatList, String.class, "key", true);
		ErrorCode<DramaOrder> code = this.addDramaOrder(odi, seatArea, StringUtils.join(seatLableList, ","), disid, mobile, null, partner, mobile);
		if(!code.isSuccess()){
			return code;
		}
		DramaOrder order = code.getRetval();
		order.setClerkid(user.getId());
		Map<String, String> otherInfoMap = JsonUtils.readJsonToMap(order.getOtherinfo());
		otherInfoMap.put(OrderConstant.OTHERKEY_TELEPHONE, telephone);
		order.setOtherinfo(JsonUtils.writeMapToJson(otherInfoMap));
		if(isBind){
			ErrorCode<Member> memberCode = bindMember(order, telephone, bindmobile, checkpass, user);
			if(!memberCode.isSuccess()){
				return ErrorCode.getFailure(memberCode.getMsg());
			}
			Member member = memberCode.getRetval();
			order.setMemberid(member.getId());
		}
		
		//后台线下订单
		if(offlineFlag){
			order.setPaymethod(PaymethodConstant.PAYMETHOD_GEWARA_OFFLINEPAY);
		}else{
			order.setPaymethod(PaymethodConstant.PAYMETHOD_PAYECO_DNA);
		}
		daoService.saveObject(order);
		monitorService.saveAddLog(user.getId(), DramaOrder.class, order.getId(), order);
		return ErrorCode.getSuccessReturn(order);
	}
	
	private ErrorCode<Member> bindMember(DramaOrder order, String telephone, String bindmobile, String checkpass, User user){
		Member member = memberService.getMemberByMobile(telephone);
		Map<String, String> otherInfoMap = JsonUtils.readJsonToMap(order.getOtherinfo());
		if(StringUtils.isBlank(bindmobile)){
			if(member == null){
				ErrorCode<Member> code = memberService.createWithMobile(telephone, user);
				if(!code.isSuccess()){
					dbLogger.warn("drama_create_bindMember:" + code.getErrcode() + "," + code.getMsg());
					cancelDramaOrder(order, order.getUkey(), "重复订单");
					return ErrorCode.getFailure(code.getMsg());
				}
				member = code.getRetval();
				otherInfoMap.put(OrderConstant.OTHERKEY_CREATEMEMBER, "true");
			}
		}else{
			if(member == null){
				otherInfoMap.put(OrderConstant.OTHERKEY_CREATEMEMBER, "true");
			}
			ErrorCode<Member> memberCode = memberService.createMemberWithBindMobile(bindmobile, checkpass, null, null);
			if(!memberCode.isSuccess()){
				dbLogger.warn("drama_create_code_bindMember:" + memberCode.getErrcode() + "," + memberCode.getMsg());
				cancelDramaOrder(order, order.getUkey(), "重复订单");
				return ErrorCode.getFailure(memberCode.getMsg());
			}
			member = memberCode.getRetval();
		}
		otherInfoMap.put(OrderConstant.OTHERKEY_BINDMEMBER, String.valueOf(member.getId()));
		otherInfoMap.put(OrderConstant.OTHERKEY_BINDMOBILE, member.getMobile());
		order.setOtherinfo(JsonUtils.writeMapToJson(otherInfoMap));
		return ErrorCode.getSuccessReturn(member);
	}
	
	private ErrorCode validateBindMember(String telephone, final boolean isBind, String bindmobile, String checkpass){
		if(isBind){
			if(StringUtils.isBlank(bindmobile)){
				if(!ValidateUtil.isMobile(telephone)){
					return ErrorCode.getFailure("绑定的用户手机号格式错误！");
				}
			}else{
				if(StringUtils.isBlank(telephone)){
					return ErrorCode.getFailure("电话号码格式错误！");
				}
				if(!ValidateUtil.isMobile(bindmobile)){
					return ErrorCode.getFailure("绑定的用户手机号格式错误！");
				}
				if(StringUtils.isBlank(checkpass)){
					return ErrorCode.getFailure("绑定动态码不能为空！");
				}
			}
		}else{
			if(StringUtils.isBlank(telephone)){
				return ErrorCode.getFailure("电话号码格式错误！");
			}
		}
		return ErrorCode.SUCCESS;
	}
	
	private ErrorCode validateApiUser(ApiUser partner){
		if(partner == null){
			return ErrorCode.getFailure("下单失败，权限问题！");
		}
		if(!(partner.getId().equals(PartnerConstant.GEWA_DRAMA_ADMIN_MOBILE) 
			|| partner.getId().equals(PartnerConstant.GEWA_DRAMA_ADMIN_OFFLINE))){
			return ErrorCode.getFailure("下单失败，权限问题！");
		}
		return ErrorCode.SUCCESS;
	}
	
	@Override
	public ErrorCode<DramaOrder> addDramaOrder(OpenDramaItem odi, TheatreSeatArea seatArea, String seatLabel,Long disid, String mobile, Member member, ApiUser partner, String ukey){
		//检查该用户有没有未付款的订单，如果有，将所有未付款的订单取消
		try{
			Long memberid = null;
			if(member != null){
				memberid = member.getId();
			}else{
				memberid = partner.getId();
			}
			if(StringUtils.isBlank(ukey)) ukey = String.valueOf(memberid);
			ErrorCode lastOrder = processLastOrder(memberid, ukey);
			if(!lastOrder.isSuccess()){
				return ErrorCode.getFailure(ApiConstant.CODE_SIGN_ERROR, lastOrder.getMsg());
			}
		}catch(Exception e){
			dbLogger.warn(StringUtil.getExceptionTrace(e, 5));
		}
		ErrorCode<DramaOrder> code = addInnerDramaOrder(odi, seatArea, seatLabel, disid, mobile, member, partner, ukey);
		if(!code.isSuccess()) return code;
		DramaOrder order = code.getRetval();
		return lockRemoteSeat(order, odi, null);
	}
	
	private ErrorCode<DramaOrder> addInnerDramaOrder(OpenDramaItem odi, TheatreSeatArea seatArea, String seatLabel,Long disid, String mobile, Member member, ApiUser partner, String ukey){
		ErrorCode<List<String>> remoteLockList = theatreOperationService.updateRemoteLockSeat(seatArea, OpiConstant.SECONDS_ADDORDER, false);
		if(!remoteLockList.isSuccess()){
			return ErrorCode.getFailure(ApiConstant.CODE_SIGN_ERROR, "订单失败，场馆网络异常！");
		}
		for(int i=0; i < 3;i++){
			try{
				return dramaOrderService.addDramaOrder(odi, seatArea, seatLabel, disid, mobile, member, partner, ukey, remoteLockList.getRetval());
			}catch (Exception e) {
				//非乐观锁异常
				if(!isUpdateErrorException(e)){
					dbLogger.warn(StringUtil.getExceptionTrace(e, 10));		
					String msg = "购买出错！";
					if(e instanceof OrderException){
						msg = ((OrderException)e).getMsg();
					}
					return ErrorCode.getFailure(msg);
				}else{
					dbLogger.warn("addDramaOrder:" + seatLabel+ ":" + i + "," + StringUtil.getExceptionTrace(e, 3));
				}
			}
		}
		return ErrorCode.getFailure("购买出错！");
	}
	
	@Override
	public ErrorCode processLastOrder(Long memberid, String ukey){
		try{
			ErrorCode<DramaOrder> lastOrder = dramaOrderService.processLastOrder(memberid, ukey);
			if(!lastOrder.isSuccess()){
				return ErrorCode.getFailure(ApiConstant.CODE_SIGN_ERROR, lastOrder.getMsg());
			}
			DramaOrder order = lastOrder.getRetval();
			if(order != null){
				if(order.isNew()){
					cancelDramaOrder(order, order.getUkey()+"", "重复订单");
				}
			}
			return ErrorCode.SUCCESS;
		}catch(Exception e){
			dbLogger.warn(StringUtil.getExceptionTrace(e, 5));
		}
		return ErrorCodeConstant.DATEERROR;
	}
	
	@Override
	public void cancelDramaOrder(String tradeNo, String ukey, String reason) {
		DramaOrder order = daoService.getObjectByUkey(DramaOrder.class, "tradeNo", tradeNo, false);
		cancelDramaOrder(order, ukey, reason);
	}
	
	@Override
	public void cancelDramaOrder(DramaOrder order, String ukey, String reason) {
		OpenDramaItem odi = daoService.getObjectByUkey(OpenDramaItem.class, "dpid", order.getDpid());
		if(!odi.hasSeller(OdiConstant.PARTNER_GEWA)){
			ErrorCode code = theatreOperationService.unlockRemoteSeat(order.getId());
			if(!code.isSuccess()){
				dbLogger.warn(code.getErrcode() + "," + code.getMsg());
			}else{
				List<SellDramaSeat> seatList = dramaOrderService.getDramaOrderSeatList(order.getId());
				theatreOperationService.removeLockSeatFromQryItemResponse(order.getAreaid(), seatList);
			}
		}
		dramaOrderService.cancelDramaOrder(order,  ukey, reason);
	}
	
	private ErrorCode<DramaOrder> lockRemotePrice(DramaOrder order, OpenDramaItem odi, String spkey){
		List<BuyItem> buyList = daoService.getObjectListByField(BuyItem.class, "orderid", order.getId());
		ErrorCode remoteCode = theatreOperationService.lockRemotePrice(odi, order, order.getMobile(), buyList);
		if(!remoteCode.isSuccess()){
			cancelDramaOrder(order, order.getUkey()+"", "锁定座位出错！");
			return ErrorCode.getFailure(remoteCode.getErrcode(), remoteCode.getMsg());
		}
		order.setStatus(OrderConstant.STATUS_NEW);
		daoService.saveObject(order);
		initSpecialDiscount(order, odi, buyList, spkey);
		return ErrorCode.getSuccessReturn(order);
	}
	
	
	private ErrorCode<DramaOrder> lockRemoteSeat(DramaOrder order, OpenDramaItem odi, String spkey){
		List<SellDramaSeat> seatList = dramaOrderService.getDramaOrderSeatList(order.getId());
		List<BuyItem> buyList = daoService.getObjectListByField(BuyItem.class, "orderid", order.getId());
		ErrorCode remoteCode = theatreOperationService.lockRemoteSeat(odi, order, order.getMobile(), seatList, buyList);
		if(!remoteCode.isSuccess()){
			cancelDramaOrder(order, order.getUkey()+"", "锁定座位出错！");
			return ErrorCode.getFailure(remoteCode.getErrcode(), remoteCode.getMsg());
		}
		order.setStatus(OrderConstant.STATUS_NEW);
		daoService.saveObject(order);
		theatreOperationService.addLockSeatToQryItemResponse(order.getAreaid(), seatList);
		initSpecialDiscount(order, odi, null, spkey);
		return ErrorCode.getSuccessReturn(order);
	}
	private void initSpecialDiscount(DramaOrder order, OpenDramaItem odi, List<BuyItem> buyList, String spkey){
		String spid = null;
		if(StringUtils.isNotBlank(spkey)){
			spid = PKCoderUtil.decryptString(spkey, SpecialDiscount.ENCODE_KEY);
		}
		if(StringUtils.isNotBlank(spid)){
			Map<String, String> otherinfoMap = VmUtils.readJsonToMap(odi.getOtherinfo());
			List<OpenDramaItem> itemList = new ArrayList<OpenDramaItem>();
			itemList.add(odi);
			if(odi.isOpenprice() && !CollectionUtils.isEmpty(buyList)){
				itemList = dramaOrderService.getOpenDramaItemList(odi, buyList);
				otherinfoMap = dramaOrderService.getOtherInfoMap(itemList);
			}
			SpecialDiscount sd = daoService.getObject(SpecialDiscount.class, new Long(spid));
			PayValidHelper pvh = new PayValidHelper(otherinfoMap);
			if(sd != null && DramaSpecialDiscountHelper.isEnabled(sd, itemList, pvh).isSuccess()){
				order.setOtherinfo(JsonUtils.addJsonKeyValue(order.getOtherinfo(), OpiConstant.FROM_SPID, spid));
				daoService.saveObject(order);
			}
		}
	}

	@Override
	public OrderOther getDramaOrderOtherData(DramaOrder order, List<BuyItem> buyList, final Map<Long, OpenDramaItem> odiMap, ModelMap model){
		Map<Long, TheatreSeatPrice> priceMap = new HashMap<Long, TheatreSeatPrice>();
		Map<Long, DisQuantity> disMap = new HashMap<Long, DisQuantity>();
		
		model.put("priceMap", priceMap);
		model.put("disMap", disMap);
		OrderOther orderOther = new OrderOther();
		model.put("orderOther", orderOther);
		for (BuyItem buy : buyList) {
			if(!StringUtils.equals(buy.getTag(), BuyItemConstant.TAG_DRAMAPLAYITEM)) continue;
			OpenDramaItem odi = odiMap.get(buy.getRelatedid());
			if(odi == null){
				odi = daoService.getObjectByUkey(OpenDramaItem.class, "dpid", buy.getRelatedid());
				odiMap.put(buy.getRelatedid(), odi);
			}
			if(!orderOther.isGreetings() && StringUtils.equals(odi.getGreetings(), Status.Y)){
				orderOther.setGreetings(true);
			}
			String takemethod = dramaOrderService.getTakemethodByOdi(order, odi);
			if(!orderOther.isEwarning() && StringUtils.equals(takemethod, TheatreProfile.TAKEMETHOD_A)){
				orderOther.setEwarning(true);
			}
			if(!orderOther.hasTakemethod(TheatreProfile.TAKEMETHOD_E)){
				//不包含电子票
				orderOther.setTakemethod(takemethod);
				if(StringUtils.isNotBlank(odi.getExpressid())){
					orderOther.setExpressid(odi.getExpressid());
				}
			}else if(orderOther.hasTakemethod(TheatreProfile.TAKEMETHOD_E, TheatreProfile.TAKEMETHOD_A)){
				//含快递、电子票
				if(StringUtils.equals(takemethod, TheatreProfile.TAKEMETHOD_E)){
					orderOther.setTakemethod(takemethod);
					if(StringUtils.isNotBlank(odi.getExpressid())){
						orderOther.setExpressid(odi.getExpressid());
					}
				}
			}
			if(odi.isOpenCardPay()){
				orderOther.insertElecard(OrderOther.PAY_CARD);
			}
			if(odi.isDisCountPay()){
				orderOther.insertElecard(OrderOther.PAY_DISCOUNT);
			}
			if(odi.isOpenPointPay()){
				orderOther.setOpenPointPay(true);
				orderOther.setMaxpoint(Math.max(orderOther.getMaxpoint(), odi.getMaxpoint()));
				orderOther.setMinpoint(Math.max(orderOther.getMinpoint(), odi.getMinpoint()));
			}
			if(!StringUtils.equals(buy.getSmallitemtype(), BuyItemConstant.SMALL_ITEMTYPE_PRICE)) continue;
			TheatreSeatPrice price = priceMap.get(buy.getSmallitemid());
			if(price == null){
				price = daoService.getObject(TheatreSeatPrice.class, buy.getSmallitemid());
				priceMap.put(buy.getSmallitemid(), price);
			}
			if(buy.getDisid() != null){
				DisQuantity dis = disMap.get(buy.getDisid());
				if(dis == null){
					dis = daoService.getObject(DisQuantity.class, buy.getDisid());
					disMap.put(buy.getDisid(), dis);
				}
			}
		}
		return orderOther;
	}
	
	@Override
	public ErrorCode updateOtherInfo(DramaOrder order, String greeting, Long memberid, ModelMap model){
		if(order == null) return ErrorCode.getFailure("订单错误！");
		if(order.isAllPaid() || order.isCancel()) return ErrorCode.getFailure("不能修改已支付或已（过时）取消的订单！"); 
		OpenDramaItem item = daoService.getObjectByUkey(OpenDramaItem.class, "dpid", order.getDpid(), false);
		List<BuyItem> buyList = daoService.getObjectListByField(BuyItem.class, "orderid", order.getId());
		List<OpenDramaItem> itemList = dramaOrderService.getOpenDramaItemList(item, buyList);
		Map<Long, OpenDramaItem> odiMap = BeanUtil.beanListToMap(itemList, "dpid");	
		OrderOther orderOther = getDramaOrderOtherData(order, buyList, odiMap, model);
		if(!orderOther.isGreetings()){
			return ErrorCode.getFailure("该订单不支持个性化票面设置！");
		}
		if (StringUtils.length(greeting) > 10){
			return ErrorCode.getFailure("定制类容不符合长度！");
		}
		ErrorCode<String> code = GewaOrderHelper.validGreetings(greeting);
		if(!code.isSuccess()) return ErrorCode.getFailure(code.getMsg());
		greeting = code.getRetval();
		Map<String, String> otherInfoMap = JsonUtils.readJsonToMap(order.getOtherinfo());
		if(StringUtils.isBlank(greeting)){
			otherInfoMap.remove(OrderConstant.OTHERKEY_GREETINGS);
		}else{
			otherInfoMap.put(OrderConstant.OTHERKEY_GREETINGS, greeting);
		}
		order.setOtherinfo(JsonUtils.writeMapToJson(otherInfoMap));
		daoService.saveObject(order);
		dbLogger.warn("drama order greeting:" + memberid +",greeting:" + greeting);
		return ErrorCode.SUCCESS;
	}
}
