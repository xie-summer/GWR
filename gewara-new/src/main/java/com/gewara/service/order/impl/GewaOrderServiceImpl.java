package com.gewara.service.order.impl;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.RandomUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import com.gewara.constant.AdminCityContant;
import com.gewara.constant.ApiConstant;
import com.gewara.constant.PayConstant;
import com.gewara.constant.PaymethodConstant;
import com.gewara.constant.PointConstant;
import com.gewara.constant.Status;
import com.gewara.constant.app.AppConstant;
import com.gewara.constant.order.ElecCardConstant;
import com.gewara.constant.sys.ConfigConstant;
import com.gewara.constant.sys.JsonDataKey;
import com.gewara.constant.sys.MongoData;
import com.gewara.constant.ticket.OrderConstant;
import com.gewara.constant.ticket.OrderExtraConstant;
import com.gewara.constant.ticket.OrderNoteConstant;
import com.gewara.helper.order.GewaOrderHelper;
import com.gewara.helper.order.OrderContainer;
import com.gewara.json.AppSourceCount;
import com.gewara.json.order.OutOriginOrder;
import com.gewara.model.api.RandomNum;
import com.gewara.model.common.JsonData;
import com.gewara.model.common.Province;
import com.gewara.model.express.ExpressConfig;
import com.gewara.model.express.ExpressProvince;
import com.gewara.model.goods.Goods;
import com.gewara.model.pay.BuyItem;
import com.gewara.model.pay.Cpcounter;
import com.gewara.model.pay.Discount;
import com.gewara.model.pay.ElecCard;
import com.gewara.model.pay.GewaOrder;
import com.gewara.model.pay.OrderAddress;
import com.gewara.model.pay.OrderExtra;
import com.gewara.model.pay.OrderNote;
import com.gewara.model.pay.OtherFeeDetail;
import com.gewara.model.pay.SdRecord;
import com.gewara.model.pay.SpCode;
import com.gewara.model.pay.Spcounter;
import com.gewara.model.pay.SpecialDiscount;
import com.gewara.model.pay.SportOrder;
import com.gewara.model.pay.TicketOrder;
import com.gewara.model.user.MemberInfo;
import com.gewara.model.user.MemberUsefulAddress;
import com.gewara.mongo.MongoService;
import com.gewara.service.OperationService;
import com.gewara.service.OrderException;
import com.gewara.service.gewapay.PaymentService;
import com.gewara.service.impl.BaseServiceImpl;
import com.gewara.service.member.PointService;
import com.gewara.service.order.GewaOrderService;
import com.gewara.support.ErrorCode;
import com.gewara.untrans.CooperateService;
import com.gewara.untrans.monitor.MonitorService;
import com.gewara.util.BeanUtil;
import com.gewara.util.DateUtil;
import com.gewara.util.HttpResult;
import com.gewara.util.HttpUtils;
import com.gewara.util.JsonUtils;
import com.gewara.util.StringUtil;
import com.gewara.util.ValidateUtil;
import com.gewara.util.VmUtils;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

public class GewaOrderServiceImpl extends BaseServiceImpl implements GewaOrderService{
	@Autowired@Qualifier("pointService")
	protected PointService pointService;
	public void setPointService(PointService pointService) {
		this.pointService = pointService;
	}
	@Autowired@Qualifier("operationService")
	protected OperationService operationService;
	public void setOperationService(OperationService operationService) {
		this.operationService = operationService;
	}
	@Autowired@Qualifier("paymentService")
	protected PaymentService paymentService;
	public void setPaymentService(PaymentService paymentService) {
		this.paymentService = paymentService;
	}
	@Autowired@Qualifier("monitorService")
	protected MonitorService monitorService;
	public void setMonitorService(MonitorService monitorService) {
		this.monitorService = monitorService;
	}
	@Autowired@Qualifier("cooperateService")
	private CooperateService cooperateService;
	public void setCooperateService(CooperateService cooperateService) {
		this.cooperateService = cooperateService;
	}
	
	@Autowired@Qualifier("mongoService")
	private MongoService mongoService;
	
