package com.gewara.web.action.api2machine;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.support.PropertyComparator;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.gewara.Config;
import com.gewara.constant.ApiConstant;
import com.gewara.constant.GoodsConstant;
import com.gewara.constant.Status;
import com.gewara.constant.TagConstant;
import com.gewara.constant.sys.LogTypeConstant;
import com.gewara.constant.sys.MongoData;
import com.gewara.constant.ticket.OrderConstant;
import com.gewara.constant.ticket.OrderNoteConstant;
import com.gewara.json.SportUpGrade;
import com.gewara.json.machine.MachineAd;
import com.gewara.model.api.ApiUser;
import com.gewara.model.api.OrderResult;
import com.gewara.model.api.Synch;
import com.gewara.model.api.SynchConfig;
import com.gewara.model.common.BaseEntity;
import com.gewara.model.drama.DramaOrder;
import com.gewara.model.drama.Theatre;
import com.gewara.model.goods.BaseGoods;
import com.gewara.model.goods.Goods;
import com.gewara.model.machine.TakeTicket;
import com.gewara.model.movie.Cinema;
import com.gewara.model.pay.GewaOrder;
import com.gewara.model.pay.GoodsOrder;
import com.gewara.model.pay.OrderNote;
import com.gewara.model.pay.SportOrder;
import com.gewara.model.pay.TicketOrder;
import com.gewara.model.sport.Sport;
import com.gewara.service.SynchService;
import com.gewara.util.ApiUtils;
import com.gewara.util.BeanUtil;
import com.gewara.util.DateUtil;
import com.gewara.util.StringUtil;
import com.gewara.web.action.api.ApiAuth;
import com.gewara.web.filter.NewApiAuthenticationFilter;
import com.gewara.xmlbind.api.OrderResponse;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.QueryOperators;
@Controller
public class MachineCommonController extends BaseMachineApiController{
	@Autowired@Qualifier("synchService")
	private SynchService synchService;
	/**
	 * 1、订单成功下载到本地后同步通知接口
	 * 2、当用户取完票的时候，也调用这个接口
	 * @param xml
	 * @param sportId
	 * @param model
	 * @return
	 */
	@RequestMapping("/apimac/common/ticketStatus.xhtml")
	public String ticketStatus(String xml, String sportId, ModelMap model){
		if(StringUtils.isBlank(xml)) return getErrorXmlView(model, ApiConstant.CODE_PARAM_ERROR, "参数错误");
		if(Config.isDebugEnabled()){
			dbLogger.warnWithType(LogTypeConstant.LOG_TYPE_API, "XML:"+xml);
		}
		OrderResponse result = (OrderResponse) ApiUtils.xml2Object(ApiUtils.getBeanReader("ticketstatus",OrderResponse.class), xml);
		List<OrderResult> resultList = new ArrayList<OrderResult>();
		for(OrderResult orderResult:result.getOrderList()){
			if(StringUtils.isBlank(orderResult.getTradeno())){ 
				continue;
			}
			if(orderResult.getTaketime() != null) {
				orderResult.setIstake("Y");
			}
			GewaOrder order = daoService.getObjectByUkey(GewaOrder.class, "tradeNo", StringUtils.trim(orderResult.getTradeno()), false);
			if(order != null) {
				if(order instanceof TicketOrder) {
					orderResult.setOrdertype(OrderResult.ORDERTYPE_TICKET);
					orderResult.setPlaceid(((TicketOrder) order).getCinemaid());
				}else if(order instanceof GoodsOrder){
					orderResult.setOrdertype(OrderResult.ORDERTYPE_MEAL);
					orderResult.setPlaceid(((GoodsOrder) order).getPlaceid());
				}else if(order instanceof DramaOrder){
					orderResult.setOrdertype(OrderResult.ORDERTYPE_DRAMA);
				}else if(order instanceof SportOrder || StringUtils.isNotBlank(sportId)){
					orderResult.setOrdertype(OrderResult.ORDERTYPE_SPORT);
					orderResult.setCaption(sportId);
				}
				orderResult.setTradeno(StringUtils.trim(orderResult.getTradeno()));
				orderResult.setTicketnum(order.getQuantity());
			}
			if(StringUtils.isNotBlank(sportId)){
				orderResult.setOrdertype(OrderResult.ORDERTYPE_SPORT);
				orderResult.setCaption(sportId);
			}
			orderResult.setUpdatetime(new Timestamp(System.currentTimeMillis()));
			OrderNote onm = daoService.getObjectByUkey(OrderNote.class, "serialno", StringUtils.trim(orderResult.getTradeno()), false);
			if(onm!=null){
				if(orderResult.getTaketime() != null) { 
					onm.setTaketime(orderResult.getTaketime());
					onm.setResult(Status.Y);
				}else {
					onm.setResult(OrderNoteConstant.RESULT_S);
				}
				daoService.saveObject(onm);
			}
			resultList.add(orderResult);
			
		}
		daoService.saveObjectList(resultList);
		Timestamp cur = new Timestamp(System.currentTimeMillis());
		return getSingleResultXmlView(model, DateUtil.formatTimestamp(cur));
	}

