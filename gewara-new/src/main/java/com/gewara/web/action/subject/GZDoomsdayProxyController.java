package com.gewara.web.action.subject;
import java.sql.Timestamp;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.RequestMapping;

import com.gewara.model.user.Member;
import com.gewara.service.bbs.CommonVoteService;
import com.gewara.util.DateUtil;
import com.gewara.util.WebUtils;
import com.gewara.web.action.AnnotationController;

@Controller
public class GZDoomsdayProxyController extends AnnotationController {
	@Autowired@Qualifier("commonVoteService")
	private CommonVoteService commonVoteService;
	
	/****
	 * 广东末日专题 专用
	 * */
	private static final String TAG = "gzdoomsday";
	private static final String FLAG = "gzdoomsday_virtual";
	
	@RequestMapping("/subject/proxy/vote/delvote.xhtml")
	public String delvote(String id, ModelMap model){
		commonVoteService.delVote(id);
		return showJsonSuccess(model);
	}
	
	// 虚拟造假投票
	@RequestMapping("/subject/proxy/vote/virtualVote.xhtml")
	public String virtualVote(String itemid, Integer support, ModelMap model){
		commonVoteService.addCommonVote(FLAG, itemid, support);
		return showJsonSuccess(model);
	}

	//广州末日活动用户投票
	@RequestMapping("/ajax/subject/proxy/vote/voteit.xhtml")
	public String voteit(@CookieValue(value=LOGIN_COOKIE_NAME, required=false)String sessid, String itemid, HttpServletRequest request,ModelMap model){
		//1. 活动时间: 20121212 ~ 20121218
		Timestamp enddate = DateUtil.parseTimestamp("2012-12-19 00:00:00");
		Timestamp curdate = DateUtil.getCurFullTimestamp();
		if(curdate.compareTo(enddate) >= 0) return showJsonError(model, "活动已结束！");
		Member member = loginService.getLogonMemberBySessid(WebUtils.getRemoteIp(request), sessid);
		if(member == null) return showJsonError_NOT_LOGIN(model);
		//2. memberid + movieid 验证, 并增加投票数量
		Map<String, Object> voteMap = commonVoteService.getSingleVote(TAG, member.getId(), null);
		if(voteMap != null) return showJsonError(model, "您已投过票！");
		commonVoteService.addVoteMap(TAG, itemid, member.getId(), FLAG);
		return showJsonSuccess(model);
	}
}
