package com.gewara.untrans.drama.impl;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.stereotype.Service;

import com.gewara.Config;
import com.gewara.api.gpticket.service.GpticketApiService;
import com.gewara.api.gpticket.vo.ticket.FieldAreaSeatVo;
import com.gewara.api.gpticket.vo.ticket.FieldAreaVo;
import com.gewara.api.gpticket.vo.ticket.FieldVo;
import com.gewara.api.gpticket.vo.ticket.ShowAreaVo;
import com.gewara.api.gpticket.vo.ticket.ShowItemVo;
import com.gewara.api.gpticket.vo.ticket.ShowPackPriceVo;
import com.gewara.api.gpticket.vo.ticket.ShowPriceVo;
import com.gewara.api.gpticket.vo.ticket.ShowSeatVo;
import com.gewara.api.vo.ResultCode;
import com.gewara.constant.ApiConstant;
import com.gewara.constant.DramaConstant;
import com.gewara.constant.OdiConstant;
import com.gewara.constant.PaymethodConstant;
import com.gewara.constant.Status;
import com.gewara.constant.sys.JsonDataKey;
import com.gewara.constant.sys.LogTypeConstant;
import com.gewara.constant.ticket.OpiConstant;
import com.gewara.helper.UpdateDpiContainer;
import com.gewara.model.common.JsonData;
import com.gewara.model.drama.Drama;
import com.gewara.model.drama.DramaPlayItem;
import com.gewara.model.drama.OpenDramaItem;
import com.gewara.model.drama.Theatre;
import com.gewara.model.drama.TheatreField;
import com.gewara.model.drama.TheatreProfile;
import com.gewara.model.drama.TheatreRoom;
import com.gewara.model.drama.TheatreSeatArea;
import com.gewara.service.DaoService;
import com.gewara.service.OrderException;
import com.gewara.service.drama.DpiManageService;
import com.gewara.service.drama.DramaPlayItemService;
import com.gewara.service.drama.OpenDramaService;
import com.gewara.service.drama.synch.TheatreSynchService;
import com.gewara.support.ErrorCode;
import com.gewara.untrans.drama.OdiOpenService;
import com.gewara.untrans.drama.RemoteTheatreService;
import com.gewara.untrans.drama.TheatreOperationService;
import com.gewara.untrans.monitor.MonitorService;
import com.gewara.util.BeanUtil;
import com.gewara.util.ChangeEntry;
import com.gewara.util.DateUtil;
import com.gewara.util.GewaLogger;
import com.gewara.util.JsonUtils;
import com.gewara.util.LoggerUtils;
import com.gewara.web.action.inner.util.DramaRemoteUtil;

@Service("remoteTheatreService")
public class RemoteTheatreServiceImpl implements RemoteTheatreService {
	private GewaLogger dbLogger = LoggerUtils.getLogger(getClass(), Config.getServerIp(), Config.SYSTEMID);
	
	@Autowired@Qualifier("gpticketApiService")
	private GpticketApiService gpticketApiService;
	
	@Autowired@Qualifier("daoService")
	private DaoService daoService;
	
	@Autowired@Qualifier("monitorService")
	private MonitorService monitorService;
	
	@Autowired@Qualifier("hibernateTemplate")
	private HibernateTemplate hibernateTemplate;
	
	@Autowired@Qualifier("theatreSynchService")
	private TheatreSynchService theatreSynchService;
	
	@Autowired@Qualifier("dramaPlayItemService")
	private DramaPlayItemService dramaPlayItemService;
	
	@Autowired@Qualifier("openDramaService")
	private OpenDramaService openDramaService;
	
	@Autowired@Qualifier("dpiManageService")
	private DpiManageService dpiManageService;
	
	@Autowired@Qualifier("odiOpenService")
	private OdiOpenService odiOpenService;
	
	@Autowired@Qualifier("theatreOperationService")
	private TheatreOperationService theatreOperationService;
	
