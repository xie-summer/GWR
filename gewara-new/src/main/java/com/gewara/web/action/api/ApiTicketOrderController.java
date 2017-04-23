package com.gewara.web.action.api;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Restrictions;
import org.hibernate.criterion.Subqueries;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import com.gewara.constant.AdminCityContant;
import com.gewara.constant.ApiConstant;
import com.gewara.constant.app.AppConstant;
import com.gewara.constant.ticket.OpiConstant;
import com.gewara.constant.ticket.OrderConstant;
import com.gewara.constant.ticket.PartnerConstant;
import com.gewara.helper.ticket.CloseRuleOpiFilter;
import com.gewara.helper.ticket.OpiFilter;
import com.gewara.model.api.ApiUser;
import com.gewara.model.common.City;
import com.gewara.model.goods.Goods;
import com.gewara.model.goods.GoodsGift;
import com.gewara.model.movie.Cinema;
import com.gewara.model.movie.CinemaRoom;
import com.gewara.model.movie.Movie;
import com.gewara.model.movie.RoomSeat;
import com.gewara.model.partner.PartnerCloseRule;
import com.gewara.model.pay.GewaOrder;
import com.gewara.model.pay.TicketOrder;
import com.gewara.model.ticket.OpenPlayItem;
import com.gewara.model.ticket.SellSeat;
import com.gewara.model.user.Member;
import com.gewara.model.user.OpenMember;
import com.gewara.pay.PartnerPayUtil;
import com.gewara.service.MessageService;
import com.gewara.service.OperationService;
import com.gewara.support.ErrorCode;
import com.gewara.util.BeanUtil;
import com.gewara.util.DateUtil;
import com.gewara.util.PKCoderUtil;
import com.gewara.web.action.api2mobile.ApiTicketBaseController;

@Controller
public class ApiTicketOrderController extends ApiTicketBaseController{
	@Autowired@Qualifier("messageService")
	private MessageService messageService;
	public void setMessageService(MessageService messageService) {
		this.messageService = messageService;
	}

	@RequestMapping("/api/outorder/openCinemaList.xhtml")
	public String cinemaList(String key, String encryptCode, String citycode, String version, ModelMap model){
		ApiAuth auth = checkRights(encryptCode, key);
		if(!auth.isChecked()) return getErrorXmlView(model, auth.getCode(), auth.getMsg());
		ErrorCode code = addCinemaListData(auth.getApiUser(), citycode, model);
		if(!code.isSuccess()) return getErrorXmlView(model, code.getErrcode(), code.getMsg()); 
		model.put("version", version);
		return getXmlView(model, "api/outorder/openCinemaList.vm");
	}
	
	/**
	 * 查询放映场次
	 * @param key
	 * @param cinemaid
	 * @param movieid
	 * @param playdate
	 * @param encryptCode
	 * @param model
	 * @return
	 */
	@RequestMapping("/api/outorder/opiList.xhtml")
	public String opiList(String key, Long cinemaid, Long movieid, Date playdate, 
			String encryptCode, String citycode, ModelMap model){
		ApiAuth auth = checkRights(encryptCode, key);
		if(!auth.isChecked()) return getErrorXmlView(model, auth.getCode(), auth.getMsg());
		if(cinemaid==null && movieid==null || playdate==null) return getErrorXmlView(model, ApiConstant.CODE_PARAM_ERROR, "影院或电影必须输入一个！");
		ApiUser partner = auth.getApiUser();
		ErrorCode code = addOpiListData(partner, citycode, cinemaid, movieid, playdate, model);
		if(!code.isSuccess()) return getErrorXmlView(model, code.getErrcode(), code.getMsg()); 
		return getXmlView(model, "api/outorder/opiList.vm");
	}
	@RequestMapping("/api/outorder/futureMovieList.xhtml")
	public String futureMovieList(String key, Integer from, Integer maxnum, String encryptCode, ModelMap model){
		ApiAuth auth = checkRights(encryptCode, key);
		if(!auth.isChecked()) return getErrorXmlView(model, auth.getCode(), auth.getMsg());
		if(from == null) from=0;
		if(maxnum == null) maxnum=100;
		List<Movie>  mpiList = mcpService.getFutureMovieList(from, maxnum,null);
		List<PartnerCloseRule> pcrList = partnerService.getCloseRuleList();
		
		CloseRuleOpiFilter filter = new CloseRuleOpiFilter(auth.getApiUser(), pcrList);
		filter.filterMovie(mpiList);
		model.put("mpiList", mpiList);
		return getXmlView(model, "api/outorder/futureMovieList.vm");
	}
	@RequestMapping("/api/outorder/opmList.xhtml")
	public String opmList(String key, Date playdate, Long cinemaid, String encryptCode, String citycode, String version, ModelMap model){
		ApiAuth auth = checkRights(encryptCode, key);
		if(!auth.isChecked()) return getErrorXmlView(model, auth.getCode(), auth.getMsg());
		ApiUser partner = auth.getApiUser();
		//支付宝临时使用if(partner.getId().equals(PayConstant.PARTNER_ALIPAY2)) playdate = null; 
		ErrorCode code = addOpmListData(partner, citycode, cinemaid, playdate, model);
		if(!code.isSuccess()) return getErrorXmlView(model, code.getErrcode(), code.getMsg()); 
		model.put("version", version);
		return getXmlView(model, "api/outorder/opmList.vm");
	}
	
