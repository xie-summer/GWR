package com.gewara.web.action.admin.common;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import com.gewara.model.common.DataDictionary;
import com.gewara.util.BeanUtil;
import com.gewara.util.BindUtils;
import com.gewara.util.DateUtil;
import com.gewara.web.action.admin.BaseAdminController;
import com.gewara.web.util.PageUtil;

@Controller
public class DataDictionaryAdminController extends BaseAdminController{

	@RequestMapping("/admin/sysmgr/dataDictionaryList.xhtml")
	public String dataDictionaryList(Integer pageNo, String objectName, ModelMap model){
		if(pageNo == null) pageNo = 0;
		int rowsPerPage = 20;
		int firstPerPage = pageNo * rowsPerPage;
		int count = commonService.getDataDictionaryCount(objectName);
		List<DataDictionary> dictionaryList = commonService.getDataDictionaryList(objectName, firstPerPage, rowsPerPage);
		PageUtil pageUtil = new PageUtil(count, rowsPerPage, firstPerPage, "/admin/sysmgr/dataDictionary.xhtml");
		Map params = new HashMap();
		params.put("objectName", objectName);
		pageUtil.initPageInfo(params);
		model.put("pageUtil", pageUtil);
		model.put("dictionaryList", dictionaryList);
		return "admin/sysmgr/dataDictionaryList.vm";
	}
	
	@RequestMapping("/admin/sysmgr/saveDataDictionary.xhtml")
	public String saveOrUpdateDataDictionary(Long id, String objectName, String propertyName, String dataType, HttpServletRequest request, ModelMap model){
		DataDictionary dataDictionary = daoService.getObject(DataDictionary.class, id);
		Map dataMap = request.getParameterMap();
		if(dataDictionary == null){
			dataDictionary = new DataDictionary(objectName, propertyName, dataType);
		}else{
			dataDictionary.setUpdatetime(DateUtil.getCurFullTimestamp());
		}
		BindUtils.bindData(dataDictionary, dataMap);
		if(StringUtils.isBlank(dataDictionary.getObjectName())) return showJsonError(model, "对象名称不能为空！");
		if(StringUtils.isBlank(dataDictionary.getPropertyName())) return showJsonError(model, "属性名称不能为空！");
		if(StringUtils.isBlank(dataDictionary.getPropertyRealName())) return showJsonError(model, "属性中文名称不能为空！");
		if(StringUtils.isBlank(dataDictionary.getDataType())) return showJsonError(model, "数据类型不能为空！");
		daoService.saveObject(dataDictionary);		
		return showJsonSuccess(model);
	}
	
	@RequestMapping("/admin/sysmgr/getDataDictionary.xhtml")
	public String getDataDictionary(Long id, ModelMap model){
		DataDictionary dataDictionary = daoService.getObject(DataDictionary.class, id);
		if(dataDictionary == null) return showJsonError(model, "数据不存在或被删除！");
		return showJsonSuccess(model, BeanUtil.getBeanMap(dataDictionary));
	}
	
	@RequestMapping("/admin/sysmgr/delDataDictionary.xhtml")
	public String delDataDictionary(Long id, ModelMap model){
		DataDictionary dataDictionary = daoService.getObject(DataDictionary.class, id);
		if(dataDictionary == null) return showJsonError(model, "数据不存在或被删除！");
		daoService.removeObject(dataDictionary);
		return showJsonSuccess(model);
	}
}
