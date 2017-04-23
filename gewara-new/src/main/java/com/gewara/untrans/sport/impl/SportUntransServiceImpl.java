package com.gewara.untrans.sport.impl;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.stereotype.Service;

import com.gewara.constant.ApiConstant;
import com.gewara.constant.OpenTimeItemConstant;
import com.gewara.constant.OpenTimeTableConstant;
import com.gewara.constant.sys.ErrorCodeConstant;
import com.gewara.constant.ticket.OrderConstant;
import com.gewara.model.api.ApiUser;
import com.gewara.model.pay.GewaOrder;
import com.gewara.model.pay.GoodsOrder;
import com.gewara.model.pay.SMSRecord;
import com.gewara.model.pay.SportOrder;
import com.gewara.model.sport.CusOrder;
import com.gewara.model.sport.OpenTimeItem;
import com.gewara.model.sport.OpenTimeSale;
import com.gewara.model.sport.OpenTimeTable;
import com.gewara.model.sport.SellDeposit;
import com.gewara.model.sport.SellTimeTable;
import com.gewara.model.user.Member;
import com.gewara.model.user.MemberInfo;
import com.gewara.pay.PayUtil;
import com.gewara.service.OrderException;
import com.gewara.service.sport.GuaranteeOrderService;
import com.gewara.service.sport.OpenTimeSaleService;
import com.gewara.service.sport.SportOrderService;
import com.gewara.support.ErrorCode;
import com.gewara.untrans.UntransService;
import com.gewara.untrans.impl.AbstractUntrantsService;
import com.gewara.untrans.sport.RemoteSportService;
import com.gewara.untrans.sport.SportUntransService;
import com.gewara.util.BeanUtil;
import com.gewara.util.DateUtil;
import com.gewara.util.StringUtil;
import com.gewara.xmlbind.sport.RemoteMemberCardInfo;

@Service("sportUntransService")
public class SportUntransServiceImpl extends AbstractUntrantsService implements SportUntransService {
	
	@Autowired
	private OpenTimeSaleService openTimeSaleService;
	
	@Autowired@Qualifier("untransService")
	private UntransService untransService;

	@Autowired
	private RemoteSportService remoteSportService;
	
	@Autowired
	private SportOrderService sportOrderService;
	
	@Autowired
	private GuaranteeOrderService guaranteeOrderService;
	
	@Autowired
	private HibernateTemplate hibernateTemplate;
	
	@Override
	public ErrorCode<Map> openTimeSaleJoin(Long otsid, Member member, Integer price, String jointype){
		if(price == null) return ErrorCode.getFailure(ApiConstant.CODE_SIGN_ERROR, "竞拍价格不能为空！");
		Timestamp addtime = DateUtil.getCurFullTimestamp();
		OpenTimeSale ots = daoService.getObject(OpenTimeSale.class, otsid);
		OpenTimeTable ott = daoService.getObject(OpenTimeTable.class, ots.getOttid());
		if(ott == null || !ott.isBooking()) return ErrorCode.getFailure(ApiConstant.CODE_SIGN_ERROR, "场次不存在或未开放！");
		SellDeposit deposit = guaranteeOrderService.getSellDeposit(ots.getId(), member.getId(), SellDeposit.STATUS_PAID_SUCCESS);
		if(deposit == null){
			return ErrorCode.getFullErrorCode(ApiConstant.CODE_SIGN_ERROR, "当前竞价场次未付保证金！", OpenTimeTableConstant.GUARANTEE_UNPAY);
		}
		if(ots.hasLockStatus(OpenTimeTableConstant.SALE_STATUS_UNLOCK)){
			List<OpenTimeItem> itemList = daoService.getObjectList(OpenTimeItem.class, BeanUtil.getIdList(ots.getOtiids(), ","));
			List<Long> remoteIdList = BeanUtil.getBeanPropertyList(itemList, "rotiid", true);
			ErrorCode<String> code = remoteSportService.lockOrder(ott, remoteIdList, OpenTimeTableConstant.ITEM_TYPE_VIE);
			if(!code.isSuccess()) return ErrorCode.getFailure(code.getErrcode(), code.getMsg());
			for (int i = 0; i< 3; i ++) {
				try{
					ots.setLockStatus(OpenTimeTableConstant.SALE_STATUS_LOCK);
					daoService.saveObject(ots);
					break;
				}catch(Throwable e){
					//非乐观锁异常
					if(!isUpdateErrorException(e)){
						dbLogger.warn(StringUtil.getExceptionTrace(e, 10));					
						return ErrorCode.getFailure("竞价失败！");
					}else{
						dbLogger.warn("SALE_STATUS_LOCK:" + ots.getId() + ":" + i + "," + StringUtil.getExceptionTrace(e, 3));
					}
				}
			}
			if(!ots.hasLockStatus(OpenTimeTableConstant.SALE_STATUS_LOCK)) return ErrorCode.getFailure("竞价失败！");
			daoService.saveObjectList(itemList);
			dbLogger.warnWithType("SALE_STATUS_LOCK", BeanUtil.buildString(ots, true));
		}
		for(int i = 0; i< 3; i ++){
			try{
				return openTimeSaleService.joinOpenTimeSale(ots.getId(), deposit, member, price, addtime);
			}catch(Throwable e){
				//非乐观锁异常
				if(e instanceof OrderException){
					OrderException e1 = (OrderException)e;
					dbLogger.warn(e1.getCode() +","+ e1.getMsg());
					return ErrorCode.getFailure("竞价失败！");
				}else if(!isUpdateErrorException(e)){
					dbLogger.warn(StringUtil.getExceptionTrace(e, 10));					
					return ErrorCode.getFailure("竞价失败！");
				}else{
					dbLogger.warn("openTimeSaleJoin:" + ots.getId() + ":" + i + "," + StringUtil.getExceptionTrace(e, 3));
				}
			}
		}
		return ErrorCode.getFailure(ApiConstant.CODE_SIGN_ERROR, "竞价失败！");
	}