	@RequestMapping("/api/outorder/opcList.xhtml")
	public String opcList(String key, Date playdate, Long movieid, String encryptCode, String citycode, String version, ModelMap model){
		ApiAuth auth = checkRights(encryptCode, key);
		if(!auth.isChecked()) return getErrorXmlView(model, auth.getCode(), auth.getMsg());
		ApiUser partner = auth.getApiUser();
		if(StringUtils.isNotBlank(citycode)){
			if(!partner.supportsCity(citycode)) return getErrorXmlView(model, ApiConstant.CODE_PARTNER_NORIGHTS, "不支持城市" + citycode);
		}else{
			citycode = partner.getCitycode();
		}

		List<Cinema> opcList = partnerService.getOpenCinemaList(partner, citycode, movieid, playdate);

		List<PartnerCloseRule> pcrList = partnerService.getCloseRuleList();
		CloseRuleOpiFilter filter = new CloseRuleOpiFilter(partner, pcrList);
		filter.filterCinema(opcList);
		
		model.put("opcList", opcList);
		model.put("version", version);
		model.put("citynameMap", AdminCityContant.getCitycode2CitynameMap());
		return getXmlView(model, "api/outorder/opcList.vm");
	}
	@RequestMapping("/api/outorder/playdateList.xhtml")
	public String opcList(String key, String encryptCode, Long movieid, Long cinemaid, ModelMap model){
		ApiAuth auth = checkRights(encryptCode, key);
		if(!auth.isChecked()) return getErrorXmlView(model, auth.getCode(), auth.getMsg());
		if(movieid==null || cinemaid==null) return getErrorXmlView(model, ApiConstant.CODE_SIGN_ERROR, "缺少参数");
		List<Date> playdateList = openPlayService.getCinemaAndMovieOpenDateList(cinemaid, movieid);
		model.put("playdateList", playdateList);
		return getXmlView(model, "api/outorder/playdateList.vm");
	}
	@RequestMapping("/api/order/roomList.xhtml")
	public String roomList(String key, String encryptCode, Date updateDate, String cinemaid, String citycode, ModelMap model){
		ApiAuth auth = checkRights(encryptCode, DateUtil.formatDate(updateDate), key);
		if(!auth.isChecked()) return getErrorXmlView(model, auth.getCode(), auth.getMsg());
		ApiUser partner = auth.getApiUser(); 
		if(StringUtils.isNotBlank(citycode)){
			if(!partner.supportsCity(citycode)) return getErrorXmlView(model, ApiConstant.CODE_PARTNER_NORIGHTS, "不支持城市" + citycode);
		}else{
			citycode = partner.getCitycode();
		}
		List<CinemaRoom> roomList = null;
		if(StringUtils.isNotBlank(cinemaid)){
			roomList = daoService.getObjectListByField(CinemaRoom.class, "cinemaid", new Long(cinemaid));
		}else{
			DetachedCriteria query = DetachedCriteria.forClass(CinemaRoom.class, "r");
			query.add(Restrictions.gt("updatetime", updateDate));
			DetachedCriteria subquery = DetachedCriteria.forClass(Cinema.class, "p");
			subquery.add(Restrictions.eq("p.booking", Cinema.BOOKING_OPEN));
			subquery.add(Restrictions.eqProperty("p.id", "r.cinemaid"));
			List<String> citycodeList = Arrays.asList(StringUtils.split(citycode, ","));
			if(citycodeList.size()==1){
				if(!StringUtils.equals(citycode, AdminCityContant.CITYCODE_ALL)) subquery.add(Restrictions.eq("citycode", citycode)); 
			}else{
				subquery.add(Restrictions.in("citycode", citycodeList));
			}
			subquery.add(Subqueries.exists(subquery));
			roomList = hibernateTemplate.findByCriteria(query);
		}
		model.put("roomList", roomList);
		return getXmlView(model, "api/order/roomList.vm");
	}
	/**
	 * 获取场次座位信息
	 */
	@RequestMapping("/api/order/opiSeatInfo.xhtml")
	public String getOpiSeatList(String key, String encryptCode, Long mpid, ModelMap model){
		ApiAuth auth = checkRights(encryptCode, mpid+"", key);
		if(!auth.isChecked()) return getErrorXmlView(model, auth.getCode(), auth.getMsg());

		ApiUser partner = auth.getApiUser();
		if(mpid==null) return getErrorXmlView(model, ApiConstant.CODE_PARAM_ERROR, "参数不正确！");
		OpenPlayItem opi = daoService.getObjectByUkey(OpenPlayItem.class, "mpid", mpid, true);
		if(opi == null) return getErrorXmlView(model, ApiConstant.CODE_OPI_NOT_EXISTS, "场次不存在或已删除！");
		if(StringUtils.contains(opi.getOtherinfo(), OpiConstant.ADDRESS) && partner.getId() >= PartnerConstant.GEWA_CLIENT){
			return getErrorXmlView(model, ApiConstant.CODE_OPI_CLOSED, "本场不接受（或暂停）预订！");
		}
		GoodsGift goodsGift = goodsOrderService.getBindGoodsGift(opi, partner.getId());
		if(goodsGift!=null){
			return getErrorXmlView(model, ApiConstant.CODE_OPI_CLOSED, "本场不接受（或暂停）预订！");
		}
		ErrorCode code = addOpiSeatListData(opi, partner, model);
		if(!code.isSuccess()) return getErrorXmlView(model, code.getErrcode(), code.getMsg());
		return getXmlView(model, "api/order/opiSeatInfo.vm");
	}
	/**
	 * 获取场次座位信息
	 */
	@RequestMapping("/api/order/opiLockedSeat.xhtml")
	public String getOpiLockedSeatList(String key, String encryptCode, Long mpid, String appVersion, ModelMap model){
		ApiAuth auth = checkRights(encryptCode, mpid+"", key);
		if(!auth.isChecked()) return getErrorXmlView(model, auth.getCode(), auth.getMsg());

		if(mpid==null) return getErrorXmlView(model, ApiConstant.CODE_PARAM_ERROR, "参数不正确！");
		ApiUser partner = auth.getApiUser();
		OpenPlayItem opi = daoService.getObjectByUkey(OpenPlayItem.class, "mpid", mpid, true);
		if(opi == null) return getErrorXmlView(model, ApiConstant.CODE_OPI_NOT_EXISTS, "场次不存在或已删除！");
		if(StringUtils.contains(opi.getOtherinfo(), OpiConstant.ADDRESS) && partner.getId() >= PartnerConstant.GEWA_CLIENT){
			return getErrorXmlView(model, ApiConstant.CODE_OPI_CLOSED, "本场不接受（或暂停）预订！");
		}
		GoodsGift goodsGift = goodsOrderService.getBindGoodsGift(opi, partner.getId());
		if(goodsGift!=null){
			if(partner.getId() < PartnerConstant.GEWA_CLIENT){//Gewara商户
				if(StringUtils.isBlank(appVersion) || appVersion.compareTo(AppConstant.MOVIE_APPVERSION_1_5_6)<0){
					return getErrorXmlView(model, ApiConstant.CODE_DATA_ERROR, "本场次不支持当前版本，请升级！");
				}
				model.put("goodsGift", goodsGift);
				Goods goods = daoService.getObject(Goods.class, goodsGift.getGoodsid());
				model.put("goods", goods);
			}else{
				return getErrorXmlView(model, ApiConstant.CODE_OPI_CLOSED, "本场不接受（或暂停）预订！");
			}
		}
		ErrorCode code = addOpiLockedSeatListData(opi, partner, model);
		if(!code.isSuccess()) return getErrorXmlView(model, code.getErrcode(), code.getMsg()); 
		return getXmlView(model, "api/order/opiLockedSeat.vm");
	}
	@RequestMapping("/api/order/roomSeatInfo.xhtml")
	public String getRoomSeatList(String key, String encryptCode, Long roomid, ModelMap model){
		ApiAuth auth = checkRights(encryptCode, roomid+"", key);
		if(!auth.isChecked()) return getErrorXmlView(model, auth.getCode(), auth.getMsg());
		if(roomid==null) return getErrorXmlView(model, ApiConstant.CODE_PARAM_ERROR, "参数不正确！");
		CinemaRoom room = daoService.getObject(CinemaRoom.class, roomid);
		List<RoomSeat> seatList = openPlayService.getSeatListByRoomId(roomid);
		Map<Integer, String> lineMap = new HashMap<Integer, String>();
		Map<String, RoomSeat> seatMap = BeanUtil.beanListToMap(seatList, "position");
		Map<Integer, String> rowMap = new HashMap<Integer, String>();
		RoomSeat seat = null; String status;
		for(int i=1; i<= room.getLinenum(); i++){
			List<String> seatRankList = new ArrayList<String>();
			for(int j=1; j<= room.getRanknum(); j++){
				seat = seatMap.get(i + ":" + j);
				if(seat == null){
					status = "ZL"; //走廊
				}else{
					status = seat.getSeatrank();
					rowMap.put(i, seat.getSeatline());
				}
				seatRankList.add(status);
			}
			lineMap.put(i, StringUtils.join(seatRankList, ","));
		}
		model.put("lineMap", lineMap);
		model.put("room", room);
		model.put("rowMap", rowMap);
		return getXmlView(model, "api/order/roomSeatInfo.vm");
	}
	/**
	 * 增加订单（Partner）
	 * @param key
	 * @param encryptCode
	 * @param mpid
	 * @param mobile
	 * @param ukey
	 * @param seatLabel
	 * @param model
	 * @return
	 */
	@RequestMapping("/api/order/addPartnerOrder.xhtml")
	public String addPartnerOrder(String key, String encryptCode, Long mpid, String mobile, 
			String encMobile, String ukey, String seatLabel, String resv, HttpServletRequest request, ModelMap model){
		ApiAuth auth = checkRights(encryptCode, ""+mpid, mobile, encMobile, seatLabel, key);
		if(!auth.isChecked()) return getErrorXmlView(model, auth.getCode(), auth.getMsg());
		ApiUser partner = auth.getApiUser();
		if(StringUtils.isBlank(mobile)) {
			mobile = PKCoderUtil.decryptWithThiDES(partner.getSecretKey(), encMobile, "utf-8");
			if(StringUtils.isBlank(mobile)){
				return getErrorXmlView(model, ApiConstant.CODE_PARAM_ERROR, "解密encMobile出错！");
			}
		}
		if(StringUtils.isNotBlank(resv)) model.put("resv", resv);
		OpenPlayItem opi = daoService.getObjectByUkey(OpenPlayItem.class, "mpid", mpid, true);
		if(opi==null) return getErrorXmlView(model, ApiConstant.CODE_OPI_NOT_EXISTS, "场次不存在！");
		if(!opi.isOpenToPartner()) return getErrorXmlView(model, ApiConstant.CODE_OPI_CLOSED, "本场次不支持订票");
		if(!opi.isOrder()) return getErrorXmlView(model, ApiConstant.CODE_OPI_CLOSED, OpiConstant.getStatusStr(opi));
		List<PartnerCloseRule> pcrList = partnerService.getCloseRuleList();
		OpiFilter filter = new CloseRuleOpiFilter(partner, pcrList);
		if(filter.excludeOpi(opi)) return getErrorXmlView(model, ApiConstant.CODE_OPI_CLOSED, "本场不接受（或暂停）预订！");
		
		ErrorCode code = addOrder(partner, null, StringUtils.isBlank(ukey)? StringUtils.reverse(partner.getId()+mobile):ukey, opi, mobile, seatLabel, model);
		if(!code.isSuccess()) return getErrorXmlView(model, code.getErrcode(), code.getMsg());
		logAppSourceOrder(request, (GewaOrder)model.get("order"), null, null);
		return getXmlView(model, "api/order/order.vm");
	}
	