	@Override
	public SpecialDiscount getSpdiscountBySpflag(String spflag){
		List<SpecialDiscount> result = hibernateTemplate.find("from SpecialDiscount where flag = ?", spflag);
		if(result.size()>0) return result.get(0);
		return null;
	}
	@Override
	public ErrorCode<GewaOrder> removeDiscount(GewaOrder order, Long discountId){
		if(!order.isNew()) return ErrorCode.getFailure("订单状态错误（" + order.getStatusText() + "）！");
		if(order.getStatus().equals(OrderConstant.STATUS_NEW_CONFIRM)) return ErrorCode.getFailure("已确认的订单不能修改！");
		if(order.isAllPaid() || order.isCancel()) return ErrorCode.getFailure("不能操作已支付或已（过时）取消的订单！");
		Map<String, String> otherinfoMap = VmUtils.readJsonToMap(order.getOtherinfo());
		if(StringUtils.isNotBlank(otherinfoMap.get(PayConstant.KEY_CHANGECOST)) || 
				StringUtils.isNotBlank(otherinfoMap.get(PayConstant.KEY_BINDGOODS))){
			return ErrorCode.getFailure("此优惠无法取消！您可选择关闭此页面后重新下订单。");
		}
		List<Discount> discountList = paymentService.getOrderDiscountList(order);
		Discount cur = null;
		for(Discount discount: discountList){
			if(discount.getId().equals(discountId)){
				cur = discount;
				break;
			}
		}
		if(cur!=null){
			GewaOrderHelper.unuseDiscount(order, discountList, cur);
			baseDao.removeObject(cur);
			if(discountList.size()==0){
				otherinfoMap.remove(PayConstant.KEY_CARDBINDPAY);
				order.setOtherinfo(JsonUtils.writeMapToJson(otherinfoMap));
			}
			baseDao.saveObject(order);
			return ErrorCode.getSuccessReturn(order);
		}
		return ErrorCode.getFailure("此兑换券没有使用");
		
	}
	/**
	 * 订单取消等名额恢复
	 * @param tradeNo
	 */
	protected void restoreSdCounterByTrade(String tradeNo){
		SdRecord record = baseDao.getObject(SdRecord.class, tradeNo);
		if(record!=null){
			Spcounter spcounter = baseDao.getObject(Spcounter.class, record.getSpcounterid());
			if(StringUtils.equals(spcounter.getCtltype(), Spcounter.CTLTYPE_QUANTITY)){
				spcounter.setAllowaddnum(spcounter.getAllowaddnum() + record.getQuantity());
			}else{
				spcounter.setAllowaddnum(spcounter.getAllowaddnum() + 1);
			}
			if(StringUtils.isBlank(record.getCpcounters())){
				List<Long> cpidList = BeanUtil.getIdList(record.getCpcounters(), ",");
				List<Cpcounter> counterList = baseDao.getObjectList(Cpcounter.class, cpidList);
				for(Cpcounter cp: counterList){
					if(StringUtils.equals(spcounter.getCtltype(), Spcounter.CTLTYPE_QUANTITY)){
						cp.setAllownum(cp.getAllownum() + record.getQuantity());
					}else{
						cp.setAllownum(cp.getAllownum() + 1);
					}
				}
				baseDao.saveObjectList(counterList);
			}
			baseDao.saveObject(spcounter);
			baseDao.removeObject(record);
		}
	}

	@Override
	public void addOrderOrigin(GewaOrder order, String originStr){
		try{
			String ori = validOrderOrigin(originStr);
			if(StringUtils.isNotBlank(ori)){
				AppSourceCount asc = new AppSourceCount(order);
				asc.setOrderOrigin(ori);
				monitorService.addMonitorEntry(AppConstant.TABLE_APPSOURCE, BeanUtil.getSimpleStringMap(asc));
				if (StringUtils.startsWith(originStr, "360cps")) {
					String[] origin = StringUtils.split(originStr,":");
					if (origin.length != 4) return;
					String[] temp = origin[3].split("#");
					if (temp.length != 4) return;
					OutOriginOrder cpsOrder = new OutOriginOrder();
					cpsOrder.setId(order.getId());
					cpsOrder.setBid(temp[3]);
					cpsOrder.setQid(temp[2]);
					cpsOrder.setQihoo_id(temp[1]);
					cpsOrder.setExt(temp[0]);
					cpsOrder.setOrder_id(order.getTradeNo());
					cpsOrder.setOrder_time(DateUtil.formatTimestamp(order.getAddtime()));
					cpsOrder.setP_info(order.getDescription2());
					cpsOrder.setStatus("new");
					// 先设置为0，否则360当天的订单同步为出错
					cpsOrder.setAmount(0);
					if (order instanceof TicketOrder) {
						TicketOrder ticketOrder = (TicketOrder)order;
						cpsOrder.setMovieid(ticketOrder.getMovieid());
						cpsOrder.setDescription2(order.getDescription2());
						cpsOrder.setServer_price("0");
						cpsOrder.setTotal_comm(String.valueOf(order.getDue()));
						cpsOrder.setTotal_price(order.getDue());
						cpsOrder.setAmount(order.getTotalfee());
						cpsOrder.setCoupon(String.valueOf(order.getDiscount()));
						cpsOrder.setStatus(order.getStatus());
						cpsOrder.setQuantity(order.getQuantity() == null ? "" : order.getQuantity().toString());
						cpsOrder.setUnitprice(order.getUnitprice() == null ? "" : order.getUnitprice().toString());
					}
					cpsOrder.setOrder_updtime(DateUtil.formatTimestamp(order.getAddtime()));
					cpsOrder.setOutOrigin("360cps");
					mongoService.saveOrUpdateObject(cpsOrder, MongoData.DEFAULT_ID_NAME);
				}
			}
		}catch(Exception e){//ignore
		}
	}
	@Override
	public String validOrderOrigin(String originStr){
		try{
			if(StringUtils.isBlank(originStr)) return null;
			String[] origin = StringUtils.split(originStr,":");
			if((!StringUtils.startsWith(originStr, "360cps") && origin.length!=3) 
					|| (StringUtils.startsWith(originStr, "360cps") && origin.length != 4)) return null;
			if(StringUtil.md5WithKey(origin[0] + origin[1], 8).equals(origin[2])){
				Long time = new Long(origin[1]);
				if(time > System.currentTimeMillis() && StringUtils.isNotBlank(origin[0])){
					return origin[0];
				}
			}
		}catch(Exception e){//ignore
		}
		return null;
	}
	@Override
	public void addBuyItemList(GewaOrder order, String items){
		List<BuyItem> itemList = getBuyItemList(items);
		if(itemList.size()>0) {
			int itemfee = 0;
			for(BuyItem item : itemList){
				item.setOrderid(order.getId());
				item.setMemberid(order.getMemberid());
				itemfee += item.getDue();
			}
			baseDao.saveObjectList(itemList);
			order.setItemfee(itemfee);
			baseDao.saveObject(order);
		}
	}
	
