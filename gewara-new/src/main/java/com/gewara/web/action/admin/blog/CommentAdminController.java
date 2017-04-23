package com.gewara.web.action.admin.blog;

import java.sql.Timestamp;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.support.PropertyComparator;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import com.gewara.constant.ExpGrade;
import com.gewara.constant.Status;
import com.gewara.constant.SysAction;
import com.gewara.constant.TagConstant;
import com.gewara.constant.sys.LogTypeConstant;
import com.gewara.model.movie.Cinema;
import com.gewara.model.movie.Movie;
import com.gewara.model.sport.Sport;
import com.gewara.model.user.SysMessageAction;
import com.gewara.support.ErrorCode;
import com.gewara.support.ServiceHelper;
import com.gewara.untrans.CommentService;
import com.gewara.untrans.WalaApiService;
import com.gewara.untrans.gym.SynchGymService;
import com.gewara.util.DateUtil;
import com.gewara.web.action.admin.BaseAdminController;
import com.gewara.web.util.PageUtil;
import com.gewara.xmlbind.bbs.Comment;
import com.gewara.xmlbind.gym.RemoteCoach;
import com.gewara.xmlbind.gym.RemoteGym;

@Controller
public class CommentAdminController extends BaseAdminController {
	@Autowired@Qualifier("commentService")
	private CommentService commentService;

	@Autowired@Qualifier("walaApiService")
	private WalaApiService walaApiService;
	
	@Autowired@Qualifier("synchGymService")
	private SynchGymService synchGymService;
	
	@RequestMapping("/admin/blog/microcommentList.xhtml")
	public String commentList(ModelMap model, Long memberid, String tag, Long relatedid,String body,String status, Timestamp beginDate, Timestamp endDate, String isexport, Integer pageNo, HttpServletResponse response){
		if(pageNo == null) pageNo = 0;
		Integer maxNum = 30;
		Integer from = pageNo * maxNum;
		
		List<Comment> commentList = commentService.getCommentList(tag, relatedid, memberid, body, status, beginDate, endDate, from, maxNum);
		Integer count = commentService.getCommentCount(tag, relatedid, memberid, body, status, beginDate, endDate);
		Map params = new HashMap();

		params.put("memberid", memberid);
		params.put("tag", tag);
		params.put("relatedid",relatedid);
		params.put("body", body);
		params.put("status", status);
		params.put("beginDate", beginDate);
		params.put("endDate", endDate);
		PageUtil pageUtil = new PageUtil(count,maxNum,pageNo,"admin/blog/microcommentList.xhtml");
		pageUtil.initPageInfo(params);
		model.putAll(params);
		model.put("commentList", commentList);
		addCacheMember(model, ServiceHelper.getMemberIdListFromBeanList(commentList));
		model.put("pageUtil", pageUtil);
		
		// excel导出
		if(StringUtils.isNotBlank(isexport)){
			List<Comment> exportcommentList = commentService.getCommentList(tag, relatedid, memberid, body, status, -1, -1);
			download("xls", response);
			Collections.sort(commentList, new PropertyComparator("addtime", false, true));
			model.put("commentList", exportcommentList);
			return "admin/common/exportMicrocommentList.vm";
		}
		return "admin/common/commentList.vm";
	}
	
