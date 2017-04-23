package com.gewara.web.action.partner;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.Cookie;
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

import com.gewara.constant.AdminCityContant;
import com.gewara.constant.CityData;
import com.gewara.constant.PayConstant;
import com.gewara.constant.PaymethodConstant;
import com.gewara.constant.content.SignName;
import com.gewara.constant.ticket.PartnerConstant;
import com.gewara.model.api.ApiUser;
import com.gewara.model.content.GewaCommend;
import com.gewara.model.movie.Movie;
import com.gewara.model.pay.TicketOrder;
import com.gewara.pay.PartnerPayUtil;
import com.gewara.support.ErrorCode;
import com.gewara.untrans.CommonService;
import com.gewara.util.BeanUtil;
import com.gewara.util.DateUtil;
import com.gewara.util.HttpUtils;
import com.gewara.util.StringUtil;
import com.gewara.util.VmUtils;
import com.gewara.util.WebUtils;
import com.gewara.web.util.PageUtil;

/**
 * 银联
 * 
 */
@Controller
public class PartnerUnionpayController extends BasePartnerController{

	@Autowired@Qualifier("commonService")
	private CommonService commonService;
	private static final int pageSize = 10;
	private String UNIONPAY_URL = "http://58.246.226.99/";//http://online.unionpay.com/
	
	private ApiUser getUnionpay(){
		ApiUser apiUser = daoService.getObject(ApiUser.class, PartnerConstant.PARTNER_UNIONPAY);
		Map infoMap = VmUtils.readJsonToMap(apiUser.getOtherinfo());
		if(infoMap.get("unionpayUrl") != null){
			UNIONPAY_URL = infoMap.get("unionpayUrl")+"";
		}
		return apiUser;
	}

	@RequestMapping("/partner/unionpay/index.xhtml")
	public String index(String token, String encQryStr, String moviename, String citycode,
			HttpServletResponse response, ModelMap model, HttpServletRequest request, Integer pageNo) {
		ApiUser partner = getUnionpay();
		try {
			if(StringUtils.isNotBlank(moviename))
			moviename = URLDecoder.decode(moviename,"UTF-8");
		} catch (UnsupportedEncodingException e) {}
		citycode = this.getCitycodeByPartner(citycode, partner, request, response, model);
		model.put("partnerCityMap", CityData.getCityNameMap(partner.getCitycode()));
		List<Movie> movieList = getOpenMovieListByDate(partner, citycode, null);
		model.put("partner", partner);
		token = "0028";
		ErrorCode code = addInitParams(token, encQryStr, model);
		if(!code.isSuccess()) return forwardMessage(model, code.getMsg());
		model.put("iframeUrl", VmUtils.getJsonValueByKey(partner.getOtherinfo(), PayConstant.KEY_IFRAME_URL));
		if(pageNo==null) pageNo = 0;
		movieList = filterMovieList1(moviename, movieList);
		int rowsCount = movieList.size();
		PageUtil pageUtil = new PageUtil(rowsCount , pageSize, pageNo,"partner/unionpay/index.xhtml", true, true);
		pageUtil.initPageInfo(request.getParameterMap());
		movieList = BeanUtil.getSubList(movieList, pageNo*pageSize, pageSize);
		model.put("pageUtil", pageUtil);
		model.put("movieList", movieList);
		model.put("unionpayUrl", UNIONPAY_URL);
		return "partner/unionpay/index.vm";
	}

	@RequestMapping("/partner/unionpay/movieList.xhtml")
	public String movieList(ModelMap model, HttpServletResponse response, HttpServletRequest request) {
		ApiUser partner = getUnionpay();
		String citycode = this.getCitycodeByPartner(null, partner, request, response, model);
		List<Movie> movieList = getOpenMovieListByDate(partner, citycode, null);
		movieList = BeanUtil.getSubList(movieList, 0, 9);
		model.put("partner", partner);
		model.put("movieList", movieList);
		model.put("unionpayUrl", UNIONPAY_URL);
		model.put("partnerCityMap", CityData.getCityNameMap(partner.getCitycode()));
		return "partner/unionpay/guidePage.vm";
	}
	
	@RequestMapping("/partner/unionpay/movieDetail.xhtml")
	public String movieDetail(Long movieid, @CookieValue(required=false,value="ukey") String ukey, String tokenId,
			String encQryStr, Date fyrq, HttpServletResponse response, ModelMap model, HttpServletRequest request){
		ApiUser partner = getUnionpay();
		Movie movie = daoService.getObject(Movie.class, movieid);
		if(movie==null) return forwardMessage(model, "影片不存在！");
		String citycode = this.getCitycodeByPartner(partner, request, response);
		model.put("partnerCityMap", CityData.getCityNameMap(partner.getCitycode()));
		addOpiListData(partner, movieid, fyrq, null, null, model, citycode);
		model.put("movie", movie);
		model.put("encQryStr", encQryStr);
		if(StringUtils.isBlank(encQryStr)){
			ErrorCode code = addInitParams(tokenId, encQryStr, model);
			if(!code.isSuccess()) return forwardMessage(model, code.getMsg());
		}
		if(StringUtils.isBlank(ukey)) {
			PartnerUtil.setUkCookie(response, config.getBasePath() + "partner/unionpay/");
		}
		this.getGewaCommend(model);
		model.put("iframeUrl", VmUtils.getJsonValueByKey(partner.getOtherinfo(), PayConstant.KEY_IFRAME_URL));
		model.put("unionpayUrl", UNIONPAY_URL);
		return "partner/unionpay/movieDetail.vm";
	}
	
