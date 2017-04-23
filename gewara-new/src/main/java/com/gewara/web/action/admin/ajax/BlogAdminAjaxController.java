package com.gewara.web.action.admin.ajax;
import java.sql.Timestamp;
import java.util.ArrayList;
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

import com.gewara.constant.DiaryConstant;
import com.gewara.constant.ExpGrade;
import com.gewara.constant.Status;
import com.gewara.constant.SysAction;
import com.gewara.constant.TagConstant;
import com.gewara.constant.sys.LogTypeConstant;
import com.gewara.helper.sys.RelateClassHelper;
import com.gewara.model.BaseObject;
import com.gewara.model.acl.User;
import com.gewara.model.bbs.Bkmember;
import com.gewara.model.bbs.BlackMember;
import com.gewara.model.bbs.Diary;
import com.gewara.model.bbs.DiaryBase;
import com.gewara.model.bbs.DiaryComment;
import com.gewara.model.common.RelateToCity;
import com.gewara.model.pay.SMSRecord;
import com.gewara.model.user.Agenda;
import com.gewara.model.user.MemberInfo;
import com.gewara.model.user.SysMessageAction;
import com.gewara.service.bbs.BlogService;
import com.gewara.service.bbs.DiaryService;
import com.gewara.untrans.CommentService;
import com.gewara.untrans.SearchService;
import com.gewara.util.BeanUtil;
import com.gewara.util.BindUtils;
import com.gewara.util.ChangeEntry;
import com.gewara.util.DateUtil;
import com.gewara.util.ValidateUtil;
import com.gewara.web.action.admin.BaseAdminController;
import com.gewara.web.util.ContentHelper;
import com.gewara.xmlbind.bbs.Comment;
@Controller
public class BlogAdminAjaxController extends BaseAdminController{
	@Autowired@Qualifier("blogService")
	private BlogService blogService = null;
	public void setBlogService(BlogService blogService) {
		this.blogService = blogService;
	}
	@Autowired@Qualifier("commentService")
	private CommentService commentService;
	@Autowired@Qualifier("diaryService")
	private DiaryService diaryService;
	public void setDiaryService(DiaryService diaryService) {
		this.diaryService = diaryService;
	}

	@Autowired@Qualifier("searchService")
	private SearchService searchService;
	public void setSearchService(SearchService searchService) {
		this.searchService = searchService;
	}
	/****************************************************************
	 * @param diaryId
	 * @return
	 */
	@RequestMapping("/admin/blog/getDiaryById.xhtml")
	public String getDiaryById(Long diaryId, ModelMap model){
		DiaryBase diary = diaryService.getDiaryBase(diaryId);
		if(diary == null) return showJsonError(model, "该贴子不存在或被删除！");
		String diaryBody = blogService.getDiaryBody(diary.getId());
		Map map = BeanUtil.getBeanMap(diary, false);
		map.put("diaryBody", diaryBody);
		return showJsonSuccess(model, map);
	}
	/**
	 * 后台
	 * 版主修改Diary
	 * @param tag
	 * @param relatedid
	 * @param diaryId
	 * @param subject
	 * @param body
	 * @param summary
	 * @return
	 */
	@RequestMapping("/admin/blog/updateDiary.xhtml")
	public String updateDiary(Long diaryId, Timestamp addtimenew, String reason1, String body,
			ModelMap model, HttpServletRequest request){
		DiaryBase diary = diaryService.getDiaryBase(diaryId);
		BindUtils.bindData(diary, request.getParameterMap());
		if(StringUtils.isBlank(diary.getSubject())) return showJsonError(model, "标题不能为空！");
		if(StringUtils.isBlank(body)) return showJsonError(model, "内容不能为空！");
		//验证内容
		String msg=ValidateUtil.validateNewsContent(null, body);
		if(StringUtils.isNotBlank(msg))return showJsonError(model, msg);
		diary.setUpdatetime(new Timestamp(System.currentTimeMillis()));
		if(addtimenew!=null) diary.setAddtime(addtimenew);
		daoService.updateObject(diary);
		blogService.saveDiaryBody(diary.getId(), null, body);
		SysMessageAction sysMessage=new SysMessageAction(SysAction.STATUS_RESULT);
		sysMessage.setFrommemberid(getLogonUser().getId());
		sysMessage.setBody("您的发表的【"+diary.getSubject()+"】,被管理员已重新编辑,原因：" + reason1);
		sysMessage.setTomemberid(diary.getMemberid());
		daoService.saveObject(sysMessage);
		return showJsonSuccess(model);
	}
	
	
	/**
	 * 后台
	 * @param diaryId
	 * @param type
	 * @return
	 */
	@RequestMapping("/admin/blog/updateDiaryType.xhtml")
	public String updateDiaryType(Long diaryId, String type, ModelMap model){
		String dtype = DiaryConstant.DIARY_TYPE_MAP.get(type);
		if(dtype == null) return showJsonError(model, "类型错误！");
		DiaryBase diary = diaryService.getDiaryBase(diaryId);
		diary.setType(dtype);
		daoService.saveObject(diary);
		return showJsonSuccess(model);
	}
	
