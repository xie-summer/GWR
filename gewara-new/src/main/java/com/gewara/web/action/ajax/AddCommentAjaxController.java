package com.gewara.web.action.ajax;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.RequestMapping;

import com.gewara.command.CommentCommand;
import com.gewara.constant.CookieConstant;
import com.gewara.constant.ExpGrade;
import com.gewara.constant.TagConstant;
import com.gewara.json.MemberStats;
import com.gewara.model.BaseObject;
import com.gewara.model.user.Member;
import com.gewara.service.OperationService;
import com.gewara.service.bbs.BlogService;
import com.gewara.service.bbs.MarkService;
import com.gewara.support.ErrorCode;
import com.gewara.support.ServiceHelper;
import com.gewara.untrans.CommentService;
import com.gewara.untrans.MemberCountService;
import com.gewara.untrans.ShareService;
import com.gewara.util.BeanUtil;
import com.gewara.util.JsonUtils;
import com.gewara.util.StringUtil;
import com.gewara.util.VmUtils;
import com.gewara.util.WebUtils;
import com.gewara.web.action.AnnotationController;
import com.gewara.xmlbind.bbs.Comment;


@Controller
public class AddCommentAjaxController extends AnnotationController {

	@Autowired@Qualifier("blogService")
	private BlogService blogService;
	
	@Autowired@Qualifier("commentService")
	private CommentService commentService;
	
	@Autowired@Qualifier("memberCountService")
	private MemberCountService memberCountService;
	
	@Autowired@Qualifier("shareService")
	private ShareService shareService;
	
	@Autowired@Qualifier("operationService")
	private OperationService operationService;

	@Autowired@Qualifier("markService")
	private MarkService markService;
	
	public static final List<String> MARK_LIST = Arrays.asList("screenmark", "airqualitymark", "attitudemark", "feelingmark", "environmentmark",
			"audiomark", "programmark", "generalmark", "guidemark", "storymark", "songmark", "spacemark", "promark", "interactivemark", "pricemark",
			"servicemark", "musicmark", "fieldmark", "performmark", "foodmark");
	
	private static Map<String, String> MEMBER_TAG_MAP = new HashMap<String, String>();
	static{
		MEMBER_TAG_MAP.put(TagConstant.TAG_CINEMA, TagConstant.TAG_MEMBER_CINEMA);
		MEMBER_TAG_MAP.put(TagConstant.TAG_SPORT, TagConstant.TAG_MEMBER_SPORT);
		MEMBER_TAG_MAP.put(TagConstant.TAG_THEATRE, TagConstant.TAG_MEMBER_THEATRE);
		MEMBER_TAG_MAP.put(TagConstant.TAG_GYM, TagConstant.TAG_MEMBER_GYM);
	}
	@RequestMapping("/ajax/common/saveComent.xhtml")
	public String addComment(@CookieValue(value = LOGIN_COOKIE_NAME, required = false)String sessid, 
			@CookieValue(value=CookieConstant.MEMBER_POINT, required=false) String pointxy,
			HttpServletRequest request, CommentCommand command, ModelMap model) throws Exception {
		String ip = WebUtils.getRemoteIp(request);
		Member member = loginService.getLogonMemberBySessid(ip, sessid);
		if(member == null) return showJsonError_NOT_LOGIN(model);
		if (blogService.isBlackMember(member.getId())){
			return showJsonError(model, "你在黑名单中，暂不能发表哇啦，如有疑问请联系格瓦拉客服！");
		}
		Integer eggs = blogService.isBadEgg(member);
		if (eggs == 1) {
			return showJsonError(model, "您的发表体验机会已用完，完善账户信息就可以使用哇啦、站内信、活动、知道、论坛的所有功能啦！");
		}else if (eggs ==7) {
			return showJsonError(model, "您的发表体验机会已用完，确认您的邮件地址就可以使用哇啦、站内信、活动、知道、论坛的所有功能啦！");
		}else if (eggs == 51) {
			return showJsonError(model, "操作过于频繁！");
		}else if (eggs == 0) {
			return showJsonError(model, "非法请求！");
		}
		model.put("logonMember", member);
		//赋值经纬度
		command.pointxy = pointxy;
		ErrorCode<Comment> result = null;
		if(command.relatedid != null){
			result = addRelatedidTemplate(member, command, WebUtils.getIpAndPort(ip, request));
		}else{
			result = addTopicComment(member, command, WebUtils.getIpAndPort(ip, request));
		}
		if(!result.isSuccess()) return showJsonError(model, result.getMsg());
		List<Comment> commentList = new ArrayList<Comment>();
		commentList.add(result.getRetval());
		model.put("commentList", commentList);
		List<Long> cIds = BeanUtil.getBeanPropertyList(commentList, Long.class, "transferid", true);// 转载评论
		if(!cIds.isEmpty()){
			List<Comment> tranferCommentList = commentService.getCommentByIdList(cIds);
			Map<Long, Comment> tranferCommentMap = BeanUtil.beanListToMap(tranferCommentList, "id");
			model.put("tranferCommentMap", tranferCommentMap);
			addCacheMember(model, ServiceHelper.getMemberIdListFromBeanList(tranferCommentMap.values()));
		}
		addCacheMember(model, ServiceHelper.getMemberIdListFromBeanList(commentList));
		if(command.hasFloor()){
			int count = 0;
			if(StringUtils.isNotBlank(command.tag) && command.relatedid != null){
				count = commentService.getCommentCountByRelatedId(command.tag, command.relatedid);
			}else if(StringUtils.isNotBlank(command.title)){
				count = commentService.searchCommentCount(command.title, CommentCommand.TYPE_MODERATOR);
			}else if(command.hasLongWala()){
				count = commentService.getLongCommentCount(command.tag, command.relatedid, null);
			}
			model.put("commentCount", count);
		}
		model.put("command", command);
		if(command.hasJson()) return showJsonSuccess(model);
		if(StringUtils.equals(command.tag, "movie")){
			return "wala/wide_saveWala.vm";
		}
		return "wala/saveWala.vm";
	}
	
