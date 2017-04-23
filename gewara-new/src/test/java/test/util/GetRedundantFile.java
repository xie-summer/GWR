package test.util;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;

public class GetRedundantFile {
	public static void main(String[] args) throws Exception {
		String javaPath = "E:\\gewaworkspace\\gewara\\shanghai\\src\\main\\java";
		String vmBase = "E:\\gewaworkspace\\gewara\\shanghai\\src\\main\\webapp\\WEB-INF\\";
		String[] vmPath = new String[]{"pages", "template"};
		List<String> allFileList = new ArrayList<String>();
		List<String> vmList = new ArrayList<String>();
		searchFile(allFileList, javaPath, ".java");
		for(String vm: vmPath){
			searchFile(vmList, vmBase + vm, ".vm");
		}
		allFileList.addAll(vmList);
		List<String> searchVmList = new ArrayList<String>(vmList.size());
		String searchVm=null;
		for(String vmFile: vmList){
			searchVm = vmFile.substring(vmBase.length()).replaceAll("\\\\", "/");
			searchVm = searchVm.substring(searchVm.indexOf("/")+1);
			searchVmList.add(searchVm);
		}
		System.out.println("total file:" + allFileList.size() + ", total VM:" + searchVmList.size());
		//List<String> usedFile = new ArrayList<String>();
		int i=0;
		for(String file: allFileList){
			List<String> findList = findInFile(file, searchVmList);
			//usedFile.addAll(findList);
			searchVmList.removeAll(findList);
			i++;
			if(i%50==0) System.out.println("progress:" + i + ", " + file);
		}
		System.out.println("-------------------------------------------not used---------------------------------------");
		for(String key: searchVmList){
			System.out.println(key);
		}
	}
	public static List<String> findInFile(String file, List<String> searchKeyList) throws IOException{
		String encoding = "GBK";
		if(file.endsWith(".vm")) encoding = "UTF-8";
		InputStream is = new BufferedInputStream(new FileInputStream(file));
		List<String> fileLines = IOUtils.readLines(is, encoding);
		is.close();
		List<String> result = new ArrayList<String>();
		for(String key: searchKeyList){
			for(String line: fileLines){
				if(line.contains(key)){
					result.add(key);
					break;
				}
			}
		}
		return result;
	}
	public static void searchFile(List<String> fileList, String path, final String ext) throws IOException{
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
		searchFile(dir, fileList, filter);
	}
	public static void searchFile(File dir, List<String> fileList, FileFilter filter) throws IOException{
		File[] result = dir.listFiles(filter);
		List<File> dirList = new ArrayList<File>();
		System.out.println("search " + dir.getCanonicalPath() + " ...");
		for(File f:result){
			if(f.isDirectory()) dirList.add(f);
			else fileList.add(f.getCanonicalPath());
		}
		for(File f:dirList) searchFile(f, fileList, filter);
	}
}
