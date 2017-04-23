package com.gewara.web.action.admin.content;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import com.gewara.constant.TagConstant;
import com.gewara.constant.sys.MongoData;
import com.gewara.helper.sys.RelateClassHelper;
import com.gewara.model.BaseObject;
import com.gewara.model.content.Link;
import com.gewara.model.user.UserMessage;
import com.gewara.model.user.UserMessageAction;
import com.gewara.mongo.MongoService;
import com.gewara.service.bbs.UserMessageService;
import com.gewara.support.ErrorCode;
import com.gewara.untrans.GewaPicService;
import com.gewara.untrans.sitemap.SitemapGenService;
import com.gewara.util.BeanUtil;
import com.gewara.util.BindUtils;
import com.gewara.util.ValidateUtil;
import com.gewara.web.action.admin.BaseAdminController;
import com.gewara.web.support.ResponseStreamWriter;
import com.gewara.web.util.PageUtil;
/**
 * 站点管理模块
 * @author acerge(acerge@163.com)
 * @since 7:08:32 PM Oct 17, 2011
 */
@Controller
public class SiteAdminController extends BaseAdminController {
	@Autowired@Qualifier("mongoService")
	private MongoService mongoService;
	public void setMongoService(MongoService mongoService) {
		this.mongoService = mongoService;
	}
	@Autowired@Qualifier("userMessageService")
	private UserMessageService userMessageService;
	public void setUserMessageService(UserMessageService userMessageService) {
		this.userMessageService = userMessageService;
	}
	@Autowired@Qualifier("gewaPicService")
	private GewaPicService gewaPicService;
	public void setGewaPicService(GewaPicService gewaPicService) {
		this.gewaPicService = gewaPicService;
	}
	@Autowired@Qualifier("gewaSitemapGenService")
	private SitemapGenService gewaSitemapGenService;
	@Autowired@Qualifier("blogSitemapGenService")
	private SitemapGenService blogSitemapGenService;
	@Autowired@Qualifier("sportSitemapGenService")
	private SitemapGenService sportSitemapGenService;
	@Autowired@Qualifier("qaSitemapGenService")
	private SitemapGenService qaSitemapGenService;
	@Autowired@Qualifier("everyDaySitemapGenService")
	private SitemapGenService everyDaySitemapGenService;
	@Autowired@Qualifier("commuSitemapGenService")
	private SitemapGenService commuSitemapGenService;
	@Autowired@Qualifier("newsSitemapGenService")
	private SitemapGenService newsSitemapGenService;
	@Autowired@Qualifier("dramaSitemapGenService")
	private SitemapGenService dramaSitemapGenService;
	
	public void setBlogSitemapGenService(SitemapGenService blogSitemapGenService) {
		this.blogSitemapGenService = blogSitemapGenService;
	}

	public void setSportSitemapGenService(SitemapGenService sportSitemapGenService) {
		this.sportSitemapGenService = sportSitemapGenService;
	}
	public void setEveryDaySitemapGenService(SitemapGenService everyDaySitemapGenService) {
		this.everyDaySitemapGenService = everyDaySitemapGenService;
	}
	public void setQaSitemapGenService(SitemapGenService qaSitemapGenService) {
		this.qaSitemapGenService = qaSitemapGenService;
	}
	public void setCommuSitemapGenService(SitemapGenService commuSitemapGenService) {
		this.commuSitemapGenService = commuSitemapGenService;
	}
	public void setNewsSitemapGenService(SitemapGenService newsSitemapGenService) {
		this.newsSitemapGenService = newsSitemapGenService;
	}
	public void setDramaSitemapGenService(SitemapGenService dramaSitemapGenService) {
		this.dramaSitemapGenService = dramaSitemapGenService;
	}
	
