package com.gewara.web.action.common;

import java.util.Date;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.gewara.model.acl.GewaraUser;
import com.gewara.model.acl.User;
import com.gewara.untrans.GewaPicService;
import com.gewara.util.DateUtil;
import com.gewara.util.WebUtils;
import com.gewara.web.action.AnnotationController;
@Controller
public class UploadSingleAPK extends AnnotationController {
	@Autowired@Qualifier("gewaPicService")
	private GewaPicService gewaPicService;
	public void setGewaPicService(GewaPicService gewaPicService) {
		this.gewaPicService = gewaPicService;
	}
	@RequestMapping(value="/common/uploadSingleAPK.xhtml", method=RequestMethod.GET)
	public String showForm(){
		return "common/singleAPK.vm";
	}

	@RequestMapping(value="/common/uploadSingleAPK.xhtml", method=RequestMethod.POST)
	public String handleFormUpload(@CookieValue(value=LOGIN_COOKIE_NAME, required=false)String sessid,
			HttpServletRequest request, String successFile, String tag, Long relatedid, String callback, String ofilename,
			ModelMap model) throws Exception {
		GewaraUser user = loginService.getLogonGewaraUserBySessid(WebUtils.getRemoteIp(request), sessid);
		if(user == null || !(user instanceof User)) {
			model.put("msg", "请先登录！");
			return "redirect:/common/uploadSingleAPK.xhtml";
		}
		String realName = ofilename.toLowerCase();
		String uploadPath = getUploadPath();
		//1、获取上传的绝对路径
		model.put("tag", tag);
		model.put("relatedid", relatedid);
		model.put("uploadPath", uploadPath);
		model.put("callback", callback);
		gewaPicService.moveRemoteTempTo(user.getId(), tag, relatedid, uploadPath, successFile, realName);//将文件移动到正式文件夹
		model.put("msg", "ok");
		model.put("picpath", uploadPath + realName);
		return "redirect:/common/uploadSingleAPK.xhtml";
	}
	private String getUploadPath() {
		return "upload/apk/" + DateUtil.format(new Date(), "yyyyMM") + "/";
	}
}