	private List<BuyItem> getBuyItemList(String items){
		List<BuyItem> itemList = new ArrayList<BuyItem>();
		if(StringUtils.isBlank(items)) return itemList;
		String[] str = items.split(",");
		for(int i=0; i<str.length; i+=2){
			Goods goods = baseDao.getObject(Goods.class, Long.valueOf(str[i]));
			if(goods!=null) {
				BuyItem item = new BuyItem(Integer.valueOf(str[i+1]), goods);
				item.setTotalfee(goods.getUnitprice()*item.getQuantity());
				item.setTotalcost(goods.getCostprice()*item.getQuantity());
				itemList.add(item);
			}
		}
		return itemList;
	}
	@Override
	public void zeropayGewaOrder(GewaOrder order) throws OrderException {
		if(order.isPaidSuccess() || order.isPaidFailure() || order.isPaidUnfix()) return;
		if(order.isZeroPay()){
			//1,检查卡
			List<Discount> discountList = paymentService.getOrderDiscountList(order);
			for(Discount discount: discountList){
				if(PayConstant.DISCOUNT_TAG_ECARD.equals(discount.getTag())){
					ElecCard card = baseDao.getObject(ElecCard.class, discount.getRelatedid());
					if(card.isUsed()){
						dbLogger.error("order:支付订单" + order.getTradeNo() + "失败，抵用券使用有问题（重复使用？）！");
						throw new OrderException(ApiConstant.CODE_DATA_ERROR, "抵用券" + card.getCardno() + "已经使用过");
					}
				}else if(PayConstant.DISCOUNT_TAG_POINT.equals(discount.getTag())){
					//ignore
				}else if(PayConstant.DISCOUNT_TAG_PARTNER.equals(discount.getTag())){
					//ignore 商家优惠
				}
			}
			//2,更改状态
			Timestamp curTime = new Timestamp(System.currentTimeMillis());
			dbLogger.warn(order.getTradeNo() + "GewaPay支付前状态：" + order.getStatus());
			order.setStatus(OrderConstant.STATUS_PAID_FAILURE); //先设置成此状态，第二步设成paid_success
			order.setPaidtime(curTime);
			order.setUpdatetime(curTime);
			order.setModifytime(curTime);
			baseDao.saveObject(order);
		}
	}
	/**
	 * @param order
	 * @param discountList
	 * @param pointRatio
	 * @param deductPoint 需要减少的积分
	 * @throws OrderException
	 */
	protected OrderContainer processOrderPayInternal(GewaOrder order) throws OrderException{
		if(order.getStatus().startsWith(OrderConstant.STATUS_PAID)){
			if(order.isNotAllPaid()) throw new OrderException(ApiConstant.CODE_PAY_ERROR, "订单付款金额不足！");
			if(order.isPaidFailure()){//付钱了，但状态还是未成功，做检查
				OrderContainer container = new OrderContainer();
				Map<String, String> changeHisMap = JsonUtils.readJsonToMap(order.getChangehis());
				String successChange = changeHisMap.get(OrderConstant.CHANGEHIS_KEY_SUCCESSCHANGE);
				//是否是成功订单代处理 success = "true" 是就是跳过积分，特价活动
				final boolean checkChange = Boolean.parseBoolean(successChange);
				//1、更新抵用券
				//int count = 0; //优惠券使用次数
				int pdeduct = 0; //积分使用和
				SpecialDiscount sd = null;
				List<Discount> discountList = paymentService.getOrderDiscountList(order);
				Map<Long, ElecCard> useCardMap = new HashMap<Long, ElecCard>();
				for(Discount discount: discountList){
					if(PayConstant.DISCOUNT_TAG_ECARD.equals(discount.getTag())){
						ElecCard card = baseDao.getObject(ElecCard.class, discount.getRelatedid());
						if(card.isUsed() && !order.getId().equals(card.getOrderid())){
							dbLogger.error("order:支付订单" + order.getTradeNo() + "失败，抵用券使用有问题（重复使用？）！");
							throw new OrderException(ApiConstant.CODE_PAY_ERROR, "抵用券" + card.getCardno() + "已经使用过");
						}
						card.setStatus(ElecCardConstant.STATUS_USED);
						card.setOrderid(order.getId());
						baseDao.saveObject(card);
						useCardMap.put(discount.getId(), card);
					}else if(PayConstant.DISCOUNT_TAG_POINT.equals(discount.getTag())){//返回积分使用记录
						pdeduct += ConfigConstant.POINT_RATIO * discount.getAmount();
					}else if(PayConstant.DISCOUNT_TAG_PARTNER.equals(discount.getTag())){
						sd = baseDao.getObject(SpecialDiscount.class, discount.getRelatedid());
						Spcounter spcounter = paymentService.updateSpdiscountMemberCount(sd, order);
						container.setSpdiscount(sd);
						container.setSpcounter(spcounter);
						if(StringUtils.equals(sd.getVerifyType(), SpecialDiscount.VERIFYTYPE_ONLYONE)){
							String spcodeStr = JsonUtils.getJsonValueByKey(order.getOtherinfo(), PayConstant.KEY_USE_SPCODE);
							if(StringUtils.isNotBlank(spcodeStr)){
								SpCode spcode = baseDao.getObject(SpCode.class, Long.valueOf(spcodeStr));
								if(spcode.getUsedcount() > 0){
									throw new OrderException(ApiConstant.CODE_PAY_ERROR, "电子码已经使用过！");
								}
								spcode.setUsedcount(spcode.getUsedcount() + 1);
								baseDao.saveObject(spcode);
							}
							
						}
					}
					discount.setStatus(OrderConstant.DISCOUNT_STATUS_Y);
					baseDao.saveObject(discount);
				}
				if(!useCardMap.isEmpty()){
					container.setUseCardMap(useCardMap);
				}
				if(!checkChange && (sd!=null && (StringUtils.isNotBlank(sd.getValidBackUrl()) || StringUtils.equals("true",sd.getCardNumUnique())))){
					dbLogger.warn(sd.getFlag()+"验证数据：" + order.getTradeNo());
					if(StringUtils.equals("true",sd.getCardNumUnique())){
						boolean result = cooperateService.addCardnumOperation(order.getTradeNo(), order.getOtherinfo(),sd.getId());
						if(!result){
							throw new OrderException(ApiConstant.CODE_DATA_ERROR, "支付订单" + order.getTradeNo() + "失败，" + result);
						}
					}
					if(StringUtils.isNotBlank(sd.getValidBackUrl())){
						Map<String, String> params = new HashMap<String, String>();
						params.put("otherinfo", order.getOtherinfo());
						params.put("tradeno", order.getTradeNo());
						params.put("orderid", order.getId()+"");
						params.put("spid", ""+sd.getId());
						params.put("addtime", DateUtil.formatTimestamp(order.getAddtime()));
						//TODO:8080 using container config
						HttpResult code = HttpUtils.postUrlAsString("http://localhost:8080/" + sd.getValidBackUrl(), params);
						dbLogger.warn(sd.getFlag()+"获取返回验证：" + code.getMsg()  + ":" + code.getResponse());
						if(code.isSuccess()){
							String res = code.getResponse();
							if(!StringUtils.contains(res, "success")){
								throw new OrderException(ApiConstant.CODE_DATA_ERROR, "支付订单" + order.getTradeNo() + "失败，" + res);
							}
						}else{
							throw new OrderException(ApiConstant.CODE_DATA_ERROR, "验证请求错误，请重试:" + order.getTradeNo());
						}
					}
				}
				if(!checkChange && !order.sureOutPartner()  && pdeduct > 0){
					//检查积分
					MemberInfo info = baseDao.getObject(MemberInfo.class, order.getMemberid());
					if(info.getPointvalue() < pdeduct) throw new OrderException(ApiConstant.CODE_DATA_ERROR, "积分不够，需要" + pdeduct + "积分，但只有" + info.getPointvalue());
					pointService.addPointInfo(order.getMemberid(), -pdeduct, order.getTradeNo(), PointConstant.TAG_TRADE);
				}
				order.setStatus(OrderConstant.STATUS_PAID_UNFIX);
				baseDao.saveObject(order);
				container.setDiscountList(discountList);
				container.setOrder(order);
				return container;
			}else{
				//不做处理
				return null;
			}
		}else{
			throw new OrderException(ApiConstant.CODE_DATA_ERROR, "订单状态不正确！");
		}
		
	}
	
