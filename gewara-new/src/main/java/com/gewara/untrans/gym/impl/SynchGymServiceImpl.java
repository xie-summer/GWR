package com.gewara.untrans.gym.impl;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Service;

import com.gewara.constant.ApiConstant;
import com.gewara.constant.TagConstant;
import com.gewara.constant.ticket.OrderConstant;
import com.gewara.model.pay.GymOrder;
import com.gewara.support.ErrorCode;
import com.gewara.untrans.AbstractSynchBaseService;
import com.gewara.untrans.gym.SynchGymService;
import com.gewara.util.DateUtil;
import com.gewara.web.action.inner.util.GymRemoteUtil;
import com.gewara.xmlbind.DataWrapper;
import com.gewara.xmlbind.common.BrandnameCount;
import com.gewara.xmlbind.common.BrandnameCountList;
import com.gewara.xmlbind.gym.BookingRecord;
import com.gewara.xmlbind.gym.BookingRecordListResponse;
import com.gewara.xmlbind.gym.CardItem;
import com.gewara.xmlbind.gym.CardItemListResponse;
import com.gewara.xmlbind.gym.GymListResponse;
import com.gewara.xmlbind.gym.RemoteCoach;
import com.gewara.xmlbind.gym.RemoteCoachListResponse;
import com.gewara.xmlbind.gym.RemoteCourse;
import com.gewara.xmlbind.gym.RemoteCourseListResponse;
import com.gewara.xmlbind.gym.RemoteGym;
import com.gewara.xmlbind.gym.RemoteSpecialCourse;
import com.gewara.xmlbind.gym.SpecialCourseListResponse;

@Service("synchGymService")
public class SynchGymServiceImpl extends AbstractSynchBaseService implements SynchGymService, InitializingBean {
	private static int TIME_OUT_ = 5000;
	private String gymApiUrl;
	@Override
	public void afterPropertiesSet() throws Exception {
		gymApiUrl = config.getString("gymApiUrl");
		
	}
	@Override
	public ErrorCode<CardItem> getGymCardItem(Serializable cid, boolean cache){
		if(cid == null) return ErrorCode.getFailure(ApiConstant.CODE_SIGN_ERROR, "健身卡编号为空！");
		//String key = CacheConstant.buildKey(GymRemoteUtil.CACHE_KEY_GYMCARDITEM, cid);
		CardItem response = null;//(GymCardItemResponse) cacheService.get(CacheConstant.REGION_HALFDAY, key);
		if(!cache || response == null){
			String url = gymApiUrl + GymRemoteUtil.getGymCardUrl();
			Map<String, String> params = new HashMap<String, String>();
			params.put("cardId", cid + "");
			ErrorCode<DataWrapper> code = getObject(DataWrapper.class, url, params, TIME_OUT_);
			if(!code.isSuccess()) return ErrorCode.getFailure(code.getErrcode(), code.getMsg());
			response = code.getRetval().getCard();
			if(response == null) return ErrorCode.getFailure(ApiConstant.CODE_SIGN_ERROR, "数据转换错误！");
			//cacheService.set(CacheConstant.REGION_HALFDAY, key, response);
		}
		return ErrorCode.getSuccessReturn(response);
	}
	
	@Override
	public ErrorCode<CardItem> showBuyCardItem(Serializable cid, String speciallist, boolean cache){
		if(cid == null) return ErrorCode.getFailure(ApiConstant.CODE_SIGN_ERROR, "健身卡编号为空！");
		if(StringUtils.isBlank(speciallist)) return ErrorCode.getFailure(ApiConstant.CODE_SIGN_ERROR, "所选课程不能为空！");
		//String key = CacheConstant.buildKey(GymRemoteUtil.CACHE_KEY_BUYCARDITEM, cid, speciallist);
		CardItem response = null;//(GymCardItemResponse) cacheService.get(CacheConstant.REGION_HALFDAY, key);
		if(!cache || response == null){
			String url = gymApiUrl + GymRemoteUtil.getShowGymCardUrl();
			Map<String, String> params = new HashMap<String, String>();
			params.put("cardId", cid + "");
			params.put("courseIds", speciallist);
			ErrorCode<DataWrapper> code = getObject(DataWrapper.class, url, params, TIME_OUT_);
			if(!code.isSuccess()) return ErrorCode.getFailure(code.getErrcode(), code.getMsg());
			response = code.getRetval().getCard();
			if(response == null) return ErrorCode.getFailure(ApiConstant.CODE_SIGN_ERROR, "数据转换错误！");
			//cacheService.set(CacheConstant.REGION_HALFDAY, key, response);
		}
		return ErrorCode.getSuccessReturn(response);
	}

