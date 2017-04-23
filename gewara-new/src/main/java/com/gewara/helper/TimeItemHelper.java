package com.gewara.helper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import com.gewara.constant.OpenTimeItemConstant;
import com.gewara.constant.OpenTimeTableConstant;
import com.gewara.model.sport.OpenTimeItem;
import com.gewara.support.MultiPropertyComparator;
import com.gewara.util.DateUtil;

public class TimeItemHelper {
	private List<OpenTimeItem> otiList;
	public OpenTimeItem getOtiDetail(Long field, String hour) {
		for (OpenTimeItem sti : getOtiList()) {
			if (("" + sti.getFieldid()).equals(field + "")
					&& sti.getHour().equals(hour))
				return sti;
		}
		return null;
	}
	
	public List<String> getPlayHourList() {
		List<String> hourList = new ArrayList();
		for(OpenTimeItem item : otiList){
			if(!hourList.contains(item.getHour()) && !StringUtils.equalsIgnoreCase(item.getStatus(), OpenTimeItemConstant.STATUS_DELETE)) hourList.add(item.getHour());
		}
		return hourList;
	}
	public Map<String, OpenTimeItem> getOtiMap() {
		Map<String, OpenTimeItem> otiMap = new HashMap<String, OpenTimeItem>();
		for(OpenTimeItem item : otiList){
			String key = item.getFieldid() + item.getHour();
			otiMap.put(key, item);
		}
		return otiMap;
	}
	public List<OpenTimeItem> getOperItemList(){
		List<OpenTimeItem> itemList = new ArrayList<OpenTimeItem>();
		for(OpenTimeItem item : otiList){
			if(item.hasAvailable() || item.hasWait() || item.hasStatus(OpenTimeItemConstant.STATUS_SOLD)){
				itemList.add(item);
			}
		}
		return itemList;
	}
	
	public Map<Integer, String> getHourPriceMap(){
		Map<Integer, List<String>> hpMap = new LinkedHashMap<Integer, List<String>>();
		Map<Integer, String> resultMap = new LinkedHashMap<Integer, String>();
		List<OpenTimeItem> list = new ArrayList<OpenTimeItem>(otiList);
		Collections.sort(list, new MultiPropertyComparator<OpenTimeItem>(new String[]{"hour"}, new boolean[]{true}));
		for(OpenTimeItem oti : list){
			List<String> tmpList =  new ArrayList<String>();
			if(hpMap.containsKey(oti.getPrice())) {
				tmpList =  hpMap.get(oti.getPrice());
			}
			if(!tmpList.contains(oti.getHour())) tmpList.add(oti.getHour());
			hpMap.put(oti.getPrice(), tmpList);
		}
		for(Integer prc : hpMap.keySet()){
			resultMap.put(prc, getSimpleHour(hpMap.get(prc)));
		}
		return resultMap;
	}
	public String getSimpleHour(List<String> hourList){
		Map<String ,String> map = new LinkedHashMap<String, String>();
		int i = 0;
		String tmp = "";
		String key = "";
		for(String hour : hourList){
			if(i==0) {
				tmp = hour;
				key = hour;
				map.put(key, "");
			}else {
				if(getBeValue(tmp, hour)==1){
					map.put(key, hour);
					tmp = hour;
				}else {
					key = hour;
					tmp = hour;
					map.put(key, "");
				}
			}
			i++;
		}
		String result = "";
		for(String k : map.keySet()){
			if(StringUtils.isNotBlank(map.get(k))) result = result + "," + k + "--" + map.get(k);
			else result = result + "," + k;
		}
		if(StringUtils.isNotBlank(result)) result = result.substring(1);
		return result;
	}

	private int getBeValue(String hour1, String hour2){
		int h1 = Integer.valueOf(hour1.split(":")[0]);
		int h2 = Integer.valueOf(hour2.split(":")[0]);
		return h2 - h1;
	}
	public List<OpenTimeItem> getOtiList() {
		return otiList;
	}
	public void setOtiList(List<OpenTimeItem> otiList) {
		this.otiList = otiList;
	}
	public  TimeItemHelper(List<OpenTimeItem> otiList){
		this.otiList = otiList;
	}
	public String getOpenStatus(OpenTimeItem oti, String opentype){
		if(StringUtils.equals(opentype, OpenTimeTableConstant.OPEN_TYPE_FIELD)){
			return oti.hasAvailable()?"open":"lock";
		}else{
			return oti.getValidtime().after(DateUtil.getMillTimestamp())?"open":"lock";
		}
	}
}
