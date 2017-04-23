package com.gewara.job.impl;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.orm.hibernate3.HibernateTemplate;

import com.gewara.Config;
import com.gewara.api.mobile.service.MobileService;
import com.gewara.command.EmailRecord;
import com.gewara.constant.AdminCityContant;
import com.gewara.constant.OpenTimeTableConstant;
import com.gewara.constant.PaymethodConstant;
import com.gewara.constant.Status;
import com.gewara.constant.TagConstant;
import com.gewara.constant.sys.CacheConstant;
import com.gewara.constant.sys.JsonDataKey;
import com.gewara.constant.sys.LogTypeConstant;
import com.gewara.constant.sys.MongoData;
import com.gewara.constant.ticket.OpiConstant;
import com.gewara.constant.ticket.OrderConstant;
import com.gewara.helper.sys.RelateClassHelper;
import com.gewara.job.EveryDayJob;
import com.gewara.job.JobService;
import com.gewara.json.CinemaIncrementalReport;
import com.gewara.json.MemberStats;
import com.gewara.json.SeeMovie;
import com.gewara.json.WDOrderContrast;
import com.gewara.json.pay.ReconciliationSettle;
import com.gewara.model.common.GewaCity;
import com.gewara.model.common.JsonData;
import com.gewara.model.movie.Cinema;
import com.gewara.model.movie.CityPrice;
import com.gewara.model.movie.Movie;
import com.gewara.model.pay.SpecialDiscount;
import com.gewara.model.pay.TicketOrder;
import com.gewara.model.sport.OpenTimeItem;
import com.gewara.model.sport.Sport2Item;
import com.gewara.model.sport.SportProfile;
import com.gewara.model.user.Member;
import com.gewara.mongo.MongoService;
import com.gewara.pay.NewPayUtil;
import com.gewara.service.DaoService;
import com.gewara.service.api.ApiSportService;
import com.gewara.service.bbs.BlogService;
import com.gewara.service.bbs.MarkService;
import com.gewara.service.bbs.UserMessageService;
import com.gewara.service.member.PointService;
import com.gewara.service.movie.MCPService;
import com.gewara.service.order.OrderQueryService;
import com.gewara.service.partner.PartnerSynchService;
import com.gewara.service.pay.GatewayService;
import com.gewara.service.sport.OpenTimeTableService;
import com.gewara.service.sport.SportService;
import com.gewara.service.ticket.MoviePriceService;
import com.gewara.service.ticket.TicketOrderService;
import com.gewara.support.ErrorCode;
import com.gewara.support.VelocityTemplate;
import com.gewara.untrans.CacheDataService;
import com.gewara.untrans.CacheService;
import com.gewara.untrans.CommentService;
import com.gewara.untrans.MailService;
import com.gewara.untrans.MemberCountService;
import com.gewara.untrans.MovieTrendsService;
import com.gewara.untrans.NosqlService;
import com.gewara.untrans.ShareService;
import com.gewara.untrans.monitor.MonitorService;
import com.gewara.untrans.monitor.MonitorService.EXCEPTION_TAG;
import com.gewara.untrans.sport.RemoteSportService;
import com.gewara.untrans.ticket.RemoteTicketService;
import com.gewara.untrans.ticket.TicketOperationService;
import com.gewara.util.BeanUtil;
import com.gewara.util.CAUtil;
import com.gewara.util.DateUtil;
import com.gewara.util.HttpResult;
import com.gewara.util.HttpUtils;
import com.gewara.util.JsonUtils;
import com.gewara.util.ObjectId;
import com.gewara.util.StringUtil;
import com.gewara.util.ValidateUtil;
import com.gewara.util.VmUtils;
import com.gewara.xmlbind.bbs.Comment;
import com.gewara.xmlbind.sport.GstOtt;
import com.gewara.xmlbind.sport.GstSportField;
import com.gewara.xmlbind.ticket.WdOrder;

public class EveryDayJobImpl extends JobService implements EveryDayJob{
	@Autowired@Qualifier("markService")
	private MarkService markService;
	public void setMarkService(MarkService markService) {
		this.markService = markService;
	}
	@Autowired@Qualifier("velocityTemplate")
	private VelocityTemplate velocityTemplate;
	public void setVelocityTemplate(VelocityTemplate velocityTemplate) {
		this.velocityTemplate = velocityTemplate;
	}
	@Autowired@Qualifier("commentService")
	private CommentService commentService;
	public void setCommentService(CommentService commentService) {
		this.commentService = commentService;
	}
	@Autowired@Qualifier("mcpService")
	private MCPService mcpService;
	public void setMcpService(MCPService mcpService) {
		this.mcpService = mcpService;
	}
	@Autowired@Qualifier("blogService")
	protected BlogService blogService;
/*	@Autowired@Qualifier("gewaMailService")
	private GewaMailService gewaMailService;

	@Autowired@Qualifier("untransService")
	private UntransService untransService;
*/	@Autowired@Qualifier("memberCountService")
	private MemberCountService memberCountService;
	@Autowired@Qualifier("cacheService")
	private CacheService cacheService;

	@Autowired@Qualifier("hibernateTemplate")
	private HibernateTemplate hibernateTemplate;
	public void setHibernateTemplate(HibernateTemplate hibernateTemplate) {
		this.hibernateTemplate = hibernateTemplate;
	}

	@Autowired@Qualifier("daoService")
	private DaoService daoService;
	public void setDaoService(DaoService daoService) {
		this.daoService = daoService;
	}

	@Autowired@Qualifier("partnerSynchService")
	private PartnerSynchService partnerSynchService;
	public void setPartnerSynchService(PartnerSynchService partnerSynchService) {
		this.partnerSynchService = partnerSynchService;
	}
	@Autowired@Qualifier("moviePriceService")
	private MoviePriceService moviePriceService;
	public void setMoviePriceService(MoviePriceService moviePriceService) {
		this.moviePriceService = moviePriceService;
	}
	
	@Autowired@Qualifier("openTimeTableService")
	private OpenTimeTableService openTimeTableService;
	@Autowired@Qualifier("remoteSportService")
	private RemoteSportService remoteSportService;
	@Autowired@Qualifier("apiSportService")
	private ApiSportService apiSportService;
	
	@Autowired@Qualifier("mobileService")
	private MobileService mobileService;
	
	@Autowired@Qualifier("orderQueryService")
	private OrderQueryService orderQueryService;
	
	@Autowired@Qualifier("config")
	private Config config;
	public void setConfig(Config config) {
		this.config = config;
	}
	@Autowired@Qualifier("shareService")
	private ShareService shareService;
	
	@Autowired@Qualifier("remoteTicketService")
	private RemoteTicketService remoteTicketService;
	
	@Autowired@Qualifier("cacheDataService")
	private CacheDataService cacheDataService;

