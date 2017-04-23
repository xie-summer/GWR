package com.gewara.untrans.impl;

import java.util.Map;
import java.util.concurrent.BlockingQueue;

import com.gewara.Config;
import com.gewara.constant.sys.MongoData;
import com.gewara.mongo.MongoService;
import com.gewara.util.GewaLogger;
import com.gewara.util.LoggerUtils;
import com.gewara.util.ObjectId;

public class MongoWorkThread extends Thread {
	private GewaLogger dbLogger = LoggerUtils.getLogger(this.getClass(), Config.getServerIp(), Config.SYSTEMID);
	private BlockingQueue<Map> mongoQueue;
	private MongoService mongoService;
	private boolean run = true;
	
	public MongoWorkThread(String name, BlockingQueue<Map> mongoQueue, MongoService mongoService){
		super(name);
		this.mongoQueue = mongoQueue;
		this.mongoService = mongoService;
	}
	public void stopConsume(){
		this.run = false;
	}
	
	@Override
	public void run() {
		while(run){
			try{
				Map entry = mongoQueue.take();
				entry.put(MongoData.SYSTEM_ID, ObjectId.uuid());
				String namespace = ""+entry.get("namespace");
				entry.remove("namespace");	// remove namespace
				mongoService.addMap(entry, MongoData.SYSTEM_ID, namespace);
			}catch(Exception e){
				dbLogger.warn("", e);
			}
		}
	}
}
