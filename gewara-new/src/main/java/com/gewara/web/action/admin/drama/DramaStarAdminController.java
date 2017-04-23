package com.gewara.web.action.admin.drama;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.type.TypeReference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import com.gewara.constant.TagConstant;
import com.gewara.model.drama.Drama;
import com.gewara.model.drama.DramaStar;
import com.gewara.service.drama.DramaService;
import com.gewara.service.drama.DramaStarService;
import com.gewara.support.ServiceHelper;
import com.gewara.untrans.SearchService;
import com.gewara.util.BeanUtil;
import com.gewara.util.BindUtils;
import com.gewara.util.ChangeEntry;
import com.gewara.util.JsonUtils;
import com.gewara.util.PinYinUtils;
import com.gewara.util.ValidateUtil;
import com.gewara.web.action.admin.BaseAdminController;
import com.gewara.web.util.PageUtil;

/**
 *    @function 后台 明星管理 
 * 	@author bob.hu
 *		@date	2010-12-01 15:36:50
 */
@Controller
public class DramaStarAdminController extends BaseAdminController {
	
	@Autowired@Qualifier("dramaStarService")
	private DramaStarService dramaStarService;
	public void setDramaStarService(DramaStarService dramaStarService) {
		this.dramaStarService = dramaStarService;
	}
	@Autowired@Qualifier("dramaService")
	private DramaService dramaService;
	public void setDramaService(DramaService dramaService){
		this.dramaService = dramaService;
	}
	
	@Autowired@Qualifier("searchService")
	private SearchService searchService;
	public void setSearchService(SearchService searchService) {
		this.searchService = searchService;
	}
	
	@RequestMapping("/admin/drama/dramaStarList.xhtml")
	public String dramaStarList(String type, String name, String startype, Integer pageNo, ModelMap model) {
		if (pageNo == null) pageNo = 0;
		int rowsPerpage = 25;
		int firstRow = pageNo * rowsPerpage;
		List<DramaStar> starList = dramaStarService.getStarListByName(type, name, startype, firstRow, rowsPerpage);
		int starListcount =  dramaStarService.getStarCountByName(type, name, startype);
		PageUtil pageUtil = new PageUtil(starListcount, rowsPerpage, pageNo, "admin/drama/dramaStarList.xhtml", false, true);
		Map params = new HashMap();
		params.put("name", name);
		params.put("startype", startype);
		pageUtil.initPageInfo(params);
		model.put("pageUtil", pageUtil);
		
		model.put("starList", starList);
		Map<Long, DramaStar> troupeMap = daoService.getObjectMap(DramaStar.class, BeanUtil.getBeanPropertyList(starList, Long.class, "troupe", true));
		model.put("troupeMap", troupeMap);
		return "admin/drama/star/starList.vm";
	}
	@RequestMapping("/admin/drama/modifyStarDetail.xhtml")
	public String modifyStarDetail(Long starid, String type, ModelMap model) {
		DramaStar dramaStar = null;
		if (starid != null){
			dramaStar = daoService.getObject(DramaStar.class, starid);
			List<Map<String,String>> dramaMapList = JsonUtils.readJsonToObject(new TypeReference<List<Map<String, String>>>(){}, dramaStar.getRepresentativeRelate());
			String dramaids = "";
			String dramas = "";
			if(dramaMapList!=null){
				for (Map<String, String> map : dramaMapList) {
					dramaids +="," + map.get("id");
					dramas += ","  + map.get("name");
				}
				model.put("dramaids", dramaids.substring(1));
				model.put("dramas", dramas.substring(1));
			}
		}
		if(StringUtils.isBlank(type)) type = TagConstant.TAG_DRAMA;
		model.put("type", type);
		model.put("dramaStar", dramaStar);
		return "admin/drama/star/starForm.vm";
	}
	@RequestMapping("/admin/drama/saveStar.xhtml")
	public String saveStar(Long starid, String name, HttpServletRequest request, ModelMap model) {
		DramaStar dramaStar = new DramaStar(name);
		if (starid != null){
			dramaStar = daoService.getObject(DramaStar.class, starid);
		}
		ChangeEntry changeEntry = new ChangeEntry(dramaStar);

		Map<String, String[]> dataMap = request.getParameterMap();
		if(dataMap.get("startype") == null) return showJsonError(model, "类型不能为空！");
		BindUtils.bindData(dramaStar, dataMap);
		//验证内容
		String msg=ValidateUtil.validateNewsContent(null,dramaStar.getContent());
		if(StringUtils.isNotBlank(msg)) return showJsonError(model, msg);
		dramaStar.setPinyin(PinYinUtils.getPinyin(dramaStar.getName()));
		String dramaids = ServiceHelper.get(dataMap, "dramaids");
		if(StringUtils.isNotBlank(dramaids)){
			List<Map<String,String>> dataList = new ArrayList<Map<String,String>>();
			List<String> idList= Arrays.asList(dramaids.split(","));
			for (String string : idList) {
				if(StringUtils.isNotBlank(string)){
					Drama drama = daoService.getObject(Drama.class, Long.valueOf(string));
					if(drama != null){
						Map<String,String> dataMap2 = new HashMap<String, String>();
						dataMap2.put("id", string);
						dataMap2.put("name", drama.getName());
						dataList.add(dataMap2);
					}
				}
			}
			dramaStar.setRepresentativeRelate(JsonUtils.writeObjectToJson(dataList));
		}
		if(StringUtils.isBlank(dramaStar.getStartype())) return showJsonError(model, "请选择类型！");
		else{
			if(StringUtils.contains(dramaStar.getStartype(), DramaStar.TYPE_TROUPE)&&(StringUtils.contains(dramaStar.getStartype(), DramaStar.TYPE_STAR)||StringUtils.contains(dramaStar.getStartype(), DramaStar.TYPE_DIRECTOR))){
				return showJsonError(model, "选择了剧团就不能关联其它类型！");
			}
		}
		dramaStar.setUpdatetime(new Timestamp(System.currentTimeMillis()));
		daoService.saveObject(dramaStar);
		monitorService.saveChangeLog(getLogonUser().getId(), DramaStar.class, dramaStar.getId(),changeEntry.getChangeMap( dramaStar));
		searchService.pushSearchKey(dramaStar);//更新索引至索引服务器
		return showJsonSuccess(model, ""+dramaStar.getId());
	}
	