	/**
	 * 一体机回传同步时间,如果传递的xml有异常，同步时间不变
	 * @param cinemaid
	 * @param successtime
	 * @param isTakeTradeNo
	 * @param mctype
	 * @param model
	 * @return
	 */
	@RequestMapping("/apimac/common/synchReport.xhtml")
	public String synchReport(Long venueId, Timestamp successtime, String mctype, String macid, ModelMap model){
		Synch synch = daoService.getObject(Synch.class, venueId);
		if(StringUtils.equals(mctype, "gewara")){//测试
			apiService.saveSynchWithCinema(synch, null, null, null, null);
		}else{
			apiService.saveSynchWithCinema(synch, null, successtime, null, null);
		}
		if(StringUtils.isNotBlank(macid)){
			getMachineSynch(venueId, null, macid, successtime);
		}
		return getSingleResultXmlView(model, DateUtil.formatTimestamp(successtime));
	}
	
	/**
	 * 一体机动态获取同步的配置信息，比如获取下载订单的接口等等
	 * @param tag
	 * @param model
	 * @return
	 */
	@RequestMapping("/apimac/common/synchConfig.xhtml")
	public String synchConfig(String tag, ModelMap model){
		ApiAuth auth = NewApiAuthenticationFilter.getApiAuth();
		ApiUser apiUser = auth.getApiUser();
		if(StringUtils.isBlank(tag))tag = Synch.TGA_CINEMA;
		List<SynchConfig> synchConfigList = daoService.getAllObjects(SynchConfig.class);
		Map<String, String> passwordconfig = new HashMap<String, String>();
		for (SynchConfig synchConfig:synchConfigList) {
			if(StringUtils.contains(synchConfig.getTtype(), "password"))
				passwordconfig.put(synchConfig.getTtype(), StringUtil.md5(synchConfig.getTvalue()+apiUser.getPrivatekey()));
		}
		model.put("passwordconfig", passwordconfig);
		model.put("synchConfigList", synchConfigList);
		return getXmlView(model, "api2machine/synchConfig.vm");
	}
	/**
	 * 获得一体机关机时间
	 * @param key
	 * @param encryptCode
	 * @param venueId
	 * @param model
	 * @return
	 */
	@RequestMapping("/apimac/common/getMachineShutDownTime.xhtml")
	public String getMachineConfig(Long venueId, ModelMap model){
		//身份校验
		String finalShutDownTime=null;//返回关机时间 yyyy-MM-dd HH:mm:ss
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("venueId", venueId);
		List<Map> map = mongoService.find(MongoData.NS_MACHINECONFIG, params);
		if(map==null||(map.size()==0)){
			return getErrorXmlView(model, ApiConstant.CODE_DATA_ERROR, "关机时间配置不存在");
		}
		//获取配置
		Map cfg=map.get(0);
		String defTime=(String)cfg.get("defShutDownTime");//默认关机时间 HH:mm
		Integer unitTime=(Integer)cfg.get("unitTime");//单位时间
		
		//校验配置
		if(StringUtils.isBlank(defTime)){
			return getErrorXmlView(model, ApiConstant.CODE_DATA_ERROR, "没有配置默认关机时间");
		}
		//格式校验
		SimpleDateFormat sdfHM=new SimpleDateFormat("HH:mm");
		try {
			sdfHM.parse(defTime);
		} catch (ParseException e) {
			return getErrorXmlView(model, ApiConstant.CODE_DATA_ERROR, "默认关机时间格式配置错误(正确格式HH:mm)");
		}
		if(unitTime==null){
			return getErrorXmlView(model, ApiConstant.CODE_DATA_ERROR, "没有配置单位时间");
		}
		
		//组装默认关机时间
		SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd");
		SimpleDateFormat sdfFull=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String defShutDownDate=sdf.format(new Date())+" "+defTime+":00";//yyyy-MM-dd HH:mm:ss
		Date defShutDownTime=null;
		try {
			defShutDownTime = sdfFull.parse(defShutDownDate);
		} catch (ParseException e) {
			return getErrorXmlView(model, ApiConstant.CODE_DATA_ERROR, "默认关机时间格式配置错误(正确格式HH:mm)");
		}
		
		//查询当日18点至次日6点间最晚场次时间
		Timestamp curtime = new Timestamp(System.currentTimeMillis());
		int hour = DateUtil.getHour(curtime);
		if(hour>=0 && hour <=5){
			return getErrorXmlView(model, ApiConstant.CODE_DATA_ERROR, "凌晨到5点不做同步");
		}
		Timestamp begtime = DateUtil.getBeginningTimeOfDay(curtime);
		Timestamp startTime = DateUtil.addHour(begtime, 18);
		Timestamp endTime = DateUtil.addHour(startTime, 12);
		Timestamp lastPlayTime=null;
		String hql="select new map(max(o.playtime) as lasttime ) from OpenPlayItem o where o.cinemaid=? and o.playtime>=? and o.playtime<=? ";
		List<Map<String, Object>> resultList=hibernateTemplate.find(hql, venueId, startTime, endTime);
		if(resultList!=null&&resultList.size()>0){
			lastPlayTime=(Timestamp) resultList.get(0).get("lasttime");
		}
		if(null!=lastPlayTime){//查询关机时间=当日18点至次日6点间最晚场次时间+单位时间
			//关机时间
			Timestamp tempTime = DateUtil.addMinute(lastPlayTime, unitTime);
			//如果关机时间小于默认自动关机时间，则取默认自动关机时间给终端机
			if(tempTime.before(defShutDownTime)){
				finalShutDownTime=defShutDownDate;
			}else{
				finalShutDownTime=sdfFull.format(tempTime);
			}
		}else{//当日没有场次,关机时间取默认关机时间
			finalShutDownTime=defShutDownDate;
		}
		return getSingleResultXmlView(model, finalShutDownTime);
	}
	
