package com.gewara.web.action.admin.sport;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import com.gewara.Config;
import com.gewara.command.SearchOrderCommand;
import com.gewara.constant.OpenTimeItemConstant;
import com.gewara.constant.OpenTimeTableConstant;
import com.gewara.constant.PaymethodConstant;
import com.gewara.constant.Status;
import com.gewara.constant.TagConstant;
import com.gewara.constant.sys.LogTypeConstant;
import com.gewara.constant.sys.MongoData;
import com.gewara.constant.ticket.OpiConstant;
import com.gewara.constant.ticket.OrderConstant;
import com.gewara.helper.TimeItemHelper;
import com.gewara.json.RelateToSettle;
import com.gewara.model.acl.User;
import com.gewara.model.pay.ElecCardBatch;
import com.gewara.model.pay.PayBank;
import com.gewara.model.pay.SMSRecord;
import com.gewara.model.pay.SportOrder;
import com.gewara.model.sport.CusOrder;
import com.gewara.model.sport.OpenTimeItem;
import com.gewara.model.sport.OpenTimeSale;
import com.gewara.model.sport.OpenTimeTable;
import com.gewara.model.sport.ProgramItemTime;
import com.gewara.model.sport.SellTimeTable;
import com.gewara.model.sport.Sport;
import com.gewara.model.sport.Sport2Item;
import com.gewara.model.sport.SportField;
import com.gewara.model.sport.SportItem;
import com.gewara.model.sport.SportProfile;
import com.gewara.model.sport.SportSettle;
import com.gewara.mongo.MongoService;
import com.gewara.service.MessageService;
import com.gewara.service.SettleService;
import com.gewara.service.api.ApiSportService;
import com.gewara.service.gewapay.PaymentService;
import com.gewara.service.sport.OpenTimeTableService;
import com.gewara.service.sport.SportOrderService;
import com.gewara.service.sport.SportService;
import com.gewara.support.ErrorCode;
import com.gewara.support.ServiceHelper;
import com.gewara.untrans.UntransService;
import com.gewara.untrans.sport.RemoteSportService;
import com.gewara.untrans.sport.SportUntransService;
import com.gewara.untrans.ticket.OrderProcessService;
import com.gewara.util.BeanUtil;
import com.gewara.util.BindUtils;
import com.gewara.util.ChangeEntry;
import com.gewara.util.DateUtil;
import com.gewara.util.JsonUtils;
import com.gewara.util.StringUtil;
import com.gewara.util.ValidateUtil;
import com.gewara.util.VmUtils;
import com.gewara.util.WebUtils;
import com.gewara.web.action.admin.BaseAdminController;
import com.gewara.web.util.PageUtil;
import com.gewara.xmlbind.sport.GstOtt;
import com.gewara.xmlbind.sport.GstSportField;

@Controller
public class SportOttAdminController extends BaseAdminController {
	@Autowired @Qualifier("jdbcTemplate")
	private JdbcTemplate jdbcTemplate;
	public void setJdbcTemplate(JdbcTemplate template) {
		jdbcTemplate = template;
	}
	@Autowired@Qualifier("untransService")
	private UntransService untransService;
	public void setUntransService(UntransService untransService) {
		this.untransService = untransService;
	}
	
	@Autowired@Qualifier("sportUntransService")
	private SportUntransService sportUntransService;
	@Autowired@Qualifier("sportOrderService")
	private SportOrderService sportOrderService;
	public void setSportOrderService(SportOrderService sportOrderService) {
		this.sportOrderService = sportOrderService;
	}
	@Autowired@Qualifier("sportService")
	private SportService sportService;
	public void setSportService(SportService sportService) {
		this.sportService = sportService;
	}
	@Autowired@Qualifier("mongoService")
	private MongoService mongoService;
	public void setMongoService(MongoService mongoService) {
		this.mongoService = mongoService;
	}
	@Autowired@Qualifier("messageService")
	private MessageService messageService;
	public void setMessageService(MessageService messageService) {
		this.messageService = messageService;
	}
	@Autowired@Qualifier("apiSportService")
	public ApiSportService apiSportService;
	public void setApiSportService(ApiSportService apiSportService) {
		this.apiSportService = apiSportService;
	}
	@Autowired@Qualifier("orderProcessService")
	private OrderProcessService orderProcessService;
	public void setProcessOrderService(OrderProcessService orderProcessService) {
		this.orderProcessService = orderProcessService;
	}
	
	@Autowired@Qualifier("openTimeTableService")
	private OpenTimeTableService openTimeTableService;
	public void setOpenTimeTableService(OpenTimeTableService openTimeTableService){
		this.openTimeTableService = openTimeTableService;
	}
	@Autowired@Qualifier("paymentService")
	private PaymentService paymentService;
	@Autowired@Qualifier("config")
	protected Config config;
	
	@Autowired@Qualifier("remoteSportService")
	private RemoteSportService remoteSportService;
	@Autowired@Qualifier("settleService")
	private SettleService settleService;
	//基础数据
	@RequestMapping("/admin/sport/open/baseData.xhtml")
	public String baseData(Long sportid, Long itemid, ModelMap model) {
		Sport sport = daoService.getObject(Sport.class, sportid);
		if(sport == null) return alertMessage(model, "场馆不存在!");
		SportProfile profile = daoService.getObject(SportProfile.class, sportid);
		SportItem curitem = daoService.getObject(SportItem.class, itemid);
		Map<Long, List<SportField>> fieldMapList = new HashMap<Long, List<SportField>>();
		List<SportItem> itemList = sportService.getSportItemListBySportId(sport.getId(), null);
		if(curitem != null){
			model.put("curitem", curitem);
			Sport2Item sport2Item = sportService.getSport2Item(sport.getId(), curitem.getId());
			model.put("sport2Item", sport2Item);
			fieldMapList.put(curitem.getId(), sportOrderService.getSportFieldList(sport.getId(), curitem.getId()));
		}else{
			for(SportItem item : itemList){
				fieldMapList.put(item.getId(), sportOrderService.getSportFieldList(sport.getId(), item.getId()));
			}
		}
		model.put("cursport", sport);
		model.put("profile", profile);
		model.put("itemList", itemList);
		model.put("fieldMapList", fieldMapList);
		return "admin/sport/open/baseData.vm";
	}
	//保存基础数据
	@RequestMapping("/admin/sport/open/saveBaseData.xhtml")
	public String saveBaseData(Long id, ModelMap model, HttpServletRequest request) {
		Sport sport = daoService.getObject(Sport.class, id);
		if(sport == null) return showJsonError(model, "场馆不存在或被删除！");
		SportProfile profile = null;
		if(id != null){
			profile = daoService.getObject(SportProfile.class, id);
			if(profile == null) return showJsonError_NOT_FOUND(model);
		}else{
			profile = new SportProfile(id);
		}
		Map<String, String> dataMap = WebUtils.getRequestMap(request);
		BindUtils.bindData(profile, dataMap);
		if(StringUtils.isBlank(profile.getEncryptCode())) {
			String encryptCode = StringUtil.md5("!@#$%sportid^&*()" + id);
			profile.setEncryptCode(encryptCode);
		}
		sport.setBooking(profile.getBooking());
		sport.setUpdatetime(DateUtil.getCurFullTimestamp());
		if(StringUtils.isBlank(sport.getCitycode())) return showJsonError(model, "城市不能为空！");
		profile.setCitycode(sport.getCitycode());
		daoService.saveObject(sport);
		daoService.saveObject(profile);
		model.put("sportid", id);
		return showJsonSuccess(model);
	}
	
