package com.gewara.service.gewapay.impl;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.gewara.command.InvoiceCommand;
import com.gewara.constant.AdminCityContant;
import com.gewara.constant.ApiConstant;
import com.gewara.constant.ChargeConstant;
import com.gewara.constant.DramaConstant;
import com.gewara.constant.InvoiceConstant;
import com.gewara.constant.SysAction;
import com.gewara.constant.sys.LogTypeConstant;
import com.gewara.constant.ticket.OrderExtraConstant;
import com.gewara.model.acl.GewaraUser;
import com.gewara.model.acl.User;
import com.gewara.model.pay.BaseOrderExtra;
import com.gewara.model.pay.Charge;
import com.gewara.model.pay.GewaOrder;
import com.gewara.model.pay.OrderExtra;
import com.gewara.model.pay.OrderExtraHis;
import com.gewara.model.user.Invoice;
import com.gewara.model.user.InvoiceRelate;
import com.gewara.model.user.Member;
import com.gewara.service.OrderException;
import com.gewara.service.gewapay.InvoiceService;
import com.gewara.service.impl.BaseServiceImpl;
import com.gewara.support.ErrorCode;
import com.gewara.support.ReadOnlyTemplate;
import com.gewara.untrans.monitor.MonitorService;
import com.gewara.util.BeanUtil;
import com.gewara.util.ChangeEntry;
import com.gewara.util.DateUtil;

@Service("invoiceService")
public class InvoiceServiceImpl extends BaseServiceImpl implements InvoiceService {
	@Autowired@Qualifier("readOnlyTemplate")
	private ReadOnlyTemplate readOnlyTemplate;
	public void setReadOnlyHibernateTemplate(ReadOnlyTemplate readOnlyTemplate) {
		this.readOnlyTemplate = readOnlyTemplate;
	}	
	@Autowired@Qualifier("monitorService")
	private MonitorService monitorService;
	public void setMonitorService(MonitorService monitorService) {
		this.monitorService = monitorService;
	}
	
