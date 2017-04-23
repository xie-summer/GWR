package com.gewara.web.action.admin.ajax;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import com.gewara.model.acl.User;
import com.gewara.model.content.Video;
import com.gewara.model.movie.Movie;
import com.gewara.service.content.VideoService;
import com.gewara.untrans.SearchService;
import com.gewara.util.BeanUtil;
import com.gewara.util.BindUtils;
import com.gewara.util.ChangeEntry;
import com.gewara.util.IQiYiAuthUtils;
import com.gewara.util.JsonUtils;
import com.gewara.util.YoukuOAuthUtils;
import com.gewara.util.YoukuOAuthUtils.Page;
import com.gewara.web.action.admin.BaseAdminController;
import com.gewara.web.util.PageUtil;
import com.gewara.xmlbind.gym.RemoteCoach;

@Controller
public class AdminVideoAjaxController extends BaseAdminController{
	@Autowired@Qualifier("videoService")
	private VideoService videoService;
	
	public void setVideoService(VideoService videoService) {
		this.videoService = videoService;
	}
	
	@Autowired@Qualifier("searchService")
	private SearchService searchService;
	public void setSearchService(SearchService searchService) {
		this.searchService = searchService;
	}
	
	@RequestMapping("/admin/common/ajax/getVideoById.xhtml")
	public String getVideoById(Long videoId, ModelMap model) {
		Video video = daoService.getObject(Video.class, videoId);
		Map result = BeanUtil.getBeanMap(video);
		return showJsonSuccess(model, result);
	}

	@RequestMapping("/admin/common/ajax/saveOrUpdateVideo.xhtml")
	public String saveOrUpdateVideo(Long id,HttpServletRequest request, ModelMap model) {
		User user = getLogonUser();
		Video video = new Video(user.getId());
		if (id!=null) {
			video = daoService.getObject(Video.class, new Long(id));
			video.setUpdatetime(new Timestamp(System.currentTimeMillis()));
		}
		
		Map videoMap = request.getParameterMap();
		BindUtils.bindData(video, videoMap);
		video.setMemberid(user.getId());
		video.setVerifymemberid(user.getId());
		daoService.saveObject(video);
		ChangeEntry changeEntry = new ChangeEntry(video);
		monitorService.saveChangeLog(user.getId(), Video.class, video.getId(),changeEntry.getChangeMap( video));
		searchService.pushSearchKey(video);
		Map result = BeanUtil.getBeanMap(video);
		return showJsonSuccess(model, result);
	}
	
	@RequestMapping("/admin/common/ajax/removeVideoById.xhtml")
	public String removeVideoById(Long videoId, ModelMap model) {
		User user = getLogonUser();
		Video video = daoService.getObject(Video.class, videoId);
		daoService.removeObject(video);
		monitorService.saveDelLog(user.getId(), videoId, video);
		return showJsonSuccess(model);
	}
	
