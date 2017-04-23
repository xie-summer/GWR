package com.gewara.web.action.admin.report;
import java.sql.Timestamp;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import com.gewara.constant.AdminCityContant;
import com.gewara.constant.PaymethodConstant;
import com.gewara.constant.ticket.OrderConstant;
import com.gewara.model.movie.Cinema;
import com.gewara.model.movie.Movie;
import com.gewara.model.pay.TicketOrder;
import com.gewara.untrans.ticket.SpecialDiscountService;
import com.gewara.util.BeanUtil;
import com.gewara.web.action.admin.BaseAdminController;
@Controller
public class PartnerCountController extends BaseAdminController{
	@Autowired@Qualifier("specialDiscountService")
	private SpecialDiscountService specialDiscountService;
	@RequestMapping("/admin/datacount/specialDiscountOrderList.xhtml")
	public String specialDiscountOrderList(Timestamp starttime, Timestamp endtime, Long sid, String citycode, String status, ModelMap model){
		String url = "admin/datacount/specialDiscountOrderList.vm";
		Map<String, String> cityMap = new TreeMap<String, String>(AdminCityContant.getCitycode2CitynameMap());
		model.put("cityMap", cityMap);
		if(starttime==null || endtime==null)  return url;
		List<TicketOrder> tickorderList = specialDiscountService.specialDiscountOrderList(starttime, endtime, sid, citycode, status);
		if (CollectionUtils.isNotEmpty(tickorderList)) {
			model.put("qryOrderList", tickorderList);
			List<Long> movieIdList = BeanUtil.getBeanPropertyList(tickorderList, "movieid", true);
			List<Long> cinemaIdList = BeanUtil.getBeanPropertyList(tickorderList, "cinemaid", true);
			List<Movie> movieList = new LinkedList<Movie>();
			List<Cinema> cinemaList = new LinkedList<Cinema>();
			for (Long movieid : movieIdList) {
				movieList.add(daoService.getObjectByUkey(Movie.class, "id", movieid, true));
			}
			for (Long cinemaid : cinemaIdList) {
				cinemaList.add(daoService.getObjectByUkey(Cinema.class, "id", cinemaid, true));
			}
			Map<Long, Movie> movieMap = BeanUtil.beanListToMap(movieList, "id");
			Map<Long, Cinema> cinemaMap = BeanUtil.beanListToMap(cinemaList, "id");
			model.put("movieMap", movieMap);
			model.put("cinemaMap", cinemaMap);
		}
		model.put("starttime", starttime);
		model.put("endtime", endtime);
		model.put("orderStatusMap", OrderConstant.statusMap);
		model.put("paytextMap", PaymethodConstant.getPayTextMap());
		return url;
	}
}
