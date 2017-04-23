package test;

import net.spy.memcached.AddrUtil;
import net.spy.memcached.MemcachedClient;

public class MemcachedTest {
	
	public static void main(String[] args) throws Exception {
		MemcachedClient mc = new MemcachedClient(AddrUtil.getAddresses("192.168.2.183:11211"));
      try {
           StringBuilder sb = new StringBuilder();
           for(int i = 0; i < 1400000; i++) {
                   char c = (char)('A' + (char)(Math.random() * 26.0));
                   sb.append(c);
           }
           long start = System.currentTimeMillis();
           mc.set("TEST_JAVA_VALUE", 0, sb.toString());
           System.out.println("Set took " + (System.currentTimeMillis() - start) + "ms.");

           start = System.currentTimeMillis();
           String value = (String)mc.get("TEST_JAVA_VALUE");
           System.out.println(value + "==Retrieve took " + (System.currentTimeMillis() - start) + "ms.");
      } catch(Exception e){
      	e.printStackTrace();
      }finally {
           mc.shutdown();
      }
	}
}