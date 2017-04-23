package com.gewara.web.action.ajax;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.RequestMapping;

import com.gewara.Config;
import com.gewara.constant.CookieConstant;
import com.gewara.constant.DiaryConstant;
import com.gewara.constant.ExpGrade;
import com.gewara.constant.Status;
import com.gewara.constant.TagConstant;
import com.gewara.json.MemberSign;
import com.gewara.json.MemberStats;
import com.gewara.model.BaseObject;
import com.gewara.model.acl.GewaraUser;
import com.gewara.model.agency.Agency;
import com.gewara.model.bbs.Diary;
import com.gewara.model.content.Picture;
import com.gewara.model.content.Video;
import com.gewara.model.drama.Drama;
import com.gewara.model.drama.DramaStar;
import com.gewara.model.drama.Theatre;
import com.gewara.model.movie.Cinema;
import com.gewara.model.movie.Movie;
import com.gewara.model.sport.Sport;
import com.gewara.model.user.Member;
import com.gewara.model.user.MemberPicture;
import com.gewara.service.OperationService;
import com.gewara.service.bbs.BlogService;
import com.gewara.service.bbs.DiaryService;
import com.gewara.service.bbs.MarkService;
import com.gewara.support.ErrorCode;
import com.gewara.support.ServiceHelper;
import com.gewara.support.VelocityTemplate;
import com.gewara.untrans.CacheDataService;
import com.gewara.untrans.CommentService;
import com.gewara.untrans.MemberCountService;
import com.gewara.untrans.NosqlService;
import com.gewara.untrans.ShareService;
import com.gewara.untrans.WalaApiService;
import com.gewara.untrans.gym.SynchGymService;
import com.gewara.util.BeanUtil;
import com.gewara.util.JsonUtils;
import com.gewara.util.StringUtil;
import com.gewara.util.VmUtils;
import com.gewara.util.WebUtils;
import com.gewara.web.action.AnnotationController;
import com.gewara.web.util.PageUtil;
import com.gewara.xmlbind.activity.RemoteActivity;
import com.gewara.xmlbind.bbs.Comment;
import com.gewara.xmlbind.gym.RemoteCoach;

@Controller
public class CommentAjaxController extends AnnotationController {
	@Autowired@Qualifier("memberCountService")
	private MemberCountService memberCountService;
	@Autowired@Qualifier("markService")
	private MarkService markService;
	public void setMarkService(MarkService markService) {
		this.markService = markService;
	}
	@Autowired@Qualifier("cacheDataService")
	private CacheDataService cacheDataService;

	@Autowired@Qualifier("nosqlService")
	private NosqlService nosqlService;
	
	@Autowired@Qualifier("velocityTemplate")
	private VelocityTemplate velocityTemplate;
	
	@Autowired@Qualifier("synchGymService")
	private SynchGymService synchGymService;
	
	@Autowired@Qualifier("walaApiService")
	private WalaApiService walaApiService;
	
	
	public static final List<String> MARK_LIST = Arrays.asList("screenmark", "airqualitymark", "attitudemark", "feelingmark", "environmentmark",
			"audiomark", "programmark", "generalmark", "guidemark", "storymark", "songmark", "spacemark", "promark", "interactivemark", "pricemark",
			"servicemark", "musicmark", "fieldmark", "performmark", "foodmark");
	@Autowired@Qualifier("commentService")
	private CommentService commentService;

	public void setCommentService(CommentService commentService) {
		this.commentService = commentService;
	}
	@Autowired@Qualifier("operationService")
	private OperationService operationService;
	@Autowired@Qualifier("diaryService")
	private DiaryService diaryService;

	public void setDiaryService(DiaryService diaryService) {
		this.diaryService = diaryService;
	}

	@Autowired@Qualifier("shareService")
	private ShareService shareService;

	public void setShareService(ShareService shareService) {
		this.shareService = shareService;
	}

	@Autowired@Qualifier("blogService")
	private BlogService blogService;

	public void setBlogService(BlogService blogService) {
		this.blogService = blogService;
	}

	@Autowired@Qualifier("config")
	private Config config;

