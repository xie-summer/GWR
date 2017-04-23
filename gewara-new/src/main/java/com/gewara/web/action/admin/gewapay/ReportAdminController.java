package com.gewara.web.action.admin.gewapay;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.CriteriaSpecification;
import org.hibernate.type.IntegerType;
import org.hibernate.type.StringType;
import org.hibernate.type.TimestampType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.support.PropertyComparator;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.alibaba.dubbo.common.utils.CollectionUtils;
import com.gewara.constant.AdminCityContant;
import com.gewara.constant.GoodsConstant;
import com.gewara.constant.SmsConstant;
import com.gewara.constant.content.FilmFestConstant;
import com.gewara.constant.sys.MongoData;
import com.gewara.constant.ticket.OrderConstant;
import com.gewara.model.movie.Cinema;
import com.gewara.model.movie.Movie;
import com.gewara.model.movie.SpecialActivity;
import com.gewara.model.pay.GoodsOrder;
import com.gewara.model.pay.SportOrder;
import com.gewara.model.pay.TicketOrder;
import com.gewara.model.ticket.OpenPlayItem;
import com.gewara.mongo.MongoService;
import com.gewara.service.movie.FilmFestService;
import com.gewara.support.ErrorCode;
import com.gewara.untrans.terminal.TerminalService;
import com.gewara.util.BeanUtil;
import com.gewara.util.DateUtil;
import com.gewara.web.action.admin.BaseAdminController;
import com.gewara.web.util.PageUtil;
import com.gewara.xmlbind.terminal.TakeInfo;

@Controller
public class ReportAdminController extends BaseAdminController {
	@Autowired @Qualifier("jdbcTemplate")
	private JdbcTemplate jdbcTemplate;
	public void setJdbcTemplate(JdbcTemplate template) {
		jdbcTemplate = template;
	}
	@Autowired@Qualifier("mongoService")
	private MongoService mongoService;
	@Autowired@Qualifier("filmFestService")
	private FilmFestService filmFestService;
	@Autowired@Qualifier("terminalService")
	private TerminalService terminalService;


	@RequestMapping("/admin/gewapay/reportByGewa.xhtml")
	public String gewaReportByOpi(Long cinemaid, Date datefrom, Date dateto, String opentype, HttpServletRequest request, ModelMap model) {
		String citycode = getAdminCitycode(request);
		getCityData(citycode, model);
		if (datefrom == null || dateto == null)
			return "admin/gewapay/report/reportByGewa.vm";
		if (cinemaid == null) {
			model.put("datefrom", DateUtil.format(datefrom, "yyyy-MM-dd"));
			model.put("dateto", DateUtil.format(dateto, "yyyy-MM-dd"));
			model.put("opentype", opentype);
			return "redirect:/admin/gewapay/reportByGroupDate.xhtml";
		}
		Cinema cinema = daoService.getObject(Cinema.class, cinemaid);
		String hql = "select new map(opi.mpid as mpid, sum(t.quantity) as quantity,"
				+ "sum(t.totalfee) as totalfee, sum(t.gewapaid+t.alipaid) as paid, "
				+ "sum(t.discount) as discount, opi.moviename as moviename, opi.playtime as playtime,"
				+ "opi.roomname as roomname, opi.gewaprice as gewaprice) from TicketOrder t, OpenPlayItem opi "
				+ "where t.status=? and t.mpid=opi.mpid and opi.cinemaid=? and t.cinemaid=? and opi.playtime>=? and opi.playtime<=? ";
		if (StringUtils.isNotBlank(opentype))
			hql = hql + "and opi.opentype=? ";
		hql = hql + "group by opi.mpid, opi.moviename, opi.playtime, opi.roomname, opi.gewaprice order by opi.playtime";
		List<Map> dataMap = new ArrayList<Map>();
		if (StringUtils.isNotBlank(opentype))
			dataMap = hibernateTemplate.find(hql, OrderConstant.STATUS_PAID_SUCCESS, cinemaid, cinemaid, DateUtil.getBeginningTimeOfDay(datefrom),
					DateUtil.getLastTimeOfDay(dateto), opentype);
		else
			dataMap = hibernateTemplate.find(hql, OrderConstant.STATUS_PAID_SUCCESS, cinemaid, cinemaid, DateUtil.getBeginningTimeOfDay(datefrom),
					DateUtil.getLastTimeOfDay(dateto));
		model.put("dataMap", dataMap);
		model.put("cinema", cinema);
		return "admin/gewapay/report/reportByGewa.vm";
	}
	@RequestMapping("/admin/gewapay/reportByGroupDate.xhtml")
	public String reportByGroupDate(Date datefrom, Date dateto, String opentype, HttpServletRequest request, ModelMap model) {
		String citycode = getAdminCitycode(request);
		getCityData(citycode, model);
		String hql = "select new map(opi.cinemaid as cinemaid, to_char(opi.playtime, 'yyyy-MM-dd') as playdate, sum(t.quantity) as quantity, "
				+ "sum(t.quantity) as totalquantity, count(*) as count, " + "sum(t.totalfee) as totalfee, sum(t.gewapaid+t.alipaid) as paid, "
				+ "sum(t.discount) as discount) " + "from TicketOrder t, OpenPlayItem opi "
				+ "where t.status=? and t.mpid=opi.mpid and opi.playtime>=? and opi.playtime<=? ";
		if (StringUtils.isNotBlank(opentype))
			hql = hql + "and opi.opentype=? ";
		hql = hql + "group by opi.cinemaid, to_char(opi.playtime, 'yyyy-MM-dd') order by opi.cinemaid, to_char(opi.playtime, 'yyyy-MM-dd')";
		List<Map> dataMap = new ArrayList<Map>();
		if (StringUtils.isNotBlank(opentype))
			dataMap = hibernateTemplate.find(hql, OrderConstant.STATUS_PAID_SUCCESS, DateUtil.getBeginningTimeOfDay(datefrom),
					DateUtil.getLastTimeOfDay(dateto), opentype);
		else
			dataMap = hibernateTemplate.find(hql, OrderConstant.STATUS_PAID_SUCCESS, DateUtil.getBeginningTimeOfDay(datefrom),
					DateUtil.getLastTimeOfDay(dateto));
		Map<Long, Cinema> cinemaMap = new HashMap<Long, Cinema>();
		Map<Long, List<Map>> cdMap = new HashMap<Long, List<Map>>();
		for (Map map : dataMap) {
			Long cid = (Long) map.get("cinemaid");
			if (!cinemaMap.containsKey(cid)) {
				cinemaMap.put(cid, daoService.getObject(Cinema.class, cid));
			}
			if (!cdMap.containsKey(cid)) {
				List<Map> t = new ArrayList<Map>();
				t.add(map);
				cdMap.put(cid, t);
			} else {
				List<Map> t = cdMap.get(cid);
				t.add(map);
			}
		}
		model.put("dataMap", dataMap);
		model.put("cdMap", cdMap);
		model.put("cinemaMap", cinemaMap);
		return "admin/gewapay/report/reportByGroupDate.vm";
	}

