package com.gewara.web.action.admin.member;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import com.gewara.constant.MemberConstant;
import com.gewara.constant.ticket.OrderConstant;
import com.gewara.json.MemberStats;
import com.gewara.model.acl.GewaraUser;
import com.gewara.model.user.Member;
import com.gewara.model.user.MemberInfo;
import com.gewara.service.bbs.SnsService;
import com.gewara.service.drama.DrawActivityService;
import com.gewara.support.ErrorCode;
import com.gewara.untrans.MemberCountService;
import com.gewara.util.VmUtils;
import com.gewara.web.action.admin.BaseAdminController;
import com.gewara.web.util.PageUtil;

@Controller
public class NewTaskAdminController extends BaseAdminController {
	@Autowired@Qualifier("memberCountService")
	private MemberCountService memberCountService;
	@Autowired@Qualifier("snsService")
	private SnsService snsService;
	public void setSnsService(SnsService snsService) {
		this.snsService = snsService;
	}
	@Autowired@Qualifier("drawActivityService")
	private DrawActivityService drawActivityService;
	public void setDrawActivityService(DrawActivityService drawActivityService) {
		this.drawActivityService = drawActivityService;
	}

	//后台用户新手任务管理
	@RequestMapping("/admin/sns/taskList.xhtml")
	public String adminTaskList(ModelMap model, Long memberid, String nickname, 
			String mobile, String email, Integer pageNo){
		if(memberid==null && StringUtils.isBlank(nickname) && StringUtils.isBlank(mobile) 
				&& StringUtils.isBlank(email) ) return "admin/sns/newtaskList.vm";
		if(pageNo==null) pageNo=0;
		Integer rowsPage=18;
		Integer from =pageNo*rowsPage;
		List<Member> memberList = snsService.searchMember(memberid, nickname, mobile, email, from, rowsPage);
		Integer memberCount = snsService.searchMemberCount(memberid, nickname, mobile, email);
		Map<String, Boolean> headPicMap = new HashMap<String, Boolean>();
		Map<String, Boolean> buyticketMap = new HashMap<String, Boolean>();
		Map<String, Boolean> movieCommentMap = new HashMap<String, Boolean>();
		Map<String, Boolean> confirmRegMap = new HashMap<String, Boolean>();
		Map<String, Boolean> fiveFriendMap = new HashMap<String, Boolean>();
		Map<String, Boolean> bindMoileMap = new HashMap<String, Boolean>();
		Map<String,Boolean> sendWalaMap = new HashMap<String,Boolean>();
		Map<String,Boolean> joincommuMap = new HashMap<String,Boolean>();
		for(Member member : memberList){
			MemberInfo memberInfo = daoService.getObject(MemberInfo.class, member.getId());
			List<String> otherNewTasksList = memberInfo.getOtherNewtaskList();
			headPicMap.put(member.getId()+"", memberInfo.isFinishedTask(MemberConstant.TASK_UPDATE_HEAD_PIC));//头像
			headPicMap.put(member.getId()+"_isTake", VmUtils.contains(otherNewTasksList, MemberConstant.TASK_UPDATE_HEAD_PIC));//是否已领取奖励
			
			buyticketMap.put(member.getId()+"", memberInfo.isFinishedTask(MemberConstant.TASK_BUYED_TICKET));//购票
			buyticketMap.put(member.getId()+"_isTake",VmUtils.contains(otherNewTasksList, MemberConstant.TASK_BUYED_TICKET));
			
			movieCommentMap.put(member.getId()+"", memberInfo.isFinishedTask(MemberConstant.TASK_MOVIE_COMMENT));//影评
			movieCommentMap.put(member.getId()+"_isTake", VmUtils.contains(otherNewTasksList, MemberConstant.TASK_MOVIE_COMMENT));
			
			confirmRegMap.put(member.getId()+"", memberInfo.isFinishedTask(MemberConstant.TASK_CONFIRMREG));//邮箱
			confirmRegMap.put(member.getId()+"_isTake", VmUtils.contains(otherNewTasksList, MemberConstant.TASK_CONFIRMREG));
			
			fiveFriendMap.put(member.getId()+"", memberInfo.isFinishedTask(MemberConstant.TASK_FIVEFRIEND));//关注五个好友
			fiveFriendMap.put(member.getId()+"_isTake", VmUtils.contains(otherNewTasksList, MemberConstant.TASK_FIVEFRIEND));
			
			bindMoileMap.put(member.getId()+"", memberInfo.isFinishedTask(MemberConstant.TASK_BINDMOBILE));//绑定手机
			bindMoileMap.put(member.getId()+"_isTake", VmUtils.contains(otherNewTasksList, MemberConstant.TASK_BINDMOBILE));
			
			sendWalaMap.put(member.getId()+"", memberInfo.isFinishedTask(MemberConstant.TASK_SENDWALA));//发哇啦
			sendWalaMap.put(member.getId()+"_isTake", VmUtils.contains(otherNewTasksList, MemberConstant.TASK_SENDWALA));
			
			joincommuMap.put(member.getId()+"", memberInfo.isFinishedTask(MemberConstant.TASK_JOINCOMMU));//加入圈子
			joincommuMap.put(member.getId()+"_isTake", VmUtils.contains(otherNewTasksList, MemberConstant.TASK_JOINCOMMU));
		}
		PageUtil pageUtil=new PageUtil(memberCount, rowsPage, pageNo, "admin/sns/taskList.xhtml");
		Map params = new HashMap();
		params.put("email", email);
		params.put("mobile", mobile);
		params.put("nickname", nickname);
		params.put("memberid", memberid);
		pageUtil.initPageInfo(params);
		model.put("pageUtil",pageUtil);
		model.put("headPicMap", headPicMap);
		model.put("buyticketMap", buyticketMap);
		model.put("movieCommentMap", movieCommentMap);
		model.put("confirmRegMap", confirmRegMap);
		model.put("fiveFriendMap", fiveFriendMap);
		model.put("bindMoileMap", bindMoileMap);
		model.put("sendWalaMap", sendWalaMap);
		model.put("joincommuMap", joincommuMap);
		model.put("memberList", memberList);
		model.put("memberCount", memberCount);
		
		return "admin/sns/newtaskList.vm";
	}
	//后台客服手动添加用户新手任务积分
	@RequestMapping("/admin/sns/addMemberNewTask.xhtml")
	public String addMemberNewTask(ModelMap model, Long memberid){
		MemberInfo memberInfo=daoService.getObject(MemberInfo.class, memberid);
		GewaraUser user = getLogonUser();
		if(memberInfo==null) return showJsonError(model, "参数错误!");
		if(memberInfo.isReceived()){
			return showJsonError(model, "该用户已经领取了新手任务积分！");
		}else{
			Member member = daoService.getObject(Member.class, memberInfo.getId());
			ErrorCode code = drawActivityService.sendNewTaskCardPrize(memberInfo, member);
			if(!code.isSuccess()) return showJsonError(model, code.getMsg());
		}
		dbLogger.warn("新手任务加积分的管理员：" + user.getRealname());
		return showJsonSuccess(model, "领取新手任务积分成功!");
	}
	@RequestMapping("/admin/sns/refreshTask.xhtml")
	public String refreshTask(ModelMap model, Long memberid){
		Member member=daoService.getObject(Member.class, memberid);
		if(member==null) return showJsonError(model, "用户不存在！");
		MemberInfo info = daoService.getObject(MemberInfo.class, member.getId());
		if(!info.isReceived()){
			String result = "";
			//是否已经购买电影票
			if(!info.isFinishedTask(MemberConstant.TASK_BUYED_TICKET)){
				boolean isBuyTicket = isBoughtTicket(member.getId());
				if(isBuyTicket) {
					memberService.saveNewTask(member.getId(), MemberConstant.TASK_BUYED_TICKET);
					result += "购票任务完成！";
				}
			}
			//是否有5位好友或关注5位好友,新手任务添加一项
			if(!info.isFinishedTask(MemberConstant.TASK_FIVEFRIEND)){
				Map dataMap = memberCountService.getMemberCount(member.getId());
				Integer friendCount = 0;
				if(dataMap!=null){
					friendCount = (Integer)dataMap.get(MemberStats.FIELD_ATTENTIONCOUNT);
					if(friendCount==null) friendCount = 0;
				}
				model.put("friendCount", friendCount);
				if(friendCount>=5){
					memberService.saveNewTask(member.getId(), MemberConstant.TASK_FIVEFRIEND);
					result += "添加关注任务完成！";
				}
			}
			//是否绑定手机
			if(!info.isFinishedTask(MemberConstant.TASK_BINDMOBILE)){
				if(member.isBindMobile()){
					memberService.saveNewTask(info.getId(), MemberConstant.TASK_BINDMOBILE);
					result += "绑定手机任务完成！";
				}
			}
			if(!info.isFinishedTask(MemberConstant.TASK_MOVIE_COMMENT)){
				//TODO:检查是否发过影评
				//memberService.saveNewTask(member.getId(), MemberConstant.TASK_MOVIE_COMMENT);
			}

			if(StringUtils.isNotBlank(result)) {
				return showJsonError(model, result);
			}
		}
		return showJsonError(model, "没有更新到数据！");
	}
	private boolean isBoughtTicket(Long memberid) {
		String query = "select count(id) from TicketOrder where memberid=? and status=?";
		List result = hibernateTemplate.find(query, memberid, OrderConstant.STATUS_PAID_SUCCESS);
		return Integer.parseInt(""+result.get(0)) > 0;
	}
}
