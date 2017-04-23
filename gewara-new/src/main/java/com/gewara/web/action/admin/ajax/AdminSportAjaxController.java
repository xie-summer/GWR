package com.gewara.web.action.admin.ajax;

import java.sql.Timestamp;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import com.gewara.constant.Flag;
import com.gewara.constant.Status;
import com.gewara.model.common.County;
import com.gewara.model.common.Indexarea;
import com.gewara.model.common.Subwaystation;
import com.gewara.model.sport.Sport;
import com.gewara.model.sport.Sport2Item;
import com.gewara.model.sport.SportItem;
import com.gewara.model.sport.SportItemPrice;
import com.gewara.model.sport.SportPrice;
import com.gewara.model.sport.SportPriceTable;
import com.gewara.service.sport.SportService;
import com.gewara.support.ServiceHelper;
import com.gewara.untrans.SearchService;
import com.gewara.untrans.impl.ControllerService;
import com.gewara.util.BeanUtil;
import com.gewara.util.BindUtils;
import com.gewara.util.ChangeEntry;
import com.gewara.util.DateUtil;
import com.gewara.util.JsonUtils;
import com.gewara.util.PinYinUtils;
import com.gewara.util.ValidateUtil;
import com.gewara.util.VmUtils;
import com.gewara.web.action.admin.BaseAdminController;

@Controller
public class AdminSportAjaxController extends BaseAdminController {
	@Autowired@Qualifier("sportService")
	private SportService sportService;
	public void setSportService(SportService sportService) {
		this.sportService = sportService;
	}
	@Autowired@Qualifier("controllerService")
	private ControllerService controllerService;

	@Autowired@Qualifier("searchService")
	private SearchService searchService;
	public void setSearchService(SearchService searchService) {
		this.searchService = searchService;
	}
	
	@RequestMapping("/admin/sport/ajax/saveOrUpdateSportItem.xhtml")
	public String saveOrUpdateSportItem(HttpServletRequest request, Long id, ModelMap model) {
		SportItem sportItem = new SportItem("");
		if (id != null) sportItem = daoService.getObject(SportItem.class, new Long(id));
		ChangeEntry changeEntry = new ChangeEntry(sportItem);
		BindUtils.bindData(sportItem, request.getParameterMap());
		String[] otherinfoList = new String[]{
				Flag.SERVICE_EXPLOSIVE, Flag.SERVICE_CALORIE, Flag.SERVICE_ENDURANCE, Flag.SERVICE_RATIO
		};
		Map otherinfoMap = controllerService.getAndSetOtherinfo(sportItem.getOtherinfo(), Arrays.asList(otherinfoList), request);
		//验证内容
		String msg=ValidateUtil.validateNewsContent(null, sportItem.getContent());
		if(StringUtils.isNotBlank(msg)) return showJsonError_DATAERROR(model);
		sportItem.setUpdatetime(DateUtil.getCurFullTimestamp());
		sportItem.setOtherinfo(JsonUtils.writeObjectToJson(otherinfoMap));
		daoService.saveObject(sportItem);
		monitorService.saveChangeLog(getLogonUser().getId(), SportItem.class, sportItem.getId(),changeEntry.getChangeMap(sportItem));
		searchService.pushSearchKey(sportItem);//更新索引至索引服务器
		return showJsonSuccess(model, BeanUtil.getBeanMap(sportItem));
	}

	@RequestMapping("/admin/sport/ajax/getSportPriceTableById.xhtml")
	public String getSportPriceTableById(Long sportPriceTableId, ModelMap model) {
		SportPriceTable sportPriceTable = daoService.getObject(SportPriceTable.class, sportPriceTableId);
		Map result = BeanUtil.getBeanMap(sportPriceTable, true);
		return showJsonSuccess(model,result);
	}

