package com.gewara.web.action.admin.movie;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import com.gewara.constant.Flag;
import com.gewara.model.common.County;
import com.gewara.model.common.Indexarea;
import com.gewara.model.common.Subwaystation;
import com.gewara.model.movie.Cinema;
import com.gewara.service.movie.MCPService;
import com.gewara.untrans.SearchService;
import com.gewara.untrans.impl.ControllerService;
import com.gewara.util.BindUtils;
import com.gewara.util.ChangeEntry;
import com.gewara.util.JsonUtils;
import com.gewara.util.PinYinUtils;
import com.gewara.util.ValidateUtil;
import com.gewara.web.action.admin.BaseAdminController;

@Controller
public class AdminCinemaAjaxController extends BaseAdminController{
	@Autowired@Qualifier("mcpService")
	private MCPService mcpService;
	public void setMcpService(MCPService mcpService) {
		this.mcpService = mcpService;
	}

	@Autowired@Qualifier("searchService")
	private SearchService searchService;
	@Autowired@Qualifier("controllerService")
	private ControllerService controllerService;
	
	@RequestMapping("/admin/cinema/ajax/setCinemaHotValue.xhtml")
	public String setCinemaHotValue(Long cinemaId, Integer value, ModelMap model) {
		mcpService.updateCinemaHotValue(cinemaId, value);
		return showJsonSuccess(model);
	}
	private void setCinemaTelephones(String areaCode,String phone,String phoneRemark,List<Map<String,String>> phoneList){
		Map<String,String> phones = new HashMap<String,String>();
		if(StringUtils.isNotBlank(areaCode)){
			phones.put("areaCode", areaCode);
		}
		if(StringUtils.isNotBlank(phone)){
			phones.put("phone", phone);
		}
		if(StringUtils.isNotBlank(phoneRemark)){
			phones.put("phoneRemark", phoneRemark);
		}
		if(!phones.isEmpty()){
			phoneList.add(phones);
		}
	}
	