	public void setConfig(Config config) {
		this.config = config;
	}

/*	@RequestMapping("/ajax/newDiaryList.xhtml")
	public String newDiaryList(@CookieValue(value = LOGIN_COOKIE_NAME, required = false)
	String sessid, Integer pageNo, Long relatedid, String tag, String type, String area, ModelMap model, HttpServletRequest request,
			HttpServletResponse response) {
		if (StringUtils.isBlank(tag))
			tag = "movie";
		if (StringUtils.isBlank(type))
			type = "new";
		if (pageNo == null)
			pageNo = 0;
		int rowsPerPage = 10;
		int firstPerPage = pageNo * rowsPerPage;
		List<Diary> diaryList = new ArrayList<Diary>();
		boolean isDiary = false;
		String citycode = "";
		if (StringUtils.isNotBlank(area))
			citycode = WebUtils.getAndSetDefault(request, response);
		if ("new".equals(type)) {
			isDiary = true;
			diaryList = diaryService.getDiaryList(Diary.class, citycode, Diary.DIARY_TYPE_COMMENT, tag, relatedid, firstPerPage, rowsPerPage, "addtime");
		} else if ("hot".equals(type)) {
			isDiary = true;
			diaryList = diaryService.getDiaryList(Diary.class, citycode, Diary.DIARY_TYPE_COMMENT, tag, relatedid, firstPerPage, rowsPerPage,
					"flowernum");
		} else if ("friend".equals(type)) {
			isDiary = true;
			Member member = loginService.getLogonMemberBySessid(WebUtils.getRemoteIp(request), sessid);
			if (member != null) {
				model.put("logonMember", member);
				diaryList = diaryService.getFriendDiaryList(Diary.DIARY_TYPE_COMMENT, tag, relatedid, member.getId(), firstPerPage, rowsPerPage);
			}
		}
		if (isDiary) {
			Map mapdiary = new HashMap();
			for (Diary d : diaryList) {
				mapdiary.put(d.getId(), blogService.getDiaryBody(d.getId()));
			}
			model.put("diaryList", diaryList);
			model.put("diaryBody", mapdiary);
		}
		model.put("type", type);
		addCacheMember(model, ServiceHelper.getMemberIdListFromBeanList(diaryList));
		return "common/XXXnewDiaryList.vm";
	}
*/
	@RequestMapping("/ajax/new_DiaryList.xhtml")
	public String transDiaryList(@CookieValue(value = LOGIN_COOKIE_NAME, required = false)
	String sessid, Integer pageNo, Long relatedid, String tag, String type, String area, ModelMap model, HttpServletRequest request,
			HttpServletResponse response) {
		if (StringUtils.isBlank(tag))
			tag = "movie";
		if (StringUtils.isBlank(type))
			type = "new";
		if (pageNo == null)
			pageNo = 0;
		int rowsPerPage = 5;
		int firstPerPage = pageNo * rowsPerPage;
		List<Diary> diaryList = new ArrayList<Diary>();
		boolean isDiary = false;
		String citycode = "";
		if (StringUtils.isNotBlank(area))
			citycode = WebUtils.getAndSetDefault(request, response);
		if ("new".equals(type)) {
			isDiary = true;
			diaryList = diaryService.getDiaryList(Diary.class, citycode, DiaryConstant.DIARY_TYPE_COMMENT, tag, relatedid, firstPerPage, rowsPerPage, "addtime");
		} else if ("hot".equals(type)) {
			isDiary = true;
			diaryList = diaryService.getDiaryList(Diary.class, citycode, DiaryConstant.DIARY_TYPE_COMMENT, tag, relatedid, firstPerPage, rowsPerPage,
					"poohnum"); 
		} else if ("friend".equals(type)) {
			isDiary = true;
			Member member = loginService.getLogonMemberBySessid(WebUtils.getRemoteIp(request), sessid);
			if (member != null) {
				model.put("logonMember", member);
				diaryList = diaryService.getFriendDiaryList(DiaryConstant.DIARY_TYPE_COMMENT, tag, relatedid, member.getId(), firstPerPage, rowsPerPage);
			}
		}
		if (isDiary) {
			Map mapdiary = new HashMap();
			for (Diary d : diaryList) {
				mapdiary.put(d.getId(), blogService.getDiaryBody(d.getId()));
			}
			model.put("diaryList", diaryList);
			model.put("diaryBody", mapdiary);
		}
		model.put("type", type);
		model.put("tag", tag);
		addCacheMember(model, ServiceHelper.getMemberIdListFromBeanList(diaryList));
		return "common/new_diaryList.vm";
	}


