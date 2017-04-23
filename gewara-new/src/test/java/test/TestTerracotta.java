package test;

import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;

import org.terracotta.api.ClusteringToolkit;
import org.terracotta.api.TerracottaClient;

public class TestTerracotta {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception{
		TerracottaClient client = new TerracottaClient("192.168.2.108:9510");
		final ClusteringToolkit toolkit = client.getToolkit();
		new Thread(new Runnable(){
			@Override
			public void run() {
				testReentranceLock("thread1", toolkit);
			}
		}).start();

		new Thread(new Runnable(){
			@Override
			public void run() {
				testReentranceLock("thread2", toolkit);
			}
		}).start();


/*		Map map = toolkit.getMap("xxxxx");
		//map.put(StringUtil.getRandomString(10), new Member());
		System.out.println(map.keySet());
		map.put(StringUtil.getRandomString(10), StringUtil.getRandomString(10));
		System.out.println(map.keySet());
		map.put(StringUtil.getRandomString(10), StringUtil.getRandomString(10));
		//Object s = toolkit.getClusterInfo().getClusterTopology();
		System.out.println(BeanUtil.buildString(toolkit.getClusterInfo(), true));
*/		
	}
	public static void testLock(ClusteringToolkit toolkit) throws Exception{
		//ClusterInfo info = toolkit.getClusterInfo();
		ReadWriteLock lock = toolkit.getReadWriteLock("sssss");
		CyclicBarrier barrier = new CyclicBarrier(31);
		for(int i=0;i<30;i++){
			new ReadWorker(barrier, "work" + i, lock).start();
		}
		new ReadWorker(barrier, "work" + 31, lock).start();
		for(int i=0;i<20;i++){
			Lock lock2 = lock.writeLock();
			try{
				//lock2.lock();
				boolean locked = lock2.tryLock(5, TimeUnit.SECONDS);
				if(locked){
					System.out.println("test--------------------------------" + i);
				}else{
					System.out.println("TimeOut..........." + i);
				}
			}finally{
				lock2.unlock();
			}
			
		}
	}
	public static void testReentranceLock(String name, ClusteringToolkit toolkit){
		ReadWriteLock rlock = toolkit.getReadWriteLock("sssss");
		Lock lock = rlock.writeLock();
		lock.lock();
		System.out.println(name + ":first lock!");
		ReadWriteLock rlock2 = toolkit.getReadWriteLock("sssss");
		Lock lock2 = rlock2.writeLock();
		lock2.lock();
		System.out.println(name + ":second lock!");
		lock2.unlock();
		System.out.println(name + ":second unlock!");
		lock.unlock();
		System.out.println(name + ":first unlock!");
		
	}
	public static class ReadWorker extends Thread{
		private String name;
		private ReadWriteLock lock;
		private int count;
		private CyclicBarrier barrier;
		public ReadWorker(CyclicBarrier barrier, String name, ReadWriteLock lock){
			this.name = name;
			this.lock = lock;
			this.barrier = barrier;
		}
		public void run(){
			try {
				barrier.await();
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			} catch (BrokenBarrierException e1) {
				e1.printStackTrace();
			}
			while(true){
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				Lock lock2= lock.readLock();
				boolean locked = false;
				try{
					
					try {
						locked = lock2.tryLock(5, TimeUnit.SECONDS);
						if(locked){
							System.out.println(name + "-->" + count++);
						}else{
							System.out.println(name + "Timeout......" + count++);
						}
					} catch (InterruptedException e) {
						System.out.println(name + "InterruptedException......");
					}
				}finally{
					if(locked){
						try{
							lock2.unlock();
						}catch(Exception e){
							System.out.println(name + "UnLockError......");
						}
					}
				}
			}
		}
	}
}
