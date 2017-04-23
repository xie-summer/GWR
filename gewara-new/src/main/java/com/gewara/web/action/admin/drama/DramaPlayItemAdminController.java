package com.gewara.web.action.admin.drama;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import com.gewara.constant.OdiConstant;
import com.gewara.constant.Status;
import com.gewara.constant.TagConstant;
import com.gewara.constant.content.SignName;
import com.gewara.helper.TspHelper;
import com.gewara.model.acl.User;
import com.gewara.model.content.GewaCommend;
import com.gewara.model.drama.Drama;
import com.gewara.model.drama.DramaPlayItem;
import com.gewara.model.drama.DramaStar;
import com.gewara.model.drama.OpenDramaItem;
import com.gewara.model.drama.Theatre;
import com.gewara.model.drama.TheatreField;
import com.gewara.model.drama.TheatreSeatPrice;
import com.gewara.service.drama.DramaPlayItemService;
import com.gewara.service.drama.DramaToStarService;
import com.gewara.service.drama.OpenDramaService;
import com.gewara.support.ErrorCode;
import com.gewara.util.BeanUtil;
import com.gewara.util.BindUtils;
import com.gewara.util.DateUtil;
import com.gewara.web.action.admin.BaseAdminController;
import com.gewara.web.util.PageUtil;

@Controller
public class DramaPlayItemAdminController extends BaseAdminController {
	@Autowired@Qualifier("dramaPlayItemService")
	private DramaPlayItemService dramaPlayItemService;
	public void setDramaPlayItemService(DramaPlayItemService dramaPlayItemService) {
		this.dramaPlayItemService = dramaPlayItemService;
	}
	@Autowired@Qualifier("openDramaService")
	private OpenDramaService openDramaService;
	public void setOpenDramaService(OpenDramaService openDramaService) {
		this.openDramaService = openDramaService;
	}
	@Autowired@Qualifier("dramaToStarService")
	private DramaToStarService dramaToStarService;
	public void setDramaToStarService(DramaToStarService dramaToStarService) {
		this.dramaToStarService = dramaToStarService;
	}
	@RequestMapping("/admin/drama/dramaPlayItem/itemList.xhtml")
	public String itemList(Long theatreid, Date date, String all, HttpServletRequest request, ModelMap model) {
		String citycode = getAdminCitycode(request);
		Theatre theatre = daoService.getObject(Theatre.class, theatreid);
		Timestamp curtime = new Timestamp(System.currentTimeMillis());
		Timestamp begintime = DateUtil.getBeginningTimeOfDay(curtime);
		List<Map> dateMapList = dramaPlayItemService.getDateCount(theatreid, begintime);
		List<DramaPlayItem> dpiList = new ArrayList<DramaPlayItem>();
		if(date==null && dateMapList.size()>0){
			Map m = dateMapList.get(0);
			date = DateUtil.parseDate(m.get("playdate").toString(),"yyyy-MM-dd");
		}
		if(date==null) date = new Date();
		Timestamp from = new Timestamp(date.getTime());
		Timestamp to = DateUtil.getMonthLastDay(from);
		to = DateUtil.getLastTimeOfDay(to);
		dpiList = dramaPlayItemService.getDramaPlayItemList(citycode, theatreid, null, from, to, null, false);
		Map<Long, TheatreField> sectionMap = new HashMap<Long, TheatreField>();
		Map<String, List<Integer>> priceMap = new HashMap<String, List<Integer>>();
		Map<Long, Boolean> bookingMap = new HashMap<Long, Boolean>();
		Map<Long, TspHelper> tspHelperMap = new HashMap<Long, TspHelper>();
		for(DramaPlayItem item : dpiList){
			TheatreField section = daoService.getObject(TheatreField.class, item.getRoomid());
			sectionMap.put(item.getId(), section);
			dramaPlayItemService.initDramaPlayItem(item);
			OpenDramaItem odi = daoService.getObjectByUkey(OpenDramaItem.class, "dpid", item.getId(), false);
			bookingMap.put(item.getId(), odi!=null);
			List<TheatreSeatPrice> tspList = dramaPlayItemService.getTspList(item.getId());
			TspHelper tspHelper = new TspHelper(tspList);
			tspHelperMap.put(item.getId(), tspHelper);
		}
		model.put("all", all);
		model.put("curdate", date);
		model.put("theatre", theatre);
		model.put("dpiList", dpiList);
		model.put("priceMap", priceMap);
		model.put("sectionMap", sectionMap);
		model.put("dateMapList", dateMapList);
		model.put("bookingMap", bookingMap);
		model.put("tspHelperMap", tspHelperMap);
		model.put("curdate", DateUtil.format(date, "yyyy-MM-dd"));
		return "admin/theatre/itemList.vm";
	}
	@RequestMapping("/admin/drama/dramaPlayItem/toCopy.xhtml")
	public String toCopy(Long itemid, ModelMap model){
		DramaPlayItem item = daoService.getObject(DramaPlayItem.class, itemid);
		if(!item.hasGewa()) return showJsonError(model, "非格瓦拉排期不能复制！");
		model.put("curdate", item.getPlaytime());
		model.put("item", item);
		List<TheatreField> theatreRoomList = daoService.getObjectListByField(TheatreField.class, "theatreid", item.getTheatreid());
		if(theatreRoomList.isEmpty()) return showJsonError(model, "该场馆没有场地！");
		model.put("theatreRoomList", theatreRoomList);
		return "admin/theatre/copyDialog.vm";
	}

