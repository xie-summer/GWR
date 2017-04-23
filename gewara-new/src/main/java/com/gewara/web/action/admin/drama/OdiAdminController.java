package com.gewara.web.action.admin.drama;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import com.gewara.constant.OdiConstant;
import com.gewara.constant.Status;
import com.gewara.helper.TspHelper;
import com.gewara.model.drama.DramaPlayItem;
import com.gewara.model.drama.OpenDramaItem;
import com.gewara.model.drama.Theatre;
import com.gewara.model.drama.TheatreField;
import com.gewara.model.drama.TheatreSeatArea;
import com.gewara.model.drama.TheatreSeatPrice;
import com.gewara.service.drama.DpiManageService;
import com.gewara.service.drama.DramaPlayItemService;
import com.gewara.support.ErrorCode;
import com.gewara.support.MultiPropertyComparator;
import com.gewara.untrans.drama.OdiOpenService;
import com.gewara.untrans.drama.TheatreOperationService;
import com.gewara.util.BeanUtil;
import com.gewara.util.DateUtil;
import com.gewara.web.action.admin.BaseAdminController;

@Controller
public class OdiAdminController extends BaseAdminController {

	@Autowired@Qualifier("dpiManageService")
	private DpiManageService dpiManageService;
	
	@Autowired@Qualifier("odiOpenService")
	private OdiOpenService odiOpenService;
	
	@Autowired@Qualifier("theatreOperationService")
	private TheatreOperationService theatreOperationService;
	
	@Autowired@Qualifier("dramaPlayItemService")
	private DramaPlayItemService dramaPlayItemService;
	
	@RequestMapping("/admin/dramaTicket/updateAreaStats.xhtml")
	public String updateAreaStats(Long areaid, ModelMap model) {
		TheatreSeatArea seatArea = daoService.getObject(TheatreSeatArea.class, areaid);
		if(seatArea == null) return showJsonError(model, "区域不存在或被删除！");
		OpenDramaItem odi = daoService.getObjectByUkey(OpenDramaItem.class, "dpid", seatArea.getDpid());
		int count = dpiManageService.verifySeatAreaSeatLock(areaid);
		ErrorCode<List<String>> remoteLockList = theatreOperationService.updateRemoteLock(odi, seatArea, OdiConstant.SECONDS_ADDORDER, true);
		if(!remoteLockList.isSuccess()) {
			return showJsonError(model, remoteLockList.getMsg());
		}
		dpiManageService.updateSeatAreaStats(odi, seatArea, remoteLockList.getRetval(), false);
		Map jsonMap = BeanUtil.getBeanMapWithKey(seatArea, "gsellnum", "csellnum", "locknum", "remainnum", "updatetime");
		if(count>0){
			jsonMap.put("msg", "校验锁定且卖出座位" + count + "个");
		}
		return showJsonSuccess(model, jsonMap);
	}
	
	@RequestMapping("/admin/dramaTicket/updateOdiStats.xhtml")
	public String updateOdiStats(Long itemid, ModelMap model){
		OpenDramaItem odi = daoService.getObjectByUkey(OpenDramaItem.class, "dpid", itemid);
		if(odi == null) return showJsonError(model, "场次不存在或被删除！");
		odiOpenService.asynchUpdateAreaStats(odi);
		Map jsonMap = BeanUtil.getBeanMapWithKey(odi, "gsellnum", "csellnum", "locknum", "remainnum", "updatetime");
		return showJsonSuccess(model, jsonMap);
	}
	
	@RequestMapping("/admin/dramaTicket/odiStats.xhtml")
	public String odiStats(Long tid, Date date, Integer maxnum, HttpServletRequest request, ModelMap model){
		final String viewPage = "admin/theatreticket/odiStats.vm";
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
		
		Collections.sort(dateMapList, new MultiPropertyComparator(new String[]{"playdate"}, new boolean[]{true}));
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
}
