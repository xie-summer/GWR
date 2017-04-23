package com.gewara.helper.sys;

import java.util.ArrayList;
import java.util.List;

public abstract class ObjectFilter<T>{
	/**
	 * 过滤掉itemList中不可用的对象，返回这此被过滤掉的对象
	 * @param removeItemList
	 */
	public void applyFilter(List<T> itemList){
		if(!hasFilter()) return;
		List<T> removeList = new ArrayList<T>();
		for(T item:itemList){
			if(excludeOpi(item)) removeList.add(item);
		}
		itemList.removeAll(removeList);
	}
	public abstract boolean hasFilter();
	public abstract boolean excludeOpi(T item);
}