	@RequestMapping("/admin/sport/open/saveSport2Item.xhtml")
	public String saveSport2Item(Long id, String openStatus, Integer openBefore, ModelMap model, HttpServletRequest request){
		Sport2Item sport2Item = daoService.getObject(Sport2Item.class, id);
		if(sport2Item == null) return showJsonError(model, "该对象不存在！");
		ChangeEntry changeEntry = new ChangeEntry(sport2Item);
		Map dataMap = request.getParameterMap();
		BindUtils.bindData(sport2Item, dataMap);
		Map otherinfoMap = JsonUtils.readJsonToMap(sport2Item.getOtherinfo());
		otherinfoMap.put(Sport2Item.OPEN_STATUS, openStatus);
		if(openBefore != null)otherinfoMap.put(Sport2Item.OPEN_BEFORE, openBefore+"");
		sport2Item.setOtherinfo(JsonUtils.writeMapToJson(otherinfoMap));
		daoService.saveObject(sport2Item);
		monitorService.saveChangeLog(getLogonUser().getId(), Sport2Item.class, sport2Item.getId(), changeEntry.getChangeMap(sport2Item));
		return showJsonSuccess(model);
	}
	//场地
	@RequestMapping("/admin/sport/open/sportField.xhtml")
	public String sportField(Long fid, ModelMap model) {
		SportField field = daoService.getObject(SportField.class, fid);
		Map jsonMap = new HashMap();
		jsonMap.put("id", field.getId());
		jsonMap.put("ordernum", field.getOrdernum());
		jsonMap.put("name", field.getName());
		jsonMap.put("description", field.getDescription());
		return showJsonSuccess(model, jsonMap);
	}
	//修改场地
	@RequestMapping("/admin/sport/open/modSportField.xhtml")
	public String modSportField(Long sid, ModelMap model) {
		SportField sf = daoService.getObject(SportField.class, sid);
		if(sf==null) return showJsonError(model, "记录不存在！");
		String status = "Y";
		if(StringUtils.equals("Y", sf.getStatus())) status = "N";
		sf.setStatus(status);
		Map result = new HashMap();
		daoService.saveObject(sf);
		dbLogger.warnWithType(LogTypeConstant.LOG_TYPE_USERACTION, getLogonUser().getId()+"修改：" + sid + sf.getStatus());
		result.put("status", status);
		return showJsonSuccess(model,result);
	}
	//设置场馆的预定状态
	@RequestMapping("/admin/sport/open/changeBooking.xhtml")
	public String changeBooking(Long sid, ModelMap model) {
		User user = getLogonUser();
		Sport sport = daoService.getObject(Sport.class, sid);
		String status = sport.getBooking();
		if (Sport.BOOKING_OPEN.equals(sport.getBooking()))
			sport.setBooking(Sport.BOOKING_CLOSE);
		else if (Sport.BOOKING_CLOSE.equals(sport.getBooking()))
			sport.setBooking(Sport.BOOKING_OPEN);
		daoService.saveObject(sport);
		dbLogger.warnWithType(LogTypeConstant.LOG_TYPE_USERACTION, "场馆"+sport.getId()+"状态:" + user.getId()+"" + status +"--->" + sport.getBooking());
		return showJsonSuccess(model);
	}
	//开放场次列表
	@RequestMapping("/admin/sport/open/ottList.xhtml")
	public String openTimeTableList(Long sportid, Long itemid, Date qryDate, ModelMap model) {
		Date datefrom = null;
		Date dateto = null;
		if(qryDate!=null) {
			datefrom = qryDate;
			dateto = DateUtil.getLastTimeOfDay(datefrom);
		}else {
			datefrom = DateUtil.getBeginningTimeOfDay(new Date());
		}
		Map<Integer, List<Sport>> spMap = sportOrderService.getProfileSportList();
		Map<Long,Integer> sportOpenTimeTableCountMap=new HashMap<Long,Integer>();
		List<Sport> sportList = new ArrayList<Sport>();
		for(Integer sortnum : spMap.keySet()){
			for(Sport sport : spMap.get(sortnum)){
				sportList.add(sport);
				Integer count=sportOrderService.getSportOpenTimeTableCount(sport.getId());
				sportOpenTimeTableCountMap.put(sport.getId(), count);
			}
		}
		model.put("sportOpenTimeTableCountMap", sportOpenTimeTableCountMap);
		Sport sport = null;
		if(sportid!=null) {
			sport = daoService.getObject(Sport.class, sportid);
		}else {
			sport = sportList.get(0);
			sportid = sport.getId();
		}
		Map<Long ,SportItem> itemMap = new HashMap<Long, SportItem>();
		List<OpenTimeTable> ottList = sportOrderService.getOttList(sportid, itemid, datefrom, dateto, false);
		List<SportItem> itemList = sportOrderService.getOpenSportItemList(sportid, datefrom, dateto, false);
		Map<Long, Integer> countMap = new HashMap<Long, Integer>();
		int count = 0;
		int sum = 0;
		for(SportItem item : itemList){
			if(!itemMap.containsKey(item.getId())) itemMap.put(item.getId(), item);
			count = sportOrderService.getOttCount(sportid, item.getId(), datefrom, dateto, false);
			sum = sum + count;
			countMap.put(item.getId(), count);
		}
		model.put("sum", sum);
		model.put("cursport", sport);
		model.put("ottList", ottList);
		model.put("itemList", itemList);
		model.put("itemMap", itemMap);
		model.put("countMap", countMap);
		model.put("spMap", spMap);
		model.put("sportList", sportList);
		return "admin/sport/open/ottList.vm";
	}
	//开放场次详细
	@RequestMapping("/admin/sport/open/ottDetail.xhtml")
	public String openTimeTableDetail(Long ottid, ModelMap model,SearchOrderCommand soc) {
		OpenTimeTable ott = daoService.getObject(OpenTimeTable.class, ottid);
		Long sportid = ott.getSportid();
		Long itemid = ott.getItemid();
		Sport sport = daoService.getObject(Sport.class, sportid);
		SportItem item = daoService.getObject(SportItem.class, itemid);
		soc.setOttid(ottid);
		List<SportOrder> orderList = sportOrderService.getSportOrderList(soc);
		List<OpenTimeItem> otiList = openTimeTableService.getOpenItemList(ott.getId());
		model.put("ott", ott);
		model.put("item", item);
		model.put("sport", sport);
		model.put("orderList", orderList);
		model.put("itemHelper", new TimeItemHelper(otiList));
		model.put("ordertype", soc.getOrdertype());
		model.put("ottid", soc.getOttid());
		List<SportField> fieldList = sportOrderService.getAllSportFieldList(ottid);
		model.put("fieldList", fieldList);
		if(ott.hasField()){
			List<String> playHourList = sportOrderService.getPlayHourList(ottid, null);
			Map<Long, Boolean> resMap = new HashMap<Long, Boolean>();
			for(SportOrder order : orderList){
				CusOrder cusOrder = daoService.getObjectByUkey(CusOrder.class, "orderid", order.getId(), false);
				if(cusOrder!=null && StringUtils.equals(cusOrder.getStatus(), OrderConstant.STATUS_PAID_SUCCESS) && StringUtils.isBlank(cusOrder.getResponse())){
					resMap.put(order.getId(), true);
				}else {
					resMap.put(order.getId(), false);
				}
			}
			model.put("resMap", resMap);
			model.put("playHourList", playHourList);
			model.put("checkValue", StringUtil.md5(""+ott.getSportid()+ott.getItemid()+ott.getRemoteid()));
		}else if(ott.hasPeriod()||ott.hasInning()){
			List<OpenTimeItem> itemList = openTimeTableService.getOpenItemList(ottid);
			model.put("itemList", itemList);
			return "admin/sport/open/ottPeriodDetail.vm";
		}
		return "admin/sport/open/ottDetail.vm";
	}
	@RequestMapping("/admin/sport/open/fixOrder.xhtml")
	public String fixOrder(Long orderid, ModelMap model) {
		SportOrder order = daoService.getObject(SportOrder.class, orderid);
		if(!StringUtils.equals(order.getStatus(), OrderConstant.STATUS_PAID_SUCCESS)){
			return showJsonError(model, "非支付成功的订单，不能确认");
		}
		ErrorCode<String> code = sportUntransService.updateCuOrder(order, null);
		if(!code.isSuccess()) return showJsonError(model, code.getMsg());
		return showJsonSuccess(model);
	}
	//设置每个场次开放状态
	@RequestMapping("/admin/sport/open/setOTTStatus.xhtml")
	public String setOTIPrice(Long ottid, String status, ModelMap model) {
		OpenTimeTable ott = daoService.getObject(OpenTimeTable.class, ottid);
		Long sportid = ott.getSportid();
		Long itemid = ott.getItemid();
		Date playdate = ott.getPlaydate();
		if(StringUtils.equals(status, "R") || StringUtils.equals(status, "D")){ //删除
			ErrorCode<String> code = sportOrderService.delOtt(ottid);
			if(!code.isSuccess()) return showJsonError(model, code.getMsg());
			if(StringUtils.equals(status, "R")){
				String result = forceSynch(sportid, itemid, playdate, null);
				return showJsonSuccess(model, result);
			}else{
				return showJsonSuccess(model);
			}
		}else {
			if(StringUtils.equals(status, "Y")){
				List<OpenTimeItem> otiList = openTimeTableService.getOpenItemList(ott.getId());
				for(OpenTimeItem item : otiList){
					if(item.hasZeroPrice()) {
						return showJsonError(model, "有场地价格为0，请核实后开放！");
					}
				}
			}
			ott.setStatus(status);
			daoService.saveObject(ott);
			if(StringUtils.equals(status, "Y")){
				openTimeTableService.updateOttOtherData(ott);
			}
			/*if(StringUtils.equals(status, "D")){//取消发送短信
				List<String> orderList = orderQueryService.getTradeNoListByMpid(TagConstant.TAG_SPORT, ott.getId(), OrderConstant.STATUS_PAID_SUCCESS);
				commonService.updateSMSRecordStatus(orderList);
			}*/
			return showJsonSuccess(model);
		}
		
	}
	//设置每个场次开放预定时间
	@RequestMapping("/admin/sport/open/setOTTOpentime.xhtml")
	public String setOTTOpentime(Long ottid, Timestamp opentime, ModelMap model) {
		OpenTimeTable ott = daoService.getObject(OpenTimeTable.class, ottid);
		ott.setOpentime(opentime);
		daoService.saveObject(ott);
		return showJsonSuccess(model);
	}
	//设置每个场次结束预定时间
	@RequestMapping("/admin/sport/open/setOTTCloseime.xhtml")
	public String setOTTCloseime(Long ottid, Timestamp closetime, ModelMap model) {
		OpenTimeTable ott = daoService.getObject(OpenTimeTable.class, ottid);
		ott.setClosetime(closetime);
		daoService.saveObject(ott);
		return showJsonSuccess(model);
	}
	//废弃开放的场次
	@RequestMapping("/admin/sport/open/dropOTT.xhtml")
	public String dropOTT(Long ottid,ModelMap model) {
		OpenTimeTable ott = daoService.getObject(OpenTimeTable.class, ottid);
		ott.setStatus(OpenTimeTableConstant.STATUS_DISCARD);
		daoService.saveObject(ott);
		return showJsonSuccess(model);
	}

