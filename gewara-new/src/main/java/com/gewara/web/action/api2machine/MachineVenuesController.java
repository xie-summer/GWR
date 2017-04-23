package com.gewara.web.action.api2machine;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.gewara.constant.ApiConstant;
import com.gewara.constant.GoodsConstant;
import com.gewara.constant.TagConstant;
import com.gewara.constant.sys.MongoData;
import com.gewara.model.api.OrderResult;
import com.gewara.model.api.Synch;
import com.gewara.model.common.BaseEntity;
import com.gewara.model.goods.Goods;
import com.gewara.model.movie.Cinema;
import com.gewara.model.pay.GoodsOrder;
import com.gewara.model.pay.SportOrder;
import com.gewara.model.pay.TicketOrder;
import com.gewara.model.sport.OpenTimeTable;
import com.gewara.model.sport.Sport;
import com.gewara.model.ticket.OpenPlayItem;
import com.gewara.service.SynchService;
import com.gewara.util.BeanUtil;
import com.gewara.util.DateUtil;
import com.gewara.util.JsonUtils;
import com.gewara.util.StringUtil;

@Controller
public class MachineVenuesController extends BaseMachineApiController{
	@Autowired@Qualifier("synchService")
	private SynchService synchService;
	/**
	 * 提供给取票终端机下载个gewara订单，目前订单有：<ul>
	 * <li>1 : 场馆订单
	 * <li>2 : 场馆卖品订单
	 * <li>3 : 电影订单
	 * <li>4 : 电影卖品订单
	 * </ul>
	 * @param model spring ui ModelMap
	 * @param key
	 * @param encryptCode
	 * @param recordId 订单记录id
	 * @param tag 订单类型，目前有 sport、sport
	 * @param ticketnum
	 * @return 订单xml模板视图 api/ticket/toSynchronizeOrderList.vm
	 */
	@RequestMapping("/apimac/synch/downGewaOrder.xhtml")
	public String downGewaOrder(ModelMap model, Long relatedid, String tag,String ticketnum,Long sportitemid,
			@RequestParam(defaultValue="0",required=false,value="type")Integer type){
		if(relatedid==null) {
			return getErrorXmlView(model, ApiConstant.CODE_PARAM_ERROR, "传递参数错误！");
		}
		model.put("downOrderSuccesstime", DateUtil.formatTimestamp(new Timestamp(System.currentTimeMillis())));
		/**根据tag值，API调用判断 **/
		model.put("tag", tag);
		if(StringUtils.equals(Synch.TAG_SPORT,tag)){
			return downSportOrder(relatedid,model,ticketnum,sportitemid);
		}
		return this.downCinemaOrder(relatedid,model,ticketnum,type);
	}
	/**
	 * 封装订单视图xml模板文件中对应key=value对
	 * @param objects {"OrderCode","Mobile","BusinissName","ItemName",
				"PlayDate","OrderDetail","CheckPassword", "OrderType", "TotalNum", 
				"TotalPrice", "OrderTime"} 对应的顺序value
	 * @return map
	 */
	private Map getOrderMap(Object...objects){
		String [] fieldArr = {"OrderCode","Mobile","BusinissName","ItemName",
				"PlayDate","OrderDetail","CheckPassword", "OrderType", "TotalNum", 
				"TotalPrice", "OrderTime","StadiumDetailID","StadiumID","SyncType"};
		Map order = new LinkedHashMap();
		for(int index = 0;index < objects.length;index++){
			order.put(fieldArr[index], objects[index]);
		}
		return order;
	}
	/**
	 * 下载运动场馆订单
	 * @param apiUser
	 * @param recordId
	 * @param model
	 * @param ticketnum
	 * @return 订单视图xml模板
	 */
	private String downSportOrder(Long recordId, ModelMap model, String ticketnum, Long sportitemid){
		Sport sport = daoService.getObject(Sport.class, recordId);
		if(sport == null){
			return getErrorXmlView(model,ApiConstant.CODE_DATA_ERROR,"没有该运动场馆");
		}
		Synch synch = getSynch(recordId,Synch.TAG_SPORT,ticketnum);
		List<SportOrder> sportOrderList = synchService.getOrderListBySportIdAndLasttime(recordId, sportitemid, DateUtil.addSecond(synch.getSuccesstime(), -10));
		List<Map> orderList = new ArrayList<Map>();
		for(SportOrder sportOrder:sportOrderList){
			//{"详细":"(1号场地：15:00)(2号场地：15:00)","运动项目":"羽毛球A","运动馆名":"格致羽毛球馆","时间":"2011-12-30"}
			Map<String, String> descMap = JsonUtils.readJsonToMap(sportOrder.getDescription2());
			String detail = "";
			if(StringUtils.isNotBlank(descMap.get("时长"))){
				detail = paseSportDetail(descMap.get("详细"),true) + "@" + descMap.get("时长");
			}else{
				detail =  paseSportDetail(descMap.get("详细"),false);
			}
			OpenTimeTable ott = daoService.getObjectByUkey(OpenTimeTable.class, "id", sportOrder.getMpid(), true);
			String playDate = DateUtil.format(ott.getPlaydate(),"yyyy-MM-dd HH:mm:ss");
			orderList.add(getOrderMap(sportOrder.getTradeNo(),sportOrder.getMobile().substring(7),
					descMap.get("运动馆名"),descMap.get("运动项目"),playDate,
					detail,StringUtil.md5(sportOrder.getCheckpass()),
					1,sportOrder.getQuantity(),sportOrder.getTotalfee(),DateUtil.formatTimestamp(sportOrder.getAddtime()),
					sportOrder.getOttid(),recordId,this.getSyncType(sportOrder.getTradeNo())));
		}
		orderList.addAll(getGoodsOderList(recordId,sport.getName(),synch,2,GoodsConstant.GOODS_TAG_BMH_SPORT));
		model.put("orderList", orderList);
		return getXmlView(model,"api/ticket/toSynchronizeOrderList.vm");
	}
	/**
	 * 下载电影订单
	 * @param apiUser
	 * @param recordId
	 * @param model
	 * @param ticketnum
	 * @return
	 */
	private String downCinemaOrder(Long recordId,ModelMap model,String ticketnum,Integer type){
		Cinema cinema = daoService.getObject(Cinema.class, recordId);
		if(cinema == null){
			return getErrorXmlView(model, ApiConstant.CODE_DATA_ERROR, "没有该影院！");
		}
		Synch synch = null;
		if(StringUtils.equals(type+"", "0")){
			synch = getGoodsSynch(recordId, Synch.TGA_CINEMA, ticketnum);
		}else {
			synch = getSynch(recordId,Synch.TGA_CINEMA,ticketnum);
		}
		//a) 当type=0时，接口仅返回给我们该影院卖品订单
	    //b) 当type=1时，接口仅返回该影院电影订单
	    //c) 当type=2时，接口返回该影院卖品+电影订单
		List<Map> orderList = new ArrayList<Map>();
		if(type==0){
			initGoodsOrder(orderList,recordId,synch,cinema);
		}else if(type==1){
			initTicketOrder(recordId, cinema, synch, orderList);
		}else if(type==2){
			initGoodsOrder(orderList,recordId,synch,cinema);
			initTicketOrder(recordId, cinema, synch, orderList);
		}
		model.put("orderList", orderList);
		return getXmlView(model,"api/ticket/toSynchronizeOrderList.vm");
	}
	/**
	 * 获取下载订单是同步记录信息
	 * @param recordId
	 * @param synchTag
	 * @param ticketnum
	 * @return
	 */
	private Synch getSynch(Long recordId,String synchTag,String ticketnum){
		Synch synch = daoService.getObject(Synch.class, recordId);
		Timestamp cur = new Timestamp(System.currentTimeMillis());
		Timestamp curDate = DateUtil.addDay(DateUtil.getCurTruncTimestamp(), -8);
		if(synch == null){
			synch = new Synch(recordId,synchTag);
		}else if(synch.getSuccesstime() != null && curDate.before(synch.getSuccesstime())){
			curDate = null;
		}
		synch = apiService.saveSynchWithCinema(synch, cur, curDate, ticketnum, null);
		return synch;
	}
	
