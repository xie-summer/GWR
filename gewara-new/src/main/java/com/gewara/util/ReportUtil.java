package com.gewara.util;

import java.util.Collection;

public class ReportUtil {
	public static Object getIntSum(Collection rowList, String property, Object defaultStr){
		if(rowList==null) return defaultStr;
		try{
			Double result = getSumInner(rowList, property);
			return result.intValue();
		}catch(Exception e){
			return defaultStr;
		}

	}
	public static Object getSum(Collection rowList, String property, Object defaultStr){
		if(rowList==null) return defaultStr;
		try{
			Double result = getSumInner(rowList, property);
			return result;
		}catch(Exception e){
			return defaultStr;
		}

	}
	
	private static Double getSumInner(Collection rowList, String property){
		double result = 0;
		for(Object row: rowList){
			Object value = BeanUtil.get(row, property);
			if(value!=null){
				result += Double.valueOf(""+value);
			}
		}
		return result;
	}
}
