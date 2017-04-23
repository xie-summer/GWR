package com.gewara.web.action.sport;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.gewara.constant.ChargeConstant;
import com.gewara.constant.OpenTimeTableConstant;
import com.gewara.constant.sys.CacheConstant;
import com.gewara.model.pay.Charge;
import com.gewara.model.pay.SportOrder;
import com.gewara.model.sport.OpenTimeSale;
import com.gewara.model.sport.OpenTimeSaleMember;
import com.gewara.model.sport.OpenTimeTable;
import com.gewara.model.sport.SellDeposit;
import com.gewara.model.sport.Sport;
import com.gewara.model.sport.SportField;
import com.gewara.model.sport.SportItem;
import com.gewara.model.user.Member;
import com.gewara.service.gewapay.PaymentService;
import com.gewara.service.sport.GuaranteeOrderService;
import com.gewara.service.sport.OpenTimeSaleService;
import com.gewara.support.ErrorCode;
import com.gewara.support.MultiPropertyComparator;
import com.gewara.untrans.CacheService;
import com.gewara.untrans.impl.ControllerService;
import com.gewara.untrans.sport.SportUntransService;
import com.gewara.util.BeanUtil;
import com.gewara.util.DateUtil;
import com.gewara.util.WebUtils;
import com.gewara.web.action.AnnotationController;

@Controller
public class OpenTimeSaleController extends AnnotationController {

	@Autowired
	private OpenTimeSaleService openTimeSaleService;
	
	@Autowired
	private GuaranteeOrderService guaranteeOrderService;
	
	@Autowired
	private ControllerService controllerService;
	
	@Autowired
	private PaymentService paymentService;
	
	@Autowired
	private CacheService cacheService;
	
	@Autowired@Qualifier("sportUntransService")
	private SportUntransService sportUntransService;
	
	
	@RequestMapping("/sport/open/getOpenTimeSale.xhtml")
	public String openTimeSale(@CookieValue(value=LOGIN_COOKIE_NAME, required=false)String sessid, HttpServletRequest request, HttpServletResponse response, Long otsid, ModelMap model){
		Member member = loginService.getLogonMemberBySessid(WebUtils.getRemoteIp(request), sessid);
		if(member == null){
			return gotoLogin("/sport/open/getOpenTimeSale.xhtml", request, model);
		}
		model.put("logonMember", member);
		OpenTimeSale curOts = daoService.getObject(OpenTimeSale.class, otsid);
		if(curOts == null) return showMessageAndReturn(model, request, "竞价场次不存在！");
		OpenTimeTable ott = daoService.getObject(OpenTimeTable.class, curOts.getOttid());
		if(ott == null || !ott.isBooking()) return showMessageAndReturn(model, request, "场次没有开放或过期！");
		model.put("curOts", curOts);
		if(curOts.hasSuccess()){
			
		}
		String citycode = WebUtils.getAndSetDefault(request, response);
		List<OpenTimeSale> saleList = openTimeSaleService.getJoinOtsList(citycode, member.getId(), true, "addtime", true, 0, 10);
		Collections.sort(saleList, new MultiPropertyComparator(new String[]{"playdate","starttime"}, new boolean[]{true, true}));
		int joincount = 0, pocesscount = 0;
		if(!saleList.contains(curOts)){
			saleList.add(0,curOts);
		}
		Map<Long,SportOrder> orderMap = new HashMap<Long,SportOrder>();
		model.put("orderMap", orderMap);
		for (OpenTimeSale openTimeSale : saleList) {
			if(openTimeSale.hasLockStatus(OpenTimeTableConstant.SALE_STATUS_SUCCESS)){
				SportOrder order = daoService.getObject(SportOrder.class, openTimeSale.getOrderid());
				orderMap.put(openTimeSale.getId(), order);
				pocesscount ++;
			}else if(openTimeSale.hasBooking()){
				joincount ++;
			}
		}
		model.put("joincount", joincount);
		model.put("pocesscount", pocesscount);
		List<Long> memberidList = BeanUtil.getBeanPropertyList(saleList, "memberid", true);
		model.put("saleList", saleList);
		List<OpenTimeSale> historySaleList = openTimeSaleService.getJoinOtsList(citycode, member.getId(), false, "addtime", false, 0, -1);
		memberidList.addAll(BeanUtil.getBeanPropertyList(historySaleList, Long.class, "memberid", true));
		model.put("historySaleList", historySaleList);
		List<OpenTimeSale> otsList = new ArrayList<OpenTimeSale>();
		otsList.addAll(saleList);
		otsList.addAll(historySaleList);
		openTimeSaleInfo(otsList, model);
		model.put("curOts", curOts);
		List<Long> otsIdList = openTimeSaleService.getOtsIdListMemberJoin(member.getId());
		model.put("otsIdList", otsIdList);
		SellDeposit deposit = guaranteeOrderService.getSellDeposit(otsid, member.getId(), SellDeposit.STATUS_PAID_SUCCESS);
		model.put("deposit", deposit);
		joinOtsMember(curOts, null, memberidList, model);
		return "sport/itemsale/wide_sportBid.vm";
	}
	private void openTimeSaleInfo(OpenTimeSale ots, ModelMap model){
		List<OpenTimeSale> otsList = new ArrayList<OpenTimeSale>();
		otsList.add(ots);
		openTimeSaleInfo(otsList, model);
	}
	
