package com.gewara.constant;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CharacteristicType {
	public static final String CHARACTERISTIC_TYPE_IMAX= "IMAX";
	public static final String CHARACTERISTIC_TYPE_CHINAMAX= "CHINAMAX";
	public static final String CHARACTERISTIC_TYPE_POLYMAX= "POLYMAX";
	public static final String CHARACTERISTIC_TYPE_DOLBYATMOS= "DOLBYATMOS";
	public static final String CHARACTERISTIC_TYPE_4K = "4K";
	public static final String CHARACTERISTIC_TYPE_REALD= "REALD";
	public static final String CHARACTERISTIC_TYPE_DOLBY= "DOLBY";
	public static final String CHARACTERISTIC_TYPE_XPAN= "XPAN";
	public static final String CHARACTERISTIC_TYPE_4D= "4D";
	public static final String CHARACTERISTIC_TYPE_SHAKE= "SHAKE";
	public static final String CHARACTERISTIC_TYPE_LOVERS= "LOVERS";
	public static final String CHARACTERISTIC_TYPE_VIP= "VIP";
	public static final String CHARACTERISTIC_TYPE_DOUBLE_3D= "DOUBLE3D";
	public static final Map<String,String>  characteristicNameMap = new HashMap<String,String>();
	public static List<String> characteristicTypeList = null;
	public static final List<String>  cTypeList = Arrays.asList(new String[]{CHARACTERISTIC_TYPE_IMAX,CHARACTERISTIC_TYPE_CHINAMAX,
			CHARACTERISTIC_TYPE_REALD,CHARACTERISTIC_TYPE_DOUBLE_3D,CHARACTERISTIC_TYPE_4D,CHARACTERISTIC_TYPE_4K,
			CHARACTERISTIC_TYPE_DOLBYATMOS});
	
	static{
		characteristicTypeList = Arrays.asList(new String[]{CHARACTERISTIC_TYPE_IMAX,CHARACTERISTIC_TYPE_CHINAMAX,CHARACTERISTIC_TYPE_POLYMAX,
				CHARACTERISTIC_TYPE_DOLBYATMOS,CHARACTERISTIC_TYPE_4K,CHARACTERISTIC_TYPE_REALD,CHARACTERISTIC_TYPE_DOLBY,
				CHARACTERISTIC_TYPE_XPAN,CHARACTERISTIC_TYPE_4D,CHARACTERISTIC_TYPE_SHAKE,CHARACTERISTIC_TYPE_LOVERS,CHARACTERISTIC_TYPE_VIP,
				CHARACTERISTIC_TYPE_DOUBLE_3D});
		characteristicNameMap.put(CHARACTERISTIC_TYPE_IMAX, "IMAX");
		characteristicNameMap.put(CHARACTERISTIC_TYPE_CHINAMAX, "中国巨幕");
		characteristicNameMap.put(CHARACTERISTIC_TYPE_POLYMAX, "保利巨幕");
		characteristicNameMap.put(CHARACTERISTIC_TYPE_DOLBYATMOS, "杜比全景声");
		characteristicNameMap.put(CHARACTERISTIC_TYPE_4K, "4K");
		characteristicNameMap.put(CHARACTERISTIC_TYPE_REALD, "RealD");
		characteristicNameMap.put(CHARACTERISTIC_TYPE_DOLBY, "Dolby");
		characteristicNameMap.put(CHARACTERISTIC_TYPE_XPAN, "XPAN");
		characteristicNameMap.put(CHARACTERISTIC_TYPE_4D, "4D");
		characteristicNameMap.put(CHARACTERISTIC_TYPE_SHAKE, "震动");
		characteristicNameMap.put(CHARACTERISTIC_TYPE_LOVERS, "情侣");
		characteristicNameMap.put(CHARACTERISTIC_TYPE_VIP, "VIP");
		characteristicNameMap.put(CHARACTERISTIC_TYPE_DOUBLE_3D, "双机3D");
	}
}