	/**
	 * 按规则解析
	 * "21:00-22:00 17号场地 50元;20:00-21:00 18号场地 50元;21:00-22:00 19号场地 50元;22:00-23:00 20号场地 50元;"
	 * (1号场地：15:00)(2号场地：15:00)
	 * 字符串
	 * @param detail
	 * @param swin 是否是游泳
	 * @return
	 */
	private  String paseSportDetail(String detail,boolean swin){
		if(detail.indexOf("(") != -1){
			detail = StringUtils.replace(detail, ")(", "@");
			detail = StringUtils.replace(detail, "(", "");
			detail = StringUtils.replace(detail, ")", "");
		}else{
			String[] details = StringUtils.split(detail,";");
			StringBuilder sb = new StringBuilder();
			for(String str:details){
				if(StringUtils.isNotBlank(str)){
					String[] strs = StringUtils.split(str," ");
					if(swin){
						return strs[0];
					}
					sb.append(strs[1]).append("：").append(strs[0]).append(" ").append(strs[2]);
				}
				sb.append("@");
			}
			detail = sb.toString();
			if(detail.lastIndexOf("@") == detail.length() - 1){
				detail = detail.substring(0, detail.length() - 1);
			}
		}
		return detail;
	}
	
	private Synch getGoodsSynch(Long recordId,String synchTag,String ticketnum){
		Synch synch = daoService.getObject(Synch.class, recordId);
		Timestamp cur = new Timestamp(System.currentTimeMillis());
		Timestamp curDate = DateUtil.addDay(DateUtil.getCurTruncTimestamp(), -8);
		if(synch == null){
			synch = new Synch(recordId,synchTag);
		}else if(synch.getSuccesstime() != null && curDate.before(synch.getSuccesstime())){
			curDate = null;
		}
		synch = apiService.saveSynchGoodsWithCinema(synch, cur, curDate, ticketnum, null);
		return synch;
	}
	
