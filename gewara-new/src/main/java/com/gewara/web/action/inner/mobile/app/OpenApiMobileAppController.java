package com.gewara.web.action.inner.mobile.app;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import com.gewara.api.userdevice.UserDeviceService;
import com.gewara.api.vo.UserDevice;
import com.gewara.constant.AdminCityContant;
import com.gewara.constant.ApiConstant;
import com.gewara.constant.Status;
import com.gewara.constant.TagConstant;
import com.gewara.constant.app.PushConstant;
import com.gewara.constant.order.AddressConstant;
import com.gewara.constant.sys.ConfigConstant;
import com.gewara.constant.sys.MongoData;
import com.gewara.json.AppSourceCount;
import com.gewara.json.MobileApp;
import com.gewara.json.MobileUpGrade;
import com.gewara.json.mobile.MobileAdvertisingYouMi;
import com.gewara.model.bbs.CustomerQuestion;
import com.gewara.model.common.GewaConfig;
import com.gewara.service.bbs.CustomerQuestionService;
import com.gewara.util.HttpResult;
import com.gewara.util.HttpUtils;
import com.gewara.util.JsonUtils;
import com.gewara.util.StringUtil;
import com.gewara.web.action.api2.AppDriverUtils;
import com.gewara.web.action.inner.mobile.BaseOpenApiController;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

@Controller
public class OpenApiMobileAppController extends BaseOpenApiController{
	@Autowired@Qualifier("customerQuestionService")
	private CustomerQuestionService customerQuestionService;
	@Autowired@Qualifier("appDriverUtils")
	private AppDriverUtils appDriverUtils;
	
	@Autowired@Qualifier("userDeviceService")
	private UserDeviceService userDeviceService;
	/**
	 * 格瓦拉app中其他的app应用
	 */
	@RequestMapping("/openapi/mobile/app/otherAppList.xhtml")
	public String showOtherApplicationList(String apptype, String osType, ModelMap model, HttpServletRequest request) {
		String[] params = { "apptype", "osType" };
		if (validateParamsRequired(params, request)) {
			return getErrorXmlView(model, ApiConstant.CODE_PARAM_ERROR, "apptype or osType is null");
		}
		DBObject object = new BasicDBObject();
		object.put("ostype", osType);
		object.put("status", Status.Y);
		BasicDBList list = new BasicDBList();
		list.add(apptype);
		object.put("coverapp", new BasicDBObject("$in", list));
		List<MobileApp> mobileappList = mongoService.getObjectList(MobileApp.class, object, "sortFlag", true, 0, 100);
		model.put("appList", mobileappList);
		return getXmlView(model, "api/mobile/otherAppList.vm");
	}
	private boolean validateParamsRequired(String[] params, HttpServletRequest request) {
		if (params == null) {
			return false;
		}
		for (String str : params) {
			if (!StringUtils.isBlank(request.getParameter(str))) {
				return false;
			}
		}
		return true;
	}
	
	/**
	 * 软件升级
	 * 参数都必传，apptype,appsource除外
	 * tag:系统iphone/android
	 * apptype:应用类型（cinema,sport,bar），为了兼容低版本，后续改成必须传入
	 * appSource:应用来源，兼容低版本，后续改成必须传入
	 */
	@RequestMapping("/openapi/mobile/app/upGrade.xhtml")
	public String upgrade(String tag, String apptype, String appSource, ModelMap model) {
		apptype = StringUtils.isBlank(apptype) ? TagConstant.TAG_CINEMA : apptype;
		if (StringUtils.isBlank(appSource)) {
			if (StringUtils.equalsIgnoreCase(AddressConstant.ADDRESS_ANDROID,tag))
				appSource = "AS01";// 获取googemarket的更新路径
			else if(StringUtils.equalsIgnoreCase(AddressConstant.ADDRESS_IPHONE, tag))
				appSource = "AS02";// 获取appstore的更新路径
			
		}
		MobileUpGrade upgrade = nosqlService.getLastMobileUpGrade(tag, apptype, appSource);
		if (upgrade!=null){
			model.put("mobileUpGrade", upgrade);
		}
		return getXmlView(model, "inner/mobile/upGrade.vm");
	}
	/**
	 * app请求的api地址
	 */
	@RequestMapping("/openapi/mobile/app/getApiPath.xhtml")
	public String getApiPath(HttpServletResponse response) throws IOException{
		GewaConfig cfg = daoService.getObject(GewaConfig.class, ConfigConstant.CFG_MOBILE_APIPATH);
		PrintWriter writer = response.getWriter();
		writer.write(cfg.getContent());
		writer.flush();
		writer.close();
		return null;
	}
	
	/**
	 * 意见回馈信息
	 */
	@RequestMapping("/openapi/mobile/app/addComplain.xhtml")
	public String addComplain(ModelMap model, String citycode, String body, String email, String phonetype,String osVersion, String appVersion, String mobileType) {
		if(StringUtils.isNotBlank(appVersion)){
			body = body + " [appVersion:" + appVersion + "]";
		}
		if(StringUtils.isNotBlank(mobileType)){
			body = body + " [mobileType:" + mobileType + "]";
		}
		if(StringUtils.isNotBlank(osVersion)){
			body = body + " [osVersion:" + osVersion + "]";
		}
		CustomerQuestion question = customerQuestionService.addCustomerQuestion(citycode, null, email, CustomerQuestion.TAG_ADVISE, body, phonetype);
		if(question == null) return getErrorXmlView(model, ApiConstant.CODE_DATA_ERROR, "保存意见内容失败！");
		return getSuccessXmlView(model);
	}
	
