package com.gewara.web.action.sport;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.support.PropertyComparator;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.RequestMapping;

import com.gewara.Config;
import com.gewara.constant.OpenTimeItemConstant;
import com.gewara.constant.OpenTimeTableConstant;
import com.gewara.constant.Status;
import com.gewara.constant.ticket.OpiConstant;
import com.gewara.constant.ticket.OrderConstant;
import com.gewara.helper.SportOrderHelper;
import com.gewara.helper.TimeItemHelper;
import com.gewara.helper.discount.SportSpecialDiscountHelper;
import com.gewara.helper.order.OrderContainer;
import com.gewara.helper.order.SportOrderContainer;
import com.gewara.model.pay.Discount;
import com.gewara.model.pay.ElecCard;
import com.gewara.model.pay.MemberAccount;
import com.gewara.model.pay.SpecialDiscount;
import com.gewara.model.pay.SportOrder;
import com.gewara.model.sport.MemberCardInfo;
import com.gewara.model.sport.MemberCardType;
import com.gewara.model.sport.OpenTimeItem;
import com.gewara.model.sport.OpenTimeSale;
import com.gewara.model.sport.OpenTimeTable;
import com.gewara.model.sport.SellTimeTable;
import com.gewara.model.sport.Sport;
import com.gewara.model.sport.Sport2Item;
import com.gewara.model.sport.SportField;
import com.gewara.model.sport.SportItem;
import com.gewara.model.sport.SportProfile;
import com.gewara.model.user.Member;
import com.gewara.pay.PayUtil;
import com.gewara.pay.PayValidHelper;
import com.gewara.service.OrderException;
import com.gewara.service.api.ApiSportService;
import com.gewara.service.gewapay.ElecCardService;
import com.gewara.service.gewapay.PaymentService;
import com.gewara.service.sport.MemberCardService;
import com.gewara.service.sport.OpenTimeTableService;
import com.gewara.service.sport.SportOrderService;
import com.gewara.service.sport.SportService;
import com.gewara.service.ticket.TicketOrderService;
import com.gewara.support.ErrorCode;
import com.gewara.untrans.impl.ControllerService;
import com.gewara.untrans.sport.RemoteMemberCardService;
import com.gewara.untrans.sport.RemoteSportService;
import com.gewara.untrans.sport.SportUntransService;
import com.gewara.untrans.ticket.SpecialDiscountService;
import com.gewara.util.BeanUtil;
import com.gewara.util.DateUtil;
import com.gewara.util.JsonUtils;
import com.gewara.util.PKCoderUtil;
import com.gewara.util.StringUtil;
import com.gewara.util.ValidateUtil;
import com.gewara.util.VmUtils;
import com.gewara.util.WebUtils;
import com.gewara.web.action.AnnotationController;
import com.gewara.xmlbind.sport.GstOtt;
import com.gewara.xmlbind.sport.RemoteMemberCardInfo;

@Controller
public class SportOrderController extends AnnotationController {
	public static List<String> OPENTYPE_LIST = Arrays.asList(OpenTimeTableConstant.OPEN_TYPE_FIELD, OpenTimeTableConstant.OPEN_TYPE_PERIOD, OpenTimeTableConstant.OPEN_TYPE_INNING);
	@Autowired@Qualifier("controllerService")
	private ControllerService controllerService;
	public void setControllerService(ControllerService controllerService) {
		this.controllerService = controllerService;
	}
	@Autowired@Qualifier("sportOrderService")
	private SportOrderService sportOrderService;
	public void setSportOrderService(SportOrderService sportOrderService) {
		this.sportOrderService = sportOrderService;
	}
	@Autowired@Qualifier("paymentService")
	private PaymentService paymentService;
	@Autowired@Qualifier("openTimeTableService")
	private OpenTimeTableService openTimeTableService;
	public void setOpenTimeTableService(OpenTimeTableService openTimeTableService) {
		this.openTimeTableService = openTimeTableService;
	}
	@Autowired@Qualifier("sportService")
	private SportService sportService;
	public void setSportService(SportService sportService) {
		this.sportService = sportService;
	}
	@Autowired@Qualifier("elecCardService")
	private ElecCardService elecCardService;
	public void setElecCardService(ElecCardService elecCardService) {
		this.elecCardService = elecCardService;
	}
	@Autowired@Qualifier("apiSportService")
	private ApiSportService apiSportService;
	public void setApiSportService(ApiSportService apiSportService) {
		this.apiSportService = apiSportService;
	}
	@Autowired@Qualifier("specialDiscountService")
	private SpecialDiscountService specialDiscountService;
	@Autowired@Qualifier("config")
	protected Config config;
	