	@Override
	public ErrorCode<RemoteGym> getRemoteGym(Serializable gymId, boolean cache) {
		if(gymId == null) return ErrorCode.getFailure(ApiConstant.CODE_SIGN_ERROR, "场馆Id不能为空！");
		RemoteGym response = null; 
		if(!cache || response == null){
			String url =  gymApiUrl + GymRemoteUtil.getGymUrl();
			Map<String, String>	params = new HashMap<String, String>();
			params.put("gymId", gymId + "");
			ErrorCode<DataWrapper> code = getObject(DataWrapper.class, url, params, TIME_OUT_);
			if(!code.isSuccess()) {
				return ErrorCode.getFailure(code.getErrcode(), code.getMsg());
			}
			response = code.getRetval().getGym();
			if(response == null) return ErrorCode.getFailure(ApiConstant.CODE_SIGN_ERROR, "数据转换错误！");
		}
		return ErrorCode.getSuccessReturn(response);
	}
	
	@Override
	public ErrorCode<String> lockCard(GymOrder order){
		if(order == null || StringUtils.startsWith(order.getStatus(), OrderConstant.STATUS_PAID)) return ErrorCode.getFailure(ApiConstant.CODE_SIGN_ERROR, "订单错误！");
		String url = gymApiUrl + GymRemoteUtil.getLockCardUrl();
		Map<String, String> params = new HashMap<String, String>();
		params.put("orderId", order.getId()+"");
		params.put("cardId", order.getGci()+"");
		params.put("quantity", order.getQuantity()+"");
		return getRemoteResult(url, params, TIME_OUT_);
	}

	@Override
	public ErrorCode<List<RemoteGym>> getRemoteGymIdList(List<Long> idList) {
		if(CollectionUtils.isEmpty(idList)) return ErrorCode.getFailure(ApiConstant.CODE_SIGN_ERROR, "参数不能为空或空集合！");
		String url = gymApiUrl + GymRemoteUtil.getGymIdListUrl();
		Map<String, String> params = new HashMap<String, String>();
		params.put("ids", StringUtils.join(idList, ","));
		return getObjectList(GymListResponse.class, url, params, TIME_OUT_);
	}

	@Override
	public ErrorCode<List<RemoteGym>> getGymListByCoachId(Long coachId) {
		return getGymListByCoachId(coachId, "clickedtimes", false, 0, 100);
	}
	
	@Override
	public ErrorCode<List<RemoteGym>> getGymListByCoachId(Long coachId, String order, boolean asc, int from, int maxnum) {
		if(coachId == null) return ErrorCode.getFailure(ApiConstant.CODE_SIGN_ERROR, "教练ID不能为空！");
		String url = gymApiUrl + GymRemoteUtil.getGymListByCoachIdUrl();
		Map<String, String>	params = new HashMap<String, String>();
		params.put("coachId", String.valueOf(coachId));
		if(StringUtils.isNotBlank(order)){
			params.put("order", order);
			params.put("asc", String.valueOf(asc));
		}
		params.put("pageNo", String.valueOf(from));
		params.put("pageSize", String.valueOf(maxnum));
		return getObjectList(GymListResponse.class, url, params, TIME_OUT_);
	}
	
	@Override
	public ErrorCode<Integer> getGymCountByCoachId(Long coachId) {
		if(coachId == null) return ErrorCode.getFailure(ApiConstant.CODE_SIGN_ERROR, "教练ID不能为空！");
		String url = gymApiUrl + GymRemoteUtil.getGymCountByCoachIdUrl();
		Map<String, String>	params = new HashMap<String, String>();
		params.put("coachId", String.valueOf(coachId));
		return getRemoteCount(url, params, TIME_OUT_);
	}
	
