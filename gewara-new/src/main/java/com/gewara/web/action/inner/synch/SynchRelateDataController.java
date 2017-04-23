package com.gewara.web.action.inner.synch;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.ProjectionList;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.gewara.constant.ApiConstant;
import com.gewara.helper.sys.RelateClassHelper;
import com.gewara.model.movie.Cinema;
import com.gewara.model.movie.CinemaProfile;
import com.gewara.model.movie.CinemaRoom;
import com.gewara.model.movie.Movie;
import com.gewara.model.movie.RoomSeat;
import com.gewara.model.pay.PayMethod;
import com.gewara.service.movie.MCPService;
import com.gewara.util.BeanUtil;
import com.gewara.util.JsonUtils;
import com.gewara.util.WebUtils;
import com.gewara.web.action.api.BaseApiController;
/**
 * 测试JSON数据组织方式
 * @author gebiao(ge.biao@gewara.com)
 * @since Jul 25, 2012 8:28:16 PM
 */
@Controller
public class SynchRelateDataController extends BaseApiController {
	@Autowired
	private MCPService mcpService;
	//支持的类型
	private List<String> supportTagList = Arrays.asList("cinema", "movie", "gym", "gymcoach", "gymcourse", "sport", "sportitem", "drama", "theatre", "memberInfo", "film");
	//每种类型支持的字段
	private Map<String, List<String>> supportFieldsMap = new HashMap<String, List<String>>();
	//private Map<String, List<String>> disabledFieldsMap = new HashMap<String, List<String>>();
	@PostConstruct
	public void init(){
		supportFieldsMap.put("cinema", Arrays.asList("id", "name", "citycode","booking", "otherinfo")); //baobiao_cinema: otherinfo取出jointCinema
		supportFieldsMap.put("sport", Arrays.asList("id", "name", "citycode"));// baobiao_sport: sport_profile取出COMPANY
		
		supportFieldsMap.put("gym", Arrays.asList("id", "name", "citycode")); //暂无
		supportFieldsMap.put("gymcoach", Arrays.asList("id", "name")); //暂无
		supportFieldsMap.put("gymcourse", Arrays.asList("id", "name")); //暂无
		
		supportFieldsMap.put("theatre", Arrays.asList("id", "name", "citycode"));
		supportFieldsMap.put("movie", Arrays.asList("id", "moviename", "releasedate", "state", "flag")); //报表未同步
		supportFieldsMap.put("drama", Arrays.asList("id", "dramaname")); //报表未同步
		supportFieldsMap.put("film", Arrays.asList("id","hcid","filmid","name","movieid","plang","language","edition","addtime","updatetime","flag"));
		//supportFieldsMap.put("sportitem", commonFields);
		supportFieldsMap.put("memberInfo", Arrays.asList("id", "nickname", "addtime", "fromcity")); //报表未同步
		//disabledFieldsMap.put("memberInfo", Arrays.asList("mobile", "password"));
	}
	private Collection<String> getFieldList(String tag, String fields){
		List<String> allFields = supportFieldsMap.get(tag);
		if(StringUtils.isBlank(fields)) return allFields;
		List<String> myFields = Arrays.asList(StringUtils.split(fields, ","));
		Collection<String> result = CollectionUtils.intersection(allFields, myFields);
		return result;
	}
	@RequestMapping("/inner/synch/getRelateDataList.xhtml")
	public String getRelateDataList(ModelMap model, String tag, String fields, int from, int maxnum, Timestamp updatetime){
		if(!supportTagList.contains(tag) || updatetime==null) {
			return getErrorXmlView(model, ApiConstant.CODE_PARAM_ERROR, "参数错误");
		}
		Class clazz = RelateClassHelper.getRelateClazz(tag);
		DetachedCriteria query = DetachedCriteria.forClass(clazz);
		query.add(Restrictions.ge("updatetime", updatetime));
		Collection<String> fieldList = getFieldList(tag, fields);
		ProjectionList properties = Projections.projectionList();
		for(String field: fieldList){
			properties.add(Projections.property(field).as(field));
		}
		query.setProjection(properties);
		query.setResultTransformer(DetachedCriteria.ALIAS_TO_ENTITY_MAP);
		List<Map> resultList = hibernateTemplate.findByCriteria(query, from, Math.min(maxnum, 1000));
		model.put("tag", tag);
		model.put("fieldList", fieldList);
		model.put("resultList", resultList);
		return getXmlView(model, "inner/synch/relateDataList.vm");
	}
	@RequestMapping("/inner/synch/getCurMovieList.xhtml")
	public String getCurMovieList(ModelMap model, String fields){
		List<String> fieldList = new ArrayList<String>(getFieldList("movie", fields));
		List<Movie> movieList = mcpService.getCurMovieList();
		List<Map> resultList = BeanUtil.getBeanMapList(movieList, fieldList.toArray(new String[]{}));
		model.put("tag", "movie");
		model.put("fieldList", fieldList);
		model.put("resultList", resultList);
		return getXmlView(model, "inner/synch/relateDataList.vm");
	}

