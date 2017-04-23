package com.gewara.web.action.api2mobile.news;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import com.gewara.constant.ApiConstant;
import com.gewara.model.content.News;
import com.gewara.model.content.NewsPage;
import com.gewara.service.content.NewsService;
import com.gewara.web.action.api.BaseApiController;
@Controller
public class Api2NewsController extends BaseApiController{
	@Autowired@Qualifier("newsService")
	private NewsService newsService;
	@RequestMapping("/api2/news/newsList.xhtml")
	public String commentList(String citycode, String tag, Long relatedid, String category, Long categoryid, Integer from, Integer maxnum, ModelMap model){
		if(from == null) from = 0;
		if(maxnum == null || maxnum>100) maxnum = 20;
		List<News> newsList = new ArrayList<News>();
		if(StringUtils.isBlank(tag) && StringUtils.isBlank(category)) return getErrorXmlView(model, ApiConstant.CODE_DATA_ERROR,"缺少参数tag 或者category！");
		newsList = newsService.getNewsList(citycode, tag, relatedid, category, categoryid, null, from, maxnum);
		model.put("newsList", newsList);
		return getXmlView(model, "api2/news/newsList.vm");
	}
	@RequestMapping("/api2/news/newsDetail.xhtml")
	public String commentList(Long newsid, String isSimpleHtml, Integer width, Integer height, ModelMap model){
		if(newsid == null) return getErrorXmlView(model, ApiConstant.CODE_DATA_ERROR,"缺少参数newsid！");
		News news = daoService.getObject(News.class, newsid);
		if(news==null) return getErrorXmlView(model, ApiConstant.CODE_DATA_ERROR,"新闻不存在");
		String content = news.getContent();
		if(news.getPagesize()>1){
			for(int i=2;i<=news.getPagesize();i++){
				NewsPage np = newsService.getNewsPageByNewsidAndPageno(news.getId(), i);
				if(np!=null && StringUtils.isNotBlank(np.getContent())) {
					content = content + "<br /><br />" +np.getContent();
				}
			}
		}
		model.put("news", news);
		model.put("content", getSimpleHtmlContent(content, isSimpleHtml, width, height));
		return getXmlView(model, "api2/news/newsDetail.vm");
	}
}
