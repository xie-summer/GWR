package com.gewara.web.action.admin.balance;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.gewara.constant.PaymethodConstant;
import com.gewara.constant.sys.ConfigConstant;
import com.gewara.constant.sys.MongoData;
import com.gewara.constant.ticket.OrderConstant;
import com.gewara.model.common.GewaConfig;
import com.gewara.model.pay.CcbPosSettle;
import com.gewara.model.pay.GewaOrder;
import com.gewara.mongo.MongoService;
import com.gewara.pay.CCBPosPayUtil;
import com.gewara.util.DateUtil;
import com.gewara.util.HttpResult;
import com.gewara.util.HttpUtils;
import com.gewara.util.JsonUtils;
import com.gewara.web.action.admin.BaseAdminController;
import com.gewara.web.util.PageUtil;
@Controller
public class CcbPosAdminController extends BaseAdminController{
	@Autowired@Qualifier("mongoService")
	private MongoService mongoService;
	public void setMongoService(MongoService mongoService) {
		this.mongoService = mongoService;
	}
	public static List<String> regMatch(String src, Pattern pattern) {
		Matcher matcher = pattern.matcher(src);
		List<String> msgList = new ArrayList<String>();
		if(matcher.find()){
			String result = matcher.replaceAll("$1,$2,$3,$4,$5,$6");
			msgList.add(result);
		}
		return msgList;
	}
	@RequestMapping("/admin/balance/ccbpos/index.xhtml")
	public String index(){
		return "admin/balance/ccbpos/index.vm";
	}
	@RequestMapping("/admin/balance/ccbpos/refund.xhtml")
	public String refund(String tradeNo, ModelMap model){
		GewaOrder order = daoService.getObjectByUkey(GewaOrder.class, "tradeNo", tradeNo, false);
		if(order==null) return forwardMessage(model, "订单不存在！");
		if(StringUtils.equals(order.getStatus(), OrderConstant.STATUS_PAID_RETURN)){
			return forwardMessage(model, "订单已经退款");
		}
		boolean refund = false;
		String response = "";
		if(StringUtils.equals(order.getPaymethod(), PaymethodConstant.PAYMETHOD_CCBPOSPAY)){ 
			String opkey = "ccbpos" + order.getTradeNo();
			boolean isOper = operationService.isAllowOperation(opkey, 3600*20);
			if(!isOper) return forwardMessage(model, "20小时内容只能操作一次！");
			GewaConfig gconfig = daoService.getObject(GewaConfig.class, ConfigConstant.CFG_CCBPOSPAY);
			String refundurl = CCBPosPayUtil.getRefundUrl(gconfig);
			Map<String, String> params = new HashMap<String, String>();
			params.put("trade_no", order.getTradeNo());
			HttpResult result = HttpUtils.postUrlAsString(refundurl, null);
			if(result.isSuccess()){
				response = result.getResponse();
				if(StringUtils.contains(response, "success")) refund = true;
			}else {
				response = result.getMsg();
			}
			operationService.updateOperation(opkey, 3600*20);
		}
		String msg = refund?"退款成功":"退款失败";
		return forwardMessage(model, msg + ", " + response);
	}
	@RequestMapping("/admin/balance/ccbpos/unlock.xhtml")
	public String unlock(String tradeNo, ModelMap model){
		GewaOrder order = daoService.getObjectByUkey(GewaOrder.class, "tradeNo", tradeNo, false);
		if(order==null) return forwardMessage(model, "订单不存在！");
		boolean refund = false;
		String response = "";
		if(StringUtils.equals(order.getPaymethod(), PaymethodConstant.PAYMETHOD_CCBPOSPAY)){ 
			GewaConfig gconfig = daoService.getObject(GewaConfig.class, ConfigConstant.CFG_CCBPOSPAY);
			String refundurl = CCBPosPayUtil.getUnlockUrl(gconfig);
			Map<String, String> params = new HashMap<String, String>();
			params.put("trade_no", order.getTradeNo());
			HttpResult result = HttpUtils.postUrlAsString(refundurl, null);
			if(result.isSuccess()){
				response = result.getResponse();
				if(StringUtils.contains(response, "success")) refund = true;
			}else {
				response = result.getMsg();
			}
		}
		String msg = refund?"解锁成功":"解锁失败";
		return forwardMessage(model, msg + ", " + response);
	}
	@RequestMapping("/admin/balance/ccbpos/ccbpreno.xhtml")
	public String ccbpreno(String tradeNo, ModelMap model){
		GewaOrder order = daoService.getObjectByUkey(GewaOrder.class, "tradeNo", tradeNo, false);
		if(order==null) return forwardMessage(model, "订单不存在！");
		String response = "";
		if(StringUtils.equals(order.getPaymethod(), PaymethodConstant.PAYMETHOD_CCBPOSPAY)){ 
			GewaConfig gconfig = daoService.getObject(GewaConfig.class, ConfigConstant.CFG_CCBPOSPAY);
			String prenourl = CCBPosPayUtil.getPrenoUrl(gconfig);
			Map<String, String> params = new HashMap<String, String>();
			params.put("tradeno", order.getTradeNo());
			HttpResult result = HttpUtils.postUrlAsString(prenourl, null);
			if(result.isSuccess()){
				response = result.getResponse();
			}else {
				response = result.getMsg();
			}
		}
		return forwardMessage(model, response);
	}
	
