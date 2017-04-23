package com.gewara.service.bbs.impl;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.criterion.Subqueries;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.gewara.constant.PaymethodConstant;
import com.gewara.constant.SysAction;
import com.gewara.constant.TagConstant;
import com.gewara.constant.sys.MongoData;
import com.gewara.model.BaseObject;
import com.gewara.model.bbs.commu.Commu;
import com.gewara.model.movie.Cinema;
import com.gewara.model.movie.Movie;
import com.gewara.model.pay.TicketOrder;
import com.gewara.model.ticket.OpenPlayItem;
import com.gewara.model.user.SysMessageAction;
import com.gewara.model.user.UserMessageAction;
import com.gewara.mongo.MongoService;
import com.gewara.service.bbs.UserMessageService;
import com.gewara.service.impl.BaseServiceImpl;
import com.gewara.support.ReadOnlyTemplate;
import com.gewara.util.DateUtil;
import com.gewara.util.StringUtil;
@Service("userMessageService")
public class UserMessageServiceImpl extends BaseServiceImpl implements UserMessageService{
	@Autowired@Qualifier("mongoService")
	private MongoService mongoService;
	public void setMongoService(MongoService mongoService) {
		this.mongoService = mongoService;
	}
	@Autowired@Qualifier("readOnlyTemplate")
	private ReadOnlyTemplate readOnlyTemplate;
	public void setReadOnlyHibernateTemplate(ReadOnlyTemplate readOnlyTemplate) {
		this.readOnlyTemplate = readOnlyTemplate;
	}	
	@Override
	public Integer getReceiveUserMessageCountByMemberid(Long memberid, Integer isread) {
		DetachedCriteria query = DetachedCriteria.forClass(UserMessageAction.class);
		query.add(Restrictions.eq("tomemberid", memberid));
		query.add(Restrictions.ne("status", "tdel"));
		if(isread!=null) query.add(Restrictions.eq("isread", isread));
		query.setProjection(Projections.rowCount());
		List list = readOnlyTemplate.findByCriteria(query);
		if(list.isEmpty()) return 0;
		return Integer.parseInt(list.get(0)+"");
	}
	
	
	@Override
	public List<UserMessageAction> getReceiveUserMessageListByMemberid(Long memberid, Integer isread, int from, int maxnum) {
		DetachedCriteria query = DetachedCriteria.forClass(UserMessageAction.class);
		if(isread!=null) query.add(Restrictions.eq("isread", isread));
		query.add(Restrictions.eq("tomemberid", memberid));
		query.add(Restrictions.ne("status", "tdel"));
		query.addOrder(Order.desc("addtime"));
		List<UserMessageAction> list = readOnlyTemplate.findByCriteria(query, from, maxnum);
		return list;
	}
	@Override
	public List<UserMessageAction> getUserMessageListByGroupid(Long groupid) {
		DetachedCriteria query = DetachedCriteria.forClass(UserMessageAction.class);
		query.add(Restrictions.eq("groupid", groupid));
		query.addOrder(Order.asc("addtime"));
		List<UserMessageAction> list = readOnlyTemplate.findByCriteria(query);
		return list;
	}
	@Override
	public Integer getSendUserMessageCountByMemberid(Long memberid) {
		DetachedCriteria query = DetachedCriteria.forClass(UserMessageAction.class);
		query.add(Restrictions.ne("status", TagConstant.STATUS_FDEL));
		query.add(Restrictions.eq("frommemberid", memberid));
		query.setProjection(Projections.rowCount());
		List list = readOnlyTemplate.findByCriteria(query);
		if(list.isEmpty()) return 0;
		return Integer.parseInt(list.get(0)+"");
	}

	@Override
	public List<UserMessageAction> getSendUserMessageListByMemberid(Long memberid, int from, int maxnum) {
		DetachedCriteria query = DetachedCriteria.forClass(UserMessageAction.class);
		query.add(Restrictions.ne("status", TagConstant.STATUS_FDEL));
		query.add(Restrictions.eq("frommemberid", memberid));
		query.addOrder(Order.desc("addtime"));
		List<UserMessageAction> list = readOnlyTemplate.findByCriteria(query, from, maxnum);
		return list;
	}
	
	@Override
	public Integer getSysMsgCountByMemberid(Long memberid, String status) {
		DetachedCriteria query = DetachedCriteria.forClass(SysMessageAction.class);
		query.add(Restrictions.eq("tomemberid", memberid));
		if(StringUtils.isNotBlank(status)) query.add(Restrictions.eq("status", status));
		query.setProjection(Projections.rowCount());
		List list = readOnlyTemplate.findByCriteria(query);
		if(list.isEmpty()) return 0;
		return Integer.parseInt(list.get(0)+"");
	}