	@Override
	public OrderExtra processOrderExtra(GewaOrder order){
		Map<String, String> changeHisMap = JsonUtils.readJsonToMap(order.getChangehis());
		String successChange = changeHisMap.get(OrderConstant.CHANGEHIS_KEY_SUCCESSCHANGE);
		//是否是成功订单代处理 success = "true" 是就更改OrderExtra 
		final boolean checkChange = !Boolean.parseBoolean(successChange);
		OrderExtra orderExtra = baseDao.getObject(OrderExtra.class, order.getId());
		if(!checkChange && orderExtra == null){
			final String updateSql = "update OrderExtra set id=? where tradeno=? ";
			int update = hibernateTemplate.bulkUpdate(updateSql, order.getId(), order.getTradeNo());
			hibernateTemplate.flush();
			dbLogger.warn("update_orderExtra ："+ order.getTradeNo() + ",update:" + update);
			orderExtra = baseDao.getObject(OrderExtra.class, order.getId());
		}
		if(orderExtra == null){
			orderExtra = new OrderExtra(order);
		}
		if(StringUtils.startsWith(order.getPaymethod(), PaymethodConstant.PAYMETHOD_UMPAY)){
			orderExtra.setInvoice(OrderExtraConstant.INVOICE_F);
		}
		OrderAddress orderAddress = baseDao.getObject(OrderAddress.class, order.getTradeNo());
		if(orderAddress != null){
			orderExtra.setExpresstype(orderAddress.getExpresstype());
		}
		baseDao.saveObject(orderExtra);
		return orderExtra;
	}
	
