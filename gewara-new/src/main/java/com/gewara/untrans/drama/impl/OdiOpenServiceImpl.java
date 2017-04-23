package com.gewara.untrans.drama.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.stereotype.Service;

import com.gewara.Config;
import com.gewara.constant.ApiConstant;
import com.gewara.constant.OdiConstant;
import com.gewara.constant.Status;
import com.gewara.model.drama.Drama;
import com.gewara.model.drama.DramaPlayItem;
import com.gewara.model.drama.OpenDramaItem;
import com.gewara.model.drama.OpenTheatreSeat;
import com.gewara.model.drama.Theatre;
import com.gewara.model.drama.TheatreField;
import com.gewara.model.drama.TheatreProfile;
import com.gewara.model.drama.TheatreRoom;
import com.gewara.model.drama.TheatreSeatArea;
import com.gewara.model.drama.TheatreSeatPrice;
import com.gewara.service.DaoService;
import com.gewara.service.OrderException;
import com.gewara.service.drama.DpiManageService;
import com.gewara.service.drama.OpenDramaService;
import com.gewara.service.drama.synch.TheatreSynchService;
import com.gewara.support.ErrorCode;
import com.gewara.untrans.AsynchTask;
import com.gewara.untrans.AsynchTaskProcessor;
import com.gewara.untrans.AsynchTaskService;
import com.gewara.untrans.drama.OdiOpenService;
import com.gewara.untrans.drama.RemoteTheatreService;
import com.gewara.untrans.drama.TheatreOperationService;
import com.gewara.util.BeanUtil;
import com.gewara.util.ChangeEntry;
import com.gewara.util.DateUtil;
import com.gewara.util.GewaLogger;
import com.gewara.util.LoggerUtils;

@Service("odiOpenService")
public class OdiOpenServiceImpl implements OdiOpenService, InitializingBean {
	
	protected final transient GewaLogger dbLogger = LoggerUtils.getLogger(getClass(), Config.getServerIp(), Config.SYSTEMID);
	
	@Autowired@Qualifier("daoService")
	private DaoService daoService;
	
	@Autowired@Qualifier("openDramaService")
	private OpenDramaService openDramaService;
	
	@Autowired@Qualifier("theatreSynchService")
	private TheatreSynchService theatreSynchService;
	
	@Autowired@Qualifier("remoteTheatreService")
	private RemoteTheatreService remoteTheatreService;
	
	@Autowired@Qualifier("dpiManageService")
	private DpiManageService dpiManageService;
	
	@Autowired@Qualifier("theatreOperationService")
	private TheatreOperationService theatreOperationService;
	
	@Autowired@Qualifier("hibernateTemplate")
	private HibernateTemplate hibernateTemplate;
	
	@Autowired@Qualifier("asynchTaskService4Job")
	private AsynchTaskService asynchTaskService4Job;
	
	@Override
	public ErrorCode refreshAreaSeat(Long userid, OpenDramaItem odi, TheatreSeatArea seatArea, boolean refresh, List<String> msgList) {
		if(odi.hasGewara()){
			return refresnAreaSeatGewa(userid, odi, seatArea, refresh, msgList);
		}else{
			return remoteTheatreService.refreshAreaSeat(userid, odi, seatArea, refresh, msgList);
		}
	}

	private ErrorCode refresnAreaSeatGewa(Long userid, OpenDramaItem odi, TheatreSeatArea seatArea, boolean refresh, List<String> msgList){
		if(!odi.hasGewara()) return ErrorCode.getFailure("非格瓦拉场次！");
		TheatreField field = daoService.getObject(TheatreField.class, odi.getRoomid());
		if(field==null) return ErrorCode.getFailure("无放映厅，不能开放！");
		if(!refresh){
			List<OpenTheatreSeat> seatList = daoService.getObjectListByField(OpenTheatreSeat.class, "areaid", seatArea.getId());
			if(!seatList.isEmpty()) return ErrorCode.getFailure("区域座位已存在！");
		}
		List<TheatreRoom> roomList = daoService.getObjectListByField(TheatreRoom.class, "fieldid", field.getId());
		Map<String,TheatreRoom> roomMap = BeanUtil.beanListToMap(roomList, "num");
		TheatreRoom room = roomMap.get(seatArea.getRoomnum());
		ErrorCode code = openDramaService.openSeat(userid, odi, seatArea, room);
		if(code.isSuccess()) return code;
		int sell = theatreSynchService.refreshSellSeatId(seatArea);
		msgList.add("刷新场次区域锁定或卖出座位ID:" + sell + "个！");
		String seatmap = openDramaService.getTheatreSeatAreaMapStr(seatArea);
		seatArea.setSeatmap(seatmap);
		daoService.saveObject(seatArea);
		return ErrorCode.SUCCESS;
	}
	
