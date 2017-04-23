package com.gewara.service.bbs.impl;

import java.sql.Timestamp;
import java.util.Date;
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

import com.gewara.constant.TagConstant;
import com.gewara.json.MemberStats;
import com.gewara.model.drama.DramaOrder;
import com.gewara.model.drama.OpenDramaItem;
import com.gewara.model.pay.SMSRecord;
import com.gewara.model.pay.SportOrder;
import com.gewara.model.pay.TicketOrder;
import com.gewara.model.sport.OpenTimeTable;
import com.gewara.model.ticket.OpenPlayItem;
import com.gewara.model.user.Agenda;
import com.gewara.model.user.Member;
import com.gewara.service.bbs.AgendaService;
import com.gewara.service.impl.BaseServiceImpl;
import com.gewara.support.ReadOnlyTemplate;
import com.gewara.untrans.MemberCountService;
import com.gewara.util.DateUtil;
import com.gewara.util.JsonUtils;
import com.gewara.xmlbind.activity.RemoteActivity;



@Service("agendaService")
public class AgendaServiceImpl extends BaseServiceImpl implements AgendaService {
	@Autowired@Qualifier("readOnlyTemplate")
	private ReadOnlyTemplate readOnlyTemplate;
	public void setReadOnlyHibernateTemplate(ReadOnlyTemplate readOnlyTemplate) {
		this.readOnlyTemplate = readOnlyTemplate;
	}	
	@Autowired@Qualifier("memberCountService")
	private MemberCountService memberCountService;
	
	@Override
	public void deleteActivityAgenda(Long activityid, Member member){
		//删除生活
		DetachedCriteria query = DetachedCriteria.forClass(Agenda.class);
		query.add(Restrictions.eq("actionid", activityid));
		if(member!=null)query.add(Restrictions.eq("memberid", member.getId())); //管理员在后台删除活动
		List<Agenda> agendaList = readOnlyTemplate.findByCriteria(query);
		baseDao.removeObjectList(agendaList);
	}
	
	@Override
	public List<Agenda> getAgendaList(Long actionid){
		DetachedCriteria query = DetachedCriteria.forClass(Agenda.class);
		query.add(Restrictions.eq("actionid", actionid));
		query.addOrder(Order.desc("addtime"));
		List<Agenda> agendaList = readOnlyTemplate.findByCriteria(query);
		return agendaList;
	}
	@Override
	public List<Agenda> getAgendaListByDate(Long memberid, Date date, boolean flag,boolean isNewTime, int from, int maxnum){
		DetachedCriteria query = DetachedCriteria.forClass(Agenda.class);
		if(memberid != null)query.add(Restrictions.eq("memberid", memberid));
		if(date != null){
			if(flag){ //当天
				query.add(Restrictions.eq("startdate", date));
			}else {//未来7天
				query.add(Restrictions.ge("startdate", date));
				query.add(Restrictions.le("startdate", DateUtil.addDay(date, 6)));
			}
		}
		if(isNewTime){
			query.addOrder(Order.desc("addtime"));
		}else{
			query.addOrder(Order.asc("startdate"));
			query.addOrder(Order.asc("starttime"));
		}
		List<Agenda> agendaList = readOnlyTemplate.findByCriteria(query, from ,maxnum);
		return agendaList;
	}
	@Override
	public Integer getAgendaCountByDate(Long memberid, Date date){
		DetachedCriteria query = DetachedCriteria.forClass(Agenda.class);
		if(memberid != null)query.add(Restrictions.eq("memberid", memberid));
		if(date != null)query.add(Restrictions.eq("startdate", date));
		query.setProjection(Projections.rowCount());
		List list = readOnlyTemplate.findByCriteria(query);
		if (list.isEmpty()) return 0;
		return Integer.parseInt(""+list.get(0));
	}
	