	// 按影院统计（按开放场次时间统计）
	@RequestMapping("/admin/gewapay/cinemaReport.xhtml")
	public String reportCM(Long cinemaid, Date datefrom, Date dateto, String opentype, HttpServletRequest request, ModelMap model) {
		String citycode = getAdminCitycode(request);
		getCityData(citycode, model);
		if (datefrom == null || dateto == null)
			return "admin/gewapay/report/cinemaReport.vm";
		Cinema cinema = daoService.getObject(Cinema.class, cinemaid);
		List<Movie> movieList = new ArrayList<Movie>();
		Map<String, Integer> quantityMap = new HashMap<String, Integer>();
		String hql = "select new map(opi.movieid as movieid, opi.costprice as costprice, sum(t.quantity) as quantity) from TicketOrder t, OpenPlayItem opi "
				+ "where t.status=? and t.mpid=opi.mpid and opi.cinemaid=? and t.cinemaid=? and opi.playtime>=? and opi.playtime<=? ";
		if (StringUtils.isNotBlank(opentype))
			hql = hql + "and opi.opentype=? ";
		hql = hql + "group by opi.movieid,opi.costprice";
		List<Map> list = new ArrayList<Map>();
		if (StringUtils.isNotBlank(opentype))
			list = hibernateTemplate.find(hql, OrderConstant.STATUS_PAID_SUCCESS, cinemaid, cinemaid, DateUtil.getBeginningTimeOfDay(datefrom),
					DateUtil.getLastTimeOfDay(dateto), opentype);
		else
			list = hibernateTemplate.find(hql, OrderConstant.STATUS_PAID_SUCCESS, cinemaid, cinemaid, DateUtil.getBeginningTimeOfDay(datefrom),
					DateUtil.getLastTimeOfDay(dateto));
		Movie movie = null;
		Integer costprice = null;
		Set costpriceList = new TreeSet();
		Map priceCountMap = new HashMap();
		for (Map entry : list) {
			costpriceList.add(entry.get("costprice"));
			Long quantity = (Long) priceCountMap.get(entry.get("costprice"));
			if (quantity == null)
				quantity = 0L;
			quantity += (Long) entry.get("quantity");
			priceCountMap.put(entry.get("costprice"), quantity);
			if (entry.get("movieid") != null) {
				movie = daoService.getObject(Movie.class, new Long(entry.get("movieid") + ""));
				if (!movieList.contains(movie))
					movieList.add(movie);
			}
			if (movie != null) {
				if (entry.get("quantity") != null)
					quantityMap.put(movie.getId() + "_" + entry.get("costprice"), new Integer(entry.get("quantity") + ""));
				else
					quantityMap.put(movie.getId() + "_" + costprice, null);
			}
		}
		model.put("cinema", cinema);
		model.put("priceCountMap", priceCountMap);
		model.put("movieList", movieList);
		model.put("quantityMap", quantityMap);
		model.put("costpriceList", costpriceList);
		return "admin/gewapay/report/cinemaReport.vm";
	}

	// 按影院统计(按播放时间)
	@RequestMapping("/admin/gewapay/reportByDate.xhtml")
	public String cinemaReportByOpi(Date datefrom, Date dateto, String opentype, HttpServletRequest request, ModelMap model) {
		String citycode = getAdminCitycode(request);
		getCityData(citycode, model);
		String hql = "select new map(opi.cinemaid as cinemaid, to_char(opi.playtime, 'yyyy-MM-dd') as playdate, "
				+ "sum(t.quantity) as totalquantity, sum(t.costprice*t.quantity) as totalamount, count(*) as count) "
				+ "from TicketOrder t, OpenPlayItem opi " + "where t.mpid=opi.mpid and t.status=? and opi.playtime>=? and opi.playtime<=? ";
		if (StringUtils.isNotBlank(opentype))
			hql = hql + "and opi.opentype=? ";
		hql = hql + "group by opi.cinemaid,to_char(opi.playtime, 'yyyy-MM-dd') order by opi.cinemaid, to_char(opi.playtime, 'yyyy-MM-dd')";
		List<Map> dataMap = new ArrayList<Map>();
		if (StringUtils.isNotBlank(opentype))
			dataMap = hibernateTemplate.find(hql, OrderConstant.STATUS_PAID_SUCCESS, DateUtil.getBeginningTimeOfDay(datefrom),
					DateUtil.getLastTimeOfDay(dateto), opentype);
		else
			dataMap = hibernateTemplate.find(hql, OrderConstant.STATUS_PAID_SUCCESS, DateUtil.getBeginningTimeOfDay(datefrom),
					DateUtil.getLastTimeOfDay(dateto));
		Map<Long, Cinema> cinemaMap = new HashMap<Long, Cinema>();
		Map<Long, List<Map>> cdMap = new HashMap<Long, List<Map>>();
		List<String> strdateList = new ArrayList<String>();
		List<String> xtList = new ArrayList<String>();
		Date tmp = datefrom;
		while (tmp.compareTo(dateto) <= 0) {
			strdateList.add(DateUtil.formatDate(tmp));
			tmp = DateUtil.addDay(tmp, 1);
		}
		for (Map map : dataMap) {
			Long cid = (Long) map.get("cinemaid");
			if (!cinemaMap.containsKey(cid)) {
				cinemaMap.put(cid, daoService.getObject(Cinema.class, cid));
			}
			xtList.add(cid + "" + map.get("playdate"));
			if (!cdMap.containsKey(cid)) {
				List<Map> t = new ArrayList<Map>();
				t.add(map);
				cdMap.put(cid, t);
			} else {
				List<Map> t = cdMap.get(cid);
				t.add(map);
			}
		}
		for (String strdate : strdateList) {
			for (Long cinemaid : cdMap.keySet()) {
				if (!xtList.contains(cinemaid + "" + strdate)) {
					Map temp = new HashMap();
					temp.put("cinemaid", cinemaid);
					temp.put("playdate", strdate);
					temp.put("totalquantity", 0);
					temp.put("totalamount", 0);
					temp.put("count", 0);
					cdMap.get(cinemaid).add(temp);
				}
			}
		}

		model.put("dataMap", dataMap);
		model.put("cdMap", cdMap);
		model.put("cinemaMap", cinemaMap);
		model.put("strdateList", strdateList);
		return "admin/gewapay/report/reportByDate.vm";
	}

	// 根据场次ID查询
	@RequestMapping("/admin/gewapay/reportOpiDetail.xhtml")
	public String reportOpiDetail(Long mpid, ModelMap model) {
		String hql = "from TicketOrder t where t.mpid=? and t.status=? order by paidtime";
		List<TicketOrder> orderList = hibernateTemplate.find(hql, new Object[] { mpid, OrderConstant.STATUS_PAID_SUCCESS });
		model.put("orderList", orderList);
		return "admin/gewapay/report/reportOpiDetail.vm";
	}

