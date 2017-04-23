package com.gewara.web.action.subject.gewacup;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.ui.ModelMap;

import com.gewara.constant.Status;
import com.gewara.constant.sys.MongoData;
import com.gewara.json.gewacup.ClubInfo;
import com.gewara.json.gewacup.MiddleTable;
import com.gewara.json.gewacup.Players;
import com.gewara.model.pay.GewaOrder;
import com.gewara.mongo.MongoService;
import com.gewara.util.DateUtil;
import com.gewara.util.StringUtil;
import com.gewara.web.action.AnnotationController;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;


public abstract class BaseGewaCupController extends AnnotationController{
	@Autowired@Qualifier("mongoService")
	protected MongoService mongoService;
	//保存俱乐部信息
	protected void saveClubInfo(ClubInfo club, String yearstype){
		if(StringUtils.isBlank(yearstype)) yearstype = MongoData.GEWA_CUP_YEARS_2012;
		club.setAddtime(DateUtil.currentTime());
		club.setYearstype(yearstype);
		mongoService.saveOrUpdateObject(club, "id");
	}
	//保存参赛人信息
	protected void savePlayers(List<Players>  players, String type, String yearstype, String source){
		if(StringUtils.isBlank(yearstype)) yearstype = MongoData.GEWA_CUP_YEARS_2012;
		if(!players.isEmpty()){
			Players player = players.get(0);
			if(StringUtils.isBlank(player.getId())){
				player.setId(System.currentTimeMillis() + StringUtil.getRandomString(5));
			}else{
				DBObject queryCondition = new BasicDBObject();
				DBObject relate1 = mongoService.queryBasicDBObject("fromid", "=", player.getId());
				DBObject relate2 = mongoService.queryBasicDBObject("yearstype", "=", yearstype);
				queryCondition.putAll(relate1);
				queryCondition.putAll(relate2);
				MiddleTable temp = mongoService.getObjectList(MiddleTable.class, queryCondition).get(0);
				if(StringUtils.isNotBlank(temp.getToid()))
					mongoService.removeObjectById(Players.class, "id", temp.getToid());
				mongoService.removeObject(temp, "id");
			}
			player.setYearstype(yearstype);
			player.setAddtime(DateUtil.currentTime());
			player.setSource(source);
			MiddleTable mt = new MiddleTable();
			mt.setId(System.currentTimeMillis() + StringUtil.getRandomString(5));
			mt.setType(type);
			mt.setAddtime(new Timestamp(System.currentTimeMillis()));
			mt.setFromPlayer(player.getIdcards());
			mt.setFromid(player.getId());
			mt.setMemberid(player.getMemberid());
			mt.setClubInfoId(player.getClubInfoId());
			mt.setStatus(Status.Y_NEW);
			mt.setYearstype(yearstype);
			if(StringUtils.equals(type, MongoData.GEWA_CUP_BOY_DOUBLE) || StringUtils.equals(type, MongoData.GEWA_CUP_GIRL_DOUBLE) || StringUtils.equals(type, MongoData.GEWA_CUP_MIXED_DOUBLE)){
				Players player2 = players.get(1);
				player2.setId(System.currentTimeMillis() + StringUtil.getRandomString(5));
				player2.setYearstype(yearstype);
				player2.setAddtime(DateUtil.currentTime());
				player2.setSource(source);
				mongoService.saveOrUpdateObject(player2, "id");
				mt.setToPlayer(player2.getIdcards());
				mt.setToid(player2.getId());
			}
			mongoService.saveOrUpdateObject(player, "id");
			mongoService.saveOrUpdateObject(mt, "id");
		}
	}
	//判断身份证是否重复
	protected boolean getIdcards(String idcards, String type, String yearstype){
		DBObject queryCondition = new BasicDBObject();
		DBObject relate1 = mongoService.queryBasicDBObject("idcards", "=", idcards);
		DBObject relate2 = mongoService.queryBasicDBObject("yearstype", "=", yearstype);
		queryCondition.putAll(relate1);
		queryCondition.putAll(relate2);
		int count = mongoService.getObjectCount(Players.class, queryCondition);
		if(count == 0) return false;
		queryCondition = new BasicDBObject();
		relate1 = mongoService.queryBasicDBObject("fromPlayer", "=", idcards);
		DBObject relate3 = mongoService.queryBasicDBObject("type", "=", type);
		queryCondition.putAll(relate1);
		queryCondition.putAll(relate2);
		queryCondition.putAll(relate3);
		count = mongoService.getObjectCount(MiddleTable.class, queryCondition);
		queryCondition = new BasicDBObject();
		relate1 = mongoService.queryBasicDBObject("toPlayer", "=", idcards);
		queryCondition.putAll(relate1);
		queryCondition.putAll(relate2);
		queryCondition.putAll(relate3);
		count += mongoService.getObjectCount(MiddleTable.class, queryCondition);
		if(count > 0) return true; 
		count = getObjectCountbyPropertyList(MiddleTable.class.getCanonicalName(), idcards, new String[]{"fromPlayer","toPlayer"});
		if(count <= 1) return false;
		return true;
	}
	protected Integer getObjectCountbyPropertyList(String namespace, Object propertyvalue, String... propertyname){
		Query query = new Query();
		if(!ArrayUtils.isEmpty(propertyname)){
			List<Criteria> criteriaList = new ArrayList<Criteria>();
			for(String property : propertyname){
				criteriaList.add(new Criteria(property).is(propertyvalue));
			}
			if(!criteriaList.isEmpty()){
				Criteria criteria1 = new Criteria().orOperator(criteriaList.toArray(new Criteria[]{}));
				query.addCriteria(criteria1);
			}
		}
		int count = mongoService.find(namespace, query.getQueryObject()).size();
		return count;
	}
	protected void getClubPlayersInfo(List<ClubInfo> clubList, String yearstype, ModelMap model){
		if(StringUtils.isBlank(yearstype)) yearstype = MongoData.GEWA_CUP_YEARS_2012;
		List<Map> clubMapList = new ArrayList<Map>();
		for(ClubInfo club : clubList){
			List<Map> boysingleList = new ArrayList<Map>();
			List<Map> boydoubleList = new ArrayList<Map>();
			List<Map> girlsingleList = new ArrayList<Map>();
			List<Map> girldoubleList = new ArrayList<Map>();
			List<Map> mixeddoubleList = new ArrayList<Map>();
			Map clubMap = new HashMap();
			DBObject queryCondition = new BasicDBObject();
			DBObject relate1 = mongoService.queryBasicDBObject("clubInfoId", "=", club.getId());
			DBObject relate2 = mongoService.queryBasicDBObject("yearstype", "=", yearstype);
			queryCondition.putAll(relate1);
			queryCondition.putAll(relate2);
			List<MiddleTable> middleList = mongoService.getObjectList(MiddleTable.class, queryCondition);
			if(!middleList.isEmpty()){
				for(MiddleTable middle : middleList){
					Map playerMap = new HashMap();
					Players player = mongoService.getObject(Players.class, "id", middle.getFromid());
					playerMap.put("status", middle.getStatus());
					playerMap.put("player", player.getPlayer());
					playerMap.put("idcards", player.getIdcards());
					playerMap.put("phone", player.getPhone());
					playerMap.put("idcardslogo", player.getIdcardslogo());
					playerMap.put("sex", player.getSex());
					playerMap.put("mid", middle.getId());
					playerMap.put("addtime", middle.getAddtime());
					Players partnerplayer = new Players();
					if(StringUtils.equals(middle.getType(), MongoData.GEWA_CUP_BOY_SINGLE)){
						boysingleList.add(playerMap);
					}else if(StringUtils.equals(middle.getType(), MongoData.GEWA_CUP_GIRL_SINGLE)){
						girlsingleList.add(playerMap);
					}else if(StringUtils.equals(middle.getType(), MongoData.GEWA_CUP_BOY_DOUBLE)){
						partnerplayer = mongoService.getObject(Players.class, "id", middle.getToid());
						playerMap.put("partnerplayer", partnerplayer.getPlayer());
						playerMap.put("partneridcards", partnerplayer.getIdcards());
						playerMap.put("partnerphone", partnerplayer.getPhone());
						playerMap.put("partneridcardslogo", partnerplayer.getIdcardslogo());
						boydoubleList.add(playerMap);
					}else if(StringUtils.equals(middle.getType(), MongoData.GEWA_CUP_GIRL_DOUBLE)){
						partnerplayer = mongoService.getObject(Players.class, "id", middle.getToid());
						playerMap.put("partnerplayer", partnerplayer.getPlayer());
						playerMap.put("partneridcards", partnerplayer.getIdcards());
						playerMap.put("partnerphone", partnerplayer.getPhone());
						playerMap.put("partneridcardslogo", partnerplayer.getIdcardslogo());
						girldoubleList.add(playerMap);
					}else{
						partnerplayer = mongoService.getObject(Players.class, "id", middle.getToid());
						playerMap.put("partnerplayer", partnerplayer.getPlayer());
						playerMap.put("partneridcards", partnerplayer.getIdcards());
						playerMap.put("partnerphone", partnerplayer.getPhone());
						playerMap.put("partnersex", partnerplayer.getSex());
						playerMap.put("partneridcardslogo", partnerplayer.getIdcardslogo());
						mixeddoubleList.add(playerMap);
					}
				}
			}
			if(club.getOrderid() != null) {
				GewaOrder order = daoService.getObject(GewaOrder.class, club.getOrderid());
				clubMap.put("orderInfo", order);
			}
			clubMap.put("club", club);
			clubMap.put("boysingleList", boysingleList);
			clubMap.put("boydoubleList", boydoubleList);
			clubMap.put("girlsingleList", girlsingleList);
			clubMap.put("girldoubleList", girldoubleList);
			clubMap.put("mixeddoubleList", mixeddoubleList);
			clubMap.put("orderid", club.getOrderid());
			clubMapList.add(clubMap);
		}
		model.put("clubMapList", clubMapList);
	}
	