	@RequestMapping("/admin/balance/ccbpos/gewaSettle.xhtml")
	public String gewaSettle(String tradeno, ModelMap model){
		if(StringUtils.isNotBlank(tradeno)){
			GewaOrder order = daoService.getObjectByUkey(GewaOrder.class, "tradeNo", tradeno, false);
			Map<String, String> result = JsonUtils.readJsonToMap(order.getOtherinfo());
			if(StringUtils.equalsIgnoreCase(result.get("gewasettle"), "Y")){
				return showJsonError(model, "该订单已经结账");
			}else {
				GewaConfig gconfig = daoService.getObject(GewaConfig.class, ConfigConstant.CFG_CCBPOSPAY);
				Map<String, String> params = new HashMap<String, String>();
				params.put("trade_no", order.getTradeNo());
				String gsUrl = CCBPosPayUtil.getGewaSettleUrl(gconfig);
				HttpResult httpRes = HttpUtils.postUrlAsString(gsUrl, null);
				if(httpRes.isSuccess()){
					String response = httpRes.getResponse();
					if(StringUtils.contains(response, "success")) {
						result.put("gewasettle", "Y");
						order.setOtherinfo(JsonUtils.writeMapToJson(result));
						daoService.saveObject(order);
						return showJsonSuccess(model);
					}else {
						return showJsonError(model, httpRes.getMsg());
					}
				}else {
					return showJsonError(model, httpRes.getMsg());
				}
			}
		}
		return showJsonError(model, "订单号不存在");
	}
	@RequestMapping("/admin/balance/ccbpos/ccbposQuickyQry.xhtml")
	@ResponseBody
	public String ccbposQuickyQry(String tradeNo){
		if(StringUtils.isNotBlank(tradeNo)){
			GewaConfig gconfig = daoService.getObject(GewaConfig.class, ConfigConstant.CFG_CCBPOSPAY);
			Map<String, String> params = new HashMap<String, String>();
			params.put("trade_no", tradeNo);
			String gsUrl = CCBPosPayUtil.getQuickyQryUrl(gconfig);
			HttpResult httpRes = HttpUtils.postUrlAsString(gsUrl, params);
			if(httpRes.isSuccess()){
				return httpRes.getResponse();
			}else {
				return httpRes.getMsg();
			}
		}
		return "请输入订单号";
	}
	@RequestMapping("/admin/balance/ccbpos/ccbposCardQry.xhtml")
	@ResponseBody
	public String ccbposCardQry(String cardpan){
		if(StringUtils.isNotBlank(cardpan)){
			GewaConfig gconfig = daoService.getObject(GewaConfig.class, ConfigConstant.CFG_CCBPOSPAY);
			Map<String, String> params = new HashMap<String, String>();
			params.put("cardpan", cardpan);
			String gsUrl = CCBPosPayUtil.getCardQryUrl(gconfig);
			HttpResult httpRes = HttpUtils.postUrlAsString(gsUrl, params);
			if(httpRes.isSuccess()){
				return httpRes.getResponse();
			}else {
				return httpRes.getMsg();
			}
		}
		return "请输入卡号";
	}
	@RequestMapping("/admin/balance/ccbpos/updateSec.xhtml")
	@ResponseBody
	public String updateSec(String date){
		if(StringUtils.isNotBlank(date)){
			GewaConfig gconfig = daoService.getObject(GewaConfig.class, ConfigConstant.CFG_CCBPOSPAY);
			Map<String, String> params = new HashMap<String, String>();
			params.put("date", date);
			String gsUrl = CCBPosPayUtil.getUpdateSecUrl(gconfig);
			HttpResult httpRes = HttpUtils.postUrlAsString(gsUrl, params);
			if(httpRes.isSuccess()){
				return httpRes.getResponse();
			}else {
				return httpRes.getMsg();
			}
		}
		return "请输入日期";
	}
	
