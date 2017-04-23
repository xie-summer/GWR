package com.gewara.web.action.inner.mobile.comment;

import java.io.ByteArrayInputStream;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import com.gewara.constant.ApiConstant;
import com.gewara.constant.Status;
import com.gewara.constant.TagConstant;
import com.gewara.json.MemberSign;
import com.gewara.model.acl.GewaraUser;
import com.gewara.model.content.Picture;
import com.gewara.model.user.Member;
import com.gewara.service.bbs.BlogService;
import com.gewara.support.ErrorCode;
import com.gewara.support.ServiceHelper;
import com.gewara.untrans.GewaPicService;
import com.gewara.untrans.WalaApiService;
import com.gewara.untrans.monitor.RoleTag;
import com.gewara.util.BeanUtil;
import com.gewara.util.DateUtil;
import com.gewara.util.PictureUtil;
import com.gewara.util.StringUtil;
import com.gewara.web.action.inner.OpenApiAuth;
import com.gewara.web.action.inner.mobile.BaseOpenApiMobileController;
import com.gewara.web.filter.OpenApiMobileAuthenticationFilter;
import com.gewara.xmlbind.bbs.Comment;
import com.gewara.xmlbind.bbs.ReComment;

@Controller
public class OpenApiMobileCommentController extends BaseOpenApiMobileController{
	@Autowired@Qualifier("walaApiService")
	private WalaApiService walaApiService;
	
	@Autowired@Qualifier("blogService")
	private BlogService blogService;
	@Autowired
	@Qualifier("gewaPicService")
	private GewaPicService gewaPicService;
	
	/**
	 * 回复哇啦
	 */
	@RequestMapping("/openapi/mobile/comment/reCommentAdd.xhtml")
	public String reCommentAdd(Long commentid, String memberEncode, String body,Long replyid, ModelMap model){
		OpenApiAuth auth = OpenApiMobileAuthenticationFilter.getOpenApiAuth();
		if(commentid==null ||memberEncode==null || StringUtils.isBlank(body)) 
			return getErrorXmlView(model, ApiConstant.CODE_PARAM_ERROR, "invalid commentid,memberEncode,body");
		Member member = OpenApiMobileAuthenticationFilter.getOpenApiAuth().getMember();
		Comment comment = commentService.getCommentById(commentid);//daoService.getObject(Comment.class, commentid);
		if(comment==null) return getErrorXmlView(model, ApiConstant.CODE_PARAM_ERROR, "传递参数错误！");
		ReComment recomment = new ReComment(member.getId());
		String address = auth.getApiUser().getBriefname();
		recomment.setAddress(address);
		if(replyid == null){
			recomment.setTomemberid(comment.getMemberid());
			recomment.setTag(ReComment.TAG_COMMENT);
		}else{
			ReComment transferReComm = walaApiService.getReCommentById(replyid);//daoService.getObject(ReComment.class, replyid);
			if(transferReComm == null) return getErrorXmlView(model, ApiConstant.CODE_DATA_ERROR, "回复内容不存在！");
			recomment.setTomemberid(transferReComm.getMemberid());
			recomment.setTransferid(replyid);
			recomment.setTag(ReComment.TAG_RECOMMENT);
		}
		recomment.setBody(body);
		recomment.setRelatedid(commentid);
		String filterkey = blogService.filterContentKey(body);
		if (StringUtils.isNotBlank(filterkey)) {
			recomment.setStatus(Status.N_FILTER);
			String title = "有人发恶意评论！";
			String content = "有人恶意评论，包含过滤关键字memberId = " + member.getId() + body;
			monitorService.saveSysWarn(ReComment.class, recomment.getId(), title, content, RoleTag.bbs);
			return getErrorXmlView(model, ApiConstant.CODE_DATA_ERROR, "你发的回复内容含有敏感关键词已被屏蔽！");
		}else{
			commentService.updateCommentReplyCount(commentid, Comment.TYPE_ADDREPLY);//哇啦回复数增加
		}
		walaApiService.saveReComment(recomment);
		return getSuccessXmlView(model);
	}
	

	/**
	 * 哇啦列表
	 */
	@RequestMapping("/openapi/mobile/comment/commentList.xhtml")
	public String commentList(String tag, Long relatedid, Long mincommentid, String type, Integer from, Integer maxnum, ModelMap model, HttpServletRequest request){
		if(from == null) from = 0;
		if(maxnum == null || maxnum>100) maxnum = 10;
		List<Comment> commentList = null;
		if(StringUtils.isNotBlank(tag)){
			if(relatedid == null) return getErrorXmlView(model, ApiConstant.CODE_DATA_ERROR,"关联ID不能为空！");
			if(StringUtils.equals(type, "hot")){ //预留
				commentList = commentService.getHotCommentListByRelatedId(tag,"", relatedid, null, null, from, maxnum);
			}else {
				commentList = commentService.getCommentListByRelatedId(tag, null, relatedid, null, mincommentid, from, maxnum);
			}
		}else{
			return getErrorXmlView(model, ApiConstant.CODE_DATA_ERROR, "参数错误!");
		}
		putCommentListNode(model);
		getCommentListMap(commentList, model, request);
		return getOpenApiXmlList(model);
	}
	