	/**
	 * 一体机和pos机应用升级接口
	 * @param key
	 * @param encryptCode
	 * @param apptype 应用类型 可选值:SPORT_ITS(智能终端系统),SPORT_BOOKING(羽毛球在线预订系统)
	 * @param model
	 * @return
	 */
	@RequestMapping("/apimac/common/getUpGradeFile.xhtml")
	public String getUpGradeFile(@RequestParam(required=false,value="apptype",defaultValue=SportUpGrade.SPORT_APP_BOOKING) String apptype,
			ModelMap model){
		DBObject params=new BasicDBObject();
		params.put("apptype", apptype.trim());
		List<SportUpGrade> upGradeList=mongoService.getObjectList(SportUpGrade.class, params, "addTime", false, 0, 1);
		model.put("upGrade", upGradeList.isEmpty() ? null : upGradeList.get(0));
		return getXmlView(model, "api2machine/upGrade.vm");
	}
	
	/**
	 * pos/pad设备状态跟踪
	 * @param key
	 * @param encryptCode
	 * @param equipmentid
	 * @param sportid
	 * @param relatedid
	 * @param type
	 * @param model
	 * @return
	 */
	@RequestMapping("/apimac/common/equipmentStatus.xhtml")
	public String equipmentStatus(
			String equipmentid,
			@RequestParam(required=false,value="equipmentType",defaultValue="pos") String equipmentType,
			Long venueId,
			String type,
			String appversion,
			ModelMap model){
		if(StringUtils.isBlank(equipmentid))return getErrorXmlView(model, ApiConstant.CODE_PARAM_ERROR, "设备id不能为空！");
		Map<String, Object> paramMap = new HashMap<String, Object>();
		paramMap.put(MongoData.SYSTEM_ID, equipmentid);
		Map<String, Object> map = this.mongoService.findOne(MongoData.SYSTEM_ID, paramMap);
		if(map == null){
			map = new HashMap<String, Object>();
			map.put(MongoData.SYSTEM_ID, equipmentid);
		}
		BaseEntity be = null;
		String placeName = "场馆id错误";
		if(TagConstant.TAG_SPORT.equals(type)){
			be = this.daoService.getObject(Sport.class, venueId);
			placeName = (String) BeanUtil.get(be, "name");
		}else if(TagConstant.TAG_DRAMA.equals(type)){
			be = this.daoService.getObject(Theatre.class, venueId);
			placeName = (String) BeanUtil.get(be, "name");
		}else if(TagConstant.TAG_CINEMA.equals(type)){
			be = this.daoService.getObject(Cinema.class, venueId);
			placeName = (String) BeanUtil.get(be, "name");
		}
		map.put("id", equipmentid);
		map.put("venueId", venueId);
		map.put("type", type);
		map.put("equipmentType", equipmentType);
		map.put("appversion", appversion);
		map.put("sportName", placeName);
		map.put("synchTime", DateUtil.formatTimestamp(DateUtil.getMillTimestamp()));
		this.mongoService.saveOrUpdateMap(map, MongoData.SYSTEM_ID, MongoData.NS_EQUIPMENTSTATUS);
		model.put("result", "success");
		return  getXmlView(model, "api2machine/result.vm");
	}
	
