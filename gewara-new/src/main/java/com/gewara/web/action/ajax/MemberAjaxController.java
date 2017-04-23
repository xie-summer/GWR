package com.gewara.web.action.ajax;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
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

import com.gewara.constant.Flag;
import com.gewara.constant.MemberConstant;
import com.gewara.constant.sys.CacheConstant;
import com.gewara.model.bbs.Diary;
import com.gewara.model.bbs.DiaryBase;
import com.gewara.model.bbs.DiaryHist;
import com.gewara.model.user.Member;
import com.gewara.model.user.Point;
import com.gewara.model.user.ShareMember;
import com.gewara.service.bbs.BlogService;
import com.gewara.service.bbs.DiaryService;
import com.gewara.service.member.FriendService;
import com.gewara.service.member.PointService;
import com.gewara.support.ServiceHelper;
import com.gewara.untrans.CacheDataService;
import com.gewara.untrans.ShareService;
import com.gewara.untrans.impl.ControllerService;
import com.gewara.util.BeanUtil;
import com.gewara.util.DateUtil;
import com.gewara.util.JsonUtils;
import com.gewara.util.WebUtils;
import com.gewara.web.action.AnnotationController;
import com.gewara.web.util.PageUtil;

@Controller
public class MemberAjaxController extends AnnotationController {
	@Autowired@Qualifier("friendService")
	private FriendService friendService;
	@Autowired@Qualifier("pointService")
	private PointService pointService;
	@Autowired@Qualifier("diaryService")
	private DiaryService diaryService;
	@Autowired@Qualifier("blogService")
	private BlogService blogService;
	@Autowired@Qualifier("cacheDataService")
	private CacheDataService cacheDataService;
	@Autowired@Qualifier("controllerService")
	private ControllerService controllerService;
	@Autowired@Qualifier("shareService")
	private ShareService shareService;
	
	@RequestMapping("/ajax/acct/pointListTable.xhtml")
	public String pointListTable(@CookieValue(value=LOGIN_COOKIE_NAME, required=false)String sessid, HttpServletRequest request, ModelMap model, Integer pageNo){
		Timestamp addTime = cacheDataService.getHistoryUpdateTime(CacheConstant.KEY_POINTUPDATE);
		Member member = loginService.getLogonMemberBySessid(WebUtils.getRemoteIp(request), sessid);
		if (member == null) return showJsonError_NOT_LOGIN(model);
		if(pageNo == null) pageNo =0;
		int rowsPerPage = 10;
		int firstPerPage = pageNo*rowsPerPage;
		List<Point> pointList = pointService.getPointListByMemberid(member.getId(), null, addTime, null, null, firstPerPage, rowsPerPage);
		int pointCount = pointService.getPointCountByMemberid(member.getId(), null, addTime, null);
		PageUtil pageUtil = new PageUtil(pointCount, rowsPerPage, pageNo, "/ajax/acct/pointList.xhtml", true, true);
		Map params = new HashMap();
		pageUtil.initPageInfo(params);
		model.put("pageUtil", pageUtil);
		model.put("pointList", pointList);
		return "home/acct/pointListTable.vm";
	}
	
	//新版论坛
	@RequestMapping("/ajax/comment/newCommentList.xhtml")
	public String getNewCommentList(@CookieValue(value=LOGIN_COOKIE_NAME, required=false)String sessid,
			HttpServletRequest request, ModelMap model, String type, Integer pageNo, Long memberid, String flag) {
		String ip = WebUtils.getRemoteIp(request);
		Member logonMember = loginService.getLogonMemberBySessid(ip, sessid);
		if (logonMember == null) return showJsonError_NOT_LOGIN(model);
		//判断访问权限
		if(memberid!=null&&!memberid.equals(logonMember.getId())){
			model.putAll(friendService.isPrivate(memberid));
			model.putAll(controllerService.getCommonData(model, logonMember, memberid));
		}
		if (pageNo == null) pageNo = 0;
		int rowsPerPage = 15;
		int firstPerPage = pageNo * rowsPerPage;
		int count = 0;// 数据总条数
		List<DiaryBase> list = new ArrayList<DiaryBase>();
		Map<Long, String> bodyMap = new HashMap<Long, String>();
		
		Class clazz = Diary.class;
		if(StringUtils.equals(flag, Flag.FLAG_HISTORY)){
			clazz = DiaryHist.class;
		}else{
			model.put("diaryUpdate", cacheDataService.getHistoryUpdateTime(CacheConstant.KEY_DIARYUPDATE));
		}
		// 获取当前用户最近发表的更多评论信息
		if ("rediarytopic".equals(type)) {// 获取当前用户最近回复的更多帖子信息
			list = diaryService.getRepliedDiaryList(clazz, memberid, pageNo*rowsPerPage, rowsPerPage);
			count = diaryService.getRepliedDiaryCount(clazz, memberid);
		} else {// 获取当前用户最近发表的更多帖子信息
			list = diaryService.getDiaryListByMemberid(clazz, null, null, memberid, firstPerPage,rowsPerPage);
			count = diaryService.getDiaryCountByMemberid(clazz, null, null, memberid);
		}
		List<Long> memberidList = ServiceHelper.getMemberIdListFromBeanList(list);
	    addCacheMember(model, memberidList);
	    
		for (DiaryBase diary : list) {
			bodyMap.put(diary.getId(), blogService.getDiaryBody(diary.getId()));
		}
		
		model.put("bodyMap", bodyMap);
		PageUtil pageUtil = new PageUtil(count, rowsPerPage, pageNo, "/ajax/comment/newCommentList.xhtml", true, true);
		Map params=new HashMap();
		params.put("memberid", memberid);
		params.put("type", type);
		pageUtil.initPageInfo(params);
		model.put("logonMember", logonMember);
		model.put("member", daoService.getObject(Member.class, memberid));
		model.put("list", list);
		model.put("pageUtil", pageUtil);
		return "sns/userComment/myComment.vm";
	}
	
