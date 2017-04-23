package com.gewara.jms;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.camel.Header;
import org.apache.camel.language.Simple;
import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.type.TypeReference;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.orm.hibernate3.HibernateOptimisticLockingFailureException;

import com.gewara.Config;
import com.gewara.constant.PayConstant;
import com.gewara.constant.TagConstant;
import com.gewara.constant.sys.LogTypeConstant;
import com.gewara.constant.ticket.OrderConstant;
import com.gewara.constant.ticket.PartnerConstant;
import com.gewara.helper.order.DramaOrderContainer;
import com.gewara.helper.order.SportOrderContainer;
import com.gewara.helper.order.TicketOrderContainer;
import com.gewara.helper.ticket.UpdateMpiContainer;
import com.gewara.json.MemberStats;
import com.gewara.model.api.OrderResult;
import com.gewara.model.bbs.LinkShare;
import com.gewara.model.drama.DramaOrder;
import com.gewara.model.drama.OpenDramaItem;
import com.gewara.model.movie.Cinema;
import com.gewara.model.movie.Movie;
import com.gewara.model.pay.BuyItem;
import com.gewara.model.pay.GewaOrder;
import com.gewara.model.pay.GoodsOrder;
import com.gewara.model.pay.GymOrder;
import com.gewara.model.pay.OrderRefund;
import com.gewara.model.pay.SMSRecord;
import com.gewara.model.pay.SettleOrder;
import com.gewara.model.pay.SportOrder;
import com.gewara.model.pay.TicketOrder;
import com.gewara.model.ticket.OpenPlayItem;
import com.gewara.pay.PayUtil;
import com.gewara.service.DaoService;
import com.gewara.service.bbs.AgendaService;
import com.gewara.service.bbs.UserMessageService;
import com.gewara.service.drama.DramaOrderService;
import com.gewara.service.member.TreasureService;
import com.gewara.service.partner.PartnerSynchService;
import com.gewara.service.ticket.SuccessOrderService;
import com.gewara.service.ticket.TicketProcessService;
import com.gewara.service.ticket.TicketSynchService;
import com.gewara.support.ErrorCode;
import com.gewara.untrans.CommentService;
import com.gewara.untrans.MailService;
import com.gewara.untrans.MemberCountService;
import com.gewara.untrans.PageCacheService;
import com.gewara.untrans.ShareService;
import com.gewara.untrans.UntransService;
import com.gewara.untrans.drama.OdiOpenService;
import com.gewara.untrans.mobile.PushService;
import com.gewara.untrans.order.BroadcastOrderService;
import com.gewara.untrans.order.RefundOperationService;
import com.gewara.untrans.spider.SpiderOperationService;
import com.gewara.untrans.ticket.MpiOpenService;
import com.gewara.untrans.ticket.OrderProcessService;
import com.gewara.untrans.ticket.TicketOperationService;
import com.gewara.untrans.ticket.TicketRollCallService;
import com.gewara.util.DateUtil;
import com.gewara.util.GewaLogger;
import com.gewara.util.JsonUtils;
import com.gewara.util.LoggerUtils;
import com.gewara.util.StringUtil;
import com.gewara.util.VmUtils;
import com.gewara.web.support.DynamicStats.LogCounter;
import com.gewara.web.support.ResourceStatsUtil;

public class JmsMsgConsumer implements InitializingBean{
	private final transient GewaLogger dbLogger = LoggerUtils.getLogger(getClass(), Config.getServerIp(), Config.SYSTEMID);
	@Autowired@Qualifier("mpiOpenService")
	private MpiOpenService mpiOpenService;
	
	@Autowired@Qualifier("memberCountService")
	private MemberCountService memberCountService;
	@Autowired@Qualifier("mailService")
	private MailService mailService;
	
