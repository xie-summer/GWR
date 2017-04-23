package com.gewara.web.action.api2partner;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.betwixt.io.BeanReader;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import com.gewara.constant.ApiConstant;
import com.gewara.constant.ticket.PartnerConstant;
import com.gewara.model.api.ApiUser;
import com.gewara.model.pay.TicketOrder;
import com.gewara.model.user.Member;
import com.gewara.model.user.OpenMember;
import com.gewara.pay.PayBoxPayUtil;
import com.gewara.support.ErrorCode;
import com.gewara.util.ApiUtils;
import com.gewara.util.StringUtil;
import com.gewara.util.WebUtils;
import com.gewara.web.action.api.BaseApiController;
import com.gewara.xmlbind.partner.IBoxPay;
import com.gewara.xmlbind.partner.IBoxPayResult;
import com.gewara.xmlbind.partner.PartnerBoxPayUser;

@Controller
public class Api2ParnterBoxPayController extends BaseApiController {
	private ApiUser getBoxPay(){
		return daoService.getObject(ApiUser.class, PartnerConstant.PARTNER_BOX_PAY);
	}
	@RequestMapping("/api2/partner/boxpay/toCheckLogin.xhtml")
	public String boxLoginBind(String token,String iboxUserId,ModelMap model,HttpServletRequest request,HttpServletResponse response){
		ApiUser partner = this.getBoxPay();
		String checkUserResult = PayBoxPayUtil.getCheckBoxLogin(partner,token,iboxUserId);
		if("fail".equals(checkUserResult)){
			return getErrorXmlView(model, ApiConstant.CODE_DATA_ERROR, "用户登录校验未通过");
		}
		BeanReader beanReader = ApiUtils.getBeanReader("iboxpay",IBoxPay.class);
		IBoxPay boxpay = (IBoxPay)ApiUtils.xml2Object(beanReader, checkUserResult);
		IBoxPayResult result = boxpay.getResult();
		if(result == null || !"0".equals(boxpay.getResult().getRespCode())){
			return getErrorXmlView(model, ApiConstant.CODE_DATA_ERROR, "用户登录校验未通过");
		}
		PartnerBoxPayUser user = result.getResponse();
		if(!this.checkSign(user.getSignMsg(), new String[]{"iboxUserId","parterId","partnerUserId","result","signType","token"}, 
				user.getIboxUserId(),user.getParterId(),user.getPartnerUserId(),user.getResult(),user.getSignType(),user.getToken())){
			return getErrorXmlView(model, ApiConstant.CODE_DATA_ERROR, "用户登录校验未通过");
		}
		OpenMember openMember = memberService.getOpenMemberByLoginname("boxpay", user.getIboxUserId());
		if(openMember == null){
			openMember = memberService.createOpenMember(WebUtils.getAndSetDefault(request, response), "boxpay","b", user.getIboxUserId(), WebUtils.getRemoteIp(request));
		}
		Member member = daoService.getObject(Member.class, openMember.getMemberid());
		model.put("member", member);
		model.put("boxUserId", iboxUserId);
		model.put("token", token);
		if(StringUtils.isBlank(user.getPartnerUserId())){
			String bindResult = PayBoxPayUtil.bindUser(token, member.getId() + "", user.getIboxUserId(),partner);
			boxpay = (IBoxPay)ApiUtils.xml2Object(beanReader, bindResult);
			result = boxpay.getResult();
			if(result == null || !"0".equals(result.getRespCode())){
				return getErrorXmlView(model, ApiConstant.CODE_DATA_ERROR, result.getErrorDesc());
			}
			user = result.getResponse();
			if(!this.checkSign(user.getSignMsg(), new String[]{"iboxUserId","parterId","partnerUserId","result","signType","token"}, 
					user.getIboxUserId(),user.getParterId(),user.getPartnerUserId(),user.getResult(),user.getSignType(),user.getToken())){
				return getErrorXmlView(model, ApiConstant.CODE_DATA_ERROR, "用户与box绑定出错");
			}
		}
		ErrorCode<String> encodeResult = memberService.getAndSetMemberEncode(member);
		model.put("memberEncode", encodeResult.getRetval());
		return this.getXmlView(model, "/api/box/member.vm");
	}
	@RequestMapping("/api2/partner/boxpay/addOrderToBoxPay.xhtml")
	public String addOrderToBoxPay(String orderId,String memberEncode,ModelMap model){
		if(StringUtils.isBlank(memberEncode)){
			return getErrorXmlView(model, ApiConstant.CODE_DATA_ERROR, "memberEncode 不能为空");
		}
		Member member =  memberService.getMemberByEncode(memberEncode);
		if(member == null){
			return getErrorXmlView(model, ApiConstant.CODE_DATA_ERROR, "用户为空");
		}
		OpenMember openMember = memberService.getOpenMemberByMemberid("boxpay", member.getId());
		if(openMember == null){
			return getErrorXmlView(model, ApiConstant.CODE_DATA_ERROR, "此盒子用户尚未在格瓦拉平台登录");
		}
		TicketOrder order = daoService.getObjectByUkey(TicketOrder.class, "tradeNo", orderId, true);
		if(order == null){
			return getErrorXmlView(model, ApiConstant.CODE_DATA_ERROR, "订单不存在");
		}
		if(!order.getMemberid().equals(member.getId())){
			return getErrorXmlView(model, ApiConstant.CODE_DATA_ERROR,"不能修改他人的订单！");
		}
		if(order.isAllPaid() || order.isCancel()) {
			return getErrorXmlView(model, ApiConstant.CODE_DATA_ERROR, "不能修改已支付或已（过时）取消的订单！");
		}
		String result = PayBoxPayUtil.saveOrder(order, this.getBoxPay(),openMember.getLoginname());
		if("fail".equals(result)){
			return getErrorXmlView(model, ApiConstant.CODE_DATA_ERROR, "添加订单到boxPay失败");
		}
		BeanReader beanReader = ApiUtils.getBeanReader("iboxpay",IBoxPay.class);
		IBoxPay boxpay = (IBoxPay)ApiUtils.xml2Object(beanReader, result);
		IBoxPayResult payResult = boxpay.getResult();
		if(payResult == null || !"0".equals(payResult.getRespCode())){
			return getErrorXmlView(model, ApiConstant.CODE_DATA_ERROR, "添加订单到boxPay失败");
		}
		PartnerBoxPayUser boxPayResponse = payResult.getResponse();
		if(!this.checkSign(boxPayResponse.getSignMsg(), new String[]{"bizType","callbackUrl","createTime","cutOffTime","iboxUserId","orderAmount","orderNo","orderSerial","orderTime","parterId","signType"}, 
				boxPayResponse.getBizType(),boxPayResponse.getCallbackUrl(),boxPayResponse.getCreateTime(),boxPayResponse.getCutOffTime(),boxPayResponse.getIboxUserId(),boxPayResponse.getOrderAmount(),
				boxPayResponse.getOrderNo(),boxPayResponse.getOrderSerial(),boxPayResponse.getOrderTime(),boxPayResponse.getParterId(),boxPayResponse.getSignType())){
			return getErrorXmlView(model, ApiConstant.CODE_DATA_ERROR, "添加订单到boxPay失败");
		}
		model.put("orderSerial", boxPayResponse.getOrderSerial());
		return this.getXmlView(model, "/api/box/result.vm");
	}
	private boolean checkSign(String signMsg,String[] keys,String ...strs){
		StringBuilder sb = new StringBuilder();
		if(strs != null){
			if(keys.length == strs.length){
				int i = 0;
				for(String key : keys){
					if(StringUtils.isNotBlank(strs[i])){
						sb.append(key).append("=").append(strs[i]).append("&");
					}
					i++;
				}
			}else{
				return false;
			}
		}else{
			return false;
		}
		sb.append("key=").append(PayBoxPayUtil.getBoxKey());
		String vilaSign = sb.toString();
		this.dbLogger.warn(vilaSign );
		try {
			if(signMsg.equals(StringUtil.md5(URLEncoder.encode(vilaSign, "UTF-8")).toUpperCase())){
				return true;
			}
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return false;
		}
		return false;
	}
	
}
