package com.gewara.untrans.impl;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.gewara.constant.AdminCityContant;
import com.gewara.constant.DiaryConstant;
import com.gewara.constant.TagConstant;
import com.gewara.constant.content.SignName;
import com.gewara.constant.sys.CacheConstant;
import com.gewara.constant.sys.MongoData;
import com.gewara.helper.sys.RelateClassHelper;
import com.gewara.model.BaseObject;
import com.gewara.model.bbs.Correction;
import com.gewara.model.bbs.commu.Commu;
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
import com.gewara.model.drama.Drama;
import com.gewara.model.movie.GrabTicketSubject;
import com.gewara.model.movie.TempMovie;
import com.gewara.mongo.MongoService;
import com.gewara.service.DaoService;
import com.gewara.service.bbs.DiaryService;
import com.gewara.support.ErrorCode;
import com.gewara.support.ReadOnlyTemplate;
import com.gewara.untrans.CacheService;
import com.gewara.untrans.CommonService;
import com.gewara.untrans.RelateService;
import com.gewara.untrans.activity.SynchActivityService;
import com.gewara.util.BeanUtil;
import com.gewara.util.ClassUtils;
import com.gewara.util.DateUtil;
import com.gewara.util.LongitudeAndLatitude;
import com.gewara.util.OuterSorter;
import com.gewara.util.RelatedHelper;
import com.gewara.util.VmUtils;
import com.gewara.xmlbind.activity.RemoteActivity;

@Service("commonService")
public class CommonServiceImpl implements CommonService {
	@Autowired@Qualifier("mongoService")
	private MongoService mongoService;
	@Autowired@Qualifier("cacheService")
	private CacheService cacheService;
	@Autowired@Qualifier("daoService")
	private DaoService daoService;
	@Autowired@Qualifier("diaryService")
	private DiaryService diaryService;
	public void setDiaryService(DiaryService diaryService){
		this.diaryService = diaryService;
	}
	@Autowired@Qualifier("synchActivityService")
	private SynchActivityService synchActivityService;
	@Autowired@Qualifier("relateService")
	private RelateService relateService;
	@Autowired@Qualifier("readOnlyTemplate")
	private ReadOnlyTemplate readOnlyTemplate;
	public void setReadOnlyHibernateTemplate(ReadOnlyTemplate readOnlyTemplate) {
		this.readOnlyTemplate = readOnlyTemplate;
	}	

	@Override
	public List<DiscountInfo> getCurrentDiscountInfoByRelatedid(String tag, Long relatedid) {
		String query = "from DiscountInfo d where d.relatedid=? and d.tag=? and (d.validtime>=? or d.validtime=null)";
		return readOnlyTemplate.find(query, relatedid, tag, new Date());
	}

	public List<DiscountInfo> getDiscountInfoByRelatedidAndTag(Long relatedid, String tag) {
		return readOnlyTemplate.find("from DiscountInfo d where d.relatedid=? and d.tag=?", relatedid, tag);
	}

	@Override
	public List<Bulletin> getBulletinListByTag(String citycode, String tag) {
		String hql = "from Bulletin b where b.citycode=? and b.tag=? and (b.validtime>=? or b.validtime=null) order by b.relatedid desc";
		List result = readOnlyTemplate.find(hql, citycode, tag, DateUtil.getCurDate());
		return result;
	}

	@Override
	public void updateBulletinHotValue(Long id, Integer value) {
		Bulletin b = daoService.getObject(Bulletin.class, id);
		b.setHotvalue(value);
		daoService.saveObject(b);
	}

	@Override
	public List<Bulletin> getBulletinListByHotvalue(String citycode, String tag, Integer hotvalue) {
		String hql = "from Bulletin b where b.citycode=? and b.tag=? and b.hotvalue=? and (b.validtime>=? or b.validtime=null) order by b.relatedid desc";
		List result = readOnlyTemplate.find(hql, citycode, tag, hotvalue, DateUtil.getCurDate());
		return result;
	}

	@Override
	public List<Bulletin> getCurrentBulletinsByRelatedidAndHotvalue(String citycode, Long relatedid, Integer hotvalue) {
		String query = "from Bulletin n where n.citycode=? and n.relatedid=? and n.hotvalue=? and (n.validtime>=? or n.validtime=null)";
		return readOnlyTemplate.find(query, citycode, relatedid, hotvalue, DateUtil.getCurDate());
	}

	@Override
	public Integer getDiscountInfoCount(Long sportid, String tag) {
		DetachedCriteria query = DetachedCriteria.forClass(DiscountInfo.class);
		query.add(Restrictions.eq("relatedid", sportid));
		query.add(Restrictions.eq("tag", tag));
		query.add(Restrictions.or(Restrictions.ge("validtime", DateUtil.getCurDate()), Restrictions.isNull("validtime")));
		query.setProjection(Projections.rowCount());
		return readOnlyTemplate.findByCriteria(query).size() > 0 ? new Integer(readOnlyTemplate.findByCriteria(query).get(0) + "") : 0;
	}

