package com.gewara.web.action.merchant;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.support.PropertyComparator;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.gewara.constant.AdminCityContant;
import com.gewara.constant.ticket.OrderConstant;
import com.gewara.model.movie.Cinema;
import com.gewara.model.movie.CinemaProfile;
import com.gewara.model.movie.Movie;
import com.gewara.model.partner.Merchant;
import com.gewara.model.pay.GoodsOrder;
import com.gewara.model.pay.OrderRefund;
import com.gewara.model.pay.TicketOrder;
import com.gewara.model.ticket.OpenPlayItem;
import com.gewara.service.gewapay.RefundService;
import com.gewara.service.gewapay.ReportService;
import com.gewara.service.movie.MCPService;
import com.gewara.service.order.OrderQueryService;
import com.gewara.support.MultiPropertyComparator;
import com.gewara.util.BeanUtil;
import com.gewara.util.DateUtil;
import com.gewara.util.ReportUtil;

@Controller
public class MerchantReportController extends BaseMerchantController{
	@Autowired@Qualifier("reportService")
	private ReportService reportService;
	public void setHfhService(ReportService reportService) {
		this.reportService = reportService;
	}
	@Autowired@Qualifier("refundService")
	private RefundService refundService;
	@Autowired@Qualifier("mcpService")
	private MCPService mcpService;
	@Autowired@Qualifier("orderQueryService")
	private OrderQueryService orderQueryService;
	/**
	 * 多影院汇总报表
	 * @param timefrom
	 * @param timeto
	 * @param cinemaId
	 * @param opentype
	 * @param model
	 * @return
	 */
	@RequestMapping(method=RequestMethod.GET, value="/merchant/report/summary.xhtml")
	public String summary(ModelMap model){
		Merchant merchant = getLogonMerchant();
		List<Cinema> cinemaList = daoService.getObjectList(Cinema.class, BeanUtil.getIdList(merchant.getRelatelist(), ","));
		Map cinemaMap = new TreeMap();
		cinemaMap.putAll(BeanUtil.groupBeanList(cinemaList, "citycode"));
		model.put("cinemaMap", cinemaMap);
		model.put("cityMap", AdminCityContant.getCitycode2CitynameMap());
		List<Movie> movieList = mcpService.getCurMovieList();
		mcpService.sortMoviesByMpiCount(AdminCityContant.CITYCODE_SH, movieList);
		model.put("movieList", movieList);
				
		return "merchant/report/summary.vm";
	}
	@RequestMapping(method=RequestMethod.POST, value="/merchant/report/summary.xhtml")
	public String summaryResult(Timestamp timefrom, Timestamp timeto, String selectCinema, 
			Long movieid, String opentype, String timetype, ModelMap model, String isXls, HttpServletResponse response){
		if(timefrom==null || timeto==null) {
			return forwardMessage(model, "请选择时间范围！");
		}
		if(DateUtil.getDiffDay(timeto, timefrom)>31){
			return forwardMessage(model, "时间跨度不能大于1月！");
		}
		List<Long> cinemaidList = new ArrayList<Long>();
		Merchant merchant = getLogonMerchant();
		String citycode = null;
		Long cinemaId = null;
		if(StringUtils.isNotBlank(selectCinema)){
			if(StringUtils.startsWith(selectCinema, "C")){
				citycode = selectCinema.substring(1);
			}else{
				cinemaId = Long.valueOf(selectCinema);
			}
		}
		if(cinemaId!=null){
			if(!hasRights(merchant, cinemaId)) return show404(model, "无此影院权限！");
			cinemaidList.add(cinemaId);
		}else{
			if(StringUtils.isNotBlank(citycode)){
				List<Cinema> cinemaList = daoService.getObjectList(Cinema.class, BeanUtil.getIdList(merchant.getRelatelist(), ","));
				Map<String, List<Cinema>> cinemaMap = BeanUtil.groupBeanList(cinemaList, "citycode");
				if(cinemaMap.containsKey(citycode)){
					cinemaidList.addAll(BeanUtil.getBeanPropertyList(cinemaMap.get(citycode), Long.class, "id", true));
				}
			}else{
				cinemaidList.addAll(BeanUtil.getIdList(merchant.getRelatelist(), ","));
			}
		}
		List<Map> dataList = new ArrayList<Map>();
		if("SYS".equals(opentype)){
			opentype=merchant.getOpentype();
		}
		for(Long cinemaid: cinemaidList){
			Map data = null;
			if("addtime".equals(timetype)){
				data = reportService.getCinemaSummaryByAddtime(cinemaid, timefrom, timeto, movieid, opentype);
			}else{
				data = reportService.getCinemaSummaryByPlaytime(cinemaid, timefrom, timeto, movieid, opentype);
			}
			if(data!=null){
				Cinema cinema = daoService.getObject(Cinema.class, cinemaid);
				data.put("cinema", cinema);
				data.put("citycode", cinema.getCitycode());
				data.put("cinemaname", cinema.getName());
				dataList.add(data);
			}
		}
		model.put("ReportUtil", new ReportUtil());
		model.put("cityMap", AdminCityContant.getCitycode2CitynameMap());
		model.put("dataList", dataList);
		Collections.sort(dataList, new MultiPropertyComparator(new String[]{"citycode","cinemaname"}, new boolean[]{true, true}));
		
		downloadXls(isXls, response);
		return "merchant/report/summaryResult.vm";
	}
	@RequestMapping(method=RequestMethod.GET, value="/merchant/report/cinemaReport.xhtml")
	public String cinemaReportForm(Timestamp timefrom, Timestamp timeto, Long cinemaId, 
			String opentype, String timetype, ModelMap model, String isXls, HttpServletResponse response){
		if(timefrom!=null && timeto!=null && cinemaId!=null && timetype!=null){
			//直接调用
			return cinemaReport(timefrom, timeto, cinemaId, opentype, timetype, model, isXls, response);
		}
		Merchant merchant = getLogonMerchant();
		if(cinemaId!=null){
			if(!hasRights(merchant, cinemaId)) return show404(model, "无此影院权限！");
		}
		List<Cinema> cinemaList = daoService.getObjectList(Cinema.class, BeanUtil.getIdList(merchant.getRelatelist(), ","));
		model.put("cinemaList", cinemaList);
		return "merchant/report/cinemaReport.vm";
	}
	@RequestMapping(method=RequestMethod.POST, value="/merchant/report/cinemaReport.xhtml")
	public String cinemaReport(Timestamp timefrom, Timestamp timeto, Long cinemaId, 
			String opentype, String timetype, ModelMap model, String isXls, HttpServletResponse response){
		Merchant merchant = getLogonMerchant();
		if(!hasRights(merchant, cinemaId)) return show404(model, "无此影院权限！");
		if(timefrom==null || timeto==null) {
			return forwardMessage(model, "时间不正确！");
		}
		if(DateUtil.getDiffDay(timeto, timefrom)>31){
			return forwardMessage(model, "时间跨度不能大于1月！");
		}
		
		Cinema cinema = daoService.getObject(Cinema.class, cinemaId);
		model.put("cinema", cinema);
		model.put("ReportUtil", new ReportUtil());
		List<Map> dataList = null;
		if(StringUtils.equals(timetype, "addtime")){
			dataList = reportService.getTicketOrderDataByAddtime(cinemaId,null, timefrom, timeto, opentype);
		}else{
			dataList = reportService.getTicketOrderDataByPlaytime(cinemaId,null, timefrom, timeto, opentype);
		}
		Map<Long, OpenPlayItem> opiMap = new HashMap<Long, OpenPlayItem>();
		OpenPlayItem opi = null;
		for(Map entry : dataList){
			opi = daoService.getObjectByUkey(OpenPlayItem.class, "mpid", new Long(entry.get("mpid")+""), true);
			if(opi!=null)opiMap.put(opi.getMpid(), opi);
		}
		model.put("dataList", dataList);
		model.put("opiMap", opiMap);
		downloadXls(isXls, response);
		return "merchant/report/cinemaReportResult.vm";
	}
	@RequestMapping(method=RequestMethod.GET, value="/merchant/report/refundReport.xhtml")
	public String refundReport(ModelMap model){
		Merchant merchant = getLogonMerchant();
		List<Cinema> cinemaList = daoService.getObjectList(Cinema.class, BeanUtil.getIdList(merchant.getRelatelist(), ","));
		Map cinemaMap = new TreeMap();
		cinemaMap.putAll(BeanUtil.groupBeanList(cinemaList, "citycode"));
		model.put("cinemaMap", cinemaMap);
		model.put("cityMap", AdminCityContant.getCitycode2CitynameMap());
		return "merchant/report/refundReport.vm";
	}
	@RequestMapping(method=RequestMethod.POST, value="/merchant/report/refundReport.xhtml")
	public String refundReport(Timestamp timefrom, Timestamp timeto, String selectCinema, ModelMap model, String isXls, HttpServletResponse response){
		Merchant merchant = getLogonMerchant();
		if(timefrom==null || timeto==null) {
			return forwardMessage(model, "时间不正确！");
		}
		if(DateUtil.getDiffDay(timeto, timefrom)>31){
			return forwardMessage(model, "时间跨度不能大于1月！");
		}
		String citycode = null;
		Long cinemaId = null;
		List<Long> cinemaidList = new ArrayList<Long>();
		if(StringUtils.isNotBlank(selectCinema)){
			if(StringUtils.startsWith(selectCinema, "C")){
				citycode = selectCinema.substring(1);
			}else{
				cinemaId = Long.valueOf(selectCinema);
			}
		}
		if(cinemaId!=null){
			if(!hasRights(merchant, cinemaId)) return show404(model, "无此影院权限！");

			cinemaidList.add(cinemaId);
		}else{
			if(StringUtils.isNotBlank(citycode)){
				List<Cinema> cinemaList = daoService.getObjectList(Cinema.class, BeanUtil.getIdList(merchant.getRelatelist(), ","));
				Map<String, List<Cinema>> cinemaMap = BeanUtil.groupBeanList(cinemaList, "citycode");
				if(cinemaMap.containsKey(citycode)){
					cinemaidList.addAll(BeanUtil.getBeanPropertyList(cinemaMap.get(citycode), Long.class, "id", true));
				}
			}else{
				cinemaidList.addAll(BeanUtil.getIdList(merchant.getRelatelist(), ","));
			}
		} 
		List<OrderRefund> refundList = new ArrayList<OrderRefund>();
		for(Long cid: cinemaidList){
			List<OrderRefund> tmp = refundService.getSettleRefundList("ticket", timefrom, timeto, cid);
			refundList.addAll(tmp);
		}
		Map<String, TicketOrder> orderMap = new HashMap<String, TicketOrder>();
		for(OrderRefund refund: refundList){
			TicketOrder tmp = daoService.getObjectByUkey(TicketOrder.class, "tradeNo", refund.getTradeno(), false);
			orderMap.put(refund.getTradeno(), tmp);
		}
		Map<Long, Cinema> cinemaMap = daoService.getObjectMap(Cinema.class, cinemaidList);
		model.put("cinemaMap", cinemaMap);
		model.put("refundList", refundList);
		model.put("orderMap", orderMap);
		model.put("cityMap", AdminCityContant.getCitycode2CitynameMap());
		model.put("ReportUtil", new ReportUtil());
		downloadXls(isXls, response);
		return "merchant/report/refundReportResult.vm";
	}
	@RequestMapping(method=RequestMethod.GET, value="/merchant/report/goodsSummary.xhtml")
	public String goodsSummary(ModelMap model){
		Merchant merchant = getLogonMerchant();
		List<Cinema> cinemaList = daoService.getObjectList(Cinema.class, BeanUtil.getIdList(merchant.getRelatelist(), ","));
		Map cinemaMap = new TreeMap();
		cinemaMap.putAll(BeanUtil.groupBeanList(cinemaList, "citycode"));
		model.put("cinemaMap", cinemaMap);
		model.put("cityMap", AdminCityContant.getCitycode2CitynameMap());
		return "merchant/report/goodsSummary.vm";
	}
	@RequestMapping(method=RequestMethod.POST, value="/merchant/report/goodsSummary.xhtml")
	public String goodsSummaryResult(Timestamp timefrom, Timestamp timeto, String selectCinema, String timetype, ModelMap model, String isXls, HttpServletResponse response){
		if(timefrom==null || timeto==null) {
			return forwardMessage(model, "请选择时间范围！");
		}
		if(DateUtil.getDiffDay(timeto, timefrom)>31){
			return forwardMessage(model, "时间跨度不能大于1月！");
		}
		List<Long> cinemaidList = new ArrayList<Long>();
		Merchant merchant = getLogonMerchant();
		String citycode = null;
		Long cinemaId = null;
		if(StringUtils.isNotBlank(selectCinema)){
			if(StringUtils.startsWith(selectCinema, "C")){
				citycode = selectCinema.substring(1);
			}else{
				cinemaId = Long.valueOf(selectCinema);
			}
		}
		if(cinemaId!=null){
			if(!hasRights(merchant, cinemaId)) return show404(model, "无此影院权限！");
			cinemaidList.add(cinemaId);
		}else{
			if(StringUtils.isNotBlank(citycode)){
				List<Cinema> cinemaList = daoService.getObjectList(Cinema.class, BeanUtil.getIdList(merchant.getRelatelist(), ","));
				Map<String, List<Cinema>> cinemaMap = BeanUtil.groupBeanList(cinemaList, "citycode");
				if(cinemaMap.containsKey(citycode)){
					cinemaidList.addAll(BeanUtil.getBeanPropertyList(cinemaMap.get(citycode), Long.class, "id", true));
				}
			}else{
				cinemaidList.addAll(BeanUtil.getIdList(merchant.getRelatelist(), ","));
			}
		}
		List<Map> dataList = new ArrayList<Map>();
		
		for(Long cinemaid: cinemaidList){
			Map data = null;
			if("addtime".equals(timetype)){
				data = reportService.getGoodsSummaryByAddtime(cinemaid, timefrom, timeto,false);
			}else{
				data = reportService.getGoodsSummaryByTaketime(cinemaid, timefrom, timeto);
			}
			if(data!=null){
				data.put("cinema", daoService.getObject(Cinema.class, cinemaid));
				dataList.add(data);
			}
		}
		model.put("ReportUtil", new ReportUtil());
		model.put("cityMap", AdminCityContant.getCitycode2CitynameMap());
		model.put("dataList", dataList);
		downloadXls(isXls, response);
		return "merchant/report/goodsSummaryResult.vm";
	}
	@RequestMapping("/merchant/report/goodsReport.xhtml")
	public String goodsReport(@RequestParam("cinemaId")Long cinemaId, String timetype, Timestamp timefrom, Timestamp timeto, ModelMap model, String isXls, HttpServletResponse response){
		Merchant merchant = getLogonMerchant();
		if(!hasRights(merchant, cinemaId)) return show404(model, "无此影院权限！");

		CinemaProfile profile = daoService.getObject(CinemaProfile.class, cinemaId);
		model.put("profile", profile);
		if(timefrom==null || timeto==null) {
			return forwardMessage(model, "请选择时间范围！");
		}
		if(DateUtil.getDiffDay(timeto, timefrom)>31){
			return forwardMessage(model, "时间跨度不能大于1月！");
		}
		List<GoodsOrder> orderList = null;
		if("addtime".equals(timetype)){
			orderList = reportService.getCinemaGoodsOrderByAddtime(cinemaId, timefrom, timeto,false);
		}else{
			orderList = reportService.getCinemaGoodsOrderByTaketime(cinemaId, timefrom, timeto);
		}
		Collections.sort(orderList, new PropertyComparator("addtime", false, false));
		model.put("orderList", orderList);
		model.put("cinema", daoService.getObject(Cinema.class, cinemaId));
		model.put("ReportUtil", new ReportUtil());
		downloadXls(isXls, response);
		return "merchant/report/goodsReport.vm";
	}
	@RequestMapping("/merchant/report/orderList.xhtml")
	public String orderList(Long mpid, ModelMap model, String isXls, HttpServletResponse response){
		OpenPlayItem opi = daoService.getObjectByUkey(OpenPlayItem.class, "mpid", mpid);
		Merchant merchant = getLogonMerchant();
		if(!hasRights(merchant, opi.getCinemaid())) return show404(model, "无此影院权限！");

		model.put("ReportUtil", new ReportUtil());
		model.put("opi", opi);
		List<TicketOrder> orderList = orderQueryService.getTicketOrderListByMpid(mpid, OrderConstant.STATUS_PAID_SUCCESS);
		model.put("orderList", orderList);
		downloadXls(isXls, response);
		return "merchant/report/orderList.vm";
	}
}
