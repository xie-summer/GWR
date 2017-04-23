package com.gewara.service.api.impl;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.gewara.constant.OpenTimeTableConstant;
import com.gewara.helper.SportSynchHelper;
import com.gewara.model.sport.OpenTimeItem;
import com.gewara.model.sport.OpenTimeTable;
import com.gewara.model.sport.Sport;
import com.gewara.model.sport.Sport2Item;
import com.gewara.model.sport.SportField;
import com.gewara.model.sport.SportItem;
import com.gewara.model.sport.SportProfile;
import com.gewara.service.api.ApiSportService;
import com.gewara.service.impl.BaseServiceImpl;
import com.gewara.service.sport.OpenTimeTableService;
import com.gewara.service.sport.SportOrderService;
import com.gewara.service.sport.SportService;
import com.gewara.support.ErrorCode;
import com.gewara.util.BeanUtil;
import com.gewara.util.DateUtil;
import com.gewara.util.JsonUtils;
import com.gewara.util.ValidateUtil;
import com.gewara.xmlbind.sport.GstOti;
import com.gewara.xmlbind.sport.GstOtt;
import com.gewara.xmlbind.sport.GstSportField;
@Service("apiSportService")
public class ApiSportServiceImpl extends BaseServiceImpl implements ApiSportService{
	@Autowired@Qualifier("sportService")
	private SportService sportService;
	@Autowired@Qualifier("openTimeTableService")
	private OpenTimeTableService openTimeTableService;
	@Autowired@Qualifier("sportOrderService")
	private SportOrderService sportOrderService;
	
	private SportField getSportField(Long sportid, Long itemid, Long remoteid){
		String qry = "from SportField f where f.sportid=? and f.itemid=? and f.remoteid=?";
		List<SportField> sfList = hibernateTemplate.find(qry, sportid, itemid, remoteid);
		if(sfList.isEmpty()) return null;
		return sfList.get(0);
	}
	@Override
	public SportField getSportField(Long sportid, Long itemid, String fieldname){
		String qry = "from SportField f where f.sportid=? and f.itemid=? and f.name=?";
		List<SportField> sfList = hibernateTemplate.find(qry, sportid, itemid, fieldname);
		if(sfList.isEmpty()) return null;
		return sfList.get(0);
	}
	
