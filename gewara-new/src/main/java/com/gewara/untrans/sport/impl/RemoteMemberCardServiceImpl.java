package com.gewara.untrans.sport.impl;

import java.sql.Timestamp;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.gewara.config.SportAPIConfig;
import com.gewara.constant.ApiConstant;
import com.gewara.constant.MemberCardConstant;
import com.gewara.constant.sys.HttpTimeout;
import com.gewara.model.pay.MemberCardOrder;
import com.gewara.model.pay.SportOrder;
import com.gewara.model.sport.MemberCardInfo;
import com.gewara.model.sport.MemberCardType;
import com.gewara.support.ErrorCode;
import com.gewara.untrans.AbstractSynchBaseService;
import com.gewara.untrans.sport.RemoteMemberCardService;
import com.gewara.util.ApiUtils;
import com.gewara.util.DateUtil;
import com.gewara.util.HttpResult;
import com.gewara.util.JsonUtils;
import com.gewara.xmlbind.DataWrapper;
import com.gewara.xmlbind.sport.RemoteCardPayOrder;
import com.gewara.xmlbind.sport.RemoteMemberCardInfo;
import com.gewara.xmlbind.sport.RemoteMemberCardInfoList;
import com.gewara.xmlbind.sport.RemoteMemberCardOrder;
import com.gewara.xmlbind.sport.RemoteMemberCardOrderList;
import com.gewara.xmlbind.sport.RemoteMemberCardType;
import com.gewara.xmlbind.sport.RemoteMemberCardTypeList;
@Service("remoteMemberCardService")
public class RemoteMemberCardServiceImpl extends AbstractSynchBaseService implements RemoteMemberCardService{
	@Value("${openApi.sportAppkey}")
	private String openApiSportAppkey = null;
	@Value("${openApi.sportSecretCode}")
	private String openApiSportSecretCode = null;

