package com.gewara.web.action.community;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.ui.ModelMap;

import com.gewara.constant.sys.JsonDataKey;
import com.gewara.model.bbs.commu.Commu;
import com.gewara.model.bbs.commu.CommuMember;
import com.gewara.model.common.County;
import com.gewara.model.user.Member;
import com.gewara.service.JsonDataService;
import com.gewara.service.bbs.CommuService;
import com.gewara.support.ReadOnlyTemplate;
import com.gewara.support.ServiceHelper;
import com.gewara.web.action.BaseHomeController;

public class BaseCommuController extends BaseHomeController {
	@Autowired@Qualifier("readOnlyTemplate")
	private ReadOnlyTemplate readOnlyTemplate;
	public void setReadOnlyHibernateTemplate(ReadOnlyTemplate readOnlyTemplate) {
		this.readOnlyTemplate = readOnlyTemplate;
	}
	@Autowired@Qualifier("commuService")
	protected CommuService commuService;
	public void setCommuService(CommuService commuService) {
		this.commuService = commuService;
	}
	@Autowired@Qualifier("jsonDataService")
	protected JsonDataService jsonDataService;
	public void setJsonDataService(JsonDataService jsonDataService) {
		this.jsonDataService = jsonDataService;
	}
	
	protected Map getCommuCommonData(ModelMap modelmap, Commu commu, Member member){
		Map model = new HashMap();
		//圈子管理员信息
		Member adminMember = daoService.getObject(Member.class, commu.getAdminid());
		//圈子成员人数
		Integer commuMemberNum = commuService.getCommumemberCount(commu.getId(), null);
		//加入此圈子的还加入
		List<Commu> commuList = commuService.getAlikeCommuList(commu.getId(), 0, 6);
		model.put("commuList", commuList);

		//最近加入成员
		List<CommuMember> commuMemberList = commuService.getCommuMemberById(commu.getId(), null, null, "", 0, 6);
		addCacheMember(modelmap, ServiceHelper.getMemberIdListFromBeanList(commuMemberList));
		
		if(StringUtils.isNotBlank(commu.getCountycode())){
			//圈子所属的区
			DetachedCriteria query = DetachedCriteria.forClass(County.class);
			query.add(Restrictions.eq("countycode", commu.getCountycode()));
			List<County> list = readOnlyTemplate.findByCriteria(query, 0, 1);
			if(!list.isEmpty()) model.put("commuCounty",list.get(0));
		}
		// 检查当前圈子的状态
		String checkstatus = commuService.getCheckStatusByIDAndMemID(commu.getId());
		model.put("checkstatus", checkstatus);
		// 圈子颜色 + 布局
		Map<String, String> jsonDataColor = jsonDataService.getJsonData(JsonDataKey.KEY_COMMUCOLOR + commu.getId());
		if(jsonDataColor != null){
			model.put("jsonDataColor", jsonDataColor);
		}
		Map<String, String> jsonDataLayout = jsonDataService.getJsonData(JsonDataKey.KEY_COMMULAYOUT + commu.getId());
		if(jsonDataLayout != null){
			model.put("jsonDataLayout", jsonDataLayout);
		}
		model.put("commuBgpic", commu.getCommubg());
		model.put("commu", commu);
		model.put("commuMemberList", commuMemberList);
		
		model.put("commuMemberNum", commuMemberNum);
		model.put("adminMember", adminMember);

		Long memberid = null;
		if(member!=null){
			memberid = member.getId();
			boolean isCommuMember = commuService.isCommuMember(commu.getId(), member.getId());
			model.put("isCommuMember", isCommuMember);
			model.put("member", member);
			model.put("isAdmin", member.getId().equals(commu.getAdminid()));
			addCacheMember(modelmap, member.getId());
		}
		String purview = this.getCommuPurviewByMemberId(commu, memberid);
		model.put("purview", purview);
		
		model.put("logonMember", member);
		return model;
	}
	
	private String getCommuPurviewByMemberId(Commu commu, Long memberid) {
		String retval = "public";
		if(StringUtils.isNotBlank(commu.getVisitpermission())){
			if(Commu.COMMU_VISITPERMISSION_COMMUADMIN.equals(commu.getVisitpermission())){
				boolean isCommuAdmin=commuService.isCommuAdminByMemberid(commu.getId(), memberid);
				if(!isCommuAdmin)
					retval="admin";
			} else if(Commu.COMMU_VISITPERMISSION_COMMUMEMBER.equals(commu.getVisitpermission())){
				boolean isCommuMember=commuService.isCommuMember(commu.getId(), memberid);
				if(!isCommuMember)
					retval="member";
			}
		}
		return retval;
	}
}