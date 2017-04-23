package com.gewara.untrans.activity.impl;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.stereotype.Service;

import com.gewara.constant.AdminCityContant;
import com.gewara.constant.ApiConstant;
import com.gewara.constant.sys.CacheConstant;
import com.gewara.constant.sys.HttpTimeout;
import com.gewara.model.content.GewaCommend;
import com.gewara.model.user.Member;
import com.gewara.support.ErrorCode;
import com.gewara.untrans.AbstractSynchBaseService;
import com.gewara.untrans.CacheService;
import com.gewara.untrans.activity.SynchActivityService;
import com.gewara.util.ApiUtils;
import com.gewara.util.BeanUtil;
import com.gewara.util.DateUtil;
import com.gewara.util.HttpResult;
import com.gewara.web.action.inner.util.ActivityRemoteUtil;
import com.gewara.xmlbind.BaseObjectResponse;
import com.gewara.xmlbind.DataWrapper;
import com.gewara.xmlbind.activity.CategoryCount;
import com.gewara.xmlbind.activity.CategoryCountList;
import com.gewara.xmlbind.activity.CountyCount;
import com.gewara.xmlbind.activity.CountyCountList;
import com.gewara.xmlbind.activity.RemoteActivity;
import com.gewara.xmlbind.activity.RemoteActivityList;
import com.gewara.xmlbind.activity.RemoteActivityMpi;
import com.gewara.xmlbind.activity.RemoteActivityMpiList;
import com.gewara.xmlbind.activity.RemoteApplyjoin;
import com.gewara.xmlbind.activity.RemoteApplyjoinList;
import com.gewara.xmlbind.activity.RemoteTreasure;
import com.gewara.xmlbind.activity.RemoteTreasureList;

@Service("synchActivityService")
public class SynchActivityServiceImpl extends AbstractSynchBaseService implements SynchActivityService, InitializingBean {
	@Autowired@Qualifier("hibernateTemplate")
	private HibernateTemplate hibernateTemplate;
	@Autowired@Qualifier("cacheService")
	private CacheService cacheService;
	
	private String activityApiUrl;
	@Override
	public void afterPropertiesSet() throws Exception {
		activityApiUrl = config.getString("activityApiUrl");
		
	}

	protected ErrorCode<Integer> getCommonActivityCount(String title, String citycode, String countycode, String atype, String status, Long memberid, 
			Integer timetype, String flag, 
			String tag, List<Long> relatedidList,
			String category, List<Long> categoryidList,
			Timestamp starttime, Timestamp endtime, 
			List<Long> activityIdList,String sources,String isFee) {
		String url = activityApiUrl + ActivityRemoteUtil.getActivityCountUrl();
		Map<String, String> params = new HashMap<String, String>();
		params.putAll(getQryMap(title, citycode, countycode, atype, status, memberid, null, timetype, flag, tag, relatedidList, category, categoryidList, 
				starttime, endtime, activityIdList, sources, isFee));
		HttpResult result = getRequestResult(url, params, HttpTimeout.SHORT_REQUEST);
		return getRemoteCount(result);
	}
	
	protected ErrorCode<List<RemoteActivity>> getCommonActivityList(String citycode, String atype, Long memberid,
			Integer timetype, String flag, 
			String tag, List<Long> relatedidList,
			String category, List<Long> categoryidList,
			Timestamp starttime, Timestamp endtime, 
			Integer from, Integer maxnum) {
		return getCommonActivityList(null, citycode, null, atype, null, memberid, null, timetype, flag, tag, relatedidList, category, categoryidList, starttime, endtime, null, null, null, null, "Y", from, maxnum);
	}
	protected ErrorCode<List<RemoteActivity>> getCommonActivityList(String citycode, String atype, Long memberid,
			Integer timetype, String flag, 
			String tag, Long relatedid,
			String category, Long categoryid,
			Timestamp starttime, Timestamp endtime, 
			Integer from, Integer maxnum) {
		return getActivityList(null, citycode, atype, null, memberid, timetype, flag, tag, relatedid, category, categoryid, starttime, endtime, null, null, null, null, "N", from, maxnum);
	}
	protected ErrorCode<List<RemoteActivity>> getCommonActivityListByTimetype(String citycode, String atype,
			Integer timetype, String flag, 
			String tag, Long relatedid,
			String category, Long categoryid,
			Integer from, Integer maxnum) {
		if(timetype == null) return ErrorCode.getFailure(ApiConstant.CODE_SIGN_ERROR, "timetype不能为空！");
		return getActivityList(null, citycode, atype, null, null, timetype, flag, tag, relatedid, category, categoryid, null, null, null, null, null, null, "N", from, maxnum);
	}
	protected ErrorCode<List<RemoteActivity>> getCommonActivityListByTime(String citycode, String atype, String flag, 
			String tag, Long relatedid,
			String category, Long categoryid, Timestamp starttime, Timestamp endtime, 
			Integer from, Integer maxnum) {
		if(starttime == null || endtime==null) return ErrorCode.getFailure(ApiConstant.CODE_SIGN_ERROR, "starttime 或者 endtime 不能为空！");
		return getActivityList(null, citycode, atype, null, null, null, flag, tag, relatedid, category, categoryid, starttime, endtime, null, null, null, null, "N", from, maxnum);
	}
	protected ErrorCode<List<RemoteActivity>> getCommonActivityListByTime(String citycode, String atype, String flag, 
			String tag, Long relatedid,
			String category, Long categoryid, Timestamp starttime, Timestamp endtime, String order, String asc,
			Integer from, Integer maxnum) {
		if(starttime == null || endtime==null) return ErrorCode.getFailure(ApiConstant.CODE_SIGN_ERROR, "starttime 或者 endtime 不能为空！");
		return getActivityList(null, citycode, atype, null, null, null, flag, tag, relatedid, category, categoryid, starttime, endtime, null, null, null, order, asc, from, maxnum);
	}
	protected ErrorCode<List<RemoteActivity>> getCommonActivityListByTag(String citycode, String atype, String flag, 
			String tag, Long relatedid,
			String category, Long categoryid, Timestamp starttime, Timestamp endtime, String order, String asc,
			Integer from, Integer maxnum) {
		if(StringUtils.isBlank(tag)) return ErrorCode.getFailure(ApiConstant.CODE_SIGN_ERROR, "tag 不能为空！");
		return getActivityList(null, citycode, atype, null, null, null, flag, tag, relatedid, category, categoryid, starttime, endtime, null, null, null, order, asc, from, maxnum);
	}
	protected ErrorCode<Integer> getCommonActivityCountByTime(String citycode, String atype, String flag, 
			String tag, Long relatedid,
			String category, Long categoryid, Timestamp starttime, Timestamp endtime) {
		if(starttime == null || endtime==null) return ErrorCode.getFailure(ApiConstant.CODE_SIGN_ERROR, "starttime 或者 endtime 不能为空！");
		return getActivityCount(null, citycode, atype, null, null, null, flag, tag, relatedid, category, categoryid, starttime, endtime, null, null, null);
	}
	
