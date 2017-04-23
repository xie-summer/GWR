package com.gewara.web.action.subject;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.hibernate.StaleObjectStateException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.orm.hibernate3.HibernateOptimisticLockingFailureException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.gewara.constant.DrawActicityConstant;
import com.gewara.constant.MemberConstant;
import com.gewara.constant.TagConstant;
import com.gewara.constant.ticket.OrderConstant;
import com.gewara.model.common.VersionCtl;
import com.gewara.model.draw.DrawActivity;
import com.gewara.model.draw.Prize;
import com.gewara.model.draw.WinnerInfo;
import com.gewara.model.goods.Goods;
import com.gewara.model.pay.GoodsOrder;
import com.gewara.model.pay.SMSRecord;
import com.gewara.model.user.Member;
import com.gewara.model.user.MemberInfo;
import com.gewara.model.user.ShareMember;
import com.gewara.service.OperationService;
import com.gewara.service.drama.DrawActivityService;
import com.gewara.service.order.GoodsOrderService;
import com.gewara.service.order.OrderQueryService;
import com.gewara.support.ErrorCode;
import com.gewara.untrans.CommentService;
import com.gewara.untrans.ShareService;
import com.gewara.untrans.UntransService;
import com.gewara.util.DateUtil;
import com.gewara.util.StringUtil;
import com.gewara.util.VmUtils;
import com.gewara.web.action.AnnotationController;
import com.gewara.xmlbind.bbs.Comment;

@Controller
public class SubjectBOCController extends AnnotationController{
	@Autowired@Qualifier("operationService")
	private OperationService operationService;
	public void setOperationService(OperationService operationService) {
		this.operationService = operationService;
	}
	
	@Autowired@Qualifier("goodsOrderService")
	private GoodsOrderService goodsOrderService;
	public void setGoodsOrderService(GoodsOrderService goodsOrderService) {
		this.goodsOrderService = goodsOrderService;
	}
	
	@Autowired@Qualifier("orderQueryService")
	private OrderQueryService orderQueryService;
	public void setOrderQueryService(OrderQueryService orderQueryService) {
		this.orderQueryService = orderQueryService;
	}
	
	@Autowired@Qualifier("drawActivityService")
	private DrawActivityService drawActivityService;
	public void setDrawActivityService(DrawActivityService drawActivityService) {
		this.drawActivityService = drawActivityService;
	}
	
	@Autowired@Qualifier("shareService")
	private ShareService shareService;
	public void setShareService(ShareService shareService) {
		this.shareService = shareService;
	}
	
	@Autowired@Qualifier("commentService")
	private CommentService commentService;
	public void setCommentService(CommentService commentService) {
		this.commentService = commentService;
	}
	
	@Autowired@Qualifier("untransService")
	private UntransService untransService;
	public void setUntransService(UntransService untransService) {
		this.untransService = untransService;
	}
	
