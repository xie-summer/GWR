package com.gewara.service.member.impl;

import java.sql.Timestamp;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.gewara.constant.ApiConstant;
import com.gewara.constant.BindConstant;
import com.gewara.constant.SmsConstant;
import com.gewara.model.pay.SMSRecord;
import com.gewara.model.user.BindMobile;
import com.gewara.service.OperationService;
import com.gewara.service.impl.BaseServiceImpl;
import com.gewara.service.member.BindMobileService;
import com.gewara.support.ErrorCode;
import com.gewara.untrans.CacheService;
import com.gewara.util.DateUtil;
import com.gewara.util.StringUtil;
import com.gewara.util.ValidateUtil;

@Service("bindMobileService")
public class BindMobileServiceImpl extends BaseServiceImpl implements BindMobileService {
	@Autowired@Qualifier("cacheService")
	private CacheService cacheService;
	public void setCacheService(CacheService cacheService){
		this.cacheService = cacheService;
	}
	@Autowired@Qualifier("operationService")
	private OperationService operationService;
	public void setOperationService(OperationService operationService) {
		this.operationService = operationService;
	}
	
	@Override
	public ErrorCode<SMSRecord> refreshBindMobile(String tag, String mobile, String ip) {
		String msgTemplate = BindConstant.getMsgTemplate(tag);
		return refreshBindMobile(tag, mobile, ip, msgTemplate);
	}

	@Override
	public ErrorCode<SMSRecord> refreshBindMobile(String tag, String mobile, String ip, String msgTemplate) {
		return refreshBindMobile(tag, mobile, ip, msgTemplate, true);
	}
	
	@Override
	public ErrorCode<SMSRecord> refreshNoSecurityBindMobile(String tag, String mobile, String ip, String msgTemplate){
		return refreshBindMobile(tag, mobile, ip, msgTemplate, false);
	}
	
	private ErrorCode<SMSRecord> refreshBindMobile(String tag, String mobile, String ip, String msgTemplate, final boolean checkSecurity){
		if(!ValidateUtil.isMobile(mobile)) return ErrorCode.getFailure("手机号码格式不正确！");
		if(!BindConstant.VALID_TAG_LIST.contains(tag)) throw new IllegalArgumentException("类型不正确！");
		Timestamp curtime = DateUtil.getCurFullTimestamp();
		String checkpass = StringUtil.getDigitalRandomString(6);
		Timestamp validtime = DateUtil.addMinute(curtime, BindConstant.VALID_MIN);
		//1、ip检查
		BindMobile bind = baseDao.getObject(BindMobile.class, tag + mobile);
		if(checkSecurity && bind!=null && bind.getSendcount() >= BindConstant.getMaxSendnum(tag)){
			dbLogger.warn("您的手机号有异常，请联系客服解决：" + tag + ", " + mobile);
			return ErrorCode.getFailure("您的手机号有异常，请联系客服解决！");
		}
		
		String opkey = tag + ip;
		boolean allow = operationService.updateOperation(opkey, 30, OperationService.ONE_HOUR, 30);
		if(checkSecurity && !allow){
			return ErrorCode.getFailure(ApiConstant.CODE_DATA_ERROR, "您的IP获取动态码太过频繁！");
		}
		//2、手机号检查，每天最多发5次
		String mobileKey = tag + mobile;
		allow = operationService.updateOperation(mobileKey, 60, OperationService.ONE_DAY, 3);
		if(checkSecurity && !allow){
			return ErrorCode.getFailure(ApiConstant.CODE_DATA_ERROR, "同一手机号每天只可获取3次动态码！");
		}
		
		if(bind==null){
			bind = new BindMobile(tag, mobile, checkpass, ip);
		}else{
			bind.setLastip(ip);
			bind.setCheckcount(0);			//复位
			bind.setMobile(mobile);
			bind.setCheckpass(checkpass);
		}
		bind.setValidtime(validtime);
		bind.setSendcount(bind.getSendcount() + 1);
		baseDao.saveObject(bind);
		String msg = StringUtils.replace(msgTemplate, "checkpass", bind.getCheckpass());
		String ukey = "M" + DateUtil.format(curtime, "yyMMddHHmmss");
		SMSRecord sms = new SMSRecord(null, ukey, mobile, msg, curtime, DateUtil.addMinute(curtime, 5), SmsConstant.SMSTYPE_DYNCODE);
		sms.setTag(tag);
		baseDao.saveObject(sms);
		return ErrorCode.getSuccessReturn(sms);
	}
	
	@Override
	public ErrorCode preCheckBindMobile(String tag, String mobile, String checkpass) {
		return checkBindMobile(tag, mobile, checkpass, 0);
	}

