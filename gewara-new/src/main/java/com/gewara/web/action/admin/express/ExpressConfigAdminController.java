package com.gewara.web.action.admin.express;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import com.gewara.constant.ticket.OrderExtraConstant;
import com.gewara.model.common.Province;
import com.gewara.model.express.ExpressConfig;
import com.gewara.model.express.ExpressProvince;
import com.gewara.model.express.TicketFaceConfig;
import com.gewara.service.express.ExpressConfigService;
import com.gewara.util.BeanUtil;
import com.gewara.util.ChangeEntry;
import com.gewara.util.DateUtil;
import com.gewara.web.action.admin.BaseAdminController;
import com.gewara.web.util.PageUtil;

@Controller
public class ExpressConfigAdminController extends BaseAdminController {

	@Autowired@Qualifier("expressConfigService")
	private ExpressConfigService expressConfigService;
	
	@RequestMapping("/admin/express/configList.xhtml")
	public String expressConfigList(Integer pageNo, ModelMap model){
		if(pageNo == null) pageNo = 0;
		int rowsPerPage = 30;
		int firstPre = pageNo * rowsPerPage;
		int count = daoService.getObjectCount(ExpressConfig.class);
		List<ExpressConfig> expressConfigList = expressConfigService.getExpressConfigList(firstPre, rowsPerPage);
		PageUtil pageUtil = new PageUtil(count, rowsPerPage, pageNo, "admin/express/configList.xhtml");
		pageUtil.initPageInfo();
		model.put("pageUtil", pageUtil);
		model.put("expressConfigList", expressConfigList);
		model.put("expressTypeMap", OrderExtraConstant.EXPRESS_TYPE_TEXT_MAP);
		return "admin/express/configList.vm";
	}
	
	@RequestMapping("/admin/express/getExpressConfig.xhtml")
	public String getExpressConfig(String id, ModelMap model){
		if(StringUtils.isNotBlank(id)){
			ExpressConfig expressConfig = daoService.getObject(ExpressConfig.class, id);
			model.put("expressConfig", expressConfig);
		}
		model.put("expressTypeMap", OrderExtraConstant.EXPRESS_TYPE_TEXT_MAP);
		return "admin/express/expressConfig.vm";
	}
	
	@RequestMapping("/admin/express/saveExpressConfig.xhtml")
	public String saveExpressConfig(String id, String name, String expresstype, String remark, String update, ModelMap model){
		if(StringUtils.isBlank(id)) return showJsonError(model, "编号不能为空！");
		if(StringUtils.isBlank(name)) return showJsonError(model, "配送方式名称不能为空！");
		if(StringUtils.isBlank(expresstype)) return showJsonError(model, "配送类型不能为空！");
		ExpressConfig expressConfig = null;
		ChangeEntry changeEntry = null;
		if(Boolean.parseBoolean(update)){
			expressConfig = daoService.getObject(ExpressConfig.class, id);
			if(expressConfig == null) return showJsonError(model, "更新有错误，数据不存在或被删除！");
			changeEntry = new ChangeEntry(expressConfig);
			expressConfig.setName(name);
			expressConfig.setUpdatetime(DateUtil.getCurFullTimestamp());
		}else{
			expressConfig = daoService.getObject(ExpressConfig.class, id);
			if(expressConfig != null) return showJsonError(model, "新增有错误，已存在编号为：" + id +"，的配送方式！" );
			expressConfig = new ExpressConfig(id, name, expresstype);
			changeEntry = new ChangeEntry(expressConfig);
		}
		expressConfig.setRemark(remark);
		daoService.saveObject(expressConfig);
		monitorService.saveChangeLog(getLogonUser().getId(), ExpressConfig.class, id, changeEntry.getChangeMap(expressConfig));
		return showJsonSuccess(model);
	}
	
	@RequestMapping("/admin/express/faceConfigList.xhtml")
	public String faceConfigList(Integer pageNo, ModelMap model){
		if(pageNo == null) pageNo = 0;
		int rowsPerPage = 30;
		int firstPre = pageNo * rowsPerPage;
		int count = daoService.getObjectCount(ExpressConfig.class);
		List<TicketFaceConfig> faceConfigList = daoService.queryByRowsRange("from TicketFaceConfig order by addtime desc", firstPre, rowsPerPage);
		PageUtil pageUtil = new PageUtil(count, rowsPerPage, pageNo, "admin/express/faceConfigList.xhtml");
		pageUtil.initPageInfo();
		model.put("pageUtil", pageUtil);
		model.put("faceConfigList", faceConfigList);
		return "admin/express/faceConfigList.vm";
	}
	
	@RequestMapping("/admin/express/getTicketFaceConfig.xhtml")
	public String getTicketFaceConfig(String id, ModelMap model){
		if(StringUtils.isNotBlank(id)){
			TicketFaceConfig faceConfig = daoService.getObject(TicketFaceConfig.class, id);
			model.put("faceConfig", faceConfig);
		}
		return "admin/express/faceConfig.vm";
	}
	