	@RequestMapping("/admin/drama/dramaPlayItem/itemDetail.xhtml")
	public String itemDetail(Long id, Long theatreid, ModelMap model){
		DramaPlayItem item = daoService.getObject(DramaPlayItem.class, id);
		Theatre theatre = daoService.getObject(Theatre.class, theatreid);
		List<TheatreField> fieldList = new ArrayList<TheatreField>();
		Long dramaid = null;
		if(item == null){
			if(theatre == null) return showJsonError(model, "场馆不存在或被删除！");
			fieldList = daoService.getObjectListByField(TheatreField.class, "theatreid", theatre.getId());
		}else{
			if(!item.hasGewa()) return showJsonError(model, "非格瓦拉排期，不能修改！");
			theatre = daoService.getObject(Theatre.class, item.getTheatreid());
			TheatreField field = daoService.getObject(TheatreField.class, item.getRoomid());
			fieldList.add(field);
			OpenDramaItem odi = daoService.getObjectByUkey(OpenDramaItem.class, "dpid", item.getId());
			model.put("odi", odi);
			dramaid = theatre.getId();
		}
		if(fieldList.isEmpty()) return showJsonError(model, "场馆没有设置场地！");
		model.put("theatre", theatre);
		model.put("item", item);
		model.put("fieldList", fieldList);
		model.put("opentypeTextMap", OdiConstant.opentypeTextMap);
		model.putAll(getDramaData(dramaid, theatre.getId()));
		return "admin/theatre/itemDetail.vm";
	}
	@RequestMapping("/admin/drama/dramaPlayItem/saveItem.xhtml")
	public String saveItem(Long id, HttpServletRequest request, ModelMap model){
		User user = getLogonUser();
		Timestamp cur = DateUtil.getCurFullTimestamp();
		DramaPlayItem item = null;
		if(id!=null){
			item = daoService.getObject(DramaPlayItem.class, id);
			if(item == null) return showJsonError_NOT_FOUND(model);
		}else{
			item = new DramaPlayItem(cur);
		}
		BindUtils.bindData(item, request.getParameterMap());
		ErrorCode<DramaPlayItem> code = dramaPlayItemService.saveDramaPlayItem(item, user.getId());
		if(!code.isSuccess()) return showJsonError(model, code.getMsg());
		return showJsonSuccess(model, DateUtil.format(code.getRetval().getPlaytime(), "yyyy-MM-dd"));
	}
	@RequestMapping("/admin/drama/dramaPlayItem/delItem.xhtml")
	public String delItem(Long id, ModelMap model) {
		ErrorCode code = dramaPlayItemService.removieDramaPlayItem(id);
		if(!code.isSuccess()) return showJsonError(model, code.getMsg());
		return showJsonSuccess(model);
	}
	@RequestMapping("/admin/drama/dramaPlayItem/getTsp.xhtml")
	public String getTsp(Long id, ModelMap model) {
		TheatreSeatPrice tsp = daoService.getObject(TheatreSeatPrice.class, id);
		return showJsonSuccess(model, BeanUtil.getBeanMapWithKey(tsp, "id", "price", "costprice", "theatreprice"));
	}
	@RequestMapping("/admin/drama/dramaPlayItem/delTsp.xhtml")
	public String delTsp(Long id, ModelMap model) {
		User user = getLogonUser();
		TheatreSeatPrice tsp = daoService.getObject(TheatreSeatPrice.class, id);
		if(tsp == null) return showJsonError_DATAERROR(model);
		Integer count = openDramaService.getDramaOrderCount(tsp.getDpid());
		if(count>0) return showJsonError(model, "该价格已经有订单不能删除！");
		dbLogger.warn("用户："+user.getId()+"删除话剧场次价格:"+tsp.getPrice());
		DramaPlayItem item = daoService.getObject(DramaPlayItem.class, tsp.getDpid());
		tsp.setUpdatetime(new Timestamp(System.currentTimeMillis()));
		tsp.setStatus(Status.DEL);
		daoService.saveObject(tsp);
		daoService.saveObject(item);
		return showJsonSuccess(model);
	}
	@RequestMapping("/admin/drama/dramaPlayItem/copyItem.xhtml")
	public String copyOdi(Long itemid, String playdates, String rooms, ModelMap model) throws Exception{
		playdates = StringUtils.replaceOnce(playdates, ",", "");
		rooms = StringUtils.replaceOnce(rooms, ",", "");
		DramaPlayItem item = daoService.getObject(DramaPlayItem.class, itemid);
		ErrorCode code = dramaPlayItemService.saveDramaPlayItem(item, playdates, rooms);
		if(!code.isSuccess()) return showJsonError(model, code.getMsg());
		return showJsonSuccess(model);
	}
	
