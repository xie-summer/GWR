package com.gewara.web.action.admin.balance;

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import com.gewara.json.pay.ReconciliationSettle;
import com.gewara.model.pay.GewaOrder;
import com.gewara.mongo.MongoService;
import com.gewara.pay.NewPayUtil;
import com.gewara.service.order.OrderQueryService;
import com.gewara.service.pay.GatewayService;
import com.gewara.util.BeanUtil;
import com.gewara.util.CAUtil;
import com.gewara.util.DateUtil;
import com.gewara.util.HttpResult;
import com.gewara.util.HttpUtils;
import com.gewara.util.JsonUtils;
import com.gewara.util.VmUtils;
import com.gewara.web.action.admin.BaseAdminController;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
@Controller
public class PartnerQryAdminController extends BaseAdminController{
	@Autowired
	@Qualifier("mongoService")
	private MongoService mongoService;

	public void setMongoService(MongoService mongoService) {
		this.mongoService = mongoService;
	}
	@Autowired
	@Qualifier("orderQueryService")
	private OrderQueryService orderQueryService;	

	@Autowired@Qualifier("gatewayService")
	private GatewayService gatewayService;

	@RequestMapping("/admin/balance/jsbank/payReconciliation.xhtml")
	public String payReconciliation(Date startTime, Date endTime, String tradeNo, String paymethod, ModelMap model) {
		if (StringUtils.isBlank(paymethod) || ((startTime == null || endTime == null) && StringUtils.isBlank(tradeNo))) {
			model.put("errorMessage", "请按订单号或时间段进行查询");
			return "admin/balance/jsbank/payReconciliation.vm";
		}
		model.put("startTime", DateUtil.format(startTime, "yyyy-MM-dd"));
		model.put("endTime", DateUtil.format(endTime, "yyyy-MM-dd"));
		if (StringUtils.isBlank(tradeNo)) {
			long start = DateUtil.getCurDateMills(startTime);
			long end = DateUtil.getCurDateMills(endTime);
			if ((end - start) > 1000 * 60 * 60 * 24 * 7) {
				model.put("errorMessage", "时间跨度请不要超过7天时间");
				return "admin/balance/jsbank/payReconciliation.vm";
			}
		}
		DBObject params = new BasicDBObject();
		params.put("payMethod", paymethod);
		if (StringUtils.isNotBlank(tradeNo)) {
			params.put("tradeNo", tradeNo);
		} else {
			params.putAll(mongoService.queryAdvancedDBObject("addTime", new String[] { ">=", "<=" },
					new String[] { DateUtil.format(startTime, "yyyyMMddHHmmss"), DateUtil.format(endTime, "yyyyMMddHHmmss") }));
		}
		List<ReconciliationSettle> settles = mongoService.getObjectList(ReconciliationSettle.class, params);
		Map<String, ReconciliationSettle> settlesMap = BeanUtil.beanListToMap(settles, "tradeNo");
		model.put("settlesMap", settlesMap);
		List<GewaOrder> ticketorders = orderQueryService.getTicketOrderListByPayMethod(
				startTime == null ? null : DateUtil.getBeginTimestamp(startTime), endTime == null ? null : DateUtil.getBeginTimestamp(endTime),
				paymethod, tradeNo);
		model.put("ticketorders", ticketorders);
		List<String> tradeNos = BeanUtil.getBeanPropertyList(ticketorders, String.class, "tradeNo", true);
		Set settleTradenos = new HashSet();
		settleTradenos.addAll(settlesMap.keySet());
		settleTradenos.removeAll(tradeNos);
		model.put("settleTradenos", settleTradenos);
		return "admin/balance/jsbank/payReconciliation.vm";
	}

	@RequestMapping("/admin/balance/jsbank/downReconciliationFile.xhtml")
	public String downReconciliationFile(Date startTime, Date endTime, String paymethod, ModelMap model) {
		boolean isSwitch = gatewayService.isSwitch(paymethod);
		Map<String, String> paramMap = new LinkedHashMap<String, String>();
		paramMap.put("paymethod", paymethod);
		paramMap.put("startTime", DateUtil.format(startTime, "yyyy-MM-dd HH:mm:ss"));
		paramMap.put("endTime", DateUtil.format(endTime, "yyyy-MM-dd HH:mm:ss"));
		// paramMap.put("stlmDate", "2012-08-23 00:00:00");
		if(isSwitch){
			paramMap.put("gatewayCode", paymethod);
			paramMap.put("merchantCode", "jsbChina");
		}
		String paramStr = JsonUtils.writeMapToJson(paramMap);
		String sign = CAUtil.doSign(paramStr, NewPayUtil.getMerprikey(), "utf-8", "SHA1WithRSA");
		Map<String, String> postMap = new HashMap<String, String>();
		postMap.put("merid", NewPayUtil.getMerid());
		try {
			postMap.put("params", Base64.encodeBase64String(paramStr.getBytes("UTF-8")));
			postMap.put("sign", sign);
			String downReconciliationFileUrl = NewPayUtil.getDownReconciliationFileUrl();
			if(isSwitch){
				downReconciliationFileUrl = NewPayUtil.getNewDownReconciliationFileUrl();
			}
			HttpResult code = HttpUtils.postUrlAsString(downReconciliationFileUrl, postMap);
			if (code.isSuccess()) {
				String res = new String(Base64.decodeBase64(code.getResponse()), "utf-8");
				Map<String, String> returnMap = VmUtils.readJsonToMap(res);
				model.put("downParams", returnMap);
				model.put("submitParams", VmUtils.readJsonToMap(returnMap.get("submitParams")));
				return "admin/balance/jsbank/downReconciliationFile.vm";
			} else {
				return this.showJsonError(model, "下载时出错");
			}
		} catch (Exception e) {
			e.printStackTrace();
			return this.showJsonError(model, "下载时出错");
		}
	}
}
