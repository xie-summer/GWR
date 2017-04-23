package com.gewara.web.action.admin.balance;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import com.gewara.constant.PaymethodConstant;
import com.gewara.constant.sys.LogTypeConstant;
import com.gewara.constant.ticket.OrderConstant;
import com.gewara.model.acl.User;
import com.gewara.model.pay.AccountRecord;
import com.gewara.model.pay.Adjustment;
import com.gewara.model.pay.Charge;
import com.gewara.model.pay.CheckRecord;
import com.gewara.model.pay.GewaOrder;
import com.gewara.service.gewapay.PaymentService;
import com.gewara.support.ErrorCode;
import com.gewara.util.DateUtil;
import com.gewara.web.action.admin.BaseAdminController;
import com.gewara.web.util.PageUtil;

@Controller
public class PayVerifyAdminController extends BaseAdminController{
	@Autowired@Qualifier("paymentService")
	private PaymentService paymentService;
	public void setPaymentService(PaymentService paymentService) {
		this.paymentService = paymentService;
	}
	@RequestMapping("/admin/balance/dailySheet.xhtml")
	public String dailySheet(Integer pageNo, ModelMap model){
		if(pageNo==null) pageNo=0;
		int rowsPerPage = 30;
		int firstRow = pageNo*rowsPerPage;
		List<CheckRecord> checkRecordList = paymentService.getCheckRecordList(firstRow, rowsPerPage);
		int count = daoService.getObjectCount(CheckRecord.class);
		PageUtil pageUtil=new PageUtil(count,rowsPerPage,pageNo, "/admin/balance/dailySheet.xhtml");
		pageUtil.initPageInfo();
		model.put("pageUtil", pageUtil);
		model.put("checkRecordList", checkRecordList);
		return "admin/balance/dailySheet.vm";
	}
	@RequestMapping("/admin/balance/settleAccount.xhtml")
	public String settleAccount(ModelMap model){
		final User user = getLogonUser();
		boolean isAllow = operationService.updateOperation("closeCount", 60 * 40);
		if(!isAllow){
			return showError(model, "结账40分钟内不允许第二次结账，请10-20分钟后查看结果后再进一步操作！");
		}
		final CheckRecord lastCheckRecord = paymentService.getLastCheckRecord();
		String msg = "后台正在结账，请过10-20分钟查看结果！";
		final boolean checklast = lastCheckRecord.getStatus().compareTo(CheckRecord.STATUS_STEP2)<0;
		if(checklast) {
			msg = "上次的账单未结算完成，断续结算上次的！";
		}
		Thread t = new Thread(new Runnable(){
			@Override
			public void run() {
				try{
					dbLogger.warnWithType(LogTypeConstant.LOG_TYPE_ORDER_PAY, "开始结账...");
					CheckRecord curCheck = null;
					if(checklast){
						curCheck = lastCheckRecord;
					}else{
						ErrorCode<CheckRecord> code = paymentService.closeAccountStep1(user.getId());
						if(code.isSuccess()){
							curCheck = code.getRetval();
						}else{
							dbLogger.warnWithType(LogTypeConstant.LOG_TYPE_ORDER_PAY, "结账失败！");
						}
					}
					paymentService.closeAccountStep2(curCheck);
				}catch(Exception e){
					dbLogger.warnWithType(LogTypeConstant.LOG_TYPE_ORDER_PAY, "结账出现错误！", e);
				}
			}
		});
		t.setDaemon(true);
		t.start();
		return showError(model, msg);
	}
	@RequestMapping("/admin/balance/checkDetail.xhtml")
	public String checkDetail(Long mid, Long checkid, ModelMap model){
		CheckRecord checkRecord = daoService.getObject(CheckRecord.class, checkid);
		AccountRecord arecord = paymentService.getAccountRecord(checkid, mid);
		List<GewaOrder> orderList = paymentService.getPaidOrderList(mid, checkRecord.getFromtime(), checkRecord.getChecktime());
		List<Charge> chargeList = paymentService.getChargeList(mid, checkRecord.getFromtime(), checkRecord.getChecktime(), Charge.STATUS_PAID);
		List<Adjustment> adjustmentList = paymentService.getAdjustmentList(mid, checkRecord.getFromtime(), checkRecord.getChecktime(), Adjustment.STATUS_SUCCESS);
		model.put("checkRecord", checkRecord);
		model.put("arecord", arecord);
		model.put("orderList", orderList);
		model.put("chargeList", chargeList);
		model.put("adjustmentList", adjustmentList);
		return "admin/balance/memberCheckDetail.vm";
	}

