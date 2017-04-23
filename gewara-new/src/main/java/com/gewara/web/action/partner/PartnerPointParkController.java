/**
 * 
 */
package com.gewara.web.action.partner;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.gewara.constant.AdminCityContant;
import com.gewara.constant.CityData;
import com.gewara.constant.ticket.PartnerConstant;
import com.gewara.model.api.ApiUser;
import com.gewara.model.pay.TicketOrder;
import com.gewara.pay.PointParkUtil;
import com.gewara.support.ErrorCode;
import com.gewara.util.JsonUtils;
import com.gewara.util.PKCoderUtil;
import com.gewara.util.VmUtils;
import com.gewara.util.WebUtils;

/**
 * 交通银行积分乐园
 * @author Administrator
 *
 */
@Controller
public class PartnerPointParkController extends BasePartnerController{
	private ApiUser getPointPark(){
		return daoService.getObject(ApiUser.class, PartnerConstant.PARTNER_POINTPARK);
	}
	public static final String DES3 = "UcZuqnZIRK9Vil1Rb+Q0r2i/TDB/iT3D";
	@RequestMapping("/partner/pointpark/opiList.xhtml")
	public String opiList(HttpServletResponse response, Date fyrq, Long movieid,
			@CookieValue(required=false,value="ukey") String ukey, String plaintext, String sign, String citydes,
			ModelMap model, HttpServletRequest request) {
		Map<String, String> paramsMap = null;
		Map<String, String> desMap = new HashMap<String, String>();
		if(StringUtils.isNotBlank(citydes)){
			String strDes = PKCoderUtil.decryptString(citydes, DES3);
			desMap = VmUtils.readJsonToMap(strDes);
			plaintext = desMap.get("plaintext");
			sign = desMap.get("sign");
		}
		desMap.put("plaintext", plaintext);
		desMap.put("sign", sign);
		paramsMap = VmUtils.readJsonToMap(plaintext);
		String format = "JSON";
		ErrorCode<String> code = PointParkUtil.validParams(format, plaintext, sign);
		if(!code.isSuccess()) return forwardMessage(model, "参数有错误！");
		String ppmid = code.getRetval();
		if(StringUtils.isBlank(ukey)) PartnerUtil.setUkCookie(response, config.getBasePath() + "partner/pointpark/");
		ApiUser partner = getPointPark();
		model.put("memberId", paramsMap.get("memberId"));
		model.put("tpi", paramsMap.get("tpi"));
		model.put("timestamp", paramsMap.get("timestamp"));
		model.put("sign", sign);
		model.put("ppmid", ppmid);
		model.put("citydes", PKCoderUtil.encryptString(JsonUtils.writeMapToJson(desMap), DES3));
		String citycode = this.getCitycodeByPartner(partner, request, response);
		model.put("partnerCityMap", CityData.getCityNameMap(partner.getCitycode()));
		addOpiListData(partner, movieid, fyrq, null, null, model, citycode);
		return "partner/pointpark/step1.vm";
	}
	//第二步：选择座位
	@RequestMapping("/partner/pointpark/chooseSeatTest.xhtml")
	public String chooseSeatTest(Long mpid, @CookieValue(value="ukey", required=false) String ukey, ModelMap model, HttpServletRequest request, HttpServletResponse response){
		if(mpid==null) return forwardMessage(model, "缺少参数！");
		if(StringUtils.isBlank(ukey)) {
			PartnerUtil.setUkCookie(response, config.getBasePath() + "partner/pointpark/");
		}
		
		String ppmid = "M000001";
		ApiUser partner = getPointPark();
		model.put("ppmid", ppmid);
		this.getCitycodeByPartner(partner, request, response);
		model.put("partnerCityMap", CityData.getCityNameMap(partner.getCitycode()));
		return chooseSeat(partner, mpid, ppmid, "partner/pointpark/step2.vm", model);
	}
	//第二步：选择座位
	@RequestMapping("/partner/pointpark/chooseSeat.xhtml")
	public String chooseSeat(Long mpid, String format, String plaintext, String sign, String ppmid, ModelMap model,
			String citydes, HttpServletRequest request, HttpServletResponse response){
		dbLogger.warn(plaintext);
		if(mpid==null) return forwardMessage(model, "缺少参数！");
		if(StringUtils.isBlank(ppmid)){
			if(StringUtils.isNotBlank(plaintext)){
				ErrorCode<String> code = PointParkUtil.validParams(format, plaintext, sign);
				if(!code.isSuccess()) return forwardMessage(model, "参数有错误！");
				ppmid = code.getRetval();
			}
		}
		if(StringUtils.isBlank(ppmid)) {
			model.put("mpid", mpid);
			return "redirect:/partner/pointpark/ajaxLogin.xhtml";
		}
		ApiUser partner = getPointPark();
		model.put("ppmid", ppmid);
		model.put("citydes", citydes);
		this.getCitycodeByPartner(partner, request, response);
		model.put("partnerCityMap", CityData.getCityNameMap(partner.getCitycode()));
		return chooseSeat(partner, mpid, ppmid, "partner/pointpark/step2.vm", model);
	}
	@RequestMapping("/partner/pointpark/seatPage.xhtml")
	public String seatPage(Long mpid, Long partnerid, Integer price,  @CookieValue(value="ukey", required=false) String ukey, HttpServletResponse response, ModelMap model){
		model.put("mpid", mpid);
		model.put("partnerid", partnerid);
		model.put("price", price);
		model.put("ukey", ukey);
		if(StringUtils.isBlank(ukey)) ukey = PartnerUtil.setUkCookie(response, config.getBasePath() + "partner/pointpark/");
		ErrorCode<String> code = addSeatData(mpid, partnerid, ukey, model);
		if(!code.isSuccess()) return showJsonError(model, code.getMsg());
		return "partner/seatPage.vm";
	}
	//第三步:锁座位，加订单
	@RequestMapping("/partner/pointpark/addOrder.xhtml")
	public String addOrder(@CookieValue(required=false,value="ukey") String ukey, String ppmid,
			String captchaId, String captcha, Long mpid, @RequestParam("mobile")String mobile,
			@RequestParam("seatid")String seatid,String citydes, HttpServletRequest request, ModelMap model){
		if(mpid==null || StringUtils.isBlank(ukey)) return showJsonError(model, "缺少参数！");
		boolean validCaptcha = controllerService.validateCaptcha(captchaId, captcha, WebUtils.getRemoteIp(request));
		if(!validCaptcha) return showJsonError(model, "验证码错误！");
		if(StringUtils.isBlank(ppmid)) return showJsonError(model, "请先登录！");
		ApiUser partner = getPointPark();
		String paymethod = "partnerPay";
		model.put("citydes", citydes);
		ErrorCode code = addOrder(mpid, mobile, seatid, ukey, partner, ppmid, null, null, paymethod, WebUtils.getRemoteIp(request), model);
		if(!code.isSuccess())return showJsonError(model, code.getMsg()); 
		return showJsonSuccess(model, model.get("orderId")+"");
	}
	
