package com.gewara.web.action.subject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import com.gewara.Config;
import com.gewara.model.draw.DrawActivity;
import com.gewara.model.draw.Prize;
import com.gewara.model.draw.WinnerInfo;
import com.gewara.service.drama.DrawActivityService;
import com.gewara.untrans.CommentService;
import com.gewara.util.BeanUtil;
import com.gewara.util.DateUtil;
import com.gewara.util.JsonUtils;
import com.gewara.util.WebUtils;
import com.gewara.web.action.AnnotationController;

// 重庆分站邀请专题 代理
@Controller
public class ChongQingHolidayProxyController  extends AnnotationController {

	@Autowired@Qualifier("drawActivityService")
	private DrawActivityService drawActivityService;
	public void setDrawActivityService(DrawActivityService drawActivityService) {
		this.drawActivityService = drawActivityService;
	}
	@Autowired@Qualifier("commentService")
	protected CommentService commentService;
	public void setCommentService(CommentService commentService) {
		this.commentService = commentService;
	}
	@Autowired@Qualifier("config")
	private Config config;
	public void setConfig(Config config) {
		this.config = config;
	}
	//查看邀请的用户
	@RequestMapping("/subject/proxy/holiday/getInviteMemberList.xhtml")
	public String getInviteMemberList(Long memberid, ModelMap model){
		if(memberid == null) return showJsonError(model, "参数错误！");
		List<Long> memberIdList = drawActivityService.getInviteMemberList(memberid, "email", true, DateUtil.parseTimestamp("2012-06-15 00:00:00"), DateUtil.getCurFullTimestamp());
		addCacheMember(model, memberIdList);
		Map<Long,Map> dataMap = (Map<Long,Map>) model.get("cacheMemberMap");
		return showJsonSuccess(model, JsonUtils.writeObjectToJson(dataMap));
	}
	//查看中奖信息
	@RequestMapping("/subject/proxy/holiday/getWinnerInfoList.xhtml")
	public String getInviteMemberList(String tag, ModelMap model){
		DrawActivity da = daoService.getObjectByUkey(DrawActivity.class, "tag", tag, true);
		if(da == null) return showJsonError(model, "参数错误！");
		List<Prize> pList = drawActivityService.getPrizeListByDid(da.getId(), new String[]{"A","D","P","drama","remark","waibi"});
		List<Long> pIdList = BeanUtil.getBeanPropertyList(pList,Long.class,"id",true);
		List<WinnerInfo> infoList = drawActivityService.getWinnerList(da.getId(),pIdList, null , null, "system",null,null,null,0,16);
		List<Map> winnerMapList = BeanUtil.getBeanMapList(infoList, new String[]{"memberid", "mobile", "prizeid"});
		for(Map info : winnerMapList){
			Prize prize = daoService.getObject(Prize.class, Long.valueOf(info.get("prizeid")+""));
			info.put("plevel", prize.getPlevel());
		}
		return showJsonSuccess(model, JsonUtils.writeObjectToJson(winnerMapList));
	}
	//查看单个用户中奖信息
	@RequestMapping("/subject/proxy/holiday/getMemberWinnerInfoList.xhtml")
	public String getMemberWinnerInfoList(String tag, Long memberid, ModelMap model){
		if(memberid == null) return showJsonError(model, "参数错误！");
		DrawActivity da = daoService.getObjectByUkey(DrawActivity.class, "tag", tag, true);
		if(da == null) return showJsonError(model, "参数错误！");
		List<WinnerInfo> winnerList = drawActivityService.getWinnerInfoByMemberid(da.getId(), memberid, 0, 10);
		List<Map> winnerMapList = BeanUtil.getBeanMapList(winnerList, new String[]{"prizeid","addtime"});
		List<Map> resultList = new ArrayList<Map>();
		for(Map info : winnerMapList){
			Prize prize = daoService.getObject(Prize.class, Long.valueOf(info.get("prizeid")+""));
			if(StringUtils.equals(prize.getPtype(), "empty")){
				continue;
			}
			info.put("plevel", prize.getPlevel());
			resultList.add(info);
		}
		return showJsonSuccess(model, JsonUtils.writeObjectToJson(resultList));
	}
	//查看中奖数量
	@RequestMapping("/subject/proxy/holiday/getWinnerCount.xhtml")
	public String getWinnerCount(String tag, ModelMap model){
		DrawActivity da = daoService.getObjectByUkey(DrawActivity.class, "tag", tag, true);
		if(da == null) return showJsonError(model, "参数错误！");
		int count = drawActivityService.getJoinDrawActivityCount(da.getId());
		return showJsonSuccess(model, count+"");
	}
	//查看用户抽奖次数
	@RequestMapping("/subject/proxy/holiday/getMemberDrawCount.xhtml")
	public String getMemberDrawCount(String tag, Long memberid, ModelMap model){
		DrawActivity da = daoService.getObjectByUkey(DrawActivity.class, "tag", tag, true);
		if(da == null) return showJsonError(model, "参数错误！");
		int drawcount = drawActivityService.getMemberWinnerCount(memberid, da.getId(), da.getStarttime(), da.getEndtime());
		int count = drawActivityService.getInviteMemberCount(memberid, "email", true, DateUtil.parseTimestamp("2012-06-15 00:00:00"), DateUtil.getCurFullTimestamp());
		int curcount = count + 1 - drawcount;
		return showJsonSuccess(model, curcount+"");
	}
	//邀请好友
	@RequestMapping("/inviteFriend.xhtml")
	public String drawInviteFriend(HttpServletResponse res, Long from, String invitetype){
		WebUtils.setInviteFromCookie(res, config.getBasePath(), from, invitetype);
		String str = "invite";
		if("email".equals(invitetype)){
			str = "holiday.xhtml";
		}else if("invitation".equals(invitetype)){
			str = "invitation.xhtml";
		}else if("musiccat".equals(invitetype)){
			str = "musiccat.xhtml";
		}
		return "redirect:/zhuanti/"+str;
	} 
}