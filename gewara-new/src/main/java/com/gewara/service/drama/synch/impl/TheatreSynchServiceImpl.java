package com.gewara.service.drama.synch.impl;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.gewara.api.gpticket.vo.ticket.FieldAreaSeatVo;
import com.gewara.api.gpticket.vo.ticket.ShowAreaVo;
import com.gewara.api.gpticket.vo.ticket.ShowItemVo;
import com.gewara.api.gpticket.vo.ticket.ShowPackPriceVo;
import com.gewara.api.gpticket.vo.ticket.ShowPriceVo;
import com.gewara.api.gpticket.vo.ticket.ShowSeatVo;
import com.gewara.constant.ApiConstant;
import com.gewara.constant.OdiConstant;
import com.gewara.constant.Status;
import com.gewara.constant.order.SettleConfigConstant;
import com.gewara.constant.ticket.SeatConstant;
import com.gewara.helper.UpdateDpiContainer;
import com.gewara.model.drama.DisQuantity;
import com.gewara.model.drama.Drama;
import com.gewara.model.drama.DramaPlayItem;
import com.gewara.model.drama.DramaSettle;
import com.gewara.model.drama.OpenDramaItem;
import com.gewara.model.drama.OpenTheatreSeat;
import com.gewara.model.drama.Theatre;
import com.gewara.model.drama.TheatreField;
import com.gewara.model.drama.TheatreRoom;
import com.gewara.model.drama.TheatreRoomSeat;
import com.gewara.model.drama.TheatreSeatArea;
import com.gewara.model.drama.TheatreSeatPrice;
import com.gewara.service.SettleService;
import com.gewara.service.drama.synch.TheatreSynchService;
import com.gewara.service.impl.BaseServiceImpl;
import com.gewara.support.ErrorCode;
import com.gewara.support.MultiPropertyComparator;
import com.gewara.untrans.monitor.MonitorService;
import com.gewara.util.BeanUtil;
import com.gewara.util.ChangeEntry;
import com.gewara.util.DateUtil;
import com.gewara.util.JsonUtils;
import com.gewara.web.action.inner.util.DramaRemoteUtil;

@Service("theatreSynchService")
public class TheatreSynchServiceImpl extends BaseServiceImpl implements TheatreSynchService {
	
	@Autowired@Qualifier("monitorService")
	private MonitorService monitorService;
	
	@Autowired@Qualifier("settleService")
	private SettleService settleService;
	
	@Override
	public void updateShowItemVo(UpdateDpiContainer container, ShowItemVo itemVo, DramaPlayItem dpi, Theatre theatre, TheatreField room, List<String> msgList){
		if(StringUtils.equals(itemVo.getStatus(), DramaPlayItem.STATUS_N)){//删除
			if(dpi != null){
				String msg = "删除排片: theatreid:" + itemVo.getTheatreid() + ", dpi:" + JsonUtils.writeObjectToJson(dpi);
				dbLogger.warn(msg);
				container.addDelete(dpi.getId());
				dpi.setStatus(Status.N);
				baseDao.saveObject(dpi);
			}
		}else{//修改、增加
			boolean isAdd = false;
			if(dpi == null){
				dpi = new DramaPlayItem(DateUtil.getCurFullTimestamp());
				isAdd = true;
			}
			ChangeEntry changeEntry = new ChangeEntry(dpi);
			Drama drama = baseDao.getObject(Drama.class, itemVo.getDramaid());
			ErrorCode result = updateDpi(itemVo, dpi, theatre, room, drama);
			baseDao.saveObject(dpi);
			if(isAdd){
				container.addInsert(dpi);
			}else if(StringUtils.isNotBlank(result.getMsg())) {
				container.addUpdate(dpi);
				msgList.add(result.getMsg());
				ErrorCode code = updatePlayItem(dpi);
				if(StringUtils.isNotBlank(code.getMsg())){
					msgList.add(code.getMsg());
				}
			}
			monitorService.saveSysChangeLog(DramaPlayItem.class, dpi.getId(), changeEntry.getChangeMap(dpi));
		}
	}
	
