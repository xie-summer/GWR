package com.gewara.web.action.admin.ticket;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.support.PropertyComparator;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import com.gewara.constant.GoodsConstant;
import com.gewara.constant.PaymethodConstant;
import com.gewara.constant.Status;
import com.gewara.constant.ticket.OpiConstant;
import com.gewara.constant.ticket.OrderConstant;
import com.gewara.model.acl.User;
import com.gewara.model.common.BaseInfo;
import com.gewara.model.drama.Theatre;
import com.gewara.model.express.ExpressConfig;
import com.gewara.model.goods.ActivityGoods;
import com.gewara.model.goods.BaseGoods;
import com.gewara.model.goods.Goods;
import com.gewara.model.goods.GoodsGift;
import com.gewara.model.goods.GoodsPrice;
import com.gewara.model.goods.GoodsSportGift;
import com.gewara.model.goods.GoodsTheatreGift;
import com.gewara.model.movie.Cinema;
import com.gewara.model.pay.ElecCardBatch;
import com.gewara.model.pay.GoodsOrder;
import com.gewara.model.pay.OrderAddress;
import com.gewara.model.pay.PayBank;
import com.gewara.model.sport.Sport;
import com.gewara.service.BarcodeService;
import com.gewara.service.gewapay.PaymentService;
import com.gewara.service.movie.MCPService;
import com.gewara.service.order.GoodsOrderService;
import com.gewara.service.order.GoodsService;
import com.gewara.service.sport.SportService;
import com.gewara.support.ErrorCode;
import com.gewara.support.MultiPropertyComparator;
import com.gewara.support.ServiceHelper;
import com.gewara.untrans.CacheService;
import com.gewara.untrans.activity.SynchActivityService;
import com.gewara.untrans.monitor.RoleTag;
import com.gewara.util.BeanUtil;
import com.gewara.util.BindUtils;
import com.gewara.util.ChangeEntry;
import com.gewara.util.JsonUtils;
import com.gewara.util.ValidateUtil;
import com.gewara.util.VmUtils;
import com.gewara.web.action.admin.BaseAdminController;
import com.gewara.xmlbind.activity.RemoteActivity;

@Controller
public class GoodsAdminController extends BaseAdminController{
	@Autowired@Qualifier("cacheService")
	private CacheService cacheService;
	public void setCacheService(CacheService cacheService) {
		this.cacheService = cacheService;
	}
	@Autowired@Qualifier("sportService")
	private SportService sportService;
	public void setSportService(SportService sportService) {
		this.sportService = sportService;
	}
	@Autowired@Qualifier("goodsOrderService")
	private GoodsOrderService goodsOrderService;
	public void setGoodsOrderService(GoodsOrderService goodsOrderService) {
		this.goodsOrderService = goodsOrderService;
	}
	@Autowired@Qualifier("goodsService")
	private GoodsService goodsService;
	public void setGoodsService(GoodsService goodsService) {
		this.goodsService = goodsService;
	}
	@Autowired@Qualifier("mcpService")
	private MCPService mcpService;
	public void setMCPService(MCPService mcpService) {
		this.mcpService = mcpService;
	}
	@Autowired@Qualifier("synchActivityService")
	private SynchActivityService synchActivityService;
	public void setActivityRemoteService(SynchActivityService synchActivityService) {
		this.synchActivityService = synchActivityService;
	}
	@Autowired@Qualifier("paymentService")
	private PaymentService paymentService;
	@Autowired@Qualifier("barcodeService")
	private BarcodeService barcodeService;
	
	//积分兑换
	@RequestMapping("/admin/goods/goodsListToPoint.xhtml")
	public String pointExchangeList(ModelMap model){
		model.put("tag", "point");
		String qry = "from Goods g where g.tag=? and g.status!=? order by g.goodssort asc, g.addtime desc";
		List<Goods> goodsList = hibernateTemplate.find(qry, GoodsConstant.GOODS_TAG_POINT, Status.DEL);
		Map<Long, Integer> quantityMap = new HashMap<Long, Integer>();
		for(Goods goods : goodsList){
			int sum = goodsOrderService.getGoodsOrderQuantity(goods.getId(), OrderConstant.STATUS_PAID_SUCCESS);
			quantityMap.put(goods.getId(), goods.getQuantity()-sum);
		}
		model.put("goodsList", goodsList);
		model.put("quantityMap", quantityMap);
		return "admin/goods/goodsList_point.vm";
	}
	//活动收费
	@RequestMapping("/admin/goods/goodsListToActivity.xhtml")
	public String activityList(String time, ModelMap model){
		String qry = "from ActivityGoods g where g.status!=? " ;
		if(StringUtils.isNotBlank(time)) qry = qry + "and g.totime<? ";
		else qry = qry + "and g.totime>=? ";
		qry  = qry + "order by g.goodssort asc, g.addtime desc";
		List<ActivityGoods> goodsList= hibernateTemplate.find(qry, Status.DEL, new Timestamp(System.currentTimeMillis()));
		model.put("goodsList", goodsList);
		return "admin/goods/goodsList_activity.vm";
	}
	