	@Override
	public List<Agenda> getAgendaList(String status, int from, int maxnum, Date fromDate, Date toDate, String keyName) {
		DetachedCriteria query = DetachedCriteria.forClass(Agenda.class, "ag");
		DetachedCriteria subquery = DetachedCriteria.forClass(SMSRecord.class, "sms");
		if(fromDate != null){
			query.add(Restrictions.ge("ag.startdate", fromDate));
		}
		if(toDate != null){
			query.add(Restrictions.le("ag.enddate", toDate));
		}
		if(StringUtils.isNotBlank(keyName)){
			query.add(Restrictions.or(Restrictions.or(Restrictions.like("ag.title", "%"+keyName+"%"),Restrictions.like("ag.membername", "%"+keyName+"%")), Restrictions.or(Restrictions.like("ag.address", "%"+keyName+"%"),Restrictions.like("ag.content", "%"+keyName+"%"))));
		}
		query.addOrder(Order.asc("ag.id"));
		if(StringUtils.isNotBlank(status)){
			subquery.add(Restrictions.eq("sms.status", status));
			subquery.add(Restrictions.like("content", "你安排了%"));
			subquery.add(Restrictions.eqProperty("sms.relatedid", "ag.id"));
			subquery.setProjection(Projections.property("sms.relatedid"));
			query.add(Subqueries.exists(subquery));
		}
		List<Agenda> agendaList = readOnlyTemplate.findByCriteria(query,from,maxnum);
		return agendaList;
	}
	@Override
	public Integer getAgendaListCount(String status, Date fromDate, Date toDate, String keyName) {
		DetachedCriteria query = DetachedCriteria.forClass(Agenda.class, "ag");
		DetachedCriteria subquery = DetachedCriteria.forClass(SMSRecord.class, "sms");
		if(fromDate != null){
			query.add(Restrictions.ge("ag.startdate", fromDate));
		}
		if(toDate != null){
			query.add(Restrictions.le("ag.enddate", toDate));
		}
		if(StringUtils.isNotBlank(keyName)){
			query.add(Restrictions.or(Restrictions.or(Restrictions.like("ag.title", "%"+keyName+"%"),Restrictions.like("ag.membername", "%"+keyName+"%")), Restrictions.or(Restrictions.like("ag.address", "%"+keyName+"%"),Restrictions.like("ag.content", "%"+keyName+"%"))));
		}
		query.setProjection(Projections.rowCount());
		if(StringUtils.isNotBlank(status)){
			subquery.add(Restrictions.eq("sms.status", status));
			subquery.add(Restrictions.like("content", "你安排了%"));
			subquery.add(Restrictions.eqProperty("sms.relatedid", "ag.id"));
			subquery.setProjection(Projections.property("sms.relatedid"));
			query.add(Subqueries.exists(subquery));
		}
		List<Agenda> agendaList = readOnlyTemplate.findByCriteria(query);
		return new Integer(agendaList.get(0)+"");
	}
	@Override
	public List<SMSRecord> getFriendListFromSMS(Long recordid) {
		DetachedCriteria query = DetachedCriteria.forClass(SMSRecord.class);
		query.add(Restrictions.eq("tradeNo", "ag"+recordid));
		query.add(Restrictions.like("content", "%邀请你%"));
		List<SMSRecord> agendaList = readOnlyTemplate.findByCriteria(query);
		return agendaList;
	}

