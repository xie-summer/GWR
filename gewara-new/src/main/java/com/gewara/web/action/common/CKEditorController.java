package com.gewara.web.action.common;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.RequestMapping;

import com.gewara.Config;
import com.gewara.model.acl.GewaraUser;
import com.gewara.untrans.GewaPicService;
import com.gewara.util.DateUtil;
import com.gewara.util.StringUtil;
import com.gewara.util.WebUtils;
import com.gewara.web.action.AnnotationController;
@Controller
public class CKEditorController extends AnnotationController {
	@Autowired@Qualifier("config")
	private Config config;
	public void setConfig(Config config) {
		this.config = config;
	}
	@Autowired@Qualifier("gewaPicService")
	private GewaPicService gewaPicService;
	public void setGewaPicService(GewaPicService gewaPicService) {
		this.gewaPicService = gewaPicService;
	}
	@RequestMapping("/common/fckUploadCallback.xhtml")
	public void uploadCallback(@CookieValue(value=LOGIN_COOKIE_NAME, required=false)String sessid,
			HttpServletRequest request, HttpServletResponse response, String paramchk, String successFile, String invalidFile, String msg) throws IOException{
		GewaraUser user = loginService.getLogonGewaraUserBySessid(WebUtils.getRemoteIp(request), sessid);
		String mycheck = StringUtil.md5(successFile + config.getString("uploadKey"));
		response.setCharacterEncoding("UTF-8");
		response.setContentType("text/html; charset=UTF-8");
		response.setHeader("Cache-Control", "no-cache");
		PrintWriter out = response.getWriter();
		String uploadPath = "";
		if(StringUtils.isBlank(msg)) {
			if(!mycheck.equals(paramchk)) {
				msg = "校验错误";
			}else{
				if(StringUtils.isNotBlank(invalidFile)){
					msg = "文件名不合法";
				}else if(user==null){
					msg = "请先登录！";
				}else{
					uploadPath = "userfiles/image/" + DateUtil.format(new Date(), "yyyyMM") + "/"; //上传路径
					gewaPicService.moveRemoteTempTo(user.getId(), "bbs", null, uploadPath, successFile);//将文件移动到正式文件夹
				}
			}
		}
		String callback = request.getParameter("CKEditorFuncNum");
		String result = getCKUploadRes(callback, uploadPath, successFile, msg);
		out.print(result);
		out.flush();
		out.close();
	}
	private String getCKUploadRes(String callback, String uploadPath, String successFile, String msg){
		String imgUrl = StringUtils.isBlank(successFile)? "": config.getString("picPath") + uploadPath + successFile;
		if(StringUtils.isBlank(msg)) msg = "";
		String result = "<script type=\"text/javascript\">";
		result = result + "window.parent.CKEDITOR.tools.callFunction(" + callback + ",'" + imgUrl + "','" +  msg + "')";
		result = result + "</script>";
		return result;
	}
}