	/**
	 * 保存IPhone/android手机客户端访问令牌
	 * devicetoken,tag 必须传入
	 * 其他参数暂时未非必须，为了兼容低客户端版本
	 * 
	 * tag:系统类型android/iphone
	 * deviceid:设备id（android，iphone设备唯一标示）
	 * apptype:应用类型
	 * devicetoken:主要针对android，用来做android的push消息
	 * 
	 */
	@RequestMapping("/openapi/mobile/app/addDeviceToken.xhtml")
	public String addDeviceToken(String tag,String devicetoken,String appVersion,String deviceid,String rights,String apptype,String pushstatus,ModelMap model) {
		if(!StringUtils.equals(config.getString("sendPushServerFlag"),PushConstant.SEND_PUSH_SERVER_FLAG)){
			return getSuccessXmlView(model);
		}
		if (StringUtils.isBlank(tag)) {
			return getErrorXmlView(model, ApiConstant.CODE_DATA_ERROR, "参数有误！");
		}
		tag = tag.toUpperCase();
		apptype = StringUtils.isBlank(apptype) ? TagConstant.TAG_CINEMA : apptype;
		if(StringUtils.isBlank(pushstatus) && StringUtils.isNotBlank(rights)){
			pushstatus = JsonUtils.getJsonValueByKey(rights, "pushstatus");
		}
		UserDevice ud = new UserDevice(null,deviceid,devicetoken,tag,apptype,appVersion,pushstatus);
		try {
			userDeviceService.saveUserDevice(ud);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return getSuccessXmlView(model);
	}
	
	/**
	 * 数据统计
	 * 
	 * 参数都必传，citycode，apptype除外
	 * @param key
	 * @param encryptCode
	 * @param appSource
	 *           应用来源
	 * @param osType
	 *           系统类型
	 * @param deviceId
	 *           设备ID
	 * @param apptype
	 *           应用类型 电影，运动，酒吧
	 * @param flag
	 * @param model
	 * @return
	 */
	@RequestMapping("/openapi/mobile/app/insOpenCount.xhtml")
	public String installOpen(HttpServletRequest request, String apptype, 
			String appSource, String osType, String deviceid,String deviceMAC, String citycode, ModelMap model) {
		if (StringUtils.isBlank(citycode)) citycode = AdminCityContant.CITYCODE_SH;
		if (StringUtils.isNotBlank(appSource) && StringUtils.isNotBlank(osType) && StringUtils.isNotBlank(deviceid)) {
			osType = osType.toUpperCase();
			if (StringUtils.isBlank(apptype)) apptype = TagConstant.TAG_CINEMA;// 兼容以前电影低版本
			logAppSource(request, citycode, null, AppSourceCount.TYPE_IO, apptype);
		}
		if(StringUtils.isNotBlank(deviceMAC)){
			try {
				Map params = new HashMap();
				params.put("apptype", apptype);
				params.put("deviceid", deviceMAC.toLowerCase());
				List<MobileAdvertisingYouMi> mobileAdvertisings = mongoService.getObjectList(MobileAdvertisingYouMi.class,params,"addTime", false, 0, 1);
				if(mobileAdvertisings != null && mobileAdvertisings.size() > 0){
					for(MobileAdvertisingYouMi mobileAdvertising : mobileAdvertisings){
						dbLogger.warn("手机ios广告推广：deviceMAC " + deviceMAC + "---appsource" + mobileAdvertising.getAppsource());
						if("AS47".equals(mobileAdvertising.getAppsource())){
							notifyYouMi(mobileAdvertising,deviceid);
						}else if("AS48".equals(mobileAdvertising.getAppsource())){
							notifyLiMei(mobileAdvertising,deviceid);
						}else if("AS80".equals(mobileAdvertising.getAppsource())){//爱普动力
							appDriverUtils.notifyAppDriver(mobileAdvertising, deviceid);
						}
					}
				}
				
			} catch (Exception e) {
				dbLogger.warn("手机ios广告推广：deviceMAC 回调出错" + deviceMAC + ":exception:" + e.getMessage());
			}
		}
		return getSuccessXmlView(model);
	}

	private void notifyYouMi(MobileAdvertisingYouMi aym,String deviceid){
		if(aym != null){
			dbLogger.warn("手机ios有米广告推广回调：deviceMAC " + aym.getDeviceid());
			String t = System.currentTimeMillis()/1000 + "";
			String sig = StringUtil.md5("439a68c84b672e20" + StringUtil.md5(aym.getDeviceid()) + t).substring(12, 20);
			StringBuilder url = new StringBuilder(aym.getUrl());
			url.append("&ts=0").append("&t=").append(t).append("&sig=").append(sig);
			HttpResult code = HttpUtils.getUrlAsString(url.toString());
			if(code.isSuccess()){
				aym.setYmRecord(code.getResponse());
				aym.setOpenUDID(deviceid);
				mongoService.saveOrUpdateObject(aym, MongoData.DEFAULT_ID_NAME);
			}
		}
	}
	private void notifyLiMei(MobileAdvertisingYouMi aym,String deviceid){
		if(aym != null){
			StringBuilder url = new StringBuilder("http://api.lmmob.com/capCallbackApi/1/?");
			url.append("appId=").append(aym.getApptype()).append("&udid=").append(aym.getDeviceid().toUpperCase()).append("&returnFormat=1");
			HttpResult code = HttpUtils.getUrlAsString(url.toString());
			if(code.isSuccess()){
				aym.setYmRecord(code.getResponse());
				aym.setOpenUDID(deviceid);
				mongoService.saveOrUpdateObject(aym, MongoData.DEFAULT_ID_NAME);
			}
		}
	}
}
