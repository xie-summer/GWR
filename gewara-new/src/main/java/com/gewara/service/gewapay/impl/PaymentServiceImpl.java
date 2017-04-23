package com.gewara.service.gewapay.impl;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.criterion.Subqueries;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.ui.ModelMap;

import com.gewara.Config;
import com.gewara.constant.ApiConstant;
import com.gewara.constant.BindConstant;
import com.gewara.constant.ChargeConstant;
import com.gewara.constant.MemberConstant;
import com.gewara.constant.PayConstant;
import com.gewara.constant.PaymethodConstant;
import com.gewara.constant.PointConstant;
import com.gewara.constant.SysAction;
import com.gewara.constant.sys.ConfigConstant;
import com.gewara.constant.sys.ErrorCodeConstant;
import com.gewara.constant.sys.LogTypeConstant;
import com.gewara.constant.sys.MongoData;
import com.gewara.constant.ticket.OrderConstant;
import com.gewara.constant.ticket.RefundConstant;
import com.gewara.helper.discount.SpecialDiscountHelper;
import com.gewara.model.api.ApiUser;
import com.gewara.model.common.GewaConfig;
import com.gewara.model.common.VersionCtl;
import com.gewara.model.draw.DrawActivity;
import com.gewara.model.draw.Prize;
import com.gewara.model.draw.WinnerInfo;
import com.gewara.model.goods.Goods;
import com.gewara.model.pay.AccountRecord;
import com.gewara.model.pay.Adjustment;
import com.gewara.model.pay.Charge;
import com.gewara.model.pay.CheckRecord;
import com.gewara.model.pay.Cpcounter;
import com.gewara.model.pay.Discount;
import com.gewara.model.pay.GewaOrder;
import com.gewara.model.pay.MemberAccount;
import com.gewara.model.pay.OrderRefund;
import com.gewara.model.pay.OtherFeeDetail;
import com.gewara.model.pay.PayBank;
import com.gewara.model.pay.PayMerchant;
import com.gewara.model.pay.RepeatingPayorder;
import com.gewara.model.pay.SdRecord;
import com.gewara.model.pay.Spcounter;
import com.gewara.model.pay.SpecialDiscount;
import com.gewara.model.sport.SellDeposit;
import com.gewara.model.user.Member;
import com.gewara.model.user.MemberInfo;
import com.gewara.mongo.MongoService;
import com.gewara.pay.CardpayUtil;
import com.gewara.pay.ChinapayUtil;
import com.gewara.pay.GewapayUtil;
import com.gewara.pay.MemberCardPayUtil;
import com.gewara.pay.NewPayUtil;
import com.gewara.pay.PartnerPayUtil;
import com.gewara.pay.PayUtil;
import com.gewara.pay.PayValidHelper;
import com.gewara.pay.SpSdoUtil;
import com.gewara.pay.TelecomPayUtil;
import com.gewara.pay.UmPayUtil;
import com.gewara.service.OperationService;
import com.gewara.service.OrderException;
import com.gewara.service.bbs.UserMessageService;
import com.gewara.service.drama.DrawActivityService;
import com.gewara.service.gewapay.PaymentService;
import com.gewara.service.impl.BaseServiceImpl;
import com.gewara.service.member.BindMobileService;
import com.gewara.service.member.PointService;
import com.gewara.service.partner.PartnerSynchService;
import com.gewara.service.pay.GatewayService;
import com.gewara.support.ErrorCode;
import com.gewara.untrans.monitor.MonitorService;
import com.gewara.untrans.ticket.OrderLogService;
import com.gewara.util.BeanUtil;
import com.gewara.util.CAUtil;
import com.gewara.util.DateUtil;
import com.gewara.util.HttpResult;
import com.gewara.util.HttpUtils;
import com.gewara.util.JsonUtils;
import com.gewara.util.PKCoderUtil;
import com.gewara.util.StringUtil;
import com.gewara.util.ValidateUtil;
import com.gewara.util.VmUtils;
@Service("paymentService")
public class PaymentServiceImpl extends BaseServiceImpl implements PaymentService, InitializingBean {
	@Value("${gewapay.privatekey}")
	private String gewaPayPrivatekey;
	@Autowired@Qualifier("orderLogService")
	private OrderLogService orderLogService;
	@Autowired@Qualifier("partnerSynchService")
	private PartnerSynchService partnerSynchService;
	public void setPartnerSynchService(PartnerSynchService partnerSynchService) {
		this.partnerSynchService = partnerSynchService;
	}
	
	@Autowired@Qualifier("operationService")
	private OperationService operationService;
	public void setOperationService(OperationService operationService) {
		this.operationService = operationService;
	}
	@Autowired@Qualifier("config")
	private Config config;
	public void setConfig(Config config) {
		this.config = config;
	}
	@Autowired@Qualifier("bindMobileService")
	private BindMobileService bindMobileService;
	public void setBindMobileService(BindMobileService bindMobileService){
		this.bindMobileService = bindMobileService;
	}
	@Autowired@Qualifier("monitorService")
	protected MonitorService monitorService;
	public void setMonitorService(MonitorService monitorService) {
		this.monitorService = monitorService;
	}
	
	@Autowired@Qualifier("drawActivityService")
	private DrawActivityService drawActivityService;
	public void setDrawActivityService(DrawActivityService drawActivityService){
		this.drawActivityService = drawActivityService;
	}
	@Autowired@Qualifier("pointService")
	private PointService pointService;
	public void setPointService(PointService pointService) {
		this.pointService = pointService;
	}
	@Autowired@Qualifier("mongoService")
	private  MongoService mongoService;
	public void setMongoService(MongoService mongoService) {
		this.mongoService = mongoService;
	}
	@Autowired
	@Qualifier("userMessageService")
	private UserMessageService userMessageService;
	public void setUserMessageService(UserMessageService userMessageService) {
		this.userMessageService = userMessageService;
	}
	
	@Autowired@Qualifier("gatewayService")
	private GatewayService gatewayService;
	
	public List<String> limitPayList = new ArrayList();
	@Override
	public List<String> getLimitPayList(){
		return new ArrayList(limitPayList);
	}
	@Override
	public void reInitLimitPayList(){
		GewaConfig gc = baseDao.getObject(GewaConfig.class, ConfigConstant.CFG_PAYLIMIT);
		String[] strs  = StringUtils.split(gc.getContent(), ",");
		if(strs!=null) limitPayList = Arrays.asList(strs);
	}
	
	@Override
	public String getOrderPayUrl2(GewaOrder order) {
		String paymethod = order.getPaymethod();
		String charset="utf-8";
		Long timestamp = System.currentTimeMillis();
		String check = StringUtil.md5("paytmp" + paymethod + timestamp, 16);
		if(paymethod.equals(PaymethodConstant.PAYMETHOD_CHINAPAY1)||
				paymethod.equals(PaymethodConstant.PAYMETHOD_CHINAPAY2) || 
				paymethod.equals(PaymethodConstant.PAYMETHOD_CHINAPAYSRCB)){
			if(StringUtils.equals(order.getPaybank(), "db") && 
					StringUtils.equals(order.getOrdertype(), "drama")){//只有drama订单支持担保交易
				charset = "gbk";
			}
		}else if(paymethod.equals(PaymethodConstant.PAYMETHOD_BCPAY)){
			charset = "gbk";
		}else if(paymethod.equals(PaymethodConstant.PAYMETHOD_HZBANKPAY)){
			charset = "gbk";
		}else if(paymethod.equals(PaymethodConstant.PAYMETHOD_CMPAY)){
			charset = "gbk";
		}else if(paymethod.equals(PaymethodConstant.PAYMETHOD_ABCHINAPAY)){
			charset = "gbk";
		}else if(paymethod.equals(PaymethodConstant.PAYMETHOD_BFBPAY) || paymethod.equals(PaymethodConstant.PAYMETHOD_BFBWAPPAY)){
			charset = "gbk";
		}
		String url = config.getBasePath() + "tmpOrderForm.xhtml?orderId=" + order.getId() + "&charset=" + charset + "&t=" + timestamp + "&check=" + check + "&paymethod=" + paymethod; 
		return url;
	}
	@Override
	public String getChargePayUrl2(Charge charge) {
		String paymethod = charge.getPaymethod();
		String charset="utf-8";
		Long timestamp = System.currentTimeMillis();
		String check = StringUtil.md5("paytmp" + paymethod + timestamp, 16);
		if(paymethod.equals(PaymethodConstant.PAYMETHOD_CHINAPAY1)||
				paymethod.equals(PaymethodConstant.PAYMETHOD_CHINAPAY2) || 
				paymethod.equals(PaymethodConstant.PAYMETHOD_CHINAPAYSRCB)){
			if(StringUtils.equals(charge.getPaybank(), "db")){//只有drama订单支持担保交易
				charset = "gbk";
			}
		}else if(paymethod.equals(PaymethodConstant.PAYMETHOD_BCPAY)){
			charset = "gbk";
		}else if(paymethod.equals(PaymethodConstant.PAYMETHOD_HZBANKPAY)){
			charset = "gbk";
		}
		String url = config.getBasePath() + "tmpChargeForm.xhtml?chargeId=" + charge.getId() + "&charset=" + charset + "&t=" + timestamp + "&check=" + check + "&paymethod=" + paymethod; 
		return url;
	}
	@Override
	public final Map<String, String> getNetPayParams(GewaOrder order, String clientIp, String checkvalue){
		String paymethod =  order.getPaymethod();
		if(paymethod.equals(PaymethodConstant.PAYMETHOD_CHINAPAY1)|| 
				paymethod.equals(PaymethodConstant.PAYMETHOD_CHINAPAY2) || 
				paymethod.equals(PaymethodConstant.PAYMETHOD_CHINAPAYSRCB)){
			if(StringUtils.equals(order.getPaybank(), "db") && 
					StringUtils.equals(order.getOrdertype(), "drama")){//只有drama订单支持担保交易
				return ChinapayUtil.getDanbaoParams(order, clientIp);
			}
			return ChinapayUtil.getNetPayParams(order);
		}else if(paymethod.equals(PaymethodConstant.PAYMETHOD_OKCARDPAY)){
			return CardpayUtil.getNetPayParams(order);
		}else if(paymethod.equals(PaymethodConstant.PAYMETHOD_PARTNERPAY)){
			ApiUser partner = baseDao.getObject(ApiUser.class, order.getPartnerid());
			return PartnerPayUtil.getNetPayParams(order, partner);
		}else if(paymethod.equals(PaymethodConstant.PAYMETHOD_SPSDOPAY1)){
			return SpSdoUtil.getNetPayParams(order);
		}else if(paymethod.equals(PaymethodConstant.PAYMETHOD_GEWAPAY)){
			return GewapayUtil.getOrderPayParams(order, config.getAbsPath(), checkvalue);
		}else if(paymethod.equals(PaymethodConstant.PAYMETHOD_MEMBERCARDPAY)){
			return MemberCardPayUtil.getOrderPayParams(order, config.getAbsPath());
		}else if(paymethod.startsWith(PaymethodConstant.PAYMETHOD_UMPAY)){
			return UmPayUtil.getNetPayParams(order);
		}else if(paymethod.endsWith(PaymethodConstant.PAYMETHOD_TELECOM)){
			return TelecomPayUtil.getNetPayParams(order);
		}else if(paymethod.endsWith(PaymethodConstant.PAYMETHOD_MOBILE_TELECOM)){
			return TelecomPayUtil.getNetMobilePayParams(order);
		}else if(paymethod.equals(PaymethodConstant.PAYMETHOD_CCBPOSPAY)){
			return GewapayUtil.getCcbposPayParams(order, config.getAbsPath());
		}else if(paymethod.equals(PaymethodConstant.PAYMETHOD_IPSPAY) 
				|| paymethod.equals(PaymethodConstant.PAYMETHOD_ALIBANKPAY)
				|| paymethod.equals(PaymethodConstant.PAYMETHOD_SDOPAY)
				|| paymethod.equals(PaymethodConstant.PAYMETHOD_ALLINPAY)
				|| paymethod.equals(PaymethodConstant.PAYMETHOD_SDOPAY)
				|| paymethod.equals(PaymethodConstant.PAYMETHOD_TENPAY)){
			throw new IllegalArgumentException("已下线！！");
		}else	{	
			return null;
		}
	}
		
