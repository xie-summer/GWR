package com.gewara.web.action.admin.drama;

import java.awt.Insets;
import java.io.InputStream;
import java.io.StringReader;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.support.PropertyComparator;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.multipart.MultipartResolver;
import org.zefer.pd4ml.PD4Constants;
import org.zefer.pd4ml.PD4ML;

import com.gewara.command.DisQuantityCommand;
import com.gewara.command.SearchOrderCommand;
import com.gewara.constant.DramaConstant;
import com.gewara.constant.OdiConstant;
import com.gewara.constant.PaymethodConstant;
import com.gewara.constant.Status;
import com.gewara.constant.TagConstant;
import com.gewara.constant.order.BuyItemConstant;
import com.gewara.constant.ticket.OpiConstant;
import com.gewara.constant.ticket.OrderConstant;
import com.gewara.constant.ticket.OrderExtraConstant;
import com.gewara.helper.DisQuanHelper;
import com.gewara.helper.DramaSeatStatusUtil;
import com.gewara.helper.TspHelper;
import com.gewara.model.acl.User;
import com.gewara.model.api.CooperUser;
import com.gewara.model.drama.DisQuantity;
import com.gewara.model.drama.Drama;
import com.gewara.model.drama.DramaOrder;
import com.gewara.model.drama.DramaPlayItem;
import com.gewara.model.drama.DramaSettle;
import com.gewara.model.drama.OpenDramaItem;
import com.gewara.model.drama.OpenTheatreSeat;
import com.gewara.model.drama.SellDramaSeat;
import com.gewara.model.drama.Theatre;
import com.gewara.model.drama.TheatreField;
import com.gewara.model.drama.TheatreProfile;
import com.gewara.model.drama.TheatreRoom;
import com.gewara.model.drama.TheatreSeatArea;
import com.gewara.model.drama.TheatreSeatPrice;
import com.gewara.model.express.ExpressConfig;
import com.gewara.model.express.TicketFaceConfig;
import com.gewara.model.goods.TicketGoods;
import com.gewara.model.pay.BuyItem;
import com.gewara.model.pay.ElecCardBatch;
import com.gewara.model.pay.GewaOrder;
import com.gewara.model.pay.GoodsOrder;
import com.gewara.model.pay.OrderAddress;
import com.gewara.model.pay.OrderNote;
import com.gewara.model.pay.PayBank;
import com.gewara.model.pay.SettleConfig;
import com.gewara.service.BarcodeService;
import com.gewara.service.MessageService;
import com.gewara.service.OrderException;
import com.gewara.service.drama.DpiManageService;
import com.gewara.service.drama.DramaOrderService;
import com.gewara.service.drama.DramaPlayItemService;
import com.gewara.service.drama.OpenDramaService;
import com.gewara.service.gewapay.PaymentService;
import com.gewara.service.order.GoodsService;
import com.gewara.service.order.OrderQueryService;
import com.gewara.support.ErrorCode;
import com.gewara.support.MultiPropertyComparator;
import com.gewara.support.VelocityTemplate;
import com.gewara.untrans.drama.OdiOpenService;
import com.gewara.untrans.drama.RemoteTheatreService;
import com.gewara.untrans.drama.TheatreOperationService;
import com.gewara.util.BeanUtil;
import com.gewara.util.BindUtils;
import com.gewara.util.ChangeEntry;
import com.gewara.util.DateUtil;
import com.gewara.util.JsonUtils;
import com.gewara.util.StringUtil;
import com.gewara.util.VmUtils;
import com.gewara.util.WebUtils;
import com.gewara.web.action.admin.BaseAdminController;
import com.gewara.web.action.inner.util.DramaRemoteUtil;
@Controller
public class DramaOrderAdminController extends BaseAdminController{
	@Autowired@Qualifier("orderQueryService")
	private OrderQueryService orderQueryService;
	@Autowired@Qualifier("openDramaService")
	private OpenDramaService openDramaService;
	public void setOpenDramaService(OpenDramaService openDramaService) {
		this.openDramaService = openDramaService;
	}
	@Autowired@Qualifier("dramaPlayItemService")
	private DramaPlayItemService dramaPlayItemService;
	public void setDramaPlayItemService(DramaPlayItemService dramaPlayItemService) {
		this.dramaPlayItemService = dramaPlayItemService;
	}
	
	@Autowired@Qualifier("dramaOrderService")
	private DramaOrderService dramaOrderService;
	public void setDramaOrderService(DramaOrderService dramaOrderService) {
		this.dramaOrderService = dramaOrderService;
	}
	
	@Autowired@Qualifier("gewaMultipartResolver")
	private MultipartResolver gewaMultipartResolver;
	
	@Autowired@Qualifier("paymentService")
	private PaymentService paymentService;
	
	@Autowired@Qualifier("barcodeService")
	private BarcodeService barcodeService;

	@Autowired@Qualifier("messageService")
	private MessageService messageService;
	
	@Autowired@Qualifier("dpiManageService")
	private DpiManageService dpiManageService;

	@Autowired@Qualifier("theatreOperationService")
	private TheatreOperationService theatreOperationService;
	
	@Autowired@Qualifier("remoteTheatreService")
	private RemoteTheatreService remoteTheatreService;
	
	@Autowired@Qualifier("odiOpenService")
	private OdiOpenService odiOpenService;
	
	@Autowired@Qualifier("velocityTemplate")
	private VelocityTemplate velocityTemplate;
	
	@Autowired@Qualifier("goodsService")
	private GoodsService goodsService;
	
