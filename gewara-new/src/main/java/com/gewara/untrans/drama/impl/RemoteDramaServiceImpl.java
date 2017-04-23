package com.gewara.untrans.drama.impl;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import com.gewara.Config;
import com.gewara.api.gpticket.service.GpticketOrderApiService;
import com.gewara.api.gpticket.vo.SignVo;
import com.gewara.api.gpticket.vo.command.OrderCommandVo;
import com.gewara.api.gpticket.vo.command.OrderIdVo;
import com.gewara.api.gpticket.vo.ticket.DramaRemoteOrderVo;
import com.gewara.api.vo.ResultCode;
import com.gewara.commons.api.ApiSysParamConstants;
import com.gewara.commons.sign.Sign;
import com.gewara.constant.OdiConstant;
import com.gewara.support.ErrorCode;
import com.gewara.untrans.drama.RemoteDramaService;
import com.gewara.util.BeanUtil;
import com.gewara.util.GewaLogger;
import com.gewara.util.LoggerUtils;

public class RemoteDramaServiceImpl implements RemoteDramaService {
	private final transient GewaLogger dbLogger = LoggerUtils.getLogger(getClass(), Config.getServerIp(), Config.SYSTEMID);
	
	private GpticketOrderApiService gpticketOrderApiService;
	private String appkey;
	private String secretCode;
	
	public GpticketOrderApiService getGpticketOrderApiService() {
		return gpticketOrderApiService;
	}

	public void setGpticketOrderApiService(GpticketOrderApiService gpticketOrderApiService) {
		this.gpticketOrderApiService = gpticketOrderApiService;
	}

	public String getAppkey() {
		return appkey;
	}

	public void setAppkey(String appkey) {
		this.appkey = appkey;
	}

	public String getSecretCode() {
		return secretCode;
	}

	public void setSecretCode(String secretCode) {
		this.secretCode = secretCode;
	}

	private <T> ErrorCode<T> convert(ResultCode<T> response){
		if(!response.isSuccess()){
			return ErrorCode.getFailure(response.getErrcode(), response.getMsg());
		}
		return ErrorCode.getSuccessReturn(response.getRetval());
	}
	
	private ErrorCode convertSuccess(ResultCode response){
		if(!response.isSuccess()){
			return ErrorCode.getFailure(response.getErrcode(), response.getMsg());
		}
		return ErrorCode.SUCCESS;
	}
	
	private SignVo getSignVo(Map<String, String> params){
		final SignVo sign = new SignVo();
		sign.setAppkey(getAppkey());
		params.put(ApiSysParamConstants.APPKEY, getAppkey());
		sign.setSign(Sign.signMD5(params, getSecretCode()));
		return sign;
	}
	
	@Override
	public ErrorCode<DramaRemoteOrderVo> backOrder(Long orderid, String description){
		final OrderIdVo orderIdVo = new OrderIdVo(orderid);
		orderIdVo.setDescription(description);
		SignVo signVo = getSignVo(BeanUtil.getSimpleStringMap(orderIdVo));
		try{
			ResultCode<DramaRemoteOrderVo> response = gpticketOrderApiService.backRemoteOrder(orderIdVo, signVo);
			if(!response.isSuccess()) return ErrorCode.getFailure(response.getErrcode(), response.getMsg());
			return ErrorCode.getSuccessReturn(response.getRetval());
		}catch(Exception e){
			dbLogger.warn("backOrder", e);
			return ErrorCode.getFailure("退票失败！");
		}
	}
	
	@Override
	public ErrorCode<String> getRemoteLockSeat(String areaseqno){
		Map<String, String> params = new HashMap<String, String>();
		params.put("areaseqno", areaseqno);
		SignVo signVo = getSignVo(params);
		try{
			ResultCode<String> response = gpticketOrderApiService.getRemoteLockSeat(areaseqno, signVo);
			return convert(response);
		}catch(Exception e){
			dbLogger.warn("getRemoteLockSeat", e);
			return ErrorCode.getFailure("场馆网络异常！");
		}
	}

	@Override
	public ErrorCode<String> getRemoteLockPrice(String areaseqno){
		Map<String, String> params = new HashMap<String, String>();
		params.put("areaseqno", areaseqno);
		SignVo signVo = getSignVo(params);
		try{
			ResultCode<String> response = gpticketOrderApiService.getRemoteLockPrice(areaseqno, signVo);
			return convert(response);
		}catch(Exception e){
			dbLogger.warn("getRemoteLockSeat", e);
			return ErrorCode.getFailure("场馆网络异常！");
		}
	}
	
	@Override
	public ErrorCode<DramaRemoteOrderVo> qryOrder(Long orderid, boolean forceRefresh) {
		final OrderIdVo orderIdVo = new OrderIdVo(orderid);
		orderIdVo.setForceRefresh(forceRefresh);
		SignVo signVo = getSignVo(BeanUtil.getSimpleStringMap(orderIdVo));
		try{
			ResultCode<DramaRemoteOrderVo> response = gpticketOrderApiService.qryRemoteOrder(orderIdVo, signVo);
			return convert(response);
		}catch(Exception e){
			dbLogger.warn("qryOrder", e);
			return ErrorCode.getFailure("查询订单错误！");
		}
	}
	
