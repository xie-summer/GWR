package com.gewara.web.action.api2machine;

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

import com.gewara.constant.AdminCityContant;
import com.gewara.constant.ApiConstant;
import com.gewara.constant.GoodsConstant;
import com.gewara.constant.SmsConstant;
import com.gewara.constant.Status;
import com.gewara.constant.content.FilmFestConstant;
import com.gewara.constant.content.SignName;
import com.gewara.constant.sys.ConfigConstant;
import com.gewara.constant.sys.MongoData;
import com.gewara.constant.ticket.OpiConstant;
import com.gewara.constant.ticket.OrderConstant;
import com.gewara.constant.ticket.PartnerConstant;
import com.gewara.helper.order.GewaOrderHelper;
import com.gewara.model.api.ApiUser;
import com.gewara.model.api.OrderResult;
import com.gewara.model.api.Synch;
import com.gewara.model.common.GewaConfig;
import com.gewara.model.content.GewaCommend;
import com.gewara.model.goods.Goods;
import com.gewara.model.movie.Cinema;
import com.gewara.model.movie.CinemaProfile;
import com.gewara.model.movie.Movie;
import com.gewara.model.pay.GewaOrder;
import com.gewara.model.pay.GoodsOrder;
import com.gewara.model.pay.SMSRecord;
import com.gewara.model.pay.TicketOrder;
import com.gewara.model.ticket.OpenPlayItem;
import com.gewara.model.ticket.SellSeat;
import com.gewara.pay.PayUtil;
import com.gewara.service.OperationService;
import com.gewara.service.SynchService;
import com.gewara.support.ErrorCode;
import com.gewara.untrans.CommonService;
import com.gewara.util.BeanUtil;
import com.gewara.util.DateUtil;
import com.gewara.util.JsonUtils;
import com.gewara.util.StringUtil;
import com.gewara.util.VmUtils;
import com.gewara.util.WebUtils;
import com.gewara.web.action.api.ApiAuth;
import com.gewara.web.filter.NewApiAuthenticationFilter;

