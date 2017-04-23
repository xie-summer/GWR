package com.gewara.service.partner.impl;

import java.io.File;
import java.io.FileWriter;
import java.io.Writer;
import java.sql.Timestamp;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.gewara.Config;
import com.gewara.constant.ApiConstant;
import com.gewara.constant.sys.ConfigConstant;
import com.gewara.constant.sys.LogTypeConstant;
import com.gewara.constant.ticket.OrderConstant;
import com.gewara.constant.ticket.PartnerConstant;
import com.gewara.model.api.ApiUser;
import com.gewara.model.common.GewaConfig;
import com.gewara.model.drama.DramaOrder;
import com.gewara.model.partner.CallbackOrder;
import com.gewara.model.pay.GewaOrder;
import com.gewara.model.pay.GoodsOrder;
import com.gewara.model.pay.TicketOrder;
import com.gewara.pay.ChinapayUtil;
import com.gewara.pay.PayUtil;
import com.gewara.pay.SpSdoUtil;
import com.gewara.service.impl.BaseServiceImpl;
import com.gewara.service.partner.PartnerSynchService;
import com.gewara.support.ErrorCode;
import com.gewara.util.DateUtil;
import com.gewara.util.HttpResult;
import com.gewara.util.HttpUtils;
import com.gewara.util.JsonUtils;
import com.gewara.util.StringUtil;
@Service("partnerSynchService")
public class PartnerSynchServiceImpl extends BaseServiceImpl implements PartnerSynchService{
	@Autowired@Qualifier("config")
	private Config config;
	public void setConfig(Config config) {
		this.config = config;
	}
	@Override
	public CallbackOrder addCallbackOrder(GewaOrder gorder, String pushflag, boolean renew){
		if(gorder instanceof TicketOrder || gorder instanceof DramaOrder || gorder instanceof GoodsOrder) {
			if(!gorder.sureOutPartner()) return null;
			ApiUser partner = baseDao.getObject(ApiUser.class, gorder.getPartnerid());
			if(partner == null) return null;
			if(StringUtils.contains(partner.getPushflag(), pushflag)){
				CallbackOrder callorder = baseDao.getObject(CallbackOrder.class, gorder.getId());
				if(callorder==null) callorder = new CallbackOrder(gorder);
				if(renew) callorder.setStatus(CallbackOrder.STATUS_N);
				baseDao.saveObject(callorder);
				return callorder;
			}
		}
		return null;
	}
	@Override
	public ErrorCode pushCallbackOrder(CallbackOrder callOrder){
		if(callOrder.isSuccess()) return ErrorCode.SUCCESS;
		GewaOrder order = baseDao.getObject(GewaOrder.class, callOrder.getOrderid());
		ApiUser partner = baseDao.getObject(ApiUser.class, order.getPartnerid());
		return pushCallbackOrder(callOrder, order, partner);
	}
	@Override
	public ErrorCode pushCallbackOrder(String tradeNo, String pushflag) {
		if(!(PayUtil.isTicketTrade(tradeNo) || StringUtils.startsWith(tradeNo,PayUtil.FLAG_GOODS))) return ErrorCode.getFailure("Only push TicketOrder!");
		GewaOrder order = baseDao.getObjectByUkey(GewaOrder.class, "tradeNo", tradeNo, false);
		Map<String, String> changeHisMap = JsonUtils.readJsonToMap(order.getChangehis());
		String successChange = changeHisMap.get(OrderConstant.CHANGEHIS_KEY_SUCCESSCHANGE);
		//是否是成功订单代处理 success = "true" 
		final boolean checkChange = Boolean.parseBoolean(successChange);
		if(checkChange){
			return ErrorCode.getFailure("paid success change error!");
		}
		CallbackOrder callorder = baseDao.getObject(CallbackOrder.class, order.getId());
		if(callorder==null) return ErrorCode.getFailure("paid push not exists");
		ApiUser partner = baseDao.getObject(ApiUser.class, order.getPartnerid());
		return pushCallbackOrder(callorder, order, partner);
	}
	private ErrorCode pushCallbackOrder(CallbackOrder callOrder, GewaOrder order, ApiUser partner){
		if(callOrder.isSuccess()) return ErrorCode.SUCCESS;
		callOrder.addCalltimes();
		boolean result = false;
		String errmsg = "";
		try{
			if(PartnerConstant.PARTNER_UNION.equals(callOrder.getPartnerid())){
				result = ChinapayUtil.callbackBianmin((TicketOrder)order);
			}else if(PartnerConstant.PARTNER_SPSDO.equals(order.getPartnerid())){
				result = SpSdoUtil.sendOrder((TicketOrder)order);
			}else{
				Map params = new HashMap();
				String status = ApiConstant.getMappedOrderStatus(order.getFullStatus());
				params.put("tradeno", order.getTradeNo());
				params.put("status", status);
				String encryptCode = StringUtil.md5(order.getTradeNo() + status + partner.getPartnerkey() + partner.getPrivatekey());
				params.put("encryptCode", encryptCode);
				if(StringUtils.equals(order.getPartnerid()+"", partner.getId()+"")){
					params.put("payseqno", order.getPayseqno());
				}
				//TODO:Service之外
				HttpResult pushResult = HttpUtils.postUrlAsString(partner.getPushurl(), params);
				if(StringUtils.contains(pushResult.getResponse(), "success")){
					result = true;
				}else {
					errmsg = "pushReturn:" + pushResult.getResponse();
					dbLogger.warn(partner.getPushurl() + ":" + order.getTradeNo() + ":" + params + ":" + pushResult.getResponse());
				}
			}
		}catch(Exception e){
			dbLogger.errorWithType(LogTypeConstant.LOG_TYPE_PARTNER, "回传商家订单错误", e);
			return ErrorCode.getFailure("回传商家订单错误");
		}
		callOrder.setUpdatetime(new Timestamp(System.currentTimeMillis()));
		if(result){
			callOrder.setStatus(CallbackOrder.STATUS_Y);
			baseDao.saveObject(callOrder);
			return ErrorCode.SUCCESS;
		}else{
			callOrder.setStatus(CallbackOrder.STATUS_F);
			baseDao.saveObject(callOrder);
			return ErrorCode.getFailure("回传商家订单失败：" + errmsg);
		}
	}
	@Override
	public String writeChinapayTransFile(){
		GewaConfig gc = baseDao.getObject(GewaConfig.class, ConfigConstant.CFG_CHINAPAY_FTPFILE);
		Date cur = DateUtil.getCurDate();
		Date filedate = DateUtil.parseDate(gc.getContent());
		if(filedate.after(cur)) return "date invalid"; 
		Date orderdate = DateUtil.addDay(filedate, -2);
		File dir = new File("/opt/lamp/ftp_storage/" + DateUtil.format(filedate, "yyyyMMdd"));
		if(!dir.exists()) dir.mkdirs();
		File dstFile = new File(dir, ChinapayUtil.getTransFilename(filedate));
		Map<String, String> params = new HashMap<String, String>();
		params.put("orderdate", DateUtil.formatDate(orderdate));
		String url = "http://localhost:8080" + config.getBasePath() + "partner/chinapay/orderList.xhtml";
		//TODO:Service之外
		HttpResult call = HttpUtils.postUrlAsString(url, params);
		String result = StringUtils.trim(call.getResponse())+"\n";
		Writer writer = null;
		try{
			dbLogger.warn("write chinapay files:" + dstFile.getCanonicalPath().toString());
			if(!result.startsWith(ChinapayUtil.getMerSysId())) {
				dbLogger.error(result);
				return "error:" + call.getMsg() + result;
			}
			writer = new FileWriter(dstFile);
			writer.append(result);
			Date next = DateUtil.addDay(filedate, 1);
			gc.setContent(DateUtil.formatDate(next));
			baseDao.saveObject(gc);
			return dstFile.getCanonicalPath();
		}catch(Exception e){
			dbLogger.error(StringUtil.getExceptionTrace(e));
			return "exception:" + e.getMessage();
		}finally{
			if(writer!=null) try{writer.close();}catch(Exception e2){}
		}
	}
	@Override
	public List<CallbackOrder> getCallbackOrderList(int maxtimes) {
		String sql = "from CallbackOrder where status like ? and calltimes < ? and updatetime < addtime + 1 order by orderid, addtime";
		List<CallbackOrder> callList = hibernateTemplate.find(sql, CallbackOrder.STATUS_N + "%", maxtimes);
		return callList;
	}
}