	@Override
	public ErrorCode<DramaRemoteOrderVo> checkOrder(Long orderid, boolean forceRefresh){
		final OrderIdVo orderIdVo = new OrderIdVo(orderid);
		orderIdVo.setForceRefresh(forceRefresh);
		SignVo signVo = getSignVo(BeanUtil.getSimpleStringMap(orderIdVo));
		try{
			ResultCode<DramaRemoteOrderVo> response = gpticketOrderApiService.checkRemoteOrder(orderIdVo, signVo);
			return convert(response);
		}catch(Exception e){
			dbLogger.warn("checkOrder", e);
			return ErrorCode.getFailure("查询订单错误！");
		}
	}
	
	@Override
	public ErrorCode<String> qryTicketPrice(String seqno){
		Map<String, String> params = new HashMap<String, String>();
		params.put("seqno", seqno);
		SignVo signVo = getSignVo(params);
		try{
			ResultCode<String> response = gpticketOrderApiService.qryTicketPrice(seqno, signVo);
			return convert(response);
		}catch(Exception e){
			dbLogger.warn("qryTicketPrice", e);
			return ErrorCode.getFailure("获取场次票面错误！");
		}
	}
	
	@Override
	public ErrorCode<String> qryOrderPrintInfo(Long orderid){
		final OrderIdVo orderIdVo = new OrderIdVo(orderid);
		SignVo signVo = getSignVo(BeanUtil.getSimpleStringMap(orderIdVo));
		try{
			ResultCode<String> response = gpticketOrderApiService.qryOrderPrintInfo(orderIdVo, signVo);
			return convert(response);
		}catch(Exception e){
			dbLogger.warn("qryTicketPrice", e);
			return ErrorCode.getFailure("获取订单打票信息错误！");
		}
	}

	@Override
	public ErrorCode<DramaRemoteOrderVo> newCreateOrder(String seqno, Long orderid, String mobile, String areaseqno, String opentype, String seatLabel) {
		final OrderCommandVo orderCommand = new OrderCommandVo(seqno, orderid, mobile, areaseqno, opentype, seatLabel);
		SignVo signVo = getSignVo(BeanUtil.getSimpleStringMap(orderCommand));
		try{
			ResultCode<DramaRemoteOrderVo> response = gpticketOrderApiService.newCreateOrder(orderCommand, signVo);
			return convert(response);
		}catch(Exception e){
			dbLogger.warn("createOrder", e);
			return ErrorCode.getFailure("下单失败！");
		}
	}

	@Override
	public ErrorCode<DramaRemoteOrderVo> newLockSeat(String seqno, Long orderid, String mobile, String areaseqno, String seatLabel) {
		final OrderCommandVo orderCommand = new OrderCommandVo(seqno, orderid, mobile, areaseqno, OdiConstant.OPEN_TYPE_SEAT, seatLabel);
		SignVo signVo = getSignVo(BeanUtil.getSimpleStringMap(orderCommand));
		try{
			ResultCode<DramaRemoteOrderVo> response = gpticketOrderApiService.newLockSeat(orderCommand, signVo);
			return convert(response);
		}catch(Exception e){
			dbLogger.warn("lockSeat", e);
			return ErrorCode.getFailure("下单失败！");
		}
	}

	@Override
	public ErrorCode<DramaRemoteOrderVo> newLockPrice(String seqno, Long orderid, String mobile, String areaseqno, String seatLabel) {
		final OrderCommandVo orderCommand = new OrderCommandVo(seqno, orderid, mobile, areaseqno, OdiConstant.OPEN_TYPE_PRICE, seatLabel);
		SignVo signVo = getSignVo(BeanUtil.getSimpleStringMap(orderCommand));
		try{
			ResultCode<DramaRemoteOrderVo> response = gpticketOrderApiService.newLockPrice(orderCommand, signVo);
			return convert(response);
		}catch(Exception e){
			dbLogger.warn("lockPrice", e);
			return ErrorCode.getFailure("下单失败！");
		}
	}

	@Override
	public ErrorCode<DramaRemoteOrderVo> newFixOrder(String seqno, Long orderid, String mobile, String areaseqno, String opentype, String seatLabel, String greetings) {
		final OrderCommandVo orderCommand = new OrderCommandVo(seqno, orderid, mobile, areaseqno, opentype, seatLabel);
		if(StringUtils.isNotBlank(greetings)){
			orderCommand.setGreetings(greetings);
		}
		SignVo signVo = getSignVo(BeanUtil.getSimpleStringMap(orderCommand));
		try{
			ResultCode<DramaRemoteOrderVo> response = gpticketOrderApiService.newFixOrder(orderCommand, signVo);
			return convert(response);
		}catch(Exception e){
			dbLogger.warn("fixOrder", e);
			return ErrorCode.getFailure("确认订单失败！");
		}
	}
	
	@Override
	public ErrorCode newUnRemoteOrder(Long orderid){
		final OrderIdVo orderIdVo = new OrderIdVo(orderid);
		SignVo signVo = getSignVo(BeanUtil.getSimpleStringMap(orderIdVo));
		try{
			ResultCode response = gpticketOrderApiService.newUnRemoteOrder(orderIdVo, signVo);
			return convertSuccess(response);
		}catch(Exception e){
			dbLogger.warn("unRemoteOrder", e);
			return ErrorCode.getFailure("取消订单失败！");
		}
	}
}