	@RequestMapping("/admin/site/genSitemap.xhtml")
	public String genSitemap(String[] genType, ModelMap model){
		List<String> msgList = new ArrayList<String>();
		if(genType ==null || genType.length==0){
			msgList.add("必须选择要生成的类型！");
			return "admin/site/genSitemap.vm";   		
		}
		List<String> typeList = Arrays.asList(genType);
		if(typeList.contains("place")){
			gewaSitemapGenService.genSitemap();
			msgList.add("生成场所站点地图");
		}
		if(typeList.contains("blog")){
			blogSitemapGenService.genSitemap();
			msgList.add("生成论坛站点地图!");
		}
		if(typeList.contains("sport")){
			sportSitemapGenService.genSitemap();
			msgList.add("生成运动模块站点地图");
		}
		if(typeList.contains("qa")){
			qaSitemapGenService.genSitemap();
			msgList.add("生成QA模块站点地图");
		}
		if(typeList.contains("commu")){
			commuSitemapGenService.genSitemap();
			msgList.add("生成圈子模块站点地图");
		}
		if(typeList.contains("xinwen")){
			newsSitemapGenService.genSitemap();
			msgList.add("生成新闻模块站点地图");
		}
		if(typeList.contains("drama")){
			dramaSitemapGenService.genSitemap();
			msgList.add("生成话剧模块站点地图");
		}
		if(typeList.contains("new")){
			everyDaySitemapGenService.genSitemap();
			msgList.add("生成每日新地址站点地图");
		}
		return showMessage(model, msgList);
	}
	@RequestMapping("/getSitemap.xhtml")
	public void getSiteMap(HttpServletResponse res, String name){
		try {
			gewaPicService.getFileFromRemote(new ResponseStreamWriter(res, name, "application/xml"), name);
		} catch (IOException e) {
			dbLogger.error("", e);
		}
	}
	@RequestMapping("/admin/site/seoList.xhtml")
	public String seoList(String tag,Integer pageNo,String name,ModelMap model){
		if(StringUtils.isBlank(tag)) return "admin/site/seoList.vm";
		if(pageNo==null) pageNo=0;
		Integer count = Integer.valueOf(hibernateTemplate.findByCriteria(
				this.getSeoQuery(tag,name).setProjection(Projections.rowCount())).get(0)+"");
		PageUtil pageUtil = new PageUtil(count, 15, pageNo, "admin/seo/seoList.xhtml");
		Map params = new HashMap();
		params.put("name", name);
		params.put("tag", tag);
		pageUtil.initPageInfo(params);
		List list = hibernateTemplate.findByCriteria(getSeoQuery(tag,name),pageNo*15,15);
		model.put("pageUtil", pageUtil);
		model.put("list", list);
		return "admin/site/seoList.vm";
	}
	@RequestMapping("/admin/site/getSeo.xhtml")
	public String getSeo(Long id, String tag, ModelMap model) {
		Class clazz = RelateClassHelper.getRelateClazz(tag);
		BaseObject obj = daoService.getObject(clazz, id);
		Map result = BeanUtil.getBeanMap(obj);
		return showJsonSuccess(model, result);
	}

	@RequestMapping("/admin/site/saveSeo.xhtml")
	public String saveSeo(HttpServletRequest request, ModelMap model) {
		Map<String, String[]> seoMap = request.getParameterMap();
		Class clazz = RelateClassHelper.getRelateClazz(seoMap.get("tag")[0]);
		BaseObject obj = daoService.getObject(clazz, new Long(seoMap.get("id")[0]));
		BindUtils.bindData(obj, seoMap);
		daoService.saveObject(obj);
		return showJsonSuccess(model);
	}
	private DetachedCriteria getSeoQuery(String tag,String name){
		Class clazz = RelateClassHelper.getRelateClazz(tag);
		if(clazz!=null){
			DetachedCriteria query = DetachedCriteria.forClass(clazz);
			if(StringUtils.isNotBlank(name)){
				if("gymcourse".equals(tag))
					query.add(Restrictions.ilike("coursename", name, MatchMode.ANYWHERE));
				else if("sportservice".equals(tag))
					query.add(Restrictions.ilike("itemname", name, MatchMode.ANYWHERE));
				else if("activity".equals(tag)) {
					query.add(Restrictions.ilike("title", name, MatchMode.ANYWHERE));
					query.add(Restrictions.or(Restrictions.ge("startdate", new Date()), Restrictions.ge("enddate", new Date())));
				}else if("movie".equals(tag))
					query.add(Restrictions.ilike("moviename", name, MatchMode.ANYWHERE));
				else
					query.add(Restrictions.ilike("name", name, MatchMode.ANYWHERE));
			}else {
				if("activity".equals(tag)) {
					query.add(Restrictions.or(Restrictions.ge("startdate", new Date()), Restrictions.ge("enddate", new Date())));
				}
			}
			query.addOrder(Order.asc("id"));
			return query;
		}
		return null;
	}
	//友情链接数据
	@RequestMapping("/admin/site/linkList.xhtml")
	public String linkList(ModelMap model){
		List<Link> pictureLinkList = commonService.getLinkListByType(Link.TYPE_PICTURE);
		List<Link> textLinkList = commonService.getLinkListByType(Link.TYPE_TEXT);
		model.put("pictureLinkList", pictureLinkList);
		model.put("textLinkList", textLinkList);
		return "admin/site/linkList.vm";
	}
	@RequestMapping("/admin/site/saveLink.xhtml")
	public String saveLink(Long id, HttpServletRequest request, ModelMap model) {
		Link link = new Link("");
		if (id != null) {
			link = daoService.getObject(Link.class, id);
		}
		BindUtils.bindData(link, request.getParameterMap());
		daoService.saveObject(link);
		return showJsonSuccess(model);
	}

	@RequestMapping("/admin/site/getLink.xhtml")
	public String getLink(Long id, ModelMap model) {
		Link link = daoService.getObject(Link.class, id);
		Map result = BeanUtil.getBeanMap(link);
		return showJsonSuccess(model, result);
	}

	@RequestMapping("/admin/site/removeLink.xhtml")
	public String removeLink(Long id, ModelMap model) {
		Link link = daoService.getObject(Link.class, id);
		daoService.removeObject(link);
		return showJsonSuccess(model);
	}

