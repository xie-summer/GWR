package com.gewara.web.action.common;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import com.gewara.constant.sys.JsonDataKey;
import com.gewara.model.bbs.DiaryBase;
import com.gewara.model.common.JsonData;
import com.gewara.service.JsonDataService;
import com.gewara.service.bbs.BlogService;
import com.gewara.service.bbs.DiaryService;
import com.gewara.util.VmUtils;
import com.gewara.web.action.admin.BaseAdminController;

@Controller
public class HelpCenterController extends BaseAdminController{
	@Autowired@Qualifier("jsonDataService")
	private JsonDataService jsonDataService;
	public void setJsonDataService(JsonDataService jsonDataService) {
		this.jsonDataService = jsonDataService;
	}
	@Autowired@Qualifier("diaryService")
	private DiaryService diaryService;
	public void setDiaryService(DiaryService diaryService) {
		this.diaryService = diaryService;
	}
	@Autowired@Qualifier("blogService")
	private BlogService blogService;
	public void setBlogService(BlogService blogService) {
		this.blogService = blogService;
	}
	@RequestMapping("/helpCenter/index.dhtml")
	public String helpCenter(String dkey, String diaryId, ModelMap model){
		Map<String, List<JsonData>> subdataMap = new HashMap<String, List<JsonData>>();
		// 取出1级大类
		List<JsonData> mainMenuList = jsonDataService.getListByTag(JsonDataKey.TAG_HELPCENTER, null, "tag", false, 0, 20);
		// 取出1级对应的子级
		for (JsonData jsonData : mainMenuList) {
			List<JsonData> subList = jsonDataService.getListByTag(jsonData.getDkey(), null, "tag", false, 0, 20);
			subdataMap.put(jsonData.getDkey(), subList);
		}
		model.put("subdataMap", subdataMap);
		model.put("mainMenuList", mainMenuList);
		if(StringUtils.isBlank(diaryId)){
			model.put("isIndex", true);
			return "footer/helpMap.vm";
		}
		if(StringUtils.isBlank(dkey)) return show404(model, "dkey 不能为空...");
		JsonData json = daoService.getObject(JsonData.class, dkey);
		if(json == null) return show404(model, "该菜单项不存在...");
		String relatedid = VmUtils.readJsonToMap(json.getData()).get("relatedid");
		if(StringUtils.equals(diaryId, relatedid)){
			Long relateid = Long.valueOf(relatedid);
			DiaryBase diary = diaryService.getDiaryBase(relateid);
			if(diary == null){
				return show404(model, "该贴子不存在...");
			}
			model.put("diaryBody", blogService.getDiaryBody(diary.getId()));
			model.put("diary", diary);
		}else return show404(model, "该贴子不存在...");
		model.put("jsonData", json);
		return "footer/helpMap.vm";
	}
}