@Controller
public class MachineCinemaController extends BaseMachineApiController{
	@Autowired@Qualifier("synchService")
	private SynchService synchService;
	@Autowired@Qualifier("commonService")
	private CommonService commonService;
	private boolean checkSynchkey(Synch synch, String synchsign){
		return StringUtil.md5(synch.getCinemaid() + synch.getSynchkey() + DateUtil.formatDate(new Date())).equalsIgnoreCase(synchsign);
	}
	
	
	/**
	 * 根据影院、手机后4位、取票密码查询订单信息
	 * 当一体机查询不到订单的时候，调用该接口
	 * @param cinemaid
	 * @param checkpass
	 * @param shortmobile
	 * @param model
	 * @return
	 */
	@RequestMapping("/apimac/cinema/qryOrderList.xhtml")
	public String qryOrderList(Long cinemaid, String checkpass, String shortmobile, ModelMap model){
		if(cinemaid==null || StringUtils.isBlank(checkpass) || StringUtils.isBlank(shortmobile)) {
			return getErrorXmlView(model, ApiConstant.CODE_DATA_ERROR, "缺少参数");
		}
		Cinema cinema = daoService.getObject(Cinema.class, cinemaid);
		if(cinema == null) return getErrorXmlView(model, ApiConstant.CODE_DATA_ERROR, "没有该影院！");
		ApiAuth auth = NewApiAuthenticationFilter.getApiAuth();
		ApiUser apiUser = auth.getApiUser();
		List<TicketOrder> orderList = new ArrayList<TicketOrder>();
		List<GoodsOrder> goodsOrderList = new ArrayList<GoodsOrder>();
		Timestamp addtime = DateUtil.addDay(DateUtil.getMillTimestamp(), -7);
		List<GewaOrder> torderList = getGewaOrderByCheckpass(cinemaid, addtime, checkpass, shortmobile);
		for(GewaOrder order : torderList){
			if(order instanceof TicketOrder) {
				orderList.add((TicketOrder)order);
				String gorderno = PayUtil.FLAG_GOODS + order.getTradeNo().substring(1);
				GoodsOrder gorder = daoService.getObjectByUkey(GoodsOrder.class, "tradeNo", gorderno);
				if(gorder!=null){
					goodsOrderList.add(gorder);
				}
			}else if(order instanceof GoodsOrder){
				goodsOrderList.add((GoodsOrder)order);
			}
		}
		addCommonMap(apiUser, cinema, orderList, goodsOrderList, model);
		model.put("nowtime", DateUtil.getMillTimestamp());
		return getXmlView(model, "api2machine/downCinemaOrderList.vm");
	}
	/**
	 * 下载影院订单
	 * @param cinemaid
	 * @param ticketnum
	 * @param lastTime
	 * @param pageSize
	 * @param request
	 * @param model
	 * @return
	 */
	@RequestMapping("/apimac/cinema/downOrder.xhtml")
	public String downOrder2(Long cinemaid, String ordertype, String synchsign, String ticketnum, 
			Timestamp lastTime,Integer pageSize, HttpServletRequest request, ModelMap model){
		if(lastTime!=null){
			if(lastTime.before(DateUtil.addDay(new Timestamp(System.currentTimeMillis()), -8))){
				return getErrorXmlView(model, ApiConstant.CODE_PARAM_ERROR, "时间参数有错误！"); 
			}
		}
		ApiAuth auth = NewApiAuthenticationFilter.getApiAuth();
		ApiUser apiUser = auth.getApiUser();
		
		if(cinemaid==null) return getErrorXmlView(model, ApiConstant.CODE_PARAM_ERROR, "传递参数错误！");
		Synch synch = daoService.getObject(Synch.class, cinemaid);
		if(synch == null) {
			synch = new Synch(cinemaid,Synch.TGA_CINEMA);
		}else {
			boolean checksynchkey = checkSynchkey(synch, synchsign);
			if(!checksynchkey) return getErrorXmlView(model, ApiConstant.CODE_DATA_ERROR, "校验cinemakey错误！");
		}
		String ip = WebUtils.getRemoteIp(request);
		return this.downTicketOrder(apiUser, cinemaid, ordertype, ticketnum,lastTime,pageSize,ip, synch, model);
	}
	private String downTicketOrder(ApiUser apiUser, Long cinemaid, String ordertype, String ticketnum, Timestamp lastTime, Integer pageSize, String ip, Synch synch, ModelMap model){
		Cinema cinema = daoService.getObject(Cinema.class, cinemaid);
		if(cinema == null) return getErrorXmlView(model, ApiConstant.CODE_DATA_ERROR, "没有该影院！");
		Timestamp cur = new Timestamp(System.currentTimeMillis());
		apiService.saveSynchWithCinema(synch, cur, null, ticketnum, ip);
		model.put("nowtime", cur);
		List<TicketOrder> orderList = new ArrayList<TicketOrder>();
		boolean downticket = StringUtils.isBlank(ordertype) || StringUtils.equals(ordertype, OrderConstant.ORDER_TYPE_TICKET);
		boolean downgoods = StringUtils.isBlank(ordertype) || StringUtils.equals(ordertype, OrderConstant.ORDER_TYPE_GOODS);
		if(downticket){
			if(pageSize == null && lastTime != null){
				orderList = synchService.getOrderListByCinemaIdAndLasttime(cinemaid, DateUtil.addMinute(lastTime, -5));
				if(!orderList.isEmpty()) cur = orderList.get(orderList.size() -1).getModifytime();
				model.put("nowtime", cur);
			}else if(pageSize == null || lastTime == null){
				orderList = synchService.getOrderListByCinemaIdAndLasttime(cinemaid, DateUtil.addMinute(synch.getSuccesstime(), -5));
			}else{
				orderList = synchService.getOrderListByCinemaIdAndLasttime(cinemaid, DateUtil.addMinute(lastTime, -5), pageSize);
				if(!orderList.isEmpty()) cur = orderList.get(orderList.size() -1).getModifytime();
				model.put("nowtime", cur);
			}
		}
		List<GoodsOrder> goodsOrderList = new ArrayList<GoodsOrder>();
		boolean showSeq = false;
		if(downgoods){
			//GewaConfig smsConfig = daoService.getObject(GewaConfig.class, ConfigConstant.CFG_TICKET_GOODS_MSG);
			//List<Long> idList = BeanUtil.getIdList(smsConfig.getContent(), ",");
			//if(idList.contains(cinema.getId())){
			CinemaProfile profile = daoService.getObject(CinemaProfile.class,cinemaid);
			if(profile != null && profile.hasDefinePaper()){
				showSeq = true;
			}
			goodsOrderList = synchService.getGoodsOrderListByRelatedidAndLasttime(cinema.getId(), DateUtil.addMinute(synch.getSuccesstime(), -5),GoodsConstant.GOODS_TAG_BMH);
		}
		model.put("showSeq", showSeq);
		addCommonMap(apiUser, cinema, orderList, goodsOrderList, model);
		return getXmlView(model, "api2machine/downCinemaOrderList.vm");
	}
	private void addCommonMap(ApiUser apiUser, Cinema cinema, List<TicketOrder> orderList, List<GoodsOrder> goodsOrderList, ModelMap model){
		GewaConfig gcon = daoService.getObject(GewaConfig.class, ConfigConstant.CFG_CUSTOM_PAPER);
		Map<Long/*mpid*/,OpenPlayItem> opiMap = new HashMap<Long, OpenPlayItem>();
		Map<String, String> checkMap = new HashMap<String, String>();
		Map<String,String> orderMap = new HashMap<String, String>();
		Map<String,String> mobileMap = new HashMap<String, String>();
		List<TicketOrder> tmpList = new ArrayList<TicketOrder>(orderList);
		Map<Long, List<SellSeat>> seatMap = new HashMap<Long, List<SellSeat>>();
		Map<String, Integer> filmfestMap = new HashMap<String, Integer>();
		Map<String, Movie> movieMap = new HashMap<String, Movie>();
		Map<String, String> lyMovienameMap = new HashMap<String, String>();
		Map<String, String> cpMap = new HashMap<String, String>();
		for(TicketOrder order: tmpList){
			int filmfest = 0;
			if(opiMap.get(order.getMpid())==null){
				OpenPlayItem opi = daoService.getObjectByUkey(OpenPlayItem.class, "mpid", order.getMpid(), true);
				opiMap.put(order.getMpid(), opi);
			}
			List<String> movienameList = new ArrayList<String>();
			Movie movie = daoService.getObject(Movie.class, order.getMovieid());
			if(StringUtils.contains(movie.getFlag(), FilmFestConstant.TAG_FILMFEST_16)){
				OpenPlayItem opi = opiMap.get(order.getMpid());
				Map<String, String> opiOtherMap = VmUtils.readJsonToMap(opi.getOtherinfo());
				String smpno = opiOtherMap.get(OpiConstant.SMPNO);
				if(StringUtils.isNotBlank(smpno)){
					filmfest = 1;
					String lymovieids = opiOtherMap.get(OpiConstant.LYMOVIEIDS);
					if(StringUtils.isNotBlank(lymovieids)){
						List<Long> movieidList = BeanUtil.getIdList(lymovieids, ",");
						for(Long mid : movieidList){
							Movie lymovie = daoService.getObject(Movie.class, mid);
							movienameList.add(lymovie.getMoviename());
						}
					}
				}
			}
			lyMovienameMap.put(order.getTradeNo(), StringUtils.join(movienameList, ","));
			movieMap.put(order.getTradeNo(), movie);
			filmfestMap.put(order.getTradeNo(), filmfest);
			String checkpass = StringUtil.md5(order.getCheckpass() + apiUser.getPrivatekey());
			checkMap.put(order.getCheckpass(), checkpass);
			OrderResult orderResult = daoService.getObject(OrderResult.class, order.getTradeNo());
			String flag = "0";
			if(orderResult != null) flag = orderResult.getResult();
			if(StringUtils.isBlank(flag)) flag = "0";
			List<SellSeat> seatList = ticketOrderService.getOrderSeatList(order.getId());
			seatMap.put(order.getId(), seatList);
			orderMap.put(order.getTradeNo(), flag);
			mobileMap.put(order.getTradeNo(), StringUtils.substring(order.getMobile(), 7));
			cpMap.put(order.getTradeNo(), getDefContent(gcon, order));
		}
		Map<Long, Goods> goodsMap = new HashMap<Long, Goods>();
		for(GoodsOrder order : goodsOrderList){
			if(goodsMap.get(order.getGoodsid())==null) {
				Goods goods = daoService.getObject(Goods.class, order.getGoodsid());
				goodsMap.put(order.getGoodsid(), goods);
			}
			String checkpass = StringUtil.md5(order.getCheckpass() + apiUser.getPrivatekey());
			checkMap.put(order.getCheckpass(), checkpass);
			if(!orderMap.containsKey(order.getTradeNo())){
				OrderResult orderResult = daoService.getObject(OrderResult.class, order.getTradeNo());
				String flag = "0";
				if(orderResult != null) flag = orderResult.getResult();
				if(StringUtils.isBlank(flag)) flag = "0";
				orderMap.put(order.getTradeNo(), flag);
			}
			mobileMap.put(order.getTradeNo(), StringUtils.substring(order.getMobile(), 7));
		}
		model.put("goodsOrderList", goodsOrderList);
		model.put("goodsMap", goodsMap);
		
		model.put("filmfestMap", filmfestMap);
		model.put("movieMap", movieMap);
		model.put("lyMovienameMap", lyMovienameMap);
		model.put("mobileMap", mobileMap);
		model.put("orderMap", orderMap);
		model.put("orderList", orderList);
		model.put("opiMap", opiMap);
		model.put("checkMap", checkMap);
		model.put("seatMap", seatMap);
		model.put("cinema", cinema);
		model.put("cpMap", cpMap);
		model.put("GewaOrderHelper", new GewaOrderHelper());
	}
	/**
	 * 根据订单号下载当个订单接口
	 * @param tradeNo
	 * @param model
	 * @return
	 */
	@RequestMapping("/apimac/cinema/downSingleOrder.xhtml")
	public String downSingleOrder(String tradeNo, ModelMap model){
		if(StringUtils.isBlank(tradeNo)) {
			return getErrorXmlView(model, ApiConstant.CODE_PARAM_ERROR, "传递参数错误！");
		}
		TicketOrder ticketOrder = daoService.getObjectByUkey(TicketOrder.class, "tradeNo", tradeNo, false);
		if(ticketOrder == null) {
			return getErrorXmlView(model, ApiConstant.CODE_DATA_ERROR, "没有该订单！");
		}
		Cinema cinema = daoService.getObject(Cinema.class, ticketOrder.getCinemaid());
		ApiAuth auth = NewApiAuthenticationFilter.getApiAuth();
		ApiUser synchUser = auth.getApiUser();
		OrderResult orderResult = daoService.getObject(OrderResult.class, ticketOrder.getTradeNo());
		String flag = "0";
		if(orderResult != null && StringUtils.isNotBlank(orderResult.getResult())){
			flag = orderResult.getResult();
		}
		flag = flag.substring(0,1);
		model.put("order", ticketOrder);
		String status = ApiConstant.getMappedOrderStatus(ticketOrder.getFullStatus());
		model.put("status", status);
		model.put("cinema", cinema);
		model.put("GewaOrderHelper", new GewaOrderHelper());
		model.put("mobile", StringUtils.substring(ticketOrder.getMobile(), 7));
		model.put("seatList", ticketOrderService.getOrderSeatList(ticketOrder.getId()));
		model.put("flag", flag);
		model.put("checkpass", StringUtil.md5(ticketOrder.getCheckpass()+synchUser.getPrivatekey()));
		model.put("opi", daoService.getObjectByUkey(OpenPlayItem.class, "mpid", ticketOrder.getMpid(), true));
		return getXmlView(model, "api2machine/singleTicketOrder.vm");
	}
	/**
	 * 商家无法正常出票订单回传
	 * @param tradeNo
	 * @param remark
	 * @param model
	 * @return
	 */
	@RequestMapping("/apimac/cinema/notifyWarnCallback.xhtml")
	public String notifyWarnCallback(String tradeNo, String remark, ModelMap model){
		ApiAuth auth = NewApiAuthenticationFilter.getApiAuth();
		ApiUser apiUser = auth.getApiUser();
		if(StringUtils.isBlank(tradeNo)) {
			return getErrorXmlView(model, ApiConstant.CODE_PARAM_ERROR, "传递参数错误！");
		}
		if(StringUtils.isNotBlank(remark) && StringUtils.length(remark) > 100) {
			return getErrorXmlView(model, ApiConstant.CODE_PARAM_ERROR, "备注字数不能超过100！");
		}
		GewaOrder order = daoService.getObjectByUkey(GewaOrder.class, "tradeNo", tradeNo, false);
		if(order == null) {
			return getErrorXmlView(model, ApiConstant.CODE_DATA_ERROR, "没有该订单！");
		}
		Map params = new HashMap();
		params.put(MongoData.SYSTEM_ID, MongoData.buildId());
		params.put(MongoData.ACTION_ADDTIME, DateUtil.getCurFullTimestamp());
		params.put(MongoData.ACTION_REMARK, remark);
		params.put(MongoData.ACTION_STATUS, Status.N);
		params.put(MongoData.ACTION_TRADENO, tradeNo);
		params.put(MongoData.ACTION_TYPE, "order");	// 订单问题
		params.put(MongoData.ACTION_API_PARTNERNAME, apiUser.getPartnername());
		params.put(MongoData.ACTION_API_PARTNERKEY, apiUser.getPartnerkey());
		mongoService.saveOrUpdateMap(params, MongoData.SYSTEM_ID, MongoData.NS_API_WARNCALLBACK);
		model.put("result", true);
		return getXmlView(model, "api2machine/result.vm");
	}
	/**
	 * 同步成功订单短信重发
	 * @param tradeNo
	 * @param model
	 * @return
	 */
	@RequestMapping("/apimac/cinema/notifySuccessCallback.xhtml")
	public String notifySuccessCallback( String tradeNo, ModelMap model){
		ApiAuth auth = NewApiAuthenticationFilter.getApiAuth();
		ApiUser apiUser = auth.getApiUser();
		if(StringUtils.isBlank(tradeNo)) {
			return getErrorXmlView(model, ApiConstant.CODE_PARAM_ERROR, "传递参数错误！");
		}
		TicketOrder order = daoService.getObjectByUkey(TicketOrder.class, "tradeNo", tradeNo, false);
		if(order == null){
			return getErrorXmlView(model, ApiConstant.CODE_DATA_ERROR, "没有该订单！");
		}
		if(order.isPaidSuccess()){
			String opkey = apiUser.getPartnerkey() + tradeNo;
			if(!operationService.isAllowOperation(opkey, OperationService.ONE_DAY, 3)){
				return getErrorXmlView(model, ApiConstant.CODE_REPEAT_OPERATION, "同一订单24小时内最多允许发送3次！");
			}
			// 发送短信
			Map jsonmap = JsonUtils.readJsonToMap(apiUser.getOtherinfo());
			String msgTemplate = ""+jsonmap.get("msgTemplate");
			if(StringUtils.isBlank(msgTemplate)){
				return getErrorXmlView(model, ApiConstant.CODE_DATA_ERROR, "短信模板未设置！");
			}
			OpenPlayItem opi = daoService.getObjectByUkey(OpenPlayItem.class, "mpid", order.getMpid(), true);
			List<SellSeat> seatList = ticketOrderService.getOrderSeatList(order.getId());
			ErrorCode<String> msg = messageService.getCheckpassMsg(msgTemplate, order, seatList, opi);
			if(!msg.isSuccess()) return getErrorXmlView(model, ApiConstant.CODE_DATA_ERROR, msg.getMsg());
			Timestamp curtime = DateUtil.getCurTruncTimestamp();
			SMSRecord sms = new SMSRecord(tradeNo, order.getMobile(), msg.getRetval(), DateUtil.addMinute(curtime, -5), 
					DateUtil.addHour(curtime, 3), SmsConstant.SMSTYPE_NOW_API);
			untransService.sendMsgAtServer(sms, false);
			operationService.updateOperation(opkey, OperationService.ONE_DAY, 3);
		}else{
			return getErrorXmlView(model, ApiConstant.CODE_DATA_ERROR, "订单未成功！不能发送短信");
		}
		model.put("result",true);
		return getXmlView(model, "api2machine/result.vm");
	}
	
