package com.gewara.web.action.admin.blog;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import com.gewara.constant.Status;
import com.gewara.constant.content.OpenShareConstant;
import com.gewara.model.bbs.LinkShare;
import com.gewara.model.common.JsonData;
import com.gewara.untrans.ShareService;
import com.gewara.util.DateUtil;
import com.gewara.util.JsonUtils;
import com.gewara.web.action.admin.BaseAdminController;
import com.gewara.web.util.PageUtil;

@Controller
public class ShareAdminController extends BaseAdminController {
	@Autowired@Qualifier("shareService")
	private ShareService shareService;
	public void setShareService(ShareService shareService) {
		this.shareService = shareService;
	}
	@RequestMapping("/admin/blog/setUpShare.xhtml")
	public String setUpShare(){
		return "admin/blog/share/setupShare.vm";
	}
	
	@RequestMapping("/admin/blog/getShareInfo.xhtml")
	public String getShareInfo(String tag, ModelMap model){
		if(!OpenShareConstant.SHARETAGLIST.contains(tag)) return showJsonError(model, "非法参数！");
		List<JsonData> jsonDataList = daoService.getObjectListByField(JsonData.class, "tag", tag);
		model.put("jsonDataList", jsonDataList);
		model.put("tag", tag);
		return "admin/blog/share/shareInfo.vm";
	}
	
	@RequestMapping("/admin/blog/saveShareInfo.xhtml")
	public String getShareInfo(String tag, String content, ModelMap model){
		if(!OpenShareConstant.SHARETAGLIST.contains(tag)) return showJsonError(model, "非法参数！");
		if(StringUtils.isBlank(content)) return showJsonError(model, "内容不能为空！");
		if(content.length() > 100) return showJsonError(model, "内容不能大于100个字符！");
		List<JsonData> jsonDataList = daoService.getObjectListByField(JsonData.class, "tag", tag);
		JsonData jsonData = null;
		if(jsonDataList == null || jsonDataList.isEmpty()){
			jsonData = new JsonData();
			jsonData.setTag(tag);
			jsonData.setDkey(tag+DateUtil.currentTimeStr());
		}else if(jsonDataList.size() == 1){
			jsonData = jsonDataList.get(0);
		}else {
			return showJsonError(model, "数据出现错误，请联系技术人员！数据出现"+jsonDataList.size()+"条！");
		}
		Map dataMap = new HashMap();
		dataMap.put("updatetime", DateUtil.getCurFullTimestampStr());
		dataMap.put("content", content);
		jsonData.setValidtime(DateUtil.addDay(DateUtil.getCurFullTimestamp(), 365));
		jsonData.setData(JsonUtils.writeMapToJson(dataMap));
		daoService.saveObject(jsonData);
		return showJsonSuccess(model);
	}
	@RequestMapping("/admin/blog/searchShareHisList.xhtml")
	public String searchShareHisList(Timestamp starttime,Timestamp endtime,Integer pageNo,Integer maxPage,ModelMap model){
		String shareType ="sina";
		if(pageNo == null) pageNo = 0;
		if(maxPage == null) maxPage = 20;
		int firstRow = pageNo*maxPage;
		List<LinkShare> shareList =  shareService.searchShareSinaHisList(starttime, endtime, Status.Y,shareType, firstRow, maxPage);
		int count = shareService.searchShareCount(starttime, endtime, Status.Y,shareType);
		PageUtil pageUtil=new PageUtil(count,maxPage,pageNo,"/admin/blog/searchShareHisList.xhtml");
		Map params = new HashMap();
		params.put("starttime",starttime );
		params.put("endtime",endtime);
		params.put("maxPage",maxPage);
		pageUtil.initPageInfo(params);
		model.put("pageUtil", pageUtil);
		List<Long> idsList = new ArrayList();
		for(LinkShare share : shareList){
			idsList.add(share.getMemberid());
		}
		this.addCacheMember(model, idsList);
		model.put("starttime", starttime);
		model.put("endtime", endtime);
		model.put("maxPage", maxPage);
		model.put("shareList", shareList);
		return "admin/blog/share/sinaShareDetail.vm";
	}
}
