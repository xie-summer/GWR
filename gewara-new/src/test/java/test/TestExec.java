package test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.exec.ExecuteException;

import com.gewara.util.HttpUtils;

public class TestExec {

	/**
	 * @param args
	 * @throws IOException 
	 * @throws ExecuteException 
	 */
	public static void main(String[] args) {
		String[] servers=new String[]{"43","44","45","46","47","48","49","50","51","39","49","37"};
		String base = "http://172.22.1.";
		for(String server:servers){
			String result = HttpUtils.getUrlAsString(base + server + ":82/server.jsp").getResponse();
			System.out.println(result);
		}
	}
	public static String getStr(int limitperiod){
		int hour = limitperiod/60;
		int min = limitperiod%60;
		int day = 0;
		if(hour > 24){
			day = hour/24;
			hour = hour % 24;
		}
		String result = (day > 0?day+"天":"") + (hour>0? hour+"小时":"") + (min>0?min+"分":"");
		return result;
	}
	//"D:\\xampp\\htdocs\\shanghai"
	public static void findFile(String path){
		List errorlist = new ArrayList();
		List exists = new ArrayList();
		File dir = new File(path);
		
		BufferedReader read;
		try {
			read = new BufferedReader(new InputStreamReader(new FileInputStream("D:/vmlist.txt")));
			for(String line = read.readLine(); line != null; line = read.readLine()){ 
			   errorlist.add(line);
				System.out.println(line); 
			}
			read.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println(errorlist.size()); 
		if(!dir.exists() || !dir.isDirectory()) {
			System.out.println("目录错误");
		}
		File temp;
		File[] list = dir.listFiles();
		for(int i = 0; i < list.length; i++) {
			temp = list[i];
			if(temp.isDirectory()) {
				findFile(temp.getAbsolutePath());
			}else if(temp.isFile()) {
				if(temp.getName().contains("vm")) {
					/*for (int j = 0; j < errorlist.size(); j++) {
						errorlist.get(j).toString().equals(temp.getName());
						exists.add(temp.getName());
					}*/
				}
				exists.add(temp.getName());
				System.out.println(temp.getName());
			}
		}
	}
	
}
