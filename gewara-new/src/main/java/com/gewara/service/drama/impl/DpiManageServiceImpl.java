package com.gewara.service.drama.impl;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.ProjectionList;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.gewara.constant.Flag;
import com.gewara.constant.OdiConstant;
import com.gewara.constant.Status;
import com.gewara.constant.TheatreSeatConstant;
import com.gewara.constant.sys.CacheConstant;
import com.gewara.constant.ticket.OpiConstant;
import com.gewara.constant.ticket.SeatConstant;
import com.gewara.helper.DramaSeatStatusUtil;
import com.gewara.model.drama.Drama;
import com.gewara.model.drama.OpenDramaItem;
import com.gewara.model.drama.OpenTheatreSeat;
import com.gewara.model.drama.TheatreProfile;
import com.gewara.model.drama.TheatreSeatArea;
import com.gewara.model.drama.TheatreSeatPrice;
import com.gewara.model.drama.TspSaleCount;
import com.gewara.service.drama.DpiManageService;
import com.gewara.service.impl.BaseServiceImpl;
import com.gewara.untrans.CacheService;
import com.gewara.untrans.monitor.MonitorService;
import com.gewara.util.BeanUtil;
import com.gewara.util.ChangeEntry;
import com.gewara.util.DateUtil;
import com.gewara.util.JsonUtils;
import com.gewara.util.StringUtil;

@Service("dpiManageService")
public class DpiManageServiceImpl extends BaseServiceImpl implements DpiManageService {
	
	@Autowired@Qualifier("monitorService")
	protected MonitorService monitorService;
	
	@Autowired@Qualifier("cacheService")
	private CacheService cacheService;
	
	public TheatreSeatArea updateSeatAreaStats(OpenDramaItem odi, TheatreSeatArea seatArea, List<String> remoteLockList, boolean isFinished){
		if(odi.isOpenseat()){
			return updateTheatreSeatAreaStats(odi, seatArea, remoteLockList, isFinished);
		}else{
			return updateTheatreSeatPriceStats(odi, seatArea, remoteLockList, isFinished);
		}
	}
	
	@Override
	public TheatreSeatArea updateTheatreSeatAreaStats(OpenDramaItem odi, TheatreSeatArea seatArea, List<String> remoteLockList, boolean isFinished){
		Timestamp cur = new Timestamp(System.currentTimeMillis());
		if(!odi.isOpenseat()) return seatArea;
		if(DateUtil.addHour(odi.getPlaytime(), 48).before(cur)) return seatArea;//过期48小时的不更新
		final String query1 = "select seatline||':'||seatrank from OpenTheatreSeat o where o.dpid = ? and o.areaid=? and o.status in ('" + StringUtils.join(TheatreSeatConstant.STATUS_LOCK_LIST, "','") + "') ";
		List<String> gewalockList = hibernateTemplate.find(query1, seatArea.getDpid(), seatArea.getId());
		String sqlqry = "SELECT sum(o.quantity) from WEBDATA.ticket_order o where o.status='paid_success' and o.order_type='drama' and o.relatedid=? and o.areaid=? ";
		Integer gsellnum = 0;
		try{
			gsellnum = jdbcTemplate.queryForInt(sqlqry, seatArea.getDpid(), seatArea.getId());
		}catch(Exception e){
			dbLogger.warn(StringUtil.getExceptionTrace(e));
		}
		gewalockList.removeAll(remoteLockList);
		Integer csellnum = remoteLockList.isEmpty()? 0: remoteLockList.size() - gsellnum;
		ChangeEntry changeEntry = new ChangeEntry(seatArea);
		seatArea.setGsellnum(gsellnum);
		seatArea.setLocknum(gewalockList.size());
		seatArea.setCsellnum(csellnum>0?csellnum:0);
		seatArea.setUpdatetime(cur);
		if(isFinished) seatArea.setOtherinfo(JsonUtils.addJsonKeyValue(seatArea.getOtherinfo(), OpiConstant.STATISTICS, "true"));
		if(!changeEntry.getChangeMap(seatArea).isEmpty()){
			seatArea.setUpdatetime(DateUtil.getCurFullTimestamp());
			baseDao.saveObject(seatArea);
		}
		return seatArea;
	}
	
