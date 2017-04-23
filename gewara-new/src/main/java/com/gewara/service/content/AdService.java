package com.gewara.service.content;

import java.util.List;

import com.gewara.model.content.AdPosition;
import com.gewara.model.content.Advertising;

public interface AdService {
	List<AdPosition> getAdPositionListByTag(String tag, int from, int maxnum);
	Integer getAdPositionCountByTag(String tag);
	List<Advertising> getAdvertisingListByAdPositionid(String citycode,Long adpositionid, String order);
	List<Advertising> getAdvertisingListByAdPositionid(String citycode,Long adpositionid);
	Integer getAdCountByAdPosition(String citycode,String pid);
	Integer getSumRemaintimesByAdPosition(String citycode,Long adpid);
	void changRaterang(String citycode,Long adpid);
	Advertising getRandomAd(String citycode,String pid);
	Advertising getAdvertising(String tag, Long relatedid,String pTag);
	List<Advertising> getAdListByPid(String citycode, String pid);
	List<Advertising> getAdListByPid(String citycode, String pid, String tag, Long relatedid);
	/**
	 * 
	 * @param string
	 * @return
	 */
	Advertising getFirstAdByPostionTag(String tag);
}
