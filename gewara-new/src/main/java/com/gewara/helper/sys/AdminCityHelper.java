package com.gewara.helper.sys;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;

import com.gewara.constant.AdminCityContant;
import com.gewara.model.common.GewaCity;

public class AdminCityHelper implements Comparable<AdminCityHelper> {
	private String provincename;
	private List<GewaCity> gewaCityList;

	private AdminCityHelper(String provinceName, List<GewaCity> gewaCityList) {
		this.provincename = provinceName;
		this.gewaCityList = gewaCityList;
	}
	
	public static List<AdminCityHelper> province2CityListMap() {
		Map<GewaCity, List<GewaCity>> province2CityListMap = AdminCityContant.getAdmMap();
		List<GewaCity> zhixiashi = new LinkedList<GewaCity>();
		List<AdminCityHelper> adminCityHelperList = new ArrayList<AdminCityHelper>();
		TreeSet<AdminCityHelper> adminCitySet = new TreeSet<AdminCityHelper>();
		for (Map.Entry<GewaCity, List<GewaCity>> entry : province2CityListMap.entrySet()) {
			// 直辖市key
			if (StringUtils.startsWith(entry.getKey().getProvincename(), "上海") || StringUtils.startsWith(entry.getKey().getProvincename(), "北京")
					|| StringUtils.startsWith(entry.getKey().getProvincename(), "重庆") || StringUtils.startsWith(entry.getKey().getProvincename(), "天津")) {
				zhixiashi.addAll(entry.getValue());
			} else {
				// 省份key
				adminCitySet.add(new AdminCityHelper(entry.getKey().getProvincename(), entry.getValue()));
			}
		}
		adminCityHelperList.add(0, new AdminCityHelper("直辖市", zhixiashi));
		adminCityHelperList.addAll(adminCitySet);
		return adminCityHelperList;
	}
	
	public String getProvincename() {
		return provincename;
	}

	public void setProvincename(String provincename) {
		this.provincename = provincename;
	}

	public List<GewaCity> getGewaCityList() {
		return gewaCityList;
	}

	public void setGewaCityList(List<GewaCity> gewaCityList) {
		this.gewaCityList = gewaCityList;
	}

	@Override
	public int compareTo(AdminCityHelper o) {
		if (o == null || CollectionUtils.isEmpty(o.getGewaCityList()) || CollectionUtils.isEmpty(this.getGewaCityList())) {
			return -1;
		}
		int size = o.getGewaCityList().size() - this.getGewaCityList().size();
		// 为了后续放treemap方便，如果list的长度相同返回1
		return size == 0? 1:size;
	}
}