	@Override
	public ErrorCode<List<TheatreField>> updateTheatreField(Long userid, Theatre theatre, final List<String> msgList){
		ResultCode<List<FieldVo>> response = gpticketApiService.getFieldList(theatre.getId());
		if(!response.isSuccess()) return ErrorCode.getFailure(response.getErrcode(), response.getMsg());
		List<TheatreField> fieldList = daoService.getObjectListByField(TheatreField.class, "theatreid", theatre.getId());
		Map<String,TheatreField> fieldMap = BeanUtil.beanListToMap(fieldList, "fieldnum");
		List<FieldVo> fieldVoList = response.getRetval();
		List<TheatreField> theatreFieldList = new ArrayList<TheatreField>();
		for (FieldVo fieldVo : fieldVoList) {
			if(StringUtils.isBlank(fieldVo.getFieldnum())){
				msgList.add("场馆：" + theatre.getName() + "[" + theatre.getId()+ "],场地：" + fieldVo.getName() +"-->没有设置序号");
				continue;
			}
			TheatreField field = fieldMap.get(fieldVo.getFieldnum());
			boolean isAdd = false;
			if(field == null){
				field = new TheatreField(theatre.getId(), fieldVo.getFieldtype());
				msgList.add("场馆：" + theatre.getName() + "[" + theatre.getId()+ "],新增：" + fieldVo.getName());
				isAdd = true;
			}
			ChangeEntry changeEntry = new ChangeEntry(field);
			DramaRemoteUtil.copyTheatreField(field, fieldVo);
			if(!isAdd && !changeEntry.getChangeMap(field).isEmpty()){
				msgList.add("场馆：" + theatre.getName() + "[" + theatre.getId()+ "],修改：" + fieldVo.getName());
			}
			daoService.saveObject(field);
			theatreFieldList.add(field);
			monitorService.saveChangeLog(userid, TheatreField.class, field.getId(), changeEntry.getChangeMap(field));
			ErrorCode<List<TheatreRoom>> code = updateTheatreRoom(userid, field, msgList);
			if(!code.isSuccess()){
				msgList.add("场馆：" + theatre.getName() + ",更新区域错误：" + code.getErrcode() + "," +code.getMsg());
			}
		}
		return ErrorCode.getSuccessReturn(theatreFieldList);
	}
	
	@Override
	public ErrorCode<List<TheatreRoom>> updateTheatreRoom(Long userid, TheatreField field, final List<String> msgList){
		if(field.hasFieldtype(OdiConstant.PARTNER_GEWA)) return ErrorCode.getFailure(ApiConstant.CODE_SIGN_ERROR, "非第三方场馆");
		ResultCode<List<FieldAreaVo>> response = gpticketApiService.getFieldAreaList(field.getTheatreid(), field.getFieldnum());
		if(!response.isSuccess()) return ErrorCode.getFailure(response.getErrcode(), response.getMsg());
		List<FieldAreaVo> fieldAreaVoList = response.getRetval();
		List<TheatreRoom> roomList = daoService.getObjectListByField(TheatreRoom.class, "fieldid", field.getId());
		Map<String,TheatreRoom> roomMap = BeanUtil.beanListToMap(roomList, "num");
		List<TheatreRoom> theatreRoomList = new ArrayList<TheatreRoom>();
		for (FieldAreaVo areaVo : fieldAreaVoList) {
			TheatreRoom room = roomMap.get(areaVo.getAreanum());
			boolean isAdd = false;
			if(room == null){
				room = new TheatreRoom(field.getTheatreid(), field.getId());
				msgList.add("场馆ID：[" + field.getTheatreid()+ "],场地：" +field.getName()+ "[" + field.getId()+ "],新增：" + areaVo.getName());
				isAdd = true;
			}
			ChangeEntry changeEntry = new ChangeEntry(room);
			DramaRemoteUtil.copyTheatreRoom(room, areaVo);
			if(!isAdd && !changeEntry.getChangeMap(room).isEmpty()){
				msgList.add("场馆ID：[" + field.getTheatreid()+ "],场地：" +field.getName()+ "[" + field.getId()+ "],修改：" + areaVo.getName());
			}
			daoService.saveObject(room);
			theatreRoomList.add(room);
			monitorService.saveChangeLog(userid, TheatreRoom.class, room.getId(), changeEntry.getChangeMap(room));
		}
		return ErrorCode.getSuccessReturn(theatreRoomList);
	}
	
