package com.gewara.web.action.partner;

import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.gewara.constant.PaymethodConstant;
import com.gewara.constant.ticket.PartnerConstant;
import com.gewara.helper.ticket.PartnerPriceHelper;
import com.gewara.model.api.ApiUser;
import com.gewara.model.common.County;
import com.gewara.model.movie.Cinema;
import com.gewara.model.movie.Movie;
import com.gewara.model.pay.TicketOrder;
import com.gewara.model.ticket.OpenPlayItem;
import com.gewara.support.ErrorCode;
import com.gewara.util.BeanUtil;
import com.gewara.util.VmUtils;
import com.gewara.util.WebUtils;

@Controller
public class PartnerJuXiangController extends BasePartnerController{
	private ApiUser getJuXiang(){
		return daoService.getObject(ApiUser.class, PartnerConstant.PARTNER_JU_XIANG);
	}
	
	private String getResolution(String width){
		if(StringUtils.isBlank(width) || !StringUtils.equals("1024", width)){
			return "";
		}
		return "1024";
	}
	
	@RequestMapping("/partner/juxiang/opiList.xhtml")
	public String opiList(HttpServletResponse response,Long movieid,Date fyrq,int pageNo ,
			@CookieValue(required=false,value="ukey") String ukey,ModelMap model,String width) {
		if(StringUtils.isBlank(ukey)) PartnerUtil.setUkCookie(response, config.getBasePath() + "partner/juxiang/");
		ApiUser partner = getJuXiang();
		List<Date> dateList = null;
		if(fyrq == null){
			dateList = partnerService.getPlaydateList(partner,  partner.getDefaultCity(), movieid);
			if(dateList != null && dateList.size() > 0){
				fyrq = dateList.get(0);
			}
		}
		addOpiListData(partner, movieid, fyrq, null, null, model);
		model.put("dateList",BeanUtil.getSubList((List<Date>)model.get("dateList"), 0, 4));
		int maxnum = 8;
		List<Movie> movies = (List<Movie>)model.get("movieList");
		if(movies != null){
			List<Movie> subMovies = BeanUtil.getSubList(movies, pageNo * maxnum, maxnum);
			model.put("movieList", subMovies);
			if(subMovies.size() >= maxnum){
				model.put("nextPage", true);
			}
			if(pageNo > 0){
				model.put("upPage", true);
			}
			if(movieid == null){
				if(subMovies.size()>0) model.put("movie",subMovies.get(0));
			}else{
				model.put("movie", daoService.getObject(Movie.class, movieid));
			}
		}
		model.put("fyrq", fyrq);
		model.put("nextPageNo", pageNo + 1);
		model.put("prePageNo", pageNo - 1);
		model.put("currentPage", pageNo);
		model.put("width",width);
		return "partner/juxiang/step1" + getResolution(width) + ".vm";
	}
	
	@RequestMapping("/partner/juxiang/chooseArea.xhtml")
	public String chooseArea(long movieid,Date fyrq,String width,ModelMap model){
		Movie movie = daoService.getObject(Movie.class, movieid);
		ApiUser partner = getJuXiang();
		List<OpenPlayItem> opiList = partnerService.getPartnerOpiList(partner,  partner.getDefaultCity(), null, movieid, fyrq);
		Map<Long/*cinemaid*/, List<OpenPlayItem>> opiMap = BeanUtil.groupBeanList(opiList, "cinemaid");
		List<Cinema> cinemaList = daoService.getObjectList(Cinema.class, opiMap.keySet());
		Map<String/*countycode*/, List<Cinema>> cinemaMap = BeanUtil.groupBeanList(cinemaList, "countycode");
		List<County> countyList = daoService.getObjectList(County.class, cinemaMap.keySet());
		model.put("countyList", countyList);
		model.put("movie", movie);
		model.put("fyrq", fyrq);
		model.put("width",width);
		return "partner/juxiang/chooseArea" + getResolution(width) + ".vm";
	}
	