	/**
	 * 发表哇啦(带图片)
	 */
	@RequestMapping("/openapi/mobile/comment/commentAdd.xhtml")
	public String commentAdd(String memberEncode,String apptype,
			String body, String tag, Long relatedid, String pic,String filetype, 
			Long transferid,Integer markvalue, ModelMap model){
		OpenApiAuth auth = OpenApiMobileAuthenticationFilter.getOpenApiAuth();
		if(memberEncode==null||body==null||tag == null)  return getErrorXmlView(model, ApiConstant.CODE_PARAM_ERROR, "传递参数错误！");
		if(StringUtils.isBlank(tag)) return getErrorXmlView(model, ApiConstant.CODE_PARAM_ERROR, "tag不能为空！");
		String path = "images/comment/";
		Member member = OpenApiMobileAuthenticationFilter.getOpenApiAuth().getMember();
		String address = auth.getApiUser().getBriefname();
		String filepath = "";
		if(markvalue != null && markvalue >10 && markvalue <= 20) {
			//兼容手机客户端电影应用,客户端*2
			markvalue = markvalue/2;
		}
		if(pic!=null && transferid == null){//转载不支持图片
			if(StringUtils.isBlank(filetype)) return getErrorXmlView(model, ApiConstant.CODE_PARAM_ERROR, "传递参数错误！");
			try {
				ByteArrayInputStream is = new ByteArrayInputStream(Hex.decodeHex(pic.toCharArray()));
				String filename = gewaPicService.saveToTempPic(is, filetype);
				if(StringUtils.isNotBlank(filename)){
					if(!PictureUtil.isValidPicType(StringUtil.getFilenameExtension(filename))) {
						return getErrorXmlView(model, ApiConstant.CODE_DATA_ERROR, "发表哇啦图片格式不合法！只支持jpg,png,gif,jpeg格式");
					}
					gewaPicService.saveTempFileToRemote(filename);
					gewaPicService.moveRemoteTempTo(member.getId(), tag, relatedid, path, filename);
					filepath = path+filename;
					body += "<img src=\""+filepath+"\"/>";
				}
			} catch (Exception e) {
				return getErrorXmlView(model, ApiConstant.CODE_PARAM_ERROR, "图片错误！");
			}
		}
		String pointx = null;
		String pointy = null;
		MemberSign sign = nosqlService.getMemberSign(member.getId());
		if(sign != null){
			pointx = Double.toString(sign.getPointx());
			pointy = Double.toString(sign.getPointy());
		}
		apptype = StringUtils.isBlank(apptype)? TagConstant.TAG_CINEMA : apptype;
		ErrorCode<Comment> result = commentService.addMicroComment(member, tag, relatedid, body, null, address, transferid, false, markvalue, null, pointx, pointy, null, apptype);
		if(result.isSuccess()) {
			if(markvalue != null){
				try {
					markService.saveOrUpdateMemberMark(tag, relatedid, "generalmark", markvalue, member); 
				}catch (Exception e) {
					return getErrorXmlView(model, ApiConstant.CODE_PARAM_ERROR, e.getMessage());
				}
			}
			if(StringUtils.equals(TagConstant.TAG_ACTIVITY, tag) && StringUtils.isNotBlank(pic) ){//手机客户端酒吧活动发表带图片的哇啦-把图片转存到关联活动下
				Picture picture = new Picture(tag,relatedid,member.getId(),filepath);
				picture.setMemberType(GewaraUser.USER_TYPE_MEMBER);
				daoService.addObject(picture);
			}
			return getSingleResultXmlView(model, result.getRetval().getId()+"");
		}else{
			return getErrorXmlView(model, ApiConstant.CODE_DATA_ERROR, result.getMsg());
		}
	}
	/**
	 * 评论(我发表的评论)
	 */
	@RequestMapping("/openapi/mobile/comment/myCommentList.xhtml")
	public String sendCommentList(int from, int maxnum, ModelMap model){
		Member member = OpenApiMobileAuthenticationFilter.getOpenApiAuth().getMember();
		if(maxnum > 100) maxnum = 100;
		List<ReComment> reCommentList = walaApiService.getMicroSendReCommentList(member.getId(), from, maxnum);
		List<Long> cIds = BeanUtil.getBeanPropertyList(reCommentList, Long.class, "relatedid", true);
		if(!cIds.isEmpty()){
			List<Comment> commentList = commentService.getCommentByIdList(cIds);
			Map<Long, Comment> sendCommentMap = BeanUtil.beanListToMap(commentList, "id");
			model.put("sendCommentMap", sendCommentMap);
			addCacheMember(model, ServiceHelper.getMemberIdListFromBeanList(sendCommentMap.values()));
		}
		model.put("reCommentList", reCommentList);
		addCacheMember(model, ServiceHelper.getMemberIdListFromBeanList(reCommentList));
		return getXmlView(model, "api/mobile/sendComment.vm");
	}
	