	private List<Invoice> getInvoiceList(List<Long> invoiceIdList, Long memberid, Integer startAmount, Integer endAmount, 
			String order, boolean isasc, String invoicestatus, String contactor, String citycode, String phone, String applytype, Date fromDate, Date toDate, String pretype, int from, int maxnum){
		DetachedCriteria query = DetachedCriteria.forClass(Invoice.class);
		if(memberid!=null) query.add(Restrictions.eq("memberid", memberid));
		if(invoiceIdList.size()>0) query.add(Restrictions.in("id", invoiceIdList));
		if(fromDate!=null)query.add(Restrictions.ge(order, DateUtil.getBeginningTimeOfDay(fromDate)));
		if(toDate!=null)query.add(Restrictions.le(order, DateUtil.getLastTimeOfDay(toDate)));
		if(StringUtils.isNotBlank(contactor))query.add(Restrictions.eq("contactor", contactor));
		if(startAmount!=null && endAmount!=null){
			query.add(Restrictions.between("amount", startAmount, endAmount));
		}else if(startAmount!=null){
			query.add(Restrictions.ge("amount", startAmount));
		}else if(endAmount!=null){
			query.add(Restrictions.le("amount", endAmount));
		}
		if(StringUtils.isNotBlank(phone)) query.add(Restrictions.eq("phone", phone));
		if(StringUtils.isNotBlank(applytype)) query.add(Restrictions.eq("applytype", applytype));
		if(AdminCityContant.CITYCODE_ALL.equals(citycode)) query.add(Restrictions.ne("citycode", AdminCityContant.CITYCODE_SH));//非上海
		else if(AdminCityContant.CITYCODE_SH.equals(citycode)) query.add(Restrictions.eq("citycode", citycode));//上海
		if(StringUtils.isNotBlank(invoicestatus)){
			if(InvoiceConstant.STATUS_UNPOST.equals(invoicestatus)){//未邮寄
				query.add(Restrictions.in("invoicestatus", Arrays.asList(InvoiceConstant.STATUS_OPENED, InvoiceConstant.STATUS_UNPOST)));
			}else if(InvoiceConstant.STATUS_OPEN.equals(invoicestatus) || InvoiceConstant.STATUS_UNOPEN.equals(invoicestatus)){//按已开票、申请中
				query.add(Restrictions.like("invoicestatus", invoicestatus, MatchMode.START));
				if(invoicestatus.startsWith(InvoiceConstant.STATUS_UNOPEN)) query.add(Restrictions.ne("invoicestatus", InvoiceConstant.STATUS_TRASH));//已废弃
			}else if(StringUtils.equals(invoicestatus, InvoiceConstant.STATUS_OPEN_AGAIN)){//按快递、按平邮、废弃、已补开、申请补开
				query.add(Restrictions.like("invoicestatus", invoicestatus, MatchMode.ANYWHERE));
			}else{
				query.add(Restrictions.eq("invoicestatus", invoicestatus));
			}
		}else {
			query.add(Restrictions.ne("invoicestatus", InvoiceConstant.STATUS_TRASH));//已废弃
		}
		if(StringUtils.isNotBlank(pretype)){
			query.add(Restrictions.eq("pretype", pretype));
		}
		if(StringUtils.isNotBlank(order)){
			query.addOrder(isasc == true ? Order.asc(order) : Order.desc(order));
		}else query.addOrder(Order.desc("addtime"));
		List<Invoice> invoiceList=readOnlyTemplate.findByCriteria(query, from, maxnum);
		return invoiceList;
	}
	private Integer getInvoiceCount(List<Long> invoiceIdList, Long memberid, Integer startAmount, Integer endAmount, 
			String order, String invoicestatus, String contactor, String citycode, String phone, String applytype, Date fromDate, Date toDate, String pretype){
		DetachedCriteria query = DetachedCriteria.forClass(Invoice.class);
		if(memberid!=null) query.add(Restrictions.eq("memberid", memberid));
		if(invoiceIdList.size()>0) query.add(Restrictions.in("id", invoiceIdList));
		if(fromDate!=null)query.add(Restrictions.ge(order, DateUtil.getBeginningTimeOfDay(fromDate)));
		if(toDate!=null)query.add(Restrictions.le(order, DateUtil.getLastTimeOfDay(toDate)));
		if(StringUtils.isNotBlank(contactor))query.add(Restrictions.eq("contactor", contactor));
		if(startAmount!=null && endAmount!=null){
			query.add(Restrictions.between("amount", startAmount, endAmount));
		}else if(startAmount!=null){
			query.add(Restrictions.ge("amount", startAmount));
		}else if(endAmount!=null){
			query.add(Restrictions.le("amount", endAmount));
		}
		if(StringUtils.isNotBlank(phone))query.add(Restrictions.eq("phone", phone));
		if(StringUtils.isNotBlank(applytype)) query.add(Restrictions.eq("applytype", applytype));
		if(AdminCityContant.CITYCODE_ALL.equals(citycode)) query.add(Restrictions.ne("citycode", AdminCityContant.CITYCODE_SH));//非上海
		else if(AdminCityContant.CITYCODE_SH.equals(citycode)) query.add(Restrictions.eq("citycode", citycode));//上海
		if(StringUtils.isNotBlank(invoicestatus)){
			if(InvoiceConstant.STATUS_UNPOST.equals(invoicestatus)){//未邮寄
				query.add(Restrictions.in("invoicestatus", Arrays.asList(InvoiceConstant.STATUS_OPENED, InvoiceConstant.STATUS_UNPOST)));
			}else if(InvoiceConstant.STATUS_OPEN.equals(invoicestatus) || InvoiceConstant.STATUS_UNOPEN.equals(invoicestatus)){//按已开票、申请中
				query.add(Restrictions.like("invoicestatus", invoicestatus, MatchMode.START));
				if(invoicestatus.startsWith(InvoiceConstant.STATUS_UNOPEN)) query.add(Restrictions.ne("invoicestatus", InvoiceConstant.STATUS_TRASH));//已废弃
			}else if(StringUtils.equals(invoicestatus, InvoiceConstant.STATUS_OPEN_AGAIN)){//按快递、按平邮、废弃、已补开、申请补开
				query.add(Restrictions.like("invoicestatus", invoicestatus, MatchMode.ANYWHERE));
			}else{
				query.add(Restrictions.eq("invoicestatus", invoicestatus));
			}
		}else {
			query.add(Restrictions.ne("invoicestatus", InvoiceConstant.STATUS_TRASH));//已废弃
		}
		if(StringUtils.isNotBlank(pretype)){
			query.add(Restrictions.eq("pretype", pretype));
		}
		query.setProjection(Projections.rowCount());
		List<Invoice> invoiceList=readOnlyTemplate.findByCriteria(query);
		if(invoiceList.isEmpty()) return 0;
		return new Integer(invoiceList.get(0)+"");
	}
	@Override
	public List<Invoice> getInvoiceList(Long memberid, String invoiceid, String contactor, String phone, String order, boolean isasc, Date fromDate, Date toDate, String pretype, int from, int maxnum){
		List<Long> invoiceIdList = BeanUtil.getIdList(invoiceid, ",");
		List<Invoice> invoiceList=getInvoiceList(invoiceIdList, memberid, null, null, order, false, "", contactor, "", phone, "", fromDate, toDate, pretype, from, maxnum);
		return invoiceList;
	}
	@Override 
	public List<Invoice> getInvoiceList(Integer startAmount, Integer endAmount, Date fromDate, Date toDate, 
			String citycode, String invoicestatus, String order, boolean isasc, String applytype, String pretype, int from, int maxnum){
		List<Long> invoiceIdList=new ArrayList<Long>();
		List<Invoice> invoiceList=getInvoiceList(invoiceIdList, null, startAmount, endAmount, order, isasc,
				invoicestatus, "", citycode, "", applytype, fromDate, toDate, pretype, from, maxnum);
		return invoiceList;
	}
	@Override
	public Integer getInvoiceCount(Long memberid, String invoiceid, String contactor, String phone, String order, Date fromDate, Date toDate, String pretype){
		List<Long> invoiceIdList = BeanUtil.getIdList(invoiceid, ",");
		Integer invoiceCount = getInvoiceCount(invoiceIdList, memberid, null, null, order, "", contactor, "", phone, "", fromDate, toDate, pretype);
		return invoiceCount;
	}
	@Override 
	public Integer getInvoiceCount(Integer startAmount, Integer endAmount, Date fromDate, Date toDate, 
			String citycode, String invoicestatus, String order, String applytype, String pretype){
		List<Long> invoiceIdList=new ArrayList<Long>();
		Integer invoiceCount=getInvoiceCount(invoiceIdList, null, startAmount, endAmount, order, 
				invoicestatus, "", citycode, "", applytype, fromDate, toDate, pretype);
		return invoiceCount;
	}
	@Override
	public Integer getAllTotalOpenedInvoiceByMemberid(Long memberid){
		DetachedCriteria query = DetachedCriteria.forClass(Invoice.class);
		query.add(Restrictions.eq("memberid", memberid));
		query.add(Restrictions.like("invoicestatus", InvoiceConstant.STATUS_OPEN, MatchMode.START));
		query.setProjection(Projections.sum("amount"));
		List invoiceList = readOnlyTemplate.findByCriteria(query);
		if(invoiceList.get(0)==null) return 0;
		return Integer.parseInt(invoiceList.get(0)+"");
	}
	@Override
	public void mergeInvoice(List<Invoice> invoiceList, GewaraUser user) throws OrderException{
		Set<Invoice> invoiceSet = new HashSet<Invoice>(invoiceList);
		if(invoiceSet.isEmpty()) return;
		String pretype = "";
		Integer totalAmount=0;
		Long memberid = null;
		List<InvoiceRelate> relateList = new ArrayList<InvoiceRelate>();
		Invoice invoice2 = null;
		for(Invoice invoice: invoiceSet){
			if(memberid == null){
				memberid = invoice.getMemberid();
			}else if(invoice.equals(memberid)){
				throw new OrderException(ApiConstant.CODE_SIGN_ERROR, "不同用户发票不能合并！");
			}
			if(StringUtils.isBlank(pretype)){
				pretype = invoice.getPretype();
			}else if(!StringUtils.equals(pretype, invoice.getPretype())){
				throw new OrderException(ApiConstant.CODE_SIGN_ERROR, "开票类别不同，不能合并！");
			}
			if(invoice2 == null){
				invoice2 = invoice;
			}
			List<InvoiceRelate> tmpList = baseDao.getObjectListByField(InvoiceRelate.class, "invoiceid", invoice.getId());
			relateList.addAll(tmpList);
			totalAmount+=invoice.getAmount();
			ChangeEntry changeEntry = new ChangeEntry(invoice);
			List<String> tradoList = BeanUtil.getBeanPropertyList(tmpList, "tradeNo", true);
			invoice.setRelatedid(StringUtils.join(tradoList, ","));
			invoice.setInvoicestatus(InvoiceConstant.STATUS_TRASH);//废弃发票
			baseDao.saveObject(invoice);
			monitorService.saveChangeLog(user.getId(), Invoice.class, invoice.getId(),changeEntry.getChangeMap(invoice));
		}
		Invoice newInvoice = new Invoice(memberid);
		newInvoice.setInvoicestatus(InvoiceConstant.STATUS_APPLY);
		newInvoice.setTitle(invoice2.getTitle());
		newInvoice.setAddress(invoice2.getAddress());
		newInvoice.setCitycode(invoice2.getCitycode());
		newInvoice.setContactor(invoice2.getContactor());
		newInvoice.setInvoicetype(invoice2.getApplytype());
		newInvoice.setPhone(invoice2.getPhone());
		newInvoice.setPostcode(invoice2.getPostcode());
		newInvoice.setPretype(pretype);
		newInvoice.setAmount(totalAmount);
		baseDao.saveObject(newInvoice);
		for (InvoiceRelate invoiceRelate : relateList) {
			Long oldInvoiceId = invoiceRelate.getInvoiceid();
			invoiceRelate.setInvoiceid(newInvoice.getId());
			dbLogger.warnWithType(LogTypeConstant.LOG_TYPE_USERACTION, "userid:" +user.getId()+",订单号：" + invoiceRelate.getTradeNo() + ", invoiceId：" + oldInvoiceId + " ---> " + invoiceRelate.getInvoiceid());
			baseDao.saveObject(invoiceRelate);
		}
	}
	@Override
	public List<String> getOpenedRelatedidList(Long memberid){
		String hql = "select tradeNo from InvoiceRelate r where r.memberid= ? ";
		List<String> idList = hibernateTemplate.find(hql, memberid);
		return idList;
	}
	@Override
	public ErrorCode updateInvoiceTrash(Invoice invoice, User user) {
		if(invoice==null) return ErrorCode.getFailure("数据部存在!");
		if(invoice.getInvoicestatus().startsWith("Y")){
			return ErrorCode.getFailure( "不能废弃已开或已平邮或已快递或未邮寄的发票！");
		}
		ChangeEntry changeEntry = new ChangeEntry(invoice);
		invoice.setInvoicestatus(InvoiceConstant.STATUS_TRASH);//废弃发票
		List<InvoiceRelate> relateList = baseDao.getObjectListByField(InvoiceRelate.class, "invoiceid", invoice.getId());
		List<String> tradeNoList = BeanUtil.getBeanPropertyList(relateList, "tradeNo", true);
		Timestamp cur = DateUtil.getCurFullTimestamp();
		List<BaseOrderExtra> extraList = new ArrayList<BaseOrderExtra>();
		for (String tradeNo : tradeNoList) {
			OrderExtra orderExtra = baseDao.getObjectByUkey(OrderExtra.class, "tradeno", tradeNo);
			BaseOrderExtra  baseOrderExtra = orderExtra;
			if(baseOrderExtra == null){
				OrderExtraHis orderExtraHis = baseDao.getObjectByUkey(OrderExtraHis.class, "tradeno", tradeNo);
				baseOrderExtra = orderExtraHis;
			}
			if(baseOrderExtra != null && StringUtils.equals(baseOrderExtra.getInvoice(), OrderExtraConstant.INVOICE_Y)){
				baseOrderExtra.setInvoice(OrderExtraConstant.INVOICE_N);
				baseOrderExtra.setUpdatetime(cur);
				extraList.add(baseOrderExtra);
			}
		}
		baseDao.saveObjectList(extraList);
		baseDao.removeObjectList(relateList);
		baseDao.saveObject(invoice);
		monitorService.saveChangeLog(user.getId(), Invoice.class, invoice.getId(),changeEntry.getChangeMap( invoice));
		return ErrorCode.SUCCESS;
	}
	