	@Override
	public ErrorCode updateDramaPlayItem(Long userid, Long theatreid, final List<String> msgList, int notUpdateWithMin){
		if(theatreid==null) return ErrorCode.getFailure("场馆ID不能为空！");
		Theatre theatre = daoService.getObject(Theatre.class, theatreid);
		if(theatre == null) return ErrorCode.getFailure("场馆不存在！");
		String jsonKey = JsonDataKey.KEY_SYNCH_DRAMAPLAYITEM + "_" + theatreid;
		JsonData jsonData = daoService.getObject(JsonData.class, jsonKey);
		Timestamp updatetime = null;
		Timestamp cur = DateUtil.getCurFullTimestamp();
		if(jsonData == null){
			jsonData = new JsonData(jsonKey);
			updatetime = cur;
			jsonData.setTag("synch");
			jsonData.setValidtime(DateUtil.parseTimestamp("2032-01-01 00:00:00"));
		}else{
			Map<String, String> dataMap = JsonUtils.readJsonToMap(jsonData.getData());
			updatetime = DateUtil.parseTimestamp(dataMap.get("updatetime"));
		}
		if(DateUtil.addMinute(updatetime, notUpdateWithMin).compareTo(cur) > 0){
			return ErrorCode.getFailure("在" + notUpdateWithMin + "内已经更新，本次忽略！");
		}
		ResultCode<List<ShowItemVo>> response = gpticketApiService.getShowItemList(theatreid, DateUtil.addMinute(updatetime, -30));//倒退30分
		if(!response.isSuccess()){
			String msg = "更新排片错误：" + response.getMsg() + ", code:" + response.getErrcode() + ", res:" + response.getRetval();
			msgList.add(msg);
			return ErrorCode.getFailure(msg);
		}
		List<ShowItemVo> itemVoList = response.getRetval();
		List<TheatreField> fieldList = daoService.getObjectListByField(TheatreField.class, "theatreid", theatreid);
		Map<String,TheatreField> fieldMap = BeanUtil.beanListToMap(fieldList, "fieldnum");
		UpdateDpiContainer container = new UpdateDpiContainer();
		for (ShowItemVo itemVo : itemVoList) {
			TheatreField room = fieldMap.get(itemVo.getFieldnum());
			if(room==null){
				String msg = "更新排片错误：场地未关联：" + theatre.getName() + ":" + itemVo.getFieldnum();
				msgList.add(msg);
				dbLogger.warnWithType(LogTypeConstant.LOG_TYPE_API, msg);
				continue;
			}
			DramaPlayItem item = dramaPlayItemService.getDpiBySeqno(itemVo.getPartner(), itemVo.getSiseq());
			theatreSynchService.updateShowItemVo(container, itemVo, item, theatre, room, msgList);
			if(item != null){
				ErrorCode<List<TheatreSeatArea>> codeArea = updateTheateSeatArea(userid, container, item, msgList);
				String msg = "场次：[" + item.getId() + "]" +item.getTheatrename() + item.getDramaname() + " " + DateUtil.format(item.getPlaytime(), "yyyy-MM-dd") 
						+ "(" + DateUtil.getCnWeek(item.getPlaytime()) +")" + DateUtil.format(item.getPlaytime(), "HH:mm") ;
				if(!codeArea.isSuccess()){
					String tmp = msg + ",更新区域错误：" + codeArea.getErrcode() + "," + codeArea.getMsg();
					msgList.add(tmp);
				}else{
					updateSeatPrice(userid, container, item, codeArea.getRetval(), msgList);
				}
			}
		}
		Map<String, String> dataMap = JsonUtils.readJsonToMap(jsonData.getData());
		dataMap.put("updatetime", DateUtil.format(cur, "yyyy-MM-dd HH:mm:ss"));
		jsonData.setData(JsonUtils.writeObjectToJson(dataMap));
		daoService.saveObject(jsonData);
		return ErrorCode.SUCCESS;
	}
	
