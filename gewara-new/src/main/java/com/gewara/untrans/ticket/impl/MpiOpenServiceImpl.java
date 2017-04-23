package com.gewara.untrans.ticket.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import com.gewara.Config;
import com.gewara.constant.AdminCityContant;
import com.gewara.constant.sys.LogTypeConstant;
import com.gewara.constant.sys.MongoData;
import com.gewara.constant.ticket.OpiConstant;
import com.gewara.helper.sys.CachedScript;
import com.gewara.helper.sys.ScriptEngineUtil;
import com.gewara.helper.ticket.AutoSetterHelper;
import com.gewara.json.TempRoomSeat;
import com.gewara.model.movie.MoviePlayItem;
import com.gewara.model.ticket.AutoSetter;
import com.gewara.model.ticket.OpenPlayItem;
import com.gewara.mongo.MongoService;
import com.gewara.service.DaoService;
import com.gewara.service.ticket.OpenPlayService;
import com.gewara.service.ticket.OpiManageService;
import com.gewara.support.ErrorCode;
import com.gewara.untrans.AsynchTask;
import com.gewara.untrans.AsynchTaskProcessor;
import com.gewara.untrans.AsynchTaskService;
import com.gewara.untrans.NosqlService;
import com.gewara.untrans.PlayItemRefreshService;
import com.gewara.untrans.monitor.MonitorService;
import com.gewara.untrans.monitor.RoleTag;
import com.gewara.untrans.ticket.MpiOpenService;
import com.gewara.untrans.ticket.RemoteTicketService;
import com.gewara.untrans.ticket.TempRoomSeatService;
import com.gewara.untrans.ticket.TicketOperationService;
import com.gewara.util.BeanUtil;
import com.gewara.util.DateUtil;
import com.gewara.util.GewaLogger;
import com.gewara.util.LoggerUtils;
import com.gewara.util.StringUtil;
import com.gewara.xmlbind.ticket.MpiSeat;

@Service("mpiOpenService")
public class MpiOpenServiceImpl implements MpiOpenService, InitializingBean{
	private final transient GewaLogger dbLogger = LoggerUtils.getLogger(getClass(), Config.getServerIp(), Config.SYSTEMID);
	@Autowired@Qualifier("asynchTaskService4Job")
	private AsynchTaskService asynchTaskService4Job;

	@Autowired@Qualifier("asynchTaskService4Opi")
	private AsynchTaskService asynchTaskService4Opi;
	
	@Autowired@Qualifier("mongoService")
	private MongoService mongoService;
	
	@Autowired@Qualifier("jdbcTemplate")
	private JdbcTemplate jdbcTemplate;

	@Autowired@Qualifier("tempRoomSeatService")
	private TempRoomSeatService tempRoomSeatService;

	@Autowired@Qualifier("opiManageService")
	private OpiManageService opiManageService;
	
	@Autowired@Qualifier("remoteTicketService")
	private RemoteTicketService remoteTicketService;
	
	@Autowired@Qualifier("ticketOperationService")
	private TicketOperationService ticketOperationService;
	
	@Autowired@Qualifier("monitorService")
	private MonitorService monitorService;

	@Autowired@Qualifier("openPlayService")
	private OpenPlayService openPlayService;
	@Autowired@Qualifier("nosqlService")
	private NosqlService nosqlService;

	@Autowired@Qualifier("daoService")
	private DaoService daoService;