	@Override
	public Map<BaseOrderExtra,GewaOrder> validDataTradeNo(Long memberid, Set<String> tradeNoSet, Timestamp cur, final boolean checkValidtime) throws OrderException{
		Timestamp validtime = DateUtil.addDay(DateUtil.getBeginningTimeOfDay(cur), -180);
		Map<BaseOrderExtra, GewaOrder> extraMap = new Hashtable<BaseOrderExtra,GewaOrder>();
		for (String tradeNo : tradeNoSet) {
			OrderExtra orderExtra= baseDao.getObjectByUkey(OrderExtra.class, "tradeno", tradeNo, true);
			BaseOrderExtra baseExtra = orderExtra;
			if(baseExtra == null && !checkValidtime){
				OrderExtraHis orderExtraHis= baseDao.getObjectByUkey(OrderExtraHis.class, "tradeno", tradeNo, true);
				baseExtra = orderExtraHis;
			}
			if(baseExtra == null) continue;
			if(StringUtils.equals(baseExtra.getInvoice(), OrderExtraConstant.INVOICE_Y)){
				throw new OrderException(ApiConstant.CODE_SIGN_ERROR, "订单号："+baseExtra.getTradeno()+ "已申请发票，不能重复申请！");
			}
			if(StringUtils.equals(baseExtra.getInvoice(), OrderExtraConstant.INVOICE_F)){
				throw new OrderException(ApiConstant.CODE_SIGN_ERROR, "订单号："+baseExtra.getTradeno()+ "不能申请发票！");
			}
			GewaOrder gewaOrder = baseDao.getObjectByUkey(GewaOrder.class, "tradeNo", tradeNo, true);
			if(gewaOrder == null){
				throw new OrderException(ApiConstant.CODE_SIGN_ERROR, "订单号" + tradeNo + "不存在，不能申请发票！");
			}
			if(!gewaOrder.isPaidSuccess()) throw new OrderException(ApiConstant.CODE_SIGN_ERROR, gewaOrder.getTradeNo() + "非成功订单号，不能申请发票！");
			if(!gewaOrder.getMemberid().equals(memberid)){
				throw new OrderException(ApiConstant.CODE_SIGN_ERROR, "非法申请发票！");
			}
			if(checkValidtime && gewaOrder.getPaidtime().before(validtime)){
				throw new OrderException(ApiConstant.CODE_SIGN_ERROR, "订单号："+gewaOrder.getTradeNo()+"已过期，不能申请发票！");
			}
			extraMap.put(baseExtra, gewaOrder);
		}
		return extraMap;
	}
	