	//第二步：选择座位
	@RequestMapping("/partner/unionpay/chooseSeat.xhtml")
	public String chooseSeat(@CookieValue(value="ukey", required=false) String ukey,
			Long mpid,String token,String encQryStr, ModelMap model, HttpServletRequest request, HttpServletResponse response){
		if(mpid==null) return forwardMessage(model, "缺少参数！");
		if(StringUtils.isBlank(ukey)) {
			ukey = PartnerUtil.setUkCookie(response, config.getBasePath() + "partner/unionpay/");
		}
		Map<String, String> reqParamMap = PartnerUtil.getParamMap(encQryStr);
		String userid = reqParamMap.get("userid");
		if(token != null){
			ErrorCode code = addInitParams(token, encQryStr, model);
			if(!code.isSuccess()) return forwardMessage(model, code.getMsg());
		}else if(StringUtils.isBlank(userid)) { 
			//return forwardMessage(model, "请先登录！");
		}
		ApiUser partner = getUnionpay();
		this.getGewaCommend(model);
		model.put("encQryStr", encQryStr);
		getCitycodeByPartner(partner, request, response);
		model.put("partnerCityMap", CityData.getCityNameMap(partner.getCitycode()));
		model.put("unionpayUrl", UNIONPAY_URL);
		String view = "partner/unionpay/chooseSeat.vm";
		String result = chooseSeat(partner, mpid, ukey, "partner/unionpay/chooseSeat.vm", model);
		if(!StringUtils.equals(result, view)){
			return "redirect:/partner/unionpay/chooseSeatError.xhtml";
		}
		return result;
	}
	

	@RequestMapping("/partner/unionpay/chooseSeatError.xhtml")
	public String chooseSeatError(){
		return "partner/unionpay/chooseSeatError.vm";
	}
	
	@RequestMapping("/partner/unionpay/seatPage.xhtml")
	public String seatPage(Long mpid, Long partnerid, Integer price, @CookieValue(value="ukey", required=false) String ukey, ModelMap model){
		model.put("mpid", mpid);
		model.put("partnerid", partnerid);
		model.put("price", price);
		model.put("ukey", ukey);
		if(StringUtils.isBlank(ukey)) { 
			return showJsonError(model, "缺少参数,请刷新重试！");
		}
		ErrorCode<String> code = addSeatData(mpid, partnerid, ukey, model);
		if(!code.isSuccess()) return showJsonError(model, code.getMsg());
		return "partner/unionpay/new_seatPage.vm";
	}
	
	//第三步:锁座位，加订单
	@RequestMapping("/partner/unionpay/addOrder.xhtml")
	public String addOrder(@CookieValue(required=false,value="ukey") String ukey,
			String captchaId, String captcha, Long mpid, String encQryStr, 
			@RequestParam("mobile") String mobile, @RequestParam("seatid")String seatid, HttpServletRequest request, ModelMap model){
		if(mpid==null) return showJsonError(model, "缺少参数！");
		//if(StringUtils.isBlank(encQryStr)) return showJsonError(model, "请先登录！");
		boolean validCaptcha = controllerService.validateCaptcha(captchaId, captcha, WebUtils.getRemoteIp(request));
		if(!validCaptcha) return showJsonError(model, "验证码错误！");
		Map<String, String> paramMap = PartnerUtil.getParamMap(encQryStr);
		String userid = paramMap.get("userid"); 
		//if(StringUtils.isBlank(userid)) return showJsonError(model, "获取用户标识错误,请重新登录！");
		ApiUser partner = getUnionpay();
		model.put("userid", userid);
		ErrorCode code = addOrder(mpid, mobile, seatid, ukey, partner, userid, null, null, PaymethodConstant.PAYMETHOD_PARTNERPAY, WebUtils.getRemoteIp(request), model);
		if(code.isSuccess()) return showJsonSuccess(model, model.get("orderId")+"");
		return showJsonError(model, code.getMsg());
	}
	
	//第四步：确认订单去支付
	@RequestMapping(method=RequestMethod.POST,value="/partner/unionpay/saveOrder.xhtml")
	public String saveOrder(ModelMap model, String mobile,
			@CookieValue(required=false,value="ukey") String ukey, String paybank,
			@RequestParam("orderId")long orderId, String encQryStr){
		if(StringUtils.isBlank(ukey)) return forwardMessage(model, "缺少参数！");
		model.put("encQryStr", encQryStr);
		String ret = saveOrder(orderId, mobile, PaymethodConstant.PAYMETHOD_PARTNERPAY, paybank, ukey, model);
		return ret;
	}
	