	@Autowired@Qualifier("jdbcTemplate")
	protected JdbcTemplate jdbcTemplate;
	public void setJdbcTemplate(JdbcTemplate template){
		jdbcTemplate = template;
	}
	@Autowired@Qualifier("nosqlService")
	private NosqlService nosqlService;
	
	@Autowired@Qualifier("mongoService")
	private MongoService mongoService;
	public void setMongoService(MongoService mongoService) {
		this.mongoService = mongoService;
	}
	@Autowired@Qualifier("userMessageService")
	private UserMessageService userMessageService;
	public void setUserMessageService(UserMessageService userMessageService){
		this.userMessageService = userMessageService;
	}
	@Autowired@Qualifier("pointService")
	private PointService pointService;
	public void setPointService(PointService pointService) {
		this.pointService = pointService;
	}
	@Autowired@Qualifier("sportService")
	private SportService sportService;
	public void setSportService(SportService sportService) {
		this.sportService = sportService;
	}
	
	@Autowired@Qualifier("ticketOperationService")
	private TicketOperationService ticketOperationService;
	
	@Autowired@Qualifier("ticketOrderService")
	private TicketOrderService ticketOrderService;
	
	@Autowired@Qualifier("mailService")
	private MailService mailService;
	
	@Autowired@Qualifier("monitorService")
	private MonitorService monitorService;
	
	@Autowired
	@Qualifier("movieTrendsService")
	private MovieTrendsService movieTrendsService;	

	@Autowired@Qualifier("gatewayService")
	private GatewayService gatewayService;
	
	public void doJob() {
		try {
			updateBoughtcount();
		} catch (Exception e) {
			monitorService.logException(EXCEPTION_TAG.JOB, "updateBoughtcount", "EveryDayJobImpl", e, null);
			dbLogger.warn(StringUtil.getExceptionTrace(e, 10));
		}
		try {
			sendEmailToOverdue();
		} catch (Exception e) {
			monitorService.logException(EXCEPTION_TAG.JOB, "sendEmailToOverdue", "EveryDayJobImpl", e, null);
			dbLogger.warn(StringUtil.getExceptionTrace(e, 10));
		}
		try {
			updatePartnerMovieAndCinema();
		} catch (Exception e) {
			monitorService.logException(EXCEPTION_TAG.JOB, "updatePartnerMovieAndCinema", "EveryDayJobImpl", e, null);
			dbLogger.warn(StringUtil.getExceptionTrace(e, 10));
		}
		/*try{暂停使用
			updateSportOpenTimeItem();
		} catch (Exception e) {
			dbLogger.warn(StringUtil.getExceptionTrace(e, 10));
		}*/
		
		try{
			updateSportPrice();
		} catch (Exception e) {
			monitorService.logException(EXCEPTION_TAG.JOB, "updateSportPrice", "EveryDayJobImpl", e, null);
			dbLogger.warn(StringUtil.getExceptionTrace(e, 10));
		}
		
		try{
			addMovieReleaseProjectionTip();
		} catch (Exception e) {
			monitorService.logException(EXCEPTION_TAG.JOB, "addMovieReleaseProjectionTip", "EveryDayJobImpl", e, null);
			dbLogger.warn(StringUtil.getExceptionTrace(e, 10));
		}
		try{
			executeJSBChinaReconciliation();
		} catch (Exception e) {
			monitorService.logException(EXCEPTION_TAG.JOB, "executeJSBChinaReconciliation", "EveryDayJobImpl", e, null);
			dbLogger.warn(StringUtil.getExceptionTrace(e, 10));
		}
		try{
			updateHotCinema();
		} catch (Exception e) {
			monitorService.logException(EXCEPTION_TAG.JOB, "updateHotCinema", "EveryDayJobImpl", e, null);
			dbLogger.warn(StringUtil.getExceptionTrace(e, 10));
		}
		try{
			movieTrendsService.saveMovieTrendsCount();
		} catch (Exception e) {
			monitorService.logException(EXCEPTION_TAG.JOB, "movieTrendsService", "EveryDayJobImpl", e, null);
			dbLogger.warn(StringUtil.getExceptionTrace(e, 10));
		}
		try{
			pushMobileToGewamail();
		} catch (Exception e) {
			monitorService.logException(EXCEPTION_TAG.JOB, "pushMobileToGewamail", "EveryDayJobImpl", e, null);
			dbLogger.warn(StringUtil.getExceptionTrace(e, 10));
		}
		try{
			updateEverydayCinemaIncremental();
		} catch (Exception e) {
			monitorService.logException(EXCEPTION_TAG.JOB, "updateEverydayCinemaIncremental", "EveryDayJobImpl", e, null);
			dbLogger.warn(StringUtil.getExceptionTrace(e, 10));
		}
		try{
			getWDOrderByDate();
		} catch (Exception e) {
			monitorService.logException(EXCEPTION_TAG.JOB, "getWDOrderByDate", "EveryDayJobImpl", e, null);
			dbLogger.warn(StringUtil.getExceptionTrace(e, 10));
		}
		
		try {
			// 同步360cps订单
			ticketOrderService.syn360CPSOrderByDay(null);
		} catch (Exception e) {
			monitorService.logException(EXCEPTION_TAG.JOB, "update360CPSOrder", "EveryDayJobImpl", e, null);
			dbLogger.warn(StringUtil.getExceptionTrace(e, 10));
		}
	}
	public void updateHotCinema(){
		String[] sqlList = new String[]{"DELETE FROM WEBDATA.HOTSPOT_CINEMA",
				"insert into  WEBDATA.HOTSPOT_CINEMA(cinemaid, BUYQUANTITY) select cinemaid, count(1) from webdata.ticket_order " +
				"where addtime>sysdate - 5 and status='paid_success' and order_type='ticket' group by cinemaid having count(1)> 100 "
		};
		int[] count = jdbcTemplate.batchUpdate(sqlList);
		dbLogger.warn("removeHotCinema:" +  count[0] + ",insertHotCinema:" +  count[1]);
	}
	/**
	 * @function 查询所有场次的卖价, 设置平均价
	 * @author bob.hu
	 * @date 2011-04-26 17:20:33
	 */
	public void updateBoughtcount() {
		String hql2 = "select new map(o.movieid as omovieid, avg(o.gewaprice) as avgprice, min(o.gewaprice) as minprice, max(o.gewaprice) as maxprice) from OpenPlayItem o " + " where o.playtime>=? " + " group by o.movieid ";
		Timestamp curtime = DateUtil.getCurTruncTimestamp();
		List<Map> result2 = hibernateTemplate.find(hql2, curtime);
		// 更新剩余的平均价，最低价，最高价
		for (Map map : result2) {
			Long movieid = Long.valueOf("" + map.get("omovieid"));
			try {
				Integer avgprice = new Double("" + map.get("avgprice")).intValue();
				Integer minprice = new Double("" + map.get("minprice")).intValue();
				Integer maxprice = new Double("" + map.get("maxprice")).intValue();
				Movie movie = daoService.getObject(Movie.class, movieid);
				if (movie != null) {
					movie.setAvgprice(avgprice);
					if (minprice == null || minprice < 5) {
						minprice = 5;
					}
					if (maxprice == null || maxprice < 5) {
						maxprice = 5;
					}
					movie.setMinprice(minprice);
					movie.setMaxprice(maxprice);
					daoService.saveObject(movie);
				}
			} catch (Exception e) {
				monitorService.logException(EXCEPTION_TAG.JOB, "updateBoughtcount", "EveryDayJobImpl", e, null);
				dbLogger.warn("*************** Fire Wall! in Movie price" + movieid);
			}
		}
		updatePlacePrice();
	}