	public void setMailService(MailService mailService) {
		this.mailService = mailService;
	}
	@Autowired@Qualifier("ticketOperationService")
	private TicketOperationService ticketOperationService;
	public void setTicketOperationService(TicketOperationService ticketOperationService) {
		this.ticketOperationService = ticketOperationService;
	}
	@Autowired@Qualifier("partnerSynchService")
	private PartnerSynchService partnerSynchService;
	public void setPartnerSynchService(PartnerSynchService partnerSynchService) {
		this.partnerSynchService = partnerSynchService;
	}
	@Autowired@Qualifier("orderProcessService")
	private OrderProcessService orderProcessService;
	public void setProcessOrderService(OrderProcessService orderProcessService) {
		this.orderProcessService = orderProcessService;
	}
	@Autowired@Qualifier("untransService")
	private UntransService untransService;
	public void setUntransService(UntransService untransService) {
		this.untransService = untransService;
	}
	@Autowired@Qualifier("pageCacheService")
	private PageCacheService pageCacheService;
	public void setPageCacheService(PageCacheService pageCacheService) {
		this.pageCacheService = pageCacheService;
	}
	@Autowired@Qualifier("daoService")
	private DaoService daoService;
	public void setDaoService(DaoService daoService) {
		this.daoService = daoService;
	}
	@Autowired@Qualifier("agendaService")
	private AgendaService agendaService;
	public void setAgendaService(AgendaService agendaService) {
		this.agendaService = agendaService;
	}
	@Autowired@Qualifier("shareService")
	private ShareService shareService;
	public void setShareService(ShareService shareService) {
		this.shareService = shareService;
	}
	@Autowired@Qualifier("ticketRollCallService")
	private TicketRollCallService ticketRollCallService;
	public void setTicketRollCallService(TicketRollCallService ticketRollCallService) {
		this.ticketRollCallService = ticketRollCallService;
	}
	@Autowired@Qualifier("userMessageService")
	private UserMessageService userMessageService;
	public void setUserMessageService(UserMessageService userMessageService) {
		this.userMessageService = userMessageService;
	}
	
	@Autowired@Qualifier("pushService")
	private PushService pushService;
	public void setPushService(PushService pushService) {
		this.pushService = pushService;
	}
	@Autowired@Qualifier("broadcastOrderService")
	private BroadcastOrderService broadcastOrderService;
	@Autowired@Qualifier("odiOpenService")
	private OdiOpenService odiOpenService;
	
	@Autowired@Qualifier("dramaOrderService")
	private DramaOrderService dramaOrderService;
	
	@Autowired@Qualifier("ticketSynchService")
	private TicketSynchService ticketSynchService;
	public void setTicketSynchService(TicketSynchService ticketSynchService){
		this.ticketSynchService = ticketSynchService;
	}
	@Autowired@Qualifier("ticketProcessService")
	private TicketProcessService ticketProcessService;
	public void setTicketProcessService(TicketProcessService ticketProcessService) {
		this.ticketProcessService = ticketProcessService;
	}
	@Autowired@Qualifier("successOrderService")
	private SuccessOrderService successOrderService;
	public void setSuccessOrderService(SuccessOrderService successOrderService) {
		this.successOrderService = successOrderService;
	}
	
	@Autowired@Qualifier("spiderOperationService")
	private SpiderOperationService spiderOperationService;
	public void setSpiderOperationService(SpiderOperationService spiderOperationService) {
		this.spiderOperationService = spiderOperationService;
	}
	
	@Autowired@Qualifier("refundOperationService")
	private RefundOperationService refundOperationService;
	
	@Autowired@Qualifier("treasureService")
	private TreasureService treasureService;
	
	@Autowired@Qualifier("commentService")
	private CommentService commentService;
	

	public boolean isOrderMsg(@Header("msgtag")String msgtag){
		return "order".equals(msgtag);
	}
	public void process(@Header("msgtag")String msgtag){
		dbLogger.warn("MessageTag:" + msgtag);
	}
	/**
	 * 订单成功时触发此事件（paid_failure*---->paid_success）
	 * @param tradeNo
	 */
	public void addOrder(@Simple("${body[tradeNo]}") String tradeNo) {
		
		for(int i=0; i < 3;i++){
			long time = System.currentTimeMillis();
			LogCounter lc = ResourceStatsUtil.getCallStats().beforeProcess(STATS_ALTERORDERSUCCESS, time);
			try{
				alterOrderSuccess(tradeNo);
				break;
			}catch(Throwable e){
				//非乐观锁异常
				if(!isUpdateErrorException(e)){
					dbLogger.warn("", e);
					break;
				}else{
				}
			}finally{
				ResourceStatsUtil.getCallStats().afterProcess(lc, time);
			}
		}
	}
	protected boolean isUpdateErrorException(Throwable e){
		if(e instanceof HibernateOptimisticLockingFailureException){
			HibernateOptimisticLockingFailureException exc = (HibernateOptimisticLockingFailureException)e;
			dbLogger.warn(exc.getPersistentClassName() + ":" + exc.getIdentifier());
			return true;
		}
		return false;
	}

