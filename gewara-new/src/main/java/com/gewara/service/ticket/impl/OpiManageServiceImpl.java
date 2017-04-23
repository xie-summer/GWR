package com.gewara.service.ticket.impl;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.gewara.constant.AdminCityContant;
import com.gewara.constant.ApiConstant;
import com.gewara.constant.sys.ErrorCodeConstant;
import com.gewara.constant.sys.JsonDataKey;
import com.gewara.constant.sys.LogTypeConstant;
import com.gewara.constant.sys.SpecialRights;
import com.gewara.constant.ticket.OpiConstant;
import com.gewara.constant.ticket.SeatConstant;
import com.gewara.helper.sys.CachedScript;
import com.gewara.helper.sys.CachedScript.ScriptResult;
import com.gewara.helper.sys.ScriptEngineUtil;
import com.gewara.helper.ticket.AutoSetterHelper;
import com.gewara.json.TempRoomSeat;
import com.gewara.model.acl.User;
import com.gewara.model.common.HisData;
import com.gewara.model.common.JsonData;
import com.gewara.model.movie.Cinema;
import com.gewara.model.movie.CinemaProfile;
import com.gewara.model.movie.CinemaRoom;
import com.gewara.model.movie.Movie;
import com.gewara.model.movie.MoviePlayItem;
import com.gewara.model.movie.MoviePrice;
import com.gewara.model.movie.RoomSeat;
import com.gewara.model.ticket.AutoSetter;
import com.gewara.model.ticket.OpenPlayItem;
import com.gewara.model.ticket.OpenPlayItemExt;
import com.gewara.model.ticket.OpenSeat;
import com.gewara.model.ticket.SellSeat;
import com.gewara.service.JsonDataService;
import com.gewara.service.impl.BaseServiceImpl;
import com.gewara.service.order.OrderQueryService;
import com.gewara.service.ticket.OpenPlayService;
import com.gewara.service.ticket.OpiManageService;
import com.gewara.support.ErrorCode;
import com.gewara.untrans.CacheService;
import com.gewara.untrans.monitor.MonitorService;
import com.gewara.untrans.monitor.RoleTag;
import com.gewara.untrans.ticket.TicketOperationService;
import com.gewara.util.BeanUtil;
import com.gewara.util.BindUtils;
import com.gewara.util.ChangeEntry;
import com.gewara.util.DateUtil;
import com.gewara.util.JsonUtils;
import com.gewara.util.StringUtil;
import com.gewara.util.VmUtils;
import com.gewara.xmlbind.ticket.MpiSeat;

@Service("opiManageService")
public class OpiManageServiceImpl extends BaseServiceImpl implements OpiManageService{
	@Autowired@Qualifier("jsonDataService")
	private JsonDataService jsonDataService;
	@Autowired@Qualifier("openPlayService")
	private OpenPlayService openPlayService;
	@Autowired@Qualifier("ticketOperationService")
	private TicketOperationService ticketOperationService;
	@Autowired@Qualifier("cacheService")
	private CacheService cacheService;
	@Autowired@Qualifier("orderQueryService")
	private OrderQueryService orderQueryService;
	@Autowired@Qualifier("monitorService")
	private MonitorService monitorService;


	@Override
	public ErrorCode changeMpiStatus(Long mpid, String status, Long userid){
		OpenPlayItem opi = baseDao.getObjectByUkey(OpenPlayItem.class, "mpid", mpid, true);
		if(opi == null) return ErrorCodeConstant.NOT_FOUND;
		if(!OpiConstant.isValidEdition(opi.getEdition())) return ErrorCode.getFailure("影片版本错误！！");
		if(OpiConstant.STATUS_BOOK.equals(status)){
			if(opi.getCostprice()==null) return ErrorCode.getFailure("请先设置成本价！");
			if(!opi.hasGewara()){//判断最低价
				if(opi.getLowest()!=null && opi.getCostprice() < opi.getLowest()) 
					return ErrorCode.getFailure("成本价不能小于最低价！");
			}
			
			OpenPlayItemExt opiExt = baseDao.getObject(OpenPlayItemExt.class, opi.getMpid());
			if(opiExt!=null && opiExt.getOpenuser()<=0){//第一次开放
				Timestamp cur = new Timestamp(System.currentTimeMillis());
				opiExt.setOpentime(cur);
				opiExt.setOpenuser(userid);
				MoviePlayItem mpi = baseDao.getObject(MoviePlayItem.class, mpid);
				if(mpi!=null){
					opiExt.setDelayMin((int)DateUtil.getDiffMinu(cur, mpi.getCreatetime()));
				}
			}
		}
		opi.setStatus(status);
		baseDao.saveObject(opi);
		return ErrorCode.getSuccessReturn(opi);
	}
	@Override
	public ErrorCode batchLockSeat(Long mpid, String locktype, String lockreason, String lockline, String lockrank) {
		if(!SeatConstant.STATUS_LOCK_LIST.contains(locktype)) return ErrorCode.getFailure("锁定类型不对！");
		String lines = StringUtils.join(BeanUtil.getIdList(lockline, ","), ",");
		String rows = StringUtils.join(BeanUtil.getIdList(lockrank, ","), ",");
		if(StringUtils.isBlank(lines) && StringUtils.isBlank(lockrank)) 
			return ErrorCode.getFailure("请输入行号或列号！");
		String update = "update OpenSeat set status= ? where mpid = ? and status = ? ";
		if(StringUtils.isNotBlank(lines)) update += "and lineno in (" + lines + ") ";
		if(StringUtils.isNotBlank(rows)) update +=  "and rankno in (" + rows + ") ";
		int locknum = hibernateTemplate.bulkUpdate(update, locktype, mpid, SeatConstant.STATUS_NEW);
		if(locknum > 0) {
			openPlayService.clearOpenSeatCache(mpid);
		}
		return ErrorCode.getSuccessReturn(locknum);
	}
	@Override
	public ErrorCode batchUnLockSeat(Long mpid, String lockline, String lockrank) {
		String lines = StringUtils.join(BeanUtil.getIdList(lockline, ","), ",");
		String rows = StringUtils.join(BeanUtil.getIdList(lockrank, ","), ",");
		if(StringUtils.isBlank(lines)) return ErrorCode.getFailure("请输入行号！");
		String locktypes="'" + SeatConstant.STATUS_LOCKB + "','" + SeatConstant.STATUS_LOCKC + "','" + SeatConstant.STATUS_LOCKD + "'";
		String update = "update OpenSeat set status= ? where mpid = ? and status in (" + locktypes + ") ";
		if(StringUtils.isNotBlank(lines)) update += "and lineno in (" + lines + ") ";
		if(StringUtils.isNotBlank(rows)) update +=  "and rankno in (" + rows + ") ";
		int locknum = hibernateTemplate.bulkUpdate(update, SeatConstant.STATUS_NEW, mpid);
		openPlayService.clearOpenSeatCache(mpid);
		return ErrorCode.getSuccessReturn(locknum);
	}
	@Override
	public ErrorCode<OpenSeat> releaseSeat(Long mpid, Long seatId) {
		OpenSeat oseat = baseDao.getObject(OpenSeat.class, seatId);
		if(!oseat.isLocked()) return ErrorCode.getFailure("此座位没锁定！");
		oseat.setStatus(SeatConstant.STATUS_NEW);
		baseDao.saveObject(oseat);
		openPlayService.clearOpenSeatCache(mpid);
		return ErrorCode.getSuccessReturn(oseat);
	}
	@Override
	public ErrorCode<String> addPriceSeat(Long mpid, String seattype, Long seatid) {
		if(StringUtils.isBlank(seattype)||seatid == null) 
			return ErrorCode.getFailure("参数有错误，类型和座位不能为空！");
		if(StringUtils.isNotBlank(seattype) && (seattype.length()!=1 || "BCDE".indexOf(seattype) < 0))
			return ErrorCode.getFailure("座位类型不正确！");
		OpenSeat oseat = baseDao.getObject(OpenSeat.class, seatid);
		SellSeat sellSeat = baseDao.getObject(SellSeat.class, seatid);
		if(sellSeat==null || sellSeat.isAvailable(new Timestamp(System.currentTimeMillis())) || oseat.isLocked()){
			String msg = oseat.getSeatLabel() + ":" + oseat.getSeattype() + "--->" + seattype;
			oseat.setSeattype(seattype);
			baseDao.saveObject(oseat);
			openPlayService.clearOpenSeatCache(mpid);
			return ErrorCode.getSuccessReturn(msg);
		}
		return ErrorCode.getFailure("座位正在出售或已售出，不能更改价格！");
	}
	@Override
	public ErrorCode<String> removePriceSeat(Long mpid, Long seatid) {
		OpenSeat oseat = baseDao.getObject(OpenSeat.class, seatid);
		SellSeat sellSeat = baseDao.getObject(SellSeat.class, seatid);
		if(sellSeat==null || sellSeat.isAvailable(new Timestamp(System.currentTimeMillis())) || oseat.isLocked()){
			String msg = oseat.getSeatLabel() + ":" + oseat.getSeattype() + "--->" + SeatConstant.SEAT_TYPE_A;
			oseat.setSeattype(SeatConstant.SEAT_TYPE_A);
			baseDao.saveObject(oseat);
			openPlayService.clearOpenSeatCache(mpid);
			return ErrorCode.getSuccessReturn(msg);
		}
		return ErrorCode.getFailure("座位正在出售或已售出，不能更改价格！");
	}
	@Override
	public ErrorCode<OpenPlayItem> openMpi(MoviePlayItem mpi, Long userid, String opentype, TempRoomSeat tempRoomSeat){
		if(userid==null) return ErrorCodeConstant.NOT_LOGIN;
		ErrorCode<OpenPlayItem> result = openMpiStep1(mpi, opentype, userid);
		if(result.isSuccess()){
			baseDao.saveObject(result.getRetval());
			batchInsertOpenSeat(result.getRetval(), tempRoomSeat);
		}
		return result;
	}
	@Override
	public ErrorCode<OpenPlayItem> openWdMpi(MoviePlayItem mpi, Long userid) {
		if(userid==null) return ErrorCodeConstant.NOT_LOGIN;
		ErrorCode<OpenPlayItem> result = openMpiStep1(mpi, OpiConstant.OPEN_WD, userid);
		if(result.isSuccess()){
			OpenPlayItem opi = result.getRetval();
			opi.setPartner(OpiConstant.PARTNER_CLOSE);
			baseDao.saveObject(result.getRetval());
		}
		return result;
	}
	
