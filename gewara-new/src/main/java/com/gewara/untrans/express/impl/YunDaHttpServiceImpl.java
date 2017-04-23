package com.gewara.untrans.express.impl;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.gewara.Config;
import com.gewara.constant.ApiConstant;
import com.gewara.constant.sys.LogTypeConstant;
import com.gewara.constant.ticket.OrderExtraConstant;
import com.gewara.model.common.GewaConfig;
import com.gewara.model.express.ExpressOrder;
import com.gewara.model.pay.OrderAddress;
import com.gewara.model.pay.OrderExtra;
import com.gewara.service.DaoService;
import com.gewara.support.ErrorCode;
import com.gewara.untrans.express.YunDaHttpService;
import com.gewara.util.GewaLogger;
import com.gewara.util.HttpResult;
import com.gewara.util.HttpUtils;
import com.gewara.util.JsonUtils;
import com.gewara.util.LoggerUtils;
import com.gewara.util.StringUtil;

@Service("yunDaHttpService")
public class YunDaHttpServiceImpl implements YunDaHttpService {
	protected final transient GewaLogger dbLogger = LoggerUtils.getLogger(getClass(), Config.getServerIp(), Config.SYSTEMID);
	private final static int TIME_OUT = 60000;
	private static final String QUERY = "/query/json.php";
	private static final String INTERFACE = "/interface.php";
	private static final String VERSION = "1.0";
	
	@Autowired@Qualifier("daoService")
	private DaoService daoService;
	
	@Value("${express.yunda.apiUrl}")
	private String yunDaApiUrl;
	
	@Value("${express.yunda.partnerid}")
	private String partnerid;
	
	@Value("${express.yunda.password}")
	private String password;
	
	public ErrorCode<ExpressOrder> qryExpress(ExpressOrder expressOrder){
		if(expressOrder == null) return ErrorCode.getFailure(ApiConstant.CODE_SIGN_ERROR,"快递信息不能为空！");
		if(StringUtils.isBlank(expressOrder.getExpressnote())){
			return ErrorCode.getFailure(ApiConstant.CODE_SIGN_ERROR, "快递单号不能为空！");
		}
		Map<String, String> params = new HashMap<String, String>();
		params.put("mailno", expressOrder.getExpressnote());
		params.put("partnerid", partnerid);
		HttpResult result = HttpUtils.getUrlAsString(yunDaApiUrl + QUERY, params, TIME_OUT);
		if(!result.isSuccess()){
			return ErrorCode.getFailure(ApiConstant.CODE_DATA_ERROR, result.getMsg());
		}
		try{
			Map jsonMap = JsonUtils.readJsonToMap(result.getResponse());
			boolean expressResult = Boolean.parseBoolean((String) jsonMap.get("result"));
			if(!expressResult){
				dbLogger.warnWithType(LogTypeConstant.LOG_TYPE_API, result.getResponse());
				return ErrorCode.getFailure(ApiConstant.CODE_SIGN_ERROR, (String)jsonMap.get("remark"));
			}
			return ErrorCode.getSuccessReturn(expressOrder);
		}catch (Exception e) {
			dbLogger.warnWithType(LogTypeConstant.LOG_TYPE_API, QUERY, e);
			return ErrorCode.getFailure(ApiConstant.CODE_SIGN_ERROR, "快递信息数据错误！");
		}
	}
	
	private String surround(String elname, String value){
		return "<" + elname + ">" + value + "</" + elname + ">";
	}
	
