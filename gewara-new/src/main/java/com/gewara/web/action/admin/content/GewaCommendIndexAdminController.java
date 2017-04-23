package com.gewara.web.action.admin.content;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import com.gewara.constant.TagConstant;
import com.gewara.constant.content.SignName;
import com.gewara.model.acl.User;
import com.gewara.model.content.GewaCommend;
import com.gewara.model.movie.SpecialActivity;
import com.gewara.model.sport.Sport;
import com.gewara.model.sport.SportPrice;
import com.gewara.model.sport.SportPriceTable;
import com.gewara.service.content.SpecialActivityService;
import com.gewara.service.sport.SportService;
import com.gewara.util.BeanUtil;
import com.gewara.util.BindUtils;
import com.gewara.util.ChangeEntry;
import com.gewara.util.RelatedHelper;
import com.gewara.util.ValidateUtil;
import com.gewara.web.util.PageUtil;

/**
 *	 @function 话剧后台推荐 
 * 	@author bob.hu
 *		@date	2010-11-29 11:47:56
 */
@Controller
public class GewaCommendIndexAdminController extends GewaCommendBaseAdminController {
	@Autowired@Qualifier("sportService")
	private SportService sportService;
	public void setSportService(SportService sportService) {
		this.sportService = sportService;
	}
	@Autowired@Qualifier("specialActivityService")
	private SpecialActivityService specialActivityService;
	public void setSpecialActivityService(SpecialActivityService specialActivityService) {
		this.specialActivityService = specialActivityService;
	}

	/***
	 *  话剧数据推荐
	 */
	@RequestMapping("/admin/drama/dramaCommend.xhtml")
	public String dramaCommend(){
		return "admin/drama/commendIndex.vm";
	}
	
	/**
	 *	话题后台推荐列表 (传递signname)  
	 */
	@RequestMapping("/admin/recommend/basedramaindex.xhtml")
	public String search2(ModelMap model, HttpServletRequest request, String signname) {
		String url = "admin/recommend/publicRecommend.vm";
		RelatedHelper rh = new RelatedHelper();
		model.put("relatedHelper", rh);
		return getCommendList(rh, signname, null, false, url, request, model);
	}

