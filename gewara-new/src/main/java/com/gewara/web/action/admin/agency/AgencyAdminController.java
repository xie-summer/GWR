package com.gewara.web.action.admin.agency;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import com.gewara.constant.GoodsConstant;
import com.gewara.constant.TagConstant;
import com.gewara.model.acl.User;
import com.gewara.model.agency.Agency;
import com.gewara.model.agency.AgencyProfile;
import com.gewara.model.agency.AgencyToVenue;
import com.gewara.model.agency.Curriculum;
import com.gewara.model.agency.TrainingGoods;
import com.gewara.model.drama.DramaStar;
import com.gewara.model.drama.DramaToStar;
import com.gewara.model.sport.Sport;
import com.gewara.service.drama.DramaToStarService;
import com.gewara.service.sport.AgencyService;
import com.gewara.untrans.SearchService;
import com.gewara.util.BeanUtil;
import com.gewara.util.BindUtils;
import com.gewara.util.ChangeEntry;
import com.gewara.util.DateUtil;
import com.gewara.util.ValidateUtil;
import com.gewara.web.action.admin.BaseAdminController;

@Controller
public class AgencyAdminController extends BaseAdminController{
	
	@Autowired@Qualifier("agencyService")
	private AgencyService agencyService;
	@Autowired@Qualifier("dramaToStarService")
	private DramaToStarService dramaToStarService;
	@Autowired@Qualifier("searchService")
	private SearchService searchService;