	private Bulletin getBulletinByRelatedid(String citycode, String tag, String type, Long relatedid) {
		DetachedCriteria dc = DetachedCriteria.forClass(Bulletin.class);
		if (StringUtils.isNotBlank(citycode))
			dc.add(Restrictions.eq("citycode", citycode));
		dc.add(Restrictions.eq("tag", tag));
		dc.add(Restrictions.eq("bulletintype", type));
		dc.add(Restrictions.eq("relatedid", relatedid));
		dc.add(Restrictions.or(Restrictions.ge("validtime", DateUtil.getCurDate()), Restrictions.isNull("validtime")));
		dc.addOrder(Order.desc("posttime"));
		List<Bulletin> list = readOnlyTemplate.findByCriteria(dc, 0, 1);
		return list.isEmpty() ? null : list.get(0);
	}

	@Override
	public List<Bulletin> getBulletinListByTagAndTypeAndRelatedid(String citycode, String tag, String type, boolean isCommend, Long relatedid) {
		DetachedCriteria dc = DetachedCriteria.forClass(Bulletin.class);
		if (StringUtils.isNotBlank(citycode))
			dc.add(Restrictions.eq("citycode", citycode));
		if (isCommend) {
			dc.add(Restrictions.eq("hotvalue", Bulletin.RECOMMEND_HOTVALUE));
		}
		if (tag != null)
			dc.add(Restrictions.eq("tag", tag));
		if (type != null)
			dc.add(Restrictions.eq("bulletintype", type));
		if (relatedid != null)
			dc.add(Restrictions.eq("relatedid", relatedid));
		dc.add(Restrictions.or(Restrictions.ge("validtime", DateUtil.getCurDate()), Restrictions.isNull("validtime")));
		dc.addOrder(Order.desc("posttime"));
		List<Bulletin> list = readOnlyTemplate.findByCriteria(dc);
		return list;
	}

	@Override
	public List<Correction> getCorrectionList(String status,Timestamp starttime,Timestamp endtime,int from,int maxnum) {
		DetachedCriteria query = DetachedCriteria.forClass(Correction.class);
		if("treated".equals(status)){
			query.add(Restrictions.eq("check", "1"));
		}else{
			query.add(Restrictions.eq("check", "0"));
		}
		if(starttime !=null && endtime !=null){
		    query.add(Restrictions.between("addtime", starttime, endtime));
		}
		query.addOrder(Order.desc("addtime"));
		List<Correction> list = readOnlyTemplate.findByCriteria(query, from, maxnum);
		return list;
	}

	@Override
	public Integer getCorrectionCount(String status,Timestamp starttime,Timestamp endtime) {
		DetachedCriteria query = DetachedCriteria.forClass(Correction.class);
		if("treated".equals(status)){
			query.add(Restrictions.eq("check", "1"));
		}else{
			query.add(Restrictions.eq("check", "0"));
		}
		if(starttime !=null && endtime !=null){
		    query.add(Restrictions.between("addtime", starttime, endtime));
		}
		query.setProjection(Projections.rowCount());
		List<Long> list = readOnlyTemplate.findByCriteria(query);
		if (list.isEmpty())
			return 0;
		return Integer.parseInt("" + list.get(0));
	}

	@Override
	public List<TempMovie> getTempMovieList(String tag,String status,Timestamp starttime,Timestamp endtime,String type,Integer point,int from,int maxnum){
		DetachedCriteria query = DetachedCriteria.forClass(TempMovie.class);
		if("treated".equals(tag)){
			query.add(Restrictions.eq("status", status));
		}else{
			query.add(Restrictions.ne("status", status));
		}
		if(starttime !=null && endtime !=null){
		    query.add(Restrictions.between("addtime", starttime, endtime));
		}
		if(StringUtils.isNotBlank(type)){
			query.add(Restrictions.like("type",type,MatchMode.ANYWHERE));
		}
		if(point !=null){
			query.add(Restrictions.eq("point", point));
		}
		query.addOrder(Order.desc("addtime"));
		List<TempMovie> list = readOnlyTemplate.findByCriteria(query, from, maxnum);
		return list;
	}
	@Override
	public Integer getTempMovieCount(String tag,String status,Timestamp starttime,Timestamp endtime){
		DetachedCriteria query = DetachedCriteria.forClass(TempMovie.class);
		if("treated".equals(tag)){
			query.add(Restrictions.eq("status", status));
		}else{
			query.add(Restrictions.ne("status", status));
		}
		if(starttime !=null && endtime !=null){
		    query.add(Restrictions.between("addtime", starttime, endtime));
		}
		query.setProjection(Projections.rowCount());
		List list = readOnlyTemplate.findByCriteria(query);
		if (list.isEmpty())
			return 0;
		return Integer.parseInt(list.get(0) + "");
	}
	@Override
	public Integer getPlaceCount(String status,Timestamp starttime,Timestamp endtime) {
		DetachedCriteria query = DetachedCriteria.forClass(Place.class);
		if("treated".equals(status)){
			query.add(Restrictions.eq("ispass", "1"));
		}else{
			query.add(Restrictions.eq("ispass", "0"));
		}
		if(starttime !=null && endtime !=null){
		    query.add(Restrictions.between("addtime", starttime, endtime));
		}
		query.add(Restrictions.eq("isdel", "0"));
		query.setProjection(Projections.rowCount());
		List list = readOnlyTemplate.findByCriteria(query);
		if (list.isEmpty())
			return 0;
		return Integer.parseInt(list.get(0) + "");
	}
	@Override
	public List<Place> getPlaceList(String status,Timestamp starttime,Timestamp endtime,int from, int maxnum) {
		DetachedCriteria query = DetachedCriteria.forClass(Place.class);
		if("treated".equals(status)){
			query.add(Restrictions.eq("ispass", "1"));
		}else{
			query.add(Restrictions.eq("ispass", "0"));
		}
		if(starttime !=null && endtime !=null){
		    query.add(Restrictions.between("addtime", starttime, endtime));
		}
		query.add(Restrictions.eq("isdel", "0"));
		query.addOrder(Order.desc("addtime"));
		List<Place> list = readOnlyTemplate.findByCriteria(query, from, maxnum);
		return list;
	}

