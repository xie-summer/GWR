package com.gewara.service.api.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Service;

import com.gewara.constant.AdminCityContant;
import com.gewara.constant.content.SignName;
import com.gewara.constant.order.AddressConstant;
import com.gewara.model.api.ApiUser;
import com.gewara.model.api.ApiUserExtra;
import com.gewara.model.content.GewaCommend;
import com.gewara.model.content.PhoneAdvertisement;
import com.gewara.model.mobile.ApiConfig;
import com.gewara.service.api.ApiMobileService;
import com.gewara.service.impl.BaseServiceImpl;
import com.gewara.util.BeanUtil;
import com.gewara.util.DateUtil;

@Service("apiMobileService")
public class ApiMobileServiceImpl extends BaseServiceImpl implements ApiMobileService,InitializingBean {
	private List<Long> mobilePartnerList = null;
	@Override
	public GewaCommend getPhoneIndexAdvertInfo(String citycode, String tag) {
		DetachedCriteria query = DetachedCriteria.forClass(GewaCommend.class);
		query.add(Restrictions.eq("signname", SignName.PHONE_INDEX_ADVERT));
		query.add(Restrictions.or(Restrictions.eq("tag", AddressConstant.ADDRESS_ALL), Restrictions.eq("tag", tag)));
		query.add(Restrictions.gt("ordernum", 0));
		query.add(Restrictions.or(Restrictions.eq("citycode", AdminCityContant.CITYCODE_ALL), Restrictions.like("citycode", citycode, MatchMode.ANYWHERE)));
		List<GewaCommend> list = hibernateTemplate.findByCriteria(query, 0, 1);
		if (list.isEmpty())
			return null;
		return list.get(0);
	}
	@Override
	public List<PhoneAdvertisement> getPhoneAdvertList(String apptype, String osType,String citycode,String advtype,int from, int maxnum) {
		DetachedCriteria query = DetachedCriteria.forClass(PhoneAdvertisement.class);
		if (StringUtils.isNotBlank(apptype))
			query.add(Restrictions.eq("apptype", apptype));
		if (StringUtils.isNotBlank(osType)) {
			query.add(Restrictions.or(Restrictions.eq("osType", osType), Restrictions.eq("osType", PhoneAdvertisement.OS_TYPE_ALL)));
		}
		if (StringUtils.isNotBlank(citycode)) {
			query.add(Restrictions.or(Restrictions.eq("citycode", AdminCityContant.CITYCODE_ALL), Restrictions.like("citycode", citycode, MatchMode.ANYWHERE)));
		}
		if(StringUtils.isNotBlank(advtype)){
			query.add(Restrictions.eq("advertType", advtype));
		}
		query.add(Restrictions.le("starttime", DateUtil.getCurFullTimestamp()));
		query.add(Restrictions.ge("endtime", DateUtil.getCurFullTimestamp()));
		query.add(Restrictions.eq("isshow", PhoneAdvertisement.IS_SHOW_Y));
		query.add(Restrictions.ne("status", PhoneAdvertisement.STATUS_DELETE));
		query.addOrder(Order.asc("rank"));
		List<PhoneAdvertisement> list = hibernateTemplate.findByCriteria(query, from, maxnum);
		return list;
	}
	
	/*@Override
	public List<Long> getGewaParnteridList(){
		return mobilePartnerList;
	}*/
	@Override
	public void afterPropertiesSet() throws Exception {
		initApiUserList();
	}
	private Map<String, ApiUser> partnerMap = new HashMap<String, ApiUser>();
	private Map<Long, ApiUserExtra> partnerExtraMap = new HashMap<Long, ApiUserExtra>();
	@Override
	public ApiUser getApiUserByAppkey(String appkey){
		return partnerMap.get(appkey);
	}
	@Override
	public ApiUserExtra getApiUserExtraById(Long id){
		return partnerExtraMap.get(id);
	}
	@Override
	public void initApiUserList(){
		List<ApiUser> userList = baseDao.getObjectListByField(ApiUser.class, "status", ApiUser.STATUS_OPEN);
		for(ApiUser user : userList){
			partnerMap.put(user.getPartnerkey(), user);
			ApiUserExtra extra = baseDao.getObject(ApiUserExtra.class, user.getId());
			if(extra!=null){
				partnerExtraMap.put(user.getId(), extra);
			}
		}
		//TODO:∂ØÃ¨≈‰÷√À¢–¬£ø
		ApiConfig acf = baseDao.getObject(ApiConfig.class, ApiConfig.API2_GEWAMEMBER_PARTNERS);
		mobilePartnerList = BeanUtil.getIdList(acf.getContent(), ",");
	}
	@Override
	public boolean isGewaPartner(Long partnerid) {
		return mobilePartnerList.contains(partnerid);
	}
}
