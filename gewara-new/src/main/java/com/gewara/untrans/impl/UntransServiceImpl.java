package com.gewara.untrans.impl;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.gewara.Config;
import com.gewara.constant.SmsConstant;
import com.gewara.constant.TagConstant;
import com.gewara.constant.sys.CacheConstant;
import com.gewara.constant.sys.MongoData;
import com.gewara.json.OrderWarn;
import com.gewara.json.SeeDrama;
import com.gewara.json.SeeMovie;
import com.gewara.json.SeeOrder;
import com.gewara.json.SeeSport;
import com.gewara.model.drama.DramaOrder;
import com.gewara.model.pay.GewaOrder;
import com.gewara.model.pay.SMSRecord;
import com.gewara.model.pay.SportOrder;
import com.gewara.model.pay.TicketOrder;
import com.gewara.model.ticket.OpenPlayItem;
import com.gewara.model.ticket.SellSeat;
import com.gewara.mongo.MongoService;
import com.gewara.service.DaoService;
import com.gewara.service.MessageService;
import com.gewara.service.drama.DramaService;
import com.gewara.service.drama.TheatreService;
import com.gewara.service.movie.MCPService;
import com.gewara.service.sport.OpenTimeTableService;
import com.gewara.service.ticket.OpenPlayService;
import com.gewara.service.ticket.TicketOrderService;
import com.gewara.support.ErrorCode;
import com.gewara.untrans.CacheService;
import com.gewara.untrans.UntransService;
import com.gewara.untrans.activity.SynchActivityService;
import com.gewara.untrans.mobile.MobileService;
import com.gewara.util.BeanUtil;
import com.gewara.util.DateUtil;
import com.gewara.util.GewaLogger;
import com.gewara.util.LoggerUtils;
import com.gewara.util.ObjectId;
import com.gewara.util.StringUtil;
import com.gewara.web.action.drama.SearchTheatreCommand;
import com.gewara.xmlbind.activity.RemoteActivity;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

@Service("untransService")
public class UntransServiceImpl implements UntransService{
	public static final Map<String, String> flagMap = new HashMap<String, String>();
	private final transient GewaLogger dbLogger = LoggerUtils.getLogger(getClass(), Config.getServerIp(), Config.SYSTEMID);
	@Autowired@Qualifier("openPlayService")
	private OpenPlayService openPlayService;

	@Autowired@Qualifier("mcpService")
	private MCPService mcpService;
	
	@Autowired@Qualifier("dramaService")
	private DramaService dramaService;
	
	@Autowired@Qualifier("theatreService")
	private TheatreService theatreService;

	@Autowired@Qualifier("synchActivityService")
	private SynchActivityService synchActivityService;
	@Autowired@Qualifier("cacheService")
	private CacheService cacheService;
	@Autowired@Qualifier("openTimeTableService")
	private OpenTimeTableService openTimeTableService;
	@Autowired@Qualifier("ticketOrderService")
	private TicketOrderService ticketOrderService;
	public void setTicketOrderService(TicketOrderService ticketOrderService) {
		this.ticketOrderService = ticketOrderService;
	}
	@Autowired@Qualifier("masMobileService")
	private MobileService masMobileService;
	public void setMaxMobileService(MobileService masMobileService) {
		this.masMobileService = masMobileService;
	}

