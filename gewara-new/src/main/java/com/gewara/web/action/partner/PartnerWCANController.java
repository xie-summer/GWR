package com.gewara.web.action.partner;

import java.net.URLEncoder;
import java.sql.Timestamp;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.gewara.constant.AdminCityContant;
import com.gewara.constant.SmsConstant;
import com.gewara.constant.sys.MongoData;
import com.gewara.model.common.VersionCtl;
import com.gewara.model.draw.DrawActivity;
import com.gewara.model.draw.Prize;
import com.gewara.model.draw.WinnerInfo;
import com.gewara.model.pay.ElecCard;
import com.gewara.model.pay.SMSRecord;
import com.gewara.model.user.Member;
import com.gewara.mongo.MongoService;
import com.gewara.service.drama.DrawActivityService;
import com.gewara.service.gewapay.PaymentService;
import com.gewara.support.ErrorCode;
import com.gewara.untrans.UntransService;
import com.gewara.util.DateUtil;
import com.gewara.util.HttpResult;
import com.gewara.util.HttpUtils;
import com.gewara.util.StringUtil;
import com.gewara.util.ValidateUtil;
import com.gewara.util.WebUtils;
import com.gewara.web.action.AnnotationController;

/**
 * 
 * 微能科技   联通积分兑换瓦币
 *
 */
@Controller
public class PartnerWCANController  extends AnnotationController{
	@Autowired@Qualifier("mongoService")
	private MongoService mongoService;
	@Autowired@Qualifier("paymentService")
	private PaymentService paymentService;
	
	@Autowired@Qualifier("drawActivityService")
	private DrawActivityService drawActivityService;
	public void setDrawActivityService(DrawActivityService drawActivityService) {
		this.drawActivityService = drawActivityService;
	}
	
	@Autowired@Qualifier("untransService")
	private UntransService untransService;
	public void setUntransService(UntransService untransService) {
		this.untransService = untransService;
	}
	
	private final String PASSWORD = "58ibPzuI5HVHwfjJ";//认证密钥
	
	private final String QUERY_ORDER_URL = "http://183.129.191.50:8081/recvResponse!queryOrder.do";
	private final String PRETREATMENT_RUL = "http://183.129.191.50:8081/recvResponse!preOrderResponse.do";
	private final String CONFIRM_CALLBACK_URL = "http://183.129.191.50:8081/recvResponse!confirmOrderResponse.do";
	