	/**
	 * 后台
	 * 逻辑删除日志：status=deleted
	 * @param diaryId
	 * @return
	 */
	@RequestMapping("/admin/blog/deleteDiary.xhtml")
	public String deleteDiary(Long diaryId,String reason,String reasonDetail, Long relatewara, ModelMap modelMap){
		if(!reason.equals("5")){
			reason = ContentHelper.REASONS.get(reason);
		}else{
			reason = reasonDetail;
		}
		DiaryBase diary = diaryService.getDiaryBase(diaryId);
		if(diary == null) return showJsonError(modelMap, "该贴子不存在或被删除！");
		ChangeEntry changeEntry = new ChangeEntry(diary);
		diary.setStatus(Status.N_DELETE);
		daoService.saveObject(diary);
		SysMessageAction sysMessage=new SysMessageAction(SysAction.STATUS_RESULT);
		sysMessage.setFrommemberid(1l);
		sysMessage.setBody("您发表的【"+diary.getSubject().substring(0,diary.getSubject().length()>5?5:diary.getSubject().length())+"...】内容涉及【"+reason+"】,已被管理员删除,<br/>如有任何疑问，可使用站内信或邮件（gewara@gewara.com）<br/>向管理员申诉。");
		sysMessage.setTomemberid(diary.getMemberid());
		daoService.saveObject(sysMessage);
		memberService.addExpForMember(diary.getMemberid(), -ExpGrade.EXP_DIARY_SUB);
		dbLogger.warnWithType(LogTypeConstant.LOG_TYPE_USERACTION, "管理员：【"+getLogonUser().getUsername()+"】删除帖子【"+diary.getSubject()+"】的状态为"+Status.N_DELETE+"帖子ID是"+diary.getId());
		
		// 关联 wala
		if(relatewara != null){
			Comment comment = commentService.getCommentById(relatewara);
			comment.setStatus(Status.N_DELETE);
			commentService.saveComment(comment);
		}
		searchService.pushSearchKey(diary);
		monitorService.saveChangeLog(getLogonUser().getId(), Diary.class, diary.getId(), changeEntry.getChangeMap(diary));
		return showJsonSuccess(modelMap);
	}
	/**
	 * 恢复帖子状态
	 * @param model
	 * @param diaryid
	 * @return
	 */
	@RequestMapping("/admin/blog/resumeDiary.xhtml")
	public String resumeDiary(ModelMap model, Long diaryid){
		DiaryBase diary = diaryService.getDiaryBase(diaryid);
		if(diary==null){
			return showJsonError(model, "此帖子不存在!");
		}
		diary.setStatus(Status.Y_NEW);
		daoService.saveObject(diary);
		searchService.pushSearchKey(diary);
		//发站内系统消息
		SysMessageAction sysMessage=new SysMessageAction(SysAction.STATUS_RESULT);
		sysMessage.setFrommemberid(1l);
		sysMessage.setBody("您发表的【"+diary.getSubject().substring(0,diary.getSubject().length()>5?5:diary.getSubject().length())+"...】内容已被管理员恢复,<br/>如有任何疑问，可使用站内信或邮件（gewara@gewara.com）<br/>向管理员申诉。");
		sysMessage.setTomemberid(diary.getMemberid());
		daoService.saveObject(sysMessage);
		//恢复经验值
		memberService.addExpForMember(diary.getMemberid(), ExpGrade.EXP_DIARY_SUB);
		dbLogger.warnWithType(LogTypeConstant.LOG_TYPE_USERACTION, "管理员：【"+getLogonUser().getUsername()+"】恢复了帖子【"+diaryid+"】的状态为"+Status.Y_NEW);
		return showJsonSuccess(model);
	}
	/**
	 * 恢复帖子恢复状态
	 * @param model
	 * @param diarycommentid
	 * @return
	 */
	@RequestMapping("/admin/blog/resumeDiaryComment.xhtml")
	public String resumeDiaryComment(ModelMap model, Long diarycommentid){
		DiaryComment diaryComment = daoService.getObject(DiaryComment.class, diarycommentid);
		if(diaryComment == null) return showJsonError(model, "此帖子留言不存在!");
		diaryComment.setStatus(Status.Y_NEW);
		memberService.addExpForMember(diaryComment.getMemberid(), ExpGrade.EXP_DIARY_REPLYER_ADD);
		daoService.saveObject(diaryComment);
		dbLogger.warnWithType(LogTypeConstant.LOG_TYPE_USERACTION, "管理员：【"+getLogonUser().getUsername()+"】恢复了帖子留言【"+diarycommentid+"】的状态为"+Status.Y_NEW);
		return showJsonSuccess(model);
	}
	
