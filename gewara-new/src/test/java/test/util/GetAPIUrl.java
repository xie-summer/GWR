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
import org.apache.commons.lang.StringUtils;

public class GetAPIUrl {
	public static void main(String[] args) throws Exception {
		//compareRows("F:/hibernate.txt", "F:/database.txt");
		String searchKey = "@RequestMapping(\"";
		String javaPath = "E:\\gewaworkspace\\gewara\\shanghai\\src\\main\\java\\com\\gewara\\web\\action";
		List<String> urlList = searchUrl(javaPath, searchKey);
		System.out.println(StringUtils.join(urlList, "\n"));
		//compareUrl();
	}
	public static void compareUrl() throws Exception {
		String javaPath = "E:\\gewaworkspace\\gewara\\shanghai\\src\\main\\java\\com\\gewara\\web\\action";
		List<String> urlList = searchUrl(javaPath, "@RequestMapping(");
		List<String> used = IOUtils.readLines(new FileInputStream("F:/usedUrl.txt"));
		//List<String> houtai = IOUtils.readLines(new FileInputStream("F:/manageUrl.txt"));
		String line = "============================================================================================";
		System.out.println(line);
		System.in.read();
		System.out.println("total:" + urlList.size() + ", used:" + used.size());
		//houtai.removeAll(urlList);
		urlList.removeAll(used);
		System.in.read();
		System.out.println(line);
		for(String url: urlList){
			System.out.println(url);
		}
	}
	public static void compareRows(String file1, String file2) throws IOException {
		List<String> list1 = IOUtils.readLines(new FileInputStream(file1));
		List<String> list2 = IOUtils.readLines(new FileInputStream(file2));
		List<String> one = new ArrayList<String>(list1);

		one.removeAll(list2);
		list2.removeAll(list1);
		
		System.out.println("--------only list1----------------");
		System.out.println(StringUtils.join(one, "\n"));
		System.out.println("--------only list2----------------");
		System.out.println(StringUtils.join(list2, "\n"));
	}
	public static List<String> searchUrl(String javaPath, String searchKey) throws Exception{ 
		List<String> fileList = new ArrayList<String>();
		searchFile(fileList, javaPath, ".java");
		List<String> result = new ArrayList<String>(200);
		int i=0;
		for(String file: fileList){
			List<String> findList = findInFile(file, searchKey);
			//usedFile.addAll(findList);
			result.addAll(findList);
			i++;
			if(i%50==0) System.out.println("progress:" + i + ", " + file);
		}
		List<String> urlList = new ArrayList<String>();
		for(String key: result){
			int idx1 = key.indexOf(searchKey) + searchKey.length();
			int idx2 = key.indexOf("\"", idx1 + 1);
			if(idx1>0 && idx2>0){
				urlList.add(key.substring(idx1+1, idx2));
			}
		}
		return urlList;
	}
	public static List<String> findInFile(String file, String regex) throws IOException{
		String encoding = "GBK";
		InputStream is = new BufferedInputStream(new FileInputStream(file));
		List<String> fileLines = IOUtils.readLines(is, encoding);
		is.close();
		List<String> result = new ArrayList<String>();
		for(String line: fileLines){
			if(line.contains(regex)){
				result.add(line);
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
