package com.gewara.web.action.index;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import com.gewara.json.MobileUpGrade;
import com.gewara.model.content.Advertising;
import com.gewara.service.content.AdService;
import com.gewara.untrans.NosqlService;
import com.gewara.util.WebUtils;
import com.gewara.web.action.AnnotationController;
@Controller
public class MobileIndexController extends AnnotationController{
	@Autowired@Qualifier("adService")
	private AdService adService;
	@Autowired@Qualifier("nosqlService")
	private NosqlService nosqlService;
	//手机访问
	@RequestMapping("/mobile/index.xhtml")
	public String mobiliWap(String apptype,ModelMap model, HttpServletRequest request, HttpServletResponse response){
		if(StringUtils.isBlank(apptype)) apptype = "cinema";
		String citycode = WebUtils.getAndSetDefault(request, response);
		String appsource = "AS06";
		MobileUpGrade mugandroid = nosqlService.getLastMobileUpGrade("android", apptype, appsource);
		if(mugandroid!=null) model.put("mugandroid", mugandroid);

		MobileUpGrade mugiphone = nosqlService.getLastMobileUpGrade("iphone", apptype, appsource);
		if(mugandroid!=null) model.put("mugiphone", mugiphone);

		List<Advertising> ads = adService.getAdListByPid(citycode, "mobileindex");
		model.put("adList", ads);
		return "mobile/wap.vm";
	}
	@RequestMapping("/mobile/mobileMovie.xhtml")
	public String mobileMovie(ModelMap model){
		String apptype = "cinema";
		String appsource = "AS06";
		MobileUpGrade mugandroid = nosqlService.getLastMobileUpGrade("android", apptype, appsource);
		if(mugandroid!=null) model.put("mugandroid", mugandroid);

		MobileUpGrade mugiphone = nosqlService.getLastMobileUpGrade("iphone", apptype, appsource);
		if(mugiphone!=null) model.put("mugiphone", mugiphone);

		return "mobile/movie.vm";
	}
	
	@RequestMapping("/mobile/mobileMovie15th.xhtml")
	public String mobileMovie15th(){
		return "mobile/movie15th.vm";
	}
	
	@RequestMapping("/mobile/mobileMovie16th.xhtml")
	public String mobileMovie16th(){
		return "mobile/movie16th.vm";
	}
	@RequestMapping("/mobile/mobileSport.xhtml")
	public String mobileSport(ModelMap model){
		String apptype = "sport";
		String appsource = "AS06";
		MobileUpGrade mugandroid = nosqlService.getLastMobileUpGrade("android", apptype, appsource);
		if(mugandroid!=null) model.put("mugandroid", mugandroid);

		MobileUpGrade mugiphone = nosqlService.getLastMobileUpGrade("iphone", apptype, appsource);
		if(mugandroid!=null) model.put("mugiphone", mugiphone);

		return "mobile/sport.vm";
	}
	
	/**
	 * 演出下载
	 */
	@RequestMapping("/mobile/mobileDrama.xhtml")
	public String mobileDrama(ModelMap model){
		String apptype = "drama";
		String appsource = "AS06";
		MobileUpGrade mugandroid = nosqlService.getLastMobileUpGrade("android", apptype, appsource);
		if(mugandroid!=null) model.put("mugandroid", mugandroid);

		MobileUpGrade mugiphone = nosqlService.getLastMobileUpGrade("iphone", apptype, appsource);
		if(mugandroid!=null) model.put("mugiphone", mugiphone);

		return "mobile/drama.vm";
	}

}
