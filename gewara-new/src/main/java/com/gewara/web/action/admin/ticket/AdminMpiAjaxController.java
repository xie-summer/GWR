package com.gewara.web.action.admin.ticket;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.gewara.Config;
import com.gewara.constant.TagConstant;
import com.gewara.constant.sys.CacheConstant;
import com.gewara.constant.sys.MongoData;
import com.gewara.constant.sys.SpecialRights;
import com.gewara.constant.ticket.OpiConstant;
import com.gewara.constant.ticket.OrderConstant;
import com.gewara.constant.ticket.SeatConstant;
import com.gewara.model.acl.User;
import com.gewara.model.movie.Cinema;
import com.gewara.model.movie.CinemaRoom;
import com.gewara.model.movie.Movie;
import com.gewara.model.movie.MoviePlayItem;
import com.gewara.model.movie.RoomSeat;
import com.gewara.model.ticket.AutoSetter;
import com.gewara.model.ticket.OpenPlayItem;
import com.gewara.model.ticket.OpenSeat;
import com.gewara.mongo.MongoService;
import com.gewara.service.MessageService;
import com.gewara.service.order.OrderQueryService;
import com.gewara.service.ticket.OpenPlayService;
import com.gewara.service.ticket.OpiManageService;
import com.gewara.support.ErrorCode;
import com.gewara.support.MultiPropertyComparator;
import com.gewara.untrans.CacheService;
import com.gewara.untrans.LockCallback;
import com.gewara.untrans.LockService;
import com.gewara.untrans.hbase.ChangeLogService;
import com.gewara.untrans.monitor.RoleTag;
import com.gewara.untrans.ticket.MpiOpenService;
import com.gewara.util.BeanUtil;
import com.gewara.util.ChangeEntry;
import com.gewara.util.DateUtil;
import com.gewara.util.VmUtils;
import com.gewara.web.action.admin.BaseAdminController;


/**
 * 场次管理ajax
 * 
 * @author acerge(acerge@163.com)
 * @since 5:12:54 PM Apr 12, 2011
 */
@Controller
public class AdminMpiAjaxController extends BaseAdminController {
	@Autowired@Qualifier("mongoService")
	private MongoService mongoService;
	@Autowired@Qualifier("cacheService")
	private CacheService cacheService;
	@Autowired@Qualifier("lockService")
	private LockService lockService;
	@Autowired@Qualifier("messageService")
	private MessageService messageService;
	
	public void setCacheService(CacheService cacheService) {
		this.cacheService = cacheService;
	}
	@Autowired@Qualifier("orderQueryService")
	private OrderQueryService orderQueryService;

	@Autowired@Qualifier("mpiOpenService")
	private MpiOpenService mpiOpenService;
	
	@Autowired@Qualifier("openPlayService")
	private OpenPlayService openPlayService;

	@Autowired
	@Qualifier("changeLogService")
	private ChangeLogService changeLogService;
	
	@Autowired@Qualifier("opiManageService")
	private OpiManageService opiManageService;

	public void setOpenPlayService(OpenPlayService openPlayService) {
		this.openPlayService = openPlayService;
	}

	private void saveOpiManualChange(Long userid, Long mpid, String manualAction, String manualDetail){
		Map changeMap = new HashMap();
		changeMap.put("manualAction", manualAction);
		changeMap.put("manualDetail", manualDetail);
		monitorService.saveChangeLog(userid, OpenPlayItem.class, mpid, changeMap);
	}
	/**
	 * 增加座位
	 * 
	 * @param location:L8R9
	 *           8行9列
	 * @return
	 */
	@RequestMapping("/admin/ticket/mpi/addSeat.xhtml")
	public String addSeat(Long roomid, String location, String seatline, String seatrank, ModelMap model) {
		int line = Integer.parseInt(location.substring(1, location.indexOf('R')));
		int rank = Integer.parseInt(location.substring(location.indexOf('R') + 1));
		RoomSeat seat = openPlayService.getRoomSeatByLocation(roomid, line, rank);
		if (seat == null) {
			seat = new RoomSeat(roomid, line, rank);
			if (StringUtils.isNotBlank(seatline))
				seat.setSeatline(seatline);
			if (StringUtils.isNotBlank(seatrank))
				seat.setSeatrank(seatrank);
			daoService.saveObject(seat);
			CinemaRoom room = daoService.getObject(CinemaRoom.class, roomid);
			room.setUpdatetime(new Timestamp(System.currentTimeMillis()));
			daoService.saveObject(room);
		}
		return showJsonSuccess(model);

	}