	@RequestMapping("/api/order/useCard.xhtml")
	public String useCard(ModelMap model){
		return notSupport(model);
	}

	@RequestMapping("/api/order/qryPartnerOrder.xhtml")
	public String qryPartnerOrder(String key, String encryptCode, String tradeno, ModelMap model){
		ApiAuth auth = checkRights(encryptCode, tradeno, key);
		if(!auth.isChecked()) return getErrorXmlView(model, auth.getCode(), auth.getMsg());
		TicketOrder order = daoService.getObjectByUkey(TicketOrder.class, "tradeNo", tradeno, false);
		if(order==null) return getErrorXmlView(model, ApiConstant.CODE_DATA_ERROR, "订单不存在!");
		if(!order.getPartnerid().equals(auth.getApiUser().getId())) return getErrorXmlView(model, ApiConstant.CODE_DATA_ERROR, "不能查询其他商家订单!");
		OpenPlayItem opi = daoService.getObjectByUkey(OpenPlayItem.class, "mpid", order.getMpid(), true);
		model.put("partner", auth.getApiUser());
		model.put("payUrl", PartnerPayUtil.SHORT_NOTIFY_URL);
		model.put("order", order);
		model.put("opi", opi);
		model.put("orderStatus", ApiConstant.getMappedOrderStatus(order.getFullStatus()));
		return getXmlView(model, "api/order/order.vm");
	}
	