	private void openTimeSaleInfo(List<OpenTimeSale> otsList, ModelMap model){
		List<Long> sportIdList = new ArrayList<Long>();
		List<Long> itemIdList = new ArrayList<Long>();
		List<Long> fieldIdList = new ArrayList<Long>();
		for (OpenTimeSale ots : otsList) {
			if(!sportIdList.contains(ots.getSportid())){
				sportIdList.add(ots.getSportid());
			}
			if(!itemIdList.contains(ots.getItemid())){
				itemIdList.add(ots.getItemid());
			}
			if(!fieldIdList.contains(ots.getFieldid())){
				fieldIdList.add(ots.getFieldid());
			}
		}
		Map<Long,Sport> sportMap = daoService.getObjectMap(Sport.class, sportIdList);
		model.put("sportMap", sportMap);
		Map<Long,SportItem> itemMap = daoService.getObjectMap(SportItem.class, itemIdList);
		model.put("itemMap", itemMap);
		Map<Long,SportField> fieldMap = daoService.getObjectMap(SportField.class, fieldIdList);
		model.put("fieldMap", fieldMap);
	}
	
	private void joinOtsMember(OpenTimeSale ots, Long memberid, List<Long> memberidList, ModelMap model){
		Timestamp curtime = DateUtil.getCurFullTimestamp();
		Integer count = openTimeSaleService.getOtsMemberCount(ots.getId(), DateUtil.addMinute(curtime, -30));
		boolean joinMember = openTimeSaleService.getOtsMemberCount(ots.getId(), memberid) > 0;
		List<OpenTimeSaleMember> otsMemberList = openTimeSaleService.getOtsMemberList(ots.getId(), memberid, 0, 6);
		memberidList.addAll(BeanUtil.getBeanPropertyList(otsMemberList, Long.class, "memberid", true));
		addCacheMember(model, memberidList);
		model.put("count", count);
		model.put("joinMember", joinMember);
		model.put("otsMemberList", otsMemberList);
	}
	
	@RequestMapping("/sport/open/getOtsDetail.xhtml")
	public String getOtsDetail(@CookieValue(value=LOGIN_COOKIE_NAME, required=false)String sessid, HttpServletRequest request, Long otsid, ModelMap model){
		Member member = loginService.getLogonMemberBySessid(WebUtils.getRemoteIp(request), sessid);
		if(member == null) return showJsonError(model, "请先登录！");
		model.put("logonMember", member);
		OpenTimeSale curOts = daoService.getObject(OpenTimeSale.class, otsid);
		if(curOts == null) return showJsonError(model, "竞价场次不存在！");
		OpenTimeTable ott = daoService.getObject(OpenTimeTable.class, curOts.getOttid());
		if(ott == null || !ott.isBooking()) return showJsonError(model, "场次没有开放或过期！");
		model.put("curOts", curOts);
		if(curOts.getMemberid() != null){
			addCacheMember(model, curOts.getMemberid());
		}
		SellDeposit deposit = guaranteeOrderService.getSellDeposit(otsid, member.getId(), SellDeposit.STATUS_PAID_SUCCESS);
		model.put("deposit", deposit);
		openTimeSaleInfo(curOts, model);
		return "sport/itemsale/wide_otsDetail.vm";
	}
	
