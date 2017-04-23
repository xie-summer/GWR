package com.gewara.web.action.common;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.RequestMapping;

import com.gewara.constant.CookieConstant;
import com.gewara.constant.TagConstant;
import com.gewara.model.user.Member;
import com.gewara.service.bbs.BlogService;
import com.gewara.support.ErrorCode;
import com.gewara.support.ServiceHelper;
import com.gewara.untrans.CommentService;
import com.gewara.untrans.ShareService;
import com.gewara.util.StringUtil;
import com.gewara.util.WebUtils;
import com.gewara.web.action.BaseHomeController;
import com.gewara.xmlbind.bbs.Comment;

@Controller
public class MicroTemplateController extends BaseHomeController {
	@Autowired@Qualifier("commentService")
	private CommentService commentService;
	public void setCommentService(CommentService commentService) {
		this.commentService = commentService;
	}
	@Autowired@Qualifier("blogService")
	private BlogService blogService = null;
	public void setBlogService(BlogService blogService) {
		this.blogService = blogService;
	}
	@Autowired@Qualifier("shareService")
	private ShareService shareService;
	public void setShareService(ShareService shareService) {
		this.shareService = shareService;
	}

	// 模板首页
	@RequestMapping("/loadMicroTemplate.xhtml")
	public String microTemplate(String moderate, ModelMap model){
		model.put("moderate", moderate);
		return "common/microTemplate.vm";
	}
	// 页面load时加载话题列表
	@RequestMapping("/loadMicroModerTable.xhtml")
	public String loadMicroModerTable(String moderate, Integer count, String isReply, String isZtMarquee, ModelMap model){
		List<Comment> commentList = commentService.getModeratorDetailList(moderate, false, 0, count);
		model.put("moderate", moderate);
		model.put("commentList", commentList);
		addCacheMember(model, ServiceHelper.getMemberIdListFromBeanList(commentList));
		model.put("isReply", isReply == null ? false : true);
		model.put("isZtMarquee", StringUtils.equals("true", isZtMarquee) ? true : false);
		return "common/microTemplateModerateTable.vm";
	}
	@RequestMapping("/newloadMicroModerTable.xhtml")
	public String newloadMicroModerTable(String moderate, Integer count, String isReply, ModelMap model){
		List<Comment> commentList = commentService.getModeratorDetailList(moderate, false, 0, count);
		model.put("moderate", moderate);
		model.put("isReply", isReply == null ? false : isReply);
		Map<Long, Comment> tranferCommentMap = new HashMap<Long, Comment>();// 转载评论
		if(commentList != null){
			for (Comment comment : commentList) {
				if (comment.getTransferid() != null) {
					Comment c = commentService.getCommentById(comment.getTransferid());
					if (c != null && StringUtils.isNotBlank(c.getBody())) {
						tranferCommentMap.put(c.getId(), c);
					}
				}
			}
		}
		model.put("commentList", commentList);
		addCacheMember(model, ServiceHelper.getMemberIdListFromBeanList(commentList));
		model.put("tranferCommentMap", tranferCommentMap);
		return "common/newWalaTemplate.vm";
	}
	//按类型加载哇啦
	@RequestMapping("/loadMicroByTag.xhtml")
	public String loadMicroByTag(String tag, Long relatedid, Integer rows, ModelMap model){
		List<Comment> commentList = commentService.getCommentListByRelatedId(tag,null, relatedid, null, 0, rows);
		Map<Long, Comment> tranferCommentMap = new HashMap<Long, Comment>();// 转载评论
		for (Comment comment : commentList) {
			if (comment.getTransferid() != null) {
				Comment c = commentService.getCommentById(comment.getTransferid());
				if (c != null && StringUtils.isNotBlank(c.getBody())) {
					tranferCommentMap.put(c.getId(), c);
				}
			}
		}
		model.put("tranferCommentMap", tranferCommentMap);
		addCacheMember(model, ServiceHelper.getMemberIdListFromBeanList(commentList));
		model.put("commentList", commentList);
		return "movie/new_ReplyShow.vm";
	}
		
	// 动态发表微博 Ajax
	@RequestMapping("/dnySendMicroModer.xhtml")
	public String dnySendMicroModer(@CookieValue(value=LOGIN_COOKIE_NAME, required=false)String sessid, @CookieValue(value=CookieConstant.MEMBER_POINT,required=false)String pointxy,
			HttpServletRequest request, ModelMap model, String moderate, String micrbody, String link) throws Exception {
		String ip = WebUtils.getRemoteIp(request);
		Member member = loginService.getLogonMemberBySessid(ip, sessid);
		if(member==null) return showJsonError(model, "你还没有登陆，请先登录！");
		if (StringUtils.isBlank(micrbody)) return showJsonError(model, "评论的内容不能为空！");
		if (StringUtils.length(micrbody) > 140) return showJsonError(model, "评论的内容不能超过140个字符！");
		if(blogService.isBlackMember(member.getId())) return showJsonError_BLACK_LIST(model);
		if(WebUtils.checkString(micrbody))return showJsonError(model, "评论的内容不能出现非法字符！");
		micrbody = "#" + moderate + "#"+ StringUtil.getHtmlText(micrbody);
		String pointx = null, pointy = null;
		if(StringUtils.isNotBlank(pointxy)){
			List<String> pointList = Arrays.asList(StringUtils.split(pointxy, ":"));
			if(pointList.size() == 2){
				pointx = pointList.get(0);
				pointy = pointList.get(1);
			}
		}
		ErrorCode<Comment> result = commentService.addComment(member, TagConstant.TAG_TOPIC, null, micrbody,link, false, pointx, pointy, WebUtils.getIpAndPort(ip, request));
		if(result.isSuccess()) {
			shareService.sendShareInfo("wala",result.getRetval().getId(), result.getRetval().getMemberid(), null);
		}else{
			return showJsonError(model, result.getMsg());
		}
		model.put("comment", result.getRetval());
		return showJsonSuccess(model, moderate);
	}
	@RequestMapping("/newloadRightMicroModerTable.xhtml")
	public String newloadRightMicroModerTable(String moderate, Integer count, String isReply, ModelMap model){
		List<Comment> commentList = commentService.getModeratorDetailList(moderate, false, 0, count);
		model.put("moderate", moderate);
		model.put("isReply", isReply == null ? false : isReply);
		Map<Long, Comment> tranferCommentMap = new HashMap<Long, Comment>();// 转载评论
		for (Comment comment : commentList) {
			if (comment.getTransferid() != null) {
				Comment c = commentService.getCommentById(comment.getTransferid());
				if (c != null && StringUtils.isNotBlank(c.getBody())) {
					tranferCommentMap.put(c.getId(), c);
				}
			}
		}
		model.put("commentList", commentList);
		addCacheMember(model, ServiceHelper.getMemberIdListFromBeanList(commentList));
		model.put("tranferCommentMap", tranferCommentMap);
		return "wala/replyShow.vm";
	}
}
