package com.gewara.untrans.terminal.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.gewara.constant.sys.HttpTimeout;
import com.gewara.model.pay.GewaOrder;
import com.gewara.support.ErrorCode;
import com.gewara.untrans.AbstractSynchBaseService;
import com.gewara.untrans.terminal.TerminalService;
import com.gewara.util.HttpResult;
import com.gewara.xmlbind.terminal.TakeInfo;
import com.gewara.xmlbind.terminal.TakeInfoList;
@Service("terminalService")
public class TerminalServiceImpl extends AbstractSynchBaseService implements TerminalService, InitializingBean{
	private String terminalPath;
	@Value("${terminal.takeInfoListUrl}")
	private String takeInfoListUrl;
	@Override
	public ErrorCode<List<TakeInfo>> getTakeInfoList(String tradenos) {
		String url = getUrl(takeInfoListUrl);
		Map<String, String> params = new HashMap<String, String>();
		params.put("tradenos", tradenos);
		HttpResult result = getRequestResult(url, params, HttpTimeout.TERMINAL_REQUEST);
		return getObjectList(TakeInfoList.class, result);
	}
	@Override
	public TakeInfo getTakeInfo(GewaOrder order) {
		ErrorCode<List<TakeInfo>> code = getTakeInfoList(order.getTradeNo());
		if(code.isSuccess()){
			return code.getRetval().get(0);
		}
		return null;
	}
	@Override
	public TakeInfo getTakeInfoByTradeno(String tradeno) {
		ErrorCode<List<TakeInfo>> code = getTakeInfoList(tradeno);
		if(code.isSuccess()){
			return code.getRetval().get(0);
		}
		return null;
	}
	private String getUrl(String url){
		return terminalPath + url;
	}
	@Override
	public void afterPropertiesSet() throws Exception {
		terminalPath = config.getString("terminalPath");
	}

}