	@Override
	public final ErrorCode<Map<String, String>> getNetPayParamsV2(GewaOrder order, String clientIp, String version){
		Map<String, String> paramMap = new LinkedHashMap<String, String>();
		paramMap.put("orderno", order.getTradeNo());
		paramMap.put("fee", order.getDue()+"00");
		String gateway = order.getPaymethod();
		
		String payurl = NewPayUtil.getPayurl();
		String gatewayCode = gateway;
		//由银联2.0，页面还会传老的支付方式过来，为了兼容老的，所在这儿加个决断
		if(StringUtils.startsWith(gateway, PaymethodConstant.PAYMETHOD_UNIONPAYFAST)){
			gatewayCode = PaymethodConstant.PAYMETHOD_UNIONPAYFAST;
		}
		if(gatewayService.isSwitch(gatewayCode)){
			if(StringUtils.equals(gatewayCode, PaymethodConstant.PAYMETHOD_UNIONPAYFAST)){
				gateway = PaymethodConstant.PAYMETHOD_UNIONPAYFAST;
			}
			payurl = NewPayUtil.getNewPayurl();			
			ErrorCode<PayMerchant> code = gatewayService.findMerchant(order.getCitycode(), gatewayCode,order.getPaybank());
			if(!code.isSuccess()){
				dbLogger.warn("tradeNo is " + order.getTradeNo() + " " + code.getMsg());
				orderLogService.addSysLog(order.getTradeNo(), gatewayCode, OrderLogService.ACTION_FINDMERCHANT, order.getMemberid());				
				return ErrorCode.getFailure(code.getMsg());
			}
			String merchantCode = code.getRetval().getMerchantCode();
			paramMap.put("merchantCode", merchantCode);
			//暂时在订单成功后存GatewayCode、MerchantCode
			//order.setGatewayCode(gateway);
			//order.setMerchantCode(merchantCode);
			//baseDao.saveObject(order);
		}		
		
		if(StringUtils.isNotBlank(order.getPaybank())) gateway = gateway + ":" + order.getPaybank();
		paramMap.put("gateway", gateway);
		paramMap.put("mobile", order.getMobile());
		paramMap.put("quantity", order.getQuantity()+"");
		paramMap.put("createtime", DateUtil.format(order.getAddtime(),"yyyy-MM-dd HH:mm:ss"));
		paramMap.put("updatetime", DateUtil.format(order.getUpdatetime(),"yyyy-MM-dd HH:mm:ss"));
		paramMap.put("validtime", DateUtil.format(order.getValidtime(),"yyyy-MM-dd HH:mm:ss"));
		String subject = null;
		if(StringUtils.equals(PaymethodConstant.PAYMETHOD_PAYECO_DNA, order.getPaymethod())){
			subject = order.getOrdertitle();//易联不加有效时间，语音播报不全
		}else{
			subject = DateUtil.format(order.getValidtime(), "yy-MM-dd HH:mm:ss") + "前付款" + order.getOrdertitle();
		}
		paramMap.put("subject", subject);//商品名称，最多	
		paramMap.put("description", "Gewa订单号：" + order.getTradeNo() + "请在" + DateUtil.formatTimestamp(order.getValidtime()) + "前付款");															//商品描述
		paramMap.put("clientIp", clientIp);	
		paramMap.put("reserve1", "");	
		paramMap.put("reserve2", "");
		paramMap.put("returnUrl", NewPayUtil.getReturnurl());	
		paramMap.put("notifyUrl", NewPayUtil.getNotifyurl());
		Map<String, String> tmpMap = new HashMap<String, String>();
		tmpMap.put("discount", order.getDiscount()+"");
		tmpMap.put("partnerid", order.getPartnerid()+"");
		tmpMap.put("memberid", order.getMemberid()+"");
		if(StringUtils.isNotBlank(order.getCitycode())) tmpMap.put("citycode", order.getCitycode());
		if(StringUtils.isNotBlank(version)) tmpMap.put("version", version);
		if(StringUtils.equals(order.getPaymethod(), PaymethodConstant.PAYMETHOD_UNIONPAYFAST) 
				|| StringUtils.equals(order.getPaymethod(), PaymethodConstant.PAYMETHOD_UNIONPAYFAST_ACTIVITY_JS) 
				|| StringUtils.equals(order.getPaymethod(), PaymethodConstant.PAYMETHOD_UNIONPAYFAST_ACTIVITY_BJ)
				|| StringUtils.equals(order.getPaymethod(), PaymethodConstant.PAYMETHOD_UNIONPAYFAST_ACTIVITY_SZ)
				|| StringUtils.equals(order.getPaymethod(), PaymethodConstant.PAYMETHOD_UNIONPAYFAST_ACTIVITY_GZ)
				|| StringUtils.equals(order.getPaymethod(), PaymethodConstant.PAYMETHOD_UNIONPAYFAST_ACTIVITY_ZJ)){
			Map<String, String> otherinfoMap = JsonUtils.readJsonToMap(order.getOtherinfo());
			tmpMap.put("cardNumber", otherinfoMap.get("cardNumber"));
			tmpMap.put("smsCode", otherinfoMap.get("smsCode"));
		}else if(StringUtils.equals(order.getPaymethod(), PaymethodConstant.PAYMETHOD_BOCAGRMTPAY)){
			Map<String, String> otherinfoMap = JsonUtils.readJsonToMap(order.getOtherinfo());
			tmpMap.put("agrmtPayType", otherinfoMap.get("agrmtPayType"));
			tmpMap.put("agrmtNo", otherinfoMap.get("agrmtNo"));
		}else if(StringUtils.equals(order.getPaymethod(), PaymethodConstant.PAYMETHOD_ALISMARTMOBILEPAY)){
			Map<String, String> otherinfoMap = JsonUtils.readJsonToMap(order.getOtherinfo());
			String token = otherinfoMap.get(MemberConstant.ALIWALLET_EXTERN_TOKEN);
			if(StringUtils.isNotBlank(token)) tmpMap.put(MemberConstant.ALIWALLET_EXTERN_TOKEN, token);
		}
		paramMap.put("otherinfo", JsonUtils.writeMapToJson(tmpMap));
		String paramStr = JsonUtils.writeMapToJson(paramMap);
		
		String sign = CAUtil.doSign(paramStr, NewPayUtil.getMerprikey(), "utf-8", "SHA1WithRSA");
		Map<String, String> postMap = new HashMap<String, String>();
		postMap.put("merid", NewPayUtil.getMerid());
		try {
			postMap.put("params", Base64.encodeBase64String(paramStr.getBytes("UTF-8")));
		} catch (Exception e) {
			dbLogger.warn("", e);
			orderLogService.addSysLog(order.getTradeNo(), order.getPaymethod(), OrderLogService.ACTION_APIERROR, order.getMemberid());
			return ErrorCode.getFailure("系统繁忙，请重试！");
		}
		postMap.put("sign", sign);
		HttpResult code = HttpUtils.postUrlAsString(payurl, postMap);
		if(code.isSuccess()){
			try {
				String res = new String(Base64.decodeBase64(code.getResponse()), "utf-8");
				Map<String, String> returnMap = VmUtils.readJsonToMap(res);
				return ErrorCode.getSuccessReturn(returnMap);
			} catch (Exception e) {
				dbLogger.warn("", e);
				orderLogService.addSysLog(order.getTradeNo(), order.getPaymethod(), OrderLogService.ACTION_APIERROR, order.getMemberid());
				return ErrorCode.getFailure("系统繁忙，请重试！");
			}
		}else {
			return ErrorCode.getFailure("系统繁忙，请重试！");
		}
	}
	@Override
	public final Map<String, String> getNetChargeParamsV2(Charge charge, String clientIp, String version){
		GewaOrder gorder = null;
		if(charge.getOutorderid()!=null) gorder = baseDao.getObject(GewaOrder.class, charge.getOutorderid());
		Map<String, String> paramMap = new LinkedHashMap<String, String>();
		Timestamp validtime = gorder!=null?gorder.getValidtime():DateUtil.addHour(charge.getAddtime(), 2);
		String strValidtime = DateUtil.formatTimestamp(validtime);
		paramMap.put("orderno", charge.getTradeNo());
		paramMap.put("fee", charge.getTotalfee()+"00");
		String gateway = charge.getPaymethod();
		
		String payurl = NewPayUtil.getPayurl();
		if(gatewayService.isSwitch(gateway)){
			payurl = NewPayUtil.getNewPayurl();
			ErrorCode<PayMerchant> code = gatewayService.findDefaultMerchant(gateway);
			if(!code.isSuccess()){
				dbLogger.warn("tradeNo is " + charge.getTradeNo() + " " + code.getMsg());
				orderLogService.addSysLog(charge.getTradeNo(), gateway, OrderLogService.ACTION_FINDMERCHANT, charge.getMemberid());				
				//return ErrorCode.getFailure(code.getMsg());
				return new HashMap<String, String>();
			}
			String merchantCode = code.getRetval().getMerchantCode();
			paramMap.put("merchantCode", merchantCode);
		}
		
		if(StringUtils.isNotBlank(charge.getPaybank())) gateway = gateway + ":" + charge.getPaybank();
		paramMap.put("gateway", gateway);
		paramMap.put("quantity", "1");
		paramMap.put("createtime", DateUtil.formatTimestamp(charge.getAddtime()));
		paramMap.put("updatetime", DateUtil.formatTimestamp(charge.getUpdatetime()));
		paramMap.put("validtime", strValidtime);
		paramMap.put("subject", strValidtime + "前付款");	
		paramMap.put("description", "Gewa订单号：" + charge.getTradeNo() + "请在" + strValidtime + "前付款");															//商品描述
		paramMap.put("clientIp", clientIp);	
		paramMap.put("reserve1", "");	
		paramMap.put("reserve2", "");
		paramMap.put("returnUrl", NewPayUtil.getReturnurl());	
		paramMap.put("notifyUrl", NewPayUtil.getNotifyurl());
		Map<String, String> tmpMap = new HashMap<String, String>();
		tmpMap.put("discount", "0");
		tmpMap.put("partnerid", "1");
		tmpMap.put("memberid", charge.getMemberid()+"");
		if(StringUtils.isNotBlank(version)) tmpMap.put("version", version);
		paramMap.put("otherinfo", JsonUtils.writeMapToJson(tmpMap));
		String paramStr = JsonUtils.writeMapToJson(paramMap);
		
		String sign = CAUtil.doSign(paramStr, NewPayUtil.getMerprikey(), "utf-8", "SHA1WithRSA");
		Map<String, String> postMap = new HashMap<String, String>();
		postMap.put("merid", NewPayUtil.getMerid());
		try {
			postMap.put("params", Base64.encodeBase64String(paramStr.getBytes("UTF-8")));
		} catch (Exception e) {
			e.printStackTrace();
			return new HashMap<String, String>();
		}
		postMap.put("sign", sign);
		
		HttpResult code = HttpUtils.postUrlAsString(payurl, postMap);
		//HttpResult code = HttpUtils.postUrlAsString(NewPayUtil.getPayurl(), postMap);
		if(code.isSuccess()){
			try {
				String res = new String(Base64.decodeBase64(code.getResponse()), "utf-8");
				Map<String, String> returnMap = VmUtils.readJsonToMap(res);
				return returnMap;
			} catch (Exception e) {
				e.printStackTrace();
				return new HashMap<String, String>();
			}
		}else {
			return new HashMap<String, String>();
		}
	}
	private List<String> getChargeserverMethodList(){
		GewaConfig cfg = baseDao.getObject(GewaConfig.class, ConfigConstant.CFG_CHARGE);
		List<String> paymethodList = new ArrayList<String>();
		String[] strs = null;
		if(cfg!=null) strs = StringUtils.split(cfg.getContent(), ",");
		if(strs!=null) paymethodList = Arrays.asList(StringUtils.split(cfg.getContent(), ","));
		return paymethodList;
	}
	@Override
	public final String getChargePayUrl(Charge charge, String clientIp){
		List<String> paymethodList = getChargeserverMethodList();
		if(paymethodList.contains(charge.getPaymethod())){
			return getChargePayUrl2(charge);
		}else if(StringUtils.equals(charge.getPaymethod(), PaymethodConstant.PAYMETHOD_TELECOM)){
			return TelecomPayUtil.getChargePayUrl(charge);
		}
		return null;
	}
	
	@Override
	public List<String> getPayserverMethodList(){
		GewaConfig cfg = baseDao.getObject(GewaConfig.class, config.getLong("payServer"));
		List<String> paymethodList = new ArrayList<String>();
		String[] strs = null;
		if(cfg!=null) strs = StringUtils.split(cfg.getContent(), ",");
		if(strs!=null) paymethodList = Arrays.asList(StringUtils.split(cfg.getContent(), ","));
		return paymethodList;
	}
	@Override
	public MemberAccount createNewAccount(Member member){
		MemberAccount account = new MemberAccount(member.getId());
		MemberInfo mi = baseDao.getObject(MemberInfo.class, member.getId());
		if(mi!=null){
			if(StringUtils.isNotBlank(mi.getPhone())) account.setPhone(mi.getPhone());
			if(StringUtils.isNotBlank(mi.getRealname())){
				account.setRealname(mi.getRealname());
			}else{
				account.setRealname(member.getNickname());
			}
		}else{
			account.setRealname(member.getNickname());
		}
		account.setPassword(PayUtil.getPass("123456"));
		baseDao.saveObject(account);
		return account;
	}