	/**
	 * 清除座位
	 * 
	 * @param seatId
	 * @return
	 */
	@RequestMapping("/admin/ticket/mpi/clearSeat.xhtml")
	public String clearSeat(Long roomid, String location, ModelMap model) {
		int line = Integer.parseInt(location.substring(1, location.indexOf('R')));
		int rank = Integer.parseInt(location.substring(location.indexOf('R') + 1));
		RoomSeat seat = openPlayService.getRoomSeatByLocation(roomid, line, rank);
		if (seat != null)
			daoService.removeObject(seat);
		CinemaRoom room = daoService.getObject(CinemaRoom.class, roomid);
		room.setUpdatetime(new Timestamp(System.currentTimeMillis()));
		daoService.saveObject(room);
		return showJsonSuccess(model);
	}

	@RequestMapping("/admin/ticket/mpi/setSeatRankNo.xhtml")
	public String setSeatRankNo(Long roomid, String location, String rankno, ModelMap model) {
		int line = Integer.parseInt(location.substring(1, location.indexOf('R')));
		int rank = Integer.parseInt(location.substring(location.indexOf('R') + 1));
		RoomSeat seat = openPlayService.getRoomSeatByLocation(roomid, line, rank);
		if (seat != null) {
			seat.setSeatrank(rankno);
			daoService.saveObject(seat);
			CinemaRoom room = daoService.getObject(CinemaRoom.class, roomid);
			room.setUpdatetime(new Timestamp(System.currentTimeMillis()));
			daoService.saveObject(room);
		}
		return showJsonSuccess(model);
	}

	@RequestMapping("/admin/ticket/mpi/addRowSeat.xhtml")
	public String addRowSeat(Long roomid, ModelMap model) {
		boolean success = openPlayService.addRowSeat(roomid);
		if (success)
			return showJsonSuccess(model);
		return showJsonError_DATAERROR(model);
	}

	@RequestMapping("/admin/ticket/mpi/addRankSeat.xhtml")
	public String addRankSeat(Long roomid, ModelMap model) {
		boolean success = openPlayService.addRankSeat(roomid);
		if (success)
			return showJsonSuccess(model);
		return showJsonError_DATAERROR(model);
	}

	@RequestMapping("/admin/ticket/mpi/deleteRowSeat.xhtml")
	public String deleteRowSeat(Long roomid, ModelMap model) {
		boolean success = openPlayService.deleteRowSeat(roomid);
		if (success)
			return showJsonSuccess(model);
		return showJsonError_DATAERROR(model);
	}

	@RequestMapping("/admin/ticket/mpi/deleteRankSeat.xhtml")
	public String deleteRankSeat(Long roomid, ModelMap model) {
		boolean success = openPlayService.deleteRankSeat(roomid);
		if (success)
			return showJsonSuccess(model);
		return showJsonError_DATAERROR(model);
	}

	@RequestMapping("/admin/ticket/mpi/changeSeatLine.xhtml")
	public String changeSeatLine(Long roomid, int lineno, String newline, ModelMap model) {
		boolean success = openPlayService.updateSeatLine(roomid, lineno, newline);
		if (success)
			return showJsonSuccess(model);
		return showJsonError_DATAERROR(model);
	}

	@RequestMapping("/admin/ticket/mpi/updateMpiGewaPrice.xhtml")
	public String updateMpiGewaPrice(Long mpid, Integer gewaprice, ModelMap model) {
		User user = getLogonUser();
		ErrorCode<OpenPlayItem> code = opiManageService.updateGewaPrice(mpid, gewaprice, user);
		if (code.isSuccess()) {
			if (code.getRetval() != null) {
				mpiOpenService.refreshMpiRelatePage(code.getRetval());
			}
			return showJsonSuccess(model);
		}
		return showJsonError(model, code.getMsg());
	}
	