	@Override
	public OpenDramaItem updateOdiStats(OpenDramaItem odi, int expireSeconds, boolean isFinished){
		 List<TheatreSeatArea> seatAreaList = daoService.getObjectListByField(TheatreSeatArea.class, "dpid", odi.getDpid());
		 boolean openGewa = odi.hasGewara(), flag = true;
		 int seatnum = 0, asellnum = 0, gsellnum = 0, csellnum = 0, locknum = 0;		//Gewa锁定数
		 if(odi.isOpenseat()){
			 for (TheatreSeatArea seatArea : seatAreaList) {
				 List<String> remoteList = new ArrayList<String>();
				 dpiManageService.verifySeatAreaSeatLock(seatArea.getId());
				 if(!openGewa){
					 ErrorCode<List<String>> remoteLock = theatreOperationService.updateRemoteLockSeat(seatArea, expireSeconds, true);
					 if(!remoteLock.isSuccess()){
						 flag = false;
						 dbLogger.warn("updateOdiStats: dpid:" + odi.getDpid() + ",areaid:" +seatArea.getId() + ",错误信息：" + remoteLock.getErrcode() + "," + remoteLock.getMsg());
					 }else{
						 remoteList = remoteLock.getRetval();
						 seatArea = dpiManageService.updateTheatreSeatAreaStats(odi, seatArea, remoteList, isFinished);
					 }
				 }else{
					 seatArea = dpiManageService.updateTheatreSeatAreaStats(odi, seatArea, remoteList, isFinished);
				 }
				 seatnum += seatArea.getTotal();
				 asellnum += seatArea.getLimitnum();
				 gsellnum += seatArea.getGsellnum();
				 csellnum += seatArea.getCsellnum();
				 locknum += seatArea.getLocknum();
			}
		 }else{
			 for (TheatreSeatArea seatArea : seatAreaList) {
				 List<String> remoteList = new ArrayList<String>();
				 if(!openGewa){
					 ErrorCode<List<String>> remoteLock = theatreOperationService.updateRemoteLockPrice(seatArea, expireSeconds, true);
					 if(!remoteLock.isSuccess()){
						 flag = false;
						 dbLogger.warn("updateOdiStats: dpid:" + odi.getDpid() + ",areaid:" +seatArea.getId() + ",错误信息：" + remoteLock.getErrcode() + "," + remoteLock.getMsg());
					 }else{
						 remoteList = remoteLock.getRetval();
						 seatArea = dpiManageService.updateTheatreSeatPriceStats(odi, seatArea, remoteList, isFinished);
					 }
				 }else{
					 seatArea = dpiManageService.updateTheatreSeatPriceStats(odi, seatArea, remoteList, isFinished);
				 }
				 seatnum += seatArea.getTotal();
				 asellnum += seatArea.getLimitnum();
				 gsellnum += seatArea.getGsellnum();
				 csellnum += seatArea.getCsellnum();
				 locknum += seatArea.getLocknum();
			 }
		 }
		 if(flag){
			 ChangeEntry changeEntry = new ChangeEntry(odi);
			 odi.setSeatnum(seatnum);
			 odi.setAsellnum(asellnum);
			 odi.setGsellnum(gsellnum);
			 odi.setCsellnum(csellnum);
			 odi.setLocknum(locknum);
			 if(!changeEntry.getChangeMap(odi).isEmpty()){
				 odi.setUpdatetime(DateUtil.getCurFullTimestamp());
				 daoService.saveObject(odi);
				 dbLogger.warn(BeanUtil.buildString(odi, true));
			 }
		 }
		 return odi;
	}
	
	@Override
	public void refreshDramaList(Long userid, String status){
		DetachedCriteria query = DetachedCriteria.forClass(Drama.class, "d");
		Date curDate = DateUtil.getCurDate();
		if(StringUtils.isBlank(status) || StringUtils.equals(status, Status.Y)){
			query.add(Restrictions.ge("d.enddate", curDate));
		}else{
			query.add(Restrictions.le("d.enddate", curDate));
		}
		List<Drama> dramaList = hibernateTemplate.findByCriteria(query);
		for (Drama drama : dramaList) {
			dpiManageService.refreshDramaOtherinfo(userid, drama);
		}
	}
	
	private final String TASK_UPDATEAREA = "updateArea";
	
	@Override
	public void asynchUpdateAreaStats(OpenDramaItem odi) {
		AsynchTask task = new AsynchTask(TASK_UPDATEAREA, ""+odi.getDpid(), 600, true);
		task.addInfo("dpid", odi.getDpid());
		task.addInfo("isFinished", false);
		task.addInfo("odi", odi);
		asynchTaskService4Job.addTask(task);
	}
	