	@RequestMapping("/api/order/qryTicketOrder.xhtml")
	public String queryTicketOrder(String key, String encryptCode, String memberEncode, String tradeno, ModelMap model){
		ApiAuth auth = checkRights(encryptCode, tradeno, key);
		if(!auth.isChecked()) return getErrorXmlView(model, auth.getCode(), auth.getMsg());
		Member member = memberService.getMemberByEncode(memberEncode);
		if(member==null) {
			return getErrorXmlView(model, ApiConstant.CODE_DATA_ERROR, "身份验证错误");
		}
		TicketOrder order = daoService.getObjectByUkey(TicketOrder.class, "tradeNo", tradeno, false);
		OpenPlayItem opi = daoService.getObjectByUkey(OpenPlayItem.class, "mpid", order.getMpid(), true);
		model.put("order", order);
		model.put("opi", opi);
		model.put("orderStatus", ApiConstant.getMappedOrderStatus(order.getFullStatus()));
		return getXmlView(model, "api/order/order.vm");
	}
	@RequestMapping("/api/order/qryOpenMemberOrderList.xhtml")
	public String qryOpemMemberOrderList(String key, String encryptCode, String userid, Integer pageNo, Integer from, Integer maxnum, ModelMap model){
		if(StringUtils.isBlank(userid)) return getErrorXmlView(model, ApiConstant.CODE_PARAM_ERROR, "userid不能为空");
		ApiAuth auth = checkRights(encryptCode, userid, key);
		if(!auth.isChecked()) return getErrorXmlView(model, auth.getCode(), auth.getMsg());
		OpenMember om = memberService.getOpenMemberByLoginname(key, userid);
		if(om==null) return getErrorXmlView(model, auth.getCode(), "用户不存在");
		String qry = "from TicketOrder t where t.memberid=? order by t.addtime desc";
		if(maxnum==null) maxnum = 20;
		if(maxnum > 20) maxnum = 20;
		if(pageNo==null) pageNo = 0;
		else from = pageNo*maxnum;
		if(from==null) from = 0;
		Map<String, String> orderStatusMap = new HashMap<String, String>();
		Map<Long, OpenPlayItem> opiMap = new HashMap<Long, OpenPlayItem>();
		List<TicketOrder> orderList = daoService.queryByRowsRange(qry, from, maxnum, om.getMemberid());
		for(TicketOrder order : orderList){
			if(!opiMap.containsKey(order.getMpid())){
				opiMap.put(order.getMpid(), daoService.getObjectByUkey(OpenPlayItem.class, "mpid", order.getMpid(), true));
			}
			orderStatusMap.put(order.getTradeNo(), ApiConstant.getMappedOrderStatus(order.getFullStatus()));
		}
		model.put("opiMap", opiMap);
		model.put("orderList", orderList);
		model.put("orderStatusMap", orderStatusMap);
		return getXmlView(model, "api/order/openMemberOrderList.vm");
	}
	@RequestMapping("/api/outorder/cinema.xhtml")
	public String cinema(String key, String encryptCode, Long cinemaid, Date playdate, String citycode, ModelMap model){
		ApiAuth auth = checkRights(encryptCode, key);
		if(!auth.isChecked()) return getErrorXmlView(model, auth.getCode(), auth.getMsg());
		if(cinemaid==null && playdate == null) return getErrorXmlView(model, ApiConstant.CODE_PARAM_ERROR, "传递参数错误！");
		ApiUser partner = auth.getApiUser();
		Map<String, String> citynameMap = null;
		List<Cinema> cinemaList = null;
		if(cinemaid != null){
			Cinema cinema = daoService.getObject(Cinema.class, cinemaid);
			if(cinema == null) return getErrorXmlView(model, ApiConstant.CODE_DATA_ERROR, "查询信息不存在！");
			cinemaList = Arrays.asList(cinema);
			citycode = cinema.getCitycode();
			City city = daoService.getObject(City.class, citycode);
			citynameMap = new HashMap<String, String>();
			citynameMap.put(citycode, city.getCityname());
		}else if(playdate != null){
			if(StringUtils.isNotBlank(citycode)){
				if(!partner.supportsCity(citycode)) return getErrorXmlView(model, ApiConstant.CODE_PARTNER_NORIGHTS, "不支持城市" + citycode);
			}else{
				citycode = partner.getCitycode();
			}
			//支付宝临时使用if(partner.getId().equals(PayConstant.PARTNER_ALIPAY2)) playdate = null;
			cinemaList = partnerService.getOpenCinemaList(partner, citycode, null, playdate);
			List<String> citycodeList = BeanUtil.getBeanPropertyList(cinemaList, String.class, "citycode", true);
			citynameMap = daoService.getObjectPropertyMap(City.class, "citycode", "cityname", citycodeList);
		}else{
			return getErrorXmlView(model, ApiConstant.CODE_PARAM_ERROR, "传递参数错误！");
		}
		List<PartnerCloseRule> pcrList = partnerService.getCloseRuleList();
		CloseRuleOpiFilter filter = new CloseRuleOpiFilter(partner, pcrList);
		filter.filterCinema(cinemaList);
		model.put("cinemaList", cinemaList);
		model.put("citynameMap", citynameMap);
		
		return getXmlView(model, "api/outorder/cinema.vm");
	}
	
