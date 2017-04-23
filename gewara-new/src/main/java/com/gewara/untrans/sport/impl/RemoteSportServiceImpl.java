package com.gewara.untrans.sport.impl;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.gewara.config.SportAPIConfig;
import com.gewara.constant.MemberCardConstant;
import com.gewara.constant.sys.HttpTimeout;
import com.gewara.model.pay.SportOrder;
import com.gewara.model.sport.OpenTimeTable;
import com.gewara.support.ErrorCode;
import com.gewara.untrans.AbstractSynchBaseService;
import com.gewara.untrans.sport.RemoteSportService;
import com.gewara.util.BeanUtil;
import com.gewara.util.DateUtil;
import com.gewara.util.HttpResult;
import com.gewara.util.JsonUtils;
import com.gewara.util.StringUtil;
import com.gewara.xmlbind.BaseObjectListResponse;
import com.gewara.xmlbind.sport.GstOtt;
import com.gewara.xmlbind.sport.GstOttList;
import com.gewara.xmlbind.sport.GstSportField;
import com.gewara.xmlbind.sport.GstSportFieldList;

@Service("remoteSportService")
public class RemoteSportServiceImpl extends AbstractSynchBaseService implements RemoteSportService {
	@Autowired@Qualifier("sportAPIConfig")
	private SportAPIConfig sportAPIConfig;

	@Value("${openApi.sportAppkey}")
	private String openApiSportAppkey = null;
	@Value("${openApi.sportSecretCode}")
	private String openApiSportSecretCode = null;

	private static String getCheck(Object...objects){
		String str = StringUtils.join(objects, "") + DateUtil.formatDate(new Date());
		return StringUtil.md5(str);
	}

	@Override
	public ErrorCode<List<GstOtt>> getGstOttList(Long sportid, Long itemid, Date playdate, String ge){
		String url = sportAPIConfig.getOttListUrl();
		Map<String, String> params = new HashMap<String, String>();
		params.put("sportid", sportid+"");
		if(playdate != null) params.put("playdate", DateUtil.formatDate(playdate));
		if(itemid != null) params.put("itemid", itemid+"");
		if(ge != null) params.put("ge", ge);
		return getObjectListOpenApi(GstOttList.class, url, params, HttpTimeout.NORMAL_REQUEST);
	}

	@Override
	public ErrorCode<List<GstSportField>> getGstSportFieldList(Long sportid, Long itemid){
		String url = sportAPIConfig.getFieldListUrl();
		Map<String, String> params = new HashMap<String, String>();
		params.put("sportid", sportid+"");
		if(itemid != null) params.put("itemid", itemid+"");
		params.put("checkvalue", getCheck(sportid));
		return getObjectListOpenApi(GstSportFieldList.class, url, params, HttpTimeout.NORMAL_REQUEST);
	}

	@Override
	public ErrorCode<String> getGstItemIdList(Long sportid) {
		String url = sportAPIConfig.getItemListUrl();
		Map<String, String> params = new HashMap<String, String>();
		params.put("sportid", sportid+"");
		params.put("checkvalue", getCheck(sportid));
		return getRemoteResultOpenApi(url, params, HttpTimeout.NORMAL_REQUEST);
	}
	
	@Override
	public ErrorCode<String> lockOrder(OpenTimeTable ott, List<Long> remoteIdList, String type){
		String url = sportAPIConfig.getToLockOtiUrl();
		Map<String,String> params = new HashMap<String, String>();
		params.put("sportid", String.valueOf(ott.getSportid()));
		params.put("itemid", String.valueOf(ott.getItemid()));
		params.put("ottid", String.valueOf(ott.getRemoteid()));
		params.put("otiids", StringUtils.join(remoteIdList, ","));
		if(StringUtils.isNotBlank(type)){
			params.put("type", type);
		}
		return getRemoteResultOpenApi(url, params, HttpTimeout.LONG_REQUEST);
	}
	
	@Override
	public ErrorCode<String> refundOrder(SportOrder order){
		String url = sportAPIConfig.getRefundUrl();
		Map<String,String> params = new HashMap<String, String>();
		params.put("sportid", String.valueOf(order.getSportid()));
		params.put("itemid", String.valueOf(order.getItemid()));
		params.put("tradeno", String.valueOf(order.getTradeNo()));
		return getRemoteResultOpenApi(url, params, HttpTimeout.LONG_REQUEST);
	}
	