	protected ErrorCode<List<RemoteActivity>> getCommonActivityListByMemberid(String citycode, String atype, Long memberid, Integer timetype, String flag, 
			String tag, Long relatedid,
			String category, Long categoryid, Timestamp starttime, Timestamp endtime, 
			Integer from, Integer maxnum) {
		if(memberid == null) return ErrorCode.getFailure(ApiConstant.CODE_SIGN_ERROR, "memberid不能为空！");
		return getActivityList(null, citycode, atype, null, memberid, timetype, flag, tag, relatedid, category, categoryid, starttime, endtime, null, null, null, null, "N", from, maxnum);
	}
	protected ErrorCode<List<RemoteActivity>> getCommonActivityListByFlag(String citycode, String atype, Integer timetype, String flag, 
			String tag, Long relatedid,
			String category, Long categoryid, Timestamp starttime, Timestamp endtime, 
			Integer from, Integer maxnum) {
		if(StringUtils.isBlank(flag)) return ErrorCode.getFailure(ApiConstant.CODE_SIGN_ERROR, "flag不能为空！");
		return getActivityList(null, citycode, atype, null, null, timetype, flag, tag, relatedid, category, categoryid, starttime, endtime, null, null, null, null, "N", from, maxnum);
	}
	protected ErrorCode<List<RemoteActivity>> getCommonActivityListByTag(String citycode, String atype, String flag, 
			String tag, Long relatedid,
			Timestamp starttime, Timestamp endtime, 
			Integer from, Integer maxnum) {
		if(StringUtils.isBlank(tag)) return ErrorCode.getFailure(ApiConstant.CODE_SIGN_ERROR, "tag不能为空！");
		return getActivityList(null, citycode, atype, null, null, null, flag, tag, relatedid, null, null, starttime, endtime, null, null, null, null, "N", from, maxnum);
	}
	protected ErrorCode<List<RemoteActivity>> getCommonActivityListByRelatedid(String citycode, String atype, String flag, 
			String tag, Long relatedid,
			String category, Long categoryid, Timestamp starttime, Timestamp endtime, 
			Integer from, Integer maxnum) {
		if(StringUtils.isBlank(tag) || relatedid==null) return ErrorCode.getFailure(ApiConstant.CODE_SIGN_ERROR, "tag 和 relatedid不能为空！");
		return getActivityList(null, citycode, atype, null, null, null, flag, tag, relatedid, category, categoryid, starttime, endtime, null, null, null, null, "N", from, maxnum);
	}
	protected ErrorCode<List<RemoteActivity>> getCommonActivityListByCategoryid(String citycode, String atype, String flag, 
			String tag, Long relatedid,
			String category, Long categoryid, Timestamp starttime, Timestamp endtime, 
			Integer from, Integer maxnum) {
		if(StringUtils.isBlank(category) || categoryid==null) return ErrorCode.getFailure(ApiConstant.CODE_SIGN_ERROR, "category 和 categoryid不能为空！");
		return getActivityList(null, citycode, atype, null, null, null, flag, tag, relatedid, category, categoryid, starttime, endtime, null, null, null, null, "N", from, maxnum);
	}
	protected ErrorCode<List<RemoteActivity>> getCommonActivityList(String citycode, String atype, String status, Long memberid, 
			Integer timetype, String flag, 
			String tag, Long relatedid,
			String category, Long categoryid,
			Timestamp starttime, Timestamp endtime, 
			List<Long> activityIdList,String sources,String isFee, String order, String asc, Integer from, Integer maxnum) {
		return getActivityList(null, citycode, atype, status, memberid, timetype, flag, tag, relatedid, category, categoryid, starttime, endtime, activityIdList, sources, isFee, order, asc, from, maxnum);
	}
	
	protected ErrorCode<Integer> getActivityCount(String title, String citycode, String atype, String status, Long memberid, 
			Integer timetype, String flag, 
			String tag, Long relatedid,
			String category, Long categoryid,
			Timestamp starttime, Timestamp endtime, 
			List<Long> activityIdList,String sources,String isFee) {
		List<Long> relatedidList = null;
		List<Long> categoryidList = null;
		if(relatedid!=null) relatedidList = Arrays.asList(relatedid);
		if(categoryid!=null) categoryidList = Arrays.asList(categoryid);
		return getCommonActivityCount(title, citycode, null, atype, status, memberid, timetype, flag, tag, relatedidList, category, categoryidList, starttime, endtime, activityIdList, sources, isFee);
	}
	
	protected ErrorCode<List<RemoteActivity>> getActivityList(String title, String citycode, String atype, String status, Long memberid, 
			Integer timetype, String flag, 
			String tag, Long relatedid,
			String category, Long categoryid,
			Timestamp starttime, Timestamp endtime, 
			List<Long> activityIdList,String sources,String isFee,
			String order, String asc, Integer from, Integer maxnum) {
		List<Long> relatedidList = null;
		List<Long> categoryidList = null;
		if(relatedid!=null) relatedidList = Arrays.asList(relatedid);
		if(categoryid!=null) categoryidList = Arrays.asList(categoryid);
		return getCommonActivityList(title, citycode, null, atype, status, memberid, null, timetype, flag, tag, relatedidList, category, categoryidList, starttime, endtime, activityIdList, sources, isFee, order, asc, from, maxnum);
	}
	
	private ErrorCode<RemoteActivity> getRemoteActivity(HttpResult result){
		if(result == null || !result.isSuccess()) return ErrorCode.getFailure(ApiConstant.CODE_SIGN_ERROR, "网络异常！");
		DataWrapper activity = (DataWrapper) ApiUtils.xml2Object(ApiUtils.getBeanReader("data", DataWrapper.class), result.getResponse());
		if(activity==null) return ErrorCode.getFailure("解析错误！");
		if(!activity.isSuccess()) return ErrorCode.getFailure(activity.getError());
		return ErrorCode.getSuccessReturn(activity.getActivity());
	}
	
	@Override
	public ErrorCode<RemoteActivity> getRemoteActivity(Serializable activityId){
		String url = activityApiUrl + ActivityRemoteUtil.getActivityDetailUrl();
		Map<String, String> params = new HashMap<String, String>();
		params.put("activityid", activityId +"");
		HttpResult result = getRequestResult(url, params, HttpTimeout.SHORT_REQUEST);
		return getRemoteActivity(result);
	}
	
	@Override
	public ErrorCode<List<RemoteActivity>> getRemoteActivityByMemberid(Long memberid, String tag, int from, int maxnum) {
		return getCommonActivityListByMemberid(null, null, memberid, null, null, tag, null, null, null, null, null, from, maxnum);
	}

	@Override
	public ErrorCode<List<RemoteActivity>> getRemoteActivityListByIds(List<Long> idList) {
		return getActivityList(null, null, null, null, null, null, null, null, null, null, null, null, null, idList, null, null, null, null, 0, 100);
	}
	
