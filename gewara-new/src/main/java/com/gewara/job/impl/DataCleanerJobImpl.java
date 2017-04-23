package com.gewara.job.impl;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.orm.hibernate3.HibernateTemplate;

import com.gewara.constant.order.ElecCardConstant;
import com.gewara.constant.sys.LogTypeConstant;
import com.gewara.constant.sys.MongoData;
import com.gewara.job.DataCleanerJob;
import com.gewara.job.JobService;
import com.gewara.json.PlayItemMessage;
import com.gewara.json.SeeMovie;
import com.gewara.json.SeeSport;
import com.gewara.json.TicketRollCall;
import com.gewara.model.movie.Movie;
import com.gewara.model.pay.ElecCardExtra;
import com.gewara.mongo.MongoService;
import com.gewara.service.DaoService;
import com.gewara.service.gewapay.ElecCardService;
import com.gewara.untrans.CommonService;
import com.gewara.untrans.monitor.MonitorService;
import com.gewara.untrans.monitor.MonitorService.EXCEPTION_TAG;
import com.gewara.untrans.ticket.TicketRollCallService;
import com.gewara.util.DateUtil;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

/**
 * @author <a href="mailto:acerge@163.com">gebiao(acerge)</a>
 * @since 2007-9-28下午02:05:17
 */
public class DataCleanerJobImpl extends JobService implements DataCleanerJob{
	@Autowired@Qualifier("commonService")
	private CommonService commonService;
	public void setCommonService(CommonService commonService) {
		this.commonService = commonService;
	}
	@Autowired@Qualifier("hibernateTemplate")
	private HibernateTemplate hibernateTemplate;
	public void setHibernateTemplate(HibernateTemplate hbt) {
		hibernateTemplate = hbt;
	}
	
