package com.gewara.api.service.terminal;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import com.gewara.api.terminal.service.TerminalOrderVoService;
import com.gewara.api.terminal.vo.CustomPaperVo;
import com.gewara.api.terminal.vo.DpiLayoutVo;
import com.gewara.api.terminal.vo.SynchVo;
import com.gewara.api.vo.ResultCode;
import com.gewara.api.vo.movie.OpenPlayItemVo;
import com.gewara.api.vo.order.DramaOrderVo;
import com.gewara.api.vo.order.GoodsOrderVo;
import com.gewara.api.vo.order.SportOrderVo;
import com.gewara.api.vo.order.TicketOrderVo;
import com.gewara.constant.GoodsConstant;
import com.gewara.constant.OdiConstant;
import com.gewara.constant.TagConstant;
import com.gewara.constant.ticket.OrderConstant;
import com.gewara.helper.order.DramaOrderHelper;
import com.gewara.helper.order.GewaOrderHelper;
import com.gewara.json.CustomPaper;
import com.gewara.model.api.Synch;
import com.gewara.model.drama.DramaOrder;
import com.gewara.model.drama.OpenDramaItem;
import com.gewara.model.drama.SellDramaSeat;
import com.gewara.model.express.TicketFaceConfig;
import com.gewara.model.goods.BaseGoods;
import com.gewara.model.goods.Goods;
import com.gewara.model.pay.GoodsOrder;
import com.gewara.model.pay.OrderNote;
import com.gewara.model.pay.SportOrder;
import com.gewara.model.pay.TicketOrder;
import com.gewara.model.sport.OpenTimeTable;
import com.gewara.model.ticket.OpenPlayItem;
import com.gewara.model.ticket.SellSeat;
import com.gewara.mongo.MongoService;
import com.gewara.service.SynchService;
import com.gewara.service.drama.DramaOrderService;
import com.gewara.service.impl.BaseServiceImpl;
import com.gewara.service.ticket.TicketOrderService;
import com.gewara.support.ErrorCode;
import com.gewara.untrans.drama.RemoteDramaService;
import com.gewara.util.DateUtil;
import com.gewara.util.JsonUtils;
import com.gewara.util.VmUtils;
import com.gewara.util.VoCopyUtil;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

