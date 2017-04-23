package com.gewara.web.action.admin.sport;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import com.gewara.constant.PaymethodConstant;
import com.gewara.constant.ticket.OpiConstant;
import com.gewara.model.pay.ElecCardBatch;
import com.gewara.model.pay.PayBank;
import com.gewara.model.sport.OpenTimeItem;
import com.gewara.model.sport.OpenTimeTable;
import com.gewara.model.sport.Sport;
import com.gewara.service.gewapay.PaymentService;
import com.gewara.util.BindUtils;
import com.gewara.util.JsonUtils;
import com.gewara.util.VmUtils;
import com.gewara.web.action.admin.BaseAdminController;

@Controller
public class SportOtiAdminController extends BaseAdminController {
	@Autowired@Qualifier("paymentService")
	private PaymentService paymentService;
	//场地详细
	@RequestMapping("/admin/sport/open/otiForm.xhtml")
	public String otiDetail(Long otiid, ModelMap model) {
		OpenTimeItem item = daoService.getObject(OpenTimeItem.class, otiid);
		Sport cursport = daoService.getObject(Sport.class, item.getSportid());
		OpenTimeTable table = daoService.getObject(OpenTimeTable.class, item.getOttid());
		List<PayBank> bankList = paymentService.getPayBankList(PayBank.TYPE_PC);
		model.put("item", item);
		model.put("cursport", cursport);
		model.put("table", table);
		model.put("otherinfo", table.getOtherinfo());
		model.put("confPayList", bankList);
		model.put("payTextMap", PaymethodConstant.getPayTextMap());
		return "admin/sport/open/otiForm.vm";
	}
	
	//保存场地
	@RequestMapping("/admin/sport/open/saveOti.xhtml")
	public String saveOti(Long otiid, String payoption, String paymethodlist, String defaultpaymethod, 
			String cardoption, String batchidlist, HttpServletRequest request, ModelMap model) {
		OpenTimeItem oti = daoService.getObject(OpenTimeItem.class, otiid);
		BindUtils.bindData(oti, request.getParameterMap());
		daoService.saveObject(oti);
		oti = daoService.getObject(OpenTimeItem.class, otiid);
		Map<String, String> otherinfo = VmUtils.readJsonToMap(oti.getOtherinfo());
		if(StringUtils.equals(payoption, "del")) {
			otherinfo.remove(OpiConstant.PAYOPTION);
			otherinfo.remove(OpiConstant.PAYCMETHODLIST);
			otherinfo.remove(OpiConstant.DEFAULTPAYMETHOD);
		}else if(StringUtils.isNotBlank(payoption)){
			otherinfo.put(OpiConstant.PAYOPTION, payoption);
			if(StringUtils.isNotBlank(paymethodlist)) { 
				if(StringUtils.isBlank(defaultpaymethod)) return showJsonError(model, "请选择默认支付方式");
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
		
		oti.setOtherinfo(JsonUtils.writeMapToJson(otherinfo));
		daoService.saveObject(oti);
		return showJsonSuccess(model);
	}
	
	//设置场地开放状态
	@RequestMapping("/admin/sport/open/setOTIStatus.xhtml")
	public String setOTIPrice(Long otiid, String status, ModelMap model) {
		OpenTimeItem item = daoService.getObject(OpenTimeItem.class, otiid);
		if(item == null) return showJsonError(model, "该场地不存在或被删除！");
		if(StringUtils.equals(status, "A")){
			if(item.hasZeroPrice()) {
				return showJsonError(model, "该场地价格为0，请核实后开放！");
			}
		}
		item.setStatus(status);
		daoService.saveObject(item);
		return showJsonSuccess(model);
	}
	
	//保存场地优惠券
	@RequestMapping("/admin/sport/open/setOtiElecard.xhtml")
	public String setElecard(Long otiid, String elecard, ModelMap model) {
		OpenTimeItem item = daoService.getObject(OpenTimeItem.class, otiid);
		item.setElecard(elecard);
		daoService.saveObject(item);
		return showJsonSuccess(model);
	}
}