	/**
	 * 一体机和pos广告接口
	 * @param adversion
	 * @param venueid
	 * @param model
	 * @return
	 */
	@RequestMapping("/apimac/common/getMachineAdList.xhtml")
	public String getMachineAdList(Long venueid, ModelMap model){
		if(venueid==null)return getErrorXmlView(model, ApiConstant.CODE_PARAM_ERROR, "场馆id不能为空！");
		List<MachineAd> adList = new ArrayList<MachineAd>();
		Timestamp curtime = new Timestamp(System.currentTimeMillis());
		DBObject query = mongoService.queryAdvancedDBObject("startTime", new String[]{"<="}, 
				new Object[]{DateUtil.formatTimestamp(curtime)});
		query.put("endTime",  new BasicDBObject(QueryOperators.GTE, DateUtil.formatTimestamp(curtime)));
		List<MachineAd> tmpList = mongoService.getObjectList(MachineAd.class, query);
		for(MachineAd ad : tmpList){
			if(StringUtils.isBlank(ad.getVenueid())){
				adList.add(ad);
			}else {
				List<String> idsList = Arrays.asList(ad.getVenueid().split(","));
				if(idsList.contains(venueid+"")) adList.add(ad);
			}
		}
		Collections.sort(adList, new PropertyComparator("addtime", false, false));
		model.put("adList", adList);
		return getXmlView(model, "api2machine/machineAdList.vm");
	}
	/**
	 * 上传报表错误信息
	 * @param cinemaid
	 * @param errorCode
	 * @param remark
	 * @param machineid
	 * @param model
	 * @return
	 */
	@RequestMapping("/apimac/common/reportError.xhtml")
	public String reportError(Long cinemaId,
			String errorType,String remark,String machineId,ModelMap model){
		return saveError(cinemaId, machineId, errorType, remark,"0", model);
	}
	/**
	 * 上传终端机错误信息
	 * @param cinemaid
	 * @param machineId
	 * @param errorType
	 * @param errorMessage
	 * @param model
	 * @return
	 */
	@RequestMapping("/apimac/common/machineError.xhtml")
	public String machineError(Long cinemaId,
			String machineId,String errorType,String errorMessage,ModelMap model){
		return saveError( cinemaId, machineId, errorType, errorMessage,"1", model);
	}
	