	//订单数据
	@RequestMapping("/admin/sport/open/orderList.xhtml")
	public String orderList(SearchOrderCommand soc, ModelMap model) {
		String url="admin/sport/open/orderList.vm";
		if(soc.getSportid()!=null) {
			Sport cursport = daoService.getObject(Sport.class, soc.getSportid());
			model.put("cursport", cursport);
		}
		if(soc.getOttid()!=null) {
			OpenTimeTable table = daoService.getObject(OpenTimeTable.class, soc.getOttid());
			Sport cursport = daoService.getObject(Sport.class, table.getSportid());
			model.put("cursport", cursport);
		}
		List<Sport> sportList =  sportService.getBookingEqOpenSport(null, Sport.BOOKING_OPEN);
		List<SportItem> sportItemList = sportService.getSportItemBySportId(null);
		List<SportOrder> orderList = sportOrderService.getSportOrderList(soc);
		List<Long> ottidList = BeanUtil.getBeanPropertyList(orderList, Long.class, "ottid", true);
		Map<Long, OpenTimeTable> tableMap = daoService.getObjectMap(OpenTimeTable.class, ottidList);
		model.put("tableMap", tableMap);
		model.put("sportList", sportList);
		model.put("sportItemList", sportItemList);
		model.put("orderList", orderList);
		model.put("ordertype", soc.getOrdertype());
		return url;
	}
	
	@RequestMapping("/admin/sport/open/getItemListBySportId.xhtml")
	public String getItemListBySportid(Long sportid,ModelMap model) {
		List<SportItem> sportItemList = sportService.getSportItemBySportId(sportid);
		Map result = new HashMap();
		result.put("sportItemList", sportItemList);
		return showJsonSuccess(model, result);
	}

	//保存场地价格
	@RequestMapping("/admin/sport/open/saveOtiPrice.xhtml")
	public String saveOtiPrice(Long id, Integer price, Integer costprice, Integer norprice, Long settleid, Integer upsetprice, ModelMap model) {
		OpenTimeItem item = daoService.getObject(OpenTimeItem.class, id);
		if(isValidItem(item)){
			item.setPrice(price);
			item.setCostprice(costprice);
			item.setNorprice(norprice);
			item.setUpsetprice(upsetprice);
			if(settleid != null) item.setSettleid(settleid);
			daoService.saveObject(item);
		}else {
			return showJsonError(model, "该场地已经被占用，不能修改");
		}
		
		return showJsonSuccess(model);
	}
	private boolean isValidItem(OpenTimeItem item){
		boolean isValid = false;
		if(StringUtils.equals(item.getStatus(), OpenTimeItemConstant.STATUS_DELETE)) return true;
		if(!item.hasField()) return true;
		if(OpenTimeItemConstant.STATUS_LOCKR.equals(item.getStatus())) { 
			isValid = true;
		}else {
			if(item.hasAvailable() || OpenTimeItemConstant.STATUS_LOCKL.equals(item.getStatus()) ||  OpenTimeItemConstant.STATUS_LOCKLF.equals(item.getStatus())) isValid =true;
		}
		return isValid;
	}
	//批量保存
	@RequestMapping("/admin/sport/open/saveBatchOtiPrice.xhtml")
	public String saveOtiPrice(String hours, String fields,Long id, Integer price, Integer costprice, Long settleid, Integer upsetprice, ModelMap model) {
		if(price == null && costprice == null && upsetprice == null && settleid == null) return showJsonError(model, "请设置价格！");
		OpenTimeTable table = daoService.getObject(OpenTimeTable.class, id);
		if(table.isBooking()) return showJsonError(model, "请先关闭场次再设置价格！");
		List<OpenTimeItem> itemList = openTimeTableService.getOpenItemList(table.getId());
		int count = 0;
		for(OpenTimeItem item : itemList){
			if(isValidItem(item)){
				if(StringUtils.isNotBlank(hours) && StringUtils.isNotBlank(fields)){
					String fieldid = item.getFieldid().toString();
					if(!hours.contains(item.getHour()) || !fields.contains(fieldid)){
						continue;
					}
				}else if(StringUtils.isNotBlank(hours) && StringUtils.isBlank(fields)){
					if(!hours.contains(item.getHour())){
						continue;
					}
				}else if(StringUtils.isBlank(hours) && StringUtils.isNotBlank(fields)){
					String fieldid = item.getFieldid().toString();
					if(!fields.contains(fieldid)){
						continue;
					}
				}
				count ++;
				if(price != null && price > 0) item.setPrice(price);
				if(costprice != null && costprice > 0) item.setCostprice(costprice);
				if(settleid != null) item.setSettleid(settleid);
				if(upsetprice != null) item.setUpsetprice(upsetprice);
				daoService.saveObject(item);
			}
		}
		return showJsonSuccess(model, "有"  + itemList.size() + "个记录，本次更新" + count +"个");
	}
	//保存场地价格
	@RequestMapping("/admin/sport/open/synchTime.xhtml")
	public String saveOtiPrice(Long sportid, ModelMap model) {
		Sport sport = daoService.getObject(Sport.class, sportid);
		model.put("cursport", sport);
		return "admin/sport/open/synchList.vm";
	}
	//保存场地价格
	@RequestMapping("/admin/sport/open/changeStatus.xhtml")
	public String changeStatus(Long id, String status, ModelMap model) {
		OpenTimeItem oti = daoService.getObject(OpenTimeItem.class, id);
		if(StringUtils.equals(oti.getStatus(), status)) return showJsonError(model, "repeat");
		boolean flag = false;
		if(StringUtils.equals(status, OpenTimeItemConstant.STATUS_NEW)){ //解锁
			if(StringUtils.equals(oti.getStatus(), OpenTimeItemConstant.STATUS_LOCKL)) flag = true;
		}else if(StringUtils.equals(status, OpenTimeItemConstant.STATUS_LOCKL)){ 
			if(StringUtils.equals(oti.getStatus(), OpenTimeItemConstant.STATUS_LOCKLF) || oti.hasAvailable()) flag = true;
		}else if(StringUtils.equals(status, OpenTimeItemConstant.STATUS_LOCKLF)){ 
			if(StringUtils.equals(oti.getStatus(), OpenTimeItemConstant.STATUS_LOCKL) || StringUtils.equals(oti.getStatus(), OpenTimeItemConstant.STATUS_LOCKR) || oti.hasAvailable()) flag = true;
		}else if(StringUtils.equals(oti.getStatus(), OpenTimeItemConstant.STATUS_NEW)) {
			if(status.equals(OpenTimeItemConstant.STATUS_LOCKL) || status.equals(OpenTimeItemConstant.STATUS_LOCKLF)){ //锁定
				if(oti.hasAvailable()) flag = true;
			}
		}else if(StringUtils.equals(oti.getStatus(), OpenTimeItemConstant.STATUS_LOCKR)){
			if(StringUtils.equals(status, OpenTimeItemConstant.STATUS_LOCKLF)) flag = true;
		}
		if(flag) {
			oti.setStatus(status);
			daoService.saveObject(oti);
			return showJsonSuccess(model);
		}
		return showJsonError(model, "该状态的不能进行操作！");
	}
	//批量锁
	@RequestMapping("/admin/sport/open/changeAllStatus.xhtml")
	public String changeAllStatus(Long ottid, String fields, String hours, String status, ModelMap model) {
		String[] hour = StringUtils.split(hours, ",");
		List<String> hourList = Arrays.asList(hour);
		List<Long> fieldIdList = BeanUtil.getIdList(fields, ",");
		OpenTimeTable ott = daoService.getObject(OpenTimeTable.class, ottid);
		if(ott == null) return showJsonError(model, "关联场次不存在！");
		List<OpenTimeItem> itemList = openTimeTableService.getOpenItemList(ott.getId());
		TimeItemHelper itemHelper = new TimeItemHelper(itemList);
		Map<String, OpenTimeItem> otiMap = itemHelper.getOtiMap();
		List<OpenTimeItem> otiList = new ArrayList<OpenTimeItem>();
		for(OpenTimeItem oti : otiMap.values()){
			if(fieldIdList.isEmpty() && hourList.isEmpty()){
				changeStatus(oti.getId(), status, model);
			}else if(hourList.contains(oti.getHour())&& !hourList.isEmpty()&& fieldIdList.isEmpty()){
				changeStatus(oti.getId(), status, model);
			}else if(fieldIdList.contains(oti.getFieldid())&& !fieldIdList.isEmpty() && hourList.isEmpty()){
				changeStatus(oti.getId(), status, model);
			}else if(fieldIdList.contains(oti.getFieldid()) && hourList.contains(oti.getHour())){
				changeStatus(oti.getId(), status, model);
			}
		}
		daoService.saveObjectList(otiList);
		return showJsonSuccess(model);
	}
	//保存场优惠券
	@RequestMapping("/admin/sport/open/setElecard.xhtml")
	public String setElecard(Long ottid, String elecard, ModelMap model) {
		OpenTimeTable table = daoService.getObject(OpenTimeTable.class, ottid);
		table.setElecard(elecard);
		daoService.saveObject(table);
		return showJsonSuccess(model);
	}
	//批量保存场优惠券
	@RequestMapping("/admin/sport/open/setAllElecard.xhtml")
	public String setAllElecard(String ids, String elecard, ModelMap model) {
		if(StringUtils.isBlank(ids)) return showJsonError(model, "该场馆没有场次！");
		String elecardType = "ABCDM";
		char[] chars = elecard.toCharArray();
		for(char c : chars){
			if(!StringUtils.contains(elecardType, c)){
				return showJsonError(model, "优惠券类型错误！");
			}
		}
		List<Long> idList = BeanUtil.getIdList(ids, ",");
		List<OpenTimeTable> ottList = daoService.getObjectList(OpenTimeTable.class, idList);
		for(OpenTimeTable ott : ottList){
			ott.setElecard(elecard);
		}
		daoService.saveObjectList(ottList);
		return showJsonSuccess(model);
	}
	//场次详细
	@RequestMapping("/admin/sport/open/ottForm.xhtml")
	public String ottDetail(Long ottid, ModelMap model) {
		OpenTimeTable table = daoService.getObject(OpenTimeTable.class, ottid);
		Sport cursport = daoService.getObject(Sport.class, table.getSportid());
		List<PayBank> bankList = paymentService.getPayBankList(PayBank.TYPE_PC);
		model.put("table", table);
		model.put("cursport", cursport);
		model.put("otherinfo", table.getOtherinfo());
		model.put("confPayList", bankList);
		model.put("payTextMap", PaymethodConstant.getPayTextMap());
		return "admin/sport/open/ottForm.vm";
	}