	/********************************************************************************************************************
	 * 发表点评
	 * 
	 * @param tag
	 * @param relatedId
	 * @param content
	 * @return
	 *******************************************************************************************************************/
	@RequestMapping("/ajax/common/publishComment.xhtml")
	public String publishComment(@CookieValue(value = LOGIN_COOKIE_NAME, required = false)String sessid,
			 @CookieValue(value=CookieConstant.MEMBER_POINT, required=false) String pointxy, HttpServletRequest request, String tag, Long relatedid, String content, ModelMap model) {
		String ip = WebUtils.getRemoteIp(request);
		Member member = loginService.getLogonMemberBySessid(ip, sessid);
		if (member == null)
			return showJsonError(model, "请先登录！");
		if (blogService.isBlackMember(member.getId()))
			return showJsonError_BLACK_LIST(model);
		Object obj = relateService.getRelatedObject(tag, relatedid);
		String name = (String) BeanUtil.get(obj, "name");
		if (StringUtils.isNotBlank(name))
			content = "#" + name + "#" + content;
		String pointx = "", pointy = "";
		if(StringUtils.isNotBlank(pointxy)){
			List<String> pointList = Arrays.asList(StringUtils.split(pointxy, ":"));
			if(pointList.size() == 2){
				pointx = pointList.get(0);
				pointy = pointList.get(1);
			}
		}
		ErrorCode<Comment> result = commentService.addComment(member, tag, relatedid, content, null, true,pointx, pointy, WebUtils.getIpAndPort(ip, request));
		if (result.isSuccess()) {
			shareService.sendShareInfo("wala", result.getRetval().getId(), result.getRetval().getMemberid(), null);
			memberCountService.updateMemberCount(member.getId(), MemberStats.FIELD_COMMENTCOUNT, 1, true);
			return showJsonSuccess(model);
		}
		return showJsonError(model, result.getMsg());
	}

	// 发表一句话影评
	@RequestMapping("/ajax/common/addComment.xhtml")
	public String sendMovieComment(@CookieValue(value = LOGIN_COOKIE_NAME, required = false)String sessid, 
			@CookieValue(value=CookieConstant.MEMBER_POINT,required=false)String pointxy, HttpServletRequest request,
			String tag, Long relatedid, String commentText, String marks, ModelMap model) {
		String result = commentListTemplate(request, sessid, tag, relatedid, commentText, null, null, false, null, marks, pointxy, model);
		if(StringUtils.isBlank(result)) return showJsonSuccess(model);
		return result;
	}

	private String addModeratorTitle(String name) {
		return "#" + name + "#";
	}

	@RequestMapping("/ajax/common/loadWalaCommentList.xhtml")
	public String loadWalaCommentListNew(@CookieValue(value = LOGIN_COOKIE_NAME, required = false)String sessid, 
			@CookieValue(value=CookieConstant.MEMBER_POINT,required=false)String pointxy, HttpServletRequest request,
			String tag, Long relatedid, String commentText, String bodypic, boolean isLongWala, Integer generalmark, Integer rows, ModelMap model) {
		String result = commentListTemplate(request, sessid, tag, relatedid, commentText, bodypic, generalmark, isLongWala, rows, null, pointxy, model);
		if (StringUtils.isBlank(result))return "movie/new_ReplyShow.vm";
		return result;
	}
	//页面右侧哇啦
	@RequestMapping("/ajax/common/loadWalaCommentListRight.xhtml")
	public String loadWalaCommentListRight(@CookieValue(value = LOGIN_COOKIE_NAME, required = false)String sessid, 
			@CookieValue(value=CookieConstant.MEMBER_POINT,required=false)String pointxy, HttpServletRequest request,
			String tag, Long relatedid, String commentText, Integer generalmark, Integer rows, ModelMap model) {
		String result = commentListTemplate(request, sessid, tag, relatedid, commentText, null, generalmark, false, rows, null, pointxy, model);
		if (StringUtils.isBlank(result))return "wala/replyShow.vm";
		return result;
	}
	
	// 发表(剧院、话剧)哇啦
	@RequestMapping("/ajax/common/addTheatreCommentList.xhtml")
	public String addTheatreCommentList(@CookieValue(value = LOGIN_COOKIE_NAME, required = false)String sessid, 
			@CookieValue(value=CookieConstant.MEMBER_POINT,required=false)String pointxy, HttpServletRequest request,
			String tag, Long relatedid, String commentText, Integer generalmark, ModelMap model) {
		String result = commentListTemplate(request, sessid, tag, relatedid, commentText, null, generalmark, false, 20, null, pointxy, model);
		if (StringUtils.isBlank(result))return "common/commonCommentList.vm";
		return result;
	}