	private ErrorCode<Comment> addTopicComment(Member member, CommentCommand command, String ip){
		if (StringUtils.isBlank(command.micrbody)) return ErrorCode.getFailure("哇啦内容不能为空！");
		String topic = "#" + command.title + "#";
		if(StringUtils.isNotBlank(command.title) && !StringUtils.contains(command.getMicrbody(), topic)){
			if(StringUtils.isBlank(command.micrbody)) return ErrorCode.getFailure("话题内容不能为空！");
			command.micrbody = topic + command.micrbody;
		}else{
			String body = StringUtils.substringBetween(command.micrbody, "#", "#");
			if(StringUtils.isBlank(body)) return ErrorCode.getFailure("话题内容不能为空！");
			if(StringUtils.length(body)>60){
				return ErrorCode.getFailure("话题内容长度不能超出60个字符！");
			}
		}
		if (StringUtils.length(command.micrbody) > 140){
			return ErrorCode.getFailure("哇啦内容长度不能超出140个字符！");
		}
		
		if(StringUtils.isBlank(command.tag)) command.tag = TagConstant.TAG_TOPIC;
		command.micrbody = StringUtil.getHtmlText(command.micrbody);
		command.micrbody += addLink(command.link);
		if(StringUtils.isNotBlank(command.bodypic)){
			command.micrbody+="<img src=\""+command.bodypic+"\"/>";
		}
		command.micrbody += addVideo(command.video);
		String pointx = "", pointy = "";
		if(StringUtils.isNotBlank(command.pointxy)){
			List<String> pointList = processPointxy(command.pointxy);
			if(pointList.size() == 2){
				pointx = pointList.get(0);
				pointx = pointList.get(1);
			}
		}
		
		ErrorCode<Comment> result = commentService.addComment(member, command.tag, command.relatedid, command.micrbody, command.link, false,
				command.generalmark, pointx, pointy, ip);
		if(result.isSuccess()){
			memberCountService.updateMemberCount(member.getId(), MemberStats.FIELD_COMMENTCOUNT, 1, true);
			shareService.sendShareInfo("wala",result.getRetval().getId(), result.getRetval().getMemberid(), null);
			Comment comment = result.getRetval();
			Map<Long, List<String>> videosMap = new HashMap<Long, List<String>>();// 存储点评视频地址
			if (StringUtils.isNotBlank(comment.getBody())) {
				List<String> videos = WebUtils.getVideos(comment.getBody());
				videosMap.put(comment.getId(), videos);
			}
		}
		return result;
	}
	
