package com.gewara.constant.ticket;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.collections.map.UnmodifiableMap;

public class RefundConstant {
	//public static final String STATUS_PREPAIR1 = "prepare"; 	//预处理
	public static final String STATUS_APPLY = "apply"; 		//申请
	public static final String STATUS_ACCEPT = "accept"; 	//接受
	public static final String STATUS_REJECT = "reject"; 	//拒绝退款
	public static final String STATUS_SUCCESS = "success";	//退款成功
	public static final String STATUS_FINISHED = "finish";	//处理结束
	//public static final String STATUS_CANCEL = "cancel";	//取消退款(prepair-->cancel)
	
	//需要执行的操作
	public static final String OP_CANCEL_TICKET = "cancelTicket";	//退票
	public static final String OP_ADJUST_SETTLE = "adjustSettle";	//结算调整
	public static final String OP_COMPENSATE = "compensate";		//补偿用户操作
	public static final String OP_RET2PARTNER = "ret2Partner";		//合作商家退款
	
	public static final String OP_RESULT_CANCEL_SUCCESS = "cancelSuccess"; //退票成功
	public static final String OP_RESULT_CANCEL_FAILURE = "cancelFailure"; //退票失败
	
	//退款类型 all 全额退款：part 部分退款：supplement 增补
	public static final String REFUNDTYPE_FULL = "full";			//全额退款
	public static final String REFUNDTYPE_PART = "part";			//部分退款
	public static final String REFUNDTYPE_SUPPLEMENT = "supplement";//增补差价
	
	//退款原因
	public static final String REASON_UNKNOWN = "unknown";			//未知
	public static final String REASON_USER = "user";				//用户退款
	public static final String REASON_GEWA = "gewa";				//Gewa退款
	public static final String REASON_MERCHANT = "merchant";		//商家（影院）退款
	public static final String REASON_PRICE = "price";				//价格调整
	
	//账户退款 Y：需要，N：不需要，O：未知, 参见描述(Other)，S: 已提交账务(Submit) R：财务已经返还(Refund)，F：财务返还出错(Failure)
	public static final String RETBACK_Y = "Y";
	public static final String RETBACK_N = "N";
	public static final String RETBACK_OTHER = "O";
	public static final String RETBACK_SUBMIT = "S";
	public static final String RETBACK_REFUND = "R";
	public static final String RETBACK_FAILURE = "F";
	
	//处理人
	public static final String REFUND_MANAGE_DEAL = "manageDeal";
	public static final String REFUND_FINANCE_DEAL = "financeDeal";
	public static final String REFUND_FINANCE_STATUS = "status";
	public static final String REFUND_FINANCE_RESON = "reson";

	
	public static final Map<String, String> textMap;
	public static final Map<String, String> refundTypeMap;
	public static final Map<String, String> reasonTypeMap;
	public static final Map<String, String> retbackMap;
	static{
		Map<String, String> tmp = new HashMap<String, String>();
		tmp.put(STATUS_APPLY, "新申请");
		tmp.put(STATUS_REJECT, "不接收退款");
		tmp.put(STATUS_SUCCESS, "退款成功");
		textMap = UnmodifiableMap.decorate(tmp);
		
		Map<String, String> tmpRefund = new HashMap<String, String>();
		tmpRefund.put(REFUNDTYPE_FULL, "全额退款");
		tmpRefund.put(REFUNDTYPE_PART, "部分退款");
		tmpRefund.put(REFUNDTYPE_SUPPLEMENT, "增补差价");
		refundTypeMap = UnmodifiableMap.decorate(tmpRefund);
		
		Map<String, String> tmpReason = new HashMap<String, String>();
		tmpReason.put(REASON_UNKNOWN, "未知");
		tmpReason.put(REASON_USER, "用户退款");
		tmpReason.put(REASON_GEWA, "Gewa退款");
		tmpReason.put(REASON_MERCHANT, "商家（影院）退款");
		tmpReason.put(REASON_PRICE, "价格调整");
		reasonTypeMap = UnmodifiableMap.decorate(tmpReason);
		
		Map<String, String> tmpRetack = new HashMap<String, String>();
		tmpRetack.put(RETBACK_Y, "需要");
		tmpRetack.put(RETBACK_N, "不需要");
		tmpRetack.put(RETBACK_OTHER, "参见描述");
		tmpRetack.put(RETBACK_SUBMIT, "已提交财务");
		tmpRetack.put(RETBACK_REFUND, "财务成功");
		tmpRetack.put(RETBACK_FAILURE, "财务失败");
		retbackMap = UnmodifiableMap.decorate(tmpRetack);
	}
}