	@Override
	public ErrorCode<Map> openTimeSaleCountdown(Long otsid) {
		OpenTimeSale ots = daoService.getObject(OpenTimeSale.class, otsid);
		if(ots == null || !ots.hasBooking()){
			return ErrorCode.getFailure(ApiConstant.CODE_SIGN_ERROR, "竞价场次未开始或以过期！");
		}
		boolean isJoin = false;
		Map jsonMap = new HashMap<String, Object>();
		//Long cur = System.currentTimeMillis();
		if(ots.hasSuccess()){
			jsonMap.put("memberid", ots.getMemberid());
			jsonMap.put("headpic", daoService.getObjectProperty(MemberInfo.class, ots.getMemberid(), "headpic"));
		}else{
			isJoin = ots.hasBooking();
			if(ots.hasClose() && ots.hasLockStatus(OpenTimeTableConstant.SALE_STATUS_LOCK)){
				ots.setLockStatus(OpenTimeTableConstant.SALE_STATUS_SUCCESS);
			}
			if(ots.hasLockStatus(OpenTimeTableConstant.SALE_STATUS_SUCCESS)){
				try {
					ErrorCode<SportOrder> code = sportOrderService.addSportOrder(ots);
					if(code.isSuccess()){
						SportOrder order = code.getRetval();
						SMSRecord sms = openTimeSaleService.sendMessage(order, ots);
						if(sms!=null){
							untransService.sendMsgAtServer(sms, false);
						}
					}
					dbLogger.warnWithType("openTimeSaleCountdown", BeanUtil.buildString(ots, true));
				} catch (OrderException e) {
					dbLogger.warn("openTimeSaleCountdown", e);
				}
			}
		}
		
		jsonMap.put("isJoin", isJoin);
		jsonMap.put("curprice", ots.getCurprice());
		jsonMap.put("nickname", ots.getNickname());
		return ErrorCode.getSuccessReturn(jsonMap);
	}
	
	@Override
	public void cancelSportOrder(SportOrder order, Long memberid, String reason){
		if(order.isNew() && order.getMemberid().equals(memberid)){
			OpenTimeTable ott = daoService.getObject(OpenTimeTable.class, order.getOttid());
			if(ott.hasRemoteOtt()){
				if(ott.hasField()){
					List<OpenTimeItem> otiList = sportOrderService.getMyOtiList(order.getId());
					List<Long> remoteIdList = BeanUtil.getBeanPropertyList(otiList, "rotiid", true);
					ErrorCode<String> retCode = remoteSportService.unLockOrder(ott, remoteIdList);
					if(!retCode.isSuccess()) dbLogger.warn("api取消未支付的场地有错误！");
				}
			}
			sportOrderService.cancelSportOrder(order, memberid, reason);
			dbLogger.warn("取消未支付订单：" + order.getTradeNo() + "," + reason);
		}
	}
	
