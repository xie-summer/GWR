package com.gewara.api.service.merchant;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang.StringUtils;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.support.PropertyComparator;

import com.gewara.api.merchant.service.MerchantReportService;
import com.gewara.api.merchant.vo.CinemaDetailSellerVo;
import com.gewara.api.merchant.vo.EverydayMpiReportVo;
import com.gewara.api.merchant.vo.GoodsOrderReportVo;
import com.gewara.api.merchant.vo.GoodsSummaryReportTotalVo;
import com.gewara.api.merchant.vo.GoodsSummaryReportVo;
import com.gewara.api.merchant.vo.MovieCityBoughtReportVo;
import com.gewara.api.merchant.vo.MovieMpiSeatReportVo;
import com.gewara.api.merchant.vo.MovieSellerTotalVo;
import com.gewara.api.merchant.vo.MovieSellerVo;
import com.gewara.api.merchant.vo.OrderRefundVo;
import com.gewara.api.merchant.vo.RefundOrderReportVo;
import com.gewara.api.merchant.vo.RefundOrderTotalVo;
import com.gewara.api.vo.ResultCode;
import com.gewara.command.EmailRecord;
import com.gewara.constant.GoodsConstant;
import com.gewara.constant.sys.MongoData;
import com.gewara.constant.ticket.OrderConstant;
import com.gewara.constant.ticket.RefundConstant;
import com.gewara.json.CinemaProNotify;
import com.gewara.model.api.OrderResult;
import com.gewara.model.movie.Cinema;
import com.gewara.model.movie.CinemaProfile;
import com.gewara.model.pay.GoodsOrder;
import com.gewara.model.pay.OrderRefund;
import com.gewara.model.pay.TicketOrder;
import com.gewara.model.ticket.OpenPlayItem;
import com.gewara.mongo.MongoService;
import com.gewara.service.gewapay.RefundService;
import com.gewara.service.gewapay.ReportService;
import com.gewara.service.impl.BaseServiceImpl;
import com.gewara.untrans.MailService;
import com.gewara.util.BeanUtil;
import com.gewara.util.DateUtil;
import com.gewara.util.ObjectId;
import com.gewara.util.ReportUtil;
import com.gewara.util.ValidateUtil;
import com.gewara.util.VmUtils;
import com.gewara.util.VoCopyUtil;

public class MerchantReportServiceImpl  extends BaseServiceImpl  implements MerchantReportService {
	@Autowired@Qualifier("reportService")
	private ReportService reportService;
	
	@Autowired@Qualifier("refundService")
	private RefundService refundService;
	
	@Autowired@Qualifier("mongoService")
	private MongoService mongoService;
	
	@Autowired@Qualifier("mailService")
	private MailService mailService;
	
	@Override
	public ResultCode<MovieSellerTotalVo> movieSellStatistics(String cinemaIds, Long movieId,String timeType,Timestamp startTime, Timestamp endTime
			,int from ,int maxnum) {
		if(StringUtils.isBlank(cinemaIds)){
			return ResultCode.getFailure("请选择影院！");
		}
		if(startTime == null || endTime == null) {
			return ResultCode.getFailure("请选择时间范围！");
		}
		if(DateUtil.getDiffDay(startTime, endTime)>31){
			return ResultCode.getFailure("时间跨度不能大于1月！");
		}
		String[] cIds = StringUtils.split(cinemaIds, ",");
		List<MovieSellerVo> vos = new ArrayList<MovieSellerVo>();
		List<Long> cIdList = new ArrayList<Long>();
		Map<Long,Cinema> cMap = new HashMap<Long,Cinema>();
		for(String cId : cIds){
			if(ValidateUtil.isNumber(cId)){
				Cinema cinema = baseDao.getObject(Cinema.class,Long.valueOf(cId));
				if(cinema == null){
					return ResultCode.getFailure("选择的影院" + cId + "不存在");
				}
				cIdList.add(cinema.getId());
				cMap.put(cinema.getId(), cinema);
			}
		}
		List<Map> dataList = null;
		if("addtime".equals(timeType)){
			dataList = reportService.getCinemaSummaryByAddtime(cIdList, startTime, endTime, movieId, "");
		}else{
			dataList = reportService.getCinemaSummaryByPlaytime(cIdList, startTime, endTime, movieId, "");
		}
		for(Map data : dataList){
			Cinema cinema = cMap.get(data.get("cinemaid"));
			data.put("citycode", cinema.getCitycode());
			data.put("cinemaName", cinema.getName());
			MovieSellerVo vo = new MovieSellerVo();
			try {
				BeanUtils.copyProperties(vo, data);
			} catch (Exception e) {
			}
			vos.add(vo);
		}
		MovieSellerTotalVo totalVo = newMovieSellerTotalVo(dataList,maxnum == 0 ? vos : BeanUtil.getSubList(vos, from, maxnum));
		totalVo.setCinemaCount(cIds.length);
		totalVo.setTotalCount(vos.size());
		if(movieId == null){
			totalVo.setMovieCount(this.totalMovie(timeType, startTime, endTime, cIdList));
		}else{
			totalVo.setMovieCount(dataList.isEmpty() ? 0 : 1);
		}
		return ResultCode.getSuccessReturn(totalVo);
	}
	
