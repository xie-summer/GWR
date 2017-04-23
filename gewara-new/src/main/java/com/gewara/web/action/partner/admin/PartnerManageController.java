package com.gewara.web.action.partner.admin;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.gewara.command.SearchOrderCommand;
import com.gewara.constant.Status;
import com.gewara.constant.TagConstant;
import com.gewara.constant.sys.JsonDataKey;
import com.gewara.constant.ticket.OrderConstant;
import com.gewara.model.acl.WebModule;
import com.gewara.model.api.CooperUser;
import com.gewara.model.common.JsonData;
import com.gewara.model.movie.Cinema;
import com.gewara.model.pay.BuyItem;
import com.gewara.model.pay.GewaOrder;
import com.gewara.model.pay.OrderNote;
import com.gewara.model.pay.TicketOrder;
import com.gewara.service.OperationService;
import com.gewara.service.order.OrderNoteService;
import com.gewara.service.order.OrderQueryService;
import com.gewara.untrans.UntransService;
import com.gewara.util.DateUtil;
import com.gewara.util.JsonUtils;
import com.gewara.web.action.partner.BasePartnerController;
import com.gewara.web.menu.GBMenuDataBuilder;
import com.gewara.web.menu.MenuRepository;
import com.gewara.web.support.AclService;
import com.gewara.web.util.PageUtil;
@Controller
public class PartnerManageController extends BasePartnerController{
	@Autowired@Qualifier("untransService")
	private UntransService untransService;
	public void setUntransService(UntransService untransService) {
		this.untransService = untransService;
	}
	@Autowired@Qualifier("orderQueryService")
	private OrderQueryService orderQueryService;
	@Autowired
	private AclService aclService;
	@Autowired@Qualifier("operationService")
	private OperationService operationService;
	private static final Long YONGHUAID = 10L;
	
	@Autowired@Qualifier("orderNoteService")
	private OrderNoteService orderNoteService;
	
	@RequestMapping("/partner/admin/api.xhtml")
	public String api(ModelMap model){
		CooperUser partner = getLogonCooperUser();
		if( partner==null ) return showError(model, "非法用户！");
		model.put("partner", partner);
		return "partner/admin/apiIndex.vm";
	}
	
	@RequestMapping("/partner/admin/gwapi.xhtml")
	public String gewaApi(ModelMap model){
		CooperUser partner = getLogonCooperUser();
		if( partner==null ) return showError(model, "非法用户！");
		model.put("partner", partner);
		return "partner/admin/gwapiIndex.vm";
	}
	
	@RequestMapping("/partner/admin/mobileApi.xhtml")
	public String mobileApi(ModelMap model){
		CooperUser partner = getLogonCooperUser();
		if( partner==null ) return showError(model, "非法用户！");
		model.put("partner", partner);
		return "partner/admin/mobileApiIndex.vm";
	}
	@RequestMapping("/partner/admin/apiList.xhtml")
	public String redirectApi(String apiName){
		return "api/doc/" + apiName + ".vm";
	}
	
	@RequestMapping("/partner/admin/gwapiList.xhtml")
	public String redirectGwApi(String apiName){
		return "api/gwdoc/" + apiName + ".vm";
	}
	
	@RequestMapping("/partner/admin/mobileapiList.xhtml")
	public String redirectMobileApi(String apiName){
		return "api/mobiledoc/" + apiName + ".vm";
	}
	@RequestMapping("/partner/admin/sportApi.xhtml")
	public String redirectSportApi(String apiName){
		return "api/sportdoc/" + apiName + ".vm";
	}