	@Override
	public  ErrorCode<List<RemoteActivity>> getActivityListByRelatedidList(String citycode, String atype, String tag, List<Long> relatedidList, 
			int from, int maxnum) {
		if(StringUtils.isBlank(tag)) return ErrorCode.getFailure(ApiConstant.CODE_SIGN_ERROR, "tag不能为空！");
		if(relatedidList==null || relatedidList.size()==0) return ErrorCode.getFailure(ApiConstant.CODE_SIGN_ERROR, "关联对象集合不能为空！");
		return getCommonActivityList(citycode, atype, null, null, null, tag, relatedidList, null, null, null, null, from, maxnum);
	}
	@Override
	public  ErrorCode<List<RemoteActivity>> getCurrActivityListByRelatedidList(String citycode, String atype, String tag, List<Long> relatedidList, 
			int from, int maxnum) {
		if(StringUtils.isBlank(tag)) return ErrorCode.getFailure(ApiConstant.CODE_SIGN_ERROR, "tag不能为空！");
		if(relatedidList==null || relatedidList.size()==0) return ErrorCode.getFailure(ApiConstant.CODE_SIGN_ERROR, "关联对象集合不能为空！");
		return getCommonActivityList(citycode, atype, null, RemoteActivity.TIME_CURRENT, null, tag, relatedidList, null, null, null, null, from, maxnum);
	}
	@Override
	public  ErrorCode<List<RemoteActivity>> getCurrActivityList(String citycode, String atype, String tag, Long relatedid, String category, Long categoryid,
			Timestamp starttime, Timestamp endtime, int from, int maxnum) {
		return getCommonActivityList(citycode, atype, null, RemoteActivity.TIME_CURRENT, null, tag, relatedid, category, categoryid, starttime, endtime, from, maxnum);
	}
	@Override
	public ErrorCode<List<RemoteActivity>> getActivityListByFlag(String citycode, String tag, Integer timetype, String flag, int from, int maxnum) {
		return getCommonActivityListByFlag(citycode, null, timetype, flag, tag, null, null, null, null, null, from, maxnum);
	}
	@Override
	public ErrorCode<List<RemoteActivity>> getActivityListByTimetype(String citycode, String atype, Integer timetype, String flag, String tag, Long relatedid, String category, Long categoryid, Integer from, Integer maxnum) {
		return getCommonActivityListByTimetype(citycode, atype, timetype, flag, tag, relatedid, category, categoryid, from, maxnum);
	}
	@Override
	public ErrorCode<List<RemoteActivity>> getActivityListByTime(String citycode, String atype, Timestamp starttime, Timestamp endtime, String flag, String tag, Long relatedid, String category, Long categoryid, String order, String asc, Integer from, Integer maxnum) {
		return getCommonActivityListByTime(citycode, atype, flag, tag, relatedid, category, categoryid, starttime, endtime, order, asc, from, maxnum);
	}
	@Override
	public ErrorCode<Integer> getActivityCountByTime(String citycode, String atype, Timestamp starttime, Timestamp endtime, String flag, String tag, Long relatedid, String category, Long categoryid) {
		return getCommonActivityCountByTime(citycode, atype, flag, tag, relatedid, category, categoryid, starttime, endtime);
	}
	
	@Override
	public ErrorCode<Integer> getCurrActivityCount(String citycode, String atype, String flag, String tag, Long relatedid,
			String category, Long categoryid, Timestamp starttime, Timestamp endtime) {
		return getActivityCount(null, citycode, atype, null, null, RemoteActivity.TIME_CURRENT, flag, tag, relatedid, category, categoryid, starttime, endtime, null, null, null);
	}
	@Override
	public ErrorCode<List<RemoteActivity>> getActivityList(String citycode, String atype, Integer timetype, String tag, Long relatedid, String category, Long categoryid, Integer from, Integer maxnum) {
		return getCommonActivityListByTimetype(citycode, atype, timetype, null, tag, relatedid, category, categoryid, from, maxnum);
	}
	@Override
	public ErrorCode<List<RemoteActivity>> getActivityListByOrder(String citycode, String atype, Integer timetype, String tag, Long relatedid, String category, Long categoryid, String order, Integer from, Integer maxnum) {
		return getCommonActivityList(citycode, atype, null, null, timetype, null, tag, relatedid, category, categoryid, null, null, null, null, null, order, null, from, maxnum);
	}
	@Override
	public ErrorCode<List<RemoteActivity>> getTopActivityList(String citycode, final String atype, String tag, Long relatedid) {
		String url = activityApiUrl + ActivityRemoteUtil.getTopActivityUrl();
		Map<String, String> params = new HashMap<String, String>();
		if(StringUtils.isNotBlank(citycode)) params.put("citycode", citycode);
		if(StringUtils.isNotBlank(atype)) params.put("type", atype);
		if(StringUtils.isNotBlank(tag)) params.put("tag", tag);
		if(relatedid != null) params.put("relatedid", String.valueOf(relatedid));
		HttpResult result = getRequestResult(url, params, HttpTimeout.SHORT_REQUEST);
		return getObjectList(RemoteActivityList.class, result); 
	}
	@Override
	public ErrorCode<List<RemoteActivity>> getMemberActivityListByMemberid(Long memberid, String citycode, int timetype, String tag, Long relatedid, int from, int maxnum) {
		if(memberid == null) return ErrorCode.getFailure(ApiConstant.CODE_SIGN_ERROR, "用户ID不能为空！");
		return getCommonActivityListByMemberid(citycode, null, memberid, timetype, null, tag, relatedid, null, null, null, null, from, maxnum);
	}
	@Override
	public ErrorCode<List<RemoteActivity>> getActivityListByStatus(String citycode, String status, int from, int maxnum) {
		if(StringUtils.isBlank(status)) return ErrorCode.getFailure(ApiConstant.CODE_SIGN_ERROR, "status不能为空！");
		return getCommonActivityList(citycode, null, status, null, null, null, null, null, null, null, null, null, null, null, null, null, null, from, maxnum);
	}
	@Override
	public ErrorCode<List<RemoteActivity>> getMemberActivityList(Long memberid, String tag, List<Long> relatedidList, int from, int maxnum) {
		if(memberid == null) return ErrorCode.getFailure(ApiConstant.CODE_SIGN_ERROR, "用户ID不能为空！");
		if(CollectionUtils.isEmpty(relatedidList)) return ErrorCode.getFailure(ApiConstant.CODE_SIGN_ERROR, "关联ID不能为空！");
		return getCommonActivityList(null, null, memberid, null, null, tag, relatedidList, null, null, null, null, from, maxnum);
	}