	protected List<Map<String, String>> getYMList(List<String> tmpList){
		List<Map<String, String>> list = new ArrayList<Map<String, String>>();
		Date date = new Date();
		int year = DateUtil.getYear(date);
		int year2 = year+1;
		String ym = DateUtil.format(date, "yyyy-MM");
		for(int i=1;i<=12;i++){
			String tmp = i+"";
			if(i<=9) tmp = "0"+i;
			String strYM = year + "-" + tmp;
			Map m = new HashMap();
			if(strYM.compareTo(ym)>=0 && !tmpList.contains(strYM)) {
				m.put("playdate", strYM);
				m.put("count", 0);
				list.add(m);
			}
			String strYM2 = year2 + "-" + tmp;
			if(!tmpList.contains(strYM2)){
				Map m2 = new HashMap();
				m2.put("playdate", strYM2);
				m2.put("count", 0);
				list.add(m2);
			}
		}
		return list;
	}
	class ValueComparator implements Comparator<Map<String, String>> {
		public int compare(Map<String, String> arg0, Map<String, String> arg1) {
			return arg0.get("playdate").compareTo(arg1.get("playdate"));
		}
	}
	@RequestMapping("/admin/dramaTicket/odiList.xhtml")
	public String odiList(Long tid, Date date, Integer maxnum, HttpServletRequest request, ModelMap model){
		final String viewPage = "admin/theatreticket/odiList.vm";
		if(maxnum == null) maxnum = 10;
		String citycode = getAdminCitycode(request);
		Theatre theatre = daoService.getObject(Theatre.class, tid);
		if(theatre == null){
			List<Theatre> theatreList = dramaPlayItemService.getTheatreidByDramaid(citycode, 0, 1);
			if(theatreList.isEmpty()) return viewPage;
			theatre = theatreList.get(0);
			tid = theatre.getId();
		}
		model.put("theatre", theatre);
		Timestamp curtime = new Timestamp(System.currentTimeMillis());
		Timestamp begintime = DateUtil.getBeginningTimeOfDay(curtime);
		String qry = "select new map(to_char(o.playtime, 'yyyy-MM') as playdate, count(*) as count) from DramaPlayItem o " +
				"where o.theatreid=? and o.citycode=? and (o.playtime>=? and o.period=? or o.endtime>=? and o.period=? ) and o.status=?" +
				"group by to_char(o.playtime,'yyyy-MM') order by to_char(o.playtime,'yyyy-MM')";
		List<Map<String, String>> dateMapList = hibernateTemplate.find(qry, tid, citycode, begintime, Status.Y, begintime, Status.N, Status.Y);
		List<String> tmpList = new ArrayList<String>();
		for(Map<String, String> dateMap : dateMapList){
			tmpList.add(dateMap.get("playdate"));
		}
		
		Collections.sort(dateMapList, new ValueComparator());
		if(date==null && dateMapList.size()>0){
			Map m = dateMapList.get(0);
			date = DateUtil.parseDate(m.get("playdate").toString(),"yyyy-MM-dd");
		}
		if(date==null) date = new Date();
		Timestamp from = new Timestamp(date.getTime());
		Timestamp begin = DateUtil.getBeginningTimeOfDay(from);
		Date lastdate = DateUtil.getMonthLastDay(date);
		Timestamp last = new Timestamp(lastdate.getTime());
		Timestamp to = DateUtil.getLastTimeOfDay(DateUtil.getMonthLastDay(last));
		List<DramaPlayItem> itemList = dramaPlayItemService.getUnOpenDramaPlayItemList(citycode, tid, null, begin, to, maxnum);
		dramaPlayItemService.initDramaPlayItemList(itemList);
		Map<Long, TspHelper> tspHelperMap = new HashMap<Long, TspHelper>();
		Map<Long, TheatreField> fieldMap = new HashMap<Long, TheatreField>();
		for(DramaPlayItem item : itemList){
			TheatreField field = daoService.getObject(TheatreField.class, item.getRoomid());
			List<TheatreSeatPrice> tspList = dramaPlayItemService.getTspList(item.getId());
			TspHelper tspHelper = new TspHelper(tspList);
			tspHelperMap.put(item.getId(), tspHelper);
			fieldMap.put(item.getId(), field);
		}
		model.put("itemList", itemList);
		model.put("fieldMap", fieldMap);
		model.put("tspHelperMap", tspHelperMap);
		qry = "from OpenDramaItem o where o.theatreid=? and o.citycode=? and (o.playtime>=? and o.playtime<=? and o.period=? or o.endtime>=? and o.endtime<=? and o.period=? )order by o.playtime";
		List<OpenDramaItem> odiList = hibernateTemplate.find(qry, tid, citycode, begin, to, Status.Y, begin, to, Status.N);
		Map<Long, List<TheatreSeatPrice>> tspMap = new HashMap<Long, List<TheatreSeatPrice>>();
		Map<Long, List<TheatreSeatArea>> areaMap = new HashMap<Long, List<TheatreSeatArea>>();
		Map<Long, List<TheatreSeatPrice>> priceMap = new HashMap<Long, List<TheatreSeatPrice>>();
		for(OpenDramaItem odi : odiList){
			DramaPlayItem item = daoService.getObject(DramaPlayItem.class, odi.getDpid());
			List<TheatreSeatPrice> tspList = dramaPlayItemService.getTspList(item.getId());
			TspHelper tspHelper = new TspHelper(tspList);
			tspMap.put(odi.getId(), tspHelper.getTspListBySno());
			List<TheatreSeatArea> areaList = daoService.getObjectListByField(TheatreSeatArea.class, "dpid", odi.getDpid());
			Collections.sort(areaList, new MultiPropertyComparator(new String[]{"roomnum"},  new boolean[]{true}));
			areaMap.put(odi.getDpid(), areaList);
			for(TheatreSeatArea area : areaList){
				List<TheatreSeatPrice> priceList = daoService.getObjectListByField(TheatreSeatPrice.class, "areaid", area.getId());
				Collections.sort(priceList, new MultiPropertyComparator(new String[]{"addtime"},  new boolean[]{true}));
				priceMap.put(area.getId(), priceList);
			}
		}
		model.put("maxnum", maxnum);
		model.put("tspMap", tspMap);
		model.put("areaMap", areaMap);
		model.put("priceMap", priceMap);
		Collections.sort(odiList, new MultiPropertyComparator(new String[]{"sorted", "sortnum", "playtime"}, new boolean[]{true, true, true}));
		model.put("odiList", odiList);
		model.put("dateMapList", dateMapList);
		model.put("curdate", DateUtil.format(date, "yyyy-MM-dd"));
		model.put("partnerTextMap", OdiConstant.partnerTextMap);
		return viewPage;
	}
	@RequestMapping("/admin/dramaTicket/searchOdiList.xhtml")
	public String searchOdiList(Long tid, Timestamp starttime, Timestamp endtime, HttpServletRequest request, ModelMap model){
		model.put("theatre", daoService.getObject(Theatre.class, tid));
		String citycode = getAdminCitycode(request);
		String url = "admin/theatreticket/searchOdiList.vm";
		if(starttime==null || endtime==null)  return url;
		List<OpenDramaItem> odiList = openDramaService.getOdiList(citycode, tid, null, starttime, endtime, false);
		Map<Long, List<TheatreSeatPrice>> tspMap = new HashMap<Long, List<TheatreSeatPrice>>();
		for(OpenDramaItem odi : odiList){
			DramaPlayItem item = daoService.getObject(DramaPlayItem.class, odi.getDpid());
			List<TheatreSeatPrice> tspList = dramaPlayItemService.getTspList(item.getId());
			TspHelper tspHelper = new TspHelper(tspList);
			tspMap.put(odi.getId(), tspHelper.getTspListBySno());
		}
		model.put("tspMap", tspMap);
		model.put("odiList", odiList);
		model.put("endtime", endtime);
		model.put("starttime", starttime);
		return url;
	}
	
	
	@RequestMapping("/admin/dramaTicket/baseData.xhtml")
	public String baseData(Long tid, ModelMap model){
		if(tid==null) return forwardMessage(model, "缺少参数！");
		Theatre theatre = daoService.getObject(Theatre.class, tid);
		TheatreProfile profile = daoService.getObject(TheatreProfile.class, tid);
		List<TheatreField> fieldList = daoService.getObjectListByField(TheatreField.class, "theatreid", tid);
		model.put("fieldList", fieldList);
		List<TheatreRoom> roomList = daoService.getObjectListByField(TheatreRoom.class, "theatreid", tid);
		Map<Long, List<TheatreRoom>> roomMap = BeanUtil.groupBeanList(roomList, "fieldid");
		model.put("profile", profile);
		model.put("theatre", theatre);
		model.put("fieldList", fieldList);
		model.put("roomMap", roomMap);
		model.put("partnerTextMap", OdiConstant.partnerTextMap);
		return "admin/theatreticket/baseData.vm";
	}
	@RequestMapping("/admin/dramaTicket/saveBaseData.xhtml")
	public String saveBaseData(Long tid, HttpServletRequest request, ModelMap model){
		Theatre theatre = daoService.getObject(Theatre.class, tid);
		if(theatre == null) return showJsonError(model, "场馆数据不存在！");
		User user = getLogonUser();
		TheatreProfile profile = null;
		model.put("tid", tid);
		profile = daoService.getObject(TheatreProfile.class, tid);
		if(profile == null){
			profile = new TheatreProfile(tid);
		}else{
			profile.setUpdatetime(DateUtil.getCurFullTimestamp());
		}
		Map<String,String> dataMap = WebUtils.getRequestMap(request);
		BindUtils.bindData(profile, dataMap);
		if(StringUtils.isBlank(profile.getNotifymsg1())) {
			return showJsonError(model, "成功短信内容不能为空！");
		}
		if(StringUtils.isBlank(profile.getTakemethod())) {
			return showJsonError(model, "取票方式不能为空！");
		}
		ChangeEntry changeEntry = new ChangeEntry(profile);
		daoService.saveObject(profile);
		monitorService.saveChangeLog(user.getId(), TheatreProfile.class, profile.getId(),changeEntry.getChangeMap( profile));
		ChangeEntry changeEntry2 = new ChangeEntry(theatre);
		theatre.setBooking(profile.getStatus());
		daoService.saveObject(theatre);
		monitorService.saveChangeLog(user.getId(), Theatre.class, theatre.getId(),changeEntry2.getChangeMap( theatre));
		return showJsonSuccess(model);
	}
	@RequestMapping("/admin/dramaTicket/updateRoom.xhtml")
	public String updateRoom(long secid, int linenum, int ranknum, int seatnum, ModelMap model){
		TheatreRoom section = daoService.getObject(TheatreRoom.class, secid);
		ChangeEntry changeEntry = new ChangeEntry(section);
		section.setLinenum(linenum);
		section.setRanknum(ranknum);
		section.setSeatnum(seatnum);
		section.setUpdatetime(DateUtil.getCurFullTimestamp());
		section.setSynchtime(new Timestamp(System.currentTimeMillis()));
		daoService.saveObject(section);
		String query = "delete from TheatreRoomSeat where roomid = ? and (lineno > ? or rankno > ?)";
		hibernateTemplate.bulkUpdate(query, section.getId(), linenum, ranknum);
		monitorService.saveChangeLog(getLogonUser().getId(), TheatreRoom.class, section.getId(),changeEntry.getChangeMap(section));
		return showJsonSuccess(model);
	}
	@RequestMapping("/admin/dramaTicket/firstTsp.xhtml")
	public String updateRoom(Long itemid, ModelMap model){
		DramaPlayItem item = daoService.getObject(DramaPlayItem.class, itemid);
		List<TheatreSeatPrice> tspList = dramaPlayItemService.getTspList(item.getId());
		TspHelper tspHelper = new TspHelper(tspList);
		TheatreSeatPrice tsp = tspHelper.getFirstTsp();
		if(tsp==null) return showJsonError(model, "该场次还没设置价格！");
		return showJsonSuccess(model, BeanUtil.getBeanMapWithKey(tsp, "id", "costprice", "price", "theatreprice", "seattype"));
	}
	@RequestMapping("/admin/dramaTicket/openDramPlayItem.xhtml")
	public String saveOdi(Long dpid, ModelMap model){
		ErrorCode code;
		try {
			code = odiOpenService.saveOpenDramaItem(getLogonUser().getId(), dpid);
			if(!code.isSuccess()) return showJsonError(model, code.getMsg());
		} catch (OrderException e) {
			dbLogger.warn("", e);
			return showJsonError(model, e.getMsg());
		}
		return showJsonSuccess(model);
	}
	
	@RequestMapping("/admin/dramaTicket/refreshAreaSeat.xhtml")
	public String refreshAreaSeat(Long itemid, ModelMap model){
		DramaPlayItem item = daoService.getObject(DramaPlayItem.class, itemid);
		if(item == null) return showJsonError(model, "场区不存在或被删除！");
		ErrorCode code = remoteTheatreService.refreshOpenTheatreSeat(item);
		if(!code.isSuccess()) return showJsonError(model, code.getMsg());
		return showJsonSuccess(model);
	}
	
	@RequestMapping("/admin/dramaTicket/refreshAreaHotzone.xhtml")
	public String refreshHotzone(Long itemid, ModelMap model){
		DramaPlayItem item = daoService.getObject(DramaPlayItem.class, itemid);
		if(item == null) return forwardMessage(model, "场区不存在或被删除！");
		List<TheatreSeatArea> seatAreaList = daoService.getObjectListByField(TheatreSeatArea.class, "dpid", item.getId());
		Map<String, TheatreSeatArea> seatAreaMap = BeanUtil.beanListToMap(seatAreaList, "roomnum");
		List<TheatreRoom> roomList = daoService.getObjectListByField(TheatreRoom.class, "fieldid", item.getRoomid());
		Map<String, TheatreRoom> roomMap = BeanUtil.beanListToMap(roomList, "num");
		List<String> msgList = new ArrayList<String>();
		List<TheatreSeatArea> newAreaList = new ArrayList<TheatreSeatArea>();
		for (String roomnum : seatAreaMap.keySet()) {
			TheatreRoom room = roomMap.get(roomnum);
			TheatreSeatArea seatArea = seatAreaMap.get(roomnum);
			if(room == null){
				msgList.add("场区：" + BeanUtil.buildString(seatAreaMap.get(roomnum), true));
				continue;
			}
			String msg = "场区：" + seatArea.getAreaname() + "坐标：" + seatArea.getHotzone();
			seatArea.setHotzone(room.getHotzone());
			msg += "---->坐标：" + seatArea.getHotzone();
			msgList.add(msg);
			newAreaList.add(seatArea);
		}
		daoService.saveObjectList(newAreaList);
		return forwardMessage(model, msgList);
	}

	@RequestMapping("/admin/dramaTicket/updateBatchOpDrarm.xhtml")
	public String updateBatchOpDrarm(String opOids,String openDramaSelect,ModelMap model){
		User user = getLogonUser();
		String[] ids = null;
		if(StringUtils.isNotBlank(opOids)){
			ids = StringUtils.split(opOids,",");
		}else{
			return showJsonSuccess(model, "未选择场次！");
		}
		for(String id :ids){
			OpenDramaItem odi = daoService.getObjectByUkey(OpenDramaItem.class, "dpid", Long.parseLong(id), false);
			ErrorCode<OpenDramaItem> code = openDramaService.updateStatus(odi, openDramaSelect, user.getId());
			if(!code.isSuccess()) return showJsonError(model, code.getMsg());
			//废弃场次取消发送短信
			if(Status.DEL.equals(openDramaSelect)){
				List<String> orderList = orderQueryService.getTradeNoListByMpid(TagConstant.TAG_DRAMA, Long.parseLong(openDramaSelect), OrderConstant.STATUS_PAID_SUCCESS);
				messageService.updateSMSRecordStatus(orderList);
			}
		}
		return showJsonSuccess(model);
	}
	
	
	