	@Override
	public void updateDramaPlayItem(Long userid, Long theatreid, Date playdate, final List<String> msgList){
		Theatre theatre = daoService.getObject(Theatre.class, theatreid);
		ResultCode<List<ShowItemVo>> code = gpticketApiService.getShowItemList(theatreid, playdate);
		if(!code.isSuccess()){
			dbLogger.warnWithType(LogTypeConstant.LOG_TYPE_API, "更新排片：theatreid:" + theatreid + ",msg" + code.getMsg());
			msgList.add("更新排片错误：" + code.getMsg() + ", code:" + code.getErrcode() + ", res:" + code.getRetval());
			return;
		}
		List<ShowItemVo> showItemVoList = code.getRetval();
		Timestamp starttime = DateUtil.getBeginTimestamp(playdate);
		Timestamp endtime = DateUtil.getLastTimeOfDay(starttime);
		String query = "from DramaPlayItem where theatreid = ? and playtime >= ? and playtime<? and sellerseq is not null";
		List<DramaPlayItem> oldList = hibernateTemplate.find(query, theatreid, starttime, endtime);
		Map<String, DramaPlayItem> oldMap = BeanUtil.beanListToMap(oldList, "sellerseq");
		List<TheatreField> fieldList = daoService.getObjectListByField(TheatreField.class, "theatreid", theatreid);
		Map<String,TheatreField> fieldMap = BeanUtil.beanListToMap(fieldList, "fieldnum");
		UpdateDpiContainer container = new UpdateDpiContainer();
		for (ShowItemVo itemVo : showItemVoList) {
			TheatreField room = fieldMap.get(itemVo.getFieldnum());
			if(room==null){
				String msg = "更新排片错误：场地未关联：" + theatre.getName() + ":" + itemVo.getFieldnum();
				msgList.add(msg);
				dbLogger.warnWithType(LogTypeConstant.LOG_TYPE_API, msg);
				continue;
			}
			DramaPlayItem item = oldMap.remove(itemVo.getSiseq());
			theatreSynchService.updateShowItemVo(container, itemVo, item, theatre, room, msgList);
			if(item != null){
				ErrorCode<List<TheatreSeatArea>> codeArea = updateTheateSeatArea(userid, container, item, msgList);
				String msg = "场次：[" + item.getId() + "]" +item.getTheatrename() + item.getDramaname() + " " + DateUtil.format(item.getPlaytime(), "yyyy-MM-dd") 
						+ "(" + DateUtil.getCnWeek(item.getPlaytime()) +")" + DateUtil.format(item.getPlaytime(), "HH:mm") ;
				if(!codeArea.isSuccess()){
					String tmp = msg + ",更新区域错误：" + codeArea.getErrcode() + "," + codeArea.getMsg();
					msgList.add(tmp);
				}else{
					updateSeatPrice(userid, container, item, codeArea.getRetval(), msgList);
				}
			}
		}
		for(DramaPlayItem dpi: oldMap.values()){
			if(!StringUtils.equals(dpi.getStatus(), Status.N)){
				String msg = "删除排片: theatreid:" + dpi.getTheatreid() + ", dpi:" + JsonUtils.writeObjectToJson(dpi);
				msgList.add(msg);
				dbLogger.warnWithType(LogTypeConstant.LOG_TYPE_API, msg);
				monitorService.saveDelLog(0L, dpi.getId(), dpi);
				dpi.setStatus(DramaPlayItem.STATUS_N);
				ErrorCode itemCode = theatreSynchService.updatePlayItem(dpi);
				if(StringUtils.isNotBlank(itemCode.getMsg())){
					msgList.add(itemCode.getMsg());
				}
				daoService.saveObject(dpi);
			}
		}
	}
	
