package com.gewara.web.action.admin.drama;

import java.sql.Timestamp;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import com.gewara.constant.DramaConstant;
import com.gewara.constant.Flag;
import com.gewara.constant.TagConstant;
import com.gewara.model.drama.Drama;
import com.gewara.model.drama.DramaStar;
import com.gewara.service.bbs.BlogService;
import com.gewara.service.drama.DramaStarService;
import com.gewara.service.drama.DramaToStarService;
import com.gewara.untrans.SearchService;
import com.gewara.untrans.impl.ControllerService;
import com.gewara.util.BeanUtil;
import com.gewara.util.BindUtils;
import com.gewara.util.ChangeEntry;
import com.gewara.util.JsonUtils;
import com.gewara.util.PinYinUtils;
import com.gewara.util.ValidateUtil;
import com.gewara.util.VmUtils;
import com.gewara.util.WebUtils;
import com.gewara.web.action.admin.BaseAdminController;


@Controller
public class DramaAdminController extends BaseAdminController {
	@Autowired@Qualifier("dramaStarService")
	private DramaStarService dramaStarService;
	public void setDramaStarService(DramaStarService dramaStarService) {
		this.dramaStarService = dramaStarService;
	}
	@Autowired@Qualifier("dramaToStarService")
	private DramaToStarService dramaToStarService;
	public void setDramaToStarService(DramaToStarService dramaToStarService) {
		this.dramaToStarService = dramaToStarService;
	}
	@Autowired@Qualifier("searchService")
	private SearchService searchService;
	public void setSearchService(SearchService searchService) {
		this.searchService = searchService;
	}
	
	@Autowired@Qualifier("blogService")
	private BlogService blogService;
	
	@Autowired@Qualifier("controllerService")
	private ControllerService controllerService;
	