	/**
	 * 后台
	 *	批量删除
	 */
	@RequestMapping("/admin/blog/batchDeleteDiary.xhtml")
	public String batchDeleteDiary(String idListString, String reason,String reasonDetail, ModelMap model){
		if(!reason.equals("5")){
			reason = ContentHelper.REASONS.get(reason);
		}else{
			reason = reasonDetail;
		}
		DiaryBase diary = null;
		SysMessageAction sysMessage = null;
		String[] idList = StringUtils.split(idListString, ',');
		if(idList==null) return showJsonError(model, "请选择要删除的记录！");
		for(String idString : idList){
			diary = diaryService.getDiaryBase(new Long(idString)); 
			if(diary != null){
				String status = diary.getStatus();
				diary.setStatus(Status.N_DELETE);
				diary.setUpdatetime(new Timestamp(System.currentTimeMillis()));
				daoService.saveObject(diary);
				sysMessage = new SysMessageAction(SysAction.STATUS_RESULT);
				sysMessage.setFrommemberid(1l);
				sysMessage.setBody("您发表的【"+diary.getSubject().substring(0,diary.getSubject().length()>5?5:diary.getSubject().length())+"...】内容涉及【"+reason+"】,已被管理员删除,<br/>如有任何疑问，可使用站内信或邮件（gewara@gewara.com）<br/>向管理员申诉。");
				sysMessage.setTomemberid(diary.getMemberid());
				daoService.saveObject(sysMessage);
				searchService.pushSearchKey(diary);
				memberService.addExpForMember(diary.getMemberid(), -ExpGrade.EXP_DIARY_SUB);
				dbLogger.warnWithType(LogTypeConstant.LOG_TYPE_USERACTION, "管理员：【"+getLogonUser().getUsername()+"】改变了帖子【"+diary.getId()+"】的状态"+status +"为"+ Status.N_DELETE);
			}
		}
		return showJsonSuccess(model);
	}
	/**
	 * 后台
	 */
	@RequestMapping("/admin/blog/addBlackMember.xhtml")
	public String addBlackMember(Long memberId, String description, ModelMap model){
		User user = getLogonUser();
		List<BlackMember> blackMemberList = blogService.getBlackMemberList(memberId,-1,-1);
		if(blackMemberList.isEmpty()){
			BlackMember bm = new BlackMember(memberId, description, user.getId());
			daoService.saveObject(bm);
			dbLogger.warnWithType(LogTypeConstant.LOG_TYPE_USERACTION, "管理员：【"+getLogonUser().getUsername()+"】把编号为【"+memberId+"】的用户加入黑名单。");
			return showJsonSuccess(model);
		}
		return showJsonError(model, "已在黑名单中！");
	}
	@RequestMapping("/admin/blog/addBlackMemberByIds.xhtml")
	public String addBlackMemberByIds(String memberIds, String description, ModelMap model){
		User user = getLogonUser();
		String[] ids = StringUtils.split(memberIds, ",");
		List<Long> blackList = new ArrayList<Long>();
		if(ids != null){
			for (String id : ids) {
				Long memberId = Long.parseLong(id);
				if(blackList.contains(memberId)) continue;
				blackList.add(memberId);
				List<BlackMember> blackMemberList = blogService.getBlackMemberList(memberId,-1,-1);
				if(blackMemberList.isEmpty()){
					BlackMember bm = new BlackMember(memberId, description, user.getId());
					daoService.saveObject(bm);
					dbLogger.warnWithType(LogTypeConstant.LOG_TYPE_USERACTION, "管理员：【"+getLogonUser().getUsername()+"】把编号为【"+memberId+"】的用户加入黑名单。");
				}
			}
		}
		return showJsonSuccess(model);
	}
	