	private ErrorCode<Comment> addRelatedidTemplate(Member member, CommentCommand command, String ip) {
		Object obj = relateService.getRelatedObject(command.tag, command.relatedid);
		if(obj == null) return ErrorCode.getFailure("哇啦关联的该对象实体不存在或被删除！");
		if((command.generalmark == null || command.generalmark == 0) && StringUtils.isBlank(command.micrbody) && StringUtils.isBlank(command.marks)) 
			return ErrorCode.getFailure("请评分或填写评论!"); 
		String name = (String) BeanUtil.get(obj, "name");
		if (StringUtils.isNotBlank(name)) {
			command.micrbody= addModeratorTitle(name) + command.micrbody;
		}
		String opkey = OperationService.TAG_MEMBERMARK + member.getId();
		if (!operationService.isAllowOperation(opkey, 30)) {
			return ErrorCode.getFailure("发的这么快，手有点酸了吧，休息一下再继续吧！");
		}
		Map<String, String> markMap = WebUtils.parseQueryStr(command.marks, "utf-8");
		if (StringUtils.isNotBlank(command.marks)){
			Map<String, Integer> memberMarkMap = new HashMap<String, Integer>();
			Set<String> markKeySet = markMap.keySet();
			for (String markname : markKeySet) {
				String markvalue = markMap.get(markname);
				if(MARK_LIST.contains(markname)&& markvalue.matches("\\d+")){
					int markValue = Integer.valueOf(markvalue);
					if(markValue > 0 && markValue <=10)
						memberMarkMap.put(markname, Integer.valueOf(markvalue));
				}
			}
			markService.saveOrUpdateMemberMarkMap(command.tag, command.relatedid, member, memberMarkMap);
		}else if (command.generalmark != null && command.generalmark != 0) {
			if (command.generalmark < 0 || command.generalmark > 10)
				return ErrorCode.getFailure("评分错误异常！");
			String markname = "generalmark";
			markService.saveOrUpdateMemberMark(command.tag, command.relatedid, markname, command.generalmark, member);
		}
		operationService.updateOperation(opkey, 30);
		String pointx = (String) BeanUtil.get(obj, "pointx"),pointy = (String) BeanUtil.get(obj, "pointy");
		if((StringUtils.isBlank(pointx)|| StringUtils.isBlank(pointy)) && StringUtils.isNotBlank(command.pointxy)){
			List<String> pointList = Arrays.asList(StringUtils.split(command.pointxy, ":"));
			if(pointList.size() == 2){
				pointx = pointList.get(0);
				pointy = pointList.get(1);
			}
		}
		if (StringUtils.isNotBlank(command.micrbody)) {// 发表影评(commentText不为空)
			command.micrbody = StringUtil.getHtmlText(command.micrbody);
			if(StringUtils.isNotBlank(command.bodypic)) command.micrbody += "<img src=\""+command.bodypic+"\"/>";
			if(StringUtils.length(command.micrbody) > 2980) return ErrorCode.getFailure("内容过长！");
			ErrorCode<Comment> result = null;
			String generalmarks = markMap.get("generalmark");
			if (StringUtils.isNotBlank(command.marks) && StringUtils.isNotBlank(generalmarks)){
				int generalmark1 = Integer.parseInt(generalmarks);
				if (generalmark1 <= 0 || generalmark1 > 10)	result = commentService.addComment(member, command.tag, command.relatedid, command.micrbody, null, false, pointx, pointy, ip);
				else result = commentService.addComment(member, command.tag, command.relatedid, command.micrbody, null, false, generalmark1, pointx, pointy, ip);
			} else if (null != command.generalmark && command.generalmark != 0) {
				if (command.generalmark < 0 || command.generalmark > 10)
					return ErrorCode.getFailure("评分错误异常！");
				result = commentService.addComment(member, command.tag, command.relatedid, command.micrbody, null, false, command.generalmark, pointx, pointy, ip);
			} else {
				result = commentService.addComment(member, command.tag, command.relatedid, command.micrbody, null, false, pointx, pointy, ip);
			}
			if(!result.isSuccess()) return ErrorCode.getFailure(result.getMsg());
			shareService.sendShareInfo("wala", result.getRetval().getId(), result.getRetval().getMemberid(), null);
			if (StringUtils.isBlank(command.marks)){
				if((obj instanceof BaseObject)) daoService.saveObject((BaseObject)obj);
				memberService.addExpForMember(member.getId(), ExpGrade.EXP_COMMENT_ADD_COMMON);
				memberCountService.updateMemberCount(member.getId(), MemberStats.FIELD_COMMENTCOUNT, 1, true);
			}
			//添加哇啦
			if(command.hasTag(TagConstant.TAG_CINEMA, TagConstant.TAG_SPORT, TagConstant.TAG_THEATRE, TagConstant.TAG_GYM)){
				memberCountService.updateMemberCount(member.getId(), MemberStats.FIELD_COMMENTCOUNT, 1, true);
				String linkStr = "评论 #"+name+"#";
				if(StringUtils.isNotBlank(command.bodypic)) linkStr += "<img src=\""+command.bodypic+"\"/>";
				Map otherinfoMap = new HashMap();
				otherinfoMap.put("content", command.micrbody);
				Long rid = (Long)BeanUtil.get(obj, "id");
				otherinfoMap.put("id", rid);
				Integer gmark = VmUtils.getSingleMarkStar(relateService.getRelatedObject(command.tag, rid), "general") ;
				otherinfoMap.put("gmark1", gmark/10);
				otherinfoMap.put("gmark2", gmark%10);
				String otherinfo = JsonUtils.writeObjectToJson(otherinfoMap);
				String memberTag = MEMBER_TAG_MAP.get(command.getTag());
				if(StringUtils.isNotBlank(memberTag)){
					ErrorCode<Comment> ec = commentService.addMicroComment(member, MEMBER_TAG_MAP.get(command.getTag()), rid, linkStr, "", null, null, true, null, otherinfo,null,null, ip, null);
					if(ec.isSuccess()){
						shareService.sendShareInfo("wala",ec.getRetval().getId(), ec.getRetval().getMemberid(), null);
					}
				}
			}
			return result;
		}
		return ErrorCode.getSuccess("发表评分成功！");
	}