	@Autowired
	@Qualifier("playItemRefreshService")
	private PlayItemRefreshService playItemRefreshService;
	/**
	 * 后台开放场次
	 * @param opentype
	 * @param userid
	 * @param mpid
	 * @param tempid
	 * @param setterId
	 */
	private ErrorCode openMpi(MoviePlayItem mpi, Long userid, String opentype, String tempid){
		String msg = "开放场次错误：mpid=" + mpi.getId() + ",opentype=" + opentype + ",userid=" + userid + ",error:";
		boolean error = false;
		TempRoomSeat tempRoomSeat = null;
		if(StringUtils.isNotBlank(tempid)){
			tempRoomSeat = mongoService.getObject(TempRoomSeat.class, MongoData.DEFAULT_ID_NAME, tempid);
		}
		OpenPlayItem opi = null;
		if(OpiConstant.hasPartner(opentype)){
			ErrorCode<OpenPlayItem> code = null;
			if(StringUtils.equals(opentype, OpiConstant.OPEN_PNX)){
				ErrorCode<List<MpiSeat>> seatList = remoteTicketService.getMpiSeat(mpi);
				code = opiManageService.openPnxMpi(mpi, userid, seatList.getRetval());
			}else if(StringUtils.equals(opentype, OpiConstant.OPEN_WD)){
				code = opiManageService.openWdMpi(mpi, userid);
			}else{
				code = opiManageService.openMpi(mpi, userid, opentype, tempRoomSeat);
			}
			if(!code.isSuccess()){
				msg += code.getMsg();
				error = true;
			}else{//进行第二步
				opi = code.getRetval();
				asynchUpdateOpiStats(opi, false);
			}
		}else if(StringUtils.equals(OpiConstant.OPEN_GEWARA, opentype)){
			ErrorCode<OpenPlayItem> code = opiManageService.openMpi(mpi, userid, OpiConstant.OPEN_GEWARA, tempRoomSeat);
			if(!code.isSuccess()){
				msg += code.getMsg();
				error = true;
			}else{//进行第二步
				opi = code.getRetval();
				try{
					List<String> hfhLockList = new ArrayList<String>();
					opiManageService.updateOpiStats(opi.getId(), hfhLockList, false);
				}catch(Exception e){
					dbLogger.warn("开放场次座位出现错误,mpid=" + mpi.getId(), e);
					msg += ",exception:" + StringUtil.getExceptionTrace(e, 10);
					error = true;
				}
			}
		}else if(StringUtils.equals(OpiConstant.OPERATION_DISCARD, opentype)){
			try{
				ErrorCode<OpenPlayItem> code = opiManageService.discardOpiByMpid(mpi.getId(), userid);
				if(!code.isSuccess()){
					msg += "废弃场次出错：" + code.getMsg();
					error = true;
				}else{
					monitorService.saveDelLog(userid, mpi.getId(), code.getRetval());
				}
			}catch(Exception e){
				dbLogger.warn("", e);
			}
		}else{
			error = true;
			msg += "开放类型错误";
		}
		if(error){
			return ErrorCode.getFailure(msg);
		}else{
			return ErrorCode.SUCCESS;
		}
	}
	@Override
	public void asynchAutoOpenMpiList(List<MoviePlayItem> mpiList, AutoSetter setter){
		Map<String,String> map = nosqlService.getAutoSetterLimit();
		long userId = 0L;
		if(setter.getCheckUser() != null){
			userId = setter.getCheckUser();
		}
		CachedScript limitCs = null;
		if(StringUtils.isNotBlank(setter.getLimitScript())){
			limitCs = ScriptEngineUtil.buildCachedScript(setter.getLimitScript(), true);
		}
		for(MoviePlayItem mpi: mpiList){
			if(StringUtils.isNotBlank(mpi.getSeqNo())){
				if(AutoSetterHelper.isMatch(setter, mpi, map,limitCs)){
					asynchAutoOpenMpi(mpi,userId, setter);
				}
			}
		}
	}