	@Override
	public TheatreSeatArea updateTheatreSeatPriceStats(OpenDramaItem odi, TheatreSeatArea seatArea, List<String> remoteLockList, boolean isFinished){
		Timestamp cur = DateUtil.getCurFullTimestamp();
		if(!odi.isOpenprice()) return seatArea;
		if(DateUtil.addHour(odi.getPlaytime(), 48).before(cur) && odi.hasPeriod(Status.Y) 
			|| DateUtil.addHour(odi.getEndtime(), 48).before(cur) && odi.hasPeriod(Status.N)) return seatArea;//过期48小时的不更新
		List<TheatreSeatPrice> seatPriceList = baseDao.getObjectListByField(TheatreSeatPrice.class, "areaid", seatArea.getId());
		Integer totalnum = 0, gsellnum = 0, remoteLockNum = 0, lockNum = 0;
		Map<String, Integer> lockMap = new HashMap<String, Integer>();
		List<String> msgList = new ArrayList<String>();
		List<TspSaleCount> tscList = baseDao.getObjectListByField(TspSaleCount.class, "dpid", odi.getDpid());
		List<TspSaleCount> removeList = new ArrayList<TspSaleCount>();
		Map<Long, Integer> priceLockMap = new HashMap<Long, Integer>();
		for (TspSaleCount tspSaleCount : tscList) {
			if(tspSaleCount.getValidtime().before(cur)){
				removeList.add(tspSaleCount);
			}else{
				Integer quantity = priceLockMap.get(tspSaleCount.getPriceid());
				if(quantity == null){
					quantity = 0;
				}
				quantity += tspSaleCount.getSales();
				priceLockMap.put(tspSaleCount.getPriceid(), quantity);
			}
		}
		baseDao.removeObjectList(removeList);
		hibernateTemplate.flush();
		try{
			for (String remoteLock : remoteLockList) {
				if(StringUtils.isBlank(remoteLock)) continue;
				List<String> lockList = Arrays.asList(StringUtils.split(remoteLock, ":"));
				if(lockList.size() != 2){
					dbLogger.warn("theatreSeatArea:" + seatArea.getId() + ",remoteLock:" + remoteLock);
					continue;
				}
				Integer locknum = Integer.valueOf(lockList.get(1));
				remoteLockNum += locknum;
				lockMap.put(lockList.get(0), locknum);
			}
			for (TheatreSeatPrice seatPrice: seatPriceList) {
				totalnum += seatPrice.getQuantity();
				ChangeEntry changeEntry = new ChangeEntry(seatPrice);
				Integer priceLock = priceLockMap.get(seatPrice.getId());
				priceLock = (priceLock == null? 0 : priceLock);
				lockNum += priceLock;
				gsellnum += seatPrice.getSales();
				Integer sumnum = lockMap.get(seatPrice.getSispseq());
				sumnum = (sumnum == null ? 0 : sumnum);
				Integer csellnum = sumnum - priceLock - seatPrice.getSales();
				Integer allowaddnum = seatPrice.getQuantity() - csellnum - seatPrice.getSales();
				if(odi.hasGewara()){
					allowaddnum = seatPrice.getQuantity() - priceLock - seatPrice.getSales();
				}
				allowaddnum = allowaddnum <0 ? 0 : allowaddnum;
				csellnum = csellnum < 0 ? 0 : csellnum;
				Integer oldAllowaddnum = seatPrice.getAllowaddnum();
				Integer oldCsellnum = seatPrice.getCsellnum();
				seatPrice.setAllowaddnum(allowaddnum);
				seatPrice.setCsellnum(csellnum);
				if(!changeEntry.getChangeMap(seatPrice).isEmpty()){
					String tmp = "theatreSeatArea: " + seatArea.getId() +", theatreSeatPrice:" + seatPrice.getId() 
							+ ", allowaddnum :" + oldAllowaddnum + "--->" + allowaddnum + ",csellnum :" + oldCsellnum + " ---> " + csellnum;
					msgList.add(tmp);
				}
			}
		}catch(Exception e){
			dbLogger.warn(StringUtil.getExceptionTrace(e));
		}
		baseDao.saveObjectList(seatPriceList);
		Integer csellnum = remoteLockNum - gsellnum - lockNum;
		csellnum = csellnum>0? csellnum : 0;
		Integer oldGsellnum = seatArea.getGsellnum(), oldLocknum = seatArea.getLocknum(), oldCsellnum = seatArea.getCsellnum(); 
		ChangeEntry changeEntry = new ChangeEntry(seatArea);
		seatArea.setTotal(totalnum);
		seatArea.setLimitnum(totalnum);
		seatArea.setGsellnum(gsellnum);
		seatArea.setLocknum(lockNum);
		seatArea.setCsellnum(csellnum);
		if(isFinished) seatArea.setOtherinfo(JsonUtils.addJsonKeyValue(seatArea.getOtherinfo(), OpiConstant.STATISTICS, "true"));
		if(!changeEntry.getChangeMap(seatArea).isEmpty()){
			String tmp = "theatreSeatArea: " + seatArea.getId() +", gsellnum:" + oldGsellnum + " ---> " + gsellnum
					+ ", locknum :" + oldLocknum + "--->" + lockNum + ",csellnum :" + oldCsellnum + " ---> " + csellnum;
			msgList.add(tmp);
			seatArea.setUpdatetime(cur);
			baseDao.saveObject(seatArea);
		}
		if(!msgList.isEmpty()){
			dbLogger.warn(StringUtils.join(msgList, ","));
		}
		return seatArea;
	}
	
