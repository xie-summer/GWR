package com.gewara.web.action.inner.mobile.drama;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import com.gewara.command.TheatrePriceCommand;
import com.gewara.constant.ApiConstant;
import com.gewara.constant.OdiConstant;
import com.gewara.constant.TagConstant;
import com.gewara.model.api.ApiUser;
import com.gewara.model.drama.DramaOrder;
import com.gewara.model.drama.OpenDramaItem;
import com.gewara.model.drama.TheatreSeatArea;
import com.gewara.model.express.ExpressConfig;
import com.gewara.model.express.ExpressProvince;
import com.gewara.model.pay.OrderAddress;
import com.gewara.model.user.Member;
import com.gewara.model.user.MemberUsefulAddress;
import com.gewara.support.ErrorCode;
import com.gewara.util.DateUtil;
import com.gewara.util.JsonUtils;
import com.gewara.web.action.api.ApiAuth;
import com.gewara.web.action.inner.mobile.BaseOpenApiMobileDramaController;
import com.gewara.web.filter.OpenApiMobileAuthenticationFilter;

@Controller
public class OpenApiMobileDramaOrderController extends BaseOpenApiMobileDramaController {
	/**
	 * 下话剧订单
	 */
	@RequestMapping("/openapi/mobile/drama/addDramaOrder.xhtml")
	public String addTicketOrder(HttpServletRequest request, Long dpid, Long areaid, String mobile, String seatLabel, Long priceid, 
			Integer quantity, Long disid, Long addressid, String origin, ModelMap model){
		ApiAuth auth = OpenApiMobileAuthenticationFilter.getOpenApiAuth();
		ApiUser partner = auth.getApiUser();
		Member member = OpenApiMobileAuthenticationFilter.getOpenApiAuth().getMember();
		OpenDramaItem odi = daoService.getObjectByUkey(OpenDramaItem.class, "dpid", dpid, true);
		if(odi==null) return getErrorXmlView(model, ApiConstant.CODE_OPI_NOT_EXISTS, "场次不存在！");
		TheatreSeatArea seatArea = daoService.getObject(TheatreSeatArea.class, areaid);
		if(seatArea == null) return getErrorXmlView(model, ApiConstant.CODE_DATA_ERROR, "场区不存在！");
		ErrorCode<ExpAddress> eacode = getExpAddress(odi, addressid, member);
		if(!eacode.isSuccess()){
			return getErrorXmlView(model, ApiConstant.CODE_DATA_ERROR,  eacode.getMsg());
		}
		ExpAddress expAddress = eacode.getRetval();
		ErrorCode<DramaOrder> code = null;
		if(expAddress!=null){
			code = theatreOrderService.addDramaOrder(odi, member, mobile, quantity, disid, priceid, partner, null, null);
		}else {
			code = theatreOrderService.addDramaOrder(odi, seatArea, seatLabel, null, mobile, member, partner, null);
		}
		if(!code.isSuccess()){
			return getErrorXmlView(model, ApiConstant.CODE_DATA_ERROR, code.getMsg());
		}
		DramaOrder order = code.getRetval();
		
		ErrorCode othercode = createOrderOther(order, expAddress, member);
		if(!othercode.isSuccess()){
			return getErrorXmlView(model, ApiConstant.CODE_DATA_ERROR, othercode.getMsg());
		}
		logAppSourceOrder(request, order, TagConstant.TAG_CINEMA, origin);
		return getSingleResultXmlView(model, order.getTradeNo());
	}
	/**
	 * 下话剧订单
	 */
	@RequestMapping("/openapi/mobile/drama/addDramaOrderByPrice.xhtml")
	public String addTicketOrder(String pricelist, String mobile, Long addressid, ModelMap model){
		ApiAuth auth = OpenApiMobileAuthenticationFilter.getOpenApiAuth();
		Member member = OpenApiMobileAuthenticationFilter.getOpenApiAuth().getMember();
		String ukey = member.getId().toString();
		List<TheatrePriceCommand> commandList = new ArrayList<TheatrePriceCommand>();
		Long dpid = null;
		try{
			commandList = JsonUtils.readJsonToObjectList(TheatrePriceCommand.class, pricelist);
		}catch (Exception e) {
			return getErrorXmlView(model, ApiConstant.CODE_DATA_ERROR,  "场次或价格错误！");
		}
		dpid = commandList.get(0).getItemid();
		OpenDramaItem odi = daoService.getObjectByUkey(OpenDramaItem.class, "dpid", dpid, true);
		ErrorCode<ExpAddress> eacode = getExpAddress(odi, addressid, member);
		if(!eacode.isSuccess()){
			return getErrorXmlView(model, ApiConstant.CODE_DATA_ERROR,  eacode.getMsg());
		}
		ErrorCode<DramaOrder> code = theatreOrderService.addDramaOrder(pricelist, member, mobile, auth.getApiUser(), ukey);
		if(!code.isSuccess()){
			return getErrorXmlView(model, ApiConstant.CODE_DATA_ERROR, code.getMsg());
		}
		DramaOrder order = code.getRetval();
		ExpAddress expAddress = eacode.getRetval();
		ErrorCode othercode = createOrderOther(order, expAddress, member);
		if(!othercode.isSuccess()){
			return getErrorXmlView(model, ApiConstant.CODE_DATA_ERROR, othercode.getMsg());
		}
		return getSingleResultXmlView(model, code.getRetval().getTradeNo());
	}
	private ErrorCode<ExpAddress> getExpAddress(OpenDramaItem odi, Long addressid, Member member){
		ExpressConfig expressConfig = null;
		MemberUsefulAddress memberUsefulAddress = null;
		String takemethod = dramaOrderService.getTakemethodByOdi(DateUtil.getCurFullTimestamp(), odi);
		ExpAddress expAddress = null;
		if(addressid!=null){
			if(StringUtils.equals(takemethod, OdiConstant.TAKEMETHOD_QUPIAOJI)){
				return ErrorCode.getFailure("该场次只支持电子票");
			}
			memberUsefulAddress = daoService.getObject(MemberUsefulAddress.class, addressid);
			ErrorCode acode = dramaOrderService.validMemberUserfulAddress(memberUsefulAddress);
			if(!acode.isSuccess()){
				return ErrorCode.getFailure(acode.getMsg());
			}
			if(!member.getId().equals(memberUsefulAddress.getMemberid())){
				return ErrorCode.getFailure("不能操作信息！");
			}
			expressConfig = daoService.getObject(ExpressConfig.class, odi.getExpressid());
			ErrorCode<ExpressProvince> expcode = ticketOrderService.getExpressFee(expressConfig, memberUsefulAddress.getProvincecode());
			if(!expcode.isSuccess()){
				return ErrorCode.getFailure(expcode.getMsg());
			}
			expAddress = new ExpAddress(expressConfig, memberUsefulAddress);
		}else {
			if(StringUtils.equals(takemethod, OdiConstant.TAKEMETHOD_KUAIDI)){
				return ErrorCode.getFailure("请选择快递地址！");
			}
		}
		return ErrorCode.getSuccessReturn(expAddress);
	}
	private ErrorCode createOrderOther(DramaOrder order, ExpAddress expAddress, Member member){
		if(expAddress!=null){
			ErrorCode<OrderAddress> acode = ticketOrderService.createOrderAddress(order, expAddress.getMemberUsefulAddress(), expAddress.getExpressConfig());
			if(!acode.isSuccess()){
				theatreOrderService.cancelDramaOrder(order, member.getId()+"", "系统取消");
				return ErrorCode.getFailure(acode.getMsg());
			}
			ErrorCode<Integer> code2 = ticketOrderService.computeExpressFee(order, expAddress.getExpressConfig(), acode.getRetval().getProvincecode());
			if(!code2.isSuccess()){
				theatreOrderService.cancelDramaOrder(order, member.getId()+"", "系统取消");
				return ErrorCode.getFailure(code2.getMsg());
			}
		}
		return ErrorCode.SUCCESS;
	}
	/**
	 * 修改订单的快递地址
	 */
	@RequestMapping("/openapi/mobile/drama/modifyOrderAddress.xhtml")
	public String addTicketOrder(String tradeNo, Long addressid, ModelMap model){
		Member member = OpenApiMobileAuthenticationFilter.getOpenApiAuth().getMember();
		DramaOrder order = daoService.getObjectByUkey(DramaOrder.class, "tradeNo", tradeNo, false);
		if(order == null) return getErrorXmlView(model, ApiConstant.CODE_DATA_ERROR, "订单不存在！");
		if (order.isAllPaid() || order.isCancel()) return getErrorXmlView(model, ApiConstant.CODE_DATA_ERROR, "不能操作已支付或已（过时）取消的订单！");
		if(!order.getMemberid().equals(member.getId())) return getErrorXmlView(model, ApiConstant.CODE_DATA_ERROR, "不能操作他人订单！");
		OpenDramaItem odi = daoService.getObjectByUkey(OpenDramaItem.class, "dpid", order.getDpid(), true);
		if(StringUtils.isNotBlank(odi.getExpressid())){
			ExpressConfig expressConfig = daoService.getObject(ExpressConfig.class, odi.getExpressid());
			MemberUsefulAddress memberUsefulAddress = daoService.getObject(MemberUsefulAddress.class, addressid);
			if(!member.getId().equals(memberUsefulAddress.getMemberid())){
				return getErrorXmlView(model, ApiConstant.CODE_DATA_ERROR, "不能操作他人订单！");
			}
			ErrorCode<OrderAddress> acode = ticketOrderService.createOrderAddress(order, memberUsefulAddress, expressConfig);
			if(!acode.isSuccess()){
				theatreOrderService.cancelDramaOrder(order, member.getId()+"", "系统取消");
				return getErrorXmlView(model, ApiConstant.CODE_DATA_ERROR, acode.getMsg());
			}
			ErrorCode<Integer> code2 = ticketOrderService.computeExpressFee(order, expressConfig, acode.getRetval().getProvincecode());
			if(!code2.isSuccess()){
				theatreOrderService.cancelDramaOrder(order, member.getId()+"", "系统取消");
				return getErrorXmlView(model, ApiConstant.CODE_DATA_ERROR, code2.getMsg());
			}
		}
		return getSingleResultXmlView(model, order.getTradeNo());
	}
	class ExpAddress{
		private ExpressConfig expressConfig;
		private MemberUsefulAddress memberUsefulAddress;
		public ExpAddress(ExpressConfig expressConfig, MemberUsefulAddress memberUsefulAddress){
			this.expressConfig = expressConfig;
			this.memberUsefulAddress = memberUsefulAddress;
		}
		public ExpressConfig getExpressConfig() {
			return expressConfig;
		}
		public void setExpressConfig(ExpressConfig expressConfig) {
			this.expressConfig = expressConfig;
		}
		public MemberUsefulAddress getMemberUsefulAddress() {
			return memberUsefulAddress;
		}
		public void setMemberUsefulAddress(MemberUsefulAddress memberUsefulAddress) {
			this.memberUsefulAddress = memberUsefulAddress;
		}
		
	}
}
