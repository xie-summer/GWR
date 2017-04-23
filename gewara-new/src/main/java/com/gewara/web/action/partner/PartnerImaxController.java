package com.gewara.web.action.partner;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import com.gewara.util.DateUtil;
import com.gewara.util.StringUtil;

@Controller
public class PartnerImaxController extends BasePartnerController {
	@RequestMapping("/partner/imax/goto.xhtml")
	public String returnToNew(Long movieid, HttpServletResponse response, ModelMap model){
		long validtime = System.currentTimeMillis()+DateUtil.m_hour*2;
		Cookie cookie = new Cookie("origin", "imax:" + validtime+":" +StringUtil.md5WithKey("imax" + validtime, 8));
		cookie.setPath("/cinema/order/");
		cookie.setMaxAge(60 * 60 * 12);//12 hour
		response.addCookie(cookie);
		return showRedirect("/movie/"+movieid, model);
	}

}