	private String addLink(String link){
		if(StringUtils.isBlank(link)) return "";
		String tmp = "";
		if (link.startsWith("http://")){
			tmp = "<a rel=\"nofollow\" href=\"" + link + "\" target=\"_blank\" rel=\"nofollow\">" + "链接地址" + "</a>";
		}else{
			tmp = "<a href=\"http://" + link + "\" target=\"_blank\" rel=\"nofollow\">" + "链接地址" + "</a>";
		}
		return tmp;
	}
	
	private String addModeratorTitle(String name) {
		return "#" + name + "#";
	}
	
	private List<String> processPointxy(String pointxy){
		if(StringUtils.isBlank(pointxy)) return new ArrayList<String>();
		return Arrays.asList(StringUtils.split(pointxy, ":"));
	}
	private String addVideo(String video){
		if(StringUtils.isBlank(video)) return "";
		StringBuilder sb = new StringBuilder();
		sb.append("<object classid='clsid:D27CDB6E-AE6D-11cf-96B8-444553540000' width='100%' height='200' codebase='http://download.macromedia.com/pub/shockwave/cabs/flash/swflash.cab#version=5,0,0,0'>");
		sb.append("<param name='quality' value='high' />");
		sb.append("<param name='movie' value='"+video+"'>");
		sb.append("<param name='wmode' value='transparent'/>");
		sb.append("<embed src='"+video+"' wmode='transparent' quality='high' width='100%' height='200' swLiveConnect='true' TYPE='application/x-shockwave-flash' PLUGINSPAGE='http://www.macromedia.com/go/getflashplayer'></embed></object>");
		return sb.toString();
	}
}