	@RequestMapping("/partner/juxiang/chooseCinema.xhtml")
	public String chooseCinema(long movieid,Date fyrq,int pageNo,Long cinemaId,String countyCode,
			String width,ModelMap model){
		Movie movie = daoService.getObject(Movie.class, movieid);
		ApiUser partner = getJuXiang();
		List<OpenPlayItem> opiList = partnerService.getPartnerOpiList(partner,  partner.getDefaultCity(), null, movieid, fyrq);
		Map<Long/*cinemaid*/, List<OpenPlayItem>> opiMap = BeanUtil.groupBeanList(opiList, "cinemaid");
		List<Cinema> cinemaList = daoService.getObjectList(Cinema.class, opiMap.keySet());
		Map<String/*countycode*/, List<Cinema>> cinemaMap = BeanUtil.groupBeanList(cinemaList, "countycode");
		model.put("movie", movie);
		model.put("fyrq", fyrq);
		cinemaList = new LinkedList<Cinema>();
		cinemaList.addAll(cinemaMap.remove(countyCode));
		Set<String> countycodes = cinemaMap.keySet();
		Map<String,County> countys = new HashMap<String,County>();
		for(String countycode : countycodes){
			cinemaList.addAll(cinemaMap.get(countycode));
			countys.put(countycode, daoService.getObject(County.class, countycode));
		}
		countys.put(countyCode,daoService.getObject(County.class, countyCode));
		int maxnum = 20;
		List<Cinema> subCinemas = BeanUtil.getSubList(cinemaList, pageNo * maxnum, maxnum);
		if(subCinemas.size() >= maxnum){
			model.put("nextPage", true);
		}
		if(pageNo > 0){
			model.put("upPage", true);
		}
		if(cinemaId == null && subCinemas != null && subCinemas.size() > 0){
			model.put("cinema",subCinemas.get(0));
		}else{
			model.put("cinema",daoService.getObject(Cinema.class, cinemaId));
		}
		model.put("cinemaList", subCinemas);
		model.put("county", daoService.getObject(County.class, countyCode));
		model.put("countys", countys);
		model.put("cinemaMap", cinemaMap);
		model.put("nextPageNo", pageNo + 1);
		model.put("prePageNo", pageNo - 1);
		model.put("currentPage", pageNo);
		model.put("width",width);
		return "partner/juxiang/chooseCinema" + getResolution(width) + ".vm";
	}
	
	@RequestMapping("/partner/juxiang/chooseOpi.xhtml")
	public String chooseOpi(long movieId,int pageNo,long cinemaId,Date fyrq,String width,ModelMap model){
		ApiUser partner = getJuXiang();
		List<OpenPlayItem> opiList = partnerService.getPartnerOpiList(partner,  partner.getDefaultCity(), cinemaId, movieId, fyrq);
		Movie movie = daoService.getObject(Movie.class, movieId);
		model.put("movie", movie);
		Cinema cinema = daoService.getObject(Cinema.class, cinemaId);
		model.put("cinema", cinema);
		model.put("county", daoService.getObject(County.class, cinema.getCountycode()));
		PartnerPriceHelper priceHelper = new PartnerPriceHelper();
		model.put("priceHelper", priceHelper);
		int maxnum = 5;
		List<OpenPlayItem> subOpiList = BeanUtil.getSubList(opiList, pageNo * maxnum, maxnum);
		if(subOpiList.size() >= maxnum){
			model.put("nextPage", true);
		}
		if(pageNo > 0){
			model.put("upPage", true);
		}
		model.put("opiList", subOpiList);
		model.put("fyrq", fyrq);
		model.put("nextPageNo", pageNo + 1);
		model.put("prePageNo", pageNo - 1);
		model.put("width",width);
		return "partner/juxiang/chooseOpi" + getResolution(width) + ".vm";
	}
	