	/*private void addComment1(String date, Agenda agenda, GewaOrder order, String tag){
		//添加哇啦
		//给哇啦数+1
		memberCountService.updateMemberCount(order.getMemberid(), MemberStats.FIELD_COMMENTCOUNT, 1, true);
		//给哇啦数+1
		Member member = baseDao.getObject(Member.class, order.getMemberid());
		String msgContent = "";
		msgContent += date;
		if(StringUtils.equals(tag, TagConstant.TAG_SPORT)){
			SportOrder sportorder = (SportOrder)order;
			Sport sport = baseDao.getObject(Sport.class, sportorder.getSportid());
			SportItem sportItem = baseDao.getObject(SportItem.class, sportorder.getItemid());
			if(sportItem!=null){
				String link = config.getBasePath() + "sport/item/" + sportItem.getId();
				String linkStr = "<a href=\""+link+"\" target=\"_blank\">"+sportItem.getName()+"</a>";
				msgContent +="去"+sport.getName()+" 练习 "+linkStr;
			 }
			Map otherinfoMap = new HashMap();
			otherinfoMap.put("项目", sportItem.getName());
			otherinfoMap.put("场馆ID", sport.getId());
			otherinfoMap.put("场馆", sport.getName());
			otherinfoMap.put("时间", agenda.getStarttime());
			String otherinfo = JsonUtils.writeObjectToJson(otherinfoMap);
			ErrorCode<Comment> ec = commentService.addMicroComment(member, TagConstant.TAG_SPORTAGENDA_MEMBER, agenda.getId(), msgContent,"", null, null, true, null, otherinfo,null,null,null);
			if(ec!=null){
				if(ec.isSuccess()){
					shareService.sendShareInfo("wala",ec.getRetval().getId(), ec.getRetval().getMemberid(), null);
				}
			}
		}else if(StringUtils.equals(tag, TagConstant.TAG_MOVIE)){
			TicketOrder tickerorder = (TicketOrder)order;
			Cinema cinema = baseDao.getObject(Cinema.class, tickerorder.getCinemaid());
			Movie movie = baseDao.getObject(Movie.class, tickerorder.getMovieid());
			if(movie!=null){
				String link = config.getBasePath() + "movie/" + movie.getId();
				String linkStr = "<a href=\""+link+"\" target=\"_blank\">"+movie.getName()+"</a>";
				msgContent +="在"+cinema.getName()+"观看"+linkStr;
			 }
			Map otherinfoMap = new HashMap();
			Map dMap = JsonUtils.readJsonToMap(order.getDescription2());
			otherinfoMap.put("影厅", dMap.get("影厅"));
			otherinfoMap.put("影片", dMap.get("影片"));
			otherinfoMap.put("日期", DateUtil.format(agenda.getStartdate(), "yyyy年M月dd日"));
			otherinfoMap.put("时间", agenda.getStarttime());
			otherinfoMap.put("座位", dMap.get("影票"));
			otherinfoMap.put("场次", dMap.get("场次"));
			otherinfoMap.put("影院", cinema.getName());
			otherinfoMap.put("影院ID", cinema.getId());
			String otherinfo = JsonUtils.writeObjectToJson(otherinfoMap);
			ErrorCode<Comment> ec = commentService.addMicroComment(member, TagConstant.TAG_MOVIEAGENDA_MEMBER, agenda.getId(), msgContent, "", null, null, true, null, otherinfo,null,null,null);
			if(ec!=null){
				if(ec.isSuccess()){
					shareService.sendShareInfo("wala",ec.getRetval().getId(), ec.getRetval().getMemberid(), null);
				}
			}
		}else{
			DramaOrder dramaorder = (DramaOrder)order;
			Theatre theatre = baseDao.getObject(Theatre.class, dramaorder.getTheatreid());
			Drama drama =  baseDao.getObject(Drama.class, dramaorder.getDramaid());
			if(drama!=null){
				String link = config.getBasePath() + "drama/" + drama.getId();
				String linkStr = "<a href=\""+link+"\" target=\"_blank\">"+drama.getName()+"</a>";
				msgContent +="在 "+theatre.getName()+" 观看"+linkStr;
			 }
			Map otherinfoMap = new HashMap();
			otherinfoMap.put("日期", DateUtil.format(agenda.getStartdate(),"yyyy年MM月dd日"));
			otherinfoMap.put("时间", agenda.getStarttime());
			otherinfoMap.put("区域", JsonUtils.getJsonValueByKey(agenda.getOtherinfo(), "roomname"));
			otherinfoMap.put("剧院", theatre.getName());
			otherinfoMap.put("话剧", drama.getName());
			otherinfoMap.put("剧院ID", theatre.getId());
			String otherinfo = JsonUtils.writeObjectToJson(otherinfoMap);
			ErrorCode<Comment> ec = commentService.addMicroComment(member, TagConstant.TAG_DRAMAAGENDA_MEMBER, agenda.getId(), msgContent, "", null, null, true, null, otherinfo,null,null,null);
			if(ec!=null){
				if(ec.isSuccess()){
					shareService.sendShareInfo("wala",ec.getRetval().getId(), ec.getRetval().getMemberid(), null);
				}
			}
		}
	}*/
	
