package com.gewara.util;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.betwixt.io.BeanReader;

import com.gewara.xmlbind.partner.YoukuVideo;

public class YoukuApiUtil {
	// 根据坐标，获取地址
	public static YoukuVideo getYoukuImg(String videoid) {
		String baiduMapApiUrl = "http://api.3g.youku.com/openapi-wireless/getVideoDetail";
		Map<String, String> params = new HashMap<String, String>();
		params.put("rp", "1");
		params.put("rt", "1");
		params.put("pid", "d2311fb79f91b038");
		params.put("format", "1,2,3,4,5,6");
		params.put("vid", videoid);
		HttpResult result = HttpUtils.getUrlAsString(baiduMapApiUrl, params);
		YoukuVideo video = null;
		if (result.isSuccess()) {
			BeanReader beanReader = ApiUtils.getBeanReader("document/results", YoukuVideo.class);
			video = (YoukuVideo)ApiUtils.xml2Object(beanReader, result.getResponse());
		}
		return video;
	}
	public static void main(String[] args) {
		YoukuVideo video = getYoukuImg("XNDE4OTgwMTc2");
		System.out.println(video.getImg());
	}
}
