package com.gewara.web.action.admin.balance;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import com.gewara.constant.ticket.OpiConstant;
import com.gewara.constant.ticket.OrderConstant;
import com.gewara.model.acl.User;
import com.gewara.model.movie.Cinema;
import com.gewara.model.movie.CinemaProfile;
import com.gewara.model.pay.CinemaSettle;
import com.gewara.model.ticket.OpenPlayItem;
import com.gewara.service.gewapay.ReportService;
import com.gewara.util.BeanUtil;
import com.gewara.util.ChangeEntry;
import com.gewara.util.DateUtil;
import com.gewara.web.action.admin.BaseAdminController;

@Controller
public class CinemaSettleAdminController extends BaseAdminController{
	@Autowired@Qualifier("reportService")
	private ReportService reportService;
	@RequestMapping("/admin/balance/settle/lastSettleList.xhtml")
	public String lastSettleList(ModelMap model){
		List<CinemaSettle> settleList = reportService.getLastSettleList();
		List<Cinema> cinemaList = reportService.getBookingCinemaList();
		Map<String, List<Cinema>> cinemaGroupMap = BeanUtil.groupBeanList(cinemaList, "citycode");
		Map<Long/*cinemaid*/, CinemaSettle> settleMap = BeanUtil.beanListToMap(settleList, "cinemaid");
		model.put("settleMap", settleMap);
		model.put("cinemaGroupMap", cinemaGroupMap);
		model.put("today", new Timestamp(System.currentTimeMillis()));
		return "admin/balance/settle/lastSettleList.vm";
	}
	@RequestMapping("/admin/balance/settle/cinemaSettleList.xhtml")
	public String cinemaSettleList(Long cinemaid, ModelMap model){
		String query = "from CinemaSettle where cinemaid=? order by timefrom desc";
		List<CinemaSettle> settleList = hibernateTemplate.find(query, cinemaid);
		Cinema cinema = daoService.getObject(Cinema.class, cinemaid);
		model.put("settleList", settleList);
		model.put("cinema", cinema);
		return "admin/balance/settle/cinemaSettleList.vm";
	}
	private Long hackId = 114307277L;//临时Hack，海上国际影城环球港店
	@RequestMapping("/admin/balance/settle/modifySettle.xhtml")
	public String modifySettle(Long settleId, Long cinemaid, ModelMap model){
		if(settleId!=null){
			CinemaSettle settle = daoService.getObject(CinemaSettle.class, settleId);	
			if(settle.getStatus().equals("Y")) return showError(model, "已结算，不能修改！");
			model.put("settle", settle);
			CinemaProfile profile = daoService.getObject(CinemaProfile.class, settle.getCinemaid());
			if(StringUtils.equals(profile.getOpentime(), OpiConstant.OPEN_HFH) || settle.getCinemaid().equals(hackId)){
				model.put("dateFmt", "yyyy-MM-dd 06:00:00");
			}else{
				model.put("dateFmt", "yyyy-MM-dd HH:00:00");
			}
			Cinema cinema = daoService.getObject(Cinema.class, settle.getCinemaid());
			model.put("cinema", cinema);

		}else{
			Cinema cinema = daoService.getObject(Cinema.class, cinemaid);
			model.put("cinema", cinema);
			CinemaProfile profile = daoService.getObject(CinemaProfile.class, cinemaid);
			if(StringUtils.equals(profile.getOpentime(), OpiConstant.OPEN_HFH) || cinemaid.equals(hackId)){
				model.put("dateFmt", "yyyy-MM-dd 06:00:00");
			}else{
				model.put("dateFmt", "yyyy-MM-dd HH:00:00");
			}
			CinemaSettle last = reportService.getLastSettle(cinemaid);
			if(last!=null){
				if(last.getStatus().equals("N")) return showError(model, "上期未结算，不能增加！");
				model.put("lastSettle", last);
			}
		}
		return "admin/balance/settle/modifySettle.vm";
	}
	@RequestMapping("/admin/balance/settle/saveSettle.xhtml")
	public String saveSettle(Long settleId, Long cinemaid, Timestamp timefrom, Timestamp timeto, Timestamp lasttime, Timestamp curtime, 
			Integer adjustment, Integer lastOrderRefund, Integer curOrderRefund, Integer amount, String remark, ModelMap model){
		if(adjustment!=0 && StringUtils.isBlank(remark)){
			return showJsonError(model, "结算调整不为0，必须填写备注！");
		}
		CinemaSettle settle = null;
		ChangeEntry entry = null;
		if(settleId!=null){
			settle = daoService.getObject(CinemaSettle.class, settleId);
			if(settle.getStatus().equals("Y")) return showJsonError(model, "已结算，不能修改！");
			entry = new ChangeEntry(settle);
			settle.setTimeto(timeto);
			settle.setCurtime(curtime);
		}else{
			Cinema cinema = daoService.getObject(Cinema.class, cinemaid);
			model.put("cinema", cinema);
			CinemaSettle last = reportService.getLastSettle(cinemaid);
			if(last!=null){
				if(last.getStatus().equals("N")) return showJsonError(model, "上期未结算，不能增加！");
				settle = new CinemaSettle(last, timeto, curtime);
			}else{
				settle = new CinemaSettle(cinemaid, timefrom, timeto, lasttime, curtime);
			}
		}
		if(settle.getTimefrom().after(settle.getLasttime()) || settle.getTimeto().after(settle.getCurtime())){
			return showJsonError(model, "时间参数不正确！");
		}
		settle.setAdjustment(adjustment);
		settle.setLastOrderRefund(lastOrderRefund);
		settle.setCurOrderRefund(curOrderRefund);
		settle.setAmount(amount);
		settle.setRemark(remark);
		daoService.saveObject(settle);
		User user = getLogonUser();
		if(entry==null){
			monitorService.saveAddLog(user.getId(), CinemaSettle.class, settle.getId(), BeanUtil.getBeanMap(settle));
		}else{
			monitorService.saveChangeLog(user.getId(), CinemaSettle.class, settle.getId(), entry.getChangeMap(settle));
		}
		return showJsonSuccess(model, ""+settle.getId());
	}
	@RequestMapping("/admin/balance/settle/confirmSettle.xhtml")
	public String confirmSettle(Long settleId, ModelMap model){
		CinemaSettle settle = daoService.getObject(CinemaSettle.class, settleId);
		if(settle.getStatus().equals("Y")){
			return showJsonError(model, "已经结算的状态");
		}
		if(settle.getAdjustment()==null || settle.getAmount()==null || 
				settle.getCurOrderRefund()==null || settle.getLastOrderRefund() ==null || settle.getAmount()==0){
			return showJsonError(model, "结算金额不完整！");
		}
		ChangeEntry entry = new ChangeEntry(settle);
		settle.setStatus("Y");
		daoService.saveObject(settle);
		User user = getLogonUser();
		monitorService.saveChangeLog(user.getId(), CinemaSettle.class, settle.getId(), entry.getChangeMap(settle));
		return showJsonSuccess(model);
	}
	

