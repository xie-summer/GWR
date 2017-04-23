package com.gewara.web.action.home;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import com.gewara.command.InvoiceCommand;
import com.gewara.constant.ChargeConstant;
import com.gewara.constant.PaymethodConstant;
import com.gewara.constant.ticket.OrderConstant;
import com.gewara.constant.ticket.OrderExtraConstant;
import com.gewara.model.common.City;
import com.gewara.model.common.Province;
import com.gewara.model.pay.Charge;
import com.gewara.model.pay.GewaOrder;
import com.gewara.model.pay.MemberAccount;
import com.gewara.model.pay.OrderExtra;
import com.gewara.model.user.Invoice;
import com.gewara.model.user.Member;
import com.gewara.service.OrderException;
import com.gewara.service.PlaceService;
import com.gewara.service.gewapay.InvoiceService;
import com.gewara.service.gewapay.PaymentService;
import com.gewara.support.ErrorCode;
import com.gewara.util.BeanUtil;
import com.gewara.util.DateUtil;
import com.gewara.util.ValidateUtil;
import com.gewara.web.action.BaseHomeController;
import com.gewara.web.util.PageUtil;

@Controller
public class InvoiceController extends BaseHomeController {
	@Autowired@Qualifier("paymentService")
	private PaymentService paymentService;
	public void setPaymentService(PaymentService paymentService) {
		this.paymentService = paymentService;
	}
	@Autowired@Qualifier("invoiceService")
	private InvoiceService invoiceService;
	public void setInvoiceService(InvoiceService invoiceService) {
		this.invoiceService = invoiceService;
	}
	@Autowired@Qualifier("placeService")
	private PlaceService placeService;
	public void setPlaceService(PlaceService placeService) {
		this.placeService = placeService;
	}
	@RequestMapping("/home/invoice/receiveInvoice.xhtml")
	public String receiveInvoice(ModelMap model){
		Member member = getLogonMember();
		MemberAccount memberAccount=daoService.getObjectByUkey(MemberAccount.class, "memberid", member.getId(), false);
		model.put("memberAccount", memberAccount);
		model.putAll(controllerService.getCommonData(model, member, member.getId()));
		
		return "home/invoice/invoice.vm";
	}
	@RequestMapping("/home/invoice/getReceiveInvoice.xhtml")
	public String getReceiveInvoice(ModelMap model){
		Member member = getLogonMember();
		//已开发票金额
		Integer openAmount = invoiceService.getAllTotalOpenedInvoiceByMemberid(member.getId());
		//未开发票金额
		Integer applyAmount=0;
		//所有订单
		String sql = "from GewaOrder g where g.status=? and g.memberid=? and g.addtime>=? " +
				"and exists(select o.id from OrderExtra o where o.memberid=? and o.invoice=? and o.addtime>=? and o.status=? and o.id=g.id ) order by addtime desc";
		Timestamp cur = DateUtil.getCurTruncTimestamp();
		Timestamp qtime = DateUtil.addDay(cur, - 180);
		List<GewaOrder> orderList = daoService.queryByRowsRange(sql, 0, 1000, OrderConstant.STATUS_PAID_SUCCESS, member.getId(), qtime, member.getId(), OrderExtraConstant.INVOICE_N, qtime, OrderConstant.STATUS_PAID_SUCCESS);
		List<String> appliedList = invoiceService.getOpenedRelatedidList(member.getId());
		List<GewaOrder> newOrderList = new ArrayList<GewaOrder>();
		List<Charge> newChargeList = new ArrayList<Charge>();
		List<Long> orderIdList = new ArrayList<Long>();
		for(GewaOrder order: orderList){
			if(!appliedList.contains(order.getTradeNo()) && !StringUtils.startsWith(order.getPaymethod(), PaymethodConstant.PAYMETHOD_UMPAY)){
				int due = order.getDue();
				if(StringUtils.equals(order.getPaymethod(), PaymethodConstant.PAYMETHOD_GEWAPAY)) {
					due = order.getGewapaid() - order.getWabi();
				}
				if(due>0) {
					applyAmount += due;
					newOrderList.add(order);
				}
				orderIdList.add(order.getId());
			}
		}
		//TODO:查询全部Charge？？有效期？？
		List<Charge> chargeList = paymentService.getChargeList(member.getId(), Charge.STATUS_PAID, ChargeConstant.WABIPAY);
		for(Charge charge : chargeList){
			if(!appliedList.contains(charge.getTradeNo()) && charge.isCanInvoice()){
				newChargeList.add(charge);
			}
		}
		Map<Long, OrderExtra> extraMap = daoService.getObjectMap(OrderExtra.class, orderIdList);
		model.put("extraMap", extraMap);
		model.put("orderList", newOrderList);
		model.put("chargeList", newChargeList);
		model.put("appliedList", appliedList);
		model.put("openAmount", openAmount);
		model.put("applyAmount", applyAmount);
		return "home/invoice/showReceiveInvoice.vm";
	}
	
