package com.gewara.web.action.home;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import com.alibaba.dubbo.common.utils.StringUtils;
import com.gewara.constant.MemberCardConstant;
import com.gewara.constant.Status;
import com.gewara.model.sport.MemberCardInfo;
import com.gewara.model.sport.MemberCardType;
import com.gewara.model.sport.Sport;
import com.gewara.model.user.Member;
import com.gewara.service.sport.MemberCardService;
import com.gewara.support.ErrorCode;
import com.gewara.untrans.sport.RemoteMemberCardService;
import com.gewara.util.ValidateUtil;
import com.gewara.web.action.BaseHomeController;
import com.gewara.xmlbind.sport.RemoteMemberCardInfo;

@Controller
public class SportHomeMemberCardController extends BaseHomeController{
	@Autowired@Qualifier("memberCardService")
	private MemberCardService memberCardService;
	@Autowired@Qualifier("remoteMemberCardService")
	private RemoteMemberCardService remoteMemberCardService;
	@RequestMapping("/home/acct/memberCard/cardList.xhtml")
	public String mySportMemberCardList(ModelMap model){
		Member member = getLogonMember();
		List<MemberCardInfo> mciList = memberCardService.getMemberCardInfoListByMemberid(member.getId());
		Map<Long, MemberCardType> mctMap = new HashMap<Long, MemberCardType>();
		Map<Long, List<Sport>> sportsMap = new HashMap<Long, List<Sport>>(); 
		for(MemberCardInfo info : mciList){
			List<Sport> sportList = memberCardService.getFitSportList(info.getBelongVenue());
			sportsMap.put(info.getId(), sportList);
			MemberCardType mct = daoService.getObject(MemberCardType.class, info.getTypeid());
			mctMap.put(info.getId(), mct);
		}
		model.put("sportsMap", sportsMap);
		model.put("mctMap", mctMap);
		model.put("mciList", mciList);
		return "home/acct/memberCard/vipCard.vm";
	}
	@RequestMapping("/home/ajax/memberCard/bindRemoteCard.xhtml")
	public String bindCard(ModelMap model, String checkpass, String mobile){
		if(StringUtils.isBlank(checkpass)){
			return showJsonError(model, "短信验证码不能为空！");
		}
		boolean isMobile = ValidateUtil.isMobile(mobile);
		if(!isMobile){
			return showJsonError(model, "手机号不正确！");
		}
		Member member = getLogonMember();
		ErrorCode<List<RemoteMemberCardInfo>> rcode = remoteMemberCardService.getRemoteMemberCardInfoListByCheckpass(mobile, checkpass);
		if(!rcode.isSuccess()){
			return showJsonError(model, rcode.getMsg());
		}
		ErrorCode<List<MemberCardInfo>> code = memberCardService.bindMemberCard(member, rcode.getRetval());
		if(!code.isSuccess()){
			return showJsonError(model, code.getMsg());
		}
		List<MemberCardInfo> cardList = memberCardService.getUnBindMemberCard(member, mobile);
		Map<Long, List<Sport>> sportsMap = new HashMap<Long, List<Sport>>(); 
		for(MemberCardInfo card : cardList){
			List<Sport> sportList = memberCardService.getFitSportList(card.getBelongVenue());
			sportsMap.put(card.getId(), sportList);
		}
		model.put("sportsMap", sportsMap);
		model.put("cardList", cardList);
		return "home/acct/memberCard/unBindCardList.vm";
	}
	
	@RequestMapping("/home/ajax/memberCard/sendMobilePass.xhtml")
	public String bindSportMemberCard(ModelMap model, String mobile){
		boolean isMobile = ValidateUtil.isMobile(mobile);
		if(!isMobile){
			return showJsonError(model, "手机号不正确！");
		}
		ErrorCode<String> code = remoteMemberCardService.getMobileCheckpass(mobile, MemberCardConstant.CHECKPAS_TYPE_MCARD);
		if(!code.isSuccess()){
			return showJsonError(model, code.getMsg());
		}
		return showJsonSuccess(model);
	}
	@RequestMapping("/home/ajax/memberCard/bindMemberCard.xhtml")
	public String bindMemberCard(Long cardid, ModelMap model){
		Member member = getLogonMember();
		MemberCardInfo card = daoService.getObject(MemberCardInfo.class, cardid);
		if(card==null){
			return showJsonError(model, "卡不存在！");
		}
		if(!card.getMemberid().equals(member.getId())){
			return showJsonError(model, "不能操作他人的订单!");
		}
		dbLogger.warn("用户绑定运动会员卡：memberid:" + member.getId()+", cardno:" + card.getMemberCardCode());
		card.setBindStatus(Status.Y);
		daoService.saveObject(card);
		return showJsonSuccess(model);
	}
	@RequestMapping("/home/ajax/memberCard/unbindMemberCard.xhtml")
	public String unbindMemberCard(Long cardid, ModelMap model){
		Member member = getLogonMember();
		MemberCardInfo card = daoService.getObject(MemberCardInfo.class, cardid);
		if(card==null){
			return showJsonError(model, "卡不存在！");
		}
		if(!card.getMemberid().equals(member.getId())){
			return showJsonError(model, "不能操作他人的订单!");
		}
		dbLogger.warn("用户解绑运动会员卡：memberid:" + member.getId()+", cardno:" + card.getMemberCardCode());
		card.setBindStatus(Status.N);
		daoService.saveObject(card);
		return showJsonSuccess(model);
	}
}
