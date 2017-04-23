package com.gewara.web.servlet;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.AsyncContext;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import com.gewara.Config;
import com.gewara.model.ticket.OpenPlayItem;
import com.gewara.service.DaoService;
import com.gewara.support.ErrorCode;
import com.gewara.untrans.AsynchTask;
import com.gewara.untrans.AsynchTaskProcessor;
import com.gewara.untrans.AsynchTaskService;
import com.gewara.untrans.AttackTestService;
import com.gewara.untrans.RequestAsynchTask;
import com.gewara.untrans.ticket.TicketOperationService;
import com.gewara.util.BaseWebUtils;
import com.gewara.util.GewaLogger;
import com.gewara.util.HttpResult;
import com.gewara.util.HttpResultCallback;
import com.gewara.util.LoggerUtils;
@WebServlet(urlPatterns = "/asynch/preProcess", asyncSupported = true)
public class AsynchProcessServlet extends HttpServlet {
	//Apache额外增加，用于动态映射
	public static final String ORIGINAL_URI_PARAM_NAME = "_asynch_forward_uri";
	private static final long serialVersionUID = 7573371519990791640L;
	private final transient GewaLogger dbLogger = LoggerUtils.getLogger(getClass(), Config.getServerIp(), Config.SYSTEMID);
	private static final String TASK_TYPE= "qryLockSeat";
	private static final int TIME_OUT = 56000;

	private AsynchTaskService asynchTaskService;
	private AttackTestService attackTestService;
	private TicketOperationService ticketOperationService;
	private DaoService daoService;

	Map<Long/*cinemaId*/, String> channelMap = new HashMap<Long, String>();
	
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		if(attackTestService.checkBlack(req, resp)){
			resp.sendError(400);
			return;
		}
		//判断是否和场次有关
		String forward = req.getParameter(ORIGINAL_URI_PARAM_NAME);
		String mpid = req.getParameter("mpid");
		if(StringUtils.isBlank(mpid)){
			req.getRequestDispatcher(forward).forward(req, resp);
			return;
		}
		
		AsyncContext ctx = req.startAsync();
		ctx.setTimeout(TIME_OUT);
		RequestAsynchTask task = new RequestAsynchTask(TASK_TYPE, mpid, 30, ctx, true);
		task.setSuccessForward(forward);
		task.setFailureForward(forward);
		asynchTaskService.addTask(task);
		return;
	}
	
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		doGet(req, resp);
	}

	@Override
	public void init(ServletConfig servletConfig) throws ServletException {
		ServletContext servletContext = servletConfig.getServletContext();
		WebApplicationContext ctx = WebApplicationContextUtils.getWebApplicationContext(servletContext);
		asynchTaskService = ctx.getBean("asynchTaskService", AsynchTaskService.class);
		asynchTaskService.registerTaskProcessor(TASK_TYPE, new ApiCallTaskProcessor());
		ticketOperationService  = ctx.getBean("ticketOperationService", TicketOperationService.class);
		daoService = ctx.getBean("daoService", DaoService.class);
		attackTestService = ctx.getBean(AttackTestService.class);
	}

	/**
	 * 异步Servlet请求处理
	 * @author gebiao(ge.biao@gewara.com)
	 * @since Apr 9, 2013 2:34:31 PM
	 */
	public class ApiCallTaskProcessor implements AsynchTaskProcessor<RequestAsynchTask>{
		@Override
		public void processTask(RequestAsynchTask task) {
			final AsyncContext actx = task.getCtx();
			final String forward = task.getSuccessForward();
			try {
				if(task.isTimeout()){//超时
					processTimeout(task);
				}else{
					Long mpid = Long.valueOf(task.getTaskUkey());
					OpenPlayItem opi = daoService.getObjectByUkey(OpenPlayItem.class, "mpid", mpid, true);
					if(opi==null || opi.hasGewara() || opi.isExpired()){
						actx.dispatch(forward);
					}else{
						ErrorCode<List<String>> cachedSeat = ticketOperationService.getCachedRemoteLockSeat(opi, 111);
						if(cachedSeat.isSuccess()){
							actx.dispatch(forward);
						}else{
							ticketOperationService.asynchUpdateSeatLock(opi, false, new HttpResultCallback(){
								@Override
								public void processResult(HttpResult result) {
									actx.dispatch(forward);
								}
							});
						}
					}
				}
			} catch (Exception e) {
				dbLogger.warn("", e);
				actx.dispatch(forward);
			}
		}
	
		private void processTimeout(AsynchTask task) {
			AsyncContext actx = ((RequestAsynchTask)task).getCtx();
			try{
				BaseWebUtils.writeJsonResponse((HttpServletResponse) actx.getResponse(), false, "请求超时！");
			}catch (Exception e) {
			}finally{
				try{actx.complete();}catch(Exception e){
					dbLogger.warn("", e);
				}
			}
		}

		@Override
		public int getLockSize() {
			return 10000;
		}
	}

}
