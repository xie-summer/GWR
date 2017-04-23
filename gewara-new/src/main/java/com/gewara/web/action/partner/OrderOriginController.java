package com.gewara.web.action.partner;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import com.gewara.helper.ticket.TicketUtil;
import com.gewara.util.DateUtil;
import com.gewara.util.StringUtil;
import com.gewara.util.WebUtils;
import com.gewara.web.action.AnnotationController;

@Controller
public class OrderOriginController extends AnnotationController{
	//合作商平台跳转到格瓦拉平台
	@RequestMapping("/outOrigin.xhtml")
	public String outOrigin(String origin, String target, HttpServletRequest request, HttpServletResponse response,ModelMap model) throws IOException{
		dbLogger.warnMap(WebUtils.getRequestMap(request));
		if(StringUtils.isBlank(origin) || StringUtils.isBlank(target)){
			return forwardMessage(model, "缺少参数");
		}
		long validtime = System.currentTimeMillis() + DateUtil.m_hour*2;
		addOriCookie(origin, "/cinema/order/", validtime, response);
		addOriCookie(origin, "/drama/order/", validtime, response);
		addOriCookie(origin, "/sport/order/", validtime, response);
		if(StringUtils.contains(target, "cinema/order/step1.shtml")){
			Pattern p = Pattern.compile("mpid=(\\d+)");
			Matcher m = p.matcher(target);
			if(m.find()) {
				Long mpid = Long.valueOf(m.group(1)+"");
				String token = TicketUtil.getToken(mpid);
				target = target + "&tkn=" + token;
			}
		}
		response.sendRedirect(target);
		return null;
	}
	private void addOriCookie(String origin, String path, Long validtime,HttpServletResponse response){
		WebUtils.addCookie(response, "origin",  origin + ":" + validtime + ":" + StringUtil.md5WithKey(origin + validtime, 8), path, 60 * 60 * 12);
	}
}