	@Autowired@Qualifier("gewaMailMobileService")
	private MobileService gewaMailMobileService;
	public void setGewaMailMobileService(MobileService gewaMailMobileService) {
		this.gewaMailMobileService = gewaMailMobileService;
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
	@Autowired@Qualifier("mongoService")
	private MongoService mongoService;
	public void setMongoService(MongoService mongoService) {
		this.mongoService = mongoService;
	}
	@Override
	public void saveOrderWarn(GewaOrder order, String paymethod, Integer alipaid) {
		OrderWarn warn = new OrderWarn(order, paymethod, alipaid);
		try {
			mongoService.addObject(warn, "id");
		} catch (Exception e) {
			dbLogger.error("", e);
		}
	}
	
	@Override
	public void saveSeeCount(GewaOrder order, Timestamp playtime) {
		Long memberid = order.getMemberid();
		String tradeNo = order.getTradeNo();
		Date paidDate = new Date(order.getPaidtime().getTime());
		Long sid = System.currentTimeMillis();
		try {
			Date playDate = new Date(playtime.getTime());
			if (order instanceof TicketOrder) {
				TicketOrder ticketOrder = (TicketOrder) order;
				List<SeeMovie> seeList = new ArrayList();
				SeeMovie seeorder = new SeeMovie(ticketOrder.getMovieid(), TagConstant.TAG_MOVIE, memberid, tradeNo, paidDate, playDate);
				seeorder.setId(sid + StringUtil.getRandomString(5));
				SeeMovie seeorder1 = new SeeMovie(ticketOrder.getCinemaid(), TagConstant.TAG_CINEMA, memberid, tradeNo, paidDate, playDate);
				seeorder1.setId((sid + 1) + StringUtil.getRandomString(5));
				seeList.add(seeorder);
				seeList.add(seeorder1);
				mongoService.addObjectList(seeList, MongoData.DEFAULT_ID_NAME);
			} else if (order instanceof DramaOrder) {
				DramaOrder dramaOrder = (DramaOrder) order;
				List<SeeDrama> seeList = new ArrayList();
				SeeDrama seeorder = new SeeDrama(dramaOrder.getDramaid(), TagConstant.TAG_DRAMA, memberid, tradeNo, paidDate, playDate);
				seeorder.setId(sid + StringUtil.getRandomString(5));
				SeeDrama seeorder1 = new SeeDrama(dramaOrder.getTheatreid(), TagConstant.TAG_THEATRE, memberid, tradeNo, paidDate, playDate);
				seeorder1.setId((sid + 1) + StringUtil.getRandomString(5));
				seeList.add(seeorder);
				seeList.add(seeorder1);
				mongoService.addObjectList(seeList, MongoData.DEFAULT_ID_NAME);
			} else if (order instanceof SportOrder) {
				SportOrder sportOrder = (SportOrder) order;
				List<SeeSport> seeList = new ArrayList();
				SeeSport seeorder = new SeeSport(sportOrder.getItemid(), TagConstant.TAG_SPORTITEM, memberid, tradeNo, paidDate, playDate);
				seeorder.setId(sid + StringUtil.getRandomString(5));
				SeeSport seeorder1 = new SeeSport(sportOrder.getSportid(), TagConstant.TAG_SPORT, memberid, tradeNo, paidDate, playDate);
				seeorder1.setId((sid + 1) + StringUtil.getRandomString(5));
				seeList.add(seeorder);
				seeList.add(seeorder1);
				mongoService.addObjectList(seeList, MongoData.DEFAULT_ID_NAME);
			}
		} catch (Exception e) {
			dbLogger.error("", e);
		}
	}
	@Override
	public List<Map> getPayMemberListByTagAndId(String tag, Long id, int first, int maxnum) {
		Map params = new HashMap();
		if(id != null)params.put("relatedid", id);
		params.put("tag", tag);
		List<? extends SeeOrder> seeList = new ArrayList<SeeOrder>();
		if(StringUtils.equals(tag, TagConstant.TAG_MOVIE) || StringUtils.equals(tag, TagConstant.TAG_CINEMA)) {
			seeList = mongoService.getObjectList(SeeMovie.class, params, "paidtime", false, first, maxnum);
		}else if(StringUtils.equals(tag, TagConstant.TAG_SPORT) || StringUtils.equals(tag, TagConstant.TAG_SPORTITEM)) {
			seeList = mongoService.getObjectList(SeeSport.class, params, "paidtime", false, first, maxnum);
		}else if(StringUtils.equals(tag, TagConstant.TAG_DRAMA) || StringUtils.equals(tag, TagConstant.TAG_THEATRE)) {
			seeList = mongoService.getObjectList(SeeDrama.class, params, "paidtime", false, first, maxnum);
		}
		List<Map> payMemberList = BeanUtil.getBeanMapList(seeList, "memberid", "tradeNo", "paidtime", "relatedid");
		return payMemberList;
	}
	
	@Override
	public int countPayMemberListByTagAndId(String tag, Long id) {
		Map params = new HashMap();
		if(id != null)params.put("relatedid", id);
		params.put("tag", tag);
		int seeCount = 0;
		if(StringUtils.equals(tag, TagConstant.TAG_MOVIE) || StringUtils.equals(tag, TagConstant.TAG_CINEMA)) {
			seeCount = mongoService.getObjectCount(SeeMovie.class, params);
		}else if(StringUtils.equals(tag, TagConstant.TAG_SPORT) || StringUtils.equals(tag, TagConstant.TAG_SPORTITEM)) {
			seeCount = mongoService.getObjectCount(SeeSport.class, params);
		}else if(StringUtils.equals(tag, TagConstant.TAG_DRAMA) || StringUtils.equals(tag, TagConstant.TAG_THEATRE)) {
			seeCount = mongoService.getObjectCount(SeeDrama.class, params);
		}
		return seeCount;
	}
	
	@Override
	public boolean isPlayMemberByTagAndId(Long memberid, String tag, Long id){
		return isPlayMemberByTagAndId(memberid, null, tag, id);
	}
	
	@Override
	public boolean isPlayMemberByTagAndId(Long memberid, String tradeno, String tag, Long id){
		return isPlayMemberByTagAndId(memberid, tradeno, tag, id, null, null);
	}
	@Override
	public boolean isPlayMemberByTagAndId(Long memberid, String tradeno, String tag, Long id, Date startDate, Date endDate){
		DBObject queryCondition = new BasicDBObject();
		DBObject relate1 = mongoService.queryBasicDBObject("memberid", "=", memberid);
		DBObject relate2 = mongoService.queryBasicDBObject("relatedid", "=", id);
		queryCondition.putAll(relate1);
		queryCondition.putAll(relate2);
		if(startDate != null && endDate != null){
			DBObject relate3 = mongoService.queryAdvancedDBObject("paidtime", new String[]{">", "<"}, new Date[]{startDate, endDate});
			queryCondition.putAll(relate3);
		}
		if(StringUtils.isNotBlank(tradeno)){
			DBObject relate4 = mongoService.queryBasicDBObject("tradeNo", "=", tradeno);
			queryCondition.putAll(relate4);
		}
		int count = 0;
		if(StringUtils.equals(tag, TagConstant.TAG_MOVIE) || StringUtils.equals(tag, TagConstant.TAG_CINEMA)) {
			count = mongoService.getObjectCount(SeeMovie.class, queryCondition);
		}else if(StringUtils.equals(tag, TagConstant.TAG_SPORT) || StringUtils.equals(tag, TagConstant.TAG_SPORTITEM)) {
			count = mongoService.getObjectCount(SeeSport.class, queryCondition);
		}else if(StringUtils.equals(tag, TagConstant.TAG_DRAMA) || StringUtils.equals(tag, TagConstant.TAG_THEATRE)) {
			count = mongoService.getObjectCount(SeeDrama.class, queryCondition);
		}
		if(count > 0) return true;
		return false;
	}
	
	@Override
	public ErrorCode sendMsgAtServer(SMSRecord sms, boolean resend) {
		String channel = messageService.getSmsChannel(sms.getContact(), sms.getSmstype());
		return sendMsgAtServer(sms, channel, resend);
	}
	private String[] telePre = new String[]{"133", "153", "180", "181", "189"};
	@Override
	public ErrorCode sendMsgAtServer(SMSRecord sms, String channel, boolean resend) {
		if (!StringUtils.startsWith(Config.getServerIp(), "172.22.")) {
			dbLogger.warn("非远程服务器，忽略!" + Config.getServerIp());
			return ErrorCode.getFailure("非远程服务器，忽略!" + Config.getServerIp());
		}
		if(StringUtils.startsWithAny(sms.getContact(), telePre) && !SmsConstant.SMSTYPE_10M.equals(sms.getSmstype())){
			addTeleSms(sms.getContact(), sms.getContent());
		}
		SMSRecord send = sms;
		if (sms.getSendnum() > 0) {
			if (!resend){
				return null;
			}
			sms.setStatus(SmsConstant.STATUS_D + sms.getStatus());
			daoService.saveObject(sms);
			send = new SMSRecord("");
			send.copyFrom(sms);
		}
		send.addSendnum();
		send.setSendtime(new Timestamp(System.currentTimeMillis()));
		if (send.getStatus().startsWith(SmsConstant.STATUS_D)){
			return ErrorCode.getFailure("废弃的短信不能再次发送！");
		}
		send.setChannel(channel);
		try {
			daoService.saveObject(send);
		} catch (Exception e) {
			return ErrorCode.getFailure(e.getClass() + e.getMessage());
		}
		try {
			ErrorCode code = null;
			if (MobileService.CHANNEL_MAS.equals(channel)) {
				code = masMobileService.sendMessage(send);
			}else {
				code = gewaMailMobileService.sendMessage(send);
			}
			if (code != null) {
				if(code.isSuccess()) {
					send.setStatus(SmsConstant.STATUS_Y_TRANS);
				} else {
					send.setStatus(SmsConstant.STATUS_N_SENDERROR);
					dbLogger.warn(BeanUtil.buildString(send, false) + ":" + code.getMsg());
				}
			}
		} catch (Exception e) {
			send.setStatus(SmsConstant.STATUS_N_SENDERROR);
			daoService.saveObject(send);
			dbLogger.warn(BeanUtil.buildString(send, false), e);
			return ErrorCode.getFailure(e.getClass() + e.getMessage());
		}
		daoService.saveObject(send);
		return ErrorCode.SUCCESS;
	}
	@Override
	public void addTeleSms(String mobile, String content){
		Map<String, String> row = new HashMap<String, String>();
		row.put("_id", ObjectId.uuid());
		row.put("mobile", mobile);
		row.put("content", content);
		row.put("addtime", DateUtil.getCurFullTimestampStr());
		row.put("querycount", "0");
		mongoService.saveOrUpdateMap(row, "_id", MongoData.NS_TELE_MOBILE);
	}
	@Override
	public ErrorCode reSendOrderMsg(GewaOrder order) {
		if (order instanceof TicketOrder) {
			OpenPlayItem opi = daoService.getObjectByUkey(OpenPlayItem.class, "mpid", ((TicketOrder) order).getMpid(), true);
			String msgTemplate = messageService.getCheckpassTemplate(opi);
			List<SellSeat> seatList = ticketOrderService.getOrderSeatList(order.getId());
			ErrorCode<String> msg = messageService.getCheckpassMsg(msgTemplate, ((TicketOrder) order), seatList, opi);
			if (msg.isSuccess()) {
				String result = msg.getRetval();
				SMSRecord sms = messageService.addManualMsg(order.getMemberid(), order.getMobile(), result, null);
				if (sms != null) {
					ErrorCode code = sendMsgAtServer(sms, true);
					return code;
				}
			}
		}
		return ErrorCode.getFailure("");
	}
	@Override
	public Integer getIndexKeyNumber(String key,  String citycode){
		String ckey = CacheConstant.buildKey("index_key_data_statistical_", key, citycode);
		Integer count = (Integer) cacheService.get(CacheConstant.REGION_TENMIN, ckey);
		if(count==null){
			count = statisticalIndexCount(key, citycode);
			if(count!=null){
				cacheService.set(CacheConstant.REGION_TENMIN, ckey, count);
			}
		}
		return count;
	}
	private Integer statisticalIndexCount(String key, String citycode){
		Integer count = 0;
		if(StringUtils.equals("ticketCinemaCount", key)){
			count = openPlayService.getOpiCinemaidList(citycode,null).size();
		}else if(StringUtils.equals("hotMovieCount", key)){
			count = mcpService.getCurMovieList(citycode).size();
		}else if(StringUtils.equals("ticketMovieCount", key)){
			count = openPlayService.getOpiMovieidList(citycode, null).size();
		}else if(StringUtils.equals("futureMovieCount", key)){
			count = mcpService.getFutureMovieList(0, 200, null).size();
		}else if(StringUtils.equals("movieActivityCount", key)){
			Integer activityCount = synchActivityService.getActivityCount(citycode, null,RemoteActivity.TIME_CURRENT, TagConstant.TAG_CINEMA, null).getRetval();
			count = activityCount == null ? 0 : activityCount;
		}else if(StringUtils.equals("ticketDramaCount", key)){
			count = dramaService.getDramaListCount(citycode, "1", null, "clickedtimes", null, null);
		}else if(StringUtils.equals("ticketDramaPlaceCount", key)){
			SearchTheatreCommand stc = new SearchTheatreCommand();
			stc.setBooking("open");
			count = theatreService.getTheatreListBySearchComment(stc, citycode, 0, 800).size();
		}else if(StringUtils.equals("ticketSportCount", key)){
			count = openTimeTableService.getbookingSportCount(DateUtil.parseDate(DateUtil.getCurDateStr(), "yyyy-MM-dd"), citycode);
		}
		return count;
	}
	@Override
	public SMSRecord addMessage(SMSRecord record) {
		try{
			daoService.saveObject(record);
			return record;
		}catch(Exception e){
			return messageService.getSMSRecordByUkey(record.getTradeNo(), record.getContact(), record.getSmstype());
		}
	}
}
