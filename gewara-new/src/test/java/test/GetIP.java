package test;

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Writer;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.LineIterator;
import org.apache.commons.lang.StringUtils;

import com.gewara.Config;
import com.gewara.util.GewaLogger;
import com.gewara.util.LoggerUtils;

public class GetIP {
	private static final transient GewaLogger dbLogger = LoggerUtils.getLogger(GetIP.class, Config.getServerIp(), Config.SYSTEMID);
	private static long[] ipList;
	private static Map<Long, Long> pairMap = new HashMap<Long, Long>();
	private static Map<Long, String> addressMap = new HashMap<Long, String>();

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		/*String format_pattern = "###,###.00";
		NumberFormat nf = new DecimalFormat(format_pattern);
    	Double amount = Double.valueOf("0001001.00");
		String merOrderAmt = nf.format(amount);
		System.out.println(merOrderAmt + "---->");
		System.out.println(Double.valueOf(merOrderAmt));*/
		String hostname = "asdfasdfasdfasdfsdf1212.com";

	    try {
	      InetAddress ipaddress = InetAddress.getByName(hostname);
	      System.out.println("IP address: " + ipaddress.getHostAddress());
	      Process p1 = java.lang.Runtime.getRuntime().exec("ping 8.8.7.7");
	      int returnVal = p1.waitFor();
	      boolean reachable = (returnVal==0);
	      System.out.println("IP address isReachable: " +returnVal +reachable);
	    } catch ( UnknownHostException e ) {
	      System.out.println("Could not find IP address for: " + hostname);
	    } catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	public static void getZjIp() throws IOException {
		InputStreamReader reader = new InputStreamReader(
				new BufferedInputStream(new FileInputStream("Y:\\tttt.txt")));
		LineIterator it = IOUtils.lineIterator(reader);
		int count = 0;
		Writer writer = new BufferedWriter(new FileWriter("Y:\\zj.txt"));
		while(it.hasNext()){
			String line = it.nextLine();
			if(line.contains("’„Ω≠ °")) {
				System.out.println(line);
				writer.write(line + "\n");
				count ++;
			}
		}
		writer.close();
		dbLogger.warn("total: " + count);
	}
	public static void transform() throws IOException {
		List<String> lines = IOUtils.readLines(new FileReader("Y:\\zj.txt"));
		ipList = new long[lines.size() + 4];
		int i=2;
		for(String line: lines){
			String[] ipdata = line.split("[ ]+");
			long ipn1 = getIpNum(ipdata[0]);
			long ipn2 = getIpNum(ipdata[1]);
			//System.out.println(ipdata[0] + "," + ipdata[1] + "-->" + ipn1 + ", " + ipn2);
			ipList[i] = ipn1;
			i ++;
			pairMap.put(ipn1, ipn2);
			//…⁄±¯
			addressMap.put(ipn1, ipdata[2] + "  " +  ipdata[3]);
		}
		ipList[0] = Long.MIN_VALUE;
		ipList[1] = Long.MIN_VALUE + 1;
		ipList[i] = Long.MAX_VALUE -1;
		ipList[i+1] = Long.MAX_VALUE;
		Arrays.sort(ipList);
		ipList[1] = ipList[2]-1;
		ipList[i] = pairMap.get(ipList[i-1]) + 1;
		System.out.println(ipList.length);

		getAddress("222.205.127.22");
		getAddress("222.205.127.255");
		getAddress("61.175.247.149");
		getAddress("61.175.248.0");
		getAddress("61.158.112.49");
		getAddress("222.81.98.151");

	}
	public static String getAddress(String ip){
		long ipNum = getIpNum(ip);
		int idx = findNear(ipNum);
		Long ip1 = ipList[idx];
		Long ip2 = pairMap.get(ip1);
		String find = null;
		if(ipNum>=ip1 && ipNum<=ip2) find = addressMap.get(ip1);
		System.out.println(ip + ":" + find);
		return find;
	}

	private static Long getIpNum(String ip){
		String[] ip1 = StringUtils.split(ip, ".");
		long ipn = Long.parseLong(ip1[0])*256*256*256 + Long.parseLong(ip1[1])*256*256 + Long.parseLong(ip1[2])*256 + Long.parseLong(ip1[3]);
		return ipn;
	}
	private static int findNear(long ipNum){
		int start = 0, end = ipList.length, mid = -1;
		while(start!=end && start+1 != end){
			mid = (start + end)/2;
			if(ipList[mid] ==ipNum) return mid;
			if(ipList[mid +1] ==ipNum) return mid + 1;
			if(ipNum > ipList[mid] && ipNum < ipList[mid +1]) return mid;
			if(ipNum > ipList[mid]){
				start = mid;
			}else{
				end = mid;
			}
		}
		return start;
	}
}
