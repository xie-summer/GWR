package com.gewara.web.action.partner;

import java.util.Date;
import java.util.List;
import java.util.Map;

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

import com.gewara.constant.PaymethodConstant;
import com.gewara.constant.content.SignName;
import com.gewara.constant.ticket.PartnerConstant;
import com.gewara.model.api.ApiUser;
import com.gewara.model.content.GewaCommend;
import com.gewara.model.movie.Movie;
import com.gewara.support.ErrorCode;
import com.gewara.untrans.CommonService;
import com.gewara.util.BeanUtil;
import com.gewara.util.WebUtils;
import com.gewara.web.util.PageUtil;
/**
 * 
 *天天红火网 苏州社区
 */
@Controller
public class PartnerTTHongHuoSZController extends BasePartnerController {
	@Autowired@Qualifier("commonService")
	private CommonService commonService;
	public void setCommonService(CommonService commonService) {
		this.commonService = commonService;
	}
	private ApiUser getApiUser(){
		return daoService.getObject(ApiUser.class, PartnerConstant.PARTNER_TTHONGHUO_SZ);
	}
	private static final int pageSize = 10;
	@RequestMapping("/partner/tthonghuosz/index.xhtml")
	public String index(String moviename, ModelMap model,String encQryStr, 
			@CookieValue(required=false,value="ukey") String ukey, HttpServletResponse response,HttpServletRequest request,Integer pageNo){
		ApiUser partner = getApiUser();
		if(StringUtils.isBlank(ukey)) PartnerUtil.setUkCookie(response, config.getBasePath() + "partner/tthonghuosz/");
		List<Movie> movieList = getOpenMovieListByDate(partner, partner.getDefaultCity(), null);
		int rowsCount = StringUtils.isBlank(moviename) ? movieList.size() : 1;
		if(pageNo==null) pageNo = 0;
		PageUtil pageUtil = new PageUtil(rowsCount , pageSize, pageNo,"partner/tthonghuosz/index.xhtml", true, true);
		pageUtil.initPageInfo(request.getParameterMap());
		movieList = BeanUtil.getSubList(movieList, pageNo*pageSize, pageSize);
		model.put("pageUtil", pageUtil);
		model.put("movieList", movieList);
		super.filterMovieList(moviename, movieList, model);
		model.put("partner", partner);
		model.put("paramsStr", "");
		model.put("encQryStr", encQryStr);
		return "partner/tthonghuosz/index.vm";
	}
	
	@RequestMapping("/partner/tthonghuosz/movieDetail.xhtml")
	public String movieDetail(Long movieid, String encQryStr,@CookieValue(required=false,value="ukey") String ukey,Date fyrq, 
			HttpServletResponse response, ModelMap model){
		ApiUser partner = getApiUser();
		Movie movie = daoService.getObject(Movie.class, movieid);
		if(movie==null) return forwardMessage(model, "影片不存在！");
		opiList(partner, movieid, fyrq, "partner/tthonghuosz/step1.vm", model);
		model.put("movie", movie);
		if(StringUtils.isBlank(ukey)) PartnerUtil.setUkCookie(response, config.getBasePath() + "partner/tthonghuosz/");
		List<GewaCommend> gcList = commonService.getGewaCommendListByRelatedid(null, SignName.PARTNER_AD, partner.getId(), null, true, 0, 1);
		if(gcList.size()>0){
			model.put("gewaCommend", gcList.get(0));
		}
		model.put("paramsStr", "");
		model.put("encQryStr", encQryStr);
		return "partner/tthonghuosz/movieDetail.vm";
	}
	
	//第二步：选择座位
	@RequestMapping("/partner/tthonghuosz/chooseSeat.xhtml")
	public String chooseSeat(Long mpid,String encQryStr, ModelMap model){
		if(mpid==null) return showError(model, "缺少参数！");
		Map<String, String> reqParamMap = PartnerUtil.getParamMap(encQryStr);
		ApiUser apiUser = getApiUser();
		return chooseSeat(apiUser, mpid, reqParamMap.get("userId"), "partner/tthonghuosz/chooseSeat.vm", model);
	}
	
	@RequestMapping("/partner/tthonghuosz/seatPage.xhtml")
	public String seatPage(Long mpid, Long partnerid, Integer price,  @CookieValue(value="ukey", required=false) String ukey, ModelMap model){
		model.put("mpid", mpid);
		model.put("partnerid", partnerid);
		model.put("price", price);
		model.put("ukey", ukey);
		ErrorCode<String> code = addSeatData(mpid, partnerid, ukey, model);
		if(!code.isSuccess()) return showJsonError(model, code.getMsg());
		return "partner/new_seatPage.vm";
	}
	//第三步:锁座位，加订单
	@RequestMapping("/partner/tthonghuosz/addOrder.xhtml")
	public String addOrder(String captchaId, String captcha, Long mpid,
			@CookieValue(value="ukey", required=false) String ukey,
			@RequestParam("mobile")String mobile,
			@RequestParam("seatid")String seatid, HttpServletRequest request, ModelMap model){
		ApiUser partner = getApiUser();
		boolean validCaptcha = controllerService.validateCaptcha(captchaId, captcha, WebUtils.getRemoteIp(request));
		if(!validCaptcha) return showJsonError(model, "验证码错误！");
		String paymethod = PaymethodConstant.PAYMETHOD_ALIPAY;
		ErrorCode code = addOrder(mpid, mobile, seatid, ukey, partner, partner.getBriefname(), null, null, paymethod, WebUtils.getRemoteIp(request), model);
		if(code.isSuccess()) {
			return showJsonSuccess(model, model.get("orderId")+"");
		}
		return showJsonError(model, code.getMsg());
	}
	//第四步：确认订单去支付（重新查看）
	@RequestMapping("/partner/tthonghuosz/showOrder.xhtml")
	public String showOrder(Long orderId, String encQryStr,  ModelMap model, 
			  @CookieValue(value="ukey", required=false) String ukey,
			String phonenumber,String checkvalue){
		ApiUser partner = getApiUser();
		model.put("encQryStr", encQryStr);
		model.put("phonenumber", phonenumber);
		model.put("checkvalue", checkvalue);
		List<GewaCommend> gcList = commonService.getGewaCommendListByRelatedid(null, SignName.PARTNER_AD, partner.getId(), null, true, 0, 1);
		if(gcList.size()>0){
			model.put("gewaCommend", gcList.get(0));
		}
		return showOrder(ukey, orderId, partner, "partner/tthonghuosz/confirmOrder.vm", model);
	}
	//第四步：确认订单去支付
	@RequestMapping(method=RequestMethod.POST,value="/partner/tthonghuosz/saveOrder.xhtml")
	public String saveOrder(ModelMap model, String mobile,@CookieValue(value="ukey", required=false) String ukey,
			@RequestParam("orderId")long orderId, String encQryStr){
		model.put("encQryStr", encQryStr);
		String paymethod = PaymethodConstant.PAYMETHOD_ALIPAY;
		return saveOrder(orderId, mobile, paymethod, "", ukey, model);
	}
}