	@Override
	public ErrorCode updatePlayItem(DramaPlayItem dpi){
		OpenDramaItem odi = baseDao.getObjectByUkey(OpenDramaItem.class, "dpid", dpi.getId());
		if(odi == null) return ErrorCode.SUCCESS;
		String msg = "";
		if(StringUtils.equals(dpi.getStatus(), DramaPlayItem.STATUS_N)){
			odi.setStatus(OdiConstant.STATUS_DISCARD);
			msg += "，场次删除！";
		}else{
			String diff = OdiConstant.getDifferent(odi, dpi);
			if(StringUtils.isNotBlank(diff)){
				msg = "场次自动更改，" + odi.getDramaname() + ", " + odi.getTheatrename() + ", dpid=" + odi.getDpid() + "：" + diff;
				if(!odi.getDramaid().equals(dpi.getDramaid())){//更换影片
					Drama drama = baseDao.getObject(Drama.class, dpi.getDramaid());
					odi.setDramaname(drama.getRealBriefname());
					odi.setStatus(OdiConstant.STATUS_NOBOOK);
					msg += "，项目更改，自动关闭";
				}
				if(!odi.getPlaytime().equals(dpi.getPlaytime())){
					odi.setStatus(OdiConstant.STATUS_NOBOOK);
					msg += ",演出时间更改，自动关闭"; 
				}
				if(!odi.getEndtime().equals(dpi.getEndtime())){
					odi.setStatus(OdiConstant.STATUS_NOBOOK);
					msg += "，演出结束时间更改，自动关闭"; 
				}
				if(!StringUtils.equals(odi.getLanguage(), dpi.getLanguage())){
					odi.setStatus(OdiConstant.STATUS_NOBOOK);
					msg += "，语言更改，自动关闭";  
				}
				if(!StringUtils.equals(odi.getOpentype(), dpi.getOpentype())){
					odi.setStatus(OdiConstant.STATUS_NOBOOK);
					msg += "， 开放类型更改，自动关闭";
				}
				if(!StringUtils.equals(odi.getPeriod(), dpi.getPeriod())){
					odi.setStatus(OdiConstant.STATUS_NOBOOK);
					msg += "，是否固定时间更改，自动关闭";
				}
				
				if(!dpi.getRoomid().equals(odi.getRoomid())){
					odi.setStatus(OdiConstant.STATUS_NOBOOK);
					msg += "，“！场地！”变更，自动关闭，联系系统管理员！";
				}
			}
		}
		DramaRemoteUtil.copyDramPlayItem(odi, dpi);
		baseDao.saveObject(odi);
		return ErrorCode.getSuccess(msg);
	}
	
	private ErrorCode updateDpi(ShowItemVo itemVo, DramaPlayItem dpi, Theatre theatre, TheatreField field, Drama drama){
		String msg = "";
		dpi.setCitycode(theatre.getCitycode());
		dpi.setTheatreid(theatre.getId());
		dpi.setDramaid(itemVo.getDramaid());
		dpi.setRoomid(field.getId());
		dpi.setRoomname(field.getName());
		dpi.setSeller(itemVo.getPartner());
		dpi.setSellerseq(itemVo.getSiseq());
		dpi.setName(itemVo.getShowname());
		dpi.setDramaname(drama.getRealBriefname());
		dpi.setTheatrename(theatre.getRealBriefname());
		if(dpi.getId()==null){
			dpi.setOpentype(itemVo.getItemtype());
			dpi.setPeriod(itemVo.getPeriod());
			dpi.setPlaytime(itemVo.getPlaytime());
			dpi.setEndtime(itemVo.getEndtime());
		}else{
			if(!StringUtils.equals(dpi.getOpentype(), itemVo.getItemtype())){
				String oldOpentype = dpi.getOpentype();
				dpi.setOpentype(itemVo.getItemtype());
				msg += ",场馆更改了开放类型：opentype -->" + oldOpentype + "-->" + itemVo.getItemtype();
			}
			
			if(!StringUtils.equals(dpi.getPeriod(), itemVo.getPeriod())){
				String oldPeriod = dpi.getPeriod();
				dpi.setPeriod(itemVo.getPeriod());
				msg += ",场馆都更改了是否固定时间：period -->" + oldPeriod + "-->" + itemVo.getPeriod();
			}
			
			if(!dpi.getPlaytime().equals(itemVo.getPlaytime())){
				String playtime = DateUtil.formatTimestamp(dpi.getPlaytime());
				dpi.setPlaytime(itemVo.getPlaytime());
				msg += ",场馆更改了演出时间：playtime -->" + playtime + "-->" + DateUtil.formatTimestamp(itemVo.getPlaytime());
			}
			
			if(!dpi.getEndtime().equals(itemVo.getEndtime())){
				String endtime = DateUtil.formatTimestamp(dpi.getEndtime());
				dpi.setEndtime(itemVo.getEndtime());
				msg += ",场馆更改了演出时间：endtime -->" + endtime + "-->" + DateUtil.formatTimestamp(itemVo.getEndtime());
			}
		}
		if(StringUtils.isNotBlank(msg)){
			msg = theatre.getName() + "," + drama.getDramaname() + ", dpid=" + dpi.getId() + ", " + msg;
		}
		return ErrorCode.getSuccess(msg);
	}
	