	@Override
	public ErrorCode<List<RemoteGym>> getGymListByCourseId(Long courseId) {
		return getGymListByCourseId(courseId, "clickedtimes", false, 0, 100);
	}
	
	@Override
	public ErrorCode<List<RemoteGym>> getGymListByCourseId(Long courseId, String order, boolean asc, int from, int maxnum) {
		if(courseId == null) return ErrorCode.getFailure(ApiConstant.CODE_SIGN_ERROR, "教练ID不能为空！");
		String url = gymApiUrl + GymRemoteUtil.getGymListByCourseIdUrl();
		Map<String, String> params = new HashMap<String, String>();
		params.put("itemId", String.valueOf(courseId));
		if(StringUtils.isNotBlank(order)){
			params.put("order", order);
			params.put("asc", String.valueOf(asc));
		}
		params.put("pageNo", String.valueOf(from));
		params.put("pageSize", String.valueOf(maxnum));
		return getObjectList(GymListResponse.class, url, params, TIME_OUT_);
	}
	
	@Override
	public ErrorCode<Integer> getGymCountByCourseId(Long courseId) {
		if(courseId == null) return ErrorCode.getFailure(ApiConstant.CODE_SIGN_ERROR, "教练ID不能为空！");
		String url = gymApiUrl + GymRemoteUtil.getGymCountByCourseIdUrl();
		Map<String, String> params = new HashMap<String, String>();
		params.put("itemId", String.valueOf(courseId));
		return getRemoteCount(url, params, TIME_OUT_);
	}
	
	@Override
	public ErrorCode<List<RemoteGym>> getGymList(String citycode, String countycode, String indexareacode, String order, boolean asc, int from, int maxnum) {
		String url = gymApiUrl + GymRemoteUtil.getGymListUrl();
		Map<String, String> params = new HashMap<String, String>();
		if(StringUtils.isNotBlank(citycode)) params.put("citycode", citycode);
		if(StringUtils.isNotBlank(countycode)) params.put("countycode", countycode);
		if(StringUtils.isNotBlank(indexareacode)) params.put("indexareacode", indexareacode);
		if(StringUtils.isNotBlank(order)){
			params.put("order", order);
			params.put("asc", String.valueOf(asc));
		}
		params.put("pageNo", String.valueOf(from));
		params.put("pageSize", String.valueOf(maxnum));
		return getObjectList(GymListResponse.class, url, params, TIME_OUT_);
	}
	
	@Override
	public ErrorCode<Integer> getGymCount(String citycode, String countycode, String indexareacode){
		String url = gymApiUrl + GymRemoteUtil.getGymCountUrl();
		Map<String, String> params = new HashMap<String, String>();
		if(StringUtils.isNotBlank(citycode)) params.put("citycode", citycode);
		if(StringUtils.isNotBlank(countycode)) params.put("countycode", countycode);
		if(StringUtils.isNotBlank(indexareacode)) params.put("indexareacode", indexareacode);
		return getRemoteCount(url, params, TIME_OUT_);
	}
	
	@Override
	public ErrorCode<RemoteCourse> getRemoteCourse(Serializable courseId, boolean cache){
		if(courseId == null) return ErrorCode.getFailure(ApiConstant.CODE_SIGN_ERROR, "项目ID不能为空！");
		//String key = CacheConstant.buildKey(GymRemoteUtil.CACHE_KEY_GYMCOURSE, gymId);
		RemoteCourse response = null; //(RemoteCourse) cacheService.get(CacheConstant.REGION_HALFDAY, key);
		if(!cache || response == null){
			String url = gymApiUrl + GymRemoteUtil.getCourseUrl();
			Map<String, String> params = new HashMap<String, String>();
			params.put("itemId", String.valueOf(courseId));
			ErrorCode<DataWrapper> code = getObject(DataWrapper.class, url, params, TIME_OUT_);
			if(!code.isSuccess()) return ErrorCode.getFailure(code.getErrcode(), code.getMsg());
			response = code.getRetval().getItem();
			if(response == null) return ErrorCode.getFailure(ApiConstant.CODE_SIGN_ERROR, "数据转换错误！");
			//cacheService.set(CacheConstant.REGION_HALFDAY, key, response);
		}
		return ErrorCode.getSuccessReturn(response);
	}
	@Override
	public ErrorCode<List<RemoteCourse>> getRemoteCourseIdList(List<Long> idList){
		if(CollectionUtils.isEmpty(idList)) return ErrorCode.getFailure(ApiConstant.CODE_SIGN_ERROR, "项目ID不能为空或空集合！");
		String url = gymApiUrl + GymRemoteUtil.getCourseIdListUrl();
		Map<String, String> params = new HashMap<String, String>();
		params.put("ids", StringUtils.join(idList, ","));
		return getObjectList(RemoteCourseListResponse.class, url, params, TIME_OUT_);
	}
	@Override
	public ErrorCode<List<RemoteCourse>> getHotCourseList(int from, int maxnum) {
		return getCourseListByOrder("clickedtimes", false, from, maxnum);
	}
	