	@Override
	public void updateCoupon(String citycode, String tag, Long relatedid) {
		BaseInfo baseInfo = (BaseInfo) relateService.getRelatedObject(tag, relatedid);
		Bulletin bulletin = getBulletinByRelatedid(citycode, tag, Bulletin.BULLETION_COUPON, relatedid);
		String oldCoupon = baseInfo.getCoupon();
		if (bulletin != null){
			baseInfo.setCoupon(bulletin.getBulletintitle());
		}else{
			baseInfo.setCoupon("N");
		}
		if(!StringUtils.equals(oldCoupon, baseInfo.getCoupon())){
			daoService.saveObject(baseInfo);
		}
	}


	@Override
	public List<Link> getLinkListByType(String type) {
		DetachedCriteria query = DetachedCriteria.forClass(Link.class);
		query.add(Restrictions.eq("type", type));
		query.addOrder(Order.desc("updatetime"));
		return readOnlyTemplate.findByCriteria(query);
	}
	@Override
	public List<GewaCommend> getGewaCommendList(String citycode, String signname, Long parentid, boolean isGtZero, boolean isdesc, int from, int maxnum) {
		return getGewaCommendList(citycode, signname, parentid, isGtZero, isdesc, false, from, maxnum);
	}

	public List<GewaCommend> getGewaCommendList(String citycode, String signname, Long parentid, boolean isGtZero, boolean isdesc, boolean isActivity,
			int from, int maxnum) {
		DetachedCriteria query = DetachedCriteria.forClass(GewaCommend.class);
		if (StringUtils.isNotBlank(citycode))
			query.add(Restrictions.eq("citycode", citycode));
		query.add(Restrictions.eq("signname", signname));
		if (parentid != null)
			query.add(Restrictions.eq("parentid", parentid));
		if (isActivity)
			query.add(Restrictions.ge("stoptime", DateUtil.getMillTimestamp()));
		if (isGtZero)
			query.add(Restrictions.gt("ordernum", 0));
		if (isdesc) {
			query.addOrder(Order.desc("ordernum"));
		} else {
			query.addOrder(Order.asc("ordernum"));
		}

		return readOnlyTemplate.findByCriteria(query, from, maxnum);
	}
	@Override
	public List<GewaCommend> getGewaCommendList(String citycode, List<String> signNameList, Long parentid, boolean isGtZero, boolean isdesc, boolean isActivity,
			int from, int maxnum) {
		DetachedCriteria query = DetachedCriteria.forClass(GewaCommend.class);
		if (StringUtils.isNotBlank(citycode))
			query.add(Restrictions.eq("citycode", citycode));
		if(signNameList != null && !signNameList.isEmpty()){
			if(signNameList.size() == 1){
				query.add(Restrictions.eq("signname", signNameList.get(0)));
			}else query.add(Restrictions.in("signname", signNameList));
		}
		if (parentid != null)
			query.add(Restrictions.eq("parentid", parentid));
		if (isActivity)
			query.add(Restrictions.ge("stoptime", DateUtil.getMillTimestamp()));
		if (isGtZero)
			query.add(Restrictions.gt("ordernum", 0));
		if (isdesc) {
			query.addOrder(Order.desc("addtime"));
		} else {
			query.addOrder(Order.asc("addtime"));
		}

		return readOnlyTemplate.findByCriteria(query, from, maxnum);
	}
	
	@Override
	public Integer getGewaCommendCount(String citycode, String signname, Long parentid, String tag, boolean isGtZero) {
		DetachedCriteria query = DetachedCriteria.forClass(GewaCommend.class);
		if (StringUtils.isNotBlank(citycode))
			query.add(Restrictions.eq("citycode", citycode));
		if(StringUtils.isNotBlank(signname))
			query.add(Restrictions.eq("signname", signname));
		if (parentid != null)
			query.add(Restrictions.eq("parentid", parentid));
		if (StringUtils.isNotBlank(tag))
			query.add(Restrictions.eq("tag", tag));
		if (isGtZero)
			query.add(Restrictions.gt("ordernum", 0));
		query.setProjection(Projections.rowCount());
		List list = readOnlyTemplate.findByCriteria(query);
		if (list.isEmpty())
			return 0;
		return Integer.parseInt("" + list.get(0));
	}
	@Override
	public Integer getGewaCommendCount(String citycode, List<String> signNameList, Long parentid, String tag, boolean isGtZero) {
		DetachedCriteria query = DetachedCriteria.forClass(GewaCommend.class);
		if (StringUtils.isNotBlank(citycode))
			query.add(Restrictions.eq("citycode", citycode));
		if(signNameList != null && !signNameList.isEmpty()){
			if(signNameList.size() == 1){
				query.add(Restrictions.eq("signname", signNameList.get(0)));
			}else query.add(Restrictions.in("signname", signNameList));
		}
		if (parentid != null)
			query.add(Restrictions.eq("parentid", parentid));
		if (StringUtils.isNotBlank(tag))
			query.add(Restrictions.eq("tag", tag));
		if (isGtZero)
			query.add(Restrictions.gt("ordernum", 0));
		query.setProjection(Projections.rowCount());
		List list = readOnlyTemplate.findByCriteria(query);
		if (list.isEmpty())
			return 0;
		return Integer.parseInt("" + list.get(0));
	}

