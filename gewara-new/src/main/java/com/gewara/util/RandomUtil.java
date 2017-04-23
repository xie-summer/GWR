package com.gewara.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
public class RandomUtil {
	public static <T> List<T> getRandomObjectList(List<T> originalList, int getnum){
		Random random = new Random();
		if(originalList==null || originalList.size() <=getnum) return originalList;
		List<T> result = new ArrayList<T>();
		int maxnum = originalList.size();
		for(int i=0; i< getnum;){
			T obj = originalList.get(random.nextInt(maxnum));
			if(!result.contains(obj)){
				result.add(obj); i++ ;
			}
		}
		return result;
	}
	
	public static <T> T getRandomObject(List<T> objList){
		if(objList == null || objList.size()==0) return null;
		if(objList.size() == 1) return objList.get(0);
		return objList.get(new Random().nextInt(objList.size()));
	}
	
}