	@Override
	public ErrorCode<String> unLockOrder(OpenTimeTable ott, List<Long> remoteIdList){
		String url = sportAPIConfig.getUnLockOtiUrl();
		Map<String,String> params = new HashMap<String, String>();
		params.put("sportid", String.valueOf(ott.getSportid()));
		params.put("itemid", String.valueOf(ott.getItemid()));
		params.put("ottid", String.valueOf(ott.getRemoteid()));
		params.put("otiids", StringUtils.join(remoteIdList, ","));
		return getRemoteResultOpenApi(url, params, HttpTimeout.LONG_REQUEST);
	}
	
	@Override
	public ErrorCode<List<Long>> getRemoteLockItem(OpenTimeTable ott){
		String url = sportAPIConfig.getLockOtiListUrl();
		Map<String, String> params = new HashMap<String, String>();
		params.put("ottid", ott.getRemoteid()+"");
		params.put("checkvalue", getCheck(ott.getRemoteid()+""));
		ErrorCode<String> code = getRemoteResultOpenApi(url, params, HttpTimeout.NORMAL_REQUEST);
		if(!code.isSuccess()) return ErrorCode.getFailure(code.getErrcode(), code.getMsg());
		return ErrorCode.getSuccessReturn(BeanUtil.getIdList(code.getRetval(), ","));
	}
	@Override
	public ErrorCode<String> fixOrder(SportOrder order, OpenTimeTable ott, List<Long> remoteIdList, String vipCard, String cname) {
		String url = sportAPIConfig.getFixOrderUrl();
		Map<String, String> params = new HashMap<String, String>();
		params.put("tradeno", order.getTradeNo());
		params.put("sportid", String.valueOf(order.getSportid()));
		params.put("itemid", String.valueOf(order.getItemid()));
		params.put("ottid", String.valueOf(ott.getRemoteid()));
		params.put("otiids", StringUtils.join(remoteIdList, ","));
		params.put("mobile", StringUtils.substring(order.getMobile(), order.getMobile().length()-4));
		params.put("sumcost", String.valueOf(order.getTotalcost()));
		params.put("totalfee", String.valueOf(order.getTotalfee()));
		params.put("quantity", String.valueOf(order.getQuantity()));
		params.put("vipcard", JsonUtils.getJsonValueByKey(order.getDescription2(), MemberCardConstant.VIPCARD));
		params.put("checkpass", StringUtil.md5(order.getCheckpass()));
		params.put("description", order.getDescription2());
		params.put("addtime", DateUtil.formatTimestamp(order.getAddtime()));
		if(StringUtils.isNotBlank(vipCard))params.put("vipcard", vipCard);
		if(StringUtils.isNotBlank(cname))params.put("cname", cname);
		
		return  getRemoteResultOpenApi(url, params, HttpTimeout.LONG_REQUEST);
	}
	@Override
	public ErrorCode checkOrder(SportOrder order){
		String url = sportAPIConfig.getQueryOrderUrl();
		Map<String, String> params = new HashMap<String, String>();
		params.put("tradeno", order.getTradeNo());
		return  getRemoteResultOpenApi(url, params, HttpTimeout.NORMAL_REQUEST);
	}
	private <S, T extends BaseObjectListResponse<S>> ErrorCode<List<S>> getObjectListOpenApi(Class<T> clazz, String method, Map<String, String> params, int timeout){
		HttpResult code = postSportHttpRequestXML(method, params, timeout);
		return getObjectList(clazz, code);
	}
	/**
	 * 获取远程数据结果是否成功
	 * @param url				远程连接
	 * @param params			参数
	 * @param timeount			超时时间
	 * @return
	 */
	private ErrorCode<String> getRemoteResultOpenApi(String url, Map<String,String> params, int timeout){
		HttpResult code = postSportHttpRequestXML(url, params, timeout);
		return getRemoteResult(code);
	}
	/**
	 * 运动商家远程联接返回
	 * */
	private HttpResult postSportHttpRequestXML(String method, Map<String, String> params, int timeout){
		return postOpenApiRequest(method, "xml", params, openApiSportAppkey, openApiSportSecretCode, timeout);
	}


}