	private MovieSellerTotalVo newMovieSellerTotalVo(List<Map> dataList,List<MovieSellerVo> movieSellerVoList){
		MovieSellerTotalVo vo = new MovieSellerTotalVo();
		vo.setTolalMpi(Long.valueOf(ReportUtil.getIntSum(dataList, "mpicount", "0").toString()));
		vo.setTotalcost(Long.valueOf(ReportUtil.getIntSum(dataList, "totalcost", "0").toString()));
		vo.setTotalOrder(Long.valueOf(ReportUtil.getIntSum(dataList, "totalcount", "0").toString()));
		vo.setTotalQuantity(Long.valueOf(ReportUtil.getIntSum(dataList, "totalquantity", "0").toString()));
		vo.setMovieSellerVoList(movieSellerVoList);
		return vo;
	}
	
	private int totalMovie(String timeType,Timestamp startTime, Timestamp endTime,List<Long> cIds){
		DetachedCriteria query = DetachedCriteria.forClass(TicketOrder.class);
		query.add(Restrictions.eq("settle", "Y"));
		query.add(Restrictions.in("cinemaid", cIds));
		query.add(Restrictions.eq("status", OrderConstant.STATUS_PAID_SUCCESS));
		if("addtime".equals(timeType)){
			query.add(Restrictions.ge("addtime", startTime));
			query.add(Restrictions.le("addtime", endTime));
		}else{
			query.add(Restrictions.ge("playtime", startTime));
			query.add(Restrictions.le("playtime", endTime));
		}
		query.setProjection(Projections.groupProperty("movieid"));
		List result = hibernateTemplate.findByCriteria(query);
		return new Integer(result.size() +"");
	}

	@Override
	public ResultCode<MovieSellerTotalVo> movieSellStatistics(long cinemaId,Long movieId,String timeType, String opentype,
			Timestamp startTime, Timestamp endTime,int from ,int maxnum) {
		Cinema cinema = baseDao.getObject(Cinema.class,cinemaId);
		if(cinema == null){
			return ResultCode.getFailure("请选择影院！");
		}
		if(startTime == null || endTime == null) {
			return ResultCode.getFailure("请选择时间范围！");
		}
		if(DateUtil.getDiffDay(startTime, endTime)>31){
			return ResultCode.getFailure("时间跨度不能大于1月！");
		}
		List<Map> dataList = null;
		if(StringUtils.equals("SYS", opentype)){
			CinemaProfile profile = baseDao.getObject(CinemaProfile.class, cinema.getId());
			if(profile != null){
				opentype = profile.getOpentype();
			}
		}
		if(StringUtils.equals(timeType, "addtime")){
			dataList = reportService.getTicketOrderDataByAddtime(cinemaId,movieId, startTime, endTime, opentype);
		}else{
			dataList = reportService.getTicketOrderDataByPlaytime(cinemaId,movieId, startTime, endTime, opentype);
		}
		MovieSellerTotalVo vo = new MovieSellerTotalVo();
		List<CinemaDetailSellerVo> cinemaSellerVoList = new ArrayList<CinemaDetailSellerVo>();
		vo.setTolalMpi(Long.valueOf(dataList.size() + ""));
		vo.setTotalcost(Long.valueOf(ReportUtil.getIntSum(dataList, "totalcost", "0").toString()));
		vo.setTotalOrder(Long.valueOf(ReportUtil.getIntSum(dataList, "totalcount", "0").toString()));
		vo.setTotalQuantity(Long.valueOf(ReportUtil.getIntSum(dataList, "quantity", "0").toString()));
		vo.setTotalCount(dataList.size());
		vo.setCinemaCount(1);
		vo.setMovieCount(BeanUtil.getBeanPropertyList(dataList, Long.class, "movieid", true).size());
		if(maxnum > 0 ){
			dataList = BeanUtil.getSubList(dataList, from, maxnum);
		}
		OpenPlayItem opi = null;
 		for(Map entry : dataList){
			CinemaDetailSellerVo cd = new CinemaDetailSellerVo();
			opi = baseDao.getObjectByUkey(OpenPlayItem.class, "mpid", new Long(entry.get("mpid")+""), true);
			if(opi!=null){
				cd.setMovieId(opi.getMovieid());
				cd.setMovieName(opi.getMoviename());
				cd.setRoomName(opi.getRoomname());
				cd.setPlayTime(opi.getPlaytime());
				cd.setOpentype(opi.getOpentype());
				cd.setMpId(opi.getMpid());
				try {
					BeanUtils.copyProperties(cd, entry);
				} catch (Exception e) {
				}
				cinemaSellerVoList.add(cd);
			}
		}
		vo.setCinemaSellerVoList(cinemaSellerVoList);
		return ResultCode.getSuccessReturn(vo);
	}

