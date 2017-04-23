package com.gewara.web.action.api2partner;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.ui.ModelMap;

import com.gewara.constant.sys.MongoData;
import com.gewara.json.mobile.WeixinActivity;
import com.gewara.untrans.CommonService;
import com.gewara.web.action.api.BaseApiController;

public class BaseWeixinController extends BaseApiController{
	@Autowired@Qualifier("commonService")
	protected CommonService commonService;
	protected void getInitContent(ModelMap model){
		Map map = mongoService.findOne(MongoData.NS_WEIXIN, "id", WeixinActivity.TEMPLATE_ID);
		if(map!=null)model.put("content", map.get("content"));
	}
	protected String getXml(HttpServletRequest request){
		String encode = "utf-8" ;
		BufferedReader in = null;
		try {
			in = new BufferedReader(new InputStreamReader(request.getInputStream(), encode));
			String result = "";
			String line;
			while ((line = in.readLine()) != null) {
				result += line;
			}
			in.close();
			return result;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return "";
	}
}