	@Override
	public ErrorCode checkBindMobile(String tag, String mobile, String checkpass){
		return checkBindMobile(tag, mobile, checkpass, 500);
	}
	private ErrorCode checkBindMobile(String tag, String mobile, String checkpass, int successIncrease){
		if(!ValidateUtil.isMobile(mobile)) return ErrorCode.getFailure("手机号码格式不正确！");
		if(StringUtils.isBlank(checkpass)) return ErrorCode.getFailure("动态码不能为空！");
		Timestamp curtime = DateUtil.getCurFullTimestamp();
		BindMobile bindMobile = baseDao.getObject(BindMobile.class, tag + mobile);
		if(bindMobile == null) return ErrorCode.getFailure("动态码错误，请重新获取！");
		int maxcheck = BindConstant.getMaxCheck(tag);
		if (curtime.after(bindMobile.getValidtime()) || bindMobile.getCheckcount() > maxcheck) {
			return ErrorCode.getFailure("动态码失效，请重新获取！");
		}
		
		bindMobile.setTotalcheck(bindMobile.getTotalcheck() + 1);
		if(StringUtils.equalsIgnoreCase(bindMobile.getCheckpass(), checkpass)){
			bindMobile.setCheckcount(bindMobile.getCheckcount() + 1);
			bindMobile.setCheckcount(bindMobile.getCheckcount() + successIncrease);
			baseDao.saveObject(bindMobile);
			return ErrorCode.getSuccessReturn(bindMobile); 
		}else{
			bindMobile.setCheckcount(bindMobile.getCheckcount() + 1);
			baseDao.saveObject(bindMobile);
			if(bindMobile.getCheckcount() > maxcheck){
				return ErrorCode.getFailure("动态码错误且失效，请重新获取！");
			}else{
				return ErrorCode.getFailure("动态码错误，请重新输入！");
			}
		}
	}
	
	@Override
	public boolean getAndUpdateToken(String type, String ip, int checkcount){
		String token = type + ip;
		Integer tokenCount = (Integer) cacheService.get(CacheService.REGION_LOGINKEY, token);
		if(tokenCount == null) tokenCount = 1;
		tokenCount++;
		cacheService.set(CacheService.REGION_LOGINKEY, token, tokenCount);
		return tokenCount > checkcount;
	}
	@Override
	public boolean isNeedToken(String type, String ip, int checkcount) {
		String token = type + ip;
		Integer tokenCount = (Integer)cacheService.get(CacheService.REGION_LOGINKEY, token);
		if(tokenCount == null) tokenCount = 1;
		return tokenCount > checkcount;
	}
	
	@Override
	public ErrorCode<SMSRecord> refreshBMByAdmin(String tag, String mobile, String ip, String msgTemplate) {
		if(!ValidateUtil.isMobile(mobile)) return ErrorCode.getFailure("手机号码格式不正确！");
		if(!BindConstant.VALID_TAG_LIST.contains(tag)) throw new IllegalArgumentException("类型不正确！");
		Timestamp curtime = DateUtil.getCurFullTimestamp();
		String checkpass = StringUtil.getDigitalRandomString(6);
		Timestamp validtime = DateUtil.addMinute(curtime, BindConstant.VALID_MIN);
		//1、ip检查
		BindMobile bind = baseDao.getObject(BindMobile.class, tag + mobile);
		if(bind!=null && bind.getSendcount() >= BindConstant.getMaxSendnum(tag)){
			dbLogger.warn("您的手机号有异常，请联系客服解决：" + tag + ", " + mobile);
			return ErrorCode.getFailure("您的手机号有异常，请联系客服解决！");
		}
		
		if(bind==null){
			bind = new BindMobile(tag, mobile, checkpass, ip);
		}else{
			bind.setLastip(ip);
			bind.setCheckcount(0);			//复位
			bind.setMobile(mobile);
			bind.setCheckpass(checkpass);
		}
		bind.setValidtime(validtime);
		bind.setSendcount(bind.getSendcount() + 1);
		baseDao.saveObject(bind);
		String msg = StringUtils.replace(msgTemplate, "checkpass", bind.getCheckpass());
		String ukey = "M" + DateUtil.format(curtime, "yyMMddHHmmss");
		SMSRecord sms = new SMSRecord(null, ukey, mobile, msg, curtime, DateUtil.addMinute(curtime, 5), SmsConstant.SMSTYPE_DYNCODE);
		sms.setTag(tag);
		baseDao.saveObject(sms);
		return ErrorCode.getSuccessReturn(sms);
	}
}
