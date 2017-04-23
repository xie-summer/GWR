package com.gewara.web.action.admin.report;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import com.gewara.constant.AdminCityContant;
//TODO:移入报表？？
@Controller
public class MemberCountController  extends CountController{
	@Autowired @Qualifier("jdbcTemplate")
	private JdbcTemplate jdbcTemplate;
	public void setJdbcTemplate(JdbcTemplate template) {
		jdbcTemplate = template;
	}
	@RequestMapping("/admin/datacount/member/toSmsPlatform.xhtml")
	public String toSmsPlatform(Timestamp starttime, Timestamp endtime, ModelMap model) {
		String url = "redirect:/admin/message/getMessageTask.xhtml";
		if(starttime==null || endtime==null) return url;
		if(isInvalidTime(starttime, endtime)) return forwardErrorTime(model);
		String sql = "select m.recordid as id,m.mobile as mobile from WEBDATA.member m, memberinfo mi where m.recordid = mi.recordid and m.mobile is not null and mi.addtime>=? and not exists(select t.memberid from WEBDATA.ticket_order t " +
							"where t.memberid=m.recordid and t.status='paid_success' and t.addtime>=? and t.addtime<=?) ";
		List<Map<String,Object>> list = jdbcTemplate.queryForList(sql, starttime, starttime, endtime);
		List<Long> idList = new ArrayList<Long>();
		List<String> mobileList = new ArrayList<String>();
		for(Map<String,Object> map : list){
			idList.add(new Long(map.get("id").toString()));
			mobileList.add(map.get("mobile").toString());
		}
		model.put("idList", StringUtils.join(idList, ","));
		model.put("mobileList", StringUtils.join(mobileList, ","));
		return url;	
	}
	@RequestMapping("/admin/datacount/member/shareCount.xhtml")
	public String share(Timestamp starttime, Timestamp endtime,  ModelMap model){
		String url = "admin/datacount/member/shareCount.vm";
		if (starttime == null || endtime==null) return url;
		if(isInvalidTime(starttime, endtime)) return forwardErrorTime(model);
		String sql = "select to_char(s.addtime, 'yyyy-MM-dd') as 日期, count(*) as 共计,"; 
		sql = sql + "sum(case when s.tag='ticketorder' then 1 else 0 end) as 购票,";
		sql = sql + "sum(case when s.tag='activity' then 1 else 0 end) as 活动,";
		sql = sql + "sum(case when s.tag='topic' then 1 else 0 end) as 帖子,";
		sql = sql + "sum(case when s.tag='moviecomment' then 1 else 0 end) as 影评,";
		sql = sql + "sum(case when s.tag='dramacomment' then 1 else 0 end) as 剧评,";
		sql = sql + "sum(case when s.tag='agenda' then 1 else 0 end) as 生活,";
		sql = sql + "sum(case when s.tag='wala' then 1 else 0 end) as 哇啦 ";
		sql = sql + "from WEBDATA.shares s where s.type='sina' " ;
		sql = sql + "and s.addtime>=? and s.addtime<=? ";
		sql = sql + "group by to_char(s.addtime, 'yyyy-MM-dd') having count(*)>0 ";
		sql = sql + "order by to_char(s.addtime, 'yyyy-MM-dd')";
		List<Map<String, Object>> qryMapList = jdbcTemplate.queryForList(sql, starttime, endtime);
		model.put("qryMapList", qryMapList);
		return url;
	}
	
	@RequestMapping("/admin/datacount/member/pointCount.xhtml")
	public String pointCount(String citycode, Integer pointmin,Integer pointmax, ModelMap model) {
		if(citycode == null) citycode = "000000";	
		if(pointmin==null) pointmin=500;
		String sql = "select new map(count(*) as count, sum(mi.pointvalue) as sumpoint) from MemberInfo mi where mi.pointvalue>=?";
		List params = new ArrayList();
		params.add(pointmin);
		if(StringUtils.equals(AdminCityContant.CITYCODE_ALL,citycode)){
			sql += " and mi.fromcity = ? ";
			params.add(citycode);
		}
		if(pointmax!=null){
			sql += " and mi.pointvalue<= ? ";
			params.add(pointmax);
		}
		List<Map> memberList = daoService.queryByRowsRange(sql, 0, 1, params.toArray());
		model.put("cityMap", AdminCityContant.getCitycode2CitynameMap());
		Map result = new HashMap();
		if(!memberList.isEmpty()){
			result = memberList.get(0);
		}
		model.put("result", result);
		model.put("pointmin", pointmin);
		model.put("pointmax", pointmax);
		return "admin/datacount/member/pointCount.vm";		
	}
}
