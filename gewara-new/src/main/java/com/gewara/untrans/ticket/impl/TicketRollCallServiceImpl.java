package com.gewara.untrans.ticket.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import com.gewara.Config;
import com.gewara.constant.Status;
import com.gewara.constant.sys.MongoData;
import com.gewara.json.TicketRollCall;
import com.gewara.json.TicketRollCallMember;
import com.gewara.mongo.MongoService;
import com.gewara.support.ErrorCode;
import com.gewara.untrans.ticket.TicketRollCallService;
import com.gewara.util.BeanUtil;
import com.gewara.util.DateUtil;
import com.gewara.util.GewaLogger;
import com.gewara.util.LoggerUtils;
import com.gewara.util.ValidateUtil;

@Service("ticketRollCallService")
public class TicketRollCallServiceImpl implements TicketRollCallService {
	private final transient GewaLogger dbLogger = LoggerUtils.getLogger(getClass(), Config.getServerIp(), Config.SYSTEMID);
	@Autowired@Qualifier("mongoService")
	private MongoService mongoService;
	public void setMongoService(MongoService mongoService){
		this.mongoService = mongoService;
	}
	
	@Override
	public ErrorCode<String> saveOrUpdateTicketRollCall(Long memberid, String mobile, String tag, Long relatedid, int quantity, int checkCount) {
		if((memberid == null &&!ValidateUtil.isMobile(mobile)) || StringUtils.isBlank(tag) || relatedid == null) return ErrorCode.getFailure("参数错误！");
		String memberIdStr = String.valueOf(memberid);
		Date curDate = DateUtil.currentTime();
		Date startDate = DateUtil.getBeginningTimeOfDay(curDate);
		List<TicketRollCall> memberIdToTicketRollList =	getTicketRollCall(tag, relatedid, startDate, curDate, memberIdStr, mobile);
		List<String> mobiles = BeanUtil.getBeanPropertyList(memberIdToTicketRollList, String.class, "mobile", true);
		if(memberIdToTicketRollList.isEmpty()){
			TicketRollCall ticketRollCall1 = new TicketRollCall(memberIdStr, tag, relatedid);
			ticketRollCall1.setId(memberIdStr);
			ticketRollCall1.setQuantity(quantity);
			mongoService.saveOrUpdateObject(ticketRollCall1, MongoData.DEFAULT_ID_NAME);
			TicketRollCall ticketRollCall2 = new TicketRollCall(mobile, tag, relatedid);
			ticketRollCall2.setId(mobile);
			ticketRollCall2.setQuantity(quantity);
			mongoService.saveOrUpdateObject(ticketRollCall2, MongoData.DEFAULT_ID_NAME);
		}else{ 
			for (TicketRollCall ticketRollCall : memberIdToTicketRollList) {
				int newQuantity = ticketRollCall.getQuantity() + quantity;
				ticketRollCall.setQuantity(newQuantity);
				mongoService.saveOrUpdateObject(ticketRollCall, MongoData.DEFAULT_ID_NAME);
				if(ticketRollCall.getQuantity() >= checkCount){
					TicketRollCallMember ticketRollCallMember = mongoService.getObject(TicketRollCallMember.class, MongoData.DEFAULT_ID_NAME, ticketRollCall.getMobile());
					if(ticketRollCallMember == null || !StringUtils.equals(ticketRollCallMember.getStatus(), Status.Y)){
						ticketRollCallMember = new TicketRollCallMember(ticketRollCall.getMobile(), Status.DEL);
						ticketRollCallMember.setId(ticketRollCall.getMobile());
						mongoService.saveOrUpdateObject(ticketRollCallMember, MongoData.DEFAULT_ID_NAME);
					}
				}
			}
		}
		if(mobiles.size() == 1){
			if(StringUtils.equals(mobiles.get(0), memberIdStr)){
				TicketRollCall ticketRollCall2 = new TicketRollCall(mobile, tag, relatedid);
				ticketRollCall2.setId(mobile);
				ticketRollCall2.setQuantity(quantity);
				mongoService.saveOrUpdateObject(ticketRollCall2, MongoData.DEFAULT_ID_NAME);
			}else{
				TicketRollCall ticketRollCall1 = new TicketRollCall(memberIdStr, tag, relatedid);
				ticketRollCall1.setId(memberIdStr);
				ticketRollCall1.setQuantity(quantity);
				mongoService.saveOrUpdateObject(ticketRollCall1, MongoData.DEFAULT_ID_NAME);
			}
		}
		return ErrorCode.SUCCESS;
	}
	
	@Override
	public Integer getTicketRollCallMemberCount(String status, Date startDate, Date endDate, String... mobiles){
		Query query = new Query();
		if(!ArrayUtils.isEmpty(mobiles)){
			List<Criteria> criteriaList = new ArrayList<Criteria>();
			for (String mobile : mobiles) {
				if(StringUtils.isNotBlank(mobile)){
					criteriaList.add(new Criteria("mobile").is(mobile));
				}
			}
			if(!criteriaList.isEmpty()){
				Criteria criteria1 = new Criteria().orOperator(criteriaList.toArray(new Criteria[]{}));
				query.addCriteria(criteria1);
			}
		}
		if(StringUtils.isNotBlank(status)) query.addCriteria(new Criteria("status").is(status));
		Criteria criteria2 = null;
		if(startDate !=null && endDate != null){
			criteria2 = new Criteria("addDate").gte(startDate).lte(endDate);
		}else if(startDate != null){
			criteria2 = new Criteria("addDate").gte(startDate);
		}else if(endDate != null){
			criteria2 = new Criteria("addDate").lte(endDate);
		}
		if(criteria2 != null) query.addCriteria(criteria2);
		int count = mongoService.getObjectCount(TicketRollCallMember.class, query.getQueryObject());
		return count;
	}
	
