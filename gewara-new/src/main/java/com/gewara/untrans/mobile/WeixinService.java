package com.gewara.untrans.mobile;

import java.util.Map;

import com.gewara.support.ErrorCode;

public interface WeixinService {

	boolean verify(String signature, String timestamp, String nonce);

	ErrorCode<Map<String, String>> getAccessToken();

	ErrorCode<String> validReq(String tms, String wxs);

	String getWxs(String tms);

	String getWxToUser();

}