	@Override
	public List<GewaCommend> getGewaCommendList(String citycode, String signname, Long parentid, String tag, boolean isGtZero, int first, int maxnum) {
		return getGewaCommendList(citycode, null, signname, parentid, tag, isGtZero, false, first, maxnum);
	}
	
	@Override
	public List<GewaCommend> getGewaCommendList(String citycode, String signname, Long parentid, String tag, boolean isGtZero, String order, boolean asc, int from, int maxnum){
		return getGewaCommendList(citycode, null, signname, parentid, tag, isGtZero, false, order, asc, from, maxnum);
	}
	

	private List<GewaCommend> getGewaCommendList(String citycode, String countycode, String signname, Long parentid, String tag, boolean isGtZero,
			boolean isActivity, String order, boolean asc, int from, int maxnum) {
		DetachedCriteria query = DetachedCriteria.forClass(GewaCommend.class);
		if (StringUtils.isNotBlank(citycode))
			query.add(Restrictions.or(Restrictions.eq("citycode", citycode), Restrictions.eq("citycode", AdminCityContant.CITYCODE_ALL)));
		if (StringUtils.isNotBlank(countycode))
			query.add(Restrictions.eq("countycode", countycode));
		if (StringUtils.isNotBlank(signname))
			query.add(Restrictions.eq("signname", signname));
		if (parentid != null)
			query.add(Restrictions.eq("parentid", parentid));
		if (StringUtils.isNotBlank(tag))
			query.add(Restrictions.eq("tag", tag));
		if (isGtZero)
			query.add(Restrictions.gt("ordernum", 0));
		if (isActivity)
			query.add(Restrictions.or(Restrictions.isNull("stoptime"), Restrictions.ge("stoptime", DateUtil.getMillTimestamp())));
		if(StringUtils.isNotBlank(order) && ClassUtils.hasMethod(GewaCommend.class, "get" + StringUtils.capitalize(order))){
			if(asc){
				query.addOrder(Order.asc(order));
			}else{
				query.addOrder(Order.desc(order));
			}
		}else{
			if(asc){
				query.addOrder(Order.asc("ordernum"));
			}else{
				query.addOrder(Order.desc("ordernum"));
			}
		}
		query.addOrder(Order.asc("id"));
		return readOnlyTemplate.findByCriteria(query, from, maxnum);
	}
	
	@Override
	public List<GewaCommend> getGewaCommendList(String citycode, String countycode, String signname, Long parentid, String tag, boolean isGtZero,
			boolean isActivity, int first, int maxnum) {
		return getGewaCommendList(citycode, countycode, signname, parentid, tag, isGtZero, isActivity, null, true, first, maxnum);
	}
	
	@Override
	public List<GewaCommend> getGewaCommendList(String citycode, String countycode, String signname, Long parentid, String tag, boolean isGtZero,
			boolean isActivity,boolean isStarttime, int first, int maxnum) {
		DetachedCriteria query = DetachedCriteria.forClass(GewaCommend.class);
		if (StringUtils.isNotBlank(citycode))
			query.add(Restrictions.or(Restrictions.eq("citycode", citycode), Restrictions.eq("citycode", AdminCityContant.CITYCODE_ALL)));
		if (StringUtils.isNotBlank(countycode))
			query.add(Restrictions.eq("countycode", countycode));
		if (StringUtils.isNotBlank(signname))
			query.add(Restrictions.eq("signname", signname));
		if (parentid != null)
			query.add(Restrictions.eq("parentid", parentid));
		if (StringUtils.isNotBlank(tag))
			query.add(Restrictions.eq("tag", tag));
		if (isGtZero)
			query.add(Restrictions.gt("ordernum", 0));
		if(isStarttime){
			query.add(Restrictions.le("starttime", DateUtil.getMillTimestamp()));
			query.addOrder(Order.asc("starttime"));
		}else{
			query.addOrder(Order.asc("ordernum"));
		}
		if (isActivity)
			query.add(Restrictions.or(Restrictions.isNull("stoptime"), Restrictions.ge("stoptime", DateUtil.getMillTimestamp())));
		query.addOrder(Order.asc("id"));
		return readOnlyTemplate.findByCriteria(query, first, maxnum);
	}
	

	@Override
	public List<GewaCommend> getGewaCommendListByRelatedid(String citycode, String signname, Long relatedid, String tag, boolean isGtZero, int first,
			int maxnum) {
		DetachedCriteria query = DetachedCriteria.forClass(GewaCommend.class);
		if (StringUtils.isNotBlank(citycode))
			query.add(Restrictions.eq("citycode", citycode));
		if (StringUtils.isNotBlank(signname))
			query.add(Restrictions.eq("signname", signname));
		if (relatedid != null)
			query.add(Restrictions.eq("relatedid", relatedid));
		if (StringUtils.isNotBlank(tag))
			query.add(Restrictions.eq("tag", tag));
		if (isGtZero)
			query.add(Restrictions.gt("ordernum", 0));
		query.addOrder(Order.asc("ordernum"));
		return readOnlyTemplate.findByCriteria(query, first, maxnum);
	}
	@Override
	public GewaCommend getGewaCommendByRelatedid(String signname, Long relatedid) {
		DetachedCriteria query = DetachedCriteria.forClass(GewaCommend.class);
		query.add(Restrictions.eq("signname", signname));
		query.add(Restrictions.eq("relatedid", relatedid));
		query.add(Restrictions.gt("ordernum", 0));
		query.addOrder(Order.asc("ordernum"));
		List<GewaCommend> gcList = readOnlyTemplate.findByCriteria(query, 0, 1);
		if (gcList.isEmpty())
			return null;
		return gcList.get(0);
	}