	@Override
	public ResultCode<RefundOrderTotalVo> refundReport(String cinemaIds,Long movieId, String timeType, Timestamp startTime,
			Timestamp endTime, int from, int maxnum) {
		if(StringUtils.isBlank(cinemaIds)){
			return ResultCode.getFailure("请选择影院！");
		}
		if(startTime == null || endTime == null) {
			return ResultCode.getFailure("请选择时间范围！");
		}
		if(DateUtil.getDiffDay(startTime, endTime)>31){
			return ResultCode.getFailure("时间跨度不能大于1月！");
		}
		String[] cIds = StringUtils.split(cinemaIds, ",");
		List<RefundOrderReportVo> vos = new ArrayList<RefundOrderReportVo>();
		Map data = null;
		List<Map> dataList = new ArrayList<Map>();
		Set<Long> mIds = new HashSet<Long>();
		for(String cId : cIds){
			if(ValidateUtil.isNumber(cId)){
				Cinema cinema = baseDao.getObject(Cinema.class,Long.valueOf(cId));
				if(cinema == null){
					return ResultCode.getFailure("选择的影院" + cId + "不存在");
				}
				data = reportService.getRefundOrderData(cinema.getId(), movieId, startTime, endTime, timeType);
				if(data!=null){
					RefundOrderReportVo vo = new RefundOrderReportVo();
					vo.setCinemaName(cinema.getName());
					vo.setCitycode(cinema.getCitycode());
					vo.setCinemaid(cinema.getId());
					try {
						BeanUtils.copyProperties(vo, data);
					} catch (Exception e) {
					}
					vos.add(vo);
					dataList.add(data);
					mIds.addAll((List<Long>)data.get("movieIds"));
				}
			}
		}
		RefundOrderTotalVo vo = new RefundOrderTotalVo();
		vo.setCinemaCount(vos.size());
		vo.setTotalCount(vos.size());
		vo.setMovieCount(mIds.size());
		vo.setTotalOrder(Integer.valueOf(ReportUtil.getIntSum(dataList, "totalCount", "0").toString()));
		vo.setRefundOrder(Integer.valueOf(ReportUtil.getIntSum(dataList, "orderCount", "0").toString()));
		vo.setRefundAmount(Integer.valueOf(ReportUtil.getIntSum(dataList, "totalcost", "0").toString()));
		vo.setRefundQuantity(Integer.valueOf(ReportUtil.getIntSum(dataList, "quantity", "0").toString()));
		vo.setMpiCount(Integer.valueOf(ReportUtil.getIntSum(dataList, "mpidCount", "0").toString()));
		vo.setRefundOrderReportList(maxnum == 0 ? vos : BeanUtil.getSubList(vos, from, maxnum));
		return ResultCode.getSuccessReturn(vo);
	}