	/* test测试部分url
	private final String QUERY_ORDER_URL = "http://lingle.vicp.net:8080/recvResponse!queryOrder.do";
	private final String PRETREATMENT_RUL = "http://lingle.vicp.net:8080//recvResponse!preOrderResponse.do";
	private final String CONFIRM_CALLBACK_URL = "http://lingle.vicp.net:8080/recvResponse!confirmOrderResponse.do";
	 */
	private static Map<String,String> productIds = new HashMap<String,String>();
	static{
	//	productIds.put("10002001", "5");
		productIds.put("10002001", "10");
		productIds.put("10002002", "20");
	}
	/**
	 * 订单预处理
	 * @return
	 */
	@RequestMapping("/partner/wcansoft/pretreatmentOrder.xhtml")
	@ResponseBody
	public String pretreatmentOrder(String signType,String signMsg,String customerId,final String orderId,final String inputCharset,
			final String version,final String language,final String productId,HttpServletRequest request){
		String params = WebUtils.getParamStr(request, true);
		dbLogger.warn("微能科技 积分兑换抵值券预处理:>>-->params:" + params);
		if(!ValidateUtil.isMobile(customerId)) {
			dbLogger.warn("微能科技 积分兑换抵值券预处理--》 客户手机号错误！");
			return "3002";
		}
		if(!(StringUtils.equals("1", signType)  && 
				StringUtils.equals(signMsg,getSign(request,"&",new String[]{"inputCharset","submitTimestamp","retUrl","version","language",
						"password","orderId","orderTime","customerId","productId","productName","productNum","orderInfo"})))){
			dbLogger.warn("微能科技 积分兑换抵值券--》签名失败！");
			return "1002";
		}
		/*Member member = this.daoService.getObjectByUkey(Member.class, "mobile", customerId, false);
		if(member == null){
			//用户不存在，创建新用户
			String mbPassword = StringUtils.rightPad("" + new Random().nextInt(99999999), 8, '0');
			ErrorCode<Member> result = memberService.regMemberWithMobile(customerId, customerId, mbPassword, null, "in", "WCANC", AdminCityContant.CITYCODE_SH, remoteip);
			if(result.isSuccess()){
				member = result.getRetval();
				paymentService.createNewAccount(member);
				dbLogger.warn("微能科技用户创建成功："+customerId);
				Timestamp cur = new Timestamp(System.currentTimeMillis());
				String msg = "尊敬的用户：您正在使用积分兑换格瓦拉生活网" + productIds.get(productId) + "元电影抵值券，" +
						"我们为您创建了格瓦拉帐号，用户名为" + customerId + "（手机号），" +
						"密码为"+mbPassword+"（随机生成，请尽快修改）。请登入格瓦拉生活网www.gewara.com查询。";
				SMSRecord sms = new SMSRecord(null,"WCANC"+DateUtil.format(cur, "yyyyMMddHHmmss"),customerId, msg,
						cur, DateUtil.addDay(cur, 2), SmsConstant.SMSTYPE_NOW);
				messageService.saveMessage(sms);
			}else{
				dbLogger.warn("微能科技用户创建失败："+customerId);
				return "3002";
			}
		}*/
		new Thread(new Runnable(){
			public void run(){
				callBackWCAN(orderId, productIds.get(productId), inputCharset, version, language, "0000","", PRETREATMENT_RUL);
			}
		}).start();
		return "0000";
	}
	