	@Override
	public List<SysMessageAction> getSysMsgListByMemberid(Long memberid, String status, int from, int maxnum) {
		DetachedCriteria query = DetachedCriteria.forClass(SysMessageAction.class);
		query.add(Restrictions.eq("tomemberid", memberid));
		if(StringUtils.isNotBlank(status)) query.add(Restrictions.eq("status", status));
		query.addOrder(Order.desc("addtime"));
		List<SysMessageAction> list = readOnlyTemplate.findByCriteria(query, from ,maxnum);
		return list;
	}
	@Override
	public UserMessageAction getUserMessageActionByUserMessageid(Long mid) {
		DetachedCriteria query = DetachedCriteria.forClass(UserMessageAction.class);
		query.add(Restrictions.eq("usermessageid", mid));
		List<UserMessageAction> umaList = readOnlyTemplate.findByCriteria(query, 0, 1);
		if(umaList.isEmpty())return null;
		return umaList.get(0);
	}
	@Override
	public Integer getUMACountByMemberid(Long memberid) {
		 DetachedCriteria query = DetachedCriteria.forClass(UserMessageAction.class, "u1");
		 query.add(Restrictions.ne("u1.status", TagConstant.STATUS_TDEL));
		 query.add(Restrictions.eq("u1.tomemberid", memberid));
		 
		 DetachedCriteria subquery = DetachedCriteria.forClass(UserMessageAction.class, "u2");
		 subquery.add(Restrictions.eq("u1.tomemberid", memberid));
		 subquery.add(Restrictions.eqProperty("u2.groupid", "u1.groupid"));
		 subquery.setProjection(Projections.alias(Projections.max("u2.id"), "u2id"));
		 query.add(Subqueries.propertyEq("u1.id", subquery));
		 query.setProjection(Projections.rowCount());
		 List result = readOnlyTemplate.findByCriteria(query);
		 if(result.isEmpty()) return 0;
		 return new Integer(result.get(0)+"");
	}
	@Override
	public List<UserMessageAction> getUMAListByMemberid(final Long memberid, final Integer first, final int maxnum) {
		/*String 	sql = "select a.* from user_message_action a ";
						sql = sql + "where a.tomemberid=? ";
						sql = sql + "and a.recordid=(select max(b.recordid) from user_message_action b ";
						sql = sql + "where a.tomemberid=? and b.groupid=a.groupid)";*/
		 DetachedCriteria query = DetachedCriteria.forClass(UserMessageAction.class, "u1");
		 query.add(Restrictions.ne("u1.status", TagConstant.STATUS_TDEL));
		 query.add(Restrictions.eq("u1.tomemberid", memberid));
		 
		 DetachedCriteria subquery = DetachedCriteria.forClass(UserMessageAction.class, "u2");
		 subquery.add(Restrictions.eq("u2.tomemberid", memberid));
		 subquery.add(Restrictions.eqProperty("u2.groupid", "u1.groupid"));
		 subquery.setProjection(Projections.alias(Projections.max("u2.id"), "u2id"));
		 query.add(Subqueries.propertyEq("u1.id", subquery));
		 query.addOrder(Order.desc("addtime"));
		 List<UserMessageAction> result = readOnlyTemplate.findByCriteria(query, first, maxnum);
		 return result;
	}
	
	
	/**
	 *	author: 	bob
	 *	date:		20100727
	 */
	@Override
	public List<UserMessageAction> getMessagesByMemIdAndStatus(Long memberId, String status, int isread, int from, int maxnum){
		DetachedCriteria query = DetachedCriteria.forClass(UserMessageAction.class, "u1");
		query.add(Restrictions.eq("u1.status", status));
		query.add(Restrictions.eq("u1.tomemberid", memberId));
		if(isread != -1){
			// 查询要显示在用户中心的公告
			query.add(Restrictions.eq("u1.isread", isread));
		}
		query.addOrder(Order.desc("addtime"));
		
		List<UserMessageAction> result = readOnlyTemplate.findByCriteria(query, from, maxnum);
		return result;
	}
	@Override
	/**
	 *  公告列表(单条记录)
	 */
	public UserMessageAction getPublicNotice(){
		List<UserMessageAction> list = getMessagesByMemIdAndStatus(TagConstant.ADMIN_FROMMEMBERID, TagConstant.STATUS_TOALL, TagConstant.READ_YES, 0, 1);
		if(list.isEmpty())return null;
		return list.get(0);
	}
	