	@Override
	public ResultCode<RefundOrderTotalVo> refundOrderByCinema(long cinemaId,Long movieId, String timeType, Timestamp startTime,
			Timestamp endTime, int from, int maxnum) {
		Cinema cinema = baseDao.getObject(Cinema.class,cinemaId);
		if(cinema == null){
			return ResultCode.getFailure("请选择影院！");
		}
		if(startTime == null || endTime == null) {
			return ResultCode.getFailure("请选择时间范围！");
		}
		if(DateUtil.getDiffDay(startTime, endTime)>31){
			return ResultCode.getFailure("时间跨度不能大于1月！");
		}
		List<OrderRefund> refundList = null;
		if(StringUtils.equals("addtime", timeType)){
			String query = "from OrderRefund where addtime>=? and addtime<=? and ordertype=? and placeid=? and (status=? or status=?) and orderstatus = ? ";
			refundList = hibernateTemplate.find(query, startTime, endTime, "ticket", cinemaId, RefundConstant.STATUS_SUCCESS, RefundConstant.STATUS_FINISHED, OrderConstant.STATUS_PAID_SUCCESS);
		}else{
			refundList = refundService.getSettleRefundList("ticket", startTime, endTime, cinemaId);
		}
		RefundOrderTotalVo vo = new RefundOrderTotalVo();
		List<OrderRefundVo> orderRefundVoList = new ArrayList<OrderRefundVo>();
		for(OrderRefund refund: refundList){
			TicketOrder tmp = baseDao.getObjectByUkey(TicketOrder.class, "tradeNo", refund.getTradeno(), false);
			if(movieId != null && !movieId.equals(tmp.getMovieid())){
				continue;
			}
			ResultCode<OrderRefundVo> r = VoCopyUtil.copyProperties(OrderRefundVo.class, refund);
			OrderRefundVo or = null;
			if(r.isSuccess()){
				or = r.getRetval();
			}
			or.setTradeNo(refund.getTradeno());
			or.setOtherInfo(tmp.getDescription2());
			or.setMemberId(refund.getMemberid());
			or.setNickName(tmp.getMembername());
			or.setMovieId(tmp.getMovieid());
			or.setCinemaId(tmp.getCinemaid());
			or.setPlayTime(tmp.getPlaytime());
			or.setUnitprice(tmp.getCostprice());
			or.setTotalfee(tmp.getCostprice() * tmp.getQuantity());
			or.setQuantity(tmp.getQuantity());
			or.setOrderCost(or.getTotalfee() - refund.getOldSettle() + refund.getNewSettle());
			orderRefundVoList.add(or);
		}
		vo.setRefundQuantity(Integer.parseInt(ReportUtil.getIntSum(orderRefundVoList,"quantity","0").toString()));
		vo.setRefundAmount(Integer.parseInt(ReportUtil.getIntSum(orderRefundVoList,"totalfee","0").toString()));
		vo.setRefundOrder(orderRefundVoList.size());
		vo.setTotalCount(orderRefundVoList.size());
		vo.setOrderRefundVoList(maxnum == 0 ? orderRefundVoList : BeanUtil.getSubList(orderRefundVoList, from, maxnum));
		return ResultCode.getSuccessReturn(vo);
	}

	@Override
	public ResultCode<GoodsSummaryReportTotalVo> goodsSummaryReport(String cinemaIds, String timetype, Timestamp startTime,
			Timestamp endTime, int from, int maxnum) {
		if(StringUtils.isBlank(cinemaIds)){
			return ResultCode.getFailure("请选择影院！");
		}
		if(startTime == null || endTime == null) {
			return ResultCode.getFailure("请选择时间范围！");
		}
		if(DateUtil.getDiffDay(startTime, endTime)>31){
			return ResultCode.getFailure("时间跨度不能大于1月！");
		}
		String[] cIds = StringUtils.split(cinemaIds, ",");
		GoodsSummaryReportTotalVo vo = new GoodsSummaryReportTotalVo();
		List<GoodsSummaryReportVo> vos = new ArrayList<GoodsSummaryReportVo>();
		List<Long> cIdList = new ArrayList<Long>();
		for(String cId : cIds){
			if(ValidateUtil.isNumber(cId)){
				Cinema cinema = baseDao.getObject(Cinema.class,Long.valueOf(cId));
				if(cinema == null){
					return ResultCode.getFailure("选择的影院" + cId + "不存在");
				}
				Map data = null;
				if("addtime".equals(timetype)){
					data = reportService.getGoodsSummaryByAddtime(cinema.getId(), startTime, endTime,true);
				}else{
					data = reportService.getGoodsSummaryByTaketime(cinema.getId(), startTime, endTime);
				}
				if(data != null){
					GoodsSummaryReportVo rv = new GoodsSummaryReportVo(cinema.getId(),cinema.getName(),cinema.getCitycode());
					rv.setOrderCount(Integer.parseInt(data.get("totalcount").toString()));
					rv.setQuantity(Integer.parseInt(data.get("totalquantity").toString()));
					rv.setTotalcost(Integer.parseInt(data.get("totalcost").toString()));
					vos.add(rv);
					cIdList.add(cinema.getId());
				}
			}
		}
		vo.setCinemaCount(vos.size());
		vo.setTotalCount(vos.size());
		vo.setTotalAmount(Integer.parseInt(ReportUtil.getIntSum(vos,"totalcost","0").toString()));
		vo.setOrderCount(Integer.parseInt(ReportUtil.getIntSum(vos,"orderCount","0").toString()));
		vo.setQuantity(Integer.parseInt(ReportUtil.getIntSum(vos,"quantity","0").toString()));
		vo.setGoodsSummaryReportVoList(maxnum == 0 ? vos : BeanUtil.getSubList(vos, from, maxnum));
		if(cIdList.isEmpty()){
			vo.setTotalGoods(0);
		}else{
			vo.setTotalGoods(getGoodsCount(StringUtils.join(cIdList,","), timetype, startTime, endTime));
		}
		return ResultCode.getSuccessReturn(vo);
	}
	