	/**
	 * 后台 
	 */
	@RequestMapping("/admin/blog/removeBlackMember.xhtml")
	public String removeBlackMember(Long blackMemberId, ModelMap model){
		daoService.removeObjectById(BlackMember.class, blackMemberId);
		return showJsonSuccess(model);
	}
	
	
	/***
	 * 后台 
	 */
	@RequestMapping("/admin/blog/updateDiaryFlag.xhtml")
	public String updateDiaryFlag(String flag, Long diaryId, String vflag, ModelMap model){
		DiaryBase diary = diaryService.getDiaryBase(diaryId);
		List<String> flagList = new ArrayList<String>();
		if(StringUtils.isNotBlank(diary.getFlag())){
			String[] flagArr=StringUtils.split(diary.getFlag(), ",");
			for(int i=0;i<flagArr.length;i++){
				flagList.add(flagArr[i]);
			}
		}
		if(flagList.contains(vflag)) {//删除
			for(int i=0;i<flagList.size();i++){
				if(flagList.get(i).equals(vflag))
					flagList.remove(vflag);
			}
		}else if(!flagList.contains(vflag)){
			flagList.add(vflag);//添加
			if(StringUtils.equals(vflag, "hot")){
				if(StringUtils.equals(diary.getType(), DiaryConstant.DIARY_TYPE_COMMENT)){
					memberService.addExpForMember(diary.getMemberid(), ExpGrade.EXP_TALK_HOT);
				}else if(StringUtils.equals(diary.getType(), DiaryConstant.DIARY_TYPE_TOPIC_DIARY) || StringUtils.equals(diary.getType(), DiaryConstant.DIARY_TYPE_TOPIC_VOTE)){
					memberService.addExpForMember(diary.getMemberid(), ExpGrade.EXP_DIARY_HOT);
				}
			}
			if(StringUtils.equals(vflag, "recommend")){
				memberService.addExpForMember(diary.getMemberid(), ExpGrade.EXP_DIARY_RECOMMEND);
			}
			if(StringUtils.equals(flag, "top1") && !flagList.contains("top2")) flagList.add("top2");//总论坛置顶，其他版块论坛也置顶
		}
		String toFlag="";
		for(String str: flagList){
			toFlag=toFlag+","+str;
		}
		if(StringUtils.isNotBlank(toFlag)){
			if(toFlag.startsWith(",")) toFlag = toFlag.substring(1);
			if(toFlag.endsWith(",")) toFlag = toFlag.substring(0, toFlag.length()-1);
		}
		diary.setFlag(toFlag);
		diary.setUpdatetime(new Timestamp(System.currentTimeMillis()));
		List listFlags = Arrays.asList(StringUtils.split(diary.getFlag(), ","));
		if(listFlags.contains("top1")){
			SysMessageAction sysmessage=new SysMessageAction(SysAction.STATUS_RESULT);
			sysmessage.setFrommemberid(getLogonUser().getId());
			sysmessage.setBody("您的帖子【"+diary.getSubject()+"】已被管理员置顶，并奖励20经验值!^_^");
			sysmessage.setTomemberid(diary.getMemberid());
			daoService.saveObject(sysmessage);

			MemberInfo member=daoService.getObject(MemberInfo.class, diary.getMemberid());
			member.setExpvalue(member.getExpvalue()+20);
			daoService.updateObject(member);
		}
		daoService.updateObject(diary);
		return showJsonSuccess(model);
	}
	
