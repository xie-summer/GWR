package com.gewara.web.action.common;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.gewara.model.acl.GewaraUser;
import com.gewara.model.content.Picture;
import com.gewara.untrans.GewaPicService;
import com.gewara.util.DateUtil;
import com.gewara.util.WebUtils;
import com.gewara.web.action.AnnotationController;
@Controller
public class UploadSinglePicture extends AnnotationController{
	@Autowired@Qualifier("gewaPicService")
	private GewaPicService gewaPicService;
	public void setGewaPicService(GewaPicService gewaPicService) {
		this.gewaPicService = gewaPicService;
	}
	@RequestMapping(value="/common/uploadSinglePicture.xhtml", method=RequestMethod.GET)
	public String showForm(){
		return "common/singlePicture.vm";
	}
	@RequestMapping(value="/common/uploadSinglePicture.xhtml", method=RequestMethod.POST)
	public String handleFormUpload(@CookieValue(value=LOGIN_COOKIE_NAME, required=false)String sessid,
			HttpServletRequest request, String successFile, String tag, Long relatedid, String uploadPath, String notLimit,
			String callback, String description, ModelMap model) throws Exception {
		GewaraUser user = loginService.getLogonGewaraUserBySessid(WebUtils.getRemoteIp(request), sessid);
		if(user == null) {
			model.put("msg", "请先登录！");
			return "redirect:/common/uploadSinglePicture.xhtml";
		}
		String newUploadPath = getUploadPath(uploadPath);
		if(!StringUtils.startsWith(newUploadPath, "images") || newUploadPath.length() > 50){
			model.put("msg", "上传目录有错误！");
			return "redirect:/common/uploadSinglePicture.xhtml";
		}
		//1、获取上传的绝对路径
		model.put("tag", tag);
		model.put("relatedid", relatedid);
		model.put("uploadPath", uploadPath);
		model.put("callback", callback);
		gewaPicService.moveRemoteTempTo(user.getId(), tag, relatedid, newUploadPath, successFile);//将文件移动到正式文件夹
		if(StringUtils.isNotBlank(tag) && relatedid != null){
			// 将图片计入数据库
			Picture picture = new Picture(tag, relatedid, newUploadPath + successFile, description);
			picture.setMemberid(user.getId());
			picture.setMemberType(user.getUsertype());
			daoService.saveObject(picture);
			model.put("picid", ""+picture.getId());
		}
		model.put("msg", "ok");
		model.put("picpath", newUploadPath + successFile);
		if(StringUtils.isNotBlank(notLimit)) model.put("notLimit", notLimit);
		return "redirect:/common/uploadSinglePicture.xhtml";
	}
	private String getUploadPath(String uploadPath) {
		if(StringUtils.isBlank(uploadPath)) return "/resources";
		if(!uploadPath.endsWith("/") && !uploadPath.endsWith("\\")) return uploadPath + "/" + DateUtil.format(new Date(), "yyyyMM") + "/";
		return uploadPath + DateUtil.format(new Date(), "yyyyMM") + "/";
	}
}
