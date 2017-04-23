package com.gewara.util;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.Serializable;
import java.io.Writer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.LineIterator;
import org.apache.commons.lang.StringUtils;

import com.gewara.Config;

public class IPUtil {
	public final static IpData ipData = new IpData(); 
	private final static transient GewaLogger dbLogger = LoggerUtils.getLogger(IPUtil.class, Config.getServerIp(), Config.SYSTEMID);
	public static void getProvinceIp(String province) throws IOException {
		InputStreamReader reader = new InputStreamReader(
				new BufferedInputStream(new FileInputStream("E:\\ipall.txt")));
		LineIterator it = IOUtils.lineIterator(reader);
		int count = 0;
		Writer writer = new BufferedWriter(new FileWriter("E:\\" + province + ".txt"));
		while(it.hasNext()){
			String line = it.nextLine();
			if(line.contains(province)) {
				dbLogger.warn(line);
				writer.write(line + "\n");
				count ++;
			}
		}
		writer.close();
		dbLogger.warn("ip data total: " + count);
	}
	public static void initInner(){
		Set<String> tmpList = new HashSet<String>();
		try {
			List<String> rowList = IOUtils.readLines(IPUtil.class.getClassLoader().getResourceAsStream("innerip.txt"));
			for(String ip: rowList) if(!StringUtils.startsWith(ip, "#")) tmpList.add(ip);
		} catch (IOException e) {
			dbLogger.error("", e);
		}
		ipData.inneripList = tmpList;
	}
	public static void initAll(){
		init();
		initInner();
	}
	
	public static class IpData implements Serializable{
		private static final long serialVersionUID = 1L;
		private long[] ipList;
		private Map<Long, Long> pairMap = new HashMap<Long, Long>();
		private Map<Long, String> addressMap = new HashMap<Long, String>();
		private Set<String> inneripList = new HashSet<String>();

	}
	public static int init() {
		try {
			Reader reader = new BufferedReader(new InputStreamReader(IPUtil.class.getClassLoader().getResourceAsStream("ipdata.txt"), "utf-8"));
			List<String> lines = IOUtils.readLines(reader);
			return init(lines);
		}catch(Exception e){
			throw new IllegalArgumentException("IPData ERROR!!!", e);
		}
	}
	public static int init(InputStream is){
		try {
			Reader reader = new BufferedReader(new InputStreamReader(is, "utf-8"));
			List<String> lines = IOUtils.readLines(reader);
			return init(lines);
		}catch(Exception e){
			throw new IllegalArgumentException("IPData ERROR!!!", e);
		}
		
	}
	public static int init(List<String> lines) {
		ipData.ipList = new long[lines.size() + 4];
		int i=2;
		int success = 0, error = 0;
		for(String line: lines){
			try{
				String[] ipdata = line.split("[ ]+");
				long ipn1 = getIpNum(ipdata[0]);
				long ipn2 = getIpNum(ipdata[1]);
				ipData.ipList[i] = ipn1;
				i ++;
				ipData.pairMap.put(ipn1, ipn2);
				//ÉÚ±ø
				ipData.addressMap.put(ipn1, ipdata[2] + "  " +  ipdata[3]);
				success ++;
			}catch(Exception e){
				error ++;
				dbLogger.warn("RowError:" + line + ", LineNo:" + (success + error));
			}
		}
		ipData.ipList[0] = Long.MIN_VALUE;
		ipData.ipList[1] = Long.MIN_VALUE + 1;
		ipData.ipList[i] = Long.MAX_VALUE -1;
		ipData.ipList[i+1] = Long.MAX_VALUE;
		Arrays.sort(ipData.ipList);
		ipData.ipList[1] = ipData.ipList[2]-1;
		ipData.ipList[i] = ipData.pairMap.get(ipData.ipList[i-1]) + 1;
		dbLogger.warn("Init IP Data, total count:" + lines.size() + ",success:" + success + ",error:" + error);
		return ipData.ipList.length;
	}
	public static String getAddress(String ip){
		long ipNum = getIpNum(ip);
		int idx = findNear(ipNum);
		Long ip1 = ipData.ipList[idx];
		Long ip2 = ipData.pairMap.get(ip1);
		String find = null;
		if(ipNum>=ip1 && ipNum<=ip2) find = ipData.addressMap.get(ip1);
		return find;
	}

	private static Long getIpNum(String ip){
		String[] ip1 = StringUtils.split(StringUtils.trim(ip), ".");
		if(ip1.length > 3){
			return Long.parseLong(ip1[0])*256*256*256 + Long.parseLong(ip1[1])*256*256 + Long.parseLong(ip1[2])*256 + Long.parseLong(ip1[3]);
		}
		return 0L;
	}
	
	private static int findNear(long ipNum){
		int start = 0, end = ipData.ipList.length, mid = -1;
		while(start!=end && start+1 != end){
			mid = (start + end)/2;
			if(ipData.ipList[mid] ==ipNum) return mid;
			if(ipData.ipList[mid +1] ==ipNum) return mid + 1;
			if(ipNum > ipData.ipList[mid] && ipNum < ipData.ipList[mid +1]) return mid;
			if(ipNum > ipData.ipList[mid]){
				start = mid;
			}else{
				end = mid;
			}
		}
		return start;
	}
	public static boolean isInnerIp(String ip) {
		return isLocalIP(ip) || ipData.inneripList.contains(ip);
	}
	public static boolean isLocalIP(String ip) {
		return ip.contains("192.168.") || ip.equals("127.0.0.1"); // ±¾µØ
	}

}