	@Override
	public void cancelOrderExtra(GewaOrder order){
		OrderExtra orderExtra = baseDao.getObject(OrderExtra.class, order.getId());
		if(orderExtra != null){
			orderExtra.setStatus(OrderConstant.STATUS_PAID_RETURN);
			orderExtra.setUpdatetime(DateUtil.getCurFullTimestamp());
			dbLogger.warn("cancel_orderExtra:" + order.getTradeNo() + "," + BeanUtil.buildString(orderExtra, true));
			baseDao.saveObject(orderExtra);
		}
	}
	
	@Override
	public ErrorCode cancelOrderNote(GewaOrder order){
		Timestamp cur = DateUtil.getCurFullTimestamp();
		List<OrderNote> noteList = baseDao.getObjectListByField(OrderNote.class, "orderid", order.getId());
		for (OrderNote note : noteList) {
			note.setStatus(OrderNoteConstant.STATUS_R);
			note.setUpdatetime(cur);
		}
		baseDao.saveObjectList(noteList);
		return ErrorCode.SUCCESS;
	}
	
	@Override
	public String nextRandomNum(Timestamp validtime, int length, String leftPad) {
		String random = getRandomNum();
		RandomNum randomNum = baseDao.getObject(RandomNum.class, random);
		while(randomNum!=null){
			random = getRandomNum();
			randomNum = baseDao.getObject(RandomNum.class, random);
		}
		randomNum = new RandomNum(random, validtime);
		baseDao.saveObject(randomNum);
		random = StringUtils.leftPad(random, length, leftPad);
		return random; 
	}
	private String getRandomNum(){
		int random = RandomUtils.nextInt(100000000);
		if(random<10000) random = (random * 7373);
		if(random<10000) random = (random * 1771);
		return "" + random;
	}
	
	@Override
	public ErrorCode<ExpressProvince> getExpressFee(ExpressConfig expressConfig, String provincecode){
		if(expressConfig == null) return ErrorCode.getFailure(ApiConstant.CODE_SIGN_ERROR, "配送方式不能为空！");
		if(StringUtils.isBlank(provincecode)) return ErrorCode.getFailure(ApiConstant.CODE_SIGN_ERROR, "快递城市编码不能为空！");
		Province province= baseDao.getObject(Province.class, provincecode);
		if(province == null) return ErrorCode.getFailure(ApiConstant.CODE_SIGN_ERROR, "城市编码错误！");
		List<ExpressProvince> provinceList = baseDao.getObjectListByField(ExpressProvince.class, "expressid", expressConfig.getId());
		Map<String, ExpressProvince> provinceMap = BeanUtil.beanListToMap(provinceList, "provincecode");
		ExpressProvince expressProvince = provinceMap.get(provincecode);
		if(expressProvince == null) return ErrorCode.getFailure(ApiConstant.CODE_SIGN_ERROR, province.getProvincename() +"，不支持快递！如需帮助请拨打：4000-406506");
		return ErrorCode.getSuccessReturn(expressProvince);
	}
	