	@Autowired@Qualifier("remoteSportService")
	private RemoteSportService remoteSportService;
	@Autowired@Qualifier("sportUntransService")
	private SportUntransService sportUntransService;
	
	@Autowired@Qualifier("memberCardService")
	private MemberCardService memberCardService;
	
	@Autowired@Qualifier("remoteMemberCardService")
	private RemoteMemberCardService remoteMemberCardService;
	
	@Autowired@Qualifier("ticketOrderService")
	private TicketOrderService ticketOrderService;
	
	@RequestMapping("/sport/order/step1.xhtml")
	public String chooseField(@CookieValue(value=LOGIN_COOKIE_NAME, required=false)String sessid,
			HttpServletRequest request, Long ottid, Long sid, Long tid, String fyrq, String openType, ModelMap model) {
		String spkey = request.getParameter("spkey");
		model.put("spkey", spkey);
		
		Sport sport = null;
		SportItem item = null;
		OpenTimeTable ott = null;
		if(ottid==null && (sid==null || tid==null)) return show404(model, "参数传递错误！");
		if(ottid == null){	
			sport = daoService.getObject(Sport.class, sid);
			if(sport == null) return show404(model, "该运动场管不存在或被删除！");
			item = daoService.getObject(SportItem.class, tid);
			if(item == null) return show404(model, "该项目不存在或被删除！");
			if(!DateUtil.isValidDate(fyrq)) fyrq = "";
			Date curDate = DateUtil.getCurDate();
			Date playDate = null;
			List<OpenTimeTable> itemList = new ArrayList<OpenTimeTable>();
			if(StringUtils.isBlank(fyrq)){ 
				playDate = DateUtil.getCurDate();
				itemList = openTimeTableService.getOpenTimeTableList(sid, tid, playDate, null, 0, 1);
			}else{
				if(!OPENTYPE_LIST.contains(openType)) return show404(model, "参数传递错误！");
				playDate = DateUtil.parseDate(fyrq);
				if(playDate.before(curDate)){
					playDate = curDate;
					fyrq = DateUtil.format(curDate, "yyyy-MM-dd");
				}
				itemList = openTimeTableService.getOpenTimeTableList(sid, tid, playDate, null, openType, true, 0, 1);
			}
			if(!itemList.isEmpty()){
				ott = itemList.get(0);
			}
		}else{ 
			ott = daoService.getObject(OpenTimeTable.class, ottid);
			if(ott == null) return show404(model, "场次不存在！");
			sport = daoService.getObject(Sport.class, ott.getSportid());
			if(sport == null) return show404(model, "该运动场管不存在或被删除！");
			item = daoService.getObject(SportItem.class, ott.getItemid());
			if(item == null) return show404(model, "该项目不存在或被删除！");
		}
		model.put("fyrq", fyrq);
		model.put("item", item);
		model.put("sport", sport);
		String viewPage = "";
		if(ott != null){
			if(!ott.isBooking())  return showMessageAndReturn(model, request, "本场不接受预定！");
			Member member = loginService.getLogonMemberBySessid(WebUtils.getRemoteIp(request), sessid);
			Sport2Item sport2Item = sportService.getSport2Item(ott.getSportid(), ott.getItemid());
			model.put("opentime", (StringUtils.substring(sport2Item.getOpentime(), 0, 2) + ":" + StringUtils.substring(sport2Item.getOpentime(), 2, 4)));
			model.put("closetime", (StringUtils.substring(sport2Item.getClosetime(), 0, 2) + ":" + StringUtils.substring(sport2Item.getClosetime(), 2, 4)));
			model.put("isOpen", sport2Item.isOpen());
			sportOttData(ott, member, model);
			List<MemberCardInfo> cardInfoList = new ArrayList<MemberCardInfo>();
			Map<Long, MemberCardType> mctMap = new HashMap<Long, MemberCardType>();
			if(ott.hasField()){//场地
				if(member!=null){
					cardInfoList = memberCardService.getValidMemberCardInfoListByMemberid(member.getId(), ott);
					List<Long> typidList = BeanUtil.getBeanPropertyList(cardInfoList, "typeid", true);
					mctMap = daoService.getObjectMap(MemberCardType.class, typidList);
				}
				viewPage = "sport/wide_choosebyInning.vm";
				model.put("cardInfoList", cardInfoList);
				model.put("mctMap", mctMap);
			}else if(ott.hasPeriod()){//时间段
				viewPage = "sport/wide_choosebyTime.vm";
			}else if(ott.hasInning()){//局数
				viewPage = "sport/wide_choosebyRound.vm";
			}
		}else{
			if(StringUtils.equals(openType, OpenTimeTableConstant.OPEN_TYPE_FIELD)){//场地
				viewPage = "sport/wide_choosebyInning.vm";
			}else if(StringUtils.equals(openType, OpenTimeTableConstant.OPEN_TYPE_PERIOD)){//时间段
				viewPage = "sport/wide_choosebyTime.vm";
			}else if(StringUtils.equals(openType, OpenTimeTableConstant.OPEN_TYPE_INNING)){//局数
				viewPage = "sport/wide_choosebyRound.vm";
			}
		}
		if(StringUtils.isNotBlank(viewPage)) return viewPage;
		else return show404(model, "参数传递错误！");
	}
	
