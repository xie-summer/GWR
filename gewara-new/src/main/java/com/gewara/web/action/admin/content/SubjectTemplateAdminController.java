package com.gewara.web.action.admin.content;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import com.gewara.constant.content.SignName;
import com.gewara.constant.sys.MongoData;
import com.gewara.model.content.GewaCommend;
import com.gewara.model.movie.SpecialActivity;
import com.gewara.mongo.MongoService;
import com.gewara.support.ServiceHelper;
import com.gewara.util.BeanUtil;
import com.gewara.util.BindUtils;
import com.gewara.util.WebUtils;
import com.gewara.web.action.admin.BaseAdminController;
/**
 * 专题模板
 * 后台
 * @author hubo
 *
 */
@Controller
public class SubjectTemplateAdminController extends BaseAdminController {

	@Autowired@Qualifier("mongoService")
	private MongoService mongoService;
	public void setMongoService(MongoService mongoService) {
		this.mongoService = mongoService;
	}
	
	@RequestMapping("/admin/common/index.xhtml")
	public String templatesList(){
		return "admin/common/table.vm";
	}
	
	/***
	 *  新建模板
	 */
	@RequestMapping("/admin/common/addSpeciaTemplate.xhtml")
	public String addSpeciaTemplate(){
		return "admin/common/addspeciaTpl.vm";
	}
	/***
	 *  模板基本信息保存
	 */
	@RequestMapping("/admin/common/saveSpeciaTemplate.xhtml")
	public String saveSpeciaTemplate(ModelMap model, HttpServletRequest request){
		Map<String, String[]> paramMap = request.getParameterMap();
		SpecialActivity specialActivity = new SpecialActivity("");
		String id = ServiceHelper.get(paramMap, "id");
		if (StringUtils.isNotBlank(id)){
			specialActivity = daoService.getObject(SpecialActivity.class, new Long(id));
		}
		BindUtils.bindData(specialActivity, paramMap);
		daoService.saveObject(specialActivity);
		return showJsonSuccess(model, BeanUtil.getBeanMap(specialActivity, false));
	}
	
	@RequestMapping("/admin/common/newsaveSpeciaTemplate.xhtml")
	public String newsaveSpeciaTemplate(String id, ModelMap model){
		Map saveMap = mongoService.findOne(MongoData.NS_MAINSUBJECT, MongoData.DEFAULT_ID_NAME, id);
		SpecialActivity specialActivity = new SpecialActivity("");
		specialActivity.setTeampictitle(saveMap.get(MongoData.ACTION_TITLE)+"");
		//specialActivity.setWebsite(website);
		return showJsonSuccess(model, BeanUtil.getBeanMap(specialActivity, false));
	}
	
