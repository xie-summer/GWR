package com.gewara.job.impl;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.RandomUtils;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.criterion.Subqueries;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.orm.hibernate3.HibernateTemplate;

import com.gewara.Config;
import com.gewara.constant.DiaryConstant;
import com.gewara.constant.PointConstant;
import com.gewara.constant.Status;
import com.gewara.constant.TagConstant;
import com.gewara.constant.order.AddressConstant;
import com.gewara.constant.sys.CacheConstant;
import com.gewara.constant.sys.JsonDataKey;
import com.gewara.constant.sys.LogTypeConstant;
import com.gewara.constant.sys.MongoData;
import com.gewara.job.JobService;
import com.gewara.job.SendMessageJob;
import com.gewara.json.PlayItemMessage;
import com.gewara.json.SeeDrama;
import com.gewara.json.SeeMovie;
import com.gewara.json.SeeSport;
import com.gewara.model.bbs.BlogData;
import com.gewara.model.common.JsonData;
import com.gewara.model.drama.Drama;
import com.gewara.model.drama.DramaPlayItem;
import com.gewara.model.drama.OpenDramaItem;
import com.gewara.model.pay.GewaOrder;
import com.gewara.model.pay.SMSRecord;
import com.gewara.model.ticket.OpenPlayItem;
import com.gewara.mongo.MongoService;
import com.gewara.service.DaoService;
import com.gewara.service.MessageService;
import com.gewara.service.bbs.BlogService;
import com.gewara.service.drama.OpenDramaService;
import com.gewara.service.sport.OpenTimeTableService;
import com.gewara.service.ticket.OpenPlayService;
import com.gewara.support.ErrorCode;
import com.gewara.untrans.CacheService;
import com.gewara.untrans.CommentService;
import com.gewara.untrans.NosqlService;
import com.gewara.untrans.UntransService;
import com.gewara.untrans.activity.SynchActivityService;
import com.gewara.untrans.monitor.MonitorService;
import com.gewara.untrans.monitor.MonitorService.EXCEPTION_TAG;
import com.gewara.untrans.monitor.RoleTag;
import com.gewara.util.BeanUtil;
import com.gewara.util.DateUtil;
import com.gewara.util.GewaLogger;
import com.gewara.util.LoggerUtils;
import com.gewara.util.StringUtil;
import com.gewara.util.VmUtils;
import com.gewara.xmlbind.activity.CategoryCount;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

public class SendMessageJobImpl extends JobService implements SendMessageJob {
	private transient final GewaLogger log = LoggerUtils.getLogger(getClass(), Config.getServerIp(), Config.SYSTEMID);
	private static final Integer INTERVAL = 1000; 
	@Autowired@Qualifier("monitorService")
	private MonitorService monitorService;
	public void setMonitorService(MonitorService monitorService) {
		this.monitorService = monitorService;
	}
	@Autowired@Qualifier("untransService")
	private UntransService untransService;
	public void setUntransService(UntransService untransService) {
		this.untransService = untransService;
	}
	@Autowired@Qualifier("messageService")
	private MessageService messageService;
	public void setMessageService(MessageService messageService) {
		this.messageService = messageService;
	}	
	@Autowired@Qualifier("daoService")
	private DaoService daoService;
	public void setDaoService(DaoService daoService) {
		this.daoService = daoService;
	}
	@Autowired@Qualifier("hibernateTemplate")
	private HibernateTemplate hibernateTemplate;
	public void setHibernateTemplate(HibernateTemplate hibernateTemplate) {
		this.hibernateTemplate = hibernateTemplate;
	}
	@Autowired@Qualifier("mongoService")
	private MongoService mongoService;
	public void setMongoService(MongoService mongoService) {
		this.mongoService = mongoService;
	}
	@Autowired@Qualifier("cacheService")
	private CacheService cacheService;
	public void setCacheService(CacheService cacheService) {
		this.cacheService = cacheService;
	}
	@Autowired@Qualifier("openPlayService")
	private OpenPlayService openPlayService;
	public void setOpenPlayService(OpenPlayService openPlayService) {
		this.openPlayService = openPlayService;
	}
	@Autowired@Qualifier("openDramaService")
	private OpenDramaService openDramaService;
	public void setOpenDramaService(OpenDramaService openDramaService) {
		this.openDramaService = openDramaService;
	}
	
