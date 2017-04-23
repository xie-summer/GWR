package com.gewara.web.action.admin.ajax;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import com.gewara.constant.AdminCityContant;
import com.gewara.constant.TagConstant;
import com.gewara.constant.sys.ConfigConstant;
import com.gewara.model.common.GewaConfig;
import com.gewara.model.content.News;
import com.gewara.model.content.NewsPage;
import com.gewara.service.content.NewsService;
import com.gewara.support.ServiceHelper;
import com.gewara.untrans.SearchService;
import com.gewara.untrans.ShareService;
import com.gewara.util.BeanUtil;
import com.gewara.util.BindUtils;
import com.gewara.util.ChangeEntry;
import com.gewara.util.ValidateUtil;
import com.gewara.util.VmUtils;
import com.gewara.web.action.admin.BaseAdminController;

@Controller
public class AdminNewsAjaxController extends BaseAdminController{
	@Autowired@Qualifier("newsService")
	private NewsService newsService;
	public void setNewsService(NewsService newsService) {
		this.newsService = newsService;
	}
	@Autowired@Qualifier("shareService")
	private ShareService shareService;
	public void setShareService(ShareService shareService) {
		this.shareService = shareService;
	}
	
	@Autowired@Qualifier("searchService")
	private SearchService searchService;
	public void setSearchService(SearchService searchService) {
		this.searchService = searchService;
	}
	
	@RequestMapping("/admin/common/ajax/setNewsFlagValue.xhtml")
	public String setNewsFlagValue(Long newsId, String flag, ModelMap model) {
		News news = daoService.getObject(News.class, newsId);
		news.setFlag(flag);
		news.setUpdatetime(new Timestamp(System.currentTimeMillis()));
		daoService.saveObject(news);
		return showJsonSuccess(model);
	}
	@RequestMapping("/admin/common/ajax/validateNews.xhtml")
	public String validateNews(Long newsid, ModelMap model){
		String result = newsService.validateNews(newsid);
		return showJsonSuccess(model, result);
	}
	
	@RequestMapping("/admin/common/ajax/saveOrUpdateNews.xhtml")
	public String saveOrUpdateNews(Long id, HttpServletRequest request, ModelMap model) {
		Map<String, String[]> newsMap = request.getParameterMap();
		String summary = ServiceHelper.get(newsMap, "summary");
		if(VmUtils.getByteLength(summary) > 300) return showJsonError(model, "摘要最大支持150字符!");
		
		GewaConfig gewaconfig = daoService.getObject(GewaConfig.class, ConfigConstant.CFG_SPECIAL_CHAR);
		String spchar = gewaconfig.getContent();
		String newscontent = ServiceHelper.get(newsMap, "content");
		String msg = ValidateUtil.validateNewsContent(spchar, newscontent);
		String tmp = null;
		//验证乱码
		List<String> contentList = new ArrayList<String>();
		for (int i = 2; i <= 10; i++) {
			String tmpcount = ServiceHelper.get(newsMap, "content" + i);
			if (StringUtils.isNotBlank(tmpcount)){
				tmp = ValidateUtil.validateNewsContent(spchar, tmpcount);
				if(StringUtils.isNotBlank(tmp)) msg += "第"+i+"页:" + tmp;
				contentList.add(tmpcount);
			}
		}
		if(StringUtils.isNotBlank(msg)) return showJsonError(model, msg);
		News news = new News("");
		NewsPage pn = null;
		List<NewsPage> pnList = new ArrayList<NewsPage>();
		String citycode = null;
		if (id!=null) {
			news = daoService.getObject(News.class, id);
			citycode = news.getCitycode();
		}else{
			// V3.1.1 运动分站局部问题修改.doc 将默认关联城市设为全部。
			String tag = ServiceHelper.get(newsMap, "tag");
			if (StringUtils.equals(tag, TagConstant.TAG_SPORT)) {
				citycode = AdminCityContant.CITYCODE_ALL;
			} else {
				citycode = getAdminCitycode(request);
			}
			
			
		}
		ChangeEntry changeEntry = new ChangeEntry(news);
		BindUtils.bindData(news, newsMap);
		news.setUpdatetime(new Timestamp(System.currentTimeMillis()));
		if(news.getRelatedid()!=null && StringUtils.isNotBlank(news.getTag())) {
			Object object = relateService.getRelatedObject(news.getTag(), news.getRelatedid());
			if(object!=null) {
				String countycode = (String)BeanUtil.get(object, "countycode");
				if(StringUtils.isNotBlank(countycode)) news.setCountycode(countycode);
			}
		}
		news.setCitycode(citycode);
		daoService.saveObject(news);
		monitorService.saveChangeLog(getLogonUser().getId(), News.class, news.getId(),changeEntry.getChangeMap( news));
		if(id == null && ServiceHelper.get(newsMap, "bindwb").equals("on")){
			shareService.sendShareInfo("news", news.getId(), 1L, null);
		}
		int i = 0;
		for (String content : contentList) {
			pn =newsService.getNewsPageByNewsidAndPageno(news.getId(), i + 2);
			if (pn == null)
				pn = new NewsPage();
			pn.setContent(content);
			pn.setNewsid(news.getId());
			pn.setPageno(i + 2);
			pnList.add(pn);
			i++;
		}
		for (int j = i + 2; j <= 10; j++) {
			pn = newsService.getNewsPageByNewsidAndPageno(news.getId(), j);
			if (pn != null)
				daoService.removeObject(pn);
		}
		daoService.saveObjectList(pnList);
		searchService.pushSearchKey(news);
		return showJsonSuccess(model, news.getId()+"");
	}

	@RequestMapping("/admin/common/ajax/removeNewsById.xhtml")
	public String removeNewsById(Long newsId, ModelMap model) {
		News news = daoService.removeObjectById(News.class, newsId);
		daoService.removeObject(news);
		monitorService.saveDelLog(getLogonUser().getId(), newsId, news);
		return showJsonSuccess(model);
	}
}
