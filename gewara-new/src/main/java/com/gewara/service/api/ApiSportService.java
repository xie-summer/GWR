package com.gewara.service.api;

import java.util.List;

import com.gewara.model.sport.OpenTimeItem;
import com.gewara.model.sport.SportField;
import com.gewara.support.ErrorCode;
import com.gewara.xmlbind.sport.GstOtt;
import com.gewara.xmlbind.sport.GstSportField;


public interface ApiSportService {	
	/**
	 * 同步场地基本信息
	 * @param sfList
	 */
	void addSportField(List<GstSportField> gstSportFieldList);

	/**
	 * 同步场次
	 * @param gott
	 */
	ErrorCode<List<OpenTimeItem>> saveSportTimeTable(GstOtt gott);
	/**
	 * 修改场次
	 * @param rott
	 */
	void modSportTimeTable(GstOtt rott);
	SportField getSportField(Long sportid, Long itemid, String fieldname);
}
	