	// 按影院统计（下单时间）
	@RequestMapping("/admin/gewapay/reportByCinemaAddtime.xhtml")
	public String cinemaReportByTicketAddtime(Long cinemaid, Timestamp datefrom, Timestamp dateto, String opentype, 
			HttpServletRequest request, ModelMap model) {
		String citycode = getAdminCitycode(request);
		getCityData(citycode, model);
		if (datefrom == null || dateto == null)
			return "admin/gewapay/report/cinemaReportByAddtime.vm";
		Cinema cinema = daoService.getObject(Cinema.class, cinemaid);
		String hql = "select new map(opi.mpid as mpid, opi.moviename as moviename, opi.roomname as roomname,"
				+ "t.costprice as costprice, opi.playtime as playtime, t.tradeNo as tradeNo, t.hfhpass as hfhpass,"
				+ "t.quantity as quantity, t.totalfee as totalfee, t.addtime as addtime) " + "from TicketOrder t, OpenPlayItem opi "
				+ "where t.mpid=opi.mpid and t.status=? and t.cinemaid=? and t.addtime>=? and t.addtime<=? ";
		if (StringUtils.isNotBlank(opentype))
			hql = hql + "and opi.opentype=? ";
		hql = hql + "order by t.addtime asc";
		List<Map> dataMap = new ArrayList<Map>();
		if (StringUtils.isNotBlank(opentype))
			dataMap = hibernateTemplate.find(hql, OrderConstant.STATUS_PAID_SUCCESS, cinemaid, datefrom, dateto, opentype);
		else
			dataMap = hibernateTemplate.find(hql, OrderConstant.STATUS_PAID_SUCCESS, cinemaid, datefrom, dateto);
		model.put("dataMap", dataMap);
		model.put("cinema", cinema);
		model.put("curtime", new Timestamp(System.currentTimeMillis()));
		return "admin/gewapay/report/cinemaReportByAddtime.vm";
	}

	// 按电影统计
	@RequestMapping("/admin/gewapay/reportByMovie.xhtml")
	public String movieReportByOpi(ModelMap model, Date datefrom, Date dateto, String opentype) {
		if (datefrom == null || dateto == null)
			return "admin/gewapay/report/reportByMovie.vm";
		String hql = "select new map(opi.movieid as movieid,sum(t.quantity) as quantity) from TicketOrder t, OpenPlayItem opi where t.mpid=opi.mpid and t.status=? and opi.playtime>=? and opi.playtime<=? ";
		if (StringUtils.isNotBlank(opentype))
			hql = hql + "and opi.opentype=? ";
		hql = hql + "group by opi.movieid order by sum(t.quantity) desc";
		List<Map> dataMap = new ArrayList<Map>();
		if (StringUtils.isNotBlank(opentype))
			dataMap = hibernateTemplate.find(hql, OrderConstant.STATUS_PAID_SUCCESS, DateUtil.getBeginningTimeOfDay(datefrom),
					DateUtil.getLastTimeOfDay(dateto), opentype);
		else
			dataMap = hibernateTemplate.find(hql, OrderConstant.STATUS_PAID_SUCCESS, DateUtil.getBeginningTimeOfDay(datefrom),
					DateUtil.getLastTimeOfDay(dateto));
		Map<Movie, Integer> mountMap = new LinkedHashMap<Movie, Integer>();
		Movie movie = null;
		for (Map entry : dataMap) {
			movie = daoService.getObject(Movie.class, new Long(entry.get("movieid") + ""));
			if (movie != null)
				mountMap.put(movie, new Integer(entry.get("quantity") + ""));
		}
		model.put("mountMap", mountMap);
		model.put("datefrom", datefrom);
		model.put("dateto", dateto);
		model.put("opentype", opentype);
		return "admin/gewapay/report/reportByMovie.vm";
	}

	@RequestMapping("/admin/gewapay/reportMovieDetail.xhtml")
	public String movieRemortDetail(ModelMap model, Long movieid, Date datefrom, Date dateto, String opentype) {
		if (movieid == null){
			return "admin/gewapay/report/movieReportDetail.vm";
		}
		Movie movie = daoService.getObject(Movie.class, movieid);
		String hql = " select new map(opi.movieid as movieid, opi.cinemaid as cinemaid, opi.mpid as mpid, opi.playtime as playtime, sum(t.quantity) as quantity) from TicketOrder t, OpenPlayItem opi where t.mpid=opi.mpid and t.status =? and opi.playtime>=? and opi.playtime<=? ";
		if (StringUtils.isNotBlank(opentype))
			hql = hql + "and opi.opentype=? ";
		hql = hql + "and opi.movieid =? group by opi.movieid, opi.cinemaid, opi.playtime, opi.mpid order by opi.playtime desc ";
		List<Map> dataMap = new ArrayList<Map>();
		if (StringUtils.isNotBlank(opentype))
			dataMap = hibernateTemplate.find(hql, OrderConstant.STATUS_PAID_SUCCESS, DateUtil.getBeginningTimeOfDay(datefrom),
					DateUtil.getLastTimeOfDay(dateto), opentype, movieid);
		else
			dataMap = hibernateTemplate.find(hql, OrderConstant.STATUS_PAID_SUCCESS, DateUtil.getBeginningTimeOfDay(datefrom),
					DateUtil.getLastTimeOfDay(dateto), movieid);
		for(Map map : dataMap){
			Long mid = Long.valueOf(map.get("movieid")+"");
			Long cid = Long.valueOf(map.get("cinemaid")+"");
			Movie m = daoService.getObject(Movie.class, mid);
			Cinema c = daoService.getObject(Cinema.class, cid);
			map.put("moviename", m.getRealBriefname());
			map.put("cinemaname", c.getRealBriefname());
		}
		model.put("dataMap", dataMap);
		model.put("movie", movie);
		return "admin/gewapay/report/reportMovieDetail.vm";
	}

	// 电影订单详细
	@RequestMapping("/admin/gewapay/reportDetail.xhtml")
	public String opiReport(Long mpid, ModelMap model) {
		OpenPlayItem opi = daoService.getObjectByUkey(OpenPlayItem.class, "mpid", mpid, false);
		String hql = "from TicketOrder t where t.mpid=? and t.status=? order by paidtime";
		List<TicketOrder> orderList = hibernateTemplate.find(hql, new Object[] { mpid, OrderConstant.STATUS_PAID_SUCCESS });
		model.put("orderList", orderList);
		model.put("opi", opi);
		return "admin/gewapay/report/reportDetail.vm";
	}

	// 运动订单详细
	@RequestMapping("/admin/gewapay/reportSportDetail.xhtml")
	public String reportSportDetail(Long ottid, ModelMap model) {
		String hql = "from SportOrder t where t.ottid=? and t.status=? order by t.paidtime";
		List<SportOrder> orderList = hibernateTemplate.find(hql, new Object[] { ottid, OrderConstant.STATUS_PAID_SUCCESS });
		model.put("orderList", orderList);
		return "admin/gewapay/report/reportSportDetail.vm";
	}