	@Override
	public Integer countMessagesByMemIdAndStatus(Long memberId, String status) {
		DetachedCriteria query = DetachedCriteria.forClass(UserMessageAction.class, "u1");
		query.add(Restrictions.eq("u1.status", status));
		 query.add(Restrictions.eq("u1.tomemberid", memberId));
		 
		 query.setProjection(Projections.rowCount());
		 List result = readOnlyTemplate.findByCriteria(query);
		 if(result.isEmpty()) return 0;
		 return new Integer(result.get(0)+"");
	}
	
	@Override
	public boolean isSendMsg(Long memberid){
		String hql = "select max(m.addtime) from UserMessageAction m where m.frommemberid=?";
		List<Timestamp> list = readOnlyTemplate.find(hql, memberid);
		Timestamp t = list.get(0);
		if(t==null) return true;
		if(t.getTime()+TagConstant.MAX_SECOND*1000>System.currentTimeMillis()) {
			return false;
		}
		return true;
	}
	@Override
	public void initSysMsgList(List<SysMessageAction> sysMsgList){
		if(sysMsgList==null) return;
		BaseObject relate = null;
		for(SysMessageAction sysMsg : sysMsgList){
			relate = baseDao.getObject(Commu.class, sysMsg.getActionid());
			sysMsg.setRelate(relate);
		}
	}
	@Override
	public List<UserMessageAction> getUserMessageActionByMemberid(Long memberid, int from, int maxnum){
		DetachedCriteria query = DetachedCriteria.forClass(UserMessageAction.class);
		query.add(Restrictions.or(Restrictions.eq("frommemberid", memberid), Restrictions.eq("tomemberid", memberid)));
		query.add(Restrictions.ne("status", SysAction.STATUS_APPLY));
		query.addOrder(Order.desc("addtime"));
		List<UserMessageAction> list = readOnlyTemplate.findByCriteria(query, from, maxnum);
		return list;
	}
	@Override
	public List<UserMessageAction> getUserMessageActionByFromidToid(Long frommemberid, Long tomemberid, Timestamp addtime, int from, int maxnum){
		DetachedCriteria query = DetachedCriteria.forClass(UserMessageAction.class);
		if(tomemberid!=null){
			query.add(Restrictions.or(Restrictions.and(Restrictions.eq("frommemberid", frommemberid), Restrictions.eq("tomemberid", tomemberid)), 
					Restrictions.and(Restrictions.eq("frommemberid", tomemberid), Restrictions.eq("tomemberid", frommemberid))));
		}else {
			query.add(Restrictions.or(Restrictions.eq("frommemberid", frommemberid), Restrictions.eq("tomemberid", frommemberid)));
		}
		if(addtime!=null) query.add(Restrictions.gt("addtime", addtime));
		query.add(Restrictions.ne("status", "tdel"));
		query.addOrder(Order.desc("addtime"));
		List<UserMessageAction> list = readOnlyTemplate.findByCriteria(query, from, maxnum);
		return list;
	}
	@Override
	public boolean isExistSysMessageAction(Long actionid, Long memberid, String action, boolean flag){
		DetachedCriteria query = DetachedCriteria.forClass(SysMessageAction.class);
		if(flag){ //申请加入
			query.add(Restrictions.eq("frommemberid", memberid));
		}else {//被邀请加入
			query.add(Restrictions.eq("tomemberid", memberid));
		}
		if(actionid!=null)query.add(Restrictions.eq("actionid", actionid));
		query.add(Restrictions.eq("action", action));
		query.add(Restrictions.eq("status", SysAction.STATUS_APPLY));
		List<SysMessageAction> sysList = readOnlyTemplate.findByCriteria(query);
		if(sysList.size()>0) return true;
		return false;
	}
	@Override
	public SysMessageAction getSysMessageAction(Long commuid, Long memberid, String action, boolean flag){
		DetachedCriteria query = DetachedCriteria.forClass(SysMessageAction.class);
		if(flag){//申请加入圈子
			query.add(Restrictions.eq("frommemberid", memberid));
		}else {//被邀请加入圈子
			query.add(Restrictions.eq("tomemberid", memberid));
		}
		query.add(Restrictions.eq("actionid", commuid));
		query.add(Restrictions.eq("action", action));
		query.add(Restrictions.eq("status", SysAction.STATUS_APPLY));
		List<SysMessageAction> sysList = readOnlyTemplate.findByCriteria(query, 0, 1);
		if(sysList.isEmpty())return null;
		return sysList.get(0);
	}