	/**
	 * get 同步类型
	 * 正常同步】如果本地已经存在此订单，除了取票状态其他字段全部覆盖。1
	 * 【强制同步】所有字段属性全部覆盖 2
	 * 【删除】删除此订单 3
	 * @param tradeNo
	 * @return
	 */
	private String getSyncType(String tradeNo){
		OrderResult orderResult = daoService.getObject(OrderResult.class, tradeNo);
		String flag = "1";
		if(orderResult != null){
			flag = orderResult.getResult();
		}
		if(OrderResult.RESULTU.equals(flag)){
			flag = "2";
		}else if(OrderResult.RESULTD.equals(flag)){
			flag = "3";
		}else{
			flag = "1";
		}
		return flag;
	}
	
	private void initGoodsOrder(List<Map> orderList,Long recordId,Synch synch,Cinema cinema){
		orderList.addAll(getGoodsOderList(recordId,cinema.getName(),synch,4,GoodsConstant.GOODS_TAG_BMH));
	}
	
	/**
	 * 获取对应的话剧商品订单，电影商品订单，运动商品订单
	 * @param apiUser 下载用户
	 * @param recordId 订单id
	 * @param businissName spring ui ModelMap
	 * @param synch
	 * @param orderType 不同的订单类型，例如：话剧商品订单(orderType = 6)
	 * @return 商品订单视图xml模板
	 */
	private List<Map> getGoodsOderList(Long recordId,String businissName,Synch synch,int orderType,String goosType){
		Timestamp suctime = synch.getSuccesstime();
		if(synch.getGsuctime()!=null && suctime!=null && synch.getGsuctime().after(suctime)){
			suctime = synch.getGsuctime();
		}
		List<GoodsOrder> goodsOrderList = synchService.getGoodsOrderListByRelatedidAndLasttime(recordId, DateUtil.addSecond(suctime, -10),goosType);
		List<Map> orderList = new ArrayList<Map>();
		StringBuilder sb = new StringBuilder();
		for(GoodsOrder order : goodsOrderList){
			Goods goods = daoService.getObject(Goods.class, order.getGoodsid());
			String printcontent = goods.getPrintcontent();
			if(StringUtils.isNotBlank(printcontent)){
				if(printcontent.indexOf("+") != -1){
					printcontent = StringUtils.replace(printcontent,"+","@");
				}else{
					printcontent = StringUtils.replace(printcontent,",","@");
				}
			}else {
				printcontent = "";
			}
			orderList.add(getOrderMap(order.getTradeNo(), order.getMobile().substring(7),
					businissName,goods.getShortname(),DateUtil.format(order.getValidtime(),"yyyy-MM-dd HH:mm:ss"),
						printcontent,StringUtil.md5(order.getCheckpass()),orderType,
							order.getQuantity(),order.getDue(),DateUtil.formatTimestamp(order.getAddtime()),
							order.getGoodsid(),recordId,getSyncType(order.getTradeNo())));//
			sb.append(order.getTradeNo()).append(",");
		}
		this.dbLogger.warn("本次同步的卖品订单号分别为：" + sb.toString());
		return orderList;
	}
	
