package com.gewara.web.action.subject;

import java.sql.Timestamp;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import com.gewara.constant.DrawActicityConstant;
import com.gewara.constant.MemberConstant;
import com.gewara.constant.TagConstant;
import com.gewara.model.common.VersionCtl;
import com.gewara.model.draw.DrawActivity;
import com.gewara.model.draw.Prize;
import com.gewara.model.draw.WinnerInfo;
import com.gewara.model.user.Member;
import com.gewara.model.user.MemberInfo;
import com.gewara.service.OperationService;
import com.gewara.service.drama.DrawActivityService;
import com.gewara.support.ErrorCode;
import com.gewara.untrans.CommentService;
import com.gewara.untrans.ShareService;
import com.gewara.untrans.activity.TenWowActivityService;
import com.gewara.untrans.impl.ControllerService;
import com.gewara.util.DateUtil;
import com.gewara.util.StringUtil;
import com.gewara.util.VmUtils;
import com.gewara.util.WebUtils;
import com.gewara.web.action.AnnotationController;
import com.gewara.xmlbind.bbs.Comment;

@Controller
public class SubjectTenwowController  extends AnnotationController{
	@Autowired@Qualifier("tenWowActivityService")
	private TenWowActivityService tenWowActivityService;

	@Autowired@Qualifier("operationService")
	private OperationService operationService;
	
	@Autowired@Qualifier("controllerService")
	private ControllerService controllerService;
	
	@Autowired@Qualifier("drawActivityService")
	private DrawActivityService drawActivityService;
	
	@Autowired@Qualifier("commentService")
	private CommentService commentService;
	
	@Autowired@Qualifier("shareService")
	private ShareService shareService;
	
	@RequestMapping("/subject/proxy/tenwowDraw/startDraw.xhtml")
	public String startDraw(HttpServletRequest request, String tag, String checkValue, Long memberId,String captchaId,String captcha,String pointxy,String ip,String authenticode,
			ModelMap model){
		boolean validate = controllerService.validateCaptcha(captchaId, captcha, WebUtils.getRemoteIp(request));
		if(!validate){
			model.put("directResult", "验证码输入错误，请重新输入！");
			return "common/direct.vm";
		}
		if(!StringUtils.equals(checkValue, StringUtil.md5(memberId + "tenwow_ddsafssjyi21349873"))){
			model.put("directResult", "请先登录!");
			return "common/direct.vm";
		}
		Member member = this.daoService.getObject(Member.class, memberId);
		if(member == null){
			model.put("directResult", "请先登录!");
			return "common/direct.vm";
		}
		String opkey = tag + member.getId();
		boolean allow = operationService.updateOperation(opkey, 5);
		if(!allow) {
			model.put("directResult", "操作太频繁，请休息下再兑换！");
			return "common/direct.vm";
		}
		String directResult =  clickDraw(authenticode, tag, member, pointxy, ip,"");
		model.put("directResult", directResult);
		return "common/direct.vm";
	}
	
	@RequestMapping("/api2/mobile/tenwoClickDraw.xhtml")
	public String survey(String memberEncode,String pointxy,String ip,String authenticode,String tag,ModelMap model) {
		Member member = memberService.getMemberByEncode(memberEncode);
		if(member == null){
			model.put("directResult", "请先登录!");
			return "common/direct.vm";
		}
		String opkey = tag + member.getId();
		boolean allow = operationService.updateOperation(opkey, 5);
		if(!allow) {
			model.put("directResult", "操作太频繁，请休息下再兑换！");
			return "common/direct.vm";
		}
		String directResult =  clickDraw(authenticode, tag, member, pointxy,ip,memberEncode);
		model.put("directResult", directResult);
		return "common/direct.vm";
	}
	
