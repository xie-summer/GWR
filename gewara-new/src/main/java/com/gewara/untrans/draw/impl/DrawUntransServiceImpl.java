package com.gewara.untrans.draw.impl;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.hibernate.StaleObjectStateException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.orm.hibernate3.HibernateOptimisticLockingFailureException;
import org.springframework.stereotype.Service;

import com.gewara.constant.DrawActicityConstant;
import com.gewara.constant.MemberConstant;
import com.gewara.constant.PointConstant;
import com.gewara.constant.TagConstant;
import com.gewara.model.common.VersionCtl;
import com.gewara.model.draw.DrawActivity;
import com.gewara.model.draw.Prize;
import com.gewara.model.draw.WinnerInfo;
import com.gewara.model.pay.SMSRecord;
import com.gewara.model.user.Member;
import com.gewara.model.user.MemberInfo;
import com.gewara.service.OperationService;
import com.gewara.service.drama.DrawActivityService;
import com.gewara.service.gewapay.ScalperService;
import com.gewara.service.member.PointService;
import com.gewara.service.order.OrderQueryService;
import com.gewara.support.ErrorCode;
import com.gewara.untrans.CommentService;
import com.gewara.untrans.ShareService;
import com.gewara.untrans.UntransService;
import com.gewara.untrans.draw.DrawUntransService;
import com.gewara.untrans.impl.AbstractUntrantsService;
import com.gewara.util.DateUtil;
import com.gewara.util.VmUtils;
import com.gewara.xmlbind.bbs.Comment;

@Service("drawUntransService")
public class DrawUntransServiceImpl extends AbstractUntrantsService implements DrawUntransService {

	@Autowired@Qualifier("operationService")
	private OperationService operationService;
	@Autowired@Qualifier("pointService")
	private PointService pointService;
	@Autowired@Qualifier("commentService")
	private CommentService commentService;
	@Autowired@Qualifier("shareService")
	private ShareService shareService;
	@Autowired@Qualifier("drawActivityService")
	private DrawActivityService drawActivityService;
	@Autowired@Qualifier("untransService")
	private UntransService untransService;
	@Autowired@Qualifier("orderQueryService")
	private OrderQueryService orderQueryService;
	@Autowired@Qualifier("scalperService")
	private ScalperService scalperService;
	
	@Override
	public ErrorCode<String> clickDraw(Member member, String tag, String source, String pricategory, String citycode, String pointxy, String ip) {
		return clickDraw(member, tag, source, pricategory, citycode, pointxy, ip, false, null);
	}
	
