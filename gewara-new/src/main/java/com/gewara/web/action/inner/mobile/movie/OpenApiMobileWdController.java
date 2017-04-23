package com.gewara.web.action.inner.mobile.movie;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import com.gewara.constant.ApiConstant;
import com.gewara.constant.ticket.OrderConstant;
import com.gewara.helper.order.TicketOrderContainer;
import com.gewara.model.api.ApiUser;
import com.gewara.model.pay.TicketOrder;
import com.gewara.model.ticket.OpenPlayItem;
import com.gewara.model.user.Member;
import com.gewara.service.OrderException;
import com.gewara.service.ticket.WandaService;
import com.gewara.support.ErrorCode;
import com.gewara.untrans.ticket.RemoteTicketService;
import com.gewara.util.DateUtil;
import com.gewara.util.StringUtil;
import com.gewara.web.action.inner.OpenApiAuth;
import com.gewara.web.action.inner.mobile.BaseOpenApiMobileMovieController;
import com.gewara.web.filter.OpenApiMobileAuthenticationFilter;
import com.gewara.xmlbind.ticket.TicketRemoteOrder;
@Controller
public class OpenApiMobileWdController  extends BaseOpenApiMobileMovieController{
	@Autowired@Qualifier("wandaService")
	private WandaService wandaService;
	@Autowired@Qualifier("remoteTicketService")
	private RemoteTicketService remoteTicketService;
	@RequestMapping("/openapi/mobile/playItem/opiSeatInfoByWd.xhtml")
	public String opiSeatInfoByWd(Long mpid, String mobile, String callback, Long memberid, String membername, String relkey, ModelMap model){
		OpenPlayItem opi = daoService.getObjectByUkey(OpenPlayItem.class, "mpid", mpid, false);
		if(opi == null) return getErrorXmlView(model, ApiConstant.CODE_OPI_NOT_EXISTS, "场次不存在或已删除！");
		OpenApiAuth auth = OpenApiMobileAuthenticationFilter.getOpenApiAuth();
		Member member = auth.getMember();
		if(!member.getId().equals(memberid)){
			return getErrorXmlView(model, ApiConstant.CODE_OPI_NOT_EXISTS, "非法操作！");
		}
		ErrorCode<String> code = wandaService.getWapSeatPage(opi.getSeqNo(), mpid, memberid, membername, mobile, callback, relkey);
		if(!code.isSuccess()) return getErrorXmlView(model, code.getErrcode(), code.getMsg());
		return getSingleResultXmlView(model, code.getRetval());
	}
	
	@RequestMapping("/openapi/mobile/order/createWdWapOrder.xhtml")
	public String opiSeatInfoByWd(String key, String snid, ModelMap model){
		ErrorCode<TicketOrder> code = createWdOrder(key, snid);
		if(!code.isSuccess()){
			return getErrorXmlView(model, ApiConstant.CODE_DATA_ERROR, code.getMsg()); 
		}
		return getSingleResultXmlView(model, code.getRetval().getTradeNo());
	}
	
	private ErrorCode<TicketOrder> createWdOrder(String key, String wdOrderId){
		ErrorCode<Map<String, Object>> retCode = wandaService.validCreateWdOrder(key, null, wdOrderId);
		if(!retCode.isSuccess()) {
			return ErrorCode.getFailure(retCode.getMsg());
		}
		Map<String, Object> retMap = retCode.getRetval();
		String mobile = retMap.get("mobile")+"";
		String seqno = retMap.get("seqno")+"";
		String memberid = retMap.get("memberid")+"";
		String relkey = retMap.get("relkey")+"";
		ApiUser partner = daoService.getObjectByUkey(ApiUser.class, "partnerkey", relkey);
		OpenPlayItem opi = (OpenPlayItem)retMap.get("opi");
		String tradeNo = ticketOrderService.getTicketTradeNo();
		ErrorCode<TicketRemoteOrder> result = remoteTicketService.getWdRemoteOrder(wdOrderId, opi.getSeqNo(), opi.getCinemaid());
		if(result.isSuccess()){
			TicketRemoteOrder wdOrder = result.getRetval();
			String randomNum = ticketOrderService.nextRandomNum(DateUtil.addDay(opi.getPlaytime(), 1), 8, "0");
			try{
				Member member = daoService.getObject(Member.class, Long.valueOf(memberid));
				TicketOrderContainer orderContainer = wandaService.createTicketOrder(opi, member.getId(), member.getNickname(), ""+member.getId(), mobile, wdOrder, randomNum, tradeNo, partner);
				TicketOrder order = orderContainer.getTicketOrder();
				ErrorCode<TicketRemoteOrder> remoteOrder = remoteTicketService.createWdRemoteOrder(seqno, wdOrderId, order.getId(), opi.getCinemaid());
				if(remoteOrder.isSuccess()){
					order.setStatus(OrderConstant.STATUS_NEW);
					daoService.saveObject(order);
					return ErrorCode.getSuccessReturn(order);
				}else{
					return ErrorCode.getFailure(remoteOrder.getMsg());
				}
			}catch(OrderException e){
				remoteTicketService.unlockWandaOrder(wdOrderId, opi.getCinemaid());
				dbLogger.warn(StringUtil.getExceptionTrace(e, 5));
				return ErrorCode.getFailure(result.getMsg());
			}
		}else{
			remoteTicketService.unlockWandaOrder(wdOrderId, opi.getCinemaid());
		}
		return ErrorCode.getFailure(result.getMsg());
	}
}
