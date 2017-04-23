package com.gewara.untrans.impl;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import com.gewara.constant.Status;
import com.gewara.constant.sys.MongoData;
import com.gewara.constant.ticket.OpiConstant;
import com.gewara.json.MemberSign;
import com.gewara.json.MobileUpGrade;
import com.gewara.json.MovieMpiRemark;
import com.gewara.json.PlayItemMessage;
import com.gewara.json.RoomOuterRingSeat;
import com.gewara.json.ViewFilmSchedule;
import com.gewara.json.WDOrderContrast;
import com.gewara.model.movie.MoviePlayItem;
import com.gewara.model.pay.TicketOrder;
import com.gewara.mongo.MongoService;
import com.gewara.untrans.NosqlService;
import com.gewara.util.DateUtil;
import com.gewara.util.JsonUtils;
import com.gewara.util.LongitudeAndLatitude;
import com.gewara.util.ObjectId;
import com.gewara.xmlbind.ticket.WdOrder;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

@Service("nosqlService")
public class NosqlServiceImpl implements NosqlService{
	//private List<String> mongoTableList = new ArrayList<String>();
	@Autowired
	private MongoService mongoService;
	public void ensureMongoTable(){
		//TODO:限制mongodb的表是之前就建过的
	}
	@Override
	public void memberSign(Long memberid, Double pointx, Double pointy) {
		memberSign(memberid, pointx, pointy, null);
	}
	
	public void memberSignBaiDu(Long memberid, Double bpointx, Double bpointy){
		MemberSign sign = mongoService.getObject(MemberSign.class, "memberid", memberid);
		if(sign == null) sign = new MemberSign(memberid);
		sign.setBpointx(bpointx);
		sign.setBpointy(bpointy);
		mongoService.saveOrUpdateObject(sign, "memberid");
	}
	@Override
	public void memberSign(Long memberid, Double pointx, Double pointy, String address) {
		MemberSign sign = mongoService.getObject(MemberSign.class, "memberid", memberid);
		if(sign == null) sign = new MemberSign(memberid);
		sign.setPointx(pointx);
		sign.setPointy(pointy);
		if(StringUtils.isNotBlank(address)) sign.setAddress(address);
		mongoService.saveOrUpdateObject(sign, "memberid");
	}
	@Override
	public List<MemberSign> getMemberSignListByPointR(double pointx, double pointy, long r, int from, int maxnum) {
		double maxLd =  LongitudeAndLatitude.getLongitude(pointx, pointy, r);
		double minLd =  LongitudeAndLatitude.getLongitude(pointx, pointy, -r);
		double maxLa =  LongitudeAndLatitude.getLatitude(pointy, r);
		double minLa =  LongitudeAndLatitude.getLatitude(pointy, -r);
		Query query = new Query();
		query.addCriteria(new Criteria("pointx").gte(minLd).lte(maxLd));
		query.addCriteria(new Criteria("pointy").gte(minLa).lte(maxLa));
		query.addCriteria(new Criteria("signtime").gte(System.currentTimeMillis() - DateUtil.m_day));
		List<MemberSign> signList = mongoService.getObjectList(MemberSign.class, query.getQueryObject(), "signtime", false, from, maxnum);
		return signList;
	}
	@Override
	public MemberSign getMemberSign(Long memberid) {
		MemberSign sign = mongoService.getObject(MemberSign.class, "memberid", memberid);
		return sign;
	}
	@Override
	public MobileUpGrade getLastMobileUpGrade(String tag, String apptype, String appsource) {
		Map upGradeParams = new HashMap();
		upGradeParams.put("tag", tag);
		upGradeParams.put("upgradeStatus", MobileUpGrade.UPGRADE_STATUA_Y);
		upGradeParams.put("apptype", apptype);
		upGradeParams.put("appsource", appsource);
		List<MobileUpGrade> list = mongoService.getObjectList(MobileUpGrade.class, upGradeParams, "addTime", false, 0, 1);
		return list.isEmpty()?null:list.get(0);
	}
	@Override
	public MobileUpGrade getLastMobileUpGradeById(String appid) {
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("id", appid);
		List<MobileUpGrade> mug = mongoService.getObjectList(MobileUpGrade.class, params, "addTime", false, 0, 1);
		if (mug.size() > 0) return mug.get(0);
		return null;
	}
	@Override
	public void saveSurveyList(List<Map> mapList) {
		mongoService.saveOrUpdateMapList(mapList, "id", "mobile.survey", false, true);
	}
	@Override
	public List<Map> getSurveyByMemberid(Long memberid) {
		Map map = new HashMap();
		map.put("memberid", memberid+"");
		List<Map> result = mongoService.find("mobile.survey", map);
		return result;
	}
	@Override
	public PlayItemMessage getPlayItemMessage(Long memberid, String tag, Long relatedid, Date playdate, Long categoryid){
		Map params = new HashMap();
		params.put("tag", tag);
		params.put("relatedid", relatedid);
		params.put("categoryid", categoryid);
		params.put("memberid", memberid);
		params.put("playdate", playdate);
		List<PlayItemMessage> playItemList = mongoService.find(PlayItemMessage.class, params);
		if (!playItemList.isEmpty()) {
			return playItemList.get(0);
		}
		return null;
	}
	@Override
	public List<PlayItemMessage> getSendPlayItemMessageList(String tag, String status, String type, int from, int maxnum){
		Date curDate = DateUtil.getCurDate();
		DBObject queryCondition = new BasicDBObject();
		DBObject relate1 = mongoService.queryBasicDBObject("tag", "=", tag);
		DBObject relate2 = mongoService.queryBasicDBObject("mpid", "=", null);
		DBObject relate3 = mongoService.queryBasicDBObject("status", "=", status);
		DBObject relate4 = mongoService.queryBasicDBObject("type", "=", type);
		DBObject relate5 = mongoService.queryAdvancedDBObject("playdate", new String[]{">=","<="}, new Date[]{curDate,DateUtil.addDay(curDate, 30)});
		queryCondition.putAll(relate1);
		queryCondition.putAll(relate2);
		queryCondition.putAll(relate3);
		queryCondition.putAll(relate4);
		queryCondition.putAll(relate5);
		return mongoService.getObjectList(PlayItemMessage.class, queryCondition, "updatetime", true, from, maxnum);
	}
	@Override
	public PlayItemMessage addPlayItemMessage(Long memberid, String tag, Long relatedid, 
			Date playdate, Long categoryid, String mobile, String type, String msg) {
		return addPlayItemMessage(memberid, tag, relatedid, playdate, categoryid, mobile, Status.Y, type, msg);
	}
	@Override
	public PlayItemMessage addPlayItemMessage(Long memberid, String tag, Long relatedid, 
			Date playdate, Long categoryid, String mobile, String flag, String type, String msg) {
		PlayItemMessage remind = new PlayItemMessage(tag, relatedid, categoryid, DateUtil.currentTime(), mobile, type);
		remind.setId(ObjectId.uuid());
		remind.setMemberid(memberid);
		remind.setFlag(flag);
		remind.setPlaydate(playdate);
		if(StringUtils.isNotBlank(msg)) remind.setMsg(msg);
		mongoService.saveOrUpdateObject(remind, MongoData.DEFAULT_ID_NAME);
		return remind;
	}
	