	private void alterOrderSuccess(String tradeNo){
		GewaOrder gewaOrder = daoService.getObjectByUkey(GewaOrder.class, "tradeNo", tradeNo, false);
		if(gewaOrder!=null && !gewaOrder.isPaidSuccess()){
			dbLogger.warn("订单状不正确！");
			return;
		}
		if(PayUtil.isTicketTrade(tradeNo)){
			//1、基础
			TicketOrderContainer container = successOrderService.processTicketOrderSuccess((TicketOrder) gewaOrder);
			broadcastOrderService.broadcastOrder(container.getTicketOrder());
			if(container.getGoodsOrder()!=null){
				broadcastOrderService.broadcastOrder(container.getGoodsOrder());
			}
			if(container.getSmsList()!=null){
				for (SMSRecord sms : container.getSmsList()) {
					untransService.sendMsgAtServer(sms, false);
				}
			}
			//2、更新统计
			container = successOrderService.updateTicketOrderStats((TicketOrder) gewaOrder);
			//3、更新统计
			updateTicketMemberStats(container);
		}else if(PayUtil.isDramaOrder(tradeNo)){
			DramaOrderContainer container = successOrderService.processDramaOrderSuccess((DramaOrder) gewaOrder);
			if(StringUtils.isNotBlank(container.getMsg())){
				String mailaddress = "hongyu.ji@gewara.com,cradle.yao@gewara.com,sj.xing@gewara.com,sandy.chen@gewara.com";
				mailService.sendEmail("www.gewara.com", container.getMsg(), "[" + container.getMsg() + "]", mailaddress);
			}
			try {
				DramaOrder order = container.getDramaOrder();
				if(!order.sureOutPartner()) {
					Timestamp playtime = container.getItem().getPlaytime();
					untransService.saveSeeCount(order, playtime);
				}
				broadcastOrderService.broadcastOrder(order);
			} catch (Exception e) {
				dbLogger.error(StringUtil.getExceptionTrace(e, 5));
			}
			try{
				updateDramaStats(container);
			}catch(Exception e){
				dbLogger.error(StringUtil.getExceptionTrace(e, 5));
			}
		}else if(PayUtil.isSportTrade(tradeNo)){
			SportOrder order = (SportOrder) gewaOrder;
			SportOrderContainer container = successOrderService.processSportOrderSuccess(order);
			try {
				if(!order.sureOutPartner()) {
					Timestamp playtime = new Timestamp(container.getOtt().getPlaydate().getTime());
					untransService.saveSeeCount(order, playtime);
				}
			} catch (Exception e) {
				dbLogger.error(StringUtil.getExceptionTrace(e, 5));
			}
			broadcastOrderService.broadcastOrder(container.getSportOrder());
			//添加生活
			try {
				agendaService.addOrderAgenda(order, container.getOtt());
			} catch (Exception e) {
				dbLogger.error(StringUtil.getExceptionTrace(e, 5));
			}
			
		}else if(PayUtil.isGymTrade(tradeNo)){
			GymOrder order = (GymOrder)gewaOrder;
			//更新买次数
			ticketProcessService.updateBuytimes(order.getMemberid(), order.getMobile(), OrderResult.ORDERTYPE_GYM, order.getAddtime());
		}else if(PayUtil.isGoodsTrade(tradeNo)){
			GoodsOrder order = (GoodsOrder)gewaOrder;
			successOrderService.processGoodsOrderSuccess(order);
			broadcastOrderService.broadcastOrder(order);
		}
		//处理订单结算
		try {
			String credentialsId = JsonUtils.getJsonValueByKey(gewaOrder.getOtherinfo(), OrderConstant.OTHERKEY_CREDENTIALSID);
			if (StringUtils.isNotBlank(credentialsId)) {
				SettleOrder settleOrder = daoService.getObject(SettleOrder.class, gewaOrder.getId());
				if (settleOrder == null) {
					settleOrder = new SettleOrder(gewaOrder.getId(), gewaOrder.getPaidtime(), new Long(credentialsId));
					daoService.saveObject(settleOrder);
				}
			}
		} catch (Exception e) {
			dbLogger.warn("settloOrder tradeNo:" + gewaOrder.getTradeNo());
		}
	}
	private void updateDramaStats(DramaOrderContainer container){
		DramaOrder order = container.getDramaOrder();
		OpenDramaItem item = container.getItem();
		try{
			if(item.isOpenseat()){
				odiOpenService.asynchUpdateAreaStats(item);
			}else{
				List<BuyItem> buyList = daoService.getObjectListByField(BuyItem.class, "orderid", order.getId());
				List<OpenDramaItem> itemList = dramaOrderService.getOpenDramaItemList(item, buyList);
				for (OpenDramaItem odi : itemList) {
					odiOpenService.asynchUpdateAreaStats(odi);
				}
			}
		}catch (Exception e) {
			dbLogger.error(StringUtil.getExceptionTrace(e, 5));
		}
	}
	