	/**
	 * 
	 * 开放东票场次数据
	 * @param mpi
	 * @param userid
	 * @return
	 */
	@Override
	public ErrorCode<OpenPlayItem> openPnxMpi(MoviePlayItem mpi, Long userid, List<MpiSeat> mpiSeatList){
		if(CollectionUtils.isEmpty(mpiSeatList)) {
			return ErrorCode.getFailure(ApiConstant.CODE_SIGN_ERROR, "座位数据为空！");
		}
		
		ErrorCode<OpenPlayItem> code = openMpiStep1(mpi, mpi.getOpentype(), userid);
		if(!code.isSuccess()) {
			return code;
		}
		OpenPlayItem opi = code.getRetval();
		List<OpenSeat> oseatList = new ArrayList<OpenSeat>();
		for (MpiSeat mpiSeat : mpiSeatList) {
			OpenSeat seat = createOpenSeat(mpiSeat, mpi.getId());
			oseatList.add(seat);
		}
		opi.setSeatnum(oseatList.size());
		//成本价与卖价一致
		opi.setCostprice(opi.getGewaprice());
		baseDao.saveObject(opi);
		baseDao.saveObjectList(oseatList);
		return ErrorCode.getSuccessReturn(opi);
	}
	
	@Override
	public ErrorCode<OpenPlayItem> refreshOpiSeat(Long mpid, Long userid, List<MpiSeat> mpiSeatList){
		OpenPlayItem opi = baseDao.getObjectByUkey(OpenPlayItem.class, "mpid", mpid, true);
		if(opi.hasGewara()) return ErrorCode.getFailure(ApiConstant.CODE_SIGN_ERROR, "非第三方场次！");
		if(CollectionUtils.isEmpty(mpiSeatList)) {
			return ErrorCode.getFailure(ApiConstant.CODE_SIGN_ERROR, "座位数据为空！");
		}
		List<OpenSeat> oseatList = new ArrayList<OpenSeat>();
		for (MpiSeat mpiSeat : mpiSeatList) {
			OpenSeat seat = createOpenSeat(mpiSeat, opi.getMpid());
			oseatList.add(seat);
		}
		opi.setSeatnum(oseatList.size());
		List<OpenSeat> oldSeatList = baseDao.getObjectListByField(OpenSeat.class, "mpid", opi.getMpid());
		baseDao.removeObjectList(oldSeatList);
		hibernateTemplate.flush();
		baseDao.saveObjectList(oseatList);
		hibernateTemplate.flush();
		baseDao.saveObject(opi);
		dbLogger.warn(userid + "重设场次座位：" +opi.getMpid() + opi.getOpentype() + "," + OpiConstant.getFullDesc(opi));
		return ErrorCode.getSuccessReturn(opi);
	}
	
