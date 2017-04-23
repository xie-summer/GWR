package test;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;

public class FileUtilsTest {

	public static List<String> readFile(String filename){
		try {
			return FileUtils.readLines(new File(filename));
		} catch (IOException e) {
			e.printStackTrace();
		}
		return Collections.EMPTY_LIST;
	}
	
	public static void main(String[] args) {
		String filename = "r:/2.txt";
		List<String> numberList = new ArrayList<String>(readFile(filename));
		//List<String> nSet = new ArrayList<String>(new HashSet<String>(numberList));
		
		Map<String, Integer> map = new HashMap<String, Integer>();
		
		System.out.println(numberList.size());
		for(String num : numberList){
			System.out.println(num + "=" + map.get(num));
			int c = 0;
			if(map.get(num) == null){
				map.put(num, c);
			}else{
				map.put(num, c++);
			}
			System.out.println(num + "=" + map.get(num));
			System.out.println("***********");
		}
		System.out.println(map);
		
		for(String key : map.keySet()){
			Integer o = map.get(key);
			if(o != 0){
				System.out.println(key + "=" + o);
			}
		}
	}
}
