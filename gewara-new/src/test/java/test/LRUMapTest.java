package test;

import java.util.Map;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;

import org.apache.commons.collections.map.LRUMap;

import com.gewara.util.StringUtil;

/**
 * @author gebiao(ge.biao@gewara.com)
 * @since Jul 3, 2012 4:20:59 PM
 */
public class LRUMapTest implements Runnable{
	private int hit = 0;
	private Map map;
	private CyclicBarrier sb;
	private CountDownLatch cdl;
	private String name;
	public LRUMapTest(String name, Map map, CyclicBarrier sb, CountDownLatch cdl){
		this.name = name;
		this.map = map;
		this.sb = sb;
		this.cdl = cdl;
	}
	public static void main(String[] args){
		CyclicBarrier sb = new CyclicBarrier(5);
		CountDownLatch cdl = new CountDownLatch(5);
		Map newMap = new LRUMap(100000);//µ±MapÌî³äÂúÊ±£¬³öÏÖËÀËø£¡£¡£¡£¡£¡£¡
		new Thread(new LRUMapTest("a--", newMap, sb, cdl)).start();
		new Thread(new LRUMapTest("b--", newMap, sb, cdl)).start();
		new Thread(new LRUMapTest("c--", newMap, sb, cdl)).start();
		new Thread(new LRUMapTest("d--", newMap, sb, cdl)).start();
		new Thread(new LRUMapTest("e--", newMap, sb, cdl)).start();
		long cur = System.currentTimeMillis();
		try {
			cdl.await();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		System.out.println(System.currentTimeMillis()-cur);//----60047,62860,60343
	}

	@Override
	public void run() {
		try {
			sb.await();
		} catch (InterruptedException e) {
		} catch (BrokenBarrierException e) {
		}
		for(int i=0;i< 10000000;i++){
			String key = StringUtil.getRandomString(2) + "yyxxxxxxxxx" + (i%32);
			map.put(key, key + i);
			key = StringUtil.getRandomString(2) + "yyxxxxxxxxx" + (i%32);
			if(map.get(key)!=null) hit ++;
			if(i%50000==0){
				System.out.println(name + i + ", hit:" + hit);
			}
		}
		System.out.println(name+" over, " + map.size() + " get hit:" + hit);
		cdl.countDown();
	}
}