public class TerminalOrderVoServiceImpl extends BaseServiceImpl implements TerminalOrderVoService{
	@Autowired@Qualifier("synchService")
	private SynchService synchService;
	@Autowired@Qualifier("dramaOrderService")
	private DramaOrderService dramaOrderService;
	@Autowired@Qualifier("remoteDramaService")
	private RemoteDramaService remoteDramaService;
	@Autowired@Qualifier("mongoService")
	private MongoService mongoService;
	@Override
	public ResultCode<TicketOrderVo> getTicketOrderByTradeno(String tradeno) {
		TicketOrder order = baseDao.getObjectByUkey(TicketOrder.class, "tradeNo", tradeno);
		ResultCode<TicketOrderVo> vocode = VoCopyUtil.copyProperties(TicketOrderVo.class, order);
		if(!vocode.isSuccess()){
			return ResultCode.getFailure(vocode.getMsg());
		}
		TicketOrderVo vo = vocode.getRetval();
		initTicketOrder(vo, order);
		return ResultCode.getSuccessReturn(vo);
	}
	private void initTicketOrder(TicketOrderVo vo, TicketOrder order){
		List<SellSeat> seatList = ticketOrderService.getOrderSeatList(order.getId());
		String seatprice = GewaOrderHelper.getOrderSeatTextWithService(order, seatList);
		OpenPlayItem opi = baseDao.getObjectByUkey(OpenPlayItem.class, "mpid", order.getMpid());
		vo.setMoviename(opi.getMoviename());
		vo.setCinemaname(opi.getCinemaname());
		vo.setRoomname(opi.getRoomname());
		vo.setSeatprice(seatprice);
	}
	@Autowired@Qualifier("ticketOrderService")
	private TicketOrderService ticketOrderService;
	@Override
	public ResultCode<List<TicketOrderVo>> getSuccessTicketOrderList(Long cinemaid, Timestamp playtime, Timestamp addtime) {
		if(addtime==null){
			return ResultCode.getFailure("addtime 不能为可空！");
		}
		DetachedCriteria query = DetachedCriteria.forClass(TicketOrder.class);
		query.add(Restrictions.eq("cinemaid", cinemaid));
		query.add(Restrictions.eq("status", OrderConstant.STATUS_PAID_SUCCESS));
		query.add(Restrictions.ge("playtime", playtime));
		query.add(Restrictions.ge("addtime", addtime));
		query.addOrder(Order.desc("createtime"));
		List<TicketOrder> orderList = hibernateTemplate.findByCriteria(query);
		ResultCode<List<TicketOrderVo>> code = VoCopyUtil.copyListProperties(TicketOrderVo.class, orderList);
		for(TicketOrderVo vo : code.getRetval()){
			TicketOrder order = baseDao.getObjectByUkey(TicketOrder.class, "tradeNo", vo.getTradeNo());
			initTicketOrder(vo, order);
		}
		return code;
	}
	@Override
	public ResultCode<GoodsOrderVo> getGoodsOrderByTradeno(String tradeno) {
		GoodsOrder order = baseDao.getObjectByUkey(GoodsOrder.class, "tradeNo", tradeno);
		ResultCode<GoodsOrderVo> vocode = VoCopyUtil.copyProperties(GoodsOrderVo.class, order);
		if(!vocode.isSuccess()){
			return ResultCode.getFailure(vocode.getMsg());
		}
		GoodsOrderVo vo = vocode.getRetval();
		initGoodsOrder(vo, order);
		return ResultCode.getSuccessReturn(vo);
	}
	@Override
	public ResultCode<List<GoodsOrderVo>> getSuccessGoodsOrderList(Long goodsid, Long relatedid, Timestamp addtime) {
		if(addtime==null){
			return ResultCode.getFailure("addtime 不能为可空！");
		}
		if(goodsid==null && relatedid==null){
			return ResultCode.getFailure("goodsid 和 relatedid 不能同时可空！");
		}
		DetachedCriteria query = DetachedCriteria.forClass(GoodsOrder.class);
		if(goodsid!=null){
			query.add(Restrictions.eq("goodsid", goodsid));
		}
		if(relatedid!=null){
			query.add(Restrictions.eq("placeid", relatedid));
		}
		query.add(Restrictions.eq("status", OrderConstant.STATUS_PAID_SUCCESS));
		query.add(Restrictions.ge("addtime", addtime));
		query.addOrder(Order.desc("createtime"));
		List<GoodsOrder> orderList = hibernateTemplate.findByCriteria(query);
		
		ResultCode<List<GoodsOrderVo>> code = VoCopyUtil.copyListProperties(GoodsOrderVo.class, orderList);
		for(GoodsOrderVo vo : code.getRetval()){
			GoodsOrder order = baseDao.getObjectByUkey(GoodsOrder.class, "tradeNo", vo.getTradeNo());
			initGoodsOrder(vo, order);
		}
		return code;
	}
	
	private void initGoodsOrder(GoodsOrderVo vo, GoodsOrder order){
		BaseGoods goods = baseDao.getObject(BaseGoods.class, order.getGoodsid());
		if(goods instanceof Goods){
			vo.setGoodsname(goods.getGoodsname());
			vo.setShortname(goods.getShortname());
			vo.setPrintcontent(goods.getPrintcontent());
			vo.setPrice(order.getUnitprice());
			vo.setPlaceid(goods.getRelatedid());
		}
	}
	@Override
	public ResultCode<List<String>> getRefundOrderList(Timestamp refundtime) {
		if(refundtime==null){
			return ResultCode.getFailure("refundtime 不能为可空！");
		}
		Timestamp curtime = DateUtil.getMillTimestamp();
		Timestamp addtime = DateUtil.addDay(curtime, -30);
		String hql = "select tradeno from OrderRefund where tradeno is not null and addtime>? and refundtime>?";
		List<String> refundList = hibernateTemplate.find(hql, addtime, refundtime);
		return ResultCode.getSuccessReturn(refundList);
	}
	@Override
	public ResultCode<List<OpenPlayItemVo>> getPeakOpenPlayItemList(Long cinemaid, Timestamp starttime, Timestamp endtime) {
		if(cinemaid==null || starttime==null || endtime==null){
			return ResultCode.getFailure("查询条件值不能为空");
		}
		List<OpenPlayItem> opiList = synchService.getSynchPeakOpi(cinemaid, starttime, endtime);
		return VoCopyUtil.copyListProperties(OpenPlayItemVo.class, opiList);
	}
	@Override
	public ResultCode<List<Map>> getPeakPeriodByTag(Long placeid, Timestamp starttime, Timestamp endtime, String tag) {
		if(placeid==null || starttime==null || endtime==null){
			return ResultCode.getFailure("查询条件值不能为空");
		}
		List<Map> resList = synchService.getSynchPeakPeriod(placeid, starttime, endtime, tag);
		return ResultCode.getSuccessReturn(resList);
	}
	@Override
	public ResultCode<List<SynchVo>> getSynchByPlaceids(List<Long> placeidList) {
		if(VmUtils.size(placeidList)==0){
			return ResultCode.getFailure("查询条件值不能为空");
		}
		DetachedCriteria query = DetachedCriteria.forClass(Synch.class);
		query.add(Restrictions.in("cinemaid", placeidList));
		List<Synch> list = hibernateTemplate.findByCriteria(query);
		ResultCode<List<SynchVo>> code = VoCopyUtil.copyListProperties(SynchVo.class, list);
		return code;
	}
	@Override
	public ResultCode<SportOrderVo> getSportOrderByTradeno(String tradeno) {
		SportOrder order = baseDao.getObjectByUkey(SportOrder.class, "tradeNo", tradeno);
		ResultCode<SportOrderVo> vocode = VoCopyUtil.copyProperties(SportOrderVo.class, order);
		if(!vocode.isSuccess()){
			return ResultCode.getFailure(vocode.getMsg());
		}
		SportOrderVo vo = vocode.getRetval();
		initSportOrder(vo, order);
		return ResultCode.getSuccessReturn(vo);
	}
	