	private ErrorCode<OpenPlayItem> openMpiStep1(MoviePlayItem mpi, String opentype, Long userid){
		//1)一般检验
		if(mpi==null || isExpiredMpi(mpi)) return ErrorCode.getFailure("本场次已经过时，不能开放！");
		if(mpi.getRoomid() == null) return ErrorCode.getFailure("本场次无放映厅，不能开放！");
		if(!OpiConstant.isValidEdition(mpi.getEdition())){
			 return ErrorCode.getFailure("版本有错误：" + mpi.getEdition());
		}
		Integer seatcount = openPlayService.getSeatCountByRoomId(mpi.getRoomid());
		if(seatcount==0)  return ErrorCode.getFailure("放映厅座位未安排，不能开放！");
		if(mpi.getGewaprice()==null) return ErrorCode.getFailure("请先输入统一卖价！");
		if(StringUtils.equals(mpi.getOpenStatus(), OpiConstant.MPI_OPENSTATUS_DISABLED)) return ErrorCode.getFailure("该场次为不可开放场次！");
		OpenPlayItem opi = baseDao.getObjectByUkey(OpenPlayItem.class, "mpid", mpi.getId(), true);
		if(opi != null) return ErrorCode.getFailure("本场次已经开放过！");
		updateOriginInfo(mpi);
		//2)火凤凰对接检验
		CinemaRoom room = baseDao.getObject(CinemaRoom.class, mpi.getRoomid());
		Cinema cinema = baseDao.getObject(Cinema.class, mpi.getCinemaid());
		CinemaProfile profile = baseDao.getObject(CinemaProfile.class, mpi.getCinemaid());
		if(profile==null) return ErrorCode.getFailure("基本信息没设置！"); 
		//3)开放场次
		opi = new OpenPlayItem(mpi, opentype, profile.getOpentime(), profile.getClosetime(), profile.getCminute());
		opi.setCinemaname(cinema.getRealBriefname());
		if(profile.getOpenDay() != null && StringUtils.isNotBlank(profile.getOpenDayTime())){
			opi.setOpentime(DateUtil.parseTimestamp(DateUtil.formatDate(
					DateUtil.addDay(opi.getPlaytime(), -profile.getOpenDay())) + " " + profile.getOpenDayTime(), "yyyy-MM-dd HHmm"));
		}
		Movie movie = baseDao.getObject(Movie.class, mpi.getMovieid());
		opi.setMoviename(movie.getRealBriefname());
		opi.setEdition(mpi.getEdition());
		opi.setPrice(mpi.getPrice());
		opi.setLanguage(mpi.getLanguage());
		if(OpiConstant.OPEN_LOWEST_IS_COST.contains(opentype)){
			opi.setCostprice(mpi.getLowest());		//最低价就是成本价	
		}
		Object topicId = JsonUtils.readJsonToMap(room.getOtherinfo()).get("topicId");
		if(topicId != null && StringUtils.isNotBlank((String)topicId)){
			opi.setTopicid(Long.parseLong((String)topicId));
		}else{
			opi.setTopicid(profile.getTopicid());
		}
		opi.setFee(profile.getFee());
		//3)开放座位
		opi.setSeatnum(room.getSeatnum());
		opi.setLocknum(0);	//初始只设置0
		opi.setGsellnum(0);
		opi.setCsellnum(0);
		opi.setGivepoint(0);
		Map<String, String> map = VmUtils.readJsonToMap(opi.getOtherinfo());
		map.put(OpiConstant.ISREFUND, profile.getIsRefund());
		if(StringUtils.isNotBlank(mpi.getRemark())){
			map.put(OpiConstant.SMPNO, mpi.getRemark());
		}
		opi.setOtherinfo(JsonUtils.writeMapToJson(map));
		opi.setAsellnum(room.getAllowsellnum());
		opi.setUpdatetime(new Timestamp(System.currentTimeMillis()));
		OpenPlayItemExt opiExt = new OpenPlayItemExt(userid, opi);
		baseDao.saveObject(opiExt);
		dbLogger.warn(userid + "开放场次：" +opi.getMpid() + opi.getOpentype() + "," + OpiConstant.getFullDesc(opi));
		mpi.setOpenStatus(OpiConstant.MPI_OPENSTATUS_OPEN);
		baseDao.saveObject(mpi);
		return ErrorCode.getSuccessReturn(opi);
	}
	private boolean isExpiredMpi(MoviePlayItem mpi){
		Date date = DateUtil.getCurDate();
		String time = DateUtil.format(new Date(), "HH:mm");
		if(mpi.getPlaydate().after(date)) return false;
		if(mpi.getPlaydate().before(date)) return true;
		if(time.compareTo(mpi.getPlaytime()) > 0) return true;
		return false;
	}
	@Override
	public ErrorCode<OpenPlayItem> autoOpenMpi(MoviePlayItem mpi, AutoSetter setter, TempRoomSeat tempRoomSeat, Long userid) {
		String msg="自动设置:";
		CachedScript limitCs = null;
		if(StringUtils.isNotBlank(setter.getLimitScript())){
			limitCs = ScriptEngineUtil.buildCachedScript(setter.getLimitScript(), true);
		}
		if(!AutoSetterHelper.isMatch(setter, mpi,null,limitCs)) {
			return ErrorCode.getFailure("不匹配的设置器:mpid=" + mpi.getId() + ", " + setter.getName());
		}
		int gewaprice = setter.getGewaprice();
		int costprice = setter.getCostprice();
		ScriptResult<Object> setResult = null;
		if(StringUtils.isNotBlank(setter.getPriceScript())){
			CachedScript cs = ScriptEngineUtil.buildCachedScript(setter.getPriceScript(), true);
			Map<String, Object> context = new HashMap<String, Object>();
			context.put("mpi", mpi);
			setResult = cs.run(context);
			if(setResult.hasError()){
				return ErrorCode.getFailure("设置器脚本错误:mpid=" + mpi.getId() + ", " + setter.getName());
			}
			Object gPrice = setResult.getAttribute("gewaprice");
			if(gPrice != null){
				gewaprice = ((Number)gPrice).intValue();
			}
			Object cPrice = setResult.getAttribute("costprice");
			if(cPrice != null){
				costprice = ((Number)cPrice).intValue();
			}
		}
		
		if(gewaprice > mpi.getPrice()) {
			if(costprice > mpi.getPrice()){ 
				return ErrorCode.getFailure("设置的价格" + gewaprice + "大于影院卖价" + mpi.getPrice());
			}
			msg += "[卖价大于影院卖价，以影院卖价为准]";
			gewaprice = mpi.getPrice();
		}
		CinemaRoom room = baseDao.getObject(CinemaRoom.class, mpi.getRoomid());
		if(room.getAllowsellnum()<=0){
			return ErrorCode.getFailure("影厅可售座位为0，不开放！");
		}
		ErrorCode result = checkCostPrice(mpi.getMovieid(), mpi.getCitycode(), mpi.getEdition(), mpi.getPrice(), mpi.getLowest(), costprice,Timestamp.valueOf(mpi.getFullPlaytime()));
		if(!result.isSuccess()) return result;
		mpi.setGewaprice(gewaprice);

		ErrorCode<OpenPlayItem> openResult = openMpiStep1(mpi, mpi.getOpentype(), userid);
		if(openResult.isSuccess()){
			OpenPlayItem opi = openResult.getRetval();
			if(!OpiConstant.OPEN_LOWEST_IS_COST.contains(mpi.getOpentype())){
				opi.setCostprice(costprice);		//未设置成本价才设置	
			}
			opi.setRemark(setter.getRemark());
			Map info = JsonUtils.readJsonToMap(opi.getOtherinfo());
			info.put(OpiConstant.AUTO_OPEN_INFO, msg);
			info.put(OpiConstant.AUTO_OPEN_INFO_STATUS, setter.getStatus());
			if(StringUtils.isNotBlank(mpi.getRemark())){
				info.put(OpiConstant.SMPNO, mpi.getRemark());
			}
			opi.setOtherinfo(JsonUtils.writeMapToJson(info));
			opi.setElecard(setter.getElecard());
			if(AutoSetter.STATUS_OPEN_A.equals(setter.getStatus()) && setter.opiStatus){
				opi.setStatus(OpiConstant.STATUS_BOOK);
			}
			baseDao.saveObject(opi);
			batchInsertOpenSeat(opi, tempRoomSeat);
		}
		return openResult;
	}
	@Override
	public ErrorCode batchInsertOpenSeat(OpenPlayItem opi, TempRoomSeat tempRoomSeat){
		Integer price = opi.getGewaprice();
		String insert = "insert into WEBDATA.OPEN_SEAT (LINENO,RANKNO,SEATLINE,SEATRANK,LOVEIND,SEATTYPE,RECORDID,MPID,PRICE,STATUS)" +
				"SELECT LINENO,RANKNO,SEATLINE,SEATRANK,LOVEIND,'A',WEBDATA.SEAT_SEQUENCE.NEXTVAL," + opi.getMpid() + "," + price + ",";
		if(tempRoomSeat != null && StringUtils.isNotBlank(tempRoomSeat.getSeatbody())){
			String tmp = "('" + StringUtils.join(StringUtils.split(tempRoomSeat.getSeatbody(), ","), "','") + "')";
			insert += "(CASE WHEN SEATLINE||':'||SEATRANK in " + tmp + " THEN 'B' ELSE 'A' END) AS STATUS FROM WEBDATA.ROOMSEAT WHERE ROOMID=" + opi.getRoomid();
		}else{
			insert += "'A' AS STATUS FROM WEBDATA.ROOMSEAT WHERE ROOMID=" + opi.getRoomid();
		}
		int seatnum = jdbcTemplate.update(insert);
		opi.setSeatnum(seatnum);
		if(tempRoomSeat!=null){
			Map map = VmUtils.readJsonToMap(opi.getOtherinfo());
			map.put("tempRoomSeatName", tempRoomSeat.getTmpname());
			opi.setOtherinfo(JsonUtils.writeMapToJson(map));
		}
		baseDao.saveObject(opi);
		return ErrorCode.SUCCESS;
	}
	@Override
	public ErrorCode<OpenPlayItem> updateGewaPrice(Long mpid, Integer gewaprice, User user){
		OpenPlayItem opi = baseDao.getObjectByUkey(OpenPlayItem.class, "mpid", mpid, true);
		if(opi!=null){
			if(!hasRights(user, opi)){
				return ErrorCode.getFailure("无权设置直连场次！");
			}
			if(gewaprice == null) return ErrorCode.getFailure("格瓦拉价不存在！");
			if(opi.getCostprice()==null) return ErrorCode.getFailure("请先设置成本价！");
			if(opi.getPrice() == null) return ErrorCode.getFailure("请先设置影院价！");
			if(gewaprice > opi.getPrice()) return ErrorCode.getFailure("不能大于影院卖价！");
			if(opi.isBooking()){
				return ErrorCode.getFailure("只有在不接受预定的状态下才能修改！");
			}
			OpenPlayItemExt opiExt = baseDao.getObject(OpenPlayItemExt.class, opi.getMpid());
			if(opiExt!=null && opiExt.getOpenuser()>0 && !opiExt.getOpenuser().equals(user.getId()) 
					&& !user.isRole(SpecialRights.MODIFY_OTHER_OPI)){
				return ErrorCode.getFailure("只有开放场次的人[" + opiExt.getOpenuser() + "]才能设置！");
			}
			ChangeEntry changeEntry = new ChangeEntry(opi);
			opi.setGewaprice(gewaprice);
			baseDao.saveObject(opi);
			if(opi.getCostprice() > opi.getGewaprice() || opi.getCostprice() + 12 < opi.getGewaprice()) {
				try {
					String msg = "成本价：" + opi.getCostprice() + "卖价: " + opi.getGewaprice() + ", 场次ID：" + opi.getMpid() + ",城市：" + AdminCityContant.getCitycode2CitynameMap().get(opi.getCitycode()) +",影院：" + opi.getCinemaname() + ",请核对场次信息";
					String title = "场次价格有问题";
					String content = msg;
					monitorService.saveSysWarn(OpenPlayItem.class, opi.getMpid(), title, content, RoleTag.dingpiao);
				} catch (Exception e) {
				}
			}
			monitorService.saveChangeLog(user.getId(), OpenPlayItem.class, mpid, changeEntry.getChangeMap(opi));
		}
		MoviePlayItem mpi = baseDao.getObject(MoviePlayItem.class, mpid);
		if(mpi == null) return ErrorCode.getFailure("排片信息不存在！");
		mpi.setGewaprice(gewaprice);
		baseDao.saveObject(mpi);
		return ErrorCode.getSuccessReturn(opi);
	}