	//爆米花
	@RequestMapping("/admin/goods/goodsListToBMH.xhtml")
	public String goodsListToBMH(String type, String datetype, ModelMap model, HttpServletRequest request){
		String citycode = getAdminCitycode(request);
		String tag = GoodsConstant.GOODS_TAG_BMH;
		model.put("tag", tag);
		boolean isToTime = false;
		if(StringUtils.isBlank(datetype) || StringUtils.equals(datetype, "cur")) isToTime = true;
		List<Goods> gList = goodsService.getGoodsList(Goods.class, citycode, tag, null, isToTime, false, false, null, false);
		Map<Long, BaseInfo> cinemaMap = new HashMap<Long, BaseInfo>();
		Map<Long, GoodsGift> giftMap = new HashMap<Long, GoodsGift>();
		Map<Long, GoodsSportGift> sportGiftMap = new HashMap<Long, GoodsSportGift>();
		Map<Long, GoodsTheatreGift> tgiftMap = new HashMap<Long, GoodsTheatreGift>();
		List<Goods> goodsList = new ArrayList<Goods>();
		for(Goods goods : gList){
			BaseInfo bi =null;
			GoodsGift gift = null;
			GoodsSportGift gsf = null;
			if(GoodsConstant.GOODS_TAG_BMH.equals(goods.getTag())){
				bi = daoService.getObject(Cinema.class, goods.getRelatedid());
				gift = daoService.getObjectByUkey(GoodsGift.class, "goodsid", goods.getId(), true);
			}else if(GoodsConstant.GOODS_TAG_BMH_SPORT.equals(goods.getTag())){
				bi = daoService.getObject(Sport.class, goods.getRelatedid());
				gsf=daoService.getObjectByUkey(GoodsSportGift.class, "goodsid", goods.getId(), true);
			}else if(GoodsConstant.GOODS_TAG_BMH_THEATRE.equals(goods.getTag())){
				bi = daoService.getObject(Theatre.class, goods.getRelatedid());
				GoodsTheatreGift tgift = daoService.getObjectByUkey(GoodsTheatreGift.class, "goodsid", goods.getId(), true);
				tgiftMap.put(goods.getId(), tgift);
			}
			if(bi==null){
				bi = daoService.getObject(Sport.class, goods.getRelatedid());
			}
			cinemaMap.put(goods.getId(), bi);
			giftMap.put(goods.getId(), gift);
			sportGiftMap.put(goods.getId(), gsf);
			if("gift".equals(type)){
				if(gift!=null)goodsList.add(goods);
			}else {
				goodsList.add(goods);
			}
		}
		model.put("goodsList", goodsList);
		model.put("giftMap", giftMap);
		model.put("sportGiftMap", sportGiftMap);
		model.put("tgiftMap", tgiftMap);
		model.put("cinemaMap", cinemaMap);
		return "admin/goods/goodsList_bmh.vm";
	}
	@RequestMapping("/admin/goods/goodsGift.xhtml")
	public String goodsGift(Long goodsid, ModelMap model){
		Goods goods = daoService.getObject(Goods.class, goodsid);
		if(GoodsConstant.GOODS_TAG_BMH.equals(goods.getTag())){
			Cinema cinema = daoService.getObject(Cinema.class, goods.getRelatedid());
			GoodsGift gift = daoService.getObjectByUkey(GoodsGift.class, "goodsid", goodsid, true);
			model.put("gift", gift);
			model.put("cinema", cinema);
		}else if(GoodsConstant.GOODS_TAG_BMH_SPORT.equals(goods.getTag())){
			Sport sport=daoService.getObject(Sport.class, goods.getRelatedid());
			List<Map> dateListMap=sportService.getMaxHourAndMinHour(goods.getRelatedid());
		
			String hourmax=null;
			String hourmin=null;
			if(dateListMap.size()>0){
				for(Map map:dateListMap){
					hourmin=(String)map.get("hourmin");
					hourmax=(String)map.get("hourmax");
				}
				List<Integer> hourlist=new ArrayList<Integer>();
				if(StringUtils.isNotBlank(hourmin)&&StringUtils.isNotBlank(hourmax)){
					Integer starthour=Integer.parseInt(StringUtils.substring(hourmin, 0, 2));
					Integer endhour=Integer.parseInt(StringUtils.substring(hourmax, 0, 2));
					for(int i=0;i<endhour-starthour+1;i++){
						hourlist.add(starthour+i);
					}
					model.put("hourlist", hourlist);
				}
			}
			model.put("sport", sport);
		}else if (GoodsConstant.GOODS_TAG_BMH_THEATRE.equals(goods.getTag())) {
			Theatre theatre = daoService.getObject(Theatre.class, goods.getRelatedid());
			GoodsTheatreGift gift = daoService.getObjectByUkey(GoodsTheatreGift.class, "goodsid", goodsid, true);
			model.put("gift", gift);
			model.put("theatre", theatre);
		}
		model.put("goods", goods);
		return "admin/goods/addGoodsGift.vm";
	}
	@RequestMapping("/admin/goods/addGoods.xhtml")
	public String addGoods(HttpServletRequest request, Long id, String tag, ModelMap model){
		BaseGoods goods = null;
		if(id!=null) {
			goods = daoService.getObject(BaseGoods.class, id);
			model.put("goods", goods);
			tag =  goods.getTag();
		}
		model.put("tag", tag);
		if(StringUtils.equals(GoodsConstant.GOODS_TAG_BMH, tag)||StringUtils.equals(GoodsConstant.GOODS_TAG_BMH_SPORT, tag) || StringUtils.equals(GoodsConstant.GOODS_TAG_BMH_THEATRE, tag)) {
			List<Cinema> orderCinemaList = mcpService.getBookingCinemaList(getAdminCitycode(request));
			model.put("orderCinemaList", orderCinemaList);
			return "admin/goods/addGoods_bmh.vm";
		}else if(StringUtils.equals(GoodsConstant.GOODS_TAG_POINT, tag)){
			return "admin/goods/addGoods_point.vm";
		}else if(StringUtils.equals(GoodsConstant.GOODS_TYPE_ACTIVITY, tag)){
			if(goods!=null) {
				ErrorCode<RemoteActivity> code = synchActivityService.getRemoteActivity(goods.getRelatedid());
				RemoteActivity activity = code.getRetval();
				model.put("activity", activity);
			}
			return "admin/goods/addGoods_activity.vm";
		}
		return null;
	}
	@RequestMapping("/admin/goods/goodsOrderList.xhtml")
	public String goodsOrderList(Long gid, String status, String tradeNo, String mobile, Timestamp timeFrom, Timestamp timeTo,  String ctype, String report, ModelMap model, HttpServletResponse response){
		BaseGoods goods = daoService.getObject(BaseGoods.class, gid);
		if(goods == null) return show404(model, "该商品不存在。。。");
		List<GoodsOrder> orderList = goodsOrderService.getGoodsOrderList(gid, status, tradeNo, mobile, timeFrom, timeTo);
		Collections.sort(orderList, new MultiPropertyComparator(new String[]{"addtime"}, new boolean[]{false}));
		List<String> tradeNoList = BeanUtil.getBeanPropertyList(orderList, "tradeNo", true);
		Map<String, OrderAddress> addressMap = daoService.getObjectMap(OrderAddress.class, tradeNoList);
		model.put("addressMap", addressMap);
		model.put("goods", goods);
		model.put("orderList", orderList);
		List<Long> memberidList = ServiceHelper.getMemberIdListFromBeanList(orderList);
		addCacheMember(model, memberidList);
		if(StringUtils.isNotBlank(report)){
			model.put(REPORT_DATA_KEY, orderList);
			return showReportView(model, report, "order/goodsOrderAddress.jasper");
		}
		
		if(StringUtils.equals(goods.getTag(), GoodsConstant.GOODS_TAG_POINT)) return "admin/gewapay/pointOrderList.vm";
		if(StringUtils.isNotBlank(ctype)){
			download("xls", response);
			return "admin/goods/exportGoodsOrder.vm";
		}
		return "admin/goods/goodsOrderList.vm";
	}
	//Goods Ajax Manager ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	@RequestMapping("/admin/goods/ajax/delGoodsGift.xhtml")
	public String delGoodsGift(Long goodsid, ModelMap model){
		GoodsGift gift = daoService.getObjectByUkey(GoodsGift.class, "goodsid", goodsid, true);
		String msg = "";
		if(gift!=null){
			if(StringUtils.isNotBlank(gift.getMpidlist())){ 
				Goods goods = daoService.getObject(Goods.class, gift.getGoodsid());
				msg = "绑定场次套餐已删除，注意对外关闭的场次需要手动开放！";
				monitorService.saveSysWarn(msg, "物品名称:" + goods.getGoodsname(), RoleTag.dingpiao);
			}
			daoService.removeObject(gift);
			cacheService.cleanUkey(GoodsGift.class, "goodsid", goodsid);
		}
		return showJsonSuccess(model, msg);
	}
	