	@RequestMapping("/api/outorder/movie.xhtml")
	public String movie(String key, String encryptCode, Long movieid, Long cinemaid, Date playdate, String citycode, ModelMap model){
		ApiAuth auth = checkRights(encryptCode, key);
		if(!auth.isChecked()) return getErrorXmlView(model, auth.getCode(), auth.getMsg());
		if(movieid == null && playdate == null) return getErrorXmlView(model, ApiConstant.CODE_PARAM_ERROR, "传递参数错误！");
		List<Movie> movieList = null;
		ApiUser partner = auth.getApiUser();
		if(movieid != null){
			Movie movie = daoService.getObject(Movie.class, movieid);
			if(movie == null) return getErrorXmlView(model, ApiConstant.CODE_DATA_ERROR, "查询信息不存在！");
			movieList = new ArrayList<Movie>(Arrays.asList(movie));
		}else if(playdate != null){
			//支付宝临时使用if(partner.getId().equals(PayConstant.PARTNER_ALIPAY2)) playdate = null;
			if(StringUtils.isNotBlank(citycode)){
				if(!partner.supportsCity(citycode)) return getErrorXmlView(model, ApiConstant.CODE_PARTNER_NORIGHTS, "不支持城市" + citycode);
			}else{
				citycode = partner.getCitycode();
			}
			movieList = partnerService.getOpenMovieList(partner, citycode, cinemaid, playdate);
		}else{
			return getErrorXmlView(model, ApiConstant.CODE_PARAM_ERROR, "传递参数错误！");
		}
		List<PartnerCloseRule> pcrList = partnerService.getCloseRuleList();
		CloseRuleOpiFilter filter = new CloseRuleOpiFilter(partner, pcrList);
		filter.filterMovie(movieList);
		model.put("movieList", movieList);
		return getXmlView(model, "api/outorder/movie.vm");
	}
	@RequestMapping("/api/order/qryOrderStatus.xhtml")
	public String qryOrderStatus(String key, String encryptCode, String tradeno, ModelMap model){
		ApiAuth auth = checkRights(encryptCode, tradeno, key);
		if(!auth.isChecked()) return getErrorXmlView(model, auth.getCode(), auth.getMsg());
		TicketOrder order = daoService.getObjectByUkey(TicketOrder.class, "tradeNo", tradeno, false);
		if(order==null) return getErrorXmlView(model, ApiConstant.CODE_DATA_ERROR, "订单不存在!");
		if(!order.getPartnerid().equals(auth.getApiUser().getId())) return getErrorXmlView(model, ApiConstant.CODE_DATA_ERROR, "不能查询其他商家订单!");
		OpenPlayItem opi = daoService.getObjectByUkey(OpenPlayItem.class, "mpid", order.getMpid(), true);
		model.put("order", order);
		model.put("orderStatus", ApiConstant.getMappedOrderStatus(order.getFullStatus()));
		if(order.isPaidSuccess()){//成功订单
			String msgTemplate = messageService.getCheckpassTemplate(opi);
			String checkpass = msgTemplate.indexOf("hfhpass") >= 0 ? order.getHfhpass(): order.getCheckpass();
			List<SellSeat> seatList = ticketOrderService.getOrderSeatList(order.getId());
			String remark = messageService.getCheckpassMsg(msgTemplate, order, seatList, opi).getRetval();
			String deskey = auth.getApiUser().getSecretKey();
			model.put("encCheckpass", PKCoderUtil.encryptWithThiDES(deskey, checkpass, "utf-8"));
			model.put("encMobile", PKCoderUtil.encryptWithThiDES(deskey, order.getMobile(), "utf-8"));
			model.put("encRemark", PKCoderUtil.encryptWithThiDES(deskey, remark, "utf-8"));
		}
		return getXmlView(model, "api/order/orderStatus.vm");
	}
	@RequestMapping("/api/order/qryPaidOrderByPaidtime.xhtml")
	public String qryPaidOrderByPaidtime(String key, String encryptCode, String paidtimeFrom, String paidtimeTo, ModelMap model){
		ApiAuth auth = checkRights(encryptCode, paidtimeFrom, paidtimeTo, key);
		if(!auth.isChecked()) return getErrorXmlView(model, auth.getCode(), auth.getMsg());
		Timestamp timeFrom = DateUtil.parseTimestamp(paidtimeFrom);
		Timestamp timeTo = DateUtil.parseTimestamp(paidtimeTo);
		if(timeFrom==null || timeTo ==null) return getErrorXmlView(model, ApiConstant.CODE_PARAM_ERROR, "缺少时间参数！"); 
		if(DateUtil.addDay(timeFrom, 31).before(timeTo)) return getErrorXmlView(model, ApiConstant.CODE_PARAM_ERROR, "时间跨度不能超过1个月！"); 
		List<TicketOrder> orderList = partnerService.getPaidOrderListByPaidtime(auth.getApiUser(), timeFrom, timeTo);
		model.put("orderList", orderList);
		model.put("orderStatusMap", ApiConstant.ORDER_STATUS_MAP);
		return getXmlView(model, "api/order/orderList.vm");
	}
	@RequestMapping("/api/order/qryRefundOrder.xhtml")
	public String getRefundOrderList(String key, String encryptCode, String refundtimeFrom, String refundtimeTo, ModelMap model){
		ApiAuth auth = checkRights(encryptCode, refundtimeFrom, refundtimeTo, key);
		if(!auth.isChecked()) return getErrorXmlView(model, auth.getCode(), auth.getMsg());
		Timestamp timeFrom = DateUtil.parseTimestamp(refundtimeFrom);
		Timestamp timeTo = DateUtil.parseTimestamp(refundtimeTo);
		if(timeFrom==null || timeTo ==null) return getErrorXmlView(model, ApiConstant.CODE_PARAM_ERROR, "缺少时间参数！"); 
		if(DateUtil.addDay(timeFrom, 31).before(timeTo)) return getErrorXmlView(model, ApiConstant.CODE_PARAM_ERROR, "时间跨度不能超过1个月！"); 
		List<TicketOrder> orderList = partnerService.getRefundOrderList(auth.getApiUser(), timeFrom, timeTo);
		model.put("orderList", orderList);
		model.put("orderStatusMap", ApiConstant.ORDER_STATUS_MAP);
		return getXmlView(model, "api/order/orderList.vm");
	}
	