	@Override
	public List<GewaCommend> getGewaCommendListByParentid(String signname, Long parentid, boolean isAll) {
		DetachedCriteria query = DetachedCriteria.forClass(GewaCommend.class);
		query.add(Restrictions.eq("signname", signname));
		query.add(Restrictions.eq("parentid", parentid));
		if (!isAll)
			query.add(Restrictions.gt("ordernum", 0));
		query.addOrder(Order.asc("ordernum"));
		List<GewaCommend> gcList = readOnlyTemplate.findByCriteria(query);
		return gcList;
	}

	@Override
	public void initGewaCommendList(String group, RelatedHelper rh, List<GewaCommend> gcList) {
		if (gcList == null || gcList.isEmpty())
			return;
		Object relate = null;
		Object relate2 = null;
		String tag = "";
		List<Long> activityIdList = new LinkedList<Long>();
		Map<Long,GewaCommend> map = new HashMap<Long,GewaCommend>();
		for (GewaCommend gc : gcList) {
			if (gc.getRelatedid() != null && StringUtils.isNotBlank(gc.getTag())) {
				if("activity".equals(gc.getTag())){
					if(!activityIdList.contains(gc.getRelatedid())){
						activityIdList.add(gc.getRelatedid());
						map.put(gc.getRelatedid(), gc);
					}
					continue;
				}
				if (gc.getTag().startsWith("news")) tag = "news";
				else if (gc.getTag().startsWith("diary")) tag = "diary";
				else if (gc.getTag().startsWith("movie")) tag = "movie";
				else tag = gc.getTag();
				relate = relateService.getRelatedObject(tag, gc.getRelatedid());
				if (StringUtils.equals(tag, TagConstant.TAG_DIARY) && relate == null) {
					relate = diaryService.getDiaryBase(gc.getRelatedid());
				}
				rh.addRelated1(group, gc.getId(), relate);
				if ("diary_commu".equals(gc.getTag())) {
					relate2 = daoService.getObject(Commu.class, gc.getParentid());
					gc.setTitle(((Commu) relate2).getName());
				}else if ("diary_drama".equals(gc.getTag())) {
					relate2 = daoService.getObject(Drama.class, gc.getParentid());
				}
				if(relate2!=null){
					rh.addRelated2(group, gc.getId(), relate2);
				}
			}
		}
		if(!activityIdList.isEmpty()){
			this.initActivity(group, rh, activityIdList, map);
		}
	}
	/**
	 * 活动由于远程调去，避免http400时 多次循环调用
	 * @param group
	 * @param rh
	 * @param activityIdList
	 * @param map
	 */
	private void initActivity(String group, RelatedHelper rh,List<Long> activityIdList,Map<Long,GewaCommend> map){
		Object relate = null;
		Object relate2 = null;
		ErrorCode<List<RemoteActivity>> resultCode = synchActivityService.getRemoteActivityListByIds(activityIdList);
		if(resultCode.isSuccess()){
			List<RemoteActivity> raList = resultCode.getRetval();
			if(!VmUtils.isEmptyList(raList)){
				for(RemoteActivity activity : raList){
					GewaCommend gc = map.get(activity.getId());
					if(gc == null){
						continue;
					}
					rh.addRelated1(group, gc.getId(), activity);
					if (("index_gymsport_item".equals(gc.getSignname()) || SignName.SPORTINDEX_SPORTACTIVITY.equals(gc.getSignname()))) {
						if (relate != null) {
							relate2 = daoService.getObject(RelateClassHelper.getRelateClazz((String) BeanUtil.get(relate, "category")), (Long) BeanUtil.get(relate,
									"categoryid"));
							if(relate2!=null){
								rh.addRelated2(group, gc.getId(), relate2);
							}
						}
					}
				}
			}
		}
	}
	
	@Override
	public List<HeadInfo> getHeadInfoList(String board, String citycode, int from, int maxNum) {
		DetachedCriteria query = DetachedCriteria.forClass(HeadInfo.class, "hi");
		query.addOrder(Order.desc("hi.addtime"));
		query.add(Restrictions.eq("hi.board", board));
		query.add(Restrictions.eq("hi.citycode", citycode));
		query.add(Restrictions.and(Restrictions.ne("hi.id", 1l), Restrictions.ne("hi.id", 2l)));
		List<HeadInfo> headInfoList = readOnlyTemplate.findByCriteria(query, from, maxNum);
		return headInfoList;
	}

	@Override
	public Integer getHeadInfoCount(String board) {
		DetachedCriteria query = DetachedCriteria.forClass(HeadInfo.class, "hi");
		query.setProjection(Projections.rowCount());
		query.add(Restrictions.eq("hi.board", board));
		query.add(Restrictions.and(Restrictions.ne("hi.id", 1l), Restrictions.ne("hi.id", 2l)));
		List list = readOnlyTemplate.findByCriteria(query);
		if (list.isEmpty())
			return 0;
		return Integer.parseInt("" + list.get(0));
	}