	@RequestMapping("/home/invoice/saveReceiveInvoice.xhtml")
	public String saveReceiveInvoice(InvoiceCommand invoiceCommand, ModelMap model){
		Member member = getLogonMember();
		if(StringUtils.isBlank(invoiceCommand.getTitle()))
			return showJsonError(model, "发票抬头不能为空！");
		if(StringUtils.isBlank(invoiceCommand.getContactor()))
			return showJsonError(model, "收件人不能为空！");
		if(StringUtils.isBlank(invoiceCommand.getPhone()))
			return showJsonError(model, "手机号码不能为空！");
		if(StringUtils.isBlank(invoiceCommand.getCitycode()))
			return showJsonError(model, "邮寄城市不能为空！");
		if(StringUtils.isBlank(invoiceCommand.getAddress()))
			return showJsonError(model, "邮寄地址不能为空！");
		if(StringUtils.isBlank(invoiceCommand.getPostcode()))
			return showJsonError(model, "邮政编码不能为空！");
		if(!ValidateUtil.isMobile(invoiceCommand.getPhone())) return showJsonError(model, "您输入的手机号码格式不正确!");
		if(!ValidateUtil.isPostCode(invoiceCommand.getPostcode())) return showJsonError(model,"您输入的邮政编码格式不正确！");
		try{
			ErrorCode code = invoiceService.receiveInvoice(member, invoiceCommand);
			if(!code.isSuccess()){
				return showJsonError(model, code.getMsg());
			}
			return showJsonSuccess(model);
		}catch (OrderException e) {
			return showJsonError(model, e.getMsg());
		}
	}
	@RequestMapping("/home/invoice/invoiceList.xhtml")
	public String invoiceList(ModelMap model, Date fromDate, Date toDate, String order, 
			Integer pageNo){
		Member member = getLogonMember();
		model.putAll(controllerService.getCommonData(model, member, member.getId()));
		if(StringUtils.isBlank(order))order = "addtime";
		if (pageNo == null) pageNo = 0;
		int rowsPerPage = 15;
		int first = rowsPerPage * pageNo;
		Integer invoiceCount = invoiceService.getInvoiceCount(member.getId(), "", "", "", order, fromDate, toDate, null);
		List<Invoice> invoiceList = invoiceService.getInvoiceList(member.getId(), "", "", "", order, false, fromDate, toDate, null, first, rowsPerPage);
		PageUtil pageUtil = new PageUtil(invoiceCount, rowsPerPage, pageNo, "home/invoice/invoiceList.xhtml", true, true);
		Map params = new HashMap();
		params.put("order", order);
		params.put("fromDate", DateUtil.formatDate(fromDate));
		params.put("toDate", DateUtil.formatDate(toDate));
		pageUtil.initPageInfo(params);
		model.put("invoiceList", invoiceList);
		model.put("pageUtil", pageUtil);
		return "home/invoice/invoiceList.vm";
	}
	@RequestMapping("/home/invoice/invoiceInfo.xhtml")
	public String invoiceInfo(Long invoiceid, ModelMap model){
		Member member = getLogonMember();
		model.putAll(controllerService.getCommonData(model, member, member.getId()));
		Invoice invoice = daoService.getObject(Invoice.class, invoiceid);
		model.put("invoice", invoice);
		model.put("city", daoService.getObject(City.class, invoice.getCitycode()));
		return "home/invoice/invoiceInfo.vm";
	}
	@RequestMapping("/home/invocie/ajaxLoadAddress.xhtml")
	public String ajaxLoadAddress(String tag, String provincecode, String agtag, ModelMap model){
		if(StringUtils.isBlank(tag)){
			List<Province> list = placeService.getAllProvinces();
			List<Map> provinceMap = BeanUtil.getBeanMapList(list, "provincecode", "provincename");
			model.put("provinceMap", provinceMap);
		}else if(StringUtils.equals(tag, "province")){
			List<City> list = placeService.getCityByProvinceCode(provincecode);
			List<Map> cityMap = BeanUtil.getBeanMapList(list, "citycode", "cityname");
			model.put("cityMap", cityMap);
		}
		model.put("agtag", agtag);
		return "home/invoice/locationAddress.vm";
	}
	
}