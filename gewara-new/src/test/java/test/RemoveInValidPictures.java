package test;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.LineIterator;
import org.apache.commons.lang.StringUtils;

public class RemoveInValidPictures {

	public static void main(String[] args) {
		
		System.out.println("请输入无效图片Log日志:");
		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
		try {
			String filename = in.readLine();
			removeFromFile(filename);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static String removeFromFile(String filename) throws FileNotFoundException{
		InputStreamReader reader = new InputStreamReader(new BufferedInputStream(new FileInputStream(filename)));
		LineIterator it = IOUtils.lineIterator(reader);
		while(it.hasNext()){
			String line = it.nextLine();
			StringUtils.split(line, "images/[]");
			System.out.println(line);
		}
		return "";
	}
}
