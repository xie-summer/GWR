package com.gewara.untrans.ticket.impl;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.gewara.Config;
import com.gewara.constant.sys.LogTypeConstant;
import com.gewara.constant.ticket.OpiConstant;
import com.gewara.model.movie.MoviePlayItem;
import com.gewara.model.ticket.OpenPlayItem;
import com.gewara.service.DaoService;
import com.gewara.service.ticket.OpiManageService;
import com.gewara.support.ErrorCode;
import com.gewara.untrans.AsynchTask;
import com.gewara.untrans.AsynchTaskProcessor;
import com.gewara.untrans.AsynchTaskService;
import com.gewara.untrans.ticket.BaseOpenService;
import com.gewara.untrans.ticket.RemoteTicketService;
import com.gewara.untrans.ticket.TicketOperationService;
import com.gewara.util.DateUtil;
import com.gewara.util.GewaLogger;
import com.gewara.util.LoggerUtils;

@Service("mtxOpenService")
public class MtxOpenServiceImpl implements BaseOpenService, InitializingBean{
	private final transient GewaLogger dbLogger = LoggerUtils.getLogger(getClass(), Config.getServerIp(), Config.SYSTEMID);

	@Autowired@Qualifier("asynchTaskService4Opi")
	private AsynchTaskService asynchTaskService4Opi;

	@Autowired@Qualifier("opiManageService")
	private OpiManageService opiManageService;
	
	@Autowired@Qualifier("remoteTicketService")
	private RemoteTicketService remoteTicketService;
	
	@Autowired@Qualifier("ticketOperationService")
	private TicketOperationService ticketOperationService;

	@Autowired@Qualifier("daoService")
	private DaoService daoService;

	private final String TASK_UPDATEMTXOPI = "updateMtxOpi";
	private int successFinished = 0, failureUpdate = 0;
	@Override
	public void asynchUpdateOpiStats(OpenPlayItem opi, boolean isFinished) {
		AsynchTask task = new AsynchTask(TASK_UPDATEMTXOPI, ""+opi.getMpid(), 600, true);
		task.addInfo("mpid", opi.getMpid());
		task.addInfo("isFinished", isFinished);
		task.addInfo("opi", opi);
		asynchTaskService4Opi.addTask(task);
	}

	private class UpdateOpiProcessor implements AsynchTaskProcessor {
		@Override
		public void processTask(AsynchTask task) {
			OpenPlayItem opi = (OpenPlayItem) task.getInfo("opi");
			Boolean isFinished = (Boolean) task.getInfo("isFinished");
			if (DateUtil.getCurFullTimestamp().after(opi.getPlaytime())) {
				MoviePlayItem mpi = daoService.getObject(MoviePlayItem.class, opi.getMpid());
				if (StringUtils.equals(mpi.getOpentype(), OpiConstant.OPEN_MTX)) {
					ErrorCode<Integer> code = remoteTicketService.getRemoteMtxSellNum(mpi);
					if (code.isSuccess()) {
						opiManageService.updateOpiStatsByMtx(opi.getId(), code.getRetval(), isFinished);
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
			} else {
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
		}
		@Override
		public int getLockSize() {
			return 5000;
		}
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		asynchTaskService4Opi.registerTaskProcessor(TASK_UPDATEMTXOPI, new UpdateOpiProcessor());
	}

}
