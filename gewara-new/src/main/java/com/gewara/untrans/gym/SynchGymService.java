package com.gewara.untrans.gym;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.List;

import com.gewara.model.pay.GymOrder;
import com.gewara.support.ErrorCode;
import com.gewara.xmlbind.common.BrandnameCount;
import com.gewara.xmlbind.gym.BookingRecord;
import com.gewara.xmlbind.gym.CardItem;
import com.gewara.xmlbind.gym.RemoteCoach;
import com.gewara.xmlbind.gym.RemoteCourse;
import com.gewara.xmlbind.gym.RemoteGym;
import com.gewara.xmlbind.gym.RemoteSpecialCourse;

public interface SynchGymService {
	/**
	 * 健身卡编号获取健身卡详细
	 * @param cid			健身卡编号
	 * @param cache	
	 * @return	
	 */
	ErrorCode<CardItem> getGymCardItem(Serializable cid, boolean cache);

	/**
	 * 健身卡编号获取购买健身卡详细
	 * @param cid			健身卡编号
	 * @param speciallist	所选课程编号集合，以,隔开
	 * @return
	 */
	ErrorCode<CardItem> showBuyCardItem(Serializable cid, String speciallist, boolean cache);

	/**
	 * 通过ID获取健身场馆信息
	 * @param gymId		场馆ID
	 * @param cache		
	 * @return
	 */
	ErrorCode<RemoteGym> getRemoteGym(Serializable gymId, boolean cache);

	/**
	 * 确认订单信息
	 * @param order
	 * @return
	 */
	ErrorCode<String> lockCard(GymOrder order);
	
	/**
	 * 通过场馆ID集合获取场馆信息
	 * @param idList	场馆ID集合信息
	 * @return
	 */
	ErrorCode<List<RemoteGym>> getRemoteGymIdList(List<Long> idList);
	
	/**
	 * 通过教练ID查询场馆信息
	 * @param coachId	教练ID
	 * @return
	 */
	ErrorCode<List<RemoteGym>> getGymListByCoachId(Long coachId);
	
	/**
	 * 通过教练ID查询场馆信息
	 * @param coachId	教练ID
	 * @return
	 */
	ErrorCode<List<RemoteGym>> getGymListByCoachId(Long coachId, String order, boolean asc, int from, int maxnum);
	
	/**
	 * 通过教练ID查询场馆数量
	 * @param coachId	教练ID
	 * @return
	 */
	ErrorCode<Integer> getGymCountByCoachId(Long coachId);
	
	/**
	 * 通过项目ID查询场馆信息
	 * @param courseId	课程项目ID
	 * @return
	 */
	ErrorCode<List<RemoteGym>> getGymListByCourseId(Long courseId);
	
	/**
	 * 通过项目ID查询场馆信息
	 * @param courseId	课程项目ID
	 * @return
	 */
	ErrorCode<List<RemoteGym>> getGymListByCourseId(Long courseId, String order, boolean asc, int from, int maxnum);
	
	/**
	 * 通过项目ID查询场馆数量
	 * @param courseId	项目ID
	 * @return
	 */
	ErrorCode<Integer> getGymCountByCourseId(Long courseId);
	
	/**
	 * 根据城市、区域、商圈等编码，查询场馆信息
	 * @param citycode			城市编码
	 * @param countycode		区域编码
	 * @param indexareacode		商圈编码
	 * @param order				排序字段
	 * @param from				从第几行开始
	 * @param maxnum			最大查询值
	 * @return
	 */
	ErrorCode<List<RemoteGym>> getGymList(String citycode, String countycode, String indexareacode, String order, boolean asc, int from, int maxnum);
	/**
	 * 根据城市、区域、商圈等编码，查询场馆数量
	 * @param citycode			城市编码
	 * @param countycode		区域编码
	 * @param indexareacode		商圈编码
	 * @return
	 */
	ErrorCode<Integer> getGymCount(String citycode, String countycode, String indexareacode);
	/**
	 * 通过项目ID查询项目信息
	 * @param courseId		项目ID
	 * @param cache		
	 * @return
	 */
	ErrorCode<RemoteCourse> getRemoteCourse(Serializable courseId, boolean cache);
	
	/**
	 * 通过项目ID集合获取项目信息
	 * @param idList	项目ID集合信息
	 * @return
	 */
	ErrorCode<List<RemoteCourse>> getRemoteCourseIdList(List<Long> idList);
	