	@RequestMapping("/admin/dramaTicket/updateElecard.xhtml")
	public String updateElecard(Long itemid, String elecard, ModelMap model){
		User user = getLogonUser();
		OpenDramaItem odi = daoService.getObjectByUkey(OpenDramaItem.class, "dpid", itemid, false);
		dbLogger.warn("修改可用抵用券[" + user.getId() + "]:" + odi.getDpid() + odi.getElecard() + "->" + elecard);
		ChangeEntry changeEntry = new ChangeEntry(odi);
		odi.setElecard(elecard);
		daoService.saveObject(odi);
		monitorService.saveChangeLog(user.getId(), OpenDramaItem.class, odi.getId(),changeEntry.getChangeMap( odi));
		return showJsonSuccess(model);
	}
	@RequestMapping("/admin/dramaTicket/updateExpress.xhtml")
	public String updateExpress(Long itemid, String expressid, ModelMap model){
		User user = getLogonUser();
		OpenDramaItem odi = daoService.getObjectByUkey(OpenDramaItem.class, "dpid", itemid, false);
		ChangeEntry changeEntry = new ChangeEntry(odi);
		if(StringUtils.isNotBlank(expressid)){
			ExpressConfig config = daoService.getObject(ExpressConfig.class, expressid);
			if(config == null) return showJsonError(model, "编号为：" + expressid + ",的配送方式不存在或被删除！");
			if(!StringUtils.contains(odi.getTakemethod(), OdiConstant.TAKEMETHOD_KUAIDI)){
				return showJsonError(model, "没有设置快递方式！");
			}
			odi.setExpressid(expressid);
		}else{
			odi.setExpressid(null);
		}
		daoService.saveObject(odi);
		monitorService.saveChangeLog(user.getId(), OpenDramaItem.class, odi.getId(),changeEntry.getChangeMap( odi));
		return showJsonSuccess(model);
	}
	
	@RequestMapping("/admin/dramaTicket/updateTicketFace.xhtml")
	public String updateTicketFace(Long itemid, String ticketfaceid, ModelMap model){
		User user = getLogonUser();
		OpenDramaItem odi = daoService.getObjectByUkey(OpenDramaItem.class, "dpid", itemid, false);
		ChangeEntry changeEntry = new ChangeEntry(odi);
		if(StringUtils.isNotBlank(ticketfaceid)){
			TicketFaceConfig config = daoService.getObject(TicketFaceConfig.class, ticketfaceid);
			if(config == null) return showJsonError(model, "编号为：" + ticketfaceid + ",的票面模板不存在或被删除！");
			odi.setTicketfaceid(ticketfaceid);
		}else{
			odi.setTicketfaceid(null);
		}
		daoService.saveObject(odi);
		monitorService.saveChangeLog(user.getId(), OpenDramaItem.class, odi.getId(),changeEntry.getChangeMap(odi));
		return showJsonSuccess(model);
	}
	
	@RequestMapping("/admin/dramaTicket/updateSortnum.xhtml")
	public String updateSortnum(Long itemid, Integer sortnum, ModelMap model){
		if(sortnum == null) return showJsonError(model, "序号不能为空！");
		User user = getLogonUser();
		DramaPlayItem item = daoService.getObject(DramaPlayItem.class, itemid);
		ChangeEntry changeEntry1 = new ChangeEntry(item);
		OpenDramaItem odi = daoService.getObjectByUkey(OpenDramaItem.class, "dpid", itemid, false);
		if(odi != null){
			ChangeEntry changeEntry = new ChangeEntry(odi);
			odi.setSortnum(sortnum);
			monitorService.saveChangeLog(user.getId(), OpenDramaItem.class, odi.getId(),changeEntry.getChangeMap(odi));
			daoService.saveObject(odi);
		}
		item.setSortnum(sortnum);
		daoService.saveObject(item);
		monitorService.saveChangeLog(user.getId(), DramaPlayItem.class, item.getId(),changeEntry1.getChangeMap(item));
		return showJsonSuccess(model);
	}

	@RequestMapping("/admin/dramaTicket/setStatus.xhtml")
	public String setStatus(Long itemid, String status, ModelMap model){
		User user = getLogonUser();
		OpenDramaItem odi = daoService.getObjectByUkey(OpenDramaItem.class, "dpid", itemid, false);
		if(StringUtils.equals(status, Status.Y)){
			ErrorCode<String> code = openDramaService.validOpenDramaItem(odi);
			if(!code.isSuccess()){
				return showJsonError(model, code.getMsg());
			}
		}
		ErrorCode<OpenDramaItem> code = openDramaService.updateStatus(odi, status, user.getId());
		if(!code.isSuccess()) return showJsonError(model, code.getMsg());
		//废弃场次取消发送短信
		if(Status.DEL.equals(status)){
			List<String> orderList = orderQueryService.getTradeNoListByMpid(TagConstant.TAG_DRAMA, itemid, OrderConstant.STATUS_PAID_SUCCESS);
			messageService.updateSMSRecordStatus(orderList);
		}
		String isBook = "noBook";
		if(odi.isBooking())
			isBook = "isBook";
		return showJsonSuccess(model,isBook);
	}
	@RequestMapping("/admin/dramaTicket/setPartner.xhtml")
	public String setPartner(Long itemid, String status, ModelMap model){
		User user = getLogonUser();
		OpenDramaItem odi = daoService.getObjectByUkey(OpenDramaItem.class, "dpid", itemid, false);
		ErrorCode<OpenDramaItem> code = openDramaService.updatePartner(odi, status, user.getId());
		if(!code.isSuccess()) return showJsonError(model, code.getMsg());
		return showJsonSuccess(model);
	}
	@RequestMapping("/admin/dramaTicket/setOpenTime.xhtml")
	public String setOdiOpenTime(Long itemid, Timestamp opentime, ModelMap model){
		OpenDramaItem odi = daoService.getObjectByUkey(OpenDramaItem.class, "dpid", itemid, false);
		ErrorCode<String> code = openDramaService.validOpenDramaItem(odi);
		if(!code.isSuccess()){
			return showJsonError(model, code.getMsg());
		}
		ChangeEntry changeEntry = new ChangeEntry(odi);
		odi.setOpentime(opentime);
		daoService.saveObject(odi);
		monitorService.saveChangeLog(getLogonUser().getId(), OpenDramaItem.class, odi.getId(),changeEntry.getChangeMap( odi));
		return showJsonSuccess(model);
	}
	@RequestMapping("/admin/dramaTicket/setCloseTime.xhtml")
	public String setCloseTime(Long itemid, Timestamp closetime, ModelMap model){
		OpenDramaItem odi = daoService.getObjectByUkey(OpenDramaItem.class, "dpid", itemid, false);
		ChangeEntry changeEntry = new ChangeEntry(odi);
		odi.setClosetime(closetime);
		daoService.saveObject(odi);
		monitorService.saveChangeLog(getLogonUser().getId(), OpenDramaItem.class, odi.getId(),changeEntry.getChangeMap( odi));
		return showJsonSuccess(model);
	}
	
	private Map getOpenSeatMap(OpenDramaItem odi, Long areaid){
		Map model = new HashMap();
		if(areaid != null){
			Map<Integer, String> rowMap = new HashMap<Integer, String>();
			Map<String, OpenTheatreSeat> seatMap = new HashMap<String, OpenTheatreSeat>();
			List<OpenTheatreSeat> openSeatList = openDramaService.getOpenTheatreSeatListByDpid(odi.getDpid(), areaid);
			List<SellDramaSeat> selleatList = dramaOrderService.getSellDramaSeatList(odi.getDpid(), areaid);
			DramaSeatStatusUtil seatStatusUtil = new DramaSeatStatusUtil(selleatList);
			model.put("seatStatusUtil", seatStatusUtil);
			for(OpenTheatreSeat seat:openSeatList){
				rowMap.put(seat.getLineno(), seat.getSeatline());
				seatMap.put("row" + seat.getLineno() + "rank" + seat.getRankno(), seat);
			}
			model.put("rowMap", rowMap);
			model.put("seatMap", seatMap);
		}
		TheatreField field = daoService.getObject(TheatreField.class, odi.getRoomid());
		Theatre theatre = daoService.getObject(Theatre.class, odi.getTheatreid());
		Drama drama = daoService.getObject(Drama.class, odi.getDramaid());
		model.put("opi", odi);
		model.put("field", field);
		model.put("drama", drama);
		model.put("theatre", theatre);
		return model;
	}
	@RequestMapping("/admin/dramaTicket/reAreaSeat.xhtml")
	public String reAreaSeat(Long itemid, Long areaid, ModelMap model) {
		User user = getLogonUser();
		OpenDramaItem odi = daoService.getObjectByUkey(OpenDramaItem.class, "dpid", itemid);
		TheatreSeatArea seatArea = daoService.getObject(TheatreSeatArea.class, areaid);
		ErrorCode code = odiOpenService.refreshAreaSeat(user.getId(), odi, seatArea, true, new ArrayList<String>());
		if(!code.isSuccess()) return showJsonError(model, code.getMsg());
		odi.setStatus(Status.N);
		daoService.saveObject(odi);
		return showJsonError(model, ",场次已关闭，请重新开放");
	} 
	@RequestMapping("/admin/dramaTicket/seatprice.xhtml")
	public String seatprice(@RequestParam("itemid")Long itemid, Long areaid, ModelMap model){
		DramaPlayItem item = daoService.getObject(DramaPlayItem.class, itemid);
		if(!item.isOpenseat()){
			model.put("itemid", itemid);
			return showRedirect("admin/dramaTicket/areaprice.xhtml", model);
		}
		TheatreSeatArea curSeatArea = daoService.getObject(TheatreSeatArea.class, areaid);
		List<TheatreSeatArea> areaList = daoService.getObjectListByField(TheatreSeatArea.class, "dpid", itemid);
		Collections.sort(areaList, new MultiPropertyComparator<TheatreSeatArea>(new String[]{"roomnum"}, new boolean[]{true}));
		if(curSeatArea == null){
			if(!areaList.isEmpty()){
				curSeatArea = areaList.get(0);
			}
		}
		OpenDramaItem odi = daoService.getObjectByUkey(OpenDramaItem.class, "dpid", itemid, false);
		if(curSeatArea != null){
			List<TheatreSeatPrice> tspList = daoService.getObjectListByField(TheatreSeatPrice.class, "areaid", curSeatArea.getId());
			TspHelper tspHelper = new TspHelper(tspList);
			List<TheatreSeatPrice> priceList = tspHelper.getTspBySno();
			Collections.sort(priceList, new PropertyComparator("seattype", false, true));
			Map<String,TheatreSeatPrice> seatPriceMap = BeanUtil.beanListToMap(priceList, "seattype");
			model.put("seatPriceMap", seatPriceMap);
			if(odi != null){
				model.putAll(getOpenSeatMap(odi, curSeatArea.getId()));
			}
		}
		model.put("curSeatArea", curSeatArea);
		model.put("areaList", areaList);
		model.put("item", item);
		return "admin/theatreticket/seatprice.vm";
	}
	
