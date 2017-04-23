package com.gewara.web.action.api2.bbs;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.support.PropertyComparator;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import com.gewara.constant.ApiConstant;
import com.gewara.constant.TagConstant;
import com.gewara.model.user.Member;
import com.gewara.model.user.MemberInfo;
import com.gewara.support.ErrorCode;
import com.gewara.support.ServiceHelper;
import com.gewara.untrans.activity.SynchActivityService;
import com.gewara.web.action.api.ApiAuth;
import com.gewara.web.action.api.BaseApiController;
import com.gewara.web.filter.NewApiAuthenticationFilter;
import com.gewara.xmlbind.activity.RemoteActivity;
import com.gewara.xmlbind.activity.RemoteTreasure;
import com.gewara.xmlbind.bbs.Comment;

/**
 * 活动API
 * @author taiqichao
 *
 */
@Controller
public class Api2ActivityController extends BaseApiController{
	@Autowired@Qualifier("synchActivityService")
	private SynchActivityService synchActivityService;

	private String getCommonActivityDetail(Long activityid, String memberEncode, String isSimpleHtml, Integer width, Integer height, boolean isMovie,  ModelMap model){
		if(StringUtils.isBlank(activityid+""))return getErrorXmlView(model, ApiConstant.CODE_SIGN_ERROR, "activityId不能为空！");
		ErrorCode<RemoteActivity> code = synchActivityService.getRemoteActivity(activityid);
		if(!code.isSuccess())  return getErrorXmlView(model, ApiConstant.CODE_DATA_ERROR, code.getMsg());
		RemoteActivity activity = code.getRetval();
		Member member = null;
		if(StringUtils.isNotBlank(memberEncode)){
			member = memberService.getMemberByEncode(memberEncode);
			if(member==null) return getErrorXmlView(model, ApiConstant.CODE_SIGN_ERROR, "用户不存在！");
		}
		boolean isInterest = false;
		boolean isJoin = false;
		if(member!=null){
			//获取用户收藏的活动列表用于判断是否存在该活动，如果存在则isInterest为true
			List<String> operList = synchActivityService.memberOperActivityResult(activityid, member.getId());
			if(operList.size()>=2){
				isInterest = StringUtils.equals(operList.get(0), "true");
				isJoin = StringUtils.equals(operList.get(1), "true");
			}
		}
		//查询活动场馆ID
		if(activity!=null&&activity.getRelatedid()!=null){
			Object relate1 = null;
			Object relate2 = null;
			if(activity.getRelatedid()!=null){
				relate1 = relateService.getRelatedObject(activity.getTag(), activity.getRelatedid());
			}
			if(activity.getCategoryid()!=null){
				relate2 = relateService.getRelatedObject(activity.getCategory(), activity.getCategoryid());
			}
			model.put("relate1", relate1);
			model.put("relate2", relate2);
		}
		String body = activity.getContentdetail();
		String content = getSimpleHtmlContent(body, isSimpleHtml, width, height);
		if(!isMovie && StringUtils.isNotBlank(content)){
			content = content.replaceAll(" href=\"[^>]+\"", "");
		}
		model.put("activity", activity);
		model.put("isJoin", isJoin);
		model.put("content", content);
		model.put("isInterest", isInterest);
		List<RemoteActivity> activityList = new ArrayList<RemoteActivity>();
		activityList.add(activity);
		addCacheMember(model, ServiceHelper.getMemberIdListFromBeanList(activityList));
		return getXmlView(model, "api2/activity/activityDetail.vm");
	}
	/**
	 * 活动详情
	 * @param activityId
	 * @param memberEncode
	 * @param returnField
	 * @param model
	 * @param request
	 * @return
	 */
	@RequestMapping("/api2/activity/activityDetail.xhtml")
	public String activityDetail(Long activityid, String memberEncode, String isSimpleHtml, Integer width, Integer height,  ModelMap model){
		return getCommonActivityDetail(activityid, memberEncode, isSimpleHtml, width, height, false, model);
	}
	/**
	 * 电影版块活动详情
	 * @param activityId
	 * @param memberEncode
	 * @param returnField
	 * @param model
	 * @param request
	 * @return
	 */
	@RequestMapping("/api2/activity/getMovieActivityDetail.xhtml")
	public String getMovieActivityDetail(Long activityid,String memberEncode, String isSimpleHtml, Integer width, Integer height,  ModelMap model){
		return getCommonActivityDetail(activityid, memberEncode, isSimpleHtml, width, height, true, model);
	}
	/**
	 * 请求某个项目的活动列表 例如：(羽毛球)
	 * @param categoryid 当前请求项目的ID
	 * @param from 开始位置
	 * @param maxnum 每次读取的条数
	 * @param model
	 * @return
	 */
	@RequestMapping("/api2/activity/activityListByTime.xhtml")
	public String activityListByTime(String citycode, String tag, Long relatedid, String category, Long categoryid, Timestamp starttime, Timestamp endtime, String orderField, String asc, int from, int maxnum, ModelMap model){
		if(StringUtils.isBlank(citycode) || starttime==null || endtime==null) return getErrorXmlView(model, ApiConstant.CODE_SIGN_ERROR, "缺少参数！");
		List<RemoteActivity> activityList = new ArrayList<RemoteActivity>();
		ErrorCode<List<RemoteActivity>> code = synchActivityService.getActivityListByTime(citycode, null, starttime, endtime, null, tag, relatedid, category, categoryid, orderField, asc, from, maxnum);
		if(code.isSuccess()) activityList = code.getRetval();
		putRelateMap(activityList, model);
		return getXmlView(model, "api2/activity/activityList.vm");
	}