	@Override
	public List<Charge> getChargeList(Long memberid, Timestamp from, Timestamp to, String status) {
		DetachedCriteria query = DetachedCriteria.forClass(Charge.class);
		if(StringUtils.isNotBlank(status)) query.add(Restrictions.eq("status", status));
		if(memberid != null) query.add(Restrictions.eq("memberid", memberid));
		query.add(Restrictions.ge("updatetime", from));
		query.add(Restrictions.lt("updatetime", to));
		List<Charge> result = hibernateTemplate.findByCriteria(query);
		return result;
	}

	@Override
	public List<GewaOrder> getGewaOrderListByMemberId(Long memberid) {
		DetachedCriteria query = DetachedCriteria.forClass(GewaOrder.class);
		query.add(Restrictions.eq("memberid", memberid));
		query.addOrder(Order.desc("addtime"));
		List<GewaOrder> orderList = hibernateTemplate.findByCriteria(query);
		return orderList;
	}

	@Override
	public List<Charge> getChargeListByMemberId(Long memberId,Timestamp startTime,Timestamp endTime,int from,int maxnum) {
		List<Charge> result = getChargeListByMemberId(memberId, true, startTime, endTime, from, maxnum);
		return result;
	}
	@Override
	public List<Charge> getChargeListByMemberId(Long memberId, boolean ischarge, Timestamp startTime,Timestamp endTime,int from,int maxnum) {
		DetachedCriteria query = DetachedCriteria.forClass(Charge.class);
		query.add(Restrictions.eq("memberid", memberId));
		if(startTime !=null && endTime !=null){
			query.add(Restrictions.between("addtime", startTime, endTime));
		}else if(startTime !=null ){
			query.add(Restrictions.ge("addtime", startTime));
		}else if(endTime != null){
			query.add(Restrictions.le("addtime", endTime));
		}
		if(ischarge){
			query.add(Restrictions.eq("chargetype", ChargeConstant.TYPE_CHARGE));
		}
		query.addOrder(Order.desc("addtime"));
		List<Charge> result = hibernateTemplate.findByCriteria(query,from,maxnum);
		return result;
	}
	@Override
	public ErrorCode<Charge> bankPayCharge(String tradeNo, boolean bankcharge, String payseqno, int fee, String paymethod, String paybank,String gatewayCode,String merchantCode) {
		/*********************
		 * 1、更改付款状态
		 * 2、更改订单状态
		 *****************************/
		Charge charge = baseDao.getObjectByUkey(Charge.class, "tradeNo", tradeNo, false);
		if(charge.isPaid()) return ErrorCode.getFailureReturn(charge);//已经支付过
		charge.setUpdatetime(new Timestamp(System.currentTimeMillis()));
		charge.setStatus(Charge.STATUS_PAID);
		charge.setPayseqno(payseqno);
		charge.setTotalfee(fee);
		charge.setPaymethod(paymethod);
		//-----------------
		charge.setGatewayCode(gatewayCode);
		charge.setMerchantCode(merchantCode);	
		//------------------
		baseDao.saveObject(charge);
		MemberAccount account = baseDao.getObjectByUkey(MemberAccount.class, "memberid", charge.getMemberid(), false);
		if(charge.isPaid()) {
			dbLogger.warn(tradeNo + "充值:站内金额：由"+ account.getBankcharge()+"增加" + fee);
			account.addBanlance(charge.getTotalfee());
			if(bankcharge){ 
				if(StringUtils.equals(charge.getChargeto(), ChargeConstant.WABIPAY)){
					account.addWabicharge(fee);
				}else if(StringUtils.equals(charge.getChargeto(), ChargeConstant.DEPOSITPAY)){
					account.addDepositcharge(fee);
					SellDeposit sellGuarantee = baseDao.getObjectByUkey(SellDeposit.class, "chargeid", charge.getId());
					sellGuarantee.setStatus(SellDeposit.STATUS_PAID_SUCCESS);
					sellGuarantee.setValidtime(DateUtil.addDay(sellGuarantee.getValidtime(), 30));
					baseDao.saveObject(sellGuarantee);
				}else{
					account.addBankcharge(fee);
				}
			}else account.addWabicharge(fee);
		}
		baseDao.saveObject(account);
		List<String> pmList = Arrays.asList(PaymethodConstant.PAYMETHOD_ALIPAY, PaymethodConstant.PAYMETHOD_PNRPAY);
		ErrorCode<Double> code = checkAddpoint(charge);
		if(code.isSuccess() 
				&& StringUtils.equals(charge.getChargeto(), ChargeConstant.WABIPAY) 
				&& pmList.contains(paymethod)){
			int point = Long.valueOf(Math.round(fee*code.getRetval())).intValue();
			pointService.addPointInfo(account.getMemberid(), point, charge.getId()+"", PointConstant.TAG_CHARGETOWABI);
			userMessageService.sendSiteMSG(account.getMemberid(), SysAction.STATUS_RESULT, null, "你已成功充值"+fee+"元，系统已为你赠送充值积分"+point+",点击<a href='"+config.getBasePath()+"home/acct/pointList.xhtml"+"'class='ml5' target='_blank'>查看(积分记录)</a>");
		}
		if(!charge.hasChargeto(ChargeConstant.DEPOSITPAY)){
			userMessageService.sendSiteMSG(account.getMemberid(), SysAction.STATUS_RESULT, null, "你已成功充值"+fee+"元，若有疑问请联系客服：4000-406-506。");
		}
		return ErrorCode.getSuccessReturn(charge);
	}

	@Override
	public ErrorCode<GewaOrder> netPayOrder(String tradeNo, String payseqno, int fee, String paymethod, String paybank, String from){
		return netPayOrder(tradeNo, payseqno, fee, paymethod, paybank, from, null, null,paymethod,null);
	}
	@Override
	public ErrorCode<GewaOrder> netPayOrder(String tradeNo, String payseqno, int fee, String paymethod, String paybank, String from, Timestamp paidtime,String gatewayCode,String merchantCode){
		return netPayOrder(tradeNo, payseqno, fee, paymethod, paybank, from, paidtime, null,gatewayCode,merchantCode);
	}
	@Override
	public ErrorCode<GewaOrder> netPayOrder(String tradeNo, String payseqno, int fee, String paymethod, String paybank, String from, Timestamp paidtime, Map<String, String> otherMap,String gatewayCode,String merchantCode){
		GewaOrder order = baseDao.getObjectByUkey(GewaOrder.class, "tradeNo", tradeNo, false);
		if(order==null) throw new IllegalArgumentException("订单不存在：" + tradeNo);
		if(order.getStatus().startsWith(OrderConstant.STATUS_PAID)){
			if(!StringUtils.equals(order.getPaymethod(), paymethod) || !StringUtils.equals(payseqno, order.getPayseqno())){
				RepeatingPayorder reOrder = new RepeatingPayorder(order.getPaymethod(), tradeNo, payseqno, paymethod, fee);
				baseDao.saveObject(reOrder);
				//untransService.saveOrderWarn(order, paymethod, fee);
			}
			//(order.isNetPaid() || order.getDue()==0) && order.getStatus().startsWith(OrderConstant.STATUS_PAID)
			dbLogger.warn(from + "重复调用订单支付" +  tradeNo);
			return ErrorCode.getFailureReturn(order);
		}else{
			//if(!order.isNetPaid() && order.getDue()>0 || !order.getStatus().startsWith(OrderConstant.STATUS_PAID)) {
			netPayOrder(order, payseqno, fee, paymethod, paybank, OrderConstant.STATUS_PAID_FAILURE, paidtime, otherMap,gatewayCode,merchantCode);
			partnerSynchService.addCallbackOrder(order, PayConstant.PUSH_FLAG_PAID, true);
			return ErrorCode.getSuccessReturn(order);
		}

	}
	private void netPayOrder(GewaOrder order, String payseqno, int fee, String paymethod, String paybank, String newstatus, Timestamp paidtime, Map<String, String> otherMap,String gatewayCode,String merchantCode){
		/*********************
		 * 1、更改付款状态
		 * 2、更改订单状态
		 * 3、增加用户积分
		 * 4、订单有效期加半年(为了查询方便)
		 *****************************/
		if(otherMap!=null) {
			Map<String, String> othMap = VmUtils.readJsonToMap(order.getOtherinfo());
			othMap.putAll(otherMap);
			order.setOtherinfo(JsonUtils.writeMapToJson(othMap));
		}
		String status = order.getStatus();
		Timestamp curTime = new Timestamp(System.currentTimeMillis());
		if(paidtime!=null) curTime = paidtime;
		order.setPayseqno(payseqno);
		//order.setPaybank(paybank);//不使用
		order.setPaymethod(paymethod);
		order.setAlipaid(fee);
		order.setUpdatetime(curTime);
		order.setModifytime(curTime);
		order.setPaidtime(curTime);
		order.setStatus(newstatus); //先设置成此状态，第二步设成paid_success
		
		//-----------------
		order.setGatewayCode(gatewayCode);
		order.setMerchantCode(merchantCode);
		//------------------
		
		baseDao.saveObject(order);
		dbLogger.warn(order.getTradeNo() + ","+ payseqno + "," + paybank + status + "-->" + order.getStatus());
	}
	@Override
	public void gewaPayOrder(GewaOrder order, Long memberId) throws OrderException {
		/*********************
		 * 1、更改付款状态
		 * 2、更改订单状态
		 * 3、增加用户积分
		 * 4、订单有效期加半年(为了查询方便)
		 *****************************/
		if(order != null){
			if(order.isAllPaid()) return;//防止重复订单
			MemberAccount account = baseDao.getObjectByUkey(MemberAccount.class, "memberid", memberId, false);
			//1)更改帐号
			if(!order.getMemberid().equals(memberId)) throw new OrderException(ApiConstant.CODE_SEAT_OCCUPIED, "不能替别人支付订单！");
			int banlance = account.getBanlance() - account.getDepositcharge();
			if(banlance < order.getDue()){
				throw new OrderException(ApiConstant.CODE_SEAT_OCCUPIED, "您账户内的余额不足，仅为"+banlance+"元,您需要支付" + order.getDue() + "元");
			}
			int due = order.getDue();
			account.addBanlance(-due);
			Map<String, String> orderOtherMap = VmUtils.readJsonToMap(order.getOtherinfo());
			String paybank = order.getPaybank();
			int wabi = 0, bank = 0;
			if(StringUtils.equals(ChargeConstant.WABIPAY, paybank)){
				wabi = Math.min(order.getDue(), account.getOthercharge());
				bank = order.getDue() - wabi;
			}else{
				bank = Math.min(order.getDue(), account.getBankcharge());
				wabi = order.getDue() - bank;
			}
			account.addBankcharge(-bank);
			account.addWabicharge(-wabi);
			dbLogger.warn(order.getTradeNo() + "余额消费wabi:" + wabi + ",bank:" + bank);
			baseDao.saveObject(account);
			
			//2)更改订单状态
			order.setWabi(wabi);
			order.setGewapaid(due);
			Timestamp curTime = new Timestamp(System.currentTimeMillis());
			dbLogger.warn(order.getTradeNo() + "GewaPay支付前状态：" + order.getStatus());
			order.setStatus(OrderConstant.STATUS_PAID_FAILURE); //先设置成此状态，第二步设成paid_success
			order.setPaymethod(PaymethodConstant.PAYMETHOD_GEWAPAY);
			order.setPaidtime(curTime);
			order.setUpdatetime(curTime);
			order.setModifytime(curTime);
			Map<String, String> operMap = new HashMap();
			operMap.put(ChargeConstant.WABIPAY, wabi+"");
			operMap.put(ChargeConstant.BANKPAY, bank+"");
			orderOtherMap.putAll(operMap);
			String otherinfo = JsonUtils.writeMapToJson(orderOtherMap);
			order.setOtherinfo(otherinfo);
			baseDao.saveObject(order);
		}else{
			throw new OrderException(ApiConstant.CODE_SEAT_OCCUPIED, "不存在的订单！");
		}
	}

	@Override
	public CheckRecord getLastCheckRecord() {
		List<CheckRecord> result = getCheckRecordList(0, 1);
		if(result.isEmpty()) return null;
		return result.get(0);
	}
	
