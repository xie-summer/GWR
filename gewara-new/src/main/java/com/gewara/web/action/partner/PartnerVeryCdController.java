/**
 * 
 */
package com.gewara.web.action.partner;

import java.util.Date;
import java.util.List;

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

import com.gewara.constant.AdminCityContant;
import com.gewara.constant.CityData;
import com.gewara.constant.PayConstant;
import com.gewara.constant.content.SignName;
import com.gewara.constant.ticket.PartnerConstant;
import com.gewara.model.api.ApiUser;
import com.gewara.model.content.GewaCommend;
import com.gewara.model.movie.Movie;
import com.gewara.model.pay.TicketOrder;
import com.gewara.pay.VeryCDUtil;
import com.gewara.support.ErrorCode;
import com.gewara.untrans.CommonService;
import com.gewara.util.HttpResult;
import com.gewara.util.WebUtils;

@Controller
public class PartnerVeryCdController extends BasePartnerController{
	@Autowired@Qualifier("commonService")
	private CommonService commonService;
	public void setCommonService(CommonService commonService) {
		this.commonService = commonService;
	}
	private ApiUser getVerycd(){
		return daoService.getObject(ApiUser.class, PartnerConstant.PARTNER_VERYCD);
	}
	
	@RequestMapping("/partner/verycd/index.xhtml")
	public String index(String encQryStr, String ukey, String come, String moviename, ModelMap model, HttpServletResponse response){
		if(StringUtils.isBlank(ukey)) PartnerUtil.setUkCookie(response, config.getBasePath() + "partner/verycd/");
		ApiUser partner = getVerycd();
		model.put("encQryStr", encQryStr);
		model.put("come", come);
		List<Movie> movieList = getOpenMovieListByDate(partner, partner.getDefaultCity(), null);
		super.filterMovieList(moviename, movieList, model);
		model.put("partner", partner);
		return "partner/verycd/index.vm";
	}
	
	@RequestMapping("/partner/verycd/movieDetail.xhtml")
	public String opiList(HttpServletResponse response, Date fyrq, Long movieid,String citycode,
			@CookieValue(required=false,value="ukey") String ukey, ModelMap model, HttpServletRequest request) {
		if(StringUtils.isBlank(ukey)) PartnerUtil.setUkCookie(response, config.getBasePath() + "partner/verycd/");
		ApiUser partner = getVerycd();
		if(StringUtils.isNotBlank(citycode)){
			if(!partner.supportsCity(citycode))citycode = partner.getDefaultCity();
			WebUtils.setCitycode(request, citycode, response);
			model.put("cityname", AdminCityContant.getCitycode2CitynameMap().get(citycode));
		}else{
			citycode = this.getCitycodeByPartner(partner, request, response);
		}
		addOpiListData(partner, movieid, fyrq, null, null, model, citycode);
		model.put("movieid", movieid);
		return "partner/verycd/opiList.vm";
	}
	