	//第四步：确认订单去支付（重新查看）
	@RequestMapping("/partner/pointpark/showOrder.xhtml")
	public String pointparkShowOrder(Long orderId, ModelMap model, @CookieValue(value="ukey", required=false)String ukey, 
			String citydes, HttpServletRequest request, HttpServletResponse response){
		ApiUser partner = getPointPark();
		this.getCitycodeByPartner(partner, request, response);
		model.put("citydes", citydes);
		model.put("partnerCityMap", CityData.getCityNameMap(partner.getCitycode()));
		return showOrder(ukey, orderId, partner, "partner/pointpark/step3.vm", model);
	}
	
	//第四步：确认订单去支付
	@RequestMapping(method=RequestMethod.POST,value="/partner/pointpark/saveOrder.xhtml")
	public String saveOrder(ModelMap model, @RequestParam("orderId")long orderId, 
			String mobile, @CookieValue(value="ukey", required=false)String ukey){
		if(StringUtils.isBlank(ukey)) return showJsonError(model, "缺少参数！");
		String paymethod = "partnerPay";
		return saveOrder(orderId, mobile, paymethod, "", ukey, model);
	}
	
	//查询积分乐园的订单，格瓦拉中间做代理
	@RequestMapping("/partner/pointpark/qryOrder.xhtml")
	@ResponseBody
	public String qryOrder(String tradeno, String payseqno){
		ApiUser apiUser = getPointPark();
		TicketOrder order = daoService.getObjectByUkey(TicketOrder.class, "tradeNo", tradeno, false);
		String result = PointParkUtil.getQryResult(order, payseqno, apiUser);
		dbLogger.warn("积分乐园查询结果：" + result);
		return result;
	}
	//查询积分乐园的订单，格瓦拉中间做代理
	@RequestMapping("/partner/pointpark/ajaxLogin.xhtml")
	public String ajaxLogin(Long mpid, ModelMap model){
		ApiUser apiUser = getPointPark();
		Map<String, String> paramsMap = PointParkUtil.getLoginParams(mpid, config.getAbsPath(), apiUser.getPrivatekey());
		model.put("paramsMap", paramsMap);
		model.put("loginUrl", PointParkUtil.getParamsMap(apiUser).get("loginUrl"));
		return "partner/pointpark/login.vm";
	}
	//查询积分乐园的订单，格瓦拉中间做代理
	@RequestMapping("/partner/pointpark/ajaxLogin2.xhtml")
	public String ajaxLogin2(Long mpid, ModelMap model){
		ApiUser apiUser = getPointPark();
		Map<String, String> paramsMap = PointParkUtil.getLoginParams(mpid, config.getAbsPath(), apiUser.getPrivatekey());
		model.put("paramsMap", paramsMap);
		String loginUrl = PointParkUtil.getParamsMap(apiUser).get("loginUrl");
		String format = paramsMap.get("format");
		String plaintext = paramsMap.get("plaintext");
		String sign = paramsMap.get("sign");
		return showJsonSuccess(model, loginUrl+"?format="+format+"&plaintext="+plaintext+"&sign="+sign);
	}
	//商家城市切换
	@RequestMapping("/partner/changeCity.xhtml")
	public String index(String cityname,String view,String citydes, HttpServletRequest request, HttpServletResponse response,ModelMap model) {
		String citycode = AdminCityContant.getCodeByPinyin(cityname);
		if(StringUtils.isBlank(citycode)) citycode = "310000";
		WebUtils.setCitycode(request, citycode, response);
		model.put("citydes", citydes);
		model.put("citycode", citycode);
		return "redirect:/partner/"+view+".xhtml";
	}
}