	@RequestMapping("/admin/site/updateLink.xhtml")
	public String updateLink(Long id, ModelMap model) {
		Link link = daoService.getObject(Link.class, id);
		link.setUpdatetime(new Timestamp(System.currentTimeMillis()));
		daoService.saveObject(link);
		return showJsonSuccess(model);
	}
	
	@RequestMapping("/admin/site/jobList.xhtml")
	public String jobList(ModelMap model){
		List<Map> jobMapList = mongoService.getMapList(MongoData.NS_JOB_NAMESPACE);
		if(jobMapList!=null) {
			Comparator<Map> comparator = new Comparator<Map>() {
				 public int compare(Map o1, Map o2) {
						return o2.get("jobid").toString().compareTo(o1.get("jobid").toString());
				 }
			};
			Collections.sort(jobMapList, comparator);
		}
		model.put("jobMapList", jobMapList);
		return "admin/site/jobList.vm";
	}
	@RequestMapping("/admin/site/modJob.xhtml")
	public String modJob(String id, ModelMap model){
		if(StringUtils.isNotBlank(id)) {
			Map jobMap = mongoService.getMap(MongoData.JOB_IDNAME, MongoData.NS_JOB_NAMESPACE, id);
			if(jobMap!=null){
				model.put("title", jobMap.get(MongoData.JOB_TITLE));
				model.put("description", jobMap.get(MongoData.JOB_DESC));
			}
		}
		return "admin/site/jobDetail.vm";
	}
	@RequestMapping("/admin/site/saveJob.xhtml")
	public String saveJob(String id, String title, String description, ModelMap model){
		Map toSave = new HashMap();
		if(StringUtils.isBlank(id)){
			id = ""+System.currentTimeMillis();
		}
		String msg=ValidateUtil.validateNewsContent(null, description);
		if(StringUtils.isNotBlank(msg))return showJsonError(model, msg);
		toSave.put(MongoData.SYSTEM_ID, id);
		toSave.put(MongoData.JOB_IDNAME, id);
		toSave.put(MongoData.JOB_TITLE, title);
		toSave.put(MongoData.JOB_DESC, description);
		mongoService.saveOrUpdateMap(toSave, MongoData.JOB_IDNAME, MongoData.NS_JOB_NAMESPACE, false, true);
		return showJsonSuccess(model, "添加信息成功");
	}
	@RequestMapping("/admin/site/delJob.xhtml")
	public String saveJob(String jobid,  ModelMap model){
		boolean isSuccess = mongoService.removeObjectById(MongoData.NS_JOB_NAMESPACE, MongoData.JOB_IDNAME, jobid);
		if(!isSuccess) return showJsonError(model, "删除失败");
		return showJsonSuccess(model);
	}
	/**
	 *	author: 	bob
	 *	date:		20100727 
	 *	网站公告列表
	 */
	@RequestMapping("/admin/site/batchPM.xhtml")
	public String batchPM(Integer pageNo, ModelMap model){
		final Integer rowsPerPage = 15;
		if(pageNo==null) pageNo=0;
		Integer pmCount = userMessageService.countMessagesByMemIdAndStatus(TagConstant.ADMIN_FROMMEMBERID, TagConstant.STATUS_TOALL);
		List<UserMessageAction> pmList = userMessageService.getMessagesByMemIdAndStatus(TagConstant.ADMIN_FROMMEMBERID, TagConstant.STATUS_TOALL, TagConstant.READ_STATUS_ALL, pageNo*rowsPerPage, rowsPerPage); 
		PageUtil pageUtil = new PageUtil(pmCount, rowsPerPage, pageNo, "admin/message/batchPM.xhtml");
		pageUtil.initPageInfo();
		model.put("pmList", pmList);
		model.put("pageUtil", pageUtil);
		return "admin/site/batchPM.vm";
	}
	
	@RequestMapping("/admin/site/saveBatchPM.xhtml")
	public String saveBatchPM(HttpServletRequest request, ModelMap model){
		UserMessageAction userMessageAction = null;
		UserMessage userMessage = null;
		
		// 注: 该id为 UserMessage 的id
		String id = request.getParameter("id");
		if(StringUtils.isNotBlank(id)){
			userMessage = saveUserMessage(request, this.daoService.getObject(UserMessage.class, new Long(id)));
		}else{
			userMessage = saveUserMessage(request, new UserMessage(""));
			userMessageAction = new UserMessageAction(TagConstant.ADMIN_TOMEMBERID);
			userMessageAction.setFrommemberid(TagConstant.ADMIN_FROMMEMBERID);
			userMessageAction.setUsermessageid(userMessage.getId());
			userMessageAction.setAddtime(new Timestamp(System.currentTimeMillis()));
			if(userMessageAction.getGroupid()==null) { //新发表的情况
				userMessageAction.setGroupid(userMessage.getId());
			}
			daoService.saveObject(userMessageAction);
		}
		
		ErrorCode code = ErrorCode.getSuccess("操作成功");
		model.put("msg", code.getMsg());
		return showJsonSuccess(model);
	}
	private UserMessage saveUserMessage(HttpServletRequest request, UserMessage userMessage){
		BindUtils.bindData(userMessage, request.getParameterMap());
		return daoService.saveObject(userMessage);
	}
}