	private String commentListTemplate(HttpServletRequest request, String sessid, String tag, Long relatedid, String commentText, String bodypic, 
			Integer generalmark, boolean isLongWala, Integer rows,String marks, String pointxy, ModelMap model) {
		String ip = WebUtils.getRemoteIp(request);
		Member member = loginService.getLogonMemberBySessid(ip, sessid);
		if (member == null) {
			return showJsonError_NOT_LOGIN(model);
		}
		model.put("logonMember", member);
		if (blogService.isBlackMember(member.getId()))
			return showJsonError_BLACK_LIST(model);
		Object obj = relateService.getRelatedObject(tag, relatedid);
		if((generalmark == null || generalmark == 0) && StringUtils.isBlank(commentText) && StringUtils.isBlank(marks)) 
			return showJsonError(model, "请评分或填写评论!"); 
		if (obj == null)
			return showJsonError(model, "哇啦关联的该对象实体不存在或被删除！");
		String moderator = "";
		String name = (String) BeanUtil.get(obj, "name");
		if (StringUtils.isNotBlank(name)) {
			moderator = addModeratorTitle(name);
			model.put("tagName", name);
		}
		String opkey = OperationService.TAG_MEMBERMARK + member.getId();
		if (!operationService.isAllowOperation(opkey, 30)) {
			return showJsonError(model, "发的这么快，手有点酸了吧，休息一下再继续吧！");
		}
		Map<String, String> markMap = WebUtils.parseQueryStr(marks, "utf-8");
		if (StringUtils.isNotBlank(marks)){
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
			markService.saveOrUpdateMemberMarkMap(tag, relatedid, member, memberMarkMap);
		}else if (generalmark != null && generalmark != 0) {
			if (generalmark < 0 || generalmark > 10)
				return show404(model, "评分错误异常！");
			String markname = "generalmark";
			markService.saveOrUpdateMemberMark(tag, relatedid, markname, generalmark, member);
		}
		operationService.updateOperation(opkey, 30);
		String pointx = (String) BeanUtil.get(obj, "pointx"),pointy = (String) BeanUtil.get(obj, "pointy");
		if((StringUtils.isBlank(pointx)|| StringUtils.isBlank(pointy)) && StringUtils.isNotBlank(pointxy)){
			List<String> pointList = Arrays.asList(StringUtils.split(pointxy, ":"));
			if(pointList.size() == 2){
				pointx = pointList.get(0);
				pointy = pointList.get(1);
			}
		}
		if (StringUtils.isNotBlank(commentText)) {// 发表影评(commentText不为空)
			commentText = StringUtil.getHtmlText(commentText);
			if(StringUtils.isNotBlank(bodypic)) commentText += "<img src=\""+bodypic+"\"/>";
			if(StringUtils.length(commentText) > 2980) return show404(model, "内容过长！");
			ErrorCode<Comment> result = null;
			String generalmarks = markMap.get("generalmark");
			if (StringUtils.isNotBlank(marks) && StringUtils.isNotBlank(generalmarks)){
				int generalmark1 = Integer.parseInt(generalmarks);
				if (generalmark1 <= 0 || generalmark1 > 10)	result = commentService.addComment(member, tag, relatedid, moderator + commentText, null, false, pointx, pointy, WebUtils.getIpAndPort(ip, request));
				else result = commentService.addComment(member, tag, relatedid, moderator + commentText, null, false, generalmark1, pointx, pointy, WebUtils.getIpAndPort(ip, request));
			} else if (null != generalmark && generalmark != 0) {
				if (generalmark < 0 || generalmark > 10)
					return show404(model, "评分错误异常！");
				result = commentService.addComment(member, tag, relatedid, moderator + commentText, null, false, generalmark, pointx, pointy, WebUtils.getIpAndPort(ip, request));
			} else {
				result = commentService.addComment(member, tag, relatedid, moderator + commentText, null, false, pointx, pointy, WebUtils.getIpAndPort(ip, request));
			}
			if (result.isSuccess()) {
				if(!isLongWala){
					shareService.sendShareInfo("wala", result.getRetval().getId(), result.getRetval().getMemberid(), null);
				}else{
					Integer commentCount = commentService.getLongCommentCount(tag, relatedid, null); 
					model.put("commentCount", commentCount);
				}
				if (StringUtils.isBlank(marks)){
					if (rows == null)rows = 4;
					List<Comment> commentList = null;
					if(!isLongWala){
						commentList = commentService.getCommentListByRelatedId(tag,null,relatedid, null, 0, rows);
					}else{
						commentList = commentService.getLongCommentList(tag, relatedid, null, 0, rows);
					}
					List<Long> cIds = BeanUtil.getBeanPropertyList(commentList, Long.class, "transferid", true);// 转载评论
					if(!cIds.isEmpty()){
						List<Comment> tranferCommentList = commentService.getCommentByIdList(cIds);
						Map<Long, Comment> tranferCommentMap = BeanUtil.beanListToMap(tranferCommentList, "id");
						model.put("tranferCommentMap", tranferCommentMap);
						addCacheMember(model, ServiceHelper.getMemberIdListFromBeanList(tranferCommentMap.values()));
					}
					int commnetCount = commentService.getCommentCountByRelatedId(tag, relatedid);
					model.put("commentList", commentList);
					addCacheMember(model, ServiceHelper.getMemberIdListFromBeanList(commentList));
					model.put("commnetCount", commnetCount);
					if(!(obj instanceof RemoteActivity)) daoService.saveObject((BaseObject)obj);
					memberService.addExpForMember(member.getId(), ExpGrade.EXP_COMMENT_ADD_COMMON);
					memberCountService.updateMemberCount(member.getId(), MemberStats.FIELD_COMMENTCOUNT, 1, true);
				}
				//添加哇啦
				if(StringUtils.equals(tag, TagConstant.TAG_CINEMA) || StringUtils.equals(tag, TagConstant.TAG_SPORT) || StringUtils.equals(tag, TagConstant.TAG_THEATRE) || StringUtils.equals(tag, TagConstant.TAG_GYM)){
					memberCountService.updateMemberCount(member.getId(), MemberStats.FIELD_COMMENTCOUNT, 1, true);
					String linkStr = "评论 #"+name+"#";
					Map otherinfoMap = new HashMap();
					otherinfoMap.put("content", commentText);
					Long rid = (Long)BeanUtil.get(obj, "id");
					otherinfoMap.put("id", rid);
					Integer gmark = VmUtils.getSingleMarkStar(relateService.getRelatedObject(tag, rid), "general") ;
					otherinfoMap.put("gmark1", gmark/10);
					otherinfoMap.put("gmark2", gmark%10);
					String otherinfo = JsonUtils.writeObjectToJson(otherinfoMap);
					ErrorCode<Comment> ec=null;
					if(StringUtils.equals(tag, TagConstant.TAG_CINEMA)){
						ec = commentService.addMicroComment(member, TagConstant.TAG_MEMBER_CINEMA, rid, linkStr, "", null, null, true, null, otherinfo,null,null, WebUtils.getIpAndPort(ip, request), null);
					}else if(StringUtils.equals(tag, TagConstant.TAG_SPORT)){
						ec = commentService.addMicroComment(member, TagConstant.TAG_MEMBER_SPORT, rid, linkStr, "", null, null, true, null, otherinfo,null,null, WebUtils.getIpAndPort(ip, request), null);
					}else if(StringUtils.equals(tag, TagConstant.TAG_THEATRE)){
						ec = commentService.addMicroComment(member, TagConstant.TAG_MEMBER_THEATRE, rid, linkStr, "", null, null, true, null, otherinfo,null,null, WebUtils.getIpAndPort(ip, request), null);
					}else if(StringUtils.equals(tag, TagConstant.TAG_GYM)){
						ec = commentService.addMicroComment(member, TagConstant.TAG_MEMBER_GYM, rid, linkStr, "", null, null, true, null, otherinfo,null,null, WebUtils.getIpAndPort(ip, request), null);
					}
					if(ec.isSuccess()){
						shareService.sendShareInfo("wala",ec.getRetval().getId(), ec.getRetval().getMemberid(), null);
					}
				}
			} else {
				return showJsonError(model, result.getMsg());
			}
		}
		return null;
	}

