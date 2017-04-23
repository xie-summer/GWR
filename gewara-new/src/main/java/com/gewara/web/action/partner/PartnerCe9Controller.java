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

import com.gewara.constant.CityData;
import com.gewara.constant.content.SignName;
import com.gewara.constant.ticket.PartnerConstant;
import com.gewara.model.api.ApiUser;
import com.gewara.model.content.GewaCommend;
import com.gewara.model.movie.Movie;
import com.gewara.support.ErrorCode;
import com.gewara.untrans.CommonService;
import com.gewara.util.WebUtils;

@Controller
public class PartnerCe9Controller extends BasePartnerController{
	@Autowired@Qualifier("commonService")
	private CommonService commonService;
	public void setCommonService(CommonService commonService) {
		this.commonService = commonService;
	}
	private ApiUser getCE9(){
		return daoService.getObject(ApiUser.class, PartnerConstant.PARTNER_CE9);
	}
	
	@RequestMapping("/partner/ce9/index.xhtml")
	public String index(String encQryStr,String moviename,String citycode, ModelMap model, 
			@CookieValue(required=false,value="ukey") String ukey, HttpServletResponse response, HttpServletRequest request){
		ApiUser partner = getCE9();
		if(StringUtils.isBlank(ukey)) PartnerUtil.setUkCookie(response, config.getBasePath() + "partner/ce9/");
		model.put("encQryStr", encQryStr);
		List<Movie> movieList = getOpenMovieListByDate(partner, citycode, null, response, request, model);
		super.filterMovieList(moviename, movieList, model);
		model.put("partnerCityMap", CityData.getCityNameMap(partner.getCitycode()));
		model.put("partner", partner);
		return "partner/ce9/index.vm";
	}
	
	@RequestMapping("/partner/ce9/movieDetail.xhtml")
	public String movieDetail(Long movieid, @CookieValue(required=false,value="ukey") String ukey, String come,
			String encQryStr,Date fyrq, HttpServletResponse response, ModelMap model, String citycode, HttpServletRequest request){
		ApiUser partner = getCE9();
		Movie movie = daoService.getObject(Movie.class, movieid);
		if(movie==null) return forwardMessage(model, "影片不存在！");
		citycode = this.getCitycodeByPartner(partner, request, response);
		addOpiListData(partner, movieid, fyrq, null, null, model, citycode);
		model.put("movie", movie);
		if(StringUtils.isBlank(ukey)) PartnerUtil.setUkCookie(response, config.getBasePath() + "partner/ce9/");
		List<GewaCommend> gcList = commonService.getGewaCommendListByRelatedid(null, SignName.PARTNER_AD, partner.getId(), null, true, 0, 1);
		if(gcList.size()>0){
			model.put("gewaCommend", gcList.get(0));
		}
		model.put("come", come);
		model.put("encQryStr", encQryStr);
		return "partner/ce9/movieDetail.vm";
	}
	
	//第二步：选择座位
	@RequestMapping("/partner/ce9/chooseSeat.xhtml")
	public String chooseSeat(Long mpid,String encQryStr,@CookieValue(required=false,value="ukey") String ukey, HttpServletResponse response, String phonenumber,String checkvalue, ModelMap model){
		if(mpid==null) return showError(model, "缺少参数！");
		ApiUser apiUser = getCE9();
		model.put("encQryStr", encQryStr);
		model.put("phonenumber", phonenumber);
		model.put("checkvalue", checkvalue);
		if(StringUtils.isBlank(ukey)) ukey = PartnerUtil.setUkCookie(response, config.getBasePath() + "partner/ce9/");
		return chooseSeat(apiUser, mpid, ukey, "partner/ce9/step2.vm", model);
	}
	@RequestMapping("/partner/ce9/seatPage.xhtml")
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
	@RequestMapping("/partner/ce9/addOrder.xhtml")
	public String addOrder(String phonenumber,String checkvalue,
			String captchaId, String captcha, Long mpid, String encQryStr,
			  @CookieValue(value="ukey", required=false) String ukey,
			@RequestParam("mobile")String mobile,
			@RequestParam("seatid")String seatid, HttpServletRequest request, ModelMap model){
		ApiUser partner = getCE9();
		boolean validCaptcha = controllerService.validateCaptcha(captchaId, captcha, WebUtils.getRemoteIp(request));
		String qryStr = StringUtils.isBlank(encQryStr)?"":"&encQryStr=" + encQryStr;
		String returnUrl = "partner/ce9/chooseSeat.xhtml?mpid=" + mpid + qryStr + "&mobile=" + mobile 
		+"&r="+ System.currentTimeMillis()+"&phonenumber="+phonenumber+"&checkvalue="+checkvalue;
		if(!validCaptcha) return alertMessage(model, "验证码错误！", returnUrl);
		model.put("phonenumber", phonenumber);
		model.put("checkvalue", checkvalue);
		String paymethod = "partnerPay";
		ErrorCode code = addOrder(mpid, mobile, seatid, ukey, partner, phonenumber, null, null, paymethod, WebUtils.getRemoteIp(request), model);
		if(code.isSuccess()) return showRedirect("/partner/" + partner.getPartnerpath() + "/showOrder.xhtml", model);
		return alertMessage(model, code.getMsg(), returnUrl);
	}
	//第四步：确认订单去支付（重新查看）
	@RequestMapping("/partner/ce9/showOrder.xhtml")
	public String showOrder(Long orderId, String encQryStr, String come, ModelMap model, 
			  @CookieValue(value="ukey", required=false) String ukey,
			String phonenumber,String checkvalue){
		ApiUser partner = getCE9();
		model.put("encQryStr", encQryStr);
		model.put("phonenumber", phonenumber);
		model.put("checkvalue", checkvalue);
		model.put("come", come);
		List<GewaCommend> gcList = commonService.getGewaCommendListByRelatedid(null, SignName.PARTNER_AD, partner.getId(), null, true, 0, 1);
		if(gcList.size()>0){
			model.put("gewaCommend", gcList.get(0));
		}
		return showOrder(ukey, orderId, partner, "partner/ce9/step3.vm", model);
	}
	//第四步：确认订单去支付
	@RequestMapping(method=RequestMethod.POST,value="/partner/ce9/saveOrder.xhtml")
	public String saveOrder(ModelMap model, String mobile,//String phonenumber,
			  @CookieValue(value="ukey", required=false) String ukey,
			@RequestParam("orderId")long orderId, String encQryStr){
		model.put("encQryStr", encQryStr);
		String paymethod = "partnerPay";
		return saveOrder(orderId, mobile, paymethod, "", ukey, model);
	}
}
