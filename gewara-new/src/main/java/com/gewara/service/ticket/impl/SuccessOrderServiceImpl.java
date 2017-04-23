package com.gewara.service.ticket.impl;

import java.sql.Timestamp;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.RandomUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.gewara.constant.MemberConstant;
import com.gewara.constant.PayConstant;
import com.gewara.constant.TagConstant;
import com.gewara.constant.sys.JsonDataKey;
import com.gewara.constant.ticket.OrderConstant;
import com.gewara.helper.order.DramaOrderContainer;
import com.gewara.helper.order.GoodsOrderContainer;
import com.gewara.helper.order.SportOrderContainer;
import com.gewara.helper.order.TicketOrderContainer;
import com.gewara.model.api.OrderResult;
import com.gewara.model.common.JsonData;
import com.gewara.model.drama.Drama;
import com.gewara.model.drama.DramaOrder;
import com.gewara.model.drama.OpenDramaItem;
import com.gewara.model.drama.Theatre;
import com.gewara.model.goods.BaseGoods;
import com.gewara.model.goods.TicketGoods;
import com.gewara.model.movie.CinemaProfile;
import com.gewara.model.movie.Movie;
import com.gewara.model.pay.ElecCard;
import com.gewara.model.pay.ElecCardBatch;
import com.gewara.model.pay.GoodsOrder;
import com.gewara.model.pay.OrderExtra;
import com.gewara.model.pay.SMSRecord;
import com.gewara.model.pay.SportOrder;
import com.gewara.model.pay.TicketOrder;
import com.gewara.model.sport.OpenTimeItem;
import com.gewara.model.sport.OpenTimeTable;
import com.gewara.model.sport.SellTimeTable;
import com.gewara.model.ticket.OpenPlayItem;
import com.gewara.service.MessageService;
import com.gewara.service.gewapay.ElecCardService;
import com.gewara.service.impl.BaseServiceImpl;
import com.gewara.service.member.MemberService;
import com.gewara.service.member.PointService;
import com.gewara.service.order.GoodsOrderService;
import com.gewara.service.ticket.SuccessOrderService;
import com.gewara.service.ticket.TicketOrderService;
import com.gewara.service.ticket.TicketProcessService;
import com.gewara.support.ErrorCode;
import com.gewara.util.DateUtil;
import com.gewara.util.JsonUtils;
import com.gewara.util.StringUtil;
import com.gewara.util.VmUtils;