	@Override
	public List<CheckRecord> getCheckRecordList(int from, int maxnum) {
		DetachedCriteria query = DetachedCriteria.forClass(CheckRecord.class);
		query.addOrder(Order.desc("checktime"));
		List<CheckRecord> result = hibernateTemplate.findByCriteria(query, from, maxnum);
		return result;
	}
	/**
	 * 清理账户第1步：生成有变化的用户账号
	 * @param userid
	 * @return
	 */
	@Override
	public synchronized ErrorCode<CheckRecord> closeAccountStep1(Long userid) {
		if(userid==null) return ErrorCodeConstant.NOT_LOGIN;
		//0、进行判断，取出初始化条件
		CheckRecord lastCheckRecord = getLastCheckRecord();
		if(lastCheckRecord.getStatus().compareTo(CheckRecord.STATUS_STEP2)<0) return ErrorCode.getFailure("上次的账单未结算完成，请先结算上次的！");
		Timestamp closeTime = new Timestamp(System.currentTimeMillis());
		Integer[] remain = getMemberAccountTotal();
		Timestamp lasttime = lastCheckRecord.getChecktime();
		//每日只能结一次帐
		if(lastCheckRecord.getChecktime().after(DateUtil.addHour(closeTime, -10)))
			return ErrorCode.getFailure("10小时以内只能结一次帐！"); //本次结账
		CheckRecord thisCheckRecord = new CheckRecord(userid, lasttime, closeTime);
		thisCheckRecord.setAccountsum(remain[0]);
		thisCheckRecord.setWabisum(remain[1]);
		baseDao.saveObject(thisCheckRecord);
		//下面分3步进行，查出账户有变化的用户：
		
		//1、找到账户有变化、有充值、有消费的信息

		Set<Long> memberidSet = new HashSet<Long>();
		String sqlquery = "select t.memberid from WEBDATA.member_account t where t.CHANGETIME >= ? and t.CHANGETIME< ? ";
		memberidSet.addAll(jdbcTemplate.queryForList(sqlquery, Long.class, lasttime, closeTime));

		//2)本期充值
		List<Long> chargeMemberidList = getChargeMemberidList(lasttime, closeTime);
		memberidSet.addAll(chargeMemberidList);
		dbLogger.warn("清理本期充值用户：" + chargeMemberidList.size() + "个");
		//3)账户余额支付的订单
		List<Long> gewaPaidMemberidList = getGewaPaidMemberidList(lasttime, closeTime);
		dbLogger.warn("余额支付订单用户数：" + gewaPaidMemberidList.size() + "个");
		memberidSet.addAll(gewaPaidMemberidList);
		//4)本期退款
		List<Long> adjustmentMemberidList = getAdjustmentMemberidList(lasttime, closeTime);
		dbLogger.warn("清理本期退款用户数：" + adjustmentMemberidList.size() + "个");
		memberidSet.addAll(adjustmentMemberidList);
		
		Map<Long/*memberid*/, MemberAccount> accountMap = getMemberAccountMap(new ArrayList(memberidSet));
		Map<Long/*memberid*/, AccountRecord> lastMap = getLastAccountRecordMap(new ArrayList(memberidSet));
		AccountRecord cur = null, last = null;
		MemberAccount ma = null;
		for(Long mid: memberidSet){
			ma = accountMap.get(mid);
			if(ma==null) dbLogger.warn("清理出错：MemberAccount不存在：" + mid);
			cur = new AccountRecord(ma.getId(), ma.getMemberid(), ma.getRealname());
			cur.setAbanlance(ma.getBanlance());							//a)本期余额
			last = lastMap.get(ma.getMemberid());
			if(last != null) cur.setLbanlance(last.getAbanlance());//b)上期余额
			else cur.setLbanlance(0); //新建，余额0
			baseDao.saveObject(cur);
		}
		dbLogger.warn("清理第1步完成！" + thisCheckRecord.getId());
		return ErrorCode.getSuccessReturn(thisCheckRecord);
	}
	
	private List<Long> getChargeMemberidList(Timestamp from, Timestamp to) {
		DetachedCriteria query = DetachedCriteria.forClass(Charge.class);
		query.add(Restrictions.eq("status", Charge.STATUS_PAID));
		query.add(Restrictions.ge("updatetime", from));
		query.add(Restrictions.lt("updatetime", to));
		query.setProjection(Projections.property("memberid"));
		List<Long> result = hibernateTemplate.findByCriteria(query);
		return result;
	}
	private List<Long> getGewaPaidMemberidList(Timestamp from, Timestamp to){
		DetachedCriteria query = DetachedCriteria.forClass(GewaOrder.class);
		query.add(Restrictions.like("status", OrderConstant.STATUS_PAID, MatchMode.START));
		query.add(Restrictions.eq("paymethod", PaymethodConstant.PAYMETHOD_GEWAPAY));
		query.add(Restrictions.gt("gewapaid", 0));
		query.add(Restrictions.ge("paidtime", from));
		query.add(Restrictions.lt("paidtime", to));
		query.setProjection(Projections.property("memberid"));
		List<Long> result = hibernateTemplate.findByCriteria(query);
		return result;
	}
	private List<Long> getAdjustmentMemberidList(Timestamp timefrom, Timestamp timeto) {
		DetachedCriteria query = DetachedCriteria.forClass(Adjustment.class);
		query.add(Restrictions.eq("status", Adjustment.STATUS_SUCCESS));
		query.add(Restrictions.ge("updatetime", timefrom));
		query.add(Restrictions.lt("updatetime", timeto));
		query.setProjection(Projections.property("memberid"));
		List<Long> result = hibernateTemplate.findByCriteria(query);
		return result;
	}
	/**
	 * 清理账户第2步：清理充值BillRecord, 调整BillRecord，计算余额支付订单，用户余额试算平衡
	 * @return
	 */
	@Override
	public void closeAccountStep2(CheckRecord checkRecord){//清理每个用户订单
		if(checkRecord.getStatus().compareTo(CheckRecord.STATUS_STEP2)>=0) return;//已经做过
		dbLogger.warn("结帐第4步开始.....");
		List<Charge> chargeList = getChargeList(null, checkRecord.getFromtime(), checkRecord.getChecktime(), OrderConstant.STATUS_PAID_SUCCESS);
		List<AccountRecord> recordList = baseDao.getObjectListByField(AccountRecord.class, "checkid", checkRecord.getId());
		Map<Long, AccountRecord> accountRecordMap = BeanUtil.beanListToMap(recordList, "memberid"); 
		AccountRecord cur;
		for(Charge charge: chargeList){
			cur = accountRecordMap.get(charge.getMemberid());
			cur.addCharge(charge.getTotalfee());
		}
		String query = "from GewaOrder where status like ? and paidtime>=? and paidtime<? and gewapaid>0 ";
		List<GewaOrder> orderList = hibernateTemplate.find(query, OrderConstant.STATUS_PAID + "%", checkRecord.getFromtime(), checkRecord.getChecktime());
		dbLogger.warn("更新本期订单...");
		//1、订单插入BillRecord中，顺便计算本次账户
		for(GewaOrder order: orderList){
			cur = accountRecordMap.get(order.getMemberid());
			cur.addGewapay(order.getGewapaid());
		}
		dbLogger.warn("更新本期退款...");
		List<Adjustment> adjustmentList = getAdjustmentList(null, checkRecord.getFromtime(), checkRecord.getChecktime(), Adjustment.STATUS_SUCCESS);
		//2、退款插入BillRecord中，顺便计算本次账户退款
		for(Adjustment adjustment: adjustmentList){
			cur = accountRecordMap.get(adjustment.getMemberid());
			cur.addRefund(adjustment.getSubtractAmount());
		}
		dbLogger.warn("保存所有信息...");
		//3、保存所有AccountRecord
		baseDao.saveObjectList(accountRecordMap.values());
		checkRecord.setStatus(CheckRecord.STATUS_STEP2);
		baseDao.saveObject(checkRecord);
		dbLogger.warn("结帐第4步完成！");
	}
	private Map<Long/*memberid*/, MemberAccount> getMemberAccountMap(List<Long> memberidList) {
		final String query = "from MemberAccount where memberid in (:idList)";
		List<List<Long>> idgroupList = BeanUtil.partition(memberidList, 500);
		List<MemberAccount> accountList = new ArrayList<MemberAccount>();
		for(final List<Long> aidList:idgroupList){
			List<MemberAccount> result = queryByNameParams(query, 0, 50000, "idList", aidList);
			accountList.addAll(result);
		}
		return BeanUtil.beanListToMap(accountList, "memberid");
	}
	private Map<Long/*memberid*/, AccountRecord> getLastAccountRecordMap(List<Long> memberidList){
		final String query1 = "select new map(max(id) as id, memberid as mid) from AccountRecord where memberid in (:idList) group by memberid";
		List<List<Long>> idgroupList = BeanUtil.partition(memberidList, 500);
		List<Long> aridList = new ArrayList<Long>();
		for(final List<Long> aidList:idgroupList){
			List<Map<String, Long>> result = queryByNameParams(query1, 0, 50000, "idList", aidList);
			for(Map<String, Long> row: result){
				aridList.add(row.get("id"));
			}
		}
		List<List<Long>> aridgroupList = BeanUtil.partition(aridList, 500);
		final String query2 = "from AccountRecord where id in (:idList)";
		List<AccountRecord> recordList = new ArrayList<AccountRecord>();
		for(final List<Long> al:aridgroupList){
			List<AccountRecord> result = queryByNameParams(query2, 0, 50000, "idList", al);
			recordList.addAll(result);
		}
		return BeanUtil.beanListToMap(recordList, "memberid");
	}
	@Override
	public List<Adjustment> getAdjustmentList(Long memberid, Timestamp timefrom, Timestamp timeto, String status){
		DetachedCriteria query = DetachedCriteria.forClass(Adjustment.class);
		if(memberid != null) query.add(Restrictions.eq("memberid", memberid));
		query.add(Restrictions.ge("updatetime", timefrom));
		query.add(Restrictions.lt("updatetime", timeto));
		if(StringUtils.isNotBlank(status)) query.add(Restrictions.eq("status", status));
		query.addOrder(Order.desc("updatetime"));
		query.addOrder(Order.asc("addtime"));
		List<Adjustment> result = hibernateTemplate.findByCriteria(query);
		return result;
	}

	@Override
	public List<GewaOrder> getPaidOrderList(Long memberid, Timestamp from, Timestamp to){
		DetachedCriteria query = DetachedCriteria.forClass(GewaOrder.class);
		if(memberid != null) query.add(Restrictions.eq("memberid", memberid));
		query.add(Restrictions.like("status", OrderConstant.STATUS_PAID, MatchMode.START));
		query.add(Restrictions.ge("paidtime", from));
		query.add(Restrictions.lt("paidtime", to));
		List<GewaOrder> result = hibernateTemplate.findByCriteria(query);
		return result;
	}
	@Override
	public List<GewaOrder> getPaidOrderList(Timestamp timefrom, Timestamp timeto, int from, int maxnum){
		DetachedCriteria query = DetachedCriteria.forClass(GewaOrder.class);
		query.add(Restrictions.like("status", OrderConstant.STATUS_PAID, MatchMode.START));
		query.add(Restrictions.ge("paidtime", timefrom));
		query.add(Restrictions.lt("paidtime", timeto));
		query.addOrder(Order.asc("paidtime"));
		List<GewaOrder> result = hibernateTemplate.findByCriteria(query, from, maxnum);
		return result;
	}
	@Override
	public int getPaidOrderCount(Timestamp timefrom, Timestamp timeto) {
		DetachedCriteria query = DetachedCriteria.forClass(GewaOrder.class);
		query.add(Restrictions.like("status", OrderConstant.STATUS_PAID, MatchMode.START));
		query.add(Restrictions.ge("paidtime", timefrom));
		query.add(Restrictions.lt("paidtime", timeto));
		query.setProjection(Projections.rowCount());
		List result = hibernateTemplate.findByCriteria(query);
		return Integer.parseInt("" + result.get(0));
	}
	@Override
	public List<AccountRecord> getAccountRecordList(Long checkid){
		DetachedCriteria query = DetachedCriteria.forClass(AccountRecord.class);
		query.add(Restrictions.eq("checkid", checkid));
		query.addOrder(Order.asc("accountid"));
		List<AccountRecord> result = hibernateTemplate.findByCriteria(query);
		return result;
	}

	@Override
	public <T extends GewaOrder> List<T> getUnpaidOrderList(Class<T> clazz, Long memberid,String ukey) {
		DetachedCriteria query = DetachedCriteria.forClass(clazz);
		query.add(Restrictions.eq("memberid", memberid));
		if(StringUtils.isNotBlank(ukey)){
			query.add(Restrictions.eq("ukey", ukey));
		}
		query.add(Restrictions.like("status", OrderConstant.STATUS_NEW, MatchMode.START));
		query.add(Restrictions.gt("validtime", new Timestamp(System.currentTimeMillis())));
		List<T> orderList = hibernateTemplate.findByCriteria(query);
		return orderList;
	}
	private Integer[] getMemberAccountTotal(){
		String query = "select new map(sum(banlance) as total,sum(othercharge) as wabi) from MemberAccount";
		List<Map> result = hibernateTemplate.find(query);
		return new Integer[]{Integer.parseInt("" + result.get(0).get("total")), Integer.parseInt("" + result.get(0).get("wabi"))};
	}

