package com.gewara.web.action.api;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import com.gewara.constant.ApiConstant;
import com.gewara.model.movie.Cinema;
import com.gewara.service.movie.MCPService;

@Controller
public class ApiCinemaController extends BaseApiController{
	@Autowired@Qualifier("mcpService")
	private MCPService mcpService;
	public void setMcpService(MCPService mcpService) {
		this.mcpService = mcpService;
	}
	//TODO:upgrade "sdoshopping"
	@RequestMapping("/api/cinema/cinema.xhtml")
	public String cinema(String key, Long cinemaid, String encryptCode, ModelMap model){
		ApiAuth auth = checkRights(encryptCode, key);
		if(!auth.isChecked()) return getErrorXmlView(model, auth.getCode(), auth.getMsg());
		if(cinemaid==null) return getErrorXmlView(model, ApiConstant.CODE_PARAM_ERROR, "传递参数错误！");
		Cinema cinema = daoService.getObject(Cinema.class, cinemaid);
		if(cinema == null) return getErrorXmlView(model, ApiConstant.CODE_DATA_ERROR, "没有查询信息！");
		model.put("cinema", cinema);
		model.put("indexarea", placeService.getIndexareaname(cinema.getIndexareacode()));
		model.put("generalmark", getPlaceGeneralmark(cinema));
		return getXmlView(model, "api/info/cinema/cinema.vm");
	}
	//TODO:upgrade "sdoshopping"
	@RequestMapping("/api/cinema/cinemaList.xhtml")
	public String cinemaList(String key, String countycode, String indexareacode, Long movieid, String encryptCode, ModelMap model){
		ApiAuth auth = checkRights(encryptCode, key);
		if(!auth.isChecked()) return getErrorXmlView(model, auth.getCode(), auth.getMsg());

		List<Cinema> cinemaList = new ArrayList<Cinema>();
		if(StringUtils.isBlank(indexareacode) && StringUtils.isBlank(countycode)){
			return getErrorXmlView(model, ApiConstant.CODE_PARAM_ERROR, "传递参数错误！");
		}
		cinemaList = mcpService.getCinemaListByIndexareaCodeCountycodeMovie(countycode, indexareacode, movieid);
		model.put("cinemaList", cinemaList);
		model.put("generalmarkMap", getGeneralmarkMap(new HashSet(cinemaList)));
		return getXmlView(model, "api/info/cinema/cinemaList.vm");
	}
}