	@RequestMapping("/admin/dramaTicket/updateTheatreSeatAreaMapStr.xhtml")
	public String updateTheatreSeatAreaMapStr(Long areaid, ModelMap model) {
		TheatreSeatArea seatArea = daoService.getObject(TheatreSeatArea.class, areaid);
		if(seatArea == null) return showJsonError(model, "该场次区域不存在或被删除！");
		DramaPlayItem item = daoService.getObject(DramaPlayItem.class, seatArea.getDpid());
		if(!item.isOpenseat()) return showJsonError(model, "非选座场次不需更新座位图！");
		String seatmap = openDramaService.getTheatreSeatAreaMapStr(seatArea);
		seatArea.setSeatmap(seatmap);
		seatArea.setUpdatetime(DateUtil.getCurFullTimestamp());
		daoService.saveObject(seatArea);
		return forwardMessage(model, seatmap);
	}
	
	@RequestMapping("/admin/dramaTicket/batchTheatreSeatAreaMapStr.xhtml")
	public String batchTheatreSeatAreaMapStr(ModelMap model){
		String hql = "from TheatreSeatArea t where exists(select o.dpid from OpenDramaItem o where o.dpid=t.dpid and o.opentype=?) and t.seatmap is null";
		List<TheatreSeatArea> areaList = hibernateTemplate.find(hql, OdiConstant.OPEN_TYPE_SEAT);
		List<String> msgList = new ArrayList<String>();
		for (TheatreSeatArea seatArea : areaList) {
			try{
				String seatmap = openDramaService.getTheatreSeatAreaMapStr(seatArea);
				seatArea.setSeatmap(seatmap);
				seatArea.setUpdatetime(DateUtil.getCurFullTimestamp());
				daoService.saveObject(seatArea);
				if(StringUtils.isNotBlank(seatmap)){
					msgList.add(seatmap);
				}
			}catch(Exception e){
				dbLogger.warn("", e);
			}
		}
		if(msgList.isEmpty()){
			msgList.add("没有要更新的数据！");
		}
		return forwardMessage(model, msgList);
	}
	
	@RequestMapping("/admin/dramaTicket/copyAreaHotzone.xhtml")
	public String copyAreaHotzone(Long areaid, ModelMap model){
		TheatreSeatArea seatArea = daoService.getObject(TheatreSeatArea.class, areaid);
		if(seatArea == null) return showJsonError(model, "该场次区域不存在或被删除！");
		TheatreRoom room = dramaPlayItemService.getTheatreRoomByNum(seatArea.getTheatreid(), seatArea.getFieldnum(), seatArea.getRoomnum());
		if(room == null) return showJsonError(model, "基础区域不存在或被删除！");
		if(StringUtils.isBlank(room.getHotzone())) return showJsonError(model, "基础区域坐标没有设置！");
		ChangeEntry changeEntry = new ChangeEntry(seatArea);
		seatArea.setHotzone(room.getHotzone());
		seatArea.setUpdatetime(DateUtil.getCurFullTimestamp());
		daoService.saveObject(seatArea);
		monitorService.saveChangeLog(getLogonUser().getId(), TheatreSeatArea.class, seatArea.getId(), changeEntry.getChangeMap(seatArea));
		return showJsonSuccess(model);
	}
	
	@RequestMapping("/admin/dramaTicket/copySeatArea.xhtml")
	public String copyArea(Long itemid, Long areaid, ModelMap model){
		DramaPlayItem item = daoService.getObject(DramaPlayItem.class, itemid);
		if(item == null) return showJsonError(model, "场次数据不存在或被删除！");
		if(!item.hasGewa()) return showJsonError(model, "非格瓦拉场次不能直接复制区域，需开放场次同步！");
		TheatreRoom room = daoService.getObject(TheatreRoom.class, areaid);
		if(room == null) return showJsonError(model, "区域不存在或被删除！");
		if(StringUtils.isBlank(room.getNum())) return showJsonError(model, "区域序号号不能为空！");
		TheatreField field = daoService.getObject(TheatreField.class, item.getRoomid());
		List<TheatreSeatArea> seatAreaList = daoService.getObjectListByField(TheatreSeatArea.class, "dpid", item.getId());
		Map<String, TheatreSeatArea> areaMap = BeanUtil.beanListToMap(seatAreaList, "roomnum");
		TheatreSeatArea seatArea = areaMap.get(room.getNum());
		if(seatArea != null){
			return showJsonError(model, "本场次已经存在该区域！");
		}
		seatArea = new TheatreSeatArea(item.getId());
		ChangeEntry changeEntry = new ChangeEntry(seatArea);
		DramaRemoteUtil.copyTheatreSeatArea(seatArea, item, field, room);
		daoService.saveObject(seatArea);
		monitorService.saveChangeLog(getLogonUser().getId(), TheatreSeatArea.class, seatArea.getId(), changeEntry.getChangeMap(seatArea));
		return showJsonSuccess(model);
	}
	
	@RequestMapping("/admin/dramaTicket/areaprice.xhtml")
	public String areaprice(Long itemid, ModelMap model){
		DramaPlayItem item = daoService.getObject(DramaPlayItem.class, itemid);
		if(item == null) return show404(model, "场次不存在或被删除！");
		Theatre theatre = daoService.getObject(Theatre.class, item.getTheatreid());
		model.put("theatre", theatre);
		TheatreField field = daoService.getObject(TheatreField.class, item.getRoomid());
		List<TheatreRoom> roomList = daoService.getObjectListByField(TheatreRoom.class, "fieldid", item.getRoomid());
		List<TheatreSeatPrice> seatpriceList = daoService.getObjectListByField(TheatreSeatPrice.class, "dpid", itemid);
		Map<Long, Integer> disCountMap = new HashMap<Long, Integer>();
		for(TheatreSeatPrice tsp : seatpriceList){
			disCountMap.put(tsp.getId(), dramaPlayItemService.getDisQuantityList(tsp.getId()).size());
		}
		Map<Long, List<TheatreSeatPrice>> seatpriceMap = BeanUtil.groupBeanList(seatpriceList, "areaid");
		List<TheatreSeatArea> areaList = daoService.getObjectListByField(TheatreSeatArea.class, "dpid", item.getId());
		Collections.sort(areaList, new MultiPropertyComparator<TheatreSeatArea>(new String[]{"roomnum"}, new boolean[]{true}));
		model.put("field", field);
		model.put("roomList", roomList);
		model.put("areaList", areaList);
		model.put("item", item);
		model.put("disCountMap", disCountMap);
		model.put("seatpriceMap", seatpriceMap);
		List<DramaSettle> settleList = daoService.getObjectListByField(DramaSettle.class, "dramaid", item.getDramaid());
		model.put("settleMap", BeanUtil.beanListToMap(settleList, "settleid"));
		return "admin/theatreticket/odiprice.vm";
	}
	
	@RequestMapping("/admin/dramaTicket/addPriceSeat.xhtml")
	public String addPriceSeat(Long itemid, Long areaid, String seattype, Long seatid, ModelMap model){
		User user = getLogonUser();
		ErrorCode code = openDramaService.addPriceSeat(itemid, areaid, seattype, seatid, user.getId());
		if(!code.isSuccess()) return showJsonError(model, code.getMsg());
		return showJsonSuccess(model);
	}
	@RequestMapping("/admin/dramaTicket/removePriceSeat.xhtml")
	public String removePriceSeat(Long itemid, Long areaid, Long seatid, ModelMap model){
		User user = getLogonUser();
		ErrorCode code = openDramaService.removePriceSeat(itemid, areaid, seatid, user.getId());
		if(!code.isSuccess()) return showJsonError(model, code.getMsg());
		return showJsonSuccess(model);
	}
	