	@Override
	public List<GewaCommend> getGewaCommendList(Long parentid, String signname, List tag, boolean isGtZero, int first, int maxnum) {
		DetachedCriteria query = DetachedCriteria.forClass(GewaCommend.class);
		query.add(Restrictions.eq("signname", signname));
		if (parentid != null)
			query.add(Restrictions.eq("parentid", parentid));
		if (tag != null && !tag.isEmpty())
			query.add(Restrictions.in("tag", tag));
		if (isGtZero)
			query.add(Restrictions.gt("ordernum", 0));
		query.addOrder(Order.asc("ordernum"));
		return readOnlyTemplate.findByCriteria(query, first, maxnum);
	}
	@Override
	public Integer getCommuTopicCount(Long commuid) {
		DetachedCriteria query = DetachedCriteria.forClass(CommuTopic.class);
		query.add(Restrictions.eq("commuid", commuid));
		query.setProjection(Projections.rowCount());
		List list = readOnlyTemplate.findByCriteria(query);
		if (list.isEmpty())
			return 0;
		return Integer.parseInt("" + list.get(0));
	}

	@Override
	public List<CommuTopic> getCommuTopicList(Long commuid, int from, int maxnum) {
		DetachedCriteria query = DetachedCriteria.forClass(CommuTopic.class);
		query.add(Restrictions.eq("commuid", commuid));
		query.addOrder(Order.asc("ordernum"));
		query.addOrder(Order.asc("id"));
		List<CommuTopic> commuTopicList = readOnlyTemplate.findByCriteria(query, from, maxnum);
		return commuTopicList;
	}
	@Override
	public List<GewaCommend> getGewaCommendListByid(Long relatedid, String tag, String signname, boolean isGtZero) {
		DetachedCriteria query = DetachedCriteria.forClass(GewaCommend.class);
		query.add(Restrictions.eq("signname", signname));
		if (relatedid != null)
			query.add(Restrictions.eq("relatedid", relatedid));
		if (StringUtils.isNotBlank(tag))
			query.add(Restrictions.eq("tag", tag));
		if (isGtZero)
			query.add(Restrictions.gt("ordernum", 0));
		query.addOrder(Order.asc("ordernum"));
		List<GewaCommend> gewaCommondList = readOnlyTemplate.findByCriteria(query);
		return gewaCommondList;
	}

	/********************************************************************************************************************
	 * 电影版块 - 排序查询
	 */

	/********************************************************************************************************************
	 * 
	 * 新闻 / 活动 等内容, 处理对应的关联城市名称.
	 */
	@Override
	public <T extends BaseObject> Map<Long, String> initRelateCityName(List<T> list) {
		Map<Long, String> dataMap = new HashMap<Long, String>();
		for (T object : list) {
			dataMap.put((Long) BeanUtil.get(object, "id"), revertCityname((String) BeanUtil.get(object, "citycode"), "，"));
		}
		return dataMap;
	}

	@Override
	public <T extends BaseObject> Map<Long, List<Map>> initRelateToCityName(List<T> list, String tag) {
		Map<Long, List<Map>> dataMap = new HashMap<Long, List<Map>>();
		for (T object : list) {
			List<Map> mapList = new ArrayList<Map>();
			String division = (String) BeanUtil.get(object, "division");
			Long id = (Long) BeanUtil.get(object, "id");
			String flag = (String) BeanUtil.get(object, "flag");
			Map<String, String> map = new HashMap<String, String>();
			map.put("flag", flag);
			if (StringUtils.equals(division, DiaryConstant.DIVISION_A)) {
				map.put("citycode", AdminCityContant.CITYCODE_ALL);
				mapList.add(map);
				dataMap.put(id, mapList);
			} else if (StringUtils.equals(division, DiaryConstant.DIVISION_Y)) {
				map.put("citycode", (String) BeanUtil.get(object, "citycode"));
				mapList.add(map);
				dataMap.put(id, mapList);
			} else {
				List<RelateToCity> reList = getRelateToCity(tag, id, null, null);
				List<Map> idList = BeanUtil.getBeanMapList(reList, "citycode", "flag");
				if (idList.isEmpty()) {
					map.put("citycode", (String) BeanUtil.get(object, "citycode"));
					mapList.add(map);
					dataMap.put(id, mapList);
				} else
					dataMap.put(id, idList);
			}
		}
		return dataMap;
	}

	private String revertCityname(String citycode, String joinChar) {
		if (StringUtils.isBlank(citycode))
			citycode = AdminCityContant.CITYCODE_SH;
		String[] arrs = StringUtils.split(citycode, ",");
		List<String> arrss = new ArrayList<String>();
		for (String s : arrs) {
			String cityName = AdminCityContant.allcityMap.get(s);
			if (StringUtils.isBlank(cityName)) {
				cityName = "全国";
			}
			arrss.add(cityName);
		}
		return StringUtils.join(arrss, joinChar);
	}

