package com.gewara.web.action.gewapay;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.gewara.constant.ChargeConstant;
import com.gewara.constant.PaymethodConstant;
import com.gewara.constant.sys.LogTypeConstant;
import com.gewara.model.pay.Charge;
import com.gewara.model.pay.MemberAccount;
import com.gewara.model.user.Member;
import com.gewara.pay.LakapayUtil;
import com.gewara.pay.PayUtil;
import com.gewara.support.ErrorCode;
import com.gewara.untrans.monitor.RoleTag;
import com.gewara.util.DateUtil;

/**
 * 拉卡拉支付接口（只能用来充值）
 * @author acerge(acerge@163.com)
 * @since 6:44:02 PM Sep 29, 2010
 */
@Controller
public class LakalaPayController extends BasePayController{
	@RequestMapping("/pay/lakalaQry.xhtml")
	@ResponseBody
	public String lakalaQry(String v, String service, String mer_id, String sec_id, String req_id,
			String trade_no, String amount, String lakala_query_time, String sign){
		Map<String, String> signparams = new LinkedHashMap<String, String>();
		signparams.put("amount", amount);
		signparams.put("lakala_query_time", lakala_query_time);
		signparams.put("mer_id", mer_id);
		signparams.put("req_id", req_id);
		signparams.put("sec_id", sec_id);
		signparams.put("service", service);
		signparams.put("trade_no", trade_no);
		signparams.put("v", v);
		String datastr = LakapayUtil.getJoinParam(signparams);
		dbLogger.warnWithType(LogTypeConstant.LOG_TYPE_ORDER_PAY, datastr);
		boolean valid = LakapayUtil.verifySign(datastr, sign);
		if(valid){
			//生成新的充值交易
			Member member = paymentService.getMemberByMobile(trade_no);
			if(member == null){
				dbLogger.errorWithType(LogTypeConstant.LOG_TYPE_ORDER_PAY, "member not exists");
				return "member not exists";
			}
			MemberAccount account = daoService.getObjectByUkey(MemberAccount.class, "memberid", member.getId(), false);
			if(account == null){
				account = paymentService.createNewAccount(member);
				dbLogger.errorWithType(LogTypeConstant.LOG_TYPE_ORDER_PAY, "create new account");
			}
			int fee = new Double(amount).intValue();
			if(fee==0) return "amount error";
			
			Charge charge = new Charge(PayUtil.getChargeTradeNo(), ChargeConstant.WABIPAY);
			charge.setMemberid(member.getId());
			charge.setMembername(member.getNickname());
			charge.setPaymethod(PaymethodConstant.PAYMETHOD_LAKALA);
			charge.setPaybank("bk");
			charge.setTotalfee(fee);
			charge.setValidtime(DateUtil.addHour(charge.getAddtime(), 4));
			daoService.saveObject(charge);
			
			Map<String, String> params = new LinkedHashMap<String, String>();
			params.put("amount", amount);
			params.put("can_pay", "y");
			params.put("mer_id", mer_id);
			params.put("partner_bill_no", charge.getTradeNo());
			params.put("partner_extendinfo", "");
			params.put("partner_query_time", DateUtil.format(new Date(), "yyyyMMddHHmmss"));
			params.put("req_id", req_id);
			params.put("sec_id", sec_id);
			params.put("service", service);
			params.put("v", v);
			String str = LakapayUtil.getJoinParam(params);
			str += "&sign=" + LakapayUtil.getSign(str);
			return str;
		}else{
			dbLogger.warnWithType(LogTypeConstant.LOG_TYPE_ORDER_PAY, "error sign:" + datastr);
			return "error sign";
		}
	}
	//异步调用，改变数据库状态
	@RequestMapping("/pay/lakalaNotify.xhtml")
	@ResponseBody
	public String lakalaNotify(String v, String service, String mer_id, String sec_id, String req_id, 
			String trade_no, String amount, String amount_pay, String pay_type, String partner_bill_no, 
			String lakala_bill_no, String currency, String lakala_pay_time, String sign){
		Map<String, String> signparams = new LinkedHashMap<String, String>();
		signparams.put("amount", amount);
		signparams.put("amount_pay", amount_pay);
		signparams.put("currency", currency);
		signparams.put("lakala_bill_no", lakala_bill_no);
		signparams.put("lakala_pay_time", lakala_pay_time);
		signparams.put("mer_id", mer_id);
		signparams.put("partner_bill_no", partner_bill_no);
		signparams.put("pay_type", pay_type);
		signparams.put("req_id", req_id);
		signparams.put("sec_id", sec_id);
		signparams.put("service", service);
		signparams.put("trade_no", trade_no);
		signparams.put("v", v);
		String datastr = LakapayUtil.getJoinParam(signparams);
		dbLogger.errorWithType(LogTypeConstant.LOG_TYPE_ORDER_PAY, "queryStr:" + datastr);
		dbLogger.errorWithType(LogTypeConstant.LOG_TYPE_ORDER_PAY, "params:" + signparams);

		boolean valid = LakapayUtil.verifySign(datastr, sign);
		if(valid){
			dbLogger.warnWithType(LogTypeConstant.LOG_TYPE_ORDER_PAY, "charge:" + trade_no + ", " + amount_pay);
			Integer fee = Double.valueOf(amount_pay).intValue();
			Charge charge = null;
			try{
				ErrorCode<Charge> result = paymentService.bankPayCharge(partner_bill_no, true, lakala_bill_no, fee, PaymethodConstant.PAYMETHOD_LAKALA, pay_type, PaymethodConstant.PAYMETHOD_LAKALA,null);

				if(result.isSuccess()){
					processCharge(partner_bill_no, "lakala");
				}
				charge = result.getRetval();
			}catch(Exception e){
				dbLogger.warnWithType(LogTypeConstant.LOG_TYPE_ORDER_PAY, "充值调用失败，尽快处理，订单号：" +  partner_bill_no, e);
				monitorService.saveSysWarn("充值付款调用失败，尽快处理", "订单号：" +  partner_bill_no, RoleTag.dingpiao);
				charge = daoService.getObjectByUkey(Charge.class, "tradeNo", partner_bill_no, false);
			}
			if(charge.isPaid()){
				Map<String, String> params = new LinkedHashMap<String, String>();
				params.put("is_success", "y");
				params.put("lakala_bill_no", lakala_bill_no);
				params.put("mer_id", mer_id);
				params.put("partner_bill_no", partner_bill_no);
				params.put("partner_pay_time", DateUtil.format(charge.getUpdatetime(), "yyyyMMddHHmmss"));
				params.put("req_id", req_id);
				params.put("sec_id", sec_id);
				params.put("service", service);
				params.put("v", v);
				String str = LakapayUtil.getJoinParam(params);
				str += "&sign=" + LakapayUtil.getSign(str);
				return str;
			}else{
				return "paid failure";
			}
		}else{
			dbLogger.errorWithType(LogTypeConstant.LOG_TYPE_ORDER_PAY, "Lakala SignError");
			return "sign error";
		}
	}
}