	// 图片发表哇啦(电影院、剧院、电影、话剧)
	@RequestMapping("/ajax/comment/savePictureMicroBlog.xhtml")
	public String saveMicroBlog(@CookieValue(value = LOGIN_COOKIE_NAME, required = false)
	String sessid, HttpServletRequest request, ModelMap model, Long relatedid, String tag, String body, String type, String ttag) {
		String ip = WebUtils.getRemoteIp(request);
		Member member = loginService.getLogonMemberBySessid(ip, sessid);
		if (member == null)
			return showJsonError(model, "你请先登录！");
		if (blogService.isBlackMember(member.getId()))
			return showJsonError_BLACK_LIST(model);
		if (StringUtils.isBlank(body))
			return showJsonError(model, "内容不能为空！");
		body = StringUtil.getHtmlText(body);
		String relatedName = "";
		String tmp = "";
		Comment newComment = null;
		String pointx = null;
		String pointy = null;
		MemberSign sign = nosqlService.getMemberSign(member.getId());
		if(sign != null){
			pointx = Double.toString(sign.getBpointx());
			pointy = Double.toString(sign.getBpointy());
		}
		if (type.equals("apic")) {// 管理员图片信息
			Member member2 = null;
			Picture picture = daoService.getObject(Picture.class, relatedid);
			if (picture == null)
				return showJsonError(model, "数据出错！");
				if(picture.hasMemberType(GewaraUser.USER_TYPE_MEMBER)){
					member2 = daoService.getObject(Member.class, picture.getMemberid());
				}else{
					member2 = daoService.getObject(Member.class, 1L);// 视频后台上传者的memberid是管理员User的ID，现转换为gewara用户
				}
			if (StringUtils.equals(ttag, TagConstant.TAG_MOVIE)) {
				Movie movie = daoService.getObject(Movie.class, picture.getRelatedid());
				relatedName = movie.getName();
				tmp = "movie/movie";
			} else if (StringUtils.equals(ttag, TagConstant.TAG_CINEMA)) {// 影院图片发哇啦
				Cinema cinema = daoService.getObject(Cinema.class, picture.getRelatedid());
				relatedName = cinema.getName();
				tmp = "cinema/cinema";
			} else if (StringUtils.equals(ttag, TagConstant.TAG_DRAMA)) {
				Drama drama = daoService.getObject(Drama.class, picture.getRelatedid());
				relatedName = drama.getName();
				tmp = "drama/drama";
			} else if (StringUtils.equals(ttag, TagConstant.TAG_THEATRE)) {// 剧院图片发哇啦
				Theatre theatre = daoService.getObject(Theatre.class, picture.getRelatedid());
				relatedName = theatre.getName();
				tmp = "theatre/theatre";
			} else if (StringUtils.equals(ttag, TagConstant.TAG_DRAMASTAR)) {// 明星发哇啦
				DramaStar dramaStar = daoService.getObject(DramaStar.class, picture.getRelatedid());
				relatedName = dramaStar.getName();
				tmp = "drama/star/star";
			} else if (StringUtils.equals(ttag, TagConstant.TAG_SPORT)) {// 运动发哇啦
				Sport sport = daoService.getObject(Sport.class, picture.getRelatedid());
				relatedName = sport.getName();
				tmp = "sport/sport";
			} else if (StringUtils.equals(ttag, "gymcoach")){// 教练哇啦
				ErrorCode<RemoteCoach> code = synchGymService.getRemoteCoach(picture.getRelatedid(), true);
				if(code.isSuccess()){
					RemoteCoach gymCoach = code.getRetval();
					relatedName = gymCoach.getName();
				}
				tmp = "gym/coach";
			} else if (StringUtils.equals(ttag, TagConstant.TAG_AGENCY)){
				Agency agency = daoService.getObject(Agency.class, picture.getRelatedid());
				relatedName = agency.getName();
				tmp = "sport/agency";
			}
			String aUrl = config.getBasePath() + tmp + "PictureDetail.xhtml?pid=" + picture.getId() + "&pvtype=apic";
			aUrl = "<a href=\"" + aUrl + "\" target=\"_blank\" rel=\"nofllow\">" + "链接地址" + "</a>";
			String content = "@" + member2.getNickname() + ":" + body + "#" + relatedName + "#";
			ErrorCode<Comment> result = commentService.addComment(member, tag, relatedid, content, aUrl, false, pointx, pointy, WebUtils.getIpAndPort(ip, request));
			if (!result.isSuccess()) {
				return showJsonError(model, result.getMsg());
			}
			newComment = result.getRetval();
		} else if (type.equals("mpic")) {// 网友图片信息
			MemberPicture memberPicture = daoService.getObject(MemberPicture.class, relatedid);
			if (memberPicture == null)
				return showJsonError(model, "数据出错！");
			Member member2 = daoService.getObject(Member.class, memberPicture.getMemberid());
			if (StringUtils.equals(ttag, TagConstant.TAG_MOVIE)) {// 电影发哇啦
				Movie movie = daoService.getObject(Movie.class, memberPicture.getRelatedid());
				relatedName = movie.getName();
				tmp = "movie/movie";
			} else if (StringUtils.equals(ttag, TagConstant.TAG_CINEMA)) {
				Cinema cinema = daoService.getObject(Cinema.class, memberPicture.getRelatedid());
				relatedName = cinema.getName();
				tmp = "cinema/cinema";
			} else if (StringUtils.equals(ttag, TagConstant.TAG_DRAMA)) {
				Drama drama = daoService.getObject(Drama.class, memberPicture.getRelatedid());
				relatedName = drama.getName();
				tmp = "drama/drama";
			} else if (StringUtils.equals(ttag, TagConstant.TAG_THEATRE)) {// 剧院图片发哇啦
				Theatre theatre = daoService.getObject(Theatre.class, memberPicture.getRelatedid());
				relatedName = theatre.getName();
				tmp = "theatre/theatre";
			} else if (StringUtils.equals(ttag, TagConstant.TAG_DRAMASTAR)) {// 明星发哇啦
				DramaStar dramaStar = daoService.getObject(DramaStar.class, memberPicture.getRelatedid());
				relatedName = dramaStar.getName();
				tmp = "drama/star/star";
			}
			String aUrl = config.getBasePath() + tmp + "PictureDetail.xhtml?pid=" + memberPicture.getId() + "&pvtype=mpic";
			aUrl = "<a href=\"" + aUrl + "\" target=\"_blank\" rel=\"nofollow\">" + "链接地址" + "</a>";
			String content = "@" + member2.getNickname() + ":" + body + "#" + relatedName + "#";
			ErrorCode<Comment> result = commentService.addComment(member, tag, relatedid, content, aUrl, false, pointx, pointy, WebUtils.getIpAndPort(ip, request));
			if (!result.isSuccess()) {
				return showJsonError(model, result.getMsg());
			}
			newComment = result.getRetval();
		} else if (type.equals("avideo")) {// 视频数据
			Video video = daoService.getObject(Video.class, relatedid);
			BaseObject entity = null;
			Member member2 = null;
			String nickname = "", relateName = "";
			Long rid = null;
			if (video != null) {
				if(video.hasMemberType(GewaraUser.USER_TYPE_MEMBER)){
					member2 = daoService.getObject(Member.class, video.getMemberid());
				}
				if (member2 == null)
					member2 = daoService.getObject(Member.class, 1L);// 视频后台上传者的memberid是管理员User的ID，现转换为gewara用户
				entity = (BaseObject)relateService.getRelatedObject(video.getTag(), video.getRelatedid());
				relateName = (String) BeanUtil.get(entity, "name");
				nickname = member2.getNickname();
				rid = video.getRelatedid();
			} else {
				MemberPicture memberPicture = daoService.getObject(MemberPicture.class, relatedid);
				if (memberPicture == null)
					return showJsonError(model, "数据出错！");
				member2 = daoService.getObject(Member.class, memberPicture.getMemberid());
				if (member2 == null)
					member2 = daoService.getObject(Member.class, 1L);// 视频后台上传者的memberid是管理员User的ID，现转换为gewara用户
				entity = daoService.getObject(ServiceHelper.getPalceClazz(memberPicture.getTag()), memberPicture.getRelatedid());
				relateName = (String) BeanUtil.get(entity, "name");
				nickname = member2.getNickname();
				rid = memberPicture.getRelatedid();
			}
			String aUrl = config.getBasePath() + ttag + "/" + rid + "/videolist";
			aUrl = "<a href=\"" + aUrl + "\" target=\"_blank\" rel=\"nofllow\">" + "链接地址" + "</a>";
			String content = "@" + nickname + ":" + body + "#" + relateName + "#";
			ErrorCode<Comment> result = commentService.addComment(member, tag, relatedid, content, aUrl, false, pointx, pointy, WebUtils.getIpAndPort(ip, request));
			if (!result.isSuccess()) {
				return showJsonError(model, result.getMsg());
			}
			newComment = result.getRetval();
		}

		List<Comment> commentList = commentService.getCommentList(tag, relatedid, null, "", "", 0, 30);
		Map<Long, String> contentMap = new HashMap<Long, String>();
		if(newComment != null && !commentList.contains(newComment)) commentList.add(0, newComment);
		for (Comment comment : commentList) {
			String[] aContent = comment.getBody().split(":");
			String[] bContent = aContent[1].split("#");
			contentMap.put(comment.getId(), bContent[0]);
		}
		model.put("contentMap", contentMap);
		model.put("commentList", commentList);
		addCacheMember(model, ServiceHelper.getMemberIdListFromBeanList(commentList));
		model.put("logonMember", member);
		memberService.addExpForMember(member.getId(), ExpGrade.EXP_COMMENT_ADD_COMMON);
		return "common/commonComment.vm";
	}
	
