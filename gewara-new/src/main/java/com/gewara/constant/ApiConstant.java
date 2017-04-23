package com.gewara.constant;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.collections.map.UnmodifiableMap;

import com.gewara.constant.ticket.OrderConstant;

/**
 * API订票系统中用到的常量
 * @author acerge(acerge@163.com)
 * @since 12:27:06 PM Apr 22, 2011
 * 0000	成功
 * 1001  场次不存在
 * 1002  场次当前未开放订票：1）关闭 2）只在10：00～18：00开放 3）影院关闭对外订票 等
 * 1003  影院场次信息不同步：需要等待Gewara系统更新影院信息
 *
 * 2001  无法连接影院
 * 2002  座位位置错误：产生孤座，单选情侣座等
 * 2003  座位被占用
 * 2004  座位数量限制错误：超出5个，某些场次中只能买两个或四个等
 * 2005  座位其他限制错误：有积分限制等
 * 2006  违反规则：如孤座等
 * 2098  正在更新影院数据
 * 2099  座位错误：未知错误
 * 4001  商家不存在  
 * 4002  商家无权限  
 * 4003  校验错误  
 * 4004  参数错误：缺少参数或格式不正确  
 * 4005  数据错误：如查询的数据不存在等
 * 4006  支付错误  
 *	
 * 5000	用户不存在
 * 5001	用户未登录
 * 5002	用户无权限
 * 5003	不能重复操作
 * 
 * 9999  未知错误：其他未分类的错误
 */
public abstract class ApiConstant {
	public static final String CODE_SUCCESS = "0000";				//
	public static final String CODE_OPI_NOT_EXISTS = "1001";
	public static final String CODE_OPI_CLOSED = "1002";
	public static final String CODE_OPI_UNSYNCH = "1003";
	
	public static final String CODE_CONNECTION_ERROR = "2001";
	public static final String CODE_SEAT_POS_ERROR = "2002";
	public static final String CODE_SEAT_OCCUPIED = "2003";
	public static final String CODE_SEAT_NUM_ERROR = "2004";
	public static final String CODE_SEAT_LIMITED = "2005";
	public static final String CODE_SEAT_BREAK_RULE = "2006";
	public static final String CODE_SEAT_LOCK_ERROR_CINEMA = "2010";//锁定座位出错
	public static final String CODE_CCTO_ERROR = "2011";			//第三方与影院连接不正常
	public static final String CODE_TC_ERROR = "2012";				//第三方错误
	public static final String CODE_SEAT_RELEASED = "2013";			//座位已经释放或不存在
	public static final String CODE_SYNCH_DATA = "2098";			//正在更新影院数据
	public static final String CODE_SEAT_LOCK_ERROR = "2099";
	
	public static final String CODE_PARTNER_NOT_EXISTS = "4001";
	public static final String CODE_PARTNER_NORIGHTS = "4002";
	public static final String CODE_SIGN_ERROR = "4003";
	public static final String CODE_PARAM_ERROR = "4004";
	public static final String CODE_DATA_ERROR = "4005";
	public static final String CODE_PAY_ERROR = "4006";
	
	public static final String CODE_WEIBO_EXPRIES = "4100"; //微博过期
	public static final String CODE_UNBIND_MOBILE = "4101"; //没有绑定手机
	
	public static final String CODE_MEMBER_NOT_EXISTS = "5000";
	public static final String CODE_NOTLOGIN = "5001";
	public static final String CODE_USER_NORIGHTS = "5002";
	public static final String CODE_REPEAT_OPERATION = "5003";
	public static final String CODE_NOT_EXISTS = "5004";
	
	public static final String CODE_PAYPASS_ERROR = "6001";	//支付密码过于简单
	
	public static final String CODE_UNKNOWN_ERROR = "9999";
	
	public static final Map<String, String> ORDER_STATUS_MAP;
	static{
		Map<String, String> tmp = new HashMap<String, String>();
		tmp.put(OrderConstant.STATUS_NEW, "new");
		tmp.put(OrderConstant.STATUS_NEW_UNLOCK, "new");
		tmp.put(OrderConstant.STATUS_NEW_CONFIRM, "new");
		tmp.put(OrderConstant.STATUS_PAID, "paid");
		tmp.put(OrderConstant.STATUS_PAID_FAILURE, "paid");
		tmp.put(OrderConstant.STATUS_PAID_UNFIX, "paid");
		tmp.put(OrderConstant.STATUS_PAID_SUCCESS, "success");
		tmp.put(OrderConstant.STATUS_PAID_RETURN, "refund");
		tmp.put(OrderConstant.STATUS_CANCEL, "cancel");
		tmp.put(OrderConstant.STATUS_SYS_CANCEL,"cancel");
		tmp.put(OrderConstant.STATUS_REPEAT,"repeat");
		tmp.put(OrderConstant.STATUS_USER_CANCEL,"cancel");
		tmp.put(OrderConstant.STATUS_TIMEOUT, "cancel");
		ORDER_STATUS_MAP = UnmodifiableMap.decorate(tmp);
	}
	public static Map<String, String> getOrderStatusMap(){
		return ORDER_STATUS_MAP;
	}
	public static String getMappedOrderStatus(String status){
		return ORDER_STATUS_MAP.get(status);
	}
}