	private ErrorCode processLastOrder(Long memberid, String ukey){
		try{
			ErrorCode<SportOrder> lastOrder = sportOrderService.processLastOrder(memberid, ukey);
			if(!lastOrder.isSuccess()){
				return ErrorCode.getFailure(ApiConstant.CODE_SIGN_ERROR, lastOrder.getMsg());
			}
			SportOrder order = lastOrder.getRetval();
			if(order != null){
				if(order.isNew()){
					cancelSportOrder(order, order.getMemberid(), "重复订单");
				}
			}
			return ErrorCode.SUCCESS;
		}catch(Exception e){
			dbLogger.warn(StringUtil.getExceptionTrace(e, 5));
		}
		return ErrorCodeConstant.DATEERROR;
	}
	
	@Override
	public ErrorCode<SportOrder> addSportOrder(Long otiid, String starttime, Integer time, Integer quantity, String mobile, Member member){
		return addSportOrder(otiid, starttime, time, quantity, mobile, member, null);
	}
	@Override
	public ErrorCode<SportOrder> addSportOrder(Long otiid, String starttime, Integer time, Integer quantity, String mobile, Member member, ApiUser partner){
		//非场地场次不需要远程锁定
		ErrorCode code = processLastOrder(member.getId(), String.valueOf(member.getId()));
		if(!code.isSuccess()) return ErrorCode.getFailure(code.getErrcode(), code.getMsg());
		ErrorCode<SportOrder> codeOrder = sportOrderService.addSportOrder(otiid, starttime, time, quantity, mobile, member, partner);
		return codeOrder;
	}
	
	@Override
	public ErrorCode<SportOrder> addSportOrder(OpenTimeTable ott, String fields, Long cardid, ErrorCode<RemoteMemberCardInfo> rmcode, String mobile, Member member) throws OrderException {
		return addSportOrder(ott, fields, cardid, rmcode, mobile, member, null);
	}
	
	@Override
	public ErrorCode<SportOrder> addSportOrder(OpenTimeTable ott, String fields, Long cardid, ErrorCode<RemoteMemberCardInfo> rmcode, String mobile, Member member, ApiUser partner){
		ErrorCode code = processLastOrder(member.getId(), String.valueOf(member.getId()));
		if(!code.isSuccess()) return ErrorCode.getFailure(code.getErrcode(), code.getMsg());
		try {
			ErrorCode<SportOrder> codeOrder = sportOrderService.addSportOrder(ott, fields, cardid, rmcode, mobile, member, partner);
			if(!codeOrder.isSuccess()) return codeOrder;
			if(ott.hasRemoteOtt()){
				List<OpenTimeItem> itemList = daoService.getObjectList(OpenTimeItem.class, BeanUtil.getIdList(fields, ","));
				List<Long> remoteIdList = BeanUtil.getBeanPropertyList(itemList, "rotiid", true);
				ErrorCode<String> codeRemote = remoteSportService.lockOrder(ott, remoteIdList, OpenTimeTableConstant.ITEM_TYPE_COM);
				if(!codeRemote.isSuccess()){
					cancelSportOrder(codeOrder.getRetval(), member.getId(), "远程锁定失败取消订单");
					return ErrorCode.getFailure(codeRemote.getErrcode(), codeRemote.getMsg());
				}
			}
			return codeOrder;
		} catch (OrderException e) {
			return ErrorCode.getFailure(e.getCode(), e.getMessage());
		}
		
	}

	@Override
	public ErrorCode<SportOrder> addSportOrder(Long otiid, String starttime, Integer quantity, String mobile, Member member){
		return addSportOrder(otiid, starttime, quantity, mobile, member, null);
	}
	
	@Override
	public ErrorCode<SportOrder> addSportOrder(Long otiid, String starttime, Integer quantity, String mobile, Member member, ApiUser parnter) {
		//非场地场次不需要远程锁定
		ErrorCode code = processLastOrder(member.getId(), String.valueOf(member.getId()));
		if(!code.isSuccess()) return ErrorCode.getFailure(code.getErrcode(), code.getMsg());
		ErrorCode<SportOrder> codeOrder = sportOrderService.addSportOrder(otiid, starttime, quantity, mobile, member, parnter);
		return codeOrder;
	}
	
