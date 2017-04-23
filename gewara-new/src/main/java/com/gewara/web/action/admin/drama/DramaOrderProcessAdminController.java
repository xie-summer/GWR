package com.gewara.web.action.admin.drama;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import com.gewara.constant.ticket.OpiConstant;
import com.gewara.model.acl.User;
import com.gewara.model.drama.DramaOrder;
import com.gewara.model.drama.OpenDramaItem;
import com.gewara.model.drama.OpenTheatreSeat;
import com.gewara.model.drama.SellDramaSeat;
import com.gewara.model.drama.TheatreSeatArea;
import com.gewara.model.pay.BuyItem;
import com.gewara.model.pay.GewaOrder;
import com.gewara.model.pay.GoodsOrder;
import com.gewara.service.drama.DramaOrderService;
import com.gewara.service.drama.DramaProcessService;
import com.gewara.support.ErrorCode;
import com.gewara.untrans.drama.DramaOrderProcessService;
import com.gewara.untrans.drama.TheatreOperationService;
import com.gewara.untrans.ticket.OrderProcessService;
import com.gewara.web.action.admin.BaseAdminController;

@Controller
public class DramaOrderProcessAdminController extends BaseAdminController {

	@Autowired@Qualifier("dramaOrderService")
	private DramaOrderService dramaOrderService;
	
	@Autowired@Qualifier("theatreOperationService")
	private TheatreOperationService theatreOperationService;
	
	@Autowired@Qualifier("dramaProcessService")
	private DramaProcessService dramaProcessService;
	
	@Autowired@Qualifier("dramaOrderProcessService")
	private DramaOrderProcessService dramaOrderProcessService;
	
	@Autowired@Qualifier("orderProcessService")
	private OrderProcessService orderProcessService;
	
	@RequestMapping("/admin/dramaTicket/changeSeat.xhtml")
	public String changeSeat(Long orderid, String newseat, ModelMap model){
		User user = getLogonUser();
		boolean allow = operationService.updateOperation("procRep" + orderid, 60);
		if(!allow) return showJsonError(model, "他人(系统)正在处理，请等待1分钟！");
		DramaOrder oldOrder = daoService.getObject(DramaOrder.class, orderid);
		OpenDramaItem odi = daoService.getObjectByUkey(OpenDramaItem.class, "dpid", oldOrder.getDpid(), false);
		if(!odi.isOpenseat()) return showJsonError(model, "该场次不是按照座位开放！");
		try {
			List<SellDramaSeat> seatList = dramaOrderService.getDramaOrderSeatList(orderid);
			TheatreSeatArea seatArea = daoService.getObject(TheatreSeatArea.class, oldOrder.getAreaid());
			ErrorCode<List<String>> remoteLockList = theatreOperationService.updateRemoteLockSeat(seatArea, OpiConstant.SECONDS_ADDORDER, false);
			if(!remoteLockList.isSuccess()){
				return showJsonError(model, "订单失败，场馆网络异常！");
			}
			theatreOperationService.releasePaidFailureOrderSeat(oldOrder, seatList);
			List<OpenTheatreSeat> oseatList = dramaOrderService.getNewSeatList(oldOrder, seatList, newseat, true);
			DramaOrder order = dramaProcessService.changeSeat(odi, oseatList, oldOrder, true, remoteLockList.getRetval());
			if(order!=null){
				List<SellDramaSeat> newSeatList = dramaOrderService.getDramaOrderSeatList(order.getId());
				theatreOperationService.createDramaRemoteOrder(odi, order, order.getMobile(), newSeatList, new ArrayList<BuyItem>());
				reConfirm(order.getTradeNo(), model);
			}
			operationService.resetOperation("procRep" + orderid, 5);
			dbLogger.warn(user.getNickname()+user.getId()+ "更改座位");
			return showJsonSuccess(model);
		} catch (Exception e) {
			dbLogger.error("错误", e);
			return showJsonError(model, e.getMessage());
		}
	}
	@RequestMapping("/admin/dramaTicket/reConfirmOrder.xhtml")
	public String userConfirmSuccess(ModelMap model, Long orderId, String forceReConfirm){
		boolean reChange = StringUtils.isNotBlank(forceReConfirm);
		User user = getLogonUser();
		DramaOrder order = daoService.getObject(DramaOrder.class, orderId);
		ErrorCode result = dramaOrderProcessService.reconfirmOrder(order, user.getId(), false, reChange);
		if(result.isSuccess()) return showMessage(model, "成功处理！");
		else return forwardMessage(model, result.getMsg());
	}

	@RequestMapping("/admin/dramaTicket/failConfirm.xhtml")
	public String reConfirm(String tradeNo, ModelMap model){
		GewaOrder order = daoService.getObjectByUkey(GewaOrder.class, "tradeNo", tradeNo, false);
		if(order instanceof GoodsOrder || order instanceof DramaOrder){
			ErrorCode result = orderProcessService.processOrder(order, "重新确认", null);
			if(result.isSuccess()) return showJsonSuccess(model);
			else return showJsonError(model, result.getMsg());
		}
		return showJsonError(model, "订单错误！");
	}
}