	@Override
	public ErrorCode<List<RemoteCourse>> getCourseListByOrder(String order, boolean asc, int from, int maxnum) {
		String url = gymApiUrl + GymRemoteUtil.getCourseListUrl();
		Map<String, String> params = new HashMap<String, String>();
		if(StringUtils.isNotBlank(order)){
			params.put("order", order);
			params.put("asc", String.valueOf(asc));
		}
		params.put("pageNo", String.valueOf(from));
		params.put("pageSize", String.valueOf(maxnum));
		return getObjectList(RemoteCourseListResponse.class, url, params, TIME_OUT_);
	}
	
	@Override
	public ErrorCode<Integer> getCourseCount() {
		String url = gymApiUrl + GymRemoteUtil.getCourseCountUrl();
		Map<String, String> params = new HashMap<String, String>();
		return getRemoteCount(url, params, TIME_OUT_);
	}

	@Override
	public ErrorCode<List<RemoteCourse>> getSubCourseListById(Serializable courseId) {
		if(courseId == null) return ErrorCode.getFailure(ApiConstant.CODE_SIGN_ERROR, "项目ID不能为空！");
		String url = gymApiUrl + GymRemoteUtil.getCourseBySubIdUrl();
		Map<String, String> params = new HashMap<String, String>();
		params.put("itemId", String.valueOf(courseId));
		return getObjectList(RemoteCourseListResponse.class, url, params, TIME_OUT_);
	}
	
	@Override
	public ErrorCode<RemoteSpecialCourse> getSpecialCourse(Serializable specialCourseId, boolean cache){
		if(specialCourseId == null) return ErrorCode.getFailure(ApiConstant.CODE_SIGN_ERROR, "课程ID不能为空！");
		//String key = CacheConstant.buildKey(GymRemoteUtil.CACHE_KEY_SPECIALCOURSE, specialCourseId);
		RemoteSpecialCourse response = null;//(RemoteSpecialCourse) cacheService.get(CacheConstant.REGION_HALFDAY, key);
		if(!cache || response == null){
			String url = gymApiUrl + GymRemoteUtil.getSpecialUrl();
			Map<String, String> params =  new HashMap<String, String>();
			params.put("courseId", String.valueOf(specialCourseId));
			ErrorCode<DataWrapper> code = getObject(DataWrapper.class, url, params, TIME_OUT_);
			if(!code.isSuccess()) return ErrorCode.getFailure(code.getErrcode(), code.getMsg());
			DataWrapper dataWrapper = code.getRetval();
			if(dataWrapper.getCourse() == null) return ErrorCode.getFailure(ApiConstant.CODE_SIGN_ERROR, "数据转换错误！");
			response = dataWrapper.getCourse();
			//cacheService.set(CacheConstant.REGION_HALFDAY, key, response);
		}
		return ErrorCode.getSuccessReturn(response);
	}
	
	@Override
	public ErrorCode<List<RemoteSpecialCourse>> getSpecialCourseIdList(List<Long> idList){
		if(CollectionUtils.isEmpty(idList)) return ErrorCode.getFailure(ApiConstant.CODE_SIGN_ERROR, "课程ID不能为空或空集合！");
		String url = gymApiUrl + GymRemoteUtil.getSpecialIdListUrl();
		Map<String, String> params = new HashMap<String, String>();
		params.put("ids", StringUtils.join(idList, ","));
		return getObjectList(SpecialCourseListResponse.class, url, params, TIME_OUT_);
	}
	