	@Override
	public ErrorCode<List<String>> refreshAreaSeat(Long userid, TheatreSeatArea seatArea, List<ShowSeatVo> seatVoList){
		if(seatArea.hasGewara()) return ErrorCode.getFailure(ApiConstant.CODE_SIGN_ERROR, "非第三方场次区域！");
		if(CollectionUtils.isEmpty(seatVoList)) {
			return ErrorCode.getFailure(ApiConstant.CODE_SIGN_ERROR, "座位数据为空！");
		}
		List<TheatreSeatPrice> seatPriceList = baseDao.getObjectListByField(TheatreSeatPrice.class, "areaid", seatArea.getId());
		if(seatPriceList.isEmpty()) return ErrorCode.getFailure(ApiConstant.CODE_DATA_ERROR, "场区:" + seatArea.getName() + ",价格没有同步！");
		Map<String, TheatreSeatPrice> priceMap = BeanUtil.beanListToMap(seatPriceList, "sispseq");
		List<String> msgList = new ArrayList<String>();
		List<OpenTheatreSeat> oseatList = new ArrayList<OpenTheatreSeat>();
		for (ShowSeatVo seatVo : seatVoList) {
			TheatreSeatPrice seatPrice = priceMap.get(seatVo.getPriceseq());
			if(seatPrice == null) return ErrorCode.getFailure(ApiConstant.CODE_DATA_ERROR, "价格：" + seatVo.getPrice() + "[" + seatVo.getPriceseq()+ "]不存在");
			OpenTheatreSeat seat = DramaRemoteUtil.createOpenTheatreSeat(seatVo, seatPrice);
			oseatList.add(seat);
			msgList.add("增加座位：" + seat.getLineno() + "," + seat.getRankno() +"\n");
		}
		List<OpenTheatreSeat> oldSeatList = baseDao.getObjectListByField(OpenTheatreSeat.class, "areaid", seatArea.getId());
		baseDao.removeObjectList(oldSeatList);
		hibernateTemplate.flush();
		baseDao.saveObjectList(oseatList);
		hibernateTemplate.flush();
		seatArea.setTotal(oseatList.size());
		seatArea.setLimitnum(oseatList.size());
		baseDao.saveObject(seatArea);
		hibernateTemplate.flush();
		dbLogger.warn(userid + "重设场次区域座位：" +seatArea.getDpid() + "[" + seatArea.getId() + "]"+ seatArea.getSeller() );
		return ErrorCode.getSuccessReturn(msgList);
	}

	@Override
	public Integer refreshSellSeatId(TheatreSeatArea seatArea) {
		String update = "UPDATE WEBDATA.OPEN_THEATRESEAT T SET RECORDID=(SELECT RECORDID FROM WEBDATA.SELLDRAMASEAT S WHERE T.SEATLINE=S.SEATLINE AND T.SEATRANK=S.SEATRANK AND T.AREAID=S.AREAID) " +
				"WHERE T.AREAID= ? AND EXISTS(SELECT RECORDID FROM WEBDATA.SELLDRAMASEAT S WHERE T.SEATLINE=S.SEATLINE AND T.SEATRANK=S.SEATRANK AND T.AREAID=S.AREAID)";
		int sell = jdbcTemplate.update(update, seatArea.getId());
		return sell;
	}

