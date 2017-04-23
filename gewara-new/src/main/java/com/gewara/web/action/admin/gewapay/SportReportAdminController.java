package com.gewara.web.action.admin.gewapay;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import com.gewara.constant.OpenTimeTableConstant;
import com.gewara.constant.ticket.OrderConstant;
import com.gewara.model.sport.OpenTimeTable;
import com.gewara.model.sport.Sport;
import com.gewara.model.sport.SportItem;
import com.gewara.service.sport.SportService;
import com.gewara.util.DateUtil;
import com.gewara.web.action.admin.BaseAdminController;
@Controller
public class SportReportAdminController  extends BaseAdminController{
	@Autowired@Qualifier("sportService")
	private SportService sportService;
	// 场馆场次数据分析
	@RequestMapping("/admin/gewapay/sportOpenTimeItembyAnalysis.xhtml")
	public String SportOpenTimeItemAnalysis(Long sportid, Long itemid, Timestamp datefrom, Timestamp dateto, ModelMap model) {
		String url = "admin/gewapay/report/sportOpenTimeItemAnalysis.vm";
		getSportData(model);
		List<SportItem> itemlist = null;
		if (sportid == null)
			itemlist = daoService.getAllObjects(SportItem.class);
		else
			itemlist = sportService.getSportItemListBySportId(sportid, null);
		model.put("itemlist", itemlist);
		model.put("sportid", sportid);
		model.put("itemid", itemid);
		if (datefrom == null || dateto == null)
			return url;
		DetachedCriteria qry = DetachedCriteria.forClass(OpenTimeTable.class);
		qry.add(Restrictions.ge("playdate", datefrom));
		qry.add(Restrictions.le("playdate", dateto));
		qry.add(Restrictions.eq("status", OpenTimeTableConstant.STATUS_BOOK));
		qry.add(Restrictions.eq("rstatus", "Y"));
		qry.add(Restrictions.ge("playdate", datefrom));
		if (itemid != null)
			qry.add(Restrictions.eq("itemid", itemid));
		if (sportid != null)
			qry.add(Restrictions.eq("sportid", sportid));
		qry.addOrder(Order.asc("playdate"));
		List<OpenTimeTable> openTimeTableList = hibernateTemplate.findByCriteria(qry);
		model.put("openTimeTableList", openTimeTableList);
		// String
		// hql="select new map(ott.id as id,ott.sportname as sportname,ott.itemname as itemname,ott.playdate as playdate,count())from OpenTimeTable ott,OpenTimeItem oti where ott.id=oti.ottid,ott.status=? and ott.rstatus=? and ott.playdate>=? and ott.playdate<=? group by ott.id,ott.sportname,ott.itemname,ott.playdate";
		return url;
	}

