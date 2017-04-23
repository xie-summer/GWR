package com.gewara.web.action.api2mobile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import com.gewara.constant.ApiConstant;
import com.gewara.constant.sys.MongoData;
import com.gewara.json.PhoneActivity;
import com.gewara.model.content.PhoneAdvertisement;
import com.gewara.model.movie.Cinema;
import com.gewara.model.ticket.OpenPlayItem;
import com.gewara.web.action.api.BaseApiController;

@Controller
public class Api2MobileAdvertController extends BaseApiController{
	/**
	 * 手机首页广告列表
	 */
	@RequestMapping("/api2/common/phoneAdvertList.xhtml")
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
	
	
	@RequestMapping("/api2/common/phoneActivityDetail.xhtml")
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
