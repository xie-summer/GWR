package com.gewara.web.action;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import com.gewara.model.user.Member;
import com.gewara.util.BeanUtil;
import com.gewara.util.JsonUtils;
import com.gewara.util.WebUtils;
import com.gewara.web.action.admin.BaseAdminController;

/**
 * @author <a href="mailto:acerge@163.com">gebiao(acerge)</a>
 * @since Mar 31, 2008 AT 3:20:44 PM
 */
@Controller
public class LoginController extends BaseAdminController {
	@RequestMapping("/login.xhtml")
	public String login(HttpServletRequest request, HttpServletResponse response){
		WebUtils.getAndSetDefault(request, response);
		return "login.vm";
	}
	@RequestMapping("/ajax/getLogonMember.xhtml")
	public String getLogonMember(String ip, String sessid, ModelMap model) {
		Member member = loginService.getLogonMemberBySessid(ip, sessid);
		Map<String, String> memberMap = new HashMap<String, String>();
		if(member==null){
			return showJsonError_NOT_LOGIN(model);
		}else{
			memberMap = BeanUtil.getBeanMapWithKey(member, "id", "nickname", "mobile", "rejected");
			String headpicUrl = memberService.getCacheHeadpicMap(member.getId());
			memberMap.put("headpicUrl", headpicUrl);
		}
		return showJsonSuccess(model,JsonUtils.writeMapToJson(memberMap));
	}
	
}