	@RequestMapping("/admin/goods/ajax/delSportGoodsGift.xhtml")
	public String delSportGoodsGift(Long goodsid, ModelMap model){
		GoodsSportGift sportgift = daoService.getObjectByUkey(GoodsSportGift.class, "goodsid", goodsid, true);
		String msg = "";
		if(sportgift!=null){
			daoService.removeObject(sportgift);
			cacheService.cleanUkey(GoodsSportGift.class, "goodsid", goodsid);
		}
		return showJsonSuccess(model, msg);
	}
	@RequestMapping("/admin/goods/ajax/saveGoodsGift.xhtml")
	public String saveGoodsGift(Long goodsid, String rate1, String rate2, 
			String rate3, String rate4, String rate5, 
			HttpServletRequest request, ModelMap model){
		GoodsGift gift = daoService.getObjectByUkey(GoodsGift.class, "goodsid", goodsid, true);
		if(gift==null) gift = new GoodsGift();
		BindUtils.bindData(gift, request.getParameterMap());
		String rateinfo = "1:" + rate1 + ",2:" + rate2 + ",3:" + rate3 + ",4:" + rate4 + ",5:"+rate5;
		gift.setRateinfo(rateinfo);
		daoService.saveObject(gift);
		return showJsonSuccess(model, gift.getGoodsid()+"");
	}
	