	@RequestMapping("/admin/sport/ajax/saveOrUpdateSportPriceTable.xhtml")
	public String saveOrUpdateSportPriceTable(Long sportId,String id,String itemid,
			HttpServletRequest request, ModelMap model) {
		Sport sport = daoService.getObject(Sport.class, sportId);
		Map sportPriceTableMap = request.getParameterMap();
		SportPriceTable sportPriceTable = new SportPriceTable();
		if (StringUtils.isNotBlank(id)) {
			sportPriceTable = daoService.getObject(SportPriceTable.class, Long
					.valueOf(id));
		}
		ChangeEntry changeEntry = new ChangeEntry(sportPriceTable);
		BindUtils.bindData(sportPriceTable, sportPriceTableMap);
		sportPriceTable.setSportid(sport.getId());
		SportItem sportItem = daoService.getObject(SportItem.class, new Long(itemid));
		sportPriceTable.setItemid(sportItem.getId());
		daoService.saveObject(sportPriceTable);
		monitorService.saveChangeLog(getLogonUser().getId(), SportPriceTable.class, sportPriceTable.getId(),changeEntry.getChangeMap( sportPriceTable));
		Map result = BeanUtil.getBeanMap(sportPriceTable, true);
		return showJsonSuccess(model,result);
	}
	
	@RequestMapping("/admin/sport/ajax/removeSportPriceTableById.xhtml")
	public String removeSportPriceTableById(Long sportPriceTableId, ModelMap model) {
		daoService.removeObjectById(SportPriceTable.class, sportPriceTableId);
		return this.showJsonSuccess(model);
	}

	@RequestMapping("/admin/sport/ajax/saveOrUpdateSportPrice.xhtml")
	public String saveOrUpdateSportPrice(Long sportPriceTableId,String id,
			HttpServletRequest request, ModelMap model) {
		SportPrice sportPrice = new SportPrice();
		if (StringUtils.isNotBlank(id))
			sportPrice = daoService.getObject(SportPrice.class, new Long(id));
		ChangeEntry changeEntry = new ChangeEntry(sportPrice);
		BindUtils.bindData(sportPrice, request.getParameterMap());
		sportPrice.setPricetableid(sportPriceTableId);
		sportPrice.setOrdernum(0);
		daoService.saveObject(sportPrice);
		monitorService.saveChangeLog(getLogonUser().getId(), SportPrice.class, sportPrice.getId(),changeEntry.getChangeMap( sportPrice));
		Map result = BeanUtil.getBeanMap(sportPrice, true);
		return showJsonSuccess(model,result);
	}
	
	@RequestMapping("/admin/sport/ajax/getSportPriceById.xhtml")
	public String getSportPriceById(Long sportPriceId, ModelMap model) {
		SportPrice sportPrice = daoService.getObject(SportPrice.class, sportPriceId);
		Map result = BeanUtil.getBeanMap(sportPrice, true);
		return showJsonSuccess(model,result);
	}

	@RequestMapping("/admin/sport/ajax/removeSportPriceById.xhtml")
	public String removeSportPriceById(Long sportPriceId, ModelMap model) {
		daoService.removeObjectById(SportPrice.class, sportPriceId);
		return this.showJsonSuccess(model);
	}

	@RequestMapping("/admin/sport/ajax/getSportItemById.xhtml")
	public String getSportItemById(Long itemId, ModelMap model) {
		SportItem item = daoService.getObject(SportItem.class, itemId);
		Map result = BeanUtil.getBeanMap(item);
		result.put("otherinfo", VmUtils.readJsonToMap(item.getOtherinfo()));
		return showJsonSuccess(model,result);
	}
	
	
	@RequestMapping("/admin/sport/ajax/getSport2ItemById.xhtml")
	public String getSport2ItemById(Long sportId,Long itemId, ModelMap model) {
		Sport2Item item = sportService.getSport2Item(sportId, itemId);
		if(item==null)
			return showJsonSuccess(model);
		Map result = BeanUtil.getBeanMap(item);
		result.put("otherinfo", VmUtils.readJsonToMap(item.getOtherinfo()));
		return showJsonSuccess(model,result);
	} 