	@Override
	public ErrorCode<String> clickDraw(Member member, String tag, String source, String pricategory, String citycode, String pointxy, String ip, boolean isMaxCount, Integer dayCount) {
		if(StringUtils.isBlank(tag)) return ErrorCode.getFailure("statuss=2");			//系统错误
		if(member == null) return ErrorCode.getFailure("statuss=-1");						//请先登录
		String opkey = tag + member.getId() + "clickDraw";
		boolean allow = operationService.updateOperation(opkey, 5);
		if(!allow) return ErrorCode.getFailure("statuss=9");									//操作过于频繁
		DrawActivity da = daoService.getObjectByUkey(DrawActivity.class, "tag", tag, true);
		if(da == null||!da.isJoin()) return ErrorCode.getFailure("statuss=0");			//活动未开始或已结束
		if(StringUtils.isBlank(source)) return ErrorCode.getFailure("statuss=2");		//系统错误
		if(!StringUtils.equals(da.getShowsite(), "all")){
			if(!StringUtils.equals(da.getShowsite(), source)){
				return ErrorCode.getFailure("statuss=2");											//系统错误
			}
		}
		Map<String, String> otherinfoMap = VmUtils.readJsonToMap(da.getOtherinfo());
		MemberInfo memberInfo = daoService.getObject(MemberInfo.class, member.getId());
		if(StringUtils.isNotBlank(otherinfoMap.get(DrawActicityConstant.TASK_POINT))){
			if(memberInfo.getPointvalue() < Integer.parseInt(otherinfoMap.get(DrawActicityConstant.TASK_POINTVALUE))){
				return ErrorCode.getFailure("statuss=7");											//积分不足
			}
		}
		if(StringUtils.isNotBlank(otherinfoMap.get(DrawActicityConstant.TASK_MOBILE)) && !member.isBindMobile()){
			 return ErrorCode.getFailure("statuss=5");											//请先绑定手机
		}
		if(StringUtils.isNotBlank(otherinfoMap.get(DrawActicityConstant.TASK_EMAIL)) && !memberInfo.isFinishedTask(MemberConstant.TASK_CONFIRMREG)){
			 return ErrorCode.getFailure("statuss=8");											//请先认证邮箱
		}
		if(StringUtils.isNotBlank(otherinfoMap.get(DrawActicityConstant.TASK_ONLYONE))){
			int drawtimes = drawActivityService.getMemberWinnerCount(member.getId(), da.getId(), da.getStarttime(), da.getEndtime());
			if(drawtimes > 0) return ErrorCode.getFailure("statuss=11");
		}else{
			//TODO:目前不能实现免费抽奖和按订单数抽奖共存的形式 TODO:后期需完善
			if(StringUtils.isNotBlank(otherinfoMap.get(DrawActicityConstant.TASK_DAY_COUNT))){	//每日免费抽奖次数
				int num = Integer.parseInt(otherinfoMap.get(DrawActicityConstant.TASK_DAY_COUNT));
				if(num > 0){
					Integer count = drawActivityService.getCurDrawActivityNum(da, member.getId(), num);
					if(count <= 0){
						return ErrorCode.getFailure("statuss=11");									//当前抽奖次数不足
					}
				}
			}
			//根据订单次数抽奖（目前缺陷：勾了就只能是按订单数来，而不是作为一个门槛）TODO:后期需完善
			if(StringUtils.isNotBlank(otherinfoMap.get(DrawActicityConstant.TASK_TICKET)) && StringUtils.isNotBlank(otherinfoMap.get(DrawActicityConstant.TASK_MOVIEID))){
				int drawtimes = drawActivityService.getMemberWinnerCount(member.getId(), da.getId(), da.getStarttime(), da.getEndtime());
				Long relatedid = null;
				if(StringUtils.isNotBlank(otherinfoMap.get(DrawActicityConstant.TASK_MOVIEID))) relatedid = Long.parseLong(otherinfoMap.get(DrawActicityConstant.TASK_MOVIEID));
				int pay = orderQueryService.getMemberOrderCountByMemberid(member.getId(), relatedid, da.getStarttime(), da.getEndtime(), citycode, pricategory);
				if(pay <= drawtimes) {
					return ErrorCode.getFailure("statuss=11");									//当前抽奖次数不足
				}
			}
		}
		//TODO:后期需要完善一下调教
		/*
		if(StringUtils.isNotBlank(otherinfoMap.get(DrawActicityConstant.TASK_MOREDRAW))){
			if(StringUtils.isNotBlank(otherinfoMap.get(DrawActicityConstant.TASK_HOUR))){
				Timestamp startTime = DateUtil.addHour(cur,-Integer.parseInt(otherinfoMap.get(DrawActicityConstant.TASK_HOUR)));
				List<WinnerInfo> winnerList = drawActivityService.getWinnerList(da.getId(), null,  startTime, cur, "system", member.getId(), null, null, 0, 1);
				if(winnerList.size() == 1) return "statuss=1";
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
				if(inviteCount - winnerCount >= Integer.parseInt(otherinfoMap.get(DrawActicityConstant.TASK_FRIEND))) num = inviteCount / Integer.parseInt(otherinfoMap.get(DrawActicityConstant.TASK_FRIEND));
			}
		}
		if(StringUtils.isNotBlank(otherinfoMap.get(DrawActicityConstant.TASK_WEIBO))){
			List<ShareMember> shareMemberList = shareService.getShareMemberByMemberid(Arrays.asList(MemberConstant.SOURCE_SINA, MemberConstant.SOURCE_QQ),member.getId());
			if(VmUtils.isEmptyList(shareMemberList)) return "statuss=10";
			int today = drawActivityService.getMemberWinnerCount(member.getId(), da.getId(), da.getStarttime(), da.getEndtime());
			if(today > 0) return "statuss=11";
		}*/
		VersionCtl mvc = drawActivityService.gainMemberVc(""+member.getId());
		try {
			boolean scalper =  scalperService.isScalper(member.getId(), member.getMobile());
			if(isMaxCount && !scalper){
				if(dayCount == null){
					dayCount = drawActivityService.getWinnerCount(da.getId(), null, DateUtil.getBeginTimestamp(DateUtil.getCurDate()), 
							DateUtil.getCurFullTimestamp(), null, member.getId(), null, null);
				}
				if(dayCount > 30){
					scalper = true;
				}
			}
			ErrorCode<WinnerInfo> ec = drawActivityService.baseClickDraw(da, mvc, scalper, member.getId(), member.getNickname(), member.getMobile(), ip);
			if(ec == null || !ec.isSuccess()) return ErrorCode.getFailure("statuss=4");	//奖品已发送完毕
			WinnerInfo winnerInfo = ec.getRetval();
			if(winnerInfo == null) return ErrorCode.getFailure("statuss=2");					//系统错误
			Prize prize = daoService.getObject(Prize.class, winnerInfo.getPrizeid());
			if(prize == null) return ErrorCode.getFailure("statuss=2");							//系统错误
			SMSRecord sms = drawActivityService.sendPrize(prize, winnerInfo, true);
			if(sms !=null) untransService.sendMsgAtServer(sms, false);
			if(StringUtils.isNotBlank(otherinfoMap.get(DrawActicityConstant.TASK_POINT))){
				pointService.addPointInfo(member.getId(), -Integer.parseInt(otherinfoMap.get(DrawActicityConstant.TASK_POINTVALUE)),"参加活动扣去积分", PointConstant.TAG_CONTENT);
			}
			Map<String, String> otherinfo = VmUtils.readJsonToMap(prize.getOtherinfo());
			if(StringUtils.isNotBlank(otherinfo.get(DrawActicityConstant.TASK_WALA_CONTENT))){
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
			return ErrorCode.getSuccessReturn("statuss=3&prize=" + prize.getOtype() + "&neirong=" + prize.getPlevel() + "&ptype=" + prize.getPtype());
		}catch(StaleObjectStateException e){
			return ErrorCode.getFailure("statuss=2");							//系统错误
		}catch(HibernateOptimisticLockingFailureException e){
			return ErrorCode.getFailure("statuss=2");							//系统错误
		}
	}
	
}