	//保存场次
	@RequestMapping("/admin/sport/open/saveOtt.xhtml")
	public String saveOtt(Long ottid, String payoption, String paymethodlist, String defaultpaymethod, 
			String cardoption, String batchidlist, String address, String unopengewa, HttpServletRequest request, ModelMap model) {
		OpenTimeTable ott = daoService.getObject(OpenTimeTable.class, ottid);
		BindUtils.bindData(ott, request.getParameterMap());
		daoService.saveObject(ott);
		ott = daoService.getObject(OpenTimeTable.class, ottid);
		if(StringUtils.equals(paymethodlist, ",")) paymethodlist = "";
		Map<String, String> otherinfo = VmUtils.readJsonToMap(ott.getOtherinfo());
		if(StringUtils.equals(payoption, "del")) {
			otherinfo.remove(OpiConstant.PAYOPTION);
			otherinfo.remove(OpiConstant.PAYCMETHODLIST);
			otherinfo.remove(OpiConstant.DEFAULTPAYMETHOD);
		}else if(StringUtils.isNotBlank(payoption)){
			otherinfo.put(OpiConstant.PAYOPTION, payoption);
			if(StringUtils.isNotBlank(paymethodlist)) { 
				if(StringUtils.isBlank(defaultpaymethod)) return showJsonError(model, "请选择默认支付方式");
				otherinfo.put(OpiConstant.DEFAULTPAYMETHOD, defaultpaymethod);
				otherinfo.put(OpiConstant.PAYCMETHODLIST, paymethodlist);
			}else {
				otherinfo.remove(OpiConstant.DEFAULTPAYMETHOD);
				otherinfo.remove(OpiConstant.PAYCMETHODLIST);
			}
			if(StringUtils.equals(payoption, "notuse") && StringUtils.isBlank(paymethodlist)){
				return showJsonError(model, "支付方式选择不可用，必须勾选支付方式！");
			}
		}
		if(StringUtils.equals(cardoption, "del")) {
			otherinfo.remove(OpiConstant.CARDOPTION);
			otherinfo.remove(OpiConstant.BATCHIDLIST);
		}else if(StringUtils.isNotBlank(cardoption) && StringUtils.isNotBlank(batchidlist)){
			String[] batchidList = StringUtils.split(batchidlist, ",");
			for(String batchid : batchidList){
				ElecCardBatch batch = daoService.getObject(ElecCardBatch.class, new Long(batchid));
				if(batch==null) return showJsonError(model, batchid+"对应的批次不存在！");
			}
			otherinfo.put(OpiConstant.CARDOPTION, cardoption);
			otherinfo.put(OpiConstant.BATCHIDLIST, batchidlist);
		}
		if(StringUtils.isBlank(address)){
			otherinfo.remove(OpiConstant.ADDRESS);
		}else{
			otherinfo.put(OpiConstant.ADDRESS, address);
		}
		if(StringUtils.isBlank(unopengewa)){
			otherinfo.remove(OpiConstant.UNOPENGEWA);
		}else{
			otherinfo.put(OpiConstant.UNOPENGEWA, unopengewa);
		}
		ott.setOtherinfo(JsonUtils.writeMapToJson(otherinfo));
		daoService.saveObject(ott);
		return showJsonSuccess(model);
	}
	
	//场馆确认订单
	@RequestMapping("/admin/sport/open/sportConfirm.xhtml")
	public String sportConfirm(Long orderid, ModelMap model) {
		SportOrder order = daoService.getObject(SportOrder.class, orderid);
		if(!StringUtils.equals(order.getStatus(), OrderConstant.STATUS_PAID_SUCCESS)) return showJsonError(model, "非成功的订单，不能确认！");
		List<SMSRecord> smsList= messageService.addSportOrderMessage(order).getRetval();//发送信息
		if(!CollectionUtils.isEmpty(smsList)){
			for (SMSRecord sms : smsList) {
				if(sms!=null) untransService.sendMsgAtServer(sms, false);
			}
		}
		return showJsonSuccess(model);
	}
	@RequestMapping("/admin/sport/open/failConfirm.xhtml")
	public String reConfirm(String tradeNo, String validtime, ModelMap model){
		SportOrder sorder = daoService.getObjectByUkey(SportOrder.class, "tradeNo", tradeNo, false);
		if(!StringUtils.startsWith(sorder.getStatus(), OrderConstant.STATUS_PAID_FAILURE)) return showJsonError(model, "非待处理的订单，不能确认！");
		if(StringUtils.isBlank(validtime)) validtime = "Y";
		if(StringUtils.equals(validtime, "Y")){
			Timestamp curtime = new Timestamp(System.currentTimeMillis());
			OpenTimeTable ott = daoService.getObjectByUkey(OpenTimeTable.class, "id", sorder.getMpid(), true);
			Timestamp playtime = null;
			if(ott.hasField()){
				Map<String, String> descMap = JsonUtils.readJsonToMap(sorder.getDescription2());
				playtime = Timestamp.valueOf(descMap.get("时间"));
			}else{
				SellTimeTable stt = daoService.getObject(SellTimeTable.class, sorder.getId());
				playtime = ott.getPlayTimeByHour(stt.getStarttime());
			}
			if(playtime.before(curtime)){
				return showJsonSuccess(model, "时间已过期");
			}
		}
		ErrorCode result = orderProcessService.processOrder(sorder, "重新确认", null);
		return showJsonSuccess(model, result.getMsg());
	}
	@RequestMapping("/admin/sport/open/copyPrice.xhtml")
	public String copyPrice(Long ottid, Date playdate, ModelMap model) {
		OpenTimeTable newTable = daoService.getObject(OpenTimeTable.class, ottid);
		if(playdate == null) playdate = DateUtil.addDay(newTable.getPlaydate(), -7);
		String qry = "from OpenTimeTable t where t.sportid=? and t.itemid=? and t.playdate=? and t.openType=? order by t.id desc";
		List<OpenTimeTable> oldOttList = hibernateTemplate.find(qry, newTable.getSportid(), newTable.getItemid(), playdate, newTable.getOpenType());
		if(oldOttList.size()>0){
			OpenTimeTable old = oldOttList.get(0);
			int count = 0;
			List<OpenTimeItem> newotiList = openTimeTableService.getOpenItemList(newTable.getId());
			int sum = newotiList.size();
			for(OpenTimeItem newOti : newotiList){
				if(newOti.getCostprice()*newOti.getPrice()*newOti.getNorprice()*newOti.getUpsetprice()==0 || !newTable.isBooking()){
					if(isValidItem(newOti)){
						qry = "from OpenTimeItem o where o.ottid=? and o.fieldid=? and o.hour=?";
						List<OpenTimeItem> oldOtiList = hibernateTemplate.find(qry, old.getId(), newOti.getFieldid(), newOti.getHour());
						if(oldOtiList.size()>0){
							OpenTimeItem oldOti = oldOtiList.get(0);
							if(newOti.getCostprice()==0 || !newTable.isBooking()) newOti.setCostprice(oldOti.getCostprice());
							if(newOti.getNorprice()==0  || !newTable.isBooking()) newOti.setNorprice(oldOti.getNorprice());
							if(newOti.getPrice()==0  || !newTable.isBooking()) newOti.setPrice(oldOti.getPrice());
							if(newOti.getSettleid()==null) newOti.setSettleid(oldOti.getSettleid());
							if(newOti.getUpsetprice()==0  || !newTable.isBooking()) newOti.setUpsetprice(oldOti.getUpsetprice());
							daoService.saveObject(newOti);
							count++;
						}
					}
				}
			}
			return showJsonSuccess(model, "有"  + sum + "个记录，本次更新" + count +"个");
		}else {
			return showJsonError(model, "记录不存在！");
		}
	}
	//----------------------------**************************************-------------------
	//同步场地
	@RequestMapping("/admin/sport/open/synchField.xhtml")
	public String synchField(Long sportid, Long itemid, ModelMap model) {
		ErrorCode<List<GstSportField>> code = remoteSportService.getGstSportFieldList(sportid, itemid);
		if(!code.isSuccess()) return showJsonError(model, code.getMsg());
		apiSportService.addSportField(code.getRetval());
		return showJsonSuccess(model);
	}
	//同步项目
	@RequestMapping("/admin/sport/open/synchItem.xhtml")
	public String synchItem(Long sportid, ModelMap model) {
		ErrorCode<String> code = remoteSportService.getGstItemIdList(sportid);
		if(!code.isSuccess()) return showJsonError(model, code.getMsg());
		String[] ids = StringUtils.split(code.getRetval(), ",");
		if(ids != null){
			for (String id : ids) {
				if(ValidateUtil.isNumber(id)){
					Sport2Item sport2Item = sportService.getSport2Item(sportid, Long.parseLong(id));
					if(sport2Item == null){//保存场馆项目
						sport2Item = new Sport2Item(sportid, Long.parseLong(id));
						daoService.saveObject(sport2Item);
					}
				}
			}
		}
		return showJsonSuccess(model);
	}
	
