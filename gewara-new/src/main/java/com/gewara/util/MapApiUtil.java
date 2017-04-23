package com.gewara.util;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

public class MapApiUtil {
	// 根据坐标，获取地址
	public static String getBaiduMapAddress(String x, String y) {
		if (StringUtils.length(x) < 5 || StringUtils.length(y) < 5)
			return null;
		String baiduMapApiUrl = "http://api.map.baidu.com/geocoder";
		Map<String, String> params = new HashMap<String, String>();
		params.put("output", "json");
		params.put("location", y + "," + x);
		params.put("key", "fa8a85952806516caf8d04f3ccfe5e89");
		HttpResult result = HttpUtils.getUrlAsString(baiduMapApiUrl, params);
		if (result.isSuccess()) {
			BaiduMapAddress bmd = JsonUtils.readJsonToObject(BaiduMapAddress.class, result.getResponse());
			if (bmd != null && StringUtils.equalsIgnoreCase(bmd.getStatus(), "OK") && bmd.getResult()!=null) {
				return bmd.getResult().getFormatted_address();
			}
		}
		return result.getResponse();
	}
	public static boolean isValidPoinx(Double x) {
		if(x==null) return false;
		Double d = 121.47494494915009D;//默认的poinx
		if(d.equals(x)) return false;
		if(StringUtils.length(x+"")<=4) return false;
		return true;
	}
	public static class BaiduMapAddress {
		private String status;
		private Result result;

		public String getStatus() {
			return status;
		}

		public void setStatus(String status) {
			this.status = status;
		}

		public Result getResult() {
			return result;
		}

		public void setResult(Result result) {
			this.result = result;
		}
	}

	public static class Result {
		private String formatted_address;
		private String business;
		private AddressComponent addressComponent;
		public String getFormatted_address() {
			return formatted_address;
		}

		public void setFormatted_address(String formatted_address) {
			this.formatted_address = formatted_address;
		}

		public String getBusiness() {
			return business;
		}

		public void setBusiness(String business) {
			this.business = business;
		}

		public AddressComponent getAddressComponent() {
			return addressComponent;
		}

		public void setAddressComponent(AddressComponent addressComponent) {
			this.addressComponent = addressComponent;
		}
	}

	public static class AddressComponent {
		private String city;
		private String district;
		private String province;
		private String street;
		private String street_number;

		public String getCity() {
			return city;
		}

		public void setCity(String city) {
			this.city = city;
		}

		public String getDistrict() {
			return district;
		}

		public void setDistrict(String district) {
			this.district = district;
		}

		public String getProvince() {
			return province;
		}

		public void setProvince(String province) {
			this.province = province;
		}

		public String getStreet() {
			return street;
		}

		public void setStreet(String street) {
			this.street = street;
		}

		public String getStreet_number() {
			return street_number;
		}

		public void setStreet_number(String street_number) {
			this.street_number = street_number;
		}
	}
}
