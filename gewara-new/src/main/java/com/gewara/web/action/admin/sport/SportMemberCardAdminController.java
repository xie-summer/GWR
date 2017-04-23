package com.gewara.web.action.admin.sport;

import java.sql.Timestamp;
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

import com.gewara.command.SearchOrderCommand;
import com.gewara.constant.MemberCardConstant;
import com.gewara.constant.PaymethodConstant;
import com.gewara.constant.Status;
import com.gewara.constant.TagConstant;
import com.gewara.constant.ticket.OpiConstant;
import com.gewara.constant.ticket.OrderConstant;
import com.gewara.model.common.JsonData;
import com.gewara.model.pay.ElecCardBatch;
import com.gewara.model.pay.MemberCardOrder;
import com.gewara.model.pay.PayBank;
import com.gewara.model.sport.MemberCardType;
import com.gewara.model.sport.Sport;
import com.gewara.service.gewapay.PaymentService;
import com.gewara.service.sport.MemberCardService;
import com.gewara.support.ErrorCode;
import com.gewara.untrans.sport.RemoteMemberCardService;
import com.gewara.untrans.ticket.OrderProcessService;
import com.gewara.util.BeanUtil;
import com.gewara.util.BindUtils;
import com.gewara.util.DateUtil;
import com.gewara.util.JsonUtils;
import com.gewara.util.VmUtils;
import com.gewara.web.action.admin.BaseAdminController;
import com.gewara.xmlbind.sport.RemoteMemberCardType;

@Controller
public class SportMemberCardAdminController  extends BaseAdminController{
	@Autowired@Qualifier("remoteMemberCardService")
	private RemoteMemberCardService remoteMemberCardService;
	
	@Autowired@Qualifier("memberCardService")
	private MemberCardService memberCardService;
	@Autowired@Qualifier("paymentService")
	private PaymentService paymentService;
	
	@Autowired@Qualifier("orderProcessService")
	private OrderProcessService orderProcessService;
	
	@RequestMapping("/admin/sport/open/memberCard/memberCardTypeList.xhtml")
	public String memberCardTypeList(Long sportid, ModelMap model){
		Sport sport = daoService.getObject(Sport.class, sportid);
		List<MemberCardType> mctList = memberCardService.getMemberCardTypeListBySportids(sportid, false);
		Map<Long, String> fitItemMap = new HashMap<Long, String>();
		for(MemberCardType mct : mctList){
			String name = memberCardService.getFitItem(mct.getFitItem());
			fitItemMap.put(mct.getId(), name);
		}
		model.put("mctList", mctList);
		model.put("fitItemMap", fitItemMap);
		model.put("cursport", sport);
		model.put("isMemberCardOrder", true);
		return "admin/sport/membercard/memberCardList.vm";
	}
	
	@RequestMapping("/admin/sport/open/memberCard/synchMemberCardTypeList.xhtml")
	public String synchMemberCardTypeList(Long sportid, ModelMap model){
		ErrorCode<List<RemoteMemberCardType>> mctcode = remoteMemberCardService.getRemoteMemberCardTypeListBySportid(sportid+"");
		if(!mctcode.isSuccess()){
			return showJsonError(model, mctcode.getMsg());
		}
		List<MemberCardType> mctList = memberCardService.synchMemberCardOrderList(mctcode.getRetval());
		return showJsonSuccess(model, "保存数据：" + mctList.size());
	}
	//设置每个会员卡开放状态
	@RequestMapping("/admin/sport/open/memberCard/setMctStatus.xhtml")
	public String setOTIPrice(Long id, String status, ModelMap model) {
		MemberCardType mct = daoService.getObject(MemberCardType.class, id);
		if(StringUtils.equals(status, Status.Y)){
			if(mct.getGewaprice()==null || mct.getGewaprice()<=0) {
				return showJsonError(model, "请设置卖价");
			}
			if(mct.getCostRate()==null || mct.getCostRate()<=0){
				return showJsonError(model, "请设置结算比率");
			}
			if(mct.getMingain()==null || mct.getMingain()<=0){
				return showJsonError(model, "请设置保底金");
			}
		}
		mct.setStatus(status);
		daoService.saveObject(mct);
		return showJsonSuccess(model);
	}
		
	//设置会员卡开放预定时间
	@RequestMapping("/admin/sport/open/memberCard/setMctOpentime.xhtml")
	public String setOTTOpentime(Long id, Timestamp opentime, ModelMap model) {
		MemberCardType mct = daoService.getObject(MemberCardType.class, id);
		mct.setOpentime(opentime);
		daoService.saveObject(mct);
		return showJsonSuccess(model);
	}
	//设置会员卡结束预定时间
	@RequestMapping("/admin/sport/open/memberCard/setMctCloseime.xhtml")
	public String setOTTCloseime(Long id, Timestamp closetime, ModelMap model) {
		MemberCardType mct = daoService.getObject(MemberCardType.class, id);
		mct.setClosetime(closetime);
		daoService.saveObject(mct);
		return showJsonSuccess(model);
	}
	
