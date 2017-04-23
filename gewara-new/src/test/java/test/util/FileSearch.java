package test.util;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.StringUtils;

public class FileSearch {
	public static void searchFile(Writer writer, String path, boolean onlyGen, Date date) throws IOException{
		FilenameFilter filter = new GenFileFilter(onlyGen, date);
		File dir = new File(path);
		searchFile(dir, writer, filter);
	}
	public static void searchFile(File dir, Writer writer, FilenameFilter filter) throws IOException{
		File[] result = dir.listFiles(filter);
		List<File> dirList = new ArrayList<File>();
		System.out.println("search " + dir.getCanonicalPath() + " ...");
		for(File f:result){
			if(f.isDirectory()) dirList.add(f);
			else writer.append(f.getCanonicalPath() + "\t" + f.lastModified() + "\t" + f.length() + "\n");
		}
		for(File f:dirList) searchFile(f, writer, filter);
	}
	public static void searchFile(File dir, Writer writer, Date from) throws IOException{
		searchFile(dir, writer, new DateFileFilter(from));
	}
	public static class DateFileFilter implements FilenameFilter{
		private Long from;
		public DateFileFilter(Date from){
			this.from = from.getTime();
		}
		@Override
		public boolean accept(File dir, String name) {
			File f = new File(dir, name);
			if(f.isDirectory()) return true;
			return f.lastModified() > from; 
		}
	}
	public static class GenFileFilter implements FilenameFilter{
		private boolean onlyGen;
		private Long from;
		public GenFileFilter(boolean onlyGen, Date from){
			this.onlyGen = onlyGen;
			this.from = from.getTime();
		}
		@Override
		public boolean accept(File dir, String name) {
			File f = new File(dir, name);
			if(f.isDirectory()) return true;
			if(f.lastModified() < from) return false; 
			boolean result = StringUtils.startsWith(name, "w") || 
			StringUtils.startsWith(name, "sw") || StringUtils.startsWith(name, "rw");
			if(onlyGen) return result;
			return !result;
		}
	}
}