	@Override
	public ErrorCode<List<RemoteSpecialCourse>> getSpecialCourseListByGymId(Long gymId) {
		return getSpecialCourseListByGymId(gymId, "clickedtimes", false, 0, 100);
	}

	@Override
	public ErrorCode<List<RemoteSpecialCourse>> getSpecialCourseListByGymId(Long gymId, String order, boolean asc, int from, int maxnum) {
		if(gymId == null) return ErrorCode.getFailure(ApiConstant.CODE_SIGN_ERROR, "场馆ID不能为空！");
		String url = gymApiUrl + GymRemoteUtil.getSpecialListByGymIdUrl();
		Map<String, String> params = new HashMap<String, String>();
		params.put("gymId", String.valueOf(gymId));
		if(StringUtils.isNotBlank(order)){
			params.put("order", order);
			params.put("asc", String.valueOf(asc));
		}
		params.put("pageNo", String.valueOf(from));
		params.put("pageSize", String.valueOf(maxnum));
		return getObjectList(SpecialCourseListResponse.class, url, params, TIME_OUT_);
	}
	
	@Override
	public ErrorCode<Integer> getSpecialCourseCountByGymId(Long gymId) {
		if(gymId == null) return ErrorCode.getFailure(ApiConstant.CODE_SIGN_ERROR, "场馆ID不能为空！");
		String url = gymApiUrl + GymRemoteUtil.getSpecialCountByGymIdUrl();
		Map<String, String> params = new HashMap<String, String>();
		params.put("gymId", String.valueOf(gymId));
		return getRemoteCount(url, params, TIME_OUT_);
	}
	
	@Override
	public ErrorCode<RemoteCoach> getRemoteCoach(Serializable coachId, boolean cache) {
		if(coachId == null) return ErrorCode.getFailure(ApiConstant.CODE_SIGN_ERROR, "教练ID不能为空！");
		//String key = CacheConstant.buildKey(GymRemoteUtil.CACHE_KEY_GYMCOACH, coachId);
		RemoteCoach response = null;//(RemoteCoach) cacheService.get(CacheConstant.REGION_HALFDAY, key);
		if(!cache || response == null){
			String url = gymApiUrl + GymRemoteUtil.getCoachUrl();
			Map<String, String> params =  new HashMap<String, String>();
			params.put("coachId", String.valueOf(coachId));
			ErrorCode<DataWrapper> code = getObject(DataWrapper.class, url, params, TIME_OUT_);
			if(!code.isSuccess()) return ErrorCode.getFailure(code.getErrcode(), code.getMsg());
			response = code.getRetval().getGymCoach();
			if(response == null) return ErrorCode.getFailure(ApiConstant.CODE_SIGN_ERROR, "数据转换错误！");
			//cacheService.set(CacheConstant.REGION_HALFDAY, key, response);
		}
		return ErrorCode.getSuccessReturn(response);
	}
	
	@Override
	public ErrorCode<List<RemoteCoach>> getRemoteCoachIdList(List<Long> idList) {
		if(CollectionUtils.isEmpty(idList)) return ErrorCode.getFailure(ApiConstant.CODE_SIGN_ERROR, "教练ID不能为空或空集合！");
		String url = gymApiUrl + GymRemoteUtil.getCoachIdListUrl();
		Map<String, String> params =  new HashMap<String, String>();
		params.put("idList", StringUtils.join(idList, ","));
		return getObjectList(RemoteCoachListResponse.class, url, params, TIME_OUT_);
	}

	@Override
	public ErrorCode<List<RemoteCoach>> getCoachListByGymId(Long gymId) {
		return getCoachListByGymId(gymId, "clickedtimes", false, 0, 100);
	}

	@Override
	public ErrorCode<List<RemoteCoach>> getCoachListByGymId(Long gymId, String order, boolean asc, int from, int maxnum) {
		if(gymId == null) return ErrorCode.getFailure(ApiConstant.CODE_SIGN_ERROR, "场馆ID不能为空！");
		String url = gymApiUrl + GymRemoteUtil.getCoachListByGymIdUrl();
		Map<String, String> params = new HashMap<String, String>();
		params.put("gymId", String.valueOf(gymId));
		if(StringUtils.isNotBlank(order)){
			params.put("order", order);
			params.put("asc", String.valueOf(asc));
		}
		params.put("from", String.valueOf(from));
		params.put("maxnum", String.valueOf(maxnum));
		return getObjectList(RemoteCoachListResponse.class, url, params, TIME_OUT_);
	}
	