	@RequestMapping("/admin/drama/dramaPlayItem/preDramaList.xhtml")
	public String preDramaList(Integer pageNo, Long theatreid, ModelMap model) {
		if(pageNo == null) pageNo = 0;
		int rowsPre = 8;
		int firstRowsPre = pageNo * rowsPre;
		int count = commonService.getGewaCommendCount(null, SignName.PRE_DRAMA, theatreid, TagConstant.TAG_DRAMA, false);
		List<GewaCommend> commendList = commonService.getGewaCommendList(null, SignName.PRE_DRAMA,theatreid, TagConstant.TAG_DRAMA, false, firstRowsPre, rowsPre);
		PageUtil pageUtil = new PageUtil(count, rowsPre, pageNo, "admin/drama/dramaPlayItem/preDramaList.xhtml");
		Map params = new HashMap();
		params.put("theatreid", theatreid);
		pageUtil.initPageInfo(params);
		model.put("pageUtil", pageUtil);
		List<Long> idList = BeanUtil.getBeanPropertyList(commendList, Long.class, "relatedid", true);
		Map<Long, Drama> dramaMap = daoService.getObjectMap(Drama.class, idList);
		model.put("commendList", commendList);
		model.put("dramaMap", dramaMap);
		model.put("theatre", daoService.getObject(Theatre.class, theatreid));
		return "admin/theatre/preDramaList.vm";
	}
	@RequestMapping("/admin/drama/dramaPlayItem/savePreDrama.xhtml")
	public String savePreDrama(Long theatreid, Long dramaid, ModelMap model) {
		Drama drama = daoService.getObject(Drama.class, dramaid);
		if(drama==null) return showJsonError(model, "该话剧不存在，请核实输入的ID！");
		GewaCommend commend = new GewaCommend(SignName.PRE_DRAMA);
		commend.setTag("drama");
		commend.setRelatedid(dramaid);
		commend.setParentid(theatreid);
		daoService.saveObject(commend);
		return showJsonSuccess(model);
	}
	@RequestMapping("/admin/drama/dramaPlayItem/delPreDrama.xhtml")
	public String delPreDrama(Long theatreid, Long dramaid, ModelMap model) {
		String hql = "from GewaCommend g where g.signname=? and g.relatedid=? and g.tag=? and g.parentid=?";
		List<GewaCommend> commendList = hibernateTemplate.find(hql, SignName.PRE_DRAMA, dramaid, "drama", theatreid);
		daoService.removeObjectList(commendList);
		return showJsonSuccess(model);
	}
	private Map getDramaData(Long dramaid, Long theatreid){
		Map m = new HashMap();
		List<GewaCommend> commendList = commonService.getGewaCommendList(null, SignName.PRE_DRAMA, theatreid,null, false, "addtime", false, 0, 50);
		List<Long> idList = BeanUtil.getBeanPropertyList(commendList, Long.class, "relatedid", true);
		if(dramaid!=null && !idList.contains(dramaid)){
			idList.add(0, dramaid);
			List<DramaStar> starList = dramaToStarService.getDramaStarListByDramaid(dramaid, DramaStar.TYPE_TROUPE, -1, -1);
			m.put("starList", starList);
		}
		List<Drama> dramaList = daoService.getObjectList(Drama.class, idList);
		m.put("dramaList", dramaList);
		return m; 
	}
	
	@RequestMapping("/admin/drama/dramaPlayItem/getDramaStar.xhtml")
	public String getDramaStar(Long dramaid, ModelMap model){
		List<DramaStar> starList = dramaToStarService.getDramaStarListByDramaid(dramaid, DramaStar.TYPE_TROUPE, -1, -1);
		List<Map> dramaStarList = BeanUtil.getBeanMapList(starList, "id", "name");
		Map jsonMap = new HashMap();
		jsonMap.put("starList", dramaStarList);
		return showJsonSuccess(model,jsonMap);
	}
}
