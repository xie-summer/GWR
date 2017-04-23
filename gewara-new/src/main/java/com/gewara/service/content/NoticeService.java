package com.gewara.service.content;

import java.util.List;

import com.gewara.model.content.Notice;


public interface NoticeService {

	//根据关联id和tag查询公告信息列表
	List<Notice> getNoticeListByCommuid(Long relateid,String tag,int from,int maxnum);
	//根据关联id和tag查询公告信息数量
	Integer getNoticeCountByCount(Long relatedid,String tag);


}