	private void updateTicketMemberStats(TicketOrderContainer container){
		TicketOrder order = container.getTicketOrder();
		OpenPlayItem opi = container.getOpi();
		if(opi==null){
			opi = daoService.getObjectByUkey(OpenPlayItem.class, "mpid", order.getMpid(), true);
		}
		Movie movie = container.getMovie();
		if(movie==null){
			movie = daoService.getObject(Movie.class, order.getMovieid());
		}
		
		mpiOpenService.asynchUpdateOpiStats(container.getOpi(), false);

		//1、增加生活
		if(!order.surePartner()){
			try{
				agendaService.addOrderAgenda(order, opi);
			}catch(Exception e){
				dbLogger.error(StringUtil.getExceptionTrace(e, 5));
			}
		}
		//2、发送站内信
		try {
			userMessageService.addMsgAction(order, opi);
		} catch (Exception e) {
			dbLogger.error(StringUtil.getExceptionTrace(e, 5));
		}
		
		try {
			Long rollMemberid = order.getMemberid();
			if(!order.sureOutPartner()) {
				//3、 更新用户，买票的影院
				memberCountService.saveMemberCount(order.getMemberid(), MemberStats.FIELD_LASTCINEMAID, order.getCinemaid()+"", false);
				memberCountService.saveMemberCount(order.getMemberid(), MemberStats.FIELD_LASTMOVIEID, order.getMovieid()+"", false);
				memberCountService.saveMobileLastTicket(order.getMobile(), order.getTradeNo(),order.getOrdertype(),DateUtil.getCurFullTimestampStr());
				memberCountService.saveMbrFirstTicket(order.getMemberid(), order.getTradeNo(), order.getOrdertype());
				untransService.saveSeeCount(order, opi.getPlaytime());
				treasureService.saveTreasure(order.getMemberid(), "cinema", order.getCinemaid(), "collect");
			}else{
				rollMemberid = null;
			}
			ticketRollCallService.saveOrUpdateTicketRollCall(rollMemberid, order.getMobile(), TagConstant.TAG_CINEMA, order.getCinemaid(), order.getQuantity(), 30);
		} catch (Exception e) {
			dbLogger.error(StringUtil.getExceptionTrace(e, 5));
		}
		//4、手机客户端提醒
		try {
			//判断是手机客户端的订单
			if(order.getPartnerid().equals(PartnerConstant.IPHONE) || order.getPartnerid().equals(PartnerConstant.ANDROID)){
				Cinema cinema = daoService.getObject(Cinema.class, order.getCinemaid());
				//取票提醒PUSH
				pushService.saveTakeTicketAutoPush(opi.getPlaytime(), opi.getCinemaname(),cinema.getAddress(), opi.getMoviename(), order.getMemberid(),order);
				//观影提醒PUSH
				pushService.saveFilmwatchRemindAutoPush(opi.getPlaytime(), opi.getCinemaname(), cinema.getAddress(), opi.getMoviename(),order.getMovieid(), order.getMemberid(), order.getTradeNo());
				//观影后发表哇啦PUSH
				pushService.saveSendWalaAutoPush(DateUtil.addMinute(opi.getPlaytime(), movie.getVideolen()==null?200: movie.getVideolen()), opi.getMoviename(), order.getMemberid(),movie.getId(), order.getTradeNo());
			}
		} catch (Exception e) {
			dbLogger.error("发送Push消息错误：" + order.getTradeNo(), e);
		}
	}
	public void updatePageCache(@Simple("${body[pageUrl]}") String pageUrl, 
			@Simple("${body[reqParams]}") String reqParams,
			@Simple("${body[reqCookie]}") String reqCookie,
			@Simple("${body[citycode]}") String citycode){
		Map<String, String> params = VmUtils.readJsonToMap(reqParams);
		Map<String, String[]> cookieMap = null;
		if(StringUtils.isNotBlank(reqCookie)){
			cookieMap = JsonUtils.readJsonToObject(new TypeReference<Map<String, String[]>>(){}, reqCookie);
		}
		pageCacheService.processPageView(pageUrl, params, cookieMap, citycode);
	}
	public void share2Out(@Simple("${body[tag]}") String tag, 
			@Simple("${body[tagid]}") Long tagid,
			@Simple("${body[memberid]}") Long memberid, 
			@Simple("${body[category]}") String category,
			@Simple("${body[content]}") String content,
			@Simple("${body[picUrl]}") String picUrl,
			@Simple("${body[type]}") String type){
		if(StringUtils.isBlank(content)){
			LinkShare ls= shareService.addShareInfo(tag, tagid, memberid, category,type);
			if(ls != null) shareService.sendMicroInfo(ls);
		}else{
			LinkShare ls= shareService.addShareInfo(tag, tagid, memberid, type, content, picUrl);
			if(ls != null) shareService.sendCustomInfo(ls);
		}
	}
	