	@Override
	public ErrorCode<List<TheatreSeatArea>> updateTheateSeatArea(Long userid, UpdateDpiContainer container, DramaPlayItem dpi, final List<String> msgList){
		if(dpi.hasGewa()) return ErrorCode.getFailure(ApiConstant.CODE_SIGN_ERROR, "非第三方场次！");
		ResultCode<List<ShowAreaVo>> response = gpticketApiService.getShowAreaList(dpi.getSellerseq());
		if(!response.isSuccess()) return ErrorCode.getFailure(response.getErrcode(), response.getMsg());
		ErrorCode<List<TheatreSeatArea>> code = theatreSynchService.updateShowAreaVo(userid, container, dpi, response.getRetval(), msgList);
		return code;
	}
	

	@Override
	public ErrorCode updateSeatPrice(Long userid, UpdateDpiContainer container, DramaPlayItem item, List<TheatreSeatArea> seatAreaList, final List<String> msgList) {
		String msg = "场次：[" + item.getId() + "]" +item.getTheatrename() + item.getDramaname() + " " + DateUtil.format(item.getPlaytime(), "yyyy-MM-dd") 
				+ "(" + DateUtil.getCnWeek(item.getPlaytime()) +")" + DateUtil.format(item.getPlaytime(), "HH:mm") ;
		for (TheatreSeatArea seatArea : seatAreaList) {
			ErrorCode codePrice = updateSeatPrice(userid, container, seatArea);
			if(!codePrice.isSuccess()){
				String tmp = msg + ",区域：" + seatArea.getAreaname() +"[" + seatArea.getId()+ "],更新价格错误：" + codePrice.getErrcode() +"," + codePrice.getMsg();
				msgList.add(tmp);
			}else if(StringUtils.isNotBlank(codePrice.getMsg())){
				msgList.add(codePrice.getMsg());
			}
			ErrorCode codeDisquantity = updateDisQuantity(userid, container, seatArea);
			if(!codeDisquantity.isSuccess()){
				msgList.add("更新区域:" + seatArea.getAreaname() + ",套票价格错误：" + codeDisquantity.getErrcode() + "," + codeDisquantity.getMsg());
				continue;
			}else if(StringUtils.isNotBlank(codeDisquantity.getMsg())){
				msgList.add(codeDisquantity.getMsg());
			}
		}
		return ErrorCode.SUCCESS;
	}
	
	@Override
	public ErrorCode updateSeatPrice(Long userid, UpdateDpiContainer container, TheatreSeatArea seatArea){
		if(seatArea.hasGewara()) return ErrorCode.getFailure(ApiConstant.CODE_SIGN_ERROR, "非第三方场区！");
		ResultCode<List<ShowPriceVo>> response = gpticketApiService.getShowPriceList(seatArea.getSellerseq());
		if(!response.isSuccess()) return ErrorCode.getFailure(response.getErrcode(), response.getMsg());
		ErrorCode code = theatreSynchService.updateSeatPriceVo(userid, container, seatArea, response.getRetval());
		return code;
	}
	
	@Override
	public ErrorCode updateDisQuantity(Long userid, UpdateDpiContainer container, TheatreSeatArea seatArea){
		if(seatArea.hasGewara()) return ErrorCode.getFailure(ApiConstant.CODE_SIGN_ERROR, "非第三方场区！");
		ResultCode<List<ShowPackPriceVo>> response = gpticketApiService.getShowPackPriceList(seatArea.getSellerseq());
		if(!response.isSuccess()) return ErrorCode.getFailure(response.getErrcode(), response.getMsg());
		ErrorCode code = theatreSynchService.updateShowPackPriceVo(userid, container, seatArea, response.getRetval());
		return code;
	}
	