	@RequestMapping("/admin/blog/updateRelateToCityFlag.xhtml")
	public String updateRelateToCityFlag(String flag, Long relatedid, String citycode, String tag, String vflag, ModelMap model){
		if(StringUtils.isBlank(citycode)||StringUtils.isBlank(tag)||relatedid == null) return showJsonError(model, "参数错误！");
		BaseObject object = null;
		if(StringUtils.equals(tag, TagConstant.TAG_DIARY)){
			object = diaryService.getDiaryBase(relatedid);
		}else object = (BaseObject)relateService.getRelatedObject(tag, relatedid);
		ChangeEntry changeEntry = new ChangeEntry(object);
		String division = (String)BeanUtil.get(object, "division");
		String  oldFlag = (String)BeanUtil.get(object, "flag");
		String oldCitycode = (String)BeanUtil.get(object, "citycode");
		RelateToCity relateToCity = null;
		List<String> flagList = new ArrayList<String>();
		if(StringUtils.equals(division, DiaryConstant.DIVISION_N)){
			List<RelateToCity> reList = commonService.getRelateToCity(tag, relatedid, citycode, null);
			if(!reList.isEmpty()){
				relateToCity = reList.get(0);
			}
		}
		if(relateToCity != null){
			oldFlag = relateToCity.getFlag();
		}
		if(StringUtils.isNotBlank(oldFlag)){
			flagList.addAll(Arrays.asList(StringUtils.split(oldFlag, ",")));
		}
		if(flagList.contains(vflag)) {//删除
			for(int i=0;i<flagList.size();i++){
				if(flagList.get(i).equals(vflag))
					flagList.remove(vflag);
			}
		}else if(!flagList.contains(vflag)){
			flagList.add(vflag);//添加
			if(StringUtils.equals(flag, "top1") && !flagList.contains("top2")) flagList.add("top2");//总论坛置顶，其他版块论坛也置顶
		}
		String toFlag = StringUtils.join(flagList.toArray(), ",");
		if(relateToCity != null){
			ChangeEntry changeEntry2 = new ChangeEntry(relateToCity);
			relateToCity.setFlag(toFlag);
			if(StringUtils.equals(oldCitycode, citycode)){
				BeanUtil.set(object, "flag", toFlag);
				BeanUtil.set(object, "updatetime", DateUtil.getCurFullTimestamp());
				daoService.saveObject(object);
				monitorService.saveChangeLog(getLogonUser().getId(), RelateClassHelper.getRelateClazz(tag), relatedid, changeEntry.getChangeMap(object));
			}
			daoService.saveObject(relateToCity);
			monitorService.saveChangeLog(getLogonUser().getId(), RelateToCity.class, relateToCity.getId(), changeEntry2.getChangeMap(relateToCity));
		}else{
			BeanUtil.set(object, "flag", toFlag);
			BeanUtil.set(object, "updatetime", DateUtil.getCurFullTimestamp());
			daoService.saveObject(object);
			monitorService.saveChangeLog(getLogonUser().getId(), RelateClassHelper.getRelateClazz(tag), relatedid, changeEntry.getChangeMap(object));
		}
		return showJsonSuccess(model);
	}
	/***
	 * 后台 
	 */
	@RequestMapping("/admin/blog/updateDiaryStatus.xhtml")
	public String updateDiaryStatus(Long did, String value, ModelMap model){
		DiaryBase diary = diaryService.getDiaryBase(did);
		if(diary!=null){
			String oldStatus = diary.getStatus();
			if(oldStatus.indexOf(Status.Y_DOWN)>=0){
				if(value.indexOf(Status.Y_DOWN)<0) diary.setUtime(diary.getReplytime());
				else diary.setUtime(Timestamp.valueOf("2007-01-01 00:00:00"));
			}else { 
				if(value.indexOf(Status.Y_DOWN)>=0) diary.setUtime(Timestamp.valueOf("2007-01-01 00:00:00"));
				else diary.setUtime(diary.getReplytime());
			}
			diary.setStatus(value);
			daoService.saveObject(diary);
			searchService.pushSearchKey(diary);//更新索引至索引服务器
			return showJsonSuccess(model);
		}
		return showJsonError_DATAERROR(model);
	}