	@Override
	public List<GewaCommend> getCommendListByRelatedid(Long relatedid, String signname, String tag) {
		DetachedCriteria query = DetachedCriteria.forClass(GewaCommend.class);
		if(relatedid != null)query.add(Restrictions.eq("relatedid", relatedid));
		query.add(Restrictions.eq("signname", signname));
		if(StringUtils.isNotBlank(tag))query.add(Restrictions.eq("tag", tag));
		return readOnlyTemplate.findByCriteria(query);
	}
	@Override
	public Relationship getRelationship(String category, String tag, Long relatedid2, Timestamp validtime) {
		DetachedCriteria query = DetachedCriteria.forClass(Relationship.class);
		query.add(Restrictions.eq("category", category));
		query.add(Restrictions.eq("tag", tag));
		query.add(Restrictions.eq("relatedid2", relatedid2));
		if (validtime != null)
			query.add(Restrictions.ge("validtime", validtime));
		List<Relationship> reList = readOnlyTemplate.findByCriteria(query, 0, 1);
		if (!reList.isEmpty())
			return reList.get(0);
		return null;
	}

	@Override
	public Integer getRelationshipCount(String category, Long relatedid1, String tag, Long relatedid2, Timestamp validtime) {
		DetachedCriteria query = DetachedCriteria.forClass(Relationship.class);
		query.setProjection(Projections.rowCount());
		if (StringUtils.isNotBlank(category))
			query.add(Restrictions.eq("category", category));
		if (relatedid1 != null)
			query.add(Restrictions.eq("relatedid1", relatedid1));
		if (StringUtils.isNotBlank(tag))
			query.add(Restrictions.eq("tag", tag));
		if (relatedid2 != null)
			query.add(Restrictions.eq("relatedid2", relatedid2));
		if (validtime != null)
			query.add(Restrictions.le("validtime", validtime));
		List<Long> reList = readOnlyTemplate.findByCriteria(query);
		if (!reList.isEmpty())
			return Integer.valueOf(reList.get(0) + "");
		return 0;
	}

	@Override
	public List<Relationship> getRelationshipList(String category, Long relatedid1, String tag, Long relatedid2, Timestamp validtime, int from,
			int maxnum) {
		DetachedCriteria query = DetachedCriteria.forClass(Relationship.class);
		if (StringUtils.isNotBlank(category))
			query.add(Restrictions.eq("category", category));
		if (relatedid1 != null)
			query.add(Restrictions.eq("relatedid1", relatedid1));
		if (StringUtils.isNotBlank(tag))
			query.add(Restrictions.eq("tag", tag));
		if (relatedid2 != null)
			query.add(Restrictions.eq("relatedid2", relatedid2));
		if (validtime != null)
			query.add(Restrictions.le("validtime", validtime));
		query.addOrder(Order.desc("addtime"));
		List<Relationship> reList = readOnlyTemplate.findByCriteria(query, from, maxnum);
		return reList;
	}

	@Override
	public List<GrabTicketSubject> getGrabTicketSubjectList(String citycode, String tag, int from, int maxnum) {
		DetachedCriteria query = DetachedCriteria.forClass(GrabTicketSubject.class);
		if (StringUtils.isNotBlank(citycode))
			query.add(Restrictions.eq("citycode", citycode));
		if (StringUtils.isNotBlank(tag))
			query.add(Restrictions.eq("tag", tag));
		query.addOrder(Order.desc("addtime"));
		List<GrabTicketSubject> subjectList = readOnlyTemplate.findByCriteria(query, from, maxnum);
		return subjectList;
	}

	@Override
	public List<RelateToCity> getRelateToCity(String tag, Long relatedid, String citycode, String flag) {
		DetachedCriteria query = DetachedCriteria.forClass(RelateToCity.class);
		query.add(Restrictions.eq("tag", tag));
		if (StringUtils.isNotBlank(citycode))
			query.add(Restrictions.eq("citycode", citycode));
		query.add(Restrictions.eq("relatedid", relatedid));
		if (StringUtils.isNotBlank(flag))
			query.add(Restrictions.like("flag", flag, MatchMode.ANYWHERE));
		query.addOrder(Order.desc("addtime"));
		List<RelateToCity> reList = readOnlyTemplate.findByCriteria(query);
		return reList;
	}

	@Override
	public Map<String, Integer> getActivityCount() {
		Map<String, Integer> result = (Map<String, Integer>) cacheService.get(CacheConstant.REGION_HALFDAY, CacheConstant.KEY_ACTIVITY_COUNT);
		if(result == null){
			result = new HashMap<String, Integer>();
		}
		return result;
	}

	@Override
	public Map<String, Integer> getNewsCount() {
		Map<String, Integer> result = (Map<String, Integer>) cacheService.get(CacheConstant.REGION_HALFDAY, CacheConstant.KEY_NEWS_COUNT);
		if(result == null){
			result = new HashMap<String, Integer>();
		}
		return result;
	}
	

	
	@Override
	public Map<String, Integer> getPictureCount() {
		Map<String, Integer> result = (Map<String, Integer>) cacheService.get(CacheConstant.REGION_HALFDAY, CacheConstant.KEY_PICTURE_COUNT);
		if(result == null){
			result = new HashMap<String, Integer>();
		}
		return result;
	}

	@Override
	public Map<String, Integer> getVideoCount() {
		Map<String, Integer> result = (Map<String, Integer>) cacheService.get(CacheConstant.REGION_HALFDAY, CacheConstant.KEY_VIDEO_COUNT);
		if(result == null){
			result = new HashMap<String, Integer>();
		}
		return result;
	}