	@RequestMapping("/admin/express/saveTicketFaceConfig.xhtml")
	public String saveTicketFaceConfig(String id, String remark, String facecontent, String update, ModelMap model){
		if(StringUtils.isBlank(id)) return showJsonError(model, "编号不能为空！");
		if(StringUtils.isBlank(facecontent)) return showJsonError(model, "票面模板内容不能为空！");
		TicketFaceConfig faceConfig = null;
		ChangeEntry changeEntry = null;
		if(Boolean.parseBoolean(update)){
			faceConfig = daoService.getObject(TicketFaceConfig.class, id);
			if(faceConfig == null) return showJsonError(model, "更新有错误，数据不存在或被删除！");
			changeEntry = new ChangeEntry(faceConfig);
			faceConfig.setUpdatetime(DateUtil.getCurFullTimestamp());
		}else{
			faceConfig = daoService.getObject(TicketFaceConfig.class, id);
			if(faceConfig != null) return showJsonError(model, "新增有错误，已存在编号为：" + id +"，的票面模板内容！" );
			faceConfig = new TicketFaceConfig(id);
			changeEntry = new ChangeEntry(faceConfig);
		}
		faceConfig.setRemark(remark);
		faceConfig.setFacecontent(facecontent);
		daoService.saveObject(faceConfig);
		monitorService.saveChangeLog(getLogonUser().getId(), TicketFaceConfig.class, id, changeEntry.getChangeMap(faceConfig));
		return showJsonSuccess(model);
	}
	
	@RequestMapping("/admin/express/getExpressProvinceList.xhtml")
	public String ExpressProvinceList(String expressid, ModelMap model){
		if(StringUtils.isBlank(expressid)) return showJsonError(model, "参数错误，不能为空！");
		ExpressConfig expressConfig = daoService.getObject(ExpressConfig.class, expressid);
		if(expressConfig == null) return showJsonError(model, "配送方式不存在或被删除！");
		List<ExpressProvince> expressProvinceList = daoService.getObjectListByField(ExpressProvince.class, "expressid", expressid);
		model.put("expressProvinceList", expressProvinceList);
		model.put("expressConfig", expressConfig);
		return "admin/express/expressProvinceList.vm";
	}
	
	@RequestMapping("/admin/express/getExpressProvince.xhtml")
	public String getExpressProvince(Long id, String expressid, ModelMap model){
		if(id != null){
			ExpressProvince expressProvince = daoService.getObject(ExpressProvince.class, id);
			model.put("expressProvince", expressProvince);
		}else{
			if(StringUtils.isBlank(expressid)) return show404(model, "配送方式编号不能为空！");
			ExpressConfig expressConfig = daoService.getObject(ExpressConfig.class, expressid);
			if(expressConfig == null) return show404(model, "配送方式编号为" + expressid +"不存在！");
			List<Province> provinceList = daoService.getAllObjects(Province.class);
			model.put("provinceList", provinceList);
		}
		return "admin/express/expressProvince.vm";
	}
	
	@RequestMapping("/admin/express/saveExpressProvince.xhtml")
	public String saveExpressProvince(Long id, String name, Integer expressfee, Integer freelimit, String provice, String expressid, ModelMap model){
		if(StringUtils.isBlank(name)) return showJsonError(model, "配送区域名称不能为空！");
		if(expressfee == null || expressfee <0) return showJsonError(model, "寄件费不能为空或不能小于0！");
		if(freelimit == null || freelimit < 0) return showJsonError(model, "免费额度不能为空或不能小于0！");
		if(StringUtils.isBlank(expressid)) return showJsonError(model, "配送方式编号不能为空！");
		ExpressProvince expressProvince = null;
		if(id != null){
			expressProvince = daoService.getObject(ExpressProvince.class, id);
			if(expressProvince == null) return showJsonError(model, "配送区域不存在或被删除！");
			ChangeEntry changeEntry = new ChangeEntry(expressProvince);
			expressProvince.setName(name);
			expressProvince.setExpressfee(expressfee);
			expressProvince.setFreelimit(freelimit);
			expressProvince.setUpdatetime(DateUtil.getCurFullTimestamp());
			daoService.saveObject(expressProvince);
			monitorService.saveChangeLog(getLogonUser().getId(), ExpressConfig.class, id, changeEntry.getChangeMap(expressProvince));
		}else{
			ExpressConfig expressConfig = daoService.getObject(ExpressConfig.class, expressid);
			if(expressConfig == null) return showJsonError(model, "配送方式编号为："+ expressid +",数据不存在或被删除！");
			if(StringUtils.isBlank(provice)) return showJsonError(model, "配送区域不能为空！");
			List<String> proviceList = Arrays.asList(StringUtils.split(provice, ","));
			if(CollectionUtils.isEmpty(proviceList)) return showJsonError(model, "配送区域不能为空！");
			List<ExpressProvince> expressProvinceList = expressConfigService.getExpressList(expressid, proviceList);
			if(!expressProvinceList.isEmpty()) return showJsonError(model, "该配送方式已存以下配送区域：" + StringUtils.join(BeanUtil.getBeanPropertyList(expressProvinceList, "provincename", true), ","));
			for (String provincecode : proviceList) {
				Province province = daoService.getObject(Province.class, provincecode);
				if(province == null) return showJsonError(model, "配送省份编号为：" + province + "不存在！");
				expressProvince = new ExpressProvince(provincecode, expressid, expressfee, freelimit);
				expressProvince.setName(name);
				expressProvince.setProvincename(province.getProvincename());
				expressProvinceList.add(expressProvince);
			}
			daoService.saveObjectList(expressProvinceList);
		}
		return showJsonSuccess(model);
	}
	@RequestMapping("/admin/express/delExpressProvince.xhtml")
	public String delExpressProvince(Long id, ModelMap model){
		ExpressProvince expressProvince = daoService.getObject(ExpressProvince.class, id);
		if(expressProvince == null) return showJsonError(model, "配送区域不存在或被删除！");
		daoService.removeObject(expressProvince);
		monitorService.saveDelLog(getLogonUser().getId(), id, expressProvince);
		return showJsonSuccess(model);
	}
}