	@RequestMapping("/sport/order/step2.xhtml")
	public String addOrder(@CookieValue(value=LOGIN_COOKIE_NAME, required=false)String sessid, @CookieValue(required=false,value="origin") String origin,
			HttpServletRequest request, String captchaId, String captcha,
			Long ottid, String mobile, String fieldid, Long cardid, ModelMap model){
		String spkey = request.getParameter("spkey");
		
		String ip = WebUtils.getRemoteIp(request);
		boolean isValidCaptcha = controllerService.validateCaptcha(captchaId, captcha, WebUtils.getRemoteIp(request));
		if(!isValidCaptcha) return showJsonError(model, "验证码错误！");
		Member member = loginService.getLogonMemberBySessid(ip, sessid);
		if(member == null) return showJsonError(model, "请先登录！");
		if(!ValidateUtil.isMobile(mobile)) return showJsonError(model, "手机号有错误！");
		OpenTimeTable ott = daoService.getObject(OpenTimeTable.class, ottid);
		ErrorCode<SportOrder> code = null;
		try {
			ErrorCode<RemoteMemberCardInfo> mccode = null;
			if(cardid!=null && ott.hasField()){
				MemberCardInfo card = daoService.getObject(MemberCardInfo.class, cardid);
				mccode = remoteMemberCardService.getMemberCardInfo(card);
			}
			code = sportUntransService.addSportOrder(ott, fieldid, cardid, mccode, mobile, member);
		} catch (OrderException e) {
			dbLogger.error("订单错误：" + e.getMessage());
			return showJsonError(model, e.getMessage());
		} catch (Exception e){
			dbLogger.error("订单错误：" + StringUtil.getExceptionTrace(e));
			return showJsonError(model, "订单有错误，可能是时间段被别人占用，请刷新页面重新选择！");
		}
		if(!code.isSuccess()) return showJsonError(model, code.getMsg());
		saveOrderOtherInfo(code.getRetval(), ott, spkey);
		if(StringUtils.isNotBlank(origin)){
			ticketOrderService.addOrderOrigin(code.getRetval(), origin);
		}
		return showJsonSuccess(model, code.getRetval().getId()+"");
	}