	@Override
	public ErrorCode<List<RemoteActivity>> getMemberJoinActivityList(Long memberid, int from, int maxnum) {
		if(memberid == null) return ErrorCode.getFailure(ApiConstant.CODE_SIGN_ERROR, "用户ID不能为空！");
		String url = activityApiUrl + ActivityRemoteUtil.getJoinListByMemberidUrl();
		Map<String, String> params = new HashMap<String, String>();
		params.put("memberid", String.valueOf(memberid));
		params.put("from", String.valueOf(from));
		params.put("maxnum", String.valueOf(maxnum));
		HttpResult result = getRequestResult(url, params, HttpTimeout.SHORT_REQUEST);
		return getObjectList(RemoteActivityList.class, result);
	}
	@Override
	public ErrorCode<List<RemoteActivity>> getActivityListByTag(String citycode, String atype, String flag, 
			String tag, Long relatedid,
			String category, Long categoryid, Timestamp starttime, Timestamp endtime, String order, String asc,
			Integer from, Integer maxnum) {
		return getCommonActivityListByTag(citycode, atype, flag, tag, relatedid, category, categoryid, starttime, endtime, order, asc, from, maxnum);
	}
	@Override
	public ErrorCode<List<RemoteActivity>> getActivityListByType(String citycode, String countycode, String atype, String datetype, Integer timetype, 
			String tag, Long relatedid, String category, Long categoryid, String isFee, String order, 
			Integer from, Integer maxnum) {
		List<Long> relatedidList = new ArrayList<Long>();
		List<Long> categoryidList = new ArrayList<Long>();
		if(relatedid!=null) relatedidList.add(relatedid);
		if(categoryid!=null) categoryidList.add(categoryid);
		return getCommonActivityList(null, citycode, countycode, atype, null, null, datetype, timetype, null, tag, relatedidList, category, categoryidList, null, null, null, null, isFee, order, null, from, maxnum);
	}
	@Override
	public ErrorCode<List<RemoteActivity>> getGewaCommendActivityList(List<Long> idList, boolean isClose) {
		ErrorCode<List<RemoteActivity>> code = getRemoteActivityListByIds(idList);
		if(!code.isSuccess()) return code;
		List<RemoteActivity> activityList = code.getRetval();
		for (Iterator iterator = activityList.iterator(); iterator.hasNext();) {
			RemoteActivity remoteActivity = (RemoteActivity) iterator.next();
			if(isClose && !remoteActivity.isOver2()|| !isClose && remoteActivity.isOver2()) iterator.remove();
		}
		return code;
	}
	@Override
	public ErrorCode<List<RemoteActivity>> getFriendActivityList(String tag, List<Long> idList, int from, int maxnum) {
		if(CollectionUtils.isEmpty(idList)) return ErrorCode.getFailure(ApiConstant.CODE_SIGN_ERROR, "朋友ID不能为空！");
		String url = activityApiUrl + ActivityRemoteUtil.getActivityListByFidListUrl();
		Map<String, String> params = new HashMap<String, String>();
		if(StringUtils.isNotBlank(tag)) params.put("tag", tag);
		params.put("memberids", StringUtils.join(idList, ",")); 
		params.put("from", from+""); 
		params.put("maxnum", maxnum+""); 
		HttpResult result = getRequestResult(url, params, HttpTimeout.SHORT_REQUEST);
		return getObjectList(RemoteActivityList.class, result);
	}
	
	@Override
	public ErrorCode<String> collectActivity(Long memberid, Long activityId) {
		if(memberid == null) return ErrorCode.getFailure(ApiConstant.CODE_SIGN_ERROR, "用户ID不能为空！");
		if(activityId == null) return ErrorCode.getFailure(ApiConstant.CODE_SIGN_ERROR, "活动ID不能为空！");
		String url = activityApiUrl + ActivityRemoteUtil.getAddCollectUrl();
		Map<String, String> params = new HashMap<String, String>();
		params.put("memberid", memberid + "");
		params.put("activityid", activityId + "");
		HttpResult result = getRequestResult(url, params, HttpTimeout.SHORT_REQUEST);
		if(!result.isSuccess()) return ErrorCode.getFailure(ApiConstant.CODE_SIGN_ERROR, "网络异常！");
		BaseObjectResponse response = (BaseObjectResponse) ApiUtils.xml2Object(ApiUtils.getBeanReader("data", BaseObjectResponse.class), result.getResponse());
		if(!response.isSuccess() || !StringUtils.equals(response.getResult(), "true")) return ErrorCode.getFailure(response.getCode(), response.getError());
		return ErrorCode.SUCCESS;
	}
	@Override
	public ErrorCode<String> cancelCollection(Long activityid, Long memberid) {
		if(memberid == null) return ErrorCode.getFailure(ApiConstant.CODE_SIGN_ERROR, "用户ID不能为空！");
		if(activityid == null) return ErrorCode.getFailure(ApiConstant.CODE_SIGN_ERROR, "活动ID不能为空！");
		String url = activityApiUrl + ActivityRemoteUtil.getCancelCollectUrl();
		Map<String, String> params = new HashMap<String, String>();
		params.put("memberid", String.valueOf(memberid));
		params.put("activityid", String.valueOf(activityid));
		return getRemoteResult(url, params, HttpTimeout.SHORT_REQUEST);
	}
	@Override
	public ErrorCode<RemoteApplyjoin> getApplyJoin(Long memberid, Long activityid) {
		if(memberid == null) return ErrorCode.getFailure(ApiConstant.CODE_SIGN_ERROR, "用户ID不能为空！");
		if(activityid == null) return ErrorCode.getFailure(ApiConstant.CODE_SIGN_ERROR, "活动ID不能为空！");
		String url = activityApiUrl + ActivityRemoteUtil.getApplyJoinByMemberidUrl();
		Map<String, String> params = new HashMap<String, String>();
		params.put("memberid", String.valueOf(memberid));
		params.put("activityid", String.valueOf(activityid));
		HttpResult result = getRequestResult(url, params, HttpTimeout.SHORT_REQUEST);
		if(result == null) return ErrorCode.getFailure(ApiConstant.CODE_SIGN_ERROR, "网络异常！");
		DataWrapper response = (DataWrapper) ApiUtils.xml2Object(ApiUtils.getBeanReader("data", DataWrapper.class), result.getResponse());
		if(!response.isSuccess()) return ErrorCode.getFailure(response.getCode(), response.getError());
		return ErrorCode.getSuccessReturn(response.getApplyjoin());
	}

	@Override
	public ErrorCode<List<RemoteApplyjoin>> getApplyJoinByMemberids(List<Long> memberids, Long activityid){
		if(memberids == null || memberids.isEmpty()){
			return ErrorCode.getFailure(ApiConstant.CODE_SIGN_ERROR, "用户ID不能为空！");
		}
		if(activityid == null){
			return ErrorCode.getFailure(ApiConstant.CODE_SIGN_ERROR, "活动ID不能为空！");
		}
		String url = activityApiUrl + ActivityRemoteUtil.applyJoinByMemberidsUrl;
		Map<String, String> params = new HashMap<String, String>();
		params.put("memberids", StringUtils.join(memberids, ','));
		params.put("activityid", String.valueOf(activityid));
		HttpResult result = getRequestResult(url, params, HttpTimeout.SHORT_REQUEST);
		return getObjectList(RemoteApplyjoinList.class, result);
	}
	
	@Override
	public ErrorCode<List<RemoteApplyjoin>> getApplyJoinListByActivityid(Long activityid) {
		return getApplyJoinListByActivityid(activityid, 0, 100);
	}
	
	@Override
	public ErrorCode<List<RemoteApplyjoin>> getApplyJoinListByActivityid(Long activityid, int from, int maxnum) {
		if(activityid == null) return ErrorCode.getFailure(ApiConstant.CODE_SIGN_ERROR, "活动ID不能为空！");
		String url = activityApiUrl + ActivityRemoteUtil.getJoinByActivityIdUrl();
		Map<String, String> params = new HashMap<String, String>();
		params.put("activityid", String.valueOf(activityid));
		params.put("from", String.valueOf(from));
		params.put("maxnum", String.valueOf(maxnum));
		HttpResult result = getRequestResult(url, params, HttpTimeout.SHORT_REQUEST);
		return getObjectList(RemoteApplyjoinList.class, result);
	}