	@Override
	public List<MovieMpiRemark> getMovieMpiRemarkList(Long movieid, String citycode, int maxnum) {
		DBObject queryCondition = new BasicDBObject();
		BasicDBList values = new BasicDBList();
		values.add(mongoService.queryBasicDBObject("cityCode", "=","000000"));
		values.add(mongoService.queryBasicDBObject("cityCode", "=", citycode));
		queryCondition.put("$or", values);
		queryCondition.putAll(mongoService.queryBasicDBObject("movieId", "=", movieid));
		queryCondition.putAll(mongoService.queryBasicDBObject("validTime", ">", DateUtil.format(DateUtil.currentTime(), "yyyy-MM-dd HH:mm:ss")));
		List<MovieMpiRemark> remarkList = mongoService.getObjectList(MovieMpiRemark.class, queryCondition, "validTime", true, 0, 5);
		return remarkList;
	}
	
	@Override
	public Map<String,String> getAutoSetterLimit(){
		Map map = new HashMap();
		List<Map> limitList = mongoService.getMapList(MongoData.NS_AUTO_SETTER_LIMIT);
		for(Map m : limitList){
			map.put(m.get("name").toString(), m.get("value"));
		}
		return map;
	}
	@Override
	public Map<String, String> getOuterRingSeatByRoomId(Long roomId) {
		Map params = new HashMap();
		params.put("roomId", roomId);
		List<RoomOuterRingSeat> rrsList = mongoService.getObjectList(RoomOuterRingSeat.class, params, "addTime", true,0, 1);
		Map<String,String> outerRingseatMap = new HashMap<String,String>();
		if(rrsList != null && rrsList.size() > 0){
			List<String> outerRingseatList =  new ArrayList<String>(Arrays.asList(StringUtils.split(rrsList.get(0).getOuterRingSeat(),",")));
			for(String s : outerRingseatList){
				String[] ss = StringUtils.split(s, ":");
				if(ss != null && ss.length == 2){
					outerRingseatMap.put(ss[0], ss[1]);
				}
			}
		}
		return outerRingseatMap;
	}
	@Override
	public List<Map> getBuyTicketRanking() {
		Timestamp cur = DateUtil.getLastTimeOfDay(DateUtil.getCurFullTimestamp());
		Timestamp end = DateUtil.addDay(cur,-1);
		Timestamp start = DateUtil.getBeginningTimeOfDay(end);
		Map params = new HashMap();
		params.put("weekArea",DateUtil.format(start, "yyyy-MM-dd") + "--" + DateUtil.format(end, "yyyy-MM-dd"));
		List<Map> saleMovie = mongoService.find(MongoData.NS_BUYTICKET_RANKING, params,"orderNum", true, 0, 10);
		return saleMovie;
	}
	@Override
	public ViewFilmSchedule addViewFilmSchedule(MoviePlayItem mpi, String tag,Long movieId,long memberId,String source) {
		Map params = new HashMap();
		params.put("memberId", memberId);
		if(ViewFilmSchedule.TYPE_MOVIE_FILMFEST.equals(tag)){
			params.put("movieId", movieId);
			params.put("type", tag);
		}else if(ViewFilmSchedule.TYPE_SCHEDULE_FILMFEST.equals(tag)){
			params.put("mpid", mpi.getId());
			params.put("type", tag);
		}else{
			return null;
		}
		List<ViewFilmSchedule> vs = mongoService.getObjectList(ViewFilmSchedule.class, params, "addTime", false, 0, 1);
		ViewFilmSchedule vfs = null;
		if(vs.isEmpty()){
			if(ViewFilmSchedule.TYPE_MOVIE_FILMFEST.equals(tag)){
				vfs = new ViewFilmSchedule(tag,null,movieId,memberId);
			}else{
				vfs = new ViewFilmSchedule(tag,mpi.getId(),mpi.getMovieid(),memberId);
				vfs.setPlayTime(DateUtil.formatDate(mpi.getPlaydate()));
				Map qryParams = new HashMap();
				qryParams.put("memberId", memberId);
				qryParams.put("movieId", mpi.getMovieid());
				qryParams.put("type", ViewFilmSchedule.TYPE_MOVIE_FILMFEST);
				List<ViewFilmSchedule> mvs = mongoService.getObjectList(ViewFilmSchedule.class, qryParams, "addTime", false, 0, 1);
				if(mvs.isEmpty()){
					ViewFilmSchedule mvfs = new ViewFilmSchedule(ViewFilmSchedule.TYPE_MOVIE_FILMFEST,null,mpi.getMovieid(),memberId);
					mvfs.setAddTime(DateUtil.format(DateUtil.getCurFullTimestamp(), "yyyy-MM-dd HH:mm:ss"));
					mvfs.set_id(ObjectId.uuid());
					mvfs.setSource(source);
					mongoService.addObject(mvfs, MongoData.SYSTEM_ID);
				}
			}
			vfs.setSource(source);
			vfs.setAddTime(DateUtil.format(DateUtil.getCurFullTimestamp(), "yyyy-MM-dd HH:mm:ss"));
			vfs.set_id(ObjectId.uuid());
			mongoService.addObject(vfs, MongoData.SYSTEM_ID);
		}
		return vfs;
	}
	@Override
	public List<WDOrderContrast> saveWDOrderContrast(List<TicketOrder> gewaOrderList,
			List<WdOrder> wdOrderList,Date addDate) {
		List<WDOrderContrast> list = new ArrayList<WDOrderContrast>();
		for(TicketOrder order : gewaOrderList){
			WDOrderContrast wc = new WDOrderContrast(ObjectId.uuid(),DateUtil.formatDate(addDate),order.getCinemaid(),
					order.getTradeNo(),"GEWA",JsonUtils.getJsonValueByKey(order.getDescription2(), "影票"),order.getQuantity(),
					order.getTotalcost(),JsonUtils.getJsonValueByKey(order.getDescription2(), "影片"),DateUtil.formatTimestamp(order.getPlaytime()),
					DateUtil.formatTimestamp(order.getAddtime()));
			wc.setRoomName(JsonUtils.getJsonValueByKey(order.getDescription2(), "影厅"));
			mongoService.saveOrUpdateObject(wc, MongoData.SYSTEM_ID);
			list.add(wc);
		}
		for(WdOrder w : wdOrderList){
			WDOrderContrast wc = new WDOrderContrast(ObjectId.uuid(),DateUtil.formatDate(addDate),
					w.getcId(),w.getSnid(),OpiConstant.OPEN_WD,w.getSeats(),w.getSeatNum(),w.getTicketMoney().intValue(),w.getFilmName(),
					w.getShowDate() + " " + w.getShowTime(),DateUtil.formatTimestamp(w.getOrderTime()));
			wc.setRoomName(w.getHallName());
			wc.setPayMode(w.getPayMode());
			mongoService.saveOrUpdateObject(wc, MongoData.SYSTEM_ID);
			list.add(wc);
		}
		return list;
	}
}