	//后台机构列表
	@RequestMapping("/admin/agency/getAgencyList.xhtml")
	public String getAgencyList(String searchKey, HttpServletRequest request, ModelMap model){
		List<Agency> agencyList = new ArrayList<Agency>();
		if(ValidateUtil.isNumber(searchKey)){
			agencyList.add(daoService.getObject(Agency.class, searchKey));
		}else{
			agencyList = agencyService.getAgencyList(searchKey, getAdminCitycode(request), "hotvalue", false, 0, 100);
		}
		model.put("agencyList", agencyList);
		return "/admin/agency/agencyList.vm";
	}
	//后台获取单个机构信息
	@RequestMapping("/admin/agency/modifyAgencyDetail.xhtml")
	public String modifyAgencyDetail(Long agencyId, ModelMap model){
		if(agencyId != null){
			Agency agency = daoService.getObject(Agency.class, agencyId);
			model.put("agency", agency);
		}
		return "/admin/agency/agencyForm.vm";
	}
	//后台保存机构
	@RequestMapping("/admin/agency/ajax/saveAgency.xhtml")
	public String saveAgency(Long agencyId, String name, HttpServletRequest request, ModelMap model) {
		Timestamp curTimestamp = DateUtil.getCurFullTimestamp();
		Agency agency = null;
		if(agencyId == null){
			agency = new Agency(name, getAdminCitycode(request));
		}else{
			agency = daoService.getObject(Agency.class, agencyId);
		}
		agency.setUpdatetime(curTimestamp);
		ChangeEntry changeEntry = new ChangeEntry(agency);
		Map<String, String[]> dataMap = request.getParameterMap(); 
		BindUtils.bindData(agency, dataMap);
		agency = daoService.saveObject(agency);
		monitorService.saveChangeLog(getLogonUser().getId(), Agency.class, agency.getId(),changeEntry.getChangeMap(agency));
		searchService.pushSearchKey(agency);
		return showJsonSuccess(model, agency.getId()+"");
	}
	//后台修改机构状态和排序值
	@RequestMapping("/admin/agency/ajax/updateAgencyHotValueOrStatus.xhtml")
	public String setAgencyHotValue(Long agencyId, Integer hotvalue, String status, ModelMap model){
		Agency agency = daoService.getObject(Agency.class, agencyId);
		if(agency == null) return showJsonError(model, "未找到此机构！");
		ChangeEntry changeEntry = new ChangeEntry(agency);
		if(hotvalue != null) agency.setHotvalue(hotvalue);
		if(StringUtils.isNotBlank(status)){
			agency.setStatus(status);
			List<TrainingGoods> trainingGoodsList = agencyService.getTrainingGoodsList(agency.getCitycode(), TagConstant.TAG_AGENCY, agencyId, null, null, null, "goodssort", true, false, 0, 500);
			for (TrainingGoods trainingGoods : trainingGoodsList) {
				trainingGoods.setStatus(status);
			}
			daoService.saveObjectList(trainingGoodsList);
		}
		daoService.saveObject(agency);
		monitorService.saveChangeLog(getLogonUser().getId(), Agency.class, agency.getId(),changeEntry.getChangeMap(agency));
		return showJsonSuccess(model);
	}
	//后台获取机构的常驻场馆列表
	@RequestMapping("/admin/agency/getAgencyToVenueList.xhtml")
	public String getAgencyToVenueList(Long agencyId, ModelMap model){
		Agency agency = daoService.getObject(Agency.class, agencyId);
		if(agency == null) return show404(model, "未找到此机构！");
		List<AgencyToVenue> atvList = daoService.getObjectListByField(AgencyToVenue.class, "agencyId", agencyId);
		List<Long> idList = BeanUtil.getBeanPropertyList(atvList, Long.class, "venueId", true);
		Map<Long, Sport> sportMap = daoService.getObjectMap(Sport.class, idList);
		model.put("sportMap", sportMap);
		model.put("atvList", atvList);
		model.put("agency", agency);
		return "/admin/agency/agencyToVenueList.vm";
	}
	//得到单个场馆
	@RequestMapping("/admin/agency/getAgencyToVenue.xhtml")
	public String getAgencyToVenue(Long id, Long agencyId, ModelMap model){
		Agency agency = daoService.getObject(Agency.class, agencyId);
		if(agency == null) return showJsonError(model, "未找到此机构！");
		AgencyToVenue atv = daoService.getObject(AgencyToVenue.class, id);
		if(atv != null){
			Sport sport = daoService.getObject(Sport.class, atv.getVenueId());
			if(sport == null) return showJsonError(model, "未找到此关联场馆！");
			model.put("atv", atv);
			model.put("sport", sport);
		}
		model.put("agency", agency);
		return "/admin/agency/agencyToVenueForm.vm";
	}
	//添加常驻场馆
	@RequestMapping("/admin/agency/saveAgencyToVenue.xhtml")
	public String saveAgencyToVenue(Long id, Long agencyId, Long venueId, String agencytype, ModelMap model){
		Agency agency = daoService.getObject(Agency.class, agencyId);
		if(agency == null) return showJsonError(model, "未找到此机构！");
		Sport sport = daoService.getObject(Sport.class, venueId);
		if(sport == null) return showJsonError(model, "未找到此关联场馆！");
		AgencyToVenue atv = null;
		if(id == null){
			atv = new AgencyToVenue();
			atv.setNumsort(0);
		}else{
			atv = daoService.getObject(AgencyToVenue.class, id);
		}
		atv.setAgencyId(agencyId);
		atv.setVenueId(venueId);
		atv.setAgencytype(agencytype);
		daoService.saveObject(atv);
		return showJsonSuccess(model);
	}
	//删除常驻场馆
	@RequestMapping("/admin/agency/delAgencyToVenue.xhtml")
	public String delAgencyToVenue(Long id, ModelMap model){
		daoService.removeObjectById(AgencyToVenue.class, id);
		return showJsonSuccess(model);
	}
	//排序
	@RequestMapping("/admin/agency/changeNumsort.xhtml")
	public String changeNumsort(Long id,Integer numsort, ModelMap model){
		if(numsort == null) return showJsonError(model, "排序不能为空！");
		AgencyToVenue atv = daoService.getObject(AgencyToVenue.class, id);
		if(atv == null) return showJsonError(model, "未找到此常驻场馆！");
		atv.setNumsort(numsort);
		daoService.saveObject(atv);
		return showJsonSuccess(model);
	}
	//得到机构的教练列表
	@RequestMapping("/admin/agency/getSportStarList.xhtml")
	public String getSportStarList(Long agencyId, ModelMap model){
		Agency agency = daoService.getObject(Agency.class, agencyId);
		if(agency == null) return show404(model, "未找到此机构！");
		List<DramaToStar> dtsList = dramaToStarService.getDramaToStarListByDramaid(TagConstant.TAG_AGENCY, agencyId, false);
		List<Long> starIdList = BeanUtil.getBeanPropertyList(dtsList, Long.class, "starid", true);
		Map<Long,DramaStar> starMap = daoService.getObjectMap(DramaStar.class, starIdList);
		model.put("dtsList", dtsList);
		model.put("starMap", starMap);
		model.put("agency", agency);
		return "/admin/agency/sportStarList.vm";
	}
	@RequestMapping("/admin/agency/getSportStar.xhtml")
	public String getSportStar(Long agencyId, Long id, ModelMap model){
		Agency agency = daoService.getObject(Agency.class, agencyId);
		if(agency == null) return show404(model, "未找到此机构！");
		if(id != null){
			DramaToStar dts = daoService.getObject(DramaToStar.class, id);
			model.put("dts", dts);
		}
		model.put("agency", agency);
		return "/admin/agency/sportStarForm.vm";
	}
	//保存机构教练
	@RequestMapping("/admin/agency/saveSporStar.xhtml")
	public String saveSporStar(Long id, Long relatedId, Long starId, String type, ModelMap model){
		DramaStar star = daoService.getObject(DramaStar.class, starId);
		if(star == null || StringUtils.equals(star.getTag(), TagConstant.TAG_DRAMA)) return showJsonError(model, "未找到此教练或此教练不是运动教练！");
		if(StringUtils.equals(type, GoodsConstant.GOODS_TYPE_TRAINING)){
			TrainingGoods trainingGoods = daoService.getObject(TrainingGoods.class, relatedId);
			if(trainingGoods == null) return showJsonError(model, "未找到此课程！");
		}else{
			type = TagConstant.TAG_AGENCY;
			Agency agency = daoService.getObject(Agency.class, relatedId);
			if(agency == null) return showJsonError(model, "未找到此机构！");
		}
		int count = dramaToStarService.getStarCount(relatedId, starId);
		if(count == 0){
			DramaToStar dts = null;
			if(id == null){
				dts = new DramaToStar();
				dts.setNumsort(0);
			}else{
				dts = daoService.getObject(DramaToStar.class, id);
			}
			dts.setTag(type);
			dts.setDramaid(relatedId);
			dts.setStarid(starId);
			daoService.saveObject(dts);
		}
		return showJsonSuccess(model);
	}
	@RequestMapping("/admin/agency/changeDtsNumsort.xhtml")
	public String changeDtsNumsort(Long id,Integer numsort, ModelMap model){
		if(numsort == null) return showJsonError(model, "排序不能为空！");
		DramaToStar dts = daoService.getObject(DramaToStar.class, id);
		if(dts == null) return showJsonError(model, "未找到此教练！");
		dts.setNumsort(numsort);
		daoService.saveObject(dts);
		return showJsonSuccess(model);
	}
	@RequestMapping("/admin/agency/delDts.xhtml")
	public String delDts(Long id, ModelMap model){
		daoService.removeObjectById(DramaToStar.class, id);
		return showJsonSuccess(model);
	}
	@RequestMapping("/admin/agency/delCurriculum.xhtml")
	public String delCurriculum(Long id, ModelMap model){
		daoService.removeObjectById(Curriculum.class, id);
		return showJsonSuccess(model);
	}
	@RequestMapping("/admin/agency/saveBaseData.xhtml")
	public String saveBaseData(Long agencyId, HttpServletRequest request, ModelMap model){
		Agency agency = daoService.getObject(Agency.class, agencyId);
		if(agency == null) return showJsonError(model, "参数错误！");
		AgencyProfile profile = daoService.getObject(AgencyProfile.class, agencyId);
		if(profile==null) profile = new AgencyProfile();
		ChangeEntry changeEntry = new ChangeEntry(profile);
		BindUtils.bindData(profile, request.getParameterMap());
		profile.setId(agency.getId());
		if(profile.getMobiles() != null){
			String mobiles = StringUtils.replace(profile.getMobiles(), "，", ",");
			String[] mobileList = StringUtils.split(mobiles, ",");
			for(String mobile : mobileList){
				if (!ValidateUtil.isMobile(mobile)) {
					return showJsonError(model, mobile+"此手机号有误！");
				}
			}
			profile.setMobiles(StringUtils.join(mobileList, ","));
		}
		daoService.saveObject(profile);
		User user = getLogonUser();
		model.put("agencyId", agencyId);
		model.put("msg", "保存成功！");
		if(!StringUtils.equals(agency.getBooking(), profile.getStatus())) {
			agency.setBooking(profile.getStatus());
			daoService.saveObject(agency);
		}
		monitorService.saveChangeLog(user.getId(), AgencyProfile.class, profile.getId(), changeEntry.getChangeMap(profile));
		return "redirect:/admin/agency/baseData.xhtml";
	}
	@RequestMapping("/admin/agency/baseData.xhtml")
	public String baseData(Long agencyId, ModelMap model){
		Agency agency =  daoService.getObject(Agency.class, agencyId);
		model.put("agency", agency);
		AgencyProfile profile = daoService.getObject(AgencyProfile.class, agencyId);
		model.put("profile", profile);
		return "admin/agency/baseData.vm";
	}
}