	@RequestMapping("/admin/sport/open/saveOrUpdateField.xhtml")
	public String saveOrUpdate(Long id, Long sportid, Long itemid, String name, ModelMap model){
		SportField sportField = new SportField();
		if(id != null){
			sportField = daoService.getObject(SportField.class, id);
			if(sportField == null) return showJsonError(model, "该数据不存在或被删除！");
		}else{
			sportField = new SportField(sportid, itemid, name);
		}
		ChangeEntry changeEntry = new ChangeEntry(sportField);
		sportField.setName(name);
		SportField oldSportField = apiSportService.getSportField(sportid, itemid, name);
		if(oldSportField != null && !oldSportField.getId().equals(sportField.getId())) return showJsonError(model, "该名称的数据已存在！");
		daoService.saveObject(sportField);
		monitorService.saveChangeLog(getLogonUser().getId(), SportField.class, sportField.getId(),changeEntry.getChangeMap(sportField));
		return showJsonSuccess(model);
	}
	@RequestMapping("/admin/sport/open/getSportField.xhtml")
	public String getSportField(Long id, ModelMap model){
		SportField sportField = daoService.getObject(SportField.class, id);
		if(sportField == null) return showJsonError(model, "该数据不存在或被删除！");
		return showJsonSuccess(model, BeanUtil.getBeanMap(sportField));
	}
	@RequestMapping("/admin/sport/open/setFieldOrdernum.xhtml")
	public String getFieldOrdernum(Long id, Integer ordernum, ModelMap model){
		SportField sportField = daoService.getObject(SportField.class, id);
		if(sportField == null) return showJsonError(model, "该数据不存在或被删除！");
		if(ordernum == null) return showJsonError(model, "排序字段不能为空！");
		ChangeEntry changeEntry = new ChangeEntry(sportField);
		sportField.setOrdernum(ordernum);
		daoService.saveObject(sportField);
		monitorService.saveChangeLog(getLogonUser().getId(), SportField.class, sportField.getId(),changeEntry.getChangeMap(sportField));
		return showJsonSuccess(model, BeanUtil.getBeanMap(sportField));
	}
	
	//同步场次
	@RequestMapping("/admin/sport/open/synchOttList.xhtml")
	public String synchOtt(Long sportid, Long itemid, ModelMap model) {
		String result = forceSynch(sportid, itemid, null, "ge");
		if(StringUtils.contains(result, "error")){
			return showJsonError(model, result);
		}
		return showJsonSuccess(model);
	}
	
	//同步场次
	@RequestMapping("/admin/sport/open/synchSingle.xhtml")
	public String synchSingle(Long id, ModelMap model) {
		OpenTimeTable ott = daoService.getObject(OpenTimeTable.class, id);
		if(ott.isBooking()) return showJsonError(model, "该场次已开放，请先关闭再同步！");
		String result = forceSynch(ott.getSportid(), ott.getItemid(), ott.getPlaydate(), null);
		return showJsonSuccess(model, result);
	}
	private String forceSynch(Long sportid, Long itemid, Date playdate, String ge){
		ErrorCode<List<GstOtt>> code = remoteSportService.getGstOttList(sportid, itemid, playdate, ge);
		List<String> msgList = new ArrayList<String>();
		if(code.isSuccess()){
			List<GstOtt> ottList = code.getRetval();
			for(GstOtt ott : ottList){
				Sport2Item sport2Item = sportService.getSport2Item(ott.getSportid(), ott.getItemid());
				if(sport2Item != null && StringUtils.equals(sport2Item.getCreatetype(), Sport2Item.RANGE)){
					if(StringUtils.equals(itemid+"", ott.getItemid()+"")){
						dbLogger.warn("同步信息sportid="+sportid+", playdate="+DateUtil.formatDate(playdate)+", itemid="+itemid);
						ErrorCode<List<OpenTimeItem>> resultCode = apiSportService.saveSportTimeTable(ott);
						if(!resultCode.isSuccess()) return "error:"+resultCode.getMsg();
						msgList.add("sportid="+sportid + ", itemid=" + itemid + ", itemid=" + ott.getItemid() + ", playdate=" + DateUtil.formatDate(playdate));
						List<OpenTimeItem> itemList = resultCode.getRetval();
						Map<String,List<OpenTimeItem>> itemMap = BeanUtil.groupBeanList(itemList, "itemtype");
						List<OpenTimeItem> otiList = itemMap.get(OpenTimeTableConstant.ITEM_TYPE_VIE);
						if(!CollectionUtils.isEmpty(otiList)){
							Map<String,List<OpenTimeItem>> tmpMap = BeanUtil.groupBeanList(itemList, "saleInd");
							for (String bindInd : tmpMap.keySet()) {
								openTimeTableService.refreshOpenTimeSale(tmpMap.get(bindInd), 5, msgList);
							}
						}
					}
				}
			}
		}else{
			return "error:"+code.getMsg();
		}
		return StringUtils.join(msgList, ":");
	}
	
	//同步场次中已锁定的场次
	@RequestMapping("/admin/sport/open/synchLockOtiList.xhtml")
	public String synchOti(Long ottid, ModelMap model) {
		OpenTimeTable ott = daoService.getObject(OpenTimeTable.class, ottid);
		Map<String, String> params = new HashMap<String, String>();
		params.put("ottid", ott.getRemoteid()+"");
		params.put("checkvalue", getCheck(ottid));
		return showJsonSuccess(model);
	}
	private static String getCheck(Object...objects){
		String str = StringUtils.join(objects, "") + DateUtil.formatDate(new Date());
		return StringUtil.md5(str);
	}
	//设置特价活动
	@RequestMapping("/admin/sport/open/setOpiSpflag.xhtml")
	public String setOpiSpflag(Long id, String spflag, ModelMap model){
		OpenTimeTable ott = daoService.getObject(OpenTimeTable.class, id);
		if(ott==null) return showJsonError_NOT_FOUND(model);
		ott.setSpflag(spflag);
		daoService.saveObject(ott);
		return showJsonSuccess(model);
	}
	
	//同步结算
	@RequestMapping("/admin/sport/open/sportSettleList.xhtml")
	public String sportSettleList(Long sportid, Integer pageNo, String param, ModelMap model){
		Sport sport = daoService.getObject(Sport.class,sportid);
		if(sport == null) return show404(model, "该场馆不存在或被删除!");
		if(pageNo == null) pageNo = 0;
		int rowsPerPage = 1;
		int fisrtRowPer = 0;
		int count = 0;
		Map params = new HashMap();
		params.put("tag", TagConstant.TAG_SPORT);
		params.put("relatedid", sportid);
		if(StringUtils.isNotBlank(param)){
			rowsPerPage = 5;
			fisrtRowPer = pageNo*rowsPerPage;
			count = mongoService.getObjectCount(RelateToSettle.class, params);
			
			PageUtil pageUtil = new PageUtil(count, rowsPerPage, pageNo, "admin/sport/open/sportSettleList.xhtml");
			Map params1 = new HashMap();
			params1.put("sportid", sportid);
			params1.put("param", param);
			pageUtil.initPageInfo(params1);
			model.put("pageUtil", pageUtil);
		
		}
		List<RelateToSettle> settleList = mongoService.getObjectList(RelateToSettle.class, params, "updatetime", false, fisrtRowPer, rowsPerPage);
		model.put("settleList", settleList);
		model.put("sport", sport);
		return "admin/sport/open/sportSettleList.vm";
	}
	
