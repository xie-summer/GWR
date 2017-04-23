package com.gewara.web.action.admin.gewapay;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
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

import com.gewara.constant.Status;
import com.gewara.constant.TagConstant;
import com.gewara.constant.content.SignName;
import com.gewara.helper.GoodsDisQuanHelper;
import com.gewara.helper.GoodsPriceHelper;
import com.gewara.model.acl.User;
import com.gewara.model.content.GewaCommend;
import com.gewara.model.drama.Drama;
import com.gewara.model.drama.DramaStar;
import com.gewara.model.drama.Theatre;
import com.gewara.model.drama.TheatreRoom;
import com.gewara.model.goods.BaseGoods;
import com.gewara.model.goods.GoodsDisQuantity;
import com.gewara.model.goods.GoodsPrice;
import com.gewara.model.goods.TicketGoods;
import com.gewara.service.drama.DramaPlayItemService;
import com.gewara.service.drama.DramaToStarService;
import com.gewara.service.order.GoodsService;
import com.gewara.support.ErrorCode;
import com.gewara.util.BeanUtil;
import com.gewara.util.BindUtils;
import com.gewara.util.ChangeEntry;
import com.gewara.util.DateUtil;
import com.gewara.util.StringUtil;
import com.gewara.util.WebUtils;
import com.gewara.web.action.admin.BaseAdminController;

@Controller
public class TicketGoodsAdminController extends BaseAdminController {
	@Autowired@Qualifier("goodsService")
	private GoodsService goodsService;
	
	@Autowired@Qualifier("dramaToStarService")
	private DramaToStarService dramaToStarService;
	
	@Autowired@Qualifier("dramaPlayItemService")
	private DramaPlayItemService dramaPlayItemService;
	
	protected List<Map<String, String>> getYMList(List<String> tmpList){
		List<Map<String, String>> list = new ArrayList<Map<String, String>>();
		Date date = new Date();
		int year = DateUtil.getYear(date);
		int year2 = year+1;
		String ym = DateUtil.format(date, "yyyy-MM");
		for(int i=1;i<=12;i++){
			String tmp = i+"";
			if(i<=9) tmp = "0"+i;
			String strYM = year + "-" + tmp;
			Map m = new HashMap();
			if(strYM.compareTo(ym)>=0 && !tmpList.contains(strYM)) {
				m.put("playdate", strYM);
				m.put("count", 0);
				list.add(m);
			}
			String strYM2 = year2 + "-" + tmp;
			if(!tmpList.contains(strYM2)){
				Map m2 = new HashMap();
				m2.put("playdate", strYM2);
				m2.put("count", 0);
				list.add(m2);
			}
		}
		return list;
	}
	class ValueComparator implements Comparator<Map<String, String>> {
		public int compare(Map<String, String> arg0, Map<String, String> arg1) {
			return arg0.get("playdate").compareTo(arg1.get("playdate"));
		}
	}
	@RequestMapping("/admin/goods/dramaPlayItem/toCopy.xhtml")
	public String toCopy(Long gid, ModelMap model){
		BaseGoods goods = daoService.getObject(BaseGoods.class, gid);
		if(goods == null) return showJsonError_NOT_FOUND(model);
		if(!goods.hasPeriod()) return showJsonError(model, "不是时间段场次不能复制！");
		model.put("curdate", goods.getFromvalidtime());
		model.put("goods", goods);
		List<TheatreRoom> theatreRoomList = dramaPlayItemService.getRoomList(goods.getRelatedid());
		model.put("theatreRoomList", theatreRoomList);
		return "admin/goods/copyDialog.vm";
	}
	@RequestMapping("/admin/goods/dramaPlayItem/copyItem.xhtml")
	public String copyOdi(Long gid, String playdates, String rooms, ModelMap model) throws Exception{
		playdates = StringUtils.replaceOnce(playdates, ",", "");
		rooms = StringUtils.replaceOnce(rooms, ",", "");
		TicketGoods goods = daoService.getObject(TicketGoods.class, gid);
		ErrorCode code = goodsService.saveTicketGoods(goods, playdates, rooms);
		if(!code.isSuccess()) return showJsonError(model, code.getMsg());
		return showJsonSuccess(model);
	}
	@RequestMapping("/admin/goods/ajax/savePeriodTicketGoods.xhtml")
	public String savePeriodTicketGoods(Long id, String tag, Long relatedid, String category, Long categoryid, Long starid,
			Long roomid, Timestamp fromvalidtime, String language, String summary, String description, Integer maxbuy,
			HttpServletRequest request, ModelMap model){
		String citycode = getAdminCitycode(request);
		Timestamp tovalidtime = DateUtil.addHour(fromvalidtime, 2);
		ErrorCode<TicketGoods> code = goodsService.saveCommonTicket(id, citycode, null, tag, relatedid, category, categoryid,
				starid, roomid, fromvalidtime, tovalidtime, language, summary, description, maxbuy, Status.Y, getLogonUser());
		if(!code.isSuccess()) return showJsonError(model, code.getMsg());
		return showJsonSuccess(model);
	}
	