	@RequestMapping("/admin/dramaTicket/booking.xhtml")
	public String booking(@RequestParam("itemid")Long itemid, SearchOrderCommand soc, ModelMap model) {
		OpenDramaItem odi = daoService.getObjectByUkey(OpenDramaItem.class, "dpid", itemid, false);
		if(odi == null) return showMessage(model, "本场不接受预订！");
		soc.setMptype(BuyItemConstant.TAG_DRAMAPLAYITEM);
		soc.setItemid(null);
		soc.setMpid(itemid);
		List<DramaOrder> orderList = dramaOrderService.getDramaOrderList(soc);
		List<String> tradeNoList = BeanUtil.getBeanPropertyList(orderList, "tradeNo", true);
		Map<String, OrderAddress> addressMap = daoService.getObjectMap(OrderAddress.class, tradeNoList);
		Map<Long, Map<Long, OrderNote>> noteMap = new HashMap<Long, Map<Long, OrderNote>>();
		Map<Long, List<BuyItem>> itemMap = new HashMap<Long, List<BuyItem>>();
		for (GewaOrder order : orderList) {
			List<BuyItem> itemList = daoService.getObjectListByField(BuyItem.class, "orderid", order.getId());
			itemMap.put(order.getId(), itemList);
			if(order.isPaidSuccess()){
				List<OrderNote> noteList = daoService.getObjectListByField(OrderNote.class, "orderid", order.getId());
				Map<Long,OrderNote> tmpMap = BeanUtil.beanListToMap(noteList, "smallitemid");
				noteMap.put(order.getId(), tmpMap);
			}
		}
		TheatreSeatArea curSeatArea = daoService.getObject(TheatreSeatArea.class, soc.getAreaid());
		List<String> rLockList = new ArrayList<String>();
		List<TheatreSeatArea> areaList = daoService.getObjectListByField(TheatreSeatArea.class, "dpid", itemid);
		Collections.sort(areaList, new MultiPropertyComparator<TheatreSeatArea>(new String[]{"roomnum"}, new boolean[]{true}));
		if(curSeatArea == null){
			if(!areaList.isEmpty()){
				curSeatArea = areaList.get(0);
			}
		}
		Long areaid = null;
		if(curSeatArea != null){
			if(odi.getPlaytime().after(DateUtil.getCurFullTimestamp())){
				if(odi.isOpenseat()){
					ErrorCode<List<String>> remoteLockList = theatreOperationService.updateRemoteLockSeat(curSeatArea, OdiConstant.SECONDS_UPDATE_SEAT, true);
					if(remoteLockList.isSuccess()){
						rLockList = remoteLockList.getRetval();
						/*opiManageService.updateOpiStats(opi, hfhLockList, false);
						openPlayService.updateOpiSeatMap(mpid, room, openSeatList, hfhLockList, seatStatusUtil);*/
					}else{
						model.put("hfherror", "场馆服务器连接不正常：" + remoteLockList.getErrcode() + ":" + remoteLockList.getMsg());
					}
				}
			}
			areaid = curSeatArea.getId();
		}
		model.put("rLockList", rLockList);
		model.put("curSeatArea", curSeatArea);
		model.put("noteMap", noteMap);
		model.put("itemMap", itemMap);
		model.put("addressMap", addressMap);
		model.put("areaList", areaList);
		model.put("orderList", orderList);
		model.putAll(getOpenSeatMap(odi, areaid));
		return "admin/theatreticket/booking.vm";
	}
	@RequestMapping("/admin/dramaTicket/lockSeat.xhtml")
	public String lockSeat(Long seatId, String locktype, String lockReason, ModelMap model){
		User user = getLogonUser();
		ErrorCode code = openDramaService.lockSeat(seatId, locktype, lockReason, user.getId());
		if(!code.isSuccess()) return showJsonError(model, code.getMsg());
		return showJsonSuccess(model);
	}
	@RequestMapping("/admin/dramaTicket/releaseSeat.xhtml")
	public String releaseSeat(Long seatId, ModelMap model){
		User user = getLogonUser();
		ErrorCode code = openDramaService.unLockSeat(seatId, user.getId());
		if(!code.isSuccess()) return showJsonError(model, code.getMsg());
		return showJsonSuccess(model);
	}
	@RequestMapping("/admin/dramaTicket/batchUnLockSeat.xhtml")
	public String releaseSeat(Long itemid, Long areaid, String lockline, String lockrank, ModelMap model){
		User user = getLogonUser();
		ErrorCode code = openDramaService.batchUnLockSeat(itemid, areaid, lockline, lockrank, user.getId());
		if(!code.isSuccess()) return showJsonError(model, code.getMsg());
		return showJsonSuccess(model);
	}
	@RequestMapping("/admin/dramaTicket/batchLockSeat.xhtml")
	public String batchLockSeat(Long itemid, Long areaid, String locktype, String lockreason, String lockline, String lockrank, ModelMap model){
		User user = getLogonUser();
		ErrorCode code = openDramaService.batchLockSeat(itemid, areaid, locktype, lockreason, lockline, lockrank, user.getId());
		if(!code.isSuccess()) return showJsonError(model, code.getMsg());
		return showJsonSuccess(model);
	}
	@RequestMapping("/admin/dramaTicket/orderList.xhtml")
	public String orderList(SearchOrderCommand soc, String xls,HttpServletRequest request, HttpServletResponse res, ModelMap model){
		if(soc.getPlaceid()!=null) {
			model.put("theatre", daoService.getObject(Theatre.class, soc.getPlaceid()));
		}
		String citycode = getAdminCitycode(request);
		soc.setCitycode(citycode);
		Timestamp cur = DateUtil.getCurFullTimestamp();
		if((soc.getMpid() != null || soc.getPlaceid() != null) && (soc.getTimeFrom() == null || soc.getTimeTo() == null)){
			soc.setTimeTo(cur);
			soc.setTimeFrom(DateUtil.getBeginningTimeOfDay(DateUtil.addDay(cur, -1)));
		}
		if(soc.hasBlankCond()) model.put("msg", "查询条件过少，请精确查询条件！");
		List<GewaOrder> orderList = dramaOrderService.getAllDramaOrderList(soc);
		//List<Long> dpidList = BeanUtil.getBeanPropertyList(orderList, Long.class, "dpid", true);
		List<String> tradeNoList = BeanUtil.getBeanPropertyList(orderList, "tradeNo", true);
		Map<String, OrderAddress> addressMap = daoService.getObjectMap(OrderAddress.class, tradeNoList);
		Map<Long, Map<Long, OrderNote>> noteMap = new HashMap<Long, Map<Long, OrderNote>>();
		Map<Long, List<BuyItem>> itemMap = new HashMap<Long, List<BuyItem>>();
		int totalQuantity = 0;
		int tTotalAmount = 0;
		Set<Long> theatreIdSet = new HashSet<Long>();
		Set<Long> dramaIdSet = new HashSet<Long>();
		for (GewaOrder order : orderList) {
			totalQuantity+=order.getQuantity();
			tTotalAmount+=order.getTotalAmount();
			List<BuyItem> itemList = daoService.getObjectListByField(BuyItem.class, "orderid", order.getId());
			itemMap.put(order.getId(), itemList);
			for (BuyItem buyItem : itemList) {
				theatreIdSet.add(buyItem.getPlaceid());
				dramaIdSet.add(buyItem.getItemid());
			}
			if(order.isPaidSuccess()){
				List<OrderNote> noteList = daoService.getObjectListByField(OrderNote.class, "orderid", order.getId());
				Map<Long,OrderNote> tmpMap = BeanUtil.beanListToMap(noteList, "smallitemid");
				noteMap.put(order.getId(), tmpMap);
			}
		}
		model.put("noteMap", noteMap);
		model.put("itemMap", itemMap);
		model.put("addressMap", addressMap);
		Map<Long, Theatre> orderTheatreMap = daoService.getObjectMap(Theatre.class, theatreIdSet);
		Map<Long, Drama> orderDramaMap = daoService.getObjectMap(Drama.class, dramaIdSet);
		model.put("orderTheatreMap", orderTheatreMap);
		model.put("orderDramaMap", orderDramaMap);
		model.put("orderList", orderList);
		model.put("orderNum", orderList.size());
		model.put("totalQuantity", Integer.toString(totalQuantity));
		model.put("tTotalAmount",Integer.toString(tTotalAmount));
		model.put("ordertype", StringUtils.isBlank(soc.getOrdertype())?" " : soc.getOrdertype());
		model.put("timeFrom", soc.getTimeFrom());
		model.put("timeTo", soc.getTimeTo());
		if(StringUtils.isNotBlank(xls)) {
			download("xls", res);
			model.put("xls", xls);
			return "admin/theatreticket/orders.vm";
		}else{
			List<Long> idList = dramaPlayItemService.getCurBookingTheatreList(citycode, null);
			Map<Long, Theatre> theatreMap = daoService.getObjectMap(Theatre.class, idList);
			model.put("theatreMap", theatreMap);
			if(soc.getPlaceid() != null){
				List<Long> dramaIdList = openDramaService.getCurDramaidList(soc.getPlaceid());
				Map<Long, Drama> dramaMap = daoService.getObjectMap(Drama.class, dramaIdList);
				model.put("dramaMap", dramaMap);
				List<OpenDramaItem> opiList = openDramaService.getOdiList(citycode, soc.getPlaceid(), soc.getItemid(), cur, null, false);
				Map<Long, List<OpenDramaItem>> opiMap = BeanUtil.groupBeanList(opiList, "theatreid");
				List<TicketGoods> goodsList = goodsService.getTicketGoodsList(citycode, TagConstant.TAG_THEATRE, soc.getPlaceid(), TagConstant.TAG_DRAMA, soc.getItemid(), null, cur, null, false, false);
				Map<Long, List<TicketGoods>> goodsTheatreMap = BeanUtil.groupBeanList(goodsList, "relatedid");
				Map<Long, TicketGoods> goodsMap = BeanUtil.beanListToMap(goodsList, "id");
				model.put("goodsTheatreMap", goodsTheatreMap);
				model.put("goodsMap", goodsMap);
				model.put("opiMap", opiMap);
			}
		}
		return "admin/theatreticket/orderList.vm";
	}
	
	@RequestMapping("/admin/ajax/dramaTicket/dramaList.xhtml")
	public String dramaList(Long theatreid, ModelMap model){
		if(theatreid == null) return showJsonError(model, "参数为空！");
		List<Long> dramaIdList = openDramaService.getCurDramaidList(theatreid);
		List<Drama> dramaList = daoService.getObjectList(Drama.class, dramaIdList);
		List<Map> jsonMap = BeanUtil.getBeanMapList(dramaList, "id", "realBriefname");
		return showJsonSuccess(model, JsonUtils.writeObjectToJson(jsonMap));
	}
	
	@RequestMapping("/admin/dramaTicket/setExpressNo.xhtml")
	public String setExpressNo(Long orderId, String expressNo,String expressMode, ModelMap model){
		GewaOrder order = daoService.getObject(GewaOrder.class, orderId);
		if(order == null) return showJsonError(model, "该订单号不存在或被删除！");
		if(StringUtils.isBlank(expressNo)) return showJsonError(model, "快递订单号不能为空！");
		if(order instanceof DramaOrder || order instanceof GoodsOrder){
			Map<String,String> otherInfoMap = JsonUtils.readJsonToMap(order.getOtherinfo());
			otherInfoMap.put(OrderConstant.ORDER_EXPRESSNO, expressNo);
			otherInfoMap.put(OrderConstant.ORDER_EXPRESSMode, expressMode);//快递方式
			order.setOtherinfo(JsonUtils.writeObjectToJson(otherInfoMap));
			daoService.saveObject(order);
			return showJsonSuccess(model);
		}else {
			return showJsonError(model, "未找到此订单！");
		}
	}
	
	@RequestMapping("/admin/dramaTicket/handleFormUpload.xhtml")
	public String handleFormUpload(ModelMap model, HttpServletRequest req) throws Exception {
		List<String> msgList = new ArrayList<String>();
		MultipartHttpServletRequest multipartRequest = gewaMultipartResolver.resolveMultipart(req);
		MultipartFile file = multipartRequest.getFile("file");
		if(file == null){
			msgList.add("上传文件为空!");
			return forwardMessage(model, msgList);
		}
		// 检测文件后缀bob.20120611
		String orifilename = file.getOriginalFilename();
		if(!StringUtils.endsWithIgnoreCase(orifilename, "xls")){
			msgList.add("请确认上传文件为2003版的excel文件(xls), 07版本的暂不支持(xlsx)!");
			return forwardMessage(model, msgList); 
		}
		InputStream stream = null;
		stream = file.getInputStream();
		POIFSFileSystem fs;
		HSSFWorkbook workbook;
		fs = new POIFSFileSystem(stream);
		workbook = new HSSFWorkbook(fs);
		HSSFSheet dataSheet = workbook.getSheetAt(0);
		HSSFRow dataRow = null;
		String tradeNo = "";
		String expressNo = "";
		String expressMode = "";
		//获取Excel所有信息开始导入
		int lastNum = dataSheet.getLastRowNum();
		List<DramaOrder> orderList = new ArrayList();
		for(int i=1;i<lastNum;i++){
			dataRow = dataSheet.getRow(i);
			if(dataRow!=null){
				HSSFCell dataCell = dataRow.getCell(1);//获取订单号
				if(dataCell!=null){
					tradeNo = dataCell.getRichStringCellValue().getString();
					if(StringUtils.isNotBlank(tradeNo)){
						tradeNo = StringUtils.replace(tradeNo, "[", "");
						tradeNo = StringUtils.replace(tradeNo, "]", "");
						DramaOrder order = daoService.getObjectByUkey(DramaOrder.class, "tradeNo", tradeNo);
						if(order == null) continue;
						dataCell = dataRow.getCell(13);
						if(dataCell!=null){
							expressNo = dataCell.getRichStringCellValue().getString();
							expressNo = StringUtils.trim(expressNo);
						}else{
							expressNo = null;
						}
						if(StringUtils.isBlank(expressNo)) continue;
						dataCell = dataRow.getCell(12);
						if(dataCell!=null){
							expressMode = dataCell.getRichStringCellValue().getString();
							expressMode = StringUtils.trim(expressMode);
						}else{
							expressMode = null;
						}
						if(StringUtils.isBlank(expressMode)) continue;
						Map<String,String> otherInfoMap = JsonUtils.readJsonToMap(order.getOtherinfo());
						otherInfoMap.put(OrderConstant.ORDER_EXPRESSNO, expressNo);
						otherInfoMap.put(OrderConstant.ORDER_EXPRESSMode, expressMode);//快递方式
						order.setOtherinfo(JsonUtils.writeObjectToJson(otherInfoMap));
						orderList.add(order);
					}
				}
			}
		}
		daoService.saveObjectList(orderList);
		return "redirect:/admin/dramaTicket/odiList.xhtml";
	}
	