	@Override
	public ErrorCode processSportOrder(SportOrder order, OpenTimeTable ott, List<OpenTimeItem> otiList) throws OrderException{
		if(ott.hasRemoteOtt()){
			ErrorCode<String> code = updateCuOrder(order, ott);
			if (!code.isSuccess()) {
				dbLogger.warn(order.getTradeNo() + "传递订单失败：" + code.getMsg());
				return code;
			}
		}
		sportOrderService.processSportOrder(order, ott, otiList);
		return ErrorCode.SUCCESS;
	}
	
	@Override
	public ErrorCode<String> updateCuOrder(GewaOrder order, OpenTimeTable ott) {
		String msg = "";
		if(PayUtil.isSportTrade(order.getTradeNo())){
			if(ott==null){
				SportOrder sOrder = (SportOrder)order;
				ott = daoService.getObject(OpenTimeTable.class, sOrder.getOttid());
			}
			msg = "确认运动订单失败：";
		}else if(PayUtil.isGoodsTrade(order.getTradeNo())){
			msg = "确认运动物品订单失败：";
		}else return ErrorCode.getFailure("订单类型不是运动或物品订单");
		if(ott.hasRemoteOtt()){
			CusOrder cusOrder = daoService.getObjectByUkey(CusOrder.class, "orderid", order.getId(), false);
			if(!StringUtils.equals(cusOrder.getStatus(), OrderConstant.STATUS_PAID_SUCCESS)){
				ErrorCode<String> code = fixOrder(order, ott);
				if(!code.isSuccess()) {
					dbLogger.warn(msg + order.getTradeNo()+", 原因：" + code.getMsg());
					if(cusOrder!=null){
						cusOrder.setResponse(code.getMsg());
						daoService.saveObject(cusOrder);
					}
					return ErrorCode.getFailure(msg + code.getMsg());
				}else{
					cusOrder.setStatus(OrderConstant.STATUS_PAID_SUCCESS);
					cusOrder.setResponse("success");
					daoService.saveObject(cusOrder);
				}
			}
		}
		return ErrorCode.SUCCESS;
	}
	
	@Override
	public ErrorCode<String> fixOrder(GewaOrder order, OpenTimeTable ott) {
		if(!PayUtil.isSportTrade(order.getTradeNo())&&!PayUtil.isGoodsTrade(order.getTradeNo()))
			return ErrorCode.getFailure("订单类型不是运动或物品订单");
		Map model = new HashMap();
		if(order instanceof  GoodsOrder){
			return ErrorCode.SUCCESS;
		}else if(order instanceof  SportOrder){
			SportOrder sorder = (SportOrder)order;
			model.put("sportid", sorder.getSportid());
		}else {
			return ErrorCode.getFailure("订单类型错误！"); 
		}
		List<OpenTimeItem> otiList = sportOrderService.getMyOtiList(order.getId());
		List<Long> remoteIdList = new ArrayList<Long>();
		if(ott.hasField()){
			remoteIdList = BeanUtil.getBeanPropertyList(otiList, Long.class, "rotiid", true);
		}else{
			SellTimeTable sellTimeTable = daoService.getObject(SellTimeTable.class, order.getId());
			if(sellTimeTable != null){
				OpenTimeItem item = daoService.getObject(OpenTimeItem.class, sellTimeTable.getOtiid());
				remoteIdList.add(item.getRotiid());
			}
		}
		return remoteSportService.fixOrder((SportOrder)order, ott, remoteIdList, null, null);
	}
	@Override
	public int sysLockOti(OpenTimeTable ott) {
		ErrorCode<List<Long>> code = remoteSportService.getRemoteLockItem(ott);
		int i = 0;
		if(!code.isSuccess()) return i;
		List<Long> idList = code.getRetval();
		List<OpenTimeItem> otiList = new ArrayList<OpenTimeItem>();
		for(Long id : idList){
			OpenTimeItem oti = getOpenTimeItem(ott, id);
			if(oti!=null && oti.hasStatusNew()){
				oti.setStatus(OpenTimeItemConstant.STATUS_LOCKR);
				otiList.add(oti);
				i++;
			}
		}
		daoService.saveObjectList(otiList);
		return i;
	}
	private OpenTimeItem getOpenTimeItem(OpenTimeTable ott, Long rotiid){
		DetachedCriteria query = DetachedCriteria.forClass(OpenTimeItem.class);
		query.add(Restrictions.eq("ottid", ott.getId()));
		query.add(Restrictions.eq("rotiid", rotiid));
		List<OpenTimeItem> itemList = hibernateTemplate.findByCriteria(query, 0, 1);
		if(itemList.isEmpty())return null;
		return itemList.get(0);
	}
}