	@RequestMapping("/admin/sport/open/getSportSettle.xhtml")
	public String getSettle(String id, ModelMap model){
		if(StringUtils.isBlank(id)) return showJsonError(model, "参数错误！");
		RelateToSettle settle = mongoService.getObject(RelateToSettle.class, MongoData.DEFAULT_ID_NAME, id);
		if(settle == null) return showJsonError(model, "该数据不存在或被删除！");
		Map result = BeanUtil.getBeanMap(settle);
		return showJsonSuccess(model, result);
	}
	@RequestMapping("/admin/sport/open/saveSportSettle.xhtml")
	public String saveOrUpdateSettle(String id, Long sid, String content, ModelMap model){
		User user = getLogonUser();
		RelateToSettle settle = null;
		if(StringUtils.isBlank(content)) return showJsonError(model, "内容不能为空！");
		Sport sport = daoService.getObject(Sport.class, sid);
		if(sport == null) return showJsonError(model, "参数错误！");
		if(StringUtils.isNotBlank(id)){
			settle = mongoService.getObject(RelateToSettle.class, MongoData.DEFAULT_ID_NAME, id);
			if(settle == null) return showJsonError(model, "该数据不存在或被删除！");
			if(!user.getId().equals(settle.getUserid())) return showJsonError(model, "不能修改他人信息！");
			settle.setContent(content);
			settle.setUpdatetime(DateUtil.formatTimestamp(System.currentTimeMillis()));
			settle.setUsername(user.getNickname());
		}else{
			settle = new RelateToSettle(TagConstant.TAG_SPORT, sid, user.getId(), content);
			settle.setId(ServiceHelper.assignID(TagConstant.TAG_SPORT));
			settle.setUsername(user.getNickname());
		}
		mongoService.saveOrUpdateObject(settle, MongoData.DEFAULT_ID_NAME);
		return showJsonSuccess(model);
	}
	
	@RequestMapping("/admin/sport/open/sumcost.xhtml")
	public String sumcost(ModelMap model){
		String qry = "from SportOrder o where o.status like ? order by o.addtime";
		List<SportOrder> orderList = hibernateTemplate.find(qry, OrderConstant.STATUS_PAID + "%");
		int i = 0;
		for(SportOrder order : orderList){
			int sumcost = order.getSumcost();
			order.setTotalcost(sumcost);
			daoService.saveObject(order);
			i++;
		}
		return forwardMessage(model, "更新" + i);
	}
	@RequestMapping("/cus/sportList.xhtml")
	public String sportList(Long sportid,String ids,String checkValue, ModelMap model){
		List<Map> sportList = null;
		if(StringUtils.isBlank(ids)){
			if(sportid==null) sportid = 0L;
			//if(!StringUtil.md5(sportid+DateUtil.formatDate(new Date())).equals(checkValue))return "admin/sport/open/sportList.vm";
			String qry = "select new map(s.id as id, s.name as name, s.briefname as briefname) from Sport s where s.id>=?";
			sportList = hibernateTemplate.find(qry, sportid);
		}else{
			if(!StringUtil.md5(ids+DateUtil.formatDate(new Date())).equals(checkValue))return "admin/sport/open/sportList.vm";
			List<Long> idList = BeanUtil.getIdList(ids, ",");
			if(idList.size() != 0){
				ids = StringUtils.join(idList.toArray(),",");
				String qry = "select new map(s.id as id, s.name as name, s.briefname as briefname) from Sport s where s.id in ("+ids+")";
				sportList = hibernateTemplate.find(qry);
			}
		}
		model.put("sportList", sportList);
		return "admin/sport/open/sportList.vm";
	}
	
	@RequestMapping("/admin/sport/open/programItemList.xhtml")
	public String programItemList(Long sportid, Long itemid, ModelMap  model){
		if(sportid == null || itemid == null) return show404(model, "参数错误！");
		Sport sport = daoService.getObject(Sport.class, sportid);
		SportItem sportItem = daoService.getObject(SportItem.class, itemid);
		model.put("cursport", sport);
		model.put("sportItem", sportItem);
		List<ProgramItemTime> programItemList = sportService.getProgramItemTimeList(sportid, itemid);
		model.put("programItemList", programItemList);	
		return "admin/sport/open/programItemList.vm";
	}
	
	@RequestMapping("/admin/sport/open/saveProgramItemTime.xhtml")
	public String saveProgramItemTime(Long id, Long sportid, Long itemid, Integer week, String fieldids, HttpServletRequest request, ModelMap model){
		String citycode = getAdminCitycode(request);
		User user = getLogonUser();
		Map dataMap = request.getParameterMap();
		ErrorCode<ProgramItemTime> code = openTimeTableService.saveOrUpdateProgramItem(id, sportid, itemid, week, fieldids, dataMap, user, citycode);
		if(!code.isSuccess()) return showJsonError(model, code.getMsg());
		return showJsonSuccess(model);
	}
	
	
	@RequestMapping("/admin/sport/open/getProgramItemTime.xhtml")
	public String getProgramItemTime(Long id, Long sportid, Long itemid, ModelMap model){
		ProgramItemTime programItemTime = daoService.getObject(ProgramItemTime.class, id);
		model.put("programItemTime", programItemTime);
		List<SportField> fieldList = sportOrderService.getSportFieldList(sportid, itemid);
		model.put("fieldList", fieldList);
		Sport sport = daoService.getObject(Sport.class, sportid);
		SportItem sportItem = daoService.getObject(SportItem.class, itemid);
		model.put("cursport", sport);
		model.put("sportItem", sportItem);
		return "admin/sport/open/programItemDefault.vm";
	}
	
	@RequestMapping("/admin/sport/open/delProgramItemTime.xhtml")
	public String deleteProgramItem(Long id, ModelMap model){
		ProgramItemTime programItemTime = daoService.getObject(ProgramItemTime.class, id);
		if(programItemTime == null) return showJsonError(model, "该场馆的时间段数据不存在！");
		daoService.removeObject(programItemTime);
		monitorService.saveDelLog(getLogonUser().getId(), id, programItemTime);
		return showJsonSuccess(model);
	}
	
	@RequestMapping("/admin/sport/open/modifyOttPrice.xhtml")
	public String modifyOttPrice(Long ottid, Integer price, Integer costprice, Integer sportprice, ModelMap model){
		OpenTimeTable ott = daoService.getObject(OpenTimeTable.class, ottid);
		if(ott == null) return showJsonError(model, "该场次不存在或被删除！");
		if(price == null || price <=0) return showJsonError(model, "价格错误！");
		if(costprice == null) return showJsonError(model, "成本价不能为空！");
		if(sportprice == null) return showJsonError(model, "场馆价不能为空！");
		ChangeEntry changeEntry = new ChangeEntry(ott);
		ott.setPrice(price);
		ott.setCostprice(costprice);
		ott.setSportprice(sportprice);
		daoService.saveObject(ott);
		monitorService.saveChangeLog(getLogonUser().getId(), OpenTimeTable.class, ottid, changeEntry.getChangeMap(ott));
		return showJsonSuccess(model);
	}
	@RequestMapping("/admin/sport/open/saveNewProgramItemTime.xhtml")
	public String saveNewProgramItemTime(Long id, Long sportid, Long itemid, String week, String fieldids, HttpServletRequest request, ModelMap model){
		String citycode = getAdminCitycode(request);
		Sport2Item sport2Item = sportService.getSport2Item(sportid, itemid);
		if(StringUtils.equals(sport2Item.getCreatetype(), Sport2Item.GEWA)){
			User user = getLogonUser();
			Map dataMap = request.getParameterMap();
			String[] weeks = StringUtils.split(week, ",");
			if(weeks != null){
				for(int i=0; i<weeks.length; i++){
					ErrorCode<ProgramItemTime> code = openTimeTableService.saveOrUpdateProgramItem(id, sportid, itemid, Integer.parseInt(weeks[i]), fieldids, dataMap, user, citycode);
					if(!code.isSuccess()) return showJsonError(model, code.getMsg());
				}
			}
			return showJsonSuccess(model);
		}
		return showJsonError(model, "场次生成类型为远程同步，本地不能生成场次！");
	}
	//新版基础数据
	@RequestMapping("/admin/sport/open/newBaseData.xhtml")
	public String newBaseData(Long sportid, Long itemid, ModelMap model) {
		Sport sport = daoService.getObject(Sport.class, sportid);
		if(sport == null) return alertMessage(model, "场馆不存在!");
		SportProfile profile = daoService.getObject(SportProfile.class, sportid);
		SportItem curitem = daoService.getObject(SportItem.class, itemid);
		Map<Long, List<SportField>> fieldMapList = new HashMap<Long, List<SportField>>();
		List<SportItem> itemList = sportService.getSportItemListBySportId(sport.getId(), null);
		if(curitem == null && !itemList.isEmpty()){
			curitem = daoService.getObject(SportItem.class, itemList.get(0).getId());
		}
		if(curitem != null){
			model.put("curitem", curitem);
			Sport2Item sport2Item = sportService.getSport2Item(sport.getId(), curitem.getId());
			model.put("sport2Item", sport2Item);
			fieldMapList.put(curitem.getId(), sportOrderService.getSportFieldList(sport.getId(), curitem.getId()));
		}
		model.put("cursport", sport);
		model.put("profile", profile);
		model.put("itemList", itemList);
		model.put("fieldMapList", fieldMapList);
		return "admin/sport/open/new_baseData.vm";
	}

