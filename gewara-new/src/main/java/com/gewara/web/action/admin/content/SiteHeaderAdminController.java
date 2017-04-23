package com.gewara.web.action.admin.content;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import com.gewara.constant.Flag;
import com.gewara.constant.TagConstant;
import com.gewara.constant.content.SignName;
import com.gewara.model.common.Relationship;
import com.gewara.model.content.GewaCommend;
import com.gewara.model.content.HeadInfo;
import com.gewara.support.ServiceHelper;
import com.gewara.util.BeanUtil;
import com.gewara.util.BindUtils;
import com.gewara.util.DateUtil;
import com.gewara.web.action.admin.BaseAdminController;
import com.gewara.web.util.PageUtil;

/**
 * 关联对象套头，如某部影院套头
 * @author gebiao(ge.biao@gewara.com)
 * @since Apr 25, 2013 8:33:53 PM
 */
@Controller
public class SiteHeaderAdminController extends BaseAdminController {
	//添加头部信息跳转
	@RequestMapping("/admin/site/header/addHeadInfo.xhtml")
	public String addHeaderInfo(HttpServletRequest request, ModelMap model){
		model.put("admincitycode", this.getAdminCitycode(request));
		return "admin/site/header/headInfo.vm";
	}
	//添加头部信息
	@RequestMapping("/admin/site/header/saveHeadInfo.xhtml")
	public String saveHeaderInfo(HttpServletRequest request,ModelMap model){
		Map map=request.getParameterMap();
		HeadInfo hi=new HeadInfo("");
		BindUtils.bindData(hi, map);
		try {
			daoService.saveObject(hi);
		} catch (Exception e) {
			return showJsonSuccess(model, "添加失败！");
		}
		Map result = new HashMap();
		result.put("id", hi.getId());
		return showJsonSuccess(model, result);
	}
	@RequestMapping("/admin/site/header/queryHeadRelated.xhtml")
	public String queryHeadRelated(Integer pageNo, String tag, ModelMap model) {
		if (pageNo == null)
			pageNo = 0;
		int rowsPerPage = 20;
		int firstPage = pageNo * rowsPerPage;
		int count = commonService.getRelationshipCount(Flag.FLAG_HEAD, null, tag, null, null);
		List<Relationship> reList = commonService.getRelationshipList(Flag.FLAG_HEAD, null, tag, null, null, firstPage, rowsPerPage);
		PageUtil pageUtil = new PageUtil(count, rowsPerPage, pageNo, "/admin/site/header/queryHeadRelated.xhtml");
		Map params = new HashMap();
		params.put("tag", tag);
		pageUtil.initPageInfo(params);
		model.put("reList", reList);
		model.put("pageUtil", pageUtil);
		return "admin/site/header/headRelatedList.vm";
	}
	@RequestMapping("/admin/site/header/getHeadInfo.xhtml")
	public String getHeadInfo(Long id, String board, HttpServletRequest request, ModelMap model){
		HeadInfo headInfo = daoService.getObject(HeadInfo.class, id);
		if(headInfo == null) return showJsonError(model, "该数据不存在或被删除！");
		String signName = SignName.INDEX_HEADINFO;
		if(StringUtils.equals(board, TagConstant.TAG_DRAMA)){
			signName = SignName.DRAMA_HEADINFO;
		}else if(StringUtils.equals(board, SignName.MOVIE_HEADINFO)){
			signName = board;
		}else if(StringUtils.equals(board, TagConstant.TAG_SPORT)){
			signName = SignName.SPORT_HEADINFO;
		}
		List<GewaCommend> gcHeadList = commonService.getGewaCommendList(getAdminCitycode(request), signName, null, HeadInfo.TAG, true, 0, 1);
		if(!gcHeadList.isEmpty()){
			model.put("gewaCommend", gcHeadList.get(0));
		}
		model.put("headInfo", headInfo);
		return "admin/site/header/headInfoCommend.vm";
	}
	
	//修改头部信息
	@RequestMapping("/admin/site/header/updateHeaderInfo.xhtml")
	public String updateHeaderInfo(ModelMap model,Long hid, HttpServletRequest request){
		HeadInfo hi=daoService.getObject(HeadInfo.class, hid);
		model.put("headInfo",hi);
		model.put("admincitycode", this.getAdminCitycode(request));
		return "admin/site/header/headInfo.vm";
	}