	@RequestMapping("/admin/goods/ajax/saveGoodsSportGift.xhtml")
	public String saveGoodsSportGift(Long goodsid, String rate1, String rate2, 
			String rate3, String rate4, String rate5, 
			HttpServletRequest request, ModelMap model){
		GoodsSportGift sportgift = daoService.getObjectByUkey(GoodsSportGift.class, "goodsid", goodsid, true);
		if(sportgift==null) sportgift = new GoodsSportGift();
		BindUtils.bindData(sportgift, request.getParameterMap());
		String rateinfo = "1:" + rate1 + ",2:" + rate2 + ",3:" + rate3 + ",4:" + rate4 + ",5:"+rate5;
		sportgift.setRateinfo(rateinfo);
		daoService.saveObject(sportgift);
		return showJsonSuccess(model, sportgift.getGoodsid()+"");
	}
	@RequestMapping("/admin/goods/ajax/saveGoodsTheatreGift.xhtml")
	public String saveGoodsTheatreGift(Long goodsid, String rate1, String rate2, 
			String rate3, String rate4, String rate5, 
			HttpServletRequest request, ModelMap model){
		GoodsTheatreGift tgift = daoService.getObjectByUkey(GoodsTheatreGift.class, "goodsid", goodsid, true);
		if(tgift==null) tgift = new GoodsTheatreGift();
		BindUtils.bindData(tgift, request.getParameterMap());
		String rateinfo = "1:" + rate1 + ",2:" + rate2 + ",3:" + rate3 + ",4:" + rate4 + ",5:"+rate5;
		tgift.setRateinfo(rateinfo);
		daoService.saveObject(tgift);
		return showJsonSuccess(model, tgift.getGoodsid()+"");
	}
	
	@RequestMapping("/admin/goods/ajax/delgoods.xhtml")
	public String delgoods(Long gid, ModelMap model){
		BaseGoods goods = daoService.getObject(BaseGoods.class, gid);
		ChangeEntry changeEntry = new ChangeEntry(goods);
		goods.setStatus(Status.DEL);
		daoService.saveObject(goods);
		monitorService.saveChangeLog(getLogonUser().getId(), BaseGoods.class, gid, changeEntry.getChangeMap(goods));
		return showJsonSuccess(model);
	}
	
