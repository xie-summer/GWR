package com.gewara.untrans;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import com.gewara.Config;
import com.gewara.commons.sign.Sign;
import com.gewara.constant.ApiConstant;
import com.gewara.support.ErrorCode;
import com.gewara.untrans.monitor.MonitorService;
import com.gewara.util.ApiUtils;
import com.gewara.util.DateUtil;
import com.gewara.util.GewaLogger;
import com.gewara.util.HttpResult;
import com.gewara.util.HttpUtils;
import com.gewara.util.LoggerUtils;
import com.gewara.xmlbind.BaseInnerResponse;
import com.gewara.xmlbind.BaseObjectListResponse;
import com.gewara.xmlbind.BaseObjectResponse;

public abstract class AbstractSynchBaseService {
	protected final transient GewaLogger dbLogger = LoggerUtils.getLogger(getClass(), Config.getServerIp(), Config.SYSTEMID);
	@Autowired@Qualifier("config")
	protected Config config;
	@Autowired@Qualifier("monitorService")
	protected MonitorService monitorService;
	/**
	 * 获取远程访问数据
	 * @param url				远程连接
	 * @param params			参数
	 * @param timeount			超时时间
	 * @return
	 */
	
	protected HttpResult getRequestResult(String url, Map<String,String> params, int timeount){
		long cur = System.currentTimeMillis();
		HttpResult result = null;
		int length = getParamLength(params);
		if(length<1000) result = HttpUtils.getUrlAsString(url, params, timeount);
		else result = HttpUtils.postUrlAsString(url, params, timeount);
		Map<String, String> paramsLog = new HashMap<String, String>();
		paramsLog.put("uri", url);
		if(params != null) paramsLog.putAll(params);
		monitorService.addApiCall(paramsLog, cur, result.isSuccess());
		return result;
	}
	
	private int getParamLength(Map<String, String> params){
		int i=0;
		if(params != null){
			for(String value:params.values()) i+= StringUtils.length(value);
		}
		return i;
	}
	
	/**
	 * 获取远程xml数据转成绑下的对象
	 * @param clazz				xml转换的对象
	 * @param result			远程访问数据
	 * @return
	 */
	protected <T extends BaseInnerResponse> ErrorCode<T> getObject(Class<T> clazz, HttpResult result){
		try{
			if(result == null || !result.isSuccess()) return ErrorCode.getFailure(ApiConstant.CODE_SIGN_ERROR, "网络异常！");
			T response = (T) ApiUtils.xml2Object(ApiUtils.getBeanReader("data", clazz), result.getResponse());
			if(response==null) return ErrorCode.getFailure("解析错误！");
			if(!response.isSuccess()) {
				return ErrorCode.getFailure(response.getCode(), response.getError())
						.setSyscode(response.getSyscode()).setSysmsg(response.getSysmsg());
			}
			return ErrorCode.getSuccessReturn(response);
		}catch (Exception e) {
			dbLogger.error("xml数据格式错误：", e);
			return ErrorCode.getFailure(ApiConstant.CODE_SIGN_ERROR, "数据错误！");
		}
	}
	
	/**
	 * 获取远程xml数据转成绑下的对象
	 * @param clazz				xml转换的对象
	 * @param url				远程连接
	 * @param params			参数
	 * @param timeount			超时时间
	 * @return
	 */
	protected <T extends BaseInnerResponse> ErrorCode<T> getObject(Class<T> clazz, String url, Map<String, String> params, int timeout){
		ErrorCode<HttpResult> code = getUrlAsString(url, params, timeout);
		if(!code.isSuccess()) return ErrorCode.getFailure(code.getErrcode(), code.getMsg())
				.setSyscode(code.getSyscode()).setSysmsg(code.getSysmsg());
		if(Config.isDebugEnabled()){
			dbLogger.warn("send:" + params + ", receive:" + code.getRetval());
		}
		return getObject(clazz, code.getRetval());
	}
	
	/**
	 * 获取远程xml数据转成绑下的对象中generics集合
	 * @param clazz				xml转换的对象
	 * @param result			远程访问数据
	 * @return
	 */
	protected <S, T extends BaseObjectListResponse<S>> ErrorCode<List<S>> getObjectList(Class<T> clazz, HttpResult result){
		try{
			if(result == null || !result.isSuccess()) return ErrorCode.getFailure(ApiConstant.CODE_SIGN_ERROR, "网络异常！");
			T response = (T) ApiUtils.xml2Object(ApiUtils.getBeanReader("data", clazz), result.getResponse());
			if(response==null) return ErrorCode.getFailure("解析错误");
			if(!response.isSuccess()) { 
				dbLogger.warn("http 请求错误：" + clazz.getSimpleName() + ", code=" + response.getCode() + ", error=" + response.getError()  + ", response=" + result.getResponse());
				return ErrorCode.getFailure(response.getCode(), response.getError()).setSyscode(response.getSyscode()).setSysmsg(response.getSysmsg());
			}
			return ErrorCode.getSuccessReturn(response.getObjectList());
		}catch (Exception e) {
			dbLogger.error("xml数据格式错误：", e);
			return ErrorCode.getFailure(ApiConstant.CODE_SIGN_ERROR, "数据错误！");
		}
	}
	