	@RequestMapping("/admin/ticket/mpi/saveSetError.xhtml")
	public String saveSetError(Long mpid,String msgType,String msg,String price,ModelMap model){
		Map map = new HashMap();
		User user = getLogonUser();
		map.put("msgType", msgType);
		map.put("msg",msg);
		map.put("price", price);
		Map<Long, Map<String, String>> result = changeLogService.getChangeLogList(Config.SYSTEMID, "OpenPlayItem", mpid);
		if(!VmUtils.isEmptyList(result)){
			map.put("最后修改该场次的用户", ((Map<String, String>)result.values().toArray()[result.values().size() - 1]).get("userid"));
		}
		monitorService.saveChangeLog(user.getId(), OpenPlayItem.class, mpid, map);
		return showJsonSuccess(model);
	}
	
	@RequestMapping("/admin/ticket/mpi/updateMpiFee.xhtml")
	public String updateMpiFee(Long mpid, Integer fee, ModelMap model) {
		User user = getLogonUser();
		ErrorCode<OpenPlayItem> result = opiManageService.updateOpiFee(mpid, fee, user.getId());
		if(result.isSuccess()) return showJsonSuccess(model);
		return showJsonError(model, result.getMsg());
		
	}
	@RequestMapping("/admin/ticket/mpi/updateMpiCostPrice.xhtml")
	public String updateMpiCostPrice(Long mpid, Integer costprice, ModelMap model) {
		User user = getLogonUser();
		ErrorCode code = opiManageService.updateCostPrice(mpid, costprice, user);
		if (code.isSuccess()) {
			return showJsonSuccess(model);
		}
		return showJsonError(model, code.getMsg());
	}

	@RequestMapping("/admin/ticket/mpi/updateMpiElecard.xhtml")
	public String updateMpiElecard(Long mpid, String cardtype, ModelMap model) {
		User user = getLogonUser();
		String type = StringUtils.upperCase(StringUtils.trim(cardtype));
		ErrorCode<String> code = opiManageService.updateElecard(mpid, type, user);
		if (code.isSuccess()){
			saveOpiManualChange(user.getId(), mpid, "修改优惠类型", code.getRetval());
			return showJsonSuccess(model);
		}
		return showJsonError(model, code.getMsg());
	}

	@RequestMapping("/admin/ticket/mpi/changeMpiStatus.xhtml")
	public String changeMpiStatus(Long mpid, String status, ModelMap model) {
		User user = getLogonUser();
		OpenPlayItem opi = daoService.getObjectByUkey(OpenPlayItem.class, "mpid", mpid, true);
		ChangeEntry changeEntry = new ChangeEntry(opi);
		ErrorCode<OpenPlayItem> code = opiManageService.changeMpiStatus(mpid, status, user.getId());
		if (code.isSuccess()){
			opi = code.getRetval();
			if(OpiConstant.STATUS_BOOK.equals(opi.getStatus())){
				CinemaRoom room = daoService.getObject(CinemaRoom.class, opi.getRoomid());
				String editionErr = OpiConstant.validateRoomPlaytype(room.getPlaytype(), opi.getEdition());
				if(StringUtils.isNotBlank(editionErr)){
					monitorService.saveSysWarn("开放场次可能有问题：" + editionErr, BeanUtil.buildString(opi, false), RoleTag.dingpiao);
				}
			}
			//open 或者close 都需要刷新缓存
			mpiOpenService.refreshMpiRelatePage(opi);
			
			//废弃场次取消发送短信
			if(OpiConstant.STATUS_DISCARD.equals(status)){
				List<String> orderList = orderQueryService.getTradeNoListByMpid(TagConstant.TAG_MOVIE, mpid, OrderConstant.STATUS_PAID_SUCCESS);
				messageService.updateSMSRecordStatus(orderList);
			}
			monitorService.saveChangeLog(user.getId(), OpenPlayItem.class, opi.getMpid(), changeEntry.getChangeMap(opi));
			return showJsonSuccess(model);
		}
		return showJsonError(model, code.getMsg());
	}