	@Autowired@Qualifier("commentService")
	CommentService commentService;
	
	@Autowired@Qualifier("synchActivityService")
	private SynchActivityService synchActivityService;
	
	@Autowired@Qualifier("nosqlService")
	private NosqlService nosqlService;
	@Autowired@Qualifier("openTimeTableService")
	private OpenTimeTableService openTimeTableService;
	
	@Autowired@Qualifier("blogService")
	private BlogService blogService;
	/**
	 * 手机短信
	 */
	@Override
	public void sendMobileMessage() {
		//1、发送短信
		Long cur = System.currentTimeMillis();
		Long endtime = cur + DateUtil.m_minute * 10;
		List<SMSRecord> sentList = new ArrayList<SMSRecord>();
		List<SMSRecord> msgList = messageService.getUnSendMessageList(40);
		JsonData jd = daoService.getObject(JsonData.class, JsonDataKey.KEY_SMSCHANNEL);
		Map<String, String> map = VmUtils.readJsonToMap(jd.getData());
		int success = 0, failure =0;
		while(msgList.size() > 0 && cur < endtime){
			for(SMSRecord sms:msgList){
				String channel = messageService.getSmsChannel(sms.getContact(), sms.getSmstype(), map);
				ErrorCode code = untransService.sendMsgAtServer(sms, channel, true);
				if(code.isSuccess()){
					success ++;
				}else{
					log.error("发送短信错误：" + code.getMsg());
					failure ++;
				}
				sentList.add(sms);
				try {
					Thread.sleep(INTERVAL);
				} catch (Exception e) {
					log.warn("", e);
				}
			}
			sentList.addAll(msgList);
			msgList = messageService.getUnSendMessageList(40);
			msgList.removeAll(sentList);
			cur = System.currentTimeMillis();
		}
		if(success>0 || failure>0) log.warn("发送短信任务：共发送成功:" + success + ",失败:" + failure + "个");
		if(RandomUtils.nextInt(10)>5) return;
		String time = DateUtil.formatTime(new Date());
		if("08:30".compareTo(time) > 0 || "22:30".compareTo(time) < 0) return;//
		//2、检查发送短信失败的记录，发送提醒邮件
		List<SMSRecord> failureList = messageService.getFailureSMSList();
		if(failureList.size() > 0){
			String content = "未成功发送的订单号：手机号——";
			for (SMSRecord sms:failureList) {
				content += sms.getTradeNo()+":"+sms.getContact()+"。";
			}
			Map model = new HashMap();
			model.put("msg", content);
			model.put("returnUrl", "admin/message/smsList.xhtml?failure=true");
			monitorService.saveSysTemplateWarn("短信发送未成功信息", "warn/msgmail.vm", model, RoleTag.kefu);
		}
		//3、检查短信未加入的队列的订单
		List<GewaOrder> orderList = messageService.getUnSendOrderList();
		if(orderList.size() > 0){
			Map model = new HashMap();
			List<String> tradeNoList = BeanUtil.getBeanPropertyList(orderList, String.class, "tradeNo", true);
			model.put("msg", "下面的订单没有发送短信：" + StringUtils.join(tradeNoList, ","));
			model.put("returnUrl", "admin/message/unSmsRecordOrderList.xhtml");
			monitorService.saveSysTemplateWarn("订单短信未加入发送队列", "warn/msgmail.vm", model, RoleTag.kefu);
		}
	}
	
