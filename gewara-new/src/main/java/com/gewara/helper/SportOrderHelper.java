/**
 * 
 */
package com.gewara.helper;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import com.gewara.model.pay.Discount;
import com.gewara.model.sport.OpenTimeItem;
import com.gewara.model.sport.OpenTimeTable;
import com.gewara.support.MultiPropertyComparator;
import com.gewara.util.BeanUtil;
import com.gewara.util.DateUtil;

/**
 * @author Administrator
 *
 */
public class SportOrderHelper {
	public static OpenTimeItem getMaxOpenTimeItem(List<OpenTimeItem> otiList, List<Discount> discountList, String opentime, String closetime){
		List<OpenTimeItem> result = new ArrayList<OpenTimeItem>(otiList);
		Collections.sort(result, new MultiPropertyComparator<OpenTimeItem>(new String[]{"price"}, new boolean[]{false}));
		if(discountList.isEmpty()) return result.get(0);
		List<Long> idList = BeanUtil.getBeanPropertyList(discountList, Long.class, "goodsid", true);
		for(OpenTimeItem os:otiList){
			if(StringUtils.isNotBlank(opentime) && StringUtils.isNotBlank(closetime) 
				&& closetime.compareTo(StringUtils.replace(os.getEndhour(),":",""))>0 && opentime.compareTo(StringUtils.replace(os.getHour(),":",""))<=0 ){
				if(!idList.contains(os.getId()))return os;
			}else if(!idList.contains(os.getId())) return os;
		}
		return null;
	}
	public static String getFieldText(List<OpenTimeItem> otiList){
		String info = "";
		Map<String, List<String>> map = new LinkedHashMap<String, List<String>>();
		List<OpenTimeItem> tmpList = new ArrayList<OpenTimeItem>(otiList);
		Collections.sort(tmpList, new MultiPropertyComparator<OpenTimeItem>(new String[]{"ikey"}, new boolean[]{true}));
		for(OpenTimeItem oti : tmpList){
			String toHour = DateUtil.format(DateUtil.addHour(DateUtil.parseDate(oti.getHour(), "HH:mm"), 1), "HH:mm");
			if(!map.containsKey(oti.getHour() + "-" + toHour + " " + oti.getFieldname())) {
				List<String> tmp = new ArrayList<String>();
				tmp.add(oti.getPrice() + "元");
				map.put(oti.getHour() + "-" + toHour + " " + oti.getFieldname(), tmp);
			}else {
				List<String> tmp =  map.get(oti.getFieldname());
				tmp.add(oti.getPrice() + "元");
				map.put(oti.getHour() + "-" + toHour + " " + oti.getFieldname(), tmp);
			}
		}
		for(Map.Entry<String, List<String>> m : map.entrySet()){
			info = info + m.getKey()+" "+ StringUtils.join(m.getValue(), ", ") +";";
		}
		return info;
	}
	public static String getMessageText(List<OpenTimeItem> otiList){
		String message = "";
		Collections.sort(otiList, new MultiPropertyComparator<OpenTimeItem>(new String[]{"hour"}, new boolean[]{true}));
		int i = 0;
		for(OpenTimeItem oti : otiList){
			String[] hours = oti.getHour().split(":");
			String hour = hours[0] + "点";
			if(!StringUtils.equals(hours[1], "00")){
				hour = hour + hours[1] + "分";
			}
			String fname = oti.getFieldname();
			if(i==0) message = hour + fname;
			else message = message + "，" + hour  + fname.replace("场地", "");
			i++;
		}
		return "场次("+message+")";
	}
	public static String getOtis(List<OpenTimeItem> otiList) {
		List<Long> idList = BeanUtil.getBeanPropertyList(otiList, Long.class, "rotiid", true);
		return StringUtils.join(idList, ",");
	}
	public static Timestamp getPlaytime(OpenTimeTable table, List<OpenTimeItem> otiList) {
		List<OpenTimeItem> itemList = new ArrayList<OpenTimeItem>(otiList);
		OpenTimeItem item = itemList.get(0);
		String strDate = DateUtil.format(table.getPlaydate(), "yyyy-MM-dd");
		strDate = strDate + " " + item.getHour() + ":00";
		return DateUtil.parseTimestamp(strDate);
	}
	public static Integer getSumcost(List<OpenTimeItem> otiList) {
		int sum = 0;
		for(OpenTimeItem item : otiList){
			sum = sum + item.getCostprice();
		}
		return sum;
	}
	