	@Override
	public Agenda getAgendaByAction(Long actionid, String action, Long memberid){
		DetachedCriteria query = DetachedCriteria.forClass(Agenda.class);
		query.add(Restrictions.eq("actionid", actionid));
		query.add(Restrictions.eq("action", action));
		query.add(Restrictions.eq("memberid", memberid));
		List<Agenda> agendaList = readOnlyTemplate.findByCriteria(query);
		if(agendaList.size()>0) return agendaList.get(0);
		return null;
	}
	
	@Override
	public List<Agenda> getAgendaListByDate(Long memberid, Date startDate, Date endDate){
		DetachedCriteria query = DetachedCriteria.forClass(Agenda.class);
		query.add(Restrictions.eq("memberid", memberid));
		query.add(Restrictions.ge("startdate", startDate));
		query.add(Restrictions.lt("startdate", endDate));
		query.addOrder(Order.desc("startdate"));
		query.addOrder(Order.desc("starttime"));
		List<Agenda> agendaList = readOnlyTemplate.findByCriteria(query);
		return agendaList;
	}
	
	@Override
	public void addOrderAgenda(TicketOrder order, OpenPlayItem opi) {
		if(opi!=null) {
			String content = DateUtil.format(opi.getPlaytime(), "MM月dd日 HH:mm") + " 在 " + opi.getCinemaname() + " 观看 " + opi.getMoviename();
			String description2 = order.getDescription2();
			Map dmap = JsonUtils.readJsonToMap(description2);
			Map dataMap = new HashMap();
			dataMap.put("roomname", opi.getRoomname());
			dataMap.put("relatedname", opi.getCinemaname());
			dataMap.put("categoryname", opi.getMoviename());
			dataMap.put("seat", dmap.get("影票"));
			String otherinfo = JsonUtils.writeObjectToJson(dataMap);
			Agenda agenda = addAgenda(content, order.getMemberid(), order.getMembername(), DateUtil.parseDate(DateUtil.format(opi.getPlaytime(), "yyyy-MM-dd")), DateUtil.format(opi.getPlaytime(), "HH:mm"), content, TagConstant.TAG_CINEMA, opi.getCinemaid(), TagConstant.TAG_MOVIE, opi.getMovieid(), order.getAddtime(), TagConstant.AGENDA_ACTION_TICKET, order.getId(), null, null, null, otherinfo);
			if(agenda != null){//添加哇啦
				//addComment(DateUtil.format(agenda.getStartdate(), "MM月dd日"), agenda, order, TagConstant.TAG_MOVIE);
			}
		}
	}
	
	@Override
	public void addOrderAgenda(SportOrder order, OpenTimeTable ott) {
		if(ott != null){
			Map dataMap = new HashMap();
			dataMap.put("relatedname", ott.getSportname());
			dataMap.put("categoryname", ott.getItemname());
			String otherinfo = JsonUtils.writeObjectToJson(dataMap);
			String descriptiondate = JsonUtils.getJsonValueByKey(order.getDescription2(), "详细");
			String starttime = null;
			String endtime = null;
			if(descriptiondate != null){
				starttime = StringUtils.substring(descriptiondate, 0, 5);
				endtime = StringUtils.substring(descriptiondate, 6, 11);
			}
			String content = DateUtil.format(ott.getPlaydate(), "MM月dd日") + " " ;
			if(StringUtils.isBlank(starttime)) content += "00:00";
			else content += starttime;
			if(StringUtils.isBlank(endtime)) content += " - " + endtime;
			content += " 在 " + ott.getSportname() + " 练习" + ott.getItemname();
			Agenda agenda = addAgenda(content, order.getMemberid(), order.getMembername(), ott.getPlaydate(), starttime, content, TagConstant.TAG_SPORT, 
					ott.getSportid(), TagConstant.TAG_SPORTITEM, ott.getItemid(), order.getAddtime(), 
					TagConstant.AGENDA_ACTION_SPORT, order.getId(), ott.getPlaydate(), endtime, null, otherinfo);
			if(agenda != null){//添加哇啦
				//addComment(DateUtil.format(agenda.getStartdate(), "MM月dd日"), agenda, order, TagConstant.TAG_SPORT);
			}
			memberCountService.saveMemberCount(order.getMemberid(), MemberStats.FIELD_LASTSPORTID, order.getSportid()+"", false);
		}
	}
	
