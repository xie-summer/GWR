package com.gewara.web.action.inner.mobile.activity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import com.gewara.constant.AdminCityContant;
import com.gewara.constant.ApiConstant;
import com.gewara.constant.sys.MongoData;
import com.gewara.json.PhoneActivity;
import com.gewara.model.content.PhoneAdvertisement;
import com.gewara.model.movie.Cinema;
import com.gewara.model.ticket.OpenPlayItem;
import com.gewara.web.action.inner.mobile.BaseOpenApiMobileController;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
@Controller
public class OpenApiMobilePhoneActivityController extends BaseOpenApiMobileController{
	/**
	 * 手机客户端活动列表
	 * @param apptype
	 * @param osType
	 * @param citycode
	 * @param from
	 * @param maxnum
	 * @param model
	 * @return
	 */
	@RequestMapping("/openapi/mobile/phoneActivity/phoneActivityList.xhtml")
	public String phoneActivityList(String apptype,String osType,String citycode,
			int from,int maxnum,ModelMap model, HttpServletRequest request){
		if(!(PhoneActivity.OS_TYPE_ANDROID.equals(osType) || PhoneActivity.OS_TYPE_IPHONE.equals(osType))){
			return getErrorXmlView(model, ApiConstant.CODE_SIGN_ERROR, "ostype 应为ANDROID或IPHONE");
		}
		if(StringUtils.isBlank(citycode)){
			citycode = AdminCityContant.CITYCODE_ALL;
		}
		DBObject params = new BasicDBObject();
		DBObject inparams = new BasicDBObject();
		inparams.put("$in", new String[]{osType, PhoneActivity.OS_TYPE_ALL});
		params.put("status", PhoneActivity.STATUS_NEW);
		params.put("apptype", apptype);
		params.put("ostype", inparams);
		Pattern pattern = Pattern.compile(citycode,Pattern.CASE_INSENSITIVE);
		params.put("citycode", pattern);
		if(maxnum > 20){
			maxnum = 20;
		}
		List<PhoneActivity> phoneActivityList = mongoService.getObjectList(PhoneActivity.class, params,"rank",true,from,maxnum);
		getPhoneActivityListMap(phoneActivityList, model, request);
		return getOpenApiXmlList(model);
	}
	/**
	 * 手机首页广告列表
	 */
	@RequestMapping("/openapi/mobile/phoneAdver/indexAdvertList.xhtml")
	public String phoneAdvertList(String apptype,String osType,String citycode, Integer from, Integer maxnum, ModelMap model){
		if(StringUtils.isBlank(apptype)){
			return getErrorXmlView(model, ApiConstant.CODE_SIGN_ERROR,"应用类型不能为空");
		}
		if(StringUtils.isBlank(citycode)){
			return getErrorXmlView(model, ApiConstant.CODE_SIGN_ERROR,"citycode不能为空");
		}
		if(!(PhoneAdvertisement.OS_TYPE_ANDROID.equals(osType) || PhoneAdvertisement.OS_TYPE_IPHONE.equals(osType))){
			return getErrorXmlView(model, ApiConstant.CODE_SIGN_ERROR,"osType只能为ANDROID或IPHONE");
		}
		if("cinema".equals(apptype)){
			apptype = "ciname";
		}
		if(from==null) from = 0;
		if(maxnum==null) maxnum = 10;
		List<PhoneAdvertisement> advertList = apiMobileService.getPhoneAdvertList(apptype, osType,citycode,null, from, maxnum);
		model.put("advertList", advertList);
		return getXmlView(model, "api/mobile/phoneAdvertList.vm");
	}
	/**
	 * 手机活动详细
	 */
	@RequestMapping("/openapi/mobile/phoneActivity/phoneActivityDetail.xhtml")
	public String phoneActivityDetail(String activityid, ModelMap model){
		if(activityid == null){
			return getErrorXmlView(model, ApiConstant.CODE_SIGN_ERROR, "activityid不能为空！");
		}
		PhoneActivity activity = mongoService.getObject(PhoneActivity.class, MongoData.DEFAULT_ID_NAME, activityid);
		if(activity == null){
			return getErrorXmlView(model, ApiConstant.CODE_SIGN_ERROR, "相关活动未找到！");
		}
		model.put("activity", activity);
		List<OpenPlayItem> opiList = new ArrayList<OpenPlayItem>();
		String[] mpids = StringUtils.split(activity.getOpiinfo(),",");
		Map<Long,Cinema> cinemas = new HashMap<Long,Cinema>();
		if(mpids != null){
			for(String mpid:mpids){
				OpenPlayItem opi = daoService.getObjectByUkey(OpenPlayItem.class, "mpid", Long.parseLong(mpid.trim()), true);
				if(opi!=null){
					opiList.add(opi);
					cinemas.put(opi.getCinemaid(), this.daoService.getObject(Cinema.class, opi.getCinemaid()));
				}
			}
		}
		model.put("cinemas", cinemas);
		model.put("opiList", opiList);
		return getXmlView(model, "api/sport/phoneActivityDetail.vm");
	}
}