	@Autowired@Qualifier("sportAPIConfig")
	private SportAPIConfig sportAPIConfig;
	private Map<String, String> getCommonMap(Map<String, Object> map){
		Map<String, String> params = new HashMap<String, String>();
		params.put("partner", sportAPIConfig.getMerId());
		for(String key : map.keySet()){
			Object value = map.get(key);
			String strV = "";
			if(value instanceof Number){
				strV = String.valueOf(value);
			}else if(value instanceof Timestamp){
				strV = DateUtil.formatTimestamp((Timestamp)value);
			}else if(value instanceof Date){
				strV = DateUtil.formatDate((Date)value);
			}else {
				strV = String.valueOf(value);
			}
			params.put(key, strV);
		}
		return params;
	}
	@Override
	public ErrorCode<List<RemoteMemberCardType>> getRemoteMemberCardTypeListBySportid(String sportids) {
		String url = sportAPIConfig.getMemberCardtypeListUrl();
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("sportIDs", sportids);
		HttpResult result = postSportHttpRequestXML(url, getCommonMap(params), HttpTimeout.SHORT_REQUEST);
		return getObjectList(RemoteMemberCardTypeList.class, result);
	}
	@Override
	public ErrorCode<RemoteMemberCardType> getRemoteMemberCardTypeByKey(String cardtypeKey) {
		String url = sportAPIConfig.getMemberCardtypeUrl();
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("cardTypeUkey", cardtypeKey);
		HttpResult result = postSportHttpRequestXML(url, getCommonMap(params), HttpTimeout.SHORT_REQUEST);
		ErrorCode<DataWrapper> code  = getDataWrapper(result);
		if(!code.isSuccess()){
			return ErrorCode.getFailure(code.getMsg());
		}
		return getRemoteMemberCardType(result);
	}
	@Override
	public ErrorCode<List<RemoteMemberCardInfo>> getRemoteMemberCardInfoListByCheckpass(String mobile, String checkpass) {
		String url = sportAPIConfig.getQryMemberCardListUrl();
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("mobile", mobile);
		params.put("checkpass", checkpass);
		HttpResult result = postSportHttpRequestXML(url, getCommonMap(params), HttpTimeout.SHORT_REQUEST);
		return getObjectList(RemoteMemberCardInfoList.class, result);
	}
	private ErrorCode<DataWrapper> getDataWrapper(HttpResult result){
		if(result == null || !result.isSuccess()) return ErrorCode.getFailure(ApiConstant.CODE_SIGN_ERROR, "网络异常！");
		DataWrapper wrapper = (DataWrapper) ApiUtils.xml2Object(ApiUtils.getBeanReader("data", DataWrapper.class), result.getResponse());
		if(wrapper==null) return ErrorCode.getFailure("解析错误！");
		if(!wrapper.isSuccess()) return ErrorCode.getFailure(wrapper.getError());
		return ErrorCode.getSuccessReturn(wrapper);
	}
	private ErrorCode<RemoteMemberCardType> getRemoteMemberCardType(HttpResult result){
		ErrorCode<DataWrapper> code  = getDataWrapper(result);
		if(!code.isSuccess()){
			return ErrorCode.getFailure(code.getMsg());
		}
		return ErrorCode.getSuccessReturn(code.getRetval().getMemberCardType());
	}
	private ErrorCode<RemoteMemberCardOrder> getRemoteMemberCardOrder(HttpResult result){
		ErrorCode<DataWrapper> code  = getDataWrapper(result);
		if(!code.isSuccess()){
			return ErrorCode.getFailure(code.getMsg());
		}
		return ErrorCode.getSuccessReturn(code.getRetval().getSportMemberCardOrder());
	}
	private ErrorCode<RemoteMemberCardInfo> getRemoteMemberCardInfo(HttpResult result){
		ErrorCode<DataWrapper> code  = getDataWrapper(result);
		if(!code.isSuccess()){
			return ErrorCode.getFailure(code.getMsg());
		}
		return ErrorCode.getSuccessReturn(code.getRetval().getMemberInfo());
	}
	private ErrorCode<RemoteCardPayOrder> getRemoteCardPayOrder(HttpResult result){
		ErrorCode<DataWrapper> code  = getDataWrapper(result);
		if(!code.isSuccess()){
			return ErrorCode.getFailure(code.getMsg());
		}
		return ErrorCode.getSuccessReturn(code.getRetval().getCardPayOrder());
	}
	@Override
	public ErrorCode<RemoteMemberCardOrder> createRemoteMemberCardOrder(MemberCardOrder order, MemberCardType mct) {
		String url = sportAPIConfig.getCreateMemberCardOrderUrl();
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("mobile", order.getMobile());
		params.put("ptTradeNo", order.getTradeNo());
		params.put("userUkey", order.getUkey());
		params.put("cardTypeUkey", mct.getCardTypeUkey());
		params.put("memberCardNum", order.getQuantity());
		params.put("sumMoney", order.getTotalfee());
		params.put("settleAccountsSumMoney", order.getCostprice());
		HttpResult result = postSportHttpRequestXML(url, getCommonMap(params), HttpTimeout.NORMAL_REQUEST);
		return getRemoteMemberCardOrder(result);
	}
	
	@Override
	public ErrorCode<RemoteMemberCardOrder> commitRemoteMemberCardOrder(MemberCardOrder order) {
		String url = sportAPIConfig.getCommitMemberCardOrderUrl();
		String tradeNo = JsonUtils.getJsonValueByKey(order.getOtherinfo(), MemberCardConstant.CUS_TRADENO);
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("tradeNo", tradeNo);
		HttpResult result = postSportHttpRequestXML(url, getCommonMap(params), HttpTimeout.NORMAL_REQUEST);
		return getRemoteMemberCardOrder(result);
	}
	