	@RequestMapping("/admin/goods/ajax/saveTicketGoods.xhtml")
	public String saveCommonTicketGoods(Long id, String goodsname, String tag, Long relatedid, String category, Long categoryid, Long starid,
			Long roomid, Timestamp fromvalidtime, Timestamp tovalidtime, String language, String summary, String description, Integer maxbuy,
			HttpServletRequest request, ModelMap model){
		String citycode = getAdminCitycode(request);
		ErrorCode<TicketGoods> code = goodsService.saveCommonTicket(id, citycode, goodsname, tag, relatedid, category, categoryid, 
			starid, roomid, fromvalidtime, tovalidtime, language, summary, description, maxbuy, Status.N, getLogonUser());
		if(!code.isSuccess()) return showJsonError(model, code.getMsg());
		return showJsonSuccess(model);
	}
	
	@RequestMapping("/admin/goods/drama/ticketGoodsList.xhtml")
	public String dramaTicketGoodsList(Long relatedid, String period, Date date, HttpServletRequest request, ModelMap model){
		Object baseInfo = relateService.getRelatedObject(TagConstant.TAG_THEATRE, relatedid);
		model.put("baseInfo", baseInfo);
		String citycode = getAdminCitycode(request);
		Timestamp cur = DateUtil.getCurFullTimestamp();
		Timestamp fromvalidtime = DateUtil.getMonthFirstDay(cur);
		List<Map<String, String>> dateMapList = goodsService.getTicketGoodsMapList(citycode, TagConstant.TAG_THEATRE, relatedid, null, null, period, fromvalidtime, false);
		List<String> tmpList = new ArrayList<String>();
		for(Map<String, String> dateMap : dateMapList){
			tmpList.add(dateMap.get("playdate"));
		}
		Collections.sort(dateMapList, new ValueComparator());
		if(date==null && dateMapList.size()>0){
			Map m = dateMapList.get(0);
			date = DateUtil.parseDate(m.get("playdate").toString(),"yyyy-MM-dd");
		}
		if(date==null){
			date = new Date(fromvalidtime.getTime());
		}
		Timestamp fromtime = new Timestamp(date.getTime());
		Timestamp totime = DateUtil.getMonthLastDay(fromtime);
		totime = DateUtil.getLastTimeOfDay(totime);
		model.put("dateMapList", dateMapList);
		List<TicketGoods> ticketGoodsList = goodsService.getTicketGoodsList(citycode, TagConstant.TAG_THEATRE, relatedid, null, null, period, fromtime, totime, false, false);
		model.put("ticketGoodsList", ticketGoodsList);
		Map<Long, List<GoodsPrice>> goodsPriceMap = new HashMap<Long, List<GoodsPrice>>();
		for (TicketGoods commonTicketGoods : ticketGoodsList) {
			List<GoodsPrice> goodsPriceList = daoService.getObjectListByField(GoodsPrice.class, "goodsid", commonTicketGoods.getId());
			goodsPriceMap.put(commonTicketGoods.getId(), goodsPriceList);
		}
		model.put("goodsPriceMap", goodsPriceMap);
		List<Long> categoryIdList = BeanUtil.getBeanPropertyList(ticketGoodsList, "categoryid", true);
		Map<Long, Drama> dramaMap = daoService.getObjectMap(Drama.class, categoryIdList);
		model.put("dramaMap", dramaMap);
		return "admin/goods/commonTicketList.vm";
	}
	
	@RequestMapping("/admin/goods/drama/getTicketGoods.xhtml")
	public String dramaTicketGoods(Long id, Long relatedid, String period, ModelMap model){
		Long dramaid = null;
		if(id!=null) {
			TicketGoods goods = daoService.getObject(TicketGoods.class, id);
			relatedid = goods.getRelatedid();
			model.put("goods", goods);
			dramaid = goods.getCategoryid();
			period = goods.getPeriod();
		}
		Theatre theatre = daoService.getObject(Theatre.class, relatedid);
		model.putAll(getDramaData(dramaid, theatre.getId()));
		model.put("theatre", theatre);
		List<TheatreRoom> roomList = dramaPlayItemService.getRoomList(relatedid);
		model.put("roomList", roomList);
		model.put("period", period);
		if(StringUtils.equals(period, Status.Y)) return "admin/goods/periodTicketGoods.vm";
		return "admin/goods/commonTicketGoods.vm";
	}
	private Map getDramaData(Long dramaid, Long theatreid){
		Map m = new HashMap();
		List<GewaCommend> commendList = commonService.getGewaCommendList(null, SignName.PRE_DRAMA, theatreid,null, false, 0, 50);
		List<Long> idList = BeanUtil.getBeanPropertyList(commendList, Long.class, "relatedid", true);
		if(dramaid!=null && !idList.contains(dramaid)){
			idList.add(0, dramaid);
		}
		List<DramaStar> starList = dramaToStarService.getDramaStarListByDramaid(dramaid, DramaStar.TYPE_TROUPE, -1, -1);
		m.put("starList", starList);
		List<Drama> dramaList = daoService.getObjectList(Drama.class, idList);
		m.put("dramaList", dramaList);
		return m; 
	}
	