	@RequestMapping("/inner/synch/getCinemaRoom.xhtml")
	public String getCinemaRoom(Long cinemaid, ModelMap model){
		if(cinemaid == null) return getErrorXmlView(model, ApiConstant.CODE_SIGN_ERROR, "影院ID不能为空！");
		List<CinemaRoom> roomList = daoService.getObjectListByField(CinemaRoom.class, "cinemaid", cinemaid);
		if(roomList.isEmpty()) return getErrorXmlView(model, ApiConstant.CODE_SIGN_ERROR, "没有查询到数据！");
		model.put("roomList", roomList);
		return getXmlView(model, "inner/synch/cinemaRoom.vm");
	}
	
	@RequestMapping("/inner/synch/getRoomSeat.xhtml")
	public String getRoomSeat(Long cinemaid, String roomnum, ModelMap model){
		if(cinemaid == null || StringUtils.isBlank(roomnum)) return getErrorXmlView(model, ApiConstant.CODE_SIGN_ERROR, "影院ID或影厅序号不能为空！");
		CinemaRoom room = mcpService.getRoomByRoomnum(cinemaid, roomnum);
		if(room == null) return getErrorXmlView(model, ApiConstant.CODE_SIGN_ERROR, "影厅不存在！");
		List<RoomSeat> roomSeatList = daoService.getObjectListByField(RoomSeat.class, "roomid", room.getId());
		if(roomSeatList.isEmpty()) return getErrorXmlView(model, ApiConstant.CODE_SIGN_ERROR, "没有查询到数据！");
		model.put("roomSeatList", roomSeatList);
		return getXmlView(model, "inner/synch/roomSeat.vm");
	}
	
	@RequestMapping("/inner/synch/getCinemaRelateDataList.xhtml")
	public String getRelateDataList(ModelMap model, int from, int maxnum, Timestamp updatetime){
		DetachedCriteria query = DetachedCriteria.forClass(Cinema.class);
		query.add(Restrictions.ge("updatetime", updatetime));
		List<Cinema> cinemaList = hibernateTemplate.findByCriteria(query, from, Math.min(maxnum, 1000));
		List<Map> resultList = new ArrayList<Map>();
		for(Cinema cinema : cinemaList){
			Map map = new HashMap();
			map.put("id", cinema.getId());
			map.put("name", cinema.getName());
			map.put("booking", cinema.getBooking());
			map.put("citycode", cinema.getCitycode());
			CinemaProfile profile = daoService.getObject(CinemaProfile.class, cinema.getId());
			if(profile!=null){
				map.put("opentype", profile.getOpentype());
				map.put("direct", profile.getDirect());
			}
			resultList.add(map);
		}
		model.put("tag", "cinema");
		model.put("resultList", resultList);
		model.put("fieldList", Arrays.asList("id", "name", "booking", "citycode", "opentype", "direct"));
		return getXmlView(model, "inner/synch/relateDataList.vm");		
	}
	@RequestMapping("/inner2/paymethod/queryAllPaymethod.xhtml")
	@ResponseBody
	public String queryPayTextMap(HttpServletRequest request){
		checkIp(WebUtils.getRemoteIp(request));
		List<PayMethod> payMethods = daoService.getAllObjects(PayMethod.class);
		Map<String, String> payTextMap = new HashMap<String, String>();
		for (PayMethod p : payMethods)
			payTextMap.put(p.getPayMethod(), p.getPayMethodText());
		
		return JsonUtils.writeMapToJson(payTextMap);
	}
	private String[] ips = new String[]{"114.80.171.2", "180.153.146.1", "172.22.1.", "127.0.0.1", "192.168."};
	public void checkIp(String ip){
		if(WebUtils.isLocalIp(ip)) return;
		for(String pre:ips){
			if(StringUtils.startsWith(ip, pre)) return;
		}
		throw new  IllegalArgumentException("IP禁止访问");
	}
}