	@Override
	public List<Charge> validDataCharge(Long memberid, Set<String> chargeNoSet, Timestamp cur, final boolean checkValidtime) throws OrderException{
		Timestamp validtime = DateUtil.addDay(DateUtil.getBeginningTimeOfDay(cur), -180);
		List<Charge> chargeList = new ArrayList<Charge>();
		for (String tradeNo : chargeNoSet) {
			Charge charge = baseDao.getObjectByUkey(Charge.class, "tradeNo", tradeNo, true);
			if(charge == null){
				throw new OrderException(ApiConstant.CODE_SIGN_ERROR, "订单号" + tradeNo + "不存在，不能申请发票！");
			}
			if(!charge.getMemberid().equals(memberid)){
				throw new OrderException(ApiConstant.CODE_SIGN_ERROR, "非法申请发票！");
			}
			if(!charge.hasChargeto(ChargeConstant.WABIPAY)){
				throw new OrderException(ApiConstant.CODE_SIGN_ERROR, "充值订单号："+charge.getTradeNo()+"非瓦币充值，不能申请发票！");
			}
			if(checkValidtime && charge.getUpdatetime().before(validtime)){
				throw new OrderException(ApiConstant.CODE_SIGN_ERROR, "充值订单号："+charge.getTradeNo()+"已过期，不能申请发票！");
			}
			if(charge.isCanInvoice()){
				chargeList.add(charge);
			}
		}
		return chargeList;
	}
	private Integer recevieInvoice(InvoiceCommand invoiceCommand, User user) throws OrderException{
		if(StringUtils.isBlank(invoiceCommand.getOrderidList())){
			throw new OrderException(ApiConstant.CODE_SIGN_ERROR,"申请发票，订单号不能为空！");
		}
		List<String> tradeNoList = Arrays.asList(StringUtils.split(invoiceCommand.getOrderidList(), ","));
		if(CollectionUtils.isEmpty(tradeNoList)){
			throw new OrderException(ApiConstant.CODE_SIGN_ERROR,"请选择要开发票的订单！");
		}
		Set<String> tradeNoSet = new HashSet<String>(tradeNoList);
		List<InvoiceRelate> appliedList = baseDao.getObjectList(InvoiceRelate.class, tradeNoSet);
		if(!appliedList.isEmpty()){
			throw new OrderException(ApiConstant.CODE_SIGN_ERROR, "申请发票错误，已申请发票的订单号为：" + StringUtils.join(BeanUtil.getBeanPropertyList(appliedList, "tradeNo", true), ","));
		}
		if(invoiceCommand.getMemberid() == null){
			throw new OrderException(ApiConstant.CODE_SIGN_ERROR, "申请人不能为空！");
		}
		final boolean checkValidtime = (user == null);
		Timestamp cur = DateUtil.getCurFullTimestamp();
		Map<BaseOrderExtra,GewaOrder> extraMap = validDataTradeNo(invoiceCommand.getMemberid(), tradeNoSet, cur, checkValidtime);
		Set<BaseOrderExtra> extraList = extraMap.keySet();
		List<String> tradeList = BeanUtil.getBeanPropertyList(extraList, "tradeno", true);
		Set<String> chargeNoSet = new HashSet<String>(tradeNoSet);
		chargeNoSet.removeAll(tradeList);
		List<Charge> chargeList = validDataCharge(invoiceCommand.getMemberid(), chargeNoSet, cur, checkValidtime);
		Map<String, List<BaseOrderExtra>> orderExtraMap = BeanUtil.groupBeanList(extraList, "pretype");
		final String applytype = checkValidtime ? SysAction.APPLY_TYPE_SELFSERVICE : SysAction.APPLY_TYPE_CUSTOMSERVICE;
		int totalAmount = 0;
		boolean validCharge = true;		//是否计算过充值, true 未计算， false 已计算
		for (String pretype : orderExtraMap.keySet()) {
			Map<String/*tradeNo*/,Long/*orderid*/> tradeNoMap = new Hashtable<String, Long>();
			List<BaseOrderExtra> orderExtreaList = orderExtraMap.get(pretype);
			int orderAmount = 0;
			for (BaseOrderExtra baseExtra : orderExtreaList) {
				GewaOrder gewaOrder = extraMap.get(baseExtra);
				orderAmount += gewaOrder.getAlipaid() + gewaOrder.getGewapaid() - gewaOrder.getWabi();
				baseExtra.setInvoice(OrderExtraConstant.INVOICE_Y);
				baseExtra.setUpdatetime(cur);
				tradeNoMap.put(baseExtra.getTradeno(), gewaOrder.getId());
			}
			//pretype == 'M' Gewara 自营
			if(StringUtils.equals(pretype, DramaConstant.PRETYPE_MANAGE)){
				validCharge = false;
				for (Charge charge : chargeList) {
					orderAmount += charge.getTotalfee();
					tradeNoMap.put(charge.getTradeNo(), charge.getId());
				}
			}
			totalAmount += orderAmount;
			if(orderAmount > 0 ){
				Invoice invoice = new Invoice(invoiceCommand.getMemberid(), invoiceCommand.getAddress(), orderAmount, invoiceCommand.getTitle(), invoiceCommand.getPhone(), 
						invoiceCommand.getPostcode(), invoiceCommand.getContactor(), invoiceCommand.getInvoicetype(), StringUtils.join(tradeNoMap.keySet(),","));
				ChangeEntry changeEntry = new ChangeEntry(invoice);
				invoice.setCitycode(invoiceCommand.getCitycode());
				invoice.setPretype(pretype);
				if(!checkValidtime){
					invoice.setAdminid(user.getId());
				}
				invoice.setApplytype(applytype);
				baseDao.saveObject(invoice);
				try{
					List<InvoiceRelate> relateList = addInvoiceRelate(invoice, tradeNoMap);
					baseDao.saveObjectList(relateList);
				}catch(Exception e){
					dbLogger.warn("", e);
					throw new OrderException(ApiConstant.CODE_SIGN_ERROR, "申请发票错误！");
				}
				baseDao.saveObjectList(orderExtreaList);
				dbLogger.warn("invoice_orderExtra:" + orderExtreaList.size());
				if(!checkValidtime){
					monitorService.saveChangeLog(user.getId(), Invoice.class, invoice.getId(),changeEntry.getChangeMap(invoice));
				}
			}
		}
		//未计算充值
		if(validCharge){
			Map<String/*tradeNo*/,Long/*orderid*/> tradeNoMap = new Hashtable<String, Long>();
			int orderAmount = 0;
			for (Charge charge : chargeList) {
				orderAmount += charge.getTotalfee();
				tradeNoMap.put(charge.getTradeNo(), charge.getId());
			}
			if(orderAmount > 0 ){
				Invoice invoice = new Invoice(invoiceCommand.getMemberid(), invoiceCommand.getAddress(), orderAmount, invoiceCommand.getTitle(), invoiceCommand.getPhone(), 
						invoiceCommand.getPostcode(), invoiceCommand.getContactor(), invoiceCommand.getInvoicetype(), StringUtils.join(tradeNoMap.keySet(),","));
				ChangeEntry changeEntry = new ChangeEntry(invoice);
				invoice.setCitycode(invoiceCommand.getCitycode());
				invoice.setPretype(DramaConstant.PRETYPE_MANAGE);
				if(!checkValidtime){
					invoice.setAdminid(user.getId());
				}
				invoice.setApplytype(applytype);
				baseDao.saveObject(invoice);
				try{
					List<InvoiceRelate> relateList = addInvoiceRelate(invoice, tradeNoMap);
					baseDao.saveObjectList(relateList);
				}catch(Exception e){
					dbLogger.warn("", e);
					throw new OrderException(ApiConstant.CODE_SIGN_ERROR, "申请发票错误！");
				}
				if(!checkValidtime){
					monitorService.saveChangeLog(user.getId(), Invoice.class, invoice.getId(),changeEntry.getChangeMap(invoice));
				}
			}
			totalAmount += orderAmount;
		}
		return totalAmount;
	}
	
