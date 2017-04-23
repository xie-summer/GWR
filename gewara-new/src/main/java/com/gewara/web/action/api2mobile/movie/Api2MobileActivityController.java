package com.gewara.web.action.api2mobile.movie;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
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
import com.gewara.constant.ticket.OpiConstant;
import com.gewara.helper.ticket.CloseRuleOpiFilter;
import com.gewara.helper.ticket.OpiFilter;
import com.gewara.model.api.ApiUser;
import com.gewara.model.common.BaseEntity;
import com.gewara.model.movie.Cinema;
import com.gewara.model.movie.Movie;
import com.gewara.model.movie.MoviePlayItem;
import com.gewara.model.partner.PartnerCloseRule;
import com.gewara.model.ticket.OpenPlayItem;
import com.gewara.service.partner.PartnerService;
import com.gewara.support.ErrorCode;
import com.gewara.support.MultiPropertyComparator;
import com.gewara.untrans.activity.SynchActivityService;
import com.gewara.util.BeanUtil;
import com.gewara.web.action.api.BaseApiController;
import com.gewara.web.filter.NewApiAuthenticationFilter;
import com.gewara.xmlbind.activity.RemoteActivity;
import com.gewara.xmlbind.activity.RemoteActivityMpi;
@Controller
public class Api2MobileActivityController extends BaseApiController{
	@Autowired@Qualifier("synchActivityService")
	private SynchActivityService synchActivityService;
	@Autowired@Qualifier("partnerService")
	protected PartnerService partnerService;
	private boolean isValidMobileActivity(RemoteActivity activity){
		if(StringUtils.equals(activity.getSign(), RemoteActivity.SIGN_PRICE5)) return false;
		if(StringUtils.equals(activity.getSign(), RemoteActivity.SIGN_PUBSALE)) return false;
		if(StringUtils.equals(activity.getSign(), RemoteActivity.SIGN_RESERVE)) return false;
		if(StringUtils.equals(activity.getOnlinePay(), "Y")) return false;
		if(StringUtils.isNotBlank(activity.getUsePoint())) return false;
		if(StringUtils.isNotBlank(activity.getJoinForm())) return false;
		//if(StringUtils.isNotBlank(activity.getPriceinfo()) && activity.getPriceinfo().compareTo("0")>0) return false;
		return true;
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
	private void getRelateData(List<RemoteActivity> activityList, ModelMap model){
		if(activityList==null) return;
		Map<Long, Object> relateMap = new HashMap<Long, Object>();
		for(RemoteActivity activity : activityList) {
			if(StringUtils.isNotBlank(activity.getTag()) && activity.getRelatedid()!=null){
				Object object = relateService.getRelatedObject(activity.getTag(), activity.getRelatedid());
				if(object!=null) relateMap.put(activity.getId(), object);
			}
		}
		model.put("relateMap", relateMap);
	}
	//电影最近一周即将开始和进行中活动
	@RequestMapping("/api2/mobile/movie/activityList.xhtml")
	public String activityList(String citycode, String atype, String dateType, String isFee, String tag, Long relatedid, String category, Long categoryid, int from, int maxnum, ModelMap model){
		List<RemoteActivity> activityList = getActivityList(citycode, atype, dateType, isFee, tag, relatedid, category, categoryid);
		activityList = BeanUtil.getSubList(activityList, from, maxnum);
		getRelateData(activityList, model);
		model.put("activityList", activityList);
		return getXmlView(model, "api2/movie/activity/activityList.vm");
	}
	//电影最近一周的活动列表
	@RequestMapping("/api2/mobile/movie/activityCount.xhtml")
	public String activityCount(String citycode, String atype, String dateType, String isFee, String tag, Long relatedid, String category, Long categoryid, ModelMap model){
		List<RemoteActivity> activityList = getActivityList(citycode, atype, dateType, isFee, tag, relatedid, category, categoryid);
		return getSingleResultXmlView(model, activityList.size());
	}
	//电影活动关联的场次，场次关联影院
	@RequestMapping("/api2/mobile/movie/cinemaListByActivityOpi.xhtml")
	public String cinemaListByActivityOpi(Long activityid, ModelMap model){
		if(StringUtils.isBlank(activityid+""))return getErrorXmlView(model, ApiConstant.CODE_SIGN_ERROR, "activityId不能为空！");
		ErrorCode<RemoteActivity> code = synchActivityService.getRemoteActivity(activityid);
		if(!code.isSuccess())  return getErrorXmlView(model, ApiConstant.CODE_DATA_ERROR, code.getMsg());
		List<String> mpidList = synchActivityService.getActivityMpidList(activityid);
		List<OpenPlayItem> opiList = new ArrayList<OpenPlayItem>();
		for(String mpid : mpidList){
			OpenPlayItem opi = daoService.getObjectByUkey(OpenPlayItem.class, "mpid", Long.valueOf(mpid));
			if(opi!=null && !opi.isExpired() && opi.isOpenToPartner()) opiList.add(opi);
		}
		List<Long> cinemaidList = BeanUtil.getBeanPropertyList(opiList, Long.class, "cinemaid", true);
		List<Cinema> cinemaList = daoService.getObjectList(Cinema.class, cinemaidList);
		model.put("cinemaList", cinemaList);
		model.put("generalmarkMap", getGeneralmarkMap(new HashSet<BaseEntity>(cinemaList)));
		return getXmlView(model, "api2/mobile/cinemaList.vm");
	}
	//电影活动中的场次
	@RequestMapping("/api2/mobile/movie/activityOpiList.xhtml")
	public String activityList(Long activityid, Long cinemaid, ModelMap model){
		if(activityid==null || cinemaid==null)return getErrorXmlView(model, ApiConstant.CODE_SIGN_ERROR, "参数不能为空！");
		ErrorCode<RemoteActivity> code = synchActivityService.getRemoteActivity(activityid);
		if(!code.isSuccess())  return getErrorXmlView(model, ApiConstant.CODE_DATA_ERROR, code.getMsg());
		//List<String> mpidList = synchActivityService.getActivityMpidList(activityid);
		ErrorCode<List<RemoteActivityMpi>> code2 = synchActivityService.getRemoteActiviyMpiList(activityid);
		List<RemoteActivityMpi> ampiList = new ArrayList<RemoteActivityMpi>();
		if(code2.isSuccess()) ampiList = code2.getRetval();
		Map<Long, OpenPlayItem> opiMap = new HashMap<Long, OpenPlayItem>();
		Map<Long, String> reasonMap = new HashMap<Long, String>();
		List<MoviePlayItem> playItemList = new ArrayList<MoviePlayItem>();
		Map<Long, Cinema> cinemaMap = new HashMap<Long, Cinema>();
		Map<Long, Movie> movieMap = new HashMap<Long, Movie>();
		Map<Long, String> guestMap = new HashMap<Long, String>();
		ApiUser partner = NewApiAuthenticationFilter.getApiAuth().getApiUser();
		List<PartnerCloseRule> pcrList = partnerService.getCloseRuleList();
		OpiFilter filter = new CloseRuleOpiFilter(partner, pcrList);
		for(RemoteActivityMpi ampi : ampiList){
			MoviePlayItem mpi = daoService.getObject(MoviePlayItem.class, ampi.getMpid());
			if(mpi!=null && cinemaid.equals(mpi.getCinemaid())){
				OpenPlayItem opi=daoService.getObjectByUkey(OpenPlayItem.class, "mpid", mpi.getId(), false);
				if(opi!=null && !opi.isExpired() && opi.isOpenToPartner()) {
					if(filter.excludeOpi(opi)) opi = null;
					playItemList.add(mpi);
					opiMap.put(mpi.getId(),opi);
					reasonMap.put(mpi.getId(), OpiConstant.getUnbookingReason(opi));
					if(!cinemaMap.containsKey(mpi.getCinemaid())){
						cinemaMap.put(mpi.getCinemaid(), daoService.getObject(Cinema.class, mpi.getCinemaid()));
					}
					if(!movieMap.containsKey(mpi.getMovieid())){
						movieMap.put(mpi.getMovieid(), daoService.getObject(Movie.class, mpi.getMovieid()));
					}
				}
			}
			guestMap.put(ampi.getMpid(), ampi.getGuest());
		}
		model.put("opiMap", opiMap);
		model.put("reasonMap", reasonMap);
		model.put("movieMap", movieMap);
		model.put("cinemaMap", cinemaMap);
		model.put("guestMap", guestMap);
		model.put("curMpiList", playItemList);
		Collections.sort(playItemList, new MultiPropertyComparator(new String[]{"playdate" ,"playtime"}, new boolean[]{true, true}));
		return getXmlView(model, "api2/movie/movie/mpiList.vm");
	}
}