	@Override
	public ErrorCode<List<TheatreSeatArea>> updateShowAreaVo(Long userid, UpdateDpiContainer container, DramaPlayItem dpi, List<ShowAreaVo> areaVoList, final List<String> msgList) {
		if(CollectionUtils.isEmpty(areaVoList)) {
			msgList.add("场次：项目-->" + dpi.getDramaname() + ",场馆-->" + dpi.getTheatrename() + ", playtime-->" + DateUtil.formatTimestamp(dpi.getPlaytime()) + "没有下载到场区信息！");
			return ErrorCode.SUCCESS;
		}
		List<TheatreSeatArea> newAreaList = new ArrayList<TheatreSeatArea>();
		List<TheatreSeatArea> areaList = baseDao.getObjectListByField(TheatreSeatArea.class, "dpid", dpi.getId());
		Map<String,TheatreSeatArea> areaMap = BeanUtil.beanListToMap(areaList, "sellerseq");
		String msg = "";
		for (ShowAreaVo areaVo : areaVoList) {
			TheatreSeatArea seatArea = areaMap.remove(areaVo.getSaseqNo());
			boolean isAdd = false;
			if(seatArea == null){
				seatArea = new TheatreSeatArea(dpi.getId());
				msg += ",新增区域：" + areaVo.getAreaname();
			}
			ChangeEntry changeEntry = new ChangeEntry(seatArea);
			DramaRemoteUtil.copyTheateSeatArea(seatArea, dpi, areaVo);
			baseDao.saveObject(seatArea);
			if(!isAdd && !changeEntry.getChangeMap(seatArea).isEmpty()){
				msg += ",修改区域：" + seatArea.getAreaname();
				container.addUpdateArea(dpi.getId(), seatArea);
			}
			newAreaList.add(seatArea);
			monitorService.saveChangeLog(userid, TheatreSeatArea.class, seatArea.getId(), changeEntry.getChangeMap(seatArea));
		}
		for (TheatreSeatArea seatArea : areaMap.values()) {
			if(!StringUtils.equals(seatArea.getStatus(), Status.N)){
				ChangeEntry changeEntry = new ChangeEntry(seatArea);
				seatArea.setStatus(Status.N);
				baseDao.saveObject(seatArea);
				msg += ",刪除区域：" + seatArea.getAreaname();
				monitorService.saveChangeLog(userid, TheatreSeatArea.class, seatArea.getId(), changeEntry.getChangeMap(seatArea));
			}
		}
		if(StringUtils.isNotBlank(msg)){
			msg = "场次：项目-->" + dpi.getDramaname() + ",场馆-->" + dpi.getTheatrename() + ", playtime-->" + DateUtil.formatTimestamp(dpi.getPlaytime()) + "," + msg;
			msgList.add(msg);
		}
		return ErrorCode.getSuccessReturn(newAreaList);
	}
	