	/**
	 * 查询课程信息，默认点击量降序排序
	 * @param from
	 * @param maxnum
	 * @return
	 */
	ErrorCode<List<RemoteCourse>> getHotCourseList(int from, int maxnum);
	/**
	 * 查询课程信息
	 * @param order				排序字段
	 * @param asc				是否升序
	 * @param from				
	 * @param maxnum
	 * @return
	 */
	ErrorCode<List<RemoteCourse>> getCourseListByOrder(String order, boolean asc, int from, int maxnum);
	
	/**
	 * 查询课程数量
	 * @return
	 */
	ErrorCode<Integer> getCourseCount();
	
	/**
	 * 获取子项目信息
	 * @param courseId 	项目ID
	 * @return
	 */
	ErrorCode<List<RemoteCourse>> getSubCourseListById(Serializable courseId);
	
	/**
	 * 通过课程信息查询课程信息
	 * @param specialCourseId
	 * @param cache
	 * @return
	 */
	ErrorCode<RemoteSpecialCourse> getSpecialCourse(Serializable specialCourseId, boolean cache);
	
	/**
	 * 通过课程ID信息查询课程数据
	 * @param idList		课程ID集合信息
	 * @return
	 */
	ErrorCode<List<RemoteSpecialCourse>> getSpecialCourseIdList(List<Long> idList);
	
	/**
	 * 通过场馆ID查询课程信息，默认点击量排序
	 * @param gymId
	 * @return
	 */
	ErrorCode<List<RemoteSpecialCourse>> getSpecialCourseListByGymId(Long gymId);
	
	/**
	 * 通过场馆ID查询课程信息
	 * @param gymId			场馆ID
	 * @param order			排序字段
	 * @param asc			是否升序
	 * @param from			
	 * @param maxnum
	 * @return
	 */
	ErrorCode<List<RemoteSpecialCourse>> getSpecialCourseListByGymId(Long gymId, String order, boolean asc, int from, int maxnum);
	
	/**
	 * 通过场馆ID查询课程数量
	 * @param gymId			场馆ID
	 * @return
	 */
	ErrorCode<Integer> getSpecialCourseCountByGymId(Long gymId);
	
	/**
	 * 通过教练ID查询教练信息
	 * @param coachId		教练ID
	 * @param cache		
	 * @return
	 */
	ErrorCode<RemoteCoach> getRemoteCoach(Serializable coachId, boolean cache);
	
	/**
	 * 通过教练ID集合查询教练信息
	 * @param idList		教练ID
	 * @return
	 */
	ErrorCode<List<RemoteCoach>> getRemoteCoachIdList(List<Long> idList);
	/**
	 * 通过场馆ID查询教练信息
	 * @param gymId			场馆ID		
	 * @return
	 */
	ErrorCode<List<RemoteCoach>> getCoachListByGymId(Long gymId);
	
	/**
	 * 通过场馆ID查询教练信息
	 * @param gymId				场馆ID
	 * @param order				排序字段
	 * @param asc				是否升序
	 * @param from				第几行数据开始
	 * @param maxnum			查询最大数量值
	 * @return
	 */
	ErrorCode<List<RemoteCoach>> getCoachListByGymId(Long gymId, String order, boolean asc, int from, int maxnum);
	
	/**
	 * 通过场馆ID查询教练数量
	 * @param gymId				场馆ID
	 * @return
	 */
	ErrorCode<Integer> getCoachCountByGymId(Long gymId);
	
	/**
	 * 根据品牌名称分组，查询品牌数量
	 * @param citycode			城市编码
	 * @param countycode		区域编码
	 * @param indexareacode		商圈编码
	 * @return
	 */
	ErrorCode<List<BrandnameCount>> getGroupGymByBrand(String citycode, String countycode, String indexareacode);
	
	/**
	 * 通过教练ID查询项目信息
	 * @param coachId			教练ID
	 * @return
	 */
	ErrorCode<List<RemoteCourse>> getCourseListByCoachId(Long coachId);
	
	/**
	 * 通过教练ID查询项目信息
	 * @param coachId			教练ID
	 * @param order				排序字段
	 * @param asc				是否升序
	 * @param from				第几行数据开始
	 * @param maxnum			查询最大数量值
	 * @return
	 */
	ErrorCode<List<RemoteCourse>> getCourseListByCoachId(Long coachId, String order, boolean asc, int from, int maxnum);
	