	@RequestMapping("/admin/goods/delGoodsList.xhtml")
	public String delGoodsList(ModelMap model) {
		List<Goods> goodsList = daoService.getObjectListByField(Goods.class, "status", Status.DEL);
		Collections.sort(goodsList, new PropertyComparator("addtime", false, false));
		model.put("goodsList", goodsList);
		return "admin/goods/delGoodsList.vm";
	}
	@RequestMapping("/admin/goods/ajax/renewgoods.xhtml")
	public String renewgoods(Long gid, ModelMap model){
		BaseGoods goods = daoService.getObject(BaseGoods.class, gid);
		ChangeEntry changeEntry = new ChangeEntry(goods);
		goods.setStatus(Status.Y);
		daoService.saveObject(goods);
		monitorService.saveChangeLog(getLogonUser().getId(), BaseGoods.class, gid, changeEntry.getChangeMap(goods));
		return showJsonSuccess(model);
	}
	@RequestMapping("/admin/goods/ajax/getGoodsById.xhtml")
	public String getGoodsById(Long goodsId, ModelMap model){
		Goods goods = daoService.getObject(Goods.class, goodsId);
		Map result = BeanUtil.getBeanMap(goods);
		return showJsonSuccess(model, result);
	}
	
	@RequestMapping("/admin/goods/ajax/saveGoods.xhtml")
	public String saveGoods(HttpServletRequest request, String goodsname, Integer unitprice, Long id, ModelMap model){
		User user = getLogonUser();
		BaseGoods goods = null;
		String deliver = request.getParameter("deliver");
		String expressid = request.getParameter("expressid");
		if (StringUtils.equals(deliver, "address") && StringUtils.isBlank(expressid)) {
			return showJsonError(model, "必须填写快递方式！");
		}
		if (StringUtils.equals(deliver, "address") && StringUtils.isNotBlank(expressid)) {
			ExpressConfig config = daoService.getObject(ExpressConfig.class, expressid);
			if(config == null) {
				return showJsonError(model, "编号为：" + expressid + ",的配送方式不存在或被删除！");
			}
		}
		if(id != null){
			goods =  daoService.getObject(BaseGoods.class, new Long(id));
			goods.setClerkid(user.getId());
		}else{
			goods = new Goods(goodsname, unitprice, user.getId());
			String citycode = getAdminCitycode(request);
			goods.setCitycode(citycode);
		}
		goods.setManager(GoodsConstant.MANAGER_USER);
		BindUtils.bindData(goods, request.getParameterMap());
		if(goods.getAllowaddnum()==null){
			goods.setAllowaddnum(0);
		}
		//验证内容
		String msg=ValidateUtil.validateNewsContent(null, goods.getDescription());
		if(StringUtils.isNotBlank(msg)) return showJsonError(model, msg);
		daoService.saveObject(goods);
		return showJsonSuccess(model, BeanUtil.getBeanMap(goods));
	}
	@RequestMapping("/admin/goods/ajax/copyGoodsById.xhtml")
	public String copyGoodsById(Long gId, ModelMap model){
		BaseGoods goods = daoService.getObject(BaseGoods.class, gId);
		Goods newgoods = new Goods();
		try {
			PropertyUtils.copyProperties(newgoods, goods);
			newgoods.setId(null);
			daoService.saveObject(newgoods);
		} catch (Exception e) {
			dbLogger.error("", e);
		} 
		return showJsonSuccess(model);
	}
	@RequestMapping("/admin/goods/ajax/changeGoodsSort.xhtml")
	public String changeGoodsSort(ModelMap model, Long goodsId, Integer num){
		BaseGoods goods = daoService.getObject(BaseGoods.class, goodsId);
		if(goods==null) return showJsonError(model, "该数据不存在！");
		goods.setGoodssort(num);
		daoService.saveObject(goods);
		return showJsonSuccess(model);
	}
	@RequestMapping("/admin/goods/goodsOther.xhtml")
	public String goodsOther(Long goodsid, ModelMap model) {
		BaseGoods goods = daoService.getObject(BaseGoods.class, goodsid);
		List<PayBank> bankList = paymentService.getPayBankList(PayBank.TYPE_PC);
		model.put("goods", goods);
		model.put("otherinfo", goods.getOtherinfo());
		model.put("confPayList", bankList);
		model.put("payTextMap", PaymethodConstant.getPayTextMap());
		return "admin/goods/goodsOther.vm";
	}
	@RequestMapping("/admin/goods/saveGoodsOther.xhtml")
	public String saveOpiOther(Long goodsid, String payoption, String paymethodlist, String defaultpaymethod, 
			String cardoption, String batchidlist, String barcode, HttpServletRequest request, ModelMap model) {
		BaseGoods goods = daoService.getObject(BaseGoods.class, goodsid);
		BindUtils.bindData(goods, request.getParameterMap());
		daoService.saveObject(goods);
		goods = daoService.getObject(BaseGoods.class, goodsid);
		Map<String, String> otherinfo = VmUtils.readJsonToMap(goods.getOtherinfo());
		if(StringUtils.equals(paymethodlist, ",")) paymethodlist = "";
		if(StringUtils.equals(payoption, "del")) {
			otherinfo.remove(OpiConstant.PAYOPTION);
			otherinfo.remove(OpiConstant.PAYCMETHODLIST);
			otherinfo.remove(OpiConstant.DEFAULTPAYMETHOD);
		}else if(StringUtils.isNotBlank(payoption)){
			otherinfo.put(OpiConstant.PAYOPTION, payoption);
			if(StringUtils.isNotBlank(paymethodlist)) { 
				paymethodlist = checkpaymethodlist(paymethodlist);
				List<String> paymethodList = Arrays.asList(StringUtils.split(paymethodlist, ","));
				if(StringUtils.isBlank(defaultpaymethod) && paymethodList.size()!=1) return showJsonError(model, "请选择默认支付方式");
				
				otherinfo.put(OpiConstant.DEFAULTPAYMETHOD, defaultpaymethod);
				otherinfo.put(OpiConstant.PAYCMETHODLIST, paymethodlist);
			}else {
				otherinfo.remove(OpiConstant.DEFAULTPAYMETHOD);
				otherinfo.remove(OpiConstant.PAYCMETHODLIST);
			}
			if(StringUtils.equals(payoption, "notuse") && StringUtils.isBlank(paymethodlist)){
				return showJsonError(model, "支付方式选择不可用，必须勾选支付方式！");
			}
		}
		if(StringUtils.equals(cardoption, "del")) {
			otherinfo.remove(OpiConstant.CARDOPTION);
			otherinfo.remove(OpiConstant.BATCHIDLIST);
		}else if(StringUtils.isNotBlank(cardoption) && StringUtils.isNotBlank(batchidlist)){
			String[] batchidList = StringUtils.split(batchidlist, ",");
			for(String batchid : batchidList){
				ElecCardBatch batch = daoService.getObject(ElecCardBatch.class, new Long(batchid));
				if(batch==null) return showJsonError(model, batchid+"对应的批次不存在！");
			}
			otherinfo.put(OpiConstant.CARDOPTION, cardoption);
			otherinfo.put(OpiConstant.BATCHIDLIST, batchidlist);
		}
		goods.setOtherinfo(JsonUtils.writeMapToJson(otherinfo));
		daoService.saveObject(goods);
		if(StringUtils.equals(barcode, Status.Y) && goods.getRelatedid()!=null){
			barcodeService.createNewBarcodeByPlaceid(goods.getRelatedid());
		}
		return showJsonSuccess(model);
	}
	private String checkpaymethodlist(String paymethodlist){
		if(StringUtils.equals(paymethodlist, ",")) paymethodlist = "";
		return VmUtils.printList(Arrays.asList(StringUtils.split(paymethodlist, ",")));
	}
	@RequestMapping("/admin/goods/removeGoodsOther.xhtml")
	public String removeOpiOther(Long goodsid, String payoption, String cardoption, ModelMap model) {
		BaseGoods goods = daoService.getObject(BaseGoods.class, goodsid);
		Map<String, String> otherinfo = VmUtils.readJsonToMap(goods.getOtherinfo());
		if(StringUtils.isNotBlank(payoption)) {
			otherinfo.remove(OpiConstant.PAYOPTION);
			otherinfo.remove(OpiConstant.PAYCMETHODLIST);
			otherinfo.remove(OpiConstant.DEFAULTPAYMETHOD);
		}
		if(StringUtils.isNotBlank(cardoption)) {
			otherinfo.remove(OpiConstant.CARDOPTION);
			otherinfo.remove(OpiConstant.BATCHIDLIST);
		}
		goods.setOtherinfo(JsonUtils.writeMapToJson(otherinfo));
		daoService.saveObject(goods);
		return showJsonSuccess(model);
	}
	