	@RequestMapping("/admin/dramaTicket/odiDetail.xhtml")
	public String odiDetail(Long itemid, ModelMap model){
		OpenDramaItem odi = daoService.getObjectByUkey(OpenDramaItem.class, "dpid", itemid, false);
		model.put("odi", odi);
		model.put("theatre", daoService.getObject(Theatre.class, odi.getTheatreid()));
		model.put("drama", daoService.getObject(Drama.class, odi.getDramaid()));
		model.put("dramaSaleCycleMap", DramaConstant.dramaSaleCycleMap);
		return "admin/theatreticket/odiDetail.vm";
	}
	@RequestMapping("/admin/dramaTicket/modOdi.xhtml")
	public String modOdi(Long itemid, HttpServletRequest request, ModelMap model){
		User user = getLogonUser();
		OpenDramaItem odi = daoService.getObjectByUkey(OpenDramaItem.class, "dpid", itemid, false);
		if(odi == null) return showJsonError_NOT_FOUND(model);
		ChangeEntry changeEntry = new ChangeEntry(odi);
		dbLogger.warn("用户：" + user.getId()+"修改演出场次：" + itemid);
		BindUtils.bindData(odi, request.getParameterMap());
		DramaPlayItem item = daoService.getObject(DramaPlayItem.class, itemid);
		ChangeEntry changeEntry2 = new ChangeEntry(item);
		odi.setUpdatetime(DateUtil.getCurFullTimestamp());
		daoService.saveObject(odi);
		monitorService.saveChangeLog(user.getId(), OpenDramaItem.class, odi.getId(), changeEntry.getChangeMap(odi));
		item.setPlaytime(odi.getPlaytime());
		daoService.saveObject(item);
		barcodeService.createNewBarcodeByPlaceid(odi.getTheatreid());
		monitorService.saveChangeLog(user.getId(), DramaPlayItem.class, item.getId(), changeEntry2.getChangeMap(item));
		return showJsonSuccess(model);
	}
	//场次详细
	@RequestMapping("/admin/dramaTicket/odiForm.xhtml")
	public String ottDetail(Long itemid, ModelMap model) {
		OpenDramaItem odi = daoService.getObjectByUkey(OpenDramaItem.class, "dpid", itemid, false);
		List<PayBank> bankList = paymentService.getPayBankList(PayBank.TYPE_PC);
		model.put("odi", odi);
		model.put("otherinfo", odi.getOtherinfo());
		model.put("theatre", daoService.getObject(Theatre.class, odi.getTheatreid()));
		model.put("drama", daoService.getObject(Drama.class, odi.getDramaid()));
		model.put("confPayList", bankList);
		model.put("payTextMap", PaymethodConstant.getPayTextMap());
		return "admin/theatreticket/odiForm.vm";
	}
	//场次价格的设定
	@RequestMapping("/admin/dramaTicket/saveOdiPrice.xhtml")
	public String saveOdiPrice(Long id, Long dpid, Long areaid, Integer price, HttpServletRequest request, ModelMap model) {
		if(areaid == null) return showJsonError(model, "区域编号不能为空！");
		User user = getLogonUser();
		TheatreSeatPrice tsp = null;
		DramaPlayItem item = daoService.getObject(DramaPlayItem.class, dpid);
		TheatreSeatArea area = daoService.getObject(TheatreSeatArea.class, areaid);
		if(area == null) return showJsonError(model, "区域不存在或被删除！");
		if(id!=null){
			tsp=daoService.getObject(TheatreSeatPrice.class, id);
			if(tsp == null) return showJsonError(model, "价格数据不存在或被删除！");
		}else {
			if(!item.hasGewa()){
				return showJsonError(model, "非GEWA场次不能添加价格，请先下载价格！");
			}
			tsp = new TheatreSeatPrice(item.getId(), areaid, "", price, item.getSeller());
			tsp.setDramaid(area.getDramaid());
			List<TheatreSeatPrice> tspList = daoService.getObjectListByField(TheatreSeatPrice.class, "areaid", areaid);
			TspHelper tspHelper = new TspHelper(tspList);
			
			List<String> typeList = OdiConstant.SEATTYPE_LIST; 
			StringBuffer strf = new StringBuffer("");
			for(TheatreSeatPrice ts : tspHelper.getTspListBySno()){
				strf.append(ts.getSeattype()+",");
			}
			for(String s : typeList){
				if(strf.indexOf(s)==-1){
					tsp.setSeattype(s);
					break ;
				}
			}
		}
		Map<String,String> dataMap = WebUtils.getRequestMap(request);
		ChangeEntry changeEntry = new ChangeEntry(tsp);
		BindUtils.bindData(tsp, request.getParameterMap());
		if(tsp.getQuantity()== null || tsp.getQuantity()<0){
			return showJsonError(model, "价格购票，票数不能为空或小于0！");
		}
		String quantity = dataMap.get("quantity");
		if(StringUtils.isBlank(quantity) || Integer.parseInt(quantity)<0){
			return showJsonError(model, "价格购票，票数不能为空或小于0！");
		}
		List<DramaSettle> settleList = daoService.getObjectListByField(DramaSettle.class, "dramaid", item.getDramaid());
		Map<Long,DramaSettle> settleMap = BeanUtil.beanListToMap(settleList, "settleid");
		if(settleMap.get(tsp.getSettleid()) == null) return showJsonError(model, "结算比率错误！");
		//库存数
		Integer quantityInt = Integer.parseInt(quantity);
		tsp.setAllowaddnum(quantityInt);
		//库存数+卖出数=总库存数
		tsp.setQuantity(quantityInt + tsp.getSales());
		daoService.saveObject(item);
		if(!changeEntry.getChangeMap(tsp).isEmpty()){
			dramaPlayItemService.refreshDramaPrice(tsp.getDramaid());
		}
		tsp.setUpdatetime(new Timestamp(System.currentTimeMillis()));
		daoService.saveObject(tsp);
		monitorService.saveChangeLog(getLogonUser().getId(), TheatreSeatPrice.class, tsp.getId(), changeEntry.getChangeMap(tsp));
		dbLogger.warn("用户："+user.getId()+"增加话剧场次价格:"+tsp.getPrice());
		return showJsonSuccess(model, ""+tsp.getId());
	}
	
	@RequestMapping("/admin/dramaTicket/refreshDramaPrice.xhtml")
	public String refreshDramaPrice(Long dramaid, ModelMap model){
		Drama drama = daoService.getObject(Drama.class, dramaid);
		if(drama == null) return showJsonError(model, "项目数据不存在！");
		dramaPlayItemService.refreshDramaPrice(drama.getId());
		return showJsonSuccess(model);
	}
	
	@RequestMapping("/admin/dramaTicket/getOdiPrice.xhtml")
	public String getOdiPrice(Long id, Long areaid, ModelMap model) {
		TheatreSeatArea seatArea = null;
		if(id != null){
			TheatreSeatPrice tsp = daoService.getObject(TheatreSeatPrice.class, id);
			if(tsp == null) return showJsonError(model, "场次价格不存在或被删除！");
			model.put("tsp", tsp);
			seatArea = daoService.getObject(TheatreSeatArea.class, tsp.getAreaid());
		}else{
			seatArea = daoService.getObject(TheatreSeatArea.class, areaid);
		}
		if(seatArea == null) return showJsonError(model, "区域不存在或被删除,不能操作该区的价格！");
		model.put("seatArea", seatArea);
		List<DramaSettle> settleList = daoService.getObjectListByField(DramaSettle.class, "dramaid", seatArea.getDramaid());
		if(settleList.isEmpty()) return showJsonError(model, "请先添加结算比率！如：不知道结算比率，添加100%比率！");
		Map<Long, DramaSettle> settleMap = BeanUtil.beanListToMap(settleList, "settleid");
		model.put("settleMap", settleMap);
		return "admin/theatreticket/areaSeat.vm";
	}
	
	@RequestMapping("/admin/dramaTicket/setOdiPriceStatus.xhtml")
	public String setOdiPriceStatus(Long id, String status, ModelMap model) {
		if(StringUtils.isBlank(status)) return showJsonError_DATAERROR(model);
		User user = getLogonUser();
		TheatreSeatPrice tsp = daoService.getObject(TheatreSeatPrice.class, id);
		ErrorCode code = openDramaService.updateTheatreSeatPrice(tsp, status, DateUtil.getCurFullTimestamp(), user.getId());
		if(!code.isSuccess()){
			return showJsonError(model, code.getMsg());
		}
		dramaPlayItemService.refreshDramaPrice(tsp.getDramaid());
		return showJsonSuccess(model);
	}
	
	@RequestMapping("/admin/dramaTicket/setShowPrice.xhtml")
	public String setShowPrice(Long id, String showprice, ModelMap model) {
		if(StringUtils.isBlank(showprice)) return showJsonError_DATAERROR(model);
		User user = getLogonUser();
		TheatreSeatPrice tsp = daoService.getObject(TheatreSeatPrice.class, id);
		if(tsp == null) return showJsonError(model, "价格不存在或被删除！");
		ChangeEntry changeEntry = new ChangeEntry(tsp);
		tsp.setShowprice(showprice);
		tsp.setUpdatetime(DateUtil.getCurFullTimestamp());
		daoService.saveObject(tsp);
		monitorService.saveChangeLog(user.getId(), TheatreSeatPrice.class, tsp.getId(), changeEntry.getChangeMap(tsp));
		return showJsonSuccess(model);
	}
	
