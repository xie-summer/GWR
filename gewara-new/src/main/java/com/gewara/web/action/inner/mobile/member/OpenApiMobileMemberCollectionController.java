package com.gewara.web.action.inner.mobile.member;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import com.gewara.constant.ApiConstant;
import com.gewara.constant.TagConstant;
import com.gewara.constant.sys.MongoData;
import com.gewara.json.MemberStats;
import com.gewara.json.PlayItemMessage;
import com.gewara.model.common.BaseEntity;
import com.gewara.model.movie.Movie;
import com.gewara.model.user.Member;
import com.gewara.model.user.Treasure;
import com.gewara.service.content.RecommendService;
import com.gewara.service.member.TreasureService;
import com.gewara.untrans.MemberCountService;
import com.gewara.untrans.WalaApiService;
import com.gewara.untrans.activity.SynchActivityService;
import com.gewara.web.action.inner.mobile.BaseOpenApiController;
import com.gewara.web.filter.OpenApiMobileAuthenticationFilter;
@Controller
public class OpenApiMobileMemberCollectionController extends BaseOpenApiController {
	@Autowired@Qualifier("treasureService")
	private TreasureService treasureService;
	@Autowired@Qualifier("recommendService")
	private RecommendService recommendService;
	@Autowired@Qualifier("walaApiService")
	private WalaApiService walaApiService;
	@Autowired@Qualifier("memberCountService")
	private MemberCountService memberCountService;
	@Autowired@Qualifier("synchActivityService")
	private SynchActivityService synchActivityService;
	/**
	 * 用户对一个场馆是否关注
	 */
	@RequestMapping("/openapi/mobile/member/isCollection.xhtml")
	public String isCollection(String tag, Long relatedid,ModelMap model){
		Member member = OpenApiMobileAuthenticationFilter.getOpenApiAuth().getMember();
		Treasure treasure = treasureService.getTreasureByTagMemberidRelatedid(tag, member.getId(), relatedid, "collect");
		int result = 0;
		if(treasure != null) result = 1;
		return getSingleResultXmlView(model, result);
	}
	/**
	 * 对场馆或电影加关注
	 */
	@RequestMapping("/openapi/mobile/member/addCollection.xhtml")
	public String addCollection(ModelMap model, 
			String tag, Long relatedid,String isPush,String osType,String appVersion){
		Member member = OpenApiMobileAuthenticationFilter.getOpenApiAuth().getMember();
		if(member == null) return getErrorXmlView(model, ApiConstant.CODE_MEMBER_NOT_EXISTS, "用户不存在！");
		Treasure treasure = treasureService.getTreasureByTagMemberidRelatedid(tag, member.getId(), relatedid, "collect");
		if(StringUtils.equals(tag,"member")){
			if(treasure != null) return getErrorXmlView(model, ApiConstant.CODE_DATA_ERROR, "该用户你已关注过！");
			Member myMember = daoService.getObject(Member.class, relatedid);
			if(myMember==null)return getErrorXmlView(model, ApiConstant.CODE_PARAM_ERROR, "传递参数错误！");
			if(StringUtils.equals(member.getId()+"", relatedid+"")) return getErrorXmlView(model, ApiConstant.CODE_DATA_ERROR, "不能关注自己！"); 
			//关注，粉丝数加一
			memberCountService.updateMemberCount(member.getId(), MemberStats.FIELD_ATTENTIONCOUNT, 1, true);
			//关注，粉丝数加一
			memberCountService.updateMemberCount(relatedid, MemberStats.FIELD_FANSCOUNT, 1, true);
			//添加一条粉丝通知
			recommendService.memberAddFansCount(relatedid, MongoData.MESSAGE_FANS_ADD, MongoData.MESSAGE_FANS, 1);
		}else if(StringUtils.equals(tag,TagConstant.TAG_ACTIVITY)){
			if(treasure != null) return getErrorXmlView(model, ApiConstant.CODE_DATA_ERROR, "该活动你已关注过！");
			synchActivityService.addClickedtimes(relatedid);
		}else if(StringUtils.equals(tag,TagConstant.TAG_GYM)){
			if(treasure != null) return getErrorXmlView(model, ApiConstant.CODE_DATA_ERROR, "该场馆你已关注过！");
		}else{
			if(treasure != null) return getErrorXmlView(model, ApiConstant.CODE_DATA_ERROR, "该项目你已关注过！");
			BaseEntity relate = (BaseEntity) relateService.getRelatedObject(tag, relatedid);
			if(relate == null) return getErrorXmlView(model, ApiConstant.CODE_PARAM_ERROR, "收藏的项目不存在！");
			if(StringUtils.equals("movie", tag) && StringUtils.equals("Y", isPush)){
				this.savePlayItemMessage(member.getId(), (Movie)relate, osType.toLowerCase(), appVersion);
			}
			relate.addCollection();
			daoService.saveObject(relate);
		}
		treasure = new Treasure(member.getId(), tag, relatedid, Treasure.ACTION_COLLECT);
		walaApiService.addTreasure(treasure);
		daoService.saveObject(treasure);
		return getSuccessXmlView(model);
	}
	private void savePlayItemMessage(long memberId, Movie movie,String osType,String appVersion){
		String msg = "温馨提示：电影《" + movie.getName() + "》已开放售票，欢迎购票观看。#version#" + appVersion;
		nosqlService.addPlayItemMessage(memberId, "cinema", null, movie.getReleasedate(), movie.getId(), null, osType, msg);
	}
	