	/**
	 * 查询不同城市的电影所有场次的最高价，最低价，平均价
	 */
	public void updateCityprice(){
		String hql = "select new map(o.citycode as ocitycode,o.movieid as omovieid, avg(o.gewaprice) as avgprice, min(o.gewaprice) as minprice, max(o.gewaprice) as maxprice, count(distinct o.cinemaid) as cquantity, count(*) as quantity) from OpenPlayItem o " + " where o.playtime>=? " + " group by o.movieid, o.citycode ";
		Timestamp curtime = DateUtil.getCurTruncTimestamp();
		List<Map> result = hibernateTemplate.find(hql, curtime);
		String sql = "from CityPrice c where c.tag=? and c.citycode=? and c.relatedid=?" ;
		for(Map map : result){
			String citycode = ""+map.get("ocitycode");
			Long movieid = Long.valueOf("" + map.get("omovieid"));
			Integer avgprice = new Double("" + map.get("avgprice")).intValue();
			Integer minprice = new Double("" + map.get("minprice")).intValue();
			Integer maxprice = new Double("" + map.get("maxprice")).intValue();
			Integer cquantity = new Double("" + map.get("cquantity")).intValue();
			Integer quantity = new Double("" + map.get("quantity")).intValue();
			List<CityPrice> list = daoService.queryByRowsRange(sql, 0, 1, TagConstant.TAG_MOVIE, citycode, movieid);
			CityPrice cityprice = null;
			if(list.isEmpty()){
				cityprice = new CityPrice(citycode, TagConstant.TAG_MOVIE, movieid);
			}else{
				cityprice = list.get(0);
				cityprice.setUpdatetime(DateUtil.getCurFullTimestamp());
			}
			cityprice.setAvgprice(avgprice);
			if (minprice == null || minprice < 5) {
				minprice = 5;
			}
			if (maxprice == null || maxprice < 5) {
				maxprice = 5;
			}
			if(cquantity == null){
				cquantity = 0;
			}
			if(quantity == null){
				quantity = 0;
			}
			cityprice.setMinprice(minprice);
			cityprice.setMaxprice(maxprice);
			cityprice.setCquantity(cquantity);
			cityprice.setQuantity(quantity);
			daoService.saveObject(cityprice);
		}
		dbLogger.warn("更新：" + result.size());
	}
	
	// 7天邮箱/手机短信 检测兑换券到期
	public void sendEmailToOverdue() {
/*		dbLogger.warn("检测兑换券到期: start *******");
		Timestamp curtime = DateUtil.getCurTruncTimestamp();
		Timestamp time1 = DateUtil.addDay(curtime, 7);
		Timestamp time2 = DateUtil.addDay(time1, 1);
		String hql = "select new map(e.possessor as memberid, count(*) as count)from ElecCard e where e.possessor is not null and e.status=? and e.possessor is not null and "
				+ "((e.endtime is null and e.ebatch.timeto > ? and e.ebatch.timeto <= ?) or (e.endtime >? and e.endtime<= ?)) group by e.possessor having count(*) > 0";
		List<Map> cardList = hibernateTemplate.find(hql, ElecCardConstant.STATUS_SOLD, time1, time2, time1, time2);
		dbLogger.warn("*** 记录数 = " + cardList.size() + " *******" + DateUtil.formatDate(time1) + " -- " + DateUtil.formatDate(time2));
		Member member = null;
		
		int mobilecount = 0, emailcount = 0;
		for (Map cardMap : cardList) {
			Long memberid = new Long("" + cardMap.get("memberid"));
			String count = "" + cardMap.get("count");
			member = daoService.getObject(Member.class, memberid);
			if (member != null) {
				if (StringUtils.isNotBlank(member.getMobile())) { // 发短信
					String msg = "温馨提醒：亲爱的格瓦拉用户，你有" + count + "张票券7天后过期，请尽快使用，以免过期失效。";
					SMSRecord sms = new SMSRecord(memberid + "_" + count, member.getMobile(), msg, DateUtil.addHour(curtime, 8), DateUtil.addHour(curtime, 10), SmsConstant.SMSTYPE_CO);
					untransService.addMessage(sms);
					mobilecount ++;
				} else { // 发邮件
					if(StringUtils.isNotBlank(member.getEmail())){
						gewaMailService.sendCardWarnEmail(member.getNickname(), member.getEmail(), count);
						emailcount ++;
					}
				}
			}
		}
		dbLogger.warn("成功发送 Mobile:" + mobilecount + ", Email:" + emailcount);*/
	}

	public void updatePartnerMovieAndCinema() {
		String strdate = DateUtil.formatDate(new Date());
		strdate = StringUtil.md5(strdate);
		Map<String, String> params = new HashMap<String, String>();
		params.put("check", strdate);
		HttpResult result = HttpUtils.postUrlAsString(config.getAbsPath() + "/synch/qq/cinemas.xhtml", params);
		if (StringUtils.equals(result.getResponse(), "success"))
			dbLogger.warn("synch qq cinema success");
		result = HttpUtils.postUrlAsString(config.getAbsPath() + "/synch/qq/movies.xhtml", params);
		if (StringUtils.equals(result.getResponse(), "success"))
			dbLogger.warn("synch qq movie success");
	}

