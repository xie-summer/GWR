package com.gewara.web.action.subject;

import java.util.Arrays;
import java.util.Date;
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
import com.gewara.constant.sys.MongoData;
import com.gewara.model.user.Member;
import com.gewara.model.user.ShareMember;
import com.gewara.mongo.MongoService;
import com.gewara.service.OperationService;
import com.gewara.untrans.CommentService;
import com.gewara.untrans.ShareService;
import com.gewara.util.DateUtil;
import com.gewara.util.ObjectId;
import com.gewara.util.StringUtil;
import com.gewara.web.action.AnnotationController;
@Controller
public class SubjectMotorShowProxyController extends AnnotationController{
	@Autowired@Qualifier("mongoService")
	private MongoService mongoService;
	
	@Autowired@Qualifier("operationService")
	private OperationService operationService;
	
	@Autowired@Qualifier("shareService")
	private ShareService shareService;
	
	@Autowired@Qualifier("commentService")
	protected CommentService commentService;
	
	private void addDrawTimes(Long memberid,String type){
		Date now = new Date();
		Map params = new HashMap();
		params.put("memberid", memberid);
		params.put("type", type);
		Map map = mongoService.findOne(MongoData.NS_COMMON_MOTORSHOW_DRAWTIMES, params);
		if(map == null){
			map = new HashMap();
			map.put("memberid", memberid);
			map.put("addTime", DateUtil.format(now, "yyyy-MM-dd"));
			map.put("updateTime", DateUtil.format(now, "yyyy-MM-dd HH:mm:ss"));
			map.put("freeTimes", 1);
			map.put("useTime", 0);
			map.put("type", type);
			map.put(MongoData.SYSTEM_ID, ObjectId.uuid());
			mongoService.saveOrUpdateMap(map, MongoData.SYSTEM_ID, MongoData.NS_COMMON_MOTORSHOW_DRAWTIMES);
		}
	}

	//分享微博
	@RequestMapping("/subject/proxy/sharesMotorShowInfo.xhtml")
	public String sharesTicketInfo(Long memberid, String check, String content, ModelMap model){
		String checkcode = StringUtil.md5(memberid + "njmk5678");
		if(!StringUtils.equals(check, checkcode)) {
			return showJsonSuccess(model, "nologin");
		}
		Member member = daoService.getObject(Member.class, memberid);
		if(member == null) {
			return showJsonError(model, "请先登录！");
		}
		if(StringUtils.isBlank(content)) {
			return showJsonError(model, "分享内容不能为空！");
		}
		boolean allow = operationService.updateOperation("shareFilmFest" + member.getId(), OperationService.HALF_MINUTE, 5);
		if(!allow) {
			return showJsonError(model, "你操作过于频繁，请稍后再试！");
		}
		List<ShareMember>  shareMemberList = shareService.getShareMemberByMemberid(Arrays.asList(MemberConstant.SOURCE_SINA), member.getId());
		if(shareMemberList.isEmpty()) {
			return showJsonError(model, "noBindWeiBo");
		}
		shareService.sendShareInfo("subject", null, member.getId(), content, "images/festivalLogo/201304/s41bfa751_13dd909ebed__7c38.jpg");
		addDrawTimes(memberid,"WEIBO");
		return showJsonSuccess(model,"success");
	}
}