	@RequestMapping("/sport/order/synchOti.xhtml")
	public String synchOti(Long ottid, ModelMap model){
		OpenTimeTable ott = daoService.getObject(OpenTimeTable.class, ottid);
		if(ott==null) return forwardMessage(model, ottid+"不存在");
		if(ott!=null && ott.isBooking() && ott.hasField() && ott.hasRemoteOtt()){
			/*Timestamp curtime = new Timestamp(System.currentTimeMillis());
			Integer mm = Integer.valueOf(DateUtil.format(curtime, "mm"));
			if(mm%3==0){
				sportUntransService.sysLockOti(ott);
			}*/
			ErrorCode<List<GstOtt>> code = remoteSportService.getGstOttList(ott.getSportid(), ott.getItemid(), ott.getPlaydate(), null);
			if(code.isSuccess()){
				List<GstOtt> ottList = code.getRetval();
				for(GstOtt gott : ottList){
					Sport2Item sport2Item = sportService.getSport2Item(ott.getSportid(), ott.getItemid());
					if(sport2Item != null && StringUtils.equals(sport2Item.getCreatetype(), Sport2Item.RANGE)){
						if(StringUtils.equals(ott.getItemid()+"", gott.getItemid()+"")){
							apiSportService.modSportTimeTable(gott);
						}
					}
				}
			}
		}
		openTimeTableService.updateOpenTimeTable(ott);
		return showJsonSuccess(model);
	}
	@RequestMapping("/sport/order/saveOrderDis.xhtml")
	public String saveOrderInfo(@CookieValue(value=LOGIN_COOKIE_NAME, required=false)String sessid,
			HttpServletRequest request,Long orderId, String discounttype, Integer usepoint, String mobile, ModelMap model){
		if(!ValidateUtil.isMobile(mobile)) return showJsonError(model, "手机格式不正确");
		if(StringUtils.equals(discounttype, "point")){
			if(usepoint == null) return showJsonError(model, "积分不正确!");
			return usePoint(sessid, request, orderId, usepoint, model);
		}else if(StringUtils.equals(discounttype, "card")){
			boolean usecard = false;
			List<Discount> discountList = daoService.getObjectListByField(Discount.class, "orderid", orderId);
			for(Discount discount: discountList){
				if("ABCD".indexOf(discount.getCardtype())>=0) usecard = true;
			}
			if(!usecard) return showJsonError(model, "您选择了运动票券优惠，但未使用任何票券!");
		}else if(StringUtils.equals(discounttype, "none")){
			SportOrder order = daoService.getObject(SportOrder.class, orderId);
			if(order.getDiscount() > 0) return showJsonError(model, "您选择了不使用优惠，但订单中使用了其他优惠!");
		}else if(StringUtils.isNotBlank(discounttype)){
			Long spid = Long.parseLong(discounttype);
			SpecialDiscount sd = daoService.getObject(SpecialDiscount.class, spid);
			if(StringUtils.isNotBlank(sd.getValidateUrl())){
				Map jsonMap = new HashMap<String, String>();
				jsonMap.put("url", sd.getValidateUrl() + "?orderId=" + orderId + "&spid=" + sd.getId());
				return showJsonSuccess(model, jsonMap);
			}
			ErrorCode<OrderContainer> discount = specialDiscountService.useSpecialDiscount(OrderConstant.ORDER_TYPE_SPORT, orderId, sd, WebUtils.getRemoteIp(request));
			if(discount.isSuccess()) return showJsonSuccess(model, ""+discount.getRetval().getCurUsedDiscount().getAmount());
			return showJsonError(model, discount.getMsg());
		}
		return showJsonSuccess(model);
	}
	@RequestMapping("/sport/order/usePoint.xhtml")
	public String usePoint(@CookieValue(value=LOGIN_COOKIE_NAME, required=false)String sessid,
			HttpServletRequest request, Long orderId, int pointvalue, ModelMap model){
		String ip = WebUtils.getRemoteIp(request);
		Member member = loginService.getLogonMemberBySessid(ip, sessid);
		if(member == null) return showJsonError(model, "请先登录！");
		ErrorCode code = sportOrderService.usePoint(orderId, member.getId(), pointvalue);
		if(code.isSuccess()) return showJsonSuccess(model);
		return showJsonError(model, code.getMsg());
	}
	@RequestMapping("/sport/order/useCardByPass.xhtml")
	public String useCardByPass(@CookieValue(value=LOGIN_COOKIE_NAME, required=false)String sessid,
			HttpServletRequest request, Long orderId, String cardpass, ModelMap model){
		String ip = WebUtils.getRemoteIp(request);
		Member member = loginService.getLogonMemberBySessid(ip, sessid);
		if(member == null) return showJsonError(model, "请先登录！");
		if(StringUtils.isBlank(cardpass)) return showJsonError(model, "请输入卡密码！");
		ElecCard card = elecCardService.getElecCardByPass(StringUtils.upperCase(cardpass));
		return useElecCard(orderId, card, member.getId(), model);
	}
	@RequestMapping("/sport/order/useElecCardByNo.xhtml")
	public String useElecCardByNo(@CookieValue(value=LOGIN_COOKIE_NAME, required=false)String sessid, String password,
			HttpServletRequest request, Long orderId, String cardno, ModelMap model){
		String ip = WebUtils.getRemoteIp(request);
		Member member = loginService.getLogonMemberBySessid(ip, sessid);
		if(member == null) return showJsonError(model, "请先登录！");
		if(StringUtils.isBlank(password)) return showJsonError(model, "支付密码不能为空！");
		MemberAccount memberAccount = daoService.getObjectByUkey(MemberAccount.class, "memberid", member.getId(), true);
		if(memberAccount == null || memberAccount.isNopassword()) return showJsonError(model, "先创建帐号或设置支付密码！"); 
		if(!StringUtils.equals(PayUtil.getPass(password), memberAccount.getPassword())) return showJsonError(model, "支付密码错误！");
		if(StringUtils.isBlank(cardno)) return showJsonError(model, "请输入卡号！");
		ElecCard card = elecCardService.getMemberElecCardByNo(member.getId(), cardno);
		if(card==null) return showJsonError(model, "卡密码不存在，重新输入！");
		
		if(card.getPossessor()==null || !card.getPossessor().equals(member.getId())){
			return showJsonError(model, "卡号有错误，重新输入！");
		}
		return useElecCard(orderId, card, member.getId(), model);
	}
	private String useElecCard(Long orderId, ElecCard card, Long memberid, ModelMap model){
		if(card==null) return showJsonError(model, "卡密码不存在，重新输入！");
		ErrorCode<SportOrderContainer> code = sportOrderService.useElecCard(orderId, card, memberid);
		if(code.isSuccess()) {
			Map jsonMap = new HashMap<String, String>(); 
			jsonMap.put("cardno", card.getCardno());
			jsonMap.put("validtime", DateUtil.format(card.getTimeto(), "yyyy-MM-dd"));
			SportOrder order = code.getRetval().getSportOrder();
			List<Discount> discountList = paymentService.getOrderDiscountList(order);
			for(Discount discount: discountList){
				if(discount.getRelatedid().equals(card.getId())){
					jsonMap.put("description", discount.getDescription());
					jsonMap.put("discountId", discount.getId());
					jsonMap.put("discount", discount.getAmount());
					jsonMap.put("usage", card.gainUsage());
					break;
				}
			}
			jsonMap.put("count", discountList.size());
			jsonMap.put("due", order.getDue());
			jsonMap.put("totalDiscount", order.getDiscount());
			jsonMap.put("totalAmount", order.getTotalAmount());
			jsonMap.put("type", card.getCardtype());
			jsonMap.put("exchangetype", card.getEbatch().getExchangetype());
			return showJsonSuccess(model, jsonMap);
		}
		return showJsonError(model, code.getMsg()+"如有疑问请联系客服！");
	}
	