	@RequestMapping("/admin/dramaTicket/setAreaStatus.xhtml")
	public String setAreaaStatus(Long id, String status, ModelMap model) {
		if(StringUtils.isBlank(status)) return showJsonError_DATAERROR(model);
		User user = getLogonUser();
		TheatreSeatArea area = daoService.getObject(TheatreSeatArea.class, id);
		dbLogger.warn("用户：" + user.getId() + "修改区域状态:" + status);
		ChangeEntry changeEntry = new ChangeEntry(area);
		area.setUpdatetime(DateUtil.getCurFullTimestamp());
		area.setStatus(status);
		daoService.saveObject(area);
		monitorService.saveChangeLog(user.getId(), TheatreSeatArea.class, area.getId(),changeEntry.getChangeMap( area));
		return showJsonSuccess(model);
	}
	@RequestMapping("/admin/dramaTicket/delOdi.xhtml")
	public String delOdi(Long itemid, ModelMap model) {
		ErrorCode code = openDramaService.removeOpenDramaItem(itemid);
		if(!code.isSuccess()) return showJsonError(model, code.getMsg());
		List<TheatreSeatPrice> tspList = dramaPlayItemService.getTspList(itemid);
		List<TheatreSeatPrice> delList  = new ArrayList();
		for(TheatreSeatPrice seatprice : tspList){
			seatprice.setUpdatetime(new Timestamp(System.currentTimeMillis()));
			seatprice.setStatus(Status.DEL);
			delList.add(seatprice);
		}
		daoService.saveObjectList(delList);
		return showJsonSuccess(model);
	}
	@RequestMapping("/admin/dramaTicket/getDiscount.xhtml")
	public String getDiscount(Long tspid, ModelMap model) {
		TheatreSeatPrice tsp = daoService.getObject(TheatreSeatPrice.class, tspid);
		List<DisQuantity> disList = dramaPlayItemService.getDisQuantityList(tspid);
		DisQuanHelper disHelper = new DisQuanHelper(disList);
		model.put("tsp", tsp);
		model.put("disHelper", disHelper);
		TheatreSeatArea seatArea = daoService.getObject(TheatreSeatArea.class, tsp.getAreaid());
		List<DramaSettle> settleList = daoService.getObjectListByField(DramaSettle.class, "dramaid", seatArea.getDramaid());
		model.put("settleMap", BeanUtil.beanListToMap(settleList, "settleid"));
		return "admin/theatreticket/disList.vm";
	}
	