	private int getGoodsCount(String ids,String timetype, Timestamp startTime,Timestamp endTime){
		int goodsCount = 0;
		if("addtime".equals(timetype)){
			String qry = "select o.goodsid from GoodsOrder o where o.status=? and o.addtime>=? and o.addtime<? and o.placeid in (" +
					ids +") and exists(select g.id from Goods g where g.relatedid in (" + ids + ") and g.tag=? and g.id=o.goodsid) " +
					"and exists(select r.tradeno from OrderResult r where r.taketime is not null and r.istake='Y' and r.tradeno=o.tradeNo) " +
					"group by o.goodsid";
			goodsCount = hibernateTemplate.find(qry, OrderConstant.STATUS_PAID_SUCCESS, startTime, endTime, GoodsConstant.GOODS_TAG_BMH).size();
		}else{
			Timestamp minaddtime = DateUtil.addDay(startTime, -15); 
			String qry = "select o.goodsid from GoodsOrder o where o.status=? and o.addtime>=? and o.addtime<? " +
					"and exists(select g.id from Goods g where g.relatedid in (" + ids + ") and g.tag=? and g.id=o.goodsid) " +
					"and exists(select r.tradeno from OrderResult r where r.taketime is not null and r.taketime>= ? and taketime<? and r.istake=? and r.tradeno=o.tradeNo) " +
					"group by o.goodsid";
			goodsCount = hibernateTemplate.find(qry, OrderConstant.STATUS_PAID_SUCCESS, minaddtime, endTime, GoodsConstant.GOODS_TAG_BMH, startTime, endTime, "Y").size();
		}
		return goodsCount;
	}

	@Override
	public ResultCode<GoodsSummaryReportTotalVo> goodsSummaryReportByCinema(long cinemaId, String timetype,Timestamp startTime, Timestamp endTime, int from, int maxnum) {
		Cinema cinema = baseDao.getObject(Cinema.class,cinemaId);
		if(cinema == null){
			return ResultCode.getFailure("请选择影院！");
		}
		if(startTime == null || endTime == null) {
			return ResultCode.getFailure("请选择时间范围！");
		}
		if(DateUtil.getDiffDay(startTime, endTime)>31){
			return ResultCode.getFailure("时间跨度不能大于1月！");
		}
		GoodsSummaryReportTotalVo vo = new GoodsSummaryReportTotalVo();
		List<GoodsOrder> orderList = null;
		if("addtime".equals(timetype)){
			orderList = reportService.getCinemaGoodsOrderByAddtime(cinemaId, startTime, endTime,true);
		}else{
			orderList = reportService.getCinemaGoodsOrderByTaketime(cinemaId, startTime, endTime);
		}
		vo.setTotalCount(orderList.size());
		vo.setOrderCount(orderList.size());
		vo.setCinemaCount(1);
		vo.setQuantity(Integer.parseInt(ReportUtil.getIntSum(orderList,"quantity","0").toString()));
		vo.setTotalAmount(Integer.parseInt(ReportUtil.getIntSum(orderList,"totalfee","0").toString()));
		vo.setTotalGoods(orderList.isEmpty() ? 0 : getGoodsCount(cinemaId + "", timetype, startTime, endTime));
		Collections.sort(orderList, new PropertyComparator("addtime", false, false));
		if(maxnum != 0){
			orderList = BeanUtil.getSubList(orderList, from, maxnum);
		}
		List<GoodsOrderReportVo> orderVos = new ArrayList<GoodsOrderReportVo>();
		for(GoodsOrder order : orderList){
			GoodsOrderReportVo  orderVo = new GoodsOrderReportVo(order.getTradeNo(),order.getOrdertitle(),order.getAddtime(),order.getUnitprice(),order.getQuantity());
			OrderResult result = baseDao.getObjectByUkey(OrderResult.class, "tradeno", order.getTradeNo(),true);
			if(result != null && StringUtils.equals("Y", result.getIstake())){
				orderVo.setTaketime(result.getTaketime());
			}
			orderVos.add(orderVo);
		}
		vo.setGoodsOrderReportVoList(orderVos);
		return ResultCode.getSuccessReturn(vo);
	}