	@RequestMapping("/admin/gewapay/reportByMovieAndCostprice.xhtml")
	public String reportCMByAddtime(Long cinemaid, Timestamp datefrom, Timestamp dateto, String opentype, HttpServletRequest request, ModelMap model) {
		String citycode = getAdminCitycode(request);
		getCityData(citycode, model);
		if (cinemaid == null)
			return forwardMessage(model, "请选择影院！");
		if (datefrom == null || dateto == null)
			return "admin/gewapay/report/reportByMovieAndCostprice.vm";
		Cinema cinema = daoService.getObject(Cinema.class, cinemaid);
		List<Movie> movieList = new ArrayList<Movie>();
		Map<String, Integer> quantityMap = new HashMap<String, Integer>();
		String hql = "select new map(opi.movieid as movieid, opi.costprice as costprice, sum(t.quantity) as quantity) from TicketOrder t, OpenPlayItem opi "
				+ "where t.status=? and t.mpid=opi.mpid and opi.cinemaid=? and t.cinemaid=? and t.addtime>=? and t.addtime<=? ";
		if (StringUtils.isNotBlank(opentype))
			hql = hql + "and opi.opentype=? ";
		hql = hql + "group by opi.movieid,opi.costprice";
		List<Map> list = new ArrayList<Map>();
		if (StringUtils.isNotBlank(opentype))
			list = hibernateTemplate.find(hql, OrderConstant.STATUS_PAID_SUCCESS, cinemaid, cinemaid, datefrom, dateto, opentype);
		else
			list = hibernateTemplate.find(hql, OrderConstant.STATUS_PAID_SUCCESS, cinemaid, cinemaid, datefrom, dateto);
		Movie movie = null;
		Integer costprice = null;
		Set costpriceList = new TreeSet();
		Map priceCountMap = new HashMap();
		for (Map entry : list) {
			costpriceList.add(entry.get("costprice"));
			Long quantity = (Long) priceCountMap.get(entry.get("costprice"));
			if (quantity == null)
				quantity = 0L;
			quantity += (Long) entry.get("quantity");
			priceCountMap.put(entry.get("costprice"), quantity);
			if (entry.get("movieid") != null) {
				movie = daoService.getObject(Movie.class, new Long(entry.get("movieid") + ""));
				if (!movieList.contains(movie))
					movieList.add(movie);
			}
			if (movie != null) {
				if (entry.get("quantity") != null)
					quantityMap.put(movie.getId() + "_" + entry.get("costprice"), new Integer(entry.get("quantity") + ""));
				else
					quantityMap.put(movie.getId() + "_" + costprice, null);
			}
		}
		model.put("cinema", cinema);
		model.put("priceCountMap", priceCountMap);
		model.put("movieList", movieList);
		model.put("quantityMap", quantityMap);
		model.put("costpriceList", costpriceList);
		return "admin/gewapay/report/reportByMovieAndCostprice.vm";
	}
	private void getCityData(String citycode, ModelMap model) {
		String cinemaHql = "select new map(c.id as cinemaid, c.name as cinemaname) from "
				+ "Cinema c where c.citycode=? and c.id in (select p.id from CinemaProfile p)";
		List<Map> cinemaList = hibernateTemplate.find(cinemaHql, citycode);
		model.put("cinemaList", cinemaList);
	}


	@RequestMapping("/admin/gewapay/reportByHour.xhtml")
	public String reportByHour(Timestamp starttime, Timestamp endtime, ModelMap model) {
		if (starttime == null)
			return "admin/gewapay/report/reportByHour.vm";
		if (endtime == null)
			endtime = new Timestamp(System.currentTimeMillis());
		String qry = "select new map(to_char(t.addtime,'hh24') as hh, count(*) as count) from TicketOrder t "
				+ "where t.status=? and t.addtime>=? and t.addtime<? " + "group by to_char(t.addtime,'hh24') order by to_char(t.addtime,'hh24')";
		List<Map> dataList = hibernateTemplate.find(qry, OrderConstant.STATUS_PAID_SUCCESS, starttime, endtime);
		model.put("dataList", dataList);
		return "admin/gewapay/report/reportByHour.vm";
	}

	@RequestMapping("/admin/gewapay/reportByMeal.xhtml")
	public String meal(Long cid, Timestamp starttime, Timestamp endtime, ModelMap model) {
		List<Long> cinemaidList = hibernateTemplate.find("select distinct g.relatedid from Goods g where g.tag=?", GoodsConstant.GOODS_TAG_BMH);
		List<Cinema> cinemaList = daoService.getObjectList(Cinema.class, cinemaidList);
		model.put("cinemaList", cinemaList);
		if (cid == null || starttime == null || endtime == null)
			return "admin/gewapay/report/reportByMeal.vm";
		String qry = "from GoodsOrder o where o.status=? and o.addtime>=? and o.addtime<=?"
				+ "and exists(select g.id from Goods g where g.relatedid=? and g.tag=? and g.id=o.goodsid) order by o.addtime desc";
		List<GoodsOrder> goodsOrderList = hibernateTemplate.find(qry, OrderConstant.STATUS_PAID_SUCCESS, starttime, endtime, cid,
				GoodsConstant.GOODS_TAG_BMH);
		List<String> tradenoList = BeanUtil.getBeanPropertyList(goodsOrderList, String.class, "tradeNo", true);
		ErrorCode<List<TakeInfo>> tiCode = terminalService.getTakeInfoList(StringUtils.join(tradenoList, ","));
		if(!tiCode.isSuccess()){
			return forwardMessage(model, tiCode.getMsg());
		}
		List<TakeInfo> tiList = tiCode.getRetval();
		Map<String, TakeInfo> orderResultMap = BeanUtil.beanListToMap(tiList,"tradeno");
		model.put("orderList", goodsOrderList);
		model.put("cinema", daoService.getObject(Cinema.class, cid));
		model.put("orderResults", orderResultMap);
		return "admin/gewapay/report/reportByMeal.vm";
	}