	@Override
	public ErrorCode updateSeatPriceVo(Long userid, UpdateDpiContainer container, TheatreSeatArea seatArea, List<ShowPriceVo> priceVoList) {
		if(CollectionUtils.isEmpty(priceVoList)) {
			return ErrorCode.getFailure("区域：" + seatArea.getAreaname() + "没有下载到价格列表！");
		}
		List<TheatreSeatPrice> priceList = baseDao.getObjectListByField(TheatreSeatPrice.class, "areaid", seatArea.getId());
		String seattype = SeatConstant.SEAT_TYPE_A;
		int index = -1;
		if(!priceList.isEmpty()){
			Collections.sort(priceList, new MultiPropertyComparator(new String[]{"seattype"}, new boolean[]{false}));
			seattype = priceList.get(0).getSeattype();
			index = OdiConstant.SEATTYPE_LIST.indexOf(seattype);
		}
		Map<String,TheatreSeatPrice> priceMap = BeanUtil.beanListToMap(priceList, "sispseq");
		Collections.sort(priceVoList, new MultiPropertyComparator(new String[]{"price"}, new boolean[]{true}));
		ErrorCode<DramaSettle> settleCode = settleService.addDramaSettle(userid, seatArea.getDramaid(), OdiConstant.DEFAULT_DISCOUNT, SettleConfigConstant.DISCOUNT_TYPE_PERCENT);
		if(!settleCode.isSuccess()) return ErrorCode.getFailure(settleCode.getErrcode(), settleCode.getMsg());
		DramaSettle settle = settleCode.getRetval();
		String msg = "";
		for (ShowPriceVo priceVo : priceVoList) {
			TheatreSeatPrice seatPrice = priceMap.remove(priceVo.getSispseq());
			ChangeEntry changeEntry = null;
			if(seatPrice == null){
				index ++;
				String type = OdiConstant.SEATTYPE_LIST.get(index);
				seatPrice = new TheatreSeatPrice(seatArea.getDpid(), seatArea.getId(), type, 0, priceVo.getPartner());
				seatPrice.setDramaid(seatArea.getDramaid());
				seatPrice.setSettleid(settle.getSettleid());
				msg += ",新增价格：" + priceVo.getPrice() + ",total:" + priceVo.getTicketTotal() + ",limit:" + priceVo.getTicketLimit();
			}else{
				changeEntry = new ChangeEntry(seatPrice);
			}
			Integer oldPrice = seatPrice.getTheatreprice();
			DramaRemoteUtil.copyTheatreSeatPrice(seatPrice, priceVo);
			if(changeEntry != null){
				if(!changeEntry.getChangeMap(seatPrice).isEmpty()){
					msg += ",修改价格：" + oldPrice + " --> " + priceVo.getPrice() + "[" + seatPrice.getSeattype() +"], status:" + seatPrice.getStatus();
					monitorService.saveChangeLog(userid, TheatreSeatPrice.class, seatPrice.getId(), changeEntry.getChangeMap(seatPrice));
				}
			}else{
				seatPrice.setAllowaddnum(priceVo.getTicketLimit());
				monitorService.saveAddLog(userid, TheatreSeatPrice.class, seatPrice.getId(), seatPrice);
			}
			baseDao.saveObject(seatPrice);
		}
		for (TheatreSeatPrice seatPrice : priceMap.values()) {
			if(!StringUtils.equals(seatPrice.getStatus(), Status.DEL)){
				ChangeEntry changeEntry = new ChangeEntry(seatPrice);
				seatPrice.setStatus(Status.DEL);
				baseDao.saveObject(seatPrice);
				msg += ",刪除价格：" + seatPrice.getTheatreprice() + "[" + seatPrice.getSeattype() +"], status:" + seatPrice.getStatus();
				monitorService.saveChangeLog(userid, TheatreSeatPrice.class, seatPrice.getId(), changeEntry.getChangeMap(seatPrice));
			}
		}
		String tmpMsg = "";
		if(StringUtils.isNotBlank(msg)){
			tmpMsg = "区域：" + seatArea.getAreaname() + "[" + seatArea.getId()+"]" + msg;
			ChangeEntry changeEntry = new ChangeEntry(seatArea);
			seatArea.setStatus(Status.N);
			baseDao.saveObject(seatArea);
			monitorService.saveChangeLog(userid, TheatreSeatArea.class, seatArea.getId(), changeEntry.getChangeMap(seatArea));
			container.addUpdateArea(seatArea.getDpid(), seatArea);
		}
		return ErrorCode.getSuccess(tmpMsg);
	}
	