	private void initTicketOrder(Long recordId, Cinema cinema, Synch synch,
			List<Map> orderList) {
		List<TicketOrder> cinemaOrderList = synchService.getOrderListByCinemaIdAndLasttime(recordId, DateUtil.addSecond(synch.getSuccesstime(), -10));
		for(TicketOrder cinemaOrder:cinemaOrderList){
			//{"影片":"让子弹飞","场次":"12月30日 22:00","影票":"B排15座30元,B排16座30元"}
			Map<String, String> descriptionMap = JsonUtils.readJsonToMap(cinemaOrder.getDescription2());
			String playDate = descriptionMap.get("场次");
			OpenPlayItem opi = daoService.getObjectByUkey(OpenPlayItem.class, "mpid", cinemaOrder.getMpid(), true);
			if(opi != null){
				playDate = DateUtil.format(opi.getPlaytime(),"yyyy-MM-dd HH:mm:ss");
			}
			orderList.add(getOrderMap(
					cinemaOrder.getTradeNo(),cinemaOrder.getMobile().substring(7),
					cinema.getName(),descriptionMap.get("影片"),playDate,
					StringUtils.replace(descriptionMap.get("影票"),",","@"),StringUtil.md5(cinemaOrder.getCheckpass()),
					3,cinemaOrder.getQuantity(),cinemaOrder.getTotalfee(),DateUtil.formatTimestamp(cinemaOrder.getAddtime()),
					cinemaOrder.getMpid(),recordId,getSyncType(cinemaOrder.getTradeNo())));
		}
	}
	
	
	@RequestMapping("/apimac/synch/equipmentStatus.xhtml")
	public String equipmentStatus(
			String equipmentid,
			@RequestParam(required=false,value="equipmentType",defaultValue="pos")
			String equipmentType,
			Long sportid,
			Long relatedid,
			String type,
			String appversion,
			ModelMap model){
		if(sportid == null){
			sportid = relatedid;
		}
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
			be = this.daoService.getObject(Sport.class, sportid);
			placeName = (String) BeanUtil.get(be, "name");
		}else if(TagConstant.TAG_CINEMA.equals(type)){
			be = this.daoService.getObject(Cinema.class, sportid);
			placeName = (String) BeanUtil.get(be, "name");
		}
		map.put("id", equipmentid);
		map.put("sportid", sportid+"");
		map.put("type", type);
		map.put("equipmentType", equipmentType);
		map.put("appversion", appversion);
		map.put("sportName", placeName);
		map.put("synchTime", DateUtil.formatTimestamp(DateUtil.getMillTimestamp()));
		this.mongoService.saveOrUpdateMap(map, MongoData.SYSTEM_ID, MongoData.NS_EQUIPMENTSTATUS);
		return getSingleResultXmlView(model, "success");
	}
}