	@RequestMapping("/api2/activity/activityListByType.xhtml")
	public String activityListByTime(String citycode, String countycode, String tag, Long relatedid, 
			String category, Long categoryid, String atype, String datetype, Integer timetype, String isFee,
			String orderField, int from, int maxnum, ModelMap model){
		if(StringUtils.isBlank(citycode) || StringUtils.isBlank(tag)) return getErrorXmlView(model, ApiConstant.CODE_SIGN_ERROR, "缺少参数！");
		if(StringUtils.isBlank(orderField)) orderField = "addtime";
		List<RemoteActivity> activityList = new ArrayList<RemoteActivity>();
		ErrorCode<List<RemoteActivity>> code = synchActivityService.getActivityListByType(citycode, countycode, atype, datetype, timetype, tag, relatedid, category, categoryid, isFee, orderField, from, maxnum);
		if(code.isSuccess()) activityList = code.getRetval();
		putRelateMap(activityList, model);
		return getXmlView(model, "api2/activity/activityList.vm");
	}
	
	/**
	 * 请求某个场馆的活动列表 
	 * @param categoryid 当前请求项目的ID
	 * @param from 开始位置
	 * @param maxnum 每次读取的条数
	 * @param model
	 * @return
	 */
	@RequestMapping("/api2/activity/activityListByRelatedid.xhtml")
	public String activityListByTag(String citycode, String tag, Long relatedid, String category, Long categoryid, Timestamp starttime, Timestamp endtime, String orderField, String asc, int from, int maxnum, ModelMap model){
		if(StringUtils.isBlank(tag) || relatedid==null) return getErrorXmlView(model, ApiConstant.CODE_SIGN_ERROR, "缺少参数！");
		List<RemoteActivity> activityList = new ArrayList<RemoteActivity>();
		ErrorCode<List<RemoteActivity>> code = synchActivityService.getActivityListByTag(citycode, null, null, tag, relatedid, category, categoryid, starttime, endtime, orderField, asc, from, maxnum);
		if(code.isSuccess()) activityList = code.getRetval();
		putRelateMap(activityList, model);
		return getXmlView(model, "api2/activity/activityList.vm");
	}
	 