	@RequestMapping("/admin/sport/open/setMctElecard.xhtml")
	public String setElecard(Long id, String elecard, ModelMap model) {
		MemberCardType mct = daoService.getObject(MemberCardType.class, id);
		mct.setElecard(elecard);
		daoService.saveObject(mct);
		return showJsonSuccess(model);
	}
	//批量保存会员卡优惠券
	@RequestMapping("/admin/sport/open/memberCard/setAllMctElecard.xhtml")
	public String setAllElecard(String ids, String elecard, ModelMap model) {
		if(StringUtils.isBlank(ids)) return showJsonError(model, "该场馆没有场次！");
		String elecardType = "ABCDM";
		char[] chars = elecard.toCharArray();
		for(char c : chars){
			if(!StringUtils.contains(elecardType, c)){
				return showJsonError(model, "优惠券类型错误！");
			}
		}
		List<Long> idList = BeanUtil.getIdList(ids, ",");
		List<MemberCardType> mctList = daoService.getObjectList(MemberCardType.class, idList);
		for(MemberCardType mct : mctList){
			mct.setElecard(elecard);
		}
		daoService.saveObjectList(mctList);
		return showJsonSuccess(model);
	}
	
	@RequestMapping("/admin/sport/open/memberCard/getMct.xhtml")
	public String getMct(Long id, ModelMap model) {
		MemberCardType mct = daoService.getObject(MemberCardType.class, id);
		List<PayBank> bankList = paymentService.getPayBankList(PayBank.TYPE_PC);
		model.put("otherinfo", mct.getOtherinfo());
		model.put("confPayList", bankList);
		model.put("mct", mct);
		model.put("payTextMap", PaymethodConstant.getPayTextMap());
		model.put("isMemberCardOrder", true);
		return "admin/sport/membercard/mctForm.vm";
	}
	@RequestMapping("/admin/sport/open/memberCard/failOrder.xhtml")
	public String failOrder(String tradeNo, ModelMap model) {
		MemberCardOrder order = daoService.getObjectByUkey(MemberCardOrder.class, "tradeNo", tradeNo);
		if(!StringUtils.startsWith(order.getStatus(), OrderConstant.STATUS_PAID_FAILURE)) return showJsonError(model, "非待处理的订单，不能确认！");
		ErrorCode result = orderProcessService.processOrder(order, "重新确认", null);
		return forwardMessage(model, result.getMsg());
	}
	//保存场次
	@RequestMapping("/admin/sport/open/memberCard/saveMct.xhtml")
	public String saveOtt(Long id, String payoption, String paymethodlist, String defaultpaymethod, 
			String cardoption, String batchidlist, String address, String unopengewa, HttpServletRequest request, ModelMap model) {
		MemberCardType mct = daoService.getObject(MemberCardType.class, id);
		BindUtils.bindData(mct, request.getParameterMap());
		daoService.saveObject(mct);
		if(StringUtils.equals(paymethodlist, ",")) paymethodlist = "";
		Map<String, String> otherinfo = VmUtils.readJsonToMap(mct.getOtherinfo());
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
		if(StringUtils.isBlank(address)){
			otherinfo.remove(OpiConstant.ADDRESS);
		}else{
			otherinfo.put(OpiConstant.ADDRESS, address);
		}
		if(StringUtils.isBlank(unopengewa)){
			otherinfo.remove(OpiConstant.UNOPENGEWA);
		}else{
			otherinfo.put(OpiConstant.UNOPENGEWA, unopengewa);
		}
		mct.setOtherinfo(JsonUtils.writeMapToJson(otherinfo));
		daoService.saveObject(mct);
		return showJsonSuccess(model);
	}
	@RequestMapping("/admin/sport/open/memberCard/orderList.xhtml")
	public String orderList(SearchOrderCommand soc, ModelMap model) {
		Sport sport = daoService.getObject(Sport.class, soc.getSportid());
		List<MemberCardOrder> orderList = memberCardService.getMemberCardOrderList(soc);
		model.put("orderList", orderList);
		model.put("cursport", sport);
		model.put("isMemberCardOrder", true);
		return "admin/sport/membercard/orderList.vm";
	}
	
	@RequestMapping("/admin/sport/open/memberCard/msg.xhtml")
	public String msg(String content, ModelMap model) {
		String vm = "admin/sport/membercard/smsContent.vm";
		JsonData template = daoService.getObject(JsonData.class, MemberCardConstant.KEY_MEMBERCARDMSG);
		model.put("template", template);
		if(StringUtils.isBlank(content)){
			return vm;
		}
		if(template==null){
			template = new JsonData(MemberCardConstant.KEY_MEMBERCARDMSG);
			template.setTag(TagConstant.TAG_MEMBERCARD);
			template.setValidtime(DateUtil.addDay(DateUtil.getCurFullTimestamp(), 360*2));
		}
		template.setData(content);
		daoService.saveObject(template);
		return vm;
	}
}
