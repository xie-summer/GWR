package com.gewara.web.action.partner;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.betwixt.io.BeanReader;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.gewara.Config;
import com.gewara.constant.ApiConstant;
import com.gewara.constant.PaymethodConstant;
import com.gewara.constant.sys.CacheConstant;
import com.gewara.constant.sys.LogTypeConstant;
import com.gewara.constant.ticket.OrderProcessConstant;
import com.gewara.constant.ticket.PartnerConstant;
import com.gewara.jms.JmsConstant;
import com.gewara.model.api.ApiUser;
import com.gewara.model.pay.GewaOrder;
import com.gewara.model.pay.TicketOrder;
import com.gewara.model.user.Member;
import com.gewara.model.user.OpenMember;
import com.gewara.pay.PartnerPayUtil;
import com.gewara.pay.PayBoxPayUtil;
import com.gewara.service.gewapay.PaymentService;
import com.gewara.support.ErrorCode;
import com.gewara.untrans.CacheService;
import com.gewara.untrans.JmsService;
import com.gewara.untrans.monitor.RoleTag;
import com.gewara.util.ApiUtils;
import com.gewara.util.DateUtil;
import com.gewara.util.StringUtil;
import com.gewara.util.WebUtils;
import com.gewara.web.action.api.ApiAuth;
import com.gewara.web.action.api.BaseApiController;
import com.gewara.xmlbind.partner.IBoxPay;
import com.gewara.xmlbind.partner.IBoxPayResult;
import com.gewara.xmlbind.partner.PartnerBoxPayUser;

@Controller
public class PartnerBoxPayController extends BaseApiController{
	@Autowired@Qualifier("paymentService")
	private PaymentService paymentService;
	public void setPaymentService(PaymentService paymentService) {
		this.paymentService = paymentService;
	}

	@Autowired@Qualifier("jmsService")
	private JmsService jmsService;
	public void setJmsService(JmsService jmsService) {
		this.jmsService = jmsService;
	}
	@Autowired@Qualifier("cacheService")
	private CacheService cacheService;
	public void setCacheService(CacheService cacheService) {
		this.cacheService = cacheService;
	}
	private ApiUser getBoxPay(){
		return daoService.getObject(ApiUser.class, PartnerConstant.PARTNER_BOX_PAY);
	}
	