	@RequestMapping("/partner/admin/console.xhtml")
	public String adminConsole(String reload, ModelMap model){
		CooperUser partner = getLogonCooperUser();
		if( partner==null ) return showError(model, "非法用户！");
		model.put("partner", partner);
		MenuRepository repository = (MenuRepository)applicationContext.getServletContext().getAttribute(MenuRepository.PARTNER_MENU_REPOSITORY_KEY);
		if(repository==null || "true".equals(reload)){
			repository = new MenuRepository(aclService.getMenuList(WebModule.TAG_PARTNER));
			applicationContext.getServletContext().setAttribute(MenuRepository.PARTNER_MENU_REPOSITORY_KEY, repository);
		}
		List<GrantedAuthority> granted = partner.getAuthorities();
		String[] roles = new String[granted.size()];
		for (int i = 0; i < granted.size(); i++) {
			roles[i]=granted.get(i).getAuthority();
		}
		GBMenuDataBuilder mdb = new GBMenuDataBuilder(config.getBasePath(), roles, repository);
		String menuData = mdb.getMenuData().toString();
		if(StringUtils.isNotBlank(partner.getTag())){
			return "redirect:/partner/admin/locQry.xhtml";
		}
		model.put("menuData", menuData);
		return "partner/admin/adminConsole.vm";
	}
	@RequestMapping(value="/partner/login.xhtml",method=RequestMethod.GET)
	public String partnerLogin(){
		return "partner/login.vm";
	}
	@RequestMapping(value="/partner/admin/orderList.xhtml", method=RequestMethod.GET)
	public String orderList(ModelMap model){
		CooperUser partner = getLogonCooperUser();
		if(partner==null) return showMessage(model, "请登录！");
		model.put("partner", partner);
		Date date1 = new Date();
		List<String> dateList = new ArrayList<String>();
		for(int i=0; i<5; i++){
			dateList.add(DateUtil.formatDate(DateUtil.addDay(date1, i)));
		}
		model.put("dateList", dateList);
		model.put("timeFrom", DateUtil.formatTimestamp(DateUtil.addDay(DateUtil.getCurTruncTimestamp(), -30)));
		model.put("timeTo", DateUtil.getCurFullTimestampStr());
		return "partner/admin/orderList.vm";
	}	
	@RequestMapping("/partner/admin/sendMessageByTradeNo.xhtml")
	public String sendMessageById(Long tradeno, ModelMap model){
		CooperUser partner = getLogonCooperUser();
		TicketOrder order = daoService.getObjectByUkey(TicketOrder.class, "tradeNo", tradeno, false);
		if(order == null) return showJsonError(model, "查询信息不存在！");
		if(!order.getPartnerid().equals(partner.getPartnerid())) {
			return showJsonError(model, "不能查询其他商家订单!");
		}
		if(!StringUtils.equals(order.getStatus(), OrderConstant.STATUS_PAID_SUCCESS)){
			return showJsonError(model, "非成功的订单不能发送消息");
		}
		if(!order.isPaidSuccess()) return showJsonError(model,"未成功的订单！");
		String opkey = OperationService.TAG_SENDTICKETPWD + partner.getPartnerid() + order.getId();
		if(!operationService.isAllowOperation(opkey, OperationService.ONE_DAY * 3, 3)){
			return showJsonError(model,  "同一订单最多只能发送3次！");
		}
		untransService.reSendOrderMsg(order);
		operationService.updateOperation(opkey, OperationService.ONE_DAY * 3, 3);
		return showJsonSuccess(model, "发送成功！");
	}
	@RequestMapping(value="/partner/admin/orderList.xhtml", method=RequestMethod.POST)
	public String orderList(String report, Date date, SearchOrderCommand soc, ModelMap model){
		CooperUser partner = getLogonCooperUser();
		if(partner==null) return showMessage(model, "请登录！");
		model.put("partner", partner);
		if(date !=null){
			soc.setTimeFrom(new Timestamp(DateUtil.getBeginningTimeOfDay(date).getTime()));
			soc.setTimeTo(new Timestamp(DateUtil.getLastTimeOfDay(date).getTime()));
		}
		if(soc.getTimeFrom() == null || soc.getTimeTo() == null){
			return showError(model, "时间必选！");
		}
		if(soc.getTimeTo().after(DateUtil.addDay(soc.getTimeFrom(), 31))){
			return showError(model, "时间跨度不能大于1个月！");
		}
		Date date1 = new Date();
		List<String> dateList = new ArrayList<String>();
		for(int i=0; i<5; i++){
			dateList.add(DateUtil.formatDate(DateUtil.addDay(date1, i)));
		}
		model.put("dateList", dateList);
		List<TicketOrder> orderList = orderQueryService.getTicketOrderList(partner, soc);
		Map<Long, String> cinemanameMap = new HashMap<Long, String>();
		for(TicketOrder order : orderList){
			if(!cinemanameMap.containsKey(order.getCinemaid())){
				Cinema cinema = daoService.getObject(Cinema.class, order.getCinemaid());
				cinemanameMap.put(cinema.getId(), cinema.getRealBriefname());
			}
		}
		model.put("timeFrom", DateUtil.formatTimestamp(soc.getTimeFrom()));
		model.put("timeTo", DateUtil.formatTimestamp(soc.getTimeTo()));
		model.put("orderList", orderList);
		model.put("cinemanameMap", cinemanameMap);
		model.put("ordertype", StringUtils.isBlank(soc.getOrdertype())?" " : soc.getOrdertype());
		model.put("cinema", daoService.getObject(Cinema.class, YONGHUAID));
		if(StringUtils.isNotBlank(report)){
			model.put("orderListSize", orderList.size());
			model.put(REPORT_DATA_KEY, orderList);
			return showReportView(model, report, "partner/orderList.jasper");
		}
		return "partner/admin/orderList.vm";
	}
	@RequestMapping("/partner/admin/orderReport.xhtml")
	public String orderReport(Date dateFrom, Date dateTo, SearchOrderCommand soc, ModelMap model){
		CooperUser partner = getLogonCooperUser();
		if(partner==null) return showMessage(model, "请登录！");
		model.put("partner", partner);
		if(dateFrom !=null)  
			soc.setTimeFrom(new Timestamp(dateFrom.getTime()));
		if(dateTo != null) soc.setTimeTo(DateUtil.getLastTimeOfDay(new Timestamp(dateTo.getTime())));
		List<Map> dataMap = orderQueryService.getTicketOrderListByDate(partner, soc);
		model.put("dataMap", dataMap);
		return "partner/admin/orderReport.vm";
	}
	@RequestMapping("/partner/admin/manageDetail.xhtml")
	public String manageDetail(ModelMap model){
		CooperUser partner = getLogonCooperUser();
		if( partner==null ) return showError(model, "非法用户！");
		model.put("partner", partner);
		return "partner/admin/manageDetail.vm";
	}
	
	
	@RequestMapping("/partner/admin/orderOriginReport.xhtml")
	public String orderOriginReport(SearchOrderCommand soc, ModelMap model){
		CooperUser partner = getLogonCooperUser();
		Timestamp curtime = DateUtil.getCurFullTimestamp();
		if( soc.getTimeFrom()  ==null){
			soc.setTimeFrom(DateUtil.addDay(curtime, -7));
		}
		if(soc.getTimeTo() == null){
			soc.setTimeTo(curtime);
		}
		if(soc.getTimeTo().after(DateUtil.addDay(soc.getTimeFrom(), 7))){
			return forwardMessage(model, "时间跨度不能大于7天！");
		}
		model.put("partner", partner);
		List<GewaOrder> oriList = orderQueryService.getOrderOriginListByDate(partner,soc);
		model.put("oriList", oriList);
		model.put("appSourcesMap", getAppSourceMap());
		return "partner/admin/oriOrderList.vm";
	}
	