	@Override
	public List<Adjustment> getAdjustmentList(String status, int from, int maxrows) {
		DetachedCriteria query = DetachedCriteria.forClass(Adjustment.class);
		if(StringUtils.isNotBlank(status)) query.add(Restrictions.eq("status", status));
		query.addOrder(Order.desc("updatetime"));
		query.addOrder(Order.asc("addtime"));
		List<Adjustment> result = hibernateTemplate.findByCriteria(query, from, maxrows);
		return result;
	}
	@Override
	public Integer getAdjustmentCount(String status) {
		DetachedCriteria query = DetachedCriteria.forClass(Adjustment.class);
		if(StringUtils.isNotBlank(status)) query.add(Restrictions.eq("status", status));
		query.setProjection(Projections.rowCount());
		List result = hibernateTemplate.findByCriteria(query);
		return Integer.valueOf(""+result.get(0));
	}

	@Override
	public ErrorCode approveAdjustment(Adjustment adjustment, MemberAccount account, Long userid) {
		if(adjustment.getStatus().equals(Adjustment.STATUS_SUCCESS)) return ErrorCode.getFailure("该项已完款！");
		if(adjustment.getSubtractAmount() > account.getBanlance()){
			return ErrorCode.getFailure("无法调整，账户余额不足！");
		}
		int amount = adjustment.getAddAmount();
		int bankcharge = adjustment.getAddBankcharge();
		int othercharge = adjustment.getAddOthercharge();
		int depositcharge = adjustment.getAddDepositCharge();
		
		if(account.getBankcharge()+bankcharge<0) return ErrorCode.getFailure("无法调整，可退余额不足！");
		if(account.getOthercharge()+othercharge<0) return ErrorCode.getFailure("无法调整，不可退余额不足");
		if(account.getDepositcharge()+depositcharge<0) return ErrorCode.getFailure("无法调整，保证金余额不足");
		if(amount!=(bankcharge+othercharge+depositcharge)){
			return ErrorCode.getFailure("无法调整，总调整余额不等于账户金额加瓦币加保证金");
		}
		account.addBanlance(amount);
		account.addBankcharge(bankcharge);
		account.addWabicharge(othercharge);
		account.addDepositcharge(depositcharge);
		adjustment.setUpdatetime(new Timestamp(System.currentTimeMillis()));
		adjustment.setClerkid(userid);
		adjustment.setStatus(Adjustment.STATUS_SUCCESS);
		baseDao.saveObject(account);
		baseDao.saveObject(adjustment);
		return ErrorCode.SUCCESS;
	}
	@Override
	public ErrorCode approveAdjustment(Adjustment adjustment, Long userid) {
		MemberAccount account = baseDao.getObject(MemberAccount.class, adjustment.getAccountid());
		return approveAdjustment(adjustment, account, userid);
	}
	@Override
	public List<Adjustment> getAdjustmentListByMemberId(Long memberid, String status){
		DetachedCriteria query = DetachedCriteria.forClass(Adjustment.class);
		query.add(Restrictions.eq("memberid", memberid));
		if(StringUtils.isNotBlank(status)) query.add(Restrictions.eq("status", status));
		query.addOrder(Order.desc("updatetime"));
		query.addOrder(Order.asc("addtime"));
		List<Adjustment> result = hibernateTemplate.findByCriteria(query);
		return result;
	}
	@Override
	public Integer getGoodsCountByTag(String tag){
		DetachedCriteria query = DetachedCriteria.forClass(Goods.class);
		Date nowTime = new Timestamp(System.currentTimeMillis());
		query.add(Restrictions.eq("tag", tag));
		query.add(Restrictions.le("releasetime", nowTime));
		query.add(Restrictions.ge("totime", nowTime));
		query.setProjection(Projections.rowCount());
		List<Goods> goodsList = hibernateTemplate.findByCriteria(query);
		if(goodsList.get(0) == null) return 0;
		return new Integer (goodsList.get(0)+"");
	}
	@Override
	public Member getMemberByMobile(String mobile) {
		String query = "from Member where mobile=?";
		List<Member> memberList = hibernateTemplate.find(query, mobile);
		if(memberList.isEmpty()) return null;
		return memberList.get(0);
	}
	@Override
	public ErrorCode validateAccount(Long memberid, Integer amount){
		MemberAccount account = baseDao.getObjectByUkey(MemberAccount.class, "memberid", memberid, false);
		if (account == null) {
			return ErrorCode.getFailure("您还没有建立账户！");
		}
		if(account.getBanlance() < amount){
			return ErrorCode.getFailure("您账户内的余额不足，仅为"+account.getBanlance()+"元,您需要支付" + amount + "元");
		}
		return ErrorCode.SUCCESS;
	}
	@Override
	public ErrorCode validateAccount(Long memberid, String password) {
		MemberAccount account = baseDao.getObjectByUkey(MemberAccount.class, "memberid", memberid, false);
		if (account == null) {
			return ErrorCode.getFailure("您还没有建立账户！");
		}
		if(!PayUtil.passEquals(password, account.getPassword())){
			return ErrorCode.getFailure("支付密码不正确！");
		}
		return ErrorCode.SUCCESS;
	}
	@Override
	public ErrorCode validateAccount(Long memberid, Integer amount, String password){
		if(amount == 0) return ErrorCode.SUCCESS;
		MemberAccount account = baseDao.getObjectByUkey(MemberAccount.class, "memberid", memberid, false);
		if (account == null) {
			return ErrorCode.getFailure("您还没有建立账户！");
		}
		if(account.getBanlance() < amount){
			return ErrorCode.getFailure("您账户内的余额不足，仅为"+account.getBanlance()+"元,您需要支付" + amount + "元");
		}
		if(!PayUtil.passEquals(password, account.getPassword())){
			return ErrorCode.getFailure("支付密码不正确！");
		}
		if(!account.hasRights()){
			return ErrorCode.getFailure("你的账户暂被禁用，如果有疑问请联系客服");
		}
		return ErrorCode.SUCCESS;
	}
	@Override
	public Integer getChargeCountByMemberId(Long memberId, boolean ischarge, Timestamp startTime, Timestamp endTime) {
		DetachedCriteria query = DetachedCriteria.forClass(Charge.class);
		query.add(Restrictions.eq("memberid", memberId));
		if(startTime !=null && endTime !=null){
			query.add(Restrictions.between("addtime", startTime, endTime));
		}else if(startTime !=null ){
			query.add(Restrictions.ge("addtime", startTime));
		}else if(endTime != null){
			query.add(Restrictions.le("addtime", endTime));
		}
		if(ischarge){
			query.add(Restrictions.eq("chargetype", ChargeConstant.TYPE_CHARGE));
		}
		query.setProjection(Projections.rowCount());
		List result = hibernateTemplate.findByCriteria(query);
		return new Integer(result.get(0)+"");
	}
	@Override
	public ErrorCode isAllowChangePaymethod(GewaOrder order, String newpaymethod, String newpaybank) {
		if(order.getDue()==0){
			return ErrorCode.SUCCESS;
		}
		List<Discount> discountList = getOrderDiscountList(order);
		for(Discount discount: discountList){
			if(discount.getTag().equals(PayConstant.DISCOUNT_TAG_PARTNER)){
				SpecialDiscount sd = baseDao.getObject(SpecialDiscount.class, discount.getRelatedid());
				if(!sd.isValidPaymethod(newpaymethod, newpaybank)){
					return ErrorCode.getFailure("您参与“" + sd.getDescription() + "”优惠活动，不能更改支付方式！");
				}
			}
		}
		Map<String, String> otherFeeMap = JsonUtils.readJsonToMap(order.getOtherFeeRemark());
		String umpayfee = otherFeeMap.get(OtherFeeDetail.FEETYPE_U);
		if(StringUtils.isNotBlank(umpayfee) && Integer.parseInt(umpayfee)>0 && !order.getPaymethod().equals(newpaymethod)){
			//产生费用的一定绑定支付方式
			return ErrorCode.getFailure("不可更换支付方式！！");
		}
		if(StringUtils.equals(order.getStatus(), OrderConstant.STATUS_NEW_CONFIRM) 
				&& StringUtils.startsWith(newpaymethod, PaymethodConstant.PAYMETHOD_UMPAY) && !StringUtils.startsWith(order.getPaymethod(),PaymethodConstant.PAYMETHOD_UMPAY)){
			return ErrorCode.getFailure("不可更换支付方式！！");
		}
		return ErrorCode.SUCCESS;
	}
	@Override
	public AccountRecord getAccountRecord(Long checkid, Long memberid) {
		String query = "from AccountRecord t where t.checkid=? and t.memberid=?";
		List<AccountRecord> result = hibernateTemplate.find(query, checkid, memberid);
		if(result.size()>0) return result.get(0);
		return null;
	}
	@Override
	public List<SpecialDiscount> getSpecialDiscountList(String tag, String opentype) {
		Timestamp cur = DateUtil.getCurTruncTimestamp();
		String query = "select id from SpecialDiscount where tag=? and timeto >= ? and opentype=? order by sortnum";
		List<Long> idList = hibernateTemplate.find(query, tag, cur, opentype);
		List<SpecialDiscount> result = baseDao.getObjectList(SpecialDiscount.class, idList);
		return result;
	}
	@Override
	public List<SpecialDiscount> getPartnerSpecialDiscountList(String tag, Long partnerid) {
		Timestamp cur = DateUtil.getCurTruncTimestamp();
		String query = "select id from SpecialDiscount where tag=? and timeto >= ? and opentype= ? and ptnids = ? order by sortnum";
		List<Long> idList = hibernateTemplate.find(query, tag, cur, SpecialDiscount.OPENTYPE_PARTNER, partnerid+"");
		List<SpecialDiscount> result = baseDao.getObjectList(SpecialDiscount.class, idList);
		return result;
	}
	@Override
	public List<SpecialDiscount> getMobileSpecialDiscountList(String tag, Long partnerid) {
		Timestamp cur = DateUtil.getCurTruncTimestamp();
		String query = "select id from SpecialDiscount where tag=? and timeto >= ? and opentype= ? order by sortnum";
		List<SpecialDiscount> result = new ArrayList<SpecialDiscount>();
		List<Long> spidList = hibernateTemplate.find(query, tag, cur, SpecialDiscount.OPENTYPE_WAP);
		List<SpecialDiscount> sdList = baseDao.getObjectList(SpecialDiscount.class, spidList);
		for(SpecialDiscount sd : sdList){
			List<Long> idList = BeanUtil.getIdList(sd.getPtnids(), ",");
			if(idList.contains(partnerid)){
				result.add(sd);
			}
		}
		return result;
	}
	@Override
	public ErrorCode addSpdiscountCharge(GewaOrder order, SpecialDiscount sd, Long userid, boolean isSupplement) {
		if(order.sureOutPartner()) return ErrorCode.getFailure("商家订单，无法返利！");
		String flag = "rebates" + sd.getId();
		Map<String, String> otherinfo = VmUtils.readJsonToMap(order.getOtherinfo());
		if(otherinfo.containsKey(flag)) return ErrorCode.getFailure("已经加入过奖金！");
		otherinfo.put(flag, "" + sd.getRebates());
		boolean usesp = false;
		List<Discount> discountList = getOrderDiscountList(order);
		for(Discount discount: discountList){
			if(discount.getTag().equals(PayConstant.DISCOUNT_TAG_PARTNER) && discount.getRelatedid().equals(sd.getId())){
				usesp = true;break;
			}
		}
		if(!usesp && !isSupplement) return ErrorCode.getFailure("未使用此优惠！");
		String chargeTradeNo = PayUtil.FLAG_CHARGE + order.getTradeNo().substring(1);
		addRebates(chargeTradeNo, order.getMemberid(), order.getMembername(), order.getTradeNo(), flag, sd.getRebates());
		if(isSupplement) otherinfo.put("supplement", ""+sd.getId());
		order.setOtherinfo(JsonUtils.writeMapToJson(otherinfo));
		baseDao.saveObject(order);
		dbLogger.warn("增加返利[" + userid + "]:" + sd.getId() + "," + order.getTradeNo());
		return ErrorCode.SUCCESS;
	}
	@Override
	public ErrorCode addSpdiscountCard(GewaOrder order, SpecialDiscount sd, Long userid, boolean isSupplement){
		if(order.sureOutPartner()) {
			return ErrorCode.getFailure("商家订单，无法返券！");
		}
		if(!(sd.getDrawactivity() != null && SpecialDiscount.REBATES_CARDD.equals(sd.getRebatestype()) && sd.getBindDrawCardNum() != null && sd.getBindDrawCardNum() > 0)){
			return ErrorCode.getFailure("此优惠不支持返券活动！");
		}
		String flag = "rebatesCard" + sd.getId();
		Map<String, String> otherinfo = VmUtils.readJsonToMap(order.getOtherinfo());
		if(otherinfo.containsKey(flag)){
			return ErrorCode.getFailure("此订单已领取，请不要重复操作！");
		}
		otherinfo.put(flag, "" + sd.getRebates());
		boolean usesp = false;
		List<Discount> discountList = getOrderDiscountList(order);
		for(Discount discount: discountList){
			if(discount.getTag().equals(PayConstant.DISCOUNT_TAG_PARTNER) && discount.getRelatedid().equals(sd.getId())){
				usesp = true;break;
			}
		}
		if(!usesp && !isSupplement){
			return ErrorCode.getFailure("未使用此优惠！");
		}
		DrawActivity da = baseDao.getObject(DrawActivity.class, sd.getDrawactivity());
		if(da == null){
			return ErrorCode.getFailure( "对应的奖品还未设置，敬请耐心等待!");
		}
		int joinCount = drawActivityService.getJoinDrawActivityCount(da.getId());
		if(joinCount >= sd.getRebatesmax()){
			return ErrorCode.getFailure("很抱歉，您来晚了，奖品已领完!");
		}
		String[] opkeyList = SpecialDiscountHelper.getUniqueKey(sd, null, order);
		Set<String> opkeySet = new HashSet(Arrays.asList(opkeyList));
		for(String opkey: opkeySet){
			boolean allow = operationService.updateOperation("draw" + opkey, sd.getDrawperiod()*60, sd.getBindDrawCardNum());
			if(!allow) return ErrorCode.getFailure( "本活动每个用户" + SpecialDiscountHelper.getTimeStr(sd.getDrawperiod())  +"只能领取" + sd.getBindDrawCardNum() + "次！");
		}
		VersionCtl mvc = drawActivityService.gainMemberVc(order.getMemberid() + "");
		ErrorCode<WinnerInfo>  code = drawActivityService.baseClickDraw(da, mvc, false, baseDao.getObject(Member.class, order.getMemberid()));
		if(!code.isSuccess()){
			return code;
		}
		WinnerInfo winner = code.getRetval();
		if(isSupplement) otherinfo.put("supplement", ""+sd.getId());
		otherinfo.put("winnerInfoIds","" + winner.getId());
		order.setOtherinfo(JsonUtils.writeMapToJson(otherinfo));
		baseDao.saveObject(order);
		dbLogger.warn("增加返D卡[" + userid + "]:" + sd.getId() + "," + order.getTradeNo());
		if(StringUtils.equals(order.getStatus(), OrderConstant.STATUS_PAID_SUCCESS)){
			drawActivityService.sendPrize(baseDao.getObject(Prize.class,winner.getPrizeid()), winner, false);
		}else{
			return ErrorCode.getFailure("您的奖品已领取，系统正在处理中，稍后会发送到账号！");
		}
		return ErrorCode.SUCCESS;
	}
	private void addRebates(String chargeTradeNo, Long memberid, String membername, String payseqno, 
			String paybank, Integer totalfee){
		Charge charge = new Charge(chargeTradeNo, ChargeConstant.WABIPAY);
		charge.setMemberid(memberid);
		charge.setMembername(membername);
		charge.setPaymethod(PaymethodConstant.PAYMETHOD_SYSPAY);
		charge.setPayseqno(payseqno);
		charge.setPaybank(paybank);
		charge.setTotalfee(totalfee);
		charge.setStatus(Charge.STATUS_PAID);
		baseDao.saveObject(charge);
		MemberAccount account = baseDao.getObjectByUkey(MemberAccount.class, "memberid", charge.getMemberid(), false);
		if(account == null) {
			Member member = baseDao.getObject(Member.class, memberid);
			account = createNewAccount(member);
		}
		account.addBanlance(charge.getTotalfee());
		account.addWabicharge(charge.getTotalfee());
		baseDao.saveObject(account);
	}
	@Override
	public ErrorCode removeSpdiscountCharge(GewaOrder order, SpecialDiscount sd, Long userid) {
		if(order.sureOutPartner()) return ErrorCode.getFailure("商家订单，无返利！");
		String flag = "rebates" + sd.getId();
		Map<String, String> otherinfo = VmUtils.readJsonToMap(order.getOtherinfo());
		if(!otherinfo.containsKey(flag)) return ErrorCode.getFailure("未加入过返利！");
		otherinfo.remove(flag);
		otherinfo.remove("supplement");
		Charge charge = baseDao.getObjectByUkey(Charge.class, "tradeNo", PayUtil.FLAG_CHARGE + order.getTradeNo().substring(1), false);
		if(charge==null) return ErrorCode.getFailure("未找到返利记录，请联系系统管理员！");
		baseDao.removeObject(charge);
		MemberAccount account = baseDao.getObjectByUkey(MemberAccount.class, "memberid", charge.getMemberid(), false);
		account.addBanlance(-charge.getTotalfee());
		account.addWabicharge(-charge.getTotalfee());
		baseDao.saveObject(account);
		
		order.setOtherinfo(JsonUtils.writeMapToJson(otherinfo));
		baseDao.saveObject(order);
		dbLogger.warn("取消返利[" + userid + "]:" + sd.getId() + "," + order.getTradeNo());
		return ErrorCode.SUCCESS;
	}
	@Override
	public ErrorCode validUse(GewaOrder order){
		Map<String, String> otherFeeMap = JsonUtils.readJsonToMap(order.getOtherFeeRemark());
		String umpayfee = otherFeeMap.get(OtherFeeDetail.FEETYPE_U);
		if (StringUtils.isNotBlank(umpayfee) && Integer.parseInt(umpayfee)>0){
			return ErrorCode.getFailure("订单已有手续费，不能再使用优惠！");
		}
		return ErrorCode.SUCCESS;
	}
	@Override
	public ErrorCode<Integer> getSpdiscountAmount(SpecialDiscountHelper sdh, GewaOrder order, SpecialDiscount sd, Spcounter spcounter, PayValidHelper pvh) {
		List<Cpcounter> cpcounterList = new ArrayList<Cpcounter>();
		if(spcounter != null){
			cpcounterList = baseDao.getObjectListByField(Cpcounter.class, "spcounterid", spcounter.getId());
		}
		ErrorCode<Integer> result = sdh.getSpdiscountAmount(sd, spcounter, cpcounterList, pvh);
		if(!result.isSuccess()) return result;
		String[] opkeyList = SpecialDiscountHelper.getUniqueKey(sd, spcounter, order);
		for(String opkey: opkeyList){
			if(!operationService.isAllowOperation(opkey, OperationService.ONE_MINUTE * sd.getLimitperiod(), sd.getLimitnum())){
				return ErrorCode.getFailure(sd.getLimitperiodStr() + "内最多优惠" + sd.getLimitnum() + "次！");
			}
		}
		return result;
	}
	@Override
	public ErrorCode<Map> mobileResetAccountPass(Member member, String password, String repassword, String checkpass){
		Map jsonMap = new HashMap();
		if(StringUtils.isBlank(password)) {
			jsonMap.put("password",  "支付密码不能为空！");
			return ErrorCode.getFailureReturn(jsonMap);
		}
		if(StringUtils.isBlank(repassword)) {
			jsonMap.put("repassword",  "确认支付密码不能为空！");
			return ErrorCode.getFailureReturn(jsonMap);
		}
		if(!StringUtils.equals(password, repassword)) {
			jsonMap.put("repassword", "支付密码与确认支付密码不一致！");
			return ErrorCode.getFailureReturn(jsonMap);
		}
		if(StringUtils.equals(password, "123456")){
			jsonMap.put("password", "支付密码过于简单！");
			return ErrorCode.getFailureReturn(jsonMap);
		}
		if(!ValidateUtil.isPassword(password)){
			jsonMap.put("password", "支付密码格式不正确,只能是字母，数字，下划线，长度6—14位！");
			return ErrorCode.getFailureReturn(jsonMap);
		}
		if(StringUtils.equals(StringUtil.md5(password), member.getPassword())){
			jsonMap.put("password", "支付密码不能跟登录密码相同！");
			return ErrorCode.getFailureReturn(jsonMap);
		}
		ErrorCode bindCode = bindMobileService.checkBindMobile(BindConstant.TAG_ACCOUNT_BACKPASS, member.getMobile(), checkpass);
		if(!bindCode.isSuccess()) {
			jsonMap.put("msg", bindCode.getMsg());
			return ErrorCode.getFailureReturn(jsonMap);
		}
		MemberAccount account = baseDao.getObjectByUkey(MemberAccount.class, "memberid", member.getId(), false);
		if(account == null) {
			jsonMap.put("msg", "请先创建支付帐号！");
			return ErrorCode.getFailureReturn(jsonMap);
		}
		account.setPassword(PayUtil.getPass(password));
		account.setUpdatetime(DateUtil.getCurFullTimestamp());
		baseDao.saveObject(account);
		return ErrorCode.getSuccessReturn(jsonMap);
	}
	@Override
	public void afterPropertiesSet() throws Exception {
		reInitLimitPayList();
	}
	@Override
	public String getBindPay(List<Discount> discountList, Map<String, String> orderOtherinfo, GewaOrder order){
		String bindpay = null;
		if(discountList.size() > 0){
			bindpay = orderOtherinfo.get(PayConstant.KEY_CARDBINDPAY);
			if(StringUtils.isBlank(bindpay)){	
				for(Discount discount: discountList){
					if(StringUtils.equals(PayConstant.DISCOUNT_TAG_PARTNER, discount.getTag())){
						SpecialDiscount sd = baseDao.getObject(SpecialDiscount.class, discount.getRelatedid());
						if(StringUtils.isNotBlank(sd.getPaymethod())){
							bindpay = sd.getPaymethod();
							break;
						}
					}
				}
			}
		}else {
			if(StringUtils.equals(order.getPaymethod(), PaymethodConstant.PAYMETHOD_MEMBERCARDPAY)
				||StringUtils.equals(order.getPaymethod(), PaymethodConstant.PAYMETHOD_GEWARA_OFFLINEPAY)
				||StringUtils.equals(order.getPaymethod(), PaymethodConstant.PAYMETHOD_PAYECO_DNA)){
				bindpay = order.getPaymethod();
			}
		}
		Map<String, String> otherFeeMap = JsonUtils.readJsonToMap(order.getOtherFeeRemark());
		String umpayfee = otherFeeMap.get(OtherFeeDetail.FEETYPE_U);
		if(StringUtils.isBlank(bindpay) && StringUtils.isNotBlank(umpayfee) && Integer.parseInt(umpayfee)>0){
			//有费用产生就一定绑定支付，paybank要加上？？
			bindpay = order.getPaymethod();
		}
		return bindpay;
	}
	@Override
	public Spcounter updateSpdiscountMemberCount(SpecialDiscount sd, GewaOrder order) throws OrderException{
		Spcounter spcounter = getSpdiscountCounter(sd);
		String[] opkeyList = SpecialDiscountHelper.getUniqueKey(sd, spcounter, order);
		for(String opkey: opkeyList){
			boolean allow = operationService.updateOperation(opkey,	OperationService.ONE_MINUTE	* sd.getLimitperiod(), sd.getLimitnum(), order.getTradeNo());
			if(!allow){
				dbLogger.error("超出限够数量：" + order.getTradeNo() + "," + opkey);
				throw new OrderException(ApiConstant.CODE_PAY_ERROR, "优惠活动超出参与次数限制:" + opkey);
			}
		}
		return spcounter;
	}
	@Override
	public void updateSpdiscountPaidCount(SpecialDiscount sd, GewaOrder order){
		//FIXME: 单个订单并发调用，想办法判断此订单是否更新过名额
		Spcounter spcounter = getSpdiscountCounter(sd);
		//注意，spcounter不能为空
		spcounter.setSellordernum(spcounter.getSellordernum()+1);
		spcounter.setAllordernum(spcounter.getAllordernum()+1);
		spcounter.setSellquantity(spcounter.getSellquantity()+order.getQuantity());
		spcounter.setAllquantity(spcounter.getAllquantity()+order.getQuantity());
		baseDao.saveObject(spcounter);

		List<Cpcounter> cpcounterList = baseDao.getObjectListByField(Cpcounter.class, "spcounterid", spcounter.getId());
		Cpcounter cityCpcounter = SpecialDiscountHelper.updateCitySellCounter(cpcounterList, order);
		if(cityCpcounter != null) baseDao.saveObject(cityCpcounter);
		Cpcounter partnerCpcounter = SpecialDiscountHelper.updatePartnerSellCounter(cpcounterList, order);
		if(partnerCpcounter != null) baseDao.saveObject(partnerCpcounter);
		
		dbLogger.warnWithType(LogTypeConstant.LOG_TYPE_LOGIC, "use_sd_id:" + sd.getId() + ", use_sp_id:" + spcounter.getId() + ", orderid" + order.getId());

		sd.setSellcount(sd.getSellcount() + order.getQuantity());
		baseDao.saveObject(sd);
	}
	@Override
	public void updateSpdiscountAddCount(SpecialDiscount sd, Spcounter spcounter, GewaOrder order){
		//注意，spcounter不能为空
		dbLogger.warnWithType(LogTypeConstant.LOG_TYPE_LOGIC, "use_sd_id:" + sd.getId() + ", use_sp_id:" + spcounter.getId() + ", orderid" + order.getId());
		List<Cpcounter> cpcounterList = baseDao.getObjectListByField(Cpcounter.class, "spcounterid", spcounter.getId());
		String cpcounters = "";
		Cpcounter cityCpcounter = SpecialDiscountHelper.updateCityAddCounter(spcounter, cpcounterList, order);
		if(cityCpcounter != null) {
			baseDao.saveObject(cityCpcounter);
			cpcounters += cityCpcounter.getId();
		}
		Cpcounter partnerCpcounter = SpecialDiscountHelper.updatePartnerAddCounter(spcounter, cpcounterList, order);
		if(partnerCpcounter != null) {
			baseDao.saveObject(partnerCpcounter);
			if(StringUtils.isNotBlank(cpcounters)) cpcounters += ",";
			cpcounters += partnerCpcounter.getId();
		}
		if(StringUtils.equals(spcounter.getCtltype(), Spcounter.CTLTYPE_QUANTITY)){
			spcounter.setAllowaddnum(spcounter.getAllowaddnum() - order.getQuantity());
		}else{
			spcounter.setAllowaddnum(spcounter.getAllowaddnum() - 1);
		}
		SdRecord sdRecord = new SdRecord(order.getTradeNo(), order.getQuantity(), spcounter.getId(), cpcounters, order.getValidtime());
		baseDao.saveObject(sdRecord);
		baseDao.saveObject(spcounter);
	}
	@Override
	public ErrorCode restoreSdCounterBySpcounter(Long spcounterid){
		//未支付的订单
		String query = "from SdRecord t where validtime < ? and spcounterid = ? ";
		List<SdRecord> recordList = hibernateTemplate.find(query, new Timestamp(System.currentTimeMillis()), spcounterid);
		Spcounter spcounter = baseDao.getObject(Spcounter.class, spcounterid);
		List<Cpcounter> cpcounterList = baseDao.getObjectListByField(Cpcounter.class, "spcounterid", spcounterid);
		Map<Long, Cpcounter> cpcounterMap = BeanUtil.beanListToMap(cpcounterList, "id");
		int restoreCount = 0, restoreSum = 0, remain = 0;
		for(SdRecord record: recordList){
			GewaOrder order = baseDao.getObjectByUkey(GewaOrder.class, "tradeNo", record.getTradeNo());
			if(!order.getStatus().startsWith(OrderConstant.STATUS_PAID)){
				int num = 1;
				if(StringUtils.equals(spcounter.getCtltype(), Spcounter.CTLTYPE_QUANTITY)){
					num = record.getQuantity();
				}
				List<Long> cpidList = BeanUtil.getIdList(record.getCpcounters(), ",");
				for(Long cpid: cpidList){
					Cpcounter cp = cpcounterMap.get(cpid);
					if(cp==null){
						dbLogger.warn("InvalidCpcounter:" + BeanUtil.getBeanMap(record));
					}else{
						cp.setAllownum(cp.getAllownum() + num);
					}
				}
				restoreCount ++;
				restoreSum += num;
			}
		}
		if(StringUtils.equals(spcounter.getCtltype(), Spcounter.CTLTYPE_QUANTITY)){
			remain =  spcounter.getBasenum() - spcounter.getSellquantity();
		}else{
			remain =  spcounter.getBasenum() - spcounter.getSellordernum();
		}
		int restoreAllow = spcounter.getAllowaddnum() + restoreSum;
		int allow = Math.min(remain, restoreAllow);
		String before = "" + BeanUtil.getBeanMap(spcounter);
		spcounter.setAllowaddnum(allow);
		baseDao.saveObject(spcounter);
		baseDao.saveObjectList(cpcounterList);
		baseDao.removeObjectList(recordList);
		return ErrorCode.getSuccess("订单个数：" + recordList.size() + ", 过时未付款：" + restoreSum + ", 恢复量：" + restoreCount + "恢复前：" + before + ", 实际恢复为：" + allow);
	}

