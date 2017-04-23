package com.gewara.web.action.user;

import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.gewara.model.user.Member;
import com.gewara.util.WebUtils;
import com.gewara.web.action.AnnotationController;

@Controller
public class QCController extends AnnotationController {
	//×¢²áÒ³Ãæ
	@RequestMapping("/qc/register.xhtml")
	public String qcRegister(){
		return "home/register/qcRegister.vm";
	}
	//µÇÂ¼Ò³Ãæ
	@RequestMapping(value="/qc/login.xhtml",method=RequestMethod.GET)
	public String qcLogin(@CookieValue(value=LOGIN_COOKIE_NAME, required=false)String sessid,
			HttpServletRequest request, ModelMap model){
		Member member = loginService.getLogonMemberBySessid(WebUtils.getRemoteIp(request), sessid);
		if(member != null) {
			model.put("commuid", "7924870");
			return showRedirect("/quan/commuDetail.xhtml", model);
		}
		return "home/register/qcLogin.vm";
	}
}