	@Override
	public ErrorCode<Integer> getCoachCountByGymId(Long gymId) {
		if(gymId == null) return ErrorCode.getFailure(ApiConstant.CODE_SIGN_ERROR, "场馆ID不能为空！");
		String url = gymApiUrl + GymRemoteUtil.getCoachCountByGymIdUrl();
		Map<String, String> params = new HashMap<String, String>();
		params.put("gymId", String.valueOf(gymId));
		return getRemoteCount(url, params, TIME_OUT_);
	}

	@Override
	public ErrorCode<List<BrandnameCount>> getGroupGymByBrand(String citycode, String countycode, String indexareacode) {
		String url = gymApiUrl + GymRemoteUtil.getGroupGymByBrandUrl(); 
		Map<String, String> params = new HashMap<String, String>();
		if(StringUtils.isNotBlank(citycode)) params.put("citycode", citycode);
		if(StringUtils.isNotBlank(countycode)) params.put("countycode", countycode);
		if(StringUtils.isNotBlank(indexareacode)) params.put("indexareacode", indexareacode);
		return getObjectList(BrandnameCountList.class, url, params, TIME_OUT_);
	}

	@Override
	public ErrorCode<List<RemoteCourse>> getCourseListByCoachId(Long coachId) {
		return getCourseListByCoachId(coachId, "clickedtimes", false, 0, 100);
	}

	@Override
	public ErrorCode<List<RemoteCourse>> getCourseListByCoachId(Long coachId, String order, boolean asc, int from, int maxnum) {
		if(coachId == null) return ErrorCode.getFailure(ApiConstant.CODE_SIGN_ERROR, "教练ID不能为空！");
		String url = gymApiUrl + GymRemoteUtil.getCourseListByCoachIdUrl();
		Map<String,String> params = new HashMap<String, String>();
		params.put("coachId", String.valueOf(coachId));
		if(StringUtils.isNotBlank(order)){
			params.put("order", order);
			params.put("asc", String.valueOf(asc));
		}
		params.put("pageNo", String.valueOf(from));
		params.put("pageSize", String.valueOf(maxnum));
		return getObjectList(RemoteCourseListResponse.class, url, params, TIME_OUT_);
	}
	
	@Override
	public ErrorCode<Integer> getCourseCountByCoachId(Long coachId) {
		if(coachId == null) return ErrorCode.getFailure(ApiConstant.CODE_SIGN_ERROR, "教练ID不能为空！");
		String url = gymApiUrl + GymRemoteUtil.getCourseCountByCoachIdUrl();
		Map<String,String> params = new HashMap<String, String>();
		params.put("coachId", String.valueOf(coachId));
		return getRemoteCount(url, params, TIME_OUT_);
	}

	@Override
	public ErrorCode<List<CardItem>> getValidGymCardListByGymId(Long gymId, String itemType, Integer minprice, Integer maxprice, String order, boolean asc, int from, int maxnum) {
		if(gymId == null) return ErrorCode.getFailure(ApiConstant.CODE_SIGN_ERROR, "场馆ID不能为空！");
		String url = gymApiUrl + GymRemoteUtil.getCardListByGymIdUrl();
		Map<String, String> params = new HashMap<String, String>();
		params.put("gymId", String.valueOf(gymId));
		if(StringUtils.isNotBlank(itemType)) params.put("type", itemType);
		if(minprice != null) params.put("minprice", String.valueOf(minprice));
		if(maxprice != null) params.put("maxprice", String.valueOf(maxprice));
		if(StringUtils.isNotBlank(order)){
			params.put("order", order);
			params.put("asc", String.valueOf(asc));
		}
		params.put("pageNo", String.valueOf(from));
		params.put("pageSize", String.valueOf(maxnum));
		return getObjectList(CardItemListResponse.class, url, params, TIME_OUT_);
	}
	
