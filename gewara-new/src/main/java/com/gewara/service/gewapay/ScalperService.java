package com.gewara.service.gewapay;

import java.util.List;
import java.util.Map;

import com.gewara.support.ErrorCode;
import com.gewara.untrans.monitor.ConfigTrigger;

/**
 * 黄牛限制相关
 * <p>黄牛将限制使用特殊优惠
 * @author user
 *
 */
public interface ScalperService extends ConfigTrigger {

	/**
	 * 是否限制黄牛使用特殊优惠活动
	 * <p>1、用户id是否是黄牛
	 * <p>1、判断手机号是否是黄牛使用的手机号
	 * <p>2、判断该黄牛是否使用过该特殊优惠
	 * @param memberId 用户id
	 * @param phone 用户绑定的手机号
	 * @param specialDiscountId 特殊优惠id
	 * @return
	 */
	ErrorCode<String> checkScalperLimited(Long memberId, String phone, Long specialDiscountId);
	/**
	 * @param memberId
	 * @param phone
	 * @param absolute
	 * @return
	 */
	boolean isScalper(Long memberId, String phone);
	
	/**
	 * 根据ip获取涉嫌黄牛清单
	 * @param hours 多少小时内的注册用户
	 * @param count 一个ip最少注册用户数，默认5
	 * @return
	 */
	Map<String,List<Map>> getSuspectScalperByIp(int hours, int count);
	
}