	@RequestMapping("/api/partner/boxpay/toCheckLogin.xhtml")
	public String boxLoginBind(String key,String encryptCode,String token,String iboxUserId,ModelMap model,HttpServletRequest request,HttpServletResponse response){
		ApiAuth apiAuth = this.check(key, encryptCode, request);
		if(!apiAuth.isChecked()){
			return getErrorXmlView(model, apiAuth.getCode(), apiAuth.getMsg());
		}
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
	@RequestMapping("/api/partner/boxpay/addOrderToBoxPay.xhtml")
	public String addOrderToBoxPay(String key,String encryptCode,String orderId,String memberEncode,ModelMap model,HttpServletRequest request){
		ApiAuth apiAuth = this.check(key, encryptCode, request);
		if(!apiAuth.isChecked()){
			return getErrorXmlView(model, apiAuth.getCode(), apiAuth.getMsg());
		}
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
	
	/**
	 * @param  payStatus	响应码	String(10)	Y	0 成功，其它 失败
	 * @param  errorDesc	错误描述	String(100)	N	失败时返回
		成功时不返回
		成功响应参数
	 * @param 	parterId	合作方ID	String(100)	Y	合作ID
	 * @param 	bizType	业务类型	Number(2)	Y	
	 * @param 	orderNo	订单流水号	String(50)	Y	商户订单流水号
	 * @param 	orderTime	订单时间	String(15)	Y	商户订单时间:格式
	  	YYYY-MM-DD hh:mm:ss
	 * @param 	orderAmount	订单金额	Number(15)	Y	商户订单金额
		精确到分
	 * @param 	tradeNo	交易流水号	String(20)	Y	盒子支付流水号
	 * @param 	tradeAmount	交易金额	Number(15)	Y	盒子支付交易金额
		精确到分
	 * @param 	tradeTime	交易时间	String(15)	Y	交易时间：格式
		YYYY-MM-DD hh:mm:ss
	 * @param 	signType	签名类型	Number(1)	Y	1,MD5  2 ,RSA
	 * @param signMsg	签名	String(300)	Y	
	 */
	// 改变数据库状态
	@RequestMapping("/partner/pay/boxPayNotify.xhtml")
	@ResponseBody
	public String boxPayNotify(HttpServletRequest request, String payStatus,String errorDesc,String parterId,String bizType,
			String orderNo,String orderTime,String orderAmount,String tradeNo,String tradeAmount,String tradeTime,String signType,
			String signMsg,String orderSerial,String sysRefNo) {
		String params = WebUtils.getParamStr(request, true);
		String headers = WebUtils.getHeaderStr(request);
		dbLogger.error("商家订单调用Param:" + params);
		dbLogger.error("商家订单调用Header:" + headers);
		if(!"0".equals(payStatus)){
			return orderNo + "|" + tradeNo + "|" + errorDesc;
		}
		ApiUser partner = this.getBoxPay();
		if (partner == null) {
			dbLogger.error("商家不存在");
			monitorService.saveSysWarn("商家API错误：商家不存在,params:",  params + "\nheader:" + headers, RoleTag.jishu);
			return orderNo + "|" + tradeNo + "|partner not exists";
		}
		if(!partner.isRole(ApiUser.ROLE_PAYORDER)){
			dbLogger.error("不支持商家支付");
			monitorService.saveSysWarn("商家API错误：不支持商家支付,params:",  params + "\nheader:" + headers, RoleTag.jishu);
			return orderNo + "|" + tradeNo + "|pay not supported";
		}
		boolean valid = checkSign(signMsg,new String[]{"bizType","orderAmount","orderNo","orderSerial","orderTime","parterId","payStatus","signType","sysRefNo","tradeAmount","tradeNo","tradeTime"}, 
				bizType,orderAmount,orderNo,orderSerial,orderTime,parterId,payStatus,signType,sysRefNo,tradeAmount,tradeNo,tradeTime);
		if(valid && queryOrderValid(orderNo,orderSerial,partner)) {
			dbLogger.warnWithType(LogTypeConstant.LOG_TYPE_ORDER_PAY, "签名成功" + orderNo);
			orderMonitorService.addOrderPayCallback(tradeNo, OrderProcessConstant.CALLTYPE_NOTIFY, PaymethodConstant.PAYMETHOD_PARTNERPAY, params + ",host=" + Config.getServerIp());
			String remoteIp = WebUtils.getRemoteIp(request);
			if(!PartnerPayUtil.isValidIp(remoteIp, partner)){//非法IP调用，报警
				dbLogger.error("商家付款非法IP调用");
				monitorService.saveSysWarn("商家API错误：商家付款非法IP调用,params:", params + "\nheader:" + headers, RoleTag.jishu);
				dbLogger.error("商家付款非法IP调用：" + remoteIp);
			}
			 //可以在不同状态下获取订单信息，操作商家数据库使数据同步 
			int fee = new Double(tradeAmount).intValue()/100;
			try{
				ErrorCode<GewaOrder> result = paymentService.netPayOrder(orderNo, tradeNo, fee, PaymethodConstant.PAYMETHOD_PARTNERPAY, "bk", partner.getBriefname());
				if(result.isSuccess()) processPay(orderNo, partner.getBriefname());
				TicketOrder order = daoService.getObjectByUkey(TicketOrder.class, "tradeNo", orderNo, false);
				if (order == null) {
					monitorService.saveSysWarn("商家API错误：商家订单不存在,params:", params + "\nheader:" + headers, RoleTag.jishu);
					return orderNo + "|" + tradeNo + "|order not exists";
				}
			}catch(Exception e){
				dbLogger.warnWithType(LogTypeConstant.LOG_TYPE_ORDER_PAY, "", e);
				monitorService.saveSysWarn("订单付款调用失败，尽快处理", "订单号：" +  orderNo, RoleTag.dingpiao);
			}
			return "success";
		}else{
			dbLogger.error("非法的订单信息");
			monitorService.saveSysWarn("商家API错误：非法的订单信息,params:", params + "\nheader:" + headers, RoleTag.jishu);
			return orderNo + "|" + tradeNo + "|" + valid;
		}
	}
	
	private boolean queryOrderValid(String orderNo,String orderSerial,ApiUser partner){
		String result = PayBoxPayUtil.queryOrder(orderNo, orderSerial, partner);
		if("fail".equals(result)){
			dbLogger.error("调用boxpay订单查询未找到订单");
			return false;
		}
		BeanReader beanReader = ApiUtils.getBeanReader("iboxpay",IBoxPay.class);
		IBoxPay boxpay = (IBoxPay)ApiUtils.xml2Object(beanReader, result);
		IBoxPayResult payResult = boxpay.getResult();
		if(payResult == null || !"0".equals(payResult.getRespCode())){
			dbLogger.error("调用boxpay订单查询未找到订单");
			return false;
		}
		PartnerBoxPayUser boxPayResponse = payResult.getResponse();
		if(!"Y".equals(boxPayResponse.getOrderStatus())){
			dbLogger.error("订单在boxpay商户中未支付");
			return false;
		}
		if(!this.checkSign(boxPayResponse.getSignMsg(), new String[]{"bizType","callbackUrl","createTime","orderAmount","orderNo","orderSerial","orderStatus","orderTime","parterId","payTime","signType","sysRefNo"}, 
				boxPayResponse.getBizType(),boxPayResponse.getCallbackUrl(),boxPayResponse.getCreateTime(),boxPayResponse.getOrderAmount(),
				boxPayResponse.getOrderNo(),boxPayResponse.getOrderSerial(),boxPayResponse.getOrderStatus(),boxPayResponse.getOrderTime(),
				boxPayResponse.getParterId(),boxPayResponse.getPayTime(),boxPayResponse.getSignType(),boxPayResponse.getSysRefNo())){
			dbLogger.error("boxpay商户非法签名");
			return false;
		}
		return true;
	}
	
	protected void processPay(String tradeNo, String from){
		String key = "processOrder" + tradeNo;
		Long last = (Long) cacheService.get(CacheConstant.REGION_TENMIN, key);
		Long cur = System.currentTimeMillis();
		cacheService.set(CacheConstant.REGION_TENMIN, key, cur);
		if(last != null && last + DateUtil.m_minute * 5 > cur) {//5分钟内只处理一次
			dbLogger.warnWithType(LogTypeConstant.LOG_TYPE_ORDER_PAY, "忽略订单处理调用：" + key);
			return;
		}
		jmsService.sendMsgToDst(JmsConstant.QUEUE_PAY, JmsConstant.TAG_ORDER, "tradeNo,from", tradeNo, from);
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
