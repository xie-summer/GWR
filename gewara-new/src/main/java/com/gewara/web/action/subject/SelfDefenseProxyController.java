package com.gewara.web.action.subject;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import com.gewara.constant.Status;
import com.gewara.constant.sys.MongoData;
import com.gewara.model.user.Member;
import com.gewara.mongo.MongoService;
import com.gewara.util.DateUtil;
import com.gewara.util.ObjectId;
import com.gewara.util.StringUtil;
import com.gewara.web.action.AnnotationController;

// 女子防狼术(专题代理)
@Controller
public class SelfDefenseProxyController  extends AnnotationController {
	
	@Autowired@Qualifier("mongoService")
	private MongoService mongoService;
	
	@RequestMapping("/subject/proxy/selfDefense/memberVote.xhtml")
	public String memberVote(Long memberid, String check, String type, String tag, String style, ModelMap model){
		String checkcode = StringUtil.md5(memberid + "235yh8hsd8f804");
		if(!StringUtils.equals(check, checkcode)) return showJsonSuccess(model, "请先登录。");
		Member member = daoService.getObject(Member.class, memberid);
		if(member == null) return showJsonSuccess(model, "请先登录。");
		if(StringUtils.isBlank(type) || StringUtils.isBlank(tag) || StringUtils.isBlank(style)) return showJsonSuccess(model, "此活动未开始或已结束。");
		Map params = new HashMap();
		params.put(MongoData.GEWA_CUP_MEMBERID, memberid);
		params.put(MongoData.ACTION_TYPE, type);
		params.put(MongoData.ACTION_TAG, tag);
		Map voteMap = mongoService.findOne(MongoData.NS_ACTIVITY_COMMON_MEMBER, params);
		if(voteMap == null){
			voteMap = new HashMap();
			voteMap.putAll(params);
			voteMap.put(MongoData.ACTION_ADDTIME, DateUtil.currentTime());
			voteMap.put(MongoData.SYSTEM_ID, ObjectId.uuid());
			voteMap.put(style, Status.Y);
		}else if(voteMap.get(style) != null){
			return showJsonSuccess(model, "此攻略招式你已支持过。");
		}else{
			voteMap.put(style, Status.Y);
		}
		mongoService.saveOrUpdateMap(voteMap, MongoData.SYSTEM_ID, MongoData.NS_ACTIVITY_COMMON_MEMBER);
		return showJsonSuccess(model);
	}
	
	@RequestMapping("/subject/proxy/selfDefense/getVoteCount.xhtml")
	public String getVoteCount(String type, String tag, String style, ModelMap model){
		if(StringUtils.isBlank(type) || StringUtils.isBlank(tag)) return showJsonSuccess(model, "此活动未开始或已结束。");
		Map params = new HashMap();
		params.put(MongoData.ACTION_TYPE, type);
		params.put(MongoData.ACTION_TAG, tag);
		if(StringUtils.isNotBlank(style)) params.put(style, Status.Y);
		int count = mongoService.getCount(MongoData.NS_ACTIVITY_COMMON_MEMBER, params);
		return showJsonSuccess(model, count+"");
	}
	
}