	private void setStationExitnumber(String subwayLinesName,String subwayExitnumberName,String subwayDetailName,
			List<Map<String,String>> subWayLineList,HttpServletRequest request){
		Map<String,String> lineMap = new HashMap<String,String>();
		String subwayLines = request.getParameter(subwayLinesName);
		String subwayExitnumber = request.getParameter(subwayExitnumberName);
		String subwayDetail = request.getParameter(subwayDetailName);
		if(StringUtils.isNotBlank(subwayLines)){
			lineMap.put("lines", subwayLines);
		}
		if(StringUtils.isNotBlank(subwayExitnumber)){
			lineMap.put("exitnumber", subwayExitnumber);
		}
		if(StringUtils.isNotBlank(subwayDetail)){
			lineMap.put("detail", subwayDetail);
		}
		if(!lineMap.isEmpty()){
			subWayLineList.add(lineMap);
		}
	}
	/**
	 * 组织地铁站下面对应设置的出口线路数据
	 * @param stationid 地铁站id
	 * @param exitnumLs 对应站点下面的出口数 [1,2,3] 表示 第一个地铁站下设置了1个出口，第二个下设置了2个出口，第三站设置了3个出口
	 * @param index
	 * @param subWayLineList
	 * @param subwayTransport
	 * @param request
	 */
	private void setSubwayTransport(Long stationid,String[] exitnumLs,int index,List<Map<String,String>> subWayLineList,
			Map<Long,List<Map<String,String>>> subwayTransport,HttpServletRequest request){
		int lineNum =  index + 1;
		if(exitnumLs.length > index){
			int exitnumLength = Integer.parseInt(exitnumLs[index]);
			if(exitnumLength == 1){
				setStationExitnumber("subwayLines" + lineNum, "subwayExitnumber" + lineNum,"subwayDetail" + lineNum, 
						subWayLineList, request);
			}else{
				for(int i = 0;i < exitnumLength;i++){
					setStationExitnumber("subwayLines" + lineNum + "[" + i + "]", "subwayExitnumber" + lineNum + "[" + i + "]", 
							"subwayDetail" + lineNum + "[" + i + "]", subWayLineList, request);
				}
			}
		}
		subwayTransport.put(stationid, subWayLineList);
	}
	//保存影院LOGO信息
	@RequestMapping("/admin/cinema/ajax/saveCinemaLogo.xhtml")
	public String saveCinemaLogo(Long cinemaId,String logo,ModelMap model){
		if(cinemaId==null) {
			return this.showJsonError(model, "影院ID不能为空");
		}
		Cinema  cinema = daoService.getObject(Cinema.class, new Long(cinemaId));
		if(cinema == null){
			return this.showJsonError(model, "影院为空");
		}
		ChangeEntry changeEntry = new ChangeEntry(cinema);
		cinema.setUpdatetime(new Timestamp(System.currentTimeMillis()));
		cinema.setLogo(logo);
		daoService.saveObject(cinema);
		monitorService.saveChangeLog(getLogonUser().getId(), Cinema.class, cinema.getId(),changeEntry.getChangeMap(cinema));
		return this.showJsonSuccess(model,cinema.getId()+"");
	}
	//保存影院基础信息
	@RequestMapping("/admin/cinema/ajax/saveAroundCinema.xhtml")
	public String saveAroundCinema(Long cinemaId, Integer transportLength,Integer subwaystationLength,String exitnumberLength, 
			HttpServletRequest request, ModelMap model){
		if(cinemaId==null) {
			return this.showJsonError(model, "影院ID不能为空");
		}
		Cinema  cinema = daoService.getObject(Cinema.class, new Long(cinemaId));
		if(cinema == null){
			return this.showJsonError(model, "影院为空");
		}
		ChangeEntry changeEntry = new ChangeEntry(cinema);
		cinema.setUpdatetime(new Timestamp(System.currentTimeMillis()));
		List<Long> stationIdList = new ArrayList<Long>();
		if(subwaystationLength != null){
			String[] exitnumLs = StringUtils.split(exitnumberLength, ",");
			Map<Long,List<Map<String,String>>> subwayTransport = new HashMap<Long,List<Map<String,String>>>();
			if(subwaystationLength == 1){
				String stationid = request.getParameter("stationid");
				List<Map<String,String>> subWayLineList = new LinkedList<Map<String,String>>();
				if(StringUtils.isNotBlank(stationid)){
					Long stationId = Long.parseLong(stationid);
					stationIdList.add(stationId);
					setSubwayTransport(stationId,exitnumLs,0, subWayLineList,subwayTransport, request);
				}
			}else{
				for(int index = 0;index < subwaystationLength;index++){
					List<Map<String,String>> subWayLineList = new LinkedList<Map<String,String>>();
					String stationid = request.getParameter("stationid[" + index + "]");
					if(StringUtils.isNotBlank(stationid)){
						Long stationId = Long.parseLong(stationid);
						stationIdList.add(stationId);
						setSubwayTransport(stationId,exitnumLs,index, subWayLineList,subwayTransport, request);
					}
				}
			}
			if(subwayTransport.isEmpty()){
				cinema.setSubwayTransport(null);
			}else{
				cinema.setSubwayTransport(JsonUtils.writeObjectToJson(subwayTransport));
			}
		}
		
		if(stationIdList.isEmpty()){
			cinema.setStationid(null);
			cinema.setStationname(null);
		}else{
			Subwaystation subwaystation = daoService.getObject(Subwaystation.class, stationIdList.get(0));
			cinema.setStationid(stationIdList.get(0));
			cinema.setStationname(subwaystation.getStationname());
			String qry = "select s.line.id from Line2Station s where s.station.id in (" + StringUtils.join(stationIdList, ",")+ ")";
			cinema.setLineidlist(StringUtils.join(hibernateTemplate.find(qry), ","));
		}
		if(transportLength != null){
			String cinemaTransport = "";
			if(transportLength == 1){
				String transportLine = request.getParameter("transportLine");
				String transportSta = request.getParameter("transportSta");
				String transport = request.getParameter("transport");
				if(StringUtils.isNotBlank(transportLine)){
					 cinemaTransport += transportLine + "路";
				}
				if(StringUtils.isNotBlank(transportSta)){
					cinemaTransport += transportSta + "站下车";
				}
				if(StringUtils.isNotBlank(transport)){
					cinemaTransport += transport;
				}
			}else{
				for(int index = 0;index < transportLength;index++){
					String transportLine = request.getParameter("transportLine[" + index + "]");
					String transportSta = request.getParameter("transportSta[" + index + "]");
					String transport = request.getParameter("transport[" + index + "]");
					if(StringUtils.isNotBlank(cinemaTransport) && (StringUtils.isNotBlank(transportLine) || StringUtils.isNotBlank(transportSta) || 
							StringUtils.isNotBlank(transport))){
						cinemaTransport += "@";
					}
					if(StringUtils.isNotBlank(transportLine)){
						 cinemaTransport += transportLine + "路";
					}
					if(StringUtils.isNotBlank(transportSta)){
						cinemaTransport += transportSta + "站下车";
					}
					if(StringUtils.isNotBlank(transport)){
						cinemaTransport += transport;
					}
				}
			}
			if(StringUtils.isNotBlank(cinemaTransport)){
				cinema.setTransport(cinemaTransport);
			}else{
				cinema.setTransport(null);
			}
		}
		String[] otheritemList = new String[]{
				Flag.SERVICE_WEBCOMMENT, Flag.SERVICE_PAIRSEAT, Flag.SERVICE_PARK, Flag.SERVICE_VISACARD, Flag.SERVICE_PLAYGROUND, 
				Flag.SERVICE_3D, Flag.SERVICE_SALE, Flag.SERVICE_FOOD, Flag.SERVICE_RESTREGION, Flag.SERVICE_IMAX, Flag.SERVICE_CHILD,
				Flag.SERVICE_PARK_RECOMMEND, Flag.SERVICE_VISACARD_RECOMMEND, Flag.SERVICE_3D_RECOMMEND, Flag.SERVICE_SALE_RECOMMEND, 
				Flag.SERVICE_FOOD_RECOMMEND, Flag.SERVICE_RESTREGION_RECOMMEND, Flag.SERVICE_PAIRSEAT_RECOMMEND, Flag.SERVICE_IMAX_RECOMMEND, 
				Flag.SERVICE_CHILD_RECOMMEND, Flag.SERVICE_CHARACTERISTIC,Flag.SERVICE_RECREATION,Flag.SERVICE_SHOPPING,
				Flag.SERVICE_RECREATION_RECOMMEND,Flag.SERVICE_SHOPPING_RECOMMEND,Flag.SERVICE_SHOPPING_TIME,Flag.SERVICE_SHOPPING_TIME_RECOMMEND,
				Flag.SERVICE_EARLY_END_MPI,Flag.SERVICE_OTHER_PARK,Flag.SERVICE_PARK_RECOMMEND_REMARK
		};
		Map<String, Object> otherinfoMap = controllerService.getAndSetOtherinfo(cinema.getOtherinfo(), Arrays.asList(otheritemList), request);
		cinema.setOtherinfo(JsonUtils.writeObjectToJson(otherinfoMap));
		daoService.saveObject(cinema);
		monitorService.saveChangeLog(getLogonUser().getId(), Cinema.class, cinema.getId(),changeEntry.getChangeMap(cinema));
		searchService.pushSearchKey(cinema);//更新索引至索引服务器
		return showJsonSuccess(model, cinema.getId()+"");
	}
	//保存影院基础信息
	@RequestMapping("/admin/cinema/ajax/saveBaseCinema.xhtml")
	public String saveBaseCinema(Long cinemaId, String name, String countyCode, String indexareaCode, 
			Integer telephoneLength, HttpServletRequest request, ModelMap model){
		Cinema cinema = null;
		String citycode = null;
		if(cinemaId!=null) {
			cinema = daoService.getObject(Cinema.class, new Long(cinemaId));
			citycode = cinema.getCitycode();
		}else{
			cinema = new Cinema(name);
			citycode = getAdminCitycode(request);
		}
		ChangeEntry changeEntry = new ChangeEntry(cinema);
		BindUtils.bindData(cinema, request.getParameterMap());
		//验证内容
		String msg=ValidateUtil.validateNewsContent(null, cinema.getContent());
		if(StringUtils.isNotBlank(msg))return showJsonError(model, msg);
		cinema.setCitycode(citycode);

		if(StringUtils.isNotBlank(countyCode)){
			cinema.setCountycode(StringUtils.trimToNull(countyCode));
			cinema.setCountyname(daoService.getObject(County.class, countyCode).getCountyname());
		}else{
			cinema.setCountycode(null);
			cinema.setCountyname(null);
		}
		if(StringUtils.isNotBlank(indexareaCode)){
			String indexareacode = StringUtils.trimToNull(indexareaCode);
			cinema.setIndexareacode(indexareacode);
			cinema.setIndexareaname(daoService.getObject(Indexarea.class, indexareacode).getIndexareaname());
		}else {
			cinema.setIndexareacode(null);
			cinema.setIndexareaname(null);
		}
		if(cinema.getAddtime()==null) cinema.setAddtime(new Timestamp(System.currentTimeMillis()));
		if(StringUtils.isBlank(cinema.getPinyin())) {
			cinema.setPinyin(PinYinUtils.getPinyin(cinema.getName()));
		}
		cinema.setUpdatetime(new Timestamp(System.currentTimeMillis()));
		if(telephoneLength != null){
			List<Map<String,String>> phoneList = new LinkedList<Map<String,String>>();
			if(telephoneLength == 1){
				String areaCode = request.getParameter("areaCode");
				String phone = request.getParameter("phone");
				String phoneRemark = request.getParameter("phoneRemark");
				setCinemaTelephones(areaCode, phone, phoneRemark, phoneList);
			}else{
				for(int index = 0;index < telephoneLength;index++){
					String areaCode = request.getParameter("areaCode[" + index + "]");
					String phone = request.getParameter("phone[" + index + "]");
					String phoneRemark = request.getParameter("phoneRemark[" + index + "]");
					this.setCinemaTelephones(areaCode, phone, phoneRemark, phoneList);
				}
			}
			if(phoneList.isEmpty()){
				cinema.setContactTelephone(null);
			}else{
				cinema.setContactTelephone(JsonUtils.writeObjectToJson(phoneList));
			}
		}
		Map<String, Object> otherinfoMap = JsonUtils.readJsonToMap(cinema.getOtherinfo());
		String info = request.getParameter(Flag.SERVICE_COMMENTID);
		if(StringUtils.isNotBlank(info)){
			otherinfoMap.put(Flag.SERVICE_COMMENTID, info);
		}else{
			otherinfoMap.remove(Flag.SERVICE_COMMENTID);
		}
		cinema.setOtherinfo(JsonUtils.writeObjectToJson(otherinfoMap));
		daoService.saveObject(cinema);
		
		monitorService.saveChangeLog(getLogonUser().getId(), Cinema.class, cinema.getId(),changeEntry.getChangeMap(cinema));
		searchService.pushSearchKey(cinema);//更新索引至索引服务器
		return showJsonSuccess(model, cinema.getId()+"");
	}
	
	
	//保存影院
	@RequestMapping("/admin/cinema/ajax/saveCinema.xhtml")
	public String saveCinema(Long cinemaId, String name, String countyCode, String indexareaCode, 
			Long stationid, HttpServletRequest request, ModelMap model){
		Cinema cinema = null;
		String citycode = null;
		if(cinemaId!=null) {
			cinema = daoService.getObject(Cinema.class, new Long(cinemaId));
			citycode = cinema.getCitycode();
		}else{
			cinema = new Cinema(name);
			citycode = getAdminCitycode(request);
		}
		ChangeEntry changeEntry = new ChangeEntry(cinema);
		BindUtils.bindData(cinema, request.getParameterMap());
		String[] otheritemList = new String[]{
				Flag.SERVICE_WEBCOMMENT, Flag.SERVICE_PAIRSEAT, Flag.SERVICE_PARK, Flag.SERVICE_VISACARD, Flag.SERVICE_PLAYGROUND, 
				Flag.SERVICE_3D, Flag.SERVICE_SALE, Flag.SERVICE_FOOD, Flag.SERVICE_RESTREGION, Flag.SERVICE_IMAX, Flag.SERVICE_CHILD,
				Flag.SERVICE_PARK_RECOMMEND, Flag.SERVICE_VISACARD_RECOMMEND, Flag.SERVICE_3D_RECOMMEND, Flag.SERVICE_SALE_RECOMMEND, 
				Flag.SERVICE_FOOD_RECOMMEND, Flag.SERVICE_RESTREGION_RECOMMEND, Flag.SERVICE_PAIRSEAT_RECOMMEND, Flag.SERVICE_IMAX_RECOMMEND, 
				Flag.SERVICE_CHILD_RECOMMEND, Flag.SERVICE_COMMENTID, Flag.SERVICE_CHARACTERISTIC,Flag.SERVICE_RECREATION,Flag.SERVICE_SHOPPING,
				Flag.SERVICE_RECREATION_RECOMMEND,Flag.SERVICE_SHOPPING_RECOMMEND,Flag.SERVICE_SHOPPING_TIME,Flag.SERVICE_SHOPPING_TIME_RECOMMEND,
				Flag.SERVICE_EARLY_END_MPI,Flag.SERVICE_OTHER_PARK,Flag.SERVICE_PARK_RECOMMEND_REMARK
		};
		Map<String, Object> otherinfoMap = controllerService.getAndSetOtherinfo(cinema.getOtherinfo(), Arrays.asList(otheritemList), request);
		//验证内容
		String msg=ValidateUtil.validateNewsContent(null, cinema.getContent());
		if(StringUtils.isNotBlank(msg))return showJsonError(model, msg);
		cinema.setCitycode(citycode);

		if(StringUtils.isNotBlank(countyCode)){
			cinema.setCountycode(StringUtils.trimToNull(countyCode));
			cinema.setCountyname(daoService.getObject(County.class, countyCode).getCountyname());
		}else{
			cinema.setCountycode(null);
			cinema.setCountyname(null);
		}
		if(StringUtils.isNotBlank(indexareaCode)){
			String indexareacode = StringUtils.trimToNull(indexareaCode);
			cinema.setIndexareacode(indexareacode);
			cinema.setIndexareaname(daoService.getObject(Indexarea.class, indexareacode).getIndexareaname());
		}else {
			cinema.setIndexareacode(null);
			cinema.setIndexareaname(null);
		}
		if(stationid!=null){
			Subwaystation subwaystation = daoService.getObject(Subwaystation.class, stationid);
			cinema.setStationname(subwaystation.getStationname());
		}else{
			cinema.setStationname(null);
		}
		if(cinema.getAddtime()==null) cinema.setAddtime(new Timestamp(System.currentTimeMillis()));
		if(StringUtils.isBlank(cinema.getPinyin())) {
			cinema.setPinyin(PinYinUtils.getPinyin(cinema.getName()));
		}
		cinema.setOtherinfo(JsonUtils.writeObjectToJson(otherinfoMap));
		cinema.setUpdatetime(new Timestamp(System.currentTimeMillis()));
		daoService.saveObject(cinema);
		
		monitorService.saveChangeLog(getLogonUser().getId(), Cinema.class, cinema.getId(),changeEntry.getChangeMap(cinema));
		searchService.pushSearchKey(cinema);//更新索引至索引服务器
		return showJsonSuccess(model, cinema.getId()+"");
	}
}
