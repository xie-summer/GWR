package com.gewara.web.action.ajax;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.RequestMapping;

import com.gewara.model.drama.Drama;
import com.gewara.model.drama.Theatre;
import com.gewara.model.movie.Cinema;
import com.gewara.model.movie.Movie;
import com.gewara.model.sport.Sport;
import com.gewara.model.sport.SportItem;
import com.gewara.model.user.Member;
import com.gewara.service.drama.DramaService;
import com.gewara.service.drama.TheatreService;
import com.gewara.service.movie.MCPService;
import com.gewara.service.order.OrderQueryService;
import com.gewara.service.sport.SportService;
import com.gewara.support.ErrorCode;
import com.gewara.untrans.activity.SynchActivityService;
import com.gewara.untrans.gym.SynchGymService;
import com.gewara.util.BeanUtil;
import com.gewara.util.WebUtils;
import com.gewara.web.action.AnnotationController;
import com.gewara.xmlbind.activity.RemoteActivity;
import com.gewara.xmlbind.gym.RemoteCourse;
import com.gewara.xmlbind.gym.RemoteGym;


/**
 *  @function Ajax获取关联信息(论坛/活动/... 弹出框)
 * 	@author bob.hu
 *	@date	2011-07-07 19:37:57
 */
@Controller
public class ConstRelationAjaxController extends AnnotationController {
	@Autowired@Qualifier("sportService")
	private SportService sportService;
	public void setSportService(SportService sportService) {
		this.sportService = sportService;
	}

	@Autowired@Qualifier("dramaService")
	private DramaService dramaService;
	public void setDramaService(DramaService dramaService) {
		this.dramaService = dramaService;
	}
	@Autowired@Qualifier("theatreService")
	private TheatreService theatreService;
	public void setTheatreService(TheatreService theatreService) {
		this.theatreService = theatreService;
	}
	@Autowired@Qualifier("mcpService")
	private MCPService mcpService;
	public void setMcpService(MCPService mcpService) {
		this.mcpService = mcpService;
	}
	@Autowired@Qualifier("orderQueryService")
	private OrderQueryService orderQueryService;
	public void setOrderQueryService(OrderQueryService orderQueryService) {
		this.orderQueryService = orderQueryService;
	}
	@Autowired@Qualifier("synchActivityService")
	private SynchActivityService synchActivityService;
	
	@Autowired@Qualifier("synchGymService")
	private SynchGymService synchGymService;
	/***
	 *  Ajax 获取用户喜欢的电影+影院
	 *  新增一种展示方式，传isnewmode，由李峰整理；活动使用
	 * */
	@RequestMapping("/ajaxLoadUserFav.xhtml")
	public String ajaxLoadUserFav(@CookieValue(value=LOGIN_COOKIE_NAME, required=false)String sessid,
			HttpServletRequest request, String tag, String citycode,String isnewmode, ModelMap model){
		Member member = loginService.getLogonMemberBySessid(WebUtils.getRemoteIp(request), sessid);
		if(member != null){
			Long memberid = member.getId();
			if(StringUtils.equals(tag, "cinema")){	// 电影版块
				List<Movie> movieList = orderQueryService.getMemberOrderMovieList(memberid, 4);
				List<Map> movieMapList = BeanUtil.getBeanMapList(movieList, "id", "moviename");
				model.put("movieMap", movieMapList);
				List<Cinema> cinemaList = orderQueryService.getMemberOrderCinemaList(memberid, 4);
				List<Map> cinemaMapList = BeanUtil.getBeanMapList(cinemaList, "id", "name","address");
				model.put("cinemaMap", cinemaMapList);
				
				List<Movie> hotmovieList = mcpService.getCurMovieListByMpiCount(citycode, 0, 8);
				List<Map> hotmovieMapList = BeanUtil.getBeanMapList(hotmovieList, "id", "moviename");
				model.put("hotmovieMap", hotmovieMapList);
				List<Cinema> hotcinemaList = mcpService.getHotBookingCinames(citycode, 0, 8);
				List<Map> hotcinemaMapList = BeanUtil.getBeanMapList(hotcinemaList, "id", "name","address");
				model.put("hotcinemaMap", hotcinemaMapList);
			}else if(StringUtils.equals(tag, "gym")){ // 健身版块
				ErrorCode<List<RemoteCourse>> courseCode = synchGymService.getHotCourseList(0, 8);
				if(courseCode.isSuccess()){
					List<RemoteCourse> courseList = courseCode.getRetval();
					List<Map> hotcourseMapList = BeanUtil.getBeanMapList(courseList, "id", "coursename");
					model.put("hotcourseMap", hotcourseMapList);
				}
				ErrorCode<List<RemoteGym>> gymCode = synchGymService.getGymList(citycode, null, null, null, false, 0, 8);
				if(gymCode.isSuccess()){
					List<RemoteGym> hotgymList = gymCode.getRetval();
					List<Map> hotgymMapList = BeanUtil.getBeanMapList(hotgymList, "id", "name","address");
					model.put("hotgymMap", hotgymMapList);
				}
			}else if(StringUtils.equals(tag, "sport")){ // 运动版块
				List<SportItem> sportItemList = sportService.getTopSportItemList();
				List<Map> sportItemMapList = BeanUtil.getBeanMapList(sportItemList, "id", "itemname");
				model.put("hotsportItemMap", sportItemMapList);
				
				List<Sport> hotsportList = sportService.getHotSports(citycode, null, true, 8);
				List<Map> hotsportMapList = BeanUtil.getBeanMapList(hotsportList, "id", "name","address");
				model.put("hotsportMap", hotsportMapList);
			}else if(StringUtils.equals(tag, "theatre")){ // 话剧版块
				List<Drama> hotdramaList = dramaService.getHotDrama(citycode, "clickedtimes", 0, 8);
				List<Map> hotdramaMapList = BeanUtil.getBeanMapList(hotdramaList, "id", "dramaname");
				model.put("hotdramaMap", hotdramaMapList);
				
				List<Theatre> theatreList = theatreService.getTheatreListByHotvalue(citycode, null, 0, 8);
				List<Map> hottheatreList = BeanUtil.getBeanMapList(theatreList, "id", "name","address");
				model.put("hottheatreMap", hottheatreList);
			}else if(StringUtils.equals(tag, "activity")){ // 活动版块
				List<RemoteActivity> listActivity = new ArrayList<RemoteActivity>();
				ErrorCode<List<RemoteActivity>> code = synchActivityService.getRemoteActivityByMemberid(memberid, null, 0, 8);
				if(code.isSuccess()) listActivity = code.getRetval();
				List<Map> activityMapList = BeanUtil.getBeanMapList(listActivity, "id", "title");
				model.put("activityMap", activityMapList);
			}
		}
		model.put("tag", tag);
		model.put("isnewmode", isnewmode);
		return "common/const_relation_data.vm";
	}
}