	/**
	 * 处理刚付款完成时触发（new_*---->paid_failure）
	 * @param tradeNo
	 * @param from
	 */
	public void processOrder(@Simple("${body[tradeNo]}") String tradeNo, 
			@Simple("${body[from]}") String from){
		long time = System.currentTimeMillis();
		LogCounter lc = ResourceStatsUtil.getCallStats().beforeProcess(STATS_PROCESSORDER, time);
		try{
			partnerSynchService.pushCallbackOrder(tradeNo, PayConstant.PUSH_FLAG_PAID);
			GewaOrder order = daoService.getObjectByUkey(GewaOrder.class, "tradeNo", tradeNo, false);
			ErrorCode result = orderProcessService.processOrder(order, from, "0");
			dbLogger.warn(tradeNo + ":" + result.getMsg());
		}finally{
			ResourceStatsUtil.getCallStats().afterProcess(lc, time);
		}
	}
	
	/**
	 * 满天星远程订单确认成功时触发
	 * @param orderid
	 */
	public void correctOrder(@Simple("${body[orderid]}") Long orderid){
		try{
			TicketOrder order = daoService.getObject(TicketOrder.class, orderid);
			if(!order.isPaidSuccess()){
				ErrorCode<String> result = orderProcessService.reconfirmOrder(order, null, true, false);
				if(result.isSuccess()){
					String msg = "满天星“待处理”订单：" + order.getTradeNo() + "成功！";
					dbLogger.warnWithType(LogTypeConstant.LOG_TYPE_JOB, msg);
				}else{
					String msg = "满天星“待处理”订单失败：" + order.getTradeNo() + result.getMsg();
					dbLogger.warnWithType(LogTypeConstant.LOG_TYPE_JOB, msg);
				}
			}
		}catch(Exception e){
			dbLogger.warnWithType(LogTypeConstant.LOG_TYPE_JOB, StringUtil.getExceptionTrace(e));
		}
	}
	/**
	 * 充值成功时触发
	 * @param tradeNo
	 * @param from
	 */
	public void processCharge(@Simple("${body[tradeNo]}") String tradeNo, @Simple("${body[from]}") String from){
		dbLogger.warn("充值：" + tradeNo + from);
	}
	
