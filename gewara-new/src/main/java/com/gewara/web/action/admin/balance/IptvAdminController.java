package com.gewara.web.action.admin.balance;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.gewara.util.HttpResult;
import com.gewara.util.HttpUtils;
import com.gewara.web.action.admin.BaseAdminController;

@Controller
public class IptvAdminController extends BaseAdminController{
	@RequestMapping(value="/admin/balance/iptv/orderList.xhtml", method=RequestMethod.GET)
	public String iptvOrderList(){
		return "admin/balance/iptv/orderList.vm";
	}

	@RequestMapping(value="/admin/balance/iptv/orderList.xhtml", method=RequestMethod.POST)
	public String iptvOrderList(String tradeno, String userid, String mobile, String starttime, String endtime, String url, ModelMap model){
		if(StringUtils.isBlank(url)){
			url = "http://222.68.195.29:8080/gewala/qryOrder";
		}
		
		Map<String, String> params = new HashMap<String, String>();
		params.put("tradeno", tradeno);
		params.put("userid", userid);
		params.put("mobile", mobile);
		params.put("starttime", starttime);
		params.put("endtime", endtime);
		String result = "";
		HttpResult httpRes = HttpUtils.postUrlAsString(url, params);
		if(httpRes.isSuccess()){
			result = httpRes.getResponse();
		}else {
			result =  httpRes.getMsg();
		}
		model.put("result", result);
		return "admin/balance/iptv/orderList.vm";
	}
}