	private List<String> clazzListNonRealtime = Arrays.asList("movie", "cinema", "drama", "theatre", "sport");
	private List<String> clazzListRealtime = Arrays.asList("gewacommend", "picture", "sportservice", "gewaquestion", "video", "diary", "dramastar", "news");
	// 每20分钟更新一次点击量
	public void updateClicktimes() {
		String msg = "更新";
		for (String clazzName : clazzListNonRealtime) {
			try{
				Class clazz = RelateClassHelper.getRelateClazz(clazzName);
				int count = doUpdateClicktimes(clazz, false);
				msg += clazzName + ":" + count;
			}catch(Exception e){
				monitorService.logException(EXCEPTION_TAG.JOB, "updateClicktimes", "EveryDayJobImpl", e, null);
				dbLogger.warn("", e);
			}
		}
		for (String clazzName : clazzListRealtime) {
			try{
				Class clazz = RelateClassHelper.getRelateClazz(clazzName);
				int count = doUpdateClicktimes(clazz, true);
				msg += clazzName + ":" + count;
			}catch(Exception e){
				monitorService.logException(EXCEPTION_TAG.JOB, "updateClicktimes", "EveryDayJobImpl", e, null);
				dbLogger.warn("", e);
			}
		}
		dbLogger.warn(msg);
	}

	private int doUpdateClicktimes(Class clazz, boolean forceUpdate) {
		String clazzName = clazz.getSimpleName();
		String ids = cacheDataService.getAndSetIdsFromCachePool(clazz, null);
		//int idcount = StringUtils.split(ids, ",").length;
		cacheDataService.cleanIdsFromCachePool(clazz);
		String uncleans = ",";
		int totalcount = 0;
		Map<Long, Integer> updateMap = new HashMap<Long, Integer>();
		if (StringUtils.isNotBlank(ids)) {
			List<Long> idList = BeanUtil.getIdList(ids, ",");
			for (Long id : idList) {
				try {
					String key = clazz.getCanonicalName() + "." + id;
					Integer count = (Integer) cacheService.get(CacheConstant.REGION_TWOHOUR, key);
					if (count == null) continue; // 不存在了
					totalcount += count;
					if (count > 10 || forceUpdate) {
						updateMap.put(id, count);
					} else {
						uncleans += id + ",";
					}
				} catch (Exception e) {
					dbLogger.error("**" + "〖" + clazzName + ":" + id + "Exception〗", e);
				}
			}
			if (uncleans.length() > 1) {
				cacheService.set(CacheConstant.REGION_ONEDAY, clazz.getCanonicalName(), uncleans);
			}
			for (Long id : updateMap.keySet()) {
				int count = updateMap.get(id);
				cacheDataService.cleanClazzKeyCount(clazz, id);
				daoService.addPropertyNum(clazz, id, "clickedtimes", count);
			}
		}
		return totalcount;
	}

	public void updateMarkCount() {
		Timestamp endtime = DateUtil.getCurFullTimestamp();
		JsonData data = daoService.getObject(JsonData.class, TagConstant.TAG_MOVIE + JsonDataKey.KEY_MARKDATA);
		if(data != null) {
			if(DateUtil.getDiffDay(endtime, data.getValidtime()) >= 0) {
				markService.updateAvgMarkTimes(TagConstant.TAG_MOVIE, DateUtil.addDay(endtime, -30), endtime);
			}
		}else {
			markService.updateAvgMarkTimes(TagConstant.TAG_MOVIE, DateUtil.addDay(endtime, -30), endtime);
		}
	}
	public void writeChinapayTransFile() {
		partnerSynchService.writeChinapayTransFile();
	}

	public void addPointStats() {
		Timestamp curTimestamp = DateUtil.getCurTruncTimestamp();
		pointService.addPointStats(curTimestamp);
		dbLogger.warn("最后统计时间：" + curTimestamp);
	}
	
	/**
	 * 站内信定时任务
	 */
	public void sendSysMsgAction(){
		dbLogger.warn("开始发送站内信");
		Timestamp curTime = DateUtil.getCurFullTimestamp();
		Criteria criter1 = new Criteria("sendtime").lte(curTime);
		Criteria criter2 = new Criteria("validtime").gte(curTime);
		Query query = new Query(criter1).addCriteria(criter2);
		List<Map> sysMessageMap = mongoService.find(MongoData.NS_SYSMESSAGEACTION, query.getQueryObject());
		dbLogger.warn("查询出：" + sysMessageMap.size() + "条符合条件的未发送站内信");
		int count = 0;
		for(Map map : sysMessageMap){
			String action = map.get("action").toString();
			String body = velocityTemplate.parseTemplate("mail/messageAction.vm", new HashMap(map));
			userMessageService.sendSiteMSG(Long.valueOf(map.get("tomemberid")+""), action, Long.valueOf(map.get("actionid")+""), body);
			mongoService.removeObjectList(MongoData.NS_SYSMESSAGEACTION, map);
			count++;
		}
		dbLogger.warn("共计发送：" + count + "条站内信");
	}
	
	/**
	 * 更新影院的某一电影的最高价格，最小价格，平均价格
	 */
	@Override
	public void updatePlacePrice() {
		dbLogger.warn("更新影院的某一电影最高价格，最小价格，平均价格开始");
		String hql = "select new map(o.cinemaid as ocinemaid, o.movieid as omovieid, avg(o.gewaprice) as avgprice, min(o.gewaprice) as minprice, max(o.gewaprice) as maxprice) from OpenPlayItem o " + " where o.playtime>=? " + " group by o.cinemaid,o.movieid";
		Timestamp curtime = DateUtil.getCurTruncTimestamp();
		List<Map> result = hibernateTemplate.find(hql, curtime);
		int count = 0;
		for (Map map : result) {
			Long cinemaid = Long.valueOf("" + map.get("ocinemaid"));
			Long movieid = Long.valueOf("" + map.get("omovieid"));
			if(cinemaid != null && movieid != null){
				try {
					Integer avgprice = new Double("" + map.get("avgprice")).intValue();
					Integer minprice = new Double("" + map.get("minprice")).intValue();
					Integer maxprice = new Double("" + map.get("maxprice")).intValue();
					moviePriceService.saveOrUpdatePlacePrice(TagConstant.TAG_CINEMA, cinemaid, TagConstant.TAG_MOVIE, movieid, avgprice, minprice, maxprice);
					count++;
				} catch (Exception e) {
					monitorService.logException(EXCEPTION_TAG.JOB, "updatePlacePrice", "EveryDayJobImpl", e, null);
					dbLogger.warn("错误cinemaid：" + cinemaid + "  错误movieid：" + movieid);
				}
			}
		}
		dbLogger.warn("更新影院的某一电影最高价格，最小价格，平均价格结束，共更新：" + count + "条");
	}
	
