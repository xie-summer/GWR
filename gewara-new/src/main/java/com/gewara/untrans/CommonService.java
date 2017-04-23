package com.gewara.untrans;

import java.sql.Timestamp;
import java.util.List;
import java.util.Map;

import com.gewara.model.BaseObject;
import com.gewara.model.bbs.Correction;
import com.gewara.model.bbs.commu.CommuTopic;
import com.gewara.model.common.BaseInfo;
import com.gewara.model.common.DataDictionary;
import com.gewara.model.common.Place;
import com.gewara.model.common.RelateToCity;
import com.gewara.model.common.Relationship;
import com.gewara.model.content.Bulletin;
import com.gewara.model.content.DiscountInfo;
import com.gewara.model.content.GewaCommend;
import com.gewara.model.content.HeadInfo;
import com.gewara.model.content.Link;
import com.gewara.model.content.PhoneAdvertisement;
import com.gewara.model.movie.GrabTicketSubject;
import com.gewara.model.movie.TempMovie;
import com.gewara.util.RelatedHelper;

/**
 * Service for News，DiscountInfo，Bulletin
 * @author <a href="mailto:acerge@163.com">gebiao(acerge)</a>
 * @since 2007-9-28下午02:05:17
 */
public interface CommonService {
	//1. 公告
	List<Bulletin> getCurrentBulletinsByRelatedidAndHotvalue(String citycode, Long relatedid, Integer hotvalue);
	List<Bulletin> getBulletinListByTag(String citycode, String tag);
	List<Bulletin> getBulletinListByTagAndTypeAndRelatedid(String citycode, String tag,String type,boolean isCommend,Long relatedid);
	//2. 折扣
	List<DiscountInfo> getCurrentDiscountInfoByRelatedid(String tag,Long relatedid);
	List<DiscountInfo> getDiscountInfoByRelatedidAndTag(Long relatedid, String tag);
	/**
	 * 按sportid查询优惠信息条数
	 * */
	Integer getDiscountInfoCount(Long sportid,String tag);
	void updateBulletinHotValue(Long id,Integer value);
	List<Bulletin> getBulletinListByHotvalue(String citycode, String tag, Integer hotvalue);
	
	List<Correction> getCorrectionList(String status,Timestamp starttime,Timestamp endtime,int from,int maxnum);
	Integer getCorrectionCount(String status,Timestamp starttime,Timestamp endtime);
	List<TempMovie> getTempMovieList(String tag,String status,Timestamp starttime,Timestamp endtime,String type,Integer point,int from,int maxnum);
	Integer getTempMovieCount(String tag,String status,Timestamp starttime,Timestamp endtime);
	List<Place> getPlaceList(String status,Timestamp starttime,Timestamp endtime,int from,int maxnum);
	Integer getPlaceCount(String status,Timestamp starttime,Timestamp endtime);
	/**
	 * 根据tag和relatedid更新场馆优惠卷标记
	 * @param tag
	 * @param relatedid
	 */
	void updateCoupon(String citycode, String tag, Long relatedid);
	
	List<Link> getLinkListByType(String type);
	
	void initGewaCommendList(String group, RelatedHelper rh, List<GewaCommend> gcList);
	GewaCommend getGewaCommendByRelatedid(String signname, Long relateid);
	List<GewaCommend> getGewaCommendListByParentid(String signname, Long parentid,boolean isAll);
	Integer getGewaCommendCount(String citycode, String signname, Long parentid, String tag, boolean isGtZero);
	Integer getGewaCommendCount(String citycode, List<String> signNameList, Long parentid, String tag, boolean isGtZero);
	