	/**
	 * 通过教练ID查询项目数量
	 * @param coachId			教练ID
	 * @return
	 */
	ErrorCode<Integer> getCourseCountByCoachId(Long coachId);
	
	/**
	 * 通过场馆ID、卡类型、价格区间查询健身卡数据
	 * @param gymId				场馆ID
	 * @param itemType			卡类型
	 * @param minprice			最小价格
	 * @param maxprice			最大价格
	 * @param order				排序字段
	 * @param asc				是否升序
	 * @param from				第几行数据开始
	 * @param maxnum			查询最大数量值
	 * @return
	 */
	ErrorCode<List<CardItem>> getValidGymCardListByGymId(Long gymId, String itemType, Integer minprice, Integer maxprice, String order, boolean asc, int from, int maxnum);
	
	/**
	 * 通过场馆ID、卡类型、价格区间查询健身卡数量
	 * @param gymId				场馆ID
	 * @param itemType			卡类型
	 * @param minprice			最小价格
	 * @param maxprice			最大价格
	 * @return
	 */
	ErrorCode<Integer> getValidGymCardCountByGymId(Long gymId, String itemType, Integer minprice, Integer maxprice);
	
	/**
	 * 通过预约ID查询预约信息
	 * @param recordId			预约ID
	 * @param cache			是否缓存
	 * @return
	 */
	ErrorCode<BookingRecord> getCourseBooking(Serializable recordId, boolean cache);
	
	/**
	 * 通过预约唯一属性名称与属性值查询预约信息
	 * @param keyName			属性名称
	 * @param keyValue			属性值
	 * @param cache			是否缓存
	 * @return
	 */
	ErrorCode<BookingRecord> getCourseBookingByUkey(String keyName, String keyValue, boolean cache);
	
	/**
	 * 通过场馆ID查询预约课程的信息
	 * @param gymId				场馆ID
	 * @param starttime			开始时间段
	 * @param endtime			结束时间段
	 * @param order				排序字段
	 * @param asc				是否升序
	 * @param from
	 * @param maxnum
	 * @return
	 */
	ErrorCode<List<BookingRecord>> getCourseBookingListByGymId(Long gymId, Timestamp starttime, Timestamp endtime, String order, boolean asc, int from, int maxnum);
	
	/**
	 * 通过场馆ID查询预约课程的数量
	 * @param gymId				场馆ID
	 * @param starttime			开始时间段
	 * @param endtime			结束时间段
	 * @return
	 */
	ErrorCode<Integer> getCourseBookingCountByGymId(Long gymId, Timestamp starttime, Timestamp endtime);
	
	/**
	 * 通过项目ID更新属性值
	 * @param courseId			项目ID
	 * @param fieldName			属性名称
	 * @param fieldValue		属性值
	 * @param isCover			是否覆盖(针对Integer类型，false是加减)
	 * @return
	 */
	ErrorCode<String> updateCourseByField(Serializable courseId, String fieldName, Serializable fieldValue, boolean isCover);
	
	
	/**
	 * 通过教练ID更新属性值
	 * @param coachId			教练ID
	 * @param fieldName			属性名称
	 * @param fieldValue		属性值
	 * @param isCover			是否覆盖(针对Integer类型，false是加减)
	 * @return
	 */
	ErrorCode<String> updateCoachByField(Serializable coachId, String fieldName, Serializable fieldValue, boolean isCover);

	/**
	 * 通过场馆ID更新属性值
	 * @param coachId			教练ID
	 * @param fieldName			属性名称
	 * @param fieldValue		属性值
	 * @param isCover			是否覆盖(针对Integer类型，false是加减)
	 * @return
	 */
	ErrorCode<String> updateGymByField(Serializable gymId, String fieldName, Serializable fieldValue, boolean isCover);
	
	/**
	 * 通过关联类型与ID更新属性值
	 * @param tag				类型
	 * @param relatedid			类型ID
	 * @param fieldName			属性名称
	 * @param fieldValue		属性值
	 * @param isCover			是否覆盖(针对Integer类型，false是加减)
	 * @return
	 */
	ErrorCode<String> updateRelatedByField(String tag, Serializable relatedid, String fieldName, Serializable fieldValue, boolean isCover);
}
