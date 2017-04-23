package com.gewara.util;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

public class FileSearchUtil {
	/**
	 * @param path 搜索路径
	 * @param fileList
	 * @param ext 文件扩展名
	 * @throws IOException
	 */
	public static Map getFileTree(String path, final String ext) throws IOException{
		FileFilter filter = new FileFilter(){
			@Override
			public boolean accept(File pathname) {
				try {
					return pathname.isDirectory() && !pathname.getCanonicalPath().contains(".svn") || pathname.getCanonicalPath().endsWith(ext);
				} catch (IOException e) {
					return false;
				}
			}
			
		};
		File dir = new File(path);
		Map fileMap = new HashMap();
		fileMap.put("path", "/");
		fileMap.put("fileName", dir.getName());
		fileMap.put("children", searchFile(dir, filter, dir.getCanonicalPath()));
		return fileMap;
	}
	public static List searchFile(File dir, FileFilter filter, String basePath) throws IOException{
		File[] files = dir.listFiles();
		List<File> dirList = new ArrayList<File>();
		List<Map> result = new ArrayList<Map>();
		System.out.println("search " + dir.getCanonicalPath() + " ...");
		for(File f:files){
			if(f.isDirectory()) {
				dirList.add(f);
			} else if(filter.accept(f)) {
				Map<String, String> fileMap = new HashMap<String, String>();
				fileMap.put("fileName", f.getName());
				result.add(fileMap);
			}
		}
		for(File f:dirList) {
			Map dirMap = new HashMap();
			dirMap.put("fileName", f.getName());
			dirMap.put("path", StringUtils.replace(f.getAbsolutePath().substring(basePath.length() + 1), "\\", "/") + "/");
			List children = searchFile(f, filter, basePath);
			if(!children.isEmpty()){
				dirMap.put("children", children);
			}
			result.add(dirMap);
		}
		return result;
	}
	public static void main(String[] args) throws IOException{
		Map result = getFileTree("E:\\gewaworkspace\\gptbs\\src\\main\\webapp\\WEB-INF\\pages", ".vm");
		System.out.println(result);
	}
}
