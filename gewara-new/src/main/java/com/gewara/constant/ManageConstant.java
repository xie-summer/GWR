package com.gewara.constant;

import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.collections.map.UnmodifiableMap;

/**
 * 提供后台相关常量
 * @since Dec 31, 2011, 4:10:09 PM
 * @author acerge(gebiao)
 * @function 
 */
public class ManageConstant {
	public static final Map<String, String> deptMap;
	public static final Map<String, String> applyMap;
	
	static{
		Map<String, String> tmp = new LinkedHashMap<String, String>();
		tmp.put("0102", "0102-法务组");
		tmp.put("0103", "0103-财务部");
		tmp.put("0104", "0104-人事行政部");
		tmp.put("0106", "0106-信息支持中心");
		//tmp.put("03", "03-行政部");
		tmp.put("0101", "0101-总经办");
		//tmp.put("05", "05-产品运营部");
		tmp.put("0202", "0202-电子商务中心");
		tmp.put("0203", "0203-公共产品部");
		tmp.put("0201", "0201-技术部");
		tmp.put("0301", "0301-品牌媒介组");
		tmp.put("0302", "0302-商务合作部");
		tmp.put("0303", "0303-团体销售部");
		tmp.put("0405", "0405-演出事业部");
		tmp.put("0406", "0406-电影事业部");
		tmp.put("0407", "0407-运动事业部");
		//tmp.put("12", "12-网站运营中心");
		//tmp.put("13", "13-手机运营组");
		deptMap = UnmodifiableMap.decorate(tmp);
		
		tmp = new LinkedHashMap<String, String>();
		tmp.put("01", "01-线上活动");
		tmp.put("02", "02-市场销售");
		tmp.put("03", "03-商务合作");
		tmp.put("04", "04-公司赠送");
		tmp.put("05", "05-线下活动");
		tmp.put("06", "06-员工福利");
		tmp.put("07", "07-媒体置换");
		tmp.put("08", "08-客服赔偿");
		tmp.put("09", "09-代订购票");
		tmp.put("99", "99-其他");
		tmp.put("11", "11-销售福利");
		tmp.put("12", "12-销售营销");
		tmp.put("13", "13-商务营销");
		tmp.put("14", "14-商务活动");
		tmp.put("15", "15-积分兑换");
		tmp.put("16", "16-抢票活动");
		tmp.put("17", "17-更换票券");
		tmp.put("18", "18-包场专用");
		applyMap = UnmodifiableMap.decorate(tmp);
	}
}
