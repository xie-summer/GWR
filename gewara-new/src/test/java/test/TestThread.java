package test;

import java.io.IOException;

import org.apache.commons.lang.math.RandomUtils;

import com.gewara.Config;
import com.gewara.util.GewaLogger;
import com.gewara.util.LoggerUtils;

public class TestThread {
	public static void main(String[] args) throws IOException{
		for (int i = 0; i < 50; i++) {
			Thread t = new Producer("Producer" + i, 3000);
			t.setDaemon(true);
			t.start();
		}
		System.in.read();
	}
	public static final String upper = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnpqrstuvwxyz23456789"; //0,1
	public static String getRandomString(int length){
		StringBuilder sb = new StringBuilder();  
		for(int i = 0; i < length; i++) {  
			sb.append(upper.charAt(RandomUtils.nextInt(upper.length())));  
		}  
		return sb.toString();  
	}
	
	static class Producer extends Thread{

		private final transient GewaLogger dbLogger = LoggerUtils.getLogger(getClass(), Config.getServerIp(), Config.SYSTEMID);
		public Producer(String name, int len){
			this.setName(name);
			this.len = len;
			dbLogger.warn(TestThread.getRandomString(len));
		}
		int len = 50;
		int i=0;
		@Override
		public void run() {
			long cur = System.currentTimeMillis();
			for(;i<5000;i++){
				dbLogger.warn(TestThread.getRandomString(len));
			}
			long dd = System.currentTimeMillis() - cur;
			System.out.println(this.getName() + ":" + dd);
		}	
	}
	
}