	//定时同步商家开放的场次
	public void updateSportOpenTimeItem(){
		dbLogger.warnWithType(LogTypeConstant.LOG_TYPE_JOB, "定时同步运动商家场次数据star...");
		String sql = "from SportProfile s where s.booking=? order by s.id";
		List<SportProfile> spList = hibernateTemplate.find(sql, SportProfile.STATUS_OPEN);
		for(SportProfile sp : spList){
			//同步项目
			ErrorCode<String> code = remoteSportService.getGstItemIdList(sp.getId());
			if(code.isSuccess()){
				String[] ids = StringUtils.split(code.getRetval(), ",");
				if(ids != null){
					for (String id : ids) {
						if(ValidateUtil.isNumber(id)){
							Long itemid = Long.parseLong(id);
							Sport2Item sport2Item = sportService.getSport2Item(sp.getId(), itemid);
							if(sport2Item == null){//保存场馆项目
								sport2Item = new Sport2Item(sp.getId(), itemid);
								dbLogger.error("运动商家场次同步场地数据保存Sport2Item(sportid:" + sp.getId() + "  itemid:" + itemid + ")");
								daoService.saveObject(sport2Item);
							}
							//同步场次
							ErrorCode<List<GstOtt>> codeResult = remoteSportService.getGstOttList(sp.getId(), itemid, null, ">=");
							if(codeResult.isSuccess()){
								ErrorCode<List<GstSportField>> fieldResultCode = remoteSportService.getGstSportFieldList(sp.getId(), itemid);
								if(fieldResultCode.isSuccess()){
									apiSportService.addSportField(fieldResultCode.getRetval());
								}else{
									dbLogger.error("运动商家场次同步场地数据错误(sportid:" + sp.getId() + "  itemid:" + itemid + "):" + code.getMsg());
								}
								List<GstOtt> ottList = codeResult.getRetval();
								for(GstOtt ott : ottList){
									//保存场次
									ErrorCode<List<OpenTimeItem>> resultCode = apiSportService.saveSportTimeTable(ott);
									if(resultCode.isSuccess()){
										List<OpenTimeItem> itemList = resultCode.getRetval();
										Map<String,List<OpenTimeItem>> itemMap = BeanUtil.groupBeanList(itemList, "itemtype");
										List<OpenTimeItem> otiList = itemMap.get(OpenTimeTableConstant.ITEM_TYPE_VIE);
										if(!CollectionUtils.isEmpty(otiList)){
											Map<String,List<OpenTimeItem>> tmpMap = BeanUtil.groupBeanList(itemList, "saleInd");
											for (String saleInd : tmpMap.keySet()) {
												List<String> msgList = new ArrayList<String>();
												openTimeTableService.refreshOpenTimeSale(tmpMap.get(saleInd), 5, msgList);
												if(!CollectionUtils.isEmpty(msgList)){
													dbLogger.error("运动商家场次同步数据成功，保存竞拍场地错误(sportid:" + sp.getId() + "  itemid:" + itemid + "):" + msgList.toString());
												}
											}
										}
									}else{
										dbLogger.error("运动商家场次同步数据成功，保存错误(" + JsonUtils.writeObjectToJson(ott) + "):" + code.getMsg());
									}
								}
							}else{
								dbLogger.error("运动商家场次同步数据错误(sportid:" + sp.getId() + "  itemid:" + itemid + "):" + code.getMsg());
							}
						}
					}
				}
			}else{
				dbLogger.error("运动商家场次项目数据错误(sportid:" + sp.getId() + "):" + code.getMsg());
			}
		}
		dbLogger.warnWithType(LogTypeConstant.LOG_TYPE_JOB, "定时同步运动商家场次数据end...");
	}
	
	public void updateSportPrice(){
		dbLogger.warnWithType(LogTypeConstant.LOG_TYPE_JOB, "更新运动场馆项目价格：start");
		String hql = "select a.sportid,a.itemid, max(a.price) as maxprice,min(a.price) as minprice,avg(a.price) as avgprice " +
				"from WEBDATA.open_timeitem a where exists (select b.recordid from WEBDATA.open_timetable b where b.recordid=a.ottid and b.playdate >? and b.status=? and b.opentime<? and b.closetime >?) " +
				"group by a.sportid,a.itemid " +
				"union all select c.sportid,c.itemid,max(c.price) as maxprice,min(c.price) as minprice, avg(c.price) as avgprice  from WEBDATA.open_timetable c where c.playdate >=? and c.status=? and c.opentime<? and c.closetime >? " +
				"and not exists(select a.recordid from WEBDATA.open_timeitem a where c.sportid=a.sportid and c.itemid=a.itemid) " +
				"group by c.sportid,c.itemid";
		Timestamp  cur = DateUtil.getCurFullTimestamp();
		Date playDate = DateUtil.getBeginningTimeOfDay(cur);
		List<Map<String, Object>> result = jdbcTemplate.queryForList(hql, playDate, Status.Y, cur, cur, playDate, Status.Y, cur, cur);
		// 更新剩余的平均价，最低价，最高价
		for (Map map : result) {
			Long sportid = Long.valueOf("" + map.get("sportid"));
			Long itemid = Long.valueOf("" + map.get("itemid"));
			try {
				Integer avgprice = new Double("" + map.get("avgprice")).intValue();
				Integer minprice = new Double("" + map.get("minprice")).intValue();
				Integer maxprice = new Double("" + map.get("maxprice")).intValue();
				sportService.updateSportItemPrice(sportid, itemid, minprice, avgprice, maxprice);
			} catch (Exception e) {
				monitorService.logException(EXCEPTION_TAG.JOB, "updateSportPrice", "EveryDayJobImpl", e, null);
				dbLogger.warn("* Fire Wall! in sport2item price: sportid" + sportid +",itemid" +itemid);
			}
		}
		dbLogger.warnWithType(LogTypeConstant.LOG_TYPE_JOB, "更新运动场馆项目价格：end");
	}
	