	@RequestMapping("/admin/balance/queryOrder.xhtml")
	public String queryOrder(Long checkid, Date date, Integer pageNo, ModelMap model){
		Timestamp starttime = null, endtime = null;
		if(checkid != null){
			CheckRecord checkRecord = daoService.getObject(CheckRecord.class, checkid);
			starttime = checkRecord.getFromtime();
			endtime = checkRecord.getChecktime();
			model.put("checkRecord", checkRecord);
		}else{
			starttime = DateUtil.getBeginningTimeOfDay(new Timestamp(date.getTime()));
			endtime = DateUtil.getLastTimeOfDay(starttime);
		}
		int count = paymentService.getPaidOrderCount(starttime, endtime);
		if(pageNo==null) pageNo=0;
		int rowsPerPage = 100;
		Map params = new HashMap();
		params.put("date", new String[]{DateUtil.formatDate(date)});
		PageUtil pageUtil = new PageUtil(count, rowsPerPage, pageNo, "admin/balance/queryOrder.xhtml");
		pageUtil.initPageInfo(params);
		model.put("pageUtil", pageUtil);
		model.put("rowsCount", count);
		
		String query = "select new map(sum(totalfee) as totalAmount, sum(gewapaid + alipaid) as totalpaid, sum(gewapaid) as gewapaid, " +
				"sum(alipaid) as alipaid, sum(discount) as discount, count(id) as totalcount, sum(quantity) as quantity) " +
				"from GewaOrder where status like ? and paidtime >= ? and paidtime < ? ";
		List<Map> result = hibernateTemplate.find(query, OrderConstant.STATUS_PAID + "%", starttime, endtime);
		model.put("statMap", result.get(0));
		List<GewaOrder> orderList = paymentService.getPaidOrderList(starttime, endtime, pageNo * rowsPerPage, rowsPerPage);
		model.put("orderList", orderList);
		return "admin/balance/orderList.vm";
	}
	@RequestMapping("/admin/balance/cashReport.xhtml")
	public String cashReport(HttpServletResponse response, ModelMap model, String format, Date timeFrom, Date timeTo){
		if(timeFrom==null || timeTo==null) return "admin/balance/cashReport.vm";
		model.put("paymethodList", PaymethodConstant.PAYMETHOD_LIST);
		model.put("paytextMap", PaymethodConstant.getPayTextMap());
		if(DateUtil.addDay(timeFrom, 15).before(timeTo)) timeTo = DateUtil.addDay(timeFrom, 30);
		model.put("timeFrom", DateUtil.formatDate(timeFrom));
		model.put("timeTo", DateUtil.formatDate(timeTo));
		Set<String> notEmptyPaymethod = new HashSet<String>();
		String orderQuery = "select new map(sum(alipaid+gewapaid) as amount, to_char(paidtime,'yyyy-mm-dd') as paydate, paymethod as paymethod) from GewaOrder " +
				"where paidtime > ? and paidtime < ? and status like 'paid%' " +
				"group by to_char(paidtime,'yyyy-mm-dd'), paymethod";
		Timestamp from = DateUtil.getBeginningTimeOfDay(new Timestamp(timeFrom.getTime()));
		Timestamp to = DateUtil.getLastTimeOfDay(new Timestamp(timeTo.getTime()));
		List<Map> orderGroupList = hibernateTemplate.find(orderQuery, from, to);
		Map<String/*day*/, Map<String/*paymethod*/, Map/*order,charge*/>> statsMap = new HashMap<String, Map<String, Map>>();
		for(Map row: orderGroupList){
			Map<String, Map> dayMap = statsMap.get(row.get("paydate"));
			if(dayMap==null){
				dayMap = new HashMap();
				statsMap.put((String) row.get("paydate"), dayMap);
			}
			Map orderChargeMap = new HashMap();
			orderChargeMap.put("order", row.get("amount"));
			dayMap.put((String) row.get("paymethod"), orderChargeMap);
			notEmptyPaymethod.add((String) row.get("paymethod"));
		}
		
		String chargeQuery = "select new map(sum(totalfee) as amount, to_char(updatetime,'yyyy-mm-dd') as paydate, paymethod as paymethod) from Charge " +
				"where updatetime > ? and updatetime < ? and status like 'paid%' " +
				"group by to_char(updatetime,'yyyy-mm-dd'), paymethod";
		List<Map> chargeGroupList = hibernateTemplate.find(chargeQuery, from, to);
		for(Map row: chargeGroupList){
			Map<String, Map> dayMap = statsMap.get(row.get("paydate"));
			if(dayMap==null){
				dayMap = new HashMap();
				statsMap.put((String) row.get("paydate"), dayMap);
			}
			Map orderChargeMap = dayMap.get(row.get("paymethod"));
			if(orderChargeMap==null){
				orderChargeMap = new HashMap();
				dayMap.put((String) row.get("paymethod"), orderChargeMap);
			}
			orderChargeMap.put("charge", row.get("amount"));
			notEmptyPaymethod.add((String) row.get("paymethod"));
		}
		Date c = timeFrom;
		List<String> dayList = new ArrayList<String>();
		while(c.before(timeTo)){
			dayList.add(DateUtil.formatDate(c));
			c = DateUtil.addDay(c, 1);
		}
		dayList.add(DateUtil.formatDate(timeTo));
		model.put("dayList", dayList);
		model.put("validList", notEmptyPaymethod);
		model.put("statsMap", statsMap);
		if("xls".equals(format)){
			download("xls", response);
		}
		return "admin/balance/cashReport.vm";
	}
}