	@Override
	public ErrorCode<List<RemoteApplyjoin>> getApplyJoinListByMemberid(Long memberid, int from, int maxnum) {
		if(memberid == null) return ErrorCode.getFailure(ApiConstant.CODE_SIGN_ERROR, "用户ID不能为空！");
		String url = activityApiUrl + ActivityRemoteUtil.getJoinByMemberidUrl();
		Map<String, String> params = new HashMap<String, String>();
		params.put("memberid", String.valueOf(memberid));
		params.put("from", String.valueOf(from));
		params.put("maxnum", String.valueOf(maxnum));
		HttpResult result = getRequestResult(url, params, HttpTimeout.SHORT_REQUEST);
		return getObjectList(RemoteApplyjoinList.class, result);
	}

	@Override
	public ErrorCode addClickedtimes(Long activityid) {
		String url = activityApiUrl + ActivityRemoteUtil.getAddClickedUrl();
		Map<String, String>	params = new HashMap<String, String>();
		params.put("activityid", activityid + "");
		HttpResult result = getRequestResult(url, params, HttpTimeout.SHORT_REQUEST);
		if(!result.isSuccess()) return ErrorCode.getFailure(ApiConstant.CODE_SIGN_ERROR, "网络异常！");
		BaseObjectResponse response = (BaseObjectResponse) ApiUtils.xml2Object(ApiUtils.getBeanReader("data", BaseObjectResponse.class), result.getResponse());
		if(!response.isSuccess() || !StringUtils.equals(response.getResult(), "true")) return ErrorCode.getFailure(response.getCode(), response.getError());
		return ErrorCode.SUCCESS;
	}
	@Override
	public ErrorCode<List<CountyCount>> getGroupActivityByTag(String tag, Date date, String citycode) {
		if(StringUtils.isBlank(tag)) return ErrorCode.getFailure(ApiConstant.CODE_SIGN_ERROR, "TAG不能为空！");
		if(date == null) return ErrorCode.getFailure(ApiConstant.CODE_SIGN_ERROR, "日期不能为空！");
		if(StringUtils.isBlank(citycode)) return ErrorCode.getFailure(ApiConstant.CODE_SIGN_ERROR, "城市代码！");
		String url = activityApiUrl + ActivityRemoteUtil.getGroupByCountyUrl();
		Map<String, String> params = new HashMap<String, String>();
		params.put("tag", tag);
		params.put("date", DateUtil.format(date, "yyyy-MM-dd"));
		params.put("citycode", citycode);
		HttpResult result = getRequestResult(url, params, HttpTimeout.SHORT_REQUEST);
		return getObjectList(CountyCountList.class, result);
	}


	@Override
	public ErrorCode<Integer> getMemberActivityCount(Long memberid, String citycode, int timetype, String tag, Long relatedid) {
		if(memberid == null) return ErrorCode.getFailure(ApiConstant.CODE_SIGN_ERROR, "用户ID不能为空！");
		String url = activityApiUrl + ActivityRemoteUtil.getActivityCountByMemberidUrl();
		Map<String, String> params = new HashMap<String, String>();
		params.put("memberid",  String.valueOf(memberid));
		if(StringUtils.isNotBlank(citycode)) params.put("citycode", citycode);
		params.put("timetype", String.valueOf(timetype));
		if(StringUtils.isNotBlank(tag)) params.put("tag", tag);
		if(relatedid != null) params.put("relatedid", String.valueOf(relatedid));
		HttpResult result = getRequestResult(url, params, HttpTimeout.SHORT_REQUEST);
		return getRemoteCount(result);
	}


	@Override
	public ErrorCode<Integer> getActivityCount(String citycode, String atype, int timetype, String tag, Long relatedid) {
		String url = activityApiUrl + ActivityRemoteUtil.getActivityCountUrl();
		Map<String, String> params = new HashMap<String, String>();
		if(StringUtils.isNotBlank(citycode)) params.put("citycode", citycode);
		if(StringUtils.isNotBlank(atype)) params.put("type", atype);
		params.put("timetype", String.valueOf(timetype));
		if(StringUtils.isNotBlank(tag)) params.put("tag", tag);
		if(relatedid != null) params.put("relatedids", String.valueOf(relatedid));
		HttpResult result = getRequestResult(url, params, HttpTimeout.SHORT_REQUEST);
		return getRemoteCount(result);
	}


	@Override
	public ErrorCode<List<RemoteApplyjoin>> getApplyJoinList(String citycode, String tag, Long relatedid, int from, int maxnum) {
		String url = activityApiUrl + ActivityRemoteUtil.getJoinByRelatedidUrl();
		Map<String, String> params = new HashMap<String, String>();
		params.put("citycode", citycode);
		params.put("tag", tag);
		params.put("relatedid", String.valueOf(relatedid));
		params.put("from", String.valueOf(from));
		params.put("maxnum", String.valueOf(maxnum));
		HttpResult result = getRequestResult(url, params, HttpTimeout.SHORT_REQUEST);
		return getObjectList(RemoteApplyjoinList.class, result);
	}

	@Override
	public ErrorCode<Integer> getApplyJoinCountByTag(String citycode, String tag) {
		String url = activityApiUrl + ActivityRemoteUtil.getJoinCountByTagUrl();
		Map<String, String> params = new HashMap<String, String>();
		params.put("citycode", citycode);
		params.put("tag", tag);
		HttpResult result = getRequestResult(url, params, HttpTimeout.SHORT_REQUEST);
		return getRemoteCount(result);
	}
	