	@Override
	public Map<String, Integer> getCommentCount() {
		Map<String, Integer> result = (Map<String, Integer>) cacheService.get(CacheConstant.REGION_HALFDAY, CacheConstant.KEY_COMMENT_COUNT);
		if(result == null){
			result = new HashMap<String, Integer>();
		}
		return result;
	}

	@Override
	public Map<String, Integer> getDiaryCount() {
		Map<String, Integer> result = (Map<String, Integer>) cacheService.get(CacheConstant.REGION_HALFDAY, CacheConstant.KEY_DIARY_COUNT);
		if(result == null){
			result = new HashMap<String, Integer>();
		}
		return result;
	}

	@Override
	public Map<String, Integer> getCommuCount() {
		Map<String, Integer> result = (Map<String, Integer>) cacheService.get(CacheConstant.REGION_HALFDAY, CacheConstant.KEY_COMMU_COUNT);
		if(result == null){
			result = new HashMap<String, Integer>();
		}
		return result;
	}

	@Override
	public Map<String, Integer> getSportItemSportCount() {
		Map<String, Integer> result = (Map<String, Integer>) cacheService.get(CacheConstant.REGION_HALFDAY,
				CacheConstant.KEY_SPORTITEM_ITEMID_SPORTCOUNT);
		if(result == null){
			result = new HashMap<String, Integer>();
		}
		return result;
	}

	@Override
	public List<PhoneAdvertisement> getNewPhoneAdvertisementList(String status) {
		DetachedCriteria query = DetachedCriteria.forClass(PhoneAdvertisement.class);
		query.add(Restrictions.eq("status", status));
		query.addOrder(Order.desc("addtime"));
		List<PhoneAdvertisement> phoneadvertisementList = readOnlyTemplate.findByCriteria(query);
		return phoneadvertisementList;
	}

	@Override
	public Map getCurIndexDataSheet() {
		Map dataMap = (Map) cacheService.get(CacheConstant.REGION_TENMIN, MongoData.INDEX_KEY);
		if(dataMap == null){
			dataMap = mongoService.getMap(MongoData.DEFAULT_ID_NAME, MongoData.NS_INDEX_DATASHEET, MongoData.INDEX_KEY);
			cacheService.set(CacheConstant.REGION_TENMIN, MongoData.INDEX_KEY, dataMap);
		}
		return dataMap;
	}
	
	@Override
	public Integer getDataDictionaryCount(String objectName){
		DetachedCriteria query = DetachedCriteria.forClass(DataDictionary.class);
		if(StringUtils.isNotBlank(objectName)){
			query.add(Restrictions.eq("objectName", objectName));
		}
		query.setProjection(Projections.rowCount());
		List result = readOnlyTemplate.findByCriteria(query);
		if(result.isEmpty()) return 0;
		return Integer.valueOf(result.get(0)+"");
	}
	@Override
	public List<DataDictionary> getDataDictionaryList(String objectName, int from, int maxnum){
		DetachedCriteria query = DetachedCriteria.forClass(DataDictionary.class);
		if(StringUtils.isNotBlank(objectName)){
			query.add(Restrictions.eq("objectName", objectName));
		}
		query.addOrder(Order.asc("id"));
		List<DataDictionary> dataList = readOnlyTemplate.findByCriteria(query, from, maxnum);
		return dataList;
	}

	@Override
	public <T extends BaseInfo> List<T> getBaiDuNearPlaceObjectList(Class<T> clazz, String citycode, String countycode, String bpointx, String bpointy, double spaceRound) {
		if(StringUtils.isBlank(bpointx)|| StringUtils.isBlank(bpointy) || spaceRound <= 0) return new ArrayList<T>();
		double pointx = Double.parseDouble(bpointx);
		double pointy = Double.parseDouble(bpointy);
		double maxLd =  LongitudeAndLatitude.getLongitude(pointx, pointy, spaceRound);
		double minLd =  LongitudeAndLatitude.getLongitude(pointx, pointy, -spaceRound);
		if(maxLd <minLd){
			double temp = maxLd;
			maxLd = minLd;
			minLd = temp;
		}
		double maxLa =  LongitudeAndLatitude.getLatitude(pointy, spaceRound);
		double minLa =  LongitudeAndLatitude.getLatitude(pointy, -spaceRound);
		if(maxLa < minLa){
			double temp = maxLa;
			maxLa = minLa;
			minLa = temp;
		}
		DetachedCriteria query = DetachedCriteria.forClass(clazz);
		query.add(Restrictions.eq("citycode", citycode));
		if(StringUtils.isNotBlank(countycode)) query.add(Restrictions.eq("countycode", countycode));
		query.add(Restrictions.ge("bpointx", ""+minLd));
		query.add(Restrictions.le("bpointx", ""+maxLd));
		query.add(Restrictions.ge("bpointy", ""+minLa));
		query.add(Restrictions.le("bpointy", ""+maxLa));
		List<T> resultList = readOnlyTemplate.findByCriteria(query);
		OuterSorter sorter = new OuterSorter<T>(false);
		for(T baseInfo : resultList){
			String xpoint = (String) BeanUtil.get(baseInfo, "bpointx");
			String ypoint = (String) BeanUtil.get(baseInfo, "bpointy");
			long value = Math.round(LongitudeAndLatitude.getDistance(pointx, pointy, Double.parseDouble(xpoint), Double.parseDouble(ypoint)));
			sorter.addBean(value, baseInfo);
		}
		resultList = sorter.getAscResult();
		return resultList;
	}
}