	@RequestMapping("/admin/balance/settle/viewSettle.xhtml")
	public String viewSettle(Long settleId, String opentype, ModelMap model){
		CinemaSettle settle = daoService.getObject(CinemaSettle.class, settleId);
		CinemaProfile profile = daoService.getObject(CinemaProfile.class, settle.getCinemaid());
		if(StringUtils.isBlank(opentype)) opentype = profile.getOpentype();
		model.put("opentype", opentype);
		if(StringUtils.equals(opentype, "ALL")) opentype="";
		model.put("settle", settle);
		addDayReportData(settle.getCinemaid(), settle.getTimefrom(), settle.getTimeto(), opentype, settle.getLasttime(), settle.getCurtime(), model);
		return "admin/balance/settle/viewSettle.vm";
	}
	
	private void addDayReportData(Long cinemaid, Timestamp timefrom, Timestamp timeto, String opentype, 
			Timestamp lasttime, Timestamp curtime, ModelMap model) {
		Cinema cinema = daoService.getObject(Cinema.class, cinemaid);
		//TODO:时间类型，目前只有火凤凰按下单时间
		CinemaProfile cp = daoService.getObject(CinemaProfile.class, cinemaid);
		String timeCondition = "t.playtime>=? and t.playtime<? ";
		if(StringUtils.equals(cp.getOpentype(), OpiConstant.OPEN_HFH) || cinemaid.equals(hackId)){
			timeCondition = "t.addtime>=? and t.addtime<? ";
		}
		Map<Timestamp, Timestamp> tempTime = splitTime(timefrom, timeto);
		Map<Timestamp, Map> timeDataMap = new HashMap();
		String hql = "select new map(" +
				"sum(case when t.status='paid_success' then t.quantity else 0 end) as quantity, " +
				"sum(t.costprice * case when t.status='paid_success' then t.quantity else 0 end) as price, " +
				"sum(case when t.status='paid_return' then t.quantity else 0 end) as retquantity, " +
				"sum(t.costprice * case when t.status='paid_return' then t.quantity else 0 end) as retprice" +
				") from TicketOrder t " +
				"where t.cinemaid=? and " + timeCondition;
		if (StringUtils.isNotBlank(opentype)){
			hql = hql + "and t.category=? ";
		}
		hql = hql + "and t.status like ? and t.settle='Y' order by t.addtime asc";

		for (Timestamp ts : tempTime.keySet()) {
			List params = new ArrayList();
			params.add(cinemaid);
			params.add(ts);
			params.add(tempTime.get(ts));
			List<Map> dataMap = new ArrayList<Map>();
			if (StringUtils.isNotBlank(opentype)){
				params.add(opentype);
			}
			params.add(OrderConstant.STATUS_PAID + "%");
			dataMap = hibernateTemplate.find(hql, params.toArray());
			if (dataMap.size() > 0) {
				timeDataMap.put(ts, dataMap.get(0));
			}
		}
		model.put("cinema", cinema);
		model.put("tempTime", tempTime);
		model.put("timeDataMap", timeDataMap);
		model.putAll(reportService.getRefundData(cinemaid, timefrom, timeto, lasttime, curtime));
	}
	