	@Override
	public ErrorCode<Integer> getMemberJoinActivityCount(Long memberid) {
		String url = activityApiUrl + ActivityRemoteUtil.getJoinCountByMemberidUrl();
		Map<String, String> params = new HashMap<String, String>();
		params.put("memberid", String.valueOf(memberid));
		HttpResult result = getRequestResult(url, params, HttpTimeout.SHORT_REQUEST);
		return getRemoteCount(result);
	}
	@Override
	public ErrorCode<Integer> getFriendActivityCount(String tag, List<Long> idList) {
		if(CollectionUtils.isEmpty(idList)) return ErrorCode.getFailure(ApiConstant.CODE_SIGN_ERROR, "朋友ID不能为空！");
		String url = activityApiUrl + ActivityRemoteUtil.getActivityCountByFidListUrl();
		Map<String, String> params = new HashMap<String, String>();
		if(StringUtils.isNotBlank(tag)) params.put("tag", tag);
		params.put("memberids", StringUtils.join(idList, ","));
		HttpResult result = getRequestResult(url, params, HttpTimeout.SHORT_REQUEST);
		return getRemoteCount(result);
	}
	@Override
	public ErrorCode<RemoteActivity> joinActivity(Long memberid, Long activityid, String sex, String realname, String mobile, Integer joinnum, Date joinDate, String walaAddress) {
		if(memberid == null) return ErrorCode.getFailure(ApiConstant.CODE_SIGN_ERROR, "用户ID不能为空！");
		if(activityid == null) return ErrorCode.getFailure(ApiConstant.CODE_SIGN_ERROR, "活动ID不能为空！");
		if(joinnum == null) return ErrorCode.getFailure(ApiConstant.CODE_SIGN_ERROR, "参加人数不能为空");
		if(StringUtils.isBlank(walaAddress)) return ErrorCode.getFailure(ApiConstant.CODE_SIGN_ERROR, "参加活动来源不能为空");
		String url = activityApiUrl + ActivityRemoteUtil.getJoinActivityUrl();
		
		Map<String, String> params = new HashMap<String, String>();
		params.put("memberid", String.valueOf(memberid));
		params.put("activityid", String.valueOf(activityid));
		params.put("walaaddress", walaAddress);
		if(StringUtils.isNotBlank(sex)) params.put("sex", sex);
		if(StringUtils.isNotBlank(realname)) params.put("realname", realname);
		if(StringUtils.isNotBlank(mobile)) params.put("mobile", mobile);
		if(joinDate!=null) params.put("joindate", DateUtil.formatDate(joinDate));
		params.put("joinnum", String.valueOf(joinnum));
		HttpResult result = getRequestResult(url, params, HttpTimeout.SHORT_REQUEST);
		return getRemoteActivity(result);
	}
	@Override
	public ErrorCode<RemoteActivity> cancelActivity(Long activityid, Long memberid) {
		if(memberid == null) return ErrorCode.getFailure(ApiConstant.CODE_SIGN_ERROR, "用户ID不能为空！");
		if(activityid == null) return ErrorCode.getFailure(ApiConstant.CODE_SIGN_ERROR, "活动ID不能为空！");
		String url = activityApiUrl + ActivityRemoteUtil.getCancelActivityUrl();
		Map<String, String> params = new HashMap<String, String>();
		params.put("memberid", String.valueOf(memberid));
		params.put("activityid", String.valueOf(activityid));
		HttpResult result = getRequestResult(url, params, HttpTimeout.SHORT_REQUEST);
		return getRemoteActivity(result);
	}

	@Override
	public ErrorCode<List<RemoteTreasure>> getTreasureList(Long activityid, String asc, int from, int maxnum) {
		if(activityid==null) return ErrorCode.getFailure(ApiConstant.CODE_SIGN_ERROR, "活动id不能为空！");
		String url = activityApiUrl + ActivityRemoteUtil.getTreasureListUrl();
		Map<String, String> params = new HashMap<String, String>();
		params.put("activityid", String.valueOf(activityid));
		if(StringUtils.isNotBlank(asc))params.put("asc", asc);
		params.put("from", from+"");
		params.put("maxnum", maxnum+"");
		HttpResult result = getRequestResult(url, params, HttpTimeout.SHORT_REQUEST);
		return getObjectList(RemoteTreasureList.class, result);
	}

	@Override
	public ErrorCode<List<RemoteActivity>> getMemberCollActivityList(Long memberid, String tag, String order, String asc, int from, int maxnum) {
		String url = activityApiUrl + ActivityRemoteUtil.getMemberCollUrl();
		Map<String, String> params = new HashMap<String, String>();
		params.put("memberid", String.valueOf(memberid));
		if(StringUtils.isNotBlank(tag))params.put("tag", tag);
		if(StringUtils.isNotBlank(order))params.put("order", order);
		if(StringUtils.isNotBlank(asc))params.put("asc", asc);
		params.put("from", from+"");
		params.put("maxnum", maxnum+"");
		HttpResult result = getRequestResult(url, params, HttpTimeout.SHORT_REQUEST);
		return getObjectList(RemoteActivityList.class, result);
	}

	@Override
	public ErrorCode<RemoteActivity> addActivity(Member member, String citycode, Long activityid, String tag, Long relatedid, String category, Long categoryid, String title, Integer price, Timestamp starttime, Timestamp endtime,
			String contentdetail, String address) {
		String url = activityApiUrl + ActivityRemoteUtil.getAddActivityUrl();
		Map<String, String> params = new HashMap<String, String>();
		params.put("memberid", String.valueOf(member.getId()));
		if(activityid!=null)params.put("activityid", activityid+"");
		if(StringUtils.isNotBlank(tag))params.put("tag", tag);
		if(relatedid!=null) params.put("relatedid", relatedid+"");
		if(StringUtils.isNotBlank(category))params.put("category", category);
		if(categoryid!=null) params.put("categoryid", categoryid+"");
		params.put("citycode", citycode);
		params.put("title", title);
		if(price!=null) params.put("price", price+"");
		params.put("content", contentdetail);
		params.put("bgtime", DateUtil.format(starttime, "yyyy-MM-dd HH:mm"));
		params.put("edtime",  DateUtil.format(endtime, "yyyy-MM-dd HH:mm"));
		HttpResult result = getRequestResult(url, params, HttpTimeout.SHORT_REQUEST);
		return getRemoteActivity(result);
	}
	