	private class UpdateAreaProcessor implements AsynchTaskProcessor {
		@Override
		public void processTask(AsynchTask task) {
			OpenDramaItem odi = (OpenDramaItem) task.getInfo("odi");
			Boolean isFinished = (Boolean) task.getInfo("isFinished");
			updateOdiStats(odi, OdiConstant.SECONDS_ADDORDER, isFinished);
		}

		@Override
		public int getLockSize() {
			return 5000;
		}
	}
	@Override
	public void afterPropertiesSet() throws Exception {
		asynchTaskService4Job.registerTaskProcessor(TASK_UPDATEAREA, new UpdateAreaProcessor());
	}
	
	@Override
	public ErrorCode saveOpenDramaItem(Long userid, Long dpid)throws OrderException{
		DramaPlayItem dpi = daoService.getObject(DramaPlayItem.class, dpid);
		OpenDramaItem odi= daoService.getObjectByUkey(OpenDramaItem.class, "dpid", dpid, false);
		if(odi!=null) return ErrorCode.getFailure("该场次已经开放过，不能重复开放！");
		Theatre theatre = daoService.getObject(Theatre.class, dpi.getTheatreid());
		Drama drama = daoService.getObject(Drama.class, dpi.getDramaid());
		if(theatre==null) return ErrorCode.getFailure("话剧院不存在！");
		if(drama==null) return ErrorCode.getFailure("话剧不存在！");
		TheatreProfile profile = daoService.getObject(TheatreProfile.class, dpi.getTheatreid());
		if(profile==null) return ErrorCode.getFailure("剧院基础信息没有设置，请先设置！");
		
		String hql = "select count(*) from TheatreSeatPrice s where s.dpid=? and (costprice<=0 or price<=0 or theatreprice<=0) and status!=?";
		List list = hibernateTemplate.find(hql, dpi.getId(), Status.DEL);
		int i = Integer.valueOf(list.get(0)+"");
		if(i>0) return ErrorCode.getFailure("有价格为0，请先设置价格再开放场次");
		list = daoService.getObjectListByField(TheatreSeatPrice.class, "dpid", dpi.getId());
		if(list.isEmpty()) return ErrorCode.getFailure("还没有设置价格，请先设置价格再开放场次");
		
		if(!dpi.hasGewa()){
			return remoteTheatreService.openDramPlayitem(dpi, theatre, drama, profile);
		}else{
			return openDramaItemByGewa(userid, dpi, theatre, drama, profile);
		}
	}
	
	private ErrorCode openDramaItemByGewa(Long userid, DramaPlayItem item, Theatre theatre, Drama drama, TheatreProfile profile) throws OrderException{
		if(!item.hasGewa()) return ErrorCode.getFailure("非GEWA类型开放" + item.getSeller());
		TheatreField field = daoService.getObject(TheatreField.class, item.getRoomid());
		if(field==null) return ErrorCode.getFailure("无放映厅，不能开放！");
		List<TheatreSeatArea> seatAreaList = daoService.getObjectListByField(TheatreSeatArea.class, "dpid", item.getId());
		if(seatAreaList.isEmpty()){
			return ErrorCode.getFailure(field.getName()+ "没有区域，请先添加区域");
		}
		List<TheatreRoom> roomList = daoService.getObjectListByField(TheatreRoom.class, "fieldid", field.getId());
		Map<String,TheatreRoom> roomMap = BeanUtil.beanListToMap(roomList, "num");
		OpenDramaItem odi = new OpenDramaItem(theatre, drama, item, profile); //添加场次
		remoteTheatreService.clearOdiPreferential(odi, drama);
		daoService.saveObject(odi);
		if(item.isOpenseat()) { 
			if(item.getPlaytime()==null) return ErrorCode.getFailure("演出时间没填写！");
			for(TheatreSeatArea seatArea : seatAreaList) {
				TheatreRoom room = roomMap.get(seatArea.getRoomnum());
				ErrorCode<String> code = openDramaService.openSeat(userid, odi, seatArea, room);
				if(!code.isSuccess()){
					 throw new OrderException(ApiConstant.CODE_DATA_ERROR, code.getMsg());
				}else{
					String seatmap = openDramaService.getTheatreSeatAreaMapStr(seatArea);
					seatArea.setSeatmap(seatmap);
					daoService.saveObject(seatArea);
				}
			}
		}else if(item.isOpenprice()){
			openPrice(odi);
		}else {
			return ErrorCode.getFailure("开放类型错误！");
		}
		asynchUpdateAreaStats(odi);
		return ErrorCode.SUCCESS;
	}
	private void openPrice(OpenDramaItem odi){
		odi.setUpdatetime(DateUtil.getCurFullTimestamp());
		daoService.saveObject(odi);
	}
}