	/**
	 * 电影上下映任务
	 */
	public void addMovieReleaseProjectionTip(){
		Member member = daoService.getObject(Member.class, 1L);
		//电影上映提醒
		List<Movie> releaseMovieList = mcpService.getReleaseMovieList(DateUtil.getCurDate());
		for(Movie movie : releaseMovieList){
			memberCountService.updateMemberCount(0L, MemberStats.FIELD_COMMENTCOUNT, 1, true);
			Integer gmark = VmUtils.getSingleMarkStar(daoService.getObject(Movie.class, movie.getId()), "general");
			Map otherinfoMap = new HashMap();
			otherinfoMap.put("moviename", movie.getMoviename());
			otherinfoMap.put("gmark1", gmark/10);
			otherinfoMap.put("gmark2", gmark%10);
			otherinfoMap.put("release", "release");
			otherinfoMap.put("highlight", movie.getHighlight());
			String otherinfo = JsonUtils.writeObjectToJson(otherinfoMap);
			ErrorCode<Comment> ec = commentService.addMicroComment(member, TagConstant.TAG_MOVIE_RELEASE, movie.getId(), "你感兴趣的电影", movie.getLogo(), null, null, true, null, otherinfo,null,null,Config.getServerIp(), null);
			if(ec.isSuccess()){
				shareService.sendShareInfo("wala",ec.getRetval().getId(), ec.getRetval().getMemberid(), null);
			}
		}
		dbLogger.warn("用户名："+member.getNickname()+" 电影上映提醒产生的哇啦保存成功");
		//电影下映提示
		List<Map<String, Object>> movieProjectionCountList = mcpService.getMovieProjectionCount(DateUtil.addDay(DateUtil.parseDate(DateUtil.currentTimeStr(), "yyyy-MM-dd"), -21), OpiConstant.MPI_OPENSTATUS_OPEN);
		for(Map<String, Object> map : movieProjectionCountList){
			Long count = (Long)map.get("count");
			Long movieid = (Long)map.get("movieid");
			Movie movie = daoService.getObject(Movie.class, movieid);
			if(count<=20){
				Map param = new HashMap();
				param.put("memberid", member.getId());
				
				param.put("tag", TagConstant.TAG_MOVIE);
				param.put("relatedid", movieid);
				List<SeeMovie> seeOrderList = mongoService.find(SeeMovie.class, param);
				if(seeOrderList.isEmpty()){
					memberCountService.updateMemberCount(0L, MemberStats.FIELD_COMMENTCOUNT, 1, true);
					Integer gmark = VmUtils.getSingleMarkStar(daoService.getObject(Movie.class, movie.getId()), "general") ;
					Integer commentNum = commentService.getCommentCountByRelatedId(TagConstant.TAG_MOVIE,movie.getId());
					Map otherinfoMap = new HashMap();
					otherinfoMap.put("moviename", movie.getMoviename());
					otherinfoMap.put("commentNum", commentNum);
					otherinfoMap.put("downrelease", "downrelease");
					otherinfoMap.put("gmark1", gmark/10);
					otherinfoMap.put("gmark2", gmark%10);
					String otherinfo = JsonUtils.writeObjectToJson(otherinfoMap);
					ErrorCode<Comment> ec = commentService.addMicroComment(member, TagConstant.TAG_MOVIE_DOWN, movie.getId(), "你感兴趣的电影", movie.getLogo(), null, null, true, null, otherinfo,null,null,Config.getServerIp(), null);
					if(ec.isSuccess()){
						shareService.sendShareInfo("wala",ec.getRetval().getId(), ec.getRetval().getMemberid(), null);
					}
				}
			} 
		}
		dbLogger.warn("用户名："+member.getNickname()+"   电影下映提示产生的哇啦保存成功");
	}
	
	/**
	 * 下载江苏银行对账文件
	 */
	public void executeJSBChinaReconciliation() {
		boolean isSwitch = gatewayService.isSwitch(PaymethodConstant.PAYMETHOD_JSBCHINA);
		Map<String, String> paramMap = new LinkedHashMap<String, String>();
		paramMap.put("paymethod", PaymethodConstant.PAYMETHOD_JSBCHINA);
		paramMap.put("startTime",DateUtil.format(DateUtil.addDay(new Date(), -1), "yyyy-MM-dd") + " 00:00:00");
		paramMap.put("endTime", DateUtil.format(new Date(), "yyyy-MM-dd") + " 00:00:00");
		paramMap.put("stlmDate",DateUtil.format(DateUtil.addDay(new Date(), -1), "yyyy-MM-dd") + " 00:00:00");
		if(isSwitch){
			paramMap.put("gatewayCode", PaymethodConstant.PAYMETHOD_JSBCHINA);
			paramMap.put("merchantCode", "jsbChina");
		}
		String paramStr = JsonUtils.writeMapToJson(paramMap);
		String sign = CAUtil.doSign(paramStr, NewPayUtil.getMerprikey(), "utf-8", "SHA1WithRSA");
		Map<String, String> postMap = new HashMap<String, String>();
		postMap.put("merid", NewPayUtil.getMerid());
		try {
			postMap.put("params", Base64.encodeBase64String(paramStr.getBytes("UTF-8")));
			postMap.put("sign", sign);
			String downReconciliationFileUrl = NewPayUtil.getDownReconciliationFileUrl();
			if(isSwitch){
				downReconciliationFileUrl = NewPayUtil.getNewDownReconciliationFileUrl();
			}
			HttpResult code = HttpUtils.postUrlAsString(downReconciliationFileUrl, postMap);
			if(code.isSuccess()){
				String res = new String(Base64.decodeBase64(code.getResponse()), "utf-8");
				Map<String, String> returnMap = VmUtils.readJsonToMap(res);
				Map<String,String> params = VmUtils.readJsonToMap(returnMap.get("submitParams"));
				String merId= params.get("MerId");
				HttpResult downFileCode = HttpUtils.postUrlAsString(returnMap.get("downurl"), params);
				if(downFileCode.isSuccess()){
					String orderLine = downFileCode.getResponse();
					orderLine = StringUtils.replace(orderLine,"20010521" + merId,"");
					orderLine = StringUtils.replace(orderLine," ","");
					String[] orderLines = StringUtils.split(orderLine, "\r\n");
					int length = orderLines.length;
					for(int index = 1 ;index < length;index++){
						String orderStr = orderLines[index];
						String tradeNo = StringUtils.substring(orderStr, 0, 16);//订单号
						String amount = StringUtils.substring(orderStr, 16, 28);//订单金额
						String addTime = StringUtils.substring(orderStr, 28, 42);//下单时间
						String sysTraceNo = StringUtils.substring(orderStr, 45, 57);//订单系统跟踪号
						String rspCd = StringUtils.substring(orderStr, 57, 61);//响应码
						String settleDate = StringUtils.substring(orderStr, 61, 65);//清算日期
						String authID = StringUtils.substring(orderStr, 65, 71);//预授权号
						this.saveReconciliationSettle(tradeNo, amount, addTime, sysTraceNo, rspCd, settleDate, authID,PaymethodConstant.PAYMETHOD_JSBCHINA);
					}
				}else{
					dbLogger.errorWithType("order", "江苏银行对账文件下载时出错:" + downFileCode.getMsg());
				}
			}else {
				dbLogger.errorWithType("order", "江苏银行对账文件下载时请求参数出错");
			}
		} catch (Exception e) {
			monitorService.logException(EXCEPTION_TAG.JOB, "executeJSBChinaReconciliation", "EveryDayJobImpl", e, null);
			dbLogger.error("江苏银行对账文件下载", e);
		}
	}
	