	@Autowired@Qualifier("monitorService")
	private MonitorService monitorService;
	public void setMonitorService(MonitorService monitorService) {
		this.monitorService = monitorService;
	}
	@Autowired@Qualifier("jdbcTemplate")
	private JdbcTemplate jdbcTemplate;
	public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}
	@Autowired@Qualifier("mongoService")
	private MongoService mongoService;
	public void setMongoService(MongoService mongoService) {
		this.mongoService = mongoService;
	}
	@Autowired@Qualifier("elecCardService")
	private ElecCardService elecCardService;
	public void setElecCardService(ElecCardService elecCardService) {
		this.elecCardService = elecCardService;
	}

	@Autowired@Qualifier("ticketRollCallService")
	private TicketRollCallService ticketRollCallService;
	public void setTicketRollCallService(TicketRollCallService ticketRollCallService){
		this.ticketRollCallService = ticketRollCallService;
	}
	@Autowired@Qualifier("daoService")
	private DaoService daoService;
	public void setDaoService(DaoService daoService) {
		this.daoService = daoService;
	}
	/***
	 * recordid,movieid,cinemaid,roomid,language,textlanguage,playdate,price,
	 * discountprice,remark,playtime,studentprice,playroom
	 */
	public void cleanMovieList() {
		String sql[] = new String[2];
		dbLogger.warnWithType(LogTypeConstant.LOG_TYPE_JOB, "start clean MOVIELISTHIS....");
		//清除重复
		String delSql = "delete from WEBDATA.MOVIELISTHIS where playdate>sysdate-5 and recordid in (select recordid from WEBDATA.MOVIELIST)";
		int count = jdbcTemplate.update(delSql);
		dbLogger.warnWithType(LogTypeConstant.LOG_TYPE_JOB, "Clear repeate data:" + count);

		delSql = "delete from WEBDATA.JSONDATA where validtime<sysdate-1 and dkey like 'mpi%'";
		count = jdbcTemplate.update(delSql);
		dbLogger.warnWithType(LogTypeConstant.LOG_TYPE_JOB, "Clear JSONDATA:" + count);

		sql[0] = "INSERT INTO WEBDATA.MOVIELISTHIS (" +
				"SELECT * FROM WEBDATA.MOVIELIST WHERE playdate < sysdate - 1)";
		//TODO:电影节后删除
		//sql[1] = "INSERT INTO WEBDATA.FESTMPI(MPID,BATCH) SELECT RECORDID,BATCH FROM WEBDATA.MOVIELIST WHERE PLAYDATE < SYSDATE - 1 AND BATCH=120595079";
		sql[1] = "DELETE FROM WEBDATA.MOVIELIST WHERE playdate < sysdate - 1";
		int[] result = jdbcTemplate.batchUpdate(sql);
		dbLogger.warnWithType(LogTypeConstant.LOG_TYPE_JOB, "Clear history data of MOVIELIST TO MOVIELISTHIS, " + result[0]);
	}
	public void cleanJsonData(){
		String delSql = "delete from WEBDATA.HIS_DATA where VALIDTIME<sysdate-1";
		int count = jdbcTemplate.update(delSql);
		dbLogger.warnWithType(LogTypeConstant.LOG_TYPE_JOB, "Clear HisData:" + count);
	}
	public void cleanOpenSeat(){
		String sqlStr1 = "DELETE FROM WEBDATA.OPEN_SEAT O WHERE O.MPID IN (" +
				"	SELECT T.MPID FROM WEBDATA.OPEN_PLAYITEM T WHERE T.PLAYTIME < SYSDATE - 1.3 )";
		String sqlStr2 = "DELETE FROM WEBDATA.OPEN_SEAT O WHERE O.MPID IN (" +
				"	SELECT T.MPID FROM WEBDATA.OPEN_PLAYITEM T WHERE T.PLAYTIME < SYSDATE - 1 )";
		dbLogger.warnWithType(LogTypeConstant.LOG_TYPE_JOB, "Clear unused OpenPlayItem And OpenSeat...");
		int count = jdbcTemplate.update(sqlStr1);
		dbLogger.warnWithType(LogTypeConstant.LOG_TYPE_JOB, "Clear unused OpenPlayItem And OpenSeat Step1, count: " + count + " records");
		count = jdbcTemplate.update(sqlStr2);
		dbLogger.warnWithType(LogTypeConstant.LOG_TYPE_JOB, "Clear unused OpenPlayItem And OpenSeat Step2, count: " + count + " records, Complete!");
		
	}

	public void cleanCoupon(){
		//cinema
		String query = "select c.id from Cinema c where c.coupon != 'N'";
		List<Long> idList = hibernateTemplate.find(query);
		for(Long relatedid:idList){
			commonService.updateCoupon(null, "cinema", relatedid);
		}
		//sport
		query = "select c.id from Sport c where c.coupon != 'N'";
		idList = hibernateTemplate.find(query);
		for(Long relatedid:idList){
			commonService.updateCoupon(null, "sport", relatedid);
		}
	}
	public void cleanUserOperation(){
		hibernateTemplate.bulkUpdate("delete UserOperation where validtime < ? ", 
				DateUtil.addDay(new Timestamp(System.currentTimeMillis()), -1));
		hibernateTemplate.bulkUpdate("delete LastOperation where validtime < ? ", 
				DateUtil.addDay(new Timestamp(System.currentTimeMillis()), -1));
	}
	@Override
	public void cleanAll(){
		try{
			cleanMovieList();
		}catch(Exception e){
			monitorService.logException(EXCEPTION_TAG.JOB, "cleanMovieList", "DataCleanerJobImpl", e, null);
			dbLogger.error("", e);
		}
		try{
			cleanOpenSeat();
		}catch(Exception e){
			monitorService.logException(EXCEPTION_TAG.JOB, "cleanOpenSeat", "DataCleanerJobImpl", e, null);
			dbLogger.error("", e);
		}
		try{
			cleanJsonData();
		}catch(Exception e){
			monitorService.logException(EXCEPTION_TAG.JOB, "cleanJsonData", "DataCleanerJobImpl", e, null);
			dbLogger.error("", e);
		}
		try{
			cleanCoupon();
		}catch(Exception e){
			monitorService.logException(EXCEPTION_TAG.JOB, "cleanCoupon", "DataCleanerJobImpl", e, null);
			dbLogger.error("", e);
		}
			
		try{
			cleanUserOperation();
		}catch(Exception e){
			monitorService.logException(EXCEPTION_TAG.JOB, "cleanUserOperation", "DataCleanerJobImpl", e, null);
			dbLogger.error("", e);
		}
		try{
			updateReplyCount();
		}catch(Exception e){
			monitorService.logException(EXCEPTION_TAG.JOB, "updateReplyCount", "DataCleanerJobImpl", e, null);
			dbLogger.error("", e);
		}
		try{
			cleanSmsrecord();
		}catch(Exception e){
			monitorService.logException(EXCEPTION_TAG.JOB, "cleanSmsrecord", "DataCleanerJobImpl", e, null);
			dbLogger.error("", e);
		}
		try{
			cleanMemberAction();
		}catch(Exception e){
			monitorService.logException(EXCEPTION_TAG.JOB, "cleanMemberAction", "DataCleanerJobImpl", e, null);
			dbLogger.error("", e);
		}
		try{
			cleanRandomnum();
		}catch(Exception e){
			monitorService.logException(EXCEPTION_TAG.JOB, "cleanRandomnum", "DataCleanerJobImpl", e, null);
			dbLogger.error("", e);
		}

		try{
			cleanOrderResult();
		}catch(Exception e){
			monitorService.logException(EXCEPTION_TAG.JOB, "cleanOrderResult", "DataCleanerJobImpl", e, null);
			dbLogger.error("", e);
		}
		try{
			cleanOpenTimeItem();
		}catch(Exception e){
			monitorService.logException(EXCEPTION_TAG.JOB, "cleanOpenTimeItem", "DataCleanerJobImpl", e, null);
			dbLogger.error("", e);
		}
		
		try{
			cleanTicketRollCall();
		}catch (Exception e) {
			monitorService.logException(EXCEPTION_TAG.JOB, "cleanTicketRollCall", "DataCleanerJobImpl", e, null);
			dbLogger.error("", e);
		}
		
		try{
			cleanPlayItemMessage();
		}catch (Exception e) {
			monitorService.logException(EXCEPTION_TAG.JOB, "cleanPlayItemMessage", "DataCleanerJobImpl", e, null);
			dbLogger.error("", e);
		}
		try{
			updateEelecCardPossessor();
		}catch (Exception e){
			monitorService.logException(EXCEPTION_TAG.JOB, "updateEelecCardPossessor", "DataCleanerJobImpl", e, null);
			dbLogger.error("", e);
		}
		try{
			updateEelecCardStats();
		}catch (Exception e){
			monitorService.logException(EXCEPTION_TAG.JOB, "updateEelecCardStats", "DataCleanerJobImpl", e, null);
			dbLogger.error("", e);
		}
		try{
			cleanSeeMovie();
		}catch(Exception e){
			monitorService.logException(EXCEPTION_TAG.JOB, "cleanSeeMovie", "DataCleanerJobImpl", e, null);
			dbLogger.error("", e);
		}
		try{
			cleanSeeSport();
		}catch(Exception e){
			monitorService.logException(EXCEPTION_TAG.JOB, "cleanSeeSport", "DataCleanerJobImpl", e, null);
			dbLogger.error("", e);
		}
		try{
			updateRegisterId();
		}catch(Exception e){
			monitorService.logException(EXCEPTION_TAG.JOB, "updateRegisterId", "DataCleanerJobImpl", e, null);
			dbLogger.error("", e);
		}
		try{
			correctPlayTime();
		} catch (Exception e) {
			monitorService.logException(EXCEPTION_TAG.JOB, "correctPlayTime", "DataCleanerJobImpl", e, null);
			dbLogger.warn("", e);
		}

	}
	public void updateReplyCount(){
		Timestamp threeDay = DateUtil.addDay(new Timestamp(System.currentTimeMillis()), -3);
		String diarySQL = "update WEBDATA.diary d set d.replycount=(" +
				"select count(recordid) from WEBDATA.diarycomment dc where dc.diaryid=d.recordid) " +
				"where d.replytime > ? and d.replycount != (select count(recordid) from WEBDATA.diarycomment dc where dc.diaryid=d.recordid)";
		int result = jdbcTemplate.update(diarySQL, threeDay);
		dbLogger.warnWithType(LogTypeConstant.LOG_TYPE_JOB, "帖子回复校验" + result + "个");
		
		/*String qaSQL = "update WEBDATA.gewaquestion q set q.replycount=(select count(recordid) from WEBDATA.gewaanswer a where a.questionid=q.recordid) " +
				"where q.updatetime > ? and q.replycount!=(select count(recordid) from WEBDATA.gewaanswer a where a.questionid=q.recordid)";
		result = jdbcTemplate.update(qaSQL, threeDay);
		dbLogger.warnWithType(LogTypeConstant.LOG_TYPE_JOB, "知道回复校验" + result + "个");*/
	}
	public void cleanSmsrecord(){
		String date = DateUtil.format(DateUtil.addDay(new Date(), -20), "yyyyMMdd");
		String[] sql = new String[]{
			"insert into WEBDATA.smsrecord_his select * from WEBDATA.smsrecord where SENDTIME < to_date('" + date + "','yyyyMMdd') and (status='Y' or status like 'D%' or status like 'P%' or status = 'Y_IGNORE' or status='FILTER')",
			"delete from WEBDATA.smsrecord where SENDTIME < to_date('" + date + "','yyyyMMdd') and (status='Y' or status like 'D%' or status like 'P%' or status = 'Y_IGNORE' or status='FILTER')"};
		int[] result = jdbcTemplate.batchUpdate(sql);
		dbLogger.warnWithType(LogTypeConstant.LOG_TYPE_JOB, "Clear SmsRecord data: " + result[0]);
	}
	public void cleanMemberAction(){
		String[] sql = new String[]{
				"delete WEBDATA.AGENDA where START_DATE<sysdate-30",
				"delete WEBDATA.SYSTEM_MESSAGE_ACTION where addtime<sysdate-60",
				"delete WEBDATA.USER_MESSAGE where addtime<sysdate-180",
				"delete WEBDATA.USER_MESSAGE_ACTION where addtime<sysdate-180"
		};
		int[] result = jdbcTemplate.batchUpdate(sql);
		dbLogger.warnWithType(LogTypeConstant.LOG_TYPE_JOB, "Clear Agenda,SYSTEM_MESSAGE_ACTION,USER_MESSAGE,USER_MESSAGE_ACTION: " + result[0] + "," +result[1] + "," + result[2] + "," + result[3]);
	}
	public void cleanRandomnum(){
		String sql = "delete WEBDATA.randomnum where VALIDITY<sysdate - 10";
		int result = jdbcTemplate.update(sql);
		dbLogger.warnWithType(LogTypeConstant.LOG_TYPE_JOB, "Clear randomnum: " + result);
	}
	
	public void cleanOrderResult(){
		String date = DateUtil.format(DateUtil.addDay(new Date(), -60), "yyyyMMdd");
		String sql1 = "insert into WEBDATA.orderresult_his select * from WEBDATA.orderresult r where r.updatetime<to_date('" + date + "','yyyyMMdd')";
		String sql2 = "delete from WEBDATA.orderresult o where o.updatetime<to_date('" + date + "','yyyyMMdd')";
		String[] sql = new String[]{sql1, sql2};
		int[] result = jdbcTemplate.batchUpdate(sql);
		dbLogger.warnWithType(LogTypeConstant.LOG_TYPE_JOB, "Clear OrderResult data: " + result[0]);
	}
	public void cleanSeeMovie(){
		Date adddate = DateUtil.addDay(new Date(), -7);
		String strDate = DateUtil.formatDate(adddate);
		
		DBObject dbObject = new BasicDBObject();
		dbObject.put("adddate", new BasicDBObject("$lt", strDate));
		dbObject.put("tag", new BasicDBObject("$in", new String[]{"cinema", "movie"}));
		int count1 = mongoService.getCount(SeeMovie.class.getCanonicalName());
		mongoService.removeObjectList(SeeMovie.class.getCanonicalName(), dbObject);
		int count2 = mongoService.getCount(SeeMovie.class.getCanonicalName());
		dbLogger.warnWithType(LogTypeConstant.LOG_TYPE_JOB, "ClearSeeMovie:" + strDate + count1 + "--->" + count2);
	}
	public void cleanSeeSport(){
		Date adddate = DateUtil.addDay(new Date(), -7);
		String strDate = DateUtil.formatDate(adddate);
		
		DBObject dbObject = new BasicDBObject();
		dbObject.put("adddate", new BasicDBObject("$lt", strDate));
		int count1 = mongoService.getCount(SeeSport.class.getCanonicalName());
		mongoService.removeObjectList(SeeSport.class.getCanonicalName(), dbObject);
		int count2 = mongoService.getCount(SeeSport.class.getCanonicalName());
		dbLogger.warnWithType(LogTypeConstant.LOG_TYPE_JOB, "ClearSeeSport:" + strDate + count1 + "--->" + count2);
	}
	public void cleanOpenTimeItem(){
		String sql = "delete from WEBDATA.open_timeitem o where exists(select t.recordid from WEBDATA.open_timetable t where t.recordid=o.ottid and t.playdate<?)";
		int i = jdbcTemplate.update(sql, DateUtil.addDay(new Date(), -90));
		dbLogger.warnWithType(LogTypeConstant.LOG_TYPE_JOB, "Clear OpenTimeItem data: " + i);
	}
	
	public void cleanTicketRollCall(){
		Date endDate = DateUtil.getLastTimeOfDay(DateUtil.addDay(DateUtil.getCurDate(), -1));
		Query query = ticketRollCallService.getTicketRollCallQuery(null, null, null, endDate);
		int count1 = mongoService.getCount(TicketRollCall.class.getCanonicalName());
		mongoService.removeObjectList(TicketRollCall.class.getCanonicalName(), query.getQueryObject());
		int count2 = mongoService.getCount(TicketRollCall.class.getCanonicalName());
		dbLogger.warnWithType(LogTypeConstant.LOG_TYPE_JOB, "Clear TicketRollCall Data:" + DateUtil.format(endDate, "yyyy-MM-dd HH:mm:ss") + count1 + "---->" + count2);
	}
	
	public void cleanPlayItemMessage(){
		Date strDate = DateUtil.addDay(DateUtil.getCurDate(), -7);
		DBObject queryCondition = new BasicDBObject();
		DBObject relate1 = mongoService.queryAdvancedDBObject("playdate", new String[]{"<"}, new Date[]{strDate});
		queryCondition.putAll(relate1);
		int count1 = mongoService.getCount(PlayItemMessage.class.getCanonicalName());
		List<PlayItemMessage> playitemList = mongoService.getObjectList(PlayItemMessage.class, queryCondition);
		for(PlayItemMessage pim : playitemList){
			Movie movie = daoService.getObject(Movie.class, pim.getCategoryid());
			if(pim.getRelatedid() == null){
				if(pim.getPlaydate() != movie.getReleasedate()){
					pim.setPlaydate(movie.getReleasedate());
					mongoService.saveOrUpdateObject(pim, MongoData.DEFAULT_ID_NAME);
				}
			}
		}
		mongoService.removeObjectList(PlayItemMessage.class.getCanonicalName(), queryCondition);
		int count2 = mongoService.getCount(PlayItemMessage.class.getCanonicalName());
		dbLogger.warnWithType(LogTypeConstant.LOG_TYPE_JOB, "Clear PlayItemMessage data:" + DateUtil.format(strDate, "yyyy-MM-dd HH:mm:ss") + count1 + "--->" + count2);
	}
	public void updateEelecCardStats(){
		List<ElecCardExtra> batchList = elecCardService.getSubCardExtraListByStatus(ElecCardConstant.DATA_NOW);
		for(ElecCardExtra extra: batchList){
			elecCardService.updateBatchExtra(extra);
		}
	}
	/**
	 * 更新注册邀请人信息
	 */
	public void updateRegisterId() {
		String hql = "update EmailInvite e set e.registerid = (select id from Member m where m.email = e.email) where e.registerid is null and exists (select id from Member s where s.email = e.email)";
		int result = hibernateTemplate.bulkUpdate(hql);
		dbLogger.warnWithType(LogTypeConstant.LOG_TYPE_JOB, "updateRegisterId:" + result);
	}

	/**
	 * 更新使用过的券的使用者
	 */
	public void updateEelecCardPossessor(){
		String sql = "update WEBDATA.veleccard c set possessor = (select memberid from WEBDATA.ticket_order t where t.recordid=c.orderid) where c.status='U' and c.possessor is null";
		int i = jdbcTemplate.update(sql);
		dbLogger.warnWithType(LogTypeConstant.LOG_TYPE_JOB, "update ElecCard possessor: " + i);
	}
	/**
	 * 纠正TicketOrder中的Playtime
	 */
	public void correctPlayTime(){
		//只更新影票订单，其他有需要再加
		String sql = "update webdata.ticket_order t set t.playtime=(select playtime from webdata.open_playitem o where o.mpid=t.relatedid) " +
				"where t.order_type='ticket' and t.createtime > ? and exists (select playtime from webdata.open_playitem s where s.mpid=t.relatedid and s.playtime!=t.playtime)";
		int count = jdbcTemplate.update(sql, DateUtil.addDay(new Timestamp(System.currentTimeMillis()), -10));
		dbLogger.warn("correctPlayTime:" +  count);
	}

}