	private Map<String, String> getQryMap(String title, String citycode, String countycode, String atype, String status, Long memberid, 
			String datetype, Integer timetype, String flag, 
			String tag, List<Long> relatedidList,
			String category, List<Long> categoryidList,
			Timestamp starttime, Timestamp endtime, 
			List<Long> activityIdList,String sources,String isFee){
		Map<String, String> params = new HashMap<String, String>();
		if(StringUtils.isNotBlank(citycode)) params.put("citycode", citycode);
		if(StringUtils.isNotBlank(citycode)) params.put("countycode", countycode);
		if(StringUtils.isNotBlank(tag)) params.put("tag", tag);
		if(StringUtils.isNotBlank(title)) params.put("title", title);
		if(StringUtils.isNotBlank(atype)) params.put("type", atype);
		if(StringUtils.isNotBlank(status)) params.put("status", status);
		if(memberid!=null) params.put("memberid", memberid+"");
		if(timetype==null) timetype = -1;  
		params.put("timetype", timetype+"");
		if(StringUtils.isNotBlank(datetype)) params.put("datetype", datetype);
		if(StringUtils.isNotBlank(flag)) params.put("flag", flag);
		if(StringUtils.isNotBlank(tag)) params.put("tag", tag);
		if(relatedidList!=null && relatedidList.size()>0) params.put("relatedids", StringUtils.join(relatedidList, ","));
		if(StringUtils.isNotBlank(category)) params.put("category", category);
		if(categoryidList!=null && categoryidList.size()>0) params.put("categoryids", StringUtils.join(categoryidList, ","));
		if(starttime!=null) params.put("starttime", DateUtil.formatTimestamp(starttime));
		if(endtime!=null) params.put("endtime", DateUtil.formatTimestamp(endtime));
		if(activityIdList!=null && activityIdList.size()>0) params.put("activityids", StringUtils.join(activityIdList, ","));
		if(StringUtils.isNotBlank(sources)) params.put("sources", sources);
		if(StringUtils.isNotBlank(isFee)) params.put("isfee", isFee);
		return params;
	}
	protected ErrorCode<List<RemoteActivity>> getCommonActivityList(String title, String citycode, String countycode, String atype, String status, Long memberid, 
			String datetype, Integer timetype, String flag, 
			String tag, List<Long> relatedidList,
			String category, List<Long>  categoryidList,
			Timestamp starttime, Timestamp endtime, 
			List<Long> activityIdList,String sources,String isFee, String order, String asc, Integer from, Integer maxnum) {
		String url = activityApiUrl + ActivityRemoteUtil.getActivityListUrl();
		Map<String, String> params = new HashMap<String, String>();
		params.putAll(getQryMap(title, citycode, countycode, atype, status, memberid, datetype, timetype, flag, 
				tag, relatedidList, category, categoryidList, 
				starttime, endtime, activityIdList, sources, isFee));
		if(StringUtils.isBlank(asc)) asc = "N";
		if(StringUtils.isBlank(order)) order = "addtime";
		if(from==null) from = 0;
		if(maxnum==null) maxnum = 20;
		if(maxnum>100) maxnum = 100;
		params.put("order", order);
		params.put("asc", asc);
		params.put("from", String.valueOf(from));
		params.put("maxnum", String.valueOf(maxnum));
		HttpResult result = getRequestResult(url, params, HttpTimeout.SHORT_REQUEST);
		return getObjectList(RemoteActivityList.class, result);
	}
	@Override
	public List<RemoteActivity> getGewaCommendActivityList(String citycode, String signname, String tag, int from, int maxnum){
		List<RemoteActivity> activityList = new ArrayList<RemoteActivity>();
		DetachedCriteria query = DetachedCriteria.forClass(GewaCommend.class,"gc");
		if(StringUtils.isNotBlank(citycode)) query.add(Restrictions.or(Restrictions.eq("gc.citycode", AdminCityContant.CITYCODE_ALL), Restrictions.like("gc.citycode", citycode, MatchMode.ANYWHERE)));
		if(StringUtils.isNotBlank(signname))query.add(Restrictions.eq("gc.signname", signname));
		if(StringUtils.isNotBlank(tag)) query.add(Restrictions.eq("gc.tag", tag));
		query.add(Restrictions.gt("gc.ordernum", 0));
		query.setProjection(Projections.property("gc.relatedid"));
		List<Long> relatedidList = hibernateTemplate.findByCriteria(query, from, maxnum);
		if(relatedidList.size()==0) return activityList;
		ErrorCode<List<RemoteActivity>> code = getRemoteActivityListByIds(relatedidList);
		if(code.isSuccess()){
			for(RemoteActivity activity : code.getRetval()){
				if(activity.isPlaying()){
					activityList.add(activity);
				}
			}
		}
		return activityList;
	}
	@Override
	public ErrorCode<List<CategoryCount>> getCategoryCountList() {
		String url = activityApiUrl + ActivityRemoteUtil.getCategoryCountUrl();
		Map<String, String> params = new HashMap<String, String>();
		HttpResult result = getRequestResult(url, params, HttpTimeout.SHORT_REQUEST);
		return getObjectList(CategoryCountList.class, result);
	}
	@Override
	public List<String> getActivityIdList(Timestamp begintime, Timestamp endtime) {
		List<String> activityIdList = new ArrayList<String>();
		String url = activityApiUrl + ActivityRemoteUtil.getSiteMapCountUrl();
		Map<String, String> params = new HashMap<String, String>();
		params.put("beginTime", DateUtil.formatTimestamp(begintime));
		if(endtime!=null) params.put("endTime", DateUtil.formatTimestamp(endtime));
		HttpResult result = getRequestResult(url, params, HttpTimeout.SHORT_REQUEST);
		if(!result.isSuccess()) return activityIdList;
		BaseObjectResponse response = (BaseObjectResponse) ApiUtils.xml2Object(ApiUtils.getBeanReader("data", BaseObjectResponse.class), result.getResponse());
		return Arrays.asList(StringUtils.split(response.getResult(), ","));
	}
	@Override
	public Integer getJoinCountByAddtime(Timestamp begintime, Timestamp endtime) {
		String url = activityApiUrl + ActivityRemoteUtil.getTopActivityUrl();
		Map<String, String> params = new HashMap<String, String>();
		params.put("beginTime", DateUtil.formatTimestamp(begintime));
		if(endtime!=null) params.put("endTime", DateUtil.formatTimestamp(endtime));
		HttpResult result = getRequestResult(url, params, HttpTimeout.SHORT_REQUEST);
		if(!result.isSuccess()) return 0;
		BaseObjectResponse response = (BaseObjectResponse) ApiUtils.xml2Object(ApiUtils.getBeanReader("data", BaseObjectResponse.class), result.getResponse());
		return Integer.valueOf(response.getResult());
	}

	@Override
	public List<Long> getTopAddMemberidList(String citycode, String tag, int maxnum) {
		String url = activityApiUrl + ActivityRemoteUtil.getTopAddMemberUrl();
		Map<String, String> params = new HashMap<String, String>();
		if(StringUtils.isNotBlank(citycode)) params.put("citycode", citycode);
		if(StringUtils.isNotBlank(tag)) params.put("tag", tag);
		params.put("maxnum", maxnum+"");
		HttpResult result = getRequestResult(url, params, HttpTimeout.SHORT_REQUEST);
		List<Long> idList = new ArrayList<Long>();
		if(!result.isSuccess()) return idList;
		BaseObjectResponse response = (BaseObjectResponse) ApiUtils.xml2Object(ApiUtils.getBeanReader("data", BaseObjectResponse.class), result.getResponse());
		for(String str : response.getResult().split(",")){
			idList.add(Long.valueOf(str));
		}
		return idList;
	}
	@Override
	public ErrorCode<List<RemoteActivity>> getCommendActivityList(String citycode, String tag, Long relatedid, String cateogory, Long categoryid, int from, int maxnum){
		String url = activityApiUrl + ActivityRemoteUtil.getCommendActivityUrl();
		Map<String, String> params = new HashMap<String, String>();
		params.put("citycode", citycode);
		if(StringUtils.isNotBlank(tag)) params.put("tag", tag);
		if(relatedid!=null) params.put("relatedid", relatedid+"");
		if(StringUtils.isNotBlank(cateogory)) params.put("cateogory", cateogory);
		if(categoryid!=null) params.put("categoryid", categoryid+"");
		params.put("from", String.valueOf(from));
		params.put("maxnum", String.valueOf(maxnum));
		HttpResult result = getRequestResult(url, params, HttpTimeout.SHORT_REQUEST);
		return getObjectList(RemoteActivityList.class, result);
	}
	@Override
	public ErrorCode<List<RemoteActivity>> getHotActivityList(String citycode, String tag, Long relatedid, String cateogory, Long categoryid, int from, int maxnum){
		String url = activityApiUrl + ActivityRemoteUtil.getHotActivityUrl();
		Map<String, String> params = new HashMap<String, String>();
		params.put("citycode", citycode);
		if(StringUtils.isNotBlank(tag)) params.put("tag", tag);
		if(relatedid!=null) params.put("relatedid", relatedid+"");
		if(StringUtils.isNotBlank(cateogory)) params.put("cateogory", cateogory);
		if(categoryid!=null) params.put("categoryid", categoryid+"");
		params.put("from", String.valueOf(from));
		params.put("maxnum", String.valueOf(maxnum));
		HttpResult result = getRequestResult(url, params, HttpTimeout.SHORT_REQUEST);
		return getObjectList(RemoteActivityList.class, result);
	}
	@Override
	public ErrorCode<List<RemoteActivity>> getActivityListBySignname(String citycode, String signname, int from, int maxnum){
		String url = activityApiUrl + ActivityRemoteUtil.getActivityUrlBySignname();
		Map<String, String> params = new HashMap<String, String>();
		params.put("citycode", citycode);
		params.put("signname", signname);
		params.put("from", String.valueOf(from));
		params.put("maxnum", String.valueOf(maxnum));
		HttpResult result = getRequestResult(url, params, HttpTimeout.SHORT_REQUEST);
		return getObjectList(RemoteActivityList.class, result);
	}
	