	private String saveError(long cinemaId,String machineId,
			String errorType,String errorMessage,String type,ModelMap model){
		if(machineId == null || errorType == null){
			return getErrorXmlView(model, ApiConstant.CODE_PARAM_ERROR, "参数错误");
		}
		Map<String, Object> toSave = new HashMap<String, Object>();
		toSave.put(MongoData.SYSTEM_ID, MongoData.buildId());
		toSave.put("id", cinemaId+System.currentTimeMillis());
		Cinema cinema = this.daoService.getObject(Cinema.class, cinemaId);
		if(cinema == null){
			return getErrorXmlView(model, ApiConstant.CODE_PARAM_ERROR, "cinemaid错误");
		}
		toSave.put("cinemaid", cinemaId);
		toSave.put("name", cinema.getName());
		toSave.put("errorCode", errorType);
		toSave.put("machineid", machineId);
		toSave.put("remark", errorMessage);
		toSave.put("type", type);
		toSave.put("addtime", new Timestamp(System.currentTimeMillis()));
		mongoService.saveOrUpdateMap(toSave, "id", MongoData.NS_TICKET_MACHINE_ERROR, false, true);
		model.put("result", "success");
		return getXmlView(model, "api2machine/result.vm");
	}
	//供一体机查询已经退款的订单，一体机删除本地已经存储的这些订单
	@RequestMapping("/apimac/common/refundOrderList.xhtml")
	public String refundOrderList(Long venueid, String lastdelteds, ModelMap model){
		if(venueid == null){
			return getErrorXmlView(model, ApiConstant.CODE_PARAM_ERROR, "缺少参数venueid");
		}
		//打印一体机上次已经删除的订单
		dbLogger.warn("venueid="+venueid+", lastdelteds=" + lastdelteds);
		Timestamp curtime = DateUtil.getMillTimestamp();
		Timestamp addtime = DateUtil.addDay(curtime, -15);
		Timestamp modifytime = DateUtil.addDay(curtime, -1);
		
		
		String hql = "select tradeno from OrderRefund where tradeno is not null and addtime>? and refundtime>?";
		
		List<String> refundList = hibernateTemplate.find(hql, addtime, modifytime);
		if (refundList.isEmpty()){
			return getSingleResultXmlView(model, StringUtils.join(refundList, ","));
		}
		String tradenoSql = "";
		for (int i = 0; i < refundList.size(); i++) {
			tradenoSql += ",?";
		}
		String sql = "from GewaOrder t where t.addtime>? and t.modifytime>? and t.status=? and t.tradeNo in (" + StringUtils.substring(tradenoSql, 1) + ")";
		List params = new ArrayList();
		params.add(addtime);
		params.add(modifytime);
		params.add(OrderConstant.STATUS_PAID_RETURN);
		params.addAll(refundList);
		List<GewaOrder> orderList = hibernateTemplate.find(sql, params.toArray());
		List<String> tradenoList = getTradeNoList(orderList, venueid);
		return getSingleResultXmlView(model, StringUtils.join(tradenoList, ","));
	}
	//查询传递过来的订单号的取票状态
	@RequestMapping("/apimac/common/qupiaoResult.xhtml")
	public String tradenos(String tradenos, ModelMap model){
		if(StringUtils.isBlank(tradenos)){
			return getErrorXmlView(model, ApiConstant.CODE_PARAM_ERROR, "请输入订单号");
		}
		Set<String> strSet = new HashSet<String>();
		List<String> snos = Arrays.asList(tradenos.split(","));
		List<OrderResult> resList = daoService.getObjectList(OrderResult.class, snos);
		for(OrderResult result : resList){
			if(StringUtils.equals(result.getIstake(), Status.Y)){
				strSet.add(result.getTradeno());
			}
		}
		List<String> onnos = synchService.getSerialnoList(tradenos);
		strSet.addAll(onnos);
		return getSingleResultXmlView(model, StringUtils.join(strSet, ","));
	}
	