	@Override
	public void sendPlayItemMessage(){
		Date curDate = DateUtil.getCurDate();
		List<PlayItemMessage> playitemList = nosqlService.getSendPlayItemMessageList(TagConstant.TAG_CINEMA, Status.N, AddressConstant.ADDRESS_WEB, 0, 500);
		log.warn("发送场次提醒，共" + playitemList.size() + "个");
		for (PlayItemMessage playItemMessage : playitemList) {
			if(StringUtils.equals(playItemMessage.getFlag(), Status.N)){
				Timestamp starttime = new Timestamp(playItemMessage.getPlaydate().getTime());
				Timestamp endtime = new Timestamp(DateUtil.getLastTimeOfDay(curDate).getTime());
				List<OpenPlayItem> opiList = openPlayService.getOpiList(null, playItemMessage.getRelatedid(), playItemMessage.getCategoryid(), starttime, endtime, true, 1);
				if(!opiList.isEmpty()){
					playItemMessage.setFlag(Status.Y);
				}
			}
			if (StringUtils.equals(playItemMessage.getFlag(), Status.Y)) {
				SMSRecord sms = messageService.addManualMsg(playItemMessage.getRelatedid(), playItemMessage.getMobile(), playItemMessage.getMsg(), null);
				if(sms !=null){
					untransService.sendMsgAtServer(sms, true);
					playItemMessage.setStatus(Status.Y);
				}
			}
			playItemMessage.setUpdatetime(DateUtil.currentTime());
			mongoService.saveOrUpdateObject(playItemMessage, MongoData.DEFAULT_ID_NAME);
		}
		//pushPlayItemMessage();
	}
	
	@Override
	public void sendDramaPlayItemMessage(){
		try {
			sendSportPlayItemMessage();
		} catch (Exception e) {
			monitorService.logException(EXCEPTION_TAG.JOB, "sendDramaPlayItemMessage", "SendMessageJobImpl", e, null);
			log.warn("执行运动开放场次提醒有错误:" + e.getMessage());
		}
		Date curDate = DateUtil.getCurDate();
		List<PlayItemMessage> playitemList = nosqlService.getSendPlayItemMessageList(TagConstant.TAG_THEATRE, Status.N, AddressConstant.ADDRESS_WEB, 0, 500);
		log.warn("发送话剧场次提醒，共" + playitemList.size() + "个");
		for (PlayItemMessage playItemMessage : playitemList) {
			if(StringUtils.equals(playItemMessage.getFlag(), Status.N)){
				Timestamp starttime = new Timestamp(playItemMessage.getPlaydate().getTime());
				Timestamp endtime = new Timestamp(DateUtil.getLastTimeOfDay(curDate).getTime());
				List<OpenDramaItem> odiList = openDramaService.getOdiList(null, playItemMessage.getRelatedid(), playItemMessage.getCategoryid(), starttime, endtime, true);
				if(!odiList.isEmpty()) playItemMessage.setFlag(Status.Y);
			}
			if(StringUtils.equals(playItemMessage.getFlag(), Status.Y)){
				SMSRecord sms = messageService.addManualMsg(playItemMessage.getRelatedid(), playItemMessage.getMobile(), playItemMessage.getMsg(), null);
				if(sms !=null){
					untransService.sendMsgAtServer(sms, true);
					playItemMessage.setStatus(Status.Y);
				}
			}
			playItemMessage.setUpdatetime(DateUtil.currentTime());
			mongoService.saveOrUpdateObject(playItemMessage, MongoData.DEFAULT_ID_NAME);
		}
	}
	
	private void sendSportPlayItemMessage(){
		List<PlayItemMessage> playitemList = nosqlService.getSendPlayItemMessageList(TagConstant.TAG_SPORT, Status.N, AddressConstant.ADDRESS_WEB, 0, 500);
		log.warn("发送运动场次提醒，共" + playitemList.size() + "个");
		for (PlayItemMessage playItemMessage : playitemList) {
			if(StringUtils.equals(playItemMessage.getFlag(), Status.N)){
				int count = openTimeTableService.getOpenTimeTableCount(playItemMessage.getRelatedid(), playItemMessage.getCategoryid(), playItemMessage.getPlaydate(), null, null);
				if(count > 0) playItemMessage.setFlag(Status.Y);
			}
			if(StringUtils.equals(playItemMessage.getFlag(), Status.Y)){
				SMSRecord sms = messageService.addManualMsg(playItemMessage.getRelatedid(), playItemMessage.getMobile(), playItemMessage.getMsg(), null);
				if(sms !=null){
					untransService.sendMsgAtServer(sms, true);
					playItemMessage.setStatus(Status.Y);
				}
			}
			playItemMessage.setUpdatetime(DateUtil.currentTime());
			mongoService.saveOrUpdateObject(playItemMessage, MongoData.DEFAULT_ID_NAME);
		}
	}
	