	@RequestMapping("/admin/common/ajax/updateVideoHotValue.xhtml")
	public String updateVideoHotValue(Long id, Integer value, ModelMap model) {
		Video video = daoService.getObject(Video.class, id);
		if(video==null) return showJsonError(model, "不存在该视频!");
		video.setHotvalue(value);
		daoService.updateObject(video);
		return showJsonSuccess(model);
	}
	@RequestMapping("/admin/common/ajax/updateVideoOrderIndex.xhtml")
	public String updateVideoOrderIndex(Long id, Integer value, ModelMap model) {
		Video video = daoService.getObject(Video.class, id);
		if(video==null) return showJsonError(model, "不存在该视频!");
		video.setOrderNum(value);
		daoService.updateObject(video);
		return showJsonSuccess(model);
	}
	
	
	
	
	@RequestMapping("/admin/common/ajax/youkuVideoByKeyword.xhtml")
	public String youkuOAuthByKeyword(String tag, Long relatedid, Integer pageNo, Integer rowsPerPage, String keyword, ModelMap model){
		if(pageNo == null) pageNo = 0;
		if(rowsPerPage == null) rowsPerPage = 10;
		Page page = YoukuOAuthUtils.getYoukuSearchesByKeyworld(keyword, pageNo + 1, rowsPerPage);
		Object baseObject = relateService.getRelatedObject(tag, relatedid);
		String name = null;
		if(baseObject == null) return show404(model, "请选择场馆！");
		if((baseObject.getClass()).equals(Movie.class)){
			name= ""+BeanUtil.get(baseObject, "moviename");
		}else if((baseObject.getClass()).equals(RemoteCoach.class)){
			name= ""+BeanUtil.get(baseObject, "coachname");
		}
		Integer count = page.getTotal();
		model.put("count", count);
		PageUtil pageUtil = new PageUtil(count, rowsPerPage, pageNo, "/admin/common/ajax/youkuVideoByKeyword.xhtml", true, true);
		Map params = new HashMap();
		params.put("tag", tag);
		params.put("relatedid", relatedid);
		params.put("keyword", keyword);
		pageUtil.initPageInfo(params);
		// tag + relatedid = 唯一确定已关联视频
		List<Video> videosList = videoService.getVideoListByTag(tag, relatedid, 0, 200);
		List<String> urlList = BeanUtil.getBeanPropertyList(videosList, String.class, "url", true);
		model.put("urlList", urlList);
		model.put("name", name);
		List videoList = page.getVideos();
		model.put("videoList", videoList);
		model.put("pageUtil", pageUtil);
		model.put("CLIENT_ID", YoukuOAuthUtils.CLIENT_ID);
		return "admin/common/youkuVideos.vm";
	}
	
	@RequestMapping("/admin/common/ajax/iqiyiVideoByKeyword.xhtml")
	public String iQiYiVideoByKeyword(String tag, Long relatedid, Integer pageNo, Integer rowsPerPage, String keyword,String threeCategory,String categoryIds, ModelMap model){
		if(pageNo == null) pageNo = 0;
		if(rowsPerPage == null) rowsPerPage = 20;
		String result = IQiYiAuthUtils.searchAlbum(keyword,threeCategory,0, 20,categoryIds);
		if(StringUtils.isNotBlank(result)){
			Map ablums = JsonUtils.readJsonToMap(result);
			if(ablums.get("code") != null && StringUtils.equals("E00004", ablums.get("code").toString())){
				model.put("error", ablums.get("message"));
				return show404(model, "未查询到数据");
			}
			List<Map> dataMap = new ArrayList<Map>();
			List<Map> datas = (List<Map>)ablums.get("data");
			for(Map map : datas){
				List<Integer> tvIds = (List<Integer>)map.get("tvIds");
				for(Integer tvId : tvIds){
					String vedioInfo = IQiYiAuthUtils.getVedioInfo(tvId + "");
					if(StringUtils.isNotBlank(vedioInfo)){
						Map vedioInfos = JsonUtils.readJsonToMap(vedioInfo);
						dataMap.add((Map)vedioInfos.get("data"));
					}
				}
			}
			model.put("videoList", dataMap);
			int count = (Integer)ablums.get("total");
			model.put("count", count);
			PageUtil pageUtil=new PageUtil(count,20,pageNo,"/admin/common/ajax/iqiyiVideoByKeyword.xhtml");
			Map<String,String> params = new HashMap<String,String>();
			params.put("tag", tag);
			params.put("relatedid", relatedid + "");
			params.put("keyword", keyword);
			pageUtil.initPageInfo(params);
			model.put("pageUtil", pageUtil);
		}
		// tag + relatedid = 唯一确定已关联视频
		List<Video> videosList = videoService.getVideoListByTag(tag, relatedid, 0, 200);
		List<String> urlList = BeanUtil.getBeanPropertyList(videosList, String.class, "url", true);
		model.put("urlList", urlList);
		model.put("relatedid", relatedid);
		if(StringUtils.isNotBlank(threeCategory) && StringUtils.equals("电影",threeCategory)){
			model.put("relationVideo", true);
		}
		return "admin/common/iQiYiVideos.vm";
	}
}