	protected List<Map> getPersonalPlayersInfo(List<MiddleTable> midList){
		List<Map> personalList = new ArrayList<Map>();
		for(MiddleTable middle : midList){
			Players player = mongoService.getObject(Players.class, "id", middle.getFromid());
			Map map = new HashMap();
			map.put("status", middle.getStatus());
			map.put("type", middle.getType());
			map.put("name", player.getPlayer());
			map.put("idcards", player.getIdcards());
			map.put("phone", player.getPhone());
			map.put("sex", player.getSex());
			map.put("id", player.getId());
			map.put("idcardslogo", player.getIdcardslogo());
			map.put("source", player.getSource());
			map.put("club", middle.getClubInfoId());
			map.put("mid", middle.getId());
			map.put("addtime", middle.getAddtime());
			map.put("orderid", player.getOrderid());
			map.put("iptvstatus", player.getStatus());
			if(player.getOrderid() != null && !StringUtils.equals(player.getSource(), "iptv")) {
				GewaOrder order = daoService.getObject(GewaOrder.class, player.getOrderid());
				map.put("orderInfo", order);
			}
			if(StringUtils.equals(middle.getType(), MongoData.GEWA_CUP_BOY_DOUBLE)||StringUtils.equals(middle.getType(), MongoData.GEWA_CUP_GIRL_DOUBLE)||StringUtils.equals(middle.getType(), MongoData.GEWA_CUP_MIXED_DOUBLE)){
				Players partner = mongoService.getObject(Players.class, "id", middle.getToid());
				map.put("partnerplayer", partner.getPlayer());
				map.put("partnerphone", partner.getPhone());
				map.put("partnersex", partner.getSex());
				map.put("partneridcards", partner.getIdcards());
				map.put("partneridcardslogo", partner.getIdcardslogo());
			}
			personalList.add(map);
		}
		return personalList;
	}
	protected boolean deletePlayers(String mid){
		MiddleTable mt = mongoService.getObject(MiddleTable.class, "id", mid);
		if(mt != null){
			mongoService.removeObjectById(Players.class, "id", mt.getFromid());
			if(mt.getToid() != null) mongoService.removeObjectById(Players.class, "id", mt.getToid());
			return mongoService.removeObject(mt, "id");
		}
		return false;
	}
	protected String getTime(String tag, String type){
		Map params = new HashMap();
		params.put("type", type);
		params.put("tag", tag);
		Map timeMap = mongoService.findOne(MongoData.NS_ACTIVITY_SINGLES, params);
		if(timeMap == null) return "ready";
		Date curTimestamp = DateUtil.currentTime();
		Date startTimestamp = (Date)timeMap.get("starttime");
		Date endTimestamp = (Date)timeMap.get("endtime");
		if(curTimestamp.before(startTimestamp)) return "ready";
		if(curTimestamp.after(endTimestamp)) return "over";
		return "game";
	}
}