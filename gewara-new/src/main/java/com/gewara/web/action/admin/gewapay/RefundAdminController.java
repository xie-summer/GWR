package com.gewara.web.action.admin.gewapay;
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

import com.gewara.command.SearchRefundCommand;
import com.gewara.constant.sys.JsonDataKey;
import com.gewara.constant.ticket.OpiConstant;
import com.gewara.constant.ticket.OrderConstant;
import com.gewara.constant.ticket.RefundConstant;
import com.gewara.model.acl.User;
import com.gewara.model.api.OrderResult;
import com.gewara.model.common.JsonData;
import com.gewara.model.drama.DramaOrder;
import com.gewara.model.drama.OpenDramaItem;
import com.gewara.model.goods.BaseGoods;
import com.gewara.model.movie.Cinema;
import com.gewara.model.movie.CinemaProfile;
import com.gewara.model.pay.AccountRefund;
import com.gewara.model.pay.BuyItem;
import com.gewara.model.pay.Discount;
import com.gewara.model.pay.GewaOrder;
import com.gewara.model.pay.GoodsOrder;
import com.gewara.model.pay.GymOrder;
import com.gewara.model.pay.OrderRefund;
import com.gewara.model.pay.PubSale;
import com.gewara.model.pay.PubSaleOrder;
import com.gewara.model.pay.SportOrder;
import com.gewara.model.pay.TicketOrder;
import com.gewara.model.sport.OpenTimeTable;
import com.gewara.model.sport.SellTimeTable;
import com.gewara.model.ticket.OpenPlayItem;
import com.gewara.service.gewapay.AccountRefundService;
import com.gewara.service.gewapay.PaymentService;
import com.gewara.service.gewapay.RefundService;
import com.gewara.service.order.OrderQueryService;
import com.gewara.support.ErrorCode;
import com.gewara.untrans.order.RefundOperationService;
import com.gewara.untrans.ticket.TicketOperationService;
import com.gewara.util.BeanUtil;
import com.gewara.util.ChangeEntry;
import com.gewara.util.DateUtil;
import com.gewara.util.JsonUtils;
import com.gewara.util.ValidateUtil;
import com.gewara.util.VmUtils;
import com.gewara.web.action.admin.BaseAdminController;
import com.gewara.web.util.PageUtil;

@Controller
public class RefundAdminController extends BaseAdminController{
	
	@Autowired@Qualifier("refundService")
	private RefundService refundService;
	@Autowired@Qualifier("orderQueryService")
	private OrderQueryService orderQueryService;
	@Autowired@Qualifier("ticketOperationService")
	private TicketOperationService ticketOperationService;
	@Autowired@Qualifier("accountRefundService")
	private AccountRefundService accountRefundService;
	@Autowired@Qualifier("paymentService")
	private PaymentService paymentService;
	
	
	@Autowired@Qualifier("refundOperationService")
	private RefundOperationService refundOperationService;