	@Override	
	public ErrorCode<List<String>> updateShowPackPriceVo(Long userid, UpdateDpiContainer container, TheatreSeatArea seatArea, List<ShowPackPriceVo> packPriceVoList){
		if(CollectionUtils.isEmpty(packPriceVoList)) {
			return ErrorCode.SUCCESS;
		}
		List<TheatreSeatPrice> priceList = baseDao.getObjectListByField(TheatreSeatPrice.class, "areaid", seatArea.getId());
		Map<String,TheatreSeatPrice> priceMap = BeanUtil.beanListToMap(priceList, "sispseq");
		List<DisQuantity> disList = baseDao.getObjectListByField(DisQuantity.class, "areaid", seatArea.getId());
		Map<String,DisQuantity> disMap = BeanUtil.beanListToMap(disList, "sispseq");
		List<String> msgList = new ArrayList<String>();
		ErrorCode<DramaSettle> settleCode = settleService.addDramaSettle(userid, seatArea.getDramaid(), OdiConstant.DEFAULT_DISCOUNT, SettleConfigConstant.DISCOUNT_TYPE_PERCENT);
		if(!settleCode.isSuccess()) return ErrorCode.getFailure(settleCode.getErrcode(), settleCode.getMsg());
		DramaSettle settle = settleCode.getRetval();
		Timestamp cur = DateUtil.getCurFullTimestamp();
		Set<TheatreSeatPrice> seatPriceSet = new HashSet<TheatreSeatPrice>();
		for (ShowPackPriceVo packPriceVo : packPriceVoList) {
			TheatreSeatPrice seatPrice = priceMap.get(packPriceVo.getSispseq());
			if(seatPrice == null){
				msgList.add("套票：" + packPriceVo.getName() + "[" +packPriceVo.getSispseq()+ "]价格不存在！");
				continue;
			}
			DisQuantity disQuantity = disMap.remove(packPriceVo.getPackpseq());
			ChangeEntry changeEntry = null;
			if(disQuantity == null){
				disQuantity = new DisQuantity(seatPrice, packPriceVo.getQuantity(), OdiConstant.DISQUANTITY_DITYPE_P);
				disQuantity.setSettleid(settle.getSettleid());
			}else{
				changeEntry = new ChangeEntry(disQuantity);
			}
			DramaRemoteUtil.copyDisQuantity(disQuantity, packPriceVo);
			if(seatPrice.hasRetail() && !disQuantity.hasRetail()){
				seatPrice.setStatus(disQuantity.getRetail());
				seatPrice.setUpdatetime(cur);
				seatPriceSet.add(seatPrice);
			}
			if(changeEntry != null){
				if(!changeEntry.getChangeMap(disQuantity).isEmpty()){
					disQuantity.setUpdatetime(cur);
					msgList.add("修改价格套票：" + disQuantity.getTheatreprice() + "[" + disQuantity.getId() +"]");
					monitorService.saveChangeLog(userid, DisQuantity.class, disQuantity.getId(), changeEntry.getChangeMap(disQuantity));
				}
			}else{
				msgList.add("增加价格套票：" + disQuantity.getTheatreprice() + "[" + disQuantity.getId() +"]");
				monitorService.saveAddLog(userid, DisQuantity.class, disQuantity.getId(), disQuantity);
			}
			baseDao.saveObject(disQuantity);
		}
		baseDao.saveObjectList(seatPriceSet);
		for (DisQuantity disQuantity : disMap.values()) {
			if(!disQuantity.hasStatus(Status.N)){
				ChangeEntry changeEntry = new ChangeEntry(disQuantity);
				disQuantity.setStatus(Status.N);
				baseDao.saveObject(disQuantity);
				msgList.add("刪除价格套票：" + disQuantity.getTheatreprice() + "[" + disQuantity.getId() +"], status:" + disQuantity.getStatus());
				monitorService.saveChangeLog(userid, TheatreSeatPrice.class, disQuantity.getId(), changeEntry.getChangeMap(disQuantity));
			}
		}
		return ErrorCode.getSuccessReturn(msgList);
	}
	
