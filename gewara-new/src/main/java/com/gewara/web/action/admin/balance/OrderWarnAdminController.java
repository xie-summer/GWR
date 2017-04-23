package com.gewara.web.action.admin.balance;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import com.gewara.constant.PaymethodConstant;
import com.gewara.constant.ticket.OrderConstant;
import com.gewara.json.OrderWarn;
import com.gewara.model.acl.User;
import com.gewara.model.pay.GewaOrder;
import com.gewara.model.pay.RepeatingPayorder;
import com.gewara.mongo.MongoService;
import com.gewara.untrans.UntransService;
import com.gewara.util.DateUtil;
import com.gewara.util.JsonUtils;
import com.gewara.util.StringUtil;
import com.gewara.web.action.admin.BaseAdminController;

@Controller
public class OrderWarnAdminController extends BaseAdminController {
	@Autowired@Qualifier("mongoService")
	private MongoService mongoService;
	public void setMongoService(MongoService mongoService) {
		this.mongoService = mongoService;
	}
	@Autowired@Qualifier("untransService")
	private UntransService untransService;
	public void setUntransService(UntransService untransService) {
		this.untransService = untransService;
	}
	
	@RequestMapping("/admin/balance/warn/importMongoRepeating.xhtml")
	public String importMongoObjToDB(ModelMap model){
		List<OrderWarn> orders = mongoService.getObjectList(OrderWarn.class);
		dbLogger.warn("orderwarn list size:" + orders.size());
		RepeatingPayorder reOrder = null;
		List<RepeatingPayorder> reOderList = new ArrayList<RepeatingPayorder>();
		for (int i = 0; i < orders.size(); i++){
			OrderWarn ow = orders.get(i);
			String successPayMethod = ow.getPaymethod1();
			String tradeno = ow.getTradeno();
			String payseqNo = StringUtil.getRandomString(10);
			String payMethod = ow.getPaymethod2();
			Integer amount = ow.getAlipaid();
			String status = ow.getStatus();
			String confirmUser = ow.getAuser();
			String addDate = ow.getAdddate();
			
			reOrder = new RepeatingPayorder(successPayMethod, tradeno, payseqNo, payMethod, amount);
			reOrder.setStatus(status);
			reOrder.setConfirmUser(confirmUser);
			reOrder.setNotifyTime(DateUtil.getBeginTimestamp(DateUtil.parseDate(addDate)));
			reOderList.add(reOrder);
		}
		daoService.saveObjectList(reOderList);
		dbLogger.warn("repeating order list size: " + reOderList.size());
		
		return showJsonSuccess(model, "" + JsonUtils.writeObjectToJson(reOderList));
	}
	
	
	@RequestMapping("/admin/balance/warn/reapeatPayList.xhtml")
	public String reapeatPayWarn(Timestamp adddate, String status, ModelMap model) {
		
		DetachedCriteria query = DetachedCriteria.forClass(RepeatingPayorder.class);
		if (adddate != null){
			query.add(Restrictions.between("notifyTime", DateUtil.getBeginningTimeOfDay(adddate), DateUtil.getEndTimestamp(adddate)));
		}
		if (StringUtils.isNotBlank(status)){
			query.add(Restrictions.eq("status", status));
		}
		
		hibernateTemplate.setMaxResults(2000);
		List<RepeatingPayorder> orders = hibernateTemplate.findByCriteria(query);
		model.put("orders", orders);
		
		model.put("paytextMap", PaymethodConstant.getPayTextMap());
		return "admin/balance/warn/reapeatPay.vm";
	}
	@RequestMapping("/admin/balance/warn/amountErrorList.xhtml")
	public String warnAmountList(Date startdate, Date enddate, ModelMap model) {
		Date date = new Date();
		if(startdate==null) startdate = DateUtil.addDay(date, -30);
		if(enddate==null) enddate = date;
		String sql = "from GewaOrder t where t.status like ? and t.addtime>=? and t.addtime<=? and (t.totalfee+t.itemfee+t.otherfee-discount)!=(gewapaid+alipaid) order by t.addtime desc";
		List<GewaOrder> orderList = hibernateTemplate.find(sql, OrderConstant.STATUS_PAID+"%", DateUtil.getBeginningTimeOfDay(startdate), DateUtil.getLastTimeOfDay(enddate));
		model.put("orderList", orderList);
		return "admin/balance/warn/warnAmountOrder.vm";
	}
	@RequestMapping("/admin/balance/warn/acceptOrderWarn.xhtml")
	public String acceptSysWarn(String id, ModelMap model){
		RepeatingPayorder reOrder = hibernateTemplate.get(RepeatingPayorder.class, id);
		if (reOrder == null)
			return showJsonError(model, "没有找到相关订单");
		User user = getLogonUser();
		reOrder.setConfirmUser(user.getId() + "@" + user.getUsername());
		reOrder.setConfirmTime(DateUtil.getCurFullTimestamp());
		reOrder.setStatus("Y");
		daoService.saveObject(reOrder);
		return showJsonSuccess(model);
	}
	@RequestMapping("/admin/balance/warn/updateOrderWarn.xhtml")
	public String updateSysWarn(String id, ModelMap model){
		OrderWarn warn = mongoService.getObject(OrderWarn.class, "id", id);
		User user = getLogonUser();
		String userKey = "@" + user.getId() + "@";
		warn.addFixed(userKey);
		warn.setStatus("Y");
		mongoService.saveOrUpdateObject(warn, "id");
		return showJsonSuccess(model);
	}
	@RequestMapping("/admin/balance/warn/importPayWarn.xhtml")
	public String importPayWarn(String warns, ModelMap model) {
		List<String> strList = Arrays.asList(StringUtils.split(warns, "\n"));
		int i = 0;
		for(String str : strList){
			String[] s = StringUtils.split(str, ",");
			if(s!=null && s.length==3){
				String tradeNo = s[0];
				String paymethod = s[2];
				GewaOrder order = daoService.getObjectByUkey(GewaOrder.class, "tradeNo", tradeNo, false);
				if(order!=null){ 
					untransService.saveOrderWarn(order, paymethod, order.getAlipaid());
					i++;
				}
			}
		}
		return showJsonSuccess(model, "成功导入:" + i);
	}
}