	//请求用户同步信息
	@RequestMapping("/ajax/member/synchroizaInfo.xhtml")
	public String forMemberSynchroizaInfo(@CookieValue(value=LOGIN_COOKIE_NAME, required=false)String sessid,HttpServletRequest request, ModelMap model){
		Member member = loginService.getLogonMemberBySessid(WebUtils.getRemoteIp(request), sessid);
		if(member == null) return showJsonError_NOT_LOGIN(model);
		List<ShareMember> shareMemberList = shareService.getShareMemberByMemberid(Arrays.asList(MemberConstant.SOURCE_SINA, MemberConstant.SOURCE_QQ),member.getId());
		if(shareMemberList != null && !shareMemberList.isEmpty()){
			for (Iterator iterator = shareMemberList.iterator(); iterator.hasNext();) {
				ShareMember shareMember = (ShareMember) iterator.next();
				if(StringUtils.equals(shareMember.getSource(), MemberConstant.SOURCE_SINA)){
					Map<String,String> otherMap = JsonUtils.readJsonToMap(shareMember.getOtherinfo());
					if(StringUtils.equals(otherMap.get("accessrights"), "0")){
						iterator.remove();
						continue;
					}
					if(shareMember.getAddtime() != null && otherMap.get("expires") != null){
						Timestamp addtime = shareMember.getAddtime();
						int expires = Integer.parseInt(otherMap.get("expires")+"") - 60;
						Timestamp duetime = DateUtil.addSecond(addtime, expires);
						if(DateUtil.isAfter(duetime)) continue;
					}
					iterator.remove();
					shareService.updateShareMemberRights(shareMember);
				}
			}
		}
		List<String> appList = BeanUtil.getBeanPropertyList(shareMemberList, String.class, "source", true);
		Map result = new HashMap();
		result.put("appList", appList);
		return showJsonSuccess(model, result);
	}
	
	@RequestMapping("/ajax/member/getSinaFriendList.xhtml")
	public String getSinaFriendList(@CookieValue(value=LOGIN_COOKIE_NAME, required=false)String sessid,HttpServletRequest request, String sinakey, ModelMap model){
		Member member = loginService.getLogonMemberBySessid(WebUtils.getRemoteIp(request), sessid);
		if(member == null) return showJsonError_NOT_LOGIN(model);
		List<String> keyList = shareService.getSinaFriendList(member.getId(), 100);
		List<String> resultList = new ArrayList<String>();
		sinakey = StringUtils.replace(sinakey, "@", "");
		if(keyList != null && !keyList.isEmpty()){
			for(String name : keyList){
				if(StringUtils.contains(name, sinakey)) resultList.add("@"+name);
			}
		}
		Map jsonMap = new HashMap();
		jsonMap.put("sinaList", resultList);
		/*String result = "[\"" + StringUtils.join(resultList, "\",\"") + "\"]";
		model.put("result", result);
		return "common/searchkey.vm";*/
		return showJsonSuccess(model, jsonMap);
	}
	
	@RequestMapping("/ajax/member/sendShareContent.xhtml")
	public String sendShareContent(@CookieValue(value=LOGIN_COOKIE_NAME, required=false)String sessid,HttpServletRequest request,
			String tag, Long tagid, String content, String picUrl, ModelMap model){
		Member member = loginService.getLogonMemberBySessid(WebUtils.getRemoteIp(request), sessid);
		if(member == null) return showJsonError_NOT_LOGIN(model);
		if(StringUtils.isBlank(tag) || StringUtils.isBlank(content)) return showJsonError(model, "参数错误！");
		List<ShareMember>  shareMemberList = shareService.getShareMemberByMemberid(Arrays.asList(MemberConstant.SOURCE_SINA, MemberConstant.SOURCE_QQ), member.getId());
		for(ShareMember shareMember : shareMemberList) {
			if(StringUtils.equals(shareMember.getSource(), MemberConstant.SOURCE_SINA)){
				Map<String,String> otherMap = JsonUtils.readJsonToMap(shareMember.getOtherinfo());
				if(StringUtils.equals(otherMap.get("accessrights"), "0") || shareMember.getAddtime() == null || otherMap.get("expires") == null){
					return showJsonError(model, "请先绑定微博！");
				}
				Timestamp addtime = shareMember.getAddtime();
				int expires = Integer.parseInt(otherMap.get("expires")+"") - 60;
				Timestamp duetime = DateUtil.addSecond(addtime, expires);
				if(!DateUtil.isAfter(duetime)){
					shareService.updateShareMemberRights(shareMember);
					return showJsonError(model, "请先绑定微博！");
				}
			}
		}
		shareService.sendShareInfo(tag, tagid, member.getId(), content, picUrl);
		return showJsonSuccess(model);
	}
	
}