	@Override
	public Spcounter getSpdiscountCounter(SpecialDiscount sd) {
		if(sd.getSpcounterid()!=null){
			Spcounter spcounter = baseDao.getObject(Spcounter.class, sd.getSpcounterid());
			return spcounter;
		}
		return null;
	}
	
	
	/**
	 * 抽奖活动为中奖会员加瓦币
	 * @param member 会员
	 * @param totalfee 增加瓦币金额
	 * @param drawActivityId 抽奖活动id
	 * @return
	 */
	public ErrorCode<?> addWaiBiByDrawActivity(String drawActivityId,Member member,Integer totalfee){
		String chargeTradeNo=PayUtil.getChargeTradeNo();
		String flag="draw_"+drawActivityId;
		addRebates(chargeTradeNo,member.getId(),member.getNickname(),chargeTradeNo,flag,totalfee);
		return ErrorCode.SUCCESS;
	}
	@Override
	public ErrorCode validateWbPay(Member member, MemberAccount account, String payPass, String wbpay){
		if(StringUtils.isBlank(payPass)) return ErrorCode.getFailure("支付密码不能为空！");
		if(StringUtils.isBlank(wbpay)) return ErrorCode.getFailure("请选择余额支付方式！");
		if(account == null || account.isNopassword()) return ErrorCode.getFailure("账户为空或密码过于简单！");
		if(!PayUtil.passEquals(payPass, account.getPassword())) return ErrorCode.getFailure("支付密码不正确！");
		if(!account.hasRights()){
			return ErrorCode.getFailure("你的账户暂被禁用，如果有疑问请联系客服");
		}
		int banlance = account.getBanlance(), bankcharge = account.getBankcharge(), othercharge = account.getOthercharge(), depositcharge = account.getDepositcharge();
		Long memberid = account.getMemberid();
		if(banlance==0) return ErrorCode.getFailure(ApiConstant.CODE_SIGN_ERROR, "账户余额为0，不能支付！");
		if(banlance!=(bankcharge + othercharge + depositcharge)){ 
			dbLogger.warn("严重异常：memberid:" + memberid + ", banlance:" + banlance + ", bankcharge:" + bankcharge + ", othercharge:" + othercharge);
			return ErrorCode.getFailure("账户金额异常，请联系客服！");
		}
		if(StringUtils.equals(ChargeConstant.WABIPAY, wbpay) && account.getOthercharge()<=0) return ErrorCode.getFailure("瓦币为0，不能选择瓦币支付");
		if(StringUtils.equals(ChargeConstant.BANKPAY, wbpay) && account.getBankcharge()<=0) return ErrorCode.getFailure("账户金额为0，不能选择账户金额支付");
		return ErrorCode.SUCCESS;
	}
	@Override
	public Charge addChargeByOrder(Member member, MemberAccount account, GewaOrder order, String chargeMethod) {
		String chargeno = PayUtil.FLAG_CHARGE + order.getTradeNo().substring(1);
		int totalfee = 0;
		Charge charge = baseDao.getObjectByUkey(Charge.class, "tradeNo", chargeno, false);
		if(charge==null){
			charge = new Charge(chargeno, ChargeConstant.WABIPAY);
			charge.setMemberid(member.getId());
			charge.setMembername(member.getNickname());
			charge.setOutorderid(order.getId());
			charge.setValidtime(order.getValidtime());
		}
		charge.setPaymethod(chargeMethod);
		totalfee = order.getDue() - account.getBanlance();
		charge.setTotalfee(totalfee);
		charge.setChargetype(ChargeConstant.TYPE_ORDER);
		baseDao.saveObject(charge);
		return charge;
	}
	
