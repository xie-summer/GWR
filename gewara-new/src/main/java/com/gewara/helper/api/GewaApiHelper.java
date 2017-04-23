package com.gewara.helper.api;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang.StringUtils;

import com.gewara.model.common.BaseInfo;
import com.gewara.model.content.Picture;
import com.gewara.util.VmUtils;

public class GewaApiHelper {
	private static Map<String, String> contentMap = new ConcurrentHashMap<String, String>();
	//图片信息
	public static Map<String, Object> getPicture(Picture picture, String pictureUrl){
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("pictureid", picture.getId());
		params.put("pictureUrl", pictureUrl);
		params.put("description", picture.getDescription());
		return params;
	}
	
	//场馆信息
	protected static Map<String, Object> getBaseInfo(BaseInfo info, String logo, String firstpic){
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("englishname", info.getEnglishname());
		params.put("logo", logo);
		if(StringUtils.isNotBlank(firstpic)) params.put("firstpic", firstpic);
		params.put("pointx", info.getPointx());
		params.put("pointy", info.getPointy());
		params.put("bpointx", info.getBpointy());
		params.put("bpointy", info.getBpointx());
		params.put("opentime", info.getOpentime());
		params.put("citycode", info.getCitycode());
		params.put("cityname", info.getCityname());
		params.put("countycode", info.getCountycode());
		params.put("contactphone", info.getContactphone());
		params.put("countyname", info.getCountyname());
		params.put("indexarea", info.getIndexareaname());
		params.put("address", info.getAddress());
		params.put("clickedtimes", info.getClickedtimes());
		params.put("collectedtimes", info.getCollectedtimes());
		String key = info.getClass().getName()+info.getId();
		String content = contentMap.get(key);
		if(content==null){
			content = VmUtils.getHtmlText(info.getContent(), 10000);
			if(content == null){
				contentMap.put(key, "");
			}else {
				contentMap.put(key, content);
			}
		}
		params.put("content", content);
		return params;
	}
}
