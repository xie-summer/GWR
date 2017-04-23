package com.gewara.web.action.admin.sport;

import java.sql.Timestamp;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.collections.map.HashedMap;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import com.gewara.constant.PaymethodConstant;
import com.gewara.constant.ticket.OpiConstant;
import com.gewara.model.acl.User;
import com.gewara.model.pay.ElecCardBatch;
import com.gewara.model.pay.PayBank;
import com.gewara.model.sport.Guarantee;
import com.gewara.service.gewapay.PaymentService;
import com.gewara.service.sport.GuaranteeService;
import com.gewara.util.BindUtils;
import com.gewara.util.ChangeEntry;
import com.gewara.util.DateUtil;
import com.gewara.util.JsonUtils;
import com.gewara.util.VmUtils;
import com.gewara.util.WebUtils;
import com.gewara.web.action.admin.BaseAdminController;
import com.gewara.web.util.PageUtil;

@Controller
public class GuaranteeAdminController extends BaseAdminController {
	
	@Autowired
	private GuaranteeService guaranteeService;
	
	@Autowired
	private PaymentService paymentService;
	
	@RequestMapping("/admin/guarantee/guaranteeList.xhtml")
	public String guaranteeList(Integer pageNo, Timestamp starttime, Timestamp endtime, String status, HttpServletRequest request, ModelMap model){
		if(pageNo == null) pageNo =0;
		int rowsPrePage = 30;
		int firstPre = pageNo * rowsPrePage;
		String citycode = getAdminCitycode(request);
		int count = guaranteeService.getGuaranteeCount(citycode, starttime, endtime, status);
		if(count>0){
			List<Guarantee> guaranteeList = guaranteeService.getGuaranteeList(citycode, starttime, endtime, status, null, false, firstPre, rowsPrePage);
			model.put("guaranteeList", guaranteeList);
		}
		PageUtil pageUtil = new PageUtil(count, rowsPrePage, pageNo, "admin/guarantee/guaranteeList.xhtml");
		Map params = new HashedMap();
		params.put("starttime", DateUtil.formatTimestamp(starttime));
		params.put("endtime", DateUtil.formatTimestamp(endtime));
		params.put("status", status);
		pageUtil.initPageInfo(params);
		model.put("pageUtil", pageUtil);
		model.put("starttime", starttime);
		model.put("endtime", endtime);
		return "admin/guarantee/guaranteeList.vm";
	}
	
	@RequestMapping("/admin/guarantee/getGuarantee.xhtml")
	public String getGuarantee(Long id, ModelMap model){
		Guarantee guarantee = daoService.getObject(Guarantee.class, id);
		model.put("guarantee", guarantee);
		return "admin/guarantee/guaranteeDetail.vm";
	}
	
	@RequestMapping("/admin/guarantee/saveGuarantee.xhtml")
	public String saveGuarantee(Long id, Integer price, HttpServletRequest request, ModelMap model){
		User user = getLogonUser();
		Guarantee guarantee = null;
		if(id != null){
			guarantee = daoService.getObject(Guarantee.class, id);
			if(guarantee == null) return showJsonError_NOT_FOUND(model);
			guarantee.setUpdatetime(DateUtil.getCurFullTimestamp());
		}else{
			String citycode = getAdminCitycode(request);
			guarantee = new Guarantee(price);
			guarantee.setCreateuser(user.getId());
			guarantee.setCitycode(citycode);
		}
		ChangeEntry changeEntry = new ChangeEntry(guarantee);
		Map<String,String> dataMap = WebUtils.getRequestMap(request);
		BindUtils.bind(guarantee, dataMap, false, Guarantee.disallowBindField);
		if(price == null) return showJsonError(model, "价格不能为空！");
		daoService.saveObject(guarantee);
		monitorService.saveChangeLog(user.getId(), Guarantee.class, guarantee.getId(), changeEntry.getChangeMap(guarantee));
		return showJsonSuccess(model, "" + guarantee.getId());
	}
	
	@RequestMapping("/admin/guarantee/getGuaranteeOther.xhtml")
	public String getGuaranteeOther(Long id, ModelMap model){
		Guarantee guarantee = daoService.getObject(Guarantee.class, id);
		List<PayBank> bankList = paymentService.getPayBankList(PayBank.TYPE_PC);
		model.put("guarantee", guarantee);
		model.put("otherinfo", guarantee.getOtherinfo());
		model.put("confPayList", bankList);
		model.put("payTextMap", PaymethodConstant.getPayTextMap());
		return "admin/guarantee/guaranteeOther.vm";
	}
	
