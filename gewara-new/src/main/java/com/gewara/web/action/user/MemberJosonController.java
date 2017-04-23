package com.gewara.web.action.user;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.RequestMapping;

import com.gewara.constant.Status;
import com.gewara.constant.content.SignName;
import com.gewara.model.bbs.DiaryBase;
import com.gewara.model.bbs.DiaryComment;
import com.gewara.model.bbs.DiaryHist;
import com.gewara.model.bbs.Moderator;
import com.gewara.model.user.Member;
import com.gewara.model.user.MemberInfo;
import com.gewara.service.bbs.BlogService;
import com.gewara.service.bbs.DiaryService;
import com.gewara.untrans.WalaApiService;
import com.gewara.untrans.monitor.MonitorService;
import com.gewara.untrans.monitor.RoleTag;
import com.gewara.util.WebUtils;
import com.gewara.web.action.BaseHomeController;
import com.gewara.xmlbind.bbs.ReComment;

@Controller
public class MemberJosonController extends BaseHomeController{
	private static Map<String, String> LOAD_LOGINBOX_MAP = new HashMap<String, String>();
	static{
		LOAD_LOGINBOX_MAP.put("new", "newloadLoginBox.vm");
		LOAD_LOGINBOX_MAP.put("point", "sns/hongbao/loadLoginBox.vm");
	}
	@Autowired@Qualifier("monitorService")
	private MonitorService monitorService;
	public void setMonitorService(MonitorService monitorService) {
		this.monitorService = monitorService;
	}

	@Autowired@Qualifier("walaApiService")
	private WalaApiService walaApiService;

	@Autowired@Qualifier("diaryService")
	private DiaryService diaryService;
	public void setDiaryService(DiaryService diaryService) {
		this.diaryService = diaryService;
	}
	@Autowired@Qualifier("blogService")
	private BlogService blogService = null;
	public void setBlogService(BlogService blogService) {
		this.blogService = blogService;
	}
	//加载登录后用户信息框
	@RequestMapping("/loadLoginBox.xhtml")
	public String loadLoginBox(ModelMap model, String tag, HttpServletRequest request, @CookieValue(value=LOGIN_COOKIE_NAME, required=false)String sessid, HttpServletResponse response){
		Member member = loginService.getLogonMemberBySessid(WebUtils.getRemoteIp(request), sessid);
		String citycode = WebUtils.getAndSetDefault(request, response);
		if(member != null) {
			model.put("member", member);
			model.put("memberInfo", daoService.getObject(MemberInfo.class, member.getId()));
			model.put("loginedGewaList",commonService.getGewaCommendList(citycode, SignName.INDEX_LOGINED, null, null, true, 0, 3)); //右侧推荐数据
		}
		model.put("citycode", citycode);
		String view = LOAD_LOGINBOX_MAP.get(tag);
		if(StringUtils.isNotBlank(view)) return view;
		return "loadLoginBox.vm";
	}
	
	//帖子回复列表
	@RequestMapping("/home/getDiaryCommentList.xhtml")
	public String getDiaryCommentList(Long requestid, ModelMap model) throws Exception {
		DiaryBase diary = this.diaryService.getDiaryBase(requestid);
		if(diary == null){
			List<ReComment> reCommentList = walaApiService.getReCommentByRelatedidAndTomemberid(requestid, null, null, 0, 100);
			model.put("reCommentList",reCommentList);
			moderMemberList(model, true);
		}else{
			List<DiaryComment> diaryCommentList = diaryService.getDiaryCommentList(requestid);
			Map<Long,List<String>> mapPic=new HashMap<Long, List<String>>();//存储每一篇帖子所包含的图片地址
			for (DiaryComment diaryComment : diaryCommentList) {
				if(StringUtils.isNotBlank(diaryComment.getBody())){
					String img="";        
					Pattern p_image;        
					Matcher m_image;        
					List<String> pics = new ArrayList<String>();     
					String regEx_img = "<img.*src=(.*?)[^>]*?>"; //图片链接地址        
					p_image = Pattern.compile(regEx_img,Pattern.CASE_INSENSITIVE);        
					m_image = p_image.matcher(diaryComment.getBody());      
					while(m_image.find()){        
						img = img + "," + m_image.group();        
						Matcher m  = Pattern.compile("src=\"?(.*?)(\"|>|\\s+)").matcher(img); //匹配src     
						while(m.find()){     
							pics.add(m.group(1));     
						}     
					}
					mapPic.put(diaryComment.getId(), pics);
				}
			}
			model.put("commentMapPic",mapPic);
			model.put("diaryCommentList", diaryCommentList);
		}
		model.put("logonMember", getLogonMember());
		return "home/action/reply.vm";
	}
	/**
	 * 主持人信息
	 * @param model
	 */
	private void moderMemberList(ModelMap model,boolean isModer){
		Set<Long> moderList = new HashSet<Long>();
		//获取主持人话题信息
		List<Moderator> moderatorList = moderatorService.getModeratorByType(Moderator.SHOW_TYPE_WEB, Moderator.TYPE_TODAY);
		for (Moderator moderator : moderatorList) {
			String[] memberids = null;
			if(StringUtils.isNotBlank(moderator.getMemberid()))
				 memberids = moderator.getMemberid().split(",");
			if(memberids!=null){
				for (String string : memberids) {
					moderList.add(new Long(string+""));
				}
			}
		}
		if(isModer)
			model.put("moderatorList", moderatorList);
		model.put("moderList", moderList);
	}
	//回复帖子
	@RequestMapping("/home/saveDiaryComment.xhtml")
	public String saveDiaryComment(Long requestid, String body, ModelMap model, HttpServletRequest request) throws Exception {
		DiaryBase diary = diaryService.getDiaryBase(requestid);
		if(diary == null) diary = daoService.getObject(DiaryHist.class, requestid);
		diary.addReplycount();
		daoService.saveObject(diary);
		DiaryComment dc = new DiaryComment(diary.getId(), getLogonMember().getId(), body);
		dc.setIp(WebUtils.getIpAndPort(WebUtils.getRemoteIp(request), request));
		String key = blogService.filterContentKey(body);
		boolean filter = false;
		if(StringUtils.isNotBlank(key)) {
			dc.setStatus(Status.N_FILTER);
			filter = true;
		}
		daoService.saveObject(dc);
		if(filter){
			String title = "有人发恶意评论！" + key;
			String content = "有人恶意评论，包含过滤关键字memberId = " + dc.getMemberid() + body;
			monitorService.saveSysWarn(DiaryComment.class, dc.getId(), title, content, RoleTag.bbs);
			return showJsonError(model, "你发表的内容包含过滤关键字，需要管理员审核！");
		}
		return showJsonSuccess(model);
	}
	

}
