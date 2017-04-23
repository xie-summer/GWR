package com.gewara.web.action.inner.common;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import com.gewara.constant.ApiConstant;
import com.gewara.constant.TagConstant;
import com.gewara.model.user.Member;
import com.gewara.model.user.Treasure;
import com.gewara.service.member.TreasureService;
import com.gewara.untrans.WalaApiService;
import com.gewara.web.action.api.BaseApiController;
import com.gewara.web.component.ShLoginService;

@Controller
public class ApiTreasureController extends BaseApiController {
	public static final List<String> TAG_LIST = Arrays.asList(TagConstant.TAG_GYM, TagConstant.TAG_GYMCOURSE, TagConstant.TAG_GYMCOACH, TagConstant.TAG_ACTIVITY);
	
	@Autowired@Qualifier("loginService")
	private ShLoginService loginService;
	
	@Autowired@Qualifier("treasureService")
	private TreasureService treasureService;
	
	@Autowired@Qualifier("walaApiService")
	private WalaApiService walaApiService;
	
	@RequestMapping("/inner/treasure/collect/addByTag.xhtml")
	public String addCollect(String tag, Long relatedid, String action, String sessid, String ip, ModelMap model){
		if(StringUtils.isBlank(tag) || relatedid == null || StringUtils.isBlank(sessid) || StringUtils.isBlank(ip) || StringUtils.isBlank(action) || !Treasure.ACTION_LIST.contains(action)) return getErrorXmlView(model, ApiConstant.CODE_SIGN_ERROR, "参数错误！");
		Member member = loginService.getLogonMemberBySessid(ip, sessid);
		if(member == null) return getErrorXmlView(model, ApiConstant.CODE_SIGN_ERROR, "请先登录！");
		if(!TAG_LIST.contains(tag)) return getErrorXmlView(model, ApiConstant.CODE_SIGN_ERROR, "参数错误！");
		Treasure treasure = treasureService.getTreasureByTagMemberidRelatedid(tag, member.getId(), relatedid, action);
		if(treasure != null) return getErrorXmlView(model, ApiConstant.CODE_SIGN_ERROR, "该对象已收藏！");
		treasure = new Treasure(member.getId(), tag, relatedid, action);
		walaApiService.addTreasure(treasure);
		daoService.saveObject(treasure);
		return getSingleResultXmlView(model, "success");
	}
	
	@RequestMapping("/inner/treasure/collect/deleteByTag.xhtml")
	public String delCollect(String tag, Long relatedid, String action, String sessid, String ip, ModelMap model){
		if(StringUtils.isBlank(tag) || relatedid == null || StringUtils.isBlank(sessid) || StringUtils.isBlank(ip) || StringUtils.isBlank(action) || !Treasure.ACTION_LIST.contains(action)) return getErrorXmlView(model, ApiConstant.CODE_SIGN_ERROR, "参数错误！");
		Member member = loginService.getLogonMemberBySessid(ip, sessid);
		if(member == null) return getErrorXmlView(model, ApiConstant.CODE_SIGN_ERROR, "请先登录！");
		if(!TAG_LIST.contains(tag)) return getErrorXmlView(model, ApiConstant.CODE_SIGN_ERROR, "参数错误！");
		Treasure treasure = treasureService.getTreasureByTagMemberidRelatedid(tag, member.getId(), relatedid, action);
		if(treasure == null) return getErrorXmlView(model, ApiConstant.CODE_SIGN_ERROR, "该对象没有收藏！");
		walaApiService.delTreasure(member.getId(), relatedid, tag, Treasure.ACTION_COLLECT);
		daoService.removeObject(treasure);
		return getSingleResultXmlView(model, "success");
	}
}