	@Override
	public void asynchAutoOpenMpiList(List<MoviePlayItem> mpiList){
		Map<Long/*cinemaid*/, List<MoviePlayItem>> mpiListMap = BeanUtil.groupBeanList(mpiList, "cinemaid");
		Map<String,String> map = nosqlService.getAutoSetterLimit();
		for(Long cinemaid: mpiListMap.keySet()){
			List<AutoSetter> setterList = openPlayService.getValidSetterList(cinemaid,AutoSetter.STATUS_OPEN_A);
			List<AutoSetter> handSetterList = openPlayService.getValidSetterList(cinemaid,AutoSetter.STATUS_OPEN);//手动设置器，如果有符合手动的就不进行自动开放。
			List<MoviePlayItem> tmpList = mpiListMap.get(cinemaid);
			if(tmpList!=null){
				for(MoviePlayItem mpi: tmpList){
					if(StringUtils.isNotBlank(mpi.getSeqNo())){
						CachedScript limitCs = null;
						boolean isHand = false;
						for(AutoSetter setter: handSetterList){
							if(StringUtils.isBlank(setter.getMovies()) || !BeanUtil.getIdList(setter.getMovies(), ",").contains(mpi.getMovieid())){
								continue;
							}
							if(StringUtils.isNotBlank(setter.getLimitScript())){
								limitCs = ScriptEngineUtil.buildCachedScript(setter.getLimitScript(), true);
							}
							if(AutoSetterHelper.isMatch(setter, mpi, map,limitCs)){
								isHand = true;
								break;
							}
						}
						if(isHand){
							continue;
						}
						for(AutoSetter setter: setterList){
							if(StringUtils.isNotBlank(setter.getLimitScript())){
								limitCs = ScriptEngineUtil.buildCachedScript(setter.getLimitScript(), true);
							}
							if(AutoSetterHelper.isMatch(setter, mpi, map, limitCs)){
								long userId = 0L;
								if(setter.getCheckUser() != null){
									userId = setter.getCheckUser();
								}
								asynchAutoOpenMpi(mpi,userId, setter);
								break;
							}
						}
					}
				}
			}
		}
	}
	private ErrorCode autoOpenMpi(MoviePlayItem mpi, Long userid, AutoSetter setter){
		if(StringUtils.equals(mpi.getOpentype(), OpiConstant.OPEN_PNX)){
			return ErrorCode.getFailure("东票场次，不能自动！");
		}
		OpenPlayItem opi = null;
		String msg = "AutoOpen场次错误：mpid=" + mpi.getId() + ",error:";
		boolean error = false;
		try{
			//自动设置 mpid=15966411,tempid=null,setterId=102764885
			TempRoomSeat tempRoomSeat = null;
			if(StringUtils.isNotBlank(setter.getSeatmap())){
				tempRoomSeat = tempRoomSeatService.getRoomSeat(mpi.getRoomid(), setter.getSeatmap());
			}
			//第1步：价格等设置
			ErrorCode<OpenPlayItem> result = opiManageService.autoOpenMpi(mpi, setter, tempRoomSeat, userid);
			
			if(result.isSuccess()){
				opi = result.getRetval();
				//第2步
				try{
					ErrorCode<List<String>> remoteLockList = ticketOperationService.updateRemoteLockSeat(opi, OpiConstant.SECONDS_SHOW_SEAT, false);
					List<String> hfhLockList = new ArrayList<String>();
					if(remoteLockList.isSuccess()){
						hfhLockList = remoteLockList.getRetval();
					}
					opiManageService.updateOpiStats(opi.getId(), hfhLockList, false);
				}catch(Exception e){
					dbLogger.warn("开放场次座位出现错误,mpid=" + mpi.getId(), e);
					msg += ",exception:" + StringUtil.getExceptionTrace(e, 10);
				}
				monitorService.saveAddLog(userid, OpenPlayItem.class, opi.getMpid(), opi);
				//第3步
			}
		}catch(Exception e){
			dbLogger.warn(userid + "," + mpi.getId() + "," + setter.getId(), e);
		}
		if(opi!=null){
			msg += "城市："+AdminCityContant.allcityMap.get(opi.getCitycode())+",影院："+opi.getCinemaname();
		}
		if(opi!=null && opi.getAsellnum()<=0){
			monitorService.saveSysWarn("开场次可能有问题，允许卖出座位为0，请调整！mpid=" + opi.getMpid(), OpiConstant.getFullDesc(opi), RoleTag.dingpiao);
		}
		if(error){
			return ErrorCode.getFailure(msg);
		}else{
			return ErrorCode.SUCCESS;
		}
	}
	private final String TASK_UPDATEOPI = "updateOpi";
	private final String TASK_OPENMPI = "openMpi";
	private int successFinished = 0, failureUpdate = 0;
	@Override
	public void asynchUpdateOpiStats(OpenPlayItem opi, boolean isFinished) {
		AsynchTask task = new AsynchTask(TASK_UPDATEOPI, ""+opi.getMpid(), 600, true);
		task.addInfo("mpid", opi.getMpid());
		task.addInfo("isFinished", isFinished);
		task.addInfo("opi", opi);
		asynchTaskService4Opi.addTask(task);
	}
	@Override
	public ErrorCode asynchOpenMpi(MoviePlayItem mpi, Long userid, String opentype, String tempid) {
		AsynchTask task = new AsynchTask(TASK_OPENMPI, ""+mpi.getId(), 600, true);
		task.addInfo("mpi", mpi);
		task.addInfo("userid", userid);
		task.addInfo("opentype", opentype);
		task.addInfo("tempid", tempid);
		asynchTaskService4Job.addTask(task);
		return ErrorCode.SUCCESS;
	}
	
	@Override
	public ErrorCode asynchAutoOpenMpi(MoviePlayItem mpi, Long userid, AutoSetter setter) {
		if(OpiConstant.OPEN_PNX.equals(mpi.getOpentype())){
			return ErrorCode.getFailure("东票场次暂不支持！");
		}
		AsynchTask task = new AsynchTask(TASK_OPENMPI, ""+mpi.getId(), 600, true);
		task.addInfo("mpi", mpi);
		task.addInfo("userid", userid);
		task.addInfo("setter", setter);
		asynchTaskService4Job.addTask(task);
		return ErrorCode.SUCCESS;
	}
	@Override
	public void refreshMpiRelatePage(List<OpenPlayItem> opiList) {
		if (CollectionUtils.isEmpty(opiList))
			return;
		Map<String, OpenPlayItem> distinctOpi = new HashMap<String, OpenPlayItem>();
		for(OpenPlayItem opi: opiList)
			distinctOpi.put(opi.getCitycode() + "," + opi.getCinemaid() + "," 
											+ opi.getMovieid() + "," + DateUtil.formatDate(opi.getPlaytime()),
											opi);
		while (distinctOpi.values().iterator().hasNext()){
			refreshMpiRelatePage(distinctOpi.values().iterator().next());
		}
	}

