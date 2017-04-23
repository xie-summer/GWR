package com.gewara.web.action.user;

import java.util.Arrays;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.RequestMapping;

import com.gewara.constant.CookieConstant;
import com.gewara.constant.ExpGrade;
import com.gewara.constant.Status;
import com.gewara.constant.order.AddressConstant;
import com.gewara.json.MemberStats;
import com.gewara.model.user.Member;
import com.gewara.model.user.MemberInfo;
import com.gewara.model.user.Treasure;
import com.gewara.service.bbs.BlogService;
import com.gewara.service.member.FriendService;
import com.gewara.support.ErrorCode;
import com.gewara.untrans.CommentService;
import com.gewara.untrans.MemberCountService;
import com.gewara.untrans.ShareService;
import com.gewara.util.WebUtils;
import com.gewara.web.action.AnnotationController;
import com.gewara.xmlbind.bbs.Comment;

/**
 * 有关哇啦用户详细页
 * @author lss
 *
 */
@Controller
public class MicroMemberController extends AnnotationController {
	@Autowired@Qualifier("commentService")
	private CommentService commentService;

	@Autowired@Qualifier("blogService")
	private BlogService blogService;
	@Autowired@Qualifier("memberCountService")
	private MemberCountService memberCountService;

	
	@Autowired@Qualifier("friendService")
	private FriendService friendService;
	public void setFriendService(FriendService friendService) {
		this.friendService = friendService;
	}
	@Autowired@Qualifier("shareService")
	private ShareService shareService;
	public void setShareService(ShareService shareService) {
		this.shareService = shareService;
	}

	/**
	 *  wara 个人介绍保存
	 * */
	@RequestMapping("/wala/saveMemberDesc.xhtml")
	public String saveMemberDesc(@CookieValue(value=LOGIN_COOKIE_NAME, required=false)String sessid,
			HttpServletRequest request, ModelMap model, String memberdesc){
		Member member = loginService.getLogonMemberBySessid(WebUtils.getRemoteIp(request), sessid);
		if(member == null) return showError(model, "未登录!");
		MemberInfo memberInfo = daoService.getObject(MemberInfo.class, member.getId());
		memberInfo.setIntroduce(memberdesc);
		daoService.saveObject(memberInfo);
		model.put("memberInfo", memberInfo);
		return "wala/memberdesc.vm";
	}
	
	
	/**
	 * 删除微薄
	 */
	@RequestMapping("/wala/deleteMicroBlog.xhtml")
	public String deleteMicroBlog(ModelMap model, Long mid, @CookieValue(value=LOGIN_COOKIE_NAME, required=false)String sessid, HttpServletRequest request) {
		Comment comment = commentService.getCommentById(mid);
		if (comment == null) {
			return show404(model, "你删除的资源不存在");
		}
		Member member = loginService.getLogonMemberBySessid(WebUtils.getRemoteIp(request), sessid);
		if (comment.getMemberid().equals(member.getId())) {
			comment.setStatus(Status.N_DELETE);
			memberService.addExpForMember(member.getId(), -ExpGrade.EXP_COMMENT_ADD_COMMON);
			commentService.updateComment(comment);
			memberCountService.updateMemberCount(member.getId(), MemberStats.FIELD_COMMENTCOUNT, 1, false);
			return showJsonSuccess(model);
		} else {
			return showJsonError(model, "不能删除他人的哇啦！");
		}
	}
	
	/**
	 * 转载
	 * 
	 * @param model
	 * @param transferid
	 * @param body
	 * @return
	 */
	@RequestMapping("/wala/addTransferComment.xhtml")
	public String addTransferComment(@CookieValue(value=CookieConstant.MEMBER_POINT, required=false)String pointxy, ModelMap model, Long transferid, 
			String body, @CookieValue(value=LOGIN_COOKIE_NAME, required=false)String sessid, HttpServletRequest request) {
		String ip = WebUtils.getRemoteIp(request);
		Member member = loginService.getLogonMemberBySessid(ip, sessid);
		if (StringUtils.isBlank(body) || "顺便说点什么吧...".equals(body))
			body = "转发哇啦。";
		if (StringUtils.isNotBlank(body) && body.length() > 140)
			return showJsonError(model, "转载发表的内容长度不能大于140字符！");
		Comment comment = commentService.getCommentById(transferid);
		if (comment != null) {
			String pointx = null, pointy = null;
			if(StringUtils.isNotBlank(pointxy)){
				List<String> pointList = Arrays.asList(StringUtils.split(pointxy, ":"));
				if(pointList.size() == 2){
					pointx = pointList.get(0);
					pointy = pointList.get(1);
				}
			}
			ErrorCode<Comment> result = commentService.addMicroComment(member, comment.getTag(), comment.getRelatedid(), body, null, AddressConstant.ADDRESS_WEB, transferid, false, null, pointx, pointy, WebUtils.getIpAndPort(ip, request));
			if(result.isSuccess()){
				shareService.sendShareInfo("wala",result.getRetval().getId(), result.getRetval().getMemberid(), null);
				return showJsonSuccess(model);
			}
			return showJsonError(model, result.getMsg());
		}
		return showJsonError(model, "转载失败！");
	}
	
	/**
	 * 解除好友关系
	 */
	@RequestMapping("/wala/removeFriend.xhtml")
	public String removeFriend(Long memberid,ModelMap model, @CookieValue(value=LOGIN_COOKIE_NAME, required=false)String sessid, HttpServletRequest request){
		Member member = loginService.getLogonMemberBySessid(WebUtils.getRemoteIp(request), sessid);
		Member memberto = daoService.getObject(Member.class,memberid);
		try{
			//解除好友关系
			friendService.deleteFriend(member.getId(), memberto.getId());
			//删除关注记录
			blogService.cancelTreasure(member.getId(), memberto.getId(), Treasure.TAG_MEMBER, Treasure.ACTION_COLLECT);
		}catch(Exception e){
			return showJsonError(model,"解除好友关系失败！");
		}
		return showJsonSuccess(model);
	}
	
	/**
	 * 检查用户是否存在
	 */
	@RequestMapping("/check/checkUserName.xhtml")
	public String checkUserName(ModelMap model,String username, @CookieValue(value=LOGIN_COOKIE_NAME, required=false)String sessid, HttpServletRequest request){
		Member member = loginService.getLogonMemberBySessid(WebUtils.getRemoteIp(request), sessid);
		Member toMember = friendService.checkUserName(username);
		if(toMember == null){
			return showJsonError(model, "你要发送私信的用户不存在！");
		}
		boolean isTreasure = blogService.isTreasureMember(toMember.getId(), member.getId());
		if(!isTreasure){
			return showJsonError(model, "你要发送私信的用户暂未关注你！你不能不对TA发送私信！");
		}else{
			return showJsonSuccess(model);
		}
	}
}