	//第四步：确认订单去支付（重新查看）
	@RequestMapping("/partner/unionpay/showOrder.xhtml")
	public String showOrder(Long orderId, String encQryStr, ModelMap model, String userid, 
			@CookieValue(value="ukey", required=false)String ukey, HttpServletRequest request, HttpServletResponse response){
		ApiUser partner = getUnionpay();
		model.put("encQryStr", encQryStr);
		this.getGewaCommend(model);
		model.put("iframeUrl", VmUtils.getJsonValueByKey(partner.getOtherinfo(), PayConstant.KEY_IFRAME_URL));
		this.getCitycodeByPartner(partner, request, response);
		model.put("partnerCityMap", CityData.getCityNameMap(partner.getCitycode()));
		if(StringUtils.isBlank(ukey)) ukey = userid;
		model.put("unionpayUrl", UNIONPAY_URL);
		String view = "partner/unionpay/confirmOrder.vm";
		String result = showOrder(ukey, orderId, partner, "partner/unionpay/confirmOrder.vm", model);
		if(!StringUtils.equals(view, result)){
			TicketOrder order = daoService.getObject(TicketOrder.class, orderId);
			model.put("tradeNo", order.getTradeNo());
			return "redirect:/partner/showOrderResult.xhtml";
		}
		return result;
	}

	//前台通知
	@RequestMapping("/partner/unionpay/payReturn.xhtml")
	public String payReturn(String key, String tradeno, String paidAmount, String payseqno, 
			String version, String checkvalue,ModelMap model, HttpServletRequest request){
		dbLogger.warn(WebUtils.getParamStr(request, true));
		if(StringUtils.isNotBlank(checkvalue)){
			model.put("tradeNo", tradeno);
			model.put("unionpayUrl", UNIONPAY_URL);
			Map<String,String> params = new HashMap<String, String>();
			params.put("key", key);
			params.put("tradeno", tradeno);
			params.put("paidAmount", paidAmount);
			params.put("payseqno", payseqno);
			params.put("version", version);
			params.put("checkvalue", checkvalue);
			ApiUser partner = getUnionpay();
			String notify = StringUtils.isNotBlank(partner.getNotifyurl())? partner.getNotifyurl(): PartnerPayUtil.NOTIFY_URL;
			HttpUtils.postUrlAsString(notify, params);
		}
		return "redirect:https://www.gewara.com/partner/showOrderResult.xhtml";
	}
	
	private ErrorCode addInitParams(String token, String encQryStr, ModelMap model){
		if(StringUtils.isNotBlank(token)){
			ErrorCode<String> user = getLoginUser(token);
			if(!user.isSuccess()) return user;
			Map<String, String> params = new LinkedHashMap<String, String>();
			params.put("userid", user.getRetval());
			String ukey = DateUtil.format(new Date(), "MMss") + StringUtil.getRandomString(4);
			params.put("ukey", ukey);
			encQryStr = PartnerUtil.getParamStr(params);
		}else if(StringUtils.isBlank(encQryStr)){
			return ErrorCode.SUCCESS;
		}
		model.put("encQryStr", encQryStr);
		return ErrorCode.SUCCESS;
	}
	
	private ErrorCode getLoginUser(String token){
		return ErrorCode.getSuccessReturn(StringUtil.getRandomString(10)+token);//FIXME:remove
	}
	
	public String setUkCookie(HttpServletResponse response,String cookieName,String cookieValue, String path){
		Map<String, String> params = new HashMap<String, String>();
		params.put("mobile_basePath", cookieValue);
		String ukey = PartnerUtil.getParamStr(params);
		Cookie cookie = new Cookie(cookieName, ukey);
		cookie.setPath(path);
		cookie.setMaxAge(60 * 60 * 12);//12 hour
		response.addCookie(cookie);
		return ukey;
	}
	
	private void getGewaCommend(ModelMap model){
		List<GewaCommend> gcList = commonService.getGewaCommendListByRelatedid(null, SignName.PARTNER_AD, getUnionpay().getId(), null, true, 0, 1);
		if(gcList.size()>0){
			model.put("gewaCommend", gcList.get(0));
		}
	}

	private List<Movie> filterMovieList1(String moviename,List<Movie> movieList){
		if(StringUtils.isNotBlank(moviename)){
			for (Movie movie :movieList) {
				if(StringUtils.contains(movie.getMoviename(),moviename)){
					List list = new ArrayList();
					list.add(movie);
					return list;
				}
			}
		}else if(StringUtils.isBlank(moviename)){
			return movieList;
		}
		return new ArrayList();
	}
	
	//商家城市切换
	@RequestMapping("/partner/unionpay/changeCity.xhtml")
	public String index(String cityname,String view,String citydes, HttpServletRequest request, HttpServletResponse response,ModelMap model) {
		String citycode = AdminCityContant.getCodeByPinyin(cityname);
		if(StringUtils.isBlank(citycode)) citycode = "310000";
		WebUtils.setCitycode(request, citycode, response);
		model.put("citydes", citydes);
		model.put("citycode", citycode);
		return "redirect:https://www.gewara.com/partner/"+view+".xhtml";
	}
	
}