	//实时验证取票
	@RequestMapping("/apimac/common/validTakeTicket.xhtml")
	public String tradenos(Long placeid, String checkpass, String macid, ModelMap model){
		if(placeid==null || StringUtils.isBlank(checkpass) || StringUtils.isBlank(macid)){
			return getErrorXmlView(model, ApiConstant.CODE_PARAM_ERROR, "缺少输入参数");
		}
		Timestamp curtime = DateUtil.getMillTimestamp();
		Timestamp addtime = DateUtil.addDay(curtime, -90);
		OrderNote note = getOrderNoteByCheckpass(placeid, addtime, checkpass);
		if(note==null){
			return getErrorXmlView(model, ApiConstant.CODE_DATA_ERROR, "订单不存在，请核实输入信息");
		}
		String tradeno = note.getTradeno();
		TakeTicket tt = validTakeTicket(placeid, macid, tradeno);
		if(tt!=null){
			if(StringUtils.equals(tt.getStatus(), Status.Y)){
				return getErrorXmlView(model, ApiConstant.CODE_DATA_ERROR, "验证失败，不能重复验证");
			}
		}else {
			tt = new TakeTicket();
			tt.setMacid(macid);
			tt.setPlaceid(placeid);
			tt.setTaketime(curtime);
			tt.setTradeno(tradeno);
		}
		tt.setStatus(Status.Y);
		daoService.saveObject(tt);
		model.put("order", daoService.getObjectByUkey(GewaOrder.class, "tradeNo", tradeno));
		return getXmlView(model, "api2machine/takeTicket.vm");
	}
		
	private List<String> getTradeNoList(List<GewaOrder> orderList, Long venueid){
		List<String> tradenoList = new ArrayList<String>();
		for (GewaOrder gewaOrder : orderList) {
			if(gewaOrder instanceof TicketOrder){
				TicketOrder order = (TicketOrder)gewaOrder;
				if(order.getCinemaid().equals(venueid)){
					tradenoList.add(gewaOrder.getTradeNo());
				}
			}else if(gewaOrder instanceof DramaOrder){
				DramaOrder order = (DramaOrder)gewaOrder;
				if(order.getTheatreid().equals(venueid)){
					tradenoList.add(gewaOrder.getTradeNo());
				}
			}else if(gewaOrder instanceof SportOrder){
				SportOrder order = (SportOrder)gewaOrder;
				if(order.getSportid().equals(venueid)){
					tradenoList.add(gewaOrder.getTradeNo());
				}
			}else if(gewaOrder instanceof GoodsOrder){
				GoodsOrder order = (GoodsOrder)gewaOrder;
				BaseGoods goods = daoService.getObject(BaseGoods.class, order.getGoodsid());
				if(goods.getRelatedid()!=null && goods.getRelatedid().equals(venueid)){
					tradenoList.add(gewaOrder.getTradeNo());
				}
			}
		}
		return tradenoList;
	}
	
