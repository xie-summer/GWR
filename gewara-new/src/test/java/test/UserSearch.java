package test;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.Writer;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;

public class UserSearch {

	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		//matchMail("E:/logger/mem_email.txt", "E:/logger/email.txt");
		//removeMatch("E:/logger/email.txt", "E:/logger/match.txt", "E:/logger/excludeMatch.txt");
		removeRepeat("E:/logger/excludeMatch.txt", "E:/logger/norepeat.txt");
	}
	public static void removeRepeat(String srcFile, String dstFile) throws Exception {
		Writer writer = new BufferedWriter(new FileWriter(dstFile));
		File tmp1 = new File(srcFile);
		File tmp2 = new File("E:/logger/tmp.txt");
		String[] firstList = new String[]{"0","1","2","3","4","5","6","7","8","9",
				"a","b","c","d","e","f","g","h","i","j","k","l","m","n",
				"o","p","q","r","s","t","u","v","w","x","y","z"};
		for(String first: firstList){
			FileUtils.copyFile(tmp1, tmp2);
			Writer tmpWriter = new BufferedWriter(new FileWriter(tmp1));
			BufferedReader in = new BufferedReader(new FileReader(tmp2));
			String line = in.readLine();
			Set<String> mailList = new TreeSet<String>();
			int i=1;
			while(line!=null){
				String s = line.trim();
				if(s.startsWith(first)){
					mailList.add(s);
					i++;
				}else{
					tmpWriter.write(s + "\n");
				}
				line = in.readLine();
				if(i%1000==0) System.out.println("find:" + i);
			}
			tmpWriter.close();
			FileUtils.copyFile(tmp1, tmp2);
			System.out.println(first + ":" + mailList.size());
			for(String mail: mailList){
				writer.write(mail + "\n");
			}
			writer.flush();
		}
		writer.close();
	}
	public static void removeMatch(String srcFile, String matchFile, String dstFile) throws Exception {
		Set<String> set = new HashSet<String>();
		addEmail(set, matchFile);
		Writer writer = new BufferedWriter(new FileWriter(dstFile));
		BufferedReader in = new BufferedReader(new FileReader(srcFile));
		String line = in.readLine();
		int i=0;
		while(line!=null){
			if(!set.contains(line.trim())){
				writer.write(line+"\n");
			}
			line = in.readLine();
			i++;
			if(i%10000==0) System.out.println(i + ":" + line);
		}
		writer.close();
	}
	public static void matchMail(String file1, String file2) throws Exception {
		Set<String> set = new HashSet<String>();
		addEmail(set, file1);
		Writer writer = new BufferedWriter(new FileWriter("E:/logger/match.txt"));
		BufferedReader in = new BufferedReader(new FileReader(file2));
		String line = in.readLine();
		int i=0;
		Set<String> match = new HashSet<String>();
		while(line!=null){
			if(set.contains(line.trim())){
				//writer.write(line+"\n");
				match.add(line.trim());
			}
			line = in.readLine();
			i++;
			if(i%10000==0) System.out.println(i + ":" + line);
		}
		for(String s:match){
			writer.write(s+"\n");
		}
		writer.close();
	}
	public static void addAllEmail() throws Exception{
		Writer writer = new BufferedWriter(new FileWriter("E:/logger/email.txt"));
		//writeEmail(writer, new File("E:/email/tianya_11.txt"));
		File dir = new File("E:/email");
		File[] files = dir.listFiles();
		for(File file: files){
			System.out.println("Start Parse:" + file.getCanonicalPath());
			writeEmail(writer, file);
			System.out.println("End Parse:" + file.getCanonicalPath());
		}
		writer.close();
	}
	public static void addEmail(Set mails, String filename) throws Exception{
		BufferedReader in = new BufferedReader(new FileReader(filename));
		String line = in.readLine();
		int i=0;
		while(line!=null){
			mails.add(line.trim());
			line = in.readLine();
			i++;
			if(i%10000==0) System.out.println(i + ":" + line);
		}
	}
	public static void writeEmail(Writer writer, File file) throws Exception{
		Pattern pattern = Pattern.compile("^[A-Z0-9._-]+@[A-Z0-9_-]+(\\.[A-Z0-9_-]+)*(\\.[A-Z]{2,4})+$", Pattern.CASE_INSENSITIVE);
		BufferedReader in = new BufferedReader(new FileReader(file));
		String line = in.readLine();
		int count = 0, success=0;
		while(line!=null){
			String[] result = StringUtils.split(line.trim(), " \t");
			//System.out.println(StringUtils.join(result, "#"));
			for(int i=0;i<result.length;i++){
				if(pattern.matcher(result[i].trim()).find()){
					writer.write(result[i].trim().toLowerCase()+"\n");
					success++;
				}
			}
			count++;
			line = in.readLine();
			if(count%100000==0) System.out.println(count + ":" + line + ",success:" + success);
		}
	}
}