	// 明星关联剧团
	@RequestMapping("/admin/drama/attachTroupe.xhtml")
	public String attachTroupe(Long starid, ModelMap model){
		DramaStar dramaStar = daoService.getObject(DramaStar.class, starid);
		if(DramaStar.TYPE_TROUPE.equals(dramaStar.getStartype())){
			return showJsonError(model, "该记录已是剧团, 无需关联!");
		}
		model.put("dramaStar", dramaStar);
		if(dramaStar.getTroupe() != 0){
			// 有关联剧团 - 目前支持1对1关系
			DramaStar troupe = daoService.getObject(DramaStar.class, dramaStar.getTroupe());
			model.put("troupe", troupe);
		}
		return "admin/drama/star/attachtroupe.vm";
	}
	
	@RequestMapping("/admin/drama/saveAttachTroupe.xhtml")
	public String saveAttachTroupe(Long starid, Long troupeid, ModelMap model){
		DramaStar dramaStar = daoService.getObject(DramaStar.class, starid);
		DramaStar dramaTroupe = daoService.getObject(DramaStar.class, troupeid);
		if(dramaTroupe == null){
			return showJsonError(model, "剧团不存在!");
		}
		dramaStar.setTroupe(troupeid);
		int starnum = dramaStarService.getStarCountByTroupid(troupeid);
		starnum += 1;
		dramaTroupe.setStarnum(starnum);
		daoService.saveObjectList(dramaStar, dramaTroupe);
		return showJsonSuccess(model);
	}
	
	// 取消剧团关联
	@RequestMapping("/admin/drama/delAttachTroupe.xhtml")
	public String delAttachTroupe(Long starid, Long troupeid, ModelMap model){
		DramaStar dramaStar = daoService.getObject(DramaStar.class, starid);
		DramaStar dramaTroupe = daoService.getObject(DramaStar.class, troupeid);
		if(dramaTroupe == null){
			return showJsonError(model, "剧团不存在!");
		}
		dramaStar.setTroupe(0L);
		int starnum = dramaStarService.getStarCountByTroupid(troupeid);
		starnum = (starnum -1 < 0? 0 : starnum-1);
		dramaTroupe.setStarnum(starnum);
		daoService.saveObjectList(dramaStar, dramaTroupe);
		return showJsonSuccess(model);
	}
	
	@RequestMapping("/admin/drama/star/loadDramaTable.xhtml")
	public String loadDramaTable(Long starid, String name, ModelMap model, HttpServletRequest request){
		if(StringUtils.isBlank(name)) return showJsonError(model, "查询名称不能为空！"); 
		String citycode = getAdminCitycode(request);
		DramaStar dramaStar = daoService.getObject(DramaStar.class, starid);
		if(dramaStar != null){
			model.put("dramaStar", dramaStar);
			List<Map<String,String>> dramaMapList = JsonUtils.readJsonToObject(new TypeReference<List<Map<String, String>>>(){}, dramaStar.getRepresentativeRelate());
			List<Long> dramaIdList = new ArrayList<Long>();
			if(dramaMapList != null){
				for (Map<String, String> map : dramaMapList) {
					dramaIdList.add(Long.valueOf(map.get("id")));
				}
				model.put("dramaIdList", dramaIdList);
			}
		}
		List<Drama> dramaList = dramaService.getDramaListByName(citycode, name, 0, 100);
		model.put("dramaList", dramaList);
		return "admin/drama/star/troupecontainer.vm";
	}
}