	/**
	 * 请求某个项目的活动数量：(羽毛球)
	 * @param categoryid 当前请求项目的ID
	 * @param from 开始位置
	 * @param maxnum 每次读取的条数
	 * @param model
	 * @return
	 */
	@RequestMapping("/api2/activity/activityCountByTime.xhtml")
	public String activityByTag(String citycode, String tag, Long relatedid, String category, Long categoryid, Timestamp starttime, Timestamp endtime, ModelMap model){
		if(starttime==null || endtime==null) return getErrorXmlView(model, ApiConstant.CODE_SIGN_ERROR, "缺少参数！");
		ErrorCode<Integer> code = synchActivityService.getActivityCountByTime(citycode, null, starttime, endtime, null, tag, relatedid, category, categoryid);
		if(!code.isSuccess()) return getErrorXmlView(model, ApiConstant.CODE_SIGN_ERROR, code.getMsg());
		Integer count = code.getRetval();
		model.put("count", count);
		return getSingleResultXmlView(model, count);
	}
	/**
	 * 根据活动ID集合查询活动信息
	 * @param ids 活动id集合
	 * @param model
	 * @return
	 */
	@RequestMapping("/api2/activity/activityListByIds.xhtml")
	public String activityList(String ids, ModelMap model){
		if(ids == null) return getErrorXmlView(model,  ApiConstant.CODE_PARAM_ERROR, "参数错误！");
		List<String> activityidList = Arrays.asList(StringUtils.split(ids, ","));
		List<Long> aidIds = new ArrayList<Long>();
		for (String aid : activityidList) {
			aidIds.add(Long.parseLong(aid));
		}
		List<RemoteActivity> activityList = new ArrayList<RemoteActivity>();
		if(activityidList.size() > 0) { 
			ErrorCode<List<RemoteActivity>> code = synchActivityService.getRemoteActivityListByIds(aidIds);
			if(code.isSuccess()) activityList = code.getRetval();
		}
		putRelateMap(activityList, model);
		return getXmlView(model, "api2/activity/activityList.vm"); 
	}
	
	/**
	 * 活动感兴趣的用户列表
	 * @param memberid 用户id
	 * @param tag 标签
	 * @param relatedid 关联id
	 * @param from 初始值
	 * @param maxnum 每页显示数
	 * @param model
	 * @return
	 */
	@RequestMapping("/api2/activity/interestedMemberList.xhtml")
	public String getMemberByInterested(Long activityid, String asc, int from, int maxnum, ModelMap model){
		ErrorCode<List<RemoteTreasure>> code = synchActivityService.getTreasureList(activityid, asc, from, maxnum);
		if(!code.isSuccess())  return getErrorXmlView(model,  ApiConstant.CODE_SIGN_ERROR, code.getMsg());
		List<RemoteTreasure> treasureList = code.getRetval();
		Map<Long, MemberInfo> infoMapList = new HashMap<Long, MemberInfo>();
		for(RemoteTreasure treasure : treasureList){
			infoMapList.put(treasure.getMemberid(), daoService.getObject(MemberInfo.class, treasure.getMemberid()));
		}
		model.put("treasureList", treasureList);
	    model.put("infoMapList", infoMapList);
		return getXmlView(model, "api2/activity/interestedMemberList.vm");
	}
	/**
	 * 参加活动用户列表
	 * @param activityid
	 * @param from 初始值
	 * @param maxnum 每页显示数
	 * @param model
	 * @return
	 */
	@RequestMapping("/api2/activity/joinMemberList.xhtml")
	public String joinMemberList(ModelMap model){
		return notSupport(model);
	}
	/**
	 * 感兴趣的活动
	 * @param memberEncode 用户
	 * @param tag 标签
	 * @param relatedid 关联id
	 * @param from 初始值
	 * @param maxnum 每页显示数
	 * @param model
	 * @return
	 */
	@RequestMapping("/api2/activity/collActivityList.xhtml")
	public String interestedActivity(ModelMap model){
		return notSupport(model);
	}
	/**
	 * 创建召集
	 * @param memberEncode
	 * @param sportItemId
	 * @param statarTime
	 * @param priceInfo
	 * @param sportId
	 * @param content
	 * @param pic
	 * @param model
	 * @return
	 */
	@RequestMapping("/api2/activity/addActivity.xhtml")
	public String saveActivity(ModelMap model){
		return notSupport(model);
	}
	
	/**
	 * 参加活动
	 * @param memberEncode
	 * @param activityId
	 * @param realname
	 * @param sex
	 * @param joinnum
	 * @param joindate
	 * @param mobile
	 * @param model
	 * @param request
	 * @return
	 */
	@RequestMapping("/api2/activity/joinActivity.xhtml")
	public String joinActivity(ModelMap model){
		return notSupport(model);
	}
	
