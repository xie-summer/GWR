package com.gewara.service.api;

import java.util.List;

import com.gewara.model.api.ApiUser;
import com.gewara.model.api.ApiUserExtra;
import com.gewara.model.content.GewaCommend;
import com.gewara.model.content.PhoneAdvertisement;


public interface ApiMobileService {
	/**
	 * 获取客户端首页广告信息
	 */
	GewaCommend getPhoneIndexAdvertInfo(String citycode,String tag);
	/**
	 * 查询广告列表
	 * @param apptype 产品
	 * @param osType 系统类型
	 * @param citycode 城市代码
	 * @param advtype 广告类型
	 * @param from 开始索引
	 * @param maxnum 条数
	 * @return
	 */
	List<PhoneAdvertisement> getPhoneAdvertList(String apptype, String osType,String citycode,String advtype,int from, int maxnum);
	//List<Long> getGewaParnteridList();
	boolean isGewaPartner(Long partnerid);
	void initApiUserList();
	ApiUserExtra getApiUserExtraById(Long id);
	ApiUser getApiUserByAppkey(String appkey);
}