	/**
	 * 当前时间前一个小时
	 * 
	 * @return
	 */
	private Date getOneHourAgo() {
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.HOUR, -1);// 前一小时
		return cal.getTime();
	}

	/**
	 * 获得当天的开始时间
	 * 
	 * @return
	 */
	private Date getStartTimeOfDay() {
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		return cal.getTime();

	}

	/**
	 * 当前时间
	 * 
	 * @return
	 */
	private Date getNowTime() {
		Calendar cal = Calendar.getInstance();
		return cal.getTime();
	}

	/**
	 * 监测当前时间(下单时间、场次时间)，全国的数据前10和后10的1小时城市、影院、出票量、销售金额、毛利润
	 * 销售金额=AMOUNT+ITEMFEE+OTHERFEE 毛利润=AMOUNT-COSTPRICE*QUANTITY
	 * 
	 * @return
	 */
	@RequestMapping("/admin/gewapay/monitorByCityAndCinema.xhtml")
	public String monitorByCityAndCinema(@RequestParam(defaultValue = "addtime", required = false, value = "timetype") String timetype, ModelMap model) {

		Date startTime = getStartTimeOfDay();
		Date currentTime = getNowTime();

		StringBuilder hql = new StringBuilder();
		hql.append("SELECT T.CITYCODE AS CITYCODE,");
		hql.append("OP.CINEMANAME  AS CINEMANAME,");
		hql.append("SUM(T.QUANTITY)AS TICKETCOUNT,");
		hql.append("SUM(T.AMOUNT+T.ITEMFEE+T.OTHERFEE) AS TOTALAMOUNT,");
		// costprice*quantity
		hql.append("SUM(T.AMOUNT-T.COSTPRICE*T.QUANTITY) AS GAINS ");
		hql.append("FROM WEBDATA.TICKET_ORDER T ");
		hql.append("LEFT JOIN WEBDATA.OPEN_PLAYITEM OP ");
		hql.append("ON T.RELATEDID=OP.MPID ");
		hql.append("WHERE T.ORDER_TYPE='ticket' ");
		hql.append("AND T.STATUS like 'paid%' ");
		// 时间条件
		if ("addtime".equals(timetype)) {// 下单时间
			hql.append("AND T.ADDTIME>=? ");
			hql.append("AND T.ADDTIME<=? ");
		} else {// 场次时间
			hql.append("AND OP.PLAYTIME>=? ");
			hql.append("AND OP.PLAYTIME<=? ");
		}
		hql.append("GROUP BY T.CITYCODE,OP.CINEMANAME ");
		hql.append("ORDER BY TICKETCOUNT DESC ");
		String excutehql = "SELECT * FROM ( # ) where rownum <=10";
		excutehql = excutehql.replace("#", hql.toString());
		// 前十名
		List<Map<String, Object>> topMapList = jdbcTemplate.queryForList(excutehql, startTime, currentTime);
		// 后十名
		excutehql = StringUtils.replace(excutehql, "DESC", "ASC");
		List<Map<String, Object>> lastMapList = jdbcTemplate.queryForList(excutehql, startTime, currentTime);
		model.put("topMapList", topMapList);
		model.put("lastMapList", lastMapList);
		model.put("startTime", startTime);
		model.put("endTime", currentTime);
		model.put("cityData", AdminCityContant.getCitycode2CitynameMap());
		return "admin/gewapay/report/monitorByCityAndCinema.vm";
	}

	/**
	 * 当前时间的每个城市的出票量、总订单、总销售额、总成本、毛利润、总折扣
	 * 
	 * @param model
	 * @return
	 */
	@RequestMapping("/admin/gewapay/monitorByCity.xhtml")
	public String monitorByCity(ModelMap model) {

		// 一天开始时间
		Date startTime = getStartTimeOfDay();
		Date currentTime = getNowTime();

		StringBuilder hql = new StringBuilder();
		hql.append("select new map(t.citycode as citycode,");
		hql.append("count(*) as ordercount,");
		hql.append("sum(t.quantity) as quantity,");
		hql.append("sum(t.totalfee+t.itemfee+t.otherfee) as totalamount,");
		hql.append("sum(t.costprice*t.quantity) as totalcost,");
		hql.append("sum(t.totalfee-t.costprice*t.quantity) as gains,");
		hql.append("sum(t.discount) as discount ) ");
		hql.append(" from TicketOrder t where t.addtime>=? and t.addtime<=? and t.status like 'paid%' ");
		hql.append(" group by t.citycode order by count(*) desc ");
		List<Map<String, Object>> qryMapList = hibernateTemplate.find(hql.toString(), startTime, currentTime);
		model.put("cityData", AdminCityContant.getCitycode2CitynameMap());
		model.put("qryMapList", qryMapList);
		model.put("startTime", startTime);
		model.put("endTime", currentTime);
		return "admin/gewapay/report/monitorByCity.vm";
	}

	/**
	 * 当前时间内2小时未出票量的影院
	 * 
	 * @return
	 */
	@RequestMapping("/admin/gewapay/monitorByCinema.xhtml")
	public String monitorByCinema(String citycode, ModelMap model) {

		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.HOUR, -2);// 2小时前
		Date startTime = cal.getTime();
		Date currentTime = getNowTime();

		List<Object> params = new ArrayList<Object>();
		params.add(startTime);
		params.add(currentTime);

		StringBuilder hql = new StringBuilder();
		hql.append(" select new map(c.name as name ) from Cinema c ");
		hql.append(" where c.booking='open' and c.id not in ( select t.cinemaid from TicketOrder t where t.addtime>=? and t.addtime<=? and t.status like 'paid%' ) ");
		if (StringUtils.isNotBlank(citycode)) {// 城市
			hql.append(" and c.citycode=? ");
			params.add(citycode);
		}
		List<Map<String, Object>> qryMapList = hibernateTemplate.find(hql.toString(), params.toArray());
		model.put("cityData", AdminCityContant.getCitycode2CitynameMap());
		model.put("qryMapList", qryMapList);
		model.put("startTime", startTime);
		model.put("endTime", currentTime);
		return "admin/gewapay/report/monitorByCinema.vm";
	}

	/**
	 * 当前时间1个小时内，一场次出票量大于30张的影院（影院、电影、场次时间、成本金额、销售金额、出票量）
	 * 
	 * @param model
	 * @return
	 */
	@RequestMapping("/admin/gewapay/monitorByPlayItem.xhtml")
	public String monitorByPlayItem(Integer pageNo, Timestamp starttime,
			Timestamp endtime, ModelMap model) {
		if(pageNo==null) pageNo = 0;
		Date startTime = getOneHourAgo();
		Date currentTime = getNowTime();

		// 分页指定查询一个区间
		if (starttime != null && endtime != null) {
			startTime = starttime;
			currentTime = endtime;
		}

		final StringBuilder sql = new StringBuilder();
		sql.append("SELECT OP.CINEMANAME AS CINEMANAME,");
		sql.append("OP.CITYCODE AS CITYCODE,");
		sql.append("OP.MOVIENAME AS MOVIENAME,");
		sql.append("OP.PLAYTIME AS PLAYTIME,");
		sql.append("OP.ROOMNAME AS ROOMNAME,");
		sql.append("SUM(T.COSTPRICE*T.QUANTITY) AS TOTALCOST,");
		sql.append("SUM(T.AMOUNT+T.ITEMFEE+T.OTHERFEE) AS TOTALAMOUNT,");
		sql.append("SUM(T.QUANTITY) AS QUANTITY  ");
		sql.append("FROM WEBDATA.TICKET_ORDER T ");
		sql.append("LEFT JOIN WEBDATA.OPEN_PLAYITEM OP ");
		sql.append("ON T.RELATEDID=OP.MPID ");
		sql.append("WHERE T.ORDER_TYPE='ticket' ");
		sql.append("AND T.STATUS LIKE 'paid%' ");
		sql.append("AND T.ADDTIME>=? ");
		sql.append("AND T.ADDTIME<=? ");
		sql.append("GROUP BY T.RELATEDID,OP.CINEMANAME,OP.MOVIENAME,OP.PLAYTIME,OP.ROOMNAME,OP.CITYCODE ");
		sql.append("HAVING SUM(T.QUANTITY)>30 ");
		sql.append("ORDER BY SUM(T.QUANTITY) DESC ");

		final Integer pageSize = 50;
		final Integer startIndex = pageNo * pageSize;
		final Date startT = startTime;
		final Date endT = currentTime;

		// 页集合
		List<Map<String, Object>> qryMapList = hibernateTemplate.executeFind(new HibernateCallback() {
			@Override
			public Object doInHibernate(Session session) throws HibernateException, SQLException {
				Query query = session.createSQLQuery(sql.toString()).addScalar("CINEMANAME", StringType.INSTANCE)
						.addScalar("CITYCODE", StringType.INSTANCE).addScalar("MOVIENAME", StringType.INSTANCE)
						.addScalar("PLAYTIME", TimestampType.INSTANCE).addScalar("ROOMNAME", StringType.INSTANCE)
						.addScalar("TOTALCOST", IntegerType.INSTANCE).addScalar("TOTALAMOUNT", IntegerType.INSTANCE)
						.addScalar("QUANTITY", IntegerType.INSTANCE);
				query.setFirstResult(startIndex).setMaxResults(pageSize).setResultTransformer(CriteriaSpecification.ALIAS_TO_ENTITY_MAP);
				query.setParameter(0, startT);
				query.setParameter(1, endT);
				return query.list();
			}
		});

		// 总条数
		final StringBuilder counthql = new StringBuilder();
		counthql.append("SELECT COUNT(*) FROM ( ");
		counthql.append("SELECT T.RELATEDID  FROM WEBDATA.TICKET_ORDER T ");
		counthql.append("LEFT JOIN WEBDATA.OPEN_PLAYITEM OP ");
		counthql.append("ON T.RELATEDID=OP.MPID ");
		counthql.append("WHERE T.ORDER_TYPE='ticket' ");
		counthql.append("AND T.STATUS LIKE 'paid%' ");
		counthql.append("AND T.ADDTIME>=? ");
		counthql.append("AND T.ADDTIME<=? ");
		counthql.append("GROUP BY T.RELATEDID ");
		counthql.append("HAVING SUM(T.QUANTITY)>30 ) ");
		Integer totalCount = jdbcTemplate.queryForInt(counthql.toString(), startT, endT);

		// 分页控件初始化
		PageUtil pageUtil = new PageUtil(totalCount, pageSize, pageNo, "admin/gewapay/monitorByPlayItem.xhtml");
		Map<String, Object> pageData = new HashMap<String, Object>();
		pageData.put("starttime", DateUtil.formatTimestamp(startT));
		pageData.put("endtime", DateUtil.formatTimestamp(endT));
		pageUtil.initPageInfo(pageData);

		model.put("pageUtil", pageUtil);
		model.put("startTime", startTime);
		model.put("endTime", currentTime);
		model.put("qryMapList", qryMapList);
		model.put("cityData", AdminCityContant.getCitycode2CitynameMap());

		return "admin/gewapay/report/monitorByPlayItem.vm";
	}

	private static final String TEMPHQL = "select count(s.id) from SMSRecord s where s.smstype=? and s.sendtime>=? and s.sendtime<=? ";
	private static final String EXCUTESQLTEMP = "SELECT * FROM ( # ) WHERE ROWNUM <=10";

	/**
	 * 查询指定类型短信发送失败数量
	 * 
	 * @param smstype
	 *            短信类型
	 * @param startTime
	 *            开始时间
	 * @param nowTime
	 *            结束时间
	 * @return 发送失败数量
	 */
	private Integer getSMSFaildCount(String smstype, Date startTime, Date nowTime) {
		Integer count = 0;
		StringBuilder hql = new StringBuilder();
		hql.append(TEMPHQL).append(" and s.status like ? ");
		List resultList = hibernateTemplate.find(hql.toString(), smstype, startTime, nowTime, "N_%");
		if (resultList != null && resultList.size() > 0) {
			count = Integer.parseInt(resultList.get(0).toString());
		}
		return count;
	}

	/**
	 * 查询指定类型短信发送成功数量
	 * 
	 * @param smstype
	 *            短信类型
	 * @param startTime
	 *            开始时间
	 * @param nowTime
	 *            结束时间
	 * @return 发送成功数量
	 */
	private Integer getSMSSuccessCount(String smstype, Date startTime, Date nowTime) {
		Integer count = 0;
		StringBuilder hql = new StringBuilder();
		hql.append(TEMPHQL).append(" and s.status=? ");
		List resultList = hibernateTemplate.find(hql.toString(), smstype, startTime, nowTime, SmsConstant.STATUS_Y);
		if (resultList != null && resultList.size() > 0) {
			count = Integer.parseInt(resultList.get(0).toString());
		}
		return count;
	}

	/**
	 * 当前时间1个小时内，取票短信、温馨提示短信未成功的数量、总数量
	 * 
	 * @param model
	 * @return
	 */
	@RequestMapping("/admin/gewapay/monitorSMS.xhtml")
	public String monitorSMS(ModelMap model) {
		Date startTime = getStartTimeOfDay();
		Date currentTime = getNowTime();
		// 取票短信成功数量
		Integer nowSuccessCount = getSMSSuccessCount(SmsConstant.SMSTYPE_NOW, startTime, currentTime);
		// 取票短信失败数量
		Integer nowFailedCount = getSMSFaildCount(SmsConstant.SMSTYPE_NOW, startTime, currentTime);
		// 温馨提示短信成功数量
		Integer tipSuccessCount = getSMSSuccessCount(SmsConstant.SMSTYPE_3H, startTime, currentTime);
		// 温馨提示短信失败数量
		Integer tipFailedCount = getSMSFaildCount(SmsConstant.SMSTYPE_3H, startTime, currentTime);
		model.put("nowSuccessCount", nowSuccessCount);
		model.put("nowFailedCount", nowFailedCount);
		model.put("tipSuccessCount", tipSuccessCount);
		model.put("tipFailedCount", tipFailedCount);
		model.put("startTime", startTime);
		model.put("endTime", currentTime);
		return "admin/gewapay/report/monitorSMS.vm";
	}

	/**
	 * 当天券使用前10名,特殊活动的使用前10位
	 * 
	 * @return
	 */
	@RequestMapping("/admin/gewapay/monitorDiscount.xhtml")
	public String monitorDiscount(ModelMap model) {

		Date currentTime = getNowTime();
		// 一天开始时间
		Date startTime = getOneHourAgo();

		// 券使用前10名
		StringBuilder sql = new StringBuilder();
		sql.append(" SELECT DM.BATCHID AS BATCHID ,");
		sql.append(" B.CHANNELINFO AS DESCRIPTION,");
		sql.append(" DM.CARDTYPE AS CARDTYPE, ");
		sql.append(" COUNT(DM.RECORDID) AS UESDCOUNT ");
		sql.append(" FROM WEBDATA.DISCOUNT_ITEM DM LEFT JOIN WEBDATA.ELECCARD_BATCH B ON DM.BATCHID=B.RECORDID ");
		sql.append(" LEFT JOIN WEBDATA.TICKET_ORDER T ON DM.ORDERID=T.RECORDID ");
		sql.append(" WHERE DM.TAG='ecard' AND T.ADDTIME>=? AND T.ADDTIME <=? ");
		sql.append(" GROUP BY DM.BATCHID,B.CHANNELINFO,DM.CARDTYPE ");
		sql.append(" ORDER BY COUNT(DM.RECORDID) DESC ");
		String excuteSql = EXCUTESQLTEMP.replace("#", sql.toString());
		List<Map<String, Object>> cardMapList = jdbcTemplate.queryForList(excuteSql, startTime, currentTime);

		// 特殊活动的使用前10位
		StringBuilder spSql = new StringBuilder();
		spSql.append("SELECT DM.DESCRIPTION  AS DESCRIPTION, ");
		spSql.append("DM.RELATEDID  AS RELATEDID, ");
		spSql.append("COUNT(DM.ORDERID)  AS ORDERCOUNT, ");
		spSql.append("SUM(T.AMOUNT+T.ITEMFEE+T.OTHERFEE) AS TOTALAMOUNT, ");
		spSql.append("SUM(T.QUANTITY)  AS TICKETCOUNT, ");
		spSql.append("SUM(T.COSTPRICE*T.QUANTITY) AS TOTALCOST, ");
		spSql.append("SUM(T.DISCOUNT)  AS DISCOUNT ");
		spSql.append("FROM WEBDATA.DISCOUNT_ITEM DM ");
		spSql.append("LEFT JOIN WEBDATA.TICKET_ORDER T ");
		spSql.append("ON DM.ORDERID=T.RECORDID ");
		spSql.append("WHERE DM.TAG='partner' AND T.ADDTIME>=? AND T.ADDTIME <=? ");
		spSql.append("GROUP BY DM.DESCRIPTION, DM.RELATEDID ");
		spSql.append("ORDER BY COUNT(DM.RECORDID) DESC ");
		excuteSql = EXCUTESQLTEMP.replace("#", spSql.toString());
		List<Map<String, Object>> spMapList = jdbcTemplate.queryForList(excuteSql, startTime, currentTime);

		model.put("startTime", startTime);
		model.put("endTime", currentTime);
		model.put("cardMapList", cardMapList);
		model.put("spMapList", spMapList);
		
		// 48小时内一批券使用前10名
		StringBuilder sql48 = new StringBuilder();
		sql48.append(" SELECT DM.BATCHID AS BATCHID ,");
		sql48.append(" B.CHANNELINFO AS DESCRIPTION,");
		sql48.append(" DM.CARDTYPE AS CARDTYPE, ");
		sql48.append(" COUNT(DM.RECORDID) AS UESDCOUNT,");
		sql48.append(" SUM(DM.AMOUNT) AS AMOUNT,");
		sql48.append(" SUM(T.QUANTITY) AS QUANTITY,");
		sql48.append(" SUM(T.COSTPRICE) AS COSTPRICE");
		sql48.append(" FROM WEBDATA.DISCOUNT_ITEM DM LEFT JOIN WEBDATA.ELECCARD_BATCH B ON DM.BATCHID=B.RECORDID ");
		sql48.append(" LEFT JOIN WEBDATA.TICKET_ORDER T ON DM.ORDERID=T.RECORDID ");
		sql48.append(" WHERE DM.TAG='ecard' AND T.ADDTIME>=? AND T.ADDTIME <=? ");
		sql48.append(" GROUP BY DM.BATCHID,B.CHANNELINFO,DM.CARDTYPE ");
		sql48.append(" ORDER BY COUNT(DM.RECORDID) DESC ");
		String excuteSql48 = EXCUTESQLTEMP.replace("#", sql48.toString());
		List<Map<String, Object>> cardMapList48 = jdbcTemplate.queryForList(excuteSql48, DateUtil.addHour(currentTime, -48), currentTime);
		model.put("cardMapList48", cardMapList48);
		
		
		//48小时内单个用户使用数量超过10张的用户昵称和ID
		StringBuilder memberSql = new StringBuilder();
		memberSql.append(" SELECT T.MEMBERID AS MEMBERID ,");
		memberSql.append(" T.MEMBERNAME AS MEMBERNAME");
		memberSql.append(" FROM WEBDATA.DISCOUNT_ITEM DM LEFT JOIN WEBDATA.ELECCARD_BATCH B ON DM.BATCHID=B.RECORDID ");
		memberSql.append(" LEFT JOIN WEBDATA.TICKET_ORDER T ON DM.ORDERID=T.RECORDID ");
		memberSql.append(" WHERE DM.TAG='ecard' AND T.ADDTIME>=? AND T.ADDTIME <=? ");
		memberSql.append(" GROUP BY T.MEMBERID,T.MEMBERNAME HAVING COUNT(T.MEMBERID) > 10");
		memberSql.append(" ORDER BY COUNT(T.MEMBERID) DESC ");
		String excuteMemberSql = EXCUTESQLTEMP.replace("#", memberSql.toString());
		List<Map<String, Object>> memberList = jdbcTemplate.queryForList(excuteMemberSql, DateUtil.addHour(currentTime, -48), currentTime);
		model.put("memberList", memberList);
		
		return "admin/gewapay/report/monitorDiscount.vm";
	}

	/*************************** 电影节场次 监测 begin wangqingchuan ***************************************/
	@RequestMapping("/admin/gewapay/monitorFilmFestivalOPI.xhtml")
	public String monitorFilmFestivalOpenPlayItem(final Long cinemaid, String opentype, Timestamp starttime, Timestamp endtime,
			Integer pageNo, String order, HttpServletResponse res, String isXls, ModelMap model) {
		List<Long> cinemaIdList = filmFestService.getFilmFestCinema(FilmFestConstant.TAG_FILMFEST_16, AdminCityContant.CITYCODE_SH, null);
		Collections.sort(cinemaIdList);
		List<Cinema> cinemaList = daoService.getObjectList(Cinema.class, cinemaIdList);
		Collections.sort(cinemaList, new PropertyComparator("pinyin", false, true));
		model.put("cinemaList", cinemaList);
		String url = "admin/gewapay/report/monitorFilmFestivalOPI.vm";
		if (starttime == null || endtime == null){
			return url;
		}
		// 跳转路径
		SpecialActivity sa = filmFestService.getSpecialActivity(FilmFestConstant.TAG_FILMFEST_16);
		Long batch = sa.getId();
		if(pageNo == null) pageNo = 0;
		int maxnum = 200;
		int from = pageNo * maxnum;
		final Date startT = starttime;
		final Date endT = endtime;
		// 获取数据列表
		final StringBuffer sql = new StringBuffer();
		sql.append(" select o.MPID as MPID, o.cinemaid as 影院编号,o.CINEMANAME as 影院, o.MOVIEID,o.MOVIENAME as 影片, o.ROOMNAME 影厅, o.otherinfo 其他, ");
		sql.append(" to_char(o.PLAYTIME,'YYYY-MM-dd HH24:mi:ss') as 时间,o.SEATNUM as  总座位数,o.LOCKNUM as 格瓦拉禁售,(o.SEATNUM - o.LOCKNUM) as 格瓦拉可售, ");
		sql.append(" o.GSELLNUM as 格瓦拉售出,(o.SEATNUM - o.LOCKNUM - o.GSELLNUM) as 格瓦拉剩余 ");
		// 查询对象
		String tempSql = " from WEBDATA.open_playitem o where o.citycode = ? and o.playtime >= ? and o.playtime <= ? "
								+ "and o.opentype=? and exists(select m.mpid from WEBDATA.view_festmpi  m where m.batch = ? and m.mpid=o.mpid) ";
		
		// 参数列表
		List params = new ArrayList();
		params.add(AdminCityContant.CITYCODE_SH);
		params.add(starttime);
		params.add(endtime);
		params.add(opentype);
		params.add(batch);
		if (cinemaid != null) {
			tempSql += " and o.cinemaid = ? ";
			params.add(cinemaid);
		} else if(!CollectionUtils.isEmpty(cinemaIdList)){
			// 将所有上海影院的id拼接到sql语句中
			tempSql += " and o.cinemaid in  (?" + StringUtils.repeat(",?", cinemaIdList.size()-1)+ ") ";
			params.addAll(cinemaIdList);
		}
		String countSql = "select count(*) " + tempSql;
		int count = jdbcTemplate.queryForInt(countSql, params.toArray());
		if(count > 0){
			sql.append(tempSql);
			sql.append(" order by "+order+" desc ");
			//sql.append(" ,o.CITYCODE,o.cinemaid,o.MOVIEID,o.ROOMID, o.PLAYTIME asc ");
			
			// 获取单页数据集合
			List<Map<String, Object>> results = daoService.queryMapBySQL(sql.toString(), from, maxnum, params.toArray());
			model.put("cityData", AdminCityContant.getCitycode2CitynameMap());
			model.put("startTime", startT);
			model.put("endTime", endT);
			model.put("opiMapList", results);
			model.put("pageNo", pageNo);
			model.put("order", order);
			Map bNumMap = new HashMap();
			Map cNumMap = new HashMap();
			Map dNumMap = new HashMap();
			Map allNumMap = new HashMap();
			for (Map<String, Object> row : results) {
				Map<String, String> rowMap = mongoService.getMap("mpid", MongoData.NS_OPI_LOCKNUM, "" + row.get("MPID"));
				if (rowMap != null) {
					bNumMap.put(row.get("MPID"), rowMap.get("B"));
					cNumMap.put(row.get("MPID"), rowMap.get("C"));
					dNumMap.put(row.get("MPID"), rowMap.get("D"));
					allNumMap.put(row.get("MPID"), rowMap.get("ALL"));
				}
			}
			model.put("bNumMap", bNumMap);
			model.put("cNumMap", cNumMap);
			model.put("dNumMap", dNumMap);
			model.put("allNumMap", allNumMap);
			PageUtil pageUtil = new PageUtil(count,maxnum,pageNo,"admin/gewapay/monitorFilmFestivalOPI.xhtml",true,true);
			Map paramsMap = new HashMap();
			paramsMap.put("cinemaid", cinemaid);
			paramsMap.put("opentype",opentype);
			paramsMap.put("starttime",starttime);
			paramsMap.put("endtime", endtime);
			paramsMap.put("pageNo", pageNo);
			paramsMap.put("order", order);
			paramsMap.put("isXls", isXls);
			pageUtil.initPageInfo(paramsMap);
			model.put("pageUtil",pageUtil);
		}
		// 导出excel
		downloadReportOPI(res, isXls);
		return url;
	}


	protected void downloadReportOPI(HttpServletResponse res, String isXls) {
		if (StringUtils.isNotBlank(isXls)) {
			Random radom = new Random(System.currentTimeMillis());
			int rd = radom.nextInt();
			String exportName = "GewaraBB_" + DateUtil.format(new Date(), "yyMMdd") + "_" + rd + ".xls";
			res.setContentType("application/xls");
			res.addHeader("Content-Disposition", "attachment;filename=" + exportName);
		}
	}
	/*************************** 电影节场次 监测 end wangqingchuan ***************************************/
	
	/**
	 * 影院场次结算错误信息 
	 */
	@RequestMapping("/admin/gewapay/monitorByPrice.xhtml")
	public String monitorByPrice(ModelMap model){
		//格瓦拉售价低于格瓦拉结算价
		StringBuilder hql_a = new StringBuilder();
		hql_a.append(" select opi.citycode as citycode, opi.cinemaname as cinemaname, opi.recordid as id, opi.moviename as moviename, opi.roomname as roomname, opi.playtime as playtime, ");
		hql_a.append(" ua.displayname as openuser, ub.displayname as createuser,opi.costprice as costprice, opi.gewaprice as gewaprice, opi.price as price ");
		hql_a.append(" from WEBDATA.Open_PlayItem opi left join WEBDATA.Open_PlayItem_Ext pie on opi.mpid = pie.mpid ");
		hql_a.append(" left join WEBDATA.App_User ua on pie.openuser = ua.id  left join WEBDATA.App_User ub on pie.createuser = ub.id ");
		hql_a.append(" where opi.closetime >= ? and opi.status='Y' and opi.gewaprice < opi.costprice");
		List<Map<String, Object>> hql_aList = jdbcTemplate.queryForList(hql_a.toString(), DateUtil.getCurFullTimestamp());
		model.put("hql_aList", hql_aList);
		//格瓦拉售价与格瓦拉成本之间的差额不符合为格瓦拉结算价的20%和小于等于12元同时满足的要求的
		StringBuilder hql_b = new StringBuilder();
		hql_b.append(" select opi.citycode as citycode, opi.cinemaname as cinemaname, opi.recordid as id, opi.moviename as moviename, opi.roomname as roomname, opi.playtime as playtime, ");
		hql_b.append(" ua.displayname as openuser, ub.displayname as createuser,opi.costprice as costprice, opi.gewaprice as gewaprice, opi.price as price ");
		hql_b.append(" from WEBDATA.Open_PlayItem opi left join WEBDATA.Open_PlayItem_Ext pie on opi.mpid = pie.mpid ");
		hql_b.append(" left join WEBDATA.App_User ua on pie.openuser = ua.id  left join WEBDATA.App_User ub on pie.createuser = ub.id ");
		hql_b.append(" where opi.closetime >= ? and opi.status='Y' and ((gewaprice-costprice)>trunc(costprice*0.2,0) or (gewaprice-costprice)>10)");
		List<Map<String, Object>> hql_bList = jdbcTemplate.queryForList(hql_b.toString(), DateUtil.getCurFullTimestamp());
		model.put("hql_bList", hql_bList);
		//格瓦拉售价大于影院价
		StringBuilder hql_c = new StringBuilder();
		hql_c.append(" select opi.citycode as citycode, opi.cinemaname as cinemaname, opi.recordid as id, opi.moviename as moviename, opi.roomname as roomname, opi.playtime as playtime, ");
		hql_c.append(" ua.displayname as openuser, ub.displayname as createuser,opi.costprice as costprice, opi.gewaprice as gewaprice, opi.price as price ");
		hql_c.append(" from WEBDATA.Open_PlayItem opi left join WEBDATA.Open_PlayItem_Ext pie on opi.mpid = pie.mpid ");
		hql_c.append(" left join WEBDATA.App_User ua on pie.openuser = ua.id  left join WEBDATA.App_User ub on pie.createuser = ub.id ");
		hql_c.append(" where opi.closetime >= ? and opi.status='Y' and opi.gewaprice > opi.price");
		List<Map<String, Object>> hql_cList = jdbcTemplate.queryForList(hql_c.toString(), DateUtil.getCurFullTimestamp());
		model.put("hql_cList", hql_cList);
		return "admin/gewapay/report/monitorPrice.vm";
	}
}