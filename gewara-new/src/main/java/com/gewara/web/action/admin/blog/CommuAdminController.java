package com.gewara.web.action.admin.blog;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.criterion.Subqueries;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import com.gewara.constant.Status;
import com.gewara.constant.SysAction;
import com.gewara.constant.content.ManagerCheckConstant;
import com.gewara.json.ManageCheck;
import com.gewara.model.acl.GewaraUser;
import com.gewara.model.bbs.commu.Commu;
import com.gewara.model.bbs.commu.CommuManage;
import com.gewara.model.content.Picture;
import com.gewara.model.user.Album;
import com.gewara.model.user.Member;
import com.gewara.mongo.MongoService;
import com.gewara.service.bbs.CommuAdminService;
import com.gewara.service.bbs.UserMessageService;
import com.gewara.support.ServiceHelper;
import com.gewara.util.DateUtil;
import com.gewara.web.action.admin.BaseAdminController;
import com.gewara.web.util.PageUtil;

/**
 *    @function 圈子后台管理
 * 	@author bob.hu
 *		@date	2010-11-09 15:06:04
 */
@Controller
public class CommuAdminController extends BaseAdminController {
	
	@Autowired@Qualifier("commuAdminService")
	private CommuAdminService commuAdminService;
	public void setCommuAdminService(CommuAdminService commuAdminService) {
		this.commuAdminService = commuAdminService;
	}
	@Autowired@Qualifier("userMessageService")
	private UserMessageService userMessageService;
	public void setUserMessageService(UserMessageService userMessageService) {
		this.userMessageService = userMessageService;
	}
	@Autowired@Qualifier("mongoService")
	private MongoService mongoService;
	public void setMongoService(MongoService mongoService) {
		this.mongoService = mongoService;
	}
	
	@RequestMapping("/admin/audit/commu/searchCommu.xhtml")
	public String searchCommu(ModelMap model,Long commuid,String commuName,String nickName,String status,Integer pageNo){
		if(pageNo==null) pageNo=0;
		Integer maxNum=50;
		Integer from=pageNo*maxNum;
		Map mapCommu=commuAdminService.getCommuInfoList(commuid, commuName, nickName,status,from,maxNum);
		Integer count=commuAdminService.getCommuInfoCount(commuid, commuName, nickName, status);
		PageUtil pageUtil=new PageUtil(count,maxNum,pageNo,"admin/audit/commu/searchCommu.xhtml");
		Map params=new HashMap();
		params.put("commuid", commuid);
		params.put("commuName", commuName);
		params.put("nickName", nickName);
		params.put("status", status);
		pageUtil.initPageInfo(params);
		model.put("pageUtil",pageUtil);
		model.put("mapCommu",mapCommu);
		return "admin/commu/commuList.vm";
	}
	@RequestMapping("/admin/audit/commu/deleteCommu.xhtml")
	public String deleteCommu(ModelMap model,Long commuid){
		commuAdminService.updateCommuStatus(commuid,Status.N_DELETE);
		return showJsonSuccess(model);
	}
	
	@RequestMapping("/admin/audit/commu/deleteBatchCommu.xhtml")
	public String deleteBatchCommu(ModelMap model,String commuids){
		if(StringUtils.isNotBlank(commuids)){
			String[] commuidList = StringUtils.split(commuids, ",");
			for(String commuid : commuidList){
				if(StringUtils.isBlank(commuid)){
					continue;
				}
				long id = Long.parseLong(commuid);
				commuAdminService.updateCommuStatus(id,Status.N_DELETE);
			}
		}
		return showJsonSuccess(model);
	}
	
	@RequestMapping("/admin/audit/commu/cancelDeleteCommu.xhtml")
	public String cancelDeleteCommu(ModelMap model,Long commuid){
		try{
			commuAdminService.updateCommuStatus(commuid,Status.Y);
		}catch(Exception e){
			return showJsonError(model, "取消删除失败!");
		}
		return showJsonSuccess(model);
	}
	