	@Override
	public void updateRelateCount(){
		log.warn("开始执行批量数据查询......");
		Date curDate = DateUtil.getCurDate();
		Map<String, Integer> itemCountMap = new HashMap<String, Integer>();
		String mhql = "select new map(movieid as mid,citycode as citycode,playdate as playdate,count(distinct cinemaid) as rowcount) from MoviePlayItem where playdate>=? group by movieid,citycode,playdate";
		List<Map> itemMapList = hibernateTemplate.find(mhql, curDate);
		for (Map map : itemMapList) {
			String key = String.valueOf(map.get("mid")) + (String)map.get("citycode") + DateUtil.format((Date)map.get("playdate"), "yyyy-MM-dd");
			itemCountMap.put(key, Integer.valueOf(String.valueOf(map.get("rowcount"))));
		}
		cacheService.set(CacheConstant.REGION_HALFDAY, CacheConstant.KEY_MOVIEPLAYITEM_MOVIEID_CITYCODE_PLAYDATE_COUNT, itemCountMap);
		log.warn("查询关联电影排片数量：" + itemCountMap.size());

		Map<String/*"relatedid+category"*/, Map<String,Integer>> blogDataMap = new HashMap<String, Map<String,Integer>>();
		ErrorCode<List<CategoryCount>> code = synchActivityService.getCategoryCountList();
		if(code.isSuccess()){
			Map<String, Integer> activityCountMap = new HashMap<String, Integer>();
			for (CategoryCount cc : code.getRetval()) {
				activityCountMap.put(cc.getCategory(), cc.getCount());
			}
			cacheService.set(CacheConstant.REGION_HALFDAY, CacheConstant.KEY_ACTIVITY_COUNT, activityCountMap);
			log.warn("查询关联活动数量：" + activityCountMap.size());
		}
		
		
		String nhql = "select new map(categoryid as mid, category as category, count(categoryid) as rowcount) from News where category is not null and categoryid is not null group by category,categoryid";
		List<Map> newsCountMapList = hibernateTemplate.find(nhql);
		Map<String, Integer> newsCountMap = new HashMap<String, Integer>();
		for (Map map : newsCountMapList) {
			String mid = String.valueOf(map.get("mid"));
			String category = String.valueOf(map.get("category"));
			String key = mid + category;
			Integer count = Integer.valueOf(String.valueOf(map.get("rowcount")));
			count = (count == null ? 0 : count);
			newsCountMap.put(key, count);
			String blogKey = mid + "_" + category;
			Map<String,Integer> dataMap = blogDataMap.get(blogKey);
			if(dataMap == null){
				dataMap = new HashMap<String, Integer>();
				blogDataMap.put(blogKey, dataMap);
			}
			dataMap.put(BlogData.KEY_NEWS_NAME, count);
		}
		cacheService.set(CacheConstant.REGION_HALFDAY, CacheConstant.KEY_NEWS_COUNT, newsCountMap);
		
		log.warn("查询关联新闻数量：" + newsCountMap.size());
		String phql = "select new map(relatedid as mid, tag as tag, count(relatedid) as rowcount) from Picture where tag is not null and relatedid is not null group by tag,relatedid";
		List<Map> pictrueCountMapList = hibernateTemplate.find(phql);
		Map<String, Integer> pictrueCountMap = new HashMap<String, Integer>();
		for (Map map : pictrueCountMapList) {
			String mid = String.valueOf(map.get("mid"));
			String category = String.valueOf(map.get("tag"));
			String key = mid + category;
			Integer count = Integer.valueOf(String.valueOf(map.get("rowcount")));
			count = (count == null ? 0 : count);
			pictrueCountMap.put(key, count);
			String blogKey = mid + "_" + category;
			Map<String,Integer> dataMap = blogDataMap.get(blogKey);
			if(dataMap == null){
				dataMap = new HashMap<String, Integer>();
				blogDataMap.put(blogKey, dataMap);
			}
			dataMap.put(BlogData.KEY_PICTURE_NAME, count);
		}
		cacheService.set(CacheConstant.REGION_HALFDAY, CacheConstant.KEY_PICTURE_COUNT, pictrueCountMap);
		log.warn("查询关联照片数量：" + pictrueCountMap.size());
		
		String vhql = "select new map(relatedid as mid, tag as tag, count(relatedid) as rowcount) from Video where tag is not null and relatedid is not null group by tag,relatedid";
		List<Map> videoCountMapList = hibernateTemplate.find(vhql);
		Map<String, Integer> videoCountMap = new HashMap<String, Integer>();
		for (Map map : videoCountMapList) {
			String mid = String.valueOf(map.get("mid"));
			String category = String.valueOf(map.get("tag"));
			String key = mid + category;
			Integer count = Integer.valueOf(String.valueOf(map.get("rowcount")));
			count = (count == null ? 0 : count);
			videoCountMap.put(key, count);
			String blogKey = mid + "_" + category;
			Map<String,Integer> dataMap = blogDataMap.get(blogKey);
			if(dataMap == null){
				dataMap = new HashMap<String, Integer>();
				blogDataMap.put(blogKey, dataMap);
			}
			dataMap.put(BlogData.KEY_VIDEO_NAME, count);
		}
		cacheService.set(CacheConstant.REGION_HALFDAY, CacheConstant.KEY_VIDEO_COUNT, videoCountMap);
		log.warn("查询关联预告片数量：" + videoCountMap.size());
		
		String dhql = "select new map(categoryid as mid, category as category, count(categoryid) as rowcount) from Diary where category is not null and categoryid is not null and type=? and status like ? and status<>? group by category,categoryid";
		List<Map> diaryCountMapList = hibernateTemplate.find(dhql, DiaryConstant.DIARY_TYPE_COMMENT, Status.Y + "%", Status.Y_TREAT);
		Map<String, Integer> diaryCountMap = new HashMap<String, Integer>();
		for (Map map : diaryCountMapList) {
			String mid = String.valueOf(map.get("mid"));
			String category = String.valueOf(map.get("category"));
			String key = mid + category;
			Integer count = Integer.valueOf(String.valueOf(map.get("rowcount")));
			count = (count == null ? 0 : count);
			diaryCountMap.put(key, count);
			String blogKey = mid + "_" + category;
			Map<String,Integer> dataMap = blogDataMap.get(blogKey);
			if(dataMap == null){
				dataMap = new HashMap<String, Integer>();
				blogDataMap.put(blogKey, dataMap);
			}
			dataMap.put(BlogData.KEY_DIARY_NAME, count);
		}
		cacheService.set(CacheConstant.REGION_HALFDAY, CacheConstant.KEY_DIARY_COUNT, diaryCountMap);
		log.warn("查询关联贴子数量：" + diaryCountMap.size());
		
//		String chql = "select new map(relatedid as mid, tag as tag, count(relatedid) as rowcount) from Comment where tag is not null and relatedid is not null and status like ? and status<>? group by tag,relatedid";
//		List<Map> commentCountMapList = hibernateTemplate.find(chql, Status.Y + "%", Status.Y_TREAT);
		try{
			List<HashMap> commentCountMapList = commentService.getTaskCommentList();
			Map<String, Integer> commentCountMap = new HashMap<String, Integer>();
			for (Map map : commentCountMapList) {
				String mid = String.valueOf(map.get("mid"));
				String category = String.valueOf(map.get("tag"));
				String key = mid + category;
				Integer count = Integer.valueOf(String.valueOf(map.get("rowcount")));
				count = (count == null ? 0 : count);
				commentCountMap.put(key, count);
				String blogKey = mid + "_" + category;
				Map<String,Integer> dataMap = blogDataMap.get(blogKey);
				if(dataMap == null){
					dataMap = new HashMap<String, Integer>();
					blogDataMap.put(blogKey, dataMap);
				}
				dataMap.put(BlogData.KEY_COMMENT_NAME, count);
			}
			cacheService.set(CacheConstant.REGION_HALFDAY, CacheConstant.KEY_COMMENT_COUNT, commentCountMap);
			log.warn("查询关联哇啦数量：" + commentCountMap.size());
		}catch (Exception e) {
			monitorService.logException(EXCEPTION_TAG.JOB, "updateRelateCount", "SendMessageJobImpl", e, null);
			dbLogger.warnWithType(LogTypeConstant.LOG_TYPE_JOB, StringUtil.getExceptionTrace(e, 10));
		}
		
		String shql = "select new map(itemid as mid, playdate as playdate, count(distinct sportid) as rowcount) from OpenTimeTable where status =? and rstatus = ? and playdate>=? group by itemid,playdate";
		List<Map> sportItemMapList = hibernateTemplate.find(shql, Status.Y, Status.Y, curDate);
		Map<String, Integer> sportItemCountMap = new HashMap<String, Integer>();
		for (Map map : sportItemMapList) {
			String key = String.valueOf(map.get("mid")) + DateUtil.format((Date)map.get("playdate"), "yyyy-MM-dd");
			sportItemCountMap.put(key, Integer.valueOf(String.valueOf(map.get("rowcount"))));
		}
		cacheService.set(CacheConstant.REGION_HALFDAY, CacheConstant.KEY_SPORTITEM_ITEMID_PLAYDATE_COUNT, sportItemCountMap);
		log.warn("查询运动项目开放场馆数量：" + sportItemCountMap.size());
		
		String sporthql = "select new map(itemid as mid, count(distinct sportid) as rowcount) from Sport2Item group by itemid";
		List<Map> sportItemList = hibernateTemplate.find(sporthql);
		Map<String, Integer> sportCountMap = new HashMap<String, Integer>();
		for (Map map : sportItemList) {
			String key = String.valueOf(map.get("mid"));
			sportCountMap.put(key, Integer.valueOf(String.valueOf(map.get("rowcount"))));
		}
		cacheService.set(CacheConstant.REGION_HALFDAY, CacheConstant.KEY_SPORTITEM_ITEMID_SPORTCOUNT, sportCountMap);
		log.warn("查询运动项目场馆数量：" + sportCountMap.size());
		
		String commuhql = "select new map(smallcategoryid as mid, smallcategory as category, count(smallcategoryid) as rowcount) from Commu where smallcategoryid is not null and smallcategory is not null and status like ? and status<>? group by smallcategory,smallcategoryid";
		List<Map> commuMapList = hibernateTemplate.find(commuhql, Status.Y + "%", Status.Y_TREAT);
		Map<String, Integer> commuCountMap = new HashMap<String, Integer>();
		for (Map map : commuMapList) {
			String key = String.valueOf(map.get("mid")) + String.valueOf(map.get("category"));
			commuCountMap.put(key, Integer.valueOf(String.valueOf(map.get("rowcount"))));
		}
		cacheService.set(CacheConstant.REGION_HALFDAY, CacheConstant.KEY_COMMU_COUNT, commuCountMap);
		log.warn("查询关联圈子数量：" + commuCountMap.size());
		
		for (String keyname : blogDataMap.keySet()) {
			try{
				List<String> keyValue = Arrays.asList(StringUtils.split(keyname, "_"));
				blogService.saveOrUpdateBlogData(0L, keyValue.get(1), Long.parseLong(keyValue.get(0)), blogDataMap.get(keyname));
			}catch(Exception e){
				log.warn("", e);
			}
		}
	}
	