	@Override
	public ErrorCode<List<RemoteActivity>> getActivityListByDatetype(String citycode, String atype, String datetype, String isFee,
			String tag, Long relatedid, String category, Long categoryid, int from, int maxnum){
		String url = activityApiUrl + ActivityRemoteUtil.getHotActivityUrl();
		Map<String, String> params = new HashMap<String, String>();
		params.put("citycode", citycode);
		params.put("datetype", datetype);
		if(StringUtils.isNotBlank(atype))params.put("type", atype);
		if(StringUtils.isNotBlank(isFee))params.put("isFee", isFee);
		if(StringUtils.isNotBlank(tag))params.put("tag", tag);
		if(relatedid!=null) params.put("relatedid", relatedid+"");
		if(StringUtils.isNotBlank(category))params.put("category", category);
		if(categoryid!=null) params.put("categoryid", categoryid+"");
		params.put("from", String.valueOf(from));
		params.put("maxnum", String.valueOf(maxnum));
		HttpResult result = getRequestResult(url, params, HttpTimeout.SHORT_REQUEST);
		return getObjectList(RemoteActivityList.class, result);
	}
	@Override
	public List<String> getActivityMpidList(Long activityid) {
		List<String> activityIdList = new ArrayList<String>();
		String url = activityApiUrl + ActivityRemoteUtil.getActivityMpiUrl();
		Map<String, String> params = new HashMap<String, String>();
		params.put("activityid", String.valueOf(activityid));
		HttpResult result = getRequestResult(url, params, HttpTimeout.SHORT_REQUEST);
		if(!result.isSuccess()) return activityIdList;
		BaseObjectResponse response = (BaseObjectResponse) ApiUtils.xml2Object(ApiUtils.getBeanReader("data", BaseObjectResponse.class), result.getResponse());
		if(response==null || StringUtils.isBlank(response.getResult())) return activityIdList;
		return Arrays.asList(StringUtils.split(response.getResult(), ","));
	}

	@Override
	public ErrorCode<List<RemoteActivity>> getActivityListByMemberidList(String citycode, String atype, String datetype, List<Long> memberidList,
			String isFee, String tag, Long relatedid, String category, Long categoryid, int from, int maxnum) {
		if(memberidList==null || memberidList.size()==0) return ErrorCode.getFailure("用户id集合不能为空");
		String url = activityApiUrl + ActivityRemoteUtil.getActivityUrlByMembers();
		Map<String, String> params = new HashMap<String, String>();
		params.put("citycode", citycode);
		params.put("timetype", "-1");
		if(StringUtils.isNotBlank(datetype))params.put("datetype", datetype);
		if(StringUtils.isNotBlank(atype))params.put("type", atype);
		if(StringUtils.isNotBlank(isFee))params.put("isFee", isFee);
		if(StringUtils.isNotBlank(tag))params.put("tag", tag);
		if(relatedid!=null) params.put("relatedid", relatedid+"");
		if(StringUtils.isNotBlank(category))params.put("category", category);
		if(categoryid!=null) params.put("categoryid", categoryid+"");
		params.put("memberids", StringUtils.join(memberidList, ","));
		params.put("from", String.valueOf(from));
		params.put("maxnum", String.valueOf(maxnum));
		HttpResult result = getRequestResult(url, params, HttpTimeout.SHORT_REQUEST);
		return getObjectList(RemoteActivityList.class, result);
	}
	
	@Override
	public List<String> memberOperActivityResult(Long activityid, Long memberid) {
		List<String> activityIdList = new ArrayList<String>();
		String url = activityApiUrl + ActivityRemoteUtil.getMemberOperActivityResult();
		Map<String, String> params = new HashMap<String, String>();
		params.put("relatedid", String.valueOf(activityid));
		params.put("memberid", String.valueOf(memberid));
		HttpResult result = getRequestResult(url, params, HttpTimeout.SHORT_REQUEST);
		if(!result.isSuccess()) return activityIdList;
		BaseObjectResponse response = (BaseObjectResponse) ApiUtils.xml2Object(ApiUtils.getBeanReader("data", BaseObjectResponse.class), result.getResponse());
		if(response==null || StringUtils.isBlank(response.getResult())) return activityIdList;
		return Arrays.asList(StringUtils.split(response.getResult(), ","));
	}
	
	@Override
	public ErrorCode<List<RemoteActivityMpi>> getRemoteActiviyMpiList(Long activityid) {
		if(activityid==null) return ErrorCode.getFailure("activityid不能为空");
		String url = activityApiUrl + ActivityRemoteUtil.getActivityMpiGuestUrl();
		Map<String, String> params = new HashMap<String, String>();
		params.put("activityid", String.valueOf(activityid));
		HttpResult result = getRequestResult(url, params, HttpTimeout.SHORT_REQUEST);
		return getObjectList(RemoteActivityMpiList.class, result);
	}
	@Override
	public void updateActiviyOrderMobile(String tradeNo, String mobile) {
		String url = activityApiUrl + ActivityRemoteUtil.getActivityOrderUpdateMoblie();
		Map<String, String> params = new HashMap<String, String>();
		params.put("tradeNo", tradeNo);
		params.put("mobile", mobile);
		getRequestResult(url, params, HttpTimeout.SHORT_REQUEST);
	}
	@Override
	public ErrorCode<String> activityOrderReturn(String tradeNo) {
		String url = activityApiUrl + ActivityRemoteUtil.getActivityOrderReturn();
		Map<String, String> params = new HashMap<String, String>();
		params.put("tradeNo", tradeNo);
		HttpResult result = getRequestResult(url, params, HttpTimeout.SHORT_REQUEST);
		return getRemoteResult(result);
	}
	@Override
	public List<Long> getActivityRelatedidByTag(String citycode, Integer timetype, String tag){
		String key = CacheConstant.buildKey("get1123ActivityRelatedidByTag222", citycode,timetype, tag);
		List<Long> idList = (List<Long>) cacheService.get(CacheConstant.REGION_ONEHOUR, key);
		if(idList==null ){
			String url = activityApiUrl + ActivityRemoteUtil.getActivityRelatedidByTagUrl();
			Map<String, String> params = new HashMap<String, String>();
			params.put("citycode", citycode);
			params.put("timetype", timetype+"");
			params.put("tag", tag);
			HttpResult result = getRequestResult(url, params, HttpTimeout.SHORT_REQUEST);
			ErrorCode<String> res = getRemoteResult(result);
			if(res.isSuccess()){
				String ids = res.getRetval();
				idList = BeanUtil.getIdList(ids, ",");
				cacheService.set(CacheConstant.REGION_ONEHOUR, key, idList);
			}
		}
		return idList;
	}
}