	@Override
	public ErrorCode<Integer> getValidGymCardCountByGymId(Long gymId, String itemType, Integer minprice, Integer maxprice) {
		if(gymId == null) return ErrorCode.getFailure(ApiConstant.CODE_SIGN_ERROR, "场馆ID不能为空！");
		String url = gymApiUrl + GymRemoteUtil.getCardCoutByGymIdUrl();
		Map<String, String> params = new HashMap<String, String>();
		params.put("gymId", String.valueOf(gymId));
		if(StringUtils.isNotBlank(itemType)) params.put("type", itemType);
		if(minprice != null) params.put("minprice", String.valueOf(minprice));
		if(maxprice != null) params.put("maxprice", String.valueOf(maxprice));
		return getRemoteCount(url, params, TIME_OUT_);
	}
	
	@Override
	public ErrorCode<BookingRecord> getCourseBooking(Serializable recordId, boolean cache){
		if(recordId == null) return ErrorCode.getFailure(ApiConstant.CODE_SIGN_ERROR, "预约ID不能为空！");
		//String key = CacheConstant.buildKey(GymRemoteUtil.CACHE_KEY_BOOKINGRECORD, recordId);
		BookingRecord response = null; //(BookingRecord) cacheService.get(CacheConstant.REGION_HALFDAY, key);
		if(!cache || response == null){
			String url = gymApiUrl + GymRemoteUtil.getBookingUrl();
			Map<String, String> params = new HashMap<String, String>();
			params.put("recordId", String.valueOf(recordId));
			ErrorCode<DataWrapper> code = getObject(DataWrapper.class, url, params, TIME_OUT_);
			if(!code.isSuccess()) return ErrorCode.getFailure(code.getErrcode(), code.getMsg());
			response = code.getRetval().getBookingRecord();
			if(response == null) return ErrorCode.getFailure(ApiConstant.CODE_SIGN_ERROR, "数据格式转换错误！");
			//cacheService.set(CacheConstant.REGION_HALFDAY, key, response);
		}
		return ErrorCode.getSuccessReturn(response);
	}
	
	@Override
	public ErrorCode<BookingRecord> getCourseBookingByUkey(String keyName, String keyValue, boolean cache){
		if(StringUtils.isBlank(keyName) || StringUtils.isBlank(keyValue)) return ErrorCode.getFailure(ApiConstant.CODE_SIGN_ERROR, "参数错误！");
		//String key = CacheConstant.buildKey(GymRemoteUtil.CACHE_KEY_BOOKINGRECORD, keyName, keyValue);
		BookingRecord response = null; //(BookingRecord) cacheService.get(CacheConstant.REGION_HALFDAY, key);
		if(!cache || response == null){
			String url = gymApiUrl + GymRemoteUtil.getBookingByUkeyUrl();
			Map<String, String> params = new HashMap<String, String>();
			params.put("keyName", keyName);
			params.put("keyValue", keyValue);
			ErrorCode<DataWrapper> code = getObject(DataWrapper.class, url, params, TIME_OUT_);
			if(!code.isSuccess()) return ErrorCode.getFailure(code.getErrcode(), code.getMsg());
			response = code.getRetval().getBookingRecord();
			if(response == null) return ErrorCode.getFailure(ApiConstant.CODE_SIGN_ERROR, "数据格式转换错误！");
			//cacheService.set(CacheConstant.REGION_HALFDAY, key, response);
		}
		return ErrorCode.getSuccessReturn(response);
	}
	
	@Override
	public ErrorCode<List<BookingRecord>> getCourseBookingListByGymId(Long gymId, Timestamp starttime, Timestamp endtime, String order, boolean asc, int from, int maxnum) {
		if(gymId == null) return ErrorCode.getFailure(ApiConstant.CODE_SIGN_ERROR, "场馆ID不能为空！");
		String url = gymApiUrl + GymRemoteUtil.getBookingListByGymIdUrl();
		Map<String,String> params = new HashMap<String, String>();
		params.put("gymId", String.valueOf(gymId));
		if(starttime != null) params.put("starttime", DateUtil.formatTimestamp(starttime));
		if(endtime != null) params.put("endtime", DateUtil.formatTimestamp(endtime));
		if(StringUtils.isNotBlank(order)){
			params.put("order", order);
			params.put("asc", String.valueOf(asc));
		}
		params.put("from", String.valueOf(from));
		params.put("maxnum", String.valueOf(maxnum));
		return getObjectList(BookingRecordListResponse.class, url, params, TIME_OUT_);
	}
	
