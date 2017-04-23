
package test;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import org.apache.commons.lang.StringUtils;


public class HotApacheMonitor {
	public static final String QUERY_APACHE = "sc query apache";
	public static final String STOP_APACHE = "sc stop apache";
	public static final String START_APACHE = "sc start apache";
	
	public static void main(String[] args) throws Exception {
		if(isExecDone("RUNNING")){
			stopApache();
			startApache();
		}else if(isExecDone("STOPPED")){
			startApache();
		}
		
		
		/*
		DefaultExecutor executor = new DefaultExecutor();
		CommandLine commandLine = CommandLine.parse(STOP_APACHE); 
		executor.execute(commandLine);
		System.out.println("Apache服务已经停止...准备重启...");
		Thread.sleep(1000);
		executor.execute(CommandLine.parse(START_APACHE));
		Thread.sleep(2000);
		executor.execute(CommandLine.parse(QUERY_APACHE));*/
	}
	
	private static boolean isExecDone(String flag) throws Exception{
		Process queryprocess = Runtime.getRuntime().exec(QUERY_APACHE);
		BufferedReader buffer = new BufferedReader(new InputStreamReader(queryprocess.getInputStream()));
		String lines;
		while (true) {
			lines = buffer.readLine();
			if (lines == null)
				return false;
			if(StringUtils.indexOf(lines, flag) != -1){
				return true;
			}
			System.out.flush();
		}
	}
	private static void stopApache() throws Exception{
		System.out.println("准备停止Apache...");
		Runtime.getRuntime().exec(STOP_APACHE);
		Thread.sleep(5000);
		if(isExecDone("STOPPED")){
			System.out.println("Apache服务已经停止...");
		}else{
			stopApache();
		}
	}
	private static void startApache() throws Exception{
		System.out.println("准备启动Apache...");
		Runtime.getRuntime().exec(START_APACHE);
		Thread.sleep(5000);
		if(isExecDone("RUNNING")){
			System.out.println("Apache服务已经启动...");
		}
	}
}