	@Override
	public void addOrderAgenda(DramaOrder order, OpenDramaItem odt) {
		if(odt!=null) {
			String content = DateUtil.format(odt.getPlaytime(), "MM月dd日 HH:mm") + " 在 " + odt.getTheatrename() + " 观看 " + odt.getDramaname();
			Map dataMap = new HashMap();
			dataMap.put("roomname", odt.getRoomname());
			dataMap.put("relatedname", odt.getTheatrename());
			dataMap.put("categoryname", odt.getDramaname());
			String otherinfo = JsonUtils.writeObjectToJson(dataMap);
			Agenda agenda = addAgenda(content, order.getMemberid(), order.getMembername(), DateUtil.parseDate(DateUtil.format(odt.getPlaytime(), "yyyy-MM-dd")), DateUtil.format(odt.getPlaytime(), "HH:mm"), content, TagConstant.TAG_THEATRE, odt.getTheatreid(), TagConstant.TAG_DRAMA, odt.getDramaid(), order.getAddtime(), TagConstant.AGENDA_ACTION_DRAMA, order.getId(), null, null, null, otherinfo);
			if(agenda != null){//添加哇啦
				//addComment(DateUtil.format(agenda.getStartdate(), "MM月dd日"), agenda, order, TagConstant.TAG_DRAMA);
			}
		}
	}
	
	@Override 
	public Agenda addActivityAgenda(RemoteActivity activity, Member member){
		Long memberid = null;
		String nickname = null;
		String action = "";
		if(member != null && StringUtils.equals(activity.getSign(), RemoteActivity.SIGN_PRICE5)){//五元抢票
			action = TagConstant.AGENDA_ACTION_PRICE5;
			memberid = member.getId();
			nickname = member.getNickname();
		}else if(member != null && StringUtils.equals(activity.getSign(), RemoteActivity.SIGN_PUBSALE)){//竞拍
			action = TagConstant.AGENDA_ACTION_PUBSALE;
			memberid = member.getId();
			nickname = member.getNickname();
		}else if(member != null){//参加活动
			action = TagConstant.AGENDA_ACTION_JOIN_ACTIVITY;
			memberid = member.getId();
			nickname = member.getNickname();
		}else{//创建活动
			action = TagConstant.AGENDA_ACTION_CREATE_ACTIVITY;
			memberid = activity.getMemberid();
			nickname = activity.getMembername();
		}
		Map dataMap = new HashMap();
		if(StringUtils.isNotBlank(activity.getLogo())) dataMap.put("logo", activity.getLogo());
		String otherinfo = JsonUtils.writeObjectToJson(dataMap);
		return addAgenda(activity.getTitle(), memberid, nickname, activity.getStartdate(), activity.getStarttime(), activity.getTitle(), TagConstant.TAG_ACTIVITY, activity.getId(), null, null, DateUtil.getCurFullTimestamp(), action, activity.getId(), activity.getEnddate(), activity.getEndtime(), activity.getAddress(), otherinfo);
	}
	
	@Override 
	public Agenda addAgenda(String title, Long memberid, String membername, Date startdate, String starttime, String content, String tag, Long relatedid,
			String category, Long categoryid, Timestamp addtime, String action, Long actionid, Date enddate, String endtime, String address, String otherinfo){
		Agenda agenda = new Agenda();
		agenda.setTitle(title);
		agenda.setMemberid(memberid);
		agenda.setMembername(membername);
		agenda.setStartdate(startdate);
		agenda.setStarttime(starttime);
		agenda.setContent(content);
		agenda.setTag(tag);
		agenda.setRelatedid(relatedid);
		agenda.setCategory(category);
		agenda.setCategoryid(categoryid);
		agenda.setAddtime(addtime);
		agenda.setAction(action);
		agenda.setActionid(actionid);
		agenda.setEnddate(enddate);
		agenda.setEndtime(endtime);
		agenda.setAddress(address);
		agenda.setOtherinfo(otherinfo);
		return baseDao.saveObject(agenda);
	}
}