	@RequestMapping(value="/partner/admin/appsourceList.xhtml", method=RequestMethod.GET)
	public String orderReportByCooperuser(ModelMap model){
		CooperUser partner = getLogonCooperUser();
		model.put("partner", partner);
		model.put("appSourcesMap", getAppSourceMap());
		return "partner/admin/appsourceOrderList.vm";
	}
	
	@RequestMapping(value="/partner/admin/appsourceList.xhtml", method=RequestMethod.POST)
	public String orderReportByCooperuser(Timestamp dateFrom, Timestamp dateTo, String appsource, ModelMap model){
		if(StringUtils.isBlank(appsource) || dateFrom==null || dateTo==null){
			return forwardMessage(model, "缺少查询参数");
		}
		if(dateTo.after(DateUtil.addDay(dateFrom, 7))){
			return showError(model, "时间跨度不能大于7天！");
		}
		CooperUser partner = getLogonCooperUser();
		if(!partner.getAsList().contains(appsource)){
			return forwardMessage(model, "没有权限访问");
		}
		List<GewaOrder> oriList = orderQueryService.getOrderAppsourceListByDate(partner, dateFrom, dateTo, appsource);
		model.put("oriList", oriList);
		model.put("partner", partner);
		model.put("appSourcesMap", getAppSourceMap());
		return "partner/admin/appsourceOrderList.vm";
	}
	private Map<String, String> getAppSourceMap(){
		String key = JsonDataKey.KEY_MOBILE_APPSOURCE;
		JsonData jsonData = daoService.getObject(JsonData.class, key);
		Map<String, String> appSourcesMap = JsonUtils.readJsonToMap(jsonData.getData());
		return new TreeMap(appSourcesMap);
	}
	