	private List<TicketRollCall> getTicketRollCall(String tag, Long relatedid, Date startDate, Date endDate, String... mobiles){
		Query query = getTicketRollCallQuery(tag, relatedid, startDate, endDate, mobiles);
		List<TicketRollCall> ticketRollCallList = mongoService.getObjectList(TicketRollCall.class, query.getQueryObject());
		return ticketRollCallList;
	}
	
	@Override
	public Query getTicketRollCallQuery(String tag, Long relatedid, Date startDate, Date endDate, String... mobiles){
		Query query = new Query();
		if(!ArrayUtils.isEmpty(mobiles)){
			List<Criteria> criteriaList = new ArrayList<Criteria>();
			for (String mobile : mobiles) {
				if(StringUtils.isNotBlank(mobile)){
					criteriaList.add(new Criteria("mobile").is(mobile));
				}
			}
			if(!criteriaList.isEmpty()){
				Criteria criteria1 = new Criteria().orOperator(criteriaList.toArray(new Criteria[]{}));
				query.addCriteria(criteria1);
			}
		}
		if(StringUtils.isNotBlank(tag)){
			Criteria criteria3 = new Criteria("tag").is(tag);
			query.addCriteria(criteria3);
		}
		if(relatedid != null){
			Criteria criteria4 = new Criteria("relatedid").is(relatedid);
			query.addCriteria(criteria4);
		}
		Criteria criteria5 = null;
		if(startDate !=null && endDate != null){
			criteria5 = new Criteria("addDate").gte(startDate).lte(endDate);
		}else if(startDate != null){
			criteria5 = new Criteria("addDate").gte(startDate);
		}else if(endDate != null){
			criteria5 = new Criteria("addDate").lte(endDate);
		}
		if(criteria5 != null) query.addCriteria(criteria5);
		return query;
	}
	@Override
	public List<TicketRollCallMember> getTicketRollCallMemberList(String status, Date startDate, Date endDate, int from, int maxnum, String... mobiles){
		Query query = new Query();
		if(!ArrayUtils.isEmpty(mobiles)){
			List<Criteria> criteriaList = new ArrayList<Criteria>();
			for (String mobile : mobiles) {
				if(StringUtils.isNotBlank(mobile)){
					criteriaList.add(new Criteria("mobile").is(mobile));
				}
			}
			if(!criteriaList.isEmpty()){
				Criteria criteria1 = new Criteria().orOperator(criteriaList.toArray(new Criteria[]{}));
				query.addCriteria(criteria1);
			}
		}
		if(StringUtils.isNotBlank(status)) {
			query.addCriteria(new Criteria("status").is(status));
		}
		Criteria criteria2 = null;
		if(startDate !=null && endDate != null){
			criteria2 = new Criteria("addDate").gte(startDate).lte(endDate);
		}else if(startDate != null){
			criteria2 = new Criteria("addDate").gte(startDate);
		}else if(endDate != null){
			criteria2 = new Criteria("addDate").lte(endDate);
		}
		if(criteria2 != null) query.addCriteria(criteria2);
		List<TicketRollCallMember> ticketRollCallMemberList = mongoService.getObjectList(TicketRollCallMember.class, query.getQueryObject(), "addDate", false, from, maxnum);
		return ticketRollCallMemberList;
	}
	
	@Override
	public boolean isTicketRollCallMember(Long memberid, String mobile){
		List<String> mobilesList = new ArrayList<String>();
		if(memberid != null){
			mobilesList.add(String.valueOf(memberid));
		}
		if(StringUtils.isNotBlank(mobile)){
			mobilesList.add(mobile);
		}
		if(mobilesList.isEmpty()) return false;
		try{
			List<TicketRollCallMember> callMemberList = getTicketRollCallMemberList(null, null, null, 0, 1, mobilesList.toArray(new String[]{}));
			for (TicketRollCallMember ticketRollCallMember : callMemberList) {
				if(!StringUtils.equals(ticketRollCallMember.getStatus(), Status.Y))
					return true;
			}
		}catch(Exception e) {
			dbLogger.warn("");
			return false;
		}
		return false;
	}

	@Override
	public boolean removeRollCallMember(String id) {
		return mongoService.removeObjectById(TicketRollCallMember.class, MongoData.DEFAULT_ID_NAME, id);
	}

	@Override
	public ErrorCode addTicketRollMember(String mobile, String status, String reason, Long userid) {
		if(StringUtils.isBlank(mobile)) return ErrorCode.getFailure("用户ID或手机号不能为空！");
		if(StringUtils.isBlank(status)) return ErrorCode.getFailure("状态不能为空！");
		TicketRollCallMember ticketRollCallMember = mongoService.getObject(TicketRollCallMember.class, MongoData.DEFAULT_ID_NAME, mobile);
		if(ticketRollCallMember == null){
			ticketRollCallMember = new TicketRollCallMember(mobile, status);
			ticketRollCallMember.setId(mobile);
			ticketRollCallMember.setUserid(userid);
			ticketRollCallMember.setReason(reason);
			mongoService.saveOrUpdateObject(ticketRollCallMember, MongoData.DEFAULT_ID_NAME);
		}else {
			return ErrorCode.getFailure("该用户ID或手机号数据已经存在！");
		}
		return ErrorCode.SUCCESS;

	}
}