	@RequestMapping("/sport/order/setPeriod.xhtml")
	public String setPeriod(@CookieValue(value=LOGIN_COOKIE_NAME, required=false)String sessid,
			HttpServletRequest request, Long otiid, String captchaId, String captcha, String mobile, String starttime, Integer time, Integer quantity, ModelMap model){
		boolean isValidCaptcha = controllerService.validateCaptcha(captchaId, captcha, WebUtils.getRemoteIp(request));
		if(!isValidCaptcha) return showJsonError(model, "验证码错误！");
		Member member = loginService.getLogonMemberBySessid(WebUtils.getRemoteIp(request), sessid);
		if(member == null) return showJsonError(model, "请先登录！");
		ErrorCode<SportOrder> code = sportUntransService.addSportOrder(otiid, starttime, time, quantity, mobile, member);
		if(!code.isSuccess()) return showJsonError(model, code.getMsg());
		return showJsonSuccess(model, code.getRetval().getId()+"");
	}
	
	@RequestMapping("/sport/order/setInning.xhtml")
	public String setInning(@CookieValue(value=LOGIN_COOKIE_NAME, required=false)String sessid,
			HttpServletRequest request, Long otiid, String captchaId, String captcha, String mobile, String starttime, Integer quantity, ModelMap model){
		boolean isValidCaptcha = controllerService.validateCaptcha(captchaId, captcha, WebUtils.getRemoteIp(request));
		if(!isValidCaptcha) return showJsonError(model, "验证码错误！");
		Member member = loginService.getLogonMemberBySessid(WebUtils.getRemoteIp(request), sessid);
		if(member == null) return showJsonError(model, "请先登录！");
		ErrorCode<SportOrder> code = sportUntransService.addSportOrder(otiid, starttime, quantity, mobile, member);
		if(!code.isSuccess()) return showJsonError(model, code.getMsg());
		return showJsonSuccess(model, code.getRetval().getId()+"");
	}
	