	// 运动基本模块(传递signname)
	@RequestMapping("/admin/recommend/basesportindex.xhtml")
	public String basesportindex(ModelMap model, HttpServletRequest request, String signname) {
		String url = "admin/recommend/movieindex/search.vm";
		RelatedHelper rh = new RelatedHelper();
		model.put("relatedHelper", rh);
		return getCommendList(rh, signname, null, false, url, request, model);
	}
	@RequestMapping("/admin/recommend/sportvenuecommend.xhtml")
	public String sportvenuecommend(ModelMap model, HttpServletRequest request, String signname) {
		
		Map<Long,SportPriceTable> sportPriceTableMap = new HashMap<Long,SportPriceTable>();
		Map<Long,SportPrice> sportPriceMap = new HashMap<Long,SportPrice>();
		
		List<GewaCommend> gcList = commonService.getGewaCommendList(getAdminCitycode(request), signname, null,null, false,0,200);
		Map<String,List<GewaCommend>> gewaCommendMap = new HashMap<String,List<GewaCommend>>();
		List<GewaCommend> top10list = new ArrayList<GewaCommend>();
		Set<Long> sportidList = new HashSet<Long>();
		RelatedHelper rh = new RelatedHelper();
		model.put("relatedHelper", rh);
		if(gcList != null && gcList.size() > 0){
			commonService.initGewaCommendList("gcList", rh, gcList);
			model.put("gcList", gcList);
			List<Serializable> parentidList = BeanUtil.getBeanPropertyList(gcList, Serializable.class, "parentid", true);
			relateService.addRelatedObject(1, "parentidList", rh, TagConstant.TAG_SPORTITEM, parentidList);
			List<GewaCommend> gewaCommendList = null;
			String countyname = null;
			for(GewaCommend gc : gcList){
				if(gc.getRelatedid()== null) continue;
				SportPriceTable sportPriceTable = sportService.getSportPriceTable(gc.getRelatedid(), gc.getParentid());
				sportPriceTableMap.put(gc.getId(), sportPriceTable);
				if(sportPriceTable != null){
					SportPrice sportPrice=sportService.getSportPriceByPriceTableId(sportPriceTable.getId());
					sportPriceMap.put(gc.getId(), sportPrice);
					sportidList.add(sportPriceTable.getSportid());
					Sport sport = daoService.getObject(Sport.class, gc.getRelatedid());
					countyname = sport.getCountyname();
					gewaCommendList = gewaCommendMap.get(countyname);
					if(gewaCommendList == null) {
						gewaCommendList = new ArrayList<GewaCommend>();
						gewaCommendMap.put(countyname, gewaCommendList);
					}
					gewaCommendList.add(gc);
				}
			}
			if(gcList.size()>10){
				top10list.addAll(gcList.subList(0, 10));
			}else{
				top10list.addAll(gcList);
			} 
		}
		Map<Long, Sport> sportMap = daoService.getObjectMap(Sport.class, sportidList);
		model.put("sportMap", sportMap);
		model.put("top10list", top10list);
		model.put("gewaCommendMap", gewaCommendMap);
		model.put("signname", signname);
		model.put("sportPriceTableMap", sportPriceTableMap);
		model.put("sportPriceMap", sportPriceMap);
		return "admin/recommend/sportindex/search2.vm";
	}
	@RequestMapping("/admin/recommend/specialActivityList.xhtml")
	public String activityList(Integer pageNo,String type, HttpServletRequest request, ModelMap model) throws Exception {
		if(type != null)model.put("type",type);
		if(pageNo == null) pageNo = 0;
		int rowsPerPage = 20;
		int firstRow = pageNo*rowsPerPage;
		List<SpecialActivity> activityList = specialActivityService.getSpecialActivityList(null, null, null, firstRow, rowsPerPage);
		int count = specialActivityService.getSpecialActivityCount(null, null, null);
		model.put("activityList", activityList);
		PageUtil pageUtil=new PageUtil(count,rowsPerPage,pageNo,"/admin/recommend/specialActivityList.xhtml");
		pageUtil.initPageInfo();
		model.put("pageUtil", pageUtil);
		String citycode = getAdminCitycode(request);
		addActivityExtrData(model, citycode);
		return "admin/recommend/specialActivityList.vm";
	}
	@RequestMapping("/admin/recommend/saveOrUpdateSpecialActivity.xhtml")
	public String saveOrUpdateSpecialActivity(Long id, HttpServletRequest request, ModelMap model) {
		User user = getLogonUser();
		SpecialActivity specialActivity = new SpecialActivity("");
		if (id!=null)
			specialActivity = daoService.getObject(SpecialActivity.class,
					new Long(id));
		ChangeEntry changeEntry = new ChangeEntry(specialActivity);
		BindUtils.bindData(specialActivity, request.getParameterMap());
		//验证内容
		String msg=ValidateUtil.validateNewsContent(null, specialActivity.getContent());
		if(StringUtils.isNotBlank(msg))return showJsonError(model, msg);
		daoService.saveObject(specialActivity);
		monitorService.saveChangeLog(user.getId(),SpecialActivity.class, specialActivity.getId(),changeEntry.getChangeMap( specialActivity));
		Map result = BeanUtil.getBeanMap(specialActivity, true);
		return showJsonSuccess(model, result);
	}
	@RequestMapping("/admin/recommend/getSpecialActivityById.xhtml")
	public String getSpecialActivityById(Long activityId, ModelMap model) {
		SpecialActivity sa = daoService.getObject(SpecialActivity.class, activityId);
		Map result = BeanUtil.getBeanMap(sa, true);
		return showJsonSuccess(model, result);
	}
	@RequestMapping("/admin/recommend/removeSpecialActivityById.xhtml")
	public String removeSpecialActivityById(Long activityId, HttpServletRequest request, ModelMap model) {
		SpecialActivity sa = daoService.getObject(SpecialActivity.class, activityId);
		if (sa == null) return showJsonError(model, "数据不存在！");
		// 删除关联数据
		List<GewaCommend> list = getRelateList(activityId, request);
		if(list != null && list.size() > 0){
			daoService.removeObjectList(list);
		}
		daoService.removeObject(sa);
		monitorService.saveDelLog(getLogonUser().getId(), activityId, sa);
		return showJsonSuccess(model);
	}
	@RequestMapping("/admin/recommend/updateSpecailStatus.xhtml")
	public String updateSpecailStatus(Long specid, String specstatus, ModelMap model){
		SpecialActivity specialActivity = daoService.getObject(SpecialActivity.class, specid);
		if(specialActivity == null) return showJsonError_NOT_FOUND(model);
		specialActivity.setStatus(specstatus);
		daoService.saveObject(specialActivity);
		return showJsonSuccess(model);
	}
	