	@Override
	public ErrorCode<List<MpiSeat>> refreshOpiSeat(Long mpid, Long userid, boolean refresh, final List<String> msgList){
		MoviePlayItem mpi = daoService.getObject(MoviePlayItem.class, mpid);
		ErrorCode<List<MpiSeat>> seatList = remoteTicketService.getMpiSeat(mpi);
		//错误或不刷新直接返回
		if(!seatList.isSuccess() || !refresh) return seatList;
		ErrorCode<OpenPlayItem> code = opiManageService.refreshOpiSeat(mpid, userid, seatList.getRetval());
		if(!code.isSuccess()) return ErrorCode.getFailure(code.getErrcode(), code.getMsg());
		OpenPlayItem opi = code.getRetval();
		openPlayService.refreshOpenSeatList(mpid);
		String update = "UPDATE WEBDATA.OPEN_SEAT T SET RECORDID=(SELECT RECORDID FROM WEBDATA.SELLSEAT S WHERE T.SEATLINE=S.SEATLINE AND T.SEATRANK=S.SEATRANK AND T.MPID=S.MPID) " +
				"WHERE T.MPID= ? AND EXISTS(SELECT RECORDID FROM WEBDATA.SELLSEAT S WHERE T.SEATLINE=S.SEATLINE AND T.SEATRANK=S.SEATRANK AND T.MPID=S.MPID)";
		int sell = jdbcTemplate.update(update, mpid);
		msgList.add("刷新场次锁定或卖出座位ID:" + sell + "个！");
		ErrorCode<List<String>> remoteLockList = ticketOperationService.updateRemoteLockSeat(opi, OpiConstant.SECONDS_SHOW_SEAT, false);
		List<String> hfhLockList = new ArrayList<String>();
		if(remoteLockList.isSuccess()){
			hfhLockList = remoteLockList.getRetval();
		}
		opiManageService.updateOpiStats(opi.getId(), hfhLockList, false);
		return seatList;
	}
	
	@Override
	public void refreshMpiRelatePage(OpenPlayItem opi) {
		if (opi == null)
			return;
		if(opi.isOrder()){
			Map<String, String> params = new HashMap<String, String>();
			params.put("fyrq", DateUtil.formatDate(opi.getPlaytime()));
			params.put("cid", opi.getCinemaid().toString());
			params.put("movieid", opi.getMovieid().toString());
			playItemRefreshService.clearOrderedPageCache(params, opi.getCitycode());
			
		}
	}

	private class UpdateOpiProcessor implements AsynchTaskProcessor {
		@Override
		public void processTask(AsynchTask task) {
			OpenPlayItem opi = (OpenPlayItem) task.getInfo("opi");
			Boolean isFinished = (Boolean) task.getInfo("isFinished");
			ErrorCode<List<String>> remoteLockList = ticketOperationService.updateRemoteLockSeat(opi, OpiConstant.SECONDS_ADDORDER, true);
			if (remoteLockList.isSuccess()) {
				opiManageService.updateOpiStats(opi.getId(), remoteLockList.getRetval(), isFinished);
				if (isFinished) {
					successFinished ++;
					if (successFinished % 100 == 0) {
						dbLogger.warnWithType(LogTypeConstant.LOG_TYPE_JOB, "统计影院座位，成功结束" + successFinished + "个场次, 失败结束：" + failureUpdate);
					}
				}
			} else if (isFinished) {
				failureUpdate++;
			}
			
		}

		@Override
		public int getLockSize() {
			return 5000;
		}
	}
	
	private class OpenMpiProcessor implements AsynchTaskProcessor {
		@Override
		public void processTask(AsynchTask task) {
			MoviePlayItem mpi = (MoviePlayItem) task.getInfo("mpi");
			Long userid = (Long) task.getInfo("userid");
			String opentype = (String) task.getInfo("opentype");
			String tempid = (String) task.getInfo("tempid");
			AutoSetter setter = (AutoSetter) task.getInfo("setter");
			if(setter!=null){
				autoOpenMpi(mpi, userid, setter);
			}else{
				ErrorCode result = openMpi(mpi, userid, opentype, tempid);
				if(!result.isSuccess()){
					dbLogger.warn(result.getMsg());
				}
				
			}
		}
		@Override
		public int getLockSize() {
			return 1000;
		}
	}
	@Override
	public void afterPropertiesSet() throws Exception {
		asynchTaskService4Opi.registerTaskProcessor(TASK_UPDATEOPI, new UpdateOpiProcessor());
		asynchTaskService4Job.registerTaskProcessor(TASK_OPENMPI, new OpenMpiProcessor());
	}
}