	//第二步：选择座位
	@RequestMapping("/partner/verycd/chooseSeat.xhtml")
	public String chooseSeat(Long mpid,@CookieValue(required=false,value="ukey") String ukey, HttpServletResponse response, ModelMap model){
		if(mpid==null) return showError(model, "缺少参数！");
		ApiUser apiUser = getVerycd();
		if(StringUtils.isBlank(ukey)) ukey = PartnerUtil.setUkCookie(response, config.getBasePath() + "partner/verycd/");
		return chooseSeat(apiUser, mpid, ukey, "partner/verycd/step2.vm", model);
	}
	@RequestMapping("/partner/verycd/seatPage.xhtml")
	public String seatPage(Long mpid, Long partnerid, Integer price,  @CookieValue(value="ukey", required=false) String ukey, ModelMap model){
		model.put("mpid", mpid);
		model.put("partnerid", partnerid);
		model.put("price", price);
		model.put("ukey", ukey);
		if(StringUtils.isBlank(ukey)){
			return showJsonError(model, "缺少参数，请刷新重试！");
		}
		ErrorCode<String> code = addSeatData(mpid, partnerid, ukey, model);
		if(!code.isSuccess()) return showJsonError(model, code.getMsg());
		return "partner/seatPage.vm";
	}
	//第三步:锁座位，加订单
	@RequestMapping("/partner/verycd/addOrder.xhtml")
	public String addOrder(@CookieValue(required=false,value="ukey") String ukey, 
			String captchaId, String captcha, Long mpid, String encQryStr,
			@RequestParam("mobile")String mobile,
			@RequestParam("seatid")String seatid, HttpServletRequest request, ModelMap model){
		if(mpid==null || StringUtils.isBlank(ukey)) return showError(model, "缺少参数！");
		boolean validCaptcha = controllerService.validateCaptcha(captchaId, captcha, WebUtils.getRemoteIp(request));
		String qryStr = StringUtils.isBlank(encQryStr)?"":"&encQryStr=" + encQryStr;
		String returnUrl = "partner/verycd/chooseSeat.xhtml?mpid=" + mpid + qryStr + "&mobile=" + mobile +"&r="+ System.currentTimeMillis();
		if(!validCaptcha) return alertMessage(model, "验证码错误！", returnUrl);
		ApiUser partner = getVerycd();
		String paymethod = "partnerPay";
		ErrorCode code = addOrder(mpid, mobile, seatid, ukey, partner, null, null, null, paymethod, WebUtils.getRemoteIp(request), model);
		if(code.isSuccess()) return showRedirect("/partner/" + partner.getPartnerpath() + "/showOrder.xhtml", model);
		return alertMessage(model, code.getMsg(), returnUrl);
	}
	//第四步：确认订单去支付（重新查看）
	@RequestMapping("/partner/verycd/showOrder.xhtml")
	public String showOrder(Long orderId, String encQryStr, String come, ModelMap model, 
			@CookieValue(value="ukey", required=false)String ukey){
		ApiUser partner = getVerycd();
		model.put("encQryStr", encQryStr);
		model.put("come", come);
		List<GewaCommend> gcList = commonService.getGewaCommendListByRelatedid(null, SignName.PARTNER_AD, partner.getId(), null, true, 0, 1);
		if(gcList.size()>0){
			model.put("gewaCommend", gcList.get(0));
		}
		model.put("partnerCityMap", CityData.getCityNameMap(partner.getCitycode()));
		return showOrder(ukey, orderId, partner, "partner/verycd/step3.vm", model);
	}
	//第四步：确认订单去支付
	@RequestMapping(method=RequestMethod.POST,value="/partner/verycd/saveOrder.xhtml")
	public String saveOrder(ModelMap model, String mobile,
			@CookieValue(required=false,value="ukey") String ukey, 
			@RequestParam("orderId")long orderId, String encQryStr){
		if(StringUtils.isBlank(ukey)) return showError(model, "缺少参数！");
		model.put("encQryStr", encQryStr);
		String paymethod = "partnerPay";
		return saveOrder(orderId, mobile, paymethod, "", ukey, model);
	}

	//查询杉德的订单，格瓦拉中间做代理
	@RequestMapping("/partner/verycd/qryOrder.xhtml")
	@ResponseBody
	public String qryOrder(String tradeno){
		TicketOrder order = daoService.getObjectByUkey(TicketOrder.class, "tradeNo", tradeno, false);
		if(order == null)return "verycd order not find";
		HttpResult result = VeryCDUtil.getQryOrder(order, getVerycd());
		if(StringUtils.contains(result.getResponse(),PayConstant.PUSH_FLAG_PAID)){
			return "success";
		}
		return result.getResponse();
	}
	
	//商家城市切换
	@RequestMapping("/partner/verycd/changeCity.xhtml")
	public String index(String cityname,String movieid, HttpServletRequest request, HttpServletResponse response,ModelMap model) {
		String citycode = AdminCityContant.getCodeByPinyin(cityname);
		if(StringUtils.isBlank(citycode)) citycode = "310000";
		WebUtils.setCitycode(request, citycode, response);
		model.put("movieid", movieid);
		return "redirect:/partner/verycd/movieDetail.xhtml";
	}
	
}