	@RequestMapping("/admin/guarantee/saveGuaranteeOther.xhtml")
	public String saveOpiOther(Long id, String payoption, String paymethodlist, String defaultpaymethod, 
			String cardoption, String batchidlist, HttpServletRequest request, ModelMap model) {
		Guarantee guarantee = daoService.getObject(Guarantee.class, id);
		BindUtils.bind(guarantee, request.getParameterMap(), false, Guarantee.disallowBindField);
		daoService.saveObject(guarantee);
		guarantee = daoService.getObject(Guarantee.class, id);
		Map<String, String> otherinfo = VmUtils.readJsonToMap(guarantee.getOtherinfo());
		if(StringUtils.equals(paymethodlist, ",")) paymethodlist = "";
		if(StringUtils.equals(payoption, "del")) {
			otherinfo.remove(OpiConstant.PAYOPTION);
			otherinfo.remove(OpiConstant.PAYCMETHODLIST);
			otherinfo.remove(OpiConstant.DEFAULTPAYMETHOD);
		}else if(StringUtils.isNotBlank(payoption)){
			otherinfo.put(OpiConstant.PAYOPTION, payoption);
			if(StringUtils.isNotBlank(paymethodlist)) { 
				paymethodlist = checkpaymethodlist(paymethodlist);
				List<String> paymethodList = Arrays.asList(StringUtils.split(paymethodlist, ","));
				if(StringUtils.isBlank(defaultpaymethod) && paymethodList.size()!=1) return showJsonError(model, "请选择默认支付方式");
				
				otherinfo.put(OpiConstant.DEFAULTPAYMETHOD, defaultpaymethod);
				otherinfo.put(OpiConstant.PAYCMETHODLIST, paymethodlist);
			}else {
				otherinfo.remove(OpiConstant.DEFAULTPAYMETHOD);
				otherinfo.remove(OpiConstant.PAYCMETHODLIST);
			}
			if(StringUtils.equals(payoption, "notuse") && StringUtils.isBlank(paymethodlist)){
				return showJsonError(model, "支付方式选择不可用，必须勾选支付方式！");
			}
		}
		if(StringUtils.equals(cardoption, "del")) {
			otherinfo.remove(OpiConstant.CARDOPTION);
			otherinfo.remove(OpiConstant.BATCHIDLIST);
		}else if(StringUtils.isNotBlank(cardoption) && StringUtils.isNotBlank(batchidlist)){
			String[] batchidList = StringUtils.split(batchidlist, ",");
			for(String batchid : batchidList){
				ElecCardBatch batch = daoService.getObject(ElecCardBatch.class, new Long(batchid));
				if(batch==null) return showJsonError(model, batchid+"对应的批次不存在！");
			}
			otherinfo.put(OpiConstant.CARDOPTION, cardoption);
			otherinfo.put(OpiConstant.BATCHIDLIST, batchidlist);
		}
		guarantee.setOtherinfo(JsonUtils.writeMapToJson(otherinfo));
		daoService.saveObject(guarantee);
		return showJsonSuccess(model);
	}
	private String checkpaymethodlist(String paymethodlist){
		if(StringUtils.equals(paymethodlist, ",")) paymethodlist = "";
		return VmUtils.printList(Arrays.asList(StringUtils.split(paymethodlist, ",")));
	}
	@RequestMapping("/admin/guarantee/removeGuaranteeOther.xhtml")
	public String removeOpiOther(Long id, String payoption, String cardoption, ModelMap model) {
		Guarantee guarantee = daoService.getObject(Guarantee.class, id);
		ChangeEntry changeEntry = new ChangeEntry(guarantee);
		Map<String, String> otherinfo = VmUtils.readJsonToMap(guarantee.getOtherinfo());
		if(StringUtils.isNotBlank(payoption)) {
			otherinfo.remove(OpiConstant.PAYOPTION);
			otherinfo.remove(OpiConstant.PAYCMETHODLIST);
			otherinfo.remove(OpiConstant.DEFAULTPAYMETHOD);
		}
		if(StringUtils.isNotBlank(cardoption)) {
			otherinfo.remove(OpiConstant.CARDOPTION);
			otherinfo.remove(OpiConstant.BATCHIDLIST);
		}
		guarantee.setOtherinfo(JsonUtils.writeMapToJson(otherinfo));
		daoService.saveObject(guarantee);
		monitorService.saveChangeLog(getLogonUser().getId(), Guarantee.class, guarantee.getId(), changeEntry.getChangeMap(guarantee));
		return showJsonSuccess(model);
	}
}