	@RequestMapping("/admin/sport/ajax/saveOrUpdateRelateSportItem.xhtml")
	public String saveOrUpdateRelateSportItem(HttpServletRequest request, ModelMap model) {
		Long sportId=new Long(request.getParameter("sportId")); 
		Long itemId=new Long(request.getParameter("itemId"));
		String method=request.getParameter("method");  //判断是新增 、修改
		Map<String,String> newMap=new HashMap<String,String>();
		for(Iterator it=request.getParameterMap().entrySet().iterator();it.hasNext();)
		{
			Entry<String,String> entry=(Entry)it.next();
			if("sportId".equals(entry.getKey())||"itemId".equals(entry.getKey())||"method".equals(entry.getKey()))
				continue ;
			   newMap.put(entry.getKey(), request.getParameter(entry.getKey()));
		}
		String otherInfo=JsonUtils.writeObjectToJson(newMap);
		Sport2Item s2i = sportService.getSport2Item(sportId, itemId);
		if(s2i==null){
			s2i = new Sport2Item(sportId, itemId);
			s2i.setOtherinfo(otherInfo);
			daoService.saveObject(s2i);
		}
		else if(s2i!=null&&"add".equals(method)){
			 return showJsonError(model, "该项目已经添加！");
		}
		else{
			s2i.setOtherinfo(otherInfo);
			daoService.updateObject(s2i);
		}
		return showJsonSuccess(model);
	}
	
	@RequestMapping("/admin/sport/ajax/removeRelateSportItemById.xhtml")
	public String removeRelateSportItemById(Long sportId, Long itemId, ModelMap model) {
		Sport2Item s2i = sportService.getSport2Item(sportId, itemId);
		if(s2i!=null) {
			daoService.removeObject(s2i);
		}
		return showJsonSuccess(model);
	}

	@RequestMapping("/admin/sport/ajax/saveSport.xhtml")
	public String saveSport(String sportId,String name,String countyCode, String indexareaCode,
			String stationid,HttpServletRequest request, ModelMap model){
		Sport sport = null;
		if(StringUtils.isNotBlank(sportId)) {
			sport = daoService.getObject(Sport.class, new Long(sportId));
		}else {
			sport = new Sport(name);
			sport.setCitycode(this.getAdminCitycode(request));
		}
		ChangeEntry changeEntry = new ChangeEntry(sport);
		BindUtils.bindData(sport, request.getParameterMap());
		
		Map<String, Object> otherinfoMap = controllerService.getAndSetOtherinfo(sport.getOtherinfo(), ServiceHelper.getOtherInfoList(), request);
		sport.setOtherinfo(JsonUtils.writeObjectToJson(otherinfoMap));
		//验证内容
		String msg=ValidateUtil.validateNewsContent(null, sport.getContent());
		if(StringUtils.isNotBlank(msg)) return showJsonError(model, msg);
		if(StringUtils.isNotBlank(countyCode)){
			sport.setCountycode(StringUtils.trimToNull(countyCode));
			sport.setCountyname(daoService.getObject(County.class, countyCode).getCountyname());
		}else{
			sport.setCountycode(null);
			sport.setCountyname(null);
		}
		if(StringUtils.isNotBlank(indexareaCode)){
			String indexareacode = StringUtils.trimToNull(indexareaCode);
			sport.setIndexareacode(indexareacode);
			sport.setIndexareaname(daoService.getObject(Indexarea.class, indexareacode).getIndexareaname());
		}else {
			sport.setIndexareacode(null);
			sport.setIndexareaname(null);
		}
		
		if(StringUtils.isNotBlank(stationid)){
			Subwaystation subwaystation = daoService.getObject(Subwaystation.class, Long.valueOf(stationid));
			sport.setStationname(subwaystation.getStationname());
		}else{
			sport.setStationname(null);
		}
		if(sport.getAddtime()==null) sport.setAddtime(new Timestamp(System.currentTimeMillis()));
		sport.setPinyin(PinYinUtils.getPinyin(sport.getName()));
		sport.setUpdatetime(DateUtil.getCurFullTimestamp());
//		sport.setSearchkey(placeService.getSearchKey(sport));
		if(StringUtils.isBlank(sport.getBooking())){
			sport.setBooking(Sport.BOOKING_CLOSE);
		}
		daoService.saveObject(sport);
		monitorService.saveChangeLog(getLogonUser().getId(), Sport.class, sport.getId(),changeEntry.getChangeMap(sport));
		searchService.pushSearchKey(sport);
		return this.showJsonSuccess(model,sport.getId()+"");
	}