	@Override
	public ErrorCode<String> updateElecard(Long mpid, String cardtype, User user){
		OpenPlayItem opi = baseDao.getObjectByUkey(OpenPlayItem.class, "mpid", mpid, true);
		if(opi!=null){
			if(!hasRights(user, opi)){
				return ErrorCode.getFailure("无权设置直连场次！");
			}
			String msg = opi.getElecard() + "--->" + cardtype;
			opi.setElecard(cardtype);
			baseDao.saveObject(opi);
			return ErrorCode.getSuccessReturn(msg);
		}
		return ErrorCode.getFailure("场次不存在！");
	}

	private boolean hasRights(User user, OpenPlayItem opi){
		return opi.hasGewara() || user.isRole(SpecialRights.ONLINEMPI);
	}
	@Override
	public ErrorCode<OpenPlayItem> updateCostPrice(Long mpid, Integer costprice, User user){
		if(user==null) return ErrorCodeConstant.NOT_LOGIN;
		OpenPlayItem opi = baseDao.getObjectByUkey(OpenPlayItem.class, "mpid", mpid, true);
		if(!hasRights(user, opi)){
			return ErrorCode.getFailure("无权设置直连场次！");
		}
		OpenPlayItemExt opiExt = baseDao.getObject(OpenPlayItemExt.class, opi.getMpid());
		if(opiExt!=null && opiExt.getOpenuser()>0 && !opiExt.getOpenuser().equals(user.getId()) 
				&& !user.isRole(SpecialRights.MODIFY_OTHER_OPI)){
			return ErrorCode.getFailure("只有开放场次的人[" + opiExt.getOpenuser() + "]才能设置！");
		}
		if(costprice==null) return ErrorCode.getFailure("成本价必须设置！");
		if(opi.isBooking()){
			return ErrorCode.getFailure("只有在不接受预定的状态下才能修改！");
		}
		if(!opi.hasGewara()){
			if(OpiConstant.OPEN_LOWEST_IS_COST.contains(opi.getOpentype())){
				return ErrorCode.getFailure("此影片成本价由影院设置，不能更改！");
			}
			
			ErrorCode result = checkCostPrice(opi.getMovieid(), opi.getCitycode(), opi.getEdition(), opi.getPrice(), opi.getLowest(), costprice,opi.getPlaytime());
			if(!result.isSuccess()) return result;

			if(opi.hasOpentype(OpiConstant.OPEN_MTX)){
				ErrorCode code = ticketOperationService.updateCostPrice(opi.getSeqNo(), costprice);
				if(!code.isSuccess()) return ErrorCode.getFailure(code.getMsg());
			}
		}
		ChangeEntry changeEntry = new ChangeEntry(opi);
		opi.setCostprice(costprice);
		if (opi.getCostprice() > opi.getGewaprice() || opi.getCostprice() + 12 < opi.getGewaprice()) {
			try {
				String msg = "成本价：" + opi.getCostprice() + "卖价: " + opi.getGewaprice() + ", 场次ID：" + opi.getMpid() + ",城市：" + AdminCityContant.getCitycode2CitynameMap().get(opi.getCitycode()) +",影院：" + opi.getCinemaname() + ",请核对场次信息";
				String title = "场次价格有问题";
				String content = msg;
				monitorService.saveSysWarn(OpenPlayItem.class, opi.getMpid(), title, content, RoleTag.dingpiao);
			} catch (Exception e) {
			}
		}
		monitorService.saveChangeLog(user.getId(), OpenPlayItem.class, mpid, changeEntry.getChangeMap(opi));
		baseDao.saveObject(opi);
		return ErrorCode.SUCCESS;
	}
	private ErrorCode checkCostPrice(Long movieid, String citycode, String edition, int price, Integer lowest, Integer costprice,Timestamp playTime){
		MoviePrice mp = openPlayService.getMoviePrice(movieid, citycode);
		if(mp!=null){
			ErrorCode<Integer> code = OpiConstant.getLowerPrice(edition, mp,playTime);
			if(!code.isSuccess()) return ErrorCode.getFailure(code.getMsg());
			int lowerPrice = code.getRetval();
			if(lowerPrice > costprice) {
				return ErrorCode.getFailure("成本价" + costprice + "不能低于影片最低卖价" + lowerPrice);
			}
		}else{
			return ErrorCode.getFailure("影片" + movieid + "未设置最低价格!");
		}
		if(costprice>price*5/2) return ErrorCode.getFailure("成本价不能大于影院卖价的2.5倍");
		if(lowest!=null && costprice < lowest) return ErrorCode.getFailure("不能小于最低票价！");
		return ErrorCode.SUCCESS;
	}
	@Override
	public ErrorCode<OpenPlayItem> updateOpenPlayItem(Long opid){
		OpenPlayItem opi = baseDao.getObject(OpenPlayItem.class, opid);
		MoviePlayItem mpi = baseDao.getObject(MoviePlayItem.class, opi.getMpid());
		if(mpi==null){
			opi.setStatus(OpiConstant.STATUS_DISCARD);
			baseDao.saveObject(opi);
			dbLogger.warn("自动更改场次(ID=" + opi.getMpid() + ")：排片已删除, 自动关闭此场次：" + OpiConstant.getFullDesc(opi));
			return ErrorCode.getFailure("自动更改场次(ID=" + opi.getMpid() + ")：排片已删除, 自动关闭此场次：" + OpiConstant.getFullDesc(opi));
		}
		String diff = OpiConstant.getDifferent(opi, mpi);
		String s = "";
		if(StringUtils.isNotBlank(diff)){
			s = "场次自动更改，" + opi.getMoviename() + ", " + opi.getCinemaname() + ", mpid=" + opi.getMpid() + "：" + diff;
			Long timeDiff = opi.getPlaytime().getTime() - Timestamp.valueOf(mpi.getFullPlaytime()).getTime();
			if(!opi.getMovieid().equals(mpi.getMovieid())){//更换影片
				Movie movie = baseDao.getObject(Movie.class, mpi.getMovieid());
				opi.setMoviename(movie.getRealBriefname());
				opi.setStatus(OpiConstant.STATUS_DISCARD);
				s += "，影片更改，自动关闭";
			}
			if(Math.abs(timeDiff) > DateUtil.m_minute/*2012-12-18更改逻辑 * 25*/){
				//TODO:火凤凰改时间退票，满天星？？
				opi.setStatus(OpiConstant.STATUS_DISCARD);
				s += "，时间更改，自动关闭，注意影院是否退票！";
	 		}
			if(opi.getPrice()==null || mpi.getPrice()==null){
				opi.setStatus(OpiConstant.STATUS_DISCARD);
				s += "，影院卖价未设置，自动关闭";
			}
			if(opi.getPrice()==null){
				s += "，场次没设置价格！！！！";
			}else if(!opi.getPrice().equals(mpi.getPrice())){
				opi.setStatus(OpiConstant.STATUS_DISCARD);
				s += "，价格变更，自动关闭";
	 		}
			if(OpiConstant.OPEN_LOWEST_IS_COST.contains(opi.getOpentype())){
				//万达价格更改，场次暂停购票
				if(mpi.getLowest()!=null && opi.getLowest()!=null && !mpi.getLowest().equals(opi.getLowest())){
					s += "，影院更改结算价:" + opi.getLowest() + "-->" + mpi.getLowest() + "，自动关闭，请更新排片！";
					opi.setCostprice(mpi.getLowest());
					opi.setStatus(OpiConstant.STATUS_DISCARD);
				}
			}
			if(!mpi.getRoomid().equals(opi.getRoomid())){
				opi.setStatus(OpiConstant.STATUS_DISCARD);
				s += "，“！影厅！”变更，自动关闭，联系系统管理员！！！！！！！！！！！！";
			}
		}
		if(StringUtils.equals(mpi.getOpenStatus(), OpiConstant.MPI_OPENSTATUS_PAST)){
			opi.setStatus(OpiConstant.STATUS_PAST);
		}
		opi.copyFrom(mpi);
		baseDao.saveObject(opi);
		return ErrorCode.getFullErrorCode(ApiConstant.CODE_SUCCESS, s, opi);
	}
	@Override
	public ErrorCode batchAddPriceSeat(Long mpid, String seattype, String rows, String ranks) {
		if(StringUtils.isBlank(seattype) || !StringUtils.contains("BCDE", seattype))
			return ErrorCode.getFailure("座位类型不正确！");
		rows = StringUtils.join(BeanUtil.getIdList(rows, ","), ",");
		ranks = StringUtils.join(BeanUtil.getIdList(ranks, ","), ",");
		if(StringUtils.isBlank(rows) && StringUtils.isBlank(ranks)) 
			return ErrorCode.getFailure("请输入行号或列号！");
		String query = "from OpenSeat where mpid = ? and seattype=? ";
		if(StringUtils.isNotBlank(rows)) query += "and lineno in (" + rows + ") ";
		if(StringUtils.isNotBlank(ranks)) query +=  "and rankno in (" + ranks + ") ";
		List<OpenSeat> oseatList = hibernateTemplate.find(query, mpid, SeatConstant.SEAT_TYPE_A);
		int num = 0;
		Timestamp cur = new Timestamp(System.currentTimeMillis());
		for(OpenSeat oseat: oseatList){
			SellSeat sellSeat = baseDao.getObject(SellSeat.class, oseat.getId());
			if(sellSeat==null || sellSeat.isAvailable(cur) || oseat.isLocked()){
				oseat.setSeattype(seattype);
				baseDao.saveObject(oseat);
				num++;
			}
		}
		if(num>0) openPlayService.clearOpenSeatCache(mpid);
		return ErrorCode.getSuccess("共更新" + num + "个:" + SeatConstant.SEAT_TYPE_A + "--->" + seattype + ",rows=" + rows + ",ranks=" + ranks);
	}
	@Override
	public ErrorCode<OpenPlayItem> discardOpiByMpid(Long mpid, Long userid){
		Integer count = orderQueryService.getTicketOrderCountByMpid(mpid);
		if (count > 0) return ErrorCode.getFailure("有订单，不能废弃！");
		OpenPlayItem opi = baseDao.getObjectByUkey(OpenPlayItem.class, "mpid", mpid, true);
		
		if(opi != null) {
			openPlayService.clearOpenSeatCache(mpid);
			if(opi.getStatus().equals(OpiConstant.STATUS_BOOK)) return ErrorCode.getFailure("只有未开放的场次才能废弃！");
			String delOpenSeat = "delete OpenSeat where mpid = ? ";
			hibernateTemplate.bulkUpdate(delOpenSeat, mpid);
			OpenPlayItemExt ext = baseDao.getObject(OpenPlayItemExt.class, opi.getMpid());
			if(ext!=null) baseDao.removeObject(ext);
			jsonDataService.addRemoveObject(OpenPlayItem.class, opi.getId());
			baseDao.removeObject(opi);
			
			openPlayService.clearOpenSeatCache(mpid);
		}
		cacheService.cleanUkey(OpenPlayItem.class, "mpid", mpid);
		MoviePlayItem mpi = baseDao.getObject(MoviePlayItem.class, mpid);
		if(mpi != null){
			mpi.setOpenStatus(OpiConstant.MPI_OPENSTATUS_INIT);
			baseDao.updateObject(mpi);
		}
		dbLogger.warn("废弃场次[" + userid + "]:" + mpid);
		return ErrorCode.getSuccessReturn(opi);
	}
	@Override
	public ErrorCode updateSeatInitStatus(Long seatid, String initstatus) {
		if(!RoomSeat.isValidStatus(initstatus)) return ErrorCode.getFailure("状态不合法！");
		RoomSeat seat = baseDao.getObject(RoomSeat.class, seatid);
		seat.setInitstatus(initstatus);
		return ErrorCode.SUCCESS;
	}
	@Override
	public int verifyOpiSeatLock(Long mpid){
		String query1 = "from OpenSeat o where o.mpid = ? and o.status in ('" + 
				StringUtils.join(SeatConstant.STATUS_LOCK_LIST, "','") + 
				"') and exists (select id from SellSeat s where s.status= ? and s.id = o.id) ";
		List<OpenSeat> oseatList = hibernateTemplate.find(query1, mpid, SeatConstant.STATUS_SOLD);
		if(oseatList.size() > 0){
			OpenPlayItem opi = baseDao.getObjectByUkey(OpenPlayItem.class, "mpid", mpid, true);
			opi.setLocknum(opi.getLocknum() - oseatList.size());
			baseDao.updateObject(opi);
			for(OpenSeat seat: oseatList){
				seat.setStatus(SeatConstant.STATUS_NEW);
				baseDao.updateObject(seat);
			}
		}
		return oseatList.size();
	}