	/**
	 * ticket系统同步到最新排片数据时触发
	 * @param cinemaid
	 */
	public void updateMoviePlayItem(@Simple("${body[cinemaid]}")Long cinemaid){
		long time = System.currentTimeMillis();
		LogCounter lc = ResourceStatsUtil.getCallStats().beforeProcess(STATS_UPDATEMOVIEPLAYITEM, time); 
		try{
			List<String> msgList = new ArrayList<String>();
			UpdateMpiContainer container = new UpdateMpiContainer();
			ticketOperationService.updateMoviePlayItem(container , cinemaid, msgList, 0);
			ticketSynchService.updateOpenPlayItem(cinemaid, msgList);
			mpiOpenService.asynchAutoOpenMpiList(container.getInsertList());
		}catch (Exception e) {
			dbLogger.warn("下载更新订票系统的排片出错," + StringUtil.getExceptionTrace(e));
		}finally{
			ResourceStatsUtil.getCallStats().afterProcess(lc, time);
		}
	}

	public void testQueue(@Simple("${body}")String body){
		dbLogger.warn(body);
	}

	/**
	 * spider系统同步到最新排片数据时触发
	 * 
	 * @param cinemaid
	 *
	 * @author leo.li
	 * Modify Time Jun 24, 2013 11:51:25 AM
	 */
	public void updateMoviePlayItemFromSpider(@Simple("${body[cinemaid]}")Long cinemaid){
		try{
			List<String> msgList = new ArrayList<String>();
			UpdateMpiContainer container = new UpdateMpiContainer();
			spiderOperationService.updateMoviePlayItem(container , cinemaid, msgList, 0);
			//ticketSynchService.updateOpenPlayItem(msgList);
			//mpiOpenService.asynchAutoOpenMpiList(container.getInsertList());
		}catch (Exception e) {
			dbLogger.warn("下载更新订票系统的排片出错," + StringUtil.getExceptionTrace(e));
		}
	}
	
	public void backGpticketRemoteOrder(@Simple("${body[orderid]}")Long orderid){
		try{
			DramaOrder dramaOrder = daoService.getObject(DramaOrder.class, orderid);
			OrderRefund refund = daoService.getObjectByUkey(OrderRefund.class, "tradeno", dramaOrder.getTradeNo());
			ErrorCode code = refundOperationService.confirmRefund(refund, dramaOrder, 0L, "系统自动");
			if(!code.isSuccess()){
				dbLogger.warn(code.getMsg());
			}
		}catch(Exception e){
			dbLogger.warn("", e);
		}
	}
	private static final String STATS_PROCESSORDER = "JmsMsgConsumer.processOrder";
	private static final String STATS_UPDATEMOVIEPLAYITEM = "JmsMsgConsumer.updateMoviePlayItem";
	private static final String STATS_ALTERORDERSUCCESS = "JmsMsgConsumer.alterOrderSuccess";

	@Override
	public void afterPropertiesSet() throws Exception {
		ResourceStatsUtil.registerCall(STATS_PROCESSORDER);
		ResourceStatsUtil.registerCall(STATS_UPDATEMOVIEPLAYITEM);
		ResourceStatsUtil.registerCall(STATS_ALTERORDERSUCCESS);
	}
	
	public void handleSmsMO(@Simple("${body[mobile]}") String mobile, @Simple("${body[content]}") String content){
		try{
			dbLogger.warn("handleSmsMO the mobile is " + mobile + ",content is " + content);//先打印几天，稳定后删除 TODO
			if(StringUtils.isBlank(mobile) || StringUtils.isBlank(content)){
				return;
			}
			String prefix = StringUtils.substring(content, 0, 2);
			if(StringUtils.equals(prefix, "G#")){
				System.out.println("G#");
			}else if(StringUtils.equals(prefix, "W#")){
				System.out.println("W#");
			}else if(StringUtils.equals(prefix, "R#")){
				System.out.println("R#");
			}else{
				commentService.addReplyToComment(mobile, content, null);
			}
			
		}catch(Exception e){
			dbLogger.warn("", e);
		}
	}
}