	@RequestMapping("/admin/sport/ajax/updateSportFlagValue.xhtml")
	public String updateSportFlagValue(Long sportId, String value, String value2, ModelMap model) {
		String rvalue = null;
		if(StringUtils.isNotBlank(value)) {
			rvalue = value;
			if(StringUtils.isNotBlank(value2))rvalue += ","+value2;
		}else if(StringUtils.isNotBlank(value2)){
			rvalue = value2;
		}
		sportService.updateSportFlag(sportId, rvalue);
		return showJsonSuccess(model);
	}

	@RequestMapping("/admin/sport/ajax/updateSportHotValue.xhtml")
	public String updateSportHotValue(Long sportId, Integer value, ModelMap model) {
		sportService.updateSportHotValue(sportId, value);
		return showJsonSuccess(model);
	}
	
	@RequestMapping("/admin/sport/ajax/setSportItemFlagValue.xhtml")
	public String setSportItemFlagValue(Long sportId, String value, ModelMap model) {
		sportService.updateSportItemFlagValue(sportId, value);
		return showJsonSuccess(model);
	}
	
	@RequestMapping("/admin/sport/ajax/updateSport2ItemBooking.xhtml")
	public String updateSport2ItemBooking(Long sportId, Long itemId,String booking, ModelMap model) {
		 Sport2Item sport2Item= sportService.getSport2Item(sportId, itemId);
		 sport2Item.setBooking(booking);
		 daoService.updateObject(sport2Item);
		return showJsonSuccess(model);
	}

