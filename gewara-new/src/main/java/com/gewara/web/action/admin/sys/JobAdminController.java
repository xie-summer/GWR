package com.gewara.web.action.admin.sys;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.gewara.job.DataCleanerJob;
import com.gewara.job.PageCacheJob;
import com.gewara.job.SendMessageJob;
import com.gewara.job.impl.EveryDayJobImpl;
import com.gewara.job.impl.TheatreSeatPriceJobImpl;
import com.gewara.job.impl.TicketOrderJobImpl;
import com.gewara.job.impl.TicketRemoteJobImpl;
import com.gewara.web.action.admin.BaseAdminController;

@Controller
public class JobAdminController extends BaseAdminController{
	
	@Autowired@Qualifier("ticketRemoteJob")
	private TicketRemoteJobImpl ticketRemoteJob;
	@Autowired@Qualifier("pageCacheJob")
	private PageCacheJob pageCacheJob;
	@Autowired@Qualifier("ticketOrderJob")
	private TicketOrderJobImpl ticketOrderJob;
	
	@Autowired@Qualifier("everyDayJob")
	private EveryDayJobImpl everyDayJob;
	@Autowired@Qualifier("sendMessageJob")
	private SendMessageJob sendMessageJob;

	@Autowired@Qualifier("dataCleanerJob")
	private DataCleanerJob dataCleanerJob;

	@Autowired@Qualifier("theatreSeatPriceJob")
	private TheatreSeatPriceJobImpl theatreSeatPriceJob;
	
	@RequestMapping("/admin/sysmgr/job/updateMoviePlayItem.xhtml")
	public String updateTicket(ModelMap model) {
		ticketRemoteJob.updateMoviePlayItem();
		return showJsonSuccess(model);
	}
	
	@RequestMapping("/admin/sysmgr/job/sendWarningMsg.xhtml")
	public String ticketOrderJob(ModelMap model){
		ticketOrderJob.sendWarningMsg();
		return showJsonSuccess(model);
	}
	@RequestMapping("/admin/sysmgr/job/cleanAll.xhtml")
	public String cleanAll(ModelMap model){
		dataCleanerJob.cleanAll();
		return showJsonSuccess(model);
	}
	
	@RequestMapping("/admin/sysmgr/job/cityprice.xhtml")
	public String everyDay(ModelMap model){
		everyDayJob.updateCityprice();
		return showJsonSuccess(model);
	}
	@RequestMapping("/admin/sysmgr/job/updateEveryWeekTicketOrder.xhtml")
	@ResponseBody
	public String updateEveryWeekTicketOrder(){
		everyDayJob.updateEveryWeekTicketOrder();
		return "start!!!";
	}
	@RequestMapping("/admin/sysmgr/job/updateClicktimes.xhtml")
	@ResponseBody
	public String handle2UpdateClickedtimes(){
		everyDayJob.updateClicktimes();
		return "start!!!";
	}
	@RequestMapping("/admin/sysmgr/job/updateRelateCount.xhtml")
	public String updateRelateCount(ModelMap model){
		sendMessageJob.updateRelateCount();
		return showJsonSuccess(model);
	}
	@RequestMapping("/admin/sysmgr/job/moviePageView.xhtml")
	@ResponseBody
	public String moviePageView() {
		pageCacheJob.refreshMoviePageView();
		return "success";
	}
	@RequestMapping("/admin/sysmgr/job/cinemaPageView.xhtml")
	@ResponseBody
	public String cinemaPageView() {
		pageCacheJob.refreshCinemaPageView();
		return "success";
	}

	@RequestMapping("/admin/sysmgr/job/updateDiaryEveryDay.xhtml")
	public String updateDiaryEveryDay(ModelMap model){
		sendMessageJob.updateDiaryEveryDay();
		return showJsonSuccess(model);
	}
	
	@RequestMapping("/admin/sysmgr/job/updatePriceAllownum.xhtml")
	public String updatePriceAllownum(ModelMap model){
		theatreSeatPriceJob.updatePriceAllownum();
		return showJsonSuccess(model);
	}
}