	@Override
	public void updateOpiStats(Long opid, List<String> hfhLockList, boolean isFinished){
		OpenPlayItem opi = baseDao.getObject(OpenPlayItem.class, opid);
		Timestamp cur = new Timestamp(System.currentTimeMillis());
		if(DateUtil.addHour(opi.getPlaytime(), 48).before(cur)) {
			return;//过期48小时的不更新
		}
		final String query1 = "select seatline||':'||seatrank from OpenSeat o where o.mpid = ? and o.status in ('" + StringUtils.join(SeatConstant.STATUS_LOCK_LIST, "','") + "') ";
		List<String> gewalockList = hibernateTemplate.find(query1, opi.getMpid());
		String sqlqry = "SELECT sum(o.quantity) from WEBDATA.ticket_order o where o.status='paid_success' and o.order_type='ticket' and o.relatedid=?";
		Integer gsellnum = 0;
		try{
			gsellnum = jdbcTemplate.queryForInt(sqlqry, opi.getMpid());
		}catch(Exception e){
			dbLogger.warn(StringUtil.getExceptionTrace(e));
		}
		gewalockList.removeAll(hfhLockList);
		Integer csellnum = hfhLockList.isEmpty()? 0: hfhLockList.size() - gsellnum - 1;
		if(csellnum<0){
			csellnum = 0;
		}
		boolean unchange = opi.getGsellnum().equals(gsellnum) && opi.getLocknum().equals(gewalockList.size()) && opi.getCsellnum().equals(csellnum);
		if(!unchange || isFinished){
			opi.setGsellnum(gsellnum);
			opi.setLocknum(gewalockList.size());
			opi.setCsellnum(csellnum);
			opi.setUpdatetime(new Timestamp(System.currentTimeMillis()));
			if(isFinished) {
				opi.setOtherinfo(JsonUtils.addJsonKeyValue(opi.getOtherinfo(), OpiConstant.STATISTICS, "true"));
			}
			baseDao.saveObject(opi);
		}
	}
	@Override
	public ErrorCode<Long> updateSeatLoveInd(Long seatid, String loveInd) {
		RoomSeat seat = baseDao.getObject(RoomSeat.class, seatid);
		RoomSeat another = null;
		if("0".equals(seat.getLoveInd()) || "1".equals(seat.getLoveInd())){
			another = getAnotherLoveSeat(seat, 1); 
		}else{
			another = getAnotherLoveSeat(seat, -1);
		}
		if("0".equals(loveInd)){//清除
			if(!"0".equals(seat.getLoveInd())){//原来设置过了
				seat.setLoveInd("0");
				if(another!=null) {
					another.setLoveInd("0");
					baseDao.saveObject(another);
				}
				baseDao.saveObject(seat);
				return ErrorCode.getSuccessReturn(another.getId());
			}
		}else{
			if("0".equals(seat.getLoveInd())){//没设置
				if(another == null || !"0".equals(another.getLoveInd())) return ErrorCode.getFailure("旁边座位状态错误！");
				seat.setLoveInd("1");
				another.setLoveInd("2");
				baseDao.saveObjectList(seat, another);
				return ErrorCode.getSuccessReturn(another.getId());
			}
		}
		return ErrorCode.getFailure("状态未改变");
	}
	private RoomSeat getAnotherLoveSeat(RoomSeat seat, Integer add/*-1: left, +1:right*/){
		String query = "from RoomSeat t where t.roomid=? and t.lineno=? and t.rankno=? ";
		List<RoomSeat> seatList = hibernateTemplate.find(query, seat.getRoomid(), seat.getLineno(), seat.getRankno()+add);
		if(seatList.size() > 0) return seatList.get(0);
		return null;
	}
	@Override
	public ErrorCode batchRemovePriceSeat(Long mpid, String seattype, String rows, String ranks) {
		if(StringUtils.isBlank(seattype) || !StringUtils.contains("BCDE", seattype))
			return ErrorCode.getFailure("座位类型不正确！");
		rows = StringUtils.join(BeanUtil.getIdList(rows, ","), ",");
		ranks = StringUtils.join(BeanUtil.getIdList(ranks, ","), ",");
		if(StringUtils.isBlank(rows) && StringUtils.isBlank(ranks)) 
			return ErrorCode.getFailure("请输入行号或列号！");
		String query = "from OpenSeat where mpid = ? and seattype = ? ";
		if(StringUtils.isNotBlank(rows)) query += "and lineno in (" + rows + ") ";
		if(StringUtils.isNotBlank(ranks)) query +=  "and rankno in (" + ranks + ") ";
		List<OpenSeat> oseatList = hibernateTemplate.find(query, mpid, seattype);
		int num = 0;
		Timestamp cur = new Timestamp(System.currentTimeMillis());
		for(OpenSeat oseat: oseatList){
			SellSeat sellSeat = baseDao.getObject(SellSeat.class, oseat.getId());
			if(sellSeat==null || sellSeat.isAvailable(cur) || oseat.isLocked()){
				oseat.setSeattype(SeatConstant.SEAT_TYPE_A);
				baseDao.saveObject(oseat);
			}
		}
		if(num>0) openPlayService.clearOpenSeatCache(mpid);
		return ErrorCode.getSuccess("共更新" + num + "个:" + seattype +  "--->" + SeatConstant.SEAT_TYPE_A + ",rows=" + rows + ",ranks=" + ranks);
	}
	@Override
	public void updateOriginInfo(MoviePlayItem mpi) {
		if(mpi.isHfh()){
			JsonData jd = baseDao.getObject(JsonData.class, JsonDataKey.KEY_EDITION_LANGUAGE + mpi.getId());
			Map<String, String> changeMap = null;
			if(jd != null) {
				changeMap = VmUtils.readJsonToMap(jd.getData());
			}else{
				changeMap = new HashMap<String, String>();
			}
			String hedition = mpi.getEdition();
			String hlang = mpi.getLanguage();
			if(StringUtils.isBlank(changeMap.get("edition"))) {
				changeMap.put("edition", hedition);
			}
			if(StringUtils.isBlank(changeMap.get("language"))) {
				changeMap.put("language", hlang);
			}
			if(StringUtils.isBlank(changeMap.get("price"))) {
				changeMap.put("price", ""+mpi.getPrice());
			}
			if(changeMap.isEmpty()) return;
			if(jd==null){
				jd = new JsonData();
				jd.setDkey(JsonDataKey.KEY_EDITION_LANGUAGE + mpi.getId());
				jd.setValidtime(new Timestamp(DateUtil.addDay(mpi.getPlaydate(), 1).getTime()));
			}
			jd.setData(JsonUtils.writeMapToJson(changeMap));
			baseDao.saveObject(jd);
		}
	}
	@Override
	public boolean restoreMpiFromHisData(String seqNo) {
		MoviePlayItem mpi = new MoviePlayItem();
		HisData hisData = baseDao.getObject(HisData.class, HisData.KEY_PRE_MPI + seqNo);
		if(hisData==null) return false;
		Map<String, String> map = JsonUtils.readJsonToMap(hisData.getJsonData());
		BindUtils.bindData(mpi, map);
		Long mpid = mpi.getId();
		mpi.setId(null);
		if(mpi.hasOpenStatus(OpiConstant.MPI_OPENSTATUS_OPEN)){
			OpenPlayItem opi = baseDao.getObjectByUkey(OpenPlayItem.class, "mpid", mpid);
			if(opi == null) mpi.setOpenStatus(OpiConstant.MPI_OPENSTATUS_INIT);
		}else{
			mpi.setOpenStatus(OpiConstant.MPI_OPENSTATUS_INIT);
		}
		baseDao.saveObject(mpi);
		hibernateTemplate.flush();
		String update = "update WEBDATA.movielist set recordid=? where recordid=?";
		jdbcTemplate.update(update, mpid, mpi.getId());
		return true;
	}
	