	@Override
	public ResultCode<EverydayMpiReportVo> mpiReportByPlayDate(Date playDate) {
		if(playDate == null){
			return ResultCode.getFailure(ResultCode.CODE_DATA_ERROR, "playDate不能为空！"); 
		}
		String cSql = "select count(c.id) from CinemaProfile c where c.opentype is not null and c.opentype <> 'GEWA'";
		String qry = "select count(distinct m.cinemaid) from MoviePlayItem m where m.playdate=?";
		String movieQry = "select new map(count(m.movieid) as mpiCount,m.movieid as movieId) from MoviePlayItem m where m.playdate=? group by m.movieid";
		String goleMovieQry = "select new map(count(m.movieid) as mpiCount,m.movieid as movieId)from MoviePlayItem m where m.playdate=? and playtime >= ? and playtime <= ? group by m.movieid";
		String startHour = "18:00";
		String endHour = "21:00";
		int week = DateUtil.getWeek(playDate);
		if(week == 6 || week == 7){
			startHour = "13:00";
		}
		List<Map<String,Long>> movieMpiCount = hibernateTemplate.find(movieQry,playDate);
		EverydayMpiReportVo vo = new EverydayMpiReportVo(playDate,Integer.parseInt(hibernateTemplate.find(cSql).get(0).toString()),
				Integer.parseInt(hibernateTemplate.find(qry,playDate).get(0).toString()),
				(Integer)(ReportUtil.getIntSum(movieMpiCount,"mpiCount","0")),
				movieMpiCount,hibernateTemplate.find(goleMovieQry,playDate,startHour,endHour));
		return ResultCode.getSuccessReturn(vo);
	}
	
	@Override
	public ResultCode<List<MovieMpiSeatReportVo>> mpiRoomSeatReports(Date playDate,String startHour,String endHour){
		if(playDate == null || StringUtils.isBlank(startHour) || StringUtils.isBlank(endHour)){
			return ResultCode.getFailure(ResultCode.CODE_DATA_ERROR, "参数不能为空！"); 
		}
		String movieQry = "select new map(m.id as mpid,m.movieid as movieId,m.roomid as roomId) from MoviePlayItem m where m.playdate=? and playtime >= ? and playtime < ?";
		List<Map<String,Long>> tmpMapList = hibernateTemplate.find(movieQry,playDate,startHour,endHour);
		Map<Long,MovieMpiSeatReportVo> movieSeatMap = new HashMap<Long,MovieMpiSeatReportVo>();
		for(Map<String,Long> map : tmpMapList){
			MovieMpiSeatReportVo vo = movieSeatMap.get(map.get("movieId"));
			if(vo == null){
				vo = new MovieMpiSeatReportVo(map.get("movieId"));
			}
			int seats = vo.getAllSeats() == null ? 0 : vo.getAllSeats();
			/*CinemaRoom cr = baseDao.getObject(CinemaRoom.class, map.get("roomId"));
			if(cr != null){
				seats = seats + (cr.getSeatnum() == null ? 0 : cr.getSeatnum());
			}*/
			//vo.setAllSeats(seats);
			Long mpid = map.get("mpid");
			OpenPlayItem opi = baseDao.getObjectByUkey(OpenPlayItem.class, "mpid", mpid, true);
			if(opi != null){
				vo.setAllSeats(seats + opi.getSeatnum());
				int opiC = vo.getOpiCount() == null ? 0 : vo.getOpiCount();
				int sumGewaprice = vo.getSumGewaprice() == null ? 0 : vo.getSumGewaprice();
				int sellCount = vo.getAllSellSeats() == null ? 0 : vo.getAllSellSeats();
				int gewaSellCount = vo.getGewaSellSeats() == null ? 0 : vo.getGewaSellSeats();
				vo.setAllSellSeats(sellCount + opi.getGsellnum() + opi.getCsellnum() + opi.getLocknum());
				vo.setGewaSellSeats(gewaSellCount + opi.getGsellnum());
				if(opi.getGewaprice() != null){
					opiC++;
					sumGewaprice += opi.getGewaprice();
				}
				vo.setOpiCount(opiC);
				vo.setSumGewaprice(sumGewaprice);
			}
			movieSeatMap.put(map.get("movieId"), vo);
		}
		List<MovieMpiSeatReportVo> list = new ArrayList(movieSeatMap.values());
		return ResultCode.getSuccessReturn(list);
	}