	public ErrorCode saveOrUpdateExpress(OrderExtra order, GewaConfig gewaConfig){
		if(order == null){
			return ErrorCode.getFailure(ApiConstant.CODE_SIGN_ERROR, "订单或快递单错误！");
		}
		if(!order.hasExpressType(OrderExtraConstant.EXPRESS_YUNDA)){
			return ErrorCode.getFailure(ApiConstant.CODE_SIGN_ERROR, "快订单类型错误！");
		}
		Map<String, String> params = new HashMap<String, String>();
		params.put("partnerid", partnerid);
		params.put("version", VERSION);
		params.put("request", "data");
		String xmldata = surround("orderid", order.getTradeno());
		xmldata += surround("mailno", order.getExpressnote());
		xmldata += surround("customerid", partnerid);
		xmldata += senderXml(gewaConfig);	//发件人信息
		OrderAddress orderAddress = daoService.getObject(OrderAddress.class, order.getTradeno());
		xmldata += receiverXml(orderAddress);		//收件人信息
		try {
			xmldata = surround("order", xmldata);
			xmldata = surround("orders", xmldata);
			String base64XmlData = Base64.encodeBase64String(xmldata.getBytes("UTF-8"));
			params.put("xmldata", base64XmlData);
			params.put("validation", StringUtil.md5(base64XmlData + partnerid + password));
		} catch (Exception e) {
			dbLogger.warn("", e);
			return ErrorCode.getFailure(ApiConstant.CODE_SIGN_ERROR, "数据base64编码错误！");
		}
		HttpResult result = HttpUtils.postUrlAsString(yunDaApiUrl + INTERFACE, params, TIME_OUT);
		if(!result.isSuccess()){
			return ErrorCode.getFailure(ApiConstant.CODE_SIGN_ERROR, result.getMsg());
		}
		return ErrorCode.SUCCESS;
	}
	private String senderXml(GewaConfig gewaConfig){
		Map<String, String> jsonSendMap = JsonUtils.readJsonToMap(gewaConfig.getContent());
		String xmlSend = surround("name", jsonSendMap.get("name"));
		xmlSend += surround("company", jsonSendMap.get("company"));
		xmlSend += surround("city", jsonSendMap.get("city"));
		xmlSend += surround("address", jsonSendMap.get("address"));
		xmlSend += surround("postcode", jsonSendMap.get("postcode"));
		xmlSend += surround("phone", jsonSendMap.get("phone"));
		xmlSend += surround("mobile", jsonSendMap.get("mobile"));
		return surround("sender", xmlSend);
	}
	
	private String receiverXml(OrderAddress orderAddress){
		String xmlReceiver = surround("name", orderAddress.getRealname());
		xmlReceiver += surround("city", orderAddress.getCityname() + orderAddress.getCountyname());
		xmlReceiver += surround("address", orderAddress.getAddress());
		if(StringUtils.isNotBlank(orderAddress.getPostalcode())){
			xmlReceiver += surround("postcode", orderAddress.getPostalcode());
		}
		xmlReceiver += surround("mobile", orderAddress.getMobile());
		return surround("receiver", xmlReceiver);
	}
	
	public ErrorCode updateExpressStatus(OrderExtra order, String status){
		if(order == null){
			return ErrorCode.getFailure(ApiConstant.CODE_SIGN_ERROR, "订单错误,不能为空！");
		}
		Map<String, String> params = new HashMap<String, String>();
		params.put("partnerid", partnerid);
		params.put("version", VERSION);
		params.put("request", "info");
		String xmldata = surround("orderid", order.getTradeno());
		xmldata += surround("callback", "D_00001");
		xmldata += surround("command", "status");
		xmldata += surround("parameter", status);
		xmldata = surround("order", xmldata);
		xmldata = surround("orders", xmldata);
		try {
			String base64XmlData = Base64.encodeBase64String(xmldata.getBytes("UTF-8"));
			params.put("xmldata", base64XmlData);
			params.put("validation", StringUtil.md5(base64XmlData + partnerid + password));
		} catch (Exception e) {
			dbLogger.warn("", e);
			return ErrorCode.getFailure(ApiConstant.CODE_SIGN_ERROR, "数据base64编码错误！");
		}
		HttpResult result = HttpUtils.postUrlAsString(yunDaApiUrl + INTERFACE, params, TIME_OUT);
		if(!result.isSuccess()){
			return ErrorCode.getFailure(ApiConstant.CODE_SIGN_ERROR, result.getMsg());
		}
		return ErrorCode.SUCCESS;
	}
}