	@RequestMapping("/admin/ticket/mpi/changePartnerStatus.xhtml")
	public String changePartnerStatus(Long mpid, String status, ModelMap model) {
		opiManageService.changePartnerStatus(mpid, status);
		return showJsonSuccess(model);
	}

	@RequestMapping("/admin/ticket/mpi/setMpiOpentime.xhtml")
	public String changeOpentime(Long mpid, Timestamp opentime, ModelMap model) {
		User user = getLogonUser();
		ErrorCode<OpenPlayItem> result = opiManageService.changeOpentime(mpid, opentime, user.getId());
		if(result.isSuccess()) return showJsonSuccess(model);
		return showJsonError(model, result.getMsg());
	}

	@RequestMapping("/admin/ticket/mpi/setMpiClosetime.xhtml")
	public String changeClosetime(Long mpid, Timestamp closetime, ModelMap model) {
		User user = getLogonUser();
		ErrorCode<OpenPlayItem> result = opiManageService.changeClosetime(mpid, closetime, user.getId());
		if(result.isSuccess()) return showJsonSuccess(model);
		return showJsonError(model, result.getMsg());
	}

	@RequestMapping("/admin/ticket/mpi/lockSeat.xhtml")
	public String lockSeat(Long mpid, Long seatId, String locktype, String lockReason, ModelMap model) {
		User user = getLogonUser();
		ErrorCode<OpenSeat> code = openPlayService.lockSeat(mpid, seatId, locktype, lockReason);
		if (code.isSuccess()){
			saveOpiManualChange(user.getId(), mpid, "锁定座位", code.getRetval().getSeatLabel());
			List<OpenSeat> openSeatList = openPlayService.refreshOpenSeatList(mpid);
			refreshLockNum(mpid, openSeatList);
			return showJsonSuccess(model);
		}
		return showJsonError(model, code.getMsg());
	}
	@RequestMapping("/admin/ticket/mpi/batchLock.xhtml")
	public String batchLock(Long mpid, String locktype, String lockReason, String lockline, String lockrank, ModelMap model) {
		User user = getLogonUser();
		ErrorCode code = opiManageService.batchLockSeat(mpid, locktype, lockReason, lockline, lockrank);
		if (code.isSuccess()){
			saveOpiManualChange(user.getId(), mpid, "批量锁定座位", "type="+ locktype + ",line:" +  lockline + ",rank:" + lockrank +",reason:" + lockReason);
			List<OpenSeat> openSeatList = openPlayService.refreshOpenSeatList(mpid);
			refreshLockNum(mpid, openSeatList);
			return showJsonSuccess(model);
		}
		return showJsonError(model, code.getMsg());
	}

	@RequestMapping("/admin/ticket/mpi/batchUnLock.xhtml")
	public String batchUnLock(Long mpid, String lockline, String lockrank, ModelMap model) {
		User user = getLogonUser();
		ErrorCode code = opiManageService.batchUnLockSeat(mpid, lockline, lockrank);
		if (code.isSuccess()){
			saveOpiManualChange(user.getId(), mpid, "批量释放座位", "line:" +  lockline + ",rank:" + lockrank);
			List<OpenSeat> openSeatList = openPlayService.refreshOpenSeatList(mpid);
			refreshLockNum(mpid, openSeatList);
			return showJsonSuccess(model);
		}
		return showJsonError(model, code.getMsg());
	}

