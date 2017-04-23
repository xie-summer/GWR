package com.gewara.support;

import java.lang.reflect.InvocationTargetException;
import java.util.Comparator;

import org.apache.commons.beanutils.PropertyUtils;

import com.gewara.util.PinYinUtils;

public class PinyinComparator implements Comparator {
	private String property = "";
	public PinyinComparator(String property){
		this.property = property;
	}
	public int compare(Object o1, Object o2) {
		if(o1==o2) return 0;
		if(o1==null && o2!=null) return -1;
		if(o2==null && o1!=null) return 1;
		String s1 = null, s2 = null;
		try {
			s1 = (String) PropertyUtils.getProperty(o1, property);
			s2 = (String) PropertyUtils.getProperty(o2, property);
		} catch (IllegalAccessException e) {
		} catch (InvocationTargetException e) {
		} catch (NoSuchMethodException e) {
		}
		if(s1==null && s2==null) return 0;
		if(s1!=null && s2 != null) return PinYinUtils.getPinyin(s1).compareTo(PinYinUtils.getPinyin(s2));
		if(s1==null) return -1;
		else return 1;
	}
}