	@RequestMapping("/admin/goods/updateElecard.xhtml")
	public String updateElecard(Long gid, String elecard, ModelMap model){
		BaseGoods goods = daoService.getObject(BaseGoods.class, gid);
		if(goods == null) return showJsonError(model, "该数据不存在或被删除！");
		ChangeEntry changeEntry = new ChangeEntry(goods);
		goods.setElecard(elecard);
		daoService.saveObject(goods);
		monitorService.saveChangeLog(getLogonUser().getId(), BaseGoods.class, gid, changeEntry.getChangeMap(goods));
		return showJsonSuccess(model);
	}
	
	@RequestMapping("/admin/goods/updateFromtime.xhtml")
	public String updateFromtime(Long gid, Timestamp fromtime, ModelMap model){
		if(fromtime == null) return showJsonError(model, "时间不能为空！");
		BaseGoods goods = daoService.getObject(BaseGoods.class, gid);
		if(goods == null) return showJsonError(model, "该数据不存在或被删除！");
		ChangeEntry changeEntry = new ChangeEntry(goods);
		goods.setFromtime(fromtime);
		daoService.saveObject(goods);
		monitorService.saveChangeLog(getLogonUser().getId(), BaseGoods.class, gid, changeEntry.getChangeMap(goods));
		return showJsonSuccess(model);
	}
	