	/**
	 * 用户的哇啦
	 */
	@RequestMapping("/openapi/mobile/comment/memberCommentList.xhtml")
	public String userCommentList(String tags, Long memberid, Integer from, Integer maxnum, ModelMap model, HttpServletRequest request){
		if(memberid == null) return getErrorXmlView(model, ApiConstant.CODE_DATA_ERROR, "参数错误！");
		if(from == null) from = 0;
		if(maxnum == null || maxnum>100) maxnum = 20;
		String[] ts = null;
		if(StringUtils.isNotBlank(tags)){
			ts = StringUtils.split(tags, ",");
		}
		List<Comment> commentList = commentService.getCommentListByTags(ts, memberid, true, from, maxnum);
		putCommentListNode(model);
		getCommentListMap(commentList, model, request);
		return getOpenApiXmlList(model);
	}
	/**
	 * 评论(别人回复我的)（我的哇啦）
	 */
	@RequestMapping("/openapi/mobile/comment/receiveCommentList.xhtml")
	public String receiveCommentList(Integer from, Integer maxnum, ModelMap model){
		Member member = OpenApiMobileAuthenticationFilter.getOpenApiAuth().getMember();
		if(from == null) from = 0;
		if(maxnum == null || maxnum > 100) maxnum = 10;
		List<ReComment> recommentList = walaApiService.getMicroReceiveReCommentList(member.getId(), from, maxnum);
		List<Long> cIds = BeanUtil.getBeanPropertyList(recommentList, Long.class, "relatedid", true);
		if(!cIds.isEmpty()){
			List<Comment> commentList = commentService.getCommentByIdList(cIds);
			Map<Long, Comment> commentMap = BeanUtil.beanListToMap(commentList, "id");
			model.put("commentMap", commentMap);
			addCacheMember(model, ServiceHelper.getMemberIdListFromBeanList(commentMap.values()));
		}
		model.put("recommentList", recommentList);
		addCacheMember(model, ServiceHelper.getMemberIdListFromBeanList(recommentList));
		return getXmlView(model, "api/mobile/receiveComment.vm");
	}
	
	/**
	 *  哇啦详情
	 */
	@RequestMapping("/openapi/mobile/comment/commentDetail.xhtml")
	public String comment(Long commentid, ModelMap model, HttpServletRequest request){
		Comment comment = commentService.getCommentById(commentid);
		if(comment == null) return getErrorXmlView(model,ApiConstant.CODE_DATA_ERROR, "哇啦不存在！");
		getCommentMap(comment, model, request);
		putCommentNode(model);
		return getOpenApiXmlDetail(model);
	}
	
	/**
	 * 回复哇啦的列表
	 */
	@RequestMapping("/openapi/mobile/comment/replyCommentList.xhtml")
	public String reCommentList(Long commentid, Integer from, Integer maxnum, ModelMap model){
		if(commentid==null) return getErrorXmlView(model, ApiConstant.CODE_PARAM_ERROR, "invalid commentid");
		if(from == null) from =0;
		if(maxnum == null || maxnum>100) maxnum = 10;
		List<ReComment> reCommentList = walaApiService.getReCommentByRelatedidAndTomemberid(commentid, null, null, from, maxnum);
		Integer reCommentCount = walaApiService.getReCommentCountByRelatedidAndTomemberid(commentid, null, null);
		model.put("reCommentCount", reCommentCount);
		model.put("reCommentList", reCommentList);
		addCacheMember(model, ServiceHelper.getMemberIdListFromBeanList(reCommentList));
		return getXmlView(model, "api/mobile/reCommentList.vm");
	}
	
	/**
	 * 哇啦增加顶的功能
	 * @param diaryIds 帖子id集合
	 * @param model
	 * @return
	 */
	@RequestMapping("/openapi/mobile/comment/addFlower.xhtml")
	public String getDiaryList(Long commentid, ModelMap model){
		Member member = OpenApiMobileAuthenticationFilter.getOpenApiAuth().getMember();
		String opkey = "commentFlower" + member.getId() + commentid;
		if(!operationService.updateOperationOneDay(opkey, true)) return getErrorXmlView(model,  ApiConstant.CODE_PARAM_ERROR, "不能重复操作");
		Comment comment = commentService.getCommentById(commentid);
		if(comment == null) return getErrorXmlView(model,ApiConstant.CODE_DATA_ERROR, "哇啦不存在！");
		comment.addFlowernum();
		comment.setOrderTime(DateUtil.addMinute(comment.getOrderTime(), 144));
		commentService.updateComment(comment);
		return getSingleResultXmlView(model, comment.getFlowernum());
	}
}