	@Override
	public ErrorCode<Integer> getCourseBookingCountByGymId(Long gymId, Timestamp starttime, Timestamp endtime) {
		if(gymId == null) return ErrorCode.getFailure(ApiConstant.CODE_SIGN_ERROR, "场馆ID不能为空！");
		String url = gymApiUrl + GymRemoteUtil.getBookingCountByGymIdUrl();
		Map<String, String> params = new HashMap<String, String>();
		params.put("gymId", String.valueOf(gymId));
		if(starttime != null) params.put("starttime", DateUtil.formatTimestamp(starttime));
		if(endtime != null) params.put("endtime", DateUtil.formatTimestamp(endtime));
		return getRemoteCount(url, params, TIME_OUT_);
	}

	@Override
	public ErrorCode<String> updateCourseByField(Serializable courseId, String fieldName, Serializable fieldValue, boolean isCover) {
		if(courseId == null || StringUtils.isBlank(fieldName) || fieldValue == null) return ErrorCode.getFailure(ApiConstant.CODE_SIGN_ERROR, "所有参数不能为空！");
		String url = gymApiUrl + GymRemoteUtil.getUpdateCourseByPropertyUrl();
		Map<String, String> params = new HashMap<String, String>();
		params.put("courseId", String.valueOf(courseId));
		params.put("fieldName", fieldName);
		params.put("fieldValue", String.valueOf(fieldValue));
		params.put("isCover", String.valueOf(isCover));
		return getRemoteResult(url, params, TIME_OUT_);
	}

	@Override
	public ErrorCode<String> updateCoachByField(Serializable coachId, String fieldName, Serializable fieldValue, boolean isCover) {
		if(coachId == null || StringUtils.isBlank(fieldName) || fieldValue == null) return ErrorCode.getFailure(ApiConstant.CODE_SIGN_ERROR, "所有参数不能为空！");
		String url = gymApiUrl + GymRemoteUtil.getUpdateCoachByPropertyUrl();
		Map<String, String> params = new HashMap<String, String>();
		params.put("coachId", String.valueOf(coachId));
		params.put("fieldName", fieldName);
		params.put("fieldValue", String.valueOf(fieldValue));
		params.put("isCover", String.valueOf(isCover));
		return getRemoteResult(url, params, TIME_OUT_);
	}
	
	@Override
	public ErrorCode<String> updateGymByField(Serializable gymId, String fieldName, Serializable fieldValue, boolean isCover){
		if(gymId == null || StringUtils.isBlank(fieldName) || fieldValue == null) return ErrorCode.getFailure(ApiConstant.CODE_SIGN_ERROR, "所有参数不能为空！");
		String url = gymApiUrl + GymRemoteUtil.getUpdateGymByPropertyUrl();
		Map<String, String> params = new HashMap<String, String>();
		params.put("gymId", String.valueOf(gymId));
		params.put("fieldName", fieldName);
		params.put("fieldValue", String.valueOf(fieldValue));
		params.put("isCover", String.valueOf(isCover));
		return getRemoteResult(url, params, TIME_OUT_);
	}
	
	@Override
	public ErrorCode<String> updateRelatedByField(String tag, Serializable relatedid, String fieldName, Serializable fieldValue, boolean isCover) {
		ErrorCode<String> code = null;
		if(StringUtils.equals(tag, TagConstant.TAG_GYM)){
			code = updateGymByField(relatedid, fieldName, fieldValue, isCover);
		}else if(StringUtils.equals(tag, TagConstant.TAG_GYMCOURSE)){
			code = updateCourseByField(relatedid, fieldName, fieldValue, isCover);
		}else if(StringUtils.equals(tag, TagConstant.TAG_GYMCOACH)){
			code = updateCoachByField(relatedid, fieldName, fieldValue, isCover);
		}
		if(code == null) return ErrorCode.getFailure(ApiConstant.CODE_SIGN_ERROR, "类型错误！");
		return code;
	}
}
