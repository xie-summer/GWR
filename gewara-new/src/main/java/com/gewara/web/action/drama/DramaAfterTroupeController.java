package com.gewara.web.action.drama;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import com.gewara.constant.Status;
import com.gewara.constant.content.SignName;
import com.gewara.model.bbs.Diary;
import com.gewara.model.bbs.DiaryBase;
import com.gewara.model.bbs.DiaryHist;
import com.gewara.model.bbs.commu.Commu;
import com.gewara.model.bbs.commu.CommuMember;
import com.gewara.model.content.GewaCommend;
import com.gewara.service.bbs.CommuService;
import com.gewara.support.ServiceHelper;
import com.gewara.util.BeanUtil;
import com.gewara.util.RelatedHelper;
import com.gewara.util.WebUtils;
import com.gewara.web.util.PageUtil;
import com.gewara.xmlbind.bbs.Comment;
@Controller
public class DramaAfterTroupeController extends BaseDramaController{

	@Autowired@Qualifier("commuService")
	private CommuService commuService;
	public void setCommuService(CommuService commuService){
		this.commuService = commuService;
	}
	@RequestMapping("/drama/troupeIndex.xhtml")
	public String troupeIndex(ModelMap model, HttpServletRequest request, HttpServletResponse response){
		String citycode = WebUtils.getAndSetDefault(request, response);
		RelatedHelper rh = new RelatedHelper();
		//招募活动5条
		List<GewaCommend> activityList = commonService.getGewaCommendList(citycode, SignName.DRAMA_RECRUIT_ACTIVITY, null, null, true, 0, 5);
		model.put("activityList", activityList);
		commonService.initGewaCommendList("activityList", rh, activityList);
		//哇啦3条
		List<Comment> commentList = commentService.searchCommentList("格瓦拉追剧团", null, 0, 3);
		model.put("commentList", commentList);
		Long commuid = 56862624L;
		//圈子
		Commu commu = daoService.getObject(Commu.class, commuid);//格瓦拉追剧团圈子
		if(commu != null && commu.hasStatus(Status.Y)){
			model.put("commu", commu);
		}
		model.put("rh", rh);
		//圈子成员人数
		if(commu != null){
			Integer commuMemberNum = commuService.getCommumemberCount(commu.getId(), null);
			model.put("commuMemberNum", commuMemberNum);
		}
		//最近加入的成员
		List<CommuMember> listCommuMember = commuService.getCommuMemberById(commuid, null, null, "", 0, 6);
		model.put("listCommuMember", listCommuMember);
		addCacheMember(model, ServiceHelper.getMemberIdListFromBeanList(listCommuMember));
		return "drama/afterTroupe/index.vm";
	}
	
	//往期回顾异步加载
	@RequestMapping("/drama/ajax/oldTimey.xhtml")
	public String getOldTimey(String type, HttpServletRequest request, HttpServletResponse response, Integer pageNo,ModelMap model){
		String citycode = WebUtils.getAndSetDefault(request, response);
		if(pageNo == null) pageNo = 0;
		int rowsPerPage = 16;//往期回顾16条每页
		int from = pageNo * rowsPerPage;
		List<String> singNameList = Arrays.asList(new String[]{SignName.DRAMA_RECRUIT_DIARY, SignName.DRAMA_RECRUIT_DIARYSP});
		if("sp".equals(type)){
			singNameList = Arrays.asList(SignName.DRAMA_RECRUIT_DIARYSP);
		}else if("normal".equals(type)){
			singNameList = Arrays.asList(SignName.DRAMA_RECRUIT_DIARY);
		}
		List<GewaCommend> diaryList = commonService.getGewaCommendList(citycode, singNameList, null, false, true, false, from, rowsPerPage);
		int count = commonService.getGewaCommendCount(citycode, singNameList, null, null, false);
		PageUtil pageUtil = new PageUtil(count, rowsPerPage, pageNo, "/drama/troupeIndex.xhtml", true, true);
		Map map = new HashMap();
		pageUtil.initPageInfo(map);
		List<Long> memberidList = new ArrayList<Long>();
		Map<Long, String> diaryMap = new HashMap<Long, String>();
		Map<Long, Long> memberidMap = new HashMap<Long, Long>();
		for(GewaCommend commend : diaryList){
			DiaryBase diary = daoService.getObject(Diary.class, commend.getRelatedid());
			if(diary == null) diary = daoService.getObject(DiaryHist.class, commend.getRelatedid());
			if(diary != null){
				diaryMap.put(commend.getId(), diary.getSubject());
				memberidMap.put(commend.getId(), diary.getMemberid());
				memberidList.add(diary.getMemberid());
			}
		}
		addCacheMember(model, memberidList);
		model.put("memberidMap", memberidMap);
		model.put("diaryList", diaryList);
		model.put("diaryMap", diaryMap);
		model.put("pageUtil", pageUtil);
		return "drama/afterTroupe/oldtimey.vm";
	}
	
	//哇啦异步加载
	@RequestMapping("/drama/ajax/loadMicroModerTable.xhtml")
	public String loadMicroModerTable(String moderate, Integer count, String isReply, ModelMap model){
		List<Comment> commentList = commentService.getModeratorDetailList(moderate, false, 0, count);
		model.put("moderate", moderate);
		model.put("commentList", commentList);
		List<Long> cIds = BeanUtil.getBeanPropertyList(commentList, Long.class, "transferid", true);
		if(!cIds.isEmpty()){
			List<Comment> tranferCommentList = commentService.getCommentByIdList(cIds);
			Map<Long, Comment> tranferCommentMap = BeanUtil.beanListToMap(tranferCommentList, "id");
			model.put("tranferCommentMap", tranferCommentMap);
			addCacheMember(model, ServiceHelper.getMemberIdListFromBeanList(tranferCommentMap.values()));
		}
		addCacheMember(model, ServiceHelper.getMemberIdListFromBeanList(commentList));
		model.put("isReply", isReply == null ? false : true);
		return "drama/afterTroupe/microModerateTable.vm";
	}
}