	/**
	 * 取消参加活动
	 * @param memberEncode
	 * @param activityId
	 * @param model
	 * @param request
	 * @return
	 */
	@RequestMapping("/api2/activity/cancelJoinActivity.xhtml")
	public String cancelJoinActivity(ModelMap model){
		return notSupport(model);
	}
	
	/**
	 * 添加收藏
	 * @param memberEncode
	 * @param tag
	 * @param relatedid
	 * @param model
	 * @param request
	 * @return
	 */
	@RequestMapping("/api2/activity/addCollection.xhtml")
	public String addCollection(ModelMap model){
		return notSupport(model);
	}
	
	/**
	 * 取消收藏
	 * @param memberEncode
	 * @param tag
	 * @param relatedid
	 * @param request
	 * @param model
	 * @return
	 */
	@RequestMapping("/api2/activity/cancelCollection.xhtml")
	public String cancelCollection(ModelMap model){
		return notSupport(model);
	}
	
	/**
	 * 活动列表
	 * @param tag
	 * @param memberEncode
	 * @param citycode
	 * @param type
	 * @param returnField
	 * @param distance
	 * @param pointx
	 * @param pointy
	 * @param from
	 * @param maxnum
	 * @param model
	 * @param request
	 * @return
	 */
	@RequestMapping("/api2/activity/activityList.xhtml")
	public String activityList(String tag, Long relatedid, String category, Long categoryid, String citycode, int from,int maxnum, ModelMap model){
		ApiAuth auth = NewApiAuthenticationFilter.getApiAuth();
		if(StringUtils.isBlank(citycode))citycode = auth.getApiUser().getDefaultCity();
		if(StringUtils.isBlank(tag)) return getErrorXmlView(model, ApiConstant.CODE_SIGN_ERROR, "tag不能为空！");
		if(maxnum > 20) maxnum = 20;
		List<RemoteActivity> activityList = new ArrayList<RemoteActivity>();
		ErrorCode<List<RemoteActivity>> code = synchActivityService.getActivityList(citycode, null, null, tag, relatedid, category, categoryid, from, maxnum);
		if(code.isSuccess()){
			activityList = code.getRetval();
			Collections.sort(activityList, new PropertyComparator("activityStartTime", false, true));
		}
		putRelateMap(activityList, model);
		return getXmlView(model, "api2/activity/activityList.vm"); 
	}
	
	/**
	 * 根据用户查询活动
	 * @param memberEncode
	 * @param tag
	 * @param from
	 * @param maxnum
	 * @param model
	 * @return
	 */
	@RequestMapping("/api2/activity/memberActivityList.xhtml")
	public String memberidActivity(ModelMap model){
		return notSupport(model);
	}
	
	@RequestMapping("/api2/activity/memberJoinActivityList.xhtml")
	public String memberJoinActivityList(ModelMap model){
		return notSupport(model);
	}
	
	@RequestMapping("/api2/activity/getCommentListByActivityid.xhtml")
	public String memberidActivity(Long activityid, String orderField, String haveface, Integer from, Integer maxnum, ModelMap model){
		List<Comment> commentList = commentService.getCommentListByRelatedId(TagConstant.TAG_ACTIVITY,null, activityid, orderField, from, maxnum);
		getCommCommentData(model, commentList, haveface);
		return getXmlView(model, "api2/comment/commentList.vm");
	}
	
	@RequestMapping("/api2/activity/getActivityListBySignname.xhtml")
	public String getActivityListBySignname(String citycode, String signname, Integer from, Integer maxnum, ModelMap model){
		ErrorCode<List<RemoteActivity>> code = synchActivityService.getActivityListBySignname(citycode, signname, from, maxnum);
		List<RemoteActivity> activityList = new ArrayList<RemoteActivity>();
		if(code.isSuccess()) activityList = code.getRetval();
		putRelateMap(activityList, model);
		return getXmlView(model, "api2/activity/activityList.vm");
	}
	
	/**
	 * 活动详情
	 * @param activityId
	 * @param memberEncode
	 * @param returnField
	 * @param model
	 * @param request
	 * @return
	 */
	@RequestMapping("/api2/activity/operActivityResult.xhtml")
	public String activityDetail(ModelMap model){
		return notSupport(model);
	}
}