	/**
	 * 后台批量删除生活
	 * @param model
	 * @param idListString
	 * @return
	 */
	@RequestMapping("/admin/blog/deleteAgendaList.xhtml")
	public String deleteAgndaList(ModelMap model, String idListString){
		String[] idList = StringUtils.split(idListString, ",");
		if(idList == null) return showJsonError(model, "请选择要删除的记录！");
		Agenda agenda = null;
		for(String idString : idList){
			agenda = daoService.getObject(Agenda.class, new Long(idString));
			if(agenda == null) return showJsonError_NOT_FOUND(model);
			daoService.removeObject(agenda);
		}
		return showJsonSuccess(model);
	}
	
	/**
	 * 后台批量修改生活的状态
	 * @param model
	 * @param idListString
	 * @param status
	 * @return
	 */
	@RequestMapping("/admin/blog/updateAgendaList.xhtml")
	public String updateAgendaList(ModelMap model, String idListString, String status){
		String[] idList = StringUtils.split(idListString, ",");
		if(idList==null) return showJsonError(model, "请选择要删除的记录！");
		List<SMSRecord> smsList = null;
		for(String idString : idList){
			smsList = daoService.getObjectListByField(SMSRecord.class, "relatedid", new Long(idString));
			if(smsList == null || StringUtils.isBlank(status)) continue;
			for(SMSRecord sms : smsList){
				sms.setStatus(status);
				daoService.saveObject(sms);
			}
		}
		return showJsonSuccess(model);
	}
	/**
	 * 删除版主
	 * @param tag
	 * @param relatedid
	 * @param banzhuId
	 * @return
	 */
	@RequestMapping("/admin/blog/removeBanzhu.xhtml")
	public String removeBanzhu(Long banzhuId, ModelMap model) {
		Bkmember bkmember = daoService.getObject(Bkmember.class, banzhuId);
		if (bkmember == null) return showJsonError(model, "该版主不存在或已经删除！");
		daoService.removeObject(bkmember);
		return showJsonSuccess(model);
	}
	/**
	 * 设置成版主
	 * @param tag
	 * @param relatedid
	 * @param banzhuId
	 * @return
	 */
	@RequestMapping("/admin/blog/setToBanzhu.xhtml")
	public String setToBanzhu(Long banzhuId, ModelMap model) {
		Bkmember bkmember = daoService.getObject(Bkmember.class, banzhuId);
		if (bkmember == null) return showJsonError(model, "该版主不存在或已经删除！");
		bkmember.setRole(Bkmember.ROLE_BANZHU);
		daoService.saveObject(bkmember);
		return showJsonSuccess(model); //"成功增加版主权限！";
	}

	@RequestMapping("/admin/blog/setToManager.xhtml")
	public String setToManager(Long banzhuId, ModelMap model) {
		Bkmember bkmember = daoService.getObject(Bkmember.class, banzhuId);
		if (bkmember == null) return showJsonError(model, "该版主不存在或已经删除！");
		bkmember.setRole(Bkmember.ROLE_MANAGER);
		daoService.saveObject(bkmember);
		return showJsonSuccess(model); //"成功增加管理员身份！";
	}
}
