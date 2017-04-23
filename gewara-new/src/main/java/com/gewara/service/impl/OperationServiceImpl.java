package com.gewara.service.impl;

import java.sql.Timestamp;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.gewara.constant.sys.CacheConstant;
import com.gewara.model.common.LastOperation;
import com.gewara.model.common.UserOperation;
import com.gewara.service.OperationService;
import com.gewara.support.ErrorCode;
import com.gewara.untrans.CacheService;
import com.gewara.util.DateUtil;
@Service("operationService")
public class OperationServiceImpl extends BaseServiceImpl implements OperationService{
	@Autowired@Qualifier("cacheService")
	private CacheService cacheService;
	@Override
	public boolean updateOperationOneDay(String opkey, boolean update) {
		return update ? updateOperation(opkey, ONE_DAY): isAllowOperation(opkey, ONE_DAY);
	}
	@Override
	public boolean isAllowOperation(String opkey, int allowIntervalSecond) {
		UserOperation op = baseDao.getObject(UserOperation.class, opkey);
		Timestamp cur = new Timestamp(System.currentTimeMillis());
		if(op != null){
			double diff = DateUtil.getDiffSecond(cur, op.getUpdatetime());
			if(diff < allowIntervalSecond){//间隔小于allowIntervalSecond，不允许
				return false;
			}
		}
		return true;
	}
	@Override
	public boolean updateOperation(String opkey, int allowIntervalSecond){
		return updateOperation(opkey, allowIntervalSecond, null);
	}
	@Override
	public boolean updateOperation(String opkey, int allowIntervalSecond, String secondkey){
		UserOperation op = baseDao.getObject(UserOperation.class, opkey);
		Timestamp cur = new Timestamp(System.currentTimeMillis());
		boolean allow = true;
		if(op != null){
			if(StringUtils.isNotBlank(secondkey) && StringUtils.equals(secondkey, op.getSecondkey())){
				//不需更新
				return true;
			}
			double diff = DateUtil.getDiffSecond(cur, op.getUpdatetime());
			if(diff < allowIntervalSecond){//间隔小于allowIntervalSecond，不允许
				allow = false;
				op.setRefused(op.getRefused()+1);
			}else{//allow
				op.setSecondkey(secondkey);
				op.setRefused(0);
				op.setUpdatetime(cur);
			}
		}else{
			op = new UserOperation(opkey, cur);
			op.setSecondkey(secondkey);
		}
		op.setValidtime(DateUtil.addSecond(cur, allowIntervalSecond));
		baseDao.saveObject(op);
		return allow;
	}
	@Override
	public boolean isAllowOperation(String opkey, int scopeSecond, int allowNum) {
		UserOperation op = baseDao.getObject(UserOperation.class, opkey);
		Timestamp cur = new Timestamp(System.currentTimeMillis());
		if(op != null){
			double diff = DateUtil.getDiffSecond(cur, op.getAddtime());
			if(diff < scopeSecond){
				if(op.getOpnum() >= allowNum) return false;
			}
		}
		return true;
	}
	@Override
	public boolean updateOperation(String opkey, int scopeSecond, int allowNum) {
		return updateOperation(opkey, scopeSecond, allowNum, null);
	}
	@Override
	public boolean updateOperation(String opkey, int scopeSecond, int allowNum, String secondkey) {
		UserOperation op = baseDao.getObject(UserOperation.class, opkey);
		Timestamp cur = new Timestamp(System.currentTimeMillis());
		boolean allow = true;
		if(op != null){
			if(StringUtils.isNotBlank(secondkey) && StringUtils.equals(secondkey, op.getSecondkey())){
				//不需更新
				return true;
			}
			double diff = DateUtil.getDiffSecond(cur, op.getAddtime());
			if(diff < scopeSecond){
				if(op.getOpnum() >= allowNum) {
					allow = false;
				}
			}else{
				op.setAddtime(cur);//重新来过
				op.setValidtime(DateUtil.addSecond(cur, scopeSecond));
				op.setOpnum(0);
			}
			if(allow) {
				op.setSecondkey(secondkey);
				op.setUpdatetime(cur);
				op.setOpnum(op.getOpnum()+1);
				op.setRefused(0);
			}else{
				op.setRefused(op.getRefused()+1);
			}
		}else{
			op = new UserOperation(opkey, cur);
			op.setSecondkey(secondkey);
			op.setValidtime(DateUtil.addSecond(cur, scopeSecond));
		}
		baseDao.saveObject(op);
		return allow;
	}