	private OpenTimeTable getOpenTimeTable(Long sportid,Long remoteid){
		String hql = "from OpenTimeTable ott where ott.sportid =? and ott.remoteid = ?";
		List<OpenTimeTable> openList = hibernateTemplate.find(hql,sportid,remoteid);
		return openList.isEmpty()?null:openList.get(0);
	}
	private List<OpenTimeTable> getOpenTimeTable2(Long sportid,Long remoteid){
		String hql = "from OpenTimeTable ott where ott.sportid =? and ott.remoteid = ?";
		List<OpenTimeTable> openList = hibernateTemplate.find(hql,sportid,remoteid);
		return openList;
	}
	//-------------------------------------------------
	@Override
	public void addSportField(List<GstSportField> gstSportFieldList) {
		if(CollectionUtils.isEmpty(gstSportFieldList)) return;
		for(GstSportField rsf : gstSportFieldList){
			SportField sf = getSportField(rsf.getSportid(), rsf.getItemid(), rsf.getId());
			if(sf==null){
				sf = SportSynchHelper.createSportField(rsf);
			}else {
				if(StringUtils.equalsIgnoreCase(rsf.getStatus(), "delete")) {
					sf.setStatus("N");
				}
				sf.setOrdernum(rsf.getOrdernum());
				sf.setName(rsf.getName());
			}
			baseDao.saveObject(sf);
		}
	}
	@Override
	public ErrorCode<List<OpenTimeItem>> saveSportTimeTable(GstOtt rott) {
		OpenTimeTable ott = getOpenTimeTable(rott.getSportid(), rott.getId());
		if(ott==null){
			ott = SportSynchHelper.createOpenTimeTable(rott);
			Sport sport = baseDao.getObject(Sport.class, rott.getSportid());
			ott.setCitycode(sport.getCitycode());
			SportItem item = baseDao.getObject(SportItem.class, ott.getItemid());
			ott.setSportname(sport.getName());
			ott.setItemname(item.getItemname());
			SportProfile sp = baseDao.getObject(SportProfile.class, rott.getSportid());
			openTimeTableService.clearOttPreferential(ott, sp);
		}else {
			ott.setRstatus(rott.getStatus());
		}
		Sport2Item sport2Item = sportService.getSport2Item(ott.getSportid(), ott.getItemid());
		if(sport2Item == null){//保存场馆项目
			sport2Item = new Sport2Item(ott.getSportid(), ott.getItemid());
			baseDao.saveObject(sport2Item);
		}
		if(StringUtils.isBlank(ott.getRemark())){
			ott.setRemark(sport2Item.getPrompting());
		}
		baseDao.saveObject(ott);
		Map<String, String> otherinfoMap = JsonUtils.readJsonToMap(sport2Item.getOtherinfo());
		String openStatus = otherinfoMap.get(Sport2Item.OPEN_STATUS);
		String openBefore = otherinfoMap.get(Sport2Item.OPEN_BEFORE);
		Map<Long, OpenTimeItem> newOtiMap = new HashMap<Long, OpenTimeItem>();
		Map<String, OpenTimeItem> oldOtiMap = new HashMap<String, OpenTimeItem>();
		Map<Long, SportField> fieldMap = new HashMap<Long, SportField>();
		List<OpenTimeItem> newOtiList = openTimeTableService.getOpenItemList(ott.getId());
		newOtiMap = BeanUtil.beanListToMap(newOtiList, "rotiid");
		Date playdate = DateUtil.addDay(ott.getPlaydate(), -7);
		List<OpenTimeTable> oldOttList = getOttList(ott.getSportid(), ott.getItemid(), playdate, ott.getOpenType());
		if(!oldOttList.isEmpty()){
			OpenTimeTable oldOtt = oldOttList.get(0);
			List<OpenTimeItem> oldOtiList = openTimeTableService.getOpenItemList(oldOtt.getId());
			oldOtiMap = BeanUtil.beanListToMap(oldOtiList, "otiKey");
		}
		List<SportField> fieldList = sportOrderService.getSportFieldList(ott.getSportid(), ott.getItemid());
		fieldMap = BeanUtil.beanListToMap(fieldList, "remoteid");
		ErrorCode<List<OpenTimeItem>> result = saveOtiList(oldOtiMap, newOtiMap, fieldMap, rott.getOtiList(), ott, openStatus, openBefore);
		if(result.isSuccess()){
			ott.setQuantity(rott.getOtiList().size());
			baseDao.saveObject(ott);
		}
		return result;
	}
	