	@RequestMapping("/sport/open/joinOtsMember.xhtml")
	public String joinOtsMember(@CookieValue(value=LOGIN_COOKIE_NAME, required=false)String sessid, HttpServletRequest request, Long otsid, String jointype, ModelMap model){
		Member member = null;
		Long memberid = null;
		if(StringUtils.equals(jointype, "member")){
			member = loginService.getLogonMemberBySessid(WebUtils.getRemoteIp(request), sessid);
			if(member == null) return showJsonError_NOT_LOGIN(model);
			memberid = member.getId();
		}
		OpenTimeSale curOts = daoService.getObject(OpenTimeSale.class, otsid);
		if(curOts == null) return showJsonError(model, "竞价场次不存在！");
		List<Long> memberidList = new ArrayList<Long>();
		model.put("curOts", curOts);
		joinOtsMember(curOts, memberid, memberidList, model);
		addCacheMember(model, memberidList);
		return "sport/itemsale/wide_otsMember.vm";
	}
	
	@RequestMapping("/sport/open/time.xhtml")
	@ResponseBody
	public String timeOpen(Long id){
		String key = OpenTimeTableConstant.KEY_OPENTIMESALE_CLOSETIME_ + id;
		Long valid = (Long) cacheService.get(CacheConstant.REGION_HALFHOUR, key);
		if(valid==null){
			OpenTimeSale ots = daoService.getObject(OpenTimeSale.class, id);
			if(ots != null){
				valid = ots.getClosetime().getTime();
				cacheService.set(CacheConstant.REGION_HALFHOUR, key, valid);
			}
		}
		if(valid!=null){
			Long cur = System.currentTimeMillis();
			Long remain = valid - cur;
			return ""+remain;
		}
		return "";
	}
	
	@RequestMapping("/sport/open/join.xhtml")
	public String countdown(@CookieValue(value=LOGIN_COOKIE_NAME, required=false)String sessid, HttpServletRequest request, Long otsid, Integer price, String jointype, ModelMap model) {
		Member member = loginService.getLogonMemberBySessid(WebUtils.getRemoteIp(request), sessid);
		if(member == null) return showJsonError(model, "请先登录！");
		OpenTimeSale ots = daoService.getObject(OpenTimeSale.class, otsid);
		if(ots == null) return showJsonError_NOT_FOUND(model);
		if(!ots.hasBooking()){
			Map jsonMap = new HashMap();
			jsonMap.put("isJoin", false);
			jsonMap.put("msg", "竞价场次已结束！");
			return showJsonError(model, jsonMap);
		}
		ErrorCode<Map> code = sportUntransService.openTimeSaleJoin(otsid, member, price, jointype);
		if(!code.isSuccess()){
			Map jsonMap = new HashMap();
			jsonMap.put("isJoin", true);
			jsonMap.put("curprice", price);
			jsonMap.put("msg", code.getMsg());
			return showJsonError(model, jsonMap);
		}
		return showJsonSuccess(model, code.getRetval());
	}
	
	@RequestMapping("/sport/open/countdown.xhtml")
	public String countdown(@CookieValue(value=LOGIN_COOKIE_NAME, required=false)String sessid, HttpServletRequest request, Long otsid, ModelMap model) {
		Member member = loginService.getLogonMemberBySessid(WebUtils.getRemoteIp(request), sessid);
		if(member == null) return showJsonError(model, "请先登录！");
		ErrorCode<Map> code = sportUntransService.openTimeSaleCountdown(otsid);
		if(!code.isSuccess()) return showJsonError(model, code.getMsg());
		return showJsonSuccess(model, code.getRetval());
	}
	