	@RequestMapping("/partner/wcansoft/confirmOrder.xhtml")
	@ResponseBody
	public String confirmWabiOrder(String signType,String signMsg,final String inputCharset,final String language,final String orderId,
			String customerId,String settleResult,final String version,HttpServletRequest request){
		String remoteip = WebUtils.getRemoteIp(request);
		String params = WebUtils.getParamStr(request, true);
		dbLogger.warn("微能科技 积分兑换电影抵值券开始:>>ip:"+remoteip + "-->params:" + params);
		if(!StringUtils.equals("0000",settleResult)){
			return "4001";
		}
		if(!ValidateUtil.isMobile(customerId)) {
			dbLogger.warn("微能科技 积分兑换电影抵值券--》 客户手机号错误！");
			return "3002";
		}
		if(!(StringUtils.equals("1", signType)  && 
				StringUtils.equals(signMsg,getSign(request,"&",new String[]{"inputCharset","submitTimestamp","retUrl","version","language",
						"password","orderId","orderTime","customerId","productId","productName","productNum","orderInfo","settleResult"})))){
			dbLogger.warn("微能科技 积分兑换电影抵值券--》签名失败！");
			return "1002";
		}
		if(!isAllowCharge(customerId + orderId)){
			dbLogger.warn("微能科技 积分兑换电影抵值券重复请求");
			return "9999";
		}
		String[] orderResult = queryOrder(inputCharset, version, language, orderId);
		if(orderResult == null || orderResult.length != 8 || !StringUtils.equals(orderResult[7],"5")){
			dbLogger.warn("微能科技 积分兑换电影抵值券--》订单查询出错，未查到有效订单！");
			return "2001";
		}
		int amount = (Integer.parseInt(orderResult[5]) * Integer.parseInt(orderResult[6]))/100;
		DrawActivity da = daoService.getObjectByUkey(DrawActivity.class, "tag", "WCCAN_" + amount, true);
		if(da == null||!da.isJoin()) {
			dbLogger.warn("微能科技 积分兑换电影抵值券--》对应抵值券抽奖活动尚未设置！");
			return "4001";
		}
		Member member = this.daoService.getObjectByUkey(Member.class, "mobile", customerId, false);
		String msg = "";
		if(member == null){
			//用户不存在，创建新用户
			String mbPassword = StringUtils.rightPad("" + new Random().nextInt(99999999), 8, '0');
			ErrorCode<Member> result = memberService.regMemberWithMobile(customerId, customerId, mbPassword, null, "in", "WCANC", AdminCityContant.CITYCODE_SH, remoteip);
			if(result.isSuccess()){
				member = result.getRetval();
				paymentService.createNewAccount(member);
				dbLogger.warn("微能科技用户创建成功："+customerId);
				msg = "尊敬的用户：您现在就可以用您的手机号码为用户名登录www.gewara.com在线选座购电影票了，密码为" +
						mbPassword+"（随机生成，请尽快修改）。我们已经成功为您绑定" + amount + "元抵值券。赶快登录吧！";
				Timestamp cur = new Timestamp(System.currentTimeMillis());
				SMSRecord sms = new SMSRecord(null,"WCANC"+DateUtil.format(cur, "yyyyMMddHHmmss"),customerId, msg,
						cur, DateUtil.addDay(cur, 2), SmsConstant.SMSTYPE_NOW);
				untransService.addMessage(sms);
				untransService.sendMsgAtServer(sms, false);
			}else{
				dbLogger.warn("微能科技用户创建失败："+customerId);
				return "3002";
			}
		}
		//FIXME:黄牛？？
		VersionCtl mvc = drawActivityService.gainMemberVc(""+member.getId());
		ErrorCode<WinnerInfo> ec = drawActivityService.baseClickDraw(da, mvc, false, member);
		if(ec == null || !ec.isSuccess()) {
			dbLogger.warn("微能科技 积分兑换电影抵值券--》抽取抵值券时失败！");
			return "4001";
		}
		WinnerInfo winnerInfo = ec.getRetval();
		if(winnerInfo == null) {
			dbLogger.warn("微能科技 积分兑换电影抵值券--》抽取抵值券时失败！winnerInfo 为null");
			return "4001";
		}
		Prize prize = daoService.getObject(Prize.class, winnerInfo.getPrizeid());
		if(prize == null) {
			dbLogger.warn("微能科技 积分兑换电影抵值券--》抽取抵值券时失败！没有对应抵值券");
			return "4001";
		}
		drawActivityService.sendPrize(prize, winnerInfo, true);
		this.saveWCANCharge(customerId,orderId);
		String cardno = prize.getRemark();
		if(winnerInfo.getRelatedid() != null){
			cardno = daoService.getObject(ElecCard.class, winnerInfo.getRelatedid()).getCardno();
		}
		if(StringUtils.isBlank(cardno)){
			cardno = "0000";
		}
		final String tradeNo = cardno;
		new Thread(new Runnable(){
			public void run(){
				callBackWCAN(orderId, "", inputCharset, version, language, "0000", tradeNo,CONFIRM_CALLBACK_URL);
			}
		}).start();
		return "0000";
	}
	