	// 按场馆下单统计（下单时间）
	@RequestMapping("/admin/gewapay/reportBySportAddtime.xhtml")
	public String ReportBySportAddtime(Long sportid, Timestamp datefrom, Timestamp dateto, ModelMap model) {
		getSportData(model);
		if (datefrom == null || dateto == null)
			return "admin/gewapay/report/sportReportByAddtime.vm";

		String hql = "select new map(ott.id as ottid, ott.playdate as playdate,"
				+ "ott.itemid as itemid,count(t.id) as ids,sum(t.quantity) as qiamtity,sum(t.costprice*t.quantity) as costprice) from SportOrder t, OpenTimeTable ott ";
		List<Map> dataMap = new ArrayList<Map>();
		if (sportid == null) {
			hql = hql
					+ "where t.ottid=ott.id and t.status=? and t.addtime>=? and t.addtime<=? group by ott.id,ott.playdate,ott.itemid order by ott.playdate";
			dataMap = hibernateTemplate.find(hql, OrderConstant.STATUS_PAID_SUCCESS, DateUtil.getBeginningTimeOfDay(datefrom),
					DateUtil.getLastTimeOfDay(dateto));
		} else {
			hql = hql
					+ "where t.ottid=ott.id and t.status=? and t.sportid=? and t.addtime>=? and t.addtime<=? group by ott.id,ott.playdate,ott.itemid order by ott.playdate";
			dataMap = hibernateTemplate.find(hql, OrderConstant.STATUS_PAID_SUCCESS, sportid, DateUtil.getBeginningTimeOfDay(datefrom),
					DateUtil.getLastTimeOfDay(dateto));
			Sport sport = daoService.getObject(Sport.class, sportid);
			model.put("sport", sport);
		}

		Map<Long, String> sportnameMap = new HashMap<Long, String>();
		long orderCount = 0;
		long costpricecount = 0;
		long ticketcount = 0;
		for (Map map : dataMap) {
			Long itemid = (Long) map.get("itemid");
			long costprice = (Long) map.get("costprice");
			costpricecount = costpricecount + costprice;
			long ids = (Long) map.get("ids");
			orderCount = orderCount + ids;
			long qiamtity = (Long) map.get("qiamtity");
			ticketcount = ticketcount + qiamtity;
			sportnameMap.put(itemid, getItemName(itemid));
		}
		model.put("sportnameMap", sportnameMap);
		model.put("orderCount", orderCount);
		model.put("costpricecount", costpricecount);
		model.put("ticketcount", ticketcount);
		model.put("dataMap", dataMap);
		model.put("sportid", sportid);
		return "admin/gewapay/report/sportReportByAddtime.vm";
	}
	// 按场馆时间统计
	@RequestMapping("/admin/gewapay/reportSportByDate.xhtml")
	public String reportSportByDate(Date datefrom, Date dateto, ModelMap model) {
		getSportData(model);
		String hql = "select new map(ott.playdate as playdate,ott.sportid as sportid, "
				+ "count(t.id) as ids,sum(t.quantity) as qiamtity,sum(t.costprice*t.quantity) as costprice) from SportOrder t, OpenTimeTable ott "
				+ "where t.ottid=ott.id and t.status=? and ott.playdate>=? and ott.playdate<=? ";
		hql = hql + "group by ott.playdate,ott.sportid order by ott.playdate,ott.sportid";
		List<Map> dataMap = new ArrayList<Map>();
		dataMap = hibernateTemplate.find(hql, OrderConstant.STATUS_PAID_SUCCESS, DateUtil.getBeginningTimeOfDay(datefrom),
				DateUtil.getLastTimeOfDay(dateto));
		Map<Long, Sport> sportMap = new HashMap<Long, Sport>();
		Map<Long, List<Map>> cdMap = new HashMap<Long, List<Map>>();
		for (Map map : dataMap) {
			Long sportid = (Long) map.get("sportid");
			sportMap.put(sportid, daoService.getObject(Sport.class, sportid));
			if (!cdMap.containsKey(sportid)) {
				List<Map> list = new ArrayList<Map>();
				list.add(map);
				cdMap.put(sportid, list);
			} else {
				List<Map> list = cdMap.get(sportid);
				list.add(map);
			}
		}
		model.put("dataMap", dataMap);
		model.put("cdMap", cdMap);
		model.put("sportMap", sportMap);
		return "admin/gewapay/report/reportSportByDate.vm";
	}
	// 按运动场次统计
	@RequestMapping("/admin/gewapay/reportSportByOpi.xhtml")
	public String sportReportByOpi(Long sportid, Date datefrom, Date dateto, ModelMap model) {
		getSportData(model);
		if (datefrom == null || dateto == null)
			return "admin/gewapay/report/sportReportByOpi.vm";
		if (sportid == null) {
			model.put("datefrom", DateUtil.format(datefrom, "yyyy-MM-dd"));
			model.put("dateto", DateUtil.format(dateto, "yyyy-MM-dd"));
			return "redirect:/admin/gewapay/reportSportByDate.xhtml";
		}
		Sport sport = daoService.getObject(Sport.class, sportid);
		// WMSYS.WM_CONCAT
		String hql = "select new map(ott.id as ottid, ott.playdate as playdate,"
				+ "ott.itemid as itemid,count(t.id) as ids,sum(t.quantity) as qiamtity,sum(t.costprice*t.quantity) as costprice) from SportOrder t, OpenTimeTable ott "
				+ "where t.ottid=ott.id and t.status=? and t.sportid=? and ott.playdate>=? and ott.playdate<=? ";
		hql = hql + "group by ott.id,ott.playdate,ott.itemid order by ott.playdate";
		List<Map> dataMap = new ArrayList<Map>();

		dataMap = hibernateTemplate.find(hql, OrderConstant.STATUS_PAID_SUCCESS, sportid, DateUtil.getBeginningTimeOfDay(datefrom),
				DateUtil.getLastTimeOfDay(dateto));

		Map<Long, String> sportnameMap = new HashMap<Long, String>();
		long orderCount = 0;
		long costpricecount = 0;
		long ticketcount = 0;
		for (Map map : dataMap) {
			Long itemid = (Long) map.get("itemid");
			long costprice = (Long) map.get("costprice");
			costpricecount = costpricecount + costprice;
			long ids = (Long) map.get("ids");
			orderCount = orderCount + ids;
			long qiamtity = (Long) map.get("qiamtity");
			ticketcount = ticketcount + qiamtity;
			sportnameMap.put(itemid, getItemName(itemid));
		}
		model.put("sportnameMap", sportnameMap);
		model.put("orderCount", orderCount);
		model.put("costpricecount", costpricecount);
		model.put("ticketcount", ticketcount);
		model.put("dataMap", dataMap);
		model.put("sport", sport);
		model.put("sportid", sportid);
		return "admin/gewapay/report/sportReportByOpi.vm";
	}
	private void getSportData(ModelMap model) {
		String sportHql = "select new map(s.id as sportid, s.name as sportname) from "
				+ "Sport s where s.id in (select sp.id from SportProfile sp where sp.booking=?)";
		List<Map> sportList = hibernateTemplate.find(sportHql, Sport.BOOKING_OPEN);
		model.put("sportList", sportList);
	}

	private String getItemName(Long itemid) {
		String hql = "select itemname from SportItem where id=?";
		List<String> list = hibernateTemplate.find(hql, itemid);
		if (list.isEmpty())
			return "";
		return list.get(0);
	}

}