	@RequestMapping("/admin/balance/ccbpos/ccbposlog.xhtml")
	public String ccbposlog(String logurl, ModelMap model){
		String log = "";
		if(StringUtils.isNotBlank(logurl)){
			HttpResult code = HttpUtils.postUrlAsString(logurl, null);
			if(code.isSuccess()){
				log = code.getResponse();
			}else {
				log = code.getMsg();
			}
		}
		model.put("log", log);
		return "admin/balance/ccbpos/ccbposlog.vm";
	}
	@RequestMapping("/admin/balance/ccbpos/qrypos.xhtml")
	public String qrypossql(){
		return "admin/balance/ccbpos/ccbposqry.vm";
	}
	@RequestMapping("/admin/balance/ccbpos/ccbposdb.xhtml")
	@ResponseBody
	public String possql(String url, String sql){
		String ccbposdb = "";
		if(StringUtils.isNotBlank(url)){
			Map<String, String> params = new HashMap<String, String>();
			params.put("sql", sql);
			HttpResult code = HttpUtils.postUrlAsString(url, params);
			if(code.isSuccess()){
				ccbposdb = code.getResponse();
			}else {
				ccbposdb = code.getMsg();
			}
		}
		return ccbposdb;
	}
	
	
	@RequestMapping("/admin/balance/ccbpos/toSettle.xhtml")
	public String toSettle(HttpServletRequest request){
		String settles = request.getParameter("settles");
		List<CcbPosSettle> settleList = new ArrayList<CcbPosSettle>();
		if(StringUtils.isNotBlank(settles)){
			List<String> lines = Arrays.asList(StringUtils.split(settles, "\n"));
			String reg = ".*1052900783201200001[^\\d]+(\\d+)[^\\d]+(\\d+)[^\\d]+(\\d+)[^\\d]+\\d+[^\\d]+([\\d\\.]+)[^\\d]+([\\d\\.]+)[^\\d]+[\\d\\.]+[^\\d]+([\\d\\.]+)[^\\d]+(\\d+)[^\\d]+\\d+";
			Pattern pattern = Pattern.compile(reg, Pattern.CASE_INSENSITIVE);
			List<String> msgList = new ArrayList<String>();
			for(String line: lines){
				msgList.addAll(regMatch(line, pattern));
			}
			List<Map> mapList = new ArrayList<Map>();
			for(String msg : msgList){
				String[] info = StringUtils.split(msg, ",");
				String payseqno = info[1]+"_" + info[0] + "_" + info[2];
				CcbPosSettle settle = daoService.getObjectByUkey(CcbPosSettle.class, "payseqno", payseqno, false);
				if(settle==null){
					settle = new CcbPosSettle(info, payseqno);
					settleList.add(settle);
				}
				if(!StringUtils.equals(settle.getSettle(), "Y")){
					Map params = new HashMap();
					params.put("payseqno", settle.getPayseqno());
					Map map = mongoService.findOne(MongoData.NS_CCBPOS_ORDER, params);
					if(map!=null){
						String tradeno = map.get("tradeno")+"";
						Integer alipaid = Integer.valueOf(map.get("alipaid")+"");
						Date paiddate = DateUtil.parseDate(map.get("paiddate")+"", "yyyyMMdd");
						if(StringUtils.equals(alipaid+"", settle.getAmount()+"")){
							map.put("settle", "Y");
							settle.setSettle("Y");
						}else {
							map.put("settle", "N_P");
							settle.setSettle("N_P");
						}
						settle.setAlipaid(alipaid);
						settle.setPaiddate(paiddate);
						settle.setTradeno(tradeno);
						mapList.add(map);
					}
				}
			}
			mongoService.saveOrUpdateMapList(mapList, "tradeno", MongoData.NS_CCBPOS_ORDER, false, false);
			daoService.saveObjectList(settleList);
			return "redirect:/admin/balance/ccbpos/settleOrderList.xhtml";
		}
		return "admin/balance/ccbpos/settle.vm";
	}
	@RequestMapping("/admin/balance/ccbpos/toSettle2.xhtml")
	public String toSettle2(ModelMap model){
		List<Map> mapList = new ArrayList<Map>();
		String qry = "from CcbPosSettle where settle=? order by settledate";
		List<CcbPosSettle> settleList = hibernateTemplate.find(qry, "N");
		int i = 0;
		for(CcbPosSettle settle : settleList){
			Date settledate = settle.getSettledate();
			Date rdate = DateUtil.addDay(settledate, -1);
			String[] str = StringUtils.split(settle.getPayseqno(), "_");
			String rpayseqno = DateUtil.format(rdate, "yyyyMMdd") + "_" + str[1] + "_" + str[2];
			Map params = new HashMap();
			params.put("payseqno", rpayseqno);
			Map map = mongoService.findOne(MongoData.NS_CCBPOS_ORDER, params);
			if(map!=null){
				String tradeno = map.get("tradeno")+"";
				Integer alipaid = Integer.valueOf(map.get("alipaid")+"");
				Date paiddate = DateUtil.parseDate(map.get("paiddate")+"", "yyyyMMdd");
				if(StringUtils.equals(alipaid+"", settle.getAmount()+"")){
					map.put("settle", "Y");
					settle.setSettle("Y");
				}else {
					map.put("settle", "N_P");
					settle.setSettle("N_P");
				}
				settle.setAlipaid(alipaid);
				settle.setPaiddate(paiddate);
				settle.setTradeno(tradeno);
				mapList.add(map);
				i++;
			}
		}
		mongoService.saveOrUpdateMapList(mapList, "tradeno", MongoData.NS_CCBPOS_ORDER, false, false);
		daoService.saveObjectList(settleList);
		return forwardMessage(model, "更新数据：" + i);
	}
	@RequestMapping("/admin/balance/ccbpos/toSettle3.xhtml")
	public String toSettle3(ModelMap model){
		List<Map> mapList = new ArrayList<Map>();
		String qry = "from CcbPosSettle where settle=? order by settledate";
		List<CcbPosSettle> settleList = hibernateTemplate.find(qry, "N");
		int i = 0;
		for(CcbPosSettle settle : settleList){
			Date settledate = settle.getSettledate();
			Date rdate = DateUtil.addDay(settledate, 1);
			String[] str = StringUtils.split(settle.getPayseqno(), "_");
			String rpayseqno = DateUtil.format(rdate, "yyyyMMdd") + "_" + str[1] + "_" + str[2];
			Map params = new HashMap();
			params.put("payseqno", rpayseqno);
			Map map = mongoService.findOne(MongoData.NS_CCBPOS_ORDER, params);
			if(map!=null){
				String tradeno = map.get("tradeno")+"";
				Integer alipaid = Integer.valueOf(map.get("alipaid")+"");
				Date paiddate = DateUtil.parseDate(map.get("paiddate")+"", "yyyyMMdd");
				if(StringUtils.equals(alipaid+"", settle.getAmount()+"")){
					map.put("settle", "Y");
					settle.setSettle("Y");
				}else {
					map.put("settle", "N_P");
					settle.setSettle("N_P");
				}
				settle.setAlipaid(alipaid);
				settle.setPaiddate(paiddate);
				settle.setTradeno(tradeno);
				mapList.add(map);
				i++;
			}
		}
		mongoService.saveOrUpdateMapList(mapList, "tradeno", MongoData.NS_CCBPOS_ORDER, false, false);
		daoService.saveObjectList(settleList);
		return forwardMessage(model, "更新数据：" + i);
	}
	@RequestMapping("/admin/balance/ccbpos/importCcbPosOrder.xhtml")
	public String importCcbPosOrder(Timestamp paidtime, ModelMap model){
		if(paidtime==null){
			Timestamp curtime = DateUtil.getMillTimestamp();
			paidtime = DateUtil.getBeginningTimeOfDay(curtime);
		}
		String sql = "from GewaOrder t where t.paymethod=? and status like ? and t.paidtime>=? order by paidtime";
		List<Map> mapList = new ArrayList<Map>();
		List<GewaOrder> orderList = hibernateTemplate.find(sql, PaymethodConstant.PAYMETHOD_CCBPOSPAY, "paid%", paidtime);
		for(GewaOrder order : orderList){
			Map map = new HashMap();
			map.put("tradeno", order.getTradeNo());
			map.put("paiddate", DateUtil.format(order.getPaidtime(), "yyyyMMdd"));
			map.put("alipaid", order.getAlipaid()+"");
			map.put("payseqno", order.getPayseqno());
			map.put("settle", "N");
			mapList.add(map);
		}
		mongoService.saveOrUpdateMapList(mapList, "tradeno", MongoData.NS_CCBPOS_ORDER, false, false);
		return forwardMessage(model, "一共导入数据：" + mapList.size());
	}
	@RequestMapping("/admin/balance/ccbpos/settleOrderList.xhtml")
	public String settleOrderList(Integer pageNo, Date startdate, Date enddate, String settle, String tradeno, String cardpan, ModelMap model){
		if(startdate!=null) {
			if(pageNo==null) pageNo = 0;
			Integer maxNum = 2000;
			int count = Integer.valueOf(hibernateTemplate.findByCriteria(settleOrderQuery(startdate, enddate, settle, tradeno, cardpan, false).setProjection(Projections.rowCount())).get(0)+"");
			Map params = new HashMap();
			params.put("startdate", DateUtil.format(startdate, "yyyy-MM-dd"));
			params.put("end", DateUtil.format(enddate, "yyyy-MM-dd"));
			if(StringUtils.isNotBlank(settle)){
				params.put("settle", settle);
			}
			PageUtil pageUtil = new PageUtil(count, maxNum, pageNo, "admin/balance/ccbpos/settleOrderList.xhtml", true, true);
			pageUtil.initPageInfo(params);
			model.put("pageUtil", pageUtil);
			List<CcbPosSettle> orderList = hibernateTemplate.findByCriteria(settleOrderQuery(startdate, enddate, settle, tradeno, cardpan, true), maxNum*pageNo, maxNum);
			model.put("orderList", orderList);
		}
		return "admin/balance/ccbpos/orderList.vm";
	}
	@RequestMapping("/admin/balance/ccbpos/gewaUnSettle.xhtml")
	public String gewaUnSettle(ModelMap model){
		Map params = new HashMap();
		params.put("settle", "N");
		List<Map> qryMapList = new ArrayList<Map>();
		List<Map> qryMapList1 = mongoService.find(MongoData.NS_CCBPOS_ORDER, params);
		
		params = new HashMap();
		params.put("settle", "N_P");
		List<Map> qryMapList2 = mongoService.find(MongoData.NS_CCBPOS_ORDER, params);
		qryMapList.addAll(qryMapList1);
		qryMapList.addAll(qryMapList2);
		model.put("orderList", qryMapList);
		return "admin/balance/ccbpos/unSettleOrderList.vm";
	}
	@RequestMapping("/admin/balance/ccbpos/gewaUnSettle2.xhtml")
	public String gewaUnSettle2(ModelMap model){
		Map params = new HashMap();
		params.put("settle", "N");
		List<Map> qryMapList = new ArrayList<Map>();
		List<Map> qryMapList1 = mongoService.find(MongoData.NS_CCBPOS_ORDER, params);
		
		params = new HashMap();
		params.put("settle", "N_P");
		List<Map> qryMapList2 = mongoService.find(MongoData.NS_CCBPOS_ORDER, params);
		qryMapList.addAll(qryMapList1);
		qryMapList.addAll(qryMapList2);
		int i = 0;
		for(Map map : qryMapList){
			CcbPosSettle settle = daoService.getObjectByUkey(CcbPosSettle.class, "tradeno", map.get("tradeno")+"", false);
			if(settle!=null && StringUtils.equals(settle.getSettle(), "Y")){
				map.put("settle", "Y");
				mongoService.saveOrUpdateMap(map, "tradeno", MongoData.NS_CCBPOS_ORDER);
				i++;
			}
		}
		model.put("orderList", qryMapList);
		return forwardMessage(model, "一共更新数据：" + i);
	}
	@RequestMapping("/admin/balance/ccbpos/delSettle.xhtml")
	public String delRepeatSettle(String settledate, ModelMap model){
		if(StringUtils.isBlank(settledate)) return forwardMessage(model, "请输入日期");
		String hql = "from CcbPosSettle where to_char(settledate,'yyyy-MM-dd')=?";
		List<CcbPosSettle> settleList = hibernateTemplate.find(hql, settledate);
		daoService.removeObjectList(settleList);
		return forwardMessage(model, "一共删除数据：" + settleList.size());
	}
	public DetachedCriteria settleOrderQuery(Date startdate, Date enddate, String settle, String tradeno, String cardpan, boolean isList){
		DetachedCriteria query = DetachedCriteria.forClass(CcbPosSettle.class);
		query.add(Restrictions.ge("settledate", startdate));
		if(StringUtils.isNotBlank(tradeno)){
			query.add(Restrictions.eq("tradeno", tradeno));
		}
		if(StringUtils.isNotBlank(cardpan)){
			query.add(Restrictions.eq("cardpan", cardpan));
		}
		if(enddate!=null){
			query.add(Restrictions.le("settledate", enddate));
		}
		if(StringUtils.isNotBlank(settle)){
			query.add(Restrictions.eq("settle", settle));
		}
		if(isList){
			query.addOrder(Order.asc("settledate"));
			query.addOrder(Order.asc("paiddate"));
		}
		return query;
	}
}