@Service("successOrderService")
public class SuccessOrderServiceImpl extends BaseServiceImpl implements SuccessOrderService{
	@Autowired@Qualifier("memberService")
	private MemberService memberService;
	public void setMemberService(MemberService memberService) {
		this.memberService = memberService;
	}
	@Autowired@Qualifier("elecCardService")
	private ElecCardService elecCardService;
	@Autowired@Qualifier("ticketOrderService")
	private TicketOrderService ticketOrderService;
	public void setTicketOrderService(TicketOrderService ticketOrderService) {
		this.ticketOrderService = ticketOrderService;
	}	
	@Autowired@Qualifier("goodsOrderService")
	private GoodsOrderService goodsOrderService;
	public void setGoodsOrderService(GoodsOrderService goodsOrderService) {
		this.goodsOrderService = goodsOrderService;
	}
	@Autowired@Qualifier("ticketProcessService")
	private TicketProcessService ticketProcessService;
	public void setTicketProcessService(TicketProcessService ticketProcessService) {
		this.ticketProcessService = ticketProcessService;
	}
	@Autowired@Qualifier("messageService")
	private MessageService messageService;
	public void setMessageService(MessageService messageService) {
		this.messageService = messageService;
	}
	@Autowired@Qualifier("pointService")
	private PointService pointService;
	public void setPointService(PointService pointService) {
		this.pointService = pointService;
	}
	@Override
	public TicketOrderContainer processTicketOrderSuccess(TicketOrder order){
		TicketOrderContainer container = new TicketOrderContainer(order);
		OrderExtra extra = baseDao.getObject(OrderExtra.class, order.getId());
		if(!StringUtils.isBlank(extra.getProcessLevel()) && !StringUtils.equals(OrderExtra.LEVEL_INIT, extra.getProcessLevel())){
			//已经处理过
			return container;
		}
		//1、判断是否自动关联套餐
		Map<String, String> otherMap = VmUtils.readJsonToMap(order.getOtherinfo());
		String bindgoods = otherMap.get(PayConstant.KEY_BINDGOODS);
		String goodsgift = otherMap.get(PayConstant.KEY_GOODSGIFT);
		//GewaConfig smsConfig = baseDao.getObject(GewaConfig.class, ConfigConstant.CFG_TICKET_GOODS_MSG);
		CinemaProfile profile = baseDao.getObject(CinemaProfile.class, order.getCinemaid());
		if(StringUtils.isNotBlank(bindgoods) || StringUtils.isNotBlank(goodsgift)) {
			String randomNum = goodsOrderService.nextRandomNum(DateUtil.addDay(new Timestamp(System.currentTimeMillis()), 60), 8, "0");
			ErrorCode<GoodsOrder> gorder = ticketOrderService.addBindGoodsOrder(order, randomNum);
			if(gorder.isSuccess()){
				GoodsOrder goodsOrder = gorder.getRetval();
				container.setGoodsOrder(goodsOrder);
				/*if(!(smsConfig != null && StringUtils.isNotBlank(smsConfig.getContent())
						&& Arrays.asList(StringUtils.split(smsConfig.getContent(), ",")).contains(order.getCinemaid() + ""))){*/
				if(!(profile != null && profile.hasDefinePaper())){
					List<SMSRecord> smsList = messageService.addMessage(goodsOrder).getRetval();//只在notify中发短信
					if(!CollectionUtils.isEmpty(smsList)){
						container.setSmsList(smsList);
					}
				}
				try{
					ticketProcessService.updateBuytimes(order.getMemberid(), order.getMobile(), OrderResult.ORDERTYPE_GOODS, order.getAddtime());
				}catch (Exception e) {
					dbLogger.error(StringUtil.getExceptionTrace(e, 5));
				}
				try{
					BaseGoods goods = baseDao.getObject(BaseGoods.class, goodsOrder.getGoodsid());
					int sales = goods.getSales() + order.getQuantity();
					goods.setSales(sales);
					baseDao.saveObject(goods);
				}catch (Exception e) {
					dbLogger.error(StringUtil.getExceptionTrace(e, 5));
				}
			}
		}else {
			//2、系统生产购买套餐的订单
			try{
				ErrorCode<GoodsOrder> gorder = goodsOrderService.addGoodsOrderByBuyItem(order);
				if(gorder.isSuccess()){
					GoodsOrder goodsOrder = gorder.getRetval();
					container.setGoodsOrder(goodsOrder);
					/*if(!(smsConfig != null && StringUtils.isNotBlank(smsConfig.getContent())
							&& Arrays.asList(StringUtils.split(smsConfig.getContent(), ",")).contains(order.getCinemaid() + ""))){*/
					if(!(profile != null && profile.hasDefinePaper())){
						List<SMSRecord> smsList = messageService.addMessage(goodsOrder).getRetval();
						if(!CollectionUtils.isEmpty(smsList)){
							container.setSmsList(smsList);
						}
					}
					try{
						ticketProcessService.updateBuytimes(order.getMemberid(), order.getMobile(), OrderResult.ORDERTYPE_GOODS, order.getAddtime());
					}catch (Exception e) {
						dbLogger.error(StringUtil.getExceptionTrace(e, 5));
					}
					try{
						BaseGoods goods = baseDao.getObject(BaseGoods.class, goodsOrder.getGoodsid());
						int sales = goods.getSales() + order.getQuantity();
						goods.setSales(sales);
						baseDao.saveObject(goods);
					}catch (Exception e) {
						dbLogger.error(StringUtil.getExceptionTrace(e, 5));
					}
				}
			}catch(Exception e){
				dbLogger.error(StringUtil.getExceptionTrace(e, 5));
			}
		}
		//3)邀请好友第一次消费送积分 TODO:还需要？？
		pointService.addPointToInvite(order.getMemberid(), order.getDue());
		//4)完成新手任务
		memberService.saveNewTask(order.getMemberid(), MemberConstant.TASK_BUYED_TICKET);
		
		extra.setProcessLevel(OrderExtra.LEVEL_MAIN);
		baseDao.saveObject(extra);
		return container;
	}
	
