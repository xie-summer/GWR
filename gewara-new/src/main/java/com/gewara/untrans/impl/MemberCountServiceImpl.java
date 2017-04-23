package com.gewara.untrans.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.gewara.Config;
import com.gewara.constant.sys.MongoData;
import com.gewara.constant.ticket.OrderConstant;
import com.gewara.json.MemberStats;
import com.gewara.mongo.MongoService;
import com.gewara.untrans.MemberCountService;
import com.gewara.util.GewaLogger;
import com.gewara.util.LoggerUtils;
import com.gewara.util.StringUtil;
@Service("memberCountService")
public class MemberCountServiceImpl implements MemberCountService{
	private final transient GewaLogger dbLogger = LoggerUtils.getLogger(getClass(), Config.getServerIp(), Config.SYSTEMID);
	@Autowired
	private MongoService mongoService;
	@Override
	public int getFansCountByMemberId(Long memberid){
		Map dataMap = mongoService.findOne(MongoData.NS_MEMBERCOUNT, MongoData.DEFAULT_ID_NAME, memberid);
		Integer fansCount = 0;
		if(dataMap!=null){
			 fansCount = (Integer)dataMap.get(MemberStats.FIELD_FANSCOUNT);
			 if(fansCount==null) fansCount = 0;
		}
		return fansCount;
	}
	@Override
	public Map getMemberCount(Long memberid) {
		//关注，哇啦，瓦丝数量
		Map dataMap = mongoService.findOne(MongoData.NS_MEMBERCOUNT, MongoData.DEFAULT_ID_NAME, memberid);
		if(dataMap==null) dataMap = new HashMap();
		if(dataMap.get(MemberStats.FIELD_COMMENTCOUNT) == null){
			dataMap.put(MemberStats.FIELD_COMMENTCOUNT, 0);
		}
		if(dataMap.get(MemberStats.FIELD_ATTENTIONCOUNT) == null) {
			dataMap.put(MemberStats.FIELD_ATTENTIONCOUNT, 0);
		}
		if(dataMap.get(MemberStats.FIELD_FANSCOUNT) == null){
			dataMap.put(MemberStats.FIELD_FANSCOUNT, 0);
		}
		return dataMap;
	}
	@Override
	public void updateMemberCount(Long memberid, String key, int value, boolean isAdd){
		try{
			Map dataMap = mongoService.findOne(MongoData.NS_MEMBERCOUNT, MongoData.DEFAULT_ID_NAME, memberid);
			if(dataMap == null ) dataMap = new HashMap();
			if(dataMap.containsKey(key)) {
				int oldValue = (Integer) dataMap.get(key);
				int newValue = 0;
				if(isAdd){ 
					newValue = oldValue + value;
				}else {
					newValue = oldValue - value;
				}
				if(newValue < 0) newValue = 0;
				dataMap.put(key, newValue);
			} else {
				if(value < 0) value = 0;
				dataMap.put(key, value);
			}
			dataMap.put("id", memberid);
			dataMap.put("updatetime", System.currentTimeMillis());
			mongoService.saveOrUpdateMap(dataMap, MongoData.DEFAULT_ID_NAME, MongoData.NS_MEMBERCOUNT);
		}catch (Exception e) {
			dbLogger.error("", e);
		}
	}
	// ---------------------用户行为统计----------------------
	@Override
	public void saveMemberCount(Long memberid, String key, String value, boolean isReplace) {
		saveMemberCount(memberid, key, value, null, isReplace);
	}
	@Override
	public void saveMemberCount(Long memberid, String key, String value, Integer maxnum, boolean isReplace) {
		try {
			Map map = mongoService.findOne(MongoData.NS_MEMBERCOUNT, MongoData.DEFAULT_ID_NAME, memberid);
			if(map==null) map = new HashMap();
			if(map.containsKey(key)) {
				if(isReplace) map.put(key, value);
				else {
					if(maxnum==null) maxnum = 10;
					String str = map.get(key)+"";
					if(StringUtils.isNotBlank(str)) {
						List<String> strList = Arrays.asList(str.split(","));
						List<String> newList = new ArrayList<String>(strList);
						if (strList.contains(value)) {
							newList.remove(value);
							newList.add(0, value);
							str = StringUtils.join(newList, ",");
						} else {
							str = value + "," + str;
						}
					} else {
						str = value;
					}
					String[] tmp = str.split(",");
					if (tmp.length > maxnum) {
						String[] tmp2 = Arrays.copyOf(tmp, maxnum);
						str = StringUtils.join(tmp2, ",");
					}
					map.put(key, str);
				}
			} else {
				map.put(key, value);
			}
			map.put("id", memberid);
			map.put("updatetime", System.currentTimeMillis());
			mongoService.saveOrUpdateMap(map, MongoData.DEFAULT_ID_NAME, MongoData.NS_MEMBERCOUNT);
		} catch (Exception e) {
			dbLogger.error("", e);
		}
	}
	/*@Override
	public void updateFansCount(long maxid){
		Query query = new Query();
		List<Criteria> criteriaList = new ArrayList<Criteria>();
		criteriaList.add(new Criteria(MemberStats.FIELD_ATTENTIONCOUNT).ne(null));
		criteriaList.add(new Criteria(MemberStats.FIELD_FANSCOUNT).ne(null));
		Criteria criter1 = new Criteria().orOperator(criteriaList.toArray(new Criteria[]{}));
		Criteria criter2 = new Criteria("id").gt(maxid);
		query.addCriteria(criter1);
		query.addCriteria(criter2);
		for(int page=0;page<110;page++){
			int rows = 1000;
			int from = page * rows;
			dbLogger.warn("--------------------------------开始执行修改用户关注数和粉丝数程序----page:" + page);
			List<Map> list = mongoService.find(MongoData.NS_MEMBERCOUNT, query.getQueryObject(), "id", true, from, rows);
			if(list.isEmpty()){
				dbLogger.warn("未找到数据，升级程序结束-------page:" + page);
				break;
			}
			for(Map map : list){
				if(map.get(MemberStats.FIELD_ATTENTIONCOUNT) != null) map.put(MemberStats.FIELD_ATTENTIONCOUNT, queryCount("memberid", (Long)map.get("id")));
				if(map.get(MemberStats.FIELD_FANSCOUNT) != null) map.put(MemberStats.FIELD_FANSCOUNT, queryCount("relatedid", (Long)map.get("id")));
				mongoService.saveOrUpdateMap(map, "_id", MongoData.NS_MEMBERCOUNT);
			}
			dbLogger.warn("update memberid:" + list.get(list.size()-1).get("id"));
		}
	}
	private int queryCount(String temp, Long memberid){
		DetachedCriteria query = DetachedCriteria.forClass(Treasure.class);
		query.add(Restrictions.eq("action", Treasure.ACTION_COLLECT));
		query.add(Restrictions.eq("tag", Treasure.TAG_MEMBER));
		query.add(Restrictions.eq(temp, memberid));
		query.setProjection(Projections.rowCount());
		return Integer.parseInt(""+readOnlyTemplate.findByCriteria(query).get(0));
	}*/
	@Override
	public Map getMemberInfoStats(Long memberid) {
		Map result = mongoService.findOne(MongoData.NS_MEMBER_INFO, "myid", memberid);
		return result;
	}

