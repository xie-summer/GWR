package com.gewara.web.action.ajax;

import java.sql.Timestamp;
import java.util.ArrayList;
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

import com.gewara.command.CommentCommand;
import com.gewara.helper.sys.RelateClassHelper;
import com.gewara.model.BaseObject;
import com.gewara.model.user.Member;
import com.gewara.support.ServiceHelper;
import com.gewara.untrans.CommentService;
import com.gewara.untrans.CommonService;
import com.gewara.util.BeanUtil;
import com.gewara.util.DateUtil;
import com.gewara.util.MarkHelper;
import com.gewara.util.WebUtils;
import com.gewara.web.action.AnnotationController;
import com.gewara.web.util.PageUtil;
import com.gewara.xmlbind.bbs.Comment;

@Controller
public class QryCommentAjaxController extends AnnotationController {

	@Autowired@Qualifier("commentService")
	private CommentService commentService;
	@Autowired@Qualifier("commonService")
	private CommonService commonService;
	
	@RequestMapping("/ajax/common/qryTopComment.xhtml")
	public String qryTopComment(String tag, Long relatedid, ModelMap model){
		Map<String, Integer> commentMap = commonService.getCommentCount();
		String key = relatedid + tag;
		if(commentMap.get(key) != null){
			BaseObject object = daoService.getObject(RelateClassHelper.getRelateClazz(tag), relatedid);
			if(object != null){
				List<Comment> commentList = commentService.getCommentListByRelatedId(tag, null,relatedid, "addtime", 0, 3);
				model.put("commentList", commentList);
				model.put("object", object);
				model.put("tag", tag);
				model.put("relatedid", relatedid);
				addCacheMember(model, ServiceHelper.getMemberIdListFromBeanList(commentList));
			}
		}
		return "wala/showTopWala.vm";
	}
	
	@RequestMapping("/ajax/common/qryComment.xhtml")
	public String getCommentList(@CookieValue(value = LOGIN_COOKIE_NAME, required = false)String sessid, 
			HttpServletRequest request,CommentCommand command, ModelMap model){
		int firstPre = command.getPageNumber() * command.getMaxCount();
		List<Comment> commentList = new ArrayList<Comment>();
		if(StringUtils.isNotBlank(command.tag) && command.relatedid != null){
			Object object = relateService.getRelatedObject(command.tag, command.relatedid);
			if(object == null) return showJsonError(model, "该数据不存在或被删除！");
			if(StringUtils.equals("hot", command.order)){
				if(firstPre >= 20){
					model.put("commentCount", 0);
					return showJsonError(model, "没有更多哇啦！");	
				}
				commentList = commentService.getHotCommentListByRelatedId(command.tag,"", command.relatedid, null, null,firstPre, command.getMaxCount());
				if(commentList.size() < command.getMaxCount() || (commentList.size() == command.getMaxCount() && firstPre == 20 - command.getMaxCount())){
					model.put("commentCount", 0);
				}else{
					model.put("commentCount", 20);
				}
			}else{
				commentList = commentService.getCommentListByRelatedId(command.tag,command.getFlag(), command.relatedid, command.order, firstPre, command.getMaxCount());
			}
		}else if(StringUtils.isNotBlank(command.title)){
			if(StringUtils.equals("hot", command.order)){
				Timestamp startTime = null;
				Timestamp endTime = null;
				if(DateUtil.isValidDate(command.getStartTime()) && DateUtil.isValidDate(command.getEndTime())){
					startTime = DateUtil.parseTimestamp(command.getStartTime());
					endTime = DateUtil.parseTimestamp(command.getEndTime());
				}else{
					endTime = DateUtil.getCurFullTimestamp();
					startTime = DateUtil.addDay(endTime, -30);
				}
				commentList = commentService.getHotCommentListByTopic(command.title, startTime, endTime, null, firstPre, command.getMaxCount());
			}else{
				commentList = commentService.searchCommentList(command.title, CommentCommand.TYPE_MODERATOR, firstPre, command.getMaxCount());
			}
		}else if(command.hasLongWala()){
			commentList = commentService.getLongCommentList(command.tag, command.relatedid, null, firstPre, command.getMaxCount());
		}
		if(command.hasPages() || command.hasFloor() || command.hasCount()){
			int count = 0;
			if(StringUtils.isNotBlank(command.tag) && command.relatedid != null){
				count = commentService.getCommentCountByRelatedId(command.tag,command.getFlag(), command.relatedid);
			}else if(StringUtils.isNotBlank(command.title)){
				count = commentService.searchCommentCount(command.title, CommentCommand.TYPE_MODERATOR);
			}else if(command.hasLongWala()){
				count = commentService.getLongCommentCount(command.tag, command.relatedid, null);
			}
			if(command.hasPages()){
				PageUtil pageUtil = new PageUtil(count, command.getMaxCount(), command.getPageNumber(), "ajax/common/qryComment.xhtml", true, true);
				Map params = requestMap(command);
				pageUtil.initPageInfo(params);
				model.put("pageUtil", pageUtil);
			}
			if(command.hasFloor() || command.hasCount()){
				model.put("commentCount", count);
			}
		}
		if(commentList.isEmpty() && command.getPageNumber() > 0) return showJsonError(model, "没有更多哇啦！");
		model.put("commentList", commentList);
		List<Long> cIds = BeanUtil.getBeanPropertyList(commentList, Long.class, "transferid", true);
		if(!cIds.isEmpty()){
			List<Comment> tranferCommentList = commentService.getCommentByIdList(cIds);
			Map<Long, Comment> tranferCommentMap = BeanUtil.beanListToMap(tranferCommentList, "id");
			model.put("tranferCommentMap", tranferCommentMap);
			addCacheMember(model, ServiceHelper.getMemberIdListFromBeanList(tranferCommentMap.values()));
		}
		if(command.hasMarks()){
			model.put("markHelper", new MarkHelper());
		}
		addCacheMember(model, ServiceHelper.getMemberIdListFromBeanList(commentList));
		Member member = loginService.getLogonMemberBySessid(WebUtils.getRemoteIp(request), sessid);
		if(member != null){
			addCacheMember(model, member.getId());
			model.put("logonMember", member);
		}
		model.put("command", command);
		if(command.getIsWide() != null){
			return "wala/wide_wala.vm";
		}
		return "wala/wala.vm";
	}
	
	private Map requestMap(CommentCommand command){
		Map parmas = new HashMap();
		if(StringUtils.isNotBlank(command.tag)){
			parmas.put("tag", command.tag);
		}
		if(command.relatedid != null){
			parmas.put("relatedid", command.relatedid);
		}
		if(StringUtils.isNotBlank(command.title)){
			parmas.put("title", command.title);
		}
		if(StringUtils.isNotBlank(command.getIsLongWala())){
			parmas.put("isLongWala", command.getIsLongWala());
		}
		if(StringUtils.isNotBlank(command.getHasMarks())){
			parmas.put("hasMarks", command.getHasMarks());
		}
		if(StringUtils.isNotBlank(command.getPages())){
			parmas.put("pages", command.getPages());
		}
		if(StringUtils.isNotBlank(command.getIsPic())){
			parmas.put("isPic", command.getIsPic());
		}
		if(StringUtils.isNotBlank(command.getIsVideo())){
			parmas.put("isVideo", command.getIsVideo());
		}
		return parmas;
	}
}