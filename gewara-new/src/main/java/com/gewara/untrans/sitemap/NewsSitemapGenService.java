package com.gewara.untrans.sitemap;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.stereotype.Service;

import com.gewara.util.DateUtil;



/**
 *    @function 新闻 站点地图 
 * 	@author bob.hu
 *		@date	2011-07-11 16:04:08
 */
@Service("newsSitemapGenService")
public class NewsSitemapGenService extends AbstractSitemapGenService implements SitemapGenService{
	public NewsSitemapGenService(){
		this.setFileName("/genfile/newssitemap.xml");
	}
	@Override
	public void writeUrlList(Writer writer) throws IOException{
		List<String> tags = new ArrayList<String>();
		tags.add("cinema");
		tags.add("theatre");
		tags.add("gym");
		tags.add("sport");
		
		String lastmod = DateUtil.formatDate(new Date());
		String loc;
		//0)
		for(String tag : tags){
			loc = siteUrl + "news/" + tag;
			this.writerUrl(writer, loc, lastmod, "always", "1.0");
		}
		// 1) News
		List<Long> newsList = hibernateTemplate.find("select c.id from News c order by c.addtime desc");
		for(Long newsid : newsList){
			loc = siteUrl + "news/" + newsid;
			this.writerUrl(writer, loc, lastmod, "daily", "0.8");
		}
	}
}