	@Override
	public void saveMobileLastTicket(String mobile, String tradeNo, String orderType, String time) {
		String encryptMobile = StringUtil.md5("xxxxYYYxxx" + mobile);
		try {
			Map map = mongoService.findOne(MongoData.NS_LASTORDER, MongoData.DEFAULT_ID_NAME, encryptMobile);
			if (map == null) {
				map = new HashMap();
				map.put(MongoData.DEFAULT_ID_NAME, encryptMobile);
			}
			Map<String, String> ticketMap = new HashMap<String, String>();
			ticketMap.put("tradeno", tradeNo);
			ticketMap.put("time", time);
			map.put(orderType, ticketMap);
			if (OrderConstant.ORDER_TYPE_TICKET.equals(orderType) || OrderConstant.ORDER_TYPE_DRAMA.equals(orderType)
					|| OrderConstant.ORDER_TYPE_SPORT.equals(orderType)) {
				map.put("lastorder", ticketMap);
			}
			mongoService.saveOrUpdateMap(map, MongoData.DEFAULT_ID_NAME, MongoData.NS_LASTORDER);
		} catch (Exception e) {
			dbLogger.error("", e);
		}
	}
	
	@Override
	public void saveMbrFirstTicket(Long memberid, String tradeNo, String orderType) {
		try {
			Map map = mongoService.findOne(MongoData.NS_FIRSTORDER, MongoData.SYSTEM_ID, memberid);
			if (map == null) {
				map = new HashMap();
				map.put(MongoData.SYSTEM_ID, memberid);
//				map.put(MongoData.DEFAULT_ID_NAME, memberid);
			}
			boolean isFirst = false;
			if (OrderConstant.ORDER_TYPE_TICKET.equals(orderType) && map.get(OrderConstant.ORDER_TYPE_TICKET)==null) {
				map.put(orderType, tradeNo);
				isFirst = true;
			}else if (OrderConstant.ORDER_TYPE_DRAMA.equals(orderType) && map.get(OrderConstant.ORDER_TYPE_DRAMA)==null) {
				map.put(orderType, tradeNo);
				isFirst = true;
			}else if (OrderConstant.ORDER_TYPE_SPORT.equals(orderType) && map.get(OrderConstant.ORDER_TYPE_SPORT)==null) {
				map.put(orderType, tradeNo);
				isFirst = true;
			}
			if (isFirst) {
				mongoService.saveOrUpdateMap(map, MongoData.SYSTEM_ID, MongoData.NS_FIRSTORDER);
			}
		} catch (Exception e) {
			dbLogger.error("", e);
		}
	}
	
	@Override
	public String getMobileLastTrade(String mobile) {
		Map map = mongoService.findOne(MongoData.NS_LASTORDER, MongoData.DEFAULT_ID_NAME, StringUtil.md5("xxxxYYYxxx" + mobile));
		if (map != null) {
			Map<String, String> orderMap = (Map<String, String>) map.get("lastorder");
			String tradeno = orderMap.get("tradeno");
			return tradeno;
		}else{
			return null;
		}
		
	}
}