	private void initApplyCommus(List<CommuManage> list){
		if(list == null || list.size() == 0) return;
		for(CommuManage commuManage : list){
			commuManage.setCommuname(daoService.getObject(Commu.class, commuManage.getCommuid()).getName());
		}
	}
	/**
	 *  申请圈子认证列表
	 */
	@RequestMapping("/admin/audit/commu/checkApplyCommuList.xhtml")
	public String checkApplyCommu(){
		return "admin/audit/commu/applytable.vm";
	}
	@RequestMapping("/admin/audit/commu/checkApplyCommu.xhtml")
	public String checkApplyCommu(ModelMap model, String status, Integer pageNo){
		if(pageNo==null) pageNo=0;
		Integer maxNum=20;
		Integer from=pageNo*maxNum;
		List<CommuManage> list = commuAdminService.getCommuManageListByStatus(status, from, maxNum);
		Integer count = commuAdminService.getCommuManageCount(status);
		PageUtil pageUtil=new PageUtil(count,maxNum,pageNo,"admin/audit/commu/checkApplyCommu.xhtml");
		Map params = new HashMap();
		params.put("status", status);
		pageUtil.initPageInfo(params);
		model.put("pageUtil",pageUtil);
		
		initApplyCommus(list);
		model.put("applylist", list);
		return "admin/audit/commu/applycontent.vm";
	}
	
	/**
	 *  更新圈子状态
	 */
	@RequestMapping("/admin/audit/commu/changeApplyStatus.xhtml")
	public String changeApplyStatus(ModelMap model, Long applyid, String status, String reason){
		CommuManage commuManage = daoService.getObject(CommuManage.class, applyid);
		if(commuManage == null){ return showJsonError_NOT_FOUND(model);}
		
		Commu commu = daoService.getObject(Commu.class, commuManage.getCommuid());
		String msg = "";
		if("Y".equals(status)){
			msg = "恭喜你！您的圈子【"+commu.getName()+"】认证已通过格瓦拉审核，现在起你可以发布收费活动和免费使用手机短信通知功能。在使用过程中遇到任何问题请联系本站客服 4000-406-506";
		}else if("N".equals(status)){
			msg = "很遗憾，您的圈子【"+commu.getName()+"】认证没有通过审核，原因是：<BR />" + reason + "<BR />如有任何疑问，请联系本站客服 4000-406-506。";
		}
		if(!"W".equals(status)){
			userMessageService.sendSiteMSG(commuManage.getApplymemberid(), SysAction.ACTION_COMMU_APPLYCERTIFICATION, commuManage.getId(), msg);
		}
		commuManage.setCheckstatus(status);
		daoService.saveObject(commuManage);
		// 更新圈子状态
		commu.setCheckstatus(status);
		daoService.saveObject(commu);
		return showJsonSuccess(model);
	}
	