	private void callBackWCAN(String orderId,String orderAmount,String inputCharset,String version,String language,String retResult,
			String retMsg,String retUrl){
		String submitTimestamp = DateUtil.format(new Date(), "yyyyMMddHHmmss"); 
		StringBuilder signMsg = new StringBuilder();
		signMsg.append("inputCharset").append("=").append(inputCharset).append("&");
		signMsg.append("submitTimestamp").append("=").append(submitTimestamp).append("&");
		signMsg.append("version").append("=").append(version).append("&");
		signMsg.append("language").append("=").append(language).append("&");
		signMsg.append("password").append("=").append(PASSWORD).append("&");
		signMsg.append("retResult").append("=").append(retResult).append("&");
		signMsg.append("orderId").append("=").append(orderId).append("&");
		if(StringUtils.isNotBlank(orderAmount)){
			signMsg.append("orderAmount").append("=").append(orderAmount);
		}else{
			signMsg.append("retMsg").append("=").append(retMsg);
		}
		Map<String,String> params = new HashMap<String,String>();
		params.put("signType", "1");
		params.put("signMsg",StringUtil.md5(signMsg.toString()).toUpperCase());
		
		params.put("inputCharset", inputCharset);
		params.put("submitTimestamp", submitTimestamp);
		params.put("version", version);
		params.put("language",language);
		params.put("retResult",retResult);
		params.put("orderId",orderId);
		if(StringUtils.isNotBlank(orderAmount)){
			params.put("orderAmount",orderAmount);
		}else{
			params.put("retMsg",retMsg);
		}
		HttpResult result = HttpUtils.postUrlAsString(retUrl, params);
		if(result.isSuccess()){
			dbLogger.warn("微能科技 积分兑换电影抵值券回调结果：" + result.getResponse());
		}else{
			dbLogger.warn("微能科技 积分兑换电影抵值券回调失败：" + result.getMsg());
		}
	}
	
	private String[] queryOrder(String inputCharset,String version,String language,String orderId){
		String submitTimestamp = DateUtil.format(new Date(), "yyyyMMddHHmmss"); 
		StringBuilder signMsg = new StringBuilder();
		signMsg.append("inputCharset").append("=").append(inputCharset).append("&");
		signMsg.append("submitTimestamp").append("=").append(submitTimestamp).append("&");
		signMsg.append("version").append("=").append(version).append("&");
		signMsg.append("language").append("=").append(language).append("&");
		signMsg.append("password").append("=").append(PASSWORD).append("&");
		signMsg.append("orderId").append("=").append(orderId);
		Map<String,String> params = new HashMap<String,String>();
		params.put("signType", "1");
		params.put("signMsg",StringUtil.md5(signMsg.toString()).toUpperCase());
		
		params.put("inputCharset", inputCharset);
		params.put("submitTimestamp", submitTimestamp);
		params.put("version", version);
		params.put("language",language);
		params.put("orderId",orderId);
		HttpResult result = HttpUtils.postUrlAsString(QUERY_ORDER_URL, params);
		if(result.isSuccess()){
			dbLogger.warn("微能科技 积分兑换电影抵值券查询结果：" + result.getResponse());
			return StringUtils.split(result.getResponse(), "|");
		}else{
			dbLogger.warn("微能科技 积分兑换电影抵值券查询失败：" + result.getMsg());
			return null;
		}
	}
	
	private String getSign(HttpServletRequest request,String spliter,String[] signParamNams){
		StringBuilder paramsStr = new StringBuilder();
		try {
			for (String pname : signParamNams) {
				if(StringUtils.equals("password",pname)){
					paramsStr.append(pname).append("=").append(PASSWORD).append(spliter);
				}else{
					if(request.getParameter(pname) != null){
						paramsStr.append(pname).append("=").append(URLEncoder.encode(request.getParameter(pname),"UTF-8")).append(spliter);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		String signMsg = paramsStr.toString();
		signMsg = signMsg.substring(0,signMsg.length()-1);
		dbLogger.warn("签名串---->" + signMsg);
		return StringUtil.md5(signMsg);
	}
	
	private boolean isAllowCharge(String mkey){
		Map params = new HashMap();
		params.put("mkey",  mkey);
		int count = mongoService.getCount(MongoData.NS_WCAN_CHARGE, params);
		return count == 0;
	}
	
	private void saveWCANCharge(String mobile,String orderId){
		Map saveMember = new HashMap();
		saveMember.put(MongoData.SYSTEM_ID, MongoData.buildId());
		saveMember.put("mkey", mobile + orderId);
		saveMember.put("addtime", DateUtil.formatTimestamp(System.currentTimeMillis()));
		mongoService.saveOrUpdateMap(saveMember, MongoData.SYSTEM_ID, MongoData.NS_WCAN_CHARGE);
	}
}