	@Override
	public ErrorCode<Integer> computeExpressFee(GewaOrder order, ExpressConfig expressConfig, String provincecode){
		ErrorCode<ExpressProvince> code = getExpressFee(expressConfig, provincecode);
		if(!code.isSuccess()) return ErrorCode.getFailure(code.getErrcode(), code.getMsg());
		ExpressProvince province = code.getRetval();
		int price = province.getExpressfee();
		String reason = "快递费" + province.getExpressfee();
		// 免运费必须大于 0;
		if(province.getFreelimit()> 0 && order.getDue()>= province.getFreelimit()){
			reason = reason + ",满" +province.getFreelimit() +"免运费"; 
			price = 0;
		}
		List<OtherFeeDetail> feeList = baseDao.getObjectListByField(OtherFeeDetail.class, "orderid", order.getId());
		Map<String, OtherFeeDetail> feeMap = BeanUtil.beanListToMap(feeList, "feetype");
		OtherFeeDetail feeDetail = feeMap.get(OtherFeeDetail.FEETYPE_E);
		if(feeDetail == null){
			feeDetail = new OtherFeeDetail(order.getId(), OtherFeeDetail.FEETYPE_E, price, reason);
		}else{
			feeDetail.setFee(price);
			feeDetail.setReason(reason);
		}
		//计算快递费
		feeMap.put(OtherFeeDetail.FEETYPE_E, feeDetail);
		GewaOrderHelper.refreshOtherfee(order, feeMap.values());
		Map<String, String> otherFeeMap = JsonUtils.readJsonToMap(order.getOtherFeeRemark());
		final int tmpPrice = feeDetail.getFee() * feeDetail.getQuantity();
		otherFeeMap.put(OtherFeeDetail.FEETYPE_E, tmpPrice +"");
		order.setOtherFeeRemark(JsonUtils.writeMapToJson(otherFeeMap));
		order.setExpress(Status.Y);
		baseDao.saveObjectList(order, feeDetail);
		return ErrorCode.getSuccessReturn(tmpPrice);
	}
	
	@Override
	public ErrorCode clearExpressFee(GewaOrder order){
		List<OtherFeeDetail> feeList = baseDao.getObjectListByField(OtherFeeDetail.class, "orderid", order.getId());
		Map<String, OtherFeeDetail> feeMap = BeanUtil.beanListToMap(feeList, "feetype");
		OtherFeeDetail feeDetail = feeMap.remove(OtherFeeDetail.FEETYPE_E);
		if(feeDetail == null) return ErrorCode.SUCCESS;
		Map<String, String> otherFeeMap = JsonUtils.readJsonToMap(order.getOtherFeeRemark());
		otherFeeMap.remove(OtherFeeDetail.FEETYPE_E);
		order.setOtherFeeRemark(JsonUtils.writeMapToJson(otherFeeMap));
		GewaOrderHelper.refreshOtherfee(order, feeMap.values());
		baseDao.removeObject(feeDetail);
		order.setExpress(Status.N);
		baseDao.saveObject(order);
		return ErrorCode.SUCCESS;
	}

	@Override
	public Integer getUmpayfee(GewaOrder order){
		JsonData rateData = baseDao.getObject(JsonData.class, JsonDataKey.KEY_UMPAYFEE);
		int umpayfee = 0;
		int umpayothefee = 0;
		if(order.getOtherfee()>0){
			List<OtherFeeDetail> feeList = baseDao.getObjectListByField(OtherFeeDetail.class, "orderid", order.getId());
			Map<String, OtherFeeDetail> feeMap = BeanUtil.beanListToMap(feeList, "feetype");
			OtherFeeDetail feeDetail = feeMap.get(OtherFeeDetail.FEETYPE_U);
			if(feeDetail!=null){
				umpayothefee = feeDetail.getFee();
			}
		}
		if(StringUtils.equals(order.getCitycode(), AdminCityContant.CITYCODE_SH)){
			umpayfee = VmUtils.getFeeByRate(order.getTotalAmount()-umpayothefee, order.getDiscount(), rateData.getData());
		}else{
			umpayfee = VmUtils.getFeeByRate(order.getTotalAmount()-umpayothefee, order.getDiscount(), "30");
		}
		return umpayfee;
	}
	