	@RequestMapping("/admin/goods/goodsprice.xhtml")
	public String goodsPriceList(Long gid, ModelMap model){
		BaseGoods goods = daoService.getObject(BaseGoods.class, gid);
		model.put("goods", goods);
		Object object = relateService.getRelatedObject(goods.getTag(), goods.getRelatedid());
		model.put("object", object);
		List<GoodsPrice> priceList = daoService.getObjectListByField(GoodsPrice.class, "goodsid", gid);
		Map<Long, Integer> disCountMap = new HashMap<Long, Integer>();
		for(GoodsPrice tsp : priceList){
			disCountMap.put(tsp.getId(), goodsService.getGoodsDisList(tsp.getId()).size());
		}
		GoodsPriceHelper tspHelper = new GoodsPriceHelper(priceList);
		model.put("tspHelper", tspHelper);
		model.put("disCountMap", disCountMap);
		return "admin/goods/goodsprice.vm";
	}
	
	@RequestMapping("/admin/goods/getGoodsPrice.xhtml")
	public String getOdiPrice(Long id, ModelMap model) {
		GoodsPrice goodsPrice = daoService.getObject(GoodsPrice.class, id);
		if(goodsPrice == null) return showJsonError(model, "该数据不存在或被删除！");
		Map jsonMap = BeanUtil.getBeanMap(goodsPrice);
		return showJsonSuccess(model, jsonMap);
	}
	
	@RequestMapping("/admin/goods/getGoodsDiscount.xhtml")
	public String getDiscount(Long gid, ModelMap model) {
		GoodsPrice tsp = daoService.getObject(GoodsPrice.class, gid);
		if(tsp == null) return showJsonError(model, "价格不存在或被删除！");
		List<GoodsDisQuantity> disList = goodsService.getGoodsDisList(gid);
		GoodsDisQuanHelper disHelper = new GoodsDisQuanHelper(disList);
		model.put("tsp", tsp);
		model.put("disHelper", disHelper);
		return "admin/goods/goods_disList.vm";
	}

	@RequestMapping("/admin/goods/saveGoodsDiscount.xhtml")
	public String getDiscount(Long gspid, Integer quantity, Integer price, Integer costprice, Integer oriprice, Integer allownum, ModelMap model) {
		if(price == null || price<=0) return showJsonError(model, "保存错误：" + price + "<=0");
		if(costprice == null || costprice<0) return showJsonError(model, "结算价错误！");
		if(allownum == null) return showJsonError(model, "库存不能为空！");
		GoodsPrice goodsPrice = daoService.getObject(GoodsPrice.class, gspid);
		if(goodsPrice == null) return showJsonError(model, "价格不存在或被删除！");
		List<GoodsDisQuantity> disquanList = goodsService.getGoodsDisList(gspid);
		GoodsDisQuanHelper disHelper = new GoodsDisQuanHelper(disquanList);
		GoodsDisQuantity discount = disHelper.getDisByQuantity(quantity);
		ChangeEntry changeEntry = new ChangeEntry(discount);
		if(discount==null){
			discount = new GoodsDisQuantity(gspid, quantity, price, costprice, oriprice);
			discount.setGoodsid(goodsPrice.getId());
		}else {
			discount.setPrice(price);
			discount.setCostprice(costprice);
			discount.setOriprice(oriprice);
			discount.setUpdatetime(DateUtil.getCurFullTimestamp());
		}
		//总库存数=库存数+卖出数量
		int allownums = allownum + discount.getSellordernum();
		discount.setAllownum(allownums);
		daoService.saveObject(discount);
		monitorService.saveChangeLog(getLogonUser().getId(), GoodsDisQuantity.class, discount.getId(),changeEntry.getChangeMap(discount));
		return showJsonSuccess(model);
	}
	
	@RequestMapping("/admin/goods/delGoodsDiscount.xhtml")
	public String getDiscount(Long gspid, Integer quantity, ModelMap model) {
		List<GoodsDisQuantity> disquanList = goodsService.getGoodsDisList(gspid);
		GoodsDisQuanHelper disHelper = new GoodsDisQuanHelper(disquanList);
		GoodsDisQuantity discount = disHelper.getDisByQuantity(quantity);
		if(discount==null){
			return showJsonError(model, "数据不存在！");
		}
		Long disid = discount.getId();
		daoService.removeObject(discount);
		monitorService.saveDelLog(getLogonUser().getId(), disid, discount);
		return showJsonSuccess(model);
	}
	