	//新场次管理
	@RequestMapping("/admin/sport/open/newOttList.xhtml")
	public String newOpenTimeTableList(Long sportid, Long itemid, Date qryDate, ModelMap model) {
		Date datefrom = null;
		Date dateto = null;
		if(qryDate!=null) {
			datefrom = qryDate;
			dateto = DateUtil.getLastTimeOfDay(datefrom);
		}else {
			datefrom = DateUtil.getBeginningTimeOfDay(new Date());
		}
		Sport sport = null;
		if(sportid!=null) {
			sport = daoService.getObject(Sport.class, sportid);
		}else {
			return showError(model, "运动场馆不存在！");
		}
		Map<Long ,SportItem> itemMap = new HashMap<Long, SportItem>();
		List<OpenTimeTable> ottList = sportOrderService.getOttList(sportid, itemid, datefrom, dateto, false);
		List<SportItem> itemList = sportOrderService.getOpenSportItemList(sportid, datefrom, dateto, false);
		Map<Long, Integer> countMap = new HashMap<Long, Integer>();
		int count = 0;
		int sum = 0;
		for(SportItem item : itemList){
			if(!itemMap.containsKey(item.getId())) itemMap.put(item.getId(), item);
			count = sportOrderService.getOttCount(sportid, item.getId(), datefrom, dateto, false);
			sum = sum + count;
			countMap.put(item.getId(), count);
		}
		model.put("sum", sum);
		model.put("cursport", sport);
		model.put("ottList", ottList);
		model.put("itemList", itemList);
		model.put("itemMap", itemMap);
		model.put("countMap", countMap);
		return "admin/sport/open/new_ottList.vm";
	}
	
	//新开放场次详细
	@RequestMapping("/admin/sport/open/newOttDetail.xhtml")
	public String newOpenTimeTableDetail(Long ottid, ModelMap model,SearchOrderCommand soc) {
		OpenTimeTable ott = daoService.getObject(OpenTimeTable.class, ottid);
		Long sportid = ott.getSportid();
		Long itemid = ott.getItemid();
		Sport sport = daoService.getObject(Sport.class, sportid);
		SportItem item = daoService.getObject(SportItem.class, itemid);
		soc.setOttid(ottid);
		List<OpenTimeItem> otiList = openTimeTableService.getOpenItemList(ott.getId());
		model.put("ott", ott);
		model.put("item", item);
		model.put("sport", sport);
		model.put("itemHelper", new TimeItemHelper(otiList));
		model.put("ordertype", soc.getOrdertype());
		model.put("ottid", soc.getOttid());
		List<SportField> fieldList = sportOrderService.getAllSportFieldList(ottid);
		List<SportSettle> settleList = settleService.getSportSettleList(sportid, itemid);
		model.put("settleList", settleList);
		model.put("fieldList", fieldList);
		if(ott.hasField()){
			List<String> playHourList = sportOrderService.getPlayHourList(ottid, null);
			model.put("playHourList", playHourList);
			model.put("checkValue", StringUtil.md5(""+ott.getSportid()+ott.getItemid()+ott.getRemoteid()));
			List<OpenTimeSale> saleList = daoService.getObjectListByField(OpenTimeSale.class, "ottid", ott.getId());
			model.put("saleList", saleList);
		}else if(ott.hasPeriod() || ott.hasInning()){
			List<OpenTimeItem> itemList = openTimeTableService.getOpenItemList(ottid);
			model.put("itemList", itemList);
			return "admin/sport/open/new_ottPeriodDetail.vm";
		}
		return "admin/sport/open/new_ottDetail.vm";
	}

	@RequestMapping("/admin/sport/open/refreshOpenTimeSale.xhtml")
	public String refreshOpenTimeSale(Long ottid, ModelMap model){
		OpenTimeTable ott = daoService.getObject(OpenTimeTable.class, ottid);
		if(ott == null) return forwardMessage(model, "场次数据不存在！");
		if(!ott.hasField()) return forwardMessage(model, "非场地场次，没有竞拍数据！");
		List<OpenTimeItem> itemList = daoService.getObjectListByField(OpenTimeItem.class, "ottid", ott.getId());
		Map<String, List<OpenTimeItem>> itemMap = BeanUtil.groupBeanList(itemList, "itemtype");
		List<OpenTimeItem>	odiList = itemMap.get(OpenTimeTableConstant.ITEM_TYPE_VIE);
		if(CollectionUtils.isEmpty(odiList)) return forwardMessage(model, "该场次没有竞价场次！");
		Map<String, List<OpenTimeItem>> tmpMap = BeanUtil.groupBeanList(odiList, "saleInd");
		List<String> msgList = new ArrayList<String>();
		for (String bindInd : tmpMap.keySet()) {
			openTimeTableService.refreshOpenTimeSale(tmpMap.get(bindInd), 5, msgList);
		}
		if(msgList.isEmpty()){
			msgList.add("没有更新数据！");
		}
		return forwardMessage(model, msgList);
	}
	
	@RequestMapping("/admin/sport/open/setOtsByGuaranteeid.xhtml")
	public String setOtsByGuaranteeid(Long id, Long guaranteeid, ModelMap model){
		OpenTimeSale openTimeSale = daoService.getObject(OpenTimeSale.class, id);
		ChangeEntry changeEntry = new ChangeEntry(openTimeSale);
		openTimeSale.setGuaranteeid(guaranteeid);
		daoService.saveObject(openTimeSale);
		monitorService.saveChangeLog(getLogonUser().getId(), OpenTimeSale.class, openTimeSale.getId(), changeEntry.getChangeMap(openTimeSale));
		return showJsonSuccess(model);
	}
	
	//修改开始时间
	@RequestMapping("/admin/sport/open/saveOtiHour.xhtml")
	public String saveOtiHour(Long id, String hour, ModelMap model){
		if(id == null || StringUtils.isBlank(hour)) return showJsonError(model, "参数错误！");
		OpenTimeItem item = daoService.getObject(OpenTimeItem.class, id);
		OpenTimeTable ott = daoService.getObject(OpenTimeTable.class, item.getOttid());
		if(ott.isBooking()) return showJsonError(model, "该场次已开放，不能修改");
		if(ott.hasField())return showJsonError(model, "该场次开放类型，不能修改");
		String endhour = item.getEndhour();
		if(hour.compareTo(endhour)>=0) return showJsonError(model, "开始时间不能大于等于结束时间！");
		item.setHour(hour);
		daoService.saveObject(item);
		return showJsonSuccess(model);
	}
	//修改结束时间
	@RequestMapping("/admin/sport/open/saveOtiEndHour.xhtml")
	public String saveOtiEndHour(Long id,String endhour, ModelMap model){
		if(id == null || StringUtils.isBlank(endhour)) return showJsonError(model, "参数错误！");
		OpenTimeItem item = daoService.getObject(OpenTimeItem.class, id);
		OpenTimeTable ott = daoService.getObject(OpenTimeTable.class, item.getOttid());
		if(ott.hasField()) return showJsonError(model, "该场次开放类型，不能修改");
		if(ott.isBooking()) return showJsonError(model, "该场次已开放，不能修改");
		String hour = item.getHour();
		if(hour.compareTo(endhour)>=0) return showJsonError(model, "结束时间段不能小于开始时间！");
		Timestamp tmpValidtime = ott.getPlayTimeByHour(endhour);
		if(item.hasUnitWhote()){
			item.setValidtime(DateUtil.addMinute(tmpValidtime, -30));
		}else if(item.hasUnitTime()){
			item.setValidtime(DateUtil.addMinute(tmpValidtime, -item.getUnitMinute()));
		}
		item.setEndhour(endhour);
		daoService.saveObject(item);
		return showJsonSuccess(model);
	}
	//保存人次，局数价格
	@RequestMapping("/admin/sport/open/savePeriodOtiPrice.xhtml")
	public String savePeriodOtiPrice(Long id, Integer price, Integer costprice, Integer norprice, Long settleid, Integer upsetprice, ModelMap model) {
		OpenTimeItem item = daoService.getObject(OpenTimeItem.class, id);
		OpenTimeTable ott = daoService.getObject(OpenTimeTable.class, item.getOttid());
		if(ott.isBooking()) return showJsonError(model, "该场次已开放，不能修改");
		item.setPrice(price);
		item.setCostprice(costprice);
		item.setNorprice(norprice);
		item.setUpsetprice(upsetprice);
		if(settleid != null) item.setSettleid(settleid);
		daoService.saveObject(item);
		return showJsonSuccess(model);
	}