	/**
	 *  图片信息保存
	 */
	@RequestMapping("/admin/common/savePic.xhtml")
	public String savePic(ModelMap model, Long cid, Long bid, String link, String note, Integer ordernum, String picpath, String flag, HttpServletRequest request, HttpServletResponse response){
		if(bid == null){
			return showJsonError_DATAERROR(model);
		}
		GewaCommend gewaCommend = null;
		if(cid == null){
			gewaCommend = new GewaCommend("");
		}else{
			gewaCommend = daoService.getObject(GewaCommend.class, cid);
		}
		gewaCommend.setLink(link);
		gewaCommend.setLogo(picpath);
		gewaCommend.setTitle(note);
		gewaCommend.setRelatedid(bid);
		gewaCommend.setOrdernum(ordernum);
		gewaCommend.setCitycode(WebUtils.getAndSetDefault(request, response));
		
		if("logo".equals(flag)){
			gewaCommend.setSignname(SignName.TPL_LOGO);
		}else if("headpic".equals(flag)){
			gewaCommend.setSignname(SignName.TPL_HEADPIC);
		}else if("blogpic".equals(flag)){
			gewaCommend.setSignname(SignName.TPL_BLOGPIC);
		}else{
			gewaCommend.setSignname(SignName.TPL_TEAMPIC);
			gewaCommend.setParentid(bid);
		}
		gewaCommend = daoService.saveObject(gewaCommend);
		
		// 图片保存完成之后更新 专题表, 图片组 不用更新
		SpecialActivity specialActivity = daoService.getObject(SpecialActivity.class, bid);
		if("logo".equals(flag)){
			specialActivity.setLogo(gewaCommend.getId());
			daoService.saveObject(specialActivity);
		}else	if("headpic".equals(flag)){
			specialActivity.setHeadpic(gewaCommend.getId());
			daoService.saveObject(specialActivity);
		}else if("blogpic".equals(flag)){
			specialActivity.setBlogpic(gewaCommend.getId());
			daoService.saveObject(specialActivity);
		}
		return showJsonSuccess(model, gewaCommend.getId().toString());
	}
	
	
	/**
	 *  更多信息保存
	 */
	@RequestMapping("/admin/common/saveMore.xhtml")
	public String saveMore(ModelMap model, Long bid, String walatitle, String acttitle, 
			String surveytitle, String answertitle, String blogtitle, String teampictitle){
		if(bid == null){
			return showJsonError_DATAERROR(model);
		}
		SpecialActivity specialActivity = daoService.getObject(SpecialActivity.class, bid);
		specialActivity.setWalatitle(walatitle);
		specialActivity.setActtitle(acttitle);
		specialActivity.setSurveytitle(surveytitle);
		specialActivity.setAnswertitle(answertitle);
		specialActivity.setBlogtitle(blogtitle);
		specialActivity.setTeampictitle(teampictitle);
		
		daoService.saveObject(specialActivity);
		return showJsonSuccess(model);
	}
	
	
	/***
	 *  修改前加载
	 */
	@RequestMapping("/admin/common/loadSpecialTpl.xhtml")
	public String loadSpecialTpl(ModelMap model, HttpServletRequest request, Long tid){
		if(tid == null){
			return showJsonError_DATAERROR(model);
		}
		
		SpecialActivity specialActivity = daoService.getObject(SpecialActivity.class, tid);
		if(specialActivity == null){
			return showJsonError_DATAERROR(model);
		}
		model.put("specialActivity", specialActivity);
		
		// 封面图片 + 头图与论坛图片
		Long logoid = specialActivity.getLogo();
		if(logoid != null){
			GewaCommend gewaCommend_logo = daoService.getObject(GewaCommend.class, logoid);
			model.put("gewaCommend_logo", gewaCommend_logo);
		}
		Long headid = specialActivity.getHeadpic();
		if(headid != null){
			GewaCommend gewaCommend_head = daoService.getObject(GewaCommend.class, headid);
			model.put("gewaCommend_head", gewaCommend_head);
		}
		Long blogid = specialActivity.getBlogpic();
		if(blogid != null){
			GewaCommend gewaCommend_blog = daoService.getObject(GewaCommend.class, blogid);
			model.put("gewaCommend_blog", gewaCommend_blog);
		}
		
		// 组图
		List<GewaCommend> teampicList = new ArrayList<GewaCommend>();
		teampicList = commonService.getGewaCommendList(getAdminCitycode(request), SignName.TPL_TEAMPIC, specialActivity.getId(),null, false,0, 200);
		model.put("teampicList", teampicList);
		
		return "admin/common/addspeciaTpl.vm";
	}
	
	
	/***
	 *  组图删除(GewaCommend 直接删除, 同时删除服务器上传图片)
	 */
	@RequestMapping("/admin/common/delTeampicSingle.xhtml")
	public String delTeampicSingle(ModelMap model, Long cid){
		GewaCommend gewaCommend = daoService.getObject(GewaCommend.class, cid);
		if(gewaCommend != null){
			daoService.removeObjectById(GewaCommend.class, cid);
			monitorService.saveDelLog(getLogonUser().getId(), cid, gewaCommend);
		}
		return showJsonSuccess(model);
	}
	
	/***
	 *  前台展示 - SubjectTemplateController.templatesShow()
	 */
	
}