	// 按影院统计(按播放时间)
	@RequestMapping("/admin/gewapay/reportByOpi.xhtml")
	public String cinemaReportByOpi(Long cinemaid, Date datefrom, Date dateto, String opentype, String flag, HttpServletRequest request, ModelMap model) {
		String citycode = getAdminCitycode(request);
		getCityData(citycode, model);
		if (datefrom == null || dateto == null)
			return "admin/gewapay/report/reportByOpi.vm";
		if (cinemaid == null) {
			model.put("datefrom", DateUtil.format(datefrom, "yyyy-MM-dd"));
			model.put("dateto", DateUtil.format(dateto, "yyyy-MM-dd"));
			model.put("opentype", opentype);
			model.put("flag", flag);
			return "redirect:/admin/gewapay/reportByDate.xhtml";
		}
		Cinema cinema = daoService.getObject(Cinema.class, cinemaid);
		// WMSYS.WM_CONCAT
		String hql = "select new map(t.mpid as mpid, max(t.playtime) as playtime, count(distinct t.costprice) as costpricecount, wmconcat(t.costprice) as concatprice, " +
				"sum(t.quantity) as totalquantity, sum(t.costprice*t.quantity) as totalamount, count(*) as count) " +
				"from TicketOrder t where t.cinemaid=? and t.playtime>=? and t.playtime<=? ";
		List params = new ArrayList();
		params.add(cinemaid);
		params.add(DateUtil.getBeginningTimeOfDay(datefrom));
		params.add(DateUtil.getLastTimeOfDay(dateto));

		if (StringUtils.isNotBlank(opentype)){
			hql = hql + "and t.category = ? ";
			params.add(opentype);
		}
			
		if (StringUtils.equals(flag, "settle")){//只做结算
			hql += " and t.status like ? and settle='Y'";
			params.add(OrderConstant.STATUS_PAID+"%");
		}else{
			hql += " and t.status = ? ";
			params.add(OrderConstant.STATUS_PAID_SUCCESS);
		}
		hql = hql + "group by t.mpid order by max(t.playtime)";
		
		List<Map> dataMap = new ArrayList<Map>();
		dataMap = hibernateTemplate.find(hql, params.toArray());
		Map<String, Long> orderCountMap = new LinkedHashMap<String, Long>();// 每天的订单数量
		Map<String, Long> orderQuantityMap = new LinkedHashMap<String, Long>();// 每天的订单数量
		Map<String, Long> orderDueMap = new LinkedHashMap<String, Long>();// 每天的订单总额
		List<String> strdateList = new ArrayList<String>();
		Date tmp = datefrom;
		while (tmp.compareTo(dateto) <= 0) {
			strdateList.add(DateUtil.formatDate(tmp));
			tmp = DateUtil.addDay(tmp, 1);
		}
		for (Map map : dataMap) {
			Long mpid = Long.valueOf(map.get("mpid")+"");
			OpenPlayItem opi = daoService.getObjectByUkey(OpenPlayItem.class, "mpid", mpid, false);
			Timestamp time = opi.getPlaytime();
			String format = DateUtil.format(time, "yyyy-MM-dd");
			map.put("moviename", opi.getMoviename());
			map.put("costprice", opi.getCostprice());
			map.put("roomname", opi.getRoomname());
			if (orderCountMap.containsKey(format)) {
				Long count = orderCountMap.get(format) + (Long) map.get("count");
				Long dues = (Long) map.get("totalamount") + orderDueMap.get(format);
				Long quantity = (Long) map.get("totalquantity") + orderQuantityMap.get(format);
				orderCountMap.put(format, count);
				orderDueMap.put(format, dues);
				orderQuantityMap.put(format, quantity);
			} else {
				orderCountMap.put(format, (Long) map.get("count"));
				orderDueMap.put(format, (Long) map.get("totalamount"));
				orderQuantityMap.put(format, (Long) map.get("totalquantity"));
			}

		}
		model.put("orderCountMap", orderCountMap);
		model.put("orderQuantityMap", orderQuantityMap);
		model.put("orderDueMap", orderDueMap);
		model.put("dataMap", dataMap);
		model.put("cinema", cinema);
		model.put("strdateList", strdateList);
		return "admin/gewapay/report/reportByOpi.vm";
	}
	private static Map<Timestamp, Timestamp> splitTime(Timestamp datefrom, Timestamp dateto) {
		Map<Timestamp, Timestamp> tempTime = new LinkedHashMap<Timestamp, Timestamp>();
		Timestamp begin = datefrom;
		Timestamp end = null;
		while(begin.before(dateto)){
			end = DateUtil.addDay(begin, 1);
			tempTime.put(begin, end);
			begin = end;
		}
		return tempTime;
	}
	private void getCityData(String citycode, ModelMap model) {
		String cinemaHql = "select new map(c.id as cinemaid, c.name as cinemaname) from "
				+ "Cinema c where c.citycode=? and c.id in (select p.id from CinemaProfile p)";
		List<Map> cinemaList = hibernateTemplate.find(cinemaHql, citycode);
		model.put("cinemaList", cinemaList);
	}
}