	/**
	 * 取消关注
	 */
	@RequestMapping("/openapi/mobile/member/cancelCollection.xhtml")
	public String collectDel(String tag, Long relatedid, ModelMap model){
		if(StringUtils.isBlank(tag) || relatedid==null)
			return getErrorXmlView(model, ApiConstant.CODE_PARAM_ERROR, "传递参数错误！");
		Member member = OpenApiMobileAuthenticationFilter.getOpenApiAuth().getMember();
		if(tag.equals("member")){
			Member myMember = daoService.getObject(Member.class, relatedid);
			if(myMember==null)return getErrorXmlView(model, ApiConstant.CODE_PARAM_ERROR, "传递参数错误！");
		}
		if(StringUtils.equals(member.getId()+"", relatedid+"")) return getErrorXmlView(model, ApiConstant.CODE_DATA_ERROR, "操作异常"); 
		Treasure treasure = treasureService.getTreasureByTagMemberidRelatedid(tag, member.getId(), relatedid, Treasure.ACTION_COLLECT);
		if(treasure==null) {
			return getErrorXmlView(model, ApiConstant.CODE_DATA_ERROR, "找不到记录！");
		}
		walaApiService.delTreasure(treasure.getMemberid(), treasure.getRelatedid(), treasure.getTag(), treasure.getAction());
		daoService.removeObject(treasure);
		if(tag.equals(TagConstant.TAG_ACTIVITY)){
			addCollect(relatedid,-1);
		}else if(StringUtils.equals(TagConstant.TAG_MOVIE, tag)){
			cancelPlayItemMessage(member.getId(),relatedid);
		}else if(StringUtils.equals(tag, "member")){
			memberCountService.updateMemberCount(member.getId(), MemberStats.FIELD_ATTENTIONCOUNT, 1, false);
			memberCountService.updateMemberCount(relatedid, MemberStats.FIELD_FANSCOUNT, 1, false);
		}
		return getSuccessXmlView(model);
	}
	private void addCollect(long relatedid,int num){
		Map<String, Object> paramMap = new HashMap<String, Object>();
		paramMap.put(MongoData.SYSTEM_ID, relatedid);
		paramMap.put("tag", TagConstant.TAG_CONACTIVITY);
		Map<String, Object> map = this.mongoService.findOne(MongoData.NS_SIGN, paramMap);
		if(map == null){
			map = new HashMap();
			map.put(MongoData.SYSTEM_ID, relatedid);
			map.put("count", 0);
		}
		map.put("count", new Integer((map.get("count")+""))+num);
		map.put("tag",  TagConstant.TAG_CONACTIVITY);
		mongoService.saveOrUpdateMap(map, MongoData.SYSTEM_ID, MongoData.NS_SIGN);
	}
	private String cancelPlayItemMessage(long memberId,long movieId){
		Map params = new HashMap();
		params.put("tag", "cinema");
		params.put("categoryid", movieId);
		params.put("memberid", memberId);
		List<PlayItemMessage> playItemList = mongoService.find(PlayItemMessage.class, params);
		if (!playItemList.isEmpty()){
			mongoService.removeObjectList(playItemList, MongoData.DEFAULT_ID_NAME);
		}
		return "success";
	}
}
