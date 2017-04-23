package com.gewara.untrans.impl;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.ui.ModelMap;

import com.gewara.model.user.Member;
import com.gewara.model.user.MemberInfo;
import com.gewara.service.DaoService;
import com.gewara.service.bbs.BlogService;
import com.gewara.service.member.MemberService;
import com.gewara.support.GewaCaptchaService;
import com.gewara.untrans.RelateService;
import com.gewara.util.BeanUtil;
import com.gewara.util.JsonUtils;
import com.gewara.util.RelatedHelper;

/**
 * 为了写一些Controller中出现的一些共用方法
 * @author acerge(acerge@163.com)
 * @since 7:39:47 PM Sep 23, 2011
 */
@Service("controllerService")
public class ControllerService {
	private static final String CAPTCHAPRE = "zt2reXy";
	
	@Autowired@Qualifier("captchaService")
	private GewaCaptchaService captchaService;
	@Autowired@Qualifier("daoService")
	private DaoService daoService;
	public void setDaoService(DaoService daoService) {
		this.daoService = daoService;
	}
	@Autowired@Qualifier("memberService")
	private MemberService memberService;
	public void setMemberService(MemberService memberService) {
		this.memberService = memberService;
	}
	@Autowired@Qualifier("relateService")
	private RelateService relateService;
	
	@Autowired@Qualifier("blogService")
	protected BlogService blogService;
	/**
	 * 用来做验证码
	 * @param request
	 * @param captcha
	 * @return
	 */
	public boolean validateZtCaptcha(String captchaId, String captcha, String ip) {
		return validateCaptcha(captchaId+CAPTCHAPRE, captcha, ip);
	}
	/**
	 * 用来做验证码
	 * @param request
	 * @param captcha
	 * @return
	 */
	public boolean validateCaptcha(String captchaId, String captcha, String ip) {
		boolean validCaptcha = false;
		if(StringUtils.isNotBlank(captcha)){
			captcha = StringUtils.lowerCase(captcha);
			validCaptcha = captchaService.validateResponseForID(captchaId, captcha, ip);
		}
		return validCaptcha;
	}
	//用户共用数据
	public Map getCommonData(ModelMap modelMap, Member logonMember, Long memberid) {
		Member member = daoService.getObject(Member.class, memberid);
		MemberInfo memberInfo = daoService.getObject(MemberInfo.class, memberid);
		Map model = new HashMap();
		model.put("member", member);
		addCacheMember(modelMap, memberid);
		model.put("memberInfo", memberInfo);
		model.put("logonMember", logonMember);
		
		//是朋友/关注
		if(logonMember.getId().equals(memberid)){
			model.put("isFriend", false);
			model.put("isMyFriend", false);
		}else{
			boolean isMyFriend =blogService.isTreasureMember(logonMember.getId(),memberid);
			model.put("isFriend", true);
			model.put("isMyFriend", isMyFriend);
		}
		return model;
	}
	// VO保存otherinfo
	public Map<String, Object> getAndSetOtherinfo(String otherinfo, List<String> itemList, HttpServletRequest request){
		Map<String, Object> otherinfoMap = JsonUtils.readJsonToMap(otherinfo);
		for(String otheritem: itemList){
			String info = request.getParameter(otheritem);
			if(StringUtils.isNotBlank(info)){
				otherinfoMap.put(otheritem, info);
			}else {
				otherinfoMap.remove(otheritem);
			}
		}
		return otherinfoMap;
	}
	
	public void initRelate(String group, RelatedHelper rh, List objList) {
		if(objList == null || objList.isEmpty()) return;
		Object relate = null;
		for (Object object : objList) {
			if(object==null) continue;
			String tag = (String) BeanUtil.get(object, "tag");
			Long relatedid = (Long) BeanUtil.get(object, "relatedid");
			String category = (String) BeanUtil.get(object, "category");
			Long categoryid = (Long) BeanUtil.get(object, "categoryid");
			Long id = (Long) BeanUtil.get(object, "id");
			if (StringUtils.isNotBlank(tag) && relatedid != null) {
				relate = relateService.getRelatedObject(tag, relatedid);
				rh.addRelated1(group, id, relate);
			}
			if (StringUtils.isNotBlank(category) && categoryid != null) {
				relate = relateService.getRelatedObject(category, categoryid);
				rh.addRelated2(group, id, relate);
			}
		}
	}
	public  void initRelate(String group, RelatedHelper rh, Object... objList) {
		if (objList != null)
			initRelate(group, rh, Arrays.asList(objList));
	}
	
	private void addCacheMember(ModelMap model, Long memberid) {
		Map<Long, Map> cacheMemberMap = (Map<Long, Map>) model.get("cacheMemberMap");
		if(cacheMemberMap==null){
			cacheMemberMap = new HashMap<Long, Map>();
			model.put("cacheMemberMap", cacheMemberMap);
		}
		Map singleInfo = memberService.getCacheMemberInfoMap(memberid);
		cacheMemberMap.put(memberid, singleInfo);
	}
}
