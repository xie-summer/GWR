package com.gewara.web.action.admin.report;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import com.gewara.model.user.Member;
import com.gewara.model.user.MemberInfo;
import com.gewara.util.DateUtil;
import com.gewara.web.util.PageUtil;

@Controller
public class TicketCountAdminController extends CountController {
	@Autowired @Qualifier("jdbcTemplate")
	private JdbcTemplate jdbcTemplate;
	public void setJdbcTemplate(JdbcTemplate template) {
		jdbcTemplate = template;
	}
	@RequestMapping("/admin/datacount/ticket/memberCount.xhtml")
	public String memberCount(Timestamp starttime, Timestamp endtime, int type, Integer pageNo, ModelMap model){
		String url = "admin/datacount/ticket/memberCount.vm";
		if(starttime==null || endtime==null) return url;
		if(isInvalidTime(starttime, endtime)) return forwardErrorTime(model);
		if(pageNo==null) pageNo = 0;
		String sql = "select new map(t.memberid as memberid, max(t.addtime) as addtime, max(t.mobile) as mobile) ";
		sql += "from TicketOrder t where t.status='paid_success' and t.addtime>=? and t.addtime<=? and t.partnerid=1 group by t.memberid ";
		if(type==1) sql += "having count(*)=1";
		else sql += "having count(*)>1";
		List<Map> mapList = daoService.queryByRowsRange(sql, pageNo, 50, starttime, endtime);
		List<Member> memberList = new ArrayList<Member>();
		Map<Long, MemberInfo> infoMap = new HashMap<Long, MemberInfo>(); 
		Map<Long, Timestamp> timeMap = new HashMap<Long, Timestamp>();
		Map<Long, Integer> buynumMap = new HashMap<Long, Integer>();
		for(Map m : mapList){
			Long memberid = Long.valueOf(m.get("memberid")+"");
			Member member = daoService.getObject(Member.class, memberid);
			memberList.add(member);
			infoMap.put(memberid, daoService.getObject(MemberInfo.class, memberid));
			timeMap.put(memberid, (Timestamp)m.get("addtime"));
		}
		sql = "select sum(count(*)) from TicketOrder t where t.status='paid_success' and t.addtime>=? and t.addtime<=? and t.partnerid=1 group by t.memberid ";
		if(type==1) sql += "having count(*)=1";
		else sql += "having count(*)>1";
		List list = hibernateTemplate.find(sql, starttime, endtime);
		int count = Integer.parseInt(list.get(0)+"");
		PageUtil pageUtil = new PageUtil(count, 50, pageNo, "admin/datacount/ticket/memberCount.xhtml");
		Map params = new HashMap();
		params.put("starttime", DateUtil.formatTimestamp(starttime));
		params.put("endtime", DateUtil.formatTimestamp(endtime));
		params.put("type", type);
		pageUtil.initPageInfo(params);
		model.put("pageUtil", pageUtil);
		model.put("memberList", memberList);
		model.put("infoMap", infoMap);
		model.put("timeMap", timeMap);
		model.put("buynumMap", buynumMap);
		return url;
	}
	//TODO:移入报表
	@RequestMapping("/admin/datacount/ticket/activityCount.xhtml")
	public String activityCount(Timestamp starttime, Timestamp endtime,
			ModelMap model) {
		String url = "admin/datacount/ticket/activityCount.vm";
		if(starttime==null || endtime==null) return url;
		if(isInvalidTime(starttime, endtime)) return forwardErrorTime(model);
		String sql = "select (select g.goodsname from WEBDATA.goods g where g.recordid=t.relatedid) as 活动名称, ";
				sql += "(select g.unitprice from WEBDATA.goods g where g.recordid=t.relatedid) as 卖价, ";
				sql += "count(*) 总订单数, sum(t.quantity) as 总份数, sum(t.amount) as 总销售额 ";
				sql += "from WEBDATA.ticket_order t, goods g where t.order_type='goods' and t.status='paid_success' ";
				sql += "and t.addtime>=? and t.addtime<=? ";
				sql += "and g.tag='activity' and t.relatedid=g.recordid ";
				sql += "group by t.relatedid";
		List<Map<String, Object>> qryMapList = jdbcTemplate.queryForList(sql, starttime, endtime);
		model.put("qryMapList", qryMapList);
		model.put("starttime", starttime);
		model.put("endtime", endtime);
		return url;
	}
}
