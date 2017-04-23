package test;

import java.io.File;
import java.util.Collection;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;

public class FileGewaProjectCheck {

	public static final String vmpath = "E:/gewaworkspace/shanghai/gewara-new/src/main/webapp/WEB-INF/pages/";
	
	public static void main(String[] args) {
		File file = new File(vmpath);
		
		
		Collection<File> files = FileUtils.listFiles(file, null, true);
		for(File inner : files){
			String filename = inner.getName();
			if(StringUtils.contains(filename, "-")){
				System.out.println(filename);
			}
		}
	}
}