	@RequestMapping("/admin/common/commuPicture.xhtml")
	public String checkCommuPicture(Long commuid, boolean check, Integer pageNo, ModelMap model){
		List<Picture> pictureList = new ArrayList<Picture>();
		if(pageNo == null)pageNo=0;
		int rowsPerPage = 30;
		if(commuid != null){
			int firstPage = rowsPerPage* pageNo;
			int count = Integer.valueOf(hibernateTemplate.findByCriteria(queryCommuPicture(commuid,check,null,null).setProjection(Projections.rowCount())).get(0)+"");
			pictureList = hibernateTemplate.findByCriteria(queryCommuPicture(commuid,check,null,null), firstPage, rowsPerPage);
			Map<Long, Object> pictureMap = new HashMap<Long, Object>();
			for (Picture picture : pictureList) {
				Album album = daoService.getObject(Album.class, picture.getRelatedid());
				if(album !=null)pictureMap.put(picture.getId(),daoService.getObject(Commu.class, album.getCommuid()));
			}
			PageUtil pageUtil = new PageUtil(count, rowsPerPage, pageNo, "admin/common/commuPicture.xhtml");
			Map params = new HashMap();
			params.put("commuid", commuid);
			pageUtil.initPageInfo(params);
			model.put("pictureList", pictureList);
			model.put("pageUtil", pageUtil);
			model.put("pictureMap", pictureMap);
		}else{
			model.put("lg", "Y");
			Date operationtime = null;
			Criteria criter1 = new Criteria("tag").is(ManagerCheckConstant.USER_ALBUM_PICTURE);
			Query query = new Query(criter1);
			List<ManageCheck> manageCheckList = mongoService.getObjectList(ManageCheck.class, query.getQueryObject(), "modifytime", false, 0, 1);
			if(!manageCheckList.isEmpty()){
				ManageCheck manageCheck = manageCheckList.get(0);
				model.put("manageCheck", manageCheck);
				operationtime = new Date(manageCheck.getModifytime());
			}
			int checkCount = 0;
			if(operationtime != null){	
				checkCount = Integer.valueOf(hibernateTemplate.findByCriteria(queryCommuPicture(commuid,true,operationtime,null).setProjection(Projections.rowCount())).get(0)+"");
			}
			int uncheckCount = Integer.valueOf(hibernateTemplate.findByCriteria(queryCommuPicture(commuid,false,operationtime,null).setProjection(Projections.rowCount())).get(0)+"");
			int checkPage = mongoService.getObjectCount(ManageCheck.class, query.getQueryObject());
			int uncheckPage = 0;
			if((uncheckCount % rowsPerPage) == 0){
				uncheckPage = uncheckCount / rowsPerPage;
			}else{
				uncheckPage = uncheckCount / rowsPerPage + 1;
			}
			if(check){
				List<ManageCheck> manageCheckUsers = mongoService.getObjectList(ManageCheck.class, query.getQueryObject(), "modifytime", true,pageNo, 1);
				if(!manageCheckUsers.isEmpty()){
					ManageCheck userCheck = manageCheckUsers.get(0);
					model.put("userCheck", userCheck);
					pictureList = hibernateTemplate.findByCriteria(queryCommuPicture(commuid,check,new Date(userCheck.getModifytime()),new Date(userCheck.getUnmodifytime().getTime())));
				}
			}else{
				pictureList = hibernateTemplate.findByCriteria(queryCommuPicture(commuid,check,operationtime,null), 0, rowsPerPage);
			}
			Map<Long, Object> pictureMap = new HashMap<Long, Object>();
			Map<Long,Member> memberMap = new HashMap<Long, Member>();
			for (Picture picture : pictureList) {
				Album album = daoService.getObject(Album.class, picture.getRelatedid());
				if(album !=null)pictureMap.put(picture.getId(),daoService.getObject(Commu.class, album.getCommuid()));
				if(picture.hasMemberType(GewaraUser.USER_TYPE_MEMBER)){
					memberMap.put(picture.getId(),daoService.getObject(Member.class, picture.getMemberid()));
				}
				operationtime=new Date(picture.getPosttime().getTime());
			}
			model.put("pageNo", pageNo);
			model.put("pictureMap",pictureMap);
			model.put("memberMap",memberMap);
			model.put("time", DateUtil.getCurDateMills(operationtime));
			model.put("pictureList",pictureList);
			model.put("checkPage", checkPage);
			model.put("uncheckPage",uncheckPage);
			model.put("checkCount", checkCount);
			model.put("check", check);
			model.put("uncheckCount",uncheckCount);
			model.put("tag",ManagerCheckConstant.USER_ALBUM_PICTURE);
		}
		addCacheMember(model, ServiceHelper.getMemberIdListFromBeanList(pictureList));
		return "admin/commu/commuPicture.vm";
	}
	
	
	private DetachedCriteria queryCommuPicture(Long commuid,boolean check,Date modifytime,Date undatetime) {
		DetachedCriteria query = DetachedCriteria.forClass(Picture.class, "p");
		query.add(Restrictions.eq("p.tag", "album"));
		DetachedCriteria subQuery = DetachedCriteria.forClass(Album.class, "a");
		subQuery.add(Restrictions.eqProperty("p.relatedid", "a.id"));
		if(commuid != null) subQuery.add(Restrictions.eq("a.commuid", commuid));
		else{
			subQuery.add(Restrictions.ne("a.commuid", 0L));
			if(modifytime != null){
				if(check){
					if(undatetime != null && !modifytime.equals(undatetime)){
						query.add(Restrictions.gt("p.posttime", undatetime));
					}
					query.add(Restrictions.le("p.posttime", modifytime));
				}else{
					query.add(Restrictions.gt("p.posttime", modifytime));
				}
			}
		} 
		subQuery.setProjection(Projections.property("a.id"));
		query.add(Subqueries.exists(subQuery));
		query.addOrder(Order.asc("p.posttime"));
		return query;
	}
}