	@Override
	public void saveMpiToHisData(MoviePlayItem mpi){
		if(StringUtils.isBlank(mpi.getSeqNo())) return;
		String key = HisData.KEY_PRE_MPI + mpi.getSeqNo();
		HisData hisData = baseDao.getObject(HisData.class, HisData.KEY_PRE_MPI + mpi.getSeqNo());
		if(hisData == null){
			hisData = new HisData(key, JsonUtils.writeObjectToJson(mpi), DateUtil.parseTimestamp(mpi.getFullPlaytime()));
		}else{
			hisData.setJsonData(JsonUtils.writeObjectToJson(mpi));
			hisData.setValidtime(DateUtil.parseTimestamp(mpi.getFullPlaytime()));
		}
		baseDao.saveObject(hisData);
	}
	public void saveLastChange(){
		
	}
	private OpenSeat createOpenSeat(MpiSeat seat, Long mpid){
		OpenSeat oseat = new OpenSeat();
		oseat.setLineno(seat.getLineno());
		oseat.setRankno(seat.getRankno());
		oseat.setSeatline(seat.getSeatline());
		oseat.setSeatrank(seat.getSeatrank());
		oseat.setMpid(mpid);
		oseat.setLoveInd("0");
		oseat.setSeattype(SeatConstant.SEAT_TYPE_A);
		oseat.setStatus(SeatConstant.STATUS_NEW);
		return oseat;
	}
	@Override
	public void updateLocknum(Long mpid, int total) {
		OpenPlayItem opi = baseDao.getObjectByUkey(OpenPlayItem.class, "mpid", mpid, true);
		if(opi.getLocknum()!=total){
			opi.setLocknum(total);
			baseDao.saveObject(opi);
		}
	}
	@Override
	public ErrorCode<OpenPlayItem> updateOpiFee(Long mpid, Integer fee, Long userid) {
		OpenPlayItem opi = baseDao.getObjectByUkey(OpenPlayItem.class, "mpid", mpid, true);
		if(fee<0 || fee>=opi.getPrice()) return ErrorCode.getFailure("设置服务费错误：" + fee);
		int oldfee = opi.getFee();
		opi.setFee(fee);
		baseDao.saveObject(opi);
		dbLogger.warnWithType(LogTypeConstant.LOG_TYPE_USERACTION, "修改场次服务费[" + mpid + "][user:" + userid + "][" + oldfee + "-->" +  fee + "][" + DateUtil.formatTimestamp(new Date()) + "]");
		return ErrorCode.getSuccessReturn(opi);
	}
	@Override
	public ErrorCode<OpenPlayItem> changePartnerStatus(Long mpid, String status) {
		OpenPlayItem opi = baseDao.getObjectByUkey(OpenPlayItem.class, "mpid", mpid, true);
		//TODO:status值验证
		opi.setPartner(status);
		baseDao.saveObject(opi);
		return ErrorCode.getSuccessReturn(opi);
	}
	@Override
	public ErrorCode<OpenPlayItem> changeOpentime(Long mpid, Timestamp opentime, Long userid) {
		if (opentime == null)
			return ErrorCode.getFailure("请输入开放时间！");
		OpenPlayItem opi = baseDao.getObjectByUkey(OpenPlayItem.class, "mpid", mpid, true);
		if (opi == null){
			return ErrorCode.getFailure("场次不存在！");
		}
		if(opentime.after(opi.getClosetime())){
			return ErrorCode.getFailure("开放时间不能在结束时间之后！");
		}
		ChangeEntry changeEntry = new ChangeEntry(opi);
		opi.setOpentime(opentime);
		baseDao.saveObject(opi);
		monitorService.saveChangeLog(userid, OpenPlayItem.class, opi.getMpid(), changeEntry.getChangeMap(opi));
		return ErrorCode.getSuccessReturn(opi);
	}
	@Override
	public ErrorCode<OpenPlayItem> changeClosetime(Long mpid, Timestamp closetime, Long userid) {
		if (closetime == null)
			return ErrorCode.getFailure("请输入关闭时间！");
		OpenPlayItem opi = baseDao.getObjectByUkey(OpenPlayItem.class, "mpid", mpid, true);
		if (closetime.after(opi.getPlaytime())){
			return ErrorCode.getFailure("关闭时间不能在放映时间之后！");
		}
		ChangeEntry changeEntry = new ChangeEntry(opi);
		opi.setClosetime(closetime);
		baseDao.saveObject(opi);
		monitorService.saveChangeLog(userid, OpenPlayItem.class, opi.getMpid(), changeEntry.getChangeMap(opi));
		return ErrorCode.getSuccessReturn(opi);
	}
	@Override
	public ErrorCode<OpenPlayItem> updatePointLimit(Long opid, Integer minpoint, Integer maxpoint, Long userid) {
		OpenPlayItem opi = baseDao.getObject(OpenPlayItem.class, opid);
		if (opi == null)
			return ErrorCode.getFailure("该场次不存在");
		if (minpoint == null || maxpoint == null)
			return ErrorCode.getFailure("信息填写不完整");
		if (minpoint < 0 || maxpoint < 0)
			return ErrorCode.getFailure("数据不合法");
		if (minpoint > maxpoint)
			return ErrorCode.getFailure("积分下限不能大于积分上限");
		ChangeEntry changeEntry = new ChangeEntry(opi);
		opi.setMinpoint(minpoint);
		opi.setMaxpoint(maxpoint);
		baseDao.saveObject(opi);
		monitorService.saveChangeLog(userid, OpenPlayItem.class, opi.getMpid(), changeEntry.getChangeMap(opi));
		return ErrorCode.getSuccessReturn(opi);
	}
	@Override
	public ErrorCode<OpenPlayItem> updateGivepoint(Long opid, Integer point, Long userid) {
		OpenPlayItem opi = baseDao.getObject(OpenPlayItem.class, opid);
		if (opi == null)
			return ErrorCode.getFailure("该场次不存在");
		if (point == null)
			return ErrorCode.getFailure("请输入积分值");
		ChangeEntry changeEntry = new ChangeEntry(opi);
		opi.setGivepoint(point);
		baseDao.saveObject(opi);
		monitorService.saveChangeLog(userid, OpenPlayItem.class, opi.getMpid(), changeEntry.getChangeMap(opi));
		return ErrorCode.getSuccessReturn(opi);
	}
	@Override
	public ErrorCode<OpenPlayItem> updateSpflag(Long opid, String spflag, Long userid) {
		OpenPlayItem opi = baseDao.getObject(OpenPlayItem.class, opid);
		if (opi == null){
			return ErrorCode.getFailure("该场次不存在");
		}
		ChangeEntry changeEntry = new ChangeEntry(opi);
		opi.setSpflag(spflag);
		baseDao.saveObject(opi);
		monitorService.saveChangeLog(userid, OpenPlayItem.class, opi.getMpid(), changeEntry.getChangeMap(opi));
		return ErrorCode.getSuccessReturn(opi);
	}
	@Override
	public ErrorCode<OpenPlayItem> updateOpiRemark(Long opid, String remark, Long userid) {
		OpenPlayItem opi = baseDao.getObject(OpenPlayItem.class, opid);
		MoviePlayItem mpi = baseDao.getObject(MoviePlayItem.class, opi.getMpid());
		if(mpi == null) return ErrorCode.getFailure("场次信息不存在！");
		ChangeEntry opiChangeEntry = new ChangeEntry(opi);
		opi.setRemark(remark);
		Map diff = opiChangeEntry.getChangeMap(opi);
		monitorService.saveChangeLog(userid, OpenPlayItem.class, mpi.getId(), diff);
		baseDao.saveObject(opi);
		return ErrorCode.getSuccessReturn(opi);
	}
	@Override
	public void updateOpiStatsByMtx(Long opid, int sellNum, boolean isFinished){
		OpenPlayItem opi = baseDao.getObject(OpenPlayItem.class, opid);
		String sqlqry = "SELECT sum(o.quantity) from WEBDATA.ticket_order o where o.status='paid_success' and o.order_type='ticket' and o.relatedid=?";
		Integer gsellnum = 0;
		try{
			gsellnum = jdbcTemplate.queryForInt(sqlqry, opi.getMpid());
		}catch(Exception e){
			dbLogger.warn(StringUtil.getExceptionTrace(e));
		}
		Integer csellnum = sellNum - gsellnum - 1;
		opi.setGsellnum(gsellnum);
		opi.setCsellnum(csellnum>0?csellnum:0);
		opi.setUpdatetime(new Timestamp(System.currentTimeMillis()));
		if(isFinished) {
			opi.setOtherinfo(JsonUtils.addJsonKeyValue(opi.getOtherinfo(), OpiConstant.STATISTICS, "true"));
		}
		baseDao.saveObject(opi);
	}
}