	@Override
	public ErrorCode<List<ShowSeatVo>> refreshAreaSeat(Long userid, OpenDramaItem odi, TheatreSeatArea seatArea, boolean refresh, final List<String> msgList){
		if(odi.hasGewara()) return ErrorCode.getFailure(ApiConstant.CODE_SIGN_ERROR, "非第三方场次！");
		if(!odi.isOpenseat()) return ErrorCode.getFailure(ApiConstant.CODE_SIGN_ERROR, "非选座场次！");
		if(!odi.getDpid().equals(seatArea.getDpid())) return ErrorCode.getFailure(ApiConstant.CODE_SIGN_ERROR, "场区不存在该场次！");
		ResultCode<List<ShowSeatVo>> response = gpticketApiService.getShowSeatList(seatArea.getSellerseq());
		//错误或不刷新直接返回
		if(!response.isSuccess()){
			return ErrorCode.getFailure(response.getErrcode(), response.getMsg());
		}else if(!refresh){
			return ErrorCode.getSuccessReturn(response.getRetval());
		}
		ErrorCode<List<String>> code = theatreSynchService.refreshAreaSeat(userid, seatArea, response.getRetval());
		if(!code.isSuccess()) return ErrorCode.getFailure(code.getErrcode(), code.getMsg());
		
		int sell = theatreSynchService.refreshSellSeatId(seatArea);
		msgList.add("刷新场次区域锁定或卖出座位ID:" + sell + "个！");
		String seatmap = openDramaService.getTheatreSeatAreaMapStr(seatArea);
		seatArea.setSeatmap(seatmap);
		daoService.saveObject(seatArea);
		ErrorCode<List<String>> remoteLockList = theatreOperationService.updateRemoteLockSeat(seatArea, OdiConstant.SECONDS_SHOW_SEAT, false);
		List<String> hfhLockList = new ArrayList<String>();
		if(remoteLockList.isSuccess()){
			hfhLockList = remoteLockList.getRetval();
			dpiManageService.updateTheatreSeatAreaStats(odi, seatArea, hfhLockList, false);
		}
		return ErrorCode.getSuccessReturn(response.getRetval());
	}

	@Override
	public ErrorCode<List<FieldAreaSeatVo>> updateTheatreRoomSeat(Long userid, TheatreRoom room, boolean refresh, List<String> msgList) {
		TheatreField field = daoService.getObject(TheatreField.class, room.getFieldid());
		ResultCode<List<FieldAreaSeatVo>> response = gpticketApiService.getFieldAreaSeatList(room.getTheatreid(), field.getFieldnum(), room.getNum());
		if(!response.isSuccess()){
			return ErrorCode.getFailure(response.getErrcode(), response.getMsg());
		}else if(!refresh){
			return ErrorCode.getSuccessReturn(response.getRetval());
		}
		List<String> tmpList = theatreSynchService.updateRoomSeatList(room, response.getRetval(), refresh);
		msgList.addAll(tmpList);
		return ErrorCode.getSuccessReturn(response.getRetval());
	}