	@RequestMapping("/ajax/comment/qryMemberCommentList.xhtml")
	public String commentListByMember(Integer pageNo,int maxnum, ModelMap model, long relatedid, String tag,long memberId){
		if(pageNo == null){
			pageNo = 0;
		}
		if(maxnum > 30){
			maxnum = 30;
		}
		List<Comment> commentList = commentService.getCommentList(tag, relatedid, memberId, "", "", pageNo * maxnum, maxnum);
		model.put("commentList", commentList);
		String viewPage = velocityTemplate.parseTemplate("movie/mod_memberComment.vm", model);
		Map jsonMap = new HashMap();
		jsonMap.put("viewPage", viewPage);
		return showJsonSuccess(model, jsonMap);
	}

	@RequestMapping("/ajax/comment/qryCommentList.xhtml")
	public String commentList(@CookieValue(value = LOGIN_COOKIE_NAME, required = false)
			String sessid, Integer pageNo,int maxnum,HttpServletRequest request, ModelMap model, Long relatedid, String tag) {
		if(StringUtils.equals(tag, "picture")){
			cacheDataService.getAndSetIdsFromCachePool(Picture.class, relatedid);
			cacheDataService.getAndSetClazzKeyCount(Picture.class, relatedid);
		}
		if(pageNo == null){
			pageNo = 0;
		}
		if(maxnum > 30){
			maxnum = 30;
		}
		List<Comment> commentList = commentService.getCommentList(tag, relatedid, null, "", "", pageNo * maxnum, maxnum);
		Map<Long, String> contentMap = new HashMap<Long, String>();
		for (Comment comment : commentList) {
			String[] aContent = StringUtils.split(comment.getBody(), ":");
			if(aContent != null && aContent.length > 1){
				String[] bContent = StringUtils.split(aContent[1], "#");
				contentMap.put(comment.getId(), bContent[0]);
			}
		}
		model.put("contentMap", contentMap);
		model.put("commentList", commentList);
		addCacheMember(model, ServiceHelper.getMemberIdListFromBeanList(commentList));
		Member logonMember = loginService.getLogonMemberBySessid(WebUtils.getRemoteIp(request), sessid);
		if(logonMember != null){
			model.put("logonMember", logonMember);
			addCacheMember(model, logonMember.getId());
		}
		model.put("relatedid", relatedid);
		model.put("tag", tag);
		PageUtil pageUtil = new PageUtil(commentService.getCommentCount(tag, relatedid,null, "", ""), maxnum, pageNo, "/ajax/comment/qryCommentList.xhtml", true, true);
		Map params = new HashMap();
		params.put("relatedid", relatedid);
		params.put("tag", tag);
		params.put("maxnum",maxnum);
		pageUtil.initPageInfo(params);
		model.put("pageUtil", pageUtil);
		return "common/mod_commonComment.vm";
	}
	