	private void initSportOrder(SportOrderVo vo, SportOrder order){
		Map<String, String> descMap = VmUtils.readJsonToMap(order.getDescription2());
		String timedes = descMap.get("时间");
		String detail = descMap.get("详细");
		String timelen = descMap.get("时长");
		OpenTimeTable ott = baseDao.getObject(OpenTimeTable.class, order.getOttid());
		vo.setSportname(ott.getSportname());
		vo.setItemname(ott.getItemname());
		vo.setPlaydate(ott.getPlaydate());
		vo.setPlaytime(DateUtil.parseTimestamp(timedes));
		vo.setTimelen(timelen);
		vo.setDetail(detail);
	}
	@Override
	public ResultCode<List<SportOrderVo>> getSuccessTicketOrderList(Long sportid, Long itemid, Timestamp addtime) {
		if(sportid==null || addtime==null){
			return ResultCode.getFailure("sportid和addtime 不能为空！");
		}
		DetachedCriteria query = DetachedCriteria.forClass(SportOrder.class);
		query.add(Restrictions.eq("sportid", sportid));
		if(itemid!=null){
			query.add(Restrictions.gt("itemid", itemid));
		}
		query.add(Restrictions.eq("status", OrderConstant.STATUS_PAID_SUCCESS));
		query.add(Restrictions.ge("addtime", addtime));
		query.addOrder(Order.desc("addtime"));
		List<SportOrder> orderList = hibernateTemplate.findByCriteria(query);
		ResultCode<List<SportOrderVo>> code = VoCopyUtil.copyListProperties(SportOrderVo.class, orderList);
		for(SportOrderVo vo : code.getRetval()){
			SportOrder order = baseDao.getObjectByUkey(SportOrder.class, "tradeNo", vo.getTradeNo());
			initSportOrder(vo, order);
		}
		return code;
	}
	private List<DramaOrderVo> initDramaOrder(DramaOrder order, DramaOrderVo vo){
		List<DramaOrderVo> voList = new ArrayList<DramaOrderVo>();
		List<OrderNote> noteList = baseDao.getObjectListByField(OrderNote.class, "orderid", order.getId());
		Map<String, String> otherMap = JsonUtils.readJsonToMap(order.getOtherinfo());
		String seatprice = "";
		String pricetype = "";
		String dis = "N";
		if(otherMap.containsKey("disid")){	
			dis = "Y";
		}
		for(OrderNote note : noteList){
			for(int i =1; i<=note.getTicketnum();i++){
				seatprice = seatprice + "," + order.getUnitprice();
				pricetype = pricetype + "," + dis;
			}
			OpenDramaItem odi = baseDao.getObjectByUkey(OpenDramaItem.class, "dpid", note.getSmallitemid());
			String ticketface = "";
			if(odi.hasSeller(OdiConstant.PARTNER_GPTBS)){
				ErrorCode<String> code = remoteDramaService.qryOrderPrintInfo(order.getId());
				if(code.isSuccess()){
					ticketface = code.getRetval();
				}
			}
			ResultCode<DramaOrderVo> vvocode = VoCopyUtil.copyProperties(DramaOrderVo.class, vo);
			DramaOrderVo tmpvo = vvocode.getRetval();
			tmpvo.setId(note.getId());
			tmpvo.setOrderid(note.getOrderid());
			tmpvo.setQuantity(note.getTicketnum());
			tmpvo.setCheckpass(note.getCheckpass());
			tmpvo.setTheatreid(note.getPlaceid());
			tmpvo.setDramaid(note.getItemid());
			tmpvo.setDramaname(odi.getDramaname());
			tmpvo.setTheatrename(odi.getTheatrename());
			tmpvo.setOpentype(odi.getOpentype());
			tmpvo.setRoomname(odi.getRoomname());
			tmpvo.setPeriod(odi.getPeriod());
			tmpvo.setPlaytime(odi.getPlaytime());
			tmpvo.setDpid(odi.getDpid());
			tmpvo.setTicketfaceid(odi.getTicketfaceid());
			tmpvo.setTicketface(ticketface);
			tmpvo.setSerialno(note.getSerialno());
			tmpvo.setDpiname(odi.getName());
			tmpvo.setSeller(odi.getSeller());
			tmpvo.setSellerseq(odi.getSellerseq());
			if(odi.isOpenseat()) {
				List<SellDramaSeat> seatList = dramaOrderService.getDramaOrderSeatList(order.getId());
				tmpvo.setSeatprice(DramaOrderHelper.getDramaOrderSeatText(seatList));
			}else {
				tmpvo.setSeatprice(seatprice.substring(1));
			}
			tmpvo.setPricetype(pricetype.substring(1));
			voList.add(tmpvo);
		}
		return voList;
	}
	@Override
	public ResultCode<List<DramaOrderVo>> getDramaOrderByTradeno(String tradeno) {
		DramaOrder order = baseDao.getObjectByUkey(DramaOrder.class, "tradeNo", tradeno);
		ResultCode<DramaOrderVo> vocode = VoCopyUtil.copyProperties(DramaOrderVo.class, order);
		if(!vocode.isSuccess()){
			return ResultCode.getFailure(vocode.getMsg());
		}
		DramaOrderVo vo = vocode.getRetval();
		List<DramaOrderVo> voList = initDramaOrder(order, vo); 
		return ResultCode.getSuccessReturn(voList);
	}
	@Override
	public ResultCode<DpiLayoutVo> getDpiLayoutVo(Long dpid) {
		OpenDramaItem odi = baseDao.getObjectByUkey(OpenDramaItem.class, "dpid", dpid);
		if(odi.hasSeller(OdiConstant.PARTNER_GPTBS)){
			ErrorCode<String> code = remoteDramaService.qryTicketPrice(odi.getSellerseq());
			if(code.isSuccess()){
				DpiLayoutVo vo = new DpiLayoutVo();
				vo.setContent(code.getRetval());
				vo.setDpid(dpid);
				vo.setSeller(odi.getSeller());
				return ResultCode.getSuccessReturn(vo);
			}else {
				return ResultCode.getFailure(code.getMsg());
			}
		}else if(odi.hasSeller(OdiConstant.PARTNER_GEWA)){
			if(StringUtils.isNotBlank(odi.getTicketfaceid())){
				TicketFaceConfig tfc = baseDao.getObject(TicketFaceConfig.class, odi.getTicketfaceid());
				DpiLayoutVo vo = new DpiLayoutVo();
				vo.setContent(tfc.getFacecontent());
				vo.setDpid(dpid);
				vo.setSeller(odi.getSeller());
				return ResultCode.getSuccessReturn(vo);
			}else {
				return ResultCode.getFailure("场次没有设置票版！");
			}
		}
		return ResultCode.getFailure("场次类型错误！");
	}
	@Override
	public ResultCode<String> getDramaOrderPrintInfo(String tradeno) {
		DramaOrder order = baseDao.getObjectByUkey(DramaOrder.class, "tradeNo", tradeno);
		ErrorCode<String> code = remoteDramaService.qryOrderPrintInfo(order.getId());
		if(code.isSuccess()){
			return ResultCode.getSuccessReturn(code.getRetval());
		}
		return ResultCode.getFailure(code.getMsg());
	}
	@Override
	public List<CustomPaperVo> getCustomPaperVoList(String tag, Date startdate, Date enddate) {
		DBObject queryCondition = new BasicDBObject();
		DBObject relate1 = mongoService.queryBasicDBObject("tag", "=", TagConstant.TAG_CINEMA);
		DBObject relate2 = mongoService.queryAdvancedDBObject("addtime", new String[]{">=","<="}, new Date[]{startdate, enddate});
		queryCondition.putAll(relate1);
		queryCondition.putAll(relate2);
		List<CustomPaper> customPaperList = mongoService.getObjectList(CustomPaper.class, queryCondition, "addtime", false, 0, 2000);
		List<CustomPaperVo> voList = new ArrayList<CustomPaperVo>();
		for(CustomPaper paper : customPaperList){
			ResultCode<CustomPaperVo> vocode = VoCopyUtil.copyProperties(CustomPaperVo.class, paper);
			if(vocode.isSuccess()){
				voList.add(vocode.getRetval());
			}
		}
		return voList;
	}
	@Override
	public ResultCode<List<DramaOrderVo>> getSuccessDramaOrderList(Long dramaid, Timestamp addtime) {
		if(dramaid==null){
			return ResultCode.getFailure("dramaid 不能为空！");
		}
		DetachedCriteria query = DetachedCriteria.forClass(DramaOrder.class);
		query.add(Restrictions.eq("dramaid", dramaid));
		if(addtime!=null){
			query.add(Restrictions.ge("addtime", addtime));
		}
		query.addOrder(Order.desc("addtime"));
		List<DramaOrder> orderList = hibernateTemplate.findByCriteria(query);
		List<DramaOrderVo> result = new ArrayList<DramaOrderVo>();
		for(DramaOrder order : orderList){
			ResultCode<DramaOrderVo> vocode = VoCopyUtil.copyProperties(DramaOrderVo.class, order);
			if(vocode.isSuccess()){
				result.addAll(initDramaOrder(order, vocode.getRetval()));
			}
		}
		return ResultCode.getSuccessReturn(result);
	}
	@Override
	public ResultCode<List<String>> getSuccessTicketOrderListByPlaytime(List<Long> cinemaidList, Timestamp startPlaytime, Timestamp endPlaytime) {
		if(startPlaytime==null || endPlaytime==null){
			return ResultCode.getFailure("startPlaytime,endPlaytime 不能为可空！");
		}
		if(cinemaidList.size()==0){
			return ResultCode.getFailure("影院id不能为空！");
		}
		DetachedCriteria query = DetachedCriteria.forClass(TicketOrder.class);
		query.add(Restrictions.in("cinemaid", cinemaidList));
		query.add(Restrictions.eq("status", OrderConstant.STATUS_PAID_SUCCESS));
		query.add(Restrictions.ge("playtime", startPlaytime));
		query.add(Restrictions.lt("playtime", endPlaytime));
		query.setProjection(Projections.property("tradeNo"));
		List<String> tradeNoList = hibernateTemplate.findByCriteria(query);
		return ResultCode.getSuccessReturn(tradeNoList);
	}
	@Override
	public ResultCode<List<GoodsOrderVo>> getMealSuccessGoodsOrderList(List<Long> placeidList, Timestamp starttime, Timestamp endtime) {
		if(starttime==null || endtime==null){
			return ResultCode.getFailure("时间 不能为可空！");
		}
		if(placeidList.size()==0){
			return ResultCode.getFailure("场馆id不能为空！");
		}
		DetachedCriteria query = DetachedCriteria.forClass(GoodsOrder.class);
		query.add(Restrictions.in("placeid", placeidList));
		query.add(Restrictions.eq("status", OrderConstant.STATUS_PAID_SUCCESS));
		query.add(Restrictions.eq("category", GoodsConstant.GOODS_TYPE_GOODS));
		query.add(Restrictions.eq("pricategory", TagConstant.TAG_MOVIE));
		query.add(Restrictions.ge("addtime", starttime));
		query.add(Restrictions.le("addtime", endtime));
		query.addOrder(Order.desc("addtime"));
		List<GoodsOrder> orderList = hibernateTemplate.findByCriteria(query);
		
		ResultCode<List<GoodsOrderVo>> code = VoCopyUtil.copyListProperties(GoodsOrderVo.class, orderList);
		for(GoodsOrderVo vo : code.getRetval()){
			GoodsOrder order = baseDao.getObjectByUkey(GoodsOrder.class, "tradeNo", vo.getTradeNo());
			initGoodsOrder(vo, order);
		}
		return code;
	}
}