	@RequestMapping(value="/partner/admin/locQry.xhtml")
	public String locQry(Timestamp fromtime, Timestamp totime, String mobile, String checkpass, Integer pageNo, ModelMap model){
		Timestamp curtime = DateUtil.getCurFullTimestamp();
		if(fromtime==null){
			fromtime = DateUtil.addDay(curtime, -10);
		}
		if(totime==null){
			totime = curtime;
		}
		int day = DateUtil.getDiffDay(fromtime, totime);
		if(day>31){
			return forwardMessage(model, "查询时间跨度不能查过31天");
		}
		if(pageNo==null){
			pageNo = 0;
		}
		CooperUser partner = getLogonCooperUser();
		model.put("user", partner);
		if(StringUtils.isBlank(partner.getTag())){
			return forwardMessage(model, "你没有权限");
		}
		String placeids = partner.getRelatedids();
		String itemids = partner.getCategoryids();
		int rows = 10;
		int count = orderNoteService.getOrderNoteCountByPlaceids(TagConstant.TAG_DRAMA, mobile, checkpass, fromtime, totime, placeids, itemids);
		List<OrderNote> noteList = orderNoteService.getOrderNoteListByPlaceids(TagConstant.TAG_DRAMA, mobile, checkpass, fromtime, totime, 
				placeids, itemids, pageNo * rows, rows);
		Map<Long, Integer> disMap = new HashMap<Long, Integer>();
		Map<Long, Integer> totalMap = new HashMap<Long, Integer>();
		for(OrderNote note : noteList){
			List<BuyItem> itemList = daoService.getObjectListByField(BuyItem.class, "orderid", note.getOrderid());
			int dis = 0, total = 0;
			for(BuyItem bi : itemList){
				if(bi.getRelatedid().equals(note.getSmallitemid())){
					dis = dis + bi.getDiscount();
					total = total + bi.getTotalfee();
				}
			}
			disMap.put(note.getId(), dis);
			totalMap.put(note.getId(), total);
		}
		PageUtil pageUtil = new PageUtil(count, rows, pageNo, "partner/admin/locQry.xhtml");
		Map params = new HashMap();
		if(StringUtils.isNotBlank(mobile)){
			params.put("mobile", mobile);
		}
		params.put("fromtime", DateUtil.formatTimestamp(fromtime));
		params.put("totime", DateUtil.formatTimestamp(totime));
		pageUtil.initPageInfo(params);
		model.put("pageUtil", pageUtil);
		
		model.put("count", count);
		model.put("disMap", disMap);
		model.put("totalMap", totalMap);
		model.put("noteList", noteList);
		model.put("fromtime", DateUtil.formatTimestamp(fromtime));
		model.put("totime", DateUtil.formatTimestamp(totime));
		return "partner/admin/unionPayActivity.vm";
	}
	@RequestMapping(value="/partner/admin/getTicketStatus.xhtml")
	public String setTicketStatus(Long id, ModelMap model){
		OrderNote note = daoService.getObject(OrderNote.class, id);
		model.put("note", note);
		return "partner/admin/qpModify.vm";
	}
	@RequestMapping(value="/partner/admin/setTicketStatus.xhtml")
	public String setTicketStatus(Long id, String qptype, String qpremark, ModelMap model){
		OrderNote note = daoService.getObject(OrderNote.class, id);
		Map<String, String> otherMap = JsonUtils.readJsonToMap(note.getOtherinfo());
		otherMap.put("qpremark", qpremark);
		otherMap.put("qptype", qptype);
		note.setOtherinfo(JsonUtils.writeMapToJson(otherMap));
		if(StringUtils.equals(qptype, "people") || StringUtils.equals(qptype, "pos")){
			note.setTaketime(DateUtil.getCurFullTimestamp());
			note.setResult(Status.Y);
		}
		daoService.saveObject(note);
		return showJsonSuccess(model);
	}
}