	@Override
	public void resetSpcounter(Spcounter spcounter, Long userid){
		Timestamp cur = DateUtil.getCurFullTimestamp();
		Timestamp periodtime = spcounter.getPeriodtime();
		if(cur.after(DateUtil.addMinute(periodtime, spcounter.getPeriodMinute()))){//在周期时间之后才能复位
			monitorService.saveAddLog(userid, Spcounter.class, spcounter.getId(), spcounter);
			Double tmpMinute = DateUtil.getDiffMinu(cur, periodtime);
			int time = tmpMinute.intValue() / spcounter.getPeriodMinute();
			Timestamp tmpTime = DateUtil.addMinute(periodtime, spcounter.getPeriodMinute()*time);
			spcounter.setPeriodtime(tmpTime);
			spcounter.setAllowaddnum(spcounter.getBasenum());
			
			spcounter.setSellordernum(0);
			spcounter.setSellquantity(0);
			
			List<Cpcounter> cpcounterList = baseDao.getObjectListByField(Cpcounter.class, "spcounterid", spcounter.getId());
			for (Cpcounter cpcounter : cpcounterList) {
				monitorService.saveAddLog(userid, Cpcounter.class, cpcounter.getId(), cpcounter);
				cpcounter.setAllownum(cpcounter.getBasenum());
				cpcounter.setSellquantity(0);
				cpcounter.setSellorder(0);
			}
			baseDao.saveObjectList(cpcounterList);
			baseDao.saveObject(spcounter);
		}
	}
	@Override
	public ErrorCode usePayServer(String paymethod, Map<String, String> params, String clientIp, HttpServletRequest request, HttpServletResponse response, ModelMap model) {
		String returnCode = params.get("returnCode");
		if("0000".equals(returnCode)){
			String submitParams = params.get("submitParams");
			boolean verify = CAUtil.doCheck(submitParams, params.get("sign"), NewPayUtil.getGewapubkey(), "SHA1WithRSA");
			if(!verify) return ErrorCode.getFailure("签名验证错误！！");
			String method = params.get("httpMethod");
			String encoding = params.get("httpEncoding");
			try {
				request.setCharacterEncoding(encoding);
			} catch (Exception e) {
				e.printStackTrace();
			}
			response.setCharacterEncoding(encoding);
			Map<String, String> submitMap = VmUtils.readJsonToMap(submitParams);
			String[] paramNames = params.get("paramNames").split(",");
			String payurl = params.get("payurl");
			model.put("method", method);
			model.put("submitParams", submitMap);
			model.put("paramsNames", paramNames);
			model.put("payUrl", payurl);
			dbLogger.warn("the ip changed of the tradeNo :" + params);
			return ErrorCode.SUCCESS;
		}else {
			return ErrorCode.getFailure("返回code错误：" + returnCode + ", 错误消息:" + params.get("returnMsg"));
		}
	}
	@Override
	public ErrorCode usePayServer(GewaOrder order, String clientIp, Map paramsData, String version, HttpServletRequest request, ModelMap model) {
		ErrorCode<Map<String, String>> result = getNetPayParamsV2(order, clientIp, version);
		if(!result.isSuccess()) return result;
		Map<String, String> params = result.getRetval();
		String returnCode = params.get("returnCode");
		if("0000".equals(returnCode)){
			String submitParams = params.get("submitParams");
			boolean verify = CAUtil.doCheck(submitParams, params.get("sign"), NewPayUtil.getGewapubkey(), "SHA1WithRSA");
			if(!verify) return ErrorCode.getFailure("签名验证错误！！");
			String method = params.get("httpMethod");
			String encoding = params.get("httpEncoding");
			try {
				request.setCharacterEncoding(encoding);
			} catch (Exception e) {
				e.printStackTrace();
			}
			Map<String, String> submitMap = VmUtils.readJsonToMap(submitParams);
			String[] paramNames = params.get("paramNames").split(",");
			String payurl = params.get("payurl");
			for(String key : paramNames){
				paramsData.put(key, submitMap.get(key));
			}
			model.put("method", method);
			model.put("payUrl", payurl);
			return ErrorCode.SUCCESS;
		}else {
			return ErrorCode.getFailure("返回code错误：" + returnCode + ", 错误消息:" + params.get("returnMsg"));
		}
	}
	@Override
	public ErrorCode usePayServer(Charge charge, String clientIp, Map paramsData, String version, HttpServletRequest request, ModelMap model) {
		Map<String, String> params = getNetChargeParamsV2(charge, clientIp, version);
		String returnCode = params.get("returnCode");
		if("0000".equals(returnCode)){
			String submitParams = params.get("submitParams");
			boolean verify = CAUtil.doCheck(submitParams, params.get("sign"), NewPayUtil.getGewapubkey(), "SHA1WithRSA");
			if(!verify) return ErrorCode.getFailure("签名验证错误！！");
			String method = params.get("httpMethod");
			String encoding = params.get("httpEncoding");
			try {
				request.setCharacterEncoding(encoding);
			} catch (Exception e) {
				e.printStackTrace();
			}
			Map<String, String> submitMap = VmUtils.readJsonToMap(submitParams);
			String[] paramNames = params.get("paramNames").split(",");
			String payurl = params.get("payurl");
			for(String key : paramNames){
				paramsData.put(key, submitMap.get(key));
			}
			model.put("method", method);
			model.put("payUrl", payurl);
			return ErrorCode.SUCCESS;
		}else {
			return ErrorCode.getFailure("返回code错误：" + returnCode + ", 错误消息:" + params.get("returnMsg"));
		}
	}
	@Override
	public ErrorCode refundSupplementOrder(OrderRefund refund, GewaOrder order, Long userid) {
		String msg = "";
		Timestamp cur = new Timestamp(System.currentTimeMillis());
		if(!refund.getRefundtype().equals(RefundConstant.REFUNDTYPE_SUPPLEMENT)) {
			return ErrorCode.getFailure("退款类型有错误！");
		}
		//3.2)恢复余额
		if(order.sureOutPartner()) {//商户订单
			msg += "[商户订单，无法退款到余额，请注意退款到其相应账户]";
		}else{
			MemberAccount account = baseDao.getObjectByUkey(MemberAccount.class, "memberid", order.getMemberid(), false);
			if(account==null){
				Member member = baseDao.getObject(Member.class, order.getMemberid());
				account = createNewAccount(member);
			}
			Adjustment adjustment = new Adjustment(account.getId(), order.getMemberid(), order.getMembername(), Adjustment.CORRECT_ORDER);
			adjustment.setTradeno(order.getTradeNo());
			adjustment.setAmount(refund.getGewaRetAmount());
			adjustment.setContent(order.getTradeNo() + "订单降价退款");
			adjustment.setUpdatetime(cur);
			adjustment.setClerkid(userid);
			adjustment.setStatus(Adjustment.STATUS_SUCCESS);
			
			account.addBanlance(adjustment.getAddAmount());
			Integer bankcharge = 0, othercharge=0, oldbankcharge = account.getBankcharge(), oldothercharge = account.getOthercharge();
			if(StringUtils.equals(order.getPaymethod(), PaymethodConstant.PAYMETHOD_GEWAPAY)){
				Map<String, String> boMap = VmUtils.readJsonToMap(order.getOtherinfo());
				if(boMap.containsKey(ChargeConstant.BANKPAY)){
					bankcharge = Math.min(refund.getGewaRetAmount(), Integer.valueOf(boMap.get(ChargeConstant.BANKPAY)));
					othercharge = refund.getGewaRetAmount() - bankcharge;
				}else{
					othercharge = refund.getGewaRetAmount();
				}
			}else {
				bankcharge = refund.getGewaRetAmount();
			}
			if(bankcharge+othercharge!=refund.getGewaRetAmount()) {
				throw new IllegalArgumentException("订单金额错误，联系系统开发人员！");
			}
			dbLogger.warn(order.getTradeNo() + "退款到余额:瓦币：由"+ oldothercharge + "增加"+othercharge+",账户金额：由"+oldbankcharge+"增加"+bankcharge);
			account.addBankcharge(bankcharge);
			account.addWabicharge(othercharge);
			adjustment.setBankcharge(bankcharge);
			adjustment.setOthercharge(othercharge);
			
			baseDao.saveObject(account);
			baseDao.saveObject(adjustment);
			dbLogger.warn(order.getTradeNo() + "退款到余额....");
		}
		refund.setRefundtime(cur);
		refund.setStatus(RefundConstant.STATUS_SUCCESS);
		baseDao.saveObject(refund);
		return ErrorCode.getSuccess(msg);
	}
	@Override
	public ErrorCode<Integer> bankToWaBi(MemberAccount account, Integer bank) {
		if(account==null) return ErrorCode.getFailure("账户为空，转化失败！");
		if(bank==null || bank<=0) return ErrorCode.getFailure("转化金额必须大于0的整数！");
		if(account.getBankcharge()<bank) return ErrorCode.getFailure("超出实际账户金额，转化失败！");
		account.addBankcharge(-bank);
		account.addWabicharge(bank);
		baseDao.saveObject(account);
		return ErrorCode.getSuccessReturn(0);
	}
	@Override
	public List<PayBank> getPayBankList(String type) {
		String hql = "from PayBank where banktype=? order by sortnum";
		List<PayBank> bankList = hibernateTemplate.find(hql, type);
		return bankList;
	}
	@Override
	public ErrorCode<Double> checkAddpoint(Charge charge){
		if(StringUtils.equals(charge.getChargeto(), ChargeConstant.DEPOSITPAY)){
			return ErrorCode.getFailure("保证金不支付送积分");
		}
		Timestamp starttime = null;
		Timestamp endtime = null;
		double  multiple = 0;
		Map param = new HashMap();
		param.put(MongoData.ACTION_TYPE, "recharge");
		param.put(MongoData.ACTION_TAG, "recharge");
		Map map = mongoService.findOne(MongoData.NS_INTEGRAL, param);
		if(map == null) return ErrorCode.getFailure("暂不开放送积分");
		String isSend = map.get("isSend")+"";
		if(StringUtils.equals(isSend, "Y")){
			starttime = DateUtil.parseTimestamp(map.get("starttime")+"");
			endtime = DateUtil.parseTimestamp(map.get("endtime")+"");
			Timestamp chargetime = charge.getAddtime();
			if(chargetime.after(starttime) && chargetime.before(endtime)){
				multiple = Double.valueOf(map.get("multiple")+"");
				return ErrorCode.getSuccessReturn(multiple);
			}
			return ErrorCode.getFailure("不在时间范围内,暂不送积分");
		}
		return ErrorCode.getFailure("暂不开放送积分");
	}
	@Override
	public String getDecryptIdcard(String encryptIdCard) {
		String encryptKey = config.getString("encryptKey");
		if(StringUtils.isBlank(encryptIdCard)){
			return encryptIdCard;
		}
		return  PKCoderUtil.decryptWithThiDES(encryptKey, encryptIdCard, "utf-8");
	}
	@Override
	public String getEncryptIdcard(String idCard) {
		String encryptKey = config.getString("encryptKey");
		String enCodeCard = "";
		if(StringUtils.isBlank(idCard)){
			return  enCodeCard;
		}else{
			enCodeCard = PKCoderUtil.encryptWithThiDES(encryptKey, idCard, "utf-8");
			return  enCodeCard;
		}
	}
	@Override
	public List<Charge> getChargeList(Long memberid, String status, String chargeto) {
		//TODO:最多返回1000
		String hql = "from Charge where memberid=? and status=? and chargeto = ? order by addtime desc";
		return hibernateTemplate.find(hql, memberid, status, chargeto);
	}
	@Override
	public List<MemberAccount> encryAccounts() {
		DetachedCriteria query = DetachedCriteria.forClass(MemberAccount.class);
		query.add(Restrictions.isNotNull("idcard"));
		query.add(Restrictions.isNotNull("encryidcard"));
		List<MemberAccount> accountList =  hibernateTemplate.findByCriteria(query,0,1000);
		return accountList;
	}
	@Override
	public Long encryIDCard(Long maxid) {
		DetachedCriteria query = DetachedCriteria.forClass(MemberAccount.class);
		query.add(Restrictions.isNotNull("idcard"));
		query.add(Restrictions.gt("memberid", maxid));
		query.addOrder(Order.asc("memberid"));
		
		List<MemberAccount> accountList =  hibernateTemplate.findByCriteria(query, 0, 5000);
		if(accountList.isEmpty()) return null;
		dbLogger.warn(accountList.size()+"");
		int success = 0;
		for (int i = 0; i < accountList.size(); i++) {
			MemberAccount account = accountList.get(i);
			String newId = getEncryptIdcard(StringUtil.ToDBC(account.getIdcard()));
			if(!StringUtils.equals(newId, account.getEncryidcard())){
				account.setEncryidcard(newId);
				baseDao.saveObject(account);
				success ++;
			}
			if(success % 100 == 0){
				dbLogger.warn(account.getMemberid() + ":" + success);
			}
		}
		return accountList.get(accountList.size()-1).getMemberid();
	}
	@Override
	public Integer anlyEncryAccounts() {
		int total1 = 0;
		DetachedCriteria query = DetachedCriteria.forClass(MemberAccount.class);
		query.add(Restrictions.isNull("idcard"));
		query.add(Restrictions.isNotNull("encryidcard"));
		query.setProjection(Projections.rowCount());
		List result =  hibernateTemplate.findByCriteria(query);
		if(result==null||result.isEmpty()||result.get(0) == null) {
			total1=0;
		}else{
			total1=Integer.parseInt("" + result.get(0));
		}
		return total1;
	}
	