	@Override
	public ErrorCode<Integer> computeUmpayfee(GewaOrder order){
		if(!ValidateUtil.isYdMobile(order.getMobile())) return ErrorCode.getFailure(ApiConstant.CODE_DATA_ERROR, "话费支付仅支持移动手机");
		//if(!StringUtils.equals(order.getCitycode(), AdminCityContant.CITYCODE_SH)) return showJsonError(model, "仅支持上海移动手机");
		
		List<OtherFeeDetail> feeList = baseDao.getObjectListByField(OtherFeeDetail.class, "orderid", order.getId());
		Map<String, OtherFeeDetail> feeMap = BeanUtil.beanListToMap(feeList, "feetype");
		OtherFeeDetail feeDetail = feeMap.get(OtherFeeDetail.FEETYPE_U);
		if(feeDetail == null){
			int umpayfee = getUmpayfee(order);
			String reason = "话费支付手续费" + umpayfee+"元";
			feeDetail = new OtherFeeDetail(order.getId(), OtherFeeDetail.FEETYPE_U, umpayfee, reason);
			//计算话费支付手续费
			feeMap.put(OtherFeeDetail.FEETYPE_U, feeDetail);
			GewaOrderHelper.refreshOtherfee(order, feeMap.values());
			Map<String, String> otherMap = VmUtils.readJsonToMap(order.getOtherinfo());
			otherMap.put("otherfeeTitle", "话费支付手续费" + umpayfee+"元");
			order.setOtherinfo(JsonUtils.writeMapToJson(otherMap));
			Map<String, String> otherFeeMap = JsonUtils.readJsonToMap(order.getOtherFeeRemark());
			otherFeeMap.put(OtherFeeDetail.FEETYPE_U, umpayfee +"");
			order.setOtherFeeRemark(JsonUtils.writeMapToJson(otherFeeMap));
			baseDao.saveObjectList(order, feeDetail);
			return ErrorCode.getSuccessReturn(umpayfee);
		}else{
			return ErrorCode.getSuccessReturn(feeDetail.getFee());
		}
	}
	
	@Override
	public ErrorCode<Integer> computeChangeOrderFee(GewaOrder order){
		Map<String,String> changeHisMap = JsonUtils.readJsonToMap(order.getChangehis());
		String successChange = changeHisMap.get(OrderConstant.CHANGEHIS_KEY_SUCCESSCHANGE);
		Integer subAmount = 0;
		String cartype = "";
		if(order instanceof TicketOrder){
			cartype = PayConstant.CARDTYPE_INNER_MOVIE;
		}else if(order instanceof SportOrder){
			cartype = PayConstant.CARDTYPE_INNER_SPORT;
		}
		if(StringUtils.isBlank(cartype)){
			return ErrorCode.getFailure("成功订单暂不支持换场次！");
		}
		if(StringUtils.isNotBlank(successChange) && Boolean.parseBoolean(successChange)){
			GewaOrder oldOrder = baseDao.getObjectByUkey(GewaOrder.class, "tradeNo", changeHisMap.get(OrderConstant.CHANGEHIS_KEY_CHANGESEAT));
			if(oldOrder != null){
				subAmount = order.getTotalAmount() - oldOrder.getTotalAmount();
				if(subAmount < 0){
					subAmount = Math.abs(subAmount);
					List<OtherFeeDetail> feeList = baseDao.getObjectListByField(OtherFeeDetail.class, "orderid", order.getId());
					Map<String, OtherFeeDetail> feeMap = BeanUtil.beanListToMap(feeList, "feetype");
					OtherFeeDetail feeDetail = feeMap.get(OtherFeeDetail.FEETYPE_C);
					String reason = "更换订单产生费用" + subAmount + "元";
					if(feeDetail == null){
						feeDetail = new OtherFeeDetail(order.getId(), OtherFeeDetail.FEETYPE_C, subAmount, reason);
					}else{
						feeDetail.setFee(subAmount);
						feeDetail.setReason(reason);
					}
					//更换订单产生多余的费用
					feeMap.put(OtherFeeDetail.FEETYPE_C, feeDetail);
					GewaOrderHelper.refreshOtherfee(order, feeMap.values());
					Map<String, String> otherFeeMap = JsonUtils.readJsonToMap(order.getOtherFeeRemark());
					final int tmpPrice = feeDetail.getFee() * feeDetail.getQuantity();
					otherFeeMap.put(OtherFeeDetail.FEETYPE_C, tmpPrice +"");
					order.setOtherFeeRemark(JsonUtils.writeMapToJson(otherFeeMap));
					baseDao.saveObjectList(order, feeDetail);
				}else if(subAmount > 0){
					List<Discount> discountList = paymentService.getOrderDiscountList(order);
					Discount discount = new Discount(order.getId(), PayConstant.DISCOUNT_TAG_INNER,  order.getMemberid(), cartype);
					discount.setDescription("格瓦拉优惠" + subAmount + "元");
					discount.setAmount(subAmount);
					baseDao.saveObject(discount);
					GewaOrderHelper.useDiscount(order, discountList, discount);
				}
			}
		}
		return ErrorCode.getSuccessReturn(subAmount);
	}
	