	@RequestMapping("/admin/goods/saveGoodsPrice.xhtml")
	public String saveOdiPrice(Long id, Long goodsid, HttpServletRequest request, ModelMap model) {
		User user = getLogonUser();
		BaseGoods goods = daoService.getObject(BaseGoods.class, goodsid);
		if(goods == null) return showJsonError(model, "该数据不存在或被删除！");
		GoodsPrice goodsPrice = new GoodsPrice(goodsid);
		if(id!=null){
			goodsPrice = daoService.getObject(GoodsPrice.class, id);
		}else {
			List<GoodsPrice> tspList = daoService.getObjectListByField(GoodsPrice.class, "goodsid", goodsid);
			GoodsPriceHelper tspHelper = new GoodsPriceHelper(tspList);
			List<String> typeList = GoodsPrice.getSeatTypeList(); 
			StringBuffer strf = new StringBuffer("");
			for(GoodsPrice ts : tspHelper.getGoodsPriceListBySno()){
				strf.append(ts.getPricelevel()+",");
			}
			for(String s : typeList){
				if(strf.indexOf(s)==-1){
					goodsPrice.setPricelevel(s);
					break ;
				}
			}
		}
		Map<String,String> dataMap = WebUtils.getRequestMap(request);
		ChangeEntry changeEntry = new ChangeEntry(goodsPrice);
		BindUtils.bind(goodsPrice, dataMap, false, GoodsPrice.disallowBindField);
		String quantity = dataMap.get("quantity");
		if(StringUtils.isBlank(quantity) || Integer.parseInt(quantity)<=0){
			return showJsonError(model, "价格购票，票数不能为空或小于1！");
		}
		//库存数
		Integer quantityInt = Integer.parseInt(quantity);
		goodsPrice.setAllowaddnum(quantityInt);
		//库存数+卖出数=总库存数
		goodsPrice.setQuantity(quantityInt + goodsPrice.getSellquantity());
		if(!StringUtil.regMatch(goodsPrice.getSection(), "^[1-9A-Z]$", false)){
			return showJsonError(model, "区域错误，必须是1-9或A-Z");
		}
		if(goods.getMinprice() == null || goods.getMinprice() > goodsPrice.getPrice()){
			goods.setMinprice(goodsPrice.getPrice());
		}else if((goods.getMaxprice() == null && goodsPrice.getPrice() > goods.getMinprice()) ||
					(goods.getMaxprice() != null && goods.getMaxprice() < goodsPrice.getPrice())){
			goods.setMaxprice(goodsPrice.getPrice());
		}
		goodsPrice.setUpdatetime(DateUtil.getCurFullTimestamp());
		daoService.saveObjectList(goodsPrice, goods);
		dbLogger.warn("用户："+user.getId()+"增加通票场次价格:"+goodsPrice.getPrice());
		monitorService.saveChangeLog(user.getId(), GoodsPrice.class, goods.getId(), changeEntry.getChangeMap(goodsPrice));
		return showJsonSuccess(model, ""+goodsPrice.getId());
	}
	
	@RequestMapping("/admin/goods/batchPriceStatus.xhtml")
	public String setGoodsPriceStatus(Long goodsid,String status,ModelMap model){
		BaseGoods goods = daoService.getObject(BaseGoods.class, goodsid);
		if(goods == null) return showJsonError(model, "该数据不存在或被删除！");
		List<GoodsPrice> tspList = daoService.getObjectListByField(GoodsPrice.class, "goodsid", goods.getId());
		for(GoodsPrice tsp : tspList){
			tsp.setStatus(status);
			daoService.saveObject(tsp);
		}
		return showJsonSuccess(model);
	}
	
	@RequestMapping("/admin/goods/setPriceStatus.xhtml")
	public String delOdiPrice(Long id, String status, ModelMap model) {
		if(StringUtils.isBlank(status)) return showJsonError(model, "状态不能为空！");
		User user = getLogonUser();
		GoodsPrice goodsPrice = daoService.getObject(GoodsPrice.class, id);
		ChangeEntry changeEntry = new ChangeEntry(goodsPrice);
		dbLogger.warn("用户：" + user.getId() + "删除通票场次价格:" + goodsPrice.getPrice());
		goodsPrice.setStatus(status);
		daoService.saveObject(goodsPrice);
		monitorService.saveChangeLog(user.getId(), GoodsPrice.class, goodsPrice.getId(), changeEntry.getChangeMap(goodsPrice));
		return showJsonSuccess(model);
	}
}
