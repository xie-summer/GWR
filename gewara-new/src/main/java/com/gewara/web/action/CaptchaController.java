package com.gewara.web.action;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.patchca.PatchcaUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import com.gewara.Config;
import com.gewara.support.GewaCaptchaService;
import com.gewara.util.StringUtil;
import com.gewara.util.VmUtils;
import com.gewara.util.WebUtils;

@Controller
public class CaptchaController extends AnnotationController{
	@Autowired@Qualifier("config")
	private Config config;
	@Autowired@Qualifier("captchaService")
	private GewaCaptchaService captchaService;
	@RequestMapping("/getCaptchaId.xhtml")
	public String getCaptchaId(HttpServletRequest request, ModelMap model){
		String captchaId = VmUtils.getRandomCaptchaId();
		boolean isAjax = WebUtils.isAjaxRequest(request);
		if(!isAjax){
			captchaId = StringUtils.reverse(captchaId.substring(0, 16)) + captchaId.substring(16);
			dbLogger.error("GetInvalidCaptchaID:" + WebUtils.getRemoteIp(request));
		}
		return showJsonSuccess(model, captchaId);
	}
	@RequestMapping("/captcha.xhtml")
	public void showPicture(HttpServletResponse response, HttpServletRequest request, String captchaId, String zt/*专题特别使用*/) throws Exception {
		captchaId = StringUtils.substring(captchaId, 0, 100);
		//支持专题活动定制
		if(!VmUtils.isValidCaptchaId(captchaId)) {
			captchaId = "err" + captchaId;
			dbLogger.error("UseInvalidCaptchaID:" + WebUtils.getRemoteIp(request));
		}
		if(StringUtils.isNotBlank(zt)){
			String referer = request.getHeader("referer");
			if(!StringUtils.contains(referer, config.getString("domain")) && !WebUtils.isLocalRequest(request)){
				captchaId = "errorCaptcha"+zt;
			}else{
				captchaId += "zt2reXy";
			}
		}
		try{
			BufferedImage challenge = captchaService.getCaptchaImage(captchaId);
			response.setHeader("Cache-Control", "no-store");
			response.setHeader("Pragma", "no-cache");
			response.setDateHeader("Expires", 0);
			response.setContentType("image/jpeg");
			ServletOutputStream os = response.getOutputStream();
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			PatchcaUtils.printImage(bos, challenge);
			os.write(bos.toByteArray());
			os.flush();
			os.close();
		}catch(Exception e){
			dbLogger.error(StringUtil.getExceptionTrace(e));
		}
	}
}