	@RequestMapping("/ajax/comment/commentList.xhtml")
	public String commentList(@CookieValue(value = LOGIN_COOKIE_NAME, required = false)
			String sessid, HttpServletRequest request, ModelMap model, String type, Long relatedid, String tag) {
		if(StringUtils.equals(tag, "picture")){
			cacheDataService.getAndSetIdsFromCachePool(Picture.class, relatedid);
			cacheDataService.getAndSetClazzKeyCount(Picture.class, relatedid);
		}
		List<Comment> commentList = commentService.getCommentList(tag, relatedid, null, "", "", 0, 30);
		Map<Long, String> contentMap = new HashMap<Long, String>();
		for (Comment comment : commentList) {
			String[] aContent = StringUtils.split(comment.getBody(), ":");
			if(aContent != null && aContent.length > 1){
				String[] bContent = StringUtils.split(aContent[1], "#");
				contentMap.put(comment.getId(), bContent[0]);
			}
		}
		model.put("contentMap", contentMap);
		model.put("commentList", commentList);
		addCacheMember(model, ServiceHelper.getMemberIdListFromBeanList(commentList));
		Member logonMember = loginService.getLogonMemberBySessid(WebUtils.getRemoteIp(request), sessid);
		if(logonMember != null){
			model.put("logonMember", logonMember);
			addCacheMember(model, logonMember.getId());
		}
		if(StringUtils.equals(type, TagConstant.TAG_MOVIE) && (StringUtils.equals(tag, "picture") || StringUtils.equals(tag, "video"))){
			model.put("relatedid", relatedid);
			model.put("tag", tag);
			return "common/mod_commonComment.vm";
		}
		return "common/commonComment.vm";
	}

	@RequestMapping("/ajax/comment/deleteComment.xhtml")
	public String deleteComment(@CookieValue(value = LOGIN_COOKIE_NAME, required = false)
	String sessid, HttpServletRequest request, Long commentid, ModelMap model) {
		Member logonMember = loginService.getLogonMemberBySessid(WebUtils.getRemoteIp(request), sessid);
		if(logonMember == null) return showJsonError(model, "请先登录！");
		Comment comment = commentService.getCommentById(commentid);
		if (comment == null) return showJsonError(model, "数据有误！");
		if(!logonMember.getId().equals(comment.getMemberid())) return showJsonError(model, "不能删除别人的数据！");
		//只修改状态不做物理删除
		//commentService.deleteComment(comment.getId());
		comment.setStatus(Status.N_DELETE);
		commentService.updateComment(comment);
		walaApiService.deleteMicroReComment(comment.getId());
		dbLogger.warn("用户(id:"+logonMember.getId()+")删除哇啦:"+comment.getId()+"status:"+Status.N_DELETE);
		return showJsonSuccess(model);
	}
}