	public static List<String> getStarttimeList(Date playdate, OpenTimeItem item){
		List<String> timeList = new ArrayList<String>();
		if(playdate ==  null || item == null || StringUtils.isBlank(item.getHour()) || StringUtils.isBlank(item.getEndhour()) || item.getUnitMinute() == null)
			return timeList;
		String strDate = DateUtil.format(playdate, "yyyy-MM-dd");
		String startDate = strDate + " " + item.getHour() + ":00";
		String endDate = strDate + " " + item.getEndhour() + ":00";
		Timestamp cur = DateUtil.getCurFullTimestamp();
		Timestamp starttime = DateUtil.parseTimestamp(startDate);
		if(cur.after(starttime)){
			double subMinutes = DateUtil.getDiffMinu(cur, starttime);
			int count = Double.valueOf((subMinutes%30) == 0 ? (subMinutes/30) : ((subMinutes/30)+1)).intValue();
			starttime = DateUtil.addMinute(starttime, count*30);
		}
		Timestamp endtime = DateUtil.parseTimestamp(endDate);
		int minutes = 0;
		if(item.hasUnitTime()){
			minutes = item.getUnitMinute();
		}else{
			minutes = 30;
		}
		Timestamp validtime = DateUtil.addMinute(endtime, -minutes);
		if(validtime.before(starttime)) return timeList;
		timeList.add(DateUtil.format(starttime, "HH:mm"));
		while (starttime.before(validtime)) {
			starttime = DateUtil.addMinute(starttime, 30);
			if(starttime.before(endtime)){
				timeList.add(DateUtil.format(starttime, "HH:mm"));
			}
		}
		return timeList;
	}
	public static int getMinutes(OpenTimeTable ott, OpenTimeItem oti, String starttime){
		List<String> timeList = getStarttimeList(ott.getPlaydate(), oti);
		int i = 0;
		for(String time : timeList){
			if(time.compareTo(starttime)>=0) {
				i++;
			}
		}
		return i*30;
	}
	public static List<Integer> getPeriodList(Date playdate, OpenTimeItem item){
		List<Integer> timeList = new ArrayList<Integer>();
		if(playdate ==  null || item == null || StringUtils.isBlank(item.getHour()) || StringUtils.isBlank(item.getEndhour()) || item.getUnitMinute() == null)
			return timeList;
		String strDate = DateUtil.format(playdate, "yyyy-MM-dd");
		String startDate = strDate + " " + item.getHour() + ":00";
		String endDate = strDate + " " + item.getEndhour() + ":00";
		Timestamp starttime = DateUtil.parseTimestamp(startDate);
		Timestamp endtime = DateUtil.parseTimestamp(endDate);
		Timestamp cur = DateUtil.getCurFullTimestamp();
		if(cur.after(starttime)){
			double subMinutes = DateUtil.getDiffMinu(cur, starttime);
			int count = Double.valueOf((subMinutes%30) == 0 ? (subMinutes/30) : ((subMinutes/30)+1)).intValue();
			starttime = DateUtil.addMinute(starttime, count*30);
		}
		double sumMinutes = DateUtil.getDiffMinu(endtime, starttime);
		int tmpMinutes = item.getUnitMinute();
		if(item.hasUnitWhote()){
			tmpMinutes = 30;
		}
		int count = Double.valueOf(sumMinutes/tmpMinutes).intValue();
		for (int i = 1; i<=count; i++) {	
			int tmp = tmpMinutes*i;
			timeList.add(tmp);
		}
		return timeList;
	}
	
	public static String getDefalutEndTime(Date playdate, OpenTimeItem item){
		if(playdate ==  null || item == null || StringUtils.isBlank(item.getHour()) || item.getUnitMinute() == null)
			return "";
		String strDate = DateUtil.format(playdate, "yyyy-MM-dd");
		String startDate = strDate + " " + item.getHour() + ":00";
		Timestamp starttime = DateUtil.parseTimestamp(startDate);
		Timestamp cur = DateUtil.getCurFullTimestamp();
		if(cur.after(starttime)){
			double subMinutes = DateUtil.getDiffMinu(cur, starttime);
			int count = Double.valueOf((subMinutes%30) == 0 ? (subMinutes/30) : ((subMinutes/30)+1)).intValue();
			starttime = DateUtil.addMinute(starttime, count*30);
		}
		int minutes = item.getUnitMinute();
		Timestamp validtime = DateUtil.addMinute(starttime, minutes);
		return DateUtil.format(validtime, "HH:mm");
	}
}
