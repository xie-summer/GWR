package com.gewara.web.action.admin.content;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import com.gewara.constant.Status;
import com.gewara.model.bbs.Moderator;
import com.gewara.model.user.Member;
import com.gewara.service.bbs.ModeratorService;
import com.gewara.untrans.CommentService;
import com.gewara.web.action.admin.BaseAdminController;

@Controller
public class ModeratorAdminController extends BaseAdminController {

	@Autowired@Qualifier("moderatorService")
	private ModeratorService moderatorService;
	public void setModeratorService(ModeratorService moderatorService) {
		this.moderatorService = moderatorService;
	}
	@Autowired@Qualifier("commentService")
	private CommentService commentService;
	public void setCommentService(CommentService commentService) {
		this.commentService = commentService;
	}
	/**
	 * 显示话题信息
	 * @param model
	 * @return
	 */
	@RequestMapping("/admin/blog/setModeratorList.xhtml")
	public String setHostList(ModelMap model, String atype){
		if(StringUtils.isBlank(atype)) atype="today";
		if(StringUtils.equals("hot", atype)){
			List<Moderator> moderatorHotList = moderatorService.getModeratorList(Moderator.TYPE_HOT, null, Status.Y, 0, 100);
			initModeratorCount(moderatorHotList);
			model.put("moderatorHotList", moderatorHotList);
		}else if(StringUtils.equals("today", atype)){
			List<Moderator> moderatorTodayList = moderatorService.getModeratorList(Moderator.TYPE_TODAY, null,Status.Y, 0, 100);
			initModeratorCount(moderatorTodayList);
			model.put("moderatorTodayList", moderatorTodayList);
		}else if(StringUtils.equals("del", atype)){
			List<Moderator> moderatorDeleteList = moderatorService.getModeratorList("", null, Status.N_DELETE, 0, 100);
			initModeratorCount(moderatorDeleteList);
			model.put("moderatorDeleteList", moderatorDeleteList);
		}
		model.put("atype", atype);
		return "admin/sns/moderatorList.vm";
	}
	private void initModeratorCount(List<Moderator> moderatorList){
		for(Moderator moderator : moderatorList){
			Integer count = commentService.getModeratorDetailCount(moderator.getTitle());
			moderator.setCommentcount(count);
		}
	}
	
	/**
	 * 添加话题信息
	 * @param model
	 * @param memberid
	 * @param isHost
	 * @return
	 */
	@RequestMapping("/admin/blog/addModerator.xhtml")
	public String addModerator(ModelMap model,String memberid,String title,String summary,String type){
		Moderator hi = new Moderator(memberid);
		if(StringUtils.isNotBlank(memberid)){
			String[] members = memberid.split(",");
			for (String string : members) {
				Member member = daoService.getObject(Member.class, new Long(string+""));
				if(member == null) return showJsonError(model, "你添加的主持人不存在！");
			}
		}
		hi.setTitle(title);
		hi.setSummary(summary);
		hi.setType(type);
		hi.setMstatus(Status.Y);
		daoService.saveObject(hi);
		return showJsonSuccess(model);
	}
	
	
	/**
	 * 话题排序
	 */
	@RequestMapping("/admin/sns/setModeratorOrder.xhtml")
	public String setModeratorOrderNum(ModelMap model,Long id,Integer ordernum){
		Moderator moderator = daoService.getObject(Moderator.class, id);
		moderator.setOrdernum(ordernum);
		try{
			 daoService.updateObject(moderator);
			 return showJsonSuccess(model);
		}catch(Exception e){
			return showJsonError(model, "设置失败!");
		}
	}
	
	/**
	 * 设置话题
	 */
	@RequestMapping("/admin/sns/setModeratorAddress.xhtml")
	public String setModeratorAddress(ModelMap model,Long id,Integer showaddress){
		Moderator moderator = daoService.getObject(Moderator.class, id);
		moderator.setShowaddress(showaddress);
		try{
			 daoService.updateObject(moderator);
			 return showJsonSuccess(model);
		}catch(Exception e){
			return showJsonError(model, "设置失败!");
		}
	}
	
	@RequestMapping("/admin/blog/updateModerator.xhtml")
	public String updateModerator(ModelMap model,Long id,String memberid,String title,String summary,String type){
		Moderator moderator = daoService.getObject(Moderator.class, id);
		if(moderator!=null){
			moderator.setSummary(summary);
			moderator.setTitle(title);
			moderator.setMemberid(memberid);
			moderator.setType(type);
			daoService.updateObject(moderator);
			return showJsonSuccess(model);
		}else{
			return showJsonError(model, "你好修改的对象不存在！");
		}
	}
	
	/**
	 * 删除话题信息
	 */
	@RequestMapping("/admin/sns/deleteModerator.xhtml")
	public String deleteModerator(ModelMap model,Long mid){
		Moderator moderator = daoService.getObject(Moderator.class, mid);
		if(moderator !=null){
			//daoService.removeObject(moderator);
			moderator.setMstatus(Status.N_DELETE);
			daoService.saveObject(moderator);
			return showJsonSuccess(model);
		}
		return showJsonError(model, "删除失败！");
	}
	/**
	 * 是否显示楼层
	 */
	@RequestMapping("/admin/sns/showFloor.xhtml")
	public String showFloor(ModelMap model,Long mid,Integer showfloor){
		Moderator moderator = daoService.getObject(Moderator.class, mid);
		if(moderator !=null){
			moderator.setShowfloor(showfloor);
			daoService.updateObject(moderator);
			return showJsonSuccess(model);
		}
		return showJsonError(model, "显示失败！");
	}
}