	@RequestMapping("/admin/blog/hotRecommendComment.xhtml")
	public String hotRecommendComment(ModelMap model,int hotValue,long id){
		Comment comment = commentService.getCommentById(id);
		if(comment == null){
			return showJsonError(model, "操作失败,此条哇啦已不存在！");
		}
		if(hotValue == 500){
			comment.setOrderTime(DateUtil.addDay(comment.getOrderTime(), 5));
		}else{
			comment.setOrderTime(DateUtil.addDay(comment.getOrderTime(), -5));
		}
		commentService.updateComment(comment);
		return showJsonSuccess(model);
	}
	/**
	 * 批量删除哇啦信息
	 */
	@RequestMapping("/admin/blog/deleteMultiComment.xhtml")
	public String deleteMultiComment(ModelMap model,String ids, String status,String reason,String reasonDetail){
		if(!"5".equals(reason)&&Status.N_DELETE.equals(status)){
			reason = ServiceHelper.getReason(reason);
		}else{
			reason=reasonDetail;
		}
		try{
			if(StringUtils.isNotBlank(ids)){
				String[] ides = ids.split(",");
					for (String obj : ides) {
						Long id = Long.valueOf(obj);
						String value="";
						SysMessageAction sysmessage=new SysMessageAction(SysAction.STATUS_RESULT);
						sysmessage.setFrommemberid(1l);
						Comment comment = commentService.getCommentById(id);
						comment.setStatus(status);
						boolean b = false;
						if(TagConstant.TAG_TOPIC.equals(comment.getTag())){
							b=true;
						}else{
							value = returnValue(comment);
						}
						if(status.equals(Status.N_DELETE)){
							if(b){
								int bodylength = comment.getBody().length();
								sysmessage.setBody("您发表的哇啦【"+comment.getBody().substring(0,bodylength>5?5:bodylength)+"...】涉及【"+reason+"】,已被管理员删除,<br/>如有任何疑问，可使用站内信或邮件（gewara@gewara.com）<br/>向管理员申诉。");
							}else{
								sysmessage.setBody("您对【"+value+"...】的点评涉及【"+reason+"】,已被管理员删除,<br/>如有任何疑问，可使用站内信或邮件（gewara@gewara.com）<br/>向管理员申诉。");
							}
							memberService.addExpForMember(comment.getMemberid(), -ExpGrade.EXP_COMMENT_ADD_COMMON);
							dbLogger.warnWithType(LogTypeConstant.LOG_TYPE_USERACTION, "管理员:"+getLogonUser().getNickname()+" 删除编号为："+id+",类型为："+comment.getTag()+"comment");
						}else{
							if(b){
								int bodylength = comment.getBody().length();
								sysmessage.setBody("您发表的哇啦【"+comment.getBody().substring(0,bodylength>5?5:bodylength)+"...】,已通过管理员审核。");
							}else
								sysmessage.setBody("您对【"+value+"...】的点评已通过管理员审核。");
						}
						sysmessage.setTomemberid(comment.getMemberid());
						daoService.saveObject(sysmessage);
						commentService.updateComment(comment);
						walaApiService.deleteMicroReComment(comment.getId());
					}
					return showJsonSuccess(model);
			}else{
				return showJsonError(model, "操作失败！");
			}
		}catch(Exception e){
			return showJsonError(model, "操作失败！");
		}
	}
	
	private String returnValue(Comment comment){
		String value = "";
		if("gym".equals(comment.getTag())){
			ErrorCode<RemoteGym> code = synchGymService.getRemoteGym(comment.getRelatedid(), true);
			if(code.isSuccess()){
				RemoteGym gym = code.getRetval();
				value=gym.getName().substring(0,gym.getName().length()>5?5:gym.getName().length());
			}
		}else if("movie".equals(comment.getTag())){
			Movie movie=daoService.getObject(Movie.class, comment.getRelatedid());
			value=movie.getName().substring(0,movie.getName().length()>5?5:movie.getName().length());
		}else if("sport".equals(comment.getTag())){
			Sport sport=daoService.getObject(Sport.class, comment.getRelatedid());
			value=sport.getName().substring(0,sport.getName().length()>5?5:sport.getName().length());
		}else if("gymcoach".equals(comment.getTag())){
			ErrorCode<RemoteCoach> code = synchGymService.getRemoteCoach(comment.getRelatedid(), true);
			if(code.isSuccess()){
				RemoteCoach gymcoach = code.getRetval();
				value=gymcoach.getName().substring(0,gymcoach.getName().length()>5?5:gymcoach.getName().length());
			}
		}else if("cinema".equals(comment.getTag())){
			Cinema cinema=daoService.getObject(Cinema.class,comment.getRelatedid());
			value=cinema.getName().substring(0,cinema.getName().length()>5?5:cinema.getName().length());
		}
		return value;
	}
	
}