	@Override
	public ResultCode<List<MovieCityBoughtReportVo>> movieCityBoughtReport(Timestamp startTime,Timestamp endTime) {
		if(startTime == null || endTime == null){
			return ResultCode.getFailure(ResultCode.CODE_DATA_ERROR, "参数不能为空！"); 
		}
		if(startTime.after(endTime)){
			return ResultCode.getFailure(ResultCode.CODE_DATA_ERROR, "结束时间不能在开始时间之前"); 
		}
		if(DateUtil.getDiffDay(startTime, endTime) > 1){
			return ResultCode.getFailure(ResultCode.CODE_DATA_ERROR, "时间跨度不能大于一天"); 
		}
		String qry = "select new map(t.movieid as movieId,t.citycode as citycode,count(t.movieid) as boughtCount) from TicketOrder t where t.addtime >= ? and " +
				"t.addtime < ? and t.status = ? group by t.movieid,t.citycode";
		List<Map<String,Object>> tmpMapList = hibernateTemplate.find(qry,startTime,endTime,OrderConstant.STATUS_PAID_SUCCESS);
		List<MovieCityBoughtReportVo> list = new ArrayList<MovieCityBoughtReportVo>();
		if(!VmUtils.isEmptyList(tmpMapList)){
			for(Map<String,Object> map : tmpMapList){
				list.add(new MovieCityBoughtReportVo((Long)map.get("movieId"),map.get("citycode").toString(),Integer.parseInt(map.get("boughtCount").toString())));
			}
		}
		return ResultCode.getSuccessReturn(list);
	}

	@Override
	public ResultCode addCinemaNotify(String num, Long cinemaId, String title,
			String content, String publishUser) {
		if(cinemaId == null || StringUtils.isBlank(num) || StringUtils.isBlank(title) || StringUtils.isBlank(content) || 
				StringUtils.isBlank(publishUser)){
			return ResultCode.getFailure(ResultCode.CODE_DATA_ERROR, "参数不能为空！"); 
		}
		Cinema cinema = baseDao.getObject(Cinema.class, cinemaId);
		if(cinema == null){
			return ResultCode.getFailure(ResultCode.CODE_DATA_ERROR, "影院不存在！"); 
		}
		Map<String,Object> params = new HashMap<String,Object>();
		params.put("num", num);
		List<CinemaProNotify> nList = mongoService.getObjectList(CinemaProNotify.class, params, "addTime", false, 0, 1);
		CinemaProNotify cpn = null;
		if(!VmUtils.isEmptyList(nList)){
			cpn = nList.get(0);
			cpn.setTitle(title);
			cpn.setContent(content);
			cpn.setCinemaId(cinemaId);
			cpn.setCinemaName(cinema.getName());
		}else{
			cpn = new CinemaProNotify(ObjectId.uuid(),num,cinemaId,cinema.getName(),DateUtil.getCurFullTimestampStr(),title,content,publishUser);
		}
		mongoService.saveOrUpdateObject(cpn, MongoData.SYSTEM_ID);
		String mailContent =  publishUser + "发起了" + cinema.getName() + "影院,标题为\"" + title + "\"的公告，需要电商进行处理。";
		mailService.sendEmail(EmailRecord.SENDER_GEWARA, "商家系统公告需处理",mailContent, "operation021@gewara.com");
		return ResultCode.getSuccess("添加成功");
	}

}