	/**
	 * 二代机购票取消订单
	 * @param tradeNo
	 * @param model
	 * @return
	 */
	@RequestMapping("/apimac/cinema/cancelTicketOrder.xhtml")
	public String cancelTicketOrder(String tradeNo, ModelMap model){
		if(StringUtils.isBlank(tradeNo)) {
			return getErrorXmlView(model, ApiConstant.CODE_PARAM_ERROR, "传递参数错误！");
		}
		TicketOrder order = daoService.getObjectByUkey(TicketOrder.class, "tradeNo", tradeNo, false);
		if(order == null) {
			return getErrorXmlView(model, ApiConstant.CODE_DATA_ERROR, "没有该订单！");
		}
		ApiAuth auth = NewApiAuthenticationFilter.getApiAuth();
		ApiUser apiUser = auth.getApiUser();
		if(StringUtils.contains(order.getStatus(), OrderConstant.STATUS_PAID) || 
				!StringUtils.equals(order.getPartnerid()+"", PartnerConstant.PARTNER_MACBUY+"")) {
			return getErrorXmlView(model, ApiConstant.CODE_DATA_ERROR, "此订单不能被取消！");
		}
		if(!StringUtils.equals(apiUser.getId()+"", PartnerConstant.PARTNER_CHANGTU+"")){
			String status = OrderConstant.STATUS_TIMEOUT;
			ticketOrderService.cancelTicketOrder2(tradeNo, order.getMemberid(), status, "终端支付超时取消");
		}
		model.put("result", true);
		return getXmlView(model, "api2machine/result.vm");
	}
	/**
	 * 获取后台热映电影推荐图片
	 * @param model
	 * @return
	 */
	@RequestMapping("/apimac/cinema/getMoviePic.xhtml")
	public String hotMoviePic(ModelMap model){
		Map<String, Object> qparam = new HashMap<String, Object>();
		List<Map> map = mongoService.find(MongoData.NS_TICKET_MACHINE_IMAGES, qparam);
		model.put("picMap", map);
		return getXmlView(model, "api2machine/moviePicList.vm");
	}
	@RequestMapping("/apimac/cinema/hotMovieList.xhtml")
	public String hotMovieList(Integer maxnum, ModelMap model, HttpServletRequest request){
		if(maxnum==null) maxnum = 10;
		List<Movie> movieList = new ArrayList<Movie>();
		List<GewaCommend> gcMovieList = commonService.getGewaCommendList(AdminCityContant.CITYCODE_SH, SignName.INDEX_MOVIELIST_NEW, null, null, true, 0, maxnum);
		List<Map<String, Object>> resMapList = new ArrayList<Map<String, Object>>();
		for(GewaCommend commend : gcMovieList){
			if(commend.getRelatedid()!=null) {
				Movie movie = daoService.getObject(Movie.class, commend.getRelatedid());
				if(movie!=null){
					movieList.add(movie);
					Map<String, Object> resMap = getMovieData(movie);
					resMapList.add(resMap);
				}
			}
		}
		return getOpenApiXmlList(resMapList, "movieList,movie", model, request);
	}
	
}