	@Override
	public List<String> updateRoomSeatList(TheatreRoom room, List<FieldAreaSeatVo> seatVoList, boolean forceUpdate){
		List<String> msgList = new ArrayList<String>();
		if(!forceUpdate || CollectionUtils.isEmpty(seatVoList)){
			msgList.add("没有要更新的数据！");
			return msgList;
		}else{ //更新座位图
			int maxRow=0, maxRank=0, minRow = 1, minRank = 1;  int count=0;
			List<TheatreRoomSeat> removeList = new ArrayList<TheatreRoomSeat>();
			List<TheatreRoomSeat> addList = new ArrayList<TheatreRoomSeat>();
			int updated = 0;
			count += seatVoList.size();
			Map<String,TheatreRoomSeat> srMap = getRoomSeatMapByRoomid(room.getId());
			for(FieldAreaSeatVo seat : seatVoList){
				maxRow = Math.max(maxRow, seat.getLineno());
				maxRank = Math.max(maxRank, seat.getRankno());
				minRow = Math.min(minRow, seat.getLineno());
				minRank = Math.min(minRank, seat.getRankno());
				TheatreRoomSeat as = srMap.remove(seat.getLineno()+","+seat.getRankno());
				String msg="";
				boolean addnew = false;
				if(as!=null){
					if(!StringUtils.equals(as.getSeatline(), seat.getSeatline())){
						msg += "行编号" + as.getSeatline() + "->" + seat.getSeatline();
					}
					if(!StringUtils.equals(as.getSeatrank(), seat.getSeatrank())){
						msg += "列编号" + as.getSeatrank() +"->" + seat.getSeatrank();
					}
					if(StringUtils.equals(seat.getSeatline(),"0") && StringUtils.equals(seat.getSeatrank(),"0")){
						msg += "走廊:" + "行编号" + as.getSeatline() + "->" + seat.getSeatrank();
						msg += "," + "列编号" + as.getSeatrank() +"->" + seat.getSeatrank();
					}
					if(StringUtils.isNotBlank(msg)){
						msgList.add("修改座位：" + msg + "\n");
						removeList.add(as);
						addnew = true;
						updated ++;
					}
				}else{
					addnew =true;
				}
				
				if(addnew) {
					if(StringUtils.equals(seat.getSeatline(),"0") && StringUtils.equals(seat.getSeatrank(),"0")){
						msgList.add("走廊" + seat.getSeatline() + "," + seat.getSeatrank() +"\n");
					}else{
						as = new TheatreRoomSeat(room.getId(), seat.getLineno(), seat.getRankno());
						//默认为 0 非情侣座
						as.setLoveInd("0");
						as.setSeatline(seat.getSeatline());
						as.setSeatrank(seat.getSeatrank());
						msgList.add("增加座位" + seat.getLineno() + "," + seat.getRankno() +"\n");
						addList.add(as);
					}
				}
			}
			for(Map.Entry<String, TheatreRoomSeat> map:srMap.entrySet()){
				removeList.add(map.getValue());
				msgList.add("删除座位" + map.getValue().getSeatLabel() + "\n");
			}
			//x,y : 0,0
			room.setRanknum(maxRank - minRank +1);
			room.setLinenum(maxRow - minRow +1);
			room.setFirstline(minRow);
			room.setFirstrank(minRank);
			room.setSeatnum(count);
			if(updated !=0 || removeList.size()!=0 || addList.size()!=0){//有更新
				room.setUpdatetime(new Timestamp(System.currentTimeMillis()));
			}
			baseDao.saveObject(room);
			baseDao.removeObjectList(removeList);
			hibernateTemplate.flush();
			baseDao.addObjectList(addList);
			hibernateTemplate.flush();
			msgList.add("座位数：" + room.getSeatnum() + "[" + count + "]座位行数：" + maxRow + "，座位列数：" + maxRank + "，场区：" + room.getRoomname());
			return msgList;
		}
	}
	private Map<String,TheatreRoomSeat> getRoomSeatMapByRoomid(Long roomid){
		List<TheatreRoomSeat> roomSeatList = baseDao.getObjectListByField(TheatreRoomSeat.class, "roomid", roomid);
		Map<String,TheatreRoomSeat> seatMap = new HashMap<String,TheatreRoomSeat>();
		for(TheatreRoomSeat rs : roomSeatList){
			seatMap.put(rs.getLineno()+","+rs.getRankno(), rs);
		}
		return seatMap;
	}

}