	@RequestMapping("/subject/proxy/bocDraw/startDraw.xhtml")
	@ResponseBody
	public String startDraw(String tag,String checkValue,String pointxy,String ip,Long gid,Long memberId){
		if(!StringUtils.equals(checkValue, StringUtil.md5(tag + "ajsdjflajsldjjkj"))){
			return "statuss=-1";
		}
		Member member = this.daoService.getObject(Member.class, memberId);
		if(member == null){
			return "statuss=-1";
		}
		String opkey = tag + member.getId();
		boolean allow = operationService.updateOperation(opkey, 5);
		if(!allow) {
			return "statuss=9";
		}
		DrawActivity da = daoService.getObjectByUkey(DrawActivity.class, "tag", tag, true);
		if(da == null||!da.isJoin()) {
			return "statuss=0";
		}
		Map<String, String> otherinfoMap = VmUtils.readJsonToMap(da.getOtherinfo());
		if(StringUtils.isNotBlank(otherinfoMap.get(DrawActicityConstant.TASK_MOBILE)) && !member.isBindMobile()) {
			return "statuss=5";
		}
		MemberInfo memberInfo = daoService.getObject(MemberInfo.class, member.getId());
		if(StringUtils.isNotBlank(otherinfoMap.get(DrawActicityConstant.TASK_EMAIL)) && !memberInfo.isFinishedTask(MemberConstant.TASK_CONFIRMREG)) {
			return "statuss=8";
		}
		if(StringUtils.isNotBlank(otherinfoMap.get(DrawActicityConstant.TASK_TICKET)) && StringUtils.isNotBlank(otherinfoMap.get(DrawActicityConstant.TASK_MOVIEID))){
			Long movieid = Long.parseLong(otherinfoMap.get(DrawActicityConstant.TASK_MOVIEID));
			int pay = orderQueryService.getMemberOrderCountByMemberid(member.getId(), movieid);
			if(pay == 0) {
				return "statuss=6";
			}
		}
		Timestamp cur = DateUtil.getCurFullTimestamp();
		Integer num = 50;
		if(StringUtils.isNotBlank(otherinfoMap.get(DrawActicityConstant.TASK_MOREDRAW))){
			if(StringUtils.isNotBlank(otherinfoMap.get(DrawActicityConstant.TASK_HOUR))){
				Timestamp startTime = DateUtil.addHour(cur,-Integer.parseInt(otherinfoMap.get(DrawActicityConstant.TASK_HOUR)));
				List<WinnerInfo> winnerList = drawActivityService.getWinnerList(da.getId(), null,  startTime, cur, "system", member.getId(), null, null, 0, 1);
				if(winnerList.size() == 1){
					return "statuss=1";
				}
				Timestamp from = DateUtil.getCurTruncTimestamp();
				Timestamp to = DateUtil.getMillTimestamp();
				int today = drawActivityService.getMemberWinnerCount(member.getId(), da.getId(), from, to);
				num = today + 1;
			}else if(StringUtils.isNotBlank(otherinfoMap.get(DrawActicityConstant.TASK_FRIEND))){
				Date date = DateUtil.currentTime();
				Timestamp startTime = DateUtil.getBeginTimestamp(date);
				Timestamp endTime = DateUtil.getEndTimestamp(date);
				Integer winnerCount = drawActivityService.getMemberWinnerCount(member.getId(), da.getId(), startTime, endTime);
				Integer inviteCount = memberService.getInviteCountByMemberid(member.getId(), startTime, endTime);
				if(inviteCount - winnerCount >= Integer.parseInt(otherinfoMap.get(DrawActicityConstant.TASK_FRIEND))) {
					num = inviteCount / Integer.parseInt(otherinfoMap.get(DrawActicityConstant.TASK_FRIEND));
				}
			}
		}
		if(StringUtils.isNotBlank(otherinfoMap.get(DrawActicityConstant.TASK_WEIBO))){
			List<ShareMember> shareMemberList = shareService.getShareMemberByMemberid(Arrays.asList(MemberConstant.SOURCE_SINA, MemberConstant.SOURCE_QQ),member.getId());
			if(VmUtils.isEmptyList(shareMemberList)) {
				return "statuss=10";
			}
			int today = drawActivityService.getMemberWinnerCount(member.getId(), da.getId(), da.getStarttime(), da.getEndtime());
			if(today > 0) {
				return "statuss=11";
			}
		}
		if(StringUtils.isNotBlank(otherinfoMap.get(DrawActicityConstant.TASK_ONLYONE))){
			int drawtimes = drawActivityService.getMemberWinnerCount(member.getId(), da.getId(), da.getStarttime(), da.getEndtime());
			if(drawtimes > 0) {
				return "statuss=11";
			}
		}
		Integer count = drawActivityService.getMemberWinnerCount(member.getId(), da.getId(),DateUtil.addDay(cur,0 - DateUtil.getWeek(cur)) ,DateUtil.addDay(cur,7 - DateUtil.getWeek(cur)));
		if((num - count) <= 0) {
			return "statuss=1";
		}
		List<GoodsOrder> goodOrders = new ArrayList<GoodsOrder>();Goods goods = null;
		if (gid != null) {
			goods= daoService.getObject(Goods.class, gid);
			if(goods == null){
				return "statuss=0";
			}
			goodOrders = goodsOrderService.getGoodsOrderList(goods.getId(), memberId, OrderConstant.STATUS_PAID_SUCCESS, false, true, 1);
			if(goodOrders.isEmpty() || StringUtils.indexOf(goodOrders.get(0).getOtherinfo(), "rebatesCard" + goods.getId()) != -1){
				return "statuss=4";
			}
		}
		
		VersionCtl mvc = drawActivityService.gainMemberVc(""+member.getId());
		try {
			//FIXME:黄牛？？
			ErrorCode<WinnerInfo> ec = drawActivityService.baseClickDraw(da, mvc, false, member);
			if(ec == null || !ec.isSuccess()) return "statuss=11";
			WinnerInfo winnerInfo = ec.getRetval();
			if(winnerInfo == null) return "statuss=2";
			Prize prize = daoService.getObject(Prize.class, winnerInfo.getPrizeid());
			if(prize == null) return "statuss=2";
			SMSRecord sms =drawActivityService.sendPrize(prize, winnerInfo, true);
			if(sms !=null) untransService.sendMsgAtServer(sms, false);
			/*if(StringUtils.isNotBlank(otherinfoMap.get(DrawActicityConstant.TASK_POINT))){
				MemberInfo info = daoService.getObject(MemberInfo.class, member.getId());
				if(info.getPointvalue() < Integer.parseInt(otherinfoMap.get(DrawActicityConstant.TASK_POINTVALUE))) return "statuss=7";
				pointService.addPointInfo(info, -Integer.parseInt(otherinfoMap.get(DrawActicityConstant.TASK_POINTVALUE)),"参加活动扣去积分", PointConstant.TAG_CONTENT);
			}*/
			Map otherinfo = VmUtils.readJsonToMap(prize.getOtherinfo());
			if(otherinfo.get(DrawActicityConstant.TASK_WALA_CONTENT) != null){
				String link = null;
				if(otherinfo.get(DrawActicityConstant.TASK_WALA_LINK) != null){
					link = otherinfo.get(DrawActicityConstant.TASK_WALA_LINK)+"";
					link = "<a href=\""+link+"\" target=\"_blank\" rel=\"nofollow\">"+"链接地址"+"</a>";
				}
				String pointx = null, pointy = null;
				if(StringUtils.isNotBlank(pointxy)){
					List<String> pointList = Arrays.asList(StringUtils.split(pointxy, ":"));
					if(pointList.size() == 2){
						pointx = pointList.get(0);
						pointy = pointList.get(1);
					}
				}
				ErrorCode<Comment> result = commentService.addComment(member, TagConstant.TAG_TOPIC, null, otherinfo.get(DrawActicityConstant.TASK_WALA_CONTENT)+"", link, false, pointx, pointy, ip);
				if(result.isSuccess()) {
					shareService.sendShareInfo("wala",result.getRetval().getId(), result.getRetval().getMemberid(), null);
				}
			}
			String fangshi = "1";
			if (prize.getPtype().equals("remark") || prize.getPtype().equals("drama"))
				fangshi = "2";
			else if (prize.getPtype().equals("empty"))
				fangshi = "3";
			/*GoodsOrder order = goodOrders.get(0);
			Map orderOtherinfo = VmUtils.readJsonToMap(order.getOtherinfo());
			orderOtherinfo.put("rebatesCard" + goods.getId(), goods.getGoodsname());
			orderOtherinfo.put("winnerInfoIds", "" + winnerInfo.getId());
			order.setOtherinfo(JsonUtils.writeMapToJson(orderOtherinfo));
			daoService.saveObject(order);*/
			return "statuss=3&prize="+prize.getOtype() + "&ptype="+prize.getPtype() + "&fangshi="+fangshi;
		}catch(StaleObjectStateException e){
			return "statuss=2";
		}catch(HibernateOptimisticLockingFailureException e){
			return "statuss=2";
		}
	}
	
}
