/**
 * 
 */
package com.gewara.web.action.partner;

import java.util.Date;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.gewara.constant.PayConstant;
import com.gewara.constant.sys.LogTypeConstant;
import com.gewara.constant.ticket.PartnerConstant;
import com.gewara.model.api.ApiUser;
import com.gewara.model.pay.TicketOrder;
import com.gewara.pay.SandPayUtil;
import com.gewara.support.ErrorCode;
import com.gewara.untrans.PartnerWebService;
import com.gewara.util.WebUtils;
import com.gewara.xmlbind.pay.QrySandOrder;

/**
 * 杉德金卡通
 * @author Administrator
 *
 */
@Controller
public class PartnerSandController extends BasePartnerController{
	
	private final String PAID_SUCCESS = "0000";
	@Autowired@Qualifier("partnerWebService")
	private PartnerWebService partnerWebService;
	
	private ApiUser getSand(){
		return daoService.getObject(ApiUser.class, PartnerConstant.PARTNER_SAND);
	}
	
	@RequestMapping("/partner/sand/opiList.xhtml")
	public String opiList(HttpServletResponse response, Date fyrq, Long movieid,
			@CookieValue(required=false,value="ukey") String ukey, ModelMap model) {
		if(StringUtils.isBlank(ukey)) PartnerUtil.setUkCookie(response, config.getBasePath() + "partner/sand/");
		ApiUser partner = getSand();
		addOpiListData(partner, movieid, fyrq, null, null, model);
		return "partner/sand/step1.vm";
	}
	
	//第二步：选择座位
	@RequestMapping("/partner/sand/chooseSeat.xhtml")
	public String chooseSeat(Long mpid, @CookieValue(required=false,value="ukey") String ukey, ModelMap model,
			 HttpServletRequest request, HttpServletResponse response){
		if(mpid==null) return forwardMessage(model, "缺少参数！");
		if(StringUtils.isBlank(ukey)) PartnerUtil.setUkCookie(response, config.getBasePath() + "partner/sand/");
		ApiUser partner = getSand();
		this.getCitycodeByPartner(partner, request, response);
		if(StringUtils.isBlank(ukey)){
			return showJsonError(model, "缺少参数，请刷新重试！");
		}
		return chooseSeat(partner, mpid, ukey, "partner/sand/step2.vm", model);
	}
	
	@RequestMapping("/partner/sand/seatPage.xhtml")
	public String seatPage(Long mpid, Long partnerid, Integer price,  @CookieValue(value="ukey", required=false) String ukey, ModelMap model){
		model.put("mpid", mpid);
		model.put("partnerid", partnerid);
		model.put("price", price);
		model.put("ukey", ukey);
		ErrorCode<String> code = addSeatData(mpid, partnerid, ukey, model);
		if(!code.isSuccess()) return showJsonError(model, code.getMsg());
		return "partner/seatPage.vm";
	}
	
	//第三步:锁座位，加订单
	@RequestMapping("/partner/sand/addOrder.xhtml")
	public String addOrder(@CookieValue(required=false,value="ukey") String ukey, 
			String captchaId, String captcha, Long mpid, @RequestParam("mobile")String mobile,
			@RequestParam("seatid")String seatid, HttpServletRequest request, ModelMap model){
		if(mpid==null || StringUtils.isBlank(ukey)) return showJsonError(model, "缺少参数！");
		boolean validCaptcha = controllerService.validateCaptcha(captchaId, captcha, WebUtils.getRemoteIp(request));
		if(!validCaptcha) return showJsonError(model, "验证码错误！");
		ApiUser partner = getSand();
		String paymethod = "partnerPay";
		ErrorCode code = addOrder(mpid, mobile, seatid, ukey, partner, null, null, null, paymethod, WebUtils.getRemoteIp(request), model);
		if(!code.isSuccess())return showJsonError(model, code.getMsg()); 
		return showJsonSuccess(model, model.get("orderId")+"");
	}
	
	//第四步:确认订单
	@RequestMapping("/partner/sand/showOrder.xhtml")
	public String showOrder(@CookieValue(required=false,value="ukey") String ukey, Long orderId, ModelMap model){
		if(StringUtils.isBlank(ukey)) return showJsonError(model, "缺少参数！");
		ApiUser partner = getSand();
		return showOrder(ukey, orderId, partner, "partner/sand/step3.vm", model);
	}
	
	//第五步:跳转到杉德支付
	@RequestMapping(method=RequestMethod.POST,value="/partner/sand/saveOrder.xhtml")
	public String saveOrder(ModelMap model, @RequestParam("orderId")long orderId, 
			String mobile, @CookieValue(value="ukey", required=false)String ukey){
		if(StringUtils.isBlank(ukey)) return showJsonError(model, "缺少参数！");
		String paymethod = "partnerPay";
		return saveOrder(orderId, mobile, paymethod, "", ukey, model);
	}

	//查询杉德的订单，格瓦拉中间做代理
	@RequestMapping("/partner/sand/qryOrder.xhtml")
	@ResponseBody
	public String qryOrder(String tradeno){
		TicketOrder order = daoService.getObjectByUkey(TicketOrder.class, "tradeNo", tradeno, false);
		if(order == null)return "sand order not find";
		QrySandOrder result = partnerWebService.qrySandOrder(order);
		boolean flag = SandPayUtil.checkSign(result);
		if(!flag){
			dbLogger.warnWithType(LogTypeConstant.LOG_TYPE_PARTNER, "杉德 check sign error：" + result.getSign() + ",Tradeno:"+result.getTradeno());
			return "sign error";
		}
		dbLogger.warnWithType(LogTypeConstant.LOG_TYPE_PARTNER, "杉德银卡通查询结果：" + result.getOrder_stauts() + ",Tradeno:"+result.getTradeno());
		if(StringUtils.equals(PAID_SUCCESS,  result.getOrder_stauts()))return PayConstant.PUSH_FLAG_PAID;
		return result.getOrder_stauts();
	}
	
}