	private ErrorCode<List<OpenTimeItem>> saveOtiList(Map<String, OpenTimeItem> oldOtiMap, Map<Long, OpenTimeItem> newOtiMap, Map<Long, SportField> fieldMap, List<GstOti> gstotiList, OpenTimeTable ott, String openStatus, String openBefore){
		List<OpenTimeItem> itemList = new ArrayList<OpenTimeItem>();
		for (GstOti roti : gstotiList) {
			SportField sf = fieldMap.get(roti.getFieldid());
			if(sf == null){ 
				return ErrorCode.getFailure("为找到此场地，请先同步场地!");
			}
			OpenTimeItem oti = newOtiMap.get(roti.getId());
			if(oti == null) {
				oti = SportSynchHelper.createOpenTimeItem(ott.getSportid(), ott.getItemid(), roti);
				oti.setFieldid(sf.getId());
				oti.setOttid(ott.getId());
				String otiKey = sf.getId() + roti.getHour();
				OpenTimeItem oldOti = oldOtiMap.get(otiKey);
				if(oldOti != null && !oldOti.hasZeroPrice()){//给新场地设置价格
					oti.setCostprice(oldOti.getCostprice());
					oti.setPrice(oldOti.getPrice());
					oti.setSettleid(oldOti.getSettleid());
					oti.setUpsetprice(oldOti.getUpsetprice());
				}
			}else {
				oti.setNorprice(roti.getNorprice());
				oti.setUnitType(roti.getUnitType());
			}
			//判断是否开放
			if(StringUtils.equals(openStatus, OpenTimeTableConstant.STATUS_BOOK) && !StringUtils.equals(ott.getStatus(), OpenTimeTableConstant.STATUS_BOOK) && !oti.hasZeroPrice() && ValidateUtil.isNumber(openBefore)){
				if(!DateUtil.isAfter(DateUtil.addDay(ott.getPlaydate(), -Integer.parseInt(openBefore)))) ott.setStatus(OpenTimeTableConstant.STATUS_BOOK);
			}
			oti.setBindInd(roti.getBindInd2());
			oti.setSaleInd(roti.getBindInd1());
			oti.setItemtype(roti.getOtiType());
			oti.setAuctionprice(roti.getAuctionPrice());
			if(StringUtils.isBlank(oti.getUnitType())||StringUtils.isBlank(oti.getOpenType())||StringUtils.isBlank(oti.getEndhour())){
				oti.setUnitType(OpenTimeTableConstant.UNIT_TYPE_WHOLE);
				oti.setOpenType(ott.getOpenType());
				Timestamp startHour = ott.getPlayTimeByHour(roti.getHour());
				Timestamp endHour = DateUtil.addHour(startHour, 1);
				oti.setEndhour(DateUtil.format(endHour, "HH:mm"));
				oti.setQuantity(1);
				oti.setSales(0);
				oti.setUnitMinute(60);
			}else if(!oti.hasField()){
				Timestamp playtime = ott.getPlayTimeByHour(oti.getEndhour());
				int minutes = 30;
				if(oti.hasUnitTime()){
					minutes = oti.getUnitMinute();
				}
				Timestamp validtime = DateUtil.addMinute(playtime, -minutes);
				oti.setValidtime(validtime);
			}
			oti.setFieldname(sf.getName());
			oti.setStatusByV2(roti.getStatus());
			itemList.add(oti);
		}
		baseDao.saveObjectList(itemList);
		return ErrorCode.getSuccessReturn(itemList);
	}
	
	@Override
	public void modSportTimeTable(GstOtt rott) {
		List<OpenTimeTable> ottList = getOpenTimeTable2(rott.getSportid(), rott.getId());
		for(OpenTimeTable ott : ottList){
			for(GstOti roti : rott.getOtiList()){
				SportField sf = getSportField(ott.getSportid(), ott.getItemid(), roti.getFieldid());
				if(sf==null){
					dbLogger.warn("场地信息为空：sportid=" + ott.getSportid()+", itemid="+ott.getItemid() + ", playdate=" + DateUtil.formatDate(ott.getPlaydate())
							+", rott ottid="+roti.getOttid()+", ikey=" + roti.getIkey());
					return;
				}
				List<OpenTimeItem> otiList = getOtiList(ott.getId(), sf.getId(), roti.getHour());
				for(OpenTimeItem oti : otiList){
					oti.setStatusByV2(roti.getStatus());
					baseDao.saveObject(oti);
				}
			}
		}
	}
	
	private List<OpenTimeTable> getOttList(Long sportid, Long itemid, Date playdate, String openType){
		String qry = "from OpenTimeTable t where t.sportid=? and t.itemid=? and t.playdate=? and t.openType=? order by t.id desc";
		return hibernateTemplate.find(qry, sportid, itemid, playdate, openType);
	}
	private List<OpenTimeItem> getOtiList(Long ottid, Long fieldid, String hour){
		String qry = "from OpenTimeItem o where o.ottid=? and o.fieldid=? and o.hour=?";
		return hibernateTemplate.find(qry, ottid, fieldid, hour);
	}
}