	@RequestMapping("/admin/ticket/mpi/releaseSeat.xhtml")
	public String releaseSeat(Long mpid, Long seatId, ModelMap model) {
		User user = getLogonUser();
		ErrorCode<OpenSeat> code = opiManageService.releaseSeat(mpid, seatId);
		if (code.isSuccess()){
			saveOpiManualChange(user.getId(), mpid, "释放座位", code.getRetval().getSeatLabel());
			List<OpenSeat> openSeatList = openPlayService.refreshOpenSeatList(mpid);
			refreshLockNum(mpid, openSeatList);
			return showJsonSuccess(model);
		}
		return showJsonError(model, code.getMsg());
	}
	@RequestMapping("/admin/ticket/mpi/refreshLockNum.xhtml")
	@ResponseBody
	public String refreshLockNum(Long mpid){
		opiManageService.verifyOpiSeatLock(mpid);
		List<OpenSeat> openSeatList = openPlayService.getOpenSeatList(mpid);
		Map<String, String> result = refreshLockNum(mpid, openSeatList);
		return ""+result + new Date();
	}
	private Map<String, String> refreshLockNum(Long mpid, List<OpenSeat> openSeatList){
		Map<String, String> lockNumMap = new HashMap<String, String>();
		int lockB = 0;
		int lockC = 0;
		int lockD = 0;
		int total = 0;
		for(OpenSeat seat: openSeatList){
			if(seat.isLocked()){
				total ++;
				if(seat.getStatus().equals(SeatConstant.STATUS_LOCKB)) lockB++;
				else if(seat.getStatus().equals(SeatConstant.STATUS_LOCKC)) lockC++;
				else lockD ++;
			}
		}
		lockNumMap.put("_id", "" + mpid);
		lockNumMap.put("mpid", "" + mpid);
		lockNumMap.put("B", ""+lockB);
		lockNumMap.put("C", ""+lockC);
		lockNumMap.put("D", ""+lockD);
		lockNumMap.put("ALL", ""+total);
		
		OpenPlayItem opi = daoService.getObjectByUkey(OpenPlayItem.class, "mpid", mpid, true);
		lockNumMap.put("validtime", DateUtil.formatTimestamp(opi.getPlaytime()));
		mongoService.saveOrUpdateMap(lockNumMap, "mpid", MongoData.NS_OPI_LOCKNUM);
		opiManageService.updateLocknum(mpid, total);
		return lockNumMap;
	}
	@RequestMapping("/admin/ticket/mpi/openMpi.xhtml")
	public String openMpi(Long mpid, String tempid, ModelMap model) {
		User user = getLogonUser();
		MoviePlayItem mpi = daoService.getObject(MoviePlayItem.class, mpid);
		mpiOpenService.asynchOpenMpi(mpi, user.getId(), OpiConstant.OPEN_GEWARA, tempid);
		return showJsonSuccess(model, "后台正在开放场次！");
	}

	@RequestMapping("/admin/ticket/mpi/openHfhMpi.xhtml")
	public String openHfhMpi(Long mpid, String tempid, ModelMap model) {
		User user = getLogonUser();
		if(!user.isRole(SpecialRights.ONLINEMPI)){
			return showJsonError(model, "无权限开放系统直连场次！");
		}
		MoviePlayItem mpi = daoService.getObject(MoviePlayItem.class, mpid);
		
		ErrorCode result = mpiOpenService.asynchOpenMpi(mpi, user.getId(), mpi.getOpentype(), tempid);
		if(result.isSuccess()){
			return showJsonSuccess(model, "后台正在开放场次！");
		}else{
			return showJsonError(model, result.getMsg());
		}
	}
	@RequestMapping("/admin/ticket/mpi/autoOpenHfhMpi.xhtml")
	public String autoOpenHfhMpi(Long mpid, Long setterId, ModelMap model) {
		User user = getLogonUser();
		if(!user.isRole(SpecialRights.ONLINEMPI)){
			return showJsonError(model, "无权限开放系统直连场次！");
		}
		MoviePlayItem mpi = daoService.getObject(MoviePlayItem.class, mpid);
		if(OpiConstant.OPEN_PNX.equals(mpi.getOpentype())){
			return showJsonError(model, "东票场次暂不支持！");
		}else{
			AutoSetter setter = daoService.getObject(AutoSetter.class, setterId);
			if(AutoSetter.STATUS_OPEN_A.equals(setter.getStatus()) ){
				setter.opiStatus = false;	
			}
			mpiOpenService.asynchAutoOpenMpi(mpi, user.getId(), setter);
		}
		return showJsonSuccess(model, "后台正在开放场次！");
	}

