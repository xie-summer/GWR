package com.gewara.service.content;

import java.util.List;
import java.util.Map;


public interface RecommendService{
	Map<Long, String> getCommuListByToSameMember(Long memberid, String tag, String action);
	List<Map> getCommuMyTreasureMember(Long commuid, Long memberid, String tag, String action);
	
	void memberAddFansCount(Long memberid, String type, String tag, Integer count);
	/************
	 * Common Function 针对专题(MongoDB)
	 * tag: 			专题唯一标识
	 * signanme: 	专题指定类型(新闻, 视频, 论坛, 知道etc.)
	 * */
	List<Map> getRecommendMap(String tag, String signname);
	List<Map> getRecommendMap(String tag, String signname, Long relatedid, int from, int maxnum);
	List<Map> getRecommendMap(String tag, String signname, String parentid);

}