	@Override
	public boolean isAllowOperation(String opkey, int allowIntervalSecond, int scopeSecond, int allowNum) {
		UserOperation op = baseDao.getObject(UserOperation.class, opkey);
		Timestamp cur = new Timestamp(System.currentTimeMillis());
		if(op != null){
			double diff = DateUtil.getDiffSecond(cur, op.getAddtime());
			if(diff < scopeSecond){
				if(op.getOpnum() >= allowNum) {
					return false;
				}else{
					double diff2 = DateUtil.getDiffSecond(cur, op.getUpdatetime());
					if(diff2 < allowIntervalSecond){//间隔小于allowIntervalSecond，不允许
						return false;
					}					
				}
			}
		}
		return true;
	}
	@Override
	public boolean updateOperation(String opkey, int allowIntervalSecond, int scopeSecond, int allowNum) {
		return updateOperation(opkey, allowIntervalSecond, scopeSecond, allowNum, null);
	}
	@Override
	public boolean updateOperation(String opkey, int allowIntervalSecond, int scopeSecond, int allowNum, String secondkey) {
		UserOperation op = baseDao.getObject(UserOperation.class, opkey);
		Timestamp cur = new Timestamp(System.currentTimeMillis());
		dbLogger.error("opkey:" + opkey + ", cur=" +cur);
		boolean allow = true;
		if(op != null){
			if(StringUtils.isNotBlank(secondkey) && StringUtils.equals(secondkey, op.getSecondkey())){
				//不需更新
				return true;
			}
			double diff = DateUtil.getDiffSecond(cur, op.getAddtime());
			if(diff < scopeSecond){
				if(op.getOpnum() >= allowNum) {
					allow = false;
				}else{
					double diff2 = DateUtil.getDiffSecond(cur, op.getUpdatetime());
					if(diff2 < allowIntervalSecond){//间隔小于allowIntervalSecond，不允许
						allow = false;
					}
				}
			}else{
				op.setAddtime(cur);//重新来过
				op.setValidtime(DateUtil.addSecond(cur, scopeSecond));
				op.setOpnum(0);
			}
			if(allow) {
				op.setUpdatetime(cur);
				op.setOpnum(op.getOpnum()+1);
				op.setRefused(0);
				op.setSecondkey(secondkey);
			}else{
				op.setRefused(op.getRefused()+1);
			}
		}else{
			op = new UserOperation(opkey, cur);
			op.setSecondkey(secondkey);
			op.setValidtime(DateUtil.addSecond(cur, scopeSecond));
		}
		baseDao.saveObject(op);
		return allow;
	}
	@Override
	public void resetOperation(String opkey, int secondNum){
		UserOperation op = baseDao.getObject(UserOperation.class, opkey);
		if(op!=null){
			Timestamp cur = new Timestamp(System.currentTimeMillis());
			op.setUpdatetime(DateUtil.addSecond(cur, -secondNum));
			//addtime不往前退 上面那个带次数的就重置不了
			op.setAddtime(DateUtil.addSecond(cur, -secondNum));
			baseDao.saveObject(op);
		}
	}
	
	@Override
	public ErrorCode<String> checkLimitInCache(String key, int limitedCount) {
		int nowCount = this.cacheService.incr(CacheConstant.REGION_ONEHOUR, key, 1, 1);
		if(nowCount > limitedCount){
			return ErrorCode.getFailureReturn(nowCount);
		}
		return ErrorCode.SUCCESS;
	}
	@Override
	public ErrorCode<String> updateLoginLimitInCache(String key, int maxnum) {
		Long time = (Long) cacheService.get(CacheConstant.REGION_HALFDAY, "TIME" + key);
		Integer count=1;
		Long cur = System.currentTimeMillis();
		if(time!=null){
			Long rc = (time + DateUtil.m_minute*10);
			if(rc > cur){
				count = (Integer) cacheService.get(CacheConstant.REGION_HALFDAY, "LIMIT" + key);
				if(count==null) count = 1;
				else count++;
			}else{
				cacheService.set(CacheConstant.REGION_HALFDAY, "TIME" + key, cur);
			}
		}else{
			cacheService.set(CacheConstant.REGION_HALFDAY, "TIME" + key, cur);	
		}
		cacheService.set(CacheConstant.REGION_HALFDAY, "LIMIT" + key, count);
		if(count > maxnum){
			return ErrorCode.getFailure("loginLimitedCount=" + count);
		}
		return ErrorCode.getSuccess("");
	}
	@Override
	public ErrorCode<String> checkLoginLimitNum(String key, int maxnum) {
		Long time = (Long) cacheService.get(CacheConstant.REGION_HALFDAY, "TIME" + key);
		Long cur = System.currentTimeMillis();
		if(time!=null){
			Long rc = (time + DateUtil.m_minute*10);
			if(rc > cur){
				Integer count = (Integer) cacheService.get(CacheConstant.REGION_HALFDAY, "LIMIT" + key);
				if(count!=null && count> maxnum) {
					return ErrorCode.getFailure("登录失败次数受限，请稍后再试！");
				}
			}
		}
		return ErrorCode.SUCCESS;
	}
	
	@Override
	public LastOperation updateLastOperation(String lastkey, String lastvalue, Timestamp lasttime, Timestamp validtime, String tag) {
		LastOperation last = new LastOperation(lastkey, lastvalue, lasttime, validtime, tag);
		baseDao.saveObject(last);
		return last;
	}

}