	@RequestMapping("/ajax/sport/getSeatPage.shtml")
	public String getSeatPage(@CookieValue(value=LOGIN_COOKIE_NAME, required=false)String sessid, Long otiid, HttpServletRequest request, ModelMap model){
		OpenTimeItem openTimeItem = daoService.getObject(OpenTimeItem.class, otiid);
		if(openTimeItem == null) return showJsonError(model, "该场地不存在或被删除！");
		OpenTimeTable ott = daoService.getObject(OpenTimeTable.class, openTimeItem.getOttid());
		Timestamp cur = DateUtil.getCurFullTimestamp();
		if(ott == null || !ott.isBooking() || !openTimeItem.hasStatusNew()|| cur.after(openTimeItem.getValidtime())) 
			return showJsonError(model, "该场地不接受预订！");
		Member member = loginService.getLogonMemberBySessid(WebUtils.getRemoteIp(request), sessid);
		if(member != null){
			SportOrder order = sportOrderService.getLastUnpaidSportOrder(member.getId(), member.getId()+"", ott.getId());
			if(order != null){
				SellTimeTable sellTimeTable = daoService.getObject(SellTimeTable.class, order.getId());
				model.put("sellTimeTable", sellTimeTable);
			}
			model.put("member", member);
		}
		List<String> timeList = SportOrderHelper.getStarttimeList(ott.getPlaydate(), openTimeItem);
		List<Integer> periodList = SportOrderHelper.getPeriodList(ott.getPlaydate(), openTimeItem);
		SportProfile sp = daoService.getObject(SportProfile.class, ott.getSportid());
		model.put("sp", sp);
		model.put("ott", ott);
		if(!timeList.isEmpty()){
			model.put("defaultStartTime", timeList.get(0));
		}
		List<OpenTimeItem> otiList = openTimeTableService.getOpenItemList(ott.getId());
		List<OpenTimeItem> newOtiList = new ArrayList<OpenTimeItem>();
		for (OpenTimeItem oti : otiList) {
			if(oti.hasStatusNew() && cur.before(oti.getValidtime())){
				newOtiList.add(oti);
			}
		}
		Collections.sort(newOtiList, new PropertyComparator("hour", false, true));
		model.put("otiList", newOtiList);
		model.put("defaultEndTime", SportOrderHelper.getDefalutEndTime(ott.getPlaydate(), openTimeItem));
		model.put("timeList", timeList);
		model.put("periodList", periodList);
		model.put("curOti", openTimeItem);
		if(ott.hasInning()){
			List<Long> fieldIdList = BeanUtil.getBeanPropertyList(newOtiList, Long.class, "fieldid", true);
			Map<Long, SportField> fieldMap = daoService.getObjectMap(SportField.class, fieldIdList);
			model.put("fieldMap", fieldMap);
			return "sport/wide_inningSeatPage.vm";
		}
		return "sport/wide_periodSeatPage.vm";
	}
	