	@Override
	public ErrorCode receiveInvoice(Member member, InvoiceCommand invoiceCommand) throws OrderException {
		if(StringUtils.isBlank(invoiceCommand.getOrderidList())){
			return ErrorCode.getFailure("申请发票，订单号不能为空！");
		}
		List<String> tradeNoList = Arrays.asList(StringUtils.split(invoiceCommand.getOrderidList(), ","));
		if(CollectionUtils.isEmpty(tradeNoList)){
			return ErrorCode.getFailure("请选择要开发票的订单！");
		}
		Set<String> tradeNoSet = new HashSet<String>(tradeNoList);
		List<InvoiceRelate> appliedList = baseDao.getObjectList(InvoiceRelate.class, tradeNoSet);
		if(!appliedList.isEmpty()){
			return ErrorCode.getFailure("申请发票金额错误或部分订单已经申请！");
		}
		invoiceCommand.setMemberid(member.getId());
		int amount = recevieInvoice(invoiceCommand, null);
		if(amount<=0) return ErrorCode.getFailure("申请发票金额必须大于零！");
		if(amount<100)	return ErrorCode.getFailure("申请发票金额不能小于100元！");
		return ErrorCode.SUCCESS;
	}
	
	@Override
	public ErrorCode receiveInvoice(User user, InvoiceCommand invoiceCommand) throws OrderException{
		if(user == null) return ErrorCode.getFailure("请先登录！");
		if(invoiceCommand.getMemberid() == null) return ErrorCode.getFailure("用户或商户ID不能为空！");
		int amount = recevieInvoice(invoiceCommand, user);
		if(amount<=0) return ErrorCode.getFailure("申请发票金额必须大于零！");
		return ErrorCode.SUCCESS;
	}
	
	private List<InvoiceRelate> addInvoiceRelate(Invoice invoice, Map<String,Long> tradeNoMap){
		List<InvoiceRelate> relateList = new ArrayList<InvoiceRelate>();
		for (String tradeNo : tradeNoMap.keySet()) {
			InvoiceRelate invoiceRelate = new InvoiceRelate(tradeNo);
			invoiceRelate.setOrderid(tradeNoMap.get(tradeNo));
			invoiceRelate.setMemberid(invoice.getMemberid());
			invoiceRelate.setInvoiceid(invoice.getId());
			if(!relateList.contains(invoiceRelate)){
				relateList.add(invoiceRelate);
			}
		}
		return relateList;
	}
	
}