	@Override
	public TicketOrderContainer updateTicketOrderStats(TicketOrder order){
		TicketOrderContainer container = new TicketOrderContainer(order);
		OrderExtra extra = baseDao.getObject(OrderExtra.class, order.getId());
		if(!StringUtils.equals(extra.getProcessLevel(), OrderExtra.LEVEL_MAIN)){
			return container;
		}
		//1、更新影片购票数
		Movie movie = baseDao.getObject(Movie.class, order.getMovieid());
		movie.addCollection();
		movie.addQuguo();
		movie.addXiangqu();
		double factor = 1 + new Double(RandomUtils.nextInt(220)/100.0d);
		int bought = new Double(factor * order.getQuantity()).intValue();
		movie.addBoughtcount(bought);
		movie.setClickedtimes(movie.getClickedtimes() + bought);
		baseDao.saveObject(movie);

		//2、更新场次数据
		OpenPlayItem opi = baseDao.getObjectByUkey(OpenPlayItem.class, "mpid", order.getMpid(), true);
		opi.setGsellnum(opi.getGsellnum() + order.getQuantity());
		baseDao.saveObject(opi);

		//3、更新购买次数
		ticketProcessService.updateBuytimes(order.getMemberid(), order.getMobile(), OrderResult.ORDERTYPE_TICKET, order.getAddtime());
		container.setOpi(opi);
		container.setMovie(movie);
		
		extra.setProcessLevel(OrderExtra.LEVEL_FINISH);
		baseDao.saveObject(extra);
		return container;
	}