	@RequestMapping("/apimac/common/downGoodsOrderList.xhtml")
	public String downGoodsOrderList(Long placeid, String tag, ModelMap model){
		Synch synch = daoService.getObject(Synch.class, placeid);
		if(StringUtils.isBlank(tag)) tag = Synch.TGA_CINEMA;
		if(synch == null) {
			Timestamp curtime = DateUtil.getCurFullTimestamp();
			curtime = DateUtil.addDay(curtime, -30);
			synch = new Synch(placeid, Synch.TGA_DRAMA);
			synch.setBarcodesuctime(curtime);
			synch.setBarcodesyntime(curtime);
			synch.setGsuctime(curtime);
			synch.setSuccesstime(curtime);
			synch.setSynchtime(curtime);
			synch.setGsyntime(curtime);
			daoService.saveObject(synch);
		}
		Theatre theatre = daoService.getObject(Theatre.class, placeid);
		List<GoodsOrder> goodsOrderList = synchService.getGoodsOrderListByRelatedidAndLasttime(placeid, DateUtil.addMinute(synch.getGsuctime(), -5),GoodsConstant.GOODS_TAG_BMH_THEATRE);
		return commonGoodsOrderData(theatre, goodsOrderList, model);
	}
	private String commonGoodsOrderData(Theatre theatre, List<GoodsOrder> goodsOrderList, ModelMap model){
		Map<Long, Goods> goodsMap = new HashMap<Long, Goods>();
		ApiAuth auth = NewApiAuthenticationFilter.getApiAuth();
		ApiUser apiUser = auth.getApiUser();
		Map<String, String> checkMap = new HashMap<String, String>();
		Map<String,String> orderMap = new HashMap<String, String>();
		Map<String,String> mobileMap = new HashMap<String, String>();
		for(GoodsOrder order : goodsOrderList){
			if(goodsMap.get(order.getGoodsid())==null) {
				Goods goods = daoService.getObject(Goods.class, order.getGoodsid());
				goodsMap.put(order.getGoodsid(), goods);
			}
			String checkpass = StringUtil.md5(order.getCheckpass() + apiUser.getPrivatekey());
			checkMap.put(order.getCheckpass(), checkpass);
			if(!orderMap.containsKey(order.getTradeNo())){
				OrderResult orderResult = daoService.getObject(OrderResult.class, order.getTradeNo());
				String flag = "0";
				if(orderResult != null) flag = orderResult.getResult();
				if(StringUtils.isBlank(flag)) flag = "0";
				orderMap.put(order.getTradeNo(), flag);
			}
			mobileMap.put(order.getTradeNo(), StringUtils.substring(order.getMobile(), 7));
		}
		model.put("nowtime", DateUtil.getMillTimestamp());
		model.put("goodsOrderList", goodsOrderList);
		model.put("goodsMap", goodsMap);
		model.put("checkMap", checkMap);
		model.put("orderMap", orderMap);
		model.put("cinema", theatre);
		model.put("mobileMap", mobileMap);
		return getXmlView(model, "api2machine/downCinemaOrderList.vm");
	}
	@RequestMapping("/apimac/common/qryGoodsOrderList.xhtml")
	public String qryOrderList(Long placeid, String checkpass, String shortmobile, ModelMap model){
		if(placeid==null || StringUtils.isBlank(checkpass) || StringUtils.isBlank(shortmobile)) {
			return getErrorXmlView(model, ApiConstant.CODE_DATA_ERROR, "缺少参数");
		}
		Theatre theatre = daoService.getObject(Theatre.class, placeid);
		List<GoodsOrder> goodsOrderList = new ArrayList<GoodsOrder>();
		Timestamp addtime = DateUtil.addDay(DateUtil.getMillTimestamp(), -30);
		List<GewaOrder> torderList = getGewaOrderByCheckpass(placeid, addtime, checkpass, shortmobile);
		for(GewaOrder order : torderList){
			if(order instanceof GoodsOrder){
				goodsOrderList.add((GoodsOrder)order);
			}
		}
		return commonGoodsOrderData(theatre, goodsOrderList, model);
	}
	@RequestMapping("/apimac/common/getSysTime.xhtml")
	public String getSysTime(ModelMap model){
		Timestamp curtime = DateUtil.getCurFullTimestamp();
		return getSingleResultXmlView(model, DateUtil.formatTimestamp(curtime));
	}
}