	private void saveReconciliationSettle(String tradeNo,String amount,String addTime,String sysTraceNo,String rspCd,
			String settleDate,String authID,String peymethod){
		ReconciliationSettle settle = new ReconciliationSettle();
		settle.set_id(ObjectId.uuid());
		settle.setTradeNo(tradeNo);
		settle.setAmount(Integer.parseInt(amount)/100  + "");
		settle.setAuthID(authID);
		settle.setRspCd(rspCd);
		settle.setSettleDate(settleDate);
		settle.setAddTime(addTime);
		settle.setSysTraceNo(sysTraceNo);
		settle.setPayMethod(peymethod);
		mongoService.addObject(settle, MongoData.SYSTEM_ID);
	}
	
	
	/**
	 * 预加载热门座位
	 */
	public void preloadHotCinimaMPI(){
		Long lastLoadTime = (Long)cacheService.get(CacheConstant.REGION_ONEDAY, CacheConstant.KEY_HOTSPOT_CINEMA_MPI_CACHE);
		long curTime = System.currentTimeMillis();
		if(lastLoadTime != null && lastLoadTime.longValue() > (curTime - DateUtil.m_minute * 20)){
			dbLogger.warnWithType(LogTypeConstant.LOG_TYPE_JOB, "未预加载,上次预热时间:" + DateUtil.mill2Timestamp(lastLoadTime) + "该次检查时间:" + DateUtil.formatTimestamp(curTime));
			//20分钟内已经加载
			return;
		}
		
		dbLogger.warnWithType(LogTypeConstant.LOG_TYPE_JOB, "预加载热门座位cache...");
		dbLogger.warnWithType(LogTypeConstant.LOG_TYPE_JOB, "上次预热时间:" + DateUtil.mill2Timestamp(lastLoadTime) + " 该次检查时间:" + DateUtil.formatTimestamp(curTime));
		try{
			Timestamp cur = DateUtil.mill2Timestamp(curTime);
			List<SpecialDiscount> spdiscountList = hibernateTemplate.find("from SpecialDiscount where timeto >= ? order by sortnum desc", cur);
			if(spdiscountList != null && !spdiscountList.isEmpty()){
				String curDateStr = DateUtil.getCurDateStr();
				Timestamp cur_15 = DateUtil.addMinute(cur, 15);
				for(SpecialDiscount sd: spdiscountList){
					Timestamp addTime = DateUtil.parseTimestamp(curDateStr + " " + StringUtils.substring(sd.getAddtime1(), 0, 2) + ":" + StringUtils.substring(sd.getAddtime1(), 2) + ":00");
					if(StringUtils.isNotEmpty(sd.getAddweek()) /*&& sd.getAddweek().contains(DateUtil.getWeek(cur).toString())*/){
						//如果指定了星期几搞活动
						if(sd.getAddweek().contains(DateUtil.getWeek(cur).toString()) && cur_15.after(addTime) && cur.before(addTime)){
							//下单时间在当前时间+15分钟前，并且下单时间在当前时间后
							dbLogger.warnWithType(LogTypeConstant.LOG_TYPE_JOB, "预加载座位特殊优惠ID:" + sd.getId() + " addWeek:" + sd.getAddweek() + " addTime:" + DateUtil.formatTimestamp(addTime) + " curTime:" + DateUtil.formatTimestamp(cur));
							ticketOperationService.preloadHotspotPmiCache();
							cacheService.set(CacheConstant.REGION_ONEDAY, CacheConstant.KEY_HOTSPOT_CINEMA_MPI_CACHE, Long.valueOf(curTime));
							return;
						}
					}else{
						if(cur_15.after(addTime) && cur.before(addTime)){
							dbLogger.warnWithType(LogTypeConstant.LOG_TYPE_JOB, "预加载座位特殊优惠ID:" + sd.getId() + " addTime:" + DateUtil.formatTimestamp(addTime) + " curTime:" + DateUtil.formatTimestamp(cur));
							ticketOperationService.preloadHotspotPmiCache();
							cacheService.set(CacheConstant.REGION_ONEDAY, CacheConstant.KEY_HOTSPOT_CINEMA_MPI_CACHE, Long.valueOf(curTime));
							return;
						}
					}
				}
			}
		}catch (Exception e) {
			dbLogger.errorWithType(LogTypeConstant.LOG_TYPE_JOB, "预加载异常", e);
		}
	}
	public void getWDOrderByDate(){
		Timestamp addDate = DateUtil.getBeginningTimeOfDay(DateUtil.addDay(DateUtil.getCurFullTimestamp(), -1));
		ErrorCode<List<WdOrder>> result = remoteTicketService.getWDOrderList(addDate);
		if(!result.isSuccess()){
			dbLogger.warnWithType(LogTypeConstant.LOG_TYPE_JOB, "同步万达每日订单出错，同步当日：" + DateUtil.formatDate(addDate));
		}
		List<WdOrder> wdOrderList = result.getRetval();
		if(wdOrderList == null){
			wdOrderList = new ArrayList<WdOrder>();
		}
		List<WDOrderContrast> list = nosqlService.saveWDOrderContrast(ticketOrderService.wdOrderContrast(addDate, wdOrderList),wdOrderList, addDate);
		if(!list.isEmpty()){
			Map map = new HashMap();
			List<Cinema> cinemaList = daoService.getObjectList(Cinema.class,BeanUtil.getBeanPropertyList(list, Long.class,"cinemaId", true));
			map.put("cinemaMap", BeanUtil.beanListToMap(cinemaList, "id"));
			map.put("cinemaList", cinemaList);
			map.put("contrastList", list);
			String body = velocityTemplate.parseTemplate("mail/wdOrderContrastDetail.vm", map);
			mailService.sendEmail(EmailRecord.SENDER_GEWARA, "万达订单出现差异",body, "ge.biao@gewara.com,bin.liu@gewara.com,jelly.nee@gewara.com,chenlu.li@gewara.com,xuelai.zhang@gewara.com,jacker.cheng@gewara.com,ping.wu@gewara.com,hongyun.li@gewara.com");
		}
	}
	/**
	 * 推送手机号到gewamail系统
	 * 只推送前一天的所有订单数据
	 */
	public void pushMobileToGewamail(){
		try{
			Timestamp startTime = DateUtil.getBeginTimestamp(DateUtil.addDay(DateUtil.currentTime(), -1));
			Timestamp endTime = DateUtil.getBeginTimestamp(DateUtil.currentTime());
			String hql = "select distinct mobile from GewaOrder where addtime >= ? and addtime < ? and status like ?";
			List<String> moblieList = hibernateTemplate.find(hql, startTime, endTime, OrderConstant.STATUS_PAID + "%");
			if(!moblieList.isEmpty()){
				mobileService.saveMobiles(moblieList);
				dbLogger.warnWithType(LogTypeConstant.LOG_TYPE_JOB, "共推送手机数：" + moblieList.size());
			}
		}catch(Exception e){
			monitorService.logException(EXCEPTION_TAG.JOB, "pushMobileToGewamail", "EveryDayJobImpl", e, null);
			dbLogger.errorWithType(LogTypeConstant.LOG_TYPE_JOB, "推送手机号异常", e);
		}
	}
	