	private String clickDraw(String authenticode,String tag,Member member,String pointxy,String ip,String memberEncode){
		if(!tenWowActivityService.isAuthenticode(authenticode)){
			return "对不起，亲爱的用户，您输入的活动码无法使用，请输入有效的活动码进行兑换!";
		}
		DrawActivity da = daoService.getObjectByUkey(DrawActivity.class, "tag", tag, true);
		if(da == null||!da.isJoin()) {
			return "活动还未开始或已结束，请在活动期间进行兑换！";
		}
		Map<String, String> otherinfoMap = VmUtils.readJsonToMap(da.getOtherinfo());
		if(StringUtils.isNotBlank(otherinfoMap.get(DrawActicityConstant.TASK_MOBILE)) && !member.isBindMobile()) {
			return "参与该活动需要用户绑定手机号！";
		}
		MemberInfo memberInfo = daoService.getObject(MemberInfo.class, member.getId());
		if(StringUtils.isNotBlank(otherinfoMap.get(DrawActicityConstant.TASK_EMAIL)) && !memberInfo.isFinishedTask(MemberConstant.TASK_CONFIRMREG)) {
			return "参与该活动需要用户绑定邮箱！";
		}
		Timestamp cur = DateUtil.getCurFullTimestamp();
		if(StringUtils.isNotBlank(otherinfoMap.get(DrawActicityConstant.TASK_MOREDRAW))){
			if(StringUtils.isNotBlank(otherinfoMap.get(DrawActicityConstant.TASK_HOUR))){
				Timestamp startTime = DateUtil.addHour(cur,-Integer.parseInt(otherinfoMap.get(DrawActicityConstant.TASK_HOUR)));
				List<WinnerInfo> winnerList = drawActivityService.getWinnerList(da.getId(), null,  startTime, cur, "system", member.getId(), member.getMobile(), null, 0, 1);
				if(winnerList.size() == 1){
					return "对不起，亲爱的用户，" + Integer.parseInt(otherinfoMap.get(DrawActicityConstant.TASK_HOUR)) + "小时内只能兑换一次哦";
				}
			}
		}
		if(StringUtils.isNotBlank(otherinfoMap.get(DrawActicityConstant.TASK_ONLYONE))){
			int drawtimes = drawActivityService.getMemberWinnerCount(member.getId(), da.getId(), da.getStarttime(), da.getEndtime());
			if(drawtimes > 0) {
				return "对不起，亲爱的用户您只有一次兑换机会哦！";
			}
		}
		VersionCtl mvc = drawActivityService.gainMemberVc(""+member.getId());
		//FIXME:黄牛？？
		ErrorCode<WinnerInfo> ec = drawActivityService.baseClickDraw(da, mvc, false, member);
		if(ec == null || !ec.isSuccess()) {
			return ec.getMsg();
		}
		WinnerInfo winnerInfo = ec.getRetval();
		if(winnerInfo == null) {
			return "兑换时用户信息出错，请重新兑换！";
		}
		Prize prize = daoService.getObject(Prize.class, winnerInfo.getPrizeid());
		if(prize == null) {
			return "对不起，亲爱的用户,奖品已兑换完，请下次兑换！";
		}
		tenWowActivityService.useAuthenticode(authenticode, member.getId() + "", memberEncode, member.getNickname());
		drawActivityService.sendPrize(prize, winnerInfo, false);
		/*if(sms !=null) {
			untransService.sendMsgAtServer(sms, false);
		}*/
		Map otherinfo = VmUtils.readJsonToMap(prize.getOtherinfo());
		if(otherinfo.get(DrawActicityConstant.TASK_WALA_CONTENT) != null){
			String link = null;
			if(otherinfo.get(DrawActicityConstant.TASK_WALA_LINK) != null){
				link = otherinfo.get(DrawActicityConstant.TASK_WALA_LINK)+"";
				link = "<a href=\""+link+"\" target=\"_blank\" rel=\"nofollow\">"+"链接地址"+"</a>";
			}
			String pointx = null, pointy = null;
			if(StringUtils.isNotBlank(pointxy)){
				List<String> pointList = Arrays.asList(StringUtils.split(pointxy, ":"));
				if(pointList.size() == 2){
					pointx = pointList.get(0);
					pointy = pointList.get(1);
				}
			}
			ErrorCode<Comment> result = commentService.addComment(member, TagConstant.TAG_TOPIC, null, otherinfo.get(DrawActicityConstant.TASK_WALA_CONTENT)+"", link, false, pointx, pointy, ip);
			if(result.isSuccess()) {
				shareService.sendShareInfo("wala",result.getRetval().getId(), result.getRetval().getMemberid(), null);
			}
		}
		if(prize.getPtype().equals("empty")){
			return "success:empty";
		}
		return "success:" + prize.getPlevel();
	}
}