	@RequestMapping("/api/message/reSend.xhtml")
	public String reSend(String key, String encryptCode, String tradeno, ModelMap model){
		ApiAuth auth = checkRights(encryptCode, tradeno, key);
		if(!auth.isChecked()) return getErrorXmlView(model, auth.getCode(), auth.getMsg());
		if(StringUtils.isBlank(tradeno)) return getErrorXmlView(model, ApiConstant.CODE_PARAM_ERROR, "传递参数错误！");
		TicketOrder order = daoService.getObjectByUkey(TicketOrder.class, "tradeNo", tradeno, false);
		if(order == null) return getErrorXmlView(model, ApiConstant.CODE_DATA_ERROR, "查询信息不存在！");
		if(!order.getPartnerid().equals(auth.getApiUser().getId())) return getErrorXmlView(model, ApiConstant.CODE_DATA_ERROR, "不能查询其他商家订单!");
		if(!StringUtils.equals(order.getStatus(), OrderConstant.STATUS_PAID_SUCCESS)){
			return getErrorXmlView(model, ApiConstant.CODE_DATA_ERROR, "非成功的订单不能发送消息");
		}
		if(!order.isPaidSuccess()) return getErrorXmlView(model, ApiConstant.CODE_DATA_ERROR, "未成功的订单！");
		String opkey = OperationService.TAG_SENDTICKETPWD + auth.getApiUser().getId() + order.getId();
		if(!operationService.isAllowOperation(opkey, OperationService.ONE_DAY * 3, 3)){
			return getErrorXmlView(model, ApiConstant.CODE_DATA_ERROR,  "同一订单最多只能发送3次！");
		}
		untransService.reSendOrderMsg(order);
		operationService.updateOperation(opkey, OperationService.ONE_DAY * 3, 3);
		return getXmlView(model, "api/outorder/result.vm");
	}
}