	@RequestMapping("/admin/ticket/mpi/setSeatInitstatus.xhtml")
	public String setSeatInitstatus(Long seatid, String initstatus, ModelMap model) {
		ErrorCode code = opiManageService.updateSeatInitStatus(seatid, initstatus);
		if (code.isSuccess())
			return showJsonSuccess(model);
		return showJsonError(model, code.getMsg());
	}

	@RequestMapping("/admin/ticket/mpi/setLoveInd.xhtml")
	public String setLoveInd(Long seatid, String loveInd, ModelMap model) {
		ErrorCode<Long> code = opiManageService.updateSeatLoveInd(seatid, loveInd);
		if (code.isSuccess())
			return showJsonSuccess(model, "" + code.getRetval());
		return showJsonError(model, code.getMsg());
	}
	@RequestMapping("/admin/ticket/saveMpiRemark.xhtml")
	public String saveMpiRemark(Long opid, String remark,  ModelMap model) {
		User user = getLogonUser();
		ErrorCode code = opiManageService.updateOpiRemark(opid, remark, user.getId());
		if (code.isSuccess()){
			return showJsonSuccess(model);
		}
		return showJsonError(model, code.getMsg());
	}
	


	/**
	 * 将座位加入到某种类型中
	 * @param mpid
	 * @param seattype
	 * @param seatid
	 * @return
	 */
	@RequestMapping("/admin/ticket/mpi/addPriceSeat.xhtml")
	public String addPriceSeat(Long mpid, String seattype, Long seatid, ModelMap model) {
		User user = getLogonUser();
		ErrorCode<String> code = opiManageService.addPriceSeat(mpid, seattype, seatid);
		if (code.isSuccess()){
			openPlayService.refreshOpenSeatList(mpid);
			saveOpiManualChange(user.getId(), mpid, "更改座位类型", code.getRetval());
			return showJsonSuccess(model);
		}
		return showJsonError(model, code.getMsg());

	}

	@RequestMapping("/admin/ticket/mpi/removePriceSeat.xhtml")
	public String removePriceSeat(Long mpid, Long seatid, ModelMap model) {
		User user = getLogonUser();
		ErrorCode<String> code = opiManageService.removePriceSeat(mpid, seatid);
		if (code.isSuccess()){
			openPlayService.refreshOpenSeatList(mpid);
			saveOpiManualChange(user.getId(), mpid, "更改座位类型", code.getRetval());
			return showJsonSuccess(model);
		}
		return showJsonError(model, code.getMsg());

	}

	@RequestMapping("/admin/ticket/mpi/batchAddPriceSeat.xhtml")
	public String batchAddPriceSeat(Long mpid, String seattype, String rows, String ranks, ModelMap model) {
		User user = getLogonUser();
		ErrorCode<String> code = opiManageService.batchAddPriceSeat(mpid, seattype, rows, ranks);
		if (code.isSuccess()){
			saveOpiManualChange(user.getId(), mpid, "批量更改座位价格", code.getRetval());
			return showJsonSuccess(model);
		}
			
		return showJsonError(model, code.getMsg());
	}

	@RequestMapping("/admin/ticket/mpi/batchRemovePriceSeat.xhtml")
	public String batchRemovePriceSeat(Long mpid, String seattype, String rows, String ranks, ModelMap model) {
		User user = getLogonUser();
		ErrorCode<String> code = opiManageService.batchRemovePriceSeat(mpid, seattype, rows, ranks);
		if (code.isSuccess()){
			saveOpiManualChange(user.getId(), mpid, "批量更改座位价格", code.getRetval());
			openPlayService.refreshOpenSeatList(mpid);
			return showJsonSuccess(model);
		}
		return showJsonError(model, code.getMsg());

	}