	@Override
	public void updateIndexDataSheet(){
		dbLogger.warn("开始执行首页数据统计查询...");
		Timestamp curtime = DateUtil.getCurTruncTimestamp();
		Timestamp queryTime = DateUtil.addDay(curtime, -7);
		Map dataMap = new HashMap();
		dataMap.put(MongoData.DEFAULT_ID_NAME, MongoData.INDEX_KEY);
		//帖子总数
		String ahql = "select count(*) from Diary d where d.addtime >= ?";
		List alldiaryCount = hibernateTemplate.find(ahql, queryTime);
		int allDcount = 0;
		if(!alldiaryCount.isEmpty()){
			allDcount = Integer.valueOf(alldiaryCount.get(0)+"");
			dbLogger.warn("帖子总数:" + allDcount);
		}
		dataMap.put(MongoData.INDEX_ALL_DIARY_COUNT, allDcount);
		//影评数
		String dhql = "select count(*) from Diary where tag is not null and (relatedid is not null or categoryid is not null) and addtime >= ?";
		List comentDiaryCount = hibernateTemplate.find(dhql, queryTime);
		int commentDcount = 0;
		if(!comentDiaryCount.isEmpty()){
			commentDcount = Integer.valueOf(comentDiaryCount.get(0) + "");
			dbLogger.warn("影评数:" + commentDcount);
		}
		dataMap.put(MongoData.INDEX_DIARY_COUNT, commentDcount);
		Date adddate = DateUtil.addDay(DateUtil.getDateFromTimestamp(queryTime), -7);
		String strDate = DateUtil.formatDate(adddate);
		DBObject dbObject = new BasicDBObject();
		DBObject addDateObject = mongoService.queryBasicDBObject("adddate", ">=", strDate);
		dbObject.putAll(addDateObject);
		DBObject tagObject = mongoService.queryBasicDBObject("tag", "=", TagConstant.TAG_MOVIE);
		dbObject.putAll(tagObject);
		//观影数
		int moviecount = mongoService.getObjectCount(SeeMovie.class, dbObject);
		dataMap.put(MongoData.INDEX_TICKET_COUNT, moviecount);
		dbLogger.warn("观影数:" + moviecount);
		dbObject = new BasicDBObject();
		dbObject.putAll(addDateObject);
		tagObject = mongoService.queryAdvancedDBObject("tag", new String[]{"="}, new String[]{TagConstant.TAG_DRAMA});
		dbObject.putAll(tagObject);
		//看话剧数
		int dramacount = mongoService.getObjectCount(SeeDrama.class, dbObject);
		dataMap.put(MongoData.INDEX_DRAMA_COUNT, dramacount);
		dbLogger.warn("话剧数:" + dramacount);
		dbObject = new BasicDBObject();
		dbObject.putAll(addDateObject);
		tagObject = mongoService.queryAdvancedDBObject("tag", new String[]{"="}, new String[]{TagConstant.TAG_SPORT});
		dbObject.putAll(tagObject);
		//运动数
		int sportcount = mongoService.getObjectCount(SeeSport.class, dbObject);
		dataMap.put(MongoData.INDEX_SPORT_COUNT, sportcount);
		dbLogger.warn("运动数:" + sportcount);
		try{
			//参加活动数
			//TODO:此处有异常
			int aCount = synchActivityService.getJoinCountByAddtime(queryTime, null);
			dbLogger.warn("参加活动数:" + aCount);
			dataMap.put(MongoData.INDEX_JOIN_ACTIVITY_COUNT, aCount);
		}catch(Exception e){
			dbLogger.warn("", e);
		}
		try{
			//领红包数
			String phql = "select count(*) from Point p where p.tag= ? and p.addtime>=?";
			List pointCount = hibernateTemplate.find(phql, PointConstant.TAG_LOGIN_ACTIVIRY, queryTime);
			int pCount = 0;
			if(!pointCount.isEmpty()){
				pCount =  Integer.valueOf(pointCount.get(0) + "");
				dbLogger.warn("领红包数:" + pCount);
			}
			dataMap.put(MongoData.INDEX_POINT_COUNT, pCount);
		}catch(Exception e){
			dbLogger.warn("", e);
		}
		mongoService.saveOrUpdateMap(dataMap, MongoData.DEFAULT_ID_NAME, MongoData.NS_INDEX_DATASHEET);
		cacheService.set(CacheConstant.REGION_TENMIN, MongoData.INDEX_KEY, dataMap);
		dbLogger.warn("执行首页数据统计结束...");
	}
	@SuppressWarnings("unused")
	@Override
	public void sendTheatreMessage() {
		DBObject queryCondition = new BasicDBObject();
		DBObject relate1 = mongoService.queryBasicDBObject("tag", "=", "theatreDraCollect");
		DBObject relate2 = mongoService.queryBasicDBObject("mpid", "=", null);
		DBObject relate3 = mongoService.queryBasicDBObject("status", "=", Status.N);
		DBObject relate4 = mongoService.queryBasicDBObject("type", "=", AddressConstant.ADDRESS_WEB);
		queryCondition.putAll(relate1);
		queryCondition.putAll(relate2);
		queryCondition.putAll(relate3);
		queryCondition.putAll(relate4);
		//TODO:move to nosqlService
		List<PlayItemMessage> playitemList = mongoService.getObjectList(PlayItemMessage.class, queryCondition, "updatetime", true, 0, 500);
		log.warn("发送场馆开发购票提醒，共" + playitemList.size() + "个");
		for (PlayItemMessage playItemMessage : playitemList) {
			Date lastSend = playItemMessage.getUpdatetime();
			Long theatreId = playItemMessage.getRelatedid();
			Date fromDate = DateUtil.getCurDate();
			DetachedCriteria query = DetachedCriteria.forClass(Drama.class,"d");
			query.add(Restrictions.le("releasedate", fromDate));
			query.add(Restrictions.gt("releasedate", lastSend));
			query.add(Restrictions.ge("enddate", fromDate)); 
			DetachedCriteria sub = DetachedCriteria.forClass(DramaPlayItem.class, "item");
			sub.add(Restrictions.gt("item.playtime", fromDate));
			sub.add(Restrictions.eq("item.theatreid", theatreId));
			sub.add(Restrictions.eqProperty("item.dramaid", "d.id"));
			sub.setProjection(Projections.property("item.id"));
			query.add(Subqueries.exists(sub));
			List<Drama> dramaList = hibernateTemplate.findByCriteria(query);
			for(Drama drama:dramaList){
				//TODO 发送短信
			}
			playItemMessage.setUpdatetime(DateUtil.currentTime());
			mongoService.saveOrUpdateObject(playItemMessage, MongoData.DEFAULT_ID_NAME);
		}
	}
	