	@Override
	public void refreshDramaOtherinfo(Long userid, Drama drama){
		ChangeEntry changeEntry = new ChangeEntry(drama);
		DetachedCriteria query = DetachedCriteria.forClass(OpenDramaItem.class, "o");
		query.add(Restrictions.eq("o.dramaid", drama.getId()));
		query.add(Restrictions.eq("o.status", Status.Y));
		ProjectionList projectionList = Projections.projectionList();
		projectionList.add(Projections.property("o.opentype"), "opentype");
		projectionList.add(Projections.property("o.takemethod"), "takemethod");
		projectionList.add(Projections.property("o.maxpoint"), "maxpoint");
		projectionList.add(Projections.property("o.elecard"), "elecard");
		query.setProjection(projectionList);
		query.setResultTransformer(DetachedCriteria.ALIAS_TO_ENTITY_MAP);
		List<Map> mapList = hibernateTemplate.findByCriteria(query);
		boolean lineSeat = false, express = false, eticket = false, pointpay = false, cardpay = false;
		Map<String, String> otherInfoMap = JsonUtils.readJsonToMap(drama.getOtherinfo());
		for (Map map : mapList) {
			if(!lineSeat){
				String opentype = String.valueOf(map.get("opentype"));
				if(StringUtils.equals(opentype, OdiConstant.OPEN_TYPE_SEAT)){
					lineSeat = true;
				}
			}
			if(!express){
				String takemethod = String.valueOf(map.get("takemethod"));
				if(StringUtils.contains(takemethod, TheatreProfile.TAKEMETHOD_E)){
					express = true;
				}
			}
			if(!eticket){
				String takemethod = String.valueOf(map.get("takemethod"));
				if(StringUtils.contains(takemethod, TheatreProfile.TAKEMETHOD_A)){
					eticket = true;
				}
			}
			if(!pointpay){
				Integer maxpoint = (Integer)map.get("maxpoint");
				if(maxpoint != null && maxpoint> 0){
					pointpay = true;
				}
			}
			if(!cardpay){
				String elecard = String.valueOf(map.get("elecard"));
				if(StringUtils.contains("ABD", elecard)){
					cardpay = true;
				}
			}
		}
		if(lineSeat){
			otherInfoMap.put(Flag.SERVICE_LINESEAT, String.valueOf(lineSeat));
		}else{
			otherInfoMap.remove(Flag.SERVICE_LINESEAT);
		}
		if(express){
			otherInfoMap.put(Flag.SERVICE_EXPRESS, String.valueOf(express));
		}else{
			otherInfoMap.remove(Flag.SERVICE_EXPRESS);
		}
		if(eticket){
			otherInfoMap.put(Flag.SERVICE_ETICKET, String.valueOf(eticket));
		}else{
			otherInfoMap.remove(Flag.SERVICE_ETICKET);
		}
		if(pointpay){
			otherInfoMap.put(Flag.SERVICE_POINTPAY, String.valueOf(pointpay));
		}else{
			otherInfoMap.remove(Flag.SERVICE_POINTPAY);
		}
		if(cardpay){
			otherInfoMap.put(Flag.SERVICE_CARDPAY, String.valueOf(cardpay));
		}else{
			otherInfoMap.remove(Flag.SERVICE_CARDPAY);
		}
		drama.setOtherinfo(JsonUtils.writeMapToJson(otherInfoMap));
		if(!changeEntry.getChangeMap(drama).isEmpty()){
			baseDao.saveObject(drama);
			monitorService.saveChangeLog(userid, Drama.class, drama.getId(), changeEntry.getChangeMap(drama));
		}
	}
	
