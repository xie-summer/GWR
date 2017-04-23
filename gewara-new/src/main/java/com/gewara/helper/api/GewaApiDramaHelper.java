package com.gewara.helper.api;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.gewara.model.drama.Drama;
import com.gewara.model.drama.DramaPlayItem;
import com.gewara.model.drama.Theatre;
import com.gewara.model.drama.TheatreField;
import com.gewara.model.drama.TheatreSeatArea;
import com.gewara.model.drama.TheatreSeatPrice;
import com.gewara.util.VmUtils;

public class GewaApiDramaHelper extends GewaApiHelper {
	private static Map<Long, String> contentMap = new ConcurrentHashMap<Long, String>();
	//话剧
	public static Map<String, Object> getDramaData(Drama drama, String logo){
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("dramaid", drama.getId());
		params.put("dramaname", drama.getDramaname());
		params.put("englishname", drama.getEnglishname());
		params.put("language", drama.getLanguage());
		params.put("length", drama.getLength());
		params.put("logo", logo);
		params.put("actors", drama.getActors());
		params.put("director", drama.getDirector());
		params.put("type", drama.getType());
		params.put("state", drama.getState());
		params.put("highlight", drama.getHighlight());
		params.put("releasedate", drama.getReleasedate());
		params.put("enddate", drama.getEnddate());
		params.put("clickedtimes", drama.getClickedtimes());
		params.put("collectedtimes", drama.getCollectedtimes());
		String content = contentMap.get(drama.getId());
		if(content==null){
			content = VmUtils.getHtmlText(drama.getContent(), 10000);
			if(content == null){
				contentMap.put(drama.getId(), "");
			}else {
				contentMap.put(drama.getId(), content);
			}
		}
		params.put("content", content);
		return params;
	}
	//剧院
	public static Map<String, Object> getTheatreData(Theatre theatre, String logo, String firstpic){
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("theatreid", theatre.getId());
		params.put("theatrename", theatre.getName());
		params.putAll(getBaseInfo(theatre, logo, firstpic));
		return params;
	}
	
	//话剧场次
	public static Map<String, Object> getDramaPlayItemData(DramaPlayItem dpi){
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("dpid", dpi.getId());
		params.put("name", dpi.getName());
		params.put("dramaid", dpi.getDramaid());
		params.put("fieldid", dpi.getRoomid());
		params.put("fieldname", dpi.getRoomname());
		params.put("theatreid", dpi.getTheatreid());
		params.put("language", dpi.getLanguage());
		params.put("playtime", dpi.getPlaytime());
		params.put("citycode", dpi.getCitycode());
		params.put("dramaname", dpi.getDramaname());
		params.put("theatrename", dpi.getTheatrename());
		params.put("endtime", dpi.getEndtime());
		params.put("opentype", dpi.getOpentype());
		params.put("period", dpi.getPeriod());
		return params;
	}
	//场地
	public static Map<String, Object> getTheatreField(TheatreField field){
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("logo", field.getLimg());
		params.put("fieldid", field.getId());
		params.put("theatrid", field.getTheatreid());
		params.put("fieldnum", field.getFieldnum());
		params.put("fieldtype", field.getFieldtype());
		params.put("name", field.getName());
		params.put("description", field.getDescription());
		return params;
	}
	//场次区域
	public static Map<String, Object> getTheatreSeatAreaData(TheatreSeatArea seatArea){
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("dpid", seatArea.getDpid());
		params.put("theatreid", seatArea.getTheatreid());
		params.put("dramaid", seatArea.getDramaid());
		params.put("status", seatArea.getStatus());
		
		params.put("areaid", seatArea.getId());
		params.put("areaname", seatArea.getAreaname());
		params.put("description", seatArea.getDescription());
		params.put("fieldnum", seatArea.getFieldnum());
		params.put("firstline", seatArea.getFirstline());
		params.put("firstrank", seatArea.getFirstrank());
		params.put("hotzone", seatArea.getHotzone());
		params.put("linenum", seatArea.getLinenum());
		params.put("ranknum", seatArea.getRanknum());
		params.put("standing", seatArea.getStanding());
		params.put("roomnum", seatArea.getRoomnum());
		return params;
	}
	//场次价格
	public static Map<String, Object> getTheatreSeatPriceData(TheatreSeatPrice seatPrice){
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("priceid", seatPrice.getId());
		params.put("dpid", seatPrice.getDpid());
		params.put("areaid", seatPrice.getAreaid());
		params.put("price", seatPrice.getPrice());
		params.put("theatreprice", seatPrice.getTheatreprice());
		params.put("seattype", seatPrice.getSeattype());
		params.put("remark", seatPrice.getRemark());
		return params;
	}
}