	@Override
	public void updateDiaryEveryDay(){
		dbLogger.warn("开始每天评论数据开始start...");
		Timestamp cur = DateUtil.getCurFullTimestamp();
		Timestamp starttime = DateUtil.getBeginTimestamp(cur);
		starttime = DateUtil.addDay(starttime, -1);
		Timestamp endtime = DateUtil.getLastTimeOfDay(cur);
		try{
			List<Map> diaryCountMapList = blogService.getDiaryMapList(starttime, endtime);
			for (Map map : diaryCountMapList) {
				String mid = String.valueOf(map.get("categoryid"));
				String category = String.valueOf(map.get("category"));
				String adddate = String.valueOf(map.get("adddate"));
				Integer count = Integer.valueOf(String.valueOf(map.get("rowcount")));
				count = (count == null ? 0 : count);
				blogService.saveOrUpdateBlogDateEveryDay(0L, category, Long.parseLong(mid), TagConstant.TAG_DIARY, DateUtil.parseDate(adddate), count);
				dbLogger.warn("更新帖子数：类型：" + category +",ID: " + Long.parseLong(mid) +",日期：" + adddate + ",数量：" + count);
			}
		}catch(Exception e){
			dbLogger.warn("", e);
		}
		dbLogger.warn("开始每天评论数据开始start...");
	}
}