	@Override
	public int verifySeatAreaSeatLock(Long areaid){
		String query1 = "from OpenTheatreSeat o where o.areaid = ? and o.status in ('" + 
				StringUtils.join(TheatreSeatConstant.STATUS_LOCK_LIST, "','") + 
				"') and exists (select id from SellDramaSeat s where s.status= ? and s.id = o.id) ";
		List<OpenTheatreSeat> oseatList = hibernateTemplate.find(query1, areaid, TheatreSeatConstant.STATUS_SOLD);
		if(oseatList.size() > 0){
			TheatreSeatArea seatArea = baseDao.getObject(TheatreSeatArea.class, areaid);
			seatArea.setLocknum(seatArea.getLocknum() - oseatList.size());
			baseDao.updateObject(seatArea);
			for(OpenTheatreSeat seat: oseatList){
				seat.setStatus(TheatreSeatConstant.STATUS_NEW);
				baseDao.updateObject(seat);
			}
		}
		return oseatList.size();
	}
	
	@Override
	public void updateAreaSeatMap(TheatreSeatArea seatArea, List<OpenTheatreSeat> openSeatList, List<String> hfhLockList, DramaSeatStatusUtil seatStatusUtil) {
		Long updatetime = (Long) cacheService.get(CacheConstant.REGION_ONEDAY, TheatreSeatConstant.SEATMAP_UPDATE + seatArea.getId());
		Long cur = System.currentTimeMillis();
		if(updatetime==null || updatetime < cur - DateUtil.m_second*30){//30秒内未更新过
			String seatStr = getAreaSeatStr(seatArea, openSeatList, hfhLockList, seatStatusUtil);
			cacheService.set(CacheConstant.REGION_ONEDAY, TheatreSeatConstant.SEATMAP_KEY + seatArea.getId(), seatStr);
			cacheService.set(CacheConstant.REGION_ONEDAY, TheatreSeatConstant.SEATMAP_UPDATE + seatArea.getId(), cur);
		}
	}
	
	private String getAreaSeatStr(TheatreSeatArea seatArea, List<OpenTheatreSeat> openSeatList, List<String> hfhLockList, DramaSeatStatusUtil seatStatusUtil){
		Map<String, OpenTheatreSeat> seatMap = BeanUtil.beanListToMap(openSeatList, "position");
		OpenTheatreSeat oseat = null;
		String status = "";
		List<String> lineList = new ArrayList<String>();
		for(int i=seatArea.getFirstline(); i<= seatArea.getLinenum(); i++){
			List<String> seatRankList = new ArrayList<String>();
			for(int j=seatArea.getFirstrank(); j<= seatArea.getRanknum(); j++){
				oseat = seatMap.get(i + ":" + j);
				if(oseat == null){
					status = "ZL"; //走廊
				}else{
					if(hfhLockList.contains(oseat.getKey())){ 
						status = "LK"; //锁定
					}else if(SeatConstant.STATUS_NEW.equals(seatStatusUtil.getFullStatus(oseat))){
						status = seatStatusUtil.getFullStatus(oseat);
					}else{
						status = "LK"; //锁定
					}
				}
				seatRankList.add(status);
			}
			lineList.add(StringUtils.join(seatRankList, ","));
		}
		return StringUtils.join(lineList, "@@");
	}
	
	@Override
	public String[] getAreaSeatMap(Long areaid){
		String seatMap = (String) cacheService.get(CacheConstant.REGION_ONEDAY, TheatreSeatConstant.SEATMAP_KEY + areaid);
		if(StringUtils.isNotBlank(seatMap)){
			Long updatetime = (Long) cacheService.get(CacheConstant.REGION_ONEDAY, TheatreSeatConstant.SEATMAP_UPDATE + areaid);
			return new String[]{seatMap, updatetime==null?"":DateUtil.format(new Timestamp(updatetime), "HH:mm:ss")};
		}
		return null;
	}
	
}