	@Override
	public DramaOrderContainer processDramaOrderSuccess(DramaOrder order) {
		OrderExtra extra = baseDao.getObject(OrderExtra.class, order.getId());
		OpenDramaItem openDramaItem = baseDao.getObjectByUkey(OpenDramaItem.class, "dpid", order.getDpid(), true);
		DramaOrderContainer container = new DramaOrderContainer(order, openDramaItem);
		if(!StringUtils.isBlank(extra.getProcessLevel()) && !StringUtils.equals(OrderExtra.LEVEL_INIT, extra.getProcessLevel())){
			//已经处理过
			return container;
		}
		//更新购买次数
		ticketProcessService.updateBuytimes(order.getMemberid(), order.getMobile(), OrderResult.ORDERTYPE_DRAMA, order.getAddtime());
		Drama drama = baseDao.getObject(Drama.class, order.getDramaid());
		drama.addCollection();
		drama.addQuguo();
		drama.addXiangqu();
		double factor = 1 + new Double(RandomUtils.nextInt(220)/100.0d);
		drama.addBoughtcount(new Double(factor * order.getQuantity()).intValue());
		baseDao.saveObject(drama);
		Theatre theatre = baseDao.getObject(Theatre.class,order.getTheatreid());
		theatre.addBoughtcount(new Double(factor * order.getQuantity()).intValue());
		baseDao.saveObject(theatre);
		/*//更新用户，买票的剧院
		int  sellnum = 0; 
		if(openDramaItem.getGsellnum()!=null){
			sellnum = openDramaItem.getGsellnum();
		}
		openDramaItem.setGsellnum(sellnum+order.getQuantity());
		baseDao.saveObject(openDramaItem);
		int buycount = 0;
		int lockNum = 0, totalnum = 0;
		if(openDramaItem.isOpenseat()){
			//获取指定ROOM的座位数
			List<OpenTheatreSeat> openSeatList = openDramaService.getOpenTheatreSeatListByDpid(openDramaItem.getDpid(), null);
			totalnum = openSeatList.size();
			buycount = openDramaItem.getGsellnum();
			for(OpenTheatreSeat seat:openSeatList){
				if(seat.isLocked()){
					lockNum ++;
				}
			}
		}else if(openDramaItem.isOpenprice()){
			List<TheatreSeatPrice> tspList = dramaPlayItemService.getTspList(openDramaItem.getDpid(), null);
			TspHelper tspHelper = new TspHelper(tspList);
			List<TheatreSeatPrice> tspList1 = tspHelper.getTspListBySno();
			for(TheatreSeatPrice seatprice : tspList1){
				totalnum += seatprice.getQuantity();
				buycount += seatprice.getSales();
			}
		}
		
		int aSeat = totalnum - lockNum - buycount;//计算剩余座位数
		if(aSeat<=3){
			container.setMsg("场次 "+openDramaItem.getPlaytime()+" ["+openDramaItem.getDramaname()+"]话剧剩余座位不足");
		}*/
		extra.setProcessLevel(OrderExtra.LEVEL_FINISH);
		baseDao.saveObject(extra);
		return container;
	}
	@Override
	public SportOrderContainer processSportOrderSuccess(SportOrder order) {
		OrderExtra extra = baseDao.getObject(OrderExtra.class, order.getId());
		SportOrderContainer container = new SportOrderContainer(order);
		if(!StringUtils.isBlank(extra.getProcessLevel()) && !StringUtils.equals(OrderExtra.LEVEL_INIT, extra.getProcessLevel())){
			//已经处理过
			return container;
		}
		//更新用户，买票的运动场馆
		OpenTimeTable ott = baseDao.getObject(OpenTimeTable.class, order.getOttid());
		//更新购买次数
		ticketProcessService.updateBuytimes(order.getMemberid(), order.getMobile(), OrderResult.ORDERTYPE_SPORT, order.getAddtime());
		if(ott.hasPeriod()||ott.hasInning()){
			SellTimeTable sellTimeTable = baseDao.getObject(SellTimeTable.class, order.getId());
			if(sellTimeTable != null && sellTimeTable.getOtiid()!= null){
				OpenTimeItem oti = baseDao.getObject(OpenTimeItem.class, sellTimeTable.getOtiid());
				oti.setSales(oti.getSales() + order.getQuantity());
				baseDao.saveObject(oti);
			}
		}
		
		container.setOtt(ott);
		extra.setProcessLevel(OrderExtra.LEVEL_FINISH);
		baseDao.saveObject(extra);
		return container;
	}
	@Override
	public GoodsOrderContainer processGoodsOrderSuccess(GoodsOrder order) {
		OrderExtra extra = baseDao.getObject(OrderExtra.class, order.getId());
		GoodsOrderContainer container = new GoodsOrderContainer(order);
		if(!StringUtils.isBlank(extra.getProcessLevel()) && !StringUtils.equals(OrderExtra.LEVEL_INIT, extra.getProcessLevel())){
			//已经处理过
			return container;
		}
		//处理逻辑
		ticketProcessService.updateBuytimes(order.getMemberid(), order.getMobile(), OrderResult.ORDERTYPE_GOODS, order.getAddtime());
		BaseGoods goods = baseDao.getObject(BaseGoods.class, order.getGoodsid());
		int sales = goods.getSales() + order.getQuantity();
		goods.setSales(sales);
		baseDao.saveObject(goods);
		if(goods instanceof TicketGoods){
			TicketGoods ticketGoods = (TicketGoods)goods;
			//更新购票数
			if(StringUtils.equals(TagConstant.TAG_DRAMA, ticketGoods.getCategory())){
				Drama drama = baseDao.getObject(Drama.class, ticketGoods.getItemid());
				drama.addCollection();
				drama.addQuguo();
				drama.addXiangqu();
				double factor = 1 + new Double(RandomUtils.nextInt(220)/100.0d);
				drama.addBoughtcount(new Double(factor * order.getQuantity()).intValue());
				baseDao.saveObject(drama);
				Theatre theatre = baseDao.getObject(Theatre.class,ticketGoods.getRelatedid());
				if(theatre!=null){
					theatre.addBoughtcount(new Double(factor * order.getQuantity()).intValue());
					baseDao.saveObject(theatre);
				}
			}
		}
		Map<String,String> otherMap = VmUtils.readJsonToMap(order.getOtherinfo());
		String cardNo = otherMap.get(OrderConstant.OTHERKEY_DELAY_CARDNO);
		if(StringUtils.isNotBlank(cardNo)){//处理电子票有偿延期
			saveDelayElecCard(order.getMemberid(),cardNo);
		}
		extra.setProcessLevel(OrderExtra.LEVEL_FINISH);
		baseDao.saveObject(extra);
		return container;
	}
	
	private void saveDelayElecCard(Long memberId,String cardNo){
		ElecCard card = elecCardService.getMemberElecCardByNo(memberId, cardNo);
		ElecCardBatch ebatch = card.getEbatch();
		card.setEndtime(DateUtil.addDay(card.getTimeto(), ebatch.getDelayUseDays()));
		baseDao.updateObject(card);
		JsonData jd = baseDao.getObject(JsonData.class, JsonDataKey.KEY_ELECCARD_DELAY);
		if(jd == null){
			jd = new JsonData(JsonDataKey.KEY_ELECCARD_DELAY);
		}
		Map<String,String> delayCardMap = VmUtils.readJsonToMap(jd.getData());
		String cards = delayCardMap.get(card.getEbatch().getId().toString());
		if(StringUtils.isBlank(cards)){
			cards = card.getCardno();
		}else{
			cards = cards + "," + card.getCardno();
		}
		delayCardMap.put(card.getEbatch().getId() + "", cards);
		jd.setData(JsonUtils.writeMapToJson(delayCardMap));
		baseDao.saveObject(jd);
	}
}
