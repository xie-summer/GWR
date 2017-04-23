package com.gewara.web.action.inner.mobile.activity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.support.PropertyComparator;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import com.gewara.constant.ApiConstant;
import com.gewara.constant.DiaryConstant;
import com.gewara.constant.Status;
import com.gewara.constant.TagConstant;
import com.gewara.constant.order.AddressConstant;
import com.gewara.model.bbs.Diary;
import com.gewara.model.goods.ActivityGoods;
import com.gewara.model.user.Agenda;
import com.gewara.model.user.Member;
import com.gewara.service.bbs.AgendaService;
import com.gewara.service.bbs.DiaryService;
import com.gewara.service.content.NewsService;
import com.gewara.support.ErrorCode;
import com.gewara.support.ServiceHelper;
import com.gewara.untrans.activity.SynchActivityService;
import com.gewara.util.BeanUtil;
import com.gewara.web.action.inner.OpenApiAuth;
import com.gewara.web.action.inner.mobile.BaseOpenApiMobileController;
import com.gewara.web.filter.OpenApiMobileAuthenticationFilter;
import com.gewara.xmlbind.activity.RemoteActivity;

@Controller
public class OpenApiMobileActivityController extends BaseOpenApiMobileController{
	@Autowired@Qualifier("synchActivityService")
	private SynchActivityService synchActivityService;
	@Autowired@Qualifier("newsService")
	private NewsService newsService;
	@Autowired@Qualifier("diaryService")
	private DiaryService diaryService;
	@Autowired@Qualifier("agendaService")
	private AgendaService agendaService;
	private String getCommonActivityDetail(Long activityid, String memberEncode, 
			String isSimpleHtml, Integer width, Integer height, boolean isMovie,  String appVersion, ModelMap model, HttpServletRequest request){
		if(StringUtils.isBlank(activityid+""))return getErrorXmlView(model, ApiConstant.CODE_SIGN_ERROR, "activityId不能为空！");
		ErrorCode<RemoteActivity> code = synchActivityService.getRemoteActivity(activityid);
		if(!code.isSuccess())  return getErrorXmlView(model, ApiConstant.CODE_DATA_ERROR, code.getMsg());
		RemoteActivity activity = code.getRetval();
		Member member = null;
		if(StringUtils.isNotBlank(memberEncode)){
			member = OpenApiMobileAuthenticationFilter.getOpenApiAuth().getMember();
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
		Map<String, Object> resMap = getActivityMap(activity);
		if(activity!=null&&activity.getRelatedid()!=null){
			if(activity.getRelatedid()!=null){
				Object relate1 = relateService.getRelatedObject(activity.getTag(), activity.getRelatedid());
				if(relate1!=null) {
					String placename = BeanUtil.getBeanMap(relate1).get("name")+"";
					String pointx = BeanUtil.getBeanMap(relate1).get("pointx")+"";
					String pointy = BeanUtil.getBeanMap(relate1).get("pointx")+"";
					resMap.put("placename", placename);
					resMap.put("pointx", pointx);
					resMap.put("pointy", pointy);
				}
			}
			if(activity.getCategoryid()!=null){
				Object relate2 = relateService.getRelatedObject(activity.getCategory(), activity.getCategoryid());
				if(relate2!=null) {
					String itemname = BeanUtil.getBeanMap(relate2).get("name")+"";
					resMap.put("itemname", itemname);
				}
			}
		}
		String body = activity.getContentdetail();
		String content = getSimpleHtmlContent(body, isSimpleHtml, width, height);
		if(!isMovie && StringUtils.isNotBlank(content)){
			content = content.replaceAll(" href=\"[^>]+\"", "");
		}
		int ispay = 0;
		if(StringUtils.equals(activity.getNeedprepay(), Status.Y)){
			List<ActivityGoods> goodsList = daoService.getObjectListByField(ActivityGoods.class, "relatedid", activity.getId());
			for(ActivityGoods goods : goodsList){
				if(isBooingActivityGoods(goods, appVersion)){
					ispay = 1;
				}
			}
		}
		resMap.put("ispay", ispay);
		resMap.put("isJoin", isJoin?1:0);
		resMap.put("isInterest", isInterest?1:0);
		resMap.put("content", content);
		initField(model, request);
		model.put("root", "activity");
		model.put("resMap", resMap);
		return getOpenApiXmlDetail(model);
	}
	/**
	 * 活动详情
	 */
	@RequestMapping("/openapi/mobile/activity/activityDetail.xhtml")
	public String activityDetail(Long activityid, String memberEncode, 
			String isSimpleHtml, Integer width, Integer height, String appVersion, ModelMap model, HttpServletRequest request){
		return getCommonActivityDetail(activityid, memberEncode, isSimpleHtml, width, height, false, appVersion, model, request);
	}
	/**
	 * 电影版块活动详情
	 */
	@RequestMapping("/openapi/mobile/activity/getMovieActivityDetail.xhtml")
	public String getMovieActivityDetail(Long activityid,String memberEncode, 
			String isSimpleHtml, Integer width, Integer height, String appVersion, ModelMap model, HttpServletRequest request){
		return getCommonActivityDetail(activityid, memberEncode, isSimpleHtml, width, height, true, appVersion, model, request);
	}
	/**
	 * 取消参加活动
	 */
	@RequestMapping("/openapi/mobile/activity/cancelJoinActivity.xhtml")
	public String cancelJoinActivity(Long activityid,ModelMap model){
		Member member = OpenApiMobileAuthenticationFilter.getOpenApiAuth().getMember();
		ErrorCode<RemoteActivity> code = synchActivityService.cancelActivity(activityid, member.getId());
		if(!code.isSuccess()) return getErrorXmlView(model, ApiConstant.CODE_DATA_ERROR, code.getMsg());
		return getSuccessXmlView(model);
	}
	/**
	 * 参加活动
	 */
	@RequestMapping("/openapi/mobile/activity/joinActivity.xhtml")
	public String joinActivity(Long activityid,
			String realname,Integer sex,Integer joinnum, Date joindate, String mobile,ModelMap model){
		OpenApiAuth auth = OpenApiMobileAuthenticationFilter.getOpenApiAuth();
		if(activityid == null){
			return getErrorXmlView(model, ApiConstant.CODE_SIGN_ERROR, "activityid不能为空！");
		}
		Member member = OpenApiMobileAuthenticationFilter.getOpenApiAuth().getMember();
		String strsex = null;
		if(sex!=null) strsex = sex==0?"男":"女";
		ErrorCode<RemoteActivity> code = synchActivityService.joinActivity(member.getId(), activityid, strsex, realname, mobile, joinnum, joindate, AddressConstant.getApiAddress(auth.getApiUser().getId()));
		if(!code.isSuccess()) return getErrorXmlView(model, ApiConstant.CODE_SIGN_ERROR, code.getMsg());
		return getSuccessXmlView(model);
	}
	
	//电影最近一周即将开始和进行中活动
	@RequestMapping("/openapi/mobile/activity/movieAweekActivityList.xhtml")
	public String activityList(String citycode, String atype, String dateType, String tag, 
			Long relatedid, String category, Long categoryid, int from, int maxnum, ModelMap model, HttpServletRequest request){
		List<RemoteActivity> activityList = getActivityList(citycode, atype, dateType, null, tag, relatedid, category, categoryid);
		activityList = BeanUtil.getSubList(activityList, from, maxnum);
		List<Map<String, Object>> resMapList = new ArrayList<Map<String, Object>>();
		for(RemoteActivity activity : activityList){
			Map<String, Object> resMap = getActivityMap(activity);
			if(activity.getRelatedid()!=null){
				Object relate1 = relateService.getRelatedObject(activity.getTag(), activity.getRelatedid());
				if(relate1!=null) {
					String placename = BeanUtil.getBeanMap(relate1).get("name")+"";
					String pointx = BeanUtil.getBeanMap(relate1).get("pointx")+"";
					String pointy = BeanUtil.getBeanMap(relate1).get("pointx")+"";
					resMap.put("placename", placename);
					resMap.put("pointx", pointx);
					resMap.put("pointy", pointy);
				}
			}
			resMapList.add(resMap);
		}
		initField(model, request);
		putActivityListNode(model);
		model.put("resMapList", resMapList);
		return getOpenApiXmlList(model);
	}
	
	private List<RemoteActivity> getActivityList(String citycode, String atype, String dateType, String isFee, String tag, Long relatedid, String category, Long categoryid){
		List<RemoteActivity> activityList = new ArrayList<RemoteActivity>();
		List<RemoteActivity> qryActivityList = new ArrayList<RemoteActivity>();
		
		if(StringUtils.isBlank(dateType))  dateType = RemoteActivity.DATETYPE_AWEEK;
		ErrorCode<List<RemoteActivity>> code = synchActivityService.getActivityListByDatetype(citycode, atype, dateType, isFee, tag, relatedid, category, categoryid, 0, 400);
		if(code.isSuccess()) qryActivityList = code.getRetval();
		for(RemoteActivity activity : qryActivityList){
			if(isValidMobileActivity(activity)) { 
				activityList.add(activity);
			}
		}
		Collections.sort(activityList, new PropertyComparator("addtime", false, false));
		
		if(relatedid==null && categoryid==null){
			List<Long> activityidList = BeanUtil.getBeanPropertyList(activityList, Long.class, "id", true);
			List<RemoteActivity> signnameActivityList = new ArrayList<RemoteActivity>();
			ErrorCode<List<RemoteActivity>> code2 = synchActivityService.getActivityListBySignname(citycode, "activity_mobile", 0, 20);
			if(code2.isSuccess()) signnameActivityList = code2.getRetval();
			int i = 0;
			for(RemoteActivity activity : signnameActivityList){
				if(activity.isPlaying()){
					if(activityidList.contains(activity.getId())){
						activityList.remove(activity);
						activityList.add(i, activity);
						i++;
					}else {
						activityList.add(i, activity);
						i++;
					}
				}
			}
		}
		return activityList;
	}
	private boolean isValidMobileActivity(RemoteActivity activity){
		if(StringUtils.equals(activity.getSign(), RemoteActivity.SIGN_PRICE5)) return false;
		if(StringUtils.equals(activity.getSign(), RemoteActivity.SIGN_PUBSALE)) return false;
		if(StringUtils.equals(activity.getSign(), RemoteActivity.SIGN_RESERVE)) return false;
		//if(StringUtils.equals(activity.getOnlinePay(), "Y")) return false;
		if(StringUtils.isNotBlank(activity.getUsePoint())) return false;
		//if(StringUtils.isNotBlank(activity.getJoinForm())) return false;
		return true;
	}
	
	@RequestMapping("/openapi/mobile/relateCount.xhtml")
	public String movieRelateCount(String citycode, String fields, String tag, Long relatedid, ModelMap model){
		if(StringUtils.isBlank(citycode) || StringUtils.isBlank(fields) || StringUtils.isBlank(tag) || relatedid==null){
			return getErrorXmlView(model, ApiConstant.CODE_DATA_ERROR, "缺少参数");
		}
		int newsCount=0, activityCount=0, commentCount=0, diaryCount=0, pictureCount=0;
		List<String> filedList = Arrays.asList(fields.split(","));
		if(filedList.contains("newsCount")){
			newsCount = newsService.getNewsCount(citycode, tag, null, relatedid, null);
		}
		String tag1 = null, category1 = null; Long relatedid1=null, categoryid1 = null;
		boolean isatag = false;
		if(ServiceHelper.isTag(tag)){
			tag1 = tag;
			isatag = true;
			relatedid1 = relatedid;
		}else if(ServiceHelper.isCategory(tag)){
			category1 = tag;
			categoryid1 = relatedid;
			isatag = true;
		}else {
			activityCount = 0;
		}
		if(isatag){
			if(filedList.contains("activityCount")){
				ErrorCode<Integer> code = synchActivityService.getCurrActivityCount(citycode, null, null, tag1, relatedid1, category1, categoryid1, null, null);
				if(code.isSuccess()) activityCount = code.getRetval();
			}
		}
		if(filedList.contains("commentCount")){
			commentCount = commentService.getCommentCountByRelatedId(TagConstant.TAG_MOVIE, relatedid);
		}
		if(filedList.contains("diaryCount")){
			String type = null;
			if(StringUtils.equals(tag, TagConstant.TAG_MOVIE)) type = DiaryConstant.DIARY_TYPE_COMMENT;
			diaryCount = diaryService.getDiaryCount(Diary.class, citycode, type, TagConstant.TAG_MOVIE, relatedid);
		}
		if(filedList.contains("pictureCount")){
			pictureCount = pictureService.getPictureCountByRelatedid(TagConstant.TAG_MOVIE, relatedid);
			pictureCount = pictureCount + pictureService.getMemberPictureCount(relatedid, TagConstant.TAG_MOVIE, null, TagConstant.FLAG_PIC, Status.Y);
		}
		model.put("newsCount", newsCount);
		model.put("activityCount", activityCount);
		model.put("commentCount", commentCount);
		model.put("diaryCount", diaryCount);
		model.put("pictureCount", pictureCount);
		return getXmlView(model, "api2/mobile/relateCount.vm");
	}
	
	@RequestMapping("/openapi/mobile/agenda/getAgenadaListByActionid.xhtml")
	public String getAgenadaListByActionid(Long actionid, ModelMap model){
		Map<Long, Object> relateMap = new HashMap<Long, Object>();
		Map<Long, Object> categoryMap = new HashMap<Long, Object>();
		List<Agenda> agendaList = agendaService.getAgendaList(actionid);
		for(Agenda agenda : agendaList){
			if(agenda.getRelatedid() != null && StringUtils.isNotBlank(agenda.getTag())){
				Object relate = relateService.getRelatedObject(agenda.getTag(), agenda.getRelatedid());
				relateMap.put(agenda.getId(), relate);
			}
			if(agenda.getCategoryid() != null && StringUtils.isNotBlank(agenda.getCategory())){
				Object relate = relateService.getRelatedObject(agenda.getCategory(), agenda.getCategoryid());
				categoryMap.put(agenda.getId(), relate);
			}
		}
		model.put("relateMap", relateMap);
		model.put("categoryMap", categoryMap);
		model.put("agendaList", agendaList);
		addCacheMember(model, ServiceHelper.getMemberIdListFromBeanList(agendaList));
		return getXmlView(model, "api2/comment/agendaList.vm");
	}
}