	@RequestMapping("/admin/ticket/mpi/discardOpi.xhtml")
	public String discardOpi(final Long mpid, ModelMap model) {
		final Map result = new HashMap(); 
		final Long userId = getLogonUser().getId();
		boolean lock = lockService.tryDoWithWriteLock("discard" + (mpid % 577), new LockCallback() {
			@Override
			public void processWithInLock() {
				ErrorCode code = opiManageService.discardOpiByMpid(mpid, userId);
				if(!code.isSuccess()){
					result.put("msg", code.getMsg());
				}
			}
		});
		if(!lock){
			return showJsonError(model, "他人正在废弃！");
		}
		return result.isEmpty()?showJsonSuccess(model):showJsonError(model, "废弃场次失败，" + result.get("msg"));
	}
	@RequestMapping("/admin/ticket/mpi/change2Unopen.xhtml")
	public String change2Unopen(Long mpid, ModelMap model) {
		MoviePlayItem mpi = daoService.getObject(MoviePlayItem.class, mpid);
		if(OpiConstant.MPI_OPENSTATUS_INIT.equals(mpi.getOpenStatus())){
			mpi.setOpenStatus(OpiConstant.MPI_OPENSTATUS_CLOSE);
			daoService.saveObject(mpi);
			return showJsonSuccess(model);
		}
		return showJsonError(model, "场次状态不正确，请刷新！");
	}

	
	@RequestMapping("/admin/ticket/mpi/getOpenPlayItem.xhtml")
	public String getOpenPlayItem(Long opid, ModelMap model) {
		OpenPlayItem opi = daoService.getObject(OpenPlayItem.class, opid);
		if (opi == null)
			return showJsonError_NOT_FOUND(model);
		Map map = BeanUtil.getBeanMapWithKey(opi, "minpoint", "maxpoint");
		return showJsonSuccess(model, map);
	}

	@RequestMapping("/admin/ticket/mpi/savePointParams.xhtml")
	public String savePointParams(Long opid, Integer minpoint, Integer maxpoint, ModelMap model) {
		User user = getLogonUser();
		ErrorCode<OpenPlayItem> result = opiManageService.updatePointLimit(opid, minpoint, maxpoint, user.getId());
		if(result.isSuccess()) return showJsonSuccess(model);
		return showJsonError(model, result.getMsg());
	}

	@RequestMapping("/admin/ticket/mpi/setOpiGivepoint.xhtml")
	public String setOpiGivepoint(Long opid, Integer point, ModelMap model) {
		User user = getLogonUser();
		ErrorCode<OpenPlayItem> result = opiManageService.updateGivepoint(opid, point, user.getId());
		if(result.isSuccess()) return showJsonSuccess(model);
		return showJsonError(model, result.getMsg());
	}

	@RequestMapping("/admin/ticket/mpi/setOpiSpflag.xhtml")
	public String setOpiSpflag(Long opid, String spflag, ModelMap model) {
		User user = getLogonUser();
		ErrorCode<OpenPlayItem> result = opiManageService.updateSpflag(opid, spflag, user.getId());
		if(result.isSuccess()) return showJsonSuccess(model);
		return showJsonError(model, result.getMsg());
	}
	
	@RequestMapping("/admin/ticket/mpi/setMpidGather.xhtml")
	public String setOpiGather(Long mpid, ModelMap model){
		User user = getLogonUser();
		MoviePlayItem item = daoService.getObject(MoviePlayItem.class, mpid);
		if (item == null)	return showJsonError_NOT_FOUND(model);
		String key = CacheConstant.KEY_OPIGATHER + user.getId();
		String idListStr = (String) cacheService.get(CacheConstant.REGION_ONEHOUR, key);
		List<Long> itemIdList = null;
		if(StringUtils.isBlank(idListStr)) itemIdList = new ArrayList<Long>();
		else itemIdList = BeanUtil.getIdList(idListStr, ",");
		if(itemIdList.contains(mpid)) return showJsonError(model, "已收集过，不能重复操作！");
		itemIdList.add(mpid);
		cacheService.set(CacheConstant.REGION_ONEHOUR, key, StringUtils.join(itemIdList.toArray(), ","));
		return showJsonSuccess(model);
	}
	