	@RequestMapping("/admin/goods/updateTotime.xhtml")
	public String updateTotime(Long gid, Timestamp totime, ModelMap model){
		if(totime == null) return showJsonError(model, "时间不能为空！");
		BaseGoods goods = daoService.getObject(BaseGoods.class, gid);
		if(goods == null) return showJsonError(model, "该数据不存在或被删除！");
		ChangeEntry changeEntry = new ChangeEntry(goods);
		goods.setTotime(totime);
		daoService.saveObject(goods);
		monitorService.saveChangeLog(getLogonUser().getId(), BaseGoods.class, gid, changeEntry.getChangeMap(goods));
		return showJsonSuccess(model);
	}
	
	@RequestMapping("/admin/goods/updateExpress.xhtml")
	public String updateExpressid(Long gid, String expressid, ModelMap model){
		BaseGoods goods = daoService.getObject(BaseGoods.class, gid);
		if(goods == null) return showJsonError(model, "该数据不存在或被删除！");
		ChangeEntry changeEntry = new ChangeEntry(goods);
		if(StringUtils.isNotBlank(expressid)){
			ExpressConfig config = daoService.getObject(ExpressConfig.class, expressid);
			if(config == null) return showJsonError(model, "编号为：" + expressid + ",的配送方式不存在或被删除！");
			goods.setExpressid(expressid);
		}else{
			goods.setExpressid(null);
		}
		daoService.saveObject(goods);
		monitorService.saveChangeLog(getLogonUser().getId(), BaseGoods.class, gid, changeEntry.getChangeMap(goods));
		return showJsonSuccess(model);
	}
	
	@RequestMapping("/admin/goods/updateStatus.xhtml")
	public String updateStatus(Long gid, String status, ModelMap model){
		BaseGoods goods = daoService.getObject(BaseGoods.class, gid);
		if(goods == null) return showJsonError(model, "该数据不存在或被删除！");
		if(StringUtils.isBlank(status)) return showJsonError(model, "状态不能为空！");
		if(StringUtils.equals(status, Status.Y)){
			List<GoodsPrice> goodsPriceList = daoService.getObjectListByField(GoodsPrice.class, "goodsid", goods.getId());
			if(CollectionUtils.isEmpty(goodsPriceList)) return showJsonError(model, "价格没有设置不能开放！");
		}
		ChangeEntry changeEntry = new ChangeEntry(goods);
		goods.setStatus(status);
		daoService.saveObject(goods);
		monitorService.saveChangeLog(getLogonUser().getId(), BaseGoods.class, gid, changeEntry.getChangeMap(goods));
		String isBook = "noBook";
		if(goods.hasBooking()){
			isBook = "isBook";
		}
		return showJsonSuccess(model,isBook);
	}
	
}
