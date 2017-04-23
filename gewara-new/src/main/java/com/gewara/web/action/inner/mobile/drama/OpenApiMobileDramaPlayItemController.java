package com.gewara.web.action.inner.mobile.drama;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import com.gewara.constant.ApiConstant;
import com.gewara.constant.OdiConstant;
import com.gewara.constant.ticket.PartnerConstant;
import com.gewara.helper.DramaSeatStatusUtil;
import com.gewara.helper.TspHelper;
import com.gewara.helper.api.GewaApiDramaHelper;
import com.gewara.model.api.ApiUser;
import com.gewara.model.drama.DisQuantity;
import com.gewara.model.drama.Drama;
import com.gewara.model.drama.DramaPlayItem;
import com.gewara.model.drama.OpenDramaItem;
import com.gewara.model.drama.OpenTheatreSeat;
import com.gewara.model.drama.SellDramaSeat;
import com.gewara.model.drama.TheatreField;
import com.gewara.model.drama.TheatreSeatArea;
import com.gewara.model.drama.TheatreSeatPrice;
import com.gewara.model.express.ExpressProvince;
import com.gewara.support.ErrorCode;
import com.gewara.util.BeanUtil;
import com.gewara.util.DateUtil;
import com.gewara.web.action.api.ApiAuth;
import com.gewara.web.action.inner.mobile.BaseOpenApiMobileDramaController;
import com.gewara.web.filter.OpenApiMobileAuthenticationFilter;
@Controller
public class OpenApiMobileDramaPlayItemController extends BaseOpenApiMobileDramaController{
	private Map<String, Object> getMoreDramaPlayItemData(DramaPlayItem dpi){
		Map<String, Object> resMap = new HashMap<String, Object>();
		TheatreField field = daoService.getObject(TheatreField.class, dpi.getRoomid());
		if(field!=null) resMap.put("fieldlogo", field.getLogo());
		int booking = 0;
		OpenDramaItem odi = daoService.getObjectByUkey(OpenDramaItem.class, "dpid", dpi.getId());
		int maxbuy = 0;
		if(odi!=null) {
			if(odi.isBooking()) booking = 1;
			resMap.put("elecard", odi.getElecard());
			resMap.put("expressid", odi.getExpressid());
			resMap.put("takemethod", dramaOrderService.getTakemethodByOdi(DateUtil.getCurFullTimestamp(), odi));
			maxbuy = odi.getMaxbuy();
		}
		resMap.put("maxbuy", maxbuy);
		resMap.put("booking", booking);
		return resMap;
	}
	@RequestMapping("/openapi/mobile/drama/calendarList.xhtml")
	public String calendarList(String citycode, String playMonth, ModelMap model) {
		Date playdate = DateUtil.parseDate(playMonth+"-01", "yyyy-MM-dd");
		Map<String, Integer> tdateMap = dramaService.getMonthDramaCountGroupPlaydate(citycode, playdate);
		Map<String, Integer> dateMap = new TreeMap<String, Integer>(tdateMap);
		Map<String, Integer> dateMap2 = new TreeMap<String, Integer>();
		Map<String, List<Drama>> dramaMap = new HashMap<String, List<Drama>>();
		Map<String, String> theatrenamesMap = new HashMap<String, String>();
		for(String key : dateMap.keySet()){
			Date keydate = DateUtil.parseDate(key);
			String tmp = DateUtil.format(keydate, "yyyy-MM");
			if(!StringUtils.equals(playMonth, tmp)){
				continue;
			}
			dateMap2.put(key, dateMap.get(key));
			List<OpenDramaItem> odiList = dramaService.getBookingOdiList(citycode, keydate, null, null);
			List<Long> dramaidList = BeanUtil.getBeanPropertyList(odiList, Long.class, "dramaid", true);
			List<Drama> dramaList = daoService.getObjectList(Drama.class, dramaidList);
			dramaMap.put(key, dramaList);
			for(Drama drama : dramaList){
				List<OpenDramaItem> tmpList = dramaService.getBookingOdiList(citycode, keydate, drama.getId(), null);
				List<String> nameList = BeanUtil.getBeanPropertyList(tmpList, String.class, "theatrename", true);
				theatrenamesMap.put(key+drama.getId(), StringUtils.join(nameList, ","));
			}
		}
		model.put("dateMap", dateMap2);
		model.put("dramaMap", dramaMap);
		model.put("theatrenamesMap", theatrenamesMap);
		return getXmlView(model, "inner/mobile/drama/calList.vm");
	}
	@RequestMapping("/openapi/mobile/dramaPlayItem/playDateList.xhtml")
	public String playDateList(Long dramaid, ModelMap model) {
		List<Date> playdateList = dramaPlayItemService.getDramaPlayMonthDateList(dramaid, true);
		model.put("openDateList", playdateList);
		return getXmlView(model, "inner/mobile/openDateList.vm");
	}
	@RequestMapping("/openapi/mobile/dramaPlayItem/dramaPlayItemList.xhtml")
	public String dramPlayItemList(String citycode, Long dramaid, Long theatreid, Integer from, Integer maxnum, String isbooking, ModelMap model, HttpServletRequest request) {
		List<DramaPlayItem> dpiList = new ArrayList<DramaPlayItem>();
		if(StringUtils.isNotBlank(isbooking)){
			List<OpenDramaItem> odiList = dramaService.getOpenDramaItemListBydramaid(citycode, dramaid, theatreid, true);
			List<Long> dpidList = BeanUtil.getBeanPropertyList(odiList, Long.class, "dpid", false);
			dpiList = daoService.getObjectList(DramaPlayItem.class, dpidList);
		}else {
			dpiList = dramaPlayItemService.getDramaPlayItemList(citycode, null, dramaid, DateUtil.getCurFullTimestamp(), null, null);
		}
		if(from!=null && maxnum!=null) {
			dpiList = BeanUtil.getSubList(dpiList, from, maxnum);
		}
		List<Map<String, Object>> resMapList = new ArrayList<Map<String, Object>>();
		for(DramaPlayItem dpi : dpiList){
			Map<String, Object> resMap = GewaApiDramaHelper.getDramaPlayItemData(dpi);
			resMap.putAll(getMoreDramaPlayItemData(dpi));
			List<TheatreSeatPrice> tspList = dramaPlayItemService.getTspList(dpi.getId());
			List<Integer> priceList = BeanUtil.getBeanPropertyList(tspList, Integer.class, "price", true);
			Collections.sort(priceList);
			resMap.put("prices", StringUtils.join(priceList, ","));
			resMapList.add(resMap);
		}
		model.put("resMapList", resMapList);
		initField(model, request);
		putDramaPlayItemListNode(model);
		return getOpenApiXmlList(model);
	}
	@RequestMapping("/openapi/mobile/dramaPlayItem/dramaPlayItemDetail.xhtml")
	public String dramaPlayDetail(Long dpid, ModelMap model, HttpServletRequest request) {
		DramaPlayItem dpi = daoService.getObject(DramaPlayItem.class, dpid);
		if(dpi==null) return getErrorXmlView(model, ApiConstant.CODE_DATA_ERROR, "场次不存在!");
		Drama drama = daoService.getObject(Drama.class, dpi.getDramaid());
		Map<String, Object> resMap = GewaApiDramaHelper.getDramaPlayItemData(dpi);
		List<TheatreSeatArea> seatAreaList = daoService.getObjectListByField(TheatreSeatArea.class, "dpid", dpid);
		List<TheatreSeatPrice> priceList = dramaPlayItemService.getTspList(dpid);
		Map<Long, List<TheatreSeatPrice>> seatPriceMap = BeanUtil.groupBeanList(priceList, "areaid");
		List<DisQuantity> disquanList = dramaPlayItemService.getDisQuantityListByDpid(dpid);
		List<TheatreSeatPrice> tspList2 = dramaPlayItemService.getTspList(dpid);
		TspHelper tspHelper = new TspHelper(tspList2, disquanList);
		resMap.putAll(getMoreDramaPlayItemData(dpi));
		resMap.put("dramaname", drama.getRealBriefname());
		model.put("resMap", resMap);
		model.put("dpi", dpi);
		model.put("seatAreaList", seatAreaList);
		model.put("seatPriceMap", seatPriceMap);
		model.put("tspHelper", tspHelper);
		initField(model, request);
		return getXmlView(model, "inner/mobile/drama/dramaPlayItem.vm");
	}
	@RequestMapping("/openapi/mobile/dramaPlayItem/expressProvinceList.xhtml")
	public String expressProvinceList(Long dpid, ModelMap model) {
		OpenDramaItem odi = daoService.getObjectByUkey(OpenDramaItem.class, "dpid", dpid);
		List<ExpressProvince> expressProvinceList = daoService.getObjectListByField(ExpressProvince.class, "expressid", odi.getExpressid());
		model.put("expressProvinceList", expressProvinceList);
		return getXmlView(model, "inner/mobile/expressProvinceList.vm");
	}
	@RequestMapping("/openapi/mobile/dramaPlayItem/gpHotzone.xhtml")
	public String calendarList(Long dpid, ModelMap model) {
		OpenDramaItem odi = daoService.getObjectByUkey(OpenDramaItem.class, "dpid", dpid, false);
		if(odi==null) getErrorXmlView(model, ApiConstant.CODE_DATA_ERROR, "场次不存在!");
		TheatreField field = daoService.getObject(TheatreField.class, odi.getRoomid());
		List<TheatreSeatArea> areaList = daoService.getObjectListByField(TheatreSeatArea.class, "dpid", dpid);
		Map<Long, String> priceMap = new HashMap<Long, String>();
		Map<Long, Integer> bookMap = new HashMap<Long, Integer>();
		List<TheatreSeatPrice> tspList = dramaPlayItemService.getTspList(dpid);
		Map<Long, List<TheatreSeatPrice>> seatPriceMap = BeanUtil.groupBeanList(tspList, "areaid");
		for(Long areaid : seatPriceMap.keySet()){
			List<TheatreSeatPrice> tmpList = seatPriceMap.get(areaid);
			List<Integer> priceList = BeanUtil.getBeanPropertyList(tmpList, Integer.class, "price", true);
			priceMap.put(areaid, StringUtils.join(priceList, ","));
			int booking = 0;
			for(TheatreSeatPrice tsp : tmpList){
				if(tsp.hasAllowBooking()){
					booking = 1;
				}
			}
			bookMap.put(areaid, booking);
		}
		model.put("field", field);
		model.put("bookMap", bookMap);
		model.put("priceMap", priceMap);
		model.put("areaList", areaList);
		return getXmlView(model, "inner/mobile/drama/gphotzone.vm");
	}
	@RequestMapping("/openapi/mobile/dramaPlayItem/theatreSeatAreaDetail.xhtml")
	public String theatreSeatAreaDetail(Long areaid, ModelMap model, HttpServletRequest request) {
		TheatreSeatArea area = daoService.getObject(TheatreSeatArea.class, areaid);
		if(area==null) getErrorXmlView(model, ApiConstant.CODE_DATA_ERROR, "区域不存在!");
		Map<String, Object> resMap = GewaApiDramaHelper.getTheatreSeatAreaData(area);
		initField(model, request);
		model.put("root", "theatreSeatArea");
		model.put("resMap", resMap);
		return getOpenApiXmlDetail(model);
	}
	@RequestMapping("/openapi/mobile/dramaPlayItem/seatInfo.xhtml")
	public String dramPlayItemList(Long dpid, Long areaid, ModelMap model, HttpServletRequest request) {
		OpenDramaItem odi = daoService.getObjectByUkey(OpenDramaItem.class, "dpid", dpid, false);
		DramaPlayItem dpi = daoService.getObject(DramaPlayItem.class, dpid);
		if(odi==null || dpi==null) getErrorXmlView(model, ApiConstant.CODE_DATA_ERROR, "场次不存在!");
		if(!odi.isBooking()){
			return getErrorXmlView(model, ApiConstant.CODE_DATA_ERROR, "本场次不接受预定");
		}
		List<OpenTheatreSeat> openSeatList = openDramaService.getOpenTheatreSeatListByDpid(odi.getDpid(), areaid);
		TheatreSeatArea seatArea = daoService.getObject(TheatreSeatArea.class, areaid);
		if(seatArea == null) return getErrorXmlView(model, ApiConstant.CODE_DATA_ERROR, "场区不存在！");
		ApiAuth auth = OpenApiMobileAuthenticationFilter.getOpenApiAuth();
		ApiUser partner = auth.getApiUser();
		Map<String, OpenTheatreSeat> seatMap = new HashMap<String, OpenTheatreSeat>();
		Map<Integer, String> rowMap = new HashMap<Integer, String>();
		List<SellDramaSeat> selleatList = dramaOrderService.getSellDramaSeatList(odi.getDpid(), seatArea.getId());
		DramaSeatStatusUtil seatStatusUtil = new DramaSeatStatusUtil(selleatList);
		List<String> rLockList = new ArrayList<String>(); 
		ErrorCode<List<String>> remoteLockList = theatreOperationService.updateRemoteLockSeat(seatArea, OdiConstant.SECONDS_UPDATE_SEAT, true);
		if(remoteLockList.isSuccess()){
			rLockList = remoteLockList.getRetval();
		}
		Map<String, Integer> typeMap = new HashMap<String, Integer>();
		List<String> seatTypeList = new ArrayList<String>();
		List<TheatreSeatPrice> tspList = dramaPlayItemService.getTspList(dpid,areaid);
		Map<String, TheatreSeatPrice> seatPriceMap = BeanUtil.beanListToMap(tspList, "seattype");
		
		for(OpenTheatreSeat seat:openSeatList){
			rowMap.put(seat.getLineno(), seat.getSeatline());
			seatMap.put("row" + seat.getLineno() + "rank" + seat.getRankno(), seat);
			if(!typeMap.containsKey(seat.getSeattype())){
				typeMap.put(seat.getSeattype(), seat.getPrice());
				seatTypeList.add(seat.getSeattype()+":"+seat.getPrice());
			}
		}
		initField(model, request);
		Map<String, Object> resMap = GewaApiDramaHelper.getDramaPlayItemData(dpi);
		resMap.putAll(getMoreDramaPlayItemData(dpi));
		model.put("resMap", resMap);
		model.put("room", seatArea);
		model.put("seatMap", seatMap);
		model.put("rowMap", rowMap);
		model.put("rLockList", rLockList);
		model.put("seatTypes", StringUtils.join(seatTypeList, ","));
		model.put("seatStatusUtil", seatStatusUtil);
		model.putAll(getMoreDramaPlayItemData(dpi));
		model.put("seatPriceMap", seatPriceMap);
		if(partner.getId().equals(PartnerConstant.GEWAP)){
			return getXmlView(model, "inner/mobile/drama/dpiSeatInfo.vm");
		}else {
			return getXmlView(model, "inner/mobile/drama/dpiSeatInfo2.vm");
		}
		
	}
}