	//批量添加场地
	@RequestMapping("/admin/sport/open/batchSaveField.xhtml")
	public String batchSaveField(Long id, Long sportid, Long itemid, String name, Integer num, Integer initialnum, String status, ModelMap model){
		if(id != null){
			SportField sportField = daoService.getObject(SportField.class, id);
			if(sportField == null) return showJsonError(model, "该数据不存在或被删除！");
			ChangeEntry changeEntry = new ChangeEntry(sportField);
			sportField.setName(name);
			SportField oldSportField = apiSportService.getSportField(sportid, itemid, name);
			if(oldSportField != null && !oldSportField.getId().equals(sportField.getId())) return showJsonError(model, "该名称的数据已存在！");
			daoService.saveObject(sportField);
			monitorService.saveChangeLog(getLogonUser().getId(), SportField.class, sportField.getId(),changeEntry.getChangeMap(sportField));
		}else{
			if(num == null && initialnum == null){
				num = 1;
				initialnum = 0;
			}
			for(int i=initialnum;i<(num+initialnum);i++){
				String newName = StringUtils.replace(name, "#", ""+i);
				SportField sportField = new SportField(sportid, itemid, newName);
				ChangeEntry changeEntry = new ChangeEntry(sportField);
				SportField oldSportField = apiSportService.getSportField(sportid, itemid, newName);
				if(oldSportField != null && !oldSportField.getId().equals(sportField.getId())) return showJsonError(model, "该名称的数据已存在！");
				sportField.setStatus(status);
				daoService.saveObject(sportField);
				monitorService.saveChangeLog(getLogonUser().getId(), SportField.class, sportField.getId(),changeEntry.getChangeMap(sportField));
				
			}
		}
		return showJsonSuccess(model);
	}
	//新版时间段管理
	@RequestMapping("/admin/sport/open/newProgramItemList.xhtml")
	public String newProgramItemList(Long sportid, Long itemid, ModelMap  model){
		if(sportid == null || itemid == null) return show404(model, "参数错误！");
		Sport sport = daoService.getObject(Sport.class, sportid);
		SportItem sportItem = daoService.getObject(SportItem.class, itemid);
		List<SportField> fieldList = sportOrderService.getSportFieldList(sportid, itemid);
		List<ProgramItemTime> programItemList = sportService.getProgramItemTimeList(sportid, itemid);
		Sport2Item sport2Item = sportService.getSport2Item(sportid,itemid);
		model.put("cursport", sport);
		model.put("sportItem", sportItem);
		model.put("fieldList", fieldList);
		model.put("programItemList", programItemList);	
		model.put("sport2Item", sport2Item);
		return "admin/sport/open/new_programItemList.vm";
	}
	
	@RequestMapping("/admin/sport/open/bathProgramItem.xhtml")
	public String batchProgramItemTime(Long sportid, Long itemid, ModelMap model){
		Sport2Item sport2Item = sportService.getSport2Item(sportid, itemid);
		if(sport2Item == null) return showJsonError(model, "数据不存在或被删除！");
		ErrorCode<String> code = openTimeTableService.batchProgramItemTime(sport2Item);
		if(!code.isSuccess()) return showJsonError(model, code.getMsg());
		return showJsonSuccess(model);
	}
	
	@RequestMapping("/admin/sport/open/getNewProgramItemTime.xhtml")
	public String getNewProgramItemTime(Long id, Long sportid, Long itemid, ModelMap model){
		ProgramItemTime programItemTime = daoService.getObject(ProgramItemTime.class, id);
		model.put("programItemTime", programItemTime);
		List<SportField> fieldList = sportOrderService.getSportFieldList(sportid, itemid);
		model.put("fieldList", fieldList);
		Sport sport = daoService.getObject(Sport.class, sportid);
		SportItem sportItem = daoService.getObject(SportItem.class, itemid);
		model.put("cursport", sport);
		model.put("sportItem", sportItem);
		Sport2Item sport2Item = sportService.getSport2Item(sportid,itemid);
		model.put("sport2Item", sport2Item);
		return "admin/sport/open/new_programItemDefault.vm";
	}
	//保存基础数据
	@RequestMapping("/admin/sport/open/saveNewBaseData.xhtml")
	public String saveNewBaseData(Long id, SportProfile profile, ModelMap model,HttpServletRequest request) {
		String citycode = getAdminCitycode(request);
		profile.setId(id);
		profile.setCitycode(citycode);
	/*	profile.setLimitminutes(120);
		profile.setTickettype("B");*/
		profile.setBooking(Sport.BOOKING_OPEN);
		profile.setSortnum(0);
		profile.setPretype(SportProfile.PRETYPE_MANAGE);
		if(StringUtils.isBlank(profile.getEncryptCode())) {
			String encryptCode = StringUtil.md5("!@#$%sportid^&*()" + id);
			profile.setEncryptCode(encryptCode);
		}
		Sport sport = daoService.getObject(Sport.class, id);
		sport.setBooking(Sport.BOOKING_OPEN);
		sport.setUpdatetime(DateUtil.getCurFullTimestamp());
		daoService.saveObject(sport);
		daoService.saveObject(profile);
		model.put("sportid", id);
		return showJsonSuccess(model);
	}
	//保存基础数据
	@RequestMapping("/admin/sport/open/tempTime.xhtml")
	public String tempTime(Timestamp addtime, ModelMap model) {
		String sql = "from SportOrder s where s.addtime<=? and s.status like ?";
		List<SportOrder> orderList = hibernateTemplate.find(sql, addtime, "paid%");
		for(SportOrder order : orderList){
			OpenTimeTable ott = daoService.getObject(OpenTimeTable.class, order.getOttid());
			Map<String, String> descMap = VmUtils.readJsonToMap(order.getDescription2());
			String minhour = "00:00";
			if(ott!=null){
				 minhour = jdbcTemplate.queryForObject("select min(hour) from WEBDATA.OPEN_TIMEITEM o where o.ottid=? and memberid=?", String.class, ott.getId(), order.getMemberid());
				if(StringUtils.isBlank(minhour)) minhour = "00:00";
			}else {
				minhour = "00:00";
			}
			descMap.put("时间", DateUtil.format(ott.getPlaydate(),"yyyy-MM-dd") + " " + minhour + ":00");
			order.setDescription2(JsonUtils.writeMapToJson(descMap));
		}
		daoService.saveObjectList(orderList);
		return forwardMessage(model, "更新数据：" + orderList.size());
	}
	//同步所以场次价格
	@RequestMapping("/admin/sport/open/copyOtiPriceList.xhtml")
	public String copyOtiPriceList(Long sportid, Long itemid, ModelMap model){
		Sport2Item sport2Item = sportService.getSport2Item(sportid, itemid);
		if(sport2Item == null) return showJsonError(model, "未找到sport2item");
		List<OpenTimeTable> ottList = sportOrderService.getOttList(sportid, itemid, DateUtil.getBeginningTimeOfDay(new Date()), null, false);
		if(ottList.isEmpty()) return showJsonError(model, "未找到场次！");
		Map<String, String> otherinfoMap = JsonUtils.readJsonToMap(sport2Item.getOtherinfo());
		String openStatus = otherinfoMap.get(Sport2Item.OPEN_STATUS);
		String openBefore = otherinfoMap.get(Sport2Item.OPEN_BEFORE);
		if(StringUtils.isBlank(openStatus)) openStatus = Status.N;
		int count = 0;
		int sum = 0;
		for (OpenTimeTable newTable : ottList) {
			Date playdate = DateUtil.addDay(newTable.getPlaydate(), -7);
			String qry = "from OpenTimeTable t where t.sportid=? and t.itemid=? and t.playdate=? and t.openType=? order by t.id desc";
			List<OpenTimeTable> oldOttList = hibernateTemplate.find(qry, newTable.getSportid(), newTable.getItemid(), playdate, newTable.getOpenType());
			if(oldOttList.size()>0){
				OpenTimeTable old = oldOttList.get(0);
				List<OpenTimeItem> newotiList = openTimeTableService.getOpenItemList(newTable.getId());
				sum += newotiList.size();
				for(OpenTimeItem newOti : newotiList){
					if(newOti.hasZeroPrice()){
						if(isValidItem(newOti)){
							qry = "from OpenTimeItem o where o.ottid=? and o.fieldid=? and o.hour=?";
							List<OpenTimeItem> oldOtiList = hibernateTemplate.find(qry, old.getId(), newOti.getFieldid(), newOti.getHour());
							if(oldOtiList.size()>0){
								OpenTimeItem oldOti = oldOtiList.get(0);
								if(newOti.getCostprice()==0) newOti.setCostprice(oldOti.getCostprice());
								if(newOti.getNorprice()==0) newOti.setNorprice(oldOti.getNorprice());
								if(newOti.getPrice()==0) newOti.setPrice(oldOti.getPrice());
								if(newOti.getSettleid()==null) newOti.setSettleid(oldOti.getSettleid());
								if(newOti.getUpsetprice()==0) newOti.setUpsetprice(oldOti.getUpsetprice());
								daoService.saveObject(newOti);
								count++;
							}
						}
					}
					if(!StringUtils.equals(openStatus, newTable.getStatus()) && !newOti.hasZeroPrice()){
						if(StringUtils.equals(openStatus, OpenTimeTableConstant.STATUS_BOOK) && !newOti.hasZeroPrice() && ValidateUtil.isNumber(openBefore)){
							if(!DateUtil.isAfter(DateUtil.addDay(newTable.getPlaydate(), -Integer.parseInt(openBefore)))) newTable.setStatus(OpenTimeTableConstant.STATUS_BOOK);
							else newTable.setStatus(OpenTimeTableConstant.STATUS_NOBOOK);
						}else{
							newTable.setStatus(OpenTimeTableConstant.STATUS_NOBOOK);
						}
						daoService.saveObject(newTable);
					}
				}
			}
		}
		return showJsonSuccess(model, "有"  + sum + "个记录，本次更新" + count +"个");
	}

}