	@RequestMapping("/admin/ticket/mpi/getMpidGather.xhtml")
	public String getOpiGather(String mpids, ModelMap model){
		if(StringUtils.isBlank(mpids)){
			User user = getLogonUser();
			String key = CacheConstant.KEY_OPIGATHER + user.getId();
			mpids = (String) cacheService.get(CacheConstant.REGION_ONEHOUR, key);
		}else model.put("isRead", true);
		List<Long> itemIdList = null;
		if(StringUtils.isNotBlank(mpids)){
			itemIdList = BeanUtil.getIdList(mpids, ",");
			List<MoviePlayItem> playItemList = daoService.getObjectList(MoviePlayItem.class, itemIdList);
			Collections.sort(playItemList, new MultiPropertyComparator(new String[]{"cinemaid","playdate","playtime"}, new boolean[]{true, true, true}));
			model.put("idListStr", mpids);
			model.put("playItemList", playItemList);
			Map<Long, Movie> movieMap = new HashMap<Long, Movie>();
			Map<Long, Cinema> cinemaMap = new HashMap<Long, Cinema>();
			Map<Long, OpenPlayItem> opiMap = new HashMap<Long, OpenPlayItem>();
			for (MoviePlayItem moviePlayItem : playItemList) {
				Movie movie = daoService.getObject(Movie.class, moviePlayItem.getMovieid());
				movieMap.put(moviePlayItem.getMovieid(), movie);
				Cinema cinema = daoService.getObject(Cinema.class, moviePlayItem.getCinemaid());
				cinemaMap.put(moviePlayItem.getCinemaid(), cinema);
				OpenPlayItem opi = daoService.getObjectByUkey(OpenPlayItem.class, "mpid", moviePlayItem.getId(), true);
				opiMap.put(moviePlayItem.getId(), opi);
			}
			model.put("movieMap", movieMap);
			model.put("cinemaMap", cinemaMap);
			model.put("opiMap", opiMap);
		}else return showJsonError(model, "没有收集场次！");
		return "admin/ticket/opiGatherList.vm";
	}
	@RequestMapping("/admin/ticket/mpi/delMpidGather.xhtml")
	public String delOpiGather(Long mpid, ModelMap model){
		User user = getLogonUser();
		MoviePlayItem item = daoService.getObject(MoviePlayItem.class, mpid);
		if (item == null)	return showJsonError_NOT_FOUND(model);
		String key = CacheConstant.KEY_OPIGATHER + user.getId();
		String idListStr = (String) cacheService.get(CacheConstant.REGION_ONEHOUR, key);
		List<Long> itemIdList = null;
		if(StringUtils.isBlank(idListStr)) return showJsonError(model, "没有收集场次信息！");
		itemIdList = BeanUtil.getIdList(idListStr, ",");
		itemIdList.remove(mpid);
		idListStr = StringUtils.join(itemIdList.toArray(), ",");
		cacheService.set(CacheConstant.REGION_ONEHOUR, key, idListStr);
		return showJsonSuccess(model, idListStr);
	}
	@RequestMapping("/admin/ticket/mpi/getLastChange.xhtml")
	public String getLastChange(Long mpid, ModelMap model) {
		String key = OpiConstant.getLastChangeKey(mpid);
		String lastChange = (String) cacheService.get(CacheConstant.REGION_HALFDAY, key);
		if(StringUtils.isBlank(lastChange)){
			lastChange = "信息已超时，请查询历史";
		}
		return showJsonSuccess(model, lastChange);
	}
	
	@RequestMapping("/admin/ticket/mpi/delAllMpidGather.xhtml")
	public String delOpiGather(String mpids, ModelMap model){
		User user = getLogonUser();
		if(StringUtils.isBlank(mpids)){
			return showJsonError(model, "请传入要删除的场次！");
		}
		String[] mpIds = StringUtils.split(mpids, ",");
		String key = CacheConstant.KEY_OPIGATHER + user.getId();
		String idListStr = (String) cacheService.get(CacheConstant.REGION_ONEHOUR, key);
		List<Long> itemIdList = null;
		if(StringUtils.isBlank(idListStr)) return showJsonError(model, "没有收集场次信息！");
		itemIdList = BeanUtil.getIdList(idListStr, ",");
		for(String mpId : mpIds){
			itemIdList.remove(Long.parseLong(mpId));
		}
		idListStr = StringUtils.join(itemIdList.toArray(), ",");
		cacheService.set(CacheConstant.REGION_ONEHOUR, key, idListStr);
		return showJsonSuccess(model, idListStr);
	}
}