	//第二步：选择座位
	@RequestMapping("/partner/juxiang/chooseSeat.xhtml")
	public String chooseSeat(@CookieValue(value="ukey", required=false) String ukey,String width,
			long mpid, ModelMap model, HttpServletRequest request, HttpServletResponse response){
		if(StringUtils.isBlank(ukey)) {
			PartnerUtil.setUkCookie(response, config.getBasePath() + "partner/juxiang/");
		}
		ApiUser partner = getJuXiang();
		getCitycodeByPartner(partner, request, response);
		model.put("width",width);
		String view = "partner/juxiang/chooseSeat" + getResolution(width) + ".vm";
		String result = chooseSeat(partner, mpid, ukey, "partner/juxiang/chooseSeat" + getResolution(width) + ".vm", model);
		if(!StringUtils.equals(result, view)){
			return "redirect:/partner/juxiang/chooseSeatError.xhtml";
		}
		return result;
	}
	
	@RequestMapping("/partner/juxiang/seatPage.xhtml")
	public String seatPage(Long mpid,  Integer price, @CookieValue(value="ukey", required=false) String ukey, ModelMap model){
		ApiUser partner = getJuXiang();
		model.put("mpid", mpid);
		model.put("partnerid", partner.getId());
		model.put("price", price);
		model.put("ukey", ukey);
		ErrorCode<String> code = addSeatData(mpid, partner.getId(), ukey, model);
		if(!code.isSuccess()) {
			return showJsonError(model, code.getMsg());
		}
		return "partner/juxiang/new_seatPage.vm";
	}
	
	//第三步:锁座位，加订单
	@RequestMapping("/partner/juxiang/addOrder.xhtml")
	public String addOrder(@CookieValue(required=false,value="ukey") String ukey,
			/*String captchaId, String captcha,*/ Long mpid,  
			@RequestParam("mobile") String mobile, @RequestParam("seatid")String seatid, HttpServletRequest request, ModelMap model){
		if(mpid==null) return showJsonError(model, "缺少参数！");
		//boolean validCaptcha = controllerService.validateCaptcha(captchaId, captcha);
		//if(!validCaptcha) return showJsonError(model, "验证码错误！");
		//if(StringUtils.isBlank(userid)) return showJsonError(model, "获取用户标识错误,请重新登录！");
		ApiUser partner = getJuXiang();
		ErrorCode code = addOrder(mpid, mobile, seatid, ukey, partner, mobile, null, null, PaymethodConstant.PAYMETHOD_PARTNERPAY, WebUtils.getRemoteIp(request), model);
		if(code.isSuccess()) {
			return showJsonSuccess(model, model.get("orderId")+"");
		}
		return showJsonError(model, code.getMsg());
	}
	
	//第四步：确认订单去支付（重新查看）
	@RequestMapping("/partner/juxiang/showOrder.xhtml")
	public String showOrder(Long orderId, ModelMap model, String width,@CookieValue(value="ukey", required=false)String ukey){
		ApiUser partner = getJuXiang();
		PartnerPriceHelper priceHelper = new PartnerPriceHelper();
		model.put("priceHelper", priceHelper);
		String result = showOrder(ukey, orderId, partner, "partner/juxiang/confirmOrder" + getResolution(width) + ".vm", model);
		TicketOrder order = (TicketOrder)model.get("order");
		model.put("seatDescription", StringUtils.split(VmUtils.readJsonToMap(order.getDescription2()).get("影票"), ","));
		model.put("width",width);
		return result ;
	}
	
	//第四步：确认订单去支付
	@RequestMapping(method=RequestMethod.POST,value="/partner/juxiang/saveOrder.xhtml")
	public String saveOrder(ModelMap model, String mobile,HttpServletRequest request,
			@CookieValue(required=false,value="ukey") String ukey, String paybank,
			@RequestParam("orderId")long orderId){
		if(StringUtils.isBlank(ukey)) {
			return forwardMessage(model, "缺少参数！");
		}
		ErrorCode<TicketOrder> code = saveOrder(orderId, mobile, PaymethodConstant.PAYMETHOD_PARTNERPAY, paybank, ukey);
		if(!code.isSuccess()) {
			return showJsonError(model, code.getMsg());
		}
		Map<String, String> params = paymentService.getNetPayParams(code.getRetval(), WebUtils.getRemoteIp(request), null);
		return showJsonSuccess(model,params);
	}
		
}