	@RequestMapping("/sport/open/payDepositCharge.xhtml")
	public String payDepositCharge(@CookieValue(value=LOGIN_COOKIE_NAME, required=false)String sessid, HttpServletRequest request, Long otsid, String captchaId, String captcha, String mobile, ModelMap model){
		String ip = WebUtils.getRemoteIp(request);
		boolean isValidCaptcha = controllerService.validateCaptcha(captchaId, captcha, ip);
		if(!isValidCaptcha) return showJsonError(model, "验证码错误！");
		Member member = loginService.getLogonMemberBySessid(ip, sessid);
		if(member == null) return showJsonError(model, "请先登录！");
		OpenTimeSale ots = daoService.getObject(OpenTimeSale.class, otsid);
		if(ots == null || !ots.hasBooking()) return showJsonError_NOT_FOUND(model);
		ErrorCode<Charge> code = guaranteeOrderService.saveDepositCharge(ots, member, mobile);
		if(!code.isSuccess()) return showJsonError(model, code.getMsg());
		Charge charge = code.getRetval();
		return showJsonSuccess(model, charge.getId() + "");
	}
	
	@RequestMapping("/sport/open/showDeposit.xhtml")
	public String showDeposit(@CookieValue(value=LOGIN_COOKIE_NAME, required=false)String sessid, HttpServletRequest request, Long orderId, ModelMap model){
		Member member = loginService.getLogonMemberBySessid(WebUtils.getRemoteIp(request), sessid);
		if(member == null){
			return gotoLogin("/sport/open/showDeposit.xhtml", request, model);
		}
		Charge charge = daoService.getObject(Charge.class, orderId);
		if(charge == null || charge.hasValid()) return showMessageAndReturn(model, request, "已失效或不存在！");
		if (!charge.getMemberid().equals(member.getId())) return show404(model, "不能修改他人的订单！");
		SellDeposit deposit = daoService.getObjectByUkey(SellDeposit.class, "chargeid",charge.getId());
		model.put("charge", charge);
		model.put("deposit", deposit);
		OpenTimeSale ots = daoService.getObject(OpenTimeSale.class, deposit.getOtsid());
		Sport sport = daoService.getObject(Sport.class, ots.getSportid());
		SportItem item = daoService.getObject(SportItem.class, ots.getItemid());
		model.put("ots", ots);
		model.put("sport", sport);
		model.put("item", item);
		return "gewapay/sport/jingjia/wide_showOrderCharge.vm";
	}
	
	@RequestMapping("/sport/open/confirmDeposit.xhtml")
	public String confirmDeposit(@CookieValue(value=LOGIN_COOKIE_NAME, required=false)String sessid, HttpServletRequest request, Long orderId, ModelMap model){
		Member member = loginService.getLogonMemberBySessid(WebUtils.getRemoteIp(request), sessid);
		if(member == null) return showJsonError(model, "请先登录！");
		Charge charge = daoService.getObject(Charge.class, orderId);
		if(charge == null || charge.hasValid()) return showJsonError(model, "已失效或不存在！");
		if (!charge.getMemberid().equals(member.getId())) return show404(model, "不能修改他人的订单！");
		Map jsonMap = new HashMap();
		jsonMap.put("redirectUrl", paymentService.getChargePayUrl(charge, WebUtils.getRemoteIp(request)));
		return showJsonSuccess(model, jsonMap);
	}
	
	@RequestMapping("/sport/open/charge/time.xhtml")
	@ResponseBody
	public String chargeTime(Long orderId){
		String key = ChargeConstant.KEY_CHARGE_VALIDTIME_ + orderId;
		Long valid = (Long) cacheService.get(CacheConstant.REGION_HALFHOUR, key);
		if(valid==null){
			Charge charge = daoService.getObject(Charge.class, orderId);
			if(charge != null && charge.getValidtime()!= null){
				valid = charge.getValidtime().getTime();
				cacheService.set(CacheConstant.REGION_HALFHOUR, key, valid);
			}
		}
		if(valid!=null){
			Long cur = System.currentTimeMillis();
			Long remain = valid - cur;
			return ""+remain;
		}
		return "";
	}
}