	// 查找关联数据
	private List<GewaCommend> getRelateList(Long activityId, HttpServletRequest request){
		// 1个模板关联headpic, blogpic, 组图
		List<GewaCommend> needsDelList = new ArrayList<GewaCommend>();
		
		SpecialActivity specialActivity = daoService.getObject(SpecialActivity.class, activityId);
		Long headpic = specialActivity.getHeadpic();
		if(headpic != null){
			needsDelList.add(daoService.getObject(GewaCommend.class, headpic));
		}
		Long blogpic = specialActivity.getBlogpic();
		if(blogpic != null){
			needsDelList.add(daoService.getObject(GewaCommend.class, blogpic));
		}
		String citycode = getAdminCitycode(request);
		List<GewaCommend> teampicList = commonService.getGewaCommendList(citycode, SignName.TPL_TEAMPIC, activityId,null, false, 0,100);
		if(teampicList != null && teampicList.size() > 0){
			needsDelList.addAll(teampicList);
		}
		
		return needsDelList;
	}
	//TODO:dig???
	@RequestMapping("/admin/recommend/adddig.xhtml")
	public String adddig(Long commid, Long acid, String tag, ModelMap model, HttpServletRequest request)throws Exception{
		GewaCommend gewaCommend = null;
		if(commid == null){
			gewaCommend = new GewaCommend(tag);
		}else{
			gewaCommend = daoService.getObject(GewaCommend.class, commid);
		}
		String citycode = StringUtils.isBlank(gewaCommend.getCitycode()) ? getAdminCitycode(request) : gewaCommend.getCitycode();
		gewaCommend.setCitycode(citycode);
		gewaCommend.setRelatedid(acid);
		daoService.saveObject(gewaCommend);
		return showJsonSuccess(model, gewaCommend.getId()+"");
	}
	private void addActivityExtrData(ModelMap model, String citycode) throws Exception{
		// movie, ktv, bar, gym, sport,drama
		List<GewaCommend> movieCommList = commonService.getGewaCommendList(citycode, "dig_movie", null,null, false,0, 1);
		if(movieCommList != null && movieCommList.size() > 0){
			model.put("digmov", movieCommList.get(0));
		}
		List<GewaCommend> ktvCommList = commonService.getGewaCommendList(citycode, "dig_ktv", null,null, false,0, 1);
		if(ktvCommList != null && ktvCommList.size() > 0){
			model.put("digktv", ktvCommList.get(0));
		}
		List<GewaCommend> barCommList = commonService.getGewaCommendList(citycode, "dig_bar", null,null, false,0, 1);
		if(barCommList != null && barCommList.size() > 0){
			model.put("digbar", barCommList.get(0));
		}
		List<GewaCommend> gymCommList = commonService.getGewaCommendList(citycode, "dig_gym", null,null, false,0, 1);
		if(gymCommList != null && gymCommList.size() > 0){
			model.put("diggym", gymCommList.get(0));
		}
		List<GewaCommend> sportCommList = commonService.getGewaCommendList(citycode, "dig_sport", null,null, false,0, 1);
		if(sportCommList != null && sportCommList.size() > 0){
			model.put("digsport", sportCommList.get(0));
		}
		List<GewaCommend> dramaCommList = commonService.getGewaCommendList(citycode, "dig_drama", null,null, false,0, 1);
		if(dramaCommList != null && dramaCommList.size() > 0){
			model.put("digdrama", dramaCommList.get(0));
		}
	}
}