	private void sportOttData(OpenTimeTable ott, Member member, ModelMap model){
		Timestamp cur = new Timestamp(System.currentTimeMillis());
		Date curDate = DateUtil.getBeginningTimeOfDay(cur);
		List<OpenTimeTable> itemList = new ArrayList<OpenTimeTable>();
		itemList = openTimeTableService.getOpenTimeTableList(ott.getSportid(), ott.getItemid(), curDate, null, ott.getOpenType());
		model.put("itemList", itemList);
		List<OpenTimeItem> otiList = openTimeTableService.getOpenItemList(ott.getId());
		List<OpenTimeItem> myOtiList = new ArrayList<OpenTimeItem>();
		if(member != null){
			SportOrder order = sportOrderService.getLastUnpaidSportOrder(member.getId(), member.getId()+"", ott.getId());
			if(order != null){
				if(ott.hasField()){
					SellTimeTable sellTimeTable = daoService.getObject(SellTimeTable.class, order.getId());
					model.put("sellTimeTable", sellTimeTable);
				}else myOtiList.addAll(sportOrderService.getMyOtiList(order.getId()));
			}
			model.put("member", member);
		}
		model.put("playDate", ott.getPlaydate());
		SportProfile profile = daoService.getObject(SportProfile.class, ott.getSportid());
		model.put("sp", profile);
		if(ott.hasField()){//按场地
			List<SportField> fieldList = sportOrderService.getSportFieldList(ott.getId());
			boolean validOver = false;
			List<Long> otiidList = new ArrayList<Long>();
			int price = 0;
			for (OpenTimeItem openTimeItem : myOtiList) {
				otiidList.add(openTimeItem.getId());
				price += openTimeItem.getPrice();
			}
			if(ott.getPlaydate().compareTo(DateUtil.getBeginningTimeOfDay(new Date()))==0) validOver = true;
			//List<String> playHourList = sportOrderService.getPlayHourList(ottid, OpenTimeItem.STATUS_DELETE);
			List<Integer> priceList = openTimeTableService.getTimeItemPrice(ott.getId());
			model.put("validOver", validOver);
			model.put("priceList", priceList);
			model.put("fieldList", fieldList);
			model.put("myOtiList", myOtiList);
			model.put("otiidList", StringUtils.join(otiidList, ","));
			model.put("price", price);
			//获取改场馆的限制时间 cpf
			Sport2Item sport2Item = sportService.getSport2Item(ott.getSportid(), ott.getItemid());
			model.put("sportlimitMinutes", sport2Item.getLimitminutes());
			Map<String, List<OpenTimeItem>> bindOtiMap = BeanUtil.groupBeanList(otiList, "bindInd");
			for (String key : bindOtiMap.keySet()) {
				List<OpenTimeItem> tempList = bindOtiMap.get(key);
				Collections.sort(tempList, new PropertyComparator("hour", false, true));
				bindOtiMap.put(key, tempList);
			}
			model.put("bindOtiMap", bindOtiMap);
			Map<String, List<OpenTimeItem>> saleIndOtiMap = BeanUtil.groupBeanList(otiList, "saleInd");
			for (String key : saleIndOtiMap.keySet()) {
				List<OpenTimeItem> tempList = saleIndOtiMap.get(key);
				Collections.sort(tempList, new PropertyComparator("hour", false, true));
				saleIndOtiMap.put(key, tempList);
			}
			model.put("saleIndOtiMap", saleIndOtiMap);
			List<OpenTimeItem> overotiList = new ArrayList<OpenTimeItem>();
			List<String> playHourList = new ArrayList<String>();
			TimeItemHelper itemHelper = new TimeItemHelper(otiList);
			int remain = 0;
			List<Long> filedidList = BeanUtil.getBeanPropertyList(fieldList, Long.class, "id", true);
			Map<String, OpenTimeItem> otiMap = itemHelper.getOtiMap();
			for(String key : otiMap.keySet()){
				OpenTimeItem oti = otiMap.get(key);
				if(validOver && oti.hasOver(sport2Item.getLimitminutes())){
					overotiList.add(oti);
				}else{
					if(oti.hasAvailable() && oti.getFieldid()!=null && filedidList.contains(oti.getFieldid())) remain++;
					if(!VmUtils.contains(playHourList, oti.getHour()) && !StringUtils.equals(oti.getStatus(), OpenTimeItemConstant.STATUS_DELETE)) { 
						playHourList.add(oti.getHour());
					}
				}
			}
			ott.setRemain(remain);
			for (OpenTimeItem overoti : overotiList) {
				if(StringUtils.isNotBlank(overoti.getBindInd()) && !StringUtils.equals(overoti.getBindInd(), "0")){
					List<OpenTimeItem> bindotiList = bindOtiMap.get(overoti.getBindInd());
					if(bindotiList != null){
						bindOtiMap.remove(overoti.getBindInd());
						otiList.removeAll(bindotiList);
					}
				}else{
					otiList.remove(overoti);
				}
			}
			//otiList.removeAll(overotiList);
			Collections.sort(playHourList);
			model.put("playHourList", playHourList);
			model.put("ott", ott);
			model.put("otiList", otiList);
			model.put("itemHelper", itemHelper);
			model.put("otsMap", daoService.getObjectMap(OpenTimeSale.class, BeanUtil.getBeanPropertyList(otiList, Long.class, "otsid", true)));
			Map<Long, Integer> otiCountMap = new HashMap<Long, Integer>();
			otiCountMap.put(ott.getId(), sportOrderService.getOpenTimeItemCount(ott.getId(), OpenTimeItemConstant.STATUS_NEW, DateUtil.format(DateUtil.addMinute(ott.getPlaydate(), sport2Item.getLimitminutes()), "HH:mm")));
			for(OpenTimeTable otts : itemList){
				Date ottDate = otts.getPlaydate();
				if(otts.getPlaydate().equals(DateUtil.getCurDate()))
					ottDate = DateUtil.addMinute(DateUtil.currentTime(), sport2Item.getLimitminutes());
				Integer otiCount = sportOrderService.getOpenTimeItemCount(otts.getId(), OpenTimeItemConstant.STATUS_NEW, DateUtil.format(ottDate, "HH:mm"));
				otiCountMap.put(otts.getId(), otiCount);
			}
			model.put("otiCountMap", otiCountMap);
			daoService.saveObject(ott);
		}else{ //按人次、局数
			List<OpenTimeItem> newOtiList = new ArrayList<OpenTimeItem>();
			for (OpenTimeItem oti : otiList) {
				if(oti.hasStatusNew() && cur.before(oti.getValidtime())){
					SportField sp = daoService.getObject(SportField.class, oti.getFieldid());
					if(StringUtils.equals(sp.getStatus(), Status.Y)){
						newOtiList.add(oti);
					}
				}
			}
			Collections.sort(newOtiList, new PropertyComparator("hour", false, true));
			if(!newOtiList.isEmpty()){ 
				OpenTimeItem openTimeItem = newOtiList.get(0);
				List<String> timeList = SportOrderHelper.getStarttimeList(ott.getPlaydate(),openTimeItem);
				if(!timeList.isEmpty()){
					model.put("defaultStartTime", timeList.get(0));
				}
				if(ott.hasPeriod()){
					List<Integer> periodList = SportOrderHelper.getPeriodList(ott.getPlaydate(),openTimeItem);
					model.put("defaultEndTime", SportOrderHelper.getDefalutEndTime(ott.getPlaydate(),openTimeItem));
					model.put("periodList", periodList);
				}else if(ott.hasInning()){
					List<Long> fieldIdList = BeanUtil.getBeanPropertyList(newOtiList, Long.class, "fieldid", true);
					Map<Long, SportField> fieldMap = daoService.getObjectMap(SportField.class, fieldIdList);
					model.put("fieldMap", fieldMap);
				}
				model.put("ott", ott);
				model.put("timeList", timeList);
				model.put("curOti", openTimeItem);
			}
			model.put("otiList", newOtiList);
		}
	}
	
	private ErrorCode saveOrderOtherInfo(SportOrder order, OpenTimeTable ott, String spkey) {
		String spid = null;
		if(StringUtils.isNotBlank(spkey)){
			spid = PKCoderUtil.decryptString(spkey, SpecialDiscount.ENCODE_KEY);
		}
		if(StringUtils.isNotBlank(spid)){
			SportSpecialDiscountHelper helper = new SportSpecialDiscountHelper(order, ott, null, null);
			SpecialDiscount sd = daoService.getObject(SpecialDiscount.class, new Long(spid));
			PayValidHelper pvh = new PayValidHelper(VmUtils.readJsonToMap(ott.getOtherinfo()));
			ErrorCode validateCode = helper.isEnabled(sd, pvh);
			if(sd != null && validateCode.isSuccess()){
				order.setOtherinfo(JsonUtils.addJsonKeyValue(order.getOtherinfo(), OpiConstant.FROM_SPID, spid));
				daoService.saveObject(order);
			}
		}
		return ErrorCode.SUCCESS;
	}
}