	private ErrorCode<List<String>> updateAreaSeat(Long userid, OpenDramaItem odi){
		if(odi == null) return ErrorCode.getFailure("场次未开放或不存在！");
		if(odi.hasGewara()) return ErrorCode.getFailure("非第三方场次，更新错误！");
		List<String> msgList = new ArrayList<String>();
		List<TheatreSeatArea> seatAreaList = daoService.getObjectListByField(TheatreSeatArea.class, "dpid", odi.getDpid());
		UpdateDpiContainer container = new UpdateDpiContainer();
		for (TheatreSeatArea seatArea : seatAreaList) {
			ErrorCode codePrice = updateSeatPrice(userid, container, seatArea);
			if(!codePrice.isSuccess()){
				msgList.add("更新区域:" + seatArea.getAreaname() + ",价格错误：" + codePrice.getErrcode() + "," + codePrice.getMsg());
				continue;
			}else if(StringUtils.isNotBlank(codePrice.getMsg())){
				msgList.add(codePrice.getMsg());
			}
			if(odi.isOpenseat()){ //按座位开放
				ErrorCode<List<ShowSeatVo>> codeSeat = refreshAreaSeat(userid, odi, seatArea, true, msgList);
				if(!codeSeat.isSuccess()){
					msgList.add("更新区域:" + seatArea.getAreaname() + ",座位错误：" + codeSeat.getErrcode() + "," + codeSeat.getMsg());
				}
			}
			ErrorCode codeDisquantity = updateDisQuantity(userid, container, seatArea);
			if(!codeDisquantity.isSuccess()){
				msgList.add("更新区域:" + seatArea.getAreaname() + ",套票价格错误：" + codeDisquantity.getErrcode() + "," + codeDisquantity.getMsg());
				continue;
			}else if(StringUtils.isNotBlank(codeDisquantity.getMsg())){
				msgList.add(codeDisquantity.getMsg());
			}
		}
		odiOpenService.asynchUpdateAreaStats(odi);
		return ErrorCode.SUCCESS;
	}
	

	@Override
	public ErrorCode<List<String>> refreshOpenTheatreSeat(DramaPlayItem dpi){
		OpenDramaItem odi = daoService.getObjectByUkey(OpenDramaItem.class, "dpid", dpi.getId());
		if(odi == null) return ErrorCode.getFailure("排期未开放，不能刷新座位或价格！");
		ErrorCode<List<String>> code = updateAreaSeat(0L, odi);
		return code;
	}
	
	@Override
	public ErrorCode<List<String>> openDramPlayitem(DramaPlayItem dpi, Theatre theatre, Drama drama, TheatreProfile profile) throws OrderException{
		if(dpi.hasGewa()) return ErrorCode.getFailure("非第三方场次" + dpi.getSeller());
		OpenDramaItem odi = new OpenDramaItem(theatre, drama, dpi, profile);
		odi.setSeller(dpi.getSeller());
		clearOdiPreferential(odi, drama);
		daoService.saveObject(odi);
		ErrorCode<List<String>> code = updateAreaSeat(0L, odi);
		return code;
	}
	
	@Override
	public void clearOdiPreferential(OpenDramaItem odi, Drama drama){
		if(drama.hasPretype(DramaConstant.PRETYPE_ENTRUST)){
			odi.setMinpoint(0);
			odi.setMaxpoint(0);
			odi.setElecard("");
			Map<String,String> otherinfoMap = JsonUtils.readJsonToMap(odi.getOtherinfo());
			String payoption = otherinfoMap.get(OpiConstant.PAYOPTION);
			String paymethod = otherinfoMap.get(OpiConstant.PAYCMETHODLIST);
			List<String> paymethodList = new ArrayList<String>();
			if(StringUtils.isNotBlank(paymethod)){
				paymethodList.addAll(Arrays.asList(StringUtils.split(paymethod, ",")));
			}
			if(StringUtils.isNotBlank(payoption)){
				if(StringUtils.equals(payoption, "notuse")){
					if(!paymethodList.contains(PaymethodConstant.PAYMETHOD_GEWAPAY)){
						paymethodList.add(PaymethodConstant.PAYMETHOD_GEWAPAY);
					}
				}else{
					if(paymethodList.contains(PaymethodConstant.PAYMETHOD_GEWAPAY)){
						paymethodList.remove(PaymethodConstant.PAYMETHOD_GEWAPAY);
					}
				}
			}else{
				otherinfoMap.put(OpiConstant.PAYOPTION, "notuse");
				if(!paymethodList.contains(PaymethodConstant.PAYMETHOD_GEWAPAY)){
					paymethodList.add(PaymethodConstant.PAYMETHOD_GEWAPAY);
				}
			}
			otherinfoMap.put(OpiConstant.PAYCMETHODLIST, StringUtils.join(paymethodList, ","));
			odi.setOtherinfo(JsonUtils.writeMapToJson(otherinfoMap));
		}
	}
}