	@RequestMapping("/admin/refund/order/applyList.xhtml")
	public String applyList(SearchRefundCommand command, Integer pageNo, ModelMap model){
		if(pageNo==null) pageNo = 0;
		int rowsPerpage = 50;
		int firstRow = pageNo * rowsPerpage;
		model.put("pageNo", pageNo);
		List<OrderRefund> refundList = refundService.getOrderRefundList(command, null, firstRow, rowsPerpage);
		model.put("refundList", refundList);
		List<Long> idList = BeanUtil.getBeanPropertyList(refundList, Long.class, "applyuser", true);
		Map<Long, String> usernameMap = daoService.getObjectPropertyMap(User.class, "id", "nickname", idList);
		model.put("usernameMap", usernameMap);
		model.put("orderStatusMap", OrderConstant.statusMap);
		SearchRefundCommand comm = new SearchRefundCommand();
		comm.setStatus(RefundConstant.STATUS_APPLY);
		model.put("applyCount", refundService.getOrderRefundCount(comm));
		comm = new SearchRefundCommand();
		comm.setStatus(RefundConstant.STATUS_ACCEPT);
		model.put("acceptCount", refundService.getOrderRefundCount(comm));
		return "admin/refund/order/applyList.vm";
	}
	@RequestMapping("/admin/refund/order/refundList.xhtml")
	public String ticketRefundList(SearchRefundCommand command, Integer pageNo, ModelMap model){
		if(pageNo==null) pageNo = 0;
		int rowsPerpage = 50;
		model.put("pageNo", pageNo);
		int firstRow = pageNo * rowsPerpage;
		List<OrderRefund> refundList = refundService.getOrderRefundList(command, null, firstRow, rowsPerpage);
		model.put("refundList", refundList);
		SearchRefundCommand comm = new SearchRefundCommand();
		comm.setStatus(RefundConstant.STATUS_APPLY);
		model.put("applyCount", refundService.getOrderRefundCount(comm));
		comm = new SearchRefundCommand();
		comm.setStatus(RefundConstant.STATUS_ACCEPT);
		model.put("acceptCount", refundService.getOrderRefundCount(comm));
		List<Long> idList = BeanUtil.getBeanPropertyList(refundList, Long.class, "applyuser", true);
		Map<Long, String> usernameMap = daoService.getObjectPropertyMap(User.class, "id", "nickname", idList);
		model.put("usernameMap", usernameMap);
		model.put("orderStatusMap", OrderConstant.statusMap);
		return "admin/refund/order/refundList.vm";
	}
	/**
	 * 差价退款
	 * @param tradeno
	 * @param status
	 * @param model
	 * @return
	 */
	@RequestMapping("/admin/refund/order/addSupplementRefund.xhtml")
	public String addSupplementRefund(String tradeno, String status, String reapply, ModelMap model){
		ErrorCode result = addRefund(tradeno, status, RefundConstant.REFUNDTYPE_SUPPLEMENT, false/*无效*/, reapply, false, model);
		if(result.isSuccess()) return "redirect:/admin/refund/order/modifyRefund.xhtml";
		return showError(model, result.getMsg());
	}
	/**
	 * 当前订单退款
	 * @param tradeno
	 * @param status
	 * @param model
	 * @return
	 */
	@RequestMapping("/admin/refund/order/addCurFullRefund.xhtml")
	public String addCurFullRefund(String tradeno, String status, String reapply, ModelMap model){
		ErrorCode result = addRefund(tradeno, status, RefundConstant.REFUNDTYPE_FULL, false, reapply, true, model);
		if(result.isSuccess()) return "redirect:/admin/refund/order/modifyRefund.xhtml";
		return showError(model, result.getMsg());
	}
	/**
	 * 当前订单退款，电商，不检查可退影院权限
	 * @param tradeno
	 * @param status
	 * @param reapply
	 * @param model
	 * @return
	 */
	@RequestMapping("/admin/refund/order/addCurFullRefundByDs.xhtml")
	public String addCurFullRefundByDs(String tradeno, String status, String reapply, ModelMap model){
		ErrorCode result = addRefund(tradeno, status, RefundConstant.REFUNDTYPE_FULL, false, reapply, false, model);
		if(result.isSuccess()) return "redirect:/admin/refund/order/modifyRefund.xhtml";
		return showError(model, result.getMsg());
	}
	/**
	 * 过期订单全额退款
	 * @param tradeno
	 * @param status
	 * @param model
	 * @return
	 */
	@RequestMapping("/admin/refund/order/addExpFullRefund.xhtml")
	public String addExpFullRefund(String tradeno, String status, String reapply, ModelMap model){
		ErrorCode result = addRefund(tradeno, status, RefundConstant.REFUNDTYPE_FULL, true, reapply, false, model);
		if(result.isSuccess()) return "redirect:/admin/refund/order/modifyRefund.xhtml";
		return showError(model, result.getMsg());
	}
	@RequestMapping("/admin/refund/order/addPartRefund.xhtml")
	public String addPartRefund(String tradeno, String status, String reapply, ModelMap model){
		ErrorCode result = addRefund(tradeno, status, RefundConstant.REFUNDTYPE_PART, true, reapply, false, model);
		if(result.isSuccess()) return "redirect:/admin/refund/order/modifyRefund.xhtml";
		return showError(model, result.getMsg());
	}
	private ErrorCode addRefund(String tradeno, String status, String type, boolean expire, String reapply, boolean checkRefundable/*检查是否支持*/, ModelMap model){
		OrderRefund refund = daoService.getObjectByUkey(OrderRefund.class, "tradeno", tradeno, false);
		if(refund !=null && StringUtils.equals(reapply, "Y")){
			if(!refund.getStatus().equals(RefundConstant.STATUS_REJECT)){
				return ErrorCode.getFailure("状态不对，无法重新申请！");
			}
			/*if(!refund.getStatus().equals(RefundConstant.STATUS_CANCEL) && 
					!refund.getStatus().equals(RefundConstant.STATUS_REJECT)){
				return ErrorCode.getFailure("状态不对，无法重新申请！");
			}*/
		}
		if(refund==null || StringUtils.equals(reapply, "Y")){
			if(!StringUtils.equals(RefundConstant.STATUS_APPLY, status)){
				return ErrorCode.getFailure("申请状态不正确！！");
			}
			/*if(!StringUtils.equals(RefundConstant.STATUS_APPLY, status) && 
					!StringUtils.equals(RefundConstant.STATUS_PREPAIR, status)){
				return ErrorCode.getFailure("申请状态不正确！！");
			}*/
			GewaOrder order = daoService.getObjectByUkey(GewaOrder.class, "tradeNo", tradeno, false);
			User user = getLogonUser();
			ErrorCode<OrderRefund> result = null;
			if(order instanceof TicketOrder){
				OpenPlayItem opi = daoService.getObjectByUkey(OpenPlayItem.class, "mpid", ((TicketOrder) order).getMpid(), true);
				if(RefundConstant.REFUNDTYPE_SUPPLEMENT.equals(type)){
					result = refundService.getSupplementTicketOrderRefund((TicketOrder) order, opi, user.getId(), status);
				}else if(RefundConstant.REFUNDTYPE_PART.equals(type)){//部分退款
					result = refundService.getPartTicketOrderRefund((TicketOrder) order, opi, user.getId(), status);
				}else if(RefundConstant.REFUNDTYPE_FULL.equals(type)){
					if(expire){
						result = refundService.getExpFullTicketOrderRefund((TicketOrder) order, opi, user.getId(), status);
					}else{
						if(checkRefundable && !opi.hasGewara() && order.isPaidSuccess()){
							//火凤凰成功订单，客服只能申请能退票的影院！
							CinemaProfile profile = daoService.getObject(CinemaProfile.class, ((TicketOrder) order).getCinemaid());
							if(profile.getIsRefund().equals("N")) return ErrorCode.getFailure(OpiConstant.getParnterText(opi.getOpentype()) + "成功订单，无权限申请不可退票的影院！");
						}
						result = refundService.getCurFullTicketOrderRefund((TicketOrder) order, opi, user.getId(), status);
					}
				}
			}else if(order instanceof DramaOrder){
				OpenDramaItem odi = daoService.getObjectByUkey(OpenDramaItem.class, "dpid", ((DramaOrder) order).getDpid(), true);
				if(RefundConstant.REFUNDTYPE_SUPPLEMENT.equals(type)){
					result = refundService.getSupplementDramaOrderRefund((DramaOrder) order, odi, user.getId(), status);
				}/*else if(RefundConstant.REFUNDTYPE_PART.equals(type)){//部分退款
					result = refundService.getPartDramaOrderRefund((DramaOrder) order, odi, user.getId(), status);
				}*/else if(RefundConstant.REFUNDTYPE_FULL.equals(type)){
					result = refundService.getFullDramaOrderRefund((DramaOrder)order, odi, user.getId(), status);
				}
			}else if(order instanceof SportOrder){
				OpenTimeTable ott = daoService.getObject(OpenTimeTable.class, ((SportOrder) order).getOttid());
				if(RefundConstant.REFUNDTYPE_SUPPLEMENT.equals(type)){
					result = refundService.getSupplementSportOrderRefund((SportOrder) order, ott, user.getId(), status);
				}/*else if(RefundConstant.REFUNDTYPE_PART.equals(type)){//部分退款
					result = refundService.getPartSportOrderRefund((SportOrder) order, ott, user.getId(), status);
				}*/else if(RefundConstant.REFUNDTYPE_FULL.equals(type)){
					result = refundService.getFullSportOrderRefund((SportOrder)order, ott, user.getId(), status);
				}
			}else if(order instanceof GymOrder){
				result = refundService.getGymOrderRefund((GymOrder)order, user.getId(), status);
			}else if(order instanceof PubSaleOrder){
				result = refundService.getPubSaleOrderRefund((PubSaleOrder)order, user.getId(), status);
			}else if(order instanceof GoodsOrder){
				result = refundService.getGoodsOrderRefund((GoodsOrder)order, user.getId(), status);
			}else {
				return ErrorCode.getFailure("订单类型错误！");
			}
			if(!result.isSuccess()) return ErrorCode.getFailure(result.getMsg());
			if(refund==null){
				refund = result.getRetval();
			}else{//重新申请
				ChangeEntry entry = new ChangeEntry(refund);
				refund.copyFrom(result.getRetval());
				monitorService.saveChangeLog(user.getId(), OrderRefund.class, refund.getTradeno(), entry.getChangeMap(refund));
			}
			daoService.saveObject(refund);
		}
		model.put("rid", refund.getId());
		return ErrorCode.SUCCESS;
	}
	@RequestMapping("/admin/refund/order/viewSettle.xhtml")
	public String viewSettle(Long rid, ModelMap model){
		OrderRefund refund = daoService.getObject(OrderRefund.class, rid);
		if(!RefundConstant.STATUS_SUCCESS.equals(refund.getStatus())){
			return showMessage(model, "状态不正确，不能修改！");
		}
		model.put("refund", refund);
		GewaOrder order = daoService.getObjectByUkey(GewaOrder.class, "tradeNo", refund.getTradeno(), false);
		model.put("order", order);
		addViewData(order, model);
		return "admin/refund/order/viewSettle.vm";
	}
	@RequestMapping("/admin/refund/order/saveSettle.xhtml")
	public String saveSettle(Long rid, Integer newSettle, String finish, ModelMap model){
		OrderRefund refund = daoService.getObject(OrderRefund.class, rid);
		if(!RefundConstant.STATUS_SUCCESS.equals(refund.getStatus())){
			return showJsonError(model, "状态不正确，不能修改！");
		}
		refund.setNewSettle(newSettle);
		if(StringUtils.equals("Y",finish)){
			refund.setStatus(RefundConstant.STATUS_FINISHED);
		}
		daoService.saveObject(refund);
		return showJsonSuccess(model);
	}
	@RequestMapping("/admin/refund/order/modifyRefund.xhtml")
	public String modifyRefund(Long rid, ModelMap model){
		OrderRefund refund = daoService.getObject(OrderRefund.class, rid);
		if(!RefundConstant.STATUS_APPLY.equals(refund.getStatus())){
			return showMessage(model, "状态不正确，不能修改！");
		}
		/*if(!RefundConstant.STATUS_APPLY.equals(refund.getStatus()) && 
				!RefundConstant.STATUS_PREPAIR.equals(refund.getStatus())){
			return showMessage(model, "状态不正确，不能修改！");
		}*/
		model.put("refund", refund);
		GewaOrder order = daoService.getObjectByUkey(GewaOrder.class, "tradeNo", refund.getTradeno(), false);
		model.put("order", order);

		addViewData(order, model);
		return "admin/refund/order/modifyRefund.vm";
	}
	@RequestMapping("/admin/refund/order/viewCurRefund.xhtml")
	public String viewCurRefund(Long rid, String reapply, String tradeno, ModelMap model){
		model.put("timetype", "cur");
		model.put("reapply", reapply);
		return viewRefund(rid, tradeno, model);
	}
	@RequestMapping("/admin/refund/order/viewAllRefund.xhtml")
	public String viewAllRefund(Long rid, String reapply, String tradeno, ModelMap model){
		model.put("timetype", "exp");
		model.put("reapply", reapply);
		return viewRefund(rid, tradeno, model);
	}
	private String viewRefund(Long rid, String tradeno, ModelMap model){
		OrderRefund refund = null;
		if(rid!=null){
			refund = daoService.getObject(OrderRefund.class, rid);
			tradeno = refund.getTradeno();
		}else{
			refund = daoService.getObjectByUkey(OrderRefund.class, "tradeno", tradeno, false);
		}
		if(refund!=null){
			model.put("refund", refund);
		}
		GewaOrder order = daoService.getObjectByUkey(GewaOrder.class, "tradeNo", tradeno, false);
		model.put("order", order);
		addViewData(order, model);
		return "admin/refund/order/viewRefund.vm";
	}
	private void addViewData(GewaOrder order, ModelMap model){
		if(order == null) return;
		List<Discount> discountList = paymentService.getOrderDiscountList(order);
		List<BuyItem> itemList = daoService.getObjectListByField(BuyItem.class, "orderid", order.getId());
		model.put("itemList", itemList);
		model.put("discountList", discountList);
		OrderResult orderResult = daoService.getObject(OrderResult.class, order.getTradeNo());
		Timestamp timefrom = DateUtil.addDay(new Timestamp(System.currentTimeMillis()), -30);
		SearchRefundCommand comm = new SearchRefundCommand();
		comm.setStatus(RefundConstant.STATUS_SUCCESS);
		comm.setMemberid(order.getMemberid());
		int memberCount = refundService.getOrderRefundCount(comm);
		model.put("reasonCount",refundService.getRefundReason(comm));
		comm.setAddtimefrom(timefrom);
		int memberMonthCount = refundService.getOrderRefundCount(comm);
		comm = new SearchRefundCommand();
		comm.setStatus(RefundConstant.STATUS_SUCCESS);
		comm.setMobile(order.getMobile());
		int mobileCount = refundService.getOrderRefundCount(comm);
		comm.setAddtimefrom(timefrom);
		int mobileMonthCount = refundService.getOrderRefundCount(comm);
		
		model.put("orderResult", orderResult);
		model.put("itemList", itemList);
		model.put("memberCount", memberCount);
		model.put("memberMonthCount", memberMonthCount);
		model.put("mobileCount", mobileCount);
		model.put("mobileMonthCount", mobileMonthCount);
		if(order instanceof TicketOrder){
			OpenPlayItem opi = daoService.getObjectByUkey(OpenPlayItem.class, "mpid", ((TicketOrder)order).getMpid(), false);
			model.put("expire", opi.isExpired());
			CinemaProfile cinemaProfile = daoService.getObject(CinemaProfile.class, opi.getCinemaid());
			model.put("opi", opi);
			model.put("cinemaProfile", cinemaProfile);
		}else if(order instanceof DramaOrder){
			OpenDramaItem odi = daoService.getObjectByUkey(OpenDramaItem.class, "dpid", ((DramaOrder)order).getDpid(), true);
			model.put("expire", odi.isExpired());
			model.put("odi", odi);
		}else if(order instanceof SportOrder){
			OpenTimeTable ott = daoService.getObject(OpenTimeTable.class, ((SportOrder)order).getOttid());
			String strtime = null;
			Map<String, String> descMap = VmUtils.readJsonToMap(order.getDescription2());
			if(ott.hasField()){
				strtime = descMap.get("时间");
			}else{
				SellTimeTable stt = daoService.getObject(SellTimeTable.class, order.getId());
				strtime = DateUtil.formatTimestamp(ott.getPlayTimeByHour(stt.getStarttime()));
			}
			Timestamp playtime = DateUtil.parseTimestamp(strtime);
			model.put("expire", playtime.before(DateUtil.getMillTimestamp()));
			model.put("ott", ott);
		}else if(order instanceof GoodsOrder){
			BaseGoods goods = daoService.getObject(BaseGoods.class, ((GoodsOrder)order).getGoodsid());
			model.put("expire", goods.getTotime().before(DateUtil.getMillTimestamp()));
			model.put("goods", goods);
		}else if(order instanceof PubSaleOrder){
			model.put("expire", false);
			PubSale pubsale = daoService.getObject(PubSale.class, ((PubSaleOrder)order).getPubid());
			model.put("pubsale", pubsale);
		}
	}
	@RequestMapping("/admin/refund/order/saveRefund.xhtml")
	public String saveRefund(Long rid, String mobile, String reason, String applyinfo, 
			String preinfo, Integer merRetAmount, Integer gewaRetAmount, String retback, ModelMap model){
		OrderRefund refund = daoService.getObject(OrderRefund.class, rid);
		if(!RefundConstant.STATUS_APPLY.equals(refund.getStatus())){
			return showJsonError(model, "状态不正确，不能修改！");
		}
		/*if(!RefundConstant.STATUS_APPLY.equals(refund.getStatus())&& 
				!RefundConstant.STATUS_PREPAIR.equals(refund.getStatus())){
			return showJsonError(model, "状态不正确，不能修改！");
		}*/

		GewaOrder order = daoService.getObjectByUkey(GewaOrder.class, "tradeNo", refund.getTradeno(), false);
		if(!StringUtils.equals(refund.getOrderstatus(), order.getStatus())){
			return showJsonError(model, "订单状态已变更，请重新申请！");
		}

		ChangeEntry entry = new ChangeEntry(refund);
		int paidAmount =  order.getAlipaid()+order.getGewapaid();
		
		if(RefundConstant.REFUNDTYPE_PART.equals(refund.getRefundtype())){
			if(!OrderConstant.STATUS_PAID_SUCCESS.equals(order.getStatus())){
				return showJsonError(model, "只有成功的订单才能选择“部分退款”！");
			}
			if(merRetAmount <0 || gewaRetAmount < 0 || merRetAmount + gewaRetAmount==0 && paidAmount > 0){
				return showJsonError(model, "退款金额错误！");
			}
			if(merRetAmount + gewaRetAmount > paidAmount){
				return showJsonError(model, "退款金额过大！");
			}
			refund.setMerRetAmount(merRetAmount);
			refund.setGewaRetAmount(gewaRetAmount);
		}else if(RefundConstant.REFUNDTYPE_SUPPLEMENT.equals(refund.getRefundtype())){
			if(gewaRetAmount >= paidAmount || gewaRetAmount<=0){
				return showJsonError(model, "退款金额不正确！");
			}
			refund.setMerRetAmount(0);
			refund.setGewaRetAmount(gewaRetAmount);
		}else{
			if(StringUtils.isNotBlank(reason)) {
				refund.setReason(reason);
			}
		}
		if(ValidateUtil.isMobile(mobile)){
			refund.setMobile(mobile);
		}
		if(StringUtils.isNotBlank(applyinfo)) refund.setApplyinfo(applyinfo);
		if(StringUtils.isNotBlank(preinfo)) refund.setPreinfo(preinfo);
		refund.setRetback(retback);
		daoService.saveObject(refund);
		User user = getLogonUser();
		monitorService.saveChangeLog(user.getId(), OrderRefund.class, refund.getTradeno(), entry.getChangeMap(refund));
		return showJsonSuccess(model);
	}
	/**
	 * 批量处理更换场次
	 * @param mpid
	 * @param model
	 * @return
	 */
	@RequestMapping("/admin/refund/order/viewOpiOrder.xhtml")
	public String viewOpiOrder(Long mpid, ModelMap model){
		OpenPlayItem opi = daoService.getObjectByUkey(OpenPlayItem.class, "mpid", mpid, true);
		model.put("opi", opi);
		List<TicketOrder> orderList = orderQueryService.getTicketOrderListByMpid(mpid, OrderConstant.STATUS_PAID);
		model.put("orderList", orderList);
		return "admin/refund/order/viewOpiOrder.vm";
	}
	@RequestMapping("/admin/refund/order/submit2Financial.xhtml")
	public String submit2Financial(Long rid, ModelMap model){
		OrderRefund refund = daoService.getObject(OrderRefund.class, rid);
		ChangeEntry entry = new ChangeEntry(refund);
		ErrorCode result = refundService.submit2Financial(refund);
		if(result.isSuccess()){
			User user = getLogonUser();
			if(result.isSuccess()){
				AccountRefund accountRefund = (AccountRefund) result.getRetval();
				accountRefundService.deductAccountRefund(accountRefund, user.getId());
			}
			monitorService.saveChangeLog(user.getId(), OrderRefund.class, refund.getTradeno(), entry.getChangeMap(refund));
			return showMessage(model, "成功提交账户退款至财务！");
		}else{
			return showMessage(model, "提交账户退款失败：" + result.getMsg()); 
		}
	}
	@RequestMapping("/admin/refund/order/changeStatus.xhtml")
	public String changeStatus(Long rid, String status, String dealinfo, ModelMap model){
		OrderRefund refund = daoService.getObject(OrderRefund.class, rid);
		ChangeEntry entry = new ChangeEntry(refund);
		String msg = "状态转换成功:";
		/*if(RefundConstant.STATUS_CANCEL.equals(status)){//转为取消
			if(!RefundConstant.STATUS_PREPAIR.equals(refund.getStatus())) return showJsonError(model, "状态错误：只有预处理才能转为取消！");
			refund.setStatus(status);
			refund.setDealinfo(dealinfo);
			msg +="预处理--->预处理终结";
		}else if(RefundConstant.STATUS_APPLY.equals(status)){//转为申请
			if(!RefundConstant.STATUS_PREPAIR.equals(refund.getStatus())) return showJsonError(model, "状态错误：只有预处理才能转为申请！");
			refund.setStatus(status);
			msg +="预处理--->退款申请";
		}else */if(RefundConstant.STATUS_REJECT.equals(status)){//转为拒绝
			if(!RefundConstant.STATUS_APPLY.equals(refund.getStatus()) && !RefundConstant.STATUS_ACCEPT.equals(refund.getStatus())) return showJsonError(model, "状态错误：只有申请或接受才能转为不接受！");
			refund.setStatus(status);
			refund.setDealinfo(dealinfo);
			msg +="申请--->不接受";
		}else if(RefundConstant.STATUS_ACCEPT.equals(status)){//转为接受
			if(!RefundConstant.STATUS_APPLY.equals(refund.getStatus())) return showJsonError(model, "状态错误：只有申请才能转为接受！");
			if(RefundConstant.REASON_UNKNOWN.equals(refund.getReason())) return showJsonError(model, "请填退款原因！");
			refund.setStatus(status);
			msg +="退款申请--->接受";
		}else{
			return showJsonError(model, "状态错误！");
		}
		User user = getLogonUser();
		refund.setOtherinfo(JsonUtils.addJsonKeyValue(refund.getOtherinfo(), RefundConstant.REFUND_MANAGE_DEAL, user.getId() +","+ user.getUsername()));
		daoService.saveObject(refund);
		monitorService.saveChangeLog(user.getId(), OrderRefund.class, refund.getTradeno(), entry.getChangeMap(refund));
		return showJsonSuccess(model, msg);
	}
	@RequestMapping("/admin/refund/order/processRefund.xhtml")
	public String processRefund(Long rid, ModelMap model){
		OrderRefund refund = daoService.getObject(OrderRefund.class, rid);
		if(!refund.getStatus().equals(RefundConstant.STATUS_ACCEPT) && 
				!refund.getStatus().equals(RefundConstant.STATUS_SUCCESS)){
			return showMessage(model, "状态错误！");
		}
		if(RefundConstant.REASON_UNKNOWN.equals(refund.getReason())) return showMessage(model, "请选择退款原因");
		GewaOrder order = daoService.getObjectByUkey(GewaOrder.class, "tradeNo", refund.getTradeno(), false);
		model.put("order", order);
		model.put("refund", refund);
		boolean expired = refund.gainExpired();
		model.put("expired", expired);
		addViewData(order, model);
		if(RefundConstant.STATUS_ACCEPT.equals(refund.getStatus())){//接受订单
			if(StringUtils.contains(refund.getOpmark(), RefundConstant.OP_CANCEL_TICKET)){
				model.put("cancelTicket", true);
			}
		}else{
			
		}
		return "admin/refund/order/processRefund.vm";
	}
	@RequestMapping("/admin/refund/order/forceCancelTicket.xhtml")
	public String forceCancelTicket(Long rid, ModelMap model){
		OrderRefund refund = daoService.getObject(OrderRefund.class, rid);
		ErrorCode code = refundOperationService.cancelTicket(refund, true, getLogonUser());
		String msg = code.getMsg();
		if(msg == null) msg = "退票失败！";
		return showMessage(model, msg);
	}
	/**
	 * 火凤凰退票、退款
	 * @param rid
	 * @param model
	 * @return
	 */
	@RequestMapping("/admin/refund/order/cancelTicket.xhtml")
	public String cancelTicket(Long rid, ModelMap model){
		OrderRefund refund = daoService.getObject(OrderRefund.class, rid);
		ErrorCode code = refundOperationService.cancelTicket(refund, false, getLogonUser());
		String msg = code.getMsg();
		if(msg == null) msg = "退票失败！";
		return showMessage(model, msg);
	}