	/**
	 * 获取远程xml数据转成绑下的对象中generics集合
	 * @param clazz				xml转换的对象
	 * @param url				远程连接
	 * @param params			参数
	 * @param timeount			超时时间
	 * @return
	 */
	protected <S, T extends BaseObjectListResponse<S>> ErrorCode<List<S>> getObjectList(Class<T> clazz, String url, Map<String, String> params, int timeout){
		ErrorCode<HttpResult> code = getUrlAsString(url, params, timeout);
		if(!code.isSuccess()) return ErrorCode.getFailure(code.getErrcode(), code.getMsg());
		return getObjectList(clazz, code.getRetval());
	}
	
	/**
	 * 获取远程数据数量
	 * @param result			远程访问数据
	 * @return
	 */
	protected ErrorCode<Integer> getRemoteCount(HttpResult result){
		try{
			if(result == null || !result.isSuccess()) return ErrorCode.getFailure(ApiConstant.CODE_SIGN_ERROR, "网络异常！");
			ErrorCode<BaseObjectResponse> code = getObject(BaseObjectResponse.class, result);
			if(!code.isSuccess()) { 
				dbLogger.warn("http 请求错误："+", response=" + result.getResponse());
				return ErrorCode.getFailure(code.getErrcode(), code.getMsg());
			}
			return ErrorCode.getSuccessReturn(code.getRetval().toIntValue());
		}catch (Exception e) {
			dbLogger.error("xml数据格式错误：", e);
			return ErrorCode.getFailure(ApiConstant.CODE_SIGN_ERROR, "数据错误！");
		}
	}
	
	/**
	 * 获取远程数据数量
	 * @param url				远程连接
	 * @param params			参数
	 * @param timeount			超时时间
	 * @return
	 */
	protected ErrorCode<Integer> getRemoteCount(String url, Map<String,String> params, int timeount){
		ErrorCode<HttpResult> code = getUrlAsString(url, params, timeount);
		if(!code.isSuccess()) return ErrorCode.getFailure(code.getErrcode(), code.getMsg());
		return getRemoteCount(code.getRetval());
	}

	/**
	 * 获取远程访问数据
	 * @param url				远程连接
	 * @param params			参数
	 * @param timeount			超时时间
	 * @return
	 */
	protected ErrorCode<HttpResult> getUrlAsString(String url, Map<String, String> params, int timeout){
		HttpResult result = getRequestResult(url, params, timeout);
		if(!result.isSuccess()) return ErrorCode.getFailure(ApiConstant.CODE_SIGN_ERROR, "网络异常！");
		return ErrorCode.getSuccessReturn(result);
	}
	
	protected ErrorCode<String> getRemoteResult(String url, Map<String,String> params, int timeount){
		ErrorCode<HttpResult> code = getUrlAsString(url, params, timeount);
		if(!code.isSuccess()) return ErrorCode.getFailure(code.getErrcode(), code.getMsg());
		return getRemoteResult(code.getRetval());
	}

	/**
	 * 获取远程数据结果是否成功
	 * @param result			远程访问数据
	 * @return
	 */
	protected ErrorCode<String> getRemoteResult(HttpResult result){
		try{
			if(result == null || !result.isSuccess()) return ErrorCode.getFailure(ApiConstant.CODE_SIGN_ERROR, "网络异常！");
			ErrorCode<BaseObjectResponse> code = getObject(BaseObjectResponse.class, result);
			if(!code.isSuccess()) { 
				dbLogger.warn("http 请求错误："+", response=" + result.getResponse());
				return ErrorCode.getFailure(code.getErrcode(), code.getMsg());
			}
			return ErrorCode.getSuccessReturn(code.getRetval().getResult());
		}catch (Exception e) {
			dbLogger.error("xml数据格式错误：", e);
			return ErrorCode.getFailure(ApiConstant.CODE_SIGN_ERROR, "数据错误！");
		}
	}
	protected HttpResult postOpenApiRequest(String method, String format, Map<String, String> params, String appkey, String secretCode, int timeout){
		String openApiUrl = config.getString("openApiUrl");
		params.put("method", method);
		params.put("appkey", appkey);
		params.put("timestamp", DateUtil.getCurFullTimestampStr());
		params.put("format", format);
		params.put("v", "1.0");
		params.put("signmethod", "MD5");
		String sign = Sign.signMD5(params, secretCode);
		params.put("sign", sign);
		long cur = System.currentTimeMillis();
		HttpResult hr = HttpUtils.postUrlAsString(openApiUrl, params, timeout);
		Map<String, String> paramsLog = new HashMap<String, String>();
		paramsLog.put("uri", openApiUrl);
		if(params != null) paramsLog.putAll(params);
		monitorService.addApiCall(paramsLog, cur, hr.isSuccess());
		return hr;
	}

}