	@Override
	public Integer getCountMessageByMessageActionId(Long id) {
		DetachedCriteria query = DetachedCriteria.forClass(UserMessageAction.class);
		query.add(Restrictions.eq("groupid", id));
		query.setProjection(Projections.rowCount());
		
		List result = readOnlyTemplate.findByCriteria(query);
		if(result.isEmpty()) return 0;
		return new Integer(result.get(0)+"");
	}

	@Override
	public Integer getNotReadMessage(Long memberid,Integer isRead) {
		DetachedCriteria query = DetachedCriteria.forClass(UserMessageAction.class,"u");
		query.add(Restrictions.ne("u.status", TagConstant.STATUS_TDEL));
		query.add(Restrictions.eq("u.tomemberid", memberid));
		query.add(Restrictions.eq("u.isread", isRead));
		query.setProjection(Projections.rowCount());
		List list=readOnlyTemplate.findByCriteria(query);
		if(list.isEmpty()) return 0;
		return Integer.parseInt(list.get(0)+"");
	}

	@Override
	public Integer getNotReadSysMessage(Long memberid,Long isRead) {
		DetachedCriteria query = DetachedCriteria.forClass(SysMessageAction.class,"s");
		query.add(Restrictions.eq("s.tomemberid",memberid));
		query.add(Restrictions.eq("s.isread",isRead));
		query.setProjection(Projections.rowCount());
		List list = readOnlyTemplate.findByCriteria(query);
		if(list.isEmpty()) return 0;
		return Integer.parseInt(list.get(0)+"");
	}
	@Override
	public List<SysMessageAction> getNotReadSysMessageList(Long memberid,Long isRead) {
		DetachedCriteria query = DetachedCriteria.forClass(SysMessageAction.class,"s");
		query.add(Restrictions.eq("s.tomemberid",memberid));
		query.add(Restrictions.eq("s.isread",isRead));
		List<SysMessageAction> list = readOnlyTemplate.findByCriteria(query);
		return list;
	}
	@Override
	public List<SysMessageAction> getSysMessageActionListByActionidAndActionAndStatus(Long actionid, String action, String status, int from, int maxnum){
		DetachedCriteria query = DetachedCriteria.forClass(SysMessageAction.class,"s");
		query.add(Restrictions.eq("s.actionid", actionid));
		query.add(Restrictions.eq("s.action", action));
		query.add(Restrictions.eq("s.status", status));
		List<SysMessageAction> result = readOnlyTemplate.findByCriteria(query, from, maxnum);
		return result;
	}

	@Override
	public void sendSiteMSG(Long tomemberid, String action, Long actionid, String body) {
		if(StringUtils.isBlank(action)){
			action = SysAction.STATUS_RESULT;
		}
		SysMessageAction sysMessageAction = new SysMessageAction(action);
		sysMessageAction.setFrommemberid(1L);
		sysMessageAction.setTomemberid(tomemberid);
		sysMessageAction.setActionid(actionid);
		sysMessageAction.setStatus(SysAction.STATUS_RESULT);
		sysMessageAction.setBody(body);
		baseDao.saveObject(sysMessageAction);
	}
	
	@Override
	public void addMsgAction(TicketOrder order, OpenPlayItem opi){
		Movie movie = baseDao.getObject(Movie.class, opi.getMovieid());
		Cinema cinema = baseDao.getObject(Cinema.class, order.getCinemaid());
		Timestamp sendtime = DateUtil.addMinute(opi.getPlaytime(), 180);
		Timestamp validtime = DateUtil.addHour(sendtime, 6);
		Map model = new HashMap();
		model.put("moviename", movie.getMoviename());
		model.put("cinemaname", cinema.getName());
		model.put("movieid", movie.getId());
		model.put("cinemaid", cinema.getId());
		Integer pointvalue = 0;
		if(StringUtils.equals(order.getPaymethod(), PaymethodConstant.PAYMETHOD_GEWAPAY)) pointvalue = order.getDue() - order.getWabi();
		else pointvalue = order.getDue();
		if(pointvalue<0) pointvalue = 0;
		model.put("pointvalue", pointvalue.toString());
		model.put(MongoData.SYSTEM_ID, order.getId() + "" + System.currentTimeMillis() + StringUtil.getRandomString(5));
		model.put("tomemberid", order.getMemberid());
		model.put("action", SysAction.ACTION_TICKET_SUCCESS);
		model.put("actionid", movie.getId());
		model.put("sendtime", sendtime);
		model.put("validtime", validtime);
		model.put("addtime", DateUtil.getCurFullTimestampStr());
		mongoService.saveOrUpdateMap(model, MongoData.SYSTEM_ID, MongoData.NS_SYSMESSAGEACTION);
	}
}