	/**
	 * 统计影院每天的购票数，点击数、收藏数、哇啦数、购票数
	 */
	public void updateEverydayCinemaIncremental(){
		Map<String, List<GewaCity>> paoMap = AdminCityContant.proMap;
		Timestamp cur = DateUtil.getBeginTimestamp(new Date());
		Timestamp yesterday = DateUtil.addDay(cur, -1);
		int clickedtimes = 0;
		int collectedtimes = 0;
		int walaCount = 0;
		int remoteWalaTimes = 0;
		List<Map> mapList = markService.getMarkRelatedidByAddtime("cinema",yesterday, DateUtil.getLastTimeOfDay(yesterday));
		Map<String,Integer> markMap = new HashMap<String,Integer>();
		if(!VmUtils.isEmptyList(mapList)){
			for(Map map : mapList){
				markMap.put(map.get("relatedid") + "_" + map.get("flag"),Integer.parseInt(map.get("times").toString()));
			}
		}
		for(String key : paoMap.keySet()){
			for(GewaCity city : paoMap.get(key)){
				DetachedCriteria query=DetachedCriteria.forClass(Cinema.class);
				query.setProjection(Projections.property("id"));
				query.add(Restrictions.eq("citycode", city.getCitycode()));
				List<Long> cinemaidList = hibernateTemplate.findByCriteria(query);
				for(Long id : cinemaidList){
					Cinema cinema = this.daoService.getObject(Cinema.class, id);
					CinemaIncrementalReport report = new CinemaIncrementalReport(DateUtil.format(yesterday,"yyyy年MM月dd日"),
							cinema.getId(),cinema.getCitycode(),key,cinema.getName());
					if(remoteWalaTimes < 3){
						walaCount = commentService.getCommentCount("cinema",cinema.getId(),null,null,"Y_NEW",yesterday,DateUtil.getLastTimeOfDay(yesterday));
						if(walaCount == 0){
							remoteWalaTimes++;//调用发现3次超过0的，停止调用远程哇啦，防止wala挂的，一直调用
						}
					}else{
						walaCount = 0;
					}
					CinemaIncrementalReport r = mongoService.getObject(CinemaIncrementalReport.class, MongoData.SYSTEM_ID, DateUtil.format(DateUtil.addDay(cur, -2),"yyyy-MM-dd") + "_" + cinema.getId());
					if(r != null){
						clickedtimes = cinema.getClickedtimes() == null ? 0 : cinema.getClickedtimes() - r.getClickedtimes();
						collectedtimes = cinema.getCollectedtimes() == null ? 0 : cinema.getCollectedtimes() - r.getCollectedtimes();
					}else{
						clickedtimes = cinema.getClickedtimes() == null ? 0 : cinema.getClickedtimes();
						collectedtimes = cinema.getCollectedtimes() == null ? 0 : cinema.getCollectedtimes();
					}
					report.setClickedtimes(clickedtimes);
					report.setCollectedtimes(collectedtimes);
					report.setWalaCount(walaCount);
					report.setTicketCount(orderQueryService.getTicketOrderCountByCinema(cinema.getId(), yesterday, DateUtil.getLastTimeOfDay(yesterday),OrderConstant.STATUS_PAID_SUCCESS));
					report.setBuyMarkCount(markMap.get(cinema.getId() + "_" + Status.Y) == null ? 0 : markMap.get(cinema.getId() + "_" + Status.Y));
					report.setNotBuyMarkCount(markMap.get(cinema.getId() + "_" + Status.N) == null ? 0 : markMap.get(cinema.getId() + "_" + Status.N));
					report.set_id(DateUtil.format(yesterday,"yyyy-MM-dd") + "_" + cinema.getId());
					mongoService.saveOrUpdateObject(report,  MongoData.SYSTEM_ID);
				}
			}
		}
	}
	/**
	 * 更新格瓦拉购票排行榜
	 */
	@Override
	public void updateEveryWeekTicketOrder(){
		Timestamp cur = DateUtil.getLastTimeOfDay(DateUtil.getCurFullTimestamp());
		Timestamp end = DateUtil.addDay(cur, -1);
		Timestamp start = DateUtil.getBeginningTimeOfDay(end);
		DetachedCriteria query = DetachedCriteria.forClass(TicketOrder.class);
		query.add(Restrictions.eq("status", OrderConstant.STATUS_PAID_SUCCESS));
		query.add(Restrictions.ge("addtime", start));
		query.add(Restrictions.le("addtime", end));
		query.setProjection(Projections.projectionList().add(Projections.groupProperty("movieid"),"movieId")
				.add(Projections.count("movieid"), "buyCount"));
		query.addOrder(Order.desc("buyCount"));
		query.setResultTransformer(DetachedCriteria.ALIAS_TO_ENTITY_MAP);
 		List<Map> maps = hibernateTemplate.findByCriteria(query, 0, 10);
 		int order = 1;
 		for(Map map : maps){
 			Map ticketCount = null;
 			Map params = new HashMap();
 			params.put("movieId", map.get("movieId"));
 			params.put("weekArea",DateUtil.format(start, "yyyy-MM-dd") + "--" + DateUtil.format(end, "yyyy-MM-dd"));
 			ticketCount = mongoService.findOne(MongoData.NS_BUYTICKET_RANKING, params);
 			if(ticketCount == null){
 				ticketCount = new HashMap();
 				ticketCount.put(MongoData.SYSTEM_ID, ObjectId.uuid());
 				ticketCount.put("weekArea",DateUtil.format(start, "yyyy-MM-dd") + "--" + DateUtil.format(end, "yyyy-MM-dd"));
 				ticketCount.put("movieId", map.get("movieId"));
 			}
 			params.put("weekArea",DateUtil.format(DateUtil.addDay(start,-1), "yyyy-MM-dd") + "--" + DateUtil.format(DateUtil.addDay(start,-1), "yyyy-MM-dd"));
 			Map ticketMap = mongoService.findOne(MongoData.NS_BUYTICKET_RANKING, params);
 			if(ticketMap != null){
 				Integer orderNum = (Integer)ticketMap.get("orderNum");
 				if(orderNum > order){
 					ticketCount.put("orderRelatively", "rise");
 				}else if(orderNum < order){
 					ticketCount.put("orderRelatively", "drop");
 				}else{
 					ticketCount.put("orderRelatively", "");
 				}
 			}else{
 				ticketCount.put("orderRelatively", "rise");
 			}
 			ticketCount.put("buyCount", map.get("buyCount"));
 			ticketCount.put("orderNum", order);
 			mongoService.saveOrUpdateMap(ticketCount, MongoData.SYSTEM_ID, MongoData.NS_BUYTICKET_RANKING);
 			order++;
 		}
	}
}