	@Override
	public ErrorCode<String> getMobileCheckpass(String mobile, String type) {
		String url = sportAPIConfig.getMobileCheckpassUrl();
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("mobile", mobile);
		params.put("type", type);
		HttpResult result = postSportHttpRequestXML(url, getCommonMap(params), HttpTimeout.SHORT_REQUEST);
		return getRemoteResult(result);
	}
	
	@Override
	public ErrorCode<RemoteMemberCardOrder> getRemoteMemberCardOrderByTradeno(MemberCardOrder order) {
		String url = sportAPIConfig.getMemberCardOrderByTradeNoUrl();
		String tradeNo = JsonUtils.getJsonValueByKey(order.getOtherinfo(), MemberCardConstant.CUS_TRADENO);
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("tradeNo", tradeNo);
		HttpResult result = postSportHttpRequestXML(url, getCommonMap(params), HttpTimeout.SHORT_REQUEST);
		ErrorCode<List<RemoteMemberCardOrder>> code = getObjectList(RemoteMemberCardOrderList.class, result);
		if(!code.isSuccess()){
			return ErrorCode.getFailure(code.getMsg());
		}
		List<RemoteMemberCardOrder> orderList = code.getRetval();
		if(orderList.size()==0){
			return ErrorCode.getFailure("没有查询到记录");
		}
		return ErrorCode.getSuccessReturn(orderList.get(0));
	}
	
	@Override
	public ErrorCode<RemoteMemberCardInfo> getMemberCardInfo(MemberCardInfo card) {
		String url = sportAPIConfig.getMemberCardInfoUrl();
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("memberCardCode", card.getMemberCardCode());
		HttpResult result = postSportHttpRequestXML(url, getCommonMap(params), HttpTimeout.SHORT_REQUEST);
		ErrorCode<List<RemoteMemberCardInfo>> lcode = getObjectList(RemoteMemberCardInfoList.class, result);
		if(!lcode.isSuccess()){
			return ErrorCode.getFailure(lcode.getMsg());
		}
		List<RemoteMemberCardInfo> cardList = lcode.getRetval();
		if(cardList.size()==0){
			return ErrorCode.getFailure("没有查询到卡的信息！");
		}
		return ErrorCode.getSuccessReturn(cardList.get(0));
	}
	
	//会员卡支付
	@Override
	public ErrorCode<RemoteMemberCardInfo> cardPay(SportOrder order, MemberCardType mct, MemberCardInfo mci, String checkpass) {
		String url = sportAPIConfig.getCardPayUrl();
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("ptTradeNo", order.getTradeNo());
		params.put("mobile", mci.getMobile());
		params.put("checkpass", checkpass);
		params.put("cardCode", mci.getMemberCardCode());
		if(mct.hasNumCard()){
			params.put("amount", order.getQuantity());
			params.put("payAmount", order.getQuantity());
		}else if(mct.hasAmountCard()){
			int due = order.getDue();
			params.put("amount", due);
			if(mct.getDiscount()!=null){
				due = Math.round(due*mct.getDiscount()/100f);
			}
			params.put("payAmount", due);
		}
		HttpResult result = postSportHttpRequestXML(url, getCommonMap(params), HttpTimeout.NORMAL_REQUEST);
		return getRemoteMemberCardInfo(result);
	}	
	//会员卡消费查询
	@Override
	public ErrorCode<RemoteCardPayOrder> getRemoteCardPayOrder(MemberCardOrder order) {
		String url = sportAPIConfig.getCardPayResultUrl();
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("ptTradeNo", order.getTradeNo());
		HttpResult result = postSportHttpRequestXML(url, getCommonMap(params), HttpTimeout.NORMAL_REQUEST);
		return getRemoteCardPayOrder(result);
	}
	/**
	 * 运动商家远程联接返回
	 * */
	private HttpResult postSportHttpRequestXML(String method, Map<String, String> params, int timeout){
		return postOpenApiRequest(method, "xml", params, openApiSportAppkey, openApiSportSecretCode, timeout);
	}

}