	//运动项目运动后短信
	@RequestMapping("/admin/sport/ajax/updateSportItemMSG.xhtml")
	public String updateSportItemMSG(Long itemId, String defaultMsg, Integer sendTime, ModelMap model){
		SportItem sportItem = daoService.getObject(SportItem.class, itemId);
		if(sportItem == null) return showJsonError(model, "运动项目不存在！");
		Map otherinfoMap = VmUtils.readJsonToMap(sportItem.getOtherinfo());
		otherinfoMap.put("defaultMsg", defaultMsg);
		otherinfoMap.put("sendTime", ""+sendTime);
		sportItem.setOtherinfo(JsonUtils.writeObjectToJson(otherinfoMap));
		sportItem.setUpdatetime(DateUtil.getCurFullTimestamp());
		daoService.saveObject(sportItem);
		return showJsonSuccess(model);
	}
	@RequestMapping("/admin/sport/ajax/updateSport2ItemMSG.xhtml")
	public String updateSport2ItemMSG(Long sportId, Long itemId, String overmsg, Integer sendTime, ModelMap model){
		Sport2Item sport2Item = sportService.getSport2Item(sportId, itemId);
		if(sport2Item == null) return showJsonError(model, "服务项目不存在！");
		sport2Item.setOvermsg(overmsg);
		Map otherinfoMap = VmUtils.readJsonToMap(sport2Item.getOtherinfo());
		otherinfoMap.put("sendTime", ""+sendTime);
		sport2Item.setOtherinfo(JsonUtils.writeObjectToJson(otherinfoMap));
		daoService.saveObject(sport2Item);
		return showJsonSuccess(model);
	}
	//添加修改项目
	@RequestMapping("/admin/sport/ajax/getSport2Item.xhtml")
	public String getSport2Item(Long sportId, Long itemId, ModelMap model){
		Sport sport = daoService.getObject(Sport.class, sportId);
		if(sport == null) return showJsonError(model, "场馆不存在！");
		model.put("sport", sport);
		if(itemId != null){
			Sport2Item sport2Item = sportService.getSport2Item(sportId, itemId);
			if(sport2Item == null) return showJsonError(model, "服务项目不存在！");
			model.put("sport2Item", sport2Item);
			model.put("itemId", itemId);
		}
		List<SportItem> topItemList = sportService.getTopSportItemList();
		model.put("topItemList", topItemList);
		return "admin/sport/addSport2Item.vm";
	}
	@RequestMapping("/admin/sport/ajax/saveOrUpdateSport2Item.xhtml")
	public String saveOrUpdateSport2Item(Long sportId, Long itemId, Long sport2ItemId,String sporttype, String infos, ModelMap model){
		Sport2Item s2i = null;
		if(sportId != null && itemId != null){
			if(sport2ItemId == null){
				s2i = sportService.getSport2Item(sportId, itemId);
				if(s2i != null) return showJsonError(model, "该服务项目已经添加！");
				s2i = new Sport2Item(sportId, itemId);
			}else{
				s2i = daoService.getObject(Sport2Item.class, sport2ItemId);
			}
			s2i.setItemid(itemId);
			s2i.setSporttype(sporttype);
			s2i.setDescription(infos);
			daoService.saveObject(s2i);
		}else{
			return showJsonError(model, "场馆或服务项目不存在！");
		}
		return showJsonSuccess(model);
	}
	//价格表
	@RequestMapping("/admin/sport/ajax/getSportItemPrice.xhtml")
	public String getSportItemPrice(Long sportId, Long itemId, ModelMap model){
		if(sportId == null || itemId == null) return showJsonError(model, "场馆或服务项目不存在！");
		List<SportItemPrice> sipList = sportService.getSportItemPriceListBySportIdAndItemId(sportId, itemId);
		Map<Long, Sport2Item> sipMap = BeanUtil.groupBeanList(sipList, "week");
		model.put("sipMap", sipMap);
		model.put("itemId", itemId);
		return "admin/sport/sportItemPrice.vm";
	}
	@RequestMapping("/admin/sport/ajax/saveOrUpdateSportItemPrice.xhtml")
	public String saveOrUpdateSportItemPrice(Long sportId, Long itemId,String priceList, String unweek, ModelMap model){
		if(sportId == null || itemId == null) return showJsonError(model, "场馆或服务项目不存在！");
		String[] prices = StringUtils.split(priceList, ";");
		String[] unweeks = StringUtils.split(unweek, ",");
		for(String price : prices){
			String[] priceInfo = StringUtils.split(price, ",");
			if(StringUtils.isBlank(priceInfo[0])) return showJsonError(model, "星期选择错误！");
			if(StringUtils.isBlank(priceInfo[1])) return showJsonError(model, "最小价填写错误！");
			if(StringUtils.isBlank(priceInfo[2])) return showJsonError(model, "最大价填写错误！");
			SportItemPrice sip = sportService.getSportItemPriceBySportIdAndItemId(sportId, itemId, Integer.parseInt(priceInfo[0]));
			if(sip == null){
				sip = new SportItemPrice(sportId, itemId, Integer.parseInt(priceInfo[0]));
				sip.setMinprice(Integer.parseInt(priceInfo[1]));
				sip.setMaxprice(Integer.parseInt(priceInfo[2]));
			}else {
				sip.setMinprice(Integer.parseInt(priceInfo[1]));
				sip.setMaxprice(Integer.parseInt(priceInfo[2]));
				sip.setStatus(Status.Y);
			}
			daoService.saveObject(sip);
		}
		for(String week : unweeks){
			SportItemPrice sip = sportService.getSportItemPriceBySportIdAndItemId(sportId, itemId, Integer.parseInt(week));
			if(sip != null){
				sip.setStatus(Status.N);
			}
			daoService.saveObject(sip);
		}
		return showJsonSuccess(model);
	}
	@RequestMapping("/admin/sport/ajax/changeSortnum.xhtml")
	public String changeSortnum(Long sportId, Long itemId,Integer ordernum, ModelMap model) {
		Sport2Item sport2Item=sportService.getSport2Item(sportId, itemId);
		if (sport2Item == null) return showJsonError_NOT_FOUND(model);
		sport2Item.setSortnum(ordernum);
		daoService.saveObject(sport2Item);
		return showJsonSuccess(model);
	}
}