	@RequestMapping("/admin/site/header/saveHeadRelated.xhtml")
	public String saveHeadRelated(Timestamp validtime, HttpServletRequest request, ModelMap model) throws Exception {
		Map<String, String[]> requestMap = request.getParameterMap();
		String rid = ServiceHelper.get(requestMap, "id");
		Relationship relateionship = null;
		if (StringUtils.isNotBlank(rid)) {
			relateionship = daoService.getObject(Relationship.class, Long.valueOf(rid));
			if (relateionship == null)
				return showJsonError(model, "该数据不存在或被删除！");
		} else {
			relateionship = new Relationship();
			relateionship.setCategory(Flag.FLAG_HEAD);
			relateionship.setAddtime(DateUtil.getCurFullTimestamp());
			relateionship.setValidtime(DateUtil.addDay(DateUtil.getCurTruncTimestamp(), 45));
		}
		BindUtils.bindData(relateionship, requestMap);
		if (relateionship.getRelatedid1() == null)
			return showJsonError(model, "套头ID不能为空！");
		HeadInfo headInfo = daoService.getObject(HeadInfo.class, relateionship.getRelatedid1());
		if (headInfo == null)
			return showJsonError(model, "关联的套头ID不存在！");
		if (validtime != null)
			relateionship.setValidtime(validtime);
		if (StringUtils.isBlank(rid)) {
			String relatelist = ServiceHelper.get(requestMap, "relatedid2");
			if (relatelist == null)
				return showJsonError(model, "关联ID不能为空！");
			Set<String> reList = new HashSet<String>(Arrays.asList(relatelist.split(",")));
			if (reList.size() > 10)
				return showJsonError(model, "一次只能添加10条以内的数据！");
			List<Relationship> resList = new ArrayList<Relationship>();
			for (String string : reList) {
				if (StringUtils.isNotBlank(string)) {
					Long relateid = Long.valueOf(string);
					Object relate = relateService.getRelatedObject(relateionship.getTag(), relateid);
					Relationship res = commonService.getRelationship(Flag.FLAG_HEAD, relateionship.getTag(), relateid, null);
					if ((relate != null || "everyPoint".equals(relateionship.getTag())) && res == null) {
						Relationship resRelationship = new Relationship();
						resRelationship.setCategory(Flag.FLAG_HEAD);
						resRelationship.setAddtime(DateUtil.getCurFullTimestamp());
						resRelationship.setValidtime(DateUtil.addDay(DateUtil.getCurTruncTimestamp(), 45));
						PropertyUtils.copyProperties(resRelationship, relateionship);
						resRelationship.setRelatedid2(relateid);
						resList.add(resRelationship);
					}
				}
			}
			if (!resList.isEmpty()) {
				daoService.saveObjectList(resList);
				return showJsonSuccess(model);
			}
			return showJsonError(model, "没有新增数据！");
		} else {
			if (relateionship.getRelatedid2() == null)
				return showJsonError(model, "关联ID不能为空！");
			Object relate = relateService.getRelatedObject(relateionship.getTag(), relateionship.getRelatedid2());
			if (relate == null && !relateionship.getTag().equals("everyPoint"))
				return showJsonError(model, "对象不存在,请核实对象标识、ID");
			Relationship res = commonService.getRelationship(Flag.FLAG_HEAD, relateionship.getTag(), relateionship.getRelatedid2(), null);
			if (res != null && !res.getId().equals(relateionship.getId()))
				return showJsonError(model, "不能重复添加数据！");
			daoService.saveObject(relateionship);
			return showJsonSuccess(model);
		}
	}

	@RequestMapping("/admin/site/header/removeHeadRelated.xhtml")
	public String removeHeadRelated(Long rid, ModelMap model) {
		if (rid == null)
			return showJsonError(model, "参数错误！");
		Relationship relationship = daoService.getObject(Relationship.class, rid);
		if (relationship == null)
			return showJsonError(model, "对象不存在或被删除！");
		daoService.removeObject(relationship);
		return showJsonSuccess(model);
	}

	@RequestMapping("/admin/site/header/getHeadRelated.xhtml")
	public String getHeadRelated(Long rid, ModelMap model) {
		if (rid == null)
			return showJsonError(model, "参数错误！");
		Relationship relationship = daoService.getObject(Relationship.class, rid);
		if (relationship == null)
			return showJsonError(model, "对象不存在或被删除！");
		Map result = BeanUtil.getBeanMap(relationship);
		return showJsonSuccess(model, result);
	}
}