	List<GewaCommend> getGewaCommendList(Long parentid,  String signname, List tag, boolean isGtZero, int first, int maxnum);
	List<GewaCommend> getGewaCommendList(String citycode, String signname, Long parentid,String tag, boolean isGtZero, int first, int maxnum);
	List<GewaCommend> getGewaCommendList(String citycode, String signname, Long parentid, String tag, boolean isGtZero, String order, boolean asc, int from, int maxnum);
	List<GewaCommend> getGewaCommendList(String citycode,String countycode, String signname, Long parentid, String tag, boolean isGtZero, boolean isActivity, int first, int maxnum);
	List<GewaCommend> getGewaCommendList(String citycode, String signname, Long parentid, boolean isGtZero, boolean isdesc,int from, int maxnum);
	List<GewaCommend> getGewaCommendList(String citycode, List<String> signNameList, Long parentid, boolean isGtZero, boolean isdesc, boolean isActivity,int from, int maxnum);
	List<GewaCommend> getGewaCommendList(String citycode, String countycode, String signname, Long parentid, String tag, boolean isGtZero,boolean isActivity,boolean isStarttime, int first, int maxnum);
	/**
	 * 查询网站头部信息列表
	 */
	List<HeadInfo> getHeadInfoList(String board, String citycode, int from,int maxNum);
	Integer getHeadInfoCount(String board);
	/**
	 * 查询圈子话题板块列表
	 */
	List<CommuTopic> getCommuTopicList(Long commuid,int from,int maxnum);
	/**
	 * 查询圈子话题板块数量
	 * @param commuid
	 * @return
	 */
	Integer getCommuTopicCount(Long commuid);
	
	/**
	 * 根据relatedid, tag,查询推荐场馆数据
	 * @param relatedid
	 * @param tag
	 * @param signname
	 * @param isGtZero
	 * @return
	 */
	List<GewaCommend> getGewaCommendListByid(Long relatedid, String tag, String signname, boolean isGtZero);
	
	/****
	 * 
	 * 新闻 / 活动 添加, 处理对应的关联城市名称. 
	 */
	<T extends BaseObject> Map<Long, String> initRelateCityName(List<T> list);
	<T extends BaseObject> Map<Long, List<Map>>  initRelateToCityName(List<T> list, String tag);
	
	/***
	 *  分站数据分享 - 
	 * */
	List<GewaCommend> getCommendListByRelatedid(Long relatedid, String signname, String tag);
	
	Integer getRelationshipCount(String category,  Long relatedid1, String tag, Long relatedid2, Timestamp validtime);
	List<Relationship> getRelationshipList(String category,  Long relatedid1, String tag, Long relatedid2, Timestamp validtime, int from, int maxnum);
	Relationship getRelationship(String category, String tag, Long relatedid2, Timestamp validtime);
	List<RelateToCity> getRelateToCity(String tag, Long relatedid, String citycode, String flag);
	List<GewaCommend> getGewaCommendListByRelatedid(String citycode, String signname, Long relatedid, String tag, boolean isGtZero, int first, int maxnum);
	
	List<GrabTicketSubject> getGrabTicketSubjectList(String citycode, String tag, int from, int maxnum);
	/**
	 * key=relatedid+tag value=activityCount
	 */
	Map<String, Integer> getActivityCount();
	/**
	 * key=relatedid+tag value=newsCount
	 */
	Map<String, Integer> getNewsCount();
	/**
	 * key=relatedid+tag value=pictureCount
	 */
	Map<String, Integer> getPictureCount();
	/**
	 * key=relatedid+tag value=videoCount
	 */
	Map<String, Integer> getVideoCount();
	/**
	 * key=relatedid+tag value=commentCount
	 */
	Map<String, Integer> getCommentCount();
	/**
	 * key=relatedid+tag value=diaryCount
	 */
	Map<String, Integer> getDiaryCount();
	/**
	 * key=relatedid+tag value=commentCount
	 */
	Map<String, Integer> getCommuCount();
	/**
	 * key=sportitemid  value=sportCount
	 */
	Map<String, Integer> getSportItemSportCount();
	
	/**
	 * 获取未删除的广告信息
	 */
	List<PhoneAdvertisement> getNewPhoneAdvertisementList(String status);
	
	/**
	 * 获取当前所有用户互动数据
	 * @return map()
	 */
	Map getCurIndexDataSheet();
	/**
	 * 通过对象名称查询数据字典数量，如objectName为空或空字符串，则查询所有
	 * @param objectName 对象名称
	 * @return 数据字典数量 如查询不到则为 0
	 */
	Integer getDataDictionaryCount(String objectName);
	/**
	 *	通过对象名称查询数据字典，如objectName为空或空字符串，则查询所有
	 * @param objectName 对象名称
	 * @param from 开始查询数
	 * @param maxnum 查询条数
	 * @return 数据字典集合 如查询不到则 集合size为0 
	 */
	List<DataDictionary> getDataDictionaryList(String objectName, int from, int maxnum);
	
	<T extends BaseInfo> List<T> getBaiDuNearPlaceObjectList(Class<T> clazz, String citycode, String countycode, String bpointx, String bpointy, double spaceRound);
}