	/**
	 * 待处理退款
	 * @param rid
	 * @param model
	 * @return
	 */
	@RequestMapping("/admin/refund/order/confirmRefund.xhtml")
	public String confirmRefund(Long rid, ModelMap model){
		OrderRefund refund = daoService.getObject(OrderRefund.class, rid);
		User user = getLogonUser();
		ErrorCode code = refundOperationService.confirmRefund(refund, user.getId(), user.getUsername());
		String msg = code.getMsg();
		if(msg == null) msg = "退票失败！";
		return showMessage(model, msg);
	}

	@RequestMapping("/admin/refund/saveEmail.xhtml")
	public String saveEmail(String emails, ModelMap model){
		JsonData data = daoService.getObject(JsonData.class, JsonDataKey.KEY_REFUNDNOTIFY);
		if(data==null){ 
			data = new JsonData();
			data.setDkey(JsonDataKey.KEY_REFUNDNOTIFY);
		}
		data.setData(emails);
		daoService.saveObject(data);
		return showJsonSuccess(model);
	}
	
	@RequestMapping("/admin/refund/cinemaSettle.xhtml")
	public String cinemaSettle(Long cinemaid, Timestamp starttime, Timestamp endtime, Integer pageNo, ModelMap model, HttpServletRequest request){
		String citycode = getAdminCitycode(request);
		List<Cinema> cinemaList = daoService.getObjectListByField(Cinema.class, "citycode", citycode);
		Map<Long, Cinema> cinemaMap = BeanUtil.beanListToMap(cinemaList, "id");
		model.put("cinemaMap", cinemaMap);
		if(starttime == null || endtime == null) return "admin/refund/order/settle.vm";
		if(pageNo == null) pageNo = 0;
		Integer rowsPerPage = 50;
		Integer from = pageNo * rowsPerPage;
		SearchRefundCommand command = new SearchRefundCommand();
		command.setRefundtimefrom(starttime);
		command.setRefundtimeto(endtime);
		command.setPlaceid(cinemaid);
		command.setOrdertype(OrderConstant.ORDER_TYPE_TICKET);
		command.setStatus(RefundConstant.STATUS_FINISHED);
		List<OrderRefund> orderRefundList = refundService.getOrderRefundList(command, "placeid", from, rowsPerPage);
		PageUtil pageUtil = new PageUtil(refundService.getOrderRefundCount(command), rowsPerPage, pageNo, "/admin/refund/cinemaSettle.xhtml");
		Map params = new HashMap();
		params.put("cinemaid", cinemaid);
		params.put("starttime", starttime);
		params.put("endtime", endtime);
		pageUtil.initPageInfo(params);
		model.put("pageUtil", pageUtil);
		Map<String, TicketOrder> orderMap = new HashMap<String, TicketOrder>();
		for(OrderRefund orderRefund : orderRefundList){
			TicketOrder ticketOrder = daoService.getObjectByUkey(TicketOrder.class, "tradeNo", orderRefund.getTradeno(), true);
			orderMap.put(orderRefund.getTradeno(), ticketOrder);
		}
		model.put("orderMap", orderMap);
		model.put("orderRefundList", orderRefundList);
		model.put("cinemaid", cinemaid);
		model.put("starttime", starttime);
		model.put("endtime", endtime);
		return "admin/refund/order/settle.vm";
	}
	
	@RequestMapping("/admin/refund/backRemoteOrder.xhtml")
	public String backRemoteOrder(String tradeno, ModelMap model){
		TicketOrder order = daoService.getObjectByUkey(TicketOrder.class, "tradeNo", tradeno, true);
		if(order == null) return showJsonError(model, "订单不存在或被删除！");
		OpenPlayItem opi = daoService.getObjectByUkey(OpenPlayItem.class, "mpid", order.getMpid(), true);
		ErrorCode code = ticketOperationService.backRemoteOrder(getLogonUser(), order, opi);
		if(!code.isSuccess()) return showJsonError(model, code.getMsg());
		return showJsonSuccess(model);
	}
}