	@Override
	public ErrorCode validMemberUserfulAddress(MemberUsefulAddress memberUsefulAddress){
		if(StringUtils.isBlank(memberUsefulAddress.getProvincecode())
				|| StringUtils.isBlank(memberUsefulAddress.getCitycode())
				|| StringUtils.isBlank(memberUsefulAddress.getCountycode())){
				return ErrorCode.getFailure(ApiConstant.CODE_SIGN_ERROR, "地址有错误！");
			}
		return ErrorCode.SUCCESS;
	}
	
	@Override
	public ErrorCode<OrderAddress> createOrderAddress(GewaOrder order, MemberUsefulAddress memberUsefulAddress, ExpressConfig expressConfig){
		ErrorCode validCode = validMemberUserfulAddress(memberUsefulAddress);
		if(!validCode.isSuccess()) return ErrorCode.getFailure(validCode.getErrcode(), validCode.getMsg());
		OrderAddress orderAddress = baseDao.getObject(OrderAddress.class, order.getTradeNo());
		if(orderAddress == null){
			orderAddress = new OrderAddress(order.getTradeNo());
		}
		orderAddress.setUsefulAddressId(memberUsefulAddress.getId());
		orderAddress.setProvincecode(memberUsefulAddress.getProvincecode());
		orderAddress.setProvincename(memberUsefulAddress.getProvincename());
		orderAddress.setCitycode(memberUsefulAddress.getCitycode());
		orderAddress.setCityname(memberUsefulAddress.getCityname());
		orderAddress.setCountycode(memberUsefulAddress.getCountycode());
		orderAddress.setCountyname(memberUsefulAddress.getCountyname());
		orderAddress.setAddress(memberUsefulAddress.getAddress());
		orderAddress.setRealname(memberUsefulAddress.getRealname());
		orderAddress.setMobile(memberUsefulAddress.getMobile());
		orderAddress.setPostalcode(memberUsefulAddress.getPostalcode());
		orderAddress.setExpresstype(expressConfig.getExpresstype());
		baseDao.saveObject(orderAddress);
		return ErrorCode.getSuccessReturn(orderAddress);
	}
	
	/**
	 * 同步360的订单
	 * @return
	 */
	@Override
	public ErrorCode<String> syn360CPSOrderByDay(Date date) {
		ErrorCode<String> result = ErrorCode.SUCCESS;
		if (date == null) {
			date = DateUtil.getCurDate();
		}
		Date yesterDay = DateUtil.addDay(date, -1);
		String startdate = DateUtil.formatTimestamp(DateUtil.getBeginTimestamp(yesterDay));
		String enddate = DateUtil.formatTimestamp(DateUtil.getBeginTimestamp(date));
		DBObject queryCondition = new BasicDBObject();
		DBObject relate = mongoService.queryAdvancedDBObject("order_time", new String[]{">=","<"}, new String[]{startdate, enddate});
		DBObject relate1 = mongoService.queryBasicDBObject("outOrigin", "=", "360cps");
		queryCondition.putAll(relate);
		queryCondition.putAll(relate1);
		List<OutOriginOrder> cpsOrderList = mongoService.getObjectList(OutOriginOrder.class, queryCondition);
		List<Long> orderIdList = BeanUtil.getBeanPropertyList(cpsOrderList, "id", true);
		Map<String, OutOriginOrder> cpsOrderMap = BeanUtil.beanListToMap(cpsOrderList, "order_id");
		List<TicketOrder> ticketOrdersList = baseDao.getObjectList(TicketOrder.class, orderIdList);
		for (TicketOrder order : ticketOrdersList) {
			try {
				if (cpsOrderMap.get(order.getTradeNo()) != null) {
					OutOriginOrder cpsTempOrder = cpsOrderMap.get(order.getTradeNo());
					cpsTempOrder.setOrder_updtime(DateUtil.formatTimestamp(order.getUpdatetime()));
					cpsTempOrder.setDescription2(order.getDescription2());
					cpsTempOrder.setServer_price("0");
					cpsTempOrder.setTotal_comm(String.valueOf(order.getDue()));
					cpsTempOrder.setTotal_price(order.getDue());
					cpsTempOrder.setAmount(order.getTotalfee());
					cpsTempOrder.setCoupon(String.valueOf(order.getDiscount()));
					cpsTempOrder.setStatus(order.getStatus());
					cpsTempOrder.setMovieid(order.getMovieid());
					cpsTempOrder.setQuantity(order.getQuantity() == null ? "" : order.getQuantity().toString());
					cpsTempOrder.setUnitprice(order.getUnitprice() == null ? "" : order.getUnitprice().toString());
					mongoService.saveOrUpdateObject(cpsTempOrder, "order_id");
				}
			} catch (Exception e) {
			}
		}
		return result;
	}
}