	@RequestMapping("/admin/dramaTicket/saveDiscount.xhtml")
	public String getDiscount(DisQuantityCommand command, ModelMap model) {
		if(command.getPrice() == null || command.getPrice()<=0) return showJsonError(model, "保存错误：" + command.getPrice() + "<=0");
		if(command.getAllownum() == null || command.getAllownum()<0) return showJsonError(model, "数量有问题！");
		TheatreSeatPrice seatPrice = daoService.getObject(TheatreSeatPrice.class, command.getTspid());
		if(seatPrice == null) return showJsonError(model, "价格不存在或被删除！");
		if(StringUtils.isBlank(command.getDistype())) return showJsonError(model, "请设置优惠类型");
		if(command.getMaxbuy() == null) return showJsonError(model, "单次最大购票数不能为空！");
		SettleConfig settleConfig = daoService.getObject(SettleConfig.class, command.getSettleid());
		if(settleConfig == null) return showJsonError(model, "结算方式不能为空！");
		if(seatPrice.hasRetail() && StringUtils.isBlank(command.getRetail())) return showJsonError(model, "套票销售方式不能为空！");
		List<DisQuantity> disquanList = dramaPlayItemService.getDisQuantityList(command.getTspid());
		DisQuanHelper disHelper = new DisQuanHelper(disquanList);
		DisQuantity discount = disHelper.getDisByQuantity(command.getQuantity());
		ChangeEntry changeEntry = new ChangeEntry(discount);
		DramaPlayItem item = daoService.getObject(DramaPlayItem.class, seatPrice.getDpid());
		if(discount==null){
			discount = new DisQuantity(seatPrice, command.getQuantity(), command.getDistype());
			discount.setEndtime(item.getEndtime());
		}else {
			discount.setUpdatetime(DateUtil.getCurFullTimestamp());
		}
		discount.setPrice(command.getPrice());
		discount.setCostprice(command.getCostprice());
		discount.setTheatreprice(command.getTheatreprice());
		if(!seatPrice.hasRetail()){
			discount.setRetail(Status.N);
		}else{
			discount.setRetail(command.getRetail());
			
		}
		if(!discount.hasRetail() && seatPrice.hasRetail()){
			seatPrice.setRetail(discount.getRetail());
			seatPrice.setUpdatetime(DateUtil.getCurFullTimestamp());
			daoService.saveObject(seatPrice);
		}
		discount.setSettleid(settleConfig.getId());
		discount.setMaxbuy(command.getMaxbuy());
		discount.setDistype(command.getDistype());
		discount.setAllownum(command.getAllownum());
		discount.setTickettotal(command.getAllownum() + discount.getSellordernum());
		daoService.saveObject(discount);
		monitorService.saveChangeLog(getLogonUser().getId(), DisQuantity.class, discount.getId(),changeEntry.getChangeMap( discount));
		return showJsonSuccess(model, discount.getId() + "");
	}
	@RequestMapping("/admin/dramaTicket/setDiscountStatus.xhtml")
	public String getDiscount(Long discountid, String status, ModelMap model) {
		if(StringUtils.isBlank(status)) return showJsonError(model, "状态不能为空！");
		DisQuantity discount = daoService.getObject(DisQuantity.class, discountid);
		if(discount==null){
			return showJsonError(model, "数据不存在！");
		}
		ChangeEntry changeEntry = new ChangeEntry(discount);
		discount.setStatus(status);
		discount.setUpdatetime(DateUtil.getCurFullTimestamp());
		daoService.saveObject(discount);
		monitorService.saveChangeLog(getLogonUser().getId(), DisQuantity.class, discount.getId(), changeEntry.getChangeMap(discount));
		return showJsonSuccess(model);
	}
	private String checkpaymethodlist(String paymethodlist){
		if(StringUtils.equals(paymethodlist, ",")) paymethodlist = "";
		return VmUtils.printList(Arrays.asList(StringUtils.split(paymethodlist, ",")));
	}
	//保存场次
	@RequestMapping("/admin/dramaTicket/saveOdiOther.xhtml")
	public String saveOtt(Long itemid, String payoption, String paymethodlist, String defaultpaymethod, 
			String cardoption, String batchidlist, HttpServletRequest request, ModelMap model) {
		OpenDramaItem odi = daoService.getObjectByUkey(OpenDramaItem.class, "dpid", itemid, false);
		BindUtils.bindData(odi, request.getParameterMap());
		daoService.saveObject(odi);
		odi = daoService.getObjectByUkey(OpenDramaItem.class, "dpid", itemid, false);
		Map<String, String> otherinfo = VmUtils.readJsonToMap(odi.getOtherinfo());
		if(StringUtils.equals(paymethodlist, ",")) paymethodlist = "";
		if(StringUtils.equals(payoption, "del")) {
			otherinfo.remove(OpiConstant.PAYOPTION);
			otherinfo.remove(OpiConstant.PAYCMETHODLIST);
			otherinfo.remove(OpiConstant.DEFAULTPAYMETHOD);
		}else if(StringUtils.isNotBlank(payoption)){
			otherinfo.put(OpiConstant.PAYOPTION, payoption);
			if(StringUtils.isNotBlank(paymethodlist)) { 
				paymethodlist = checkpaymethodlist(paymethodlist);
				List<String> paymethodList = Arrays.asList(StringUtils.split(paymethodlist, ","));
				if(StringUtils.isBlank(defaultpaymethod) && paymethodList.size()!=1) return showJsonError(model, "请选择默认支付方式");
				
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
		
		odi.setOtherinfo(JsonUtils.writeMapToJson(otherinfo));
		daoService.saveObject(odi);
		return showJsonSuccess(model);
	}
	
	@RequestMapping("/admin/dramaTicket/batAddDpi.xhtml")
	public String batAddDpi(Long itemid, ModelMap model){
		OpenDramaItem item = daoService.getObjectByUkey(OpenDramaItem.class, "dpid", itemid, false);
		String qry = "from DramaPlayItem d where d.theatreid=? and d.dramaid=? and (d.playtime>? and o.period=? or o.endtime>? and o.period=? ) and not exists(select o.id from OpenDramaItem o where o.dpid=d.id)";
		List<DramaPlayItem> playList = hibernateTemplate.find(qry, item.getTheatreid(), item.getDramaid(), item.getPlaytime(), Status.Y, item.getPlaytime(), Status.N);
		int i = 0;
		for(DramaPlayItem play : playList){
			ErrorCode code;
			try {
				code = odiOpenService.saveOpenDramaItem(getLogonUser().getId(), play.getId());
				if(code.isSuccess()){
					i++;
				}
			} catch (OrderException e) {
				e.printStackTrace();
			}
			
		}
		return showJsonSuccess(model, "更新"+i+"个数据");
	}
	
	@RequestMapping("/admin/dramaTicket/batchAddPriceSeat.xhtml")
	public String batchAddPriceSeat(Long itemid, Long areaid, String seattype, String rankno, String lineno, ModelMap model){
		rankno = StringUtils.replaceOnce(rankno, ",", "");
		lineno = StringUtils.replaceOnce(lineno, ",", "");
		User user = getLogonUser();
		ErrorCode code = openDramaService.batchAddPriceSeat(itemid, areaid, seattype, rankno, lineno, user.getId());
		if(!code.isSuccess()) return showJsonError(model, code.getMsg());
		return showJsonSuccess(model);
	}
	
	@RequestMapping("/admin/dramaTicket/toChangeSeat.xhtml")
	public String toChangeSeat(String tradeNo, ModelMap model){
		if(StringUtils.isNotBlank(tradeNo)){
			DramaOrder order = daoService.getObjectByUkey(DramaOrder.class, "tradeNo", tradeNo, false);
			model.put("order", order);
		}
		return "admin/theatreticket/changeSeat.vm";
	}

	@RequestMapping("/admin/ticket/setAllStatus.xhtml")
	public String setAllString(Long areaid,String status,ModelMap model){
		List<TheatreSeatPrice> tspList = daoService.getObjectListByField(TheatreSeatPrice.class, "areaid", areaid);
		if(tspList.isEmpty()) return showJsonError(model, "没有更新的数据！");
		Timestamp updatetime = DateUtil.getCurFullTimestamp();
		for(TheatreSeatPrice tsp : tspList){
			openDramaService.updateTheatreSeatPrice(tsp, status, updatetime, getLogonUser().getId());
		}
		return showJsonSuccess(model);
	}
	
	@RequestMapping("/admin/dramaTicket/delDpiList.xhtml")
	public String delDpiList(Long tid, ModelMap model){
		Timestamp curtime = DateUtil.getMillTimestamp();
		String hql = "from DramaPlayItem d where d.theatreid=? and d.playtime>? and status=? order by playtime";
		List<DramaPlayItem> dpiList = hibernateTemplate.find(hql, tid, DateUtil.addDay(curtime, -200), Status.N);
		model.put("dpiList", dpiList);
		return "admin/theatreticket/delDpiList.vm";
	}
	
	@RequestMapping("/admin/dramaTicket/reDelDpi.xhtml")
	public String reDelDpi(Long dpid, ModelMap model){
		DramaPlayItem dpi = daoService.getObject(DramaPlayItem.class, dpid);
		dpi.setStatus(Status.Y);
		daoService.saveObject(dpi);
		OpenDramaItem odi = daoService.getObjectByUkey(OpenDramaItem.class, "dpid", dpid);
		if(odi!=null){
			odi.setStatus(Status.N);
			daoService.saveObject(odi);
		}
		return showJsonSuccess(model);
	}

	@RequestMapping("/admin/dramaTicket/odiUnGewaOther.xhtml")
	public String odiOther(Long itemid, String type, String ungewa, ModelMap model){
		OpenDramaItem odi = daoService.getObjectByUkey(OpenDramaItem.class, "dpid", itemid);
		if(odi == null) return showJsonError(model, "场次不存在，或被删除！");
		ChangeEntry changeEntry = new ChangeEntry(odi);
		String unGewaKey = OdiConstant.UNSHOWGEWA;
		Map<String,String> odiOtherInfo = JsonUtils.readJsonToMap(odi.getOtherinfo());
		if(StringUtils.equals(type, "open")){
			unGewaKey = OdiConstant.UNOPENGEWA;
		}
		if(StringUtils.equals(ungewa, "true")){
			odiOtherInfo.put(unGewaKey, ungewa);
		}else{
			odiOtherInfo.remove(unGewaKey);
		}
		odi.setOtherinfo(JsonUtils.writeMapToJson(odiOtherInfo));
		daoService.saveObject(odi);
		monitorService.saveChangeLog(getLogonUser().getId(), OpenDramaItem.class, odi.getId(), changeEntry.getChangeMap(odi));
		return showJsonSuccess(model);
	}
	
	@RequestMapping("/admin/dramaTicket/dramaOtherinfo.xhtml")
	public String dramaOtherinfo(Long dramaid, ModelMap model){
		Drama drama = daoService.getObject(Drama.class, dramaid);
		if(drama == null) return showJsonError_NOT_FOUND(model);
		dpiManageService.refreshDramaOtherinfo(getLogonUser().getId(), drama);
		return showJsonSuccess(model);
	}
	
	@RequestMapping("/admin/dramaTicket/batchDramaOtherInfo.xhtml")
	public String batchDramaOtherInfo(String status, ModelMap model){
		odiOpenService.refreshDramaList(getLogonUser().getId(), status);
		return showJsonSuccess(model);
	}
	@RequestMapping("/admin/dramaTicket/getLocUserList.xhtml")
	public String getLocUserList(ModelMap model){
		List<CooperUser> userList = daoService.getObjectListByField(CooperUser.class, "tag", TagConstant.TAG_THEATRE);
		model.put("userList", userList);
		return "partner/admin/locUserList.vm";
	}
	@RequestMapping("/admin/dramaTicket/getLocUser.xhtml")
	public String getLocUserList(Long id, ModelMap model){
		if(id!=null){
			CooperUser user = daoService.getObject(CooperUser.class, id);
			model.put("user", user);
		}
		return "partner/admin/locUser.vm";
	}
	@RequestMapping("/admin/dramaTicket/addLocTicketUser.xhtml")
	public String addLocTicketUser(Long id, String pass, ModelMap model, HttpServletRequest request){
		CooperUser user = null;
		boolean isadd = false;
		if(id!=null){
			user = daoService.getObject(CooperUser.class, id);
		}else {
			user = new CooperUser();
			isadd = true;
		}
		BindUtils.bindData(user, request.getParameterMap());
		if(StringUtils.isBlank(user.getRelatedids())){
			return showJsonError(model, "场馆id不能为空！");
		}
		user.setTag(TagConstant.TAG_THEATRE);
		user.setCategory(TagConstant.TAG_DRAMA);
		user.setRoles(CooperUser.ROLR_APIUSER + "," + CooperUser.ROLR_LOCTICKET);
		user.setName(user.getLoginname());
		if(StringUtils.isNotBlank(pass)){
			user.setLoginpass(StringUtil.md5(pass));
		}
		if(isadd){
			Long nid = (long)(Math.random()*1000)+1000L;
			user.setId(nid);
		}
		daoService.saveObject(user);
		return showJsonSuccess(model);
	}
	
	@RequestMapping("/admin/dramaTicket/printDramaOrder.xhtml")
	public void dramaOrder(Long orderid, String xls, HttpServletResponse response, ModelMap model){
		DramaOrder dramaOrder = daoService.getObject(DramaOrder.class, orderid);
		User user = getLogonUser();
		if(dramaOrder == null) return; //return show404(model, "订单不存在！");
		model.put("dramaOrder", dramaOrder);
		OrderAddress orderAddress = daoService.getObject(OrderAddress.class, dramaOrder.getTradeNo());
		model.put("orderAddress", orderAddress);
		List<BuyItem> itemList = daoService.getObjectListByField(BuyItem.class, "orderid", dramaOrder.getId());
		List<Long> theatreIdList = BeanUtil.getBeanPropertyList(itemList, "placeid", true);
		Map<Long,Theatre> theatreMap = daoService.getObjectMap(Theatre.class, theatreIdList);
		model.put("theatreMap", theatreMap);
		List<Long> dramaIdList = BeanUtil.getBeanPropertyList(itemList, "itemid", true);
		Map<Long,Drama> dramaMap = daoService.getObjectMap(Drama.class, dramaIdList);
		List<Long> odiIdList = BeanUtil.getBeanPropertyList(itemList, "relatedid", true);
		Map<Long,DramaPlayItem> itemMap = daoService.getObjectMap(DramaPlayItem.class, odiIdList);
		model.put("itemMap", itemMap);
		model.put("dramaMap", dramaMap);
		model.put("itemList", itemList);
		List<SellDramaSeat> sellSeatList = dramaOrderService.getDramaOrderSeatList(dramaOrder.getId());
		Map<String,SellDramaSeat> sellSeatMap = BeanUtil.beanListToMap(sellSeatList, "key");
		model.put("sellSeatMap", sellSeatMap);
		String result = velocityTemplate.parseTemplate("drama/dramaOrder.vm", model);	
		dramaOrderService.updateOrderExpress(dramaOrder.getTradeNo(), null, OrderExtraConstant.EXPRESS_STATUS_PRINT, user, OrderExtraConstant.DEAL_TYPE_BACKEND);
		downloadPdf(xls, response, result);
		//return "admin/drama/dramaOrder.vm";
	}
	
	protected void downloadPdf(String xls, HttpServletResponse response, String result){
		try{
			ServletOutputStream fos = response.getOutputStream();
			if(StringUtils.isNotBlank(result)){
				if(StringUtils.equals("pdf", xls)){
					StringReader reader = new StringReader(result);
					try{
						response.setContentType("application/pdf");
						response.addHeader("Content-Disposition", "attachment;filename=gewara"+DateUtil.format(new Date(), "yyMMdd_HHmmss") + ".pdf");
				        PD4ML pd4ml = new PD4ML();  
				        pd4ml.setPageSize(PD4Constants.A5); 
				        pd4ml.setPageInsets(new Insets(20, 0, 10, 10));  
				        pd4ml.setHtmlWidth(1000);
				        pd4ml.setPageSize(pd4ml.changePageOrientation(PD4Constants.A5));  
				        pd4ml.useTTF("java:fonts", true);  
				        pd4ml.setDefaultTTFs("SimHei", "Arial", "Courier New");  
				        //pd4ml.enableDebugInfo();
				        pd4ml.render(reader, fos);
					}finally{
						reader.close();
					}
				}else if(StringUtils.equals("xls", xls)){
					downloadXls(xls, response);
					fos.write(result.getBytes("utf-8"));
				}
			}
			fos.flush();
			fos.close();
		}catch (Exception e) {
			dbLogger.warn("", e);
		}
	}
	
	@RequestMapping("/admin/dramaTicket/printBatchDramaOrder.xhtml")
	public void batchDramaOrder(String orderids, HttpServletResponse response, ModelMap model){
		String[] orderIdStrs = orderids.split(",");
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
		User user = getLogonUser();
		for (int i = 0; i < orderIdStrs.length; i++) {
			Map<String, Object> orderMap = new HashMap<String, Object>();
			Long orderid = Long.valueOf(orderIdStrs[i]);
			DramaOrder dramaOrder = daoService.getObject(DramaOrder.class, orderid);
			if(dramaOrder == null) return;
			orderMap.put("dramaOrder", dramaOrder);
			OrderAddress orderAddress = daoService.getObject(OrderAddress.class, dramaOrder.getTradeNo());
			orderMap.put("orderAddress", orderAddress);
			List<BuyItem> itemList = daoService.getObjectListByField(BuyItem.class, "orderid", dramaOrder.getId());
			List<Long> theatreIdList = BeanUtil.getBeanPropertyList(itemList, "placeid", true);
			Map<Long,Theatre> theatreMap = daoService.getObjectMap(Theatre.class, theatreIdList);
			orderMap.put("theatreMap", theatreMap);
			List<Long> dramaIdList = BeanUtil.getBeanPropertyList(itemList, "itemid", true);
			Map<Long,Drama> dramaMap = daoService.getObjectMap(Drama.class, dramaIdList);
			List<Long> odiIdList = BeanUtil.getBeanPropertyList(itemList, "relatedid", true);
			Map<Long,DramaPlayItem> itemMap = daoService.getObjectMap(DramaPlayItem.class, odiIdList);
			orderMap.put("itemMap", itemMap);
			orderMap.put("dramaMap", dramaMap);
			orderMap.put("itemList", itemList);
			List<SellDramaSeat> sellSeatList = dramaOrderService.getDramaOrderSeatList(dramaOrder.getId());
			Map<String,SellDramaSeat> sellSeatMap = BeanUtil.beanListToMap(sellSeatList, "key");
			orderMap.put("sellSeatMap", sellSeatMap);
			dramaOrderService.updateOrderExpress(dramaOrder.getTradeNo(), null, OrderExtraConstant.EXPRESS_STATUS_PRINT, user, OrderExtraConstant.DEAL_TYPE_BACKEND);
			list.add(orderMap);
		}
		model.put("orderList", list);
		String result = velocityTemplate.parseTemplate("drama/batchDramaOrder.vm", model);	
		downloadPdf("pdf", response, result);
//		return "admin/drama/batchDramaOrder.vm";
	}
}