	/**
	 * 根据特价活动ID获取计数器信息
	 */
	@Override
	public List<Spcounter> getSpcounterBySpids(List<Long> spids) {
		DetachedCriteria query = DetachedCriteria.forClass(Spcounter.class, "sc");
		DetachedCriteria subquery = DetachedCriteria.forClass(SpecialDiscount.class, "sd");
		subquery.setProjection(Projections.property("sd.id"));
		subquery.add(Restrictions.eqProperty("sc.id", "sd.spcounterid"));
		subquery.add(Restrictions.in("sd.id", spids));
		query.add(Subqueries.exists(subquery));
		return hibernateTemplate.findByCriteria(query);
	}
	
	@Override
	public String getGewaPayPrikey() {
		return gewaPayPrivatekey;
	}
	@Override
	public List<Discount> getOrderDiscountList(GewaOrder order) {
		List<Discount> discountList = null;
		if(StringUtils.isNotBlank(order.getDisreason()) || order.getDiscount() > 0){
			discountList = baseDao.getObjectListByField(Discount.class, "orderid", order.getId());
		}else{
			discountList = new ArrayList<Discount>();
		}
		return discountList;
	}
	@Override
	public ErrorCode<MemberAccount> createOrUpdateAccount(Member member, String realname, String password, String confirmPassword, String idcard) {
		if(StringUtil.getByteLength(realname)>20){
			return ErrorCode.getFailure(ApiConstant.CODE_DATA_ERROR, "真实姓名过长！");
		}
		if("123456".equals(password) || StringUtils.length(password) < 6) {
			return ErrorCode.getFailure(ApiConstant.CODE_DATA_ERROR, "密码过于简单，请重新设置！");
		}
		if(StringUtils.length(password) >18){
			return ErrorCode.getFailure(ApiConstant.CODE_DATA_ERROR, "密码过长，不能超多18位！");
		}
		if(!StringUtils.equals(password, confirmPassword)){
			return ErrorCode.getFailure(ApiConstant.CODE_DATA_ERROR, "两次输入的密码不一致！");
		} 
		if(!ValidateUtil.isIDCard(idcard)){
			return ErrorCode.getFailure(ApiConstant.CODE_DATA_ERROR, "请输入正确格式的身份证号码！");
		}
		
		MemberAccount account = baseDao.getObjectByUkey(MemberAccount.class, "memberid", member.getId(), false);
		if(account == null){
			account = createNewAccount(member);
		}else{
			if(!account.isNopassword()) {
				return ErrorCode.getFailure(ApiConstant.CODE_DATA_ERROR, "该用户已经设置过支付密码！");
			}
		}
		if(StringUtils.equals(StringUtil.md5(password), member.getPassword())){
			return ErrorCode.getFailure(ApiConstant.CODE_DATA_ERROR, "支付密码不能跟登录密码相同！");
		}
		account.setPassword(PayUtil.getPass(password));
		account.setRealname(realname);
		String encryCard = getEncryptIdcard(StringUtil.ToDBC(idcard));
		account.setEncryidcard(encryCard);
		account.setUpdatetime(new Timestamp(System.currentTimeMillis()));
		baseDao.saveObject(account);	
		return ErrorCode.getSuccessReturn(account);
	}
	@Override
	public ErrorCode<MemberAccount> updateAccountPassword(Member member, String oldPassword, String password, String confirmPassword) {
		MemberAccount account = baseDao.getObjectByUkey(MemberAccount.class, "memberid", member.getId(), false);
		if (account == null) {
			account = createNewAccount(member);
		}
		if(StringUtils.isNotBlank(oldPassword)){
			String oldpass = PayUtil.getPass(oldPassword);
			if(!account.getPassword().equals(oldpass)){
				return ErrorCode.getFailure("输入的支付密码不正确！");
			}
		}
		if(StringUtils.isNotBlank(password) || StringUtils.isNotBlank(confirmPassword)){//原始
			if(StringUtils.equals(password, "123456")) return ErrorCode.getFailure("密码过于简单，请重新设置！");
			if(!ValidateUtil.isPassword(password)) return ErrorCode.getFailure("支付密码格式不正确,只能是字母，数字，下划线，长度6—14位！");
			if(StringUtils.equals(password, confirmPassword)){
				if(StringUtils.equals(StringUtil.md5(password), member.getPassword())){
					return ErrorCode.getFailure("支付密码不能跟登录密码相同！");
				}
				account.setPassword(PayUtil.getPass(password));
			}else{
				return ErrorCode.getFailure("两次输入的密码不一致！");
			}
		}else{
			return ErrorCode.getFailure("密码不能为空！");
		}
		account.setUpdatetime(new Timestamp(System.currentTimeMillis()));
		baseDao.saveObject(account);
		return ErrorCode.getSuccessReturn(account);
	}
}