	@RequestMapping("/admin/drama/ajax/setDramaHotValue.xhtml")
	public String setTheatreHotValue(Long id, Integer value, ModelMap model) {
		Drama drama = daoService.getObject(Drama.class, id);
		drama.setHotvalue(value);
		daoService.updateObject(drama);
		return showJsonSuccess(model);
	}
	@RequestMapping("/admin/drama/dramaList.xhtml")
	public String getTheatreList(String dramaname, HttpServletRequest request, ModelMap model) {
		DetachedCriteria qry = DetachedCriteria.forClass(Drama.class);
		qry.add(Restrictions.eq("citycode", getAdminCitycode(request)));
		if (StringUtils.isNotBlank(dramaname)) qry.add(Restrictions.like("dramaname", dramaname, MatchMode.ANYWHERE));
		qry.addOrder(Order.desc("addtime"));
		List<Drama> dramaList = hibernateTemplate.findByCriteria(qry);
		model.put("dramaList", dramaList);
		model.put("dramaTypeMap", DramaConstant.dramaTypeMap);
		return "admin/drama/dramaList.vm";
	}
	@RequestMapping("/admin/drama/modifyDramaDetail.xhtml")
	public String modifyDrama(Long dramaId, ModelMap model) {
		Drama drama = null;
		Map<String,String> dramaDataMap = new HashMap<String,String>();
		if (dramaId != null){
			drama = daoService.getObject(Drama.class, dramaId);
			
			// 取出当前关系表中的数据
			if(StringUtils.isNotBlank(drama.getActors())){
				List<Long> idList = BeanUtil.getIdList(drama.getActors(), ",");
				List<DramaStar> actorList = daoService.getObjectList(DramaStar.class, idList);
				Map<Long,String> actorMap = BeanUtil.getKeyValuePairMap(actorList, "id", "name");
				model.put("actorMap", actorMap);
			}
			if(StringUtils.isNotBlank(drama.getDirector())){
				List<Long> idList = BeanUtil.getIdList(drama.getDirector(), ",");
				List<DramaStar> directorList = daoService.getObjectList(DramaStar.class, idList);
				Map<Long,String> direcotrMap = BeanUtil.getKeyValuePairMap(directorList, "id", "name");
				model.put("direcotrMap", direcotrMap);
			}
			if(StringUtils.isNotBlank(drama.getTroupecompany())){
				List<Long> idList = BeanUtil.getIdList(drama.getTroupecompany(), ",");
				List<DramaStar> troupecompanyList = daoService.getObjectList(DramaStar.class, idList);
				Map<Long,String> troupecompanyMap = BeanUtil.getKeyValuePairMap(troupecompanyList, "id", "name");
				model.put("troupecompanyMap",troupecompanyMap);
			}
			if(StringUtils.isNotBlank(drama.getDramadata())) {
				dramaDataMap = VmUtils.readJsonToMap(drama.getDramadata());
				model.put("dramaDataMap", dramaDataMap);
			}
		}
		model.put("drama", drama);
		model.put("dramaTypeMap", DramaConstant.dramaTypeMap);
		model.put("dramaSaleCycleMap", DramaConstant.dramaSaleCycleMap);
		return "admin/drama/dramaForm.vm";
	}
	@RequestMapping("/admin/drama/saveDramaProperty.xhtml")
	public String addDramaProperty(Long dramaid, String dname, String dvalue, ModelMap model){
		dname = StringUtils.trim(dname);
		dvalue = StringUtils.trim(dvalue);
		if(StringUtils.isBlank(dname)) return showJsonError(model, "名称不能为空");
		if(StringUtils.isBlank(dvalue)) return showJsonError(model, "值不能为空");

		Drama drama = null;
		drama = daoService.getObject(Drama.class, dramaid);
		if(drama == null) return showJsonError(model, "请先保存话剧！"); 
		ChangeEntry changeEntry = new ChangeEntry(drama);
		Map<String, String> otherMap = new HashMap<String, String>();
		otherMap.putAll(VmUtils.readJsonToMap(drama.getDramadata()));
		drama.setDramadata(JsonUtils.addJsonKeyValue(drama.getDramadata(), dname, dvalue));
		daoService.saveObject(drama);
		monitorService.saveChangeLog(getLogonUser().getId(), Drama.class, drama.getId(),changeEntry.getChangeMap( drama));
		return showJsonSuccess(model);
	}
	@RequestMapping("/admin/drama/delDramaProperty.xhtml")
	public String delDramaProperty(ModelMap model, Long dramaid, String dramaData){
		dramaData = StringUtils.trim(dramaData);
		if(dramaid != null){
			Drama drama = daoService.getObject(Drama.class, dramaid);
			drama.setDramadata(JsonUtils.removeJsonKeyValue(drama.getDramadata(), dramaData));
			daoService.updateObject(drama);
		}
		return showJsonSuccess(model);
	}
	@RequestMapping("/admin/drama/saveDrama.xhtml")
	public String saveDrama(Long dramaid, HttpServletRequest request, ModelMap model) {
		Drama drama = null;
		if (dramaid != null){
			drama = daoService.getObject(Drama.class, dramaid);
			drama.setCitycode(drama.getCitycode());
		}else{
			drama = new Drama("");
			drama.setCitycode(getAdminCitycode(request));
		}
		ChangeEntry changeEntry = new ChangeEntry(drama);
		Map<String, String> dataMap = WebUtils.getRequestMap(request); 
		BindUtils.bindData(drama, dataMap);
		if(StringUtils.length(drama.getBriefname())>10){
			return showJsonError(model, "简称长度不能超过10个字！");
		}
		String[] otheritemList = new String[]{Flag.SERVICE_TICKETDESC};
		//验证内容
		Map<String, Object> otherinfoMap = controllerService.getAndSetOtherinfo(drama.getOtherinfo(), Arrays.asList(otheritemList), request);
		String msg=ValidateUtil.validateNewsContent(null,drama.getDramacompany());
		if(StringUtils.isNotBlank(msg))return showJsonError(model,msg);
		msg=ValidateUtil.validateNewsContent(null,drama.getContent());
		if(StringUtils.isNotBlank(msg))return showJsonError(model,msg);
		if(drama.getAddtime()==null) drama.setAddtime(new Timestamp(System.currentTimeMillis()));
		drama.setPinyin(PinYinUtils.getPinyin(drama.getName()));
		drama.setUpdatetime(new Timestamp(System.currentTimeMillis()));
		String actorids = dataMap.get("actorids");
		drama.setActors(actorids);
		String directorids = dataMap.get("directorids");
		drama.setDirector(directorids);
		String troupecompanyids= dataMap.get("troupecompanyids");
		drama.setTroupecompany(troupecompanyids);
		drama.setOtherinfo(JsonUtils.writeObjectToJson(otherinfoMap));
		daoService.saveObject(drama);
		monitorService.saveChangeLog(getLogonUser().getId(), Drama.class, drama.getId(),changeEntry.getChangeMap( drama));
		String idLists="";
		if(StringUtils.isNotBlank(actorids)) idLists=actorids+","+idLists;
		if(StringUtils.isNotBlank(directorids)) idLists=directorids+","+idLists;
		if(StringUtils.isNotBlank(troupecompanyids)) idLists=troupecompanyids+","+idLists;
		if(StringUtils.isNotBlank(idLists)) idLists=idLists.substring(0, idLists.length()-1);
		dramaToStarService.saveDramaToStar(drama.getId(), idLists);
		blogService.addBlogData(getLogonUser().getId(), TagConstant.TAG_DRAMA, drama.getId());
		searchService.pushSearchKey(drama);//更新索引至索引服务器
		return showJsonSuccess(model, ""+drama.getId());
	}
	/**
	 *	Ajax 加载明星库
	 */
	@RequestMapping("/admin/drama/ajaxloadStarTable.xhtml")
	public String ajaxloadStarTable(Long dramaid, String state, String dramatype, String starname, ModelMap model){
		List<DramaStar> starList = dramaStarService.getStarListByStateAndName(state, starname, dramatype,0, 100);
		model.put("starList", starList);
		List<DramaStar> starlist = dramaToStarService.getDramaStarListByDramaid(dramaid, dramatype, 0, 100);
		model.put("selstarMap", BeanUtil.beanListToMap(starlist, "id"));
		model.put("dramaStar", true);
		return "admin/drama/troupecontainer.vm";
	}
}
